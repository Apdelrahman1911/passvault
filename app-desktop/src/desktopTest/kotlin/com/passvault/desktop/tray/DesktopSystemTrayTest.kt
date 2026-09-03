package com.passvault.desktop.tray

import java.awt.Image
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.SwingUtilities
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DesktopSystemTrayTest {
    @Test
    fun `off event thread lifecycle never waits for a busy event thread`() {
        val platform = RecordingTrayPlatform()
        val tray = DesktopSystemTray(platform, SwingDesktopEventThread)
        val eventThreadEntered = CountDownLatch(1)
        val releaseEventThread = CountDownLatch(1)
        val callerReturned = CountDownLatch(1)

        SwingUtilities.invokeLater {
            eventThreadEntered.countDown()
            releaseEventThread.await(TEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        }
        assertTrue(eventThreadEntered.await(TEST_TIMEOUT_SECONDS, TimeUnit.SECONDS))

        val caller = thread(name = "tray-lifecycle-caller") {
            tray.setup(TEST_STRINGS, {}, {}, {})
            tray.hide()
            tray.cleanup()
            callerReturned.countDown()
        }
        try {
            assertTrue(
                callerReturned.await(NON_BLOCKING_TIMEOUT_SECONDS, TimeUnit.SECONDS),
                "Tray lifecycle blocked waiting for the AWT event thread",
            )
            assertFalse(platform.installed.get())
        } finally {
            releaseEventThread.countDown()
            caller.join(TimeUnit.SECONDS.toMillis(TEST_TIMEOUT_SECONDS))
        }

        val eventQueueDrained = CountDownLatch(1)
        SwingUtilities.invokeLater(eventQueueDrained::countDown)
        assertTrue(eventQueueDrained.await(TEST_TIMEOUT_SECONDS, TimeUnit.SECONDS))
        assertTrue(platform.installed.get())
        assertTrue(platform.removed.get())
        assertTrue(platform.installRanOnEventThread.get())
        assertTrue(platform.removeRanOnEventThread.get())
    }

    private class RecordingTrayPlatform : DesktopTrayPlatform {
        val installed = AtomicBoolean(false)
        val removed = AtomicBoolean(false)
        val installRanOnEventThread = AtomicBoolean(false)
        val removeRanOnEventThread = AtomicBoolean(false)

        override fun isSupported(): Boolean = true

        override fun install(
            strings: DesktopTrayStrings,
            onShow: () -> Unit,
            onLock: () -> Unit,
            onExit: () -> Unit,
            image: Image,
        ): DesktopTrayIconHandle {
            installRanOnEventThread.set(SwingUtilities.isEventDispatchThread())
            installed.set(true)
            return DesktopTrayIconHandle {
                removeRanOnEventThread.set(SwingUtilities.isEventDispatchThread())
                removed.set(true)
            }
        }
    }

    private companion object {
        val TEST_STRINGS = DesktopTrayStrings(
            tooltip = "PassVault",
            showApp = "Show",
            lockVault = "Lock",
            exit = "Exit",
        )
        const val NON_BLOCKING_TIMEOUT_SECONDS = 2L
        const val TEST_TIMEOUT_SECONDS = 5L
    }
}
