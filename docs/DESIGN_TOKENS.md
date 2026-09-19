# DESIGN_TOKENS (APPROVED 2026-09-20 — Sovereign Gate/Stitch adaptation)

> Supersedes the indigo theme. Source: Stitch export `sovereign_gate/DESIGN.md` + 4 screen
> mockups (Downloads/stitch_gatepass_enterprise_visitor_system). Only tokens/components
> backed by real backend routes are implemented; Stitch modules without backend
> (vault, verification log, watchlist, analytics, settings, QR/wallet flows) are omitted.

- Fonts: Plus Jakarta Sans (headings) + Inter (body/tables) via Google Fonts with
  system-ui fallback (offline-safe). Tabular numbers for times/IDs.
- Canvas `#F8FAFC`; card `#FFFFFF`; inset `#F1F5F9`; stroke `#E2E8F0`; ink `#0B1C30`;
  muted `#64748B`; focus ring `#2563EB`.
- Navy rail `#0F172A` (hover `#1E293B`); primary action `#2563EB` (hover `#1D4ED8`);
  danger `#DC2626` (hover `#B91C1C`).
- Status pills (dot + fill / text): ok `#16A34A`/`#DCFCE7`/`#15803D` (Approved, Active);
  warn `#D97706`/`#FEF3C7`/`#B45309` (Pending); bad `#DC2626`/`#FEE2E2`/`#B91C1C` (Rejected);
  neutral `#475569`/`#F1F5F9`/`#334155` (Completed). Map: PENDING→warn, APPROVED→ok,
  REJECTED→bad, ACTIVE→ok, COMPLETED→neutral.
- Radius: controls 8px, cards 12–16px, pills full. Shadows: slate ambient L1 cards.
- Layout: 280px navy rail + 64px white sticky header; content 12-col fluid, 24px gutters;
  8px grid rhythm; tables collapse to cards under 768px (scroll fallback kept).
- Sidebar shows only real destinations: Executive Overview (/dashboard), Gate Desk
  Operations (/passes), Host Approvals (/requests), Pre-Registration (/requests/new),
  Live Monitor (/history), Visitor Directory (/visitors), Hosts (/admin/hosts).
