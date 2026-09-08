# PVA-001 Android32 instrumentation proposal challenge

Reviewer `/root/build_config`; author `/root/android32`.
**ACCEPT_BOUNDED_SOURCE_PROPOSAL; FINAL_PATCH_REVIEW_PENDING; NOT_EXECUTED/NOT_ADMITTED.**

This is the retained proposal-stage disposition. The subsequent two-file final
patch was independently accepted **as source only** in
`ANDROID-FINAL-REVIEW.md`, bound by `ANDROID-FINAL-INPUTS.json`. Its compile,
target execution and cleanup admission remain pending; this proposal record is
not retrospectively changed into test evidence.

## Rejected original proposal

Do not connect device tests to `commonTest` using `sourceSetTreeName="test"`.
That includes all common crypto test sources and their fake/test dependencies,
not just the three selected Argon2 methods. Numerous backtick/space method names
also require an unproven DEX/minSdk compatibility check. Even a hardcoded runtime
selection would not restrict compilation. The original placement of
`sourceSetTreeName` directly in `withDeviceTest` was not API-supported here.

## Accepted narrower configuration

```kotlin
withDeviceTestBuilder {
    sourceSetTreeName = null
}.configure {
    instrumentationRunner = "com.passvault.core.crypto.Android32KdfInstrumentation"
    execution = "HOST"
}
```

Official AGP9.4 API evidence in `ANDROID_API.json` confirms the builder accepts
nullable `sourceSetTreeName` and explicitly specifies that null excludes all
common test source sets. The target returns
`HasConfigurableValue<KotlinMultiplatformAndroidDeviceTest>`; the official plugin
overview uses builder/configure chaining. DeviceTest's runner property accepts
a fully qualified class name; HOST disables on-device orchestration. Do not
create the same device compilation again with a second `withDeviceTest` call.

These are public API contracts, not verification of the downloaded plugin's
bytecode or realized Gradle graph. Generated task names/manifest/source wiring,
Kotlin compilation and Android runtime execution remain pending. Expected source
directory `src/androidDeviceTest` is documented by the official overview.

## Accepted harness bounds

- Platform `android.app.Instrumentation`, not a made-up JUnit runner name; no
  new dependencies, versions, production application identities or metadata.
- Four fixed camelCase case identifiers. First is a native ABI admission case;
  the remaining three cover binary-password, text-password and two production
  profiles. Four successful cases would contain **four actual KDF calls**, not
  four independent devices/ABIs or the entire existing common test suite.
- Reject a 64-bit process and non-four-byte pointer/size_t. The existing host
  JVM fake path cannot substitute. Run production `LibsodiumCryptoEngine`, whose
  Android actual implementation invokes the JNA-backed `crypto_pwhash`.
- Copy/pin the four existing vector constants and upstream reference provenance
  rather than silently changing historical lowercase-hex password encoding.
  A copied expected value is not a newly regenerated external oracle.
- No user data, application startup, storage, clipboard, provider/prompt or
  key-store behavior. Only synthetic constant inputs and promptly cleared owned
  arrays; memory-erasure claims remain bounded by PVD-003.
- Exact standard instrumentation start/pass/assertion-failure/error records,
  fixed class/method/count fields, and failure-aware terminal status. ABI failure
  must prevent KDF execution, not silently select a64-bit fallback. Missing,
  aborted, duplicated or mismatched cases remain failure/unexecuted evidence.
- Refuse arbitrary test discovery/filters and do not launch an orchestrator.
  Driver/XML parsing needs separate reviewed result mapping and exact case
  equality; successful `am instrument` shell exit alone never establishes pass.

## Still required before qualification

Independent final-diff review, compile/manifest/package identity evidence,
strict dependency verification and real Android target execution. Confirm the
loaded packaged JNA/libsodium ABI and both runtime widths. Keep ARM32 and x86
outcomes distinct. Require existing KDF/reference compatibility plus candidate
source identity; no test renaming alters the original PVA-001 denominator.

Root must independently admit the exact one-shot build/device commands,
isolated test application/storage, bounds, process/resource coordination,
instrumentation result mapping and success/failure/cancellation cleanup. No
device/ADB/emulator or build is currently admitted. No physical security claim
or product closure follows from adding this harness.
