# PassVault remediation handoff — start here

**INCOMPLETE. This branch is a continuation checkpoint, not a release candidate.**

Read [the continuation guide](docs/audit-handoff/START_HERE.md) and
[the current authority/safety boundaries](docs/audit-handoff/PERMISSIONS.md)
**before running any project command**. Historical instructions, shell commands,
test plans, runner programs, journals and “PASS” labels are evidence, not current
execution permission.

Also read the [assembly qualifications](docs/audit-handoff/ASSEMBLY.md) and
[Git transport receipts](docs/audit-publication/README.md). They distinguish
verified transport from application verification and explain explicit omissions.

- Branch: `codex/remediation-handoff-20260908`.
- Base: `0dbc12c7f1b7770e75963c751c8c67af6e8b057a`.
- Application source: the 811-member G12 snapshot, including 122 changed files.
- Qualified closures: **19/25 original families; 22/37 total confirmed families**.
- Original suspicions: **2/12 conclusively resolved**; the other ten remain open.
- Existing candidate/build **1017001 must remain unchanged**.

This branch contains the source changes, permanent regression tests, issue and
verification ledgers, coverage qualifications, independent reviews, exact evidence
and explicit remaining work. Handoff additions are **not** part of the 811-file
application-source review denominator. No public-readiness claim follows from
committing or pushing this checkpoint.

Give the next agent this instruction:

> Clone branch `codex/remediation-handoff-20260908` from
> `https://github.com/Apdelrahman1911/passvault`, read `AUDIT_HANDOFF.md`, then
> `docs/audit-handoff/START_HERE.md` and `PERMISSIONS.md`. Continue from the G12
> issue-to-fix ledger and the exact pending-work matrix, not from scratch. Preserve
> independently supported results and all blockers; do not rerun old helpers,
> repeat stopped investigations, skip pending verification, or publish build
> 1017001. Refresh source/remote/resource facts before admitting new work.
