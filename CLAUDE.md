# SNUXI Backend (Spring Boot + Kotlin)

## Build & Test
- Compile: `./gradlew compileKotlin`
- Build (skip lint): `./gradlew build -x ktlintCheck -x ktlintKotlinScriptCheck`
- Test: `./gradlew test`
- ktlint fails on build.gradle.kts — known issue, ignore it

## Code Style
- Conventional Commits (English): feat:, fix:, refactor:
- No wildcard imports — explicit imports only
- Follow existing patterns (consistency > improvement)
- Minimize diff — don't refactor unrelated code

## Architecture
- JPA + MySQL, Flyway migrations
- Auth: Google OAuth + session cookie (SNUXI_SESSION)
- Real-time chat: STOMP WebSocket
- Participation: users join "pots" (taxi sharing rooms) via `participants` table

## Gotchas
- `User.activePotId` field has been REMOVED — participation is tracked ONLY via participants table
- Flyway migration filenames: `V{N}__{description}.sql` (N = max existing + 1)
- build.gradle.kts has tab/indent issues causing ktlint failures — do NOT modify it
- Don't touch test files unless explicitly asked
