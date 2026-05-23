-- =============================================================================
-- V4 — Reviews module
-- =============================================================================
-- Ported from reviews-service's V1. Same shape, same constraints; the
-- "must have a completed booking" check still lives in the service layer
-- (now a direct call into InquiryService instead of a Feign hop).
-- =============================================================================

CREATE TABLE reviews (
    id                        UUID         NOT NULL DEFAULT gen_random_uuid(),
    customer_id               UUID         NOT NULL,
    photographer_profile_id   UUID         NOT NULL,
    photographer_user_id      UUID         NOT NULL,
    inquiry_id                UUID         NOT NULL,
    rating                    SMALLINT     NOT NULL,
    body                      TEXT,
    created_at                TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at                TIMESTAMPTZ  NOT NULL DEFAULT now(),
    version                   BIGINT       NOT NULL DEFAULT 0,

    CONSTRAINT pk_reviews PRIMARY KEY (id),
    CONSTRAINT chk_reviews_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT uq_reviews_customer_photographer UNIQUE (customer_id, photographer_profile_id)
);

CREATE INDEX idx_reviews_photographer_profile_created
    ON reviews (photographer_profile_id, created_at DESC);

CREATE INDEX idx_reviews_customer_created
    ON reviews (customer_id, created_at DESC);
