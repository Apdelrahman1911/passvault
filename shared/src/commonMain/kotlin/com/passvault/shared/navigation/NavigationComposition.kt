package com.passvault.shared.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.serialization.NavBackStackSerializer
import com.passvault.core.navigation.AppNavigator
import com.passvault.core.navigation.AuthRoute
import com.passvault.core.navigation.GeneratorRoute
import com.passvault.core.navigation.PassVaultNavigationState
import com.passvault.core.navigation.PassVaultRoute
import com.passvault.core.navigation.SettingsRoute
import com.passvault.core.navigation.TopLevelDestination
import com.passvault.core.navigation.TwoFactorRoute
import com.passvault.core.navigation.VaultRoute
import kotlinx.serialization.KSerializer

internal data class NavigationComposition(
    val state: PassVaultNavigationState,
    val navigator: AppNavigator,
)

private data class PersistentNavigationStacks(
    val authentication: NavBackStack<PassVaultRoute>,
    val live: Map<TopLevelDestination, NavBackStack<PassVaultRoute>>,
    val quarantine: Map<TopLevelDestination, NavBackStack<PassVaultRoute>>,
)

/**
 * Owns all durable route-only state. The root intentionally is not restored:
 * every cold/process-restored launch begins at authentication, and protected
 * route keys remain quarantined until the unlocked session validates them.
 */
@Composable
internal fun rememberNavigationComposition(initialRoute: PassVaultRoute): NavigationComposition {
    val stacks = rememberPersistentNavigationStacks(initialRoute)
    var selectedName by rememberSaveable { mutableStateOf(TopLevelDestination.HOME.name) }
    var quarantinedName by rememberSaveable { mutableStateOf(TopLevelDestination.HOME.name) }
    val state = remember(stacks) {
        PassVaultNavigationState(
            authenticationBackStack = stacks.authentication,
            mainBackStacks = stacks.live,
            quarantinedBackStacks = stacks.quarantine,
            selectedDestination = selectedName.toTopLevelDestination(),
            quarantinedDestination = quarantinedName.toTopLevelDestination(),
            onSelectedDestinationChanged = { destination -> selectedName = destination.name },
            onQuarantinedDestinationChanged = { destination -> quarantinedName = destination.name },
        )
    }
    val navigator = remember(state) { AppNavigator(state) }
    androidx.compose.runtime.LaunchedEffect(navigator, initialRoute) {
        navigator.normalizeBootstrap(initialRoute)
    }
    return remember(state, navigator) { NavigationComposition(state, navigator) }
}

@Composable
private fun rememberPersistentNavigationStacks(initialRoute: PassVaultRoute): PersistentNavigationStacks {
    val serializer = remember { NavBackStackSerializer(PassVaultRoute.serializer()) }
    return PersistentNavigationStacks(
        authentication = rememberRouteStack(serializer, initialRoute),
        live = mapOf(
            TopLevelDestination.HOME to rememberRouteStack(serializer, VaultRoute.Vault),
            TopLevelDestination.GENERATOR to rememberRouteStack(serializer, GeneratorRoute.Generator),
            TopLevelDestination.TWO_FACTOR_CODES to rememberRouteStack(serializer, TwoFactorRoute.Codes),
            TopLevelDestination.SETTINGS to rememberRouteStack(serializer, SettingsRoute.Settings),
        ),
        // Quarantine has no saveable UI holder or ViewModelStore. Locking
        // therefore retains route identifiers, never sensitive entry state.
        quarantine = mapOf(
            TopLevelDestination.HOME to rememberRouteStack(serializer),
            TopLevelDestination.GENERATOR to rememberRouteStack(serializer),
            TopLevelDestination.TWO_FACTOR_CODES to rememberRouteStack(serializer),
            TopLevelDestination.SETTINGS to rememberRouteStack(serializer),
        ),
    )
}

@Composable
private fun rememberRouteStack(
    serializer: KSerializer<NavBackStack<PassVaultRoute>>,
    initialRoute: PassVaultRoute? = null,
): NavBackStack<PassVaultRoute> = rememberSerializable(serializer = serializer) {
    if (initialRoute == null) NavBackStack() else NavBackStack(initialRoute)
}

private fun String.toTopLevelDestination(): TopLevelDestination =
    TopLevelDestination.entries.firstOrNull { destination -> destination.name == this }
        ?: TopLevelDestination.HOME
