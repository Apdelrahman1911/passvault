# Closeout controls v3 — bounded selected-subject source acceptance

Reviewer `/root/android32`; original harness author `/root/build_config`,
three-constant rebind author `/root`. The newly selected registration correction
was authored by `/root/editor`, not this reviewer.

**ACCEPT_NARROW_V3_REBIND_SOURCE_ONLY. NOT EXECUTED. NOT ADMITTED.**
This is a new, exact successor review. It does not float the historical v2
acceptance, approve whole closer/outer viability, or satisfy actual F/C admission.

## Exact tuple and preservation

| Subject | SHA-256 | Bytes / LF |
| --- | --- | ---: |
| `../build-config/CLOSEOUT-CONTROLS.py` | `441686459fa0cf8ec690c72e0547e240f19fe2ec7cd1be8b26549b6cf892600a` | 40149 /838 |
| `../build-config/DESIGN.md` | `f288a5d2ac887ed033aa50f414fd1e746aa5c0ebd3f92438f41d60e74851d249` | 24839 /325 |
| `scripts/audit/linux_database_closeout.py` | `8437c693d5a1becb8ed06ed549a5e837b8e4820c83a047c841fbcc0289f4b517` | 59609 /1100 |
| `../linux-closeout/PLAN.md` | `0b21ebfee0134c2853cfa8f3d17f9fef1b9acf812329581a93c0790c69930c5d` | 34814 /561 |

The script path is repository-relative; the other paths are relative to this
review directory. All four current tuples were independently read and hashed.
The exact preserved `CLOSEOUT-CONTROLS.v2.py.txt` and `DESIGN.v2.md` match
`bdee7e285f45f78e56d99fc6ef122767de00400d952f1e68c1d47b5d9bf35a53`
and `1e23c7432b9ea297e96ab56e7bd4742e5019af5c0726ce6ad28a2bdc8de3c8bb`.
Historical v2 acceptance JSON `f564f4a4ee1063538562f17bd7f3b4ebd0f3eaf982edf79fffa0d3445aac57ae`
and MD `f192f6a9a7206ec6feeb9cbcac246ce2b01cb9f1596ffbc50ac6044bd730cb23`
remain unchanged. V1 rejection and its own original bytes remain separate.

## Independent delta and reachability challenge

1. Complete literal reconstruction of v3 harness from preserved v2, replacing
   **only** `SOURCE_SHA256`, `SOURCE_BYTES` and `SOURCE_LF`, matched every byte.
   Thus all 28 case bodies/order, callable interface, limits, source-selection
   machinery and fixture/descriptor cleanup are unchanged. This is a source-byte
   relation, not execution or an AST/compilation result.
2. The helper at published C3 commit `bb094f8a39ba43f1ce1f43f394cb662b03febe38`
   was read as Git data and matched `b16774ff6d0c14ece40dbe2c75684a2b07e7c90c9da4373ec0a6e12e87cc7bec`.
   Applying exactly the three inspected literal hunks in memory reconstructed
   all current 59609 bytes: the three-line `Directories.open` correction, the
   `run_contract` evidence-file shape check, and admission's `evidence_files`
   iteration. No file patch was applied by this reviewer.
3. Only the first helper hunk enters this harness's selected definitions. The
   latter two are outside `SELECTED` and outside nested `main.bootstrap_tick`.
   Other selected bodies and that callback are unchanged. The complete literal
   diffs, `Directories` class, source-gating/extraction/namespace/case loop,
   `SubjectOS`/subject settlement, and B10/B11 context were read as source.
   Prior exact v2 review supports unchanged parts; this is not a fresh whole
   1100-line closer or new outer semantic review.
4. The new selected branch removes `fds[key]` **before** its local close when
   registration failed and that entry still equals the newly acquired FD. The
   relevant contrary case is a successful first dictionary store followed by
   failure of the paired `pins[key]` store. Previously final `Directories.close`
   could attempt the same numeric FD again; the corrected registry no longer
   supplies that incomplete entry. Before first publication nothing is removed;
   normal registration never enters the exception branch. Existing parent/key
   entries and original-directory drift guards are not weakened.
5. If the local close raises, removal has already occurred. This prevents a
   blind second close from that registry; it does **not** prove the FD settled.
   Fatal allocation failure during handling can still prevent cleanup. A
   hypothetical pin-only remainder does not authorize resumed/adopted work:
   failed admission unwinds, and final close enumerates `fds`, not `pins`.
   No actual FD reuse, unrelated descriptor damage or successful runtime
   correction is established by this reasoning.
