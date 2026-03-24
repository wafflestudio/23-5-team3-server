# AGENTS.md — SNUXI Backend

> Coding agent instructions for the SNUXI backend.
> Human contributors should also follow these conventions.

## Project Overview

- **Service**: SNUXI — SNU taxi ride-sharing matchmaker
- **Stack**: Kotlin 2.0 + Spring Boot 3.4 + JPA + MySQL 8 + WebSocket (STOMP)
- **JDK**: 21 (Eclipse Temurin)
- **Build**: Gradle 9.x (Kotlin DSL)
- **Auth**: Google OAuth2 → session cookie (`SNUXI_SESSION`)
- **Infra**: EC2 (Docker Compose), S3 (profile images), FCM (push)
- **DB migrations**: Flyway
- **API docs**: Swagger UI at `/swagger-ui/index.html`

## Setup Commands

```bash
# Build (compile only, fast)
./gradlew compileKotlin

# Build (full, with tests)
./gradlew build

# Run locally
./gradlew bootRun

# Single test
./gradlew test --tests "com.snuxi.pot.service.PotServiceTest"

# All tests
./gradlew test

# Lint check
./gradlew ktlintCheck

# Lint auto-fix
./gradlew ktlintFormat
```

## Domain Terminology

> **IMPORTANT**: Internal code uses `Pot`, but API URLs use `/rooms`.

| Concept | Code (internal) | API URL | DB table |
|---------|-----------------|---------|----------|
| Taxi group | `Pot`, `Pots` (entity) | `/rooms/*` | `pots` |
| Member | `Participant`, `Participants` | `/rooms/{id}/participants` | `participants` |
| Location | `Landmark` | `/maps/landmarks` | `landmarks` |

Long-term TODO: Rename `Pot` → `Room` across the codebase for consistency.

## Package Structure

```
com.snuxi/
├── admin/          # Admin dashboard (ROLE_ADMIN only)
├── chat/           # WebSocket chat (STOMP)
│   ├── controller/ # ChatRealTimeController (WS), ChatMessageController (REST)
│   ├── service/    # ChatMessageService, ChatBotService, ChatRealTimeService
│   ├── entity/     # ChatMessage
│   └── dto/
├── config/         # Security, OAuth2, WebSocket, Firebase configs
├── exception/      # Global exception handler + base DomainException
├── global/util/    # CookieUtils and shared utilities
├── infra/fcm/      # Firebase Cloud Messaging client
├── notification/   # Push notification scheduling + device management
├── participant/    # Pot membership (entity + repository only)
├── pot/            # Core domain: taxi pot (room) CRUD
│   ├── controller/ # PotController, LandmarkController
│   ├── service/    # PotService, PotCleanupScheduler, LandmarkService
│   ├── entity/     # Pots
│   ├── dto/
│   └── model/      # Landmark (read-only reference)
├── security/       # CustomOAuth2User, CurrentUserIdResolver
├── terms/          # Terms of service agreement
└── user/           # User CRUD, OAuth2, reporting, image upload
    ├── controller/ # UserController, AuthController, UserImageController
    ├── service/    # UserService, GoogleOAuth2UserService, UserImageService
    ├── model/      # User entity
    └── dto/
```

## Key Files

| Purpose | Path |
|---------|------|
| Entry point | `SnuxiApplication.kt` |
| Security config | `config/SecurityConfig.kt` |
| OAuth2 success | `config/OAuth2AuthenticationSuccessHandler.kt` |
| OAuth2 failure | `config/CustomAuthenticationFailureHandler.kt` |
| Cookie utilities | `global/util/CookieUtils.kt` |
| WebSocket config | `config/WebSocketConfig.kt` |
| Global exceptions | `exception/DomainExceptionHandler.kt` |
| DB migrations | `src/main/resources/db/migration/V*.sql` |
| App config | `src/main/resources/application.yaml` |
| Docker | `Dockerfile` (multi-stage: Gradle build → JRE) |
| CI/CD | `.github/workflows/deploy.yml` |

## Code Style

