-- ============================================================
-- Add audit fields to attraction table safely (V12)
-- ============================================================

ALTER TABLE attraction ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT now();
ALTER TABLE attraction ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT now();
