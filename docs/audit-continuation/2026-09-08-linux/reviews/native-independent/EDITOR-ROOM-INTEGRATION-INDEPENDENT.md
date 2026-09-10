# Editor → Room integration: independent source review

Reviewer /root/native_review, independent of author /root/android32; 2026-09-10.
W = /root/projects/PassVault/passvault-linux; B = W/docs/audit-continuation/2026-09-08-linux.

**Accept the prospective one-case fixture source after the bounded interruption correction below.**
No remaining fixture source blocker was identified within this narrow contract. This is NOT compilation,
execution, storage/cleanup admission or qualified PVA-007/031 closure. Root owns all such decisions.

## Exact current pins

| File relative to W | SHA-256 | Bytes / LF |
| --- | --- | --- |
| shared/src/desktopTest/kotlin/com/passvault/shared/credential/CredentialEditorRoomIntegrationTest.kt | a67ba81ad875a4c611208d30b51e40caf87cf5381622f5d6ababf38d2953cff9 | 36,660 / 712 |
| docs/audit-continuation/2026-09-08-linux/reviews/android32/EDITOR-ROOM-INTEGRATION-SOURCE.md | b754e9ac36f8735fedf34d778be5d1bdd72c9ee65ec264c8f10343cf27243d7c | 3,980 / 64 |
| docs/audit-continuation/2026-09-08-linux/reviews/android32/EDITOR-ROOM-INTEGRATION-FEASIBILITY.md | 9f5cbf27d79652e2e2481fee6d134a726e1f62767c4ccf9ea22dd3e3c9bed8d6 | 4,248 / 67 |

The initially reviewed test was de0159dfe5a7fc3aeb80370ce6bff7e35125cdb085e253d02499a2c338a54727
(35,945 bytes / 702 LF), author note 72ef32e9e4dbed0ff8278d7b98cc891072063e6454003caf7204944a92fe3feb.
Full original source and the final closeRoom delta were independently read. Replacing only the revised
closeRoom method with its original bytes reconstructs the original test pin. No production or existing
GUI02 fixture edits were needed for this correction. W has a retired Git store: these are file-byte pins,
not a HEAD/tree claim; root must bind the full source/dependency identity of any admitted invocation.

## Success contract and counterexamples challenged

- Selection is exactly one method in com.passvault.shared.credential.CredentialEditorRoomIntegrationTest,
  :shared:desktopTest: native capacity draft persists through page Save and a fresh Room database reopen.
  Its two Room generations are not two test cases. No unchanged GUI02/49-case work is revalidated here.
- Production createDatabaseBootstrap opens the actual user.home/.passvault/vault.db path, using Room,
  bundled SQLite, registered migrations and IO dispatch. The test supplies production Desktop crypto,
  vault/session, credential/folder repositories and real DAOs, not an in-memory builder or fake backend.
  Synthetic seed creation is only in seed=true and saves exactly 49 fields through the real repository.
- AWT Robot performs Add, ASCII Ctrl+A replacement, row Edit and secret checkbox input. Accessibility
  is read-only locating/state evidence; it does not invoke accessibility actions or replace native input.
  Real form callbacks are counted before dispatch to the real ViewModel. Cardinality and active owned
  window/geometry/focus guards reject wrong targets instead of fabricating callbacks.
- The added field becomes number 50 with a unique ID; all original 49 tuples are checked unchanged.
  After native edits, the pending draft's exact name/value/secret and dirty state are inspected before
  Save. Row Save is deliberately untouched. Two Save buttons must exist; the topmost/page target must
  produce OnSaveClick. Production ViewModel commitDrafts precedes validation and repository save.
- Completion waits for not-saving, clean, error-free state, not merely a callback or fixed sleep.
  A state-only false success would still fail the later reopened tuple oracle. The repository encrypts
  custom-field ID/name/value/secret into the real secret payload and authenticates/decodes those fields
  on load; it is not a memory cache supplied by the fixture.
