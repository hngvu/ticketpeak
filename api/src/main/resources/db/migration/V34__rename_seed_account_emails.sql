-- =============================================================================
-- V34 — Update seed account emails from ticketpeak.io to ticketpeak.com
-- =============================================================================

UPDATE account SET email = 'admin@ticketpeak.com'
WHERE email = 'admin@ticketpeak.io';

UPDATE account SET email = 'organizer@ticketpeak.com'
WHERE email = 'organizer@ticketpeak.io';

UPDATE account SET email = 'buyer@ticketpeak.com'
WHERE email = 'buyer@ticketpeak.io';
