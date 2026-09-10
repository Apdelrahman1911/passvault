# Android compile01 — independent scope challenge

Reviewer `/root/android32`; proposal author `/root/editor_review`; root alone
owns execution. 2026-09-10. **REVISE TO COMPILE-ONLY; SOURCE PREPARATION ACCEPTED,
NO EXECUTION ADMISSION.** Reviewed proposal SHA256
`04d8c92b5f218359629ac8c8c115cc35fd57d24d38ba28e448920b373f22bebb`
(11057 bytes/136LF), not an implemented runner or realized graph.
B = `docs/audit-continuation/2026-09-08-linux`.

## Smallest useful selection

Select only **`:core:crypto:compileAndroidDeviceTest`** in the prospective single
wrapper invocation. Drop the explicit assemble selector and APK/signing/native
collector requirements from this first scope. Compilation directly addresses
the still-uncompiled291LF harness and its real Android-main/compiler classpath.
It needs Android compile SDK inputs, not a booted Android device/system image.

Assembly would add meaningful **different** evidence: merged/final manifest,
runner DEX, generated test identity and native ABI payload. It cannot establish
actual32-bit loading or KDF results. With no usable32-bit target or admitted
binary-manifest/DEX inspector, those additional graph, transform, debug-signing
and artifact-cleanup surfaces are not necessary to remove the compile gap.
Defer assembly until imminent target preparation or a specifically identified
package/ABI question justifies it; do not forbid it generally or count its
absence as a failed compilation. If later assembly's admitted graph already
includes compilation, no second explicit compile selector is needed; that edge
has not been proven by the historical task listing.

## Wiring and counterexamples checked

- Current module lines9-25 configure AGP9.4 KMP `withDeviceTestBuilder`,
  `sourceSetTreeName=null`, runner `com.passvault.core.crypto.Android32KdfInstrumentation`,
  `execution="HOST"`, namespace `com.passvault.core.crypto`, compileSdk37,
  minSdk24 and JVM17. Null excludes common test source trees; HOST disables
  on-device orchestration, **not** Android execution or conversion to a JVM test.
- Linux03 actually registered `compileAndroidDeviceTest` at ordinary.log:228,
  described as compilation `deviceTest` in target `android`; main compiler:231,
  assemble:217, package:311. Connected tasks:156-157 install/run; host test:209
  is a different boundary. Task names are settled; **do not repeat tasks --all**.
  Realized dependency edges/types and device compile classpath remain unobserved.
- Module commonMain supplies coroutines/libsodium; androidMain has compile-only
  JNA5.19.1; androidHostTest adds the JVM sodium artifact. The harness imports
  production LibsodiumInitializer/LibsodiumCryptoEngine, not a fake/JUnit runner.
  No commonTest/core:testing substitution or dependency change is acceptable.
  Its reflective JNA API names will not be validated by ordinary compilation.
- The six-line source manifest declares multiArch/use32bitAbi only; compile
  success is not manifest merge, package/target identity or ABI proof. Do not add
  app assembleDebug/verifyDebugComposeResources: neither selects this harness.
  PVA009/PVA030's separate API29+ app instrumentation is outside this selection.

I authored the older harness; its independent source acceptance by build_config
(`B/reviews/build-config/ANDROID-FINAL-REVIEW.md`, SHA256
`0d4f1f7a26c6c119737a4aa265f5b3f7c3b268f56c74ff561ec561653f12a319`)
is reused, not replaced by self-review or a new product-fix claim here.

## Minimum evidence and admission delta

1. Freeze full source/EOL identity, SDK/tool/dependency-use scope and the exact
   selector/graph before task actions. Reuse reviewed current containment/stop/
   cleanup mechanics, **not** the static runner's instance or analyzer mapping.
   Replace only fixed selector/init and result collector. No general harness.
   If retained plugin/source evidence cannot establish edges, one separately
   admitted graph-only capture may stop before actions; no discovery-to-build
   auto-chain, graph expansion or repeat task-name discovery. Configuration,
   dependency resolution and artifact transforms can act before task execution:
   a graph check alone is not network/SDK/process containment.
