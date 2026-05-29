# 027 — Business Landing Page — Web

## Description

Trang landing page dành cho ban tổ chức sự kiện (organizers) và đối tác muốn dùng nền tảng Ticketpeak để bán vé.

Tương tự `business.ticketmaster.com` — đây là trang marketing B2B, **tách biệt hoàn toàn** khỏi consumer flow. Mục tiêu: thuyết phục organizer đăng ký tài khoản và dùng Ticketpeak Partner Portal.

Trang này là **static marketing page** — không cần API, không cần auth. Render pure SSR, không có state phức tạp.

---

## Route

```
/for-organizers
```

**Lý do chọn route này:**
- Không nằm trong `/b2b/` (b2b là portal sau khi login, cần auth)
- Không nằm trong `/auth/` (sẽ bị skip nav theo `isAuthOrPortal` trong layout)
- `/for-organizers` rõ ràng, SEO-friendly, dùng layout chính (nav + footer)
- Có thể add link "Partner Portal" vào top utility bar của layout sau

**SvelteKit files:**
```
web/src/routes/for-organizers/
├── +page.svelte
└── +page.server.ts   (optional — chỉ cần nếu muốn SSR meta tags động)
```

---

## Layout Overview

```
┌──────────────────────────────────────────────────────────────────────┐
│ [1] HERO — Full-width dark, headline + CTA                           │
├──────────────────────────────────────────────────────────────────────┤
│ [2] STATS BAR — 3–4 số ấn tượng (social proof nhanh)                │
├──────────────────────────────────────────────────────────────────────┤
│ [3] SOLUTIONS GRID — 3 tính năng chính, card layout                 │
├──────────────────────────────────────────────────────────────────────┤
│ [4] HOW IT WORKS — 3 bước đơn giản, numbered steps                  │
├──────────────────────────────────────────────────────────────────────┤
│ [5] TESTIMONIALS — 2–3 quote từ organizer (static mock)             │
├──────────────────────────────────────────────────────────────────────┤
│ [6] CTA BOTTOM — "Start selling tickets today"                       │
└──────────────────────────────────────────────────────────────────────┘
```

Dùng `bg-canvas-soft` làm page background (giống checkout page).  
Nav + footer dùng layout mặc định (`+layout.svelte`) — **không skip**.

---

## Chi Tiết Từng Section

### [1] Hero

```
┌──────────────────────────────────────────────────────────────────────┐
│  bg: ink (dark)  min-h: 560px                                        │
│  background image concert crowd, overlay gradient                    │
│                                                                      │
│  [CENTER CONTENT — max-w-3xl mx-auto text-center]                   │
│                                                                      │
│  Badge: "FOR ORGANIZERS" (pill, blue-accent-soft/ink bg)            │
│                                                                      │
│  H1: "Sell tickets to your events,                                   │
│        your way."                                                    │
│  display-xl / white / font-extrabold / tracking-tight               │
│                                                                      │
│  Subheading:                                                         │
│  "Ticketpeak gives organizers everything they need to sell out       │
│  events — from ticket creation to check-in."                         │
│  body-lg / white/80                                                  │
│                                                                      │
│  CTA buttons:                                                        │
│  [Get Started Free →]  button-primary, rounded-full, px-8, py-3.5  │
│  [See How It Works]    ghost button, border border-white/30         │
│                                                                      │
│  Scroll indicator arrow ↓                                            │
└──────────────────────────────────────────────────────────────────────┘
```

**Background image:** Unsplash concert crowd (same pattern như homepage hero).  
**Hero height:** `min-h-[560px]` desktop, `min-h-[420px]` mobile.

---

### [2] Stats Bar

Dải ngang nằm giữa hero và solutions, `bg-primary`:

