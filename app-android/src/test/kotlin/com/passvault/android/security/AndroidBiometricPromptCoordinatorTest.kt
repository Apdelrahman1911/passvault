package com.passvault.android.security

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

class AndroidBiometricPromptCoordinatorTest {

    @Test
    fun activePromptCancellationIsDispatchedOnceAndMakesCallbacksStale() {
        val dispatched = ConcurrentLinkedQueue<() -> Unit>()
        val platformCancellations = AtomicInteger()
        val reportedCancellations = AtomicInteger()
        val coordinator = AndroidBiometricPromptCoordinator(dispatched::add)
        val operation = requireNotNull(coordinator.beginOperation())
        assertTrue(
            coordinator.activate(
                operation,
                cancelAuthentication = { platformCancellations.incrementAndGet() },
                reportCancelled = { reportedCancellations.incrementAndGet() },
            ),
        )

        coordinator.cancelActive()
        coordinator.cancelActive()

        assertEquals(1, reportedCancellations.get())
        assertEquals(1, dispatched.size)
        assertEquals(0, platformCancellations.get())
        assertFalse(coordinator.finishPrompt(operation))

        dispatched.remove().invoke()
        assertEquals(1, platformCancellations.get())
    }

    @Test
    fun lockBeforePromptRegistrationRejectsTheStaleOperation() {
        val platformCancellations = AtomicInteger()
        val reportedCancellations = AtomicInteger()
        val coordinator = AndroidBiometricPromptCoordinator { action -> action() }
        val operation = requireNotNull(coordinator.beginOperation())

        coordinator.cancelActive()

        assertFalse(
            coordinator.activate(
                operation,
                cancelAuthentication = { platformCancellations.incrementAndGet() },
                reportCancelled = { reportedCancellations.incrementAndGet() },
            ),
        )
        assertEquals(1, platformCancellations.get())
        assertEquals(1, reportedCancellations.get())
        assertFalse(coordinator.finishPrompt(operation))
    }

    @Test
    fun staleCallbackCannotCompleteANewerPrompt() {
        val firstPlatformCancellations = AtomicInteger()
        val secondPlatformCancellations = AtomicInteger()
        val coordinator = AndroidBiometricPromptCoordinator { action -> action() }
        val first = requireNotNull(coordinator.beginOperation())
        assertTrue(
            coordinator.activate(
                first,
                cancelAuthentication = { firstPlatformCancellations.incrementAndGet() },
                reportCancelled = {},
            ),
        )
        coordinator.cancelActive()

        val second = requireNotNull(coordinator.beginOperation())
        assertTrue(
            coordinator.activate(
                second,
                cancelAuthentication = { secondPlatformCancellations.incrementAndGet() },
                reportCancelled = {},
            ),
        )

        assertFalse(coordinator.finishPrompt(first))
        coordinator.cancel(first)
        assertTrue(coordinator.finishPrompt(second))
        coordinator.cancelActive()
        assertEquals(1, firstPlatformCancellations.get())
        assertEquals(0, secondPlatformCancellations.get())
    }

    @Test
    fun operationCancellationClearsOnlyItsActivePrompt() {
        val platformCancellations = AtomicInteger()
        val reportedCancellations = AtomicInteger()
        val coordinator = AndroidBiometricPromptCoordinator { action -> action() }
        val operation = requireNotNull(coordinator.beginOperation())
        assertTrue(
            coordinator.activate(
                operation,
                cancelAuthentication = { platformCancellations.incrementAndGet() },
                reportCancelled = { reportedCancellations.incrementAndGet() },
            ),
        )

        coordinator.cancel(operation)
        coordinator.cancel(operation)

        assertEquals(1, platformCancellations.get())
        assertEquals(1, reportedCancellations.get())
        assertFalse(coordinator.finishPrompt(operation))
    }

    @Test
    fun platformCancellationIsDispatchedAwayFromTheCallingThread() {
        val dispatched = ConcurrentLinkedQueue<() -> Unit>()
        val cancellationThread = AtomicReference<String?>()
        val coordinator = AndroidBiometricPromptCoordinator(dispatched::add)
        val operation = requireNotNull(coordinator.beginOperation())
        assertTrue(
            coordinator.activate(
                operation,
                cancelAuthentication = { cancellationThread.set(Thread.currentThread().name) },
                reportCancelled = {},
            ),
        )

        thread(name = "lock-caller") { coordinator.cancelActive() }.join()

        assertNull(cancellationThread.get())
        thread(name = "fake-main") { dispatched.remove().invoke() }.join()
        assertEquals("fake-main", cancellationThread.get())
    }

    @Test
    fun concurrentProductionOperationsAreRejectedInsteadOfQueued() = runBlocking {
        val coordinator = AndroidBiometricPromptCoordinator { action -> action() }
        val firstEntered = CompletableDeferred<Unit>()
        val releaseFirst = CompletableDeferred<Unit>()
        val first = async {
            coordinator.withOperation(onBusy = { "busy" }) { operation ->
                firstEntered.complete(Unit)
                assertTrue(coordinator.activate(operation, cancelAuthentication = {}, reportCancelled = {}))
                assertTrue(coordinator.finishPrompt(operation))
                releaseFirst.await()
                "first"
            }
        }
        firstEntered.await()
        var secondEntered = false

        val second = coordinator.withOperation(onBusy = { "busy" }) {
            secondEntered = true
            "second"
        }

        assertEquals("busy", second)
        assertFalse(secondEntered)
        releaseFirst.complete(Unit)
        assertEquals("first", first.await())
        assertEquals("third", coordinator.withOperation(onBusy = { "busy" }) { "third" })
    }
}
