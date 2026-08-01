package com.passvault.shared

import kotlin.test.Test
import kotlin.test.assertEquals

class VaultTabShellTest {
    @Test
    fun selectingInactiveTabOpensIt() {
        assertEquals(VaultTab.HEALTH, VaultTab.HOME.toggle(VaultTab.HEALTH))
        assertEquals(VaultTab.SETTINGS, VaultTab.GENERATOR.toggle(VaultTab.SETTINGS))
    }

    @Test
    fun selectingActiveTabReturnsToVault() {
        assertEquals(VaultTab.HOME, VaultTab.GENERATOR.toggle(VaultTab.GENERATOR))
    }

    @Test
    fun selectingHomeKeepsHomeOpen() {
        assertEquals(VaultTab.HOME, VaultTab.HOME.toggle(VaultTab.HOME))
    }
}
