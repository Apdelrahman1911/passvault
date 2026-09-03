package com.passvault.core.database.repository

import com.passvault.core.crypto.CiphertextAuthenticationException
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
import com.passvault.core.security.BiometricPromptController
import com.passvault.core.security.NoOpBiometricPromptController
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Instant
import kotlin.time.Clock

/**
 * Owns vault metadata and the in-memory VEK session.
 *
 * The repository is deliberately the only runtime owner of the VEK.  All
 * transitions are serialized so a lock cannot race an unlock. Repository
 * operations receive tracked, revocable key leases so lock can cancel an
 * operation without waiting indefinitely for arbitrary suspending work.
 */
@Suppress("TooManyFunctions") // This is the sole owner of the in-memory VEK and its serialized state machine.
class VaultRepositoryImpl(
    private val vaultMetadataDao: VaultMetadataDao,
    private val cryptoEngine: CryptoEngine,
    private val keyHierarchy: VaultKeyHierarchy,
    private val biometricPromptController: BiometricPromptController = NoOpBiometricPromptController,
) : VaultRepository, VaultSessionManager {

    private val transitionMutex = Mutex()
    private val sessionMutex = Mutex()
    private val operationMutex = Mutex()
    private val lockIntents = LockIntentTracker()
    private val _sessionState = MutableStateFlow<VaultSessionState>(VaultSessionState.Uninitialized)

    private var currentVek: ByteArray? = null
    private var failedAttempts = 0
    private var nextLeaseId = 0L
    private val activeLeases = mutableMapOf<Long, SessionLease>()

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
        private const val LEASE_CANCELLATION_TIMEOUT_MILLIS = 2_000L
    }

    private class SessionLease(
        val id: Long,
        val key: ByteArray,
        val operation: Job,
    ) {
        val released = CompletableDeferred<Unit>()
        var revoked = false
        var wipeFailure: Throwable? = null
    }

    override fun getSessionState(): Flow<VaultSessionState> = _sessionState.asStateFlow()

    override suspend fun matchesMasterPassword(candidate: SensitiveText): Boolean {
        if (!MasterPasswordPolicy.acceptsExisting(candidate)) return false
        return withUnlockedSession { activeVek ->
            val metadata = vaultMetadataDao.get()
                ?: throw IllegalStateException("Vault does not exist")
            validateMetadataForUnlock(metadata)

            var candidateVek: ByteArray? = null
            try {
                val unwrappedVek = try {
                    unwrapVaultKey(metadata, candidate)
                } catch (cancel: CancellationException) {
                    throw cancel
                } catch (_: CiphertextAuthenticationException) {
                    return@withUnlockedSession false
                }
                candidateVek = unwrappedVek
                cryptoEngine.constantTimeEquals(unwrappedVek, activeVek)
            } finally {
                candidateVek?.let(cryptoEngine::secureWipe)
            }
        }
    }

    override suspend fun exists(): Result<Boolean> = operationResult {
        vaultMetadataDao.exists()
    }

    override suspend fun create(masterPassword: SensitiveText): Result<VaultId> =
        withExclusiveSessionTransition {
            if (!MasterPasswordPolicy.accepts(masterPassword)) {
                return@withExclusiveSessionTransition Result.failure(
                    IllegalArgumentException("Master password does not meet policy"),
                )
            }
            if (vaultMetadataDao.exists()) {
                return@withExclusiveSessionTransition Result.failure(IllegalStateException("Vault already exists"))
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
        withExclusiveSessionTransition {
            if (!MasterPasswordPolicy.acceptsExisting(masterPassword)) {
                return@withExclusiveSessionTransition Result.failure(
                    IllegalArgumentException("Master password length is invalid"),
                )
            }
            if (_sessionState.value is VaultSessionState.Locking) {
                return@withExclusiveSessionTransition Result.failure(
                    IllegalStateException("Vault lock is in progress"),
                )
            }
            if (_sessionState.value is VaultSessionState.Unlocked && currentVek != null) {
                return@withExclusiveSessionTransition Result.failure(IllegalStateException("Vault already unlocked"))
            }
            val unlockGeneration = try {
                lockIntents.snapshotGeneration()
            } catch (preempted: UnlockPreemptedException) {
                _sessionState.value = VaultSessionState.Locked(preempted.reason)
                return@withExclusiveSessionTransition preemptedUnlockResult()
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
                // Persisted KDF parameters are untrusted; reject malformed metadata before allocating KDF resources.
                validateMetadataForUnlock(metadata)
                candidateVek = unwrapVaultKey(metadata, masterPassword)
                verifyVaultKey(metadata, candidateVek)
                val sessionId = openSession(candidateVek, unlockGeneration)
                failedAttempts = 0
                Result.success(sessionId)
            } catch (cancel: CancellationException) {
                _sessionState.value = VaultSessionState.Locked()
                throw cancel
            } catch (preempted: UnlockPreemptedException) {
                _sessionState.value = VaultSessionState.Locked(preempted.reason)
                preemptedUnlockResult()
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
        withExclusiveSessionTransition {
            if (vaultKey.size != VEK_BYTES) {
                return@withExclusiveSessionTransition Result.failure(BiometricVaultKeyRejectedException())
            }
            if (_sessionState.value is VaultSessionState.Locking) {
                return@withExclusiveSessionTransition Result.failure(
                    IllegalStateException("Vault lock is in progress"),
                )
            }
            val activeSession = _sessionState.value as? VaultSessionState.Unlocked
            if (activeSession != null && currentVek != null) {
                // Treat an already-open session as success. This avoids a race
                // where a concurrent password unlock succeeds and the caller
                // mistakes the still-valid biometric key for an invalid one.
                return@withExclusiveSessionTransition Result.success(activeSession.sessionId)
            }
            val unlockGeneration = try {
                lockIntents.snapshotGeneration()
            } catch (preempted: UnlockPreemptedException) {
                _sessionState.value = VaultSessionState.Locked(preempted.reason)
                return@withExclusiveSessionTransition preemptedUnlockResult()
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
                val sessionId = openSession(candidateVek, unlockGeneration)
                failedAttempts = 0
                Result.success(sessionId)
            } catch (cancel: CancellationException) {
                _sessionState.value = VaultSessionState.Locked()
                throw cancel
            } catch (preempted: UnlockPreemptedException) {
                _sessionState.value = VaultSessionState.Locked(preempted.reason)
                preemptedUnlockResult()
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

    override suspend fun lock(reason: LockReason): Result<Unit> {
        cancelBiometricPromptBeforeLock()
        // Once the transition starts, caller cancellation must not leave the
        // repository-owned key or a tracked lease live in memory.
        return withContext(NonCancellable) {
            val intent = lockIntents.register(reason)
            try {
                val snapshot = requestLock(reason, requireUnlocked = false)
                    ?: return@withContext Result.success(Unit)
                cancelLeaseOperations(snapshot)
                transitionMutex.withLock {
                    completeLock(snapshot, reason, requireLeaseSettlement = false)
                }
            } finally {
                lockIntents.release(intent)
            }
        }
    }

    override suspend fun changeMasterPassword(
        currentPassword: SensitiveText,
        newPassword: SensitiveText,
    ): Result<Unit> = withExclusiveSessionTransition {
        if (!MasterPasswordPolicy.accepts(newPassword)) {
            return@withExclusiveSessionTransition Result.failure(
                IllegalArgumentException("New master password does not meet policy"),
            )
        }
        if (!MasterPasswordPolicy.acceptsExisting(currentPassword)) {
            return@withExclusiveSessionTransition Result.failure(
                IllegalArgumentException("Current master password length is invalid"),
            )
        }
        val activeVek = currentVek?.copyOf()
            ?: return@withExclusiveSessionTransition Result.failure(IllegalStateException("Vault must be unlocked"))
        if (_sessionState.value !is VaultSessionState.Unlocked) {
            cryptoEngine.secureWipe(activeVek)
            return@withExclusiveSessionTransition Result.failure(IllegalStateException("Vault must be unlocked"))
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

    override suspend fun <T> withUnlockedSession(block: suspend (ByteArray) -> T): T = coroutineScope {
        // Run caller work in a dedicated child. Lock can revoke that child
        // without cancelling the unrelated coroutine that requested locking.
        async(start = CoroutineStart.UNDISPATCHED) {
            operationMutex.withLock {
                val operation = requireNotNull(currentCoroutineContext()[Job])
                val lease = acquireSessionLease(operation)
                try {
                    val result = block(lease.key)
                    currentCoroutineContext().ensureActive()
                    sessionMutex.withLock {
                        if (lease.revoked) throw VaultSessionLockedException()
                    }
                    result
                } finally {
                    releaseSessionLease(lease)
                }
            }
        }.await()
    }

    override suspend fun <T> lockAndRun(
        reason: LockReason,
        block: suspend () -> T,
    ): T {
        cancelBiometricPromptBeforeLock()
        var transitionAcquired = false
        var lockIntent: LockIntentTracker.Intent? = null
        try {
            withContext(NonCancellable) {
                lockIntent = lockIntents.register(reason)
                val snapshot = requestLock(reason, requireUnlocked = true)
                    ?: throw VaultSessionLockedException()
                cancelLeaseOperations(snapshot)
                transitionMutex.lock()
                transitionAcquired = true
                completeLock(snapshot, reason, requireLeaseSettlement = true).getOrThrow()
            }
            return operationMutex.withLock { block() }
        } finally {
            try {
                lockIntent?.let { intent ->
                    withContext(NonCancellable) { lockIntents.release(intent) }
                }
            } finally {
                if (transitionAcquired) transitionMutex.unlock()
            }
        }
    }

    private fun cancelBiometricPromptBeforeLock() {
        // Locking must remain fail-closed even if a platform cancellation API
        // itself fails. The session transition still proceeds and wipes the
        // repository-owned key as soon as any active lease settles.
        runCatching { biometricPromptController.cancelActive() }
    }

    internal fun isUnlocked(): Boolean = _sessionState.value is VaultSessionState.Unlocked && currentVek != null

    private suspend fun acquireSessionLease(operation: Job): SessionLease = sessionMutex.withLock {
        if (_sessionState.value !is VaultSessionState.Unlocked) {
            throw VaultSessionLockedException()
        }
        val leasedVek = currentVek?.copyOf()
            ?: throw VaultSessionLockedException()
        val lease = SessionLease(++nextLeaseId, leasedVek, operation)
        activeLeases[lease.id] = lease
        lease
    }

    private suspend fun releaseSessionLease(lease: SessionLease) {
        withContext(NonCancellable) {
            sessionMutex.withLock {
                wipeSessionLeaseLocked(lease)
            }
        }
        lease.wipeFailure?.let { throw it }
    }

    @Suppress("TooGenericExceptionCaught") // Every wipe attempt must still retire and signal the lease.
    private fun wipeSessionLeaseLocked(lease: SessionLease) {
        if (activeLeases[lease.id] !== lease) return
        try {
            cryptoEngine.secureWipe(lease.key)
        } catch (error: Throwable) {
            lease.wipeFailure = error
        } finally {
            activeLeases.remove(lease.id)
            lease.released.complete(Unit)
        }
    }

    @Suppress("TooGenericExceptionCaught") // A wipe failure must not prevent lease cancellation or terminal lock.
    private suspend fun requestLock(
        reason: LockReason,
        requireUnlocked: Boolean,
    ): LockSnapshot? = sessionMutex.withLock {
        if (requireUnlocked &&
            (_sessionState.value !is VaultSessionState.Unlocked || currentVek == null)
        ) {
            throw VaultSessionLockedException()
        }
        if (!requireUnlocked && currentVek == null && _sessionState.value is VaultSessionState.Locked) {
            _sessionState.value = VaultSessionState.Locked(reason)
            return@withLock null
        }
        _sessionState.value = VaultSessionState.Locking(reason)
        val repositoryKey = currentVek
        currentVek = null
        var wipeFailure: Throwable? = null
        try {
            repositoryKey?.let(cryptoEngine::secureWipe)
        } catch (error: Throwable) {
            wipeFailure = error
        }
        val leases = activeLeases.values.toList()
        leases.forEach { it.revoked = true }
        LockSnapshot(leases, wipeFailure)
    }

    private fun cancelLeaseOperations(snapshot: LockSnapshot) {
        val lockCancellation = CancellationException("Vault session is locking")
        snapshot.leases.map(SessionLease::operation).distinct().forEach { operation ->
            operation.cancel(lockCancellation)
        }
    }

    private suspend fun completeLock(
        snapshot: LockSnapshot,
        reason: LockReason,
        requireLeaseSettlement: Boolean,
    ): Result<Unit> {
        /*
         * No transition can start while this completes, but lease cleanup only
         * needs sessionMutex and therefore remains able to make progress.
         */
        val settled = withTimeoutOrNull(LEASE_CANCELLATION_TIMEOUT_MILLIS) {
            snapshot.leases.forEach { it.released.await() }
            true
        } == true

        sessionMutex.withLock {
            if (!settled) {
                // A caller may suppress cancellation or be stuck in blocking
                // platform code. Revoke its actual leased array at the hard
                // deadline rather than allowing lock to wait indefinitely.
                snapshot.leases.forEach(::wipeSessionLeaseLocked)
            }
            snapshot.leases.firstNotNullOfOrNull(SessionLease::wipeFailure)?.let { failure ->
                if (snapshot.wipeFailure == null) snapshot.wipeFailure = failure
            }
            _sessionState.value = VaultSessionState.Locked(reason)
        }

        return if (snapshot.wipeFailure == null && (settled || !requireLeaseSettlement)) {
            Result.success(Unit)
        } else {
            Result.failure(IllegalStateException("Unable to lock vault"))
        }
    }

    private data class LockSnapshot(
        val leases: List<SessionLease>,
        var wipeFailure: Throwable?,
    )

    private fun preemptedUnlockResult(): Result<SessionId> =
        Result.failure(IllegalStateException("Unlock was superseded by a lock request"))

    private suspend fun <T> withExclusiveSessionTransition(block: suspend () -> T): T =
        transitionMutex.withLock {
            operationMutex.withLock {
                sessionMutex.withLock { block() }
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

    private suspend fun openSession(vaultKey: ByteArray, expectedLockGeneration: Long): SessionId {
        require(vaultKey.size == VEK_BYTES)
        lockIntents.verify(expectedLockGeneration)
        vaultMetadataDao.updateLastAccessed(Clock.System.now().toEpochMilliseconds())
        return lockIntents.commit(expectedLockGeneration) {
            val sessionId = SessionId("session-${kotlin.uuid.Uuid.random()}")
            currentVek = vaultKey.copyOf()
            _sessionState.value = VaultSessionState.Unlocked(sessionId)
            sessionId
        }
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

    /**
     * Verifies a candidate against the active vault without retaining or
     * exposing the plaintext master password.
     */
    suspend fun matchesMasterPassword(candidate: SensitiveText): Boolean
}

internal class VaultSessionLockedException : IllegalStateException("Vault not unlocked")

internal class BiometricVaultKeyRejectedException : IllegalStateException("Biometric vault key was rejected")

private class LockIntentTracker {
    private val mutex = Mutex()
    private var generation = 0L
    private var nextIntentId = 0L
    private val pending = linkedMapOf<Long, LockReason>()

    data class Intent(val id: Long)

    suspend fun register(reason: LockReason): Intent = mutex.withLock {
        generation++
        Intent(id = ++nextIntentId).also { pending[it.id] = reason }
    }

    suspend fun release(intent: Intent) = mutex.withLock {
        check(pending.remove(intent.id) != null) { "Lock intent was already released" }
    }

    suspend fun snapshotGeneration(): Long = mutex.withLock {
        pending.values.lastOrNull()?.let { throw UnlockPreemptedException(it) }
        generation
    }

    suspend fun verify(expectedGeneration: Long) = mutex.withLock {
        requireCurrentGeneration(expectedGeneration)
    }

    suspend fun <T> commit(expectedGeneration: Long, block: () -> T): T = mutex.withLock {
        requireCurrentGeneration(expectedGeneration)
        block()
    }

    private fun requireCurrentGeneration(expectedGeneration: Long) {
        if (generation != expectedGeneration || pending.isNotEmpty()) {
            throw UnlockPreemptedException(pending.values.lastOrNull())
        }
    }
}

private class UnlockPreemptedException(val reason: LockReason?) : IllegalStateException("Unlock preempted")
