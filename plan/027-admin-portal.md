# 027 — Platform Admin Portal (Ops Dashboard) — Web

## Description

Implement the Platform Admin portal at `/ops/dashboard` (`/goal hoàn thiện cái flow cho cả admin, organizer`) to provide internal operations team and platform admins with global administrative controls:
1. **Overview KPI stats**: Display real-time aggregated metrics (Total Users, Total Events, Platform Revenue, Active Orders).
2. **Event Moderation**: Integrate with `/api/ops/events` to view, approve (publish), open sales, postpone, and cancel any event globally.
3. **Organization Governance**: Integrate with `/api/ops/organizations` to monitor organizations, manage profiles, and update status.
4. **Audit Logs & Settings**: Provide mock system configuration settings and an interactive platform audit log.

---

## Files to Create/Modify

| File | Change |
|---|---|
| `web/src/routes/ops/dashboard/+page.server.ts` | **[NEW]** SvelteKit page server loader and actions calling Spring Boot `/api/ops` endpoints. |
| `web/src/routes/ops/dashboard/+page.svelte` | **[NEW]** Gorgeous dark-obsidian admin portal UI with stats, tabs, moderation dialog sheets, settings, and mock logs. |

---

## Implementation Details

### 1. `+page.server.ts`
- Load `access_token` from cookies. Redirect to `/ops/login` if missing or invalid.
- Concurrently fetch:
  - `GET /api/ops/events?size=100` (contains global events details).
  - `GET /api/ops/organizations?size=100` (contains global organizations list).
- Expose SvelteKit form actions:
  - `publishEvent`, `startSales`, `cancelEvent`, `postponeEvent` targeting `/api/ops/events/{id}/...`.
  - `updateOrganizationStatus` (mocks or forwards update fields including status updates).

### 2. `+page.svelte`
- Built upon a premium grid design matching the dark-obsidian `#1A1F26` theme.
- **KPI Metrics Cards**:
  - Interactive micro-animations (scale on hover, gentle glow borders).
  - Metrics: Total Events, Platform Revenue, Active Organizations, Registered Users.
- **Unified Navigation Tabs**:
  - **Events Tab**: A list table showing event name, venue, start date, and status. Features quick action buttons to Publish, Open Sales, Postpone, or Cancel.
  - **Organizations Tab**: A table listing active org names, owners, websites, and statuses (Active/Suspended/Closed). Includes a toggle button to "Suspend / Activate" the organization.
  - **Settings Tab**: Configuration panel for anti-scalping price caps (e.g. 150%), platform commission percentage (e.g. 5%), and tickets transfer limit.
  - **Audit Logs Tab**: Simulated timeline detailing administrator and system background actions.

---

## Acceptance Criteria

- [ ] Successful admin login at `/ops/login` correctly redirects to `/ops/dashboard` instead of showing a 404.
- [ ] `/ops/dashboard` layout implements a premium dark-obsidian dashboard.
- [ ] Events are loaded correctly using `/api/ops/events`.
- [ ] Organizations are loaded correctly using `/api/ops/organizations`.
- [ ] Admins can trigger event lifecycle states (Publish, Open Sales, Cancel, Postpone) directly from the tab.
- [ ] Admins can suspend or activate organizations with real-time UI reactions.
- [ ] Layout is fully responsive, clean, and complies with linter and type checks.

---

## Status

`in-progress`
