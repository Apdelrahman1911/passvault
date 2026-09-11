# Database Detekt03 proposals — UNAPPLIED

**Source-only proposals; independent review and root adjudication pending.**
No Kotlin/Gradle/Detekt/test/project-helper execution, Git, CI, process/SDK probe
or application-source write was performed by this lane. C17's four existing
target files remain byte-identical and the proposed fifth source file is absent.
GUI03's source/instance freeze is not released by these proposals.

Boundary qualification reported to root: the initial AGENTS filename search was
overbroadened to W's parent and traversed sibling directory metadata, including
held checkouts and T. No sibling file bytes were opened, no Git/runtime/helper
was executed and no sibling modification or cleanup occurred. That search
pattern was stopped; all subsequent reads/writes were W-bound. This was not a
held-runtime inspection admission and must not be repeated or described as zero
sibling-directory inspection.

Read authority: TEAM_20_RESUME, the saved pause, AUDIT_HANDOFF, handoff
START_HERE/PERMISSIONS/ASSEMBLY, PUBLISHED_PAYLOAD and W/AGENTS. Focused instruction
snapshots: secure-platform-biometric-unlock (including platform-security),
secure-backup-and-blob-storage and database-migration-integrity. Their historical
execution examples were not run. `docs/SECURITY_MODEL.md` remains the security
contract; this lane changes neither a schema nor an encryption/backup format.

## Exact proposals and diagnostics

Apply nothing without root's explicit path/freeze release. Proposed order is
01, 02, 03; 03's repository before-image includes 02. Identity and patch hashes,
per-step before/after images and combined after-images are in
`SOURCE_IDENTITIES.json`. They are source-byte identities, not runtime evidence.

| Proposal | Retained diagnostics addressed |
| --- | --- |
| `01-pagination-declaration-and-fixture-format.patch.txt` | BackupPagination.kt:11 MatchingDeclarationName; VaultBackupUnicodePaginationTest.kt:297/331/362/368/389/411/419 MaxLineLength (eight diagnostics) |
| `02-biometric-admission-and-fixture-returns.patch.txt` | VaultRepositoryImpl.kt:294 CyclomaticComplexMethod; BiometricUnlockFreshnessIntegrationTest.kt:428 ReturnCount (two diagnostics) |
| `03-sole-vek-owner-size-exception.patch.txt` | VaultRepositoryImpl.kt:51 LargeClass (one intentional class-only exception proposal) |

These are the exact eleven entries in retained
`runs/linux-detekt03/reports/core-database-checkstyle.xml`, SHA256
`ada0438da1fb43ba790f58f735315a2fc0d7e2deb55790f7fa1b2e70c721231a`,
checked against that run's retained STATIC-REPORTS capture record. No new product
family or closure is inferred. Prospective lint satisfaction is **not executed**.

## 01: keep pagination callable identity; relocate only its comparator

Keep `BackupPagination.kt`, its imports, private identifier guard and both
`emitSingleKeyPages` / `emitCompositeKeyPages` declarations. Move the existing
`BackupDatabaseTextOrder` enum plus `compareScalars`, `String.scalarAt` and
`compareCodeUnits` unchanged into the matching `BackupDatabaseTextOrder.kt`.
The moved code block is byte-identical apart from final file-boundary blank-line
separation. The enum's package, name and visibility stay unchanged; the two
pagination callables retain the existing `BackupPaginationKt` JVM file facade.
Private comparator helper facade/accessor locations are compiler-generated and
move with the enum's source; no external callable or reference is renamed.

There is no changed comparison, cursor, key-validation, page limit, callback,
exception or cancellation logic. In particular:

- SQLite UTF-8 scalar and UTF-16 unsigned-byte order retain the exact code.
- Identifier checks still precede comparison/consumer delivery, including both
  composite components and the 512-UTF-16-unit scan bound.
- Consumer completion still precedes cursor advancement; failure/cancellation
  does not trigger an extra page fetch.
- Production `VaultBackupV2Service` PRAGMA selection, snapshot transaction,
  order propagation and DAO routes are untouched.

The seven fixture changes wrap constructor arguments, one existing lambda body,
assertion arguments and attachment/history call arguments. Extra trailing commas
are formatting only. Exact synthetic IDs, Unicode encoding vectors, ciphertext
assertions, relationship/history/content checks, reverse insertion order,
attachment paths, `getOrThrow` calls and all existing finally blocks remain.
No case or assertion is added, removed, weakened or moved across a cleanup scope.
`withFixture` still locks in NonCancellable, closes Room and finally deletes only
its own directory, without following symlinks; `postVisitDirectory` still throws
an incoming IOException before deletion. Secret/input/key wiping stays intact.

Compatibility risk: the additional source file must be included in the next
root-owned source inventory/build. Do not retrofit it into consumed C17 manifests
or reinterpret retained old line/path references. This is preferable to renaming
the whole file, which would unnecessarily rename the pagination JVM facade.

## 02: extract only biometric admission; preserve secret ownership

`isCurrentBiometricAttempt` is a private suspend predicate in the same repository.
Its sole caller remains inside the unchanged transition -> operation -> session
mutex nesting, immediately after `currentCoroutineContext().ensureActive()`.
The predicate checks repository identity before touching `lockIntents`; then it
verifies the attempt's existing generation, catching **only**
`UnlockPreemptedException`. It never snapshots a replacement generation, owns a
VEK, writes state, returns a key or changes mutex authority.

