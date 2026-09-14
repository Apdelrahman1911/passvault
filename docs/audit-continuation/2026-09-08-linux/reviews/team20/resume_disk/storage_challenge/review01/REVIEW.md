# PVU001/002 and PVA037 — independent bounded source challenge

Reviewer `/root/storage_challenge`; authors `/root/platform_review` and `/root/remaining_report`.
**ACCEPT the narrow source/reuse/blocker outcomes, with the fixture qualifications below.**
This is not acceptance of an implemented fixture, a new finding/patch, runtime evidence or execution admission.
Both PVUs remain unresolved; PVA037 remains implemented/family-open. Root alone adopts canonical changes.

## PVU001: retained guard analysis survives challenge

Current `VaultRepositoryImpl`367–383/496–515/555–670 sustains the component distinction:
lock publishes Locking, detaches/wipes the repository key and revokes/cancels leases before
waiting for T; the two-second lease timer is inside `completeLock`, after T admission.
A still-live O owner plus uncancelled T holder waiting for O is required for the unproven schedule.
`SettingsViewModel.clearForLock`504–510 cancels the password-transition job before enrollment;
Desktop enrollment cancellation actively signals the bridge and joins the structured child
(`DesktopBiometricKeyStore`80–103). Neither source ordering measures native settlement.
Linux runtime20–27 chooses unavailable before loader invocation; service54–88 rejects that
capability before enrollment leases. These are the retained C16 route-specific countercontrols,
not global unreachability. No arbitrary uncooperative fake or new provider route is justified.

Existing cooperative-lease, enrollment-cancellation, prompt-failure and synthetic hard-deadline
source controls already cover their limited contracts. PVA012's retained three in-memory Room
insert/result-handoff outcomes use fake crypto/session and a memory blob store; earlier real-file
controls are separate. Neither their counts nor ordinary delete/controller tests become new
post-lock native-input/delete proof. No successful check should be replayed for this review.

## PVU002: foreground timeout is a real source distinction, not a witnessed race

`ObserveAutoLock`637–644 calls the real repository via `lockWithBoundedRetry(AutoLock)` directly.
It does not call native `DesktopWindowProtection.lock`98–108 first. The session-title effect
only invokes native unlock for Unlocked; native menu/tray/focus-loss/terminal-shutdown branches
must not be substituted. In particular, terminal lock→database-close is not foreground timeout.
The owned-dialog focus-loss exception74–85 also prevents a blanket claim that opening a dialog
necessarily invokes native locking; actual target/focus facts would still need observation.

The actual detail adapter75–99 supplies `viewModel::onEvent`. The visible delete confirmation
requires a target present in attachment metadata and an enabled nonbusy button; router/controller
preserve credential ID, target, loaded-state and synchronous busy admission guards. Internal
delete231–249 does not lease the session, but it validates the owner-scoped row and storage kind,
commits Room deletion first and separately attempts managed-object removal.

Crucially, `PassVaultApp`193–215 has no extra synchronous post-publication input gate.
NavigationDisplay212–269 uses navigator root; root replacement occurs in the same
recomposition-keyed session effect367–425 after synchronous registered-owner clearing.
The cleanup policy285–315 covers both Locking and a conflated Locked observation. Credential
clearForLock314–336 cancels jobs and resets IDs/target/loaded state, rejecting later confirmation.
Thus there is an observation boundary before that real clear, but neither source nor this review
establishes that a newly dispatched native confirmation wins it, starts the real delete, or
commits Room before cancellation/owner clear. Same-EDT scheduling, dialog disposal and real
Room suspension/cancellation are material countercontrols, not permission to force a schedule.

### Smallest meaningful existing-fixture extension (design only)

One **separately reviewed/admitted foreground-timeout role variant** of the existing serial
seed/Main/reopen fixture is plausible without a production seam. Seed one real managed attachment,
use actual Settings minimum timeout and real dialog, and retain only passive existing
StateFlow/registry/native-target/real-DAO observations. Do not widen an admitted GUI case.

Necessary qualifications: require the observed reason **AutoLock**, not arbitrary Locked;
register ordered no-op markers around the real entry owner and bind one armed episode;
prove original dialog/control identity and newly delivered post-lock press/release, not just
Robot issuance or an AWT event on some Compose surface. Markers must never wait, do IO or
mutate application/DAO state. No held EDT/DAO/frame, prequeued Delete, direct VM/delete call,
clock replacement, retries to obtain a winning schedule or inferred admission from coordinates.
A real absent-row observation before the first clear marker can bound commit-before-clear;
a later read cannot. Fresh reopen proves durable outcome, not commit time; object outcome is
separate. Clear-first/rejected/unordered input yields only a scoped countercontrol/inconclusive
observation, never PVU closure. The current fixture has no attachment seed or this receipt chain.
This named extension is actionable test-source work only after these obligations are met,
not an already qualified positive witness or a generic new harness proposal.

## PVA037: software/permission gap, not automatically hardware-only

The earliest absent real creation cut is2001→AAD reserve810–814: caller PRF is populated,
wrapping still zero. Retrieve2236 is separate. Both populated caller arrays at AES allocations
330/383 require successful KDF and AES setup; ciphertext assign335 is not gated on successful
key generation. Later create work does not bypass earlier caller/provider admission.

Current production hooks register derivation-local PRK and bracket HMAC allocation only.
The test120–195 requires that exact live PRK pointer, not any 32-byte array; test203–263 calls
actual KDF directly. Create must cross availability, operation/cancellation, real WebAuthn
result/authenticator and inventory gates; retrieve additionally crosses metadata/assertion/
identity/signature checks. Private system-DLL pointers1263–1403 and real WinRT availability
1510–1544 expose no existing admitted caller-injection path. Copied guards/callers, direct KDF,
approximate nth-allocation failure or loader/provider replacement would not fill this obligation
within the preserved native boundary. **No minimal existing-fixture extension supplies the
missing real-caller cuts without a new, currently unadmitted seam/authority.**

Production99579B still matches the prior pin. Test75135B/CMake11398B now include the separate
PVA036 after-flush source and do not match the old48464B/9982B full-file pins. The old ledger's
three-current-files-equal sentence therefore needs qualification, not deletion of retained
results. Guard4/actual-PRK2 and one KDF1 case containing two literal vectors remain accepted
historical per-method evidence; they are neither full-current-binary proof nor caller coverage.
PVA036 after-flush and genuine Hello/device/provider obligations remain separate. No refusal
is bypassed and no new native control or production patch is proposed.

## Delivery limits

`REVIEW.json` binds all four subjects; all37 listed source/context pins matched independently,
with focused semantic reads only (not37 whole-file reviews). It also binds the extra App root/
cleanup-policy read. Zero tests/builds/helpers/imports, Git/network/native/provider/device,
runtime/HOLD/process access, canonical edits, source afterimages, new findings/fixes/closures.
Only these two compact permanent review files were created; no workers or disposable outputs
need stopping/deleting. Every STOP/NO-RETRY/CLOSED/HOLD, native/PVU008, PVD, protected-ref/tag/
version/dependency/identity/signing/Store/build1017001 boundary remains unchanged.
