# PhotoConnect Monolithic

A monolithic Spring Boot port of the [PhotoConnect microservices marketplace](../PhotoConnect). Same product, same UI, same REST contract — collapsed into one deployable.

## Why a monolith version?

The microservice edition was built as a learning vehicle for Spring Cloud patterns (Eureka, Config Server, Gateway, OpenFeign, service-to-service JWTs). That's great for studying microservices, but it's a lot of moving parts for a small project. The monolith edition is the same business logic stripped down to what you'd actually ship for a system this size.

## How it maps to the microservice version

| Microservice version | Monolith equivalent |
| --- | --- |
| `auth-service` (port 8081) | `com.photoconnect.auth` package |
| `photographer-service` (port 8082) | `com.photoconnect.photographer` package |
| `customer-service` (port 8083) | `com.photoconnect.customer` package |
| `reviews-service` (port 8084) | `com.photoconnect.reviews` package |
| `api-gateway` (port 8080) | This Spring Boot app, port 8080 |
| `discovery-service` (Eureka) | *gone* — no service discovery in-process |
| `config-service` (Spring Cloud Config) | *gone* — single `application.properties` |
| OpenFeign clients | Direct bean injection (`@Autowired`) |
| Service-to-service JWTs (`typ=service`) | *gone* — calls are in-process |
| 2 Postgres DBs + 1 MySQL DB | Single `photoconnect_db` on PostgreSQL |
| Gateway header forwarding (`X-User-Id`/`X-User-Role`) | `JwtAuthenticationFilter` reads the JWT directly |
| Per-service `GatewayPrincipal` | Single shared `UserPrincipal` |
| Per-service `GlobalExceptionHandler` | Single `common.exception.GlobalExceptionHandler` |
| Per-service `ErrorResponse` DTO | Single `common.dto.ErrorResponse` |

The package structure preserves the bounded contexts — controllers in one package autowire services from another, but the directory layout still tells you which "service" something used to belong to.

## Architecture

```
                              ┌─────────────────────┐
                              │     React SPA       │
                              │  (Vite, Tailwind)   │
                              │     port 5173       │
                              └──────────┬──────────┘
                                         │  /api/v1/*
                                         ▼
                              ┌────────────────────────────┐
                              │   PhotoConnect monolith    │
                              │  Spring Boot 3.4, port 8080│
                              │                            │
                              │  com.photoconnect.auth     │
                              │  com.photoconnect.photographer │
                              │  com.photoconnect.customer │
                              │  com.photoconnect.reviews  │
                              │  com.photoconnect.common   │
                              └──┬─────────┬──────────┬────┘
                                 │         │          │
                                 ▼         ▼          ▼
                          ┌───────────┐ ┌──────┐  ┌────────┐
                          │PostgreSQL │ │Redis │  │ MinIO  │
                          │photoconnect_db│ JWT  │  │ media  │
                          └───────────┘ └──────┘  └────────┘
```

## Project layout

