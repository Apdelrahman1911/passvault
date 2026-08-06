# Production-readiness audit ledger

Last updated: 2026-07-29  
Overall state: **repository implementation and non-Detekt automation verified; user-skipped Detekt and external
publication/interactive-assurance gates remain**

This is the persistent source of truth for the audit. A code review or successful compilation is not recorded as a
manual device/visual pass.

## Status and severity

- `VERIFIED`: implementation exists and its listed automated verification passed in this checkout.
- `SOURCE`: code and states were reviewed and compiled; interactive platform verification is still required.
- `OPEN`: actionable repository work remains.
- `EXTERNAL`: requires a device, operating system, publisher asset, prior release, or independent reviewer not
  available in this checkout.
- Severity: `S0` data loss/security/release blocker, `S1` major correctness or UX, `S2` maintainability/polish.

## Project and feature inventory

| Area | Reviewed responsibility | State |
|---|---|---|
| Gradle/build logic | KMP convention plugins, catalogs, Android flavors, Desktop packaging, dependency verification, CI/release workflows | VERIFIED |
| Android host | application/activity, lifecycle, clipboard, screenshot flag, preferences, backup document picker | SOURCE |
| Desktop host | window lifecycle, keyboard/tray, clipboard, keyring, preferences, native backup file dialog | SOURCE |
| Shared host | Koin graph, database providers, theme, session bootstrap, Navigation 3 back stack | VERIFIED |
| Domain | models, typed IDs, validation, password strength/health, repository contracts | VERIFIED |
| Crypto | libsodium engine, KDF, envelopes, associated data, subkeys, memory/cancellation handling | VERIFIED |
| Database | entities, indexes, foreign keys, DAOs, transactions, encrypted repositories, schema export | VERIFIED |
| Security | biometric key release/session verification, clipboard ownership, screenshot/window protection, keyring failure policy | VERIFIED at unit/source and target-compile level; device biometric smoke open |
| Design system | light/dark semantic tokens, typography, spacing, shape, elevation, motion, feedback/form/responsive components, shared resource catalog, typed locale-safe presentation text | SOURCE |
| Onboarding | welcome, password creation/confirmation, security explanation, vault creation | VERIFIED at state/compile level |
| Unlock | password and mobile biometric unlock, failure/throttle/loading/error, lock-safe state cleanup | VERIFIED at state/repository/compile level; device smoke open |
| Vault | list/search/favorite/filter/sort, folders/tags, compact and expanded navigation | VERIFIED at state/compile level |
| Credential | detail, copy/reveal, create/edit/delete, validation, dirty-state handling, folder/tag/history/metadata preservation | VERIFIED at state/compile level |
| Generator | password/passphrase options, strength, copy/use action, saved preferences | VERIFIED at state/compile level |
| Health | weak/reused/old analysis, category lists, credential navigation | VERIFIED at state/compile level |
| Settings | security, appearance, data, local help/info; persistent theme/lock/clipboard/screenshot settings | VERIFIED at state/compile level |
| Backup | encrypted export/import, password confirmation, preview, errors, transaction restore | VERIFIED |

The empty attachment feature module was removed. Schema-version-1 attachment metadata remains only for format
preservation; attachment file creation/opening/packaging is not a feature.

## Screen and surface inventory

