# PassVault Issue Triage Summary

> Reply-aware review and final close verification performed against GitHub issues 35–146. Every disposition was
> checked against the issue, its comments, and the code. At the triage snapshot, **61 issues were closed on GitHub and
> 51 remained open**; later local remediations recorded below do not imply that their branches were pushed or their
> tracker issues were closed.

## Triage legend

- **FIX** — code/release/process evidence supports a concrete actionable defect; implement a fix and regression test.
- **DIG** — the observation is real or plausible, but runtime/hardware evidence, threat-model, legal, compatibility, or product decisions are still required.
- **CLOSE/ACCEPT** — the claimed defect is disproved, already mitigated, intentional, or only a non-security cleanup; document the rationale and close (or rewrite as an enhancement).
- Severity values in the tables are the original GitHub labels; several are intentionally narrowed in the recommendations.

## Final triage totals (after close verification)

- **FIX: 73** (44 resolved as listed below; 29 still open)
- **DIG: 8**
- **CLOSE/ACCEPT: 31**
- Critical/high findings remain urgent, but several original severities were overstated in replies (notably #37, #44, #46, and #49).

### Final verification dispositions

- **Closed with an evidence comment:** #55, #56, #66, #70, #80–#81, #97, #103, #109, #110, #111, #116, #124, #129, #131, #137, #138, #142, #145, #146.
- **Reclassified to FIX (open):** #117 (unsigned-local-release verification), #122 (pre-service temp-file ownership gap).
- **Reclassified to DIG (open):** #130 (legal/export-compliance evidence). Live App Store territory enforcement disproves its described pipeline failure, but code cannot independently establish the legal classification.
- **Reclassified to CLOSE/ACCEPT:** #80 (the user-writable Windows install premise was removed by #36), #81 (the pinned Gradle action makes PR, testing, and release refs read-only and does not cache configuration state without an encryption key).
- **Reclassified to CLOSE/ACCEPT:** #82 (Kotlin's supported Xcode integration requires this sandbox setting; the
  adjacent wrapper-validation gap is hardened and regression-tested).
- **Reclassified to CLOSE/ACCEPT:** #85 (the claimed durable POSIX file access and unprotected stock-Windows profile
  premises do not hold; atomic POSIX creation and explicit ACL-presence checks were added as defense in depth).
- **Reclassified to CLOSE/ACCEPT:** #87 (the delimiter collision exists in isolation but cannot enable the claimed
  record swap with UUID identities and independently derived per-attachment keys; v1 now rejects delimiter-bearing IDs).
- **Closed as accepted:** #146 (the Foojay resolver has no toolchain request in the repository or CI; it is dormant unused configuration, not an active defect).
- **Resolved FIX:** #36 — Windows now requires a timestamped Authenticode signature shared by the packaged launcher and biometric bridge; the installer uses a protected machine-wide location and release scripts enforce the same binding.
- **Resolved FIX:** #37 — recovery is private and can only run through the mutex-held repository boundary; DI publishes only the interface singleton, and the regression test verifies no public recovery method remains.
- **Resolved FIX:** #38 — legacy v1 envelopes are now structurally validated and Base64-decoded from an 8 KiB stream instead of being copied through a whole-file buffer and UTF-16 JSON document; its ceiling is derived from the shipped 64 MiB plaintext limit and AEAD/Base64 overhead.
- **Resolved FIX:** #39 — Desktop sensitive writes now publish all three Windows history/cloud-monitor opt-outs with zero DWORD payloads and the macOS concealed/transient pasteboard types in the same AWT transfer; ordinary clipboard writes remain unmarked and ownership-aware expiration is unchanged.
- **Resolved FIX:** #40 — failed auto-lock attempts now re-enter on a bounded timer without waiting for user activity; virtual-time tests cover unattended retries and eventual success.
- **Resolved FIX:** #41 — macOS app and bundled JVM entitlements now retain only the JIT allowance; production signing supplies a validated provisioning profile and JDK 21, while artifact verification rejects the former library-validation and unsigned-memory exceptions on every nested Mach-O. A real Developer ID-signed Temurin bundle passed nested-signature verification and launched with library validation enabled.
- **Resolved FIX:** #42 — Desktop startup obtains a process-lifetime, owner-only file lock before Koin can open the vault; a losing launch exits before constructing any vault, biometric, or session component, and lease tests cover contention and release.
- **Resolved FIX:** #43 — Desktop lock now synchronously installs the same opaque concealment curtain used at shutdown before iconifying the native window; it stays in place through asynchronous UI cleanup and is restored only on unlock or disposal.
- **Resolved FIX:** #44 — the V2 restore transfers staged-object ownership in the same non-cancellable region as the Room commit, and abort cleanup cross-checks durable attachment references before deleting; deterministic post-commit cancellation coverage preserves every live object.
- **Resolved FIX:** #45 — session operations now use tracked, revocable VEK leases rather than holding the state mutex across arbitrary suspending work. Lock publishes `Locking`, cancels the lease operation, wipes the repository key, and force-revokes any non-cooperative lease after a hard deadline; `lockAndRun` additionally refuses protected work unless lease cleanup settled.
- **Resolved FIX:** #46 — iOS backup staging directories and files now receive checked `NSFileProtectionComplete` attributes before any export write; picker copies are immediately re-protected and rejected/deleted if protection cannot be applied. Simulator regression coverage verifies ordering and fail-closed behavior; physical locked-device verification remains tracked by #35/#128.
- **Resolved FIX:** #47 — new attachment names enforce a portable Windows-safe namespace and duplicate keys model trailing-dot aliases, while legacy names remain readable and fail safely at output boundaries.
- **Resolved FIX:** #48 — credential summaries/secrets, password history, and attachment filenames now use an authenticated versioned padding format with power-of-two buckets; v2 records remain readable and are rewritten on ordinary updates.
- **Resolved FIX:** #49 — Debug and Release now use a checked-in iOS entitlement that makes `NSFileProtectionComplete` the container default and explicitly declares the per-app Keychain group; source and signed-artifact release validators enforce both properties.
- **Resolved FIX:** #50 — Room v4 removes the unused credential-title blind index without losing dependent rows; new JSON and streaming backups omit it, while authenticated legacy backup readers validate and discard the old field.
- **Resolved FIX:** #51 — contradictory attachment state/format combinations are quarantined, current-format paths remain protected and quota-accounted despite state tampering, and recovery cannot use a mutable state marker as authority to destroy a published object.
- **CLOSE/ACCEPT:** #52 — stock SQLite can read the documented credential and relationship metadata, but whole-file
  confidentiality is explicitly outside the persisted-data boundary. Current-schema allowlists now make any expansion
  of that accepted surface review-visible, and the remaining PENTEST/database-schema documentation gap is closed.
