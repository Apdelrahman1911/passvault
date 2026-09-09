# Closeout controls v2 — exact bounded source acceptance

Reviewer: /root/android32. Controls author: /root/build_config.
Review point: 2026-09-09T02:53:31Z.

**ACCEPT_BOUNDED_SOURCE_FOR_SEPARATE_ROOT_ADMISSION.**
This is not execution permission, a launcher/actual-instance acceptance, an
executed control pass, a closed cleanup obligation or application evidence.

## Accepted exact tuple

| Input | SHA-256 | Bytes / physical LF |
| --- | --- | --- |
| ../build-config/CLOSEOUT-CONTROLS.py | bdee7e285f45f78e56d99fc6ef122767de00400d952f1e68c1d47b5d9bf35a53 | 40,149 / 838 |
| ../build-config/DESIGN.md | 1e23c7432b9ea297e96ab56e7bd4742e5019af5c0726ce6ad28a2bdc8de3c8bb | 22,609 / 296 |
| scripts/audit/linux_database_closeout.py | b16774ff6d0c14ece40dbe2c75684a2b07e7c90c9da4373ec0a6e12e87cc7bec | 59,183 / 1,094 |
| ../linux-closeout/PLAN.md | cffc69357a0c4f0c5e46fd4d282439ece513eb2f166074c05f2ab4e679f8a57d | 29,298 / 474 |

V1 and its REVISE record remain unchanged:
CLOSEOUT-CONTROLS-V1-SOURCE-REVIEW.json
(5f7021de07eb6dfca45d593ceb0c20d9b4353931a4574286dee28ccfde141adb),
paired MD
(61568e00dbd6f2b16d9dbf9d8b8d99bc7d07175163f4b17f9e7da6bcf8d4ca7c),
../build-config/CLOSEOUT-CONTROLS.v1.py.txt
(acb44cae0c8ac31729f043477fa19683b20487d13fcef74965f1dd71f96d79c4)
and DESIGN.v1.md
(ab4c9a02873652b9a60482fab2f522aff33202ab1ee075f73271e986b642cc20).
Those source rejections were not executed failures.

## Independent successor challenge

The complete literal v1-to-v2 source and DESIGN diffs were inspected. The only
code change is the B10/B11 block at703–718; its full read/mutation/rejection and
close/result context through744 was reread. The prior full832-LF controls read
and exact unchanged-byte comparison support the remaining source; this is not a
new whole-helper semantic review. The complete296-LF successor DESIGN was read,
and all four accepted tuple hashes and both v1 review hashes were rechecked.
No source was imported, compiled, parsed as AST or executed by this reviewer.

- **LCC-ORACLE-01 source-resolved.** B10 now performs a real same-content pwrite/
  fsync, reads the retained original FD, checks unchanged actual bytes and hash,
  and requires an observed pin change while retaining the earlier expected pin.
  Removing the helper's pin comparison would accept that unchanged digest and
  cause the control's required rejection to fail. B11 separately changes actual
  bytes, checks real readback and injects the current pin against old captured
  bytes; removing the digest check would accept it. The two guards are now
  independently discriminating in source, not merely combined drift predicates.
  Missing observed metadata drift is HOLD/no retry. No expected output is mocked
  into a pass and no helper code or case count changed.
- **LCC-DOC-01 source-resolved.** DESIGN73–78 now requires fresh admission using
  the retained verified original Linux coordination lock. It does not authorize
  recreation, replacement, adoption, historical macOS identity or consumed-run
  permission.
- **LCC-DOC-02 source-resolved.** DESIGN285–289 keeps handoff22/37 historical and
  current22/38 after root's separately registered PVA-038, with no extra closure.
  Original19/25 and conclusive suspicions2/12 remain separate.
- **Reporting qualifications incorporated.** DESIGN174–176,210–218,257–267 now
  explicitly preserves the untested nonempty pending-signal branch, confirmed-
  after-proof/fsync removal accounting, absent per-fixture external original-pin
  census, required individual results and separate external completion proof.

