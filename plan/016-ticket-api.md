# 015 Ticket API — Issuance, TOTP QR, Check-in, Transfer, and Void

Status: done

---

## Description

Implement the `ticket` module — the core value of the platform after a purchase completes.

Each `OrderItem` in a confirmed order maps to one or more `Ticket` entities. Every ticket carries an
encrypted TOTP secret; the buyer's ticket screen derives a QR code from the current TOTP window
on-demand, rotating every 30 seconds. Gate staff validate the QR at scan time — no screenshot
replay, no duplicates.

The module also supports ticket **transfer** (buyer → another account) and **void** (on refund).

---

## Scope

| Feature | Included |
|---|---|
| Issue tickets from `OrderCreatedEvent` | ✅ |
| TOTP secret generation + AES-256 encryption at rest | ✅ |
| QR generation endpoint (returns current TOTP window, never stored) | ✅ |
| Buyer ticket list / detail | ✅ |
| Check-in: TOTP validation + idempotency | ✅ |
| Ticket transfer (initiate / accept / cancel) | ✅ |
| Void ticket on order refund | ✅ |
| Resale integration (stub hook) | stub only |
| PDF / Apple Wallet pass generation | ❌ out of scope |

---

## Dependency Map

```
order (CREATED status) ──► ticket issuance
account                ──► buyer ownership + transfer target
payment (REFUNDED)     ──► void trigger
```

---

## Proposed Package Structure

```text
api/ticket/
├── controller/
│   ├── TicketController.java          # /api/tickets — buyer-facing
│   └── CheckInController.java         # /api/partner/events/{eventId}/check-in — gate staff
├── dto/
│   ├── TicketResponse.java
│   ├── TicketQrResponse.java
│   ├── CheckInRequest.java
│   ├── CheckInResponse.java
│   ├── InitiateTransferRequest.java
│   └── TransferResponse.java
├── event/
│   └── OrderCreatedEvent.java         # published by OrderService after save
├── exception/
│   └── TicketException.java
├── model/
│   ├── Ticket.java
│   ├── TicketStatus.java
│   ├── TicketTransfer.java
│   └── TicketTransferStatus.java
├── repository/
│   ├── TicketRepository.java
│   └── TicketTransferRepository.java
└── service/
    ├── TicketIssuanceService.java     # listens to OrderCreatedEvent
    ├── TicketService.java             # buyer reads, QR, transfer
    ├── CheckInService.java            # gate staff check-in
    └── TotpService.java               # TOTP generation + verification
```

---

## Changes to Existing Code

### 1. `OrderStatus` — add `CONFIRMED`

The current `OrderStatus` enum only has `CREATED, FAILED, CANCELLED, REFUNDED`.
Rename `CREATED` → `CONFIRMED` to better reflect semantics (tickets have been issued, order is confirmed).

> **Migration note:** the DB `CHECK` constraint and any existing data must be migrated.

```java
// order/model/OrderStatus.java
public enum OrderStatus {
    CONFIRMED,   // renamed from CREATED — tickets issued
    FAILED,
    CANCELLED,
    REFUNDED
}
```

Update `OrderService.buildOrder(...)` to set `OrderStatus.CONFIRMED`.
Update `OrderReconciliationService` to stay on `FAILED`.

---

### 2. `OrderService` — publish `OrderCreatedEvent` after save

After `orderRepository.save(order)` succeeds, publish a Spring application event so
`TicketIssuanceService` can listen in its own transaction.

```java
// inside onPaymentCompleted(...), after orderRepository.save(order):
eventPublisher.publishEvent(new OrderCreatedEvent(
    this,
    order.getId(),
    order.getAccountId(),
    order.getEventId(),
    order.getItems()   // pass the item list snapshot
));
```

`OrderCreatedEvent` must be published **before** the `REQUIRES_NEW` transaction commits so that
`@TransactionalEventListener(AFTER_COMMIT)` in `TicketIssuanceService` fires after the order row
is visible to other transactions.

---

### 3. `PaymentService` / `PaymentWebhookHandler` — void hook (new)

