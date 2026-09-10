# PVU-003 Tab admission: independent source challenge

2026-09-10; reviewer `/root/editor_review`, author reviewed `/root/storage`.
**Accepted as conditional source narrowing; PVU-003 remains UNRESOLVED.**
No confirmed new defect, code correction, executed case, hang or closure follows.

## Bound scope and starting point

Started with accepted `reviews/storage/PVU003-PREPOP-CANCELLATION-NARROWING.md`
SHA256 `c2196bb8f60165e3d10afb4bc2f547cabe9f9b80bf3805a7cb7d20c7da67c76d`
(4341 B/67 LF), then reviewed `reviews/storage/PVU003-TAB-ADMISSION.md`
SHA256 `987e8dd79c31cdc59352b1680817b50c4e9594eeb0fda79e85da779f770d6161`
(8286 B/113 LF). Paths here are relative to
`docs/audit-continuation/2026-09-08-linux` unless a source path is given.
All twelve S01–S12 full-file hashes in the latter note independently matched;
only their relevant paths and surrounding guards were semantically reviewed.
The accepted Back cancellation discriminator and Linux03 evidence are reused,
not reopened. This is checkout-byte source evidence, not a Git identity or run.

## Reachability challenge: still not established

1. **Export click, native SAVE selection and Tab are distinct boundaries.**
   S12:195–205 claims busy synchronously, before launching the export and before
   obtaining an output. S02:19–21 and S12:685–693 require actual output delivery
   before calling the backup service. S01:36–53,96–118 uses Swing's modal AWT
   SAVE `FileDialog`, disposes it, then creates the destination temporary on IO.
   Thus a completed native Save selection does **not** show Tab beat busy policy.
   The reviewed project source does not establish a dispatch/yield interval in
   which genuine Tab input can beat both modality and policy publication.
   Neither source-level `suspend` nor a queued direct event supplies that proof.
2. Backup still has only the composed policy; the accepted production Tab gate
   reads it without calling `beforePop`. `RegisterBackDisposition`:153–172
   installs the registration in `SideEffect`; no synchronous Backup live-policy
   provider is supplied. This permits a conditional stale-policy hypothesis,
   not an observed native event. Host-resumed/current navigation-token admission
   and survival of the actual singleton export job must hold in the same lifetime.
   Ordinary Back retains its cancellation-before-pop countercontrol.
3. **Home visibility is not delete readiness.** S09:76–98 runs the real entry's
   `LaunchedEffect`; S08:111–155 resets and reloads even the same credential ID.
   S10:85–98 renders loading rather than detail, and178–195 requires loaded and
   non-busy state. Independently inspected `CredentialEventRouter`:178–187 checks
   those live predicates before showing confirmation; the real confirm callback
   reaches S08:275–284, which independently checks them again. A preloaded fake,
   direct confirmation event or screenshot of the Home shell does not suffice.
4. For the proposed V-only preflight queue, the real reload must finish before
   deletion can be confirmed in the relevant interval. A detail load waiting on
   V cannot be presumed to complete while export already holds V for Argon2.
   The chooser's nested event loop is not proof that Home composed/loaded in time.
   The actual chooser/Tab/load/confirmation order therefore remains the first gate.

## Opposing locks: real source support, not a witnessed cycle

- **Default-manager counterexample rejected.** AppModule:107–184 binds one
  `AttachmentRepositoryImpl` as both `AttachmentRepository` and
  `AttachmentLifecycleManager`, injects it into credential deletion and backup,
  and binds one `VaultRepositoryImpl` as `VaultSessionManager`. The default
  database-only lifecycle manager does not describe this production graph.
- S03:181–196,254–268 does real password reuse validation before V2 export.
  A distinct backup password must also pass existing-master-password length
  policy to enter S07:108–129. S07:490–508 holds operation mutex **V** across
  metadata, unwrap and key comparison; S11:285–305 dispatches crypto work to
  Default. No attachment mutex **A** is acquired by that preflight path.
  This is a genuine possible interval, not a guarantee of enough native input time.
- Deletion enters V then performs real `credentialDao.exists` (S06:259–268).
  Independently inspected DAO:101–102 declares a Room suspend `SELECT EXISTS`;
  it is not a fabricated repository suspension. Nonetheless a suspend signature
  does not establish an outstanding query or a useful scheduling interval.
- After preflight releases V, a queued delete can conditionally own V at that
  query while export acquires A and requests V (S04:58–61; S05:54–59). If the
  query then returns true, delete requests A while retaining V (S05:307–323).
  This is the claimed two-owner cycle **only if those actual boundaries occur**.
  It assumes neither a guaranteed mutex scheduler ordering nor a forced DAO wait.
