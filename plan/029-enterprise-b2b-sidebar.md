# Plan: Enterprise Collapsible Sidebar for B2B portal (Ticketmaster-style)

- Description: restructures the B2B Organizer Portal sidebar into an enterprise-grade hierarchical configuration based on the requested modular architecture. It replaces the current flat sidebar navigation with a premium collapsible accordion system divided into 5 main functional folders.
- Acceptance criteria:
  - 5 Folders (`TM1 Events`, `TM1 Sales`, `TM1 Entry`, `TM1 Reports`, `TM1 Marketing`) collapsible.
  - Sub-items nested under folders, mapping to query parameter tabs.
  - Premium animations and transitions for expanded/collapsed states.
  - Visual excellence with curated icons and colors.
- Status: done
- Outcome: Fully implemented the premium, enterprise-grade collapsible sidebar in the B2B portal. Added 5 custom collapsible folders containing the requested nested tools. Built interactive compact-mode flyout menus. Verified 100% compile-time, lint, and formatting compliance.

## Proposed Changes

### Web Layer (SvelteKit Frontend)

#### [MODIFY] [b2b/+layout.svelte](file:///c:/Users/hoang/Desktop/ticketpeak/web/src/routes/b2b/+layout.svelte)
- Refactor the B2B layout sidebar component.
- Implement folder toggle states using Svelte 5 `$state`.
- Map folder clicks to accordion slide toggle.
- Map item links to `/b2b/[route]?tab=[sub-item]&organizationId=...` for deep navigation.
- Polish style with transition classes, modern hover outlines, and high-fidelity colors.