```
PhotoConnectMonolithic/
├── pom.xml                        ← single Maven module
├── docker-compose.yml             ← Postgres + Redis + MinIO
├── scripts/
│   ├── generate-keys.ps1          ← Windows: mint RSA keypair
│   └── generate-keys.sh           ← Linux/macOS: same
├── keys/                          ← RSA PEMs (gitignored)
├── src/main/java/com/photoconnect/
│   ├── PhotoConnectApplication.java
│   ├── auth/
│   │   ├── config/    (SecurityConfig, JwtProperties, OtpProperties, AuditConfig)
│   │   ├── controller/AuthController.java
│   │   ├── domain/    (User, RefreshToken, Role, AuditableEntity)
│   │   ├── dto/       (Register/Login/Refresh/Otp/Auth/User DTOs)
│   │   ├── exception/ AuthExceptions.java
│   │   ├── mapper/    UserMapper.java
│   │   ├── repository/(UserRepository, RefreshTokenRepository)
│   │   ├── security/  (JwtService, JwtAuthenticationFilter,
│   │   │              UserPrincipal, PemKeyLoader, CorrelationIdServletFilter)
│   │   └── service/   (AuthService, OtpService, TokenBlacklistService, OtpDelivery)
│   ├── photographer/
│   │   ├── controller/(Photographer/Portfolio/Availability)Controller
│   │   ├── domain/    (PhotographerProfile, PortfolioItem, AvailabilitySlot, MediaType)
│   │   ├── dto/       (Create/Update/Response/Feed/Availability DTOs)
│   │   ├── exception/ PhotographerExceptions.java
│   │   ├── mapper/    PhotographerMapper.java
│   │   ├── repository/(PhotographerProfile, PortfolioItem, AvailabilitySlot, FeedRow)
│   │   ├── service/   (Photographer/Portfolio/Availability)Service
│   │   └── storage/   (StorageConfig, StorageProperties)
│   ├── customer/
│   │   ├── controller/(Customer/Inquiry/Favorite)Controller
│   │   ├── domain/    (Customer, Inquiry, Favorite, ContactMethod, InquiryStatus)
│   │   ├── dto/       (Create/Update/Response/Favorite DTOs)
│   │   ├── exception/ CustomerExceptions.java
│   │   ├── mapper/    (Customer/Inquiry)Mapper
│   │   ├── repository/(Customer/Inquiry/Favorite)Repository
│   │   └── service/   (Customer/Inquiry/Favorite)Service
│   ├── reviews/
│   │   ├── controller/ReviewController.java
│   │   ├── domain/    Review.java
│   │   ├── dto/       (Create/Response/Summary DTOs)
│   │   ├── exception/ ReviewExceptions.java
│   │   ├── mapper/    ReviewMapper.java
│   │   ├── repository/(ReviewRepository, ReviewSummary)
│   │   └── service/   ReviewService.java
│   └── common/
│       ├── dto/       ErrorResponse.java
│       └── exception/ (DomainException, GlobalExceptionHandler)
├── src/main/resources/
│   ├── application.properties
│   └── db/migration/
│       ├── V1__create_users_and_refresh_tokens.sql
│       ├── V2__create_photographer_tables.sql
│       ├── V3__create_customer_tables.sql
│       └── V4__create_reviews_table.sql
└── frontend/                       ← React 19 + Vite + TS + Tailwind 4
    ├── package.json
    ├── vite.config.ts              ← /api proxy to :8080
    └── src/                        ← copied from microservices version (no API changes)
```

## Prerequisites

| Tool | Version | Why |
| --- | --- | --- |
| JDK | 21 (LTS) | Spring Boot 3.4 requires 17+; this project uses 21 |
| Maven | 3.9+ | `mvn -v` |
| Docker Desktop | latest | `docker compose` v2 |
| Node | 20+ | frontend |
| openssl | any modern build | mint RSA keypair |

## Quick start

```powershell
# Windows / PowerShell
cd C:\Users\lakha\OneDrive\Documents\PhotoConnectMonolithic

# 1. Generate the RSA keypair used to sign JWTs
.\scripts\generate-keys.ps1

# 2. Bring up Postgres, Redis, MinIO
docker compose up -d
docker compose ps

# 3. Build and run the backend
mvn spring-boot:run

# 4. In another shell, start the frontend
cd frontend
npm install
npm run dev
```

```bash
# Linux / macOS
cd PhotoConnectMonolithic

./scripts/generate-keys.sh
docker compose up -d
./mvnw spring-boot:run        # or: mvn spring-boot:run

cd frontend && npm install && npm run dev
```

Open <http://localhost:5173> for the SPA, <http://localhost:8080/swagger-ui.html> for the API docs, <http://localhost:9001> for the MinIO console (`minioadmin`/`minioadmin`).

## API surface

Identical to the microservices version — same paths, same JSON shapes, same error envelope.

