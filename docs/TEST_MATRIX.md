# TEST_MATRIX.md — Behavior-to-Proof Validation

This matrix maps each major system behavior to the proof required before it can be considered done.
Agents consult this before marking any plan as `done`.

---

## How to Use

For each feature, find the relevant rows. Run the listed proof commands. All must pass.

| Symbol | Meaning |
|--------|---------|
| ✅ | Required — must pass before `done` |
| ⚠️ | Required for High-Risk work |
| 📋 | Manual verification (no automated test exists yet) |

---

## Layer Checks (Always Run for Changed Layer)

### `api/` changed

```bash
cd api && ./mvnw compile -q                    # compile check
cd api && ./mvnw spring-boot:process-aot       # GraalVM AOT check
cd api && ./mvnw verify                        # unit + integration tests (needs Docker)
```

### `web/` changed

```bash
cd web && npm run check                        # Svelte/TypeScript check
cd web && npm run lint                         # ESLint + Prettier
cd web && npm test                             # Vitest unit tests
```

---

## Feature Behavior Matrix

### Auth & Identity

| Behavior | Proof |
|----------|-------|
| Register with email → base role `BUYER` | `AuthControllerIT` |
| Login returns JWT with all roles | `AuthControllerIT` |
| Roles encoded as comma-separated string in JWT | `AuthControllerIT` |
| Route protection by role | `AuthControllerIT`, `PermissionControllerIT` |
| Multi-role account holds Set<Role> | `AccountControllerIT` |

### Venue & Manifest

| Behavior | Proof |
|----------|-------|
| Create venue → appears in list | `VenueControllerIT` |
| Create manifest → status `DRAFT` | `VenueControllerIT` |
| Publish manifest → status `PUBLISHED` | `VenueControllerIT` |
| Only one `PUBLISHED` manifest per venue | `VenueControllerIT` |
| Bulk seat creation uses `.saveAll()`, not loop `.save()` | Code review + `VenueControllerIT` |
| Seat overflow does not occur (convex hull geometry) | 📋 Visual check in ops editor |
| RS area border matches actual seat positions | 📋 Visual check in ops editor |
| Canvas handles 1,000+ seats without freeze | 📋 Manual perf check |

### Event & Offers

| Behavior | Proof |
|----------|-------|
| Create event → status `DRAFT` | `EventControllerIT` |
| Submit for approval → `PENDING_APPROVAL` | `EventControllerIT` |
| Admin approve → `PUBLISHED` | `EventControllerIT` |
| Offer deletion blocked when `quantitySold > 0` | `OfferControllerIT` |
| Sale window `endAt > startAt` enforced | `OfferControllerIT` |
| Event `endAt > startAt` enforced | `EventControllerIT` |

### Order & Reservation

| Behavior | Proof |
|----------|-------|
| Seat hold created in Redis on reserve | `ReservationControllerIT` |
| Seat status → `HOLD` on reserve | `ReservationControllerIT` |
| Hold expires → seat released → `AVAILABLE` | `ReservationControllerIT` (TTL simulation) |
| Double-booking prevented (SETNX) | `ReservationControllerIT` |
| Order confirmed → ticket issued | `OrderServiceIT` |
| Payment failure → reservation released | `PaymentControllerIT` |

### Ticket & Check-In

| Behavior | Proof |
|----------|-------|
| Ticket issued with TOTP secret on order confirm | `TicketServiceIT` |
| QR rotates every 30s (TOTP window) | `TicketServiceIT` |
| First scan → `CHECKED_IN` | `TicketServiceIT` |
| Second scan → rejected ("already used") | `TicketServiceIT` |
| Transfer → `TRANSFER_PENDING` → new owner issued | `TicketServiceIT` |
| Void on event cancel | `TicketServiceIT` |

### Resale

| Behavior | Proof |
|----------|-------|
| Listing price capped by organizer policy | `ResaleServiceIT` |
| Platform commission deducted on sale | `ResaleServiceIT` |
| Seller can cancel active listing | `ResaleServiceIT` |

### Payment & Payout

| Behavior | Proof |
|----------|-------|
| Payment webhook confirms order | `PaymentControllerIT` |
| T+3 payout job calculates gross − refunds − commission | `PayoutServiceIT` |
| Refund issued on event cancellation | `PaymentControllerIT` |

---

## Pre-Commit Checklist

Before marking any plan `done` and presenting to developer for commit approval:

- [ ] Changed `api/` → `./mvnw compile -q` passes
- [ ] Changed `api/` → `./mvnw spring-boot:process-aot` passes
- [ ] Changed `api/` → `./mvnw verify` passes (Docker running)
- [ ] Changed `web/` → `npm run check` passes (0 new errors)
- [ ] Changed `web/` → `npm run lint` passes
- [ ] Relevant plan file status updated to `done`, Outcome filled
- [ ] No existing Flyway migration files edited
- [ ] No `.save()` calls inside seat/row creation loops
