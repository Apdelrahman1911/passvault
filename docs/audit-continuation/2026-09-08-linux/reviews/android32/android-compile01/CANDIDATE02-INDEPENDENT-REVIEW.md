# Android compile01 — candidate02 independent source review

Reviewer `/root/android32`, 2026-09-10; init author `/root/editor_review`.
B = `docs/audit-continuation/2026-09-08-linux`. Root owns actual execution and cleanup.

**ACCEPT CANDIDATE02 SOURCE under the explicitly adopted ordinary-trust scope.**
This is not execution admission, syntax/runtime validation, SDK usability evidence,
or a complete outer-runner review. `ADMITTED` remains null; its fresh readRoots/offline
literal and the actual source/command/instance/coordination/cleanup binding need
independent review before the one proposed compiler cycle. No separate graph-only
cycle or automatic observation-to-expansion/retry is introduced.

## Concrete implementation challenge

Read all 180LF of `android_compile_02.init.gradle.candidate` and its 106LF note.
Text-only extraction independently matched exactly 25 unique allowlist names to
my preceding finite-envelope review and to the retained Linux03 registrations.
This is source/data checking, **not 25 tests or an observed execution graph**.

- Lines13–23 admit exact `:core:crypto:` task paths only. Both compile/resource
  packaging-named exceptions are explicit. There is no sibling/root wildcard or
  generic compile/check/package permission. Test/Exec/JavaExec guards supplement
  that finite scope. Actual task types are recorded, not independently proven safe.
  Hidden malicious behavior inside trusted plugins is not excluded by task names
  or types; frozen-source/strict-verified-plugin ordinary trust remains explicit.
- Lines55–67 reject dry-run, additional/changed selectors, exclusions, continue,
  rerun, parallel/CoD/multiple-worker/cache/verification/acquisition mismatches and
  a bound offline-policy mismatch. The outer command still owns wrapper identity,
  daemon/configuration-cache suppression, effective toolchain and environment.
- Lines68–84 capture the ordered actual graph, concrete types, enabled states and
  dependency/finalizer/mustRunAfter/shouldRunAfter constraints before task actions.
  Unbound scope or an out-of-envelope task fails without adopting it. Only the two
  compilers must be enabled; disabled safe listed prerequisites do not manufacture
  a failed cycle. Both compilers must occur in the graph. The hook does not itself
  compute transitive MAIN-to-DEVICE dependency reachability: review the retained
  actual edges rather than claiming that stronger property from membership alone.
- Lines85–144 derive output declarations from public `Task.outputs.files`, confined
  to the module's generated boundary, with absent/empty checks before actions.
  Input/output reads are bounded and symlink-checked; regular-file size/mtime/key
  are compared around streaming hashes. This is not descriptor-anchored protection
  against hostile concurrent writers. Root must establish fresh isolated state
  and exclude writers; the output boundary is not cleanup authority.
- Lines145–157 retain generic declared inputs and independently pinned source
  hashes. They correctly say **NOT compiler classpath or visitation**. The four
  current source hashes were independently reread and match the candidate. No
  unsupported Kotlin getter, runtime-classpath or variant-resolution API is used.
- Lines159–178 retain compiler success/skip/noSource/cache/failure facts and refuse
  stale, missing or skipped completion. The freshly emitted base-class checks and
  same-package `$` companions correct candidate01's substring match. Expected
  `cafebabe0000003d` is class-header evidence for JVM17, never class loading.
  The audit doFirst action and didWork alone remain insufficient; correlate the
  original compiler action's bounded `--info` log with inputs and new class receipts.
- Lines46–50 install terminal reporting before other guards and reject an otherwise
  successful build lacking both accepted compiler completions. This closes the
  candidate01 dry-run/empty-completion counterexample at source level. No such
  counterexample was executed; Gradle/Groovy compatibility remains untested.

The collector retains the bounded limits documented by the author:64 task nodes,
16 read roots,16 output declarations/compiler,4096 input roots/compiler,16384
traversed entries,256MiB/file,2GiB size-accounted hash reads,1MiB/event and8MiB JSON
payloads. A growing file is rejected on the next read chunk (up to64KiB beyond its
announced size); these are not kernel RSS/disk/time caps or hostile-writer isolation.
The outer16MiB log cap includes prefixes/newlines/Gradle output. It must preserve
failure receipts even if collection, resolution or an original compiler fails.

## Installed SDK facts and independently challenged use decision

Root's two metadata-only receipts establish these **recorded metadata facts**, not
complete usable installations or an AGP resolver result:

- Literal package `platforms;android-37.0`, API text `37.0`, revision2, extension22,
  base SDK, no preview codename; its package references `android-sdk-license`.
- Literal `build-tools;36.0.0` metadata, referencing that same license identifier.
- Existing regular41B acceptance marker remains SHA256
  `c43fa37686457c3f18caa3607945f4ec52a9d1beaaad8117e50dc4e863270c85`.

