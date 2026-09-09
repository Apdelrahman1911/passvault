# PVA-001 — minimal remaining Android32 admission prerequisites

Author `/root/android32`; source/data assessment on 2026-09-09, after the retained
catalog/XSD capture. **NOT EXECUTED, NOT ADMITTED; independent review requested.**
Root owns the shared local/CI slot, execution admission, commits and central
ledgers. This is one new report, not an amendment of historical evidence or an
implementation of the proposed supervisor. No product/source patch is proposed.

## 1. What is already authorized, and what is actually missing

The user's continuation authority already covers suitable Linux investigation,
synthetic non-publishing validation, necessary focused tests/workflows, and
dedicated-branch publication. Preparing an isolated Android development target
does **not by itself** change an application dependency, Store build number or
product identity. Do not ask for blanket permission to resume Android work, or
call all remaining work an external platform blocker.

That authority does not waive independent execution/coordination/cleanup review,
grant new host privileges, accept a contract on an unidentified entity's behalf,
or admit a speculative image/renderer/version matrix. The present assignment is
source/data-only; it authorizes none of those executions.

| Remaining item | Classification and smallest next action |
| --- | --- |
| Selected catalog checksum algorithm | **RESOLVED STATIC INTERPRETATION.** Reuse the independently accepted common/01 chain; do not repeat catalog/XSD capture. |
| Exact downloaded archive integrity/layout | **UNEXECUTED INTERNAL WORK.** After admission, one pinned archive GET and bounded inspection; no successful download is currently claimed. |
| Exact license scope/acceptance | **CONDITIONAL OWNER CLARIFICATION**, detailed below. An existing acceptance marker is present; do not claim no acceptance exists. |
| Android process/ADB/network/storage containment and interrupted closeout | **UNFINISHED INTERNAL ADMISSION/IMPLEMENTATION.** First establish available capabilities and an exact independently reviewed forward-only supervisor. Capability absence has not been proved. |
| Current image | Installed API35/x86_64-only image is unsuitable, not evidence that all software emulation is impossible. The selected API24/default/x86 image remains unprepared. |
| Harness compile/task graph/merged manifest/APK | **UNFINISHED INTERNAL BUILD EVIDENCE.** Android device tasks are plugin-generated; see section 4. This does not require a running emulator. |
| Android32 native-vector result | **UNEXECUTED TARGET EVIDENCE.** A genuinely 32-bit Android process is required; a Linux64 JVM rerun cannot substitute. |
| ARM32 versus x86 | Separate native ABI evidence. An admitted ARM32 runtime could be emulated or physical; do not automatically equate an ARM32 software gap with a physical-device requirement. |
| Genuine hardware security behavior | Remains an **external/hardware gap** when the claim actually concerns hardware, Keystore or interactive device behavior. This four-case KDF harness makes no such claim. |

No missing authoritative skill tool is necessary for this assessment. No skill
repository URL, emulator source URL or alternative tool/version is invented.

## 2. License and artifact trust: narrow the question, do not manufacture it

The selected package at `SDK-CATALOG.xml:2086–2110` references
`android-sdk-license`, not the following preview license. Its text is dated
**January 16, 2019**. Relevant actual terms read from the retained catalog:

- 1.1 includes Android system files in the SDK.
- 2.1 requires agreement before SDK use; 2.2 describes clicking acceptance.
- 2.3 limits who may receive/accept it; 2.4 requires full legal authority when
  agreeing for an employer or other entity.
- 3.1 permits use solely to develop applications for compatible Android
  implementations. This Android application investigation is not development
  of another SDK or a non-Android product.
- 3.4 restricts copying/modification, subject to third-party licenses; 3.5 says
  open-source SDK components are governed solely by their open-source licenses.
  A blanket private copy of all installed tool files should therefore not be
  justified by the word "private" alone. Preserve notices and establish the
  relevant component copying rights, or independently review a read-only use
  of installed tools instead. This is not a claim that tool copying is forbidden.
