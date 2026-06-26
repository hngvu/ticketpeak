-- =============================================================================
-- V64 — Hide Unused Venues
-- =============================================================================

-- Hide the venues that were seeded previously but are not part of the 4 requested venues.
UPDATE venue
SET status = 'INACTIVE'
WHERE id IN (
    '018f4e1a-0002-4000-8000-000000000001', -- The one from V36 (has events)
    '018f4e1a-0007-4000-8000-000000000001'  -- Cung the thao Tuyen Son from V49
);