All other bounded source-supported properties and failure paths in the v1
review remain accepted with their exact qualifications. This source rebind
introduces no process, new deletion authority, namespace reuse or product fix.

## Interface and continuing admission obligations

The sole callable remains:

    run_controls(*, scratch_fd, scratch_path, original_scratch,
                 source_bytes, emit, cancelled)

Only the fixed NEW scratch
/root/projects/PassVault/audit-runtime-linux-closeout-controls-01 is in scope.
The outer alone exclusively creates/seals it and owns its borrowed original FD,
parent/name witness, final empty-original removal and durable external evidence.
The required original_scratch fields are exactly dev, ino, uid and mode; mode
must be an owned0700 directory. Neither this review nor an empty existing name
supplies its original creation witness or authorizes adoption.

There are28 planned case IDs,13 journal and15 bootstrap/rehash/HOLD, all still
**0 executed**. No previous38-control count, assertions, descriptor counts or
journal-event count may be substituted. A full control pass needs all28 distinct
case bodies/results in exact order, original-only fixture cleanup COMPLETE,
durably retained effects and independently accepted outer settlement/exit.
The terminal is not a case-results array.

The outer launcher and its independent review remain separately required. Root
must bind exact harness/DESIGN/helper/review/launcher bytes; the retained original
Linux coordination lock and no-overlap facts; prospective one-shot admission;
original evidence/scratch witnesses; JDK/wrapper obligations if any later build
is admitted; launch12GiB/25% and running8GiB/20% floors; narrow process-memory,
wall-time and cleanup bounds; nonthrowing monotone cancellation; and actual
source/descriptor settlement. This callable starts no Gradle/build/test worker
and does not discharge another run's wrapper-stop obligation.

## Mandatory limitations

- Only selected exact Journal/Directories/recheck and bootstrap callback bodies
  can run. No helper main/admission/Guard/Forest, actual lock/closeout/runtime,
  old runner or deletion helper executes. The relocated callback uses supplied
  closure globals. Component controls are not main/bootstrap/under-lock proof.
- Real short file I/O and pin/hash checks are distinguished from synthetic
  CANCEL, pre-expired start, injected zero/EIO and B11's seal-input fault. The
  real elapsed-time cases are not hard syscall deadlines or signal-delivery
  tests. No hardware or power-loss durability is established.
- Fixture originals are source-bound in-memory witnesses, not a separately
  retained external allocation census. Later empty scratch cannot reconstruct
  every birth/name. Lost originals do not permit pathname-only recovery.
- removed_names counts only completed destructive calls plus original/link proof
  and parent fsync. A partial failure may have additional unacknowledged removals;
  HOLD is not a recovery manifest or permission to continue.
- A callback exception, late cancellation, startup/emit/I/O stall, ambiguous
  close, fatal allocation failure, SIGKILL or host loss can prevent receipts or
  finally. Require the separately observed outer result; no automatic retry,
  reuse, stop replay or manufactured terminal success.
- Per-case4KiB/four-name/24-FD and58-dictionary/256KiB sink bounds remain distinct
  from root's host/process resource controls. Cooperative path/pin samples are
  not an atomic hostile-UID sandbox or cryptographic history authentication.

At02:53:31Z workspace free space was27,752,460KiB, /tmp21,152,944KiB and available
RAM41,787/64,311MiB. These are point observations only. This review created only
compact permanent reports, no scratch/cache/build output, process/worker or stop
obligation. Source diff exit1 was expected text difference, not a control result.

Actual root/launcher admission, control execution/result reconciliation and all
real closeout C01–C07 instance obligations remain unfilled by this review. Counts,
PVD decisions, Android/native/hardware gaps, all STOP/NO-RETRY/CLOSED restrictions,
Windows FAIL/cleanup HOLD and publication/candidate1017001 fences are preserved.
