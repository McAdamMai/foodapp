#!/usr/bin/env bash
set -euo pipefail

PRIMARY_HOST=${PRIMARY_HOST:-host.docker.internal}
PRIMARY_PORT=${PRIMARY_PORT:-5432}

REPLICA_HOST=${REPLICA_HOST:-host.docker.internal}
REPLICA_PORT=${REPLICA_PORT:-5433}

DB_USER=${DB_USER:-admin}
DB_NAME=${DB_NAME:-promotion_db}

check_node() {
  local host=$1
  local port=$2
  local name=$3

  # Try connecting to the node. If connection works, check whether it's in recovery mode.
  if PGPASSWORD="$DB_PASSWORD" psql -h "$host" -p "$port" -U "$DB_USER" -d "$DB_NAME" -tAc "SELECT 1;" >/dev/null 2>&1; then
    local in_rec
    # Query pg_is_in_recovery() — returns 't' for standby, 'f' for primary.
    in_rec=$(PGPASSWORD="$DB_PASSWORD" psql -h "$host" -p "$port" -U "$DB_USER" -d "$DB_NAME" -tAc "SELECT pg_is_in_recovery();")

    # Log to stderr so it shows in console, but is NOT captured by $(...)
    echo "$name: UP, pg_is_in_recovery = $in_rec" >&2

    # Actual return value for the caller
    printf '%s\n' "$in_rec"
  else
    # Log to stderr
    echo "$name: DOWN" >&2

    # Return marker value
    printf 'DOWN\n'
  fi
}

echo "==== HA CHECK @ $(date) ====="

primary_state=$(check_node "$PRIMARY_HOST" "$PRIMARY_PORT" "PRIMARY")
replica_state=$(check_node "$REPLICA_HOST" "$REPLICA_PORT" "REPLICA")

echo "DEBUG: primary_state=[$primary_state], replica_state=[$replica_state]" >&2

# HA decision-making logic based on primary/replica state combinations
if [[ "$primary_state" == "f" && "$replica_state" == "t" ]]; then
  # Primary healthy, replica following normally
  echo "VERDICT: PRIMARY OK, REPLICA OK. NO ACTION."

elif [[ "$primary_state" == "DOWN" && "$replica_state" == "t" ]]; then
  # Primary unreachable but replica still in standby mode — safe to promote
  echo "VERDICT: PRIMARY DOWN, REPLICA IN RECOVERY. SAFE_TO_PROMOTE_REPLICA = TRUE"

elif [[ "$primary_state" == "f" && "$replica_state" == "DOWN" ]]; then
  # Replica unavailable, primary continues — replication needs to be rebuilt later
  echo "VERDICT: REPLICA DOWN, PRIMARY STILL PRIMARY. REBUILD REPLICA LATER."

elif [[ "$primary_state" == "f" && "$replica_state" == "f" ]]; then
  # Both nodes claim to be primary — extremely dangerous (split-brain)
  echo "VERDICT: SPLIT-BRAIN RISK!!! BOTH THINK THEY ARE PRIMARY. MANUAL INTERVENTION REQUIRED."

elif [[ "$primary_state" == "DOWN" && "$replica_state" == "DOWN" ]]; then
  # Both nodes unreachable — full outage
  echo "VERDICT: BOTH NODES DOWN. FULL OUTAGE. MANUAL RECOVERY REQUIRED."
else
  # Any other combination typically indicates an inconsistent or partial-failure state
  echo "VERDICT: UNKNOWN / COMPLEX STATE. MANUAL CHECK REQUIRED."
fi

echo
