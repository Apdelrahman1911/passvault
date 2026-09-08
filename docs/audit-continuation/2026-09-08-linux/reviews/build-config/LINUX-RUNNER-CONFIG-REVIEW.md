# Linux database runner: focused graph/environment challenge

Reviewer `/root/build_config`; runner author `/root/storage`.
**SOURCE REVIEW; M03 PARTIAL; NOT EXECUTION OR WHOLE-RUNNER ADMISSION.**

Reviewed runner snapshot:
`scripts/audit/linux_database_validation.py`, SHA-256
`60f639d95f2c7a6440b71955a59a8867f43e19daf572bb755f622216d9e5477e`.
Reviewed PLAN snapshot SHA-256:
`82429ac1eb77347f5a1c66710fcf93ce34912c89bdf30ce81c722de93a5f7dfc`.
Scope: full initial255 runner lines, focused environment/command call sites and
PLAN; this is **not** an independent lifecycle/cleanup implementation review.
Root and `/root/verification` own that acceptance. Later revisions need rebind.

## Accepted source contracts

- Selected checkpoint is the preserved9bdf/05014 tree, not a compilation/test of
  concurrent editor/Android/native continuation changes. Seven database class
  filters/105 source-declared methods match the original matrix. Existing
  `REVIEW.md`/`INPUTS.json` bind the source-derived Gradle/module graph; plugin
  realization remains unobserved. No root `test`, `check`, legal inventory,
  packaging or app launch is substituted.
- Exact argv includes checked-in wrapper, one worker, non-daemon, no parallel/
  CoD/configuration-cache/build-cache, in-process Kotlin and STRICT dependency
  verification. The2GiB daemon heap versus inherited512MiB worker default has
  the independently qualified JDK17 ordering in `JDK17-OPTIONS-REVIEW.md`.
  Later worker overrides and actual JDK command lines still require evidence.
- The environment is generated rather than copied from `os.environ`. It has
  fresh HOME/Gradle/Konan/XDG/Android-user/TMP and pre-initialization Java/JNA
  properties, with no ambient signing/store or JVM-option variables. Retained
  positive owned argv may be examined; no unrelated environment/command dump
  or process attach is authorized by this review.

## Rejected provisional toolchain objection

Before looking up the exact9.7.1 contract, this reviewer raised a **provisional**
concern that toolchain options needed `-P` rather than `-D`. The official9.7.1
toolchains guide **disproves that concern for both booleans**: it explicitly says
to start Gradle with `-Dorg.gradle.java.installations.auto-detect=false` and
`-Dorg.gradle.java.installations.auto-download=false`. **Keep those `-D` flags**;
do not silently turn the initial concern into a confirmed runner defect.

`LINUX-M03-PUBLIC-SOURCE.json` retains the exact official doc response/hash and
excerpts. The same read documents `org.gradle.java.installations.paths` in
`gradle.properties`, but did not establish its command-line `-D` versus `-P`
plumbing. This reviewer neither rejects the existing `-D...paths` flag nor
claims the unresolved CLI mapping is proven. No fallback/version change or
Gradle `javaToolchains` command was run.

## SQLite JVM source gap narrowed, not hidden

The earlier common/native source JAR did not contain a JVM implementation.
A separately root-authorized read now obtained the official Google Maven
`sqlite-bundled-jvm:2.6.2` source JAR (21,152bytes, SHA-256
`3749d31ce6ca154e91e842f3b9b8ce39bee897b8d9dc3526ef053b42b23bae8d`).
Its complete5,516byte `NativeLibraryLoader.jvm.kt` has SHA-256
`f41b2db9551afa478c2eedd2d7ed49b447a2ffe74f0133275fcb1622197d840a`.
The source/receipt is retained in `LINUX-M03-PUBLIC-SOURCE.json` without extraction
or import. The compiled artifact's existing verification SHA remains untouched.

Its order is:

1. `System.loadLibrary(name)`;
2. optional `androidx.sqlite.driver.bundled.path` and `.name` properties;
3. a matching library under the running `java.home/lib` on Linux;
4. native resource extraction with `Files.createTempFile("androidx_$name", null)`
   and `deleteOnExit`, then `System.load(canonicalPath)`.

