# Database01 E bootstrap v1 — source-only HOLD

Reviewer `/root/verification`; source/PLAN author and sole executor `/root`.
2026-09-09 UTC. **HOLD_SOURCE_REVISION_REQUIRED; execution count0.** This is a
review of the new bounded metadata bootstrap, not permission to execute an old
helper, the database runner, controls, cleanup or recovery.

## Exact reviewed bytes

All210 source lines/9284 bytes and55 PLAN lines/3468 bytes were read, and literal
SHA-256 hashes checked by data-read tool `312e99` (exit0):

- `reviews/linux-runner/BOOTSTRAP_E.py`:
  `cdd36fd49dac54781b3807e5f866f88e4ac9fbb06ccfb570fbfdd38fee876888`.
- `reviews/linux-runner/E-BOOTSTRAP-PLAN.md`:
  `6837d860f7a28cf827901a4c43a7493a0365f298bb0a756e61d3acc72406041a`.

Data-only tool `841f29` (exit0) preserved these exact bytes exclusively as
`verification/linux-database-e-bootstrap-v1.py.txt` and
`verification/linux-database-e-bootstrap-plan-v1.md.txt`. They are permanent
source evidence, **not executable/importable alternatives**. The earlier bounded
design review `5848cda2…` did not approve these implementation bytes.

## Two challenged issues, independently sustained by root

### E-B01 — missing externally comparable original journal identity

`last_pin` internally binds the exclusively created journal and every completed
append against descriptor/path identity, stable original fields and expected
size. This is a useful guard, not a rejected implementation of stable reading.
However, neither its original creation tuple nor last completed append tuple/
state is serialized. The terminal result contains the E original on success,
not the journal original. Therefore root's planned post-return journal stat/hash
cannot compare that file to its originally allocated tuple using retained
receipts; a current tuple must not be silently adopted as an original.

Minimal requested remedy: retain the initial journal tuple, last **completed**
append tuple/byte count and whether an append was in progress. A partial write
or failed fsync must remain distinguishable from completed evidence; the last
completed tuple must not be labeled proof of a later incomplete write. No
journal repair, append retry or replacement adoption is requested.

Root independently challenged/sustained this concern in collaboration and will
revise its source. This is an operational-evidence issue, not a PVA application
finding or a claim that mutation occurred in an unexecuted program.

### E-B02 — timeout command/prose scope mismatch

The proposed unqualified command `timeout -k 5s 20s ...` does not bind an exact
utility, and GNU timeout's default behavior uses a process group. Thus the
PLAN's "external timeout owns only this child" statement is not the default
command's direct-child signaling contract. The helper itself starts no children;
that guard narrows the practical intended group but does not change utility
semantics or justify an inaccurate authority claim.

Requested remedy: an exact independently bound `/usr/bin/timeout --foreground
-k 5s 20s ...` direct-child invocation, with ordinary installed-tool trust and
timeout/interrupt/blocked-syscall limitations stated, or a separately justified
and admitted isolated-group scope. No utility invocation or behavior probe was
performed by this reviewer. Root independently sustained the mismatch and chose
the foreground/direct-child revision; the revised PLAN still needs review.

## Surrounding guards and nonissues preserved

- Fixed Linux absolute entry, isolated/no-bytecode/nonoptimized Python, exact
  resolved interpreter path; external source/toolchain byte freeze is still
  required and not proven merely by these program strings.
- Twelve original directory tuples use dev23. The original unchanged0600
  single-link lock uses regular-file dev24. These are deliberately distinct;
  no directory/regular-file same-device requirement is invented. This reviewer
  read declared bindings but did not probe those originals.
- Every required directory is opened no-follow through retained parents and
  checked against its fixed descriptor/path tuple. The lock has no create,
  truncate, write, chmod or replacement branch; its existing descriptor is
  checked and nonblocking-flocked. Root's fresh cooperative coordination remains
  necessary; this is not a hostile-UID sandbox.
- Journal creation is exclusive0600/no-follow. Its parent fsync and completed
  durable intent precede the one fd-relative mkdir0700. The child descriptor,
  pathname, uid/mode and emptiness are checked; parent fsync and a durable
  original-child event follow. Absolute absence checks do not authorize adoption:
  the later exclusive operations and original-parent checks still reject drift.
- The descriptor registration helper and reversed one-attempt closes cover the
  ordinary acquired-FD paths. INT/TERM/HUP/ALRM handlers latch, checkpoints refuse
  subsequent intended work, and terminal pending-signal sampling is explicit.
  Severe allocation failure, blocked syscalls, SIGKILL, host loss and signals
  after terminal commitment are not falsely covered by normal finally behavior.
- All ordinary errors remain retained HOLD. There is no E deletion, journal
  repair, second mkdir namespace, runtime/checkout allocation, child launch,
  lock write or stop/retry branch. An unrecorded/partial original must not become
  future database authority by taking a replacement/current pin later.
- 128MiB address-space/64KiB file-size limits, journal cap,15s cancellation alarm,
  root's proposed20+5s envelope and before/after resource floors are qualified
  per-invocation/point-sampled controls, not host-wide leases or hard syscall
  settlement proof. Root still needs an actual tool exit and post-return
  original-E/metadata evidence; no self-preterminal result can supply that exit.

## Status and resource accounting

Await revised exact source/PLAN and then actual root preflight/instance
admission. No invocation, import, compilation, syntax/utility probe, original
identity/process probe, build, test, temporary runtime, daemon or worker occurred
in this lane. Only data reads and compact permanent review/snapshot files were
created; wrapper stop is not applicable. Both issues are source-only operational
review findings, not application cases or closure credit. All STOP/NO-RETRY/
CLOSED, Windows HOLD, PVD/hardware and publication restrictions are unchanged.
