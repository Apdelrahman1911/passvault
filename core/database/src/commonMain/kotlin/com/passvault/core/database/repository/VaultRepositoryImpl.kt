package com.passvault.core.database.repository

import com.passvault.core.crypto.CryptoEnvelope
import com.passvault.core.crypto.CryptoEngine
import com.passvault.core.crypto.DerivedKey
import com.passvault.core.crypto.VaultKeyHierarchy
import com.passvault.core.crypto.WrappedKey
import com.passvault.core.database.dao.VaultMetadataDao
import com.passvault.core.database.entity.VaultMetadataEntity
import com.passvault.core.domain.model.SessionId
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.model.VaultId
import com.passvault.core.domain.model.VaultMetadata
import com.passvault.core.domain.model.VaultSessionState
import com.passvault.core.domain.repository.LockReason
import com.passvault.core.domain.repository.VaultRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
class VaultRepositoryImpl(
    private val vaultMetadataDao: VaultMetadataDao,
    private val cryptoEngine: CryptoEngine,
    private val keyHierarchy: VaultKeyHierarchy,
    private val applicationScope: CoroutineScope,
) : VaultRepository, VaultSessionManager {

    private val sessionMutex = Mutex()
    private val _sessionState = MutableStateFlow<VaultSessionState>(VaultSessionState.Uninitialized)

    private var currentVek: ByteArray? = null
    private var currentSessionId: String? = null
    private var failedAttempts = 0

    companion object {
        private const val VAULT_FORMAT_VERSION = 1
        private const val CRYPTO_FORMAT_VERSION = 2
        private const val ARGON2_ALGORITHM_ID = "Argon2id"
        private const val VERIFICATION_AAD = "verification"
        private const val ARGON2_SALT_BYTES = 16
        private const val VERIFICATION_BYTES = 32
        private const val VEK_BYTES = 32
        private const val NONCE_BYTES = 24
        private const val MIN_ARGON2_OPS = 2
        private const val MAX_ARGON2_OPS = 10
        private const val MIN_ARGON2_MEM = 32 * 1024 * 1024
        private const val MAX_ARGON2_MEM = 256 * 1024 * 1024
        private const val MAX_ARGON2_PARALLELISM = 8
        private const val MAX_PAYLOAD_BYTES = 64 * 1024 * 1024
    }

    override fun getSessionState(): Flow<VaultSessionState> = _sessionState.asStateFlow()

    override suspend fun exists(): Result<Boolean> = operationResult {
        vaultMetadataDao.exists()
    }

    override suspend fun create(masterPassword: SensitiveText): Result<VaultId> =
        sessionMutex.withLock {
            if (vaultMetadataDao.exists()) {
                return@withLock Result.failure(IllegalStateException("Vault already exists"))
            }

            var vek: ByteArray? = null
            var salt: ByteArray? = null
            var passwordBytes: ByteArray? = null
            var derivedKey: DerivedKey? = null
            var verificationData: ByteArray? = null
            try {
                vek = keyHierarchy.generateVEK().getOrThrow()
                val params = cryptoEngine.benchmarkArgon2()
                salt = cryptoEngine.generateRandom(ARGON2_SALT_BYTES).getOrThrow()
                passwordBytes = masterPassword.toStringUnsafe().encodeToByteArray()
                derivedKey = cryptoEngine.deriveKey(
                    password = passwordBytes,
                    salt = salt,
                    opsLimit = params.opsLimit,
                    memLimit = params.memLimit,
                ).getOrThrow()

                val wrappedVek = keyHierarchy.wrapVEK(vek, derivedKey.key).getOrThrow()
                verificationData = cryptoEngine.generateRandom(VERIFICATION_BYTES).getOrThrow()
                val encryptedVerification = cryptoEngine.encrypt(
                    plaintext = verificationData,
                    key = vek,
                    associatedData = VERIFICATION_AAD.encodeToByteArray(),
                ).getOrThrow()

                val vaultId = "vault-${kotlin.uuid.Uuid.random()}"
                val now = Clock.System.now()
                vaultMetadataDao.insert(
                    VaultMetadataEntity(
                        id = 1,
                        vaultFormatVersion = VAULT_FORMAT_VERSION,
                        cryptoFormatVersion = CRYPTO_FORMAT_VERSION,
                        vaultId = vaultId,
                        argon2AlgorithmId = ARGON2_ALGORITHM_ID,
                        argon2Salt = salt.copyOf(),
                        argon2OpsLimit = params.opsLimit,
                        argon2MemLimit = params.memLimit,
                        argon2Parallelism = params.parallelism,
                        wrappedVek = wrappedVek.ciphertext.copyOf(),
                        vekNonce = wrappedVek.nonce.copyOf(),
                        encryptedVerificationRecord = CryptoEnvelope.encode(encryptedVerification),
                        verificationNonce = encryptedVerification.nonce.copyOf(),
                        createdAt = now.toEpochMilliseconds(),
                        lastAccessedAt = null,
                        entryCount = 0,
                    ),
                )
                _sessionState.value = VaultSessionState.Locked
                Result.success(VaultId(vaultId))
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Exception) {
                Result.failure(IllegalStateException("Unable to create vault"))
            } finally {
                verificationData?.let { cryptoEngine.secureWipe(it) }
                passwordBytes?.let { cryptoEngine.secureWipe(it) }
                derivedKey?.clear()
                salt?.let { cryptoEngine.secureWipe(it) }
                vek?.let { cryptoEngine.secureWipe(it) }
            }
        }

    override suspend fun unlock(masterPassword: SensitiveText): Result<SessionId> =
        sessionMutex.withLock {
            if (_sessionState.value is VaultSessionState.Unlocked && currentVek != null) {
                return@withLock Result.failure(IllegalStateException("Vault already unlocked"))
            }

            if (failedAttempts >= 3) {
                kotlinx.coroutines.delay(((failedAttempts - 2) * 500L).coerceAtMost(5_000L))
            }
            _sessionState.value = VaultSessionState.Unlocking

            var passwordBytes: ByteArray? = null
            var derivedKey: DerivedKey? = null
            var candidateVek: ByteArray? = null
            var verificationPlaintext: ByteArray? = null
            try {
                val metadata = vaultMetadataDao.get()
                    ?: throw IllegalStateException("Vault does not exist")
                validateMetadataForUnlock(metadata)
                passwordBytes = masterPassword.toStringUnsafe().encodeToByteArray()
                derivedKey = cryptoEngine.deriveKey(
                    password = passwordBytes,
                    salt = metadata.argon2Salt,
                    opsLimit = metadata.argon2OpsLimit,
                    memLimit = metadata.argon2MemLimit,
                ).getOrThrow()
                candidateVek = keyHierarchy.unwrapVEK(
                    WrappedKey(
                        ciphertext = metadata.wrappedVek,
                        nonce = metadata.vekNonce,
                    ),
                    derivedKey.key,
                ).getOrThrow()
                verificationPlaintext = cryptoEngine.decrypt(
                    ciphertext = CryptoEnvelope.normalize(metadata.encryptedVerificationRecord),
                    nonce = metadata.verificationNonce,
                    key = candidateVek,
                    associatedData = VERIFICATION_AAD.encodeToByteArray(),
                ).getOrThrow()

                currentVek = candidateVek
                candidateVek = null
                currentSessionId = "session-${kotlin.uuid.Uuid.random()}"
                failedAttempts = 0
                vaultMetadataDao.updateLastAccessed(Clock.System.now().toEpochMilliseconds())
                val sessionId = SessionId(requireNotNull(currentSessionId))
                _sessionState.value = VaultSessionState.Unlocked(sessionId)
                Result.success(sessionId)
            } catch (cancel: CancellationException) {
                _sessionState.value = VaultSessionState.Locked
                throw cancel
            } catch (_: Exception) {
                failedAttempts++
                _sessionState.value = VaultSessionState.Locked
                Result.failure(IllegalStateException("Unable to unlock vault"))
            } finally {
                passwordBytes?.let { cryptoEngine.secureWipe(it) }
                derivedKey?.clear()
                candidateVek?.let { cryptoEngine.secureWipe(it) }
                verificationPlaintext?.let { cryptoEngine.secureWipe(it) }
            }
        }

    override suspend fun lock(): Result<Unit> =
        sessionMutex.withLock {
            try {
                clearSessionLocked(LockReason.Manual)
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
        val activeVek = currentVek?.copyOf()
            ?: return@withLock Result.failure(IllegalStateException("Vault must be unlocked"))
        if (_sessionState.value !is VaultSessionState.Unlocked) {
            cryptoEngine.secureWipe(activeVek)
            return@withLock Result.failure(IllegalStateException("Vault must be unlocked"))
        }

        var currentPasswordBytes: ByteArray? = null
        var newPasswordBytes: ByteArray? = null
        var currentDerived: DerivedKey? = null
        var candidateVek: ByteArray? = null
        var newDerived: DerivedKey? = null
        var verificationData: ByteArray? = null
        var newSalt: ByteArray? = null
        try {
            val metadata = vaultMetadataDao.get()
                ?: throw IllegalStateException("Vault metadata not found")
            validateMetadataForUnlock(metadata)
            currentPasswordBytes = currentPassword.toStringUnsafe().encodeToByteArray()
            currentDerived = cryptoEngine.deriveKey(
                currentPasswordBytes,
                metadata.argon2Salt,
                metadata.argon2OpsLimit,
                metadata.argon2MemLimit,
            ).getOrThrow()
            candidateVek = keyHierarchy.unwrapVEK(
                WrappedKey(metadata.wrappedVek, metadata.vekNonce),
                currentDerived.key,
            ).getOrThrow()
            if (!cryptoEngine.constantTimeEquals(candidateVek, activeVek)) {
                throw IllegalArgumentException("Current password incorrect")
            }

            val params = cryptoEngine.benchmarkArgon2()
            newSalt = cryptoEngine.generateRandom(ARGON2_SALT_BYTES).getOrThrow()
            newPasswordBytes = newPassword.toStringUnsafe().encodeToByteArray()
            newDerived = cryptoEngine.deriveKey(
                newPasswordBytes,
                newSalt,
                params.opsLimit,
                params.memLimit,
            ).getOrThrow()
            val wrapped = keyHierarchy.wrapVEK(activeVek, newDerived.key).getOrThrow()
            verificationData = cryptoEngine.generateRandom(VERIFICATION_BYTES).getOrThrow()
            val encryptedVerification = cryptoEngine.encrypt(
                verificationData,
                activeVek,
                VERIFICATION_AAD.encodeToByteArray(),
            ).getOrThrow()

            // One SQL update is the commit point.  Existing metadata remains
            // untouched if any cryptographic preparation above fails.
            vaultMetadataDao.updateEncryptionParameters(
                salt = newSalt,
                opsLimit = params.opsLimit,
                memLimit = params.memLimit,
                parallelism = params.parallelism,
                wrappedVek = wrapped.ciphertext,
                vekNonce = wrapped.nonce,
                verificationRecord = CryptoEnvelope.encode(encryptedVerification),
                verificationNonce = encryptedVerification.nonce,
            )
            vaultMetadataDao.updateLastAccessed(Clock.System.now().toEpochMilliseconds())
            Result.success(Unit)
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (_: Exception) {
            Result.failure(IllegalStateException("Unable to change master password"))
        } finally {
            currentPasswordBytes?.let { cryptoEngine.secureWipe(it) }
            newPasswordBytes?.let { cryptoEngine.secureWipe(it) }
            currentDerived?.clear()
            candidateVek?.let { cryptoEngine.secureWipe(it) }
            newDerived?.clear()
            verificationData?.let { cryptoEngine.secureWipe(it) }
            newSalt?.let { cryptoEngine.secureWipe(it) }
            cryptoEngine.secureWipe(activeVek)
        }
    }

    override suspend fun getMetadata(): Result<VaultMetadata> = operationResult {
        val entity = vaultMetadataDao.get()
            ?: throw IllegalStateException("Vault does not exist")
        VaultMetadata(
            id = VaultId(entity.vaultId),
            formatVersion = entity.vaultFormatVersion,
            createdAt = Instant.fromEpochMilliseconds(entity.createdAt),
            lastAccessedAt = entity.lastAccessedAt?.let(Instant::fromEpochMilliseconds),
            entryCount = entity.entryCount,
        )
    }

    override suspend fun getCurrentVek(): ByteArray? =
        sessionMutex.withLock { currentVek?.copyOf() }

    override suspend fun setSessionVek(vek: ByteArray) {
        require(vek.size == VEK_BYTES) { "Invalid vault key size" }
        sessionMutex.withLock {
            currentVek?.let { cryptoEngine.secureWipe(it) }
            currentVek = vek.copyOf()
            currentSessionId = "session-${kotlin.uuid.Uuid.random()}"
            _sessionState.value = VaultSessionState.Unlocked(SessionId(requireNotNull(currentSessionId)))
        }
    }

    override suspend fun clearSession() {
        sessionMutex.withLock { clearSessionLocked(LockReason.Manual) }
    }

    fun autoLock() = requestLock(LockReason.AutoLock)

    fun lockOnBackground() = requestLock(LockReason.Background)

    fun lockOnFocusLost() = requestLock(LockReason.DesktopFocusLost)

    fun lockOnSystemSuspend() = requestLock(LockReason.SystemSuspend)

    fun isUnlocked(): Boolean = _sessionState.value is VaultSessionState.Unlocked && currentVek != null

    fun getSessionId(): String? = currentSessionId

    private fun requestLock(reason: LockReason) {
        applicationScope.launch {
            sessionMutex.withLock { clearSessionLocked(reason) }
        }
    }

    private suspend fun clearSessionLocked(reason: LockReason) {
        if (currentVek == null && _sessionState.value == VaultSessionState.Locked) return
        _sessionState.value = VaultSessionState.Locking(reason)
        currentVek?.let { cryptoEngine.secureWipe(it) }
        currentVek = null
        currentSessionId = null
        _sessionState.value = VaultSessionState.Locked
    }

    private suspend inline fun <T> operationResult(crossinline block: suspend () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (_: Exception) {
            Result.failure(IllegalStateException("Vault operation failed"))
        }

    private fun validateMetadataForUnlock(metadata: VaultMetadataEntity) {
        require(metadata.vaultFormatVersion == VAULT_FORMAT_VERSION)
        require(metadata.cryptoFormatVersion == CRYPTO_FORMAT_VERSION)
        require(metadata.argon2AlgorithmId == ARGON2_ALGORITHM_ID)
        require(metadata.argon2Salt.size == ARGON2_SALT_BYTES)
        require(metadata.argon2OpsLimit in MIN_ARGON2_OPS..MAX_ARGON2_OPS)
        require(metadata.argon2MemLimit in MIN_ARGON2_MEM..MAX_ARGON2_MEM)
        require(metadata.argon2Parallelism in 1..MAX_ARGON2_PARALLELISM)
        require(metadata.wrappedVek.size <= MAX_PAYLOAD_BYTES)
        require(CryptoEnvelope.isSupportedPayload(metadata.wrappedVek))
        require(metadata.vekNonce.size == NONCE_BYTES)
        require(metadata.encryptedVerificationRecord.size <= MAX_PAYLOAD_BYTES)
        require(CryptoEnvelope.isSupportedPayload(metadata.encryptedVerificationRecord))
        require(metadata.verificationNonce.size == NONCE_BYTES)
    }

}

/**
 * Minimal session boundary used by encrypted repositories.  Returned keys are
 * copies and must be wiped by the caller as soon as its operation completes.
 */
interface VaultSessionManager {
    suspend fun getCurrentVek(): ByteArray?
    suspend fun setSessionVek(vek: ByteArray)
    suspend fun clearSession()
}
