# TASKS (T-00 DONE 2026-09-19)

> Implementation status: only `AuthenticationModule.java` console prototype. No Spring Boot/DB/API/UI yet. T-00 decisions recorded below.

## Task list (T-00 DONE — decisions: Thymeleaf; Security session+BCrypt; InnoDB/JPA constraints; Approve→Active→Complete; dashboard+search set)
- [x] T-00 Approve docs + decide auth/frontend/DB/state-machine/search. DONE 2026-09-19.
- [x] T-01 Maven + Spring Boot skeleton + MySQL config (needs T-00). DONE 2026-09-19: `mvn package` + `mvn test` green; boots against live MySQL 8.0.46 (`Started VisitorGatePassApplication`, Tomcat 8080, `/login` 200, `/api/health` 401 under default security — expected until T-02).
- [ ] T-01 Maven + Spring Boot skeleton + MySQL config (needs T-00).
- [x] T-02 Users + auth + role-based access (needs T-00 contract). DONE 2026-09-19: User/Role entity, BCrypt, session form-login, URL role rules, env-seeded admin, 6/6 tests green, live login verified (`/dashboard` greets admin, anon redirected).
- [x] T-03 Visitors CRUD + search (F-01, F-09). DONE 2026-09-19: Visitor entity/repo/service/controller, list+search (name/phone), register/edit with validation, detail, 404 page, role guard (Admin/Security only), 13/13 tests green, live flow verified (create→302→detail, search finds, missing→404).
- [x] T-04 Hosts CRUD (Admin). DONE 2026-09-20: Host entity/repo/service/controller under `/admin/hosts`, list+search, add/edit with validation, detail, 404, Admin-only guard, 21/21 tests green, live flow verified (create→302→detail, missing→404).
- [x] T-05 Visit requests + host approve/reject (F-02…F-04). DONE 2026-09-20: VisitRequest entity/FKs, create (Security/Admin), host-scoped list/detail, approve/reject with re-decision guard, status filter, 27/27 tests green, live flow verified (admin create→302, host sees own→approve→APPROVED, re-approve→?error=state).
- [ ] T-06 Gate pass generation + status lifecycle (F-05, F-06).
- [ ] T-07 Entry/exit recording + history (F-07, F-08, F-09).
- [ ] T-08 Dashboard + reports (only planner-approved metrics).
- [ ] T-09 UI states, validation, responsive, accessibility pass.
- [ ] T-10 Test/build/lint hardening per TESTING.md + DoD.

Next approved task: T-06 (Gate pass generation + status lifecycle). T-07+ remain blocked until prior task passes DoD.
