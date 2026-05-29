-- ============================================================
-- V37 — Add color configuration to venue lookup tables
-- ============================================================

ALTER TABLE venue_level ADD COLUMN color VARCHAR(32);
ALTER TABLE section ADD COLUMN color VARCHAR(32);
ALTER TABLE price_level ADD COLUMN color VARCHAR(32);
