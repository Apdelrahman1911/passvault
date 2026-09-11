# AndroidCompile03 independent actual reconciliation

Reviewer: `/root/android_compile_review` — 2026-09-11.

**Disposition: ACCEPT_COMPILE03_ACTUAL_AND_ORIGINAL_SETTLEMENT, compiler-only.**
No outstanding blocker from this original instance. Root may record Compile03
consumed/settled and release its scheduling slot. This is not admission of another
run, a current global-idleness finding, or clearance of any older held runtime.
No replay, additional stop, or cleanup is authorized.

`B = docs/audit-continuation/2026-09-08-linux` under W;
`E = B/runs/linux-android-compile03`; `O = B/reviews/android-compile03-outer`.
Only bounded W/retained-evidence JSON/text/hash comparisons were performed here.
No build/test, helper evaluation, Git/tool/SDK/process probe, runtime descent,
stop, deletion, or repeat audit of unchanged source/index controls was performed.

## Retained evidence and original terminal

Independently inventoried exactly **42 regular, root-owned, single-link 0600
files / 659337 bytes**, confined to E and its named `logs`/`reports` directories.
All 28 checked retained image references match both full-eight metadata and SHA256;
embedded allocation, index, phases and compiler evidence match their standalone
copies. Inventory commitment (UTF-8, sorted E-relative names, each line
`<sha256>  <name>\n`):
`826da6e2209eb5617d51bfb7c842325e623c4b753b1a1ead002743868e64ca8c`.

| Evidence | SHA256 |
|---|---|
| O/ROOT-EXECUTION-ADMISSION.json | ab316b4c56f4669d680223bf7a5f38accb0e72fcf8b827357406bc08ef5aa98e |
| O/EXTERNAL-RESULT.json | 145ef33c07dab09a6e0ed5e9d6e72825b72683f1e4a5dd51c23c3e2b1dbfc0f6 |
| E/OUTER-RECEIPT.json | 07c3d0b6948142746c058d2e22710604964152bc646d6bf4e591d43feda13d61 |
| E/INNER-RESULT.json | d53cab2284b53d2615cca97a70b7d521adebe00d72fd3fe2d80dfd0fb28d780a |
| E/COMPILE-EVIDENCE.json | 2a8e0c5b029bd285e1086323b45666ae597e51582e8c9b3d0a4ac35114508e9a |
| E/logs/compile.log | 8b1cc9d5e0627e8b400c5c2b79d81dd4c97df42e5157917affb705ddbee8f753 |
| E/logs/compile-stop.log | bbdce9bdc43d437eea71045935f07af31f85d5b0a70fda123395882cac48e9fc |
| E/SOURCE-BEFORE.json = SOURCE-AFTER.json | d03da1d5b5f80d0a54e8ae6abea23cee5e308e5e72a1b1437572d4c3c96e6d70 |

The original external sequence is launch `096e59` / session `57945`, poll
`ac7304` on that session, final `7c0314` **exit 0 with no remaining session**;
all returned output is empty. This is completion evidence, not an argv echo.
Command authority remains the exact root admission. Its request SHA256
`c93cdbcf29c76d53c9ab040ca0021d24f94d4ccb451171cf6dc268302856bb45`
and genuine approval SHA256
`366f7c8a416d5d622f5051de795757f58f953e3d049cc0470430d96cb050d666`
match the actual packet records. The outer receipt is explicitly preterminal;
the original final tool return supplies terminal completion. Tool wait durations
are not total elapsed time. The retained pending-independent-review status and
`independent_semantic_acceptance:false` are not themselves acceptance; this
separate review supplies the qualified disposition without altering evidence.

## Actual graph and fresh compiler evidence

Original log marker payloads plus LF match all ten saved init files byte-for-byte
and their receipt line numbers: 3508, 3509, 3510, 3696, 3705, 3706, 3795, 3805,
3806, 3808. Collection/header/validation errors are empty.

**14 observed graph nodes and 14 unique task headers, within a 26-name ceiling**:
10 bare, 3 UP-TO-DATE, 1 SKIPPED; the remaining 12 allowed names are UNSTARTED.
The footer is `BUILD SUCCESSFUL in 2m 37s` and `10 actionable tasks: 10 executed`.
Neither 26 nor 14 is the count of freshly executed compilation tasks/test cases.
Admission prose saying “Exact26-task graph” must not become an actual-26 claim.

MAIN = `:core:crypto:compileAndroidMain`; DEVICE =
`:core:crypto:compileAndroidDeviceTest`, the sole requested selector.
DEVICE directly depends on MAIN. `:core:crypto:androidJar` is enabled, exact type
`org.gradle.jvm.tasks.Jar_Decorated`, with empty dependencies/finalizers/
mustRunAfter/shouldRunAfter; its only incoming relation is DEVICE.dependencies.
There is no separate Jar fresh-action or archive-content oracle.

Both MAIN and DEVICE have bare headers, executed/didWork true, skipped/upToDate/
noSource false, null failure/skip fields, and all three declared output roots
absent before execution. Finished receipt lists exactly MAIN and DEVICE, evidence
complete, failure null, cases 0. All **27 selected emitted-class rows** match the
original readback records in task/path/size/hash/header: 16 LibsodiumCryptoEngine
family and 11 Android32KdfInstrumentation family. Every header is
`cafebabe0000003d` (major 61). `generatedFileCount` 170/110 is not a retained-class
count. Binary bytes were not retained and were not rehashed from the disposed R.

| Principal selected class | Bytes | Recorded SHA256 |
|---|---:|---|
| android/main/.../LibsodiumCryptoEngine.class | 19832 | ccbfa3958c278d837e5aff72f66d639575c8edd635878e145d19df87d9aaed87 |
| android/deviceTest/.../Android32KdfInstrumentation.class | 27123 | 4d31a49bbb7ca1fdfffd340c10c9f306a803ca4c090da8eb1cb8f2619791c568 |

