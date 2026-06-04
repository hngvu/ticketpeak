-- ============================================================
-- V42 — Add editable geometry to venue areas
-- ============================================================

ALTER TABLE ga_area ADD COLUMN x INTEGER;
ALTER TABLE ga_area ADD COLUMN y INTEGER;
ALTER TABLE ga_area ADD COLUMN width INTEGER;
ALTER TABLE ga_area ADD COLUMN height INTEGER;

ALTER TABLE rs_area ADD COLUMN x INTEGER;
ALTER TABLE rs_area ADD COLUMN y INTEGER;
ALTER TABLE rs_area ADD COLUMN width INTEGER;
ALTER TABLE rs_area ADD COLUMN height INTEGER;
