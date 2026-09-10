# Android compile01 — implemented minimal init candidate

Author `/root/editor_review`, 2026-09-10. **SOURCE CANDIDATE; UNBOUND / NOT
EXECUTION-ADMITTED.** Root owns any execution, coordination, containment and cleanup.
This supersedes only the selection/collector part of `PROPOSAL.md`; preserve that
sealed historical proposal and Android32's independent scope review unchanged.

## Fixed code and truthful contract

`android_compile_01.init.gradle.candidate`: SHA256 `0cf5a3eae6b974ff7552abb623a036843c43ad86847d6d0f0c6509ff84575828`,
10397 bytes / 162LF. New inert-name source, not imported, parsed,
syntax-checked or executed. Independently challenge this exact file before adoption.

The sole selector is `:core:crypto:compileAndroidDeviceTest`. Main compilation is
required as an actual dependency, never another explicit selector. The init uses
stable public Gradle task graph/input/output/state interfaces, **no speculative
Kotlin getter/reflection, artifact-view or configuration-discovery framework**.
Android32 accepted this smaller evidence contract in principle in coordination;
that is not review/acceptance of this implementation.

- Bounded ordered graph rows contain paths, concrete types, enabled state,
  dependencies and finalizers. Literal `ADMITTED=null` refuses before task actions.
  A replacement literal must contain only exact reviewed graph rows, fixed absolute
  read roots and the selected offline Boolean; source hash and independent review
  change with that binding. No environment binding, graph auto-expansion, task-name
  rediscovery or automatic retry. Configuration/resolution/transforms can precede
  graph guards; an UNBOUND capture itself would still need fresh owner admission.
- Both registered compiler tasks get **generic `Task.inputs.files`** path/size/SHA
  receipts, including directory contents, checked for the four pinned required
  source files below. These are not compiler-library/variant/transform provenance,
  argument order, compiler per-file visitation or a complete platform/JDK inventory.
  No runtime classpath is deliberately resolved. Generic task input evaluation may
  resolve inputs/transforms the compile task declares; admission must cover it.
- Actual `Task.outputs.files` declarations supply the inspected roots. They must be
  inside the fixed `core/crypto/build` generated boundary; this is an allowed scope,
  **not a guessed compiler destination or cleanup authority**. Every declared root
  is checked empty/absent at graph time and again before its compiler actions.
  No cache/rerun shortcut or deletion is performed. Root must also establish fresh
  isolated source/build/private-cache state and no concurrent writers.
- Task receipts retain executed/skipped/upToDate/noSource/skipMessage/didWork/failure.
  `executed` or the added doFirst action alone is not compiler proof. Completion
  requires those statuses, a matched input receipt and a newly emitted unique
  JVM17 base class plus hashes of same-name `$` companions under declared outputs.
  The main base is `LibsodiumCryptoEngine.class`; device base is
  `Android32KdfInstrumentation.class`. Header checking is bytes only, not loading.
  Root must correlate the bounded wrapper `--info` log with original compiler
  actions; do not infer action/variant proof from status flags or hashing alone.
- Limits:64 graph nodes,16 read roots,16 output declarations per compiler,4096
  top-level inputs per compiler,16384 aggregate traversed entries,256MiB/file,
  2GiB aggregate streamed hash reads,1MiB/event and8MiB JSON payload total (terminal
  reserve). These are collector limits, not RSS/new-disk/time/log supervision.
  Prefixes/newlines and Gradle logs require the outer cap. No large retained binary,
  generated evidence file, subprocess, deletion or worker is introduced by the init.

## Invocation/admission still owned by root

