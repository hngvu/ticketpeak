# 022 — Venue Color Configuration

## Description

Refactor the Venue module's seating manifest lookup tables (`Level`, `Section`, `PriceLevel`) to support an optional color configuration field.
This field allows event organizers to specify color values (e.g. Hex codes like `#FFD700`) for different sections and pricing tiers, enabling custom visual coloring on seating charts and ticket offers.

## Acceptance Criteria

- [x] A new Flyway migration `V37__add_color_to_venue_lookups.sql` is created, adding a `color` column (`VARCHAR(32)`) to `venue_level`, `section`, and `price_level` tables.
- [x] Java entities `Level`, `Section`, and `PriceLevel` are updated to include a `color` field mapped to the `color` column.
- [x] DTO response records `LevelResponse`, `SectionResponse`, and `PriceLevelResponse` are updated to return the `color` field.
- [x] DTO request record `UpsertLookupRequest` is updated to support an optional `color` field.
- [x] `VenueService` logic is updated to save the `color` value during level, section, and price level upsert operations.
- [x] `VenueService.cloneManifest` logic is updated to preserve the `color` values when copying lookups to a cloned manifest.
- [x] Integration and compilation checks verify the changes compile cleanly and all database tests pass.

## Status
`done`

## Outcome
Successfully completed the color configuration capability for venue seating layouts:
1. Created `V37__add_color_to_venue_lookups.sql` Flyway migration.
2. Updated JPA model entities (`Level.java`, `Section.java`, `PriceLevel.java`) with color fields.
3. Updated DTO responses and added overloaded backwards-compatible constructor in `UpsertLookupRequest.java`.
4. Integrated color persistence and cloning into `VenueService.java`.
5. Verified all integration tests passed without issues.
