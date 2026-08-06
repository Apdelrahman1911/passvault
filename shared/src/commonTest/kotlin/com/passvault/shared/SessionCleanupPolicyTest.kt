package com.passvault.shared

import com.passvault.core.domain.model.SecurityError
import com.passvault.core.domain.model.VaultSessionState
import com.passvault.core.domain.repository.LockReason
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SessionCleanupPolicyTest {
    @Test
    fun `failed unlock keeps unlock feedback visible`() {
        assertFalse(shouldClearUnlockUiDuringSessionCleanup(VaultSessionState.Locked))
    }

    @Test
    fun `fatal unlock error remains visible`() {
        val state = VaultSessionState.FatalError(SecurityError.Fatal("redacted"))

        assertFalse(shouldClearUnlockUiDuringSessionCleanup(state))
    }

    @Test
    fun `actual lock clears unlock input`() {
        val state = VaultSessionState.Locking(LockReason.Manual)

        assertTrue(shouldClearUnlockUiDuringSessionCleanup(state))
    }
}
