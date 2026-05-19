# Plan: Refactor Venue Domain to Ticketmaster Model

## Description

Refactor the existing `venue` domain module from a simple hierarchical model to a highly flexible, inventory/pricing-focused model inspired by the Ticketmaster Partner API. 

The core changes include:
1. Moving `Section` and `Level` into lookup tables defined under a `Manifest`.
2. Introducing `PriceLevel` as a lookup table under a `Manifest`.
3. Taching areas into two distinct types: `GA Area` (General Admission, capacity-only) and `RS Area` (Reserved Seating, containing rows and seats).
4. Using `VARCHAR` for all primary and foreign key fields to support raw String IDs from Ticketmaster integration.
5. Renaming/Refactoring existing entities (`Venue`, `VenueManifest`, `SeatingArea`, `Zone`, `Section`, `SeatRow`, `Seat`) into the new model.

## Proposed Entity Relations

```
Venue (id: VARCHAR PK) 
  └── Manifest (id: VARCHAR PK, venue_id)
        ├── Level (id: VARCHAR, manifest_id -> Composite PK)
        ├── Section (id: VARCHAR, manifest_id -> Composite PK)
        ├── PriceLevel (id: VARCHAR, manifest_id -> Composite PK)
        ├── GAArea (id: VARCHAR PK, manifest_id, level_id, section_id, price_level_id, capacity)
        └── RSArea (id: VARCHAR PK, manifest_id, level_id, section_id, price_level_id)
              └── SeatRow (id: VARCHAR PK, rs_area_id, name, position_y)
                    └── Seat (id: VARCHAR PK, row_id, name, position_x, status, accessibility)
```

## Action Items

1. **Delete obsolete entities**:
   - `SeatingArea.java`, `SeatingAreaType.java`, `Zone.java`, `VenueManifest.java` (will be renamed to `Manifest.java`).
2. **Create new lookups and area structures**:
   - `Level.java` (and composite PK `LevelId.java`)
   - `PriceLevel.java` (and composite PK `PriceLevelId.java`)
   - `Section.java` (and composite PK `SectionId.java` - to replace previous Section)
   - `GAArea.java`
   - `RSArea.java`
3. **Refactor existing entities to use VARCHAR keys**:
   - `Venue.java`
   - `Manifest.java` (formerly `VenueManifest.java`)
   - `SeatRow.java` (to replace the previous `SeatRow.java`)
   - `Seat.java` (to replace the previous `Seat.java`)
4. **Update Enums**:
   - `VenueStatus`: `ACTIVE`, `INACTIVE`
   - `VenueManifestStatus`: `DRAFT`, `PUBLISHED`, `ARCHIVED` (renamed to `ManifestStatus.java`)
   - `SeatStatus`: `AVAILABLE`, `UNAVAILABLE`, `RESERVED`

## Acceptance Criteria

- All primary key and foreign key IDs in the `venue` domain models are of type `String` (`VARCHAR`).
- `Level`, `Section`, and `PriceLevel` are defined inside `Manifest` as lookup tables.
- `GAArea` and `RSArea` are separate models and map to Section, Level, and Price Level via raw string references or relations.
- `SeatRow` and `Seat` are linked only to `RSArea`.
- Compilation succeeds cleanly without errors.

## Status
`done`

## Outcome
Successfully refactored the entire `venue` domain module based on Ticketmaster's 3rd-party integration API structure. All tables are updated to use `String` (`VARCHAR`) keys, lookup tables (`Level`, `Section`, `PriceLevel`) are introduced relative to a specific `Manifest` with composite primary keys, and distinct areas (`GAArea`, `RSArea`) with corresponding hierarchical structures (`SeatRow`, `Seat`) are fully implemented.

