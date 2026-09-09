# Current-C4 Test wiring — independent source challenge

Reviewer `/root/storage`; wiring author `/root/build_config`; 2026-09-09.
**ACCEPT for the bounded source proposal, not execution admission or a test pass.**
The separately authored cycle driver still requires another agent's review.

## Frozen inputs

Application C4 `da8ff89b9a8579017d5d524e628dff5251f2bb90`, tree
`cf0a8a702e7cd6948236be18b491bb5a21b5886e`.

| Input | SHA-256 |
| --- | --- |
| `scripts/audit/current_cycle_tests.init.gradle` | `ca9e7dc00989fce31ba67f9c2cb2bf584576405e7e5e3e998c80e13d06403e34` |
| `reviews/build-config/current-cycle01/COMMANDS.md` | `180ac8165ba9162bd21c4b2af0a2252719f18ccb9d7943921fa3980f339c6651` |
| same directory `CLASSES.tsv` | `95e6ed4f8e5cbb92e4e6840a08e16613b14674555f7f99cd24e34b3ddcd18bda` |
| same directory `METHODS.tsv` | `10cb11cd6392e43ab7ec3e94e2f348ccd2caf6cd2127830fe9e1287ea10210be` |
| root's Git-derived `reviews/current-cycle/SOURCE.json` | `2e8b82c91215bbfee9cf4a2a3f09bad386fc8515d7fd20263fbbfa5fe1217003` |

Full init101LF, commands153LF, classes15LF and methods168LF were read. A small
text-only CSV/regex reconciliation found all14 class hashes equal to their C4
manifest and physical source images, all167 literal declarations in the listed
class/order, and D01–D07's original105 names/order unchanged against the frozen
handoff inventory. This is source reconciliation, not Kotlin parsing or execution.
The three classes sharing `RepositorySecurityIntegrationTest.kt` remain distinct
filters; unrelated matcher, metadata, mutation, Unicode, TOTP and lock-failure
classes in that file are not accidentally selected. No omission is relabelled a
pass. Ordinary163 = database114 + credential31 + shared18; cold4 = one producer
and three consumers, hence166 regression methods plus one fixture producer.

## Challenge and compatibility

- The three modules actually declare JVM `desktop` targets at17 and the
  inspected common/desktop source sets. Test filtering restricts execution, not
  compilation or all-project configuration. There are no new task clones,
  dependencies, persistence/identity/version changes or shell/fixture helpers.
- Ordinary excludes P01. Each cold invocation includes exactly one literal P01
  method and forces only its existing Test task out of date, allowing same-cycle
  compilation/cache reuse without `--rerun-tasks`. `failOnNoMatchingTests` and
  exact phase XML checks must reject no-match/skip/up-to-date substitutes.
- Explicit JDK17 executable, one fork/worker and512MiB worker heap are compatible
  with the planned JDK17 client. Worker `systemProperty` supplies startup `-D`
  arguments; client JVM-option environment variables are removed from workers.
  Provider source checks both actual properties and JVM input arguments.
- Seven distinct worker roots are required. Only loader-io's worker Java tmp
  points to its original zero-byte regular `tmp-blocker`; Gradle/compiler,
  HOME/JNA and native TMP/XDG remain healthy. Actual framework startup could
  fail before the intended loader boundary: that is a failed attempt, not the
  expected ResourceLoaderException/IOException product witness.
- Consumers require the317-character syntax; provenance is deliberately not
  claimed by the init. The driver must extract exactly one producer value from
  preserved prepare XML `system-out`, bind it unchanged to all three consumers,
  and archive13 ordinary plus four phase-specific XML files before overwrite.
  Exact suite/test `[desktop]` names remain prospective until observed; no
  suffix normalization or console-only fixture assumption is justified.
- Wrapper `--stop` omits the init/mode, since init intentionally rejects absent
  mode. Each invocation requires its own original environment/stop obligation,
  settlement and unstarted accounting before any next invocation.
- Root explicitly approved initial ordinary ONLINE strict verification; cold4
  are `--offline`, without online fallback. No SDK/JDK autodownload is allowed.
  This is not a general network sandbox. Android task listing is configuration
  evidence only, not an Android32 device case, package or complete task graph.

No blocking wiring source defect was identified. Runtime viability, exact worker
startup/ownership, source identity, five-call sequencing and cleanup admission
remain root/independent-driver-review obligations. No Test, syntax probe, target
helper import, runtime/cache, process or wrapper-stop obligation was created by
this source-only review. All prior STOP/NO-RETRY/CLOSED and hardware gaps remain.
