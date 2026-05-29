# 022 — Event Detail Page (EDP) — Web

## Description

Implement trang chi tiết sự kiện (Event Detail Page) — điểm bắt đầu của purchase flow.

Người dùng xem thông tin event, chọn loại vé + số lượng, xem breakdown giá, rồi click "Get Tickets" để tạo reservation và chuyển sang checkout.

Layout **clone Ticketmaster EDP**, **bỏ seat map / venue** (user said "trừ venue") — chỉ implement list-view ticket selection.

URL mẫu tham chiếu:
`ticketmaster.com/aespa-live-tour-synk-complxity-in-belmont-park-new-york-09-18-2026/event/30006495A5694367`

---

## Route

### URL Pattern

Ticketmaster dùng: `/{slug}/event/{id}`

```
/aespa-live-tour-synk-complxity-in-belmont-park-new-york-09-18-2026/event/30006495A5694367
 ──────────────────────────────────────────────────────────────────  ─────  ────────────────
 [slug]                                                              literal  [id]
```

SvelteKit route:

```
web/src/routes/[slug]/event/[id]/
├── +page.svelte
└── +page.server.ts
```

Cùng root `[slug]` với plan 021 (`[slug]/artist/[id]`) — SvelteKit phân biệt bằng segment thứ 2 (`event` vs `artist`).

### Params
- `[slug]` = event slug — SEO only, không gọi API
- `[id]` = event UUID — gọi API

### Link generation
```ts
href={`/${event.slug}/event/${event.id}`}
```

---

## API Endpoints

| Step | Method | Endpoint | Ghi chú |
|---|---|---|---|
| Load event (SSR) | GET | `/api/events/{id}` | title, dates, venue, attractions, status |
| Load offers (SSR) | GET | `/api/events/{id}/offers` | danh sách ticket tiers |
| Load inventory (SSR) | GET | `/api/events/{id}/inventory` | available counts per offer/area |
| Create reservation (client) | POST | `/api/reservations` | khi user click "Get Tickets" |

### `+page.server.ts` — parallel fetch

```ts
export const load: PageServerLoad = async ({ params, fetch, locals }) => {
  const { id } = params

  const [eventRes, offersRes, inventoryRes] = await Promise.all([
    fetch(`/api/events/${id}`),
    fetch(`/api/events/${id}/offers`),
    fetch(`/api/events/${id}/inventory`)
  ])

  const [event, offers, inventory] = await Promise.all([
    eventRes.json(),
    offersRes.json(),
    inventoryRes.json()
  ])

  return {
    event: event.data,          // EventResponse
    offers: offers.data,        // OfferResponse[]
    inventory: inventory.data,  // EventInventoryStatusResponse
    currentUser: locals.user ?? null
  }
}
```

---

## Layout Overview (Desktop)

