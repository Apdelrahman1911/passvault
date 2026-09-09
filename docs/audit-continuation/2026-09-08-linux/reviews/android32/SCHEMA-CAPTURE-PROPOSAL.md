# PVA-001 — fixed local three-XSD packet capture

Author `/root/android32`. **SOURCE PROPOSAL ONLY; NOT EXECUTED OR ADMITTED.**
Root alone may execute after independent source challenge and explicit current
admission. This is not the Android image downloader, installer, emulator runner,
old validation runner or a general archive utility.

## Rejected v1 and source-only successor

The independent `../build-config/ANDROID-XSD-CAPTURE-V1-SOURCE-REVIEW.md`
(SHA-256 `09ce30ec153c663eeca8f8745f25f15e3b4ba76ff2cbdf46a4673b97df881275`)
returned **REVISE**, not execution admission. Its exact before-images are kept
as inert permanent evidence:

- `CAPTURE_FIXED_XSDS.rejected-v1.py.txt`, SHA-256
  `9650b9f61f92853bdec13853296998e75112daa4d3ae3ecb68e6649be372b83b`.
- `SCHEMA-CAPTURE-PROPOSAL.rejected-v1.md`, SHA-256
  `aa652d7964a99e81a6b4fca053f8bb3c5c853b7081db9e23722eaf5b0f9f36f7`.

The current **v2 source proposal has not been executed/imported/compiled/tested**:

- R1: handled signals now only record cancellation; they never throw across
  acquisition/registration. Original directory/input/output/resource FDs are
  registered, including a new child before its parent close. Explicit safe
  checkpoints cancel before the next input/output and before success.
- R2: final closeout attempts each registered original independently, including
  after cancellation. Every ordinary close attempt has an external close-event
  record; uncertain closes are not retried. The in-packet receipt explicitly
  precedes final closes and cannot establish successful cleanup alone.
- R3: after member validation but before XSD output, and again after capture,
  observations enforce the inherited8GiB/20% running floors. Low samples remain
  evidence and cause failure, not a captured-success result.

These are author corrections awaiting a separate full successor challenge,
not executed controls or new application findings. Severe allocation/runtime
failure can still prevent accounting or receipts; root must retain actual
outer exit/interruption evidence. No rejected source is executable authority.

## Purpose and preserved uncertainty

The independent `../build-config/ANDROID-SCHEMA-SUPPLEMENT-REVIEW.md` accepted
quoted schema interpretation only conditionally. The complete applicable
`repositoryType -> remotePackage -> archives/archive -> complete/checksum`
chain and common/02 counterexample remain independently UNVERIFIED. Previous
reads did not retain a complete inert schema packet. This capture would supply
exact full texts for a reviewer; **the script makes no schema/algorithm verdict**.

It preserves the prior catalog, result, proposal and review bytes and their
historical uncertainty. It performs no HTTP request, schema-import resolution,
XML validation, JVM/class loading, SDKmanager, ADB/emulator, build or test.

## Frozen, local inputs and outputs

Only these two already-hashed installed containers may be read:

| Container under `/opt/android-sdk/cmdline-tools/23.0/lib/` | Bytes | SHA-256 |
| --- | ---: | --- |
| `sdklib/tools.sdklib.jar` |1909475| `d0bbe0e03155a7bdd9bac03567b6b26797f81a778f55d5eadc3e71e7a0a27434` |
| `repository/tools.repository.jar` |272256| `9fd419f3307be633aaf7b02b52832521c1300d4bf9058cee83bb11139990a7f9` |

Only these three member byte sequences may be captured, with no text/newline
normalization:

| Container/member | Output | Bytes | SHA-256 |
| --- | --- | ---: | --- |
| `tools.sdklib.jar:xsd/sdk-sys-img-01.xsd` | `sdk-sys-img-01.xsd` |4481| `2eb33b82b4db5d24892e8b7c9ebb2eace2d309f6fc64c57103a034b7e2a9eaf5` |
| `tools.repository.jar:xsd/repo-common-01.xsd` | `repo-common-01.xsd` |15469| `91a12ddb132fbf2a56fef48030c0bd21a6032eff1e095d92d605ac8baadb151e` |
| `tools.repository.jar:xsd/repo-common-02.xsd` | `repo-common-02.xsd` |15510| `5f8b8c8b8bc3d965c4334c20d72cb83ff475012703bd922e6a3387d35efc36e3` |

