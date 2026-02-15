# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Lille-Chat is a minimalistic instant-messaging application with two **independent** Spring Boot 4.x (Java 25) modules:

- **`backend/`** — REST API + JPA persistence (`dev.rabauer.lille.chat.backend`)
- **`frontend/`** — Vaadin 25 web UI (`dev.rabauer.lille_chat.frontend`)

No parent POM — each module has its own Maven Wrapper. Frontend communicates with backend via HTTP. Real-time message delivery uses SSE (`SseEmitter`).

## Build Commands

Commands must run from within each module directory. Use `mvnw.cmd` on Windows:

```bash
# Backend (from backend/)
mvnw.cmd clean install                # Build + test
mvnw.cmd spring-boot:run              # Run app
mvnw.cmd spring-boot:test-run         # Run with Testcontainers
mvnw.cmd test                         # Tests only
mvnw.cmd test -Dtest=UserServiceTest  # Single test class

# Frontend (from frontend/)
mvnw.cmd clean install                # Build + test (includes Vaadin frontend build)
mvnw.cmd spring-boot:run              # Run app

# Docker (from repo root)
docker compose up --build             # All services: postgres, keycloak, backend, frontend
```

**Java version note:** Project targets Java 25 but local JDK may be 21. Use `-Djava.version=21` to compile locally. Docker builds use `eclipse-temurin:25-jdk`.

## Architecture

### Backend Layering
- `dto/` — Java records for API data transfer (UserDto, ChatMessageDto, etc.)
- `service/` — Interfaces defining business contracts; implementations use JPA repositories
- Service interfaces accept/return DTOs, never entities
- `UserService.getOrCreateUser(JwtAuthenticationToken)` auto-provisions users from Keycloak JWT claims (`sub`, `preferred_username`, `email`)

### Frontend Structure
- `view/` — Vaadin views (MainView, ChatView, ConversationListView, etc.)
- `service/` — HTTP client services that call backend REST API
- `config/` — Security and WebClient configuration

### Package Naming
Backend uses `lille.chat` (two segments), frontend uses `lille_chat` (underscore) due to Java hyphen restrictions.

## Testing Patterns

### Testcontainers Setup (Backend)
- `TestcontainersConfiguration.java` — `public @TestConfiguration` with `@Bean @ServiceConnection PostgreSQLContainer`
- `TestBackendApplication.java` — test entry point via `SpringApplication.from(...).with(TestcontainersConfiguration.class)`
- Tests import config: `@Import(TestcontainersConfiguration.class)` on `@SpringBootTest` classes

### Test Helper Pattern
All service tests share a `buildToken(UUID sub, String username, String email)` method that creates mock `JwtAuthenticationToken` instances. Each `@BeforeEach` creates fresh randomized users.

## Integration Points

- **Keycloak** (port 8180): Realm `lille-chat` with clients `lille-chat-backend` (bearer-only) and `lille-chat-frontend` (public)
- **Test users:** `alice`/`alice123` (user role), `bob`/`bob123` (user + admin roles)
- **PostgreSQL** (port 5432): Database `lillechat`, user/password `lillechat/lillechat`
- **Backend → Frontend:** Frontend connects via `LILLE_CHAT_BACKEND_URL` env var (defaults to `http://backend:8080` in Docker)

## Port Mapping (Docker Compose)
- Backend: 8080
- Frontend: 8081
- Keycloak: 8180
- PostgreSQL: 5432

## Code Style

- Java 25, Spring Boot 4.0.2, Vaadin 25.0.5
- DTOs are Java `record` types
- Package-private test classes (no `public` modifier) — JUnit 5 style
- No formatter/linter configured — follow standard Java conventions
