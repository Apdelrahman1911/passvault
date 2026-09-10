# Isolated Linux batch03 — prospective raw-object transport

Author: `/root/editor`; 2026-09-09. **SOURCE ONLY: not execution admission,
retry/cleanup authority, test evidence, or a product-family closure.**

The consumed02 result is independently reconciled at
`../verification/LINUX-ISOLATED-BATCH02-ACTUAL-RESULT-REVIEW.json` (review SHA256
`d34f4e9890a35a72aa54a36c9dcbf09511396d49f2c4b8b95f5e5e4fb9379eca`).
Root released scheduling only. **Old02 R remains HOLD**: no adoption, deletion,
recovery, or automatic retry. No old runtime or transport data was read here.
Independent source challenge `LINUX-ISOLATED-BATCH02-RAW-TRANSPORT-SOURCE-CHALLENGE.md`
under that verification directory, SHA256
`e2e0c6a05fb981b7452f0bc8cc2522aca4312cb26edd792098a99288cdf69e9e`,
establishes an attribute-sensitive archive/raw-object contract gap, **not the
actual02 failing path or exclusive cause**. All earlier acceptances, consumed
attempts, failures and restrictions remain unchanged.

## Frozen candidate sources

| Source | SHA256 |
| --- | --- |
| This directory's `LAUNCH.py` (41,040 bytes / 720 LF) | `27f7a42d029e33aeb03700f90e215d64dd9cb781163e8f57529744789a6fd88e` |
| `scripts/audit/linux_isolated_batch_03.py` | `4ceaef097a1db97b673e7233aac5ff77acf817d298c2aa8adc33ec86be075c79` |
| `scripts/audit/isolated_batch_tests_03.init.gradle` | `6e333500d4a9d56599bd22417d89005d26940240228b1f0710773f5f441d0f45` |

The immutable reviewed02 outer is `../isolated-batch02/LAUNCH.py`, SHA256
`f65aecef66207da898c7bfda7d966ea235a2fe70f96b523ff977f32f8a80a6af`.
The03 delta is limited to fresh instance bindings, raw-object transport and
early origin evidence, the exact two-file transport cleanup allowlist, and the
pre-use coordination clarification below.
The inner/init are storage-lane name/hash rebindings; no workload change.

Workload remains C4 commit `da8ff89b9a8579017d5d524e628dff5251f2bb90`, tree
`cf0a8a702e7cd6948236be18b491bb5a21b5886e`, exactly **1,572 regular raw Git blobs**.
No current HEAD or later publication identity substitutes for C4. Manifest
`../current-cycle/SOURCE.json` remains SHA256
`2e8b82c91215bbfee9cf4a2a3f09bad386fc8515d7fd20263fbbfa5fe1217003`.
The CLASSES/METHODS sources and both checkout-EOL qualifications are unchanged.

## Narrow implementation and independent-review obligations

- Validate every manifest path/mode/OID before allocation or transport:
  1,572 unique safe relative paths, no empty/dot/traversal/`.git` components,
  no NUL, no file/directory ancestor collisions, regular modes100644/100755,
  executable `gradlew`, lowercase40-hex OIDs. Dictionary insertion order
  preserves manifest order; duplicate OIDs for different paths remain repeated.
- Exclusively allocate E then R. Immediately put original E/R pins, parent
  pins, source tuple and admitted packet images in the in-memory receipt and
  write/fsync flat `E/OUTER-ALLOCATION.json`, then fsync their parent directories,
  **before other directory creation or materialization**. This origin evidence
  is not successful materialization or cleanup admission. A record-write failure
  keeps the same origin data in the final receipt if that can be written.
  Later OUTER-INTENT retains the full private-subdirectory inventory as before.
- Create/fsync exclusive0600 `R/source.oids`: exactly1,572 ordered `<oid>\n`
  requests (64,452 bytes). Bind the capture to the original created FD's pin.
  Reopen that original no-follow regular file read-only/nonblocking, verify
  exact bytes/hash/FD-and-name pins before/after the Git child, rewind before
  launch, and pass that FD as stdin. No interactive pipe or whole-response RAM
  buffer. The isolated inner still requires DEVNULL stdin.
- Run the same admitted Git image against the same preserved Gitdir using
  plain `cat-file --batch`, no paths, filters, textconv, follow-symlinks or
  attribute overrides. Keep clean environment, NO_REPLACE/NO_LAZY, no prompts,
  no global/system config, optional locks off, safe hooks/fsmonitor/GC/
  maintenance/protocol/signing settings. Remove only the archive transport,
  tar parser/import and now-inapplicable attributes/tar-umask options.
- Bind `R/source.blobs` capture to its original Git-output FD pin, then parse
  the original reopened FD twice under one180-second materialization deadline.
  Pass1 checks **all** responses before writing source files/nested directories;
  pass2 rewinds the same descriptor and repeats framing and digest checks while
  materializing. Never trust saved offsets. Each at-most64-byte header must be
  exact expected lowercase OID, literal `blob`, canonical decimal size0..32MiB,
  and LF. Size is never checkout_size. Hash exactly that many payload bytes as
  `SHA1("blob <raw-size>\0" + payload)`; require one framing LF. Exactly1,572
  responses, true EOF, original byte count, whole-stream SHA256 and stable
  original FD/name pins are required on both passes. Capture/hash is checked
  again after source_check. The receipt keeps compact request/response images;
  the transport payload itself is disposable, not an artifact to upload.
