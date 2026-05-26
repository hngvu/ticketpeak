# Payout Module — Implementation Plan

> **Scope:** Ghi nhận đầy đủ thông tin payout cho seller sau resale.
> Không wallet, không auto-transfer — lưu đủ để admin/job xử lý sau.
> Prerequisite cho Resale module.

---

## 1. Prerequisite — Tách EncryptionService

Logic AES-256-GCM encrypt/decrypt hiện đang nằm trong `TotpService`. Cần tách ra trước khi implement payout.

### [NEW] `common/util/EncryptionService.java`

- Inject key từ config property `ticketpeak.encryption.secret-key` (Base64, 32 bytes) — **key riêng, khác với TOTP key**
- Expose: `String encrypt(String plaintext)`, `String decrypt(String ciphertext)`
- Dùng AES/GCM/NoPadding + random 12-byte IV, prepend IV vào ciphertext — giống logic hiện tại trong `TotpService`

### [MODIFY] `ticket/service/TotpService.java`

- Inject `EncryptionService`, xóa duplicate AES logic
- Giữ nguyên `generateSecret()`, `generateOtp()`, `verifyOtp()`, `computeWindowExpiry()`
- `encrypt()` và `decrypt()` delegate sang `EncryptionService`

### [MODIFY] `ticket/config/TotpProperties.java` → không đổi

### [NEW] `common/config/EncryptionProperties.java`

```java
@ConfigurationProperties("ticketpeak.encryption")
public record EncryptionProperties(String secretKey) {}
```

### [MODIFY] `application.yaml`

```yaml
ticketpeak:
  encryption:
    secret-key: ${ENCRYPTION_SECRET_KEY}   # Base64, 32 bytes
  totp:
    encryption-key: ${TOTP_ENCRYPTION_KEY} # giữ nguyên
```

---

## 2. Database Migration

### [NEW] `V28__create_payout_schema.sql`

```sql
CREATE TABLE payout_method (
    id              UUID        NOT NULL DEFAULT uuidv7() PRIMARY KEY,
    account_id      UUID        NOT NULL,
    type            VARCHAR(32) NOT NULL,        -- BANK_TRANSFER | MOMO | ZALOPAY
    is_primary      BOOLEAN     NOT NULL DEFAULT FALSE,
    holder_name     VARCHAR(255) NOT NULL,
    bank_code       VARCHAR(16),                 -- mã Napas, nullable với ví điện tử
    account_number  TEXT        NOT NULL,        -- encrypted AES-GCM
    status          VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE | REMOVED
    verified_at     TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL
);

-- Đảm bảo DB-level chỉ một primary per account
CREATE UNIQUE INDEX idx_payout_method_primary_account
    ON payout_method(account_id)
    WHERE is_primary = TRUE AND status = 'ACTIVE';

CREATE TABLE payout (
    id                      UUID            NOT NULL DEFAULT uuidv7() PRIMARY KEY,
    resale_listing_id       UUID            NOT NULL,   -- FK → resale_listing (added when resale module lands)
    seller_id               UUID            NOT NULL,   -- denormalized từ listing
    payout_method_id        UUID            NOT NULL,   -- FK → payout_method
    payout_method_snapshot  JSONB           NOT NULL,   -- snapshot tại thời điểm sold
    gross_amount            NUMERIC(19,2)   NOT NULL,   -- giá buyer trả
    platform_fee_percent    NUMERIC(5,2)    NOT NULL,   -- snapshot từ event lúc sold
    platform_fee_amount     NUMERIC(19,2)   NOT NULL,   -- = gross × percent / 100
    net_amount              NUMERIC(19,2)   NOT NULL,   -- = gross − fee (seller nhận)
    currency                VARCHAR(3)      NOT NULL,
    status                  VARCHAR(16)     NOT NULL DEFAULT 'PENDING',
    scheduled_after         TIMESTAMPTZ     NOT NULL,   -- earliest time để payout (event.endAt + buffer)
    processed_at            TIMESTAMPTZ,
    external_ref            VARCHAR(255),               -- mã giao dịch ngân hàng/ví để đối soát
    note                    TEXT,                       -- admin ghi chú
    created_at              TIMESTAMPTZ     NOT NULL,
    updated_at              TIMESTAMPTZ     NOT NULL
);

-- Index cho job query hiệu quả
CREATE INDEX idx_payout_status_scheduled
    ON payout(status, scheduled_after);

CREATE INDEX idx_payout_seller
    ON payout(seller_id);
```

---

## 3. Domain Models

