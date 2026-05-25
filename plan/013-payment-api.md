# 013 Payment API - Gateway Checkout and Payment Finalization

## Description
Implement the `payment` module as the gateway checkout and payment-finalization layer.

The current codebase already has:
- `offer` for commercial definition and sale windows
- `inventory` for event-scoped availability state
- `reservation` for temporary holds with expiry

Right now, `reservation.confirmReservation()` is doing work that belongs later in the payment
flow: it converts held inventory into sold inventory and marks the reservation as finalized
without a real payment step. This plan introduces a dedicated `payment` module and makes payment
success the only path that can finalize the reservation and commit inventory to sold state.

The target boundary is:
- `reservation` creates, reads, expires, and cancels temporary holds
- `payment` initiates checkout against VNPay or Stripe using a valid pending reservation
- `payment` receives and verifies gateway callbacks/webhooks and decides whether payment succeeded
  or failed
- `inventory` is updated to sold state only after verified, amount-validated payment finalization
- `order` and `ticket` will later depend on a completed payment event rather than a direct
  reservation confirmation; `payment` publishes an internal `PaymentCompletedEvent` via
  `ApplicationEventPublisher` after finalization so the future `order` module can listen
- `reservation.confirmReservation()` is removed; purchase-commit responsibility belongs entirely
  in the payment finalization path

## Gateway Integration Model

### VNPay (redirect-style)
1. Server generates a signed redirect URL (HMAC-SHA512 with `vnp_SecureHash`).
2. Buyer is redirected to VNPay.
3. VNPay sends an **IPN (Instant Payment Notification)** to a server-to-server callback URL — this
   is the authoritative signal and the only path that triggers finalization.
4. VNPay also redirects the buyer back to a **Return URL** with query params — this is used only
   to show a UI result page; it must never trigger business logic because the buyer can craft
   arbitrary query params.

> **Security rule**: only the IPN endpoint finalizes payment. The return URL endpoint reads
> payment status from the database and returns it; it does not write anything.

### Stripe (intent-style)
1. Server creates a `PaymentIntent` and returns the `client_secret` to the buyer client.
2. Buyer uses Stripe.js to confirm the payment on the client side.
3. Stripe sends a **webhook event** (`payment_intent.succeeded`, `payment_intent.payment_failed`,
   etc.) to the server — this is the authoritative signal and the only path that triggers
   finalization.

## Payment Status Lifecycle

```
PENDING      — payment record created, redirect URL generated / PaymentIntent created
PROCESSING   — buyer has left for the gateway; IPN or webhook not yet received
               (optional intermediate state; may be omitted in V1 and go straight from
                PENDING to COMPLETED/FAILED when the callback arrives)
COMPLETED    — IPN or webhook confirmed success AND amount validated
FAILED       — gateway reported failure or amount mismatch
CANCELLED    — buyer cancelled at the gateway page
EXPIRED      — reservation expired before payment was finalized
```

Transitions:
- `PENDING` → `PROCESSING` (optional, on redirect / PaymentIntent creation confirmation)
- `PENDING | PROCESSING` → `COMPLETED` (verified IPN/webhook, amount matches)
- `PENDING | PROCESSING` → `FAILED` (gateway failure or amount mismatch)
- `PENDING | PROCESSING` → `CANCELLED` (buyer-initiated cancel at gateway)
- `PENDING | PROCESSING` → `EXPIRED` (reservation expired before finalization)

Terminal states: `COMPLETED`, `FAILED`, `CANCELLED`, `EXPIRED`. No transition out of these.

## Multiple Payment Attempts per Reservation

A buyer may initiate VNPay, navigate back, and try again before their reservation expires.
The module supports multiple payment attempts per reservation under these rules:

- When a new payment is initiated for a reservation that already has a `PENDING` or `PROCESSING`
  payment, the existing record is cancelled (`CANCELLED`) before the new one is created.
- Only one payment per reservation may reach `COMPLETED`. A unique constraint on
  `(reservation_id, status)` filtered to `COMPLETED` (partial index) enforces this at the
  database level in addition to application-layer checks.
- `FAILED`, `CANCELLED`, and `EXPIRED` records are kept for audit purposes.

## Reservation Expiry Race During Finalization

IPN or webhook may arrive shortly after the reservation's `expiresAt` has passed but before the
expiry job has run. The rule is:

- If the reservation status is still `PENDING` at finalization time, treat it as valid regardless
  of `expiresAt` — the IPN/webhook represents a real payment that completed before the expiry
  job ran.
- If the reservation status is already `EXPIRED` or `CANCELLED` at finalization time, reject
  finalization: mark the payment as `FAILED`, do not convert inventory, and issue a refund
  signal (log + future integration point) for out-of-band reconciliation.

## Reservation Status After Payment Success

After successful payment finalization, the reservation status is set to `CONFIRMED`. No new
enum value is added. `CONFIRMED` means "consumed by a completed payment" going forward;
`reservation.confirmReservation()` is removed so this status is never set by any other path.

