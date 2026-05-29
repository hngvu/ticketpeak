-- =============================================================================
-- V36 — Seed data cho flow mua vé (ticket purchase flow)
--
-- Accounts có sẵn (V32 + V34):
--   organizer@ticketpeak.com  — tạo org, event, offer
--   buyer@ticketpeak.com      — mua vé
--
-- IDs cố định dùng uuidv4 literals:
--   org  : 018f4e1a-0000-4000-8000-000000000001
--   venue: 018f4e1a-0001-4000-8000-000000000001
--   event: 018f4e1a-0002-4000-8000-000000000001
--   tt GA : 018f4e1a-0010-4000-8000-000000000001
--   tt VIP: 018f4e1a-0011-4000-8000-000000000001
--   tt Reg: 018f4e1a-0012-4000-8000-000000000001
--   offer GA : 018f4e1a-0020-4000-8000-000000000001
--   offer VIP: 018f4e1a-0021-4000-8000-000000000001
--   offer Reg: 018f4e1a-0022-4000-8000-000000000001
-- =============================================================================

-- ---------------------------------------------------------------------------
-- 1. ORGANIZATION
-- ---------------------------------------------------------------------------
INSERT INTO organization (
    id, name, slug, bio, email, country_code,
    owner_account_id, status, created_at, updated_at
)
VALUES (
    '018f4e1a-0000-4000-8000-000000000001',
    'Live Nation Vietnam',
    'live-nation-vietnam',
    'Đơn vị tổ chức sự kiện âm nhạc hàng đầu Việt Nam.',
    'contact@livenation.vn',
    'VN',
    (SELECT id FROM account WHERE email = 'organizer@ticketpeak.com'),
    'APPROVED',
    NOW(), NOW()
)
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- 2. VENUE — Nhà hát Hòa Bình, TP.HCM (UUID id vì V6 refactor)
-- ---------------------------------------------------------------------------
INSERT INTO venue (
    id, name, address, city, country,
    latitude, longitude, description, status, created_at, updated_at
)
VALUES (
    '018f4e1a-0001-4000-8000-000000000001',
    'Nhà hát Hòa Bình',
    '240 Đường 3 Tháng 2, Phường 12, Quận 10',
    'TP. Hồ Chí Minh',
    'Việt Nam',
    10.7721,
    106.6658,
    'Nhà hát Hòa Bình — một trong những địa điểm biểu diễn lớn nhất TP.HCM.',
    'ACTIVE',
    NOW(), NOW()
)
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- 3. MANIFEST gốc — PUBLISHED (manifest.id là VARCHAR, manifest.venue_id là UUID)
-- ---------------------------------------------------------------------------
INSERT INTO manifest (id, venue_id, description, total_capacity, status, created_at, updated_at)
VALUES (
    'mf-hoa-binh-001',
    '018f4e1a-0001-4000-8000-000000000001',
    'Layout chính — Sàn GA + Ghế VIP + Ghế Thường',
    1040,
    'PUBLISHED',
    NOW(), NOW()
)
ON CONFLICT (id) DO NOTHING;

-- Levels
INSERT INTO venue_level (id, manifest_id, description) VALUES
    ('floor',   'mf-hoa-binh-001', 'Sàn (Floor)'),
    ('balcony', 'mf-hoa-binh-001', 'Ban công (Balcony)')
ON CONFLICT (id, manifest_id) DO NOTHING;

-- Sections
INSERT INTO section (id, manifest_id, description) VALUES
    ('sec-ga',      'mf-hoa-binh-001', 'Khu GA — Sàn đứng'),
    ('sec-vip',     'mf-hoa-binh-001', 'Khu VIP — Ngồi hàng đầu'),
    ('sec-regular', 'mf-hoa-binh-001', 'Khu Thường — Ban công')
ON CONFLICT (id, manifest_id) DO NOTHING;

-- Price Levels
INSERT INTO price_level (id, manifest_id, description) VALUES
    ('pl-ga',      'mf-hoa-binh-001', 'GA Standard'),
    ('pl-vip',     'mf-hoa-binh-001', 'VIP Premium'),
    ('pl-regular', 'mf-hoa-binh-001', 'Regular')
ON CONFLICT (id, manifest_id) DO NOTHING;

-- GA Area (1000 chỗ đứng)
INSERT INTO ga_area (id, manifest_id, level_id, section_id, price_level_id, capacity)
VALUES ('ga-floor-001', 'mf-hoa-binh-001', 'floor', 'sec-ga', 'pl-ga', 1000)
ON CONFLICT (id) DO NOTHING;

