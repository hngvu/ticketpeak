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
Implemented a "Manage Ticket Types" modal inside the Offers tab that allows creating and deleting Ticket Types.
Refactored the data model, replacing `slug` and `description` with standardized `code` fields (ADULT, CHILD, STUDENT, COMP, VIP, GROUP).
Created a database migration `V58` to apply changes, built a SvelteKit B2B API proxy to securely handle client-side queries, and updated all frontend forms/modals to use dropdown selectors. All frontend checks, unit, and integration tests passed, and changes were successfully pushed to `main`.
