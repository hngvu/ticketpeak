---
version: alpha
name: Ticketpeak-Vercel-Enterprise-Design-System
description: An elegant and premium design language for Ticketpeak — a modern event ticketing platform. It is built on a high-contrast palette using OKLCH color management, tailored to establish trust, energy, and readability for both buyers and organizers.

colors:
  primary: 'oklch(60% 0.16 250)'
  on-primary: 'oklch(100% 0 0)'
  ink: 'oklch(20% 0 0)'
  body: 'oklch(45% 0 0)'
  mute: 'oklch(70% 0 0)'
  hairline: 'oklch(92% 0 0)'
  hairline-strong: 'oklch(85% 0 0)'
  canvas: 'oklch(100% 0 0)'
  canvas-soft: 'oklch(99% 0 0)'
  canvas-soft-2: 'oklch(96% 0 0)'
  success: 'oklch(65% 0.14 160)'
  error: 'oklch(60% 0.18 25)'
  warning: 'oklch(75% 0.12 80)'

typography:
  display-xl:
    fontFamily: Geist Sans, system-ui, sans-serif
    fontSize: 48px
    fontWeight: 600
    lineHeight: 48px
    letterSpacing: -2.4px
  display-lg:
    fontFamily: Geist Sans, system-ui, sans-serif
    fontSize: 32px
    fontWeight: 600
    lineHeight: 40px
    letterSpacing: -1.28px
  display-md:
    fontFamily: Geist Sans, system-ui, sans-serif
    fontSize: 24px
    fontWeight: 600
    lineHeight: 32px
    letterSpacing: -0.96px
  display-sm:
    fontFamily: Geist Sans, system-ui, sans-serif
    fontSize: 20px
    fontWeight: 600
    lineHeight: 28px
    letterSpacing: -0.6px
  body-lg:
    fontFamily: Geist Sans, system-ui, sans-serif
    fontSize: 18px
    fontWeight: 400
    lineHeight: 28px
    letterSpacing: 0px
  body-md:
    fontFamily: Geist Sans, system-ui, sans-serif
    fontSize: 16px
    fontWeight: 400
    lineHeight: 24px
  body-md-strong:
    fontFamily: Geist Sans, system-ui, sans-serif
    fontSize: 16px
    fontWeight: 500
    lineHeight: 24px
  body-sm:
    fontFamily: Geist Sans, system-ui, sans-serif
    fontSize: 14px
    fontWeight: 400
    lineHeight: 20px
    letterSpacing: -0.28px
  body-sm-strong:
    fontFamily: Geist Sans, system-ui, sans-serif
    fontSize: 14px
    fontWeight: 500
    lineHeight: 20px
    letterSpacing: -0.28px
  caption:
    fontFamily: Geist Sans, system-ui, sans-serif
    fontSize: 12px
    fontWeight: 400
    lineHeight: 16px
  caption-mono:
    fontFamily: Geist Mono, ui-monospace, monospace
    fontSize: 12px
    fontWeight: 400
    lineHeight: 16px
  code:
    fontFamily: Geist Mono, ui-monospace, monospace
    fontSize: 13px
    fontWeight: 400
    lineHeight: 20px
  button-md:
    fontFamily: Geist Sans, system-ui, sans-serif
    fontSize: 14px
    fontWeight: 500
    lineHeight: 20px
  button-lg:
    fontFamily: Geist Sans, system-ui, sans-serif
    fontSize: 16px
    fontWeight: 500
    lineHeight: 24px

rounded:
  none: 0px
  xs: 4px
  sm: 6px
  md: 8px
  lg: 12px
  xl: 16px
  pill-sm: 64px
  pill: 100px
  full: 9999px

spacing:
  xxs: 4px
  xs: 8px
  sm: 12px
  md: 16px
  lg: 24px
  xl: 32px
  2xl: 40px
  3xl: 48px
  4xl: 64px
  5xl: 96px
  6xl: 128px
  section: 192px