-- RS Areas
INSERT INTO rs_area (id, manifest_id, level_id, section_id, price_level_id) VALUES
    ('rs-vip-001', 'mf-hoa-binh-001', 'floor',   'sec-vip',     'pl-vip'),
    ('rs-reg-001', 'mf-hoa-binh-001', 'balcony', 'sec-regular', 'pl-regular')
ON CONFLICT (id) DO NOTHING;

-- Seat Rows — VIP
INSERT INTO seat_row (id, rs_area_id, name, position_y) VALUES
    ('row-vip-a', 'rs-vip-001', 'A', 1),
    ('row-vip-b', 'rs-vip-001', 'B', 2)
ON CONFLICT (id) DO NOTHING;

-- Seats VIP A1-A10
INSERT INTO seat (id, row_id, name, position_x, status, accessibility, obstructed_view, aisle) VALUES
    ('vip-a1',  'row-vip-a', '1',  1,  'AVAILABLE', false, false, false),
    ('vip-a2',  'row-vip-a', '2',  2,  'AVAILABLE', false, false, false),
    ('vip-a3',  'row-vip-a', '3',  3,  'AVAILABLE', false, false, false),
    ('vip-a4',  'row-vip-a', '4',  4,  'AVAILABLE', false, false, false),
    ('vip-a5',  'row-vip-a', '5',  5,  'AVAILABLE', true,  false, false),
    ('vip-a6',  'row-vip-a', '6',  6,  'AVAILABLE', false, false, false),
    ('vip-a7',  'row-vip-a', '7',  7,  'AVAILABLE', false, false, false),
    ('vip-a8',  'row-vip-a', '8',  8,  'AVAILABLE', false, false, false),
    ('vip-a9',  'row-vip-a', '9',  9,  'AVAILABLE', false, false, true),
    ('vip-a10', 'row-vip-a', '10', 10, 'AVAILABLE', false, false, false)
ON CONFLICT (id) DO NOTHING;

-- Seats VIP B1-B10
INSERT INTO seat (id, row_id, name, position_x, status, accessibility, obstructed_view, aisle) VALUES
    ('vip-b1',  'row-vip-b', '1',  1,  'AVAILABLE', false, false, false),
    ('vip-b2',  'row-vip-b', '2',  2,  'AVAILABLE', false, false, false),
    ('vip-b3',  'row-vip-b', '3',  3,  'AVAILABLE', false, false, false),
    ('vip-b4',  'row-vip-b', '4',  4,  'AVAILABLE', false, false, false),
    ('vip-b5',  'row-vip-b', '5',  5,  'AVAILABLE', false, false, false),
    ('vip-b6',  'row-vip-b', '6',  6,  'AVAILABLE', false, false, false),
    ('vip-b7',  'row-vip-b', '7',  7,  'AVAILABLE', false, false, false),
    ('vip-b8',  'row-vip-b', '8',  8,  'AVAILABLE', false, false, false),
    ('vip-b9',  'row-vip-b', '9',  9,  'AVAILABLE', false, false, false),
    ('vip-b10', 'row-vip-b', '10', 10, 'AVAILABLE', false, false, false)
ON CONFLICT (id) DO NOTHING;

-- Seat Rows — Regular
INSERT INTO seat_row (id, rs_area_id, name, position_y) VALUES
    ('row-reg-c', 'rs-reg-001', 'C', 1),
    ('row-reg-d', 'rs-reg-001', 'D', 2)
ON CONFLICT (id) DO NOTHING;

-- Seats Regular C1-C10
INSERT INTO seat (id, row_id, name, position_x, status, accessibility, obstructed_view, aisle) VALUES
    ('reg-c1',  'row-reg-c', '1',  1,  'AVAILABLE', false, false, false),
    ('reg-c2',  'row-reg-c', '2',  2,  'AVAILABLE', false, false, false),
    ('reg-c3',  'row-reg-c', '3',  3,  'AVAILABLE', false, false, false),
    ('reg-c4',  'row-reg-c', '4',  4,  'AVAILABLE', false, false, false),
    ('reg-c5',  'row-reg-c', '5',  5,  'AVAILABLE', false, false, false),
    ('reg-c6',  'row-reg-c', '6',  6,  'AVAILABLE', false, false, false),
    ('reg-c7',  'row-reg-c', '7',  7,  'AVAILABLE', false, false, false),
    ('reg-c8',  'row-reg-c', '8',  8,  'AVAILABLE', false, false, false),
    ('reg-c9',  'row-reg-c', '9',  9,  'AVAILABLE', false, false, false),
    ('reg-c10', 'row-reg-c', '10', 10, 'AVAILABLE', false, false, false)
