# PVA-007 actual-Main NavHost / Room successor — independent source review

Reviewer `/root/editor_review`; author `/root/android32`; 2026-09-10.
**SOURCE ACCEPT after the terminal-input correction below. NOT execution admission.**
No further necessary source correction or new product defect was established.

## Exact input and preserved correction

Paths below are relative to `reviews/android32/navhost-draft/`.

| Input | SHA-256 |
|---|---|
| Accepted `CredentialMainNavHostRoomIntegrationTest.kt` — 38,728 bytes / 727 LF | `7eee7bc045abcb60660203f965a33a71e6e791d9ba2c22509a52ee615040ec9d` |
| Rejected terminal implementation — 38,330 bytes / 720 LF | `eaaa21fc27d85943e9993fcc7165a42c212f458136843b4c44509fd1ae4131d2` |
| `REJECTED-EAAA720-TERMINAL-CORRECTION.patch` — 1,550 bytes | `7ea32fbb0bab542aa9b64798e0911b64a27f4a236de312bdc5370e6405732b30` |

All 720 original lines were read; the seven-line-net correction was independently
challenged. Applying its inverse **in memory only** recovered the exact rejected hash,
proving no other draft change. The old 959-LF manual-owner draft was not rereviewed.

**R1, fixture-only:** the former final five-second `onEdt` wait could time out while its
EDT work continued, wrote the Quit receipt and let real Main exit before driver failure
reporting. Author and reviewer independently identified this possible false pass.
The accepted block instead queues one terminal EDT action, issues Ctrl+Q DOWN/UP and
its receipt there, and catches/reports errors **inside EDT before queued Quit handling**.
No failable timed driver wait follows scheduling. Earlier input failures cannot reach
the terminal marker. Parent deadline, exact sequence, diagnostic and exit checks remain.
The marker proves successful key-call issuance, not observed physical key state.

## Critical source support

- The Main role invokes actual `com.passvault.desktop.main()`: instance lock, global
  Koin, `PassVaultDesktopWindow`, `PassVaultApp`/NavHost and terminal coordinator belong
  to production. No replacement lifecycle/store providers, root cleanup coordinator,
  direct navigation callback or test mutation of Main's repository is introduced.
- Robot input reaches pending field name/value, toolbar Back/Stay, host Escape/Stay,
  dirty Settings/Stay and dirty Add/Discard. Real MAIN shell guards run before tab/Add
  navigation; Discard must return to detail rather than replay Add. Clean Settings→Home
  retaining detail rejects an implementation that merely blocks all tab selections.
  AWT accessibility is read-only. The bottom dock strip is conservatively excluded
  from ordinary target geometry; missing or ambiguous native targets fail closed.
- Seed, Main and verify are **three serial fresh JVM roles in one `@Test`**, not three
  tests. Startup home/preferences/tmp/XDG roots are isolated without changing worker
  `user.home`. Seed/verify use the real local Koin modules and repository interfaces,
  including genuine encrypted Room storage, blob store and Linux unavailable biometric
  host. Create really leaves the session Locked before explicit unlock. No fake DAO,
  in-memory persistence oracle or test-owned cleanup of Main's graph is substituted.
- Seed closes and exits before Main starts; verify starts only after Main's successful
  settled exit and exact trace/diagnostic checks. Same database file key, sole credential
  ID and exact original two field IDs/names/values/secret flags are required. This proves
  the intended **final durable-state** oracle; it cannot exclude a transient write later
  reverted. No crash/power-loss/recovery behavior is established.
- Exit zero alone is correctly rejected as insufficient: production instance contention
  and cleanup timeout/failure logs are forbidden. Seed/verify explicitly attempt ordinary
  lock, one checkpoint/Room close, actual host close and local Koin close; Koin close is
  not used as a substitute for them. Normal bootstrap's absent-recovery-root preflight
  returns without recovery work. No preserve/reset or recovery action is requested.

## Compile and admission boundaries

Direct lifecycle/store-owner imports and RoomDatabase/DAO construction are removed.
Source-visible repository/bootstrap APIs, same-module Main/host, explicit Koin/coroutine
and test dependencies provide a plausible compile route. This is **not compilation or
actual classpath evidence**. Adoption, exact Test runtime forwarding, dependency
verification and a newly admitted build remain root-owned; no dependency edit is implied.

