# Android32 fixture Detekt03 — unapplied proposal

**UNAPPLIED; source-only proposal, not an analyzer/compile/device pass.**
Only `core/crypto/src/androidDeviceTest/kotlin/com/passvault/core/crypto/Android32KdfInstrumentation.kt` is proposed to change.
C17 source/tests/helpers remain frozen for GUI03. Root alone may reconcile that
freeze, accept independent review, apply an exact delta and admit validation.

## Identity and retained diagnostics

- Before: 14155 bytes, SHA256 `d9bdb00637773f5d239ffcc5f7d13dc3773e528ef0dec1b1efdffec0ee182310`.
- Proposed after (in memory only): 14600 bytes, SHA256 `9c4567e4a9cd1f6023534d3b5a40dfb21981cd49cc8eff0b4d1e453429246ca8`.
- Patch: `ANDROID32-DETEKT03.UNAPPLIED.patch.txt`, 2525 bytes, SHA256 `7a3cc856f383c35570d17d68475593a0ba45771091657dffe0ef25cbbe7b0074`.
- Current bytes match the fixture tuple in
  `reviews/checkpoint17/source-prepare01/SOURCE.json`, SHA256
  `e03d29e54974413ff893f96d9bcf0c70bbf11eeeaf114ac96296987456db1f7f`.
  That retained C17 manifest records commit
  `0563e31adc9a66aefc3e74b99b9a24d17bdcdd49`, tree
  `d1bd6ca5b18d08ff3ff15896d9a78af9016be799`; no Git observation was made here.
- Detekt03 `STATIC-REPORTS.json`: SHA256
  `a202cece16b23417c6f436e2ba100c8c00f74cabb1454debd5867720c6a2dfeb`.
  The three retained crypto report hashes match its saved descriptors.
- Exact findings in `runs/linux-detekt03/reports/core-crypto-checkstyle.xml`
  (SHA256 `aeacb8d58f3712d5dfb80f7409bc335127a2f0b0e9344a99112e6d3ef30c75b1`):
  ComplexCondition 186:25; NestedBlockDepth 174:17;
  TooGenericExceptionCaught 70:22 and 194:14. Four diagnostics, not four bugs/tests.

## Minimal correction and two intentional boundaries

1. Extract only range membership into private `mappingContainsAddress`.
   The loop loses its two nested range/null guards; numeric parsing is unchanged.
   The helper has two return statements and conditions of at most two operands,
   avoiding a guard-return proliferation while preserving half-open comparisons.
2. Suppress only `TooGenericExceptionCaught` on `onStart`, with an inline reason.
   This single Throwable catch is the terminal instrumentation adapter, not a
   production recovery catch: AssertionError emits -2, all other caught failures
   (including linkage/cancellation) emit -1, then RESULT_CANCELED and immediate
   return. Narrowing it would change failure reporting. No failure earns a pass.
3. Suppress the same rule only on `functionMapping`, with a separate reason.
   This non-suspending optional metadata observer retains **Exception**, not
   Throwable: observer-only read/parse exceptions remain explicit NOT_ESTABLISHED;
   Errors still propagate to the terminal test boundary. Narrowing this catch
   could turn an existing evidence-only RuntimeException gap into a native-case
   failure. No file/class suppression, baseline, config exclusion or new test.

## Preserved contracts and source challenge

- Four fixed IDs/order unchanged: `nativeRuntimeIs32BitAndLoadsSodium`,
  `historicalBinaryPasswordVector`, `historicalTextPasswordVector`,
  `productionProfilesMatchReferenceVectors`. No new permanent test or case.
- First-case Process.is64Bit and JNA POINTER_SIZE/SIZE_T_SIZE gates, actual
  sodium/crypto_pwhash adapter discovery, version observations and address mask
  are unchanged. A 64-bit process still fails before KDF work.
- All pinned vector bytes and production deriveKey calls are unchanged: historical
  binary/text cases, then **both** ops3 and ops4 at64MiB. Production lowercase-hex
  compatibility and size_t routing are untouched; no fake or direct-JNA bypass.
- Cases/start/pass status order, unsupported argument rejection and terminal
  counters are unchanged. Setup remains within the existing try/finally ownership
  in deriveAndCheck; derived/expected/salt/password clearing order is unchanged.
- Mapping still checks at most8192 rows, rejects >4096-char rows, closes its reader
  with use, takes the first matching row and labels it OBSERVED_NOT_ELF_BOUND.
  Missing delimiter, missing/non-hex/overflowing bounds remain nonmatches. The
  unchanged predicate accepts start, excludes end and rejects reversed/empty
  intervals; it performs no endpoint subtraction, so no new overflow edge.
- Standard-library source-data comparisons confirmed six protected spans
  (inventory/setup, terminal body, status/native gate, mapping read prefix,
  mapping outcome/catch, and vectors/cleanup/constants) byte-for-byte unchanged;
  proposed lines remain <=120 characters. This is not Kotlin or analyzer execution.

## Risk remaining and smallest meaningful validation

Independent exact patch review is required. The extraction/null-flow and exact
suppression recognition still need the real pinned compiler/Detekt; source
reasoning is not a clean static run. No product-family closure or coverage credit.
After explicit GUI03 freeze release and fresh root admission, include only
`:core:crypto:detekt` in the affected-module static04 batch (not an all22 rerun),
then use the independently reviewed fresh compile02 selection
`:core:crypto:compileAndroidDeviceTest` with the exact androidJar prerequisite.
Neither step is authorized or run here; compile01 remains a consumed precompiler
failure, not a candidate to replay. No extra host suite is needed for this delta.

The actual four-case Android32/native gate remains unexecuted and separately
subject to target/image consent and admission. Compilation is not Android32 ABI,
KDF, packaged ELF/minified app, vault/backup or physical-device proof. Preserve
PVU007 STOP, PVU011 NO RETRY, PVA029 no automatic retry, G7/G8 CLOSED, held-runtime
obligations and the earlier native-agent refusal. No SDK/process/runtime/Git/CI
probe, project/helper import/execution, dependency/config/central-ledger edit or
subdelegation occurred. Only this patch and rationale were written.
