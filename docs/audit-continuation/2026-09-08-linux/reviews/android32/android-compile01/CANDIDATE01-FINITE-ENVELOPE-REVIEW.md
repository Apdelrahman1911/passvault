# Android compile01 — candidate01 and finite-envelope challenge

Reviewer `/root/android32`, 2026-09-10. **SOURCE-ONLY REVIEW; NO EXECUTION ADMISSION.**
Init author: `/root/editor_review`. B = `docs/audit-continuation/2026-09-08-linux`.
Root owns live facts, execution, coordination, source publication and cleanup.

## Disposition and explicit contract change

**REVISE candidate01; ACCEPT the finite source-scope contract below in principle.**
Root explicitly proposed and adopted replacing an unknowable exact-row graph binding
with a finite exact-task-name envelope under ordinary trust in the frozen project and
strictly verified Gradle/AGP/Kotlin plugins. This is an explicit reviewed contract
change, not admission by observing/expanding an unexpected graph. No separate,
inevitably failing graph-only setup cycle is required by this revised contract.

This supersedes the exact-graph-equality prerequisite in my earlier proposal review
`ANDROID-COMPILE01-INDEPENDENT.md` (`aae71af0471ad61b81669305797ecc6848b822aebf666dc2addee2eac5c2617b`)
only. Preserve that sealed review and the original compile-plus-APK proposal as
historical source. Compilation remains the sole proposed operation; fresh outer
admission, SDK/component use authority, limits, original-wrapper stop, settlement,
source preservation and allowlisted cleanup are not supplied by this note.

## Source justification and finite envelope

The only requested selector is `:core:crypto:compileAndroidDeviceTest`; require it
and `:core:crypto:compileAndroidMain` in the realized graph. Every admitted task is
in `:core:crypto`. The module declares no sibling-project dependency in commonMain
or androidMain. Its only project dependency, `:core:testing`, is in commonTest;
`sourceSetTreeName = null` deliberately excludes that tree from this device fixture.
Do not admit other projects on a generic “dependency task” exception.

All 25 names below were registered in the retained Linux03 task listing. They form
an upper-bound set, **not an observed graph or assertion that all are necessary**.
Membership is exact and case-sensitive, without compile/check/resource/package
prefix rules. Capture the complete actual task list, concrete types and dependency/
finalizer edges before task actions. If claiming complete ordering constraints,
also retain mustRunAfter/shouldRunAfter, or state that those constraints are omitted.

| Allowed role | Exact task names (all prefixed `:core:crypto:`) |
|---|---|
| Compilers | `compileAndroidMain`, `compileAndroidDeviceTest` |
| Configuration / AAR metadata checks | `checkKotlinGradlePluginConfigurationErrors`, `kmpPartiallyResolvedDependenciesChecker`, `checkAndroidMainAarMetadata`, `checkAndroidDeviceTestAarMetadata` |
| Prebuild anchors | `androidPreBuild`, `preAndroidMainBuild`, `preAndroidDeviceTestBuild` |
| Source/R/resource generation | `generateAndroidMainEmptyResourceFiles`, `generateAndroidDeviceTestResources`, `generateAndroidDeviceTestSources`, `generateAndroidDeviceTestRFile` |
| Resource compilation/preparation | `compileAndroidDeviceTestNavigationResources`, `processAndroidDeviceTestNavigationResources`, `mapAndroidDeviceTestSourceSetPaths`, `mergeAndroidDeviceTestResources`, `parseAndroidDeviceTestLocalResources`, `processAndroidDeviceTestResources` |
| Manifest preparation | `processAndroidMainManifest`, `mergeAndroidDeviceTestManifest`, `processAndroidDeviceTestManifest` |
| Metadata output | `writeAndroidMainAarMetadata` |
| Explicit inert compile-JAR exception | `bundleAndroidMainClassesToCompileJar` |
| Explicit resource-table packaging exception | `packageAndroidDeviceTestResources` |

The last two names must be explicit exceptions to a blanket “no packaging” name
rule: compiler-classpath/resource intermediates are not an APK/AAR release build.
Do not extend these exceptions to runtime JARs, native copies, assets, DEX, APK/AAR
assembly, signing, installation, tests, SDK acquisition or publication. Reject every
root/sibling/unlisted task, including unexpected finalizers; retain failure and do
not automatically expand or retry the envelope. A `Test`-type guard remains useful.

