# Independent Apple Kotlin 2.4.10 source-contract challenge

Reviewer `/root/native`; supplement author `/root/build_config`. **ACCEPTED IN
ITS SOURCE-ONLY SCOPE; NO APPLE EXECUTION OR ADMISSION.** This does not independently
re-accept this reviewer's earlier `APPLE_NEXT_SCOPE.md` as its own verification.
It challenges the new published-plugin-source conclusions and their boundaries.

Exact reviewed supplement: `../build-config/APPLE-KOTLIN2410-SUPPLEMENT.md`,
SHA-256 `845eaff83aca0fe4cfd595047ad92d1e9d14a7576ddf113cb4b94d46b52604ef`.
Author input binding: `../build-config/APPLE-KOTLIN2410-INPUTS.json`, SHA-256
`4716dea4433277bd3f247648ac6754d7140bce868b685d966919f90bea74b924`.
The adjacent independent JSON binds this report and the actually read ranges.

## Evidence and independence limits

The five bound report/transport packets, all ten retained member byte hashes and
all seventeen prior matrix input hashes independently matched on this point
read. Eight selected source members were read completely as inert text; the
service-message client's naming/descriptor regions were read selectively;
`KotlinNativeTarget.kt` was hash-checked only. Full hashes are not full semantic
review credit. This does not expand the historical G12 coverage denominator.

The module digest in the author's transport record matches the checked-in
Kotlin2.4.10 `.module` verification entry. The selected `gradle813SourcesElements`
artifact digest/size in that record matches the source-download receipt.
This reviewer independently recomputed the retained source-text hashes, **not**
the discarded raw HTTP module/archive bodies or their transport. No redownload,
archive extraction/import, plugin resolution or binary/source equivalence check
occurred. The actual Gradle variant/task graph remains unobserved.

## Reachability, guards and counterexamples

- **Device option: supported with the stated distinction.** The simulator task
  has `@Option(option = "device")` on `device`; task `deviceId` is
  ERROR-deprecated. The separate simulator *test-run* interface still declares
  `deviceId` and forwards it to task `device`. Thus `--device <owned-UDID>` is
  source-supported; a guessed `-P` property, deprecated task setter or unreviewed
  configuration ordering is not. Property convention/finalization source is not
  proof that a concrete invocation selected the intended owned simulator.
- **Defaults are not ownership.** The default provider really invokes
  `/usr/bin/xcrun simctl list devices available`. After the first matching UUID
  it clears the current section, but a later OS heading can restore the same
  family and overwrite the prior map entry. "First device overall" would be an
  incorrect summary. An explicit freshly owned UDID, not default selection, is
  necessary for a later admission.
- **Matching macOS architecture remains a real guard.** Factory compatibility
  requires both macOS and matching host/target architecture; it assigns the
  DEBUG test executable and project working directory. Unsupported-host tasks
  are disabled. In addition, `KotlinNativeTest.kt` has `@SkipWhenEmpty` and
  `onlyIf { executableFile.exists() }` (48–62): a missing executable can also
  produce no test execution. Neither task success nor lack of test failure is
  sufficient evidence of eight executed cases.
- **Standalone is a convention, not cleanup.** Factory `standalone=true`
  contributes `--standalone` to `simctl spawn`. No reviewed task annotation adds
  a `--standalone=false` CLI option. The source comment about Xcode automatic
  boot/shutdown and the non-standalone exit149 diagnostic do not prove runtime
  boot, guest-child or simulator settlement.
- **Filter forwarding is established, runtime naming is not.** The configured
  and command-line includes are combined, then passed as comma-joined
  `--ktest_gradle_filter`; `isFailOnNoMatchingTests=false` is explicit. With
  `--ktest_no_exit_code`, assertion outcomes are represented through service
  messages; a native exit0 is not a regression PASS. The native filter
  implementation and exact realized eight-case mapping are outside these
  retained files. No all-tests fallback is accepted.
- **Display decoration is not XML identity.** The client separates parsed
  method name from its `[targetName]` display name in `DefaultTestDescriptor`.
  It prepends a parent suite and splits at the last dot. This supports the
  anticipated display suffix but does not prove which descriptor fields the
  uncaptured Gradle XML writer emits. Do not strip/add suffixes or normalize
  future XML to manufacture the expected inventory.
- **Replacing the task map does not sanitize inheritance.** Native task setup
  imports environment variables; the executor then creates a `ProcessBuilder`
  and adds task values with `environment().putAll(...)`, without clearing its
  inherited environment. Even an explicitly small replacement task map cannot
  remove inherited credentials/options. A fresh allowlisted parent Gradle
  environment is required. Debug logging exposes the process environment, so
  ambient environment dumps/debug logging are not a safe diagnostic substitute.
- **Immediate-process destruction is not complete cleanup.** The executor
  force-destroys its immediate `Process`; that call alone does not prove awaited
  CoreSimulator, guest-child, thread, file or owned-device settlement. Host
  `TMPDIR` also does not establish Foundation `NSTemporaryDirectory()` inside a
  simulator. These remain independent execution/storage/cleanup gates.

## Disposition

No new product finding/fix/closure is established. The conventional shared
simulator task still needs exact source/command/filter/result and owned-runtime
admission; root alone owns that decision and the cross-host build slot. The
bounded batch remains seven PVA-008 plus one PVA-014 source declarations,
**zero current executions**. Genuine iPhone file-protection/picker/lock/background
and displayed/authenticated biometric behavior remain hardware **BLOCKED**.
The separate macOS native fixture-isolation gaps are not silently appended.

Clarification of the supplement's final phrase "eight PVD decisions": the
retained baseline is **eight documented design explanations, with owner
decisions separate**, not eight completed owner decisions. Confirmed closures
remain original **19/25**, all **22/37**; original suspicions **2/12**. No combined
readiness percentage is implied. All STOP/NO-RETRY/CLOSED/build1017001 and
non-publishing fences remain intact.

Operations were bounded inert JSON/source reads, hashes and permanent compact
report writes only. No Gradle/JVM/compiler/test/helper/simulator/CI or network
operation was invoked; no temporary archive, amended source, cache, build
artifact or background worker was created. Wrapper stop is not applicable to
these source-only reads and discharges no historical obligation.
