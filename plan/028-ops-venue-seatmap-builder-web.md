# 028 — Ops Venue & Seat Map Builder — Web

## Description

Xây dựng giao diện Admin (ops) để tạo và quản lý venue, manifest, và seat map.

Backend đã có đầy đủ API tại `/api/ops/venues` (Plan 008, done). Vấn đề là hiện tại admin phải dùng raw API calls (Postman/curl) để tạo venue — không có UI nào cả. Plan này làm giao diện web tại `/ops/venues`.

**Hai loại layout venue cần support:**

1. **GA (General Admission)** — không có ghế cụ thể, chỉ có sức chứa. Ví dụ: standing area, festival ground.
2. **RS (Reserved Seating)** — có sơ đồ ghế cụ thể như ảnh UBS Arena, từng ghế có tọa độ `(positionX, positionY)`.

---

## Route

```
/ops/venues                        — danh sách venue
/ops/venues/new                    — tạo venue mới
/ops/venues/[id]                   — venue detail + manifest list
/ops/venues/[id]/manifests/new     — tạo manifest mới
/ops/venues/[id]/manifests/[mid]   — manifest editor (seat map builder)
```

Nằm trong `/ops/` → `isAuthOrPortal = true` → skip consumer nav, dùng ops layout riêng.

---

## SvelteKit Files

```
web/src/routes/ops/
├── venues/
│   ├── +page.svelte                          (venue list)
│   ├── +page.server.ts
│   ├── new/
│   │   ├── +page.svelte                      (create venue form)
│   │   └── +page.server.ts
│   └── [id]/
│       ├── +page.svelte                      (venue detail)
│       ├── +page.server.ts
│       └── manifests/
│           ├── new/
│           │   ├── +page.svelte              (create manifest form)
│           │   └── +page.server.ts
│           └── [mid]/
│               ├── +page.svelte              (manifest editor / seat map builder)
│               └── +page.server.ts
```

---

## Chi Tiết Từng Màn Hình

---

### Screen 1 — Venue List `/ops/venues`

```
┌─────────────────────────────────────────────────────────────┐
│ Venues                                     [+ New Venue]    │
├──────────────────┬───────────────┬──────────┬──────────────┤
│ Name             │ City          │ Capacity │ Status       │
├──────────────────┼───────────────┼──────────┼──────────────┤
│ Sân Mỹ Đình      │ Hà Nội        │ 40,000   │ ACTIVE  ●    │
│ GEM Center       │ Hồ Chí Minh   │ 1,200    │ ACTIVE  ●    │
│ Nhà hát lớn HN   │ Hà Nội        │ 500      │ INACTIVE ○   │
└──────────────────┴───────────────┴──────────┴──────────────┘
```

- Fetch `GET /api/ops/venues` (cần thêm endpoint list nếu chưa có — xem note)
- Row click → navigate đến `/ops/venues/[id]`
- Status badge: `ACTIVE` = green dot, `INACTIVE` = gray dot

---

### Screen 2 — Create Venue `/ops/venues/new`

Form đơn giản, submit `POST /api/ops/venues`:

| Field | Type | Notes |
|---|---|---|
| Name | text | required |
| Slug | text | auto-generated từ name, editable |
| Description | textarea | optional |
| Address | text | required |
| City | text | required |
| Country | text | default "VN" |
| Capacity | number | required |
| Image URL | text | optional |

Submit → redirect về `/ops/venues/[id]`.

---

### Screen 3 — Venue Detail `/ops/venues/[id]`

```
┌──────────────────────────────────────────────────────────────┐
│  ← Back   GEM Center                   [Activate] [Edit]    │
│           Hồ Chí Minh · 1,200 seats                         │
├──────────────────────────────────────────────────────────────┤
│  MANIFESTS                              [+ New Manifest]     │
├───────────────┬────────────┬────────────┬────────────────────┤
│ Description   │ Capacity   │ Status     │ Actions            │
├───────────────┼────────────┼────────────┼────────────────────┤
│ Concert 2024  │ 1,200      │ PUBLISHED  │ [Edit] [Clone]     │
│ Theater Round │ 800        │ DRAFT      │ [Edit] [Publish]   │
│ Old Layout    │ 1,000      │ ARCHIVED   │ [Clone]            │
└───────────────┴────────────┴────────────┴────────────────────┘
```

Actions per manifest:
- `DRAFT` → [Edit] [Publish] [Delete]
- `PUBLISHED` → [Edit (readonly)] [Archive] [Clone]
- `ARCHIVED` → [Clone]

---

