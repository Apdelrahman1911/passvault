# PVA-010: smallest native-host Test wiring proposal

**SOURCE ONLY; NOT INTEGRATED OR EXECUTION-ADMITTED.** Root decides whether this
small build-script delta belongs in the fixed publication selection or stays
inert here. No build, graph discovery, test, native load, SDK/host probe, Git or
cleanup was performed. Original real-JNA V1/V2 packets remain sealed.

Sole candidate source change: `app-desktop/build.gradle.kts`.
Before SHA-256: `381ef53b5bc4ae775aa2322dbaea02588d499111d2a32e2ca3cdb2557363acde`.
Afterimage and exact unified patch are adjacent. Reviewer:
`/root/c20_remaining_review`, `../native_lifetime_wiring_review/`.
The already integrated one-case test remains byte-identical at SHA-256
`4a604009911522d2ea952b37c44a3ec19724e146b2eb781016137065bbbdedf6`.
The first patch is retained under `v1/`; independent challenge added only an
explicit non-null JVM runtime-dependency guard and the qualifications below.

## Why this is the small route

- Register only `:app-desktop:auditJnaLifetimeTest`, only on a supported native
  host. Exempt **only its exact name** from the existing all-Test native hook.
  Similar prefixes/suffixes remain ordinary Tests; an external exact duplicate
  collides with the explicit registration rather than replacing `desktopTest`.
- Leave standard `desktopTest`, other Tests, native configure/build/stage/CTest,
  production tasks and all release boundaries unchanged. No task is added to
  `check`, root `test` or another aggregate. Root's existing explicit shared-test
  list still names `:app-desktop:desktopTest`, not the new task.
- Use the typed JVM test compilation's output/classes/runtime FileCollections
  and `compileAllTaskName`, not a mapped `desktopTest` TaskProvider. This avoids
  accidentally importing that producer task and its entire native graph.
  No late `setDependsOn`, generic init/harness, graph listener, broad exclusion,
  dependency/version change or new native/CI lane is introduced.
- Exact class-file include and exact method filter, ordinary JUnit4/nonmatch
  failure, one fork/worker, 512MiB worker heap, one active processor, 120s Test
  task timeout and XML-only reports. Runtime observation is neither up-to-date
  nor build-cache reusable. Existing compilation caching need not be redesigned.
- Nine explicit `-Ppassvault.audit.jnaLifetime.*` values are required before a
  worker can fork. If Test execution is reached, missing admission fails in
  `doFirst`; this is after compilation prerequisites, not a pre-build validator.
  Merely realizing tasks/IDE models with absent values does not require private
  paths. The accepted fixture retains actual target/path/size/hash/ABI checks.
- The two additional inputs map only the Test worker's `user.home`,
  `java.io.tmpdir`, `jna.tmpdir` and working directory. They do **not** prove
  private OS HOME/TMP environment, permissions/ACLs, freshness or ownership;
  original external execution admission still owns all of those facts.

## Exact narrow selection and mandatory remaining admission

The following is a command **template**, not current literal-path authority:

```text
<checked-in wrapper; admitted HotSpot-compatible JDK17> :app-desktop:auditJnaLifetimeTest
  --no-daemon --max-workers=1 --no-parallel --no-configure-on-demand
  --no-configuration-cache --no-build-cache --dependency-verification=strict
  -Pkotlin.compiler.execution.strategy=in-process
  -Ppassvault.audit.jnaLifetime.enabled=1
  -Ppassvault.audit.jnaLifetime.target=<one admitted windows-x64/macos-x64/macos-arm64>
  -Ppassvault.audit.jnaLifetime.abi=1
  -Ppassvault.audit.jnaLifetime.library=<exact original ordinary DLL/dylib>
  -Ppassvault.audit.jnaLifetime.libraryBytes=<exact decimal size>
  -Ppassvault.audit.jnaLifetime.librarySha256=<exact lowercase SHA-256>
  -Ppassvault.audit.jnaLifetime.dataDirectory=<original empty private synthetic data>
  -Ppassvault.audit.jnaLifetime.workerHome=<original private worker HOME>
  -Ppassvault.audit.jnaLifetime.workerTemporaryDirectory=<original private worker TMP>
```

Do not add `desktopTest`, `--tests` broadening, `-x`, a package task or native
aggregate. Reuse an admitted matching ordinary library; if unavailable, separately
admit a fresh Release native configuration with `BUILD_TESTING=OFF` and exact
target architecture/toolchain, then only `--target passvault_biometric --parallel 1`.
No CTest, staging, control/instrumentation target or production artifact is needed.

**Still mandatory before any invocation:** bind actual commit/tree, current
Gradle/Kotlin configuration, literal commands/paths/library/toolchain/architecture,
private original environment and cleanup envelope; independently inspect the
actual native-host resolved graph. Direct compilation FileCollections avoid a
known producer edge but do not prove all implicit `builtBy`/plugin edges from DSL
text. Refuse a graph containing another Test, any native configure/build/stage/
CTest task, packaging, release action or unrelated execution. This patch is not
an admission bypass or protection against arbitrary build-script mutation.
Kotlin DSL compilation and aggregate non-inclusion remain unexecuted qualifications.
The selected JVM flags require an admitted HotSpot-compatible JDK17, not every
JDK17 implementation. Actual task-specific output paths are also root-bound.

The expected Test output is one non-skipped passing method in the default path
`app-desktop/build/test-results/auditJnaLifetimeTest/TEST-com.passvault.desktop.security.biometric.JnaDesktopBiometricNativeLifetimeIntegrationTest.xml`,
plus the accepted fixture's PASS and cleanup markers. Preserve compact XML/logs/
hashes before original-wrapper `--stop`, owned-worker settlement and allowlisted
output removal (or its explicitly admitted relocated report directory). A
graph/source check, task count/success, NO-SOURCE, filter result, skipped case,
marker alone or timeout is not this evidence; no automatic retry. Native compilation needs its
own resource/time bound; the120s limit applies only to this Test task.

Current VPS resource floor still blocks execution. Even a future case pass
would prove only the managed-pre-entry/real-FFI lifetime boundary, not active
native/provider/sanitizer/packaged/signature/hardware behavior or PVA-010 closure.
All STOP/NO-RETRY/CLOSED scopes and original native refusal remain unchanged.
