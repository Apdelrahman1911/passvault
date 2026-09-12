# Windows KDF01 — minimal non-publishing CI route

Author `/root/desktop_integration_author`; challenger `/root/desktop_main_review`.
**FEASIBLE TO PREPARE ONLY; UNIMPLEMENTED / UNADMITTED / UNEXECUTED.** This is not
a new Windows05 instance or a retry of any consumed/helper/refused scope.
Only this own-leaf note and inert workflow text are proposed. Root owns source
application, source freeze, publication, execution slot, CI and actual cleanup.

## Exact scope and paths

Native source review is separately accepted by `desktop_other_review`:
`B/reviews/team20/desktop_other_review/c20/NATIVE-KDF-EXACT-PATCH-REVIEW.json`,
SHA256 `6de5ea70be36aa4d9416f0de0135276a7a757975c8d1c3bddcdbdf9cf9e62321`.
Patch `native_security_author/c20/KDF-KNOWN-ANSWER.patch.txt` is
`133aba239b8040ae23c56621ba5c02a3407bbda8a85a324d12105adfbf33d268`.
It adds one CTest selector `passvault_biometric_windows_kdf_known_answer` invoking
only `passvault_biometric_windows_security_test --kdf-known-answer`.
One native process/CTest case covers two source-qualified fixed-vector equality
checks and its existing success/input guards. **No per-vector output marker
exists; do not require two markers, report two cases, or count HMAC calls.**
That branch returns before the normal RNG/temp/context/filesystem/Hello paths.
No PVA036 historical image, PRK allocation/caller cut, AES, device enrollment or
old test selector is scheduled. This CI proposal does not rereview the oracle.

Canonical application was still pending at the native review seal. Root now
records exact application in `B/reviews/team20/root/C20-NATIVE-TEST-INTEGRATION.json`,
with zero Windows execution. Required
after-images under `app-desktop/native/biometric-bridge/` are test
`b33f12ddf18f519e83dbac57797032a25192bbeb5bf071c9d62697c763576653` and CMake
`e27e27f3c1750c88c308341273410785b72de3c79af9a25f0d3e30c295a555fe`.
Their eventual source-commit binding must come from root, not a C19 label.

Proposed future canonical paths (none created here):

| Purpose | Path |
|---|---|
| Dedicated workflow | `.github/workflows/audit-windows-kdf-known-answer.yml` |
| Fixed new helper, no old-helper import | `scripts/audit/windows_kdf_known_answer_01.py` |
| Fresh activation request | `B/requests/windows-kdf-known-answer-01.json` |
| Genuine instance review | `B/reviews/team20/desktop_main_review/c20/windows-kdf/INSTANCE-ACCEPT.json` |
| Root-retained actual evidence | `B/runs/windows-kdf-known-answer01/` |

B = `docs/audit-continuation/2026-09-08-linux`. Source helper/workflow/request/review
hashes, activation nonce and source commit/tree remain **UNBOUND**. The companion
`workflow.yml.txt` is an inert route proposal, not a complete admitted packet.

## Activation, permission and scheduling boundary

Use exactly one `windows-2022` x64 job, preinstalled Python/CMake/MSVC and existing
SDK `10.0.26100.0`; no installations, fallback SDK or dependency/version changes.
Use branch-restricted **push of the one new request path**, not dispatch (the
workflow is absent from default main), PR, reusable workflow or a main merge.
Source/workflow/helper publication precedes activation; the activation must be
one-parent, **A-only request addition** immediately after the exact reviewed
source commit/tree. Check repository, dedicated ref, event=push, not deleted,
run_attempt=1, hosted Windows/X64, source cleanliness and native/input hashes
before commands. Recheck the frozen source/authority set after Job settlement.

Keep shared concurrency group `passvault-audit-validation-20260908` with
`cancel-in-progress: false`. It prevents replacement cancellation of the running
group member, **not replacement/cancellation of an older pending group run**.
The group alone proves neither global exclusivity nor local-build exclusion.
Before **either** source publication or activation push, fresh root admission
must exclude queued/pending **and** running local/CI audit cohorts and include
independent review of other-workflow triggers. That root-owned slot decision
cannot be inferred from the YAML or made by this lane; no probe is requested.
No rerun/retry or workflow-dispatch path.
Only `contents: read`; immutable existing checkout/upload action commits; exact
`${{ github.sha }}` checkout, depth2, no persisted credentials/submodules/LFS.
No `secrets` references, environment attachment, OIDC, signing, Store, write
permissions or publishing steps. Platform checkout/upload credentials are not
claimed nonexistent: none is forwarded to native/build child environments.

## Smallest affected-only command chain

Retain the old helper's four contained read-only Git checks: exact HEAD/parent
commit+tree, single-parent ancestry, A-only request diff and clean checkout.
Then one CMake version observation and these four fixed operations, never a bare
default executable, aggregate build, default CTest, ABI or historical/PRK target:

```text
cmake -S <workspace>/app-desktop/native/biometric-bridge -B <G>/build
  -G "Visual Studio 17 2022" -A x64 -DCMAKE_SYSTEM_VERSION=10.0.26100.0
  -DBUILD_TESTING=ON -DPASSVAULT_AUDIT_PVA036_037_CONTROLS=OFF
cmake --build <G>/build --config Release --target passvault_biometric_windows_security_test
  --parallel 1 --verbose -- /nodeReuse:false
ctest --test-dir <G>/build -C Release --show-only=json-v1
  -R ^passvault_biometric_windows_kdf_known_answer$
ctest --test-dir <G>/build -C Release --parallel 1 --timeout 30 --no-tests=error
  --output-on-failure --output-junit <G>/logs/kdf.xml
  -R ^passvault_biometric_windows_kdf_known_answer$
```

