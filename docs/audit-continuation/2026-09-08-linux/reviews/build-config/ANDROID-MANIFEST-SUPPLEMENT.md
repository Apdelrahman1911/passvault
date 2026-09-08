# PVA-001: independent test-only ABI manifest supplement

Reviewer `/root/build_config`; author `/root/android32`.
**ACCEPT_BOUNDED_SOURCE_PATCH; NOT_EXECUTED/NOT_ADMITTED.**

Root separately authorized this third file after the retained two-file final
review. `ANDROID-FINAL-REVIEW.md` and its original input packet remain the
historical two-file disposition, not retroactively a three-file review.

Reviewed in full:
`core/crypto/src/androidDeviceTest/AndroidManifest.xml`,209bytes/6physicalLF,
SHA-256 `95a2d24cac877cd0d4fbbfeb58bc10d06275b2c40592244617a83564ec1a06ff`.

The XML contains only the Android namespace and a test-source-set application
element setting `android:multiArch="true"` and `android:use32bitAbi="true"`.
There is no package, applicationId, instrumentation target, signing, export,
network, storage or production-application identity override.

Independent supported-attribute evidence is retained in
`ANDROID-MANIFEST-ATTRIBUTES.json`: installed SDK37 `android/R.java` declares
public `multiArch=16843918` and `use32bitAbi=16844053`; `attrs_manifest.xml`
documents32-bit preference **in a multiArch application**, with an explicit
install-ABI override taking precedence. This is why a use32bitAbi-only proposal
was not treated as sufficient. Both attributes are now present in the exact
afterimage and within root's narrowed test-only authorization.

The original final files were independently rehashed unchanged:

- `core/crypto/build.gradle.kts`:
  `a18a2ee9127da9861d4310ab7588573d0596d31c317e3ff393c8e30f70888112`.
- `Android32KdfInstrumentation.kt`:
  `d9bdb00637773f5d239ffcc5f7d13dc3773e528ef0dec1b1efdffec0ee182310`.
- Production `app-android/src/main/AndroidManifest.xml` was not edited by this
  patch; observed hash remains
  `5bd74cede2d0328c76ca7ba6ea1876df2491267d44077d54bce25df40226ab4b`.

These are point-in-time file hashes, not a frozen full-tree or merged-APK claim.
AGP9.4's generated test package/target, manifest merge, AAPT acceptance and actual
installed ABI still require evidence. A manifest in `androidDeviceTest` must
not be assumed to change a separately targeted production app's ABI. Keep
source-set isolation and inspect the exact generated manifest/package before
any independently admitted installation.

This preference cannot create32-bit support in a64-bit-only Android image and
cannot override an explicit conflicting install ABI. The harness's mandatory
process-is64Bit/pointer/size_t checks remain unchanged and fail closed. Current
installed image suitability is documented by the Android author; no guest boot,
catalog download, emulator/ADB operation or APK installation occurred here.

No application test compiled or executed. No dependencies, versions, protected
refs or production IDs changed. PVA-001 remains open/target BLOCKED, with zero
new closure credit. All two-file review limits, separate PVD decisions and
STOP/NO-RETRY/CLOSED scopes continue to apply.
