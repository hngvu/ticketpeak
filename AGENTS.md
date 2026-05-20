# AGENTS.md

> Instructions for AI coding agents working on the Ticketpeak codebase.

---

## Project Overview

Ticketpeak is an event ticketing platform for the Vietnamese market and beyond.
It supports event creation, venue and seat management, smart QR ticketing, and post-event payouts.

Main personas:
- Organizers: create events, manage seating, track revenue
- Buyers: discover events, checkout securely, receive TOTP-based QR tickets
- Platform: anti-scalping controls, i18n for Vietnamese and English

---

## Feature Modules

Each module lives under its own package in `api/` and its own route group in `web/`.
New features belong in the most specific module listed below.

| Module | Scope |
| --- | --- |
| `auth` | Login, logout, token refresh, social login, MFA |
| `iam` | Roles, permissions, policies (RBAC/ABAC) |
| `account` | User profile, KYC verification, address book, preferences |
| `organization` | Org profile, member management, approval workflows |
| `event` | Create, update, publish, cancel, postpone events |
| `venue` | Venue management, manifest, seating chart |
| `offer` | Ticket types, pricing tiers, presale and sale windows, promo codes |
| `order` | Cart, seat reservation, checkout, full order lifecycle |
| `ticket` | Ticket generation (TOTP QR), check-in, transfer, status tracking |
| `resale` | Secondary market listings, resale orders, commission calculation |
| `payment` | Payment gateway integration, organizer payouts, refunds |
| `search` | Full-text search, filtering, faceted search, result ranking |
| `discovery` | Trending events, similar events, personalized feed |
| `notification` | Email, push notifications, in-app alerts |
| `promotion` | Coupons, discounts, campaigns, vouchers |
| `report` | Revenue analytics, attendance reports, sales dashboards |
| `admin` | Content moderation, system management, audit logs |

---

## Tech Stack

| Layer | Technology |
| --- | --- |
| `web/` | SvelteKit, shadcn-svelte, Tailwind CSS v4, TanStack Query |
| `api/` | Java 21, Spring Boot 3.5, Spring Security, Spring Data JPA, Flyway |
| Database | PostgreSQL, Redis |
| Object Storage | MinIO |
| Infrastructure | Docker, GitHub Actions |

---

## Repository Layout

```text
ticketpeak/
├── api/                    # Spring Boot app + Docker Compose
│   ├── docker-compose.yml  # all docker compose commands run from here
│   ├── .env
│   └── src/
└── web/                    # SvelteKit app
    └── src/
```

`docker-compose.yml` lives in `api/`, not the repo root.
Always run `docker compose` commands from `api/`.

---

## Setup and Running Locally

### Prerequisites

- Docker and Docker Compose
- Node.js 20+ and npm

The backend, PostgreSQL, Redis, and MinIO run inside Docker.
Do not try to run `api/` standalone with Maven outside Docker.

### Start the full stack

```bash
cd api
docker compose up -d --build
docker compose ps
docker compose logs -f ticketpeak-api
docker compose down
docker compose down -v
```

Flyway migrations run automatically when the `api` container starts.
Never edit existing migration files.

### `web/` (SvelteKit)

`web/` has no Docker involvement. Run it directly with npm.

```bash
cd web
npm install
npm run dev
npm run check
npm run lint
npm run build
npm run preview
```

---

## Code Style

### `web/` (SvelteKit / TypeScript)

- Use TypeScript strict mode and avoid `any`.
- Use single quotes and no semicolons.
- Use Tailwind CSS v4 utilities only; avoid custom CSS unless necessary.
- Prefer shadcn-svelte components before building from scratch.
- Use TanStack Query for data fetching; no raw `fetch` calls inside components.
- Use Svelte stores for global state; keep local state local.
- Use `kebab-case` for routes and files, `PascalCase` for Svelte components.

### `api/` (Java / Spring Boot)

- Java 21: prefer records, sealed classes, pattern matching, and text blocks where appropriate.
- Follow `Controller -> Service -> Repository`. No business logic in controllers or repositories.
- All API response objects are Java records.
- Use Spring Data JPA for queries; raw SQL belongs only in Flyway migrations.
- Add migrations as `V{n}__{description}.sql`. Never modify existing migration files.
- REST endpoints use plural nouns and base path `/api/...` with no versioning.
- Controllers return `ResponseEntity<ApiResponse<T>>`.
- Handle exceptions globally in `@RestControllerAdvice`; do not catch and swallow them in services.

---

## Testing

### `web/`

```bash
cd web
npm test
npm run test:watch
npm run test:coverage
```

### `api/`

