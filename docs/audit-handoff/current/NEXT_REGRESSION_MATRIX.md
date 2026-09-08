# G12 next-regression prerequisites — PVA033 / PVA034 / PVA035

**NOT ADMITTED — PREPARATION ONLY. No fresh regression pass, defect closure or central coverage credit.**

Reviewer: `/root/remed_storage`. Scope: delivered generation12 only; root remains sole source/build/admission and central-status owner.

The pinned issue snapshot retains **IMPLEMENTED — REGRESSION VERIFICATION PENDING** for all three findings. The pinned G12 gate snapshot retains **BUILD_ADMISSION: BLOCKED** and **G12_FULL_REGRESSION_STATIC_MATRIX: BLOCKED**, with zero new runtime test invocations. These are quoted snapshot statuses, not fresh live observations or changes.

## 1. Scope and next-test matrix

| Finding | Purpose | Selected source methods | Current execution status |
| --- | --- | ---: | --- |
| PVA-033 | biometric attempt/lock freshness | 47 (12 primary) | NOT EXECUTED NOT ADMITTED |
| PVA-034 | test-quality discriminating identifier-budget oracle, not a production bypass | 39 (1 primary) | NOT EXECUTED NOT ADMITTED |
| PVA-035 | encoding-aware SQLite BINARY pagination | 19 (19 primary) | NOT EXECUTED NOT ADMITTED |

PVA033's 47 methods are 12 new freshness cases plus 17 general repository, 7 unlock-preemption and 11 biometric-security siblings. PVA034 changed one oracle inside the selected 39-method class. PVA035 has 13 common helper and 6 Desktop integration methods. **These are source declarations, not executed tests.**

### PVA-033

**Required source oracles (not observed results):**
- Admission precedes metadata/contains/retrieve suspension; a completed lock rejects the old attempt.
- Fresh post-lock and already-open positives succeed; stale completion cannot relock or wipe a newer valid session.
- Stale admission dominates malformed-candidate invalidation; a fresh invalid candidate still invalidates enrollment.
- Pending lock refuses admission before metadata/platform access; received candidate wiping survives propagated cancellation.
- Lock during final last-access publication prevents opening; foreign repository admission cannot authorize another repository.

**Real boundary:** In-memory Room, BundledSQLiteDriver, DesktopCryptoEngine and production repository/service; general repository fixture also uses these real boundaries.

**Controlled/synthetic boundary:** Synthetic key material and deterministic DAO/key-store/crypto timing or fault doubles, not real OS authentication, app UI, clipboard, Keychain/Keystore or a user vault.

**Cleanup:** In the473-line freshness fixture's paired-child cleanup cases, source requests both sibling cancellations before either join, tries the second join in finally, wipes the independent freshKey before a cancellable join, and marks transferred candidate ownership before return. Returned-reference assertions occur before teardown wiping. Finally attempts repository lock, fake disposal and DB close. Shared repository fixture2186-2190 instead performs sequential lock then DB close. Source cleanup intent is not observed settlement.

**Limits and separate gates:**
- Final last-access failure expects INTERNAL_ERROR; other stale rejection cases expect VAULT_LOCKED. Do not change this source oracle during result scoring.
- The new admission API means an unchanged whole original-source red fixture cannot simply be assumed to compile.
- A stale attempt may still reach an OS prompt: this correction is final publication dominance, not prompt redesign.
- Physical Android pre34/Android13 memory-pressure, real biometric cancellation/prompt and no-flash behavior remain separately admitted target/device gates. No such observations exist here.

### PVA-034

**Required source oracles (not observed results):**
- The exact method at1097 uses TagRepositoryImpl to save id four and queries the actual encrypted Room row.
- A fresh4-byte stream validator accepts that same otherwise-valid row; a fresh3-byte validator rejects with exactly: Backup identifiers exceed the validation memory budget.
- Clear returned encrypted arrays in finally; retain related valid-boundary and the39-method class's streaming/corruption/cancellation/cleanup controls.

**Real boundary:** Real in-memory Room/crypto/production backup and repository wiring plus real synthetic filesystem/blob I/O; one fixture attachment is700123 bytes.

