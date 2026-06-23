# ARCHITECTURE.md — TicketPeak System Architecture

## System Overview

TicketPeak is a two-sided event ticketing platform:
- **Venue Managers** build seat maps (manifests)
- **Organizers** price and publish events on top of those maps
- **Buyers** discover, purchase, and hold TOTP-based QR tickets
- **Gate Staff** scan and validate tickets at the door

---

## Monorepo Layout

```
ticketpeak/
├── api/                    # Spring Boot backend
│   ├── docker-compose.yml  # Infrastructure only (Postgres, Redis, MinIO)
│   └── src/
│       └── main/java/.../  # Packages by module (see below)
├── web/                    # SvelteKit frontend
│   └── src/
│       └── routes/         # File-based routing
├── plan/                   # Feature plan files (NNN-name.md)
├── docs/                   # Harness documents
└── AGENTS.md               # Agent entry point
```

---

## Backend Module Boundaries

Each module owns its own package. Cross-module calls go through service interfaces, never direct repository access across modules.

```
api/src/main/java/.../
├── auth/           # Login, logout, JWT, MFA, social OAuth
├── iam/            # Roles, permissions, RBAC/ABAC policies
├── account/        # User profile, KYC, multi-role support
├── organization/   # Org profile, member management, invitations
├── venue/          # Venue, Manifest, Levels, Sections, Areas, Rows, Seats
├── event/          # Event lifecycle, classifications, attractions
├── eventgroup/     # Tour/event group grouping
├── offer/          # Ticket tiers, pricing, presale windows, promo codes
├── order/          # Cart, reservation (Redis seat hold), checkout
├── ticket/         # TOTP QR generation, check-in, transfer, void
├── resale/         # Secondary market listings, resale orders
├── payment/        # Payment gateway (VNPay/Stripe), payouts, refunds
├── search/         # Full-text search, faceted filtering
├── discovery/      # Trending events, personalized feed
├── notification/   # Email, push, in-app alerts
├── promotion/      # Coupons, campaigns, vouchers
├── report/         # Revenue analytics, attendance dashboards
└── admin/          # Content moderation, audit logs
```

---

## Venue Data Model Hierarchy

This is the canonical model. Do not introduce new abstractions without a decision record.

```
Venue
└── Manifest (versioned seat map, one active at a time)
    ├── Level          (lookup: Floor, Balcony, Upper — with color)
    ├── Section        (lookup: Section A, Section B — belongs to Level)
    ├── PriceLevel     (lookup: VIP, Standard, GA — with color)
    ├── GA Area        (general admission block → capacity, no individual seats)
    │   └── linked to Section + PriceLevel
    └── RS Area        (reserved seating block)
        └── Row
            └── Seat   (positionX, positionY, status, accessibility flag)
```

**Key rules:**
- Organizers never touch the manifest — they only read seat categories (PriceLevels) to price offers
- `Section` replaced the old `RSArea` naming — do not reintroduce `RSArea` as a concept
- Seat coordinates: `positionX` = column (0 = left), `positionY` = row (0 = top)
- Bulk seat creation always uses `.saveAll()` — never `.save()` inside a loop
- Manifests are versioned: only one `PUBLISHED` manifest per venue at a time

---

## Frontend Route Structure

```
web/src/routes/
├── (consumer)/             # Public-facing buyer site
│   ├── +layout.svelte      # Consumer nav
│   ├── +page.svelte        # Homepage (trending events)
│   ├── events/[slug]/      # Event detail + seat map
│   └── checkout/           # Purchase flow
├── auth/                   # Login, register, OAuth callback
├── b2b/                    # Organizer portal
│   ├── events/             # Create/manage events
│   └── offers/             # Pricing configuration
└── ops/                    # Admin/ops portal
    └── venues/             # Venue + manifest management
        └── [id]/manifests/[mid]/  # Seat map builder (Konva.js canvas)
```

**Layout guard:** `/ops/` and `/b2b/` set `isAuthOrPortal = true` → skip consumer nav, use portal layout.

---

## Seat Map Canvas (Konva.js)

The ops manifest editor uses **Konva.js** (not plain SVG) for the interactive canvas.

Key decisions (do not reverse without a decision record):
- **Section boundaries** use explicit Polygon/SVG geometries (not dynamically computed convex hulls) to allow for complex, concave, and curved architectural shapes. The UI must ensure seats are placed with padding so they do not overlap boundaries.
- **Curvature** and complex layouts are supported by plotting seats within these explicit polygons.
- Canvas tools: Select (V), Pan (Space), Brush (B), Eraser (E) — Figma-style shortcuts.
- Area creation UI has been removed from the editor (Plan 033). Areas are created programmatically or via seeding; only editing of existing areas is supported.

**Performance boundary:** Konva.js handles up to ~5,000 seats before frame rate degrades. Above that, virtualization or WebGL is needed. Do not add features that iterate all seats synchronously at scale.

---

## API Namespaces

| Prefix | Audience | Example |
|--------|----------|---------|
| `/api/ops/...` | Platform admin (internal) | `/api/ops/venues` |
| `/api/partner/...` | Organizers (B2B portal) | `/api/partner/events` |
| `/api/...` | Buyers / public | `/api/events`, `/api/orders` |

Auth: `Authorization: Bearer <JWT>` on all protected endpoints.

---

## Seat Reservation Flow (Critical Path)

```
Buyer selects seat
  → POST /api/orders/reserve  (Redis SETNX seat lock, 10-min TTL)
  → Seat status → HOLD in DB
  → Buyer completes checkout within 10 min
  → POST /api/orders/confirm + payment webhook
  → Seat status → RESERVED, Ticket issued with TOTP secret
  → Timer expires without payment → Redis TTL fires → seat released → AVAILABLE
```

**Rule:** Redis seat locks expire automatically via TTL. Never manually delete lock keys.

---

## Role Model (Multi-Role, as of Plan 034)

Each account holds a `Set<Role>` via `account_role` join table.

| Role | Portal | Can do |
|------|--------|--------|
| `BUYER` | consumer | Browse, purchase, hold tickets (default on register) |
| `ORGANIZER` | `/b2b` | Create events, configure offers, view sales |
| `VENUE_MANAGER` | `/b2b` | Create venues, build manifests |
| `ADMIN` | `/ops` | Approve events/orgs, moderate, manage platform |

**Rule:** Check role membership with `principal.roles().contains(Role.X)`. Never use `principal.role() != Role.X` (old scalar pattern — removed in Plan 034).

---

## Boundary Rules for Agents

- No business logic in Controllers or Repositories — only in Services
- No raw SQL in Java code — Flyway migrations only
- No cross-module repository access — go through the owning module's service
- No reflection-heavy patterns without `reflect-config.json` entry (GraalVM native image)
- No mocking of PostgreSQL or Redis in integration tests — use Testcontainers
- No editing of existing `V{n}__*.sql` migration files — always create a new one
