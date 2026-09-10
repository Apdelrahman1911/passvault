# PVU-003 actual chooser / Home attempt / native cancel — inert snippet

2026-09-10; author `/root/editor`. **Source-only implementation; independent challenge requested.**
PVU-003 remains unresolved. No new finding, product fix, executable test case, closure or execution admission.
Paths below are repository-relative unless explicitly shortened under `reviews/`.

## Exact artifacts and reuse

- New fragment: `reviews/editor/pvu003-draft/PVU003-CHOOSER-HOME-CANCEL.kt.txt`,
  SHA256 `76d198977e09cfd38e664cd957f5c3aa159c76488843e4b4dad36667b175cbd3`, 9951 B / 173 LF.
  It is deliberately inert: import additions plus driver members, not a standalone Kotlin unit or `@Test`.
- Reused, not edited: `reviews/android32/navhost-draft/CredentialMainNavHostRoomIntegrationTest.kt`,
  SHA256 `7eee7bc045abcb60660203f965a33a71e6e791d9ba2c22509a52ee615040ec9d`, 38728 B / 727 LF.
  This is the corrected actual-Main variant, including its single queued terminal Ctrl+Q block.
  The older 38330 B terminal-race draft is not the reuse identity.
- Prior author design: `PVU003-NATIVE-ADMISSION-OBSERVATION.md` in this directory,
  SHA256 `7aef13ebf80d4312b081830f076ec154b2f72abf8da58c33f46ac5edfd910f81`.
- Prior independent conditional review: `reviews/native-independent/pvu003-draft/`
  `PVU003-NATIVE-ADMISSION-OBSERVATION-INDEPENDENT.md`,
  SHA256 `3815b3519cd59131e83d6d22ef6f418e1cf03d0117229de637cd23e705ca74b4`.
  Its accepted guard analysis/rejected stalls remain evidence; it does not review this new fragment.

This is the narrower latest assignment: **wait for the actual native chooser to be visible, attempt Home
once, cancel the chooser**. It does not implement the old immediate pre-chooser Export/Home pair, loaded
Home-detail/delete-intent observation, deletion confirmation, or any lock-cycle scenario. None is credited.
No second 700-line harness, module adoption, build wiring, production hook, mock or fake stall was added.

## Concrete native and observation contract

The future same-driver scenario unlocks the single seeded synthetic vault through real input, follows
Settings → Data Management → Export, types a synthetic all-lowercase passphrase distinct from the master,
and requires the real singleton BackupViewModel's `canExport` plus exact typed passphrase before clicking
Save encrypted backup. GlobalContext is observed only after the actual backup screen exists; no Koin graph
is created or replaced and no ViewModel event is invoked by the fragment. AppModule:197–201 supplies the
same session-scoped singleton. The driver still owns only synthetic input; actual Main owns its graph.

One visible owned SAVE FileDialog is bound by object identity. Home must be an enabled, initially unselected
real PAGE_TAB inside the main client's bounds. The actual dialog must be application-modal, active/focused,
directly owned by the frame, and the frame must have NO_EXCLUDE; no other owned surface may be showing.
The chooser rectangle padded by 16 pixels must not overlap Home. Existing admitted-display/client-size
bounds apply. No focus, activation, window move, geometry setter, modality setter, or direct UI callback is
used. Unsupported AX/native-chooser geometry or an occluded dock fails closed, not an invitation to alter it.
After moving the pointer, context/rectangle/focus/busy preconditions are rechecked before exactly one native
Home press/release. Unlike the ordinary `click`, this does not demand or force activation of the blocked frame.

A passive constant-memory AWT listener records only matching frame-owned button-one press/release events
inside that Home rectangle. Four bits distinguish Java delivery while the dialog reports showing versus
after it reports hidden. It never consumes, dispatches, blocks, logs, writes a file or changes input. Native
peer/modal filtering may happen before Java delivery: absent bits do NOT prove absent native input. Native
call completion is issuance, not physical delivery; Java delivery is not proof of Compose onClick execution.

The required normal return covers 20 post-input samples with 20ms pauses, each querying chooser visibility,
export-busy and actual Home-selected state, then one genuine Escape cancellation, followed by export-idle
and 10 post-cancel samples. These are nominal poll spacings, NOT 400ms/200ms proven elapsed windows or frame
acknowledgements: each inherited onEdt call has a 5s cap and every sample checks the inherited 150s driver
deadline. The original parent has a 200s Main-child cap and remains the necessary hard containment owner.
No run, Home-input, Escape-input or alternative timing retry is proposed.

