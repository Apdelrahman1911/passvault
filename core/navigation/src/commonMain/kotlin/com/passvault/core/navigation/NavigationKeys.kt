package com.passvault.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * The single route hierarchy consumed by `shared/PassVaultApp.kt`.
 *
 * Keep only routes that have a matching `NavDisplay` entry. Unreachable
 * deep-link, dialog, bottom-sheet, and feature-local route systems previously
 * made unsupported navigation appear implemented.
 */
@Serializable
sealed interface PassVaultRoute : NavKey

@Serializable
sealed interface AuthRoute : PassVaultRoute {
    @Serializable
    data object Onboarding : AuthRoute

    @Serializable
    data object CreatePassword : AuthRoute

    @Serializable
    data object ConfirmPassword : AuthRoute

    @Serializable
    data object SecurityExplanation : AuthRoute

    @Serializable
    data object Unlock : AuthRoute
}

@Serializable
sealed interface VaultRoute : PassVaultRoute {
    @Serializable
    data object Vault : VaultRoute

    @Serializable
    data class CredentialDetail(
        val credentialId: String,
    ) : VaultRoute

    @Serializable
    data class CredentialCreate(
        val folderId: String? = null,
    ) : VaultRoute

    @Serializable
    data class CredentialEdit(
        val credentialId: String,
    ) : VaultRoute
}

@Serializable
sealed interface GeneratorRoute : PassVaultRoute {
    @Serializable
    data object Generator : GeneratorRoute
}

@Serializable
sealed interface HealthRoute : PassVaultRoute {
    @Serializable
    data object Health : HealthRoute
}

@Serializable
sealed interface TwoFactorRoute : PassVaultRoute {
    @Serializable
    data object Codes : TwoFactorRoute
}

@Serializable
sealed interface SettingsRoute : PassVaultRoute {
    @Serializable
    data object Settings : SettingsRoute

    @Serializable
    data object Security : SettingsRoute

    @Serializable
    data object Appearance : SettingsRoute

    @Serializable
    data object Data : SettingsRoute
}

@Serializable
sealed interface BackupRoute : PassVaultRoute {
    @Serializable
    data object Backup : BackupRoute

    @Serializable
    data object Import : BackupRoute

    @Serializable
    data object Export : BackupRoute
}
