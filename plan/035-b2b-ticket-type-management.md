# B2B Ticket Type Management

## Description
This plan outlines the steps to add Ticket Type management capabilities to the B2B Event Detail page, and integrate real Ticket Types into the Offer creation flow instead of using mocked data.

## Acceptance criteria
- Organizers can create, edit, and view Ticket Types for an event.
- The Offer creation and edit modals use a dropdown to select from the event's actual Ticket Types.
- No more mocked `tt-1` or `tt-2` IDs are used in the Offer form.

## Status
done

## Outcome
Implemented a "Manage Ticket Types" modal inside the Offers tab that allows creating and deleting Ticket Types. Integrated Ticket Types dynamically into the Offer creation/edit form via API fetching in `+page.server.ts`. Mock IDs like `tt-1` were replaced with actual UUIDs. Tested the types with `npm run check` and no errors were found.
