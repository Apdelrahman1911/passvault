package com.passvault.shared.navigation

import com.passvault.core.navigation.PassVaultRouteKind
import kotlin.test.Test
import kotlin.test.assertEquals

class RouteAdapterRegistryTest {
    @Test
    fun `every concrete route has one feature-owned entry adapter`() {
        assertEquals(PassVaultRouteKind.entries.toSet(), registeredRouteAdapterKinds)
        verifyRouteAdapterRegistry()
    }
}