**Controlled/synthetic boundary:** Capacity reporting is a double; candidate bytes and LocalAttachmentBlobStore I/O are real. No user's data or maximum-size attachment generation. Forked worker java.io.tmpdir must be confined.

**Cleanup:** The focused test clears value in finally1119-1121; suite teardown139-144 sequentially locks repository, closes DB and attempts deleteRecursively. Successful cleanup has not been observed.

**Limits and separate gates:**
- This method calls acceptTag, not finish(), and does not establish whole-snapshot or complete-restore acceptance.
- The omitted-charge counterfactual remains source reasoning; no mutant has been run.
- An executable narrow mutation control is optional only if separately safely admitted with its own exact source/plan binding. It is not silently added to the frozen105-method run.
- The overquota legacy test edits metadata/rows, not an actual maximum-size generated file.

### PVA-035

**Required source oracles (not observed results):**
- 13 common methods cover finite independent UTF8/UTF16LE/UTF16BE order vectors, strict simple/composite progress, equality/prefix/invalid/oversized/maximum-key controls and exception/cancellation behavior.
- Distinguish the genuine child-Job suspended-consumer cancellation239-269 from the directly thrown CancellationException215-237.
- Six Desktop methods cover public encrypted UTF8 roundtrip, UTF16LE/BE export to UTF8 restore, all eight real DAO page queries in each asserted actual encoding at limits1/2, logical V1 import/reexport and empty/singleton/mixed plus distinct ASCII-only controls.

**Real boundary:** Real file-backed SQLite with PRAGMA encoding set before first schema and asserted after real Room reopen; production Desktop crypto/repositories/public backup/managed-blob seeding.

**Controlled/synthetic boundary:** Common ArrayDeque pages do not implement SQL filtering. Desktop MemorySink is a copied-buffer synthetic transactional adapter capped at256KiB; source chunks are at most19 bytes and managed attachment content is5 bytes.

**Cleanup:** Desktop nested NonCancellable finally attempts lock, DB close and owned-directory cleanup; fixture cleanup checks name, no symlink and expected temp parent and walks no-follow. Cancellation and returned-buffer disposal are source obligations, not measured runtime settlement.

**Limits and separate gates:**
- No cancellation-during-fetch, native-blocking cancellation or device/provider parity is demonstrated.
- Current fresh DB schema plus logical V1 backup compatibility is not a historical database-schema1-4 migration matrix or all V2 metadata1/2 combinations.
- Selected field/ciphertext/set oracles are finite; second reexport is commit-only rather than a second full roundtrip.
- The final managed-object pass is outside the reader transaction; no stronger cross-pass snapshot guarantee is inferred.
- Six methods contain multiple encoding/fixture/KDF iterations; elapsed/peak native/heap/disk cost is unmeasured.

## 2. Exact candidate selection — not an invocation authorization

Candidate task: `:core:database:desktopTest`. Ordered exact class filters:

| Order | FQCN | Source methods |
| ---: | --- | ---: |
| 1 | `com.passvault.core.database.backup.BackupPaginationTest` | 13 |
| 2 | `com.passvault.core.database.backup.VaultBackupUnicodePaginationTest` | 6 |
| 3 | `com.passvault.core.database.repository.BiometricUnlockFreshnessIntegrationTest` | 12 |
| 4 | `com.passvault.core.database.repository.RepositorySecurityIntegrationTest` | 17 |
| 5 | `com.passvault.core.database.backup.VaultBackupStreamingTest` | 39 |
| 6 | `com.passvault.core.database.repository.VaultUnlockPreemptionIntegrationTest` | 7 |
| 7 | `com.passvault.core.database.repository.RepositoryBiometricSecurityIntegrationTest` | 11 |

**Seven classes, five files, 105 source-declared methods: 87 original selected + 18 explicit siblings.** All five file hashes match delivered G12. The three repository classes share one file: file selection is not class selection. Six other sibling classes containing 25 methods remain excluded. Withdrawn115/130 counts are not adopted.

Exact names, annotation/declaration lines and XML paths are retained in `EVIDENCE.json`, rebound from:
`reviews/fresh-runtime-successor-independent-navigation-v1/METHOD-INVENTORY.json`
(SHA-256 `8a09ebe0105b42dd447af5073190f5f9dadf89813017d58480dda15e04055bcb`).

