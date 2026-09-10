# PVU-001 / PVU-002 — actual-Main fixture feasibility

Author `/root/storage`, 2026-09-10. **Both remain UNRESOLVED / VERIFICATION BLOCKED.** No newly confirmed defect, patch, fixture change, execution or closure. This continues the existing report, not the suspended demonstrations. Independent challenge of this new outcome is requested from `/root/editor_review`; root owns adoption.

## Bound context and retained proofs

W=`/root/projects/PassVault/passvault-linux`; B=`W/docs/audit-continuation/2026-09-08-linux`. Paths below are relative to W unless prefixed B.

- Current `B/ISSUE_LEDGER.json`, 191683B, SHA256 `031cbec176dc0925974851151aec534de8b361ac34220668989cdd511d5ff84b`: only PVU001/002 entries adopted as context at this read.
- Prior `B/reviews/storage/REPORT.md`: `fedae4cb95697f26e36d6da26cc3804075644096f32061c7c00c492de7f139a0`; its `SOURCE_BINDINGS.json`: `1f638c61d27198c62fa434d76b5133dd1fc9e992de07b6afa96d4c526d63e684`. Preserve rejected unwired admission and historical reader refusal; no archive/helper replay.
- Original `docs/audit-handoff/current/unresolved-investigations-outcome-only.json`: `420b7eb1a419aabec37cbc323bc7d9a9b32ca160395dcc2d5f109488820eed19`, IDs001/002 only.
- `app-desktop/src/desktopTest/kotlin/com/passvault/desktop/CredentialMainNavHostRoomIntegrationTest.kt`: `32c01afedd2db1195ea6eede8db09ec064b798f8f642f762218f56cb3f124dc5`, 40561B/755LF.

Prior S02/S03/S04/S06/S09/S10/S11/S22 complete-file hashes still match. Changed whole-file identities are S01 `aee86a3e876da0318c750c350c3ec4eb8938aa1929123533c041c3a10ec38630`, S07 `b9a9c5e6a9e9a5eee86a13a639fa32826dbfdfbd09fa81a032935a37f58f0611`, S08 `b1f18f9e32beff113d0c6d26dc16fc3b9e24460ef6039a3dc144554815431f5a`. Their critical prior ranges respectively280–410/490–664,275–336,24–45 still hash-match. No whole-file unchanged claim is inferred for these three.

Additional focused source bindings (`D=app-desktop/src/desktopMain/kotlin/com/passvault/desktop`):

| Path | SHA256 |
|---|---|
| D/di/DesktopModule.kt | `7b70040fe488c9825090548b3d8ca4a2e4f34c1989b072d2571d74c9647cd253` |
| D/security/biometric/DesktopBiometricRuntime.kt | `111d03d9d074f0dc46dfcd670bcf2b3669b6a9104236fdb88287d41ad2b2e496` |
| core/security/src/commonMain/kotlin/com/passvault/core/security/BiometricUnlock.kt | `9096a95f7c34e5a39ce7fa90e4098bc0a58bc52c1afdbbb47dc04edd1e6a0bed` |
| D/PassVaultDesktopWindow.kt | `794a8fdbaea87edca986b2f729753dad3d434467f21e59ec1fcbb9027402e702` |
| core/security/src/desktopMain/kotlin/com/passvault/desktop/security/DesktopWindowProtection.kt | `92c473e21c262972983b111b61adc18610bc17fcea92c6cc2b7f65ea4d5ee128` |
| D/DesktopSessionCleanup.kt | `865184fb667e705f43b4d8ab29f5b0b04024d0abd7c0d664aed09d5b439dc5d3` |
| feature/credential/src/commonMain/kotlin/com/passvault/feature/credential/ui/components/CredentialAttachmentSection.kt | `0723eb3221bf09f5d8076040736a03fd96c2e508d37023ee914037bb7fd899f4` |

## PVU-001: identified provider route is unavailable in this Linux fixture

Real DI resolves DesktopBiometricRuntime (module32–35). Runtime20–27 maps Linux to unavailable **before invoking a native loader**; BiometricUnlock94–110 returns UNAVAILABLE. DefaultBiometricUnlockService54–88 rejects that capability before acquiring its enrollment lease. The fixture explicitly requires Linux and uses these real modules, not a provider replacement.

Adding Settings clicks therefore cannot produce the original admitted biometric-lease/queued-password-change schedule. Replacing the provider with a noncooperative fake would merely repeat the rejected artificial witness. This excludes that route in this fixture, **not every Linux caller or the PVU globally**. Existing early Locking/key wiping, cancelled queued transition and Settings cancellation-order countercontrols remain.

**Next qualifying evidence:** an actually supported production provider/UI sequence admitting the second input and preserving both T/V owners despite real cancellation/cleanup; then separately observe repository-owned key, leased synthetic array and terminal lock timing. Hosted OS availability alone is not physical-provider evidence. No such provider evidence was obtained here.

## PVU-002: no small already-admissible positive witness

The real delete confirmation exists (attachment section272–308), but controller161–183/258–262 still requires IDs/target/loaded/nonbusy state and claims busy synchronously. Credential clearForLock cancels operations and resets that state; registered owners clear before clipboard/root replacement. Internal delete still has no session lease; that observation is not upgraded to post-lock UI harm.

A straight native Lock-then-Delete extension has an additional real-caller constraint: DesktopWindowProtection98–109 conceals/iconifies the main frame **before** notifying its lock listener. The window’s native shortcut/menu/tray routes use that path; restore139–158 is deferred until content-security acknowledgement, which follows repository lock and shared UI cleanup. This is source ordering, **not proof that a glass pane intercepts every key or that all owned dialogs/OS event queues are inaccessible**.

Current fixture288–324 exercises editing/navigation, seeds no attachments (509–517), then quits and verifies Room after process exit. It has no receipt for authoritative repository lock, newly delivered delete input, live-owner clear boundary or real DAO commit ordering. Its stable-target accessibility polling and final reopen cannot substitute for those events. Calling the repository/VM directly, queuing Delete before lock, or delaying cleanup would not close the missing claim.

**Next qualifying evidence:** separately review one synthetic real-Main/managed-attachment case with passive, non-delaying order evidence for (1) authoritative Locking/Locked publication, (2) a subsequent native confirmation actually dispatched to the original UI, (3) production admission, (4) real Room commit versus owner-clear/teardown; then fresh Room reopen and separate owned-object outcome. Include pre-lock deletion and post-clear rejection controls. A late/rejected native event provides only a countercontrol, not conclusive cross-platform resolution. No material positive schedule is established here, so do not widen GUI03 or rerun successful tray/lifecycle cases.

## Activity and boundaries

Only bounded no-follow source/JSON reads, source-path discovery and this permanent note. No production/fixture edits, builds, Git, runtime/process/SDK/cache probes or cleanup. A guessed app-desktop WindowProtection filename was absent; the retained C14 manifest was used only as a path index, then the actual current core/security file was read/hash-bound. No claim rests on the missing path. Root’s forthcoming freeze and existing GUI03 scope remain intact. All STOP/NO-RETRY/HOLD/consumed/retired scopes, G7/G8, hardware/PVD and non-publishing/build1017001 limits remain. Accounting delta: **zero cases, findings, fixes, conclusive suspicions or closures**.
