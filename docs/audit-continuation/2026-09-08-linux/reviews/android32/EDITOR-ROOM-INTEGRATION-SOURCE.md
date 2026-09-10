# Editor → Room: one new source case

Author `/root/android32`, 2026-09-10. **SOURCE ONLY; independent review and
execution admission pending.** No compilation, test, native/SDK probe, network,
Git operation or application data access by this lane. No generated artifacts
or background workers. Feasibility `9f5cbf27…` remains unchanged.

New file: `shared/src/desktopTest/kotlin/com/passvault/shared/credential/`
`CredentialEditorRoomIntegrationTest.kt`, **one `@Test`, 712 lines/36,660 bytes**.
SHA256: `a67ba81ad875a4c611208d30b51e40caf87cf5381622f5d6ababf38d2953cff9`.
No production/dependency/API or existing-test edits. GUI02 original fixture
remains `6e505b6efc7b55bdd5485ed0786cc51e25e47c08569abcf6c82f247ef714f50a`.
Its bounded read-only accessibility/AWT targeting logic is reused as source,
not reflection/import of a private driver; there is no new generic framework.

## Exact selection and caller contract

Only class `com.passvault.shared.credential.CredentialEditorRoomIntegrationTest`
in `:shared:desktopTest`; method `native capacity draft persists through page
Save and a fresh Room database reopen`. Do not rerun the three GUI02 cases or
the49 logical cases. Root may batch this new case with separately admitted work.

Before any fixture bootstrap/Compose/AWT action, require:

- `-Dpassvault.editor.syntheticDisplay=:N[.0]` equal to `DISPLAY`, no Wayland,
  Linux, English locale, accessibility not disabled.
- `-Dpassvault.editor.roomHome=<absolute-private-home>` exactly equal to the
  worker's `-Duser.home`. Use a newly owned dedicated home, e.g.
  `workers/editor-room/home`; no nonce-basename framework or in-process switching.
  The guard requires bounded canonical ASCII path, no link components,
  process-user ownership, mode0700, initially empty directory/absent `.passvault`.
  It pins `fileKey`/owner/mode and rechecks before each bootstrap.
- Original creation/ancestor/ACL authority, private HOME/tmp/native-cache
  environment, private display, wrapper/JDK17/resource bounds and cleanup remain
  **external admission duties**, not proven by metadata or a pathname.

Absent display opt-in skips without application access; an opted-in missing/
unsafe home fails before native setup. An admitted result must have exactly one
executed case, zero skips/errors/failures; a skip or stdout `PASS` is not enough.

## Behavioral and cleanup boundary

Production Desktop bootstrap, libsodium crypto, vault/session, credential/folder
repositories and real DAOs seed49 synthetic fields. Native Add creates field50;
native row edits change its pending name/value/secret tuple. **Page** Save must
adopt that draft. After save completion, a blank-composition frame barrier,
old VM cancellation/join, session lock and successful Room checkpoint/close
precede a new bootstrap/session/repositories/VM. No reseeding. Assert distinct
Room instances, unchanged database-file identity, all50 decoded tuples/49
survivors, empty drafts/clean load, disabled Add and rendered name/masking.

Finalizers release owned input/windows/VMs, lock and close each Room generation
once; errors aggregate and no files are deleted. An uncertain close is not
retried. Reviewer-requested interruption tracking preserves a secondary
`InterruptedException` behind an earlier failure, including restored thread
status; primary/suppressed errors remain intact. External settled-worker cleanup owns the synthetic database bundle and
home. Stdout/XML receipts only, no screenshots. The240s polling deadline,
60s coroutine setup/reopen and5s/3s joins are **not hard native/NonCancellable
containment**; stalled EDT/Room/native calls require root's outer bounds/HOLD.

This adds successful encrypted-disk save/reopen at capacity, not backend
rejection/pre-frame races, NavHost Back/tab/forward, IME/a11y/RTL/mobile gestures,
physical security or arbitrary owner-disposal retention. No PVA-007/031 closure,
denominator change, recovery/STOP/NO-RETRY relaxation or hardware substitution.