components:
  nav-bar:
    backgroundColor: '{colors.canvas}'
    textColor: '{colors.ink}'
    typography: '{typography.body-sm}'
    height: 64px
    padding: '{spacing.sm} {spacing.lg}'
  nav-link:
    textColor: '{colors.body}'
    typography: '{typography.body-sm}'
    rounded: '{rounded.full}'
    padding: '{spacing.xs} {spacing.sm}'
  nav-cta-signup:
    backgroundColor: '{colors.primary}'
    textColor: '{colors.on-primary}'
    typography: '{typography.body-sm-strong}'
    rounded: '{rounded.sm}'
    padding: '0px {spacing.xs}'
    height: 28px
  nav-cta-login:
    backgroundColor: '{colors.canvas}'
    textColor: '{colors.ink}'
    typography: '{typography.body-sm-strong}'
    rounded: '{rounded.sm}'
    padding: '0px {spacing.xs}'
    height: 28px
  nav-cta-ask-ai:
    backgroundColor: '{colors.canvas}'
    textColor: '{colors.ink}'
    borderColor: '{colors.hairline}'
    typography: '{typography.body-sm-strong}'
    rounded: '{rounded.sm}'
    padding: '0px {spacing.xs}'
    height: 28px
  button-primary:
    backgroundColor: '{colors.primary}'
    textColor: '{colors.on-primary}'
    typography: '{typography.button-lg}'
    rounded: '{rounded.pill}'
    padding: '0px {spacing.sm}'
  button-secondary:
    backgroundColor: '{colors.canvas}'
    textColor: '{colors.ink}'
    borderColor: '{colors.hairline}'
    typography: '{typography.button-lg}'
    rounded: '{rounded.pill}'
    padding: '0px {spacing.sm}'
  button-primary-sm:
    backgroundColor: '{colors.primary}'
    textColor: '{colors.on-primary}'
    typography: '{typography.button-md}'
    rounded: '{rounded.pill}'
    padding: '0px {spacing.xs}'
  button-secondary-sm:
    backgroundColor: '{colors.canvas}'
    textColor: '{colors.ink}'
    borderColor: '{colors.hairline}'
    typography: '{typography.button-md}'
    rounded: '{rounded.pill}'
    padding: '0px {spacing.xs}'
  tab-ghost:
    backgroundColor: '{colors.canvas}'
    textColor: '{colors.ink}'
    typography: '{typography.body-sm}'
    rounded: '{rounded.pill-sm}'
    padding: '0px {spacing.md}'
  icon-button-circular:
    backgroundColor: '{colors.canvas}'
    textColor: '{colors.ink}'
    borderColor: '{colors.hairline}'
    rounded: '{rounded.full}'
  card-marketing:
    backgroundColor: '{colors.canvas}'
    textColor: '{colors.ink}'
    borderColor: '{colors.hairline}'
    typography: '{typography.body-md}'
    rounded: '{rounded.md}'
    padding: '{spacing.lg}'
  card-marketing-large:
    backgroundColor: '{colors.canvas}'
    textColor: '{colors.ink}'
    borderColor: '{colors.hairline}'
    typography: '{typography.body-md}'
    rounded: '{rounded.lg}'
    padding: '{spacing.xl}'
  card-soft:
    backgroundColor: '{colors.canvas-soft}'
    textColor: '{colors.ink}'
    typography: '{typography.body-md}'
    rounded: '{rounded.md}'
    padding: '{spacing.lg}'
  template-card:
    backgroundColor: '{colors.canvas}'
    textColor: '{colors.ink}'
    typography: '{typography.body-md}'
    rounded: '{rounded.md}'
    padding: '{spacing.md}'
  code-editor-mockup:
    backgroundColor: '{colors.ink}'
    textColor: '{colors.on-primary}'
    typography: '{typography.code}'
    rounded: '{rounded.md}'
    padding: '{spacing.lg}'
  form-input:
    backgroundColor: '{colors.canvas}'
    textColor: '{colors.ink}'
    borderColor: '{colors.hairline}'
    typography: '{typography.body-sm}'
    rounded: '{rounded.sm}'
    padding: '0px {spacing.sm}'
    height: 40px
  form-input-sm:
    backgroundColor: '{colors.canvas}'
    textColor: '{colors.ink}'
    borderColor: '{colors.hairline}'
    typography: '{typography.body-sm}'
    rounded: '{rounded.sm}'
    padding: '0px {spacing.sm}'
    height: 32px
  form-input-lg:
    backgroundColor: '{colors.canvas}'
    textColor: '{colors.ink}'
    borderColor: '{colors.hairline}'
    typography: '{typography.body-md}'
    rounded: '{rounded.sm}'
    padding: '0px {spacing.sm}'
    height: 48px
  badge-secondary:
    backgroundColor: '{colors.canvas-soft}'
    textColor: '{colors.body}'
    typography: '{typography.caption}'
    rounded: '{rounded.full}'
    padding: '0px {spacing.xs}'
  pricing-card:
    backgroundColor: '{colors.canvas}'
    textColor: '{colors.ink}'
    borderColor: '{colors.hairline}'
    typography: '{typography.body-md}'
    rounded: '{rounded.lg}'
    padding: '{spacing.xl}'
  pricing-card-featured:
    backgroundColor: '{colors.primary}'
    textColor: '{colors.on-primary}'
    typography: '{typography.body-md}'
    rounded: '{rounded.lg}'
    padding: '{spacing.xl}'
  logo-strip:
    backgroundColor: '{colors.canvas}'
    textColor: '{colors.body}'
    typography: '{typography.body-sm}'
    padding: '{spacing.lg} {spacing.xl}'
  hero-band:
    backgroundColor: '{colors.canvas}'
    textColor: '{colors.ink}'
    typography: '{typography.display-xl}'
    padding: '{spacing.4xl} {spacing.lg}'
  feature-mesh-band:
    backgroundColor: '{colors.canvas}'
    textColor: '{colors.ink}'
    typography: '{typography.display-lg}'
    padding: '{spacing.5xl} {spacing.lg}'
  showcase-band-light:
    backgroundColor: '{colors.canvas-soft}'
    textColor: '{colors.ink}'
    typography: '{typography.display-lg}'
    padding: '{spacing.5xl} {spacing.lg}'
  showcase-band-dark:
    backgroundColor: '{colors.ink}'
    textColor: '{colors.on-primary}'
    typography: '{typography.display-lg}'
    padding: '{spacing.5xl} {spacing.lg}'
  footer:
    backgroundColor: '{colors.canvas}'
    textColor: '{colors.body}'
    typography: '{typography.body-sm}'
    padding: '{spacing.4xl} {spacing.lg}'
  link-inline:
    textColor: '{colors.primary}'
    typography: '{typography.body-md}'
  banner-marketing:
    backgroundColor: '{colors.canvas-soft}'
    textColor: '{colors.body}'
    typography: '{typography.body-sm}'
    rounded: '{rounded.full}'
    padding: '{spacing.xs} {spacing.sm}'

  # ─── Examples (illustrative) — auto-derived ───
  ex-pricing-tier:
    description: 'Default tier card. Mirrors pricing-card chrome on canvas-soft surface with a hairline border.'
    backgroundColor: '{colors.canvas-soft}'
    textColor: '{colors.ink}'
    borderColor: '{colors.hairline}'
    rounded: '{rounded.lg}'
    padding: '{spacing.xl}'
  ex-pricing-tier-featured:
    description: 'Featured tier — polarity-flipped to brand primary with white text and white CTA.'
    backgroundColor: '{colors.primary}'
    textColor: '{colors.on-primary}'
    rounded: '{rounded.lg}'
    padding: '{spacing.xl}'
  ex-product-selector:
    description: "What's Included summary card — repurposed for the platform's features tiers."
    backgroundColor: '{colors.canvas-soft}'
    rounded: '{rounded.md}'
    padding: '{spacing.lg}'
  ex-cart-drawer:
    description: 'Subscription summary — line items per add-on.'
    backgroundColor: '{colors.canvas}'
    rounded: '{rounded.md}'
    padding: '{spacing.lg}'
    item-divider: '{colors.hairline}'
  ex-app-shell-row:
    description: 'Sidebar nav row. Active state uses brand primary as a left-edge indicator bar.'
    backgroundColor: '{colors.canvas}'
    activeIndicator: '{colors.primary}'
    rounded: '{rounded.sm}'
    padding: '{spacing.xs} {spacing.sm}'
  ex-data-table-cell:
    description: "Mirrors the brand's table chrome. Header uses caption-mono uppercase mono; body uses body-sm."
    headerBackground: '{colors.canvas-soft}'
    headerTypography: '{typography.caption-mono}'
    bodyTypography: '{typography.body-sm}'
    cellPadding: '{spacing.xs} {spacing.sm}'
    rowBorder: '{colors.hairline}'
  ex-auth-form-card:
    description: 'Sign-in / sign-up card. Mirrors card-marketing-large chrome with form-input primitives inside.'
    backgroundColor: '{colors.canvas-soft}'
    rounded: '{rounded.lg}'
    padding: '{spacing.xl}'
  ex-modal-card:
    description: 'Modal dialog surface — same chrome as card-marketing-large with Level 5 modal shadow.'
    backgroundColor: '{colors.canvas}'
    rounded: '{rounded.lg}'
    padding: '{spacing.xl}'
  ex-empty-state-card:
    description: 'Empty-state illustration frame. Generous padding on canvas-soft.'
    backgroundColor: '{colors.canvas-soft}'
    rounded: '{rounded.lg}'
    padding: '{spacing.3xl}'
    captionTypography: '{typography.body-md}'
  ex-toast:
    description: 'Toast notification surface — flat-cornered card-marketing chrome with Level 4 shadow.'
    backgroundColor: '{colors.canvas}'
    rounded: '{rounded.md}'
    padding: '{spacing.sm} {spacing.md}'
    typography: '{typography.body-sm}'
