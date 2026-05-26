# 015 Data Model Fixes — TicketType Entity + GA Oversell Guard

Status: done
Outcome:
- Created `TicketType` entity and endpoints.
- Migrated `Offer.ticketTypeId` to `UUID`.
- Added Flyway migrations `V23` and `V24`.
- Enforced GA oversell guard in `InventoryEventListener`.

---

## Description

Hai vấn đề được phát hiện trong quá trình rà soát toàn bộ entity (tiến hành trước plan 016):

1. **`TicketType` entity không tồn tại.** `Offer.ticketTypeId` hiện là `VARCHAR(255)` do organizer
   tự nhập tự do, không FK vào entity nào. Theo chuẩn Ticketmaster, `TicketType` là entity độc lập
   scoped theo event; nhiều `Offer` có thể cùng tham chiếu một `TicketType` (ví dụ presale VIP và
   general-sale VIP là cùng type "VIP" nhưng là hai offer khác nhau). Hiện tại thiết kế đang buộc
   1 offer = 1 ticket type, gây nhầm lẫn khái niệm và chặn trường hợp hợp lệ trên.

2. **GA oversell risk.** Khi nhiều offer cùng `sectionId`/`priceLevelId` map vào một GA area,
   `InventoryEventListener` tạo nhiều `InventoryGa` row riêng biệt, mỗi row có `total =
   offer.capacityCap`. Không có guard kiểm tra tổng `capacityCap` của các offer trong cùng một
   area không vượt `GAArea.capacity`. Hậu quả: có thể bán nhiều hơn số chỗ vật lý.

---

## Scope

| Feature | Included |
|---|---|
| Tạo `TicketType` entity + migration | ✅ |
| Migrate `offer.ticket_type_id` từ VARCHAR → UUID FK | ✅ |
| Cập nhật `Offer.java`, `OfferService`, `OfferController`, DTOs | ✅ |
| Guard tổng `capacityCap` của các GA offer trong cùng area | ✅ |
| Cập nhật `InventoryEventListener` validate trước khi init | ✅ |
| Cập nhật plan 016 (Ticket) snapshot `ticketTypeId` UUID | ✅ |

---

## Dependency Map

```
event ──► ticket_type (scoped per event)
               ↑
            offer.ticket_type_id FK
               ↑
         inventory_ga (offer_id FK)
               ↑
         plan 016 ticket snapshot
```

---

## Proposed Package Changes

```text
api/offer/
├── model/
│   └── TicketType.java          (new entity)
├── repository/
│   └── TicketTypeRepository.java (new)
├── dto/
│   ├── TicketTypeResponse.java   (new)
│   ├── CreateTicketTypeRequest.java (new)
│   └── CreateOfferRequest.java   (ticketTypeId: String → UUID)
└── service/
    └── OfferService.java         (updated)

api/inventory/
└── service/
    └── InventoryEventListener.java (add capacity guard)
```

---

## New Implementation

### 1. `TicketType` entity

Scoped theo event. Mỗi event có tập `TicketType` riêng (ví dụ: VIP, General Admission,
Early Bird). Nhiều `Offer` có thể tham chiếu cùng một `TicketType`.

```java
@Entity
@Table(name = "ticket_type")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class TicketType {

    @Id
    @Generated(GenerationTime.INSERT)
    @Column(columnDefinition = "UUID DEFAULT uuidv7()", insertable = false, updatable = false, nullable = false)
    UUID id;

    @Column(name = "event_id", nullable = false)
    UUID eventId;                       // soft ref → event(id)

    @Column(nullable = false, length = 255)
    String name;                        // display name: "VIP", "General Admission"

    @Column(nullable = false, length = 255)
    String slug;                        // url-friendly: "vip", "general-admission"

    @Column(columnDefinition = "TEXT")
    String description;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;
}
```

**Constraint:** `UNIQUE (event_id, slug)` — slug unique per event, dùng cho URL path.

**Slug generation:** lowercase, replace spaces/special chars với `-`, ví dụ
`"VIP Weekend"` → `"vip-weekend"`.

---

### 2. Thay đổi `Offer` entity

```java
// TRƯỚC (xóa):
@Column(name = "ticket_type_id", nullable = false)
String ticketTypeId;

// SAU (thêm):
@Column(name = "ticket_type_id", nullable = false)
UUID ticketTypeId;    // soft ref → ticket_type(id)
```

