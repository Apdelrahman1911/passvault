# Fixed Linux database validation — source proposal, not admission

Source author: `/root/storage`; coauthor/design contributor: `/root`.
Owner/coauthor approval: `/root`; independent source review: `/root/verification`.
These are **one independent review and one coauthor approval**, not two
non-author votes. Root alone executes. No runner import, syntax check, inert test, JVM, Git clone,
Gradle or application test was executed by the author while writing this proposal.
Historical runners/helpers, locks, runtimes and the nineteen inert specifications
remain unadmitted/inert. This new file does not reopen any CLOSED scope.

## Purpose and source

One Linux x86_64 synthetic host regression for **PVA-033/034/035**, not a build
of the ongoing continuation changes or a replacement release candidate.

- Preserved input repository: `/root/projects/PassVault/passvault`.
- Immutable commit: `9bdf9559b7a801a6f2fa49be3d7836ea6ef4d0ed`.
- Immutable tree: `05014e9f635131d5db06701e4013b4b5a746465a`.
- Full actual-checkout manifest: **1198 tracked regular files**, Git blob/mode,
  expected checkout SHA-256 and byte size. This is not the 811-member raw G12
  manifest; retain the **two PowerShell checkout-EOL qualifications** explicitly.
- Disposable explicit `--local --no-hardlinks` clone inside the fresh runtime. Fixed
  Git configuration disables hooks, fsmonitor, automatic maintenance, global
  and system configuration; empty template, detached exact checkpoint. No
  submodules, remote credential use, protected-branch change or tag movement.
  This avoids an upload-pack server whose cwd could enter the preserved source
  repository outside R; no outside-R worker signal exception is introduced.
  Local object copying still has Git's concurrent-repository-change caveat:
  the preserved Git store must remain frozen/coordinated; full subsequent
  commit/tree/member-byte checks reject drift. A copy failure is not retried.
- Selected task only: `:core:database:desktopTest`. Seven ordered exact class
  filters are fixed in the runner and in the independently authored inventory:
  `../verification/METHOD-INVENTORY.json`, SHA-256
  `40ac2f07ea2c98d1a320e75bb59d7bbc6b3ec954efd94b349257103aaccd8979`.
  **Seven classes / five source files / 105 declared methods** (13,6,12,17,39,7,11).
- Suite names are simple class names plus literal `[desktop]`; testcase names
  are the frozen literal method names plus `[desktop]`; testcase classnames are
  exact undecorated FQCNs. No aliases, suffix stripping or post-result normalization.

The real boundaries are Room/BundledSQLiteDriver/Desktop crypto/production
repositories and backup services with synthetic storage. OS authenticator,
physical device, provider, GUI, memory-pressure and historical schema migration
coverage is not supplied. PVA-034 concerns a discriminating test oracle. Required
independent semantic adjudication remains separate from the runner's mechanical
XML mapping, process settlement and exit status.

## Fresh, single-use namespace

- Runtime `R`: `/root/projects/PassVault/audit-runtime-linux-db-01` — **must be absent**.
- Evidence `E`: continuation `runs/linux-database-01` — root precreates/seals it
  **0700, empty, original identity** before requesting acceptance.
- Coordination lock: `/root/projects/PassVault/.audit-coordination-linux-20260908/build.lock`.
  Root separately precreates/seals the parent and regular **0600 single-link**
  lock. The runner opens the original only, nonblocking exclusive flock; it
  does not create, replace, adopt or normalize the lock.
- Root attests that no other audit-owned local or CI job overlaps this run.
  Relevant current processes are also conservatively sampled. Advisory flock
  and process snapshots are not a host-wide resource lease or kernel sandbox.
- All roots/ancestors needed to open input/evidence/lock/repository/toolchain
  paths have original directory tuples recorded before execution. Directory
  tuples are `dev,ino,uid,mode`; changing directory size/mtime/link counts are
  deliberately not mistaken for replacement. Regular input tuples additionally
  bind `nlink,size,mtime_ns,ctime_ns`. There is **no invented same-device equality
  between regular files and directories** on this host's virtual filesystem.