| ID | Surface | Normal/empty/loading/error/disabled source review | Responsive/a11y source review | State |
|---|---|---|---|---|
| UI-01 | bootstrap/theme/session decision | applicable states present | no secret route restoration | SOURCE |
| UI-02 | welcome | normal/action present | bounded compact/expanded content | SOURCE |
| UI-03 | master-password creation | validation/disabled present | secure field, IME/focus/scroll | SOURCE |
| UI-04 | password confirmation/vault create | validation/loading/error/disabled | secure field and duplicate-submit guard | VERIFIED |
| UI-05 | security explanation | normal/action present | semantic cards and bounded content | SOURCE |
| UI-06 | unlock | normal/loading/error/disabled/throttle/biometric | secure input, adjacent biometric action, IME, compact/expanded | VERIFIED |
| UI-07 | vault list/search/filter/sort | loading/empty/error/retry/list | lazy keys, compact/expanded navigation | VERIFIED |
| UI-08 | folder sidebar/tag/filter controls | empty/selected/disabled | desktop pointer and compact alternatives | SOURCE |
| UI-09 | credential detail | loading/error/content/delete confirmation | reveal/copy semantics and width bounds | VERIFIED |
| UI-10 | credential create/edit form | validation/loading/error/disabled/dirty exit | focus/IME/scroll/long-field behavior | VERIFIED |
| UI-11 | generator | output/options/error/disabled | compact/expanded panels, accessible copy | VERIFIED |
| UI-12 | password health overview/categories | loading/empty/error/content | cards/lists and constrained width | VERIFIED |
| UI-13 | settings home/info/help | content and unavailable claims removed | compact/expanded sections | SOURCE |
| UI-14 | security settings | persistent controls/explanations | toggle semantics and minimum targets | VERIFIED |
| UI-15 | appearance settings | live theme selection | selected semantics/light/dark | VERIFIED |
| UI-16 | data settings | navigation and local-only wording | bounded actions | SOURCE |
| UI-17 | backup hub/export/import | form/loading/error/preview/success/disabled | secure password, focus/scroll/bounds | VERIFIED |
| UI-18 | confirmations, menus, snackbars, tooltips | destructive/feedback paths reviewed | rounded indications/semantics reviewed | SOURCE |
| UI-19 | Android system document picker and screenshot state | callback/cancel/error source reviewed | device behavior pending | EXTERNAL |
| UI-20 | Desktop file dialog/window/tray/shortcuts | cancel/error/source reviewed; packaged release startup smoke passes | detailed graphical behavior pending | SOURCE |

Manual light/dark, RTL/Arabic, maximum font scale, tiny/large window, Android IME/rotation/process recreation, and
screen-reader sweeps remain platform gates and are not falsely marked as automated passes.

## Main journey inventory

| Journey | Result |
|---|---|
| First launch -> create password -> confirm -> create vault -> vault | state/crypto/repository tests and target compilation pass |
| Restart with existing vault -> unlock -> wrong password/throttle -> correct unlock | state and real repository crypto tests pass |
| Enable mobile biometrics -> lock -> Face ID/Touch ID/strong biometric -> vault | state and verified-candidate repository tests pass; real device prompt/invalidation matrix remains open |
| Manual/background/inactivity lock -> clear sensitive feature state -> unlock route | source/state tests pass; Android lifecycle device gate remains |
| Create/edit/favorite/delete credential with folder/tags/history | repository transaction and ViewModel tests pass |
| Search/filter/sort/empty and large lazy-list behavior | state tests/source review pass; performance device profiling remains |
| Generate/copy password -> ownership-aware expiry | generator tests and clipboard boundary review pass; platform smoke remains |
| Backup export -> read/import -> wrong password/corruption/version -> preview -> restore | backup integration tests pass |
| Settings/theme change -> persistence/restart | settings tests and platform store source review pass |
| Process termination during edit | sensitive route is not restored; repository transaction boundary protects committed data |
| Database upgrade | EXTERNAL: no earlier released schema fixture exists for version 1 |

## Resolved issue register

