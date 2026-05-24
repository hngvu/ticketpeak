-- ============================================================
-- Inventory schema (Plan 011: Standard Pure Read Model)
-- ============================================================

CREATE TABLE inventory_ga (
    event_id   UUID NOT NULL REFERENCES event(id) ON DELETE CASCADE,
    area_id    VARCHAR(64) NOT NULL, -- references ga_area(id)
    offer_id   UUID NOT NULL, -- references offer(id)
    total      INTEGER NOT NULL,
    available  INTEGER NOT NULL,
    held       INTEGER NOT NULL DEFAULT 0,
    sold       INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (event_id, area_id, offer_id)
);

CREATE INDEX idx_inventory_ga_event_id ON inventory_ga(event_id);

CREATE TABLE inventory_seat (
    event_id  UUID NOT NULL REFERENCES event(id) ON DELETE CASCADE,
    seat_id   VARCHAR(64) NOT NULL, -- references seat(id)
    offer_id  UUID, -- references offer(id) (nullable)
    status    VARCHAR(32) NOT NULL DEFAULT 'AVAILABLE', -- 'AVAILABLE', 'HELD', 'SOLD'
    PRIMARY KEY (event_id, seat_id)
);

CREATE INDEX idx_inventory_seat_event_id ON inventory_seat(event_id);
