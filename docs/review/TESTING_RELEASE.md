# Testing release resource boundary

Version 1.0.9 reserves Store build 1017003 for a new testing publication.
Build 1017002 must not be reused: the 1.0.8 GitHub run uploaded Android, but its
iOS archive failed before upload. The owner confirmed no uploads outside GitHub.
Deferred findings in BACKLOG.md remain open; this is not production authorization.

## iOS archive memory correction

[Run 34979337404](https://github.com/Apdelrahman1911/passvault/actions/runs/34979337404)
on source `a929fd97ce9f103c1097027d3065758b9dd7774b` failed at
`:shared:linkReleaseFrameworkIosArm64`: Kotlin/Native release optimization exhausted
the Java heap. The adapter had overridden the project's 4 GiB setting with 2 GiB.
Its wrapper stop succeeded, but failed Xcode settlement remained **HOLD**; subsequent
cleanup refusals were consequences, not the compiler failure. External promotion
and Desktop publication were skipped. That evidence and HOLD remain binding.

The new iOS archive uses a 4 GiB heap and the standard `macos-26-intel` runner,
requiring at least 12 GiB host RAM before signing inputs are created. Xcode 26+,
the iOS Arm64 destination, application identities, worker limits and live resource
floors are unchanged. Other release batches retain their 2 GiB heaps. The extra
host capacity avoids merely shifting heap exhaustion to the 7 GiB Arm64 runner's
RAM floor. This is a configuration fix, **not yet proof of a successful archive**.
The new signed archive/export and Store processing must pass before success is
reported; do not rerun or resume the partially uploaded 1017002 release.

## Apple private-key pipe import

The follow-up main CI [34991312886](https://github.com/Apdelrahman1911/passvault/actions/runs/34991312886)
failed its macOS Intel **synthetic** signing test on source
`f25c6888db881109182ea4aebd0fe17b713a08a4`, before Desktop packaging. The private-key
import returned invalid parameters. The same runner image passed the preceding
PR CI; this failure must not be waived or blindly retried. Its cleanup passed,
with no Gradle wrapper started. Testing promotion was not launched.

Apple's published Security implementation sizes file input with `fstat` before
a single read, including `/dev/stdin`. A live pipe can therefore expose an empty
or partial key rather than the completed stream. The new bounded in-memory
adapter waits for EOF, fills a fresh pipe before launching `security`, and
requires its size to match exactly. No unencrypted key file, password argument,
or relaxed extractability/trusted-tool setting is introduced. Capacity mismatch,
empty/oversized input and timeouts fail closed. The existing native test adds a
controlled incomplete-pipe negative case and delayed/chunked successful import
into a fresh synthetic keychain, with identity and non-extractability checks.
PR CI [34993670798](https://github.com/Apdelrahman1911/passvault/actions/runs/34993670798)
validated the incomplete-pipe negative control, fragmented-input import, identity
and non-extractability checks on both Intel and Apple Silicon. Both packaging
jobs passed; all 15 CI cleanup receipts passed on checkout
`8633157ccc0538b7df3419ccfdff59d8e8bd46a3` (tree
`63347feb043a8175859a91e7ae452d5634eef8cc`). This is synthetic native evidence, not
an actual Store archive/upload. The original failed invocation's exact byte count
is unknown.

That CI still **failed**: the Linux release-automation static policy expected the
old private-key command in the shell importer. The policy now follows the buffered
helper and requires its exact stdin-only, non-extractable, restricted-tool command.
Focused mutations protect password-FD handling, helper routing, non-extractability,
password-argument refusal, trusted-tool restrictions and stdin binding. The failed
run is preserved; the corrected validator and all seven focused static cases passed
on Linux, with immediate fixture cleanup and no build/signing processes started.
An updated PR CI must pass before promotion.

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

1.0.9 local checks: nine test methods passed, now including three real synthetic
process/filesystem batches. The added iOS batch checks the private 4 GiB Gradle
configuration reaches the child process, while Android remains at 2 GiB; worker,
ownership, staging and cleanup contracts are preserved. Both Python files and
the changed workflow parsed, all 36 workflow Bash blocks passed syntax checks,
release metadata validated, and shared/Xcode version values matched. All private
test fixtures and owned processes were cleaned after settlement. These checks
used dummy wrappers, not a real iOS compiler, signing identity or Store upload.

Previous Linux baseline: nine synthetic test methods passed, including two real process/filesystem
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
