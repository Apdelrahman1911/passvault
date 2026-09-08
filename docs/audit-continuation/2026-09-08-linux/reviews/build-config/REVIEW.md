# Build/configuration continuation review

Reviewer: `/root/build_config`. **SOURCE/PUBLIC-METADATA REVIEW ONLY; NO RUN ADMISSION.**

Containing checkpoint observed: `9bdf9559b7a801a6f2fa49be3d7836ea6ef4d0ed`,
tree `05014e9f635131d5db06701e4013b4b5a746465a`. This report does not freeze other
agents' subsequent changes. `INPUTS.json` binds the inspected configuration and
selected source bytes, distinguishing full reads, focused reads and hash-only
inputs. No application finding is added or closed here.

## 1. Pending task graph: source-derived, not a realized Gradle graph

### PVA-033/034/035: `:core:database:desktopTest`

- Keep the exact seven-class/105-method matrix in the handoff. Filters restrict
  execution, not Kotlin compilation: all common/desktop test sources of this
  module still need to compile. Three selected repository classes share one
  source file; compiling a file does not execute every class in it.
- Database desktop main depends on domain, crypto and security; its desktop
  test adds `core:testing`, which depends on domain/crypto/security main code.
  Crypto test dependencies do not imply running crypto tests. Domain adds
  serialization; database adds Room 2.8.4, KSP 2.3.10, SQLite-bundled 2.6.2,
  coroutines, serialization and Okio. Desktop crypto adds JNA 5.19.1 and the
  platform variant of libsodium-bindings 0.9.5.
- KSP desktop generation and Room schema processing are part of compilation.
  `room.schemaDirectory("$projectDir/schemas")` is a **tracked schema input/output
  boundary**, not an allowlisted disposable directory. Check for generated
  schema drift; never remove permanent schemas during cleanup.
- Selected integration fixtures actually construct Room/BundledSQLiteDriver
  and DesktopCryptoEngine; they are not pure JVM mocks. Streaming and Unicode
  suites create synthetic files using the worker's default Java temporary
  directory. Some teardown paths remain sequential; source intent is not
  settlement evidence. Preserve the existing matrix's limits.
- No repository-defined task dependency from this task to app-desktop CMake,
  packaging, application launch, Detekt, legal inventory or root `check` was
  found. This is **not proof of plugin-generated execution or loader behavior**.

### PVA-031: `:feature:credential:desktopTest`

- Keep the two named classes (`CredentialCustomFieldCapacityTest`, seven
  methods; `CredentialCustomFieldDraftTest`, thirteen methods). The previous
  invocation reached zero assertions and zero XML files.
- Main compilation adds domain, crypto, designsystem, OTP and security;
  desktop adds Compose current-OS runtime/Skiko, coroutine Swing and ZXing.
  Common tests add core:testing and coroutine-test/Turbine. Designsystem resource
  generation is necessary even if filtered tests do not render a UI.
- These existing tests use synthetic repositories and scheduler-driven state;
  their execution would not establish rendered dialog, keyboard, accessibility,
  RTL, disposed-owner or real native-window behavior.

### Configuration is larger than either selection

`settings.gradle.kts` includes every app/core/feature module. With
configure-on-demand disabled, Android/iOS/Desktop configuration is not skipped
by a desktop filter. The settings Foojay marker resolves before root project
plugins. Root requests Kotlin 2.4.10, AGP 9.4.0, Compose 1.11.1, Compose compiler,
serialization, KSP, Room and Detekt 2.0.0-alpha.6. Root applies Detekt to all
subprojects; selected tests are not themselves wired to execute Detekt.

The `projectsEvaluated` legal inventory code obtains Android release, Desktop
and iOS configuration objects and attaches providers to lazy reporting tasks.
It does not call the inventory task during a selected test. This source review
does not establish all plugin/provider realization side effects.

