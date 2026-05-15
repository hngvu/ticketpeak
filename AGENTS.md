# AGENTS.md

> Instructions for AI coding agents working on the **Ticketpeak** codebase.
> Read this file before making any changes.

---

## Project Overview

**Ticketpeak** is a modern event ticketing platform for the Vietnamese market and beyond.
It supports the full lifecycle: event creation, seat management, smart QR ticketing, and post-event payouts.

Three main personas:
- **Organizers** — create events, manage seating, track revenue
- **Buyers** — discover events, checkout securely, receive TOTP-based QR tickets
- **Platform** — anti-scalping controls, i18n (Vietnamese + English)

---

## Feature Modules

Each module lives under its own package in `api/` and its own route group in `web/`. New features belong in the most specific module listed below.

| Module           | Scope                                                                 |
|------------------|-----------------------------------------------------------------------|
| `auth`           | Login, logout, token refresh, OAuth (social login), MFA               |
| `iam`            | Roles, permissions, policies (RBAC/ABAC)                              |
| `account`        | User profile, KYC verification, address book, preferences             |
| `organization`   | Org profile, member management, approval workflows                    |
| `event`          | Create, update, publish, cancel, postpone events                      |
| `venue`          | Venue management, manifest, seating chart                             |
| `offer`          | Ticket types, pricing tiers, presale & sale windows, promo codes      |
| `order`          | Cart, seat reservation, checkout, full order lifecycle                |
| `ticket`         | Ticket generation (TOTP QR), check-in, transfer, status tracking      |
| `resale`         | Secondary market listings, resale orders, commission calculation      |
| `payment`        | Payment gateway integration, organizer payouts, refunds               |
| `search`         | Full-text search, filtering, faceted search, result ranking           |
| `discovery`      | Trending events, similar events, personalised feed                    |
| `notification`   | Email, push notifications, in-app alerts                              |
| `promotion`      | Coupons, discounts, campaigns, vouchers                               |
| `report`         | Revenue analytics, attendance reports, sales dashboards               |
| `admin`          | Content moderation, system management, audit logs                     |

---

## Tech Stack

| Layer       | Technology                                                          |
|-------------|---------------------------------------------------------------------|
| `web/`      | SvelteKit, shadcn-svelte, Tailwind CSS v4, TanStack Query           |
| `api/`      | Java 21, Spring Boot 3.5, Spring Security, Spring Data JPA, Flyway |
| Database       | PostgreSQL, Redis                                                   |
| Object Storage | MinIO                                                               |
| Infrastructure | Docker, GitHub Actions                                              |

---

## Repository Layout

```
ticketpeak/
├── api/                    # Spring Boot app + Docker Compose
│   ├── docker-compose.yml  # ← tất cả lệnh docker compose chạy từ đây
│   ├── .env
│   └── src/
└── web/                    # SvelteKit app
    └── src/
```

> `docker-compose.yml` nằm trong `api/`, **không phải** root. Mọi lệnh `docker compose` đều phải chạy từ thư mục `api/`. Chạy từ sai thư mục sẽ báo lỗi `no configuration file provided`.

---

## Setup & Running Locally

### Prerequisites

- Docker & Docker Compose
- Node.js ≥ 20 + npm

The entire backend (`api/`), database (PostgreSQL), cache (Redis), and object storage (MinIO) run inside Docker. There is **no way to run `api/` outside of Docker** — do not attempt to run it standalone with Maven.

### Start the full stack

`docker-compose.yml` is inside `api/` — always `cd api` first.

```bash
cd api

# Build images and start all services: api, PostgreSQL, Redis, MinIO
docker compose up -d --build

# Check all services are healthy
docker compose ps

# Stream logs from the Spring Boot service
docker compose logs -f ticketpeak-api

# Stop all services (keeps data volumes)
docker compose down

# Stop and wipe all data (PostgreSQL, Redis, MinIO) — use when resetting state
docker compose down -v
```

Flyway migrations run **automatically** when the `api` container starts — never edit existing migration files.

### `web/` (SvelteKit)

`web/` has no Docker involvement — run it directly with npm.

```bash
cd web

npm install        # Install dependencies
npm run dev        # Start dev server (proxies API calls to http://localhost:8080)
npm run check      # Type-check with svelte-check
npm run lint       # ESLint + Prettier check
npm run build      # Production build
npm run preview    # Preview production build
```


## Code Style

### `web/` (SvelteKit / TypeScript)

- **TypeScript strict mode** — always declare types explicitly, no `any`.
- Single quotes, no semicolons.
- Tailwind CSS v4 utility classes only — do **not** write custom CSS unless there is no alternative.
- Use **shadcn-svelte** components before building anything from scratch.
- All data fetching via **TanStack Query** (`createQuery`, `createMutation`) — no raw `fetch` calls inside components.
- Svelte stores for global state; keep component state local when possible.
- File naming: `kebab-case` for routes and files, `PascalCase` for Svelte components.

### `api/` (Java / Spring Boot)

- Java 21 — prefer records, sealed classes, pattern matching, and text blocks where appropriate.
- Follow the layered structure: `Controller → Service → Repository`. No business logic in controllers or repositories.
- All API response objects are Java **records** (DTOs). Never expose `@Entity` classes directly in API responses.
- Use **Spring Data JPA** for queries; write JPQL for complex cases — raw SQL lives only in Flyway migrations.
- **Flyway migrations**: add new files named `V{n}__{description}.sql`. **Never modify existing migration files.**
- REST endpoints: plural nouns, versioned under `/api/v1/...`.
- All controllers return `ResponseEntity<ApiResponse<T>>`.
- Exceptions are handled globally via `@RestControllerAdvice` — do not catch-and-swallow exceptions in services.