Prefer `docker compose run --rm` for API tests. It starts a fresh container for each run, so tests do not depend on a previously running service container.
Testcontainers still spins up isolated PostgreSQL and Redis instances, so the local stack is not affected.

```bash
cd api

# Build and run all tests
docker compose run --rm ticketpeak-api ./mvnw verify

# Unit tests only
docker compose run --rm ticketpeak-api ./mvnw test

# Single test class
docker compose run --rm ticketpeak-api ./mvnw test -Dtest=TicketServiceTest
```

Rules:
- Every feature and bug fix must include tests.
- Integration tests in `api/` use Testcontainers. Do not mock PostgreSQL or Redis.
- All tests must pass before committing.

---

## API Conventions

- Base path: `/api`
- Auth: `Authorization: Bearer <JWT>`

### Success response

```json
{
  "success": true,
  "data": {},
  "message": "OK",
  "timestamp": "2025-05-14T10:00:00Z"
}
```

### Error response

```json
{
  "success": false,
  "error": "TICKET_NOT_FOUND",
  "message": "Ticket with id 42 does not exist",
  "timestamp": "2025-05-14T10:00:00Z"
}
```

### Validation error response

Returned when request body fails `@Valid` or `@Validated`.
The `errors` field is a map of `field -> list of messages`.

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
- `errors` is only present on `VALIDATION_FAILED` responses.
- Field names in `errors` must match the exact JSON property names in the request body, not the Java field names.
- Global `@RestControllerAdvice` handles `MethodArgumentNotValidException` and produces this shape.

---

## Git and PR Conventions

### Branch flow

```text
feat/<scope> -> main
fix/<scope>  -> main
```

- All work branches are cut from `main`.
- PRs merge into `main` directly.
- Do not push directly to `main`.

### Branch naming

```text
feat/<module>/<short-description>
fix/<module>/<short-description>
chore/<short-description>
docs/<short-description>
```

Examples:
`feat/ticket/totp-rotation`, `fix/order/seat-expiry`

### Commit format

```text
type(layer/module): short description
```

`layer` is either `api` or `web`.

| Type | Use for |
| --- | --- |
| `feat` | New functionality |
| `fix` | Bug fix |
| `chore` | Tooling, deps, config |
| `docs` | Documentation only |
| `refactor` | Restructure without behavior change |
| `test` | Adding or fixing tests |
| `style` | Formatting, whitespace |

Examples:
```text
feat(api/offer): add presale window scheduling
feat(web/order): implement seat picker UI
fix(api/order): fix seat double-booking race condition
fix(web/auth): correct OAuth redirect on mobile
chore(api): upgrade Spring Boot to 3.5.1
```

### Before committing

Run checks only for the layer you changed.

**If you changed `web/`:**

```bash
cd web && npm run check && npm run lint
```

**If you changed `api/`:**

```bash
cd api
docker compose run --rm ticketpeak-api ./mvnw compile -q
```

### Before opening a PR

Run the full test suite for the changed layer.

**If you changed `web/`:**

```bash
cd web && npm run check && npm run lint && npm test
```

**If you changed `api/`:**

```bash
cd api
docker compose run --rm ticketpeak-api ./mvnw verify
```

PRs require passing GitHub Actions CI before merge.

---

## Common Gotchas

- Never edit existing `V{n}__*.sql` Flyway files. Always create a new migration.
- MinIO bucket names must be lowercase with no underscores.
- Redis seat locks expire automatically via TTL. Do not manually delete those keys.
- Tailwind v4 utility names differ from v3 in places.
- Always invalidate relevant queries after mutations in TanStack Query flows.
- Always run `docker compose` from `api/`, not from the repo root.
- Use `cd api && docker compose down -v` to reset DB, Redis, and MinIO data.
- Do not run Spring Boot with `./mvnw spring-boot:run` outside Docker.

---

## CI/CD

- On PR to `main`: lint, type-check, unit tests, integration tests.
- On merge to `main`: build Docker images, push to registry, deploy to staging.
- Secrets are managed in GitHub repository settings and must never be committed.

---

## Feature Plans and Agent Workflow

Feature plans live in `plan/` as individual files.
Creating a plan is mandatory before starting implementation.

Naming: `NNN-feature-name.md`

Each plan file must contain:
- Description
- Acceptance criteria
- Status: `planned`, `in-progress`, or `done`
- Outcome

Before implementing any feature or significant change:

1. Plan: if no plan file exists, create one. If it exists, read it first.
2. Pause for approval: present the plan to the user and wait for explicit approval before writing code.
3. Implement: set status to `in-progress`, then write code following this file.
4. Verify: run the relevant tests and confirm acceptance criteria are met.
5. Complete: set status to `done`, fill in outcome, then commit using the commit format above.
