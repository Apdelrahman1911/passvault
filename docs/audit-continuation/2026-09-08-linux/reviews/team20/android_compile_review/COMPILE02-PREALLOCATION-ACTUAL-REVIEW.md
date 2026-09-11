# AndroidCompile02 — consumed preallocation refusal

Reviewer `/root/android_compile_review`, 2026-09-11.
**RECONCILED_CONSUMED_PREALLOCATION_PARENT_REFUSAL.** No Compile02 replay,
compiler/test credit, runtime cleanup duty or next-run admission.

B = `W/docs/audit-continuation/2026-09-08-linux`,
W = `/root/projects/PassVault/passvault-linux`.

## Exact actual

| Artifact relative to B | Independently matched SHA256 |
| --- | --- |
| `reviews/android-compile02-outer/EXTERNAL-RESULT.json` | `e7f4e5d75ccc8cf92fced10d4c187fa21eefa7db3f67ee13702042881d1f602e` |
| adjacent `ROOT-EXECUTION-ADMISSION.json` | `bfb2edbe2d228687ebdcc0ea85d54fe27ef03bb7effe277fb54dbe191742f584` |
| adjacent `PARENT-DRIFT-DIAGNOSTIC.json` | `2362e628c0be2cf4bc07a716bfa568bb8703675db01199227ac404eafe850e62` |
| `requests/LINUX-ANDROID-COMPILE-02.json` | `317f9ce411b57c1755bbc333911e5081d93d7d788c2ffae4014ab69570d2928e` |
| own `INSTANCE-ACCEPT.json` | `218147c81a9afdf7c353ea7e9f3205109f2102626fd1b063d686b2a70b47de85` |

Command/argv, cwd, login=false, request, approval and source-hash bindings
reconcile. Original terminal **ed15fe / exit70 / no session** contains exactly
`RuntimeError:original parent mismatch`, status HOLD, children=[],
inner_started=false, resource_points=[], cleanup=NOT_ATTEMPTED, no allocation
record, and equal entry/under-lock caller identities. The JSON records and
embedded terminal JSON have no duplicate keys.

The admitted outer remains SHA256
`093680180fbd4336a12ea4a29b4a665bcfac8ffae78765df25e95b759e2cccae`.
Focused unchanged control-flow reconciliation, not another source review:

- Lines 862–874 first verify/acquire the **existing original lock**, then
  compare all 20 admitted image records with their full pins/hashes. Reaching
  line 876 implies those checks passed. This is source-flow evidence, not a
  separately serialized per-image intake transcript or perpetual continuity.
- Line 876 compares parent dev/ino/uid/mode. The first comparison is workspace
  parent; its original failing FD values are not serialized.
- `authority()` and the first resource watch follow at 877–878. R/E absence
  checks and allocation follow at 885–888; index copying, raw Git transport,
  namespace/inner children and Gradle are later. None was reached.
- The exception latch writes CANCEL only if E has been allocated/opened as an
  owned evidence root. Here it only records the reason; the receipt goes to
  stdout. The finalizer has no child to supervise and attempts original-FD
  closes before exit. Terminal70 settles the original foreground invocation;
  its preterminal receipt alone does not prove every close syscall succeeded.

Thus **this invocation allocated no R/E, ran no Gradle/compiler/test, and
created no wrapper-stop or run-directory cleanup duty**. NOT_ATTEMPTED cleanup
is appropriate here, not a cleanup failure. No absence of arbitrary paths or
held runtimes, host-wide idleness, resource-floor satisfaction, or successful
later store/alias/SDK guards is inferred. Do not perform an extra stop,
cleanup or Compile02 retry. Preserve its consumed request/approval/evidence.

## Actual directory drift, not an inferred cause

The later original read-only diagnostic is terminal **877da5 / exit0**. Its
stable FD/path records agree with each other and differ from the sealed
request and original 04:30:13.875281Z point as follows:

