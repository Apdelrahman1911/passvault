---
name: production-readiness-audit
description: Orchestrate an evidence-backed, applicability-aware production readiness review across source, security, data integrity, localization, builds, artifacts, signing, workflows, stores, and manual device gates. Use before a production candidate is approved, after broad security or storage changes, when an earlier review is invalidated, or when a final READY or NOT READY verdict must be tied to an exact tree without reading private user data.
---

# Production Readiness Audit

Produce a reproducible verdict for one frozen source tree. Delegate domain checks to focused skills and preserve their raw evidence.

Load applicable focused skills rather than recreating them: `$release-provenance-and-promotion`, `$native-release-signing-and-packaging`, `$mobile-store-release-operations`, `$mobile-app-identities-and-services`, `$secure-platform-biometric-unlock`, `$secure-backup-and-blob-storage`, `$database-migration-integrity`, `$compose-ui-performance-audit`, `$localization-rtl-accessibility`, and `$navigation-verification`.

## Audit rules

1. Freeze and record the exact commit and Git tree before final verification.
2. Mark every gate `PASS`, `FAIL`, `BLOCKED`, or `NOT_APPLICABLE` with a reason and evidence reference.
3. Never turn an unexecuted or unavailable check into a pass.
4. Inspect code, configuration, schemas, generated artifacts, and synthetic fixtures; do not inspect real user secrets or private vault data.
5. Reopen affected sibling areas after a change; an old “100% reviewed” statement is not durable evidence.
6. Separate repository readiness from credentials, store review, physical-device validation, and publication authorization.

## 1. Define scope and applicability

Inventory targets, distribution channels, native bridges, databases, sensitive storage, locales, external services, and release workflows. Build a gate ledger using [`references/evidence-schema.md`](references/evidence-schema.md).

For each platform or feature, determine applicability from source and build configuration. `NOT_APPLICABLE` requires concrete evidence, such as an absent target or feature flag—not convenience.

## 2. Invoke focused audits

Load only skills relevant to the repository, normally in this order:

1. data/storage migration, backup/blob, biometric, and secure-navigation checks;
2. application identity/service and localization checks;
3. UI/navigation/performance verification;
4. candidate provenance, native signing/packaging, store operations;
5. final source, artifact, workflow, legal, and cleanliness gates.

Do not duplicate a child skill's detailed logic. Reference its evidence and carry unresolved failures into the final verdict.

## 3. Freeze the candidate

Before the final run:

- stop implementation changes;
- record `git status`, full commit, Git tree, dependency lock/verification state, and toolchain versions;
- prove generated schemas/resources are current;
- ensure later commands cannot silently edit tracked files;
- bind every built artifact and report to the frozen tree.

If verification changes source or generated outputs, invalidate the freeze, review the diff, and start the final run again.

## 4. Execute repository gates

Use the project's documented toolchain and explicit variants. Cover where applicable:

- formatting, static analysis, unit/integration/security/stress tests, and dependency verification;
- every release variant and architecture, including alternate distribution flavors;
- lint, shrinker/R8, manifest, DEX namespace, ABI/native-library, package-content, and legal-content inspection;
- iOS release/archive validation and Desktop package/runtime inspection;
- workflow/action pinning plus shell, PowerShell, Ruby, and release-validator checks;
- database migrations from every supported production version and fresh install;
- backup/blob corruption and large-data boundaries;
- localization completeness, placeholders, accessibility, and RTL checks;
- `git diff --check`, generated-output drift, and final clean-worktree proof.

Use [`scripts/validate_evidence_ledger.py`](scripts/validate_evidence_ledger.py) to reject incomplete or contradictory gate ledgers.

## 5. Perform an independent final review

Review the complete frozen diff line by line, then inspect sibling call sites and failure paths around every modified subsystem. Look for:

- fail-open security/error handling;
- stale migrations, format-version mismatches, orphan cleanup, and rollback gaps;
- secret/log leakage and unsafe temporary files;
- platform/configuration branches not exercised by the main host;
- workflow paths that build or publish different bytes;
- tests that assert mocks rather than the production boundary.

An “independent” review means a fresh pass with the implementation context reset or a separate reviewer; it does not require another agent when one is unavailable.

## 6. Issue the verdict

Return `READY` only when every applicable repository-executable gate passes and all remaining items are explicitly manual credential/store/physical-device gates that do not conceal a code failure.

Return `NOT READY` when any required gate fails, is blocked without accepted external ownership, artifacts are not bound to the frozen tree, or the worktree cannot be proven clean.

Report:

- exact commit/tree and artifact hashes;
- every issue and migration fixed;
- command-by-command pass/fail counts and evidence paths;
- remaining manual/device/store/credential actions and owners;
- genuine architectural limitations and mitigations;
- the minimum authorization needed for the next protected action.

## Forbidden shortcuts

- Calling compilation alone a full test.
- Treating a debug/simulator artifact as release proof.
- Omitting a platform because its CI runner is inconvenient.
- Reading private user data to validate behavior.
- Publishing, signing, uploading, or merging merely because validation succeeded.
- Reporting a percentage without a gate ledger that defines the denominator.