2. Record actual task types/statuses and compiler input source hashes, including
   this exact harness and the Android main production dependency path. Require
   an actual non-NO-SOURCE compiler action and new compiler output in fresh
   allowlisted build storage; a task listing or stale UP-TO-DATE/cache result
   does not remove this never-compiled-source gap. Do not add --rerun-tasks.
3. Record main and device **compiler** classpaths separately: resolved component
   versions/variants, artifact/transform hashes and required platform/JDK inputs.
   Strict verification remains unchanged. Historical Android runtime JNA5.18.1
   and compile-only5.19.1 are not freshly resolved runtime facts. Do not force
   runtime-classpath resolution, native loads or APK assembly just to report it.
4. Obtain compiler output roots from actual admitted task metadata, not guessed
   build paths. Retain bounded names/sizes/hashes for the emitted
   `com/passvault/core/crypto/Android32KdfInstrumentation.class` and relevant
   compile receipts; do not load that class or instantiate Instrumentation.
   No APK parser/DEX/ELF/signing tool is necessary for this compile-only result.
5. Retain compact exact logs/graph/input-output/hash/failure/cleanup receipts;
   source-before/after, original wrapper --stop, owned worker settlement and
   allowlisted removal of generated outputs/private caches/temp remain mandatory.
   Root's fresh admission must enforce or honestly qualify budgets: at most900s
   work+600s original stop+300s preservation/cleanup, <=4GiB owned RSS/new disk,
   <=16MiB logs/32MiB retained evidence, unchanged launch/running floors plus
   planned allocation reserve. These are ceilings, not automatic reservations
   or guarantees supplied by heap flags. No automatic enlargement/retry.

Keep JDK17/wrapper9.7.1, one worker, no daemon/parallel/CoD/configuration/build
cache, in-process Kotlin, strict verification and toolchain auto-download/detect
prohibitions. Omit static-only --continue. AGP's existing SDK download option is
`android.builder.sdkDownload`; explicitly retain its false setting. No implicit
online/offline fallback: bind admitted wrapper/dependency seed or resolution
policy once. --offline does not sandbox wrapper/plugins; empty private caches
do not prove dependency availability. No shared SDK/cache edits or broad copies.

## SDK/license boundary — precise, not blanket

Retained `ANDROID-MANIFEST-ATTRIBUTES.json` proves earlier SDK37 source/stub reads
under `/opt/android-sdk/platforms/android-37.0`, not a currently complete usable
compile SDK or required build-tools/AAPT inventory. The old Linux M03 trust
review accepted ordinary installed JDK/SDK/system-library trust for its host
selection; it is not a fresh component-use/license or Android compile admission.

The prior prerequisite note records an existing41-byte android-sdk-license
marker SHA256 `c43fa37686457c3f18caa3607945f4ec52a9d1beaaad8117e50dc4e863270c85`.
Existence is not proof of who accepted or the principal/use covered. Nor does
it prove the SDK is unlicensed. Root should cite applicable existing owner
agreement/use authority for the selected installed compile components; if not
established, request that precise clarification. **Do not fabricate acceptance,
run sdkmanager --licenses, change a marker or silently expand copying rights.**
No new API24 image agreement/installation is inherently needed for compilation.
This distinction preserves the real license gap instead of treating it as proof
that every permitted Android source/configuration task is blocked.

The recorded API35/x86_64-only image is not an Android32 target. The selected
API24/x86 image remains unprepared; this review neither boots an image nor
settles its agreement/containment. Four native cases/four intended KDF calls
remain unexecuted after compilation, as do business create/unlock/password-change/
backup, minified/ARM32 and genuine hardware gates. PVD002 lowercase-hex
compatibility stays required. Do not move this local gap into speculative CI.

