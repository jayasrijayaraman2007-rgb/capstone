# ARCHITECTURE (APPROVED T-00 — 2026-09-19)

> Status: APPROVED per T-00 decisions. Frontend: Thymeleaf + Spring MVC. Auth: Spring Security session + BCrypt.

## Approved (Problemstatement.md §10)
- Language: Java
- Backend framework: Spring Boot
- Database: MySQL
- API style: REST API
- Build: Maven
- VCS: Git & GitHub
- IDE: VS Code

## Current reality (inner repo `capstone`)
- Single-file Java console app: `AuthenticationModule.java` (HashMap users, Scanner menu, plaintext passwords).
- No `pom.xml`, no Spring Boot, no MySQL, no REST controllers.
- Diagrams present as PNGs (01–06) but content not transcribed into docs yet.

## APPROVED T-00 (2026-09-19 — do not change without planner approval)
- Standard Maven layout: `src/main/java/com/visitorgate/...`, `src/main/resources/application.properties`, `src/test/...`
- Layers: controller → service → repository (Spring Data JPA) → MySQL (InnoDB); entities map 1:1 to §6 tables.
- Auth: Spring Security + BCrypt + server session; role checks server-side (never trust frontend role).
- Frontend: Thymeleaf + Spring MVC server-rendered (no separate SPA build).

## Constraints
- Never commit `.env` / secrets; no hardcoded passwords/tokens.
- Frontend must not be treated as the security boundary.
