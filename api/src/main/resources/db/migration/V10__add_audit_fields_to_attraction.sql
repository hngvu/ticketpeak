-- ============================================================
-- Add audit fields to attraction table (V10)
-- ============================================================

ALTER TABLE attraction ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT now();
ALTER TABLE attraction ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT now();
