package com.passvault.desktop.security

import java.awt.Frame
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DesktopWindowProtectionTest {

    @Test
    fun `unattached protection is not reported as minimized`() {
        assertFalse(DesktopWindowProtection().isMinimized)
    }

    @Test
    fun `unlock rearms future lock notifications`() {
        val protection = DesktopWindowProtection()
        var locks = 0
        protection.setLockListener { locks++ }

        protection.lock()
        protection.lock()
        assertTrue(protection.isLocked)
        assertEquals(1, locks)

        protection.unlock()
        protection.lock()
        assertEquals(2, locks)
    }

    @Test
    fun `shutdown preparation locks without starting duplicate cleanup`() {
        val protection = DesktopWindowProtection()
        var locks = 0
        protection.setLockListener { locks++ }

        protection.prepareForShutdown()

        assertTrue(protection.isLocked)
        assertEquals(0, locks)
    }

    @Test
    fun `cleanup resets lifecycle state`() {
        val protection = DesktopWindowProtection()
        protection.lock()

        protection.cleanup()

        assertFalse(protection.isLocked)
        assertFalse(protection.isMinimized)
    }

    @Test
    fun `restore remains deferred until vault content is secured`() {
        val protection = DesktopWindowProtection()

        protection.lock()
        protection.restoreWindow()
        assertTrue(protection.isLocked)
        assertTrue(protection.isRestoreDeferred)

        protection.onVaultContentSecured()
        assertTrue(protection.isLocked)
        assertFalse(protection.isRestoreDeferred)
    }

    @Test
    fun `failed content security stays concealed and a later restore retries`() {
        val protection = DesktopWindowProtection()
        var securityRequests = 0
        protection.setLockListener { securityRequests++ }

        protection.lock()
        protection.restoreWindow()
        assertEquals(1, securityRequests)
        assertTrue(protection.isRestoreDeferred)

        protection.onVaultContentSecurityFailed()
        protection.restoreWindow()
        assertEquals(2, securityRequests)
        assertTrue(protection.isRestoreDeferred)

        protection.onVaultContentSecured()
        assertFalse(protection.isRestoreDeferred)
    }

    @Test
    fun `native restore remains minimized until locked content is secured`() {
        assertTrue(
            shouldDeferDesktopWindowRestore(
                oldState = Frame.ICONIFIED,
                newState = Frame.NORMAL,
                locked = true,
                contentSecured = false,
            ),
        )
        assertFalse(
            shouldDeferDesktopWindowRestore(
                oldState = Frame.ICONIFIED,
                newState = Frame.NORMAL,
                locked = true,
                contentSecured = true,
            ),
        )
        assertFalse(
            shouldDeferDesktopWindowRestore(
                oldState = Frame.ICONIFIED,
                newState = Frame.NORMAL,
                locked = false,
                contentSecured = false,
            ),
        )
    }
}
