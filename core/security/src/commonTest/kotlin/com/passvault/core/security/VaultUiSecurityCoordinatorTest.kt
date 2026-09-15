package com.passvault.core.security

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VaultUiSecurityCoordinatorTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `stale acknowledgement cannot satisfy a newer security request`() = runTest {
        val coordinator = VaultUiSecurityCoordinator()
        val first = coordinator.requestAcknowledgement()
        coordinator.acknowledge(first)
        val second = coordinator.requestAcknowledgement()
        val waiter = async { coordinator.awaitAcknowledgement(second) }

        runCurrent()
        assertFalse(waiter.isCompleted)

        coordinator.acknowledge(first)
        runCurrent()
        assertFalse(waiter.isCompleted)

        coordinator.acknowledge(second)
        waiter.await()
        assertTrue(coordinator.isAcknowledged(second))
    }

    @Test
    fun `requests are monotonic and acknowledgements may coalesce`() {
        val coordinator = VaultUiSecurityCoordinator()
        val first = coordinator.requestAcknowledgement()
        val second = coordinator.requestAcknowledgement()

        coordinator.acknowledge(second)

        assertEquals(first + 1L, second)
        assertTrue(coordinator.isAcknowledged(first))
        assertTrue(coordinator.isAcknowledged(second))
    }

    @Test
    fun `zero and future acknowledgements are rejected`() {
        val coordinator = VaultUiSecurityCoordinator()
        val requested = coordinator.requestAcknowledgement()

        assertFailsWith<IllegalArgumentException> { coordinator.acknowledge(0L) }
        assertFailsWith<IllegalArgumentException> { coordinator.acknowledge(requested + 1L) }
    }

    @Test
    fun `zero and future await requests are rejected`() = runTest {
        val coordinator = VaultUiSecurityCoordinator()
        val requested = coordinator.requestAcknowledgement()

        assertFailsWith<IllegalArgumentException> { coordinator.awaitAcknowledgement(0L) }
        assertFailsWith<IllegalArgumentException> {
            coordinator.awaitAcknowledgement(requested + 1L)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `cancelled waiter cannot acknowledge or satisfy a later request`() = runTest {
        val coordinator = VaultUiSecurityCoordinator()
        val cancelledEpoch = coordinator.requestAcknowledgement()
        val cancelledWaiter = async { coordinator.awaitAcknowledgement(cancelledEpoch) }
        runCurrent()

        cancelledWaiter.cancelAndJoin()
        assertFalse(coordinator.isAcknowledged(cancelledEpoch))

        val retryEpoch = coordinator.requestAcknowledgement()
        val retryWaiter = async { coordinator.awaitAcknowledgement(retryEpoch) }
        coordinator.acknowledge(cancelledEpoch)
        runCurrent()
        assertFalse(retryWaiter.isCompleted)

        coordinator.acknowledge(retryEpoch)
        retryWaiter.await()
        assertTrue(coordinator.isAcknowledged(retryEpoch))
    }

    @Test
    fun `lock clears every registered entry owner synchronously on each transition`() {
        val coordinator = VaultUiSecurityCoordinator()
        val calls = mutableListOf<String>()
        coordinator.registerEntrySensitiveState(RecordingOwner("credential", calls))
        coordinator.registerEntrySensitiveState(RecordingOwner("two-factor", calls))

        coordinator.clearEntrySensitiveStateForLock()
        assertEquals(listOf("credential", "two-factor"), calls)

        coordinator.clearEntrySensitiveStateForLock()
        assertEquals(
            listOf("credential", "two-factor", "credential", "two-factor"),
            calls,
        )
    }

    @Test
    fun `closed entry registration is excluded without disturbing live owners`() {
        val coordinator = VaultUiSecurityCoordinator()
        val calls = mutableListOf<String>()
        val removed = coordinator.registerEntrySensitiveState(RecordingOwner("removed", calls))
        coordinator.registerEntrySensitiveState(RecordingOwner("live", calls))

        removed.close()
        removed.close()
        coordinator.clearEntrySensitiveStateForLock()

        assertEquals(listOf("live"), calls)
    }

    @Test
    fun `same entry owner cannot be registered twice`() {
        val coordinator = VaultUiSecurityCoordinator()
        val owner = RecordingOwner("owner", mutableListOf())
        coordinator.registerEntrySensitiveState(owner)

        assertFailsWith<IllegalStateException> {
            coordinator.registerEntrySensitiveState(owner)
        }
    }

    private class RecordingOwner(
        private val name: String,
        private val calls: MutableList<String>,
    ) : EntrySensitiveStateOwner {
        override fun clearForLock() {
            calls += name
        }
    }
}