The only newly created directory would be
`docs/audit-continuation/2026-09-08-linux/reviews/android32/schema-capture-01/`.
It must not exist before the run. Exactly three XSDs plus `INTENT.json` and
`RESULT.json` are allowlisted. An occupied directory, changed input, duplicate
member or failed capture is a stop, **not automatic retry/reuse/overwrite**.
No container JAR, other SDK member or application binary is copied to reports.

## Exact proposed invocation — not executed by the author

After root binds the final script/proposal SHA-256, obtains independent
acceptance and separately admits this one read:

```text
timeout -k 5s 20s /usr/bin/python3 -I -B /root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/reviews/android32/CAPTURE_FIXED_XSDS.py
```

No arguments, configuration override or import mode is used. Root must retain
the exact external command, script hash, start/end/exit and bounded stdout/stderr
with its admission. The in-program self hash is a point read, not an independent
proof of the bytes Python previously executed. The root launch binding and
cooperative source freeze are still required. Other agents do not execute it.
This metadata capture does not create/adopt the build lock, reserve/release the
build slot, discharge a CLOSED obligation or admit a build/test job; root
coordinates the short foreground data read with any already owned work.

## Bounds and containment

- Linux, isolated standard-library Python, bytecode writes disabled; no shell,
  subprocess, network library, downloaded code or XML parser is used.
- Exact file sizes/hashes are checked before ZIP parsing. Each container is
  <=2MiB; total expected container bytes2181731. Parsing uses the already
  verified in-memory bytes, not a reopened mutable JAR path.
- At most10,000 ZIP entries per pinned container; only the three exact uniquely
  named regular/nonencrypted stored-or-deflated members are read. Each selected
  compressed and uncompressed member is <=32KiB. All exact member sizes/hashes
  are checked before writing any XSD; total XSD output35460bytes.
- Source paths are fixed and traversed with directory FDs and `O_NOFOLLOW`.
  Each regular input is compared by fstat/path identity before/after its bounded
  read. No generic `extractall`, source-member-derived output path, path glob or
  execution/import of JAR contents exists.
- The output directory is exclusively created0700. Only five fixed0600 regular
  files may be created with `O_EXCL|O_NOFOLLOW`, written/read back through their
  owned FDs, and flushed. JSON <=64KiB each; total output <=256KiB. No temporary
  directory/file, rename, deletion or shared cache/SDK write is performed.
- Per-process address space128MiB, CPU soft5s/hard6s, file size256KiB,
 64 open FDs; in-program15s cancellation-request alarm plus the root-owned
 outer20s/5s TERM/KILL limit. The alarm is not a hard wall limit: its non-throwing
 handler is observed at explicit safe checkpoints; blocking I/O may continue
 until the external hard exit (and uninterruptible I/O remains a limitation).
  These limits constrain this reader only, not another job or whole-VPS usage.
- Before output creation require inherited12GiB free disk and25% available RAM;
  also record/check8GiB/20% before XSD output and after capture. These are bounded
  point samples, not continuous whole-host resource containment or a promise
  about intervening/later unrelated demand. The externally bounded read is not
  a long application task; it writes at most256KiB. At00:25:01Z, the source-only point
  observation was23093308KiB worktree free,21556296KiB `/tmp` free and44097 of
 64311MiB RAM available. Root must refresh facts; those values are not admission.

The ZIP library necessarily parses central-directory metadata before the entry
count check. Exact complete-container hash/size checks happen first, and the
process address-space/CPU/wall caps also apply. This is a capture of three
fixed trusted-identity SDK data members, not a general hostile-archive framework
or a complete proof against malicious same-UID path mutation.