When a payment is refunded, publish a `PaymentRefundedEvent` that `TicketService` listens to for
voiding tickets. This is a stub interface for now — full refund flow is in the `payment` module plan.

```java
// payment/event/PaymentRefundedEvent.java  (new file)
public record PaymentRefundedEvent(UUID orderId, UUID accountId) {}
```

---

## New Implementation

### 4. `TicketStatus`

```java
public enum TicketStatus {
    ISSUED,          // freshly created, ready to use
    TRANSFERRED,     // pending acceptance by recipient
    CHECKED_IN,      // used at gate — terminal
    VOID             // cancelled / refunded — terminal
}
```

---

### 5. `Ticket` entity

One `Ticket` row per **unit** (i.e., per qty=1 slot in an `OrderItem`).
If an `OrderItem` has `qty = 3`, `TicketIssuanceService` creates 3 `Ticket` rows.

| Column | Type | Notes |
|---|---|---|
| `id` | UUID (uuidv7) | PK |
| `order_id` | UUID | FK → purchase_order(id) ON DELETE RESTRICT |
| `order_item_id` | UUID | FK → order_item(id) ON DELETE RESTRICT |
| `account_id` | UUID | current owner; updates on transfer |
| `event_id` | UUID | FK → event(id) |
| `offer_id` | UUID | snapshot |
| `seating_mode` | VARCHAR(32) | GA or RESERVED_SEATING |
| `area_id` | VARCHAR(64) | nullable, GA only |
| `seat_id` | VARCHAR(64) | nullable, RS only — the specific seat |
| `totp_secret_enc` | TEXT NOT NULL | AES-256-GCM encrypted TOTP secret (base32) |
| `status` | VARCHAR(16) | ISSUED / TRANSFERRED / CHECKED_IN / VOID |
| `checked_in_at` | TIMESTAMPTZ | nullable |
| `version` | BIGINT | optimistic lock |
| `created_at` | TIMESTAMPTZ | |
| `updated_at` | TIMESTAMPTZ | |

```java
@Entity
@Table(name = "ticket")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Ticket {

    @Id
    @Generated(GenerationTime.INSERT)
    @Column(columnDefinition = "UUID DEFAULT uuidv7()", insertable = false, updatable = false)
    UUID id;

    @Column(name = "order_id", nullable = false)
    UUID orderId;

    @Column(name = "order_item_id", nullable = false)
    UUID orderItemId;

    @Column(name = "account_id", nullable = false)
    UUID accountId;          // mutable — changes on transfer

    @Column(name = "event_id", nullable = false)
    UUID eventId;

    @Column(name = "offer_id", nullable = false)
    UUID offerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "seating_mode", nullable = false, length = 32)
    SeatingMode seatingMode;

    @Column(name = "area_id", length = 64)
    String areaId;

    @Column(name = "seat_id", length = 64)
    String seatId;

    @Column(name = "totp_secret_enc", nullable = false, columnDefinition = "TEXT")
    String totpSecretEnc;    // never returned to client; used only by TotpService

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    TicketStatus status;

    @Column(name = "checked_in_at")
    Instant checkedInAt;

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

**`totpSecretEnc` must never appear in any DTO or response.**

---

### 6. `TicketTransfer` entity

| Column | Type | Notes |
|---|---|---|
| `id` | UUID (uuidv7) | PK |
| `ticket_id` | UUID | FK → ticket(id) |
| `sender_id` | UUID | |
| `recipient_id` | UUID | account being transferred to |
| `status` | VARCHAR(16) | PENDING / ACCEPTED / CANCELLED |
| `expires_at` | TIMESTAMPTZ | auto-expires if not accepted |
| `created_at` | TIMESTAMPTZ | |
| `updated_at` | TIMESTAMPTZ | |

```java
public enum TicketTransferStatus {
    PENDING,
    ACCEPTED,
    CANCELLED
}
```

---

### 7. `TotpService`

Responsibilities: generate secrets, encrypt/decrypt, compute current TOTP OTP, verify OTPs.

```java
@Service
public class TotpService {

    // ── Secret lifecycle ──────────────────────────────────────────────────