PAGE_TAB/name/text mappings, renderer/input readiness, focus and geometry remain native
software observations to obtain, not grounds to replace input with callbacks. The case
really traverses the production wrapper but does not separately establish tray behavior,
focus-loss security, clipboard security, hardware biometrics, arbitrary restoration or
all dirty-navigation interleavings. No PVD boundary or product policy is redesigned.

Parent per-role deadlines are 90/200/90 seconds plus up to five seconds each for owned
child settlement; the driver is nominally 150 seconds. Launch/filesystem/native work and
NonCancellable lock/SQLite close still require an external hard bound. 512 MiB is a Java
heap cap, not a native Argon2/SQLite/Skia or total-RSS bound. Per-role log/trace acceptance
limits (256 KiB/8 KiB) are polling limits, not hard disk quotas. Root must admit the full
private X/WM/authority, native-memory/output budget and allowlisted cleanup scope.

Forced termination is failure containment only. Process exit is not proof that every
nested coroutine gracefully joined, nor external X/descendant settlement. The fixture
deletes nothing and cannot perform root's wrapper stop/worker cleanup. Unexpected
shutdown and skipped XML cannot be counted as a passing case. No real clipboard/vault,
picker/preview/URL action, signing, publication or release candidate is involved.

## Count and restriction seal

**One prospective case; three roles; thirteen ordered trace events; zero compilations
or executions by this review.** No task/event count, source proof or mock is reported as
an executed test. No closure/coverage denominator changes. C13/focused3 inputs, T/P and
held runtime evidence were untouched; this review does not admit or retry their work.
PVU-007 STOP, PVU-011 NO-RETRY, PVA-029 failure/no automatic retry, G7/G8 CLOSED and all
other HOLD/consumed scopes remain intact. Root alone may adopt, publish or admit a run.

Only inert source reads, in-memory patch comparison and this new permanent review were
performed. Source-locator misses and a truncated printer output were resolved through
focused reads, not target execution. No build, test, import, Git, network, process probe,
service, temporary application output or cache was created.

## Production byte bindings (source proof, not a whole-tree execution identity)

Names are repository-relative, with package prefixes elided only for readability.

| Source | SHA-256 |
|---|---|
| `app-desktop/.../Main.kt` | `9a67a8a5c2f05331e91ec5c18f6317a3562bd26e2c720105a8a21773edd8ae93` |
| `app-desktop/.../PassVaultDesktopWindow.kt` | `794a8fdbaea87edca986b2f729753dad3d434467f21e59ec1fcbb9027402e702` |
| `app-desktop/.../DesktopShutdownCoordinator.kt` | `7156e03eb7e5d2ea7636a9320a49aeaf15d0694522d88f78836b075c74e9043d` |
| `app-desktop/.../components/KeyboardShortcuts.kt` | `116059ec84f39cbf9f75210a638dc5c1c8a987903c6cf2663290c82437a826c5` |
| `shared/.../di/AppModule.kt` | `9b486df509202e65311caf92895ba8db6a44e6eae351266c8daa952bc553af25` |
| `app-desktop/.../di/DesktopModule.kt` | `7b70040fe488c9825090548b3d8ca4a2e4f34c1989b072d2571d74c9647cd253` |
| `shared/.../navigation/PassVaultNavigationHost.kt` | `50ec002a8b7b9e2c9324980662baff166596dddb31cef4823b4ffdfd784c9006` |
| `shared/.../VaultTab.kt` | `cf67f5bcfd1c85719ff24a6096bafae8057ec6d9f1ff3ee47d56c464bed4f97a` |
| `core/database/.../repository/VaultRepositoryImpl.kt` | `aee86a3e876da0318c750c350c3ec4eb8938aa1929123533c041c3a10ec38630` |
| `core/database/.../VaultDatabaseBootstrap.kt` | `5726afb24093afa8c56c57321d64f0ea747db69910f0b7aa984e9d18a86f2598` |
| `app-desktop/.../security/biometric/DesktopBiometricRuntime.kt` | `111d03d9d074f0dc46dfcd670bcf2b3669b6a9104236fdb88287d41ad2b2e496` |
| `app-desktop/.../security/biometric/DesktopBiometricBridge.kt` | `d67d19ea04a426f0abec1ea9fc40e466670ccc5029350bb04f2d6328457eeb55` |
| `app-desktop/build.gradle.kts` | `381ef53b5bc4ae775aa2322dbaea02588d499111d2a32e2ca3cdb2557363acde` |
| `shared/build.gradle.kts` | `826cc234a59c05efdbbcd6a0ea62f25362f0a54d588e996582903f44d09949ad` |
