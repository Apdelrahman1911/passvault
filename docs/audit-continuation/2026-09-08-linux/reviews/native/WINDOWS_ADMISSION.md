# Windows native-14: one-shot admission proposal

Author: `/root/native`. **NOT ADMITTED until independent exact-candidate
acceptance, root's resource/coordination review, and the separate request push.**
This is a new helper, not the archived runner or any G7/G8 recovery procedure.
No invocation has been executed by this author.

## Essential platform evidence, not a platform matrix

Linux cannot execute these MSVC/Win32/CNG/Windows ABI paths. Root approved a
single `windows-2022`, x64, NON-PUBLISHING batch:

| Finding/scope | CTest names | Count | What a pass could establish |
| --- | --- | ---: | --- |
| PVA-036 | `passvault_biometric_windows_file_` + `success`, `validation_failure`, `dacl_failure`, `collision`, `empty_payload`, `oversized_payload`, `empty_suffix`, `rename_failure` | 8 | Actual Win32 writer success, failure/ownership/cleanup paths with three disclosed deterministic seams |
| PVA-037 | `passvault_biometric_windows_secret_` + `normal_scope`, `early_return`, `allocation_exception`, `nested_exception` | 4 | Actual production array guard on live synthetic arrays; ordinary return and explicit bad_alloc unwinding |
| PVA-014 sibling | `passvault_biometric_abi` | 1 | DLL ABI version/additive localized-symbol resolution/null-context output rejection |
| Native compatibility sibling | `passvault_biometric_windows_security` | 1 | Missing-metadata localized-reason validation; synthetic real-CNG wrapping/envelope roundtrip and tamper controls |

These are **14 authored registrations, zero new executed cases** until exact
results are independently adjudicated. The general security test returns from
missing metadata before Windows Hello availability/inventory and never enrolls.
The ABI test supplies null contexts. See the independent source challenge at
`../native-independent/NATIVE-TEST-SCOPE.md`.

PVA-010's actual concurrent native lifetime/cancel/provider/packaged gap is NOT
covered. PVA-014's displayed Android/iOS/macOS prompts are NOT covered. Hosted
Windows does NOT prove real Windows Hello/device behavior. PVA-036 still needs
the separately reviewed historical three-red/five-positive controls. PVA-037
still needs separately admitted production-cut fault-injection if that behavior
is claimed. The existing CNG roundtrip is NOT a fixed known-answer KDF vector.

## Activation and exact source identity

Only root creates
`docs/audit-continuation/2026-09-08-linux/requests/windows-native.json` after
accepting the independent review and reserving the sole local/CI build slot.
The workflow is push-triggered on that **one path and dedicated branch only**.
Source/workflow/helper/report pushes alone do not run it. It has no dispatch,
pull-request, release, reusable-workflow or protected-environment trigger.

1. Finish/review/commit the source, helper, workflow, source bindings and
   acceptance report on `codex/audit-continuation-linux-20260908`.
2. Record that source commit `S` and tree `T`. The separately created activation
   commit must have parent `S` and change **only** the request file. That avoids
   a self-referential commit hash. No unrelated pending changes may be included.
3. Request fields are below. Root records the request's exact command vector
   from the reviewed helper and why this important gap warrants Windows.
4. Push the activation only with no audit-owned local/other-CI job queued or
   running. Retain this reservation until the run terminates and cleanup/result
   evidence is reconciled. The constant Actions concurrency group is shared by
   future audit validations; `cancel-in-progress: false` does not terminate other
   runs. Root coordination, not a JSON boolean alone, establishes cross-host
   exclusivity.
5. Before allocating, the helper validates request/review/source from the same
   stable, single-linked, regular-file byte captures that it hashes. It repeats
   those bindings under the newly retained original namespace-parent handles.
   Then the helper verifies the checked-out request HEAD against `GITHUB_SHA`, its
   direct parent/tree against `S/T`, the one-path activation diff, clean checkout,
   exact helper/workflow/binding hashes and the actual independent acceptance.
   Runtime source is the activation checkout; it differs from `S` only in the
   nonexecutable request. Both commit/tree pairs go into evidence.

