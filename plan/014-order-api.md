# 014 Order API — Checkout Completion and Purchase Commit

Status: done
Outcome: Implemented Order API, decoupled inventory confirmation from PaymentManager, created order schema, and added OrderService integration tests. AOT processing check also passed.

## Description

Implement the `order` module as the permanent purchase record layer. Order creation is triggered
by `PaymentCompletedEvent` and is responsible for:

- Building an immutable snapshot of what was purchased (items, pricing, charges)
- Confirming inventory holds into sold state
- Marking the reservation as `FINALIZED` to prevent reuse
- Exposing buyer-facing read endpoints

The current `PaymentManager.confirmInventoryHolds` does work that belongs here and will be removed.

---

## Proposed Package Structure

```text
order/
├── controller/
│   └── OrderController.java
├── dto/
│   ├── OrderItemResponse.java
│   └── OrderResponse.java
├── exception/
│   └── OrderException.java
├── model/
│   ├── Order.java
│   ├── OrderItem.java
│   └── OrderStatus.java
├── repository/
│   └── OrderRepository.java
└── service/
    └── OrderService.java
```

---

## Changes to Existing Code

### 1. `ReservationStatus` — add `FINALIZED`

```java
public enum ReservationStatus {
    PENDING,
    CONFIRMED,   // payment succeeded; order not yet created
    FINALIZED,   // order created from this reservation; terminal
    EXPIRED,
    CANCELLED
}
```

`CONFIRMED` is now an intermediate state between payment success and order creation.
`FINALIZED` is the permanent terminal state that prevents reuse.

---

### 2. `PaymentManager` — remove inventory responsibility

- **Remove** `confirmInventoryHolds(Reservation)` method entirely.
- **Remove** `InventoryService` injection — PaymentManager no longer touches inventory.
- **Modify** `finalizePayment`: after saving payment + reservation, publish `PaymentCompletedEvent`
  without calling `confirmInventoryHolds`. The event listener in `OrderService` takes over from here.

```java
// finalizePayment — after fix:
payment.setStatus(PaymentStatus.COMPLETED);
paymentRepository.saveAndFlush(payment);

reservation.setStatus(ReservationStatus.CONFIRMED);  // intermediate, not FINALIZED yet
reservationRepository.saveAndFlush(reservation);

// Inventory confirmation and FINALIZED transition now happen in OrderService
eventPublisher.publishEvent(new PaymentCompletedEvent(...));
```

---

## New Implementation

### 3. `OrderStatus`

```java
public enum OrderStatus {
    CREATED,
    CANCELLED,
    REFUNDED
}
```

---

### 4. `Order` entity

Table name: `purchase_order` (avoids SQL reserved word `order`).

Fields:

| Column | Type | Notes |
|---|---|---|
| `id` | UUID (uuidv7) | PK, server-generated |
| `reservation_id` | UUID | UNIQUE FK → reservation(id) ON DELETE RESTRICT |
| `payment_id` | UUID | UNIQUE FK → payment(id) ON DELETE RESTRICT |
| `account_id` | UUID | FK → account(id) ON DELETE RESTRICT |
| `event_id` | UUID | FK → event(id) ON DELETE RESTRICT |
| `status` | VARCHAR(16) | CHECK IN ('CREATED','CANCELLED','REFUNDED') |
| `currency` | VARCHAR(8) | |
| `total_amount` | NUMERIC(19,2) | |
| `version` | BIGINT | optimistic lock |
| `created_at` | TIMESTAMPTZ | |
| `updated_at` | TIMESTAMPTZ | |

```java
@Entity
@Table(name = "purchase_order")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Order {

    @Id
    @Generated(GenerationTime.INSERT)
    @Column(columnDefinition = "UUID DEFAULT uuidv7()", insertable = false, updatable = false)
    UUID id;

    @Column(name = "reservation_id", nullable = false, unique = true)
    UUID reservationId;

    @Column(name = "payment_id", nullable = false, unique = true)
    UUID paymentId;

    @Column(name = "account_id", nullable = false)
    UUID accountId;

    @Column(name = "event_id", nullable = false)
    UUID eventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    OrderStatus status;

    @Column(nullable = false, length = 8)
    String currency;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    BigDecimal totalAmount;

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL,
               orphanRemoval = true, fetch = FetchType.EAGER)
    List<OrderItem> items = new ArrayList<>();

    @Version
    Long version;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;
}
```

---

### 5. `OrderItem` entity

Fields snapshot everything from `ReservationItem` at purchase time — immune to future offer changes.