---

## Overview

Ticketpeak is a premium, high-energy event ticketing platform. Its design system establishes instant trust, professionalism, and modern appeal using a structured, high-contrast palette: near-white `{colors.canvas-soft}` body background, deep `{colors.ink}` typography, professional `{colors.body}` details, and a vibrant brand-defining `{colors.primary}` acting as the focal point for every key conversion.

Colors are deployed with strict hierarchy: light-mode viewports sit on a clean off-white `{colors.canvas-soft}` backdrop, separating content containers using razor-sharp `{colors.hairline}` borders. Dark-mode overlays or high-impact immersive zones utilize a complete polarity flip to `{colors.ink}`, offering stunning depth, visual relief, and premium brand aesthetics.

Typography drives the platform's editorial voice. Geist Sans carries displays, body, buttons, and structural navigation, optimized with subtle negative letter-spacing for headlines. Geist Mono is introduced for technical descriptors, ticket metadata, barcodes, and dates, underscoring the automated smart-ticketing identity of the platform.

Surfaces use a four-step depth ladder: `{colors.canvas}` (pure white for buyer-facing event cards), `{colors.canvas-soft}` (default main body background), `{colors.canvas-soft-2}` (slightly deeper inset blocks and headers), and `{colors.primary}` or `{colors.ink}` (for immersive dark actions and dashboard panels).

