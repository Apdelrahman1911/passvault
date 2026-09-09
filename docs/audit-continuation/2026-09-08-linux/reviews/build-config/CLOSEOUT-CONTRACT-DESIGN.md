# LC-LAUNCHER-CONTRACT-001 — proposed17 callable component controls

Author: `/root/build_config`; independent challenger: `/root/baseline_coverage`
(review **pending**). `/root` alone owns execution, coordination, outer admission,
central ledgers and publication. This is **SOURCE_PROPOSAL / NOT_ADMITTED**:
**17 planned cases,0 executed**, no syntax/import/compile/AST evaluation or
runtime allocation during authoring. Hashing/source reading is not a test.

This is a separate control instance, not an extension/rebind of the previous28
LC-C05/C06 controls or their four-name-per-case fixture allowlist. Those frozen
files remain unchanged and unexecuted. The separately proposed registration
MemoryError fault/success controls are not smuggled into these17 cases.

## Exact subjects and historical qualifications

All paths in this section are relative to
`/root/projects/PassVault/passvault-linux`.

| Subject/input | SHA-256 | Bytes / physical LF |
| --- | --- | --- |
| `scripts/audit/linux_database_closeout.py` | `8437c693d5a1becb8ed06ed549a5e837b8e4820c83a047c841fbcc0289f4b517` | 59,609 /1,100 |
| `.../reviews/linux-closeout/PLAN.md` | `0b21ebfee0134c2853cfa8f3d17f9fef1b9acf812329581a93c0790c69930c5d` | 34,814 /561 |
| `.../reviews/verification/METHOD-INVENTORY.json` | `40ac2f07ea2c98d1a320e75bb59d7bbc6b3ec954efd94b349257103aaccd8979` | 73,265 /1,207 |
| `.../reviews/build-config/CLOSEOUT-CONTRACT-CONTROLS.py` | `328cf3bcd5d8e94c032eaf69d287b3360e4e6d3be35b0118a69a9dd5e6c25980` | 46,695 /837 |

The previous closerb16774ff/PLANcffc6935 remains historically frozen and
**PREBUILD_CLOSEOUT_VIABILITY_HOLD**. Its single `launcher.evidence` field could
not simultaneously equal the directory string and be an array of file rows.
Read the independently confirmed source-impossibility report
`../baseline-coverage/CLOSEOUT-LAUNCHER-CONTRACT-DEFECT-REVIEW.md`
(`0d43fbaf286539d6e09f183b969bb34feec42ec9c142df2284cca9ba734197cd`)
and its paired JSON
(`3982b56c2f87c8b39dad6a936d69eac5341e739e1cf66ee1f26d6f9dbeeafb5d`).
That report preserves the prior reviewer miss and historical acceptances; none
floats to this successor and no executed old-version failure is invented.

The first schema successor0bed64d9 separated `evidence` from `evidence_files`.
The current8437c693 retains those exact contract/consumer changes and adds only
the separately challenged conditional directory-FD registry unpublication
before the original local close when pin-dictionary registration throws.
This proposal reads the current source and binds exactly8437c693, not whichever
future source happens to contain matching function names. Review of the current
closer and these controls is separate from any execution/instance admission.

The fixed handoff commit/tree strings remain
`9bdf9559b7a801a6f2fa49be3d7836ea6ef4d0ed` /
`05014e9f635131d5db06701e4013b4b5a746465a` inside the tested literal contract.
They label fictional test records and the real frozen method-inventory input;
they do **not** attest that these fixtures are that1198-file Git checkout.

## What will actually execute, only after separate admission

The callable verifies the exact supplied closer hash/size/LF **before** parsing.
It selects the whole, unmodified literal definitions:

```text
Hold require canonical sha strict_json identity absolute relative under
Directories event_rows parse_journal run_contract original_targets
```

