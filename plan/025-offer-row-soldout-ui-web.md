# 025 — OfferRow: Sold-Out UI — Web

## Description

Hoàn thiện sold-out state cho `OfferRow.svelte`.

Logic detect `soldout` đã đúng (`availState === 'soldout'` khi `inv.available === 0`), badge "Sold Out" đã render ở price slot, qty selector và add button đã ẩn. Tuy nhiên row vẫn có thể click để expand và không có visual feedback rõ ràng rằng offer này không thể chọn.

**Test data sẵn có:** `Balcony` tier trong mock data đã seed `available: 0` — test được ngay không cần mock thêm.

---

## Files Liên Quan

| File | Thay đổi |
|---|---|
| `web/src/lib/components/event/OfferRow.svelte` | Disable toggle, thêm opacity + cursor, cải thiện badge |

---

## Trạng Thái Hiện Tại

```
availState === 'soldout' → stateLabel === 'soldout'
  ✅ Ẩn OfferQtySelector
  ✅ Ẩn Add/Remove button  
  ✅ Render badge "Sold Out" ở price slot
  ❌ Toggle button (expand/collapse) vẫn clickable
  ❌ Row wrapper không có opacity-60 / cursor-not-allowed
  ❌ Row vẫn có hover effect (border đậm, shadow)
  ❌ Expanded panel vẫn mở được nếu click vào text
```

---

## Thay Đổi Cần Làm

### 1. Disable toggle button khi sold out

```svelte
<button
  type="button"
  onclick={onToggle}
  disabled={stateLabel === 'soldout'}
  class="min-w-0 flex-1 text-left focus:outline-none
    {stateLabel === 'soldout' ? 'cursor-not-allowed' : 'cursor-pointer'}"
>
```

### 2. Row wrapper — sold-out style

Thêm sold-out class vào `div` wrapper chính thay cho hover effects:

```svelte
<div
  class="flex w-full items-center gap-4 border px-4 py-5 transition-all duration-200
    {stateLabel === 'soldout'
      ? 'border-hairline bg-canvas-soft/60 opacity-60 cursor-not-allowed select-none'
      : isSelected
        ? 'border-primary/40 bg-blue-accent-soft/30 shadow-2xs'
        : 'border-hairline bg-canvas hover:border-hairline-strong hover:shadow-2xs'}"
>
```

### 3. Không expand khi sold out

Vì toggle button bị `disabled`, expanded panel sẽ tự không mở. Nếu `isExpanded` được control từ ngoài cần guard thêm:

Trong parent (`+page.svelte` hoặc list component), `onToggle` không được gọi khi soldout — đây đã được handle bởi `disabled` trên button.

### 4. Badge "Sold Out" — cải thiện visual

Badge hiện tại:
```svelte
<span class="rounded-md bg-canvas-soft-2 px-2.5 py-1 font-sans text-[10px] font-bold tracking-wider text-mute uppercase">
  Sold Out
</span>
```

Đổi sang màu rõ hơn (đỏ nhạt, không chói):
```svelte
<span class="rounded-md bg-error-soft px-2.5 py-1 font-sans text-[10px] font-bold tracking-wider text-error-deep uppercase">
  Sold Out
</span>
```

---

## Kết Quả Sau Khi Fix

| State | Visual |
|---|---|
| Available | Border hairline, hover effect, price + qty + add button |
| Selected | Border primary/40, blue tint bg, minus button |
| **Sold out** | **opacity-60, cursor-not-allowed, badge đỏ nhạt, không hover, không expand** |
| Not on sale | Date text, không có controls |
| Presale | Price bình thường + controls |

---

## Acceptance Criteria

- [ ] Row sold-out có `opacity-60` — visually muted so với available rows.
- [ ] Row sold-out không có hover effect (border không đậm lên, không có shadow).
- [ ] Toggle button bị `disabled` — click vào tên/description không expand panel.
- [ ] `cursor-not-allowed` trên toàn row khi soldout.
- [ ] Badge "Sold Out" màu error-soft / error-deep (đỏ nhạt), không phải mute.
- [ ] Qty selector và Add button vẫn ẩn (đã có, giữ nguyên).
- [ ] Available rows không bị ảnh hưởng — hover, expand, select vẫn hoạt động bình thường.
- [ ] Test với `Balcony` mock tier (`available: 0`) — render đúng luôn.
- [ ] Không có console error hay Svelte warning sau khi thay đổi.

---

## Dependency

Không có dependency mới. Mock data `available: 0` đã sẵn sàng.

---

## Status

`planned`

## Outcome

_To be filled after implementation._
