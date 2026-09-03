package com.passvault.core.database.repository

import com.passvault.core.crypto.CryptoEngine
import com.passvault.core.security.BiometricAvailability
import com.passvault.core.security.BiometricCapability
import com.passvault.core.security.BiometricFailureReason
import com.passvault.core.security.BiometricKeyStore
import com.passvault.core.security.BiometricKeyStoreException
import com.passvault.core.security.BiometricOperationResult
import com.passvault.core.security.BiometricType
import com.passvault.core.security.BiometricUnlockService
import com.passvault.core.security.BiometricUnlockStatus
import kotlinx.coroutines.CancellationException

/**
 * Connects OS-protected biometric storage to the existing verified vault
 * session boundary. The platform store never receives a password or KEK.
 */
class DefaultBiometricUnlockService(
    private val vaultRepository: VaultRepositoryImpl,
    private val sessionManager: VaultSessionManager,
    private val keyStore: BiometricKeyStore,
    private val cryptoEngine: CryptoEngine,
) : BiometricUnlockService {

    override suspend fun getStatus(): BiometricUnlockStatus {
        val metadataResult = vaultRepository.getMetadata()
        val vaultId = metadataResult.getOrNull()?.id?.value
        val repositoryHasNoVault = vaultId == null && vaultRepository.exists().getOrNull() == false
        if (vaultId != null || repositoryHasNoVault) {
            resultCall { keyStore.reconcile(vaultId) }.rethrowCancellationFailure()
        }
        val capability = valueCall { keyStore.getCapability() }
            .getOrElse {
                return BiometricUnlockStatus(
                    capability = BiometricCapability(
                        type = BiometricType.GENERIC,
                        availability = BiometricAvailability.UNAVAILABLE,
                    ),
                    isEnabled = false,
                )
            }

        val enabled = if (vaultId == null) {
            false
        } else {
            valueCall { keyStore.contains(vaultId) }.getOrDefault(false)
        }
        return BiometricUnlockStatus(capability = capability, isEnabled = enabled)
    }

    override suspend fun enable(): BiometricOperationResult {
        val capability = valueCall { keyStore.getCapability() }.getOrNull()
        val vaultId = vaultRepository.getMetadata().getOrNull()?.id?.value
        return when {
            capability == null || vaultId == null -> {
                BiometricOperationResult.Failure(BiometricFailureReason.INTERNAL_ERROR)
            }
            capability.availability == BiometricAvailability.NOT_ENROLLED -> {
                BiometricOperationResult.Failure(BiometricFailureReason.NOT_ENROLLED)
            }
            capability.availability == BiometricAvailability.LOCKED_OUT -> {
                BiometricOperationResult.Failure(BiometricFailureReason.LOCKED_OUT)
            }
            capability.availability == BiometricAvailability.UNAVAILABLE -> {
                BiometricOperationResult.Failure(BiometricFailureReason.NOT_AVAILABLE)
            }
            else -> {
                val activeVaultId = requireNotNull(vaultId)
                resultCall {
                    sessionManager.withUnlockedSession { vaultKey ->
                        keyStore.enroll(activeVaultId, vaultKey)
                    }
                }.fold(
                    onSuccess = { BiometricOperationResult.Success },
                    onFailure = { error ->
                        if (error is VaultSessionLockedException) {
                            BiometricOperationResult.Failure(BiometricFailureReason.VAULT_LOCKED)
                        } else {
                            error.toOperationResult()
                        }
                    },
                )
            }
        }
    }

    override suspend fun disable(): BiometricOperationResult {
        val vaultId = vaultRepository.getMetadata().getOrNull()?.id?.value
            ?: return BiometricOperationResult.Failure(BiometricFailureReason.INTERNAL_ERROR)
        return resultCall { keyStore.delete(vaultId) }.toOperationResult()
    }

    override suspend fun unlock(): BiometricOperationResult {
        val vaultId = vaultRepository.getMetadata().getOrNull()?.id?.value
        val enabledResult = vaultId?.let { id -> valueCall { keyStore.contains(id) } }
        return when {
            vaultId == null || enabledResult == null || enabledResult.isFailure -> {
                enabledResult?.exceptionOrNull()?.toOperationResult()
                    ?: BiometricOperationResult.Failure(BiometricFailureReason.INTERNAL_ERROR)
            }
            enabledResult.getOrNull() != true -> {
                BiometricOperationResult.Failure(BiometricFailureReason.NOT_ENABLED)
            }
            else -> {
                val activeVaultId = requireNotNull(vaultId)
                val keyResult = resultCall { keyStore.retrieve(activeVaultId) }
                val vaultKey = keyResult.getOrNull()
                if (vaultKey == null) {
                    keyResult.exceptionOrNull()?.toOperationResult()
                        ?: BiometricOperationResult.Failure(BiometricFailureReason.INTERNAL_ERROR)
                } else {
                    try {
                        val unlockResult = vaultRepository.unlockWithBiometricKey(vaultKey)
                        if (unlockResult.isSuccess) {
                            BiometricOperationResult.Success
                        } else if (unlockResult.exceptionOrNull() is BiometricVaultKeyRejectedException) {
                            // The invalid key has already been rejected. A later disable
                            // attempt can retry if secure-storage deletion fails.
                            resultCall { keyStore.delete(activeVaultId) }.rethrowCancellationFailure()
                            BiometricOperationResult.Failure(BiometricFailureReason.INVALIDATED)
                        } else {
                            unlockResult.exceptionOrNull()?.toOperationResult()
                                ?: BiometricOperationResult.Failure(BiometricFailureReason.INTERNAL_ERROR)
                        }
                    } finally {
                        cryptoEngine.secureWipe(vaultKey)
                    }
                }
            }
        }
    }

    private fun Result<Unit>.toOperationResult(): BiometricOperationResult = fold(
        onSuccess = { BiometricOperationResult.Success },
        onFailure = { error -> error.toOperationResult() },
    )

    private fun Throwable.toOperationResult(): BiometricOperationResult = when (this) {
        is CancellationException -> throw this
        is BiometricKeyStoreException.Cancelled -> BiometricOperationResult.Cancelled
        is BiometricKeyStoreException.NotAvailable -> {
            BiometricOperationResult.Failure(BiometricFailureReason.NOT_AVAILABLE)
        }
        is BiometricKeyStoreException.NotEnrolled -> {
            BiometricOperationResult.Failure(BiometricFailureReason.NOT_ENROLLED)
        }
        is BiometricKeyStoreException.LockedOut -> {
            BiometricOperationResult.Failure(BiometricFailureReason.LOCKED_OUT)
        }
        is BiometricKeyStoreException.NotEnabled -> {
            BiometricOperationResult.Failure(BiometricFailureReason.NOT_ENABLED)
        }
        is BiometricKeyStoreException.Invalidated -> {
            BiometricOperationResult.Failure(BiometricFailureReason.INVALIDATED)
        }
        is BiometricKeyStoreException.AuthenticationFailed -> {
            BiometricOperationResult.Failure(BiometricFailureReason.AUTHENTICATION_FAILED)
        }
        else -> BiometricOperationResult.Failure(BiometricFailureReason.INTERNAL_ERROR)
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend inline fun <T> valueCall(crossinline block: suspend () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (error: Exception) {
            Result.failure(error)
        }

    @Suppress("TooGenericExceptionCaught")
    private suspend inline fun <T> resultCall(crossinline block: suspend () -> Result<T>): Result<T> =
        try {
            block()
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (error: Exception) {
            Result.failure(error)
        }

    private fun Result<*>.rethrowCancellationFailure() {
        val error = exceptionOrNull()
        if (error is CancellationException) throw error
    }
}
