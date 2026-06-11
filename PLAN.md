# PLAN.md — Ticketpeak

---

## Legend

| Symbol | Meaning |
|--------|---------|
| `[ ]` | Not started |
| `[x]` | Done |
| `[-]` | In progress |
| `[~]` | Skipped / deferred |

---

## The Product in One Sentence

A ticketing platform where venue managers publish seat maps, organizers price and sell events on top of them, buyers purchase tickets as rotating QR codes, and gate staff scan them at the door.

---

## Who Does What

| Role | Portal | Owns |
|------|--------|------|
| `VENUE_MANAGER` | `/b2b` | Venue, Manifest, Levels, Sections, Rows, Seats |
| `ORGANIZER` | `/b2b` | Event, Offers (price per seat category) |
| `BUYER` | consumer site | Orders, Tickets |
| `ADMIN` | `/ops` | Platform governance |

Organizer never touches seat layout. Venue Manager never touches pricing.

---

## MVP — The Loop That Must Close

```
Venue Manager creates a venue and publishes a seat map
  → Organizer creates an event on that seat map, sets prices per seat category, publishes
  → Buyer finds the event, picks a seat, pays
  → Buyer receives a rotating QR code
  → Gate staff scans the QR at the door
  → Buyer walks in ✓
```

---

## Phase 1 — Identity

**Goal:** anyone can create an account and log in. Roles are assigned from here.

### What the user experiences
- Visitor signs up → email verified → account active with base role `BUYER`
- Applies to become Organizer or is assigned Venue Manager by Admin
- Logs in → routed to the correct portal for their role
- Returning to a protected page without a session redirects to login

### Status
- [x] Register (email + password), base role = BUYER
- [x] Login / logout
- [x] Token refresh
- [x] Roles: BUYER (default), ORGANIZER, VENUE_MANAGER, ADMIN
- [x] Route protection by role

---

## Phase 2 — Venue Portal: Venue & Seat Map

**Actor: Venue Manager** (`/b2b`)
**Goal:** describe a physical space and provide a reusable seat map. Organizers consume it — they never edit it.

### What the Venue Manager experiences

**Setting up a venue:**
- *Demo Shortcut:* For this project, we completely skip the UI for building seat maps. Venue and Manifest data (Levels, Seat Categories, Sections, Rows, Seats) will be seeded directly into the database to easily demo the booking flow. 
- The B2B portal only allows creating/editing basic venue info (name, address, city, description).

### Status
- [-] B2B: create / edit venue info
- [~] Create / build manifest UI (Skipped for demo, manifest data is seeded directly to DB)

---

## Phase 3 — Organizer Portal: Event & Offers

**Actor: Organizer** (`/b2b`)
**Goal:** pick a published manifest, set a price against each seat category, publish the event. No seat layout work — pricing only.

### What the Organizer experiences

**Creating an event:**
- B2B → Events → New Event
- Fills in title, description, date/time, banner
- Browses venues → selects one → picks a published manifest
- Saves → Event status: **DRAFT**

**Configuring pricing (Offers):**
- Sees the seat categories defined in the manifest (read-only): name, color, seat count
- Creates one Offer per category they want to sell:
  - Name, price, quantity cap, sale window (opens at / closes at), max per order
  - Maps to a Seat Category — that's the only link to the seat map
- Optionally restricts an offer to a presale access list
- Offers stay inactive until their sale window opens

**Publishing:**
- Submits for admin review → **PENDING_APPROVAL**
- Admin approves → **PUBLISHED**
- Sale window opens → **ON_SALE**
- All offers close → **ENDED**

**Cancelling or postponing:**
- Cancel → all tickets voided, orders refunded, buyers notified
- Postpone → new date set, buyers notified and given refund option

### Event status flow
```
DRAFT ──► PENDING_APPROVAL ──► PUBLISHED ──► ON_SALE ──► ENDED
                │                                │
           REJECTED                         CANCELLED
                                                 │
                                            POSTPONED ──► ON_SALE (rescheduled)
```

### Status
- [ ] B2B: create / edit event (title, date, banner)
- [ ] Select venue → select published manifest
- [ ] View seat categories from manifest (read-only: name, color, seat count)
- [ ] Create offers: name, price, quantity, sale window, seat category
- [ ] Presale access list per offer
- [ ] Max tickets per order
- [ ] Submit for approval (DRAFT → PENDING_APPROVAL)
- [ ] Admin approve / reject
- [ ] Auto ON_SALE when sale window opens
- [ ] Auto ENDED when all offers close
- [ ] Cancel event → void tickets + refund + notify
- [ ] Postpone event → notify + allow refund request
- [ ] Public event page (consumer site)

---

## Phase 4 — Consumer Site: Purchase

**Actor: Buyer**
**Goal:** find an event, pick seats, pay, receive a ticket.

### What the Buyer experiences

**Finding the event:**
- Browses homepage or searches → opens event page
- Sees event details, seat map colored by offer tier, prices

**Selecting seats:**
- Clicks a seat on the map (RS) or selects quantity (GA)
- Seats held for **10 minutes** — countdown visible
- Timer expires → seats released, buyer must start over

**Checkout:**
- Reviews order: seat(s), offer tier, price, promo code if applied
- Pays (VNPay / Stripe)
- Payment succeeds → Order confirmed → Tickets issued → "Your Tickets"
- Payment fails → reservation released → back to event page

