-- =============================================================================
-- V1 — Auth module: users + refresh_tokens
-- =============================================================================
-- Combined from auth-service's V1, V2, and V3 migrations. Single schema, no
-- engine-specific syntax (PostgreSQL everywhere now).
-- =============================================================================

CREATE TABLE users (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255),
    phone           VARCHAR(20),
    password_hash   VARCHAR(255),
    role            VARCHAR(20)  NOT NULL CHECK (role IN ('PHOTOGRAPHER', 'CUSTOMER')),
    enabled         BOOLEAN      NOT NULL DEFAULT TRUE,
    deleted_at      TIMESTAMP WITH TIME ZONE,

    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by      VARCHAR(255),
    updated_by      VARCHAR(255),

    version         BIGINT       NOT NULL DEFAULT 0,

    CONSTRAINT chk_users_phone_e164
        CHECK (phone IS NULL OR phone ~ '^\+[1-9][0-9]{7,14}$'),

    CONSTRAINT chk_users_has_identifier
        CHECK (email IS NOT NULL OR phone IS NOT NULL)
);

CREATE UNIQUE INDEX uk_users_email_active
    ON users (LOWER(email))
    WHERE deleted_at IS NULL AND email IS NOT NULL;

CREATE UNIQUE INDEX uk_users_phone_active
    ON users (phone)
    WHERE deleted_at IS NULL AND phone IS NOT NULL;

CREATE INDEX idx_users_role       ON users (role);
CREATE INDEX idx_users_deleted_at ON users (deleted_at);

CREATE TABLE refresh_tokens (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID         NOT NULL REFERENCES users(id),
    token_id     VARCHAR(64)  NOT NULL UNIQUE,
    expires_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_refresh_tokens_user_id    ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens (expires_at);