## Refund (Out of Scope for This Plan)

Refunds are not implemented in this plan. The payment entity includes `refunded_at` (nullable
`Instant`) and `refunded_amount` (nullable `BigDecimal`) columns so the schema is refund-ready
without a future migration. A `REFUNDED` status value is reserved in the enum but no transition
into it is implemented here.

## Action Items

### 1. Flyway migration

Table `payment`:
- `id` UUID PK (uuidv7)
- `reservation_id` UUID NOT NULL — FK to `reservation.id`
- `account_id` UUID NOT NULL
- `event_id` UUID NOT NULL
- `provider` VARCHAR(16) NOT NULL — `VNPAY` | `STRIPE`
- `status` VARCHAR(16) NOT NULL
- `amount` NUMERIC(19,2) NOT NULL — expected amount computed at initiation; used for validation
- `currency` VARCHAR(8) NOT NULL
- `gateway_transaction_id` VARCHAR(256) — nullable until gateway assigns one; unique when not null
- `gateway_payload` JSONB NOT NULL DEFAULT '{}' — raw request sent to gateway (for audit)
- `gateway_response` JSONB NOT NULL DEFAULT '{}' — raw callback/webhook body (for reconciliation)
- `refunded_at` TIMESTAMPTZ
- `refunded_amount` NUMERIC(19,2)
- `created_at` TIMESTAMPTZ NOT NULL
- `updated_at` TIMESTAMPTZ NOT NULL
- `version` BIGINT NOT NULL DEFAULT 0 — optimistic lock

Indexes:
- `idx_payment_reservation_id` on `reservation_id`
- `idx_payment_gateway_transaction_id` UNIQUE on `gateway_transaction_id` WHERE NOT NULL
- Partial unique index on `(reservation_id)` WHERE `status = 'COMPLETED'`

### 2. `PaymentException` factory

Create `payment/exception/PaymentException.java` following the module exception factory pattern:

```java
public final class PaymentException {
    private PaymentException() {}

    public static ApiException reservationNotFound() { ... }         // 404
    public static ApiException reservationNotPending() { ... }       // 409
    public static ApiException reservationExpired() { ... }          // 410
    public static ApiException reservationOwnerMismatch() { ... }    // 403
    public static ApiException alreadyCompleted() { ... }            // 409
    public static ApiException invalidSignature() { ... }            // 400
    public static ApiException amountMismatch() { ... }              // 422
    public static ApiException gatewayError(String detail) { ... }   // 502
    public static ApiException notFound() { ... }                    // 404
    public static ApiException unsupportedProvider() { ... }         // 400
}
```

### 3. Entity, repository, service, controller

- `Payment` entity mapped to `payment` table with `@Version` for optimistic locking.
- `PaymentStatus` enum with all values listed in the lifecycle section.
- `PaymentProvider` enum: `VNPAY`, `STRIPE`.
- `PaymentRepository` with:
  - `findByIdAndAccountId`
  - `findByReservationIdAndStatus`
  - `findPendingOrProcessingByReservationId`
  - `findByGatewayTransactionId`
- `PaymentService` with:
  - `initiatePayment(accountId, reservationId, provider)` — validates reservation, cancels any
    existing PENDING/PROCESSING payment for the same reservation, creates new Payment record,
    calls provider-specific checkout builder.
  - `finalizeVnpayIpn(params)` — verifies HMAC-SHA512 signature, validates amount, transitions
    payment to COMPLETED/FAILED, converts inventory, sets reservation CONFIRMED, publishes
    `PaymentCompletedEvent`.
  - `finalizeStripeWebhook(payload, signatureHeader)` — verifies Stripe-Signature HMAC, handles
    `payment_intent.succeeded` and `payment_intent.payment_failed` events, same finalization path.
  - `getPaymentStatus(accountId, paymentId)` — read-only, ownership check.
  - `getPaymentByReservation(accountId, reservationId)` — read-only, returns latest attempt.

### 4. Provider-specific checkout builders

- `VnpayCheckoutBuilder` — builds signed redirect URL from `Payment` record using config:
  `vnpay.tmn-code`, `vnpay.hash-secret`, `vnpay.pay-url`, `vnpay.return-url`, `vnpay.ipn-url`.
- `StripeCheckoutBuilder` — creates `PaymentIntent` via Stripe SDK using config:
  `stripe.secret-key`, `stripe.webhook-secret`.

Both builders are internal components, not Spring beans exposed beyond the service layer.

### 5. Signature verification

- VNPay: verify `vnp_SecureHash` using HMAC-SHA512 before reading any other IPN parameter.
- Stripe: verify `Stripe-Signature` header using `Stripe.Webhook.constructEvent` before
  processing the event body.
- Both: reject with `PaymentException.invalidSignature()` on mismatch; log the raw body for
  incident investigation.

### 6. Amount validation

On every IPN or webhook callback, verify:

