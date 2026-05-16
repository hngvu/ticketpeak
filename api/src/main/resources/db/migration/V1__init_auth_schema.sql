CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE account (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    avatar_url VARCHAR(255),
    birth_date DATE,
    gender VARCHAR(32),
    city_id INTEGER,
    country_code VARCHAR(8),
    role VARCHAR(32) NOT NULL,
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    status VARCHAR(32) NOT NULL
);

CREATE TABLE permission (
    code VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    scope VARCHAR(32) NOT NULL,
    action VARCHAR(255),
    resource VARCHAR(255)
);

CREATE TABLE account_permission (
    id BIGSERIAL PRIMARY KEY,
    account_id UUID NOT NULL,
    organization_id UUID NOT NULL,
    permission_code VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    granted_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    granted_by UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    revoked_by UUID,
    CONSTRAINT uq_account_permission_account_permission_organization UNIQUE (account_id, permission_code, organization_id),
    CONSTRAINT fk_account_permission_permission FOREIGN KEY (permission_code) REFERENCES permission (code)
);
