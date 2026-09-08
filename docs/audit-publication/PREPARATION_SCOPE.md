# Scope of this Git publication

This is an **incomplete remediation handoff**, not a release, a fresh audit of
the whole source, or permission to run the archived validation programs.

## What the upload is intended to preserve

- The exact G12 application snapshot: 811 source members and all 122 changed-file
  after-images, permanent regression tests included. Git storage normalization
  and the two declared PowerShell checkout-EOL differences are separately bound
  by `SOURCE-GIT-INDEX.json` and `../audit-handoff/current/source-transport.json`.
- The frozen handoff package, `../audit-handoff/PACKAGE.json` (SHA256
  `bc4e7cc7d6ae83bedcec3b4947a14de2aac52706089af9e2abbc2564c5f91a81`):
  171 members excluding the manifest itself. Supplemental publication receipts
  live in this directory so they do not rewrite that package.
- Current issue/variant/status, suspicion, design-decision, verification and
  coverage ledgers; exact prior findings, patches, reviews and compact results.
  The evidence reader actually verified 21,378 indexed paths, 16,510 exact blobs
  and 11 packs. This is transport evidence, not 21,378 reviewed source files.
- Explicit next steps and limitations. The 226 omitted paths remain described
  in `../audit-handoff/omissions.json`; do not infer that every historical record
  was uploaded. The reference inventory is a candidate locator, not complete
  semantic reference resolution: 109 unresolved/external and 31 policy-withheld
  path candidates with unresolved digests remain visible.

## Publication screening is bounded

`DIRECT-PUBLICATION-SCREENING.json` records static signatures over the intended
files at its capture time. It is not universal secret clearance. Non-UTF8 assets
were not text-scanned; historical pack screening has its own receipts.

The five specific flags in four source files were independently classified as
deliberate synthetic test fixtures or runtime-input parser predicates. See
`reviews/clone-handoff-marker-platform-v1/REPORT.md` and its exact file hashes.
No source replacement was warranted for those exact contexts. No provider token
authentication, real key parsing, scanner/test execution or blanket path allowlist
was performed. Two web captures with embedded API-key-shaped data and all included
aliases were omitted; that is not a general statement about every possible secret.

## Verification and command boundaries

The seven local reader checks in `TRANSPORT-CHECKS.json` passed their respective
positive or expected-rejection outcomes. They executed **zero application test
cases**. Remote-clone evidence, when recorded, must identify its exact pushed
commit and complete index manifest, not merely the branch name.

New clone-helper source drafts and independent review are retained as inert text
under `transport-helper/` and `reviews/`. V1 had actual source-level isolation and
cleanup-accounting objections; V2 addressed them; V3 narrowed a documentation
claim and uses its own one-shot journal namespace. Source acceptance is not
proof that a clone command executed or that cleanup succeeded. Only actual run
receipts can establish those observations. Do not replay these host-specific
programs on another machine.

Two additional metadata-display diagnostics failed on list-versus-dictionary
assumptions; `RESUME-METADATA-DIAGNOSTICS.json` records both. They changed no source
and ran no application tests. Neither is hidden as a successful check.

No Gradle, Kotlin, Xcode or native build, application test or app launch was
started for this publication. Therefore a new Gradle-stop invocation is not
applicable, and no older CLOSED stop/recovery obligation is discharged. Only an
explicitly created disposable Git-clone/environment root may be removed; the
original checkout, remediation worktree, reports and shared caches are preserved.

Only `codex/remediation-handoff-20260908` is an authorized push destination.
The fifteen inspected workflow event blocks declare no automatic application
build/publishing trigger for a push to this branch; that does not certify external
integrations. No merge, main/testing/release movement, tag/version/build-number
change, production signing, deployment approval, Store upload or app publication
is intended. Existing candidate/build **1017001 remains separate and unchanged**.

Progress is still **22/37 qualified confirmed-family closures (59.5%)**; the
remaining fifteen are not converted into passes by committing this handoff.