| ID | Sev | Problem and affected area | Root cause | Repair | Verification | State |
|---|---|---|---|---|---|---|
| SEC-001 | S0 | persisted crypto blobs lacked an unambiguous authenticated format | nonce/blob representation had no strict envelope contract | version-2 XChaCha20-Poly1305 envelope, strict parsing and contextual AAD | round-trip/wrong-key/tamper/version tests | VERIFIED |
| SEC-002 | S0 | record subkey derivation was described/used as password hashing with unsuitable semantics | password KDF and deterministic subkeys were conflated | length-prefixed purpose/record contexts with keyed BLAKE2b | domain-separation tests and source review | VERIFIED |
| SEC-003 | S0 | incomplete biometric UI/adapters could imply a security feature without VEK-bound OS cryptography | prompt success was not a reviewed key-unwrapping boundary | removed the unsafe prototype before the reviewed replacement in SEC-010 | historical Android/Desktop/shared compile and source scan | SUPERSEDED |
| SEC-004 | S0 | sensitive copy controls could bypass expiration/ownership policy | direct Compose clipboard usage | all credential/generator copy events use `ClipboardService`; clear verifies ownership | service tests/source scan | VERIFIED |
| SEC-005 | S0 | Desktop keyring fallback could create false protection | unavailable keyring behavior had a weak fallback | keyring access fails closed and is not used as vault authentication | source/tests | VERIFIED |
| SEC-006 | S0 | backup screens advertised simulated/unimplemented paths | UI preceded an integrity-safe storage format | versioned independent-password `.pvault`, bounded parser, AAD, preview and transactional restore | comprehensive backup integration suite | VERIFIED |
| SEC-007 | S0 | errors/cancellation could leak implementation details or swallow cancellation | broad exception mapping | cancellation rethrow, generic UI errors, bounded validation, buffer wipes | regression/source scan | VERIFIED |
| SEC-008 | S1 | clipboard expiry could erase content copied later by another app | delayed clear did not prove current ownership | random ownership token/value comparison | service logic tests/source review | VERIFIED |
| SEC-009 | S1 | incomplete Android biometric permission remained after the old feature deletion | manifest residue | permission was removed until the reviewed SEC-010 implementation restored it with a real adapter | historical manifest/source scan and Android compile | SUPERSEDED |
| SEC-010 | S0 | mobile convenience unlock must not persist a password or trust prompt success alone | biometric auth needs a cryptographic path to the existing VEK | Android auth-per-use Keystore wrapping and iOS device-only `biometryCurrentSet` Keychain storage; candidate VEK authenticates the vault verification record before session publication | unlock ViewModel tests, real Room/libsodium wrong-key tests, Android compile, iOS simulator compile | VERIFIED at source/test/compile level; physical-device matrix OPEN |
| DATA-001 | S0 | credential payload/relationships/history could partially update | multi-DAO writes lacked one transaction | Room transaction boundaries and explicit reference validation | repository integration tests | VERIFIED |
| DATA-002 | S0 | restore could destroy the current vault before validation | import/replace stages were not separated | read/authenticate/decode/validate before one replacement transaction | corrupt/wrong/rollback tests | VERIFIED |
| DATA-003 | S1 | folder column and compatibility cross-reference could diverge | two relationship representations were written independently | synchronize both in the repository transaction | relationship tests | VERIFIED |
| DATA-004 | S1 | folder/tag duplicates, missing references, and cycles were under-validated | validation split across UI and DAO | repository-level normalized duplicate, existence, parent, and cycle checks | repository tests | VERIFIED |
| DATA-005 | S1 | entry counts/history/favorite state could become inconsistent | mutation side effects were incomplete | transactional count/history/update handling | integration tests | VERIFIED |
| STATE-001 | S0 | rapid actions could duplicate navigation or writes | unguarded coroutine launches/effects | operation mutex/jobs, admission checks, single effect handling | ViewModel rapid-event tests | VERIFIED |
| STATE-002 | S1 | duplicate screens/ViewModels/navigation graphs could diverge | prototypes remained reachable or compilable | consolidated one feature state machine and one Navigation 3 host | route/source scan and both target compiles | VERIFIED |
| STATE-003 | S1 | sensitive editor/generator state survived lock or route changes longer than needed | singleton ViewModel state was not explicitly cleared | centralized exit/lock cleanup and job cancellation | ViewModel tests/source review | VERIFIED |
| STATE-004 | S0 | returning from the credential editor crashed on Android 10 with `SnapshotStateList.removeLast()` `NoSuchMethodError` | Kotlin/JVM could bind `removeLast()` to a newer Java list method that is absent on older Android runtimes | navigation now removes the final route through the min-SDK-compatible `removeAt(lastIndex)` operation; all equivalent source calls were audited | shared Android/Desktop compilation, full non-Detekt gate, physical-device process/Logcat monitor | VERIFIED |
| UIX-001 | S1 | primary screens had inconsistent spacing/colors/shapes and mobile layouts stretched on Desktop | scattered styling and no shared responsive rules | semantic tokens, bounded content, compact/expanded scaffolds and consistent feedback components | target compilation/source review | SOURCE |
| UIX-002 | S1 | missing loading/empty/error/disabled and retry behavior | happy-path-only surfaces | feature-specific states and shared feedback components | ViewModel tests/source review | VERIFIED |
| UIX-003 | S1 | forms lacked robust IME, validation, dirty-exit, and double-submit behavior | local focus/submit state | scrollable secure forms, focus order, visible validation, discard confirmation, guards | state tests/source review | SOURCE |
| UIX-004 | S1 | text/resources advertised cloud/CSV/attachments/support that did not ship | aspirational copy | removed unsupported UI/resources and rewrote capability documents | repository claim scan | VERIFIED |
| UIX-005 | S1 | user-facing strings and validation/errors bypassed localization, including Desktop native menus/tray/dialogs | literals and pre-rendered English were carried through UI state | centralized 539-key Compose resource catalog; `UiText` resource identifiers/arguments remain unresolved until the UI boundary; plurals and Desktop native labels migrated | duplicate-key XML validation, whole-source literal scans, feature tests, Android/Desktop/iOS compilation | VERIFIED |
| BUILD-001 | S0 | root `test` could succeed without KMP Desktop suites | ambiguous Gradle task selection | explicit aggregate dependency graph | root test execution | VERIFIED |
| BUILD-002 | S0 | CI/release used wrong flavor tasks and could publish unsigned/mis-versioned artifacts | generic tasks and job-local version edits | exact tasks, semantic version props, mandatory Android signing, unsigned Desktop artifacts only | workflow/source review, Android release/R8, and current-host Desktop packaging | VERIFIED |
| BUILD-003 | S1 | dependency policy task only printed text | no machine-enforced metadata check | committed SHA-256 verification metadata plus validating task | `verifyDependencies help` | VERIFIED |
| BUILD-004 | S1 | shared tests were not exercised against Android host compilations | KMP Android host-test source sets were not enabled and the aggregate only selected Desktop/JVM tasks | enabled host tests in all shared modules and added all 17 suites to the root aggregate | 299 Android host tests pass | VERIFIED |
| BUILD-005 | S1 | Android release lint/resource packaging found obsolete resources, invalid backup XML, a missing receiver, and adaptive-icon/orientation issues | prototype resources and manifest entries remained after feature changes | removed dead resources/receiver, added valid backup exclusions and monochrome icons, and corrected manifest configuration | `check -x detekt`, lint, R8, and Standard release assembly pass | VERIFIED |
| BUILD-006 | S1 | Android release targeted an older SDK and used an indirect/outdated core KTX version | release catalog had not been reconciled with the installed toolchain | target SDK 37 and direct core KTX 1.19.0 dependency | Android debug/release compilation and lint | VERIFIED |
| BUILD-007 | S2 | Gradle Kotlin DSL source-set delegates were scheduled for removal in Gradle 10 and domain/database code retained deprecated `kotlinx.datetime.Instant` aliases | older DSL and time imports remained across module boundaries | all source sets use `getByName`; all domain, database, backup, and test time types use `kotlin.time.Instant` | `help --warning-mode all`, 609 tests, non-Detekt `check`, release builds, iOS compile | VERIFIED |
| BUILD-008 | S0 | the packaged Windows release exited immediately with JVM startup errors even though `:app-desktop:run` passed | Compose Desktop ProGuard stripped Room reflection targets and SQLite JNI methods, rewrote incompatible Navigation bytecode, and removed JNA callbacks; the Desktop Koin graph also exposed `WindowProtection` but the window requested its concrete implementation | corrected concrete/interface Koin registration with a regression test, disabled the incompatible Desktop ProGuard transform, retained jlink/jpackage and Android R8, advanced the replacement package to 1.0.1, and made release scripts/workflows use release tasks plus a packaged-launch guard | exact `main-release` launcher remained alive for 30 seconds after a clean image rebuild; smoke script, final EXE/MSI packaging, and full gate pass | VERIFIED |
| BUILD-009 | S1 | the combined release gate completed its work but failed while storing Gradle's configuration cache | `verifyDependencies` captured a Kotlin build-script object in a `doLast` closure | replaced the closure with a typed task whose metadata file is an annotated `RegularFileProperty` input | consecutive `verifyDependencies` runs store and reuse configuration cache; combined gate passes | VERIFIED |
| BUILD-010 | S0 | the Android APK compiled and installed but crashed on its first localized screen with `MissingResourceException` | AGP's KMP Android library target disables Android resources/assets by default, so the design-system Compose resource bundle was never merged into the application APK | enabled Android resources in `core:designsystem` and added a typed APK-content verification task to Android `check` | required `strings.commonMain.cvr` entry is asserted in the APK; physical Android 10 cold start remained foregrounded with no fatal/resource exceptions | VERIFIED |
| PORT-001 | S1 | common tests contained JVM-only `String.codePointCount` and `System.currentTimeMillis` calls | JVM APIs leaked into portable source sets | multiplatform Unicode counter plus `Clock.System`; Room-only DAO test scoped to Desktop | common tests, Android host tests, and iOS simulator compilation | VERIFIED |
| TEST-001 | S1 | Android host crypto integration tests could not load libsodium on Windows | Android AAR packages device `.so` files but the host runner needs the JVM native resource | JVM libsodium artifact added to Android host-test runtime only | 119 crypto host tests pass | VERIFIED |
| DOC-001 | S1 | architecture/security/schema/release documents described nonexistent behavior and certifications | design documents were treated as implementation evidence | replaced with source-derived contracts and explicit evidence limits | claim scan | VERIFIED |
| DEAD-001 | S2 | empty attachment feature module and duplicate route files remained in build | abandoned scaffolding | module removed; metadata schema retained for compatibility | Gradle configuration/compile | VERIFIED |
| DEAD-002 | S2 | Desktop tray exposed unreachable notification/icon/visibility helpers | prototype public API had no production or test consumers | removed unused helpers and made active tray text an explicit localized setup contract | whole-repository reference scan and Desktop compile/startup smoke | VERIFIED |
| CLEAN-001 | S2 | generated compiler/test logs accumulated at repository root | diagnostic output was redirected into source-tree files during earlier repairs | removed 52 temporary log/text artifacts and ignored `.pvault` exports | final repository scan | VERIFIED |

