-- 1. Make section ID unique globally
UPDATE section SET id = manifest_id || '-' || id;

-- 2. Update references to section.id
UPDATE ga_area SET section_id = manifest_id || '-' || section_id WHERE section_id IS NOT NULL;
UPDATE rs_area SET section_id = manifest_id || '-' || section_id WHERE section_id IS NOT NULL;

UPDATE seat
SET section_id = r.manifest_id || '-' || seat.section_id
FROM seat_row sr
JOIN rs_area r ON sr.rs_area_id = r.id
WHERE seat.row_id = sr.id AND seat.section_id IS NOT NULL;

-- 3. Add columns to section
ALTER TABLE section ADD COLUMN type VARCHAR(32) NOT NULL DEFAULT 'RS';
ALTER TABLE section ADD COLUMN level_id VARCHAR(64);
ALTER TABLE section ADD COLUMN capacity INTEGER DEFAULT 0;
ALTER TABLE section ADD COLUMN price_level_id VARCHAR(64);
ALTER TABLE section ADD COLUMN ui_data JSONB;

-- 4. Migrate data from rs_area and ga_area to section
UPDATE section s
SET type = 'RS',
    level_id = r.level_id,
    ui_data = r.polygon
FROM rs_area r
WHERE s.id = r.section_id;

UPDATE section s
SET type = 'GA',
    level_id = g.level_id,
    price_level_id = g.price_level_id,
    capacity = g.capacity,
    ui_data = g.polygon
FROM ga_area g
WHERE s.id = g.section_id;

-- 5. Update seat_row to point to section_id
ALTER TABLE seat_row DROP CONSTRAINT IF EXISTS seat_row_rs_area_id_fkey;

UPDATE seat_row sr
SET rs_area_id = r.section_id
FROM rs_area r
WHERE sr.rs_area_id = r.id;

ALTER TABLE seat_row RENAME COLUMN rs_area_id TO section_id;

-- 6. Update inventory_ga
UPDATE inventory_ga ig
SET area_id = g.section_id
FROM ga_area g
WHERE ig.area_id = g.id;

ALTER TABLE inventory_ga RENAME COLUMN area_id TO section_id;

-- 7. Drop areas
DROP TABLE ga_area;
DROP TABLE rs_area;

-- 8. Alter section primary key
ALTER TABLE section DROP CONSTRAINT IF EXISTS section_pkey;
ALTER TABLE section ADD PRIMARY KEY (id);

-- 9. Drop old UI columns from section
ALTER TABLE section DROP COLUMN IF EXISTS polygon;
ALTER TABLE section DROP COLUMN IF EXISTS label_x;
ALTER TABLE section DROP COLUMN IF EXISTS label_y;

-- 10. Add foreign key back
ALTER TABLE seat_row ADD CONSTRAINT seat_row_section_id_fkey FOREIGN KEY (section_id) REFERENCES section (id) ON DELETE CASCADE;

-- 11. Update downstream tables containing area_id
ALTER TABLE reservation_item RENAME COLUMN area_id TO section_id;
ALTER TABLE order_item RENAME COLUMN area_id TO section_id;
ALTER TABLE ticket RENAME COLUMN area_id TO section_id;
ALTER TABLE resale_listing RENAME COLUMN area_id TO section_id;
