# Database outer registration — independent narrow source qualification

Reviewer: `/root/verification`; author/finder: `/root/storage`; 2026-09-09 UTC.
**CONFIRM_EXISTING_LCL-OWN-001_VARIANT_SOURCE_ONLY; ACTUAL DATABASE ADMISSION HOLD.**
This is an additive qualification of this reviewer's earlier acceptance, not a
new application finding, executed reproduction, correction or actual approval.

## Exact subjects and retained history

Paths are relative to `docs/audit-continuation/2026-09-08-linux/` unless shown.

| Subject | SHA-256 | Bytes / physical LF |
| --- | --- | ---: |
| `reviews/linux-runner/LAUNCH_DATABASE.py` | `ee46cfd974de0c7241763b251c6258a5b8a058cdaa4146fa029573c21e3812c1` | 37477 /715 |
| `reviews/linux-runner/LAUNCH-PLAN.md` | `c5bb383165e2933a3d39da67b532613ffa71007c70e9dc38037f41e363a87a29` | 11441 /200 |
| Preserved `reviews/verification/linux-database-outer-frozen-v1.py.txt` | `ee46cfd974de0c7241763b251c6258a5b8a058cdaa4146fa029573c21e3812c1` | 37477 /715 |

The earlier `reviews/verification/LINUX-DATABASE-OUTER-SOURCE-REVIEW.md`
(`6307e9ee01eda1ce735407375b6ff1516feb308257e9fe528d75473a99525f06`)
and JSON (`a7a79601c6d30312b167b20a4c91291f14d8a81e1f07665003e20df7cdedea13`)
remain unchanged. This reviewer missed the partial-publication edge in that
source/design acceptance. The bytes have not changed; this is **not** a newly
introduced regression. The earlier acceptance cannot clear actual database
admission while this qualification is unresolved. Its unaffected source
observations and explicit limitations remain historical evidence.

The complete715-line source and200-line PLAN were read in the focused review
(`2d131d`, `1565ef`, `3ef1ee`, exit0). Correct registration citations, guarded
finalization and unchanged historical hashes were reconfirmed by source/data
reads `0f9400`, `e4df9f`, `a018e2`, exit0. My preliminary message estimated
lines137/139 and186–191 incorrectly; the author challenged them. The actual
locations are133/135 and175–183, independently confirmed before sealing here.
No source program was imported, parsed as AST, compiled or executed.

## Conditional defect and surrounding guards

`Originals.__init__` creates two ordinary dictionaries at113. In `directory`:

1. The absolute/no-parent-traversal and original-parent recursion guards apply
   at123–125. For a new key,127 requires `discover=True` before any new open.
2. Lines128–129 acquire a no-follow, close-on-exec directory descriptor;131–132
   capture its pin and require directory type.
3. Line133 performs **sequential target assignments**:
   `self.fds[key], self.pins[key] = fd, original`.
4. If the first FD-map insertion succeeds, the second ordinary pin-map insertion
   raises a handled `MemoryError`, and the local close succeeds,134–136 rethrow
   with that now-closed numeric descriptor still published in `self.fds`.
   Successful parent entries remain present; the failed new pin is absent.
5. `Originals.close`175–183 later pops each FD entry in reverse insertion order
   and calls `os.close`180. It does not know this entry was already closed.

The bounded finding assumes a **single recoverable allocation failure**, a
successful first/local close and subsequently successful ordinary bookkeeping.
It does not assume system-wide exhaustion continues, that every MemoryError
unwind completes, or that OS/Python cleanup is infallible. No genuine memory
pressure or fault injection was performed. The conditional source path is
reachable with the real ordinary dictionaries; this is not dependent on making
a production mapping an arbitrary user-defined test object.

## Actual call graph and bounded unwind — no intervening allocator found

New discoveries occur in `admission`: input reads288–289, the resolved-Python
read344, and explicit directories348–349. `main` calls admission at546, before
opening the lock552, allocating its first evidence file572 or launching600.
Every `read` first discovers its parent151 and only then opens the regular file
155. Earlier completed reads close their own regular FD in172–173. A recursive
ancestor failure prevents descendants' opens rather than opening them during
unwind. Existing-directory verification and later reads use `discover=False`;
a missing registration then fails127 before opening, not after child launch.

