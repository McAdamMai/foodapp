

-- Creates an audit log table to record "who did what to which entity and when".
CREATE TABLE audit_log (
                           id UUID PRIMARY KEY,
                           entity_type TEXT NOT NULL,          -- e.g., PROMOTION, TEMPLATE
                           entity_id UUID NOT NULL,            -- promotion_id / template_id
                           action TEXT NOT NULL,               -- e.g., PROMOTION_APPROVE
                           actor TEXT NOT NULL,                -- userId
                           role TEXT,                          -- optional: CREATOR/REVIEWER/PUBLISHER/ADMIN
                           entity_version INT,                 -- optional: promotion version for debugging
                           fsm_event TEXT,                     -- optional: SUBMIT/APPROVE/...
                           change_mask JSONB,                  -- optional: ["STATUS","DATEs","RULES"]
                           before_json JSONB,                  -- optional: snapshot before change
                           after_json JSONB,                   -- optional: snapshot after change
                           created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_log_entity ON audit_log(entity_type, entity_id);
CREATE INDEX idx_audit_log_created_at ON audit_log(created_at);
