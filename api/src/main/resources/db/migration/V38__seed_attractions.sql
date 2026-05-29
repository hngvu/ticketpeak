-- =============================================================================
-- V38 — Seed Attractions & Event Attraction Mappings
-- =============================================================================

-- 1. SEED ATTRACTIONS
INSERT INTO attraction (id, name, slug, type, image_url, description) VALUES
    (
        '018fbe92-bc24-7476-80db-31cccefa9b61',
        'Đen Vâu',
        'den-vau',
        'ARTIST',
        'https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?auto=format&fit=crop&w=400&q=80',
        'Đen Vâu (Nguyễn Đức Cường) là một nam rapper và nhạc sĩ người Việt Nam. Đen Vâu được coi là một trong những nghệ sĩ hip hop thành công nhất tại Việt Nam, sở hữu nhiều ca khúc đứng đầu bảng xếp hạng âm nhạc Việt.'
    ),
    (
        '018fbe92-bc24-7476-80db-31cccefa9b62',
        'Sơn Tùng M-TP',
        'son-tung-m-tp',
        'ARTIST',
        'https://images.unsplash.com/photo-1506157786151-b8491531f063?auto=format&fit=crop&w=400&q=80',
        'Sơn Tùng M-TP (Nguyễn Thanh Tùng) là một nam ca sĩ, nhạc sĩ và diễn viên người Việt Nam. Anh nổi tiếng với phong cách âm nhạc độc đáo, giọng hát nội lực và vũ đạo điêu luyện.'
    ),
    (
        '018fbe92-bc24-7476-80db-31cccefa9b63',
        'Taylor Swift',
        'taylor-swift',
        'ARTIST',
        'https://images.unsplash.com/photo-1543794327-59a91fb7de1d?auto=format&fit=crop&w=400&q=80',
        'Taylor Swift is an American singer-songwriter. Known for her songwriting, musical versatility, artistic reinventions, and influence on the music industry.'
    )
ON CONFLICT (id) DO NOTHING;

-- 2. SEED EVENT ATTRACTION MAPPINGS (Link to Đêm nhạc Hà Nội Mùa Thu 2026)
INSERT INTO event_attraction (event_id, attraction_id) VALUES
    ('018f4e1a-0002-4000-8000-000000000001', '018fbe92-bc24-7476-80db-31cccefa9b61'), -- Đen Vâu
    ('018f4e1a-0002-4000-8000-000000000001', '018fbe92-bc24-7476-80db-31cccefa9b62')  -- Sơn Tùng M-TP
ON CONFLICT (event_id, attraction_id) DO NOTHING;
