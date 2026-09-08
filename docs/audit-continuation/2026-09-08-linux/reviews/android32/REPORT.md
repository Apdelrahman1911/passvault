# PVA-001 — Linux continuation of the Android32 KDF investigation

Author/reviewer: `/root/android32`; initial source inspection through 2026-09-08T20:44:45Z;
bounded harness/manifest implementation, review and catalog-only follow-up through 2026-09-08T22:25:31Z.
This reviewer did not author the delivered adapter correction. Root remains the
sole build/test/commit/push owner. **No application, compiler, Gradle, emulator,
adb or regression test was run. PVA-001 remains TARGET RUNTIME BLOCKED.**

## Identity and inherited evidence

Worktree: `/root/projects/PassVault/passvault-linux`, continuation branch
`codex/audit-continuation-linux-20260908`. At inspection, HEAD was handoff commit
`9bdf9559b7a801a6f2fa49be3d7836ea6ef4d0ed`, tree
`05014e9f635131d5db06701e4013b4b5a746465a`; continuation reports were untracked.
The source identities below bind the inspected bytes, not runtime correctness.
Root owns the separate refreshed-remote receipt and final continuation identity.
The two PowerShell checkout-EOL qualifications are unchanged and do not affect
these Kotlin/C sources.

The manifest follow-up observed continuation HEAD
`488ab465125234e02bfbd28b3e7579251341cb54`, Git tree
`4f4c594ff19e62e19f641304b95eb6c086781ae7`. The harness/manifest after-images
below were still worktree changes, not part of that commit/tree. Selected-file
hashes do not freeze other agents' work or establish a reviewed whole tree.

Read handoff, publication qualifications, current issue/coverage/verification
ledgers, next-regression prerequisites, runner disposition and the PVD decisions.
Applied the supplied production-readiness, backup/blob and mobile-identity skill
snapshots. No unavailable skill script was substituted or executed.

Continued, rather than replacing, these original PVA-001 records from the
read-only evidence packs (streamed selected members to stdout, never extracted
or imported as code):

| Historical object | SHA-256 | Scope reread here |
| --- | --- | --- |
| `audit-reports/20260905T083114Z/findings/PVA-001.md` | `5d19a5987949bfcfc4d662439bd58906abd6d68cad537b7a62c6f913b9508163` | Full original reachability/ABI finding |
| `remediation-reports/20260905T222925Z/reviews/PVA-001-independent-platform.json` | `6ab4675ac0c4fdfd6960dd187d3522620374032107a61ce6ccd6b079b64dc7d6` | Full original independent source acceptance and limits |
| `remediation-reports/20260905T222925Z/reviews/PVA-001-executed-evidence-independent-platform.json` | `03f23c0d28c5ac7bce1ba1f19d1bfa52ff8df3415ba47f68d627b1ca4ae20371` | Selected disposition/scope fields only; no fresh XML reconciliation |

Preserve the historical four JVM selector passes and macOS64 native comparison:
the latter's 22 cases included eight Argon2 methods with four actual native KDF
calls, not 22 KDF cases. The selector run's original overall stop-related failure
and separate later stop confirmation are not rewritten. Neither host evidence
establishes Android32 behavior; old code was already width-correct on macOS64.

## Independent source challenge — correction sustained, no new product defect

1. **Reachable production path.** `AppModule.kt:67–71` binds the shared
   `LibsodiumCryptoEngine`, not a fake or unused Android facade. Vault creation
   (`VaultRepositoryImpl.kt:136–198`), password unwrap (`675–700`), legacy backup
   (`VaultBackupService.kt:455–545`) and V2 writer/reader (`BackupV2Codec.kt:99–143,
   377–423`) reach production `deriveKey`. A valid 32-bit installation is enough;
   an attacker or malformed input is unnecessary. Source declares armeabi-v7a
   and x86 as well as the two 64-bit Android ABIs.
2. **Surrounding guards do not remove the original width defect.** Unlock checks
   persisted metadata and resource limits before KDF work (`224–278,741–766`).
   The crypto engine checks salt16, ops1–10 and memory8KiB–1GiB before the native
   call. Valid production 64MiB profiles still reach it. Benchmark fallback only
   chooses parameters; it cannot make a later mandatory derivation succeed.
