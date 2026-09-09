# Corrected Mac source-data reader: independent v1 challenge

Reviewer `/root/native_review`; author `/root/native`; 2026-09-09.
**REVISE — NO READER/WRAPPER/NETWORK OR TARGET EXECUTION ADMISSION.**

Read the entire proposed reader (268 LF /11,956 bytes), SHA-256
`b963963180e12dcf952f5356a7a06d3364450150804bc23e25c25a4da984a1f9`, and proposal
(163 LF /10,098 bytes), SHA-256
`313d6f0d464db721c5462ed3b36c0a64ce1c1a602f2eaee146c868ed26f72bbc`.
Read the original finite capture plan and both preserved capture01 failure
records as inert data. The JSON binds their exact hashes. No source execution,
import, syntax probe, GET, CI query, target API/worker control or source edit
was performed. The capture02 evidence parent was absent when inspected.

## Required source corrections

### C02-R1 — Rejected evidence-parent guard still permits a write

Reader175 opens the proposed parent;179–181 checks effective UID and private
mode. On rejection,254–256 catches the error, but finally258–260 unconditionally
calls `create_once(parent, ID + ".json", ...)` and fsyncs that directory. If a
wrong-owner or nonprivate directory is openable/writable, the supposed failed
admission guard therefore still creates a report through its rejected fd.

Counterexample: an existing writable parent with mode0755 reaches the explicit
privacy rejection, then the unconditional finalizer. This is a conditional
source path, not an observed unauthorized write: the future external wrapper's
correct gate may prevent it, and a missing/unopenable parent reaches no such
write. Nonetheless, the reader's rejection path must itself be close-only for
that parent, with failure evidence confined to stdout/stderr or a separately
admitted outer record. Do not write or fsync via a rejected parent fd.

### C02-R2 — Ordinary exception gap after acquisition

Parent open175 precedes record construction/stamp176 and the try178. An ordinary
exception during that intervening allocation/call bypasses the intended fd
finalizer. Process exit may eventually release the descriptor, but that is not
the installed cleanup path or a recorded close result. Move throwing record
setup before acquisition, or put acquisition under an already installed guard.
Preserve once-only close attempts and explicit close/interruption ambiguity;
this does not require a retry or a general recovery framework.

### C02-R3 — Dependency metadata role is not independently checked

`verified_record`112–132 checks the transport row's capture/id, integer zero
exits and shared response-digest/URL, but not the metadata record's own capture,
id, repository, exact expected endpoint or cap. `verified_commit`135–140 can
then return any well-formed commit present in that accepted-shaped metadata.
A stale/wrong-role metadata record paired with a transport row labeled for the
current identifier can therefore satisfy this reader gate. Checking that two
records share a URL does not prove that it is the identifier's permitted URL.

An honest original producer and correctly sealed outer instance may exclude
that malformed record; no wrong GET/source or malicious root is alleged. The
reader should nevertheless reject ordinary stale/misfiled role data before
using it: require the finite identifier's capture/id/repository/official URL,
integer cap/body bounds and corresponding metadata/file semantics. The CMake
process dependency must also bind the same accepted CMake commit. These checks
do not authenticate a human from the `"/root"` string; external provenance and
original-record binding remain necessary.

The author independently acknowledged all three source paths and agreed to
preserve the frozen v1 before a versioned correction. That acknowledgement is
not patch acceptance; corrected bytes need a fresh independent read.

## Narrow credit and counterexamples retained

- Direct Python file argv removes the specific JSON-string/shell-`-c` quotation
  mechanism that broke capture01. The file itself contains no network call,
  subprocess, downloaded-code evaluation/import or target probe. No syntax or
  runtime success is inferred from this manual source read.
- Twelve finite identifiers map to three official repositories, three exact
  metadata endpoints and nine candidate file paths. Main refs must resolve a
  commit object; CMake's requested v4.4.2 commit must match canonical repository
  URLs. File requests are commit-pinned and require an exact prior intent.
  Missing refs/paths/sections remain unavailable; their existence was not
  independently queried or established by this review.
