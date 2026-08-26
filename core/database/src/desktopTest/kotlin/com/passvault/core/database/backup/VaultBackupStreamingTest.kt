package com.passvault.core.database.backup

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.passvault.core.crypto.DesktopCryptoEngine
import com.passvault.core.crypto.VaultKeyHierarchy
import com.passvault.core.database.VaultDatabase
import com.passvault.core.database.attachment.AttachmentRepositoryImpl
import com.passvault.core.database.attachment.LocalAttachmentBlobStore
import com.passvault.core.database.repository.CredentialRepositoryImpl
import com.passvault.core.database.repository.VaultRepositoryImpl
import com.passvault.core.domain.model.AttachmentAvailability
import com.passvault.core.domain.model.Credential
import com.passvault.core.domain.model.CredentialId
import com.passvault.core.domain.model.CredentialType
import com.passvault.core.domain.model.PasswordHealth
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.repository.AttachmentContentSink
import com.passvault.core.domain.repository.AttachmentContentSource
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import kotlin.time.Instant
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertFails
import kotlin.test.assertTrue

@Suppress("TooManyFunctions") // One fixture exercises the complete streaming backup transaction boundary.
class VaultBackupStreamingTest {
    private lateinit var database: VaultDatabase
    private lateinit var vaultRepository: VaultRepositoryImpl
    private lateinit var credentialRepository: CredentialRepositoryImpl
    private lateinit var attachmentRepository: AttachmentRepositoryImpl
    private lateinit var backupService: VaultBackupService
    private lateinit var blobStore: LocalAttachmentBlobStore
    private lateinit var storageRoot: Path
    private lateinit var backupPath: Path
    private val cryptoEngine = DesktopCryptoEngine()
    private val credentialId = CredentialId("streaming-backup-credential")
    private val attachmentContent = ByteArray(700_123) { index -> (index % 251).toByte() }

    @BeforeTest
    fun setUp() = runTest {
        storageRoot = Files.createTempDirectory("passvault-streaming-backup-")
        backupPath = storageRoot.resolve("candidate.pvault")
        database = Room.inMemoryDatabaseBuilder<VaultDatabase>()
            .setDriver(BundledSQLiteDriver())
            .build()
        vaultRepository = VaultRepositoryImpl(
            vaultMetadataDao = database.vaultMetadataDao(),
            cryptoEngine = cryptoEngine,
            keyHierarchy = VaultKeyHierarchy(cryptoEngine),
        )
        blobStore = LocalAttachmentBlobStore(storageRoot.resolve("vault-files").toString())
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
        backupService = VaultBackupService(
            backupDao = database.vaultBackupDao(),
            database = database,
            cryptoEngine = cryptoEngine,
            vaultRepository = vaultRepository,
            sessionManager = vaultRepository,
            attachmentBlobStore = blobStore,
            attachmentLifecycleManager = attachmentRepository,
        )
        unlockNewVault()
        val credential = sampleCredential()
        try {
            credentialRepository.save(credential).getOrThrow()
        } finally {
            credential.clearSensitiveValues()
        }
        attachmentRepository.import(
            credentialId,
            ByteArrayAttachmentSource("evidence.bin", attachmentContent),
        ).getOrThrow()
    }

    @AfterTest
    fun tearDown() {
        runBlocking { vaultRepository.lock() }
        database.close()
        storageRoot.toFile().deleteRecursively()
    }

