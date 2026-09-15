# Integration and beta promotion status

## Normal-protection integration gate

The selected source is prepared on `codex/consolidate-completed-20260915`, based
on main `0dbc12c7f1b7770e75963c751c8c67af6e8b057a`. A branch push is not a main
merge, protected-check success, or review approval.

The owner explicitly authorized parallel hosted-CI builds on2026-09-15. After
wrapper and dependency preflight, independent unit/static, Android, Desktop,
macOS package, shared-compile and iOS simulator jobs run on separate hosted VMs.
Desktop permits three matrix jobs and macOS packaging two: at most nine heavy
jobs across independent VMs. This replaces the earlier serial cross-job chain,
not the one-worker limit within each job. No cross-job build output is consumed.
Required check names, read-only permissions and the fail-closed all-job CI Gate
remain unchanged; no failed/skipped job is accepted as a successful check.
Testing promotion remains blocked separately.

`ci-run.py` is permanent hosted-CI resource cleanup, not an archived audit runner.
Each shell batch gets private HOME/TMP/Gradle/Konan storage, JDK17, one worker,
in-process Kotlin, non-daemon execution and configure-on-demand disabled. Native
CMake builds use one worker and CTest has a120-second timeout. The Windows wrapper
is `gradlew.bat`; POSIX uses `gradlew`. Strict dependency verification is unchanged.
The launcher passes its actual Bash executable explicitly (native Git Bash path
onWindows), avoiding accidental selection of System32 WSL Bash. Missing/invalid
Windows shell configuration fails closed; no WSL installation or PATH fallback.
Windows processes enter a no-breakaway JobObject before shell launch; POSIX uses an
owned session and checks the unique Gradle JVM marker for detached workers.

Original-wrapper stop and positive worker settlement precede allowlisted output/
private-cache deletion. Compact reports are snapshotted even on cleanup failure;
only post-settlement reports are final. Symlink/tracked-source guards refuse unsafe
cleanup. Failed/ambiguous cleanup records HOLD and retains private roots, without
stop retries or unrelated process kills. Forced runner loss cannot prove immediate
cleanup: hosted VM disposal is the fallback, never an invented successful stop.
Artifacts retain compact reports/cleanup receipts for three days, not APK/DMG files.
Batch timeout35minutes, job timeout45minutes; wrapper stop60seconds, settlement15seconds.
RAM launch/running floors25%/20%; owner-authorized fallback disk floor3GiB is checked
before/during work. Root coordinates one audit-owned workflow run at a time; no local heavy build
overlaps it. The independent hosted jobs within that run may execute in parallel.

Android CI uses the existing `passvault.versionCode=1` override solely as a
non-publishing validation sentinel, never occupied Store build1017001. It is not
a Store allocation, replacement candidate, installable upgrade or tester artifact;
tracked Store version configuration stays unchanged.
The Android validation certificate and Apple importer fixture are synthetic and
private to the batch; no signing/Store secrets are requested. A hosted native test
or simulator compile is not physical-device security evidence. No stopped audit
scope, PVA029 failing runner, or GUI opt-in has been reopened.

**Merge still requires actual CI success and an independent GitHub approval.**
At2026-09-15 the only listed repository collaborator was the PR-author account;
the owner must provide a qualified independent reviewer with write access. Agent
source review is not a GitHub approval. Never use administrator bypass, force push,
or protection changes. Until normal requirements pass, main remains unmodified.

## Branch retention

Fourteen remote branches were inventoried on2026-09-15. Keep main/testing/release,
the seven open Dependabot PR branches176–182, the active integration, continuation,
and compact-evidence branches. No branch has yet been deleted.

The old handoff branch is a potential post-merge cleanup candidate only: its exact
commit `9bdf9559b7a801a6f2fa49be3d7836ea6ef4d0ed` (tree
`05014e9f635131d5db06701e4013b4b5a746465a`) is an ancestor of retained evidence
commit `f733441da87c3a3a00c181f832a96644b08a56c9`. Retrieval does not require
keeping a redundant branch name: fetch `codex/consolidation-evidence-20260915`,
then inspect the exact historical commit. Before any deletion, recheck exact
remote tips, ancestry, live PR/run references and whether the branch remains needed.
Do not delete the integration branch until its exact commits/evidence are durably
preserved after the protected merge; squash/rebase patch equivalence alone is not
exact commit preservation. No branch-pointer cleanup is meaningful disk reclamation.

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
