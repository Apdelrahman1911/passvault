# PVA-007 NavHost / Room draft — independent source challenge

Reviewer: `/root/editor_review`; author: `/root/android32`; 2026-09-10.
**Disposition: HOLD for integration/execution admission; useful prospective scenario.**
No concrete new product defect is established. The owner/API/classpath proof gaps below
are resolvable software investigation, not a final external or hardware blocker.

## Exact excluded inputs

Both inputs are under `reviews/android32/navhost-draft/`, relative to this continuation directory.

| Input | SHA-256 | Bytes / LF |
|---|---|---|
| `CredentialNavHostRoomIntegrationTest.kt` | `e4778ee2415b89688834ecb72865ae7aa94dffff696dc05733284f17288fd2d5` | 47,819 / 959 |
| `NAVHOST-ROOM-SOURCE-DESIGN.md` | `96b30125d27568dafe021f131180ca2f50f9204421af3ecb920f6f49d048a101` | 9,954 / 145 |

These are outside a Gradle source set and the frozen C13 selection. This review changes
neither. Exactly **one prospective `@Test`, thirteen ordered stages, zero executions**;
no compilation, closure, test count or denominator increase is asserted.

## Guard and counterexample challenge

- The intended graph is real: production application loop, actual `Window`, public
  `PassVaultApp`, real Koin modules, NavHost/entry decorators, repository and libsodium.
  That source wiring does not by itself prove the missing Window-owner contract.
- Pending custom-field drafts participate in `hasUnsavedChanges`, independently of
  saved rows. The toolbar emits `OnCancelClick`; the real router deliberately sends
  both Cancel and Back through the leave guard. Host Escape uses the real coordinator,
  active entry token and RESUMED check, rather than a test-injected leave callback.
- The MAIN dock is present during editing. Dirty Settings/Add call the forward-leave
  guard; they do not enqueue their destinations. Stay preserves pending name/value;
  Discard is expected to pop to prior detail, not replay Add. Clean Settings→Home and
  clean Escape are meaningful positive controls for stack preservation and back.
- Native input is the only UI mutator; accessibility is read-only. Real `Role.Tab`
  and selected state exist in source, but AWT PAGE_TAB mapping, unique card/field names
  and native focus must still be observed. Active, stable client-contained rectangles
  do **not** alone exclude action-dock occlusion. Missing/ambiguous targets must fail,
  not be replaced with direct callbacks or treated as an application navigation defect.
- Reopen constructs a new real bootstrap and distinct Room object only after initial
  graph teardown, retaining the same database file key. Exact original credential and
  field IDs/names/values/secret flags are checked, not merely an item count. This is a
  fresh Room reopen in the captured child, not process-restart, crash or recovery proof.

## Specific unresolved integration proof

The imported lifecycle/store APIs and `LocalViewModelStoreOwner.current` require actual
pinned third-party API/Window-owner provenance and the admitted compile classpath.
Existing Main's Window nesting or a lifecycle-only fixture proves neither. `shared`
declares lifecycle dependencies as `implementation`; app-desktop's dependency on shared
and Compose alone is not proof of compile visibility (nor proof of its absence).
Author independently confirmed that no retained owner/API or actual compile-classpath
proof had been established. A separately assigned bounded source-resolution lane is
in progress; its evidence must be reviewed separately without rewriting these inputs.

The draft correctly requires a supplied actual Test runtime classpath rather than
substituting `java.class.path`; build-task forwarding remains prospective. Do not add
unapproved dependency edges or manually supplied/resumed owners to bypass a failure.

## Cleanup and safety challenge

Private home, preferences, XDG and native temporary roots are set before child startup;
canonical path/file-key checks do not replace root's allocation and display authority.
No clipboard read/copy/paste is in the scenario: actual `DesktopClipboardService.clear`
and its shutdown guard return before OS clipboard access when no transferable is owned.

Known singleton/app jobs are cancelled/joined; the observed Window store is cleared and
composition/driver settlement is checked before database close. This does **not** join
every nested Nav3 entry job. Koin stop is not an alternative scope-settlement oracle.
`onEdt` timeout cannot stop already-running native work; actual repository lock and
bootstrap checkpoint/close use `NonCancellable`, so nominal coroutine deadlines cannot
universally bound them. Both real Room owners have one terminal close attempt; no retry
is authorized. Cleanup failure must prevent reopen and any passing evidence claim.

The 512-MiB child/one-processor cap, 150-second driver and 240-second parent deadline
are useful bounds, not guarantees of graceful settlement. Force termination is limited
to the captured child and records failure; it does not establish external descendant,
X-server or namespace settlement. The fixture deletes nothing. Root still owns sole
build admission, wrapper stop, worker/process settlement and allowlisted cleanup.
Skipped XML (including unavailable synthetic display) is not successful case evidence.

## Evidence boundary / next gate

Fresh source identity, actual classpaths, exact one-case filter, isolated display/storage,
serial build ownership and cleanup admission remain prerequisites. No Main,
PassVaultDesktopWindow, instance-lock, tray, curtain, native focus-loss/security,
production shutdown-coordinator, PVU-003 or physical-device coverage is claimed.
PVU-007 STOP, PVU-011 NO-RETRY, PVA-029's failure/no automatic retry, G7/G8 CLOSED and
all consumed/HOLD scopes remain unchanged. No old runner/helper is admitted by this note.

Only inert source reads and this new permanent review were performed. Source-printer
range/locator errors were corrected without invoking the target; no build/test/runtime
process, temporary application output or cache was created. No frozen file was edited.

## Additional independently checked source identities

The input design binds its six graph/host sources. These additional byte identities
bind the guard, dependency and cleanup observations above; this is not a whole-tree
execution identity.

| Source | SHA-256 |
|---|---|
| `shared/build.gradle.kts` | `826cc234a59c05efdbbcd6a0ea62f25362f0a54d588e996582903f44d09949ad` |
| `app-desktop/build.gradle.kts` | `381ef53b5bc4ae775aa2322dbaea02588d499111d2a32e2ca3cdb2557363acde` |
| `shared/src/commonMain/kotlin/com/passvault/shared/navigation/NavigationBackCoordinator.kt` | `7f0f546301a87c0bd758d121a118d8325b2fe366561655d1ee5c8e1b248126db` |
| `feature/credential/src/commonMain/kotlin/com/passvault/feature/credential/presentation/CredentialViewModel.kt` | `b9a9c5e6a9e9a5eee86a13a639fa32826dbfdfbd09fa81a032935a37f58f0611` |
| `feature/credential/src/commonMain/kotlin/com/passvault/feature/credential/presentation/CredentialEventRouter.kt` | `b1f18f9e32beff113d0c6d26dc16fc3b9e24460ef6039a3dc144554815431f5a` |
| `core/security/src/desktopMain/kotlin/com/passvault/desktop/security/DesktopClipboardService.kt` | `0e5d97c6bd182e5e732fc68b5861f267305e05b1d51a1378a57406c849e08c4a` |
| `core/database/src/commonMain/kotlin/com/passvault/core/database/VaultDatabaseBootstrap.kt` | `5726afb24093afa8c56c57321d64f0ea747db69910f0b7aa984e9d18a86f2598` |
| `core/database/src/commonMain/kotlin/com/passvault/core/database/repository/VaultRepositoryImpl.kt` | `aee86a3e876da0318c750c350c3ec4eb8938aa1929123533c041c3a10ec38630` |
| `core/database/src/desktopMain/kotlin/com/passvault/core/database/Database.desktop.kt` | `2aa4d34c71de4dae66a79e6d93af187a44642e4a504ff2a592d82c60245f39a7` |