Expected evidence is exactly seven regular, nonempty files at
`core/database/build/test-results/desktopTest/TEST-<FQCN>.xml`, with no extras. Independently reconcile all 105 exact methods once each and zero failures/errors/skips. Freeze any framework decoration such as `[desktop]` **before admission/results**; no permissive post hoc normalization. Exit0, a file count, successful stop/cleanup or this inventory is not a semantic pass.

## 3. Conjunctive root-owned prerequisites

The following is a preparation checklist, not confirmation that current conditions hold. Satisfying disk alone, finishing an inert proposal, or retaining old accepting source reports cannot admit a run.

### M01 — Root-owned G12 freeze and one-shot admission

Bind the final delivered G12 source manifest/freeze and author acknowledgements, exact new runner, plan, baseline, admission and single instance/U/run/evidence layout. Freeze the ordered seven FQCNs, all 105 source method names, exact XML mapping, timeout and output/evidence caps. Older G10 runtime-plan/source labels cannot authorize G12.

Owner: root. Required: any future Gradle invocation.

### M02 — Runner and inert controls

Finish a distinct root-authored proposal and obtain fresh exact accepting reviews that discharge F01-F07. The existing v1 review is REVISE; v2 and any G12 proposal were not opened/reviewed here. The19 existing cases are unexecuted specifications, not an implemented green suite; any inert checks need their own explicit admission and retained outcomes. Old36 harness checks confer no successor credit.

Owner: root plus genuinely independent exact reviewer. Required: admission.

### M03 — Actual Gradle graph, native loaders and environment

Review exact wrapper/settings/properties/plugins/version catalog/dependencies/native-loader/test-worker inputs plus fixed JDK/interpreter/SDK policy and strict dependency verification. A JVM test filter does not guarantee JVM-only configuration or compilation. Bind allowlisted environment and effective forked-worker HOME/java.io.tmpdir/Gradle/Konan confinement; forbid ambient option/init/property injection or toolchain substitution.

Owner: root plus independent configuration reviewer. Required: admission.

### M04 — Original coordination lock and exclusivity

Use the existing build-coordination/passvault-validation.lock with original parent/leaf identity, uid, modes and single-link checks; no mkdir/O_CREAT/replacement or per-U substitute. Establish immediate outermost FD cleanup. Prove current source/build/cache exclusivity, no conflicting producer/cohort and absence of all standard W build outputs, not only core/database/build.

Owner: root. Required: allocation and launch, rechecked under lock.

### M05 — Fresh isolated instance and durable allocation

Choose absent, nonoverlapping U/HOME/TMP/cache paths; no reuse, copy, prewarm, adoption or reopening of G7/G8. Record durable allocation intent and original ownership seals before Gradle; thereafter verify only. Preserve exact environment/instance identity through stop and cleanup. Synthetic fixtures must land in the forked worker's approved java.io.tmpdir.

Owner: root. Required: any child.

### M06 — Contemporary resource evidence

Meet launch floors of12GiB free disk and25% memory; maintain running floors of8GiB and20%. Prove W/U/R same-volume capacity or check each relevant volume. Historical below12GiB report is not a present measurement. Polling, Xmx2g, one worker and advisory locking are not a hard memory/process sandbox. Disk sufficiency alone never opens admission.

Owner: root. Required: launch and throughout execution.

### M07 — Command journal and stop

Durably write exact command intent and stop_required before the fork/PID gap. Internal cleanup-only stop must use the fixed original wrapper/environment with600s budget. Success requires exit0 and proven owned-process settlement. Failed, started or ambiguous stop retains the active incomplete journal and original run/HOME/TMP; no automatic retry, recreation or normalization to CLOSED.

Owner: root. Required: fork and terminal disposition.

### M08 — Evidence and independent semantics

Preserve commands/logs/source bindings and all seven exact regular nonempty XML files with no extras before success cleanup. Independently reconcile all 105 exact methods once each, with zero failure/error/skip. Fix any framework display-name decoration before admission, not after results. Exit0, file count, cleanup success and an inventory cannot establish semantic pass.

Owner: root captures; genuinely independent reviewer reconciles. Required: any regression-pass claim.

