CREATE TABLE country (
    code VARCHAR(8) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255),
    currency VARCHAR(16),
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE city (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255),
    latitude VARCHAR(64),
    longitude VARCHAR(64),
    timezone VARCHAR(64),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    country_code VARCHAR(8),
    CONSTRAINT fk_city_country FOREIGN KEY (country_code) REFERENCES country (code)
);

ALTER TABLE account ADD CONSTRAINT fk_account_city FOREIGN KEY (city_id) REFERENCES city (id);
ALTER TABLE account ADD CONSTRAINT fk_account_country FOREIGN KEY (country_code) REFERENCES country (code);
