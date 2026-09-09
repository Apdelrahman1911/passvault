# C-FIXTURE-01 — executed controls sentinel failure, source-only diagnosis

Author: `/root/verification`; 2026-09-09 UTC. Independent finding/patch
challenger: `/root/storage`. Actual execution/result adjudicator:
`/root/editor_review`. **Author diagnosis and proposed source correction only;
no after-image execution, admission or completed fix claim.**

## Evidence bound before authoring the correction

The unchanged executed source `linux_runner_controls.py` is SHA-256
`320f363e5f9bdce0e806a624f7e9fd9fc943690be57f8c75c9f43f865c04903d`;
its design is SHA-256
`1292305d4a0f1729be7016002c9f1bc99f37487077aea4f4f3d1aa1854723921`.
Both hashes match the consumed root request/approvals and external receipt.
Before authoring any correction, exact byte snapshots were exclusively created
as `linux-controls-executed-v3.py.txt` (43,044 bytes / 893 LF) and
`linux-controls-design-executed-v3.md` (17,723 bytes / 264 LF), tool `120627`,
exit0. The original source/design, previous snapshots, launcher, approvals and
failed-execution evidence were not edited or overwritten.

Original journal `CONTROL-events.jsonl`:
`75fcebbd3c666518f9b2160d186e83ad9eebb9b8774c016eae043e88e40c6eb7`.
Original external `CONTROL-LAUNCH-RECEIPT.json`:
`39a7daf48e4096fab5245c858f71aef1fe7eae507dd88b424078774fca1217f1`.
Root's one tool invocation `8b0448` exited1; its reported elapsed0.255451913s
is an external tool observation, not the internal0.2103432939911727s control
clock or a proven total cleanup deadline.

A bounded data-only read (`/usr/bin/python3 -I -B`, tool `852635`, exit0)
checked journal raw hash, size123,873, no-follow/singlelink regular-file
identity and pre/open/post stability, then parsed213 contiguous records. It
counted22 case-result records:20 `PASS`,2 `FAIL`, with the failures below.
This read did not load, compile, import or execute source code, and its count
is not a substitute for the assigned independent case/cleanup adjudication.

| Case | Sequence | Actual error | Recorded cleanup |
| --- | ---: | --- | --- |
| `LC17_direct_already_zombie` | 177 | `TypeError: object of type 'NoneType' has no len()` | `ORIGINAL_SYNTHETIC_MEMBERS_REMOVED`, descriptor HOLD false |
| `LC22_direct_death_during_capture` | 208 | Same | Same |

Neither failing case contains a subject-boundary `evidence` result. Cleanup
recorded by the launcher/fixture is not proof that the intended direct-child
branch ran. Root's external scratch-absence/PID-settlement observations and
terminal cleanup receive separate independent reconciliation.

## Reachability and surrounding guards

- The exact22-entry `CASES` tuple names `direct_death` for LC17 (line769) and
  `lambda s, a: direct_death(s, a, True)` for LC22 (line774).
- In `direct_death` (lines662–674), `MemoryJournal()` and
  `s.Processes(journal)` create only in-memory state. At line665 both schedules
  then execute `FakeChild(0, None, 910101)`.
- At line475 the frozen `FakeChild.__init__` unconditionally evaluates
  `len(payload)` before `check` can guard anything. Python's `None` has no
  length. The later `self.stdout = None` and `if payload is not None:` guard
  (lines477–478) cannot intercept this failure.
- Consequently neither failing path reaches the constructor's `os.pipe2`
  (line479), its pipe-close handling, the process-row fixture schedule, or
  `processes.direct(child)` (line671). This is **no FakeChild descriptor
  acquisition**, not a claim that the surrounding `Area` lifecycle allocated
  no descriptors; the outer Area is separate and was exercised.
- Subject `Processes.direct` (runner lines417–429) and `admit`
  (lines367–415) are therefore not challenged by these two failures. No
  production or subject-runner defect has been established. Their expected
  same-birth zombie/observed fake-poll behavior remains unverified in this run.

The existing direct-death oracle is meaningful once reached: it requires one
fake-child poll, exit0, no owned processes or pidfds and a
`direct_child_already_exited` event. LC17 supplies an already-zombie row;
LC22 supplies live then same-birth zombie rows so the subject's fresh-capture
`ProcessCompleted` path reaches the observed completion handler. These remain
mocked process assertions, not real process or hardware tests.

## Minimal correction and compatibility challenge

New distinct source: `linux_runner_controls_v4.py`, SHA-256
`dffa884b09c7786e763c9becc3b0b220242b5c9555288bf1052e9e16912fa426`,
43,063 bytes / 893 LF. Exact raw textual after-image and diff were authored
without evaluating any source (`8c7224`, exit0). The941-byte diff is
`LINUX-CONTROLS-V4.patch`, SHA-256
`6ddef57c4463e97e170ba106e8786763f426aac7ca597f2b0798ea6b2c0210f5`.

There are exactly two changed lines:

1. At475, use `payload is None or len(payload) <= 32` instead of
   `len(payload) <= 32`. Exact-None short circuit admits only the intended
   no-pipe sentinel. Non-None payloads retain the same length cap and pipe
   handling; byte payloads larger than32 still refuse before acquisition.
2. At27, bind only the distinct proposed scratch02, not consumed scratch01.
   This is a required fresh-instance namespace change, not a semantic fix or
   authorization to create it.

Counterexamples considered source-only: deleting the cap would weaken pipe
safety; changing the None arguments to empty bytes would allocate pipes which
`direct_death` has no dedicated pipe teardown for; returning early before
assigning child fields would break `poll`/exit assertions. The one sentinel
guard leaves all those existing behaviors intact. No exception is swallowed,
no assertion removed, no expected outcome changed and no mock widened.

Root requested storage's independent challenge before execution. Storage and
editor_review independently reported the same pre-subject-boundary diagnosis;
the exact after-image still requires storage's distinct
`LINUX-CONTROLS-SOURCE-REVIEW-02.json` and any later root admission. The new
design is `LINUX-CONTROLS-V4-DESIGN.md`, not an edit of the consumed design.

## Limits and resource closeout

The original22 attempt remains20 recorded PASS /2 recorded FAIL, zero current
application cases. Its admission and scratch cannot be reused. No automatic
retry or database admission follows from diagnosing a fixture defect; a new
changed-source run needs fresh independently reviewed execution, coordination,
cleanup and exact-source admission. No new-source control or compilation has
been performed by this author. Qualified-closure delta is zero.

This source-only task created only necessary permanent source, exact failed
byte snapshots and compact review/diff evidence. All data-read/write descriptors
were closed; no build/test output, cache, daemon, worker, server or emulator was
created. Gradle wrapper stop is NOT_APPLICABLE; no wrapper was invoked. No
source/report/shared cache/SDK/toolchain or unrelated process was deleted/stopped.
At00:30:42Z the worktree had23,059,580KiB available, /tmp21,556,092KiB and
MemAvailable45,065,028KiB / MemTotal65,855,360KiB, with no swap. Ambient resource
changes are not attributed to this small source-only task.

PVU-007 STOP, PVU-011 NO RETRY, PVA-029 failure/no automatic retry and G7/G8
CLOSED remain; so do all PVD, hardware, Windows cleanup-HOLD and publication
boundaries. Defined denominators remain19/25 original,22/37 all confirmed and
2/12 original suspicions, not readiness percentages.
