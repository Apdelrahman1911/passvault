# Closeout controls v1 — independent bounded source challenge

Reviewer: /root/android32. Controls author: /root/build_config.
Review point: 2026-09-09T02:41:25Z. **REVISE before separate root admission.**

No execution, import, compilation, syntax check, source AST evaluation, signal,
fixture/scratch allocation or control test occurred. This is not an actual
closeout instance acceptance, a control failure/pass, or an application finding.

## Exact tuple and method

| Input | SHA-256 | Scope |
| --- | --- | --- |
| ../build-config/CLOSEOUT-CONTROLS.py | acb44cae0c8ac31729f043477fa19683b20487d13fcef74965f1dd71f96d79c4 | All 39,748 bytes / 832 LF read as inert text |
| ../build-config/DESIGN.md | ab4c9a02873652b9a60482fab2f522aff33202ab1ee075f73271e986b642cc20 | All 19,295 bytes / 251 LF read |
| scripts/audit/linux_database_closeout.py | b16774ff6d0c14ece40dbe2c75684a2b07e7c90c9da4373ec0a6e12e87cc7bec | Selected definitions and callers, not a fresh full 1,094-LF review |
| ../linux-closeout/PLAN.md | cffc69357a0c4f0c5e46fd4d282439ece513eb2f166074c05f2ab4e679f8a57d | Full inherited contract / 474 LF |
| ../baseline-coverage/LINUX-CLOSEOUT-SOURCE-REVIEW-v1.md | d51f668387791584c80ac418cf0d1716fa85a20ebf21e236ff85ff251b1c7ecf | LC-C05/06 counterexamples and surrounding qualifications |
| ../baseline-coverage/LINUX-CLOSEOUT-SOURCE-REVIEW-v2.md | 8f5ce3844c4fff97200282b8ed16e11887ee3d74a44151a32671f6764b2dd8bf | Full correction acceptance/limitations |
| ../baseline-coverage/LINUX-CLOSEOUT-SOURCE-ACCEPTANCE.md | 9680e82520b0e47e0341ea581ee259cc98a511d841b892ecd031c2e8ecd15286 | Full exact source/PLAN acceptance |

The current helper context read here was lines1–335,380–470,612–701,976–1094:
all selected definitions, complete Journal/Directories, bootstrap callback/main,
admission and relevant Guard/recheck interfaces. Earlier independently supported
helper source review remains adopted with its limits, not restarted or replaced.
Both new files and the subject/PLAN were rehashed; no tool executed their code.

The author's root-authorized permanent inert before-images are
../build-config/CLOSEOUT-CONTROLS.v1.py.txt and DESIGN.v1.md. Their independently
rechecked hashes equal the rejected tuple above. Containing HEAD was
f18995e6633904a6845e09eab8a481635290894d, tree
8948bb6c92b5baaf406f838b48bf8e31064d1195; these new control bytes were worktree
additions, not a reviewed whole-tree or committed-source claim.

## Source-supported bounded properties

1. **No whole helper or old runner execution path.** After the exact 59,183-byte,
   1,094-LF subject hash check, source lines766–778 select nine exact top-level
   definitions and the existing nested bootstrap_tick definition. Lines435–439
   compile only those nodes. The helper's imports, constants, main, admission,
   Guard, Forest, lock/proc/mount probes and deletion bodies are not selected.
   The new harness's own main guard refuses direct use. The selected Journal/
   Directories bodies and their allowed globals were read, not assumed safe
   merely from a hash.
2. **Component evidence is not main integration.** Moving the exact callback
   node to the selected module supplies former closure inputs explicitly.
   Tests can reach its real cancellation/deadline predicates and Directory/
   recheck call sites, but cannot prove main installs it before admission,
   acquires a lock, preserves the main closure, later installs Guard, or safely
   closes a real run. DESIGN34,144–150 state these exclusions. No copied
   predicate, mock phase label or actual under-lock execution is adopted here.
3. **Real journal effects with explicit injections.** J01/J07 require parsed
   real bytes, completed sequence/length/current pin and real short writes.
   J02–J06 mutate the known original via pwrite/truncate/rename/link and require
   the expected prior rejection without append I/O. J05 honestly allows the
   observed rename to select prior-pin versus pathname rejection; it does not
   imply both branches ran. J08's zero return and J10's EIO are injected, whereas
   J09's first seven bytes and J11–J13's postwrite mutations are real operations.
   All negative cases preserve completed state, latch incomplete and separately
   schedule one no-I/O incomplete guard assertion, not a real closeout retry.
4. **Bootstrap effects are finite.** B01 binds/reads/fsyncs real synthetic
   originals; B02–B07 distinguish before-I/O from after-first-read/binding
   cancellation and deadline effects. B05/B07/B13 use real elapsed time; B03/B15
   inject an explicitly expired start. B08 keeps the replaced directory's
   original separate from its newly created replacement. B09–B13 exercise the
   function-only rehash, and B14/B15 check actual journal progress after a
   synthetic bootstrap refusal without calling that refused callback. No OS
   signal, physical durability failure or full actual bootstrap is proved.
5. **Fixture lifetime is original-bound.** The outer-created0700 scratch FD is
   borrowed. Arena retains its own no-follow ancestors; originals get retained
   duplicate/read FDs before fixture writes. The finite aliases are exact
   case-plus-suffix names, with a separate record for a new replacement.
   Expected mutable pins are refreshed only through known fixture operations;
   stable dev/ino/UID/mode and known hardlinks remain guarded. Cleanup checks all
   original pins/names/members/empty child directories before its first removal,
   uses only the borrowed original dir FD, then verifies link transitions and
   fsyncs. Unknown members or ownership uncertainty cause HOLD, never adoption.
