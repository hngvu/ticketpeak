-- =============================================================================
-- V42 — Create account_role join table for multi-role support
-- Migrates existing single role column to the new join table.
-- =============================================================================

CREATE TABLE account_role (
    account_id UUID NOT NULL REFERENCES account(id) ON DELETE CASCADE,
    role VARCHAR(32) NOT NULL,
    PRIMARY KEY (account_id, role)
);

INSERT INTO account_role (account_id, role)
SELECT id, role FROM account;

ALTER TABLE account DROP COLUMN role;
