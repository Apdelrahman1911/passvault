# Current dependency inventory — narrow C20 execution proposal

Author `/root/c20_dependency_author`, 2026-09-12. **INERT / UNADMITTED.**
Own leaf only; root remains sole executor, Git owner and cleanup owner.
No resolution, Gradle invocation, task graph observation, helper import, runtime
probe, network request, test or cleanup occurred in preparing this proposal.

## Only the missing risk

The accepted F02 records already establish historical online output/current
expected-lock equality and current fixed-payload map/metadata consistency.
Do not repeat their data checks, invoke Ruby merely to restate them, or search
for the unavailable G3 raw manifest. None proves current production resolution.
This proposal fills only that missing **current normalized production coordinate
inventory**, with the existing three producer tasks and no source/dependency edit.

References relative to the continuation root:

- `reviews/team20/session_investigation_author/c20/dependency/F02-REUSE.md`
  and `F02-EVIDENCE-TUPLES.json`; accepted independently in the corresponding
  `session_investigation_review/c20/dependency/REVIEW.md`.
- The same author leaf's `F02-CURRENT-DATA-BOUNDARY.md` and
  `F02-CURRENT-DATA-PREDICATES.json`; accepted in the review leaf's
  `current-data/REVIEW.md`. The 32 fixed-data pins are not a current graph pass.

## Source graph and guards

Root `build.gradle.kts:416-452` registers the three
`GenerateThirdPartyDependencyScopeTask` instances after project evaluation.
The task class at lines24-73 keeps its original single `@TaskAction`, original
`ModuleComponentIdentifier`-filtered `artifactView(...).artifacts.resolvedArtifacts`
provider, distinct/sorted normalization and output destination. The provider is
not replaced by `resolutionResult`, an expected lock, another dependency list,
or a configuration with easier platform requirements.

| Exact requested task | Existing configuration | Original report path |
|---|---|---|
| `:app-android:generateAndroidThirdPartyDependencyInventory` | `releaseRuntimeClasspath` | `app-android/build/reports/legal/android-production-dependencies.lock` |
| `:app-desktop:generateDesktopThirdPartyDependencyInventory` | `desktopRuntimeClasspath` | `app-desktop/build/reports/legal/desktop-production-dependencies.lock` |
| `:shared:generateIosArm64ThirdPartyDependencyInventory` | `iosArm64CompileKlibraries` | `shared/build/reports/legal/iosArm64-production-dependencies.lock` |

No explicit producer dependency/finalizer/order edge is present in the reviewed
source. The expected graph is exactly **three producer nodes and zero edges**,
not a previously observed graph. The inert init script checks that graph before
task actions, exact declared task ancestry, one original action, enabled state,
scope, resolvable named configuration and original single output path. It adds
only graph/start/finish log markers, not a task or action. Unexpected compiler,
Test, JavaExec, Exec, signing, package, native, merge or verification nodes refuse
the entire graph before their task actions. No broad task-name exemption exists.

`checkThirdPartyAttribution`, `generateThirdPartyDependencyInventory`,
`verifyDependencies`, `verifyReleaseVersion`, `dependencyReport`, `check`, tests
and Detekt are **not requested**. No validator is stripped from an aggregate.
One worker, `--no-parallel`, `--continue` let independent producer task failures
retain other useful outcomes when Gradle itself can continue; configuration or
graph failure may still prevent every producer. No automatic retry/fallback.

Source counterchecks: Desktop package hooks match package/distributable tasks;
Test/JavaExec hooks do not match these producers, and Linux has no supported
native biometric build platform in that script. Android release signing gates
match archive tasks, not inventory tasks. **Nevertheless Android configuration
reads signing-related environment/properties:** root must use an explicitly
constructed credential-free environment and an isolated source/cache/HOME, not
inherit the interactive shell, owner Gradle init scripts or local.properties.
Do not alter version, build1017001, app identity, publisher/support-email source,
signing requirements or dependency/verification policy to make configuration pass.

**Not metadata-only:** resolving an artifact view can download the already
declared artifacts/metadata or execute plugin-managed artifact transforms, even
with a three-node task graph. The graph guard does not police hidden transform
work or prove that configuration is inexpensive. Root's existing one-shot
outer process/resource/source envelope must cover the whole invocation. Android
SDK/JDK auto-provisioning stays disabled; this request supplies no emulator,
SDK, new JDK/Kotlin-Native installation or toolchain provisioning authority.
An unavailable prerequisite is a recorded failure/blocker, not a reason to
broaden scope or silently change dependencies, target definitions or providers.

## One focused invocation, not a new general runner

