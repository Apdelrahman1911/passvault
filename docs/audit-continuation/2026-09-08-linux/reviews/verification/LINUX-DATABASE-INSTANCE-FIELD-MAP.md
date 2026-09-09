# Database01 instance field map — checklist only, no originals or approvals

Reviewer: `/root/verification`; 2026-09-09 UTC. Requested by root after the
separately admitted controls02 execution. **DESCRIPTIVE SOURCE/DATA REVIEW ONLY.**
No REQUEST, ACCEPT, bootstrap admission, filesystem-original observation,
execution/probe, source edit or closeout authority is supplied by this document.
Root fills its actual packet only after the C3 publication checkpoint is ready;
the independent reviewer must receive/review that packet before authoring its
own actual acceptance. Do not copy checklist conditions as observed facts.

## 1. Fixed namespaces, scope and hash graph

All abbreviations below expand literally, never to a current branch/worktree:

| Name | Absolute value |
| --- | --- |
| BASE | `/root/projects/PassVault` |
| W | `/root/projects/PassVault/passvault-linux` |
| REPO | `/root/projects/PassVault/passvault` |
| RUN | W + `/docs/audit-continuation/2026-09-08-linux/reviews/linux-runner` |
| R | BASE + `/audit-runtime-linux-db-01` |
| E | W + `/docs/audit-continuation/2026-09-08-linux/runs/linux-database-01` |
| LOCK | BASE + `/.audit-coordination-linux-20260908/build.lock` |
| INNER | W + `/scripts/audit/linux_database_validation.py` |
| OUTER | RUN + `/LAUNCH_DATABASE.py` |
| METHODS | W + `/docs/audit-continuation/2026-09-08-linux/reviews/verification/METHOD-INVENTORY.json` |
| JDK | `/usr/lib/jvm/java-17-openjdk-amd64` |

