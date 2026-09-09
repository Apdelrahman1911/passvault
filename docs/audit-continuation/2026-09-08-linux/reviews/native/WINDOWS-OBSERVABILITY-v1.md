# Inert Windows observability proposal v1

Author `/root/native`, 2026-09-08. **SOURCE PROPOSAL ONLY; NOT APPLIED, EXECUTED
OR ADMITTED.** Root assigned this bounded follow-on after run01 was consumed.
The adjacent patch is inert text, not a helper to invoke. It does not admit
another request, recovery, retry or build. The original accepted helper,
workflow/request, reports and failed-run evidence remain unchanged.

## Exact proposal identity

- Baseline `scripts/audit/windows_native_validation.py` SHA-256:
  `67bcd0aeaa03fa829d55fecfbd640d185163805e4c0a067b281bd807333af243`.
- `WINDOWS-OBSERVABILITY-v1.patch`: **14,871 bytes**, SHA-256
  `9c20a77fa231277bf9222c4b9e2579c523740203985694b9a9ceb24df39e5675`.
- Hypothetical amended helper bytes, computed by inert string/diff generation:
  SHA-256 `13362bb5329c75acb8ade469c7fe7f2db0a9167f032bae797ed4dccbd67f231d`.
  No amended helper file was written or imported. This is not a compiled,
  executed or committed source identity and not an execution acceptance.

Motivation is the exact independently reviewed `RUN01_DIAGNOSIS.md`, not a new
product finding: run **34284083351/1** lost the configure parent exit code when
its Job failed to drain, retained no member identity, and refused an unidentified
reparse entry during complete pre-deletion gathering. Its disposition remains
**FAIL / cleanup HOLD / all 14 cases UNSTARTED**. The unknown worker/reparse
causes have not been corrected or inferred by this patch.

## Three narrow changes

### 1. Separate parent outcome from cohort completion

Immediately after the existing `GetExitCodeProcess`, retain the parent PID/exit
and command sequence/log identity in memory and append a durable
`command_parent_exit` event **before** waiting for descendants. Leave completed
`command_results`, cohort settlement and command success predicates unchanged.
The original ten-second drain deadline is set before this new event: diagnostic
writing does not restart or extend it. A journal-write exception still unwinds
through the original command `finally` and exact-Job termination path.

This means a later drain failure can retain a parent exit observation without
pretending that the whole command passed. If journal storage fails, the final
result may still retain the in-memory observation; a hard interruption can lose
either record and must remain explicitly incomplete.

### 2. One exact-Job, query-only member sample inside the drain

On the first active drain iteration only, attempt one snapshot with a **250 ms
soft budget contained within the original drain deadline**, maximum **16**
members, and a fixed `JOB_OBJECT_BASIC_PROCESS_ID_LIST` buffer. Do not resize,
retry, enumerate the host, match process names or spawn diagnostic workers.
An overflowing/failing list query produces an unknown, not a partial list
misrepresented as complete.

For each returned PID, open a non-inheritable
`PROCESS_QUERY_LIMITED_INFORMATION` handle only. Before reading any image or
time, verify that this opened handle belongs to the exact retained Job with
`IsProcessInJob`. A PID that was recycled outside that Job gets no image/time
query. The same stable handle supplies creation FILETIME and image metadata;
only the image basename and an opaque UTF-16LE full-path hash are retained.
There is no image-file, command-line, environment or process-memory read.

Caps are one Job-list call and, per attempted member, at most one open,
membership query, time query, 512-character image query and close. Cancellation
and the existing deadline are checked before each next query; handle closure
still occurs on cancellation, query failure or exceptions. API failures become
explicit unknown fields. A failed query-handle close stops the command through
its original failure/termination path, is recorded, and is not retried.

The observation is buffered for existing final-result serialization, avoiding
additional diagnostic journal writes while descendants are draining. It is a
**racy early-drain sample**, not the member set at the later failure instant,
not a process census and not evidence of which member lingered for ten seconds.
It may legitimately contain exited/unknown members or be empty after a race.
No sampled PID or basename is ever used for signaling or cleanup authority.

The time cap is a *cooperative call-admission bound*: synchronous Win32 queries,
the already-required close and existing journal `fsync` cannot be forcibly
interrupted by these Python checks. The proposal does not claim a hard real-time
deadline or add threads/processes to hide that limitation. No diagnostic query
starts after cancellation/deadline or from the termination routine; its drain
clock is never reset. This timing qualification must be independently reviewed
before any future execution proposal; an execution cannot be justified merely
by treating these calls as guaranteed instantaneous.

### 3. Observe cleanup rejection without following or deleting it

Only descendant gathering enables the optional rejection observation. On a
reparse/hardlink refusal, retain the initial volume/file index, attributes and
link count from the original no-follow handle. For a reparse entry, attempt at
most one `GetFileInformationByHandleEx(FileAttributeTagInfo)` call on that same
handle, unless cancellation is already sticky. Retain the tag/attributes or
numeric error, never reparse-target bytes. No `readlink`, `resolve`, target
open, `FSCTL_GET_REPARSE_POINT`, additional access rights or deletion is added.

The existing one handle-close attempt is observed, not retried. After closure,
the rejection is recorded using a lexical owned-relative path from the existing
`scandir` descendant; reject absolute/drive/parent-component names. Retain at
most 1,024 path characters; oversized names get only length/hash. The compact
record is buffered for final result and journaled before rethrowing the refusal.
A journal failure remains a failure/HOLD, with its error type retained in the
in-memory record, never a reason to proceed to deletion.

These two metadata reads need not be simultaneous: distinct returned attributes
are kept rather than inventing one immutable combined state. The original
cooperative hosted-filesystem qualification remains; this is not an adversarial
same-user sandbox or authority to access a reparse target. Complete handle
gathering must still succeed before the unchanged deletion loop can begin.

## Counterexamples and independent review request

The patch should be challenged against at least these source paths; these are
review scenarios, **not executed tests**:

- Parent exit 0 with an unemptied Job must retain the exit and still fail.
- Parent exit nonzero with an empty Job must not become a successful command.
- No active drain iteration, cancellation, elapsed budget, list overflow,
  access denial, PID reuse and vanished members must not trigger broader scans,
  retries or image/time queries without exact-Job membership.
- Mid-query exceptions must close the acquired query-only handle; close failure
  must be preserved, with the existing command termination still reachable.
- A snapshot cannot identify the eventual lingering member merely because a
  process was in the initial list. No root-cause label may be machine-inferred.
- Reparse rejection, hardlink rejection, tag-query failure, oversize names and
  rejection-journal failure must all leave gather-before-delete and HOLD intact.
- Neither observation failure nor metadata success can reopen a historical
  recovery scope, authorize PID/name kills or prove physical Hello behavior.

Independent reviewer `/root/native_review` is asked to challenge these exact
patch/report bytes. Any review acceptance must remain **source-only**, distinct
from admission to apply a revised executable helper or to run anything. A future
root decision would need exact amended source/authority bindings, renewed
coordination and independently reviewed execution/cleanup constraints. There is
no activation-request/workflow change in this proposal and no automatic next run.

Confirmed/suspicion closure denominators and all PVD boundaries are unchanged.
PVU-007 STOP, PVU-011 NO RETRY, PVA-029 recorded failure/no automatic retry and
G7/G8 CLOSED remain intact. The author used bounded source reads and in-memory
text-diff generation only; no helper import, syntax/runtime test, build/CI,
temporary amended helper, archive, generated binary, cache or background worker
was created. Only this permanent compact report and inert patch were written.
