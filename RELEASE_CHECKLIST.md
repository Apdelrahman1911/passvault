# PassVault release checklist

Do not publish an artifact while any required item is unchecked. Command evidence belongs in
[`docs/PRODUCTION_READINESS_AUDIT.md`](docs/PRODUCTION_READINESS_AUDIT.md).

## Repository gates

- [x] `./gradlew check` passes in this checkout with no ignored failure or static-analysis baseline.
- [x] Android Desktop/JVM, Android host, and iOS simulator compilation/test tasks pass after the final source edit.
- [x] `./gradlew :app-android:assembleRelease :app-android:bundleRelease` passes with R8/resource
  shrinking enabled.
- [x] Android Debug is `PassVault Dev` / `com.passvault.android.debug`; Android Release remains `PassVault` /
  `com.passvault.android`. No testing-track flavor or F-Droid identity exists.
- [x] The Xcode scheme runs Debug as `PassVault Dev` / `com.passvault.ios.debug` and archives Release as
  `PassVault` / `com.passvault.ios`.
- [x] The next Google Play/App Store candidate uses the exact committed build `1013002`; both the legacy Testing Candidate workflow
  and Mobile Release Kit use that same `VERSION_CODE` / `CURRENT_PROJECT_VERSION`, which is above the previously distributed
  `1000033` build.
- [ ] The release APK/AAB is signed with publisher-owned credentials and signature verification passes.
- [x] The current-host Desktop package task and exact packaged-launch startup smoke pass after the final source edit.
- [ ] Desktop packages are built for every claimed OS and signed/notarized with publisher-owned credentials.
- [x] Source scans found no vault export, signing file, strong secret signature, or sensitive production log; local
  configuration, signing material, `.pvault` files, and the local-only release runbook are ignored.
- [x] Dependency checksum metadata is present and `verifyDependencies` passes.
- [x] All reviewed Compose/presentation and Desktop native operational text is centralized in the shared resource
  catalog; resource keys are unique and presentation errors carry typed resource identifiers instead of
  pre-rendered English. Platform launcher metadata remains in its required native resource.
- [x] Deprecated Gradle 10 source-set delegates and `kotlinx.datetime.Instant` aliases are removed; warning-mode
  configuration reports no Gradle deprecations.
- [x] Room schemas 1/2/3 are exported; explicit 1 -> 2 -> 3 migrations, direct supported upgrades, fresh install,
  query-plan index use, and injected-failure rollback are tested without destructive fallback.
- [x] Streaming backup format-2 and legacy format-1 compatibility, exact limits, attachment preservation,
  wrong-password/tamper/cancellation/cleanup/rollback, and large-data boundary tests pass.
- [x] English/Arabic resource and plural keys/placeholders are complete and enforced by an allowlist-aware validator.
- [x] No debug logging, simulated success, fake repository, unsupported product claim, or placeholder contact is in a
  production path.

## Functional verification

- [ ] Fresh launch, vault creation, failed creation, unlock, failed unlock throttling, lock, restart, background lock,
  and inactivity lock pass on Android and Desktop.
- [ ] Credential create/edit/delete, custom fields, long Unicode/RTL input, folders, tags, favorites, search, filters,
  sorting, password history, generator, and health workflows pass.
- [ ] Attachment select/import, metadata, multiple files, rename, preview/open, export, duplicate names, delete,
  credential cascade, source disappearance, corruption, cancellation, provider failure, size/count limits, backup,
  restore, and temporary-plaintext cleanup pass on each supported platform.
- [ ] Backup create, preview, cancel, wrong password, corrupt file, unsupported version, restore, and post-restore
  unlock pass.
- [ ] Empty, loading, error, in-flight/disabled, and large-vault states pass for each reachable screen.
- [ ] Compact/medium/expanded widths, light/dark themes, large fonts, IME/insets, mouse/keyboard focus, semantics,
  and screen-reader labels pass.
- [ ] Android screenshot protection, clipboard ownership/expiry, Storage Access Framework, rotation, and process
  recreation pass on a device/emulator.
- [ ] Desktop resize/minimize/restore, shortcuts, menu/tray, clipboard, focus/minimize concealment, and file dialogs
  pass in a graphical session. Portable Desktop screenshot prevention is not claimed. Automated packaged-release
  process startup smoke already passes.

## Security and publication

- [ ] Independent security review and penetration test are complete for the release candidate.
- [x] Every supported prior Room schema in this repository (1 and 2) upgrades non-destructively to schema 3, and a
  fresh schema-3 install matches the exported artifact.
- [ ] Publisher identity, license, third-party notices, privacy terms, support address, and private vulnerability
  disclosure channel are verified.
- [ ] Android signing material and Desktop signing/notarization credentials are stored only in the publisher's secret
  infrastructure.
- [ ] Store declarations match the build: no account, cloud sync, analytics, or CSV credential export; local encrypted
  attachment import/open/export and opt-in Android/iOS biometric unlock plus camera/biometric permissions are
  accurately declared.
- [ ] Recovery limitations are prominent: forgotten master passwords cannot be recovered and backups require their
  independent passwords.
- [ ] The final changelog contains only verified shipped behavior.

## Current repository-external blockers

- Physical Android and iOS devices are required for biometric prompt, lockout, lifecycle, process-recreation, and
  enrollment-invalidation evidence; source/host/simulator compilation cannot prove those OS behaviors.
- Real Android device/emulator interaction is required for instrumentation and accessibility evidence.
- A human Desktop interaction/accessibility matrix and publisher-owned signing infrastructure are required for
  release packages; automated startup and Windows packaging already pass.
- The Apache-2.0 license is present, but publisher identity/support/security contacts and an independent security
  report still require external verification.