- S05:67–70 rejects nesting under the same `StableAttachmentOperation` coroutine
  context. It does not serialize two independently admitted ViewModel operations.
  V additionally requires an unlocked session/valid lease and supports revocation
  and cancellation. Those guards are not proof of either an observed cycle or
  permanent deadlock; cancellation request is not measured settlement.
- Cold recovery supplies another conditional A-only interval (S05:336–351).
  S06:755–765 reads credential attachment relations directly through the DAO,
  so ordinary detail loading alone does not establish that recovery ran. Neither
  a fresh/cold flag nor a query delay may be injected and called native reachability.
- **Writer stalls do not create the missing ordering:** stream writes occur
  inside both A/V scopes; commit is after both, abort after unwinding (S04:58–101).
  If export already owns both, deletion merely waits. If deletion finishes first,
  or its existence check is false, there is no cycle. Warm recovery supplies no
  presumed cold gap. Rejected fake-lock/stale-finalizer examples remain rejected.

## Smallest worthwhile remaining real-boundary evidence

This is an evidence requirement, **not** a new fixture, execution recipe or
permission to reopen any stopped scope. Do not build a broad hang harness first.

1. A separately admitted, finite synthetic production UI observation must first
   retain native Export/chooser/Tab dispatch order, the actual navigation gate
   outcome, surviving uncancelled export owner, real Home reload completion and
   real loaded/non-busy delete-dialog confirmation. Record whether Tab precedes
   chooser display or follows native Save; do not silently exchange the order.
   Current-policy denial, modal interception, picker cancellation or load failure
   is counterevidence to that attempt, not proof that no admissible schedule exists.
2. Only after that admission is established is a same-lifetime real A/V ownership
   trace worthwhile: export's V-only preflight; delete's V request/acquisition;
   its actual exists query outstanding/true result; export's A ownership and V
   wait; delete's A wait while still owning V. Source-qualified operation identity
   and acquire/wait/release ordering are needed, not elapsed UI busy time or two
   launched jobs. The cited code supplies no demonstrated external owner trace.
   Any minimal observation-only instrumentation would need separate source and
   execution review: no fake mutex, blocking observer, gated DAO, held writer,
   manual coroutine resume or direct repository-call substitute.
3. Real cancellation/join and owned-worker settlement, with separate synthetic
   Room-row, attachment-object and output observations after proper disposal,
   remain necessary. A temporary wait cycle would not prove data loss, committed
   output or an unbreakable hang. No vault/clipboard/user backup data is admissible.

A native admission observation alone would be useful partial evidence, **not**
a complete PVU-003 result. If the necessary owner/wait boundaries cannot be
observed under a separately reviewed non-manufacturing method, keep that explicit
limitation. A finite timing miss is INCONCLUSIVE; do not iterate an unbounded race
search, replay old helpers or promote the conditional source schedule to a finding.

## Additional exact source bindings and receipts

All S01–S12 hashes remain exactly those in the bound author note. Reused
SettingsRouteAdapters, NavigationBackCoordinator, PassVaultNavigationHost and
AppModule hashes match the accepted starting note. Additional focused bindings:

| Source path | SHA256 |
| --- | --- |
| `shared/src/commonMain/kotlin/com/passvault/shared/di/AppModule.kt` | `9b486df509202e65311caf92895ba8db6a44e6eae351266c8daa952bc553af25` |
| `core/database/src/commonMain/kotlin/com/passvault/core/database/dao/CredentialDao.kt` | `21fd55584a1794bd5eace8e98fa3d3dcc96590884c4cf188ccfcda1b645120b1` |
| `feature/credential/src/commonMain/kotlin/com/passvault/feature/credential/presentation/CredentialEventRouter.kt` | `b1f18f9e32beff113d0c6d26dc16fc3b9e24460ef6039a3dc144554815431f5a` |

Read receipts `38594a`, `59e136`, `890314`, `47ce6d`, `bb21ee`, `1be8be`
exited0. Receipt `a8f009` exited1 after a too-small textual match-count guard
also counted imports/categories; AppModule's complete binding excerpt and the
router hash had already printed, but router semantics and the later vault range
had not. The bounded corrected read `1be8be` supplied those excerpts. This was
an inert source-read limitation, not a test or an application failure/retry.
All final byte identities were rebound before this report's exclusive0600 write,
file/parent fsync and exact readback; nanosecond metadata comparisons used integers.

**Zero executed cases, new fixes or qualified closures; denominators unchanged.**
PVU-007 STOP, PVU-011 NO-RETRY, PVA-029 failure/no automatic retry, G7/G8 CLOSED,
all consumed/HOLD scopes, frozen GUI01/GUI02 and PVD boundaries remain intact.
No build, test, Git, network, native input, driver/import, held-runtime access,
control, cleanup or central-ledger change occurred. Only this permanent reviewer
note was created; no temporary/runtime artifacts or persistent workers were made.
