CREATE TABLE hold_group (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    event_id UUID NOT NULL REFERENCES event(id),
    name VARCHAR(255) NOT NULL,
    color VARCHAR(7) DEFAULT '#000000',
    type VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE TABLE kill_group (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    event_id UUID NOT NULL REFERENCES event(id),
    name VARCHAR(255) NOT NULL,
    color VARCHAR(7) DEFAULT '#000000',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE TABLE hold_group_section (
    hold_group_id UUID NOT NULL REFERENCES hold_group(id),
    section_id VARCHAR(255) NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    PRIMARY KEY (hold_group_id, section_id)
);


ALTER TABLE inventory_seat ADD COLUMN hold_group_id UUID REFERENCES hold_group(id);
ALTER TABLE inventory_seat ADD COLUMN kill_group_id UUID REFERENCES kill_group(id);

ALTER TABLE inventory_ga ADD COLUMN killed INT NOT NULL DEFAULT 0;
