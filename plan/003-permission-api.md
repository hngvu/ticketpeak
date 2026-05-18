# 003 Permission API - IAM Catalog & Account Grants

## Description
Implement the `iam` permission API layer on top of the existing `Permission` and `AccountPermission` models.
This module should define the reusable permission catalog and expose the grant/revoke workflows that organization features will depend on.

The first version should cover:
- permission catalog management for platform-level admins
- organization-scoped permission assignment to accounts
- revocation and reactivation of account permissions
- lookup endpoints for a member's permissions within an organization

The API must enforce the uniqueness and scope rules already implied by the data model:
permission codes are unique, account-permission rows are unique per `(account_id, permission_code, organization_id)`, and organization-scoped grants must not leak across organizations.

## Acceptance Criteria
- [x] Organization owners or authorized organizers can grant organization-scoped permissions to members.
- [x] Organization owners or authorized organizers can revoke or reactivate existing account permissions.
- [x] Permission lookup endpoints can list the active permissions for an account within a specific organization.
- [x] Duplicate account-permission grants are prevented by the API and validated gracefully.
- [x] Permission assignment cannot cross organization boundaries.
- [x] Public API responses use record DTOs wrapped in `ApiResponse<T>`.
- [x] Validation failures follow the standard `VALIDATION_FAILED` response shape.
- [x] Integration tests cover permission catalog operations, grant/revoke flows, scope enforcement, duplicate prevention, and validation failures.

## Status
`done`

## Outcome
Implemented the IAM permission catalog endpoints (`/api/permissions`) for platform admins and organization-scoped grant/revoke/lookup endpoints (`/api/organizations/{orgId}/permissions`). Created `PermissionRepository`, `AccountPermissionRepository`, `PermissionService`, `PermissionConstants`, `PermissionController`, and `OrganizationPermissionController`. Added Flyway migration `V3__create_organization_schema.sql` for organization and member tables. Verified with 9 integration tests in `PermissionControllerIT` covering catalog management, grants, duplicate prevention, reactivation, revocation, and robust RBAC/ABAC authorization checks.
