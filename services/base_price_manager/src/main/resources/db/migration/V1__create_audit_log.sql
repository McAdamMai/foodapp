CREATE TABLE IF NOT EXISTS audit_log (
                                         id BIGSERIAL PRIMARY KEY,
                                         entity_type VARCHAR(64) NOT NULL,
    entity_id BIGINT NOT NULL,
    entity_version BIGINT NOT NULL,
    action VARCHAR(64) NOT NULL,
    actor VARCHAR(128) NOT NULL,
    role VARCHAR(64),
    fsm_event VARCHAR(64),
    before_json TEXT,
    after_json TEXT,
    created_at TIMESTAMPTZ NOT NULL
    );

CREATE INDEX IF NOT EXISTS idx_audit_log_entity
    ON audit_log(entity_type, entity_id);

CREATE INDEX IF NOT EXISTS idx_audit_log_created_at
    ON audit_log(created_at);
