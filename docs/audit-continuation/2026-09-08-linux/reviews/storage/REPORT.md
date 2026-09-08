# PVU-001 / PVU-002 / PVU-003 — bounded Linux source continuation

Author: `/root/storage`. Source checkpoint:
`9bdf9559b7a801a6f2fa49be3d7836ea6ef4d0ed`, tree
`05014e9f635131d5db06701e4013b4b5a746465a`.

**All three remain UNRESOLVED / VERIFICATION BLOCKED. No production patch,
new confirmed finding, executed regression, conclusive PVU outcome or closure
credit is claimed.** This continues the recorded investigations; it is not a
replacement audit of storage or a runtime admission.

`SOURCE_BINDINGS.json` records complete-file hashes and inclusive physical-LF
range hashes used below. References bind the initial displayed checkpoint source,
not an unstated moving working tree. Twenty files still matched that checkpoint
at final binding; the editor's concurrently authored `NavigationBackCoordinator`
and `VaultRouteAdapters` changes were detected and are explicitly separate.
Hash equality is not semantic verification.
The frozen handoff's findings, grouped variants, rejected witnesses, historical
passes/failures and coverage qualifications remain authoritative within their
recorded scopes. No central ledger was edited by this author.

## PVU-001 — transition admission can precede lease settlement timing

Current source sustains the recorded component observation:

- `VaultRepositoryImpl` (`S01`) has transition, session and operation mutexes.
  `withUnlockedSession` takes the operation mutex, registers a leased copy and
  owns it through a child coroutine (490–508). `withExclusiveSessionTransition`
  takes transition → operation → session (659–664).
- `lock` publishes/request-locks, revokes/cancels leased operations, then waits
  for the transition mutex (361–377). The two-second settlement timeout starts
  only inside `completeLock` (617–648). A noncooperative operation owner plus a
  transition holder waiting for that operation can therefore defer that timer.
- This does **not** delay all lock defenses: `requestLock` publishes `Locking`,
  detaches and attempts to wipe the repository-owned key, and marks leases revoked
  before that transition wait (583–607). Lease cleanup needs the session mutex,
  not the transition mutex (560–578).

The production-caller countercontrols still matter. Settings biometric enable
uses `DefaultBiometricUnlockService.enable` (`S02`:54–88), which leases the key
around enrollment. Password change has its own synchronous busy claim but does
not reject solely because enrollment is busy (`S03`:426–488). Nevertheless,
Settings lock cleanup cancels the password-change waiter **before** the biometric
job (`S03`:504–524). The navigation security observer calls that cleanup before
root replacement (`S11`:367–424). The inherited platform-cancellation observations
are preserved as historical evidence, not independently re-executed on Linux.

The checkpoint's `VaultRepositoryImpl` diff from original main is the PVA-033
biometric-attempt freshness correction; it does not reorder the mutexes or move
the lease timer. PVA-033's pending regression gate is not evidence for PVU-001.

**Still missing:** a genuinely admitted production UI/provider sequence that
keeps the queued transition and lease live despite those cleanup controls,
followed by measured settlement and leased-array observations. An arbitrary
noncancellable fake or an assumed OS stall does not establish that sequence.
No source-only mutex rewrite is proposed on an unresolved user-impact claim.

## PVU-002 — internal attachment deletion does not obtain session authority

`AttachmentRepositoryImpl.delete` (`S04`:231–249) takes the attachment mutex,
performs conservative recovery, looks up the attachment with both attachment and
credential IDs, checks storage kind, commits the Room deletion, and only then
attempts owned-object deletion. Unlike import/rename/copy/verify, this method
does not acquire an unlocked-session lease. That is still an internal component
contract observation, not a demonstrated post-lock UI deletion.

Actual input and cleanup are narrower than a direct repository call:

- `CredentialAttachmentController` (`S06`:161–183,258–268) requires a non-null
  credential ID and delete target, a loaded credential, and no shared busy state.
  It claims attachment-busy synchronously before launching the operation.
- `CredentialViewModel.clearForLock` cancels tracked operations and resets the
  whole credential state; the ID, loaded flag and delete target are consequently
  unavailable to a subsequently delivered confirmation (`S07`:314–336).