These are inert requirements, not commands executed by this lane. Resolve exact
installed executables; CMake/CTest must share their installation. Require the
filtered inventory to contain **one exact name and command argv**, normal built
executable under `<G>/build/Release` plus sole argument `--kdf-known-answer`.
Missing/extra selectors or a no-argument command refuse before test launch.
Use stable same-Win32-handle reads for the necessary SDK declarations and bounded
CMakeCache/normal-security `.vcxproj`, preserving Release C++20/EH/W4/WX/SDL/CFG
and effective verbose compiler evidence. Bind the small current configure-input
set: CMake, current Windows implementation/header/RC, normal security test and
ABI source referenced at configure time; ABI/DLL need not be built or executed.
Bind `.gitattributes` and all new authority/control files too; no historical input.
This is at most nine controlled parent commands, **one test**, no Gradle/JVM.

## Reuse lifetime primitives, not the 24-case harness

Source anchors read inertly: old workflow `audit-windows-native-validation.yml`
SHA `d70ba4a276957158d2904a426e421c7303bea9c47de4fc7a96cf060ce2bb1d21`;
old `scripts/audit/windows_native_validation.py`
SHA `35c14451f4e08b6c6f222f2b0f1297370b7534422f6b4194616bcecc92fc171a`.
Use its already-reviewed fixed Windows Job/reader/cleanup semantics in a distinct
narrow helper; never import/execute it or extend its live Windows05 selectors.
Do not replace them with `subprocess.run`, PID/name kills, shell `rmdir`, `rmtree`
or a general-purpose runner. The only new workload is one target/one selector.

- One original non-breakaway Job: creation-time `JOB_LIST` assignment, suspended
  process captured before resume, `HANDLE_LIST` limited to this command's NUL and
  stdout handles; Job/source/cleanup/earlier-log handles are not inherited.
  Preserve kill-on-Job-close,16-process/3GiB caps, original process HANDLE waits,
  immediate-parent exit receipt before fallible parsing/checks and no claim that
  a parent exit means descendant settlement.
- Private fresh `<G>` and separate `<E>` under RUNNER_TEMP with run_id/attempt1
  identity and root-supplied nonce; refuse preexisting roots. Bind original
  parent/generated handles. Use the existing explicit system/tool environment
  allowlist plus private TEMP/TMP/HOME/USERPROFILE/APPDATA/LOCALAPPDATA and disabled
  MSBuild reuse/telemetry. Do not forward CI tokens, secrets, arbitrary compiler
  flags or real user storage. No cache restore/save or toolchain cleanup.
- Preserve12GiB/25% launch and8GiB/20% running floors; serial commands; original
 480s command/540s cleanup/600s job envelope without renewal. Cancellation is
  sticky before every new command/resume. Finalization owns the sole original-Job
  termination attempt; logging/resource failure cannot veto it. Observe Job zero;
  kill-on-close or hosted disposal is fallback containment, **not cleanup proof**.
- Immediately after this single chain ends, or on failure/cancellation, settle
  the original Job, retain bounded exact logs/XML/source/config/PE hashes, close
  original log handles, then gather/validate **all** generated descendants before
  any deletion. Reject reparse/link/identity/close uncertainty; delete solely by
  retained original handles, descendants before original generated root, and
  verify its absence. E, checkout/source, SDKs/caches and all old held roots stay
  outside deletion. Test failure does not prohibit independently safe cleanup;
  evidence/ownership/settlement uncertainty does. Record HOLD rather than bypass.

Windows05's24 source-qualified passes coexist with overallFAIL and the INetCache
reparse-entry refusal/whole-root HOLD. The new selected KDF path omits those old
filesystem tests; that is **not proof no tool/profile can create a reparse entry**.
Preserve refusal/no-follow/no-old-root-access rules; no claimed Windows05 cure,
recovery, native-refusal reformulation, caller cut or security/hardware closure.

## One result and compact artifact

Keep one exact raw nonempty bounded `kdf.xml` (512KiB maximum), input/tool/SDK and
settled executable/PE hashes, effective compile logs, command/exits, Job/cleanup
receipts and a size/SHA256 evidence inventory. Retain9 command logs at most,
2MiB each/8MiB aggregate and1MiB JSON bounds; snapshots before settlement are not
settled-log or executable identities. Only the normal security executable needs
a settled PE hash; upload **no exe/DLL/PDB/object/vault/profile/SDK**.

Expected installed-CTest shape, grounded in retained Windows05 `case-14.xml`:
one `testsuite` with tests1/failures0/disabled0/skipped0 (errors absent or0), one
testcase whose name and classname are the new exact selector, status=run, and no
failure/error/skipped child. No per-vector stdout assertion; empty system-out is
valid. Preserve raw XML even on failure and reconcile source+selected argv+fresh
compiled target+CTest outcome independently. Zero/extra/skipped cases, missing
XML, build success or process launch cannot be credited as this test passing.

Enable upload only after exclusive owned E creation; `always()` alone is not
ownership. Use flat JSON/JSONL/log/XML allowlist, retention3days and error on no
files. Hard cancellation can prevent evidence/upload; never fabricate success.
Root independently reconciles actual GitHub run/job status, one case, source and
cleanup receipts before any result adoption. No family closure follows merely
from this useful missing fixed-output CNG evidence.

**Next:** paired feasibility review, then only a root-assigned minimal inert
helper/instance packet once application/source freeze is exact. This note is not
the required `ACCEPT_WINDOWS_KDF_KNOWN_ANSWER_01_INSTANCE` approval. All existing
STOP/NO-RETRY/CLOSED/HOLD/native-refusal/protected-branch/version/dependency/
identity/PVD/1017001 fences persist. No build, test, GUI, Git/network/probe,
helper import/execution, canonical edit or cleanup occurred in this subtask.
