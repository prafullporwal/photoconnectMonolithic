package com.photoconnect.auth.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * A PhotoConnect user — photographer OR customer.
 *
 * <p>Email is unique among non-deleted users (partial unique index in Flyway).
 * Phone is unique among non-deleted users when present. Either email OR phone
 * must be set; OTP-only accounts have no password.</p>
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID", updatable = false, nullable = false)
    private UUID id;

    @Column(length = 255)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Version
    private Long version;
}
