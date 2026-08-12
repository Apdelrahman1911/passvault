package com.passvault.desktop

import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.model.SessionId
import com.passvault.core.domain.model.VaultId
import com.passvault.core.domain.model.VaultMetadata
import com.passvault.core.domain.model.VaultSessionState
import com.passvault.core.domain.repository.LockReason
import com.passvault.core.domain.repository.VaultRepository
import com.passvault.core.security.ClipboardService
import com.passvault.core.security.VaultUiSecurityCoordinator
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DesktopSessionCleanupTest {

    @Test
    fun `desktop session actions require an active unlocked session`() {
        assertFalse(areDesktopSessionActionsEnabled(VaultSessionState.Uninitialized))
        assertFalse(areDesktopSessionActionsEnabled(VaultSessionState.Locked()))
        assertFalse(areDesktopSessionActionsEnabled(VaultSessionState.Locking(LockReason.Background)))
        assertTrue(areDesktopSessionActionsEnabled(VaultSessionState.Unlocked(SessionId("active-session"))))
    }

    @Test
    fun `caller cancellation cannot skip vault and clipboard cleanup`() = runBlocking {
        val repository = RecordingVaultRepository()
        val clipboard = RecordingClipboardService()
        val started = CompletableDeferred<Unit>()
        val job = launch {
            try {
                started.complete(Unit)
                awaitCancellation()
            } finally {
                lockAndClear(repository, clipboard)
            }
        }

        started.await()
        job.cancelAndJoin()

        assertTrue(repository.locked)
        assertTrue(clipboard.cleared)
    }

    @Test
    fun `clipboard cleanup still runs when vault locking fails`() = runBlocking {
        val clipboard = RecordingClipboardService()

        val lockSucceeded = lockAndClear(
            vaultRepository = RecordingVaultRepository(lockFailure = IllegalStateException("lock failed")),
            clipboardService = clipboard,
        )

        assertFalse(lockSucceeded)
        assertTrue(clipboard.cleared)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `native lock while repository reports locked still waits for a fresh UI acknowledgement`() = runTest {
        val repository = RecordingVaultRepository()
        val clipboard = RecordingClipboardService()
        val coordinator = VaultUiSecurityCoordinator()
        val secured = async {
            lockClearAndAwaitUiSecurity(repository, clipboard, coordinator)
        }

        advanceTimeBy(3)
        runCurrent()
        assertTrue(repository.locked)
        assertEquals(1, repository.lockCalls)
        assertTrue(clipboard.cleared)
        assertFalse(secured.isCompleted)

        val requestEpoch = coordinator.requestedEpoch.value
        coordinator.acknowledge(requestEpoch)
        assertTrue(secured.await())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `terminal lock failure never requests UI acknowledgement or secures content`() = runTest {
        val repository = RecordingVaultRepository(returnLockFailure = true)
        val coordinator = VaultUiSecurityCoordinator()
        val secured = async {
            lockClearAndAwaitUiSecurity(repository, RecordingClipboardService(), coordinator)
        }

        advanceUntilIdle()

        assertFalse(secured.await())
        assertEquals(3, repository.lockCalls)
        assertEquals(0L, coordinator.requestedEpoch.value)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `cancelled UI wait cannot secure content and a fresh retry can complete`() = runTest {
        val repository = RecordingVaultRepository()
        val coordinator = VaultUiSecurityCoordinator()
        val first = async {
            lockClearAndAwaitUiSecurity(repository, RecordingClipboardService(), coordinator)
        }
        advanceTimeBy(3)
        runCurrent()

        first.cancelAndJoin()
        assertEquals(1L, coordinator.requestedEpoch.value)
        assertEquals(0L, coordinator.acknowledgedEpoch.value)

        val retry = async {
            lockClearAndAwaitUiSecurity(repository, RecordingClipboardService(), coordinator)
        }
        advanceTimeBy(3)
        runCurrent()
        val retryEpoch = coordinator.requestedEpoch.value
        coordinator.acknowledge(retryEpoch)

        assertTrue(retry.await())
        assertEquals(2L, retryEpoch)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `missing UI acknowledgement times out without reporting secured content`() = runTest {
        val secured = async {
            lockClearAndAwaitUiSecurity(
                RecordingVaultRepository(),
                RecordingClipboardService(),
                VaultUiSecurityCoordinator(),
            )
        }

        advanceUntilIdle()

        assertFalse(secured.await())
    }
}

private class RecordingVaultRepository(
    private val lockFailure: Exception? = null,
    private val returnLockFailure: Boolean = false,
) : VaultRepository {
    var locked = false
        private set
    var lockCalls = 0
        private set

    override suspend fun lock(reason: LockReason): Result<Unit> {
        delay(1)
        lockCalls += 1
        lockFailure?.let { throw it }
        if (returnLockFailure) return Result.failure(IllegalStateException("lock failed"))
        locked = true
        return Result.success(Unit)
    }

    override suspend fun exists(): Result<Boolean> = error("Unused")

    override suspend fun create(masterPassword: SensitiveText): Result<VaultId> = error("Unused")

    override suspend fun unlock(masterPassword: SensitiveText): Result<SessionId> = error("Unused")

    override suspend fun changeMasterPassword(
        currentPassword: SensitiveText,
        newPassword: SensitiveText,
    ): Result<Unit> = error("Unused")

    override suspend fun getMetadata(): Result<VaultMetadata> = error("Unused")

    override fun getSessionState(): Flow<VaultSessionState> = flowOf(VaultSessionState.Locked())
}

private class RecordingClipboardService : ClipboardService {
    var cleared = false
        private set

    override suspend fun clear() {
        delay(1)
        cleared = true
    }

    override suspend fun copySensitive(text: String, timeoutMs: Long) = error("Unused")

    override suspend fun copy(text: String) = error("Unused")

    override suspend fun containsSensitive(): Boolean = error("Unused")
}
