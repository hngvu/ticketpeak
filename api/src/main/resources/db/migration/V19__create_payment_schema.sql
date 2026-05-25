-- ============================================================
-- Payment schema
-- ============================================================

CREATE TABLE payment (
    id                     UUID           NOT NULL DEFAULT uuidv7() PRIMARY KEY,
    reservation_id         UUID           NOT NULL REFERENCES reservation(id) ON DELETE CASCADE,
    account_id             UUID           NOT NULL REFERENCES account(id)     ON DELETE CASCADE,
    event_id               UUID           NOT NULL REFERENCES event(id)       ON DELETE CASCADE,
    provider               VARCHAR(16)    NOT NULL,
    status                 VARCHAR(16)    NOT NULL,
    amount                 NUMERIC(19, 2) NOT NULL,
    currency               VARCHAR(8)     NOT NULL,
    gateway_transaction_id VARCHAR(256),
    gateway_payload        JSONB          NOT NULL DEFAULT '{}',
    gateway_response       JSONB          NOT NULL DEFAULT '{}',
    refunded_at            TIMESTAMPTZ,
    refunded_amount        NUMERIC(19, 2),
    created_at             TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at             TIMESTAMPTZ    NOT NULL DEFAULT now(),
    version                BIGINT         NOT NULL DEFAULT 0
);

CREATE INDEX idx_payment_reservation_id ON payment(reservation_id);
CREATE UNIQUE INDEX idx_payment_gateway_transaction_id ON payment(gateway_transaction_id) WHERE gateway_transaction_id IS NOT NULL;
CREATE UNIQUE INDEX idx_payment_completed_reservation ON payment(reservation_id) WHERE status = 'COMPLETED';
