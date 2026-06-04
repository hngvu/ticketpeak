-- =============================================================================
-- V41 — Seed VENUE_MANAGER and PROMOTER accounts
-- Password for both accounts: Password123!
-- =============================================================================

INSERT INTO account (email, password, first_name, last_name, role, status, country_code, created_at, updated_at)
VALUES
    -- Venue manager
    (
        'manager@ticketpeak.com',
        crypt('Password123!', gen_salt('bf', 10)),
        'Venue',
        'Manager',
        'VENUE_MANAGER',
        'ACTIVE',
        NULL,
        NOW(),
        NOW()
    ),
    -- Promoter
    (
        'promoter@ticketpeak.com',
        crypt('Password123!', gen_salt('bf', 10)),
        'Promoter',
        'User',
        'PROMOTER',
        'ACTIVE',
        NULL,
        NOW(),
        NOW()
    )
ON CONFLICT (email) DO NOTHING;
