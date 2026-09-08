# Independent Linux review of the pending 105-method selection

Reviewer: `/root/verification`. Application/test authors are other agents from
the published handoff; this reviewer changed no application or test source.

This document records the initial source-only phase. The later independently
authored `METHOD-MAPPING-REVIEW.json` and `HISTORICAL-XML-MAPPING.json` resolve the
display-name preparation gap below using five explicitly approved Git transport
reads. `METHOD-INVENTORY.json` revision2 is the current exact mapping; the initial
unresolved inventory remains as `METHOD-INVENTORY-source-only-v1.json`. These new
transport observations add no application execution or runtime admission.

**SOURCE-ONLY; ZERO NEW EXECUTIONS; NOT EXECUTION ADMISSION.** No new product
finding or qualified closure is established. PVA-033, PVA-034 and PVA-035 remain
`IMPLEMENTED — REGRESSION VERIFICATION PENDING`.

## Exact selection and evidence status

`METHOD-INVENTORY.json` independently inventories the current checkout:

| Class | Declared methods |
| --- | ---: |
| `BackupPaginationTest` | 13 |
| `VaultBackupUnicodePaginationTest` | 6 |
| `BiometricUnlockFreshnessIntegrationTest` | 12 |
| `RepositorySecurityIntegrationTest` | 17 |
| `VaultBackupStreamingTest` | 39 |
| `VaultUnlockPreemptionIntegrationTest` | 7 |
| `RepositoryBiometricSecurityIntegrationTest` | 11 |
| **Total** | **105** |

The five whole-file SHA-256 values match the G12 matrix. Three selected classes
share `RepositorySecurityIntegrationTest.kt`; six other classes containing 25
declared methods in that file are explicitly excluded. The abstract fixture has
`BeforeTest`/`AfterTest` hooks, not inherited test methods. These are declarations,
not discovered or executed test cases. The task is `:core:database:desktopTest`,
not an aggregate test task.

All 105 selected method bodies were displayed and inspected. The compact
`SOURCE-BINDINGS.json` records the actual inspected ranges, including fixtures
and relevant production guards; it does not claim a fresh whole-project review.
Most production siblings remain outside this bounded pass. Hashing a whole file
does not convert uninspected ranges to reviewed source.

The historical index identifies XML for the three previously existing selected
repository classes and `VaultBackupStreamingTest`, but none for the three newly
authored freshness/pagination classes. At this checkpoint the pack contents have
**not** been read: root suspended ad hoc readers while the clone's directory/file
device topology is independently assessed. The inventory therefore keeps exact
XML testcase display names unresolved. A literal `[desktop]` suffix is a candidate,
not adopted here. Historical XML access and final mapping must be resolved before
admission; no permissive post-result stripping is allowed.

## PVA-033: freshness, guards and counterexamples

The current service captures repository-owned admission at
`DefaultBiometricUnlockService.kt:96–101`, before metadata, enrollment lookup or
key retrieval can suspend. In `VaultRepositoryImpl.kt:294–329`, foreign/stale
admission is rejected before candidate-size invalidation or the already-open
success path. The stale rejection does not wipe/relock an unrelated new session.
The ordinary key-verification path still validates metadata and authenticates the
candidate. `openSession` verifies the generation before the last-access write and
commits key/state publication under the lock-intent mutex (`729–738, 804–840`).
Completed already-locked/no-key locks still advance the generation (`361–376,
582–607`), so that short-circuit is not a counterexample.

The 12 freshness methods independently exercise metadata/contains/retrieve
barriers, completed/pending/final-write locks, stale-invalid ordering, a fresh
invalid candidate, a fresh valid attempt, an already-open positive, a newer valid
session and a foreign admission. The controlled key store transfers ownership
before returning its candidate and preserves a reference for the post-service wipe
assertions (`BiometricUnlockFreshnessIntegrationTest.kt:428–445`). Assertions run
before teardown's defensive wiping; they are not made true merely by teardown.
The cancellation case cancels the coroutine after transfer and before the
service's `ensureActive`; this is genuine coroutine cancellation in the authored
scenario, not evidence about an OS biometric cancellation callback.

Attempted disproofs retained:

- An already-unlocked shortcut cannot let stale admission succeed: freshness is
  checked first, and the shortcut itself commits against the captured generation.
- A malformed stale candidate does not trigger enrollment deletion: admission
  rejection precedes length/verification failure.
- A completed lock on an already locked repository does not leave the generation
  unchanged; registration occurs before that fast path.
- The final-write case deliberately expects `INTERNAL_ERROR`, because the
  repository's preempted result there is the existing ordinary
  `IllegalStateException`; other stale-admission cases expect `VAULT_LOCKED`.
  Runtime scoring must not normalize that difference away.

No contrary production path was established in this bounded review. However,
stale work can still reach a synthetic/provider retrieval before publication is
refused. This fix does not eliminate a late native prompt. Real Android pre-34
memory-pressure callbacks, OS authentication, cancellation and physical no-flash
behavior remain separate target/device gates. The seven preemption and eleven
biometric-security sibling cases use real repository/Room/crypto plus controlled
timing/key-store delegates, not platform key-release proof.

## PVA-034: discriminating budget oracle

The focused method at `VaultBackupStreamingTest.kt:1096–1122` obtains a real
encrypted Room row through `TagRepositoryImpl.save`, with ID `four`. Production
tag creation supplies a 32-byte blind index and valid encrypted payload/nonce.
The same row is offered to two **fresh** validators: retained-byte budget four
accepts; budget three must throw the exact budget diagnostic.