| Path | Method | Auth | Description |
| --- | --- | --- | --- |
| `/api/v1/auth/register` | POST | public | Email + password signup |
| `/api/v1/auth/login` | POST | public | Email + password login |
| `/api/v1/auth/refresh` | POST | public (refresh token in body) | Rotate token pair |
| `/api/v1/auth/logout` | POST | JWT | Revoke session |
| `/api/v1/auth/otp/send` | POST | public | Send OTP to phone |
| `/api/v1/auth/otp/verify` | POST | public | Verify OTP + signup/login |
| `/api/v1/auth/me` | GET | JWT | Current user |
| `/api/v1/photographers` | GET | public | Browse available photographers |
| `/api/v1/photographers/feed` | GET | public | Mixed-media marketplace feed |
| `/api/v1/photographers/me` | GET / POST / PUT / DELETE | PHOTOGRAPHER | Own profile CRUD |
| `/api/v1/photographers/{id}` | GET | public | Specific profile |
| `/api/v1/photographers/{id}/portfolio` | GET | public | Photographer's media gallery |
| `/api/v1/photographers/{id}/availability` | GET | public | Calendar |
| `/api/v1/photographers/me/portfolio` | POST / GET | PHOTOGRAPHER | Upload + list own media |
| `/api/v1/photographers/me/portfolio/{itemId}` | DELETE | PHOTOGRAPHER | Remove media item |
| `/api/v1/photographers/me/availability` | GET / POST / DELETE | PHOTOGRAPHER | Manage own calendar |
| `/api/v1/customers/me` | GET / POST / PUT / DELETE | CUSTOMER | Own profile CRUD |
| `/api/v1/inquiries` | POST | CUSTOMER | Create inquiry |
| `/api/v1/inquiries/mine` | GET | CUSTOMER | Customer outbox |
| `/api/v1/inquiries/received` | GET | PHOTOGRAPHER | Photographer inbox |
| `/api/v1/inquiries/{id}` | GET | participant | Single inquiry |
| `/api/v1/inquiries/{id}/status` | PATCH | participant | Update status |
| `/api/v1/favorites` | GET | CUSTOMER | List bookmarks (enriched) |
| `/api/v1/favorites/{portfolioItemId}` | PUT / DELETE | CUSTOMER | Save / unsave |
| `/api/v1/favorites/{portfolioItemId}/status` | GET | CUSTOMER | Heart-button state |
| `/api/v1/reviews` | POST | CUSTOMER | Create review (needs COMPLETED inquiry) |
| `/api/v1/reviews/mine` | GET | CUSTOMER | Reviews I authored |
| `/api/v1/reviews/photographer/{id}` | GET | public | Reviews for a photographer |
| `/api/v1/reviews/summary/{id}` | GET | public | Aggregate avg + count |

## Notable design choices

### Cross-module calls are bean injections, not HTTP

The microservices version had four notable cross-service Feign calls:

- `customer-service` → `photographer-service` to verify a photographer exists when creating an inquiry.
- `customer-service` → `photographer-service` to check availability before persisting.
- `customer-service` → `photographer-service` to enrich a saved favorite with its media metadata.
- `reviews-service` → `customer-service` to find a completed booking, plus → `photographer-service` to validate the target photographer.

In the monolith those four turn into autowired beans:

- `InquiryService` injects `PhotographerService` and `AvailabilityService`.
- `FavoriteService` injects `PortfolioService`.
- `ReviewService` injects `PhotographerService` and `InquiryService`.

The semantic boundary is still there — `com.photoconnect.customer.*` only knows about `com.photoconnect.photographer.service.*` interfaces — but the wire format (HTTP + JSON) is gone, and so is the service-to-service JWT plumbing.

### One JWT principal, one filter chain

The microservices had two principal shapes — `UserPrincipal` in auth-service (from the actual JWT) and `GatewayPrincipal` everywhere else (from `X-User-Id` / `X-User-Role` headers stamped by the gateway). The monolith only ever sees the real JWT, so there's a single `com.photoconnect.auth.security.UserPrincipal` used by every controller across every module.

### Single PostgreSQL database

The microservices version used Postgres for auth/photographer/reviews and MySQL for customer-service, on purpose, to demonstrate per-service storage choices. The monolith collapses them all into `photoconnect_db` on Postgres. MySQL-specific column types (`VARCHAR(36)` UUIDs, `DATETIME(6)` timestamps) become Postgres-native `UUID` and `TIMESTAMPTZ`, and the four Flyway migrations run in order against the single schema.

### Service-to-service JWTs are gone

The microservices version had auth-service mint short-lived `typ=service` JWTs that customer-service / reviews-service used to call each other's `/internal/v1/**` endpoints. The monolith doesn't have internal endpoints — the equivalent logic is just public method calls on Spring beans, secured by Java's package boundaries and Spring's transactional context.

## Tear-down

```powershell
docker compose down          # stop, keep data
docker compose down -v       # stop + wipe Postgres/Redis/MinIO volumes
```

## What's NOT ported

- `discovery-service` / `config-service` / `api-gateway` (not needed)
- The MSG91 SMS provider integration (only dev-mode OTP is included; add the SMS adapter the same way the microservices version did if you need real delivery)
- Distributed tracing (Zipkin) — re-introduce if/when you split anything back out
- Per-service Dockerfiles, Jenkinsfile (the original repo's CI pipeline is built around the microservices topology)
