# DEFINITION_OF_DONE (DRAFT)

A task is DONE only when:
1. Requirement traceable to Problemstatement.md / approved docs is implemented.
2. Acceptance criteria in TASKS.md pass.
3. Tests relevant to the change pass (`mvn test` once Maven exists; today: `javac` compile check where applicable).
4. Lint/format pass (tool TBD — none configured yet).
5. Build passes.
6. No secrets committed; no hardcoded credentials.
7. No undocumented architecture/DB/API changes.
8. Role permissions respected; loading/empty/error states handled where UI exists.