3. **Exactly one argument changes width.** Current Android/Desktop raw adapters
   pass boxed `Long` for `outlen`, password length and `opslimit`; only `memlimit`
   selects `Int` for `Native.SIZE_T_SIZE == 4` or `Long` for 8. The final algorithm
   remains `Int`. Repository C declaration agrees with unsigned64/size_t/int.
   Saved JNA5.18.1 `dispatch.c:561–569,2838–2846` maps Integer/Long to ffi32/ffi64
   and computes size_t separately from native long. This inspection concerns
   the retained primary-source capture, not newly downloaded runtime bytes.
4. **Signedness is not a contrary case for the supported range.** All admitted
   memory values fit a nonnegative Kotlin Int. The same bits represent size_t
   without sign-bit ambiguity. This is not support for allocations above
   `Int.MAX_VALUE`, and there is no reason to widen the product's KDF bounds.
5. **LLP64 counterexample rejects `NativeLong`.** Windows64 can have native
   long32 while size_t64. Retaining the actual SIZE_T discriminator is correct;
   changing every native integer to pointer width would break unsigned64 args.
6. **The existing loader is reused.** `passwordHashFunction()` obtains
   `LibsodiumInitializer.sodiumJna`'s `Library.Handler`, then the already-loaded
   native library's `crypto_pwhash`. Saved IonSpin initializer source calls
   Android `Native.load("sodium", JnaLibsodiumInterface::class.java)` without a
   TypeMapper. The adapter does not call the upstream interface's still-Long
   memory parameter. A hypothetical unexpected non-proxy/handler fails closed;
   it is not evidence that Android actually loads the expected packaged bytes.
7. **Compatibility is unchanged.** `SensitiveText.toUtf8ByteArray()` is strict;
   `deriveKey` then calls `withLowercaseHexBytes`. Byte order, ASCII lowercase,
   leading zeros, arbitrary high bytes and embedded zero bytes survive. Owned
   temporary hex/native/derived buffers have finally cleanup. PVD-002 remains a
   required compatibility contract, not a proposed redesign or extra entropy.
   `benchmarkArgon2`'s separate fixed, nonpersistent timing password is not the
   persisted password-encoding path.
8. **Failure stays fail-closed.** Saved libsodium1.0.20 `crypto_pwhash.c:128–145`
   rejects an unsupported algorithm. The original Android32 high zero word of
   positive memlimit would occupy that argument under the old layout. No
   plaintext exposure or successful wrong-key derivation is claimed. Current
   source clears unsuccessful derived output, but no forensic erasure claim
   follows from finally clauses.

Rejected overclaims: the selector alone does not prove call-site wiring; an
Android **host** test is not Android native execution; a supported ABI list is
not proof of a tested ABI; debug success is not minified/release package proof;
64-bit Linux/macOS tests cannot expose this specific 32-bit layout error.

Saved primary-source inputs inspected in bounded ranges:

| Capture under `audit-reports/20260905T083114Z/` | SHA-256 | Inspected evidence |
| --- | --- | --- |
| `research/crypto-independent/source-androidMain_com_ionspin_kotlin_crypto_LibsodiumInitializer.kt` | `d3cfd9535d177812ad5de37743ab43f5658335b382d1b543b0076c526510d5b5` | Full loader source |
| `research/crypto-independent/jna-5.18.1-dispatch.c` | `270300a79e0520a565a523e3aed3d173f44f4a1c088ec97332569d9360d612b5` | Integer/Long dispatch and SIZE_T sizeof ranges; not whole-file rereview |
| `research/crypto-independent/ionspin-JnaLibsodiumInterface.kt` | `2a8378a4606615873505d03ce583075358e258253509a6780c786fd7489bbe9d` | `crypto_pwhash` signature around1084–1097 only |
| `evidence/audit_crypto/libsodium-1.0.20-crypto_pwhash.c` | `3afb82d3775317d2a551fe9ac8a6e864461e3696b7893edca234dd8fbad96523` | `crypto_pwhash` algorithm dispatch; not loaded-version evidence |

## Test and target facts

