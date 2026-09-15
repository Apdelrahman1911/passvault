# CI04: Windows shell selection and owner-requested parallel hosted jobs

Owner requested cancellation ofCI03, repair ofWindows failure and parallel jobs.
CI03 failed2of6synthetic guard cases beforeGradle: nativePython selected WSLbash
rather than GitBash. Windowsruntime is not passed by a mock/compilation. Previous
unit/static/dependency, Android and LinuxDesktop successes remain source-bound.
Six prior shell cleanup receipts PASS; cancelledMac cleanupHOLD/private-home
process/stop0/pre-settlement remains HOLD, with hostedVMdisposal only a fallback.
No oldHOLD is recovered, erased or treated as PASS.

Sourcea518a568f0b17a48144c053a43982d42aec125fc,
tree66b346ee40bd62337535969bb969c5a0eb01dbb8, contains six reviewed files.
Bash launcher exports actualBASH (nativecygpath onWindows); nativePython requires
an explicit valid path rather than WindowsPATH fallback. WindowsJob assignment
still precedes shell start, and wrapperstop uses the same corrected dispatch.
All prior per-host cleanup, limits, command/signing/Store restrictions remain.

Owner-authorized fanout: after wrapper/dependency preflight, at most9heavy jobs
run on separate hostedVMs. Desktopmax3/macOSpackagemax2; all-job gate unchanged.
One owned workflow run; no local heavybuild overlaps it. This overrides only the
older serial-hosted-job restriction, not per-runner worker1 or safety requirements.

First local attempt failed Bash parsing beforePython; explicitPGID settled but
empty private dirs remain HOLD under local/private (not archived). Diagnostic-only
correction was reviewed; local02 then passed syntax,8synthetic cases (1.639s) and
existing workflowsecurity validator. All owned groups settled/private dirs removed,
source hashes unchanged; no Gradle/application/platform tests run locally.

NewCI04 run34950325076 started on corrected source. This bundle is not its final
result. Preliminary merge90e319f443df2e80bd90cfc4040e6457ce402616 has the exact
reviewed tree; actualcheckout requires artifactverification. Initial launchmonitor
assertion failure is retained; fresh verifiedmonitor resumed the SAME run, not a
CIretry. Monitor/lock snapshots are not future execution authority.

Main/testing/release/tag/build1017001 unchanged. No branches deleted. Mainmerge
still needs allrequiredchecks+independentGitHubapproval. Mobilebeta gates remain.
This evidence archive inherits an archival application tree: never merge it into
main. Its second parent preserves exact integration commits for later cleanup.