Compile success is not warning-free: fixture line 149:50 reports `Any?` versus
`Any`; native-target configuration warnings do not demonstrate native execution.

## Exact source inputs

Commit `6489252e88ad553a867d67578eff45a402e62a48`, tree
`57d338a931ab0fb4e072aabcbbfd27bead8ef08a`, C18 SOURCE SHA256
`a34aee1c7ea19fb693adf67437ec67e8b9e0ee34232936cb76127764c7b242d3`.
Generic MAIN/DEVICE input receipts contain 32/28 file records. Their eight source
inputs all match C18 raw sizes/hashes: MAIN's five commonMain files
CryptoEngine, LibsodiumCryptoEngine, PaddedPayload, RawPasswordHash,
SecurePasswordGenerator, and two androidMain files AndroidCryptoEngine and
RawPasswordHash.android; DEVICE's sole source is Android32KdfInstrumentation.
The four required inputs also match their independent original source-readback
records (paths below are under `core/crypto/src/`, package `com/passvault/core/crypto`):

| Source set / file | Bytes | Raw SHA256 |
|---|---:|---|
| commonMain/kotlin/.../LibsodiumCryptoEngine.kt | 13666 | 2b8dd21fe476735f16f1ab35543417a70202e7923dfac328f9064e31f5923b57 |
| commonMain/kotlin/.../RawPasswordHash.kt | 2519 | 3fb16abe001752bf44b5a56039c58738ebed0e6ced5972e3ca0985d5a40586eb |
| androidMain/kotlin/.../RawPasswordHash.android.kt | 2837 | d2c4bcd22df2cb89a97e10a4b591d651f224f0b1c5224bc6b5de10f407d0df1c |
| androidDeviceTest/kotlin/.../Android32KdfInstrumentation.kt | 14600 | 9c4567e4a9cd1f6023534d3b5a40dfb21981cd49cc8eff0b4d1e453429246ca8 |

These are generic declared inputs, **not compiler classpaths or per-file
visitation**. Classpath attribution remains NOT_ESTABLISHED.

## Source/store/SDK and original settlement chain

- Byte-identical before/after source receipts report 3432 members / 113336811
  raw Git-blob bytes, not checkout EOL bytes; both historical EOL qualifications
  remain unchanged. These are aggregate successful check receipts, not a retained
  second full source image. Ordered C18 OIDs independently reproduce 140712 bytes
  (within 144 KiB; 3432 within 3500) and hash
  `ddee058c4a4cf9a8ab1eb63a323ab0b10dab19a4c4a43214f95e17bc2ff5fb64`.
  Batch framing arithmetic gives 113515367 bytes; both original response records
  agree on `11b749e0038eeb0acf68c9fbcc5b33889d3c7aba82823aaecda48d39c62a8c42`.
  This compares recorded transport evidence, not a new runtime stream read.
- Actual allocation/packet identities and recorded source images reconcile to
  the previously accepted instance. The sealed 553274-byte 0400 index copy and
  source index agree on
  `372f4404428ad77b58ca0bd5580318dcbe46b7ecba8de6231114c07d8eb360ef`.
  Metadata/index mounts are ro,nosuid,nodev,noexec; all five admitted SDK metadata
  image records match, SDK root inode 57, private view ro,nosuid,nodev with exec
  intentionally allowed. No new SDK install/copy/license assent is inferred;
  read-only aliases are not immutable snapshots against outside writers.
- Six isolation commands complete exit 0. Isolation setup/settled are true;
  build_ok/stop_ok false are expected because this phase requires no stop.
  Compile and its original stop both complete exit 0; intents/logs agree on the
  original wrapper/cwd, admitted flags, 900s/600s budgets and environment digest
  `c8cdb28ef4c00d725ea328770b6a4a4d52e0c034a9d70a9d267ebf286013b172`.
  Stop log says no Gradle daemons are running, scoped to that original environment.
- Inner source/Git/SDK/index stability, required stop, namespace-empty and
  cleanup-safe flags are true, errors empty. Both original outer children exit 0,
  without pidfd kill. Private pid/mnt 4026532116/4026532115 differs from parent
  4026531836/4026531841; nonpropagation and original parent identity agree.
  Parent mount guard records 10 rows. Original underlying metadata/SDK target
  identities are restored empty; the sealed index image is unchanged.
  Outer cleanup records **REMOVED_ORIGINAL_RUNTIME, 14298 snapshot entries,
  14298 removed**, with no reasons. This reviewed original receipt chain plus
  original terminal 0 supports settlement, not a new live absence/ownership probe.
- Outer elapsed 174.96s, six resource points: minimum MemAvailable 62044811264
  bytes; disk available 16680173568 bytes. Eight complete inner samples, zero
  vanished/incomplete counts; each satisfies files + directories - 1 = examined
  entries. Maxima: 9152 files, 4769 directories, 13920 examined entries,
  959829134 logical bytes. Point samples are not continuous peaks, ownership,
  cleanup or hard-deadline proof. Host churn is **11**, not zero; no exhaustive
  global-idle/no-escape claim follows.

## Claim boundary

**Zero tests, runtime KDF/vector/native32/ABI, instrumentation execution,
APK/DEX/shrinker/packaging, physical-device, GUI/native, publication or closure
credit.** Existing consumed/HOLD/STOP/NO-RETRY/CLOSED/native-refusal/protected-ref/
publication/build1017001 fences remain unchanged. Any later cohort requires its
own admission; no older runtime clearance or fresh host state is inferred here.