At the inspected handoff identity, `core/crypto/build.gradle.kts` enables only
`withHostTest`. It adds the **JVM** libsodium runtime to `androidHostTest`.
There is no device-test source set or instrumentation runner in the module;
app-android also has no instrumented-test setup. Therefore merely putting a
test in an arbitrary Android directory would not make it execute on Android.
The current version catalog and verification metadata distinguish compile-only
JNA5.19.1 from the historical Android AAR/runtime JNA5.18.1. A current resolved
dependency graph and loaded version must still be captured at admission.

The existing `Argon2Test` has eight declared methods, only three of which do
native derivation (four calls):

| Existing native method | Inputs/profile | Expected key |
| --- | --- | --- |
| `production Argon2 preserves the historical lowercase hex input` | bytes00ff01, salt00–0f, ops1/memory8KiB | `2bc5a714c8397bb9e89e70c957231bd3cd4f0b4ffe32bb6ca1f5067196b60b15` |
| `production Argon2 matches the established text password vector` | TestPassword123!, salt00–0f, ops1/memory8KiB | `4c1422bcf6ea79ca8b843170ffbaf713f854f1b97e4fd0df07d741a650cacab7` |
| `production profiles match independent Argon2id reference vectors` | TestPassword123!, salt0123456789abcdef, ops3/memory64MiB; ops4/memory64MiB | `dc1aff4d7c74898c0aca2da51e33760dbe716e70abc86a89f6e9d55d5451b03c`; `ccd29a7f8888cf2ddce1df111f8ad5ba7939b0a177ac6606ad98beb6ac160172` |

Keep the original independent-reference provenance in `Argon2Test.kt:82–94`.
Do not recompute expected values using the same production adapter. Existing
domain `SensitiveTextTest` additionally covers strict Unicode encoding; its
source checks are not Android32 evidence.

File-only Linux target observation: the only discovered installed image
descriptor is `/opt/android-sdk/system-images/android-35/default/x86_64/source.properties`
(SHA-256 `98d57aac1a56bdff21e948a44332774d7f2127dc28fa413a85ad96bfb81cb7cb`),
advertising x86_64/API35/revision2. That descriptor alone does not establish
32-bit capability. Subsequent file-only inspection of that image's `build.prop`
(SHA-256 `ca6377120f658498f4e230829dfc862ece90dd6fd26ce608cc2d5e63830e16c8`,
rechecked during this manifest follow-up) found:

```properties
ro.system.product.cpu.abilist=x86_64
ro.system.product.cpu.abilist32=
ro.system.product.cpu.abilist64=x86_64
ro.dalvik.vm.native.bridge=0
```

These declarations are contrary source evidence for using this unchanged image
as the Android32 target; do not boot it speculatively. They are not a live guest
measurement. No device transport, emulator start, guest capability probe,
Native.SIZE_T measurement or mapped-library probe was made. An actual 32-bit
runtime, preferably shipped armeabi-v7a plus a separately labeled x86 control,
remains unestablished. No physical-device claim follows from an emulator. At the
manifest checkpoint, the proposed official SDK catalog metadata request **had
not occurred**. Root subsequently renewed that one data-only request; its actual
result is recorded below, without retroactively claiming an earlier request.

## Catalog-only target feasibility — candidate found, actual target blocked

The root-authorized single metadata request ran at
2026-09-08T22:09:01.786974Z–22:09:02.342576Z and exited0. It fetched only
`https://dl.google.com/android/repository/sys-img/android/sys-img2-1.xml`:
HTTP200,183,268bytes,0.556seconds. There was one request, no redirect, no retry
and **zero archive requests**. Response limits were2MiB,10-second socket and
30-second overall deadlines, with an outer35-second process bound. No SDK
program, downloaded code, old runner or build/test was executed.

Retained request and exact metadata:

| Evidence | SHA-256 |
| --- | --- |
| `SDK-CATALOG-REQUEST.md` | `b9f35f330c3a1281901ee0a4429cc4d025a1f3054dacb50c9a5b58efaa918bcd` |
| `SDK-CATALOG.xml` | `ebf2d810d9e0c0b511ae49ee6e8c671a8fa67d210d06f5e3e068f83244ece435` |
| `SDK-CATALOG-RESULT.json` | `abe9675dde3224b784c928b2df33103e23c18ed487f51a9f7b8895e9c3e1177b` |

