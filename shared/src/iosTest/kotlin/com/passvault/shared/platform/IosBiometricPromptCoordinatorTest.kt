package com.passvault.shared.platform

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IosBiometricPromptCoordinatorTest {

    @Test
    fun activeCancellationInvalidatesOnceAndMakesCallbacksStale() {
        val coordinator = IosBiometricPromptCoordinator<FakeContext>(FakeContext::invalidate)
        val operation = requireNotNull(coordinator.beginOperation())
        val context = FakeContext()
        var reportedCancellations = 0
        assertTrue(coordinator.activate(operation, context) { reportedCancellations += 1 })

        coordinator.cancelActive()
        coordinator.cancelActive()

        assertEquals(1, context.invalidationCount)
        assertEquals(1, reportedCancellations)
        assertFalse(coordinator.finishPrompt(operation, context, complete = {}))
    }

    @Test
    fun lockBeforeContextRegistrationRejectsTheStaleOperation() {
        val coordinator = IosBiometricPromptCoordinator<FakeContext>(FakeContext::invalidate)
        val operation = requireNotNull(coordinator.beginOperation())
        val context = FakeContext()
        var reportedCancellations = 0

        coordinator.cancelActive()

        assertFalse(coordinator.activate(operation, context) { reportedCancellations += 1 })
        assertEquals(1, context.invalidationCount)
        assertEquals(1, reportedCancellations)
        assertFalse(coordinator.finishPrompt(operation, context, complete = {}))
    }

    @Test
    fun staleCallbackCannotCompleteANewerContext() {
        val coordinator = IosBiometricPromptCoordinator<FakeContext>(FakeContext::invalidate)
        val firstOperation = requireNotNull(coordinator.beginOperation())
        val firstContext = FakeContext()
        assertTrue(coordinator.activate(firstOperation, firstContext, reportCancelled = {}))
        coordinator.cancelActive()

        val secondOperation = requireNotNull(coordinator.beginOperation())
        val secondContext = FakeContext()
        assertTrue(coordinator.activate(secondOperation, secondContext, reportCancelled = {}))

        assertFalse(coordinator.finishPrompt(firstOperation, firstContext, complete = {}))
        coordinator.cancel(firstOperation)
        assertTrue(coordinator.finishPrompt(secondOperation, secondContext, complete = {}))
        assertEquals(1, firstContext.invalidationCount)
        assertEquals(1, secondContext.invalidationCount)
    }

    @Test
    fun operationCancellationClearsOnlyItsActiveContext() {
        val coordinator = IosBiometricPromptCoordinator<FakeContext>(FakeContext::invalidate)
        val operation = requireNotNull(coordinator.beginOperation())
        val context = FakeContext()
        var reportedCancellations = 0
        assertTrue(coordinator.activate(operation, context) { reportedCancellations += 1 })

        coordinator.cancel(operation)
        coordinator.cancel(operation)

        assertEquals(1, context.invalidationCount)
        assertEquals(1, reportedCancellations)
        assertFalse(coordinator.finishPrompt(operation, context, complete = {}))
    }

    @Test
    fun terminalCompletionInvalidatesContextAndReleasesOperationForReuse() = runTest {
        val coordinator = IosBiometricPromptCoordinator<FakeContext>(FakeContext::invalidate)
        var completionCount = 0

        val result = coordinator.withOperation(onBusy = { "busy" }) { operation ->
            val context = FakeContext()
            assertTrue(coordinator.activate(operation, context, reportCancelled = {}))
            assertTrue(
                coordinator.finishPrompt(operation, context) {
                    completionCount += 1
                    // A continuation may resume undispatched and immediately unwind
                    // withOperation. Completion must therefore run outside NSLock.
                    coordinator.cancelActive()
                },
            )
            assertEquals(1, context.invalidationCount)
            "first"
        }

        assertEquals("first", result)
        assertEquals(1, completionCount)
        assertEquals("second", coordinator.withOperation(onBusy = { "busy" }) { "second" })
    }

    @Test
    fun concurrentProductionOperationsAreRejectedInsteadOfQueued() = runTest {
        val coordinator = IosBiometricPromptCoordinator<FakeContext>(FakeContext::invalidate)
        val firstEntered = CompletableDeferred<Unit>()
        val releaseFirst = CompletableDeferred<Unit>()
        val first = async {
            coordinator.withOperation(onBusy = { "busy" }) {
                firstEntered.complete(Unit)
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
    }

    private class FakeContext {
        var invalidationCount: Int = 0
            private set

        fun invalidate() {
            invalidationCount += 1
        }
    }
}