**Key Characteristics:**

- **Action-Driven Primary CTA:** The `{colors.primary}` serves as the signature action trigger, reserved exclusively for conversion targets (e.g., "Buy Tickets", "Checkout", "Publish Event").
- **Strict Semantic Hierarchy:** Soft background colors and high-contrast texts for Success, Error, and Warning ensure clear buyer confirmation and dashboard feedback loops.
- **Calm, High-End Elevation:** No heavy, single drop-shadows. Instead, elevation uses stacked multi-stop shadows layered with very fine 1px inline borders to keep cards looking crisp and structurally anchored.
- **The Punctuation & Letter-Spacing Signature:** Bold section displays use sentence-case titles accompanied by negative tracking for an elegant, architectural look.

## Colors

### Brand & Accent

- **Primary Brand / Action** (`{colors.primary}` — `oklch(60% 0.16 250)`): A high-energy, high-trust blue reserved for primary CTAs, main action paths, and core interactive states.
- **Deep Ink Base** (`{colors.ink}` — `oklch(20% 0 0)`): Our primary dark anchor. Used for high-contrast headers, dark body typography, and polarity-flipped immersive bands.
- **Slate UI Body** (`{colors.body}` — `oklch(45% 0 0)`): Used as secondary text, structural borders, and support elements to maintain visual depth.
- **Mute Slate** (`{colors.mute}` — `oklch(70% 0 0)`): A lighter gray used for helper texts, disabled states, and placeholder elements.

