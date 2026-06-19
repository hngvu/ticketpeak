-- =============================================================================
-- V60 — Seed Venue Manager and Venue Manifest for Nhà hát Lớn Hà Nội
-- =============================================================================

-- 1. Ensure manager@ticketpeak.com exists in account
INSERT INTO account (email, password, first_name, last_name, status, created_at, updated_at)
VALUES (
    'manager@ticketpeak.com',
    crypt('Password123!', gen_salt('bf', 10)),
    'Venue',
    'Manager',
    'ACTIVE',
    NOW(), NOW()
)
ON CONFLICT (email) DO NOTHING;

-- 2. Ensure roles exist in account_role
INSERT INTO account_role (account_id, role)
SELECT id, 'VENUE_MANAGER' FROM account WHERE email = 'manager@ticketpeak.com'
ON CONFLICT DO NOTHING;

INSERT INTO account_role (account_id, role)
SELECT id, 'BUYER' FROM account WHERE email = 'manager@ticketpeak.com'
ON CONFLICT DO NOTHING;

-- 3. Ensure organization owned by manager@ticketpeak.com exists
INSERT INTO organization (
    id, name, slug, bio, email, country_code, owner_account_id, status, created_at, updated_at
)
VALUES (
    '018f4e1a-0000-4000-8000-000000000009',
    'Hanoi Opera Management',
    'hanoi-opera-management',
    'Ban Quản lý Nhà hát Lớn Hà Nội.',
    'manager@ticketpeak.com',
    'VN',
    (SELECT id FROM account WHERE email = 'manager@ticketpeak.com'),
    'ACTIVE',
    NOW(), NOW()
)
ON CONFLICT (id) DO NOTHING;

-- Add manager@ticketpeak.com to the organization members
INSERT INTO organization_member (organization_id, account_id, status, joined_at, updated_at)
SELECT
    '018f4e1a-0000-4000-8000-000000000009',
    id,
    'ACTIVE',
    NOW(), NOW()
FROM account WHERE email = 'manager@ticketpeak.com'
ON CONFLICT (organization_id, account_id) DO NOTHING;

-- 4. Seed Venue Manifest (M100001) for venue '018f4e1a-0006-4000-8000-000000000001' (Nhà hát Lớn Hà Nội)
INSERT INTO manifest (id, venue_id, description, total_capacity, status, objects, created_at, updated_at)
VALUES (
    'M100001',
    '018f4e1a-0006-4000-8000-000000000001',
    'Concert Layout – Nhà hát Lớn Hà Nội',
    580,
    'PUBLISHED',
    '[{"type": "stage", "text": "SÂN KHẤU", "x": 280, "y": 20, "width": 340, "height": 80}, {"type": "label", "text": "Mixer / Sound Booth", "x": 430, "y": 520, "fontSize": 12, "color": "#64748B"}]'::jsonb,
    NOW(), NOW()
)
ON CONFLICT (id) DO NOTHING;

-- 5. Seed Venue Levels
INSERT INTO venue_level (id, manifest_id, description, color)
VALUES
    ('L1', 'M100001', 'Orchestra (Tầng trệt)', '#3B82F6'),
    ('L2', 'M100001', 'Balcony (Tầng lầu)', '#8B5CF6')
ON CONFLICT (id, manifest_id) DO NOTHING;

-- 6. Seed Price Levels
INSERT INTO price_level (id, manifest_id, description, color)
VALUES
    ('PL-VIP', 'M100001', 'VIP – 2.500.000₫', '#F59E0B'),
    ('PL-PREM', 'M100001', 'Premium – 1.500.000₫', '#3B82F6'),
    ('PL-STD', 'M100001', 'Standard – 800.000₫', '#10B981')
ON CONFLICT (id, manifest_id) DO NOTHING;

-- 7. Seed Sections (Note: description and price_level_id were dropped from section in V51)
INSERT INTO section (id, manifest_id, name, color, type, level_id, capacity, ui_data)
VALUES
    (
        'M100001-SEC-VIP',
        'M100001',
        'VIP Front Row',
        '#F59E0B',
        'RS',
        'L1',
        32,
        '{"x": 220, "y": 140, "width": 460, "height": 120, "polygon": []}'::jsonb
    ),
    (
        'M100001-SEC-A',
        'M100001',
        'Standard A – Orchestra Center',
        '#3B82F6',
        'RS',
        'L1',
        64,
        '{"x": 220, "y": 280, "width": 460, "height": 180, "polygon": []}'::jsonb
    ),
    (
        'M100001-SEC-B',
        'M100001',
        'Standard B – Orchestra Wings',
        '#10B981',
        'RS',
        'L1',
        18,
        '{"x": 60, "y": 280, "width": 140, "height": 140, "polygon": []}'::jsonb
    ),
    (
        'M100001-SEC-C',
        'M100001',
        'Balcony Seats',
        '#8B5CF6',
        'RS',
        'L2',
        40,
        '{"x": 120, "y": 500, "width": 660, "height": 100, "polygon": []}'::jsonb
    ),
    (
        'M100001-GA-LOBBY',
        'M100001',
        'General Admission - Lobby',
        '#10B981',
        'GA',
        'L1',
        50,
        '{"x": 60, "y": 140, "width": 140, "height": 100, "polygon": []}'::jsonb
    )
