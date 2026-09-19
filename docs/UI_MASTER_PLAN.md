# UI/UX Master Plan — Design System Overhaul (APPROVED by requester 2026-09-20)

> Goal: lift the functional-but-basic Thymeleaf UI to a professional dashboard look.
> Hard constraints (unchanged): all routes, role guards, flows, and metrics stay per
> SCREEN_SPECIFICATIONS.md / DATA_API.md. This plan changes presentation only —
> tokens, shared layout, component classes. No new screens, no new data.

## 1. Tokens (`docs/DESIGN_TOKENS.md` — replace placeholders)
- Primary indigo `#4338ca` (hover `#3730a3`); surface `#f4f5fb`; ink `#1a1a1a`; muted `#5b6472`; border `#d7dbe3`.
- Status pills: Pending amber `#92400e` on `#fef3c7`; Approved blue `#1d4ed8` on `#dbeafe`;
  Rejected red `#b91c1c` on `#fee2e2`; Active green `#15803d` on `#dcfce7`;
  Completed slate `#475569` on `#e2e8f0`.
- Type: system stack, base 16px, scale 0.875 / 1 / 1.25 / 1.5 / 2rem. Radius 10px cards,
  6px inputs/buttons. Spacing 4/8/16/24/32.

## 2. Shared shell (`templates/fragments.html` — Sovereign rail + masthead)
- `sidebar`: 280px navy rail — PassGuard brand, live-checkpoint pill, role-aware section
  nav (real routes only), cluster footer.
- `pagehead`: 64px white sticky masthead — breadcrumbs, gates-active pill, functional
  visitor search (Admin/Security), Fast Check-In button, role pill, avatar + logout.
- Every authenticated page: sidebar + `.shell-main` wrapper + pagehead; content in
  `<main class="container">` (`.wide` for dashboard/lists). Login stays standalone.

## 3. Components (`static/css/app.css`)
- `.cards`/`.card`/`.card-value`/`.card-label` for dashboard metrics.
- `.badge` + `.b-pending/.b-approved/.b-rejected/.b-active/.b-completed` for all status output.
- `.alert`/`.alert-error` for `?error` and form summaries; `.field-error` (keeps `#b00020`).
- `.btn`/`.btn-primary`/`.btn-danger`, `.table-scroll` tables, `.form` spacing — keep T-09 a11y bits
  (labels, focus-visible, 44px targets, scope attrs).

## 4. Page upgrades (content identical, classes only)
- Dashboard: metric cards; recent-activity lists with badges.
- Lists: badge on status columns; styled filter links; keep empty states.
- Forms/details: card wrapper; error alert box in addition to field errors.
- Login: centered card. 403/404/error: centered card.

## 5. Verification
- `mvn verify` green (content assertions keep their text).
- Live: login → dashboard cards render; one list, one form, 403 page; visual check via browser.
- No new routes, no logic changes — controllers untouched except where a model attr is needed (none planned).