### Surface

- **Canvas** (`{colors.canvas}` — `oklch(100% 0 0)`): Pure white card, dropdown, and modal backgrounds to raise components cleanly off the page base.
- **Canvas Soft** (`{colors.canvas-soft}` — `oklch(99% 0 0)`): The default page background tone. Establishes a soft, premium canvas for all sections.
- **Canvas Soft 2** (`{colors.canvas-soft-2}` — `oklch(96% 0 0)`): Slightly deeper gray used for section headers, table headers, and form-input fills.
- **Hairline** (`{colors.hairline}` — `oklch(92% 0 0)`): Faint 1px separators — ticket listings, card dividing borders, and layout lines.
- **Hairline Strong** (`{colors.hairline-strong}` — `oklch(85% 0 0)`): A medium gray divider used to define boundaries on light cards.

### Text

- **Ink** (`{colors.ink}` — `oklch(20% 0 0)`): Main headlines and high-importance body blocks in light mode.
- **Body** (`{colors.body}` — `oklch(45% 0 0)`): Main text content, ticket listings, and sub-headers.
- **Mute** (`{colors.mute}` — `oklch(70% 0 0)`): Placeholders, metadata, and footnote disclaimers.
- **On Primary** (`{colors.on-primary}` — `oklch(100% 0 0)`): Text color overlaying brand-primary or dark ink blocks.

### Semantic

- **Success** (`{colors.success}` — `oklch(65% 0.14 160)`): Trustworthy green used for checkout confirmation, valid QR scans, and successful payouts.
- **Error** (`{colors.error}` — `oklch(60% 0.18 25)`): Red used for invalid QR codes, sold-out notices, and form validation blocks.
- **Warning** (`{colors.warning}` — `oklch(75% 0.12 80)`): Cautionary amber used for pending reservations, expiring checkouts, and low-ticket count warnings.

## Typography

### Font Family

The typography system uses two primary typefaces:

1. **Geist Sans**: The main display, body, button, and link carrier. Configured up to weight 600. Large hero sizes use tight negative tracking (`-2.4px` for display-xl) to look exceptionally bold, modern, and cohesive.
2. **Geist Mono**: Used exclusively for ticket metadata, date indicators, QR codes, pricing structures, and dashboard tables. Denotes reliability and high-speed technology.

### Hierarchy

| Token                         | Size | Weight | Line Height | Letter Spacing | Use                                                                  |
| ----------------------------- | ---- | ------ | ----------- | -------------- | -------------------------------------------------------------------- |
| `{typography.display-xl}`     | 48px | 600    | 48px        | -2.4px         | Hero headline ("Discover the Next Big Event.").                      |
| `{typography.display-lg}`     | 32px | 600    | 40px        | -1.28px        | Section headings ("Featured Venues", "Create Your Ticket Manifest"). |
| `{typography.display-md}`     | 24px | 600    | 32px        | -0.96px        | Event card headings, ticket price tiers.                             |
| `{typography.display-sm}`     | 20px | 600    | 28px        | -0.6px         | Interactive modals, drawer section names.                            |
| `{typography.body-lg}`        | 18px | 400    | 28px        | 0              | Description copy, organizer testimonials.                            |
| `{typography.body-md}`        | 16px | 400    | 24px        | 0              | Default paragraph, event descriptions.                               |
| `{typography.body-md-strong}` | 16px | 500    | 24px        | 0              | Emphasized dates, cart summaries.                                    |
| `{typography.body-sm}`        | 14px | 400    | 20px        | -0.28px        | Ticket details, seat numbers, nav items.                             |
| `{typography.body-sm-strong}` | 14px | 500    | 20px        | -0.28px        | "Add to Cart" inline texts, active navigation.                       |
| `{typography.caption}`        | 12px | 400    | 16px        | 0              | Copyright blocks, auxiliary form helpers.                            |
| `{typography.caption-mono}`   | 12px | 400    | 16px        | 0              | Ticket categories, date codes, platform labels.                      |
| `{typography.code}`           | 13px | 400    | 20px        | 0              | QR check-in logs, developer API snippets.                            |
| `{typography.button-md}`      | 14px | 500    | 20px        | 0              | Secondary button actions, small nav links.                           |
| `{typography.button-lg}`      | 16px | 500    | 24px        | 0              | Primary landing CTA ("Get Tickets").                                 |

