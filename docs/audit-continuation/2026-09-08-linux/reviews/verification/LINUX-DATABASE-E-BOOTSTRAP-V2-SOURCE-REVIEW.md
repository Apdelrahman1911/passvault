# Database01 E bootstrap v2 — source accepted, instance still pending

Reviewer `/root/verification`; implementation/PLAN author and sole executor
`/root`; 2026-09-09 UTC. Prior design consultation is disclosed in review
`5848cda2…`; this reviewer did not edit the bootstrap source or PLAN.

Disposition: **ACCEPT_SOURCE_DESIGN_ONLY_PENDING_ORIGINAL_INSTANCE**.
No bootstrap invocation, filesystem-original probe, utility probe, import,
compilation or runtime/control test was performed. This is not an actual filled
instance approval, database F acceptance or closer C acceptance.

## Exact bytes and correction review

Data-read tool `d43c52` (exit0) read **all220 source lines/9956 bytes** and
**all66 PLAN lines/4255 bytes**, verifying:

| File under `reviews/linux-runner/` | SHA-256 |
| --- | --- |
| `BOOTSTRAP_E.py` | `a49f750c0f8dc6a0527ee823717ec23b63031ab285fd1d297f23db3c2de77f6c` |
| `E-BOOTSTRAP-PLAN.md` | `87963b73231f34d55bf93487f960090d802dc029fe19d5b30feacc716224e87a` |

The data-only unified comparison `6fe017` (exit0) against preserved rejected v1
confirms only the added journal receipt fields/state assignments in source, and
the corrected timeout command/authority plus receipt explanation in PLAN. No
existing original-identity, exclusive-create, fsync, cancellation, resource,
finally/close or HOLD guard was weakened. The rejected original and root's
independent challenge remain in v1 review
`7e01ec39ddea6b5481a737880b65da066a081f8e2363cf6494c08ca94bbb2bec`;
they are not replaced by this acceptance or mislabeled executed failures.

- **E-B01 source correction accepted; actual receipt confirmation pending.**
  Creation stores `journal_initial_pin`. Before attempting any append bytes,
  state becomes `APPEND_STARTED_NOT_COMPLETED`. Only after full write, fsync and
  descriptor/path/original-field/size checks do the last-completed pin, completed
  byte count, completed-append count and completed state advance. A failed
  first write retains null last-completed pin; a failed second write retains the
  first completed pin plus in-progress state. A rejection before write intent
  can retain the previous state, but overall status stays HOLD. Severe failure
  can prevent output entirely; none of these fields promise recovery.
- **E-B02 source/command correction accepted; no utility execution proof.**
  The exact prospective command is now `/usr/bin/timeout --foreground -k 5s 20s
  /usr/bin/python3 -I -B <absolute BOOTSTRAP_E.py>`. GNU foreground semantics
  target the monitored direct child rather than the default process group;
  this helper has no child-creation branch. Root must bind the actual installed
  timeout and resolved Python originals before launch. Installed-utility trust
  is not a new utility implementation audit or a raw waitpid/PID census.

These are accepted source corrections to operational review issues, **not
completed runtime regressions, application fixes or new closure credit**.

## Whole-source boundary reviewed

The complete v1 guard analysis remains applicable: twelve fixed original
directory tuples (dev23, uid0), no-follow descriptor/path ancestry, separately
bound original0600/single-link lock (regular-file dev24), read-only original
lock open plus nonblocking flock, and no lock create/write/chmod/adoption.
Directory and regular-file devices are intentionally not equated. Declared
source constants are not fresh originals observed by this reviewer.

There are only two planned new objects: the exclusive0600 bounded journal
outside E and one exclusive empty0700 E under the existing original `runs`
parent. Durable intent precedes mkdir; original child identity/emptiness,
parent fsync and durable original-child event follow. Later absolute absence
checks do not substitute for these fd-relative exclusive operations. Root's
post-return E check is still needed after the final in-process observation.

Handlers latch INT/TERM/HUP/ALRM; checkpoints refuse later intended work.
Descriptor registration and reverse one-attempt close accounting cover ordinary
owned-FD paths; both meminfo reads also have context-manager close handling.
The successful source path would account for15 explicitly registered FDs
(12 directories, lock, journal, E), not a claim that such a path executed or a
count of all interpreter/stdio descriptors. Close errors/cancellation keep HOLD.
Terminal signal blocking/pending sampling is a bounded commitment, not a promise
about signals after that point, SIGKILL, severe allocation failure or host loss.

128MiB address-space/64KiB file-size limits, <=64KiB journal,15s cancellation
alarm, outer20+5s policy and12GiB/25% before versus8GiB/20% after resource floors
remain per-process or point-sampled limits. No blocked syscall is made a hard
deadline; forced/ambiguous termination cannot establish clean settlement. No
build, cache/runtime allocation, child launch, deletion, journal repair, old
helper import, alternative namespace, stop or retry exists in this source.

## Actual root instance still required before effect

Root must provide/revalidate source and PLAN hashes, original twelve-directory
and unchanged lock provenance, original interpreter/timeout bytes/metadata,
fresh resources, sole audit-local/CI coordination and producer freeze, and
actual E/journal absence. This reviewer still needs that concrete prospective
packet; these checklist conditions are not observations or approvals of it.

After the one separately admitted invocation, retain actual tool exit/output/
timing and interruption/timeout status. Compare the original initial/completed
journal receipts to the post-return file and the original E tuple to a still
empty0700 E. A successful source path expects two completed durable events;
neither that expectation nor a proposed status proves execution. Any error,
collision, drift, partial output, failed close, missing original or ambiguous
settlement is consumed HOLD, including an error before journal creation. Retain
the tiny namespace and evidence without deletion, repair, adoption or retry.

This lane produced compact permanent review documentation only. No runtime,
temporary artifact, cache, worker, daemon or emulator was created; wrapper stop
is not applicable. All STOP/NO-RETRY/CLOSED, Windows FAIL/cleanup-HOLD, PVD,
hardware and publication restrictions remain. Application executions and
closure delta remain0;19/25,22/37 and2/12 are unchanged separate denominators.
