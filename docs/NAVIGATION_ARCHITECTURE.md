# PassVault Navigation 3 architecture

## Status and verified dependency surface

This document describes the implemented PassVault navigation architecture, not the Kira reference
design in `NAVIGATION_IMPLEMENTATION.md`.

The implementation is compiled and tested against:

- Compose Multiplatform `1.11.1`;
- AndroidX Navigation 3 runtime `1.1.4`;
- JetBrains Navigation 3 UI `1.1.1`;
- AndroidX NavigationEvent runtime `1.1.2`;
- JetBrains NavigationEvent Compose for UIKit `1.0.1`;
- Lifecycle Navigation 3 / ViewModel `2.11.0`;
- Koin `4.2.2`.

The architecture rules in this document are stable design invariants. iOS gesture wiring,
transition defaults, `didMoveToWindow()` refresh behavior, and artifact compatibility are
version-specific and must be re-inspected after upgrading Compose, Navigation 3, NavigationEvent,
Lifecycle, or Koin.

## One source of truth

```text
platform Back / edge gesture / keyboard / toolbar / external input
    -> AppNavigator + NavigationBackCoordinator
    -> BackDisposition and security/lifecycle/session validation
    -> PassVaultNavigationState
    -> one active NavDisplay
    -> feature route adapter
    -> state-driven screen
```

There is no second `NavController`, native navigation stack, feature-owned back stack, or direct
screen mutation of a stack. Compact, expanded, tablet, and Desktop shells present the same logical
state.

## Routes and ownership

`core/navigation/.../NavigationKeys.kt` defines one serializable sealed `PassVaultRoute : NavKey`
hierarchy. Routes contain only stable non-secret identifiers. Every concrete route has:

- an explicit `@SerialName`;
- one `PassVaultRouteKind` identity;
- exactly one registered feature adapter;
- a serialization round-trip test;
- a top-level stack ownership rule.

`TopLevelDestination.isAllowedIn` is the central ownership policy. The Home stack owns normal vault
routes. Generator and two-factor stacks may own credential detail/edit routes opened from those
tools, so Back returns to the originating tool. Settings owns settings and backup routes. Malformed
credential/folder IDs and cross-stack destinations are rejected before entering a live stack.

## Application-owned state and tabs

`PassVaultNavigationState` owns:

- the authentication/onboarding stack;
- the selected top-level destination;
- independent Home, Generator, Two-factor, and Settings stacks;
- matching protected quarantine stacks;
- the authentication versus main root.

`rememberNavigationComposition` supplies serializable `NavBackStack` instances. Switching tabs
preserves every other stack. Reselecting the selected tab deterministically pops only that tab to
its root. External destinations reset and rebuild only their owning stack, giving deterministic
Back behavior. Saveable UI and entry ViewModel decorators are attached to live entries; quarantine
holds route keys only.

## AppNavigator mutation contract

`AppNavigator` is the only production mutation API. It implements push, single-top/deduplicating
push, pop, guarded pop completion, pop-to-root, current-entry replacement, tab selection,
cross-tab opening, root replacement, and external navigation.

Every screen-originated mutation carries a `NavigationToken` that binds it to:

- the current route;
- the active root and top-level stack;
- the current unlocked-session generation;
- the entry lifecycle state;
- the resumed navigation host.

This rejects double clicks after the first mutation, effects from inactive entries, effects from a
previous session, and navigation while the host is detached/backgrounded. Expected races are
reported as typed rejections. Invalid destinations and programmer errors remain visible rather than
being silently swallowed.

App commands use a bounded, non-replaying `SharedFlow` (capacity 16, drop oldest). External input
uses one observable conflated pending value. A single auth/root/lifecycle-gated coroutine validates
and applies it, so two observers cannot apply one delivery concurrently and stale validation cannot
mutate the graph.

## Unified Back policy

Every active adapter registers one `BackDisposition`:

| Disposition | Meaning | Navigation result |
| --- | --- | --- |
| `PopNow` | Destination is safe to leave | One authorized pop |
| `HandleInPlace` | Local UI must close first | Run the destination handler; do not pop |
| `Blocked` | An active operation forbids leaving | Consume Back; do not reveal or pop |
| `ExitApplication` | The logical application root is active | Let the platform decide whether to exit |

`NavigationBackCoordinator` applies that same policy to NavigationEvent/system Back, app-bar Back,
iOS interactive completion, Desktop Escape/Back, tab/forward actions, and programmatic guarded
completion.

For `HandleInPlace` or `Blocked`, `NavDisplay` receives only the current `NavEntry`. Navigation 3
therefore has no previous scene to reveal and cannot begin a visual interactive pop. A separate
enabled `NavigationBackHandler` consumes the platform event and performs only the allowed local
action. For `PopNow`, the full entry list is supplied and Navigation 3 owns the interactive gesture
and transition. A stale or duplicate completion cannot pop a second entry.

The guarded policies cover dirty/busy credential editors, dialogs and confirmations, search/local
vault UI, settings operations, and backup/import/export operations. New screens with transient or
blocking state must register an explicit policy before forward navigation is enabled.

## Route adapters and ViewModel lifetime