- After complete fresh checkout verification and before Gradle, preallocate
  exactly22 empty0700 generated-output roots through the same durable
  allocation_intent/allocation_original sequence: `.gradle`, `.kotlin`, `build`
  and the19 explicitly included module `build` roots listed in runner
  `GENERATED_ROOTS`. All are required absent, with no1198-manifest tracked
  equal/descendant-prefix collision. These originals, not a later current inode,
  can support separately reviewed closeout. Unknown outputs stay retained.

Root writes an external launcher receipt before invoking the runner, including
source/request/review hashes, exact entry argv, start/exit, bounded stdout/stderr,
disk/RAM/process observations and no-CI-overlap attestation. It must live outside
the empty E. The pre-journal rejection path writes stderr only; it cannot prove
its own launcher receipt. Partial allocation remains consumed/HOLD, never reuse.

## Normative inputs and actual acceptance

All control files are in this PLAN's directory. The runner has **no CLI options**;
the only prospective entry is absolute `/usr/bin/python3 -I -B <absolute runner>`.
Do not invoke until actual new owner/coauthor approval, independent acceptance and separate execution
admission exist. **Meaningful exact-source inert negative/lifecycle controls
are mandatory before operational execution**, including stable-reader mutation
schedules required by RUNNER_READINESS. Their current execution count is zero.
They require their own independently reviewed, bounded root admission; only
that new exact-source harness may import this successor as explicitly admitted.
No old runner may be imported, and no test/import occurs automatically. Root
must also obtain independent acceptance of the prompt closeout design **before**
the build; actual post-run ownership/outcome inputs are bound later.

### `SOURCE.json` (root-authored)

`format=passvault-linux-checkout-source-v1`, exact `commit`/`tree`, `files` (1198
unique entries): `{path,git_mode,git_blob,checkout_sha256,checkout_size}`.
`git_mode` is `100644` or `100755`. `checkout_eol_qualifications` has the two
explicit affected paths and their raw-vs-checkout evidence. The immutable source
manifest hash is bound in the request and both acceptances. All 1198 preserved
checkout files are stable-read under the original lock before the first journal
or runtime mutation, using root-sealed original source-directory pins:120s and
256MiB read envelope, launch-resource checks with a five-second polling target.
The two expected PowerShell checkout forms must be actual declared CRLF bytes,
not an invented809+2 raw-byte equality. Git identity plus
the complete actual checkout file hashes/executable bits is checked before and
after the attempted run; HEAD alone is insufficient. Post-failure read-only
source checks retain the original expected hashes rather than rebasing them.

### `REQUEST.json` (root-authored)

Required fields:

- `format=passvault-linux-database-request-v1`, `author=/root`,
  `run_id=linux-database-01`, exact `commit`, `tree`, `repository`, `runtime`,
  `evidence`, `lock` and `findings=[PVA-033,PVA-034,PVA-035]`.
- `command`, `stop_command`, `environment`, `limits`: **literal equality** to
  runner constants BUILD/STOP/ENV/LIMITS; no arbitrary task/property/path override.
- `no_other_audit_local_or_ci_job=true`,
  `runtime_closeout=RETAIN_ALL_REVIEW_IMMEDIATE_ROOT_CLOSEOUT`.
- `created_unix_seconds`, `expires_unix_seconds`: prospective current validity
  interval no longer than one hour; rechecked under lock before first mutation.
- `bindings`: SHA-256 keys `runner_sha256`, `plan_sha256`, `source_sha256`,
  `methods_sha256`, `command_sha256`, `stop_command_sha256`, `environment_sha256`.
  File hashes are literal bytes. Structured hashes use UTF-8
  `json.dumps(value,sort_keys=True,separators=(',',':'),ensure_ascii=True)+'\n'`.
- `directory_pins`: exact map of all required original absolute directories,
  including complete ancestry from `/`, all immutable input parents, E, lock
  parent, preserved repository root **and every tracked source parent**, and
  JDK/interpreter/Git parents. No missing
  original is silently filled after admission. Newly allocated runtime originals
  are added only after durable allocation intent and exclusive mkdir/registration.
