# API02: narrow declared-producer contract component

SOURCE / retained public DATA only, 2026-09-14. **Component candidate for independent review, not an admitted run or a complete action-mode init.** No build, test, javap, runtime/SDK/cache access, network, source import/execution, or syntax/compile probe was performed here. The canonical Graph02 source and all sealed controls remain unchanged. No additional vendor capture is requested.

## Delivered component and exact scope

`PRODUCER-GUARD.gradle.txt` defines one uninvoked closure and an all-null binding template. `PRODUCER-GUARD.diff` is its **new-file** diff against `/dev/null`, not a patch applied to Graph02 or an installed init. There is no listener, selector change, task action, task mutation, output-file enumeration fallback, or action-release flag. The return data expressly says `taskActionsAdmitted: false`.

The component binds the normal pinned-vendor API contract for selected **declared producer paths**. It checks exact public member descriptors, task owner hierarchy, source-declared annotations where available, output property/location types, non-AAR R8 branch, required property presence, and strict containment in explicitly bound private producer build roots. Existing leaf kinds are checked; no output bytes are read. Getter names, intermediate paths, artifact tokens, worker members, and inherited declarations are not invented.

The finite native/R8 input API rows deliberately distinguish **declared methods** from **call-site references**. Provider/collection getters are invoked only to check their returned interface; their input values/files are not queried. Rule-list/helper members are public-capability/descriptor checked but **not invoked**. A missing/nonpublic/wrong-descriptor member fails the whole call; there is no alternate getter, accessibility override, skip, or retry. The checks do not prove that arbitrary provider implementations have no side effects.

This is useful confidence under the ordinary vendor contract, **not** a full vendor-worker/write-sandbox/provenance audit. Uncaptured worker bodies alone are not a blocker to this component. Useful production evidence must instead come from the later admitted task outcomes and actual output contents/hashes.

## Required fresh bindings / integration

The template has exactly five keys, all null:

* `checkoutRoot`: absolute normalized `java.nio.file.Path` for the original admitted private checkout.
* `r8TaskPath`, `nativeTaskPath`: exact retained-and-newly-admitted selected task identities, supplied by the new composition, not guessed here.
* `r8OutputRoot`, `nativeOutputRoot`: absolute normalized `Path` authorities strictly within that checkout; each must equal its selected task's actual `project.layout.buildDirectory` location. These are **build-root authorities**, not inferred `build/intermediates/...` leaf names.

Arguments also supply the two selected `Task` objects and a `checked(File, boolean)` closure with the named Host07 semantics, rebound to the **same fresh** checkout/runtime/read roots. Strings are not accepted as `Path` bindings. Null/unbound values fail before producer-provider inspection. There are no consumed physical namespace, run ID, source manifest, SDK, JDK, or admission literals in this component.

The future composed init must first validate its actual whole graph and action eligibility while all task entry remains barred, then call this component before releasing that barrier. It must retain the returned stable API/location map and compare a fresh call at selected-producer entry. File existence is deliberately absent from the returned stable map so legitimate creation does not masquerade as path drift. Existing-leaf kind checks still run on each call.

No call site is supplied because inserting this before Graph02's unconditional refusal would be dead action-mode code, not the needed new composition. The whole action-mode composition is **remaining work being continued**, not completed by this component.

## Retained inputs actually rehashed

Let `B = docs/audit-continuation/2026-09-08-linux` (relative to `passvault-linux`).

| Public DATA | Bytes / lines | SHA256 |
|---|---:|---|
| `B/runs/android-agp-producers-api02/01.stdout.txt` | 137157 / 1819 | `54ecf2e3e4c3a527a3c33efe9cfb18db67533e9af897e8698b3611bbdee45093` |
| `B/runs/android-agp-producers-api02/02.stdout.txt` | 67226 / 1076 | `1f99781f1aac78f372f73e8c8d1f462b6c34bb6e5b9a50e81e9381cc930cfbf9` |

The dump headers report R8Task.class 16903 B / `db00e2fba73f35dcfaeb7306eedcd2075ad71e2a9909a76692ef0b2fdf4e16ce` and MergeNativeLibsTask.class 11361 B / `e1b9a5bc00dc0ffdf4113d732c67fa4851f8cda240909bbeae06c7925c6828f8`. These are header DATA, not a fresh inspection of extracted classes. Both headers report major 61.

