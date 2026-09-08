# Git handoff transport receipts

This directory records publication of an **incomplete remediation checkpoint**,
not an app release. Start with [`../audit-handoff/START_HERE.md`](../audit-handoff/START_HERE.md)
and retain every boundary in `PERMISSIONS.md`.

## Identity

- Branch: `codex/remediation-handoff-20260908`.
- Audit/remediation base: `0dbc12c7f1b7770e75963c751c8c67af6e8b057a`.
- Original baseline Git tree: `20fb8f6f2c6fb11ad99af80c170b4150c16beb6f`.
- G12 raw source manifest canonical SHA256:
  `7fd2dab3eac6269baede8a0ca550f0cbdfee20391a12dfbd9ac84cdac5401d91`.
- Actual **source-only staged Git tree**, before adding handoff documentation:
  `4ec996664c01e9e8c203683e54f251606177e478`.

`SOURCE-GIT-INDEX.json` binds all 811 source Git blobs and their modes: 797 retain
the original raw hashes; 14 have the explicitly calculated Git-storage line
normalization. Ordinary checkout has a different raw-byte partition: 809 raw matches
and two declared PowerShell EOL exceptions. The exact originals are retained.
Use actual clone receipts, not these expectations alone, as checkout evidence.

The containing commit also includes handoff documentation and evidence.
Use `git rev-parse HEAD HEAD^{tree}` for its identity. Later receipt-only commits
do not silently add a product fix; each receipt states the exact payload it
checked. Do not confuse a prior payload commit with a later containing commit.

## Evidence interpretation

- `ASSEMBLY-COPIES.json` maps exact preparation records to their original local
  paths. Those historical paths and device/inode identities are not authority
  to operate on a clone.
- `TRANSPORT-CHECKS.json` records read-only reader invocations, expected and
  observed outcomes, hashes and exact commands. An intentional rejection is a
  transport guard check, not an application test failure or test-case count.
- `SOURCE-GIT-INDEX.json` and staging receipts describe actual index identities.
- Remote/clone/cleanup receipts, when present, describe only their named commit
  and time. A clone verification is not a new application, platform, hardware,
  artifact, store or publication-readiness test.
- Preparation failures and unexecuted drafts are preserved, not rewritten into
  passes. `../audit-handoff/ASSEMBLY.md` explains the candidate-reference locator
  limitations and explicit omissions.

No Gradle, Xcode, native build, application test or app launch was performed for
this handoff. No dependency verification was disabled. Git hooks/signing can be
disabled **for these Git transport commands only**, to avoid starting unreviewed
hooks or accessing private signing material; that is not a quality-gate pass or
a repository security-setting change.

No main/testing/release merge or movement, tag change, version/build-number
change, store upload, deployment approval, signing or app publication is part of
this handoff. Candidate/build **1017001 stays separate and unchanged**.

The primary checkout's explicitly permitted 22 local content hashes and the
original remediation worktree's 811 raw tuples were compared with retained
identities. This is bounded preservation evidence, not a claim to inspect
excluded private files or prove continuous preservation of all unrelated work.
Only explicitly created disposable verification paths may be cleaned. Source,
permanent tests, frozen audit evidence, shared caches and unrelated processes
must be preserved.
