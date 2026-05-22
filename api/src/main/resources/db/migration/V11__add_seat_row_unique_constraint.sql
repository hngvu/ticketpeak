-- ============================================================
-- Add unique constraint to seat_row (V11)
-- ============================================================

ALTER TABLE seat_row ADD CONSTRAINT uq_seat_row_area_name UNIQUE (rs_area_id, name);
