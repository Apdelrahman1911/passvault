# Fixed three-XSD capture v1 — independent source challenge

Reviewer: `/root/build_config`. Utility/proposal author: `/root/android32`.
Review point: `2026-09-09T00:42:23Z`. **REVISE; NOT ADMITTED.**

This is a source-path challenge, not an executed failing test, product defect,
schema interpretation, archive capture, or permission to run the reader.

## Exact reviewed tuple and method

- `../android32/CAPTURE_FIXED_XSDS.py`: 14158 bytes, 320 physical LF, SHA-256
  `9650b9f61f92853bdec13853296998e75112daa4d3ae3ecb68e6649be372b83b`.
- `../android32/SCHEMA-CAPTURE-PROPOSAL.md`: 9112 bytes, 147 physical LF, SHA-256
  `aa652d7964a99e81a6b4fca053f8bb3c5c853b7081db9e23722eaf5b0f9f36f7`.
- Both files were read completely as inert text and rehashed. No compilation,
  import, execution, SDK invocation, archive decoding, XML parser, network
  access or new test was performed by this reviewer. The author states neither
  file has been executed/imported/compiled/tested and has agreed to preserve
  these rejected before-images before editing the successor.
- Both fixed installed containing-JAR hashes were independently rechecked as
  opaque file data: `tools.sdklib.jar`, 1909475 bytes,
  `d0bbe0e03155a7bdd9bac03567b6b26797f81a778f55d5eadc3e71e7a0a27434`;
  `tools.repository.jar`, 272256 bytes,
  `9fd419f3307be633aaf7b02b52832521c1300d4bf9058cee83bb11139990a7f9`.
  This does not independently verify unread ZIP members or schema linkage.

## Supported bounded design

The script is not a downloader, installer, schema validator, SDK/JVM launcher,
general archive utility, old runner or build/test supervisor. The source has
exactly two absolute JAR inputs, three exact member names, independent expected
member sizes/hashes and five fixed output basenames. No archive member name
becomes an arbitrary filesystem output path. No network/process-creation call,
Java/class import, `extractall`, deletion, SDK write or namespace-reuse path is
present. The import list uses installed Python standard-library facilities.

Full JAR size/stability/SHA-256 checks precede ZIP parsing, and parsing uses
those in-memory bytes rather than reopening the JAR. The source discloses that
central-directory metadata is parsed before the 10000-entry count check; the
fixed complete-container hashes and process limits substantially narrow that
case. Duplicate selected names, directories/encryption, special file types,
unsupported compression, changed compressed/uncompressed sizes and changed
selected hashes are rejected. Only stored/deflated, <=32KiB selected members
are read. All three are validated before the first captured XSD is created.

The declared exact XSD sizes sum to35460 bytes, with hashes matching the
proposal's previously retained assertions. Output directory creation is
exclusive; outputs use `O_EXCL|O_NOFOLLOW`, fixed permissions, readback, fsync,
regular-file/single-link checks and a bounded JSON/total-byte policy. An existing
directory fails, rather than being reused or removed. No temporary extraction
or container copy is proposed. Source capture, self-hash and path observations
are correctly qualified as non-atomic/non-hermetic, not malicious same-UID
isolation. Root's independent launch-source binding remains necessary.

## Required corrections, with reachable source counterexamples

### XSD-CAPTURE-R1 — ownership transfer is interruptible

At lines99–108, `open_directory()` opens a child at102, closes the previous
descriptor at103, and only then assigns the child as the tracked descriptor at104.
The program has already installed **throwing** signal handlers at208–210.

Counterexample: after102 returns, a handled signal raises `Refused` before104
(including while attempting103). The `except` at106 closes only the old
`descriptor`; the newly opened `child` is not registered for cleanup. An error
from103 has the same missing-child path. A further close of the old descriptor
may itself fail or refer to a descriptor whose first close already completed.
This is not cured by the leaf-file `finally` or final parent/output closes.

Acquisition before its cleanup region also appears at113–114 and164–165;
directory-return assignment occurs at223/251, and output open/assignment at233.
Do not claim failure-safe owned-descriptor cleanup across these handoffs while
asynchronous exceptions can escape between acquisition and registration.

