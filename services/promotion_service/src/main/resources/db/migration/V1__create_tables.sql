-- 1. Day Template (The Cookie Cutter)
CREATE TABLE day_template (
                              id UUID PRIMARY KEY,
                              name TEXT NOT NULL,
                              description TEXT,
                              rule_json JSONB NOT NULL,
                              created_by TEXT,
                              created_at TIMESTAMPTZ DEFAULT NOW() -- Changed to TIMESTAMPTZ for timezone safety
);

-- 2. Promotion (The Active Instance)
CREATE TABLE promotion (
                           id UUID PRIMARY KEY,
                           template_id UUID, -- Optional reference to the original template

    -- Metadata
                           name TEXT NOT NULL,
                           description TEXT,
                           status TEXT NOT NULL, -- 'DRAFT', 'PUBLISHED', etc.
                           version INT DEFAULT 1,

    -- Schedule (Upgraded to TIMESTAMPTZ to match your JSON payload "2024-11-01T05:00:00Z")
                           start_date TIMESTAMPTZ,
                           end_date TIMESTAMPTZ,

    -- The Rules (The flattened configuration we discussed)
                           rule_json JSONB,

    -- Audit Info
                           created_at TIMESTAMPTZ DEFAULT NOW(),
                           updated_at TIMESTAMPTZ DEFAULT NOW(),
                           created_by TEXT,
                           reviewed_by TEXT,
                           published_by TEXT
);

-- 3. Promotion Outbox (The Event Queue)
CREATE TABLE promotion_outbox (
                                  id UUID PRIMARY KEY,
                                  aggregate_id UUID NOT NULL, -- The Promotion ID
                                  aggregate_version INT,      -- Helpful for debugging race conditions

    -- I kept event_type! It is highly recommended for the Poller to filter messages quickly.
                                  event_type TEXT NOT NULL,

                                  change_mask JSONB,          -- Fixed typo from 'chang_mask'
                                  payload JSONB NOT NULL,     -- The massive "ActivityDefinition" JSON

                                  created_at TIMESTAMPTZ DEFAULT NOW(), -- Replaced 'occurred_at' with standard naming
                                  published_at TIMESTAMPTZ    -- NULL means "Pending", Not Null means "Sent"
);

-- 4. Audit Log (History Tracking)
CREATE TABLE audit_log (
                           id UUID PRIMARY KEY,
                           entity_type TEXT NOT NULL,  -- e.g., 'PROMOTION'
                           entity_id UUID NOT NULL,
                           action TEXT NOT NULL,       -- e.g., 'PROMOTION_CREATED'
                           actor TEXT NOT NULL,        -- User ID
                           role TEXT,
                           entity_version INT,
                           fsm_event TEXT,
                           change_mask JSONB,
                           before_json JSONB,
                           after_json JSONB,
                           created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Indexes for performance
CREATE INDEX idx_promotion_status ON promotion(status);
CREATE INDEX idx_outbox_pending ON promotion_outbox(created_at) WHERE published_at IS NULL;
CREATE INDEX idx_audit_log_entity ON audit_log(entity_type, entity_id);