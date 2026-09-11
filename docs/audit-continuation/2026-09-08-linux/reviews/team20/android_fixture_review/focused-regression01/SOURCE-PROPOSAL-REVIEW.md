# Focused regression01 — independent exact source review

Reviewer: `/root/android_fixture_review`, 2026-09-11.

**Disposition: ACCEPT SOURCE ONLY, wholly UNBOUND.** No unresolved material
source finding remains in the exact packet below within this bounded review.
This is not source application, an instance approval, execution admission,
compilation/test evidence or cleanup authority. Root must separately bind and
review the resulting exact current-source instance; binding edits change these
proposal identities.

W = `/root/projects/PassVault/passvault-linux`;
B = `W/docs/audit-continuation/2026-09-08-linux`.
Author directory: `B/reviews/team20/android_fixture_author/focused-regression01/`.
Only reports in this reviewer's own `focused-regression01/` directory were written.
The completed Android32 fixture review is separate and unchanged.

## Exact reviewed packet

All four new-file unified diffs were read completely and reconstructed **only in
memory as text**. Headers, hunk lengths, destination paths, sizes and hashes match
the sealed author packet. No helper was materialized, imported, AST-parsed or run.
`/dev/null` denotes a new-file proposal, not a fresh filesystem-absence probe.

| Author patch | Bytes | SHA256 |
| --- | ---: | --- |
| `01-inner.UNAPPLIED.patch.txt` | 40577 | `c3fd72c2dd6260e9548dc1b3e480586b460225d26f2d0334dfb93296e4fb1a78` |
| `02-init.UNAPPLIED.patch.txt` | 9598 | `b5cb94b383d6b0b66024470270bba7cc5e097d4cc198cdf53957b0de999a69df` |
| `03-outer.UNAPPLIED.patch.txt` | 51836 | `4116706cd7982f2cf8a5eb45201c81cea9983592bb8fb0130e0edf602c30d5a9` |
| `04-selection-ledgers.UNAPPLIED.patch.txt` | 3149 | `a0ca154a5c83c45d8065181afbf17afe0783e01394a49ff0c2a0004dd6eeaebb` |

Proposed after-images, **not files applied by this reviewer**:

| Future destination | Lines | SHA256 |
| --- | ---: | --- |
| `W/scripts/audit/linux_focused_regression_01.py` | 704 | `97c77cc76ecf18b86e16ae169ec83e81f9d5822e744ba2913e9ed621b05aa9c2` |
| `W/scripts/audit/focused_regression_01.init.gradle` | 128 | `e945045a8de9db6b8b773b1a49fade926b6dd55494a4f65cc6bc089816c16a79` |
| `B/reviews/focused-regression01/LAUNCH.py` | 861 | `092cf2801485ba5452f9b4a1c3a924304e59ac97aaf1988d67b3867f2ba4d5b8` |
| `B/reviews/focused-regression01/CLASSES.tsv` | 4 | `88d5b0b1bb06a7795ea6f90e52c642c532add8d41abfa0c0671abc640930f810` |
| `B/reviews/focused-regression01/METHODS.tsv` | 21 | `eed756d95856424716cdce0c7b1dafd3986acd6636b45ea95933ea5e8a1c22b5` |

The complete rationale and identity packet were also read and corroborated:

- Author `RATIONALE.md`: `6c4753f9236b45aa090cc44d7f109f951847f60bd24a8bd2968f5e872a627b08`.
- Author `SOURCE-ONLY-DATA-CHECK.json`: `7f24aa384e579a924bdac62fec19013ac50afdec7420e2c77bd6ada192614a5c`.
- Independent sibling `SOURCE-DATA-CHECK.json`: `b524fd69a1dd968929ab14ec47b69b6c13aa6938211ba5f3b948d9cef2d5d520`.
- Earlier scope-only `PRE-PROPOSAL-REVIEW.md`: `2bef95fd3d6538960a0b78c6d99a919be3175ae001abf042d41e8317d0414bab`.

## Exact scope and test-oracle checks

One wrapper build requests, in order:

1. `:core:database:desktopTest`: twelve existing biometric freshness methods.
2. `:feature:credential:desktopTest`: six existing SaveFreshness methods and the
   two specified Draft methods.
3. `:app-desktop:compileTestKotlinDesktop`: compilation only, **zero Desktop app
   testcases**.

One original wrapper stop follows build intent, including build failure. There
is no discovery command, second build phase, new test declaration, provider
producer/consumer, capacity7, all31/all166, shared/GUI Test, Android/native/device
work, packaging, or duplicate static analysis.