```
┌──────────────────────────────────────────────────────────────────────┐
│  bg-primary text-white  py-10                                        │
│                                                                      │
│  [stat]          [stat]          [stat]          [stat]             │
│  10,000+         98%             15 phút         0₫                 │
│  sự kiện tổ      tỉ lệ thanh     để tạo sự       phí setup          │
│  chức thành công toán thành công kiện đầu tiên                       │
└──────────────────────────────────────────────────────────────────────┘
```

Layout: `grid grid-cols-2 lg:grid-cols-4 gap-8` trong `max-w-[1400px] mx-auto px-4`.  
Số: `font-mono text-4xl font-black`.  
Label: `text-sm font-semibold text-white/80 mt-1`.

---

### [3] Solutions Grid

3 card, mỗi card = 1 tính năng cốt lõi:

```
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│  🎟  Tạo vé     │  │  💳 Thanh toán  │  │  📱 Check-in   │
│  dễ dàng        │  │  an toàn        │  │  thông minh     │
│                 │  │                 │  │                 │
│  Tạo nhiều loại │  │  VNPay, Stripe, │  │  QR code TOTP  │
│  vé, set giá,   │  │  chuyển khoản   │  │  luân phiên,   │
│  sale window    │  │  ngân hàng      │  │  chống giả vé  │
│                 │  │                 │  │                 │
│  [Tìm hiểu thêm]│  │  [Tìm hiểu thêm]│  │  [Tìm hiểu thêm]│
└─────────────────┘  └─────────────────┘  └─────────────────┘
```

Card style:
```css
rounded-2xl border border-hairline bg-canvas p-8 shadow-xs
hover:shadow-md hover:border-primary/30 transition-all
```

Icon: SVG inline, 40×40, trong circle `bg-blue-accent-soft`, `text-primary`.  
Title: `text-lg font-extrabold text-ink`.  
Body: `text-sm text-body leading-relaxed mt-2`.  
Link: `text-sm font-bold text-primary hover:underline mt-4 inline-flex items-center gap-1`.

---

### [4] How It Works

3 bước có đánh số, layout alternating (text trái/phải) trên desktop:

```
Bước 1 — Tạo tài khoản và event
[số "01" lớn] + mô tả + screenshot/mockup

Bước 2 — Cấu hình vé và mở bán
[số "02" lớn] + mô tả + screenshot/mockup

Bước 3 — Nhận tiền, check-in fan
[số "03" lớn] + mô tả + screenshot/mockup
```

Layout desktop: `grid grid-cols-2 gap-16 items-center` cho mỗi bước.  
Số: `font-mono text-8xl font-black text-primary/15` (decorative, background-like).  
Screenshot: placeholder `rounded-2xl bg-canvas-soft-2 aspect-video` (thêm real screenshot sau).

Mobile: stacked, số nhỏ lại `text-5xl`.

---

### [5] Testimonials

2 quote từ organizer, layout `grid grid-cols-1 md:grid-cols-2 gap-6`:

```
┌─────────────────────────────────────┐
│  "                                  │
│  Ticketpeak giúp chúng tôi bán hết  │
│  1,200 vé trong 2 giờ đầu tiên.     │
│  Quy trình setup cực kỳ đơn giản."  │
│                                     │
│  [Avatar] Nguyễn Văn A              │
│           Founder, Skyline Events   │
└─────────────────────────────────────┘
```

Card: `rounded-2xl bg-canvas border border-hairline p-8 shadow-xs`.  
Quote icon: `"` decorative, `text-6xl font-serif text-primary/20`.  
Quote text: `text-base font-semibold text-ink leading-relaxed italic`.  
Attribution: `flex items-center gap-3 mt-6` + avatar `rounded-full w-10 h-10 bg-canvas-soft-2`.

---

### [6] CTA Bottom

