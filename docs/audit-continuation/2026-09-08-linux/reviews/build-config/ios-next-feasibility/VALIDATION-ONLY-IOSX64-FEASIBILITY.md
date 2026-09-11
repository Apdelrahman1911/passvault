# Validation-only iosX64: same-ten feasibility, not a runner-label retry

Author `/root/build_config`, 2026-09-11. **Source/retained-data assessment only;
no implementation, execution admission or new product finding.** Reuses
`INTEL-SAME-TEN.md` (SHA256 `ef55795e8b0d6cc0c9f73e6f7158eb789fc6ee53234c4939364e6cbefd277ac7`)
for IOS01/Mac02 outcomes rather than repeating its label-only conclusion.

## Disposition

A separately gated **validation-only** target need not change production app
identities, versions, PVD semantics or the two shipped ARM framework targets.
However, the same-ten Intel route **cannot currently be established with the
retained verified dependency/toolchain evidence**. It requires more than a new
test-target name: the existing production-linked graph needs seventeen compatible
project variants, native crypto/cinterop and Room generation, plus currently
unverified x64 dependency artifacts. Do not implement a speculative harness or
weaken verification to discover whether this combination exists.

This is **not proof that Kotlin2.4.10 or every dependency has removed iosX64**.
Their exact x64 support/ABI remains unestablished by the inspected retained source;
no remembered support matrix, guessed artifact or unverified URL is substituted.
An unavailable artifact and an artifact absent from our trust evidence are distinct.

## Smallest structural change preserving the existing module/test contract

The full selection remains crypto2 + shared8, not two cases or ten tasks.
`shared` directly uses seven core and all eight feature modules in `commonMain`
(`shared/build.gradle.kts:69-86`), and `commonTest` adds `core:testing` (`90-98`).
Thus the minimum current module closure for an added, opt-in x64 variant is:

- `shared`;
- `core:{domain,database,crypto,security,designsystem,navigation,otp,testing}`;
- `feature:{onboarding,unlock,vault,credential,generator,health,settings,backup}`.

All seventeen current build declarations were inspected. Two runtime filters do
not avoid compiling their production sources, shared/common test sources, Compose
resources, or native dependencies. Crypto's two cases alone would still need
crypto/testing/domain/security variants and would silently omit the required eight
shared cases; that is not the requested solution.

The minimal *conceptual* wiring would be one default-off, validation-only target
registration consistently applied to this closure, retaining `commonMain`/`iosMain`
and `commonTest`/`iosTest` participation. The conventional task names would become
`:core:crypto:iosX64Test` and `:shared:iosX64Test`, with exactly the existing two
crypto method filters and two shared class filters. These are **prospective names,
not an observed available target/task graph**.

- Crypto (`build.gradle.kts:34-49`) needs the same `rawSodium` main-compilation
  cinterop for x64; a bare target addition leaves `RawPasswordHash.ios.kt` without
  its generated import. The75-byte `.def` only names the header/package: it does
  not supply a native library. `passvault_sodium.h` declares `crypto_pwhash` and a
  wrapper; the actual x64 libsodium symbols/static linkage must come from the
  compatible dependency. Header/source inspection found no ARM assembly or
  target-specific branch, but is not a generated ABI/link compatibility proof.
- Database requires the corresponding KSP target wiring, conventionally
  `kspIosX64`, in addition to its current ARM64 entries (`73-77`).
  `VaultDatabase.kt:47-60` has `@ConstructedBy` and an **expect**
  `AppDatabaseConstructor`; `Database.ios.kt` uses Room and `BundledSQLiteDriver`.
  Their code-generation/runtime/native variants cannot be omitted just because
  these selected methods do not open a database. Exact KSP2.3.10 x64 realization
  and generated constructor compatibility are unproved here.
- Keep `shared`'s existing ARM-only `iosTargets` framework loop unchanged
  (`28-39`); do not add x64 to published framework/export/packaging paths.
  No Xcode app scheme, embedding/signing, app identity or version edit is needed
  for the conceptual native test executable. No such edit was made.
- All-project configuration with CoD disabled remains required. A target gate
  does not authorize skipping Android/plugin/toolchain prerequisites, changing
  source dependencies or weakening legal/SDK checks.

## Concrete dependency and host gaps

The current catalog fixes Kotlin2.4.10, KSP2.3.10, Compose1.11.1,
libsodium bindings0.9.5, Room2.8.4, bundled SQLite2.6.2 and coroutines1.11.0.
The822452-byte verification XML contains **1575 components,114 named
`iosSimulatorArm64` components, zero named iosX64 components and zero iosX64
artifact-text occurrences** (case-insensitive). The only broad trust exceptions
are documentation/source jars and the reviewed Groovy metadata rule, not native
KLIBs. Existing ARM64 hashes cannot authenticate differently targeted x64 objects.