Root `test` is an aggregate over Desktop and Android-host suites plus version
and localization checks; root `check` additionally reaches subproject checks,
Android debug packaging and dependency/legal tasks. **Neither is a substitute
for the narrow admitted selection.** Root `verifyDependencies` is also not a
cheap standalone checksum read: it depends on cross-platform legal resolution.

## 2. Fresh execution confinement still required

1. JDK17 exists at `/usr/lib/jvm/java-17-openjdk-amd64`; its release file declares
   Ubuntu `17.0.20+8-1-24.04-Ubuntu`, x86_64. The default Java is not this binding.
   `jvmTarget=17` declares bytecode, **not a pinned runtime/toolchain**. There is
   no project `jvmToolchain` or Test `javaLauncher` setting. Bind actual wrapper,
   Java executable/release and effective child runtime; disable unadmitted
   toolchain discovery/download/substitution in the reviewed invocation.
2. `gradlew` consumes `JAVA_OPTS`/`GRADLE_OPTS`; JVMs also recognize ambient
   option variables. Start from a reviewed environment allowlist, not inherited
   arbitrary init/options/properties. Bind `HOME`, `GRADLE_USER_HOME`, project
   cache, temporary directories and `KONAN_DATA_DIR`; fresh namespaces only.
3. **Worker properties are unresolved.** No current Test configuration sets
   `java.io.tmpdir`, `user.home`, heap, forks or environment. Setting shell HOME
   or TMPDIR, or the Gradle daemon's `org.gradle.jvmargs`, does not establish
   these properties in the forked test JVM. Add/review an exact confined worker
   mechanism before execution, and retain effective worker evidence. A fresh
   HOME alone does not authorize the Linux account's actual home/vault paths.
   Root subsequently chose an exact generated `JAVA_TOOL_OPTIONS` containing
   `-Xmx512m` and confined properties, with explicit daemon `-Xmx2g`.
   `JDK17-OPTIONS-REVIEW.md` independently qualifies upstream17.0.20+8 ordering;
   it is not actual fork evidence, an OS-RAM sandbox or final runner admission.
4. DesktopCryptoEngine initializes the external libsodium loader, then obtains
   `crypto_pwhash` from `LibsodiumInitializer.sodiumJna`'s native library. Bundled
   SQLite and JNA also load native code. Subsequent **historical source-data
   reads** of the libsodium/JNA/resource loader are retained in
   `LOADER-EXCERPTS.json` and `LOADER-FOLLOWUP.json`; see the explicit ad-hoc
   reader limitations in `AD-HOC-DATA-READ-DISCLOSURE.md`. JNA5.19.1 on Linux
   defaults to `XDG_CACHE_HOME/JNA/temp` or `user.home/.cache/JNA/temp`, and its
   initialization scans marked temporary files there: bind a fresh `jna.tmpdir`
   before class initialization, not only `java.io.tmpdir`. ResourceLoader uses
   `Files.createTempDirectory("resource-loader")`, grants broad permissions to
   the extracted library and requests POSIX deletion/deleteOnExit; a private0700
   enclosing root remains essential. The captured SQLite sources JAR did **not**
   contain the JVM loader. A later separately approved official JVM source read
   is qualified in `LINUX-RUNNER-CONFIG-REVIEW.md`: the resource fallback uses
   Java's default temp file API, but system/property/JDK library search precedes
   it and `org.sqlite.tmpdir` is not consulted. No runtime artifact/loader was executed or
   binary/source equivalence verified. Bind these before claiming full M03
   admission; dependency verification is necessary but not loader proof.
5. `gradle.properties` enables parallelism, build cache and configuration cache
   and requests a 4 GiB heap. Required explicit flags must override these as
   admitted: one worker, no parallel/configuration cache/daemon, no CoD,
   in-process Kotlin, bounded heap; Detekt source configuration is serial.
6. Android app configuration consults signing environment/properties even for a
   desktop task; Desktop configuration consults publisher/signing variables.
   Exclude all signing/store/private configuration and assert no untracked
   `local.properties` or injected Gradle properties. Do not collect credential
   contents as a diagnostic. No packaging or signing task is selected.
