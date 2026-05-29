# 019 — Catalog UI (Web)

## Description

Implement the buyer-facing event catalog for Ticketpeak web — the primary discovery surface where users browse, filter, and navigate to events. Modeled after Ticketmaster's catalog patterns but scoped to Ticketpeak's API and design system.

The catalog consists of six interconnected surfaces:

| Surface | Route | Purpose |
|---|---|---|
| Discover (landing) | `/discover` | Homepage-style browse: trending, by category, by city |
| Category page | `/discover/[category]` | Filtered browse by top-level category |
| Search results | `/search` | Full-text search results |
| Attraction page | `/attractions/[id]-[slug]` | All upcoming events for an artist/team (dùng UUID `id` để gọi API) |
| Venue page | `/venues/[id]-[slug]` | All upcoming events at a venue (dùng UUID `id` để gọi API) |

---

## Ticketmaster Catalog Analysis (Reference)

### Sitemap

```
ticketmaster.com/
├── /                                   → Homepage (hero, trending, categories)
├── /discover/
│   ├── /discover/concerts              → Music catalog
│   ├── /discover/sports                → Sports catalog
│   ├── /discover/arts-theater          → Arts & Theater catalog
│   └── /discover/family               → Family catalog
├── /search?q=...                       → Full-text search
├── /{artist-slug}-tickets/artist/{id}  → Artist detail (ADP)
└── /venueinfo/{venueId}                → Venue detail (VDP)
```

### URL Parameters — thực tế Ticketmaster

Ticketmaster rất đơn giản. Chỉ có **2 pattern URL**:

**Search page** (`/search`):
```
/search?q=coldplay&startDate=2025-06-01&endDate=2025-12-31&location=london
```

| Param | Ví dụ | Ghi chú |
|---|---|---|
| `q` | `coldplay` | Từ khóa tìm kiếm |
| `startDate` | `2025-06-01` | ISO date |
| `endDate` | `2025-12-31` | ISO date |
| `location` | `london` | Tên thành phố |

**Category / Discover page** (`/discover/[category]`):
```
/discover/concerts?classificationId=KnvZfZ7vAve
```

| Param | Ví dụ | Ghi chú |
|---|---|---|
| `classificationId` | `KnvZfZ7vAve` | ID genre/sub-genre để lọc trong category |

Filter sâu hơn (genre, sub-genre) chỉ là `classificationId` thay đổi — không có thêm param nào. Location được detect từ browser / profile, không phải URL param.

---

## Ticketpeak Adaptation

### Route Structure (SvelteKit)

```
web/src/routes/
├── +layout.svelte                      → Root shell (nav + footer)
├── +page.svelte                        → Marketing homepage (existing)
│
├── discover/
│   ├── +page.svelte                    → Discover landing
│   ├── +page.server.ts                 → Load trending + featured events
│   └── [category]/
│       ├── +page.svelte                → Category browse
│       └── +page.server.ts            → Load by category + optional classificationId
│
├── search/
│   ├── +page.svelte                    → Search results
│   └── +page.server.ts                → SSR search từ API
│
├── attractions/
│   └── [id]-[slug]/
│       ├── +page.svelte                → Attraction/artist page
│       └── +page.server.ts            → Load attraction + events
│
└── venues/
    └── [id]-[slug]/
        ├── +page.svelte                → Venue page
        └── +page.server.ts            → Load venue + events
```

### URL Params (Ticketpeak — giữ đơn giản như Ticketmaster)

**`/search`**

| Param | Type | Ví dụ | Maps to API | Ghi chú |
|---|---|---|---|---|
| `q` | string | `q=lễ+hội` | `GET /api/events?query=` | Ánh xạ sang tham số `query` của API |
| `startDate` | date | `startDate=2025-06-01` | `startAfter=` | Ánh xạ sang tham số `startAfter` (Instant ISO) |
| `endDate` | date | `endDate=2025-08-31` | `startBefore=` | Ánh xạ sang tham số `startBefore` (Instant ISO) |
| `location` | string | `location=ho-chi-minh` | `city=` | Ánh xạ sang tham số `city` |

