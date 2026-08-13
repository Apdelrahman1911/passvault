---
name: kmp-nav3-architecture
description: "Design, implement, review, or migrate Kotlin Multiplatform and Compose Multiplatform navigation built on Navigation 3. Use for typed NavKey routes, application-owned state, a central navigator, feature route adapters, entry-scoped ViewModels, tab stacks, restoration, external inputs, platform Back integration, or when replacing a monolithic navigation host without running two engines."
---

# KMP Navigation 3 Architecture

Build one predictable navigation state machine shared by Android, iOS, and Desktop while leaving platform input and presentation at platform boundaries.

## Load supporting skills

Load this skill first for a new architecture or broad migration. Also load:

- `$nav3-back-stacks-tabs` for stack and top-level destination work.
- `$cross-platform-back-policy` for system, gesture, toolbar, or guarded Back.
- `$ios-compose-navigation-rtl` for any iOS navigation change.
- `$secure-navigation-restoration` when protected content or authentication exists.
- `$typed-external-navigation` for links, notifications, shortcuts, or intents.
- `$navigation-verification` before declaring the work complete.

## Stable architecture invariants

These rules are independent of a particular library version:

1. There is one navigation source of truth and one production mutation API.
2. Routes are typed, minimal identifiers. Never place repositories, ViewModels, secrets, or domain objects in a route.
3. The application owns logical state; `NavDisplay` renders it and does not become an alternate state owner.
4. Each top-level destination owns an independent stack.
5. Screen composables receive state and callbacks. Feature route adapters resolve arguments, own entry state, and translate effects into navigator calls.
6. Every mutation is checked against lifecycle, active-entry, session, destination, and argument invariants as applicable.
7. Back eligibility is decided before a platform can visually reveal the prior entry.
8. Compact, expanded, tablet, and desktop shells render the same logical state.
9. Restoration persists only route-safe information and is validated before activation.
10. External inputs cannot write stacks directly.
11. Do not run Navigation 2 and Navigation 3 simultaneously during migration.
12. Treat platform motion as a versioned presentation policy: preserve the architecture when framework defaults differ, and use supported platform-specific transition APIs when physical UX evidence justifies it.

## Reference flow

```text
Platform input
  -> strict adapter/parser
  -> AppNavigator
  -> navigation/access/Back guards
  -> application-owned NavigationState
  -> NavDisplay
  -> feature route adapter
  -> state-driven screen
```

## Implementation sequence

### 1. Inventory before editing

Find every route, direct stack mutation, navigation effect, tab switch, Back handler, platform gesture, deep link, and navigation-scoped ViewModel. Record:

- roots and authentication states;
- route arguments and their validation rules;
- which routes belong to which top-level stack;
- local UI that consumes Back;
- operations that block leaving;
- state that may be persisted;
- platform entry points and current dependency versions.

Do not infer behavior from a design document alone. Verify source and tests.

### 2. Define typed routes and a registry

Prefer a closed serializable `NavKey` hierarchy with stable serial names:

```kotlin
@Serializable
sealed interface AppRoute : NavKey

@Serializable
@SerialName("vault")
data object Vault : AppRoute

@Serializable
@SerialName("credential-detail")
data class CredentialDetail(val credentialId: String) : AppRoute
```

Keep values canonical and bounded. Add:

- an exhaustive route-kind function;
- a route registry completeness test;
- serialization round-trip and stable snapshot tests;
- ownership and argument validation.

Treat changing a serial name as a persistence migration.

### 3. Create application-owned state

State should describe roots, selected top-level destination, and the stack owned by every top-level destination. Keep mutation methods internal so the navigator is the only production writer.

```kotlin
class NavigationState(
    private val authStack: MutableList<AppRoute>,
    private val tabStacks: Map<Tab, MutableList<AppRoute>>,
) {
    val root: StateFlow<Root> = /* read-only */
    fun activeStack(): List<AppRoute> = /* selected root/stack */

    internal fun push(route: AppRoute): Boolean = /* validate then mutate */
    internal fun replaceRoot(root: Root) = /* atomic root policy */
}
```

Use `rememberSerializable`/the current public Nav3 serializer for route stacks when supported. Persist selected-tab identifiers separately. Authentication/root exposure rules belong to the security design, not blindly to the serializer.

### 4. Centralize mutations

`AppNavigator` owns the equivalents of:

- `push` and `pushSingleTop`;
- `pop` and guarded-pop completion;
- `popToRoot`;
- secure `replaceRoot`;
- `selectTab` and deterministic `reselectTab`;
- external intent application.

Issue a token/capability for the active entry containing at least route, root, selected stack, session generation, and lifecycle eligibility. Reject stale callbacks rather than applying them to a new screen or session.

Classify mutation results. Expected races such as a stale effect may be ignored deliberately; invalid routes and programmer errors must remain diagnosable.

Do not use an unlimited command channel. For UI commands, choose a bounded non-replaying flow/channel with an explicit overflow policy. For external navigation, keep at most one observable pending envelope unless the product explicitly requires a durable queue.

### 5. Split feature route adapters

Create feature-owned registration functions/files rather than a giant root host:

