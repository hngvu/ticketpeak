-- =============================================================================
-- V33 — Seed foundation data: country, city (VN + US), IAM permissions
-- =============================================================================

-- ---------------------------------------------------------------------------
-- 1. COUNTRIES
-- ---------------------------------------------------------------------------
INSERT INTO country (code, name, slug, currency, is_active) VALUES
    ('VN', 'Việt Nam',       'viet-nam',       'VND', true),
    ('US', 'United States',  'united-states',  'USD', true),
    ('AU', 'Australia',      'australia',      'AUD', true),
    ('GB', 'United Kingdom', 'united-kingdom', 'GBP', true),
    ('SG', 'Singapore',      'singapore',      'SGD', true),
    ('JP', 'Japan',          'japan',          'JPY', true),
    ('KR', 'South Korea',    'south-korea',    'KRW', true),
    ('TH', 'Thailand',       'thailand',       'THB', true)
ON CONFLICT (code) DO NOTHING;

-- ---------------------------------------------------------------------------
-- 2. CITIES — Vietnam
-- ---------------------------------------------------------------------------
INSERT INTO city (name, slug, latitude, longitude, timezone, is_active, country_code) VALUES
    ('Hà Nội',        'ha-noi',         '21.0285',  '105.8542', 'Asia/Ho_Chi_Minh', true, 'VN'),
    ('TP. Hồ Chí Minh','ho-chi-minh',  '10.8231',  '106.6297', 'Asia/Ho_Chi_Minh', true, 'VN'),
    ('Đà Nẵng',       'da-nang',        '16.0544',  '108.2022', 'Asia/Ho_Chi_Minh', true, 'VN'),
    ('Hải Phòng',     'hai-phong',      '20.8449',  '106.6881', 'Asia/Ho_Chi_Minh', true, 'VN'),
    ('Cần Thơ',       'can-tho',        '10.0452',  '105.7469', 'Asia/Ho_Chi_Minh', true, 'VN'),
    ('Nha Trang',     'nha-trang',      '12.2388',  '109.1967', 'Asia/Ho_Chi_Minh', true, 'VN'),
    ('Huế',           'hue',            '16.4637',  '107.5909', 'Asia/Ho_Chi_Minh', true, 'VN'),
    ('Đà Lạt',        'da-lat',         '11.9465',  '108.4419', 'Asia/Ho_Chi_Minh', true, 'VN'),
    ('Vũng Tàu',      'vung-tau',       '10.4113',  '107.1360', 'Asia/Ho_Chi_Minh', true, 'VN'),
    ('Quy Nhơn',      'quy-nhon',       '13.7757',  '109.2233', 'Asia/Ho_Chi_Minh', true, 'VN')
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------------------
-- 3. CITIES — United States
-- ---------------------------------------------------------------------------
INSERT INTO city (name, slug, latitude, longitude, timezone, is_active, country_code) VALUES
    ('New York',     'new-york',      '40.7128',  '-74.0060',  'America/New_York',    true, 'US'),
    ('Los Angeles',  'los-angeles',   '34.0522',  '-118.2437', 'America/Los_Angeles', true, 'US'),
    ('Chicago',      'chicago',       '41.8781',  '-87.6298',  'America/Chicago',     true, 'US'),
    ('Houston',      'houston',       '29.7604',  '-95.3698',  'America/Chicago',     true, 'US'),
    ('Las Vegas',    'las-vegas',     '36.1699',  '-115.1398', 'America/Los_Angeles', true, 'US'),
    ('Miami',        'miami',         '25.7617',  '-80.1918',  'America/New_York',    true, 'US'),
    ('Seattle',      'seattle',       '47.6062',  '-122.3321', 'America/Los_Angeles', true, 'US'),
    ('San Francisco','san-francisco', '37.7749',  '-122.4194', 'America/Los_Angeles', true, 'US')
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------------------
-- 4. CITIES — Other countries
-- ---------------------------------------------------------------------------
INSERT INTO city (name, slug, latitude, longitude, timezone, is_active, country_code) VALUES
    ('Sydney',    'sydney',    '-33.8688', '151.2093', 'Australia/Sydney',    true, 'AU'),
    ('Melbourne', 'melbourne', '-37.8136', '144.9631', 'Australia/Melbourne', true, 'AU'),
    ('London',    'london',    '51.5074',  '-0.1278',  'Europe/London',       true, 'GB'),
    ('Singapore', 'singapore', '1.3521',   '103.8198', 'Asia/Singapore',      true, 'SG'),
    ('Tokyo',     'tokyo',     '35.6762',  '139.6503', 'Asia/Tokyo',          true, 'JP'),
    ('Seoul',     'seoul',     '37.5665',  '126.9780', 'Asia/Seoul',          true, 'KR'),
    ('Bangkok',   'bangkok',   '13.7563',  '100.5018', 'Asia/Bangkok',        true, 'TH')
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------------------
-- 5. UPDATE seeded accounts with country & city now that refs exist
-- ---------------------------------------------------------------------------
UPDATE account SET country_code = 'VN' WHERE email IN (
    'admin@ticketpeak.io',
    'organizer@ticketpeak.io',
    'buyer@ticketpeak.io'
);

