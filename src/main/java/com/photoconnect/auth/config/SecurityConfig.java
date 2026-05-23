package com.photoconnect.auth.config;

import com.photoconnect.auth.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Single Spring Security configuration for the entire monolith.
 *
 * <p>Covers public auth endpoints, the public marketplace browse, the public
 * read endpoints for reviews, and protected endpoints across photographer,
 * customer, and reviews modules. Method-level rules ({@code @PreAuthorize})
 * on individual controllers carry the role checks.</p>
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ── Public auth endpoints ────────────────────────────
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/auth/register",
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh",
                                "/api/v1/auth/otp/send",
                                "/api/v1/auth/otp/verify").permitAll()

                        // ── Public marketplace reads ─────────────────────────
                        // Anonymous users can browse photographers and view feed
                        // and individual portfolios. Method-level @PreAuthorize
                        // on the controllers blocks logged-in photographers.
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/photographers",
                                "/api/v1/photographers/feed",
                                "/api/v1/photographers/*",
                                "/api/v1/photographers/*/portfolio",
                                "/api/v1/photographers/*/availability").permitAll()

                        // ── Public reviews reads ─────────────────────────────
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/reviews/photographer/**",
                                "/api/v1/reviews/summary/**").permitAll()

                        // ── Observability + docs ─────────────────────────────
                        .requestMatchers(
                                "/actuator/health/**",
                                "/actuator/info",
                                "/actuator/prometheus",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html").permitAll()

                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * CORS for the Vite dev server. In production the SPA is served from the
     * same origin as the API and CORS isn't strictly needed.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:8080"));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setExposedHeaders(List.of("X-Correlation-Id"));
        cfg.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
