package com.passvault.core.security

import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update

/**
 * Coordinates native privacy covers and sensitive entry state with the shared Compose security boundary.
 *
 * Platforms create a fresh request only after their serialized repository lock
 * and clipboard cleanup have finished. The shared UI acknowledges that exact
 * epoch only after sensitive entry and singleton state plus guarded navigation
 * have been committed and an additional Compose frame has elapsed.
 */
@OptIn(ExperimentalAtomicApi::class)
class VaultUiSecurityCoordinator {
    private val entrySensitiveStateOwners = AtomicReference<List<EntrySensitiveStateRegistration>>(emptyList())

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

    /**
     * Registers one navigation-entry owner for synchronous lock cleanup.
     *
     * The returned handle must be attached to the entry's ViewModel lifecycle. Closing it removes
     * the coordinator's strong reference without invoking cleanup; ViewModel teardown performs its
     * own final cleanup separately.
     */
    fun registerEntrySensitiveState(owner: EntrySensitiveStateOwner): AutoCloseable {
        val registration = EntrySensitiveStateRegistration(owner)
        updateEntrySensitiveStateOwners { current ->
            check(current.none { it.owner === owner }) {
                "Sensitive UI state owner is already registered"
            }
            current + registration
        }
        return object : AutoCloseable {
            override fun close() {
                updateEntrySensitiveStateOwners { current ->
                    current.filterNot { it === registration }
                }
            }
        }
    }

    /** Clears every live entry owner before clipboard cleanup and secure root replacement. */
    fun clearEntrySensitiveStateForLock() {
        entrySensitiveStateOwners.load().forEach { registration ->
            registration.owner.clearForLock()
        }
    }

    private inline fun updateEntrySensitiveStateOwners(
        transform: (List<EntrySensitiveStateRegistration>) -> List<EntrySensitiveStateRegistration>,
    ) {
        while (true) {
            val current = entrySensitiveStateOwners.load()
            val updated = transform(current)
            if (entrySensitiveStateOwners.compareAndSet(current, updated)) return
        }
    }

    private class EntrySensitiveStateRegistration(
        val owner: EntrySensitiveStateOwner,
    )
}