7. All-project configuration can create caches outside a single module. Pin
   every allowed output under the fresh workspace/runtime before launch, retain
   logs/XML first, and use the new independently reviewed cleanup mechanism.
   One-worker Gradle does not bound native threads or Kotlin/SQLite/Argon2 RAM.

Dependency verification remains unchanged: metadata verification true,
checksum-only policy with signatures false and the existing three narrow trust
rules. Wrapper SHA-256 remains
`acd53f1edaf02f1a8ff99879f8a34b302661a057d9b063ae9e35b552f804d20a`.
No checksum, repository order, version or dependency was changed.

## 3. PVA-031 bootstrap diagnosis: what is and is not established

The current handoff row already localizes G7 to settings plugin-marker
resolution and records an independently reviewed Gradle lenient-resolution
explanation. Its original lower-level transport/cache/metadata cause is still
unresolved. This reviewer read that row, **not the original archived stacktrace
or Gradle implementation capture**, and does not extend that historical proof.

The six read-only requests in `PUBLIC_HTTP.json` establish only current public
Python HTTPS observations. Foojay's marker (715 bytes) and module (2813 bytes)
match the existing metadata SHA-256s. The module declares a shadowed runtime,
JVM17 and Gradle plugin API7.6. Artifact HEAD availability is not artifact hash
verification. Neither this metadata nor HTTP200 proves Java/Gradle TLS,
repository resolution or the cause of G7. No test retry, new Gradle invocation,
old-cache reuse or build admission occurred. F1-F6 in the PVA-031 ledger remain.

A future separately admitted diagnostic must preserve a no-secret lower-level
marker exception/request outcome, not merely another generic stacktrace.
Account for the observed `plugins-artifacts.gradle.org` redirect. Do not turn
on debug/build scans or inspect private proxy/credential settings; do not patch
versions or trust metadata to make the generic error disappear.

## 4. Platform feasibility, not target evidence

### Android32 / PVA-001

The baseline crypto target enables host tests only. Its Android host test adds
the **JVM** libsodium runtime; such tests cannot establish Android32 ABI
correctness. No baseline device-test runner/source set or instrumentation
dependency was found. A separate no-new-runner-dependency two-file harness was
independently source-accepted in `ANDROID-FINAL-REVIEW.md`, bound by
`ANDROID-FINAL-INPUTS.json`; it is not an executed test or admitted device run.
Any later test-only ABI manifest needs its own supplemental review.
That later three-file source disposition is now retained separately in
`ANDROID-MANIFEST-SUPPLEMENT.md`/`ANDROID-MANIFEST-INPUTS.json`; no execution or
merged-manifest evidence is implied.

The SDK directory contains platform37.0, with `AndroidVersion.ApiLevel=37.0`,
and build tools34/35/36. Only an Android35 default x86_64 system-image directory
was observed. No `/dev/kvm` entry was observed. These directory observations
neither verify SDK completeness nor authorize an emulator/ADB/device. A process
must prove 32-bit pointer/size_t and load the real Android adapter; emulator model
or x86_64 host labels alone are insufficient. ARM32 remains its own ABI gap.

### Windows native

Standalone `app-desktop/native/biometric-bridge` CMake can bypass unrelated
Gradle bootstrap. It requires CMake>=3.25, C++20, MSVC/Windows SDK WebAuthn+WinRT
headers and the named system libraries. CMake does not select an SDK version;
the required PRF/WebAuthn API8 structure constants must be present in the pinned
runner toolchain. No SDK download or compile was attempted here.

There are fourteen Windows CTest declarations: ABI, security, eight file cases
and four borrowed-secret guards. Tests and their safety/real-provider boundaries
require the native reviewer's separate review. They do not prove Hello UI or
enrollment on hosted machines.

