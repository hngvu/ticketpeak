CREATE TABLE offer_sale_window (
    id UUID NOT NULL DEFAULT uuidv7(),
    offer_id UUID NOT NULL,
    type VARCHAR(20) NOT NULL,
    start_at TIMESTAMP NOT NULL,
    end_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_offer_sale_window PRIMARY KEY (id),
    CONSTRAINT fk_offer_sale_window_offer FOREIGN KEY (offer_id) REFERENCES offer(id) ON DELETE CASCADE,
    CONSTRAINT uq_offer_sale_window_type UNIQUE (offer_id, type)
);