**`/discover/[category]`**

| Param | Type | Ví dụ | Ghi chú |
|---|---|---|---|
| `classificationId` | string | `classificationId=abc123` | ID của sub-classification (Level 2), optional |

**Cơ cấu phân cấp Classification (2 cấp duy nhất theo Ticketmaster):**
- **Cấp 1 (Top Classification - Segment)**: Sử dụng **slug** trực tiếp trên URL path (ví dụ: `/discover/concerts`, `/discover/sports`).
- **Cấp 2 (Sub-classification - Genre/Sub-genre)**: Sử dụng **ID** truyền qua query parameter `?classificationId=` (ví dụ: `/discover/concerts?classificationId=KnvZfZ7vAve`).
- Hệ thống thiết kế tối giản đúng **2 cấp** này để phân loại danh mục sự kiện, không lồng thêm cấp sâu hơn.

**Category (Level 1) slugs:**

| Route slug | Display name |
|---|---|
| `concerts` | Concerts |
| `sports` | Sports |
| `arts` | Arts & Theater |
| `family` | Family |

---

## Page-by-Page Layout Spec

### 1. `/discover` — Discover Landing

```
┌─────────────────────────────────────────┐
│  NAV BAR (sticky, 64px)                 │
│  Logo | Category pills | Search | Auth  │
├─────────────────────────────────────────┤
│  HERO BAND (gradient-discover)          │
│  "Find your next live experience"       │  display-xl
│  [🔍 Search events, artists, venues…]  │  search input → /search?q=
│  [Concerts] [Sports] [Arts] [Family]    │  pill nav → /discover/[category]
├─────────────────────────────────────────┤
│  SECTION: Trending Near You             │  display-md
│  ← [card][card][card][card][card] →    │  horizontal scroll
├─────────────────────────────────────────┤
│  SECTION: This Weekend                  │  display-md
│  ← [card][card][card][card][card] →    │
├─────────────────────────────────────────┤
│  SECTION: Top Concerts in [City]        │  display-md
│  ← [card][card][card][card][card] →    │
├─────────────────────────────────────────┤
│  SECTION: Popular Venues                │  display-md
│  [venue card][venue card][venue card]   │  3-up grid
├─────────────────────────────────────────┤
│  FOOTER                                 │
└─────────────────────────────────────────┘
```

Data loading (`+page.server.ts`): 4 API calls song song:
- `GET /api/events?sort=startAt,asc&size=12` → trending (hoặc sort theo thuộc tính khác khả dụng)
- `GET /api/events?startAfter={friday}&startBefore={sunday}&size=12` → this weekend
- `GET /api/events?classificationId={concertsSegmentId}&city={userCity}&size=12` → top concerts (tìm theo classification ID của Concerts)
- `GET /api/venues?city={userCity}&size=6` → popular venues (tìm kiếm địa điểm theo tên hoặc lọc thủ công)

City: đọc từ cookie `preferred_city`; fallback `ho-chi-minh`.

---

### 2. `/discover/[category]` — Category Browse

```
┌─────────────────────────────────────────┐
│  NAV BAR                                │
├─────────────────────────────────────────┤
│  PAGE HERO (canvas-soft-2)              │
│  [Icon] Concerts                        │  display-lg
│  "Browse all concert events"            │  body-md / mute
├─────────────────────────────────────────┤
│  GENRE PILLS (horizontal scroll)        │
│  [All] [Rock] [Pop] [Jazz] [EDM] …     │
│  → click = navigate ?classificationId= │
├─────────────────────────────────────────┤
│  EVENT GRID (3-up desktop, gap-lg)      │
│  [card][card][card]                     │
│  [card][card][card]                     │
├─────────────────────────────────────────┤
│  PAGINATION  ← 1 [2] 3 … →             │
├─────────────────────────────────────────┤
│  FOOTER                                 │
└─────────────────────────────────────────┘
```

Không có filter bar phức tạp — chỉ genre pills đơn giản (như Ticketmaster).
Genre pills load từ `GET /classifications?segmentSlug=[category]`.
Click genre pill → `goto('/discover/concerts?classificationId=xyz', { replaceState: true })`.

