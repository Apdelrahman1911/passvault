# PVU-003 — minimal actual-Main adoption proposal

2026-09-10; author `/root/editor`; independent review requested from root.
**INERT DIFF ONLY. Not adopted, compiled, executed or execution-admitted.**
This continues the accepted chooser/Home/cancel fragment, not the lock audit or a new harness.
One proposed observation case; zero added module cases, executed cases, findings, fixes or closures.

## Exact deliverable and parent

- `PVU003-ACTUAL-MAIN-ADOPTION.patch.txt`: SHA256
  `1bec90498dfee2375b74bcab23053214755a9a504fe943352c538084d04de23a`, 21,002 bytes / 367 LF.
- Patch target is the existing
  `app-desktop/src/desktopTest/kotlin/com/passvault/desktop/CredentialMainNavHostRoomIntegrationTest.kt`.
  Exact parent `32c01afedd2db1195ea6eede8db09ec064b798f8f642f762218f56cb3f124dc5`, 40,561 bytes / 755 LF,
  was actually read/hash-bound when generating this diff. The earlier root-provided C14 parent was
  `7a0dd50d7caa6cf3cfad729f7393a0e1802fbf65b57d3cceae7c5c64ac94961f`, 40,423 bytes / 753 LF.
  Android32's separately owned editable-text-only delta
  `reviews/android32/navhost-draft/MAIN-NAVHOST-EDITABLE-TEXT-ONLY.patch` hashes
  `7f883bdbb31478d2b1fa9d5a8dfad7bae148c84c3ec5904fa57e21f87a8ff046`.
  This proposal neither authors that correction nor claims its runtime success/admission.
- Hypothetical patched source, computed as text in memory ONLY:
  `8a666a5df0c303d50ab57aa4ef51907f7c3a06ef69f6c7679519d5e992867f6c`, 53,492 bytes / 976 LF.
  It was NOT written to the module or retained as a second full harness. Net +221 LF includes the
  accepted fragment's 156 member LF and nine imports, leaving only finite scenario/receipt wiring.

## Smallest useful integration

A separate `Pvu003NativeChooserAdmissionIntegrationTest` class has exactly one proposed method:
`actual Main samples Home while the export chooser is modal then cancels`.
An absent exact `passvault.mainnav.pvu003Chooser=true` opt-in ignores it, never passes it. If opted in,
a missing synthetic-display property fails before the common display-assumption path. This flag is a
scope guard, not execution authority. Root must explicitly select this class with fresh disjoint empty
runtime/evidence roots, never the occupied integration02 roots or an unreviewed broad task filter.

Reuse the existing parent and its three SERIAL fresh JVM roles: real synthetic Room seed; actual Main;
real Room reopen/assertion. No second graph builder, launcher, terminal owner or native-input driver.
The distinct case enum is passed explicitly as the fifth child argument, with the opt-in also required
inside the chooser child. Seed/verify logic, private bus guards, synthetic HOME/XDG/preferences/cwd,
source AX correction and Main's real Koin/window/instance-lock/shutdown ownership remain unchanged.

The 76d1989 fragment's members are inserted verbatim apart from enclosing indentation. Only its comments
which describe it as an unintegrated fragment and its import block are handled as integration material.
The existing credential scenario body and its literal `EVENTS` map are byte-identical. A separate finite
case contract selects the new receipt; the original case accepts no new event or changed expectation.
Nothing is silently added to the current integration02 four-case contract or its allowed execution scope.

## Exact observable oracle, not a manufactured schedule

The fragment performs the same fixed native sequence: real unlock -> Settings/Data Management/Export ->
valid typed synthetic passphrase -> actual visible owned application-modal SAVE chooser -> one native
Home press/release -> twenty bounded samples -> one genuine Escape -> ten post-cancel samples.
There is no destination selection, output file, delete icon/confirmation, DAO/writer stall, fake policy,
manual callback, lock-cycle injection, alternative timing or input retry. Production busy/password guards,
Home's real forward-navigation/current-token gate and the native chooser boundary are not bypassed.

Normal fragment return already requires: exactly the captured/owned chooser, unselected enabled Home and
safe nonoverlapping geometry before input; chooser-showing/export-busy during samples; native dismissal,
null FileDialog.file, no export success, export-idle post-cancel samples and exact listener removal.
The adapter then records precisely one line of this bounded schema before the inherited input release
and real Main terminal path:

`PVU003_CHOOSER_HOME=[01];AFTER_CANCEL=[01];FRAME_DELIVERY=[0-9a-f]`

The two booleans are ORs of their respective samples, not continuous observation. Delivery bits retain
press/release callbacks while showing (1/2) versus hidden (4/8), not native timing or Compose callback proof.
The parent requires exactly that line plus `DRIVER_ASSERTIONS_COMPLETE` and `QUIT_KEY_CALLS_COMPLETE`
in that order, genuine child exit0, no inherited terminal-error diagnostics and then real Room verification.
No new receipt file or broad event logger is introduced; existing bound append/force/size checks are reused.

Interpret values, not just a JUnit return:
- HOME=1: Home selection was sampled while the captured chooser still reported showing and export busy.
  Useful modal-interval admission observation, not proof of a surviving private Job, host lifecycle state,
  V/A ownership, loaded Home detail, deletion, deadlock or harm.
