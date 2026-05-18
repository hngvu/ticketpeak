# 004 Organization API - Ownership & Membership

## Description
Implement the `organization` API layer on top of the existing organization persistence model.
This module should let authenticated users create and manage organizations they own, while also supporting the core membership records needed for organizer collaboration.

The first version should cover:
- organization creation
- organization profile updates
- owner-based access control

This plan intentionally stops short of invitation-token flows and permission grants.
Invitation handling is being planned separately because the codebase already has a dedicated `OrganizationInvitation` entity, and permission grants are being planned in the IAM permission API.
Member lifecycle is being planned separately because the codebase has a dedicated `OrganizationMember` entity with its own status transitions.

The API must protect server-managed fields such as `id`, `ownerAccountId`, `status`, `createdAt`, and `updatedAt`, and it must not allow arbitrary users to edit organizations they do not own or belong to.

## Acceptance Criteria
- [ ] Only admins can create a new organization and assign organizer owner.
- [ ] Organization creation validates required fields and generates a unique slug.
- [ ] Organization owners/Authorized organizers can update editable organization fields such as `name`, `bio`, `logoUrl`, `websiteUrl`, `email`, `phone`, `cityId`, and `countryCode`.
- [ ] Non-owners, authorized organizers cannot modify organization details.
- [ ] Public API responses use record DTOs wrapped in `ApiResponse<T>`.
- [ ] Validation failures follow the standard `VALIDATION_FAILED` response shape.
- [ ] Integration tests cover organization creation, updates, permission checks, slug uniqueness, and validation failures.

## Status
`planned`

## Outcome
TBD
