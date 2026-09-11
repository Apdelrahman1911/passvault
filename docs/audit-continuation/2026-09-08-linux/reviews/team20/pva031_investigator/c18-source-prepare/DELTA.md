# C18 shared source preparer: unbound text delta only

Candidate: `SOURCE-PREPARE.py.txt`, 17678 bytes, 288 LF; SHA256
`424e30c470dd88b028bb39e007c72ca4ce7ad1b83f044eeead0db1c7eb048c20`.
Future promoted path, not created here:
`reviews/checkpoint18/source-prepare01/SOURCE-PREPARE.py`.
All paths below are relative to `docs/audit-continuation/2026-09-08-linux`.

## Accepted predecessor and exact derivation

Only the independently accepted C17 preparer was used as source text:

- `reviews/checkpoint17/source-prepare01/SOURCE-PREPARE.py`: 17873 bytes,
  288 LF; SHA256 `e1cb176dd9b2a4c4b24b9c34dee997909d308ad3884d3dd1069afa0fed41534a`.
- Its three binding substitutions were reversed literally using
  `reviews/checkpoint17/source-prepare01/BINDING.json` (1792 bytes;
  SHA256 `cd9958053224396b95b00169b18af0456d0e7705688a5f942c35b4af9b4ac9ad`).
  The result exactly matched `SOURCE-PREPARE.unbound.py.txt` in that directory:
  17673 bytes, 288 LF; SHA256
  `0042497f8972713653aa3374c2f2abeba936cc1102fd0e767edd113643fc73d2`.

The predecessor's independent acceptance chain was read as inert data:

- `reviews/baseline-coverage/C17-SHARED-SOURCE-PREPARER-DELTA-REVIEW.json`:
  `ACCEPT_UNBOUND_C17_SHARED_SOURCE_PREPARER_DELTA_ONLY`;
  SHA256 `9a28609d1cbc3a78936c92ffc9f8047cdf59b7994b26fed4eda456fdf16fec09`.
- `reviews/verification/C17-SHARED-SOURCE-PREPARE-INSTANCE-REVIEW.json`:
  `ACCEPT_EXACT_ONCE_CURRENT_C17_READ_ONLY_SOURCE_PREPARATION`;
  SHA256 `5a21998d2535f98360d9ade6f05c05f2f8dcfbef06527ece0c412a1aa7a3a2b8`.
- `reviews/verification/C17-SHARED-SOURCE-PREPARE-ACTUAL-REVIEW.json`:
  `ACCEPT_CONSUMED_C17_SOURCE_PREPARATION_RESULT_NO_VALIDATION_ADMISSION`;
  SHA256 `5dd8b17acd5abcb23b9d6d56117607c6a552ae46eb707d92f7122d07fddb41f5`.

Consumed C17 capture authority does not transfer to C18 or repair the separate
missing C17 publisher external terminal.

After de-binding, only these exact literal substitutions were made:

| Before | After | Count |
| --- | --- | ---: |
| `reviews/checkpoint17` | `reviews/checkpoint18` | 2 |
| `NEW C17 checkpoint17 source-prepare01 for GUI03, Detekt03 and optional Android compile.` | `NEW C18 checkpoint18 source-prepare01 shared by AndroidCompile02, static04 and regression01.` | 1 |
| `passvault-checkpoint17-source-prepare-v1` | `passvault-checkpoint18-source-prepare-v1` | 1 |
| `PUBLISHED_EXACT_C17_SOURCE` | `PUBLISHED_EXACT_C18_SOURCE` | 1 |
| `current C17 branch` | `current C18 branch` | 1 |
| `actual C17 identity` | `actual C18 identity` | 1 |
| `CHECKPOINT-17-PUBLISHED.json` | `CHECKPOINT-18-PUBLISHED.json` | 1 |
| `published C17 commit/tree` | `published C18 commit/tree` | 1 |
| `0 < len(entries) <= 4096` | `0 < len(entries) <= 3500` | 1 |

Changed physical lines relative to the bound C17 source are exactly
`2,14,19,21,22,28,208,212,221,223`; LF count stays 288. Reversing every
substitution and reapplying the three old binding substitutions recovered the
entire bound predecessor byte-for-byte. Old bound commit/tree/admission/
publication hashes are absent from the candidate. This is a data-only proof,
not execution, import, AST parsing or a syntax check.

## Narrowed count and downstream coupling

Per root's clarification, the sole policy change is the member ceiling
4096 -> 3500. The C17 preparer has no canonical-OID buffer reader/generator or
128 KiB OID cap: none was added. For later consumers only,
`3500 * 41 = 143500 <= 147456` bytes (144 KiB), leaving 3956 bytes.
Each separately admitted AndroidCompile02/static04/regression01 consumer
must enforce its own canonical request cap and bind the exact immutable
shared C18 `SOURCE.json`. This preparer neither constructs those requests nor
admits any consumer. 3501 members must refuse; 3500 can still exceed the
unchanged byte/output limits. No future count or success is promised.

## Unchanged body and limits

Apart from the listed binding/scope literals and count narrowing, the complete
predecessor body is unchanged: exactly two eventual read-only Git operations
(`rev-parse` commit/tree and `ls-tree -r -z -l --full-tree`), no `cat-file`, OID
transport, source materialization, SDK access, runtime/cache/temp/build work
or Gradle stop. Descriptor-relative no-follow reads, ownership/single-link/
type checks, lock/config/HEAD/branch/shallow/original-directory/input checks,
and redirect/attributes absence guards remain. Exclusive `SOURCE.json` and
`SOURCE-CAPTURE.json`, fsync/readback and final rechecks remain.

The unchanged bounds are 128 MiB raw and 128 MiB observed checkout, 32 MiB per
member, 2 MiB listing/output, 180 seconds work and 30 seconds per Git call;
resource floors remain 12 GiB/25% initially and 8 GiB/20% subsequently. The
15 permitted CRLF candidates and two historical EOL qualifications remain,
with exact raw Git OID/size/mode and historical script AND raw-copy tuples.
Existing `OLD` desktop-gui02 inputs/hashes are pinned historical EOL data only,
not old-helper or runtime authority. There is no claim of atomic checkout,
hostile-root protection, hard deadline/RSS enforcement, global idleness or
arbitrary-descendant settlement. Failure/partial outputs stay HOLD with no
automatic retry.

## Unbound boundary and handoff

`P`, `TREE`, `FACTS_SHA` and `PUB_SHA` are `None`. The retained T/G/REF/LOCK
selectors are unchanged predecessor text, not fresh identities or access
permission. No fresh C18 index/object/tool-image facts are supplied or borrowed.
By text inspection, `checked(FACTS, None)` cannot admit this unbound candidate
before lock/T/Git work; that guard was not run or tested. Root must provide
fresh published C18 identity, facts and genuine independent instance admission
later, if justified. Source review does not constitute that admission.

Only this rationale and the unbound source text were created in this lane.
No T/Git/proc/SDK/runtime access, helper execution/import, AST/syntax checks,
builds/tests, allocation, manifest/capture/facts/binding/publication outputs,
old-helper retry or process diagnosis occurred. Future C18 paths were not
created. GUI03 observer01 remains consumed, its original R remains HOLD, and
all existing STOP/NO-RETRY/CLOSED/HOLD fences and freezes remain unchanged.
The independent reviewer receives the exact two-file hash-pinned delta;
root alone handles any later accepted promotion/binding/capture.
