# Fixed three-XSD capture v2 — independent source acceptance

Reviewer: `/root/build_config`. Utility/proposal author: `/root/android32`.
Review point: `2026-09-09T00:58:12Z`.
**ACCEPT_BOUNDED_SOURCE_FOR_ROOT_ADMISSION. No execution by this reviewer.**

## Exact source tuple

| File under `../android32/` | Bytes / physical LF | SHA-256 |
| --- | --- | --- |
| `CAPTURE_FIXED_XSDS.py` |19707 /450| `44799ffc328d6427482feb31db05adebb5fe31e75f482bbf003ba5b88e6b8fe8` |
| `SCHEMA-CAPTURE-PROPOSAL.md` |12617 /201| `c0ca8616be5bbfad3d099e9a4e1ee7a92b6799c8ace59136550eb89fbefa16c4` |
| `CAPTURE_FIXED_XSDS.rejected-v1.py.txt` |14158 /320| `9650b9f61f92853bdec13853296998e75112daa4d3ae3ecb68e6649be372b83b` |
| `SCHEMA-CAPTURE-PROPOSAL.rejected-v1.md` |9112 /147| `aa652d7964a99e81a6b4fca053f8bb3c5c853b7081db9e23722eaf5b0f9f36f7` |

The complete450-LF successor and201-LF proposal were independently read as
source, not merely diff-checked. Current and retained before-image hashes were
recomputed; they match the author freeze. The original REVISE report remains
`ANDROID-XSD-CAPTURE-V1-SOURCE-REVIEW.md`, SHA-256
`09ce30ec153c663eeca8f8745f25f15e3b4ba76ff2cbdf46a4673b97df881275`.
The rejected generation has no execution authority.

No script compilation/import/execution, ZIP decoding, XML parsing, Java/SDK
invocation, archive download, network request or application case was performed
in this review. `schema-capture-01` was absent at the review point; that is not
a permanent availability reservation or permission to create it.

## Independent challenges and corrected outcomes

### R1 — acquisition/registration and cancellation

The installed handlers now only record the first signal; they do not throw
across `open_owned()`'s acquisition/registration. A newly opened directory child
is registered before its parent close, so a parent-close failure no longer
loses that child. Main/input/output/resource acquisitions share the original-FD
registry. An ordinary registration failure separately attempts the newly opened
FD's close and retains its outcome. Checkpoints are outside ownership handoffs.

Counterexample trace revisited: a handled signal after opening a directory
child now sets a flag; registration/parent transition finishes, and the next
explicit input/output checkpoint fails into registered closeout. A failed
parent close causes failure while the child remains discoverable in the
registry. Source reads, selected-member processing, XSD writes and terminal
success contain cancellation checks. The final receipt alone may ignore that
flag to retain failure evidence. No later XSD phase is admitted by cancellation.

The proposal correctly calls the15s alarm a cancellation **request**, not a
hard wall limit. Blocking system calls can continue to root's separately owned
outer20s/5s timeout. Checkpoints are not atomic syscall barriers; partial bytes
or a just-created output can remain at a signal boundary. Root must reject
interrupted/ambiguous external outcomes rather than infer success from a packet.

### R2 — independent close attempts and terminal evidence

`close_owned()` records an attempt and removes the original handle from its
retry set before the one close attempt. A close error is retained, not retried
against a potentially reused integer. `close_all()` separately attempts each
remaining registered original and continues across individual close/accounting
errors. Ordinary input/resource/write finalizers close their originals; any
still-registered descriptor is available to final closeout.

Counterexample trace revisited: a failing output-directory close returns a
failure record, but does not bypass the parent-directory close. Final remaining
registrations or any non-CLOSED close event cause failure. The complete external
stdout includes close events, remaining registrations, errors and cancellation;
the return condition rejects failed status or a recorded signal. Post-receipt
fsync/close/cancellation failure cannot be hidden by the earlier packet status.

The in-packet RESULT now explicitly marks final closeout **PENDING** and is never
rewritten. The proposal requires external exit/close-event reconciliation. Its
byte diagnostic is correctly named as acknowledged completed writes, not an
inventory of partial failed files. The external receipt is still essential if
JSON generation, stdout, setup, severe allocation failure or a fatal OS event
prevents in-program accounting. This review does not claim Python can guarantee
cleanup receipts after SIGKILL/host loss/uninterruptible I/O.

