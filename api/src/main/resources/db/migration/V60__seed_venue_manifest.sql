-- =============================================================================
-- V60 — Seed Venue Manager and Venue Manifest for Nhà hát Lớn Hà Nội
--
-- Layout (canvas coordinates, x tăng phải, y tăng xuống):
--   Stage:   x=0..160,  y=80..760  (dọc bên trái)
--   VIP:     rows A-D,  16 seats/row, x bắt đầu từ 180
--   Premium: rows E-M,  18 seats/row, x bắt đầu từ 180
--   Standard: rows N-U, 16 seats/row, x bắt đầu từ 180
--   GA Lobby: phía trước bên trái
--
-- Capacity thực tế:
--   VIP      4  rows × 16 = 64
--   Premium  9  rows × 18 = 162
--   Standard 8  rows × 16 = 128
--   GA Lobby capacity    = 50
--   TOTAL                = 404
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
--    Stage object: dọc bên trái, x=0, y=80, width=160, height=680
--    Label: Sound booth ở cuối phòng
INSERT INTO manifest (id, venue_id, description, total_capacity, status, objects, created_at, updated_at)
VALUES (
    'M100001',
    '018f4e1a-0006-4000-8000-000000000001',
    'Concert Layout – Nhà hát Lớn Hà Nội',
    404,
    'PUBLISHED',
    '[{"type": "stage", "text": "STAGE", "x": 0, "y": 80, "width": 160, "height": 680}]'::jsonb,
    NOW(), NOW()
)
ON CONFLICT (id) DO NOTHING;

-- 5. Seed Venue Levels (chỉ 1 tầng – Nhà hát Lớn có 1 tầng chính)
INSERT INTO venue_level (id, manifest_id, description, color)
VALUES
    ('L1', 'M100001', 'Main Floor', '#3B82F6')
ON CONFLICT (id, manifest_id) DO NOTHING;

-- 6. Seed Price Levels
INSERT INTO price_level (id, manifest_id, description, color)
VALUES
    ('PL-VIP',  'M100001', 'VIP – 2.500.000₫',     '#F59E0B'),
    ('PL-PREM', 'M100001', 'Premium – 1.500.000₫',  '#3B82F6'),
    ('PL-STD',  'M100001', 'Standard – 800.000₫',   '#EC4899')
ON CONFLICT (id, manifest_id) DO NOTHING;

-- 7. Seed Sections
--    Mỗi section lưu ui_data với polygon rỗng (hull sẽ được tính từ seats trên canvas)
--    x/y/width/height dùng làm fallback bounding box khi polygon chưa có
--
--    Tọa độ section bao quanh seats:
--      VIP rows A-D:      y = 100..182,  x = 180..520  → box (180,88,356,112)
--      Premium rows E-M:  y = 200..380,  x = 180..556  → box (180,188,392,208)
--      Standard rows N-U: y = 400..554,  x = 180..520  → box (180,388,356,182)
--      GA Lobby:          fixed box
INSERT INTO section (id, manifest_id, name, color, type, level_id, capacity, ui_data)
VALUES
    (
        'M100001-SEC-VIP',
        'M100001',
        'Front',
        '#F59E0B',
        'RS',
        'L1',
        64,
        '{"x": 180, "y": 80, "width": 356, "height": 120, "polygon": []}'::jsonb
    ),
    (
        'M100001-SEC-PREM',
        'M100001',
        'Center',
        '#3B82F6',
        'RS',
        'L1',
        162,
        '{"x": 180, "y": 220, "width": 396, "height": 234, "polygon": []}'::jsonb
    ),
    (
        'M100001-SEC-STD',
        'M100001',
        'Rear',
        '#EC4899',
        'RS',
        'L1',
        128,
        '{"x": 180, "y": 474, "width": 356, "height": 208, "polygon": []}'::jsonb
    ),
    (
        'M100001-SEC-GA',
        'M100001',
        'GA Lobby',
        '#10B981',
        'GA',
        'L1',
        50,
        '{"x": 620, "y": 88, "width": 160, "height": 582, "polygon": []}'::jsonb
    )
