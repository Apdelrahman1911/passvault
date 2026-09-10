# PVU-003 — minimal native admission observation draft

/root/editor; 2026-09-10. **SOURCE/DESIGN ONLY. No confirmed defect, executable fixture, test run or admission.**
Excluded from the current tray/lifecycle/real-Room three-case freeze. No product/frozen test was changed.

## Continue the accepted narrowing, not another lock audit

Reuse storage/PVU003-PREPOP-CANCELLATION-NARROWING.md (c2196bb8f60165e3d10afb4bc2f547cabe9f9b80bf3805a7cb7d20c7da67c76d),
storage/PVU003-TAB-ADMISSION.md (987e8dd79c31cdc59352b1680817b50c4e9594eeb0fda79e85da779f770d6161) and
editor-independent/PVU003-TAB-ADMISSION-REVIEW.md (f0e223daba538d8f078e19261c57be0310a88dbeb43c6437acb60ac146163526),
all beneath this continuation's reviews/. Their conditional A/V ordering, rejected DAO/writer stalls,
Back-before-pop cancellation and Linux03 qualifications are preserved. This draft addresses only the earlier
missing input/admission boundary. No new mutex-cycle harness or opposing delete operation is proposed.

## What current production source can and cannot establish

1. BackupExportAction dispatches OnExportClick only through its enabled button. BackupViewModel:185–205
   validates/rejects an already active operation and claims isExporting synchronously before launching.
   createBackupFile does not enter VaultBackupService until withOwnedBackupOutput obtains a real output.
2. DesktopBackupFileStore:36–53 obtains a resource title, changes to Dispatchers.Swing, constructs an actual
   FileDialog(activeOwnerFrame(), title, SAVE), shows it and disposes it; only then creates the target-directory
   temporary on IO. The project does not opt this chooser out of normal FileDialog modality. Under Java's
   normal modal-file-dialog contract, clicking the blocked owning window while it is showing is not free
   Home-tab admission. Project source alone does not bind the actual Linux toolkit's event filtering,
   dispatcher/resource timing or default modality to an observed binary. Record these, do not assume them.
3. The genuine Home dock is always in the MAIN shell and carries Role.Tab/selected semantics. Its callback
   reaches NavigationBackCoordinator.canLeaveForForwardNavigation, then current-token selectTab. Host inactivity
   denies admission. Backup registers only a composed busy policy in SideEffect; unlike credential input,
   it supplies no synchronous currentPolicy reader. Thus native modality does not prove that a Home event
   cannot already have won before chooser display, or after dismissal before current policy admission.
   Neither resource suspension nor viewModelScope.launch proves such a useful scheduling interval exists.
4. **Owned-focus counterexample:** DesktopWindowProtection:80–86,336–342 does not schedule focus-loss locking
   when oppositeWindow belongs to the main frame's owned-window chain. The chooser selects an active owner.
   Therefore “the chooser necessarily focus-locks/cancels export” is not a source disproof. Actual owner/focus,
   shared host lifecycle/RESUMED changes and any session transition still require observation.
5. A selected Home tab alone is insufficient. The real detail entry runs loadCredential, resetting/loading
   even a retained ID. Loaded/non-busy predicates guard both the rendered delete icon and router; real confirm
   checks them again in deleteCredential. An old loaded mock, direct confirm callback or merely visible Home
   shell cannot stand in for this. Ordinary Back requests cancellation before pop; it is not the Tab path.

**Conclusion:** no universal source disproof and no demonstrated admission. At most two real boundaries remain:
pre-chooser Home admission or post-dismiss Home admission before busy-policy denial. The chooser's own modal
interval cannot be silently substituted for either. A finite missed native timing is INCONCLUSIVE.

## One finite observation worth considering, without manufacturing a schedule

Coordinate with android32's separate reviews/android32/navhost-draft/ source-only PVA-007 scaffold; do not
write a second full NavHost driver. Its proposed fresh JVM uses actual GlobalContext,
AppModule.getAllModules(desktopModule), real private Room/bootstrap/session/settings/file adapters and
PassVaultApp inside Window/runDesktopApplicationLoop. That deliberately omits PassVaultDesktopWindow.
For THIS observation's shipped outer-focus guards, a separately reviewed variant must use the actual
PassVaultDesktopWindow/coordinator around PassVaultApp, not claim a plain test Window covers those guards.
The graph builder and native-input/semantics reader can be shared; no second hidden Koin graph or production
visibility change is needed. This outer-window variant and all owners are not yet admitted.

Choose one predetermined **Export-click then Home-click** native pair, with real clock/recomposer and no
retry loop. Setup uses one synthetic real credential and, if needed for subsequent work, one small synthetic
attachment created through real repositories in fresh isolated Room/object storage. Setup is not the witness;
do not inject recovery flags, loaded ViewModel state, delayed queries, held writers or alternative mutexes.
Use actual UI to open/load that detail, navigate to the real Export screen and enter a distinct valid synthetic
backup passphrase. Resolve genuine rendered Export/Home targets before the pair. Use native input only:
no semantics performClick, direct events/repositories, remembered stale callbacks, paused frame clocks or
blocking observer. Native automation proves native dispatch reachability if observed, not physical-user timing.