---

## Testing

### `web/`

```bash
cd web

npm test              # Run all Vitest unit/component tests
npm run test:watch    # Watch mode
npm run test:coverage # Coverage report
```

### `api/`

Tests run inside Docker. `docker-compose.yml` is in `api/`, so `cd api` first. Testcontainers spins up its own isolated PostgreSQL/Redis instances — the running stack is not affected.

```bash
cd api

# Run all tests (unit + integration)
docker compose run --rm ticketpeak-api ./mvnw verify

# Unit tests only (faster, no Testcontainers)
docker compose run --rm ticketpeak-api ./mvnw test

# Single test class
docker compose run --rm ticketpeak-api ./mvnw test -Dtest=TicketServiceTest
```

**Rules:**
- Every feature and bug fix must include tests.
- Integration tests in `api/` use **Testcontainers** — do not mock PostgreSQL or Redis.
- All tests must pass before committing.


## API Conventions

- Base path: `/api`
- Auth: `Authorization: Bearer <JWT>`

### Success response

```json
{
  "success": true,
  "data": { },
  "message": "OK",
  "timestamp": "2025-05-14T10:00:00Z"
}
```

### Error response (business / application error)

```json
{
  "success": false,
  "error": "TICKET_NOT_FOUND",
  "message": "Ticket with id 42 does not exist",
  "timestamp": "2025-05-14T10:00:00Z"
}
```

### Validation error response (HTTP 400)

Returned when request body fails `@Valid` / `@Validated` constraints. The `errors` field is a map of `field → list of messages`.

```json
{
  "success": false,
  "error": "VALIDATION_FAILED",
  "message": "Request validation failed",
  "errors": {
    "email": ["must be a valid email address"],
    "price": ["must be greater than 0"],
    "startDate": ["must not be null", "must be a future date"]
  },
  "timestamp": "2025-05-14T10:00:00Z"
}
```

Rules:
- `errors` is only present on `VALIDATION_FAILED` responses — omit it on all other error types.
- Field names in `errors` must match the exact JSON property names in the request body, not the Java field names.
- Global `@RestControllerAdvice` handles `MethodArgumentNotValidException` and produces this shape — do not handle it locally in controllers.

---

## Git & PR Conventions

### Branch flow

```
feat/<scope>  ──►  dev  ──►  main
fix/<scope>   ──►  dev  ──►  main
```

- All work branches cut from `dev`.
- PRs merge into `dev` first; `dev` → `main` is a release merge.
- Do **not** push directly to `dev` or `main`.

### Branch naming

```
feat/<module>/<short-description>
fix/<module>/<short-description>
chore/<short-description>
docs/<short-description>
```

Examples: `feat/ticket/totp-rotation`, `fix/order/seat-expiry`

### Commit format

```
type(layer/module): short description
```

`layer` is either `api` or `web` — always include it so the scope of a commit is immediately clear.

| Type | Use for |
|------|---------|
| `feat` | New functionality |
| `fix` | Bug fix |
| `chore` | Tooling, deps, config |
| `docs` | Documentation only |
| `refactor` | Restructure without behaviour change |
| `test` | Adding or fixing tests |
| `style` | Formatting, whitespace |

Examples:
```
feat(api/offer): add presale window scheduling
feat(web/order): implement seat picker UI
fix(api/order): fix seat double-booking race condition
fix(web/auth): correct OAuth redirect on mobile
chore(api): upgrade Spring Boot to 3.5.1
```

### Before committing

```bash
# web/ — chạy trực tiếp
cd web && npm run check && npm run lint && npm test

# api/ — phải cd vào api/ trước vì docker-compose.yml nằm ở đó
cd api && docker compose run --rm ticketpeak-api ./mvnw verify
```

PRs require passing GitHub Actions CI before merge.


## Common Gotchas

- **Flyway:** never edit `V{n}__*.sql` files that already exist — always create a new migration.
- **MinIO:** bucket names must be lowercase with no underscores. Check `StorageConfig` for bucket name constants.
- **Redis seat locks:** reservations expire automatically via TTL. Do not manually delete Redis keys related to seat locking.
- **Tailwind v4:** utility class names differ from v3 in places — check the [v4 docs](https://tailwindcss.com/docs) before adding unfamiliar classes.
- **TanStack Query:** always invalidate relevant queries after mutations — do not rely on stale cache in booking-critical flows.
- **`docker compose` phải chạy từ `api/`:** `docker-compose.yml` nằm trong `api/`, không phải root. Luôn `cd api` trước khi chạy bất kỳ lệnh `docker compose` nào — chạy từ sai thư mục sẽ không tìm thấy file config.
- **Docker volumes:** để reset toàn bộ data (DB, Redis, MinIO) khi có migration conflict: `cd api && docker compose down -v`.
- **`api/` là Docker-only:** không chạy Spring Boot bằng `./mvnw spring-boot:run` ngoài Docker — biến môi trường và service discovery được cấu hình cho Compose network, sẽ không hoạt động ngoài container.


## CI/CD (GitHub Actions)

- **On PR to `dev`:** lint, type-check, unit tests, integration tests.
- **On merge to `main`:** build Docker images, push to registry, deploy to staging.
- Secrets are managed in GitHub repository settings — never committed to the repo.


## Feature Plans

Feature plans are stored in `plan/` as individual files.

Naming: `NNN-feature-name.md` (e.g. `001-user-auth.md`)

Each file contains:
- Description
- Acceptance criteria (defined before implementation)
- Status: planned / in-progress / done
- Outcome (filled after implementation)
