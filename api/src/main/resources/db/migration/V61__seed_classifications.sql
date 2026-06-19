-- V61__seed_classifications.sql
-- Seed default classifications for the platform (Music, Sports, Arts, etc.)

-- 1. Music and sub-categories
INSERT INTO classification (id, name, slug, level, is_active, parent_id) VALUES
    ('018f5000-0000-4000-8000-000000000001', 'Music', 'music', 1, true, null),
    ('018f5000-0000-4000-8000-000000000002', 'Concerts', 'concerts', 2, true, '018f5000-0000-4000-8000-000000000001'),
    ('018f5000-0000-4000-8000-000000000003', 'Festivals', 'festivals', 2, true, '018f5000-0000-4000-8000-000000000001');

-- 2. Sports and sub-categories
INSERT INTO classification (id, name, slug, level, is_active, parent_id) VALUES
    ('018f5000-0000-4000-8000-000000000004', 'Sports', 'sports', 1, true, null),
    ('018f5000-0000-4000-8000-000000000005', 'Football', 'football', 2, true, '018f5000-0000-4000-8000-000000000004'),
    ('018f5000-0000-4000-8000-000000000006', 'Basketball', 'basketball', 2, true, '018f5000-0000-4000-8000-000000000004'),
    ('018f5000-0000-4000-8000-000000000007', 'Esports', 'esports', 2, true, '018f5000-0000-4000-8000-000000000004');

-- 3. Arts & Theater
INSERT INTO classification (id, name, slug, level, is_active, parent_id) VALUES
    ('018f5000-0000-4000-8000-000000000008', 'Arts & Theater', 'arts-theater', 1, true, null),
    ('018f5000-0000-4000-8000-000000000009', 'Comedy', 'comedy', 2, true, '018f5000-0000-4000-8000-000000000008'),
    ('018f5000-0000-4000-8000-000000000010', 'Musicals', 'musicals', 2, true, '018f5000-0000-4000-8000-000000000008'),
    ('018f5000-0000-4000-8000-000000000011', 'Classical', 'classical', 2, true, '018f5000-0000-4000-8000-000000000008');

-- 4. Film
INSERT INTO classification (id, name, slug, level, is_active, parent_id) VALUES
    ('018f5000-0000-4000-8000-000000000012', 'Film', 'film', 1, true, null);

-- 5. Family
INSERT INTO classification (id, name, slug, level, is_active, parent_id) VALUES
    ('018f5000-0000-4000-8000-000000000013', 'Family', 'family', 1, true, null);

-- Link the seeded event in V36 ('Đêm nhạc Hà Nội Mùa Thu 2026') to 'Music' and 'Concerts'
INSERT INTO event_classification (event_id, classification_id)
SELECT '018f4e1a-0002-4000-8000-000000000001', id
FROM classification
WHERE slug IN ('music', 'concerts')
ON CONFLICT DO NOTHING;