One selection, not an execution matrix: the lowest stable, non-obsolete
`default;x86` API meeting source minSdk24, newest listed revision at that API.
The selected catalog block declares:

- Package `system-images;android-24;default;x86`, API24, ABI`x86`, revision8,
  stable channel0; no host-OS restriction or emulator dependency is declared
  in that block. Absence of such metadata does not guarantee tool compatibility.
- Archive `https://dl.google.com/android/repository/sys-img/android/x86-24_r08.zip`,
  declared313,489,224bytes (about299MiB). Neither archive reachability nor bytes,
  expanded disk usage, native ELF class or contents were checked.
- Declared checksum `c1cae7634b0216c0b5990f2c144eb8ca948e3511`. The captured
  checksum element has **no explicit algorithm/type attribute**; the receipt
  reports `unspecified` rather than silently converting its40-hex shape into
  verified SHA-1. No checksum was checked against an archive.
- Referenced `android-sdk-license` text is retained in the catalog and receipt;
  normalized extracted UTF-8 text SHA-256
  `de5fa465e908e7f098e718082f013135b9a36ee706d032cf510f4f1153424b2a`.
  The independent reviewer found that extraction's `.strip()` removes the
  parsed XML element text's final LF. Its unstripped parsed-text UTF-8 hash is
  `1f8729233617b193fd619213792ae16a41b95d2bbbf525dfe66998252ba68b16`;
  neither parsed form is the raw serialized XML byte identity. The original
  catalog and receipt were not rewritten. This lane did not accept a license
  or install a package.

`/root/build_config` independently accepted the bounded selection in
[`ANDROID-CATALOG-INDEPENDENT-REVIEW.json`](../build-config/ANDROID-CATALOG-INDEPENDENT-REVIEW.json),
SHA-256 `e62700e1ec0ff6c3e5d8dc0775d0dd9bc78624cca3ad2e67f46354abf99a8c3b`:
**ACCEPT_LOCAL_CATALOG_SELECTION_WITH_NORMALIZATION_AND_RUNTIME_QUALIFICATIONS**.
The reviewer independently parsed retained local data, performed no network
request, and preserved checksum/license/target limitations. The raw XML remains
source evidence, not a verified installed target or execution admission. No extra
catalog, archive HEAD/GET, alternative target or fallback matrix was attempted.

At the22:11:08Z local point observation, the host was Linuxx86_64 and `/dev/kvm`
was absent. Installed emulator metadata declares37.1.11/build15917651; its
`qemu/linux-x86_64/qemu-system-i386` file exists (31,971,344bytes,mode700), but
neither it nor `emulator` was run. A backend filename does not prove a usable
software accelerator, compatibility with this old image, or acceptable startup
and test duration. KVM acceleration is **not available through that absent
device at this observation**; this is not a complete host-virtualization audit
or proof that all software emulation is impossible.

Therefore there is a concrete catalog-listed candidate, but no current
Android32 target admission. Root must separately decide whether bounded
software-emulator installation/probing is viable or supply a verified32-bit
target. Required prerequisites still include verified archive/schema checksum
semantics, license handling, bounded expansion and private AVD/HOME/TMP/ADB
namespaces, generated test APK/manifest/ELF identities, time/resource limits,
one-job ownership and independently reviewed success/failure/cancellation
cleanup. No KVM setup, privilege change, emulator/ADB launch, download/install or
automatic unchanged retry is authorized by this report.

## Bounded target harness — source-accepted, not executed or admitted

Root authorized writes limited to `core/crypto/build.gradle.kts` and the new
`androidDeviceTest` platform `Instrumentation` class. The independent
`build_config` reviewer challenged the initial commonTest-sharing proposal:
its JVM backtick names and test graph should not be assumed DEX-compatible at
minSdk24. That initial proposal was **rejected before implementation**, not
silently accepted or marked passing.

The revised AGP9.4 API proposal was independently accepted from public primary
documentation: `withDeviceTestBuilder { sourceSetTreeName = null }.configure`
with the custom runner and `execution = "HOST"`. The builder contract states
that null excludes common test source sets. No orchestrator, AndroidX runner,
JUnit dependency, dependency/version or production identity change was added.
The exact generated Gradle graph, compilation and device behavior remain
unexecuted; API-document review alone is not build admission.