The root-supplied artifact identity remains AGP9.4.0 JAR 12137130 B / `606a2136d291e69ba18c9b21672ff62de0af65c1eb3a9f69c0e7da6a490348ca`; the runtime/JAR was not read here. Root reported API02 exit 0, both javap children reaped and temporary leaves removed. Those lifecycle facts are not independently adopted by this semantic/source component; the root/independent lifecycle review owns that evidence.

## Exact API map: declarations versus references

Below, `R8` means `com.android.build.gradle.internal.tasks.R8Task`; `Native` means `com.android.build.gradle.internal.tasks.MergeNativeLibsTask`. Dump line numbers are 1-based. A JVM `Methodref` whose symbolic owner is R8 does **not** prove R8 declares the method or that a superclass declaration is public.

### 1. Minified DEX declared path and branch

* **Declared** R8 `getOutputDex:()Lorg/gradle/api/file/DirectoryProperty;`, public abstract, `@Optional @OutputDirectory` (`01`, 723–733).
* **Declared** `getOutputClasses:()Lorg/gradle/api/file/RegularFileProperty;`, public abstract, optional output file (711–721). This is the alternative AAR branch, not the DEX accessor.
* R8 `doTaskAction:()V` offsets 5–60 (`01`, 896–919) calls reference-only `getComponentType:()Lorg/gradle/api/provider/Property;`, then `getOrNull`, casts `ComponentType`, and calls `ComponentType.isAar:()Z`. True selects classes; false/null selects DEX. The component is intentionally stricter: requires non-null `ComponentType` and `isAar == false`.
* Offsets 534–542 (1123–1127) capture this task, the selected output property, and feature-resource list into the worker-action lambda. The bootstrap identifies `doTaskAction$lambda$4` (1815–1819). The lambda offsets 560–578 (1525–1532) dereference that selected output and set `R8Task$R8Runnable$Params.getOutput:()Lorg/gradle/api/file/RegularFileProperty;` using its `FileSystemLocation.asFile`. The worker parameter's RegularFileProperty type does not turn the original DEX DirectoryProperty into an AAR/classes-file accessor.
* Both process-isolated and nonisolated branches submit the same `R8Runnable` with that captured worker action (1128–1159, offsets 544–629). This establishes parameter coupling, not executed DEX bytes.

### 2. Effective rules versus configuration diagnostic

The exact **call-site references**, not captured declarations, are:

| Symbolic owner R8 member | JVM descriptor | Call-site locator |
|---|---|---|
| `obtainKeepRules$gradle_core` | `()Ljava/util/List;` | CP #413, line 425; lambda offset 132 |
| `getExtractedDefaultProguardFile` | `()Lorg/gradle/api/file/DirectoryProperty;` | #416, 428; offset 136 |
| `getFailOnMissingProguardFiles` | `()Lorg/gradle/api/provider/Property;` | #419, 431; offset 143 |
| `reconcileDefaultProguardFile$gradle_core` | `(Ljava/util/List;Lorg/gradle/api/provider/Provider;Z)Ljava/util/List;` | #425, 437; offset 164 |
| `getProguardConfigurations` | `()Ljava/util/List;` | #445, 457; offset 214 |

Lambda offsets 126–170 (`01`, 1371–1389) pass the keep-rule list through default-file reconciliation, together with the directory provider and boolean missing-file policy, into `Params.getProguardConfigurationFiles:()Lorg/gradle/api/provider/ListProperty;`. Offsets 209–220 (1402–1407) separately set `Params.getProguardConfigurations:()Lorg/gradle/api/provider/ListProperty;` from the R8 list.

In contrast, **declared** R8 `getProguardConfigurationOutput:()Lorg/gradle/api/file/RegularFileProperty;` is `@OutputFile` (848–856); lambda offsets 508–529 set the corresponding worker output parameter. It is an output diagnostic, not the applied default/app/consumer input list. The component never claims otherwise.

