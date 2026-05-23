package com.photoconnect.auth.security;

import com.photoconnect.auth.config.JwtProperties;
import com.photoconnect.auth.domain.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * The only place in the codebase that touches the JWT private key.
 *
 * <p>Two token types — access (short-lived) and refresh (longer-lived). Both
 * RS256-signed. The {@code typ} claim distinguishes them so we reject
 * refresh-as-access at parse time.</p>
 *
 * <p>Service-to-service tokens are gone — the monolith has no cross-service
 * HTTP calls, so there are no service principals to mint tokens for.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    public static final String CLAIM_ROLE  = "role";
    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_TYP   = "typ";
    public static final String TYP_ACCESS  = "access";
    public static final String TYP_REFRESH = "refresh";

    private final JwtProperties properties;

    private RSAPrivateKey privateKey;
    private RSAPublicKey  publicKey;

    @PostConstruct
    void loadKeys() throws Exception {
        Path privPath = Path.of(properties.privateKeyPath());
        Path pubPath  = Path.of(properties.publicKeyPath());
        log.info("Loading RSA keys: private={}, public={}", privPath, pubPath);
        this.privateKey = PemKeyLoader.loadPrivateKey(privPath);
        this.publicKey  = PemKeyLoader.loadPublicKey(pubPath);
    }

    public IssuedToken generateAccessToken(UUID userId, String email, Role role) {
        String jti = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Instant exp = now.plus(properties.accessTokenTtl());

        String token = Jwts.builder()
                .header().add("typ", "JWT").and()
                .issuer(properties.issuer())
                .audience().add(properties.audience()).and()
                .subject(userId.toString())
                .id(jti)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claim(CLAIM_TYP, TYP_ACCESS)
                .claim(CLAIM_ROLE, role.name())
                .claim(CLAIM_EMAIL, email)
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
        return new IssuedToken(token, jti, exp);
    }

    public IssuedToken generateRefreshToken(UUID userId) {
        String jti = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Instant exp = now.plus(properties.refreshTokenTtl());

        String token = Jwts.builder()
                .header().add("typ", "JWT").and()
                .issuer(properties.issuer())
                .audience().add(properties.audience()).and()
                .subject(userId.toString())
                .id(jti)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claim(CLAIM_TYP, TYP_REFRESH)
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
        return new IssuedToken(token, jti, exp);
    }

    public Jws<Claims> parseAndVerify(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .requireIssuer(properties.issuer())
                .requireAudience(properties.audience())
                .build()
                .parseSignedClaims(token);
    }

    public record IssuedToken(String token, String jti, Instant expiresAt) {}
}