- `input_pins`: exact original regular-file tuples for runner, PLAN, SOURCE and
  METHODS, keyed by absolute path. REQUEST and the two review files are captured
  once before flock and rechecked against those same captured bytes/tuples under
  lock (avoids circular self-hash/identity fields).
- `lock_pin`: original regular lock tuple.
- `toolchain`: exact `java`, `java_release`, `git`, `python` entries with
  `{path,sha256,identity}`. Java paths are the fixed JDK17 binary/release; Git is
  `/usr/bin/git`; Python is the resolved original executable used for the fixed
  entry. Toolchain files are regular/non-set-id, stable-read, byte/identity bound
  and revalidated under lock; release identity must be JDK17 x86_64.

The actual source/graph/configuration/native-loader review remains necessary;
toolchain hashes and a JVM task filter alone are not that review. Root must bind
the build-config reviewer packet and its limits in the accepting source reports.

### `ACCEPT-root.json` and `ACCEPT-verification.json` (actual approver-authored)

Each **actual normative document** must contain:

- `format=passvault-linux-database-acceptance-v1`, `reviewer` exactly `/root` or
  `/root/verification` respectively, `runner_authors=[/root/storage,/root]`.
  `review_role` is respectively `OWNER_COAUTHOR_APPROVAL` or
  `INDEPENDENT_SOURCE_REVIEW`. The independent reviewer must not be a listed
  author; root's actual coauthor approval cannot count as an independent vote.
- `purpose=ONE_SHOT_LINUX_DATABASE_EXECUTION_AND_RETAINED_CLOSEOUT_CONTRACT`,
  `disposition=ACCEPT`. A HOLD, REVISE, old mapping report, arbitrary hashed
  report or root-labeled acceptance is not accepted.
- `bindings`: the seven REQUEST binding values **plus** `request_sha256`
  (eight values total).
- Exact `commit`, `tree`, `runtime`, `evidence`.
- `obligations`: each `F01` through `F07` exactly `ACCEPT`, backed by the
  approver's actual role-appropriate challenge/report. These fields do
  not authenticate a human/agent cryptographically; root retains authorship
  provenance through the shared cooperative freeze and collaboration receipts.
- `limits_acknowledged`, ordered literally:
  `COOPERATIVE_NAMESPACE_NOT_HOSTILE_UID_SANDBOX`,
  `POINT_SAMPLED_PROCESS_RESOURCE_EVIDENCE`,
  `NO_INDEPENDENT_SEMANTIC_PASS_FROM_RUNNER`,
  `RETAIN_ALL_IMMEDIATE_SEPARATE_ROOT_CLOSEOUT`,
  `NO_RETRY_ON_FAILURE_OR_AMBIGUITY`,
  `HARDWARE_AND_CLOSED_SCOPES_UNCHANGED`.

F01–F07 are operational runner review obligations, not new application findings
or closure credit. Both normative approvals must address the final exact bytes; changing
runner/PLAN/source/method mapping/request invalidates their binding.

## Environment, commands and resource envelope

- Fixed JDK17 `/usr/lib/jvm/java-17-openjdk-amd64`; SDK `/opt/android-sdk` is
  intended as installed configuration input, not device/emulator execution
  authority or an OS-enforced read-only mount. Exact AGP9.4 source establishes
  `-Pandroid.builder.sdkDownload=false` as the specific automatic-installation
  prohibition; it is not proof that arbitrary build code cannot write the SDK.
- Checked-in wrapper; one worker; Kotlin compiler in process; no daemon,
  parallelism, configure-on-demand, configuration cache or build-cache reuse.
  Dependency verification **STRICT**; no init scripts, dependency/version,
  application identity, release/store number or protection changes.
