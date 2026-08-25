package com.passvault.core.database.attachment

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.passvault.core.crypto.DesktopCryptoEngine
import com.passvault.core.crypto.VaultKeyHierarchy
import com.passvault.core.database.VaultDatabase
import com.passvault.core.database.entity.AttachmentRecordEntity
import com.passvault.core.database.repository.CredentialRepositoryImpl
import com.passvault.core.database.repository.VaultRepositoryImpl
import com.passvault.core.domain.model.AttachmentId
import com.passvault.core.domain.model.Credential
import com.passvault.core.domain.model.CredentialId
import com.passvault.core.domain.model.CredentialType
import com.passvault.core.domain.model.PasswordHealth
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.repository.AttachmentContentSink
import com.passvault.core.domain.repository.AttachmentContentSource
import com.passvault.core.domain.repository.AttachmentCorruptedException
import com.passvault.core.domain.repository.AttachmentCountLimitException
import com.passvault.core.domain.repository.AttachmentFileTooLargeException
import com.passvault.core.domain.repository.AttachmentInvalidFileNameException
import com.passvault.core.domain.repository.AttachmentPolicy
import com.passvault.core.domain.repository.AttachmentTotalSizeLimitException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import okio.BufferedSink
import okio.BufferedSource
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.time.Instant
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Suppress("TooManyFunctions") // Cohesive filesystem/database security fixture shares one expensive vault setup.
class AttachmentRepositoryTest {
    private lateinit var database: VaultDatabase
    private lateinit var cryptoEngine: DesktopCryptoEngine
    private lateinit var vaultRepository: VaultRepositoryImpl
    private lateinit var credentialRepository: CredentialRepositoryImpl
    private lateinit var attachmentRepository: AttachmentRepositoryImpl
    private lateinit var blobStore: FailingDeleteBlobStore
    private lateinit var storageRoot: Path
    private val credentialId = CredentialId("credential-attachments")

    @BeforeTest
    fun setUp() = runTest {
        storageRoot = Files.createTempDirectory("passvault-attachment-test-")
        database = Room.inMemoryDatabaseBuilder<VaultDatabase>()
            .setDriver(BundledSQLiteDriver())
            .build()
        cryptoEngine = DesktopCryptoEngine()
        vaultRepository = VaultRepositoryImpl(
            vaultMetadataDao = database.vaultMetadataDao(),
            cryptoEngine = cryptoEngine,
            keyHierarchy = VaultKeyHierarchy(cryptoEngine),
        )
        blobStore = FailingDeleteBlobStore(LocalAttachmentBlobStore(storageRoot.toString()))
        attachmentRepository = AttachmentRepositoryImpl(
            attachmentDao = database.attachmentDao(),
            credentialDao = database.credentialDao(),
            blobStore = blobStore,
            cryptoEngine = cryptoEngine,
            sessionManager = vaultRepository,
        )
        credentialRepository = CredentialRepositoryImpl(
            credentialDao = database.credentialDao(),
            folderDao = database.folderDao(),
            tagDao = database.tagDao(),
            attachmentDao = database.attachmentDao(),
            passwordHistoryDao = database.passwordHistoryDao(),
            cryptoEngine = cryptoEngine,
            sessionManager = vaultRepository,
            attachmentLifecycleManager = attachmentRepository,
        )
        unlockVault()
        val credential = sampleCredential()
        try {
            assertTrue(credentialRepository.save(credential).isSuccess)
        } finally {
            credential.clearSensitiveValues()
        }
    }

    @AfterTest
    fun tearDown() {
        runBlocking { vaultRepository.lock() }
        database.close()
        storageRoot.toFile().deleteRecursively()
    }

    @Test
    fun `import detects content type streams plaintext and allocates duplicate names`() = runTest {
        val pdf = "%PDF-1.7\nattachment".encodeToByteArray()
        val firstSource = ByteArraySource("report.pdf", "image/png", pdf)
        val secondSource = ByteArraySource("report.pdf", "text/plain", byteArrayOf(1, 2, 3))

        val first = attachmentRepository.import(credentialId, firstSource).getOrThrow()
        val second = attachmentRepository.import(credentialId, secondSource).getOrThrow()

        assertEquals("report.pdf", first.fileName)
        assertEquals("application/pdf", first.mimeType)
        assertEquals("report (2).pdf", second.fileName)
        assertEquals("application/octet-stream", second.mimeType)
        assertTrue(firstSource.closed)
        assertTrue(secondSource.closed)
        val sink = RecordingSink()
        assertTrue(attachmentRepository.copyContentTo(credentialId, first.id, sink).isSuccess)
        assertContentEquals(pdf, sink.bytes())
        assertTrue(sink.committed)
        assertFalse(sink.aborted)
    }