## Exact reusable pins

The ten current W files below match raw AND checkout tuples in admitted C16
manifest `2af03198511f26cfafb0ddd01470e66585697507e95c654530e1621b5ce0831b`,
P `e6738b17a7c783383a4f0ae0f17af726cffb9a40`, tree
`c4009ab5f9ba23bb8097d64cc54328b131baa0c8`. Pin equality is not compilation or
whole-file semantic review of every config. Root must rebind any later change.

| Source | SHA256 |
|---|---|
| core/crypto/build.gradle.kts (2805B/90LF) | `a18a2ee9127da9861d4310ab7588573d0596d31c317e3ff393c8e30f70888112` |
| core/crypto/src/androidDeviceTest/AndroidManifest.xml (209B/6LF) | `95a2d24cac877cd0d4fbbfeb58bc10d06275b2c40592244617a83564ec1a06ff` |
| core/crypto/src/androidDeviceTest/kotlin/com/passvault/core/crypto/Android32KdfInstrumentation.kt (14155B/291LF) | `d9bdb00637773f5d239ffcc5f7d13dc3773e528ef0dec1b1efdffec0ee182310` |
| build.gradle.kts | `6c35802a0f779baa1b4d5a65396a07824653761882ddfbfaa718a46c59fa5ee0` |
| settings.gradle.kts | `a2a3336bd1cce66d616c2a49a1773f4d3c93d437759a093ff28e92e4bd646734` |
| gradle/libs.versions.toml | `49775212b92b73197492f36f7c219dbf5d2521b6bbc197c70be50d203c58c57a` |
| gradle.properties | `323971ec6c0f02ff28c724b4c4f3120b5b8843faf3d77608485c1e15fb633235` |
| gradle/wrapper/gradle-wrapper.properties | `245b6dba5960c9c54a8495bf16195fbebe2d91a825238edbde1ac2b858c86f6f` |
| gradle/verification-metadata.xml | `21ab6f9c2873325558f5bb96c7e7f68bfbb674e2a7ce867ffe19dabdae60c299` |
| legal/third-party-dependencies.lock | `252661ae65b520539b6d7a1e301cb1f6de505a17576301e970668b1778d150e2` |

Retained evidence read, relative to B (SHA256):
- `runs/linux-isolated-batch03/logs/ordinary.log`:
  `e79ed666f239eed08e4cfe606fcb1f41b19069301c3b768c40d42ab00aaaca5b`;
  `reviews/android32/ANDROID32-TASK-NAMES-LINUX03.md`:
  `f137ce4405da81cc5d0d8177586e9e6bfa0b8418146b346e721afed9778b7967`.
- `reviews/android32/ANDROID32-ADMISSION-PREREQUISITES.md`:
  `5c2966372abb1aebb56175a7062e4ceef8fa990875a2c6d332c9b136e3397089`.
- `reviews/build-config/ANDROID_API.json`:
  `180651abf43f09da0bb245f27468b8766a66a9f6b170500f45a2629de3f88807`;
  `ANDROID-MANIFEST-ATTRIBUTES.json` in that directory:
  `c5d4be9d7f1d7bcf55a4e8e7813c97bf59c97498527a174a14d1c33079d39a67`.
- `reviews/build-config/AGP94-SDK-DOWNLOAD-SOURCE.json`:
  `f4d9c7d3f596512373a3f00fe2924ca27732ea8e2ebf51b63f0708e2f190c83b`;
  `LINUX-M03-V3-REVIEW.json` there:
  `2754aca02b5223f0ea8cebaabbe0a676b42abd74c1865a38f9c2051f8714d224`.

No build/test/helper import/execution, SDK/store/process/Git/network probe or
application/config edit. Only this new permanent review, no cache/temp/runtime
or persistent worker. Zero cases/findings/fixes/closures/denominator changes;
all STOP/NO-RETRY/CLOSED/HOLD and publication/1017001 restrictions survive.
