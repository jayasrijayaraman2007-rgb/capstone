# AGENTS.md (DRAFT)

Implementation agent rules for this repo (inner `capstone` is source of truth):
1. Source-of-truth order: Problemstatement.md → docs/PRODUCT.md → FEATURES → UI_UX → SCREEN_SPECIFICATIONS → DESIGN_TOKENS → ARCHITECTURE → DATA_API → ENGINEERING → TESTING → TASKS → DEFINITION_OF_DONE → code → README.
2. Work one TASKS.md task at a time; smallest correct change; verify acceptance criteria.
3. Never invent requirements, endpoints, schemas, UI behavior, or business rules. If docs and code disagree: STOP and report.
4. If anything is missing/conflicting/undefined: report AMBIGUITY FOUND (what, which docs, decision needed, what is blocked) and wait.
5. Conventional Commits (lowercase); feature branches; no secrets in repo; no hardcoded credentials.
6. Verify by execution (compile/tests/build) — never claim a pass without running it.
