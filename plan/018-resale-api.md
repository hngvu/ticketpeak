# 018 Resale API — Secondary Market with Anti-Scalping Enforcement

Status: done
Outcome: 
- Added resale configuration fields to Event and setup event cloning logic
- Added missing fields to CreateEventRequest and EventResponse
- Implemented ResaleListingStatus and ResaleListing (entity and repository)
- Implemented ResaleOrderStatus and ResaleOrder (entity and repository)
- Implemented ResaleListingService (CRUD for listings, check against limits and max markup)
- Implemented ResaleOrderService (initiate purchase, complete purchase with payout and ticket TOTP secret rotation)
- Implemented TicketVoidedListener to cancel listings when a ticket is voided
- Implemented ResaleReservationCleanupJob to release expired reservations
- Add ResaleServiceIT verifying all endpoints and features
- Ran tests and confirmed GraalVM native image compatibility

---

## Description

Implement the resale module — the secondary market where buyers can relist owned tickets for sale
to other buyers. The platform enforces anti-scalping by capping resale prices at a configurable
percentage above face value (set per event by the organizer). On a successful resale purchase,
the ticket is automatically transferred to the new buyer, the original seller receives a payout
(minus platform commission), and inventory is updated.

---

## Scope

| Feature | Included |
|---|---|
| Seller creates resale listing for an owned ticket | ✅ |
| Anti-scalping price cap (per event, organizer-configured) | ✅ |
| Public listing browser for an event | ✅ |
| Buyer purchases a resale listing (resale order) | ✅ |
| Platform commission deducted on sale | ✅ |
| Automatic ticket transfer to buyer after payment | ✅ |
| Seller cancels listing before purchase | ✅ |
| Listing lifecycle: `ACTIVE` → `SOLD` / `CANCELLED` | ✅ |
| Limit listings per user per event | ✅ |
| Payout creation on successful sale (hooks into plan 017) | ✅ |
| Refund on resale order cancellation | stub only |
| Dispute resolution / buyer protection | ❌ out of scope |
| Resale search / filtering by price or section | ❌ deferred to search module |

---

## Dependency Map

```
ticket  ──► resale listing  (ticket must be ISSUED, owned by seller)
payout  ──► resale          (PayoutService.createPayout called on sale)
payment ──► resale order    (payment intent + webhook)
event   ──► resale config   (resaleEnabled, resalePriceCapPercent, maxListingsPerUser)
```

---

## Prerequisites

1. `Event` entity needs 3 new fields: `resaleEnabled`, `resalePriceCapPercent`, `maxResaleListingsPerUser`.
2. `Ticket` already has `faceValue`, `currency`, `ticketTypeId`, `ticketTypeName`, `transferCount` — confirmed from plan 016 impl.
3. `PayoutService.createPayout(...)` callable from `ResaleOrderService` — confirmed from plan 017.
4. Seller must have an active primary payout method (`PayoutException::noPrimaryMethod` reused).

---

## Changes to Existing Modules

### 1. `Event` entity — resale config fields

```java
@Column(name = "resale_enabled", nullable = false)
boolean resaleEnabled;

@Column(name = "resale_price_cap_percent", precision = 5, scale = 2)
BigDecimal resalePriceCapPercent;    // nullable = no cap enforced

@Column(name = "max_resale_listings_per_user", nullable = false)
int maxResaleListingsPerUser;        // default 1
```

### 2. `PartnerEventController` — resale config endpoint

```
PUT /api/partner/events/{eventId}/resale-config
```

Request: `UpdateResaleConfigRequest(resaleEnabled, resalePriceCapPercent, maxResaleListingsPerUser)`

---

## Proposed Package Structure

```text
api/resale/
├── controller/
│   ├── ResaleListingController.java     # /api/resale — buyer & seller facing
│   └── PartnerResaleController.java     # /api/partner/events/{id}/resale-config — organizer
├── dto/
│   ├── CreateListingRequest.java
│   ├── ResaleListingResponse.java
│   ├── ResaleOrderResponse.java
│   └── UpdateResaleConfigRequest.java
├── event/
│   └── ResaleCompletedEvent.java
├── exception/
│   └── ResaleException.java
├── model/
│   ├── ResaleListing.java
│   ├── ResaleListingStatus.java
│   ├── ResaleOrder.java
│   └── ResaleOrderStatus.java
├── repository/
│   ├── ResaleListingRepository.java
│   └── ResaleOrderRepository.java
└── service/
    ├── ResaleListingService.java
    ├── ResaleOrderService.java
    └── ResaleReservationCleanupJob.java
```