Neither this lane nor `/root/build_config` nor `/root/editor_review` has retained
exact AGP9.4/Kotlin2.4.10 implementation-type references for these tasks. Capture
actual types without calling them independently proven safe types. Namespace or
`DefaultTask` membership alone proves no action semantics; the finite path/role
scope and frozen-source/verified-plugin ordinary trust are the admitted assurance.
No claim excludes malicious behavior hidden inside trusted plugins. Configuration,
resolution and artifact transforms can precede the graph guard; outer admission
must cover those operations. Graph checks are not a filesystem/network sandbox.

## Concrete candidate01 challenge

- The public Gradle `Task.inputs.files` receipts are accurately labeled generic
  declared inputs, **not compiler classpaths, libraries, visitation or variant
  provenance**. Pinning the four expected source files there and correlating the
  `--info` log with fresh emitted classes can establish useful compilation evidence
  without speculative Kotlin getter/API introspection. Missing classpath attribution
  remains a named evidence gap, not an invented prerequisite for compilation.
- Declared output roots, graph-time and pre-action emptiness checks, streaming
  hashes and post-action JVM17 class headers are materially stronger than task
  listing or success-only assertions. The output boundary is not deletion authority.
  Trust still requires root's fresh isolated state and exclusion of concurrent
  writers; path checks are not descriptor-anchored hostile-writer containment.
- `executed` alone and the added doFirst action do not prove original compiler work.
  Keep skipped/upToDate/noSource/skipMessage/didWork/failure receipts, original-action
  log correlation, required input receipts and a unique freshly emitted base class.
- Candidate01 does not reject dry-run and its successful `buildFinished` hook does
  not require both compiler completions. A dry-run can therefore report a successful
  build without those hooks/evidence. The author independently identified these
  same counterexamples; require explicit dryRun rejection and terminal enforcement
  of both accepted compiler completions. No runtime behavior was tested here.
- Tighten companion-class matching from a path substring to the same package
  directory plus exact basename / `$` companion convention. The author also proposes
  this correction. It is evidence precision, not a native-behavior fix.
- Replace exact graph binding with the explicitly adopted finite set above. Keep
  fresh read-root/offline binding: known live values can be bound before one compile
  cycle, unlike pretending a never-observed exact graph is already known.

Candidate02 must be independently reviewed as concrete new bytes before use. This
note is neither its implementation review nor an automatic acceptance of revisions.

## Exact reviewed inputs and operations

| Input | SHA256 |
|---|---|
| `B/reviews/editor-independent/android-compile01/android_compile_01.init.gradle.candidate` (10397B / 162LF) | `0cf5a3eae6b974ff7552abb623a036843c43ad86847d6d0f0c6509ff84575828` |
| Same directory `IMPLEMENTED-CANDIDATE-01.md` (7784B / 111LF) | `da8bb01f5b3f227498f06eb91cf2f8c834db59846956c8a3ef9fe226a4b38b0c` |
| Same directory `PROPOSAL.md` (11057B / 136LF) | `04d8c92b5f218359629ac8c8c115cc35fd57d24d38ba28e448920b373f22bebb` |
| `core/crypto/build.gradle.kts` | `a18a2ee9127da9861d4310ab7588573d0596d31c317e3ff393c8e30f70888112` |
| `build.gradle.kts` | `6c35802a0f779baa1b4d5a65396a07824653761882ddfbfaa718a46c59fa5ee0` |
| `settings.gradle.kts` | `a2a3336bd1cce66d616c2a49a1773f4d3c93d437759a093ff28e92e4bd646734` |
| `gradle.properties` | `323971ec6c0f02ff28c724b4c4f3120b5b8843faf3d77608485c1e15fb633235` |
| `B/runs/linux-isolated-batch03/logs/ordinary.log` (focused lines115–365) | `e79ed666f239eed08e4cfe606fcb1f41b19069301c3b768c40d42ab00aaaca5b` |

Only bounded retained-source/evidence reads, hashes and this permanent review were
performed. No candidate parsing/import/execution, build/test, JVM/Gradle, SDK/store,
network/Git/CI, process probe, dependency change or temporary runtime output/cache/
worker. No stop or deletion was needed for these short source-only reads. Zero
application/native/hardware cases, fixes, closures or denominator changes. The four
intended KDF cases/calls remain unexecuted; compiler success would not prove JNA
reflection, APK/DEX, a32-bit process, ABI/native behavior, business flows or hardware.
PVU007 STOP, PVU011 NO-RETRY, PVA029 FAIL/no automatic retry, G7/G8 CLOSED, all consumed/
HOLD scopes, eight PVD boundaries (including PVD002 hex compatibility), and the
non-publishing / occupied1017001 restrictions survive unchanged.