The twenty literal selectors match the init, both ledgers, credential review's
exact eight recommendations, and the database review's twelve-method class.
Credential class-wide filters would execute24 methods; this proposal does not
use them. Independently rechecked current source and retained Linux03 XML:

| Class | Selected / source declarations | Current observed source SHA256, NOT a binding |
| --- | ---: | --- |
| `BiometricUnlockFreshnessIntegrationTest` | 12 / 12 | `f25cd292184e1fca7d0cbb3d384dac66156683e5bc9f093876cfd3b487d85b99` |
| `CredentialCustomFieldSaveFreshnessTest` | 6 / 11 | `68eb476cd47b9cda26c86b1ecb2ce9e64f167770291a818beb41400e96953819` |
| `CredentialCustomFieldDraftTest` | 2 / 13 | `b45ded2bb7e092e73975f7b7d4e77c8e649ae72f1b7f07db9445b47559c176c2` |

Every selected declaration and historical `(FQCN, method + '[desktop]')` XML
case occurs exactly once. These current source observations reflect root's W
integration; earlier pre-proposal hashes must not be reused as new bindings.
Every proposed `source_sha256` cell remains **UNBOUND**. Historical XML identities
remain those sealed in the pre-proposal review and independent data report;
they are not new-source runtime credit.

The existing XML collector preserves before parsing and requires exactly three
expected paths, exact FQCN/name multisets and suite names, counts12/6/2 and zero
failure/error/skip attributes or nodes. Duplicate, extra or missing cases cannot
meet success. Compilation failure with missing XML means partial/unstarted work,
not twenty executed passes or failures. Real in-memory Room/Desktop crypto and
synthetic key-store/credential fixtures retain their prior review qualifications:
no failed-CAS forcing, rendered behavior or physical biometric proof.

## Graph and compiler challenge

The new init rejects different requested targets, dry-run, exclusions,
continue-on-failure and CLI test-filter overrides. It requires exactly the two
enabled Test tasks with the fixed method include sets, no excludes, fail-on-empty,
one fork/worker and no ignore-failures. Ordinary JVM Jar/KSP/resource and Kotlin
configuration-check prerequisites are not blanket-rejected. Guarding task names
rather than whole paths avoids the concrete `designsystem`/`sign` false positive.

The required compiler is enabled and has expected type
`org.jetbrains.kotlin.gradle.tasks.KotlinCompile_Decorated`. The bounded retained
JSON `B/runs/linux-android-compile01/reports/init-01.json` was independently read:
4125 bytes, SHA256
`7130a1eb3fbe8f8be2592d1241c7ac86551f3c102c8b4d542993418dc388ca96`.
Both AndroidMain and AndroidDeviceTest compiler graph rows use that type. Current
crypto/Desktop source uses the same Kotlin Multiplatform alias/version2.4.10;
Desktop declares `jvm("desktop")` and desktopTest compilation. Native bridge Exec
wiring is conditional on macOS/Windows, not Linux. Relevant module/root wiring
and the author's six wiring-source identities were corroborated as inert source.

**That is an expected same-plugin type, not an observed Desktop task graph or
compile result.** Exact-type mismatch must refuse; no guessed class, alternative
compiler, discovery task, broadening or automatic retry is authorized.

The bounded log protocol requires one graph and exactly three selected task-state
records. Each selected task must report executed/didWork true, skipped/upToDate/
noSource false, and null skipMessage/failureType. The task packet binds the log;
the phase and outer bind the saved task packet. Bare task headers, requested
selectors, omitted/disabled compilation or unrelated XML cannot claim success.
These action rows add zero testcase credit. Actual realized-graph and semantic
reconciliation remain future root-owned work.

## Complete outer and retained safety mechanisms

This proposal supplies the complete new outer, not an incompatible or unwritten
prerequisite. Linux03 inner/init bases retain SHA256
`4ceaef097a1db97b673e7233aac5ff77acf817d298c2aa8adc33ec86be075c79`
and `6e333500d4a9d56599bd22417d89005d26940240228b1f0710773f5f441d0f45`.
The new inner's Files, PID/mount preflight, resource and cumulative-settlement
blocks are byte-identical to that basis. Original stop-finally, wrapper/JDK
verification, direct-child pidfd signalling, pipe drainage and preserved evidence
retain their source contracts. No old helper is imported.

