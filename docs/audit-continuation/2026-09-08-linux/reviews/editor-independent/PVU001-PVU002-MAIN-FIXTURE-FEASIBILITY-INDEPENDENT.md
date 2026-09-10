# PVU-001 / PVU-002 actual-Main feasibility — independent source challenge

Reviewer `/root/editor_review`, 2026-09-10; outcome author `/root/storage`.
**Accept the bounded missing-witness/unavailable-route outcome. Both suspicions remain
unresolved; no newly confirmed defect, qualifying fixture, global-unreachability proof or closure.**
Root owns central adoption. This does not reopen suspended demonstrations.

## Exact inputs and source scope

B = `docs/audit-continuation/2026-09-08-linux`.

- B/reviews/storage/PVU001-PVU002-MAIN-FIXTURE-NEXT-EVIDENCE.md:
  `7c8ca9608c167a64761a51e66740fe317ed7ee60d9c7922a5364c0ecb8ea959c`, 7045B/48LF; completely read.
- B/reviews/storage/REPORT.md:
  `fedae4cb95697f26e36d6da26cc3804075644096f32061c7c00c492de7f139a0`; focused PVU001/002
  sections22–88 and missing-evidence matrix152–154 retained, not a new full report audit.
- B/reviews/storage/SOURCE_BINDINGS.json:
  `1f638c61d27198c62fa434d76b5133dd1fc9e992de07b6afa96d4c526d63e684`; relevant path/range
  bindings used. A broad earlier display truncated unrelated tail metadata; no full visible
  semantic reading of that index is claimed.
- app-desktop/src/desktopTest/kotlin/com/passvault/desktop/CredentialMainNavHostRoomIntegrationTest.kt:
  `32c01afedd2db1195ea6eede8db09ec064b798f8f642f762218f56cb3f124dc5`, 40561B/755LF.

Focused additional production reads were bound to the exact hashes in the author's table:
DesktopModule24–40, DesktopBiometricRuntime (whole45-line file), BiometricUnlock78–111,
DesktopWindowProtection74–174, PassVaultDesktopWindow185–279, DesktopSessionCleanup1–62,
and CredentialAttachmentSection272–308. Also read DefaultBiometricUnlockService54–88
(`431356205bb300aeb0a7c14759eef0bbf09583d3100082e5dd0103ac731a90f5`),
SettingsViewModel426–525 (`a19baf0425234acdae2c2d923d74dbabf1e2b6a4822d923d80b9cccabfd6d723`),
and CredentialAttachmentController161–183/258–268
(`61f3d3a8b9c17baaef3d7def87919d5e831c48c6ac160ee0f6e5cb16cd92412c`).
Prior owner/session proofs are retained from the bound report, not rerun.
Current S01/S07/S08 whole-file pins and the author's critical prior ranges280–410/490–664,
275–336,24–45 were hash-guarded again. That is not an unchanged-whole-file claim or fresh
semantic coverage outside those retained proofs.

The author's ledger snapshot `031cbec176dc0925974851151aec534de8b361ac34220668989cdd511d5ff84b`
is historical context. A current-ledger guard found
`7cc63b424d1472fb5133001f7106baecc8d30eaefcd911a06dceec960f988794`/195404B and refused
before parsing. Root requested exact author/source binding rather than a new ledger audit.

## PVU-001

- Actual Desktop DI uses DesktopBiometricRuntime.create(). Linux selects unavailable before
  the loader call. The service's capability checks reject UNAVAILABLE before withUnlockedSession
  reaches enroll. Thus Settings clicks in this real Linux fixture do not supply the original
  supported biometric-enrollment lease plus queued-password-transition witness.
- This is **route-specific**, not a proof that every Linux caller or every original schedule
  is unreachable. Preserve the prior early Locking/repository-key wipe, lease-revocation and
  queued-transition cancellation countercontrols. Settings clearForLock cancels the password
  job before its biometric job; an artificially noncooperative provider does not overcome the
  missing production UI/owner admission.
- A supported production provider and a real second UI input must first establish both live
  owners despite genuine cleanup. Separate key-owner, leased synthetic array and terminal-lock
  observations are required. Hosted OS support or a mock does not become physical-provider
  proof. No such evidence was obtained here and no substitute fixture is approved.

## PVU-002

- The confirmation UI exists, but it is not an unlocked-session lease. Conversely the absence
  of a lease in internal deletion is not by itself a reachable post-lock destructive UI action.
  The controller requires credential/target IDs and loaded/nonbusy state, synchronously claims
  busy, then launches deletion. Preserve clearForLock cancellation/state reset and registered
  sensitive-owner cleanup guards; the prior secure-navigation ordering is not a new universal
  synchronous receipt for all native events or clipboard calls.
- Native Desktop lock first enters locked state, curtains/iconifies the frame and then calls
  the listener. Restore defers until the content-security acknowledgement. The listener awaits
  repository locking and UI-security coordination. This adds real caller constraints but
  does **not** prove every owned dialog, keyboard route or queued event is intercepted.
- The current Main fixture seeds attachments=emptyList(), edits custom fields and quits/reopens
  Room. It has none of the required post-lock confirmation/admission/owner-clear/DAO-order
  receipts. The existing parent snapshot correction and successful final reopen cannot provide
  those missing temporal witnesses.
- A qualifying new case needs a synthetic managed attachment, an authoritative repository
  lock boundary, a newly delivered native confirmation, real production admission and passive
  non-delaying DAO-versus-owner-clear evidence, followed by Room reopen and distinct owned-object
  outcome. Pre-lock deletion and post-clear rejection are useful controls, not positive witnesses.
  Direct VM/repository calls, prequeued Delete or held cleanup cannot substitute. A rejected event
  on one platform also cannot conclusively resolve the entire suspicion.

## Disposition and restrictions

The author's narrow conclusion and explicit Linux software next-evidence work are supported;
no small **already-admitted** extension or widening of GUI03 is established. No new product
remediation is justified merely by this feasibility outcome. Both PVUs retain UNRESOLVED /
VERIFICATION BLOCKED, the historical failed reader and rejected artificial witnesses.
Zero new cases, findings, fixes, conclusive suspicions, closures or denominator changes.

Only static reads and this sealed note; no product/fixture changes, builds/tests, Git/network,
helper execution/import, live process/runtime/SDK/cache probes, deleted-runtime access or
background workers. No temporary/generated runtime outputs were created. Preserve every STOP,
NO-RETRY/CLOSED/HOLD/consumed scope, especially PVU-007/PVU-011/PVA-029 and G7/G8, root's
Detekt/source freeze, fresh independent execution/cleanup admission, PVD/hardware and
non-publishing/build1017001 boundaries. Root remains sole build/publication owner.
