-- =============================================================================
-- V49 — Seed additional Vietnamese venues
--
-- Bổ sung các địa điểm tổ chức sự kiện nổi tiếng tại Việt Nam
-- để admin có dữ liệu sẵn khi vận hành platform.
--
-- Liên kết với account organizer@ticketpeak.com (id mặc định của V32).
-- =============================================================================

-- ---------------------------------------------------------------------------
-- 1. Sân vận động Quốc gia Mỹ Đình — Hà Nội
-- ---------------------------------------------------------------------------
INSERT INTO venue (id, name, address, city, country, latitude, longitude, phone, email, website, description, status, created_at, updated_at)
VALUES (
    '018f4e1a-0003-4000-8000-000000000001',
    'Sân vận động Quốc gia Mỹ Đình',
    'Đường Lê Đức Thọ, Mỹ Đình 1, Nam Từ Liêm',
    'Hà Nội',
    'Việt Nam',
    21.020504, 105.773824,
    '+84 24 3785 0789',
    'info@mydinhstadium.vn',
    'https://mydinhstadium.vn',
    'Sân vận động quốc gia lớn nhất Việt Nam, sức chứa 40.000 chỗ ngồi. Địa điểm tổ chức các trận đấu bóng đá quốc tế và concert lớn.',
    'ACTIVE', NOW(), NOW()
)
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- 2. Sân vận động Quân khu 7 — TP. Hồ Chí Minh
-- ---------------------------------------------------------------------------
INSERT INTO venue (id, name, address, city, country, latitude, longitude, phone, email, website, description, status, created_at, updated_at)
VALUES (
    '018f4e1a-0004-4000-8000-000000000001',
    'Sân vận động Quân khu 7',
    '202 Hoàng Văn Thụ, Phường 9, Phú Nhuận',
    'TP. Hồ Chí Minh',
    'Việt Nam',
    10.799511, 106.666691,
    '+84 28 3844 1234',
    'contact@qk7stadium.vn',
    NULL,
    'Sân vận động đa năng tại trung tâm TP.HCM, sức chứa 25.000. Thường xuyên tổ chức concert và sự kiện thể thao.',
    'ACTIVE', NOW(), NOW()
)
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- 3. Nhà thi đấu Phú Thọ — TP. Hồ Chí Minh
-- ---------------------------------------------------------------------------
INSERT INTO venue (id, name, address, city, country, latitude, longitude, phone, email, website, description, status, created_at, updated_at)
VALUES (
    '018f4e1a-0005-4000-8000-000000000001',
    'Nhà thi đấu Phú Thọ',
    '1 Lữ Gia, Phường 15, Quận 11',
    'TP. Hồ Chí Minh',
    'Việt Nam',
    10.763511, 106.656811,
    '+84 28 3865 5678',
    'booking@phuthoarena.vn',
    'https://phuthoarena.vn',
    'Nhà thi đấu trong nhà hiện đại, phù hợp tổ chức concert, sự kiện thể thao và hội chợ.',
    'ACTIVE', NOW(), NOW()
)
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- 4. Nhà hát Lớn Hà Nội — Hà Nội
-- ---------------------------------------------------------------------------
INSERT INTO venue (id, name, address, city, country, latitude, longitude, phone, email, website, description, status, created_at, updated_at)
VALUES (
    '018f4e1a-0006-4000-8000-000000000001',
    'Nhà hát Lớn Hà Nội',
    '1 Tràng Tiền, Phan Chu Trinh, Hoàn Kiếm',
    'Hà Nội',
    'Việt Nam',
    21.024222, 105.855833,
    '+84 24 3933 1488',
    'info@hanoiopera.org.vn',
    'https://hanoiopera.org.vn',
    'Công trình kiến trúc Pháp cổ điển, trung tâm văn hóa nghệ thuật biểu diễn hàng đầu thủ đô.',
    'ACTIVE', NOW(), NOW()
)
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- 5. Cung thể thao Tuyên Sơn — Đà Nẵng
-- ---------------------------------------------------------------------------
INSERT INTO venue (id, name, address, city, country, latitude, longitude, phone, email, website, description, status, created_at, updated_at)
VALUES (
    '018f4e1a-0007-4000-8000-000000000001',
    'Cung thể thao Tuyên Sơn',
    'Phan Đăng Lưu, Hòa Cường Bắc, Hải Châu',
    'Đà Nẵng',
    'Việt Nam',
    16.031511, 108.225911,
    '+84 236 3621 789',
    'info@tuyensonarena.vn',
    'https://tuyensonarena.vn',
    'Trung tâm thể thao và sự kiện hiện đại hàng đầu tại Đà Nẵng, sức chứa 7.000 chỗ ngồi.',
    'ACTIVE', NOW(), NOW()
)
ON CONFLICT (id) DO NOTHING;