### Screen 4 — Manifest Editor `/ops/venues/[id]/manifests/[mid]`

Đây là màn hình phức tạp nhất — **Seat Map Builder**.

Layout tổng thể:

```
┌──────────────────────────────┬───────────────────────────────┐
│ LEFT PANEL (300px)           │ RIGHT PANEL — CANVAS          │
│ Lookup Tables                │                               │
│ ─────────────────────────    │   [SVG seat map render]       │
│ Levels   [+ Add]             │                               │
│  • Floor (blue)              │   Click section → highlight   │
│  • Upper (gray)              │   Click seat → select         │
│                              │                               │
│ Sections  [+ Add]            │                               │
│  • Section 101               │                               │
│  • Section 102               │                               │
│                              │                               │
│ Price Levels  [+ Add]        │                               │
│  • VIP (#FFD700)             │                               │
│  • Standard (#3B82F6)        │                               │
│  • GA (#6B7280)              │                               │
│ ─────────────────────────    │                               │
│ Areas                        │                               │
│ ─────────────────────────    │                               │
│ GA Areas  [+ Add GA Area]    │                               │
│  • Standing (500 cap)        │                               │
│                              │                               │
│ RS Areas  [+ Add RS Area]    │                               │
│  • Section 101 [+ Add Row]   │                               │
│    ▶ Row A (20 seats) [+]    │                               │
│    ▶ Row B (20 seats) [+]    │                               │
└──────────────────────────────┴───────────────────────────────┘
```

#### Left Panel — Lookup Tables

**Levels:** `PUT /api/ops/venues/manifests/{manifestId}/levels`
- Name (text), Color (color picker hex), Save

**Sections:** `PUT /api/ops/venues/manifests/{manifestId}/sections`
- Name (text), Level (select từ levels đã tạo), Color (hex), Save

**Price Levels:** `PUT /api/ops/venues/manifests/{manifestId}/price-levels`
- Name (text), Color (hex), Save

#### Left Panel — Areas

**GA Area:** `POST /api/ops/venues/manifests/{manifestId}/ga-areas`
- Name (text), Capacity (number), Section (select), Price Level (select)

**RS Area:** `POST /api/ops/venues/manifests/{manifestId}/rs-areas`
- Name (text), Section (select), Price Level (select)
- Expand để xem/thêm Rows

**Seat Rows:** `POST /api/ops/venues/rs-areas/{rsAreaId}/rows`
- Row name (text, e.g. "A", "B", "01"), Position Y (number)
- Sau khi tạo row → [+ Add Seats] mở dialog bulk add seats

**Bulk Add Seats Dialog:**
```
Row A — Add Seats
─────────────────────────────────
Seat count:  [ 20  ]
Start name:  [ 1   ]  (auto: 1, 2, 3... hoặc 001, 002...)
Position X start: [0]  step: [1]

☐ Aisle seats (comma list): 1, 20
☐ Accessible seats: _____

[Cancel]  [Add 20 seats →]
```
Loop `POST /api/ops/venues/rows/{rowId}/seats` cho từng ghế.

#### Right Panel — Canvas (SVG Seat Map)

Render visual seat map từ data đã có:

```
Mode 1 — Section View (zoom out):
  Vẽ các RS area / GA area như blocks màu theo priceLevel.color
  Label section name
  Click block → zoom in vào section đó

Mode 2 — Seat View (zoom in, sau khi click section):
  Render từng seat circle theo (positionX, positionY)
  Màu theo SeatStatus: AVAILABLE=blue, HOLD=amber, RESERVED=gray
  Accessible seats có icon ♿
  Hover → tooltip: "Row A · Seat 5"
  Click → select seat (sidebar hiện seat detail)
```

**Coordinate system:**
- `positionX` = cột (0 = trái)
- `positionY` = hàng (0 = trên)
- Canvas render: `cx = padding + positionX * seatSize`, `cy = padding + positionY * seatSize`
- `seatSize` default = 24px, zoom in = 32px

**Manifest status header:**
```
Draft  →  [Publish Manifest]  [Save Changes]
Published →  [Archive]  [Clone]  (read-only mode)
```

---

## API Endpoints Cần Thêm (Backend)

Hiện tại `OpsVenueController` **thiếu `GET /api/ops/venues`** (list all venues). Cần thêm:

```java
@GetMapping
public ResponseEntity<ApiResponse<List<VenueResponse>>> listVenues() {
    return ResponseEntity.ok(ApiResponse.success(venueService.listVenues(), "OK"));
}

@GetMapping("/{id}")
public ResponseEntity<ApiResponse<VenueResponse>> getVenue(@PathVariable UUID id) {
    return ResponseEntity.ok(ApiResponse.success(venueService.getVenue(id), "OK"));
}
```

