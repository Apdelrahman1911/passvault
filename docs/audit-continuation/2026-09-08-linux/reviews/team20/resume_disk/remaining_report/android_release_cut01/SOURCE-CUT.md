# Android minified Release / native-carrier cut01 — SOURCE/DATA only

**Disposition: two existing producer candidates identified; complete action envelope,
output binding and execution admission remain HOLD. No command, new helper,
graph recapture, matrix, build, test or runtime action is proposed here.**

## Accepted observation, not action authority

The original ReleaseGraph02 observation/refusal/local settlement is now independently
accepted and adopted by root. `SOURCE-PINS.json` binds that review/adoption and the
starting new87 note. The retained graph is226997B,
SHA256 `6348308a23d92a1fac1566ace2372cf16693d6766c53f2fec7f6fcd46e7a5fdf`:
562 nodes;1373 dependency references +3 finalizers +1 ordering reference =1377.
All output roles were deliberately UNQUERIED/UNBOUND. Actual acceptance recorded
zero task actions/cases; it did not admit R8, native actions or output providers.

## Smallest producer pair supported by the recorded relation

These are **inert proposed roots**, not an executable selector list:

| Observed task | Exact observed task type | Dependency+finalizer closure |
| --- | --- | ---: |
| `:app-android:minifyReleaseWithR8` | `com.android.build.gradle.internal.tasks.R8Task_Decorated` |340 |
| `:app-android:mergeReleaseNativeLibs` | `com.android.build.gradle.internal.tasks.MergeNativeLibsTask_Decorated` |84 |

The least fixed point over recorded dependencies and recursively expanded finalizers
is390 nodes (389 dependency-only),756 dependencies +1 finalizer, no ordering edges
and no missing/out-of-set targets. The roots overlap at34 nodes; native merging adds50
nodes beyond R8. All390 are enabled; none has a Test/Exec/JavaExec/Archive flag.
`CUT-DATA.json` preserves exact membership, root records/types and count checks.
**Flags are not proof of absence of archiving, native tools, subprocesses or side effects.**

Neither root reaches the other. The only observed single nodes whose recorded closure
covers both producers are `packageRelease`, its APK-listing finalizer and
`assembleRelease`; all include final app packaging and the signing gate, so are barred.
`stripReleaseDebugSymbols` would add one node (391 total) and is deliberately not
selected: merged pre-strip native-carrier facts are the narrower evidence scope.
It would not prove packaged bytes or runtime loading either.

R8 directly requires the existing `extractProguardFiles`,
`mergeReleaseGeneratedProguardFiles`, compiled classes, resources and other recorded
upstreams. Those producer identities do NOT identify the complete applied R8-rule
file set. `mergeReleaseComposeMapping`, profile compilation, resource conversion/
optimization, symbol extraction and native-debug-metadata aggregation are not added.
Native merging has the recorded source-folder merge plus16 project-only JNI-copy
upstreams. Its external runtime-artifact inputs/provenance remain unbound.

## Critical completeness limit:390 is NOT the scheduled action envelope

Recomputing the same dependency+finalizer closure from the **original** observed
selector `:app-android:assembleRelease` reaches450, not the562 observed nodes.
The112 remaining nodes have no incoming reference from that closure in any of the
four recorded relations. They comprise16 instances each of local-lint AAR bundling,
AAR-metadata checking, consumer-ProGuard export, annotation extraction, generated-
ProGuard merge, Java-resource merge and library-JAR synchronization. All16
Archive-flagged local-lint-AAR tasks are in this112-node remainder.

This does not refute the accepted562/1377 observation. It establishes that the
recorded edge relation alone does not reconstruct observed membership. Whether
input/provider/transform scheduling or another mechanism accounts for it is NOT
established here. Consequently, exclusion from the390-node relation cannot be
promoted to proof that a future two-selector invocation schedules only those390.
**Root/reviewer must resolve action-envelope sufficiency from admitted retained
SOURCE/DATA, or keep action admission HOLD. No new capture/helper or guessed edge
repair follows from this note.**

## Terminal exclusions and similarly named intermediates

The recorded390-node cut excludes `verifyReleaseSigningConfiguration`,
`writeReleaseSigningConfigVersions`, `packageRelease`, its APK-listing finalizer and
`assembleRelease`. There is no signing/store/upload/install/publish task-name hit
inside it. AAB/archive verifier tasks are source-declared but absent from this
assemble-only observation; absence is not new graph proof or admission.

Do not confuse the two `package*Resources` nodes (MergeResources),
`processReleaseManifestForPackage` (ProcessPackagedManifestTask), and32
`bundleAndroidMainClassesTo*Jar` nodes (BundleLibraryClassesJar) with final APK/AAB
packaging. They are intermediate resource/manifest/class preparation, not permission
to package an app. Their actions/output ownership are still unadmitted. Conversely,
`packageRelease` itself has isArchive=false: a flag/name-only safety filter is invalid.

