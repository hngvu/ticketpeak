# 021 — Attraction Detail Page (ADP) — Web

## Description

Implement trang chi tiết attraction (nghệ sĩ / ban nhạc / đội thể thao).

Layout **100% clone Ticketmaster ADP** — bao gồm hero banner với thông tin artist, danh sách sự kiện dạng accordion-row, và section biography bên dưới.

URL mẫu tham chiếu: `ticketmaster.com/aespa-tickets/artist/2887549`

---

## Route

### URL Pattern

Ticketmaster dùng pattern: `/{slug}/artist/{id}`

```
/aespa-tickets/artist/2887549
 ─────────────  ──────  ───────
 [slug]         literal  [id]
```

SvelteKit route tương ứng:

```
web/src/routes/[slug]/artist/[id]/
├── +page.svelte
└── +page.server.ts
```

**Xóa route cũ:** `web/src/routes/attractions/[id]-[slug]/` — sai pattern, xóa toàn bộ folder này.

### Route Priority

SvelteKit ưu tiên static segments trước dynamic. Các route hiện có (`/discover`, `/search`, `/auth`, `/b2b`, `/ops`, `/venues`, `/demo`) **không bị ảnh hưởng** vì static routes luôn thắng `[slug]`. Route `[slug]/artist/[id]` chỉ match khi segment thứ 2 là literal `"artist"`.

### Params

- `[slug]` = slug tên nghệ sĩ (ví dụ: `aespa-tickets`) — chỉ dùng cho SEO/display, không gọi API.
- `[id]` = UUID của attraction — dùng để gọi API.

### Link generation

Khi link đến trang này từ các trang khác (EventCard, search results…):

```ts
href={`/${attraction.slug}/artist/${attraction.id}`}
// → /aespa-tickets/artist/abc-123-uuid
```

---

## Ticketmaster ADP — Layout Analysis

### Tổng quan cấu trúc (từ trên xuống dưới)

```
┌──────────────────────────────────────────────────────────────┐
│ [1] NAV BAR (sticky, 64px)                                   │
│     Logo · Category pills · Search bar · Sign in            │
├──────────────────────────────────────────────────────────────┤
│ [2] ARTIST HERO                                              │
│     Background: blurred artist image full-width (h-64 md:h-80)│
│     Overlay gradient: rgba(0,0,0,0) → rgba(0,0,0,0.75) 100% │
│     ┌─── max-w-[1400px] mx-auto px-4 ───────────────────┐   │
│     │                                                    │   │
│     │  [Avatar 80px circular]                            │   │
│     │  Artist Name          ← display-lg / on-primary    │   │
│     │  Genre · Sub-genre    ← caption / muted white      │   │
│     │                                                    │   │
│     │  [♡ Follow]  [N Followers]                         │   │
│     │                                                    │   │
│     └────────────────────────────────────────────────────┘   │
├──────────────────────────────────────────────────────────────┤
│ [3] EVENTS SECTION                                           │
│     max-w-[1400px] mx-auto px-4 py-8                        │
│                                                              │
│  HEADER ROW:                                                 │
│  "N Events"             [📍 Location filter ▾]              │
│   display-sm / ink      dropdown → filter by city            │
│  ─────────────────────────────────────────────────────────  │
│                                                              │
│  EVENT ROW (repeat for each event, divider between each):   │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ [DAY   ] [EVENT TITLE (bold, truncated 1 line)    ]  │   │
│  │ [BLOCK ] [Venue Name · City, State          ] [CTA] │   │
│  │          [On partner site badge — if external]       │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                              │
│  [ACCORDION EXPANSION — click row to expand]:               │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  [thumb 80px]  Event Title                           │   │
│  │                Date · Time · Venue · City            │   │
│  │                Prices from $XX                       │   │
│  │                [Find Tickets ─────────────────────►] │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                              │
│  [Load more events] button (nếu còn trang tiếp theo)        │
├──────────────────────────────────────────────────────────────┤
│ [4] ABOUT SECTION                                            │
│     "About [Artist Name]"    ← display-md / ink             │
│     Bio text (truncated 4 lines, "Read more" toggle)         │
│     body-md / body                                           │
├──────────────────────────────────────────────────────────────┤
│ [5] FOOTER                                                   │
└──────────────────────────────────────────────────────────────┘
```

---

## Chi tiết từng section

### [2] Artist Hero

