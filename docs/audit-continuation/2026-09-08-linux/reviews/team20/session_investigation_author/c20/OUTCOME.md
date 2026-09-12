# C20 permitted session/storage caller outcome

Author `/root/session_investigation_author`, 2026-09-11; independent counterpart
`/root/session_investigation_review`. **NO NEW ACTIONABLE WITNESS; REQUEST REASSIGNMENT.**
PVU-001/002/006 remain unresolved. No patch or new fixture is justified by this check.

## Narrow disposition

- **PVU-001:** Current nonbiometric credential-summary/health lease callers do
  not supply the missing reason that real Settings owner cleanup would leave
  its queued password-change job alive. `SettingsViewModel.clearForLock()` still
  cancels that job first. Early Locking publication, repository-key detachment,
  lease revocation/cancellation and the inner settlement timer remain distinct.
  The already-reviewed Linux biometric-unavailable route is not reclassified.
  **Still needed:** actual production admission of both a lease and a queued
  transition surviving genuine cleanup, with measured leased-array settlement.
  Another arbitrary blocking owner would only repeat the artificial witness.
- **PVU-002:** Confirmation still requires IDs/target, loaded/nonbusy state and
  synchronous busy acquisition; owner clearing cancels work and resets state.
  Internal deletion still lacks session authority. The shared inactivity route
  does not automatically inherit the native curtain, but this alternative was
  explicitly covered in the 2026-09-09 feasibility review, not discovered here.
  **Still needed:** newly post-lock native confirmation actually admitted before
  genuine owner clearing, real Room delete/clear ordering, then proper reopen
  and separate managed-object outcome. A direct call or prequeued input is not it.
- **PVU-006:** The real create-to-automatic-unlock caller has no explicit
  interstage suspension. `clearForLock()` cancellation, lock-intent generation
  checks, real metadata/last-access I/O and terminal Room close remain barriers.
  Current Main uses the corrected return-to-finally application seam. Preserve
  the independently accepted PVA-039 lifecycle pass, not broader PVU-006 closure.
  **Still needed:** actual legitimate onboarding/terminal ordering showing harmful
  old work, retaining the real observer/disposal/coordinator/Room and external
  process-death boundary. An inserted interstage pause, omitted observer or mock
  post-close success cannot supply it. No new narrower decisive case emerged.

Retained prior outcomes: `reviews/storage/REPORT.md` (001/002 sections only),
`reviews/storage/PVU002-UI-ADMISSION-FEASIBILITY-20260909.md`,
`reviews/editor-independent/PVU001-PVU002-MAIN-FIXTURE-FEASIBILITY-INDEPENDENT.md`,
and `reviews/editor-independent/PVU006-TERMINAL-CALLER-CHALLENGE.md`, relative to B.
Current C19 report and outcome-only/current issue rows were used as context.
This does not repeat their negative findings as new progress or prove universal
unreachability. Please reassign this lane rather than commission another unchanged
source inquiry; actual future witnesses require fresh independent admission.

## Focused current-source pins

Paths are W-relative. Hashes bind bytes observed, not a Git-tree verification,
whole-file semantic review, execution or new coverage claim.

| Path (focused spans) | SHA-256 |
|---|---|
| `core/database/src/commonMain/kotlin/com/passvault/core/database/repository/VaultRepositoryImpl.kt` (137–160,225–278,367–416,496–515,588–669,739–748,822–842) | `ce012cc2b0ffc76783b081f31285c7247bdc6192e4301f6af637c2df5cdac8e8` |
| `core/database/src/commonMain/kotlin/com/passvault/core/database/repository/CredentialRepositoryImpl.kt` (176–215,372–438) | `910fa9d999a3ef7327e3e70d6a9aa142eabf8b14131402f4415f3ef4e72f1e9f` |
| `feature/settings/src/commonMain/kotlin/com/passvault/feature/settings/presentation/SettingsViewModel.kt` (426–524) | `a19baf0425234acdae2c2d923d74dbabf1e2b6a4822d923d80b9cccabfd6d723` |
| `feature/credential/src/commonMain/kotlin/com/passvault/feature/credential/presentation/CredentialAttachmentController.kt` (161–183,258–268) | `61f3d3a8b9c17baaef3d7def87919d5e831c48c6ac160ee0f6e5cb16cd92412c` |
| `feature/credential/src/commonMain/kotlin/com/passvault/feature/credential/presentation/CredentialViewModel.kt` (314–336) | `b9a9c5e6a9e9a5eee86a13a639fa32826dbfdfbd09fa81a032935a37f58f0611` |
| `core/database/src/commonMain/kotlin/com/passvault/core/database/attachment/AttachmentRepositoryImpl.kt` (231–249) | `58693cb59658f4cbba4aca4c6e1cc42c7efc69b0a7aa98a9128bf91ebea3929e` |
| `feature/onboarding/src/commonMain/kotlin/com/passvault/feature/onboarding/presentation/OnboardingViewModel.kt` (226–305) | `ae02e90a35bb2809414796df9357a5e42be8223be3a7ef614155cf00cdaea06c` |
| `shared/src/commonMain/kotlin/com/passvault/shared/navigation/PassVaultNavigationHost.kt` (145–183,367–424,630–647) | `50ec002a8b7b9e2c9324980662baff166596dddb31cef4823b4ffdfd784c9006` |
| `app-desktop/src/desktopMain/kotlin/com/passvault/desktop/DesktopApplicationLoop.kt` (whole10 lines) | `fc8c553ee02f6c84fc535ea0672436b9688dde6753101f9d952858a05516bfac` |
| `app-desktop/src/desktopMain/kotlin/com/passvault/desktop/Main.kt` (29–30,54–67,116–140) | `9a67a8a5c2f05331e91ec5c18f6317a3562bd26e2c720105a8a21773edd8ae93` |
| `app-desktop/src/desktopMain/kotlin/com/passvault/desktop/DesktopShutdownCoordinator.kt` (29–139) | `7156e03eb7e5d2ea7636a9320a49aeaf15d0694522d88f78836b075c74e9043d` |
| `core/database/src/commonMain/kotlin/com/passvault/core/database/VaultDatabaseBootstrap.kt` (145–178) | `5726afb24093afa8c56c57321d64f0ea747db69910f0b7aa984e9d18a86f2598` |

Only bounded source/data reads and this note. One lookup included a nonexistent
`core/data/src/commonMain` path; corrected to `core/database`, with no claim based
on the absent path. Truncated displays were not treated as complete readings.
No application/helper import/execution, build/test, Git/network/CI, process/SDK/
held-runtime/private-data probe or cleanup. Zero new cases, findings, fixes,
closures, conclusive suspicion outcomes or denominator changes. PVU007 STOP,
PVU011 NO RETRY, PVA029 failure/no automatic retry, G7/G8 CLOSED, native-refusal,
all HOLDs, protected refs, build1017001 and design/dependency/version rules remain.
