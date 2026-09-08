# PassVault

Offline-only Kotlin Multiplatform / Compose password manager (Android, iOS, JVM Desktop). No cloud, sync, deep links, CSV, Firebase, or F-Droid.

Work from this directory. JDK 17 and `./gradlew` only. The Gradle daemon is off. Do not re-enable `org.gradle.configureondemand` (it skips KMP `desktopTest` registration and the root `test` task can pass without running shared suites). Prefer one focused Gradle invocation; do not combine screenshot packaging, full host/iOS tests, and minified Release in one highly parallel run.

## Commands

```bash
./gradlew test                       # custom root task: all desktop/jvm + Android host + version + l10n
./gradlew :core:crypto:desktopTest   # one module — not `:module:test`
./gradlew :core:navigation:jvmTest   # this module is `jvm`, not `desktop`
./gradlew check                      # test + detekt + verifyDependencies + verifyLocalization
./gradlew detekt verifyDependencies
./gradlew :app-android:assembleDebug
./gradlew :app-desktop:run
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

Kover is not configured (`scripts/run-tests.sh --coverage` exits 2). Device tests: `:app-android:connectedDebugAndroidTest` (needs adb). iOS host: `iosApp/iosApp.xcodeproj`, scheme `PassVault` (Run = Debug/Dev ID; Archive = Release/Store ID).

## Identities

No flavors. Debug is PassVault Dev and can sit beside a Store install.

| Variant | Android | iOS |
| --- | --- | --- |
| debug | `com.passvault.android.debug` | `com.passvault.ios.debug` |
| release (every Play/TestFlight/prod track) | `com.passvault.android` | `com.passvault.ios` |

`storeScreenshot` is CI-only and reuses the debug ID: `:app-android:assembleStoreScreenshot`.

## Easy-to-miss architecture

- `shared/` is the composition root and the only live `NavDisplay`. Features emit effects; they do not own a back stack or `NavController`.
- Route keys live in `core/navigation/.../NavigationKeys.kt`. Process restore must not reopen a decrypted route.
- There is no `core:data`. Domain contracts are in `core:domain`; Room + encrypted repositories + backups are in `core:database`.
- Vault/Settings/Backup/Onboarding/Unlock ViewModels are Koin `single`s (session-scoped teardown). Credential/Generator/Health/TOTP use nav-entry `viewModel()`.
- User strings: `core/designsystem/src/commonMain/composeResources/values/strings.xml` **and** `values-ar/`. `verifyLocalization` fails on missing Arabic, copied English (unless allowlisted), placeholder drift, hardcoded `Text("...")`, and absolute LTR icons/`TextAlign.Left`.
- Room schema is **v4** (`VaultDatabase.kt`; some docs still say 3). Exported schemas: `core/database/schemas`. Migrations `1→2→3→4` are required and non-destructive. Do not add `fallbackToDestructiveMigration`. The DB file is not SQLCipher.
- Version source is `version.properties`. Keep `PassVaultBuildInfo.VERSION` and `iosApp/Configuration/Config.xcconfig` aligned (`verifyReleaseVersion`).
- New or changed Gradle artifacts need SHA-256 entries in `gradle/verification-metadata.xml`. Never disable dependency verification.

## Conventions

- `.editorconfig`: 4-space Kotlin, 120 cols (100 under `**/ui/**`, 140 in `*Test` source sets), trailing commas.
- Detekt (`detekt.yml`) is the linter; keep `parallel=false` (Detekt 2 can crash on concurrent KMP analysis).
- Tests: `*Test.kt`, kotlin.test + AssertK + Turbine. Shared tests in `commonTest`. Security changes must cover failure, tamper, lock, and redaction. Read `docs/SECURITY_MODEL.md` for crypto/storage/backup/clipboard/lock work. When docs disagree with Gradle or source, trust the executable source.
- Commits look like `fix(#72): persist non-bypassable environments`. Do not commit `local.properties`, signing material, `.pvault` files, real vaults, or `docs/MOBILE_RELEASE_AGENT_RUNBOOK.md`.

For navigation, backups, biometrics, migrations, identities, or release work, load the matching skill from `../engineering-skills/` (or the generated `agent-skills/` mirror). Do not hand-edit the mirror.
