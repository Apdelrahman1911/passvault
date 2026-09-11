package com.passvault.core.database.repository

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.passvault.core.crypto.DesktopCryptoEngine
import com.passvault.core.crypto.VaultKeyHierarchy
import com.passvault.core.database.VaultDatabase
import com.passvault.core.database.dao.VaultMetadataDao
import com.passvault.core.database.entity.VaultMetadataEntity
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.model.VaultSessionState
import com.passvault.core.domain.repository.LockReason
import com.passvault.core.security.BiometricAvailability
import com.passvault.core.security.BiometricCapability
import com.passvault.core.security.BiometricFailureReason
import com.passvault.core.security.BiometricKeyStore
import com.passvault.core.security.BiometricKeyStoreException
import com.passvault.core.security.BiometricOperationResult
import com.passvault.core.security.BiometricType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Real in-memory Room, crypto and service/repository boundaries with a synthetic
 * platform key store. Barriers model admitted work, not OS biometric timing or UI.
 * No application, real vault, clipboard, Keychain or Keystore is accessed.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("TooManyFunctions") // One lifecycle/freshness matrix sharing the real Room and crypto fixture.
class BiometricUnlockFreshnessIntegrationTest {
    private lateinit var database: VaultDatabase
    private lateinit var cryptoEngine: DesktopCryptoEngine
    private lateinit var metadata: GatedMetadataDao
    private lateinit var repository: VaultRepositoryImpl
    private lateinit var keyStore: ControlledKeyStore
    private lateinit var service: DefaultBiometricUnlockService

