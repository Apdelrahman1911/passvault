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
        val capability = try {
            keyStore.getCapability()
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (_: Exception) {
            return BiometricUnlockStatus(
                capability = BiometricCapability(
                    type = BiometricType.GENERIC,
                    availability = BiometricAvailability.UNAVAILABLE,
                ),
                isEnabled = false,
            )
        }

        val vaultId = vaultRepository.getMetadata().getOrNull()?.id?.value
        val enabled = if (vaultId == null) {
            false
        } else {
            try {
                keyStore.contains(vaultId)
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Exception) {
                false
            }
        }
        return BiometricUnlockStatus(capability = capability, isEnabled = enabled)
    }

    override suspend fun enable(): BiometricOperationResult {
        val capability = try {
            keyStore.getCapability()
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (_: Exception) {
            return BiometricOperationResult.Failure(BiometricFailureReason.INTERNAL_ERROR)
        }
        when (capability.availability) {
            BiometricAvailability.NOT_ENROLLED -> {
                return BiometricOperationResult.Failure(BiometricFailureReason.NOT_ENROLLED)
            }
            BiometricAvailability.UNAVAILABLE -> {
                return BiometricOperationResult.Failure(BiometricFailureReason.NOT_AVAILABLE)
            }
            BiometricAvailability.AVAILABLE -> Unit
        }

        val vaultId = vaultRepository.getMetadata().getOrNull()?.id?.value
            ?: return BiometricOperationResult.Failure(BiometricFailureReason.INTERNAL_ERROR)
        val vaultKey = sessionManager.getCurrentVek()
            ?: return BiometricOperationResult.Failure(BiometricFailureReason.VAULT_LOCKED)
        return try {
            keyStore.enroll(vaultId, vaultKey).toOperationResult()
        } finally {
            cryptoEngine.secureWipe(vaultKey)
        }
    }

    override suspend fun disable(): BiometricOperationResult {
        val vaultId = vaultRepository.getMetadata().getOrNull()?.id?.value
            ?: return BiometricOperationResult.Failure(BiometricFailureReason.INTERNAL_ERROR)
        return keyStore.delete(vaultId).toOperationResult()
    }

    override suspend fun unlock(): BiometricOperationResult {
        val vaultId = vaultRepository.getMetadata().getOrNull()?.id?.value
            ?: return BiometricOperationResult.Failure(BiometricFailureReason.INTERNAL_ERROR)
        val isEnabled = try {
            keyStore.contains(vaultId)
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (_: Exception) {
            false
        }
        if (!isEnabled) {
            return BiometricOperationResult.Failure(BiometricFailureReason.NOT_ENABLED)
        }

        val keyResult = keyStore.retrieve(vaultId)
        val vaultKey = keyResult.getOrElse { return it.toOperationResult() }
        return try {
            if (vaultRepository.unlockWithBiometricKey(vaultKey).isSuccess) {
                BiometricOperationResult.Success
            } else {
                // A stored key that no longer authenticates the vault must not
                // remain available for repeated use.
                keyStore.delete(vaultId)
                BiometricOperationResult.Failure(BiometricFailureReason.INVALIDATED)
            }
        } finally {
            cryptoEngine.secureWipe(vaultKey)
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
}