```
gateway_reported_amount == payment.amount
```

VNPay reports in VND integer units (×100 vs face value — handle the conversion).
Stripe reports in the smallest currency unit (cents for USD, đồng for VND).

Mismatch → `PaymentException.amountMismatch()`, mark payment `FAILED`, do not convert inventory.

### 7. Idempotency

Duplicate IPN or webhook delivery (both gateways may retry) is handled by:

1. Look up payment by `gateway_transaction_id`. If status is already `COMPLETED`, return the
   success acknowledgement immediately without re-processing.
2. Use optimistic locking (`@Version`) on `Payment` to prevent concurrent finalization of the
   same record.
3. Partial unique index on `(reservation_id) WHERE status = 'COMPLETED'` as database-level guard.

### 8. Inventory finalization

Reuse `InventoryService` methods already in place:
- GA items: `confirmGAInventory(eventId, areaId, offerId, qty)` — held → sold.
- RS items: `sellSeat(eventId, seatId)` — HELD → SOLD.

All items are processed within the same `@Transactional` boundary as the payment status update
and reservation status update. Any failure rolls back everything.

### 9. Controller endpoints

```
POST   /api/payments                        — initiate checkout (authenticated buyer)
GET    /api/payments/{id}                   — get payment status (authenticated buyer, ownership check)
GET    /api/payments/reservation/{reservationId} — get latest payment for a reservation (authenticated buyer)
GET    /api/payments/vnpay/return           — VNPay return URL handler (reads status, no writes)
POST   /api/payments/vnpay/ipn              — VNPay IPN (server-to-server, no auth, signature required)
POST   /api/payments/stripe/webhook         — Stripe webhook (no auth, signature required)
```

IPN and webhook endpoints must be excluded from Spring Security authentication filters.

### 10. Remove `reservation.confirmReservation()`

- Remove `confirmReservation` method from `ReservationService`.
- Remove `POST /api/reservations/{id}/confirm` from `ReservationController`.
- This also resolves the transaction rollback bug identified in the reservation module review
  (expireSingle was called inside the same transaction that was then rolled back by the thrown
  exception).

## Acceptance Criteria

- [ ] The `payment` module has its own package, Flyway migration, entities, repositories,
      services, controllers, and `PaymentException` factory.
- [ ] A payment attempt is created from exactly one pending, non-expired reservation owned by
      the authenticated buyer.
- [ ] When a new payment is initiated for a reservation that already has a PENDING or PROCESSING
      payment, the old record is cancelled before the new one is created.
- [ ] The `payment` table schema includes `gateway_transaction_id` (unique when not null),
      `gateway_payload`, `gateway_response`, `refunded_at`, and `refunded_amount`.
- [ ] VNPay checkout produces a signed redirect URL using HMAC-SHA512.
- [ ] Stripe checkout creates a `PaymentIntent` and returns the `client_secret`.
- [ ] VNPay IPN handler verifies `vnp_SecureHash` before processing; rejects with 400 on mismatch.
- [ ] Stripe webhook handler verifies `Stripe-Signature` before processing; rejects with 400 on
      mismatch.
- [ ] Both handlers validate that the gateway-reported amount matches `payment.amount`; mismatch
      marks payment FAILED and does not convert inventory.
- [ ] VNPay return URL endpoint only reads payment status from the database; it does not write
      any state.
- [ ] Successful payment finalization atomically: marks payment COMPLETED, sets reservation
      CONFIRMED, and converts held inventory to sold for all reservation items.
- [ ] Failed, cancelled, or amount-mismatched payments do not convert inventory to sold state.
- [ ] If the reservation is already EXPIRED or CANCELLED at IPN/webhook finalization time,
      payment is marked FAILED and inventory is not converted.
- [ ] Finalization is idempotent: duplicate IPN or webhook delivery for an already-COMPLETED
      payment returns success acknowledgement without re-processing.
- [ ] Optimistic locking (`@Version`) prevents concurrent finalization of the same payment record.
- [ ] A partial unique index on `(reservation_id) WHERE status = 'COMPLETED'` prevents two
      completed payments for the same reservation at the database level.
- [ ] `PaymentCompletedEvent` is published via `ApplicationEventPublisher` after successful
      finalization, carrying `paymentId`, `reservationId`, `eventId`, `accountId`, `amount`,
      `currency`.
- [ ] `reservation.confirmReservation()` and `POST /api/reservations/{id}/confirm` are removed.
- [ ] Buyer-facing read endpoints return payment status through record DTOs wrapped in
      `ApiResponse<T>`.
- [ ] Integration tests cover: checkout initiation for VNPay and Stripe, duplicate initiation
      cancels previous attempt, valid IPN finalization, valid webhook finalization, invalid
      signature rejection, amount mismatch rejection, duplicate callback idempotency, expired
      reservation at finalization, ownership checks, and sold inventory transition.

## Status
`planned`

## Outcome
TBD
