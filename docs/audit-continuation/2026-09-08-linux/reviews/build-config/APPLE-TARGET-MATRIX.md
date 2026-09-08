# PVA-008 / PVA-014 Apple target/configuration matrix

Reviewer `/root/build_config`. **SOURCE PREPARATION ONLY; NOT EXECUTED/ADMITTED.**
Semantic fixture review belongs to `/root/native` and is retained in
`../native/APPLE_NEXT_SCOPE.md`; this report does not replace that review.
Root remains sole cross-platform build owner. No Apple workflow is authored here.

## One useful bounded batch

| Finding | Intended target | Exact source class | Declared methods | Current target evidence |
|---|---|---|---:|---|
| PVA-008 | iOS arm64 simulator | `com.passvault.shared.platform.IosAttachmentFileStoreTest` | 7 | None; target-runtime verification remains BLOCKED |
| PVA-014 | same simulator/test binary | `com.passvault.shared.platform.IosBiometricPromptStringsTest` | 1 | None; existing shared/Android/Desktop evidence is preserved separately |

These are **eight source declarations, zero current executions**, not eight
devices or eight independent platform controls. Exact names/hashes are in
`APPLE-TARGET-INPUTS.json`. The first class contains move and forced-copy
adoption cancellation, successful transfer/close, protection-failure/cancelled
admission and immediate picker-copy protection ordering/deletion controls.
The second uses real `LAContext` objects with English/Arabic property assertions,
not an authenticated or displayed prompt. A forced false move return is not a
measured real OS move failure.

## Source-derived compilation/configuration graph

- `shared/build.gradle.kts` declares `iosArm64()` and `iosSimulatorArm64()`;
  there is no x86_64 simulator target, macOS Kotlin target or explicit native
  `testRuns`/`deviceId` configuration. An Intel-only runner cannot substitute
  for the declared arm64 simulator test binary without a separately authorized
  target/configuration change. Linux is not an Apple SDK/runtime substitute.
- `:shared` aggregates seven core API modules and all eight feature modules,
  plus Compose, Navigation3, lifecycle, Koin, Room and coroutines. Main source
  from those modules compiles even when only two test classes execute. Current
  credential edits are consequently part of the Apple compilation freeze.
- `commonTest` adds kotlin.test, coroutine-test, AssertK, Turbine, Koin-test and
  `core:testing`. Both shared `commonTest` and `iosTest` feed the native test
  compilation. A two-class execution filter does **not** remove other test
  sources or their compile dependencies. A limited text scan found no obvious
  eager top-level val/var/object/init declarations in these test directories;
  this is not whole-program initialization or test-discovery safety proof.
- Database applies KSP and Room, including `kspIosSimulatorArm64`; permanent
  `core/database/schemas` is not disposable cleanup output. Crypto configures
  `rawSodium` cinterop with checked-in `passvault_sodium.h`/`rawSodium.def`;
  native dependency/toolchain caches need a fresh confined Konan namespace.
- Root still configures all projects with CoD disabled, so Android plugin/SDK
  and signing-input exclusion remain relevant even to the Apple selection.
  Foojay/settings resolution precedes the task. Do not change dependencies or
  weaken verification to make bootstrap pass. No existing G7/G8 retry follows.

## Exact command boundary still to establish

The conventional candidate task is `:shared:iosSimulatorArm64Test`. This is a
**Kotlin-plugin convention hypothesis**, not an observed realized graph or an
admitted command. Repository source alone does not establish CLI filter syntax,
mapping of the two FQCNs to native runner names, explicit owned simulator UDID,
native runner result decorations or exact XML paths for Kotlin2.4.10.

Before a workflow is written/admitted, bind those contracts from the exact plugin
or separately reviewed narrow configuration. Do not invent a `-P` simulator
property, use a default/first/preexisting simulator, execute all native tests as
a substitute for unproven filtering, or infer success from task exit/listing.
No `xcrun`, `simctl`, native test runner or Gradle command was executed here.

The shared framework is static and sets its existing `com.passvault.shared`
bundle ID. That framework task is not necessary evidence for executing these
native tests. Do **not** use the Xcode application scheme: its build phase calls
the wrapper verifier and `:shared:embedAndSignAppleFrameworkForXcode`, and its
app configurations carry production/debug signing settings and identities.
Xcode project deployment target18.5 is an app setting; it is not proof of the
native test executable's actual minimum OS or simulator architecture.

## Execution/cleanup prerequisites, not authorization

1. Freeze the full committed source and independently review exact commands,
   runner source, eight case/name expectations and output mapping. Use the
   checked-in wrapper/JDK17, one worker, no daemon/CoD/build/configuration cache,
   serial Detekt if separately selected, in-process Kotlin and unchanged strict
   dependency verification. No overlapping Linux/Windows/Apple audit-owned job.
2. Bind actual Apple Silicon host architecture, immutable action revisions,
   observed runner image, JDK/Kotlin/Xcode/SDK and an exact supported installed
   iOS simulator runtime. No speculative matrix or toolchain replacement.
   The Linux directory observations cannot supply this target evidence.
3. Install cleanup/journaling before **any** build/test or simulator creation.
   Use absent private run roots for source outputs, HOME/TMP/Gradle/Konan/XDG;
   identify one newly owned simulator by recorded creation/UDID. No preexisting
   simulator erasure, booting a user's device or global shutdown/delete-all.
4. The selected fixtures use Foundation `NSTemporaryDirectory()` plus UUID
   names, not the host JVM's `java.io.tmpdir`. Bind its effective simulator
   namespace independently before test storage access. A host TMPDIR variable
   alone does not prove this. Preserve precise owned paths needed for cleanup,
   never sweep similarly named preexisting paths or copy user simulator data.
5. Choose a bounded one-shot build/test budget and live resource floors. Native
   compiler/Compose/KSP and simulator storage are materially broader than eight
   method bodies. Retain compact logs, XML and hashes first, then stop the proper
   wrapper, settle owned descendants and the owned simulator, and remove only
   the recorded allowlisted generated outputs/fresh runtime. A cancelled runner
   or failed stop is an explicit incomplete state, not silent retry permission.
6. Independently reconcile eight exact cases once each, no unknown/missing/
   duplicate/skipped/failure/error records, actual commands, source identity and
   cleanup outcome. Compilation, test discovery or provider mocks alone is not
   qualification. Do not upload app binaries/framework archives or sign/publish.

## What even a successful simulator batch cannot close

Physical iPhone picker/cancel/lock/background and real file-protection behavior
remain hardware BLOCKED for PVA-008. Actual selected-language prompt rendering,
biometric authentication and enrollment remain separately BLOCKED for PVA-014.
Existing PVD owner decisions and all STOP/NO-RETRY/CLOSED scopes remain separate.

Do not append the macOS native CTest suite casually. The native reviewer found
its literal `/tmp/passvault-biometric-native-test.XXXXXX` path and early returns
without whole-fixture RAII cleanup. The native reviewer also reported an
`/etc/passwd` symlink target: the current guard rejects before a content read,
but a future synthetic-only fixture should use an owned synthetic target, not
rely on a regression guard to protect a real system file. These need separately
reviewed confinement/
cleanup correction before admission. Windows native14 cannot supply this Apple
evidence, and neither native suite alone proves PVA-010's Kotlin/JNA interleaving
or exact packaged shutdown.

No finding/fix, semantic coverage or closure denominator is changed by this
configuration matrix. It records concrete preparation and remaining gates so a
successor need not repeat the source inventory or silently omit Apple work.