ON CONFLICT (id) DO NOTHING;

-- Seats Regular D1-D10
INSERT INTO seat (id, row_id, name, position_x, status, accessibility, obstructed_view, aisle) VALUES
    ('reg-d1',  'row-reg-d', '1',  1,  'AVAILABLE', false, false, false),
    ('reg-d2',  'row-reg-d', '2',  2,  'AVAILABLE', false, false, false),
    ('reg-d3',  'row-reg-d', '3',  3,  'AVAILABLE', false, false, false),
    ('reg-d4',  'row-reg-d', '4',  4,  'AVAILABLE', false, false, false),
    ('reg-d5',  'row-reg-d', '5',  5,  'AVAILABLE', false, false, false),
    ('reg-d6',  'row-reg-d', '6',  6,  'AVAILABLE', false, false, false),
    ('reg-d7',  'row-reg-d', '7',  7,  'AVAILABLE', false, false, false),
    ('reg-d8',  'row-reg-d', '8',  8,  'AVAILABLE', false, false, false),
    ('reg-d9',  'row-reg-d', '9',  9,  'AVAILABLE', false, false, false),
    ('reg-d10', 'row-reg-d', '10', 10, 'AVAILABLE', false, false, false)
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- 4. EVENT — status ONSALE
-- ---------------------------------------------------------------------------
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
    '018f4e1a-0002-4000-8000-000000000001',
    '018f4e1a-0000-4000-8000-000000000001',
    '018f4e1a-0001-4000-8000-000000000001',
    'Đêm nhạc Hà Nội Mùa Thu 2026',
    'dem-nhac-ha-noi-mua-thu-2026',
    'Đêm nhạc kết hợp các nghệ sĩ hàng đầu Việt Nam — một đêm không thể bỏ lỡ tại Nhà hát Hòa Bình.',
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

