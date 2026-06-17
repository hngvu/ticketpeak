-- Add code column to offer
ALTER TABLE offer ADD COLUMN code VARCHAR(64);

-- Update existing offers with a fallback code (using id to ensure uniqueness)
UPDATE offer SET code = CAST(id AS VARCHAR(64)) WHERE code IS NULL;

-- Make code column NOT NULL
ALTER TABLE offer ALTER COLUMN code SET NOT NULL;

-- Add unique constraint per event
ALTER TABLE offer ADD CONSTRAINT uq_offer_event_code UNIQUE (event_id, code);

-- Create index for quick lookup
CREATE INDEX idx_offer_event_code ON offer(event_id, code);