---

### 3. `/search` — Search Results

```
┌─────────────────────────────────────────┐
│  NAV BAR                                │
├─────────────────────────────────────────┤
│  SEARCH BAR (full-width, retains query) │
│  [🔍 coldplay ________________________] │
├─────────────────────────────────────────┤
│  FILTER ROW (inline, không sidebar)     │
│  [📅 Date range ▾]  [📍 Location ▾]   │
│  "42 results for coldplay"              │  body-sm / mute
├─────────────────────────────────────────┤
│  EVENT GRID / LIST                      │
│  [card][card][card]                     │
│  [card][card][card]                     │
├─────────────────────────────────────────┤
│  PAGINATION                             │
├─────────────────────────────────────────┤
│  FOOTER                                 │
└─────────────────────────────────────────┘
```

Filter dropdowns (inline, không sidebar):
- **Date range:** presets (Any, Today, This weekend, This week, This month) + custom picker
- **Location:** text input với suggestion từ `GET /venues/cities?q=`

Mobile: filter dropdowns stack xuống dưới search bar, vẫn inline — không cần bottom sheet vì chỉ có 2 filter.

No results state: "No results for `coldplay`" + suggest xóa filter + "Browse all events" CTA.

---

### 4. `/attractions/[id]-[slug]` — Artist Page

```
┌─────────────────────────────────────────┐
│  ARTIST HERO (canvas-soft-2)            │
│  [avatar 80px]  Artist Name  display-lg │
│  Genre · Sub-genre           mute       │
│  [Follow ♡]                             │
├─────────────────────────────────────────┤
│  UPCOMING EVENTS  (12 events)           │
│  ┌──────────────────────────────────┐   │
│  │ [thumb] Title · SAT 14 JUN      │   │
│  │         Venue · City             │   │
│  │         From $25  [Tickets →]   │   │
│  └──────────────────────────────────┘   │
│  [Pagination]                           │
├─────────────────────────────────────────┤
│  FOOTER                                 │
└─────────────────────────────────────────┘
```

Events dùng `EventCardHorizontal` — thumbnail trái, info phải. Phù hợp hơn khi nhiều date của cùng 1 artist.

---

### 5. `/venues/[id]-[slug]` — Venue Page

```
┌─────────────────────────────────────────┐
│  VENUE HERO                             │
│  [banner image]                         │
│  Venue Name           display-lg        │
│  Address · Capacity   mute              │
├─────────────────────────────────────────┤
│  TAB BAR: [Events] [Info] [Seating]     │
├─────────────────────────────────────────┤
│  TAB EVENTS                             │
│  [EventCardHorizontal list]             │
│  [Pagination]                           │
│                                         │
│  TAB INFO                               │
│  Address / Phone / Transport / Parking  │
│                                         │
│  TAB SEATING                            │
│  [SVG seat map — static, pinch-zoom]    │
├─────────────────────────────────────────┤
│  FOOTER                                 │
└─────────────────────────────────────────┘
```

---

## Shared Components

| Component | Path | Notes |
|---|---|---|
| `EventCard.svelte` | `lib/components/catalog/` | Vertical grid card (16:9) |
| `EventCardHorizontal.svelte` | `lib/components/catalog/` | Horizontal list card |
| `VenueCard.svelte` | `lib/components/catalog/` | Venue thumbnail card |
| `CategoryPills.svelte` | `lib/components/catalog/` | Nav pills (Concerts, Sports…) |
| `GenrePills.svelte` | `lib/components/catalog/` | Genre pills trên category page |
| `SearchBar.svelte` | `lib/components/catalog/` | Search input |
| `DateRangeDropdown.svelte` | `lib/components/catalog/` | Presets + custom picker |
| `LocationDropdown.svelte` | `lib/components/catalog/` | City typeahead |
| `PaginationBar.svelte` | `lib/components/catalog/` | Page-based nav |
| `HorizontalScroll.svelte` | `lib/components/catalog/` | Scroll row wrapper |
| `EmptyState.svelte` | `lib/components/common/` | No results |

---

## API Endpoints Used

