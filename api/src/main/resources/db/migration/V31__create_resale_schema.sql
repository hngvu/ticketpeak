CREATE TABLE resale_listing (
    id                            UUID            NOT NULL DEFAULT uuidv7() PRIMARY KEY,
    ticket_id                     UUID            NOT NULL,
    seller_id                     UUID            NOT NULL,
    event_id                      UUID            NOT NULL,
    offer_id                      UUID            NOT NULL,
    ticket_type_id                UUID            NOT NULL,
    ticket_type_name              VARCHAR(255)    NOT NULL,
    seat_id                       VARCHAR(64),
    area_id                       VARCHAR(64),
    seating_mode                  VARCHAR(32)     NOT NULL,
    face_value                    NUMERIC(19,2)   NOT NULL,
    asking_price                  NUMERIC(19,2)   NOT NULL,
    currency                      VARCHAR(3)      NOT NULL,
    status                        VARCHAR(16)     NOT NULL DEFAULT 'ACTIVE',
    reserved_until                TIMESTAMPTZ,
    version                       BIGINT          NOT NULL DEFAULT 0,
    created_at                    TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at                    TIMESTAMPTZ     NOT NULL DEFAULT now(),
    
    CONSTRAINT chk_resale_listing_status
        CHECK (status IN ('ACTIVE','RESERVED','SOLD','CANCELLED')),
    CONSTRAINT chk_resale_listing_seating_mode
        CHECK (seating_mode IN ('GENERAL_ADMISSION','RESERVED_SEATING'))
);

CREATE UNIQUE INDEX uq_resale_listing_ticket_active
    ON resale_listing(ticket_id)
    WHERE status IN ('ACTIVE', 'RESERVED');

CREATE INDEX idx_resale_listing_event_status ON resale_listing(event_id, status);
CREATE INDEX idx_resale_listing_seller       ON resale_listing(seller_id);
CREATE INDEX idx_resale_listing_ticket       ON resale_listing(ticket_id);

CREATE TABLE resale_order (
    id                    UUID          NOT NULL DEFAULT uuidv7() PRIMARY KEY,
    resale_listing_id     UUID          NOT NULL,
    buyer_id              UUID          NOT NULL,
    payment_id            UUID,
    asking_price          NUMERIC(19,2) NOT NULL,
    platform_fee_percent  NUMERIC(5,2)  NOT NULL,
    platform_fee_amount   NUMERIC(19,2) NOT NULL,
    net_amount            NUMERIC(19,2) NOT NULL,
    currency              VARCHAR(3)    NOT NULL,
    status                VARCHAR(16)   NOT NULL DEFAULT 'PENDING',
    version               BIGINT        NOT NULL DEFAULT 0,
    created_at            TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ   NOT NULL DEFAULT now(),
    
    CONSTRAINT chk_resale_order_status
        CHECK (status IN ('PENDING','CONFIRMED','FAILED','CANCELLED'))
);

CREATE INDEX idx_resale_order_listing ON resale_order(resale_listing_id);
CREATE INDEX idx_resale_order_buyer   ON resale_order(buyer_id);
