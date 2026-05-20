# 009 Event API - Event Domain & Multi-Session Scheduling

## Description
Implement the `event` API layer including support for event creation, update, classification, attraction management, and multi-session schedule configuration. 
This module allows organizers to build events, assign them to venues with snapshots of layout manifests, manage their status lifecycles, and handle multiple sessions.

The event domain contains the following entities:
- `Event`: Represents the main event listing, including title, description, category, tags, and lifecycle status.
- `EventSession`: Supports multi-session / recurring dates under a single event.
- `Attraction`: Represents performers, artists, or hosts associated with the event.
- `Classification`: Provides structured categories (Segment, Genre, Sub-genre) modeled after the Ticketmaster Partner API.

The status lifecycle of an Event flows through:
`DRAFT` -> `PUBLISHED` -> `ON_SALE` -> `ENDED` -> `CANCELLED` (with `POSTPONED` as an explicit postponed state).

When an event transitions to `PUBLISHED`, the system automatically snapshots the assigned venue's published manifest to protect the event's ticket and seating configurations from future venue/manifest modifications.

## Acceptance Criteria
- [ ] Organizers can create, retrieve, update, and list events under their organization.
- [ ] Event write endpoints validate required fields and enforce organization boundaries (only admins, organization owners, or active members can perform operations).
- [ ] Events support multi-session scheduling (`EventSession` instances), allowing multiple start/end date pairs per event.
- [ ] Organizers can manage `Attraction` and `Classification` associations for events.
- [ ] Event status transitions follow strict validation rules (e.g. cannot publish without at least one session, venue, and manifest).
- [ ] When an event transitions to `PUBLISHED`, a snapshot of the venue's active manifest is created using `VenueService.cloneManifest`.
- [ ] Organizers can postpone an event, updating the session times and transitioning the event/sessions to a postponed status.
- [ ] Organizers can cancel an event, transitioning the status to `CANCELLED` (ready for order/ticket cancellation notification).
- [ ] Organizers can clone an existing event, creating a new event in `DRAFT` status with the same structure, categories, attractions, and sessions.
- [ ] Public endpoints allow searching, filtering, and paginating public events (`PUBLISHED` or `ON_SALE`).
- [ ] Public endpoints return clean response shapes wrapped in `ApiResponse<T>`, protecting sensitive organizer/internal data.
- [ ] Integration tests cover event CRUD, multi-session scheduling, cloning, status transitions, validation errors, organization authorization checks, and public search/filtering.

## Status
`done`

## Outcome
Implemented the full Event API layer with the following structure:

**Public endpoints** (`/api/events`, `/api/attractions`, `/api/classifications`):
- Fully public search with keyword expansion across event title/description, attraction names/descriptions, classification names, and venue names/city/country.
- Hierarchical classification filtering (recursive Segment → Genre → Sub-genre traversal).
- DRAFT events are always excluded from public search and `GET /api/events/{id}`.

**Partner endpoints** (`/api/partner/events/**`):
- Full event CRUD (create, update, delete) restricted to ORGANIZER and ADMIN roles.
- Organization membership enforcement: only org owners/active members can manage events.
- `GET /api/partner/events/{id}` allows secure retrieval of DRAFT events for partners.
- Publish (with venue manifest snapshot), postpone, cancel, and clone lifecycle transitions.

**Internal/Admin endpoints** (`/api/internal/attractions`, `/api/internal/classifications`):
- Write-only (POST, DELETE) endpoints for ADMIN role only.

**Tests**: 8 integration tests covering CRUD, lifecycle, keyword search, hierarchical classification search, and DRAFT isolation — all passing.
