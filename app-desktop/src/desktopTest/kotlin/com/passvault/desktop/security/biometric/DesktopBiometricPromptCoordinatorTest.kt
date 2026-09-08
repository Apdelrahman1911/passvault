package com.passvault.desktop.security.biometric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DesktopBiometricPromptCoordinatorTest {
    @Test
    fun `prompt coordination is active only for the native prompt lifetime`() {
        val coordinator = DesktopBiometricPromptCoordinator()
        var completions = 0
        coordinator.setFinishedListener { completions += 1 }

        assertFailsWith<IllegalStateException> {
            coordinator.withPrompt {
                assertTrue(coordinator.isActive)
                coordinator.withPrompt { error("must not start") }
            }
        }

        assertFalse(coordinator.isActive)
        assertEquals(1, completions)
    }
}
