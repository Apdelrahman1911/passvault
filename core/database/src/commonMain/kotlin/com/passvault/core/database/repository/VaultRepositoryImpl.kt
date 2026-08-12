package com.passvault.core.database.repository

import com.passvault.core.crypto.CryptoEnvelope
import com.passvault.core.crypto.CryptoEngine
import com.passvault.core.crypto.DerivedKey
import com.passvault.core.crypto.EncryptedData
import com.passvault.core.crypto.Argon2Parameters
import com.passvault.core.crypto.VaultKeyHierarchy
import com.passvault.core.crypto.WrappedKey
import com.passvault.core.database.dao.VaultMetadataDao
import com.passvault.core.database.entity.VaultMetadataEntity
import com.passvault.core.domain.model.MasterPasswordPolicy
import com.passvault.core.domain.model.SessionId
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.model.VaultId
import com.passvault.core.domain.model.VaultMetadata
import com.passvault.core.domain.model.VaultSessionState
import com.passvault.core.domain.repository.LockReason
import com.passvault.core.domain.repository.VaultRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Instant
import kotlin.time.Clock

/**
 * Owns vault metadata and the in-memory VEK session.
 *
 * The repository is deliberately the only runtime owner of the VEK.  All
 * transitions are serialized so a lock cannot race an unlock or a repository
 * read obtaining a copied key.
 */
