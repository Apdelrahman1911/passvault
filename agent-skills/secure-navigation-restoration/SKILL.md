---
name: secure-navigation-restoration
description: "Design, implement, or audit fail-closed navigation for apps with authentication or protected content. Use for locked versus unlocked roots, secure root replacement, session-bound navigation, quarantined process-restored destinations, post-authentication validation, stale identifier handling, sensitive entry cleanup, or preventing restored screens from flashing before unlock."
---

# Secure Navigation Restoration

Restore useful route history without ever rendering protected content before successful authentication and validation.

## Security invariant

```text
restored route-only snapshot
  -> locked/authentication root
  -> successful authentication and current session
  -> validate every protected route
  -> activate accepted stacks
```

Never reverse this order, even for one frame.

## Threat model

Assume restored state may be:

- stale after data deletion or revocation;
- malformed or oversized;
- cross-tab or wrong-root;
- from a prior authenticated session;
- decoded before repositories are available;
- replayed after lock or account/vault replacement;
- inspected in logs/crash reports;
- associated with saveable UI state or ViewModels that contain secrets.

Navigation restoration is untrusted input. Serialization success does not establish authorization.

## Separate roots from protected history

Model at least authentication/onboarding and protected main roots. The current root is derived from current security/session state and should not be process-restored into “unlocked.”

Persist only a non-secret route snapshot:

```kotlin
@Serializable
data class ProtectedNavigationSnapshot(
    val selectedTab: Tab = Tab.HOME,
    val home: List<AppRoute> = listOf(HomeRoute),
    val settings: List<AppRoute> = listOf(SettingsRoute),
)
```

Do not put passwords, usernames, tokens, decrypted content, full records, search text, unsaved editor values, or cryptographic state in routes.

Identifiers may still be sensitive metadata. Persist only those required by the product's restoration policy and protect OS storage/backups appropriately.

## Live and quarantine state

Maintain two conceptual locations:

- **live stacks**: entries the renderer may expose in the authenticated session;
- **quarantined stacks/snapshot**: route identifiers retained for possible post-auth restoration, never rendered while locked.

On lock/session failure:

1. increment/invalidate the navigation session generation;
2. copy an allowed route-only snapshot to quarantine if restoration is desired;
3. reset live stacks to safe roots;
4. replace the root with Unlock/authentication;
5. clear entry-scoped and application-scoped sensitive UI state;
6. clear sensitive clipboard/platform state as required;
7. make old callbacks/tokens unable to mutate the new session.

Quarantine must not retain Nav3 saveable-state or ViewModel-store decorators. Keeping only route keys avoids keeping protected screen state alive behind the lock screen.

## Unlock activation

After successful authentication:

1. mark a new/current unlocked session;
2. read the quarantined snapshot;
3. validate structure and ownership;
4. validate referenced entities through authorized repositories;
5. build a sanitized snapshot;
6. activate it atomically as the main root;
7. discard quarantine;
8. refresh screen/domain state in the new session.

Never render the protected root while asynchronous validation is pending.

## Validator design

For each stack:

- enforce a small maximum depth;
- require its canonical root;
- reject invalid route argument shapes;
- require every route to belong to that stack;
- verify entity IDs exist and remain authorized;
- cache repeated lookup results for one validation pass;
- propagate coroutine cancellation;
- fail closed on repository errors;
- truncate at the first invalid child or reset to root according to documented policy.

```kotlin
suspend fun validateStack(tab: Tab, source: List<AppRoute>): List<AppRoute> {
    if (source.size > MAX_DEPTH || source.firstOrNull() != tab.rootRoute()) {
        return listOf(tab.rootRoute())
    }
    val accepted = mutableListOf(tab.rootRoute())
    for (route in source.drop(1)) {
        if (!route.isAllowedIn(tab) || !route.isStillAuthorized()) break
        accepted += route
    }
    return accepted
}
```

Preserving a valid prefix makes Back predictable without activating a stale leaf. For some high-risk products, resetting the entire stack is preferable; document the choice.

## Session-bound navigator

Navigation tokens should carry a session generation in addition to active route/root/stack/lifecycle state. Increment it whenever an unlocked session ends or protected ownership changes.

Reject:

