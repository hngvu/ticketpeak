ALTER TABLE seat ADD COLUMN position_y INTEGER;

UPDATE seat s
SET position_y = COALESCE(r.position_y, 0)
FROM seat_row r
WHERE s.row_id = r.id;

ALTER TABLE seat ALTER COLUMN position_y SET NOT NULL;
