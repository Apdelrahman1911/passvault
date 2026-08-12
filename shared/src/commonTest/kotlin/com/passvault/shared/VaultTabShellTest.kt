package com.passvault.shared

import kotlin.test.Test
import kotlin.test.assertEquals

class VaultTabShellTest {
    @Test
    fun selectingInactiveTabOpensIt() {
        assertEquals(
            VaultTab.TWO_FACTOR_CODES,
            VaultTab.HOME.toggle(VaultTab.TWO_FACTOR_CODES),
        )
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
