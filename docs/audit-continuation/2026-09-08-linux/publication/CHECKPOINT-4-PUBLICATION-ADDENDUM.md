# Checkpoint 4 late qualification — read before execution

Root-authored additive notice, 2026-09-09 UTC. This qualifies the frozen
47-file C4 selection without rewriting it. It is **not** execution, cleanup,
publication-result or release acceptance. The C3 mandatory addendum also remains
binding. No additional application finding or qualified closure is recorded.

## Database outer registration: existing source acceptance is qualified

After the C4 selection froze, storage identified and verification independently
confirmed a sibling of the existing **LCL-OWN-001** registration defect in:

- `reviews/linux-runner/LAUNCH_DATABASE.py`, SHA-256
  `ee46cfd974de0c7241763b251c6258a5b8a058cdaa4146fa029573c21e3812c1`
  (37,477 bytes /715 physical LF).
- Its original `reviews/linux-runner/LAUNCH-PLAN.md`, SHA-256
  `c5bb383165e2933a3d39da67b532613ffa71007c70e9dc38037f41e363a87a29`.

Paths in this notice are relative to the Linux continuation directory unless
otherwise indicated. Read both exact independently authored reports:

| Report | SHA-256 | Bytes / physical LF |
| --- | --- | ---: |
| `reviews/verification/LINUX-DATABASE-OUTER-REGISTRATION-QUALIFICATION.md` | `4737f349ca9c44314685492f66b4961759d056691ecf5fd97454b61d81a74f77` | 8,805 /140 |
| `reviews/verification/LINUX-DATABASE-OUTER-REGISTRATION-QUALIFICATION.json` | `326ed288776d5bfcf127d63341aaf2ae28ae21a939146fe75a01544257d82537` | 5,145 /94 |

The reviewer expressly qualifies its earlier source acceptance and discloses
the missed edge. That earlier acceptance and its unaffected observations remain
historical evidence, **not proof of whole-helper viability or actual admission**.
This is an existing source defect, not a newly introduced C4 regression.

### Exact conditional claim, not an observed incident

The paired dictionary insertion at original line133 may publish the new numeric
FD in `fds` before the second `pins` insertion raises a recoverable allocation
failure. The local handler closes that FD at135 without unpublishing it. The
later final `d.close()` at704 reaches the retained entry and attempts to close
the same number again at180.

The finding assumes the first insertion/local close succeed and subsequent
ordinary bookkeeping can proceed. Discovery occurs during admission at546,
before lock552, evidence allocation572 and child launch600. The independent
review identified **no intervening descriptor allocator** on this ordinary
no-child unwind. The established source defect is a redundant second close;
`EBADF` is expected absent reuse, but was **not observed**. No reused FD,
unrelated-descriptor injury, deletion, lost application data, postlaunch failure
or successful execution is claimed. The original exception already makes the
attempt code1/HOLD; this variant establishes neither a new operational HOLD
nor an additional emitted directory-close diagnostic.

## Successor and outstanding verification remain separate

Root authorized storage to preserve exact before-images and make only the narrow
unpublish-before-local-close correction. At this notice's authorship the frozen
successor is **author-delivered, independently under review and unexecuted**:

- Source SHA-256
  `871aa7ebca0a2c7cf3e73923396a1e918d2186332a7f7c22988d3d8469754889`
  (37,642 bytes /718 physical LF).
- PLAN SHA-256
  `965439c1a4ce968aa8c96ec9a379d5b0fdd17e55c33f1cccb6d02ebe793226f2`
  (15,746 bytes /266 physical LF).

Those successor source/PLAN files and their ongoing reviews are **excluded from
C4**. C4 consequently retains the committed ee46/c5bb versions, not the later
working-copy correction. This notice does not preaccept the successor, and a
later source acceptance cannot supply an unexecuted regression result.

The four prepared registration cases concern the corrected closer and its
different closeout outer, not this database outer. All four are unexecuted.
They cannot silently cover this sibling. Any explicit four-to-six successor
must preserve the four original intents/history and receive its own independent
source review and fresh execution admission; no such execution has occurred.
The distinct 28 component and 17 full-contract preparations are also unexecuted.
Their task/case inventories are not application test results.

**Actual database admission remains HOLD.** The narrow successor, meaningful
source-bound normal/fault controls, viable prompt closeout and the remaining
fresh source/environment/coordination/cleanup/filled-instance approvals are
required before the immutable handoff105-method database selection can run.
The receipt-schema qualification is separate and cannot clear this defect.
Empty-E bootstrap01 is consumed, not a retry target or database-test pass.

## Publication, accounting and safety

The two qualification reports and this notice are explicit **post-freeze
additions**, separately bound by the final C4 reviewer. They do not alter the
selected47 tuples,18 application-source overlay tuples or source-coverage credit.
The final Git/index receipts must enumerate every staged addition; unlisted agent
work is preserved rather than bulk-staged. Frozen references are point/historical
bindings, not a claim that excluded live working files cannot later change.

Accounting stays **19/25 original qualified closures (76%),22/38 all confirmed
qualified closures (57.9%),2/12 original suspicions conclusively resolved**.
There are16 open confirmed families and10 unresolved original suspicions; the
eight documented PVD explanations do not stand in for owner decisions. Zero new
application cases or closures. These are separate denominators, not readiness.

PVU-007 STOP, PVU-011 NO RETRY, PVA-029's49/44/5 FAIL and no automatic retry,
G7/G8 CLOSED, Windows operational failure/cleanup HOLD, genuine hardware gaps,
synthetic-only storage, root-only execution and original stop obligations remain
binding. No archived helper is replayed/imported. Protected refs/tags/protections,
dependencies, identities, Store versions/builds, signing/publication and occupied
mobile1017001 remain untouched. No build/runtime/cache was created or removed
by this source/report work; there is no new wrapper-stop obligation from it.
