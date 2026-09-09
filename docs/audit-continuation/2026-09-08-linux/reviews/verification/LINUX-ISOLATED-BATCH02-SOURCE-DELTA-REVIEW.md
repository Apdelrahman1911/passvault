# Isolated-batch02: independent source binding-delta review

Reviewer `/root/verification`; 2026-09-09.
**SUPPORT_FROZEN_BINDING_DELTA_ONLY. No actual02 request or instance is accepted.**

Tool `d27057` completed0: independently hashed all three old/new source pairs,
read their COMPLETE textual diffs and the new plan. The diff commands correctly
reported differences; these are source comparisons, not test cases. No repeated
whole-body audit, subject import/execution/syntax check, Git, process/namespace/
mount/CI probe or runtime access was performed.

## Frozen tuples and reused review

Repository root W=`/root/projects/PassVault/passvault-linux`;
B=`W/docs/audit-continuation/2026-09-08-linux`.

| New source | SHA-256 | Bytes/LF |
| --- | --- | --- |
| W/scripts/audit/linux_isolated_batch_02.py | `a1da36534c856ef9a5dddab303c7ad397b3bf2e7ca5d2ab5c07f4685d8255a31` | 37177/669 |
| W/scripts/audit/isolated_batch_tests_02.init.gradle | `b8c443198bf6cd33860a704d0ee5bb6d054eae877b92bd17187f20d6e844fdcc` | 5430/101 |
| B/reviews/isolated-batch02/LAUNCH.py | `f65aecef66207da898c7bfda7d966ea235a2fe70f96b523ff977f32f8a80a6af` | 34828/612 |
| B/reviews/isolated-batch02/PLAN.md | `1080173c56944a51a163452603785ccc83009438e5bf18bc44e18d15a0e6acdd` | 4696/78 |

Old source files remain unchanged, independently rehashed:
inner `15cbad1ce8af84d6fb1f56a92ba70494148ffc0a19e3ca9af7eb16d2dfa0ada5`;
init `24a301c6cc8818eef5e61c8a10fe03a4b4f781bd4abac4ab4266bc6985583ab1`;
outer `c5e0a4b5ca35a9dced05e2b3a2795d9a0d4169629c8ce85b2d963b28a4cce405`.
Reuse only their source-semantic reviews/qualifications:
`LINUX-ISOLATED-BATCH01-INNER-SOURCE-REVIEW.md` SHA
`2b0ea3cb39264ea17a234b66e031846054c3657de38615cec759fe0d3f31501d` and
`LINUX-ISOLATED-BATCH01-INSTANCE-REVIEW.md` SHA
`2fd0656d4da3a6d06a1dfee93107022c66b2c49f72f1d7566b710aa32186732c`.
The latter's old exact-instance admission DOES NOT transfer to02.

## Complete delta and compatibility checks

- Inner changes five lines only: R/E, SELF+INIT filenames, RUN_ID and init hash.
  Init changes three lines only: two instance labels/error text and runtime.
  Outer changes only its instance label, R/E/SELF/REQUEST/APPROVAL, INNER+INIT
  filenames, RUN+PURPOSE and the two fixed input hashes. No function-body,
  selection, parser, namespace/host-screen, cancellation or cleanup-guard change
  is hidden in the complete diffs.
- R consistently becomes `/root/projects/PassVault/audit-runtime-linux-isolated-batch02`
  in all three sources. E consistently becomes `B/runs/linux-isolated-batch02`.
  Inner CHECKOUT, private HOME/temp/cache/worker roots and original-directory
  sets derive from this R; the init's independent worker-root literal matches.
- Outer SELF is `B/reviews/isolated-batch02/LAUNCH.py`; INNER matches the new
  inner SELF exactly. Both select the new `_02` init filename. Inner and outer
  init pins equal the independently hashed new init; outer inner pin equals
  the independently hashed new inner. No old executable filename is selected.
- Outer REQUEST is `B/requests/LINUX-ISOLATED-BATCH-02.json`; APPROVAL is
  `B/reviews/verification/LINUX-ISOLATED-BATCH02-INSTANCE-ACCEPT.json`.
  Inner RUN_ID and outer RUN both equal `linux-isolated-batch02`; outer PURPOSE
  is `ONE_LINUX_ISOLATED_BATCH02`. Existing v1 formats/exact11-key request and
  seven-key approval checks remain unchanged. A copied old approval would fail
  new run/purpose/request/source binding; ordinal renaming is not admission.
- The old `reviews/build-config/current-cycle01` paths intentionally remain as
  immutable CLASSES/METHODS DATA references, not old runtime/execution reuse.
  Product source remains exact C4 commit
  `da8ff89b9a8579017d5d524e628dff5251f2bb90`, tree
  `cf0a8a702e7cd6948236be18b491bb5a21b5886e`, all1572 raw Git blob members with
  both checkout-EOL qualifications. Neither C8 publication nor selected-source
  compatibility substitutes for this full-tree identity.

## Unchanged boundaries; no launch/result credit

The plan correctly conditions02 on the Windows job having settled, genuinely
fresh external coordination/resources, clear preflight and separate root plus
independent exact-instance admission. This reviewer did not check runtime-name
absence, current tool/parent/lock pins, PID389, CI or any other live state here.
Those remain actual-packet/entry requirements, including root's external source,
request and genuine approval freeze immediately before entry and under lock.

Old01's independently reconciled preallocation refusal remains consumed HOLD;
actual review SHA `e09558d86e30dc4ff9ec294cc6e2fc2568eea75d80abd0c5b3b1b6f633e0295e`.
Its scheduling-only release, fresh-name absence or a later non-observation of
Java389 is NOT global-idle/provenance proof, a process exemption, cleanup/adoption
permission or automatic retry authority. No such exception was added here.

Five phases still declare166 regressions plus one separate producer/17 expected
XML captures, all UNSTARTED by this work. Private daemon-channel, original
pidfd/mount binding, signal-mask, bounded host-screen, exact source/XML,
resource-point-sample, once-only stop and descriptor-bound deletion semantics
and their cooperative-host/syscall/hard-interruption limitations are unchanged.
All PVU-007 STOP, PVU-011 NO RETRY, PVA-029 no automatic retry, G7/G8 CLOSED and
old Linux/namespace/Windows/macOS restrictions remain. No physical/native,
Android32, rendered Desktop, compatibility redesign or closure credit follows.
Counts remain19/25 original confirmed,22/38 all confirmed,2/12 original suspicions;
eight PVD explanations stay separate.

Only this compact permanent source-delta report was written. No future02 approval
JSON, temporary files, caches, runtime allocations or owned workers were created.