Required property: register every acquired original descriptor for exactly its
own cleanup before interruption/error can lose ownership; never blindly retry
an uncertain close against a possibly reused integer. A signal-masked ownership
transition or a non-throwing cancellation recorder with explicit safe
checkpoints can implement that property. The reviewer is not prescribing a
general hostile-process framework for this small fixed read.

### XSD-CAPTURE-R2 — one final close failure bypasses another

At306–310, `os.close(output_fd)` can raise/receive a throwing signal and skip
`os.close(parent_fd)`. There is no final close-result ledger, and the generic
cleanup statement at294 is prepared before these closes occur. The `RESULT`
can already say `CAPTURED` even if a later close/fsync fails; the proposal
correctly requires the external exit too, but that does not establish that all
independently safe closes were attempted.

Required property: attempt every registered original close independently,
retain close errors/uncertainty in the terminal external receipt, and fail/HOLD
rather than treating a receipt written before closeout as final success. Do not
rewrite an already-created RESULT or retry an ambiguous descriptor close. Root
must still retain external stdout/stderr/exit: crash/SIGKILL/host loss can defeat
all in-program receipts even with corrected ordinary failure handling.

### XSD-CAPTURE-R3 — post-start resource floors are not enforced

Lines226–227 enforce launch12GiB/25%. Lines296–297 record after-resources, but
there is no8GiB/20% running-floor comparison before the success return at316.

Counterexample: launch sample is above both floors, then unrelated host demand
reduces available memory below20% (or disk below8GiB) while the capture is active.
The after-sample records that low value, but the current code still leaves
`CAPTURED` and returns0 if filesystem operations succeed. A <=256KiB write cap
does not make whole-host resource availability invariant.

Required property: preserve and enforce the inherited running floors at the
appropriate pre-output/final observations, retaining the failing sample and a
non-success result. For this tiny, two-container/three-member task, do not
invent an application-scale polling subsystem; document the sampling bounds
and the difference between observed floors and hard process resource limits.

## Further qualifications for the successor and root admission

- Non-throwing cancellation recording is a possible simpler R1 approach, but
  then the15s alarm is **a request observed at safe checkpoints**, not a hard
  in-program wall timeout. Check cancellation before another input/output and
  before terminal success. Blocking I/O may continue to the separately owned
  outer20s/5s timeout. Keep uninterruptible-I/O/SIGKILL/no-final-receipt limits.
- Closeout must not be skipped because the cancellation flag is set. A final
  exclusive failure receipt is permitted; further captured-data writes or a
  successful result after cancellation are not.
- Address-space128MiB and CPU soft5/hard6 constrain this process, not other
  host work. RLIMIT_FSIZE is per-file; the explicit five-name/size bounds, not
  RLIMIT_FSIZE alone, provide the aggregate output bound. `written_payload_bytes`
  currently increments only when `write_new()` returns, so it is not a complete
  accounting of partial bytes left by a failed write. Qualify that diagnostic.
- Original directory FDs and leaf path/fstat checks are useful point evidence;
  they do not prove continuous pathname ancestry or absence of same-UID mutation.
  No full hostile-archive/host sandbox is required or established by this review.
- The proposed external timeout is an additional root-owned process boundary.
  Root must bind that command/tool identity and actual terminal result, rather
  than reading `subprocesses_started: 0` as a count of every process in the outer
  invocation. The Python reader itself contains no process-creation call.
- Setup failures before the main try, failures opening the output namespace,
  and fatal resource/OS events may leave no in-packet final journal. Preserve
  external failure evidence; do not manufacture a RESULT or replay the namespace.

## Preservation and unchanged audit scope

Only this compact review was written by the reviewer. At the review point,
workspace free space was22839940KiB, `/tmp`21492780KiB; MemAvailable44761172KiB
of65855360KiB. These are point observations, not admission. No temporary files,
cache/build outputs, archive extraction or persistent worker was created;
wrapper `--stop` is NOT_APPLICABLE to this source review, not satisfaction of
any historical or current other stop obligation.

PVA-001 remains OPEN and the full schema linkage independently UNVERIFIED.
No product-case, closure or readiness credit is added. Original/full confirmed
closure denominators and suspicion accounting are unchanged. Prior accepted
catalog/source evidence, eight design explanations with owner decisions
separate, PVU-007 STOP, PVU-011 NO RETRY, PVA-029's FAIL/no automatic retry,
G7/G8 CLOSED scopes and candidate1017001/publication boundaries are preserved.
