---
name: typed-external-navigation
description: "Implement or audit deep links, URLs, notifications, shortcuts, and platform intents using a strict raw-input parser, typed external intents, authentication/onboarding guards, entity validation, deterministic stack construction, bounded delivery semantics, and a central Navigation 3 navigator in Kotlin Multiplatform applications."
---

# Typed External Navigation

Treat every external navigation request as untrusted data and turn it into deterministic application state through one typed pipeline.

## Required pipeline

```text
raw platform input
  -> strict normalization and parser
  -> typed ExternalNavigationIntent
  -> delivery/lifecycle/session guard
  -> authentication/onboarding/authorization guard
  -> entity validation
  -> deterministic stack builder
  -> AppNavigator
```

No earlier stage may mutate a back stack.

## Stable invariants

1. Platform adapters provide normalized data, not arbitrary stack operations.
2. The common parser is strict, pure, and exhaustively tested.
3. Parsed intent types contain bounded identifiers, never executable callbacks or domain objects.
4. Malformed, unsupported, stale, duplicate, or unauthorized input leaves navigation unchanged.
5. Every accepted intent maps to an owning root/tab and canonical Back stack.
6. Delivery identity is distinct from destination identity so two intentional identical links can both work.
7. Pending delivery is bounded, observable, non-replaying, and session-aware.
8. External navigation obeys the same dirty/operation/Back/leave policy as in-app navigation.
9. Do not register production schemes, universal links, or associated domains until product-level contracts and security validation exist.

## Define boundaries

### Raw input

Platform code should decode its native object, enforce the approved scheme/host contract, and pass normalized components:

```kotlin
data class RawExternalNavigationInput(
    val deliveryId: String,
    val source: ExternalSource,
    val pathSegments: List<String>,
)
```

Be explicit whether segments are decoded or encoded. Do not decode twice. Reject scheme, host, query, or fragment data at the common boundary unless the contract explicitly uses them.

### Typed intent

```kotlin
sealed interface ExternalNavigationIntent {
    data class Item(val itemId: String) : ExternalNavigationIntent
    data object Settings : ExternalNavigationIntent
    data object Import : ExternalNavigationIntent
}
```

Keep intent types independent of UI implementation so URLs, notifications, shortcuts, and tests share the same path.

## Strict parsing

Validate before lookup:

- delivery ID exists, is length bounded, and uses an allowlisted character set;
- segment count and each segment length are bounded;
- segments contain no control characters, separators, traversal tokens, or unexpected encoding markers;
- destination grammar is exact;
- identifiers use canonical form;
- no unknown query parameter changes navigation silently.

Return a typed rejection, not an exception for malformed user input:

```kotlin
sealed interface ParseResult {
    data class Accepted(val envelope: Envelope) : ParseResult
    data class Rejected(val error: ParseError) : ParseResult
}
```

Do not use permissive prefix matching such as `startsWith("item")`.

## Delivery semantics

For ordinary application links, keep at most one pending envelope. A conflated `StateFlow` is useful because:

- cold-start/authentication can observe the current request;
- new input replaces stale pending input without a backlog;
- there is no replay after consumption;
- lifecycle changes do not require an unbounded queue.

Give every platform event a unique delivery ID. Maintain a small bounded set of recently handled IDs to reject duplicate native delivery. When replacing a pending request, mark the superseded delivery handled/stale so an older validator cannot apply it later.

If the product truly requires processing every event, design a durable bounded queue with explicit ordering, expiration, storage security, and user-visible conflict semantics. Never default to an unlimited channel.

## Guards and lifecycle

On submission:

- reject during onboarding/account reset unless the intent is explicitly allowed;
- defer behind authentication only when product policy allows it;
- do not apply while the host is inactive;
- bind asynchronous validation to the current pending envelope and session;
- recheck pending identity immediately before mutation;
- clear/reject pending input when session ownership changes.

An observable pending envelope plus one application effect keyed by envelope, root, session, and host state prevents the race created by two independent collectors trying to apply the same event.