---

## Domain Models

### `ResaleListingStatus`

```java
public enum ResaleListingStatus {
    ACTIVE,      // visible to buyers
    RESERVED,    // buyer initiated payment — held 15 min during payment window
    SOLD,        // terminal — ticket transferred to buyer
    CANCELLED    // terminal — seller cancelled
}
```

`RESERVED` prevents double-purchase: on "Buy Now", listing moves to `RESERVED` for 15 minutes.
If payment fails or times out, the cleanup job reverts it to `ACTIVE`.

### `ResaleListing` entity

| Column | Type | Notes |
|---|---|---|
| `id` | UUID (uuidv7) | PK |
| `ticket_id` | UUID | soft ref → ticket; partial UNIQUE where ACTIVE or RESERVED |
| `seller_id` | UUID | soft ref → account |
| `event_id` | UUID | for public browsing index |
| `offer_id` | UUID | snapshot |
| `ticket_type_id` | UUID | snapshot |
| `ticket_type_name` | VARCHAR(255) | snapshot — display without join |
| `seat_id` | VARCHAR(64) | nullable, RS only |
| `area_id` | VARCHAR(64) | nullable, GA only |
| `seating_mode` | VARCHAR(32) | |
| `face_value` | NUMERIC(19,2) | snapshot from ticket — used for cap calculation |
| `asking_price` | NUMERIC(19,2) | seller's price |
| `currency` | VARCHAR(3) | |
| `status` | VARCHAR(16) | ACTIVE/RESERVED/SOLD/CANCELLED |
| `reserved_until` | TIMESTAMPTZ | nullable; set on RESERVED |
| `version` | BIGINT | optimistic lock |
| `created_at` | TIMESTAMPTZ | |
| `updated_at` | TIMESTAMPTZ | |

### `ResaleOrderStatus`

```java
public enum ResaleOrderStatus {
    PENDING,     // payment initiated
    CONFIRMED,   // payment success — ticket transferred
    FAILED,      // payment failed — listing reverted to ACTIVE
    CANCELLED    // reservation expired or buyer cancelled
}
```

### `ResaleOrder` entity

| Column | Type | Notes |
|---|---|---|
| `id` | UUID (uuidv7) | PK |
| `resale_listing_id` | UUID | soft ref |
| `buyer_id` | UUID | soft ref |
| `payment_id` | UUID | nullable; set after payment intent created |
| `asking_price` | NUMERIC(19,2) | snapshot at purchase time |
| `platform_fee_percent` | NUMERIC(5,2) | snapshot |
| `platform_fee_amount` | NUMERIC(19,2) | |
| `net_amount` | NUMERIC(19,2) | seller receives |
| `currency` | VARCHAR(3) | |
| `status` | VARCHAR(16) | |
| `version` | BIGINT | |
| `created_at` / `updated_at` | TIMESTAMPTZ | |

---

## Services

### `ResaleListingService`

**`createListing(sellerId, req)` — validation order:**
1. Ticket owned by seller and status `ISSUED`
2. Event `resaleEnabled = true`
3. If `resalePriceCapPercent` set: `askingPrice <= faceValue * cap / 100`
4. Active listing count for seller in event < `maxResaleListingsPerUser`
5. No existing ACTIVE/RESERVED listing for this ticket (partial unique index is final guard)
6. Seller has active primary payout method

**`cancelListing(sellerId, listingId)`:**
- Only `ACTIVE` cancellable; `RESERVED` → `listingReserved()` (wait for payment to expire)

**`listActiveForEvent(eventId, pageable)`:** public; `ACTIVE` only, sorted `askingPrice ASC`

**`listMyListings(sellerId, pageable)`:** all statuses, sorted `createdAt DESC`

---

### `ResaleOrderService`

