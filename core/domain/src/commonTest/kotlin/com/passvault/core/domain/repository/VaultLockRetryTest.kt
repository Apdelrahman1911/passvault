package com.passvault.core.domain.repository

import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.model.SessionId
import com.passvault.core.domain.model.VaultId
import com.passvault.core.domain.model.VaultMetadata
import com.passvault.core.domain.model.VaultSessionState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VaultLockRetryTest {

    @Test
    fun `result failure is retried before reporting success`() = runTest {
        val repository = SequencedLockRepository(
            LockOutcome.Failure,
            LockOutcome.Success,
        )

        assertTrue(repository.lockWithBoundedRetry(LockReason.Background))
        assertEquals(2, repository.lockReasons.size)
        assertTrue(repository.lockReasons.all { it == LockReason.Background })
    }

    @Test
    fun `ordinary exception is retried without escaping the boundary`() = runTest {
        val repository = SequencedLockRepository(
            LockOutcome.Throws(IllegalStateException("provider")),
            LockOutcome.Success,
        )

        assertTrue(repository.lockWithBoundedRetry())
        assertEquals(2, repository.lockReasons.size)
    }

    @Test
    fun `exhausted failures stop after the bounded attempt count`() = runTest {
        val repository = SequencedLockRepository(
            LockOutcome.Failure,
            LockOutcome.Failure,
            LockOutcome.Failure,
            LockOutcome.Success,
        )

        assertFalse(repository.lockWithBoundedRetry())
        assertEquals(3, repository.lockReasons.size)
    }

    @Test
    fun `cancellation is never swallowed or retried`() = runTest {
        val repository = SequencedLockRepository(
            LockOutcome.Throws(CancellationException("cancelled")),
        )

        assertFailsWith<CancellationException> {
            repository.lockWithBoundedRetry()
        }
        assertEquals(1, repository.lockReasons.size)
    }
}

private class SequencedLockRepository(vararg outcomes: LockOutcome) : VaultRepository {
    private val outcomes = ArrayDeque(outcomes.toList())
    val lockReasons = mutableListOf<LockReason>()

    override suspend fun lock(reason: LockReason): Result<Unit> {
        lockReasons += reason
        return when (val outcome = outcomes.removeFirst()) {
            LockOutcome.Success -> Result.success(Unit)
            LockOutcome.Failure -> Result.failure(IllegalStateException("lock failed"))
            is LockOutcome.Throws -> throw outcome.error
        }
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

private sealed interface LockOutcome {
    data object Success : LockOutcome
    data object Failure : LockOutcome
    data class Throws(val error: Throwable) : LockOutcome
}
