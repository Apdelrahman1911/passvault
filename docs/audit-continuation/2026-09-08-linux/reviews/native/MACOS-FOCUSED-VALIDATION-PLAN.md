# Focused macOS native five — source proposal, not execution admission

This **new** cycle replaces only the unexecuted preparation of the larger Mac
helper/plan. Those files, reviews and capture failures remain dormant, unchanged
history. No old runner, capture or recovery helper is executed or imported.
PVU-007 STOP, PVU-011 NO RETRY, PVA-029's failure/no automatic retry, G7/G8 CLOSED,
and Windows01's consumed failure/filesystem HOLD remain unchanged.

## Source and admission

The new source packet is `scripts/audit/macos_focused_validation.py`,
`.github/workflows/audit-macos-focused-validation.yml`, and this plan. Independent
review of the concrete frozen bytes is required. Root alone commits the reviewed
source, records its commit/tree plus the later activation SHA/parent/tree, and
admits the push **only with the shared local/CI execution slot idle**. No request
is created by this packet. Activation adds only
`docs/audit-continuation/2026-09-08-linux/requests/macos-focused-01.json`, containing
`source_commit`, `source_tree` and a new 32-hex `nonce`; the helper checks the
single-parent addition, clean checkout, exact helper/workflow bytes and all five
reviewed native SHA256s in `NATIVE_INPUTS` (LF checkout qualification).

Push trigger: only `codex/audit-continuation-linux-20260908` and that new request
path; exact repository/ref/event and attempt 1. One `macos-15-intel` job, ten
minutes, shared audit concurrency, no cancellation of unrelated runs. Immutable
existing checkout/upload pins, contents:read, no environment/signing/store secrets,
publishing, dependencies, installs, identity/version changes or build 1017001.

## Exact workload and expected evidence

Linux cannot run these Objective-C++/Foundation/LocalAuthentication/Mach-O tests.
Use installed CMake/CTest **4.4.2**, Xcode **16.4 / 16F6**, macOS SDK **15.5**,
`/usr/bin/make`, and installed `/usr/local/bin/python3 -I -B`. Missing tools or
drift fail closed without installation/fallback. Set process-local DEVELOPER_DIR;
record observed paths, versions, architecture and synthetic binary hashes.

Configure `app-desktop/native/biometric-bridge` using Unix Makefiles,
`BUILD_TESTING=ON`, Release, explicit x86_64/SDK/clang/clang++/ObjC++/make; then
`cmake --build <private-build> --config Release --parallel 1 --target
passvault_biometric_macos_security_test passvault_biometric_abi_test`. Verify three
Mach-O64 x86_64 outputs and the exact five-name CTest inventory/selector argv.
For each name below, in this order, run:

```text
ctest --test-dir <private-build> -C Release --parallel 1 --timeout 30
  --no-tests=error --output-on-failure --output-junit <private-logs>/case-NN.xml
  -R ^<exact-name>$
```

| Order / exact name | Evidence scope, not family closure |
| --- | --- |
| 1 `passvault_biometric_macos_fixture_normal` | MAC-FIXTURE-001 explicit cleanup/child absence/sibling sentinel |
| 2 `passvault_biometric_macos_fixture_early_return` | MAC-FIXTURE-001 real filesystem assertion-return/destructor control |
| 3 `passvault_biometric_macos_fixture_cpp_exception` | MAC-FIXTURE-001 real filesystem C++ exception/destructor control |
| 4 `passvault_biometric_abi` | PVA-014 additive ABI, null-context rejection and synthetic output wiping |
| 5 `passvault_biometric_macos_security` | MAC-FIXTURE-001 and bounded PVA-010/PVA-014 synthetic metadata, localized LAContext properties, error mapping and busy-destroy schedules |

These are five actual CTest declarations, not five family closures. Retain actual
command exits and raw XML; independent review must reconcile exactly the selected
cases, failures/skips, logs, source identity and cleanup, rather than count tasks
or trust a custom XML parser. Nonzero CTest (including possible internal timeout)
is **uncertain**, stops later cases and prohibits tree deletion; retain available
bounded failure XML only as an unadjudicated snapshot. UNSTARTED and unknown
launch outcomes remain explicit. Missing/empty XML cannot establish a pass.

## Narrow trust, isolation, bounds and cleanup

Normal-path settlement is **qualified to trusted synchronous stock tools and the
reviewed closed workload**, not arbitrary descendants. Reviewed CMake has no
detached custom work; tests create no subprocesses and successful busy-destroy
reaches `destroy.get()`. Normal returns, EOF, expected outputs and empty fixture
parent are combined; a late process snapshot or foreground exit alone is not
proof. No capability/enrollment query, authentication, Keychain operation, real
vault/clipboard data, displayed prompt or physical-device evidence is admitted.

Before launch, install signal/finalization handling and acquire original no-follow
ancestor/private-root handles. New U/E and their children require owned 0700/no
ACL. Ancestors allow no ACL or exactly one non-inheriting
`0: group:everyone deny delete` entry; no ancestor is deleted. Exclusive new roots,
synthetic HOME/tmp/cache/native parent, minimal child environment and original
Actions output-file checks isolate this cycle. No Gradle/JDK build is selected,
so there is no Gradle daemon/cache or wrapper-stop task in this CMake-only job.

Launch floors: 12 GiB disk and 25% conservative **free physical pages**; running
floors: 8 GiB and 20%. Sample at launches and approximately five-second intervals
during children (fixed metadata calls can add up to ten seconds; not a hard
five-second guarantee). Configure 120s, build 240s, each CTest wrapper 45s;
global commands 480s, cleanup stops at 540s, job 600s. Logs 2 MiB each, XML 512 KiB
each, total evidence 16 MiB with a 64 KiB final reserve; upload only flat compact
JSON/log/XML for seven days, never binaries/build archives.

Timeout/cancellation/output-resource failure: only the original Popen child may
receive terminate/wait5/kill/wait5; never a copied PID/group/name kill. Forced
stop or ambiguous settlement means HOLD, no later cases/tree deletion/retry;
hosted runner disposal is containment only. Normal cleanup is once-only,
original-FD/no-follow, allowlisted generated outputs, same device/owner and
depth12/4096-entry bounds. Never recurse into native-parent residue; retain it
and remove only independently safe other generated outputs. Preserve evidence,
record cleanup/close failures, and require actual helper exit zero as well as
independent result review. Uncatchable runner termination remains an explicit
cleanup limitation, not a success. Physical iPhone/Touch ID/Windows Hello,
displayed-language/provider and full JVM/packaged lifecycle gaps remain BLOCKED
or outside this batch; iOS's separate tests do not expand these five cases.