    @Test
    fun `concurrent duplicate imports are serialized and remain independently decryptable`() = runTest {
        val first = async {
            attachmentRepository.import(credentialId, ByteArraySource("same.txt", null, byteArrayOf(1)))
        }
        val second = async {
            attachmentRepository.import(credentialId, ByteArraySource("same.txt", null, byteArrayOf(2)))
        }

        val attachments = listOf(first.await().getOrThrow(), second.await().getOrThrow())
        assertEquals(setOf("same.txt", "same (2).txt"), attachments.map { it.fileName }.toSet())
        assertEquals(2, attachments.map { it.id }.toSet().size)
        attachments.forEach { attachment ->
            assertTrue(attachmentRepository.verify(credentialId, attachment.id).isSuccess)
        }
    }

    @Test
    fun `rename validates names resolves duplicates and delete removes row and object`() = runTest {
        val first = attachmentRepository.import(
            credentialId,
            ByteArraySource("first.txt", null, byteArrayOf(1)),
        ).getOrThrow()
        attachmentRepository.import(
            credentialId,
            ByteArraySource("second.txt", null, byteArrayOf(2)),
        ).getOrThrow()
        val invalid = attachmentRepository.rename(credentialId, first.id, "../escape.txt")
        assertIs<AttachmentInvalidFileNameException>(invalid.exceptionOrNull())

        val renamed = attachmentRepository.rename(credentialId, first.id, "second.txt").getOrThrow()
        assertEquals("second (2).txt", renamed.fileName)
        val entity = requireEntity(first.id)
        val objectPath = objectPath(entity)
        assertTrue(Files.exists(objectPath))

        assertTrue(attachmentRepository.delete(credentialId, first.id).isSuccess)
        assertNull(database.attachmentDao().getById(first.id.value, credentialId.value))
        assertFalse(Files.exists(objectPath))
    }

    @Test
    fun `ciphertext tampering is rejected and aborts partial plaintext output`() = runTest {
        val content = ByteArray(AttachmentPolicy.CONTENT_CHUNK_BYTES + 17) { (it % 251).toByte() }
        val attachment = attachmentRepository.import(
            credentialId,
            ByteArraySource("tamper.bin", null, content),
        ).getOrThrow()
        val path = objectPath(requireEntity(attachment.id))
        val encrypted = Files.readAllBytes(path)
        encrypted[encrypted.lastIndex - 8] = (encrypted[encrypted.lastIndex - 8].toInt() xor 1).toByte()
        Files.write(path, encrypted)
        val sink = RecordingSink()

        val result = attachmentRepository.copyContentTo(credentialId, attachment.id, sink)

        assertIs<AttachmentCorruptedException>(result.exceptionOrNull())
        assertFalse(sink.committed)
        assertTrue(sink.aborted)
        assertEquals(0, sink.bytes().size)
    }

    @Test
    fun `truncated containers and swapped attachment objects fail authentication`() = runTest {
        val first = attachmentRepository.import(
            credentialId,
            ByteArraySource("first.bin", null, ByteArray(300_000) { 1 }),
        ).getOrThrow()
        val second = attachmentRepository.import(
            credentialId,
            ByteArraySource("second.bin", null, ByteArray(300_000) { 2 }),
        ).getOrThrow()
        val firstPath = objectPath(requireEntity(first.id))
        val secondPath = objectPath(requireEntity(second.id))
        Files.copy(firstPath, secondPath, StandardCopyOption.REPLACE_EXISTING)
        assertIs<AttachmentCorruptedException>(
            attachmentRepository.verify(credentialId, second.id).exceptionOrNull(),
        )

        Files.newByteChannel(firstPath, java.nio.file.StandardOpenOption.WRITE).use { channel ->
            channel.truncate(Files.size(firstPath) - 1)
        }
        assertIs<AttachmentCorruptedException>(
            attachmentRepository.verify(credentialId, first.id).exceptionOrNull(),
        )
    }