The current GUI03 outer is a **textual** raw-transport basis, SHA256
`24560eb22ee51f3c743476213ed6a9b15acd748566156090cfd66d2b6be8273e`.
All non-main top-level functions are byte-identical except the two reviewed OID
capacity literals. This includes source transport/materialization, original
FD/pin checks, lock authority, supervision, mount guards and snapshot cleanup.
`screen()` is also byte-identical to the accepted Detekt03 source block, SHA256
`b7e227cdb7accac627d70971d9295125cbefd689aaf9722f683e127ad115ccba`.

The new outer/inner align on pid+mnt parent namespaces, two argv namespace values,
preflight and ownership records. GUI tools/directories/results and obsolete net
fields are consistently removed; no static Git-index proof is fabricated and
no T/index mount is added to the Test workers. Original fresh-source raw object
framing/order/count/digest/two-pass transport remains. Current-T-only path binding
is required; retired Git storage and old pins cannot be borrowed.

JDK17, checked-in wrapper, no-daemon/one-worker/no-parallel/no-CoD, strict
dependency verification, disabled build/configuration caches and SDK/JDK auto-downloads,
in-process Kotlin, private worker HOME/tmp/JNA/SQLite/XDG roots, and clean worker
JVM/provider environment remain. Resource floors, bounded inventories/logs/XML,
original source-before/after and descriptor-finalization checks remain.

A completed test/compile failure can allow exact new-R cleanup only after the
original stop, settlement, source and evidence obligations and actual terminal0/1
are established. Failure to satisfy those safety predicates, cancellation,
terminal70 or descriptor uncertainty stays HOLD. The complete allowlisted
original snapshot and mount recheck precede deletion; identities and parent fsync
remain required. A preterminal JSON cannot attest a later close or actual exit.

**GUI03 is terminal70, zero tests, current-R HOLD.** Its actual execution is not
successful mechanism evidence and is neither traversed nor retried here. Source
block reuse does not discharge any consumed instance's obligations. Nonexhaustive
host screening, generic-interpreter blind spots and cooperative-root assumptions
remain explicit, not global-idle/no-escape or aggregate containment guarantees.

## Findings resolved and remaining qualifications

1. **Original budget omitted wrapper-check time.** Initial4520/920 omitted the
   retained180s wrapper-only STOP source scan. Final4700s prebuild and1100s reserve
   cover nominal3600 build +20 abort tail +180 wrapper check +600 original stop
   +120 cumulative settlement +180 source-after. The600s stop includes its own
   20s tail. The inner consumes/echoes the original5250s outer work deadline,
   rather than resetting it.
2. **Overbroad resource refusal.** `assemble*` became exact lifecycle `assemble`;
   ordinary resource assembly prerequisites remain eligible without relaxing
   exact targets, Test filters, action-state or XML success requirements.
3. **Current-source capacity coupling.** Root's requested3500-member maximum was
   paired in both inner and outer with both144KiB OID input/capture guards:
   `3500 * 41 = 143500 <= 147456`. Exact stdin length/pin/digest remains; raw128MiB
   stream and32MiB per-file limits are unchanged. This is not a final member
   count or authority for further automatic growth.
4. **Compiler/cancellation evidence must not be overstated.** The finalized
   rationale explicitly limits the compiler basis as above. Retained750s outer
   cancellation drainage can truncate worst-case wrapper-check + stop +
   settlement after arbitrary cancellation. Full stop completion is not
   guaranteed; uncertainty must be preserved as HOLD, never normalized into
   cleanup or success. Metadata/resource/marker/evidence/syscall time is
   cooperative; these allowances are not a hard shutdown-time proof.

## Binding fence and actual outcome

Commit, tree, exact source count, manifest, all class/control hashes, current
store, tool/input/device/original-lock/parent/namespace and genuine request/
instance-review bindings remain outstanding and UNBOUND in this packet. Root
alone may reconcile the post-integration source/publication freeze, bind, adopt,
admit, execute and clean. An altered bound helper needs exact fresh review;
this report is not `INSTANCE-ACCEPT.json` and supplies no such approval.

PVU007 STOP, PVU011 NO RETRY, PVA029 failure/no automatic retry, G7/G8 CLOSED,
native refusal, occupied1017001, all consumed runs and prior HOLDs remain intact.
No source/tests/helpers/T application, build/test/Git/CI, helper execution/import/
AST, network, process/SDK/runtime or broad sibling probe, central-ledger edit,
cleanup or new agent was performed. Bounded source/evidence readers completed;
only reviewer reports were written. **New executed cases: zero. No new wrapper
stop obligation, compile pass or execution/cleanup acceptance was created.**
