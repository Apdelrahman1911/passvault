---
name: cross-platform-back-policy
description: "Design, implement, or audit one explicit Back policy across Android system and predictive Back, iOS interactive swipe, toolbar buttons, Desktop Escape/Back, and programmatic actions. Use when screens have dirty editors, dialogs, transient search, active operations, navigation guards, or any risk that a gesture can bypass application rules."
---

# Cross-Platform Back Policy

Make every Back source ask the same policy before changing or visually previewing navigation.

## Non-negotiable invariant

**No platform may mutate or visually begin popping navigation through a path that bypasses the application's Back policy.**

This includes Android predictive previews and iOS interactive gestures. Preventing the final mutation is insufficient if protected content was already revealed underneath.

## Model dispositions explicitly

Use a small closed model such as:

```kotlin
enum class BackDisposition {
    PopNow,
    HandleInPlace,
    Blocked,
    ExitApplication,
}
```

- `PopNow`: revealing and removing the previous navigation entry is currently safe.
- `HandleInPlace`: Back closes local UI or performs a destination-owned action; it does not pop yet.
- `Blocked`: consume Back and expose no previous entry while an operation or policy forbids leaving.
- `ExitApplication`: the application root has no in-app Back action. Platform code decides whether to exit/dismiss.

Do not overload `Blocked` to mean “show a discard dialog.” That is normally `HandleInPlace`: the handler opens/updates local confirmation state, after which a deliberate confirmation may authorize a guarded pop.

## One coordinator

The coordinator resolves the active entry's registration, falls back to a conservative route default, and invokes exactly one action:

```kotlin
class BackCoordinator(private val navigator: AppNavigator) {
    fun effectiveDisposition(): BackDisposition = /* active registration or safe default */

    fun requestBack(): Boolean = when (effectiveDisposition()) {
        PopNow -> navigator.pop(activeToken).wasApplied
        HandleInPlace -> { activeHandler(); true }
        Blocked -> true
        ExitApplication -> false
    }
}
```

Registrations must be tied to route identity plus an active-entry/session token. Replacement registration must not be removed by disposal of the older instance. Stale entries must never become active policy owners.

Unknown routes that require explicit guards default to `Blocked`, not `PopNow`.

## Integrate each Back source

### Toolbar and programmatic Back

Call `coordinator.requestBack()`. Do not call `navigator.pop()` directly unless completing a previously authorized guard with a still-valid token.

### Desktop

Handle `Escape` and platform Back on key-down through the coordinator. Avoid duplicate key-up handling. Returning `false` only at `ExitApplication` lets the host apply its root policy.

### Android system and predictive Back

Use the current public Navigation Event/Back APIs. `PopNow` may expose the actual previous entry for predictive progress. `HandleInPlace` and `Blocked` must install a consuming handler and must not expose a navigable previous entry.

### iOS interactive Back

Let the supported navigation library own progress and settlement only for `PopNow`. For any other disposition, render only the active entry to the interactive navigation layer (or use the current supported eligibility API) and consume the gesture through the guarded handler.

A useful Nav3 rendering rule is:

```kotlin
fun <T : Any> entriesAllowedByBackPolicy(
    entries: List<NavEntry<T>>,
    disposition: BackDisposition,
): List<NavEntry<T>> =
    if (disposition == BackDisposition.PopNow) entries else entries.takeLast(1)
```

This prevents visual disclosure when the pinned UI library infers gesture eligibility from `previousEntries`. Recheck the library contract after upgrades.

## Destination decision table

Use explicit destination state:

| Destination condition | Disposition | Action |
|---|---|---|
| Clean detail/editor with prior entry | `PopNow` | Optional sensitive cleanup, then pop |
| Dirty editor | `HandleInPlace` | Show discard confirmation |
| Confirmation dialog visible | `HandleInPlace` | Dismiss dialog or apply its Back rule |
| Search/filter/local pane open | `HandleInPlace` | Close local UI |
| Import/export/save/critical operation active | `Blocked` | Consume; optionally explain |
| Tab root other than home | `HandleInPlace` | Select documented home root |
| Application home/auth root | `ExitApplication` or `Blocked` | Product/platform policy |

When multiple local conditions exist, resolve by explicit priority, typically critical operation > modal/dialog > dirty confirmation > transient UI > pop.

## Guarded-pop protocol

1. Register the current disposition with an active-entry token.
2. On Back, handle local state or show confirmation.
3. On user confirmation, revalidate the same token/session/current route.
4. Clear sensitive state in `beforePop` if required.
5. Mutate the stack once.
6. Reject later interactive/programmatic completion callbacks as stale.

Never set a boolean “pop allowed” and let any old callback use it without identity/session validation.

## Forward navigation

The same dirty/operation state may govern forward navigation. Expose a deliberate `canLeaveForForwardNavigation()` or a separate leave policy. Do not assume Back-only guards automatically protect tab changes, add buttons, notifications, or deep links.

## Lifecycle and concurrency

- Disable navigation when the host or entry is not resumed.
- Invalidate entry tokens on root/session changes.
- Make registration disposal identity-aware.
- Treat two fast Back inputs as one applied mutation plus one stale/at-root rejection.
- Cancel local confirmation state when its entry leaves.
- On background/lock/detach, stop interactive eligibility immediately.
- Do not queue Back events for replay.

## Forbidden anti-patterns

- Platform Back handlers calling stack mutation directly.
- Letting an iOS/Android predictive preview begin and checking the guard only on completion.
- A global `canGoBack` boolean unrelated to the active entry.
- Registering guard callbacks without route/session identity.
- Returning `PopNow` while a dirty dialog or critical operation is active.
- Fixed gesture animations implemented outside the navigation event owner.
- A toolbar with different semantics from system Back.
- Swallowing all Back inputs at root without a documented platform policy.

## Required tests

- Default root and nested dispositions.
- Dirty editor opens confirmation and does not pop.
- Confirmation Back dismisses locally.
- Blocked operation consumes without previous-entry exposure.
- Search/local transient UI closes before pop.
- `PopNow` calls cleanup and pops exactly once.
- Duplicate interactive plus toolbar/system completion cannot double-pop.
- Stale registration and stale session cannot mutate.
- Replacement registration survives old disposal.
- Only the last entry is rendered for `HandleInPlace`/`Blocked`.
- Tab-root Back follows the documented home/exit behavior.
- Desktop Escape and Back key use the same coordinator.
- Android system/predictive Back match toolbar results.
- iOS LTR/RTL gestures are disabled for guarded destinations.
- Background, lock, and host-detach interruption leave a coherent current route.

## Review checklist

- [ ] Every Back source calls one coordinator.
- [ ] Every destination's local/blocked states map explicitly to a disposition.
- [ ] Non-`PopNow` states cannot reveal prior entries.
- [ ] Tokens bind guard actions to the active entry and session.
- [ ] Cleanup happens before the authorized pop.
- [ ] Duplicate completion is harmless and observable in tests.
- [ ] Forward navigation cannot bypass relevant leave guards.
- [ ] Root behavior is documented per platform.
- [ ] No queued/replayed Back stream exists.

## Version boundary

The verified reference used Compose Multiplatform 1.11.1, Navigation 3 UI 1.1.1, Navigation Event runtime 1.1.2, and Navigation Event Compose UIKit 1.0.1. The unified-policy and no-visual-bypass invariants are stable. Handler state, predictive entry exposure, and gesture eligibility APIs are version-specific; inspect official source and repeat platform tests after upgrades.
