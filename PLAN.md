# PLAN.md — Ticketpeak

Development roadmap organized by phase and detailed per-module checklists.

## Legend

| Symbol | Meaning |
|--------|---------|
| `[ ]` | Not started |
| `[x]` | Done |
| `[-]` | In progress |
| `[~]` | Skipped / deferred |

---

## Phases

Phases are ordered by dependency — later phases build on the output of earlier ones. Do not start a new phase until the current one's core items are complete.

| Phase | Name | Modules | Goal |
|-------|------|---------|------|
| **1** | Foundation | `auth`, `iam`, `account` | Users can register, log in, and be authorized |
| **2** | Core Entities | `organization`, `venue`, `event`, `offer` | Organizers can create events with venues and ticket tiers |
| **3** | Transactions | `order`, `payment`, `ticket` | Buyers can purchase tickets, receive QR tickets, and be checked in |
| **4** | Secondary Market | `resale` | Ticket resale with anti-scalping enforcement |
| **5** | Growth | `search`, `discovery`, `notification`, `promotion` | Users find events, receive alerts, and use promotions |
| **6** | Intelligence | `report`, `admin` | Organizers analyze performance; admins manage the platform |

---

## Phase 1 — Foundation

> Goal: a working auth system before anything else.
> Every subsequent module depends on JWT + RBAC established here.

### `auth`
- [ ] User registration (email + password)
- [ ] Login — return access token + refresh token
- [ ] Refresh access token
- [ ] Logout (revoke refresh token)

### `iam`
- [x] Define system roles: `ADMIN`, `ORGANIZER`, `BUYER`
- [x] Define permission set per role
- [ ] `@RequirePermission` annotation / interceptor for Spring controllers
- [ ] Route guard in SvelteKit by role

### `account`
- [ ] View own profile
- [ ] Update personal info (name, avatar, phone number)
- [ ] Upload avatar to MinIO
- [ ] Manage addresses (add, edit, delete, set default)
- [ ] User preferences (language, timezone, notification settings)

---

## Phase 2 — Core Entities

> Goal: organizers can create a fully configured event with venue, seating, and ticket tiers.
> No payments or orders yet — focus on content only.

### `organization`
- [ ] Create an organization (name, logo, description, website)
- [ ] Upload org logo to MinIO
- [ ] Invite members to org via email, in-app
- [ ] Remove / leave organization
- [ ] Approval workflow: org events require admin approval before publishing
- [ ] List organizations the current user belongs to
- [ ] Public org profile page

### `venue`
- [ ] Create venue (name, address, capacity, description, photos)
- [ ] Upload venue photos to MinIO
- [ ] Update / delete venue
- [ ] Create manifest (zones, sections, rows, seats)
- [ ] Seat map builder: drag-and-drop seating chart editor
- [ ] Assigned seating: label individual seats (A1, A2…)
- [ ] General Admission zone: capacity only, no specific seats
- [ ] Save and reuse manifests across multiple events

### `event`
- [ ] Create event (title, description, banner, category, tags)
- [ ] Upload event banner / photos to MinIO
- [ ] Attach venue and select manifest
- [ ] Set date, time, and timezone
- [ ] Multi-session support (recurring / multiple event dates)
- [ ] Publish event (`DRAFT` → `PUBLISHED`)
- [ ] Cancel event (`CANCELLED`) and notify ticket holders
- [ ] Postpone event (`POSTPONED`) and update date
- [ ] Clone event (reuse structure for a new event)
- [ ] Full status lifecycle: `DRAFT` → `PUBLISHED` → `ON_SALE` → `ENDED` → `CANCELLED`
- [ ] Public event page

### `offer`
- [ ] Create ticket tier (VIP, Early Bird, Standard…)
- [ ] Set price, quantity, and description per tier
- [ ] Map tier to zone / section in venue manifest
- [ ] Presale window: early access for a specific group (email whitelist)
- [ ] Sale window: open and close dates for each tier
- [ ] Max tickets per order limit
- [ ] Create promo codes (percentage or fixed amount discount)
- [ ] Limit promo code usage (globally and per user)
- [ ] Hide / show tier without deleting it
- [ ] Auto sold-out when inventory reaches zero

---

## Phase 3 — Transactions

> Goal: buyers purchase tickets, receive valid TOTP QR tickets, and organizers check in attendees at the gate.
> This is the core value of the platform — test this phase most rigorously.

### `order`
- [ ] Add tickets to cart
- [ ] Temporary seat reservation on cart entry (Redis TTL)
- [ ] Display countdown timer for reservation hold
- [ ] Release reservation on TTL expiry, return slots to pool
- [ ] Apply promo code to order
- [ ] Order summary screen before checkout
- [ ] Checkout: hand off to payment
- [ ] Confirm order after successful payment
- [ ] Order lifecycle: `PENDING` → `CONFIRMED` → `CANCELLED` / `REFUNDED`
- [ ] Buyer order history
- [ ] Cancel order (within allowed cancellation window)
- [ ] Handle concurrent purchase race condition (pessimistic lock or DB constraint)

### `payment`
- [ ] Integrate payment gateway (VNPay / Stripe)
- [ ] Create payment intent on checkout
- [ ] Webhook receiver for gateway callbacks
- [ ] On payment success: confirm order and issue tickets
- [ ] On payment failure: release reservation
- [ ] Refund buyer when event is cancelled
- [ ] Refund on buyer request (within policy window)
- [ ] Post-event payout to organizer (scheduled job)
- [ ] Deduct platform commission before payout
- [ ] Organizer transaction history
- [ ] Real-time revenue dashboard for organizer