6. **Ordinary failure/cancellation retains cleanup.** Source-created live FDs are
   registered by the per-subject OS delegate, not a global monkeypatch. Subject
   close errors/unexpected live FDs forbid fixture deletion even if a subsequent
   known close succeeds. Arena closes all retained original/ancestor FDs; an
   ambiguous close is not retried. Outer signal handlers must record, not throw
   across ownership transfers. Fatal allocation/runtime failures may defeat
   accounting/finally; root must retain external HOLD rather than assume recovery.
7. **Finite accounting/bounds.** There are exactly28 planned cases, J01–13 and
   B01–15, not assertions, journal rows, the earlier38 controls or application
   cases. Each completed case must settle its fixture before the next begins;
   the first HOLD stops later bodies. Fixture caps are4KiB, four fixed allowed
   names per case and a conservative24-FD envelope excluding borrowed/outer FDs.
   Only58 dictionaries are expected for a fully completed run. Outer memory,
   resource floors,256KiB durable sink, no-overlap, timeout and original scratch
   removal remain separate unfilled admission responsibilities.

## Required revision and counterexamples

### LCC-ORACLE-01 — B10 does not independently discriminate the pin guard

At lines704–712, B10 and B11 both change the actual bytes from only to ONLY.
B10 retains both the old pin and old captured bytes. Current recheck_inputs
rejects at the pin comparison, but deleting that comparison would still reject
the changed digest. Thus B10 is a valid **joint-drift** rejection control, not
independent pin-only regression evidence. B11 correctly supplies the current pin
with old captured bytes and therefore isolates the digest comparison.

This is a control-oracle qualification, not a new unsafe helper path or PVA
family. The author independently challenged and agreed. The agreed finite
successor changes only B10's fixture effect: real same-content pwrite/fsync,
explicit unchanged actual bytes/hash, observed changed pin and the old expected
pin. That makes accepting a changed pin observable without a digest mismatch.
A filesystem lacking observable pin drift must HOLD, not retry or invent a pass.
No extra case or helper change is required. Review the actual successor; this
proposed correction is not itself accepted source or an executed control.

### LCC-DOC-01 — original Linux coordination is not forbidden reuse

DESIGN54's broad prohibition on any original lock reuse conflicts with the
retained-original Linux coordination contract. Independently raised by the
launcher integration reviewers and agreed by author/root: distinguish fresh
admission using the retained verified original Linux lock from prohibited
historical macOS identity, adopted/replaced inode or consumed-run authority.
Do not create a replacement lock or silently reinterpret this prose at launch.

### LCC-DOC-02 — historical and current denominators need separation

DESIGN245–246 labels22/37 as current. Root confirmed independently registered
PVA-038 changes the current all-confirmed denominator to38, closures still22.
Keep handoff22/37 historical, current22/38 explicit, and controls delta0.
Original19/25 and conclusive suspicions2/12 remain separate; these are not
readiness percentages. This is accounting correction, not closure credit.

## Required reporting qualifications, not a new cleanup authority

- Source lines295–304 increment removed_names only after the destructive call,
  original/link proof and parent fsync. A failure in between can leave an actual
  unlink/rmdir absent from that number. Label it acknowledged completed cleanup,
  not a complete raw destructive-syscall count; partial failure remains HOLD.
- Per-fixture original pins live in the reviewed callable but are not separately
  emitted as an external allocation census. Successful result plus later empty
  scratch is source-bound execution evidence, not independent reconstruction of
  every original allocation/name. A partial failure with lost original witnesses
  cannot authorize a pathname-only recovery. The author agreed to state this.
- The terminal contains counts, not a case-results array. The outer must retain
  and independently reconcile all28 durable case-result objects, effect starts,
  cleanup outcomes and exact IDs; a terminal PASS alone is insufficient.
- A PASS returned before/through terminal emit does not settle root's borrowed
  FD, scratch parent/name, original lock, final monotone cancellation or external
  exit. The outer must challenge those separately. Callback failure, late
  cancellation, external hard interruption or missing receipt cannot be adopted
  as completion.60s polling is not preemption of I/O, emit or Python startup.
- The synthetic latch is deliberately separate from real outer cancellation so
  HOLD writing can be tested without cancelling its evidence/cleanup supervisor.
  The real pending-OS-signal branch and actual closeout main remain unexecuted.
- Successful fixture fsync is OS-call evidence, not power-loss/hardware proof.
  Same-inode changes during the journal's own append are not authenticated by
  these metadata observations. Cooperatively frozen paths are not an atomic
  hostile-UID filesystem sandbox.

## Preservation, resources and next boundary

Only these compact permanent review documents were added by this reviewer.
An initial read-only path locator named an absent validation directory and warned;
all required inputs were subsequently read at explicit paths. It was not a
build/control failure and no source helper was run to locate anything.

At02:26:57Z, workspace free space was28,362,220KiB and /tmp21,250,656KiB;
MemAvailable42,222MiB of64,311MiB. These are point observations, not admission.
No scratch, cache, build artifact, process/daemon/worker or wrapper-stop obligation
was created. Gradle --stop is NOT_APPLICABLE to this source review, not resolution
of another run's obligation.

V1 remains REVISE / NOT_ADMITTED. Preserve its exact before-images and this
counterexample record, then bind the narrow successor for independent acceptance.
Actual root/launcher admission, any control executions, independently reconciled
results and all actual closeout C01–C07 obligations remain separate. No PVA/PVU
credit follows. PVU-007 STOP, PVU-011 NO RETRY, PVA-029 FAIL/no automatic retry,
G7/G8 CLOSED, Windows FAIL/cleanup HOLD, Android image/native/hardware gaps,
eight separate PVD decisions and all publication/candidate1017001 fences remain.
