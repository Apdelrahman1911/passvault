# Android compile01 — bounded source-only next-batch proposal

Author `/root/editor_review`, 2026-09-10. **PROPOSAL ONLY; independent challenge and root's
fresh source/graph/instance/coordination/cleanup admission required. No runner implementation.**
B = `docs/audit-continuation/2026-09-08-linux`. Root alone owns execution and publication.

## What can advance without a device

| Track | Local compile/package evidence | Separate runtime limitation |
|---|---|---|
| PVA-001 core KDF harness | Compile its actual device-test source/classpath; assemble and inspect its test APK. Exact selectors are already recorded below. | Needs a genuinely32-bit Android process for four native cases; image preparation, ABI/installation and process proof remain separate. |
| PVA-009/PVA-030 app clipboard/locale fixture | Source/wiring exists, but no exact generated app androidTest selector/output identity was established in these inputs. Keep this gap visible rather than invent a task. | Requires an admitted synthetic API29+ app/device/clipboard environment, not specifically32-bit. The proposed API24/x86 KDF image would not satisfy this fixture's API29 guard. |

No emulator, system-image download or new license-acceptance command is inherently needed
for compilation using already-admitted installed tool inputs. The unresolved new-image
agreement/copying question is not evidence that all source/configuration/package work is
barred. Conversely this note does not prove installed SDK availability or the scope of its
existing use authority. Bind that component-specific scope; request precise owner clarification
if missing, never manufacture acceptance. No SDK/license/target probe was performed here.

## Smallest useful single build selection

Use only the already-recorded selector vector:

`[:core:crypto:compileAndroidDeviceTest, :core:crypto:assembleAndroidDeviceTest]`

This is one prospective wrapper invocation, not two jobs or permission to run now. Reuse
JDK17, wrapper9.7.1 and the accepted static02 serial/strict FLAGS contract; omit its Detekt-only
`--continue`. Keep one worker, no daemon/parallel/CoD/configuration-cache/build-cache,
in-process Kotlin, strict dependency verification, SDK auto-download=false and Java toolchain
auto-detect/download=false. Do not rerun `tasks --all` or choose root test/check.

Linux03 established these names and packageAndroidDeviceTest's registration, **not their
realized dependency edges**. A new fixed init must reject any graph not independently admitted
before task actions. The missing graph is local admission work, not a device/license fact;
an unexpected graph can yield bounded diagnostic metadata and STOP, never automatic graph
expansion/retry or an automatic discovery→build chain. No expensive app task-only cycle is
proposed. Source-registered `:app-android:verifyDebugComposeResources`/assembleDebug packages
the ordinary app, not either instrumentation selection; adding it now would conceal the gap.

## Required evidence — package success is zero executed cases

1. Bind full frozen source/member/EOL identity, commands, actual task graph/types/statuses,
   device-test compiler inputs and resolved component/artifact hashes. Prove compilation of
   Android32KdfInstrumentation, not merely a task listing or NO-SOURCE. Preserve sourceSetTreeName
   null: no commonTest/JUnit/core:testing test-tree substitution. Record actual Android runtime
   JNA versus compileOnly5.19.1; historical runtime5.18.1 is context, not a fresh resolution.
   Do not change dependencies or verification metadata to make resolution succeed.
2. Obtain APK and merged-manifest locations from admitted task/artifact output metadata,
   never guessed filenames. Bind stable regular files below the disposable output allowlist,
   their sizes and full SHA256; reject missing/ambiguous/unsafe artifacts. Final binary manifest
   must name `com.passvault.core.crypto.Android32KdfInstrumentation`; record actual generated
   test package and targetPackage, their relationship and test/debug signing identity. These
   package IDs are not supplied by the six-line source manifest and are not invented here.
   Verify packaged multiArch/use32bitAbi and runner DEX class presence. Merged XML alone does
   not prove final APK contents; an exact existing binary-manifest/DEX inspection tool/parser
   still needs binding/admission. None is silently substituted or assumed installed.
3. Inventory/hash native entries and their resolved AAR provenance, accounting for any build
   transform. Specifically require genuine32-bit libsodium and jnidispatch entries for the ABI
   being credited: ELFCLASS32/little-endian/ET_DYN with EM_ARM for armeabi-v7a, EM_386 for x86.
   Record all other ABIs/entries; flags/directory labels alone are insufficient. Do not import
   the app verifier's six-library/four-ABI equality into this library test APK. An absent ABI
   is an explicit package-evidence gap, not permission to filter/change dependencies. ELF
   headers do not establish ARMv7 features, successful loading, size_t width or KDF correctness.
4. Retain compact graph/classpath/manifest/class/native inventories, hashes, ordinary logs and
   failure/cleanup receipts. No synthetic test XML, native-call count or closure credit. Core's
   four cases/four intended KDF calls remain unexecuted; the app's two cases are not selected.
   Business create/unlock/change/backup, minification and genuine hardware claims stay separate.

## Minimal future delta, not a new generic framework

Reuse the source-accepted static02 ownership/transport/Files/pidfd/mount/stop/cleanup mechanics,
not its execution identity or static acceptance. Both current runners remain UNBOUND and its
55-line init deliberately rejects packaging. A separately reviewed fixed successor needs only:

- Fresh namespace and frozen inputs; replace selector/init and Detekt-specific phase/mapping
  expectations. Admit exact compile/resource/manifest/dex/package edges, never connected,
  install/uninstall, emulator/device-provider, license, release/store/publish or unrelated tests.
  Do not blanket-reject the text DeviceTest or every signing task: ordinary synthetic test/debug
  signing may be necessary, with fresh private credentials only; production/private signing
  remains forbidden. No Store version/identity change or candidate1017001 replacement.