### [NEW] `payout/model/PayoutMethodType.java`

```java
public enum PayoutMethodType {
    BANK_TRANSFER,
    MOMO,
    ZALOPAY
}
```

### [NEW] `payout/model/PayoutMethodStatus.java`

```java
public enum PayoutMethodStatus {
    ACTIVE,
    REMOVED   // soft delete — không xóa hard vì payout records reference vào
}
```

### [NEW] `payout/model/PayoutStatus.java`

```java
public enum PayoutStatus {
    PENDING,      // chờ scheduledAfter
    PROCESSING,   // admin đang xử lý
    PAID,         // đã chuyển tiền thành công
    FAILED,       // chuyển thất bại — cần retry
    CANCELLED     // resale bị refund trước khi payout
}
```

### [NEW] `payout/model/PayoutMethod.java`

Fields:
- `UUID id`
- `UUID accountId`
- `PayoutMethodType type`
- `boolean isPrimary`
- `String holderName`
- `String bankCode` (nullable)
- `String accountNumber` — encrypted via `EncryptionService`, never returned to client raw
- `PayoutMethodStatus status`
- `Instant verifiedAt` (nullable)
- audit fields, `@Version`

### [NEW] `payout/model/Payout.java`

Fields:
- `UUID id`
- `UUID resaleListingId`
- `UUID sellerId`
- `UUID payoutMethodId`
- `Map<String, Object> payoutMethodSnapshot` (jsonb) — snapshot holderName, bankCode, accountNumber **decrypted**, type lúc sold
- `BigDecimal grossAmount`
- `BigDecimal platformFeePercent`
- `BigDecimal platformFeeAmount`
- `BigDecimal netAmount`
- `String currency`
- `PayoutStatus status`
- `Instant scheduledAfter` — `event.endAt + configurable buffer (default 3 days)`, fallback `event.startAt + 1 day` nếu endAt null
- `Instant processedAt` (nullable)
- `String externalRef` (nullable)
- `String note` (nullable)
- audit fields, `@Version`

---

## 4. DTOs

### [NEW] `payout/dto/CreatePayoutMethodRequest.java`

```java
public record CreatePayoutMethodRequest(
    @NotNull PayoutMethodType type,
    @NotBlank String holderName,
    String bankCode,               // required khi type = BANK_TRANSFER
    @NotBlank String accountNumber,
    boolean isPrimary
) {}
```

Validation rule: nếu `type == BANK_TRANSFER` thì `bankCode` không được blank — enforce trong service layer.

### [NEW] `payout/dto/PayoutMethodResponse.java`

```java
public record PayoutMethodResponse(
    UUID id,
    PayoutMethodType type,
    boolean isPrimary,
    String holderName,
    String bankCode,
    String maskedAccountNumber,   // hiển thị dạng ****1234, không trả raw
    PayoutMethodStatus status,
    Instant verifiedAt,
    Instant createdAt
) {}
```

### [NEW] `payout/dto/PayoutResponse.java`

```java
public record PayoutResponse(
    UUID id,
    UUID resaleListingId,
    UUID sellerId,
    BigDecimal grossAmount,
    BigDecimal platformFeePercent,
    BigDecimal platformFeeAmount,
    BigDecimal netAmount,
    String currency,
    PayoutStatus status,
    Instant scheduledAfter,
    Instant processedAt,
    String externalRef,
    String note,
    Instant createdAt
) {}
```

### [NEW] `payout/dto/UpdatePayoutStatusRequest.java` (internal/admin)

```java
public record UpdatePayoutStatusRequest(
    @NotNull PayoutStatus status,
    String externalRef,
    String note
) {}
```

---

## 5. Repositories

### [NEW] `payout/repository/PayoutMethodRepository.java`

```java
List<PayoutMethod> findByAccountIdAndStatus(UUID accountId, PayoutMethodStatus status);
Optional<PayoutMethod> findByAccountIdAndIsPrimaryTrueAndStatus(UUID accountId, PayoutMethodStatus status);
Optional<PayoutMethod> findByIdAndAccountId(UUID id, UUID accountId);
```

### [NEW] `payout/repository/PayoutRepository.java`

```java
List<Payout> findByStatusAndScheduledAfterBefore(PayoutStatus status, Instant before, Pageable pageable);
List<Payout> findBySellerId(UUID sellerId, Pageable pageable);
Optional<Payout> findByResaleListingId(UUID resaleListingId);
```

---

## 6. Services

### [NEW] `payout/service/PayoutMethodService.java`