```
┌──────────────────────────────────────────────────────────────────────┐
│ [1] NAV BAR (sticky, 64px)                                           │
├──────────────────────────────────────────────────────────────────────┤
│ [2] HERO BANNER (full-width, h-[400px], object-cover)                │
│     event image / fallback gradient                                  │
│     Overlay gradient bottom: transparent → ink/80                    │
│     Event title overlaid bottom-left (display-lg, white)            │
├──────────────────────────────────────────────────────────────────────┤
│ [3] CONTENT (max-w-[1400px] mx-auto px-4 py-8)                      │
│                                                                      │
│  ┌──────────────────────────────────┬─────────────────────────────┐  │
│  │ LEFT — MAIN (flex-1)             │ RIGHT — STICKY SIDEBAR      │  │
│  │                                  │ (w-[360px] sticky top-20)  │  │
│  │ [EVENT INFO BAND]                │                             │  │
│  │  📅 FRI, SEP 18, 2026 · 8:00 PM │ ┌─────────────────────────┐ │  │
│  │  📍 UBS Arena, Belmont Park, NY  │ │ ORDER SUMMARY CARD      │ │  │
│  │  🏷 K-Pop · Concerts             │ │ (card-marketing-large)  │ │  │
│  │                                  │ │                         │ │  │
│  │ ─────────────────────────────    │ │ FRI SEP 18 · 8:00 PM   │ │  │
│  │ [4] TICKET SELECTION             │ │ UBS Arena               │ │  │
│  │                                  │ │ ─────────────────────   │ │  │
│  │  [Standard] [VIP] filter tabs    │ │ Your Selection:         │ │  │
│  │                                  │ │ (empty state initially) │ │  │
│  │  OFFER ROW ×N (accordion)        │ │                         │ │  │
│  │  ┌────────────────────────────┐  │ │ [Subtotal]              │ │  │
│  │  │ [Offer name]  [Price] [Qty]│  │ │ [Fees]                  │ │  │
│  │  │ [Description truncated]   │  │ │ [Total]                  │ │  │
│  │  └────────────────────────────┘  │ │                         │ │  │
│  │                                  │ │ [Get Tickets ──────────►]│ │  │
│  │ ─────────────────────────────    │ └─────────────────────────┘ │  │
│  │ [5] ABOUT SECTION                │                             │  │
│  │  description (line-clamp + more) │                             │  │
│  │                                  │                             │  │
│  │ ─────────────────────────────    │                             │  │
│  │ [6] LINEUP                       │                             │  │
│  │  [attraction pill] [pill] ...    │                             │  │
│  └──────────────────────────────────┴─────────────────────────────┘  │
├──────────────────────────────────────────────────────────────────────┤
│ [7] MOBILE STICKY BOTTOM BAR (< 1024px only)                        │
│     [Total: $150]          [Get Tickets ──────────►]                │
├──────────────────────────────────────────────────────────────────────┤
│ [8] FOOTER                                                           │
└──────────────────────────────────────────────────────────────────────┘
```

---

## Chi tiết từng section

### [2] Hero Banner

```
<section class="relative h-[400px] overflow-hidden bg-ink">
  <img
    src={event.imageUrl ?? fallback}
    class="absolute inset-0 w-full h-full object-cover object-center"
    loading="eager"
  />
  <!-- gradient overlay -->
  <div class="absolute inset-0
    bg-[linear-gradient(180deg,rgba(0,0,0,0.05)_0%,rgba(0,0,0,0.15)_50%,rgba(0,0,0,0.78)_100%)]"
  />
  <!-- title overlay (bottom-left) -->
  <div class="relative h-full max-w-[1400px] mx-auto px-4 flex flex-col justify-end pb-8">
    <div class="flex flex-wrap gap-2 mb-3">
      {#each event.classifications as cls}
        <span class="...badge blue-accent-soft...">{cls.name}</span>
      {/each}
    </div>
    <h1 class="text-white text-3xl md:text-4xl font-semibold tracking-tight max-w-2xl">
      {event.title}
    </h1>
  </div>
</section>
```

Mobile: `h-[260px]`, title font-size nhỏ hơn.

---

### [3a] Event Info Band (dưới hero, trên ticket section)

```
┌─────────────────────────────────────────────────────────────────────┐
│  📅 Friday, September 18, 2026 at 8:00 PM EDT                      │
│  📍 UBS Arena · Belmont Park, NY, United States                    │
│                                                                     │
│  [♡ Save]  [↗ Share]  (nếu đã login)                               │
└─────────────────────────────────────────────────────────────────────┘
```

- Date: format `EEEE, MMMM d, yyyy 'at' h:mm a z` từ `event.startAt` + `event.timezone`
- Venue: `venueName · cityName, state` (load venue từ `event.venueId` khi SSR)
- Icons: Lucide `CalendarDays`, `MapPin`
- Save button: toggle visual (Phase 5 feature), hiển thị nhưng chưa gọi API
- Share: Web Share API nếu có, fallback copy URL

---

### [4] Ticket Selection

Đây là phần core nhất của trang. Clone Ticketmaster list-view:

#### 4a. Section Header + Filter Tabs

```
┌─────────────────────────────────────────────────────────────────────┐
│  Ticket Options                         [Standard ▾] [VIP ▾]       │
│                                                                     │
│  (nếu event.saleStartAt > now):                                    │
│  ┌───────────────────────────────────────────────────────────────┐ │
│  │ ⏰ Tickets go on sale Friday, June 1 at 10:00 AM             │ │
│  │    [🔔 Get notified]                                          │ │
│  └───────────────────────────────────────────────────────────────┘ │
│                                                                     │
│  (nếu event.status === CANCELLED):                                  │
│  ┌───────────────────────────────────────────────────────────────┐ │
│  │ ⚠️  This event has been cancelled.                            │ │
│  └───────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────┘
```

