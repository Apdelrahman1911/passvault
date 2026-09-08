# Independent Windows native-01 result reconciliation

Reviewer: `/root/native_review`; execution owner: `/root`; helper author:
`/root/native`. Disposition: **OPERATIONAL FAIL; CLEANUP HOLD; 14 CASES UNSTARTED**.
This is a review of retained evidence as data, not an invocation or retry.

## Identity and retained evidence

- Actions run **34284083351**, attempt **1**, one `windows-2022` job; terminal
  failure at **2026-09-08 22:06:09 UTC**. The evidence-upload step succeeded.
- Activation commit `a1fbdf0d5f0663225a459838d37b072a65e334f6`, tree
  `0e5144c44fc79f052d0131808b887f8440e0a2cb`; source parent
  `1a57239f74af792b64e631f51a79e1c0bf83816a`, tree
  `7375659b90f1c622b7fa29a19dcc8b268e4ed340`.
- Artifact **10078766432**, 9,684-byte archive, digest
  `cb6ff2d5bcabf3025f04157dda42f1051a1d34d698732bd25c679c6b169f3fc9`.
  Root decoded it in memory and retained 16 files / 21,682 bytes under
  `../../runs/windows-native-01/`. This reviewer did not redownload the archive:
  every retained artifact file was independently hashed against root's receipt;
  the receipt's archive digest matches the retained Actions artifact metadata.
- All raw logs, retention receipts, journal, allocation/source/request/toolchain
  captures and four root transport/API records were read. Exact reviewer hashes
  and machine-readable reconciliation are in `RESULT-REVIEW-01.json`.

The observed Git identity log matches both accepted commit/tree pairs. Its diff
contains only the request path; the status log is empty. Allocation records bind
all 12 authority/source input hashes to actual pre-command Windows checkout
bytes and the accepted committed blobs. Uploaded request/source-binding JSON was
reserialized with sorted keys: semantic equality, not equal serialized hashes,
is the correct comparison to those committed JSON files. The final post-test
source rehash was **not reached**; do not claim end-of-run drift proof or a
whole-project checkout-EOL revalidation.
The download receipt's `source_commit` names the runtime activation checkout
`C`, not parent `S`; the result and this review preserve the explicit distinction.

## Commands and actual cases

There are **five command intents and four completed exit-0 records**:

1. Git activation/source identity — completed, owned Job empty.
2. Git request-only diff — completed, owned Job empty.
3. Git clean-source status — completed, owned Job empty.
4. `cmake --version` — completed; CMake **3.31.6**; owned Job empty.
5. The exact admitted CMake configure — launched, then operationally failed at
   the owned-descendant drain. No completed configure exit record was retained.

Configure stdout reports MSVC **19.44.35228.0**, C/C++ compiler-identification and
ABI probes, and normal configuration/generation completion. **Compiler probes
did execute; this is not “no compilation work.”** It is also not a product-target
build, effective product hardening/unwind proof, linked ABI artifact or a test.
The helper reads the parent process exit code before the drain but records it
only afterwards (409–425). Therefore normal stdout cannot upgrade the configure
command to an observed exit-0 PASS.

No product-target build, CTest inventory, CTest command, case XML,
`compile-configuration.json`, or native-artifact hash file was reached/retained.
All **14 planned cases are UNSTARTED**: actual executions **0**, passes **0**,
failures **0**, skips **0**. A failed Actions job is not 14 failed native tests;
the four read-only command successes are not four regression cases. The helper's
`passed_cases_machine_scored: 0` alone would not establish these totals; the
journal, command order, early failure and absent downstream evidence do.

The toolchain capture identifies x64 Python **3.12.10**, runner image
**20260830.290.1**, requested installed SDK **10.0.26100.0**, same-directory
CMake/CTest binary hashes and a WebAuthn-header hash. It does not establish
effective product `/EHsc`/warning/hardening flags or successful target linkage.

## Failure, settlement and cleanup challenge

`Owned descendants remain after command` was emitted at helper line 416,
after its ten-second drain. The command's `finally` made the one permitted
exact-Job termination call; the subsequent `command_aborted` event, settled
configure-log receipt, and final `owned_workers: 0` support owned-worker
settlement. There is no automatic retry, second command, name-based kill or
evidence of a termination failure. This is evidence of the admitted Job's
accounting, not an independent global process census.

Cleanup then refused a reparse point at line 264 while gathering all retained
descendant handles (449–478). The helper performs that complete gather **before**
its first deletion loop. The journal contains no `delete_intent`,
`delete_root_intent`, or `cleanup_settled` event. Thus no generated-root cleanup
deletion was attempted by this helper on this path; removal/absence was not
proved. The failure `finally` takes its handle-close paths, but not every close
return value is retained; do not claim an independent all-handles-closed census.
No recorded Job-close failure changes the observed owned-zero state. Final
filesystem disposition remains **HOLD**. The hosted job's terminal state and
successful artifact upload are not proof that the generated root was deleted.

The logs do **not** identify the lingering child process/image/birth identity,
the rejected reparse path/tag/target, or retained generated-root size. Do not
invent a diagnosis such as “MSBuild node,” “compiler telemetry,” or “junction,”
assume harmlessness, follow that target, relax containment, or attempt recovery.
These are concrete observability limitations, not confirmed PassVault product
defects. `/root/native` independently challenged and agreed with these
interpretations after reading the same compact evidence and exact helper.

All five logs are complete/untruncated by their settled full-file and retained
hash/length records. Recorded Windows resource samples stayed above the
admitted floors; this is sampled evidence only, not continuous drain/cleanup
resource monitoring. Neither resource exhaustion nor a log/time limit is the
recorded failure. The failure was preserved rather than relabeled a test pass.

## Ledger and authority implications

PVA-036/037 remain target-verification-blocked; their planned native cases did
not execute. PVA-010 real native concurrency, PVA-014 displayed prompts,
historical red controls, fixed-output KDF compatibility, production allocator-cut
injection and genuine hardware boundaries remain unchanged. No new product
finding or closure is established. Original confirmed **19/25**, all confirmed
**22/37**, original suspicions **2/12** are unchanged by this attempt; the eight
PVD design explanations/owner decisions remain separate.

Root may record this completed independent reconciliation when deciding the
build-slot disposition, but **must preserve Cleanup HOLD**. This review does not
release the slot itself, authorize another attempt, change the accepted helper,
or admit a recovery namespace. Any future corrected-source proposal requires a
new independent admission/root decision; unchanged automatic retry is forbidden.
PVU-007 STOP, PVU-011 NO RETRY, PVA-029 recorded FAIL/no automatic retry, G7/G8
CLOSED, protected branches and all non-publishing fences remain untouched.

No helper was imported/executed by the reviewer; no compiler/test/CI command or
native/provider operation was run locally. Only compact permanent reports were
written; no temporary archive, build output/cache or worker was created.
Gradle stop is not applicable to this native-only attempt and discharges no
historical obligation. VPS point sample during review: about 28 GiB disk free,
41.0 GiB RAM available; no process termination or unrelated deletion performed.