    @Test
    fun `source failure closes input and leaves no row object or staging file`() = runTest {
        val source = FailingSource()

        assertTrue(attachmentRepository.import(credentialId, source).isFailure)

        assertTrue(source.closed)
        assertTrue(database.attachmentDao().getByCredential(credentialId.value).isEmpty())
        assertDirectoryEmpty(storageRoot.resolve("objects"))
        assertDirectoryEmpty(storageRoot.resolve("staging"))
    }

    @Test
    fun `cancelled import closes input and removes partial state`() = runTest {
        val readStarted = CompletableDeferred<Unit>()
        val source = SuspendingSource(readStarted)
        val importJob = async { attachmentRepository.import(credentialId, source) }
        readStarted.await()

        importJob.cancelAndJoin()

        assertTrue(source.closed)
        assertTrue(database.attachmentDao().getByCredential(credentialId.value).isEmpty())
        assertDirectoryEmpty(storageRoot.resolve("objects"))
        assertDirectoryEmpty(storageRoot.resolve("staging"))
    }

    @Test
    fun `recovery removes an encrypted staging object left by process interruption`() = runTest {
        val attachment = attachmentRepository.import(
            credentialId,
            ByteArraySource("initialize-storage.bin", null, byteArrayOf(1)),
        ).getOrThrow()
        assertTrue(attachmentRepository.delete(credentialId, attachment.id).isSuccess)
        val abandoned = storageRoot.resolve(
            "staging/00000000-0000-0000-0000-000000000001.pva.tmp",
        )
        Files.write(abandoned, byteArrayOf(1, 2, 3))

        attachmentRepository.withStableAttachments { }

        assertFalse(Files.exists(abandoned))
        assertDirectoryEmpty(storageRoot.resolve("staging"))
    }

    @Test
    fun `recovery implementation is not exposed as a public repository operation`() {
        assertFalse(
            AttachmentRepositoryImpl::class.java.methods.any { method ->
                method.name.startsWith("recoverInterruptedOperations")
            },
        )
    }

    @Test
    fun `declared file size and count limits fail before plaintext is consumed`() = runTest {
        val oversized = ByteArraySource(
            name = "oversized.bin",
            mime = null,
            content = byteArrayOf(1),
            declaredSize = AttachmentPolicy.MAX_FILE_SIZE_BYTES + 1,
        )
        assertIs<AttachmentFileTooLargeException>(
            attachmentRepository.import(credentialId, oversized).exceptionOrNull(),
        )
        assertEquals(0, oversized.readCalls)

        repeat(AttachmentPolicy.MAX_ATTACHMENTS_PER_CREDENTIAL) { index ->
            attachmentRepository.import(
                credentialId,
                ByteArraySource("file-$index.bin", null, byteArrayOf(index.toByte())),
            ).getOrThrow()
        }
        val countSource = ByteArraySource("one-too-many.bin", null, byteArrayOf(1))
        assertIs<AttachmentCountLimitException>(
            attachmentRepository.import(credentialId, countSource).exceptionOrNull(),
        )
        assertEquals(0, countSource.readCalls)
    }

    @Test
    fun `credential aggregate limit fails before opening the selected source`() = runTest {
        repeat(5) { index -> insertAggregateLimitRow(index) }
        val source = ByteArraySource(
            name = "over-total.bin",
            mime = null,
            content = byteArrayOf(1),
            declaredSize = 13L * 1024L * 1024L,
        )

        assertIs<AttachmentTotalSizeLimitException>(
            attachmentRepository.import(credentialId, source).exceptionOrNull(),
        )
        assertEquals(0, source.readCalls)
    }

    @Test
    fun `exact maximum file size streams without a full plaintext allocation`() = runTest {
        val source = RepeatingSource(AttachmentPolicy.MAX_FILE_SIZE_BYTES)
        val attachment = attachmentRepository.import(credentialId, source).getOrThrow()
        val sink = CountingSink()

        assertEquals(AttachmentPolicy.MAX_FILE_SIZE_BYTES, attachment.sizeBytes)
        assertTrue(attachmentRepository.copyContentTo(credentialId, attachment.id, sink).isSuccess)
        assertEquals(AttachmentPolicy.MAX_FILE_SIZE_BYTES, sink.byteCount)
        assertTrue(sink.committed)
    }

