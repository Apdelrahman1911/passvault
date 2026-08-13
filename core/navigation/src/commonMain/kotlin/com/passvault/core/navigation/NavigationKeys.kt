package com.passvault.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The single route hierarchy consumed by PassVault's feature route adapters and `NavDisplay`.
 *
 * Keep only routes that have a matching `NavDisplay` entry. Unreachable
 * deep-link, dialog, bottom-sheet, and feature-local route systems previously
 * made unsupported navigation appear implemented.
 */
@Serializable
sealed interface PassVaultRoute : NavKey

/** Exhaustive identity used to prove that every serializable route has one UI adapter. */
enum class PassVaultRouteKind {
    AUTH_ONBOARDING,
    AUTH_CREATE_PASSWORD,
    AUTH_CONFIRM_PASSWORD,
    AUTH_SECURITY_EXPLANATION,
    AUTH_UNLOCK,
    VAULT_ROOT,
    VAULT_CREDENTIAL_DETAIL,
    VAULT_CREDENTIAL_CREATE,
    VAULT_CREDENTIAL_EDIT,
    GENERATOR_ROOT,
    HEALTH_ROOT,
    TWO_FACTOR_ROOT,
    SETTINGS_ROOT,
    SETTINGS_SECURITY,
    SETTINGS_APPEARANCE,
    SETTINGS_DATA,
    BACKUP_ROOT,
    BACKUP_IMPORT,
    BACKUP_EXPORT,
}

@Serializable
sealed interface AuthRoute : PassVaultRoute {
    @Serializable
    @SerialName("auth.onboarding")
    data object Onboarding : AuthRoute

    @Serializable
    @SerialName("auth.create-password")
    data object CreatePassword : AuthRoute

    @Serializable
    @SerialName("auth.confirm-password")
    data object ConfirmPassword : AuthRoute

    @Serializable
    @SerialName("auth.security-explanation")
    data object SecurityExplanation : AuthRoute

    @Serializable
    @SerialName("auth.unlock")
    data object Unlock : AuthRoute
}

@Serializable
sealed interface VaultRoute : PassVaultRoute {
    @Serializable
    @SerialName("vault.root")
    data object Vault : VaultRoute

    @Serializable
    @SerialName("vault.credential-detail")
    data class CredentialDetail(
        val credentialId: String,
    ) : VaultRoute

    @Serializable
    @SerialName("vault.credential-create")
    data class CredentialCreate(
        val folderId: String? = null,
    ) : VaultRoute

    @Serializable
    @SerialName("vault.credential-edit")
    data class CredentialEdit(
        val credentialId: String,
    ) : VaultRoute
}

@Serializable
sealed interface GeneratorRoute : PassVaultRoute {
    @Serializable
    @SerialName("generator.root")
    data object Generator : GeneratorRoute
}

@Serializable
sealed interface HealthRoute : PassVaultRoute {
    @Serializable
    @SerialName("health.root")
    data object Health : HealthRoute
}

@Serializable
sealed interface TwoFactorRoute : PassVaultRoute {
    @Serializable
    @SerialName("two-factor.root")
    data object Codes : TwoFactorRoute
}

@Serializable
sealed interface SettingsRoute : PassVaultRoute {
    @Serializable
    @SerialName("settings.root")
    data object Settings : SettingsRoute

    @Serializable
    @SerialName("settings.security")
    data object Security : SettingsRoute

    @Serializable
    @SerialName("settings.appearance")
    data object Appearance : SettingsRoute

    @Serializable
    @SerialName("settings.data")
    data object Data : SettingsRoute
}

@Serializable
sealed interface BackupRoute : PassVaultRoute {
    @Serializable
    @SerialName("backup.root")
    data object Backup : BackupRoute

    @Serializable
    @SerialName("backup.import")
    data object Import : BackupRoute

    @Serializable
    @SerialName("backup.export")
    data object Export : BackupRoute
}

