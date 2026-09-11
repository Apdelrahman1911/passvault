# Android32 Detekt03 fixture proposal — independent review

Reviewer `/root/android_fixture_review`; author `/root/android_fixture_author`;
2026-09-11. **ACCEPT_SOURCE_ONLY — exact unapplied proposal accepted.**
No blocking source correction remains in this bounded review. This is not a
source-freeze release, execution admission, applied correction, Detekt pass,
compilation pass, native32 result or family closure.

Read alongside [the independent pre-proposal source/oracle/ABI review](SOURCE-CONTRACT-REVIEW.md).
That review began before receiving the author's exact proposal and records
surrounding code, invariants, contrary cases and unchanged verification limits.

## Exact accepted identities

Paths below are relative to `B/reviews/team20/android_fixture_author/` unless
otherwise stated. All hashes were independently recomputed from read bytes.

| Object | Bytes | SHA-256 |
| --- | ---: | --- |
| `ANDROID32-DETEKT03.UNAPPLIED.patch.txt` | 2525 | `7a3cc856f383c35570d17d68475593a0ba45771091657dffe0ef25cbbe7b0074` |
| `RATIONALE.md` | not used as a size binding | `24a6845f43c7b459d6af134e31f060aa199b37e66b56c9b2d2569680097f6517` |
| Current fixture before | 14155 | `d9bdb00637773f5d239ffcc5f7d13dc3773e528ef0dec1b1efdffec0ee182310` |
| Proposed fixture after, **in memory only** | 14600 | `9c4567e4a9cd1f6023534d3b5a40dfb21981cd49cc8eff0b4d1e453429246ca8` |

The sole patch target is
`core/crypto/src/androidDeviceTest/kotlin/com/passvault/core/crypto/Android32KdfInstrumentation.kt`.
No production source, build configuration, dependency, manifest or common test
change is proposed. There are no new cases or permanent tests.

Independently read `B/reviews/checkpoint17/source-prepare01/SOURCE.json`, SHA-256
`e03d29e54974413ff893f96d9bcf0c70bbf11eeeaf114ac96296987456db1f7f`.
Its target tuple agrees with the current source's exact size/hash above. This
is retained C17 source evidence, not a new Git/T/ref/index observation or a claim
of continuous hostile-mutation isolation.

## Independent comparison, without application or execution

Read every diff hunk and the complete rationale, then parsed the four unified
diff hunks as ordinary text. Checked both target headers, old/new line counts,
all old/context lines and offsets; reconstructed the proposed text in memory
without writing or applying it. Its exact after hash/size independently match
the author's prediction. No project/validation helper was executed or imported.

Byte comparisons independently preserved these five grouped source sections:

1. Case inventory and argument admission/setup.
2. Entire onStart body, terminal/status helpers and native32/runtime observation
   gate (the new suppression/comment are outside that comparison).
3. Mapping reader/loop prefix through the line-length guard.
4. Mapping result labels and Exception fallback.
5. Every vector/factory, derivation/assertion/finally block and constant.

The proposed longest line is114 characters. That is a source-data observation,
not analyzer execution. No protected source was changed by this review.

## Four-diagnostic adjudication

| Original diagnostic | Exact treatment | Independent challenge/disposition |
| --- | --- | --- |
| ComplexCondition186:25 | Private `mappingContainsAddress` | Accept: guard rejects range-size mismatch; original start/end parse order and radix16 remain; null check and half-open comparison are separated without changing their truth conditions. |
| NestedBlockDepth174:17 | Reader uses that predicate | Accept: leaves the same reader/use, loop, row counter, line guard and first-match assignment; removes the nested inline parsing branch, not a safety guard. |
| TooGenericExceptionCaught70:22 | Documented suppression on `onStart` only | Accept intentional terminal test adapter: AssertionError remains FAILURE(-2), all other caught throwables ERROR(-1), followed by canceled summary/immediate return. Narrowing would lose linkage/assertion reporting. |
| TooGenericExceptionCaught194:14 | Separately documented suppression on `functionMapping` only | Accept intentional non-suspending optional-observer boundary: preserves Exception=>NOT_ESTABLISHED; Error still escapes to the terminal boundary. Narrowing could turn an existing metadata-only gap into a native-case failure. |

