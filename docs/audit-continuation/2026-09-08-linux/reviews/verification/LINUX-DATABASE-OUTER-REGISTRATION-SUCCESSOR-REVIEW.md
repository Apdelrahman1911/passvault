# Database outer registration successor — independent bounded source review

Reviewer: `/root/verification`; implementation/PLAN author: `/root/storage`;
2026-09-09 UTC. **ACCEPT_NARROW_SUCCESSOR_SOURCE_ONLY_PENDING_REGRESSION_AND_INSTANCE.**
The three-line rollback correction is accepted at source level. Actual database
admission remains HOLD pending meaningful independent regression and all actual
instance/coordination/cleanup gates. No execution or closure is claimed.

## Exact subjects, before-images and retained miss

Paths below are relative to `docs/audit-continuation/2026-09-08-linux/reviews/`.

| Subject | SHA-256 | Bytes / physical LF |
| --- | --- | ---: |
| Successor `linux-runner/LAUNCH_DATABASE.py` | `871aa7ebca0a2c7cf3e73923396a1e918d2186332a7f7c22988d3d8469754889` | 37642 /718 |
| Successor `linux-runner/LAUNCH-PLAN.md` | `965439c1a4ce968aa8c96ec9a379d5b0fdd17e55c33f1cccb6d02ebe793226f2` | 15746 /266 |
| Author `storage/LINUX-DATABASE-OUTER-REGISTRATION-SUCCESSOR.md` | `5de601aa2996aac5c2f600d0d508b76ce4ad0e6af0eb11334a979519a5f67c68` | 6506 /112 |
| Inert before-image `storage/linux-database-outer-registration-rejected-ee46.py.txt` | `ee46cfd974de0c7241763b251c6258a5b8a058cdaa4146fa029573c21e3812c1` | 37477 /715 |
| Inert before-image `storage/linux-database-outer-registration-rejected-c5bb-LAUNCH-PLAN.md` | `c5bb383165e2933a3d39da67b532613ffa71007c70e9dc38037f41e363a87a29` | 11441 /200 |

`ac20b5` independently checked every source/PLAN before/current hash and size/LF,
compared the **entire files**, and byte-compared the author's ee46 before-image
to this reviewer's earlier `linux-database-outer-frozen-v1.py.txt`: cmp0/equal.
Each diff returned1 because the expected documented difference exists; the
source/data tool as a whole exited0. There was no failed test or helper run.

The full source delta is exactly one hunk adding three lines at135–137. Every
other source byte is unchanged. The PLAN delta is exactly66 appended lines;
`74b9b4` separately hashed its original200-line prefix to c5bb. Its complete
appendix and complete112-line author report were read; the exact changed source,
registration readers, discovery sites and guarded finalization were read again
in `65ddcc`. Current871/965 were unchanged on final read74b9b4, exit0.

This verifies before-image **bytes**, not a fresh independent observation of
the author's earlier exclusive-allocation/fsync operations7878e7 or original
inode/provenance. That remains author-reported history, not execution evidence
replayed by this review. The preserved files were read only as inert text.

The original independent defect qualification is preserved unchanged:
`verification/LINUX-DATABASE-OUTER-REGISTRATION-QUALIFICATION.md`
SHA `4737f349ca9c44314685492f66b4961759d056691ecf5fd97454b61d81a74f77`,
JSON `326ed288776d5bfcf127d63341aaf2ae28ae21a939146fe75a01544257d82537`.
Its explicit acknowledgement of this reviewer's earlier ee46 source-acceptance
miss remains. The earlier6307e9ee/a7a79601 acceptance and corrected citations
were not overwritten. This successor fixes an already existing grouped
LCL-OWN-001 infrastructure variant, not a new application family or a defect
introduced after that acceptance.

## Exact correction and two ownership outcomes

The unchanged ordinary dictionaries are constructed at113; `directory`'s
absolute/path/original-parent guards123–125 and **new-key-only** branch126–127
still apply before the no-follow directory open128–129. Pin/type guards131–132
and sequential paired publication133 are unchanged. The only addition is:

```text
135  # Unpublish this new key before the possibly completed local close.
136  self.fds.pop(key, None)
137  self.pins.pop(key, None)
```

The existing single local close is now138; the bare rethrow is139. The two pops
precede that close. They do not open, adopt or replace any descriptor, call
`close` themselves, mutate valid parent keys, or clear either complete mapping.
Production keys are ordinary strings and mappings ordinary dictionaries; signal
handlers543 only latch CANCEL, not directory registrations. This is a bounded
single-process source argument, not hostile in-process mutation immunity.

### Normal registration

When both stores133 succeed, the exception handler is bypassed. The original
FD/pin remain registered and undergo the unchanged FD/path verification140–144.
`Originals.close`178–186 later pops each retained FD entry181 before its one
close183. The successor neither closes the successful FD early nor erases its
parent's authority. **Normal close removes FD entries but retains pin metadata**;
there is no contract that both dictionaries become empty. Future regression
must not invent an all-pins-cleared postcondition.

### Handled second-pin-store failure

