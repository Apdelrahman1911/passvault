# GUI07 root-signal reachability — source addendum, no execution

**The typed pre-GUI cancellation route is reachable under outer-only delivery;
the source does not inevitably kill unshare before the original stop.** This
is a conditional control-flow conclusion, not an observed cancellation result.

Fixed source packet: `T/current_ledger/gui_successor01/`, where
`T=/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/reviews/team20/resume_disk`.
OUTER.py:100736B, SHA256
`19bccc5f4441cafe771ecac0f94f3ec81a8a8a3688346f9ac0df997e8a3d2f69`;
INNER.py:103569B, SHA256
`6fa032851905700ee350f50f1d8d3031e75932420d56409ca2175263a858ae37`.
The sealed source acceptance remains
`T/remaining_report/gui_successor_review01/SOURCE-REVIEW.json`
(11395B, `0391478f22ef28702b6ed83ef61411c6cfc50ac3aa5724888ef151807e5d8d87`).

## Concrete path through the original wrapper/inner

Assume fresh admission, active original prepare after successful namespace and
source-before checks, one handled INT/TERM/HUP delivered **only to the original
outer**, adequate original drain time, and no concurrent resource/custody/log
failure. These are necessary assumptions, not supplied runtime facts.

1. OUTER `signal_note`1173 records `signal:1/2/15`. Its live `supervise`605–608
   calls `latch`, whose288–293 exclusive write places `E/CANCEL` in the original
   retained evidence directory. For the isolated child, the drain is up to750s,
   clipped by END. The outer sends no immediate signal to unshare/inner: only an
   eventual **original-pidfd SIGKILL** at drain expiry.
2. The actual command at1410–1411 is `unshare --mount --pid --fork
   --kill-child=SIGKILL ... INNER_PYTHON -I -B -S INNER ...`. Before GUI isolation,
   the inner has the originally validated shared evidence path. INNER
   `cancelled`447–453 polls that original directory for `CANCEL`; direct signal
   forwarding by unshare is not required for this communication.
3. In the active prepare loop,687–698 observes cancellation and marks
   `cancelled_work_only` only if no timeout/resource/output failure caused the
   abort. It signals only its original child pidfd. Direct exit plus log EOF and
   retained finalization are required;735–739 admits only the exact cancellation
   error to `disposal_command_complete`.
4. The original required `prepare-stop` still executes with `stopping=True`.
   If that stop0, namespace settlement, both exact command records and logs are
   accepted,835–840 raises `OriginalPreGuiWorkCancelled`. The distinct catch at
   1549–1552 does not set the generic-exception cleanup blocker.
5. Finalization still requires input/evidence checks and own namespace/GUI
   settlement. It deliberately does **not** claim source-after or validation
   acceptance. The exact no-GUI/no-render two-command cancellation predicate at
   1586–1595 can make disposal_safe true; the terminal choice after
   `INNER-RESULT.json` is then **inner1**, not70 or product PASS.
6. Provided the original unshare remains uninterrupted and normally returns
   its child's1 before the outer drain expires, OUTER1374–1376 enters only the
   typed eligibility intake: code1, signal-only reasons, and **no outer pidfd
   kill**. All later result/stop/log/namespace/identity/allowlist guards still
   apply before disposal. Successful cancellation disposal remains outer1 /
   `VALIDATION_CANCELLED_CLEANED`, not successful validation.

## Delivery boundary matters

The outer's Popen does not create a new session. A terminal/process-group
broadcast may also reach unshare, inner or the active Gradle child, with
different handler/exit timing. If unshare dies, `--kill-child=SIGKILL` can preclude
the original stop/result route. Directly interrupted descendants can also fail
the exact cancellation-only/settlement predicates. The source alone does not
establish the pinned unshare's response to every broadcast signal; it is wrong
to claim either that every Ctrl-C succeeds or that every INT/TERM always kills
unshare. A minimal actual witness should explicitly target the original outer,
not use broadcast delivery as an unstated equivalence.

A second signal during cleanup, earlier materialization/intake cancellation,
resource/log/deadline error, forced wrapper kill, bad stop or unknown identity
still yields HOLD. No generic failure-to-cleanup promotion, original GUI06 or
Image01 release, runtime proof, new test count or full-GUI replay follows.
