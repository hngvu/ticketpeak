-- =============================================================================
-- V32 — Seed test accounts
-- Password for all accounts: Password123!
-- Uses pgcrypto crypt() with bf (bcrypt) cost=10 — compatible with Spring
-- Security BCryptPasswordEncoder ($2a$ format)
-- =============================================================================

INSERT INTO account (email, password, first_name, last_name, role, status, country_code, created_at, updated_at)
VALUES
    -- Platform admin
    (
        'admin@ticketpeak.io',
        crypt('Password123!', gen_salt('bf', 10)),
        'Admin',
        'Ticketpeak',
        'ADMIN',
        'ACTIVE',
        NULL,
        NOW(),
        NOW()
    ),
    -- Event organizer
    (
        'organizer@ticketpeak.io',
        crypt('Password123!', gen_salt('bf', 10)),
        'Nguyen',
        'Organizer',
        'ORGANIZER',
        'ACTIVE',
        NULL,
        NOW(),
        NOW()
    ),
    -- Regular buyer
    (
        'buyer@ticketpeak.io',
        crypt('Password123!', gen_salt('bf', 10)),
        'Tran',
        'Buyer',
        'BUYER',
        'ACTIVE',
        NULL,
        NOW(),
        NOW()
    )
ON CONFLICT (email) DO NOTHING;

