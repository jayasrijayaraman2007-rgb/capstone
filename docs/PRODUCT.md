# PRODUCT — Visitor Entry & Gate Pass Management System (DRAFT for review)

> Status: DRAFT. Source: `Problemstatement.md` only. No code or UI spec approved yet. Do not implement from this file until approved.

## 1. Title / Domain
- Title: Visitor Entry & Gate Pass Management System
- Domain: Visitor Management / Security Management / Access Control
- Source: Problemstatement.md §1–§2

## 2. Problem
Manual registers are slow, incomplete, inaccurate, hard to search; hard to verify visitors and track entry/exit. Source: §4.

## 3. Solution (approved scope, §5)
Digitize: visitor registration + profile management; name, contact, ID proof, address; purpose; host selection; host approve/reject; unique gate pass for approved visitors; entry time; exit time; pass status (Pending, Approved, Rejected, Active, Completed); search/view records; entry/exit history; authorized management of visitor info.

## 4. Users & Permissions (approved, §7)
| Role | Permissions (verbatim) |
|------|------------------------|
| Admin | Manage users, visitors, hosts, gate passes; view all records and reports |
| Security Officer | Register visitors, verify details, generate gate passes, record entry/exit, update pass status |
| Host / Employee | View visitor requests, approve or reject visits, view visitors assigned to them |

## 5. Success criteria (verbatim, §8)
- Register visitor in 1–2 min; gate pass without paperwork; accurate entry/exit times; fast search; complete history; authorized-only access; reduced manual-register dependency; structured records.

## 6. Out of scope (verbatim, §9)
No facial recognition, biometrics, camera integration, SMS/WhatsApp, payments, mobile app, smart-gate/RFID hardware, AI verification, multi-branch enterprise deployment.

## 7. Track (approved, §10)
Java + Spring Boot, MySQL, REST API, VS Code, Git & GitHub, Maven.

## 8. Open questions (BLOCKING — do not guess)
1. Frontend technology not specified (web UI vs. console? Thymeleaf/React/none?).
2. Auth mechanism not specified (session/Basic/JWT? password hashing?).
3. No approved UI, API contract, or DB constraints beyond attribute lists.