Absent `android-37` is not evidence that `android-37.0` is unusable. Do not rename,
link or normalize these files, infer build-tools37 is required, or declare every
compiler path externally blocked. AGP9.4 recognition, selected/default/minimum
build-tools compatibility and actual tool completeness remain empirical gaps for
the one scoped compiler cycle with SDK auto-download=false. Preserve any failure;
there is no install, license command or automatic retry/fallback authority.

Root explicitly bound the user's Linux remediation/validation authorization to
ordinary **read-only use of the supplied already-installed tools**. I independently
challenged that decision against the user restrictions, handoff PERMISSIONS and the
earlier Android32 prerequisites: **no explicit current restriction bars that narrow
existing-tool use once fresh execution admission is established**. Accept this
operational use decision without inventing a requirement for perfect historical
principal/contract attestation. The unchanged marker is not proof of its principal,
exact terms, legal compliance or a new acceptance; neither is uncertainty proof
that no existing agreement exists. No contract is accepted/reaccepted by this review.

The earlier prerequisites expressly offered independently reviewed read-only use
instead of blanket copying (lines51–56). Their selected new API24 image agreement/
copying question remains unanswered. To the extent broader wording could be read
to require new attestation before *all* existing SDK compilation, this explicit
reviewed decision narrows that interpretation; it does not erase the historical
note or resolve the new-image question. No SDK copy/install/marker change,
sdkmanager/licenses, emulator/image fallback, private signing or device operation
is permitted here. “Read-only use” is the admitted operation scope, not proof of
OS-enforced read-only mounts; the outer review must state actual enforcement.

## Exact bindings

| Input | SHA256 |
|---|---|
| `B/reviews/editor-independent/android-compile01/android_compile_02.init.gradle.candidate` (11921B / 180LF) | `aafa194609929b85ab975de27cdaf5e29766e81f3c02f5d2e21b119d76bc6bee` |
| Same directory `IMPLEMENTED-CANDIDATE-02.md` (7244B / 106LF) | `4009a381d29f900ee730db0f7c8cc37500260335c7a9663023c55470f8f0b630` |
| `B/reviews/android32/android-compile01/CANDIDATE01-FINITE-ENVELOPE-REVIEW.md` | `d1ff803ecbf17e69c8f02f3a099ef672fa85d0711a8766e4399bdda7b3748ab9` |
| `B/reviews/android32/compile01-sdk-metadata/SOURCE-FACTS.json` (1834B) | `d88abbbdac74b1e64e04eb20c48803a00b0c5ebb868171f519ecf3a32af5dd2a` |
| Same metadata directory `DISCOVERED-PACKAGE-FACTS.json` (3912B) | `7b05770303fb617afdbbd76eda7d09aabde02efcd65e1a06c9bafb8df3cf6bac` |
| `docs/audit-handoff/PERMISSIONS.md` | `3bde2eab96c83cbf313bd8f5cf184a08212cc82c67288c9c735f863ef0bd5723` |
| `B/reviews/android32/ANDROID32-ADMISSION-PREREQUISITES.md` | `5c2966372abb1aebb56175a7062e4ceef8fa990875a2c6d332c9b136e3397089` |
| `core/crypto/src/commonMain/kotlin/com/passvault/core/crypto/LibsodiumCryptoEngine.kt` (13666B) | `2b8dd21fe476735f16f1ab35543417a70202e7923dfac328f9064e31f5923b57` |
| `core/crypto/src/commonMain/kotlin/com/passvault/core/crypto/RawPasswordHash.kt` (2519B) | `3fb16abe001752bf44b5a56039c58738ebed0e6ced5972e3ca0985d5a40586eb` |
| `core/crypto/src/androidMain/kotlin/com/passvault/core/crypto/RawPasswordHash.android.kt` (2837B) | `d2c4bcd22df2cb89a97e10a4b591d651f224f0b1c5224bc6b5de10f407d0df1c` |
| `core/crypto/src/androidDeviceTest/kotlin/com/passvault/core/crypto/Android32KdfInstrumentation.kt` (14155B) | `d9bdb00637773f5d239ffcc5f7d13dc3773e528ef0dec1b1efdffec0ee182310` |

## Limits and accounting

Only source/retained-evidence reads, hashes, text-only set comparison and this
permanent report were performed. No candidate Groovy parsing/import/execution,
JVM/Gradle/build/test, live SDK/process/cache probe, network/Git/CI, runtime output,
temporary cache or background worker. No wrapper-stop obligation arose here.

Zero application/native/hardware cases, fixes, closures or denominator changes.
No claim that the291LF harness compiled yet. A later accepted compilation can close
that compilation gap only: four intended native cases/four KDF calls, Android32
process/JNA reflection, APK/DEX/ABI payload, business flows, minification and genuine
hardware remain separate. Eight PVD boundaries (especially PVD002 lowercase-hex
compatibility), PVU007 STOP, PVU011 NO-RETRY, PVA029 FAIL/no automatic retry, G7/G8
CLOSED, all consumed/HOLD scopes and non-publishing/occupied1017001 restrictions
remain intact. Do not import old runners or replay any closed helper.
