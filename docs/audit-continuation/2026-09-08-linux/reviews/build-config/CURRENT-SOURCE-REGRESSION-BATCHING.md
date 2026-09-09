# Smallest current-source regression cycle — recommendation

Author `/root/build_config`, 2026-09-09. **Source-only advice, not admission.**
Product target: published C4 `da8ff89b9a8579017d5d524e628dff5251f2bb90`, tree
`cf0a8a702e7cd6948236be18b491bb5a21b5886e`. Active helper corrections are separate
unpublished inputs; a later run must bind its actual selected source and wiring.

## Decision

Prefer **one current-C4, narrowly filtered cycle**, not an old-source105 run just
to precede another build. Frozen105 stays unchanged, **HOLD/unexecuted**: its fixed
commit/1198-file manifest/one-task/count/XML/closer contracts cannot be retargeted
by appending filters. Use small separately reviewed task/worker wiring and fresh
root execution/coordination/cleanup admission, not a generalized framework. If
immutable105 genuinely becomes ready first, it remains useful storage evidence,
but cannot validate C4 fixes.

### Minimum practical class-level selection

Every name below is a whole-class filter; these are declarations, not passes.

| Gradle task | Exact package prefix and class suffixes (method counts) |
| --- | --- |
| `:feature:credential:desktopTest` | `com.passvault.feature.credential.presentation.` — `CredentialCustomFieldSaveFreshnessTest`11, `CredentialCustomFieldDraftTest`13, `CredentialCustomFieldCapacityTest`7 |
| `:shared:desktopTest` | `com.passvault.shared.navigation.` — `NavigationBackCoordinatorTest`11, `CredentialBackFreshnessCompositionTest`7 |
| `:core:database:desktopTest` | `com.passvault.core.database.repository.` — `BiometricVerificationFailureIntegrationTest`9, `BiometricUnlockFreshnessIntegrationTest`12, `RepositoryBiometricSecurityIntegrationTest`11, `VaultUnlockPreemptionIntegrationTest`7, `RepositorySecurityIntegrationTest`17 |

**105 ordinary declarations/ten classes**:49 editor (20 new,29 existing) and56
database (9 new,47 existing), **not immutable-handoff105**. Capacity/draft protect
PVA-031 and Save compatibility; real database siblings protect stale admission,
lock/session publication, invalidation, transient failure, password/legacy
envelopes and persisted edits. Do not add matcher/metadata/lock-failure classes
merely because they share a file, or invent a per-method pruning exercise.

PVA-033/034/035's58 backup methods stay **pending**: package
`com.passvault.core.database.backup.`, `BackupPaginationTest`13,
`VaultBackupUnicodePaginationTest`6, `VaultBackupStreamingTest`39. Explicitly
appending these already-planned filters gives **163 ordinary +4 provider =167**
without another module compilation. Minimum below is109, not105-matrix
completion; the backup methods are not silently dropped. Cost is unmeasured.

### Android32 task discovery: include in the same initial invocation

Independent challenge of android32's `ANDROID32-TASK-GRAPH-NEXT-ACTION.md`
(`597fbcd21f64115fdba5e90c3346f06e6281cb6f34ebea78f807cbaf55809340`):
**include `:core:crypto:tasks --all` with the initial current-source ordinary
selection**, not a separate build/admission cycle or the immutable105 runner.
The reviewed module wires a device harness but does not prove generated package
task names; listing is useful, not device evidence or a full dependency graph.
It still realizes configuration/tasks and is not guaranteed cost-free. Use the
combined run's explicitly admitted network policy: `--offline` is global, so do
not silently impose the standalone proposal's offline policy on a fresh test
dependency bootstrap, or silently relax it online. If offline-only is chosen,
missing artifacts are a recorded blocker, not automatic retry authority.
Wrapper bootstrap/plugin networking remains separately qualified. Preserve the
listing with the same logs/cleanup; no auto-chain to guessed package/device tasks.
License/archive/device gates do not block this configuration observation. Root
must admit the combined bounds rather than treating the standalone5-minute
proposal as a bound for all tests. No separate report/control framework needed.

## Cold-provider part: four fresh workers, not a class-wide invocation