### M09 — Deletion and cache closeout

Approve evidence retention and dedicated-cache CLOSEOUT_REQUIRED contract before run. Preserve evidence/source before independently safe, exact allowlisted descriptor-bound deletion. Unknown outputs/ownership stay retained. Cache deletion needs a later separate exact admission, original ownership and per-path durable delete intent/settlement; absence is not silently success and failure is not automatically retried. No generic recovery CLI or cache manager is needed.

Owner: root. Required: execution approval and any later cleanup.

### Fixed command/environment contract — reference only

The prior contract allows only the canonical frozen `W/gradlew`, the one database task and seven ordered exact FQCNs above, `--rerun-tasks --no-configuration-cache --stacktrace`, plus the reviewed flags:
`--no-daemon`, `--max-workers=1`, `--console=plain`, `--no-parallel`, `--no-configure-on-demand`, `-Pkotlin.compiler.execution.strategy=in-process`, and the **single argv item** `-Dorg.gradle.jvmargs=-Xmx2g -Dfile.encoding=UTF-8`.

Historical fixed JDK reference:
`/opt/homebrew/Cellar/openjdk@17/17.0.17/libexec/openjdk.jdk/Contents/Home`.
This is not a current presence observation or an admitted shell command.

No extra/aggregate tasks, wildcard filters, `-x`, init scripts, `--continue`, arbitrary properties, weakened dependency verification, arbitrary instance/run IDs, fallback or general script/selftest/native/synthetic plan kinds. `--stop` remains internal cleanup-only. Root must bind the original W/R/P/A/AR/remediation/base identities and fresh-U nonoverlap; this reviewer did not inspect those live paths.

## 4. Prior runner disposition that a future proposal must resolve

The exact reviewed v1 runner SHA-256 was
`4163449af0f5d3cdf1d12264c40ee18809992e3351ffac04715bfad1ee41246f`.
The bound independent report is **REVISE**, with seven P1 **operational** correction obligations, not new product findings:

| Obligation | Required disposition in a separately reviewed candidate |
| --- | --- |
| F01 | Sticky command failure, unstarted/ambiguous status and cancellation; an aggregate exit0 must never mask them. |
| F02 | Recheck exact admission under the original shared lock; durably seal allocation intent and original evidence/root/parent/child ownership before Gradle. Partial or replaced ownership means HOLD. |
| F03 | Delete only through retained original descriptors with identity-bound operations, or retain outputs for separately admitted cleanup. A fresh pathname or rmtree capability flag is insufficient. |
| F04 | Require stable pre/open/post-fd/post-path identities for every bound input/source read; never rebase a digest onto replacement content. |
| F05 | Conservatively account for the complete after-process set, births and cwd/identity churn; unknown ownership means HOLD, not process-name/group killing. |
| F06 | Validate actual reviewer-authored acceptance purpose, independent reviewer, exact runner/source/instance bindings and disposition. Root labels, stale/HOLD reports and duplicate authors do not count. |
| F07 | Before successful cleanup require exactly seven regular nonempty expected XML files with no extras. Later independent105-method semantic scoring remains a separate obligation. |

Review: `reviews/g10db-runner-draft-navigation-v1/REPORT.json`,
SHA-256 `6d0a70655f931eeb838dc24cdd7afd8bd67147b6a7a79613034f3641bbb05b74`.

The retained **19 inert case specifications have zero executions**. They cover sticky failures/cancellation; under-lock drift; partial bootstrap/ownership; fork/PID gaps and failed stop; root replacement/partial deletion; reader replacement; process churn/PID reuse; false acceptances; missing/empty/extra/bad-method XML; failed compile; invalid argv/root/resources; and nominal settlement. Their source is
`reviews/g10db-runner-draft-navigation-v1/INERT-REGRESSION-CASES.json`,
SHA-256 `fda9349691beb680e9ccfafa652f6984c5356757c2a12f4f8402bdd4a72ae236`.
They are not implemented/passing controls. Old36 harness results do not validate a successor.

**No runner source was opened, imported or executed for this packet**, including `tools/validation_g10db_one_shot_v2.py`. Its partial/unadmitted state and any prospective distinct G12 proposal require fresh exact review; the old seven obligations do not constitute a diagnosis of unread replacement bytes.

