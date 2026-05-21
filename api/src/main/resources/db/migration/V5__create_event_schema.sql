-- ============================================================
-- Event domain schema (Plan 009)
-- ============================================================

CREATE TABLE attraction (
    id          UUID PRIMARY KEY DEFAULT uuidv7(),
    name        VARCHAR(255) NOT NULL,
    slug        VARCHAR(255) NOT NULL UNIQUE,
    type        VARCHAR(32) NOT NULL,
    image_url   VARCHAR(255),
    description TEXT
);

CREATE TABLE classification (
    id          UUID PRIMARY KEY DEFAULT uuidv7(),
    name        VARCHAR(255) NOT NULL,
    slug        VARCHAR(255) NOT NULL UNIQUE,
    level       INTEGER NOT NULL,
    is_active   BOOLEAN NOT NULL,
    parent_id   UUID REFERENCES classification(id) ON DELETE SET NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE event (
    id                   UUID PRIMARY KEY DEFAULT uuidv7(),
    organization_id      UUID NOT NULL REFERENCES organization(id) ON DELETE CASCADE,
    venue_id             UUID NOT NULL, -- soft link to venue(id), matches Java Event.venueId type
    title                VARCHAR(255) NOT NULL,
    slug                 VARCHAR(255) NOT NULL UNIQUE,
    description          TEXT,
    status               VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    start_at             TIMESTAMPTZ NOT NULL,
    end_at               TIMESTAMPTZ,
    timezone             VARCHAR(64) NOT NULL,
    sale_start_at        TIMESTAMPTZ,
    sale_end_at          TIMESTAMPTZ,
    restrict_single_seat BOOLEAN NOT NULL DEFAULT false,
    safe_tix_enabled     BOOLEAN NOT NULL DEFAULT false,
    transfer_enabled     BOOLEAN NOT NULL DEFAULT true,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE event_classification (
    event_id          UUID NOT NULL REFERENCES event(id) ON DELETE CASCADE,
    classification_id UUID NOT NULL REFERENCES classification(id) ON DELETE CASCADE,
    PRIMARY KEY (event_id, classification_id)
);

CREATE TABLE event_attraction (
    event_id      UUID NOT NULL REFERENCES event(id) ON DELETE CASCADE,
    attraction_id UUID NOT NULL REFERENCES attraction(id) ON DELETE CASCADE,
    PRIMARY KEY (event_id, attraction_id)
);

CREATE TABLE event_manifest (
    event_id    UUID PRIMARY KEY REFERENCES event(id) ON DELETE CASCADE,
    manifest_id VARCHAR(64) NOT NULL
);
