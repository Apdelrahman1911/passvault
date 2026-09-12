# Windows source03 — KDF1 plus three reported-I/O-status cases

2026-09-12 UTC. Author `/root/c20_windows_author`; independent reviewer
`/root/c20_windows_review`. B = `docs/audit-continuation/2026-09-08-linux`.
**INERT SOURCE; no request, nonce, instance acceptance, publication or execution.**
Root alone owns canonical integration, Git/network/CI, the exclusive build slot,
actual-result adoption and cleanup. This bounded four-case batch replaces the
proposed KDF-only route; it does not add old cases or create a general runner.

## Exact source and compatibility boundary

| In this leaf | Root-only canonical destination |
|---|---|
| `windows_kdf_file_io_01.py.txt` | `scripts/audit/windows_kdf_file_io_01.py` |
| `audit-windows-kdf-file-io-validation.yml.txt` | `.github/workflows/audit-windows-kdf-file-io-validation.yml` |

`SOURCE03-FILES.json` binds afterimages and complete source02/helper and
source01/workflow diffs. The source02 helper precedent has SHA256
`7fbf6c70469697fe097c989efaea9c78a8ed9a3a3ab9e3183c0c043e891be92f`;
its accepted review is `B/reviews/team20/resume12/windows_review/SOURCE02-REVIEW.json`,
SHA256 `446bf160fa604885513b714de1033fd01da2ad0a5ff07218190c137c0683a019`.
Neither predecessor nor the consumed Windows05 helper is imported, executed or
modified. Their held roots and recovery refusals are untouched.

Only the reviewed workload/source/identity deltas change: new fixed paths and
suite, four names/argv/XML leaves, three accepted native afterimage hashes,
one additional IO01 review input, four-case inventory/serial selection and a
twelve-log ceiling. The `Windows` class is unchanged. `Run` changes only that
log ceiling/message; child creation, original handles, Job lifetime, retention,
termination and descriptor-bound deletion algorithms are reused literally.

The unchanged lifetime, cleanup, resource, failure and evidence terms in
`B/reviews/team20/desktop_integration_author/c20/windows-kdf/source01/SOURCE-SCOPE.md`
(SHA256 `2c6c5dbbcad3815c23e93590d79871939aac495e7592827ae3585cf19aabe8e1`)
remain applicable. This scope explicitly **supersedes** its one-case workload,
nine-log ceiling, singleton selector, old input hashes and old namespace/reviewer
terms; do not adopt those contradictory terms into this four-case instance.
The short essential controls and changed requirements follow.

## Four meaningful cases, one current normal executable

Build only `passvault_biometric_windows_security_test`, controls OFF.

| Ordered CTest name | Native argv after exact normal EXE | XML |
|---|---|---|
| `passvault_biometric_windows_kdf_known_answer` | `--kdf-known-answer` | `kdf.xml` |
| `passvault_biometric_windows_file_io_write_failure` | `--file-io-case write_failure` | `file-io-write.xml` |
| `passvault_biometric_windows_file_io_flush_failure` | `--file-io-case flush_failure` | `file-io-flush.xml` |
| `passvault_biometric_windows_file_io_close_failure` | `--file-io-case close_failure` | `file-io-close.xml` |

KDF1 covers two source-qualified fixed-vector equality assertions and success/
input guards; no per-vector stdout markers, extra HMAC case count or caller-secret
cut is claimed. The three I/O cases use real owned files/handles/ACLs plus deliberate
reported-status injection. Write writes a real two-byte prefix then reports false;
flush successfully writes/flushes all five bytes then reports false; close really
closes successfully then reports false. Their pre-teardown identity/bytes and
cleanup assertions are independently source-accepted by
`B/reviews/team20/resume12/windows_file_review/IO01-SOURCE-REVIEW.json`, SHA256
`a13876a95a1c13e51a7ead1328263bc2467a61e4ef51a0c34729db2f10ec08e6`.
These are not actual OS write/flush/close failures, hard crash, application
cancellation/compensation, provider/Hello/hardware or broad durability evidence.
PVA-036 crash/cancel residual remains open; no family closure follows.