Filter tabs = TicketType names (nhóm theo `offer.ticketTypeId`). Chỉ hiện tabs nếu có > 1 loại.

#### 4b. Offer Row (collapsed — default)

Mỗi offer render thành một row. Clone Ticketmaster list-view:

```
┌─────────────────────────────────────────────────────────────────────┐
│                                                                     │
│  [OFFER NAME bold]                    [PRICE]    [QTY ▾]  [+Add]  │
│   Standard GA                         $125.00    1 ticket   [+]    │
│   [Description, body-sm, mute, 1 line truncate]                    │
│                                                                     │
│   (nếu sold out):                                                   │
│   Standard GA                         SOLD OUT   ─────    ─────   │
│                                                                     │
│   (nếu presale only):                                               │
│   VIP Floor                           $350.00    Presale Only      │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

**Left side:**
- Offer name: `body-md-strong` / `ink`
- Description: `body-sm` / `mute`, `line-clamp-1` (expand khi row mở rộng)

**Right side (3 elements):**
- Price: `font-mono text-sm font-bold text-ink` — `faceValue` formatted
  - Nhỏ hơn bên dưới: `body-sm / mute` "per ticket"
- Qty selector: `<select>` styled với Tailwind
  - Options: từ `offer.sellableQuantities` (e.g. [1, 2, 3, 4])
  - Disabled nếu sold out hoặc not on sale
- Add button `[+]`: `button-primary-sm` (`rounded-full`, `w-8 h-8`, icon `+`)
  - Click → thêm vào selection; button đổi thành `[-]` / "Selected" state

**Row states:**

| State | Trigger | UI |
|---|---|---|
| Available | `gaInventory.available > 0` + sale window active | Default |
| Selected | user đã chọn qty > 0 | Row highlight `blue-accent-soft`, `[-]` button |
| Sold out | `gaInventory.available === 0` | Price → "Sold Out" badge, qty/add disabled |
| Not on sale | Không có active `saleWindow` | Price → "Sale Starts {date}" |
| Presale | saleWindow type = PRESALE | Badge "Presale" + nếu user không eligible → disabled |

**Row container:**
```css
flex items-center gap-4 px-4 py-5
border border-hairline rounded-lg bg-canvas
hover:border-hairline-strong hover:shadow-sm
transition-all cursor-pointer mb-3
/* selected state: */
border-primary/40 bg-blue-accent-soft/30 shadow-sm
```

#### 4c. Offer Row (expanded — click để expand)

Click vào row → expand panel chi tiết:

```
┌─────────────────────────────────────────────────────────────────────┐
│  Standard GA                          $125.00     [2 ▾]   [−] [+] │
├─────────────────────────────────────────────────────────────────────┤
│  EXPANDED:                                                          │
│  Full description text (no line-clamp)                              │
│                                                                     │
│  Price breakdown:                                                   │
│  Face value        $125.00 × 2       = $250.00                     │
│  Service fee        $15.00 × 2       = $ 30.00   (từ charges)      │
│  Facility fee        $5.00 × 2       = $ 10.00   (từ charges)      │
│  ─────────────────────────────────────────────                     │
│  Subtotal                             $290.00                       │
│                                                                     │
│  ℹ️  Up to 4 tickets per order                                      │
└─────────────────────────────────────────────────────────────────────┘
```

Charges: loop `offer.charges` → mỗi charge hiện `name · amount × qty`.

Max per order: `offer.eventTicketMinimum` → hiện info note.

---

### [5] About Section

```
┌─────────────────────────────────────────────────────────────────────┐
│  About this event                                                   │
│  ───────────────                                                    │
│                                                                     │
│  [event.description — line-clamp-4 mặc định]                       │
│                                                                     │
│  [Read more ▾]                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

- Toggle `line-clamp-4` / full text
- Nếu description null/empty → ẩn section

---

### [6] Lineup Section

```
┌─────────────────────────────────────────────────────────────────────┐
│  Lineup                                                             │
│  ───────                                                            │
│                                                                     │
│  [Avatar] aespa        [Avatar] DJ Support      ...                │
│           K-Pop                  Electronic                         │
│  → link /{slug}/artist/{id}                                        │
└─────────────────────────────────────────────────────────────────────┘
```

