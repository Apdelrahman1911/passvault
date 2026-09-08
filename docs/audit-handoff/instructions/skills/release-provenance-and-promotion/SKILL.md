---
name: release-provenance-and-promotion
description: Design, audit, implement, or repair CI release pipelines that must bind reviewed source, versions, artifacts, signatures, store build IDs, promotions, tags, and publication. Use before candidate or production releases, after branch-protection changes, when protected rebases break ancestry checks, when a workflow rebuilds during promotion, or when partial uploads and reruns must remain safe.
---

# Release Provenance and Promotion

Make one immutable candidate the authority from review through publication.

## Non-negotiable invariants

1. Bind every candidate to a full commit SHA, Git tree, version, build number, and artifact hashes.
2. Build each mobile binary once. Promote recorded store build IDs; never rebuild for a later track.
3. Freeze signed Desktop artifacts before publication. Stable publication consumes the frozen bundle.
4. Treat versions, tags, commit messages, and partial diffs as labels, not provenance.
5. Require protected approvals at authority-changing boundaries.
6. Make reruns target the same candidate or allocate a new candidate explicitly; never silently mix them.
7. Fail closed when receipts, source, signatures, environment scope, or artifact retention disagree.

## Workflow

### 1. Inventory reality

Inspect, rather than trust documentation:

- release workflows, reusable workflows, triggers, permissions, concurrency, environments, and cleanup;
- branch protection, merge mode, force-push policy, and actual Git graph;
- every version/build-number source and uniqueness rule;
- where each artifact is built, signed, uploaded, promoted, downloaded, or rebuilt;
- candidate manifests, receipts, attestations, checksums, tags, and retention;
- manual approvals and external review state.

Draw the actual flow and mark every point that creates a new binary or changes external state.

### 2. Define the candidate contract

Use one manifest containing at least:

```text
schema version
source commit + Git tree
marketing version + unique build number
platform artifact name + SHA-256 + size
signing identity/fingerprint where applicable
store application identity + uploaded build identifier
workflow run/attempt + creation time
```

Canonicalize and validate the manifest before signing it or deriving receipts. Do not include secrets, private tester data, IPA/AAB binaries in public releases, or mutable URLs as authority.

### 3. Handle protected Git topology

Accept a candidate only when either:

- the approved source commit is an ancestor of the candidate; or
- both commits share repository history and their complete Git trees are equal.

Reject unrelated histories even if a copied tree happens to match. Run `scripts/verify_candidate_source.sh` with full SHAs. Record the accepted candidate SHA; subsequent artifacts bind to it, not the pre-rebase SHA.

### 4. Separate build, promotion, and publication

- Candidate: compile, sign if required for testing, upload once, record receipts, build test Desktop packages.
- Promotion: use store APIs to move the recorded build; do not compile.
- Production signing: sign/notarize the exact release source and freeze attested Desktop output in validation-only mode.
- Publication: verify live store build IDs and publish the exact frozen Desktop bundle.

Use one non-cancelling concurrency group for operations that inspect or mutate the same release lifecycle.

### 5. Design failure and rerun behavior

Assume later stages can fail after earlier uploads. On rerun:

- discover and validate already-created external builds;
- resume only when their identity and hashes match the candidate;
- allocate a new candidate when source or binary bytes changed;
- delete transient unsigned/signing-request artifacts on success and failure;
- never move an existing immutable tag or replace release assets silently.

### 6. Preserve authorization

Keep read-only validation separate from uploads, signing, promotion, review submission, and publication. Do not use credentials while planning. Require explicit authority for protected merges and every production mutation.

## Required verification

Create adversarial tests for:

- direct ancestry, protected exact-tree rebase, divergent tree, and unrelated matching tree;
- truncated/tampered manifests, wrong SHA, size, version, signing fingerprint, and store build ID;
- duplicate build numbers and unsafe workflow reruns;
- partial mobile upload followed by Desktop/signing failure;
- expired/missing artifacts and wrong candidate tags;
- environment/branch mismatch and overlapping release runs;
- stable publication attempting to rebuild.

Run workflow syntax/action-pin checks and a no-publication rehearsal. Record exact commits, runs, artifacts, and manual gates in the final report.

## Forbidden patterns

- Proving provenance with a version string, branch name, tag, or commit message.
- Rebuilding after internal testing and calling the result “the same release.”
- Accepting arbitrary same-tree commits without shared history.
- Using an unbounded build-number retry scheme.
- Publishing unsigned production fallbacks after signing failure.
- Treating successful upload as review approval or public availability.

## Resources

- Run [`scripts/verify_candidate_source.sh`](scripts/verify_candidate_source.sh) for protected-rebase source checks.
- Read [`references/pipeline-contract.md`](references/pipeline-contract.md) when designing manifests, receipts, environments, cleanup, or reruns.