- 6.1 describes separate usage-statistics consent. The validation scope does not
  authorize enabling telemetry, accounts or network services in the guest.

**New file-only observation:** at 2026-09-09T03:19Z,
`/opt/android-sdk/licenses/android-sdk-license` existed as a regular 41-byte file.
A bounded no-follow FD read, with stable device/inode/size/mtime/ctime before and
after, returned exactly a leading LF followed by
`24333f8a63b6825ea9c5514f83c2829b004d1fee` (no trailing LF).
Its SHA-256 is
`c43fa37686457c3f18caa3607945f4ec52a9d1beaaad8117e50dc4e863270c85`.
It was neither created nor changed here. This is existing local metadata, **not**
proof of who agreed, agreement for this entity/use, or a match to this catalog's
exact text. Direct SHA-1 of the raw parsed text was
`efa68a6b3c661d18699d5c026771d5911cdc2f83`; trimming the outer whitespace gave
`9002c006f4b8d9a16e715a9fa4df30ddb8abf9d9`. The SDK's actual license-normalization
contract was not inspected, so these two computations establish neither a
canonical SDK hash nor a license mismatch. Do not manufacture an acceptance file.

Smallest resolution: root should first determine whether an authorized owner's
existing acceptance covers this exact agreement/use. If that is already
documented, no new blanket authorization or automatic acceptance is needed. If
it is not established, ask the owner that specific question, or obtain explicit
authority to accept this retained agreement for the appropriate principal before
use. The user has authorized validation, not explicitly attested to agreement or
entity-binding authority. A successful/no-op `sdkmanager --licenses` is not a
substitute; the retained installed shell contract already warns against it.

The installed emulator has `NOTICE.csv` (43,518 bytes) and `NOTICE.txt`
(252,030 bytes); platform-tools has `NOTICE.txt` (1,175,837 bytes). Their initial
component/license headings were inspected as text, not their entire legal scope.
They show component-specific notices exist; they do **not** establish a complete
copy-rights disposition for the proposed bundle. No binary disassembly or SDK
program was run for this license assessment.

Artifact-trust prerequisite is now precise: selected archive
`https://dl.google.com/android/repository/sys-img/android/x86-24_r08.zip`,
**313,489,224 bytes**, catalog **SHA-1**
`c1cae7634b0216c0b5990f2c144eb8ca948e3511`. The complete namespace/type/group
chain is independently accepted in
`../build-config/ANDROID-XSD-CAPTURE-ACTUAL-AND-SCHEMA-REVIEW.md`.
HTTPS origin validation, exact length and this legacy digest can be explicitly
reviewed as the intended trust scope; they are not a publisher signature. A
locally computed SHA-256 subsequently binds received bytes, not independent
publisher authenticity. No supplied rule mandates an unavailable upstream
SHA-256: do not turn obtaining one into an invented absolute blocker. If root
adopts a stronger trust policy, its verified publisher source becomes an actual
prerequisite. No archive request, integrity result or installation exists yet.

## 3. Exact native evidence required — keep the ABI and compatibility boundaries

The accepted implementation still reaches the already-loaded sodium JNA proxy's
`crypto_pwhash`; it does not load a second library or invoke the upstream
incorrectly sized interface method. Current Android argument construction is:

| C parameter | Current supplied type | Required Android32 native width |
| --- | --- | ---: |
| out / passwd / salt | JNA Memory pointers | 4 bytes each |
| outlen / passwdlen / opslimit | Kotlin Long / boxed Long | 8 bytes each |
| memlimit (`size_t`) | `passwordHashMemoryLimitArgument(memLimit, Native.SIZE_T_SIZE)` | boxed Int / 4 bytes |
| algorithm (`int`) | Kotlin Int / boxed Int | 4 bytes |

The checked-in C declaration agrees with these types; it is source contract
evidence, not a new runtime ABI test. `NativeLong` would be wrong for Windows
LLP64. No register/stack offsets, ARM alignment behavior or binary linkage are
inferred from this table. The public deriveKey path retains strict caller text
encoding and **lowercase ASCII-hex of the caller's bytes** before the native KDF.
Do not "fix" PVD-002 by passing raw UTF-8, uppercase hex or a changed profile.