**`initiatePurchase(buyerId, listingId)`:**
- `findByIdForUpdate` (pessimistic lock) on listing
- Listing must be `ACTIVE`; buyer cannot be seller
- Set listing `RESERVED` + `reservedUntil = now + 15min`
- Calculate fees; build `ResaleOrder(PENDING)`
- Stub: payment intent creation (wired when payment module extends to resale)

**`onPaymentSuccess(resaleOrderId)`** — called by payment webhook:
- `findByIdForUpdate` on ticket → change `accountId` to buyer, increment `transferCount`
- Listing → `SOLD`; Order → `CONFIRMED`
- Call `payoutService.createPayout(listing.id, sellerId, netAmount, feePercent, currency, event.endAt, event.startAt)`
- Publish `ResaleCompletedEvent` for notification module

**`onPaymentFailure(resaleOrderId)`:**
- Order → `FAILED`
- Listing → `ACTIVE`, clear `reservedUntil`

---

### `ResaleReservationCleanupJob`

Runs every 5 minutes. Finds `RESERVED` listings where `reservedUntil < now()`, reverts to
`ACTIVE`, cancels associated `PENDING` orders.

```java
@Scheduled(fixedDelay = 300_000)
@Transactional
public void releaseExpiredReservations() {
    List<ResaleListing> expired = resaleListingRepository
        .findByStatusAndReservedUntilBefore(ResaleListingStatus.RESERVED, Instant.now());

    for (ResaleListing listing : expired) {
        listing.setStatus(ResaleListingStatus.ACTIVE);
        listing.setReservedUntil(null);
        resaleListingRepository.save(listing);

        resaleOrderRepository
            .findByResaleListingIdAndStatus(listing.getId(), ResaleOrderStatus.PENDING)
            .ifPresent(o -> {
                o.setStatus(ResaleOrderStatus.CANCELLED);
                resaleOrderRepository.save(o);
            });
    }
}
```

---

## Endpoints

```
// Buyer & seller
GET    /api/resale/listings?eventId={id}           → public active listings (price asc)
GET    /api/resale/listings/my                     → seller's own listings
POST   /api/resale/listings                        → create listing
DELETE /api/resale/listings/{listingId}            → seller cancels listing
POST   /api/resale/listings/{listingId}/purchase   → buyer initiates purchase

// Organizer
GET    /api/partner/events/{eventId}/resale-config → current resale config
PUT    /api/partner/events/{eventId}/resale-config → update config
```

---

## `ResaleException`

| Method | HTTP | Code |
|---|---|---|
| `ticketNotFound()` | 404 | RESALE_TICKET_NOT_FOUND |
| `ticketNotListable()` | 409 | RESALE_TICKET_NOT_LISTABLE |
| `resaleNotEnabled()` | 403 | RESALE_NOT_ENABLED |
| `priceExceedsCap(cap, currency)` | 422 | RESALE_PRICE_EXCEEDS_CAP |
| `listingLimitExceeded(limit)` | 409 | RESALE_LISTING_LIMIT_EXCEEDED |
| `alreadyListed()` | 409 | RESALE_ALREADY_LISTED |
| `listingNotFound()` | 404 | RESALE_LISTING_NOT_FOUND |
| `listingNotAvailable()` | 409 | RESALE_LISTING_NOT_AVAILABLE |
| `listingReserved()` | 409 | RESALE_LISTING_RESERVED |
| `listingNotCancellable()` | 409 | RESALE_LISTING_NOT_CANCELLABLE |
| `cannotBuyOwnListing()` | 409 | RESALE_CANNOT_BUY_OWN |
| `orderNotFound()` | 404 | RESALE_ORDER_NOT_FOUND |
| `eventNotFound()` | 404 | RESALE_EVENT_NOT_FOUND |

---

## DTOs

