# Linux database closeout — independent, bounded design challenge

Reviewer: `/root/baseline_coverage`. Runner author: `/root/storage` with later
root corrections. New fixed closeout implementation author: `/root/editor`.
This reviewer supplied design advice and will independently challenge that code;
this is not an independent rediscovery of that advice, a full runner review,
execution admission, deletion permission or an executed cleanup test.

## Disposition

**Accept this narrow design direction; implementation and filled-instance review
remain required before build admission.** Root/verification retain full runner
F01–F07 review. The Windows CI slot is reserved; no local build, test, inert
execution, runner import or cleanup execution is authorized by this note.

The runner deliberately retains outputs. Its aggregate `os.walk` inventory is
resource evidence, not deletion authority. `RESULT.json` is explicitly
preterminal, not a successful closeout receipt. Original directory FDs close at
runner exit. The separate closer must reopen only against already-recorded
original identities and then retain descriptor authority throughout traversal.

An earlier whole-runtime removal suggestion is **withdrawn**. Root explicitly
requires the isolated source checkout to remain, not merely the primary source.
No wholesale removal of `audit-runtime-linux-db-01` or its `checkout` is allowed.

## Exact protected and disposable scope

Always preserve R, its checkout, `.git`, all 1,198 tracked source/test/report
members, permanent tests, all source schemas, E and original run/admission
evidence. `core/database/schemas` is a tracked Room source/output boundary;
**new schema files there are protected too**, even if a drift gate fails.

Within every disposable target, retain every `reports/` and `test-results/`
subtree **in place**, including XML originals already copied into E. A copy
elsewhere does not authorize deleting its original report. Retain their needed
ancestors; a partially cleared build/cache root with retained reports is not a
fully removed root. Keep `.git` protected wherever encountered. No unrelated
cache, SDK, toolchain, checkpoint or original/continuation checkout is a target.

The complete candidate allowlist has **33 original roots**:

- Eleven private runtime roots: `home`, `tmp`, `jna`, `sqlite`, `gradle-home`,
  `konan`, `xdg-cache`, `xdg-config`, `xdg-data`, `xdg-state`, `android-user`.
- Twenty-two generated roots under `checkout`: `.gradle`, `.kotlin`, `build`,
  plus `build` under each of these nineteen exact project directories:
  `app-android`, `app-desktop`, `shared`, `core/domain`, `core/database`,
  `core/crypto`, `core/security`, `core/designsystem`, `core/navigation`,
  `core/otp`, `core/testing`, `feature/onboarding`, `feature/unlock`,
  `feature/vault`, `feature/credential`, `feature/generator`, `feature/health`,
  `feature/settings`, `feature/backup`.

No glob-based `**/build` discovery or arbitrary target CLI is acceptable. The
current settings declare no included `build-logic` project: that initial generic
suggestion was corrected after a read attempted the absent file. The failed
`cat` was an inspection diagnostic, not an application failure. Build-config's
source review agrees with this output-root list but does not establish the
realized Gradle graph or guarantee no unknown output will be produced. Unknown
outputs remain explicitly retained, not silently added to cleanup authority.

## Small runner prerequisite, not a second recovery framework

Root approved prospective preallocation of the 22 generated roots after the
fresh 1,198-member clone check and before Gradle. Check **all** targets absent and
reject equal/descendant collisions with tracked source paths before the first
mkdir. Use the existing exclusive `mkdir` with durable `allocation_intent` and
`allocation_original` records and original source-parent descriptors. This gives
each cleanup root real original authority; do not acquire it from post-run names.

The existing R plus twelve private allocations supply R, checkout and the eleven
other roots. Missing originals after partial allocation mean explicit HOLD for
the fixed full contract. Do not fill missing pins from present objects, recreate
directories or reuse the consumed run. A separate safe disposition of a small
partial-allocation residual is not an automatic retry of this helper.

## Admission and stop/settlement gates

