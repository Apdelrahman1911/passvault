package com.passvault.core.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class NavigationAccess {
    ONBOARDING,
    LOCKED,
    UNLOCKED,
}

class NavigationToken internal constructor(
    val route: PassVaultRoute,
    internal val root: NavigationRoot,
    internal val destination: TopLevelDestination,
    internal val sessionGeneration: Long,
    internal val entryResumed: Boolean,
)

enum class NavigationRejection {
    HostInactive,
    EntryInactive,
    StaleSession,
    VaultLocked,
    Duplicate,
    AtRoot,
    InvalidDestination,
    UnauthorizedExternalInput,
    DuplicateExternalDelivery,
    StaleExternalDelivery,
}

sealed interface NavigationMutation {
    data object Applied : NavigationMutation
    data object Deferred : NavigationMutation
    data class Rejected(val reason: NavigationRejection) : NavigationMutation
}

/** The sole mutation API for [PassVaultNavigationState]. */
@Suppress("TooManyFunctions", "ReturnCount")
class AppNavigator(
    val state: PassVaultNavigationState,
) {
    private var access: NavigationAccess = NavigationAccess.LOCKED
    private var sessionGeneration = 0L
    private var hostResumed = false
    private val _pendingExternal = MutableStateFlow<ExternalNavigationEnvelope?>(null)
    val pendingExternalState: StateFlow<ExternalNavigationEnvelope?> = _pendingExternal.asStateFlow()
    private val handledExternalDeliveries = ArrayDeque<String>()

    fun setHostResumed(resumed: Boolean) {
        hostResumed = resumed
    }

    fun normalizeBootstrap(initialRoute: PassVaultRoute) {
        state.normalizeBootstrap(initialRoute)
        access = if (initialRoute == AuthRoute.Onboarding) {
            NavigationAccess.ONBOARDING
        } else {
            NavigationAccess.LOCKED
        }
    }

    fun requireAuthentication() {
        if (access == NavigationAccess.UNLOCKED) sessionGeneration++
        access = NavigationAccess.LOCKED
        state.quarantineForAuthentication()
        state.replaceAuthenticationRoot(AuthRoute.Unlock)
    }

    fun requireOnboarding() {
        if (access == NavigationAccess.UNLOCKED) sessionGeneration++
        access = NavigationAccess.ONBOARDING
        _pendingExternal.value?.deliveryId?.let(::rememberExternalDelivery)
        _pendingExternal.value = null
        state.resetForOnboarding()
    }

    fun markSessionUnlocked() {
        access = NavigationAccess.UNLOCKED
    }

    fun activateUnlocked(snapshot: MainNavigationSnapshot? = null): NavigationMutation {
        if (access != NavigationAccess.UNLOCKED) {
            return NavigationMutation.Rejected(NavigationRejection.VaultLocked)
        }
        if (state.root.value != NavigationRoot.MAIN) state.activateMain(snapshot)
        return NavigationMutation.Applied
    }

    fun createToken(route: PassVaultRoute, entryResumed: Boolean): NavigationToken = NavigationToken(
        route = route,
        root = state.root.value,
        destination = state.selectedDestination.value,
        sessionGeneration = sessionGeneration,
        entryResumed = entryResumed,
    )

    fun currentToken(entryResumed: Boolean = hostResumed): NavigationToken =
        createToken(state.currentRoute(), entryResumed)

    fun isCurrent(token: NavigationToken, requireResumed: Boolean = true): Boolean =
        validateToken(token, requireResumed) == null

    fun canPop(token: NavigationToken = currentToken()): Boolean =
        isCurrent(token, requireResumed = false) && state.activeStack().size > 1

    fun previousRoute(token: NavigationToken): PassVaultRoute? =
        if (isCurrent(token, requireResumed = false)) state.previousRoute() else null

    fun push(route: PassVaultRoute, token: NavigationToken): NavigationMutation {
        require(route.requiresUnlockedVault()) { "Use pushAuthentication for authentication routes" }
        validateToken(token)?.let { return NavigationMutation.Rejected(it) }
        requireUnlockedMain()?.let { return NavigationMutation.Rejected(it) }
        if (!route.hasValidArguments() || !route.isAllowedIn(state.selectedDestination.value)) {
            return NavigationMutation.Rejected(NavigationRejection.InvalidDestination)
        }
        return if (state.pushMain(route)) {
            NavigationMutation.Applied
        } else {
            NavigationMutation.Rejected(NavigationRejection.Duplicate)
        }
    }

    fun pushSingleTop(route: PassVaultRoute, token: NavigationToken): NavigationMutation = push(route, token)

    fun pushAuthentication(route: AuthRoute, token: NavigationToken): NavigationMutation {
        validateToken(token)?.let { return NavigationMutation.Rejected(it) }
        if (state.root.value != NavigationRoot.AUTHENTICATION) {
            return NavigationMutation.Rejected(NavigationRejection.InvalidDestination)
        }
        if (state.currentRoute() == route) {
            return NavigationMutation.Rejected(NavigationRejection.Duplicate)
        }
        state.pushAuthentication(route)
        return NavigationMutation.Applied
    }

    fun pop(token: NavigationToken): NavigationMutation {
        validateToken(token)?.let { return NavigationMutation.Rejected(it) }
        val popped = when (state.root.value) {
            NavigationRoot.AUTHENTICATION -> state.popAuthentication()
            NavigationRoot.MAIN -> state.popMain()
        }
        return if (popped) NavigationMutation.Applied else NavigationMutation.Rejected(NavigationRejection.AtRoot)
    }

    /** Completes a Back decision already authorized by the active destination ViewModel. */
    fun popAfterGuard(token: NavigationToken): NavigationMutation {
        validateToken(token, requireResumed = false)?.let { return NavigationMutation.Rejected(it) }
        if (!hostResumed) return NavigationMutation.Rejected(NavigationRejection.HostInactive)
        return popInternal()
    }

    fun replaceCurrentWith(route: PassVaultRoute, token: NavigationToken): NavigationMutation {
        require(route.requiresUnlockedVault()) { "A main entry can only be replaced by a protected route" }
        validateToken(token, requireResumed = false)?.let { return NavigationMutation.Rejected(it) }
        requireUnlockedMain()?.let { return NavigationMutation.Rejected(it) }
        if (!route.hasValidArguments() || !route.isAllowedIn(state.selectedDestination.value)) {
            return NavigationMutation.Rejected(NavigationRejection.InvalidDestination)
        }
        state.replaceCurrentMain(route)
        return NavigationMutation.Applied
    }

    fun popThenEnsure(route: PassVaultRoute, token: NavigationToken): NavigationMutation {
        require(route.requiresUnlockedVault()) { "A main entry can only reveal a protected route" }
        validateToken(token, requireResumed = false)?.let { return NavigationMutation.Rejected(it) }
        requireUnlockedMain()?.let { return NavigationMutation.Rejected(it) }
        if (!route.hasValidArguments() || !route.isAllowedIn(state.selectedDestination.value)) {
            return NavigationMutation.Rejected(NavigationRejection.InvalidDestination)
        }
        state.popMain()
        if (state.currentRoute() != route) state.pushMain(route)
        return NavigationMutation.Applied
    }

    fun popToRoot(token: NavigationToken): NavigationMutation {
        validateToken(token)?.let { return NavigationMutation.Rejected(it) }
        if (state.root.value != NavigationRoot.MAIN) {
            return NavigationMutation.Rejected(NavigationRejection.InvalidDestination)
        }
        return if (state.popMainToRoot()) {
            NavigationMutation.Applied
        } else {
            NavigationMutation.Rejected(NavigationRejection.AtRoot)
        }
    }

    fun selectTab(destination: TopLevelDestination, token: NavigationToken): NavigationMutation {
        validateToken(token)?.let { return NavigationMutation.Rejected(it) }
        requireUnlockedMain()?.let { return NavigationMutation.Rejected(it) }
        if (state.selectedDestination.value == destination) {
            return if (state.popMainToRoot(destination)) {
                NavigationMutation.Applied
            } else {
                NavigationMutation.Rejected(NavigationRejection.AtRoot)
            }
        }
        state.select(destination)
        return NavigationMutation.Applied
    }

    fun openTabRoot(
        destination: TopLevelDestination,
        resetStack: Boolean,
        token: NavigationToken,
    ): NavigationMutation = openInTab(destination, route = null, resetStack = resetStack, token = token)

    fun openInTab(
        destination: TopLevelDestination,
        route: PassVaultRoute?,
        resetStack: Boolean,
        token: NavigationToken,
    ): NavigationMutation {
        require(route == null || route.requiresUnlockedVault()) {
            "Authentication routes cannot be opened in a main tab"
        }
        validateToken(token)?.let { return NavigationMutation.Rejected(it) }
        requireUnlockedMain()?.let { return NavigationMutation.Rejected(it) }
        if (route != null && (!route.hasValidArguments() || !route.isAllowedIn(destination))) {
            return NavigationMutation.Rejected(NavigationRejection.InvalidDestination)
        }
        return if (state.openIn(destination, route, resetStack)) {
            NavigationMutation.Applied
        } else {
            NavigationMutation.Rejected(NavigationRejection.Duplicate)
        }
    }

    fun defaultBackDisposition(conservativeGuard: Boolean = false): BackDisposition {
        if (!hostResumed) return BackDisposition.Blocked
        val route = state.currentRoute()
        if (state.activeStack().size > 1) {
            if (conservativeGuard && route.requiresExplicitBackRegistration()) {
                return BackDisposition.Blocked
            }
            return BackDisposition.PopNow
        }
        return if (
            state.root.value == NavigationRoot.MAIN &&
            state.selectedDestination.value != TopLevelDestination.HOME
        ) {
            BackDisposition.HandleInPlace
        } else {
            BackDisposition.ExitApplication
        }
    }

    fun handleDefaultInPlaceBack(token: NavigationToken): NavigationMutation {
        validateToken(token, requireResumed = false)?.let { return NavigationMutation.Rejected(it) }
        if (
            state.root.value == NavigationRoot.MAIN &&
            state.activeStack().size == 1 &&
            state.selectedDestination.value != TopLevelDestination.HOME
        ) {
            state.select(TopLevelDestination.HOME)
            return NavigationMutation.Applied
        }
        return NavigationMutation.Rejected(NavigationRejection.AtRoot)
    }

    fun submitExternal(envelope: ExternalNavigationEnvelope): NavigationMutation {
        if (envelope.deliveryId in handledExternalDeliveries) {
            return NavigationMutation.Rejected(NavigationRejection.DuplicateExternalDelivery)
        }
        if (access == NavigationAccess.ONBOARDING) {
            return NavigationMutation.Rejected(NavigationRejection.UnauthorizedExternalInput)
        }
        val previous = _pendingExternal.value
        if (previous?.deliveryId == envelope.deliveryId) {
            return NavigationMutation.Rejected(NavigationRejection.DuplicateExternalDelivery)
        }
        previous?.deliveryId?.let(::rememberExternalDelivery)
        _pendingExternal.value = envelope
        return NavigationMutation.Deferred
    }

    fun pendingExternal(): ExternalNavigationEnvelope? = _pendingExternal.value

    fun rejectPendingExternal(deliveryId: String) {
        if (_pendingExternal.value?.deliveryId == deliveryId) _pendingExternal.value = null
        rememberExternalDelivery(deliveryId)
    }

    fun applyValidatedExternal(envelope: ExternalNavigationEnvelope): NavigationMutation {
        val pending = _pendingExternal.value
        if (pending != envelope) {
            val reason = if (pending == null && envelope.deliveryId in handledExternalDeliveries) {
                NavigationRejection.DuplicateExternalDelivery
            } else {
                NavigationRejection.StaleExternalDelivery
            }
            return NavigationMutation.Rejected(reason)
        }
        if (envelope.deliveryId in handledExternalDeliveries) {
            return NavigationMutation.Rejected(NavigationRejection.DuplicateExternalDelivery)
        }
        if (access != NavigationAccess.UNLOCKED || state.root.value != NavigationRoot.MAIN) {
            return NavigationMutation.Deferred
        }
        if (!hostResumed) {
            return NavigationMutation.Deferred
        }
        val target = envelope.intent.toStackTarget()
        if (target.route != null &&
            (!target.route.hasValidArguments() || !target.route.isAllowedIn(target.destination))
        ) {
            return NavigationMutation.Rejected(NavigationRejection.InvalidDestination)
        }
        state.openIn(target.destination, target.route, resetStack = true)
        _pendingExternal.value = null
        rememberExternalDelivery(envelope.deliveryId)
        return NavigationMutation.Applied
    }

    private fun validateToken(
        token: NavigationToken,
        requireResumed: Boolean = true,
    ): NavigationRejection? = when {
        token.sessionGeneration != sessionGeneration -> NavigationRejection.StaleSession
        token.root != state.root.value -> NavigationRejection.EntryInactive
        token.destination != state.selectedDestination.value -> NavigationRejection.EntryInactive
        token.route != state.currentRoute() -> NavigationRejection.EntryInactive
        !hostResumed -> NavigationRejection.HostInactive
        requireResumed && !token.entryResumed -> NavigationRejection.EntryInactive
        else -> null
    }

    private fun requireUnlockedMain(): NavigationRejection? = when {
        access != NavigationAccess.UNLOCKED -> NavigationRejection.VaultLocked
        state.root.value != NavigationRoot.MAIN -> NavigationRejection.InvalidDestination
        else -> null
    }

    private fun popInternal(): NavigationMutation {
        val popped = when (state.root.value) {
            NavigationRoot.AUTHENTICATION -> state.popAuthentication()
            NavigationRoot.MAIN -> state.popMain()
        }
        return if (popped) NavigationMutation.Applied else NavigationMutation.Rejected(NavigationRejection.AtRoot)
    }

    private fun rememberExternalDelivery(deliveryId: String) {
        if (deliveryId in handledExternalDeliveries) return
        if (handledExternalDeliveries.size == MAX_HANDLED_EXTERNAL_DELIVERIES) {
            handledExternalDeliveries.removeFirst()
        }
        handledExternalDeliveries.addLast(deliveryId)
    }

    private companion object {
        const val MAX_HANDLED_EXTERNAL_DELIVERIES = 32
    }
}

