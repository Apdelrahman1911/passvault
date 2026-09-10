# PVU-003 native admission draft — independent challenge

2026-09-10; reviewer `/root/native_review`, author `/root/editor`.
**Accept conditional source/design usefulness only. Reachability remains unproved;
PVU-003 stays UNRESOLVED. No finding, fix, executable case or execution admission.**

## Starting point and preserved boundaries

Reviewed `reviews/editor/pvu003-draft/PVU003-NATIVE-ADMISSION-OBSERVATION.md`, SHA256
`7aef13ebf80d4312b081830f076ec154b2f72abf8da58c33f46ac5edfd910f81` (11123 B/125 LF).
Paths here are relative to this continuation directory unless a source path is given.
Reused and re-read the accepted storage PREPOP narrowing (`c2196bb8…c76d`), storage
TAB admission (`987e8dd7…6161`) and editor-independent TAB review (`f0e223da…3526`),
with their full hashes bound in the author note. Conditional A/V ordering, rejected
DAO/writer stalls, Back-before-pop cancellation and Linux03 qualifications stand.
This is the distinct permitted admission-design review, not a reformulation/retry
of the errored native task or an extension of frozen C13.

## Independent guard / counterexample review

- The genuine enabled Export button reaches `startExport`; busy is claimed before
  launch (BackupViewModel:185–205). Output acquisition precedes the backup service
  (BackupFileCleanup:19–21; BackupViewModel:685–693). Desktop uses the actual SAVE
  FileDialog on Swing and disposes it before IO output creation. There is no project
  modality opt-out. Resource suspension/launch does not prove a usable Home-input
  interval; the visible modal chooser is not free access to its blocked owner.
- The MAIN shell's Home Role.Tab reaches the forward-navigation gate/current-token
  selection. Backup still supplies only a composed registration, unlike the live
  credential policy. Host-resumed and active-entry checks remain real guards.
  No direct event, remembered callback or ordinary Back can substitute for the
  proposed native Tab input; ordinary Back requests cancellation before pop.
- The owned-focus counterexample is valid, narrowly: WindowProtection:80–86,336–342
  skips scheduling focus-loss locking for an opposite window in the protected
  frame's owner chain. `activeOwnerFrame` can also return null; a non-owned/null
  opposite window is not exempt. This guard neither cancels an earlier timer nor
  proves shared lifecycle RESUMED, session continuity, or an uncancelled export job.
  The actual wrapper attaches protection, configures minimize/focus locking with a
  30-second focus delay, and routes a lock through shared security acknowledgement
  (PassVaultDesktopWindow:238–278,307–324,453). Thus neither “chooser always locks”
  nor “owned chooser guarantees no lock” follows. Toolkit behavior remains unobserved.
- A returned Home detail must genuinely load: the entry's LaunchedEffect invokes
  loadCredential, which resets even the same ID. Rendered delete, its router and
  eventual confirm's ViewModel all check current loaded/non-busy state. Merely
  selected Home or an old loaded fake is inadequate. Opening the real delete dialog
  while export-busy is observed is only **delete-intent admission**; it is not a
  delete job, V acquisition, A wait, lock cycle, permanent hang or durable harm.

## Is the finite observation worthwhile?

**Yes, as one necessary precursor, not a complete PVU-003 experiment.** The fixed
pre-chooser Export→Home pair could provide genuinely new input-order/guard evidence
without admitting opposing deletion or a broad race search. Its successful outcome
must be reported as the actual selected/detail/dialog predicates and export-busy
state, never as proof of the private export Job's survival. Native automation is
not physical-user timing evidence. Omit the optional attachment for this first gate:
there is no attachment/deletion oracle here to justify its extra work or outputs.

No Home transition, picker cancellation, denied delete, finished export or missed
finite timing is a counterobservation/INCONCLUSIVE result, not universal disproof.
Separate an unsent input from delivered input without a transition; missing AWT
records do not prove modality or a particular navigation rejection. Timestamped
observations must distinguish actual queried window visibility from delayed event
notification. Observe the real lifecycle, not an inference from native focus alone.
Do not silently change to a post-dismiss pair, Back, repeated timing, held frame,
blocked DAO/writer, injected policy or forced coroutine scheduling. Stop before the
real confirm button. Genuine dialog/picker dismissal and terminal cleanup still
need exact, separately reviewed ownership and native locator contracts.

## Nonduplication and smallest productive software step