## Validate target data

After authentication, validate referenced entity IDs through authorized repositories. Propagate coroutine cancellation and fail closed on repository errors. Never treat canonical UUID syntax as proof that the current user/vault may open it.

Use a validator independent of screen construction so it is unit-testable.

## Deterministic stack builder

Every intent maps exhaustively to:

```kotlin
data class ExternalStackTarget(
    val destination: TopLevelDestination,
    val route: AppRoute? = null,
    val resetStack: Boolean = true,
)
```

Examples:

```text
Item(valid ID) -> HOME / ItemDetail(ID)
Settings       -> SETTINGS
Import         -> SETTINGS / Data / Import   (if Data is a real route)
```

The target must satisfy route ownership and argument validation. Resetting to a canonical target path usually gives reliable Back semantics. If preserving the target tab's prior history is a product requirement, document and test it explicitly.

## Interaction with guarded screens

External navigation is forward navigation. Before leaving the visible entry, consult the same leave/Back policy used by tab and in-app actions. A dirty editor or blocked operation must not be bypassed by a notification.

Choose an explicit policy: defer, reject, or open a confirmation. Do not silently discard user work.

## Platform registration

Only register:

- Android intent filters whose schemes/hosts/paths are approved and verified;
- iOS URL types or associated domains backed by an actual product contract and hosted association file;
- notification payload keys with a versioned schema.

Keep parsing architecture ready before registration. Do not invent public URLs or use a broad custom scheme just because the internal route exists.

## Forbidden anti-patterns

- Passing a raw URI directly to `navigator.push`.
- A string-to-screen `when` in platform code.
- Letting external input choose arbitrary class/route names.
- Storing a full notification payload or domain object in a route.
- Applying links before authentication/authorization completes.
- Two collectors independently applying the same pending intent.
- An unlimited/replaying navigation event stream.
- Deduplicating forever by destination rather than bounded delivery ID.
- Appending the target to the currently selected arbitrary tab.
- Ignoring dirty editor/blocked operation policy.
- Broad intent filters or associated domains without a defined contract.
- Logging sensitive link arguments.

## Required tests

- Every supported path parses to the exact typed intent.
- Empty, overlong, malformed, encoded separator, control-character, traversal-like, unsupported, extra-segment, and invalid-ID inputs reject without mutation.
- Every intent builds the exact owning stack.
- Wrong-stack/invalid route target is rejected.
- Duplicate delivery ID applies once.
- Two intentional identical destinations with different IDs can apply separately.
- Rapid inputs conflate/replace according to policy with no backlog.
- A superseded asynchronous validator cannot apply stale input.
- Locked input defers, then validates/applies after unlock.
- Onboarding/reset rejects and clears pending input.
- Authorization/stale entity/repository failure rejects safely.
- Background/inactive host defers without replay race.
- Session change invalidates old pending validation.
- Dirty/blocked visible entry follows the defined leave policy.
- Deep-link Back history is deterministic in every tab/layout.
- Android/iOS platform adapters enforce the public URL contract.

## Review checklist

- [ ] Raw, typed, guard, validator, stack-builder, and mutation stages are separate.
- [ ] Parser grammar and bounds are explicit.
- [ ] Delivery state is bounded and deduplicated by event ID.
- [ ] Only one observable application path exists.
- [ ] Auth, onboarding, lifecycle, session, entity, and leave guards run before mutation.
- [ ] Every intent owns a deterministic stack.
- [ ] Invalid input never partially mutates navigation.
- [ ] Platform URL/notification contracts are intentional and least-privilege.
- [ ] Sensitive arguments are not logged.

## Version boundary

The pipeline is framework-stable. The verified navigation integration used Compose Multiplatform 1.11.1 and Navigation 3 runtime 1.1.4/UI 1.1.1. Android intent, iOS universal-link, notification, lifecycle, and Nav3 stack APIs are platform/version-specific and must be rechecked when those dependencies or public contracts change.
