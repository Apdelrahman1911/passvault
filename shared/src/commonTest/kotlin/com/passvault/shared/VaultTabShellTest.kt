package com.passvault.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class VaultTabShellTest {
    @Test
    fun selectingInactiveTabOpensIt() {
        assertEquals(VaultTab.HEALTH, null.toggle(VaultTab.HEALTH))
        assertEquals(VaultTab.SETTINGS, VaultTab.GENERATOR.toggle(VaultTab.SETTINGS))
    }

    @Test
    fun selectingActiveTabReturnsToVault() {
        assertNull(VaultTab.GENERATOR.toggle(VaultTab.GENERATOR))
    }
}