Request schema (placeholders are intentionally non-executable; do not commit
this example as an activation):

```json
{
  "schema": 1,
  "suite": "windows-native-14-v1",
  "owner": "/root",
  "exclusive_build_slot": true,
  "nonce": "<fresh 32 lowercase hexadecimal characters>",
  "source_commit": "<S: 40 lowercase hexadecimal characters>",
  "source_tree": "<T: 40 lowercase hexadecimal characters>",
  "helper_sha256": "<exact LF helper bytes>",
  "workflow_sha256": "<exact LF workflow bytes>",
  "source_bindings_sha256": "<SOURCE_BINDINGS.json bytes>",
  "independent_review_path": "docs/audit-continuation/2026-09-08-linux/reviews/native-independent/ADMISSION.json",
  "independent_review_sha256": "<reviewer's exact report bytes>",
  "sdk": "10.0.26100.0",
  "max_seconds": 900,
  "case_names": ["<all 14 exact ordered names from SOURCE_BINDINGS.json>"]
}
```

The reviewer-authored JSON must identify `reviewer: /root/native_review`, exact
`helper_sha256`, `workflow_sha256`, `source_bindings_sha256`, and disposition
`ACCEPT_WINDOWS_NATIVE_14_ADMISSION`. A merely root-authored acceptance label,
scope-only note, old review, pending review or mismatched hash fails admission.
The code author does not write that acceptance. Review reports should also state
the native input hashes, operational qualifications and unexecuted-control gaps.

Actions attempt must be 1. A failed step stops all subsequent commands; no loop
retries configuration, compilation, CTest or a failed process-stop request.
A later corrected-source request requires a new independent admission and root
decision, not an unchanged automatic retry. PVA-029 is never selected.

## Exact toolchain and command contract

- Fresh GitHub-hosted `windows-2022` x64; preinstalled Python >=3.11 x64,
  CMake/CTest >=3.25 from the same directory, VS 2022 generator/MSVC C++20,
  installed Windows SDK `10.0.26100.0` with the required WebAuthn API8/PRF
  declarations. Missing tools/headers fail; no automatic install or fallback.
- The CMake project itself enforces min3.25; helper records exact executable
  hashes/paths, runner image version and WebAuthn header hash. Generated project
  checks require `stdcpp20`, synchronous exception handling (`/EHsc`), `/W4`,
  `/WX`, `/sdl`, `/guard:cf`. CMakeCache selection and verbose compile/link output
  are retained. The admitted source already contains its normal hardening flags.
- No Gradle/JDK process is needed for this native-only scope. JDK17/wrapper
  requirements still apply to every separately admitted Gradle invocation;
  no Gradle `--stop` is invented here. Dependency, identity/version, SDK source
  and release configuration are not changed by selecting an installed compiler.
- The helper runs read-only Git identity/diff/status commands, `cmake --version`,
  then the following exact vectors (`W` = checkout, `U` = owned generated root,
  `E` = separate compact evidence directory):

```text
cmake -S W/app-desktop/native/biometric-bridge -B U/build
  -G "Visual Studio 17 2022" -A x64
  -DCMAKE_SYSTEM_VERSION=10.0.26100.0 -DBUILD_TESTING=ON
cmake --build U/build --config Release --target
  passvault_biometric_windows_security_test passvault_biometric_abi_test
  --parallel 1 --verbose -- /nodeReuse:false
ctest --test-dir U/build -C Release --show-only=json-v1
ctest --test-dir U/build -C Release --parallel 1 --timeout 30
  --no-tests=error --output-on-failure --output-junit E/case-NN.xml
  -R ^<one exact escaped case name>$
```

The last vector is invoked once per case in the fixed ordered list. Inventory
must be exactly 14 names; each CTest result must contain its exact one named
case and no failure/error/skip. It is not 14 Gradle tasks or 14 provider sessions.
Machine scoring counts only observed individually passing XMLs; independent
review must report actual failed/executed/skipped cases from retained XML/logs.

## Resource, process and cleanup admission

