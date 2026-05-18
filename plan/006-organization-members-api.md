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
- [x] Organization owners and authorized organizers can list members for an organization they can access.
- [x] Member records expose the current `status` and audit timestamps.
- [x] Organization owners can mark members as `REMOVED` when they are kicked out.
- [x] Members can mark their own membership as `LEFT` when they leave voluntarily.
- [x] Re-adding a previously removed or left member reuses the unique membership row or otherwise respects the existing uniqueness constraint.
- [x] Membership status transitions are validated and persisted correctly.
- [x] Public API responses use record DTOs wrapped in `ApiResponse<T>`.
- [x] Validation failures follow the standard `VALIDATION_FAILED` response shape.
- [x] Integration tests cover member listing, status transitions, access checks, uniqueness handling, and validation failures.

## Status
`done`

## Outcome
Implemented `OrganizationMemberService` and `OrganizationMemberController` providing endpoints for member listing, status inspection, member removal, leaving organizations, and member restoration. All transitions respect the `OrganizationMemberStatus` model (`ACTIVE`, `LEFT`, `REMOVED`) and enforce RBAC and ABAC permissions (`ORG_MEMBER:REMOVE`, `ORG_MEMBER:INVITE`). Verified with comprehensive integration tests in `OrganizationMemberControllerIT`.