-- ---------------------------------------------------------------------------
-- 6. IAM — Platform-scoped permissions (ADMIN operations)
-- ---------------------------------------------------------------------------
INSERT INTO permission (code, name, scope, action, resource) VALUES
    -- Organization management
    ('ORG:APPROVE',         'Approve organization',        'PLATFORM', 'APPROVE',  'ORGANIZATION'),
    ('ORG:REJECT',          'Reject organization',         'PLATFORM', 'REJECT',   'ORGANIZATION'),
    ('ORG:SUSPEND',         'Suspend organization',        'PLATFORM', 'SUSPEND',  'ORGANIZATION'),

    -- Event management (platform admin)
    ('EVENT:FEATURE',       'Feature event on homepage',   'PLATFORM', 'FEATURE',  'EVENT'),
    ('EVENT:CANCEL_ANY',    'Cancel any event',            'PLATFORM', 'CANCEL',   'EVENT'),

    -- User/account management
    ('ACCOUNT:BAN',         'Ban a user account',          'PLATFORM', 'BAN',      'ACCOUNT'),
    ('ACCOUNT:UNBAN',       'Unban a user account',        'PLATFORM', 'UNBAN',    'ACCOUNT'),

    -- Payout management
    ('PAYOUT:APPROVE',      'Approve organizer payout',    'PLATFORM', 'APPROVE',  'PAYOUT'),
    ('PAYOUT:REJECT',       'Reject organizer payout',     'PLATFORM', 'REJECT',   'PAYOUT'),

    -- Venue management (platform)
    ('VENUE:CREATE_ANY',    'Create venue for any org',    'PLATFORM', 'CREATE',   'VENUE'),
    ('VENUE:UPDATE_ANY',    'Update any venue',            'PLATFORM', 'UPDATE',   'VENUE')
ON CONFLICT (code) DO NOTHING;

-- ---------------------------------------------------------------------------
-- 7. IAM — Organization-scoped permissions (ORGANIZER delegation)
-- ---------------------------------------------------------------------------
INSERT INTO permission (code, name, scope, action, resource) VALUES
    -- Event management within org
    ('EVENT:CREATE',        'Create events',               'ORGANIZATION', 'CREATE',  'EVENT'),
    ('EVENT:UPDATE',        'Update events',               'ORGANIZATION', 'UPDATE',  'EVENT'),
    ('EVENT:PUBLISH',       'Publish events',              'ORGANIZATION', 'PUBLISH', 'EVENT'),
    ('EVENT:CANCEL',        'Cancel own events',           'ORGANIZATION', 'CANCEL',  'EVENT'),

    -- Offer / ticket types
    ('OFFER:MANAGE',        'Manage offers & pricing',     'ORGANIZATION', 'MANAGE',  'OFFER'),

    -- Member management
    ('ORG_MEMBER:INVITE',   'Invite members to org',       'ORGANIZATION', 'INVITE',  'ORG_MEMBER'),
    ('ORG_MEMBER:REMOVE',   'Remove members from org',     'ORGANIZATION', 'REMOVE',  'ORG_MEMBER'),
    ('ORG_MEMBER:MANAGE',   'Manage member roles',         'ORGANIZATION', 'MANAGE',  'ORG_MEMBER'),

    -- Venue management within org
    ('VENUE:CREATE',        'Create venues for own org',   'ORGANIZATION', 'CREATE',  'VENUE'),
    ('VENUE:UPDATE',        'Update own venues',           'ORGANIZATION', 'UPDATE',  'VENUE'),

    -- Reporting
    ('REPORT:VIEW',         'View sales & attendance reports', 'ORGANIZATION', 'VIEW', 'REPORT'),

    -- Check-in
    ('CHECKIN:SCAN',        'Scan tickets at the door',    'ORGANIZATION', 'SCAN',    'CHECKIN'),

    -- Payout
    ('PAYOUT:REQUEST',      'Request organizer payout',    'ORGANIZATION', 'REQUEST', 'PAYOUT'),
    ('PAYOUT:VIEW',         'View payout history',         'ORGANIZATION', 'VIEW',    'PAYOUT'),

    -- Resale configuration
    ('RESALE:CONFIGURE',    'Configure resale settings',   'ORGANIZATION', 'CONFIGURE', 'RESALE')
ON CONFLICT (code) DO NOTHING;
