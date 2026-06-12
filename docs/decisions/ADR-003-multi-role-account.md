# ADR-003 — Multi-Role Account Model

**Date:** 2026-06  
**Status:** Accepted  
**Decider:** Hoàng Vũ  
**Plan:** 034-multi-role-account.md

---

## Context

Account model ban đầu dùng scalar `role` column (one role per account). Khi cần một account vừa là `ORGANIZER` vừa là `VENUE_MANAGER`, scalar model không đủ.

## Decision

Chuyển sang `account_role` join table (many-to-many). `Account` entity expose `Set<Role> roles`.

## Key Code Pattern

```java
// Correct (Plan 034+)
principal.roles().contains(Role.ADMIN)

// Wrong (old pattern — do not use)
principal.role() != Role.ADMIN
```

JWT encodes all roles as comma-separated string in `role` claim.

## Consequences

- Account có thể hold multiple roles đồng thời
- Tất cả `@PreAuthorize` expressions dùng `hasRole()` — vẫn hoạt động vì Spring Security check `GrantedAuthority` list
- Mọi direct role check trong code phải dùng `.contains()` pattern

## Reversal Condition

Không đảo ngược — đã migrate production data.
