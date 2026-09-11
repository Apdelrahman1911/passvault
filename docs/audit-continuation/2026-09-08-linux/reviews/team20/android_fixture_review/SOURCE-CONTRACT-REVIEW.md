# Android32 fixture — independent pre-proposal contract review

Reviewer `/root/android_fixture_review`, 2026-09-11. Paired author
`/root/android_fixture_author`. **SOURCE/DATA ONLY; no application correction,
execution, admission, closure or native32 result.** This review was begun before
the author's unapplied proposal and does not itself approve an unseen patch.

## Authority and exact inputs

Read `TEAM_20_RESUME.md`, the saved pause, `AUDIT_HANDOFF.md`, handoff
START_HERE/PERMISSIONS/ASSEMBLY, publication PUBLISHED_PAYLOAD, repository AGENTS,
the focused production-readiness/secure-backup instruction snapshots, and the
relevant SECURITY_MODEL/VAULT_FORMAT contracts. Old example commands are dormant.
C17 source/tests/helpers and publication T remain frozen; root alone may release
the freeze and admit execution. No Git/T, SDK/runtime/process probe, project or
validation-helper execution/import, build, test, network request, central-ledger
edit or background worker was used. Only source/evidence reads and ordinary
standard-library JSON selection were performed. A few guessed config paths were
absent and supplied no evidence; the actual root `detekt.yml` was then read.

Repository-relative SHA-256 identities read independently:

| Source | SHA-256 |
| --- | --- |
| `core/crypto/src/androidDeviceTest/kotlin/com/passvault/core/crypto/Android32KdfInstrumentation.kt` | `d9bdb00637773f5d239ffcc5f7d13dc3773e528ef0dec1b1efdffec0ee182310` |
| `core/crypto/src/androidDeviceTest/AndroidManifest.xml` | `95a2d24cac877cd0d4fbbfeb58bc10d06275b2c40592244617a83564ec1a06ff` |
| `core/crypto/build.gradle.kts` | `a18a2ee9127da9861d4310ab7588573d0596d31c317e3ff393c8e30f70888112` |
| `core/crypto/src/commonMain/kotlin/com/passvault/core/crypto/RawPasswordHash.kt` | `3fb16abe001752bf44b5a56039c58738ebed0e6ced5972e3ca0985d5a40586eb` |
| `core/crypto/src/androidMain/kotlin/com/passvault/core/crypto/RawPasswordHash.android.kt` | `d2c4bcd22df2cb89a97e10a4b591d651f224f0b1c5224bc6b5de10f407d0df1c` |
| `core/crypto/src/commonMain/kotlin/com/passvault/core/crypto/LibsodiumCryptoEngine.kt` | `2b8dd21fe476735f16f1ab35543417a70202e7923dfac328f9064e31f5923b57` |
| `core/crypto/src/commonMain/kotlin/com/passvault/core/crypto/CryptoEngine.kt` | `571ff97bf0758e788e8ad405ab0c147b52e040fcc291c28080329d44ba8516b9` |
| `core/crypto/src/commonTest/kotlin/com/passvault/core/crypto/Argon2Test.kt` | `195fb796a4ed2a6a03cc27c6a785ebe09efd2dd977b30591f51025912e597231` |
| `core/crypto/src/commonTest/kotlin/com/passvault/core/crypto/RawPasswordHashArgumentTest.kt` | `c441eca77913a450fab817f6450f7aaa82ab8cbcb7077a25252e451ac7c6459a` |
| `core/crypto/src/nativeInterop/cinterop/passvault_sodium.h` | `192edcd85cfc70c602b5141b567bee03162fb33bae3af0029490a8bd67f49dce` |
| `detekt.yml` | `ef1abd634b35c339037856ee5f00b584a74d6a9783c6c0629637adbd8bc1af5e` |

The complete fixture, Android/desktop raw adapters, common raw-byte helper,
manifest, module configuration, C declaration and two named common test files
were read. Engine derivation/cancellation, DerivedKey cleanup, security/vault
documentation and current ledgers were focused excerpts, not whole-file semantic
reviews. Some long historical report output was truncated; no unseen text is
credited. The separate independent prerequisite review's source/ABI/qualification
sections were read in a bounded follow-up.

## Actual diagnostics, not test cases