ON CONFLICT (id) DO NOTHING;

-- 8. Seed Seat Rows & Seats
-- Section: VIP Front Row (M100001-SEC-VIP) - 2 rows A, B; 16 seats each
INSERT INTO seat_row (id, section_id, name)
VALUES
    ('M100001-SEC-VIP-row-A', 'M100001-SEC-VIP', 'A'),
    ('M100001-SEC-VIP-row-B', 'M100001-SEC-VIP', 'B')
ON CONFLICT (id) DO NOTHING;

INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
SELECT
    sr.id || '-s' || lpad(s.val::text, 3, '0'),
    sr.id,
    lpad(s.val::text, 3, '0'),
    240 + (s.val - 1) * 18,
    CASE WHEN sr.name = 'A' THEN 150 ELSE 172 END,
    'AVAILABLE',
    'PL-VIP',
    'M100001-SEC-VIP'
FROM seat_row sr
CROSS JOIN generate_series(1, 16) AS s(val)
WHERE sr.section_id = 'M100001-SEC-VIP'
ON CONFLICT (id) DO NOTHING;

-- Section: Standard A (M100001-SEC-A) - 4 rows C, D, E, F; 16 seats each
INSERT INTO seat_row (id, section_id, name)
VALUES
    ('M100001-SEC-A-row-C', 'M100001-SEC-A', 'C'),
    ('M100001-SEC-A-row-D', 'M100001-SEC-A', 'D'),
    ('M100001-SEC-A-row-E', 'M100001-SEC-A', 'E'),
    ('M100001-SEC-A-row-F', 'M100001-SEC-A', 'F')
ON CONFLICT (id) DO NOTHING;

INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
SELECT
    sr.id || '-s' || lpad(s.val::text, 3, '0'),
    sr.id,
    lpad(s.val::text, 3, '0'),
    240 + (s.val - 1) * 18,
    290 + (ascii(sr.name) - 67) * 22,
    'AVAILABLE',
    'PL-PREM',
    'M100001-SEC-A'
FROM seat_row sr
CROSS JOIN generate_series(1, 16) AS s(val)
WHERE sr.section_id = 'M100001-SEC-A'
ON CONFLICT (id) DO NOTHING;

-- Section: Standard B (M100001-SEC-B) - 3 rows L, M, N; 6 seats each
INSERT INTO seat_row (id, section_id, name)
VALUES
    ('M100001-SEC-B-row-L', 'M100001-SEC-B', 'L'),
    ('M100001-SEC-B-row-M', 'M100001-SEC-B', 'M'),
    ('M100001-SEC-B-row-N', 'M100001-SEC-B', 'N')
ON CONFLICT (id) DO NOTHING;

INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
SELECT
    sr.id || '-s' || lpad(s.val::text, 3, '0'),
    sr.id,
    lpad(s.val::text, 3, '0'),
    70 + (s.val - 1) * 18,
    290 + (ascii(sr.name) - 76) * 22,
    'AVAILABLE',
    'PL-STD',
    'M100001-SEC-B'
FROM seat_row sr
CROSS JOIN generate_series(1, 6) AS s(val)
WHERE sr.section_id = 'M100001-SEC-B'
ON CONFLICT (id) DO NOTHING;

-- Section: Balcony (M100001-SEC-C) - 2 rows P, Q; 20 seats each
INSERT INTO seat_row (id, section_id, name)
VALUES
    ('M100001-SEC-C-row-P', 'M100001-SEC-C', 'P'),
    ('M100001-SEC-C-row-Q', 'M100001-SEC-C', 'Q')
ON CONFLICT (id) DO NOTHING;

INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
SELECT
    sr.id || '-s' || lpad(s.val::text, 3, '0'),
    sr.id,
    lpad(s.val::text, 3, '0'),
    140 + (s.val - 1) * 18,
    510 + (ascii(sr.name) - 80) * 22,
    'AVAILABLE',
    'PL-STD',
    'M100001-SEC-C'
FROM seat_row sr
CROSS JOIN generate_series(1, 20) AS s(val)
WHERE sr.section_id = 'M100001-SEC-C'
ON CONFLICT (id) DO NOTHING;
