package com.photoconnect;

import com.photoconnect.auth.config.JwtProperties;
import com.photoconnect.auth.config.OtpProperties;
import com.photoconnect.photographer.storage.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * PhotoConnect monolith entry point.
 *
 * <p>All four business contexts (auth, photographer, customer, reviews) live
 * in this single Spring Boot application. They share one Postgres database,
 * one Spring Security filter chain, one HTTP server, and one process.</p>
 *
 * <p>Cross-context calls happen as in-process bean injection rather than
 * Feign over HTTP. The packages (com.photoconnect.{auth, photographer,
 * customer, reviews}) act as logical module boundaries — controllers in
 * one package can autowire services from another, but the directory layout
 * keeps the bounded contexts visible.</p>
 */
@SpringBootApplication
@EnableConfigurationProperties({
        JwtProperties.class,
        OtpProperties.class,
        StorageProperties.class
})
public class PhotoConnectApplication {

    public static void main(String[] args) {
        SpringApplication.run(PhotoConnectApplication.class, args);
    }
}