Kiểm tra `VenueService` xem đã có `listVenues()` / `getVenue()` chưa trước khi thêm.

---

## Data Flow Tạo Seat Map (Ví Dụ GEM Center)

```
1. Tạo Venue: POST /api/ops/venues
   → { name: "GEM Center", capacity: 1200, ... }

2. Tạo Manifest: POST /api/ops/venues/{venueId}/manifests
   → { description: "Concert Layout", totalCapacity: 1200 }

3. Tạo Levels:
   PUT .../levels  { name: "Floor", color: "#3B82F6" }
   PUT .../levels  { name: "Balcony", color: "#6B7280" }

4. Tạo Sections:
   PUT .../sections  { name: "Section A", levelId: "floor-id", color: "#3B82F6" }
   PUT .../sections  { name: "Section B", levelId: "floor-id", color: "#60A5FA" }

5. Tạo Price Levels:
   PUT .../price-levels  { name: "VIP", color: "#FFD700" }
   PUT .../price-levels  { name: "Standard", color: "#3B82F6" }

6. Tạo RS Area:
   POST .../rs-areas  { name: "Section A RS", sectionId: "...", priceLevelId: "..." }

7. Tạo Rows:
   POST .../rs-areas/{id}/rows  { name: "A", positionY: 0 }
   POST .../rs-areas/{id}/rows  { name: "B", positionY: 1 }

8. Tạo Seats (bulk):
   POST .../rows/{rowId}/seats  { name: "1", positionX: 0 }
   POST .../rows/{rowId}/seats  { name: "2", positionX: 1 }
   ...

9. Publish Manifest:
   POST .../manifests/{id}/publish
```

---

## Seat Map Canvas — Kỹ Thuật

Dùng SVG inline trong Svelte (không cần thư viện bên ngoài):

```svelte
<svg viewBox="0 0 {canvasWidth} {canvasHeight}" class="w-full">
  {#each seats as seat}
    <circle
      cx={padding + seat.positionX * SEAT_SIZE}
      cy={padding + seat.row.positionY * SEAT_SIZE}
      r={SEAT_RADIUS}
      fill={seatColor(seat.status)}
      stroke={isSelected(seat) ? '#fff' : 'none'}
      stroke-width="2"
      class="cursor-pointer transition-opacity hover:opacity-80"
      onclick={() => selectSeat(seat)}
    >
      <title>Row {seat.row.name} · Seat {seat.name}</title>
    </circle>
  {/each}
</svg>
```

Canvas size tự động tính từ `max(positionX) * SEAT_SIZE + padding * 2`.

---

## Acceptance Criteria

- [ ] `/ops/venues` liệt kê tất cả venues, click row navigate đúng.
- [ ] Tạo venue mới qua form, redirect về detail page sau khi tạo.
- [ ] Venue detail hiện manifest list với status badges và action buttons đúng.
- [ ] Tạo manifest mới, redirect về manifest editor.
- [ ] Manifest editor: left panel load đúng Levels / Sections / Price Levels từ API.
- [ ] Thêm Level / Section / Price Level qua form inline, save gọi đúng `PUT` endpoint.
- [ ] Color picker cho Level/Section/Price Level, color preview hiện đúng.
- [ ] Tạo GA Area với capacity và mapping section/price level.
- [ ] Tạo RS Area, expand để thấy rows.
- [ ] Thêm Seat Row vào RS Area.
- [ ] Bulk add seats dialog: nhập count + start name → tạo đúng số ghế, đúng positionX.
- [ ] SVG canvas render seats theo positionX/positionY khi có data.
- [ ] Hover seat → tooltip "Row X · Seat Y".
- [ ] Publish Manifest button gọi đúng endpoint, manifest status update.
- [ ] PUBLISHED manifest → read-only mode (không edit lookup tables).
- [ ] Không crash khi manifest chưa có seat nào (empty canvas).
- [ ] Mobile: left panel collapse thành bottom sheet, canvas full width.

---

## Dependency

- Backend `GET /api/ops/venues` (list) và `GET /api/ops/venues/{id}` (get) cần thêm nếu chưa có.
- Tất cả write endpoints đã có trong `OpsVenueController` — không cần thêm.
- Ops layout (`/ops/+layout.svelte`) đã có — route tự skip consumer nav.

---

## Status

`planned`

## Outcome

_To be filled after implementation._