- Entry owners are registered through the actual Koin ViewModel factory and
  synchronously cleared by `VaultUiSecurityCoordinator` (`S10`:188–234,
  `S09`:53–82). The navigation observer performs owner clearing before clipboard
  cleanup/root replacement (`S11`:367–424).
- The event router does not add hidden session-token authorization (`S08`:24–45).
  That absence does not cancel the controller's guards. The tracked production
  delete caller found by the bounded symbol search is the controller; no external
  destructive entry point was introduced by this continuation.

**Still missing:** real newly post-lock input admission before synchronous owner
clearing, and real DAO persistence/teardown ordering. An action admitted before
lock, a direct locked repository call, or a fake recording a delete invocation
does not prove this missing claim. Room-row removal and filesystem cleanup must
also be scored separately; a cleanup attempt is not proof of successful unlink.

## PVU-003 — inverse order remains; harmful production overlap not established

The same DI singleton identities remain wired (`S10`:107–182):

- Attachment operations and V2 export/restore hold attachment → vault operation
  (`S04`:54–84,197–305; `S17`:47–101,137–175).
- Credential deletion holds vault operation → attachment and then performs the
  credential/attachment-row commit (`S05`:259–270; `S04`:307–328).
- The actual discovered production credential-delete caller remains
  `CredentialViewModel.deleteCredential` (`S07`:275–311). It and the attachment
  controller reject shared busy state synchronously.
- Credential/backup Back and forward policy rejects active operations
  (`S12`:39–83, `S13`:215–238, `S14`:147–177, `S15`:32–34). iOS explicitly declares
  one scene (`S21`:35–42). This policy source is not proof of every platform input
  timing or actual window behavior.

### Additional challenged overlap lead — rejected as a shipped input witness

A possible stale-finalizer sequence was inspected instead of assuming that the
normal navigation guard admits overlap:

1. `cancelOperation` cancels export A and clears busy flags immediately.
2. Export A could still own its ViewModel mutex during noncancellable sink cleanup.
   Desktop's real sink abort dispatches to IO, so cleanup is not inherently one
   synchronous main-thread instruction (`S19`:191–197).
3. A hypothetical export B admitted during that cleanup sets `isExporting=true`
   before waiting for the same ViewModel mutex. A's unconditional `finally`
   could clear B's flag before B starts (`S16`:195–252,421–440).

**Disproof of the proposed ordinary cancel-button witness:** a tracked Kotlin
symbol search for `OnCancelOperation` found only the handler at line147 and event
declaration at line536 of `BackupViewModel`. There is no shipped UI producer in
that search. The actual export UI contains the create action and progress, not
the assumed operation-cancel button (`S20`:54–118). Therefore invoking that event
directly in a test would not demonstrate the hypothesized user path.

Lock cleanup can also cancel work, but a lock → unlock → new export while old
cleanup remains pending needs its own admitted session/provider timing proof.
It is not established here. Likewise, an old cancelled operation waiting for
the vault mutex is subject to coroutine cancellation; it cannot be presumed to
stay in the inverse-order cycle merely because an asynchronous cleanup exists.

The editor investigator separately reported no admitted same-frame harmful
cycle. A pre-recomposition `BackRegistration` snapshot is not treated as proof
of the missing cycle. During this report's final binding, that agent was authoring
dynamic credential-policy changes in `NavigationBackCoordinator` and
`VaultRouteAdapters`. This report binds their earlier displayed checkpoint bytes;
the new patch requires its own independent review and does not silently inherit
these line references. No additional PVA family is assigned to the storage lead.

**Still missing:** independently admitted *uncancelled* production callers forming
the inverse-order wait, plus lock/cancel/join controls and actual durable-state
observations. An arbitrary two-coroutine repository fixture would establish only
the already-recorded component possibility, not a user deadlock or data loss.

## Precise next regression proposals — not authored/executed tests

These are bounded fixture requirements, not an admission, command or assertion
that such tests have run. No new test source was added in this pass.