Independently read android32's sealed `reviews/android32/navhost-draft/` design
`NAVHOST-ROOM-SOURCE-DESIGN.md` SHA256
`96b30125d27568dafe021f131180ca2f50f9204421af3ecb920f6f49d048a101`, and focused
GraphOwner/native input/owner sections of `CredentialNavHostRoomIntegrationTest.kt`
SHA256 `e4778ee2415b89688834ecb72865ae7aa94dffff696dc05733284f17288fd2d5`.
That unintegrated PVA-007 scaffold is independently owned/reviewed elsewhere. It has
no PVU-003 case; its helpers are private and scenario-specific, not an already
available common driver. This review does not replace its independent source review.

The minimal next software work is **one coordinated test-only scenario adapter and
small shared input/observation primitives in the prospective app-desktopTest lane**,
not another full NavHost/Koin driver or a product-policy patch justified by a race
that has not been observed. Preserve PVA-007's scenario and qualification boundaries.
Three concrete constraints prevent simply swapping a composable or copying helpers:

1. Current `click` calls `scrollTo`, including an 80ms stable-target wait on each
   call; it rechecks an active owner (559–609,666–669). Two such calls are not the
   proposed pair with both targets resolved beforehand. The new finite pair needs
   an independently reviewed input contract without inserted frame/target waits
   between its two dispatches, no stale callback, and honest delivered-target evidence.
   Current geometry is also capped at 1000px width; the shipped wrapper defaults to
   1200px. Its geometry/real native SAVE locator must be explicit, not assumed reusable.
2. Use actual PassVaultDesktopWindow and the same real GlobalContext graph. It also
   initializes tray, biometric attachment, menus, native protection and persistent
   window preferences, not only a Window. The plain scaffold intentionally omits
   these. Source/owner/output review must include those additions without replacing
   security/lifecycle behavior, assuming hardware coverage or borrowing C13 admission.
3. Existing GraphOwner.close cancels root/app jobs and closes bootstrap itself
   (320–351); actual wrapper close starts DesktopShutdownCoordinator cleanup
   (wrapper:172–180; coordinator:48–60,89–109). Combining both unchanged risks
   duplicate/racing terminal ownership. Specify and source-review one terminal
   owner before an executable variant; no automatic lock/close retries or copied
   cleanup authority. A test-only observer does not confer cleanup admission.

## Byte identity and scope receipt

All sixteen production files explicitly referenced/pinned in the author draft were
independently hash-matched and their relevant guards read. AppModule additionally
matched `9b486df509202e65311caf92895ba8db6a44e6eae351266c8daa952bc553af25`, including
the singleton BackupViewModel and production shared attachment/session bindings.
Additional focused production bindings (not a Git tree or execution identity):

| Relative source path | SHA256 |
| --- | --- |
| app-desktop/src/desktopMain/kotlin/com/passvault/desktop/PassVaultDesktopWindow.kt | 794a8fdbaea87edca986b2f729753dad3d434467f21e59ec1fcbb9027402e702 |
| app-desktop/src/desktopMain/kotlin/com/passvault/desktop/DesktopShutdownCoordinator.kt | 7156e03eb7e5d2ea7636a9320a49aeaf15d0694522d88f78836b075c74e9043d |
| app-desktop/src/desktopMain/kotlin/com/passvault/desktop/Main.kt | 9a67a8a5c2f05331e91ec5c18f6317a3562bd26e2c720105a8a21773edd8ae93 |
| shared/src/commonMain/kotlin/com/passvault/shared/PassVaultApp.kt | 75349af0300801f0cad87f72b6afe9701e54bd24dafdd8867d7509c776506737 |

All 27 bound source/report files were rebound before this exclusive0600 write,
file/parent fsync and exact readback/hash. One oversized read's truncated focus
excerpt was completed by a bounded subsequent inert read, not a runtime retry.
Only this permanent note/directory was created. No source/frozen C13 edit, helper
import, syntax probe, Git/network, application/native input, build/test, runtime,
cache, storage/process sweep, cleanup action or persistent worker occurred; wrapper
stop is N/A. Root retains execution/coordination/cleanup/publication ownership.
PVU-007 STOP, PVU-011 NO-RETRY, PVA-029 failure/no automatic retry, G7/G8 CLOSED,
Windows05 filesystem HOLD, all consumed/HOLD scopes, PVD and hardware gaps remain.
**Zero executed cases, fixes or closures; every denominator is unchanged.**