ON CONFLICT (id) DO NOTHING;

-- 8. Seed Seat Rows & Seats
--
-- Quy ước tọa độ:
--   seat_x_start = 188           (cạnh phải của stage + 28px gap)
--   seat_spacing = 22            (px giữa các seat)
--   row_y_start  = 100           (y của row A)
--   row_spacing  = 26            (px giữa các row)
--
-- VIP: rows A–D (4 rows × 16 seats), price_level = PL-VIP
--   row A: y=100, row B: y=126, row C: y=152, row D: y=178
-- Premium: rows E–M (9 rows × 18 seats), price_level = PL-PREM
--   row E: y=204, … row M: y=412  (step 26; nhưng gap +26 từ VIP)
-- Standard: rows N–U (8 rows × 16 seats), price_level = PL-STD
--   row N: y=438, … row U: y=620

-- ── VIP rows ───────────────────────────────────────────────────────────────
INSERT INTO seat_row (id, section_id, name)
SELECT
    'M100001-SEC-VIP-row-' || chr(64 + r.n),
    'M100001-SEC-VIP',
    chr(64 + r.n)
FROM generate_series(1, 4) AS r(n)
ON CONFLICT (id) DO NOTHING;

INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
SELECT
    'M100001-SEC-VIP-row-' || chr(64 + r.n) || '-s' || lpad(s.val::text, 3, '0'),
    'M100001-SEC-VIP-row-' || chr(64 + r.n),
    lpad(s.val::text, 3, '0'),
    188 + (s.val - 1) * 22,
    100 + (r.n - 1) * 26,
    'AVAILABLE',
    'PL-VIP',
    'M100001-SEC-VIP'
FROM generate_series(1, 4)  AS r(n)
CROSS JOIN generate_series(1, 16) AS s(val)
ON CONFLICT (id) DO NOTHING;

-- ── Premium rows ────────────────────────────────────────────────────────────
INSERT INTO seat_row (id, section_id, name)
SELECT
    'M100001-SEC-PREM-row-' || chr(64 + r.n),
    'M100001-SEC-PREM',
    chr(64 + r.n)
FROM generate_series(5, 13) AS r(n)   -- E(5) .. M(13)
ON CONFLICT (id) DO NOTHING;

INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
SELECT
    'M100001-SEC-PREM-row-' || chr(64 + r.n) || '-s' || lpad(s.val::text, 3, '0'),
    'M100001-SEC-PREM-row-' || chr(64 + r.n),
    lpad(s.val::text, 3, '0'),
    188 + (s.val - 1) * 22,
    204 + (r.n - 5) * 26,   -- row E(n=5) → y=204, row M(n=13) → y=412
    'AVAILABLE',
    'PL-PREM',
    'M100001-SEC-PREM'
FROM generate_series(5, 13)  AS r(n)
CROSS JOIN generate_series(1, 18) AS s(val)
ON CONFLICT (id) DO NOTHING;

-- ── Standard rows ───────────────────────────────────────────────────────────
INSERT INTO seat_row (id, section_id, name)
SELECT
    'M100001-SEC-STD-row-' || chr(64 + r.n),
    'M100001-SEC-STD',
    chr(64 + r.n)
FROM generate_series(14, 21) AS r(n)  -- N(14) .. U(21)
ON CONFLICT (id) DO NOTHING;

INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
SELECT
    'M100001-SEC-STD-row-' || chr(64 + r.n) || '-s' || lpad(s.val::text, 3, '0'),
    'M100001-SEC-STD-row-' || chr(64 + r.n),
    lpad(s.val::text, 3, '0'),
    188 + (s.val - 1) * 22,
    438 + (r.n - 14) * 26,  -- row N(n=14) → y=438, row U(n=21) → y=620
    'AVAILABLE',
    'PL-STD',
    'M100001-SEC-STD'
FROM generate_series(14, 21) AS r(n)
CROSS JOIN generate_series(1, 16) AS s(val)
ON CONFLICT (id) DO NOTHING;
