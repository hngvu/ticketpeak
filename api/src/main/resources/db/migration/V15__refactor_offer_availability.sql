-- ============================================================
-- Refactor Offer availability fields (Plan 011 Prep)
-- ============================================================

ALTER TABLE offer RENAME COLUMN quantity_available TO capacity_cap;
ALTER TABLE offer DROP COLUMN quantity_sold;