If `:app-desktop:desktopTest` is used instead, its Test tasks **also depend on
CMake bridge build, CTest and staging** on Windows/macOS. The Gradle CMake build
uses bare `--parallel`; bind a native worker limit or independently correct that
configuration. Root's native agent proposes standalone explicit `--parallel 1`
instead. Do not use packaging workflows or sign/copy a replacement candidate.

### Apple

Source declares iOS arm64 and arm64-simulator targets; crypto creates rawSodium
cinterop from the checked-in header/definition. Shared frameworks are static.
An iOS simulator compile may validate Kotlin/cinterop syntax on a suitable
Apple Silicon macOS/Xcode SDK, but does not execute tests or verify physical
iPhone security, Keychain protection, clipboard behavior or prompts.

The Xcode application build phase calls a wrapper verifier and
`:shared:embedAndSignAppleFrameworkForXcode`; avoid that path for a pure,
non-signing Kotlin compile check. Prefer explicitly scoped compile/native-test
tasks under fresh admission, with pinned JDK17/Kotlin/Xcode/SDK/architecture and
no production signing inputs. Current Xcode source requests iOS18.5 minimum.
Native macOS CMake compiles Objective-C++ with Foundation, LocalAuthentication
and Security frameworks; host test code is not physical-device proof.

## 5. Resource/cleanup/limits

At approximately20:39 UTC, `df -Pk` showed 8,405,592 KiB free on the worktree
volume and 7,303,280 KiB on `/tmp`; `free -m` showed 26,099 MiB available of
64,311 MiB, no swap. Both volumes remained below the12GiB launch floor. These
are bounded observations, not a reservation; growth from earlier observations
was **not** reclamation by this agent. No shared cache/toolchain was removed.

At21:20:27 UTC a later `df -Pk` observation showed29,819,460KiB free on the
worktree and18,591,664KiB on `/tmp`; `free -m` showed43,009MiB available, no
swap. Both had risen above12GiB, **not through reclamation by this agent**.
This does not overwrite the earlier low-space observations or reserve capacity.

Only source/configuration/data reads, hashes and bounded public HTTP reads ran.
No Gradle, compiler, application test, native build, application/recovery script, emulator,
ADB server or persistent worker was started. HTTP handles closed and their
short-lived interpreter processes exited. There are no disposable build outputs
from this task; wrapper `--stop` is NOT_APPLICABLE, not settlement of an old gate.
Only compact reports/metadata are retained. Referenced skill helper scripts
remain absent/unexecuted; no authoritative repository was invented. Two earlier
ad-hoc data readers lacked independent admission and were not equivalent to the
supplied transport tool; `AD-HOC-DATA-READ-DISCLOSURE.md` records that gap, their
bounds and their missing byte-exact saved script provenance. Subsequent archived
source reads use the separately reviewed Git data reader under root's admission.

Two9.4 API documentation reads were stopped at a2.5MB body cap and a candidate
official raw-source locator returned404. These are documentation-inspection
failures, not application tests; no exact DSL proof is inferred from them.
The amended target-API read and two distinct9.4 builder/device API reads later
succeeded within root-approved bounds. `ANDROID_API.json` preserves both the
failures and the independent public-contract evidence; it is not plugin execution.
One shell inspection also requested a nonexistent `raw_sodium.h`; its exit1
is preserved here. The actual checked-in header is `passvault_sodium.h` and was
subsequently read. Later source lookups for a nonexistent
`LibsodiumCryptoEngine.android.kt` and `config/dependency-license-inventory.lock.json`
also failed; actual paths are `AndroidCryptoEngine.kt`/`RawPasswordHash.android.kt`
and `legal/third-party-dependencies.lock`, and those were then read. One report
patch was rejected because an expected context line did not exist; it made no
partial file edits and was corrected. These are inspection/editing failures,
not application test failures or product findings.

All STOP/NO-RETRY/CLOSED restrictions, PVA-029 FAIL, the eight separate PVD
decisions and existing build1017001 remain unchanged. The next build gate is
**BLOCKED**, not admitted by this configuration report.
