# Apple selection: Kotlin 2.4.10 source-contract supplement

Reviewer `/root/build_config`. **SOURCE ONLY; NO APPLE EXECUTION OR ADMISSION.**
This supplements, rather than rewrites, `APPLE-TARGET-MATRIX.md` and its exact
eight-declaration inventory. The earlier unknowns remain accurate for that
earlier read. `/root/native` owns the separate fixture-semantic review.
Root remains the sole cross-platform build owner; no workflow is authored here.

## Exact official source, not a resolved plugin or simulator

Root authorized two bounded public reads; both are complete and recorded in
`KOTLIN2410-METADATA.json` and `KOTLIN2410-NATIVE-TEST-SOURCE.json`:

- Maven module metadata: 143988 bytes, SHA-256
  `0e7713d84605da0bee038b578f31795882ccd721220a79cc36736ac9af9cb412`,
  matching the existing dependency-verification entry.
- Its `gradle813SourcesElements` source artifact: 3538728 bytes, SHA-256
  `70e277a44fbcf4d58444bb8b1459cb80686380537aa7414b80745a71607b36bb`,
  matching that metadata. Ten complete selected source files are retained as
  inert text; no archive is retained, extracted or imported.

This establishes the selected published source contracts, **not** actual Gradle
variant resolution, binary/source equivalence, plugin configuration or target
execution. Exact selected-member hashes and this report's input binding are in
`APPLE-KOTLIN2410-INPUTS.json`. No further network read occurred for this report.

## Contracts now narrowed

1. **Device option.** `KotlinNativeTest.kt` lines257–267 marks the *task's*
   `deviceId` property `DeprecationLevel.ERROR`, and declares `device` with
   `@Option(option = "device")`. An explicit task `--device <owned-UDID>` is
   source-supported; do not invent a project property or use deprecated task
   `deviceId`. This differs from the still-supported *test-run interface*
   `deviceId`: `KotlinNativeBinaryTestRun.kt` lines36–42 and73–81 forwards that
   interface property to the task's `device`.
2. **No implicit simulator selection.** The test factory lines92–104 installs
   a lazy device convention, finalized on read. The default provider runs
   `/usr/bin/xcrun simctl list devices available`. It takes the first matching
   UUID in each recognized OS section; a later section for the same family
   can overwrite that family's prior selection. Neither a default, first nor
   preexisting device is audit-owned. Bind a newly created, explicit UDID before
   execution; a convention is not ownership or lifecycle evidence.
3. **Host and executable.** `KotlinNativeTestRunFactories.kt` lines46–88 enables
   simulator tests only on macOS with matching host/target architecture, selects
   the DEBUG test binary, sets `targetName`, and uses the project directory as
   the working directory. For this repository's `iosSimulatorArm64` target,
   Apple Silicon/macOS remains required. An unsupported-host skipped task is not
   a test pass. Execution filtering does not shrink the compilation graph.
4. **Standalone behavior.** The factory sets `standalone` convention to `true`.
   `KotlinNativeTest.kt` lines269–296 constructs `xcrun simctl spawn`, optional
   debugger/standalone flags, the explicit device, executable and `--`, followed
   by test arguments. No standalone CLI option annotation appears in this
   reviewed task. The source comment says standalone delegates automatic
   boot/shutdown to Xcode; this is not proof of actual owned-simulator settlement.
   Do not invent a `--standalone=false` Gradle option or assume a prebooted device
   is necessary/sufficient without a separately reviewed execution choice.
5. **Filter bridge, not runtime filter proof.** `KotlinTest.kt` extends
   `AbstractTestTask`; its included patterns combine configured includes and
   `commandLineIncludePatterns`. `KotlinNativeTest.kt` lines179–205 converts
   nonempty patterns to comma-joined `--ktest_gradle_filter=...`. It adds
   `--ktest_logger=TEAMCITY` and `--ktest_no_exit_code`; test failure is reported
   through service messages, so native process exit0 alone is not success.
   `KotlinTest.kt` line36 explicitly sets `isFailOnNoMatchingTests=false`.
   A zero-case or skipped task therefore cannot satisfy the audit. The native
   runtime's filter implementation was not among the ten selected files.
6. **Descriptor naming.** `KotlinNativeTest.kt` lines146–151 supplies
   `targetName` as suffix and enables suite-name prepending. The service-message
   client lines123–143 prepends the parent suite, parses a class/method using
   the last dot (lines264–284), and forms method display names with
   `[targetName]`. This supports anticipating `[iosSimulatorArm64]` display
   decoration; it does not establish exact emitted JUnit XML names, filenames,
   directory layout or runtime suite strings. Do not silently normalize future
   evidence to fit that anticipation.

## Environment and cancellation qualifications

`KotlinNativeTest.kt` lines40–43 initially imports all environment variables.
More importantly, `TCServiceMessagesTestExecutor.kt` lines93–104 constructs a
`ProcessBuilder` and calls `environment().putAll(taskEnvironment)` without
clearing its inherited process environment. Merely assigning a small task
`environment` map **does not remove inherited credentials or option variables**.
A fresh allowlisted parent Gradle environment is required. Its debug logger
lines108–114 prints the process environment; do not enable debug logging or
collect ambient environment dumps to diagnose this selection.

The executor's `finally` calls `destroyProcessIfNeeded`, which force-destroys its
immediate `Process` object. That source behavior is not proof that CoreSimulator
services, guest children, owned files or the simulator have settled. The root
runner needs separate bounded, positively owned lifecycle and cleanup evidence.
No global simulator shutdown/delete, unrelated process signaling or historical
recovery helper is authorized by the plugin's cleanup code.

Foundation `NSTemporaryDirectory()` in the selected fixtures remains a separate
guest-storage boundary. Neither these plugin sources nor a host `TMPDIR` prove
its effective simulator namespace. Bind synthetic storage and cleanup before
any admitted fixture access; do not read real simulator/user data to discover it.

## Remaining admission and evidence

The conventional task `:shared:iosSimulatorArm64Test` remains source-derived,
not a realized task-graph observation: the `testTaskName` helper implementation
and Gradle XML writer were not captured. Its command-line include bridge is now
source-established; exact native filter/name mapping and nonempty eight-case XML
acceptance still need a reviewed contract. Do not substitute an all-tests run,
task count, compilation or command0 for that evidence.

Keep one useful batch: **PVA-008 seven declarations plus PVA-014 one declaration,
zero current executions**. Preserve the broad shared/main/test compilation,
KSP/schema/cinterop and all-project configuration qualifications in the matrix.
Before any run, root must bind the committed source, exact commands and expected
cases, toolchain/architecture/runtime, one owned simulator, clean environment,
fresh coordination, time/resource limits and independently reviewed cleanup.
Use no signing/app Xcode scheme, release workflow, production inputs or large
binary artifacts. No concurrent audit-owned local/CI build is permitted.

Even successful simulator execution would not establish physical iPhone
picker/lock/background/file-protection security or displayed/authenticated
biometric behavior. Those hardware gaps remain **BLOCKED**. The macOS native
suite's distinct fixture-isolation/cleanup gaps remain separate; do not append
it casually. All STOP, NO-RETRY, CLOSED and build1017001 restrictions survive.

No finding, fix or closure is added here. Denominators stay **19/25 original
confirmed, 22/37 total confirmed, 2/12 original suspicions**; eight PVD decisions
remain separate. This report used local inert text reads/hashes and permanent
report writing only. No build/JVM/native/simulator process or temporary archive
remains from this reviewer; a wrapper stop is not applicable to these reads.