```java
// CreateListingRequest.java
public record CreateListingRequest(
    @NotNull UUID ticketId,
    @NotNull @DecimalMin("0.01") BigDecimal askingPrice
) {}

// ResaleListingResponse.java
public record ResaleListingResponse(
    UUID id, UUID ticketId, UUID sellerId, UUID eventId,
    String ticketTypeName, String seatingMode,
    String areaId, String seatId,
    BigDecimal faceValue, BigDecimal askingPrice, String currency,
    ResaleListingStatus status, Instant createdAt
) {
    public static ResaleListingResponse from(ResaleListing l) { ... }
}

// ResaleOrderResponse.java
public record ResaleOrderResponse(
    UUID orderId, UUID listingId,
    BigDecimal askingPrice, BigDecimal platformFeeAmount, BigDecimal netAmount,
    String currency, ResaleOrderStatus status, Instant createdAt
) {
    public static ResaleOrderResponse from(ResaleOrder o, ResaleListing l) { ... }
}

// UpdateResaleConfigRequest.java
public record UpdateResaleConfigRequest(
    @NotNull Boolean resaleEnabled,
    @DecimalMin("100.0") BigDecimal resalePriceCapPercent,  // nullable = no cap
    @NotNull @Min(1) Integer maxResaleListingsPerUser
) {}
```

---

## Repositories

```java
// ResaleListingRepository
Optional<ResaleListing> findActiveByTicketId(UUID ticketId);   // WHERE status IN ('ACTIVE','RESERVED')

long countBySellerIdAndEventIdAndStatusIn(UUID sellerId, UUID eventId, List<ResaleListingStatus> statuses);

Page<ResaleListing> findByEventIdAndStatusOrderByAskingPriceAsc(UUID eventId, ResaleListingStatus status, Pageable p);

Page<ResaleListing> findBySellerIdOrderByCreatedAtDesc(UUID sellerId, Pageable p);

List<ResaleListing> findByStatusAndReservedUntilBefore(ResaleListingStatus status, Instant before);

@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT r FROM ResaleListing r WHERE r.id = :id")
Optional<ResaleListing> findByIdForUpdate(UUID id);

// ResaleOrderRepository
Optional<ResaleOrder> findByResaleListingIdAndStatus(UUID listingId, ResaleOrderStatus status);
```

---

## Database Migrations

### `V30__add_resale_config_to_event.sql`

```sql
ALTER TABLE event
    ADD COLUMN resale_enabled                BOOLEAN      NOT NULL DEFAULT FALSE,
    ADD COLUMN resale_price_cap_percent      NUMERIC(5,2),
    ADD COLUMN max_resale_listings_per_user  INT          NOT NULL DEFAULT 1;
```

### `V31__create_resale_schema.sql`

```sql
CREATE TABLE resale_listing (
    id                UUID            NOT NULL DEFAULT uuidv7() PRIMARY KEY,
    ticket_id         UUID            NOT NULL,
    seller_id         UUID            NOT NULL,
    event_id          UUID            NOT NULL,
    offer_id          UUID            NOT NULL,
    ticket_type_id    UUID            NOT NULL,
    ticket_type_name  VARCHAR(255)    NOT NULL,
    seat_id           VARCHAR(64),
    area_id           VARCHAR(64),
    seating_mode      VARCHAR(32)     NOT NULL,
    face_value        NUMERIC(19,2)   NOT NULL,
    asking_price      NUMERIC(19,2)   NOT NULL,
    currency          VARCHAR(3)      NOT NULL,
    status            VARCHAR(16)     NOT NULL DEFAULT 'ACTIVE',
    reserved_until    TIMESTAMPTZ,
    version           BIGINT          NOT NULL DEFAULT 0,
    created_at        TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ     NOT NULL DEFAULT now(),
    CONSTRAINT chk_resale_listing_status
        CHECK (status IN ('ACTIVE','RESERVED','SOLD','CANCELLED')),
    CONSTRAINT chk_resale_listing_seating_mode
        CHECK (seating_mode IN ('GENERAL_ADMISSION','RESERVED_SEATING'))
);

-- Only one ACTIVE/RESERVED listing per ticket at a time
CREATE UNIQUE INDEX uq_resale_listing_ticket_active
    ON resale_listing(ticket_id)
    WHERE status IN ('ACTIVE', 'RESERVED');

CREATE INDEX idx_resale_listing_event_status ON resale_listing(event_id, status);
CREATE INDEX idx_resale_listing_seller       ON resale_listing(seller_id);
CREATE INDEX idx_resale_listing_ticket       ON resale_listing(ticket_id);

CREATE TABLE resale_order (
    id                    UUID          NOT NULL DEFAULT uuidv7() PRIMARY KEY,
    resale_listing_id     UUID          NOT NULL,
    buyer_id              UUID          NOT NULL,
    payment_id            UUID,
    asking_price          NUMERIC(19,2) NOT NULL,
    platform_fee_percent  NUMERIC(5,2)  NOT NULL,
    platform_fee_amount   NUMERIC(19,2) NOT NULL,
    net_amount            NUMERIC(19,2) NOT NULL,
    currency              VARCHAR(3)    NOT NULL,
    status                VARCHAR(16)   NOT NULL DEFAULT 'PENDING',
    version               BIGINT        NOT NULL DEFAULT 0,
    created_at            TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT chk_resale_order_status
        CHECK (status IN ('PENDING','CONFIRMED','FAILED','CANCELLED'))
);

CREATE INDEX idx_resale_order_listing ON resale_order(resale_listing_id);
CREATE INDEX idx_resale_order_buyer   ON resale_order(buyer_id);
```