The new harness has four fixed camelCase case IDs under
`com.passvault.core.crypto.Android32KdfInstrumentation`:

| Case ID | Intended evidence | Native KDF calls |
| --- | --- | ---: |
| `nativeRuntimeIs32BitAndLoadsSodium` | Actual process width, JNA widths/runtime version, same-library sodium version and bounded optional symbol-mapping row |0 |
| `historicalBinaryPasswordVector` | Existing binary-password8KiB vector copied exactly |1 |
| `historicalTextPasswordVector` | Existing text-password8KiB vector copied exactly |1 |
| `productionProfilesMatchReferenceVectors` | Existing independent ops3/4,64MiB vectors copied exactly |2 |

This is an explicit fixed instrumentation selection, **not JUnit discovery or
four new executed methods**. It calls production crypto directly with the
unchanged oracle bytes above; it does not call/import all commonTest classes.
All four statuses and exact names must be reconciled from raw transcript/XML.
Source intends fail-stop: body exceptions and linkage/assertion errors emit
error/failure and finish non-successfully; a64-bit process fails before any
KDF and later cases remain unexecuted. A terminal success is only after all
four fixed cases returned. Source intends to wipe every allocated vector
buffer and DerivedKey in finally; process-death erasure is not promised.

Process checks are `!Process.is64Bit`, SIZE_T4 and POINTER4, not merely
Build.SUPPORTED_ABIS. A default install on a64-bit guest can still choose64-bit
even when32-bit processes are supported: future admission must select an actual
32-bit process explicitly and record how; it must not interpret the resulting
width failure as an Android32 KDF execution. JNA VERSION is read reflectively
rather than compiled inline from5.19.1. `sodium_version_string` comes from the retained
Proxy handler's library that supplied `crypto_pwhash`. Only its own symbol's
mapping row is retained, no memory dump. A denied/missing maps read is explicitly
`NOT_ESTABLISHED`, and APK ELF/hash/minified reconciliation is unconditionally
separate. No mapping filename or process bitness is labeled that proof.

Initial implemented source identities submitted for independent code review:

- `core/crypto/build.gradle.kts`:
  `7fd198ff5e61eee269ed359cde854764aba670d3325ee23d9179df1f27f5ed09`.
- `core/crypto/src/androidDeviceTest/kotlin/com/passvault/core/crypto/Android32KdfInstrumentation.kt`:
  `eadcc983a2faf883cefba13c4e7e55f3d9dc70f5c41bdab50a2f8a2b73bc5c22`,277LF.

The independent reviewer then required two bounded harness corrections:

- Resolve inherited VERSION-field accessibility instead of leaving it a
  mandatory reflection uncertainty. The second image uses `isAccessible = true`
  only on that one exact app-library static constant; it does not reflectively
  access private platform/native-memory APIs or use Java9-only APIs on min24.
- Replace the named-selector blocklist with an allowlist. Empty arguments, or
  only a bounded String `additionalTestOutputDir`, are accepted; the latter is
  inert AGP transport metadata and is never opened/used. All other keys,
  including unlisted selector/dry-run/shard aliases, fail before `start()`.
  A separate guard rejects anything other than four uniquely named cases.

The revised two-file images received independent final source acceptance from
`/root/build_config`: [`ANDROID-FINAL-REVIEW.md`](../build-config/ANDROID-FINAL-REVIEW.md)
and [`ANDROID-FINAL-INPUTS.json`](../build-config/ANDROID-FINAL-INPUTS.json).
Disposition: **ACCEPT_BOUNDED_SOURCE_PATCH; NOT_EXECUTED; NOT_ADMITTED**. This
explicitly supersedes this report's earlier pending final-code-review label;
it does not admit an execution helper, parser, target, cleanup plan or build.
The hashes below were rechecked at this manifest follow-up and remain unchanged
for both accepted files. The independent proposal review remains
`../build-config/ANDROID-PROPOSAL-REVIEW.md`, with API receipts in
`../build-config/ANDROID_API.json`; neither rejection history nor uncertainty
about the realized Gradle graph is erased.

### Test-only ABI preference manifest

