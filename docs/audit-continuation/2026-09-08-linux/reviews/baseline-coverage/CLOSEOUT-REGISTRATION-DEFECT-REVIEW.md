# LCL-OWN-001 — closer and new outer registration variants

Disposition: **CONFIRMED, bounded source defect; whole viability remains HOLD.**
Independent reviewer: `/root/baseline_coverage`; subject author: `/root/editor`.
This is a grouped audit-infrastructure issue, not a new product PVA family.
No helper, control, application test, process probe or fault injection ran.

## Exact scope and retained history

Closer v3 source `0bed64d9ecb2ebb3ff7a41d04d8c66240e9c82c161f28f13860b8c54f93f5c02`
and PLAN `bdd2f816592d409c7211375673ba3507375c52af8d41bf0688ab196fe8f0392c`
are retained as `linux-closeout/registration-rejected.py.txt` and
`registration-rejected-PLAN.md`. This reviewer verifies their bytes; author
reports exclusive preservation before editing, not a retrospectively observed
allocation original supplied by this review.

The separate, unaccepted NEW outer draft has source
`1ed2916192f01c1e18b25fc5e46bf9ec15f1a6d3ee9bc067c8ac5afe8f4a7e5e`
and PLAN `53439bfdfa0147a0a9f8685159729a5bcefa210ef28423a430e5775840822920`.
Its review here is only the corresponding registration/unwind path, **not** full
supervisor, protocol, timing, signaling, cleanup or actual-instance acceptance.
The JSON records whether each exact outer input was read at the live draft name
or the author's separately preserved registration-rejected before-image.

Grouping follows the independent `/root/editor_review` controls-launcher report
`CLOSEOUT-CONTROLS-LAUNCHER-PRELIMINARY-BODY-REVIEW.json`, SHA-256
`1f001b44fc5a8dbfd65eaed6115e7247737f76ee2e55e155ec7b6c6cb4310910`.
That report independently confirmed the same two-map partial publication shape
in `/root/storage`'s unaccepted controls launcher. This review does not execute
or reopen that lane's source, or widen into consumed/archived helpers.

The independently accepted disjoint `evidence`/`evidence_files` correction is a
separate narrow result: review JSON
`1b84d746f7483970a5349a963b45d7b68c31e32a70d741f5ace08b4346c1fe5a`.
This later exception-safety qualification does not retract its exact two-field
source result or silently upgrade it into whole-helper viability. The earlier
b167 launcher-contract defect, reviewer miss and historical acceptance remain.

## Independently challenged closer path

1. `Directories.open` opens a directory FD (183-185), obtains/validates its pin
   (186-188), then executes `self.fds[key], self.pins[key] = fd, pin` (189).
   Target stores are sequential. A handled ordinary `MemoryError` on the second
   built-in dictionary insertion can follow a successful first insertion.
2. `except BaseException` locally closes the FD (190-192), but does not remove
   the successful `self.fds` insertion. Assume this first close returns normally.
3. New discoveries occur in bootstrap input reads/bind inside `admission`
   (615 onward; read call621). Main calls admission at1002-1003 before the lock
   FD at1010, journal at1020, forest or deletion. Later non-bootstrap opens may
   verify known registrations but cannot discover missing authority.
4. On this failed admission, `journal` and `lock_fd` are `None`. The error path
   writes the fixed diagnostic to existing stderr, then main finally invokes
   `dirs.close()` (1089). Its reversed FD snapshot (248-255) includes the stale
   numeric value and attempts `os.close` again (252).

This is a conditional source path contradicting the intended once-only
ownership discipline. It is not a natural OOM observation or runtime regression.

## Separate NEW outer path

`Originals.directory` has the same sequential stores at194 and local close196
without unregistration. Discoveries are restricted to admission reads351/449
and explicit original ancestors454. Main admission686 precedes lock692,
first Evidence712 and child launch745. `Supervision` starts with no files/child;
its failure bookkeeping does not open FDs. On this failure main skips all
child/evidence/lock work and reaches `d.close()`851, which pops the stale value239
and retries its close241. The registry pop in final cleanup is too late to
prevent the already-completed local close from being attempted a second time.

## Guards, counterexamples and impact limits

- Before either map publication, failure closes only the local FD. A failed
  first insertion leaves no stale `fds` entry. Normal successful registration
  transfers ownership to final cleanup. Those paths do not establish this defect.
- Successfully registered parent FDs remain legitimate originals and should not
  be removed by the correction. No broad dictionary clear or pin-map reset is
  needed. Failed admission must not be resumed or made into new authority.
- No intervening FD allocator was identified on either inspected single-threaded
  failed-admission unwind. After a successful first close, expected second-close
  behavior is EBADF and an extra descriptor-settlement diagnostic. **No reused
  number, unrelated-FD closure, data deletion harm or product impact is shown.**
- The original MemoryError already requires operational HOLD. This issue does
  not establish a new false operational HOLD, false PASS or cleanup success.
- A later/catastrophic allocation failure can prevent final JSON, tuple/list
  construction or even the cleanup path. No universal MemoryError settlement
  guarantee, hard deadline, safe retry or automatic recovery is claimed.
- Outer/closer code has not been executed, imported, compiled, parsed as Python
  syntax, or tested. The error outcome and EBADF are source reasoning only.

## Disposition and necessary successor work

Root and author independently counterchallenged and accepted this bounded
variant description. Root authorized only narrowly unpublishing the incomplete
FD-map entry before its sole local close, preserving unrelated entries and all
original pins, exact before-images, PLAN qualifications and affected outer pins.
This document records the defect, **not** acceptance of changed bytes.

Each successor needs exact independent source/delta review. Before execution,
meaningful separately admitted tiny synthetic controls must inject the specific
second-store failure around an actual acquired directory FD; record pre/post
registry state, local close attempts, retained parent/pin authority and normal
registration. Do not induce VPS memory pressure. A deliberately reused-number
sentinel may test the component's ownership effect, but would not demonstrate
such reuse on these actual main unwind paths. Existing28 journal/bootstrap
controls and17 proposed launcher-contract cases do not silently count this
registration effect, replace LC-C05/06 requirements or authorize the whole main.

Full corrected outer review, fresh coordination/execution/cleanup admission,
source-bound regression evidence, actual original database results and role-
correct filled F/C/outer approvals remain separate. No actual request/approval
is supplied by this report and no build, helper, signal, deletion or retry is
permitted by its disposition.

## Accounting, restrictions and resource discipline

Zero new product families, application cases, executed controls or closures.
Current aggregate accounting is19/25 original closures;22/38 all closures,16open;
2/12 original suspicions,10open;8 PVD explanations, owner choices separate.
These are different denominators, not readiness percentages. This bounded
infrastructure review itself does not independently adjudicate PVA-038.

PVU-007 STOP, PVU-011 NO RETRY, PVA-029's49 cases/44PASS/5FAIL and no automatic
retry, G7/G8 CLOSED, old-runner prohibition and Windows FAIL/cleanup HOLD with
all14 intended application cases unstarted are retained. Synthetic-only scope,
root sole build ownership, hardware gaps, protected refs, dependencies/versions/
identities, signing/store/publication and occupied1017001 restrictions remain.

Only compact permanent reviewer reports and bounded read-only source/hash/Git
diff operations were used. `git diff --no-index` exit1 denotes observed source
changes, not a test. No source edits, staging, temp/cache files, build outputs,
workers, daemons, background jobs, process signals, wrapper invocation or cleanup
occurred. Reader FDs closed; wrapper --stop is not applicable to this lane.