    @Test
    fun `v2 backup streams authenticates and restores an attachment to a fresh object`() = runTest {
        val originalEntity = database.attachmentDao().getByCredential(credentialId.value).single()
        val createSink = PathBackupSink(backupPath)

        withBackupPassword { password ->
            val created = backupService.createBackup(password, createSink).getOrThrow()
            assertEquals(1, created.credentialCount)
            assertEquals(1, created.attachmentCount)
        }
        assertTrue(createSink.committed)
        assertFalse(createSink.aborted)
        assertTrue(Files.size(backupPath) > attachmentContent.size)

        val inspectSource = PathBackupSource(backupPath, maximumChunkBytes = 7)
        withBackupPassword { password ->
            val inspection = backupService.inspectBackup(inspectSource, password).getOrThrow()
            assertEquals(1, inspection.credentialCount)
            assertEquals(1, inspection.attachmentCount)
            assertTrue(inspection.warnings.isEmpty())
        }
        assertTrue(inspectSource.closed)
        assertTrue(inspectSource.maximumRequestedBytes <= STREAM_READ_BUFFER_BYTES)

        credentialRepository.delete(credentialId).getOrThrow()
        assertTrue(database.attachmentDao().getByCredential(credentialId.value).isEmpty())

        val restoreSource = PathBackupSource(backupPath, maximumChunkBytes = 31)
        withBackupPassword { password ->
            val restored = backupService.restoreBackup(restoreSource, password).getOrThrow()
            assertEquals(1, restored.credentialCount)
            assertEquals(1, restored.attachmentCount)
        }
        assertTrue(restoreSource.closed)
        unlockExistingVault()

        val restoredCredential = assertNotNull(credentialRepository.getById(credentialId).getOrThrow())
        try {
            val restoredAttachment = restoredCredential.attachments.single()
            val restoredEntity = database.attachmentDao().getByCredential(credentialId.value).single()
            assertEquals(originalEntity.id, restoredEntity.id)
            assertNotEquals(originalEntity.storagePath, restoredEntity.storagePath)
            val output = RecordingAttachmentSink()
            attachmentRepository.copyContentTo(credentialId, restoredAttachment.id, output).getOrThrow()
            assertContentEquals(attachmentContent, output.bytes())
            assertTrue(output.committed)
        } finally {
            restoredCredential.clearSensitiveValues()
        }
    }

    @Test
    fun `v2 backup preserves a quarantined filename and usable authenticated content`() = runTest {
        val original = database.attachmentDao().getByCredential(credentialId.value).single()
        val corruptedFilename = original.encryptedFilename.copyOf().also { bytes ->
            bytes[bytes.lastIndex] = (bytes.last().toInt() xor 1).toByte()
        }
        database.attachmentDao().update(original.copy(encryptedFilename = corruptedFilename))

        createBackup()
        credentialRepository.delete(credentialId).getOrThrow()
        withBackupPassword { password ->
            backupService.restoreBackup(PathBackupSource(backupPath), password).getOrThrow()
        }
        unlockExistingVault()

        val restoredCredential = assertNotNull(credentialRepository.getById(credentialId).getOrThrow())
        try {
            val restoredAttachment = restoredCredential.attachments.single()
            assertEquals(AttachmentAvailability.CORRUPTED_FILENAME, restoredAttachment.availability)
            assertContentEquals(
                corruptedFilename,
                database.attachmentDao().getByCredential(credentialId.value).single().encryptedFilename,
            )
            val output = RecordingAttachmentSink()
            attachmentRepository.copyContentTo(credentialId, restoredAttachment.id, output).getOrThrow()
            assertContentEquals(attachmentContent, output.bytes())
            assertTrue(output.committed)
        } finally {
            restoredCredential.clearSensitiveValues()
        }
    }

    @Test
    fun `wrong password tampering and truncation preserve the active vault and objects`() = runTest {
        createBackup()
        val originalObjectPaths = objectPaths()

        withPassword("definitely the wrong backup password") { wrong ->
            assertTrue(backupService.inspectBackup(PathBackupSource(backupPath), wrong).isFailure)
        }

        val tamperedPath = storageRoot.resolve("tampered.pvault")
        Files.copy(backupPath, tamperedPath, StandardCopyOption.REPLACE_EXISTING)
        Files.newByteChannel(
            tamperedPath,
            StandardOpenOption.READ,
            StandardOpenOption.WRITE,
        ).use { channel ->
            val position = Files.size(tamperedPath) - 9
            channel.position(position)
            val value = java.nio.ByteBuffer.allocate(1)
            channel.read(value)
            value.flip()
            value.put(0, (value.get(0).toInt() xor 1).toByte())
            channel.position(position)
            channel.write(value)
        }
        withBackupPassword { password ->
            assertTrue(backupService.restoreBackup(PathBackupSource(tamperedPath), password).isFailure)
        }
        assertActiveVaultUnchanged(originalObjectPaths)

        val truncatedPath = storageRoot.resolve("truncated.pvault")
        Files.copy(backupPath, truncatedPath, StandardCopyOption.REPLACE_EXISTING)
        Files.newByteChannel(truncatedPath, StandardOpenOption.WRITE).use { channel ->
            channel.truncate(Files.size(truncatedPath) - 37)
        }
        withBackupPassword { password ->
            assertTrue(backupService.inspectBackup(PathBackupSource(truncatedPath), password).isFailure)
        }
        assertActiveVaultUnchanged(originalObjectPaths)
    }

