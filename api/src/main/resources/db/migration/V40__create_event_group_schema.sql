-- =============================================================================
-- V40 — Create Event Group & Event Group Member schemas
-- =============================================================================

CREATE TABLE event_group (
    id              UUID PRIMARY KEY DEFAULT uuidv7(),
    organization_id UUID NOT NULL REFERENCES organization(id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    slug            VARCHAR(255) NOT NULL UNIQUE,
    description     TEXT,
    image_url       VARCHAR(255),
    is_active       BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE event_group_member (
    event_group_id UUID NOT NULL REFERENCES event_group(id) ON DELETE CASCADE,
    event_id       UUID NOT NULL REFERENCES event(id) ON DELETE CASCADE,
    display_order  INT NOT NULL DEFAULT 0,
    PRIMARY KEY (event_group_id, event_id)
);