Root separately authorized only
`core/crypto/src/androidDeviceTest/AndroidManifest.xml`, now implemented with
an `<application>` carrying `android:multiArch="true"` and
`android:use32bitAbi="true"`. There is no package, application ID, targetPackage,
instrumentation, namespace, minSdk or product-manifest override. The public SDK
attribute inspection and independently accepted proposal are recorded in
`../build-config/ANDROID-MANIFEST-ATTRIBUTES.json`. `/root/build_config` then
independently accepted the exact third-file after-image in
[`ANDROID-MANIFEST-SUPPLEMENT.md`](../build-config/ANDROID-MANIFEST-SUPPLEMENT.md)
and [`ANDROID-MANIFEST-INPUTS.json`](../build-config/ANDROID-MANIFEST-INPUTS.json):
**ACCEPT_BOUNDED_SOURCE_PATCH; NOT_EXECUTED; NOT_ADMITTED**. This separate
supplement does not retroactively expand the original two-file review.

`use32bitAbi` is a preference for a multiArch application, not unconditional
32-bit enforcement; an install ABI override takes precedence. The generated
test target/manifest wiring and actual process remain unverified. The runtime
`Process.is64Bit`, POINTER4 and SIZE_T4 fail-closed checks remain mandatory.
This manifest cannot convert the installed 64-bit-only image declarations into
32-bit capability or count as a native KDF pass.

| Final selected source | SHA-256 | Current review scope |
| --- | --- | --- |
| `core/crypto/build.gradle.kts` | `a18a2ee9127da9861d4310ab7588573d0596d31c317e3ff393c8e30f70888112` | Independent final source acceptance; 90LF |
| `core/crypto/src/androidDeviceTest/kotlin/com/passvault/core/crypto/Android32KdfInstrumentation.kt` | `d9bdb00637773f5d239ffcc5f7d13dc3773e528ef0dec1b1efdffec0ee182310` | Independent final source acceptance; 291LF |
| `core/crypto/src/androidDeviceTest/AndroidManifest.xml` | `95a2d24cac877cd0d4fbbfeb58bc10d06275b2c40592244617a83564ec1a06ff` | Independent source acceptance by separate supplement; 209 bytes/6LF |

The production `app-android/src/main/AndroidManifest.xml` hash before and after
the test-only addition was
`5bd74cede2d0328c76ca7ba6ea1876df2491267d44077d54bce25df40226ab4b`.
No product manifest or identity was edited. These are source-only point
observations, not generated/merged manifest or packaged-application proof.

Following separately reviewed wiring and execution admission, required work is:

1. **Android32 native vectors:** freeze exact source/tree, test APK and library
   hashes, supported/runtime ABI, runner and method-name mapping. Execute the
   fixed real adapter vectors with synthetic data on one admitted 32-bit target.
   Retain compact raw instrumentation transcript, exact test XML/statuses and
   actual loaded-version/mapping evidence. A 64-bit run is only a compatibility
   control; it cannot discharge this stage. No automatic unchanged reruns.
2. **Real synthetic workflow:** create a new isolated Android Room database
   with the production Android builder/driver and real crypto/repositories,
   create then unlock a synthetic vault, reject a wrong password without opening
   a session, save/read a synthetic credential, change password and verify old
   rejection/new success. Export/inspect/restore V2 plus an independently seeded
   historical-format backup, then unlock with the restored **vault password**
   and compare decrypted credential bytes. A backup-container password must not
   be mistaken for the restored master password. Select tiny payloads and
   production bounded KDF profiles; do not seed a real vault or bypass crypto
   with `FakeCryptoEngine`. The real Desktop streaming fixture at
   `VaultBackupStreamingTest.kt:73–144` is a wiring reference, not an Android
   fixture or new pass. Adapt cleanup rather than copying its sequential
   teardown: lock/session cleanup, DB close, buffer clearing and allowlisted
   file cleanup must each be attempted independently.
3. **Compatibility discriminator:** retain the existing fixed vectors and a
   historical synthetic backup/metadata fixture, rather than relying solely on
   same-version roundtrip (which could pass after both reader and writer drift).
   Do not change the PVD-002 password encoding to make a fixture pass.
4. **Packaging remains separate:** current source declares all four ABIs and
   verifies libjnidispatch/libsodium ELF class/machine among the exact library
   set. R8 keeps the binding package. Those checks do not establish a current
   minified installed app's loader or every production crypto path. Such a
   non-publishing validation needs its own reviewed exact configuration; it is
   not a replacement of candidate/build1017001 and must not use production
   signing/store secrets. No release task is admitted by this report.