/** One value for every concrete route, used by serialization and registry completeness tests. */
internal val passVaultRouteExamples: List<PassVaultRoute>
    get() = listOf(
        AuthRoute.Onboarding,
        AuthRoute.CreatePassword,
        AuthRoute.ConfirmPassword,
        AuthRoute.SecurityExplanation,
        AuthRoute.Unlock,
        VaultRoute.Vault,
        VaultRoute.CredentialDetail("00000000-0000-0000-0000-000000000001"),
        VaultRoute.CredentialCreate("00000000-0000-0000-0000-000000000002"),
        VaultRoute.CredentialEdit("00000000-0000-0000-0000-000000000003"),
        GeneratorRoute.Generator,
        HealthRoute.Health,
        TwoFactorRoute.Codes,
        SettingsRoute.Settings,
        SettingsRoute.Security,
        SettingsRoute.Appearance,
        SettingsRoute.Data,
        BackupRoute.Backup,
        BackupRoute.Import,
        BackupRoute.Export,
    )

@Suppress("CyclomaticComplexMethod")
fun PassVaultRoute.kind(): PassVaultRouteKind = when (this) {
    AuthRoute.Onboarding -> PassVaultRouteKind.AUTH_ONBOARDING
    AuthRoute.CreatePassword -> PassVaultRouteKind.AUTH_CREATE_PASSWORD
    AuthRoute.ConfirmPassword -> PassVaultRouteKind.AUTH_CONFIRM_PASSWORD
    AuthRoute.SecurityExplanation -> PassVaultRouteKind.AUTH_SECURITY_EXPLANATION
    AuthRoute.Unlock -> PassVaultRouteKind.AUTH_UNLOCK
    VaultRoute.Vault -> PassVaultRouteKind.VAULT_ROOT
    is VaultRoute.CredentialDetail -> PassVaultRouteKind.VAULT_CREDENTIAL_DETAIL
    is VaultRoute.CredentialCreate -> PassVaultRouteKind.VAULT_CREDENTIAL_CREATE
    is VaultRoute.CredentialEdit -> PassVaultRouteKind.VAULT_CREDENTIAL_EDIT
    GeneratorRoute.Generator -> PassVaultRouteKind.GENERATOR_ROOT
    HealthRoute.Health -> PassVaultRouteKind.HEALTH_ROOT
    TwoFactorRoute.Codes -> PassVaultRouteKind.TWO_FACTOR_ROOT
    SettingsRoute.Settings -> PassVaultRouteKind.SETTINGS_ROOT
    SettingsRoute.Security -> PassVaultRouteKind.SETTINGS_SECURITY
    SettingsRoute.Appearance -> PassVaultRouteKind.SETTINGS_APPEARANCE
    SettingsRoute.Data -> PassVaultRouteKind.SETTINGS_DATA
    BackupRoute.Backup -> PassVaultRouteKind.BACKUP_ROOT
    BackupRoute.Import -> PassVaultRouteKind.BACKUP_IMPORT
    BackupRoute.Export -> PassVaultRouteKind.BACKUP_EXPORT
}

/** Rejects malformed route payloads before they can enter a live or restored stack. */
internal fun PassVaultRoute.hasValidArguments(): Boolean = when (this) {
    is VaultRoute.CredentialDetail -> credentialId.isCanonicalNavigationId()
    is VaultRoute.CredentialEdit -> credentialId.isCanonicalNavigationId()
    is VaultRoute.CredentialCreate -> folderId == null || folderId.isCanonicalNavigationId()
    else -> true
}

private fun String.isCanonicalNavigationId(): Boolean {
    if (length != NAVIGATION_ID_TEXT_LENGTH) return false
    return indices.all { index ->
        when (index) {
            8, 13, 18, 23 -> this[index] == '-'
            else -> this[index] in '0'..'9' || this[index] in 'a'..'f' || this[index] in 'A'..'F'
        }
    }
}

private const val NAVIGATION_ID_TEXT_LENGTH = 36
