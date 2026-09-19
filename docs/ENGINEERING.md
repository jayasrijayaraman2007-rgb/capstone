# ENGINEERING (DRAFT)

- Toolchain (verified 2026-09-19): JDK 26, Maven 3.9.16 at `C:\Users\jayas\.tools\apache-maven-3.9.16` (on user PATH), Spring Boot 3.5.4, `java.version=21`.
- Local DB (dev only, user-local, no admin service): MySQL 8.0.46 zip at `C:\Users\jayas\.tools\mysql-8.0.46-winx64`, datadir `C:\Users\jayas\.tools\mysql-data`, bound to 127.0.0.1:3306. Auto-starts on Windows logon via `Startup\start-mysql-visitorgate.cmd`; manual start: `mysqld.exe --datadir=... --port=3306 --bind-address=127.0.0.1`. DB `visitorgate` + least-privilege `gateapp` user; `root@localhost` has a generated password. Creds in user env vars `DB_URL`/`DB_USER`/`DB_PASSWORD`/`DB_ROOT_PASSWORD` only.
- Style: small classes/methods, clear names, no dead code, no `console.log`/debug leftovers, no magic values.
- Security: BCrypt (or Spring Security-approved hasher) once auth decided; validate all input; never commit `.env`/secrets; never hardcode credentials (note: current `AuthenticationModule.java` hardcodes `admin/admin123` + plaintext HashMap — must be replaced, not carried forward).
- Config: DB credentials via env only.
- Git: feature branches + lowercase Conventional Commits (`feat(auth): ...`, `fix(...)`, `test(...)`, `docs(...)`, `refactor(...)`). No vague messages.
