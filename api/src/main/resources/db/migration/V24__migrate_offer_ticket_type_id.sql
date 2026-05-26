-- Bước 1: Tạo một ticket_type row cho mỗi (event_id, ticket_type_id) hiện có trong offer
INSERT INTO ticket_type (event_id, name, slug)
SELECT DISTINCT
    event_id,
    ticket_type_id AS name,
    ticket_type_id AS slug
FROM offer;

-- Bước 2: Thêm cột UUID tạm
ALTER TABLE offer ADD COLUMN ticket_type_uuid UUID;

-- Bước 3: Điền UUID từ ticket_type vừa tạo
UPDATE offer o
SET ticket_type_uuid = (
    SELECT tt.id
    FROM ticket_type tt
    WHERE tt.event_id = o.event_id
      AND tt.slug     = o.ticket_type_id
);

-- Bước 4: Drop cột cũ, rename cột mới, thêm NOT NULL
ALTER TABLE offer DROP COLUMN ticket_type_id;
ALTER TABLE offer RENAME COLUMN ticket_type_uuid TO ticket_type_id;
ALTER TABLE offer ALTER COLUMN ticket_type_id SET NOT NULL;

-- Bước 5: Drop index cũ dùng VARCHAR, tạo index mới
DROP INDEX IF EXISTS idx_offer_event_ticket_type_id;
CREATE INDEX idx_offer_ticket_type_id ON offer(ticket_type_id);