    @Test
    fun `metadata source change between authenticated passes rolls back and removes staged objects`() = runTest {
        createBackup()
        val alternatePath = storageRoot.resolve("alternate.pvault")
        saveCredential("Alternate valid backup")
        withBackupPassword { password ->
            backupService.createBackup(password, PathBackupSink(alternatePath)).getOrThrow()
        }
        saveCredential("Active vault sentinel")
        val expectedObjectPaths = objectPaths()
        val source = SwitchingBackupSource(backupPath, alternatePath)

        withBackupPassword { password ->
            assertTrue(backupService.restoreBackup(source, password).isFailure)
        }

        assertTrue(source.closed)
        unlockExistingVault()
        val active = assertNotNull(credentialRepository.getById(credentialId).getOrThrow())
        try {
            assertEquals("Active vault sentinel", active.title)
        } finally {
            active.clearSensitiveValues()
        }
        assertEquals(expectedObjectPaths, objectPaths())
        assertDirectoryEmpty(storageRoot.resolve("vault-files/staging"))
    }

    @Test
    fun `cancelled attachment restore closes input and removes every staged object`() = runTest {
        createBackup()
        credentialRepository.delete(credentialId).getOrThrow()
        assertTrue(objectPaths().isEmpty())
        val readSuspended = CompletableDeferred<Unit>()
        val source = SuspendingBackupSource(
            delegate = PathBackupSource(backupPath),
            suspendAfterBytes = 80_000,
            readSuspended = readSuspended,
        )

        val restore = async {
            withBackupPassword { password ->
                backupService.restoreBackup(source, password)
            }
        }
        readSuspended.await()
        restore.cancelAndJoin()

        assertTrue(source.closed)
        assertTrue(database.attachmentDao().getByCredential(credentialId.value).isEmpty())
        assertTrue(objectPaths().isEmpty())
        assertDirectoryEmpty(storageRoot.resolve("vault-files/staging"))
    }

    @Test
    fun `post commit cancellation keeps every restored attachment object`() = runTest {
        createBackup()
        credentialRepository.delete(credentialId).getOrThrow()
        assertTrue(objectPaths().isEmpty())
        val transactionCommitted = CompletableDeferred<Unit>()
        val source = PathBackupSource(backupPath)
        val magic = readMagic(source)
        val service = VaultBackupV2Service(
            backupDao = database.vaultBackupDao(),
            database = database,
            cryptoEngine = cryptoEngine,
            sessionManager = vaultRepository,
            blobStore = blobStore,
            attachmentLifecycleManager = attachmentRepository,
            newValidator = backupService::newStreamValidator,
            activateRestore = { replaceVault ->
                replaceVault()
                transactionCommitted.complete(Unit)
                awaitCancellation()
            },
        )

        val restore = async {
            withBackupPassword { password ->
                service.restoreAfterMagic(source, password, magic) {}
            }
        }
        transactionCommitted.await()
        val committedAttachment = database.attachmentDao().getByCredential(credentialId.value).single()
        assertTrue(blobStore.exists(committedAttachment.storagePath))

        restore.cancelAndJoin()

        assertTrue(source.closed)
        val restoredAttachment = database.attachmentDao().getByCredential(credentialId.value).single()
        assertEquals(committedAttachment.storagePath, restoredAttachment.storagePath)
        assertTrue(blobStore.exists(restoredAttachment.storagePath))
        assertEquals(setOf(restoredAttachment.storagePath.substringAfterLast('/')), objectPaths())
    }

    @Test
    fun `output failure aborts the candidate and oversized declared input is closed`() = runTest {
        val failingSink = FailingBackupSink()
        withBackupPassword { password ->
            assertTrue(backupService.createBackup(password, failingSink).isFailure)
        }
        assertTrue(failingSink.aborted)
        assertFalse(failingSink.committed)

        val oversized = DeclaredOversizedSource()
        withBackupPassword { password ->
            assertTrue(backupService.inspectBackup(oversized, password).isFailure)
        }
        assertTrue(oversized.closed)
        assertEquals(1, oversized.readCalls)
    }