The permanent harness has four fixed intended cases and four intended KDF calls:
one width/loader case with **zero** KDF calls, two 8KiB compatibility-vector
cases, and one case containing the ops3/ops4 64MiB vectors. It checks
`!Process.is64Bit()`, JNA POINTER4/SIZE_T4 before KDF; actual runtime JNA VERSION
is read rather than compile-time-inlined. It obtains the sodium version from
the same loaded proxy library. Those checks are source-reviewed, uncompiled and
unexecuted. `multiArch=true` plus `use32bitAbi=true` is a preference, not proof of
target support or the executing ABI.

The next runtime receipt must bind the frozen source, exact APK(s), generated
test package/target, runner, resolved Android dependency artifacts and the
installed process. Retain a bounded raw instrumentation transcript and reconcile
all four exact start/pass identities, expected/started/passed counts and terminal
result. A shell/Gradle exit, empty XML or partial case list is insufficient.

Separately inventory and hash actual packaged `libsodium.so` and
`libjnidispatch.so` bytes in the tested APK. For x86 expect ELFCLASS32, little
endian, ET_DYN and EM_386; for ARM32 expect ELFCLASS32, little endian, ET_DYN and
EM_ARM. Architecture-directory labels alone are insufficient; these fields alone
also do not prove full ARMv7 feature compatibility, exports or successful loading.
Collect relevant symbols/ELF flags as needed without executing the libraries.
Compare with exact resolved AAR inputs, then reconcile the loaded library path
where available. The optional own-symbol `/proc/self/maps` row and Java JNA
version are **not** ELF hashes or a jnidispatch native-version proof. Android's
historical JNA runtime is 5.18.1; the compile-only declaration is 5.19.1. Do not
change either dependency merely to make the harness build.

Even four passing native cases establish only that exact ABI/runtime/profile
slice. Synthetic create/unlock/wrong-password/password-change, credential
round-trip, V2 and independently seeded legacy backup compatibility remain
separate required business evidence. Minified application/loader, ARM32 and
hardware-specific claims do not acquire credit from a debug x86 harness.

## 4. Small useful build evidence exists before a target is ready

Source reread confirms `core/crypto/build.gradle.kts` applies AGP9.4.0's Kotlin
Multiplatform library plugin, calls `withDeviceTestBuilder` with
`sourceSetTreeName=null`, and selects the custom Instrumentation with
`execution="HOST"`. `androidHostTest` instead adds the **JVM** sodium artifact.
The retained official API data establishes this source-set/runner contract,
not realized task names or a resolved device classpath. This report does not
invent `assemble...DeviceTest`, `package...DeviceTest` or connected-test tasks.
Focused default `/root/.gradle` lookups found no selected AGP/JNA/sodium artifact
directories; that is **not** an exhaustive cache survey or proof dependencies
are unavailable in root's admitted configuration.

The existing project task `:app-android:verifyDebugComposeResources` depends on
`assembleDebug`, checks the four configured ABIs/native headers and legal assets,
and concerns the application Debug APK. It does not select or package this new
`core:crypto` instrumentation. A rerun of that unchanged verifier alone would
not close the harness gap. Its six-library application inventory is not the
library-module test APK's expected inventory. The Debug app is explicitly
unminified. Release package/assemble/bundle tasks depend on release-signing
verification: **do not bypass that guard, request signing inputs or rebuild
candidate 1017001 to seek minified evidence.**

After root settles/releases the database slot, the smallest useful build track
is one narrow harness compilation/package verification, with no device launch.
First resolve its exact task names/dependencies from the already available
AGP source/metadata or, if that cannot establish them, separately admit a bounded
Gradle task-discovery invocation (`:core:crypto:tasks --all`). Even discovery
executes build configuration and may resolve plugins; it needs the same owner,
JDK17, wrapper, strict verification and preinstalled cleanup controls. It is not
admitted here and must not auto-chain compilation. Freeze the resulting exact
task graph before approving the one package invocation. Inspect the merged
manifest, runner DEX/class presence, actual artifact graph, generated test
identity and native entries. This yields useful missing packaging/compilation
evidence but **zero executed application cases**; do not label it an Android32
native fix verification.

