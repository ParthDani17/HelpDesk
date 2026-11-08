
CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                       name VARCHAR(100) NOT NULL,
                       email VARCHAR(255) NOT NULL,
                       password VARCHAR(255) NOT NULL,

                       role VARCHAR(20) NOT NULL DEFAULT 'USER',
                       is_active BOOLEAN NOT NULL DEFAULT TRUE,

                       created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT chk_users_role
                           CHECK (role IN ('USER', 'AGENT', 'ADMIN')),

                       CONSTRAINT chk_users_name
                           CHECK (LENGTH(TRIM(name)) > 0),

                       CONSTRAINT chk_users_email
                           CHECK (LENGTH(TRIM(email)) > 0)
);

CREATE UNIQUE INDEX idx_users_email_lower
    ON users (LOWER(email));
