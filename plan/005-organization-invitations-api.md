# 005 Organization Invitations API - Tokenized Join Flow

## Description
Implement the `organization` invitation API layer on top of the existing `OrganizationInvitation` model.
This module should handle the token-based invitation lifecycle that connects organization owners and invited accounts.

The first version should cover:
- invitation creation by organization owners
- invitation token generation and validation
- accepting or rejecting invitations
- expiration handling
- converting accepted invitations into active `OrganizationMember` records

The API must respect the current invitation schema:
each invitation belongs to exactly one organization, targets exactly one invitee account, carries a unique token, and has explicit status transitions.

## Acceptance Criteria
## Acceptance Criteria
- [x] Organization owners, authorized organizers can create invitation records for specific invitee accounts.
- [x] Invitation creation generates a unique token and stores an expiration timestamp.
- [x] Invitation tokens can be validated without exposing unrelated organization data.
- [x] Invitees (Account with role 'ORGANIZER') can accept or reject valid invitations.
- [x] Accepting an invitation creates or activates the corresponding organization membership record.
- [x] Rejected or expired invitations cannot be accepted again.
- [x] Invitation status transitions are enforced and persisted correctly.
- [x] Public API responses use record DTOs wrapped in `ApiResponse<T>`.
- [x] Validation failures follow the standard `VALIDATION_FAILED` response shape.
- [x] Integration tests cover invitation creation, token validation, accept/reject flows, expiration behavior, and membership creation on acceptance.

## Status
`done`

## Outcome
Implemented the Organization Invitations API handling the token-based join flow. Organization owners, admins, and members with `ORG_MEMBER:INVITE` permission can invite other `ORGANIZER` accounts. Invitees can list their invitations via `/api/organizations/invitations/me` and accept/reject them. Accepting an invitation automatically activates the `OrganizationMember` record. Test suite `OrganizationInvitationControllerIT` verifies all flows and role constraints successfully.
