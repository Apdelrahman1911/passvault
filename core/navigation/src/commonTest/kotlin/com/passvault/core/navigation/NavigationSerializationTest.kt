package com.passvault.core.navigation

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NavigationSerializationTest {
    private val json = Json {
        classDiscriminator = "routeType"
        encodeDefaults = true
    }

    @Test
    fun `every registered route round trips with a stable serial name`() {
        assertEquals(19, passVaultRouteExamples.size)
        assertEquals(
            PassVaultRouteKind.entries.toSet(),
            passVaultRouteExamples.map(PassVaultRoute::kind).toSet(),
        )
        passVaultRouteExamples.forEach { route ->
            val encoded = json.encodeToString(PassVaultRoute.serializer(), route)
            val restored = json.decodeFromString(PassVaultRoute.serializer(), encoded)
            assertEquals(route, restored, encoded)
            assertTrue(encoded.contains("\"routeType\":"), encoded)
            assertTrue(!encoded.contains(route::class.qualifiedName.orEmpty()), encoded)
        }
    }

    @Test
    fun `main navigation snapshot preserves every tab stack`() {
        val snapshot = MainNavigationSnapshot(
            selectedDestination = TopLevelDestination.SETTINGS,
            home = listOf(VaultRoute.Vault, VaultRoute.CredentialDetail("credential")),
            generator = listOf(GeneratorRoute.Generator, HealthRoute.Health),
            twoFactorCodes = listOf(TwoFactorRoute.Codes),
            settings = listOf(SettingsRoute.Settings, BackupRoute.Export),
        )
        val encoded = json.encodeToString(snapshot)
        assertEquals(snapshot, json.decodeFromString<MainNavigationSnapshot>(encoded))
    }
}
