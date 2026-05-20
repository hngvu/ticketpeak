-- Convert venue.id from VARCHAR(64) to UUID and update manifest.venue_id accordingly.
-- PostgreSQL 18+ required for uuidv7().

-- 1. Drop FK constraint on manifest.venue_id
ALTER TABLE manifest DROP CONSTRAINT manifest_venue_id_fkey;

-- 2. Convert venue.id to UUID
ALTER TABLE venue ALTER COLUMN id TYPE UUID USING (md5(id)::uuid);
ALTER TABLE venue ALTER COLUMN id SET DEFAULT uuidv7();

-- 3. Convert manifest.venue_id to UUID
ALTER TABLE manifest ALTER COLUMN venue_id TYPE UUID USING (md5(venue_id)::uuid);

-- 4. Re-add FK constraint
ALTER TABLE manifest ADD CONSTRAINT manifest_venue_id_fkey FOREIGN KEY (venue_id) REFERENCES venue (id) ON DELETE CASCADE;
