package com.passvault.desktop.tray

import java.awt.Image
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.swing.SwingUtilities
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DesktopSystemTrayLocalizationTest {
    @Test
    fun `installed labels follow English Arabic English without replacing callbacks or the handle`() {
        val eventThread = QueuedEventThread()
        val platform = RecordingTrayPlatform { eventThread.isDispatching }
        val tray = DesktopSystemTray(platform, eventThread)
        val actions = mutableListOf<String>()
        var lockAllowed = true
        val guardedLock: () -> Unit = {
            if (lockAllowed) {
                actions.add("lock")
            }
        }
        try {
            tray.setup(ENGLISH, { actions.add("show-one") }, guardedLock, { actions.add("exit-one") })
            assertEquals(0, platform.installAttempts)
            eventThread.drain()
            val icon = platform.icons.single()
            assertEquals(ENGLISH, icon.strings)

            tray.setup(ARABIC, { actions.add("show-two") }, guardedLock, { actions.add("exit-two") })
            assertEquals(ENGLISH, icon.strings, "Native labels must not mutate before event-thread dispatch")
            eventThread.drain()
            assertEquals(ARABIC, icon.strings)
            lockAllowed = false
            eventThread.dispatch { icon.invokeActions() }
            eventThread.drain()
            assertEquals(listOf("show-two", "exit-two"), actions)

            tray.setup(ENGLISH, { actions.add("show-three") }, guardedLock, { actions.add("exit-three") })
            eventThread.drain()
            lockAllowed = true
            eventThread.dispatch { icon.invokeActions() }
            eventThread.drain()
            assertEquals(ENGLISH, icon.strings)
            assertEquals(listOf(ARABIC, ENGLISH), icon.successfulUpdates)
            assertEquals(1, platform.installAttempts)
            assertEquals(0, icon.removeAttempts)
            assertEquals(listOf("show-two", "exit-two", "show-three", "lock", "exit-three"), actions)

            tray.cleanup()
            eventThread.drain()
            val actionCount = actions.size
            eventThread.dispatch { icon.invokeActions() }
            eventThread.drain()
            assertTrue(icon.removed)
            assertEquals(actionCount, actions.size, "Disposed callbacks must remain inert")
        } finally {
            tray.cleanup()
            eventThread.drain()
        }
    }

    @Test
    fun `failed label update retains its owner and the next setup retries all labels`() {
        val eventThread = QueuedEventThread()
        val platform = RecordingTrayPlatform { eventThread.isDispatching }
        val tray = DesktopSystemTray(platform, eventThread)
        try {
            tray.setup(ENGLISH, {}, {}, {})
            eventThread.drain()
            val icon = platform.icons.single()
            icon.failNextUpdate = true
            tray.setup(ARABIC, {}, {}, {})
            eventThread.drain()
            assertEquals(1, icon.updateAttempts)
            assertEquals(1, platform.installAttempts)
            assertFalse(icon.removed)

            tray.setup(ARABIC, {}, {}, {})
            eventThread.drain()
            assertEquals(2, icon.updateAttempts)
            assertEquals(ARABIC, icon.strings)
            assertEquals(1, platform.installAttempts, "Update failure cannot orphan an installed tray icon")
        } finally {
            tray.cleanup()
            eventThread.drain()
        }
    }

    @Test
    fun `failed removal keeps the handle for language refresh and later cleanup`() {
        val eventThread = QueuedEventThread()
        val platform = RecordingTrayPlatform { eventThread.isDispatching }
        val tray = DesktopSystemTray(platform, eventThread)
        try {
            tray.setup(ENGLISH, {}, {}, {})
            eventThread.drain()
            val icon = platform.icons.single()
            icon.failNextRemove = true
            tray.hide()
            eventThread.drain()
            assertFalse(icon.removed)

            tray.setup(ARABIC, {}, {}, {})
            eventThread.drain()
            assertEquals(ARABIC, icon.strings)
            assertEquals(1, platform.installAttempts)
            assertEquals(1, icon.removeAttempts)

            tray.cleanup()
            eventThread.drain()
            assertTrue(icon.removed)
            assertEquals(2, icon.removeAttempts)
        } finally {
            tray.cleanup()
            eventThread.drain()
        }
    }

    @Test
    fun `unsupported and failed installs stay retryable with the latest language`() {
        val eventThread = QueuedEventThread()
        val platform = RecordingTrayPlatform { eventThread.isDispatching }
        val tray = DesktopSystemTray(platform, eventThread)
        try {
            platform.supported = false
            tray.setup(ENGLISH, {}, {}, {})
            eventThread.drain()
            assertEquals(0, platform.installAttempts)

            platform.supported = true
            platform.failNextInstall = true
            tray.setup(ENGLISH, {}, {}, {})
            eventThread.drain()
            assertTrue(platform.icons.isEmpty())

            tray.setup(ARABIC, {}, {}, {})
            eventThread.drain()
            assertEquals(2, platform.installAttempts)
            assertEquals(ARABIC, platform.icons.single().strings)
        } finally {
            tray.cleanup()
            eventThread.drain()
        }
    }

    @Test
    fun `the Swing dispatcher confines installation label updates and removal to the event thread`() {
        val platform = RecordingTrayPlatform(SwingUtilities::isEventDispatchThread)
        val tray = DesktopSystemTray(platform, SwingDesktopEventThread)
        try {
            tray.setup(ENGLISH, {}, {}, {})
            tray.setup(ARABIC, {}, {}, {})
            tray.cleanup()
            drainSwingEventQueue()
            val icon = platform.icons.single()
            assertEquals(ARABIC, icon.strings)
            assertEquals(listOf(ARABIC), icon.successfulUpdates)
            assertTrue(icon.removed)
        } finally {
            tray.cleanup()
            drainSwingEventQueue()
        }
    }

    private fun drainSwingEventQueue() {
        val drained = CountDownLatch(1)
        SwingUtilities.invokeLater(drained::countDown)
        assertTrue(drained.await(TEST_TIMEOUT_SECONDS, TimeUnit.SECONDS), "Event-thread work did not settle")
    }

    private class QueuedEventThread : DesktopEventThread {
        private val pending = ArrayDeque<() -> Unit>()
        var isDispatching = false
            private set

        override fun dispatch(block: () -> Unit) {
            pending.addLast(block)
        }

        fun drain() {
            var tasks = 0
            while (pending.isNotEmpty()) {
                check(++tasks <= MAX_DISPATCH_TASKS) { "Unexpected unbounded event-thread work" }
                isDispatching = true
                try {
                    pending.removeFirst().invoke()
                } finally {
                    isDispatching = false
                }
            }
        }
    }

    private class RecordingTrayPlatform(private val isEventThread: () -> Boolean) : DesktopTrayPlatform {
        var supported = true
        var failNextInstall = false
        var installAttempts = 0
        val icons = mutableListOf<RecordingIcon>()

        override fun isSupported(): Boolean = supported

        override fun install(
            strings: DesktopTrayStrings,
            onShow: () -> Unit,
            onLock: () -> Unit,
            onExit: () -> Unit,
            image: Image,
        ): DesktopTrayIconHandle {
            assertTrue(isEventThread())
            installAttempts++
            if (failNextInstall) {
                failNextInstall = false
                error("Synthetic install failure")
            }
            return RecordingIcon(strings, onShow, onLock, onExit, isEventThread).also(icons::add)
        }
    }

    private class RecordingIcon(
        var strings: DesktopTrayStrings,
        private val onShow: () -> Unit,
        private val onLock: () -> Unit,
        private val onExit: () -> Unit,
        private val isEventThread: () -> Boolean,
    ) : DesktopTrayIconHandle {
        var updateAttempts = 0
        var removeAttempts = 0
        var failNextUpdate = false
        var failNextRemove = false
        var removed = false
        val successfulUpdates = mutableListOf<DesktopTrayStrings>()

        override fun updateStrings(strings: DesktopTrayStrings) {
            assertTrue(isEventThread())
            updateAttempts++
            if (failNextUpdate) {
                failNextUpdate = false
                // A real adapter can have updated only some fields before an OS failure.
                this.strings = this.strings.copy(tooltip = strings.tooltip)
                error("Synthetic label-update failure")
            }
            this.strings = strings
            successfulUpdates.add(strings)
        }

        override fun remove() {
            assertTrue(isEventThread())
            removeAttempts++
            if (failNextRemove) {
                failNextRemove = false
                error("Synthetic remove failure")
            }
            removed = true
        }

        fun invokeActions() {
            assertTrue(isEventThread())
            onShow()
            onLock()
            onExit()
        }
    }

    private companion object {
        val ENGLISH = DesktopTrayStrings("PassVault Password Manager", "Show PassVault", "Lock Vault", "Exit")
        val ARABIC = DesktopTrayStrings("مدير كلمات المرور PassVault", "إظهار PassVault", "قفل الخزنة", "خروج")
        const val TEST_TIMEOUT_SECONDS = 5L
        const val MAX_DISPATCH_TASKS = 32
    }
}
