-- ============================================================
-- Inventory schema (Plan 011)
-- ============================================================

CREATE TABLE ga_inventory (
    id         UUID PRIMARY KEY DEFAULT uuidv7(),
    event_id   UUID NOT NULL REFERENCES event(id) ON DELETE CASCADE,
    area_id    VARCHAR(64) NOT NULL, -- references ga_area(id)
    capacity   INTEGER NOT NULL,
    held       INTEGER NOT NULL DEFAULT 0,
    purchased  INTEGER NOT NULL DEFAULT 0,
    version    BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_ga_event_area UNIQUE (event_id, area_id)
);

CREATE INDEX idx_ga_inventory_event_id ON ga_inventory(event_id);

CREATE TABLE inventory_hold_place (
    id          UUID PRIMARY KEY DEFAULT uuidv7(),
    event_id    UUID NOT NULL REFERENCES event(id) ON DELETE CASCADE,
    seat_id     VARCHAR(64), -- references seat(id) (NULL for GA)
    area_id     VARCHAR(64), -- references ga_area(id) (NULL for Reserved Seating)
    quantity    INTEGER NOT NULL DEFAULT 1,
    hold_token  VARCHAR(64) NOT NULL,
    expires_at  TIMESTAMPTZ NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_event_seat_hold UNIQUE (event_id, seat_id)
);

CREATE INDEX idx_inventory_hold_place_token ON inventory_hold_place(hold_token);
CREATE INDEX idx_inventory_hold_place_expires ON inventory_hold_place(expires_at);
CREATE INDEX idx_inventory_hold_place_event ON inventory_hold_place(event_id);