## Remaining gates

| ID | Sev | Required work | Completion evidence | State |
|---|---|---|---|---|
| GATE-001 | S0 | root tests, non-Detekt `check`, dependency policy, Android release/R8, current-host Desktop package, and exact packaged-launch startup after the last source edit | successful command log below | VERIFIED |
| GATE-002 | S1 | remaining Android interactive lifecycle, process recreation, IME, rotation, picker cancellation/permissions, screenshot, clipboard, accessibility/font/RTL sweep; install and cold-start Logcat smoke now pass on a physical Android 10 device | completed emulator/device matrix with results | EXTERNAL |
| GATE-003 | S1 | Desktop interactive resize/minimize/focus/keyboard/tray/clipboard/keyring/file-dialog/light-dark/accessibility sweep | graphical Windows/Linux/macOS matrix | EXTERNAL |
| GATE-004 | S1 | prior-release migration/upgrade test | actual supported earlier schema/database fixture | EXTERNAL |
| GATE-005 | S0 | release identity and trust | publisher license/owner/contact, Android signing, Desktop signing/notarization/update policy | EXTERNAL |
| GATE-006 | S0 | independent security assurance | third-party cryptographic review and penetration-test report | EXTERNAL |
| GATE-007 | S1 | future iOS runtime | macOS/Xcode compile, platform services, lifecycle/keychain/UI tests | EXTERNAL |
| GATE-008 | S2 | Gradle 10 Kotlin DSL and `kotlinx.datetime.Instant` deprecation migration | warning-mode configuration and full regression | VERIFIED |
| GATE-009 | S2 | Detekt was explicitly skipped by the user; the existing report contains one `ReturnCount`, two `TooManyFunctions`, and two `TooGenericExceptionCaught` findings in Android adapters | a later accepted `gradlew detekt` run with all five findings resolved | OPEN |
| GATE-010 | S1 | mobile biometric runtime behavior | physical Android and iPhone tests for success, cancel, lockout, backgrounding, process recreation, and enrollment invalidation | OPEN |

