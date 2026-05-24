-- ============================================================
-- Create offer_sale_window relational schema
-- ============================================================

CREATE TABLE offer_sale_window (
    id         UUID PRIMARY KEY DEFAULT uuidv7(),
    offer_id   UUID NOT NULL REFERENCES offer(id) ON DELETE CASCADE,
    type       VARCHAR(64) NOT NULL,
    start_at   TIMESTAMPTZ NOT NULL,
    end_at     TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_offer_sale_window_type UNIQUE (offer_id, type)
);

CREATE INDEX idx_offer_sale_window_offer_id ON offer_sale_window(offer_id);

-- Drop deprecated flat window columns from offer
ALTER TABLE offer DROP COLUMN IF EXISTS presale_start_at;
ALTER TABLE offer DROP COLUMN IF EXISTS presale_end_at;
ALTER TABLE offer DROP COLUMN IF EXISTS sale_start_at;
ALTER TABLE offer DROP COLUMN IF EXISTS sale_end_at;