- **CLOSE/ACCEPT:** #61 — Android's GCM key already required provider randomization by platform default and encryption
  never supplied an IV; the key spec now states that invariant explicitly. StrongBox is optional capability hardening,
  not part of PassVault's OS trust boundary or a universal hardware guarantee, so the default KeyMint implementation
  remains the documented compatibility policy rather than a Medium-severity bypass.
- **CLOSE/ACCEPT:** #62 — the five-second repository delay and 30-second UI cooldown are intentionally process-local
  friction, not a durable anti-guessing boundary. Persisting attacker-modifiable counters cannot protect a copied vault
  from offline guessing and can create an owner lockout; the password policy and per-attempt Argon2id cost remain the
  applicable controls. Documentation now prevents the throttle from being represented as stronger than it is.
- **CLOSE/ACCEPT:** #67 — all recorded Gradle artifacts are pinned by SHA-256 and an induced checksum mismatch fails
  before project configuration while naming the coordinate. This is strong integrity after pinning, not publisher
  provenance at first use. Generated PGP metadata would not authenticate signer ownership; enabling it safely requires
  independently sourced fingerprints, narrow scopes, rotation/revocation policy, an unsigned-artifact inventory, and
  clean-cache coverage. The checksum-only boundary and atomic migration requirements are now explicit, and the gate
  separates disallowed PGP metadata from alternate digests rather than representing PGP as a weak hash.
- **CLOSE/ACCEPT:** #82 — Kotlin's documented direct-integration phase requires Xcode User Script Sandboxing and the
  phase's "Based on dependency analysis" option disabled. Forcing the sandbox on reproducibly fails in
  `:shared:checkSandboxAndWriteProtection`, and it could not prevent reviewed Gradle logic from replacing the framework
  that the phase exists to produce. The real adjacent gap is closed: Xcode checks the exact wrapper JAR SHA-256 before
  invoking Gradle, the iOS archive job runs Gradle's pinned wrapper-validation action, and adversarial tests reject a
  changed JAR, a missing phase check, a missing release action, or the incompatible sandbox setting.
- **CLOSE/ACCEPT:** #85 — create-then-chmod was present, but production creates and hardens an empty data root before
  Koin or SQLite can create sensitive children. On macOS, a directory descriptor opened before a later `chmod(0700)`
  could still enumerate names but `openat` was denied after search permission was revoked, disproving the claimed
  durable read path; the observed home was `0750`, not world-traversable. Windows atomically inherits the user-profile
  DACL, while administrators and policy-authorized agents remain inside the OS trust boundary. As defense in depth,
  all four current Desktop initializers now share atomic `0700` POSIX creation, repair existing modes, reject missing
  access-control support, and preserve rather than overwrite Windows SYSTEM/administrator policy; host tests cover
  new nested directories, repair, inherited ACL presence, and symlink rejection.
- **CLOSE/ACCEPT:** #87 — the v1 filename AAD byte encoding is delimiter-ambiguous in isolation, but current attachment
  and credential IDs are delimiter-free UUIDs and each filename uses an independently random key-derivation context.
  Replaying ciphertext under another row therefore selects the wrong key; copying that context also breaks the
  length-prefixed content AAD. The shared filename builder now rejects `:` in either identifier and locks the deployed
  v1 bytes with compatibility tests. A future broader identifier format must use a versioned, length-prefixed encoding
  and an explicit legacy migration rather than changing existing AAD in place.
- **Resolved FIX:** #89 — exact backup/master-password reuse was accepted even though the product described the
  secrets as separate. The impact is Low rather than Medium because a stolen local database is already an offline
  master-password verifier and a decrypted backup already exposes its snapshot. New exports now trial-unwrap the VEK,
  compare it with the active session key in constant time, reject an exact match at the service boundary, and preserve
  imports of existing backups. Candidate bytes, derived keys, and unwrapped keys retain their existing wipe paths.
- **CLOSE/ACCEPT:** #91 — the title is false for the shipped selector: it benchmarks libsodium's 2-operation/64 MiB
  profile and selects either 3 or 4 operations at 64 MiB, so capable devices receive more work rather than less.
  The fixed memory ceiling is an intentional cross-platform availability policy, not a demonstrated vulnerability;
  the 32–256 MiB vault reader range is not a writer target, and format-2 backups deliberately admit only the two
  64 MiB profiles. Raising memory remains a future enhancement requiring mobile OOM/latency evidence and a new backup
  format version, while #94 already supplies independent known-answer tests for both shipped profiles.
- **CLOSE/ACCEPT:** #108 — the latency difference is real: a Desktop probe measured about 96 ms for a wrong password
  versus less than 1 ms for malformed or missing metadata. It is not an authentication oracle: all passwords take the
  same KDF path when metadata is valid, startup already exposes vault existence, and a database attacker can inspect
  the structural state being inferred. Pre-KDF validation deliberately bounds attacker-controlled parameters and
  avoids resource amplification; a regression test now locks that ordering and the shared generic error contract.
- **CLOSE/ACCEPT:** #115 — all four fields are plaintext, but this is the already documented local metadata boundary,
  not exposure of attachment content or credential secrets. Current UI creation writes no folder icon and provides no
  tag editor; new MIME values come from a small fixed detector, while exact size is required for quotas/integrity and
  is independently inferable from unpadded object framing. Removing the unused color index alone would not conceal
  colors and still requires a schema migration. Schema tests now make every visible field and index review-explicit.