| Method | Logic |
|--------|-------|
| `addMethod(accountId, req)` | Encrypt accountNumber, nếu `isPrimary=true` thì unset primary cũ (guard: DB partial unique index sẽ catch race condition) |
| `setPrimary(accountId, methodId)` | Unset primary cũ → set primary mới trong cùng transaction |
| `removeMethod(accountId, methodId)` | Soft delete → REMOVED. Guard: throw nếu có payout PENDING hoặc PROCESSING reference vào method này |
| `listMethods(accountId)` | Trả về ACTIVE methods, mask accountNumber |
| `getMethod(accountId, methodId)` | Single method, ownership check |

### [NEW] `payout/service/PayoutService.java`

| Method | Caller | Logic |
|--------|--------|-------|
| `createPayout(resaleListingId, sellerId, methodId, grossAmount, platformFeePercent, currency, eventEndAt)` | ResaleService (khi sold) | Tính fee, net, set `scheduledAfter`, snapshot payout method (decrypt accountNumber vào snapshot), save PENDING |
| `markProcessing(payoutId)` | Admin | PENDING → PROCESSING |
| `markPaid(payoutId, externalRef, note)` | Admin | PROCESSING → PAID, set `processedAt` |
| `markFailed(payoutId, note)` | Admin | PROCESSING → FAILED |
| `cancelPayout(resaleListingId)` | ResaleService (khi refund) | PENDING → CANCELLED. Throw nếu đã PROCESSING hoặc PAID |
| `listPayouts(status, scheduledBefore, pageable)` | Admin | Filter cho job/dashboard |

### [NEW] `payout/service/PayoutScheduledReleaseJob.java`

```java
@Scheduled(cron = "0 0 8 * * *")   // 8h sáng hàng ngày
public void releaseEligiblePayouts() {
    // Query PENDING payouts có scheduledAfter <= now()
    // Log ra danh sách để admin xử lý
    // Không tự chuyển tiền — chỉ log/notify
}
```

> Job này chỉ notify, không tự transfer — đúng với yêu cầu "lưu thông tin đầy đủ để xử lý sau".
> Khi cần auto-transfer sau này chỉ cần thêm logic vào đây.

---

## 7. Controllers

### [NEW] `payout/controller/PayoutMethodController.java`

```
GET    /api/payout/methods              — list seller's active methods
POST   /api/payout/methods              — add new method
PUT    /api/payout/methods/{id}/primary — set as primary
DELETE /api/payout/methods/{id}         — soft delete
```

Tất cả require `@AuthenticationPrincipal`, ownership enforced trong service.

### [NEW] `payout/controller/InternalPayoutController.java`

```
GET   /api/internal/payouts                    — list with filter (status, scheduledBefore, sellerId)
GET   /api/internal/payouts/{id}               — detail
PATCH /api/internal/payouts/{id}/status        — update status (PROCESSING / PAID / FAILED)
```

Require `hasRole('ADMIN')`.

### [NEW] `payout/exception/PayoutException.java`

| Factory method | HTTP | Code |
|----------------|------|------|
| `methodNotFound()` | 404 | `PAYOUT_METHOD_NOT_FOUND` |
| `methodInUse()` | 409 | `PAYOUT_METHOD_IN_USE` |
| `payoutNotFound()` | 404 | `PAYOUT_NOT_FOUND` |
| `invalidStatusTransition()` | 409 | `PAYOUT_INVALID_STATUS_TRANSITION` |
| `payoutAlreadyProcessed()` | 409 | `PAYOUT_ALREADY_PROCESSED` |
| `noPrimaryMethod()` | 422 | `PAYOUT_NO_PRIMARY_METHOD` |

---

## 8. Implementation Order

1. `EncryptionService` + refactor `TotpService` — foundation, unblock mọi thứ sau
2. Migration `V28`
3. Models + Enums
4. Repositories
5. `PayoutMethodService` + Controller
6. `PayoutService` (không có `createPayout` chưa — để resale gọi sau)
7. `InternalPayoutController`
8. `PayoutScheduledReleaseJob`
9. Integration tests

---

## 9. Open Questions trước khi code

| # | Câu hỏi | Ảnh hưởng |
|---|---------|-----------|
| 1 | Buffer sau event bao nhiêu ngày? (default 3) | `scheduledAfter` calculation |
| 2 | Có verify payout method (admin approve) trước khi seller dùng được không? | Nếu có: guard trong `createListing` check `verifiedAt != null` |
| 3 | Seller có xem được status payout của mình không? | Nếu có: thêm `GET /api/payout/payouts` cho seller |
