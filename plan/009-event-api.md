# 009 Event API - Core Lifecycle and Catalogs

## Description
Implement the `event` API layer on top of the existing event domain model and add the missing database schema for it.

The current `event` package contains four distinct models:
- `Event` for the core event aggregate
- `Classification` for hierarchical categories
- `Attraction` for performers, teams, or similar catalog entries
- `EventStatus` / `AttractionType` enums for lifecycle and catalog typing

The model shows that `Event` is an organization-owned, venue-linked aggregate with direct fields for schedule, sale window, and feature flags. `Classification` and `Attraction` are independent catalog entities in the same module, not associations on `Event` itself.

The first version should cover:
- event schema creation and repository support
- event creation and editing
- event publication and lifecycle transitions
- slug uniqueness
- organization and venue validation
- classification catalog CRUD
- attraction catalog CRUD
- public event read endpoints for buyer-facing and organizer-facing views

## Acceptance Criteria
- [ ] The event module has a matching Flyway migration that creates the `event`, `classification`, and `attraction` tables with columns aligned to the current entity model.
- [ ] Authorized organizers can create a new event under an organization they control. (permission: `EVENT:CREATE`)
- [ ] Event creation validates required fields such as organization, venue, title, slug, start time, timezone, and status.
- [ ] Event slugs are unique.
- [ ] Event updates are restricted to editable fields while the event remains in an editable state.
- [ ] Event lifecycle transitions are enforced for `DRAFT`, `PUBLISHED`, `ONSALE`, `OFFSALE`, `CANCELED`, `POSTPONED`,  `RESCHEDULED`, `COMPLETED`. (only organizers in organization with permission `EVENT:PUBLISH` can publish event; system auto schedule to change state `PUBLISHED` - `ONSALE` - `OFFSALE` - `COMPLETED`)
- [ ] The boolean feature flags `restrictSingleSeat`, `safeTixEnabled`, and `transferEnabled` are writable and validated.
- [ ] Classification records can be created, updated, listed, retrieved, activated, and deactivated.
- [ ] Classification records support parent-child hierarchy through `parentId` and preserve `level` ordering.
- [ ] Attraction records can be created, updated, listed, and retrieved, with `type`, `imageUrl`, and `description` fields exposed through DTOs.
- [ ] Public read endpoints expose event details, classification data, and attraction data through record DTOs wrapped in `ApiResponse<T>`.
- [ ] Validation failures follow the standard `VALIDATION_FAILED` response shape.
- [ ] Integration tests cover event creation, editing, slug uniqueness, lifecycle transitions, taxonomy CRUD, public reads, schema alignment, and validation failures.

## Status
`planned`

## Outcome
TBD
