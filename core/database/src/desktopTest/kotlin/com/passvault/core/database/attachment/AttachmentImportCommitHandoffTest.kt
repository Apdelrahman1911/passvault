package com.passvault.core.database.attachment

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.passvault.core.database.VaultDatabase
import com.passvault.core.database.dao.AttachmentDao
import com.passvault.core.database.entity.AttachmentRecordEntity
import com.passvault.core.database.entity.CredentialRecordEntity
import com.passvault.core.database.repository.VaultSessionManager
import com.passvault.core.domain.model.AttachmentId
import com.passvault.core.domain.model.CredentialId
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.repository.AttachmentContentSource
import com.passvault.core.domain.repository.LockReason
import com.passvault.core.testing.fakes.FakeCryptoEngine
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okio.Buffer
import okio.BufferedSink
import okio.BufferedSource
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.CoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * The generated Room DAO really commits on a separately queued dispatcher.
 * No DAO double withholds or throws its result. Fake crypto and an in-memory
 * encrypted-object store isolate result dispatch; real-file/libsodium controls
 * live in AttachmentRepositoryTest.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AttachmentImportCommitHandoffTest {
    @Test
    fun `cancel after real Room commit but before result dispatch keeps the referenced object`() = runTest {
        exercise(Delivery.CANCEL_AFTER_COMMIT)
    }

    @Test
    fun `cancel while the real Room insert is queued respects its eventual durable ownership`() = runTest {
        exercise(Delivery.CANCEL_QUEUED_INSERT)
    }

    @Test
    fun `successful real Room result delivery keeps a verifiable object`() = runTest {
        exercise(Delivery.DELIVER)
    }

    private suspend fun TestScope.exercise(delivery: Delivery) {
        val roomDispatcher = QueuedRoomDispatcher()
        val database = Room.inMemoryDatabaseBuilder<VaultDatabase>()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(roomDispatcher)
            .build()
        val blobs = MemoryBlobStore()
        val session = SyntheticSession()
        val source = SmallSource()
        var importing: Deferred<*>? = null
        try {
            val seed = async { database.credentialDao().insertOrUpdate(ownerEntity()) }
            pumpUntil(roomDispatcher) { seed.isCompleted }
            seed.await()

            val delegate = database.attachmentDao()
            var requestedInsert: AttachmentRecordEntity? = null
            val observedDao = object : AttachmentDao by delegate {
                override suspend fun insert(entity: AttachmentRecordEntity): Long {
                    requestedInsert = entity
                    return delegate.insert(entity)
                }
            }
            val repository = AttachmentRepositoryImpl(
                attachmentDao = observedDao,
                credentialDao = database.credentialDao(),
                blobStore = blobs,
                cryptoEngine = FakeCryptoEngine(),
                sessionManager = session,
            )
            val operation = async { repository.import(CredentialId(OWNER_ID), source) }
            importing = operation
            // Stop with the actual insert dispatched but not yet run by Room.
            pumpUntil(roomDispatcher) { requestedInsert != null }
            val row = assertNotNull(requestedInsert)
            assertFalse(operation.isCompleted)
            assertTrue(blobs.exists(row.storagePath))

            verifyCommitBeforeDelivery(delivery, roomDispatcher, delegate, row, operation)
            if (delivery != Delivery.DELIVER) operation.cancel()
            pumpUntil(roomDispatcher) { operation.isCompleted }
            assertEquals(1, source.closeCalls)

            val persisted = async { delegate.getById(row.id, OWNER_ID) }
            pumpUntil(roomDispatcher) { persisted.isCompleted }
            val persistedRow = persisted.await()
            // Cancellation of a queued DAO call is not proof of rollback:
            // Room may run it under its database-owned Job. Either outcome
            // must preserve the durable row/object ownership invariant.
            if (delivery == Delivery.CANCEL_QUEUED_INSERT && persistedRow == null) {
                assertTrue(operation.isCancelled)
                assertFalse(blobs.exists(row.storagePath))
            } else {
                assertEquals(row, persistedRow)
                assertTrue(blobs.exists(row.storagePath))
                if (delivery == Delivery.DELIVER) operation.await().getOrThrow()
                else assertTrue(operation.isCancelled)
                val verified = async { repository.verify(CredentialId(OWNER_ID), AttachmentId(row.id)) }
                pumpUntil(roomDispatcher) { verified.isCompleted }
                verified.await().getOrThrow()
            }
        } finally {
            importing?.cancel()
            pumpUntil(roomDispatcher) { importing?.isCompleted != false }
            database.close()
            roomDispatcher.drain()
            blobs.clear()
            session.clear()
        }
    }

    private fun TestScope.verifyCommitBeforeDelivery(
        delivery: Delivery,
        roomDispatcher: QueuedRoomDispatcher,
        delegate: AttachmentDao,
        row: AttachmentRecordEntity,
        operation: Deferred<*>,
    ) {
        if (delivery != Delivery.CANCEL_QUEUED_INSERT) {
            // Drain ONLY Room. The caller's successful insert continuation
            // remains queued on runTest's dispatcher, not executed here.
            roomDispatcher.drain()
            val durableRead = async(roomDispatcher) { delegate.getById(row.id, OWNER_ID) }
            roomDispatcher.drain()
            assertTrue(durableRead.isCompleted)
            assertEquals(row, durableRead.getCompleted(), "The generated DAO has durably committed READY")
            assertFalse(operation.isCompleted, "The caller has not received Room's success result")
        }
    }

    private fun TestScope.pumpUntil(dispatcher: QueuedRoomDispatcher, complete: () -> Boolean) {
        repeat(MAX_DISPATCH_ROUNDS) {
            testScheduler.runCurrent()
            if (complete()) return
            dispatcher.drain()
        }
        check(complete()) { "Synthetic dispatcher schedule did not settle within its bounded step budget" }
    }

    private class QueuedRoomDispatcher : CoroutineDispatcher() {
        private val work = ConcurrentLinkedQueue<Runnable>()

        override fun dispatch(context: CoroutineContext, block: Runnable) {
            work.add(block)
        }

        fun drain() {
            repeat(MAX_DISPATCH_ROUNDS) {
                val next = work.poll() ?: return
                next.run()
            }
            check(work.isEmpty()) { "Room's synthetic dispatcher did not become idle" }
        }
    }

    private class SmallSource : AttachmentContentSource {
        override val displayName = "synthetic.bin"
        override val claimedMimeType: String? = null
        override val declaredSizeBytes = 3L
        var closeCalls = 0
        private var read = false

        override suspend fun read(buffer: ByteArray): Int {
            if (read) return -1
            read = true
            byteArrayOf(1, 2, 3).copyInto(buffer)
            return 3
        }

        override suspend fun close() {
            closeCalls++
        }
    }

    private class MemoryBlobStore : AttachmentBlobStore {
        private val objects = mutableMapOf<String, ByteArray>()

        override suspend fun <T> writeAtomically(relativePath: String, writer: suspend (BufferedSink) -> T): T {
            check(relativePath !in objects)
            val buffer = Buffer()
            return try {
                writer(buffer).also { objects[relativePath] = buffer.readByteArray() }
            } finally {
                buffer.clear()
            }
        }

        override suspend fun <T> read(
            relativePath: String,
            maxBytes: Long,
            reader: suspend (BufferedSource, Long) -> T,
        ): T {
            val bytes = requireNotNull(objects[relativePath])
            require(bytes.size.toLong() <= maxBytes)
            val buffer = Buffer().write(bytes)
            return try { reader(buffer, bytes.size.toLong()) } finally { buffer.clear() }
        }

        override suspend fun delete(relativePath: String) {
            objects.remove(relativePath)?.fill(0)
        }

        override suspend fun exists(relativePath: String) = relativePath in objects

        override suspend fun removeUnreferencedObjects(referencedPaths: Set<String>) {
            (objects.keys - referencedPaths).forEach { delete(it) }
        }

        fun clear() {
            objects.values.forEach { it.fill(0) }
            objects.clear()
        }
    }

    private class SyntheticSession : VaultSessionManager {
        private val key = ByteArray(32) { 0x5a }
        override suspend fun <T> withUnlockedSession(block: suspend (ByteArray) -> T): T = block(key)
        override suspend fun <T> lockAndRun(reason: LockReason, block: suspend () -> T): T = block()
        override suspend fun matchesMasterPassword(candidate: SensitiveText): Boolean = false
        fun clear() = key.fill(0)
    }

    private fun ownerEntity() = CredentialRecordEntity(
        id = OWNER_ID,
        type = "Login",
        summaryPayload = byteArrayOf(1),
        summaryNonce = ByteArray(24),
        secretPayload = byteArrayOf(1),
        secretNonce = ByteArray(24),
        folderId = null,
        createdAt = 0,
        updatedAt = 0,
        lastUsedAt = null,
    )

    private enum class Delivery { CANCEL_QUEUED_INSERT, CANCEL_AFTER_COMMIT, DELIVER }

    private companion object {
        const val OWNER_ID = "synthetic-commit-owner"
        const val MAX_DISPATCH_ROUNDS = 1_000
    }
}
