-- V58__refactor_ticket_type_code.sql
-- Refactor ticket_type: replace slug with code, drop description

-- 1. Add new column `code` (nullable first to avoid constraint violation on existing rows)
ALTER TABLE ticket_type ADD COLUMN code VARCHAR(20);

-- 2. Migrate existing slug values to code (if any)
UPDATE ticket_type SET code = UPPER(REPLACE(slug, '-', '_'));

-- Fallback for any nulls (if any exist)
UPDATE ticket_type SET code = 'STANDARD' WHERE code IS NULL;

-- Make code column non-null
ALTER TABLE ticket_type ALTER COLUMN code SET NOT NULL;

-- 3. Update unique constraints (do this BEFORE dropping the slug column)
ALTER TABLE ticket_type DROP CONSTRAINT uq_ticket_type_event_slug;
ALTER TABLE ticket_type ADD CONSTRAINT uq_ticket_type_event_code UNIQUE (event_id, code);

-- 4. Drop old columns
ALTER TABLE ticket_type DROP COLUMN slug;
ALTER TABLE ticket_type DROP COLUMN description;
