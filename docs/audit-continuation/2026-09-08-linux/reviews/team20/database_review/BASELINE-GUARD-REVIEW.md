# Database independent baseline guard review

Reviewer: `/root/database_review` — 2026-09-11. **Source-only, before exact
proposal review; no source application or execution admission.**

Authority read: TEAM_20_RESUME, saved pause, AUDIT_HANDOFF, handoff START_HERE,
PERMISSIONS and ASSEMBLY, publication PUBLISHED_PAYLOAD, applicable AGENTS,
current issue/verification ledger rows, SECURITY_MODEL, and focused biometric,
backup/blob and migration skill instructions/references. Their example commands
were not executed. C17, application/tests/helpers, T and all permanent fences
remain intact. Root alone reconciles GUI03, scheduling and central records.

**Reader-scope deviation, reported to root:** the first AGENTS locator used
`find .. -name AGENTS.md` from `/root/projects/PassVault`. That unintentionally
enumerated sibling directory/filename metadata, including T and held checkout
AGENTS paths. No sibling file content was opened, executed, changed or cleaned.
All subsequent reads/writes were W-bound. This does not support a blanket claim
of zero T/held-runtime metadata access; it did not authorize any further access.

## Exact baseline

`BASELINE-SOURCE-DIAGNOSTICS.json` binds the four diagnostic-bearing files plus
ten related source/config files to the retained C17 source manifest, commit
`0563e31adc9a66aefc3e74b99b9a24d17bdcdd49`, tree
`d1bd6ca5b18d08ff3ff15896d9a78af9016be799`. All fourteen raw hash/size tuples
match; this is not a fresh Git/ref check. The retained database XML/SARIF/HTML
digests match both original and saved STATIC-REPORTS capture descriptors.

The **eleven static errors**, not tests or defect families, agree in log,
checkstyle and SARIF:

| Source (module-relative suffix) | Original location | Diagnostic |
|---|---:|---|
| `backup/BackupPagination.kt` (commonMain) | 11:21 | MatchingDeclarationName |
| `repository/VaultRepositoryImpl.kt` (commonMain) | 294:26 | CyclomaticComplexMethod, 15 > 14 |
| same | 51:7 | LargeClass |
| `backup/VaultBackupUnicodePaginationTest.kt` (desktopTest) | 297, 331, 362, 368, 389, 411, 419 (column 1) | seven MaxLineLength |
| `repository/BiometricUnlockFreshnessIntegrationTest.kt` (desktopTest) | 428:30 | ReturnCount, 3 > 2 |

The ledger already records qualified PVA-033/035/038 closure from the consumed
C4 Linux03 evidence. Those qualifications survive. Detekt03 is a separate
failed static execution with zero application cases; it does not erase prior
results or authorize replay. This review adds no closure or denominator change.

## Biometric admission, ownership and counterexamples

Read all of `VaultRepositoryImpl.kt` and `DefaultBiometricUnlockService.kt`,
the complete twelve-case freshness fixture and nine-case verification-routing
fixture, the dedicated provider fixture as source only, and relevant repository
preemption/biometric sibling oracles.

1. Service captures `beginBiometricUnlock` before metadata or OS-key lookup
   (service lines 96–120). Capturing a replacement generation after retrieval
   would revive a revoked prompt; no complexity change may do that.
2. `unlockWithBiometricKey` checks activity, repository identity and generation
   under the same transition -> operation -> session mutex envelope, **before**
   checking key length. A stale malformed key must return VAULT_LOCKED without
   enrollment deletion. Checking length first would turn that scenario into
   INVALIDATED. A foreign token must never reach this repository's validation.
3. The early stale path owns no new session. Relocking/resetting state there
   would destroy or mislabel a newer password-opened session. The freshness
   oracle pins both state and active-key equality after the old completion.
4. Initial verification alone is insufficient: lock intents register under
   their own mutex even while the transition/session mutexes are held. Both
   existing-session success and new-session publication retain atomic
   `lockIntents.commit(expectedGeneration)`; `openSession` also verifies before
   and commits after the last-access write. The final-write preemption result
   is intentionally INTERNAL_ERROR rather than the earlier VAULT_LOCKED result.
5. Only `UnlockPreemptedException` may be converted to false/locked by an
   extracted admission predicate. Catching Exception/Throwable/runCatching
   would hide cancellation or unrelated failures. `ensureActive` stays first.
6. Provider/ordinary argument failures are not invalidation. Only typed AEAD
   authentication and private malformed-verification-plaintext rejection map
   to `BiometricVaultKeyRejectedException`. Cancellation is rethrown. No change
   may broaden those catches or publish before authenticated verification.
