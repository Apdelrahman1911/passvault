# Current-cycle01 — exact selected Test wiring and commands

Author `/root/build_config`; root executor; independent wiring reviewer
`/root/storage`. **SOURCE PROPOSAL ONLY: no Gradle/import/compile/test/admission.**

Application/test source: C4 `da8ff89b9a8579017d5d524e628dff5251f2bb90`, tree
`cf0a8a702e7cd6948236be18b491bb5a21b5886e`. The new init is a separately bound
input, not retrospectively part of C4. `CLASSES.tsv` binds14 exact classes to12
source-blob hashes; `METHODS.tsv` lists167 literal source declarations by call.
These were extracted as text from the C4 Git objects, not by importing tests.

Root explicitly chooses **all original105 +49 editor +13 PVA-038 =167**:
ordinary163 (database114, credential31, shared18), then1 producer +3 cold
consumers. This is166 regression methods plus1 fixture producer, not167 passes.
All original105 names/classes are preserved as D01–D07 at current C4 source.
The immutable handoff105 runner/admission stays preserved and UNEXECUTED,
superseded for scheduling only, not rebound, replayed or silently dropped.

## One compilation cycle, five serial invocations

The following Bash notation defines exact prospective argv, **not a runnable
admission/cleanup helper**. Root first binds pristine C4 checkout, this init,
JDK17/checked-in wrapper, clean environment, fresh original roots, one execution
slot, bounds and installed cleanup. No init is supplied to wrapper `--stop`.

```bash
R=/root/projects/PassVault/audit-runtime-linux-current-cycle01
WRAPPER="$R/checkout/gradlew"
INIT=/root/projects/PassVault/passvault-linux/scripts/audit/current_cycle_tests.init.gradle
SAFETY=(
  --no-daemon --max-workers=1 --console=plain --no-parallel
  --no-configure-on-demand --no-configuration-cache --no-build-cache
  --dependency-verification=strict --stacktrace
  -Pkotlin.compiler.execution.strategy=in-process
  -Pandroid.builder.sdkDownload=false
  '-Dorg.gradle.jvmargs=-Xmx2g -Dfile.encoding=UTF-8'
  -Dorg.gradle.java.installations.auto-download=false
  -Dorg.gradle.java.installations.auto-detect=false
  -Dorg.gradle.java.installations.paths=/usr/lib/jvm/java-17-openjdk-amd64
)

# ordinary: fixture environment variable absent; 163 methods /13 XML suites.
"$WRAPPER" --init-script "$INIT" -Ppassvault.audit.mode=ordinary \
  :core:crypto:tasks --all \
  :core:database:desktopTest :feature:credential:desktopTest :shared:desktopTest \
  "${SAFETY[@]}"

# prepare: fixture variable absent; preserve exactly one producer result/output.
"$WRAPPER" --init-script "$INIT" -Ppassvault.audit.mode=prepare \
  :core:database:desktopTest --offline "${SAFETY[@]}"

# FIXTURE is the one validated317-character value from prepare's XML system-out,
# without the literal PVA038_FIXTURE= prefix; never a guessed/stale fixture.
PASSVAULT_PVA038_FIXTURE="$FIXTURE" "$WRAPPER" --init-script "$INIT" \
  -Ppassvault.audit.mode=success :core:database:desktopTest --offline "${SAFETY[@]}"
PASSVAULT_PVA038_FIXTURE="$FIXTURE" "$WRAPPER" --init-script "$INIT" \
  -Ppassvault.audit.mode=wrong-key :core:database:desktopTest --offline "${SAFETY[@]}"
PASSVAULT_PVA038_FIXTURE="$FIXTURE" "$WRAPPER" --init-script "$INIT" \
  -Ppassvault.audit.mode=loader-io :core:database:desktopTest --offline "${SAFETY[@]}"

# After EACH invocation, under the SAME bound environment, before next work:
"$WRAPPER" --stop "${SAFETY[@]}"
```

Working directory for every command is `$R/checkout`. Root supplies JDK17
`JAVA_HOME`/PATH and private `$R/gradle-home` as `GRADLE_USER_HOME`; client
HOME/TMP/Java/JNA/XDG/Konan/Android-user locations remain separate healthy roots
under R. No ambient signing/store/JVM-option/private configuration is admitted.
The init strips client JVM-option variables from workers and sets worker-only
startup properties; it never changes the client's temporary directory.

