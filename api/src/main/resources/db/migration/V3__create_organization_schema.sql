CREATE TABLE organization (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    bio TEXT,
    logo_url VARCHAR(255),
    website_url VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(255),
    city_id INTEGER,
    country_code VARCHAR(8),
    owner_account_id UUID NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE organization_member (
    id SERIAL PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organization (id) ON DELETE CASCADE,
    account_id UUID NOT NULL,
    status VARCHAR(32) NOT NULL,
    invited_by UUID,
    joined_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_organization_member_org_account UNIQUE (organization_id, account_id)
);

CREATE TABLE organization_invitation (
    id SERIAL PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organization (id) ON DELETE CASCADE,
    invitee_account_id UUID NOT NULL,
    invited_by UUID NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    responded_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
