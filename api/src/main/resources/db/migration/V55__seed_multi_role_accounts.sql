-- =============================================================================
-- V43 — Clean up PROMOTER role and seed multi-role accounts
-- PROMOTER is removed from the Role enum, so any existing PROMOTER entries
-- in account_role (migrated from V41) must be cleaned up before the app
-- starts. This migration also creates seed accounts with .com emails and
-- grants multi-role assignments.
-- The buyer account carries extra roles (ORGANIZER, VENUE_MANAGER) for
-- testing multi-role scenarios.
-- =============================================================================

-- Clean up PROMOTER entries from the migrated data
DELETE FROM account_role WHERE role = 'PROMOTER';

-- Create seed accounts with .com emails (no role column — V42 dropped it)
INSERT INTO account (email, password, first_name, last_name, status, country_code, created_at, updated_at)
VALUES
    (
        'admin@ticketpeak.com',
        crypt('Password123!', gen_salt('bf', 10)),
        'Admin',
        'Ticketpeak',
        'ACTIVE',
        NULL,
        NOW(),
        NOW()
    ),
    (
        'organizer@ticketpeak.com',
        crypt('Password123!', gen_salt('bf', 10)),
        'Nguyen',
        'Organizer',
        'ACTIVE',
        NULL,
        NOW(),
        NOW()
    ),
    (
        'buyer@ticketpeak.com',
        crypt('Password123!', gen_salt('bf', 10)),
        'Tran',
        'Buyer',
        'ACTIVE',
        NULL,
        NOW(),
        NOW()
    )
ON CONFLICT (email) DO NOTHING;

-- Admin only needs ADMIN role (bypasses all permission checks)
INSERT INTO account_role (account_id, role)
SELECT id, 'ADMIN' FROM account WHERE email = 'admin@ticketpeak.com'
ON CONFLICT DO NOTHING;

-- Organizer gets ORGANIZER role
INSERT INTO account_role (account_id, role)
SELECT id, 'BUYER' FROM account WHERE email = 'organizer@ticketpeak.com'
ON CONFLICT DO NOTHING;
INSERT INTO account_role (account_id, role)
SELECT id, 'ORGANIZER' FROM account WHERE email = 'organizer@ticketpeak.com'
ON CONFLICT DO NOTHING;

-- Buyer gets multiple roles for testing
INSERT INTO account_role (account_id, role)
SELECT id, 'BUYER' FROM account WHERE email = 'buyer@ticketpeak.com'
ON CONFLICT DO NOTHING;

-- Grant additional roles to existing .com accounts from V41
INSERT INTO account_role (account_id, role)
SELECT id, 'BUYER' FROM account WHERE email = 'venue@ticketpeak.com'
ON CONFLICT DO NOTHING;
INSERT INTO account_role (account_id, role)
SELECT id, 'VENUE_MANAGER' FROM account WHERE email = 'venue@ticketpeak.com'
ON CONFLICT DO NOTHING;