```kotlin
fun EntryProviderScope<AppRoute>.credentialRouteAdapters(context: RouteContext) {
    entry<CredentialDetail> { route ->
        val token = entryNavigationToken(context.navigator, route)
        val viewModel: CredentialViewModel = koinViewModel(
            key = "credential:${route.credentialId}",
        )
        CredentialScreen(
            state = viewModel.state.collectAsState().value,
            onBack = { context.back.requestBack() },
        )
    }
}
```

Adapters own route argument resolution, entry-scoped ViewModels, lifecycle-aware effect collection, navigation callbacks, and destination Back registration. Screens stay navigation-agnostic.

Use a registry test so adding a route without an adapter fails.

### 6. Scope state correctly

Use the Nav3 saveable-state and ViewModel-store entry decorators supported by the pinned version. Entry-scoped state is the default for detail/edit/import/export/tool screens. Application or authenticated-session scope is reserved for genuinely shared state.

Every sensitive entry-scoped ViewModel must clear secrets in its teardown path. Lock/session replacement must also clear application/session-scoped sensitive state centrally.

Never make a screen ViewModel a singleton merely to access it from a root NavHost.

### 7. Integrate platforms without divergence

Platform adapters translate Android Back/predictive Back, iOS interactive Back, Desktop Escape/Back, links, notifications, and shortcuts into the same navigator/back-policy APIs. Do not put `Platform.isIos` branches in shared screens.

Keep one logical navigation state and one Compose stack. A second UIKit/UINavigationController stack is a different architecture and requires an explicit product decision.

Navigation-version defaults are not architectural guarantees. If a newer Navigation 3 release has
materially different iOS timing from a prior Navigation 2 implementation, retain Navigation 3 and
adjust only the public iOS transition specifications after source inspection and device testing.
Keep interactive progress library-owned, keep predictive content linear, and test post-release
settlement separately from finger tracking. Never migrate backward solely to obtain an older
default duration.

## Migration rules

Migrate in coherent batches:

1. Add typed routes and tests without changing behavior.
2. Add state and navigator behind current UI.
3. Move all mutations to the navigator and delete direct mutation paths.
4. Introduce the unified Back policy before enabling interactive Back.
5. Establish independent tab stacks.
6. Move route wiring feature by feature into adapters.
7. correct ViewModel scopes and sensitive cleanup.
8. Add secure restoration and external navigation.
9. Replace platform hacks with supported adapters.
10. Delete obsolete hosts, routes, gestures, and transition overrides.

At every stage there must be one active engine. Use adapters around legacy callbacks temporarily, not a second back stack.

## Forbidden anti-patterns

- Raw string route construction or serialized domain objects.
- Public mutable stacks passed throughout the UI.
- Direct stack mutation from screens or effects.
- A single enum plus composition switching masquerading as tab navigation.
- One shared stack that loses or cross-contaminates tab history.
- Global singleton ViewModels for entry-owned workflows.
- An unbounded or replaying navigation command stream.
- Navigating from a stale `LaunchedEffect` without an active-entry/session check.
- A monolithic host that constructs every ViewModel and owns every feature effect.
- Persisting protected screen UI state where authentication-root rendering can restore it.
- Platform gestures that can bypass the Back policy.
- Private recognizer/class-name inspection or global fixed navigation tweens.
- Silently swallowing invalid destinations.

## Review checklist

- [ ] Exactly one state owner and one production mutation API exist.
- [ ] Every route is typed, serializable if restored, minimal, valid, and registered.
- [ ] Every route maps to exactly one owning stack unless a deliberate cross-stack rule is documented.
- [ ] All production mutations flow through `AppNavigator`.
- [ ] Stale effects, duplicate input, and old-session tokens cannot mutate current state.
- [ ] Every top-level stack survives switching and configuration recreation.
- [ ] Reselect behavior is deterministic and documented.
- [ ] Route adapters are feature-owned and root composition remains small.
- [ ] Entry-owned ViewModels are entry scoped and cleaned up.
- [ ] Back, restoration, and external navigation satisfy their focused skills.
- [ ] Android, iOS, and Desktop render the same logical state.
- [ ] Obsolete engines, gestures, and direct stack paths are deleted.

## Verification gate

Use `$navigation-verification`. At minimum require route serialization/registry tests, state-machine tests, stack/tab tests, lifecycle and rapid-input tests, restoration/security tests, external-input tests, every platform Back path, Android builds, iOS Release linkage, static analysis, dependency verification, and physical iOS gesture validation.

## Verified version boundary

This architecture was integrated and executable-tested with:

- Compose Multiplatform 1.11.1;
- AndroidX Navigation 3 runtime 1.1.4;
- JetBrains Navigation 3 UI 1.1.1;
- Navigation Event runtime 1.1.2;
- JetBrains Navigation Event Compose UIKit 1.0.1;
- Lifecycle 2.11.0;
- Koin 4.2.2.

The invariants above are stable. Serializer types, entry decorators, `NavDisplay` parameters, predictive-transition helpers, UIKit gesture installation, and lifecycle APIs are version-specific. Inspect current official source/docs and rerun all navigation tests after upgrading any of these dependencies.