Proposed compact ceilings for review, not existing enforcement: discovery at
most 5 minutes plus a separately budgeted 2-minute stop/cleanup; harness package
at most 15 minutes plus 2-minute stop/cleanup; at most 4GiB aggregate owned RSS,
one Gradle worker and 4GiB new disk allocation, 16MiB combined logs and 32MiB
retained evidence. Launch must retain the 12GiB/25% host floors **in addition to
the disk reservation**; running floors remain 8GiB/20%. One-worker/heap flags do
not enforce aggregate RSS. Validate containment and these bounds rather than
silently enlarging them or automatically retrying. Disable configure-on-demand,
parallel execution/Detekt, persistent daemons and configuration/build-cache
writes as appropriate to the separately reviewed isolated build configuration;
dependency verification remains strict. Do not rewrite project defaults or
dependencies for this proposal. Wrapper `--stop`, owned-worker settlement and
allowlisted output cleanup apply even after discovery/failure/cancellation.

## 5. Smallest target track after the slot: preparation, then one boot

Reuse `TARGET-FEASIBILITY-PROPOSAL.md`, not another catalog GET. The next target
operation can be **archive preparation only**: new independently reviewed
supervision, one fixed GET, streaming length/SHA-1/SHA-256 verification, bounded
ZIP validation/extraction and compact identity/layout evidence. Do not run
sdkmanager, invent package.xml, boot or install the app in that phase. Defer
large tool copies until a separately reviewed boot is imminent; an image-only
subphase could fit within **3GiB new storage** (299MiB archive, up to 2.5GiB
extracted image, bounded metadata/logs), with the existing 15-minute common
preparation deadline. This smaller ceiling is a proposal to review, not a
relaxation of the preserved original 6GiB preparation+runtime envelope. Cleanup
the owned archive/partials promptly. If input review cannot lead to imminent use,
root must close out the staged image instead of retaining an indefinite cache.

Only after actual archive layout/identities and component-use scope are reviewed
should root admit the one software cold boot: API24/default/x86 revision8,
acceleration off, one vCPU, 1GiB guest RAM, private network/device/process view,
no physical USB/KVM/clipboard/account access, no internet and no application.
The preserved proposal caps aggregate host RSS at 4GiB, CPU at two cores' worth,
total private input/runtime storage at 6GiB, and each phase at 15 minutes.
Preserve its exact port/server ownership, no-auto-retry and settlement contracts;
independently review installed emulator/ADB semantics before execution. A guest
boot receipt must capture the actual 32-bit ABI properties/backend and owned
settlement, but it is still **zero KDF/application cases**. A later fixed-vector
run needs a separately frozen request and fail-closed result parser.

The previous `/dev/kvm` absence was a point observation, not a reason to declare
software emulation impossible. Monitor-only limits are not kernel containment;
no namespace/cgroup capability or special privilege has been tested here. These
are concrete engineering prerequisites, not evidence that only new hardware can
advance PVA-001. If the single admitted profile fails or cannot be safely
contained, preserve that exact failure and request the specific missing target
or capability; do not auto-fallback to another image, renderer, version or CI.

## 6. Exact inputs, resource observation and accounting

Observed containing HEAD `bb094f8a39ba43f1ce1f43f394cb662b03febe38`, tree
`82967f050816454b6905d560dfec15d0e0b091f5`. Other agents have unrelated worktree
changes; this is not a whole-tree freeze or fresh remote-ref verification.
Selected full-file SHA-256 identities reread for this assessment:

