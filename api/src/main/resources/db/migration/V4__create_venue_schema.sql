-- ============================================================
-- Venue domain schema (Plan 008)
-- All primary keys are VARCHAR to support Ticketmaster IDs
-- ============================================================

CREATE TABLE venue (
    id          VARCHAR(64) PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    address     TEXT         NOT NULL,
    city        VARCHAR(128) NOT NULL,
    country     VARCHAR(128) NOT NULL,
    latitude    NUMERIC(9, 6),
    longitude   NUMERIC(9, 6),
    phone       VARCHAR(64),
    email       VARCHAR(255),
    website     VARCHAR(255),
    description TEXT,
    thumbnail_url VARCHAR(255),
    images      JSONB,
    status      VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE manifest (
    id             VARCHAR(64) PRIMARY KEY,
    venue_id       VARCHAR(64) NOT NULL REFERENCES venue (id) ON DELETE CASCADE,
    description    VARCHAR(255) NOT NULL,
    total_capacity INTEGER      NOT NULL DEFAULT 0,
    status         VARCHAR(32)  NOT NULL DEFAULT 'DRAFT',
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Lookup table: Level (per manifest)
CREATE TABLE venue_level (
    id          VARCHAR(64)  NOT NULL,
    manifest_id VARCHAR(64)  NOT NULL REFERENCES manifest (id) ON DELETE CASCADE,
    description VARCHAR(255) NOT NULL,
    PRIMARY KEY (id, manifest_id)
);

-- Lookup table: Section (per manifest)
CREATE TABLE section (
    id          VARCHAR(64)  NOT NULL,
    manifest_id VARCHAR(64)  NOT NULL REFERENCES manifest (id) ON DELETE CASCADE,
    description VARCHAR(255) NOT NULL,
    PRIMARY KEY (id, manifest_id)
);

-- Lookup table: PriceLevel (per manifest)
CREATE TABLE price_level (
    id          VARCHAR(64)  NOT NULL,
    manifest_id VARCHAR(64)  NOT NULL REFERENCES manifest (id) ON DELETE CASCADE,
    description VARCHAR(255),
    PRIMARY KEY (id, manifest_id)
);

-- General Admission area
CREATE TABLE ga_area (
    id             VARCHAR(64) PRIMARY KEY,
    manifest_id    VARCHAR(64) NOT NULL REFERENCES manifest (id) ON DELETE CASCADE,
    level_id       VARCHAR(64) NOT NULL,
    section_id     VARCHAR(64) NOT NULL,
    price_level_id VARCHAR(64) NOT NULL,
    capacity       INTEGER     NOT NULL DEFAULT 0
);

-- Reserved Seating area
CREATE TABLE rs_area (
    id             VARCHAR(64) PRIMARY KEY,
    manifest_id    VARCHAR(64) NOT NULL REFERENCES manifest (id) ON DELETE CASCADE,
    level_id       VARCHAR(64) NOT NULL,
    section_id     VARCHAR(64) NOT NULL,
    price_level_id VARCHAR(64) NOT NULL
);

CREATE TABLE seat_row (
    id         VARCHAR(64) PRIMARY KEY,
    rs_area_id VARCHAR(64) NOT NULL REFERENCES rs_area (id) ON DELETE CASCADE,
    name       VARCHAR(64) NOT NULL,
    position_y INTEGER
);

CREATE TABLE seat (
    id             VARCHAR(64) PRIMARY KEY,
    row_id         VARCHAR(64) NOT NULL REFERENCES seat_row (id) ON DELETE CASCADE,
    name           VARCHAR(64) NOT NULL,
    position_x     INTEGER,
    status         VARCHAR(32) NOT NULL DEFAULT 'AVAILABLE',
    accessibility  BOOLEAN,
    obstructed_view BOOLEAN,
    aisle          BOOLEAN,
    CONSTRAINT uq_seat_row_name UNIQUE (row_id, name)
);
