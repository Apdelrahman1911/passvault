# Completed-fix consolidation (2026-09-15)

C20 investigation expansion is paused by the owner. This is a selective integration,
not an audit restart, blanket merge, new release candidate, or beta-readiness claim.

## Source and preservation

- Main baseline: `0dbc12c7f1b7770e75963c751c8c67af6e8b057a`.
- Published continuation: `df30e43c26184c30a9845c80c368d3ad5413f125`, tree
  `3c20f739e96f1897324a88ec08076461341b4f8b`.
- Original handoff: `9bdf9559b7a801a6f2fa49be3d7836ea6ef4d0ed`.
- Testing baseline: `2ae65df7111a9c5493740e8b772932be77eb98bc`.
- Release / occupied `v1.0.7-rc.1017001`:
  `61f55216302023d9872aba17546126450e5fbad3`. Do not replace this build or tag.

Detailed evidence is retained in the public repository at the exact continuation
commit, outside this integration's working tree:
[canonical C20 report](https://github.com/Apdelrahman1911/passvault/blob/df30e43c26184c30a9845c80c368d3ad5413f125/docs/audit-continuation/2026-09-08-linux/C20_REMAINING_WORK_SOLO_2026-09-14.md).
Its sibling `ISSUE_LEDGER.json`, `VERIFICATION_LEDGER.json`, and `COVERAGE_DELTA.json`
retain evidence qualifications, failures, source identities and independent reviews.
The same commit retains `docs/audit-handoff/` and its evidence packs/index. No evidence
branch is being deleted. Historical instructions in those records are not execution authority.

The September 15 remote inventory found 12 branches: main/testing/release, two
handoff/continuation branches, and seven Dependabot branches with open PRs176–182.
Dependency upgrades are separate, not part of this consolidation. No branch is
being deleted. Testing/release non-merge commits are patch-equivalent to main
except the combined 1.0.6 promotion `852a765f4970a4bfe0171d3cf5c435052e742701`.
Its full tree equals `5316377df67cff6b46a4b611c71465959efa9820`
(`49b2ddde937e78b50cc9ede42907579e8b8e8fb9`), an ancestor of main. It is not a
missing independent final snapshot. Historical local dirty/HOLD stores were not
reopened for an exhaustive uncommitted-work inventory; they remain preserved.

## Integration policy

Select completed corrections and their permanent regression tests by actual
source diff and dependencies, not merely by branch or closed-issue path lists.
Shared files containing unfinished changes require hunk-level selection. Keep
licenses, schemas/migrations, build configuration and necessary release tooling.
Do not import audit runners, run namespaces, generated artifacts, or intermediate
reports. Preserve unselected work on its original branch.

Prior test results remain scoped to their original source. Source-affinity review
can support reuse of unchanged results, but does not prove the integrated tree.
The integration must receive focused regression checks and an independent diff
review before merge. Main/testing require `Validate Gradle Wrapper`, `Run Tests`,
one approving PR review, resolved conversations and linear history. Never use
an administrator bypass. A coding-agent review is not a GitHub approval.

See [BACKLOG.md](BACKLOG.md) for open work and beta gates. The historical 27/39
qualified family closures are not an integrated-source or readiness percentage.

## This integration's new evidence

The compact verification/review bundle is separately preserved and its remote
commit was read back after push:

[f733441da87c3a3a00c181f832a96644b08a56c9](https://github.com/Apdelrahman1911/passvault/tree/f733441da87c3a3a00c181f832a96644b08a56c9/docs/consolidation-evidence/2026-09-15).

This contains the selected-source manifests, actual logs/XML, the original static
check failure and corrected check, independent reviews and retained-evidence
affinity qualifications. Its inherited application tree is archival, **not** the
selective integration build source. The integrated product source is the commit
containing this document; see VERIFICATION.md for the post-test documentation
additions and unchanged test-method relocation.

See [VERIFICATION.md](VERIFICATION.md) for results and [INTEGRATION.md](INTEGRATION.md)
for the outstanding protected-CI/merge and conditional beta-promotion sequence.