- **Linter**: ktlint (enforced via Gradle plugin)
- Run `./gradlew ktlintCheck` before committing
- Run `./gradlew ktlintFormat` to auto-fix
- Follow Kotlin official coding conventions
- 4-space indentation (ktlint default)

## Architecture Rules

- **Layer separation**: Controller → Service → Repository. No shortcuts.
- **Controllers** handle HTTP/WS mapping only. No business logic.
- **Services** contain all business logic. Inject repositories via constructor.
- **DTOs** for request/response. Never expose entities directly to controllers.
- **Entities** are JPA-only. No business methods on entities.
- **Exceptions**: Domain exceptions live in each domain package (`PotException.kt`, `UserException.kt`). Global handler in `exception/DomainExceptionHandler.kt`.
- **Logging**: Use SLF4J with companion object pattern:
  ```kotlin
  companion object {
      private val logger = LoggerFactory.getLogger(MyService::class.java)
  }
  ```
  Log `info` for key business events, `error` for failures with context.

## API Conventions

- Success responses: return DTO directly (no wrapper)
- Error responses: `ErrorResponse` class via `DomainExceptionHandler`
- New endpoints: add `@Operation(summary = "...")` annotation when possible
- Auth: most endpoints require authenticated session. Public endpoints are explicitly listed in `SecurityConfig.kt`

## Database & Migrations (Flyway)

- **NEVER modify existing migration files.** Flyway checksums will break.
- New migrations: `V{next_number}__{snake_case_description}.sql`
  - Current latest: `V15`. Next: `V16__description.sql`
  - Use double underscore `__` between version and description
- Test migrations locally before pushing
- Column additions: use `ALTER TABLE ... ADD COLUMN`, not recreate

## Testing

- Framework: JUnit 5 + Spring Boot Test
- Test DB: H2 in-memory (already in dependencies)
- Mocking: MockK (preferred for Kotlin)
- **New features should include Service-layer unit tests**
- Test file location mirrors source: `src/test/kotlin/com/snuxi/{domain}/...`
- Run relevant tests after changes: `./gradlew test --tests "com.snuxi.{domain}.*"`

## Git Conventions

- **Commit messages**: conventional commits, English only
  - `feat: add gender matching option`
  - `fix: resolve OAuth cookie cleanup on failure`
  - `refactor: extract pot validation logic`
  - `chore: add ktlint configuration`
  - `docs: update AGENTS.md`
- **Branch naming**: `feat/feature-name`, `fix/bug-name`, `refactor/target`, `chore/task`
- **PR**: 1 topic per PR, small diffs, remove debug logs before merge
- **IMPORTANT**: Always run `./gradlew compileKotlin` before pushing. Import errors break deploy.

## Don'ts

- **No secrets in code.** Use environment variables. (TODO: move `terms-jwt.secret` to env var)
- **No wildcard imports** (`import com.snuxi.pot.*` — specify each class)
- **No `@ToString` on entities** (circular reference risk with JPA)
- **No direct repository calls from controllers** (go through Service)
- **No modifying Flyway migration files** after they've been applied
- **No full build in tight loops** — use `compileKotlin` or single test for fast feedback

## CI/CD

- Push to `main` → GitHub Actions → Docker build → EC2 deploy
- Docker build uses `gradle build -x test` (tests skipped in Docker for speed)
- Workflow also runs `compileKotlin` to catch compilation errors early
- Deployment: `docker-compose.prod.yaml` on EC2

## Long-term TODOs

- [ ] Rename `Pot` → `Room` across codebase for API consistency
- [ ] Move `terms-jwt.secret` to environment variable
- [ ] Add HikariCP pool configuration (after EC2 upgrade to 4GB)
- [ ] Clean up expired chat messages in `PotCleanupScheduler`
- [ ] Add comprehensive test coverage (Service layer priority)
- [ ] Add pre-commit hook for ktlint + compile check

## When Stuck

- Don't push speculative large changes
- Ask a clarifying question or propose a plan first
- Check existing patterns in `pot/service/PotService.kt` as reference
- Check `SecurityConfig.kt` for auth/endpoint rules
