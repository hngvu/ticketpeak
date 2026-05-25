-- ============================================================
-- Fix Order Schema: Remove global UNIQUE from reservation_id
-- ============================================================

ALTER TABLE purchase_order DROP CONSTRAINT purchase_order_reservation_id_key;
ALTER TABLE purchase_order DROP CONSTRAINT purchase_order_payment_id_key;

CREATE UNIQUE INDEX idx_purchase_order_unique_active_reservation
    ON purchase_order(reservation_id)
    WHERE status IN ('CREATED', 'CANCELLED', 'REFUNDED');

CREATE UNIQUE INDEX idx_purchase_order_unique_active_payment
    ON purchase_order(payment_id)
    WHERE status IN ('CREATED', 'CANCELLED', 'REFUNDED');