6. The harness supplies ordinary private dictionaries; its current cases do
   not inject the paired-registration store failure. B11's `pins[path]` change
   is the separate **rehash input** dictionary, not `Directories.pins`. The
   dedicated four registration controls therefore remain necessary and outside
   these 28. The separate 17 full-contract cases likewise do not acquire a pass
   or admission from this review. Neither set was imported or executed here.
7. The source gate still checks actual supplied helper type, length, LF count
   and SHA-256 before deferred extraction. Supplying the historical b167 source
   now fails that gate; an old launcher/request/review tuple cannot silently
   authorize the new subject. `run_contract`, `admission`, helper main/Guard/
   Forest, process/mount checks, actual lock/runtime/deletion paths and the old
   validation runner remain outside executed selection.

The narrow registration review `c4b7e9d03dc8e5160c0f6cf628cbd4ee97e7dcb27707d73059c61f17576c6370`
and intermediate evidence-contract review `1b84d746f7483970a5349a963b45d7b68c31e32a70d741f5ace08b4346c1fe5a`
were read/rehashed for their bounded provenance, not promoted to whole-source
approval. The historical b167 impossible `evidence` contract and its earlier
reviewer miss remain preserved. This source rebind does not retroactively make
C3's closer viable or release its database prebuild HOLD.

## DESIGN and unchanged control quality

The complete v2-to-v3 DESIGN diff correctly labels authorship, preserves the
two v2 originals/old acceptance, records the real selected-code delta and keeps
the four registration/17 full-contract scopes separate. It retains the root's
reported stale-context authoring failure and unchanged-hash recheck before a
corrected edit. Those historical tool events are root's report, not events this
reviewer re-executed. No authoring failure is converted into a control failure.

B10 still performs real same-content write/fsync/readback, preserves bytes/hash
and requires observed metadata drift; B11 changes actual bytes and pairs the
current pin with the old captured digest. Their different guards remain
source-discriminating, not mocked success or equivalent combined checks. There
are still **13 journal +15 bootstrap/rehash/HOLD cases =28 planned, zero run**.
The separate 17+4 controls are not appended to `CASES` or its namespace.

Planned real synthetic filesystem and small I/O effects remain distinct from synthetic
cancellation/expired-start state, injected zero/EIO and supplied seal-input faults.
No OS signal delivery, hard blocked-syscall deadline, crash/host-loss durability,
hardware, Gradle stop or application behavior is established. The untested
nonempty pending-signal branch, per-fixture external original-pin-census gap,
confirmed-only removal accounting and fatal/late-interruption limits remain.

## What root still must obtain

This permits only recording an exact bounded source disposition. The separately
reviewed launcher must bind this harness/DESIGN/subject/PLAN/**new review** tuple,
original Linux coordination and prospective one-shot request, exclusive scratch
birth/borrowed-FD authority, bounded durable evidence/cancellation, fresh resources
and independently observed close/removal/exit settlement. No pending or old
request is rebound here. The unchanged callable does not create or own the
outer scratch FD and cannot authorize its own final removal.

Meaningful component/full-contract/registration controls and all applicable
source/actual-instance/runtime/cleanup gates remain open. Root is the sole
local/CI executor. No whole-outer acceptance, actual F01–F07/C01–C07 approval,
scratch creation, source import, control run or helper invocation follows.

This review used bounded regular/no-follow stable eight-field reads, literal
byte comparison and read-only Git/diff observations. No AST parse, Python
syntax/bytecode compilation, project/helper import or control execution occurred.
The source `diff` exit1 meant text differences, not a failing runtime control.
Combined output truncation was followed by focused reads and complete literal
reconstruction; no whole-helper proof is inferred from a truncated display.
All foreground readers settled; read FDs closed. Only these compact permanent
review files were created, no scratch/cache/runtime/worker or wrapper-stop duty.

Resource point after reconciliation: worktree free 23470168KiB, `/tmp`
19949320KiB, available RAM 38070/64311MiB. These are not launch admission or a
continuous/global idle proof. Zero new product families, application cases,
executed controls or closures. Counts remain19/25,22/38,2/12 with eight PVD
explanations/owner choices separate. All STOP/NO-RETRY/CLOSED scopes, PVA-029
FAIL, Windows FAIL/cleanup HOLD, hardware and publication/1017001 fences remain.