Concrete inspected ARM64-only trust evidence includes libsodium main **and
cinterop** KLIBs, Room runtime, bundled SQLite main/cinterop, Compose UI,
coroutines core/test and Koin test. This does not say upstream publishes only ARM;
it says their x64 artifacts/support and exact hashes are not established here.
Adding a target while leaving dependency versions unchanged still selects new
ABI artifacts: strict verification cannot treat those as already reviewed. An
x64 native build would need exact authoritative variant/support information and
independent artifact verification, or a separately authorized dependency change
if this version graph does not support that ABI. Neither is admitted by this note.

The already-verified Kotlin plugin source packet contains ten test/plugin members.
`KotlinNativeTestRunFactories.kt:46-68` requires macOS **and host architecture
matching the target**, so an actually supported x64 simulator target would satisfy
the architecture predicate on the previously observed Intel host. That is a
conditional guard proof, **not iosX64 DSL/target support**: the retained members do
not contain Kotlin2.4.10's target enumeration/preset/HostManager implementation,
compiler platform libraries or a verified x64 dependency variant matrix. A comment
mentioning `iosX64` in `KotlinNativeTarget.kt` is not that support contract.

Mac02's retained Intel14GiB resource success supplies no iOS SDK/simulator runtime,
Kotlin/Native x64 toolchain or JDK17-x64 admission. Its Xcode16.4/**macOS** SDK15.5
observation is not an installed compatible **iOS simulator** observation. Exact
licensed, installed toolchain/runtime and a newly owned synthetic simulator would
still be needed; no licence acceptance, downloads or live discovery occurred.
A paid account/large-runner entitlement is **not proven necessary or available**.
The no-new-ABI alternative is a sufficiently resourced, compatible Apple Silicon
host for the existing target, whose availability is external; IOS01's consumed
resource failure cannot be turned into an automatic retry on that host.

## Meaning and safety of the same ten

Current C17 `IosAttachmentFileStoreTest.kt` is12028B/SHA256
`406c47fd2ea71afe50a57acfbc5050bcda05a3050e94a16c5555a5e053bdc337`,
not the older9452-byte fixture recorded in the original feasibility note. It
retains the same seven method names/behaviors and now requires the explicit
`PASSVAULT_IOS_TEST_PARENT`, canonical Foundation temporary directory beneath it,
0700/effective-UID parent checks and bounded dispatcher settlement. Do not
remove those guards to obtain an Intel pass. OS file-protection behavior remains
physical-iPhone evidence, not something an x64 simulator can establish.

The prompt case still exercises real `LAContext` reason/cancel properties, not
biometric authentication or displayed language. `SecurityTest.setUp` still uses
`LibsodiumCryptoEngine`; the positive VEK roundtrip and typed wrong-KEK failure
remain meaningful native-provider checks if genuinely executed on a compatible
ABI. Replacing the provider, copying only test-shaped substitutes, discarding
KSP/production dependencies or silently dropping either module would not supply
these same production-linked ten cases. No such reduction is proposed.

## Exact source basis and unchanged accounting

The32 selected current build/metadata/source files were point-hashed against
retained C17 `SOURCE.json` SHA256
`e03d29e54974413ff893f96d9bcf0c70bbf11eeeaf114ac96296987456db1f7f`:
commit `0563e31adc9a66aefc3e74b99b9a24d17bdcdd49`, tree
`d1bd6ca5b18d08ff3ff15896d9a78af9016be799`; all matched their raw identities.
Selection: the seventeen build files above; root settings/build, catalog,
Gradle properties and verification XML; crypto header/def/iOS raw hash actual;
three selected test files; two shared iOS implementation files; and database's
iOS builder/common `VaultDatabase.kt`. This is point-source equality, not a new
Git/whole-tree/target execution capture.

Additional exact inputs: verification XML SHA256
`21ab6f9c2873325558f5bb96c7e7f68bfbb674e2a7ce867ffe19dabdae60c299`;
verified Kotlin source packet SHA256
`2730e2ec45b5638f7e58712d3a64960e81d61324c9a8ae8bbaa74066f8db73ee`
(all ten retained member text hashes checked); catalog SHA256
`49775212b92b73197492f36f7c219dbf5d2521b6bbc197c70be50d203c58c57a`.
No new source acquisition, cache/store/SDK/proc/runtime probe, Git/network,
helper import/execution, build/test/CI or source/central-ledger edit. Only this
permanent compact note is written; no temporary output or worker remains.

**Ten remain UNSTARTED; zero new tests/closures/denominator changes.** Independent
challenge of this assessment remains for root. All STOP/NO-RETRY/CLOSED/consumed/
HOLD scopes, PVD/hardware and protected-branch/Store/signing/1017001 boundaries
remain unchanged. This new feasibility question grants no ios01 retry authority.