    @BeforeTest
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder<VaultDatabase>()
            .setDriver(BundledSQLiteDriver())
            .build()
        cryptoEngine = DesktopCryptoEngine()
        metadata = GatedMetadataDao(database.vaultMetadataDao())
        repository = VaultRepositoryImpl(metadata, cryptoEngine, VaultKeyHierarchy(cryptoEngine))
        keyStore = ControlledKeyStore()
        service = DefaultBiometricUnlockService(repository, repository, keyStore, cryptoEngine)
    }

    @AfterTest
    fun tearDown() {
        try {
            if (::repository.isInitialized) runBlocking { repository.lock().getOrThrow() }
        } finally {
            if (::keyStore.isInitialized) keyStore.dispose()
            if (::database.isInitialized) database.close()
        }
    }

    @Test
    fun `completed lock rejects a key returned by an earlier biometric attempt`() = runTest {
        enrollAndLock()
        assertCompletedLockRejects(keyStore.pauseNextRetrieval())
    }

    @Test
    fun `admission precedes the biometric metadata lookup`() = runTest {
        enrollAndLock()
        assertCompletedLockRejects(metadata.pauseNextGet())
    }

    @Test
    fun `admission precedes the platform enrollment lookup`() = runTest {
        enrollAndLock()
        assertCompletedLockRejects(keyStore.pauseNextContains())
    }

    @Test
    fun `a genuinely fresh attempt after completed lock still opens the vault`() = runTest {
        enrollAndLock()
        assertTrue(repository.lock(LockReason.MemoryPressure).isSuccess)

        assertEquals(BiometricOperationResult.Success, service.unlock())
        assertTrue(repository.isUnlocked())
        assertTrue(repository.getSessionState().first() is VaultSessionState.Unlocked)
        assertTrue(keyStore.isEnrolled)
        assertEquals(0, keyStore.deleteCalls)
        assertReturnedKeyWiped()
    }

    @Test
    fun `a stale completion cannot succeed against or relock a newer valid session`() = runTest {
        enrollAndLock()
        val gate = keyStore.pauseNextRetrieval()
        val oldUnlock = async { service.unlock() }
        var freshKey: ByteArray? = null
        try {
            gate.entered.await()
            assertTrue(repository.lock(LockReason.MemoryPressure).isSuccess)
            unlockWithPassword()
            val freshState = repository.getSessionState().first()
            assertTrue(freshState is VaultSessionState.Unlocked)
            freshKey = repository.withUnlockedSession { it.copyOf() }
            gate.release.complete(Unit)

            assertEquals(vaultLockedResult(), oldUnlock.await())
            assertEquals(freshState, repository.getSessionState().first())
            assertTrue(repository.isUnlocked())
            repository.withUnlockedSession { assertContentEquals(requireNotNull(freshKey), it) }
            assertTrue(keyStore.isEnrolled)
            assertEquals(0, keyStore.deleteCalls)
            assertReturnedKeyWiped()
        } finally {
            gate.release.complete(Unit)
            freshKey?.fill(0)
            oldUnlock.cancelAndJoin()
        }
    }

    @Test
    fun `stale admission takes precedence over malformed candidate invalidation`() = runTest {
        enrollAndLock()
        keyStore.nextCandidate = byteArrayOf(1)
        assertCompletedLockRejects(keyStore.pauseNextRetrieval())
    }

    @Test
    fun `a fresh invalid candidate still invalidates its enrollment`() = runTest {
        enrollAndLock()
        keyStore.nextCandidate = ByteArray(32) { 0x5a }

        assertEquals(
            BiometricOperationResult.Failure(BiometricFailureReason.INVALIDATED),
            service.unlock(),
        )
        assertFalse(repository.isUnlocked())
        assertFalse(keyStore.isEnrolled)
        assertEquals(1, keyStore.deleteCalls)
        assertReturnedKeyWiped()
    }

    @Test
    fun `pending lock refuses admission without metadata or platform access`() = runTest {
        enrollAndLock()
        unlockWithPassword()
        val gate = Gate()
        val restore = async { repository.lockAndRun(LockReason.Restore) { gate.pause() } }
        try {
            gate.entered.await()
            metadata.getCalls = 0
            keyStore.containsCalls = 0
            keyStore.retrieveCalls = 0
            val unlock = async { service.unlock() }
            try {
                runCurrent()
                assertTrue(unlock.isCompleted, "A pending intent must reject before waiting on repository IO")
                assertEquals(vaultLockedResult(), unlock.await())
                assertEquals(0, metadata.getCalls)
                assertEquals(0, keyStore.containsCalls)
                assertEquals(0, keyStore.retrieveCalls)
                assertFalse(repository.isUnlocked())
            } finally {
                unlock.cancelAndJoin()
            }
        } finally {
            gate.release.complete(Unit)
            restore.await()
        }
    }

    @Test
    fun `cancellation after key transfer propagates and wipes the received array`() = runTest {
        enrollAndLock()
        keyStore.cancelOnReturn = true
        val unlock = async { service.unlock() }

        assertFailsWith<CancellationException> { unlock.await() }
        assertTrue(unlock.isCancelled)
        assertFalse(repository.isUnlocked())
        assertTrue(keyStore.isEnrolled)
        assertEquals(0, keyStore.deleteCalls)
        assertReturnedKeyWiped()
    }

    @Test
    fun `lock during the last access write still prevents biometric publication`() = runTest {
        enrollAndLock()
        val gate = metadata.pauseNextLastAccess()
        val observed = mutableListOf<VaultSessionState>()
        val collector = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.getSessionState().collect(observed::add)
        }
        val unlock = async { service.unlock() }
        try {
            gate.entered.await()
            val locking = async { repository.lock(LockReason.MemoryPressure) }
            try {
                runCurrent()
                assertFalse(locking.isCompleted)
                gate.release.complete(Unit)

                assertEquals(
                    BiometricOperationResult.Failure(BiometricFailureReason.INTERNAL_ERROR),
                    unlock.await(),
                )
                assertTrue(locking.await().isSuccess)
                assertEquals(VaultSessionState.Locked(LockReason.MemoryPressure), repository.getSessionState().first())
                assertFalse(observed.any { it is VaultSessionState.Unlocked })
                assertFalse(repository.isUnlocked())
                assertTrue(keyStore.isEnrolled)
                assertEquals(0, keyStore.deleteCalls)
                assertReturnedKeyWiped()
            } finally {
                gate.release.complete(Unit)
                locking.cancelAndJoin()
            }
        } finally {
            gate.release.complete(Unit)
            unlock.cancel()
            collector.cancel()
            try {
                unlock.join()
            } finally {
                collector.join()
            }
        }
    }

    @Test
    fun `an already open session still succeeds without an intervening lock`() = runTest {
        enrollAndLock()
        unlockWithPassword()
        val state = repository.getSessionState().first()

        assertEquals(BiometricOperationResult.Success, service.unlock())
        assertEquals(state, repository.getSessionState().first())
        assertTrue(repository.isUnlocked())
        assertTrue(keyStore.isEnrolled)
        assertEquals(0, keyStore.deleteCalls)
        assertReturnedKeyWiped()
    }

    @Test
    fun `an admission from another repository cannot authorize this repository`() = runTest {
        enrollAndLock()
        val otherRepository = VaultRepositoryImpl(metadata, cryptoEngine, VaultKeyHierarchy(cryptoEngine))
        val otherAttempt = otherRepository.beginBiometricUnlock().getOrThrow()
        val vaultId = repository.getMetadata().getOrThrow().id.value
        val candidate = keyStore.retrieve(vaultId).getOrThrow()
        try {
            val result = repository.unlockWithBiometricKey(candidate, otherAttempt)

            assertTrue(result.exceptionOrNull() is VaultSessionLockedException)
            assertFalse(repository.isUnlocked())
            assertTrue(keyStore.isEnrolled)
            assertEquals(0, keyStore.deleteCalls)
        } finally {
            candidate.fill(0)
        }
    }

    private suspend fun enrollAndLock() {
        val password = SensitiveText.from(MASTER_PASSWORD)
        try {
            assertTrue(repository.create(password).isSuccess)
            assertTrue(repository.unlock(password).isSuccess)
            assertEquals(BiometricOperationResult.Success, service.enable())
            assertTrue(repository.lock().isSuccess)
        } finally {
            password.clear()
        }
    }

    private suspend fun unlockWithPassword() {
        val password = SensitiveText.from(MASTER_PASSWORD)
        try {
            assertTrue(repository.unlock(password).isSuccess)
        } finally {
            password.clear()
        }
    }

    private suspend fun TestScope.assertCompletedLockRejects(gate: Gate) {
        val observed = mutableListOf<VaultSessionState>()
        val collector = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.getSessionState().collect(observed::add)
        }
        val unlock = async { service.unlock() }
        try {
            gate.entered.await()
            assertTrue(repository.lock(LockReason.MemoryPressure).isSuccess)
            assertEquals(VaultSessionState.Locked(LockReason.MemoryPressure), repository.getSessionState().first())
            assertFalse(unlock.isCompleted)
            gate.release.complete(Unit)

            assertEquals(vaultLockedResult(), unlock.await())
            assertFalse(repository.isUnlocked())
            assertEquals(VaultSessionState.Locked(LockReason.MemoryPressure), repository.getSessionState().first())
            assertFalse(observed.any { it is VaultSessionState.Unlocked })
            assertEquals(1, keyStore.retrieveCalls)
            assertTrue(keyStore.isEnrolled)
            assertEquals(0, keyStore.deleteCalls)
            assertReturnedKeyWiped()
        } finally {
            gate.release.complete(Unit)
            unlock.cancel()
            collector.cancel()
            try {
                unlock.join()
            } finally {
                collector.join()
            }
        }
    }

    private fun assertReturnedKeyWiped() {
        assertTrue(requireNotNull(keyStore.lastReturnedKey).all { it == 0.toByte() })
    }

    private fun vaultLockedResult() = BiometricOperationResult.Failure(BiometricFailureReason.VAULT_LOCKED)

    private class Gate {
        val entered = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()

        suspend fun pause() {
            entered.complete(Unit)
            release.await()
        }
    }

    private class GatedMetadataDao(private val delegate: VaultMetadataDao) : VaultMetadataDao by delegate {
        private var nextGet: Gate? = null
        private var nextLastAccess: Gate? = null
        var getCalls = 0

        fun pauseNextGet(): Gate = Gate().also {
            check(nextGet == null)
            nextGet = it
        }

        fun pauseNextLastAccess(): Gate = Gate().also {
            check(nextLastAccess == null)
            nextLastAccess = it
        }

        override suspend fun get(): VaultMetadataEntity? {
            getCalls++
            val gate = nextGet
            nextGet = null
            gate?.pause()
            return delegate.get()
        }

        override suspend fun updateLastAccessed(timestamp: Long) {
            val gate = nextLastAccess
            nextLastAccess = null
            gate?.pause()
            delegate.updateLastAccessed(timestamp)
        }
    }

    private class ControlledKeyStore : BiometricKeyStore {
        private var storedKey: ByteArray? = null
        private var enrolledVaultId: String? = null
        private var nextContains: Gate? = null
        private var nextRetrieval: Gate? = null
        var nextCandidate: ByteArray? = null
        var cancelOnReturn = false
        var containsCalls = 0
        var retrieveCalls = 0
        var deleteCalls = 0
        var lastReturnedKey: ByteArray? = null
            private set
        val isEnrolled: Boolean get() = storedKey != null

        fun pauseNextContains(): Gate = Gate().also {
            check(nextContains == null)
            nextContains = it
        }

        fun pauseNextRetrieval(): Gate = Gate().also {
            check(nextRetrieval == null)
            nextRetrieval = it
        }

        override suspend fun getCapability() = BiometricCapability(
            type = BiometricType.GENERIC,
            availability = BiometricAvailability.AVAILABLE,
        )

        override suspend fun contains(vaultId: String): Boolean {
            containsCalls++
            val gate = nextContains
            nextContains = null
            gate?.pause()
            return vaultId == enrolledVaultId && isEnrolled
        }

        override suspend fun enroll(vaultId: String, vaultKey: ByteArray): Result<Unit> {
            clear()
            enrolledVaultId = vaultId
            storedKey = vaultKey.copyOf()
            return Result.success(Unit)
        }

        override suspend fun retrieve(vaultId: String): Result<ByteArray> {
            retrieveCalls++
            val candidate = if (vaultId == enrolledVaultId) {
                nextCandidate ?: storedKey?.copyOf()
            } else {
                null
            } ?: return Result.failure(BiometricKeyStoreException.NotEnabled())
            nextCandidate = null
            val gate = nextRetrieval
            nextRetrieval = null
            var transferred = false
            return try {
                gate?.pause()
                if (cancelOnReturn) currentCoroutineContext().cancel(CancellationException("Synthetic cancellation"))
                lastReturnedKey = candidate
                transferred = true
                Result.success(candidate)
            } finally {
                if (!transferred) candidate.fill(0)
            }
        }

        override suspend fun delete(vaultId: String): Result<Unit> {
            check(vaultId == enrolledVaultId)
            deleteCalls++
            clear()
            return Result.success(Unit)
        }

        fun clear() {
            storedKey?.fill(0)
            storedKey = null
            nextCandidate?.fill(0)
            nextCandidate = null
            enrolledVaultId = null
        }

        fun dispose() {
            clear()
            lastReturnedKey?.fill(0)
            lastReturnedKey = null
        }
    }

    private companion object {
        const val MASTER_PASSWORD = "correct horse battery staple"
    }
}