Before any future stage, root must record exact commands and target needs,
timeouts/case expectations, resource bounds, synthetic namespace, own-process
coordination and cleanup admission. A sensible proposed vector stage ceiling
is15 minutes with one serial instrumentation process and no larger than the
existing64MiB KDF calls; this is a **proposal, not measured demand or admission**.
Source/build bootstrap, launch disk12GiB/memory25%, running disk8GiB/memory20%,
wrapper stop and settled-owned-process evidence remain conjunctive gates.
Build/test job ownership is global across local and CI; this reviewer does not
launch either. No Apple/Windows CI run could substitute for Android32.

## Source-byte bindings and precise scope

All below are **pre-harness** inspected identities. Full hash does not mean
whole-file rereview. Future edits require their own after-image binding.

| Source | SHA-256 | Review scope |
| --- | --- | --- |
| `core/crypto/src/commonMain/kotlin/com/passvault/core/crypto/RawPasswordHash.kt` | `3fb16abe001752bf44b5a56039c58738ebed0e6ced5972e3ca0985d5a40586eb` | Full70LF |
| `core/crypto/src/androidMain/kotlin/com/passvault/core/crypto/RawPasswordHash.android.kt` | `d2c4bcd22df2cb89a97e10a4b591d651f224f0b1c5224bc6b5de10f407d0df1c` | Full86LF |
| `core/crypto/src/desktopMain/kotlin/com/passvault/core/crypto/RawPasswordHash.desktop.kt` | `d2c4bcd22df2cb89a97e10a4b591d651f224f0b1c5224bc6b5de10f407d0df1c` | Full86LF |
| `core/crypto/src/iosMain/kotlin/com/passvault/core/crypto/RawPasswordHash.ios.kt` | `3238d40ccfa77f164834ce60d15c33a8d3e5173c6669142d0fe0ee3bef396fc9` | Full50LF |
| `core/crypto/src/commonMain/kotlin/com/passvault/core/crypto/LibsodiumCryptoEngine.kt` | `2b8dd21fe476735f16f1ab35543417a70202e7923dfac328f9064e31f5923b57` | Full336LF; focused KDF/cleanup reasoning |
| `core/crypto/src/commonTest/kotlin/com/passvault/core/crypto/RawPasswordHashArgumentTest.kt` | `c441eca77913a450fab817f6450f7aaa82ab8cbcb7077a25252e451ac7c6459a` | Full43LF |
| `core/crypto/src/commonTest/kotlin/com/passvault/core/crypto/Argon2Test.kt` | `195fb796a4ed2a6a03cc27c6a785ebe09efd2dd977b30591f51025912e597231` | Full207LF |
| `core/crypto/src/androidMain/kotlin/com/passvault/core/crypto/AndroidCryptoEngine.kt` | `729a9e49d731ff72125b5c155ef95c898411cc1672c625e9e49b7f74d7746e27` | Full10LF |
| `core/crypto/src/nativeInterop/cinterop/passvault_sodium.h` | `192edcd85cfc70c602b5141b567bee03162fb33bae3af0029490a8bd67f49dce` | Full C declaration/wrapper |
| `core/crypto/build.gradle.kts` | `35738bf74b338c54e7ac88ca37b3ab7a3c73d9e32785052f9282893a5e2616b9` | Full module wiring |
| `app-android/build.gradle.kts` | `bc4aef06f156617d15b41bfbf41d58cc4b455c4fcd15152f8ed31a4b8fcc7625` | Package verifier/479–489/531–601 and test-dependency tail; truncated middle not whole-file credit |
| `app-android/proguard-rules.pro` | `7d065f82f2f55a000d3cc1137576bfa9cefa4b38c9059fc6bcfef657c965ac5c` | Full policy |
| `gradle/libs.versions.toml` | `49775212b92b73197492f36f7c219dbf5d2521b6bbc197c70be50d203c58c57a` | Relevant AGP/SDK/libsodium/JNA/test entries only |
| `gradle/verification-metadata.xml` | `21ab6f9c2873325558f5bb96c7e7f68bfbb674e2a7ce867ffe19dabdae60c299` | libsodium/JNA/test artifact entries only |
| `shared/src/commonMain/kotlin/com/passvault/shared/di/AppModule.kt` | `9b486df509202e65311caf92895ba8db6a44e6eae351266c8daa952bc553af25` |53–82/crypto binding |
| `core/domain/src/commonMain/kotlin/com/passvault/core/domain/model/ValueTypes.kt` | `9935405eef7911b34cd28a5aebd367fd1592927466e6347a4a0344129000edbd` |66–94/strict encoding entry; historical underlying encoder review retained |
| `core/database/src/commonMain/kotlin/com/passvault/core/database/repository/VaultRepositoryImpl.kt` | `f2fc2ff02926e62230eff5e4723b2a68998649eee5f758f5ff99df93bcf61469` |136–214,224–280,675–719,739–778; password-change caller search only |
| `core/database/src/commonMain/kotlin/com/passvault/core/database/backup/BackupV2Codec.kt` | `997346d5ae32e1699e1939b2b1cc53664477cf9be8a3eca0e52dcb7561410445` |96–148,376–426 |
| `core/database/src/commonMain/kotlin/com/passvault/core/database/backup/VaultBackupService.kt` | `68d3e276432489d2c4efb4b854cd203a5f0f3f380565fe292c7e3b49d9be2c12` |455–551 |
| `core/database/build.gradle.kts` | `e83ca3a79cafd99f25d9d5a34c4db15f30ba1784a2e7613cfb44d9cbd7ffdbbc` |Full module wiring |
| `core/database/src/androidMain/kotlin/com/passvault/core/database/Database.android.kt` | `29cb20792690ce8947458e81cac3e47b789d247de0b09ea2265b5506aa23eb67` |Full58LF; no opening of application storage |
| `core/database/src/desktopTest/kotlin/com/passvault/core/database/backup/VaultBackupStreamingTest.kt` | `ebf9675ec7c551c291848dc24a72a6056098d31e75d67ee31510112608dfc2d2` |1–160/fixture only; no execution |

