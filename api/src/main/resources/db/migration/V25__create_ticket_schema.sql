CREATE TABLE ticket (
    id                UUID         NOT NULL DEFAULT uuidv7() PRIMARY KEY,
    order_id          UUID         NOT NULL REFERENCES purchase_order(id) ON DELETE RESTRICT,
    order_item_id     UUID         NOT NULL REFERENCES order_item(id)     ON DELETE RESTRICT,
    account_id        UUID         NOT NULL REFERENCES account(id)        ON DELETE RESTRICT,
    event_id          UUID         NOT NULL REFERENCES event(id)          ON DELETE RESTRICT,
    offer_id          UUID         NOT NULL,
    ticket_type_id    UUID         NOT NULL,
    ticket_type_name  VARCHAR(255) NOT NULL,
    offer_name        VARCHAR(255) NOT NULL,
    face_value        DECIMAL(10,2) NOT NULL,
    currency          VARCHAR(3)   NOT NULL,
    seating_mode      VARCHAR(32)  NOT NULL,
    area_id           VARCHAR(64),
    seat_id           VARCHAR(64),
    totp_secret_enc   TEXT         NOT NULL,
    status            VARCHAR(16)  NOT NULL DEFAULT 'ISSUED',
    checked_in_at     TIMESTAMPTZ,
    version           BIGINT       NOT NULL DEFAULT 0,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_ticket_status
        CHECK (status IN ('ISSUED','TRANSFERRED','CHECKED_IN','VOID')),
    CONSTRAINT chk_ticket_seating_mode
        CHECK (seating_mode IN ('GENERAL_ADMISSION','RESERVED_SEATING'))
);

CREATE INDEX idx_ticket_account_id  ON ticket(account_id);
CREATE INDEX idx_ticket_order_id    ON ticket(order_id);
CREATE INDEX idx_ticket_event_id    ON ticket(event_id);

CREATE TABLE ticket_transfer (
    id            UUID        NOT NULL DEFAULT uuidv7() PRIMARY KEY,
    ticket_id     UUID        NOT NULL REFERENCES ticket(id) ON DELETE RESTRICT,
    sender_id     UUID        NOT NULL REFERENCES account(id) ON DELETE RESTRICT,
    recipient_id  UUID        NOT NULL REFERENCES account(id) ON DELETE RESTRICT,
    status        VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    expires_at    TIMESTAMPTZ NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_transfer_status
        CHECK (status IN ('PENDING','ACCEPTED','CANCELLED'))
);

CREATE INDEX idx_ticket_transfer_ticket_id    ON ticket_transfer(ticket_id);
CREATE INDEX idx_ticket_transfer_recipient_id ON ticket_transfer(recipient_id);