`runs/linux-detekt03/reports/core-crypto-checkstyle.xml` SHA-256
`aeacb8d58f3712d5dfb80f7409bc335127a2f0b0e9344a99112e6d3ef30c75b1`
and log lines 177–185 agree on four diagnostics in the single fixture:

| Rule | Original location | Contract implicated |
| --- | --- | --- |
| ComplexCondition | 186:25 | Numeric half-open address match |
| NestedBlockDepth | 174:17 | Bounded optional mapping observer |
| TooGenericExceptionCaught | 70:22 | Terminal instrumentation failure adapter (`Throwable`) |
| TooGenericExceptionCaught | 194:14 | Optional mapping-evidence boundary (`Exception`) |

These are not four new product families and not executed application cases.
Root config uses version-matched defaults, ignores private functions only for
TooManyFunctions, and does not waive generic catches or complexity globally.

## Four-case discriminating contract

The inventory contains these exact case IDs in this order (fixture lines32–37):

1. `nativeRuntimeIs32BitAndLoadsSodium`: zero KDF calls. Checks executing
   `!Process.is64Bit()` before initializing sodium; then JNA POINTER_SIZE=4 and
   SIZE_T_SIZE=4. Records API, native-long width and reflected runtime JNA version.
2. `historicalBinaryPasswordVector`: one production derivation using bytes
   `00 ff 01`, salt bytes `00..0f`, ops1, memory8192, expected
   `2bc5a714c8397bb9e89e70c957231bd3cd4f0b4ffe32bb6ca1f5067196b60b15`.
3. `historicalTextPasswordVector`: one production derivation using UTF-8 bytes
   for the fixed synthetic `TestPassword123!`, the same byte salt/ops/memory,
   expected `4c1422bcf6ea79ca8b843170ffbaf713f854f1b97e4fd0df07d741a650cacab7`.
4. `productionProfilesMatchReferenceVectors`: **two** production derivations,
   same synthetic password, ASCII salt `0123456789abcdef`, memory67108864,
   ops3 => `dc1aff4d7c74898c0aca2da51e33760dbe716e70abc86a89f6e9d55d5451b03c`,
   ops4 => `ccd29a7f8888cf2ddce1df111f8ad5ba7939b0a177ac6606ad98beb6ac160172`.

All four expected key literals match `commonTest/Argon2Test.kt`. The production
profile comments pin upstream Argon2id v1.3 CLI tag20190702/commit
`62358ba2123abd17fccf2a108a301d4b52c01a7c`, input lowercase hex and 2^16KiB.
No reference generator was rerun. The fixture does not derive its expected
values from the production implementation under test.

`deriveKey(ByteArray,...)` accepts arbitrary nonempty bytes, including binary
`ff`; strict UTF-16-to-UTF-8 validation is a different caller boundary. The
engine preserves historical lowercase ASCII-hex input, not raw UTF-8 or
uppercase hex. Changing this byte contract, omitting ops4, using a fake engine,
or reusing actual output as expected output would destroy meaningful coverage.
The two small-memory vectors are adapter compatibility checks, not claims that
ops1/8KiB are production vault writer parameters. The last case tests fixed
profiles, not benchmark timing or profile-selection policy.

## ABI and contrary evidence

The Android raw adapter gets `crypto_pwhash` from the already-loaded sodium JNA
proxy's `Library.Handler`, rather than loading a second library or calling the
upstream incorrectly sized interface method. Its argument order agrees with the
checked-in C declaration:

| C argument | Supplied Android type | Android32 width |
| --- | --- | --- |
| out/passwd/salt | JNA Memory pointer | 4 |
| outlen/passwdlen/opslimit | boxed Long | 8 |
| memlimit (`size_t`) | selector using Native.SIZE_T_SIZE => boxed Int | 4 |
| algorithm | boxed Int | 4 |

The selector rejects negative limits/unsupported widths; width8 produces Long.
Native.LONG_SIZE/NativeLong is not a portable replacement (Windows LLP64).
Host argument-type tests establish source intent, not a live32-bit call ABI.

The fixture reflects actual JNA VERSION rather than inlining compileOnly5.19.1;
it reads the sodium version and function pointer from the same loaded proxy
library, masks the pointer to unsigned32 and rejects zero. Historical packaged
JNA5.18.1 metadata is not a newly resolved runtime observation. The manifest's
multiArch/use32bitAbi flags merely request an ABI preference. A64-bit process
must fail the first case, never skip it or substitute host-JVM results.