Dùng `event.attractions[]`:
- Avatar: `attraction.imageUrl`, 48×48, `rounded-full`
- Name: `body-sm-strong` / `ink`
- Type: `caption` / `mute`
- Link: `/${attraction.slug}/artist/${attraction.id}`
- Layout: horizontal flex wrap, gap-4

---

### [Right] Order Summary Sidebar

Sticky sidebar trên desktop (`sticky top-20`), ẩn trên mobile (thay bằng bottom bar):

```
┌─────────────────────────────────────────┐
│  card-marketing-large chrome            │
│                                         │
│  FRI, SEP 18, 2026                      │  caption-mono / mute
│  8:00 PM EDT                            │  display-sm / ink
│  UBS Arena                              │  body-sm / mute
│  Belmont Park, NY                       │  body-sm / mute
│                                         │
│  ─────────────────────────────────────  │
│                                         │
│  (khi chưa chọn gì):                   │
│  [ticket icon]                          │
│  Select tickets to get started          │  body-sm / mute, centered
│                                         │
│  (khi đã chọn):                         │
│  Your Selection                         │  body-sm-strong / ink
│                                         │
│  Standard GA × 2         $250.00        │  body-sm / body
│    Service fee × 2        $30.00        │  caption / mute, indent
│    Facility fee × 2       $10.00        │  caption / mute, indent
│                                         │
│  ─────────────────────────────────────  │
│  Total                   $290.00        │  body-md-strong / ink
│                                         │
│  ─────────────────────────────────────  │
│                                         │
│  [Get Tickets ──────────────────────►]  │  button-primary full-width
│                                         │
│  (khi loading reservation):             │
│  [Reserving your tickets...]            │  disabled, spinner
│                                         │
│  ℹ️  Prices include all fees             │  caption / mute
└─────────────────────────────────────────┘
```

**"Get Tickets" flow:**
1. Click → `loading = true`
2. POST `/api/reservations` với `{ eventId, items: [{ offerId, seatingMode: 'GA', areaId, qty }] }`
3. On success → `goto(`/checkout/${reservation.id}`)`
4. On error → hiện error banner inline trong sidebar

---

### [7] Mobile Sticky Bottom Bar

Chỉ hiện trên `< lg (1024px)`, thay thế sidebar:

```
┌─────────────────────────────────────────────────────────────────────┐
│  (khi chưa chọn):                                                   │
│  [Select tickets to continue ──────────────────────────────────►]  │
│  button-primary full-width disabled                                 │
│                                                                     │
│  (khi đã chọn):                                                     │
│  Total: $290.00 (2 tickets)     [Get Tickets ──────────────────►]  │
│  body-sm-strong / ink           button-primary                      │
└─────────────────────────────────────────────────────────────────────┘
```

- Fixed bottom, `z-50`, background `canvas`, top border `hairline`, padding `py-3 px-4`
- Trên mobile: offer rows không có inline qty/add → user click row → qty selector hiện trong expanded panel

---

## State Management (`+page.svelte`)

```ts
// Selection state: offerId → qty
let selection = $state<Map<string, number>>(new Map())

// Derived: selected offers với full offer data
const selectedItems = $derived(
  [...selection.entries()]
    .filter(([, qty]) => qty > 0)
    .map(([offerId, qty]) => ({
      offer: data.offers.find(o => o.id === offerId)!,
      qty
    }))
)

// Derived: total price (faceValue + charges) × qty
const orderTotal = $derived(
  selectedItems.reduce((acc, { offer, qty }) => {
    const chargesPerTicket = offer.charges.reduce((s, c) =>
      s + (c.isPercentage ? offer.faceValue * c.amount / 100 : c.amount), 0)
    return acc + (offer.faceValue + chargesPerTicket) * qty
  }, 0)
)

// Derived: total ticket count
const totalQty = $derived(
  selectedItems.reduce((sum, { qty }) => sum + qty, 0)
)

// UI states
let bioExpanded = $state(false)
let expandedOfferId = $state<string | null>(null)
let reserving = $state(false)
let reservationError = $state<string | null>(null)

// Helpers
function setQty(offerId: string, qty: number) {
  if (qty === 0) selection.delete(offerId)
  else selection.set(offerId, qty)
  selection = new Map(selection) // trigger reactivity
}

// Create reservation
async function getTickets() {
  if (totalQty === 0) return
  reserving = true
  reservationError = null
  try {
    const items = selectedItems.map(({ offer, qty }) => ({
      offerId: offer.id,
      seatingMode: offer.seatingMode,
      areaId: getAreaId(offer),   // từ inventory
      qty
    }))
    const res = await fetch('/api/reservations', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ eventId: data.event.id, items })
    })
    const json = await res.json()
    if (!res.ok) throw new Error(json.message ?? 'Failed to reserve')
    goto(`/checkout/${json.data.id}`)
  } catch (e) {
    reservationError = (e as Error).message
    reserving = false
  }
}

// Get areaId for GA offer from inventory
function getAreaId(offer: OfferResponse): string | undefined {
  return data.inventory.gaInventory.find(g => g.offerId === offer.id)?.areaId
}
```