---

## Concurrency Design

### Double-purchase prevention
`findByIdForUpdate` (`SELECT FOR UPDATE`) on listing. First winner sets `RESERVED`;
second sees `status != ACTIVE` → 409 `RESALE_LISTING_NOT_AVAILABLE`.

### Partial unique index as final guard
`uq_resale_listing_ticket_active` prevents two ACTIVE/RESERVED rows for the same ticket
even if application-level check is bypassed under concurrent load. DB constraint wins.

---

## Acceptance Criteria

| # | Criterion |
|---|---|
| 1 | Price above cap → 422 `RESALE_PRICE_EXCEEDS_CAP` with cap amount in message. |
| 2 | Resale disabled on event → 403 `RESALE_NOT_ENABLED`. |
| 3 | Ticket not ISSUED → 409 `RESALE_TICKET_NOT_LISTABLE`. |
| 4 | Listing limit exceeded → 409 `RESALE_LISTING_LIMIT_EXCEEDED`. |
| 5 | Duplicate active listing for same ticket → 409 (partial unique index). |
| 6 | No primary payout method → 422 `PAYOUT_NO_PRIMARY_METHOD`. |
| 7 | Two concurrent "Buy Now" → exactly one succeeds, one gets 409. |
| 8 | Payment success → ticket owner = buyer, listing SOLD, payout created for seller. |
| 9 | Payment failure → listing reverts to ACTIVE, order FAILED. |
| 10 | Reservation expired (> 15 min) → cleanup job reverts listing to ACTIVE, order CANCELLED. |
| 11 | Buyer cannot purchase own listing → 409 `RESALE_CANNOT_BUY_OWN`. |
| 12 | `GET /api/resale/listings?eventId=...` returns only ACTIVE, sorted price asc. |
| 13 | `./mvnw verify` passes. |

---

## Integration Test Coverage — `ResaleServiceIT.java`

```text
create_listing_happy_path
create_listing_price_cap_rejected
create_listing_resale_disabled_rejected
create_listing_ticket_not_issued_rejected
create_listing_limit_exceeded_rejected
create_listing_no_primary_payout_rejected
create_listing_duplicate_rejected
cancel_listing_happy_path
cancel_listing_reserved_rejected
purchase_happy_path_transfers_ticket_and_creates_payout
purchase_concurrent_only_one_succeeds
purchase_own_listing_rejected
payment_failure_reverts_listing_to_active
expired_reservation_cleanup_reverts_to_active
```

---

## Open Questions / Deferred

| Item | Decision |
|---|---|
| Payment gateway integration for resale order | Stub in `initiatePurchase`; wired when payment module extends |
| Buyer refund on confirmed resale | Stub only — full dispute flow is Phase 6 |
| Resale listing search / filter by section, price range | Deferred to search module (Phase 5) |
| Notify buyer when SOLD-OUT event gets new listing | Deferred to notification module |
| `Ticket.transferCount` cap enforcement per event | Tracked but not enforced; organizer config deferred |

---

## Verification Plan

```powershell
cd api
./mvnw compile -q
./mvnw test -Dtest=ResaleServiceIT
./mvnw spring-boot:process-aot
./mvnw verify
```