    /** Generate a random 20-byte (160-bit) base32 TOTP secret. */
    public String generateSecret() { ... }  // SecureRandom → base32

    /** Encrypt the raw secret with AES-256-GCM using the platform key. */
    public String encrypt(String rawSecret) { ... }

    /** Decrypt stored ciphertext back to raw secret. */
    private String decrypt(String ciphertext) { ... }

    // ── OTP operations ────────────────────────────────────────────────────

    /**
     * Compute the 6-digit TOTP OTP for the current 30-second window.
     * Never stored — called at the moment of QR generation.
     */
    public String generateOtp(String totpSecretEnc) { ... }

    /**
     * Verify an OTP from a scanner against the ticket's secret.
     * Accepts current window and ±1 window for clock skew.
     */
    public boolean verifyOtp(String totpSecretEnc, String otpFromScanner) { ... }
}
```

**Key management:** the AES key is injected from `application.properties`
(`ticketpeak.totp.encryption-key`, base64-encoded 32-byte key).
Loaded via a `@ConfigurationProperties` record. The key must never be logged or returned.

```java
@ConfigurationProperties("ticketpeak.totp")
public record TotpProperties(String encryptionKey) {}
```

Add to `.env`:
```
TOTP_ENCRYPTION_KEY=<base64-encoded-32-bytes>
```

Add to `application.properties`:
```properties
ticketpeak.totp.encryption-key=${TOTP_ENCRYPTION_KEY}
```

**Library:** use `com.eatthepath:java-otp` (already TOTP-standard, no reflection issues with GraalVM).
Add to `pom.xml`:
```xml
<dependency>
    <groupId>com.eatthepath</groupId>
    <artifactId>java-otp</artifactId>
    <version>0.4.1</version>
</dependency>
```

For AES-256-GCM, use the standard `javax.crypto` API (JDK built-in, no extra deps needed).

---

### 8. `TicketIssuanceService`

Listens to `OrderCreatedEvent` after commit. Expands each `OrderItem` by its `qty` into individual
`Ticket` rows, each with its own unique TOTP secret.

```java
@Slf4j
@Service
public class TicketIssuanceService {

    private final TicketRepository ticketRepository;
    private final TotpService totpService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onOrderCreated(OrderCreatedEvent event) {
        UUID orderId = event.orderId();

        // Idempotency guard
        if (ticketRepository.existsByOrderId(orderId)) {
            log.info("Tickets already issued for order {}. Skipping.", orderId);
            return;
        }

        List<Ticket> tickets = new ArrayList<>();

        for (OrderItemSnapshot item : event.items()) {
            for (int i = 0; i < item.qty(); i++) {
                String rawSecret = totpService.generateSecret();
                String encryptedSecret = totpService.encrypt(rawSecret);

                tickets.add(Ticket.builder()
                    .orderId(orderId)
                    .orderItemId(item.id())
                    .accountId(event.accountId())
                    .eventId(event.eventId())
                    .offerId(item.offerId())
                    .seatingMode(item.seatingMode())
                    .areaId(item.areaId())
                    .seatId(item.seatId())
                    .totpSecretEnc(encryptedSecret)
                    .status(TicketStatus.ISSUED)
                    .build());
            }
        }

        ticketRepository.saveAll(tickets);   // single bulk insert
        log.info("Issued {} ticket(s) for order {}.", tickets.size(), orderId);
    }
}
```

**RS seats:** for `RESERVED_SEATING`, `item.qty()` is always 1 (one item per seat from
`ReservationItem`), so each item produces exactly 1 ticket with its specific `seat_id`.

**GA seats:** `item.qty()` may be > 1 (buyer bought 3 GA tickets). Each of the 3 tickets shares
the same `area_id` but has its own unique TOTP secret and `id`.

---

### 9. `TicketService` — buyer-facing operations

```java
@Service
public class TicketService {

    // ── Reads ─────────────────────────────────────────────────────────────

    /** All tickets owned by the authenticated buyer. */
    public Page<TicketResponse> listMyTickets(UUID accountId, Pageable pageable) { ... }