If admission raises, the tuple assignment at546 does not complete: main's
`request`, `captures`, `bindings` remainNone. `state` already exists with
child=None, empty files/streams and no collected child exit (358–367); lock_fd
isNone and lock_held false (543–544). Exception616–622 records the original
failure using bookkeeping-only `state.fail`369–379; no emergency wait occurs.

In finally623–705, `close_pipes` returns immediately516–518; no held-lock unlock,
postrun source read630, resource sample645, writer close647–659/684–698 or lock
close699 occurs. Signal-mask/pending bookkeeping and summary construction do not
open descriptors. `d.close()`704 nevertheless processes the stale FD entry.
No intervening descriptor allocator is identified on this bounded unwind.

**Established:** a redundant second close of the numeric descriptor; absent
intervening reuse, Linux `EBADF` is the expected second-close result. This is a
source inference, not an observed syscall/error. **Not established:** reused
FD number, unrelated-descriptor injury, deletion, lost application data,
postlaunch occurrence, successful admission or false PASS.

The original MemoryError/no-child state already makes complete=false and
code1/HOLD665–669. `d.close()` returns error-type strings, but704–705 only tests
their truthiness and forces code1; it does **not** add them to `state.errors`.
Thus this variant establishes neither a new operational HOLD nor necessarily an
extra emitted directory-close diagnostic. The later stderr write708 uses the
existing descriptor2 after `d.close`; it is not a demonstrated intervening open.

## Counterexamples, author challenge and required correction

- With successful paired registration, the original descriptor is retained and
  closed once in normal finalization; this finding does not reject that path.
- Failure before or during the first FD-map insertion leaves no newly published
  FD entry. A failed initial/local close or repeated catastrophic allocation
  errors are different paths and are not settled by this bounded claim.
- No broader “double close implies FD reuse/damage” assertion is accepted.
  The author agreed the ordinary unwind establishes redundancy/expected EBADF,
  not reuse or a new product family, and corrected my preliminary citations.

A narrowly scoped successor should unpublish the incomplete new FD registration
**before** its local close, without erasing previously valid parent entries or
substituting current descriptors/pins. That correction needs independently
challenged exact source/PLAN bindings and meaningful, separately admitted
source-bound normal/fault regression before actual database admission. This
review neither implements that change nor preaccepts an unspecified successor.

The existing four registration controls bind closer8437 and **different NEW
closeout outer5047**, not database outeree46. Those four remain unchanged and
unexecuted here; their future outcome cannot silently cover this sibling. Any
extension/rebind is a separate explicit root assignment and admission. The
receipt-schema documentation question is separate and cannot clear this defect.

## Authority, counts and resource settlement

No REQUEST/ACCEPT/receipt was created; no E/runtime/coordination namespace was
probed, adopted or removed. No execution, cleanup, retry, process or CI authority
is supplied. All build source remains the handoff commit
`9bdf9559b7a801a6f2fa49be3d7836ea6ef4d0ed`, tree
`05014e9f635131d5db06701e4013b4b5a746465a`; neither a continuation checkpoint nor
105 source declarations is an executed database result.

This review created only its permanent MD/JSON. Source-data readers exited; no
build/cache/temp runtime, worker, daemon or emulator was created. Wrapper stop
is NOT_APPLICABLE_NO_WRAPPER_LAUNCH, not discharge of any prior stop obligation.
Root owns resource monitoring and the sole audit-local/CI slot. No unrelated
process/cache/toolchain/source/test/report was stopped or deleted.

Closure delta0;19/25 original confirmed,22/38 all confirmed (16 remain),2/12
original suspicions (10 remain), eight PVD explanations/owner decisions separate.
These are different denominators, not readiness scores. PVU-007 STOP, PVU-011
NO-RETRY, PVA-029 FAIL/no automatic retry, G7/G8 CLOSED, Windows failure/cleanup
HOLD, hardware gaps, mobile1017001 and all protected-ref/dependency/version/
identity/signing/store/publication restrictions remain intact.
