# PVA-001: Android task-name uncertainty resolved by Linux03

Author `/root/android32`, root-directed retained-log/source reconciliation.
No new execution, SDK/runtime access, package inspection or Android test result.

Linux03 selected `:core:crypto:tasks --all` against C4 commit
`da8ff89b9a8579017d5d524e628dff5251f2bb90`, tree
`cf0a8a702e7cd6948236be18b491bb5a21b5886e`. Its retained ordinary log names the
following tasks; each has prefix `:core:crypto:`:

| Exact task | Log line | Boundary |
| --- | ---: | --- |
| `compileAndroidDeviceTest` | 228 | Device-test compilation, not execution |
| `assembleAndroidDeviceTest` | 217 | Device-test output assembly |
| `packageAndroidDeviceTest` | 311 | Package task exists; dependency edges unproved |
| `connectedAndroidDeviceTest` | 156 | Installs/runs tests; not package-only |
| `connectedAndroidTest` | 157 | Connected aggregate also exists; not a substitute |
| `testAndroidHostTest` | 209 | Host-JVM test, not Android32 native evidence |

These names agree with the existing module/root wiring. The task-name gap in
`ANDROID32-TASK-GRAPH-NEXT-ACTION.md` and the final source review is now resolved;
**do not repeat discovery just to recover these names.** Historical reports
remain unchanged. The realized selected dependency graph, compiled harness,
merged manifest/test identity, resolved classpath and APK/ELF contents remain
unverified. A task listing proves neither edges nor package success.

The earlier plan's blanket exclusion of "signing" actions was over-broad. The
log registers `signingConfigWriterAndroidDeviceTest` (332),
`validateSigningAndroidDeviceTest` (345) and
`writeAndroidDeviceTestSigningConfigVersions` (346); registration does not prove
their inclusion in a selected graph or private-signing access. Root clarified:
production signing/private signing material remain prohibited. Ordinary
synthetic debug/test signing in a fresh isolated runtime is not a separate
product/Store owner decision, but still needs exact graph, credential-free
execution and cleanup admission. No signing guard may be silently bypassed.

## Exact retained inputs — SHA-256

`B` = `docs/audit-continuation/2026-09-08-linux`.

- `B/runs/linux-isolated-batch03/logs/ordinary.log`:
  `e79ed666f239eed08e4cfe606fcb1f41b19069301c3b768c40d42ab00aaaca5b`
- `B/reviews/current-cycle/SOURCE.json`:
  `2e8b82c91215bbfee9cf4a2a3f09bad386fc8515d7fd20263fbbfa5fe1217003`
- `core/crypto/build.gradle.kts`:
  `a18a2ee9127da9861d4310ab7588573d0596d31c317e3ff393c8e30f70888112`
- `build.gradle.kts`:
  `6c35802a0f779baa1b4d5a65396a07824653761882ddfbfaa718a46c59fa5ee0`
- `B/reviews/build-config/ANDROID-FINAL-REVIEW.md`:
  `0d4f1f7a26c6c119737a4aa265f5b3f7c3b268f56c74ff561ec561653f12a319`

SDK license scope and a usable32-bit target remain blocked. This note supplies
no license, target, package, execution or cleanup authority, and zero Android
cases/closures. No catalog/source recapture or production/request edit occurred.