    @Test
    fun `credential deletion cascades metadata then removes every encrypted attachment`() = runTest {
        val attachment = attachmentRepository.import(
            credentialId,
            ByteArraySource("delete-with-parent.bin", null, byteArrayOf(4, 5, 6)),
        ).getOrThrow()
        val path = objectPath(requireEntity(attachment.id))

        assertTrue(credentialRepository.delete(credentialId).isSuccess)

        assertFalse(Files.exists(path))
        assertNull(database.attachmentDao().getById(attachment.id.value, credentialId.value))
        assertNull(database.credentialDao().getById(credentialId.value))
    }

    @Test
    fun `attachment delete commits metadata first and recovers an object cleanup failure`() = runTest {
        val attachment = attachmentRepository.import(
            credentialId,
            ByteArraySource("cleanup-failure.bin", null, byteArrayOf(7, 8, 9)),
        ).getOrThrow()
        val path = objectPath(requireEntity(attachment.id))
        blobStore.failNextDelete = true

        assertTrue(attachmentRepository.delete(credentialId, attachment.id).isSuccess)

        assertNull(database.attachmentDao().getById(attachment.id.value, credentialId.value))
        assertTrue(Files.exists(path))
        attachmentRepository.withStableAttachments { }
        assertFalse(Files.exists(path))
    }

    @Test
    fun `credential delete preserves its Room commit when encrypted object cleanup fails`() = runTest {
        val attachment = attachmentRepository.import(
            credentialId,
            ByteArraySource("parent-cleanup-failure.bin", null, byteArrayOf(1, 3, 5)),
        ).getOrThrow()
        val path = objectPath(requireEntity(attachment.id))
        blobStore.failNextDelete = true

        assertTrue(credentialRepository.delete(credentialId).isSuccess)

        assertNull(database.credentialDao().getById(credentialId.value))
        assertNull(database.attachmentDao().getById(attachment.id.value, credentialId.value))
        assertTrue(Files.exists(path))
        attachmentRepository.withStableAttachments { }
        assertFalse(Files.exists(path))
    }

    private suspend fun unlockVault() {
        val password = SensitiveText.from(TEST_MASTER_PASSWORD)
        try {
            assertTrue(vaultRepository.create(password).isSuccess)
            assertTrue(vaultRepository.unlock(password).isSuccess)
        } finally {
            password.clear()
        }
    }

    private fun sampleCredential() = Credential(
        id = credentialId,
        type = CredentialType.Login,
        title = "Attachment test",
        username = null,
        email = null,
        password = SensitiveText.from("test-password"),
        urls = emptyList(),
        notes = null,
        recoveryCodes = emptyList(),
        apiKeys = emptyList(),
        licenseKeys = emptyList(),
        customFields = emptyList(),
        folderId = null,
        tagIds = emptySet(),
        isFavorite = false,
        attachments = emptyList(),
        passwordHistory = emptyList(),
        createdAt = Instant.fromEpochMilliseconds(1),
        updatedAt = Instant.fromEpochMilliseconds(1),
        lastUsedAt = null,
        passwordHealth = PasswordHealth.UNKNOWN,
    )

    private suspend fun requireEntity(id: AttachmentId): AttachmentRecordEntity = assertNotNull(
        database.attachmentDao().getById(id.value, credentialId.value),
    )

    private fun objectPath(entity: AttachmentRecordEntity): Path = storageRoot.resolve(entity.storagePath)

    private suspend fun insertAggregateLimitRow(index: Int) {
        val objectId = "00000000-0000-0000-0000-${index.toString().padStart(12, '0')}"
        database.attachmentDao().insert(
            AttachmentRecordEntity(
                id = "aggregate-$index",
                credentialId = credentialId.value,
                encryptedFilename = byteArrayOf(1),
                filenameNonce = byteArrayOf(1),
                mimeType = "application/octet-stream",
                sizeBytes = 100L * 1024L * 1024L,
                storagePath = "objects/$objectId.pva",
                keyDerivationContext = "aggregate-context-$index",
                createdAt = index.toLong(),
                contentFormatVersion = AttachmentPolicy.CONTENT_FORMAT_VERSION,
                storageState = AttachmentRecordEntity.STORAGE_STATE_READY,
            ),
        )
    }

