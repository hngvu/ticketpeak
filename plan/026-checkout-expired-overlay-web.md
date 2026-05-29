# 026 — Checkout: Session Expired Overlay — Web

## Description

Thêm overlay rõ ràng khi checkout session hết hạn (`isExpired = true`).

Hiện tại khi `CheckoutTimer` callback `onExpire` kích hoạt, `isExpired` được set thành `true` và các button bị `disabled`. Nhưng user vẫn thấy toàn bộ form mà không có hướng dẫn rõ ràng phải làm gì tiếp theo — không có CTA để thoát.

---

## Files Liên Quan

| File | Thay đổi |
|---|---|
| `web/src/routes/checkout/[id]/+page.svelte` | Thêm overlay khi `isExpired === true` |

---

## Trạng Thái Hiện Tại

```
isExpired = true →
  ✅ CheckoutTimer hiện "Hold Time Expired" banner (trong component riêng)
  ✅ Payment method buttons bị disabled
  ✅ "Place Order" button bị disabled
  ❌ Không có overlay / modal che form
  ❌ Không có CTA "Back to Event"
  ❌ User bị mắc kẹt — không biết phải làm gì
```

**Data sẵn có:** `data.event.slug` đã load trong `+page.server.ts` — dùng để build back link.

---

## Thiết Kế Overlay

```
┌──────────────────────────────────────────────────────────────────────┐
│  [backdrop: fixed inset-0 bg-ink/60 backdrop-blur-sm z-50]          │
│                                                                      │
│              ┌──────────────────────────────────┐                   │
│              │  [icon: clock đã hết / X-circle] │                   │
│              │                                  │                   │
│              │  Session Expired                 │  display-md / ink │
│              │                                  │                   │
│              │  Your ticket hold has been       │  body-sm / mute   │
│              │  released. Please go back and    │                   │
│              │  select tickets again.           │                   │
│              │                                  │                   │
│              │  [← Back to Event]               │  button-primary   │
│              │                                  │                   │
│              └──────────────────────────────────┘                   │
└──────────────────────────────────────────────────────────────────────┘
```

---

## Implementation

### Overlay markup (thêm vào cuối `+page.svelte`, trước `</div>` ngoài cùng)

```svelte
{#if isExpired}
  <div
    class="fixed inset-0 z-50 flex items-center justify-center bg-ink/60 backdrop-blur-sm px-4"
  >
    <div
      class="w-full max-w-sm rounded-2xl border border-hairline bg-canvas p-8 text-center shadow-xl"
    >
      <!-- Icon -->
      <div class="mx-auto mb-5 flex h-14 w-14 items-center justify-center rounded-full bg-error-soft text-error-deep">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
          class="h-7 w-7"
        >
          <circle cx="12" cy="12" r="10" />
          <polyline points="12 6 12 12 16 14" />
        </svg>
      </div>

      <!-- Title -->
      <h2 class="font-sans text-xl font-extrabold text-ink">
        Session Expired
      </h2>

      <!-- Description -->
      <p class="mt-2 font-sans text-sm leading-relaxed text-mute">
        Your ticket hold has been released back to inventory.
        Please return to the event page to select tickets again.
      </p>

      <!-- CTA -->
      <button
        type="button"
        onclick={() => goto(`/${data.event?.slug ?? ''}`)}
        class="mt-6 flex w-full cursor-pointer items-center justify-center gap-2 rounded-full bg-primary py-3 font-sans text-sm font-bold text-white shadow-md transition-all hover:bg-link-deep active:scale-[0.98]"
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2.5"
          stroke-linecap="round"
          stroke-linejoin="round"
          class="h-4 w-4"
        >
          <path d="m15 18-6-6 6-6" />
        </svg>
        Back to Event
      </button>
    </div>
  </div>
{/if}
```

### Back link logic

```ts
// data.event đã có slug từ server load (cả real API lẫn mock fallback)
// Real event slug: data.event.slug
// Mock event slug: MOCK_EVENTS[0].slug

onclick={() => goto(`/${data.event?.slug ?? ''}`)}
```

Nếu `event.slug` null (edge case): fallback về `/` (homepage).

---

## Acceptance Criteria

- [ ] Khi `isExpired = true`: overlay xuất hiện full-screen với backdrop blur.
- [ ] Overlay có icon đồng hồ trong circle `error-soft`.
- [ ] Tiêu đề "Session Expired" rõ ràng, `font-extrabold`.
- [ ] Mô tả giải thích vé đã được trả lại inventory.
- [ ] Button "Back to Event" navigate đúng về `/{event.slug}`.
- [ ] Nếu `event.slug` null: fallback navigate về `/` không crash.
- [ ] Overlay `z-50` — hiện trên tất cả nội dung khác.
- [ ] Backdrop `bg-ink/60 backdrop-blur-sm` — form bên dưới bị che mờ.
- [ ] Trước khi expired: overlay không hiện, form hoạt động bình thường.
- [ ] Mobile: overlay card `max-w-sm` centered, padding đủ, không bị clip.
- [ ] `CheckoutTimer` expired banner vẫn hiện bên dưới overlay (không conflict).

---

## Dependency

- `data.event.slug` phải có trong page data — đã có trong cả real API path và mock fallback trong `+page.server.ts`.
- `goto` từ `$app/navigation` đã được import sẵn trong file.

---

## Status

`planned`

## Outcome

_To be filled after implementation._