- Replace the analyzer report collector with a fixed bounded artifact collector. Reuse stable
  descriptors, initial-absence versus read-drift distinction and retention-before-semantics.
  Stream APK/native hashes; do not load/extract/upload a whole APK into evidence. Explicitly
  bind the binary-manifest/DEX reader, exact output allowlist and file/byte accounting.
- Retain original wrapper --stop in finally, owned worker/namespace settlement, source-before/
  after and original-directory/mount checks. Archive compact evidence before validated deletion;
  uncertainty remains HOLD, not a cleanup retry. Remove only owned generated outputs/private
  caches/temp/debug-key material after settlement, never shared caches/SDKs/source/tests/reports.
  Keep an APK only for a concretely imminent admitted target use; otherwise retain hashes and
  compact content evidence and remove its disposable copy. No large binary publication.

Static02 FLAGS do **not** contain --offline, and mount/PID isolation is not network isolation.
No network probe/download step is proposed. If the prospective batch must be offline, add the
explicit flag only with a separately bound existing wrapper/dependency seed; fresh private
GRADLE_USER_HOME does not prove cache availability, and --offline does not block wrapper or
arbitrary plugin network access. Missing seed/tool/dependency facts must stop or receive their
own narrowly reviewed admission, never an automatic online/SDK-install/license fallback.

For review, bound work to900s, preserve the existing600s original-stop allowance, and budget
300s for remaining preservation/cleanup (1800s total); no automatic enlargement/retry. Aim
at <=4GiB aggregate owned RSS and <=4GiB new disk, <=16MiB logs/32MiB retained evidence. These
are proposed ceilings, not enforcement supplied by heap flags: exact metering and APK/member
caps must be admitted. Retain base launch disk>=12GiB plus the new allocation reservation and
RAM>=25% available; running floors remain disk>=8GiB/RAM>=20%. Root checks current resources
and owns the single local/CI slot. No resource/process probes or build obligation arose here.

## Exact source/data bindings (SHA256)

Focused source/text reads only; some combined output was truncated, so no whole-project,
whole-SDK or whole-runner audit is claimed. Current fixture source was rebound to the preceding
independent refactor review; older task-name uncertainty is superseded only by Linux03's note.

| Input | SHA256 |
|---|---|
| B/reviews/android32/ANDROID32-TASK-NAMES-LINUX03.md | `f137ce4405da81cc5d0d8177586e9e6bfa0b8418146b346e721afed9778b7967` |
| Same directory ANDROID32-ADMISSION-PREREQUISITES.md | `5c2966372abb1aebb56175a7062e4ceef8fa990875a2c6d332c9b136e3397089` |
| core/crypto/build.gradle.kts | `a18a2ee9127da9861d4310ab7588573d0596d31c317e3ff393c8e30f70888112` |
| core/crypto/src/androidDeviceTest/AndroidManifest.xml | `95a2d24cac877cd0d4fbbfeb58bc10d06275b2c40592244617a83564ec1a06ff` |
| core KDF harness (full source path in section1's module) | `d9bdb00637773f5d239ffcc5f7d13dc3773e528ef0dec1b1efdffec0ee182310` |
| app-android/build.gradle.kts | `c7a382773cb11383d583b6fd1dcead85aac30ccc0a38f81995f535dc1029da42` |
| app clipboard/locale fixture (preceding independent review's exact path) | `580c71e5fc5c917b7f62535282fa3436a3eada15e35ac7afa3bcd584f890c29d` |
| gradle/libs.versions.toml | `49775212b92b73197492f36f7c219dbf5d2521b6bbc197c70be50d203c58c57a` |
| gradle.properties | `323971ec6c0f02ff28c724b4c4f3120b5b8843faf3d77608485c1e15fb633235` |
| gradle/wrapper/gradle-wrapper.properties | `245b6dba5960c9c54a8495bf16195fbebe2d91a825238edbde1ac2b858c86f6f` |
| scripts/audit/linux_detekt_02.py | `e5b2352080d4eafc2d22c3a50a7d3f4b31f4b324672cab1e59bdef8b91a2d294` |
| scripts/audit/detekt_02.init.gradle | `2ff75e0d26f4bd2d7bca7a0c62d929fed9174115ad20b665e5b10caabe51b9e6` |
| B/reviews/detekt02-outer/LAUNCH.py | `545de8da07253be6153533d8e97b583aab3b1c78f9d39f0c399d277509d4ce3a` |
| B/reviews/editor/detekt02/SUCCESSOR-INDEPENDENT.md | `a47d10edbfa41f0775192c82f27d26f42515933b1dea2b602ec80859ae004857` |
| B/reviews/verification/LINUX-DETEKT02-OUTER-DELTA-REVIEW.md | `0afae9eb0de75f840b4530d4e8f330f5397b38c6b27d8190a89e93033247bb55` |

No executable/module/C16 selected input was edited. No helper was imported/executed, no Git,
SDK/store/runtime/process/build/CI/network/target operation occurred, and no cache/worker/
temporary runtime output was created. Only this sealed proposal. Zero findings, fixes, cases
or denominator changes. All STOP/NO-RETRY/CLOSED/HOLD/consumed restrictions, PVU-007/PVU-011/
PVA-029 and G7/G8, PVD/hardware and non-publishing boundaries remain unchanged.