-- ---------------------------------------------------------------------------
-- 5. SNAPSHOT MANIFEST cho event (EventService.getSnapshotManifestId)
--    format: 'evt-{event_id}-snap'
-- ---------------------------------------------------------------------------
INSERT INTO manifest (id, venue_id, description, total_capacity, status, created_at, updated_at)
VALUES (
    'evt-018f4e1a-0002-4000-8000-000000000001-snap',
    '018f4e1a-0001-4000-8000-000000000001',
    'Snapshot: Đêm nhạc Hà Nội Mùa Thu 2026',
    1040,
    'PUBLISHED',
    NOW(), NOW()
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO event_manifest (event_id, manifest_id)
VALUES (
    '018f4e1a-0002-4000-8000-000000000001',
    'evt-018f4e1a-0002-4000-8000-000000000001-snap'
)
ON CONFLICT (event_id) DO NOTHING;

-- Snapshot: Levels / Sections / PriceLevels
INSERT INTO venue_level (id, manifest_id, description) VALUES
    ('floor',   'evt-018f4e1a-0002-4000-8000-000000000001-snap', 'Sàn (Floor)'),
    ('balcony', 'evt-018f4e1a-0002-4000-8000-000000000001-snap', 'Ban công (Balcony)')
ON CONFLICT (id, manifest_id) DO NOTHING;

INSERT INTO section (id, manifest_id, description) VALUES
    ('sec-ga',      'evt-018f4e1a-0002-4000-8000-000000000001-snap', 'Khu GA — Sàn đứng'),
    ('sec-vip',     'evt-018f4e1a-0002-4000-8000-000000000001-snap', 'Khu VIP — Ngồi hàng đầu'),
    ('sec-regular', 'evt-018f4e1a-0002-4000-8000-000000000001-snap', 'Khu Thường — Ban công')
ON CONFLICT (id, manifest_id) DO NOTHING;

INSERT INTO price_level (id, manifest_id, description) VALUES
    ('pl-ga',      'evt-018f4e1a-0002-4000-8000-000000000001-snap', 'GA Standard'),
    ('pl-vip',     'evt-018f4e1a-0002-4000-8000-000000000001-snap', 'VIP Premium'),
    ('pl-regular', 'evt-018f4e1a-0002-4000-8000-000000000001-snap', 'Regular')
ON CONFLICT (id, manifest_id) DO NOTHING;

-- Snapshot: GA Area
INSERT INTO ga_area (id, manifest_id, level_id, section_id, price_level_id, capacity)
VALUES ('ga-snap-001', 'evt-018f4e1a-0002-4000-8000-000000000001-snap', 'floor', 'sec-ga', 'pl-ga', 1000)
ON CONFLICT (id) DO NOTHING;

-- Snapshot: RS Areas
INSERT INTO rs_area (id, manifest_id, level_id, section_id, price_level_id) VALUES
    ('rs-vip-snap', 'evt-018f4e1a-0002-4000-8000-000000000001-snap', 'floor',   'sec-vip',     'pl-vip'),
    ('rs-reg-snap', 'evt-018f4e1a-0002-4000-8000-000000000001-snap', 'balcony', 'sec-regular', 'pl-regular')
ON CONFLICT (id) DO NOTHING;

-- Snapshot: Seat Rows
INSERT INTO seat_row (id, rs_area_id, name, position_y) VALUES
    ('row-vip-a-snap', 'rs-vip-snap', 'A', 1),
    ('row-vip-b-snap', 'rs-vip-snap', 'B', 2),
    ('row-reg-c-snap', 'rs-reg-snap', 'C', 1),
    ('row-reg-d-snap', 'rs-reg-snap', 'D', 2)
ON CONFLICT (id) DO NOTHING;

-- Snapshot: Seats VIP A
INSERT INTO seat (id, row_id, name, position_x, status, accessibility, obstructed_view, aisle) VALUES
    ('snap-vip-a1',  'row-vip-a-snap', '1',  1,  'AVAILABLE', false, false, false),
    ('snap-vip-a2',  'row-vip-a-snap', '2',  2,  'AVAILABLE', false, false, false),
    ('snap-vip-a3',  'row-vip-a-snap', '3',  3,  'AVAILABLE', false, false, false),
    ('snap-vip-a4',  'row-vip-a-snap', '4',  4,  'AVAILABLE', false, false, false),
    ('snap-vip-a5',  'row-vip-a-snap', '5',  5,  'AVAILABLE', true,  false, false),
    ('snap-vip-a6',  'row-vip-a-snap', '6',  6,  'AVAILABLE', false, false, false),
    ('snap-vip-a7',  'row-vip-a-snap', '7',  7,  'AVAILABLE', false, false, false),
    ('snap-vip-a8',  'row-vip-a-snap', '8',  8,  'AVAILABLE', false, false, false),
    ('snap-vip-a9',  'row-vip-a-snap', '9',  9,  'AVAILABLE', false, false, true),
    ('snap-vip-a10', 'row-vip-a-snap', '10', 10, 'AVAILABLE', false, false, false)
ON CONFLICT (id) DO NOTHING;

-- Snapshot: Seats VIP B
INSERT INTO seat (id, row_id, name, position_x, status, accessibility, obstructed_view, aisle) VALUES
    ('snap-vip-b1',  'row-vip-b-snap', '1',  1,  'AVAILABLE', false, false, false),
    ('snap-vip-b2',  'row-vip-b-snap', '2',  2,  'AVAILABLE', false, false, false),
    ('snap-vip-b3',  'row-vip-b-snap', '3',  3,  'AVAILABLE', false, false, false),
    ('snap-vip-b4',  'row-vip-b-snap', '4',  4,  'AVAILABLE', false, false, false),
    ('snap-vip-b5',  'row-vip-b-snap', '5',  5,  'AVAILABLE', false, false, false),
    ('snap-vip-b6',  'row-vip-b-snap', '6',  6,  'AVAILABLE', false, false, false),
    ('snap-vip-b7',  'row-vip-b-snap', '7',  7,  'AVAILABLE', false, false, false),
    ('snap-vip-b8',  'row-vip-b-snap', '8',  8,  'AVAILABLE', false, false, false),
    ('snap-vip-b9',  'row-vip-b-snap', '9',  9,  'AVAILABLE', false, false, false),
    ('snap-vip-b10', 'row-vip-b-snap', '10', 10, 'AVAILABLE', false, false, false)
ON CONFLICT (id) DO NOTHING;

-- Snapshot: Seats Regular C
INSERT INTO seat (id, row_id, name, position_x, status, accessibility, obstructed_view, aisle) VALUES
    ('snap-reg-c1',  'row-reg-c-snap', '1',  1,  'AVAILABLE', false, false, false),
    ('snap-reg-c2',  'row-reg-c-snap', '2',  2,  'AVAILABLE', false, false, false),
    ('snap-reg-c3',  'row-reg-c-snap', '3',  3,  'AVAILABLE', false, false, false),
    ('snap-reg-c4',  'row-reg-c-snap', '4',  4,  'AVAILABLE', false, false, false),
    ('snap-reg-c5',  'row-reg-c-snap', '5',  5,  'AVAILABLE', false, false, false),
    ('snap-reg-c6',  'row-reg-c-snap', '6',  6,  'AVAILABLE', false, false, false),
    ('snap-reg-c7',  'row-reg-c-snap', '7',  7,  'AVAILABLE', false, false, false),
    ('snap-reg-c8',  'row-reg-c-snap', '8',  8,  'AVAILABLE', false, false, false),
    ('snap-reg-c9',  'row-reg-c-snap', '9',  9,  'AVAILABLE', false, false, false),
    ('snap-reg-c10', 'row-reg-c-snap', '10', 10, 'AVAILABLE', false, false, false)
ON CONFLICT (id) DO NOTHING;

-- Snapshot: Seats Regular D
INSERT INTO seat (id, row_id, name, position_x, status, accessibility, obstructed_view, aisle) VALUES
    ('snap-reg-d1',  'row-reg-d-snap', '1',  1,  'AVAILABLE', false, false, false),
    ('snap-reg-d2',  'row-reg-d-snap', '2',  2,  'AVAILABLE', false, false, false),
    ('snap-reg-d3',  'row-reg-d-snap', '3',  3,  'AVAILABLE', false, false, false),
    ('snap-reg-d4',  'row-reg-d-snap', '4',  4,  'AVAILABLE', false, false, false),
    ('snap-reg-d5',  'row-reg-d-snap', '5',  5,  'AVAILABLE', false, false, false),
    ('snap-reg-d6',  'row-reg-d-snap', '6',  6,  'AVAILABLE', false, false, false),
    ('snap-reg-d7',  'row-reg-d-snap', '7',  7,  'AVAILABLE', false, false, false),
    ('snap-reg-d8',  'row-reg-d-snap', '8',  8,  'AVAILABLE', false, false, false),
    ('snap-reg-d9',  'row-reg-d-snap', '9',  9,  'AVAILABLE', false, false, false),
    ('snap-reg-d10', 'row-reg-d-snap', '10', 10, 'AVAILABLE', false, false, false)
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- 6. TICKET TYPES
-- ---------------------------------------------------------------------------
INSERT INTO ticket_type (id, event_id, name, slug, description, created_at, updated_at) VALUES
    (
        '018f4e1a-0010-4000-8000-000000000001',
        '018f4e1a-0002-4000-8000-000000000001',
        'Vé Thường (GA)',
        've-thuong-ga',
        'Vé vào sàn đứng — tự do không ghế ngồi',
        NOW(), NOW()
    ),
    (
        '018f4e1a-0011-4000-8000-000000000001',
        '018f4e1a-0002-4000-8000-000000000001',
        'Vé VIP',
        've-vip',
        'Ghế ngồi hàng đầu sát sân khấu',
        NOW(), NOW()
    ),
    (
        '018f4e1a-0012-4000-8000-000000000001',
        '018f4e1a-0002-4000-8000-000000000001',
        'Vé Thường (Ngồi)',
        've-thuong-ngoi',
        'Ghế ngồi ban công — tầm nhìn toàn cảnh',
        NOW(), NOW()
    )
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- 7. OFFERS
-- ---------------------------------------------------------------------------

-- Offer GA
INSERT INTO offer (
    id, event_id, ticket_type_id,
    name, description, currency, face_value,
    restricted_payment, event_ticket_minimum,
    capacity_cap, sellable_quantities,
    seating_mode, section_id, price_level_id,
    charges, created_at, updated_at
) VALUES (
    '018f4e1a-0020-4000-8000-000000000001',
    '018f4e1a-0002-4000-8000-000000000001',
    '018f4e1a-0010-4000-8000-000000000001',
    'Vé Thường GA',
    'Vé vào sàn GA — đứng tự do, không chọn ghế.',
    'VND', 500000,
    false, 1,
    1000, '[1,2,4,6]'::jsonb,
    'GENERAL_ADMISSION', 'sec-ga', 'pl-ga',
    '[{"name":"Phí dịch vụ","type":"SERVICE","amount":50000,"isPercentage":false}]'::jsonb,
    NOW(), NOW()
) ON CONFLICT (id) DO NOTHING;

-- Offer VIP
INSERT INTO offer (
    id, event_id, ticket_type_id,
    name, description, currency, face_value,
    restricted_payment, event_ticket_minimum,
    capacity_cap, sellable_quantities,
    seating_mode, section_id, price_level_id,
    charges, created_at, updated_at
) VALUES (
    '018f4e1a-0021-4000-8000-000000000001',
    '018f4e1a-0002-4000-8000-000000000001',
    '018f4e1a-0011-4000-8000-000000000001',
    'Vé VIP',
    'Ghế ngồi VIP — hàng A, B sát sân khấu.',
    'VND', 1500000,
    false, 1,
    20, '[1,2]'::jsonb,
    'RESERVED_SEATING', 'sec-vip', 'pl-vip',
    '[{"name":"Phí dịch vụ","type":"SERVICE","amount":150000,"isPercentage":false}]'::jsonb,
    NOW(), NOW()
) ON CONFLICT (id) DO NOTHING;

-- Offer Regular
INSERT INTO offer (
    id, event_id, ticket_type_id,
    name, description, currency, face_value,
    restricted_payment, event_ticket_minimum,
    capacity_cap, sellable_quantities,
    seating_mode, section_id, price_level_id,
    charges, created_at, updated_at
) VALUES (
    '018f4e1a-0022-4000-8000-000000000001',
    '018f4e1a-0002-4000-8000-000000000001',
    '018f4e1a-0012-4000-8000-000000000001',
    'Vé Thường Ngồi',
    'Ghế ngồi ban công — hàng C, D.',
    'VND', 750000,
    false, 1,
    40, '[1,2,4]'::jsonb,
    'RESERVED_SEATING', 'sec-regular', 'pl-regular',
    '[{"name":"Phí dịch vụ","type":"SERVICE","amount":75000,"isPercentage":false}]'::jsonb,
    NOW(), NOW()
) ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- 8. OFFER SALE WINDOWS — GENERAL_SALE đang mở
-- ---------------------------------------------------------------------------
INSERT INTO offer_sale_window (offer_id, type, start_at, end_at, created_at, updated_at) VALUES
    ('018f4e1a-0020-4000-8000-000000000001', 'GENERAL_SALE',
     NOW() - INTERVAL '1 day', NOW() + INTERVAL '29 days', NOW(), NOW()),
    ('018f4e1a-0021-4000-8000-000000000001', 'GENERAL_SALE',
     NOW() - INTERVAL '1 day', NOW() + INTERVAL '29 days', NOW(), NOW()),
    ('018f4e1a-0022-4000-8000-000000000001', 'GENERAL_SALE',
     NOW() - INTERVAL '1 day', NOW() + INTERVAL '29 days', NOW(), NOW())
ON CONFLICT (offer_id, type) DO NOTHING;

-- ---------------------------------------------------------------------------
-- 9. INVENTORY — GA (area_id là id của snapshot ga_area)
-- ---------------------------------------------------------------------------
INSERT INTO inventory_ga (event_id, area_id, offer_id, total, available, held, sold)
VALUES (
    '018f4e1a-0002-4000-8000-000000000001',
    'ga-snap-001',
    '018f4e1a-0020-4000-8000-000000000001',
    1000, 1000, 0, 0
)
ON CONFLICT (event_id, area_id, offer_id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- 10. INVENTORY — Seats RS (dùng seat ids của snapshot)
-- ---------------------------------------------------------------------------
INSERT INTO inventory_seat (event_id, seat_id, offer_id, status)
SELECT
    '018f4e1a-0002-4000-8000-000000000001',
    s.id,
    '018f4e1a-0021-4000-8000-000000000001',
    'AVAILABLE'
FROM seat s
WHERE s.row_id IN ('row-vip-a-snap', 'row-vip-b-snap')
ON CONFLICT (event_id, seat_id) DO NOTHING;

INSERT INTO inventory_seat (event_id, seat_id, offer_id, status)
SELECT
    '018f4e1a-0002-4000-8000-000000000001',
    s.id,
    '018f4e1a-0022-4000-8000-000000000001',
    'AVAILABLE'
FROM seat s
WHERE s.row_id IN ('row-reg-c-snap', 'row-reg-d-snap')
ON CONFLICT (event_id, seat_id) DO NOTHING;
