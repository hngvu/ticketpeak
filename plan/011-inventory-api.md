# Plan 011: Inventory API

## Description

The Inventory API is responsible for managing ticket inventory (both General Admission capacity and Reserved Seating allocations) with extreme high-concurrency performance, avoiding double-bookings, and handling real-time reservation holds.

Following the Ticketmaster standard:
1. **Standalone Event Model**: Each night, performance, or show is treated as a standalone `Event` resource. There are no nested event sessions.
2. **Stable Place Identifier**: Every physical seat inventory record uses a compound stable ID format `{eventId}:{seatId}` as the unique runtime key.
3. **Atomic GA Tracking**: General Admission (GA) hold and purchase operations utilize atomic database updates (using decrement-with-bounds queries) to eliminate race conditions.
4. **Reserved Seat Holds**: Bookings are tracked in an `inventory_hold_place` table with a unique constraint on `(event_id, seat_id)` to ensure hardware-level isolation.
5. **Lazy Cleanup & Scheduled Expiry**: Expired holds are cleaned up periodically by a background task, and read queries filter out expired holds lazily.
6. **ONSALE Initialization Trigger**: Inventory records are generated when the event status transitions to `ONSALE`, rather than when initially drafted.

---

## Status

`done`

---

## Acceptance Criteria

### 1. Database Schema
- [x] Create `inventory_hold_place` table to store seat holds with fields:
  - `id` (UUID Primary Key)
  - `event_id` (UUID, NOT NULL)
  - `seat_id` (UUID, NULL for GA, NOT NULL for reserved seating)
  - `hold_token` (VARCHAR(64), NOT NULL) - correlates to checkout session / cart
  - `expires_at` (TIMESTAMPTZ, NOT NULL)
  - `created_at` (TIMESTAMPTZ, NOT NULL DEFAULT now())
  - Unique constraint `uq_event_seat_hold (event_id, seat_id)` for reserved seating (only active/non-expired holds, or absolute constraint if deleted on expiry).
- [x] Create `ga_inventory` table to track GA availability per event and area:
  - `id` (UUID Primary Key)
  - `event_id` (UUID, NOT NULL)
  - `area_id` (UUID, NOT NULL)
  - `capacity` (INTEGER, NOT NULL)
  - `held` (INTEGER, NOT NULL DEFAULT 0)
  - `purchased` (INTEGER, NOT NULL DEFAULT 0)
  - `version` (BIGINT, NOT NULL DEFAULT 0)
  - Unique constraint `uq_ga_event_area (event_id, area_id)`
- [x] Create indexes for quick lookups on `hold_token`, `expires_at`, and `event_id`.

### 2. Inventory Initialization
- [x] Implement inventory initialization listener triggered when an `Event` transitions to status `ONSALE`.
- [x] For reserved seating: Fetch the venue seating manifest (sections, rows, seats), and initialize availability in Redis or DB.
- [x] For GA seating: Populate `ga_inventory` based on the venue's GA area capacity.
- [x] Ensure initialization is completely idempotent using compound stable keys `{eventId}:{seatId}` for seats.

### 3. Reserved Seating Reservation & Hold Flow
- [x] Implement endpoint to request holds: `POST /api/offers/hold` or `POST /api/orders/hold`.
- [x] Acquire locks/reservations atomically.
- [x] For reserved seating, insert hold records into `inventory_hold_place` using `hold_token`. If constraint `uq_event_seat_hold` is violated, throw `ApiException` with 409 Conflict status.
- [x] Return the active hold details including the `holdToken` and `expiresAt` timestamp.

### 4. General Admission (GA) Reservation Flow
- [x] Request holds for a specific quantity in a GA area.
- [x] Execute an atomic update to verify capacity and increment the `held` count:
  ```sql
  UPDATE ga_inventory
  SET held = held + :qty
  WHERE event_id = :eid AND area_id = :aid AND (capacity - held - purchased) >= :qty
  ```
- [x] Create an inventory hold record mapping the hold quantity to the `hold_token`.

### 5. Hold Release & Cleanup
- [x] Implement background worker `@Scheduled` (runs every 30 seconds) that deletes expired holds from `inventory_hold_place` and atomically decrements `held` counts in `ga_inventory`.
- [x] When an order is completed, transition holds to permanent sales by updating `purchased` and deleting the hold records.
- [x] Ensure lazy cleanup: read queries must check `expires_at > now()` to guarantee accurate real-time inventory counts even if the background cleaner has not run yet.

---

## Outcome

We have fully implemented Plan 011 of the Inventory API backend:
1. **Flyway Migrations**: Applied `V16__create_inventory_schema.sql` successfully.
2. **Event-Driven Initialization**: Hooked transactional listener `InventoryEventListener` to spring event status transitions.
3. **Core Services**: Built atomic decrements in `GAInventoryRepository` and locks in `InventoryHoldPlaceRepository`, managed via `InventoryService`.
4. **Scheduled Cleaner**: Set up `InventoryCleanupService` to run every 30 seconds for background expiries.
5. **Buyer Controller**: Exposed buyer-facing routes `/api/inventory/hold` and `/api/inventory/event/{id}`.
6. **Robust Validation**: Verified through a comprehensive integration test suite `InventoryControllerIT.java` showing 100% green test passes across both targeted checks and the full API verification command.
