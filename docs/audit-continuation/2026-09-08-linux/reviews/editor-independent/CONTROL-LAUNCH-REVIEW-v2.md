# NEW Linux inert-control launcher — corrected source accepted

Reviewer `/root/editor_review`; author `/root`.
2026-09-08. **ACCEPT SOURCE ONLY; FILLED-INSTANCE/EXECUTION ADMISSION PENDING.**

`CONTROL-LAUNCH-INPUTS-v2.json` binds launcher
`967ddf9a5d792712dff248f29b7845b58fcafe68e99c772341bac845ce744210`
(21,518 bytes / 400 LF) and plan
`66f9345ae32f43d6da894e6edfd688fda1f2325372effc1dd1beace60a21d29c`
(10,261 bytes / 160 LF). The complete v0-to-corrected source/plan diffs and
surrounding admission, cleanup and terminal paths were re-read. Preserved v0
files match the previously reviewed hashes; the initial REVISE is not erased.

## Independent correction challenge

**CL-R01 accepted at source.** `verify_lock` rechecks the captured original
regular-file tuple against both fd and no-follow name under the original parent.
Following the durable rmdir intent, cleanup repeats original BASE ancestry and
scratch fd/name/emptiness checks immediately before its one rmdir. It fsyncs the
original parent, checks absence and records outcome. The original lock is also
rechecked before terminal evaluation. Missing/replaced objects throw into HOLD;
there is no replacement adoption, retry or recursive cleanup.

This remains cooperative pathname-based deletion, not an atomic kernel
compare-inode-and-delete primitive or protection against a hostile same-UID
writer. An original empty-root rmdir is namespace evidence, not proof that a
helper's separate descriptor HOLD was settled. That HOLD cannot yield PASS.

**CL-R02 accepted at source.** The literal 22 ordered names match the accepted
controls' declaration. The launcher checks that declaration before calling the
helper, then independently requires the same ordered result names, PASS and
successful namespace/cleanup fields, explicitly false descriptor-settlement
HOLD, true per-case/total soft-bound markers, correct runner hash, consistent
22/22/22 counts, no unstarted names, empty scratch and zero real-child/signal/
application counts. Existing final cancellation/resource/evidence/cleanup gates
remain conjunctive. Missing or inconsistent fields do not normalize to success.

The reviewer also read the actual storage-authored controls-review JSON at
`bfdc0e7ebabc06078195c91e5f6014ca446895c8e22d0fb5f31848a963079432`.
It accepts exact v3 controls/design `320f363e...` / `1292305d...` as **source
only**, accurately discloses storage's coauthorship of the subject runner and
does not authorize execution. This launcher review does not invent another
independent full-controls review. The helper's real file/pipe cases and mocked
process/signal/pidfd/stop cases remain different kinds of evidence.

## Admission and limitations retained

The unchanged finite fresh coordination bootstrap description is source-acceptable.
Root reported it would perform only that metadata allocation next, not controls.
Actual creation/descriptor/fsync evidence and the original lock tuple must be
reviewed rather than inferred from a later current pathname.

This document is **not** `CONTROL-ACCEPT-editor_review.json`. Before that actual
approver-authored document, independently check the completed exact request,
seven hash/input-pin bindings, original ancestor/lock/Python tuples, prospective
time window, actual accepting storage report and root approval, fresh scratch
absence and root's sole-local/CI-slot observation. No command is run merely
because source or metadata-bootstrap review is accepted.

The one fixed isolated Python entry, 240s alarm/60s cleanup grace, resource/input/
evidence caps, monotone cancellation, original scratch cleanup, masked terminal
boundary and required external observed-exit receipt retain the qualifications
in v1. Descriptor-close failure or an incomplete/failed outer exit remains HOLD;
do not retry unknown fd numbers or infer settlement from a journal alone.
Soft polling, synchronous calls, Python signal delivery and hostile namespace
limitations are not OS-enforced total resource/wall-time guarantees.

Root must independently reconcile actual case/evidence/cleanup outcomes and
obtain another-agent result challenge before separately admitting a database
run. No wrapper runs here, so `--stop` is N/A, not an observed successful stop.
No old runner, G7/G8 recovery, PVU-007/PVU-011/PVA-029 retry or Windows retry is
admitted. PVD, hardware, publication and all application denominators remain
unchanged. **Zero controls, imports, syntax/compiler probes or application tests
were performed by this reviewer.**

The two root-reported rejected patch preparations did not constitute controls
or application failures; the v0 hash and successful final diff are retained.
Only text/metadata reads and compact review writes occurred here. No reviewer
scratch, build/cache output, daemon, child process or background worker exists
to clean; no shared cache/source/evidence or unrelated process was removed.
