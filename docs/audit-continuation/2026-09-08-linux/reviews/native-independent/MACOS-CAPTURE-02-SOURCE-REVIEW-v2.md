# Mac source-data reader v2: independent source challenge

Reviewer `/root/native_review`; author `/root/native`; 2026-09-09.
**QUALIFIED SOURCE ACCEPTANCE of C02-R1/R2/R3 corrections only.**
**WRAPPER / ORIGINAL INSTANCE / NETWORK / EXECUTION ADMISSION: HOLD.**

Read the entire frozen reader (330 LF /15,395 bytes), SHA-256
`32fdca27e7e70573b0b5888809a955bdb4d76974400becd2054abb825c7901c4`,
and proposal (199 LF /12,459 bytes), SHA-256
`697b7e112ca9debac318c67c1890d10cd3645d97ded352e15c4dd80be1b90fdf`,
as inert source/data. No reader execution, import, syntax probe, GET, CI query,
target/API probe or process control was performed. The separate capture02
evidence parent remained absent at the review's passive namespace observation.

The prior rejected reader/proposal were independently hash-checked in their
verbatim `rejected-v1` archives: `b963963180e12dcf952f5356a7a06d3364450150804bc23e25c25a4da984a1f9`
and `313d6f0d464db721c5462ed3b36c0a64ce1c1a602f2eaee146c868ed26f72bbc`.
The v1 independent REVISE report remains authoritative for those bytes; its
Markdown hash is `707736efca262228a2cb0e7b701afd3bf8eff8bb55cc6336afbf80a8ec60a44b`.
This new report does not replace the recorded failed capture01 or grant a retry.

## Independently traced corrections

### C02-R1 — Rejected parent is now close-only

Reader230–239 initializes the admission flag false, opens the directory inside
the guarded scope and sets the flag only after successful fstat/UID/private-mode
checks. Finally315–324 gates both report creation and directory fsync on that
flag; the original descriptor's one close attempt remains outside the gate.
The previous writable0755-directory counterexample now reaches no parent write
or fsync. Failed open has no acquired descriptor to close. Failed fstat, owner
or private-mode check takes the close-only path. This is a source trace, not an
executed permission/close test. A successful close allows the failure JSON to
reach stdout; a close error or abnormal interruption may prevent that receipt.

### C02-R2 — Ordinary acquisition gap removed

Record construction/time acquisition230 and sentinel setup231–233 precede the
try234 and open235. All ordinary post-acquisition operations are now inside the
finalizer's scope. Parent write/serialization/fsync failure still traverses the
nested close-finally. No close retry or deletion was introduced. This removes
the identified ordinary allocation/stamp gap, not asynchronous interruption
between a syscall and assignment or a guarantee that a close attempt succeeded.

### C02-R3 — Stale/misfiled role checks added

`verified_record`112–185 now requires object records and the finite metadata
capture/id/repository/official URL, exact key inventory, bounded exact-integer
response cap/body count, accepted parse status and shaped SHA-256. The caller
`verified_commit`188–193 additionally requires a shaped commit SHA. The previous
different-role metadata plus currently labeled transport counterexample fails
the metadata's own role/endpoint checks even when the two supplied URLs/digests
match each other. Integer checks reject bool/float exit and metadata bounds.

For CMake process metadata,125–157 resolves the accepted CMake commit and binds
the file URL, path, download URL, selection and bounded source identity to it.
The mention observation must be a bool;253–256 requires true before the last
slot. This is consistent for the externally frozen original dependency records,
not an atomic multi-file snapshot if those records change between reads. Root
still must read the accepted process source and establish semantic need before
the conditional intent/GET. A substring mention alone is not that proof.

All three issues were infrastructure source corrections, not product families.
No test, runtime parser success or new PVA closure is credited.

## Deliberately narrow acceptance

- Direct file argv removes the failed shell/JSON `-c` quoting mechanism. The
  reader contains no network/subprocess/downloaded-code evaluation or target
  work. This manual read does not establish installed Python compatibility,
  syntax/runtime success, trusted startup or correctness of a future shell.
