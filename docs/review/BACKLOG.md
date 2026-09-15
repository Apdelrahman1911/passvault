# Deferred work and beta gates

**OPEN / NOT BETA READY.** The owner paused C20 expansion, not security or data-loss
gates. Source-reviewed corrections on the continuation are not automatically
selected into main or verified on the integrated tree.

## Open confirmed defects

| ID | Remaining work / required evidence | Beta applicability |
|---|---|---|
| PVA-001 | Actual supported Android32 native loading/KDF known answers; mandatory legacy strict UTF-8 → lowercase-hex create/unlock/backup compatibility, including ARM32 and minified runtime. Compilation/DEX/ELF inspection already exists, not runtime proof. | Android mandatory |
| PVA-007 | Remaining rapid/pre-frame dirty Back/tab/Add, delivered keyboard/IME, accessibility/RTL and mobile lifecycle editor contracts. Reuse accepted Main/Room and failed-Save evidence within affinity. | Android/iOS data-loss gate |
| PVA-008 | Seven prepared iOS attachment cancellation/adoption methods; applicable physical file protection/provider/lifecycle evidence. | iOS plaintext ownership gate |
| PVA-009 | Actual Android ClipboardManager unavailable-read, recopy and cleanup ownership. Injected host tests are insufficient. | Android privacy gate |
| PVA-010 | Active-native cancellation/late-return, concurrency/provider and packaged lifetime. Scoped macOS JNA and sanitizer passes exist. | Desktop-only if build/source separation is established |
| PVA-014 | Apple prompt-property check and displayed biometric prompt language/propagation on supported mobile targets. | Mobile quality gate; Desktop remainder separate |
| PVA-027 | Installed-package and remaining supported-platform tray language validation. Development Linux EN/AR/EN, Lock/Exit evidence exists. | Desktop-only |
| PVA-029 | Resolve applicability of candidate/attestation identity mismatch to the selected beta path; bounded permitted diagnosis/repair and discriminating verification. Preserve 44PASS/5FAIL. No automatic retry. | Delivery provenance gate, not Desktop-only |
| PVA-030 | Actual Android system-locale changes and lifecycle refresh, batched with PVA-009 on an isolated target. | Android quality gate |
| PVA-031 | Real input, rapid/pre-frame rejection, accessibility/RTL and mobile draft retention, shared with PVA-007. | Mobile data-loss gate |
| PVA-036 | Remaining Windows owned TMP/TEMP crash/application-cancellation cleanup. Two after-flush cases passed; overall CI cleanup failed/HOLD. | Desktop-only |
| PVA-037 | Permitted whole-caller post-PRF AAD / post-KDF AES and later allocation-cut evidence. Existing guards/limited cases insufficient. | Desktop-only |

All twelve have correction code on the continuation; their essential verification
is incomplete. Additional repair may be needed. Excluding a defect's patch from
integration does **not** make an applicable beta gate disappear.

## Other unfinished work (not confirmed defects)

- PVU-001 provider/caller lease settlement; PVU-002 durable DAO post-lock-delete
  witness; PVU-003 chooser/Home/cancel; PVU-004 iOS gesture/Back/scene and LTR/RTL
  input; PVU-005 backend lock curtain; PVU-006 Room/terminal late publication;
  PVU-008 provider content/destruction; PVU-009 native cancellation latency.
- PVU-007 **STOP**; PVU-011 **NO RETRY**. Do not investigate or reformulate them.
  PVU-010/012 retain their accepted outcomes. Original suspicions remain2/12
  conclusively resolved; the other ten are not automatically confirmed defects.
- Android framework/final-package correspondence remains incomplete after
  interrupted ACTION07, with no retry authority. Preserve source/overlay/resource/
  dependency/native-carrier and legal-attribution correspondence checks.
- Retain `PVA038_IOS_NATIVE_AEAD` **BLOCKED**: the PVA-038 closure covers
  Desktop cold-provider/routing only. Exact iOS/Kotlin-Native AEAD exception
  behavior and real authentication remain unproven; common catch/source review
  and Linux tests do not provide this mobile native evidence.
- Main uses schema5; candidate1017001 uses schema4. Applicable supported upgrade,
  failed-upgrade transaction rollback and data/ciphertext preservation evidence must cover the actual beta
  source. The selective integration has passed focused real-Room Linux upgrade,
  rollback/reopen and ciphertext-preservation regressions; final mobile artifact/
  target correspondence is still required. Keeping migrations alone is not proof.
- Final integration diff, workflow safety, regression/source affinity, manifest,
  signed artifact provenance, Store identity and processing checks remain required.
- Desktop installed image/native lifetime/legal packaging and publisher-supplied
  Debian SUPPORT_EMAIL are separate from mobile beta; no invented licensing waiver.

All Desktop-only deferrals above require demonstrated separation from the mobile
build/release graph; the label alone is not evidence of non-applicability.

## Owner decisions, separate from defects

Eight PVD choices remain: metadata/database protection; future versioned KDF
(legacy compatibility mandatory now); best-effort memory erasure; iOS clipboard/
background tradeoffs; external-viewer plaintext retention; optional new-enrollment
TOTP minimum; restore compensation versus durable recovery protocol; and supported
platform evidence ownership/resources. No silent redesign or closure-by-deferral.

## What is needed externally

- An isolated supported32-bit Android target with explicit ARM32 coverage and
  OS/API/ABI details; or an approved licensed image/runner. A64-bit-only device
  cannot satisfy this gate. Android framework tests need isolated app/clipboard
  storage; real Keystore/biometric claims need physical hardware and a tester.
- An adequately provisioned Apple-Silicon/Xcode26+/JDK17 runner and simulator for
  ten prepared software methods; an iPhone/iPad and tester for protection,
  biometrics/provider/scene/input behavior. Hosted simulator results are not
  physical security proof.
- Narrow permitted mechanisms for restricted outstanding witnesses; this does
  not reopen STOP/NO-RETRY, G7/G8 or WindowsGraph03 CLOSED scopes.
- A qualified GitHub reviewer to approve the integration PR after checks.
- Before upload: established protected signing/Store access, live build-number
  readback, and confirmation of intended Play destination if ambiguous. Repository
  config specifies internal candidate upload followed by closed `alpha` promotion;
  do not guess which stage the owner intends. iOS config still marks identity
  status `unverified`, requiring established preflight confirmation.

## Release sequence, once gates pass

1. Merge reviewed, checked integration through normal main protections.
2. Promote verified source through normal testing protections; account for
   different source SHAs by accepted ancestry/exact-tree provenance, not labels.
3. Read current Store allocations and assign a valid **new** build, never1017001.
4. Use established candidate signing/upload workflows, one heavy job at a time;
   retain compact receipts/hashes and clean owned temporary resources immediately.
5. Verify actual upload, processing and tester availability. Promote the same
   recorded binary where needed; do not rebuild to change testing tracks.

Actual beta upload is conditionally authorized, not production publication.
Store approval and tester-group availability are distinct from successful upload.