It selects the literal source assignments `COMMIT`, `TREE`, `METHODS_SHA`,
`PRIVATE`, `GENERATED` and `LABELS`, not replacement mock values. The real
`strict_json`/duplicate-key hook, SHA/canonical functions, event parser and every
predicate in **the entire `run_contract` function** run. The two positive cases
must reach its normal seven-value tuple return and match the complete expected
request/result/original/launcher/events/source/owned-set objects. No prerequisite
guard is replaced with True or skipped by a stub. `time.time_ns()` and monotonic
time use the actual standard-library clock, not a mocked successful interval.

The second component is the literal final top-level `for` node in `admission`,
immediately before its return. Only a small new wrapper is synthesized; the
selected loop node and its body are not rewritten. At the bound source it is
lines700–703,280 bytes/4 LF, SHA-256
`6ad2dfdb823cb76507c1f7dabb75900a33fe47d3cde9a33bb0b9dca2eef3e84e`:

```python
    for row in launcher['evidence_files']:
        data, pin, digest = dirs.read(row['path'], MIB, durable=True)
        require(pin == row['identity'] and digest == row['sha256'], 'durable launcher evidence mismatch')
        captured[row['path']], pins[row['path']] = data, pin
```

That component uses the real selected `Directories.read` and its actual
no-follow reads, streaming hash, EOF, full pre/open/post FD/name identity,
file/parent `fsync`, and input close. Both normal cases must durably read and
capture all four actual synthetic files in order. The two mismatched receipt
cases must pass the full contract first, perform exactly the first real durable
read, then refuse before storing it or opening a later file. Distinct pin-only
and hash-only mutations make each comparison discriminating.

The original_targets function also runs unchanged after each normal contract
return. It checks the coherent **in-memory**35 allocation-intent/original pairs,
parent/sequence/private-mode/UID relationships and33 ordered projected targets.
This is a pure record projection, **not**35 filesystem allocations or verified
original runtime provenance. `main`, the rest of `admission`, `Guard`, `Forest`,
`Journal`, process/mount/resource probes, `source_check`, actual coordination,
target traversal and deletion helpers are never compiled/called. No validation
runner or rejected/archived helper is imported, extracted or replayed.

Consequently these controls do not prove whole C01–C07 admission, C05 journal
effects, C06 cancellation effects, original runtime cleanup, real accepted
producer receipt generation, whole-main integration or hardware behavior. They
cover the actual finite schema/parser/consumer components that the previous28
controls did not cover. An unchanged full-function positive return is stronger
than a source-substring/mock assertion but remains synthetic component evidence.

## Fixed once-only launcher/callable interface

```text
run_contract_controls(*, scratch_fd, scratch_path, original_scratch,
                      source_bytes, methods_bytes, emit, cancelled)
```

There are no mode switches, optional alternate paths, hash overrides, adaptive
source selection or retry entry. A module-local consumed latch additionally
refuses a second call in the same loaded namespace; the durable once-only
instance/receipt restriction must still be enforced by the outer launcher. A
fresh module reload cannot legitimize a previously consumed instance.

Root must first obtain separate source and actual-instance review for this
harness/DESIGN/current closer/reviews, new outer source/PLAN, exact byte/pin
inputs, retained verified original Linux coordination, no-overlap/resources,
evidence destination, cancellation, timeout and cleanup. The old28 launcher
allowlist is insufficient. Historical macOS lock/process identities and moving
execution to Linux/CI provide no authority. Do not recreate or adopt a different
current coordination-lock inode to manufacture fresh permission.

Root exclusively creates, records and retains the original0700 top:

```text
/root/projects/PassVault/audit-runtime-linux-closeout-contract-controls-01
```

This path is **prospective and uncreated/unprobed by the author**. No preexisting
top may be adopted, even if empty. Root supplies its borrowed original FD and
exact stable `{dev,ino,uid,mode}` pin, retains no-follow ancestry/name witnesses
and owns final original empty-top removal and its descriptor settlement. The
callable neither creates/closes that borrowed top FD nor removes its name.

`source_bytes` and `methods_bytes` must be real bounded original-bound input
captures. The actual frozen73265-byte method inventory is copied into the
private fixture, reread/hashed, and supplied to the actual contract. Its literal
METHODS_SHA guard is **not** changed to an invented synthetic digest. This adds
no execution credit to its105 source-declared application test methods.