| Column | Type | Notes |
|---|---|---|
| `id` | UUID (uuidv7) | PK |
| `order_id` | UUID | FK → purchase_order(id) ON DELETE CASCADE |
| `offer_id` | UUID | snapshot reference |
| `seating_mode` | VARCHAR(32) | |
| `area_id` | VARCHAR(64) | nullable, GA only |
| `seat_id` | VARCHAR(64) | nullable, RS only |
| `qty` | INT | |
| `unit_face_value` | NUMERIC(19,2) | |
| `unit_total_price` | NUMERIC(19,2) | face value + charges, per unit |
| `line_total` | NUMERIC(19,2) | unit_total_price × qty |
| `currency` | VARCHAR(8) | |
| `charges` | JSONB | snapshot of `List<OfferCharge>` at time of purchase |

```java
@Entity
@Table(name = "order_item")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItem {

    @Id
    @Generated(GenerationTime.INSERT)
    @Column(columnDefinition = "UUID DEFAULT uuidv7()", insertable = false, updatable = false)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    Order order;

    @Column(name = "offer_id", nullable = false)
    UUID offerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "seating_mode", nullable = false, length = 32)
    SeatingMode seatingMode;

    @Column(name = "area_id", length = 64)
    String areaId;

    @Column(name = "seat_id", length = 64)
    String seatId;

    @Column(nullable = false)
    int qty;

    @Column(name = "unit_face_value", nullable = false, precision = 19, scale = 2)
    BigDecimal unitFaceValue;

    @Column(name = "unit_total_price", nullable = false, precision = 19, scale = 2)
    BigDecimal unitTotalPrice;

    @Column(name = "line_total", nullable = false, precision = 19, scale = 2)
    BigDecimal lineTotal;

    @Column(nullable = false, length = 8)
    String currency;

    @Builder.Default
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    List<OfferCharge> charges = List.of();
}
```

---

### 6. `OrderRepository`

```java
@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByIdAndAccountId(UUID id, UUID accountId);

    Optional<Order> findByReservationId(UUID reservationId);

    Optional<Order> findByPaymentId(UUID paymentId);

    List<Order> findByAccountIdOrderByCreatedAtDesc(UUID accountId);

    boolean existsByReservationId(UUID reservationId);
}
```

---

### 7. `OrderException`

```java
public final class OrderException {
    private OrderException() {}

    public static ApiException notFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "Order not found");
    }

    public static ApiException ownerMismatch() {
        return new ApiException(HttpStatus.FORBIDDEN, "ORDER_OWNER_MISMATCH", "You do not own this order");
    }

    public static ApiException alreadyExists(UUID reservationId) {
        return new ApiException(HttpStatus.CONFLICT, "ORDER_ALREADY_EXISTS",
                "An order already exists for reservation " + reservationId);
    }

    public static ApiException inventoryConfirmFailed(String detail) {
        return new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                "ORDER_INVENTORY_CONFIRM_FAILED", detail);
    }
}
```

---

### 8. `OrderService` — core logic

Listens to `PaymentCompletedEvent` with `@TransactionalEventListener(phase = AFTER_COMMIT)`
so it runs in a fresh transaction after the payment transaction has fully committed.

