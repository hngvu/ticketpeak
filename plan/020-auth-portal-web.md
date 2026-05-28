# 020 — Auth & Portal Entry UI (Web)

## Description

Implement ba entry point xác thực cho Ticketpeak web:

| Route | Dành cho | Mô tả |
|---|---|---|
| `/auth` | Buyers | Login + Register gộp một trang (tab hoặc toggle) |
| `/b2b` | Organizers | Portal landing + Login riêng |
| `/ops/login` | Admins | Login nội bộ — tách hoàn toàn |

Ba portal này chia sẻ cùng backend auth API nhưng có layout, brand tone, và redirect destination khác nhau.

---

## Route Structure (SvelteKit)

```
web/src/routes/
│
├── auth/
│   ├── +page.svelte              → Login / Register toggle (buyer)
│   └── +page.server.ts           → Xử lý form action: login, register
│
├── b2b/
│   ├── +layout.svelte            → B2B shell (khác hoàn toàn layout public)
│   ├── +page.svelte              → Organizer portal landing (marketing mini)
│   └── login/
│       ├── +page.svelte          → Organizer login form
│       └── +page.server.ts       → Form action: login → redirect /b2b/dashboard
│
└── ops/
    ├── +layout.svelte            → Ops shell (stripped, dark)
    └── login/
        ├── +page.svelte          → Admin login form
        └── +page.server.ts       → Form action: login → redirect /ops/dashboard
```

### Redirect sau login

| Portal | Redirect thành công | Redirect thất bại |
|---|---|---|
| `/auth` (buyer) | `/discover` (hoặc `?redirect=` param) | Stay, hiện error |
| `/b2b/login` | `/b2b/dashboard` | Stay, hiện error |
| `/ops/login` | `/ops/dashboard` | Stay, hiện error |

### Guard

- `/b2b/**` (trừ `/b2b` landing và `/b2b/login`): yêu cầu role `ORGANIZER` — redirect `/b2b-id/` nếu không có token.
- `/ops/**` (trừ `/ops/login`): yêu cầu role `ADMIN` — redirect `/ops/login` nếu không có token.
- `/auth`: nếu đã login (bất kỳ role), redirect về portal tương ứng.

Guard implement trong `+layout.server.ts` của từng portal shell.

---

## `/auth` — Buyer Login / Register

### Layout

```
┌─────────────────────────────────────────┐
│  NAV BAR (minimal — logo only)          │
├─────────────────────────────────────────┤
│                                         │
│  SPLIT: Left 55% hero / Right 45% form  │
│                                         │
│  LEFT (desktop only):                   │
│  gradient-discover bg                   │
│  Ticketpeak logo (white)                │
│  Tagline: "Your next event              │
│            starts here."                │
│  [Event collage / illustration]         │
│                                         │
│  RIGHT:                                 │
│  ┌─────────────────────────────────┐    │
│  │  [Login]  [Register]            │    │  tab toggle
│  │  ─────────────────────────────  │    │
│  │  LOGIN TAB:                     │    │
│  │  Email ________________________ │    │
│  │  Password _____________________ │    │
│  │  [Forgot password?]             │    │
│  │  [Sign in ──────────────────►]  │    │
│  │  ─ or ─                        │    │
│  │  [Continue with Google]         │    │
│  │                                 │    │
│  │  REGISTER TAB:                  │    │
│  │  Full name ___________________  │    │
│  │  Email ________________________ │    │
│  │  Password _____________________ │    │
│  │  [Create account ───────────►]  │    │
│  │  ─ or ─                        │    │
│  │  [Continue with Google]         │    │
│  └─────────────────────────────────┘    │
│                                         │
│  Are you an organizer?                  │
│  [Go to B2B portal →]                  │
├─────────────────────────────────────────┤
│  FOOTER (minimal)                       │
└─────────────────────────────────────────┘
```

### URL params

| Param | Ví dụ | Ghi chú |
|---|---|---|
| `tab` | `?tab=register` | Mở sẵn tab Register |
| `redirect` | `?redirect=%2Fevents%2Fconcert-xyz` | Sau login redirect về đây |

### Form Actions (`+page.server.ts`)

```
?/login   → POST /api/auth/login   → set cookie → redirect
?/register → POST /api/accounts/register + auto-login → set cookie → redirect
```

Token lưu trong **httpOnly cookie** `access_token` + `refresh_token` — không lưu localStorage.

### Error states

- Sai password: inline error dưới field Password, không toast.
- Email chưa verify (nếu có): banner warning với "Resend verification email".
- Rate limit (429): banner error "Too many attempts, try again in X minutes".

---

## `/b2b` — Organizer Portal Landing

### `/b2b` (landing page, public)

```
┌─────────────────────────────────────────┐
│  B2B NAV: Logo Ticketpeak for Business  │
│           [Sign in] [Get started]       │
├─────────────────────────────────────────┤
│  HERO                                   │
│  gradient-book bg (dark charcoal→blue)  │
│  "Sell tickets.                         │
│   Grow your audience."                  │  display-xl / on-primary
│  "Everything organizers need…"          │  body-lg / on-primary muted
│  [Get started free]  [Sign in →]       │
├─────────────────────────────────────────┤
│  FEATURES (3-up grid)                   │
│  [Venue builder] [Analytics] [Payouts]  │
├─────────────────────────────────────────┤
│  FOOTER                                 │
└─────────────────────────────────────────┘
```

Trang này public, không cần auth. Click "Get started" → `/auth?tab=register&redirect=/b2b/dashboard`. Click "Sign in" → `/b2b/login`.

### `/b2b/login`

