-- Fix seeded organization status to match OrganizationStatus enum (ACTIVE, SUSPENDED, CLOSED)
UPDATE organization SET status = 'ACTIVE' WHERE status = 'APPROVED';
