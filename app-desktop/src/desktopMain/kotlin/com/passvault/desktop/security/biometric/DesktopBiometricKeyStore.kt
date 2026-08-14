package com.passvault.desktop.security.biometric

import com.passvault.core.security.BiometricCapability
import com.passvault.core.security.BiometricKeyStore
import com.passvault.core.security.BiometricKeyStoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import java.security.MessageDigest

internal class DesktopBiometricKeyStore(
    private val bridge: DesktopBiometricBridge,
    private val promptCoordinator: DesktopBiometricPromptCoordinator = DesktopBiometricPromptCoordinator(),
    private val blockingDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BiometricKeyStore {
    private val mutex = Mutex()

    override suspend fun getCapability(): BiometricCapability = withContext(Dispatchers.IO) {
        bridge.getCapability()
    }

    override suspend fun contains(vaultId: String): Boolean = withExclusiveOperation {
        withVaultHash(vaultId) { vaultHash ->
            withContext(Dispatchers.IO) { bridge.contains(vaultHash) }
        }
    }

    override suspend fun enroll(vaultId: String, vaultKey: ByteArray): Result<Unit> = operationResult {
        require(vaultKey.size == VAULT_KEY_BYTES) { "Vault encryption keys must be exactly 32 bytes" }
        withExclusiveOperation {
            withVaultHash(vaultId) { vaultHash ->
                runCancellablePrompt {
                    promptCoordinator.withPrompt { bridge.enroll(vaultHash, vaultKey) }
                }
            }
        }
    }

    override suspend fun retrieve(vaultId: String): Result<ByteArray> {
        var producedKey: ByteArray? = null
        var transferred = false
        return try {
            val result = operationResult {
                withExclusiveOperation {
                    withVaultHash(vaultId) { vaultHash ->
                        runCancellablePrompt(onDiscard = ByteArray::wipe) {
                            promptCoordinator.withPrompt { bridge.retrieve(vaultHash) }
                        }
                            .also { producedKey = it }
                    }
                }
            }
            currentCoroutineContext().ensureActive()
            transferred = result.isSuccess
            result
        } finally {
            if (!transferred) producedKey?.fill(0)
        }
    }

    /**
     * A blocking native prompt runs as a structured IO child. Cancellation
     * actively reaches the OS prompt, then waits for native cleanup before the
     * mutex can admit another operation. A value produced after cancellation
     * is discarded through [onDiscard].
     */
    private suspend fun <T> runCancellablePrompt(
        onDiscard: (T) -> Unit = {},
        operation: () -> T,
    ): T = coroutineScope {
        val worker = async(blockingDispatcher) {
            val value = operation()
            try {
                currentCoroutineContext().ensureActive()
                value
            } catch (cancel: CancellationException) {
                onDiscard(value)
                throw cancel
            }
        }
        try {
            try {
                worker.await()
            } catch (cancel: CancellationException) {
                // Always arm the operation-scoped one-shot cancellation. The
                // worker may have passed coroutine admission but not yet run
                // its first instruction, so an "entered" flag has an inherent
                // check-then-enter race. If it never enters, finally clears the
                // unused signal after the cancelled worker has settled.
                runCatching { bridge.cancelPendingOrActive() }
                withContext(NonCancellable) { worker.join() }
                throw cancel
            }
        } finally {
            bridge.clearPendingCancellation()
        }
    }

    override suspend fun delete(vaultId: String): Result<Unit> = operationResult {
        withExclusiveOperation {
            withVaultHash(vaultId) { vaultHash ->
                withContext(Dispatchers.IO) { bridge.delete(vaultHash) }
            }
        }
    }

    /**
     * The OS prompt boundary is single-owner and deliberately non-queuing.
     * Rapid duplicate calls fail immediately instead of becoming stale prompts
     * that replay after the user has completed or cancelled the first one.
     */
    private suspend inline fun <T> withExclusiveOperation(
        crossinline operation: suspend () -> T,
    ): T {
        if (!mutex.tryLock()) throw DesktopBiometricBridgeException.Busy
        return try {
            operation()
        } finally {
            mutex.unlock()
        }
    }

    private suspend inline fun <T> operationResult(crossinline operation: suspend () -> T): Result<T> = try {
        Result.success(operation())
    } catch (cancel: CancellationException) {
        throw cancel
    } catch (error: DesktopBiometricBridgeException) {
        Result.failure(error.toKeyStoreException())
    } catch (_: Exception) {
        Result.failure(BiometricKeyStoreException.AuthenticationFailed())
    }

    private suspend inline fun <T> withVaultHash(
        vaultId: String,
        crossinline block: suspend (ByteArray) -> T,
    ): T {
        val hash = MessageDigest.getInstance("SHA-256").digest(vaultId.encodeToByteArray())
        return try {
            block(hash)
        } finally {
            hash.fill(0)
        }
    }

    private companion object {
        const val VAULT_KEY_BYTES = 32
    }
}

private fun ByteArray.wipe() {
    fill(0)
}

private fun DesktopBiometricBridgeException.toKeyStoreException(): BiometricKeyStoreException = when (this) {
    DesktopBiometricBridgeException.Cancelled -> BiometricKeyStoreException.Cancelled()
    DesktopBiometricBridgeException.NotAvailable -> BiometricKeyStoreException.NotAvailable()
    DesktopBiometricBridgeException.NotEnrolled -> BiometricKeyStoreException.NotEnrolled()
    DesktopBiometricBridgeException.LockedOut -> BiometricKeyStoreException.LockedOut()
    DesktopBiometricBridgeException.NotEnabled -> BiometricKeyStoreException.NotEnabled()
    DesktopBiometricBridgeException.Invalidated -> BiometricKeyStoreException.Invalidated()
    DesktopBiometricBridgeException.AuthenticationFailed,
    DesktopBiometricBridgeException.Busy,
    DesktopBiometricBridgeException.Internal,
    -> BiometricKeyStoreException.AuthenticationFailed()
}
