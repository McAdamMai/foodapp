-- V4__modify_promotion_outbox.sql

-- 1. Remove the fields you requested
ALTER TABLE promotion_outbox
DROP COLUMN event_type,
DROP COLUMN occurred_at;

-- 2. (Recommended) Fix the typo from V3: 'chang_mask' -> 'change_mask'
-- If you don't fix this, your Java @Column(name="change_mask") will fail.
ALTER TABLE promotion_outbox
    RENAME COLUMN chang_mask TO change_mask;