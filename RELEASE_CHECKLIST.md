# PassVault release checklist

Do not publish an artifact while any required item is unchecked. Command evidence belongs in
[`docs/PRODUCTION_READINESS_AUDIT.md`](docs/PRODUCTION_READINESS_AUDIT.md).

## Repository gates

- [x] `.\gradlew.bat test verifyDependencies` passes in this checkout (609 tests, no failures).
- [ ] `.\gradlew.bat detekt` passes. It was skipped at the user's request; five findings remain in the existing report.
- [x] `.\gradlew.bat check -x detekt` passes without ignored failures.
- [x] `.\gradlew.bat :app-android:assembleStandardRelease` passes with R8/resource shrinking enabled.
- [ ] The release APK/AAB is signed with publisher-owned credentials and signature verification passes.
- [x] The current-host Windows Desktop package task produces EXE and MSI artifacts.
- [x] The exact Windows release image used for packaging remains running through a controlled 30-second startup
  smoke; the broken optimized 1.0.0 package was replaced by version 1.0.1.
- [ ] Desktop packages are built for every claimed OS and signed/notarized with publisher-owned credentials.
- [x] Source scans found no vault export, signing file, strong secret signature, or sensitive production log; local
  configuration and `.pvault` files are ignored. This checkout has no Git metadata with which to prove commit history.
- [x] Dependency checksum metadata is present and `verifyDependencies` passes.
- [x] All reviewed Compose/presentation and Desktop native operational text is centralized in the shared resource
  catalog; resource keys are unique and presentation errors carry typed resource identifiers instead of
  pre-rendered English. Platform launcher metadata remains in its required native resource.
- [x] Deprecated Gradle 10 source-set delegates and `kotlinx.datetime.Instant` aliases are removed; warning-mode
  configuration reports no Gradle deprecations.
- [x] Room schema-version-1 JSON is current; there is no prior released schema for a migration fixture.
- [x] Backup format compatibility and wrong-password/tamper/rollback tests pass.
- [x] No debug logging, simulated success, fake repository, unsupported product claim, or placeholder contact is in a
  production path.

## Functional verification

- [ ] Fresh launch, vault creation, failed creation, unlock, failed unlock throttling, lock, restart, background lock,
  and inactivity lock pass on Android and Desktop.
- [ ] Credential create/edit/delete, custom fields, long Unicode/RTL input, folders, tags, favorites, search, filters,
  sorting, password history, generator, and health workflows pass.
- [ ] Backup create, preview, cancel, wrong password, corrupt file, unsupported version, restore, and post-restore
  unlock pass.
- [ ] Empty, loading, error, in-flight/disabled, and large-vault states pass for each reachable screen.
- [ ] Compact/medium/expanded widths, light/dark themes, large fonts, IME/insets, mouse/keyboard focus, semantics,
  and screen-reader labels pass.
- [ ] Android screenshot protection, clipboard ownership/expiry, Storage Access Framework, rotation, and process
  recreation pass on a device/emulator.
- [ ] Desktop resize/minimize/restore, shortcuts, menu/tray, clipboard, keyring, and file dialogs pass in a graphical
  session. Automated packaged-release process startup smoke already passes.

## Security and publication

- [ ] Independent security review and penetration test are complete for the release candidate.
- [ ] A real previous release has an upgrade/migration test; otherwise the release is explicitly the first schema.
- [ ] Publisher identity, license, third-party notices, privacy terms, support address, and private vulnerability
  disclosure channel are verified.
- [ ] Android signing material and Desktop signing/notarization credentials are stored only in the publisher's secret
  infrastructure.
- [ ] Store declarations match the build: no account, cloud sync, analytics, biometric unlock, CSV export, or
  attachment-file support.
- [ ] Recovery limitations are prominent: forgotten master passwords cannot be recovered and backups require their
  independent passwords.
- [ ] The final changelog contains only verified shipped behavior.

## Current repository-external blockers

- macOS/Xcode is required for iOS validation.
- Real Android device/emulator interaction is required for instrumentation and accessibility evidence.
- A human Desktop interaction/accessibility matrix and publisher-owned signing infrastructure are required for
  release packages; automated startup and Windows packaging already pass.
- This checkout has no verified project license, owner, support contact, or independent security report.