The fixed `INPUTS` map contains eleven leaves: six native configure inputs,
`.gitattributes`, the three retained KDF authority receipts, and IO01's independent
review. The accepted native implementation/test/CMake hashes are respectively
`93746b0cdb399aa17dd8c3c915e907244d4e86e5b566c51714ccc7f80310bd5d`,
`7042b2e340640d189ced072c75c17f7a10d0fb287d43d77b3c722279a071bb60`,
`89fa2928be4da01c17f09cc0813fbf1f284f76ecc6d05a20176389cff9903b10`.
RC/ABI source are configure inputs, not targets/cases to execute. Controls OFF
avoids historical-source reads. The old `root/C20-NATIVE-TEST-INTEGRATION.json`
input describes earlier KDF-only application, **not these expanded afterimages**.
Root's new `B/reviews/team20/root/C20-WINDOWS-IO-INTEGRATION.json`, SHA256
`9e95bd776e407826706132ae137973a43e72c85e2f2e566adb06350795ab4b58`, and exact
canonical bytes must be reconciled in the independent source/instance review.
This is a narrowly bound Windows source set, not a full Linux source-capture
prerequisite. The helper/workflow/scope/review/request are separately hash-bound.

## Source publication, then independently admitted activation

One `windows-2022` hosted X64 job, Python>=3.11, preinstalled CMake/CTest/MSVC,
VS17 2022/x64 and SDK `10.0.26100.0`. No installation/fallback/dependency change,
matrix/cache/release reuse/environment attachment/signing/Store upload/application publishing.
Only `contents: read`; immutable checkout/upload action pins; exact push SHA,
depth2, no persisted Git credentials/submodules/LFS. Native children receive the
unchanged explicit tool/system allowlist, not CI credential environment.

Only push on `codex/audit-continuation-linux-20260908` touching the fresh
`B/requests/windows-kdf-file-io-01.json` triggers the one guarded job. Exact
repository/ref/push/not-deleted/attempt1 guards remain. The helper also requires
its exact canonical path, no CLI overrides and the hosted Windows/X64 context.
Source is published **without** the request, then activation is a sole-parent,
**A-only** request addition directly after the exact reviewed source commit/tree.
No request edit, source drift, merge, second run or automatic retry is admitted.

Root supplies a fresh32-lowercase-hex nonce. The request requires schema1, suite
`windows-kdf-file-io-01`, owner `/root`, the exact ordered `case_names` and complete
`case_argv` map above, target, `audit_controls:false`, SDK, `max_seconds:480`, exact
source commit/tree and helper/workflow/scope SHA256. Its only accepting-review path
is `B/reviews/team20/resume12/windows_review/IO-BATCH-INSTANCE-ACCEPT.json`.
That genuine reviewer-authored schema1 record must name `/root/c20_windows_review`,
disposition `ACCEPT_WINDOWS_KDF_FILE_IO_01_INSTANCE`, same suite/nonce/case_names/
case_argv/target/controls, full eleven-leaf `input_sha256` map and exact helper/
workflow/scope hashes. No fabricated placeholder can authorize execution.

Root's six literal-true request attestations remain `exclusive_build_slot`,
`source_push_excludes_queued_pending_running_local_ci`,
`activation_push_excludes_queued_pending_running_local_ci`,
`source_push_other_workflow_triggers_reviewed`,
`activation_push_other_workflow_triggers_reviewed`, `generated_cleanup_admitted`.
They are attestations, **not helper verification of remote/local queue state**.
Before **each** source and activation push, root must freshly exclude queued,
pending and running local/CI audit work, examine open continuation PRs (ordinary
PR synchronize workflows can trigger), and independently review the exact push's
other-workflow trigger effects. `cancel-in-progress:false` protects neither older
pending runs nor all other jobs and does not establish local/CI exclusivity.

Instance acceptance binds content/nonce, not a self-referential containing commit.
Root separately reconciles final source commit/tree, complete publication delta,
exact request bytes/hash, genuine review, fresh global slot and cleanup admission
**before activation**. Source acceptance alone is not execution permission.

## Twelve parent commands maximum; unchanged nonrenewable bounds

