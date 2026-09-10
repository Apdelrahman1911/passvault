# PVU-003: real export preflight supplies a conditional queue, not UI admission

2026-09-10; author `storage`. **UNRESOLVED; no confirmed defect, patch, test,
execution admission or closure.** Independent challenge is pending root's
post-GUI02 scheduling of `editor_review`.

Starts from accepted `PVU003-PREPOP-CANCELLATION-NARROWING.md`, SHA-256
`c2196bb8f60165e3d10afb4bc2f547cabe9f9b80bf3805a7cb7d20c7da67c76d`.
Its distinct Tab path, singleton backup owner, Back cancellation countercontrol
and Linux03 qualifications are retained, not re-audited here. This follow-on
traces only the selected production export/Tab/delete discriminator.

## Real prerequisites which a component-only witness omits

The backup ViewModel synchronously claims export busy before launching its job
(`S12`:185–213), but it must obtain a real output before calling the backup
service (`S02`:12–25; `S12`:677–695). Desktop performs an AWT SAVE `FileDialog`
on Swing and creates a destination-directory temporary (`S01`:36–53,96–118,
204–236). The modal chooser is not bypassed here; no delivered Tab event before
its input boundary or the composed busy-policy update has been demonstrated.

Returning to the Home detail is not proof that deletion is already admissible.
The production detail entry calls `loadCredential` in a `LaunchedEffect`
(`S09`:76–98). That method cancels old work and resets the state to loading;
there is no same-ID early return (`S08`:111–155). The displayed delete action
needs a loaded, non-busy credential and opens a real confirmation dialog
(`S10`:178–195,428–446); the ViewModel independently checks loaded/not-busy
before claiming deletion (`S08`:275–284). A prepared fake loaded state or direct
confirmation call does not satisfy these prerequisites.

## Concrete conditional opposing-lock schedule

Let **A** be the singleton attachment-operation mutex and **V** the vault
operation mutex. The following is a source-supported *conditional schedule*,
not an observed admitted UI sequence:

1. Real export-to-Tab input succeeds through the production navigation gate;
   the real Home detail finishes any reload, the native destination is selected,
   and the uncancelled export reaches password validation. These input/loading
   events still require evidence; a wait in a native chooser is not assumed to
   complete Home composition or loading.
2. A valid, distinct backup password which also passes existing-master-password
   length policy reaches `matchesMasterPassword`. It holds **V** while reading
   metadata and unwrapping the candidate key (`S03`:181–196,254–268;
   `S07`:108–129,490–508,675–698). Real derivation dispatches to
   `Dispatchers.Default` (`S11`:37–67,285–305). The key comparison is not an
   arbitrarily held mock/provider scope.
3. A real delete confirmation admitted during that preflight can wait for **V**.
   After the preflight releases it, deletion can own **V** while its actual
   `credentialDao.exists` query is outstanding (`S06`:259–268). Export can then
   acquire **A** and wait for **V** (`S04`:58–61; `S05`:54–59).
4. If that credential exists and the delete query resumes after export owns
   **A**, deletion calls `deleteCredentialAndAttachments`, waiting for **A**
   while retaining **V** (`S06`:261–266; `S05`:307–323). Both callers are real
   production operations, but their actual UI admission, mutex ownership and
   ordering have not been observed. No direct repository calls are proposed as
   reachability evidence.

A separate natural **A**-only boundary exists when attachment recovery has not
yet completed: export holds **A** during the pending-row/path queries and blob
sweep before asking for **V** (`S05`:54–59,336–351). Normal credential loading
reads attachments directly through its DAO (`S06`:755–765), rather than proving
that recovery ran. This is conditional on real recovery state, not permission
to toggle that private flag or artificially suspend a repository.

## Counterexamples and exact remaining limit

- If Tab input loses to the current busy policy or native dialog, the alleged
  two-caller UI sequence is not established. Cancelled picker/output creation,
  invalid/reused password, failed reload, missing credential and busy deletion
  are also countercontrols, not passing witnesses.
- If export acquires both locks before deletion, deletion merely waits for
  export; if deletion finishes before export acquires **A**, there is no cycle.
  A warm recovery path cannot be assumed to supply the cold-query interval.
