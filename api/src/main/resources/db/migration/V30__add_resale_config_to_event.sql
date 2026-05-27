ALTER TABLE event
    ADD COLUMN resale_enabled                BOOLEAN      NOT NULL DEFAULT FALSE,
    ADD COLUMN resale_price_cap_percent      NUMERIC(5,2),
    ADD COLUMN max_resale_listings_per_user  INT          NOT NULL DEFAULT 1;