## 5. What can be resolved without user data/hardware

- Root engineering work can finish/freeze/review the runner, mapping, graph/native-loader/environment/isolation contract and independently admitted inert controls; then, only if newly admitted, execute the synthetic Room/crypto/storage regressions and obtain independent result reconciliation. No private user vault or physical authenticator is needed for that bounded host run.
- Contemporary disk/memory/exclusivity, strict dependency availability and exact toolchain/bootstrap conditions still require root evidence. Historical cold-cache/fooJay failure cause/current availability is unobserved. Failure must not trigger closed-cache reuse, relaxed verification, toolchain substitution or automatic retry.
- Physical biometric prompt/memory-pressure/cancellation/no-flash and broader non-host/provider/historical-schema combinations are separate target/provider gates, not claims supplied by host doubles. Do not recast them as PVU007/PVU011 investigation or a PVA029 retry.

## 6. Provenance, overlap and hard fences

Current delivered canonical G12 source identity:
`7fd2dab3eac6269baede8a0ca550f0cbdfee20391a12dfbd9ac84cdac5401d91`.

Raw manifest artifact:
`verification/source-manifest-v12.json`,169300 bytes,
SHA-256 `2f029f9cefd3262313e960b1bcc3cabd875d02ebe31a44e6e78f946bb3e98f1f`;
811 source tuples (metadata, not full semantic audit).

Delivery:
`delivery/generation12/FILES.json`,46323 bytes,
SHA-256 `5de7e5c50b146c23827879c4ebd57fced4a4a1b8046cdea94bad978934c09c54`;
122 changed-file records (metadata).

Durable issue-map snapshot:
`reviews/g12-checkpoint-inputs-root-v1/snapshots/findings/issue-to-fix.json`,908468 bytes,
SHA-256 `64af0e4ea483711008ad27ee65075850f7708181a6227bcec91390c84b319637`.
Only rows32/33/34 (zero-based) support this matrix. Root reported a later PVA030 wording-only central correction; the same-hash immutable snapshot was verified instead of rereading mutable `findings/issue-to-fix.json`.

Pinned gate snapshot:
`verification/g12-current-gates.json`,3775 bytes,
SHA-256 `a15281540b42c52bc1d4ef8fc985557ab5beb917d27ec278a30bdf446a09fe7d`.
No silent refresh or central adoption.

All nine retained after-images below were raw-byte hash-checked against delivery/manifest. Full inert text was displayed for the473-line freshness fixture,125-line pagination helper,291-line common test and522-line Unicode integration fixture; streaming/repository source displays were bounded. Total distinct displayed physical lines: 2308 across 6 files; **zero central review coverage credit**. Hashing the other service/repository/V2 bodies is not a full semantic review; behavior advice also relies on explicitly pinned prior source-only reviews.

The reviewer originated PVA034/PVA035 and had prior design/review involvement. Root authored PVA034's patch; do not mislabel prior source reviews or this preparation packet as fresh independent executable verification. Actual results require genuinely independent reconciliation.

**Permanent fences:** G7/G8 runtime/recovery/replay/cache CLOSED; PVA029 FAIL/no automatic retry; PVU007 STOP/no investigation or reformulation; PVU011 NO RETRY/no containment relaxation. No helper/project execution, syntax/AST/compiler/tests, runtime/cache/process/resource/network/Git/live W/P/private reads, replay or broad traversal occurred. Only new report files in this packet's namespace are written. Source and all central statuses remain untouched.

## Appendix — exact G12 source/test bindings

- `core/database/src/commonMain/kotlin/com/passvault/core/database/repository/DefaultBiometricUnlockService.kt`
  - 9170 bytes; source mode0644 / retained R mode0600; SHA-256 `431356205bb300aeb0a7c14759eef0bbf09583d3100082e5dd0103ac731a90f5`.
  - Retained after-image: `reviews/PVA-033-root-patch-v2/after/core/database/src/commonMain/kotlin/com/passvault/core/database/repository/DefaultBiometricUnlockService.kt`.
