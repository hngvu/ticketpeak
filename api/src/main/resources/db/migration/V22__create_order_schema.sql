-- ============================================================
-- Order Schema
-- ============================================================

CREATE TABLE purchase_order (
    id             UUID           NOT NULL DEFAULT uuidv7() PRIMARY KEY,
    reservation_id UUID           NOT NULL UNIQUE REFERENCES reservation(id) ON DELETE RESTRICT,
    payment_id     UUID           NOT NULL UNIQUE REFERENCES payment(id) ON DELETE RESTRICT,
    account_id     UUID           NOT NULL REFERENCES account(id) ON DELETE RESTRICT,
    event_id       UUID           NOT NULL REFERENCES event(id) ON DELETE RESTRICT,
    status         VARCHAR(16)    NOT NULL,
    currency       VARCHAR(8)     NOT NULL,
    total_amount   NUMERIC(19, 2) NOT NULL,
    version        BIGINT         NOT NULL DEFAULT 0,
    created_at     TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ    NOT NULL DEFAULT now(),

    CONSTRAINT chk_purchase_order_status CHECK (status IN ('CREATED', 'FAILED', 'CANCELLED', 'REFUNDED'))
);

CREATE INDEX idx_purchase_order_account_id ON purchase_order(account_id);
CREATE INDEX idx_purchase_order_event_id   ON purchase_order(event_id);
CREATE INDEX idx_purchase_order_created_at ON purchase_order(created_at);

CREATE TABLE order_item (
    id               UUID           NOT NULL DEFAULT uuidv7() PRIMARY KEY,
    order_id         UUID           NOT NULL REFERENCES purchase_order(id) ON DELETE CASCADE,
    offer_id         UUID           NOT NULL,
    seating_mode     VARCHAR(32)    NOT NULL,
    area_id          VARCHAR(64),
    seat_id          VARCHAR(64),
    qty              INTEGER        NOT NULL DEFAULT 1,
    unit_face_value  NUMERIC(19, 2) NOT NULL,
    unit_total_price NUMERIC(19, 2) NOT NULL,
    line_total       NUMERIC(19, 2) NOT NULL,
    currency         VARCHAR(8)     NOT NULL,
    charges          JSONB          NOT NULL DEFAULT '[]',

    CONSTRAINT chk_order_item_seating CHECK (
        (seating_mode = 'GENERAL_ADMISSION' AND area_id IS NOT NULL AND seat_id IS NULL  AND qty >= 1)
     OR (seating_mode = 'RESERVED_SEATING'  AND seat_id IS NOT NULL AND area_id IS NULL  AND qty = 1)
    )
);

CREATE INDEX idx_order_item_order_id ON order_item(order_id);
