-- ========================================================
-- 1. Expander Tracker (The "State" Machine)
-- ========================================================
-- This table tracks how far we have expanded each promotion.
-- It uses DATE types to avoid "Midnight/Timezone" bugs.

CREATE TABLE expander_tracker (
                                  promotion_id UUID PRIMARY KEY,

    -- Idempotency & Versioning
                                  last_processed_version INT NOT NULL,
                                  updated_at TIMESTAMPTZ DEFAULT NOW(),

    -- Rolling Window Logic (Business Dates, NOT Timestamps)
    -- We use DATE because "Jan 20" is "Jan 20" regardless of timezone.
                                  valid_start_date DATE NOT NULL,
                                  valid_end_date DATE NOT NULL,

    -- The Cursor: "How many days into the future have we built?"
    -- NULL means we haven't started expanding yet (Scenario 1)
                                  covered_until_date DATE, -- NULL = Not started yet

                                  status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, PAUSED, COMPLETED

    -- The Rules Payload (Merged)
                                  rules_json JSONB NOT NULL
);

-- ========================================================
-- 2. Time Slice (The Partitioned Inventory)
-- ========================================================
-- This table holds the actual exploded availability.
-- It is partitioned by DATE to allow O(1) cleanup.

CREATE TABLE time_slice (
                            id UUID NOT NULL,
                            promotion_id UUID NOT NULL,
                            version INT NOT NULL,

    -- The Partition Key (Business Date)
    -- This allows us to say "DROP January" instantly.
                            slice_date DATE NOT NULL,

    -- Execution Times (Absolute UTC)
                            start_time TIMESTAMPTZ NOT NULL,
                            end_time TIMESTAMPTZ NOT NULL,

    -- Context
                            timezone TEXT NOT NULL,       -- e.g. 'America/New_York'
                            effect_type TEXT NOT NULL,    -- e.g. 'DISCOUNT'
                            effect_value DOUBLE PRECISION NOT NULL,

    -- CONSTRAINT: Partition key MUST be part of PK
                            PRIMARY KEY (id, slice_date),

    -- CONSTRAINT: Ensure we don't overlap slices for the same Zone + Time
    -- Note: We include promotion_id to ensure scope.
                            UNIQUE (promotion_id, timezone, start_time, slice_date)
) PARTITION BY RANGE (slice_date);

-- ========================================================
-- 3. Indices
-- ========================================================
-- Indices on the parent table automatically propagate to partitions.

-- Critical for: "Find all slices for Promotion X" (e.g. for Full Rebuild)
CREATE INDEX idx_time_slice_promo_date
    ON time_slice (promotion_id, slice_date);

-- Critical for: "User Query: Show me deals for Today in NY"
CREATE INDEX idx_time_slice_date_zone
    ON time_slice (slice_date, timezone);

-- Index for the Nightly Roller Job
-- "Find me active promotions that need extension"
CREATE INDEX idx_tracker_rolling_window
    ON expansion_tracker (status, valid_end_date, covered_until_date);

-- ========================================================
-- 4. Partitions (Buckets)
-- ========================================================
-- You need to create these ahead of time (e.g., via pg_partman or Scheduler)
-- Here are examples for the immediate future.

-- Default partition catches any dates we forgot to create buckets for (Safety Net)
CREATE TABLE time_slice_default PARTITION OF time_slice DEFAULT;

-- Bucket 1: January 2026
CREATE TABLE time_slice_2026_01 PARTITION OF time_slice
    FOR VALUES FROM ('2026-01-01') TO ('2026-02-01');

-- Bucket 2: February 2026
CREATE TABLE time_slice_2026_02 PARTITION OF time_slice
    FOR VALUES FROM ('2026-02-01') TO ('2026-03-01');

-- Bucket 3: March 2026
CREATE TABLE time_slice_2026_03 PARTITION OF time_slice
    FOR VALUES FROM ('2026-03-01') TO ('2026-04-01');