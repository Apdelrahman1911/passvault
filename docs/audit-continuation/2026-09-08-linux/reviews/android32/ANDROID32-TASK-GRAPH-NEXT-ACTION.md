# Android32: source graph boundary and smallest next action

Author `/root/android32`; source-only continuation of the existing prerequisite
report. **No task discovery, build, package, device operation or admission.**
Observed C4 HEAD `da8ff89b9a8579017d5d524e628dff5251f2bb90`, tree
`cf0a8a702e7cd6948236be18b491bb5a21b5886e`. Root owns execution and independent
challenge. PVA-001 remains open; no application case or closure credit changes.

## What the checked-in wiring establishes

- The narrow module is `:core:crypto`, using Kotlin2.4.10 and AGP9.4.0's
  `com.android.kotlin.multiplatform.library`, not `:app-android`. Its
  `android { withDeviceTestBuilder ... }` selects
  `com.passvault.core.crypto.Android32KdfInstrumentation`. SDK compile37/min24,
  JVM target17 and wrapper9.7.1 remain unchanged.
- `sourceSetTreeName=null` excludes inferred common test source trees. The
  retained official9.4 API excerpt supports this; no new JUnit/AndroidX runner
  dependency or `:core:testing` device dependency was added. Device compilation
  still needs its actual resolved main/runtime dependency graph checked.
- `execution="HOST"` disables **on-device orchestration**; it does not turn
  these device tests into host-JVM tests. Conversely, `androidHostTest` adds
  the JVM sodium artifact and cannot establish the Android32 native result.
- Root `test` explicitly depends on Desktop/JVM and Android **host** tests,
  version and localization checks (`build.gradle.kts:498–562`), not this device
  harness. Root `check` additionally aggregates Detekt/dependency/legal and
  subproject checks (`564–581`). Neither is the narrow package-only selector.
- The root legal-inventory block creates providers for app release/Desktop/iOS
  configurations (`415–453`). The inspected root/module source adds no edge
  from the device-test compilation to those inventory or release tasks. This
  is **not** proof of the realized plugin graph or absence of configuration-time
  work. Configure-on-demand stays disabled: all-project plugin/SDK configuration
  can still fail or consume resources before any selected task runs.
- The manifest requests `multiArch=true` and `use32bitAbi=true`; it supplies no
  test application ID or instrumentation targetPackage. Those flags do not
  prove a32-bit process. Actual merged manifest/package identity, runner DEX,
  resolved AAR/JNA/native entries and tested APK hashes remain package evidence.
- The existing app Debug resource verifier packages the **application**, not
  this library harness. Its six-library inventory is not automatically this
  APK's inventory. Release/signing guards and occupied1017001 stay untouched.
- The fixed runner exposes four cases, three KDF cases/four intended KDF calls.
  Only inert `additionalTestOutputDir` metadata is accepted; no class/filter/
  shard/list arguments should be added. Package success is zero executed cases.

## Exact generated task names: still an explicit gap

Do **not** substitute guessed `assembleAndroidDeviceTest`,
`packageAndroidDeviceTest`, `connectedAndroidDeviceTest`, APK paths or test IDs.
The generic API-doc `connectedAndroidTest` example is not this realized KMP
task graph. The recorded raw-source locator404 is not repaired or retried here.

The previously obtained official AGP9.4 sources JAR was read only in memory and
released. `/root/build_config` confirmed that no local copy/task-registration
excerpt was retained by that lane; `LINUX-RUNNER-CONFIG-REVIEW.md:116–120` records
the same retention. `AGP94-SDK-DOWNLOAD-SOURCE.json` preserves SDK-option/service
excerpts, not KMP device-task registration. A focused tracked-name lookup found
no matching Gradle-source JAR/task-manager/task-graph input; this is not an
exhaustive host-cache absence claim. No retrieval or repeat cache/SDK survey ran.

## Smallest prospective root-owned action

One **separately admitted configuration-discovery invocation**, not a new
framework or an automatic package/test chain. The task selector is exactly
`:core:crypto:tasks --all`. Prospective wrapper argv, **not execution authority**:

```text
./gradlew :core:crypto:tasks --all --console=plain --offline
  --no-daemon --max-workers=1 --no-parallel --no-configure-on-demand
  --no-configuration-cache --no-build-cache --dependency-verification=strict
  -Pkotlin.compiler.execution.strategy=in-process
  -Pandroid.builder.sdkDownload=false
  -Dorg.gradle.java.installations.auto-detect=false
  -Dorg.gradle.java.installations.auto-download=false
```

Root must first bind exact selected source, JDK17, existing pinned wrapper
distribution, isolated environment/storage, sole execution slot, memory/disk/
time bounds and reviewed cleanup. `--offline` does not prevent wrapper bootstrap
downloads or arbitrary plugin network access; do not use it as a sandbox claim
or automatically retry online. The SDK option disables the source-reviewed
automatic-download path, not arbitrary SDK writes. Do not reuse/import the old
runner or extend the database runner's scope by copying this argv into it.

Reuse prerequisite report§4's proposed discovery ceiling (5min plus separately
bounded2min stop/cleanup), not an unbounded `tasks` run. Preserve compact task
output/failure, run this wrapper's `--stop`, settle owned workers, then clean
only admitted generated paths; no shared-cache/toolchain deletion. No device,
ADB, emulator, license or download command is part of this selection.

From the result, freeze the actual **package-only** task names and dependency
graph before separately admitting a package invocation. Task listing alone may
not expose every edge: an admitted dry-run/graph observation may still be needed;
it too executes configuration and must not auto-chain a build. Require absence
of install/connected/device/release/signing/store/publish actions in that selected
graph. Then inspect the merged manifest, runner and actual native/runtime APK
contents. Only a later separately admitted isolated Android32 run can reconcile
the four exact native case results. Android progress need not wait for database
admission, only its own admission and root's sole slot. All prior STOP/NO-RETRY,
PVA-029, G7/G8 and physical-hardware limitations remain unchanged.

## Exact source inputs (SHA-256)

Paths are repository-relative; `B/` means
`docs/audit-continuation/2026-09-08-linux/reviews/build-config/`.

| Input | SHA-256 |
| --- | --- |
| `build.gradle.kts` | `6c35802a0f779baa1b4d5a65396a07824653761882ddfbfaa718a46c59fa5ee0` |
| `settings.gradle.kts` | `a2a3336bd1cce66d616c2a49a1773f4d3c93d437759a093ff28e92e4bd646734` |
| `gradle.properties` | `323971ec6c0f02ff28c724b4c4f3120b5b8843faf3d77608485c1e15fb633235` |
| `gradle/libs.versions.toml` | `49775212b92b73197492f36f7c219dbf5d2521b6bbc197c70be50d203c58c57a` |
| `gradle/wrapper/gradle-wrapper.properties` | `245b6dba5960c9c54a8495bf16195fbebe2d91a825238edbde1ac2b858c86f6f` |
| `core/crypto/build.gradle.kts` | `a18a2ee9127da9861d4310ab7588573d0596d31c317e3ff393c8e30f70888112` |
| `core/crypto/src/androidDeviceTest/AndroidManifest.xml` | `95a2d24cac877cd0d4fbbfeb58bc10d06275b2c40592244617a83564ec1a06ff` |
| `core/crypto/src/androidDeviceTest/kotlin/com/passvault/core/crypto/Android32KdfInstrumentation.kt` | `d9bdb00637773f5d239ffcc5f7d13dc3773e528ef0dec1b1efdffec0ee182310` |
| `B/ANDROID_API.json` | `180651abf43f09da0bb245f27468b8766a66a9f6b170500f45a2629de3f88807` |
| `B/AGP94-SDK-DOWNLOAD-SOURCE.json` | `f4d9c7d3f596512373a3f00fe2924ca27732ea8e2ebf51b63f0708e2f190c83b` |
| `B/LINUX-RUNNER-CONFIG-REVIEW.md` | `ed793874d6377ac6e5963d55a0a83ce0744b491df11e9d477a1d36e5c158a699` |

Only this permanent report was created. No scratch/cache/build/device output,
background worker, SDK modification or wrapper-stop obligation arose here.
