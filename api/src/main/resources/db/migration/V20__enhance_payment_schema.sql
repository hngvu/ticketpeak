-- ============================================================
-- Enhance Payment schema with safety constraints and indexes
-- ============================================================

-- Create index on account_id
CREATE INDEX idx_payment_account_id ON payment(account_id);

-- Add CHECK constraints for status and provider
ALTER TABLE payment
    ADD CONSTRAINT chk_payment_status
        CHECK (status IN ('PENDING','COMPLETED','FAILED','CANCELLED','EXPIRED','REFUNDED')),
    ADD CONSTRAINT chk_payment_provider
        CHECK (provider IN ('VNPAY','STRIPE'));

-- Change ON DELETE actions from CASCADE to RESTRICT for financial safety
ALTER TABLE payment DROP CONSTRAINT IF EXISTS payment_reservation_id_fkey;
ALTER TABLE payment ADD CONSTRAINT payment_reservation_id_fkey FOREIGN KEY (reservation_id) REFERENCES reservation(id) ON DELETE RESTRICT;

ALTER TABLE payment DROP CONSTRAINT IF EXISTS payment_account_id_fkey;
ALTER TABLE payment ADD CONSTRAINT payment_account_id_fkey FOREIGN KEY (account_id) REFERENCES account(id) ON DELETE RESTRICT;

ALTER TABLE payment DROP CONSTRAINT IF EXISTS payment_event_id_fkey;
ALTER TABLE payment ADD CONSTRAINT payment_event_id_fkey FOREIGN KEY (event_id) REFERENCES event(id) ON DELETE RESTRICT;
