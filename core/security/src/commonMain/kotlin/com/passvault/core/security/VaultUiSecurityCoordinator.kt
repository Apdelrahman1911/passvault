package com.passvault.core.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update

/**
 * Coordinates native privacy covers with the shared Compose security boundary.
 *
 * Platforms create a fresh request only after their serialized repository lock
 * and clipboard cleanup have finished. The shared UI acknowledges that exact
 * epoch only after sensitive singleton state and guarded navigation have been
 * committed and an additional Compose frame has elapsed.
 */
class VaultUiSecurityCoordinator {
    private val _requestedEpoch = MutableStateFlow(0L)
    val requestedEpoch = _requestedEpoch.asStateFlow()

    private val _acknowledgedEpoch = MutableStateFlow(0L)
    val acknowledgedEpoch = _acknowledgedEpoch.asStateFlow()

    fun requestAcknowledgement(): Long {
        var requested = 0L
        _requestedEpoch.update { current ->
            check(current < Long.MAX_VALUE) { "Vault UI security epoch exhausted" }
            (current + 1L).also { requested = it }
        }
        return requested
    }

    fun acknowledge(epoch: Long) {
        require(epoch > 0L && epoch <= _requestedEpoch.value) {
            "Vault UI security acknowledgement is outside the requested range"
        }
        _acknowledgedEpoch.update { current -> maxOf(current, epoch) }
    }

    fun isAcknowledged(epoch: Long): Boolean = epoch > 0L && _acknowledgedEpoch.value >= epoch

    suspend fun awaitAcknowledgement(epoch: Long) {
        require(epoch > 0L && epoch <= _requestedEpoch.value) {
            "Vault UI security request is outside the requested range"
        }
        acknowledgedEpoch.first { acknowledged -> acknowledged >= epoch }
    }
}
