# Windows native-14 cohort 02 — source/admission scope

Author: `/root/native`. **SOURCE ONLY; no request, execution, test credit or
admission is created by this note.** Root alone may reserve the shared local/CI
build slot and activate a separately accepted, exact-source instance. This is
not the old observability patch, a general runner, or recovery of Windows01.

## Preserve the failed predecessor

Run **34284083351/attempt1**, source parent
`1a57239f74af792b64e631f51a79e1c0bf83816a`, activation
`a1fbdf0d5f0663225a459838d37b072a65e334f6`, remains **FAIL/HOLD**. Four read-only
commands returned0; configure performed compiler probes and printed normal
configure/generate completion. Its actual parent exit was read but not retained
before the ten-second whole-Job drain failed. One exact-Job termination and
later observed zero support worker settlement. Complete pre-deletion gathering
then rejected an **unidentified reparse entry**; no deletion was attempted.
The child identity, reparse path/tag/target and root size are unproved. Product
build and all14 CTests were **UNSTARTED**, not failed test cases.

This successor changes the admitted **cohort boundary** and retention ordering;
it does **not** diagnose that child/reparse entry, claim stock-tool benignity
for it, or convert the old failure into a pass. Keep old `windows-native.json`,
`SOURCE_BINDINGS.json`, source, result review and operational failures frozen.

## Essential target scope — unchanged14, no PVU-008 experiment

Linux cannot execute these MSVC/Win32/CNG/DLL paths. The exact ordered names
remain the helper's `CASES`, independently challenged in
`../native-independent/NATIVE-TEST-SCOPE.md`:

| Finding/scope | Ordered CTest suffixes | Cases authored |
| --- | --- | ---: |
| PVA-036, `passvault_biometric_windows_file_` | `success`, `validation_failure`, `dacl_failure`, `collision`, `empty_payload`, `oversized_payload`, `empty_suffix`, `rename_failure` | 8 |
| PVA-037, `passvault_biometric_windows_secret_` | `normal_scope`, `early_return`, `allocation_exception`, `nested_exception` | 4 |
| PVA-014 ABI sibling | `passvault_biometric_abi` | 1 |
| Synthetic native compatibility sibling | `passvault_biometric_windows_security` | 1 |

The same production guard/Win32 operations, disclosed deterministic seams,
live synthetic arrays and explicit exception oracles remain. ABI calls have
null contexts; the general suite's missing metadata returns before credential
inventory/Hello. It also exercises synthetic real-CNG roundtrip/tamper paths.
No real vault, backup, clipboard, credential, Hello prompt or private material
is selected. No destroy-status/content-lifetime experiment is added for
PVU-008; its accepted unresolved content-contract outcome remains unchanged.

The helper now binds **six current native files plus `.gitattributes` inline**.
Its CMake SHA256 is
`f72420bc39a5c5dad514b60eb0583880fc329e0a1a57a9b5dd12c8eeeb3c54e3`;
the old external binding names the older CMake bytes. Current Apple-only fixture
additions do not expand this Windows inventory. Each of the seven exact actual
checkout hashes is rechecked before allocation and after Job settlement.
This is not whole-tree/raw G12 checkout-EOL certification.

## Fresh request and independent acceptance

The only trigger is an **addition**, not modification/reuse, of
`docs/audit-continuation/2026-09-08-linux/requests/windows-cohort-02.json` on
`codex/audit-continuation-linux-20260908`. Source/helper/workflow/report-only
pushes do not activate it. No dispatch, release, pull-request, reusable workflow
or protected-environment entry exists. Attempt must be1, repository/ref exact,
runner GitHub-hosted Windows/X64. No automatic retry or request is supplied.

Root first commits the exact reviewed helper, workflow, this note and the
reviewer's own acceptance. A later activation commit `C` must have **exactly
one parent `S`**, match `GITHUB_SHA`, and change only `A<TAB><new request>`.
The request binds `S` and its tree `T`; evidence retains both source `S/T` and
activation `C/tree` identities. The fresh request requires:

- `schema: 1`, `suite: windows-native-14-cohort-v2`, `owner: /root`,
  `exclusive_build_slot: true` and a fresh32-lowercase-hex `nonce`;
- `source_commit`, `source_tree`, `helper_sha256`, `workflow_sha256`,
  `scope_sha256`, `independent_review_path`, `independent_review_sha256`;
- `sdk: 10.0.26100.0`, `max_seconds: 480`, exact ordered14 `case_names`.

The separately authored JSON must identify `reviewer: /root/native_review`,
disposition **`ACCEPT_WINDOWS_NATIVE_14_COHORT_ADMISSION`**, and match the exact
helper/workflow/scope hashes. Old acceptance labels/bindings cannot activate
this successor. Hash validation parses the same bounded stable bytes it hashes.
Root's Boolean is not a cross-host lock: root must independently establish and
retain one audit-owned build/test job across local and CI through reconciliation.

## Toolchain and fixed command vector

One `windows-2022` x64 job, **10-minute** Actions limit. Preinstalled x64
Python>=3.11, same-directory CMake/CTest (project min3.25), VS2022/MSVC/C++20,
installed Windows SDK10.0.26100.0 with the existing WebAuthn API8/PRF declarations.
Missing requirements fail without installing/replacing tools or dependencies.
Tool executable/header hashes and hosted image version are retained.

`W` is checkout, `U` the newly allocated generated root; commands are serial:

```text
git -c core.hooksPath=NUL -c core.fsmonitor=false -c gc.auto=0
  rev-parse HEAD HEAD^{tree} HEAD^ HEAD^^{tree}
git <same -c options> rev-list --parents -n 1 HEAD
git <same -c options> diff --name-status HEAD^ HEAD --
git <same -c options> status --porcelain=v1 --untracked-files=all
cmake --version
cmake -S W/app-desktop/native/biometric-bridge -B U/build
  -G "Visual Studio 17 2022" -A x64
  -DCMAKE_SYSTEM_VERSION=10.0.26100.0 -DBUILD_TESTING=ON
cmake --build U/build --config Release --target
  passvault_biometric_windows_security_test passvault_biometric_abi_test
  --parallel 1 --verbose -- /nodeReuse:false
ctest --test-dir U/build -C Release --show-only=json-v1
ctest --test-dir U/build -C Release --parallel 1 --timeout 30
  --no-tests=error --output-on-failure --output-junit U/logs/case-NN.xml
  -R ^<one exact escaped case name>$
```

The last command runs once per case. There are22 intended parent invocations,
**not22 tests**. Metadata/inventory commands have30s caps, configure120s,
build240s, each CTest45s outer/30s child. The shared480s command deadline limits
the whole series; per-command maxima are not additive available time. Cleanup
has a540s cutoff, leaving bounded Actions time for compact artifact upload.
Pre-build VCXPROJ/cache checks are **snapshots** of C++20, synchronous exception
unwind, W4/WX/SDL/CFG settings, not final hashes or effective compiler proof.
Final project/AMD64 PE hashes follow settlement; verbose compile output must
still be independently reconciled. No Gradle, JDK, packaging or app launch is
selected; wrapper `--stop` is NOT_APPLICABLE here, not a prior-stop discharge.

## One positively owned cohort and bounded retention

Before any command, create one unnamed, non-breakaway Job: active-process cap16,
aggregate committed child memory3GiB, kill-on-close. Creation-time
`PROC_THREAD_ATTRIBUTE_JOB_LIST` binding is atomic; children start suspended.
The explicit inherited handle list contains only current stdout/stderr and NUL
stdin, never the Job, parents/root or previous logs. Original handles are used
for waits, exit retrieval, Job control and cleanup; no PID/name killing occurs.

Read-only preflight commands still require Job emptiness. **From configure
through all CTests, positively owned stock-tool descendants may remain between
phases.** There is no per-command whole-Job drain or termination. Each immediate
parent's actual exit is saved to memory and fsynced journal before Job queries,
log checks or parsing. Job-active observations and eventual settlement remain
separate. A nonzero parent/other failure launches no later command.