- HOME=0, AFTER_CANCEL=1: only after-dismissal selection observed; not modal admission.
- Both zero: bounded no-transition counterobservation; not universal source disproof or a gate identity.
Any geometry/focus/AX/precondition/cleanup failure yields no valid completed observation. Both booleans
may be one. A returned observation case means this finite evidence/terminal/durable-tuple contract completed,
NOT that PVU-003 is resolved or a product defect was proved absent. No receipt value is forced to the
hypothesized outcome. Export busy/idle and native focus do not prove coroutine/lifecycle settlement.

## Source conclusion and retained investigations

The current focused six production files still match the accepted pins: DesktopBackupFileStore
`ce13726b1c0c6d401c1a8d6d5a47abb18b129984eb1d205fd24a021fe94f9fd5`; BackupFileCleanup
`829962a3eba0fcd198febed205b3b3a22a3aeddf74a4e40aedeae95e39695bb3`; BackupViewModel
`afd73521b4fc0d1b06af29bdc7d4940f4646507a66fcc4e55d801f2448060e63`; NavigationBackCoordinator
`7f0f546301a87c0bd758d121a118d8325b2fe366561655d1ee5c8e1b248126db`; PassVaultNavigationHost
`50ec002a8b7b9e2c9324980662baff166596dddb31cef4823b4ffdfd784c9006`; SettingsRouteAdapters
`6339edcfe9a09916a5485894e287c40697e85909158c0c348e51b486b8073c06`.

FileStore:36-53,96-111 and BackupFileCleanup:19-21 confirm that normal null selection returns before output
creation and backup-service entry. This is a deliberate precursor, not a lock-cycle experiment. Java's
application-modal chooser contract counters free owning-frame input while showing. Actual Linux peer
behavior has not been newly observed. Neither source proves a useful pre-display/post-dismiss native input
window nor disproves every such window. Current Backup busy policy is composed, ordinary Back requests
cancellation before pop, and real Home reload/delete predicates still matter. Therefore source does NOT
conclusively resolve original PVU-003; the modal-only observation cannot replace those other gates.

Reused/preserved predecessor identities, all under this continuation's `reviews/`:
- `storage/PVU003-PREPOP-CANCELLATION-NARROWING.md`: `c2196bb8f60165e3d10afb4bc2f547cabe9f9b80bf3805a7cb7d20c7da67c76d`.
- `storage/PVU003-TAB-ADMISSION.md`: `987e8dd79c31cdc59352b1680817b50c4e9594eeb0fda79e85da779f770d6161`.
- `editor-independent/PVU003-TAB-ADMISSION-REVIEW.md`: `f0e223daba538d8f078e19261c57be0310a88dbeb43c6437acb60ac146163526`.
- `editor/pvu003-draft/PVU003-CHOOSER-HOME-CANCEL.kt.txt`: `76d198977e09cfd38e664cd957f5c3aa159c76488843e4b4dad36667b175cbd3`.
- Its `PVU003-CHOOSER-HOME-CANCEL-SOURCE-NOTE.md`: `a9caf50f56f7b239f146cc6ae895a48485f7d7ff6ba5a7e51a9ea2dbb451305f`.
- `native-independent/pvu003-draft/PVU003-CHOOSER-HOME-CANCEL-INDEPENDENT.md`: `4d44120da698cf75d99805a008b17d11453ed98586848ada4505f970d8ef4f33`.

## Admission, cleanup and resource limits

Fresh independent integration review, explicit adoption and a new root-owned execution/cleanup plan are
still required. Do not delay or insert into the currently admitted correction/unstarted-Room batch.
This is meaningful remaining Linux SOFTWARE/native-toolkit evidence, not an external-hardware impossibility.
No blanket continuation admission, old helper replay or inherited process/lock authority is supplied.

The unchanged 150s driver deadline caps observation AND cleanup awaits; expiry can prevent Escape entirely.
The 5s EDT waits cannot settle already-started work. Focus/target loss, listener removal, input release,
native/EDT interruption or cancellation failure remain non-pass and outer settlement/HOLD duties.
There is no guaranteed cleanup, forced-dispose success, new OnCancelOperation call or competing Main close.
The original parent bounds remain 90s seed, 200s Main, 90s verify, with one 5s child-settlement attempt each
and one child at a time with `-Xmx512m` (heap only, NOT an RSS bound). Failed child force-containment
never produces passing terminal evidence.
A new outer admission must cover JDK17/toolkit/private display and bus, native chooser geometry/default
directory, HOME/XDG/preferences/cwd isolation, exact child/classpath identity, RAM/time limits, failed/native
settlement, compact XML/log/trace retention, wrapper stop and only allowlisted generated-output cleanup.
Null chosen file and idle UI are not evidence of all native/toolkit/filesystem workers having settled.

This authoring step used only bounded no-follow source/data reads, hashes, metadata guards excluding atime
and in-memory text transformations. One oversized combined-note output was truncated and its missing tail
was completed by a narrowed data read, not a runtime retry. Only this note and the inert diff/directory
were created; no module/source-helper import, AST/syntax/compile/build/test, Git, process/cache/SDK/device
probe, runtime/native operation, network, cache/temp output or persistent worker. Foreground data readers
settled; wrapper stop N/A. Root owns builds/Git/ledgers/cleanup. All STOP/NO-RETRY/CLOSED/consumed/HOLD scopes,
PVU-007, PVU-011, PVA-029 failure/no automatic retry, G7/G8 and PVD limits remain. The unrelated native refusal
was not revisited. **PVU-003 UNRESOLVED; every denominator and hardware qualification unchanged.**
