-- ============================================================
-- Inventory schema (Plan 011 Refactored: Pure Read Model)
-- ============================================================

CREATE TABLE ga_inventory (
    id         UUID PRIMARY KEY DEFAULT uuidv7(),
    event_id   UUID NOT NULL REFERENCES event(id) ON DELETE CASCADE,
    area_id    VARCHAR(64) NOT NULL, -- references ga_area(id)
    capacity   INTEGER NOT NULL,
    sold       INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT uq_ga_event_area UNIQUE (event_id, area_id)
);

CREATE INDEX idx_ga_inventory_event_id ON ga_inventory(event_id);

CREATE TABLE inventory_seat (
    id        UUID PRIMARY KEY DEFAULT uuidv7(),
    event_id  UUID NOT NULL REFERENCES event(id) ON DELETE CASCADE,
    seat_id   VARCHAR(64) NOT NULL, -- references seat(id)
    status    VARCHAR(32) NOT NULL DEFAULT 'AVAILABLE', -- 'AVAILABLE', 'SOLD'
    CONSTRAINT uq_event_seat UNIQUE (event_id, seat_id)
);

CREATE INDEX idx_inventory_seat_event_id ON inventory_seat(event_id);
