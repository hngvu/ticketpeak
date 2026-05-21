-- ============================================================
-- Offer domain schema (Plan 010)
-- ============================================================

CREATE TABLE offer (
    id                     UUID PRIMARY KEY DEFAULT uuidv7(),
    event_id               UUID NOT NULL REFERENCES event(id) ON DELETE CASCADE,
    ticket_type_id         VARCHAR(255) NOT NULL UNIQUE,
    name                   VARCHAR(255) NOT NULL,
    description            TEXT,
    currency               VARCHAR(8) NOT NULL,
    face_value             NUMERIC(19,2) NOT NULL,
    restricted_payment     BOOLEAN NOT NULL DEFAULT false,
    event_ticket_minimum   INTEGER NOT NULL,
    sellable_quantities    JSONB NOT NULL,
    presale_start_at       TIMESTAMPTZ,
    presale_end_at         TIMESTAMPTZ,
    sale_start_at          TIMESTAMPTZ,
    sale_end_at            TIMESTAMPTZ,
    seating_mode           VARCHAR(32) NOT NULL,
    section_id             VARCHAR(64),
    price_level_id         VARCHAR(64),
    charges                JSONB NOT NULL DEFAULT '[]'::jsonb,
    created_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_offer_face_value_non_negative CHECK (face_value >= 0),
    CONSTRAINT ck_offer_event_ticket_minimum_positive CHECK (event_ticket_minimum > 0)
);

CREATE INDEX idx_offer_event_id ON offer(event_id);
CREATE INDEX idx_offer_event_ticket_type_id ON offer(event_id, ticket_type_id);
