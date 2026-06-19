-- V62__seed_rich_demo_data.sql\n
-- ===========================================================================
-- EVENT: The Eras Tour Vietnam (Mini Concert)
-- ===========================================================================

INSERT INTO event (
    id, organization_id, venue_id,
    title, slug, description, status,
    start_at, end_at, timezone,
    sale_start_at, sale_end_at,
    restrict_single_seat, safe_tix_enabled, transfer_enabled,
    max_transfer_count, service_fee_percent,
    resale_enabled, max_resale_listings_per_user, resale_price_cap_percent,
    created_at, updated_at
)
VALUES (
    '018f4e1a-0002-4000-8000-000000000101',
    '018f4e1a-0000-4000-8000-000000000001',
    '018f4e1a-0001-4000-8000-000000000001',
    'The Eras Tour Vietnam (Mini Concert)',
    'the-eras-tour-vietnam',
    'Taylor Swift mang The Eras Tour đến Việt Nam với quy mô thân mật.',
    'ONSALE',
    NOW() + INTERVAL '30 days',
    NOW() + INTERVAL '30 days 4 hours',
    'Asia/Ho_Chi_Minh',
    NOW() - INTERVAL '1 day',
    NOW() + INTERVAL '29 days',
    false, false, true,
    2,
    10.00,
    false, 1, NULL,
    NOW(), NOW()
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO event_attraction (event_id, attraction_id) VALUES ('018f4e1a-0002-4000-8000-000000000101', '018fbe92-bc24-7476-80db-31cccefa9b63') ON CONFLICT DO NOTHING;
INSERT INTO event_classification (event_id, classification_id) 
SELECT '018f4e1a-0002-4000-8000-000000000101', id FROM classification WHERE slug IN ('music', 'concerts') ON CONFLICT DO NOTHING;

INSERT INTO manifest (id, venue_id, description, total_capacity, status, created_at, updated_at)
VALUES ('m-eras', '018f4e1a-0001-4000-8000-000000000001', 'Snapshot: The Eras Tour Vietnam (Mini Concert)', 1040, 'PUBLISHED', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO event_manifest (event_id, manifest_id) VALUES ('018f4e1a-0002-4000-8000-000000000101', 'm-eras') ON CONFLICT DO NOTHING;

INSERT INTO venue_level (id, manifest_id, description) VALUES
    ('floor-eras',   'm-eras', 'Sàn (Floor)'),
    ('balcony-eras', 'm-eras', 'Ban công (Balcony)')
ON CONFLICT (id, manifest_id) DO NOTHING;

INSERT INTO price_level (id, manifest_id, description) VALUES
    ('pl-ga-eras',      'm-eras', 'GA Standard'),
    ('pl-vip-eras',     'm-eras', 'VIP Premium'),
    ('pl-regular-eras', 'm-eras', 'Regular')
ON CONFLICT (id, manifest_id) DO NOTHING;

INSERT INTO section (id, manifest_id, name, type, level_id, capacity) VALUES
    ('sec-ga-eras', 'm-eras', 'Khu GA — Sàn đứng', 'GA', 'floor-eras', 1000),
    ('sec-vip-eras', 'm-eras', 'Khu VIP — Ngồi hàng đầu', 'RS', 'floor-eras', 20),
    ('sec-regular-eras', 'm-eras', 'Khu Thường — Ban công', 'RS', 'balcony-eras', 40)
ON CONFLICT (id) DO NOTHING;

INSERT INTO seat_row (id, section_id, name) VALUES
    ('row-vip-a-eras', 'sec-vip-eras', 'A'),
    ('row-vip-b-eras', 'sec-vip-eras', 'B'),
    ('row-reg-c-eras', 'sec-regular-eras', 'C'),
    ('row-reg-d-eras', 'sec-regular-eras', 'D')
ON CONFLICT (id) DO NOTHING;

INSERT INTO seat (id, row_id, name, position_x, position_y, status) VALUES
    ('seat-vip-a1-eras',  'row-vip-a-eras', '1',  1, 1, 'AVAILABLE'),
    ('seat-vip-a2-eras',  'row-vip-a-eras', '2',  2, 1, 'AVAILABLE'),
    ('seat-vip-a3-eras',  'row-vip-a-eras', '3',  3, 1, 'AVAILABLE'),
    ('seat-vip-b1-eras',  'row-vip-b-eras', '1',  1, 2, 'AVAILABLE'),
    ('seat-vip-b2-eras',  'row-vip-b-eras', '2',  2, 2, 'AVAILABLE'),
    ('seat-reg-c1-eras',  'row-reg-c-eras', '1',  1, 3, 'AVAILABLE'),
    ('seat-reg-c2-eras',  'row-reg-c-eras', '2',  2, 3, 'AVAILABLE'),
    ('seat-reg-d1-eras',  'row-reg-d-eras', '1',  1, 4, 'AVAILABLE'),
    ('seat-reg-d2-eras',  'row-reg-d-eras', '2',  2, 4, 'AVAILABLE')
ON CONFLICT (id) DO NOTHING;

INSERT INTO ticket_type (id, event_id, name, code, created_at, updated_at) VALUES
    ('018f4e1a-0010-4000-8000-000000000101',  '018f4e1a-0002-4000-8000-000000000101', 'Vé Thường (GA)', 'GA', NOW(), NOW()),
    ('018f4e1a-0011-4000-8000-000000000101', '018f4e1a-0002-4000-8000-000000000101', 'Vé VIP', 'VIP', NOW(), NOW()),
    ('018f4e1a-0012-4000-8000-000000000101', '018f4e1a-0002-4000-8000-000000000101', 'Vé Thường (Ngồi)', 'REGULAR', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO offer (id, event_id, ticket_type_id, code, name, description, currency, face_value, restricted_payment, event_ticket_minimum, capacity_cap, sellable_quantities, seating_mode, section_id, price_level_id, charges, created_at, updated_at) VALUES 
    ('018f4e1a-0020-4000-8000-000000000101', '018f4e1a-0002-4000-8000-000000000101', '018f4e1a-0010-4000-8000-000000000101', 'GA', 'Vé Thường GA', 'Vé vào sàn GA', 'VND', 500000, false, 1, 1000, '[1,2,4,6]'::jsonb, 'GENERAL_ADMISSION', 'sec-ga-eras', 'pl-ga-eras', '[{"name":"Phí dịch vụ","type":"SERVICE","amount":50000,"isPercentage":false}]'::jsonb, NOW(), NOW()),
    ('018f4e1a-0021-4000-8000-000000000101', '018f4e1a-0002-4000-8000-000000000101', '018f4e1a-0011-4000-8000-000000000101', 'VIP', 'Vé VIP', 'Ghế VIP', 'VND', 1500000, false, 1, 20, '[1,2]'::jsonb, 'RESERVED_SEATING', 'sec-vip-eras', 'pl-vip-eras', '[{"name":"Phí dịch vụ","type":"SERVICE","amount":150000,"isPercentage":false}]'::jsonb, NOW(), NOW()),
    ('018f4e1a-0022-4000-8000-000000000101', '018f4e1a-0002-4000-8000-000000000101', '018f4e1a-0012-4000-8000-000000000101', 'REGULAR', 'Vé Thường Ngồi', 'Ghế thường', 'VND', 750000, false, 1, 40, '[1,2,4]'::jsonb, 'RESERVED_SEATING', 'sec-regular-eras', 'pl-regular-eras', '[{"name":"Phí dịch vụ","type":"SERVICE","amount":75000,"isPercentage":false}]'::jsonb, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO offer_sale_window (offer_id, type, start_at, end_at, created_at, updated_at) VALUES
    ('018f4e1a-0020-4000-8000-000000000101', 'GENERAL_SALE', NOW() - INTERVAL '1 day', NOW() + INTERVAL '29 days', NOW(), NOW()),
    ('018f4e1a-0021-4000-8000-000000000101', 'GENERAL_SALE', NOW() - INTERVAL '1 day', NOW() + INTERVAL '29 days', NOW(), NOW()),
    ('018f4e1a-0022-4000-8000-000000000101', 'GENERAL_SALE', NOW() - INTERVAL '1 day', NOW() + INTERVAL '29 days', NOW(), NOW())
ON CONFLICT (offer_id, type) DO NOTHING;

INSERT INTO inventory_ga (event_id, section_id, offer_id, total, available, held, sold)
VALUES ('018f4e1a-0002-4000-8000-000000000101', 'sec-ga-eras', '018f4e1a-0020-4000-8000-000000000101', 1000, 1000, 0, 0)
ON CONFLICT (event_id, section_id, offer_id) DO NOTHING;

INSERT INTO inventory_seat (event_id, seat_id, offer_id, status) SELECT '018f4e1a-0002-4000-8000-000000000101', s.id, '018f4e1a-0021-4000-8000-000000000101', 'AVAILABLE' FROM seat s WHERE s.row_id IN ('row-vip-a-eras', 'row-vip-b-eras') ON CONFLICT (event_id, seat_id) DO NOTHING;
INSERT INTO inventory_seat (event_id, seat_id, offer_id, status) SELECT '018f4e1a-0002-4000-8000-000000000101', s.id, '018f4e1a-0022-4000-8000-000000000101', 'AVAILABLE' FROM seat s WHERE s.row_id IN ('row-reg-c-eras', 'row-reg-d-eras') ON CONFLICT (event_id, seat_id) DO NOTHING;

-- ===========================================================================
-- EVENT: Liveshow Đen Vâu - Đồng Âm
-- ===========================================================================

INSERT INTO event (
    id, organization_id, venue_id,
    title, slug, description, status,
    start_at, end_at, timezone,
    sale_start_at, sale_end_at,
    restrict_single_seat, safe_tix_enabled, transfer_enabled,
    max_transfer_count, service_fee_percent,
    resale_enabled, max_resale_listings_per_user, resale_price_cap_percent,
    created_at, updated_at
)
VALUES (
    '018f4e1a-0002-4000-8000-000000000102',
    '018f4e1a-0000-4000-8000-000000000001',
    '018f4e1a-0001-4000-8000-000000000001',
    'Liveshow Đen Vâu - Đồng Âm',
    'liveshow-den-vau-dong-am',
    'Đêm nhạc đặc biệt dành riêng cho các Đồng Âm.',
    'ONSALE',
    NOW() + INTERVAL '30 days',
    NOW() + INTERVAL '30 days 4 hours',
    'Asia/Ho_Chi_Minh',
    NOW() - INTERVAL '1 day',
    NOW() + INTERVAL '29 days',
    false, false, true,
    2,
    10.00,
    false, 1, NULL,
    NOW(), NOW()
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO event_attraction (event_id, attraction_id) VALUES ('018f4e1a-0002-4000-8000-000000000102', '018fbe92-bc24-7476-80db-31cccefa9b61') ON CONFLICT DO NOTHING;
INSERT INTO event_classification (event_id, classification_id) 
SELECT '018f4e1a-0002-4000-8000-000000000102', id FROM classification WHERE slug IN ('music', 'concerts') ON CONFLICT DO NOTHING;

INSERT INTO manifest (id, venue_id, description, total_capacity, status, created_at, updated_at)
VALUES ('m-denvau', '018f4e1a-0001-4000-8000-000000000001', 'Snapshot: Liveshow Đen Vâu - Đồng Âm', 1040, 'PUBLISHED', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO event_manifest (event_id, manifest_id) VALUES ('018f4e1a-0002-4000-8000-000000000102', 'm-denvau') ON CONFLICT DO NOTHING;

INSERT INTO venue_level (id, manifest_id, description) VALUES
    ('floor-denvau',   'm-denvau', 'Sàn (Floor)'),
    ('balcony-denvau', 'm-denvau', 'Ban công (Balcony)')
ON CONFLICT (id, manifest_id) DO NOTHING;

INSERT INTO price_level (id, manifest_id, description) VALUES
    ('pl-ga-denvau',      'm-denvau', 'GA Standard'),
    ('pl-vip-denvau',     'm-denvau', 'VIP Premium'),
    ('pl-regular-denvau', 'm-denvau', 'Regular')
ON CONFLICT (id, manifest_id) DO NOTHING;

INSERT INTO section (id, manifest_id, name, type, level_id, capacity) VALUES
    ('sec-ga-denvau', 'm-denvau', 'Khu GA — Sàn đứng', 'GA', 'floor-denvau', 1000),
    ('sec-vip-denvau', 'm-denvau', 'Khu VIP — Ngồi hàng đầu', 'RS', 'floor-denvau', 20),
    ('sec-regular-denvau', 'm-denvau', 'Khu Thường — Ban công', 'RS', 'balcony-denvau', 40)
ON CONFLICT (id) DO NOTHING;

INSERT INTO seat_row (id, section_id, name) VALUES
    ('row-vip-a-denvau', 'sec-vip-denvau', 'A'),
    ('row-vip-b-denvau', 'sec-vip-denvau', 'B'),
    ('row-reg-c-denvau', 'sec-regular-denvau', 'C'),
    ('row-reg-d-denvau', 'sec-regular-denvau', 'D')
ON CONFLICT (id) DO NOTHING;

INSERT INTO seat (id, row_id, name, position_x, position_y, status) VALUES
    ('seat-vip-a1-denvau',  'row-vip-a-denvau', '1',  1, 1, 'AVAILABLE'),
    ('seat-vip-a2-denvau',  'row-vip-a-denvau', '2',  2, 1, 'AVAILABLE'),
    ('seat-vip-a3-denvau',  'row-vip-a-denvau', '3',  3, 1, 'AVAILABLE'),
    ('seat-vip-b1-denvau',  'row-vip-b-denvau', '1',  1, 2, 'AVAILABLE'),
    ('seat-vip-b2-denvau',  'row-vip-b-denvau', '2',  2, 2, 'AVAILABLE'),
    ('seat-reg-c1-denvau',  'row-reg-c-denvau', '1',  1, 3, 'AVAILABLE'),
    ('seat-reg-c2-denvau',  'row-reg-c-denvau', '2',  2, 3, 'AVAILABLE'),
    ('seat-reg-d1-denvau',  'row-reg-d-denvau', '1',  1, 4, 'AVAILABLE'),
    ('seat-reg-d2-denvau',  'row-reg-d-denvau', '2',  2, 4, 'AVAILABLE')
ON CONFLICT (id) DO NOTHING;

INSERT INTO ticket_type (id, event_id, name, code, created_at, updated_at) VALUES
    ('018f4e1a-0010-4000-8000-000000000102',  '018f4e1a-0002-4000-8000-000000000102', 'Vé Thường (GA)', 'GA', NOW(), NOW()),
    ('018f4e1a-0011-4000-8000-000000000102', '018f4e1a-0002-4000-8000-000000000102', 'Vé VIP', 'VIP', NOW(), NOW()),
    ('018f4e1a-0012-4000-8000-000000000102', '018f4e1a-0002-4000-8000-000000000102', 'Vé Thường (Ngồi)', 'REGULAR', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO offer (id, event_id, ticket_type_id, code, name, description, currency, face_value, restricted_payment, event_ticket_minimum, capacity_cap, sellable_quantities, seating_mode, section_id, price_level_id, charges, created_at, updated_at) VALUES 
    ('018f4e1a-0020-4000-8000-000000000102', '018f4e1a-0002-4000-8000-000000000102', '018f4e1a-0010-4000-8000-000000000102', 'GA', 'Vé Thường GA', 'Vé vào sàn GA', 'VND', 500000, false, 1, 1000, '[1,2,4,6]'::jsonb, 'GENERAL_ADMISSION', 'sec-ga-denvau', 'pl-ga-denvau', '[{"name":"Phí dịch vụ","type":"SERVICE","amount":50000,"isPercentage":false}]'::jsonb, NOW(), NOW()),
    ('018f4e1a-0021-4000-8000-000000000102', '018f4e1a-0002-4000-8000-000000000102', '018f4e1a-0011-4000-8000-000000000102', 'VIP', 'Vé VIP', 'Ghế VIP', 'VND', 1500000, false, 1, 20, '[1,2]'::jsonb, 'RESERVED_SEATING', 'sec-vip-denvau', 'pl-vip-denvau', '[{"name":"Phí dịch vụ","type":"SERVICE","amount":150000,"isPercentage":false}]'::jsonb, NOW(), NOW()),
    ('018f4e1a-0022-4000-8000-000000000102', '018f4e1a-0002-4000-8000-000000000102', '018f4e1a-0012-4000-8000-000000000102', 'REGULAR', 'Vé Thường Ngồi', 'Ghế thường', 'VND', 750000, false, 1, 40, '[1,2,4]'::jsonb, 'RESERVED_SEATING', 'sec-regular-denvau', 'pl-regular-denvau', '[{"name":"Phí dịch vụ","type":"SERVICE","amount":75000,"isPercentage":false}]'::jsonb, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO offer_sale_window (offer_id, type, start_at, end_at, created_at, updated_at) VALUES
    ('018f4e1a-0020-4000-8000-000000000102', 'GENERAL_SALE', NOW() - INTERVAL '1 day', NOW() + INTERVAL '29 days', NOW(), NOW()),
    ('018f4e1a-0021-4000-8000-000000000102', 'GENERAL_SALE', NOW() - INTERVAL '1 day', NOW() + INTERVAL '29 days', NOW(), NOW()),
    ('018f4e1a-0022-4000-8000-000000000102', 'GENERAL_SALE', NOW() - INTERVAL '1 day', NOW() + INTERVAL '29 days', NOW(), NOW())
ON CONFLICT (offer_id, type) DO NOTHING;

INSERT INTO inventory_ga (event_id, section_id, offer_id, total, available, held, sold)
VALUES ('018f4e1a-0002-4000-8000-000000000102', 'sec-ga-denvau', '018f4e1a-0020-4000-8000-000000000102', 1000, 1000, 0, 0)
ON CONFLICT (event_id, section_id, offer_id) DO NOTHING;

INSERT INTO inventory_seat (event_id, seat_id, offer_id, status) SELECT '018f4e1a-0002-4000-8000-000000000102', s.id, '018f4e1a-0021-4000-8000-000000000102', 'AVAILABLE' FROM seat s WHERE s.row_id IN ('row-vip-a-denvau', 'row-vip-b-denvau') ON CONFLICT (event_id, seat_id) DO NOTHING;
INSERT INTO inventory_seat (event_id, seat_id, offer_id, status) SELECT '018f4e1a-0002-4000-8000-000000000102', s.id, '018f4e1a-0022-4000-8000-000000000102', 'AVAILABLE' FROM seat s WHERE s.row_id IN ('row-reg-c-denvau', 'row-reg-d-denvau') ON CONFLICT (event_id, seat_id) DO NOTHING;

-- ===========================================================================
-- EVENT: Sơn Tùng M-TP: Sky Tour 2026
-- ===========================================================================

INSERT INTO event (
    id, organization_id, venue_id,
    title, slug, description, status,
    start_at, end_at, timezone,
    sale_start_at, sale_end_at,
    restrict_single_seat, safe_tix_enabled, transfer_enabled,
    max_transfer_count, service_fee_percent,
    resale_enabled, max_resale_listings_per_user, resale_price_cap_percent,
    created_at, updated_at
)
VALUES (
    '018f4e1a-0002-4000-8000-000000000103',
    '018f4e1a-0000-4000-8000-000000000001',
    '018f4e1a-0001-4000-8000-000000000001',
    'Sơn Tùng M-TP: Sky Tour 2026',
    'sky-tour-2026',
    'Sự trở lại hoành tráng của Sơn Tùng M-TP.',
    'ONSALE',
    NOW() + INTERVAL '30 days',
    NOW() + INTERVAL '30 days 4 hours',
    'Asia/Ho_Chi_Minh',
    NOW() - INTERVAL '1 day',
    NOW() + INTERVAL '29 days',
    false, false, true,
    2,
    10.00,
    false, 1, NULL,
    NOW(), NOW()
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO event_attraction (event_id, attraction_id) VALUES ('018f4e1a-0002-4000-8000-000000000103', '018fbe92-bc24-7476-80db-31cccefa9b62') ON CONFLICT DO NOTHING;
INSERT INTO event_classification (event_id, classification_id) 
SELECT '018f4e1a-0002-4000-8000-000000000103', id FROM classification WHERE slug IN ('music', 'concerts') ON CONFLICT DO NOTHING;

INSERT INTO manifest (id, venue_id, description, total_capacity, status, created_at, updated_at)
VALUES ('m-sontung', '018f4e1a-0001-4000-8000-000000000001', 'Snapshot: Sơn Tùng M-TP: Sky Tour 2026', 1040, 'PUBLISHED', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO event_manifest (event_id, manifest_id) VALUES ('018f4e1a-0002-4000-8000-000000000103', 'm-sontung') ON CONFLICT DO NOTHING;

INSERT INTO venue_level (id, manifest_id, description) VALUES
    ('floor-sontung',   'm-sontung', 'Sàn (Floor)'),
    ('balcony-sontung', 'm-sontung', 'Ban công (Balcony)')
ON CONFLICT (id, manifest_id) DO NOTHING;

INSERT INTO price_level (id, manifest_id, description) VALUES
    ('pl-ga-sontung',      'm-sontung', 'GA Standard'),
    ('pl-vip-sontung',     'm-sontung', 'VIP Premium'),
    ('pl-regular-sontung', 'm-sontung', 'Regular')
ON CONFLICT (id, manifest_id) DO NOTHING;

INSERT INTO section (id, manifest_id, name, type, level_id, capacity) VALUES
    ('sec-ga-sontung', 'm-sontung', 'Khu GA — Sàn đứng', 'GA', 'floor-sontung', 1000),
    ('sec-vip-sontung', 'm-sontung', 'Khu VIP — Ngồi hàng đầu', 'RS', 'floor-sontung', 20),
    ('sec-regular-sontung', 'm-sontung', 'Khu Thường — Ban công', 'RS', 'balcony-sontung', 40)
ON CONFLICT (id) DO NOTHING;

INSERT INTO seat_row (id, section_id, name) VALUES
    ('row-vip-a-sontung', 'sec-vip-sontung', 'A'),
    ('row-vip-b-sontung', 'sec-vip-sontung', 'B'),
    ('row-reg-c-sontung', 'sec-regular-sontung', 'C'),
    ('row-reg-d-sontung', 'sec-regular-sontung', 'D')
ON CONFLICT (id) DO NOTHING;

INSERT INTO seat (id, row_id, name, position_x, position_y, status) VALUES
    ('seat-vip-a1-sontung',  'row-vip-a-sontung', '1',  1, 1, 'AVAILABLE'),
    ('seat-vip-a2-sontung',  'row-vip-a-sontung', '2',  2, 1, 'AVAILABLE'),
    ('seat-vip-a3-sontung',  'row-vip-a-sontung', '3',  3, 1, 'AVAILABLE'),
    ('seat-vip-b1-sontung',  'row-vip-b-sontung', '1',  1, 2, 'AVAILABLE'),
    ('seat-vip-b2-sontung',  'row-vip-b-sontung', '2',  2, 2, 'AVAILABLE'),
    ('seat-reg-c1-sontung',  'row-reg-c-sontung', '1',  1, 3, 'AVAILABLE'),
    ('seat-reg-c2-sontung',  'row-reg-c-sontung', '2',  2, 3, 'AVAILABLE'),
    ('seat-reg-d1-sontung',  'row-reg-d-sontung', '1',  1, 4, 'AVAILABLE'),
    ('seat-reg-d2-sontung',  'row-reg-d-sontung', '2',  2, 4, 'AVAILABLE')
ON CONFLICT (id) DO NOTHING;

INSERT INTO ticket_type (id, event_id, name, code, created_at, updated_at) VALUES
    ('018f4e1a-0010-4000-8000-000000000103',  '018f4e1a-0002-4000-8000-000000000103', 'Vé Thường (GA)', 'GA', NOW(), NOW()),
    ('018f4e1a-0011-4000-8000-000000000103', '018f4e1a-0002-4000-8000-000000000103', 'Vé VIP', 'VIP', NOW(), NOW()),
    ('018f4e1a-0012-4000-8000-000000000103', '018f4e1a-0002-4000-8000-000000000103', 'Vé Thường (Ngồi)', 'REGULAR', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO offer (id, event_id, ticket_type_id, code, name, description, currency, face_value, restricted_payment, event_ticket_minimum, capacity_cap, sellable_quantities, seating_mode, section_id, price_level_id, charges, created_at, updated_at) VALUES 
    ('018f4e1a-0020-4000-8000-000000000103', '018f4e1a-0002-4000-8000-000000000103', '018f4e1a-0010-4000-8000-000000000103', 'GA', 'Vé Thường GA', 'Vé vào sàn GA', 'VND', 500000, false, 1, 1000, '[1,2,4,6]'::jsonb, 'GENERAL_ADMISSION', 'sec-ga-sontung', 'pl-ga-sontung', '[{"name":"Phí dịch vụ","type":"SERVICE","amount":50000,"isPercentage":false}]'::jsonb, NOW(), NOW()),
    ('018f4e1a-0021-4000-8000-000000000103', '018f4e1a-0002-4000-8000-000000000103', '018f4e1a-0011-4000-8000-000000000103', 'VIP', 'Vé VIP', 'Ghế VIP', 'VND', 1500000, false, 1, 20, '[1,2]'::jsonb, 'RESERVED_SEATING', 'sec-vip-sontung', 'pl-vip-sontung', '[{"name":"Phí dịch vụ","type":"SERVICE","amount":150000,"isPercentage":false}]'::jsonb, NOW(), NOW()),
    ('018f4e1a-0022-4000-8000-000000000103', '018f4e1a-0002-4000-8000-000000000103', '018f4e1a-0012-4000-8000-000000000103', 'REGULAR', 'Vé Thường Ngồi', 'Ghế thường', 'VND', 750000, false, 1, 40, '[1,2,4]'::jsonb, 'RESERVED_SEATING', 'sec-regular-sontung', 'pl-regular-sontung', '[{"name":"Phí dịch vụ","type":"SERVICE","amount":75000,"isPercentage":false}]'::jsonb, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO offer_sale_window (offer_id, type, start_at, end_at, created_at, updated_at) VALUES
    ('018f4e1a-0020-4000-8000-000000000103', 'GENERAL_SALE', NOW() - INTERVAL '1 day', NOW() + INTERVAL '29 days', NOW(), NOW()),
    ('018f4e1a-0021-4000-8000-000000000103', 'GENERAL_SALE', NOW() - INTERVAL '1 day', NOW() + INTERVAL '29 days', NOW(), NOW()),
    ('018f4e1a-0022-4000-8000-000000000103', 'GENERAL_SALE', NOW() - INTERVAL '1 day', NOW() + INTERVAL '29 days', NOW(), NOW())
ON CONFLICT (offer_id, type) DO NOTHING;

INSERT INTO inventory_ga (event_id, section_id, offer_id, total, available, held, sold)
VALUES ('018f4e1a-0002-4000-8000-000000000103', 'sec-ga-sontung', '018f4e1a-0020-4000-8000-000000000103', 1000, 1000, 0, 0)
ON CONFLICT (event_id, section_id, offer_id) DO NOTHING;

INSERT INTO inventory_seat (event_id, seat_id, offer_id, status) SELECT '018f4e1a-0002-4000-8000-000000000103', s.id, '018f4e1a-0021-4000-8000-000000000103', 'AVAILABLE' FROM seat s WHERE s.row_id IN ('row-vip-a-sontung', 'row-vip-b-sontung') ON CONFLICT (event_id, seat_id) DO NOTHING;
INSERT INTO inventory_seat (event_id, seat_id, offer_id, status) SELECT '018f4e1a-0002-4000-8000-000000000103', s.id, '018f4e1a-0022-4000-8000-000000000103', 'AVAILABLE' FROM seat s WHERE s.row_id IN ('row-reg-c-sontung', 'row-reg-d-sontung') ON CONFLICT (event_id, seat_id) DO NOTHING;

-- ===========================================================================
-- EVENT: Hội thảo TechPeak 2026
-- ===========================================================================

INSERT INTO event (
    id, organization_id, venue_id,
    title, slug, description, status,
    start_at, end_at, timezone,
    sale_start_at, sale_end_at,
    restrict_single_seat, safe_tix_enabled, transfer_enabled,
    max_transfer_count, service_fee_percent,
    resale_enabled, max_resale_listings_per_user, resale_price_cap_percent,
    created_at, updated_at
)
VALUES (
    '018f4e1a-0002-4000-8000-000000000104',
    '018f4e1a-0000-4000-8000-000000000001',
    '018f4e1a-0001-4000-8000-000000000001',
    'Hội thảo TechPeak 2026',
    'techpeak-2026',
    'Sự kiện công nghệ lớn nhất năm quy tụ các chuyên gia hàng đầu.',
    'ONSALE',
    NOW() + INTERVAL '30 days',
    NOW() + INTERVAL '30 days 4 hours',
    'Asia/Ho_Chi_Minh',
    NOW() - INTERVAL '1 day',
    NOW() + INTERVAL '29 days',
    false, false, true,
    2,
    10.00,
    false, 1, NULL,
    NOW(), NOW()
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO event_attraction (event_id, attraction_id) VALUES ('018f4e1a-0002-4000-8000-000000000104', '018fbe92-bc24-7476-80db-31cccefa9b61') ON CONFLICT DO NOTHING;
INSERT INTO event_classification (event_id, classification_id) 
SELECT '018f4e1a-0002-4000-8000-000000000104', id FROM classification WHERE slug IN ('family') ON CONFLICT DO NOTHING;

INSERT INTO manifest (id, venue_id, description, total_capacity, status, created_at, updated_at)
VALUES ('m-techpeak', '018f4e1a-0001-4000-8000-000000000001', 'Snapshot: Hội thảo TechPeak 2026', 1040, 'PUBLISHED', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO event_manifest (event_id, manifest_id) VALUES ('018f4e1a-0002-4000-8000-000000000104', 'm-techpeak') ON CONFLICT DO NOTHING;

INSERT INTO venue_level (id, manifest_id, description) VALUES
    ('floor-techpeak',   'm-techpeak', 'Sàn (Floor)'),
    ('balcony-techpeak', 'm-techpeak', 'Ban công (Balcony)')
ON CONFLICT (id, manifest_id) DO NOTHING;

INSERT INTO price_level (id, manifest_id, description) VALUES
    ('pl-ga-techpeak',      'm-techpeak', 'GA Standard'),
    ('pl-vip-techpeak',     'm-techpeak', 'VIP Premium'),
    ('pl-regular-techpeak', 'm-techpeak', 'Regular')
ON CONFLICT (id, manifest_id) DO NOTHING;

INSERT INTO section (id, manifest_id, name, type, level_id, capacity) VALUES
    ('sec-ga-techpeak', 'm-techpeak', 'Khu GA — Sàn đứng', 'GA', 'floor-techpeak', 1000),
    ('sec-vip-techpeak', 'm-techpeak', 'Khu VIP — Ngồi hàng đầu', 'RS', 'floor-techpeak', 20),
    ('sec-regular-techpeak', 'm-techpeak', 'Khu Thường — Ban công', 'RS', 'balcony-techpeak', 40)
ON CONFLICT (id) DO NOTHING;

INSERT INTO seat_row (id, section_id, name) VALUES
    ('row-vip-a-techpeak', 'sec-vip-techpeak', 'A'),
    ('row-vip-b-techpeak', 'sec-vip-techpeak', 'B'),
    ('row-reg-c-techpeak', 'sec-regular-techpeak', 'C'),
    ('row-reg-d-techpeak', 'sec-regular-techpeak', 'D')
ON CONFLICT (id) DO NOTHING;

INSERT INTO seat (id, row_id, name, position_x, position_y, status) VALUES
    ('seat-vip-a1-techpeak',  'row-vip-a-techpeak', '1',  1, 1, 'AVAILABLE'),
    ('seat-vip-a2-techpeak',  'row-vip-a-techpeak', '2',  2, 1, 'AVAILABLE'),
    ('seat-vip-a3-techpeak',  'row-vip-a-techpeak', '3',  3, 1, 'AVAILABLE'),
    ('seat-vip-b1-techpeak',  'row-vip-b-techpeak', '1',  1, 2, 'AVAILABLE'),
    ('seat-vip-b2-techpeak',  'row-vip-b-techpeak', '2',  2, 2, 'AVAILABLE'),
    ('seat-reg-c1-techpeak',  'row-reg-c-techpeak', '1',  1, 3, 'AVAILABLE'),
    ('seat-reg-c2-techpeak',  'row-reg-c-techpeak', '2',  2, 3, 'AVAILABLE'),
    ('seat-reg-d1-techpeak',  'row-reg-d-techpeak', '1',  1, 4, 'AVAILABLE'),
    ('seat-reg-d2-techpeak',  'row-reg-d-techpeak', '2',  2, 4, 'AVAILABLE')
ON CONFLICT (id) DO NOTHING;

INSERT INTO ticket_type (id, event_id, name, code, created_at, updated_at) VALUES
    ('018f4e1a-0010-4000-8000-000000000104',  '018f4e1a-0002-4000-8000-000000000104', 'Vé Thường (GA)', 'GA', NOW(), NOW()),
    ('018f4e1a-0011-4000-8000-000000000104', '018f4e1a-0002-4000-8000-000000000104', 'Vé VIP', 'VIP', NOW(), NOW()),
    ('018f4e1a-0012-4000-8000-000000000104', '018f4e1a-0002-4000-8000-000000000104', 'Vé Thường (Ngồi)', 'REGULAR', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO offer (id, event_id, ticket_type_id, code, name, description, currency, face_value, restricted_payment, event_ticket_minimum, capacity_cap, sellable_quantities, seating_mode, section_id, price_level_id, charges, created_at, updated_at) VALUES 
    ('018f4e1a-0020-4000-8000-000000000104', '018f4e1a-0002-4000-8000-000000000104', '018f4e1a-0010-4000-8000-000000000104', 'GA', 'Vé Thường GA', 'Vé vào sàn GA', 'VND', 500000, false, 1, 1000, '[1,2,4,6]'::jsonb, 'GENERAL_ADMISSION', 'sec-ga-techpeak', 'pl-ga-techpeak', '[{"name":"Phí dịch vụ","type":"SERVICE","amount":50000,"isPercentage":false}]'::jsonb, NOW(), NOW()),
    ('018f4e1a-0021-4000-8000-000000000104', '018f4e1a-0002-4000-8000-000000000104', '018f4e1a-0011-4000-8000-000000000104', 'VIP', 'Vé VIP', 'Ghế VIP', 'VND', 1500000, false, 1, 20, '[1,2]'::jsonb, 'RESERVED_SEATING', 'sec-vip-techpeak', 'pl-vip-techpeak', '[{"name":"Phí dịch vụ","type":"SERVICE","amount":150000,"isPercentage":false}]'::jsonb, NOW(), NOW()),
    ('018f4e1a-0022-4000-8000-000000000104', '018f4e1a-0002-4000-8000-000000000104', '018f4e1a-0012-4000-8000-000000000104', 'REGULAR', 'Vé Thường Ngồi', 'Ghế thường', 'VND', 750000, false, 1, 40, '[1,2,4]'::jsonb, 'RESERVED_SEATING', 'sec-regular-techpeak', 'pl-regular-techpeak', '[{"name":"Phí dịch vụ","type":"SERVICE","amount":75000,"isPercentage":false}]'::jsonb, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO offer_sale_window (offer_id, type, start_at, end_at, created_at, updated_at) VALUES
    ('018f4e1a-0020-4000-8000-000000000104', 'GENERAL_SALE', NOW() - INTERVAL '1 day', NOW() + INTERVAL '29 days', NOW(), NOW()),
    ('018f4e1a-0021-4000-8000-000000000104', 'GENERAL_SALE', NOW() - INTERVAL '1 day', NOW() + INTERVAL '29 days', NOW(), NOW()),
    ('018f4e1a-0022-4000-8000-000000000104', 'GENERAL_SALE', NOW() - INTERVAL '1 day', NOW() + INTERVAL '29 days', NOW(), NOW())
ON CONFLICT (offer_id, type) DO NOTHING;

INSERT INTO inventory_ga (event_id, section_id, offer_id, total, available, held, sold)
VALUES ('018f4e1a-0002-4000-8000-000000000104', 'sec-ga-techpeak', '018f4e1a-0020-4000-8000-000000000104', 1000, 1000, 0, 0)
ON CONFLICT (event_id, section_id, offer_id) DO NOTHING;

INSERT INTO inventory_seat (event_id, seat_id, offer_id, status) SELECT '018f4e1a-0002-4000-8000-000000000104', s.id, '018f4e1a-0021-4000-8000-000000000104', 'AVAILABLE' FROM seat s WHERE s.row_id IN ('row-vip-a-techpeak', 'row-vip-b-techpeak') ON CONFLICT (event_id, seat_id) DO NOTHING;
INSERT INTO inventory_seat (event_id, seat_id, offer_id, status) SELECT '018f4e1a-0002-4000-8000-000000000104', s.id, '018f4e1a-0022-4000-8000-000000000104', 'AVAILABLE' FROM seat s WHERE s.row_id IN ('row-reg-c-techpeak', 'row-reg-d-techpeak') ON CONFLICT (event_id, seat_id) DO NOTHING;
