-- Remove unused fields from Section
ALTER TABLE section DROP COLUMN IF EXISTS description;
ALTER TABLE section DROP COLUMN IF EXISTS price_level_id;

-- Remove unused fields from Seat
ALTER TABLE seat DROP COLUMN IF EXISTS accessibility;
ALTER TABLE seat DROP COLUMN IF EXISTS obstructed_view;
ALTER TABLE seat DROP COLUMN IF EXISTS aisle;