The helper implementations, List element types, exact default/app/generated/consumer membership, and ordering are not present in the two dumps. The narrow component therefore binds/checks these capabilities without calling reconciliation early. Later effective input/configuration evidence remains necessary; a source keep line or generated configuration path alone is not evidence of actual minified retention. No follow-on vendor capture is implicitly requested.

### 3. Mapping and diagnostics on the same R8 parameter set

* **Reference-only** R8 `getMappingFile:()Lorg/gradle/api/file/RegularFileProperty;` is CP #489 (`01`, 501), called at lambda offset 409. Offsets 404–425 (1477–1484) dereference it and set the same-name Params property.
* **Reference-only** R8 `getMappingPartitionFile:()Lorg/gradle/api/file/RegularFileProperty;` is #493 (505), called at offset 435 and set at 451 (1485–1492).
* **Declared**, public abstract `@OutputFile`, all `()Lorg/gradle/api/file/RegularFileProperty;`: `getProguardSeedsOutput` (828), `getProguardUsageOutput` (838), `getProguardConfigurationOutput` (848), `getMissingKeepRulesOutput` (858), `getR8Metadata` (868). Lambda output parameter blocks are respectively 456–477, 482–503, 508–529, 534–555, 818–829.
* Analyzer data/report outputs are declared optional RegularFileProperty outputs (780–802), set at offsets 352–399 only if declared boolean `getEnableR8ConfigurationAnalyzerReport` is enabled. The component honors that condition.
* The mapping/diagnostic/selected output assignments share the lambda task/Params pair; this is stronger than unrelated output filenames. It is not an assertion that any invocation or writer completed.
* `Params.getInputProguardMapping` is different: offsets 175–204 use reference-only `getTestedMappingFile:()Lorg/gradle/api/file/ConfigurableFileCollection;` (1390–1401). The component does not confuse that input mapping with raw output mapping.

Additional declared R8 outputs are guarded without claiming their production: optional feature DEX/resources directories, main-DEX list, ART profile, classes-file branch; required Java-resource output; optional analyzer diagnostics. This avoids a narrow DEX-only path claim while silently ignoring other known output locations.

### 4. Native declared output and limited input provenance

**Declared** Native outputs (`02`):

* `getOutputDir:()Lorg/gradle/api/file/DirectoryProperty;`, `@OutputDirectory` (594–602).
* `getTestOnlyDir:()Lorg/gradle/api/file/DirectoryProperty;`, optional output directory (604–614).
* `getMergeBlameFile:()Lorg/gradle/api/file/RegularFileProperty;`, optional output file (539–549).

Under the normal MergeNativeLibs producer contract, `getOutputDir` is the declared merged-native output to guard/capture. It is not the package consumer's stripped-native accessor. The outer body submits its work action at offsets 425–449 (793–805); the worker lambda/body is not dumped. **That absence is not a demand for a full worker audit and does not block this narrowed declared-path component.** Later successful-task contents are the useful evidence.

**Declared** ConfigurableFileCollection getters: project (429), subproject (447), external (465), test-only (499), unfiltered project (616). All but unfiltered project have `@InputFiles`; unfiltered project is `@Internal`. Declared profiler DirectoryProperty (483) is optional/classpath. Declared `getExternalArtifactCollection` (517) and `getSubProjectArtifactCollection` (528) have descriptor `()Lorg/gradle/api/provider/Property;`, generic `Property<ArtifactCollection>`, and are **@Internal**. Excludes/pick-firsts/test-only are declared input `SetProperty<String>` (551/562/573).

Native `doTaskAction` consumes its producer-held ArtifactCollections via `getOrNull` and `ArtifactCollection.getArtifacts:()Ljava/util/Set;` at offsets 22–155 and 156–289 (`02`, 641–742). `ResolvedArtifactResult.getFile:()Ljava/io/File;` is paired with `getId:()Lorg/gradle/api/artifacts/component/ComponentArtifactIdentifier;`, then `getComponentIdentifier:()Lorg/gradle/api/artifacts/component/ComponentIdentifier;`, then `getDisplayName:()Ljava/lang/String;` (offsets 102–124 / 236–258). This builds an origins map, not full variant/byte provenance.

