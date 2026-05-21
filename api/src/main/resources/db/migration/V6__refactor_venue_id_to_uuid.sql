-- ============================================================
-- Refactor venue.id: VARCHAR(64) → UUID (uuidv7 auto-generate)
-- ============================================================

-- Step 1: Drop FK from manifest → venue (sẽ re-add sau khi đổi type)
ALTER TABLE manifest DROP CONSTRAINT IF EXISTS manifest_venue_id_fkey;

-- Step 2: Alter venue.id từ VARCHAR(64) → UUID
-- Rows có UUID-formatted string được cast trực tiếp; rows khác (Ticketmaster-style) được assign random UUID.
-- In dev/test: chạy 'docker compose down -v' để reset nếu có dữ liệu không hợp lệ.
ALTER TABLE venue
    ALTER COLUMN id TYPE UUID
    USING CASE
        WHEN id ~ '^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$'
        THEN id::uuid
        ELSE gen_random_uuid()
    END;

ALTER TABLE venue ALTER COLUMN id SET DEFAULT uuidv7();

-- Step 3: Alter manifest.venue_id từ VARCHAR(64) → UUID
ALTER TABLE manifest
    ALTER COLUMN venue_id TYPE UUID
    USING CASE
        WHEN venue_id ~ '^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$'
        THEN venue_id::uuid
        ELSE gen_random_uuid()
    END;

-- Step 4: Re-add FK constraint
ALTER TABLE manifest
    ADD CONSTRAINT fk_manifest_venue_id
    FOREIGN KEY (venue_id) REFERENCES venue(id) ON DELETE CASCADE;
