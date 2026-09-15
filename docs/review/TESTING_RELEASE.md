# Testing release resource boundary

Version 1.0.8 uses Store build 1017002, confirmed from the last GitHub publication
(1.0.7/1017001) and the owner's statement that there were no external uploads.
Deferred findings in BACKLOG.md remain open; this is not production authorization.

The release build adapter reuses the current CI process-scope implementation,
not archived audit runners. It preserves signing HOME on iOS, uses private build
caches and DerivedData, stops the original wrapper, and requires owned settlement
before moving selected deliverables or deleting disposable outputs. Required
artifacts remain until successful durable retention. Failure/HOLD is not cleanup
success and must not be automatically retried. A nested CLEANUP_HOLD is retained.

Packaged Desktop smoke checks use a fresh synthetic HOME and owned process scope.
Xcode failure/cancellation retains HOLD: normal subprocess completion checks do
not establish cancellation settlement of all XPC-launched build services. Failed
keychain restoration/deletion likewise blocks cleanup. Hosted runner loss is an
uncertified cleanup interruption, not a passed receipt.

## Focused verification

Linux: nine synthetic test methods passed, including two real process/filesystem
adapter batches using a **dummy**, not Gradle, wrapper. These protect byte
preservation, selected artifact retention, occupied/missing/symlink refusals,
wrapper-stop/settlement ordering, initial-report-copy failure, and
retention-before-disposal. Two Python ASTs, three workflow YAML files and fourteen
guarded Bash blocks passed syntax checks. Temporary fixtures were removed only
after settlement; no real build daemon, app, Store or hardware was exercised.

The tests are included in the existing hosted CI resource-guard jobs, without
adding another platform matrix. They do not prove real signing, packaged app
startup, Store processing or Xcode cancellation. Before enabling publication,
review actual hosted results and record fresh release admission against the final
source. Preserve every failure and hardware limitation rather than closing them
through owner deferral or source review.
