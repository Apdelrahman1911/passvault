# Prospective C7 publication exception: independent source review

Reviewer `/root/native_review`; author `/root`; 2026-09-09.
**QUALIFIED SOURCE ACCEPTANCE of the amended prospective publication policy.
Not execution permission, retrospective Linux02 admission, settlement proof or
a cleanup/HOLD release.** No Git command was invoked by this reviewer.

## Exact sources and preserved first-draft objections

Read `publication/BORROWED-OBJECT-PUBLICATION-SCOPE.md` in full before and after
the author's narrow corrections. Paths are relative to
`docs/audit-continuation/2026-09-08-linux/`.

| Revision | LF / bytes | SHA-256 |
| --- | ---: | --- |
| First draft, challenged | 65 / 4121 | `b32161b9c8646337d6c070b8096b24bb4b037559c7b99feaa115728767d1914e` |
| Amended scope, accepted with qualifications | 71 / 4621 | `bfa801d07d6f53eff5221f62c8224324206513fe0557761b3fdfbc9fe1f6c476` |

Independent counterexamples were **source/configuration traces, not executed
Git controls or claims about the current effective Git configuration**:

- First-draft `commit.gpgsign=false` does not disable configured `push.gpgSign`.
  An otherwise ordinary push could sign a push certificate despite the no-signing
  boundary. The amended exact push vector adds `--no-signed`.
- An explicit single branch refspec does not negate `push.followTags=true`.
  Annotated tags could be followed despite the no-tags boundary. The amended
  push adds `--no-follow-tags`, plus `--recurse-submodules=no` to avoid inherited
  recursive push behavior extending remote effects beyond this branch/repository.
- “Owned transient Git writes” did not identify how ownership was acquired or
  exclude an existing temporary. Amended lines21-23 require exclusive new names,
  permit removal only of temporaries created by that invocation, and prohibit
  existing object/temporary removal and failed-removal retry.

The author agreed and made these exact narrow corrections. No broad new runner
or containment framework was requested. The first draft's objections/hash are
preserved here rather than treating it as having already supplied these guards.

## Why the preservation distinction can support this narrow policy

Linux02's frozen PLAN/request prohibited source-store writes generally. There
was **no pre-existing append-only exception**. The new policy acknowledges that
fact and is prospective only, after a recorded scheduling release that does not
clear historical process uncertainty or filesystem HOLD.

For a reader resolving an already-existing C4 object by its object ID, adding
different objects does not require changing the old file/path/content. Keeping
all pre-existing object/pack/index/metadata bytes, identities/modes and access
intact can therefore preserve that reader's inputs without pretending the reader
has exited. Continuation-owned index/commit metadata and its dedicated refs are
separately restricted; the held checkouts are not reused or modified.

This is **cooperative fixed-object preservation**, not a claim that enumeration
or directory timestamps are unchanged, not arbitrary reader/writer containment,
and not proof that an unknown historical writer is absent. Additions are visible
to an object-store enumeration. Before/after inventories detect covered drift;
they do not prevent concurrent mutation or prove continuously unchanged state.
The amended text explicitly retains these writer/inventory limitations.

## Obligations retained before any C7 mutation

- Root must record the scheduling release and actual bounded, no-follow baseline
  inventory, C4/C6 identities, dedicated ref and protected local refs/tags. This
  source review did not acquire those inventories or inspect held runtime state.
- Execute only selected-file publication within the amended metadata authority,
  with the explicit per-command hook/fsmonitor/GC/maintenance/signing guards and
  corrected push flags. The policy does not authorize an unexpected filter,
  callback, extra path, alternate store configuration or background operation
  merely because a Git command requested it. Stop rather than broaden the scope.
- Check all pre-existing entries against the original inventory after stage/
  commit and again after push; separately account for additions and preserve
  object accessibility. Record exact paths, source/commit/tree identities, exits,
  whitespace results and remote-ref evidence. No bulk add or evidence trimming.
- Any unexpected state/change is a failure to report, **not** permission to
  reset, repair, prune, repack, collect garbage, remove an old temporary or retry
  cleanup. Git success cannot discharge existing stop/HOLD obligations.

No remaining material source objection was identified within those expressly
limited assumptions. Actual preservation/publication still requires its own
results; this note is not evidence that C7 ran or that any old worker settled.
All STOP/NO-RETRY/CLOSED, Linux02 cleanup HOLD, protected-ref/tag, signing/store,
identity/dependency/build1017001 and hardware boundaries remain unchanged.

Only this compact permanent review was written. No Git invocation, environment
probe, held-scope read, helper execution/import, build/test, mount, recovery,
cleanup, runtime artifact or count/closure change occurred.
