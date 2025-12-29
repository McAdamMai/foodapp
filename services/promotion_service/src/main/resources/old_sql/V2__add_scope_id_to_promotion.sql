-- V2__add_scope_id_to_promotion.sql
ALTER TABLE promotion
ADD COLUMN scope_id UUID;