- **Resolved FIX:** #120 — under an explicit `umask 022`, the shared store reproduced `0755` on `objects/` and
  `staging/` while their `0700` parent still prevented cross-user traversal. This confirms a defense-in-depth defect,
  not the title's implied live disclosure. The Desktop factory now atomically creates or repairs each attachment child
  through the existing private-directory policy: POSIX requires `0700`, while Windows requires an inherited non-empty
  ACL without discarding system-managed entries. A host integration test covers the complete directory chain.
- **Resolved FIX:** #121 — a direct macOS native consumer reproduced `pv_bio_destroy` remaining blocked until an
  active operation completed, while the production JNA wrapper already prevented this from hanging shutdown. The
  macOS ABI now waits at most 250 milliseconds and, on timeout, transfers final context reclamation to the operation's
  scope guard instead of freeing memory still in use. Native tests cover both cancellable and committed operation
  windows; a permanently wedged OS call can retain resources only until the terminal process deadline.
- **CLOSE/ACCEPT:** #123 — the 10-byte floor is below RFC 4226 R6, but PassVault consumes issuer-owned keys and the
  published Google Authenticator Key URI example itself uses 10 bytes. Rejecting that established input would break
  interoperability without strengthening the issuer's account. The accepted 10–128-byte range, residual risk, and
  non-conformance are now explicit in source and security documentation; manual and URI boundary tests pin rejection
  below 10 bytes, acceptance at 10 bytes, and acceptance at the RFC's 16-byte boundary.
- **CLOSE/ACCEPT:** #55 — readable KDF parameters already determine the authenticated unwrap key, `entry_count` reveals no more than the accepted plaintext database structure, and exported `vault_id` values remain inside authenticated encryption; the proposed AAD change adds no independent protection.
- **Resolved FIX:** #54 and #102 — password text is encoded directly into owned mutable UTF-8 and historical lowercase-hex buffers before raw libsodium Argon2 calls on every platform, preserving existing vault/backup keys while removing the avoidable immutable KDF copies.
- **Resolved FIX:** #53 — unreadable or historically unsafe attachment names are isolated as visible quarantined rows, so valid siblings remain usable and the corrupt row can be renamed or deleted while every plaintext-producing path remains fail-closed.
- **Resolved FIX:** #57 — password-strength bands are now length-dominant with bounded variety credit and explicit common-pattern, sequence, year, and repetition penalties; corpus regressions prevent short composed passwords from outranking substantially longer unpredictable ones.
- **Resolved FIX:** #58 — Android preview plaintext now has a short lease backed by path-safe persistent `JobScheduler` deletion registered before URI disclosure, with reboot, foreground, and memory-pressure cleanup and synchronized Play permission records.
- **Resolved FIX:** #59 — lock intent is registered independently of the serialized session transition and checked atomically at the unlock commit point, so password and biometric unlocks cannot publish a session after `lock()` or `lockAndRun()` is requested.
- **Resolved FIX:** #60 — one lifecycle-owned Android biometric coordinator now provides the key-store and prompt-controller bindings, rejects queued operations, cancels on lock or host loss, and makes late or superseded callbacks inert.
- **Resolved FIX:** #63 — the shared master-password policy now applies the corrected strength floor at repository and UI boundaries without changing legacy unlock acceptance, and its sensitive-buffer path avoids immutable plaintext string materialization.
- **Resolved FIX:** #65 — iOS lock transitions now invalidate the lifecycle-owned active `LAContext`; one non-queuing key-store/controller instance rejects stale callbacks and wipes late Keychain results without changing protected-item policy or format.
- **Resolved FIX:** #68 — TOTP generation now decodes Base32 directly from mutable character buffers and uses byte-array HMAC implementations on every platform, clearing every application-owned seed and digest intermediate without introducing a long-lived decoded-key cache.
- **Resolved FIX:** #69 — format-2 backup readers and writers now admit only the two historical 64 MiB Argon2 profiles (three or four operations), rejecting attacker-only profiles before derivation while leaving legacy format-1 compatibility unchanged.
- **Resolved FIX:** #71 — format-2 restore now authenticates actual encrypted attachment bytes in metadata schema 3, preflights and rechecks attachment-volume capacity with bounded reserve, preserves schema-1/2 compatibility, maps real storage-full failures to an actionable localized error, and cleans objects published immediately before cancellation.
- **Resolved FIX:** #76 — the required CI test job now runs checksum-pinned OpenGrep rules across an exact 516-file Kotlin/Swift/native/automation inventory, proves detection with a known-bad canary, rejects analyzer errors and coverage drift, and makes every Detekt aggregate depend on a reviewed 393-file source floor. Android Lint remains a separate gate; no crash-to-success behavior was found. Detekt stays serial because no stable Detekt 2 release existed on 2026-09-03.
- **Resolved FIX:** #78 — Dependabot now schedules Gradle and Bundler updates as well as GitHub Actions, and a release-automation guard ensures the two runtime/release dependency manifests remain enrolled.
- **Resolved FIX:** #88 — `SensitiveText` no longer implies non-existent automatic clearing; a scoped temporary-copy API clears its copied characters on every exit path.
- **Resolved FIX:** #92 — review scope now correctly includes managed attachment storage and the macOS/Windows native biometric unlock bridge, with a CI-run documentation guard against the stale non-feature claims.
- **Resolved FIX:** #95 — the fixed Argon2 parallelism value, binding limitation, and fail-closed reader behavior are now documented consistently and covered by a vault-metadata regression test.
- **Resolved FIX:** #96 — V2 length-prefixed attachment IDs now decode UTF-8 strictly and wipe the intermediate bytes; malformed sequences have direct regression coverage.
- **Resolved FIX:** #100 — import source ownership is centralized in one non-cancellable outer `finally`, preventing a throwing close from triggering a second close and preserving completed encrypted imports.
- **Resolved FIX:** #104 — MIME sniffing now accumulates a bounded replayed prefix before binding the authenticated type, so short reads cannot permanently downgrade known content.
- **Resolved FIX:** #105 — attachment operations now detect a stability-scope re-entry through coroutine context and fail immediately instead of waiting on their own non-reentrant mutex; direct and credential-delete regressions verify the lock is released.
- **Resolved FIX:** #106 — QR enrollment now parses independently of manual controls, preserving URI-provided parameters and closing the scanner on every terminal result.
- **Resolved FIX:** #119 — production screenshot protection no longer exposes a runtime disable operation; the enabled state is one-way and a regression test guards the public API.
- **Resolved FIX:** #134 — sourced shell-library contracts are documented and enforced, inline workflow callers enable strict mode, and macOS keychain capture now checks the `security` command directly instead of losing its status through process substitution.
- **Resolved FIX:** #136 — every iOS `LAContext` now passes through a suspending, `finally`-backed lifecycle helper, including capability probing, enrollment, and Keychain reads; simulator tests cover success, failure, and cancellation invalidation.
- **Mitigated, verification still open:** #132 — iOS now observes protected-data transitions, keeps the native cover
  opaque, locks and scrubs the session, dismantles Compose, checkpoints/closes Room, stops Koin, and rebuilds only
  after data is available. Automated lifecycle/WAL/protection tests and Debug/Release simulator builds pass; a
  passcode-enabled physical-iPhone forced-I/O and repeated-cycle matrix is still required before closure.

