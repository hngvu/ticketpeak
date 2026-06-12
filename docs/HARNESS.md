# HARNESS.md — TicketPeak Agent Collaboration Model

## Purpose

This file describes how humans and coding agents collaborate on TicketPeak.
Agents read this before touching any code. Humans update this when the model changes.

---

## The Two Roles

| Role | Who | Responsibility |
|------|-----|----------------|
| **Human** | Developer (Hoàng) | Product decisions, approvals, architecture calls, commit sign-off |
| **Agent** | OpenCode / Claude Code | Feature intake, implementation, test runs, plan updates |

Agents never commit without explicit developer approval.
Agents never skip writing a plan before implementing.

---

## Before Any Implementation

1. Check `plan/` — does a plan file exist for this work?
2. If no → create `plan/NNN-feature-name.md` using `docs/templates/PLAN_TEMPLATE.md`
3. Present plan to developer. Wait for explicit approval ("go ahead", "approved", "ok")
4. Only after approval: set plan status to `in-progress` and begin coding

---

## After Implementation

1. Run relevant checks (see AGENTS.md for commands)
2. Update plan status to `done`, fill in Outcome
3. Present final diff to developer for explicit approval before any commit

---

## Work Classification

Before implementing, agents classify work using `docs/FEATURE_INTAKE.md`.
Classification determines how much planning is required before coding starts.

---

## What Agents Inherit Between Sessions

All durable decisions live in `docs/decisions/`.
Agents read this directory before starting any significant change.
Do not re-litigate closed decisions without a new product or technical reason.

---

## Boundaries Agents Must Not Cross

- Never edit existing Flyway migration files (`V{n}__*.sql`)
- Never commit code without developer approval
- Never touch pricing or payment logic without explicit human sign-off
- Never change the RBAC/IAM model without reading `docs/ARCHITECTURE.md` first
- Never skip the plan step, even for "quick" fixes
