package com.passvault.core.domain.model

import com.passvault.core.domain.repository.LockReason

sealed interface VaultSessionState {
    data object Uninitialized : VaultSessionState
    /**
     * A completed locked state. [reason] is present only when this state was
     * reached through an explicit lock transition; initialization and failed
     * unlock attempts use `null`. Retaining the reason in the terminal state
     * keeps security cleanup correct when StateFlow conflates the preceding
     * [Locking] emission.
     */
    data class Locked(val reason: LockReason? = null) : VaultSessionState
    data object Unlocking : VaultSessionState
    data class Unlocked(val sessionId: SessionId) : VaultSessionState
    data class Locking(val reason: LockReason) : VaultSessionState
}
