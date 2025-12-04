#!/usr/bin/env bash
set -euo pipefail

echo "======================================="
echo " 🟦 FoodApp HA Bootstrap Starting..."
echo "======================================="

# --- Step 1. Start docker compose --------------------------------------
echo "➡️  Starting Docker Compose..."
docker compose down -v --remove-orphans || true
docker compose up -d

echo "⏳ Waiting 5 seconds for Postgres to boot..."
sleep 5

# --- Step 2. Create replication user on primary -------------------------
echo "➡️  Creating replication user on PRIMARY..."

docker exec -i postgres-primary \
  psql -U admin -d promotion_db -c \
  "DO \$\$ BEGIN
      CREATE ROLE replicator WITH REPLICATION LOGIN PASSWORD 'replicator_password';
     EXCEPTION WHEN duplicate_object THEN
      RAISE NOTICE 'replicator already exists';
     END \$\$;"

echo "➡️  Replicator ensured."

# --- Step 3. Update primary's pg_hba.conf ------------------------------
echo "➡️  Updating pg_hba.conf for replication..."

PRIMARY_CONF="/var/lib/postgresql/data/pg_hba.conf"

docker exec -i postgres-primary bash -c "
  echo 'host replication replicator 0.0.0.0/0 md5' >> $PRIMARY_CONF
"

echo "➡️  Reloading PRIMARY Postgres config..."
docker exec postgres-primary psql -U admin -d promotion_db -c "SELECT pg_reload_conf();"

# --- Step 4. Create basebackup directory --------------------------------
echo "➡️  Preparing replica basebackup directory..."
rm -rf replica-data
mkdir replica-data

# --- Step 5. Run pg_basebackup via temp client ---------------------------
echo '➡️  Running pg_basebackup from PRIMARY...'

docker run --rm \
  --network=host \
  -e PGPASSWORD=replicator_password \
  -v "$(pwd)/replica-data:/replica-data" \
  postgres:16 \
  pg_basebackup -h localhost -p 5432 -U replicator \
      -D /replica-data -Fp -Xs -P -R

echo "➡️  Basebackup complete."

# --- Step 6. Fix primary_conninfo host setting ---------------------------
echo "➡️  Fixing primary_conninfo in replica-data/postgresql.auto.conf..."

sed -i '' "s/host=localhost/host=host.docker.internal/" replica-data/postgresql.auto.conf

# --- Step 7. Start replica container -------------------------------------
echo "➡️  Starting replica container..."

docker rm -f postgres-replica || true

docker run -d \
  --name postgres-replica \
  -p 5433:5432 \
  -v "$(pwd)/replica-data:/var/lib/postgresql/data" \
  postgres:16

sleep 3

# --- Step 8. Check health -------------------------------------------------
echo "➡️  Checking replica state..."

docker exec postgres-replica psql -U admin -d promotion_db -tAc "SELECT pg_is_in_recovery();" || true

echo "➡️  Checking primary-side replication..."

docker exec postgres-primary \
  psql -U admin -d promotion_db \
  -c "SELECT client_addr, state, sync_state FROM pg_stat_replication;"

echo "======================================="
echo " 🟩 HA Bootstrap Complete!"
echo "   PRIMARY  : localhost:5432"
echo "   REPLICA  : localhost:5433"
echo "======================================="