| Scope | Meaningful boundary and positive/countercontrols | Unsettled claim |
| --- | --- | --- |
| PVU-001 | Production repository/Settings caller with synthetic Room metadata; independently traced actual supported provider cancellation; observe repository key, leased alias, transition and terminal lock separately; cooperative completion, cancelled queued password change, and fresh operation controls. | Real caller/provider reachability and latency; Linux mocks cannot prove OS stalling. |
| PVU-002 | Production controller/ViewModel and repository over an isolated file-backed Room database plus app-private synthetic attachment object; show exact UI input/lock/owner-clear order; assert stored row and object before and after reopen; compare pre-lock admission, post-clear confirmation, wrong-owner ID, and Room delete failure. | Fresh post-lock UI admission; no direct repository/fake-only closure. |
| PVU-003 | Only after caller admission is independently established, combine production feature callers, actual singleton repositories and Room with observation-only scheduling hooks; assert A/V ownership, both waits and cancellation/lock release; inspect rows/objects after reopen. Same-entry busy, ordinary Back/forward, single-scene and cancelled-old-operation controls remain mandatory. | UI admission and a durable harmful cycle; no arbitrary-coroutine substitute. |

All fixtures must use synthetic secrets and isolated storage, preserve historical
password/backup compatibility, and avoid redesigning PVD-007's cross-system restore
boundary. A physical device is still necessary for physical biometric/security
claims. File-backed reopen is not power-loss/crash proof.

## Historical evidence-reader gap and retained failure

The current outcome projection was read for all three PVUs, including the
recorded independent dispositions and original evidence pointers. The requested
underlying independent report is:

`remediation-reports/20260905T222925Z/reviews/storage-pvu-independent-platform.json`

Indexed SHA-256:
`1834ada72a62c9e9e76ec7b916910c756c640b426967f1b85026bd26abadef09`,
8,846 bytes, `evidence-packs/evidence-010.tar.xz`.

After full inspection of the supplied reader, root explicitly authorized its
documented read-only `show` operation. This exact invocation failed:

```text
python3 -B docs/audit-handoff/evidence.py show remediation-reports/20260905T222925Z/reviews/storage-pvu-independent-platform.json
exit 2
REFUSED: file type/size/links
```

Subsequent bounded `stat` inspection found directory `st_dev=23` and regular file
`st_dev=24` for PACKAGE/index/pack, with one hard link and bounded file sizes. This
is consistent with the reader's same-device refusal on this virtual filesystem;
it is not an application defect or archive-corruption result. No retry, viewer
weakening, archive extraction, old helper import or fallback reader occurred in
this pass. The underlying report was **not** freshly opened; its exact historical
review remains a named gap until a separate data-only reader is admitted. This
does not invalidate the delivered projection or erase the old independent review.

## Resource, cleanup, authority and counting

- Work was bounded text/JSON/Git reads and small permanent review writes. Initial
  inspected worktree free space was 4,466 MiB, `/tmp` 3,276 MiB; a later sample was
  15,579 / 15,117 MiB with 34,720 MiB available RAM. The improvement came from
  external activity: this author deleted nothing. No resource observation admits
  a build by itself.
- No app, Gradle, native compiler, packaging job, test server, emulator or persistent
  worker was started. No build artifacts, caches or temporary archives were created.
  Gradle stop/build-output cleanup is **NOT_APPLICABLE**, not a successful discharge
  of historical obligations. The reader used `-B`; no bytecode cache was created.
- A few initial guessed source paths were absent and were corrected through actual
  tracked filenames. These inspection-path failures were not tests; no claimed
  source range rests on an unread missing file.
- The first source-binding write attempt failed before creating its output
  (`AssertionError`, exit1), correctly detecting the editor's concurrent source
  change. The report was qualified rather than overwriting those changes or
  rebasing old citations. The subsequent data-only capture binds the immutable
  checkpoint plus explicit observed working-tree differences for those two files.
- PVU-007 STOP, PVU-011 NO RETRY, PVA-029 FAIL/no automatic retry and G7/G8 CLOSED
  remain binding. No owner PVD decision, version, identity, dependency, protected
  branch, tag, candidate/build1017001 or publication operation was changed.
- Accounting delta: **0 closures, 0 new confirmed findings, 0 conclusive PVU
  outcomes, 0 application test cases, 0 runtime invocations**. Starting separate
  denominators remain **19/25 original, 22/37 total, 2/12 original suspicions**.
  This bounded source pass does not replace the historical LF coverage ledger.

Root may request independent challenge of this report and later exact historical
blob reconciliation. This report itself is not fresh independent review of a new
patch, executable test result or runtime admission.