Select exactly one method of
`com.passvault.core.database.repository.BiometricProviderInitializationFaultIntegrationTest`
per fresh JDK17 worker: `prepare`, then `success`, `wrong-key`, `loader-io` (literal
method names in source). Set `PASSVAULT_PVA038_MODE`; validate/bind the producer's
317-character `PVA038_FIXTURE` unchanged as `PASSVAULT_PVA038_FIXTURE` for all
consumers. Each mode needs its own owned `pva038-*` run root and worker-startup
`-Duser.home=<root>/home`, `-Djna.tmpdir=<root>/jna`,
`-Djava.io.tmpdir=<root>/tmp`; loader-io instead uses its zero-byte regular
`<root>/tmp-blocker`. Its Gradle/compiler temporary directory must stay healthy.

Use a **small reviewed Gradle Test init/configuration** for worker args/environment
and `maxParallelForks=1`, not poisoned client-wide `JAVA_TOOL_OPTIONS`. Simplest:
one ordinary three-task invocation, then four serial single-method database
invocations in the same admitted cycle. Preserve XML/logs before overwrite;
reuse this cycle's imminent-needed compiled outputs/private dependency cache.
No repeated `--rerun-tasks` or intermediate `clean`; require actual test execution,
not up-to-date credit. No custom Test-task/fixture-exchange framework is needed;
`forkEvery=1` alone does not provide mode/fixture isolation.

**109 declarations =108 regression methods +1 producer**; PVA-038's13 comprise
9 routing/1 producer/3 consumers, not13 cold tests. Require exact XML cases,
no failures/errors/skips, actual fresh-worker startup and loader/IOException
witnesses. Warm siblings use real Room; cold loader-io intentionally uses the
in-memory DAO. A pre-target framework failure is not the required witness.

## Narrow execution requirements / remaining gaps

Keep the existing strict JDK17/wrapper/one-worker/non-daemon/no-CoD/no-cache/
in-process-Kotlin flags, SDK-download prohibition and JDK discovery/download
disabling. Proposed2GiB Gradle/512MiB worker heaps are not RAM guarantees. Narrow
blockers: actual worker startup wiring, bootstrap/toolchain/dependency viability,
exact source/XML selection, fresh owned roots and root's time/resource/cleanup
admission. Install cleanup first; preserve evidence, wrapper `--stop`, settle
owned workers after each invocation, then remove allowlisted outputs no longer
needed (never tracked schemas/shared caches). Monitor RAM/disk; one local/CI job.

`:shared:desktopTest` compiles shared/feature dependencies; filters restrict
execution, not compilation. No root `test`/`check` or packaging. Rendered UI/IME,
native gestures, Android32/iOS-provider/physical security/Hello remain separate.
All STOP/NO-RETRY/CLOSED restrictions remain. No execution/import/scratch/cache/
worker or closure was created here.

### Existing source references (not new audit scope)

- `reviews/build-config/REVIEW.md`, `LINUX-RUNNER-CONFIG-REVIEW.md`: module graph,
  source-only toolchain/loader qualifications; `shared/build.gradle.kts` adds the
  shared target/dependency boundary.
- `reviews/editor-independent/BACK-PATCH-INPUTS-v2.json`, `BACK-PATCH-REVIEW.md`,
  `INPUT-CALLBACK-PATCH-REVIEW.md`: exact editor source and20-new/29-total counts;
  the unchanged draft13 and capacity7 declarations are in their named sources.
- `reviews/verification/METHOD-INVENTORY.json`: frozen105/47+58 and excluded
  siblings. The security/preemption/matcher/metadata/lock classes share
  `RepositorySecurityIntegrationTest.kt`; class names are not separate files.
- `reviews/storage/PVA-038-INDEPENDENT-PATCH-REVIEW.md` and both new biometric
  test sources: routing, fixture, modes, worker startup and real-loader oracle.
- `scripts/audit/linux_database_validation.py` constants/admission/XML contract;
  `publication/CHECKPOINT-4-PUBLICATION-ADDENDUM.md` and
  `CHECKPOINT-4-PUBLISHED-RECEIPT.json`: HOLD and current committed identity.

Inspection qualifications: combined displays sometimes truncated; relevant
contracts were reread narrowly. Guessed/brace-expanded nonexistent paths gave
inspection errors, not test failures. Denominators/closures remain unchanged.
