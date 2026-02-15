# Project Guidelines — Lille-Chat

## Architecture

Minimalistic instant-messaging application with two **independent** Spring Boot 4.x (Java 25) modules:

- **`backend/`** — REST API + JPA persistence (`dev.rabauer.lille.chat.backend`)
- **`frontend/`** — Vaadin 25 web UI (`dev.rabauer.lille_chat.frontend`)

No parent POM — each module has its own Maven Wrapper and build lifecycle. The frontend communicates with the backend via HTTP. Real-time message delivery uses SSE (`SseEmitter`). Both default to port 8080; in Docker Compose the backend maps to `8080`, frontend to `8081`.

### Backend layering

```
dto/          Java records for API data transfer (UserDto, ChatMessageDto, etc.)
service/      Interfaces defining the business contract (UserService, FriendshipService, etc.)
              ↳ Implementations, entities, repositories are NOT yet created
```

Service interfaces accept/return DTOs, never entities. `UserService.getOrCreateUser(JwtAuthenticationToken)` auto-provisions users from Keycloak JWT claims (`sub`, `preferred_username`, `email`).

## Code Style

- Java 25, Spring Boot 4.0.2, Vaadin 25.0.5
- DTOs are Java `record` types in `dto/` package — see [UserDto.java](backend/src/main/java/dev/rabauer/lille/chat/backend/dto/UserDto.java)
- Service contracts are `interface` types in `service/` — see [UserService.java](backend/src/main/java/dev/rabauer/lille/chat/backend/service/UserService.java)
- Package-private test classes (no `public` modifier) — JUnit 5 style
- Package naming diverges due to hyphen restriction: backend uses `lille.chat` (two segments), frontend uses `lille_chat` (underscore)
- No formatter/linter configured — follow standard Java conventions

## Build and Test

Commands must run from within each module directory. Use `mvnw.cmd` on Windows:

```bash
# Backend
cd backend
mvnw.cmd clean install                # Build + test
mvnw.cmd spring-boot:run              # Run app
mvnw.cmd spring-boot:test-run         # Run with Testcontainers (dev services)
mvnw.cmd test                         # Tests only
mvnw.cmd test-compile "-Djava.version=21"  # Local compile check (JDK 21 workaround)

# Frontend
cd frontend
mvnw.cmd clean install                # Build + test (includes Vaadin frontend build)
mvnw.cmd spring-boot:run              # Run app
mvnw.cmd test                         # Tests only

# Docker (from repo root)
docker compose up --build             # All services: postgres, keycloak, backend, frontend
```

> **Note:** Project targets Java 25 but local JDK may be 21. Use `-Djava.version=21` to compile locally. Docker builds use `eclipse-temurin:25-jdk`.

## Project Conventions

- **Testcontainers pattern (backend):** Uses Spring Boot 4.x test-run pattern:
  - [TestcontainersConfiguration.java](backend/src/test/java/dev/rabauer/lille/chat/backend/TestcontainersConfiguration.java) — `public @TestConfiguration` with `@Bean @ServiceConnection PostgreSQLContainer` (public because service tests live in a sub-package)
  - [TestBackendApplication.java](backend/src/test/java/dev/rabauer/lille/chat/backend/TestBackendApplication.java) — test entry point via `SpringApplication.from(...).with(TestcontainersConfiguration.class)`
  - Tests import config: `@Import(TestcontainersConfiguration.class)` on `@SpringBootTest` classes
- **Test helper pattern:** All service tests share a `buildToken(UUID sub, String username, String email)` method that creates mock `JwtAuthenticationToken` instances. Each `@BeforeEach` creates fresh randomized users via `userService.getOrCreateUser(buildToken(...))`.
- **Spring Boot 4.x test starters (backend only):** Uses separate `-test` starters (`spring-boot-starter-data-jpa-test`, `spring-boot-starter-webmvc-test`) instead of the monolithic `spring-boot-starter-test`
- **Vaadin production profile:** Frontend `pom.xml` has a `production` profile that excludes `vaadin-dev` and enables `productionMode` — used by the Dockerfile (`-Pproduction`)
- **Vaadin BOM:** Version management via `vaadin-bom:25.0.5` in `<dependencyManagement>`

## Integration Points

- **Keycloak** (port 8180): Realm `lille-chat` with clients `lille-chat-backend` (bearer-only) and `lille-chat-frontend` (public). Test users: `alice`/`alice123` (user role), `bob`/`bob123` (user + admin roles). Realm config: [keycloak/lille-chat-realm.json](keycloak/lille-chat-realm.json)
- **PostgreSQL** (port 5432): Database `lillechat`, user/password `lillechat/lillechat`. Shared between backend and Keycloak in Docker Compose.
- **Backend → Frontend:** Frontend connects via `LILLE_CHAT_BACKEND_URL` env var (defaults to `http://backend:8080` in Docker)

## Security

- Backend is an OAuth2 resource server — dependency `spring-boot-starter-oauth2-resource-server` is present but security config (`SecurityFilterChain`, CORS) is not yet implemented
- User identity comes from Keycloak JWT: `sub` → user ID (UUID), `preferred_username` → username, `email` → email
- All `/api/**` endpoints should require Bearer token authentication (not yet enforced)

## Known Gaps

- No JPA entities, repositories, or service implementations — only interfaces and DTOs exist
- No REST controllers or security configuration
- No SSE notification service
- Backend `application.yaml` has no datasource/JPA/security config — relies on Docker Compose env vars
- Frontend has no Vaadin views or backend HTTP client
- Frontend `pom.xml` description still says "Backend" (copy-paste error)
- No CI/CD pipeline