Không dùng `@ManyToOne` vì `ticket_type` cùng module với `offer` —
trên thực tế đây là intra-module reference nên có thể dùng `@ManyToOne`.
Tuy nhiên giữ soft reference để nhất quán với convention của project.

> **Note:** `OfferService` hiện dùng `ticketTypeId` String làm path variable cho
> `findByEventIdAndTicketTypeId(...)`. Sau khi đổi sang UUID, các endpoint partner
> offer sẽ dùng `ticketTypeId` UUID hoặc slug của `TicketType` làm path param.
> **Chọn slug** để URL vẫn human-readable và backward-compatible hơn.

---

### 3. `TicketTypeRepository`

```java
@Repository
public interface TicketTypeRepository extends JpaRepository<TicketType, UUID> {

    List<TicketType> findByEventIdOrderByCreatedAtAsc(UUID eventId);

    Optional<TicketType> findByEventIdAndSlug(UUID eventId, String slug);

    boolean existsByEventIdAndSlug(UUID eventId, String slug);
}
```

---

### 4. `TicketTypeResponse` DTO

```java
public record TicketTypeResponse(
    UUID id,
    UUID eventId,
    String name,
    String slug,
    String description,
    Instant createdAt,
    Instant updatedAt
) {
    public static TicketTypeResponse from(TicketType t) { ... }
}
```

---

### 5. Cập nhật `OfferService`

**Tạo offer:** nhận `ticketTypeId` UUID thay vì String. Validate UUID tồn tại và thuộc
đúng event trước khi tạo offer.

```java
// Trong createOffer(UUID eventId, CreateOfferRequest request):
TicketType ticketType = ticketTypeRepository.findById(request.ticketTypeId())
    .filter(tt -> tt.getEventId().equals(eventId))
    .orElseThrow(OfferException::ticketTypeNotFound);
```

**Lookup offer:** các endpoint hiện dùng `ticketTypeId` String (slug của cũ) làm path
variable. Chuyển sang dùng `Offer.id` (UUID) hoặc tra qua `TicketType.slug`. Đề xuất:

```
GET  /api/partner/events/{eventId}/offers           → list (không đổi)
POST /api/partner/events/{eventId}/offers           → create (không đổi)
GET  /api/partner/events/{eventId}/offers/{offerId} → by Offer UUID (đổi từ ticketTypeId slug)
PUT  /api/partner/events/{eventId}/offers/{offerId} → by Offer UUID
DEL  /api/partner/events/{eventId}/offers/{offerId} → by Offer UUID
```

Thêm partner endpoints cho `TicketType`:

```
POST /api/partner/events/{eventId}/ticket-types            → tạo TicketType
GET  /api/partner/events/{eventId}/ticket-types            → list
GET  /api/partner/events/{eventId}/ticket-types/{slug}     → get by slug
DEL  /api/partner/events/{eventId}/ticket-types/{slug}     → delete (chỉ khi không có offer nào ref)
```

---

### 6. GA Oversell Guard trong `InventoryEventListener`

Thêm validation trước khi `saveAll` GA inventory: tổng `capacityCap` của các offer
map vào cùng một `area_id` không được vượt `GAArea.capacity`.

```java
// Trong onEventStatusTransition, sau khi build gaInventories list:

// Group by areaId, kiểm tra tổng capacity
Map<String, Integer> totalByArea = new HashMap<>();
for (InventoryGa row : gaInventories) {
    totalByArea.merge(row.getAreaId(), row.getTotal(), Integer::sum);
}

for (GAArea area : gaAreas) {
    Integer totalAllocated = totalByArea.get(area.getId());
    if (totalAllocated != null && totalAllocated > area.getCapacity()) {
        throw InventoryException.capacityExceeded(
            "GA area " + area.getId() + ": allocated " + totalAllocated
            + " but capacity is " + area.getCapacity()
        );
    }
}

inventoryGaRepository.saveAll(gaInventories);
```

Thêm `InventoryException.capacityExceeded(String detail)`:

```java
public static ApiException capacityExceeded(String detail) {
    return new ApiException(CONFLICT, "INVENTORY_CAPACITY_EXCEEDED", detail);
}
```

---

## Database Migrations

### `V23__create_ticket_type.sql`

