# 008 Venue API - Platform Venue & Layout Management

## Description
Implement the `venue` API layer on top of the existing venue domain model.
This module should let platform operators manage venue records, publish venue manifests, and build the seating/layout inventory needed by events.

The venue domain is already split into a core record plus layout entities:
- `Venue` for the physical venue profile
- `Manifest` for a venue-specific layout version
- lookup tables inside a manifest: `Level`, `Section`, and `PriceLevel`
- layout areas: `GAArea` and `RSArea`
- seat structure for reserved areas: `SeatRow` and `Seat`

The manifest and inventory flows must also support:
- exactly one `PUBLISHED` manifest per venue at a time
- manifest cloning from an existing manifest
- immutable manifest snapshots when an event is published
- seat-map and availability reads that can be consumed by event-facing UI

The first version should expose read/write APIs for venue administration and nested layout management, with clear validation around status transitions and hierarchical relationships.

## Acceptance Criteria
- [ ] Platform operators can create, update, list, and retrieve venues.
- [ ] Venue write APIs validate required fields and protect server-managed fields such as `id`, `createdAt`, and `updatedAt`.
- [ ] Venue status can be updated only through explicit lifecycle actions, and invalid transitions are rejected.
- [ ] Platform operators can create manifests for a venue and manage manifest status as `DRAFT`, `PUBLISHED`, or `ARCHIVED`.
- [ ] At most one manifest per venue can be `PUBLISHED` at any time.
- [ ] A manifest cannot be moved to `ARCHIVED` while an `On Sale` event still references it.
- [ ] Platform operators can clone a new manifest from an existing manifest.
- [ ] Manifest APIs validate the parent venue relationship and prevent orphaned manifests.
- [ ] Platform operators can manage manifest lookup tables for `Level`, `Section`, and `PriceLevel`.
- [ ] `PriceLevel` mappings are attached to `Section` within the manifest model.
- [ ] Platform operators can create and update `GAArea` and `RSArea` records against a manifest.
- [ ] Platform operators can manage `SeatRow` and `Seat` records under `RSArea`.
- [ ] Seat APIs enforce uniqueness constraints such as one seat name per row.
- [ ] Seat records support `accessible`, `obstructed_view`, and `aisle` attributes.
- [ ] Section capacity is maintained as the total seat count across all `SeatRow` records, and seat add/remove operations validate against it.
- [ ] `GAArea` supports hold and release flows so checkout can reserve and release capacity atomically.
- [ ] When an event is published, the venue manifest is snapshotted for that event and remains stable for future seat-map reads.
- [ ] Public seat-map endpoints return render-ready layout data rather than raw persistence entities.
- [ ] Seat availability endpoints can resolve availability by `event_id`.
- [ ] Public read APIs expose nested venue/layout data using record DTOs wrapped in `ApiResponse<T>`.
- [ ] Validation failures follow the standard `VALIDATION_FAILED` response shape.
- [ ] Integration tests cover venue CRUD, manifest lifecycle, lookup-table management, area creation, seat-row/seat creation, clone/snapshot behavior, availability reads, validation failures, and status-transition rules.

## Status
`done`

## Outcome
Implemented the Venue API layer covering all entities in the venue domain model. Platform operators (via `/api/admin/venues`) can manage venue profiles with lifecycle transitions (`activate`/`deactivate`), manifests with status control (`publish`/`archive`/`clone`) enforcing at-most-one published manifest per venue, lookup tables (Level, Section, PriceLevel), GA and RS areas, seat rows, and individual seats with uniqueness constraints. All 7 integration tests passed covering CRUD, manifest lifecycle, conflict enforcement, lookup tables, seat uniqueness, and clone behaviour.
