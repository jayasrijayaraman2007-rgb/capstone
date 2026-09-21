# TESTING (APPROVED — suite green 2026-09-21, 71 tests)

- Suite: `mvn verify` — 43 tests, 0 failures (repo, service, MockMvc controller, auth, authz per-role, validation, error/404/state-guard, logout, format check). All `@Transactional` (no dev-data pollution); `@DataJpaTest` pinned to MySQL (`Replace.NONE`, no H2).
- Lint/format: Spotless (`removeUnusedImports`, `trimTrailingWhitespace`, `endWithNewline`) bound to `verify`; full-formatter plugins excluded — palantir/google formatters crash on JDK 26 (internal javac APIs).
- Manual checklist verified live: login, logout (302→`?logout`, session killed), protected-route denial, visitor create/retrieve, approve/reject (+re-decision guard), gate-pass generate (idempotent), entry, exit, role restrictions, dashboard metrics, 403 page.
- Traceability: tests cover approved flows F-01…F-10 only.
- Legacy console prototype `AuthenticationModule.java` was deleted (superseded by Spring Security); no references remain in code.
