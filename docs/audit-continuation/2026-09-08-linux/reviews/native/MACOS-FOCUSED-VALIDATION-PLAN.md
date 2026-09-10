# Focused macOS native five 02 — source proposal, not execution admission

Author `/root/native`. **SOURCE ONLY; NOT EXECUTED OR ADMITTED.** This02 packet
changes only the resource parser/accounting and diagnostics of consumed focused01,
plus fresh request/workflow/namespace identities. It is not an unchanged rerun or
automatic retry. Linux03 retains execution priority; root alone owns any future
source/instance/coordination/cleanup admission after independent source review.

The larger Mac helper/plan, capture failures, focused01 failure and their reviews
remain dormant, unchanged history. No old runner, capture or recovery helper is
executed or imported.
PVU-007 STOP, PVU-011 NO RETRY, PVA-029's failure/no automatic retry, G7/G8 CLOSED,
and Windows01's consumed failure/filesystem HOLD remain unchanged.

## Preserved01 and source basis for the one accounting correction

Before editing, exact01 source was copied into the exclusive new
`macos-focused-01-before/` directory as read-only, non-executable inert evidence:

| Inert before-image | LF / bytes | SHA256 |
| --- | ---: | --- |
| `macos_focused_validation.py.txt` | 617 / 33401 | `4448517d4615a34090d2acc61cd8eb872d3e8c1445fd711e8d7aa146f243b69c` |
| `audit-macos-focused-validation.yml.txt` | 68 / 2645 | `3d9c947eaacaabec26a8bea26810305ec37021773a83edee38ee1887e1356ac5` |
| `MACOS-FOCUSED-VALIDATION-PLAN.md.txt` | 102 / 6615 | `bfa26b13cdab4477b9f5e118b0b7369184be1af10b9c9c47441c66fabe9d9f07` |

Run34333087199 remains **FAIL_RESOURCE_GATE_BEFORE_PRODUCT_COMMAND; five
UNSTARTED, zero product passes/failures**. Source
`96f7758de9984528f4624944c0594d47fc20f14b`, activation
`17a19b0a36472e586851f5ea4185cefe9adaad71`; actual helper exit1. The original
private-root removal was independently source-qualified only for its prelaunch
normal metadata path; final-exit-zero was unmet, and no final-FD or arbitrary
descendant proof follows. Its scheduling-only release, no-retry restriction,
request, admission and result review remain unchanged. Missing historical total
and speculative count prevent recomputing its fraction or a hypothetical pass.

`../build-config/MACOS-MEMORY-CAPTURE04-SEMANTICS.md`, SHA256
`b78ab6dc110cdfa12c1772f1a7d0ec1349d8193f8e39c199bb10fedf13e5dd44`, independently
reconciles the bounded retained Apple-source capture04. Default
`system_cmds-vm_stat.c.txt:129-135` prints F=`free_count - speculative_count`
and S=`speculative_count` from the same response. XNU
`xnu-vm_statistics.h.txt:158-163` calls speculative already included in raw free;
`xnu-host.c.txt:808,818,848` adds and returns the same speculative local.
Thus **(printed F + printed S) * that response's page size** reconstructs the
kernel-reported free-page class, not a full available/reclaimable-memory estimate.
Adding S again to raw Mach `free_count` would double-count and is not this code.
No inactive, purgeable, file-backed, compressor or other category is added.

Those retained source/blob/API identities do not map the upstream revision to
the earlier installed macOS15.7.9/kernel24G830 or prove the next runner's values.
This is a prospective source-supported parser/metric correction; installed-version
compatibility remains a qualification, not a manufactured runtime observation.
Capture03 stays consumed FAIL with exact rejecting field/cause UNKNOWN. No new
capture/probe, source network access, test, import or execution is included here.

## Source and admission

The new source packet is `scripts/audit/macos_focused_validation.py`,
`.github/workflows/audit-macos-focused-validation.yml`, and this plan. Independent
review of the concrete frozen bytes is required. Root alone commits the reviewed
source, records its commit/tree plus the later activation SHA/parent/tree, and
admits the push **only with the shared local/CI execution slot idle**. No request
is created by this packet. Activation adds only
`docs/audit-continuation/2026-09-08-linux/requests/macos-focused-02.json`, containing
`source_commit`, `source_tree` and a new 32-hex `nonce`; the helper checks the
single-parent addition, clean checkout, exact helper/workflow bytes and all five
reviewed native SHA256s in `NATIVE_INPUTS` (LF checkout qualification).
Fresh generated/evidence names are `passvault-macos-focused-02-<run_id>-1-<nonce>`
and `passvault-macos-focused-02-<run_id>-1-evidence`; the artifact name is
`macos-focused-02-<run_id>-1`. No old request or execution admission is reused.

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

Launch floors remain **12 GiB disk /25% RAM**; running floors remain **8 GiB /20%**.
The numerator is now `kernel_free_equivalent_bytes = (printed_free_pages +
printed_speculative_pages) * page_size_bytes`. Keep the old
`free_physical_bytes = printed_free_pages * page_size_bytes` unchanged as a
separately named observation, not silently relabeled as the new gate. This
explicitly changes the unused-only proxy to the OS-classified free class including
speculative pages; it can admit samples the old proxy rejected, without reducing
percentages or asserting that all reclaimable memory is immediately free.

Use the same single default `/usr/bin/vm_stat` command. Require one complete
snapshot header and exactly one full integer row for each printed count; missing,
duplicate, malformed, negative or interval-suffixed values HOLD without zero
fallback. Require dynamic positive page size, positive already-read total and
derived bytes no greater than total; use exact integer ratio comparisons, never
clamp an impossible reading. Before parser/page-size/floor rejection retain the
launch/running phase, total, metric label, available page/F/S values, old and
derived bytes and unchanged floors in the result record. Unread/unrecognized
values remain null; malformed-field row counts remain diagnostic, not readings.
Total physical bytes are also retained before the existing positive-total guard.
The resource elapsed time marks the observation attempt's start; final retention
still depends on reaching the existing finalizer, not surviving hard termination.

Prospective regression requirements, **not executed cases**: S=0 reproduces the
old numerator; nonzero S is added only once; absent/duplicate/malformed fields,
P=0 or derived>total reject; equality at25/20% and12/8GiB remains accepted while
below the RAM ratio or one byte below the disk floor rejects. A retained sample is not a memory
reservation, hardware proof or proof of the metric on every installed OS.

Sample at launches and approximately five-second intervals
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
