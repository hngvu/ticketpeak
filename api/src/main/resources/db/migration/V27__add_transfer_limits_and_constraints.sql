ALTER TABLE event
    ADD COLUMN max_transfer_count INT NOT NULL DEFAULT 0;

ALTER TABLE ticket
    ADD COLUMN transfer_count INT NOT NULL DEFAULT 0;

-- Prevent race condition (double-click creates duplicate pending transfers)
CREATE UNIQUE INDEX idx_ticket_transfer_pending_ticket ON ticket_transfer(ticket_id) WHERE status = 'PENDING';