- Stdin is read to cap+1; oversize prefixes are labeled and fail. With the
  intended blocking pipe, an accepted shorter read depends on EOF. Duplicate
  keys/nonfinite literal constants are rejected; file metadata uses strict
  Base64, UTF-8 and actual Git blob size/hash checks. Parse completion still
  cannot establish curl success. Truncated HTTP delivery that leaves a parsed
  body must remain unadjudicated unless the separately observed curl exit is0.
- The parser's best status is BODY_PARSED_TRANSPORT_UNADJUDICATED, not PASS.
  Integer exit checks reject bool/float zero. Write/fsync/close failures do not
  become reader exit0 merely because a BODY_PARSED record was partly written;
  the outer actual exit remains necessary and failures may leave partial or
  unlisted permanent source/receipt files. No deletion or close retry is added.
- Source sections merge LF-only ranges, retain full-source/section hashes and
  do not reinterpret CR/VT as physical LF. Missing anchors/line-cap overflow
  fail, not trigger new source requests. A cmUVProcessChain substring supports
  only a mention flag: actual relevance of the conditional last GET must be
  adjudicated from captured source before its intent, not from a mention alone.
- The declared response caps sum to1,802,240 bytes (98,304 metadata +1,703,936
  file envelopes), below2MiB. This is an accepted-body budget, not measured wire
  traffic/RAM use or a proof that an overlimit failed pipe receives no extra
  buffered bytes. Full envelope limits also bound decoded retention; section
  line limits alone would not bound byte size. The outer runtime/resource and
  total-retained-evidence bounds remain separate.

## External packet still missing even after source correction

No exact root wrapper or filled original-namespace instance has been accepted.
Before any separately authorized capture, root must bind installed curl/Python
and trusted startup/environment, immutable reader/source hashes, original
checkout/evidence ancestry and parent identity, each exact URL/cap, exclusive
one-shot attempt and preexisting-output rejection. Private-mode checking alone
is not ancestor/original-identity admission. The reader opens the fixed parent;
it does not itself implement the external one-shot/provenance authority.

The actual shell must capture both PIPESTATUS elements **immediately**, preserve
them on failure/cancellation and stop the whole batch at the first failed or
ambiguous pipeline. An intervening command or premature errexit must not erase
or bypass that receipt. Use actual fixed argv/quotes, no JSON source interpolation,
credential/profile/proxy injection, redirect/retry or reconstructed capture01
helper. Exact cancellation/outer wall bound, owned-child settlement, Python
startup policy, selected curl's stream-limit behavior and receipt publication
must be source-reviewed in the filled wrapper. A prose pipefail instruction is
not an implemented failure path or execution approval.

Root transport rows must come from observed statuses and verified original
record bytes, not labels or a copied journal. Missing/ambiguous transport,
output, descriptor or namespace outcome is HOLD. Keep necessary compact
permanent evidence; do not delete source, toolchains, shared caches or reports.
No Gradle invocation exists here, so --stop is not applicable and no old stop
obligation is discharged.

## Historical failure, accounting and fences

Capture01 remains consumed: three distinct metadata pipelines failed with
curl23/reader1, zero verified response/commit/source identity, all nine file
slots UNSTARTED. Its inert reconstruction explains the escaped-newline syntax
failure before reader-body execution and the missing fail-fast sequencing;
these are retained author records, not replayed commands or raw API responses.

Capture02 requests/executions/tests:0. These three review defects are source-
capture infrastructure corrections, not new product PVA families or qualified
closures. No Mac primitive/ABI/available-memory/ACL/CTest-binary equivalence,
platform execution, hardware result or overall readiness follows from source
capture. Windows01 FAIL/filesystem HOLD/no retry, all STOP/NO-RETRY/CLOSED and
protected-ref/publication/build1017001 boundaries remain unchanged.

Only this compact permanent review pair was created. No reviewer-owned build,
cache, temporary executable, background worker or evidence-parent allocation
was created; no deletion, stop, termination or recovery was performed. Root
alone may authorize a corrected, exact new capture instance later.