Root's `emit(dict)` must durably retain bounded evidence or raise. Its
`cancelled()` must expose a monotone cancellation latch, not clear it or start
another operation. Signal handlers must be installed **before** outer scratch
creation, latch only, and never asynchronously raise inside descriptor ownership
transfer. The callable installs its ownership/cleanup `finally` before its first
directory/file open. No callable operation spawns/signals/stops a process.

## Synthetic packet and eight actual files

Only these eight names are allocated in the one original top; no child
directories, substitutes, links, renamed aliases, logs or runtime targets:

```text
runner.input  plan.input  source.input  methods.input
LAUNCHER-stdout.log  LAUNCHER-stderr.log
LAUNCHER-resources.json  LAUNCHER-processes.json
```

The first two contain visibly inert synthetic text, not an executable runner or
actual admission plan. The third is a bounded1198-distinct-path/mode manifest,
including a hypothetical wrapper trio and two synthetic EOL models. Its declared
source rows are **not created as filesystem files**. It explicitly omits actual
checkout content/provenance assertions; the full tested function requires only
its count, canonical paths, regular Git modes, EOL-entry count and noncollision
with generated prefixes. The EOL examples hash actual two-/three-byte in-memory
LF/CRLF strings; they are not historical PowerShell compatibility coverage.
The fourth is the exact real method inventory. The four launcher files are
clearly marked synthetic bytes; their contents are not process/resource samples.

All eight files are exclusively created0600 with no-follow original RW FDs,
separate retained original read FDs **before** writes, real write/fsync/readback,
and full eight-field identity/hash records. They remain immutable across the17
scheduled cases and are cleaned once in the suite finally. Repeated specified
component reads are finite matrix cases, not automatic retries of failed actual
run/admission/cleanup operations.

Per immutable input cap is128 KiB; per launcher file4 KiB; actual aggregate
fixture bytes are checked **<=512 KiB**. The entire canonical serialized packet,
counting its four immutable inputs and four external fixture byte values once,
is separately checked **<=512 KiB** before each case, including after mutation.
One case packet is constructed at a time and discarded on return; only compact
case summaries remain. These byte caps describe input/fixture data, not a claim
that Python objects/interpreter RSS fit in512 KiB. The outer must establish its
own reviewed address-space/RAM floor and time/resource bounds before launch.

Other fixture request/result/original/launcher/journal/acceptance byte strings
are canonical in-memory values parsed by the real helper. Their `MEMORY-...`
path labels are never opened or created. The uncreated runtime/checkout, lock,
Git/JDK and repository labels lie inside the prospective scratch namespace.
The real OS delegate has no creation, process, signal or deletion method; it
permits only read-only ancestor/top directory opens and the four fixed external
file reads/stat/fsync/closes. Its six preallocated source-FD slots retain raw
descriptors before subsequent bookkeeping. It delegates actual kernel outcomes,
not mocked reads, hashes, predicates or success returns.

The request has exact full bindings to its four immutable real fixture originals,
canonical argv/stop/env, and self-hash in the first journal event. Two visibly
synthetic role-correct acceptance objects bind those same bytes, obligations and
literal authors/purpose; these are **not actual reviews/admission signatures**.
Launcher review hashes, directory string, four ordered row objects and enclosing
integer time interval are coherent. Time values deliberately model epoch+1s,
and high synthetic PID/start/inode numbers are labeled test data, never observed
Linux identities. Scratch/ancestor pins and eight fixture file pins are actual;
all35 original runtime records and their descendants are fictional metadata.

The stop packet models clone/checkout/source-before, all35 allocations, database
intent before stop intent, exact internally bound wrapper/argv/environment,
budget/stop obligation, started/complete/exit0/errors[], source-after and terminal0.
The no-Gradle packet models a failure after source/allocations but before the
database intent, source-after observations, no database/stop intent/observation,
all stop flagsFalse, owned_settledTrue and terminal/outer exit1. Git/build argv
retain the structural contract and wrapper safety flags but are synthetic, not
the separately admitted actual runner's complete seven-class launch request.
No Git, Java, wrapper, command, log, XML or process operation is performed.