The two suppressions name only TooGenericExceptionCaught and apply to exactly
the two reviewed functions, each with one existing generic catch. They do not
hide a new recovery path, suppress the class/file, disable guards, add a baseline
or weaken Detekt configuration. I considered replacing the generic catches
with narrower lists and rejected the resulting behavior change as unnecessary
for this lint correction. This acceptance is not a general suppression policy.

The helper has two return statements; each boolean condition has two operands.
Both endpoints remain nullable immutable local values, smart-cast only in the
non-null branch. It adds no arithmetic, inclusive upper bound, unsigned cast,
range allocation, reader, coroutine or mutable owner. Private helpers remain
excluded only from TooManyFunctions by the existing unchanged root config.
Actual pinned Kotlin/Detekt acceptance still needs later admitted execution.

## Disproof attempts and preserved security/oracle boundaries

The source predicate before and after is equivalent to:
`range.size == 2 && start != null && end != null && start <= address && address < end`.
Missing delimiter, missing/nonhex/overflowing endpoint, empty/reversed interval,
address==end and address==start were reasoned through without executing tests.
The helper does not newly accept a malformed numeric field, reject the lower
boundary, accept the upper boundary or advance past the first matching row.
Reader close/error handling and row/line limits stay at the same locations.
The optional label stays OBSERVED_NOT_ELF_BOUND or NOT_ESTABLISHED, never an ELF
identity claim. The existing post-read line check is not a preallocation bound.

The four exact IDs/order and their intended0/1/1/2 KDF calls are untouched. A
64-bit process still fails before KDF; reflected JNA pointer/size_t checks,
same-proxy native-function discovery and unsigned32 address mask are untouched.
Historical binary/text vectors and both ops3/ops4 64MiB reference literals still
use production deriveKey. Caller encoding is not confused with arbitrary-byte
deriveKey input; lowercase-hex compatibility and the native size_t selector are
unchanged. No mock success, direct-JNA replacement, new expected-value generator,
benchmark/profile-selection assertion or host result substitutes for native32.

Throwable failure reporting cannot continue into later cases or fabricate their
passes. Normal completion still requires all four successful bodies/statuses and
matching terminal counts. Cancellation/exception paths retain all fixture
finally ownership and the engine's cancellation-bound result cleanup. No secret
owner, factory, assertion or wipe was moved for cosmetic complexity reduction.

These challenges found no blocking semantic change in this exact proposal. They
do not establish absence of every possible bug in the fixture or surrounding
production implementation.

## Application and verification remain root-owned

Root must explicitly reconcile/release the C17 GUI03 freeze before applying this
exact patch. Rebind future source captures/compile candidates to the actual
after bytes; do not apply it to a different before image or mutate consumed
requests. Mechanical root application of these exact reviewed bytes needs no
new design-review loop; any semantic/source delta beyond this patch does.

The smallest relevant future analyzer selection is `:core:crypto:detekt` within
the independently admitted affected-module static04 batch, not an all22 repeat.
The separately reviewed fresh compile02 selector/prerequisite must be accepted
and admitted on its own. Nothing here authorizes either invocation. Compile01
remains consumed failure before compiler actions; Detekt03 remains its actual
consumed failure. There are zero new test cases or cleanup/runtime receipts.

Compilation, static analysis and source comparison cannot establish Android32
native KDF behavior. The exact four-case target run, APK/AAR/ELF binding, ARM32,
minified application, vault/backup flows and hardware claims remain separate.
PVA-001 and all counts remain unchanged; owner/image/target qualifications stay
open. STOP/NO-RETRY/CLOSED scopes, old held runtimes, native-agent refusal and
occupied1017001 remain untouched.

Only reports in this reviewer's directory were written. No Git/CI/build/test,
SDK/runtime/process probe, project/helper import/execution, network access,
central-ledger edit or new worker occurred. Foreground data readers completed;
no new wrapper-stop obligation was created, and no earlier obligation is settled
by this review. Root owns milestone notifications and final adjudication.
