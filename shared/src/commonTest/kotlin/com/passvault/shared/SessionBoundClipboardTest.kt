package com.passvault.shared

import com.passvault.core.domain.model.SessionId
import com.passvault.core.domain.model.VaultSessionState
import com.passvault.core.domain.repository.LockReason
import com.passvault.core.security.ClipboardService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SessionBoundClipboardTest {
    @Test
    fun `locked session rejects a sensitive copy before reaching the clipboard`() = runTest {
        val sessionState = MutableStateFlow<VaultSessionState>(VaultSessionState.Locked())
        val clipboard = RecordingClipboardService()

        assertFailsWith<IllegalStateException> {
            copySensitiveWhileUnlocked(sessionState, clipboard, "secret", 30_000L)
        }

        assertEquals(0, clipboard.copyCount)
        assertEquals(0, clipboard.clearCount)
    }

    @Test
    fun `ordinary unlocked copy succeeds without immediate cleanup`() = runTest {
        val sessionState = unlockedSessionState()
        val clipboard = RecordingClipboardService()

        copySensitiveWhileUnlocked(sessionState, clipboard, "secret", 30_000L)

        assertEquals(1, clipboard.copyCount)
        assertEquals(0, clipboard.clearCount)
    }

    @Test
    fun `lock crossing a clipboard write clears the late secret and reports failure`() = runTest {
        val sessionState = unlockedSessionState()
        val clipboard = RecordingClipboardService(
            afterCopy = { sessionState.value = VaultSessionState.Locked() },
        )

        assertFailsWith<IllegalStateException> {
            copySensitiveWhileUnlocked(sessionState, clipboard, "secret", 30_000L)
        }

        assertEquals(1, clipboard.copyCount)
        assertEquals(1, clipboard.clearCount)
    }

    @Test
    fun `iOS background lock crossing a clipboard write preserves the bounded copy`() = runTest {
        val backgroundStates = listOf<VaultSessionState>(
            VaultSessionState.Locking(LockReason.Background),
            VaultSessionState.Locked(LockReason.Background),
        )

        backgroundStates.forEach { backgroundState ->
            val sessionState = unlockedSessionState()
            val clipboard = RecordingClipboardService(
                afterCopy = { sessionState.value = backgroundState },
            )

            copySensitiveWhileUnlocked(
                sessionState = sessionState,
                clipboardService = clipboard,
                text = "secret",
                timeoutMs = 30_000L,
                preserveClipboardOnBackgroundLock = true,
            )

            assertEquals(1, clipboard.copyCount)
            assertEquals(0, clipboard.clearCount)
        }
    }

    @Test
    fun `iOS stronger lock crossing a clipboard write still invalidates the copy`() = runTest {
        val reasons = listOf(
            LockReason.Manual,
            LockReason.AutoLock,
            LockReason.MemoryPressure,
            LockReason.Restore,
        )

        reasons.forEach { reason ->
            val sessionState = unlockedSessionState()
            val clipboard = RecordingClipboardService(
                afterCopy = { sessionState.value = VaultSessionState.Locked(reason) },
            )

            assertFailsWith<IllegalStateException> {
                copySensitiveWhileUnlocked(
                    sessionState = sessionState,
                    clipboardService = clipboard,
                    text = "secret",
                    timeoutMs = 30_000L,
                    preserveClipboardOnBackgroundLock = true,
                )
            }

            assertEquals(1, clipboard.copyCount)
            assertEquals(1, clipboard.clearCount)
        }
    }

    @Test
    fun `replacement session cannot inherit a clipboard write from the prior session`() = runTest {
        val sessionState = unlockedSessionState()
        val clipboard = RecordingClipboardService(
            afterCopy = {
                sessionState.value = VaultSessionState.Unlocked(SessionId("replacement-session"))
            },
        )

        assertFailsWith<IllegalStateException> {
            copySensitiveWhileUnlocked(sessionState, clipboard, "secret", 30_000L)
        }

        assertEquals(1, clipboard.copyCount)
        assertEquals(1, clipboard.clearCount)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `caller cancellation cannot skip cleanup after a clipboard write starts`() = runTest {
        val sessionState = unlockedSessionState()
        val clipboard = BlockingCopyClipboardService()
        val copy = async {
            copySensitiveWhileUnlocked(sessionState, clipboard, "secret", 30_000L)
        }
        clipboard.copyStarted.await()

        sessionState.value = VaultSessionState.Locked()
        copy.cancel()
        runCurrent()
        assertFalse(copy.isCompleted)

        clipboard.allowCopyToReturn.complete(Unit)
        copy.cancelAndJoin()
        assertTrue(clipboard.cleared)
    }

    private fun unlockedSessionState(): MutableStateFlow<VaultSessionState> =
        MutableStateFlow(VaultSessionState.Unlocked(SessionId("test-session")))
}

private class RecordingClipboardService(
    private val afterCopy: () -> Unit = {},
) : ClipboardService {
    var copyCount = 0
        private set
    var clearCount = 0
        private set

    override suspend fun copySensitive(text: String, timeoutMs: Long) {
        copyCount++
        afterCopy()
    }

    override suspend fun clear() {
        clearCount++
    }

    override suspend fun copy(text: String) = error("Unused")

    override suspend fun containsSensitive(): Boolean = error("Unused")
}

private class BlockingCopyClipboardService : ClipboardService {
    val copyStarted = CompletableDeferred<Unit>()
    val allowCopyToReturn = CompletableDeferred<Unit>()
    var cleared = false
        private set

    override suspend fun copySensitive(text: String, timeoutMs: Long) {
        copyStarted.complete(Unit)
        allowCopyToReturn.await()
    }

    override suspend fun clear() {
        cleared = true
    }

    override suspend fun copy(text: String) = error("Unused")

    override suspend fun containsSensitive(): Boolean = error("Unused")
}