The detailed tables below retain the original recommendations for traceability; this section is the authoritative final disposition.

## Genuinely false or disproved claims

- **#55** — KDF parameter changes already fail the authenticated VEK unwrap; count metadata is redundant with the accepted plaintext schema, and exported vault IDs are encrypted.
- **#56** — Compose already supplies password semantics/content type and Android `FLAG_SECURE` disables capture.
- **#66** — only 6- and 8-digit TOTP configurations are accepted; the branch is exhaustive and tested.
- **#91** — the benchmark raises the libsodium probe's operation count from two to three or four; the fixed 64 MiB
  memory target is a tested portability policy, while a 256 MiB tier has no required mobile availability evidence.
- **#108** — the measured difference identifies only already-observable vault structure, never partial password
  correctness; pre-KDF validation is an intentional resource-safety boundary rather than a defect.
- **#115** — the listed columns are inside the explicitly accepted local metadata boundary; current writers sharply
  limit visual/MIME exposure, and hiding exact size requires object padding rather than merely bucketing one column.
- **#103** — the local GOOD bucket intentionally includes domain FAIR, so UI and execution thresholds match.
- **#109** — biometric success is cryptographically verified; resetting a ≤5-second process-local delay is not an authentication bypass.
- **#110** — `lastNavigatedSession` is intentional deduplication for a non-replayed effect.
- **#111** — the already-unlocked path does not install an unverified biometric key.
- **#117** — the claimed debug-signing fallback is false; the remaining unsigned-local-release verification gap is reclassified FIX.
- **#122** — the proposed terminal-cleanup mechanism is false; a separate pre-service ownership gap is reclassified FIX.
- **#124** — `storeScreenshot` is a debug-derived, workflow-isolated screenshot artifact, not a release vulnerability.
- **#130** — live App Store/France checks disprove the described pipeline failure; legal classification evidence remains DIG.
- **#131** — readiness literals are written only after live store checks; the alleged tautological bypass is not present.
- **#138** — fresh iOS `LAContext` instances use Apple’s zero reuse default.
- **#142** — data-protection Keychain is the iOS/Catalyst default/only store; omission is not a downgrade.
- **#145** — both privacy-manifest reason codes match actual in-container and user-selected file metadata access.

GitHub vulnerability alerts are enabled. Secret scanning, push protection, and Dependabot security updates were
disabled when read through the GitHub API on 2026-09-03; enabling those external controls remains separate from the
repository fix for #76 and requires owner authorization.

