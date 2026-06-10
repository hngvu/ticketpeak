-- Remove redundant position_y from seat_row
ALTER TABLE seat_row DROP COLUMN IF EXISTS position_y;