- A blank composition with a two-frame barrier replaces the old form; the old ViewModelStore is cleared,
  its recorded root Job joined off the EDT, and successful session lock/checkpointAndClose required.
  New bootstrap, Room instance, session/repositories and ViewModel are constructed with seed=false.
  The database must remain a bounded regular no-follow file with the same fileKey, and Room instances
  must differ. Fresh state must have no drafts/dirty flag and exactly the expected 50 decoded tuples.
- Reopened Add must be disabled at capacity. The rendered name and the secret-masking accessible
  description must be visible. This is native-input/render-tree evidence, NOT screenshots/pixel proof,
  a screen-reader exercise, or proof of secret erasure. Geometry/assertion failure is a failed case.
- Reopening a new Room/VM within this same test JVM establishes only that prospective connection/owner
  boundary. It is not a new-process restart, abrupt power-loss, fsync durability or migration matrix.
  No backend-rejection race, pre-frame race, NavHost Back/tab/forward, mobile/IME/RTL/hardware or arbitrary
  owner-retention claim follows. No preservation/reset/recovery operation is requested.

## Independent correction: preserve secondary interruption

Original closeRoom retained the first lock failure and suppressed a later checkpoint/close exception,
but did not retain a later InterruptedException's cleared thread flag. For example, a non-interruption
lock failure followed by an interrupted runBlocking checkpoint would rethrow the first failure with IE
suppressed. Outer close inspected only entering/direct-primary interruption and could report false and
return with cancellation status lost. The author accepted this concrete source counterexample.

Final lines 394–422 capture/clear entering interruption, track IE/direct cause and newly set flags after
each phase, preserve first Throwable plus distinct later suppressed failure, and restore the flag in an
outer finally before propagation. The enclosing cleanup release can now observe that restored flag even
when IE remains secondary. Existing attempt flags stay set before calls; no retry is added. This fixes
fixture failure handling, not a newly claimed product family. It has been source-reviewed, not executed.

## Lifecycle and fresh-run mapping obligations

- With no display opt-in the case assumption-skips before bootstrap/AWT: zero credit. After opt-in, the
  Linux/English/X11/accessibility and home checks are mandatory, including on missing roomHome input.
- The explicit roomHome must equal user.home exactly, be canonical/absolute/bounded, contain no symlink
  components, be current-process-owned mode0700 and initially empty with no .passvault. fileKey/owner/
  mode/path are rechecked before each bootstrap. These are fail-fast guards, not proof of original
  allocation authority, immutable ancestors, ACL safety, race exclusion or freshness by pathname.
- Root must admit a fresh dedicated Test worker, separate private HOME/tmp/native-cache environment,
  safe ancestor/allocation receipts and exclusive synthetic display. Do not share its required-empty
  home with a tray/panel worker, place preexisting caches there, or switch user.home in-process.
  The fixture alone does not prove JDK17, full classpath/native resources, source identity or X authority.
- Project source declarations support the supplied repository signatures, inherited default Manual
  lock reason, shared database/crypto/Room/lifecycle access and Desktop Compose/coroutines-swing runtime.
  This review performed no compiler/API execution; the actual module Test classpath and platform native
  resolution remain admitted-run obligations, not grounds to silently add dependencies.
- Input releases and window disposal are bounded to captured owned handles. ViewModel stores are cleared,
  jobs cancelled/joined, and Room close requires completed recorded VM jobs. First errors and cleanup
  failures survive. A failed/uncertain close is not retried; no recursive deletion exists in this fixture.
- 240-second polling, 60-second setup/reopen and 5-second/3-second waits are cooperative. Real vault lock
  and checkpointAndClose use NonCancellable; EDT/native/Room operations may outlive caller timeouts.
  Cancelling a FutureTask does not stop an already-running EDT action. Neither disposed windows, joined
  VM jobs nor fixture stdout prove the entire worker/native/Room descendant scope has settled.
- Root's external wall/RAM/disk/process/crash controls, stop/settlement policy and independently reviewed
  allowlisted cleanup must precede invocation. Production Argon2/native allocation is real, not bounded
  by JVM heap alone. Preserve compact necessary synthetic diagnostics, not a database/binary archive.