### R3 — inherited running floors

Launch12GiB/25% checks remain. After all member bytes validate but before XSD
output, the program records and checks8GiB/20%; final capture resources are also
recorded and checked. Low samples remain in the result and trigger failure,
including a low final sample after otherwise successful XSD writes. The old
low-after-resource → CAPTURED/exit0 branch is no longer supported by the source.
Samples remain point observations, not continuous monitoring or protection
against intervening/later whole-host demand. No large monitoring framework is
claimed for this two-container/three-member, <=256KiB-output operation.

## Rechecked bounded scope

The two fixed whole-JAR identities remain the same1909475-byte sdklib and272256-
byte repository inputs recorded in v1. Their size/stability/SHA-256 checks still
precede ZIP parsing. The only three selected member identities are unchanged:

- `xsd/sdk-sys-img-01.xsd`:4481 bytes,
  `2eb33b82b4db5d24892e8b7c9ebb2eace2d309f6fc64c57103a034b7e2a9eaf5`.
- `xsd/repo-common-01.xsd`:15469 bytes,
  `91a12ddb132fbf2a56fef48030c0bd21a6032eff1e095d92d605ac8baadb151e`.
- `xsd/repo-common-02.xsd`:15510 bytes,
  `5f8b8c8b8bc3d965c4334c20d72cb83ff475012703bd922e6a3387d35efc36e3`.

Uniqueness, member type/encryption/compression, size and exact SHA-256 checks
remain, with all three validated before XSD creation. Containers are not copied
to evidence; only fixed names reach exclusive0600 output files in an exclusive
0700 directory. There is no extraction-derived pathname, replacement, deletion,
new dependency, old helper, Java/class loading, SDK command, subprocess-creation
call, network API, XML parser or schema-import resolution in this reader.

The source still limits container bytes, selected compressed/uncompressed bytes,
entry count, output bytes, address space, CPU, file size and open FDs as declared.
ZIP central-directory parsing precedes its entry-count check, but follows the
exact complete-JAR hash/size checks and uses those in-memory bytes. The containing
data is fixed-identity SDK input, not an arbitrary hostile-archive capability.
RLIMIT_FSIZE alone is not the total output bound; the separate five-name and
payload-size policy supplies that limit. Installed Python/system-library trust
and cooperative source/path stability are explicit; no hermetic or continuous
same-UID tamper proof is asserted.

## Root-only next action and evidence requirements

This is independent **source acceptance**, not the missing current execution
admission. Root alone may decide whether to perform the proposed one bounded
capture. Before any invocation, root must bind this exact source/proposal and
installed timeout/Python commands, refresh resources/output absence, coordinate
the foreground data read and retain bounded external command/start/end/exit and
stdout/stderr. The outer timeout process is root-owned; the reader's zero-child
declaration is not a process count for the entire invocation. Root must settle
its own actual controller/reader without signaling unrelated work.

Afterward, a claimed successful capture requires the actual external outcome,
both journals, all three exact XSD bytes/hashes, terminal close events/empty
registry and independent result reconciliation. Partial/missing journals or
nonzero/interrupted/uncertain outcomes stay failures/HOLD, with no overwrite,
namespace reuse, automatic capture retry or archived recovery helper. No Gradle
command is involved, so wrapper `--stop` is NOT_APPLICABLE to this capture only.

Even a successful capture will supply **inert source**, not a schema-chain
verdict. The full repositoryType/package/archive/complete linkage and common/02
counterexample must then be independently read against the retained catalog.
It will not prove actual SDK-consumer behavior, archive authenticity/checksum,
license acceptance, an installed image, boot, guest/process width or four native
KDF cases. No Android download/installation/emulator/ADB/build/test is admitted
by this report; PVA-001 remains OPEN.

## Resource and preservation note

At the review point, workspace free space was32082896KiB, `/tmp`21378112KiB;
MemAvailable43205384KiB of65855360KiB. This reviewer created only this compact
permanent report: no workers, cache, temporary file, archive extraction or build
output. Older accepted source/catalog reports and v1 disagreement are intact.
No closure/suspicion denominator or hardware gate gains credit. All STOP,
NO-RETRY, CLOSED, PVD/owner-decision and candidate1017001/publication boundaries
remain unchanged.