    @Test
    fun `stream manifest rejects one entity beyond the exact global count bound`() {
        assertFails {
            backupService.newStreamValidator(
                BackupStreamManifest(
                    credentialCount = BackupLimits.MAX_ENTITY_COUNT + 1,
                    folderCount = 0,
                    tagCount = 0,
                    credentialFolderReferenceCount = 0,
                    credentialTagReferenceCount = 0,
                    attachmentCount = 0,
                    managedAttachmentCount = 0,
                    passwordHistoryCount = 0,
                ),
            )
        }
    }

    private suspend fun readMagic(source: BackupContentSource): ByteArray {
        val magic = ByteArray(BACKUP_V2_MAGIC.size)
        var offset = 0
        while (offset < magic.size) {
            val buffer = ByteArray(magic.size - offset)
            val count = source.read(buffer)
            require(count in 1..buffer.size)
            buffer.copyInto(magic, destinationOffset = offset, endIndex = count)
            offset += count
        }
        assertContentEquals(BACKUP_V2_MAGIC, magic)
        return magic
    }

    private suspend fun createBackup() {
        withBackupPassword { password ->
            backupService.createBackup(password, PathBackupSink(backupPath)).getOrThrow()
        }
    }

    private suspend fun saveCredential(title: String) {
        val credential = sampleCredential(title)
        try {
            credentialRepository.save(credential).getOrThrow()
        } finally {
            credential.clearSensitiveValues()
        }
    }

    private suspend fun assertActiveVaultUnchanged(expectedObjectPaths: Set<String>) {
        assertNotNull(credentialRepository.getById(credentialId).getOrThrow()).clearSensitiveValues()
        assertEquals(expectedObjectPaths, objectPaths())
        val attachment = database.attachmentDao().getByCredential(credentialId.value).single()
        assertTrue(
            attachmentRepository.verify(
                credentialId,
                com.passvault.core.domain.model.AttachmentId(attachment.id),
            ).isSuccess,
        )
    }

    private suspend fun unlockNewVault() {
        withMasterPassword { password ->
            vaultRepository.create(password).getOrThrow()
        }
        unlockExistingVault()
    }

    private suspend fun unlockExistingVault() {
        withMasterPassword { password ->
            vaultRepository.unlock(password).getOrThrow()
        }
    }

    private suspend fun <T> withBackupPassword(block: suspend (SensitiveText) -> T): T =
        withPassword(BACKUP_PASSWORD, block)

    private suspend fun <T> withMasterPassword(block: suspend (SensitiveText) -> T): T =
        withPassword(MASTER_PASSWORD, block)

    private suspend fun <T> withPassword(
        value: String,
        block: suspend (SensitiveText) -> T,
    ): T {
        val password = SensitiveText.from(value)
        return try {
            block(password)
        } finally {
            password.clear()
        }
    }