Reuse checked-in wrapper9.7.1 (distribution SHA
`acd53f1edaf02f1a8ff99879f8a34b302661a057d9b063ae9e35b552f804d20a`),
JDK17 and exact frozen full source/EOL/index pins. Required outer flags are
`--no-daemon --max-workers=1 --console=plain --no-parallel
--no-configure-on-demand --no-configuration-cache --no-build-cache
--dependency-verification=strict --stacktrace --info`, fixed init and sole selector,
`-Pkotlin.compiler.execution.strategy=in-process
-Pandroid.builder.sdkDownload=false`, the bound single JDK17 installation path,
auto-download/detect false, and reviewed existing heap/environment bounds.
No `--continue`, `--rerun-tasks`, install/connected/assemble/sign/package/publish,
SDK licenses command/marker edits, dependency change or online/offline fallback.
The init checks stable serial/start flags plus version/properties; it does not
pretend to independently prove wrapper identity, daemon absence, configuration-cache
suppression, effective compiler toolchain, environment sanitization or containment.

Before any invocation root must bind graph/read scope, installed-component use and
SDK authority, wrapper/dependency seed or resolution policy, source identity,
coordination, floors/reserves and cleanup. Suggested ceilings remain900s work+600s
original wrapper stop+300s preservation/cleanup,4GiB owned RSS/new disk,
16MiB logs/32MiB retained evidence; enforce or honestly qualify independently.
Preserve original failure/logs, run the original wrapper's admitted `--stop`, verify
owned worker settlement and remove only validated allowlisted generated outputs,
private temporary caches and temp. This init neither authorizes nor performs that.

## Pins, deliberate evidence gaps and restrictions

Input guards at authoring matched the source/API-review basis and required files:

| Required generic task input | SHA256 |
|---|---|
| commonMain/.../LibsodiumCryptoEngine.kt | `2b8dd21fe476735f16f1ab35543417a70202e7923dfac328f9064e31f5923b57` |
| commonMain/.../RawPasswordHash.kt | `3fb16abe001752bf44b5a56039c58738ebed0e6ced5972e3ca0985d5a40586eb` |
| androidMain/.../RawPasswordHash.android.kt | `d2c4bcd22df2cb89a97e10a4b591d651f224f0b1c5224bc6b5de10f407d0df1c` |
| androidDeviceTest/.../Android32KdfInstrumentation.kt | `d9bdb00637773f5d239ffcc5f7d13dc3773e528ef0dec1b1efdffec0ee182310` |

Other guarded inputs: module build file `a18a2ee9127da9861d4310ab7588573d0596d31c317e3ff393c8e30f70888112`;
Android32 scope review `aae71af0471ad61b81669305797ecc6848b822aebf666dc2addee2eac5c2617b`;
Linux03 task registration log `e79ed666f239eed08e4cfe606fcb1f41b19069301c3b768c40d42ab00aaaca5b`;
static02 init `2ff75e0d26f4bd2d7bca7a0c62d929fed9174115ad20b665e5b10caabe51b9e6` (mechanics as text only,
not its instance/scope/acceptance). Exact compiler graph/types remain unobserved.

**Compiler classpath completeness, component/variant/transform attribution and
effective platform/JDK inputs remain explicit gaps, not prerequisites invented from
unsupported Kotlin getters.** Compilation can remove the never-compiled harness gap
without closing these other gaps. It proves neither JNA reflective API behavior nor
any of the four native cases/four intended KDF calls, a32-bit process, APK/DEX/manifest,
ABI payload, business flows, physical hardware or release readiness. New-image license
and usable32-bit-target authority remain blocked; this does not assert existing SDK
components are unlicensed. PVD002 hex compatibility and all eight PVD boundaries stand.

No build/test/Git/network/CI, SDK/store/runtime/process/lock probe, held-runtime read,
helper execution/import or syntax check was performed by this lane. Two permanent
source/note files only; no temporary output/cache/worker. PVU007 STOP, PVU011 NO-RETRY,
PVA029 failure/no automatic retry, G7/G8 CLOSED, consumed/HOLD and publication/1017001
restrictions survive. Zero cases, fixes, closures or denominator changes credited.