```
┌──────────────────────────────────────────────────────────────────────┐
│  bg: ink (dark)  py-20  text-center                                  │
│                                                                      │
│  H2: "Sẵn sàng bán vé sự kiện đầu tiên?"                           │
│  display-lg / white                                                  │
│                                                                      │
│  Body: "Miễn phí setup. Không cần thẻ tín dụng."                   │
│  body-md / white/70                                                  │
│                                                                      │
│  [Đăng ký miễn phí →]   button-primary  rounded-full  px-10  py-4  │
│  [Liên hệ tư vấn]        ghost link, text-white/70 hover:text-white │
└──────────────────────────────────────────────────────────────────────┘
```

CTA primary link: `href="/auth"` (đến auth page, đăng ký tài khoản organizer).  
Contact link: `href="mailto:business@ticketpeak.vn"` (placeholder).

---

## Navigation Integration

Thêm link "For Organizers" vào **top utility bar** trong `+layout.svelte`:

```svelte
<!-- Trong dải top utility bar (bg-ink) -->
<a href="/for-organizers" class="transition-colors hover:text-white">For Organizers</a>
```

Thêm sau các link `Hotels`, `Sell`, `Gift Cards` hiện có.

---

## Components

Tất cả trong `web/src/routes/for-organizers/`:

| Component | Mô tả |
|---|---|
| `+page.svelte` | File chính, import các section |
| Hero section | Inline trong page (không cần tách — page chỉ có 1 file) |

Không tách thành components riêng vì đây là static page, không reuse ở nơi khác.

---

## SEO

```svelte
<svelte:head>
  <title>Dành cho Ban Tổ Chức | Ticketpeak</title>
  <meta name="description" content="Bán vé sự kiện dễ dàng với Ticketpeak. Tạo event, nhận thanh toán, check-in fan bằng QR — tất cả trong một nền tảng." />
  <link rel="canonical" href="https://ticketpeak.vn/for-organizers" />
  <!-- OG tags -->
  <meta property="og:title" content="Dành cho Ban Tổ Chức | Ticketpeak" />
  <meta property="og:description" content="Nền tảng bán vé sự kiện cho organizer Việt Nam." />
</svelte:head>
```

---

## Content (Static, Tiếng Việt)

Trang này target **organizer Việt Nam** → viết tiếng Việt là chính, tên thương hiệu giữ nguyên tiếng Anh (Ticketpeak, VNPay, TOTP...).

| Section | Ngôn ngữ |
|---|---|
| Hero, Stats, Solutions | Tiếng Việt |
| Testimonials | Tiếng Việt (tên người / tổ chức fictional placeholder) |
| CTA | Tiếng Việt |
| Nav link "For Organizers" | Tiếng Anh (giữ consistent với nav hiện tại) |

---

## Dependency

- Không cần API endpoint mới — trang static hoàn toàn.
- Link CTA "Đăng ký" trỏ về `/auth` — đã có (Plan 020).
- Link "Partner Portal" ở nav trỏ về `/b2b/dashboard` — đã có placeholder (Plan 023).
- Không cần thay đổi `+layout.svelte` ngoài thêm 1 link nav (optional, có thể làm sau).

---

## Acceptance Criteria

- [ ] Route `/for-organizers` trả về 200, render trong layout chính (có nav + search bar).
- [ ] Hero: dark background, headline, 2 CTA buttons (Get Started → `/auth`, How It Works → scroll).
- [ ] Stats bar: 4 số trên desktop, 2 cột trên mobile.
- [ ] Solutions grid: 3 cards, icons, hover effect, responsive stacked mobile.
- [ ] How It Works: 3 bước, số decorative lớn, mô tả rõ ràng.
- [ ] Testimonials: 2 quotes, avatar placeholder, tên/tổ chức.
- [ ] CTA bottom: dark bg, headline, primary button link đến `/auth`.
- [ ] Mobile responsive toàn trang — không có horizontal scroll, text không bị clip.
- [ ] `<title>` và `<meta description>` đúng.
- [ ] Link "For Organizers" trong top utility nav (optional — nếu làm thì nav không bị broken trên mobile).
- [ ] Không có console error, `npm run check` pass.

---

## Status

`planned`

## Outcome

_To be filled after implementation._