| Input | SHA-256 |
| --- | --- |
| `core/crypto/build.gradle.kts` | `a18a2ee9127da9861d4310ab7588573d0596d31c317e3ff393c8e30f70888112` |
| `androidDeviceTest/.../Android32KdfInstrumentation.kt` | `d9bdb00637773f5d239ffcc5f7d13dc3773e528ef0dec1b1efdffec0ee182310` |
| `androidDeviceTest/AndroidManifest.xml` | `95a2d24cac877cd0d4fbbfeb58bc10d06275b2c40592244617a83564ec1a06ff` |
| `commonMain/.../RawPasswordHash.kt` | `3fb16abe001752bf44b5a56039c58738ebed0e6ced5972e3ca0985d5a40586eb` |
| `androidMain/.../RawPasswordHash.android.kt` | `d2c4bcd22df2cb89a97e10a4b591d651f224f0b1c5224bc6b5de10f407d0df1c` |
| `commonMain/.../LibsodiumCryptoEngine.kt` | `2b8dd21fe476735f16f1ab35543417a70202e7923dfac328f9064e31f5923b57` |
| `commonTest/.../Argon2Test.kt` | `195fb796a4ed2a6a03cc27c6a785ebe09efd2dd977b30591f51025912e597231` |
| `nativeInterop/cinterop/passvault_sodium.h` | `192edcd85cfc70c602b5141b567bee03162fb33bae3af0029490a8bd67f49dce` |
| `app-android/build.gradle.kts` | `bc4aef06f156617d15b41bfbf41d58cc4b455c4fcd15152f8ed31a4b8fcc7625` |
| `gradle/libs.versions.toml` | `49775212b92b73197492f36f7c219dbf5d2521b6bbc197c70be50d203c58c57a` |
| `gradle.properties` | `323971ec6c0f02ff28c724b4c4f3120b5b8843faf3d77608485c1e15fb633235` |
| `SDK-CATALOG.xml` | `ebf2d810d9e0c0b511ae49ee6e8c671a8fa67d210d06f5e3e068f83244ece435` |
| Accepted actual/schema review | `a97a44c24580df4e65c1d96ba1dc71666d9d6bf7ed437bdb4550ec384dd94acb` |
| Emulator `NOTICE.csv` / `NOTICE.txt` | `ebe7da490e8c745121ef7aa24d9a2e5dfbc3507cd2914aeea92252d0b3a540c1` / `10b92e614cd8267a044b040cf1e94c8f4ad7ee44560f7e9e1be9fbc1e3529dd5` |
| Platform-tools `NOTICE.txt` | `c29da8f704720fa1d3d802b834b86d9c023f20ae2596a8f2a4e17ed5490b17ae` |

Abbreviated crypto paths above are under `core/crypto/src/` and package
`kotlin/com/passvault/core/crypto/` where applicable. Hashing an entire file does
not imply full semantic review of it: engine deriveKey, application packaging
wiring and initial NOTICE headings were the focused scopes. Several combined
inspection outputs were truncated; no whole-notice/whole-ledger/SDK-consumer
review is claimed from those outputs. License raw/normalized SHA-256 identities
remain the two retained values in the original catalog result/proposal.

At the first data read of this assessment, `df -Pk` showed worktree free
24,283,744KiB and `/tmp` free 20,508,328KiB; `free -m` showed 40,864MiB available
of 64,311MiB. These are point observations, not slot/resource admission or a
guarantee against unrelated consumption. All this lane's foreground file/data
readers exited; it created no background worker, SDK/cache mutation, temporary
extraction or runtime directory. The sole new file is this permanent compact
report. There is no new wrapper-stop obligation from these data-only reads, and
no claim that other owners' processes or earlier obligations are settled.

This assessment adds **zero tests, native calls, qualified closures or resolved
suspicions**. Current central denominators remain 19/25 original confirmed
closures, 22/38 all confirmed closures and 2/12 original suspicions; the handoff's
22/37 is preserved history. The eight PVD explanations/owner decisions stay
separate. PVU-007 STOP, PVU-011 NO-RETRY, PVA-029's recorded FAIL/no automatic
retry, G7/G8 CLOSED scopes, no old-runner import/execution and occupied mobile
build 1017001 remain unchanged. No new scope is admitted by this report.
