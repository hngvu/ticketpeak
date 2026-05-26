ALTER TABLE event
    ADD COLUMN service_fee_percent NUMERIC(5,2) NOT NULL DEFAULT 0.00;

COMMENT ON COLUMN event.service_fee_percent IS
    'Platform service fee (%) deducted from resale gross amount. Snapshot vào payout tại thời điểm sold.';