    /** Single ticket detail (must be owner). */
    public TicketResponse getMyTicket(UUID accountId, UUID ticketId) { ... }

    // ── QR generation ─────────────────────────────────────────────────────

    /**
     * Returns the current 6-digit TOTP for the ticket's current 30-second window.
     * The QR encodes: ticketId + ":" + currentOtp.
     * The frontend renders this as a QR image client-side (e.g., qrcode.js).
     * The backend never generates or caches an image.
     */
    public TicketQrResponse getQr(UUID accountId, UUID ticketId) {
        Ticket ticket = ticketRepository.findByIdAndAccountId(ticketId, accountId)
            .orElseThrow(TicketException::notFound);

        if (ticket.getStatus() == TicketStatus.VOID) throw TicketException.voided();
        if (ticket.getStatus() == TicketStatus.CHECKED_IN) throw TicketException.alreadyUsed();

        String otp = totpService.generateOtp(ticket.getTotpSecretEnc());
        long windowExpiresAt = computeWindowExpiry();  // epoch ms of next 30-second boundary

        return new TicketQrResponse(ticket.getId(), otp, windowExpiresAt);
    }

    // ── Transfer ──────────────────────────────────────────────────────────

    public TransferResponse initiateTransfer(UUID senderId, UUID ticketId, InitiateTransferRequest req) {
        Ticket ticket = ticketRepository.findByIdAndAccountId(ticketId, senderId)
            .orElseThrow(TicketException::notFound);

        if (ticket.getStatus() != TicketStatus.ISSUED) throw TicketException.notTransferable();
        if (senderId.equals(req.recipientAccountId())) throw TicketException.cannotTransferToSelf();

        // Cancel any previous pending transfer for this ticket
        transferRepository.findPendingByTicketId(ticketId)
            .ifPresent(t -> { t.setStatus(TicketTransferStatus.CANCELLED); transferRepository.save(t); });

        ticket.setStatus(TicketStatus.TRANSFERRED);
        ticketRepository.save(ticket);

        TicketTransfer transfer = TicketTransfer.builder()
            .ticketId(ticketId)
            .senderId(senderId)
            .recipientId(req.recipientAccountId())
            .status(TicketTransferStatus.PENDING)
            .expiresAt(Instant.now().plus(Duration.ofHours(48)))
            .build();

        transferRepository.save(transfer);
        return TransferResponse.from(transfer);
    }

    public void acceptTransfer(UUID recipientId, UUID transferId) {
        TicketTransfer transfer = transferRepository.findById(transferId)
            .orElseThrow(TicketException::transferNotFound);

        if (!transfer.getRecipientId().equals(recipientId)) throw TicketException::notFound;
        if (transfer.getStatus() != TicketTransferStatus.PENDING) throw TicketException.transferNotPending();
        if (transfer.getExpiresAt().isBefore(Instant.now())) throw TicketException.transferExpired();

        Ticket ticket = ticketRepository.findById(transfer.getTicketId())
            .orElseThrow(TicketException::notFound);

        ticket.setAccountId(recipientId);
        ticket.setStatus(TicketStatus.ISSUED);
        ticketRepository.save(ticket);

        transfer.setStatus(TicketTransferStatus.ACCEPTED);
        transferRepository.save(transfer);
    }

    public void cancelTransfer(UUID senderId, UUID transferId) {
        TicketTransfer transfer = transferRepository.findById(transferId)
            .orElseThrow(TicketException::transferNotFound);

        if (!transfer.getSenderId().equals(senderId)) throw TicketException.transferNotFound();
        if (transfer.getStatus() != TicketTransferStatus.PENDING) throw TicketException.transferNotPending();

        Ticket ticket = ticketRepository.findById(transfer.getTicketId())
            .orElseThrow(TicketException::notFound);

        ticket.setStatus(TicketStatus.ISSUED);
        ticketRepository.save(ticket);

        transfer.setStatus(TicketTransferStatus.CANCELLED);
        transferRepository.save(transfer);
    }

    // ── Void (called by payment refund hook) ─────────────────────────────

