# CI05 narrow macOS settlement-probe repair

CI04 actual checkout90e319f443df2e80bd90cfc4040e6457ce402616 fails the macOS
synthetic termination test after TERM when killpg(pid,0) reports EPERM. It does
not prove a Gradle/product defect. Kernel reason remains unproven. ReturnFalse
(not-proven-empty) on this probe; only ESRCH permits empty=True. All nonzero
signal errors still propagate; persistent uncertainty keeps cleanupHOLD.
No platform permission escalation, unrelated process kill or oldHOLD recovery.
Two POSIX mocked regression methods supplement eight existing synthetic methods.
They prove predicate/poll behavior, not actualmacOS settlement. Windows will skip
those two POSIX methods and still must pass all eight existing methods.

Root remains sole job/CI owner. CI04 remains running to collect useful platform
outcomes. No local heavy job overlaps. Fresh narrow local admission requested:
AST/Bash syntax plus actual launcher→only the two new mocked POSIX methods under a new private
HOME/TMP, timeout120seconds, explicit ownedsession/PGID, scrubbed environment,
PYTHONDONTWRITEBYTECODE=1, disk>=3GiB/RAM>=25%. Controller installs try/finally
before spawn, preserves logs, reaps direct child, signals only newlyowned PGID on
timeout, and confirms no group before deleting only this new private root.
Any cleanup ambiguity retains private root/HOLD. No Gradle/provider/app/emulator,
SDK/shared cache deletion or network test; wrapperstop=not-started. The two new methods launch no nested process sessions; existing real-process
methods run only in the next hosted CI. This short
synthetic validation consumes no hosted build slot and no local heavy resources.

After independent review and local evidence, batch any actual newlydiagnosed CI04
failures before one correctedsource PR push. Wait for CI04completion/monitorlock
release before next hosted run. Same branchrestricted PR workflow, currentpins,
permissionsreadonly, signing/Storesecretsunavailable, atmost9independentVMjobs,
worker1/host, JDK17/strictverification, batch35min/job45min/monitor3h, original
wrapperstop and verified ownedsettlement before allowlisted cleanup. Do not rerun
unchangedCI merely for reassurance; shared cleanupchange affects POSIXjob safety.
Record exact commit/tree/commands through existing ci.yml before push. Preserve
compact logs/XML/cleanup with3day retention, no application binaries. Main requires
allrequiredchecks and actualindependentGitHubapproval; no adminbypass. All historical
STOP/NO-RETRY/CLOSED/HOLD and build1017001 restrictions stay binding.

## Owner-directed immediate supersession and Windows cleanup (09:14UTC)
Owner now explicitly requires fixing observed failures then cancelling the failed
workflow immediately, not waiting for unrelated jobs. Once reviewed corrections
are sealed, cancel CI04, capture final cancellation/cleanup receipts, verify run
settled/oldmonitor released, then push one correctedsource PR synchronization.
WindowsCI04 actually passed8guardmethods, GradleDesktop129tasks and2nativeCTest,
then failed cleanup after originalwrapperstop0 because ownedJobObjects remained
nonempty. Residualprocess identity is unknown; do not invent MSBuild attribution.
After build+originalwrapperstop returned, terminate only nonempty completed-shell
no-breakaway WindowsJobs, once. Record requested indices; require existing
ActiveProcesses0 check before deletion. TERMfailure remainsHOLD without dispatch
retry; POSIXsignal behavior unchanged except reviewed EPERMpredicate. A single
additional mocked regression covers owned/empty/POSIX/active/failing dispatch;
only that new mockedmethod needs a fresh local check with the same narrow
privateHOME/TMP/outerPGID handling (no realWindowsJob or nestedprocess).

Sealed corrected source c94826d1b29493404c8f67744bfca6436ecec829, tree b58227c0ad1458b7dd52623d63899ee7d1a19c6c.
Existing ci.yml commands/pins/parallel limits unchanged. Full guard method count
is11: POSIX11execute; Windows9execute+2POSIXskips. Localchecks executed only
3mockmethods in two narrow batches; no realWindows/macOS behavior claimed.