7. Provider-returned key remains service-owned and wiped in its finally;
   repository candidate is a separate copy with its own finally; committed
   session owns another copy. Moving verification/copy/finally across helpers
   solely to shorten the class could lose one of these ownership boundaries.
8. Existing fixture `retrieve` increments calls on every attempt. Wrong vault
   does not allocate/consume `nextCandidate` or a pending gate. Same vault with
   no candidate leaves gates untouched. With a candidate, transfer happens only
   after pause and optional synthetic cancellation; untransferred arrays are
   wiped locally, transferred arrays are left for the service. Its barrier,
   cancellation and finally structure are functional oracles, not lint clutter.

A private predicate that names **initial admission validation only**, remains
inside this owner and preserves all these positions can be a meaningful local
decomposition. It is not authority to split VEK ownership. A class-specific
LargeClass exception, if accepted, must be described as an intentional local
boundary exception, not as evidence the numeric size diagnostic was false or
as blanket lint/security clearance. Exact proposal and root adjudication remain
pending at this baseline stage.

## Pagination, snapshot and cleanup counterexamples

Read the full comparator/pagers, all thirteen common pagination tests, all six
Unicode Room tests and the eight DAO page queries. Followed V2 export from
stable-attachment/session ownership through `writeSnapshotMetadata`, database
manifest and all eight pager routes, metadata write finally and sink abort.

- `PRAGMA encoding` is selected from the actual reader connection inside its
  deferred transaction and must yield exactly one supported value. A guessed
  UTF8/default or Kotlin String ordering is wrong for supported encodings.
- Concrete counterexamples are U+FB00 vs U+10000 (UTF8 vs UTF16 order), U+0100
  vs U+00FF (UTF16LE low-byte-first), and U+100FF vs U+10100 (surrogate low-byte
  ordering). Existing expected signs and explicit Room ID lists are independent
  of the implementation; do not compute the oracle with the comparator itself.
- Both tuple components are validated even if the primary advances; secondary
  reset is allowed only after primary advance. Duplicate/backward keys fail
  before their consumer, and oversized pages before any consumer. Do not weaken
  those checks or normalize/case-fold crypto-bound IDs to satisfy naming lint.
- Both pagers consume bounded pages and advance cursor only after successful
  consumer completion. Throwable/cancellation propagation is unwrapped. A
  consumer's failure must not trigger another fetch or silently skip a row.
- SQL ordering, manifest counts and metadata come from the snapshot transaction.
  Managed content later remains under attachment/session stability, but the
  selected tests are **not** a universal cross-pass snapshot or external-provider
  guarantee. The ledger's finite-vector/native-provider qualifications remain.
- Unicode test setup fixes encoding before Room's first schema creation,
  confirms actual PRAGMA, inserts in reverse order, checks contents before
  export, restores across encodings, checks ciphertext/nonces and relationships,
  and decrypts managed content. Formatting must preserve every argument and
  oracle, including `getOrThrow` and constant expected IDs/content.
- Preserve nested NonCancellable fixture lock -> database.close -> bounded
  temporary-root removal finally, stream close/abort, credential clearing,
  password clearing, encrypted/subkey/plaintext wiping and no-follow-link guards.
  Test cleanup failure must remain failure, not success.

Moving unchanged enum/comparator declarations into their matching file can
preserve the enum's Kotlin/JVM identity and keep both internal pager functions
in the existing `BackupPaginationKt` facade. That is preferable to renaming the
whole file if exact source/callable compatibility can be retained. A new file
still changes the source inventory and cannot reuse C17-bound execution requests
as if unchanged. No schema, format, ID encoding or migration change is justified.

## Evidence limits and later validation

No project code was executed. A preliminary read-only JSON-shape inspection
failed on numeric `members`; the corrected schema-aware standard-library reader
then completed. This was a reviewer data-reader error, not a product/static
collector defect or a repeated consumed execution. No runtime/helper contents
were read or imported and no Git ran; the initial T/held-directory metadata
enumeration is expressly qualified above. No Telegram duplication was sent.

Smallest validation depends on the exact proposal: first text/byte equivalence
of pure moves and formatting, then fresh owner-admitted `:core:database:detekt`
and appropriate source-change-focused compilation/regression. No all22 static
repeat, fresh-provider launch or unrelated successful suite repeat follows from
eleven lint diagnostics. Exact future method selection belongs in the final
proposal review and root's admission, not in an executable command here.