    private fun sampleCredential(title: String = "Streaming backup test") = Credential(
        id = credentialId,
        type = CredentialType.Login,
        title = title,
        username = null,
        email = null,
        password = SensitiveText.from("credential-secret"),
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

    private fun objectPaths(): Set<String> {
        val directory = storageRoot.resolve("vault-files/objects")
        if (!Files.exists(directory)) return emptySet()
        return Files.list(directory).use { paths -> paths.map { it.fileName.toString() }.toList().toSet() }
    }

    private fun assertDirectoryEmpty(path: Path) {
        if (!Files.exists(path)) return
        Files.list(path).use { assertEquals(0, it.count()) }
    }

    private class ByteArrayAttachmentSource(
        override val displayName: String,
        private val content: ByteArray,
    ) : AttachmentContentSource {
        override val claimedMimeType: String? = "application/octet-stream"
        override val declaredSizeBytes = content.size.toLong()
        private var offset = 0

        override suspend fun read(buffer: ByteArray): Int {
            if (offset == content.size) return -1
            val count = minOf(buffer.size, content.size - offset)
            content.copyInto(buffer, destinationOffset = 0, startIndex = offset, endIndex = offset + count)
            offset += count
            return count
        }

        override suspend fun close() = Unit
    }

    private class RecordingAttachmentSink : AttachmentContentSink {
        private val output = java.io.ByteArrayOutputStream()
        var committed = false

        override suspend fun write(buffer: ByteArray, byteCount: Int) {
            output.write(buffer, 0, byteCount)
        }

        override suspend fun commit() {
            committed = true
        }

        override suspend fun abort() {
            output.reset()
        }

        fun bytes(): ByteArray = output.toByteArray()
    }

    private class PathBackupSink(private val path: Path) : BackupContentSink {
        private var output: OutputStream? = Files.newOutputStream(path)
        var committed = false
        var aborted = false

        override suspend fun write(buffer: ByteArray, byteCount: Int) {
            require(byteCount in 0..buffer.size)
            check(!committed && !aborted)
            requireNotNull(output).write(buffer, 0, byteCount)
        }

        override suspend fun commit() {
            requireNotNull(output).close()
            output = null
            committed = true
        }

        override suspend fun abort() {
            output?.close()
            output = null
            aborted = true
            Files.deleteIfExists(path)
        }
    }

    private open class PathBackupSource(
        private val path: Path,
        private val maximumChunkBytes: Int = Int.MAX_VALUE,
    ) : BackupContentSource {
        private var input: InputStream? = Files.newInputStream(path)
        override val declaredSizeBytes = Files.size(path)
        var maximumRequestedBytes = 0
        var closed = false

        override suspend fun read(buffer: ByteArray): Int {
            maximumRequestedBytes = maxOf(maximumRequestedBytes, buffer.size)
            val requested = minOf(buffer.size, maximumChunkBytes)
            return requireNotNull(input).read(buffer, 0, requested)
        }

        override suspend fun rewind() {
            input?.close()
            input = Files.newInputStream(path)
            closed = false
        }

        override suspend fun close() {
            input?.close()
            input = null
            closed = true
        }
    }

    private class SuspendingBackupSource(
        private val delegate: PathBackupSource,
        private val suspendAfterBytes: Long,
        private val readSuspended: CompletableDeferred<Unit>,
    ) : BackupContentSource {
        override val declaredSizeBytes = delegate.declaredSizeBytes
        var closed = false
        private var consumed = 0L

        override suspend fun read(buffer: ByteArray): Int {
            if (consumed >= suspendAfterBytes) {
                readSuspended.complete(Unit)
                awaitCancellation()
            }
            return delegate.read(buffer).also { count ->
                if (count > 0) consumed += count
            }
        }

        override suspend fun rewind() {
            delegate.rewind()
            consumed = 0L
        }

        override suspend fun close() {
            delegate.close()
            closed = true
        }
    }

    private class FailingBackupSink : BackupContentSink {
        var committed = false
        var aborted = false
        private var writes = 0

        override suspend fun write(buffer: ByteArray, byteCount: Int) {
            writes++
            if (writes >= 2) error("simulated output failure")
        }

        override suspend fun commit() {
            committed = true
        }

        override suspend fun abort() {
            aborted = true
        }
    }

    private class SwitchingBackupSource(
        private val firstPath: Path,
        private val secondPath: Path,
    ) : BackupContentSource {
        override val declaredSizeBytes: Long? = null
        private var activePath = firstPath
        private var input: InputStream? = Files.newInputStream(activePath)
        var closed = false

        override suspend fun read(buffer: ByteArray): Int = requireNotNull(input).read(buffer)

        override suspend fun rewind() {
            input?.close()
            activePath = secondPath
            input = Files.newInputStream(activePath)
            closed = false
        }

        override suspend fun close() {
            input?.close()
            input = null
            closed = true
        }
    }

    private class DeclaredOversizedSource : BackupContentSource {
        override val declaredSizeBytes = BackupLimits.MAX_BACKUP_BYTES + 1
        var readCalls = 0
        var closed = false

        override suspend fun read(buffer: ByteArray): Int {
            readCalls++
            val magic = BACKUP_V2_MAGIC
            magic.copyInto(buffer)
            return magic.size
        }

        override suspend fun rewind() = Unit

        override suspend fun close() {
            closed = true
        }
    }

    private companion object {
        const val MASTER_PASSWORD = "correct horse battery staple"
        const val BACKUP_PASSWORD = "independent streaming backup password"
        const val STREAM_READ_BUFFER_BYTES = 64 * 1024
    }
}