Bind the new helper/plan, exact run REQUEST/SOURCE, original allocation records,
original directory map, run journal, terminal result and external launcher
receipt to the same fixed `linux-database-01` instance. Validate reviewer-authored
acceptance, not root labels attached to unrelated hashes. Current root/parent,
lock and evidence identities must match their original seals. A separate
O_EXCL closeout intent/journal in protected evidence consumes the closeout once;
never edit/normalize the original run journal or RESULT.

Acquire the same original, nonblocking fresh Linux coordination lock. Confirm
root's no-other-local-or-CI-job attestation and fresh conservative owned-process
settlement before any deletion. The closer should run no Gradle, process kill,
recovery, namespace replacement or stop retry.

- A recorded original wrapper stop must have actually started/completed with
  exit 0 and no ambiguity, and owned workers must be settled.
- Alternatively, the complete durable journal must establish that no Gradle
  launch/stop obligation was ever committed and all Git/other owned work settled.
- A nonzero build or failed XML test gate does **not** itself forbid safe cleanup
  when original stop/ownership/evidence gates are satisfied. Do not require a
  manufactured application PASS to reclaim caches.
- Failed, timed-out, unstarted or ambiguous stop retains the original HOME,
  TMP, cache, wrapper and run state. No success merely because a process name is
  absent. The fixed closer must HOLD rather than erase that outstanding authority.

Preserve all necessary bounded command/log/XML/source and failure evidence first.
Keep terminal exit/journal consistency distinct from a preterminal RESULT field.
Missing terminal evidence, uncertain process ancestry, changed pins or unclear
stop disposition requires explicit residual reporting and separate review.

## Descriptor-only bounded traversal

Perform a complete bounded preflight under retained original parent/target FDs
before the first unlink. Root seals come from original allocation records;
descendant snapshots describe contents observed under that sealed private root,
not newly witnessed creation provenance. Freeze cooperative producers throughout.

Use `scandir(fd)`, no-follow descriptor-relative stat/open, verify fd and path
identities, then `unlink(name, dir_fd=parent)` / `rmdir(name, dir_fd=parent)`.
Never check a path then hand it to pathname `rmtree`, `rm -rf` or `git clean`.
Revalidate recorded originals before each destructive step. Reject symlinks,
special files, foreign UID, hardlinked regular files, unapproved device changes,
mounted descendants and tracked/protected collisions.

This VPS's observed directory/regular-file device split is currently 23/24.
The filled admission must bind fresh actual directory and regular-file device
identities separately; require same-kind consistency, **not** directory==file
device equality and not an unbounded cross-device exemption. A filtered mount
table check must reject mounts at/below targets, including same-device bind
mounts; do not retain unrelated mount paths. These checks are point observations
under a cooperative freeze, not protection against hostile same-UID/kernel races.

Retain compact per-target inventory hashes/counts/logical bytes and a durable
deletion intent before touching that target. Bound entries, bytes, nesting,
memory, time and journal growth. Preserve reports without traversing their
content for deletion; retain enough metadata to account for their cost.
Record actual removed counts and parent fsync plus `REMOVED` or
`CLEARED_ALLOWED_MEMBERS_PROTECTED_REPORTS_RETAINED` as appropriate. A partial,
interrupted, replaced or ambiguous target stays HOLD; absence alone is not
successful deletion. Never reinterpret failed intent as a fresh target identity.

Install monotone INT/TERM/HUP interruption handling before operations; do not
keep deleting after observed cancellation. Close only owned descriptors. Hard
kill, host loss, uninterruptible kernel operations and the non-atomic interval
between final identity check and unlink/rmdir remain explicit limitations.

## Accounting and independence

Retaining the useful small source checkout and compact reports is intentional,
not abandoned build/cache cleanup. Report retained logical/allocated bytes,
deleted eligible bytes, actual disk/RAM point observations and any residual
separately; deleted-byte accounting is not measured net filesystem reclamation.

No new PVA, closure, test case or semantic application LF credit follows from
this work. All original coverage conditions, PVU-007 STOP, PVU-011 NO RETRY,
PVA-029 FAIL/no automatic retry, G7/G8 CLOSED, owner-design choices and candidate
1017001 restrictions remain unchanged. Exact source review of editor's new
helper and final filled original-instance bindings is still pending.