`shared/navigation/adapters/` is split by feature family. Adapters own route arguments, ViewModel
resolution, lifecycle-bound effect collection, navigation callbacks, and Back registration. Feature
screens receive state and callbacks and do not receive `AppNavigator` or a stack.

Credential, generator, health, and two-factor ViewModels are entry scoped through Navigation 3's
`ViewModelStoreNavEntryDecorator` and Koin `viewModel` definitions. Popping an entry destroys its
owner and calls sensitive cleanup. Vault, settings, backup, onboarding, and unlock state remain
application/session scoped because they coordinate root flows and centralized lock cleanup.

Effect collectors start before initial load/create actions and validate the current navigation
token before navigation or sensitive clipboard/URL side effects. Leaving an entry cancels its
collector.

## Restoration and security

The displayed root is intentionally not process-restored. A cold or restored process starts at
onboarding/unlock. Serializable protected route keys may be restored, but are quarantined and have
no active ViewModel/saveable UI owner.

```text
restored route-only stacks
    -> authentication root remains visible
    -> successful vault unlock
    -> validate route type, stack owner, depth, identifier, and repository existence
    -> truncate at the first stale/invalid route
    -> activate the validated stacks
```

The maximum restored depth is 32 entries per stack. Repository errors fail closed. Deleted,
malformed, cross-tab, or unauthorized destinations fall back to the owning root. Lock/session-reset
increments the session generation, quarantines routes, resets live stacks, destroys entry-scoped
state, and invokes centralized sensitive-state and clipboard cleanup. Onboarding discards protected
restoration entirely.

## External navigation

No production URL scheme, universal-link domain, or arbitrary deep-link contract is registered.
The internal pipeline is ready for a future product-defined platform adapter:

```text
decoded path segments + unique delivery ID
    -> strict parser
    -> typed ExternalNavigationIntent
    -> one conflated pending envelope
    -> onboarding/auth/lifecycle guard
    -> repository validation when required
    -> deterministic owning-stack builder
    -> AppNavigator
```

The parser rejects control characters, separators, encoded path tricks, malformed delivery IDs,
unknown destinations, oversized segments, and non-canonical credential IDs. Onboarding rejects and
does not retain external navigation. Locked sessions retain only the newest pending typed intent.

## Platform behavior

Android uses Navigation 3 / NavigationEvent for system and predictive Back. Desktop routes Escape
and the Back key through the same coordinator. Both platforms retain their Navigation 3 default
forward, pop, and predictive-pop transitions.

On iOS, `ComposeUIViewController` owns one start-edge recognizer. End-edge navigation remains
disabled, preventing two physical edges from acting as Back. UIKit
`semanticContentAttribute` and Compose `LocalLayoutDirection` are synchronized:

- LTR: physical left edge is Back;
- RTL/Arabic: physical right edge is Back.

On a runtime direction change, the host view is refreshed with public `didMoveToWindow()` only
after its semantic direction actually changes. No recognizer is discovered by class name, reordered,
replaced, or wrapped in a competing `UINavigationController`.

For the pinned versions, Compose UI owns gesture progress and the velocity/progress completion
decision. Navigation 3 UI owns transition seeking, cancellation, completion, and remaining
settlement. PassVault supplies only the public `NavDisplay` transition specifications:

- a `200 ms` iOS-only base duration for push, regular pop, and predictive pop;
- Navigation 3's regular iOS cubic curve for push and toolbar/programmatic pop;
- linear predictive-pop content progress so interactive dragging remains tied to the finger;
- physical motion derived from `LocalLayoutDirection` or the gesture's reported edge.

The explicit duration is necessary because Navigation Compose 2.9.2 uses a `200 ms` iOS default,
while Navigation 3 UI 1.1.1 uses `500 ms`. Compose UI uses velocity to choose complete versus
cancel, but it does not pass velocity magnitude to `NavDisplay`; Navigation 3 calculates settlement
from the remaining transition fraction. At 25 percent progress, a completion is therefore bounded
to about `150 ms` with PassVault's base instead of about `375 ms` with the 1.1.1 default. No
application coroutine, recognizer hook, second animation, or stack mutation owns settlement.

These implementation details, including default durations, gesture thresholds,
cancellation/failed-recognizer behavior, and transition APIs, must be checked again against
dependency source and on hardware after upgrades.

## Adding a destination

1. Add a minimal serializable route and stable serial name.
2. Add its `PassVaultRouteKind` and example value.
3. Assign its allowed top-level stack.
4. Add one feature adapter and include its kind in that adapter registry.
5. Choose entry-scoped or application-scoped state deliberately.
6. Register `BackDisposition`, including transient and active-operation behavior.
7. Route all mutations through `AppNavigator` with the active token.
8. Add serialization, registry, stack, Back, restoration, security, and external-stack tests as
   applicable.
9. Run the full navigation and platform verification matrix.

## Non-negotiable invariants

- Protected content is never displayed before authentication and post-auth validation.
- No platform can visually or logically pop around `BackDisposition`.
- Screens never mutate stacks or receive the navigator.
- Each top-level destination owns one independent durable stack.
- Navigation commands are bounded/non-replaying and every effect is entry/session/lifecycle bound.
- There is one navigation engine and one active `NavDisplay`.
- Private Compose/UIKit implementation details are never used as integration APIs.
