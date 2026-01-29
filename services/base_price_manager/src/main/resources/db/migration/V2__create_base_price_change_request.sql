CREATE TABLE IF NOT EXISTS base_price_change_request (
                                                         id BIGSERIAL PRIMARY KEY,

    -- optimistic lock
                                                         version BIGINT NOT NULL DEFAULT 0,


                                                         status VARCHAR(32) NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
    );

CREATE INDEX IF NOT EXISTS idx_bpcr_status
    ON base_price_change_request(status);
