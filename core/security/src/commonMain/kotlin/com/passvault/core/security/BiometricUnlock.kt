package com.passvault.core.security

/** The biometric name that should be presented to the user. */
enum class BiometricType {
    FACE_ID,
    TOUCH_ID,
    WINDOWS_HELLO,
    GENERIC,
}

/** Whether this device can currently perform biometric-only authentication. */
enum class BiometricAvailability {
    AVAILABLE,
    NOT_ENROLLED,
    LOCKED_OUT,
    UNAVAILABLE,
}

data class BiometricCapability(
    val type: BiometricType,
    val availability: BiometricAvailability,
)

data class BiometricUnlockStatus(
    val capability: BiometricCapability,
    val isEnabled: Boolean,
)

enum class BiometricFailureReason {
    NOT_AVAILABLE,
    NOT_ENROLLED,
    LOCKED_OUT,
    NOT_ENABLED,
    INVALIDATED,
    VAULT_LOCKED,
    AUTHENTICATION_FAILED,
    INTERNAL_ERROR,
}

sealed interface BiometricOperationResult {
    data object Success : BiometricOperationResult
    data object Cancelled : BiometricOperationResult
    data class Failure(val reason: BiometricFailureReason) : BiometricOperationResult
}

/**
 * App-facing biometric unlock boundary. Implementations must validate a
 * recovered vault key against authenticated vault metadata before opening a
 * session.
 */
interface BiometricUnlockService {
    suspend fun getStatus(): BiometricUnlockStatus
    suspend fun enable(): BiometricOperationResult
    suspend fun disable(): BiometricOperationResult
    suspend fun unlock(): BiometricOperationResult
}

/**
 * Platform storage boundary for a vault key protected by an OS biometric
 * policy. Implementations must not persist an unprotected key.
 */
interface BiometricKeyStore {
    suspend fun getCapability(): BiometricCapability
    suspend fun contains(vaultId: String): Boolean
    suspend fun enroll(vaultId: String, vaultKey: ByteArray): Result<Unit>
    suspend fun retrieve(vaultId: String): Result<ByteArray>
    suspend fun delete(vaultId: String): Result<Unit>

    /** Retires platform entries outside the active single-vault identity; null means no vault exists. */
    suspend fun reconcile(activeVaultId: String?): Result<Unit> = Result.success(Unit)
}

/** Cancels a platform biometric prompt before a vault-lock transition waits for it. */
fun interface BiometricPromptController {
    fun cancelActive()
}

/** Platforms without an externally cancellable prompt retain their existing lifecycle behavior. */
object NoOpBiometricPromptController : BiometricPromptController {
    override fun cancelActive() = Unit
}

sealed class BiometricKeyStoreException(message: String) : Exception(message) {
    class Cancelled : BiometricKeyStoreException("Biometric authentication was cancelled")
    class NotAvailable : BiometricKeyStoreException("Biometric authentication is unavailable")
    class NotEnrolled : BiometricKeyStoreException("No supported biometric is enrolled")
    class LockedOut : BiometricKeyStoreException("Biometric authentication is temporarily locked")
    class NotEnabled : BiometricKeyStoreException("Biometric unlock is not enabled")
    class Invalidated : BiometricKeyStoreException("Biometric enrollment changed")
    class AuthenticationFailed : BiometricKeyStoreException("Biometric authentication failed")
}

/** Used on platforms that do not expose an app-supported biometric prompt. */
class UnavailableBiometricKeyStore(
    private val type: BiometricType = BiometricType.GENERIC,
) : BiometricKeyStore {
    override suspend fun getCapability(): BiometricCapability = BiometricCapability(
        type = type,
        availability = BiometricAvailability.UNAVAILABLE,
    )

    override suspend fun contains(vaultId: String): Boolean = false

    override suspend fun enroll(vaultId: String, vaultKey: ByteArray): Result<Unit> =
        Result.failure(BiometricKeyStoreException.NotAvailable())

    override suspend fun retrieve(vaultId: String): Result<ByteArray> =
        Result.failure(BiometricKeyStoreException.NotAvailable())

    override suspend fun delete(vaultId: String): Result<Unit> = Result.success(Unit)
}
