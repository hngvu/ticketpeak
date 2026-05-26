-- Drop and recreate CHECK constraint with new status value
ALTER TABLE purchase_order DROP CONSTRAINT IF EXISTS chk_purchase_order_status;
ALTER TABLE purchase_order DROP CONSTRAINT IF EXISTS chk_order_status;

UPDATE purchase_order SET status = 'CONFIRMED' WHERE status = 'CREATED';

ALTER TABLE purchase_order
    ADD CONSTRAINT chk_purchase_order_status
    CHECK (status IN ('CONFIRMED','FAILED','CANCELLED','REFUNDED'));