@Suppress("TooManyFunctions") // This is the sole owner of the in-memory VEK and its serialized state machine.
class VaultRepositoryImpl(
    private val vaultMetadataDao: VaultMetadataDao,
    private val cryptoEngine: CryptoEngine,
    private val keyHierarchy: VaultKeyHierarchy,
) : VaultRepository, VaultSessionManager {

    private val sessionMutex = Mutex()
    private val _sessionState = MutableStateFlow<VaultSessionState>(VaultSessionState.Uninitialized)

    private var currentVek: ByteArray? = null
    private var failedAttempts = 0

    companion object {
        private const val MIN_VAULT_FORMAT_VERSION = 1
        private const val MAX_VAULT_FORMAT_VERSION = 2
        private const val INITIAL_VAULT_FORMAT_VERSION = 1
        private const val CRYPTO_FORMAT_VERSION = 2
        private const val ARGON2_ALGORITHM_ID = "Argon2id"
        private const val VERIFICATION_AAD = "verification"
        private const val ARGON2_SALT_BYTES = 16
        private const val VERIFICATION_BYTES = 32
        private const val VEK_BYTES = 32
        private const val NONCE_BYTES = 24
        private const val AEAD_MAGIC_BYTES = 4
        private const val AEAD_TAG_BYTES = 16
        private const val FIXED_SECRET_ENVELOPE_BYTES = AEAD_MAGIC_BYTES + VEK_BYTES + AEAD_TAG_BYTES
        private const val MAX_FIXED_SECRET_ENVELOPE_BYTES = FIXED_SECRET_ENVELOPE_BYTES + AEAD_TAG_BYTES
        private const val MIN_ARGON2_OPS = 2
        private const val MAX_ARGON2_OPS = 10
        private const val MIN_ARGON2_MEM = 32 * 1024 * 1024
        private const val MAX_ARGON2_MEM = 256 * 1024 * 1024
        // The current libsodium binding does not expose an Argon2 parallelism
        // argument. Persisting any value other than one would claim to use a
        // KDF parameter that is silently ignored during unlock.
        private const val SUPPORTED_ARGON2_PARALLELISM = 1
        private const val MAX_FAILED_ATTEMPTS = 100
    }

    override fun getSessionState(): Flow<VaultSessionState> = _sessionState.asStateFlow()

    override suspend fun exists(): Result<Boolean> = operationResult {
        vaultMetadataDao.exists()
    }

    override suspend fun create(masterPassword: SensitiveText): Result<VaultId> =
        sessionMutex.withLock {
            if (!MasterPasswordPolicy.accepts(masterPassword)) {
                return@withLock Result.failure(
                    IllegalArgumentException("Master password length is invalid"),
                )
            }
            if (vaultMetadataDao.exists()) {
                return@withLock Result.failure(IllegalStateException("Vault already exists"))
            }

            var metadata: VaultMetadataEntity? = null
            try {
                metadata = prepareInitialMetadata(masterPassword)
                vaultMetadataDao.insert(metadata)
                _sessionState.value = VaultSessionState.Locked()
                Result.success(VaultId(metadata.vaultId))
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Exception) {
                Result.failure(IllegalStateException("Unable to create vault"))
            } finally {
                metadata?.clearCryptoMaterial()
            }
        }

    private suspend fun prepareInitialMetadata(masterPassword: SensitiveText): VaultMetadataEntity {
        var vek: ByteArray? = null
        var salt: ByteArray? = null
        var passwordBytes: ByteArray? = null
        var derivedKey: DerivedKey? = null
        var verificationData: ByteArray? = null
        var wrappedVek: WrappedKey? = null
        var encryptedVerification: EncryptedData? = null
        return try {
            vek = keyHierarchy.generateVEK().getOrThrow()
            val params = cryptoEngine.benchmarkArgon2().safeForVault()
            salt = cryptoEngine.generateRandom(ARGON2_SALT_BYTES).getOrThrow()
            passwordBytes = masterPassword.toUtf8ByteArray()
            derivedKey = cryptoEngine.deriveKey(
                password = passwordBytes,
                salt = salt,
                opsLimit = params.opsLimit,
                memLimit = params.memLimit,
            ).getOrThrow()
            wrappedVek = keyHierarchy.wrapVEK(vek, derivedKey.key).getOrThrow()
            verificationData = cryptoEngine.generateRandom(VERIFICATION_BYTES).getOrThrow()
            encryptedVerification = cryptoEngine.encrypt(
                plaintext = verificationData,
                key = vek,
                associatedData = VERIFICATION_AAD.encodeToByteArray(),
            ).getOrThrow()
            initialMetadata(params, salt, wrappedVek, encryptedVerification)
        } finally {
            verificationData?.let { cryptoEngine.secureWipe(it) }
            passwordBytes?.let { cryptoEngine.secureWipe(it) }
            derivedKey?.clear()
            salt?.let { cryptoEngine.secureWipe(it) }
            wrappedVek?.clear()
            encryptedVerification?.clear()
            vek?.let { cryptoEngine.secureWipe(it) }
        }
    }

    private fun initialMetadata(
        params: Argon2Parameters,
        salt: ByteArray,
        wrappedVek: WrappedKey,
        encryptedVerification: EncryptedData,
    ): VaultMetadataEntity = VaultMetadataEntity(
        id = 1,
        vaultFormatVersion = INITIAL_VAULT_FORMAT_VERSION,
        cryptoFormatVersion = CRYPTO_FORMAT_VERSION,
        vaultId = "vault-${kotlin.uuid.Uuid.random()}",
        argon2AlgorithmId = ARGON2_ALGORITHM_ID,
        argon2Salt = salt.copyOf(),
        argon2OpsLimit = params.opsLimit,
        argon2MemLimit = params.memLimit,
        argon2Parallelism = SUPPORTED_ARGON2_PARALLELISM,
        wrappedVek = wrappedVek.ciphertext.copyOf(),
        vekNonce = wrappedVek.nonce.copyOf(),
        encryptedVerificationRecord = CryptoEnvelope.encode(encryptedVerification),
        verificationNonce = encryptedVerification.nonce.copyOf(),
        createdAt = Clock.System.now().toEpochMilliseconds(),
        lastAccessedAt = null,
        entryCount = 0,
    )

    override suspend fun unlock(masterPassword: SensitiveText): Result<SessionId> =
        sessionMutex.withLock {
            if (!MasterPasswordPolicy.acceptsExisting(masterPassword)) {
                return@withLock Result.failure(
                    IllegalArgumentException("Master password length is invalid"),
                )
            }
            if (_sessionState.value is VaultSessionState.Unlocked && currentVek != null) {
                return@withLock Result.failure(IllegalStateException("Vault already unlocked"))
            }

            currentVek?.let { cryptoEngine.secureWipe(it) }
            currentVek = null

            if (failedAttempts >= 3) {
                kotlinx.coroutines.delay(((failedAttempts - 2) * 500L).coerceAtMost(5_000L))
            }
            _sessionState.value = VaultSessionState.Unlocking

            var candidateVek: ByteArray? = null
            try {
                val metadata = vaultMetadataDao.get()
                    ?: throw IllegalStateException("Vault does not exist")
                validateMetadataForUnlock(metadata)
                candidateVek = unwrapVaultKey(metadata, masterPassword)
                verifyVaultKey(metadata, candidateVek)
                val sessionId = openSession(candidateVek)
                failedAttempts = 0
                Result.success(sessionId)
            } catch (cancel: CancellationException) {
                _sessionState.value = VaultSessionState.Locked()
                throw cancel
            } catch (_: Exception) {
                failedAttempts = (failedAttempts + 1).coerceAtMost(MAX_FAILED_ATTEMPTS)
                _sessionState.value = VaultSessionState.Locked()
                Result.failure(IllegalStateException("Unable to unlock vault"))
            } finally {
                candidateVek?.let { cryptoEngine.secureWipe(it) }
            }
        }

    /**
     * Opens a session with a key released by an OS biometric policy. The key
     * is still authenticated against the vault verification record before it
     * can become the active session key.
     */
    suspend fun unlockWithBiometricKey(vaultKey: ByteArray): Result<SessionId> =
        sessionMutex.withLock {
            if (vaultKey.size != VEK_BYTES) {
                return@withLock Result.failure(BiometricVaultKeyRejectedException())
            }
            val activeSession = _sessionState.value as? VaultSessionState.Unlocked
            if (activeSession != null && currentVek != null) {
                // Treat an already-open session as success. This avoids a race
                // where a concurrent password unlock succeeds and the caller
                // mistakes the still-valid biometric key for an invalid one.
                return@withLock Result.success(activeSession.sessionId)
            }

            currentVek?.let { cryptoEngine.secureWipe(it) }
            currentVek = null
            _sessionState.value = VaultSessionState.Unlocking
            var candidateVek: ByteArray? = null
            try {
                val metadata = vaultMetadataDao.get()
                    ?: throw IllegalStateException("Vault does not exist")
                validateMetadataForUnlock(metadata)
                candidateVek = vaultKey.copyOf()
                verifyBiometricVaultKey(metadata, candidateVek)
                val sessionId = openSession(candidateVek)
                failedAttempts = 0
                Result.success(sessionId)
            } catch (cancel: CancellationException) {
                _sessionState.value = VaultSessionState.Locked()
                throw cancel
            } catch (rejected: BiometricVaultKeyRejectedException) {
                _sessionState.value = VaultSessionState.Locked()
                Result.failure(rejected)
            } catch (_: Exception) {
                _sessionState.value = VaultSessionState.Locked()
                Result.failure(IllegalStateException("Unable to unlock vault"))
            } finally {
                candidateVek?.let { cryptoEngine.secureWipe(it) }
            }
        }

    override suspend fun lock(reason: LockReason): Result<Unit> =
        sessionMutex.withLock {
            try {
                clearSessionLocked(reason)
                Result.success(Unit)
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Exception) {
                Result.failure(IllegalStateException("Unable to lock vault"))
            }
        }

    override suspend fun changeMasterPassword(
        currentPassword: SensitiveText,
        newPassword: SensitiveText,
    ): Result<Unit> = sessionMutex.withLock {
        if (!MasterPasswordPolicy.accepts(newPassword)) {
            return@withLock Result.failure(
                IllegalArgumentException("New master password length is invalid"),
            )
        }
        if (!MasterPasswordPolicy.acceptsExisting(currentPassword)) {
            return@withLock Result.failure(
                IllegalArgumentException("Current master password length is invalid"),
            )
        }
        val activeVek = currentVek?.copyOf()
            ?: return@withLock Result.failure(IllegalStateException("Vault must be unlocked"))
        if (_sessionState.value !is VaultSessionState.Unlocked) {
            cryptoEngine.secureWipe(activeVek)
            return@withLock Result.failure(IllegalStateException("Vault must be unlocked"))
        }

        try {
            replacePasswordWrapping(activeVek, currentPassword, newPassword)
            Result.success(Unit)
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (_: Exception) {
            Result.failure(IllegalStateException("Unable to change master password"))
        } finally {
            cryptoEngine.secureWipe(activeVek)
        }
    }

    private suspend fun replacePasswordWrapping(
        activeVek: ByteArray,
        currentPassword: SensitiveText,
        newPassword: SensitiveText,
    ) {
        var newPasswordBytes: ByteArray? = null
        var candidateVek: ByteArray? = null
        var newDerived: DerivedKey? = null
        var verificationData: ByteArray? = null
        var newSalt: ByteArray? = null
        var wrappedVek: WrappedKey? = null
        var encryptedVerification: EncryptedData? = null
        try {
            val metadata = vaultMetadataDao.get()
                ?: throw IllegalStateException("Vault metadata not found")
            validateMetadataForUnlock(metadata)
            candidateVek = unwrapVaultKey(metadata, currentPassword)
            if (!cryptoEngine.constantTimeEquals(candidateVek, activeVek)) {
                throw IllegalArgumentException("Current password incorrect")
            }

            val params = cryptoEngine.benchmarkArgon2().safeForVault()
            newSalt = cryptoEngine.generateRandom(ARGON2_SALT_BYTES).getOrThrow()
            newPasswordBytes = newPassword.toUtf8ByteArray()
            newDerived = cryptoEngine.deriveKey(
                newPasswordBytes,
                newSalt,
                params.opsLimit,
                params.memLimit,
            ).getOrThrow()
            val wrapped = keyHierarchy.wrapVEK(activeVek, newDerived.key).getOrThrow()
            wrappedVek = wrapped
            verificationData = cryptoEngine.generateRandom(VERIFICATION_BYTES).getOrThrow()
            val verification = cryptoEngine.encrypt(
                verificationData,
                activeVek,
                VERIFICATION_AAD.encodeToByteArray(),
            ).getOrThrow()
            encryptedVerification = verification

            // One SQL update is the commit point.  Existing metadata remains
            // untouched if any cryptographic preparation above fails.
            vaultMetadataDao.updateEncryptionParameters(
                salt = newSalt,
                opsLimit = params.opsLimit,
                memLimit = params.memLimit,
                parallelism = SUPPORTED_ARGON2_PARALLELISM,
                wrappedVek = wrapped.ciphertext,
                vekNonce = wrapped.nonce,
                verificationRecord = CryptoEnvelope.encode(verification),
                verificationNonce = verification.nonce,
                lastAccessedAt = Clock.System.now().toEpochMilliseconds(),
            )
        } finally {
            newPasswordBytes?.let { cryptoEngine.secureWipe(it) }
            candidateVek?.let { cryptoEngine.secureWipe(it) }
            newDerived?.clear()
            verificationData?.let { cryptoEngine.secureWipe(it) }
            newSalt?.let { cryptoEngine.secureWipe(it) }
            wrappedVek?.clear()
            encryptedVerification?.clear()
        }
    }

    override suspend fun getMetadata(): Result<VaultMetadata> = operationResult {
        val entity = vaultMetadataDao.get()
            ?: throw IllegalStateException("Vault does not exist")
        validateMetadataForUnlock(entity)
        VaultMetadata(
            id = VaultId(entity.vaultId),
            formatVersion = entity.vaultFormatVersion,
            createdAt = Instant.fromEpochMilliseconds(entity.createdAt),
            lastAccessedAt = entity.lastAccessedAt?.let(Instant::fromEpochMilliseconds),
            entryCount = entity.entryCount,
        )
    }

    override suspend fun <T> withUnlockedSession(block: suspend (ByteArray) -> T): T =
        sessionMutex.withLock {
            if (_sessionState.value !is VaultSessionState.Unlocked) {
                throw VaultSessionLockedException()
            }
            val leasedVek = currentVek?.copyOf()
                ?: throw VaultSessionLockedException()
            try {
                block(leasedVek)
            } finally {
                cryptoEngine.secureWipe(leasedVek)
            }
        }

    override suspend fun <T> lockAndRun(
        reason: LockReason,
        block: suspend () -> T,
    ): T = sessionMutex.withLock {
        if (_sessionState.value !is VaultSessionState.Unlocked || currentVek == null) {
            throw VaultSessionLockedException()
        }
        clearSessionLocked(reason)
        block()
    }

    internal fun isUnlocked(): Boolean = _sessionState.value is VaultSessionState.Unlocked && currentVek != null

    private suspend fun clearSessionLocked(reason: LockReason) {
        if (currentVek == null && _sessionState.value is VaultSessionState.Locked) return
        _sessionState.value = VaultSessionState.Locking(reason)
        val keyToWipe = currentVek
        currentVek = null
        try {
            keyToWipe?.let { cryptoEngine.secureWipe(it) }
        } finally {
            _sessionState.value = VaultSessionState.Locked(reason)
        }
    }

    private suspend inline fun <T> operationResult(crossinline block: suspend () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (_: Exception) {
            Result.failure(IllegalStateException("Vault operation failed"))
        }

    private suspend fun unwrapVaultKey(
        metadata: VaultMetadataEntity,
        password: SensitiveText,
    ): ByteArray {
        var passwordBytes: ByteArray? = null
        var derivedKey: DerivedKey? = null
        return try {
            passwordBytes = password.toUtf8ByteArray()
            derivedKey = cryptoEngine.deriveKey(
                password = passwordBytes,
                salt = metadata.argon2Salt,
                opsLimit = metadata.argon2OpsLimit,
                memLimit = metadata.argon2MemLimit,
            ).getOrThrow()
            keyHierarchy.unwrapVEK(
                WrappedKey(
                    ciphertext = CryptoEnvelope.normalize(metadata.wrappedVek),
                    nonce = metadata.vekNonce,
                ),
                derivedKey.key,
            ).getOrThrow()
        } finally {
            passwordBytes?.let { cryptoEngine.secureWipe(it) }
            derivedKey?.clear()
        }
    }

    private suspend fun verifyVaultKey(metadata: VaultMetadataEntity, vaultKey: ByteArray) {
        var verificationPlaintext: ByteArray? = null
        try {
            verificationPlaintext = cryptoEngine.decrypt(
                ciphertext = CryptoEnvelope.normalize(metadata.encryptedVerificationRecord),
                nonce = metadata.verificationNonce,
                key = vaultKey,
                associatedData = VERIFICATION_AAD.encodeToByteArray(),
            ).getOrThrow()
            require(verificationPlaintext.size == VERIFICATION_BYTES)
        } finally {
            verificationPlaintext?.let { cryptoEngine.secureWipe(it) }
        }
    }

    // Authentication failures are the only failures that invalidate an enrolled key.
    @Suppress("TooGenericExceptionCaught")
    private suspend fun verifyBiometricVaultKey(metadata: VaultMetadataEntity, vaultKey: ByteArray) {
        try {
            verifyVaultKey(metadata, vaultKey)
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (_: Exception) {
            throw BiometricVaultKeyRejectedException()
        }
    }

    private suspend fun openSession(vaultKey: ByteArray): SessionId {
        require(vaultKey.size == VEK_BYTES)
        vaultMetadataDao.updateLastAccessed(Clock.System.now().toEpochMilliseconds())
        val sessionId = SessionId("session-${kotlin.uuid.Uuid.random()}")
        currentVek = vaultKey.copyOf()
        _sessionState.value = VaultSessionState.Unlocked(sessionId)
        return sessionId
    }

    private fun validateMetadataForUnlock(metadata: VaultMetadataEntity) {
        require(metadata.id == 1)
        metadata.vaultId.requireRecordIdentifier("Vault ID")
        require(metadata.vaultFormatVersion in MIN_VAULT_FORMAT_VERSION..MAX_VAULT_FORMAT_VERSION)
        require(metadata.cryptoFormatVersion == CRYPTO_FORMAT_VERSION)
        require(metadata.argon2AlgorithmId == ARGON2_ALGORITHM_ID)
        require(metadata.argon2Salt.size == ARGON2_SALT_BYTES)
        require(metadata.argon2OpsLimit in MIN_ARGON2_OPS..MAX_ARGON2_OPS)
        require(metadata.argon2MemLimit in MIN_ARGON2_MEM..MAX_ARGON2_MEM)
        require(metadata.argon2Parallelism == SUPPORTED_ARGON2_PARALLELISM)
        requireFixedSecretPayload(metadata.wrappedVek)
        require(metadata.vekNonce.size == NONCE_BYTES)
        requireFixedSecretPayload(metadata.encryptedVerificationRecord)
        require(metadata.verificationNonce.size == NONCE_BYTES)
        require(metadata.entryCount >= 0)
    }

    private fun requireFixedSecretPayload(payload: ByteArray) {
        require(payload.size in FIXED_SECRET_ENVELOPE_BYTES..MAX_FIXED_SECRET_ENVELOPE_BYTES)
        require(CryptoEnvelope.isSupportedPayload(payload))
        require(CryptoEnvelope.normalize(payload).size == FIXED_SECRET_ENVELOPE_BYTES)
    }

    private fun Argon2Parameters.safeForVault(): Argon2Parameters =
        copy(
            opsLimit = opsLimit.coerceIn(MIN_ARGON2_OPS, MAX_ARGON2_OPS),
            memLimit = memLimit.coerceIn(MIN_ARGON2_MEM, MAX_ARGON2_MEM),
        )

    private fun VaultMetadataEntity.clearCryptoMaterial() {
        argon2Salt.fill(0)
        wrappedVek.fill(0)
        vekNonce.fill(0)
        encryptedVerificationRecord.fill(0)
        verificationNonce.fill(0)
    }
}

/**
 * Minimal session boundary used by encrypted repositories. The session owns
 * and wipes the leased key copy after [withUnlockedSession] completes.
 */
interface VaultSessionManager {
    suspend fun <T> withUnlockedSession(block: suspend (ByteArray) -> T): T
    suspend fun <T> lockAndRun(reason: LockReason, block: suspend () -> T): T
}

internal class VaultSessionLockedException : IllegalStateException("Vault not unlocked")

internal class BiometricVaultKeyRejectedException : IllegalStateException("Biometric vault key was rejected")