private data class ExternalStackTarget(
    val destination: TopLevelDestination,
    val route: PassVaultRoute? = null,
)

private fun ExternalNavigationIntent.toStackTarget(): ExternalStackTarget = when (this) {
    is ExternalNavigationIntent.Credential -> ExternalStackTarget(
        TopLevelDestination.HOME,
        VaultRoute.CredentialDetail(credentialId),
    )
    ExternalNavigationIntent.Generator -> ExternalStackTarget(TopLevelDestination.GENERATOR)
    ExternalNavigationIntent.Health -> ExternalStackTarget(TopLevelDestination.GENERATOR, HealthRoute.Health)
    ExternalNavigationIntent.TwoFactorCodes -> ExternalStackTarget(TopLevelDestination.TWO_FACTOR_CODES)
    ExternalNavigationIntent.Settings -> ExternalStackTarget(TopLevelDestination.SETTINGS)
    ExternalNavigationIntent.SecuritySettings ->
        ExternalStackTarget(TopLevelDestination.SETTINGS, SettingsRoute.Security)
    ExternalNavigationIntent.AppearanceSettings ->
        ExternalStackTarget(TopLevelDestination.SETTINGS, SettingsRoute.Appearance)
    ExternalNavigationIntent.DataSettings ->
        ExternalStackTarget(TopLevelDestination.SETTINGS, SettingsRoute.Data)
    ExternalNavigationIntent.Backup -> ExternalStackTarget(TopLevelDestination.SETTINGS, BackupRoute.Backup)
    ExternalNavigationIntent.Import -> ExternalStackTarget(TopLevelDestination.SETTINGS, BackupRoute.Import)
    ExternalNavigationIntent.Export -> ExternalStackTarget(TopLevelDestination.SETTINGS, BackupRoute.Export)
}

private fun PassVaultRoute.requiresExplicitBackRegistration(): Boolean = when (this) {
    is VaultRoute,
    is SettingsRoute,
    is BackupRoute,
    is HealthRoute,
    is AuthRoute,
    -> true
    is GeneratorRoute,
    is TwoFactorRoute,
    -> false
}