Cleanup is installed before application interaction. It uses only the captured chooser, or one safely
owned unique visible candidate if capture was interrupted; title/mode/owner and active focus are rechecked.
It sends one Escape, awaits dismissal, requires `dialog.file == null`, and never selects a destination, edits
a file field, clicks Save/Enter, calls `dispose`, or invokes OnCancelOperation. The production path creates
an output only after successful selection, so this scenario deliberately stops before the backup service.
Native chooser default-directory, private HOME/XDG/cwd and auxiliary toolkit effects still need explicit
root admission; this source note is not proof of filesystem isolation. No real vault or backup is involved.

`MainNavFailures` preserves primary/suppressed failures and interruption. Listener ownership is captured
before registration and its exact object is removed in `finally`, including registration/EDT failure paths.
Post-cancel delivery bits are read only after removal. Failed removal, focus acquisition, cancellation,
input release or in-flight EDT/native work remains a failure and outer hard-settlement/HOLD obligation;
there is no success-by-forced-shutdown. Native-input target checks still have an unavoidable observation /
dispatch interval: exclusive display authority and root containment remain necessary, not claimed away.

## Result and integration boundaries

A returned DTO separates Home selected while the real chooser is showing from selection observed only
after native cancel, plus passive delivery bits. Home-before-dismissal is useful bounded UI evidence;
Home only after cancel is not modal admission. No transition is bounded counterobservation, not universal
disproof or proof of a particular NavHost rejection. Export-busy is not private Job survival, lifecycle
RESUMED, an acquired lock, a lock cycle or data loss. There is no credential click, delete request or confirm.
The existing Back-before-pop cancellation and prior conditional A/V ordering analyses remain unchanged.

A deliberate separately named scenario variant must integrate the private helpers and return result to a
separately reviewed, bounded receipt writer BEFORE the real Main terminal path. The existing PVA-007
MainNavTrace allowlist and exact ordered EVENTS oracle must not silently acquire new event names or count
this fragment as an extra test. The fragment supplies no test wrapper, properties, receipt schema, child
launcher, extra lifecycle owner, terminal cleanup or filesystem cleanup. Main's graph/instance lock/
coordinator and its genuine exit remain under Main ownership; no competing close is added.

## Production byte bindings

Relevant bytes were bounded no-follow read/hash-checked; these are source identities, not a Git tree or run:

- `feature/settings/src/commonMain/kotlin/com/passvault/feature/settings/ui/SettingsScreen.kt`
  `ab34bfa20fcb176701dc3ff6b2414191acf759b5aa912f84d8bd63825f0bbd74`
- `feature/settings/src/commonMain/kotlin/com/passvault/feature/settings/ui/DataSettingsScreen.kt`
  `af0ba12dc3fa2119d53f25a1e73d06bacd67500f4ca301fc7dce6b288daa732c`
- `feature/backup/src/commonMain/kotlin/com/passvault/feature/backup/ui/BackupExportSections.kt`
  `b6f1a3afc82ec5873f50f4764530f04a801886075985c6303f455b09ef7cb9c2`
- `feature/backup/src/commonMain/kotlin/com/passvault/feature/backup/presentation/BackupViewModel.kt`
  `afd73521b4fc0d1b06af29bdc7d4940f4646507a66fcc4e55d801f2448060e63`
- `core/designsystem/src/commonMain/composeResources/values/strings.xml`
  `5605f102cca37a9fddf7fe50ecd598c5460168fd9eb6149d4d7a8318f644b118`
- `app-desktop/src/desktopMain/kotlin/com/passvault/desktop/backup/DesktopBackupFileStore.kt`
  `ce13726b1c0c6d401c1a8d6d5a47abb18b129984eb1d205fd24a021fe94f9fd5`
- `shared/src/commonMain/kotlin/com/passvault/shared/di/AppModule.kt`
  `9b486df509202e65311caf92895ba8db6a44e6eae351266c8daa952bc553af25`

The actual canExport check preserves password acceptance/GOOD-strength and active-operation guards.
Cancellation's existing generic save-error presentation is not silently changed or asserted away.

## Authoring / resource / admission receipt

A prior authoring writer stopped at a data-only line-length guard before opening the target; it created
no target and is not an application/test failure or execution retry. The corrected fragment passed only
byte/line-bound checks and exclusive0600/fsync/readback hashing. No syntax/AST probe, source/helper import,
compile, application/native input, build/test, filesystem/process/cache/SDK sweep, Git, network or runtime
cleanup occurred in this source-only lane. Only two small permanent review artifacts were added; all short
foreground data operations reaped normally. No temporary output/cache or persistent worker was created;
wrapper stop is N/A, not falsely credited. Root retains sole execution/coordination/cleanup/publication.

Fresh independent source review, future deliberate integration review and execution/cleanup admission remain
required. PVU-007 STOP, PVU-011 NO-RETRY, PVA-029 recorded failure/no automatic retry, G7/G8 CLOSED, all consumed/
HOLD scopes and PVD/hardware boundaries remain. **Zero executed cases, fixes or closures; denominators unchanged.**
