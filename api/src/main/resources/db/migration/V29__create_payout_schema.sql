CREATE TABLE payout_method (
    id                  UUID         NOT NULL DEFAULT uuidv7() PRIMARY KEY,
    account_id          UUID         NOT NULL,
    type                VARCHAR(32)  NOT NULL, -- BANK_TRANSFER | MOMO | ZALOPAY
    is_primary          BOOLEAN      NOT NULL DEFAULT FALSE,
    holder_name         VARCHAR(255) NOT NULL,
    bank_code           VARCHAR(16),           -- nullable, only required if type is BANK_TRANSFER
    account_number_enc  TEXT         NOT NULL, -- encrypted bank account/wallet identifier
    status              VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE', -- ACTIVE | REMOVED
    verified_at         TIMESTAMPTZ,
    version             BIGINT          NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    
    CONSTRAINT chk_payout_method_type   CHECK (type   IN ('BANK_TRANSFER','MOMO','ZALOPAY')),
    CONSTRAINT chk_payout_method_status CHECK (status IN ('ACTIVE','REMOVED'))
);

-- Enforce database-level uniqueness for only one active primary method per user
CREATE UNIQUE INDEX idx_payout_method_primary_account
    ON payout_method(account_id)
    WHERE is_primary = TRUE AND status = 'ACTIVE';

CREATE TABLE payout (
    id                      UUID            NOT NULL DEFAULT uuidv7() PRIMARY KEY,
    resale_listing_id       UUID,                     -- nullable until Resale module is fully complete
    seller_id               UUID            NOT NULL,
    payout_method_id        UUID            NOT NULL,
    payout_method_snapshot  JSONB           NOT NULL, -- Captures plain payout details at sale moment
    gross_amount            NUMERIC(19,2)   NOT NULL,
    platform_fee_percent    NUMERIC(5,2)    NOT NULL,
    platform_fee_amount     NUMERIC(19,2)   NOT NULL,
    net_amount              NUMERIC(19,2)   NOT NULL,
    currency                VARCHAR(3)      NOT NULL,
    status                  VARCHAR(16)     NOT NULL DEFAULT 'PENDING',
    scheduled_after         TIMESTAMPTZ     NOT NULL, -- Earliest eligibility release time
    processed_at            TIMESTAMPTZ,
    external_ref            VARCHAR(255),
    note                    TEXT,
    version                 BIGINT          NOT NULL DEFAULT 0,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    
    CONSTRAINT chk_payout_status CHECK (status IN ('PENDING','PROCESSING','PAID','FAILED','CANCELLED'))
);

CREATE INDEX idx_payout_payout_method ON payout(payout_method_id);
CREATE INDEX idx_payout_status_scheduled ON payout(status, scheduled_after);
CREATE INDEX idx_payout_seller ON payout(seller_id);