    private fun assertDirectoryEmpty(path: Path) {
        if (!Files.exists(path)) return
        Files.list(path).use { assertEquals(0, it.count()) }
    }

    private class ByteArraySource(
        name: String,
        mime: String?,
        private val content: ByteArray,
        declaredSize: Long? = content.size.toLong(),
    ) : AttachmentContentSource {
        override val displayName = name
        override val claimedMimeType = mime
        override val declaredSizeBytes = declaredSize
        var readCalls = 0
        var closed = false
        private var offset = 0

        override suspend fun read(buffer: ByteArray): Int {
            readCalls++
            if (offset == content.size) return -1
            val count = minOf(buffer.size, content.size - offset)
            content.copyInto(buffer, endIndex = offset + count, startIndex = offset)
            offset += count
            return count
        }

        override suspend fun close() {
            closed = true
        }
    }

    private class FailingSource : AttachmentContentSource {
        override val displayName = "disappearing.bin"
        override val claimedMimeType: String? = null
        override val declaredSizeBytes: Long? = null
        var closed = false
        private var firstRead = true

        override suspend fun read(buffer: ByteArray): Int {
            if (firstRead) {
                firstRead = false
                buffer[0] = 1
                return 1
            }
            error("The selected source disappeared")
        }

        override suspend fun close() {
            closed = true
        }
    }

    private class SuspendingSource(
        private val readStarted: CompletableDeferred<Unit>,
    ) : AttachmentContentSource {
        override val displayName = "cancel.bin"
        override val claimedMimeType: String? = null
        override val declaredSizeBytes: Long? = null
        var closed = false

        override suspend fun read(buffer: ByteArray): Int {
            readStarted.complete(Unit)
            awaitCancellation()
        }

        override suspend fun close() {
            closed = true
        }
    }

    private class RepeatingSource(
        private val size: Long,
    ) : AttachmentContentSource {
        override val displayName = "maximum.bin"
        override val claimedMimeType: String? = null
        override val declaredSizeBytes = size
        private var remaining = size

        override suspend fun read(buffer: ByteArray): Int {
            if (remaining == 0L) return -1
            val count = minOf(buffer.size.toLong(), remaining).toInt()
            buffer.fill(0x5a.toByte(), toIndex = count)
            remaining -= count
            return count
        }

        override suspend fun close() = Unit
    }

    private class RecordingSink : AttachmentContentSink {
        private val output = java.io.ByteArrayOutputStream()
        var committed = false
        var aborted = false

        override suspend fun write(buffer: ByteArray, byteCount: Int) {
            output.write(buffer, 0, byteCount)
        }

        override suspend fun commit() {
            committed = true
        }

        override suspend fun abort() {
            aborted = true
            output.reset()
        }

        fun bytes(): ByteArray = output.toByteArray()
    }

    private class CountingSink : AttachmentContentSink {
        var byteCount = 0L
        var committed = false

        override suspend fun write(buffer: ByteArray, byteCount: Int) {
            this.byteCount += byteCount
        }

        override suspend fun commit() {
            committed = true
        }

        override suspend fun abort() = Unit
    }

    private class FailingDeleteBlobStore(
        private val delegate: AttachmentBlobStore,
    ) : AttachmentBlobStore {
        var failNextDelete = false

        override suspend fun <T> writeAtomically(
            relativePath: String,
            writer: suspend (BufferedSink) -> T,
        ): T = delegate.writeAtomically(relativePath, writer)

        override suspend fun <T> read(
            relativePath: String,
            maxBytes: Long,
            reader: suspend (BufferedSource, Long) -> T,
        ): T = delegate.read(relativePath, maxBytes, reader)

        override suspend fun delete(relativePath: String) {
            if (failNextDelete) {
                failNextDelete = false
                error("simulated encrypted object cleanup failure")
            }
            delegate.delete(relativePath)
        }

        override suspend fun exists(relativePath: String): Boolean = delegate.exists(relativePath)

        override suspend fun removeUnreferencedObjects(referencedPaths: Set<String>) {
            delegate.removeUnreferencedObjects(referencedPaths)
        }
    }

    private companion object {
        const val TEST_MASTER_PASSWORD = "correct horse battery staple"
    }
}