```sql
CREATE TABLE ticket_type (
    id          UUID        NOT NULL DEFAULT uuidv7() PRIMARY KEY,
    event_id    UUID        NOT NULL,   -- soft ref → event(id)
    name        VARCHAR(255) NOT NULL,
    slug        VARCHAR(255) NOT NULL,
    description TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_ticket_type_event_slug UNIQUE (event_id, slug)
);

CREATE INDEX idx_ticket_type_event_id ON ticket_type(event_id);
```

### `V24__migrate_offer_ticket_type_id.sql`

Migration 3 bước để chuyển `offer.ticket_type_id` từ VARCHAR sang UUID FK:

```sql
-- Bước 1: Tạo một ticket_type row cho mỗi (event_id, ticket_type_id) hiện có trong offer
INSERT INTO ticket_type (event_id, name, slug)
SELECT DISTINCT
    event_id,
    ticket_type_id AS name,   -- giữ nguyên string cũ làm name
    ticket_type_id AS slug    -- và slug
FROM offer;

-- Bước 2: Thêm cột UUID tạm
ALTER TABLE offer ADD COLUMN ticket_type_uuid UUID;

-- Bước 3: Điền UUID từ ticket_type vừa tạo
UPDATE offer o
SET ticket_type_uuid = (
    SELECT tt.id
    FROM ticket_type tt
    WHERE tt.event_id = o.event_id
      AND tt.slug     = o.ticket_type_id
);

-- Bước 4: Drop cột cũ, rename cột mới, thêm NOT NULL
ALTER TABLE offer DROP COLUMN ticket_type_id;
ALTER TABLE offer RENAME COLUMN ticket_type_uuid TO ticket_type_id;
ALTER TABLE offer ALTER COLUMN ticket_type_id SET NOT NULL;

-- Bước 5: Drop index cũ dùng VARCHAR, tạo index mới
DROP INDEX IF EXISTS idx_offer_event_ticket_type_id;
CREATE INDEX idx_offer_ticket_type_id ON offer(ticket_type_id);
```

> **Note:** Không tạo FK constraint `REFERENCES ticket_type(id)` vì project dùng
> soft reference giữa các entity. Integrity được enforce ở application layer.

---

## Impact trên Plan 016 (Ticket)

Ticket entity trong plan 016 phải snapshot:

```java
@Column(name = "ticket_type_id", nullable = false)
UUID ticketTypeId;          // snapshot UUID từ Offer.ticketTypeId lúc issuance

@Column(name = "ticket_type_name", nullable = false, length = 255)
String ticketTypeName;      // snapshot TicketType.name để display không cần join

@Column(name = "offer_name", nullable = false, length = 255)
String offerName;           // snapshot Offer.name

@Column(name = "face_value", nullable = false, precision = 19, scale = 2)
BigDecimal faceValue;       // snapshot từ OrderItem.unitFaceValue

@Column(nullable = false, length = 8)
String currency;            // snapshot từ OrderItem.currency
```

`TicketIssuanceService` cần fetch thêm `TicketType` (qua `Offer.ticketTypeId`) để lấy
`ticketTypeName`. Nên enrich vào `OrderItemSnapshot` lúc `OrderService` publish event
để tránh N+1 query trong issuance.

---

## Acceptance Criteria

| # | Criterion |
|---|---|
| 1 | `ticket_type` table tồn tại với constraint `UNIQUE (event_id, slug)`. |
| 2 | `offer.ticket_type_id` là UUID, không phải VARCHAR. |
| 3 | Tạo offer với `ticketTypeId` không thuộc event → 404. |
| 4 | Hai offer cùng event có thể share cùng `ticketTypeId` (cùng TicketType). |
| 5 | Xóa TicketType đang có offer tham chiếu → 409. |
| 6 | ONSALE transition với GA offers tổng capacity vượt area.capacity → 409 `INVENTORY_CAPACITY_EXCEEDED`. |
| 7 | ONSALE transition với GA offers tổng capacity ≤ area.capacity → init thành công. |
| 8 | Data migration: mọi offer hiện có đều có `ticket_type_id` UUID hợp lệ sau V24. |
| 9 | `./mvnw verify` passes. |

---

## Verification Plan

```powershell
cd api
./mvnw compile -q
./mvnw test -Dtest=OfferControllerIT,InventoryControllerIT
./mvnw spring-boot:process-aot
./mvnw verify
```
