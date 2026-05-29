# 023 — Organizer Portal B2B Flow

## Description

Implement the B2B Organizer Portal Flow (`/goal làm flow cho organizer`) to provide event organizers with a complete business management experience (Event Dashboard, Event Creator, Seating/Offer Configurations, Sales Analytics, and Check-In Gates). This includes:
1. Enhancing the Spring Boot backend with a `GET /api/partner/organizations` endpoint to retrieve active organizations for the logged-in organizer account.
2. Creating an interactive SvelteKit Dashboard at `/b2b/dashboard` to manage events, publish, open sales, clone, and view analytics.
3. Building an Event Details editor under `/b2b/dashboard/events/[id]`.
4. Developing an immersive smartphone gate Check-In Scanner simulator at `/b2b/check-in/[eventId]` integrating with the backend `POST /api/partner/events/{eventId}/check-in` scan APIs.

## Acceptance Criteria

- [ ] Spring Boot backend exposes `GET /api/partner/organizations` returning the organizations owned by or actively membered by the logged-in organizer.
- [ ] Organizer can sign in at `/b2b/login` and is redirected to `/b2b/dashboard`.
- [ ] `/b2b/dashboard` displays KPI cards (Total Events, Total Revenue, Tickets Sold, Check-In rate) and allows switching between multiple organizations if applicable.
- [ ] Organizer can create a new event (POST to `/api/partner/events`) from the dashboard with chronological date validation.
- [ ] Organizer can execute event lifecycle operations (Publish, Open Sales, Cancel, Postpone, Clone, and Resume) with clean API integrations.
- [ ] Event Details view `/b2b/dashboard/events/[id]` offers event metadata updating, pricing levels/offers inspection, and direct links to the Check-In Simulator.
- [ ] Check-In Scanner Simulator at `/b2b/check-in/[eventId]` models a smartphone gate scan device with clean, high-fidelity UI states (green for success, red for scan errors like invalid OTP, duplicate scan, wrong event).
- [ ] Side-by-side QR payload generator utility lets developers generate/copy dynamic TOTP QR payloads (`ticketId:otp`) in real-time to verify the Spring Boot check-in scan logic.
- [ ] All SvelteKit page views comply with strict TypeScript mode, single quotes, no semicolons, and HSL colors theme.
- [ ] Maven verification suite `./mvnw verify` and Svelte checks compile cleanly and successfully.

## Status
`done`

## Outcome

Successfully completed the implementation of the B2B Event Organizer Portal Flow:
1. Enhanced the Spring Boot backend by adding `GET /api/partner/organizations` and `GET /api/partner/events/{eventId}/check-in/tickets` to allow organizers to query their active organizations and issued event tickets (with active simulated OTP codes).
2. Developed the typesafe SvelteKit Dashboard route at `/b2b/dashboard` powered by Svelte 5 state runes, including KPI cards, active organization selectors, and dialog sheets for Event Creation and Cloning.
3. Implemented the Event Details editor page at `/b2b/dashboard/events/[id]` with visual ticket sales inventory progress and tier pricing grids.
4. Created an immersivesmartphone Check-In Simulator at `/b2b/check-in/[eventId]` featuring success/error camera visuals and a side-by-side attendee ticket simulation dashboard.
5. All backend verification tests (`./mvnw verify`), Svelte type diagnostics (`npm run check`), and strict linter rules (`npm run lint`) passed successfully.
