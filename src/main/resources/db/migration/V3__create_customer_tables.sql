-- =============================================================================
-- V3 — Customer module: customers, inquiries, favorites
-- =============================================================================
-- Ported from customer-service's V1..V4 migrations. MySQL-specific quirks
-- (VARCHAR(36) for UUID, DATETIME(6) for timestamps, ENGINE=InnoDB) drop out
-- now that we're on PostgreSQL with native UUID/TIMESTAMPTZ.
-- =============================================================================

CREATE TABLE customers (
    id                        UUID          NOT NULL DEFAULT gen_random_uuid(),
    user_id                   UUID          NOT NULL,
    display_name              VARCHAR(100)  NOT NULL,
    location                  VARCHAR(200),
    phone_number              VARCHAR(30),
    preferred_contact_method  VARCHAR(20)   NOT NULL DEFAULT 'EMAIL',
    created_at                TIMESTAMPTZ   NOT NULL,
    updated_at                TIMESTAMPTZ   NOT NULL,
    version                   BIGINT        NOT NULL DEFAULT 0,

    CONSTRAINT pk_customers PRIMARY KEY (id),
    CONSTRAINT uq_customers_user_id UNIQUE (user_id),
    CONSTRAINT fk_customers_user
        FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT chk_contact_method
        CHECK (preferred_contact_method IN ('EMAIL', 'PHONE', 'PLATFORM'))
);

CREATE TABLE inquiries (
    id                        UUID          NOT NULL DEFAULT gen_random_uuid(),
    customer_id               UUID          NOT NULL,
    photographer_profile_id   UUID          NOT NULL,
    photographer_user_id      UUID          NOT NULL,
    event_date                DATE          NOT NULL,
    event_type                VARCHAR(50)   NOT NULL,
    location                  VARCHAR(200),
    budget                    NUMERIC(10,2),
    message                   TEXT          NOT NULL,
    status                    VARCHAR(20)   NOT NULL DEFAULT 'NEW',
    created_at                TIMESTAMPTZ   NOT NULL,
    updated_at                TIMESTAMPTZ   NOT NULL,
    version                   BIGINT        NOT NULL DEFAULT 0,

    CONSTRAINT pk_inquiries PRIMARY KEY (id),
    CONSTRAINT chk_inquiry_status
        CHECK (status IN ('NEW', 'READ', 'RESPONDED', 'CLOSED', 'COMPLETED'))
);

CREATE INDEX idx_inquiries_customer
    ON inquiries (customer_id, created_at DESC);

CREATE INDEX idx_inquiries_photographer_user
    ON inquiries (photographer_user_id, created_at DESC);

CREATE INDEX idx_inquiries_status
    ON inquiries (status);

CREATE INDEX idx_inquiries_engagement
    ON inquiries (customer_id, photographer_profile_id, status);

CREATE TABLE favorites (
    id                        UUID          NOT NULL DEFAULT gen_random_uuid(),
    customer_id               UUID          NOT NULL,
    portfolio_item_id         UUID          NOT NULL,
    created_at                TIMESTAMPTZ   NOT NULL,

    CONSTRAINT pk_favorites PRIMARY KEY (id),
    CONSTRAINT uq_favorites_customer_item
        UNIQUE (customer_id, portfolio_item_id)
);

CREATE INDEX idx_favorites_customer_created
    ON favorites (customer_id, created_at DESC);
