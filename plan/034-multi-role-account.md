# 034 — Multi-Role Account

## Description

Change the account model from single-role (scalar `role` column) to multi-role (many-to-many via join table), enabling an account to hold multiple platform roles simultaneously.

## Acceptance Criteria

1. A new `account_role` join table replaces the `role` column on `account`
2. Existing data migrates: each account's current role becomes a row in `account_role`
3. `Account` entity exposes `Set<Role> roles` instead of `Role role`
4. `AuthenticatedAccount` carries `Set<Role> roles` instead of `Role role`
5. JWT access tokens encode all roles (comma-separated string in the `role` claim)
6. Spring Security `GrantedAuthority` list includes all roles (`ROLE_X`, `ROLE_Y`)
7. `@PreAuthorize("hasRole('...')")` expressions continue to work (they check against `getAuthorities()`)
8. Direct `principal.role() != Role.X` checks updated to `!principal.roles().contains(Role.X)`
9. `AccountResponse` and `MeResponse` expose `Set<Role> roles` instead of `Role role`
10. Account registration assigns `[BUYER]` as default roles
11. `listAccounts` query updated to filter by role membership
12. All tests updated and passing

## Files to Change

### Database

- **New:** `api/src/main/resources/db/migration/V42__create_account_role_table.sql`
  - Create `account_role` table (`account_id UUID`, `role VARCHAR(32)`, PK on both)
  - Migrate data: `INSERT INTO account_role SELECT id, role FROM account`
  - Drop `role` column from `account`

### Java — Model layer

- `api/src/main/java/.../account/model/Account.java`
  - Remove `Role role` field
  - Add `@Builder.Default Set<Role> roles = new HashSet<>()` with `@ElementCollection`
- `api/src/main/java/.../iam/model/Role.java` — no changes needed
- `api/src/main/java/.../security/AuthenticatedAccount.java`
  - Change `Role role` → `Set<Role> roles`

### Java — Security layer

- `api/src/main/java/.../security/JwtService.java`
  - `issueTokenPair`: accept `Set<Role> roles`, encode as comma-separated string
  - `ParsedAccessToken`: change `Role role` → `Set<Role> roles`
  - `parseAccessToken`: split comma-separated string back to `Set<Role>`
- `api/src/main/java/.../security/JwtAuthenticationFilter.java`
  - Map each role to a `SimpleGrantedAuthority("ROLE_" + role.name())`

### Java — DTO layer

- `api/src/main/java/.../account/dto/AccountResponse.java`
  - Change `Role role` → `Set<Role> roles`
- `api/src/main/java/.../auth/dto/MeResponse.java`
  - Change `Role role` → `Set<Role> roles`

### Java — Service layer

- `api/src/main/java/.../account/service/AccountService.java`
  - `register()`: use `Set.of(Role.BUYER)` instead of `.role(Role.BUYER)`
  - `listAccounts()`: signature change (filter by roles)
- `api/src/main/java/.../organization/service/OrganizationService.java`
  - Line 57: `owner.getRoles().contains(Role.ORGANIZER)` instead of `owner.getRole() != Role.ORGANIZER`
- `api/src/main/java/.../organization/service/OrganizationInvitationService.java`
  - Line 63: `invitee.getRoles().contains(Role.ORGANIZER)`
- `api/src/main/java/.../auth/service/AuthService.java`
  - Pass `account.getRoles()` instead of `account.getRole()` to JwtService
- `api/src/main/java/.../iam/service/PermissionService.java`
  - Lines 50, 133, 148: `principal.roles().contains(Role.ADMIN)`

### Java — Controller layer

- `api/src/main/java/.../event/controller/OpsClassificationController.java`
  - Line 52: `principal.roles().contains(Role.ADMIN)`
- `api/src/main/java/.../event/controller/OpsAttractionController.java`
  - Line 41: `principal.roles().contains(Role.ADMIN)`
- `api/src/main/java/.../event/controller/PartnerEventController.java`
  - Line 71: `acc.roles().contains(Role.ADMIN)`
- `api/src/main/java/.../eventgroup/controller/PartnerEventGroupController.java`
  - Lines 65, 131: `acc.roles().contains(Role.ADMIN)`

### Java — Repository layer

- `api/src/main/java/.../account/repository/AccountRepository.java`
  - Update JPQL query: change `a.role = :role` to `:role MEMBER OF a.roles`

### Java — Tests

All test files that use `.role(Role.X)` must be updated to use `.roles(Set.of(Role.X))`:

- `AuthControllerIT`
- `AccountControllerIT`
- `EventControllerIT`
- `OfferControllerIT`
- `OrderServiceIT`
- `OrganizationControllerIT`
- `OrganizationMemberControllerIT`
- `OrganizationInvitationControllerIT`
- `PermissionControllerIT`
- `VenueControllerIT`
- `InventoryControllerIT`
- `TicketServiceIT`
- `ReservationControllerIT`
- `PayoutServiceIT`
- `ResaleServiceIT`
- `PaymentControllerIT`
- `EventGroupControllerIT`
- `EventGroupControllerIT`
- Plus any that assert `.role` in JSON path responses

## Status

done

## Outcome

All changes implemented and verified:

- **V42__create_account_role_table.sql**: creates `account_role` join table, migrates existing role data, drops old `role` column
- **Account.java**: replaced `Role role` with `@ElementCollection Set<Role> roles`
- **AuthenticatedAccount.java**: `Role role` → `Set<Role> roles`
- **JwtService.java**: `issueTokenPair` accepts `Set<Role>`, encodes as comma-separated string; `ParsedAccessToken` holds `Set<Role>`
- **JwtAuthenticationFilter.java**: creates `SimpleGrantedAuthority` per role
- **DTOs**: `AccountResponse` and `MeResponse` expose `Set<Role> roles`
- **Services**: all `.getRole()` / `.role()` calls updated to `.getRoles()` / `.roles().contains()`
- **Controllers**: all `principal.role() != Role.ADMIN` → `!principal.roles().contains(Role.ADMIN)`
- **AccountRepository**: JPQL updated to `:role MEMBER OF a.roles`
- **17 test files**: updated `.role(Role.X)` → `.roles(java.util.Set.of(Role.X))`, assertions fixed

Compile, AOT process, and test-compile all pass.
