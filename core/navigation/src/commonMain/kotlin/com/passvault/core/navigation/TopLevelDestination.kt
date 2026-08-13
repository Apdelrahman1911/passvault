package com.passvault.core.navigation

import kotlinx.serialization.Serializable

/** The independently saved stacks presented by the authenticated application shell. */
@Serializable
enum class TopLevelDestination {
    HOME,
    GENERATOR,
    TWO_FACTOR_CODES,
    SETTINGS,
}

fun TopLevelDestination.rootRoute(): PassVaultRoute = when (this) {
    TopLevelDestination.HOME -> VaultRoute.Vault
    TopLevelDestination.GENERATOR -> GeneratorRoute.Generator
    TopLevelDestination.TWO_FACTOR_CODES -> TwoFactorRoute.Codes
    TopLevelDestination.SETTINGS -> SettingsRoute.Settings
}

fun PassVaultRoute.topLevelDestinationOrNull(): TopLevelDestination? = when (this) {
    VaultRoute.Vault -> TopLevelDestination.HOME
    GeneratorRoute.Generator -> TopLevelDestination.GENERATOR
    TwoFactorRoute.Codes -> TopLevelDestination.TWO_FACTOR_CODES
    SettingsRoute.Settings -> TopLevelDestination.SETTINGS
    else -> null
}

/**
 * Structural ownership for routes stored in a top-level stack. Detail routes
 * opened from a tool remain in that tool's stack so Back returns to the
 * originating scan/list instead of unexpectedly switching to Home.
 */
fun PassVaultRoute.isAllowedIn(destination: TopLevelDestination): Boolean = when (destination) {
    TopLevelDestination.HOME -> this is VaultRoute
    TopLevelDestination.GENERATOR ->
        this is GeneratorRoute || this is HealthRoute || this is VaultRoute.CredentialDetail ||
            this is VaultRoute.CredentialEdit
    TopLevelDestination.TWO_FACTOR_CODES ->
        this is TwoFactorRoute || this is VaultRoute.CredentialDetail || this is VaultRoute.CredentialEdit
    TopLevelDestination.SETTINGS -> this is SettingsRoute || this is BackupRoute
}