Offsets 313–424 (755–792) visit **unfiltered** project, subproject, external, test-only, and optional profiler file trees. The component binds those actual declared getter objects, not just generic runtime Configuration or filtered project inputs. It does not query their files here. Display-name association is not proof of exact variant attributes, resolved artifact bytes, transformed `.so` origin, same-resolution linkage to R8/JNA classes, or actual selected ABI outputs.

R8 input references also bound by exact names/descriptors include boot/feature/referenced/tested-mapping ConfigurableFileCollections, resources RegularFileProperty, and default DirectoryProperty. `getProgramClasses:()Ljava/util/List;` (#474, lambda 306), main-DEX helper lists, and rule helper lists are descriptor checks only. Main-DEX helper rules are not substituted for general effective shrinker rules.

## Existing control to reuse, not replay

Named SOURCE files were hashed as bytes only:

| Existing source | Bytes | SHA256 |
|---|---:|---|
| `scripts/audit/android_release_graph_02.init.gradle` | 27891 | `f4f64959d58c7ba837439de20ae27c85079c84b843c1742f63121b5601d2beee` |
| `scripts/audit/android_host_07.init.gradle` | 57246 | `5f96d323ab318a29120f19df6616eedd31c2906c6ba001897b015e8f5116d4fe` |
| `scripts/audit/linux_android_host_07.py` | 82662 | `1624bcd910714b47a812394c05d4c13b9cd6ff286be088a9421e7aa0aec3e987` |

Host07 init has `checked` at 428–442: read/write envelope, no symlink selected path/ancestor, regular/directory types, private uid and single-link regular files. It explicitly describes cooperative guards, not an arbitrary-plugin sandbox or hostile-writer protection; outer original-root identity remains necessary.

Room controls to preserve in the new composition:

* `ROOM = :core:database:copyRoomSchemas` (400), original private `core/database/schemas` root (418).
* `schemaEqual` (483–487) compares the complete relative schema set/hashes, not merely directory existence.
* `outputPaths` (525–537) checks exact Room owner, directly reads `getSchemaDirectory(): DirectoryProperty`, confines its actual **@Internal writer destination** to that private copied schema root, and rejects binding drift. Generic outputs remain a separate witness, not a manufactured declaration.
* Global pre-action equality at 821; producer/task output identity recheck at 830–835; before-finalizer equality at 835.
* After-finalizer 865–876 accepts only successful action or exact unchanged `NO-SOURCE`, requires after equality, and does not falsely claim a copy/schema test ran.

These are semantic source controls to compose freshly. **Do not reuse Host07's consumed instance, selectors, 333-task graph identity, test/worker contracts, or terminal receipts.** The companion Python source and `B/reviews/android-host07-outer` are named existing integration references, not run authority; the outer review directory was not inspected here.

## Actual remaining action composition / product risks

The next work is a new unused-source two-root composition, not another Graph02 run: selector/CLI and exact arguments, source/instance binding, whole actual graph admission (including retained uncertainty nodes and Room finalizer), output and schema barriers, consistent before/after/build-finished/result acceptance, then corresponding existing-driver/outer contract review. The component alone does none of those.

Keep the existing policy distinctions: no 390-action cap/forecast; unknown or changed actual graph identities cause whole-attempt refusal, not task skipping. Ordinary nonpublishing library JARs and local-lint AARs are not forbidden just because names contain package/bundle/publish. Final application APK/AAB production, signing, publication/Store/install actions stay barred. The 112 uncertainty nodes include generated/consumer-rule roles and cannot all be dismissed as lint noise.

Once separately admitted, useful evidence must couple fresh successful R8 and native producer outcomes to actual files/content hashes, mapping/diagnostics and applicable rule inputs, and bounded same-resolution Java/native artifact information. Actual DEX retention, native ABI/JNA linkage, and same-resolution provenance remain unverified. No build/test/PVA result, fix, closure, or publication authority is asserted here.

Verification of this delivery is limited to manual SOURCE/DATA reading, exact byte/hash checks, and textual new-file-diff reread. No Groovy/Python parser, compiler, syntax checker, test, or runtime probe was used. Independent source review is still required.
