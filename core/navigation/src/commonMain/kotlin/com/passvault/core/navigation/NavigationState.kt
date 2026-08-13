package com.passvault.core.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

enum class NavigationRoot {
    AUTHENTICATION,
    MAIN,
}

/** A non-sensitive, serializable description of the authenticated navigation stacks. */
@Serializable
data class MainNavigationSnapshot(
    val selectedDestination: TopLevelDestination = TopLevelDestination.HOME,
    val home: List<PassVaultRoute> = listOf(VaultRoute.Vault),
    val generator: List<PassVaultRoute> = listOf(GeneratorRoute.Generator),
    val twoFactorCodes: List<PassVaultRoute> = listOf(TwoFactorRoute.Codes),
    val settings: List<PassVaultRoute> = listOf(SettingsRoute.Settings),
) {
    fun stack(destination: TopLevelDestination): List<PassVaultRoute> = when (destination) {
        TopLevelDestination.HOME -> home
        TopLevelDestination.GENERATOR -> generator
        TopLevelDestination.TWO_FACTOR_CODES -> twoFactorCodes
        TopLevelDestination.SETTINGS -> settings
    }
}

/**
 * Application-owned Navigation 3 state.
 *
 * Live and quarantined stacks are supplied by the Compose persistence layer. Mutations remain
 * internal so [AppNavigator] is the only production writer.
 */
