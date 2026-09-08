---
name: nav3-back-stacks-tabs
description: "Implement or review Navigation 3 stack ownership, push/pop semantics, singleTop behavior, independent bottom-tab stacks, tab reselection, nested/detail flows, restoration, adaptive-layout consistency, and deterministic stacks created by deep links in Kotlin Multiplatform or Compose applications."
---

# Nav3 Back Stacks and Tabs

Use application-owned stacks to make Back and top-level navigation predictable for users and developers.

## Core invariants

1. Every top-level destination owns exactly one independent logical stack.
2. Every stack is non-empty and begins with that destination's canonical root.
3. A route is accepted only by an explicitly allowed owning stack.
4. Selecting another tab changes the selected-stack pointer; it does not overwrite either stack.
5. Reselecting the active tab has one documented rule. Default to pop-to-root.
6. Compact, expanded, tablet, and desktop layouts consume the same stacks and selected destination.
7. Detail/modal routes are pushed on their owning stack unless the product deliberately models a separate overlay/scene.
8. External navigation constructs a deterministic target stack; it never appends accidentally to arbitrary history.
9. Saved route stacks contain identifiers only and remain bounded.
10. All mutations pass through the navigator.

## Model the state

Use a closed top-level type whose roots and ownership rules are exhaustive:

```kotlin
enum class TopLevelDestination { HOME, SEARCH, SETTINGS }

fun TopLevelDestination.rootRoute(): AppRoute = when (this) {
    HOME -> HomeRoute
    SEARCH -> SearchRoute
    SETTINGS -> SettingsRoute
}

fun AppRoute.isAllowedIn(destination: TopLevelDestination): Boolean = when (destination) {
    HOME -> this is HomeFamilyRoute
    SEARCH -> this is SearchFamilyRoute
    SETTINGS -> this is SettingsFamilyRoute
}
```

Store one `NavBackStack<AppRoute>` per destination. Expose read-only views. Normalize corrupted/empty stacks to their root and fail tests when ownership is incomplete.

Do not derive navigation state from which composable branch happens to be rendered.

## Mutation semantics

### Push

- Validate route arguments and stack ownership first.
- Reject or no-op when the same route is already topmost.
- Choose duplicate semantics deliberately:
  - `singleTop`: suppress only an identical top route;
  - `bringExistingToFront`: truncate after an existing equal route;
  - `allowDuplicate`: only when multiple equal logical entries are meaningful and have unique identity.
- Do not mix these definitions under one undocumented `push` name.

For typical settings/detail graphs, truncating to an existing route prevents loops and accidental duplicates. For repeated instances, add a stable entry identity rather than relying on object equality accidentally.

### Pop

- Pop only the active stack.
- Never remove its root.
- If the active tab is at its root, apply the product's top-level Back policy, such as returning to HOME before exiting.
- A guarded pop must be authorized once, then completed once. A stale interactive callback must not cause a second pop.

### Root replacement

Root replacement is a security/application state transition, not ordinary push/pop. It must atomically update access state, clear or quarantine prior stacks as required, and invalidate old entry/session tokens.

### Tab selection

```kotlin
fun selectTab(destination: Tab, token: Token): Mutation {
    validate(token)?.let { return Rejected(it) }
    if (destination == selectedTab) {
        return if (popToRoot(destination)) Applied else Rejected(AtRoot)
    }
    selectedTab = destination
    return Applied
}
```

Never clear the previous tab merely because the user leaves it. Test rapid alternating selections and same-tab repeated taps.

## State and memory boundaries

Use Nav3 entry decorators for saveable UI state and entry-scoped ViewModels. Retaining tab stacks intentionally retains their entry state; keep stacks bounded by product behavior and avoid storing large objects in route keys.

When a tab is reset, removed entries must lose their saveable/ViewModel state. Security-sensitive applications must not keep protected entry decorators alive in a locked quarantine; load `$secure-navigation-restoration`.

## Deep-link stack construction

Map every typed external intent to:

```text
owning tab + canonical root + validated path to target
```

Choose and document whether a deep link resets the target stack or reuses its prior history. Resetting to a canonical path usually gives the most deterministic Back behavior. For example:

```text
Credential link -> HOME / CredentialDetail(valid ID)
Security link   -> SETTINGS / Security
```

Do not append a settings destination to whichever tab happens to be selected.

## Restoration

Serialize each stack and the selected destination. On restore:

1. Decode with stable typed-route serializers.
2. Enforce a maximum stack depth.
3. Require the correct root.
4. Enforce route ownership and argument shape.
5. Validate entity identifiers when data access is allowed.
6. Truncate at the first stale child or reset safely to root.
7. Activate only after applicable authentication/security checks.

Configuration recreation and process recreation may have different security rules. Never use process convenience to bypass a locked root.

## Adaptive UI rules

Navigation rail, bottom bar, sidebar, and tablet master/detail are presentations of the same `selectedDestination` and stacks. Do not create a second expanded-layout state machine.

If expanded UI renders multiple panes, model the scene explicitly while retaining one logical history. Verify rotation/resize does not silently replace, duplicate, or cross-move destinations.

## Forbidden anti-patterns

- A top-level enum plus `when` that recreates a tab screen and loses history.
- One global stack for all tabs without deliberate cross-tab semantics.
- Clearing all stacks on ordinary tab selection.
- Direct `backStack += route` calls outside the navigator.
- Allowing any route in any stack.
- Empty stacks or roots inferred from `firstOrNull()` without normalization.
- Replaying old navigation commands after recreation.
- Unbounded restored stacks.
- Deep links appended to the current tab by convenience.
- Separate compact and expanded navigation state.

## Required tests

Use pure state-machine tests where possible:

- push, top duplicate, existing-route truncation, optional multi-instance behavior;
- pop, root protection, pop-to-root, root replacement;
- every route's ownership mapping;
- tab switch preserves all stacks;
- tab reselection pops only that tab to root;
- rapid/repeated tab taps do not duplicate or corrupt stacks;
- nested/detail return path;
- configuration restoration of every stack and selected tab;
- malformed, wrong-root, oversized, cross-tab, and stale-entity restoration;
- deterministic deep-link stack for every external intent;
- deep link into a background tab followed by Back and tab switching;
- compact/expanded presentation equivalence;
- removed entries release entry-scoped state.

## Review checklist

- [ ] The stack owner is discoverable for every route.
- [ ] Roots are canonical and non-removable.
- [ ] Duplicate semantics are named and tested.
- [ ] Switching tabs retains stacks and reselecting is deterministic.
- [ ] Deep links build canonical history.
- [ ] Restoration is bounded and validated.
- [ ] Adaptive layouts share state.
- [ ] No production code mutates stacks outside the navigator.
- [ ] Saveable and ViewModel state are scoped to entries.
- [ ] Security rules are applied before protected restoration.

## Version boundary

The verified reference used Compose Multiplatform 1.11.1, Navigation 3 runtime 1.1.4, and JetBrains Navigation 3 UI 1.1.1. The ownership and state-machine invariants are stable; `NavBackStack`, serializer, decorator, scene, and restoration APIs are version-specific and must be checked after upgrades.
