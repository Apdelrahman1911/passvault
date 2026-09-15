## Selective completed-fix integration

Consolidates reviewed, coherent fixes into main without importing the audit history
or unfinished native/editor/Android changes. Original source selection:
`beb131add14a53ad13d9b7ebb30a5ab4738adec2`.

- Completed password scoring, Room/storage/recovery/backup and Unicode fixes;
  sensitive editor ownership, Desktop ownership/Compose-loop cleanup; permanent regressions.
- Completed release-automation guards and fixtures. No application identities,
  dependency versions, version/build numbers, protected tags or occupied1017001 change.
- Durable maintenance/status/backlog under `docs/review/`; exact historical evidence
  retained on `codex/consolidation-evidence-20260915` at
  `f733441da87c3a3a00c181f832a96644b08a56c9` and the continuation branch.
- Preserve normal CI coverage/check names; serialize builds and matrix entries,
  install hosted per-batch owned-process/output cleanup, retain compact reports.
  Synthetic validation signing only; no Store or production signing credentials.

## Verification

Selection:107 synthetic release regression cases and184 focused JUnit cases passed.
Initial Desktop Detekt class-size failure was repaired by relocating an unchanged
method; targeted corrected Detekt passed. Exact source-affinity and limitations
are in `docs/review/VERIFICATION.md`. These are not a substitute for this PR's CI.

Fresh six-case resource-guard tests passed. Source-aware full shell validation
exposed stale documentation/CI/secret-deletion assertions; original failures are
retained. After correcting those assertions, the remaining379-line source-validation
suffix passed. This is composite local evidence, not a fresh whole-suite pass;
normal CI executes the complete corrected suite. One earlier instantaneous
settlement observation remains UNKNOWN/HOLD with empty private directories preserved. Independent coding-agent reviews completed; these are not GitHub
approval. Main requires actual `Validate Gradle Wrapper`, `Run Tests` and an
independent approving GitHub review. Wait for the entire fail-closed CI Gate too;
never merge while remaining jobs are running. No admin bypass or force push.

## Explicitly deferred / NOT beta-ready

See `docs/review/BACKLOG.md` for all12 open confirmed families, suspicions, owner
choices, physical Android/iPhone requirements, provenance and mobile data-loss/
compatibility gates. C20 expansion is paused; STOP/HOLD/NO-RETRY/CLOSED scopes remain
binding. No testing promotion, build allocation, upload, release or Store approval
is included in this PR. A protected main merge alone cannot authorize an unsafe beta.

## Branch cleanup

No branches deleted yet. Keep open dependency PRs and continuation/evidence refs.
The old handoff branch is eligible for a separate exact-tip/ancestry/retrieval check
after merge, not blind deletion. Integration exact commits must stay retrievable.

Proposed source: `f8f3c9c0bd61af79e41a7a8e5efe6c0a3167ff15`, tree `312791149ba33ebd9d67aaf67819ab075e1f0b84`.