The old identity `if` plus preemption `catch` become one caller `if`; this is
expected to reduce the caller's reported cyclomatic complexity from 15 to 14.
That expected metric is unexecuted, not a static-analysis pass. The small private
predicate is a cohesive non-owning guard rather than a new state/secret owner.

| Scenario | Preserved behavior |
| --- | --- |
| Foreign repository | Fail with VaultSessionLockedException before lock-tracker, key-size or metadata access; no state/key mutation |
| Stale generation or pending intent | Same failure, before key-size rejection; cannot invalidate enrollment or relock/wipe a newer session |
| Cancellation before/while verifying | Initial ensureActive remains; cancellation from mutex suspension is not caught by the predicate |
| Fresh malformed key | Existing size rejection remains after freshness admission |
| Already-open session | Existing commit uses the original attempt generation; preemption maps to VaultSessionLockedException without changing the session |
| Fresh locked session | Existing metadata validation, candidate copy, authenticated verification, last-access write and generation-checked publication remain in the original try/finally |
| Late lock during final write | Existing preempted catch/result distinction remains, including the service's INTERNAL_ERROR mapping for this late-publication path |
| Verification/authentication/provider/cancellation failure | Existing typed rejection, generic failure and cancellation catches are untouched; copied candidate still wipes in the same finally |

`DefaultBiometricUnlockService` still receives and wipes the platform-transferred
array; the repository still owns only its candidate/session copies. Password
fallback, failed-attempt counters, tracked leases, lock cancellation, wipe-failure
handling and provider-error/non-invalidation behavior are unchanged.

The test key store now computes its candidate only inside `vaultId ==
enrolledVaultId`, then has one shared NotEnabled return. Its other return is the
existing try/finally transfer. This preserves these counterexamples:

- Mismatched vault: do not evaluate/copy/consume nextCandidate or storedKey, and
  do not consume/pause the retrieval gate.
- Same vault without candidate or stored key: NotEnabled, with gate untouched.
- Injected candidate: transfer that same instance, not a new copy. Stored key
  fallback: copy once, retaining the original enrollment buffer.
- Gate cancellation before transfer: fill the candidate with zero in finally.
- Synthetic cancel-on-return: still records lastReturnedKey and transfer before
  returning, so the service's cancellation check/wipe remains the tested owner.

No test-specific blanket suppression or extra permanent test is proposed.

## 03: narrowly scoped design exception, not a smaller-class claim

LargeClass reports a real size metric. It does not itself prove multiple security
owners are preferable. This class intentionally contains the sole live VEK,
serialized transitions and revocable-lease cleanup. Splitting that ownership
solely to meet the cosmetic threshold would broaden this lint-only change and
its security review obligations.

03 therefore separately proposes adding exactly `LargeClass` to this class's
existing `TooManyFunctions` annotation, with an explicit state-owner comment.
No file-wide, module-wide, baseline, dependency or Detekt-configuration exclusion
is proposed. The size diagnostic is an intentional exception **only if the
independent reviewer and root accept it**; it must otherwise remain unresolved.
Do not silently replace a rejection with a secret-boundary refactor or broader
suppression. This proposal does not claim that arbitrary future class growth is
reviewed or that the class cannot contain defects.

## Existing evidence and smallest useful future validation

Current ISSUE_LEDGER/VERIFICATION_LEDGER retain qualified PVA-033, PVA-035 and
PVA-038 closures from `linux-isolated-batch03` at C4, not a fresh C17 result:
12 freshness, 13 pagination and six real Room Unicode cases passed within the
larger accepted selection. The existing nine provider-routing and three
cold-provider cases remain their distinct prior evidence. Their qualifications,
physical/native/device gaps, earlier FAIL/HOLD records and no-retry boundaries
are not changed here. No old runner, consumed workload or native refusal should
be replayed merely for reassurance.

After independent acceptance, GUI03 freeze reconciliation and fresh root-owned
source/admission binding:

1. Include `:core:database:detekt` in the focused changed-module static successor,
   retaining exact report triplets and failures. Do not rerun all 22 analyzers.
2. Following the independent reviewer's scope challenge, recommend only the
   existing 12-case
   `com.passvault.core.database.repository.BiometricUnlockFreshnessIntegrationTest`
   in a fresh admitted `:core:database:desktopTest` selection for the changed
   admission predicate/fake transfer path. Normal compilation covers the new
   comparator source location. Bind the new source generation and exact selector;
   this is not authorization to replay an old runner or consumed workload.
3. Reuse the existing 13 `BackupPaginationTest` and six
   `VaultBackupUnicodePaginationTest` results with exact unchanged-code/layout
   delta qualification: comparator/helper block and pager tail unchanged, test
   tokens unchanged apart from layout/trailing commas. Do not rerun those 19
   cases, nine provider-routing cases, three cold-provider cases, or all105/166
   merely for reassurance. A substantive later change would need a new scope
   decision. Root may independently narrow or batch this recommendation.
4. Preserve exact JUnit identities/results/cleanup if admitted. Compilation or
   synthetic key-store tests would not prove physical biometrics, exhaustive
   Unicode, migrations, Android/iOS provider parity or loaded-native/package
   provenance.

Root alone owns admission, wrapper/stop/cleanup/publication and central records.
PVU007 STOP, PVU011 NO RETRY, PVA029's failure, G7/G8 CLOSED and every held-runtime
obligation remain in force. No Telegram update was duplicated by this subagent.