---

## Offer Availability Logic

```ts
function offerState(offer: OfferResponse): 'available' | 'soldout' | 'not-on-sale' | 'presale' {
  const now = new Date()

  // Check inventory
  const inv = data.inventory.gaInventory.find(g => g.offerId === offer.id)
  if (inv && inv.available === 0) return 'soldout'

  // Check sale windows
  const activeSaleWindow = offer.saleWindows.find(w =>
    new Date(w.startAt) <= now && now <= new Date(w.endAt)
  )
  if (!activeSaleWindow) {
    const upcoming = offer.saleWindows.find(w => new Date(w.startAt) > now)
    if (upcoming?.type === 'PRESALE') return 'presale'
    return 'not-on-sale'
  }
  if (activeSaleWindow.type === 'PRESALE') return 'presale'
  return 'available'
}
```

---

## Event Status Banner

Hiện banner phía trên ticket section khi event không ở trạng thái bình thường:

| `event.status` | Banner |
|---|---|
| `CANCELLED` | ⚠️ "This event has been cancelled." — `error-soft` bg |
| `POSTPONED` | 🕐 "This event has been postponed. New date TBA." — `warning-soft` bg |
| `DRAFT` | 🔒 "This event is not yet published." — `canvas-soft-2` bg |
| `ON_SALE`, `PUBLISHED` | Không hiện banner |

---

## Components

| Component | Path | Mô tả |
|---|---|---|
| `EventHero.svelte` | `lib/components/event/` | Hero banner với title overlay |
| `EventInfoBand.svelte` | `lib/components/event/` | Date + venue + share/save |
| `OfferList.svelte` | `lib/components/event/` | Wrapper list + filter tabs |
| `OfferRow.svelte` | `lib/components/event/` | Offer row (collapsed + expanded) |
| `OfferQtySelector.svelte` | `lib/components/event/` | `<select>` qty với sellableQuantities |
| `EventStatusBanner.svelte` | `lib/components/event/` | Status warning/error banner |
| `OrderSummaryCard.svelte` | `lib/components/event/` | Sidebar card (desktop) |
| `MobileOrderBar.svelte` | `lib/components/event/` | Sticky bottom bar (mobile) |
| `EventLineup.svelte` | `lib/components/event/` | Attraction pills section |
| `PriceBreakdown.svelte` | `lib/components/event/` | Charges breakdown table |

Tất cả components trong `lib/components/event/` (folder mới, tách khỏi `catalog/`).

---

## OfferRow — Spec Chi Tiết

### Collapsed