@Suppress("TooManyFunctions")
class PassVaultNavigationState(
    private val authenticationBackStack: MutableList<PassVaultRoute>,
    private val mainBackStacks: Map<TopLevelDestination, MutableList<PassVaultRoute>>,
    private val quarantinedBackStacks: Map<TopLevelDestination, MutableList<PassVaultRoute>>,
    selectedDestination: TopLevelDestination = TopLevelDestination.HOME,
    quarantinedDestination: TopLevelDestination = TopLevelDestination.HOME,
    private val onSelectedDestinationChanged: (TopLevelDestination) -> Unit = {},
    private val onQuarantinedDestinationChanged: (TopLevelDestination) -> Unit = {},
) {
    private val _root = MutableStateFlow(NavigationRoot.AUTHENTICATION)
    val root: StateFlow<NavigationRoot> = _root.asStateFlow()

    private val _selectedDestination = MutableStateFlow(selectedDestination)
    val selectedDestination: StateFlow<TopLevelDestination> = _selectedDestination.asStateFlow()

    private val _quarantinedDestination = MutableStateFlow(quarantinedDestination)
    val quarantinedDestination: StateFlow<TopLevelDestination> = _quarantinedDestination.asStateFlow()

    init {
        require(mainBackStacks.keys == TopLevelDestination.entries.toSet()) {
            "Every top-level destination requires one live back stack"
        }
        require(quarantinedBackStacks.keys == TopLevelDestination.entries.toSet()) {
            "Every top-level destination requires one quarantine back stack"
        }
        normalizeAuthenticationStack(AuthRoute.Unlock)
        TopLevelDestination.entries.forEach { destination ->
            normalizeMainStack(mainBackStacks.getValue(destination), destination)
            normalizeQuarantinedStack(quarantinedBackStacks.getValue(destination), destination)
        }
    }

    val authenticationStack: List<PassVaultRoute>
        get() = authenticationBackStack

    fun stack(destination: TopLevelDestination): List<PassVaultRoute> =
        mainBackStacks.getValue(destination)

    fun activeStack(): List<PassVaultRoute> = when (_root.value) {
        NavigationRoot.AUTHENTICATION -> authenticationBackStack
        NavigationRoot.MAIN -> mainBackStacks.getValue(_selectedDestination.value)
    }

    fun currentRoute(): PassVaultRoute = checkNotNull(activeStack().lastOrNull()) {
        "The active navigation stack must never be empty"
    }

    fun previousRoute(): PassVaultRoute? = activeStack().getOrNull(activeStack().lastIndex - 1)

    fun liveSnapshot(): MainNavigationSnapshot = snapshot(mainBackStacks, _selectedDestination.value)

    fun quarantinedSnapshotOrNull(): MainNavigationSnapshot? {
        if (quarantinedBackStacks.values.all(List<PassVaultRoute>::isEmpty)) return null
        return snapshot(quarantinedBackStacks, _quarantinedDestination.value)
    }

    fun restorableSnapshot(): MainNavigationSnapshot = quarantinedSnapshotOrNull() ?: liveSnapshot()

    internal fun normalizeBootstrap(initialRoute: PassVaultRoute) {
        when (initialRoute) {
            AuthRoute.Onboarding -> {
                val onboardingFlow = authenticationBackStack.all { route ->
                    route == AuthRoute.Onboarding ||
                        route == AuthRoute.CreatePassword ||
                        route == AuthRoute.ConfirmPassword ||
                        route == AuthRoute.SecurityExplanation
                }
                if (!onboardingFlow || authenticationBackStack.firstOrNull() != AuthRoute.Onboarding) {
                    replaceAuthenticationRoot(AuthRoute.Onboarding)
                }
            }
            AuthRoute.Unlock -> {
                if (authenticationBackStack.any { it != AuthRoute.Unlock }) {
                    replaceAuthenticationRoot(AuthRoute.Unlock)
                }
            }
            else -> error("Bootstrap route must be an authentication route: $initialRoute")
        }
    }

    internal fun pushAuthentication(route: AuthRoute) {
        if (authenticationBackStack.lastOrNull() == route) return
        val existingIndex = authenticationBackStack.indexOf(route)
        if (existingIndex >= 0) {
            authenticationBackStack.removeAfter(existingIndex)
        } else {
            authenticationBackStack.add(route)
        }
    }

    internal fun popAuthentication(): Boolean {
        if (authenticationBackStack.size <= 1) return false
        authenticationBackStack.removeAt(authenticationBackStack.lastIndex)
        return true
    }

    internal fun replaceAuthenticationRoot(route: AuthRoute) {
        authenticationBackStack.clear()
        authenticationBackStack.add(route)
        _root.value = NavigationRoot.AUTHENTICATION
    }

    internal fun select(destination: TopLevelDestination) {
        setSelectedDestination(destination)
    }

    internal fun pushMain(route: PassVaultRoute): Boolean {
        check(route.requiresUnlockedVault()) { "Authentication routes cannot enter a main stack" }
        check(route.hasValidArguments() && route.isAllowedIn(_selectedDestination.value)) {
            "$route cannot enter the ${_selectedDestination.value} stack"
        }
        val stack = mainBackStacks.getValue(_selectedDestination.value)
        if (stack.lastOrNull() == route) return false
        val existingIndex = stack.indexOf(route)
        if (existingIndex >= 0) {
            stack.removeAfter(existingIndex)
        } else {
            stack.add(route)
        }
        return true
    }

    internal fun popMain(): Boolean {
        val stack = mainBackStacks.getValue(_selectedDestination.value)
        if (stack.size <= 1) return false
        stack.removeAt(stack.lastIndex)
        return true
    }

    internal fun popMainToRoot(destination: TopLevelDestination = _selectedDestination.value): Boolean {
        val stack = mainBackStacks.getValue(destination)
        if (stack.size <= 1) return false
        stack.removeAfter(0)
        return true
    }

    internal fun replaceCurrentMain(route: PassVaultRoute): Boolean {
        val stack = mainBackStacks.getValue(_selectedDestination.value)
        if (stack.size > 1) stack.removeAt(stack.lastIndex)
        return pushMain(route)
    }

    internal fun openIn(
        destination: TopLevelDestination,
        route: PassVaultRoute?,
        resetStack: Boolean,
    ): Boolean {
        val selectionChanged = _selectedDestination.value != destination
        setSelectedDestination(destination)
        val resetChanged = resetStack && popMainToRoot(destination)
        val routeChanged = route != null && route != destination.rootRoute() && pushMain(route)
        return selectionChanged || resetChanged || routeChanged
    }

    internal fun quarantineForAuthentication() {
        val hasRestorableLiveState = _root.value == NavigationRoot.MAIN ||
            _selectedDestination.value != TopLevelDestination.HOME ||
            mainBackStacks.values.any { stack -> stack.size > 1 }
        if (hasRestorableLiveState) {
            setQuarantinedDestination(_selectedDestination.value)
            TopLevelDestination.entries.forEach { destination ->
                quarantinedBackStacks.getValue(destination).replaceWith(
                    mainBackStacks.getValue(destination),
                )
            }
        }
        resetLiveStacks()
        _root.value = NavigationRoot.AUTHENTICATION
    }

    internal fun activateMain(snapshot: MainNavigationSnapshot?) {
        val restored = snapshot ?: restorableSnapshot()
        TopLevelDestination.entries.forEach { destination ->
            val safeStack = restored.stack(destination).takeIf { routes ->
                routes.firstOrNull() == destination.rootRoute() &&
                    routes.all(PassVaultRoute::requiresUnlockedVault) &&
                    routes.all(PassVaultRoute::hasValidArguments) &&
                    routes.all { route -> route.isAllowedIn(destination) }
            } ?: listOf(destination.rootRoute())
            mainBackStacks.getValue(destination).replaceWith(safeStack)
            quarantinedBackStacks.getValue(destination).clear()
        }
        setSelectedDestination(restored.selectedDestination)
        setQuarantinedDestination(TopLevelDestination.HOME)
        _root.value = NavigationRoot.MAIN
    }

    internal fun resetForOnboarding() {
        replaceAuthenticationRoot(AuthRoute.Onboarding)
        resetLiveStacks()
        quarantinedBackStacks.values.forEach(MutableList<PassVaultRoute>::clear)
        setQuarantinedDestination(TopLevelDestination.HOME)
    }

    private fun resetLiveStacks() {
        TopLevelDestination.entries.forEach { destination ->
            mainBackStacks.getValue(destination).replaceWith(listOf(destination.rootRoute()))
        }
        setSelectedDestination(TopLevelDestination.HOME)
    }

    private fun setSelectedDestination(destination: TopLevelDestination) {
        _selectedDestination.value = destination
        onSelectedDestinationChanged(destination)
    }

    private fun setQuarantinedDestination(destination: TopLevelDestination) {
        _quarantinedDestination.value = destination
        onQuarantinedDestinationChanged(destination)
    }

    private fun normalizeAuthenticationStack(fallback: AuthRoute) {
        if (authenticationBackStack.isEmpty() || authenticationBackStack.any { it !is AuthRoute }) {
            authenticationBackStack.clear()
            authenticationBackStack.add(fallback)
        }
    }

    private fun normalizeMainStack(
        stack: MutableList<PassVaultRoute>,
        destination: TopLevelDestination,
    ) {
        if (!stack.isValidFor(destination)) {
            stack.replaceWith(listOf(destination.rootRoute()))
        }
    }

    private fun normalizeQuarantinedStack(
        stack: MutableList<PassVaultRoute>,
        destination: TopLevelDestination,
    ) {
        if (stack.isNotEmpty() && !stack.isValidFor(destination)) {
            stack.clear()
        }
    }

    private fun snapshot(
        stacks: Map<TopLevelDestination, MutableList<PassVaultRoute>>,
        selected: TopLevelDestination,
    ) = MainNavigationSnapshot(
        selectedDestination = selected,
        home = stacks.getValue(TopLevelDestination.HOME).toList(),
        generator = stacks.getValue(TopLevelDestination.GENERATOR).toList(),
        twoFactorCodes = stacks.getValue(TopLevelDestination.TWO_FACTOR_CODES).toList(),
        settings = stacks.getValue(TopLevelDestination.SETTINGS).toList(),
    )
}

private fun List<PassVaultRoute>.isValidFor(destination: TopLevelDestination): Boolean =
    firstOrNull() == destination.rootRoute() &&
        all { route ->
            route.requiresUnlockedVault() && route.hasValidArguments() && route.isAllowedIn(destination)
        }

private fun <T> MutableList<T>.removeAfter(index: Int) {
    while (lastIndex > index) removeAt(lastIndex)
}

private fun <T> MutableList<T>.replaceWith(values: Iterable<T>) {
    clear()
    addAll(values)
}
