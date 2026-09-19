# SCREEN_SPECIFICATIONS (APPROVED T-00 — 2026-09-19)

> Status: APPROVED T-00. Routes below are Thymeleaf MVC routes; REST contract for form posts confirmed in T-02.

| Proposed route | Proposed screen | Approved source | Access (proposal) |
|---|---|---|---|
| `/login` | Login | implied by Users table §6.1 | public |
| `/dashboard` | Dashboard (total, pending, approved, inside-now, completed + recent activity) | §7 "view/monitor" + T-00 | authenticated |
| `/visitors` | Visitor list + search | §5 search/view | Admin, Security |
| `/visitors/new` | Register visitor | §5, F-01 | Security (+Admin) |
| `/visitors/:id` | Visitor detail + history | §5 history | role-scoped |
| `/requests` | Pending visit requests | §5 host confirmation | Host (own), Admin |
| `/requests/:id` | Request detail, approve/reject | §5, §7 | Host (own), Admin |
| `/passes` | Gate pass list | §5 | Admin, Security |
| `/passes/:id` | Gate pass detail, entry/exit actions | §5 entry/exit | Admin, Security |
| `/history` | Entry/exit history | §5 | Admin, Security |
| `/admin/users`, `/admin/hosts` | Manage users/hosts | §7 Admin | Admin only |
| `*` | Not found; `/unauthorized` for denied | — | — |

Rules (proposal): all but `/login` require auth; role checks server-side; invalid IDs → not-found state.