- Exact generated environment, not `os.environ.copy()`: no ambient credentials,
  `_JAVA_OPTIONS`, `JDK_JAVA_OPTIONS`, `JAVA_OPTS`, `GRADLE_OPTS`, signing,
  Git configuration or arbitrary toolchain options. Private 0700 roots confine
  HOME, Gradle, Konan, XDG, Android user state, TMP/TEMP, JVM user.home/tmp,
  JNA tmp and synthetic SQLite temp. JAVA_TOOL_OPTIONS is inherited by forked
  Test JVMs rather than merely setting daemon properties.
- Inherited `-Xmx512m` is the intended default Test JVM heap; explicit daemon
  `-Xmx2g` remains in the exact Gradle JVM argument. OpenJDK17 source option
  ordering was independently reviewed by build_config; **not a total RSS,
  native allocation, cgroup or hostile-config hard cap**. Positively owned
  process argv is retained without attaching or probing unrelated processes;
  any later explicit Test worker `-Xmx` must be reconciled independently.
- `org.sqlite.tmpdir` is supplementary, **not used by the reviewed SQLite2.6.2
  JVM loader**. The newly captured exact official JVM source uses
  `Files.createTempFile`/`java.io.tmpdir` for its default resource-extraction
  fallback; earlier packets lacking the JVM source did not establish this.
  It first tries System.loadLibrary, explicit androidx.sqlite path/name options
  and java.home/lib. Root explicitly accepts normal installed JDK/SDK/system-
  library trust for this host selection, **not** hermetic/native-binary
  provenance or an OS-read-only SDK. This is not proof that a given runtime
  loaded the bundled fallback. No speculative library-search flag or toolchain
  substitution is silently added; package/provenance gates remain BLOCKED.
  JNA gets explicit fresh `jna.tmpdir` before initialization; extracted library
  permissive leaf modes remain beneath private ancestors.
- Exact official Gradle9.7.1 docs support the two `-D` toolchain auto-detection/
  auto-download prohibitions. The `-D...installations.paths` argument is only
  a non-authoritative fixed hint: exact CLI property plumbing was not proven
  by that read. Fixed JAVA_HOME/JDK binary plus observed owned argv are primary;
  do not infer a toolchain supplier/runtime result from the hint.
- Launch disk **12 GiB** and available-memory **25%**; ongoing disk **8 GiB**
  and memory **20%**. Check every relevant writable volume (runtime/evidence
  parents), ongoing samples with a five-second target in the active loop.
  Synchronous filesystem/proc/source inventory I/O can delay that target.
  One build max3600s, one stop max600s, Git each180s, settlement120s,
  work soft limit4800s monitored in command loops. Cleanup/read-only evidence
  operations keep their separate budgets after that soft limit; this is **not
  a hard4800s whole-program deadline**. Before admission root must bind an outer
  launcher duration and adequate cleanup grace, preserving interruption/HOLD
  when hard interruption prevents the sole stop/evidence/settlement sequence.
  Polling is not an OS-enforced quota, and syscalls/Popen can outlast loop checks.
- Each child pipe is drained through a **4 MiB hard retained-log cap**; excess
  is discarded with sticky failure and owned cancellation. Journal8MiB.
  XML2MiB/file,16MiB total; preserve up to32 observed TEST XMLs on error, exact
  seven mandatory files required for a successful mechanical result. Runtime
  inventory cap6GiB logical bytes,200000 files,30000 directories; periodic
  inventory plus free-space floors, not a hard filesystem allocation limit.

## Failure, cancellation, ownership and terminal semantics

Cleanup handling and signal latches exist before any child. Exact command/log
intent plus **original stop obligation is durable before the Gradle fork gap**.
Signals are blocked around launch intent/commitment; already pending
cancellation refuses the launch. Cancellation concurrent with the defined
commitment causes owned termination/retention, not retry or forgotten obligation.
Child signals are explicitly unmasked before exec. SIGHUP/SIGTERM/SIGINT are
latched monotonically. SIGKILL, host loss or interpreter crash can leave a
consumed/partial runtime and outstanding stop; the journal is evidence, **not
permission to replay/recover automatically**.