Source app706–717 gates the four final Release aggregate/package tasks. Keep that
gate unchanged; `requireReleaseSigning=false` does not remove it. Do not select
archive verifiers, aggregates, Debug or storeScreenshot as a workaround, and do not
skip/disable signing tasks. No unsigned APK/AAB may be produced.

## Mandatory guards that the small root list does not remove

- **Room/KSP:** `:core:database:kspAndroidMain` has the retained finalizer
  `:core:database:copyRoomSchemas` (RoomSchemaCopyTask). It is the one finalizer-only addition to389, absent
  from a dependency-only count. Source database69–77 still targets
  `$projectDir/schemas`. Preserve the separate direct Room/schema guard and
  exclusively owned private copied-schema/compiler/output roots, including failure
  and finalizer paths. Neither finalizer omission nor canonical/shared schema writes
  are admitted. No guard/output implementation is bound by this note.
- **Signing scope:** app442–525/602–606 still reads environment/Gradle properties and
  can inspect a configured keystore path during configuration. A graph-cut exclusion
  does not eliminate those reads. A later no-key action needs independently admitted
  clean isolated environment/property scope, not inspection of real credentials,
  signing availability or signing getters. No credentials were read here.
- **Source R8 policy:** keep the sole existing active directive
  `-keep class com.ionspin.kotlin.crypto.** { *; }`; no blanket libsodium dontwarn.
  A literal source check matches this policy; the Gradle verifier did NOT execute.
  `verifyAndroidR8Policy` is wired to `check`, not observed in this graph. Do not add
  `check`: source805–809 also brings Debug archive verification.
- **Identity/resolution/resources:** preserve Release, identity
  `com.passvault.android`, build1017001/version1.0.7, dependencies and versions.
  Bind exact source and same dependency resolution, operation-specific resources,
  one root launcher and owned settlement/cleanup before any distinct action. Graph02
  resource samples do not establish R8/native action feasibility.

## Evidence goal, not a new matrix or achieved result

The eventual narrow evidence would be actual Release minified DEX and effective
applied R8 rules/mapping, plus byte/provenance-bound matching native carriers.
RawPasswordHash.android71–74 obtains libsodium's JNA proxy/Library.Handler and resolves
`crypto_pwhash`; lines47–57 use native size_t width. Retention evidence must address
that bridge and the name-sensitive libsodium interface/Structure boundary, not only
readable names or an app keep-rule declaration. Default/JNA consumer rules remain
unresolved inputs; generated-ProGuard task identity is not their full merged content.

Android catalog JNA5.19.1 is **compileOnly** (crypto67–72); source says libsodium's
Android artifact supplies runtime JNA. Verification metadata contains several JNA/
SQLite versions. It is not a resolved runtime graph; no runtime AAR version/hash is
inferred from it. Future carrier evidence must match the R8 resolution exactly.

Source declares four ABIs/six native-library names. Existing header checks expect
little-endian ET_DYN, ELFCLASS32/EM_ARM40 for armeabi-v7a and ELFCLASS32/EM_3863 for
x86 (app159–230). These are source expectations, NOT observed ELF headers. Do not
repeat unchanged Debug package inspection or introduce another architecture matrix.
Merged pre-strip intermediates do not prove stripped/final archive bytes, Android32
load/KDF, vault/backup behavior, signing, installability or Store readiness.

## Minimum API/output facts still needed — no API names guessed

The next source decision is bounded to the two observed producer types and the
KSP/Room pair, not all562 outputs. `CUT-DATA.json` enumerates six exact subjects:
complete scheduling membership (including implicit/provider/transform work);
R8 minified-DEX output; effective applied R8 rules/raw mapping;
merged pre-strip native output plus same-resolution input artifact provenance;
KSP/Room private schema destinations; and clean signing-free configuration scope.
For each justified role, establish the exact supported API/getter from admitted
source, provider kind/cardinality, producer/toolchain affinity, owned root and
read/write scope. These API names and output paths are **not established here**.
Generic outputs.files, task flags or conventional build/intermediates paths do not
bind those facts. Root/independent reviewer decide any narrow target-graph/action-
source extension using existing controls separately; this packet authors none.

## Delivery and unchanged boundaries

Only this fresh owned review directory is written.18 named pins were independently
rehashed:9 current source,4 retained DATA,5 instruction snapshots. Named source
matches the starting note; app source matches the graph subject. Full-W/full-P22
manifest or store affinity was not independently reread here. Hashes bind bytes,
not correctness. `CUT-DATA.json` is derived inert DATA, not a helper/allowlist.

Zero product fixes, task actions, cases, closures or owned workers/stop duties.
PVU007 STOP; PVU011 NO RETRY/no inquiry; PVA029 FAIL/no automatic retry; G7/G8 CLOSED;
native/PVU008 refusals; all HOLD/UNKNOWN including Windows remote UNKNOWN; protected
refs/tags/build1017001/dependencies/versions/identities/signing/Store remain unchanged.
Root remains sole launcher, cleanup and Telegram-notification owner. Independent
challenge of this source candidate is still required; no execution follows delivery.
