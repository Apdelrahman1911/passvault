# C20 Android host graph01 — resumed independent component review

2026-09-12; reviewer `/root/c20_android_review`, author
`/root/android_platform_author` (resumed by `/root/c20_android_author`).

**ACCEPT the two existing source components for new root-owned assembly. No
source correction is supported by this review. NOT a complete executable,
request, current-instance, configuration-run or host-test admission.** Preserve
their bytes rather than rewriting them for review ceremony.

## Exact source read

B is `docs/audit-continuation/2026-09-08-linux`. Under
`B/reviews/team20/android_platform_author/c20/host1/`:

| Component, fully read | SHA-256 | LF |
|---|---|---:|
| `ANDROID-HOST-GRAPH01.init.gradle.UNBOUND.txt` | `1288054953f4e09befe835d0f6ba5222932911cb3a5ea1e3918e4ea038ef07f1` | 294 |
| `ANDROID-HOST-GRAPH01-COLLECTOR.delta.py.txt` | `b94aecf631f251bb419cd45f1189a2d956c7c9328bd8e3ce0e0b583783d8b2fd` | 284 |
| `ANDROID-HOST-GRAPH01-INTEGRATION.md` | `a191b23c151d881253da94156ccdee0e4f3ef091e360964c5c5ea8b06af25464` | — |

The prior independent `android_platform_review/c20/host1/SOURCE-CONTRACT-PLAN.md`
and applied one-method review remain applicable. Their execution prerequisites
are not silently removed. I additionally read the reused Compile03 command,
original-stop, settlement, input/source/SDK-binding and final-result loci as
**text**, not by import or invocation; that is not a fresh full-engine audit.

## Independent challenges and dispositions

1. **Task-action barrier, not dry-run reliance.** Init134–155 installs unconditional
   before/after-task refusal before binding/configuration checks. Init206–294
   records at most one graph and always throws from `finally`, including metadata
   errors. The intended exception exists only after the exact sole-Test policy.
   `--dry-run` is another defense, not the proof. A missing or rejected graph does
   not cause an automatic graph-to-test transition or retry.
2. **What can run before that barrier.** Project/plugin configuration, provider
   evaluation and dependency resolution can execute code/write files. The hooks
   are not a sandbox or proof against direct/nested/included-build execution.
   Root's final source/configuration admission must exclude introduced `buildSrc`
   or included-build task execution and other non-intercepted task work. The
   inspected `settings.gradle.kts` contains no `includeBuild`; this is not a final
   manifest-wide assertion. SDK/JDK acquisition stays disabled, with the separately
   admitted read-only existing SDK view and exact clean environment.
3. **No permissive Android/package exemption.** The graph's finite path/type/edge
   rows are observations only. Unknown resource, package, archive, Exec, signing,
   assembly, device or finalizer behavior does not become executable permission.
   At host1 admission each actual prerequisite still needs a concrete supported
   role/type/path/output mapping. App `build.gradle.kts:630–638` enables Android
   unit resources, so one Test selector does not imply SDK-free or one-node work.
4. **Selective provider reads are not deletion proof.** Init246–268 queries only
   the selected XML DirectoryProperty and, on exact source/decorated hierarchy,
   the legal-assets output getter. Unknown legal hierarchy remains unqueried.
   App `PrepareAndroidLegalAssets` uses `FileSystemOperations.sync`; its deletion
   destination and physical/symlink-safe containment must be reviewed before
   future task actions. Current lexical output records expressly do not prove it.
5. **Exact one future method.** The include is the applied new unsuffixed method,
   with no CLI filter override or exclusions, fail-on-no-match, no ignoreFailures
   and serial fork policy. Its applied file remains SHA-256
   `ea3b535b99390d4850269bcc2089c8f6e26c1096cd17052ef361fae3ba5b3332`.
   The six older cases are not selected. Graph01 starts no Test worker; the future
   worker startup/isolation and one fresh XML/task-state contract remain separate.
6. **Intended refusal is local evidence, not a successful build.** The collector
   keeps original LF suffix/log bytes before parsing, rejects duplicate JSON keys,
   extra/malformed receipts and task/success diagnostics, and requires the exact
   original command's complete exit1 and error-free log identity. It does not
   redefine unchanged `command()` exit1 as a zero exit. The callback failure tree
   admits only the intended object and three named wrapper classes; unknown,
   suppressed, cyclic, extra-leaf or over-bound trees stay nonaccepted. These
   wrapper names are a recognition contract, not an observed current chain.
7. **Later failure and cleanup counterexamples remain explicit.** `buildFinished`
   cannot see all later listeners/shutdown errors; independent final raw-log and
   terminal/process review remains mandatory. Two markers or local mapping alone
   cannot authorize cleanup. The assembly plan retains original stop, settlement,
   source-after, index/SDK/evidence stability and owned outer cleanup conditions.
   A preserved semantic mapping failure may be cleanup-safe only if those separate
   conditions genuinely hold; missing retention/safety proof remains HOLD.

## Remaining concrete integration gates

- Review the exact new inner/outer delta and graph-only result schema, including
  initialization of graph flags before intake, removal of reachable compiler work
  and the exact isolation-plus-observation phase contract. Do not inherit the old
  `build_ok`/compiler-success formula or reinterpret a compile result schema.
- Bind one current source/index manifest, matching command/init/inner/outer/request
  hashes, roots, tool/SDK/config/ownership facts, offline policy and coherent source
  count/OID/byte capacities. Null or old C18/C19 instance facts are not authority.
- Admit configuration, resource/coordination/retention/original-stop/cleanup once;
  then independently read the actual graph and final logs before preparing a
  finite host1 action allowlist. No automatic broadening on unknown tasks.

Zero build, configuration, compiler, application-test, graph or cleanup execution
occurred in this reviewer lane. No Git, helper import/replay, SDK/device/runtime or
process probe occurred. Source hashes establish bytes only. Compilation03's useful
success remains reusable and unrepeated. PVA-009/030 actual-framework, Android32,
license/target and physical-device gaps remain separate; no finding closure,
application testcase or percentage changes follow.

Root remains sole build/CI/Git/admission/cleanup owner. Preserve PVU007 STOP,
PVU011 NO RETRY, PVA029's 49/44/5 FAIL and no automatic retry, G7/G8 CLOSED,
original native refusal, consumed/HOLD scopes, protected refs/build1017001,
version/dependency/identity and PVD/hardware/account/Store restrictions.