```svelte
<div
  class="flex items-center gap-3 px-4 py-5 rounded-lg border transition-all cursor-pointer"
  class:border-hairline={state !== 'selected'}
  class:bg-canvas={state !== 'selected'}
  class:border-primary={state === 'selected'}
  class:bg-blue-accent-soft/20={state === 'selected'}
  onclick={() => expandedOfferId = expandedOfferId === offer.id ? null : offer.id}
>
  <!-- Left: name + description -->
  <div class="flex-1 min-w-0">
    <p class="text-sm font-semibold text-ink">{offer.name}</p>
    <p class="text-xs text-mute mt-0.5 line-clamp-1">{offer.description}</p>
  </div>

  <!-- Right: price + qty + add -->
  <div class="shrink-0 flex items-center gap-3">
    <!-- Price -->
    <div class="text-right">
      {#if availState === 'soldout'}
        <span class="text-xs font-semibold text-mute uppercase tracking-wide">Sold Out</span>
      {:else if availState === 'not-on-sale'}
        <span class="text-xs text-mute">Sale starts {formatDate(nextSaleWindow.startAt)}</span>
      {:else}
        <span class="font-mono text-sm font-bold text-ink">{formatCurrency(offer.faceValue, offer.currency)}</span>
        <span class="block text-[10px] text-mute font-mono">per ticket</span>
      {/if}
    </div>

    <!-- Qty selector (nếu available) -->
    {#if availState === 'available'}
      <OfferQtySelector
        quantities={offer.sellableQuantities}
        value={selection.get(offer.id) ?? 0}
        onchange={(qty) => setQty(offer.id, qty)}
      />
    {/if}

    <!-- Add / Remove button -->
    {#if availState === 'available'}
      <button
        class="w-8 h-8 rounded-full flex items-center justify-center transition-colors"
        class:bg-primary={!isSelected}
        class:text-on-primary={!isSelected}
        class:bg-canvas={isSelected}
        class:border={isSelected}
        class:border-hairline={isSelected}
        onclick|stopPropagation={() => setQty(offer.id, isSelected ? 0 : (offer.sellableQuantities[0] ?? 1))}
      >
        {isSelected ? '−' : '+'}
      </button>
    {/if}
  </div>
</div>
```

### Expanded (bên dưới collapsed, show khi `expandedOfferId === offer.id`)

```svelte
{#if expanded}
  <div class="px-4 pb-4 bg-canvas-soft rounded-b-lg border-x border-b border-hairline -mt-2">
    <!-- Full description -->
    <p class="text-sm text-body mb-4">{offer.description}</p>

    <!-- Price breakdown -->
    <PriceBreakdown {offer} qty={currentQty} />

    <!-- Info notes -->
    {#if offer.eventTicketMinimum > 1}
      <p class="text-xs text-mute mt-3">
        ℹ️ Minimum {offer.eventTicketMinimum} ticket(s) per order
      </p>
    {/if}
    {#if offer.restrictSingleSeat}
      <p class="text-xs text-mute mt-1">
        ℹ️ No single seat purchases (must buy adjacent seats)
      </p>
    {/if}
  </div>
{/if}
```

---

## PriceBreakdown Component

```
Face value          $125.00 × 2     $250.00   ← body-sm / body
Service fee          $15.00 × 2      $30.00   ← body-sm / mute
Facility fee          $5.00 × 2      $10.00   ← body-sm / mute
───────────────────────────────────────────
Subtotal                            $290.00   ← body-sm-strong / ink
```

```svelte
<!-- PriceBreakdown.svelte -->
<div class="space-y-1.5 text-sm">
  <div class="flex justify-between">
    <span class="text-body">Face value</span>
    <span class="font-mono text-body">{fmt(offer.faceValue)} × {qty} = {fmt(offer.faceValue * qty)}</span>
  </div>
  {#each offer.charges as charge}
    <div class="flex justify-between">
      <span class="text-mute">{charge.name}</span>
      <span class="font-mono text-mute">
        {charge.isPercentage
          ? fmt(offer.faceValue * charge.amount / 100)
          : fmt(charge.amount)} × {qty} = {fmt(chargeTotal(charge) * qty)}
      </span>
    </div>
  {/each}
  <div class="border-t border-hairline pt-1.5 flex justify-between font-semibold">
    <span class="text-ink">Subtotal</span>
    <span class="font-mono text-ink">{fmt(lineTotal * qty)}</span>
  </div>
</div>
```

---

## SEO

- `<title>`: `{event.title} | {formattedDate} | Ticketpeak`
  - e.g. "aespa LIVE TOUR – SYNK : COMPLæXITY | Fri Sep 18, 2026 | Ticketpeak"
- `<meta name="description">`: `Get tickets for {event.title} at {venueName} on {date}. Buy now on Ticketpeak.`
- `<link rel="canonical" href="/{event.slug}/event/{event.id}">` 
- Open Graph: `og:image` = `event.imageUrl`, `og:title`, `og:description`

---

## Responsive Behavior

