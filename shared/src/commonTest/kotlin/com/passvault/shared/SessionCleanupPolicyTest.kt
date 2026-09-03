package com.passvault.shared

import com.passvault.core.domain.model.SessionId
import com.passvault.core.domain.model.VaultSessionState
import com.passvault.core.domain.repository.LockReason
import com.passvault.core.navigation.AuthRoute
import com.passvault.core.navigation.VaultRoute
import com.passvault.core.security.ClipboardService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SessionCleanupPolicyTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `lock transition cancellation cannot interrupt clipboard cleanup`() = runTest {
        val clipboard = BlockingClipboardService()
        val cleanup = launch { clipboard.clearForLockTransition() }
        clipboard.started.await()

        cleanup.cancel()
        runCurrent()
        assertFalse(cleanup.isCompleted)

        clipboard.allowClear.complete(Unit)
        cleanup.cancelAndJoin()
        assertTrue(clipboard.cleared)
    }

    @Test
    fun `failed unlock keeps unlock feedback visible`() {
        val policy = sessionCleanupPolicy(VaultSessionState.Locked())

        assertFalse(policy.clearSensitiveUiState)
        assertFalse(policy.clearUnlockUiState)
        assertFalse(policy.clearClipboard)
    }

    @Test
    fun `actual lock clears unlock input`() {
        val state = VaultSessionState.Locking(LockReason.Manual)

        val policy = sessionCleanupPolicy(state)

        assertTrue(policy.clearSensitiveUiState)
        assertTrue(policy.clearUnlockUiState)
        assertTrue(policy.clearClipboard)
        assertFalse(policy.preserveBackupRestore)
    }

    @Test
    fun `iOS background lock preserves its bounded sensitive clipboard`() {
        val lockingPolicy = sessionCleanupPolicy(
            sessionState = VaultSessionState.Locking(LockReason.Background),
            preserveClipboardOnBackgroundLock = true,
        )
        val conflatedCompletionPolicy = sessionCleanupPolicy(
            sessionState = VaultSessionState.Locked(LockReason.Background),
            previousSessionObservation = SessionCleanupObservation(SessionPhase.UNLOCKED),
            preserveClipboardOnBackgroundLock = true,
        )
        val observedCompletionPolicy = sessionCleanupPolicy(
            sessionState = VaultSessionState.Locked(LockReason.Background),
            previousSessionObservation = SessionCleanupObservation(
                phase = SessionPhase.LOCKING,
                lockingReason = LockReason.Background,
            ),
            preserveClipboardOnBackgroundLock = true,
        )

        assertFalse(lockingPolicy.clearClipboard)
        assertFalse(conflatedCompletionPolicy.clearClipboard)
        assertFalse(observedCompletionPolicy.clearClipboard)
        assertTrue(lockingPolicy.clearSensitiveUiState)
        assertTrue(conflatedCompletionPolicy.clearSensitiveUiState)
    }

    @Test
    fun `iOS non-background locks still clear sensitive clipboard`() {
        val reasons = listOf(
            LockReason.Manual,
            LockReason.AutoLock,
            LockReason.MemoryPressure,
            LockReason.Restore,
        )

        reasons.forEach { reason ->
            val lockingPolicy = sessionCleanupPolicy(
                sessionState = VaultSessionState.Locking(reason),
                preserveClipboardOnBackgroundLock = true,
            )
            val conflatedCompletionPolicy = sessionCleanupPolicy(
                sessionState = VaultSessionState.Locked(reason),
                previousSessionObservation = SessionCleanupObservation(SessionPhase.UNLOCKED),
                preserveClipboardOnBackgroundLock = true,
            )

            assertTrue(lockingPolicy.clearClipboard, "Locking($reason) must clear")
            assertTrue(conflatedCompletionPolicy.clearClipboard, "Locked($reason) must clear")
        }
    }

    @Test
    fun `superseding non-background lock cannot inherit background clipboard preservation`() {
        val observedBackgroundLock = SessionCleanupObservation(
            phase = SessionPhase.LOCKING,
            lockingReason = LockReason.Background,
        )
        val reasons = listOf(
            LockReason.Manual,
            LockReason.AutoLock,
            LockReason.MemoryPressure,
            LockReason.Restore,
        )

        reasons.forEach { reason ->
            val policy = sessionCleanupPolicy(
                sessionState = VaultSessionState.Locked(reason),
                previousSessionObservation = observedBackgroundLock,
                preserveClipboardOnBackgroundLock = true,
            )

            assertTrue(policy.clearClipboard, "Superseding Locked($reason) must clear")
            assertTrue(policy.clearSensitiveUiState, "Superseding Locked($reason) must scrub UI")
        }
    }

    @Test
    fun `restore lock preserves the restore coordinator while scrubbing other state`() {
        val policy = sessionCleanupPolicy(
            sessionState = VaultSessionState.Locking(LockReason.Restore),
            restoreInProgress = true,
        )

        assertTrue(policy.clearSensitiveUiState)
        assertTrue(policy.clearUnlockUiState)
        assertTrue(policy.clearClipboard)
        assertTrue(policy.preserveBackupRestore)
    }

    @Test
    fun `restore reason without an owned import performs ordinary lock cleanup`() {
        val policy = sessionCleanupPolicy(VaultSessionState.Locking(LockReason.Restore))

        assertTrue(policy.clearSensitiveUiState)
        assertTrue(policy.clearUnlockUiState)
        assertTrue(policy.clearClipboard)
        assertFalse(policy.preserveBackupRestore)
    }

    @Test
    fun `conflated ordinary lock still scrubs singleton feature state`() {
        val policy = sessionCleanupPolicy(
            sessionState = VaultSessionState.Locked(LockReason.Background),
            previousSessionObservation = SessionCleanupObservation(SessionPhase.UNLOCKED),
        )

        assertTrue(policy.clearSensitiveUiState)
        assertTrue(policy.clearUnlockUiState)
        assertTrue(policy.clearClipboard)
        assertFalse(policy.preserveBackupRestore)
    }

    @Test
    fun `conflated restore lock preserves only its active restore coordinator`() {
        val policy = sessionCleanupPolicy(
            sessionState = VaultSessionState.Locked(LockReason.Restore),
            previousSessionObservation = SessionCleanupObservation(SessionPhase.UNLOCKED),
            restoreInProgress = true,
        )

        assertTrue(policy.clearSensitiveUiState)
        assertTrue(policy.clearUnlockUiState)
        assertTrue(policy.clearClipboard)
        assertTrue(policy.preserveBackupRestore)
    }

    @Test
    fun `conflated background lock cancels an import instead of impersonating restore`() {
        val policy = sessionCleanupPolicy(
            sessionState = VaultSessionState.Locked(LockReason.Background),
            previousSessionObservation = SessionCleanupObservation(SessionPhase.UNLOCKED),
            restoreInProgress = true,
        )

        assertTrue(policy.clearSensitiveUiState)
        assertTrue(policy.clearUnlockUiState)
        assertTrue(policy.clearClipboard)
        assertFalse(policy.preserveBackupRestore)
    }

    @Test
    fun `completed lock scrubs singleton state after UI recreation missed Locking`() {
        val policy = sessionCleanupPolicy(
            sessionState = VaultSessionState.Locked(LockReason.Manual),
            previousSessionObservation = SessionCleanupObservation(SessionPhase.UNINITIALIZED),
        )

        assertTrue(policy.clearSensitiveUiState)
        assertTrue(policy.clearUnlockUiState)
        assertTrue(policy.clearClipboard)
        assertFalse(policy.preserveBackupRestore)
    }

    @Test
    fun `failed unlock transition keeps its feedback`() {
        val policy = sessionCleanupPolicy(
            sessionState = VaultSessionState.Locked(),
            previousSessionObservation = SessionCleanupObservation(SessionPhase.UNLOCKING),
        )

        assertFalse(policy.clearSensitiveUiState)
        assertFalse(policy.clearUnlockUiState)
        assertFalse(policy.clearClipboard)
        assertFalse(policy.preserveBackupRestore)
    }

    @Test
    fun `initial locked transition does not impersonate a lock`() {
        val policy = sessionCleanupPolicy(
            sessionState = VaultSessionState.Locked(),
            previousSessionObservation = SessionCleanupObservation(SessionPhase.UNINITIALIZED),
        )

        assertFalse(policy.clearSensitiveUiState)
        assertFalse(policy.clearUnlockUiState)
        assertFalse(policy.clearClipboard)
        assertFalse(policy.preserveBackupRestore)
    }

    @Test
    fun `session transition tracker does not retain session identifier`() {
        val state = VaultSessionState.Unlocked(SessionId("sensitive-session-id"))

        assertEquals(
            SessionCleanupObservation(SessionPhase.UNLOCKED),
            state.toSessionCleanupObservation(),
        )
        assertEquals(
            SessionCleanupObservation(SessionPhase.LOCKING, LockReason.Background),
            VaultSessionState.Locking(LockReason.Background).toSessionCleanupObservation(),
        )
    }

    @Test
    fun `vault existence failure is not treated as a missing vault`() {
        assertEquals(InitialRouteState.Error, resolveInitialRoute(Result.failure(Exception("storage"))))
    }

    @Test
    fun `vault existence chooses the matching authentication route`() {
        assertEquals(
            InitialRouteState.Ready(AuthRoute.Unlock),
            resolveInitialRoute(Result.success(true)),
        )
        assertEquals(
            InitialRouteState.Ready(AuthRoute.Onboarding),
            resolveInitialRoute(Result.success(false)),
        )
    }

    @Test
    fun `command route guard reevaluates the latest session after unlock`() {
        val route = VaultRoute.Vault

        assertTrue(shouldRedirectToUnlock(route, VaultSessionState.Locked()))
        assertFalse(
            shouldRedirectToUnlock(
                route,
                VaultSessionState.Unlocked(SessionId("current-session")),
            ),
        )
    }

    @Test
    fun `locked observer remains fail closed`() {
        assertTrue(shouldGuardUnlockedRoutes(VaultSessionState.Locking(LockReason.Manual)))
        assertTrue(shouldGuardUnlockedRoutes(VaultSessionState.Locked()))
    }

    @Test
    fun `UI security acknowledgement requires a new request and guarded locked route`() {
        assertTrue(
            shouldAcknowledgeVaultUiSecurity(
                requestedEpoch = 2L,
                acknowledgedEpoch = 1L,
                sessionState = VaultSessionState.Locked(LockReason.Background),
                route = AuthRoute.Unlock,
            ),
        )
        assertFalse(
            shouldAcknowledgeVaultUiSecurity(
                requestedEpoch = 2L,
                acknowledgedEpoch = 2L,
                sessionState = VaultSessionState.Locked(LockReason.Background),
                route = AuthRoute.Unlock,
            ),
        )
        assertFalse(
            shouldAcknowledgeVaultUiSecurity(
                requestedEpoch = 2L,
                acknowledgedEpoch = 1L,
                sessionState = VaultSessionState.Unlocked(SessionId("active")),
                route = VaultRoute.Vault,
            ),
        )
        assertFalse(
            shouldAcknowledgeVaultUiSecurity(
                requestedEpoch = 2L,
                acknowledgedEpoch = 1L,
                sessionState = VaultSessionState.Locked(LockReason.Background),
                route = VaultRoute.Vault,
            ),
        )
    }
}

private class BlockingClipboardService : ClipboardService {
    val started = CompletableDeferred<Unit>()
    val allowClear = CompletableDeferred<Unit>()
    var cleared = false
        private set

    override suspend fun clear() {
        started.complete(Unit)
        allowClear.await()
        cleared = true
    }

    override suspend fun copySensitive(text: String, timeoutMs: Long) = error("Unused")

    override suspend fun copy(text: String) = error("Unused")

    override suspend fun containsSensitive(): Boolean = error("Unused")
}