- PASS stdout is printed before final cleanup and is not sufficient success. Require exactly one
  executed matching XML case with zero skips/errors/failures plus required cleanup/evidence. Partial
  state, timeout, interruption or uncertain settlement remains failure/HOLD, never cleanup authority.

## Focused surrounding source pins

Paths below are relative to W; these stable byte reads support the preceding source conclusions.

| Source | SHA-256 |
| --- | --- |
| core/database/src/desktopMain/kotlin/com/passvault/core/database/Database.desktop.kt | 2aa4d34c71de4dae66a79e6d93af187a44642e4a504ff2a592d82c60245f39a7 |
| core/database/src/commonMain/kotlin/com/passvault/core/database/VaultDatabaseBootstrap.kt | 5726afb24093afa8c56c57321d64f0ea747db69910f0b7aa984e9d18a86f2598 |
| core/database/src/commonMain/kotlin/com/passvault/core/database/repository/CredentialRepositoryImpl.kt | 910fa9d999a3ef7327e3e70d6a9aa142eabf8b14131402f4415f3ef4e72f1e9f |
| core/database/src/commonMain/kotlin/com/passvault/core/database/repository/VaultRepositoryImpl.kt | aee86a3e876da0318c750c350c3ec4eb8938aa1929123533c041c3a10ec38630 |
| feature/credential/src/commonMain/kotlin/com/passvault/feature/credential/presentation/CredentialViewModel.kt | b9a9c5e6a9e9a5eee86a13a639fa32826dbfdfbd09fa81a032935a37f58f0611 |
| feature/credential/src/commonMain/kotlin/com/passvault/feature/credential/presentation/CredentialFormSupport.kt | b3403f38c94808df3d9f9d227353fc0505bb0b3848d20a69da63689424a2a66c |
| feature/credential/src/commonMain/kotlin/com/passvault/feature/credential/ui/components/CustomFieldsEditor.kt | dd60f4a65b4cf951a8ba94b2b6e4cab8440d1a33942eb046f1d74ccfcf9b31db |
| core/domain/src/commonMain/kotlin/com/passvault/core/domain/repository/CredentialRepository.kt | 67e34bd0116ab988478fd4d1e11de63feac6b442f95a6b43d4f215cc14b8d648 |
| shared/build.gradle.kts | 826cc234a59c05efdbbcd6a0ea62f25362f0a54d588e996582903f44d09949ad |
| feature/credential/build.gradle.kts | a471dd3cf229c869059781856a387d2a2b7960af31bf88aeef0e74f918b5a47d |

## Review scope and accounting

One prospective case; actual compiled/executed cases, builds and hardware observations: ZERO.
No closure/denominator change. The source-only Room complement does not replace unfinished PVA-007/031
or grant credit to any original suspicion. All eight PVD design boundaries remain separate.

Read root AGENTS and focused supplied snapshots; used bounded inert reads/named source-directory listings,
hashing and byte inversion only. Short FEASIBILITY.md was root shorthand: the exact long-name note above
was supplied/verified. A bounded inert read stopped at absent VaultRepository.kt; the actual contract was
then read in CredentialRepository.kt. Other guessed source-name absences were resolved through source
directory names. These are source-navigation diagnostics, not application failures or runtime tests.

No project command, source/helper import or execution, AST/syntax probe, build, test, Git/store, API/network,
runtime/application-storage traversal or host-process probe occurred. No temporary/cache/generated output
or background worker was created. Wrapper --stop/runtime deletion is N/A; root retains host monitoring.
Only this new permanent review was written exclusive/no-follow mode0600, fsynced and exact-readback checked.

PVU-007 STOP, PVU-011 NO RETRY, PVA-029 failure/no automatic retry, G7/G8 CLOSED, Windows05 filesystem HOLD,
all other consumed/HOLD scopes and physical-platform gaps remain unchanged. Fresh execution or CI never
inherits this source review as a bypass of those restrictions.
