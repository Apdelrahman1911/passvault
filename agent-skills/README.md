# Reusable KMP Navigation Skills

These Skills were extracted from the implemented and repository-verified PassVault Navigation 3 architecture. Load the smallest applicable set:

| Task | Skill |
|---|---|
| New navigation architecture or broad migration | `$kmp-nav3-architecture` |
| Stack semantics, tabs, reselection, adaptive layouts | `$nav3-back-stacks-tabs` |
| System/gesture/toolbar/Desktop Back and guards | `$cross-platform-back-policy` |
| iOS interactive Back and LTR/RTL | `$ios-compose-navigation-rtl` |
| Authentication, lock, process restoration, secure cleanup | `$secure-navigation-restoration` |
| URLs, notifications, shortcuts, and deep links | `$typed-external-navigation` |
| Tests, performance evidence, builds, and release verdict | `$navigation-verification` |

For a new security-sensitive KMP app, load the primary skill plus all six focused skills. For a localized non-sensitive app, the secure-restoration skill is still useful when authentication exists but its secret-wiping requirements can be tailored to the threat model.

## Forward-test audit

The package was reviewed as if a fresh agent were implementing a new KMP app. The following matrix records which mandatory rules prevent each previously observed architecture failure:

| Failure mode | Preventing guidance |
|---|---|
| Lost/cross-contaminated tab history | Independent stack invariants, deterministic selection/reselection, and tab restoration tests in `$nav3-back-stacks-tabs` |
| Weak or absent process restoration | Serializable route-only stacks plus structural/entity validation in `$nav3-back-stacks-tabs` and `$secure-navigation-restoration` |
| Unlimited navigation commands | Bounded non-replaying command rule and 100,000-event capacity test in `$kmp-nav3-architecture` and `$navigation-verification` |
| Screen ViewModels held as singletons | Entry decorator/scoping rules, teardown requirements, and resource tests in `$kmp-nav3-architecture` and `$navigation-verification` |
| Monolithic root route host | Feature-owned adapter sequence, registry requirement, and forbidden giant-host pattern in `$kmp-nav3-architecture` |
| Private iOS recognizer manipulation | Explicit prohibition and supported host/direction implementation in `$ios-compose-navigation-rtl` |
| Fixed settlement/fast-swipe regressions | Version-default comparison, single transition owner, separate drag/settlement checks, supported iOS-only timing policy, and no app-owned post-release tween in `$ios-compose-navigation-rtl` |
| Arabic/RTL swipe regressions | Compose/UIKit direction synchronization, logical edge mapping, runtime switch checks, and physical matrix in `$ios-compose-navigation-rtl` |
| Interactive iOS Back bypassing guards | No-visual-bypass invariant and current-entry-only projection in `$cross-platform-back-policy` and `$ios-compose-navigation-rtl` |
| Protected screen restored before authentication | Locked-root-first quarantine/validation/activation sequence and no-protected-frame test in `$secure-navigation-restoration` |
| Weak deep-link stack reconstruction | Strict typed pipeline, owning-tab target, canonical reset path, and exhaustive stack tests in `$typed-external-navigation` |

## Version scope

Verified integration versions:

- Compose Multiplatform 1.11.1
- AndroidX Navigation 3 runtime 1.1.4
- JetBrains Navigation 3 UI 1.1.1
- Navigation Event runtime 1.1.2
- JetBrains Navigation Event Compose UIKit 1.0.1
- Lifecycle 2.11.0
- Koin 4.2.2

Architectural invariants are identified as stable inside each Skill. Public API names and fragile iOS behavior are identified as version-specific and require source inspection plus regression tests after upgrades.
