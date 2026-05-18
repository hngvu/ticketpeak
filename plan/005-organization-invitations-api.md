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
- [ ] Organization owners, authorized orgainzers can create invitation records for specific invitee accounts.
- [ ] Invitation creation generates a unique token and stores an expiration timestamp.
- [ ] Invitation tokens can be validated without exposing unrelated organization data.
- [ ] Invitees (Account with role 'ORGANIZER') can accept or reject valid invitations.
- [ ] Accepting an invitation creates or activates the corresponding organization membership record.
- [ ] Rejected or expired invitations cannot be accepted again.
- [ ] Invitation status transitions are enforced and persisted correctly.
- [ ] Public API responses use record DTOs wrapped in `ApiResponse<T>`.
- [ ] Validation failures follow the standard `VALIDATION_FAILED` response shape.
- [ ] Integration tests cover invitation creation, token validation, accept/reject flows, expiration behavior, and membership creation on acceptance.

## Status
`planned`

## Outcome
TBD
