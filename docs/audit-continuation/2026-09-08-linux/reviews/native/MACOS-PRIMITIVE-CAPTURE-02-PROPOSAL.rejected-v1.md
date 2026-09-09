# Proposed changed-reader capture02 — not admitted or executed

Author `/root/native`, requested reviewer `/root/native_review`, sole execution
owner `/root`. This is the **one corrected source-data-reader proposal** root
allowed after capture01 failed. It is not permission to retry any command.
Capture01 remains consumed: three metadata pipelines failed (curl23/reader1),
zero verified response/commit/source identities, nine source slots unstarted.
Its failure/command records remain byte-preserved. Do not execute this proposal,
reader, shell vectors or any referenced downloaded source without new root
admission following the independent source review.

## Changed source and why it is different

`CAPTURE_MACOS_SOURCE_DATA.py`: **268 LF /11,956 bytes**, SHA-256
`b963963180e12dcf952f5356a7a06d3364450150804bc23e25c25a4da984a1f9`.

This is a standalone standard-library stdin **data reader**. There is no shell
construction, `-c` source quotation, network call, subprocess, API invocation,
dynamic code import, compiled probe or target execution in it. Root would pass
one fixed identifier to a normal Python file argument. The failed JSON-string
shell interpolation is not reused. No syntax check, import, control, test or
reader execution has yet validated the correction.

Reader responsibilities: exact finite identifier/repository/path/cap selection;
bounded stdin; duplicate/nonfinite JSON rejection; commit/ref/repository and
pinned Contents-API metadata checks; strict Base64 and Git blob size/hash checks;
exclusive no-follow report creation; compact inert source text plus original LF
section ranges/full-source hashes; and independent transport-vs-parse accounting.
It never opens/queries vaults, Keychains, credentials, signing stores or tools.

## Exact source-data inventory and limits

Only these three metadata URLs may be considered in phase1:

| ID | Exact URL | Maximum body bytes |
| --- | --- | ---: |
| xnu-commit | `https://api.github.com/repos/apple-oss-distributions/xnu/git/ref/heads/main` | 32768 |
| libc-commit | `https://api.github.com/repos/apple-oss-distributions/Libc/git/ref/heads/main` | 32768 |
| cmake-commit | `https://api.github.com/repos/Kitware/CMake/commits/v4.4.2` | 32768 |

The first two official ref endpoints require an actual `commit` object for
`refs/heads/main`; an annotated/missing/wrong ref fails closed. Kitware's exact
requested release version comes from the approved image declarations, not a
guessed new release. Its returned canonical commit/repository URLs and SHA must
validate. Capture01 did **not** establish those refs, and their prospective
availability is not asserted. Missing/failed/oversized data is HOLD, no fallback.

Once root has adjudicated a metadata pipeline's actual curl0/reader0 and sealed
the returned commit C, any selected phase2 URL must be recorded **before GET**
as `https://api.github.com/repos/REPOSITORY/contents/PATH?ref=C`. Each exact
`ID.intent.json` contains only `capture`, `id`, `url`, `cap` as required by the
reader. No URL with an unfilled/mutable commit may be requested. The returned
official `url`, `download_url`, file path, type, encoding and Git blob must
confirm the candidate; a missing guessed path stays unavailable rather than
being silently replaced. No raw-download second GET is needed.

| ID / repository | Candidate path to confirm | Response cap | Retention |
| --- | --- | ---: | --- |
| xnu-event / apple-oss-distributions/xnu | bsd/sys/event.h | 65536 | complete bounded header |
| xnu-kern-event / same | bsd/kern/kern_event.c | 786432 | exact process-filter LF sections only |
| xnu-proc-info / same | bsd/sys/proc_info.h | 131072 | complete bounded header |
| xnu-libproc / same | libsyscall/wrappers/libproc/libproc.h | 65536 | complete bounded header |
| xnu-vm-statistics / same | osfmk/mach/vm_statistics.h | 65536 | complete bounded header |
| xnu-host / same | osfmk/kern/host.c | 262144 | exact HOST_VM_INFO64 LF sections only |
| libc-acl / apple-oss-distributions/Libc | include/sys/acl.h | 65536 | complete bounded header if confirmed |
| cmake-process / Kitware/CMake | Source/CTest/cmProcess.cxx | 131072 | complete bounded source |
| cmake-chain / same | Source/cmUVProcessChain.cxx | 131072 | only if the prior captured process source requires it |

At most **12 responses /1,802,240 body bytes**, below root's2MiB ceiling.
Metadata plus file caps, not guessed actual body sizes, define that limit.
No source body is evaluated. Process sections select `filt_proc` anchors +/-80
LF lines with an1800-line total cap; VM sections select `HOST_VM_INFO64` +/-130
with a1000-line cap. These are explicitly **bounded source reads**, not complete
implementation audits. Missing/truncated needed semantics remain unadmitted.
Whole API/Base64 envelopes and unrelated source portions are not retained.

