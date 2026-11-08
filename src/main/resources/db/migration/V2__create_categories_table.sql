CREATE TABLE categories (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

        name VARCHAR(100) NOT NULL,
        description VARCHAR(500),

        is_active BOOLEAN NOT NULL DEFAULT TRUE,

        created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

        CONSTRAINT chk_categories_name
            CHECK (LENGTH(TRIM(name)) > 0)
);

CREATE UNIQUE INDEX idx_categories_name_lower
    ON categories (LOWER(name));
