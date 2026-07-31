package com.passvault.core.domain.model

import com.passvault.core.domain.repository.LockReason

sealed interface VaultSessionState {
    data object Uninitialized : VaultSessionState
    data object Locked : VaultSessionState
    data object Unlocking : VaultSessionState
    data class Unlocked(val sessionId: SessionId) : VaultSessionState
    data class Locking(val reason: LockReason) : VaultSessionState
    data class FatalError(val error: SecurityError) : VaultSessionState
}

sealed interface SecurityError {
    data class AuthenticationFailed(val attempts: Int) : SecurityError
    data class CorruptedData(val message: String) : SecurityError
    data class CryptoError(val message: String) : SecurityError
    data object SessionExpired : SecurityError
    data class Fatal(val message: String) : SecurityError
}