### Order status flow
```
PENDING ──► CONFIRMED
    │
    └──► CANCELLED  (payment failed or buyer cancelled)
              │
              └──► REFUNDED  (event cancelled or refund approved)
```

### Status
- [ ] Seat map on event page: colored by offer tier, clickable (RS) / quantity picker (GA)
- [ ] 10-minute reservation hold with countdown
- [ ] Release reservation on timeout
- [ ] Apply promo code
- [ ] Order summary before payment
- [ ] Payment (VNPay / Stripe)
- [ ] Confirm order + issue tickets on success
- [ ] Release reservation on failure
- [ ] Buyer order history
- [ ] Cancel order within allowed window
- [ ] Auto-refund when event is cancelled

---

## Phase 5 — Tickets & Check-In

**Actor: Buyer (holds ticket), Organizer (runs check-in)**
**Goal:** buyer holds a secure unforgeable ticket; gate staff verifies it in under 2 seconds.

### What the Buyer experiences
- Opens ticket → sees event info + QR code
- QR **rotates every 30 seconds** — screenshot sharing is ineffective

### What Gate Staff experiences (B2B → Check-in)
- Scans buyer's QR:
  - ✓ Valid + first scan → **CHECKED_IN** → let through
  - ✗ Already scanned → "already used"
  - ✗ Bad QR → "invalid"

### Ticket transfer
- Buyer taps Transfer → enters recipient email → **TRANSFER_PENDING**
- Recipient accepts → new ticket issued, original voided
- Sender cancels before accept → reverts to **ISSUED**

### Ticket status flow
```
ISSUED ──► CHECKED_IN
   │
   ├──► TRANSFER_PENDING ──► ISSUED (new owner)
   │              │
   │              └──► ISSUED (original, if cancelled)
   │
   └──► VOID
```

### Status
- [ ] Issue ticket on order confirmation
- [ ] QR generated on-demand from TOTP, rotates every 30s
- [ ] Buyer ticket screen (QR + event details)
- [ ] Check-in screen in B2B portal (gate staff)
- [ ] Validate QR at scan time, reject duplicates
- [ ] Transfer ticket → accept / cancel flow
- [ ] Void ticket on refund or event cancellation

---

## Phase 6 — Resale

**Actor: Buyer (seller), Buyer (new buyer)**
**Goal:** buyers resell at a fair price; organizer controls the cap.

### What the user experiences
- Seller: opens a ticket → List for Resale → sets price (capped by organizer's policy)
- New buyer: sees resale listings on event page → buys → same checkout flow → ticket transferred
- Seller can cancel listing before a buyer pays

### Resale listing status flow
```
ACTIVE ──► SOLD
   │
   └──► CANCELLED
```

### Status
- [ ] Create resale listing (organizer-set price cap enforced)
- [ ] Resale listings visible on event page
- [ ] Buy resale ticket → payment → ticket transfer
- [ ] Platform commission on sale, remainder to seller
- [ ] Seller cancels listing

---

## Phase 7 — Discovery & Notifications

**Actor: Buyer, Organizer**
**Goal:** buyers find events they care about; everyone gets the right message at the right time.

### What the Buyer experiences
- Homepage: trending events in their city
- Search: keyword, category, city, date, price
- Follow an organizer → notified on new event
- Notifications: order confirmed, reminder 24h before, event cancelled/postponed, transfer received

### What the Organizer experiences
- Notifications: event approved/rejected, payout completed

### Status
- [x] Homepage: trending events by city
- [x] Search with filters
- [ ] Follow organizer
- [ ] Email: order confirmation + ticket link
- [ ] Email: event reminder (24h before)
- [ ] Email: event cancelled / postponed
- [ ] In-app notifications (read / unread)
- [ ] Notification preferences per user

---

## Phase 8 — Payouts & Reporting

**Actor: Organizer, Admin**
**Goal:** organizers get paid; everyone can see what happened.

### What the Organizer experiences
- T+3 after event ends → platform calculates gross − refunds − commission → transfers to organizer
- Dashboard: revenue by day, by seat category, check-in rate
- Export to CSV

### What the Admin experiences
- Platform overview: revenue, active events, users
- Approve / reject events and org applications
- Lock accounts, take down events, resolve disputes

### Status
- [ ] T+3 payout job
- [ ] Organizer: revenue dashboard (by day, by seat category)
- [ ] Organizer: check-in / attendance report
- [ ] Organizer: CSV export
- [ ] Admin: platform overview dashboard
- [ ] Admin: approve / reject events and orgs
- [ ] Admin: lock accounts, take down events

---

## Dependency Order

```
Phase 1  Identity
   ├─► Phase 2  Venue Portal       (Venue Manager builds seat maps)
   │     └─► Phase 3  Organizer Portal  (Organizer prices events on top)
   │               └─► Phase 4  Consumer Site  (Buyer purchases)
   │                     └─► Phase 5  Tickets & Check-In  ← MVP complete
   │                           ├─► Phase 6  Resale
   │                           ├─► Phase 7  Discovery & Notifications
   │                           └─► Phase 8  Payouts & Reporting
```

---

## Notes

- **Phases 1–5 = MVP.** Done when all four actors can complete their part of the loop without a workaround.
- **Phases 6–8 = V1.** Production-ready.
- A phase is complete when a real user can walk through its flow above without hitting a dead end.
- Update task status as work progresses: `[ ]` → `[-]` → `[x]`.