A passive, bounded observer can use public AWT Window/FileDialog owner, showing/focus/modality/exclusion
properties, native input-delivery records, actual Home Role.Tab selected state and rendered detail/delete
semantics, plus read-only singleton BackupViewModel state/identity. Do not record passwords or clipboard data.
Take no artificial “hold before SideEffect” synchronization. Window enumeration stays inside the owned child;
missing native-delivery events are not by themselves proof of modal interception.

- If Home is not selected, modal input is intercepted, host is inactive or the chooser is cancelled, retain
  that actual counterobservation and end this attempt; do not silently switch to another timing or a Back path.
- If Home was genuinely selected before chooser display, observe whether the real detail actually finishes
  loading while the actual chooser awaits user selection; do not presume its nested event loop does this.
  Select the fresh synthetic output via the actual native chooser, not by setting FileDialog fields or replacing
  BackupFileStore. The exact toolkit-specific input/locator contract needs root's separate admission.
- After dismissal, only if export is still busy, main focus/host admission has returned and the real detail
  delete action is loaded/enabled, send one genuine delete-icon click. A real confirmation dialog coexisting
  with the still-active export is useful **delete-intent admission**. End before its confirm button: this phase
  must not claim an actual delete job, V acquisition, A wait, overlap cycle, deadlock or durable harm.
  Dismiss through the genuine UI. If export finished first, loading failed or the guard denied the click,
  record that outcome; it is not universal rejection of the conditional schedule.

This fixed pre-chooser candidate does not also test every post-dismiss schedule. Actual confirm/job survival,
A/V ownership and cancellation/settlement would remain separately gated work after useful admission evidence.
The currently private export Job and back coordinator have no admitted owner trace; busy state alone is not
proof of uncancelled job identity or mutex ownership. No reflective access or forced scheduling is proposed.

## Evidence, cleanup and exact limits

Prospective compact evidence: ordered non-secret native/window-state events; Home-selected, real-detail-loaded,
delete-enabled/dialog-visible and export-busy predicates; one small synthetic screenshot only if needed to
disambiguate the actual native surface. Record precisely which boundary was reached, not a generic PASS.
Any output/temporary/Room/object observations occur only after owned settlement; no real vault or backup is read.
Retain no large backup/application archives. This document declares **zero executable test methods**.

Root must first bind the actual draft/source tree, JDK17/toolkit, native chooser locator, resource/time bounds,
synthetic HOME/preferences/Xauthority/display/storage, one-job exclusion and independently reviewed outer
cleanup. Install handling before launch, preserve compact failure evidence, stop the original wrapper, verify
owned children/workers/native owners, then remove only allowlisted runtime/generated outputs. Neither an
unwired OnCancelOperation event nor public busy=false establishes settlement; normal owned window teardown
must use the real cancellation/cleanup route. Forced/interrupted cleanup remains root's admitted outer scope,
not authority supplied here. No command, session, fixture, probe, import or execution has been run by this draft.

## Byte bindings

Re-read source pins match the accepted notes for DesktopBackupFileStore, BackupFileCleanup, BackupViewModel,
NavigationBackCoordinator, PassVaultNavigationHost, SettingsRouteAdapters, VaultRouteAdapters,
CredentialViewModel, CredentialEventRouter and CredentialDetailScreen. Additional current source pins:

| Source (relative to workspace) | SHA-256 |
| --- | --- |
| feature/backup/src/commonMain/kotlin/com/passvault/feature/backup/ui/ExportScreen.kt | 50d615dffb9350998a8c5a04f4d742fb4503f773a9b8918b3df31496e7bb4c9f |
| feature/backup/src/commonMain/kotlin/com/passvault/feature/backup/ui/BackupContent.kt | 714f755ef74a7671b17adf9b7fe447510a0b520bed81291fc42c70227c90002b |
| feature/backup/src/commonMain/kotlin/com/passvault/feature/backup/ui/BackupExportSections.kt | b6f1a3afc82ec5873f50f4764530f04a801886075985c6303f455b09ef7cb9c2 |
| shared/src/commonMain/kotlin/com/passvault/shared/VaultTab.kt | cf67f5bcfd1c85719ff24a6096bafae8057ec6d9f1ff3ee47d56c464bed4f97a |
| feature/vault/src/commonMain/kotlin/com/passvault/feature/vault/ui/VaultScreen.kt | 3b7a574fe60fcc0c4468d6d5175205a6964a04db1965e7eff05a0d5ca24156b1 |
| core/security/src/desktopMain/kotlin/com/passvault/desktop/security/DesktopWindowProtection.kt | 92c473e21c262972983b111b61adc18610bc17fcea92c6cc2b7f65ea4d5ee128 |

Bounded source-data path misses (shortened PREPOP note, VaultTabShell.kt, standalone VaultActionDock.kt and
app-desktop WindowProtection.kt) were corrected to the concrete files above; one read command exited at its
missing WindowProtection path after earlier excerpts. These are not application/test failures or retry scopes.
Only this permanent draft note/directory was created: no product edit, runtime/cache/generated artifact,
persistent worker, Git/network or cleanup operation. PVU-007 STOP, PVU-011 NO RETRY, PVA-029 no automatic retry,
G7/G8 CLOSED, all consumed/HOLD scopes and PVD boundaries remain; PVU-003 stays UNRESOLVED.