    @Transactional
    public void voidTicketsByOrderId(UUID orderId) {
        List<Ticket> tickets = ticketRepository.findByOrderId(orderId);
        tickets.forEach(t -> t.setStatus(TicketStatus.VOID));
        ticketRepository.saveAll(tickets);
    }
}
```

---

### 10. `CheckInService` — gate staff

```java
@Service
public class CheckInService {

    private final TicketRepository ticketRepository;
    private final TotpService totpService;

    /**
     * Validate a QR scan at the gate.
     *
     * QR payload format: "{ticketId}:{otp}"
     *
     * Steps:
     *   1. Parse ticketId + otp from QR payload
     *   2. Load ticket; reject if wrong event
     *   3. Reject VOID or CHECKED_IN (idempotent: return success if already checked in by same scan)
     *   4. Verify TOTP (current window ±1)
     *   5. Pessimistic lock → set CHECKED_IN + checkedInAt
     */
    @Transactional
    public CheckInResponse checkIn(UUID staffAccountId, UUID eventId, CheckInRequest request) {
        String[] parts = request.qrPayload().split(":");
        if (parts.length != 2) throw TicketException.invalidQrFormat();

        UUID ticketId = UUID.fromString(parts[0]);
        String otpFromScanner = parts[1];

        Ticket ticket = ticketRepository.findByIdForUpdate(ticketId)
            .orElseThrow(TicketException::notFound);

        if (!ticket.getEventId().equals(eventId)) throw TicketException.wrongEvent();
        if (ticket.getStatus() == TicketStatus.VOID) throw TicketException.voided();
        if (ticket.getStatus() == TicketStatus.CHECKED_IN) {
            // Idempotent: already checked in — return success with timestamp
            return CheckInResponse.alreadyCheckedIn(ticket);
        }
        if (ticket.getStatus() == TicketStatus.TRANSFERRED) throw TicketException.pendingTransfer();

        if (!totpService.verifyOtp(ticket.getTotpSecretEnc(), otpFromScanner)) {
            throw TicketException.invalidOtp();
        }

        ticket.setStatus(TicketStatus.CHECKED_IN);
        ticket.setCheckedInAt(Instant.now());
        ticketRepository.save(ticket);

        return CheckInResponse.success(ticket);
    }
}
```

**Pessimistic locking** (`SELECT ... FOR UPDATE`) prevents two scanners from simultaneously
checking in the same ticket (race condition at high-throughput events).

---

### 11. `TicketException`

```java
public final class TicketException {
    private TicketException() {}

    public static ApiException notFound() {
        return new ApiException(NOT_FOUND, "TICKET_NOT_FOUND", "Ticket not found");
    }
    public static ApiException voided() {
        return new ApiException(GONE, "TICKET_VOIDED", "This ticket has been voided");
    }
    public static ApiException alreadyUsed() {
        return new ApiException(CONFLICT, "TICKET_ALREADY_CHECKED_IN", "Ticket has already been used");
    }
    public static ApiException notTransferable() {
        return new ApiException(CONFLICT, "TICKET_NOT_TRANSFERABLE",
            "Ticket must be in ISSUED status to transfer");
    }
    public static ApiException cannotTransferToSelf() {
        return new ApiException(BAD_REQUEST, "TICKET_TRANSFER_TO_SELF",
            "Cannot transfer a ticket to yourself");
    }
    public static ApiException transferNotFound() {
        return new ApiException(NOT_FOUND, "TICKET_TRANSFER_NOT_FOUND", "Transfer not found");
    }
    public static ApiException transferNotPending() {
        return new ApiException(CONFLICT, "TICKET_TRANSFER_NOT_PENDING",
            "Transfer is no longer pending");
    }
    public static ApiException transferExpired() {
        return new ApiException(GONE, "TICKET_TRANSFER_EXPIRED", "Transfer offer has expired");
    }
    public static ApiException invalidOtp() {
        return new ApiException(UNAUTHORIZED, "TICKET_INVALID_OTP",
            "QR code is invalid or expired");
    }
    public static ApiException invalidQrFormat() {
        return new ApiException(BAD_REQUEST, "TICKET_INVALID_QR_FORMAT", "Malformed QR payload");
    }
    public static ApiException wrongEvent() {
        return new ApiException(CONFLICT, "TICKET_WRONG_EVENT",
            "Ticket does not belong to this event");
    }
    public static ApiException pendingTransfer() {
        return new ApiException(CONFLICT, "TICKET_PENDING_TRANSFER",
            "Ticket has a pending transfer; check-in rejected");
    }
    public static ApiException issuanceFailed(String detail) {
        return new ApiException(INTERNAL_SERVER_ERROR, "TICKET_ISSUANCE_FAILED", detail);
    }
}
```

---

### 12. DTOs

```java
// TicketResponse.java
public record TicketResponse(
    UUID id,
    UUID orderId,
    UUID eventId,
    UUID offerId,
    String seatingMode,
    String areaId,
    String seatId,
    String status,
    Instant checkedInAt,
    Instant createdAt
) {
    public static TicketResponse from(Ticket t) { ... }
    // NOTE: totpSecretEnc is NEVER included
}

