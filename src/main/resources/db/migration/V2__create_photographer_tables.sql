-- =============================================================================
-- V2 — Photographer module: profiles, specialties, portfolio_items, availability
-- =============================================================================
-- Combined from photographer-service's V1, V2, V3.
-- =============================================================================

CREATE TABLE photographer_profiles (
    id                   UUID          NOT NULL DEFAULT gen_random_uuid(),
    user_id              UUID          NOT NULL,
    display_name         VARCHAR(100)  NOT NULL,
    bio                  TEXT,
    location             VARCHAR(200)  NOT NULL,
    years_of_experience  INTEGER       NOT NULL DEFAULT 0,
    price_per_hour       NUMERIC(10,2) NOT NULL DEFAULT 0.00,
    available            BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at           TIMESTAMPTZ   NOT NULL,
    updated_at           TIMESTAMPTZ   NOT NULL,
    version              BIGINT        NOT NULL DEFAULT 0,

    CONSTRAINT pk_photographer_profiles PRIMARY KEY (id),
    CONSTRAINT uq_photographer_profiles_user_id UNIQUE (user_id),
    CONSTRAINT fk_photographer_profiles_user
        FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT chk_years_of_experience  CHECK (years_of_experience >= 0),
    CONSTRAINT chk_price_per_hour       CHECK (price_per_hour >= 0)
);

CREATE TABLE photographer_specialties (
    photographer_id  UUID          NOT NULL,
    specialty        VARCHAR(100)  NOT NULL,

    CONSTRAINT fk_specialties_profile
        FOREIGN KEY (photographer_id)
        REFERENCES photographer_profiles (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_photographer_profiles_available  ON photographer_profiles (available);
CREATE INDEX idx_photographer_specialties_profile ON photographer_specialties (photographer_id);

CREATE TABLE portfolio_items (
    id                       UUID         NOT NULL DEFAULT gen_random_uuid(),
    photographer_profile_id  UUID         NOT NULL,
    media_type               VARCHAR(20)  NOT NULL,
    category                 VARCHAR(100) NOT NULL,
    mime_type                VARCHAR(100) NOT NULL,
    size_bytes               BIGINT       NOT NULL,
    storage_key              VARCHAR(500) NOT NULL,
    public_url               VARCHAR(1000) NOT NULL,
    display_order            INTEGER      NOT NULL DEFAULT 0,
    uploaded_at              TIMESTAMPTZ  NOT NULL,

    CONSTRAINT pk_portfolio_items PRIMARY KEY (id),
    CONSTRAINT fk_portfolio_profile
        FOREIGN KEY (photographer_profile_id)
        REFERENCES photographer_profiles (id)
        ON DELETE CASCADE,
    CONSTRAINT chk_portfolio_media_type CHECK (media_type IN ('IMAGE', 'VIDEO', 'REEL')),
    CONSTRAINT chk_portfolio_size       CHECK (size_bytes > 0),
    CONSTRAINT uq_portfolio_storage_key UNIQUE (storage_key)
);

CREATE INDEX idx_portfolio_profile_lookup
    ON portfolio_items (photographer_profile_id, media_type, category, display_order);

CREATE TABLE availability_slots (
    id                       UUID         NOT NULL DEFAULT gen_random_uuid(),
    photographer_profile_id  UUID         NOT NULL,
    available_date           DATE         NOT NULL,
    note                     VARCHAR(200),
    created_at               TIMESTAMPTZ  NOT NULL,

    CONSTRAINT pk_availability_slots PRIMARY KEY (id),
    CONSTRAINT fk_availability_profile
        FOREIGN KEY (photographer_profile_id)
        REFERENCES photographer_profiles (id)
        ON DELETE CASCADE,
    CONSTRAINT uq_availability_profile_date
        UNIQUE (photographer_profile_id, available_date)
);

CREATE INDEX idx_availability_profile_date
    ON availability_slots (photographer_profile_id, available_date ASC);