- `core/database/src/commonMain/kotlin/com/passvault/core/database/repository/VaultRepositoryImpl.kt`
  - 36754 bytes; source mode0644 / retained R mode0600; SHA-256 `f2fc2ff02926e62230eff5e4723b2a68998649eee5f758f5ff99df93bcf61469`.
  - Retained after-image: `reviews/PVA-033-root-patch-v2/after/core/database/src/commonMain/kotlin/com/passvault/core/database/repository/VaultRepositoryImpl.kt`.
- `core/database/src/desktopTest/kotlin/com/passvault/core/database/repository/BiometricUnlockFreshnessIntegrationTest.kt`
  - 17716 bytes; source mode0644 / retained R mode0600; SHA-256 `1108e4b0d4353c840974ac0ed57331e257947e9bded200a52bc73afd3b9d3e0e`.
  - Retained after-image: `reviews/PVA-033-root-patch-v2/after/core/database/src/desktopTest/kotlin/com/passvault/core/database/repository/BiometricUnlockFreshnessIntegrationTest.kt`.
- `core/database/src/desktopTest/kotlin/com/passvault/core/database/repository/RepositorySecurityIntegrationTest.kt`
  - 96319 bytes; source mode0644 / retained R mode0600; SHA-256 `4ab2395754910b118e9f56b11044fed7f316caa6b597fcb590d10a39ac70d9df`.
  - Retained after-image: `reviews/PVA-033-root-patch-v2/after/core/database/src/desktopTest/kotlin/com/passvault/core/database/repository/RepositorySecurityIntegrationTest.kt`.
- `core/database/src/desktopTest/kotlin/com/passvault/core/database/backup/VaultBackupStreamingTest.kt`
  - 73576 bytes; source mode0644 / retained R mode0600; SHA-256 `ebf9675ec7c551c291848dc24a72a6056098d31e75d67ee31510112608dfc2d2`.
  - Retained after-image: `reviews/PVA-034-root-patch-v1/after/core/database/src/desktopTest/kotlin/com/passvault/core/database/backup/VaultBackupStreamingTest.kt`.
- `core/database/src/commonMain/kotlin/com/passvault/core/database/backup/BackupPagination.kt`
  - 4820 bytes; source mode0644 / retained R mode0600; SHA-256 `d601a0ba67bd22b0f39bc8412c4538a8fd2b0530d6a96fa83bd7f96a02aafa72`.
  - Retained after-image: `reviews/PVA-035-root-patch-v2/after/core/database/src/commonMain/kotlin/com/passvault/core/database/backup/BackupPagination.kt`.
- `core/database/src/commonMain/kotlin/com/passvault/core/database/backup/VaultBackupV2Service.kt`
  - 37596 bytes; source mode0644 / retained R mode0600; SHA-256 `aee2b621484150fe3807cec66d0a3ecd5eeff3eaf907099b839b084520a86bf4`.
  - Retained after-image: `reviews/PVA-035-root-patch-v2/after/core/database/src/commonMain/kotlin/com/passvault/core/database/backup/VaultBackupV2Service.kt`.
- `core/database/src/commonTest/kotlin/com/passvault/core/database/backup/BackupPaginationTest.kt`
  - 13328 bytes; source mode0644 / retained R mode0600; SHA-256 `4291201c78f510e359c5e63c266c8374ec60821cd50f8a904dbd4363ca2d9765`.
  - Retained after-image: `reviews/PVA-035-root-patch-v2/after/core/database/src/commonTest/kotlin/com/passvault/core/database/backup/BackupPaginationTest.kt`.
- `core/database/src/desktopTest/kotlin/com/passvault/core/database/backup/VaultBackupUnicodePaginationTest.kt`
  - 27183 bytes; source mode0644 / retained R mode0600; SHA-256 `36103de28d5bff2eaff06f6bea0aaf472983c0d8607e3383a485081974444554`.
  - Retained after-image: `reviews/PVA-035-root-patch-v2/after/core/database/src/desktopTest/kotlin/com/passvault/core/database/backup/VaultBackupUnicodePaginationTest.kt`.

See `EVIDENCE.json` for all input descriptors, source ranges/hashes, exact105-method inventory,19 unexecuted specifications, preparation reasoning and report-only accounting. `SELF-VALIDATION.json` validates this packet's consistency only; it cannot pass a product or runtime gate.