- effects emitted by a previous screen;
- interactive Back completion after lock;
- delayed external intent validation from an earlier session;
- callbacks captured before root replacement;
- mutations while the host is inactive.

Do not queue protected navigation commands for replay after unlock unless they are represented as one explicitly guarded pending external intent.

## Onboarding and reset

Onboarding/new-vault/reset transitions normally discard both live and quarantined protected histories. An old destination must never reappear after a new vault/account is created.

Keep onboarding routes in a separate authentication stack. Validate the permitted onboarding sequence and normalize unexpected restored auth routes to the onboarding/unlock root.

## Sensitive state cleanup

Entry-scoped ViewModels should wipe secrets in `onCleared`. Lock handling must also clear sensitive state in long-lived ViewModels/services because entry teardown timing is not a complete security boundary.

Define cleanup explicitly for:

- editors and generated passwords;
- revealed credentials and OTP values;
- import/export buffers and file handles;
- search queries and filtered secret results;
- clipboard ownership;
- dialogs containing secrets;
- pending external navigation;
- attachment/temp-file workflows if present.

Preserve only state required by an in-progress security transaction whose design explicitly demands it, such as a carefully controlled restore-lock transition.

## Configuration versus process recreation

- Configuration recreation may retain the current logical authenticated state while the same process/session remains valid.
- Process recreation/cold start begins fail closed at authentication, regardless of restored route keys.
- Platform snapshots/app switcher privacy are a separate protection and must also be implemented.

Do not rely on Compose restoration alone to distinguish these cases; make the root/session bootstrap policy explicit.

## External destinations

An authentication-gated link may be held as one typed, bounded pending envelope. After unlock, validate it against the new session and construct a deterministic stack. On onboarding/reset or invalid authorization, reject and forget it.

Load `$typed-external-navigation` for the full pipeline.

## Forbidden anti-patterns

- Restoring an `UNLOCKED` root from saved state.
- Rendering protected entries behind a lock overlay.
- Quarantining saveable screen state or ViewModel stores with routes.
- Persisting domain objects or secret arguments.
- Trusting a syntactically valid ID without repository authorization.
- Retaining an unbounded stack.
- Reusing old entry callbacks after session replacement.
- Continuing on validation exceptions.
- Logging restored route payloads or identifiers indiscriminately.
- Keeping pending external events through onboarding/account reset.
- Calling destructive migration/reset merely because route state is stale.

## Required tests

- Cold/process-restored launch shows only authentication before unlock.
- Configuration recreation preserves allowed current navigation.
- Lock quarantines route keys, resets live stacks, and invalidates tokens.
- Unlock activates only a validated snapshot.
- Wrong root, cross-tab route, malformed ID, stale/deleted entity, unauthorized entity, oversized stack, and repository failure all fail safely.
- Valid prefix truncation behaves as documented.
- New onboarding/vault reset destroys old quarantine.
- Session failure during validation cannot expose or activate protected state.
- Background/lock during interactive Back rejects completion.
- Old ViewModels/effects cannot mutate new-session navigation.
- Sensitive state cleanup runs for entry and application scopes.
- Auth-gated external intent is validated in the new session exactly once.
- No protected entry/saveable state is composed during locked root tests.

## Review checklist

- [ ] Process restoration never restores unlocked authority.
- [ ] Quarantine contains minimal route keys only.
- [ ] Validation happens after authentication and before rendering.
- [ ] Validation is bounded, ownership-aware, repository-aware, cancellation-safe, and fail closed.
- [ ] Session generations invalidate stale mutations.
- [ ] Root replacement and cleanup are centralized.
- [ ] Onboarding/reset clears prior protected history.
- [ ] External pending state is bounded and guarded.
- [ ] Tests prove no protected-frame exposure, not merely eventual redirect.

## Version boundary

The verified reference used Compose Multiplatform 1.11.1, Navigation 3 runtime 1.1.4, Navigation 3 UI 1.1.1, Lifecycle 2.11.0, and Koin 4.2.2. The fail-closed sequencing and minimal-state rules are stable. Nav3 serialization, saveable-state holders, ViewModel entry decorators, and platform recreation behavior are version-specific and must be revalidated after upgrades.
