package com.passvault.core.navigation

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SceneStrategiesTest {
    @Test
    fun `authentication routes remain available while locked`() {
        val routes = listOf(
            AuthRoute.Onboarding,
            AuthRoute.CreatePassword,
            AuthRoute.ConfirmPassword,
            AuthRoute.SecurityExplanation,
            AuthRoute.Unlock,
        )

        routes.forEach { route ->
            assertFalse(route.requiresUnlockedVault(), route.toString())
        }
    }

    @Test
    fun `application routes require an unlocked vault`() {
        val routes = listOf(
            VaultRoute.Vault,
            VaultRoute.CredentialDetail("credential"),
            VaultRoute.CredentialCreate(),
            VaultRoute.CredentialEdit("credential"),
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

        routes.forEach { route ->
            assertTrue(route.requiresUnlockedVault(), route.toString())
        }
    }
}