| Breakpoint | Behavior |
|---|---|
| Mobile (< 768px) | 1 column; hero h-[260px]; offer rows compact; sidebar hidden; sticky bottom bar |
| Tablet (768–1024px) | 1 column; hero h-[340px]; sidebar ẩn; sticky bottom bar |
| Desktop (≥ 1024px) | 2 columns: main `flex-1` + sidebar `w-[360px]`; no bottom bar |

---

## Design System Mapping

| Element | Token |
|---|---|
| Hero bg fallback | `ink` |
| Hero overlay | `rgba(0,0,0,0.05)` → `rgba(0,0,0,0.78)` gradient |
| Hero title | `on-primary` / `display-lg` |
| Page bg | `canvas-soft` |
| Event info band bg | `canvas` |
| Event info text | `ink` / `body-md` |
| Classification badge | `blue-accent-soft` bg / `blue-accent` text / `caption` |
| Offer row bg | `canvas` |
| Offer row border default | `hairline` |
| Offer row hover border | `hairline-strong` |
| Offer row selected border | `primary/40` |
| Offer row selected bg | `blue-accent-soft/20` |
| Offer name | `ink` / `body-md-strong` |
| Offer description | `mute` / `body-sm` |
| Price | `ink` / `caption-mono` (font-mono) |
| "Sold Out" | `mute` / `caption` uppercase |
| Add button | `primary` bg / `on-primary` text |
| Remove button | `canvas` bg / `hairline` border |
| Expanded panel bg | `canvas-soft` |
| Charge row | `mute` / `body-sm` |
| Subtotal | `ink` / `body-sm-strong` |
| Order summary card | `card-marketing-large` chrome |
| Order summary date | `mute` / `caption-mono` |
| Order summary time | `ink` / `display-sm` |
| Order total | `ink` / `body-md-strong` |
| Get Tickets CTA | `button-primary` full-width |
| Mobile bottom bar bg | `canvas` / top `hairline` border |
| Status banner cancelled | `error-soft` bg / `error` text |
| Status banner postponed | `warning-soft` bg / `warning-deep` text |
| About heading | `ink` / `display-md` |
| Lineup attraction name | `ink` / `body-sm-strong` |
| Lineup type | `mute` / `caption` |

---

## Acceptance Criteria

- [ ] URL pattern đúng: `/{slug}/event/{id}`.
- [ ] SSR: event + offers + inventory load parallel, không có waterfall.
- [ ] Hero: banner image + gradient overlay + title + classification badges.
- [ ] Event info band: date/time formatted đúng timezone, venue name + location.
- [ ] Offer rows render đủ (name, price, qty selector, add button).
- [ ] `sellableQuantities` đúng options trong qty selector.
- [ ] Offer state: available / soldout / not-on-sale render đúng UI per state.
- [ ] Click `[+]` → offer thêm vào selection; sidebar/bottom bar cập nhật.
- [ ] Click `[-]` / qty = 0 → xóa khỏi selection.
- [ ] Expand offer row → hiện full description + price breakdown (faceValue + mỗi charge).
- [ ] Order summary sidebar: empty state khi chưa chọn; items list khi đã chọn; total đúng.
- [ ] Click "Get Tickets" → POST `/api/reservations` → redirect `/checkout/{reservationId}`.
- [ ] Loading state ("Reserving...") trong khi POST.
- [ ] Error state nếu POST fail → hiện message trong sidebar.
- [ ] Event status banner hiện đúng cho CANCELLED / POSTPONED / DRAFT.
- [ ] About section: line-clamp-4 + Read more toggle.
- [ ] Lineup: hiện attractions, link đúng `/{slug}/artist/{id}`.
- [ ] Mobile: sidebar ẩn, sticky bottom bar hiện; offer rows compact.
- [ ] Desktop: 2 columns, sidebar sticky `top-20`.
- [ ] `<title>`, `<meta>`, Open Graph, canonical set đúng.
- [ ] Tokens match DESIGN.md.

---

## Dependency

Plan này phụ thuộc vào:
- **Plan 014** (order/reservation API) — `POST /api/reservations` endpoint phải hoạt động
- **Seed data** — event với đủ offers + inventory
- **Plan 023** (checkout page) — redirect target sau khi reservation tạo thành công

---

## Status

`planned`

## Outcome

_To be filled after implementation._