Original exclusively created stdout handles remain open through final retention
and deny deletion sharing. Every still-writable command log is checked at
launch, at500ms process polls, and in final drain; declared XML paths are also
checked. Limits:24 logs maximum (22 intended),2MiB each,8MiB aggregate log bytes,
512KiB per XML,1MiB per compact JSON,16KiB per journal event/2MiB journal total.
These are sampled limits with possible overshoot, not disk reservations.
Retained log prefixes share the same8MiB total cap; incomplete/unsettled prefixes
are explicitly unadjudicated and have no full-file hash. XML is generated in
`U/logs`, then bounded raw bytes are retained only after observed Job zero.
There is **no custom XML scoring**: command returns/inventory/named registrations
are not actual test cases. Independent review must reconcile exact names,
actual execution/failure/skip outcomes and complete raw XML. Missing/empty XML
cannot produce completed-success status.

Launch floors remain12GiB free/25% available physical RAM; running floors8GiB/20%,
sampled every5s during commands/natural drain. Python/runner memory lies outside
the Job's child cap. Parallelism is1; MSBuild node reuse is disabled. Sanitized
child environment redirects HOME/profile/APPDATA/TEMP into `U` and omits
credentials, signing variables and ambient compiler/init options.

## Final drain, cleanup and unchanged boundaries

On ordinary completion, allow at most10s natural final drain. Failure,
cancellation or residue enters one original-Job termination attempt and up to10s
bounded zero observation. Journal/resource/output failure cannot veto that stop.
All live logs remain monitored during post-stop waiting. Kill-on-close is final
containment, **not** observed Job-zero proof. Cancellation is sticky; afterward
only bounded evidence/owned cleanup proceeds. Default signal handling is restored
before terminal return so a late signal is not swallowed as successful exit.

Fresh exclusive namespaces under runner temp:
`U = passvault-windows-cohort-<run_id>-1-<nonce>`;
`E = passvault-windows-cohort-<run_id>-1-evidence`.
No adoption of existing paths. Only an exclusive `E` allocation enables the
workflow's compact-artifact upload. Pinned checkout/upload actions, read-only
contents permission and3-day retention remain; no secrets/environment/signing,
store upload, approval or publication step is present.

After Job zero, retain final source/generated identities and raw evidence, then
close original log handles once. Evidence-retention or original-handle close
failure causes **filesystem HOLD**, not generated-root deletion. Otherwise
gather **all** descendant no-follow handles (max5000) before any deletion,
retain identity and use bottom-up original-handle dispositions. Reparse or
non-single-linked entries reject: retain the exact lexical owned-relative path
(or length/hash if over1024 characters), same-handle volume/file identity,
attributes/link count/reparse flag and rejected-handle close outcome. Do not
read a target or tag, follow it, delete it, relax the guard, or diagnose Windows01
from a later observation. No failed close/delete/termination is retried.

This remains a cooperative fresh hosted namespace, not adversarial same-user
isolation: mkdir-to-handle bootstrap, path reads and enumeration have that
qualification. Hard kill, runner loss or the job deadline can interrupt finally,
receipt writing or upload; hosted disposal is **not an observed cleanup pass**.
Source, permanent tests/reports, shared caches, SDKs and toolchains are never
cleanup targets. Unsigned DLL/EXEs/objects/PDBs are not uploaded.

PVA-010/provider/concurrent-lifetime, actual prompt/Hello/iPhone/hardware gaps,
PVA-036 historical red controls, PVA-037 production allocation-cut injection and
a fixed CNG known-answer vector remain outside this scope. PVD choices remain
separate. PVU-007 STOP; PVU-011 NO RETRY; PVA-029 FAIL/no automatic retry; G7/G8
CLOSED. No old runner/import/recovery, protected-ref/tag/version/identity/
dependency change, signing, publication or replacement build1017001 is allowed.
