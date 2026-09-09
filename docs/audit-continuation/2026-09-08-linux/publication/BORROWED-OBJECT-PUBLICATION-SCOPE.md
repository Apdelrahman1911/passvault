# Prospective C7 publication exception — not a Linux02 recovery

Author: `/root`. **Pending independent review; no Git mutation admitted by this draft.**

The consumed Linux02 PLAN lines42–43/61–63 and request `coordination.source_store`
prohibit source-store writes generally while its checkout borrows C4 objects.
They did **not** already exempt additive publication. Their frozen wording,
failed result, unknown process provenance and cleanup HOLD remain unchanged.

This separate prospective exception is only to publish compact continuation
source/evidence on `codex/audit-continuation-linux-20260908`. It neither operates
on Linux01/02 nor uses their checkout, caches, processes or admission. Release
of the completed scheduling reservation must be recorded first; that release
does not assert complete historical descendant settlement.

## Narrow change authority

- Shared object store: `/root/projects/PassVault/passvault/.git/objects`.
  Preserve every existing regular object, pack/index and metadata file, and all
  existing directory identities/modes. Permit only new Git objects and the
  necessary new fanout directories. Transient Git writes must use exclusive new
  temporary names; only temporaries created by that invocation may be removed.
  No existing object/temporary removal or failed-removal retry is allowed. Existing
  directory timestamps can change from additions; do not claim byte-identical
  filesystem metadata. No overwrite, removal, GC, prune, repack, maintenance,
  alternates/promisor/shallow/replace introduction or store-configuration edit.
- Worktree: `/root/projects/PassVault/passvault-linux`. Only its own index,
  commit message and corresponding lock/HEAD-log metadata may be updated by
  ordinary selected-file `git add` and `git commit`. No checkout/reset/clean,
  stash, hooks, signing, permanent-source deletion or unselected-file staging.
- Refs: only fast-forward the dedicated continuation branch, its corresponding
  reflog and dedicated `origin` tracking ref/log. Push only
  `--no-signed --no-follow-tags --recurse-submodules=no origin
  HEAD:refs/heads/codex/audit-continuation-linux-20260908`, without force or tags.
  Main/testing/release/handoff refs and every tag remain unchanged. Public ref
  comparison uses `ls-remote`; no fetch is needed if those refs are unchanged.
- Git commands explicitly set `core.hooksPath=/dev/null`, `core.fsmonitor=false`,
  `gc.auto=0`, `maintenance.auto=false` and `commit.gpgsign=false`. No repository
  or global configuration is changed. Reuse the configured credential provider
  without reading/printing tokens or signing material.

## Bounded preservation and evidence

Before mutation, root records a no-follow stable-read inventory of the existing
object-store regular files (relative path, SHA-256, size, mode and identity),
original directory identities/modes, C4/C6 commit/tree, dedicated ref and
protected local refs/tags. Reject unexpected symlinks/specials or drift. Cap the
inventory at 100,000 files and 512 MiB; it is a read-only preservation check,
not a build, pack decoding audit or new product verification.

After staging/commit and again after push, compare every pre-existing entry
against that original inventory. Additions are separately accounted; preserve
C4 object availability and both preserved-checkout and continuation identities.
Record exact selected paths, source hashes, Git exits, staged whitespace results,
commit/tree and remote branch result. No bulk add or evidence-byte trimming.
Unexpected change means stop and report, **not** reset, repair, GC or deletion.

Only necessary permanent compact evidence is added. No application artifact,
runtime checkout, Gradle distribution/cache or private data is published. Git
commands are foreground and bounded; no build/test worker is launched. No new
wrapper-stop obligation arises from publication. Existing stop/HOLD obligations
are not discharged by Git success or a clean point observation.

The reason this is safe despite retained Linux02 uncertainty is preservation,
not retrospective worker clearance: old borrowed bytes and their accessibility
remain intact even for a hypothetical still-reading descendant. Independent
review must challenge that distinction. This is a cooperative preservation
scope, not an adversarial-writer sandbox: it does not clear an unknown historical
writer, guarantee continuous absence of unrelated writes, or make an inventory
prevent mutation. Any observed drift stops publication without repair. This exception does not admit another
build, namespace probe, cleanup, recovery, adoption or retry.