`VaultBackupService.kt:850–857` charges the identifier before uniqueness,
name-hash, payload and color validation. `RetainedIdentifierBudget.retain`
charges the four ASCII bytes and emits the exact asserted message. The positive
control rules out the former empty-name-hash false oracle. If only the tag charge
were omitted, source tracing reaches otherwise-valid remaining guards and the
asserted exception would be absent. **No mutant was executed.**

The row wrapper clears its arrays in `finally`. The test calls `accept`, not
`finish`; it does not establish acceptance of a complete snapshot without its
required metadata row. That is intentional for this narrow oracle and does not
justify a whole-restore claim. Production validation/format/schema/KDF behavior
was not changed by PVA-034.

## PVA-035: actual encoding and real persistence boundary

The comparator handles UTF-8 scalar order and unsigned UTF-16 byte order without
normalizing identifiers. Both pagers validate row keys before comparison and
retain strict positive progress, nonempty/oversized-page and limit guards. The
apparently unsafe high-surrogate lookahead in `scalarAt` is reached by production
pagers only after the existing well-formed-Unicode policy, with a 512-code-unit
prebound. Direct arbitrary malformed calls to the internal comparator are not a
new reachable production defect established by this review.

`VaultBackupV2Service.kt:242–257` reads `PRAGMA encoding` in its owned reader
transaction. The immutable order is passed through all eight DAO paging routes,
including the managed-object size scan and later content pass. The DAO predicates
and `ORDER BY` clauses are unchanged. Ordinary UUID IDs remain ordered as before;
accepted Unicode IDs are not renamed, re-encrypted under new IDs or rejected as
an expedient fix.

The 13 common methods have independent finite order vectors, forward/reverse
signs, equality/prefix controls, invalid/maximal Unicode, simple/composite
progress, and consumer-error/cancellation paths. `ArrayDeque` pages are controlled
input; they do not implement SQL filtering. The method at `239–269` cancels a
child suspended inside its consumer; the method at `215–237` directly throws a
`CancellationException`. Those are distinct evidence tiers. Neither proves
cancellation during a blocking native fetch.

The six Desktop methods use a real file-backed bundled SQLite database. Encoding
is set before schema creation and asserted after the Room reopen. Reversed seed
insertion cannot generate the expected ordering accidentally. The all-eight-DAO
method explicitly checks independent expected key/tuple sequences at limits one
and two. Public backup/inspect/restore paths, production encryption, relationships,
history and managed content are exercised by the authored roundtrips. Before
export, the fixture proves its encrypted source can be read; after restore it
checks selected values, relationships, ciphertext and five-byte attachment
content. Logical V1 import/reexport is separate from historical database-schema
migration.

Limits remain: finite vectors and selected field/set assertions, not exhaustive
Unicode/provider coverage; a second export checks commit rather than performing
a second complete restore. `MemorySink` is a copied-buffer adapter capped at
256 KiB and `MemorySource` fragments to at most 19 bytes, not an external file
provider. The final managed-object pass remains outside the metadata reader
transaction; no stronger cross-pass snapshot guarantee is inferred. Android/iOS
native provider parity and schema1–4 upgrade matrices are not proved here.

## Operational fixture limits relevant to future admission

These are preserved limitations, not new product/PVA families or reasons to
pretend tests already failed:

1. `VaultBackupStreamingTest.kt:139–143` sequentially locks, closes Room and calls
   `deleteRecursively`, ignoring its Boolean result. A preceding teardown failure
   can skip later cleanup. `RepositorySecurityIntegrationFixture:2186–2190` also
   uses sequential teardown. The outer admitted owner must not infer cleanup from
   green XML and must retain independently verified fixture/temp cleanup.
2. Several older preemption tests release their gate only on the normal path and
   call non-cancellable repository lock in `finally`. An early assertion failure
   can require test cancellation/outer timeout to settle the still-suspended
   child. No hang was observed. Bounded invocation/cancellation and owned-worker
   settlement remain necessary; do not report immediate exceptional cleanup as
   source-proved for every sibling.
3. The new Unicode fixture has nested `NonCancellable` lock/DB/directory cleanup,
   a temp-parent/name/no-symlink check and no-follow traversal. It is cooperative
   synthetic-fixture cleanup, not a hostile-same-user namespace sandbox.
4. All filesystem fixtures must use the admitted forked worker's isolated
   `java.io.tmpdir`, not ambient `/tmp`. One streaming attachment is 700,123 bytes;
   the overquota legacy test changes metadata/rows, not a generated maximum-size
   attachment. KDF iterations across methods can dominate runtime/RAM; source
   counts alone do not measure them.

## Review failures, resources and fences

Two source-location `grep` commands named nonexistent
`BackupIdentifierBudget.kt` and `BackupMetadata*` paths; they returned nonzero and
the chained displays did not run. Subsequent bounded source lookups located and
read the actual `RetainedIdentifierBudget.kt` and `BackupEntityBinaryCodec.kt`
definitions. These were inspection-command failures, not test cases, product
regressions, build attempts or retries of any restricted scope.

Periodic `df`/`free` observations were reported to root; at the later observation
the worktree and `/tmp` were above the 12 GiB disk floor. No resources were freed
by this reviewer, and those point observations do not admit any run or establish
host exclusivity. This task created only compact review JSON/Markdown. No build
outputs, caches, persistent server/worker, emulator or background task was created;
Gradle `--stop` is not applicable to this source/document task.

Root is sole build/test/commit/push/admission owner. No application/test/compiler,
project helper, archived runner or handoff viewer was executed/imported by this
reviewer. No pack bytes, real vaults/backups, clipboard, tester/private signing
data, production artifacts or external service settings were accessed. G7/G8
stay CLOSED; PVU-007 STOP; PVU-011 NO RETRY; PVA-029 retains its recorded FAIL and
no automatic retry. The eight PVD choices, build1017001 and publication boundaries
are unchanged.