`GATE-008` was resolved without warning suppression. The only warning left by `help --warning-mode all` is that an
iOS simulator test cannot run on a Windows host; compile-only iOS simulator tasks pass.

`GATE-009` is also not represented as a pass. The affected files are `AndroidBackupFileStore.kt`,
`AndroidClipboardService.kt`, `AndroidScreenshotProtection.kt`, and `AndroidAppSettingsStore.kt`. Detekt was excluded
only because the user explicitly requested it; no baseline, ignored failure, or suppression was added for those five
findings. The new biometric adapter has a local suppression documenting its intentionally broad fail-closed OEM
exception boundary and cohesive platform-adapter function count; it adds no Detekt finding.

Android lint has one productivity warning recommending the KTX `SharedPreferences.edit` helper. The checked
`SharedPreferences.Editor.commit()` call is intentionally retained because the store must observe and report a
synchronous persistence failure; the KTX helper discards that Boolean result.

## Verification command log

| Date | Command | Result |
|---|---|---|
| 2026-07-28 | `gradlew :core:crypto:desktopTest` | PASS |
| 2026-07-28 | `gradlew :core:database:desktopTest` | PASS |
| 2026-07-28 | `gradlew test` | PASS: 608 tests, comprising 309 Desktop/JVM and 299 Android host tests; 0 failed, errored, or skipped |
| 2026-07-28 | `gradlew :core:crypto:testAndroidHostTest` | PASS: real libsodium host runtime repaired; 119 tests |
| 2026-07-28 | `gradlew :core:designsystem:compileKotlinDesktop :feature:backup:compileKotlinDesktop :feature:health:compileKotlinDesktop :feature:credential:compileKotlinDesktop :feature:vault:compileKotlinDesktop :app-desktop:compileKotlinDesktop` | PASS |
| 2026-07-28 | `gradlew :app-android:assembleStandardDebug` | PASS |
| 2026-07-28 | `gradlew verifyDependencies help` | PASS |
| 2026-07-28 | `gradlew :feature:onboarding:compileKotlinDesktop :feature:unlock:desktopTest :feature:settings:compileKotlinDesktop :shared:compileKotlinDesktop :app-desktop:compileKotlinDesktop` | PASS |
| 2026-07-28 | `gradlew :app-android:compileStandardDebugKotlin` | PASS |
| 2026-07-28 | `gradlew check -x detekt` | PASS, including Android lint |
| 2026-07-28 | `gradlew :app-android:assembleStandardRelease` | PASS with R8/resource shrinking; unsigned release APK generated |
| 2026-07-28 | `gradlew :app-desktop:packageReleaseDistributionForCurrentOS -Ppassvault.versionName=1.0.0` | PACKAGE CREATION ONLY; later exact-launch testing found the optimized runtime was broken and the 1.0.0 artifacts were withdrawn |
| 2026-07-28 | `gradlew test verifyDependencies :app-android:assembleStandardRelease :app-desktop:packageReleaseDistributionForCurrentOS -Ppassvault.versionName=1.0.0` | BUILD PASS ONLY; it did not execute the packaged launcher and therefore did not detect BUILD-008 |
| 2026-07-28 | `gradlew :feature:credential:desktopTest :feature:credential:testAndroidHostTest` | PASS after typed credential validation/error localization |
| 2026-07-28 | `gradlew :core:database:desktopTest :core:database:testAndroidHostTest :feature:backup:desktopTest :feature:backup:testAndroidHostTest` | PASS after `kotlin.time.Instant` migration |
| 2026-07-28 | `gradlew :core:designsystem:compileKotlinIosSimulatorArm64 :shared:compileKotlinIosSimulatorArm64` | PASS; compile-only future-iOS compatibility |
| 2026-07-28 | `gradlew help --warning-mode all` | PASS; no Gradle deprecations remain, only the expected Windows-host iOS simulator runtime notice |
| 2026-07-28 | `gradlew :app-android:lintStandardRelease` | PASS with one intentional `UseKtx` productivity warning for checked `SharedPreferences.commit()` |
| 2026-07-28 | `gradlew test check -x detekt verifyDependencies :core:designsystem:compileKotlinIosSimulatorArm64 :shared:compileKotlinIosSimulatorArm64 :app-android:assembleStandardRelease :app-android:lintStandardRelease :app-desktop:packageReleaseDistributionForCurrentOS -Ppassvault.versionName=1.0.0` | BUILD PASS ONLY; packaged-launch coverage was still missing |
| 2026-07-28 | controlled `gradlew :app-desktop:run` startup smoke | DEVELOPMENT-RUNTIME PASS; later shown insufficient to verify the packaged release |
| 2026-07-28 | source scan for TODO/FIXME/HACK, sensitive logging, resource bypasses, and strong secret signatures | PASS; no unfinished markers, sensitive output, untranslated prose, or secret signatures; the Desktop headless-start diagnostic contains no sensitive data |
| 2026-07-29 | exact optimized `main-release` launcher and packaged-classpath diagnostics | FAIL reproduced: missing Room implementation, missing concrete Koin binding, stripped SQLite/JNA runtime contracts, and Navigation `VerifyError`; 1.0.0 withdrawn |
| 2026-07-29 | `gradlew :app-desktop:desktopTest` | PASS: new Desktop Koin binding regression resolves both the concrete and interface window-protection contracts |
| 2026-07-29 | `gradlew :app-desktop:createReleaseDistributable --rerun-tasks` plus controlled exact launcher | PASS: corrected 1.0.1 packaged release remained alive for 30 seconds |
| 2026-07-29 | two consecutive `gradlew verifyDependencies` runs | PASS: SHA-256 policy validated; configuration cache stored and then reused |
| 2026-07-29 | `gradlew test check -x detekt verifyDependencies :app-android:assembleStandardRelease :app-android:lintStandardRelease :app-desktop:packageReleaseDistributionForCurrentOS` | PASS: 609 tests (310 Desktop/JVM and 299 Android host), 0 failures/errors/skips; Android R8/lint and Windows EXE/MSI passed |
| 2026-07-29 | `scripts\smoke-test-desktop-release.ps1 -TimeoutSeconds 30` | PASS: exact release image used by packaging remained alive for 30 seconds and was closed without a leftover process; Windows release workflow runs the same guard before upload |
| 2026-07-29 | 1.0.1 Windows artifact checksum | PASS: EXE 116,308,992 bytes, SHA-256 `E9C2CC494E0171441AEA0C6F4AF7A2606B467A1DB7696BA095D548BB40F43F9D`; MSI 115,605,023 bytes, SHA-256 `D169A4AF9CC84D3C59E83F58F22C85611B2B655F05CE6A2A279A829B8A949BB0` |
| 2026-07-29 | connected-device Standard debug install and cold start | INITIAL FAIL reproduced on `AQM-LX1` Android 10: design-system Compose resource asset absent, causing `MissingResourceException`; repaired APK then remained foregrounded with no fatal/known exception lines |
| 2026-07-29 | credential-editor Back crash report and repository-wide `removeLast`/`removeFirst` scan | FAIL reproduced from stack trace: `SnapshotStateList.removeLast()` was the only incompatible source call; replaced with `removeAt(lastIndex)` and no equivalent calls remain |
| 2026-07-29 | `gradlew :app-android:verifyStandardDebugComposeResources --warning-mode all` | PASS: required design-system strings bundle is present in the APK; only expected Windows-host iOS simulator warning remains |
| 2026-07-29 | final connected-device process and redacted Logcat monitor | PASS: corrected Standard debug process remained foregrounded/alive with the same PID for more than five minutes; no `FATAL EXCEPTION`, `MissingResourceException`, or `NoSuchMethodError`; the only token heuristic was Android `ActivityThread` window metadata |
| 2026-07-29 | final `gradlew test check -x detekt verifyDependencies :app-android:assembleStandardRelease :app-android:lintStandardRelease :app-desktop:packageReleaseDistributionForCurrentOS` | PASS: 609 tests, Android APK resource assertion/check/lint/R8/release, dependency metadata, iOS compile-only tasks, and Windows EXE/MSI packaging |
| 2026-07-29 | editorial-vault UI targeted compilation after each feature group | PASS: onboarding, unlock, vault, credential, generator, health, settings, backup, shared application, Desktop, and iOS simulator compile-only targets |
| 2026-07-29 | controlled Desktop UI run at expanded 1536 x 830 and compact 520 x 820 sizes | PASS: responsive unlock layout remained bounded, scrollable, and responsive with no application exception; process closed cleanly |
| 2026-07-29 | final `gradlew test check -x detekt verifyDependencies :core:designsystem:compileKotlinIosSimulatorArm64 :shared:compileKotlinIosSimulatorArm64 :app-android:assembleStandardRelease :app-android:lintStandardRelease :app-desktop:packageReleaseDistributionForCurrentOS --stacktrace` | PASS: 611 tests, 0 failures/errors/skips; 934 tasks; Android lint/R8/release, dependency metadata, iOS compile-only targets, and Windows EXE/MSI packaging |
| 2026-07-29 | `scripts\smoke-test-desktop-release.ps1 -TimeoutSeconds 30` after UI rework | PASS: exact packaged launcher remained running for the full guard period and closed cleanly |
| 2026-07-29 | final UI source scan | PASS: no TODO/FIXME/HACK/XXX markers, legacy component colors, production logging, or hardcoded user-visible prose; matches were Compose animation diagnostic labels and non-sensitive benchmark timing output only |
| 2026-07-29 | post-rework Android debug runtime request | EXTERNAL: Standard debug APK and Compose-resource assertion pass, but `adb devices -l` reports no connected device, so install/interactive/Logcat verification was not represented as passing |
| 2026-07-29 | post-rework artifact checksums | PASS: EXE 116,341,760 bytes, SHA-256 `46E5205823F51C590439BA86802742CFECF45DE8C59AC8ECE0B272E55161D760`; MSI 115,637,786 bytes, SHA-256 `CDFF6165EC43BD9EC5BACC9E007EE7E93134A51686A5B85B61A24F4877BE04DE`; debug APK 34,927,908 bytes, SHA-256 `AA735EA6ABDF16800527D7333528AEFBC4F84032E3800501246D884D8982F991`; unsigned release APK 22,936,300 bytes, SHA-256 `B6A3B0A2A0DD1A684197385B1A218179F803633DC865F8364CFE6E84C4C0F9FA` |
| 2026-08-03 | `gradlew :feature:unlock:desktopTest :feature:settings:desktopTest :core:database:desktopTest` | PASS: biometric availability, cancellation, enrollment state, verified VEK success, and wrong-key fail-closed regressions |
| 2026-08-03 | `gradlew :app-android:assembleStandardDebug :shared:linkDebugFrameworkIosSimulatorArm64 verifyDependencies` | PASS: Android biometric APK, linked iOS Keychain/LocalAuthentication framework, and dependency checksums |
| 2026-08-03 | `gradlew :feature:unlock:testAndroidHostTest :feature:settings:testAndroidHostTest :app-android:lintStandardDebug` | PASS |
| 2026-08-03 | `gradlew detekt` | FAIL only on the five pre-existing GATE-009 findings; the biometric implementation adds none |
| 2026-08-03 | physical Android/iPhone biometric prompt matrix | NOT RUN: GATE-010 remains open and is not represented as passing |

## Completion rule

The repository implementation and non-Detekt automation are internally verified. Public production readiness cannot
be claimed while `GATE-009`, `GATE-010`, or the applicable external S0/S1 gates have
publisher/device/independent evidence. These gates are not TODO implementations hidden in code; they are explicit
release dependencies that the repository cannot truthfully manufacture.