## Cleanup, interruption and result interpretation

No child/daemon/worker is spawned by the reader; the separate timeout controller
is root-owned. Original directory/input/output and `/proc/meminfo` FDs have an
ownership registry. Non-throwing signal recording leaves acquisition/registration
handoffs intact; a parent close failure cannot lose its newly registered child.
Closeout ignores cancellation and independently attempts each registered close;
an uncertain close is removed from the retry set and retained as failure rather
than risking a reused FD integer. An ordinary registration failure also attempts
its new FD close. Context-managed in-memory ZIP buffers are discarded at process
exit. `-B` avoids `__pycache__`; there is no temporary file to delete. The small
successful or partial output packet is permanent evidence,
not a disposable cache. A failed/partial receipt stays failed; no previous report
or namespace is rewritten to hide it. Gradle `--stop` is NOT_APPLICABLE to this
read, without resolving any earlier stop obligation.

TERM/INT/HUP/alarm/CPU-limit signals request cancellation at safe checkpoints;
ordinary closeout still attempts a bounded final failure receipt. Checkpoints
are not an atomic syscall/mutation barrier: a signal arriving at a syscall edge
can leave partial data, but a recorded cancellation must not yield successful
terminal completion. No next input/output is started at a checkpoint with a
pending cancellation. Only the final JSON receipt may ignore that flag, so
failure/cancellation evidence can be retained without further XSD work.
SIGKILL, process crash, uninterruptible I/O or host failure can prevent a final
receipt; kernel descriptor closure/process exit does not invent that receipt.
Root must reconcile the actual external exit and interruption status, exactly
three complete output hashes, both journals and the final external close-event
summary; neither a printed success nor `RESULT.json` alone establishes a
completed command. The in-packet receipt explicitly leaves final closeout
PENDING. A subsequent close/fsync/cancellation failure causes nonzero exit and
external failure without rewriting that receipt. The external byte diagnostic
counts only acknowledged completed writes; partial failed files must be observed
and hashed separately rather than pretending that number is their total size.
If interruption or receipt failure occurs after data writes, retain the original
small packet and record the failure. No automatic capture replay or old recovery
helper is authorized.

After an admitted capture, a reviewer other than the author must read the full
applicable schema linkage and contrary definition, rehash the retained files,
bind the existing catalog namespace/selected record and distinguish schema
semantics from actual SDK-consumer behavior. There are **zero application
cases**, even if the byte capture succeeds. SHA-1/TLS trust, a publisher digest,
archive bytes and KDF correctness are not verified by capturing source text.

## Exact next API24/default/x86 gaps

1. Independent challenge of this script/proposal; root's one admitted local
   capture; independent complete-chain review. No new remote URL is needed for
   this local evidence gap if the expected installed JARs remain available.
2. Explicit authority for the captured Android SDK license and acceptance of
   the legacy SHA-1/TLS artifact trust policy, or a verified stronger publisher
   digest if that policy requires one. No license was accepted here.
3. A separate reviewed fixed downloader/stager and descriptor-bound closeout
   for the313489224-byte `x86-24_r08.zip`; actual ZIP layout and image identities
   remain unknown. This reader must not be repurposed as that installer.
4. Fresh exclusive slot/lock, resource reservation and proven USB/network/
   process containment; emulator/ADB option/child/listener contracts. KVM is
   unavailable at the prior observation, software-emulation feasibility is
   unmeasured, and no privilege/host configuration change is admitted.
5. Only after those controls: one admitted image preparation and one separately
   admitted API24/x86 software cold boot, then a separate compile/package/native
   vector run. No automatic retry, fallback matrix, ARM32 equivalence, hardware
   proof or build1017001 replacement follows.

Prior accepted evidence and all PVD boundaries, PVU-007 STOP, PVU-011 NO-RETRY,
PVA-029 FAIL/no automatic retry, G7/G8 CLOSED and publication restrictions remain
unchanged. Qualified closures19/25 and22/37, and conclusive suspicions2/12,
gain no credit from this proposal or a future data-only capture.
