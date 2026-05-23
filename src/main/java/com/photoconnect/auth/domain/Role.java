package com.photoconnect.auth.domain;

/**
 * The two user types in PhotoConnect.
 *
 * <p>Single source of truth — both photographer and customer modules import
 * this same enum (no more copy-pasting per-service Role enums). Stored in the
 * DB as the enum name and surfaced as the {@code ROLE_*} Spring Security
 * authority.</p>
 */
public enum Role {
    PHOTOGRAPHER,
    CUSTOMER
}
