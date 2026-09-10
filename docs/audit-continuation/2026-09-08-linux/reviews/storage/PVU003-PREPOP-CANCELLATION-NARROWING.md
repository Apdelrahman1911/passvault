# PVU-003: Back cancellation is not Tab admission

2026-09-10; author `storage`, independently challenged by `editor_review`.
**Source-only narrowing; no new defect, patch, test or closure.**

Read the retained PVU-001/003 outcome projection, `REPORT.md`, current issue
statuses and Linux03's accepted result/named XML cases first. Their unresolved
caller/provider boundaries and successful regression evidence are preserved.
Only the six source files bound below were then inspected.

## Narrow discriminator

Even if Backup's composed policy is stale `PopNow`, ordinary central or
interactive Back does not establish an **uncancelled** export surviving the pop:

- `SettingsRouteAdapters`:147–162 supplies `beforePop = backupViewModel.clearForLock`.
- `NavigationBackCoordinator`:60–68,97–116 invokes that callback before popping;
  registration forwards the current callback at153–166.
- `BackupViewModel`:421–449 requests cancellation of the currently tracked
  export/import jobs before returning. An exception before return prevents pop.
- Toolbar Back separately rereads current busy state and does nothing while an
  operation is active (`BackupViewModel`:138–145).

This proves **cancellation requested before pop**, not completed cancellation,
join, mutex release, provider/DAO settlement, or absence of every untracked or
noncooperative continuation. It does not resolve the inverse-order suspicion.
The exact pre-pop discriminator was not explicit in the prior storage report;
no exhaustive novelty claim across archived evidence is made.

## Smallest still-unproved integration shape

Actual Tab selection is different: `PassVaultNavigationHost`:242–246,272–274
checks `canLeaveForForwardNavigation` then selects the tab. That permission
check (`NavigationBackCoordinator`:83–95) does not invoke `beforePop`; Backup
still supplies only a composed policy, and its ViewModel is an application/session
singleton (`AppModule`:197–201). Credential live-policy guards
(`VaultRouteAdapters`:225–240) are already represented in Linux03 evidence.

Thus any worthwhile further PVU-003 attempt must first establish real
export-then-Tab input before Backup's busy policy is published, with the actual
export owner surviving, followed by genuinely overlapping opposite-order
credential deletion. Native dispatch/file-provider timing, actual job and A/V
ownership, cancellation controls and durable state remain unproved. Merely
calling both events/repositories, retaining a fake queue, or observing Tab
permission is not that witness. No fixture or execution is proposed here.

PVU-001 supplied no new discriminator beyond its retained real-provider/UI
admission and Settings cancellation boundary; that audit was not restarted.
No Git/network/build/test/helper/runtime/cache work occurred. STOP/NO-RETRY/
FAIL-no-automatic-retry/CLOSED restrictions and all denominators are unchanged.
Read-only filename misses were corrected from retained source paths, not treated
as tests. Only this compact permanent note was created.

## Checkout-byte bindings

SHA-256 below identifies reviewed bytes, not a Git tree or executed workload.
Independent review matched all six hashes (receipts `cc1acf`, `a31750`) and read
the prior report's wording (`09c515`). No independent reviewer file was edited.

| Relative source path | SHA-256 |
| --- | --- |
| `feature/backup/src/commonMain/kotlin/com/passvault/feature/backup/presentation/BackupViewModel.kt` | `afd73521b4fc0d1b06af29bdc7d4940f4646507a66fcc4e55d801f2448060e63` |
| `shared/src/commonMain/kotlin/com/passvault/shared/navigation/adapters/SettingsRouteAdapters.kt` | `6339edcfe9a09916a5485894e287c40697e85909158c0c348e51b486b8073c06` |
| `shared/src/commonMain/kotlin/com/passvault/shared/navigation/adapters/VaultRouteAdapters.kt` | `4a155566bd433f2905f6e13137d1b8e7b03da06a54fd9e12b706b4f7d3061b98` |
| `shared/src/commonMain/kotlin/com/passvault/shared/navigation/NavigationBackCoordinator.kt` | `7f0f546301a87c0bd758d121a118d8325b2fe366561655d1ee5c8e1b248126db` |
| `shared/src/commonMain/kotlin/com/passvault/shared/navigation/PassVaultNavigationHost.kt` | `50ec002a8b7b9e2c9324980662baff166596dddb31cef4823b4ffdfd784c9006` |
| `shared/src/commonMain/kotlin/com/passvault/shared/di/AppModule.kt` | `9b486df509202e65311caf92895ba8db6a44e6eae351266c8daa952bc553af25` |