```
┌──────────────────────────────────────────────────────────────┐
│  [FULL WIDTH BLURRED/DARKENED BACKGROUND IMAGE]              │
│  height: 256px (md: 320px)                                   │
│  image: attraction.imageUrl, object-cover, brightness-50     │
│  overlay: linear-gradient(to bottom,                         │
│    rgba(0,0,0,0.05) 0%,                                      │
│    rgba(0,0,0,0.70) 100%)                                    │
│                                                              │
│  Content (absolute, bottom-0, px-4 pb-6):                   │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  [Avatar]                                            │   │
│  │   80×80px, rounded-full                              │   │
│  │   border-2 border-white/30                           │   │
│  │   object-cover                                       │   │
│  │   mb-3                                               │   │
│  │                                                      │   │
│  │  aespa                          ← text-white         │   │
│  │  display-lg (32px/600)                               │   │
│  │  tracking-tight                                      │   │
│  │                                                      │   │
│  │  K-Pop · Pop                    ← text-white/70      │   │
│  │  body-sm (14px)                                      │   │
│  │  mt-1                                                │   │
│  │                                                      │   │
│  │  [♡ Follow]        66,949 Followers                  │   │
│  │   button-secondary-sm (white/20 bg,                  │   │
│  │   white text, rounded-pill)                          │   │
│  │   + caption text-white/60 ml-3                       │   │
│  └──────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────┘
```

**Follow button state:**
- Unfollow → `♡ Follow` (white outline, fill transparent)
- Following → `♥ Following` (white fill, checkmark icon optional)
- Toggle gọi API (future feature, Phase 5); hiện tại chỉ visual toggle.

---

### [3] Events Section — Event Row

Đây là phần quan trọng nhất, cần clone **chính xác** structure của Ticketmaster:

#### Event Row (collapsed — default state)

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│  [DATE    ]  EVENT TITLE (font-bold, text-ink)  [Find      │
│  [BLOCK   ]  Venue · City, State (text-mute)    [Tickets]  │
│              [On partner site] (badge nếu external)         │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

**Date Block (LEFT — fixed width 56px):**
```
┌──────────┐
│  MON     │  ← caption-mono / primary
│  14      │  ← display-sm / ink (số ngày to)
│  SEP     │  ← caption-mono / mute
└──────────┘
```

- Background: `canvas-soft`
- Border: `hairline`
- Rounded: `rounded-md`
- Width: 56px, shrink-0
- Font: monospace cho tất cả text trong block

**Center content:**
- Line 1: Event title — `body-md-strong` / `ink`, max 1 line, truncate
- Line 2: `{venueName} · {city}, {state}` — `body-sm` / `mute`
- Line 3 (optional): `[On partner site]` badge nếu `isExternal === true`
  - Background `canvas-soft-2`, text `mute`, rounded-full, caption

**Right CTA:**
- `[Find Tickets →]` button — `button-primary-sm` (`bg-primary text-on-primary rounded-pill`)
- Width: shrink-0, min-w-[120px]

**Row container:**
- `flex items-center gap-4 px-0 py-4`
- `border-b border-hairline`
- Cursor pointer
- Hover: `bg-canvas-soft/50` transition

---

#### Event Row (expanded — accordion state)

Click vào row → expand panel chi tiết bên dưới:

```
┌─────────────────────────────────────────────────────────────┐
│  [collapsed row content — như trên]                   [▼]  │
├─────────────────────────────────────────────────────────────┤
│  EXPANDED PANEL (bg canvas-soft, p-4):                      │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ [thumb      ]  aespa LIVE TOUR - SYNK : COMPLæXITY  │  │
│  │ [80×80      ]  FRI, SEP 18 · 8:00 PM                │  │
│  │ [rounded-md ]  UBS Arena · Belmont Park, NY          │  │
│  │              Prices from $125                         │  │
│  │                                                      │  │
│  │              [Find Tickets ───────────────────────►] │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

- Thumbnail: 80×80px, `rounded-md`, event banner image
- Title: `body-md-strong` / `ink`
- Date+Time: `body-sm` / `mute` ("FRI, SEP 18 · 8:00 PM")
- Venue·City: `body-sm` / `mute`
- "Prices from $XX": `body-sm-strong` / `ink` (nếu có, else omit)
- CTA: `button-primary` full, `rounded-pill`, `h-10`
- Chevron header row: rotate 180° khi expand

Chỉ 1 row expand tại một thời điểm — đóng row cũ khi mở row mới.

---

#### Section Header + Location Filter

```
┌──────────────────────────────────────────────────────────────┐
│  "24 Events"                    [📍 All Locations ▾]        │
│   display-sm / ink              dropdown filter              │
│                                                              │
│  (nếu filter active):                                       │
│  "5 Events near Ho Chi Minh City"                            │
└──────────────────────────────────────────────────────────────┘
```

- Dropdown: unique cities từ events list + "All Locations" default
- Filter client-side, không reload page
- State: local Svelte `$state`, không URL param

---

#### Load more

- Page size = 10, mặc định hiện 10 events đầu
- `[Load more events]` button centered, `button-secondary`
- Click → fetch page tiếp, append vào list (không replace)
- Ẩn button khi đã load hết

---

### [4] About Section

- Heading: `"About {name}"` — `display-md` / `ink`
- Bio: `line-clamp-4` mặc định, toggle Read more/less
- Chỉ hiện toggle nếu bio > 300 ký tự

---

## Route Structure (SvelteKit)

### `+page.server.ts`

```ts
export const load: PageServerLoad = async ({ params, fetch }) => {
  const { id } = params  // UUID từ URL

  const [attraction, eventsPage] = await Promise.all([
    fetch(`/api/attractions/${id}`).then(r => r.json()),
    fetch(`/api/events?attractionId=${id}&page=0&size=10&sort=startAt,asc`).then(r => r.json())
  ])

  return {
    attraction: attraction.data,
    initialEvents: eventsPage.data.content,
    totalEvents: eventsPage.data.totalElements,
    totalPages: eventsPage.data.totalPages
  }
}
```

### `+page.svelte` State

```ts
let expandedEventId = $state<string | null>(null)
let locationFilter = $state<string>('all')
let events = $state(data.initialEvents)
let page = $state(0)
let loading = $state(false)
let bioExpanded = $state(false)

const uniqueCities = $derived([...new Set(events.map(e => e.cityName).filter(Boolean))])

const filteredEvents = $derived(
  locationFilter === 'all' ? events : events.filter(e => e.cityName === locationFilter)
)
```

### Load more

```ts
async function loadMore() {
  loading = true
  const res = await fetch(`/api/events?attractionId=${data.attraction.id}&page=${page + 1}&size=10&sort=startAt,asc`)
  const json = await res.json()
  events = [...events, ...json.data.content]
  page++
  loading = false
}
```

---

## Components

| Component | Path | Mô tả |
|---|---|---|
| `AttractionHero.svelte` | `lib/components/catalog/` | Hero banner với artist info + follow |
| `EventAccordionList.svelte` | `lib/components/catalog/` | Wrapper list + header + location filter |
| `EventAccordionRow.svelte` | `lib/components/catalog/` | Một row (collapsed + expanded panel) |
| `LocationFilterDropdown.svelte` | `lib/components/catalog/` | Dropdown city filter |
| `AttractionBio.svelte` | `lib/components/catalog/` | Bio text với Read more toggle |

**Không dùng:**
- `EventCardHorizontal.svelte` — structure khác, thay bằng `EventAccordionRow`
- `PaginationBar.svelte` — thay bằng Load more button
- `EventCard.svelte` — không dùng trên trang này

**Dùng lại:**
- `EmptyState.svelte` — khi filter không có kết quả

---

## API Endpoints

| Action | Method | Endpoint |
|---|---|---|
| Get attraction | GET | `/api/attractions/{id}` |
| Get events (SSR, page 0) | GET | `/api/events?attractionId={id}&page=0&size=10&sort=startAt,asc` |
| Load more (client) | GET | `/api/events?attractionId={id}&page={p}&size=10&sort=startAt,asc` |

---

## SEO

- `<title>`: `{attractionName} Tickets & Tour Dates | Ticketpeak`
- `<meta name="description">`: `Find {attractionName} tickets for upcoming concerts and events. Browse all tour dates on Ticketpeak.`
- `<link rel="canonical" href="/{slug}/artist/{id}">` — tránh duplicate khi slug thay đổi

---

## Responsive Behavior

| Breakpoint | Behavior |
|---|---|
| Mobile (< 640px) | Hero h-56, avatar ẩn, date block w-12, expanded panel stack dọc (thumbnail trên, details dưới, CTA full-width) |
| Tablet (640–1024px) | Hero h-64, avatar hiện, layout như desktop nhưng hẹp hơn |
| Desktop (> 1024px) | Full layout, max-w-[1400px] centered |

---

## Design System Mapping

| Element | Token |
|---|---|
| Hero background | `ink` (#303841) + blurred image |
| Hero overlay | `rgba(0,0,0,0.05)` → `rgba(0,0,0,0.70)` gradient |
| Hero text | `on-primary` (white) |
| Hero genre text | `white/70` |
| Follow button | `white/20` bg, `white/30` border |
| Follower count | `white/60` / `caption` |
| Page bg | `canvas-soft` |
| Event row bg | `canvas` |
| Event row hover | `canvas-soft/50` |
| Row divider | `hairline` |
| Date block bg | `canvas-soft`, border `hairline` |
| Date day number | `ink` / `display-sm` |
| Date month/weekday | `primary` / `mute` / `caption-mono` |
| Event title | `ink` / `body-md-strong` |
| Venue·city | `mute` / `body-sm` |
| "On partner site" | `canvas-soft-2` bg / `mute` text |
| Find Tickets button | `button-primary-sm` |
| Chevron | `mute` |
| Expanded panel bg | `canvas-soft` |
| Prices from | `ink` / `body-sm-strong` |
| About heading | `ink` / `display-md` |
| Bio text | `body` / `body-md` |
| Read more | `primary` / `body-sm-strong` |
| Load more button | `button-secondary` |

---

## Acceptance Criteria

- [ ] URL pattern đúng: `/{slug}/artist/{id}` — xóa `attractions/[id]-[slug]` cũ.
- [ ] `params.id` dùng để gọi API; `params.slug` chỉ dùng cho SEO display.
- [ ] Các route static hiện tại (`/discover`, `/search`…) không bị ảnh hưởng.
- [ ] Hero: blurred bg image, gradient overlay, avatar, tên, genre, follow button + follower count.
- [ ] Follow button toggle hoạt động (visual only).
- [ ] Event list: accordion rows với `border-b` divider.
- [ ] Date block: MONTH / DAY (số to) / WEEKDAY, mono font.
- [ ] Click row → expand; click lại → collapse; mở row khác → đóng row cũ.
- [ ] Expanded panel: thumbnail, title, date+time, venue+city, price, Find Tickets CTA.
- [ ] "On partner site" badge hiện khi `isExternal === true`.
- [ ] Location filter dropdown lọc client-side; heading "N Events" cập nhật theo filter.
- [ ] Load more append events, ẩn button khi hết trang.
- [ ] Loading state trên Load more button.
- [ ] EmptyState khi filter rỗng.
- [ ] About bio truncate 4 dòng + Read more/less toggle.
- [ ] SSR với parallel fetch.
- [ ] `<title>`, `<meta description>`, `<link rel="canonical">` set đúng.
- [ ] Responsive: mobile avatar ẩn, expanded panel stack dọc.
- [ ] Tokens match DESIGN.md.

---

## Status
 
`done`
 
## Outcome
 
Fully implemented on SvelteKit and successfully verified:
1. **URL Route Schema**: Deleted the old dynamic route `web/src/routes/attractions/[id]-[slug]/` and created the correct SvelteKit route `web/src/routes/[slug]/artist/[id]/` matching the exact pattern `/{slug}/artist/{id}` without affecting other static routes.
2. **Parallel SSR Fetching (`+page.server.ts`)**: Loads the artist profile `/api/attractions/{id}` and hosted events list `/api/events` simultaneously at load time. Supports dynamic fallback to high-fidelity mock data if the database or API is offline.
3. **Immersive Hero Banner (`AttractionHero.svelte`)**: Features high-fidelity layout with full-width blurred background image, circular profile avatar, genre metadata tags, follow action toggle, and dynamic followers count increment.
4. **Interactive Accordion List (`EventAccordionRow.svelte`)**: Implemented compact event rows containing month/day blocks, event details, and a primary CTA. Rows expand dynamically to display detailed panels (thumbnails, time, prices, large CTA) on click, closing other active rows.
5. **Client-side Filter & Biography (`LocationFilterDropdown.svelte` & `AttractionBio.svelte`)**: Built custom select location filter dropdown matching active hosted event cities and biography segment with collapsible "Read More" button.