Assume the first FD-map insertion succeeds, the second pin-map insertion raises
one recoverable `MemoryError`, and ordinary rollback/local close/bookkeeping
complete.136 removes the just-published new FD entry;137 removes any incomplete
new pin entry (a no-op if absent);138 closes the acquired local FD once;139
reraises. Final enumeration180–183 cannot see or reclose this failed key.
Previously valid original ancestor FD/pin entries remain available for their
own normal finalization. This corrects the precise stale-registration edge.

The author independently counterchallenged and agreed these two outcomes and
the retained-pin distinction. No injection, actual MemoryError, directory open,
FD inspection or syscall close was performed in this review.

## Counterexamples, reachability and cleanup limits

- Failure while capturing the new pin, checking type, or before/during the first
  store has no new published FD key. The ordinary pops are no-ops for that key;
  the existing local-close path remains. This does not erase a valid parent.
- Failure **after completed registration**, in verification140–144, is outside
  the exception handler. The entry remains for one final registry close; the
  patch does not turn that guarded failure into an unregistered-FD leak.
- Existing-key reuse does not enter the allocation/rollback branch. A missing
  original with `discover=False` fails127 before a new open. Main does not
  reopen this registry after final close; retained pins alone confer no such
  authority.
- A failure in a rollback operation, ambiguous/failed local close, repeated
  catastrophic allocation failure, blocked syscall, uncatchable interruption
  or host loss is not proven settled by this correction. Unpublication before
  the possibly completed close deliberately avoids retrying its numeric FD;
  it is not permission to retry a failed close or claim global cleanup.

Discovery remains admission-only292/347/352. The main admission call549 precedes
lock open555, first evidence allocation575 and child launch603. A failing
parent discovery154 still precedes the file open158; earlier completed reads
close176. The unchanged single-fault unwind has no child, evidence writers or
lock; `close_pipes`519–521 and finalization633/648/687/702 skip their guarded
work before `d.close()`707. No intervening FD allocator was established for the
old redundant-close path; this review does not manufacture reuse or injury.

The original admission failure already makes completed=false/code1 at668–672.
Directory-close errors at707–708 only force code1, not a new entry in
`state.errors`. The fix therefore does not establish recovery from a former
false PASS, a newly introduced HOLD, additional error diagnostics or lost
application data. The exact before/failure/after proof remains source-only.

## PLAN authority ambiguity independently challenged and clarified

The frozen PLAN says root “commissioned a distinct registration02 six-case
proposal”; the author report uses similar wording. This reviewer challenged
that against root's direct instruction that actual four-to-six implementation
would be authorized separately **after** this source review.

Root explicitly clarified: earlier wording is **prospective scope/proposal
intent only, NOT implementation or execution authorization**. No six-case
changes or actual instance were admitted. The current implementation assignment
awaited this review; root will separately authorize it. Root accepted this
explicit qualification without rewriting frozen965. The author agreed the
proposal interpretation and left871/965/5de601 unchanged. This record preserves
the ambiguity and clarification rather than silently expanding authority.

The appendix otherwise accurately preserves the before-images, original miss,
bounded source proof, unchanged source contracts and pending regression/actual
gates. Its ordinary text is not a REQUEST, ACCEPT, complete source-freeze or
observed sole-slot/namespace fact. The historical cdc713 receipt-schema memo and
its0c6ee9/633894 review remain unchanged ee46-bound schema evidence; this review
does not rewrite that memo or claim it is a new whole-successor approval.

## Required next evidence and unchanged boundaries

Meaningful exact-source normal/fault regression, independently reviewed test
source, a separate launcher, prospective original-instance admission and actual
result/settlement adjudication remain required. Existing four controls0a012823/
27cb2e2d target closer8437 and **NEW closeout outer5047**, not871. This task did
not extend, reinterpret, execute or count them, nor the separate17/28. A future
six-case proposal must explicitly bind its own subjects and preserve all old
control evidence/failures. This source acceptance is not a component-test PASS.

Actual database F01–F07, original input/approval/toolchain/directory seals,
coordinated producer/source freeze, root's sole local/CI slot, fresh resources
and external cleanup envelope remain separate. Actual original run results,
wrapper stop/owned settlement and postrun C01–C07 cannot be filled prospectively.
The application build source remains handoff commit
`9bdf9559b7a801a6f2fa49be3d7836ea6ef4d0ed`, tree
`05014e9f635131d5db06701e4013b4b5a746465a`, not a continuation build. Source105
method declarations are not105 executed cases. Whole-repository cleanliness,
current E state or unrelated source mutations were not probed by this lane.

Only this permanent review MD/JSON was created. Data-reader commands exited;
no import/AST/syntax probe/helper/test/build, runtime namespace, cache/temp root,
worker/daemon/emulator, resource/process probe, signal or cleanup operation.
Wrapper stop is NOT_APPLICABLE_NO_WRAPPER_LAUNCH; root retains execution/resource
monitoring and all prior cleanup obligations. Nothing unrelated or permanent
was stopped or deleted.

Zero executed cases or closures.19/25 original confirmed;22/38 all confirmed
(16 remain);2/12 original suspicions (10 remain); eight PVD explanations and
owner choices separate. These are not readiness scores. PVU-007 STOP; PVU-011
NO-RETRY; PVA-029 FAIL/no automatic retry; G7/G8 CLOSED; Windows failure/cleanup
HOLD; hardware gaps; mobile1017001; protected-ref/dependency/version/identity/
signing/store/publication fences all remain unchanged.