`REQUEST.template.json` supplies the exact candidate command and narrow result
contract. The checkout/init paths are **proposed new namespace names only**;
their existence or ownership was not probed. They must be created and pinned
once by a freshly reviewed root envelope. Source commit/tree/full manifest,
member/output capacities, tools, parents/lock, environment, original-object
ownership and executor/cleanup source remain **UNBOUND**. This plan/init are
not independently sufficient to launch anything.

Use root's smallest current serial Linux executor integration, not the archived
G10/G7/G8 programs, consumed runners or a second cache manager. Keep this an
exact-three-task invocation separate from tests/Detekt; it can immediately
follow another safely settled invocation within the same cooperative quiet
window. Stop and clean each invocation before the next. Do not run a separate
dry-run/configuration probe merely for reassurance: the exact pre-action graph
guard can discriminate this scope in the actual proposed invocation.

Prospective bounds: 600s Gradle work; 120s original wrapper stop; 120s settlement,
retention and cleanup, total900s hard outer budget. Use inherited launch/running
floors (12/8GiB disk and25%/20% available RAM), current root coordination and
periodic resource observations. Gradle heap2GiB, wrapper512MiB, one worker;
no compiler daemon. A floor/process/identity/capture failure preserves its
partial evidence and follows original ownership-based cancellation/HOLD rules.

`SOURCE-PINS.json` binds a focused configuration/source set and accepted evidence
references. It is **not** a full current source freeze or G3 affinity. Root's
final full source capture must supplement it; do not substitute the old C18
manifest, inflate an obsolete member cap or reuse stale source/index identity.
The existing wrapper/JAR are used; no new dependency or tooling repository.

## Evidence and interpretation

Retain exact command/environment policy, original exit/timeout, graph/start/finish
markers, compact stdout/stderr and all three independently captured reports
outside disposable R before deletion. Evidence files are regular no-follow
reads through root's original descriptor-bound tree, not wildcard copies.

Each successful producer needs exactly one accepted start and finish, `executed`
and `didWork` true, `skipped/upToDate/noSource` false, no failure, original overall
exit0 for a fully successful batch, and the bounded exact generated bytes.
Fresh source outputs must be absent before launch; no cached/up-to-date replay
may be called current producer execution. `--no-build-cache` and a new private
project/runtime namespace keep that contract small; do not force unrelated work.

Expected bytes are already bound by the accepted historical three-report union
and current expected lock. Exact size/SHA matches in the request settle equality
without rerunning the aggregate merge or fixed-data mapping predicates. A small
post-result source/data reconciliation also verifies expected header, scope,
unique sorted rows and row counts against the unchanged expected lock. Preserve
the actual report on mismatch; do not overwrite the checked-in lock or trust
metadata. Partial output is not an overall pass, but independently usable scope
observations can be retained without restarting successful unchanged scopes.

Accounting is **three prospective producer task executions, zero tests, zero
JUnit XML, zero application cases**, regardless of297 rows or277 coordinates.
Do not increment application test counters. Only after accepted current output
and final source/stop/cleanup reconciliation may the corresponding current
normalized production scope be promoted; root owns gate adoption.

This still does not prove downloaded artifact bytes/platform provenance, all
production transitive bytes, native component/legal assertions or packaged
notice inclusion. Module-component filtering excludes project-artifact rows;
Desktop Skiko runtime OS/architecture names collapse to
`skiko-awt-runtime-current-os`. Dependency verification remains strict during
the actual invocation, but this narrow report does not provide a complete
checksum/artifact attestation, advisory clearance or supply-chain certificate.
An iOSArm64 inventory produced on Linux is not a native compiler/device pass.

## Immediate bounded cleanup

Install root's reviewed cleanup/cancellation handling **before** the invocation.
After command settlement preserve compact evidence, invoke the original
checkout's wrapper `--stop` once with the same isolated HOME/Gradle/JDK/tmp
configuration, and prove owned worker/namespace settlement. No extra stop for
older consumed stop0 runs. The stop command carries only the common flags, not
the producer tasks, init script or `--continue`.

Remove only allowlisted run-created checkout build trees, private project state,
HOME/tmp/Konan/Android-user/XDG/JNA/SQLite state, private Gradle caches and the
new raw source copy through root's original validated object lifecycle. This is
not permission to delete W/T source, permanent tests, evidence, release output,
shared caches, SDKs/toolchains or any held/closed earlier runtime. Preserve even
unexpected Gradle configuration reports under the narrow admitted conservation
contract before disposal; unknown/over-cap/link/ownership/source/capture failures
remain HOLD, not a broad recursive deletion or a fabricated cleanup pass.

All PVU007 STOP, PVU011 NO RETRY, PVA02949/44/5FAIL/no automatic retry, G7/G8
CLOSED, native refusal, original HOLDs, protected refs/tags, occupied1017001,
unchanged dependency/trust/version policy and eight separate PVD decisions remain.
