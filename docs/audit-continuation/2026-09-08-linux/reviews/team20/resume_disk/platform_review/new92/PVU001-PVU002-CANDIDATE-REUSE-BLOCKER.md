# PVU-001 / PVU-002 — compact candidate, reuse and blocker

Owner: `/root/platform_review`. **Both remain UNRESOLVED / VERIFICATION BLOCKED.**
Source-only continuation; no new confirmed defect, test afterimage, execution or closure.
`INPUTS.json` binds current source and the retained records. Root owns independent challenge,
canonical adoption and any later fixture/instance approval.

## Reuse, not new findings

The current ledger already adopts the C16 actual-Main feasibility review
(`6c00719f…`, author note `7c8ca960…`). Its Linux unavailable-provider route and native
Lock/curtain constraints are **reused**, not rediscovered as fresh witnesses. The rejected
unwired `OnCancelOperation` lead is excluded; no substitute cancel event was exercised.

PVU-001 still requires two live owners: a leased operation holding O and an uncancelled
exclusive transition holding T while awaiting O. Current `VaultRepositoryImpl`496–515,
555–670 preserves O/lease ownership, T→O→S transition order, early Locking/key detachment/
revocation, and the two-second timer only after T admission. `SettingsViewModel`504–510
cancels the password-change waiter before biometric work. Desktop enrollment cancellation
actively signals the native bridge and joins its structured worker (key store80–103);
source does not measure that worker's settlement. Linux's actual runtime selects unavailable
before loading a provider, and service54–88 rejects it before enrollment obtains a lease.
Thus the identified Linux biometric sequence remains unavailable; this is not global caller
unreachability. No new provider/UI admission or surviving T/O schedule was established.

Existing repository source already contains cooperative lease cancellation, biometric-
enrollment cancellation, prompt-cancel failure and synthetic hard-deadline controls
(`RepositorySecurityIntegrationTest`1525,1721,1782,1823,1871). Do not duplicate them with
another arbitrary noncancellable provider. These are source-presence observations, **not
newly verified passes**. The outcome-only PVA-012 row explicitly retains three PASS in-memory
Room insert/result-handoff controls from `detekt-db-source-regression8-02` and earlier
real-file postcommit/precommit/query-failure controls; reuse their limited ownership lessons,
not their counts as PVU-002 delete/input proof. Existing controller fakes and ordinary
row/object-delete tests likewise do not establish post-lock native admission.

## Additional narrow candidate: foreground timeout, not native Lock

The real credential route passes `viewModel::onEvent` (adapter75–99); the shipped attachment
confirmation invokes `OnAttachmentDeleteConfirm` (UI271–308). The router adds no session
check. Controller161–183 requires IDs/target plus loaded/nonbusy state and synchronously
claims busy. Repository231–249 checks the owner-scoped row/storage kind, commits Room delete,
then attempts managed-object cleanup without a session lease. Row commit and unlink differ.

**The additional branch isolated here is foreground inactivity:** host630–647 invokes
`AutoLockTimer` and `vaultRepository.lockWithBoundedRetry(AutoLock)` directly. Unlike
menu/tray/native lifecycle Lock, this branch does not first call
`DesktopWindowProtection.lock()` to curtain/iconify the frame. The window's session-title
effect329–344 does not add that call. This does not negate the native-Lock controls in C16.
The inspected foreground lock path also does not close Room; terminal shutdown's separate
lock→database-close path89–99 must not be substituted for it.

Crucially, host155–156 collects session state into Compose, then its recomposition-keyed
`LaunchedEffect`367–425 clears owners before clipboard/root replacement. Clearing is
synchronous **inside that effect**, not atomic with repository lock publication.
`CredentialViewModel.clearForLock`314–336 cancels work and removes loaded/ID/target state;
a subsequent confirmation cannot supply the missing authority. The remaining candidate is
only an actually newly dispatched native confirmation **after authoritative lock and before
that real owner clear**, followed by a real Room deletion. No such event/commit order was
observed here. The existence of an asynchronous observation boundary is not itself its
production timing witness.

## Minimal existing-fixture design only — not an afterimage or admission

A separately challenged extension could add **one foreground-timeout role variant** to the
existing serial seed/Main/verify fixture; do not widen an already admitted GUI case:

- Seed one synthetic managed attachment through the real attachment repository after the
  existing credential seed; retain its exact ID/object binding and initial row/object state.
  In actual Main use the existing Settings UI to choose its real minimum inactivity timeout,
  load that credential and open the real attachment confirmation. No timer/clock replacement.
- Use only passive existing boundaries: actual repository `StateFlow` sampling; a bounded
  native-event listener on the proven dialog/target; and no-op, non-suspending security-registry
  markers registered before entry creation and after loading to bracket coordinator clearing.
  Markers record to bounded memory only, never wait, perform IO, clear owners or alter state.
- Issue the one native press/release only after observing authoritative Locking/Locked; no
  prequeued Delete, held frame, delayed clear, direct VM/repository delete or new production
  seam. Target identity/geometry and full event freshness still need independent source review.
  Passive real-DAO row observations can establish an upper bound on commit; only a real absent
  row observed **before the first clear marker** supports commit-before-owner-clear. A later
  read, cancelled return or cleanup attempt must not be assigned an invented commit time.
- Let actual Main own shutdown. The existing fresh verify role reopens Room and reports row
  and bound managed-object outcomes separately. If clear wins, input is rejected, or ordering
  cannot be bound, report only that scoped countercontrol/inconclusive observation—never a
  conclusive PVU pass or universal impossibility.

The current fixture still seeds `attachments=emptyList()` (1353) and has no such receipt chain.
The design above names the smallest plausible extension without changing production; it is
not yet a reviewed implementation. **No Kotlin afterimage is authored:** a small fake/direct-
call regression would not close the missing obligation, while the real native/Room ordering
needs the independent challenge root requested. No generic test matrix or product patch.

## Boundaries

Only bounded source/retained JSON/text reads and this sealed note/index. No build/test/helper
execution/import, resource/runtime/process probes, Git/network, deletion, canonical edit,
worker or stop duty. Preserve PVU007 STOP, PVU011 NO-RETRY/no inquiry, G7/G8 CLOSED,
PVA02949/44PASS/5FAIL/no automatic retry, native/PVU008, all HOLD/UNKNOWN/PVD,15EOL+2raw and
protected refs/tags/build1017001/dependencies/versions/identities/signing/Store.
Zero new cases, findings, fixes, conclusive suspicions or closures. Available for the separate
independent consolidated current-ledger/brief review.
