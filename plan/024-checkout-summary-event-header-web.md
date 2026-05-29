# 024 — Checkout Summary: Event Info Header — Web

## Description

Thêm event thumbnail và title vào top của `CheckoutSummary` sidebar component.

Hiện tại `CheckoutSummary.svelte` chỉ hiện order line items và total — user không thấy đang thanh toán cho event nào. `+page.server.ts` đã load đủ `event` object (có `imageUrl`, `title`, `venueName`, `startAt`) nhưng chưa truyền xuống component.

---

## Files Liên Quan

| File | Thay đổi |
|---|---|
| `web/src/lib/components/checkout/CheckoutSummary.svelte` | Thêm prop `event` + render header block |
| `web/src/routes/checkout/[id]/+page.svelte` | Truyền `event={data.event}` vào `<CheckoutSummary>` |

---

## Thay đổi Component

### `CheckoutSummary.svelte` — thêm prop

```ts
let { items = [], currency = 'VND', event = null } = $props<{
  items: any[]
  currency: string
  event?: any
}>()
```

### Header block (trên `<h3>Order Summary</h3>`)

```
┌─────────────────────────────────────────┐
│  [thumbnail 56×56 rounded-lg]           │
│  Tên sự kiện (font-semibold, line-clamp-2) │
│  Thứ, ngày tháng năm · tên venue        │
├─────────────────────────────────────────┤
│  Order Summary                          │
│  ...items...                            │
└─────────────────────────────────────────┘
```

Layout chi tiết:
```svelte
{#if event}
  <div class="mb-5 flex items-start gap-3 border-b border-hairline pb-5">
    {#if event.imageUrl}
      <img
        src={event.imageUrl}
        alt={event.title}
        class="h-14 w-14 shrink-0 rounded-lg object-cover shadow-xs"
      />
    {/if}
    <div class="min-w-0">
      <p class="font-sans text-sm font-bold text-ink line-clamp-2 leading-snug">
        {event.title}
      </p>
      {#if event.startAt}
        <p class="mt-1 font-sans text-xs text-mute">
          {formatEventDate(event.startAt, event.timezone)}
        </p>
      {/if}
      {#if event.venueName}
        <p class="mt-0.5 font-sans text-xs text-mute truncate">
          {event.venueName}{event.cityName ? ` · ${event.cityName}` : ''}
        </p>
      {/if}
    </div>
  </div>
{/if}
```

### `formatEventDate` helper

```ts
function formatEventDate(dateStr: string, timezone?: string) {
  const d = new Date(dateStr)
  return d.toLocaleDateString('en-US', {
    weekday: 'short',
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    timeZone: timezone ?? 'Asia/Ho_Chi_Minh'
  })
}
```

### `+page.svelte` — truyền prop

```svelte
<CheckoutSummary
  items={reservation.items}
  currency={reservation.currency}
  event={data.event}
/>
```

---

## Acceptance Criteria

- [ ] `CheckoutSummary` nhận prop `event` (optional — không crash nếu null).
- [ ] Khi `event` có `imageUrl`: thumbnail `56×56` render ở trên cùng card, `rounded-lg`, `object-cover`.
- [ ] Khi `event.imageUrl` null/undefined: chỉ hiện text block, không có broken image.
- [ ] Event title hiện đúng, `line-clamp-2` nếu dài.
- [ ] Date format đúng timezone từ `event.timezone`.
- [ ] Venue name hiện kèm city nếu có `cityName`.
- [ ] Header block có `border-b border-hairline` ngăn cách với "Order Summary".
- [ ] Trên mobile: card vẫn đẹp, thumbnail không bị overflow.
- [ ] Mock data: `MOCK_EVENTS[0]` đã có `imageUrl` từ venue — test được luôn.

---

## Dependency

Không có dependency mới — `event` đã được load sẵn trong `+page.server.ts`.

---

## Status

`planned`

## Outcome

_To be filled after implementation._
