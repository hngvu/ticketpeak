# FEATURE_INTAKE.md — Work Classification

Before implementing any feature or change, classify it here.
Classification controls how much planning and review is required.

---

## Classification Table

| Class | Definition | Examples | Required before coding |
|-------|------------|---------|------------------------|
| **Tiny** | Single-file, no schema change, no API contract change, easily reversible | Fix a typo in a label, adjust a CSS color, fix a missing null check | A one-line description in the plan file |
| **Normal** | Multi-file, bounded scope, no Flyway migration, no breaking API change | New UI component, new service method, new endpoint on existing resource | Full plan file with acceptance criteria |
| **High-Risk** | Schema migration, breaking API change, security/payment/IAM touch, cross-module refactor | New Flyway migration, role model change, payment flow change, bulk data operation | Full plan + architecture note + explicit developer sign-off before starting |

---

## How to Classify

Ask these questions:

1. Does it require a Flyway migration? → **High-Risk**
2. Does it touch security, payment, or IAM? → **High-Risk**
3. Does it change a public API contract (add/remove/rename fields)? → **High-Risk minimum**
4. Does it touch more than 3 modules? → **High-Risk**
5. Is it a single localized fix with no schema change? → **Tiny**
6. Everything else → **Normal**

---

## TicketPeak-Specific High-Risk Zones

These areas always require explicit developer sign-off before touching:

- `api/src/main/resources/db/migration/` — Flyway migrations
- `api/.../security/` — JWT, auth filters, role resolution
- `api/.../payment/` — payment gateway, payout logic
- `api/.../iam/` — RBAC/ABAC permission model
- `api/.../order/` — reservation, seat hold, checkout
- `api/.../ticket/` — TOTP QR generation, check-in validation
- Any bulk `.saveAll()` operation across thousands of seats