- Duplicate keys and nonfinite **literal constants** are rejected. This is not
  a blanket numeric-range validator: JSON exponent overflow in an unused value
  may become an infinity in Python. The author independently disclosed the same
  qualification after the frozen full read. Consumed size/cap/exit/source count
  fields require bounded exact ints. Read the proposal's shorthand nonfinite
  rejection with this qualification, not as rejection of every such numeric
  value in every unused field.
- File-intent dictionary equality250–252 is value equality, not a strict type
  schema: a numerically equal floating-point cap can compare equal. The actual
  URL/cap still comes from the fixed inventory/validated commit; that does not
  enlarge a request or body bound. The outer owner must emit the exact declared
  integer-cap intent, not infer strict intent typing from this equality.
- File-dependency section validation154–155 is a nonempty-list check, not a
  validation of each section entry, content re-read/hash or source semantics.
  UTC/qualification and all transport extras likewise are not a fully typed
  receipt schema. Original producer/record/source binding remains external;
  shaped hashes and the `/root` label do not authenticate provenance.
- Strict Base64, UTF-8, actual Git blob size/hash and commit-pinned response
  metadata checks remain. LF-only merged section ranges/caps do not prove that
  bounded excerpts include all needed platform semantics. Empty/unhelpful or
  missing candidate material cannot grant target admission.
- The 12 finite response slots still sum to1,802,240 accepted-body bytes, not
  measured wire/RAM/retained-output consumption. Cap+1 detects an oversized
  stdin prefix; accepted shorter reads depend on the intended blocking-pipe
  EOF. Actual curl0 and reader0 remain independently necessary even if parsing
  finishes. The best parser status remains BODY_PARSED_TRANSPORT_UNADJUDICATED.
- Exclusive source/report writes and once-only closes can fail after partial
  permanent output. An exception can supersede an earlier diagnostic, and
  stdout can fail/block. No false success can be inferred solely from a partly
  written BODY_PARSED record. Outer actual statuses, bounds and unknown-state
  retention are mandatory. No cleanup or close-success claim is made here.

## Still missing before any separately authorized instance

Root must supply and obtain independent review of the exact Linux wrapper and
filled original-namespace instance. Requirements from the v1 review remain:
fixed curl/Python identities, trusted isolated startup/minimal environment,
exact argv and immutable source hashes; original checkout/evidence ancestry and
current owned private parent identity; durable exclusive per-ID attempts and
preexisting-output rejection; exact URL/integer cap and observed transport
binding; immediate capture of **both PIPESTATUS** values and whole-batch
fail-fast despite errexit/failure/cancellation; finite outer timeout and owned
child settlement; installed curl stream-limit behavior; durable compact
receipts and source records. The reader does not implement those authorities.

A UID/private-mode check does not itself establish original parent ancestry or
an adversarial same-user boundary. No new parent, attempt, transport record or
reader invocation is admitted by this report. Failed/ambiguous observations
remain HOLD, not permission to retry or reconstruct an earlier helper.

## Preservation, accounting and resources

Capture01 remains three failed metadata pipelines (curl23/reader1), zero verified
response/commit/source identities and nine unstarted source slots. No archived
application/recovery helper or unadmitted old runner was executed or imported.
Capture02 requests/executions/tests remain0. Mac ABI, toolchain equivalence,
available-memory/ACL/process primitives and hardware behavior remain unproven.
Windows01 operational FAIL/filesystem HOLD/consumed request and14 unstarted
cases, PVU-007 STOP, PVU-011 NO RETRY, PVA-029 FAIL/no automatic retry, G7/G8 CLOSED
and protected-ref/publication/version/dependency/identity/build1017001 fences
are unchanged. No product denominator changes follow from this review.

The passive repository observation was HEAD
`bb094f8a39ba43f1ce1f43f394cb662b03febe38`, tree
`82967f050816454b6905d560dfec15d0e0b091f5`; it does not claim these frozen input
bytes were committed or the concurrent shared worktree was clean. File hashes
above are the reviewed identity. A passive resource sample showed24,028,572KiB
filesystem availability and39,108,296KiB MemAvailable; these are point values,
not future admission or resource guarantees.

Only this compact permanent review pair was created. No reviewer-owned build,
cache, temporary executable, background worker or evidence parent was created;
no deletion, stop, termination or recovery was performed. Gradle --stop is not
applicable to this source read and discharges no historical obligation.