**Network policy explicitly proposed, not silently changed:** ordinary permits
strict verified Gradle resolution (no `--offline`) in the fresh private cache;
four cold calls use `--offline` and the retained same-cycle cache/distribution.
Root must admit that initial resolution policy. This explicitly replaces the
standalone Android listing's offline-only proposal for this combined command.
No automatic online retry if cold resolution is missing. `--offline` itself is
not a wrapper/plugin network sandbox. SDK/JDK automatic-download prohibitions,
all-project configuration qualifications and strict verification remain.

No root `test`/`check`, app/package/device/release task or Detekt is selected.
Android `:core:crypto:tasks --all` shares the initial configuration/log/cleanup;
its output is task-name evidence, not a complete dependency graph or Android32
case. Do not auto-chain any listed package/device task. Configuration/plugin
failure before tests is a failed attempt, not the expected product witness.

## Exact worker roots and selection

The init configures only existing `desktopTest` tasks in the three modules.
Ordinary whole-class filters are exactly D01–D08/C01–C03/S01–S02. **P01 is
excluded**, so its four assumption-gated methods cannot masquerade as ordinary
passes/skips. In each cold call only the matching literal P01 method is filtered;
see METHODS.tsv. No custom Test tasks or subprocess/fixture framework is added.

Each selected task has `maxParallelForks=1`, `forkEvery=0`,512MiB maximum heap,
explicit JDK17 executable and startup `-XX:-UsePerfData`, UTF-8, `user.home`,
`jna.tmpdir`, `java.io.tmpdir`. Cold database Test output is never up-to-date or
build-cache eligible; **only that Test task is forced**, not compilation via
`--rerun-tasks`. Compilation/default report locations stay unchanged. Global
one-worker/nonparallel flags remain required; heap/fork settings do not bound
native threads/RSS. Actual worker Java/arguments still need observed evidence.

| Call/task | Worker root, relative to R |
| --- | --- |
| ordinary/database | `workers/database` |
| ordinary/credential | `workers/credential` |
| ordinary/shared | `workers/shared` |
| prepare | `workers/pva038-prepare` |
| success | `workers/pva038-success` |
| wrong-key | `workers/pva038-wrong-key` |
| loader-io | `workers/pva038-loader-io` |

Root must separately create/admit each original0700 worker root and empty
`home`, `jna`, `tmp`, `sqlite`, `xdg-cache`, `xdg-config`, `xdg-data`, `xdg-state`
children. Loader-io additionally needs its owned zero-byte regular `tmp-blocker`;
**only its worker Java `java.io.tmpdir` points to that file**. HOME/JNA and native
TMP/XDG stay valid. The init creates/probes/removes none of these paths.
Cold workers receive matching `PASSVAULT_PVA038_MODE` and
`PASSVAULT_PVA038_RUN_ROOT`; only consumers receive the same syntax-validated
317-character fixture. Syntax validation does not prove producer provenance.

## Evidence and cleanup boundaries — no automatic chain/retry

Preserve each call's exact command/exit, compact stdout/stderr and XML under a
distinct evidence label **before the next call**: `ordinary`, `prepare`,
`success`, `wrong-key`, `loader-io`. Ordinary has13 expected XML files listed in
CLASSES.tsv; each provider call has just P01's file at the same database path.
Thus17 expected per-call XML captures, not17 tests. Later Test tasks can replace
the database result directory; do not postpone copying until all calls finish.

Required XML mapping: testcase classname=exact FQCN; suite name=simple class
name + `[desktop]`; testcase name=literal METHODS.tsv method + `[desktop]`.
This preserves the frozen105 mapping policy; unobserved names remain prospective,
not demonstrated runtime output. Require each selected method exactly once,
zero failures/errors/skips, no unexpected suites and actual Test execution.
No suffix stripping, missing-result adoption or exit0-only pass. Preserve the
producer's single public fixture value/hash and bind all three consumers to it.
Cold workers already assert fresh provider/JDK/startup args; loader-io must
reach its actual loader/IOException stack witness, not fail during framework
startup. A cold result is JVM provider/service evidence, not OS biometrics.

After each invocation preserve evidence, use its original wrapper `--stop`,
settle only owned workers and reclaim no-longer-needed allowlisted temporary
outputs. Keep this cycle's compiled outputs/private dependency cache only for
the imminent next call; do not clean/recompile between modes. At final settlement
remove only validated allowlisted generated/cache/temp outputs; never tracked
Room schemas/source/tests/reports, shared caches or toolchains. Root defines
time/disk/RAM/log/XML bounds, continuous monitoring and failure/cancellation
cleanup before launch. No execution/admission or cleanup is supplied here.

All STOP/NO-RETRY/CLOSED, PVA-029/Windows failure and physical-device gaps remain.
No init syntax/Gradle realization, runtime, resource/path probe or build occurred
while authoring these permanent compact files; wrapper stop is not applicable.
