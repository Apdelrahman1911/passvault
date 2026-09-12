# Real-JNA adapter V2 — independent source review, two corrections pending

2026-09-12. Reviewer `/root/native_review_c20`, author `/root/native_author`.
**HOLD_V2_SOURCE_PENDING_TUPLE_SORT_AND_ABSENCE_BOUNDARY.** This is a completed
bounded glue review with two small corrections requested, not source/instance/
actual-graph/execution/cleanup or recovery acceptance. Resume with a narrow
corrected delta; do not repeat unchanged framework review.

Author packet `native_lifetime_author/resumed01/runtime_adapter01/v2/`
`REAL-JNA-ADAPTER-V2-PACKET.json`7147B/SHA256
`9e791134a90752abded4f620a1149356a860b384e6e78f12fe187fc5a9cc2164`.
Helper119975B/SHA256`18abcd93e699365ccc7489d0a626ee85e393d6ee466ac177fce271256fa47e32`.
All17 declared source/predecessor/component/review/receiver/SOURCE tuples and
packet matched. Complete graph-to-runtime and preserved-V1-to-V2 diffs reconstructed
exactly. Accepted producer body8371characters and XML V2 body2155characters each
occur literally once; runtime init8448B/47dc1f6c...2b755 is byte-identical.
Existing graph/profile V2, actual Sfix binding and real-JNA components reviews
are reused. The full new gate/prepare/call/final-retention paths were read.

## Required corrections, independently challenged

1. Author identified the preproducer mixed-order false HOLD. `current_tools` is
   accumulated under WindowsPath ordering (case-insensitive), but expected tuple
   strings are sorted case-sensitively. Identical mixed-case path sets can compare
   unequal. Sort current string/hash/size tuples as well as expected tuples;
   keep exact content/path equality, duplicates and all five-tool checks.
2. Independent reviewer found optional-XML catch too wide. At V2 lines1567-1572,
   `run.open_output` remains within the FileNotFoundError catch. It repeats
   `plain_path(directory.parent)`; a parent disappearing AFTER initial successful
   metadata can therefore be classified NOT_CREATED instead of later-failure
   HOLD. Move the original opener to the `else`, immediately before its owned
   handle try/finally. Only initial no-follow metadata absence may be optional;
   no later failed opener, acquired handle or uncertain namespace gets absence
   credit or retry. This narrows failure handling; no new race-proof claim.

These are unexecuted validation-component counterexamples, not product families,
test failures, new application cases or closure changes. V1/V2 must be preserved.

## Glue checks with no further blocker found

- Same actual Sfix/shared SOURCE binding; four fixed retained graph/result/toolchain/
  genuine-review members hash/size/path/duplicate-key checked before allocation or
  producer. Real graph zero-case/full source/seven parents/stop/Job/whole-cleanup,
  product and control/request facts remain separate. All actual evidence paths/
  hashes and init graph SHA are UNBOUND, not invented acceptance.
- Current image/Python/JDK-release/five tool contents gate native producer first;
  producer's own accepted CMake/SDK guards remain. Generated-environment/full
  Gradle fingerprint is checked after prepare and before wrapper arm. Old inode
  identities and runtime paths are not reopened or treated as authority. SDK
  remains a path-only comparison qualification.
- Producer-before-Gradle, original stop-before-ambiguous-launch and ten-parent
  ceilings match. Exactly nine fixed DLL/worker properties use fresh distinct
  owned roots. One Test selector, before-actions actual graph comparison and
  native-provisioning refusal remain; no CTest/provider/packaging expansion.
- Final-only copied graph/DLL/log receipts and settled parent markers precede
  XML scoring and original whole-root cleanup. Missing XML after failed parent
  makes NO_CASE_INFERENCE, not zero cases. Retain exact full bounded XML before
  strict UTF8 oracle; XML pass alone cannot set overall operational success.
  Failed oracle may permit safe cleanup but keeps failure. Stop/Job/source/log/
  handle/cleanup/cancellation errors remain conjunctive blockers.
- Receiver interface matches immutable2327B/SHA256
  `bdc67cd9a1790e520d57ae4c0006dbb249d809ef1226c6b25da54bb3057e2d16`:
  scripts/audit/windows_real_jna_01.py; windows-real-jna-01 request and suite;
  evidence passvault-windows-real-jna-01-<run>-1-evidence; evidence_owned=true;
  only compact JSON/JSONL/log/XML;1500/1560/1620s within1800s workflow. Attempt1
  and exact selector retained. This confirms interface, not runnable admission.

Root-owned actual SOURCE acceptance, original successful graph/cleanup/independent
actual review, exact real-JNA binding/control/request/nonce/instance and fresh
slot/resource/platform/trigger/cleanup admission all remain. No automatic retry,
old Windows4 root recovery, cleanup-only CI, restricted native workflow or hardware
claim. PVA010 managed-pre-entry/real FFI only, not PVA037/native-active/provider/
Hello/sanitizer/package/hardware closure. All standing STOP/NO-RETRY/CLOSED,
protected refs/tags/dependencies/versions/identities/Store/build1017001 preserved.

Activity: named source/evidence reads, inert hash/JSON/full-diff/literal comparisons
and exclusive review write only. No helper parse/import/compile/run, Git/network,
build/test/native/SDK/cache/runtime/process/cleanup work or workers. Zero execution,
application cases, product findings/fixes, cleanup successes or closure credit.
Paused at root's AndroidGraph01 quiet-window request; resume only after release.