| Directory | Sealed inode | Diagnostic inode at 13:26:31.748858Z |
| --- | ---: | ---: |
| `/root/projects/PassVault` | 498323 | 580 |
| coordination lock parent | 661121 | 51356 |
| `B/runs` | 642474 | 54473 |
| publication `T/.git` | 1080546 | 51994 |

All dev/uid/mode/nlink fields remain equal; each four-field identity comparison
fails on inode. These are **later point facts**, not the original failing FD
records. They establish genuine metadata mismatch, not restart/remount,
replacement mechanism, historical continuity, a source-code defect or a
license problem. The correct original-lock/parent guard must not be relaxed.

The unchanged static04 request (`6cea7c172cdb7b97d7eec1a954d561baf4d06fa8daf2799a92d271b423728450`)
and regression01 request (`88da235c80fb4f6081eab0d13e255096b82769bcb9ad0e40d72dd022db3bb302`)
contain those same four old directory identities. Static04 additionally repeats
the old T/.git inode in `git_inventory_binding.metadata_directory`. Neither
untouched cohort is currently execution-admitted.

## Minimum fresh requalification proposal — no execution authorization

1. **One separately admitted root metadata packet**, under newly established
   audit quiescence/freeze and the same existing original lock: no-follow,
   stable FD/path directory pins for the four-parent union; current caller
   namespaces; exact original-lock full8 equality; appropriate owner/mode/device
   checks. Observe, do not guess, device values. No lock creation/replacement.
   Refresh affected standalone-store topology/redirect/exclusion absences and
   supporting T/T.git directory relationship; old directory continuity is not
   repaired by copying four new inode numbers.
2. Cover **all consumed directory projections**, not just `directories`:
   T/.git metadata_directory for static/Android, and Android's separately pinned
   **SDK root directory**. The diagnostic did not observe the SDK root and
   Compile02 never reached its store/SDK authority guards. SDK metadata leaves
   are not proof of SDK directory identity. This adds directory metadata only,
   not SDK copying/install/license action or a new source/index census.
3. Carry the exact accepted C18 SOURCE/index/config/tool byte identities and
   existing index semantic proof forward where their pins/hashes remain fixed
   under root's maintained freeze. Compile02's successful intake supplies a
   recent unchanged-image check. **No SOURCE recapture, index/Git replay, or
   repeated unchanged source/index semantic review is needed.** Mandatory
   same-image/alias/store checks in each admitted launcher remain intact; any
   changed regular-image identity is a new HOLD, not automatic reuse authority.
4. Preserve old sealed packets, then root may propose **metadata-only renewed
   requests/approvals for the unconsumed static04/regression01**: project fresh
   full5 pins into each existing schema (static four-field, regression five),
   refresh repeated metadata fields and any observed namespace delta, retain
   all unchanged source hashes, and obtain genuine counterpart delta review.
   Reestablish coordination and check fresh R/E-name absence through qualified
   parents before each invocation, without descending/adopting old held roots.
5. Any Android continuation must be a **distinct, separately reviewed
   Android03** with new run/R/E/request/approval identities and corresponding
   external-control hash chain, not edited/replayed Compile02. Preserve the
   existing target/26-name/source/resource/cleanup guards and C18 raw snapshot;
   do not recapture SOURCE merely to include new external controls. Root must
   freshly seal/admit it and reestablish quiescence immediately before launch.
   Another mismatch is HOLD, not permission to drop inode or original-lock
   equality or automatically retry.

Activity: named bounded W evidence reads, one admitted outer hash plus focused
control-flow text, JSON/hash/field comparisons, and this own new report only.
No helper import/evaluation/AST/execution, live T/Git/tool/SDK/process/runtime
probe, cleanup, request/control edit or central-ledger change. All older
consumed/held/STOP/NO-RETRY/CLOSED/native-refusal/protected-ref/publication/
build1017001 fences remain. Zero new cases or closure credit.
