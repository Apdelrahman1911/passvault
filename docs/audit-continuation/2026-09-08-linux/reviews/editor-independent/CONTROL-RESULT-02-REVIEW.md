# NEW Linux controls02 — independently reconciled control PASS

Reviewer `/root/editor_review`; executor/launcher author `/root`; controls author
`/root/verification`. Actual result review at **2026-09-09 01:34 UTC**.
**22 controls PASS; zero application tests. No next execution admission.**

`CONTROL-RESULT-02-REVIEW.json` binds the source, admission and observations:
SHA-256 `8284e904b16f0b4bb87f05dee4cd7fe59f2b850dcd562fc4dafc5752547e7f62`.
The actual journal is
`28f806393e0adacfdcd13913b57b2639aa0ae57010347fa67370aae1dd3aa863`
(124,307 bytes, 213 consecutive records); external receipt is
`88ee8b5264e3924aca77e392b3f69c9ad68a67b1a7cc7b9227ece35135a402b5`.

## Results and independently challenged boundaries

The consumed02 invocation attempted **22/22**, passed **22**, failed **0**,
and left **0 unstarted**. Every full case payload was read; the preterminal
report's 22 result objects exactly duplicate the individual records. All seven
admitted input hashes/full pins, 17 original directory pins, retained original
bootstrap lock and fixed Python image/pin matched again during this review.
Request, actual author/independent approvals, fresh preflight and external
receipt reconcile; the invocation was within its original request window.

**C-FIXTURE-01** (reviewer alias **LC-FIXTURE-01**) is one infrastructure defect,
not a product or subject-runner family. The current source is exactly two changed
lines from executed01: the None-sentinel guard and the separate scratch02 name.
No assertion, exception or non-None payload cap was weakened. The intended
no-pipe constructor now reaches both mocked subject direct-child branches:

- **LC17**, event177: `direct_child_already_exited`, PID910101, start100, exit0;
  `during_admit=false`.
- **LC22**, event208: `direct_child_already_exited`, PID910101, exit0;
  `during_admit=true`.

These provide meaningful fixture-regression evidence **at the declared mock
boundary only**. They are not real child, waitpid, pidfd or signal-race evidence.
Oversize-payload refusal remains source proof, not another executed case.
Changing None to empty bytes would not be equivalent: that allocates a pipe
without direct-only teardown.

LC01–08 retain actual tiny POSIX file/FD evidence with their declared fault
hooks. LC09/10/13/14 exercise real pipes/log IO around fake Popen/process rows:
positive eight-byte output, retained fake exit23 failure, one fake stop with
duplicate refusal, and four-byte retained log cap respectively. LC11/12 launch
no fake child and preserve their false/true stop-obligation boundaries; their
inner generic pipe label must not override that narrower actual evidence.
LC15–22 remain mock process/parent/signal checks; LC21's real duplicate of an
ordinary FD is not a pidfd. Fake Gradle argv, stop, PID910xxx and resource rows
are not actual Gradle/application/process tests. Real children and signals: **0**.

## Full original namespace and process reconciliation

Ordered replay **as data only** reconciled all 213 records: 24 original fixture
directory allocations with 24 rmdir pairs; 25 regular originals plus two link
aliases with 27 unlink pairs. All **51 deletion-intent pins** matched the
previously recorded originals. The two rename intents remap three original
members; later matching cleanup observations reconcile those lifecycles without
inventing separate rename-success events or adopting replacement inodes.
Repeated inode numbers after completed lifetimes are not themselves drift.

Every case root was removed before its result. All 22 namespace cleanups report
success and explicitly `descriptor_settlement_hold=false`. No pending/live
synthetic ledger objects remain. Scratch02's original inode691491/dev23 was
recorded before use and removed by events210/211; terminal212 reports unchanged
inputs, no error/cancellation/timeout, cleanup
`ORIGINAL_EMPTY_SCRATCH_REMOVED`, and **exit0**.

Root's actual tool `2f594a` exited0 with empty **combined** output, not separately
captured streams. Original PID21528/start18550209 and scratch02 were absent in
root's external observation and again independently at 01:34 UTC. Exact
no-real-child source scope plus actual process exit supplies final OS descriptor
settlement; pathname absence alone is not FD proof. No host-wide process census
or individual final close-return trace is claimed. Wrapper `--stop` is N/A:
no wrapper was launched. Tiny original coordination metadata remains needed.

## Bounds and preserved clock disagreement

Internal batch time is 0.18573881100746803 s; longest case is
0.012907249009003863 s. All recorded 180/15-second soft bounds pass.
Across **all three real launcher samples**, minimum disk availability is
32,610,701,312 bytes, minimum available RAM 44,289,724,416 bytes, and maximum
sampled self RSS 235,496 KiB. These are point samples, not continuous quotas;
fake command-resource rows are excluded.

The tool reports **0.215379499 s**, shorter than the first-to-terminal journal
wall-timestamp span **0.276773090 s**. Preserve both observations and their
different/uncertain clock scopes. There is no separately retained complete
external monotonic invocation interval and no hard240-second guarantee.
The same qualification for01 (tool0.255451913 s versus journal0.311654695 s)
remains unchanged; neither disagreement is normalized away.

## Slot recommendation, history and unchanged product progress

**Root may release this completed controls02 slot after recording this review.**
The original synthetic namespace and no-child process are settled; the reviewer
does not release the slot, admit database execution or authorize any retry.

**Controls01 stays 20 PASS / 2 FAIL, exit1**, not retroactively successful.
LC17/22 then failed with `TypeError: object of type 'NoneType' has no len()`
before the intended subject boundary. Its failed source, journal, external
receipt, independent review, prior reviewer miss, consumed approval and explicit
count erratum are preserved. Controls02 is a distinct changed-source,
independently admitted instance, not erased or repaired01 evidence.

Product progress remains **19/25 original confirmed**, **22/37 all confirmed**,
and **2/12 original suspicions**, with zero qualified-closure delta. The eight
PVD limitations stay separate. Windows34284083351 remains **FAIL / cleanup HOLD,
zero started and all14 unstarted**, with no retry. PVU-007 STOP, PVU-011 NO RETRY,
PVA-029's failure/no automatic retry, G7/G8 CLOSED, old-runner/helper prohibition,
hardware gaps and all protected-ref/dependency/version/identity/signing/store/
publication/occupied1017001 restrictions remain.

Reviewer metadata reconciliation tool `013f0e` exited0; all retained metadata
descriptors closed. An earlier inline metadata-display syntax typo failed before
its body ran; the corrected read succeeded and is not an additional control
attempt. No target import, syntax probe, compilation, build/test, scratch, cache,
generated build output, daemon, worker or emulator was created by the reviewer.
No unrelated resource was removed. At01:34 UTC, reviewer point samples showed
32.403 GB available project disk, 21.694 GB `/tmp`, and 44.820 GB available RAM
of67.436 GB. Only compact necessary permanent review evidence is retained.
