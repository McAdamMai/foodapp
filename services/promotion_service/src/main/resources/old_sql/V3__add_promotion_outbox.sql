-- V3__add_promotion_outbox.sql
CREATE TABLE promotion_outbox (
    id UUID PRIMARY KEY ,
    aggregate_id UUID NOT NULL ,
    aggregate_version INT NOT NULL ,
    event_type TEXT NOT NULL ,
    chang_mask JSONB NOT NULL ,
    payload JSONB NOT NULL ,
    occurred_at TIMESTAMPTZ NOT NULL ,
    published_at TIMESTAMPTZ
);