## Seventeen individually named controls

| ID | Required actual component effect / oracle |
| --- | --- |
| L01_valid_stop | Whole run_contract normal7-tuple with successful-stop branch; pure35-to33 projection; verbatim consumer reads/captures all four actual files/pins/hashes durably. |
| L02_valid_no_gradle | Whole run_contract normal7-tuple with positive no-Gradle exclusions/terminal1; same pure projection and four actual durable files. |
| L03_legacy_string_missing_rows | Valid directory string but no evidence_files: exact **KeyError('evidence_files')**, because source directly indexes before its shape require; no consumer I/O. No invented Hold diagnostic. |
| L04_legacy_list_directory | Old row-list in evidence, with evidence_files present so another failure cannot mask it: exact external completed launcher missing/ambiguous Hold. |
| L05_rows_wrong_type | evidence_files string rather than list: exact bounded-set Hold. |
| L06_rows_missing | Three rows: exact bounded-set Hold. |
| L07_rows_extra | Five rows: exact bounded-set Hold. |
| L08_rows_reordered | Correct four rows in wrong order: exact bounded-set Hold. |
| L09_rows_duplicate | Duplicate first path replaces a canonical second path at count4: exact bounded-set Hold. |
| L10_rows_unknown | Unknown path is only JSON data; exact bounded-set Hold before any attempt to stat/open it. |
| L11_row_nondict | An integer row: exact bounded-set Hold, not accidental iteration/type-error credit. |
| L12_row_missing_key | One row lacks path: exact-key guard Hold before path projection. |
| L13_duplicate_json_key | A second top-level evidence key is inserted into actual bytes; whole function reaches real strict_json and exact duplicate JSON key Hold. |
| L14_escaped_duplicate_json_key | evidence versus literal JSON `\u0065vidence` spelling: decoded-key duplicate Hold from the real parser. |
| L15_durable_digest_mismatch | Whole contract returns; unchanged actual full pin and wrong digest only. Real first-file read/file+parent fsync completes; loop exact durable mismatch Hold, no capture or later read. |
| L16_durable_pin_mismatch | Whole contract returns; unchanged actual digest and wrong mtime_ns in the expected full pin only. Same real durable first-file mismatch boundary; no digest mismatch can mask it. |
| L17_row_extra_key | Four rows remain, but a row has an extra object key: exact-key-set Hold. Distinct from L07's extra row; not disguised as a sixteenth case. |

The13 schema negatives require zero consumer file opens/reads/fsyncs. L01/L02
each require4 opens/closes,8 real reads including4 EOFs,4 file fsyncs and4 parent
fsyncs, exact ordered paths and exact captured bytes/full pins. L15/L16 each
require1 open/close,2 reads including1 EOF,1 file fsync and1 parent fsync. Thus a
complete suite expects10 source file opens/closes,20 reads,10 EOFs and10+10
file/parent fsyncs, separately from five initial source directory opens. Actual
short-read/environment deviations cause HOLD rather than a manufactured match.

No old rejected helper is executed to produce the counterexample. The old
string/list impossibility remains independently source-proven historical
evidence; malformed legacy shapes here are fed to the new exact successor.

## Cleanup, cancellation, evidence and limits

There are four callable-owned ancestor FDs,16 retained fixture FDs, five selected
Directories ancestor/top FDs and at most one selected input FD: at most26
explicit callable-owned FDs simultaneously. A bounded `scandir` context can
temporarily use an internal iterator descriptor, so the external accounting
allowance is **32 callable-owned FDs**, excluding root's borrowed scratch FD,
coordination/evidence/source handles and other outer resources. This does not
inherit the old28 controls'24-FD/four-name scope. Registry slots exist before
allocation syscalls; any capture/write/close uncertainty is HOLD, no adoption.

