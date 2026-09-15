# Correct fail-open validation guard; no product audit expansion

CI02 shell exit0 concealed missingrg in the pilot production-path check. Root
independently confirmed Bash conditional errexit suppression; reviewer read all
three actual workflow inputs and found neither forbidden token. This is a test
coverage defect, not evidence of a production path in the current application.

Repair two permanent test files: use an existing required Ruby standalone
File.read guard, retaining token semantics and failing closed on missing input/tool.
Five synthetic cases exercise the actual marked shell block: permitted source
without workingrg; each forbidden token; missing workflow; missing Ruby. Required
Ruby already appears throughout this script; no dependency/version change.

After CI02 settles and active jobs=0, root acquires the same validated lock and
runs only existing helper-prefix plus exact new five-case tail using the actual
source filename. No earlier107cases/local Gradle/full-suite/platform rerun locally.
Prefix ends before defmobile_receipt; tail begins at the new guard-test comment.
Validate syntax of the two files as part of the same bounded batch.

Fresh source hashes before/after, synthetic private HOME/TMP, scrubbed environment,
owned process group and explicit PGID recorded at launch, timeout120seconds,
SIGTERM then boundedSIGKILL only to that owned group if needed; confirm settlement
before removing that new private directory. Preserve logs/result. No Gradle starts,
so wrapperstop is not applicable. No old runner/controller/HOLD replay.
Root sole test owner. Available disk floor3GiB, RAM25% before launch.

Any CI02 cancellation is limited to our own superseded run34943419307, with
admitted original-wrapper stop/owned cleanup retained; collect actual receipts,
never assume cancellation proved settlement. Forced hosted cleanup may end via
VM disposal, not PASS. Preserve all completed unit/static/dependency results.
After independently reviewed fix and narrow local regression, seal exact commit/
tree before dedicated-branch push and fresh corrected-source normal PR checks.
No unchanged automatic retry. No protected merge until required CI and actual
independent GitHub approval. All beta/STOP/NO-RETRY/HOLD restrictions unchanged.

Sealed commit `a3132bbe7a4e4c4f204461a08ce850e7bac9a132`, tree `05d9386c5079451543181737ae67eb3470e7735c`; three reviewed files in
CI03-SOURCE-IDENTITY.json. This supersedes CI01/CI02 source bindings only.
Commands, one-job serial graph, JDK17/worker1, limits/cleanup and all restrictions
remain CI-PLAN.md and unchanged .github/workflows/ci.yml. Local five cases and
two syntax checks passed; original107not rerun. CI02 completedcancelled with
actual Android cleanupPASS/stop0, and no active job may overlap the new push.
