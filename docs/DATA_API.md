# DATA_API (APPROVED T-00 — 2026-09-19)

> Status: APPROVED T-00. Entities per §6 verbatim; constraints/state machine/search below are approved. Endpoint table remains PROPOSAL until T-02 contract review.

## 1. Approved entities (attribute names verbatim from §6)
- Users: user_id, name, username, password, role
- Visitors: visitor_id, name, phone, email, address, id_proof
- Hosts: host_id, name, department, phone, email
- Gate_Passes: pass_id, visitor_id, host_id, purpose, issue_date, status
- Entry_Exit: entry_exit_id, pass_id, entry_time, exit_time
- Visit_Requests: request_id, visitor_id, host_id, purpose, request_date, status

## 2. APPROVED constraints (T-00 2026-09-19)
- MySQL InnoDB; Spring Data JPA; `ddl-auto=validate` (migrations reviewed, never auto-update prod).
- PKs: auto-generated `*_id`; FKs: Visit_Requests.visitor_id→Visitors, Visit_Requests.host_id→Hosts; Gate_Passes.visitor_id→Visitors, Gate_Passes.host_id→Hosts; Entry_Exit.pass_id→Gate_Passes (one row per pass, entry set on Active, exit set on Completed).
- Uniques: Users.username unique; Hosts.email unique where present; Visitors.email optional.
- Status enums stored as VARCHAR: Visit_Requests.status ∈ {Pending, Approved, Rejected}; Gate_Passes.status ∈ {Pending, Approved, Rejected, Active, Completed}.
- Passwords: BCrypt-hashed only; never plaintext (replaces `AuthenticationModule.java:30` hardcoded credential).
- Approved DDL (applied to dev DB 2026-09-19, T-02):
  `CREATE TABLE users (user_id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100) NOT NULL, username VARCHAR(50) NOT NULL UNIQUE, password VARCHAR(100) NOT NULL, role VARCHAR(20) NOT NULL) ENGINE=InnoDB;`
- Approved ALTER (applied to dev DB, Admin-Accounts phase; reuses `users`, no new user table; existing rows default ACTIVE):
  `ALTER TABLE users ADD COLUMN employee_id VARCHAR(50) NULL UNIQUE, ADD COLUMN email VARCHAR(100) NULL UNIQUE, ADD COLUMN phone VARCHAR(20) NULL, ADD COLUMN department VARCHAR(100) NULL, ADD COLUMN designation VARCHAR(100) NULL, ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;`
- Approved DDL (applied to dev DB 2026-09-19, T-03; name/phone/id_proof required, email/address optional):
  `CREATE TABLE visitors (visitor_id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100) NOT NULL, phone VARCHAR(20) NOT NULL, email VARCHAR(100), address VARCHAR(255), id_proof VARCHAR(100) NOT NULL) ENGINE=InnoDB;`
- Approved DDL (applied to dev DB 2026-09-20, T-04; name/department required, phone/email optional; no delete — hosts are referenced by future requests/passes):
  `CREATE TABLE hosts (host_id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100) NOT NULL, department VARCHAR(100) NOT NULL, phone VARCHAR(20), email VARCHAR(100)) ENGINE=InnoDB;`
- Approved DDL (applied to dev DB 2026-09-20, T-05; status ∈ {PENDING, APPROVED, REJECTED}):
  `CREATE TABLE visit_requests (request_id BIGINT AUTO_INCREMENT PRIMARY KEY, visitor_id BIGINT NOT NULL, host_id BIGINT NOT NULL, purpose VARCHAR(255) NOT NULL, request_date DATETIME NOT NULL, status VARCHAR(20) NOT NULL, CONSTRAINT fk_vr_visitor FOREIGN KEY (visitor_id) REFERENCES visitors(visitor_id), CONSTRAINT fk_vr_host FOREIGN KEY (host_id) REFERENCES hosts(host_id)) ENGINE=InnoDB;`
- Host↔login linking (T-06 revision — replaces T-05 email-match interim): `hosts.user_id` nullable FK → `users.user_id`; a HOST login sees requests of the host whose `user_id` is their account. Admin links accounts on the host add/edit form.
  `ALTER TABLE hosts ADD COLUMN user_id BIGINT NULL, ADD CONSTRAINT fk_host_user FOREIGN KEY (user_id) REFERENCES users(user_id);`
- Approved DDL (applied to dev DB 2026-09-20, T-06; one pass per request via UNIQUE request_id; T-06 addition beyond §6 recorded here):
  `CREATE TABLE gate_passes (pass_id BIGINT AUTO_INCREMENT PRIMARY KEY, request_id BIGINT NOT NULL UNIQUE, visitor_id BIGINT NOT NULL, host_id BIGINT NOT NULL, purpose VARCHAR(255) NOT NULL, issue_date DATETIME NOT NULL, status VARCHAR(20) NOT NULL, CONSTRAINT fk_gp_request FOREIGN KEY (request_id) REFERENCES visit_requests(request_id), CONSTRAINT fk_gp_visitor FOREIGN KEY (visitor_id) REFERENCES visitors(visitor_id), CONSTRAINT fk_gp_host FOREIGN KEY (host_id) REFERENCES hosts(host_id)) ENGINE=InnoDB;`
- Approved DDL (applied to dev DB 2026-09-20, T-07; one row per pass; entry set on Active, exit on Completed):
  `CREATE TABLE entry_exit (entry_exit_id BIGINT AUTO_INCREMENT PRIMARY KEY, pass_id BIGINT NOT NULL UNIQUE, entry_time DATETIME, exit_time DATETIME, CONSTRAINT fk_ee_pass FOREIGN KEY (pass_id) REFERENCES gate_passes(pass_id)) ENGINE=InnoDB;`
- Deletion rule (T-07 fix): linked users (hosts.user_id) cannot be deleted — `UserService.deleteUser` throws `IllegalStateException` instead of leaking an FK error. No delete UI exists yet; the rule guards all future callers.

## 3. PROPOSED endpoint sketch (NOT approved — for planner review only)
Proposed only to unblock discussion; names/methods/payloads must be confirmed in a planner-approved revision before coding:
- `POST /api/auth/login`, `POST /api/auth/logout` (mechanism TBD)
- `GET/POST /api/visitors`, `GET/PUT /api/visitors/{id}`
- `GET/POST /api/hosts`, `GET /api/hosts/{id}`
- `GET/POST /api/visit-requests`, `POST /api/visit-requests/{id}/approve`, `POST /api/visit-requests/{id}/reject`
- `GET/POST /api/gate-passes`, `GET /api/gate-passes/{id}`, `POST /api/gate-passes/{id}/entry`, `POST /api/gate-passes/{id}/exit`
- All success/error envelopes, status codes, validation messages: TBD.

## 4. APPROVED decisions (T-00 2026-09-19)
1. Auth: Spring Security session + BCrypt; role enforcement server-side.
2. Constraints: see §2 above; ER follows §6 + FKs listed (diagrams 02/04 to be transcribed in T-01).
3. State machine: Visit_Requests Pending→Approved/Rejected (terminal); approved request→Gate_Pass(Approved)→entry→Active→exit→Completed; Rejected terminal; entry requires Approved, exit requires Active.
4. Search/filter: visitors by name/phone; passes/requests filter by status; dashboard counts = total visitors, pending requests, approved, inside-now (Active), completed; recent-activity list (latest passes/requests).