Total polling budget is60s/200000 subject ticks; cleanup has a separate15s
between-operation deadline and does not consult the cancellation latch. Root
must independently bound the outer launch plus settlement; blocking I/O, host
loss, SIGKILL or real allocation exhaustion are not defeated by Python finally
or point-sampled deadlines. Actual signal delivery/pending-signal and forced
OOM/MemoryError behavior are **not exercised by these17 controls**.

Source directory settlement is attempted before fixture removal. Tracked source
FD leftovers receive one explicit original-owner fallback close, but are a HOLD
and prohibit fixture removal, not a repaired PASS. A failed close is never
retried; empty tracking alone cannot erase a close-uncertainty record.

Cleanup first verifies original scratch/ancestor pins, the exact member set and
**every** created original file's FD/read-FD/name/full-pin/content hash, before
the first unlink. Only its own fixed-name originals may be removed. After each
unlink it checks retained-original nlink0, unchanged stable fields, pathname
absence and completed parent fsync. `durably_removed_names` acknowledges that
whole transition, not merely syscall return; attempted names are separately
reported. The first ambiguity stops removals and retains the rest. Fixture and
ancestor descriptors still receive independent one-shot close attempts. Root
must then independently settle its borrowed original and remove only the proven
empty original top. Unknown/replaced/partial scratch is consumed HOLD, not a
recursive-cleanup/recovery/retry opportunity.

All individual case results are emitted durably before terminal counting. Case
PASS is explicitly **pending shared suite fixture settlement**; no complete
suite PASS exists until the suite finally succeeds and root separately verifies
outer coordination/exit/interruption/FD settlement and original-top removal.
Terminal integer counters are not the case array or substitute evidence. Root
must reconcile all17 names exactly once, body-start flags, exact outcomes/I/O,
captured fixture originals, packet hashes, cleanup and external exit. A missing
receipt, callback exception, failed terminal write, timeout or incomplete outer
settlement is HOLD even if an in-memory counter says17.

The normal emitted records are compact hashes/pins/counters, not whole1198-row
fixtures or application binaries. A proposed outer bound of16 KiB per record /
256 KiB aggregate is ample for the fixed dictionaries; its exact value, sink
originals, fsync/error handling and short retention need independent admission.
Reports and permanent test source must be preserved; remove only admitted owned
temporary fixture outputs, not shared caches/SDKs/toolchains/source/reports.

## Authoring/checkpoint record and unchanged limitations

Only the two new source/DESIGN files were written. Source read/hash/line-count
review was performed; there was no helper/harness import, AST evaluation,
syntax test, compile, test, build, packaging, scratch probe/allocation, worker,
daemon, signal, process scan, cleanup helper or wrapper invocation. Therefore
there is no lane-owned worker/daemon/temp/cache to stop/remove and no wrapper
--stop obligation. The supplied engineering-skills snapshots remain authoritative
for this workspace; no repository URL/tool substitute was invented and the
generated agent-skills mirror was not edited.

Author resource observation2026-09-09T04:00:51Z: workspace23,951,796 KiB free,
/tmp20,378,244 KiB free; MemAvailable41,319,664/65,855,360 KiB, no swap. This is
an authoring point sample, **not prospective execution-resource admission**.

Current denominators remain19/25 original confirmed qualified closures,
22/38 all confirmed after separate PVA-038,2/12 original suspicions resolved.
The handoff's22/37 is historical; this infrastructure control adds0 families,
0 qualified closures and0 application/hardware test cases. Eight PVD design
explanations and outstanding owner decisions remain separate.

PVU-007 STOP, PVU-011 NO RETRY, PVA-029's actual failure/no automatic retry,
G7/G8 CLOSED, Windows operational FAIL/cleanup HOLD/all14 unstarted, physical
iPhone/Windows-Hello gaps, synthetic-only storage and every protected-ref/tag,
dependency/version/application-identity/signing/store/publication/occupied mobile
1017001 boundary remain unchanged. No admission follows merely from publication
or a successful future component run. Root alone may freeze/publish the next
resumable checkpoint and independently admit any authorized continuation.