Four contained Git commands enforce actual HEAD/parent/tree, single parent,
A-only request diff and clean source (30s each). Then CMake version30s; configure
120s with `BUILD_TESTING=ON`/controlsOFF; one normal Release target build240s with
`--parallel 1 --verbose -- /nodeReuse:false`; one anchored union CTest inventory30s;
four individually anchored CTests45s each with one worker, `--timeout 30`,
`--no-tests=error --output-on-failure --output-junit <exact-XML>`.
The complete chain stays inside the nonrenewable480s command bound, not the sum
of individual budgets. A failure aborts later selectors, which remain UNSTARTED.

Before any CTest, inventory must contain exactly the four unique names, each
with this private build's absolute normal Release EXE and its exact argv. No
bare/default command, extra argument, wrong image, missing/duplicate/extra case,
unfiltered CTest, default/all-target or old current/historical/PRK case loop.
Pre/post generated cache/project snapshots keep controlsOFF and exclude the four
existing historical/PRK instrumentation tokens. Retain effective fresh verbose
Release compile/link evidence and hardening predicates, not snapshot-only proof.
Only the settled normal AMD64 PE receives a hash/length/architecture receipt.

One original non-breakaway Job, creation-time JOB_LIST plus restricted HANDLE_LIST,
suspended creation/original-HANDLE resume/waits,16 processes and3GiB committed-memory
cap remain. Disk/RAM launch floors12GiB/25%, running8GiB/20%; command/cleanup/workflow
bounds480/540/600s. Sticky cancellation prevents new launches/resumes. Finalization
owns the sole termination attempt; parent exit/kill-on-close/hosted disposal is
not observed Job-zero settlement. No PID/name killing, retry or old-root recovery.

Fresh G/E names include this suite and run_id/attempt1; G also includes the nonce.
Both must be absent; owned E enables artifact upload only after exclusive creation.
G-private TEMP/TMP/HOME/USERPROFILE/APPDATA/LOCALAPPDATA isolate synthetic files.
Preserve exact logs/XML/source/config/PE identities before cleanup. Original Job
zero, complete retention, no handle-close failure, owned roots and original
deadline gate all-before-any descriptor-bound descendant deletion, then original
G deletion/close/absence. Any reparse/link/identity/settlement/retention uncertainty
retains HOLD; no rejected target is followed/deleted or automatically retried.
No prior held root, checkout/source, shared cache, SDK/toolchain or evidence E is
a cleanup target. Per-case fixture cleanup does not establish whole-Job cleanup.
Gradle stop is not applicable: no Gradle task is invoked. Hard cancellation may
prevent records/upload; interruption and held resources must remain explicit.

## Compact raw evidence and independent adoption

At most12 command logs,2MiB each/8MiB aggregate; four named nonempty XMLs <=512KiB
each; JSON <=1MiB and inherited bounded journal. Flat JSON/JSONL/log/XML only,
three-day retention; no binaries/PDB/objects/profile/vault/cache/archive upload.
Root received-artifact intake must inventory all files by byte length/SHA256;
that inventory does not replace the helper's original pre-cleanup raw retention.

No XML scorer is added: operational success is raw XML awaiting independent
actual-case review. Each named XML must have exactly one testsuite with tests1,
failures0/disabled0/skipped0, errors absent or0; one exact-name/classname testcase,
status=run, no failure/error/skipped children. KDF system-out may be empty; no
per-vector markers are invented. Each I/O XML must additionally retain its own
terminal `PVA036_IO case=<matching suffix> status_injection=YES real_io=PASS
pre_teardown=PASS temp_id=<volume:high:low> destination_id=<volume:high:low>
cleanup=PASS` marker. Missing/extra/skipped/cross-case XML or missing marker is not
a pass. Reconcile source-qualified assertions, four selected argv, effective
compile/hardening/instrumentation absence, settled normal PE, command exits,
artifact hashes, actual CI run/job and Job/whole-root cleanup separately.

Windows05's24 reported source-qualified passes and overallFAIL/HOLD remain.
PVU007 STOP, PVU011 NO RETRY, PVA02949/44/5/no automatic retry, G7/G8 CLOSED,
native-refusal scopes, separate PVD choices, physical/provider/package gaps,
protected refs/tags, dependency/version/identity/signing/Store/publication and
occupied1017001 boundaries remain. This packet adds zero executions or closures.
