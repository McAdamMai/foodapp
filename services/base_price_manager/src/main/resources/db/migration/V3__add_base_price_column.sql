ALTER TABLE base_price_change_request
    ADD COLUMN IF NOT EXISTS base_price NUMERIC;

