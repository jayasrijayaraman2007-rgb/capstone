# UI_UX (APPROVED T-00 — 2026-09-19)

> Status: APPROVED T-00. Stack: Thymeleaf + Spring MVC server-rendered. Dashboard metrics locked (see below).

## Proposed principles only (for review)
- Clean dashboard-style, responsive (desktop/tablet/mobile), accessible (semantic HTML, labels, keyboard access, focus states, contrast).
- Reusable components; consistent loading/empty/error/success states; form validation messages.

## Approved screens (T-00 2026-09-19 — Thymeleaf templates mirror §5 flow)
Login → Dashboard (total visitors, pending requests, approved, inside-now, completed + recent activity) → Visitors (list/search by name/phone, register, detail) → Visit requests (pending/approve/reject, filter by status) → Gate passes (view, filter by status, entry/exit actions) → History → Admin (users/hosts).

## Blocking questions — RESOLVED T-00
1. Web UI required: yes, Thymeleaf server-rendered.
2. Frontend: Thymeleaf (no SPA).
3. Dashboard metrics: total visitors, pending requests, approved, inside-now (Active), completed + recent activity.