The one original-wrapper `--stop` uses the original environment and600s budget
after any committed/ambiguous Gradle launch. Failed, timed-out, unstarted or
ambiguous stop is outstanding; **no automatic retry or replacement namespace**.
If original wrapper/parents cannot be reverified, stop cannot be safely launched
and remains outstanding. Independently safe evidence/source checks and bounded
owned settlement are still attempted; no failed stage silently grants success.

Linux `/proc` PID/starttime/live-parent observations establish positive owned
birth chains; pidfds are used where available. Without pidfd, only an unreaped
direct Popen child has narrow fallback signal authority. No group/name kill,
no stop of unrelated work, no unrelated command line/environment recording.
After-only births and cwd/identity changes are considered; unanchored relevant
processes/unclassifiable observations remain HOLD. Unrelated buildlike activity
cancels only the owned job. Short-lived missed/escaped workers and hostile
same-UID mutation cannot be ruled out by point snapshots; no stronger claim.
Descendant admission brackets refreshed child birth/ppid/pidfd checks with
fresh matching original live-parent birth/uid/cwd observations; a stale parent
row cannot authorize a replacement PID. Same-birth/UID zombies are non-signal/
non-live only with stable birth observations. A direct completion race needs
the actual unreaped Popen child's observed exit, not a generic ownership-error
exception; reaped old child PID integers do not exempt new unknown births.

Evidence includes bounded logs, exact argv/environment hashes, exits and partial
launches, original allocations/directories, process birth/argv/signal records,
resources, complete source checks, available XML bytes/hashes, and a non-PASS
`RESULT.json`. A final masked `terminal_commit` journal event decides operational
exit0 only when command0, stop0, source/XML/process/evidence gates and monotone
no-error/no-cancellation checks all hold. The boundary is the final pending
signal/latch observation under the mask; signals arriving after this explicitly
defined commitment are outside its promise. No unmasked cached-success return
or new work follows. Missing/incomplete terminal record or nonzero launcher
exit is never accepted as success. **Semantic acceptance and file closeout are
still REQUIRED even if operational exit0 is observed.**

## Retention and immediate closeout — F03 explicit choice

The runner does **not recursively delete anything**: all temporary clone,
caches, build/native extraction outputs and synthetic files remain under the
original private runtime. It also never deletes shared caches, SDK/toolchain,
source, permanent tests or evidence. This is deliberate F03 retain-for-review,
not completed resource cleanup. Root must promptly arrange a separate exact,
independently reviewed descriptor-bound closeout from the original allocation
identities and settled processes; inventory is deletion planning, not authority.
Per root's continuation boundary, closeout preserves R, the useful isolated
checkout, `.git`, all source/permanent tests/reports; it may remove only the
eleven named private temporary/cache roots and exactly admitted generated
checkout-output roots. **No whole-runtime/whole-checkout deletion is authorized.**
Every `reports/` and `test-results/` subtree is preserved in place, including
any containing generated-directory ancestors required to retain it. Source,
schemas, `.git`, permanent tests and report evidence are never deletion targets.
Closeout must record per-target intent/outcome, preserve compact evidence and
never adopt replacement/current inodes, retry ambiguous deletion or reopen old
G7/G8 caches. A stop/process/ownership uncertainty remains explicit residual.
The VPS disk/RAM discipline requires immediate attention, not indefinite cache
retention; preparation must reserve root time for that closeout.

## Permanent boundaries and accounting

PVU-007 STOP; PVU-011 NO RETRY; PVA-029 recorded FAIL/no automatic retry; G7/G8
runtime/recovery/cache/helper scopes CLOSED. Never import/execute the old runner
or replay archived helpers. No real vault/backup/clipboard, tester information,
private signing, application launch, publication, protected ref/tag movement,
version/identity/dependency change or occupied mobile build1017001 replacement.
The eight PVD choices remain separate. Android32 stays first in risk priority;
this useful admitted host selection is not Android32 or hardware evidence.

Current source proposal adds **zero application cases, closures or central
coverage**. Defined baseline denominators remain19/25 original confirmed,
22/37 total confirmed,2/12 suspicions; do not turn runner work or eventual task
counts into readiness percentages. Independent XML semantics and final ledger
review are required before root adopts any later progress.
