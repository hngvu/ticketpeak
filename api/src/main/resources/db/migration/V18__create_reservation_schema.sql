-- ============================================================
-- Reservation schema
-- ============================================================

CREATE TABLE reservation (
    id          UUID        NOT NULL DEFAULT uuidv7() PRIMARY KEY,
    account_id  UUID        NOT NULL REFERENCES account(id) ON DELETE CASCADE,
    event_id    UUID        NOT NULL REFERENCES event(id)   ON DELETE CASCADE,
    status      VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    currency    VARCHAR(8)  NOT NULL,
    expires_at  TIMESTAMPTZ NOT NULL,
    version     BIGINT      NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_reservation_account_id ON reservation(account_id);
CREATE INDEX idx_reservation_event_id   ON reservation(event_id);
CREATE INDEX idx_reservation_status     ON reservation(status);
-- Partial index: expiry job only scans PENDING rows
CREATE INDEX idx_reservation_expires_pending
    ON reservation(expires_at)
    WHERE status = 'PENDING';

CREATE TABLE reservation_item (
    id              UUID           NOT NULL DEFAULT uuidv7() PRIMARY KEY,
    reservation_id  UUID           NOT NULL REFERENCES reservation(id) ON DELETE CASCADE,
    offer_id        UUID           NOT NULL,
    seating_mode    VARCHAR(32)    NOT NULL,
    area_id         VARCHAR(64),
    seat_id         VARCHAR(64),
    qty             INTEGER        NOT NULL DEFAULT 1,
    unit_face_value NUMERIC(19, 2) NOT NULL,
    currency        VARCHAR(8)     NOT NULL,
    charges         JSONB          NOT NULL DEFAULT '[]',

    CONSTRAINT ck_reservation_item_seating CHECK (
        (seating_mode = 'GENERAL_ADMISSION' AND area_id IS NOT NULL AND seat_id IS NULL  AND qty >= 1)
     OR (seating_mode = 'RESERVED_SEATING'  AND seat_id IS NOT NULL AND area_id IS NULL  AND qty = 1)
    )
);

CREATE INDEX idx_reservation_item_reservation_id ON reservation_item(reservation_id);