## Layout

### Spacing System

- **Base scale**: 4px base multiplier.
- **Tokens**: `{spacing.xxs}` 4px · `{spacing.xs}` 8px · `{spacing.sm}` 12px · `{spacing.md}` 16px · `{spacing.lg}` 24px · `{spacing.xl}` 32px · `{spacing.2xl}` 40px · `{spacing.3xl}` 48px · `{spacing.4xl}` 64px · `{spacing.5xl}` 96px · `{spacing.6xl}` 128px · `{spacing.section}` 192px.
- **Containers**: Main site content centers perfectly with a max-width of 1400px, utilizing `{spacing.lg}` horizontal gutters on desktop and `{spacing.md}` on mobile.
- **Rhythm**: Generous spacing between marketing sections (`{spacing.4xl}` to `{spacing.5xl}`) keeps layouts breathing, while interior card elements are tightly coupled (`{spacing.xs}` to `{spacing.sm}`) to keep context clear.

### Grid Layouts

- **Event Catalog Grid**: 3-up at desktop, 2-up at tablet, 1-up on mobile devices.
- **Ticket Type Selector**: Vertical stack of cards with 12px grid gaps, showing seat selection details.
- **Organizer Dashboard Panels**: Fluid grid columns using robust flex grids for real-time sales charts.

## Elevation & Depth

| Level                    | Treatment                                                             | Use                                            |
| ------------------------ | --------------------------------------------------------------------- | ---------------------------------------------- |
| Level 0 — Flat           | Pure flat fill, no shadow.                                            | Layout sections, main backdrop panels.         |
| Level 1 — Inset Hairline | 1px solid `{colors.hairline}`.                                        | Default borders on unselected event cards.     |
| Level 2 — Subtle Drop    | Stacked shadow: `0px 1px 2px oklch(0% 0 0 / 0.05)` + hairline border. | Selected ticket options, input fields.         |
| Level 3 — Soft Stack     | Stacked shadow: `0px 4px 6px oklch(0% 0 0 / 0.06)` + hairline border. | Interactive event cards, dashboard widgets.    |
| Level 4 — Float Stack    | Stacked shadow: `0px 10px 15px oklch(0% 0 0 / 0.08)` + hairline.      | Checkout drawer, active ticket preview cards.  |
| Level 5 — Modal          | Shadow: `0px 20px 25px oklch(0% 0 0 / 0.12)` + 1px hairline border.   | QR code tickets, purchase confirmation modals. |

## Shapes

- **Base Corner Radius**:
  - `{rounded.sm}` (6px): Input fields, tight checkboxes, tab items.
  - `{rounded.md}` (8px): Default event cards, checkout panels, pricing tier selectors.
  - `{rounded.lg}` (12px): Modals, high-impact ticket layout cards.
  - `{rounded.pill}` (100px): Marketing scale buttons, pill indicators.
- **Photography Geometry**: Event banner thumbnails use a locked 16:9 aspect ratio, displaying crisp, rounded `{rounded.md}` borders to fit within the design grid.

## Components

### Buttons

- **`button-primary`**: Brand primary background (`{colors.primary}`), white text (`{colors.on-primary}`), large size (`{typography.button-lg}`), rounded pill (`{rounded.pill}`). The ultimate checkout and purchase trigger.
- **`button-secondary`**: Pure white background (`{colors.canvas}`), charcoal text (`{colors.ink}`), rounded pill (`{rounded.pill}`), bordered with `{colors.hairline}`. For secondary navigations.

### Event Cards