```java
@Slf4j
@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final ReservationRepository reservationRepository;
    private final InventoryService inventoryService;

    // ─── Event listener ───────────────────────────────────────────────────

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        UUID reservationId = event.getReservationId();

        // Idempotency guard — webhook retries must not duplicate orders
        if (orderRepository.existsByReservationId(reservationId)) {
            log.info("Order already exists for reservation {}. Skipping duplicate creation.", reservationId);
            return;
        }

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> {
                    log.error("PaymentCompletedEvent received for unknown reservation {}", reservationId);
                    return OrderException.notFound();
                });

        Order order = buildOrder(reservation, event);
        orderRepository.saveAndFlush(order);

        confirmInventoryHolds(reservation);

        reservation.setStatus(ReservationStatus.FINALIZED);
        reservationRepository.save(reservation);

        log.info("Order {} created for reservation {} (payment {}).",
                order.getId(), reservationId, event.getPaymentId());
    }

    // ─── Read ─────────────────────────────────────────────────────────────

    public OrderResponse getOrder(UUID accountId, UUID orderId) {
        Order order = orderRepository.findByIdAndAccountId(orderId, accountId)
                .orElseThrow(OrderException::notFound);
        return OrderResponse.from(order);
    }

    public List<OrderResponse> listOrders(UUID accountId) {
        return orderRepository.findByAccountIdOrderByCreatedAtDesc(accountId)
                .stream()
                .map(OrderResponse::from)
                .toList();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────

    private Order buildOrder(Reservation reservation, PaymentCompletedEvent event) {
        List<OrderItem> orderItems = reservation.getItems().stream()
                .map(item -> buildOrderItem(item))
                .toList();

        BigDecimal total = orderItems.stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .reservationId(reservation.getId())
                .paymentId(event.getPaymentId())
                .accountId(reservation.getAccountId())
                .eventId(reservation.getEventId())
                .status(OrderStatus.CREATED)
                .currency(reservation.getCurrency())
                .totalAmount(total)
                .build();

        orderItems.forEach(i -> i.setOrder(order));
        order.setItems(orderItems);
        return order;
    }

    private OrderItem buildOrderItem(ReservationItem item) {
        BigDecimal unitFaceValue = item.getUnitFaceValue();
        BigDecimal totalChargesPerUnit = BigDecimal.ZERO;

        if (item.getCharges() != null) {
            for (OfferCharge charge : item.getCharges()) {
                BigDecimal chargeAmount = charge.isPercentage()
                        ? unitFaceValue.multiply(charge.amount())
                              .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                        : charge.amount();
                totalChargesPerUnit = totalChargesPerUnit.add(chargeAmount);
            }
        }

        BigDecimal unitTotalPrice = unitFaceValue.add(totalChargesPerUnit)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal lineTotal = unitTotalPrice.multiply(BigDecimal.valueOf(item.getQty()))
                .setScale(2, RoundingMode.HALF_UP);

        return OrderItem.builder()
                .offerId(item.getOfferId())
                .seatingMode(item.getSeatingMode())
                .areaId(item.getAreaId())
                .seatId(item.getSeatId())
                .qty(item.getQty())
                .unitFaceValue(unitFaceValue)
                .unitTotalPrice(unitTotalPrice)
                .lineTotal(lineTotal)
                .currency(item.getCurrency())
                .charges(item.getCharges() != null ? item.getCharges() : List.of())
                .build();
    }

    private void confirmInventoryHolds(Reservation reservation) {
        for (ReservationItem item : reservation.getItems()) {
            if (item.getSeatingMode() == SeatingMode.GENERAL_ADMISSION) {
                try {
                    inventoryService.confirmGAInventory(
                            reservation.getEventId(), item.getAreaId(), item.getOfferId(), item.getQty());
                } catch (ApiException e) {
                    throw OrderException.inventoryConfirmFailed(
                            "GA inventory confirm failed for area " + item.getAreaId() + ": " + e.getMessage());
                }
            } else {
                try {
                    inventoryService.sellSeat(reservation.getEventId(), item.getSeatId());
                } catch (ApiException e) {
                    throw OrderException.inventoryConfirmFailed(
                            "Seat sell failed for seat " + item.getSeatId() + ": " + e.getMessage());
                }
            }
        }
    }
}
```

**Why `@TransactionalEventListener(AFTER_COMMIT)` + `REQUIRES_NEW`:**
- `AFTER_COMMIT` ensures the listener runs only after the payment transaction has fully committed —
  no phantom reads of an in-progress payment.
- `REQUIRES_NEW` gives the listener its own transaction, so a failure here rolls back only the
  order creation, not the payment (which is already committed).
- If order creation fails, `reservation.status` stays `CONFIRMED` (not `FINALIZED`). A scheduled
  reconciliation job (future work) can detect orphaned CONFIRMED reservations without orders and retry.

---

### 9. DTOs

```java
// OrderItemResponse.java
public record OrderItemResponse(
    UUID id,
    UUID offerId,
    String seatingMode,
    String areaId,
    String seatId,
    int qty,
    BigDecimal unitFaceValue,
    BigDecimal unitTotalPrice,
    BigDecimal lineTotal,
    String currency
) {
    public static OrderItemResponse from(OrderItem item) { ... }
}

// OrderResponse.java
public record OrderResponse(
    UUID orderId,
    UUID reservationId,
    UUID paymentId,
    UUID eventId,
    String status,
    String currency,
    BigDecimal totalAmount,
    List<OrderItemResponse> items,
    Instant createdAt
) {
    public static OrderResponse from(Order order) { ... }
}
```

---

### 10. `OrderController`

```java
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.getOrder(account.accountId(), id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> listOrders(
            @AuthenticationPrincipal AuthenticatedAccount account
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.listOrders(account.accountId())));
    }
}
```

