# Project Guidelines — Lille-Chat

## Architecture

Minimalistic chat application with two **independent** Spring Boot 4.x (Java 25) modules:

- **`backend/`** — REST API + JPA persistence (`dev.rabauer.lille.chat.backend`)
- **`frontend/`** — Vaadin 25 web UI (`dev.rabauer.lille_chat.frontend`)

No parent POM — each module has its own Maven Wrapper and build lifecycle. The frontend communicates with the backend via HTTP. Both default to port 8080; configure `server.port` in `application.yaml` to run them simultaneously.

## Code Style

- Java 25, Spring Boot 4.0.2, Vaadin 25.0.5
- Package-private test classes (no `public` modifier) — JUnit 5 style
- Package naming diverges due to hyphen restriction: backend uses `lille.chat` (two segments), frontend uses `lille_chat` (underscore)
- No formatter/linter configured — follow standard Java conventions

## Build and Test

Commands must run from within each module directory. Use `mvnw.cmd` on Windows:

```bash
# Backend
cd backend
mvnw.cmd clean install       # Build + test
mvnw.cmd spring-boot:run     # Run app
mvnw.cmd spring-boot:test-run # Run with Testcontainers (dev services)
mvnw.cmd test                # Tests only

# Frontend
cd frontend
mvnw.cmd clean install       # Build + test (includes Vaadin frontend build)
mvnw.cmd spring-boot:run     # Run app
mvnw.cmd test                # Tests only
```

## Project Conventions

- **Testcontainers pattern (backend):** Uses Spring Boot 4.x test-run pattern:
  - [TestcontainersConfiguration.java](backend/src/test/java/dev/rabauer/lille/chat/backend/TestcontainersConfiguration.java) — `@TestConfiguration` class for container `@Bean` declarations
  - [TestBackendApplication.java](backend/src/test/java/dev/rabauer/lille/chat/backend/TestBackendApplication.java) — test entry point via `SpringApplication.from(...).with(TestcontainersConfiguration.class)`
  - Tests import config: `@Import(TestcontainersConfiguration.class)` on `@SpringBootTest` classes
- **Spring Boot 4.x test starters:** Backend uses separate `-test` starters (`spring-boot-starter-data-jpa-test`, `spring-boot-starter-webmvc-test`) instead of the monolithic `spring-boot-starter-test`
- **Vaadin dev mode:** Frontend includes `vaadin-dev` (optional) for development and uses `vaadin-maven-plugin` with `build-frontend` goal
- **Vaadin BOM:** Version management via `vaadin-bom:25.0.5` in `<dependencyManagement>`

## Known Gaps (Early Stage)

- No database driver declared yet — JPA requires one (e.g., PostgreSQL + Testcontainers container bean)
- No controllers, services, entities, repositories, or Vaadin views implemented
- `TestcontainersConfiguration` is empty — add container beans when adding a database
- No Docker, CI/CD, or formatting configuration exists
- Frontend `pom.xml` description still says "Backend" (copy-paste error)
