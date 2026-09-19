# TESTING (DRAFT)

- Current: no tests exist. `AuthenticationModule.java` has no unit/integration tests.
- Minimum when backend exists: unit (entities/services/state machine), validation tests, auth tests, authorization tests (per-role), API tests per DATA_API contract, error-handling tests, `mvn test`, build.
- Traceability: test only approved flows F-01…F-10; do not invent flows to pad coverage.
- Manual checklist (once UI/API exist): login, logout, protected-route denial, visitor create/retrieve, approve/reject, gate-pass generate, entry, exit, role restrictions.
