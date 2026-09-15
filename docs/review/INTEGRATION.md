# Integration and beta promotion status

## Normal-protection integration gate

The selected source is prepared on `codex/consolidate-completed-20260915`, based
on main `0dbc12c7f1b7770e75963c751c8c67af6e8b057a`. A branch push is not a main
merge, protected-check success, or review approval.

Do not open the main PR until its automatic workflow is admitted. Current
`.github/workflows/ci.yml` starts seven heavy workstreams after wrapper validation,
including a three-host Desktop matrix, two macOS package jobs, Android validation
signing/builds, full tests/security analysis, and shared/iOS compilation. This
violates the campaign's one-heavy-job rule if triggered unchanged. A new branch
push does not match its protected-branch push triggers.

The remaining CI preparation is concrete:

1. Preserve `Validate Gradle Wrapper` and `Run Tests`, actual coverage and strict
   checks. Never spoof a check or treat a skipped required gate as evidence.
2. Serialize heavy jobs with a `needs` chain and each matrix with
   `max-parallel: 1`. Shared per-job concurrency is not a safe replacement: it can
   cancel pending jobs. Coordinate the whole run with the local build owner.
3. Review each native/package scope against existing CLOSED/HOLD restrictions;
   current Linux admission does not admit Windows/macOS/Android executions.
4. Install per-platform original-wrapper stop and owned-worker/output cleanup
   before each invocation; keep compact XML/logs, not unnecessary APK/package
   uploads. Use short artifact retention, JDK17, one worker, non-daemon and
   configure-on-demand disabled; retain strict dependency verification.
5. Bind the admitted commands and runner/toolchain to the integration SHA, then
   open the PR and obtain actual required checks and a qualified GitHub approval.
   No administrator bypass, force push, or protection change is authorized.

The workflow has deliberately not been weakened or partially rewritten as an
unfinished change in this product integration. Until safe CI and independent
GitHub approval exist, **main remains unmodified**. This is an outstanding
engineering/review gate, not a completed merge.

## Conditional mobile beta gate

See BACKLOG.md for mandatory Android/iOS security, compatibility, data-loss,
provenance and supported upgrade evidence. Those gates remain open even where a
patch is excluded from integration. Do not promote testing merely to trigger an
upload while they are open.

After applicable gates pass: promote the verified main source through normal
protected review, query live Store identity/build allocations, allocate a valid
new number, use established signing/upload workflows, and verify processing and
tester availability. The existing build1017001 and its tag remain untouched.

Repository policy describes an internal Play candidate followed by closed
`alpha` promotion. Confirm the owner's intended final testing destination before
upload if it remains ambiguous. No Store query, build-number allocation, signing,
upload, production publication, or tester-state mutation is claimed here.