- **`card-marketing`**: Base card for standard event listings. Pure white (`{colors.canvas}`) surface, subtle `{colors.hairline}` border, and Level 3 soft shadow. Displays event title in `{typography.display-md}` and date in `{typography.caption-mono}`.

### Checkout & Forms

- **`form-input`**: Fills with `{colors.canvas}`, bordered in `{colors.hairline}`, text in `{typography.body-sm}`, height 40px, rounded `{rounded.sm}`. Expands border to `{colors.primary}` on active focus.

### Navigation

- **`nav-bar`**: Sticked header (64px). White base (`{colors.canvas}`) with a bottom `{colors.hairline}` divider. Organizes main logo, buyer search routes, and partner dashboard access seamlessly.

## Modals

### Modal Styling & Layout

- **Sharp Corners:** All modals must use sharp, square corners (`rounded-none`). Avoid using border-radius for modal containers to maintain the structured, architectural B2B aesthetic.
- **Multi-step vs Full Forms:**
  - **For Create (New Data):** Modals should not be overwhelmingly large. If a creation form requires many inputs, break it down into multiple steps (e.g., Step 1: Basic Info, Step 2: Pricing) with internal navigation to reduce cognitive load.
  - **For Update (Editing Data):** Users editing data usually want to jump straight to a specific field without being forced to click "Next" through a wizard. Therefore, Update forms should display all fields in a single, well-sectioned view.
  - **Avoid Scrolling Modals:** If an Update form (or any form) contains so many fields that it would require vertical scrolling, **do not use a Modal**. Instead, promote the form to a **Side Drawer (Slide-over)** or a **Dedicated Page**. Modals must be compact enough to fit entirely within the viewport without internal scrollbars.

### Modal Action Buttons

- **Split alignment:** Modal actions with two buttons (e.g., Cancel + Confirm) must align one button to the left and the other to the right using `justify-content: space-between`. This improves reachability and visual balance over grouping both on the same side.
- The left button is typically the dismissive/secondary action (e.g., Cancel), styled as ghost. The right button is the primary/confirm action, styled as primary.
- Single-action modals (e.g., a lone "Close" or "Done") remain right-aligned as standard.

## Do's and Don'ts

### Do:

- **Use primary blue selectively:** Reserve `{colors.primary}` (`oklch(60% 0.16 250)`) for active buttons, transactional links, and primary QR status feedback.
- **Keep margins structured:** Standardize layouts around the 4px base spacing to avoid visual misalignment.
- **Integrate the Monospace face:** Set ticket IDs, ticket dates, currencies, and scan counters strictly in `{typography.caption-mono}` or `{typography.code}`.
- **Leverage polarity flips:** Use deep `{colors.ink}` sections to call attention to exclusive events, checkout phases, or organizer headers.

### Don't:

- **Don't use saturated primary red or green:** Keep error and success blocks bound to the soft semantic tokens to protect readability.
- **Don't apply heavy single-layer shadows:** Avoid standard browser default shadows; stick to layered multi-stop parameters.
- **Don't uppercase headings:** Keep text sentence-cased with negative tracking to sustain an architectural, modern design aesthetic.
- **Don't mix button shapes:** Keep action pills round (`{rounded.pill}`) and interface control components square (`{rounded.sm}`).

---

## Admin Panel (Ops) Conventions

### Table Layout

- **No header row:** Tables in the admin panel omit `<thead>` rows entirely. Column context is conveyed through inline labels, data format, and visual hierarchy rather than a dedicated header bar.
- **Status as inline badge:** Status is never displayed in a dedicated "Status" column. Instead, a status badge is placed inline next to the entity name (e.g., venue name, user name). The badge only renders when the status differs from the default active state — e.g., only show an "INACTIVE" badge; active entities have no badge. Badge styling uses the same semantic color tokens defined in `Colors > Semantic` above.

### Page Header & Navigation

- **Page title as header:** The page title (h1/h2) is the primary header, not the breadcrumb. Breadcrumbs sit inside the main content area as a secondary navigation aid, not as the page heading.
- **Back button only when necessary:** A "Back" button should only be added if there is no equivalent browser back behavior or breadcrumb that covers the same navigation. If a breadcrumb already links back to the parent page, no separate back button is needed.
