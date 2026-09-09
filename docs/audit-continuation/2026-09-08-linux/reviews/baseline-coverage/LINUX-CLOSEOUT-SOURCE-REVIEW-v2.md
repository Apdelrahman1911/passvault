# Linux closeout v2 — correction acceptance, narrow PLAN qualification pending

**The two v1 code corrections are independently source-accepted. The PLAN needs
one narrow prose correction before final pair acceptance. No execution, helper
import, syntax check, cleanup, inert-control run or actual-instance acceptance.**

Reviewer `/root/baseline_coverage`; helper author `/root/editor`. Root and this
reviewer remain disclosed design contributors, not helper code authors. Root
alone owns execution. `LINUX-CLOSEOUT-SOURCE-REVIEW-v1.{json,md}` and both v1
snapshots remain unchanged; neither source review is a normative postrun
`ACCEPT-baseline_coverage.json`.

## Frozen inputs and bounded successor review

- Helper: `b16774ff6d0c14ece40dbe2c75684a2b07e7c90c9da4373ec0a6e12e87cc7bec`,
  59,183 bytes / 1,094 LF.
- PLAN: `05ef7315917cf1d26106d1d69be47709738ea2bc5ee7d5805864749cbb77ba61`,
  28,699 bytes / 466 LF.
- Exact inert copies: `linux-closeout-v2.py.txt` and `linux-closeout-plan-v2.md`.

I read the entire literal v1-to-v2 source/PLAN diff and the changed code's caller,
failure and resource context. The full v1 review is retained rather than
restarted. Data-only comparison also proved six bounded unchanged regions:
fixed imports/paths/targets/schema constants; original `Directories.open`;
descriptor close; process/mount/resource/original-lock Guard; evidence/source/
Forest/recheck functions; and main's postbootstrap preflight/deletion/proofs/
terminal/close path. Their exact hashes are in the paired JSON. This is byte
reconciliation of earlier reviewed context, not fresh application semantic LF
credit or a new whole-file executable test. Live helper/PLAN bytes still matched
the frozen copies at the final comparison.

## LC-C05-01 — code correction source-accepted

The new `Journal` (258–311) checks the initial empty descriptor/path tuple after
file/parent fsync. Each append first compares the full current pin to `self.last`
and current size to `self.used`, then verifies that the pathname still names that
descriptor. The v1 equal-size rewrite counterexample with changed mutable pin
fields now fails **before** another write. Truncation, unexpected external append
and link/path replacement do not become acceptable successor state.

Only after all writes, file fsync and final descriptor/name/expected-length/
original-stable-field proof does the helper advance `last`, `used` and `sequence`.
The positive path uses the completed successor pin rather than comparing every
event to the original empty-file timestamps/size. A write/fsync/final-check
exception latches `ok=false`; main's existing propagation stops later deletion,
and there is no journal repair or namespace replacement. Postwrite path or stable
identity drift is guarded; arbitrary same-inode modification during the writer's
own append is not cryptographically distinguishable by these metadata checks.

The PLAN now explicitly retains cooperative point-observation, hostile-writer
and timestamp-resolution limitations. This is **source correction acceptance**,
not executed evidence that any closeout journal remained intact. All proposed
positive/negative journal control effects from v1 remain unexecuted here.

## LC-C06-01 — code correction source-accepted

Main installs `bootstrap_tick` (990–992), checks it before RLIMIT/setup, passes it
into `Directories` before admission, and checks it through postadmission lock
setup and immediately before journal consumption. Input reads poll before path
work and during chunks; durable reads poll between fsync boundaries. Binding,
verification, original-parent/source/event loops and under-lock rehash now poll.
An already-latched or later-observed cancellation/deadline reaches `Hold` before
the remaining bootstrap sequence is completed.

The callback is replaced by the existing full Guard only after original lock,
input and coordination authority has been bound. No bootstrap sampling adopts
current directories as historical originals. `Directories.open`, journal event
writing and descriptor closing remain nonpolling: Guard does not recursively
call itself, a best-effort HOLD journal can still be written after a latch, and
descriptor settlement is not prevented by the already-expired work deadline.
The first callback before owned FD allocation can fail without leaving opened
descriptors; later exceptions pass through main's existing finally path.

Filtering event rows is source-equivalent for the original contracts: journal
sequence/unique terminal checks remain; source-bound checks still require the
before phase and 1198 files; an `owned_process` without `start` still fails rather
than being silently omitted, while a fast-exited direct child with no observed
birth is still not PID adoption authority. Set ordering is immaterial here.

The PLAN accurately qualifies 900 seconds from main's installed monotonic clock,
not interpreter/stdlib startup; the external launcher must bound that startup.
Synchronous operations may delay polling; this is not a hard syscall deadline.
No stop, process signal, deletion retry or extra cleanup path was introduced.
Proposed bootstrap controls remain unexecuted, not inferred passes.

## LC-C03-DOC-01 — independently challenged, documentation-only revision

On successor reread I found a pre-existing overstatement: the PLAN says all JSON
parsers reject nonfinite values. `strict_json` rejects duplicate keys and the
literal `NaN`/`Infinity`/`-Infinity` tokens through `parse_constant`, but does not
provide a custom `parse_float` finite check. An overflowing exponent such as
`1e400` is therefore not covered by that parser-level claim.

The helper author independently challenged this source observation and agreed:
ignored/extra metadata or a nonempty launcher `limitations` list can contain the
value, so the prose is genuinely broader than the code. **No unsafe cleanup path
was identified.** Critical paths separately require exact LIMITS/bindings and
actual finite stat tuples; bounded admission/launcher time comparisons; typed
journal times/terminal exits; literal stop budgets from the independently
accepted original runner; and exact boolean settlement/coordination gates. New
resource/process admission does not trust external numeric metadata. Actual
filled-instance review still must inspect truthful shapes and values, not rely on
a blanket parser claim.

The agreed bounded disposition is a PLAN-only qualification, **not** a parser,
schema, dependency or authority redesign. Preserve this v2 pair and provide the
new exact PLAN hash for narrow rebind. The source itself must remain byte-exact
`b16774...` for that rebind. This observation is audit-document precision, not a
new PVA family, application failure, control execution or broad JSON-fuzz review.

## C01–C07 / next admission boundary

C01, C02, C04 and C07 retain the prior exact source-contract acceptance. C05 and
C06 are now source-accepted for the reviewed corrections above. C03's code
contract remains source-accepted **with the explicit finite-number qualification**;
the overbroad PLAN sentence remains documentation-REVISE until separately rebound.
All seven actual original-run/closeout instance obligations remain **UNFILLED**.

The literal 33 targets / 35 original allocations, separate fresh directory/file
device policy, preserved source/reports/schemas/runtime/checkout, same original
lock, positive stop-or-no-Gradle proof, external completed launcher and fresh
conservative settlement/mount/resource requirements are unchanged. The original
runner's settled RESULT boolean is still not raw three-snapshot/waitpid proof or
standalone fresh process authority. Final runner review, exact actual originals,
meaningful separately admitted controls, postrun TARGETS/EVIDENCE/REQUEST and
independent role-correct acceptances, outer closeout launcher and root's execution
approval are all separate prerequisites; this report supplies none by inference.

No build/test/helper import/compile/run or runtime deletion occurred. Only compact
permanent reports and exact inert snapshots were written. No owned daemon, cache,
temporary archive, runtime or wrapper-stop obligation was created. All PVA/PVU/PVD
denominators, failures, hardware gaps, STOP/NO-RETRY/CLOSED restrictions, Windows
cleanup HOLD and publication/candidate 1017001 fences remain unchanged.
