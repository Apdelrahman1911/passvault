# Eight owner design decisions — source-reviewed, decisions pending

**Remediation remains INCOMPLETE. These are the original PVD-001–008, not eight newly invented defects.**

The detailed explanations below were authored by `/root/remed_storage` and independently challenged by `/root` against actual source and platform/algorithm references. Root has accepted these **qualified explanations**, not made your policy choices, waived testing, approved a release, or certified the entire implementation. Root authored some remediation paths; this is independent of the explanation author, not a fresh independent patch review.

## Evidence and applicability

- Base commit: `0dbc12c7f1b7770e75963c751c8c67af6e8b057a`; current uncommitted **G12**, canonical source manifest SHA256 `7fd2dab3eac6269baede8a0ca550f0cbdfee20391a12dfbd9ac84cdac5401d91`.
- All48 exact retained source captures match G12 full path/size/hash/intended-mode tuples. Earlier captures are G10-labeled, but their48 identities are unchanged in G12. Root's point observations are not complete primary Git/extras or continuous preservation proof.
- Immutable complete author draft: `reviews/owner-decisions-refresh-storage-v2/DRAFT.md`, SHA256 `674c5995ac71ce3acf2a518d32bda6eb23759ee3abcd2f4e42ca9c20d6818bc5`. Authorial historical pending status remains in that artifact, not the current explanation-review status.
- Independent acceptance: `reviews/owner-decisions-independent-root-g11-v1/REPORT.json`, SHA256 `3436c71fdba1e84e7e5c0127c73280516ef66f8c767837425b303bb42aa546c6`; `PROSE-REVIEW.json` records all8 dispositions. The S01–S48 appendix below identifies cited source.
- Source/body options, recommendations and qualitative cost judgments are retained verbatim from the independently checked draft. No budget or runtime measurement is implied. **No tests ran in either explanation refresh/review.** Actual historical command/result ledgers remain execution authority.
- G12 applicability is bound in `verification/owner-decision-applicability-g12.json`: the same48 complete source tuples are unchanged. The original independent explanation/report acceptances remain G11 historical evidence; no fresh runtime test or new owner decision is inferred. PVA037 corrects separate native cleanup and does not promise universal memory erasure.
- Original8 classifications remain DESIGN LIMITATION / TRADEOFF. Your pending choices remain pending. **PVD-002 historical compatibility is REQUIRED now**, irrespective of whether you later fund redesign.

## Additional primary-reference clarification

The body's “not independently revalidated here” statements describe the author's bounded refresh. Root subsequently checked these public primary references, separately retained under `reviews/owner-decisions-independent-root-g11-v1/primary-references/INDEX.json`:

- [RFC4226 §4 R6](https://www.rfc-editor.org/rfc/rfc4226#section-4) requires at least128bit shared secrets and recommends160bits. [RFC6238 §5.1](https://www.rfc-editor.org/rfc/rfc6238#section-5.1) recommends HMAC-output-length keys. PassVault's10-byte legacy minimum is an interoperability exception, not proof of compliance with those stronger recommendations.
- The [Google-owned Key URI example](https://github.com/google/google-authenticator/wiki/Key-Uri-Format) explicitly uses a10-byte key. Only that documented example is used, not older wiki statements as proof of current Google app behavior.
- Apple's [`localOnly`](https://developer.apple.com/documentation/uikit/uipasteboard/optionskey/localonly) excludes other devices via Handoff, not other same-device apps. [`expirationDate`](https://developer.apple.com/documentation/uikit/uipasteboard/optionskey/expirationdate) is requested removal; it does not revoke another app's retained bytes or prove scheduling on a physical device.

None of these references converts a missing physical-device/provider/disk test into a pass.

## PVD-001 — What “encrypted vault” protects on disk

**Classification: DESIGN LIMITATION / TRADEOFF — not a confirmed defect.**

**Current source.** The Room database declares schema version 5. Android, Desktop and iOS builders configure `BundledSQLiteDriver`, not SQLCipher. Credential summary/secret payloads and attachment filenames/content have application-level encryption, but readable SQLite content still reveals IDs/types, relationships, favorite flags, timestamps and attachment MIME/size/path/context/format/state. App-private paths, Desktop hardening and iOS Complete-protection requests are different defenses, not encryption of those logical columns. [S01:29–48; S02:24–73,113–174; S03:25–68; S04:24–53; S05:30–53; S06:120–156]

Actual cryptographic bindings matter: credential encryption derives a `record:<id>` key and separates summary/secret purposes in associated data (AAD), with a further padded-format domain. Current managed attachment AAD binds ID, owner credential, key context, MIME, record type/index and record plaintext length; authenticated final totals are compared with the row's expected size. That is **not authentication of every visible metadata field**, nor confidentiality of that metadata. Legacy metadata-only attachment rows do not acquire managed-container guarantees. These conclusions come from implementation, not just entity comments. [S07:437–439,593–698,700–749,1297–1301; S13:105–185; S46:170–269,347–387; S47:30–96,131–132; S48:251–305,373–404,425–442]

**Why / risk.** Queryable relationships and accounting support ordinary SQLite operations and independent records/objects. Someone who obtains accessible database content can learn structure and attempt manipulation; payload authentication and validation cover their defined bindings, not arbitrary rollback or all relationships. This architectural boundary is not itself a demonstrated password-decryption bypass.

**Options and costs.**
- **Keep:** accurately describe encrypted payloads rather than an entirely application-encrypted SQLite file; retain current bindings, access/path protections and regression gates. No new migration is required, but metadata exposure and ongoing threat-model/test maintenance remain.
- **Change separately:** commission a reviewed whole-database layer (such as SQLCipher) and/or additional-metadata protection design only for an explicit threat model. Queryable encrypted fields, blind indexes and integrity records have distinct performance and leakage costs. This is **high-cost**: key availability before database open, bootstrap/key management, all-target packaging, non-destructive conversion, WAL/sidecars/recovery, disk/failure handling, backup compatibility and upgrades from every supported version. Fresh-install tests alone are insufficient. Confidentiality, tamper detection and rollback resistance require distinct decisions; whole-file encryption does not automatically provide all three. An integrity-generation design needs replay/rollback policy and long-term compatibility; no destructive fallback or unversioned conversion is proposed.

**Recommendation and specific owner decision.** Retain record encryption for the present corrective scope unless the owner requires confidentiality of the listed metadata against a copied database. Choose **A: accept this documented boundary**, or **B: require a separately authorized metadata/whole-database design and migration project before the affected release**. Specify the threat model and which of confidentiality, tamper detection and rollback resistance is required. **Owner decision: PENDING.**

**Evidence limit.** Schema/driver/encryption source was inspected; no user database, migration, file-access attack or device protection was exercised.

## PVD-002 — Historical password encoding before Argon2

**Classification: DESIGN LIMITATION / TRADEOFF — not a confirmed defect.**

**Current source.** Vault creation, password change and unwrap convert `SensitiveText` directly to strict UTF-8 bytes. `LibsodiumCryptoEngine.deriveKey` converts those bytes to **lowercase ASCII hexadecimal** before the raw native password-hash adapter. For example, bytes `41 42` become ASCII `4142`. The common helper clears its mutable temporary; Android/Desktop adapters use scoped native memory, and iOS pins byte arrays. Legacy and V2 backup writer/reader paths use the same `deriveKey` contract. [S08:162–198,413–474,675–700; S09:33–70; S10:11–84; S11:11–84; S12:11–47; S13:37–90; S14:75–77,142–242; S37:455–545; S45:99–143,377–423]

**Why / risk.** Hex is reversible encoding, not password strengthening: it adds no entropy. Its unusual form alone does not establish a cryptographic defect. Replacing it with raw UTF-8 changes derived keys for the same visible password and can strand existing vaults/backups. Password quality, KDF parameters, salts and implementation assurance are separate questions.

**Options and costs.**
- **Required now:** preserve exact encoding/case/byte order/text semantics and existing unlock/backup compatibility, while retaining owned-buffer cleanup. Documentation, pinned vectors and actual supported-backend verification are continuing costs, not a format migration.
- **Future change only:** commission a versioned KDF-input redesign only for a concrete benefit, not stylistic simplification. It needs authenticated format selection, legacy readers, post-authentication wrapping migration and old/new producer-reader, Unicode/binary-byte, interruption and recovery tests. That is **high compatibility cost** despite a small encoding diff. Changing KDF parameters and changing input encoding are separate proposals. Silent normalization, truncation or an undocumented replacement input path is not an acceptable cleanup patch.

**Required now; specific future owner decision.** Historical **UTF-8 → lowercase ASCII hex compatibility is already REQUIRED**, not a pending opt-in and not a blocker to unrelated fixes. The only pending owner choice is whether to fund a **future, separately authorized versioned redesign** with a concrete benefit and a migration/test plan. **Current compatibility: REQUIRED. Future redesign decision: PENDING.**

**Evidence limit.** Current caller/helper/adapter source and authored byte/vector assertions were inspected. S15:11–122 contains hex cleanup and production-adapter vector tests, but this refresh did not execute them or independently regenerate their external reference values.

## PVD-003 — What clearing a secret from memory can actually mean

**Classification: DESIGN LIMITATION / TRADEOFF — not a confirmed defect.**

**Current source.** `SensitiveText` owns a mutable character array; `clear()` overwrites it and `withExposed` clears its temporary copy in `finally`. UTF-8 results remain caller-owned. Ordinary `toString()` is redacted and its serializer throws, but `toStringUnsafe()`, byte decoding and encrypted-payload DTO serialization deliberately create immutable strings. Array wiping does not erase those strings. The crypto engine's `secureWipe` is `fill(0)`; native adapters clear storage they own. [S14:22–90,119–138,142–242,244–261; S07:593–680; S09:37–44; S10:27–84; S11:27–84; S12:23–47; S13:37–90,180–191,249–251]

TOTP clears application-owned key/pad/digest temporaries. Android/Desktop digest adapters reset `MessageDigest`; iOS requests clearing of its allocated CommonCrypto contexts. Neither is a proof that a provider, runtime or OS retained no internal copies. [S31:45–96; S32:11–46; S33:6–26; S34:6–26; S35:28–117]

**Why / risk.** This is a managed-runtime/platform assurance limit alongside avoidable engineering work. Wiping reduces the lifetime of a particular owned buffer. It cannot certify absence of copies in GC/runtime memory, registers, serialization/UI/IME buffers, OS services, swap or crash artifacts. A compromised OS/process is not defeated by an array-wipe claim. This limitation does not excuse missing ownership or failure-path cleanup.

**Options and costs.**
- **Keep and improve selectively:** explicit ownership, short lifetimes, redacted logging, mutable conversions where feasible and cancellation/error/lock cleanup regressions. Audit specific string/interop escapes rather than promising universal erasure. These changes ordinarily need no persistence migration if byte semantics stay unchanged.
- **Narrow native hardening:** fund selected native/off-heap operations for a defined threat model. This may reduce identified copies, but adds **medium/high** ABI, memory-safety, failure-cleanup, performance and platform-maintenance costs. A wholesale rewrite is high-risk and still cannot guarantee complete RAM/OS/IME erasure.

**Recommendation and specific owner decision.** Approve **best-effort application-owned memory cleanup** as the supported claim, with targeted copy reduction; reject “all secrets are erased from RAM.” Decide whether to fund a narrowly specified native-buffer project. If complete runtime/OS erasure is demanded, record that it is **not a supportable guarantee**, not a routine fix awaiting a completion checkbox. **Owner decision: PENDING.**

**Evidence limit.** Inspected cleanup calls are not memory-forensic, JIT, native-provider or physical-device evidence. Even a test observing one zero-filled array would prove only that array's state.

## PVD-004 — iOS clipboard usefulness versus immediate background clearing

**Classification: DESIGN LIMITATION / TRADEOFF — not a confirmed defect.**

**Current source.** iOS returns `true` for preserving sensitive clipboard data across the `Background` lock reason; Android returns `false`. The actual policy is in `PassVaultApp.kt`, not a separate policy file: it requests UI scrubbing for a lock transition, and requests clipboard clearing for non-background cleanup reasons. The navigation observer and session-bound copy helper use the same platform flag; the latter checks authorization before and after copying. These are **clear requests/attempts**, not guaranteed OS erasure. [S16:263–315; S17:23–56; S18:3–7; S19:3–8; S21:356–388; S22:49–62; S24:1–3]

`IosClipboardService` clamps requested lifetime to **5–300 seconds**, requests local-only pasteboard storage with an expiration date, installs a fallback timer and tracks `changeCount` so it does not intentionally clear someone else's newer item. The route supplies the configured timeout. Local-only does not mean “only PassVault can read it”: cross-app paste is the purpose. The configured/requested bound is not a measured guarantee under suspension or termination. [S20:33–64,77–112; S22:49–62]

**Why / risk.** Immediate clearing on every app switch can defeat the requested paste; retention leaves a window for a receiving app to obtain the secret. Clearing cannot make that app forget its own copy. Do not assume stronger native events always bypass this exception: the inspected protected-data-loss path also calls `lock(Background)`, then seeks UI acknowledgement and separately tears down the runtime. It is not a proven universal pasteboard-revocation path. [S23:140–179,246–265,528–563]

**Options and costs.**
- **Keep:** disclose this bounded cross-app-paste policy and separately verify explicit/superseding lock, protected-data loss, suspension, expiration, host teardown, overwrite ownership and failures with synthetic secrets on physical iOS.
- **Stricter policy:** request clearing on background too and/or shorten allowed timeouts. This reduces intended exposure but can break paste usability; it still cannot retract recipient data or ensure a callback runs after termination. Preference/UI/localization and race tests, plus persisted-setting compatibility, are typical costs; a vault-format migration is not normally needed.

**Recommendation and specific owner decision.** Choose **A: retain bounded iOS cross-app paste (recommended with disclosure and device gates)**, or **B: require background-clear attempts and accept usability loss**. Specify the permitted timeout/default and whether a stricter user option is required. Do not approve “preserve on every lock” or “guaranteed universal deletion.” **Owner decision: PENDING.**

**Evidence limit.** No physical paste, OS expiration, suspended timer, protected-data transition or clipboard failure was exercised. Simulator/fake/source evidence cannot settle those OS claims.

## PVD-005 — An external viewer can keep an attachment

**Classification: DESIGN LIMITATION / TRADEOFF — not a confirmed defect.**

**Current source.** The repository streams individually authenticated chunks to the supplied sink, validates authenticated final totals/expected size and EOF, and only then calls `sink.commit()`. The shared controller calls `output.present()` only after repository success and attempts to abort an unhanded output. This is **complete-container authentication before commit/presentation**, not “no plaintext sink writes until the final record,” nor universal provider atomicity. [S46:170–269,347–387; S48:251–281; S28:93–112,204–237]

The actual platform output paths differ:
- **Android:** both actions stage plaintext in an owned cache lease. After sink commit, presentation copies to the selected content-provider document or grants a FileProvider viewer URI. Cleanup is scoped to owned staging; provider copy is not a transaction the vault can undo. [S25:126–159,287–405,417–493]
- **Desktop:** export stages a temporary **beside the selected target**, then requests `ATOMIC_MOVE`/replacement at sink commit; export publication is not delayed until `present()`. Opening uses an owned preview path/lease and external viewer. Expiry/session/abandoned-preview cleanup does not reclaim deliberately exported files. [S26:98–135,202–298,313–469,583–591]
- **iOS:** plaintext is staged in app cache with Complete-protection requests, then supplied to an as-copy export picker or activity controller after commit. Output removal is attempted after presentation; the Foundation deletion calls are not proof of successful removal or recipient revocation. [S27:94–124,187–289,415–525]

The cited UI note says files are independently encrypted and authenticated before opening/exporting. It does **not** explain recipient retention. Adding an honest disclosure is a proposal, not an implemented change or new finding. [S29:61–69; S30:159–167]

**Why / risk.** External viewers/destinations need usable bytes and can retain, sync or back them up. Deleting an owned staging path, locking/deleting a vault record, or preventing future reads cannot force recipients to forget bytes already read. Unlink is not physical secure erasure. This unavoidable handoff boundary does not excuse avoidable owned plaintext orphans.

**Options and costs.**
- **Keep with explicit policy:** disclose plaintext transfer and non-revocability; decide confirmation UX, retain authentication and owned-staging cleanup/recovery gates, and treat a chosen export as deliberately external.
- **Restrict:** disable selected handoffs or offer a narrowly supported in-app viewer. This sacrifices interoperability and adds parser/rendering attack surface and maintenance; it cannot prevent screenshots or compromised-runtime copying. Encrypted-only export changes recipient requirements, not generic viewer revocation.

Warnings/preferences generally need localization/accessibility/UI work, not format migration. Owned cleanup still needs platform-specific cancellation, launch failure, lock, delayed completion, process-death/restart, provider-partial-write and concurrency evidence. A format-changing export needs separate version/recipient compatibility work.

**Recommendation and specific owner decision.** Choose **A: keep external opening/export with explicit plaintext-retention disclosure and required owned-cleanup gates (recommended for current functionality)**, or **B: restrict/disable those handoffs**, specifying alternatives. Decide whether confirmation is required per operation. “It will be deleted later” is not sufficient disclosure. **Owner decision: PENDING.**

**Evidence limit.** Staging, move, protection and cleanup calls were inspected, not executed. No OS/provider/viewer completion, effective permissions, crash durability, cleanup success or recipient behavior was demonstrated.

## PVD-006 — TOTP legacy seed compatibility

**Classification: DESIGN LIMITATION / TRADEOFF — not a confirmed defect.**

**Current source.** Enrollment and generation both accept decoded secrets of **10–128 bytes**. Algorithms are SHA-1/SHA-256/SHA-512, digits 6 or 8, periods 5–300 seconds; defaults are SHA-1, 6 digits and 30 seconds. Parsing bounds input/labels, checks Base32 length/padding/unused bits, rejects duplicate recognized URI parameters and inconsistent issuers, and validates options. These checks are separate from seed strength. [S31:21–25,39–109,112–273,277–359,442–512]

**Why / risk.** This is an issuer-owned-secret/legacy-interoperability policy. Ten bytes can contain **at most 80 bits**; length alone does not prove actual entropy. The code comments describe a deliberate interoperability exception and refer to an RFC/Authenticator example; those external texts were **not independently revalidated here**, so they are not fresh standards evidence. Do not market permissive acceptance as compliance with every seed-strength recommendation. Locally padding, hashing or replacing an issuer's shared seed changes generated codes and can lock the user out.

**Options and costs.**
- **Keep:** preserve documented 10-byte legacy-compatible acceptance, strict parsing and algorithm/time vector coverage. Encourage issuer-side re-enrollment with stronger seeds where available, without modifying a stored shared secret.
- **Tighten new enrollment only:** require a specified stronger minimum (for example 16 bytes/128-bit capacity), while continuing to read/generate/restore existing valid legacy entries. Select warning versus refusal deliberately. This can reject a legitimate issuer's QR code; only issuer-side re-enrollment can supply a new matching shared secret.

Retention has low policy-documentation/continuing-test cost. A new-only threshold has **medium** product/test cost to separate enrollment from read/restore, avoid lockouts, cover backups/validation and explain issuer re-enrollment. Any persisted marker needs explicit migration/old-reader handling. Actual supported digest backends and time boundaries still require authorized vector execution. [S32:11–66; S33:6–26; S34:6–26; S35:28–122; S36:18–52,103–151]

**Recommendation and specific owner decision.** Preserve current legacy reads/generation/restore in all cases. Choose **A: keep 10-byte acceptance for new enrollment too (recommended for the current compatibility scope)**, or **B: choose a stricter new-enrollment-only minimum and accept interoperability loss**. For B, specify the minimum, warning/refusal UX and legacy/backup policy before implementation. **Owner decision: PENDING.**

**Evidence limit.** Source thresholds and authored vector/80-bit boundary assertions were reviewed; no TOTP test, issuer enrollment, physical backend or external standards check ran in this refresh.

## PVD-007 — Restore is not one transaction across every system

**Classification: DESIGN LIMITATION / TRADEOFF — not a confirmed defect.**

**Current source.** V2 first-pass parsing authenticates the outer backup records, validates metadata, stages encrypted attachment objects, validates final counts/totals and EOF, then rewinds. Replay checks the manifest and metadata transcript inside an immediate Room transaction. The current activation callback marks the outer commit flag after the transaction returns, within its non-cancellable writer-connection block. Abandonment cleanup queries database-referenced paths and refuses deletion when that lookup fails; an in-memory flag alone is not deletion authority. This is outer-container authentication, not a claim that every nested restored payload is decrypted with the restored vault key during that pass. [S38:137–240,336–412,460–522,656–708; S45:164–244,368–423]

`VaultBackupService` wires that activation to `activateStreamingRestore`. It records the prior vault ID/enrollment state and copies the old VEK only if biometrics were enabled; `lockAndRun(Restore)` locks/revokes and requires lease settlement before the replacement block. Old biometric deletion is attempted **before** replacement. Replacement and obsolete-object cleanup run under `NonCancellable`. If deletion was attempted but replacement has not returned as committed, an available old-key copy is used for one best-effort re-enrollment attempt and is wiped in `finally`. After commit the code does not roll back the restored database to recover old biometric convenience. Cleanup failure is a separate warning. Legacy replacement uses `@Transaction`. [S08:511–538; S37:164–178,963–1021; S38:826–834; S39:221–249; S40:62–71; S30:433–436]

Current biometric admission is repository/generation-bound. A candidate used to **open a new session** is verified against current vault verification data before publication. The already-open-session branch can instead return that existing session; do not describe every successful return as fresh verification of the supplied key. These source guards are not hardware authentication evidence. [S08:280–359,702–739,796–842]

**Why / risk.** Room cannot atomically commit a separate OS key store and files. This is a cross-system recovery/availability limit, not evidence that corruption occurred. Compensation can fail or never run after process/power loss. `NonCancellable` handles coroutine cancellation, not crashes. A precommit interruption may leave the old database without usable biometric enrollment; postcommit interruption may leave the restored database plus obsolete encrypted objects. Recovery here means unlocking with the **known correct vault password** and valid database state, not a forgotten-password reset or guaranteed biometric continuity.

The backup-container password is not automatically made the restored vault's master password: replay inserts the saved vault metadata rather than rewrapping its VEK. New exports reject reuse of the current master password; the UI specifically says to unlock with the **restored vault password**. [S08:675–715; S37:181–269; S38:494–522; S45:99–143,377–423; S30:433]

**Options and costs.**
- **Keep with explicit recovery contract:** remain locked after activation, preserve recovery by unlocking with the **known correct vault password** and deliberate biometric re-enrollment, report cleanup outcomes accurately, and demonstrate failure handling at each phase with synthetic data. Failure/interruption must not be described as rollback of every external side effect.
- **Strengthen separately:** commission the already-proposed durable restore-generation/journal, idempotent restart reconciliation, staged authority switching and enrollment-retirement design. Committed authority must govern recovery; never persist an unprotected rollback key. This could improve determinism, not create one transaction spanning unrelated systems.

Keeping still requires real-Room/file and injected-store failure tests plus authorized actual-store/device checks: before/partial deletion, DB rollback, compensation failure, postcommit cleanup, cancellation and process-death/restart. A durable protocol is **high-cost**: persistent-state/schema migration where applicable, capacity, authenticated authority, old-backup compatibility and cross-platform retry/idempotency maintenance. No real user backups or enrollments should be used.

**Recommendation and specific owner decision.** Choose **A: accept documented DB transaction plus best-effort biometric compensation/recovery with the known correct vault password, subject to interruption evidence (recommended for this corrective scope)**, or **B: require a separately designed durable recovery protocol before the affected release**. Specify whether loss of biometric convenience until password re-enrollment is acceptable. Neither choice waives a data-integrity defect or an unexecuted gate. **Owner decision: PENDING.**

**Evidence limit.** Full current S08/S38 control flow and cited wrappers/codecs were re-read. No restore, key-store call, failure injection, process-death/restart or hardware recovery was executed.

## PVD-008 — Which tests can support which claims

**Classification: DESIGN LIMITATION / TRADEOFF — not a confirmed defect.**

**Current source.** `FakeCryptoEngine` explicitly says “without actual security”: its body is XOR-based and its tag/KDF use a deterministic test mixer. `FakeBiometricUnlockService` returns configured results and updates status/counters without an OS key-release operation. They support controlled application-contract/error tests, not production crypto or biometric assurance. [S41:10–19,60–96,127–205,267–307; S42:10–69]

The **current** `AttachmentImportCommitHandoffTest` uses generated Room DAO/Bundled SQLite **in memory**, with an observation-only insert wrapper and separate queued dispatcher, but fake crypto, a synthetic session and a three-byte input/memory object store. For the queued-insert cancellation scenario it deliberately accepts either no persisted row plus removed object, or a persisted row plus retained object; cancellation itself is not asserted to prove rollback. The other branches inspect the committed row before caller result delivery. Its fake `verify` result is not cryptographic assurance, and the word “durable” in test assertions is not disk-reopen/crash evidence. [S43:33–169,171–235]

`AttachmentContainerCodecCompatibilityTest` instead configures `DesktopCryptoEngine` and local synthetic files, independently constructs historical framing, and defines fragmented/empty/historical-read and reorder/tag/truncation checks. That is a different integration boundary, with no Room replacement or real biometric store. Its adapter configuration is visible; this refresh did not re-prove the adapter's native implementation or execute the fixture. [S44:21–193,196–254]

**Why / risk.** This is an assurance/evidence boundary, not an automatic production defect. Fakes provide deterministic, safe fault control; replacing all of them with providers can lose that control and add flakiness/protected-resource exposure. The danger is overstating a result. A fake pass, promising name, source count, compilation or timing sample cannot establish real persistence, native ABI correctness, constant-time behavior, hardware authentication or a physical lock/background sequence.

**Options and costs.**
- **Retain tiers:** keep focused fake contracts and separately require the real boundary for each claim: crypto vectors/tamper rejection, database migration/rollback/reopen, filesystem cleanup and platform/provider lifecycle.
- **If required evidence cannot be completed:** keep that gate open with a named owner/blocker, or defer the affected feature/candidate. Do not relabel a fake result as the missing real-boundary proof or promote questionable test prose into a newly confirmed finding.

Classification/precise names need no user-data migration. Real integrations have **medium/high recurring cost**: reproducible synthetic fixtures, controlled roots, deadlines/cancellation/cleanup, target toolchains/runners and sometimes physical-device/credential authorization. Record exact source/environment, actual executions/results, exclusions and gaps—not merely authored case counts.

**Recommendation and specific owner decision.** Approve a **tiered evidence plan and resources for each required real-boundary/platform gate**, while retaining useful fakes. Assign ownership/timing for missing checks or explicitly defer the affected candidate/feature. This distinction cannot authorize release on fake-only evidence where a real boundary is required. **Owner decision: PENDING.**

**Evidence limit.** S43 was re-read in full at its changed 257-line identity, as were the cited fake/codec-fixture implementations. **Zero tests ran in this refresh.** Root's command/result ledger, not this draft, determines actual execution status.

## Source appendix — exact retained bytes, not executed results

Physical LF ranges are inclusive. Source paths identify the captured project files; this author read only their exact named retained R captures. All captures are0600; intended source mode is0644. The root G12 tuple binding above is separately attributed. Re-inspected ranges are not all automatically claim-cited; unlisted ranges have no new authorial review claim.

| ID | Source path | Captured SHA-256 (root-bound to G12) | File LF | Re-inspected ranges | Current claim-cited ranges |
| --- | --- | --- | ---: | --- | --- |
| S01 | `core/database/src/commonMain/kotlin/com/passvault/core/database/VaultDatabase.kt` | `c727909e0b8e765ea56e8badec5ccea0d579967cabe1eb84ad57bbb2c79972d6` | 73 | 1–73 | 29–48 |
| S02 | `core/database/src/commonMain/kotlin/com/passvault/core/database/entity/CredentialRecordEntity.kt` | `064b9dc390c4069b76c65fb95df09e4e87a2701eebc7d4a55776855ff92f7222` | 239 | 1–239 | 24–73, 113–174 |
| S03 | `core/database/src/commonMain/kotlin/com/passvault/core/database/entity/AttachmentRecordEntity.kt` | `dcbd3d597ba89eedd0a5ace6f6e06bb910ef81e2b807f0be6d8450c94dc9322d` | 113 | 1–113 | 25–68 |
| S04 | `core/database/src/androidMain/kotlin/com/passvault/core/database/Database.android.kt` | `29cb20792690ce8947458e81cac3e47b789d247de0b09ea2265b5506aa23eb67` | 58 | 1–58 | 24–53 |
| S05 | `core/database/src/desktopMain/kotlin/com/passvault/core/database/Database.desktop.kt` | `2aa4d34c71de4dae66a79e6d93af187a44642e4a504ff2a592d82c60245f39a7` | 95 | 1–95 | 30–53 |
| S06 | `core/database/src/iosMain/kotlin/com/passvault/core/database/Database.ios.kt` | `60c14e6b6d1436246d11653c93116dc9f3567e318044ff140c6cad4059573d6f` | 188 | 100–170 | 120–156 |
| S07 | `core/database/src/commonMain/kotlin/com/passvault/core/database/repository/CredentialRepositoryImpl.kt` | `910fa9d999a3ef7327e3e70d6a9aa142eabf8b14131402f4415f3ef4e72f1e9f` | 1384 | 430–445, 578–754, 1285–1308 | 437–439, 593–698, 700–749, 1297–1301 |
| S08 | `core/database/src/commonMain/kotlin/com/passvault/core/database/repository/VaultRepositoryImpl.kt` | `f2fc2ff02926e62230eff5e4723b2a68998649eee5f758f5ff99df93bcf61469` | 842 | 1–842 | 162–198, 280–359, 413–474, 511–538, 675–739, 796–842 |
| S09 | `core/crypto/src/commonMain/kotlin/com/passvault/core/crypto/RawPasswordHash.kt` | `3fb16abe001752bf44b5a56039c58738ebed0e6ced5972e3ca0985d5a40586eb` | 70 | 1–70 | 33–70 |
| S10 | `core/crypto/src/androidMain/kotlin/com/passvault/core/crypto/RawPasswordHash.android.kt` | `d2c4bcd22df2cb89a97e10a4b591d651f224f0b1c5224bc6b5de10f407d0df1c` | 86 | 1–86 | 11–84 |
| S11 | `core/crypto/src/desktopMain/kotlin/com/passvault/core/crypto/RawPasswordHash.desktop.kt` | `d2c4bcd22df2cb89a97e10a4b591d651f224f0b1c5224bc6b5de10f407d0df1c` | 86 | 1–86 | 11–84 |
| S12 | `core/crypto/src/iosMain/kotlin/com/passvault/core/crypto/RawPasswordHash.ios.kt` | `3238d40ccfa77f164834ce60d15c33a8d3e5173c6669142d0fe0ee3bef396fc9` | 50 | 1–50 | 11–47 |
| S13 | `core/crypto/src/commonMain/kotlin/com/passvault/core/crypto/LibsodiumCryptoEngine.kt` | `2b8dd21fe476735f16f1ab35543417a70202e7923dfac328f9064e31f5923b57` | 336 | 1–198, 230–262 | 37–90, 105–191, 249–251 |
| S14 | `core/domain/src/commonMain/kotlin/com/passvault/core/domain/model/ValueTypes.kt` | `9935405eef7911b34cd28a5aebd367fd1592927466e6347a4a0344129000edbd` | 326 | 1–274, 285–326 | 22–90, 119–138, 142–242, 244–261 |
| S15 | `core/crypto/src/commonTest/kotlin/com/passvault/core/crypto/Argon2Test.kt` | `195fb796a4ed2a6a03cc27c6a785ebe09efd2dd977b30591f51025912e597231` | 207 | 1–145 | 11–122 |
| S16 | `shared/src/commonMain/kotlin/com/passvault/shared/PassVaultApp.kt` | `75349af0300801f0cad87f72b6afe9701e54bd24dafdd8867d7509c776506737` | 315 | 1–315 | 263–315 |
| S17 | `shared/src/commonMain/kotlin/com/passvault/shared/SessionBoundClipboard.kt` | `c5931804bc5569ca3fafac23600bb81df2b8d0906002874746a5a86183a036d0` | 56 | 1–56 | 23–56 |
| S18 | `shared/src/commonMain/kotlin/com/passvault/shared/platform/ClipboardLockPolicy.kt` | `2591126094c4109cf1730341017b46b8d0d3caa526f108bed34580ae229151f9` | 7 | 1–7 | 3–7 |
| S19 | `shared/src/iosMain/kotlin/com/passvault/shared/platform/ClipboardLockPolicy.ios.kt` | `89b80a3ec6765e98a2266ab750367b2fe19f8a6e9c760fea772f3d59fe8bac69` | 8 | 1–8 | 3–8 |
| S20 | `shared/src/iosMain/kotlin/com/passvault/shared/platform/IosClipboardService.kt` | `8f6e27772b962b6501408937655c1f672c78083268c26f487e7c011aa7f80933` | 113 | 1–113 | 33–64, 77–112 |
| S21 | `shared/src/commonMain/kotlin/com/passvault/shared/navigation/PassVaultNavigationHost.kt` | `50ec002a8b7b9e2c9324980662baff166596dddb31cef4823b4ffdfd784c9006` | 647 | 338–395, 585–647 | 356–388 |
| S22 | `shared/src/commonMain/kotlin/com/passvault/shared/navigation/RouteAdapterContext.kt` | `f9c52be05d40f827e68fb3dc6c9eaa5ad70ac594d3d989c2748022fb2d9d8ce9` | 86 | 35–70 | 49–62 |
| S23 | `shared/src/iosMain/kotlin/com/passvault/shared/IosAppLifecycleBridge.kt` | `0fea258bd697873b06b92beb6a48f8fea87b2f5b13c4ea4a08d84819a4e49278` | 563 | 112–181, 223–278, 510–563 | 140–179, 246–265, 528–563 |
| S24 | `shared/src/androidMain/kotlin/com/passvault/shared/platform/ClipboardLockPolicy.android.kt` | `03a9d43af553ed689ce283fc98e61933c141fa441a195c7fa09337bd2cd91dc4` | 3 | 1–3 | 1–3 |
| S25 | `app-android/src/main/kotlin/com/passvault/android/attachment/AndroidAttachmentFileStore.kt` | `0fcdb4e25e6e59de8172c446256bd8c53bbc4967b11cf8762c92417964cdb315` | 516 | 1–516 | 126–159, 287–405, 417–493 |
| S26 | `app-desktop/src/desktopMain/kotlin/com/passvault/desktop/attachment/DesktopAttachmentFileStore.kt` | `e395ed1180adbbc747ea4d0bb357822679dac73773c33a0b3403f3d0c9b3b406` | 606 | 1–606 | 98–135, 202–298, 313–469, 583–591 |
| S27 | `shared/src/iosMain/kotlin/com/passvault/shared/platform/IosAttachmentFileStore.kt` | `7f6a7e3efdeca79ae1410b98110ffd13160dd36a9aabd4cf046ceb5338096488` | 575 | 1–575 | 94–124, 187–289, 415–525 |
| S28 | `feature/credential/src/commonMain/kotlin/com/passvault/feature/credential/presentation/CredentialAttachmentController.kt` | `61f3d3a8b9c17baaef3d7def87919d5e831c48c6ac160ee0f6e5cb16cd92412c` | 338 | 1–338 | 93–112, 204–237 |
| S29 | `feature/credential/src/commonMain/kotlin/com/passvault/feature/credential/ui/components/CredentialAttachmentSection.kt` | `0723eb3221bf09f5d8076040736a03fd96c2e508d37023ee914037bb7fd899f4` | 320 | 45–85 | 61–69 |
| S30 | `core/designsystem/src/commonMain/composeResources/values/strings.xml` | `5605f102cca37a9fddf7fe50ecd598c5460168fd9eb6149d4d7a8318f644b118` | 653 | 150–175, 427–442 | 159–167, 433–436 |
| S31 | `core/otp/src/commonMain/kotlin/com/passvault/core/otp/TotpService.kt` | `c7fc45eb0ecfe1024cff74bbde55a14dbc0ba4ba2081cfd54bfe41e1b0f137ad` | 512 | 1–512 | 21–25, 39–109, 112–273, 277–359, 442–512 |
| S32 | `core/otp/src/commonMain/kotlin/com/passvault/core/otp/TotpHmac.kt` | `6b039724963cb7a5396879112165dcba0594db0372c1a2357b932614c44c3f17` | 66 | 1–66 | 11–66 |
| S33 | `core/otp/src/androidMain/kotlin/com/passvault/core/otp/TotpDigest.android.kt` | `b6f1d70874f500f7f0dfd52b5a8d8a56ad332bee28b0efaacb128cc05c1d6354` | 26 | 1–26 | 6–26 |
| S34 | `core/otp/src/desktopMain/kotlin/com/passvault/core/otp/TotpDigest.desktop.kt` | `b6f1d70874f500f7f0dfd52b5a8d8a56ad332bee28b0efaacb128cc05c1d6354` | 26 | 1–26 | 6–26 |
| S35 | `core/otp/src/iosMain/kotlin/com/passvault/core/otp/TotpDigest.ios.kt` | `65855ed92d1acfd36597660aec504687620f39531cfc09fd7af8a54831e8d277` | 122 | 1–122 | 28–122 |
| S36 | `core/otp/src/commonTest/kotlin/com/passvault/core/otp/TotpServiceTest.kt` | `368ca91664fa6998f7c55cc2e116a2991ac1a7a6d04a8ea8ff2faebb62c2505a` | 358 | 1–160 | 18–52, 103–151 |
| S37 | `core/database/src/commonMain/kotlin/com/passvault/core/database/backup/VaultBackupService.kt` | `68d3e276432489d2c4efb4b854cd203a5f0f3f380565fe292c7e3b49d9be2c12` | 1468 | 1–277, 300–395, 440–555, 940–1035 | 164–178, 181–269, 455–545, 963–1021 |
| S38 | `core/database/src/commonMain/kotlin/com/passvault/core/database/backup/VaultBackupV2Service.kt` | `aee2b621484150fe3807cec66d0a3ecd5eeff3eaf907099b839b084520a86bf4` | 912 | 1–912 | 137–240, 336–412, 460–522, 656–708, 826–834 |
| S39 | `core/database/src/commonMain/kotlin/com/passvault/core/database/dao/VaultBackupDao.kt` | `51813c6a5f9ff5a4ba542e5bb5043b2747066dfc68d6d3c41054de82da2e8281` | 261 | 200–261 | 221–249 |
| S40 | `core/security/src/commonMain/kotlin/com/passvault/core/security/BiometricUnlock.kt` | `9096a95f7c34e5a39ce7fa90e4098bc0a58bc52c1afdbbb47dc04edd1e6a0bed` | 111 | 1–90 | 62–71 |
| S41 | `core/testing/src/commonMain/kotlin/com/passvault/core/testing/fakes/FakeCryptoEngine.kt` | `3fd32dc7a5bf43fcbfe43694f83f4d9f5dc5efdbcee30bf7e0651f8fd07ca61e` | 341 | 1–341 | 10–19, 60–96, 127–205, 267–307 |
| S42 | `core/testing/src/commonMain/kotlin/com/passvault/core/testing/fakes/FakeBiometricUnlockService.kt` | `4a5548767504f5db39fcafdd0cb1b8f0e55b7e56edd87f775db25444f1689c72` | 70 | 1–70 | 10–69 |
| S43 | `core/database/src/desktopTest/kotlin/com/passvault/core/database/attachment/AttachmentImportCommitHandoffTest.kt` | `a3c5e1e2b9b97d8121644cdca1093de00f1733b895aea091063b4996d1c58af5` | 257 | 1–257 | 33–169, 171–235 |
| S44 | `core/database/src/desktopTest/kotlin/com/passvault/core/database/attachment/AttachmentContainerCodecCompatibilityTest.kt` | `edb55f48f5f62f79472bc2253420166209203d20d93803e80e3c63673c7463eb` | 254 | 1–254 | 21–193, 196–254 |
| S45 | `core/database/src/commonMain/kotlin/com/passvault/core/database/backup/BackupV2Codec.kt` | `997346d5ae32e1699e1939b2b1cc53664477cf9be8a3eca0e52dcb7561410445` | 508 | 1–508 | 99–143, 164–244, 368–423 |
| S46 | `core/database/src/commonMain/kotlin/com/passvault/core/database/attachment/AttachmentContainerCodec.kt` | `f17a55be52beb13ec0fd834bd0d60b7e55dea9d99a9f489ff5b6a1dd56cbb8ca` | 453 | 1–453 | 170–269, 347–387 |
| S47 | `core/crypto/src/commonMain/kotlin/com/passvault/core/crypto/PaddedPayload.kt` | `4558bc0b7e0c280fe2ec6b541d604569031e6558ef3e8dcc40008ec18db58903` | 161 | 1–161 | 30–96, 131–132 |
| S48 | `core/database/src/commonMain/kotlin/com/passvault/core/database/attachment/AttachmentRepositoryImpl.kt` | `58693cb59658f4cbba4aca4c6e1cc42c7efc69b0a7aa98a9128bf91ebea3929e` | 454 | 1–454 | 251–305, 373–404, 425–442 |

## Decision and safety record

No owner choice, application redesign, format/version/dependency/identity change, repository-setting change, signing, upload, promotion or publication has been made by this report. Existing candidate/build1017001 remains outside remediation. G7/G8 stay CLOSED; PVU007 STOP/no investigation or reformulation; PVU011 NO RETRY; PVA029 has no automatic retry. Full remediation/final-project review remains incomplete even though the eight explanation reviews are complete.