// TicketQrResponse.java
public record TicketQrResponse(
    UUID ticketId,
    String otp,            // 6-digit string; frontend encodes into QR
    long windowExpiresAt   // epoch ms — frontend uses to display countdown and auto-refresh
) {}

// CheckInRequest.java
public record CheckInRequest(
    @NotBlank String qrPayload   // "{ticketId}:{otp}"
) {}

// CheckInResponse.java
public record CheckInResponse(
    boolean success,
    boolean alreadyCheckedIn,
    UUID ticketId,
    String seatId,
    String areaId,
    String seatingMode,
    Instant checkedInAt
) {
    public static CheckInResponse success(Ticket t) { ... }
    public static CheckInResponse alreadyCheckedIn(Ticket t) { ... }
}

// InitiateTransferRequest.java
public record InitiateTransferRequest(
    @NotNull UUID recipientAccountId
) {}

// TransferResponse.java
public record TransferResponse(
    UUID transferId,
    UUID ticketId,
    UUID senderId,
    UUID recipientId,
    String status,
    Instant expiresAt
) {
    public static TransferResponse from(TicketTransfer t) { ... }
}
```

---

### 13. `TicketController` (buyer)

```java
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    // GET /api/tickets  — list my tickets (paginated)
    @GetMapping
    public ResponseEntity<ApiResponse<Page<TicketResponse>>> listMyTickets(
            @AuthenticationPrincipal AuthenticatedAccount account,
            Pageable pageable) { ... }

    // GET /api/tickets/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketResponse>> getTicket(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @PathVariable UUID id) { ... }

    // GET /api/tickets/{id}/qr  — returns current OTP; never caches
    @GetMapping("/{id}/qr")
    public ResponseEntity<ApiResponse<TicketQrResponse>> getQr(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @PathVariable UUID id) { ... }

    // POST /api/tickets/{id}/transfers  — initiate transfer
    @PostMapping("/{id}/transfers")
    public ResponseEntity<ApiResponse<TransferResponse>> initiateTransfer(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @PathVariable UUID id,
            @RequestBody @Valid InitiateTransferRequest request) { ... }

    // POST /api/tickets/transfers/{transferId}/accept
    @PostMapping("/transfers/{transferId}/accept")
    public ResponseEntity<ApiResponse<Void>> acceptTransfer(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @PathVariable UUID transferId) { ... }

    // DELETE /api/tickets/transfers/{transferId}  — sender cancels
    @DeleteMapping("/transfers/{transferId}")
    public ResponseEntity<ApiResponse<Void>> cancelTransfer(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @PathVariable UUID transferId) { ... }
}
```

---

### 14. `CheckInController` (gate staff / organizer)

```java
@RestController
@RequestMapping("/api/partner/events/{eventId}/check-in")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;

    // POST /api/partner/events/{eventId}/check-in
    @PostMapping
    public ResponseEntity<ApiResponse<CheckInResponse>> checkIn(
            @AuthenticationPrincipal AuthenticatedAccount staff,
            @PathVariable UUID eventId,
            @RequestBody @Valid CheckInRequest request) {
        CheckInResponse result = checkInService.checkIn(staff.accountId(), eventId, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
```

Access guard: the authenticated account must be an `ORGANIZER` role member of the org that owns the event.
For now, enforce with `@PreAuthorize` on the `ORGANIZER` role (same pattern as `PartnerEventController`).
Fine-grained org membership check is a future IAM improvement.

---

### 15. `TicketRepository`

```java
@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    Optional<Ticket> findByIdAndAccountId(UUID id, UUID accountId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Ticket t WHERE t.id = :id")
    Optional<Ticket> findByIdForUpdate(@Param("id") UUID id);

    List<Ticket> findByOrderId(UUID orderId);

    boolean existsByOrderId(UUID orderId);

    Page<Ticket> findByAccountIdOrderByCreatedAtDesc(UUID accountId, Pageable pageable);
}
```

---

### 16. `TicketTransferRepository`

```java
@Repository
public interface TicketTransferRepository extends JpaRepository<TicketTransfer, UUID> {

    @Query("SELECT t FROM TicketTransfer t WHERE t.ticketId = :ticketId AND t.status = 'PENDING'")
    Optional<TicketTransfer> findPendingByTicketId(@Param("ticketId") UUID ticketId);

    List<TicketTransfer> findByRecipientIdAndStatus(UUID recipientId, TicketTransferStatus status);
}
```

---

## Database Migrations

### `V22__create_ticket_schema.sql`

```sql
CREATE TABLE ticket (
    id                UUID         NOT NULL DEFAULT uuidv7() PRIMARY KEY,
    order_id          UUID         NOT NULL REFERENCES purchase_order(id) ON DELETE RESTRICT,
    order_item_id     UUID         NOT NULL REFERENCES order_item(id)     ON DELETE RESTRICT,
    account_id        UUID         NOT NULL REFERENCES account(id)        ON DELETE RESTRICT,
    event_id          UUID         NOT NULL REFERENCES event(id)          ON DELETE RESTRICT,
    offer_id          UUID         NOT NULL,
    seating_mode      VARCHAR(32)  NOT NULL,
    area_id           VARCHAR(64),
    seat_id           VARCHAR(64),
    totp_secret_enc   TEXT         NOT NULL,
    status            VARCHAR(16)  NOT NULL DEFAULT 'ISSUED',
    checked_in_at     TIMESTAMPTZ,
    version           BIGINT       NOT NULL DEFAULT 0,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_ticket_status
        CHECK (status IN ('ISSUED','TRANSFERRED','CHECKED_IN','VOID')),
    CONSTRAINT chk_ticket_seating_mode
        CHECK (seating_mode IN ('GENERAL_ADMISSION','RESERVED_SEATING'))
);

CREATE INDEX idx_ticket_account_id  ON ticket(account_id);
CREATE INDEX idx_ticket_order_id    ON ticket(order_id);
CREATE INDEX idx_ticket_event_id    ON ticket(event_id);

CREATE TABLE ticket_transfer (
    id            UUID        NOT NULL DEFAULT uuidv7() PRIMARY KEY,
    ticket_id     UUID        NOT NULL REFERENCES ticket(id) ON DELETE RESTRICT,
    sender_id     UUID        NOT NULL REFERENCES account(id) ON DELETE RESTRICT,
    recipient_id  UUID        NOT NULL REFERENCES account(id) ON DELETE RESTRICT,
    status        VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    expires_at    TIMESTAMPTZ NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_transfer_status
        CHECK (status IN ('PENDING','ACCEPTED','CANCELLED'))
);

CREATE INDEX idx_ticket_transfer_ticket_id    ON ticket_transfer(ticket_id);
CREATE INDEX idx_ticket_transfer_recipient_id ON ticket_transfer(recipient_id);
```

### `V23__rename_order_status_created_to_confirmed.sql`

```sql
-- Drop and recreate CHECK constraint with new status value
ALTER TABLE purchase_order DROP CONSTRAINT IF EXISTS chk_order_status;

UPDATE purchase_order SET status = 'CONFIRMED' WHERE status = 'CREATED';

ALTER TABLE purchase_order
    ADD CONSTRAINT chk_order_status
    CHECK (status IN ('CONFIRMED','FAILED','CANCELLED','REFUNDED'));
```

---

## Security Notes

- `totpSecretEnc` must be excluded from all serialization. Annotate with `@JsonIgnore` on the entity
  and never include it in any DTO record.
- The AES encryption key must be loaded from environment — never hardcoded. Fail-fast on startup
  if the key is missing or malformed.
- TOTP window tolerance: accept current window ± 1 (i.e., ±30 seconds). This is the standard TOTP
  tolerance per RFC 6238 and covers reasonable gate scanner lag.
- `GET /api/tickets/{id}/qr` is the only endpoint that touches the TOTP secret internally;
  it returns only the 6-digit OTP, never the secret itself.

---

## Acceptance Criteria

| # | Criterion |
|---|---|
| 1 | On `OrderCreatedEvent`, one `Ticket` row is created per unit (qty expansion). |
| 2 | Each ticket has a unique, encrypted TOTP secret. `totpSecretEnc` never appears in any API response. |
| 3 | `GET /api/tickets/{id}/qr` returns a fresh 6-digit OTP + window expiry. A second call 30 seconds later returns a different OTP. |
| 4 | Check-in with a valid, unused ticket returns success and sets `CHECKED_IN`. |
| 5 | Check-in with an already-used ticket is idempotent — returns success with original timestamp. |
| 6 | Check-in with an invalid or expired OTP returns `TICKET_INVALID_OTP` (401). |
| 7 | Check-in with a VOID ticket returns `TICKET_VOIDED` (410). |
| 8 | Two concurrent check-in requests for the same ticket produce exactly one `CHECKED_IN` (pessimistic lock test). |
| 9 | Transfer: sender's ticket moves to `TRANSFERRED`; recipient accepts → owner changes to recipient, status back to `ISSUED`. |
| 10 | Transfer: sender cancels before acceptance → ticket back to `ISSUED`. |
| 11 | Void: `voidTicketsByOrderId` sets all tickets for that order to `VOID`. |
| 12 | `listMyTickets` and `getMyTicket` only return tickets owned by the authenticated account. |
| 13 | Issuance is idempotent — duplicate `OrderCreatedEvent` does not create duplicate tickets. |
| 14 | All tests pass: `./mvnw verify`. AOT processing check passes: `./mvnw spring-boot:process-aot`. |

---

## Integration Test Coverage — `TicketServiceIT.java` + `CheckInServiceIT.java`

```text
TicketServiceIT:
  issuance_creates_correct_ticket_count_for_ga_qty_3
  issuance_creates_one_ticket_per_rs_item
  issuance_is_idempotent_on_duplicate_event
  get_qr_returns_valid_6_digit_otp
  get_qr_rejected_for_non_owner
  get_qr_rejected_for_void_ticket
  transfer_happy_path_changes_owner
  transfer_cancel_restores_issued_status
  transfer_to_self_rejected
  transfer_expired_rejected
  void_tickets_sets_all_to_void

CheckInServiceIT:
  checkin_valid_ticket_sets_checked_in
  checkin_idempotent_on_already_checked_in
  checkin_invalid_otp_rejected
  checkin_voided_ticket_rejected
  checkin_wrong_event_rejected
  checkin_concurrent_two_requests_only_one_succeeds
```

---

## Verification Plan

```powershell
cd api
./mvnw compile -q
./mvnw test -Dtest=TicketServiceIT,CheckInServiceIT
./mvnw spring-boot:process-aot
./mvnw verify
```

---

## Open Questions / Deferred

| Item | Decision |
|---|---|
| Push notification when transfer is received | Deferred to `notification` module |
| Ticket PDF / Apple Wallet pass | Out of scope for this plan |
| Gate staff identity verification (org membership check) | Deferred to IAM fine-grained guard |
| `java-otp` GraalVM reflect-config registration | Check after AOT processing; add if needed |
| Transfer expiry cleanup job | Scheduled job in this module; kept minimal for now |
