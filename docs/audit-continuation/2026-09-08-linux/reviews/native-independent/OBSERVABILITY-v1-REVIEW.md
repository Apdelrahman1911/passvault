# Independent review of inert Windows observability v1

Reviewer `/root/native_review`; proposal author `/root/native`.
Disposition: **SOURCE SCOPE SUPPORTED WITH QUALIFICATIONS; NOT APPLIED;
NO IMPLEMENTATION OR EXECUTION ADMISSION**.

This review concerns only the adjacent author's inert proposal under
`../native/`. It does not revise the accepted executable helper, workflow,
request, Windows01 evidence or previous admission. No patch was applied to any
file and no helper was imported, evaluated, compiled or executed.

## Exact identities and review method

| Input | SHA-256 |
| --- | --- |
| Accepted 39,470-byte / 719-LF helper | `67bcd0aeaa03fa829d55fecfbd640d185163805e4c0a067b281bd807333af243` |
| `WINDOWS-OBSERVABILITY-v1.patch`, 14,871 bytes / 251 LF | `9c20a77fa231277bf9222c4b9e2579c523740203985694b9a9ceb24df39e5675` |
| `WINDOWS-OBSERVABILITY-v1.md`, 8,537 bytes / 139 LF | `692bc8bd0d642246c5e1b6481f0ec218092f90fe65bcdfcdce8dcd5362d316c8` |
| Hypothetical amended bytes, 49,882 bytes / 887 LF | `13362bb5329c75acb8ade469c7fe7f2db0a9167f032bae797ed4dccbd67f231d` |

The complete patch and author note were displayed/read. A separate data-only
text reconstruction verified all nine hunk offsets, old/context text and line
counts against the unchanged accepted helper and reproduced the hypothetical
hash. Those hypothetical bytes existed only in memory; no amended helper was
written. Surrounding command-finally, full-gather/deletion and final-result
paths were then displayed from that in-memory text. This is not a syntax test,
runtime control, native ABI check or meaningful regression execution.

## Independent counterexamples

### Parent exit must not become command success

The proposal appends a separate in-memory parent observation and durable
`command_parent_exit` after `GetExitCodeProcess`, before the owned-Job drain.
It leaves completed command records, empty-Job requirement and exit-zero
success predicate in their existing positions. Thus parent exit zero with a
lingering Job still fails; parent exit nonzero with an empty Job still fails.
A parent-journal exception takes the existing command-finally termination path
while the in-memory observation may survive for final serialization. Neither
storage failure nor hard interruption guarantees that any final evidence exists.

The ten-second drain deadline is established before the added durable write;
neither that write nor the diagnostic sample resets it. This preserves the
clock's source-level placement, **not a proof of a hard elapsed-time bound**.

### Process metadata remains bound to the retained Job

The fixed PID buffer is 16 entries, matching the existing active-process cap.
There is one list query; failure/overflow has no resize, retry or host-scan
fallback. Each process handle requests only `0x1000`
(`PROCESS_QUERY_LIMITED_INFORMATION`) and is non-inheritable. The exact retained
Job is passed to `IsProcessInJob` before either time or image query. An outside
process that reuses a stale listed PID therefore supplies no image/time data.
Creation time and image metadata come from the same opened handle, not a later
PID reopen. No process memory, command line, environment or image file is read.

Each acquired query handle has a `finally` close, including membership failure,
deadline/cancellation return and query exception. A failed close is recorded
and raises into the original exact-Job termination path; it is not retried and
does not become success. A partially completed sample remains explicitly partial
or contains unknown fields. The proposal does not establish runtime Win32
signature/alignment/error behavior merely by declaring ctypes structures.

The initial active drain iteration is the only sampling opportunity. A member
may exit before/after its query, or the Job may empty before the list call. An
early member list is **not** the set at the later drain failure and cannot name
the eventual lingering child or diagnose run01. The returned assigned/listed
counts remain observations, not a guaranteed simultaneous census. No sampled
PID/name gains signaling or cleanup authority.

### Rejection metadata does not permit deletion or target traversal

Only existing descendant gathering opts into rejection observation; parent/root
binding retains its prior behavior. The reparse/hardlink predicates are not
relaxed. Reparse-tag metadata is queried on the original
`OPEN_REPARSE_POINT` handle, at most once, with no added access rights, target
open, readlink/resolve, target-data control call or directory traversal.
The original and tag-query attributes remain separate rather than being
presented as an atomic combined state.

The rejected handle receives the same one close attempt. Its result accompanies
the refusal. The relative path is derived lexically from the existing admitted
descendant and rejected if absolute/drive/parent-relative; long names are
length/hash-only. Neither a successful tag query nor failure to obtain it admits
the entry. The refusal is rethrown before the deletion loop, and rejection
journal failure also remains failure/HOLD. Existing gathered-handle cleanup
still runs. This does not establish that every close succeeds: unretained or
failed close outcomes remain qualifications, not permission to retry them.

## Timing qualification requiring care before any promotion

The 250-ms sample limit is a cooperative **call-admission check**, constrained
by the original drain deadline. It cannot interrupt a synchronous Win32 call,
required handle close or journal `fsync`. Additional metadata calls could
therefore delay reaching required termination even without resetting the drain
clock. This is an unresolved execution-admission question, not a demonstrated
safe timing fix or a reason to extend a timeout, add diagnostic workers, or
rerun the consumed request.

The author's sentence “No diagnostic query starts after cancellation/deadline”
must not be promoted as an atomic guarantee. A signal or elapsed deadline can
occur between `ready()` and the following call. This reviewer independently
identified that counterexample; the author separately acknowledged it while
preserving the exact v1 bytes. Before promotion, narrow that wording to the
actual sampled pre-call checks and explicitly retain the check-to-call race.
The existing note's broader soft-bound caveat is supported, but does not remove
that race or prove bounded synchronous API latency.

Future consideration would still require exact amended source and authority
bindings, evidence-volume/query-cap and cancellation/termination/handle-lifecycle
review, suitable independently reviewed controls, root's rationale and exclusive
slot, and a separately admitted cleanup contract. This source-only review
supplies none of that execution authority. It cannot be used as the accepting
Windows14 admission JSON or as permission to apply the patch.

## Preserved result and cleanup

Windows01 remains **operational FAIL / filesystem cleanup HOLD / 14 cases
UNSTARTED**. Owned Job zero was observed; root removal was not. Parent exit,
lingering child identity, reparse path/tag/target and residual namespace size
remain unknown for that consumed run. The hypothetical extra observations do not
retroactively fill those omissions or correct either unproved root cause.

No new product finding, executed case or closure credit is established. This
proposal does not affect the defined confirmed/suspicion denominators or PVD
owner decisions. PVU-007 STOP, PVU-011 NO RETRY, PVA-029 recorded FAIL/no automatic
retry, G7/G8 CLOSED and all protected-ref/non-publishing boundaries remain intact.

Only compact permanent review files were written. The reviewer created no
build/cache/temporary executable/archive or worker process; there is no wrapper
stop or generated-output deletion to perform. Resource point sample: about
24 GiB filesystem availability and 44.3 GiB available RAM. No unrelated process,
cache, source, test, SDK or toolchain was modified or removed.