- Slow stream writes occur *inside both* A/V scopes; they are not the initial
  A-without-V gap. Sink commit is after both scopes exit, and abort is outside
  their unwinding scopes (`S04`:58–101). Stalling commit/abort or replaying the
  already rejected stale-finalizer/fake-cancel example cannot supply the cycle.
- Cancellation request is not measured settlement. Real lock/cancel/join controls
  and separate Room-row/object/output observations after proper disposal remain
  necessary. No permanent hang, data loss or durable commit is claimed.

**Remaining gate:** real native export→Tab input through the shipped navigation
and file chooser, current credential loading/confirmation, and observed A/V
ownership in the same synthetic application lifetime. The source trace now
identifies an ordinary production preflight at which a delete could queue; it
does not prove the missing native admission. A direct event/repository fixture
or a timing miss is not a conclusive PVU-003 result. No new harness or execution
recipe is authored; root retains sole build/test/Git authority.

No other PVU investigation, held runtime, helper, cache, network or execution
scope was opened. Only this new note was written, outside the frozen workload;
all STOP/NO-RETRY/FAIL-no-automatic-retry/CLOSED restrictions remain intact.

## Exact checkout-byte bindings

These SHA-256 identities bind source bytes read, not a Git tree or executed
workload. `S12` and `S09` also match the accepted prior note's full-file bindings.

| ID | Relative source path | SHA-256 |
| --- | --- | --- |
| S01 | `app-desktop/src/desktopMain/kotlin/com/passvault/desktop/backup/DesktopBackupFileStore.kt` | `ce13726b1c0c6d401c1a8d6d5a47abb18b129984eb1d205fd24a021fe94f9fd5` |
| S02 | `feature/backup/src/commonMain/kotlin/com/passvault/feature/backup/presentation/BackupFileCleanup.kt` | `829962a3eba0fcd198febed205b3b3a22a3aeddf74a4e40aedeae95e39695bb3` |
| S03 | `core/database/src/commonMain/kotlin/com/passvault/core/database/backup/VaultBackupService.kt` | `68d3e276432489d2c4efb4b854cd203a5f0f3f380565fe292c7e3b49d9be2c12` |
| S04 | `core/database/src/commonMain/kotlin/com/passvault/core/database/backup/VaultBackupV2Service.kt` | `aee2b621484150fe3807cec66d0a3ecd5eeff3eaf907099b839b084520a86bf4` |
| S05 | `core/database/src/commonMain/kotlin/com/passvault/core/database/attachment/AttachmentRepositoryImpl.kt` | `58693cb59658f4cbba4aca4c6e1cc42c7efc69b0a7aa98a9128bf91ebea3929e` |
| S06 | `core/database/src/commonMain/kotlin/com/passvault/core/database/repository/CredentialRepositoryImpl.kt` | `910fa9d999a3ef7327e3e70d6a9aa142eabf8b14131402f4415f3ef4e72f1e9f` |
| S07 | `core/database/src/commonMain/kotlin/com/passvault/core/database/repository/VaultRepositoryImpl.kt` | `aee86a3e876da0318c750c350c3ec4eb8938aa1929123533c041c3a10ec38630` |
| S08 | `feature/credential/src/commonMain/kotlin/com/passvault/feature/credential/presentation/CredentialViewModel.kt` | `b9a9c5e6a9e9a5eee86a13a639fa32826dbfdfbd09fa81a032935a37f58f0611` |
| S09 | `shared/src/commonMain/kotlin/com/passvault/shared/navigation/adapters/VaultRouteAdapters.kt` | `4a155566bd433f2905f6e13137d1b8e7b03da06a54fd9e12b706b4f7d3061b98` |
| S10 | `feature/credential/src/commonMain/kotlin/com/passvault/feature/credential/ui/CredentialDetailScreen.kt` | `19335b9bf61b5c5d31f0960ff1f822d120f083ff26dda65ad9449474f3e37c4c` |
| S11 | `core/crypto/src/commonMain/kotlin/com/passvault/core/crypto/LibsodiumCryptoEngine.kt` | `2b8dd21fe476735f16f1ab35543417a70202e7923dfac328f9064e31f5923b57` |
| S12 | `feature/backup/src/commonMain/kotlin/com/passvault/feature/backup/presentation/BackupViewModel.kt` | `afd73521b4fc0d1b06af29bdc7d4940f4646507a66fcc4e55d801f2448060e63` |
