CREATE TABLE ticket_type (
    id          UUID        NOT NULL DEFAULT uuidv7() PRIMARY KEY,
    event_id    UUID        NOT NULL,
    name        VARCHAR(255) NOT NULL,
    slug        VARCHAR(255) NOT NULL,
    description TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_ticket_type_event_slug UNIQUE (event_id, slug)
);

CREATE INDEX idx_ticket_type_event_id ON ticket_type(event_id);