- Preserve descriptor-relative no-follow/O_EXCL extraction, admitted file
  devices/ownership, raw digest before final mode/fsync, periodic cancellation/
  resource checks and unchanged outer raw SHA1/SHA256 source_check before/after
  the inner. Checkout hash/EOL differences remain recorded, not normalized away.
  Replace only the top-level `source.tar` cleanup allowance with exact
  `source.oids` and `source.blobs`; add no early-failure cleanup path.

Reviewer counterexamples: wrong/missing/out-of-order OID/type; malformed,
oversized, noncanonical or truncated header/payload; missing framing LF;
trailing response/junk; empty blobs; binary NUL/newline/CRLF payloads unchanged;
duplicate OID at two different paths; unsafe/duplicate/ancestor-colliding paths;
original transport file substitution or mutation; and record failure before
materialization. These are **source-review obligations, not executed cases**.

## Admission, execution and cleanup boundaries

Fresh fixed names under the continuation base:

- R: `/root/projects/PassVault/audit-runtime-linux-isolated-batch03`.
- E: `runs/linux-isolated-batch03`; RUN `linux-isolated-batch03`.
- REQUEST: `requests/LINUX-ISOLATED-BATCH-03.json`.
- APPROVAL: `reviews/verification/LINUX-ISOLATED-BATCH03-INSTANCE-ACCEPT.json`.
- PURPOSE: `ONE_LINUX_ISOLATED_BATCH03`; SELF: this new `LAUNCH.py`.

Root alone owns one audit job across local/CI. Source acceptance must precede
any fresh request/independent instance acceptance; neither this plan nor prior
attempts authorize entry. Parent PID/birth is captured at entry/under lock as
provenance, not frozen into the unchanged11-key request or signalling authority.
The externally frozen reviewer approval separately binds source/request hashes.
Original lock, parent/image pins and mixed directory23/regular-file24 device
model still require genuinely fresh reviewed intake.

### Pre-use coordination clarification

The exact previously reviewed outer/plan are preserved as inert beforeimages:
`LAUNCH-before-29893099.py.txt`, SHA256
`29893099b6412be4b258978cf01695d8727f5e01ba508d7fe018bab07564038e`, and
`PLAN-before-087c52a8.md.txt`, SHA256
`087c52a8bf1202ead8818b712062f82c741cc80814e36af6ce6dae26c73d7101`.
The sole subsequent code change replaces nested `agents_quiescent:true` with
`other_agents_audit_execution_quiescent:true` and adds fixed
`parallel_source_work:"BOUNDED_NONOVERLAPPING_INERT_ONLY"`. Top11 request and
seven approval keys, sole root build ownership, no-CI guard, inputs, workload
and every execution/cleanup guard are unchanged; no instance admission follows.

Parallel agents are limited to assigned, non-overlapping, bounded inert source
reads/edits **outside frozen03 inputs, Git/object store, R/E/lock, SDKs,
toolchains and caches**. No builds/tests/CI/Git, imports, probes, network,
background or heavy work is admitted for them. These externally coordinated
limits are not a global-idle proof or permission to mutate reviewed inputs.

Fixed prospective entry, no arguments or optimization:

```text
/usr/bin/env -i PATH=/usr/bin:/bin LANG=C.UTF-8 LC_ALL=C.UTF-8 TZ=UTC /usr/bin/python3.12 -I -B -S /root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/reviews/isolated-batch03/LAUNCH.py
```

Unchanged supervision:6,000 seconds overall, work cutoff5,250, original stop
drain up to750, one original-child pidfd kill plus one final five-second wait;
Git output128MiB/120seconds; host screen8,192 entries/5seconds; disk/RAM floors
12GiB/25% at entry and8GiB/20% running. Original pidfd precedes every child poll;
SIGCHLD default and handler-before-unblock/verified empty signal mask remain.
No host-idle/no-escape or guaranteed userspace deadline/settlement claim.

The unchanged five-phase inner uses JDK17 and the checked-in wrapper, one worker,
non-daemon, no configure-on-demand, intact dependency verification and serial
Detekt. Its14-class mapping has166 declared regressions and one fixture producer,
**zero executions established by this source work**. Task inventory is not
Android32 derivation evidence; no hardware/Apple/Windows credit follows.

Keep compact logs/XML/hashes in new E. Remove only the admitted original new R
after exact original unshare exit0/1, all five inner safety booleans, namespace,
outer source/authority checks, parent mount refusal and complete descriptor-bound
inventory. Exit70/uncertain settlement never authorizes cleanup or a retry.
Receipts remain preterminal; actual exit is authoritative. No old02 origin or
cleanup authority is created retroactively. PVU-007 STOP, PVU-011 NO RETRY,
PVA-029 no automatic retry, G7/G8 CLOSED and all consumed scopes remain intact.
GUI execution remains separately admitted; inert GUI source work follows the
coordination limits above.

This author performed source reads, textual diffs/hashes and edits only to
the assigned LAUNCH.py/PLAN.md and exact inert beforeimages above. No helper execution/import/AST/compilation,
Git/CI/build/test, runtime/cache/process/mount probe, temporary task or worker
was started. No large generated data or disposable process was created here.
