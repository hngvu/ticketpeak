-- ============================================================
-- Fix offer schema issues:
-- 1. ticket_type_id unique → (event_id, ticket_type_id) unique
-- 2. Add quantity_available and quantity_sold for inventory tracking
-- ============================================================

ALTER TABLE offer DROP CONSTRAINT offer_ticket_type_id_key;
ALTER TABLE offer ADD CONSTRAINT uq_offer_event_ticket_type UNIQUE (event_id, ticket_type_id);

ALTER TABLE offer ADD COLUMN quantity_available INTEGER NOT NULL DEFAULT 0;
ALTER TABLE offer ADD COLUMN quantity_sold INTEGER NOT NULL DEFAULT 0;
