# FEATURES (DRAFT — derived strictly from Problemstatement.md §5, §7)

> Status: DRAFT for review. Only features explicitly listed in §5 are approved. Anything else is out of scope.

## Approved features (F-01 … F-10)
- F-01 Visitor registration (name, phone, email, address, id_proof) — §5, §6.2
- F-02 Host selection + purpose of visit per request/pass — §5, §6.4, §6.6
- F-03 Visit request creation with status — §6.6
- F-04 Host approve / reject visit requests — §5, §3, §7
- F-05 Gate pass generation (unique pass) for approved visitors only — §5, §8
- F-06 Gate pass status lifecycle: Pending, Approved, Rejected, Active, Completed — §5
- F-07 Entry time recording — §5
- F-08 Exit time recording — §5
- F-09 Search + view visitor records; view history — §5, §8
- F-10 Role-based management: Admin manages users/visitors/hosts/passes; Security registers/verifies/issues/records; Host views/decides own requests — §3, §7

## Explicitly NOT in scope
See PRODUCT.md §6 (verbatim §9 list).

## Notes / gaps
- No notification method specified (in-app only; SMS/WhatsApp explicitly out of scope).
- No report format specified beyond "view all visitor records and reports" (§7) — report shape is TBD.
- Rejection reason not specified — TBD whether required.