Neither a library filename/maps row, Java version, architecture folder label,
successful compile nor a native vector alone proves exact packaged ELF bytes,
minified application loading, ARM32, business flows or physical-device security.
Exact tested APK/AAR/native identities and admitted actual32-bit execution are
still separate. `execution="HOST"` disables device orchestration; it does not
turn this Android instrumentation into an Android host-JVM test.

## Terminal outcomes, cancellation and secret ownership

- `onCreate` accepts only inert bounded additionalTestOutputDir metadata. Any
  filters, shards, lists or unknown arguments cancel the fixed selection; no
  output directory is opened. Exact inventory cardinality/uniqueness is checked.
- Each start precedes its body. Only a normally returned body increments pass
  count and sends PASS. Throwable handling emits FAILURE(-2) for AssertionError,
  ERROR(-1) otherwise, then canceled summary and immediate return. LinkageError,
  cancellation and assertion failures cannot become successful completion or
  fabricated remaining passes. All four completed bodies/statuses and matching
  final counts are required before RESULT_OK.
- `deriveAndCheck` begins its try before factories/expected decoding; finally
  clears derived key+salt, expected, input salt and input password after success,
  mismatch or exception. Error diagnostics contain no actual derived key.
- Engine mutable lowercase-hex/native salt/native output temporaries and the
  Android Memory buffers retain their existing finally/use cleanup. The engine
  rethrows CancellationException and clears a successfully produced secret if
  dispatcher cancellation prevents delivery. Fixture cleanup remains around
  that handoff. Do not move ownership to satisfy a cosmetic lint threshold.

The broad `onStart` catch is therefore an intentional terminal test boundary,
not a production fallback that converts crypto failure to success. A precise,
explained function-local TooGenericExceptionCaught suppression is acceptable in
principle; narrowing to Exception would lose LinkageError/AssertionError status
handling. This is not blanket permission for generic catches elsewhere.

## Mapping observer and unexecuted counterexamples

Only the own-function address's first matching `/proc/self/maps` row is retained;
the full process map is not reported. Preserve reader.use, maximum8192 rows,
4096-character check on each read line, first-match stop, radix16 parsing and
the half-open `start <= address < end` predicate. The check occurs **after**
readLine; it is not a preallocation bound. No stronger heap claim is made.

Reasoned cases (source analysis, **not executed tests**): missing delimiter,
missing numeric endpoint, nonhex or Long-overflow endpoint cannot match;
address==start can match; address==end cannot; empty/reversed ranges cannot;
no match through the bound/EOF returns NOT_ESTABLISHED. IOException, policy
denial and oversized-line IllegalStateException preserve the explicit optional
mapping gap. An Error escapes this Exception boundary and reaches the terminal
fixture failure adapter, as before.

A small pure range predicate can remove both nested-if levels/complex condition
without changing read order, exception policy or cleanup. Avoid broadening this
to scan all mappings, remove limits, turn absent mapping into ELF proof, or
change the existing optional-observation failure into a native-case failure.
An exact explained functionMapping suppression is acceptable in principle:
this non-suspending observer's Exception fallback deliberately retains missing
metadata, not a KDF pass. Keep Exception rather than broaden it to Throwable.
Both suppression rationales and helper ReturnCount/default-rule risks were sent
to the author before receiving the proposal.

## Disposition and preserved limits

No source-contract change is required beyond a reviewed, semantically preserving
response to these four static diagnostics. This does not promise no other bug
exists. Exact proposal acceptance will be a separate report.

Current compile01 evidence remains consumed FAIL_BEFORE_COMPILERS with zero
target actions, JUnit XML, application/native cases or closures. The latest
Detekt03 failure remains consumed; this review neither retries it nor claims
its full execution/cleanup reconciliation. Source-only correction cannot clear
PVA-001 or the owner/image/target/native32 gaps. Root must reconcile GUI03's C17
freeze before application and bind any later compilation/static/runtime request
to the corrected source identity.

All STOP/NO-RETRY/CLOSED scopes, occupied1017001, earlier held-runtime/cleanup
obligations, protected refs and publication restrictions remain untouched. No
new wrapper-stop obligation exists from these foreground source/data reads;
this says nothing about another owner's existing obligations.