## Safety, failures and accounting

Source/metadata inspection and authorized permanent test/report edits only;
no generated build files, temporary runtime or persistent worker was created.
Gradle `--stop` and build-output deletion are
NOT_APPLICABLE to this review, not settlement of earlier obligations. Several
initial searches named nonexistent convention/crypto-di/header paths and printed
file-not-found diagnostics; later directory discovery identified the actual
files. These were inspection-path mistakes, not product or build/test failures.
No application evidence is inferred from an inspection command's exit status.

Initial point resource observation was below launch floor: worktree
4,785,764KiB available and RAM21,212MiB/64,311MiB. A later source-only observation
found15,703,044KiB available and RAM39,312MiB/64,311MiB, above those numerical
floors; no files/processes were removed by this reviewer to cause that change.
The 2026-09-08T21:55:28Z manifest-follow-up point observation was29,143,152KiB
worktree disk available,18,148,308KiB `/tmp` available and42,296MiB/64,311MiB RAM
available; no resource reclamation by this lane is claimed.
After the catalog-only request, the22:11:08Z point was29,089,376KiB worktree
available,18,138,208KiB `/tmp` available and42,132MiB/64,311MiB RAM available.
Only the compact catalog/receipt/request evidence is retained. The foreground
reader exited0 with its response/files closed, no temporary outputs or launched
background worker; no SDK/cache directory was changed and no wrapper stop was
needed for this data-only request.
Resource improvement alone does not admit a build. External concurrent disk/RAM
changes do not authorize deletion of unrelated caches, processes, toolchains or
SDKs. Root's latest admission observations supersede these point-in-time values;
this report grants no execution authority.

No new confirmed family, qualified closure or conclusive PVU outcome is claimed.
Starting denominators stay19/25 original qualified closures,22/37 all confirmed
qualified closures,2/12 conclusive original suspicions; the eight PVD explanations
and pending owner decisions remain separate. Preserve PVU-007 STOP, PVU-011 NO
RETRY, PVA-029's recorded failure/no automatic retry and G7/G8 CLOSED scopes.
No archived application/recovery helper or unadmitted old validation runner was
executed/imported. Candidate/build1017001 and protected branches remain untouched.
