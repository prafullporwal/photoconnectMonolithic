# PhotoConnect Monolithic Architecture

```
                              ┌───────────────────────┐
                              │   React 19 SPA        │
                              │   Vite + Tailwind 4   │
                              │   :5173               │
                              └───────────┬───────────┘
                                          │ /api/v1/**  (JWT in Bearer)
                                          ▼
              ┌────────────────────────────────────────────────────────────────┐
              │            PhotoConnect monolith  (Spring Boot 3.4)  :8080     │
              │  ────────────────────────────────────────────────────────────  │
              │   JwtAuthenticationFilter  →  UserPrincipal  →  controllers    │
              │  ────────────────────────────────────────────────────────────  │
              │                                                                │
              │   com.photoconnect.auth          com.photoconnect.photographer │
              │   ┌─────────────────────┐        ┌─────────────────────────┐   │
              │   │ AuthController      │        │ PhotographerController  │   │
              │   │ AuthService         │        │ PortfolioController     │   │
              │   │ JwtService          │  ◄──── │ AvailabilityController  │   │
              │   │ OtpService          │        │ PhotographerService     │   │
              │   │ TokenBlacklistSvc   │        │ PortfolioService        │   │
              │   │ UserRepository      │        │ AvailabilityService     │   │
              │   └─────────────────────┘        └──────────▲──▲───────────┘   │
              │             ▲                               │  │               │
              │             │ shared UserPrincipal          │  │ @Autowired    │
              │             │ across all controllers        │  │               │
              │             │                               │  │               │
              │   com.photoconnect.customer                 │  │               │
              │   ┌─────────────────────┐                   │  │               │
              │   │ CustomerController  │                   │  │               │
              │   │ InquiryController   │  ── @Autowired ───┘  │               │
              │   │ FavoriteController  │  ── @Autowired ──────┘               │
              │   │ InquiryService ─────┼──┐                                   │
              │   │ FavoriteService     │  │                                   │
              │   │ CustomerService     │  │                                   │
              │   └─────────────────────┘  │                                   │
              │                            │ @Autowired                        │
              │   com.photoconnect.reviews │                                   │
              │   ┌─────────────────────┐  │                                   │
              │   │ ReviewController    │  │                                   │
              │   │ ReviewService ──────┼──┘ (needs Photographer + Inquiry)    │
              │   └─────────────────────┘                                      │
              │                                                                │
              │   com.photoconnect.common                                      │
              │   ┌─────────────────────┐                                      │
              │   │ GlobalExceptionHnd  │                                      │
              │   │ ErrorResponse DTO   │                                      │
              │   └─────────────────────┘                                      │
              └────────────────┬───────────────────┬──────────────┬────────────┘
                               │                   │              │
                               ▼                   ▼              ▼
                       ┌───────────────┐    ┌───────────┐   ┌────────────┐
                       │ PostgreSQL    │    │  Redis    │   │   MinIO    │
                       │ photoconnect_db│    │ blacklist │   │   media    │
                       │ (V1..V4 .sql) │    │           │   │            │
                       └───────────────┘    └───────────┘   └────────────┘
```

## Key flows

- **User login** — SPA → app → `AuthController` → `AuthService` → mints RS256 JWT → SPA stores it.
- **Request auth** — `JwtAuthenticationFilter` parses the JWT on each request, builds `UserPrincipal`, controllers read it via `@AuthenticationPrincipal`. No header stamping, no second principal type.
- **Cross-context calls** — plain Spring bean injection. `InquiryService` calls `PhotographerService.findById(...)` as a Java method. Same JVM, same `@Transactional` context, no wire format.
  - `InquiryService` injects `PhotographerService` and `AvailabilityService`.
  - `FavoriteService` injects `PortfolioService`.
  - `ReviewService` injects `PhotographerService` and `InquiryService`.

## What disappears vs. the microservices version

| Microservices                                          | Monolith                                            |
| ------------------------------------------------------ | --------------------------------------------------- |
| 6 deployables + frontend                               | 1 deployable + frontend                             |
| Eureka discovery                                       | gone — direct bean refs                             |
| Spring Cloud Config + `config-repo/`                   | gone — single `application.properties`              |
| API gateway (JWT verify + header stamp)                | gone — `JwtAuthenticationFilter` inside the app     |
| Feign clients + `/internal/v1/**` endpoints            | gone — `@Autowired` cross-package calls             |
| Service-to-service `typ=service` JWTs                  | gone — package boundaries replace the HTTP boundary |
| `GatewayPrincipal` (from headers) + `UserPrincipal`    | single `UserPrincipal` everywhere                   |
| 3 Postgres DBs + 1 MySQL DB                            | single `photoconnect_db` on Postgres                |
| Per-service `GlobalExceptionHandler` + `ErrorResponse` | one `common.exception.GlobalExceptionHandler`       |
| Zipkin distributed tracing                             | unnecessary — single-process call stack             |

## Counterpart

The full microservices edition lives at [`../PhotoConnect`](../PhotoConnect). Same product, same UI, same REST contract — but with gateway / discovery / config / Feign / service-JWT machinery added back in. See [`../PhotoConnect/ARCHITECTURE.md`](../PhotoConnect/ARCHITECTURE.md) for that diagram.
