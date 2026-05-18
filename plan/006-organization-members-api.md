# 006 Organization Members API - Status & Access

## Description
Implement the `organization` member API layer on top of the existing `OrganizationMember` model.
This module should manage the member lifecycle inside an organization after an account has joined or been added.

The first version should cover:
- listing organization members
- reading member status within an organization
- deactivating, removing, or marking membership as left
- restoring or reactivating membership when appropriate

The API must respect the existing member status model:
membership rows are unique per `(organization_id, account_id)` and each row has an explicit `status` value rather than a boolean active flag.

## Acceptance Criteria
- [ ] Organization owners and authorized admins can list members for an organization they can access.
- [ ] Member records expose the current `status` and audit timestamps.
- [ ] Organization owners can mark members as `REMOVED` when they are kicked out.
- [ ] Members can mark their own membership as `LEFT` when they leave voluntarily.
- [ ] Re-adding a previously removed or left member reuses the unique membership row or otherwise respects the existing uniqueness constraint.
- [ ] Membership status transitions are validated and persisted correctly.
- [ ] Public API responses use record DTOs wrapped in `ApiResponse<T>`.
- [ ] Validation failures follow the standard `VALIDATION_FAILED` response shape.
- [ ] Integration tests cover member listing, status transitions, access checks, uniqueness handling, and validation failures.

## Status
`planned`

## Outcome
TBD