Application source remains commit `9bdf9559b7a801a6f2fa49be3d7836ea6ef4d0ed`,
tree `05014e9f635131d5db06701e4013b4b5a746465a`:1198 tracked checkout files,
two explicit raw/checkout PowerShell EOL qualifications, seven classes/five
source files/**105 declarations**, only PVA-033/034/035. This is not a build of
C3/current continuation changes, Android32, a release candidate or hardware.

The exact source/data hashes were reread in this lane (`239dac`):

| Role | SHA-256 |
| --- | --- |
| `INNER` | `346e1655d273d7301258e932867d835b7897e0a1588e39dd8e83c92005cac279` |
| `RUN/PLAN.md` | `74739fd3ea58cf9dface6202e12c34c909fa2280d700bcc28a3460aff0e6690f` |
| `RUN/SOURCE.json` | `3b34d8c862d33ccda9df0d77bb4507a2504c0612a236f36d6d89a60c8a7485ce` |
| `METHODS` | `40ac2f07ea2c98d1a320e75bb59d7bbc6b3ec954efd94b349257103aaccd8979` |
| `OUTER` | `ee46cfd974de0c7241763b251c6258a5b8a058cdaa4146fa029573c21e3812c1` |
| `RUN/LAUNCH-PLAN.md` | `c5bb383165e2933a3d39da67b532613ffa71007c70e9dc38037f41e363a87a29` |
| Canonical inner BUILD | `4bd6beef8dba9b306ee5da7cfb784b3a43af010022ae9d96aaf48995c2004073` |
| Canonical inner STOP | `34c867c86cfa4228a1a9b19f3c51747fd5d261bf40006f420a2dec4bc89e4c8b` |
| Canonical inner ENV | `7881be0519b62ecd60f5aa3ef70cca3cee3cb66b455da8aeb4d167ed5144660b` |

Structured hashes mean exactly UTF-8
`json.dumps(value,sort_keys=True,separators=(',',':'),ensure_ascii=True)+'\n'`;
not shell text, pretty JSON or a sorted argv. Source-transcribed literal data
were canonicalized without importing/evaluating project code (`9da2c1`, exit0):
BUILD1205B/32 argv, STOP576B/16 argv, ENV1597B/24 keys. The argv matched recorded
controls02 **mocked** LC09/LC13 command data, and ENV hash matched their mock
launch intents. This was protocol-data reconciliation, not result adjudication,
another control execution or a Gradle invocation.

## 2. Bootstrap/originals packet root must separately supply

The inner/outer sources do **not** define a generic E-bootstrap JSON schema or
create E/LOCK. No new formal schema or guessed original tuple is invented here.
Root must supply the independently reviewed exact bootstrap command/helper and
prospective authority, followed by actual compact receipts establishing:

1. The allowlisted E allocation and any necessary previously absent parent are
   explicitly scoped; original existing ancestors are retained/bound first.
   E is exclusively created0700, opened no-follow, first identity captured,
   pathname/FD agreement and durability observed, empty at handoff. Any actual
   bootstrap failure/cancellation/partial namespace is retained honestly, not
   normalized, re-adopted, relabeled or silently retried.
2. Bootstrap evidence/request/approvals live **outside E**. Do not precreate R,
   checkout or the33 cleanup targets to obtain pins. Their35 original allocation
   witnesses must come from the inner runner's durable exclusive lifecycle.
3. Reuse only the established **original Linux** coordination parent/LOCK,
   revalidated against its creation/admission history. LOCK must remain an
   owned0600 single-link regular file, opened without replacement or chmod.
   macOS lock/PID/device identities and consumed controls scratch are not
   database authority. A current pathname alone does not establish originality.
4. Seal all required original absolute directory tuples
   `{dev,ino,uid,mode}`: ancestry from `/`; immutable/control/approval parents;
   RUN and METHODS parents; E and its ancestry; LOCK parent; REPO and **every
   parent of all1198 tracked source files**; JDK binary/release, Git and resolved
   Python parents. The inner `Directories.bind` checks the complete declared
   map against opened originals, not only a selected subset. Outer needed
   parents must also be present. Do not include nonexistent future R objects.
5. Seal actual regular-file tuples
   `{dev,ino,uid,mode,nlink,size,mtime_ns,ctime_ns}` after publication/freeze.
   Input bytes/metadata must remain stable, single-link, bounded and not
   group/world writable; outer reads additionally require current UID ownership.
   Do not make regular-file device equal to directory device by assumption.
6. Bind the actual four toolchain files below by `{path,sha256,identity}`.
   Ordinary installed toolchain trust is not full transitive provenance. These
   are actual original byte/metadata reads, not inferred versions or invented
   source hashes. No version, JDK, Gradle or native probe is performed here.
7. Root records fresh no-audit-local/CI-overlap, producer/source/Git-store freeze,
   disk/RAM observations and adequate external invocation/cleanup time. Neither
   this checklist nor a prior controls slot observation attests future quiescence.
8. Original R absence, E emptiness and absence of all four outer outputs and
   `RUN/LAUNCHER.json` must be checked again by the actual admitted launcher.
   No current absence/mode/identity is claimed by this source-only lane.

**Chronology reconciliation:** the frozen inner PLAN's historical sentence
about writing an external receipt "before invoking" cannot be implemented as
precreating `RUN/LAUNCHER.json` with future exit facts. The accepted newer outer
PLAN/source requires that path absent, and root writes its completed receipt
**only after actual tool exit**. A separate prelaunch bootstrap/intent record
outside E is not that completed receipt and must not occupy the four outputs.
The inner PLAN is preserved, not silently edited or rehashed.

## 3. `RUN/REQUEST.json` — exactly the required field relationships

Root authors the actual request. The following is a field map, **not a filled
request**. All prospective time, inode, resource, execution and coordination
facts remain to be supplied and independently reviewed.

| Field(s) | Required value/relationship |
| --- | --- |
| `format`, `author`, `run_id` | `passvault-linux-database-request-v1`, `/root`, `linux-database-01` |
| `commit`, `tree` | Exact handoff commit/tree above |
| `repository`, `runtime`, `evidence`, `lock` | Expanded REPO, R, E, LOCK |
| `findings` | Ordered `["PVA-033","PVA-034","PVA-035"]` |
| `command`, `stop_command`, `environment`, `limits` | Literal inner BUILD/STOP/ENV/LIMITS below; no overrides |
| `no_other_audit_local_or_ci_job` | Actual truthful boolean `true` |
| `runtime_closeout` | `RETAIN_ALL_REVIEW_IMMEDIATE_ROOT_CLOSEOUT` |
| `created_unix_seconds`, `expires_unix_seconds` | Actual prospective finite interval, `0 < expires-created <= 3600`, current at admission/under-lock/before mutation; not copied from controls02 |
| `bindings` | Exactly seven keys: `runner_sha256`, `plan_sha256`, `source_sha256`, `methods_sha256`, `command_sha256`, `stop_command_sha256`, `environment_sha256`, with values from section1 |
| `directory_pins` | The full original-directory map described above |
| `input_pins` | Exactly four absolute keys: INNER, RUN/PLAN.md, RUN/SOURCE.json, METHODS; values are their actual original regular tuples |
| `lock_pin` | Actual unchanged original LOCK regular tuple |
| `toolchain` | Exactly `java`, `java_release`, `git`, `python`; each `{path,sha256,identity}` |
| `outer_launcher` | The exact additive object in section5, not extra keys inside seven-field `bindings` |

Toolchain paths: `java=JDK/bin/java`, `java_release=JDK/release`,
`git=/usr/bin/git`, `python=/usr/bin/python3.12` (the actual resolved executable
for both fixed Python entries). Files must not be set-ID; release bytes must
contain `JAVA_VERSION="17.` and `OS_ARCH="x86_64"`. The SDK remains
`/opt/android-sdk`, not an extra fifth toolchain entry or a readonly mount claim.

REQUEST and each acceptance are captured once and compared under lock, avoiding
circular REQUEST/self-acceptance pins. The whole final REQUEST SHA includes its
outer object, all current pins and its actual time interval. Changing any of
those invalidates the later acceptance/request hash; root must not patch a
filled request after this reviewer accepts it.

## 4. Exact inner command, environment and limits

BUILD is `[R/checkout/gradlew, ":core:database:desktopTest"]`, followed by one
`["--tests", FQCN]` pair for each class in this **ordered** table, then
`["--rerun-tasks","--stacktrace"]`, then FLAGS below. STOP is exactly
`[R/checkout/gradlew,"--stop"] + FLAGS` and has no test/rerun/stacktrace additions.

| FQCN | Declared methods |
| --- | ---: |
| `com.passvault.core.database.backup.BackupPaginationTest` | 13 |
| `com.passvault.core.database.backup.VaultBackupUnicodePaginationTest` | 6 |
| `com.passvault.core.database.repository.BiometricUnlockFreshnessIntegrationTest` | 12 |
| `com.passvault.core.database.repository.RepositorySecurityIntegrationTest` | 17 |
| `com.passvault.core.database.backup.VaultBackupStreamingTest` | 39 |
| `com.passvault.core.database.repository.VaultUnlockPreemptionIntegrationTest` | 7 |
| `com.passvault.core.database.repository.RepositoryBiometricSecurityIntegrationTest` | 11 |

FLAGS has14 elements, ordered:

```text
--no-daemon
--max-workers=1
--console=plain
--no-parallel
--no-configure-on-demand
--no-configuration-cache
--no-build-cache
--dependency-verification=strict
-Pkotlin.compiler.execution.strategy=in-process
-Pandroid.builder.sdkDownload=false
-Dorg.gradle.jvmargs=-Xmx2g -Dfile.encoding=UTF-8
-Dorg.gradle.java.installations.auto-download=false
-Dorg.gradle.java.installations.auto-detect=false
-Dorg.gradle.java.installations.paths=/usr/lib/jvm/java-17-openjdk-amd64
```

The `org.gradle.jvmargs` line is **one argv element containing a space**.

ENV is the following exact24-key map, expanding R/JDK literally:

| Key(s) | Value |
| --- | --- |
| `PATH`, `JAVA_HOME` | JDK+`/bin:/usr/bin:/bin`, JDK |
| `LANG`, `LC_ALL`, `TZ` | `C.UTF-8`, `C.UTF-8`, `UTC` |
| `HOME`, `GRADLE_USER_HOME`, `KONAN_DATA_DIR` | R+`/home`, R+`/gradle-home`, R+`/konan` |
| `XDG_CACHE_HOME`, `XDG_CONFIG_HOME`, `XDG_DATA_HOME`, `XDG_STATE_HOME` | R+`/xdg-cache`, R+`/xdg-config`, R+`/xdg-data`, R+`/xdg-state` respectively |
| `ANDROID_USER_HOME` | R+`/android-user` |
| `ANDROID_HOME`, `ANDROID_SDK_ROOT` | `/opt/android-sdk` for both |
| `TMPDIR`, `TMP`, `TEMP` | R+`/tmp` for all three |
| `SQLITE_TMPDIR` | R+`/sqlite` |
| `JAVA_TOOL_OPTIONS` | Exactly the single-space-joined seven options below |
| `GIT_CONFIG_NOSYSTEM`, `GIT_CONFIG_GLOBAL`, `GIT_TERMINAL_PROMPT`, `GIT_OPTIONAL_LOCKS` | `1`, `/dev/null`, `0`, `0` as strings |

`JAVA_TOOL_OPTIONS` is the one string composed, in order, from `-Xmx512m`,
`-XX:-UsePerfData`, `-Dfile.encoding=UTF-8`, `-Duser.home=`+R+`/home`,
`-Djava.io.tmpdir=`+R+`/tmp`, `-Djna.tmpdir=`+R+`/jna`,
`-Dorg.sqlite.tmpdir=`+R+`/sqlite`. No ambient environment merge or extra flags.

Inner LIMITS is exactly20 keys:

| Keys | Literal numeric values, same order |
| --- | --- |
| `launch_free_bytes`, `running_free_bytes` | 12884901888, 8589934592 |
| `launch_memory_fraction`, `running_memory_fraction` | 0.25, 0.20 |
| `build_seconds`, `stop_seconds`, `git_seconds` | 3600, 600, 180 |
| `settlement_seconds`, `total_seconds` | 120, 4800 |
| `log_bytes_per_command`, `journal_bytes` | 4194304, 8388608 |
| `xml_bytes_per_file`, `xml_total_bytes` | 2097152, 16777216 |
| `runtime_logical_bytes`, `runtime_files`, `runtime_directories` | 6442450944, 200000, 30000 |
| `resource_poll_seconds`, `inventory_poll_seconds` | 5, 30 |
| `preadmission_source_seconds`, `preadmission_source_bytes` | 120, 268435456 |

These heaps/deadlines/inventories are the qualified source contract, not total
RSS, a filesystem quota, actual task-graph/native-loader evidence or a hard
whole-program4800s timeout. Fixed flags, SDK/JDK trust, SQLite fallback-only temp
proof and non-authoritative installations.paths hint remain as in M03 packet
`0ce080b824c1ac14684cf6e6fa3529f8c159f8b668a29be70656c308d8df045b` and
v3 review `2754aca02b5223f0ea8cebaabbe0a676b42abd74c1865a38f9c2051f8714d224`.

## 5. Exact outer addition — same REQUEST, different argv/ENV/PLAN scopes

`REQUEST.outer_launcher` has exactly these15 keys/relationships:

| Field | Required value |
| --- | --- |
| `format`, `author`, `purpose` | `passvault-linux-database-outer-v1`, `/root`, `ONE_ORIGINAL_LINUX_DATABASE_OUTER_SUPERVISOR` |
| `launcher_sha256`, `plan_sha256` | OUTER hash ee46… and RUN/LAUNCH-PLAN.md hash c5bb… from section1 |
| `input_pins` | Exactly OUTER and RUN/LAUNCH-PLAN.md absolute paths, with actual regular tuples |
| `argv` | `["/usr/bin/python3","-I","-B",OUTER]` |
| `runner_argv` | `["/usr/bin/python3","-I","-B",INNER]` |
| `child_environment` | Exactly `PATH=/usr/bin:/bin`, `LANG=C.UTF-8`, `LC_ALL=C.UTF-8`, `TZ=UTC` |
| `limits` | Outer14-key LIMITS below, not inner20-key LIMITS |
| `evidence_paths` | Ordered RUN/LAUNCHER-stdout.log, RUN/LAUNCHER-stderr.log, RUN/LAUNCHER-resources.json, RUN/LAUNCHER-processes.json |
| `slot_owner` | `/root` |
| `no_other_audit_local_or_ci_job`, `source_and_inputs_frozen` | Both actual truthful boolean `true` |
| `limits_acknowledged` | Exact ordered outer ACK list below |

Outer LIMITS:
`bootstrap_seconds=120`, `outer_soft_seconds=6000`, `cleanup_grace_seconds=900`,
`kill_wait_seconds=60`, `pipe_after_exit_seconds=30`, `postcheck_seconds=120`,
`sample_seconds=5`, `file_bytes=1048576`, `input_bytes=4194304`,
`python_bytes=33554432`, `launch_free_bytes=12884901888`,
`running_free_bytes=8589934592`, `launch_memory_fraction=0.25`,
`running_memory_fraction=0.20`.

Outer ACK, ordered literally:

```text
ONE_ORIGINAL_DIRECT_CHILD_NO_DESCENDANT_AUTHORITY
RUNNER_OWNS_ORIGINAL_STOP_NO_PARENT_STOP_OR_RETRY
COOPERATIVE_SLOT_AND_SOURCE_FREEZE_NOT_HOST_SANDBOX
POINT_SAMPLED_RESOURCES_AND_SOFT_SYSCALL_DEADLINES
FORCED_INTERRUPTED_OR_UNSETTLED_EXIT_IS_HOLD
ROOT_TOOL_EXIT_AND_SEPARATE_CLOSEOUT_REQUIRED
```

Root still supplies the separately reviewed external whole-tool time/cleanup
envelope: at least6000+900+60+30+120=7110s plus setup/receipt margin and honest
interrupt/blocked-syscall handling. This checklist does not implement a timer
or admit a timeout utility, parent/group kill, alternative launcher or retry.

## 6. Actual `RUN/ACCEPT-root.json` and `RUN/ACCEPT-verification.json`

Root authors **only its own** actual approval. Verification authors its own only
after independent source/filled-instance review; no guessed future ACCEPT here.

| Field | Exact required relationship |
| --- | --- |
| `format` | `passvault-linux-database-acceptance-v1` |
| `reviewer` | `/root` or `/root/verification`, respectively |
| `runner_authors` | Ordered `["/root/storage","/root"]` |
| `review_role` | Root `OWNER_COAUTHOR_APPROVAL`; verification `INDEPENDENT_SOURCE_REVIEW` |
| `purpose`, `disposition` | `ONE_SHOT_LINUX_DATABASE_EXECUTION_AND_RETAINED_CLOSEOUT_CONTRACT`, actual `ACCEPT` |
| `bindings` | Exactly seven REQUEST values plus actual whole `request_sha256` = eight keys |
| `commit`, `tree`, `runtime`, `evidence` | Fixed handoff and R/E values |
| `obligations` | Exactly F01–F07, each `ACCEPT` only after actual role-appropriate review |
| `limits_acknowledged` | Ordered inner ACK list below |
| `outer_launcher` | The distinct approval object below |

Inner ACK, ordered literally:

```text
COOPERATIVE_NAMESPACE_NOT_HOSTILE_UID_SANDBOX
POINT_SAMPLED_PROCESS_RESOURCE_EVIDENCE
NO_INDEPENDENT_SEMANTIC_PASS_FROM_RUNNER
RETAIN_ALL_IMMEDIATE_SEPARATE_ROOT_CLOSEOUT
NO_RETRY_ON_FAILURE_OR_AMBIGUITY
HARDWARE_AND_CLOSED_SCOPES_UNCHANGED
```

Each approval's `outer_launcher` is exactly: `purpose` above for outer;
`launcher_authors=["/root/storage"]`; `design_contributors=["/root"]`;
`review_role=OWNER_EXECUTION_APPROVAL` for root or `INDEPENDENT_SOURCE_REVIEW`
for verification; actual `disposition=ACCEPT`; actual `launcher_sha256`,
`plan_sha256`, whole `request_sha256`; identical ordered outer
`limits_acknowledged`. It has no request-style input_pins/argv/limits additions.

The approval's top-level plan hash is747…; nested outer plan hash is c5bb….
The whole request hash appears in both appropriate approval locations. Neither
approval includes its own hash among these eight bindings. Literal
`accept_root_sha256` / `accept_verification_sha256` are added only to the later
external receipt's ten bindings. JSON role strings alone do not prove authorship.

## 7. Remaining gates — do not manufacture completed readiness

| Gate | Evidence already available / still needed |
| --- | --- |
| Publication/source freeze | Root requested C3 checkpoint first. Its completion/identity is not asserted by this lane; keep exact handoff build source and current helper pins separate. |
| Meaningful runner controls | Controls01 failure stays20 PASS/2 FAIL, including the earlier reviewer miss. Controls02 exact-v4 ran once:22 PASS/0 FAIL/0 unstarted; independent editor_review result8284… accepted all213 records and original synthetic cleanup/descriptor settlement. This author does not independently approve its own controls. Real child processes0, real signals0, application cases0; direct-child cases reach only their declared mock boundary, not waitpid/pidfd proof. No database admission follows. |
| F01 failure/terminal/grace | Reviewed inner346e + outeree46/c5bb source, but actual external envelope, exact packet and eventual terminal/tool settlement are still needed. |
| F02 original identity/admission | SOURCE3b34 data accepted; actual original E/bootstrap, complete directory/toolchain/input/lock seals, prospective REQUEST and unchanged under-lock images still require filled-instance review. |
| F03 prompt resource closeout | Independent prebuild closer source/design acceptance exists (below), not executed closeout or postrun admission. Reserve root/reviewer time and account for partial-allocation/HOLD retention; no whole-runtime deletion. |
| F04 input/lifecycle controls | Subject346e controls02 actual independent result/cleanup acceptance8284… is available within its declared real-IO/mocked-process limits. Preserve controls01 failure and distinct correction; neither the controls result nor source hashes supply the actual database instance, outer run or separate closer controls. |
| F05 ownership/coordination | Root's fresh sole audit-local/CI slot, team/source/Git-store freeze, current originals/resources and separately admitted external handling are required. No new process authority was observed here. |
| F06 actual independent approval | Final root REQUEST/root approval and this reviewer's actual eight-binding+outer approval are not supplied/filled by this checklist. Root coauthorship is not a second independent vote. |
| F07 semantic scope |105 fixed names/classnames and seven exact nonempty XMLs/no failure/error/skip must be reconciled after execution; file/task count, mocks or historical overlaps are not105 passes. Preserve PVA033 final-write INTERNAL_ERROR vs other VAULT_LOCKED, PVA034 accept-only oracle and PVA035 finite real-Room/provider/schema limits. |

Controls02 data bindings read without replay: journal
`28f806393e0adacfdcd13913b57b2639aa0ae57010347fa67370aae1dd3aa863`,
external receipt
`88ee8b5264e3924aca77e392b3f69c9ad68a67b1a7cc7b9227ece35135a402b5`,
tool `2f594a`, exit0. Independent result
`reviews/editor-independent/CONTROL-RESULT-02-REVIEW.json`, SHA-256
`8284e904b16f0b4bb87f05dee4cd7fe59f2b850dcd562fc4dafc5752547e7f62`,
reconciles all22 descriptors' HOLD=false,51 original deletion pins, two rename
projections, seven source pins,17 ancestors and original coordination/approval/
receipt relationships. C-FIXTURE-01 is validated only at its declared mocked
direct-child boundary. Tool elapsed0.215379499s versus journal0.276773090s is
retained as a timing discrepancy: neither is complete external monotonic/
hard-duration or host-wide proof. The independently completed controls slot may
be released by root; it is not a reusable execution admission.

## 8. Closeout contract retained, not prefilled from future results

Current helper b16774ff6d0c14ece40dbe2c75684a2b07e7c90c9da4373ec0a6e12e87cc7bec,
PLAN cffc69357a0c4f0c5e46fd4d282439ece513eb2f166074c05f2ab4e679f8a57d, and
baseline_coverage source-acceptance
`c47bb8c9d39d32ab21d9f7d288c2239aafc847fe260e40bbee0e391fbcd6a58e`
establish only accepted prebuild proposal source/design. It preserves
unexecuted closer inert effects, unfilled actual C01–C07 and a separately
reviewed bounded external **closeout** launcher still needed. Runner controls
do not exercise this different helper.

The independent reviewer clarified the frozen gate timing in
`reviews/baseline-coverage/LINUX-CLOSEOUT-GATE-TIMING-CLARIFICATION.json`, SHA-256
`13679c9cf79ea2cebbbb9125d8f348eca6e6dcbf2c438654852de4565b881950`,
with MD SHA-256
`90487de217b5bb07ea8d275276f076995a1e1ae379eb480ecc1bd5158837cd88`.
This reviewer read/agrees with its timing distinction and qualifications:
accepted prompt-closeout design/source is explicitly **before build**;
mandatory closer-specific LC-C05/06 controls are **before closer invocation /
actual C-instance acceptance**, not a retroactively invented historical
before-build test condition. They are unexecuted here and not waived or supplied
by runner controls02. Completing them before build is recommended to avoid
stranding generated outputs; root has assigned that preparation separately.
Any deferral still requires an independently accepted viable prompt-closeout
schedule with reserved root/reviewer attention and resources. If it cannot
meet prompt cleanup discipline, actual F readiness is **HOLD**. Both actual
postrun C01–C07 approvals still require original run evidence; neither this
clarification nor prebuild source acceptance fills them.

Postrun root must supply actual `RUN/LAUNCHER.json` with observed tool/runner
exit, enclosing times, truthful completion/no-timeout/no-interruption/sole-slot,
ten original binding values and four original output hashes/identities. For
this outer implementation, outer0 may mean collected runner1; outer1 is HOLD,
including late descriptor-close failure after a preterminal0. Neither is
application PASS or deletion authority by itself.

Then separately build the fixed closer's actual TARGETS/EVIDENCE/REQUEST from
the complete original run:35 original allocation witnesses and33 allowed tops,
matching original directory/parent pins; all1198 source bytes and two EOL
qualifications; complete sequential journal with terminal, preterminal RESULT,
logs and explained XML inventory. A committed/ambiguous Gradle launch requires
its original one-shot complete successful stop and settled workers. The
alternative no-Gradle branch requires complete positive no-launch/stop-false
proof, full35 allocation and source-before evidence, not silence in a partial
journal. No failed stop retry or missing-original adoption is permitted.

Closer REQUEST has its existing15 input bindings and exact input/directory/lock/
separate-device policy, plus actual postrun root and baseline_coverage C01–C07
approvals; do not fold these into the database request or fill future originals
now. Fresh conservative process/mount/resource gates and original flock/producer
freeze precede any deletion. Preserve R, checkout, `.git`, source, permanent
tests, schemas, all reports/test-results+ancestors, E and all evidence. The only
eligible tops are11 private cache/tmp roots and22 preallocated generated roots;
partial allocation, drift, ambiguous stop/settlement or incomplete evidence is
HOLD, not automatic cleanup or another namespace.

No database, bootstrap or closeout was executed in this lane. Only bounded
source/data reads, protocol-data hashing and this permanent field map were
produced; no build/cache/temp runtime, worker, daemon, emulator or project import.
No runtime/process/filesystem-original probe was performed. Data-read FDs were
closed; wrapper stop is NOT_APPLICABLE_NO_WRAPPER_LAUNCH. All STOP/NO-RETRY/CLOSED,
PVD, hardware, Windows failure/cleanup-HOLD and publication restrictions remain.
Closure delta0;19/25 original,22/37 all confirmed and2/12 suspicions remain
separate denominators, not readiness percentages.