### `ticket`
- [ ] Issue ticket after order is confirmed
- [ ] Each ticket has a unique TOTP secret stored encrypted in DB
- [ ] Generate QR code on-demand from TOTP (never cached, never stored as image)
- [ ] QR code rotates every 30 seconds
- [ ] Buyer ticket screen (QR + event info)
- [ ] Check-in: scan QR and validate TOTP at time of scan
- [ ] Check-in: reject already-used tickets (idempotent)
- [ ] Check-in interface for gate staff (web / mobile)
- [ ] Transfer ticket to another user (one tap)
- [ ] Cancel transfer before recipient accepts
- [ ] Ticket lifecycle: `ISSUED` → `TRANSFERRED` / `CHECKED_IN` / `VOID`
- [ ] Void ticket on order refund

---

## Phase 4 — Secondary Market

> Goal: buyers can resell tickets with platform-enforced anti-scalping rules.

### `resale`
- [ ] Buyer creates a resale listing for an owned ticket
- [ ] Set resale price (capped at max % above face value — anti-scalping)
- [ ] Organizer configures resale price cap per event
- [ ] Limit number of listings per user per event
- [ ] Public resale listings for an event
- [ ] Buy a resale ticket (create resale order)
- [ ] Platform deducts commission on successful resale
- [ ] Automatically transfer ticket to new buyer after payment
- [ ] Seller cancels listing before a buyer claims it
- [ ] Listing lifecycle: `ACTIVE` → `SOLD` / `CANCELLED`

---

## Phase 5 — Growth

> Goal: users discover relevant events, receive timely notifications, and benefit from promotions.

### `search`
- [ ] Full-text search across event titles, organizers, and venues
- [ ] Filter by category, city, date range, price, availability
- [ ] Faceted search (show result counts per filter value)
- [ ] Ranking by relevance and popularity
- [ ] Search suggestions / autocomplete
- [ ] Vietnamese language support (tokenizer, diacritic folding)

### `discovery`
- [ ] Homepage: trending events by city
- [ ] "Near you" section (geolocation)
- [ ] "Coming up" section by preferred category
- [ ] Similar events (by category, venue, organizer)
- [ ] Personalized feed based on purchase and browsing history
- [ ] Follow organizer / artist / venue
- [ ] Notify followers when a followed organizer publishes a new event

### `notification`
- [ ] Email: order confirmation
- [ ] Email: e-ticket delivery after payment
- [ ] Email: event reminder before event date
- [ ] Email: event cancelled / postponed
- [ ] Email: payout completed (organizer)
- [ ] Web push / mobile push notifications
- [ ] In-app notifications with read / unread state
- [ ] Notification preferences (enable / disable per type)
- [ ] Notify when a resale ticket becomes available for a followed event
- [ ] Notify when a ticket transfer is received

### `promotion`
- [ ] Create coupon codes (percentage or fixed amount discount)
- [ ] Usage limits (global and per-user)
- [ ] Coupon expiry date
- [ ] Campaign: group multiple events / offers under one promotion
- [ ] Early Bird auto-expires based on offer sale window
- [ ] Flash sale: discounted price within a limited time window
- [ ] Personal vouchers (issued directly to specific users)
- [ ] Campaign performance report

---

## Phase 6 — Intelligence

> Goal: organizers make data-driven decisions; admins govern and audit the platform.

### `report`
- [ ] Revenue dashboard: total, by day, by ticket tier
- [ ] Attendance report: check-in rate, no-show rate
- [ ] Sales channel breakdown: primary vs resale
- [ ] Conversion funnel: view → cart → checkout → paid
- [ ] Export reports to CSV / Excel
- [ ] Cross-event performance comparison
- [ ] Real-time ticket sales chart on event day

### `admin`
- [ ] Platform overview dashboard: users, events, revenue, tickets
- [ ] Approve / reject events before publishing
- [ ] Approve / reject user KYC submissions
- [ ] Lock / unlock user accounts
- [ ] Take down / hide events that violate policy
- [ ] Manage organizers and organizations
- [ ] Audit log: record every admin action
- [ ] System configuration (anti-scalping cap, platform fee rate, etc.)
- [ ] View and resolve buyer / organizer disputes

---

## Dependency Map

```
auth ──────────────────────────────────────────► all modules
iam ───────────────────────────────────────────► all modules
account ───────────────────────────────────────► organization, payment
organization ──────────────────────────────────► event
venue ─────────────────────────────────────────► event
event ─────────────────────────────────────────► offer, report
offer ─────────────────────────────────────────► order, promotion
order ─────────────────────────────────────────► payment, ticket
payment ───────────────────────────────────────► ticket, report
ticket ────────────────────────────────────────► resale
resale ────────────────────────────────────────► payment
event + account ───────────────────────────────► search, discovery
order + ticket + payment ──────────────────────► notification
```

---

## Notes

- A module is considered **done** when: API endpoints are tested, UI is connected, and no downstream modules are blocked.
- **Phase 1–3 = MVP** — enough to demo the full flow: create event → buy ticket → check in.
- **Phase 4–6 = V1** — production-ready for real users.
- Update this file whenever a task is started (`[-]`) or completed (`[x]`).