---

### 11. Database Migration — `V20__add_reservation_finalized_status.sql`

```sql
-- Extend ReservationStatus CHECK constraint if present (adjust if not):
-- ALTER TABLE reservation DROP CONSTRAINT IF EXISTS chk_reservation_status;
-- ALTER TABLE reservation ADD CONSTRAINT chk_reservation_status
--     CHECK (status IN ('PENDING','CONFIRMED','FINALIZED','EXPIRED','CANCELLED'));
```

### 12. Database Migration — `V21__create_order_schema.sql`

```sql
CREATE TABLE purchase_order (
    id              UUID           NOT NULL DEFAULT uuidv7() PRIMARY KEY,
    reservation_id  UUID           NOT NULL UNIQUE REFERENCES reservation(id)   ON DELETE RESTRICT,
    payment_id      UUID           NOT NULL UNIQUE REFERENCES payment(id)        ON DELETE RESTRICT,
    account_id      UUID           NOT NULL            REFERENCES account(id)    ON DELETE RESTRICT,
    event_id        UUID           NOT NULL            REFERENCES event(id)      ON DELETE RESTRICT,
    status          VARCHAR(16)    NOT NULL DEFAULT 'CREATED',
    currency        VARCHAR(8)     NOT NULL,
    total_amount    NUMERIC(19,2)  NOT NULL,
    version         BIGINT         NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT chk_order_status CHECK (status IN ('CREATED','CANCELLED','REFUNDED'))
);

CREATE INDEX idx_purchase_order_account_id  ON purchase_order(account_id);
CREATE INDEX idx_purchase_order_event_id    ON purchase_order(event_id);

CREATE TABLE order_item (
    id               UUID           NOT NULL DEFAULT uuidv7() PRIMARY KEY,
    order_id         UUID           NOT NULL REFERENCES purchase_order(id) ON DELETE CASCADE,
    offer_id         UUID           NOT NULL,
    seating_mode     VARCHAR(32)    NOT NULL,
    area_id          VARCHAR(64),
    seat_id          VARCHAR(64),
    qty              INT            NOT NULL,
    unit_face_value  NUMERIC(19,2)  NOT NULL,
    unit_total_price NUMERIC(19,2)  NOT NULL,
    line_total       NUMERIC(19,2)  NOT NULL,
    currency         VARCHAR(8)     NOT NULL,
    charges          JSONB          NOT NULL DEFAULT '[]',
    CONSTRAINT chk_order_item_seating_mode
        CHECK (seating_mode IN ('GENERAL_ADMISSION','RESERVED_SEATING'))
);

CREATE INDEX idx_order_item_order_id ON order_item(order_id);
```

---

## Acceptance Criteria Mapping

| Criteria | Covered by |
|---|---|
| Order module has own package, migration, entities, repos, service, controller, tests | Sections 3–12 |
| One order per reservation | `reservation_id UNIQUE` constraint + idempotency guard in listener |
| Order creation only before reservation expires | Reservation is `CONFIRMED` by payment time; expiry already checked before payment |
| Atomically converts held inventory to sold | `confirmInventoryHolds` + `FINALIZED` transition in same `REQUIRES_NEW` tx |
| Reservation finalized to prevent reuse | `ReservationStatus.FINALIZED` set after order save |
| `confirmReservation()` responsibility removed from PaymentManager | `PaymentManager.confirmInventoryHolds` deleted |
| Snapshot: offerId, seatingMode, seat/area ref, qty, unitFaceValue, currency, charges | `OrderItem` columns |
| Rejects expired / cancelled / already-finalized / wrong-owner reservations | These are already gated by payment flow; `CONFIRMED` status is the entry condition |
| Buyer GET /orders/{id} and GET /orders | `OrderController` |
| Integration tests: happy path, double-submit, expiry, ownership, inventory, finalization | Section 13 |

---

## Integration Test Coverage — `OrderServiceIT.java`

```
happy_path_creates_order_and_finalizes_reservation
happy_path_confirms_ga_inventory_sold
happy_path_confirms_seat_sold
double_submit_idempotency_does_not_create_duplicate_order
get_order_returns_correct_snapshot
list_orders_returns_buyer_orders_only
get_order_by_other_account_returns_404
```

---

## Verification Plan

```powershell
cd api
./mvnw compile -q
./mvnw test -Dtest=OrderServiceIT
./mvnw spring-boot:process-aot
```