## Root-owned external invocation, once and fail-fast

The reader does **not** start or settle curl. Root must independently seal the
installed Linux curl/Python executable identities, fixed wrapper argv and
trusted checkout/evidence ancestry before any future invocation. The preserved
capture01 shell orchestration is not an execution helper to reuse.

Proposed exact per-ID shape (a specification, **not a command to run now**):

```text
INSTALLED_CURL -q --proto =https --connect-timeout 5 --max-time 20
  --max-filesize EXACT_CAP --fail --silent --show-error EXACT_PINNED_URL
  | INSTALLED_PYTHON -I -B
      docs/audit-continuation/2026-09-08-linux/reviews/native/CAPTURE_MACOS_SOURCE_DATA.py
      --id EXACT_FIXED_ID
```

Use direct file argv, no `-c`, no shell-interpolated Python and no generated
downloaded program. The surrounding root-owned wrapper must set pipefail,
capture both `PIPESTATUS` values immediately and **stop the entire capture02
batch at the first failed/ambiguous pipeline**. No retries or continuing through
the same reader fault. No credentials/config/cookies/authorization header,
redirect-following, proxy credentials, user startup/profile or timeout retry.
Use an explicitly reviewed minimal environment and HTTPS transport only.

Before each network command, root must durably create the exact ID's one-shot
attempt record exclusively under the newly created private evidence parent;
an existing outcome/attempt/source section is a consumed/collision HOLD, not
an overwrite or continuation. Prebind reader/source hashes and exact URL/cap,
then capture exits separately. This cooperative external one-shot authority
is **not implemented or guaranteed by the stdin reader alone**. There is no
request activation in this packet and no parent/nonce is borrowed from01.

Each allowed pipeline has5s connect/20s curl total limits, bounded response
bytes and a separately reviewed root cancellation/outer bound. Root must stop
only the owned curl/data-reader command if required, observe its exit and
preserve interruption/unknown-stream limits. No worker-settlement, shell
timeout or kernel API guarantee follows merely from Python parsing. These
external invocation/cleanup details need explicit root instance admission.

## Evidence parent, transport and failures

New parent, **not created by this proposal**:
`reviews/native/macos-primitives-02` below the continuation report directory.
Root creates it exclusively with private mode0700 and validates its ancestry,
ownership and current identity. Reader retains its opened no-follow directory
descriptor and checks owner/private mode. This is a cooperative, frozen source-
capture boundary, not an adversarial same-user or ancestor-race sandbox. The
reader neither creates nor adopts a missing parent and cannot protect unreviewed
ancestor traversal by itself.

No source-data parse is called a network pass. The reader's best status is
`BODY_PARSED_TRANSPORT_UNADJUDICATED`. Root then writes the exact
`ID.transport.json` from actual pipeline observation with `capture`, `id`,
`url`, `response_sha256`, integer `curl_exit:0`, integer `reader_exit:0`, and
`adjudicator:"/root"`. These are point-bound owner records, not cryptographic
proof of who wrote a mutable same-user file. Independent review/root frozen
inputs, rather than the string alone, establish their provenance. Dependent
file records require the applicable metadata transport record; the last CMake
slot additionally requires its preceding process record/transport and actual
`cmUVProcessChain` reference. Missing ones fail without any substitute URL.

Every report/source write is exclusive and single-attempt. Partial writes,
fsync/close errors, incomplete prefixes, exception-before-parent-open and hard
interruption may leave only partial/no reader receipt; root retains the actual
wrapper outcome rather than claiming cleanup. No deletion or close retry is
implemented. The small permanent receipts and necessary source sections stay
as audit evidence. No source, shared cache, toolchain, SDK, unrelated file or
process is a cleanup target. No temporary file, build output or imported code
is needed; Gradle stop is NOT_APPLICABLE, not historical clearance.

## What even a successful future capture would not prove

Apple upstream main is not the future macOS15 kernel/libc identity. Public
headers/source do not prove target symbol availability, structure ABI,
Mach process authority, complete descendant observation, exact signals,
available-RAM behavior, ACL/traversal safety or CTest binary/source equivalence.
Those gates require separate implementation, full independent review and real
target controls within root's one later admitted batch. No product/native test,
workflow execution, physical device result or family closure follows from
source capture. Capture02 currently has **zero requests and zero executions**.

PVU-007 STOP, PVU-011 NO RETRY, PVA-029 FAIL/no automatic retry, G7/G8 CLOSED,
Windows01 FAIL/filesystem HOLD/consumed request, publication/version/identity/
dependency/protected-ref/build1017001 restrictions and root's sole local/CI
execution slot remain unchanged. Actual local database workers and closeout
must settle before any future Mac job; this proposal admits no CI job.