```
┌─────────────────────────────────────────┐
│  B2B NAV (logo only)                    │
├─────────────────────────────────────────┤
│  CENTERED CARD (max-w-md, card-marketing│
│  -large chrome)                         │
│                                         │
│  Ticketpeak for Business                │  display-md
│  Sign in to your organizer account      │  body-sm / mute
│                                         │
│  Email ________________________________ │
│  Password _____________________________ │
│  [Forgot password?]                     │
│  [Sign in ─────────────────────────►]  │
│                                         │
│  ─────────── or ───────────            │
│  [Continue with Google]                 │
│                                         │
│  Don't have an account?                 │
│  [Create organizer account →]          │
│  → /auth?tab=register&redirect=/b2b/...│
│                                         │
│  ← Back to Ticketpeak                  │
│  → /                                   │
├─────────────────────────────────────────┤
│  FOOTER (minimal)                       │
└─────────────────────────────────────────┘
```

Không có left-side hero split — đơn giản hơn `/auth`. Card centered trên nền `canvas-soft`.

---

## `/ops/login` — Admin Login

```
┌─────────────────────────────────────────┐
│  FULL PAGE — nền ink (#303841)          │
│                                         │
│  CENTER:                                │
│  Ticketpeak [logo trắng]                │
│  Internal Tools                         │  caption / mute (màu xám nhạt)
│                                         │
│  ┌──────────────────────────────────┐   │
│  │ Email _________________________  │   │  form-input (dark variant)
│  │ Password _______________________ │   │
│  │ [Sign in ──────────────────────►]│   │  button-primary
│  └──────────────────────────────────┘   │
│                                         │
│  Authorized access only.                │  caption / mute
│                                         │
└─────────────────────────────────────────┘
```

**Không có:** register, Google OAuth, "Forgot password" (admin reset qua internal process), link về public site.

**Validate thêm:** Nếu login thành công nhưng role ≠ `ADMIN` → logout ngay, hiện "Access denied".

Trang này **không có nav bar, không có footer** — stripped hoàn toàn.

---

## Shared Auth Logic

### Cookie strategy

```
access_token   httpOnly, Secure, SameSite=Strict, max-age=900   (15 min)
refresh_token  httpOnly, Secure, SameSite=Strict, max-age=604800 (7 days)
```

Đặt cookie trong `+page.server.ts` form action, không bao giờ expose token ra client JS.

### Token refresh

Implement trong `hooks.server.ts`:
1. Mỗi request → đọc `access_token` cookie.
2. Nếu expired (hoặc không có) nhưng có `refresh_token` → gọi `POST /api/auth/refresh`.
3. Set lại cookie với token mới → tiếp tục request.
4. Nếu refresh cũng fail → xóa cả 2 cookie → redirect về login của portal tương ứng.

### `getCurrentUser()` helper

```ts
// lib/server/auth.ts
export function getCurrentUser(cookies: Cookies): User | null
```

Dùng trong tất cả `+page.server.ts` cần check auth.

---

## Components

| Component | Path | Notes |
|---|---|---|
| `AuthTabs.svelte` | `lib/components/auth/` | Login/Register tab toggle |
| `LoginForm.svelte` | `lib/components/auth/` | Email + password form |
| `RegisterForm.svelte` | `lib/components/auth/` | Name + email + password |
| `SocialAuthButton.svelte` | `lib/components/auth/` | Google OAuth button |
| `AuthErrorBanner.svelte` | `lib/components/auth/` | Inline error display |
| `AuthCard.svelte` | `lib/components/auth/` | Centered card wrapper (B2B, Ops) |

---

## API Endpoints Used

| Action | Method | Endpoint |
|---|---|---|
| Login | POST | `/api/auth/login` |
| Register | POST | `/api/accounts/register` |
| Refresh token | POST | `/api/auth/refresh` |
| Logout | POST | `/api/auth/logout` |

---

## Design System Mapping

| Element | Token |
|---|---|
| `/auth` left panel bg | `gradient-discover-start → gradient-discover-end` |
| `/auth` right panel bg | `canvas` |
| `/b2b/login` page bg | `canvas-soft` |
| `/b2b/login` card | `card-marketing-large` chrome |
| `/b2b` hero bg | `gradient-book-start → gradient-book-end` |
| `/ops/login` page bg | `ink` (#303841) |
| `/ops/login` input | `form-input` dark variant |
| Form input | `form-input` (height 40px, `rounded.sm`) |
| Primary CTA | `button-primary` (`rounded.pill`) |
| Error inline | `error` text + `error-soft` bg |

---

## Acceptance Criteria

- [ ] `/auth` hiện login tab mặc định; `?tab=register` mở register tab.
- [ ] `?redirect=` param hoạt động: sau login redirect đúng URL, fallback `/discover`.
- [ ] Login thành công → set httpOnly cookie, không expose token ra JS.
- [ ] Register thành công → auto-login → redirect.
- [ ] `/b2b/login` login thành công → redirect `/b2b/dashboard`.
- [ ] `/b2b/**` (trừ landing + login) guard: redirect `/b2b/login` nếu không có token hoặc role ≠ `ORGANIZER`.
- [ ] `/ops/login` login thành công → redirect `/ops/dashboard`.
- [ ] `/ops/login` login với role ≠ `ADMIN` → "Access denied", session không được tạo.
- [ ] `/ops/**` (trừ `/ops/login`) guard: redirect `/ops/login` nếu không có token hoặc role ≠ `ADMIN`.
- [ ] Token refresh tự động trong `hooks.server.ts`.
- [ ] Tất cả error states hiển thị đúng (wrong password, rate limit).
- [ ] `/ops/login` không có nav, footer, register, OAuth, forgot password.
- [ ] Responsive: mobile `/auth` collapse left panel, chỉ hiện form.

---

## Status

`planned`

## Outcome

_To be filled after implementation._