## FIX NOW — critical/high findings
| Issue | Severity | Short summary | Recommended next step |
|---:|:---:|---|---|
| [36](https://github.com/Apdelrahman1911/passvault/issues/36) | High | Windows bridge integrity is self-referential: SHA-256 stored beside the DLL in a user-writable directory | The adjacent DLL/manifest check is real, but the trust root must cover the whole packaged app; use ACLs plus pinned Authenticode/signature verification. |
| [37](https://github.com/Apdelrahman1911/passvault/issues/37) | Critical | Public unsynchronized `recoverInterruptedOperations()` can mass-delete every attachment object | Recovery is public and unsynchronized, but shipped callers are serialized; make it private or mutex-owning to remove a latent mass-delete footgun (lower severity). |
| [38](https://github.com/Apdelrahman1911/passvault/issues/38) | High | Legacy v1 backup path performs ~1 GB pre-authentication allocation from a 128 MiB file | Legacy import is bounded but amplification remains pre-authentication; stream or tighten limits and test malformed user-selected backups. |
| [39](https://github.com/Apdelrahman1911/passvault/issues/39) | High | Clipboard secrets are not excluded from Windows Clipboard History / Cloud Clipboard or macOS managers | Desktop clipboard uses ordinary text only; add OS hints where possible and document that third-party history/managers cannot be fully controlled. |
| [40](https://github.com/Apdelrahman1911/passvault/issues/40) | High | Failed auto-lock stalls until the next user activity (fail-open) | Auto-lock failure waits indefinitely for activity; retry/fail closed instead of leaving the vault unlocked. |
| [41](https://github.com/Apdelrahman1911/passvault/issues/41) | High | macOS entitlements disable library validation, defeating the bridge's own Developer-ID check | macOS library-validation exceptions are real; remove them only with a signed-inside-out packaging plan and preserve bridge integrity checks. |
| [42](https://github.com/Apdelrahman1911/passvault/issues/42) | High | No single-instance lock: concurrent Desktop instances share one SQLite vault and two unlock states | Independent Desktop instances share the same vault/session files; enforce one process or an equivalent cross-process lock. |
| [43](https://github.com/Apdelrahman1911/passvault/issues/43) | High | Desktop `lock()` only iconifies; decrypted content stays in OS thumbnails and capture APIs | Lock iconifies but does not synchronously cover the rendered surface; install a privacy curtain before native window transitions. |
| [44](https://github.com/Apdelrahman1911/passvault/issues/44) | Critical | Cancellation between Room commit and the `committed` flag deletes the restored vault's attachment blobs | The commit/flag cancellation window is real but narrow; make ownership handoff atomic (lower the original Critical rating). |
| [45](https://github.com/Apdelrahman1911/passvault/issues/45) | High | `lock()` can be blocked indefinitely by a long `withUnlockedSession` block | withUnlockedSession holds the session mutex across arbitrary suspendable work; redesign leases so lock can pre-empt safely. |
| [46](https://github.com/Apdelrahman1911/passvault/issues/46) | High | Encrypted backup staging written to `Caches` with no `NSFileProtection` attribute | Caches staging lacks explicit protection, but it contains encrypted backup material rather than plaintext vault records; add protection and verify on device. |
| [49](https://github.com/Apdelrahman1911/passvault/issues/49) | High | No entitlements file, so the container default protection class is weak | Unattributed iOS files can inherit defaults, although database/attachment paths are explicitly protected; add an entitlement and archive checks (narrow scope). |

## FIX NEXT — medium/low actionable findings
| Issue | Severity | Short summary | Recommended next step |
|---:|:---:|---|---|
| [47](https://github.com/Apdelrahman1911/passvault/issues/47) | Medium | Filename policy allows Windows reserved device names and trailing dots | Reserved Win32 names and invalid trailing characters remain accepted; add platform-aware canonical filename validation. |
| [48](https://github.com/Apdelrahman1911/passvault/issues/48) | Medium | Ciphertext is unpadded: password and field lengths leak | Ciphertext length leakage is real metadata exposure; use a versioned, authenticated padding scheme if the threat model requires it. |
| [51](https://github.com/Apdelrahman1911/passvault/issues/51) | Medium | `storage_state` is unauthenticated; forced `LEGACY` downgrade denies content and skips blob deletion | storage_state is outside attachment AEAD and can cause downgrade/denial; authenticate security-relevant metadata or reconcile it safely. |
| [53](https://github.com/Apdelrahman1911/passvault/issues/53) | Medium | One undecryptable filename bricks import and rename for the entire credential | One corrupt filename aborts import/rename; quarantine/isolate bad rows without silently hiding collisions. |
| [54](https://github.com/Apdelrahman1911/passvault/issues/54) | Medium | Master password is copied into two unwipeable JVM `String`s before Argon2 | KDF paths create immutable password copies; redesign byte/character ownership while preserving legacy derivation compatibility. |
| [55](https://github.com/Apdelrahman1911/passvault/issues/55) | Medium | Argon2 parameters, `entry_count`, and `vault_id` disclosed and unauthenticated | Plaintext KDF/count metadata and tamper behavior are real; use a versioned authenticated header or encrypted metadata, not AAD-only. |
| [57](https://github.com/Apdelrahman1911/passvault/issues/57) | Medium | `PasswordStrengthEvaluator` diverges materially from true entropy | Heuristic strength scoring is materially weak, but the issue’s numeric entropy examples are inaccurate; replace wording/estimator and test corpus behavior. |
| [58](https://github.com/Apdelrahman1911/passvault/issues/58) | Medium | Attachment plaintext staged in `cacheDir` survives 10 minutes and is not cleaned on process death | Android preview plaintext can remain after process death despite startup/timer cleanup; shorten leases and clean on lock/restart. |
| [59](https://github.com/Apdelrahman1911/passvault/issues/59) | Medium | `lock()` cannot pre-empt an in-flight unlock; a session opens after lock was requested | Lock can queue behind unlock; add cancellation/session epochs even though StateFlow/navigation guards reduce the observable race. |
| [60](https://github.com/Apdelrahman1911/passvault/issues/60) | Medium | `BiometricPromptController` is wired to a no-op on Android | Android prompt cancellation is a no-op; wire lifecycle cancellation, while retaining stale-result/session-epoch checks. |
| [63](https://github.com/Apdelrahman1911/passvault/issues/63) | Medium | Master-password minimum is length-only, with no entropy floor at the authoritative boundary | Repository validation is length-only while UI heuristics are separate; enforce one reviewed policy at the authoritative boundary. |
| [64](https://github.com/Apdelrahman1911/passvault/issues/64) | Medium | Password-health analysis sprays plaintext across the heap in unwipeable `String`s | Password-health analysis creates unwipeable String/intermediate copies; reduce scope/lifetime and measure allocations without promising heap erasure. |
| [65](https://github.com/Apdelrahman1911/passvault/issues/65) | Medium | `BiometricPromptController` is wired to a no-op on iOS | iOS prompt cancellation is a no-op; invalidate active LAContext and reject stale success on lock/background. |
| [68](https://github.com/Apdelrahman1911/passvault/issues/68) | Medium | TOTP seed materialised as an unwipeable `String` on every generation, once per second | TOTP generation repeatedly materializes seed Strings; move toward byte-oriented decoding and bounded session ownership. |
| [69](https://github.com/Apdelrahman1911/passvault/issues/69) | Medium | Attacker-chosen Argon2 parameters honoured up to 256 MiB and ops 10 | Backup headers can force high Argon2 cost before authentication; cap/admit attempts with compatibility and platform budgets. |
| [71](https://github.com/Apdelrahman1911/passvault/issues/71) | Medium | Restore has no free-space precheck; up to 16 GiB staged before any DB change | Restore can stage old + new object sets without a free-space reserve; preflight and enforce a running safety bound. |
| [72](https://github.com/Apdelrahman1911/passvault/issues/72) | Medium | Unattended dispatch-to-production chain; branch promotion holds `contents: write` with no environment gate | Branch promotion still has contents:write without an environment gate, even though store signing/upload remains approval-gated; protect the mutation. |
| [73](https://github.com/Apdelrahman1911/passvault/issues/73) | Medium | Production desktop artifacts are rebuilt, not promoted byte-identically from the tested candidate | Desktop production rebuilds rather than promoting tested candidate bytes; bind provenance to the exact tested artifacts. |
| [74](https://github.com/Apdelrahman1911/passvault/issues/74) | Medium | Legacy v1 restore orphans every attachment blob on disk | Legacy restore replaces rows without reconciling blob files; remove unreferenced objects after a committed restore. |
| [75](https://github.com/Apdelrahman1911/passvault/issues/75) | Medium | Unbounded validator state: up to ~3 GB of retained identifiers | Validator identifier/reference collections are unbounded; add byte/count budgets or bounded/on-disk validation (drop the unmeasured 3 GB claim). |
| [76](https://github.com/Apdelrahman1911/passvault/issues/76) | Medium | detekt 2.0.0-alpha.5 is the only static-analysis gate, and it is not a security analyser | Detekt plus Android Lint is not a security-analysis program; add KMP/native SAST, secret scanning, dependency alerts, and failure canaries. |
| [77](https://github.com/Apdelrahman1911/passvault/issues/77) | Medium | Sensitive clipboard destroyed on every `willResignActive`, breaking paste-to-other-app | Clearing iOS clipboard on resign-active breaks copy-to-other-app; clear on lock/expiry/overwrite instead. |
| [78](https://github.com/Apdelrahman1911/passvault/issues/78) | Medium | Dependabot covers only GitHub Actions; Gradle and Ruby dependencies are never updated | The missing Gradle/Bundler update PRs are real; repository alerts may still be graph-driven, so retitle away from “no alerts”. |
| [79](https://github.com/Apdelrahman1911/passvault/issues/79) | Medium | Keychain VEK orphaned when the `NSUserDefaults` marker is lost | NSUserDefaults and Keychain enrollment can diverge; reconcile/probe both stores and make deletion idempotent. |
| [83](https://github.com/Apdelrahman1911/passvault/issues/83) | Medium | Decrypted attachment previews persist in the shared temp directory across crashes | Desktop plaintext previews have crash residue despite timers/startup sweep; add private directories, restart cleanup, and shortest practical leases. |
| [84](https://github.com/Apdelrahman1911/passvault/issues/84) | Medium | Permanent opaque-cover lockout after two consecutive cleanup failures | Two cleanup failures can leave the iOS privacy cover until restart; retain fail-closed behavior but add bounded recovery. |
| [86](https://github.com/Apdelrahman1911/passvault/issues/86) | Medium | Restored attachment objects are never verified against the vault key | Restore authenticates the outer backup but not attachment objects under the vault key; verify/quarantine before committing rows. |
| [90](https://github.com/Apdelrahman1911/passvault/issues/90) | Low | `LEGACY` rows escape quota accounting | LEGACY rows are excluded from byte quotas because they have no current blob, but they are displayed and excluded from count quotas; fix slot-count semantics separately. |
| [92](https://github.com/Apdelrahman1911/passvault/issues/92) | Medium | `PENTEST.md` states Desktop biometric unlock does not ship | Pentest scope is factually stale: Desktop biometric unlock ships; correct the document and audit scope. |
| [93](https://github.com/Apdelrahman1911/passvault/issues/93) | Medium | 30-second hardcoded unattended window on focus loss, unrelated to the user's auto-lock setting | 30 seconds is stricter than the 1-minute minimum, but focus regain resets both timers and macOS biometric suppression can be unbounded; fix the real low-severity race/timeout. |
| [94](https://github.com/Apdelrahman1911/passvault/issues/94) | Medium | Crypto marked VERIFIED while `Argon2Test` never calls Argon2 | Argon2 has integration coverage, but no independent known-answer vector; add KATs and correct VERIFIED wording. |
| [96](https://github.com/Apdelrahman1911/passvault/issues/96) | Low | `readLengthPrefixedUtf8` silently substitutes U+FFFD | Lenient UTF-8 is a real decoder inconsistency but current callers fail closed; make decoding strict as low-risk hardening. |
| [98](https://github.com/Apdelrahman1911/passvault/issues/98) | Low | `credential_records.folder_id` has no foreign key | The scalar folder_id relationship lacks a database FK; add the constraint or remove the duplicate representation. |
| [99](https://github.com/Apdelrahman1911/passvault/issues/99) | Low | Attachment content records have no chunk-count ceiling | Attachment content has no chunk-count/aggregate record bound; add a limit tied to declared object size. |
| [100](https://github.com/Apdelrahman1911/passvault/issues/100) | Low | Double `close()` of the import source when the first throws | Import cleanup may call close twice after a throwing close; make the contract idempotent and test retry behavior. |
| [101](https://github.com/Apdelrahman1911/passvault/issues/101) | Low | Pre-authentication allocation up to 65 MiB per record | A single malformed record can allocate ~65 MiB before AEAD verification; tighten per-type ceilings or stream. |
| [102](https://github.com/Apdelrahman1911/passvault/issues/102) | Low | Backup password copied into unwipeable `String`s | Backup passwords pass through immutable Strings/hex; reduce copies with versioned derivation while preserving old backups. |
| [104](https://github.com/Apdelrahman1911/passvault/issues/104) | Low | Detected MIME type depends on read granularity | MIME detection trusts the first short read; accumulate/replay a bounded prefix across reads. |
| [105](https://github.com/Apdelrahman1911/passvault/issues/105) | Low | `operationMutex` non-reentrant: `withStableAttachments` plus a repo call deadlocks | withStableAttachments holds a non-reentrant mutex across arbitrary callbacks; redesign the API to fail fast or avoid nested acquisition. |
| [106](https://github.com/Apdelrahman1911/passvault/issues/106) | Low | Valid scanned QR rejected because of an unrelated manual period field | QR URI enrollment incorrectly depends on the manual period field; separate URI/manual validation and test unrelated manual state. |
| [107](https://github.com/Apdelrahman1911/passvault/issues/107) | Low | No corruption detection, integrity check, or recovery path | No opened-database integrity/recovery policy exists; run bounded checks after open and preserve files for recovery UX. |
| [112](https://github.com/Apdelrahman1911/passvault/issues/112) | Low | Master password held as an immutable `String` in ViewModel state | Unlock/ViewModel state retains immutable password Strings; reduce lifetime and avoid persistence/logging, but do not promise total heap wiping. |
| [113](https://github.com/Apdelrahman1911/passvault/issues/113) | Low | `entry_count` desync possible; backup hard-asserts equality | Cached entry_count is normally updated transactionally but can become stale through future/out-of-band paths; derive or self-heal before backup. |
| [114](https://github.com/Apdelrahman1911/passvault/issues/114) | Low | `TwoFactorCodesViewModel.clearForLock()` has zero production callers | The missing direct caller is true, but entry-scoped ViewModels wipe on hidden/disposal; fix lock-to-entry teardown timing as a group, not this alias alone. |
| [118](https://github.com/Apdelrahman1911/passvault/issues/118) | Low | `attachment-exports` staging is plaintext with no timed cleanup | Android export staging is plaintext with weak cancellation/crash cleanup; keep private staging, authenticate first, and clean on lock/startup. |
| [125](https://github.com/Apdelrahman1911/passvault/issues/125) | Low | Cancellation race orphans decrypted seeds without wiping | A cancellation handoff can orphan decrypted TOTP seeds; make caller ownership transfer atomic (the producer already wipes on failure). |
| [126](https://github.com/Apdelrahman1911/passvault/issues/126) | Low | `dorny/test-reporter` granted `checks: write` on a PR-triggered job | PR test code runs with checks:write and persisted checkout credentials; split reporting or remove credentials before executing repository code. |
| [127](https://github.com/Apdelrahman1911/passvault/issues/127) | Low | Secrets passed as command-line arguments in Apple tooling | Apple secrets appear in argv; use supported API-key/ephemeral credential designs rather than undocumented stdin flags. |
| [134](https://github.com/Apdelrahman1911/passvault/issues/134) | Low | Three shell libraries omit `set -euo pipefail` | Sourced libraries omit strict mode and process substitution can hide command failure; add explicit status checks and failure-injection tests. |
| [136](https://github.com/Apdelrahman1911/passvault/issues/136) | Informational | `LAContext` in `getCapability()` never invalidated | LAContext capability and normal enrollment paths lack consistent invalidation; add per-operation finally cleanup (resource hygiene). |
| [139](https://github.com/Apdelrahman1911/passvault/issues/139) | Informational | 60-second SAF grace period keeps the vault unlocked in background | The 60-second grace has no screen-off/device-lock fast path and uptime timers can exceed wall time; add lifecycle revocation and device tests. |
| [141](https://github.com/Apdelrahman1911/passvault/issues/141) | Informational | `CODE_SIGN_STYLE = Automatic` in iOS Release | Automatic signing is overridden in production, but local Release remains non-reproducible; pin/fail-fast locally and verify normalized entitlements. |
| [144](https://github.com/Apdelrahman1911/passvault/issues/144) | Informational | `DesktopSystemTray` uses `invokeAndWait` from paths that may not be on the EDT | Blocking invokeAndWait plus unsynchronized tray state can deadlock/race; use nonblocking EDT dispatch or prove callers. |

## DIG — evidence or product decision required
| Issue | Severity | Short summary | Recommended next step |
|---:|:---:|---|---|
| [35](https://github.com/Apdelrahman1911/passvault/issues/35) | Verification | Complete iOS/macOS security review on Apple hardware | Apple source review is complete, but runtime/hardware evidence is still missing; keep this verification ledger open. |
| [128](https://github.com/Apdelrahman1911/passvault/issues/128) | Low | Document-picker `asCopy` intermediate sits at default protection briefly | The picker-controlled intermediate protection window needs real iOS device evidence and immediate post-receipt protection. |
| [132](https://github.com/Apdelrahman1911/passvault/issues/132) | Low | No `protectedDataWillBecomeUnavailable` handling despite `NSFileProtectionComplete` | Source mitigation and automated regression coverage are implemented; keep open until physical-device lock/forced-I/O/WAL and repeated-cycle verification proves the runtime behavior. |
| [133](https://github.com/Apdelrahman1911/passvault/issues/133) | Low | External URL opening relies on a scheme allowlist only | The title overstates the gap: HTTP(S), authority, character, and port syntax are validated. IP classes (127.0.0.1/private/link-local) are not filtered; decide if that informational hardening is needed. |
| [135](https://github.com/Apdelrahman1911/passvault/issues/135) | Low | Notices are generated, not diffed against the actual release graph | Notice checks compare repository files, not the resolved dependency graph; automate SBOM/license diffing as a release-process improvement. |
| [140](https://github.com/Apdelrahman1911/passvault/issues/140) | Informational | R8 keeps the entire libsodium binding package unshrunk | The blanket libsodium keep rule is conservative, not proven required; measure minified builds and narrow only after KAT/instrumentation tests. |
| [143](https://github.com/Apdelrahman1911/passvault/issues/143) | Informational | `AndroidAttachmentFileStore.attach()` reads shared state outside the monitor | Unsynchronized launcher reads are real but main-thread confinement is the intended invariant; enforce/assert it and test lifecycle races. |

## Original CLOSE/ACCEPT review — superseded where noted above
| Issue | Severity | Short summary | Recommended next step |
|---:|:---:|---|---|
| [50](https://github.com/Apdelrahman1911/passvault/issues/50) | Medium | `title_hash` blind index is stored and indexed but never queried | Dead title_hash/index is confirmed, but its privacy impact is overstated; remove or wire it, then close as cleanup. |
| [56](https://github.com/Apdelrahman1911/passvault/issues/56) | Medium | `SecureTextField` lacks password semantics and autofill/content-capture opt-out | The claimed semantics/autofill gap is false: Compose derives password semantics/content type and Android FLAG_SECURE disables capture. Close or merge the remaining String-churn concern with #112/#54. |
| [66](https://github.com/Apdelrahman1911/passvault/issues/66) | Medium | Digit-to-modulus mapping is a hard-coded branch, not derived from `digits` | False positive: only 6- and 8-digit TOTP configurations pass validation, and tests reject 7; the modulus branch is exhaustive. Close as maintainability. |
| [70](https://github.com/Apdelrahman1911/passvault/issues/70) | Medium | `clearVaultTables()` destroys version/migration/corruption bookkeeping that is never restored | The tables are legacy schema-only and Room bookkeeping is untouched; no current data-loss defect. Close or rewrite as future schema hygiene. |
| [80](https://github.com/Apdelrahman1911/passvault/issues/80) | Medium | Windows bridge resolves imports via default DLL search order from a user-writable directory | Already mitigated by #36: machine-wide fixed installation ACLs remove the stated write primitive, and matching timestamped Authenticode protects the launcher/bridge. The linker flag remains defense-in-depth. |
| [81](https://github.com/Apdelrahman1911/passvault/issues/81) | Medium | Gradle caching enabled in code-signing jobs with no read-only or encryption settings | False positive: pinned setup-gradle v6 defaults every non-default ref to read-only, release jobs require `release`, PR jobs use merge refs, and configuration cache is disabled without an encryption key. |
| [88](https://github.com/Apdelrahman1911/passvault/issues/88) | Low | `finalize()` on `SensitiveText` is a false assurance | finalize() is not a reliable wipe mechanism; remove the false assurance and rely on explicit clear (non-security cleanup). |
| [95](https://github.com/Apdelrahman1911/passvault/issues/95) | Low | Argon2 parallelism forced to 1, undocumented | parallelism=1 is intentional and required by the current libsodium binding; document and close. |
| [97](https://github.com/Apdelrahman1911/passvault/issues/97) | Low | Dead `DELETING` state and `updateStorageState` | DELETING/updateStorageState are dead schema/API scaffolding, not an independent tamper vulnerability; remove or implement and close as hygiene. |
| [103](https://github.com/Apdelrahman1911/passvault/issues/103) | Low | Export gate inconsistent: button needs GOOD, execution accepts FAIR | False positive: local GOOD intentionally includes domain FAIR, so UI and execution admit the same set. Close after optional parity test. |
| [109](https://github.com/Apdelrahman1911/passvault/issues/109) | Low | Biometric success resets the password failure counter | False positive as a security issue: biometric success is verified and only clears a five-second process-local delay; close unless a stronger throttle policy is adopted. |
| [110](https://github.com/Apdelrahman1911/passvault/issues/110) | Low | `clearForLock()` does not reset `lastNavigatedSession` | False positive: lastNavigatedSession intentionally deduplicates a non-replayed effect; current navigation tests support this. Close. |
| [111](https://github.com/Apdelrahman1911/passvault/issues/111) | Low | Already-unlocked short-circuit returns success for an unverified key | False positive: the already-unlocked branch does not install an unverified biometric key; new candidates are verified before publication. Close. |
| [116](https://github.com/Apdelrahman1911/passvault/issues/116) | Low | Compose `Dialog` relies on `SecureFlagPolicy.Inherit` for the QR scanner | False positive as a screenshot bypass: Dialog Inherit tracks the FLAG_SECURE parent in production; explicit SecureOn is optional hardening. Close/accept. |
| [117](https://github.com/Apdelrahman1911/passvault/issues/117) | Low | Release build silently falls back to debug signing when credentials are absent | False positive: absent release signing credentials produce an unsigned artifact, not a debug-signed Release; CI already requires and verifies signing. Close or add a local fail-fast task. |
| [119](https://github.com/Apdelrahman1911/passvault/issues/119) | Low | Screenshot-protection counter asymmetric; `disableProtection()` publicly reachable | No production path calls disableProtection(), so there is no current screenshot leak; remove the fragile public API or make it scoped, then close as preventive hardening. |
| [122](https://github.com/Apdelrahman1911/passvault/issues/122) | Low | Backup temp files leak on coroutine cancellation | False positive for coroutine cancellation: NonCancellable abort/commit handles the ownership handoff; rewrite only if crash residue is reproducibly orphaned. |
| [124](https://github.com/Apdelrahman1911/passvault/issues/124) | Low | `storeScreenshot` variant disables all protection and shares the debug applicationId | False positive as a release vulnerability: storeScreenshot is debug-derived, workflow-dispatch-only, and not uploaded to Play. Close with a publication guard. |
| [129](https://github.com/Apdelrahman1911/passvault/issues/129) | Informational | `single<Any> { Unit }` registers the root type globally in Koin | Root Any bindings are a DI workaround across platforms; replace with typed context or document/accept as non-security. |
| [130](https://github.com/Apdelrahman1911/passvault/issues/130) | Low | `ITSAppUsesNonExemptEncryption = NO` declared by a vault application | False positive as an active release failure: export-compliance status and France availability are checked against live ASC and documented; close as accepted policy, reopening on scope change. |
| [131](https://github.com/Apdelrahman1911/passvault/issues/131) | Low | Readiness manifest external-review states are asserted by `jq` | False positive as a tautological readiness bypass: live store checks run before literals are written; close or convert to evidence-fidelity enhancement. |
| [137](https://github.com/Apdelrahman1911/passvault/issues/137) | Informational | `ci.yml` has no `concurrency` block | No CI concurrency is a confirmed cost/queue issue, not a security defect; close or track as optimization. |
| [138](https://github.com/Apdelrahman1911/passvault/issues/138) | Informational | `touchIDAuthenticationAllowableReuseDuration` not explicitly zeroed on iOS | False positive mechanism: fresh iOS LAContext defaults to zero reuse; explicit zero is auditability hardening. Close/accept. |
| [142](https://github.com/Apdelrahman1911/passvault/issues/142) | Informational | `kSecUseDataProtectionKeychain` never set on iOS | False positive: Apple documents data-protection Keychain as default/only on iOS and Catalyst; explicit flag is self-documentation. Close. |
| [145](https://github.com/Apdelrahman1911/passvault/issues/145) | Informational | `PrivacyInfo.xcprivacy` over-declares FileTimestamp reason `C617.1` | False positive: C617.1 covers size/metadata inside the app container and 3B52.1 covers user-selected files; keep both manifest reasons and close. |

## Suggested execution order

1. Fix the remaining attachment/restore and session-boundary paths first (#74–#75, #77, #79, #83–#84, #86, #90, #93, #98–#99, #101, #107, #112–#114, #118, #122, #125).
2. Address the remaining CI, release, and platform defects (#72–#73, #76, #94, #117, #126–#127, #139, #141, #144).
3. Resolve every remaining DIG disposition using the required policy, runtime, legal, and platform evidence:
   #35, #128, #130,
   #132–#133, #135, #140, and #143.
4. Close accepted issues only after the corrected rationale is recorded in the threat model, release docs, or tests.