Before any native command: at least 12 GiB free on checkout/generated volumes
and 25% available physical RAM. During commands: poll every five seconds and
maintain 8 GiB/20%; log sizes are checked at least every 0.5 seconds. Below-floor
observations fail with their measured values. These are sampled floors, not
continuous measurements. Each raw log limit is 2 MiB (possible polling
overshoot). Raw logs live only in the generated root. At most a 2 MiB prefix per
log is copied to evidence, with its prefix hash and the full settled-file hash;
truncation/unsettled streams are explicit. Large raw logs are never uploaded.

Exactly one Windows Job object is created before any command. Its limits are
16 child processes, 3 GiB aggregate committed child memory and
`KILL_ON_JOB_CLOSE`; breakaway is not enabled. The Python host/runner/OS consume
memory separately and are subject to sampled resource checks, not that job cap.
`PROC_THREAD_ATTRIBUTE_JOB_LIST` binds each child atomically at creation, before
resuming its suspended primary thread. An explicit inherited-handle list gives
it only NUL stdin and the command's output log, not source/cleanup/job handles.
The fixed sanitized child environment excludes credentials, signing variables,
ambient compiler/init options and user profile/cache locations. PATH/toolchains
come from the fresh hosted image and their selected binaries are hash-recorded.

Configure: 120s; compile: 420s; inventory/read-only tools: 30s each; each CTest:
30s child timeout /45s outer bound; global command budget900s. Actions job limit
20min includes initialization, failure cleanup and upload. MSBuild node reuse is
disabled; CMake/CTest parallelism is one. A command can advance only after its
whole owned job is empty. Lingering descendants fail, are terminated only via
the exact owned job handle, and must settle. No name/PID scan, unrelated kill,
taskkill, daemon stop or old lock adoption occurs.

The sole generated root is absent before allocation:
`RUNNER_TEMP/passvault-native-<run_id>-1`. Its `build`, `tmp`, `home`, `appdata`,
`localappdata` and `logs` children confine compilation, raw logs and synthetic
fixtures/caches.
`TEMP`/`TMP` point inside it. Evidence is separately retained under
`RUNNER_TEMP/passvault-native-evidence-<run_id>-1`.

Cleanup is installed before native launch and runs after success/failure and
cooperative cancellation. Original checkout and runner-temp parent handles are
retained without DELETE authority. The original generated root handle denies rename /
delete sharing. After owned-job settlement, all descendant handles are opened
without following reparse points, checked for single links, and retained before
deletion. Cleanup uses those retained handles and bottom-up dispositions, not
fresh-path recursive deletion. Every disposition has a prior fsynced identity
journal; unknown/reparse/hard-linked entries or >5000 entries cause HOLD. Source,
tests, reports, shared caches, SDKs and toolchains are never cleanup targets.
No failed delete or failed explicit job termination is automatically retried.

The exclusively allocated hosted namespace assumes cooperative absence of
unrelated writers; `mkdir`-to-handle bootstrap and directory enumeration are not
an adversarial same-user filesystem sandbox. Test fixture `remove_all` is also
a cooperative synthetic-root contract. Hard kill/runner loss can interrupt
Python finally blocks, evidence or upload. Kill-on-job-close prevents surviving
owned children under normal OS handle cleanup; hosted disposal is external
containment, NOT an observed successful file cleanup. Residual/unsettled states
remain FAILED/HOLD with no automatic recovery/adoption. This new contract does
not reopen original G7/G8 or discharge any historical stop obligation.

## Compact retention and interpretation

Keep only request/source identities, toolchain hashes, configuration predicates,
command/resource/cleanup journal, compact stdout/stderr, exact per-case XMLs,
CTest inventory and binary hashes/AMD64 PE identities. The unsigned DLL and two
test EXEs are **not uploaded** and are deleted with their objects/PDBs/CMake
outputs after evidence capture. No packaging, install, signing, upload to
stores, candidate replacement, production versions/identity/dependency change
or real-data operation is included. Artifact retention is three days; root
extracts only compact required evidence into the continuation branch promptly.

Even all-green results are awaiting independent semantic and cleanup review,
not complete PassVault readiness, PVA-036 historical-red proof, PVA-010 closure,
universal erasure, genuine device security, PVD owner decisions or publication.