| Surface | Method | Endpoint |
|---|---|---|
| Discover trending | GET | `/api/events?sort=startAt,asc&size=12` |
| Discover this weekend | GET | `/api/events?startAfter={fri}&startBefore={sun}&size=12` |
| Category browse | GET | `/api/events?classificationId={id}&page={p}` |
| Genre list | GET | `/api/classifications` |
| Search | GET | `/api/events?query={q}&startAfter={from}&startBefore={to}&city={loc}&page={p}` |
| City suggestions | GET | _Dùng danh sách thành phố tĩnh ở client-side (Hồ Chí Minh, Hà Nội...)_ |
| Attraction + events | GET | `/api/attractions/{id}` + `/api/events?attractionId={id}&page={p}` |
| Venue + events | GET | `/api/venues/{id}` + `/api/events?venueId={id}&page={p}` |

---

## State Management

- URL params là **source of truth** duy nhất cho filter state. Không dùng Svelte store cho filter.
- Filter thay đổi → `goto(url, { replaceState: true })`.
- City preference: cookie `preferred_city`, đọc server-side trong `+layout.server.ts`.
- TanStack Query cho data fetching client-side (nếu cần refetch sau navigation).

---

## Performance

- Tất cả catalog pages: **SSR** (`+page.server.ts`) cho SEO.
- EventCard images: `loading="lazy"` trừ 6 card đầu tiên.
- EventCard: fixed 16:9 aspect container — không shift khi ảnh load.
- Pagination (không infinite scroll) — đơn giản hơn, SEO tốt hơn, back-navigate được.

---

## Design System Mapping

| Element | Token |
|---|---|
| Hero bg | `gradient-discover-start → gradient-discover-end` |
| Page bg | `canvas-soft` |
| Card surface | `canvas` |
| Card border | `hairline` |
| Card hover | Level 3 shadow + `hairline-strong` |
| Category/genre badge | `blue-accent-soft` bg + `blue-accent` text |
| Price | `primary` |
| Metadata | `mute` |
| Active genre pill | `primary` bg + `on-primary` text |
| Tab active | `primary` underline |

---

## Acceptance Criteria

- [ ] `/discover` renders 4 sections (trending, this weekend, top concerts, venues) với live data.
- [ ] `/discover/[category]` hiện genre pills; click genre → URL thêm `?classificationId=` và grid reload.
- [ ] `/search` hoạt động với `q`, `startDate`, `endDate`, `location`; filter thay đổi cập nhật URL.
- [ ] Tất cả filter state bookmarkable, shareable, browser-back hoạt động đúng.
- [ ] `/attractions/[slug]` + `/venues/[slug]` render đúng layout.
- [ ] Tất cả pages SSR.
- [ ] `EventCard` là component duy nhất dùng trên tất cả grid.
- [ ] Mobile (< 768px): 1-column grid, filter dropdowns inline.
- [ ] Tokens match DESIGN.md.
- [ ] Empty states xử lý trên tất cả surfaces.
- [ ] `<title>` và meta description set per route.

---

## Status
 
`done`
 
## Outcome
 
Fully implemented on SvelteKit client and Spring Boot api:
1. **Discover Landing (`/discover`)**: Implemented homepage rendering 4 dynamic sections: Trending, Weekend, Top Concerts, and Popular Venues using asynchronous parallel SSR queries.
2. **Category Page (`/discover/[category]`)**: Implemented dynamic genre pills with automatic layout hierarchy (Concerts, Sports, Arts, Family) mapping queries back to `/api/classifications`.
3. **Search Results (`/search`)**: Retains state with inline query parameters for `q`, `startDate`, `endDate`, and `location`. Supports date presets (Today, This Weekend) and custom pickers.
4. **Attraction & Venue Pages**: Implemented UUID-safe parsing for dynamic routes `attractions/[id]-[slug]` and `venues/[id]-[slug]` with fallbacks and list pagination.
5. **Shared Components**: Reusable components created under `lib/components/catalog/` (`EventCard`, `VenueCard`, `PaginationBar`, `HorizontalScroll`, etc.) adhering to `DESIGN.md`.

