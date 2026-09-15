package com.passvault.desktop

import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DesktopShutdownCoordinatorTest {
    @Test
    fun `close immediately exits the window and runs every cleanup boundary once`() = runTest {
        val events = mutableListOf<String>()
        val coordinator = coordinator(
            cancelBiometric = { events += "cancel-biometric" },
            lockVault = {
                events += "lock-vault"
                true
            },
            clearClipboard = { events += "clear-clipboard" },
            purgePreviews = { events += "purge-previews" },
            closeBiometric = { events += "close-biometric" },
            closeDatabase = { events += "close-database" },
        )

        coordinator.requestClose(
            prepareWindowForExit = { events += "prepare-window" },
            exitApplication = { events += "exit-application" },
        )

        assertEquals(
            listOf("cancel-biometric", "prepare-window", "exit-application"),
            events,
        )
        advanceUntilIdle()
        val report = coordinator.awaitCleanup(0L)
        assertTrue(report.completed)
        assertEquals(0, report.failureCount)
        assertEquals(1, events.count { it == "lock-vault" })
        assertEquals(1, events.count { it == "clear-clipboard" })
        assertEquals(1, events.count { it == "purge-previews" })
        assertEquals(1, events.count { it == "close-biometric" })
        assertEquals(1, events.count { it == "close-database" })
        assertTrue(events.indexOf("lock-vault") < events.indexOf("close-database"))
    }

    @Test
    fun `duplicate close requests cannot duplicate cleanup or exit`() = runTest {
        val exits = AtomicInteger(0)
        val cancellations = AtomicInteger(0)
        val coordinator = coordinator(cancelBiometric = { cancellations.incrementAndGet() })

        repeat(1_000) {
            coordinator.requestClose(
                prepareWindowForExit = {},
                exitApplication = { exits.incrementAndGet() },
            )
        }
        advanceUntilIdle()

        assertTrue(coordinator.awaitCleanup(0L).completed)
        assertEquals(1, cancellations.get())
        assertEquals(1, exits.get())
    }

    @Test
    fun `window preparation runs synchronously on the close caller`() = runTest {
        val caller = Thread.currentThread()
        var preparationThread: Thread? = null
        val coordinator = coordinator()

        coordinator.requestClose(
            prepareWindowForExit = { preparationThread = Thread.currentThread() },
            exitApplication = {},
        )

        assertSame(caller, preparationThread)
        advanceUntilIdle()
        assertTrue(coordinator.awaitCleanup(0L).completed)
    }

    @Test
    fun `non returning vault lock cannot hold the window open or suppress other cleanup`() = runTest {
        val lockEntered = CompletableDeferred<Unit>()
        val clipboardCleared = CompletableDeferred<Unit>()
        val previewsPurged = CompletableDeferred<Unit>()
        val biometricClosed = CompletableDeferred<Unit>()
        val coordinator = coordinator(
            useBackgroundScope = true,
            lockVault = {
                lockEntered.complete(Unit)
                awaitCancellation()
            },
            clearClipboard = { clipboardCleared.complete(Unit) },
            purgePreviews = { previewsPurged.complete(Unit) },
            closeBiometric = { biometricClosed.complete(Unit) },
        )
        var exited = false

        coordinator.requestClose(
            prepareWindowForExit = {},
            exitApplication = { exited = true },
        )
        runCurrent()

        assertTrue(exited)
        assertTrue(lockEntered.isCompleted)
        assertTrue(clipboardCleared.isCompleted)
        assertTrue(previewsPurged.isCompleted)
        assertTrue(biometricClosed.isCompleted)
        assertFalse(coordinator.awaitCleanup(0L).completed)
        backgroundScope.coroutineContext[kotlinx.coroutines.Job]?.cancel()
    }

    @Test
    fun `database is closed even when vault locking reports failure`() = runTest {
        var databaseClosed = false
        val coordinator = coordinator(
            lockVault = { false },
            closeDatabase = { databaseClosed = true },
        )

        assertTrue(coordinator.requestShutdown())
        assertFalse(coordinator.requestShutdown())
        advanceUntilIdle()

        val report = coordinator.awaitCleanup(0L)
        assertTrue(report.completed)
        assertTrue(databaseClosed)
        assertEquals(1, report.failureCount)
    }

    @Test
    fun `preview cleanup failure is reported without suppressing other boundaries`() = runTest {
        var clipboardCleared = false
        var biometricClosed = false
        var databaseClosed = false
        val coordinator = coordinator(
            clearClipboard = { clipboardCleared = true },
            purgePreviews = { error("preview cleanup failed") },
            closeBiometric = { biometricClosed = true },
            closeDatabase = { databaseClosed = true },
        )

        assertTrue(coordinator.requestShutdown())
        advanceUntilIdle()

        val report = coordinator.awaitCleanup(0L)
        assertTrue(report.completed)
        assertEquals(1, report.failureCount)
        assertTrue(clipboardCleared)
        assertTrue(biometricClosed)
        assertTrue(databaseClosed)
    }

    private fun kotlinx.coroutines.test.TestScope.coordinator(
        useBackgroundScope: Boolean = false,
        cancelBiometric: () -> Unit = {},
        lockVault: suspend () -> Boolean = { true },
        clearClipboard: suspend () -> Unit = {},
        purgePreviews: suspend () -> Unit = {},
        closeBiometric: () -> Unit = {},
        closeDatabase: () -> Unit = {},
    ) = DesktopShutdownCoordinator(
        scope = if (useBackgroundScope) backgroundScope else this,
        operations = DesktopShutdownOperations(
            cancelBiometricPrompt = cancelBiometric,
            lockVault = lockVault,
            clearClipboard = clearClipboard,
            purgeAttachmentPreviews = purgePreviews,
            closeBiometricHost = closeBiometric,
            closeDatabase = closeDatabase,
        ),
        ioDispatcher = testScheduler.run { kotlinx.coroutines.test.StandardTestDispatcher(this) },
    )
}