Thus **the resource-extraction branch** uses the worker's Java temporary
directory. This loader does **not** consult `org.sqlite.tmpdir`; that property
cannot establish loader confinement. `SQLITE_TMPDIR` concerns a different
native SQLite temporary-file boundary, not this Java extraction branch.

The fresh temp/JNA/home/XDG properties address the known extraction destinations,
but do not by themselves prove which native file was loaded. Bind the reviewed
normal toolchain/library-search trust boundary or explicitly confine relevant
search paths and reject conflicting candidates; do not relabel a generic TMP
setting as bundled-binary provenance. No compiled native bytes were loaded,
mapped or compared with source. The appropriate honest qualification is a
source contract plus future strict verified runtime evidence, not imaginary
binary/source equivalence or an OS sandbox.

## Remaining concrete M03 qualifications

- Confirm exact `installations.paths` command property semantics if relying on
  it as an independent exclusion control; keep toolchain auto-detection/download
  disabled and actual pinned JDK evidence in all cases.
- The PLAN calls `/opt/android-sdk` read-only configuration input, but ENV alone
  does not enforce that. AGP can resolve platform/build-tools during all-project
  configuration. A subsequently root-authorized exact9.4 source read established
  `BooleanOption.ENABLE_SDK_DOWNLOAD("android.builder.sdkDownload", true,
  FeatureStage.Supported)` at line137. `SdkComponents.kt` line347 reads that
  project option, and line112 passes `!offlineMode && enableSdkDownload` to
  `SdkLibDataFactory`. **Add `-Pandroid.builder.sdkDownload=false`** using the
  ordinary Gradle project-property CLI contract. The option parser implementation
  was not separately read, but the actual named option/default and SDK-service
  handoff were. `AGP94-SDK-DOWNLOAD-SOURCE.json` binds the evidence. This disables
  that source-reviewed automatic SDK-download path, not arbitrary SDK writes or
  an OS-level read-only sandbox. Root may explicitly trust normal installed
  JDK/SDK inputs while retaining that limitation. No SDK install/permission
  change or toolchain substitution was performed here.
- Preserve source-only versus actual worker/classpath/native-artifact evidence.
  Linux process/JDK/source hashes, seven XML filenames and command0 do not
  substitute for all105 exact semantic case results or reviewed cleanup.
- The source-qualified JNA/resource-loader temp policies still have their
  original ad-hoc-reader disclosure in `AD-HOC-DATA-READ-DISCLOSURE.md`. The new
  official SQLite read does not retrospectively strengthen those earlier tools.

The two public reads used verified TLS, explicit empty proxy handling,
12seconds/request,2MiB/1MiB body caps, zero retries and an outer45second timeout.
Only35 source-JAR members/2MiB total expansion were permitted; selected text was
bounded at100,000bytes per member. All handles/interpreters ended. Only compact
receipts/source excerpts remain; no executable or application test ran and no
wrapper stop was due. All existing restrictions and closure denominators remain.

The additional single authorized AGP read returned a4,032,871byte sources JAR,
SHA-256 `d462c399729eb35241208899f937037e40f2f9157a54fa9c455595ed6ceca091`.
It used an8MiB compressed/32MiB total expanded cap,12second request/35second outer
timeout and zero retries; selected members were at most250,000bytes. No archive
was persisted/extracted/imported. Only option/service excerpts and hashes remain.

## Minimal proposed option disposition for the final author revision

Keep explicit JDK17 `JAVA_HOME`/wrapper and generated inherited512MiB/private
properties; keep daemon2GiB and the existing serial/strict/no-cache flags.
Keep the two `-Dorg.gradle.java.installations.auto-*` false flags now documented;
add `-Pandroid.builder.sdkDownload=false`. If retaining `-D...installations.paths`,
do not rely on its unestablished CLI plumbing as the sole runtime binding.
Actual JDK/worker evidence remains mandatory. No project runtime/toolchain
declaration was found beyond bytecode target17. Do not add speculative native
search-path flags or inspect arbitrary host libraries merely to hide the
explicit normal-toolchain/native-provenance trust boundary.
