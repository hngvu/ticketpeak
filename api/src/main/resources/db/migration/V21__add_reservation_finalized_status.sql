-- ============================================================
-- Add FINALIZED status constraint to reservation table
-- ============================================================

-- Add a CHECK constraint to enforce valid reservation statuses
ALTER TABLE reservation
    ADD CONSTRAINT chk_reservation_status
    CHECK (status IN ('PENDING', 'CONFIRMED', 'FINALIZED', 'EXPIRED', 'CANCELLED'));
