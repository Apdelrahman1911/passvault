package com.passvault.core.database.backup

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.passvault.core.crypto.CryptoEnvelope
import com.passvault.core.crypto.DesktopCryptoEngine
import com.passvault.core.crypto.PaddedPayload
import com.passvault.core.crypto.VaultKeyHierarchy
import com.passvault.core.database.VaultDatabase
import com.passvault.core.database.attachment.AttachmentBlobStore
import com.passvault.core.database.attachment.AttachmentLifecycleManager
import com.passvault.core.database.attachment.AttachmentRepositoryImpl
import com.passvault.core.database.attachment.LocalAttachmentBlobStore
import com.passvault.core.database.attachment.attachmentFilenameAssociatedData
import com.passvault.core.database.dao.VaultBackupDao
import com.passvault.core.database.dao.VaultBackupEntities
import com.passvault.core.database.entity.AttachmentRecordEntity
import com.passvault.core.database.entity.TagRecordEntity
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
import com.passvault.core.domain.repository.AttachmentCountLimitException
import com.passvault.core.domain.repository.AttachmentPolicy
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import okio.Buffer
import okio.BufferedSink
import okio.BufferedSource
import java.io.IOException
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
import kotlin.test.assertNull
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

@Suppress("LargeClass", "TooManyFunctions") // One fixture exercises the complete streaming backup transaction boundary.
class VaultBackupStreamingTest {
    private lateinit var database: VaultDatabase
    private lateinit var vaultRepository: VaultRepositoryImpl
    private lateinit var credentialRepository: CredentialRepositoryImpl
    private lateinit var attachmentRepository: AttachmentRepositoryImpl
    private lateinit var backupService: VaultBackupService
    private lateinit var blobStore: TrackingAttachmentBlobStore
    private lateinit var storageRoot: Path
    private lateinit var backupPath: Path
    private var reportedAvailableBytes: Long? = null
    private val queuedAvailableBytes = ArrayDeque<Long?>()
    private var capacityChecks = 0
    private var capacityFailure: Exception? = null
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
        blobStore = createTrackingBlobStore()
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
    fun `over quota legacy vault remains loadable and exportable while new imports stay blocked`() = runTest {
        val attachmentDao = database.attachmentDao()
        val original = attachmentDao.getByCredential(credentialId.value).single()
        blobStore.delete(original.storagePath)
        attachmentDao.update(
            original.copy(
                sizeBytes = AttachmentPolicy.MAX_TOTAL_SIZE_PER_CREDENTIAL_BYTES,
                contentFormatVersion = 0,
                storageState = AttachmentRecordEntity.STORAGE_STATE_LEGACY,
            ),
        )
        repeat(AttachmentPolicy.MAX_ATTACHMENTS_PER_CREDENTIAL) { index ->
            insertLegacyAttachment(index + 1)
        }

        assertEquals(
            AttachmentPolicy.MAX_ATTACHMENTS_PER_CREDENTIAL + 1,
            attachmentDao.getOccupiedSlotCount(credentialId.value),
        )
        assertEquals(0, attachmentDao.getManagedSizeBytes(credentialId.value))
        assertLegacyAttachmentCount(AttachmentPolicy.MAX_ATTACHMENTS_PER_CREDENTIAL + 1)
        assertIs<AttachmentCountLimitException>(
            attachmentRepository.import(
                credentialId,
                ByteArrayAttachmentSource("blocked.bin", byteArrayOf(1)),
            ).exceptionOrNull(),
        )

        withBackupPassword { password ->
            val created = backupService.createBackup(password, PathBackupSink(backupPath)).getOrThrow()
            assertEquals(AttachmentPolicy.MAX_ATTACHMENTS_PER_CREDENTIAL + 1, created.attachmentCount)
            val inspected = backupService.inspectBackup(PathBackupSource(backupPath), password).getOrThrow()
            assertEquals(AttachmentPolicy.MAX_ATTACHMENTS_PER_CREDENTIAL + 1, inspected.attachmentCount)
            val restored = backupService.restoreBackup(PathBackupSource(backupPath), password).getOrThrow()
            assertEquals(AttachmentPolicy.MAX_ATTACHMENTS_PER_CREDENTIAL + 1, restored.attachmentCount)
        }

        unlockExistingVault()
        assertLegacyAttachmentCount(AttachmentPolicy.MAX_ATTACHMENTS_PER_CREDENTIAL + 1)
        assertIs<AttachmentCountLimitException>(
            attachmentRepository.import(
                credentialId,
                ByteArrayAttachmentSource("still-blocked.bin", byteArrayOf(2)),
            ).exceptionOrNull(),
        )
    }

    @Test
    fun `restore rejects insufficient capacity before staging any attachment`() = runTest {
        createBackup()
        val originalObjectPaths = objectPaths()
        val encryptedBytes = storedObjectBytesOnDisk()
        reportedAvailableBytes = minimumRestoreAvailableBytes(encryptedBytes) - 1L
        blobStore.resetWriteTracking()

        val result = withBackupPassword { password ->
            backupService.restoreBackup(PathBackupSource(backupPath), password)
        }

        val failure = assertIs<BackupInsufficientStorageException>(result.exceptionOrNull())
        assertEquals(reportedAvailableBytes, failure.availableBytes)
        assertEquals(minimumRestoreAvailableBytes(encryptedBytes), failure.requiredBytes)
        assertEquals(0, blobStore.writeCalls)
        assertActiveVaultUnchanged(originalObjectPaths)
        assertDirectoryEmpty(storageRoot.resolve("vault-files/staging"))
    }

    @Test
    fun `repeated legacy restores remove retired objects inside the attachment stability scope`() = runTest {
        val legacyBackup = withBackupPassword { password ->
            backupService.createBackup(password).getOrThrow()
        }
        val lifecycleManager = TrackingAttachmentLifecycleManager(attachmentRepository)
        val scopedService = createBackupService(attachmentLifecycleManager = lifecycleManager)
        val cleanupScopes = mutableListOf<Boolean>()
        blobStore.onCleanup = { cleanupScopes += lifecycleManager.inStableScope }
        try {
            repeat(3) { restoreIndex ->
                if (restoreIndex > 0) {
                    unlockExistingVault()
                    attachmentRepository.import(
                        credentialId,
                        ByteArrayAttachmentSource("evidence-$restoreIndex.bin", attachmentContent),
                    ).getOrThrow()
                }
                assertTrue(database.attachmentDao().getByCredential(credentialId.value).isNotEmpty())
                assertTrue(objectPaths().isNotEmpty())

                withBackupPassword { password ->
                    scopedService.restoreBackup(legacyBackup, password).getOrThrow()
                }

                assertTrue(database.attachmentDao().getByCredential(credentialId.value).isEmpty())
                assertTrue(objectPaths().isEmpty())
                assertDirectoryEmpty(storageRoot.resolve("vault-files/staging"))
            }
            assertEquals(listOf(true, true, true), cleanupScopes)
        } finally {
            blobStore.onCleanup = null
            cryptoEngine.secureWipe(legacyBackup)
        }
    }

    @Test
    fun `failed legacy Room replacement preserves attachment rows and objects`() = runTest {
        val legacyBackup = withBackupPassword { password ->
            backupService.createBackup(password).getOrThrow()
        }
        val expectedObjectPaths = objectPaths()
        val failingService = createBackupService(
            backupDao = TransactionFailingBackupDao(database.vaultBackupDao()),
        )
        blobStore.resetCleanupTracking()
        try {
            withBackupPassword { password ->
                assertTrue(failingService.restoreBackup(legacyBackup, password).isFailure)
            }

            assertTrue(database.attachmentDao().getByCredential(credentialId.value).isNotEmpty())
            assertEquals(expectedObjectPaths, objectPaths())
            assertEquals(0, blobStore.cleanupCalls)
        } finally {
            cryptoEngine.secureWipe(legacyBackup)
        }
    }

    @Test
    fun `cancellation after legacy commit cannot skip object cleanup`() = runTest {
        val legacyBackup = withBackupPassword { password ->
            backupService.createBackup(password).getOrThrow()
        }
        val cleanupStarted = CompletableDeferred<Unit>()
        val allowCleanup = CompletableDeferred<Unit>()
        blobStore.resetCleanupTracking()
        blobStore.cleanupStarted = cleanupStarted
        blobStore.allowCleanup = allowCleanup
        val restore = async {
            withBackupPassword { password ->
                backupService.restoreBackup(legacyBackup, password)
            }
        }
        try {
            cleanupStarted.await()
            assertTrue(database.attachmentDao().getByCredential(credentialId.value).isEmpty())
            assertTrue(objectPaths().isNotEmpty())

            restore.cancel()
            allowCleanup.complete(Unit)
            restore.join()

            assertTrue(restore.isCancelled)
            assertEquals(1, blobStore.cleanupCalls)
            assertTrue(objectPaths().isEmpty())
            assertDirectoryEmpty(storageRoot.resolve("vault-files/staging"))
        } finally {
            allowCleanup.complete(Unit)
            restore.cancelAndJoin()
            cryptoEngine.secureWipe(legacyBackup)
        }
    }

    @Test
    fun `legacy cleanup failure preserves restore success and surfaces a warning`() = runTest {
        val legacyBackup = withBackupPassword { password ->
            backupService.createBackup(password).getOrThrow()
        }
        val expectedObjectPaths = objectPaths()
        blobStore.resetCleanupTracking()
        blobStore.cleanupFailure = IOException("simulated cleanup failure")
        try {
            val restored = withBackupPassword { password ->
                backupService.restoreBackup(legacyBackup, password).getOrThrow()
            }

            assertTrue(database.attachmentDao().getByCredential(credentialId.value).isEmpty())
            assertEquals(expectedObjectPaths, objectPaths())
            assertEquals(1, blobStore.cleanupCalls)
            assertTrue(
                VaultBackupService.BackupWarning.OBSOLETE_ATTACHMENT_CLEANUP_FAILED in restored.warnings,
            )
        } finally {
            blobStore.cleanupFailure = null
            cryptoEngine.secureWipe(legacyBackup)
        }
    }

    @Test
    fun `v2 restore retains only objects referenced by committed rows`() = runTest {
        createBackup()
        val originalPath = database.attachmentDao().getByCredential(credentialId.value).single().storagePath
        blobStore.resetCleanupTracking()

        withBackupPassword { password ->
            backupService.restoreBackup(PathBackupSource(backupPath), password).getOrThrow()
        }

        val restoredPath = database.attachmentDao().getByCredential(credentialId.value).single().storagePath
        assertNotEquals(originalPath, restoredPath)
        assertFalse(blobStore.exists(originalPath))
        assertTrue(blobStore.exists(restoredPath))
        assertEquals(setOf(restoredPath.substringAfterLast('/')), objectPaths())
        assertEquals(1, blobStore.cleanupCalls)
    }

    @Test
    fun `restore accepts the exact capacity boundary`() = runTest {
        createBackup()
        val encryptedBytes = storedObjectBytesOnDisk()
        reportedAvailableBytes = minimumRestoreAvailableBytes(encryptedBytes)
        blobStore.resetWriteTracking()

        withBackupPassword { password ->
            backupService.restoreBackup(PathBackupSource(backupPath), password).getOrThrow()
        }

        assertEquals(1, blobStore.writeCalls)
        unlockExistingVault()
        assertNotNull(credentialRepository.getById(credentialId).getOrThrow()).clearSensitiveValues()
    }

    @Test
    fun `restore continues when filesystem capacity is unavailable`() = runTest {
        createBackup()
        credentialRepository.delete(credentialId).getOrThrow()
        reportedAvailableBytes = null
        blobStore.resetWriteTracking()

        withBackupPassword { password ->
            backupService.restoreBackup(PathBackupSource(backupPath), password).getOrThrow()
        }

        assertTrue(capacityChecks >= 2)
        assertEquals(1, blobStore.writeCalls)
    }

    @Test
    fun `restore continues when capacity query fails or reports an invalid value`() = runTest {
        createBackup()
        credentialRepository.delete(credentialId).getOrThrow()
        capacityFailure = IOException("simulated capacity query failure")

        withBackupPassword { password ->
            backupService.restoreBackup(PathBackupSource(backupPath), password).getOrThrow()
        }

        unlockExistingVault()
        credentialRepository.delete(credentialId).getOrThrow()
        capacityFailure = null
        reportedAvailableBytes = -1L
        blobStore.resetWriteTracking()

        withBackupPassword { password ->
            backupService.restoreBackup(PathBackupSource(backupPath), password).getOrThrow()
        }

        assertTrue(capacityChecks >= 4)
        assertEquals(1, blobStore.writeCalls)
    }

    @Test
    fun `capacity accounting preserves variable chunk attachment compatibility`() = runTest {
        val shortReadContent = ByteArray(4_097) { index -> (index % 197).toByte() }
        attachmentRepository.import(
            credentialId,
            TrickleAttachmentSource("short-reads.bin", shortReadContent, maximumReadBytes = 2),
        ).getOrThrow()
        createBackup()
        reportedAvailableBytes = minimumRestoreAvailableBytes(storedObjectBytesOnDisk())
        credentialRepository.delete(credentialId).getOrThrow()

        withBackupPassword { password ->
            backupService.restoreBackup(PathBackupSource(backupPath), password).getOrThrow()
        }

        unlockExistingVault()
        val attachments = database.attachmentDao().getByCredential(credentialId.value)
        assertEquals(2, attachments.size)
        attachments.forEach { attachment ->
            assertTrue(
                attachmentRepository.verify(
                    credentialId,
                    com.passvault.core.domain.model.AttachmentId(attachment.id),
                ).isSuccess,
            )
        }
    }

    @Test
    fun `restore remains compatible with metadata schema 2 attachment backups`() = runTest {
        createBackup()
        rewriteBackupManifest { manifest ->
            manifest.copy(
                metadataSchemaVersion = PRE_STORAGE_ACCOUNTING_METADATA_SCHEMA_VERSION,
                managedAttachmentObjectBytes = null,
            )
        }
        credentialRepository.delete(credentialId).getOrThrow()
        reportedAvailableBytes = Long.MAX_VALUE
        blobStore.resetWriteTracking()

        withBackupPassword { password ->
            backupService.restoreBackup(PathBackupSource(backupPath), password).getOrThrow()
        }

        assertEquals(1, blobStore.writeCalls)
        assertTrue(capacityChecks >= 1)
        unlockExistingVault()
        assertNotNull(credentialRepository.getById(credentialId).getOrThrow()).clearSensitiveValues()
    }

    @Test
    fun `restore rejects an authenticated underreported object total before staging`() = runTest {
        createBackup()
        rewriteBackupManifest { manifest ->
            manifest.copy(managedAttachmentObjectBytes = requireNotNull(manifest.managedAttachmentObjectBytes) - 1L)
        }
        val originalObjectPaths = objectPaths()
        blobStore.resetWriteTracking()

        val result = withBackupPassword { password ->
            backupService.restoreBackup(PathBackupSource(backupPath), password)
        }

        assertTrue(result.isFailure)
        assertEquals(0, blobStore.writeCalls)
        assertActiveVaultUnchanged(originalObjectPaths)
        assertDirectoryEmpty(storageRoot.resolve("vault-files/staging"))
    }

    @Test
    fun `restore rejects an authenticated overreported object total and removes staged data`() = runTest {
        createBackup()
        rewriteBackupManifest { manifest ->
            manifest.copy(managedAttachmentObjectBytes = requireNotNull(manifest.managedAttachmentObjectBytes) + 1L)
        }
        val originalObjectPaths = objectPaths()
        blobStore.resetWriteTracking()

        val result = withBackupPassword { password ->
            backupService.restoreBackup(PathBackupSource(backupPath), password)
        }

        assertTrue(result.isFailure)
        assertEquals(1, blobStore.writeCalls)
        assertActiveVaultUnchanged(originalObjectPaths)
        assertDirectoryEmpty(storageRoot.resolve("vault-files/staging"))
    }

    @Test
    fun `export aborts if an object changes after the manifest size scan`() = runTest {
        val sink = PathBackupSink(backupPath)
        blobStore.resetWriteTracking()
        blobStore.adjustSizeOnReadCall = 1
        blobStore.reportedSizeAdjustment = -1L

        val result = withBackupPassword { password -> backupService.createBackup(password, sink) }

        assertTrue(result.isFailure)
        assertFalse(sink.committed)
        assertTrue(sink.aborted)
        assertFalse(Files.exists(backupPath))
    }

    @Test
    fun `shrinking capacity aborts before the next object and cleans staged data`() = runTest {
        val secondContent = ByteArray(17_321) { index -> (index % 199).toByte() }
        attachmentRepository.import(
            credentialId,
            ByteArrayAttachmentSource("second.bin", secondContent),
        ).getOrThrow()
        createBackup()
        val originalObjectPaths = objectPaths()
        val objectSizes = database.attachmentDao().getByCredential(credentialId.value)
            .sortedBy { it.id }
            .map { attachment ->
                Files.size(storageRoot.resolve("vault-files").resolve(attachment.storagePath))
            }
        val totalBytes = objectSizes.sum()
        queuedAvailableBytes.addLast(minimumRestoreAvailableBytes(totalBytes))
        queuedAvailableBytes.addLast(minimumRestoreAvailableBytes(totalBytes))
        queuedAvailableBytes.addLast(minimumRestoreAvailableBytes(objectSizes.last()) - 1L)
        blobStore.resetWriteTracking()

        val result = withBackupPassword { password ->
            backupService.restoreBackup(PathBackupSource(backupPath), password)
        }

        assertIs<BackupInsufficientStorageException>(result.exceptionOrNull())
        assertEquals(1, blobStore.writeCalls)
        assertActiveVaultUnchanged(originalObjectPaths)
        assertDirectoryEmpty(storageRoot.resolve("vault-files/staging"))
    }

    @Test
    fun `storage full during staging is actionable and cleans every staged object`() = runTest {
        attachmentRepository.import(
            credentialId,
            ByteArrayAttachmentSource("second.bin", byteArrayOf(7, 8, 9)),
        ).getOrThrow()
        createBackup()
        val originalObjectPaths = objectPaths()
        listOf("No space left on device", "Disc quota exceeded").forEach { message ->
            blobStore.resetWriteTracking()
            blobStore.storageFullOnWriteCall = 2
            blobStore.storageFullMessage = message

            val result = withBackupPassword { password ->
                backupService.restoreBackup(PathBackupSource(backupPath), password)
            }

            val failure = assertIs<BackupInsufficientStorageException>(result.exceptionOrNull())
            assertNull(failure.requiredBytes)
            assertEquals(2, blobStore.writeCalls)
            assertActiveVaultUnchanged(originalObjectPaths)
            assertDirectoryEmpty(storageRoot.resolve("vault-files/staging"))
        }
    }

    @Test
    fun `cancellation after object publication removes the registered staged object`() = runTest {
        createBackup()
        val originalObjectPaths = objectPaths()
        val published = CompletableDeferred<Unit>()
        blobStore.resetWriteTracking()
        blobStore.suspendAfterWriteCall = 1
        blobStore.publishedWrite = published
        val source = PathBackupSource(backupPath)

        val restore = async {
            withBackupPassword { password -> backupService.restoreBackup(source, password) }
        }
        published.await()
        restore.cancelAndJoin()

        assertTrue(source.closed)
        assertActiveVaultUnchanged(originalObjectPaths)
        assertDirectoryEmpty(storageRoot.resolve("vault-files/staging"))
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
            activateRestore = { _, replaceVault ->
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
                    managedAttachmentObjectBytes = 0L,
                ),
            )
        }
    }

    @Test
    fun `stream validator rejects identifier text beyond its retained byte budget`() {
        val validator = backupService.newStreamValidator(
            manifest = BackupStreamManifest(
                credentialCount = 0,
                folderCount = 0,
                tagCount = 1,
                credentialFolderReferenceCount = 0,
                credentialTagReferenceCount = 0,
                attachmentCount = 0,
                managedAttachmentCount = 0,
                passwordHistoryCount = 0,
                managedAttachmentObjectBytes = 0L,
            ),
            retainedIdentifierBytes = 3L,
        )

        assertFailsWith<IllegalArgumentException> {
            validator.accept(
                BackupMetadataValue.Tag(
                    TagRecordEntity(
                        id = "four",
                        nameHash = ByteArray(0),
                        encryptedPayload = ByteArray(0),
                        payloadNonce = ByteArray(0),
                        color = null,
                        createdAt = 0L,
                    ),
                ),
            )
        }
    }

    @Test
    fun `stream manifest rejects a managed attachment without object bytes`() {
        assertFails {
            backupService.newStreamValidator(
                BackupStreamManifest(
                    credentialCount = 1,
                    folderCount = 0,
                    tagCount = 0,
                    credentialFolderReferenceCount = 0,
                    credentialTagReferenceCount = 0,
                    attachmentCount = 1,
                    managedAttachmentCount = 1,
                    passwordHistoryCount = 0,
                    managedAttachmentObjectBytes = 0L,
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

    private fun createBackupService(
        backupDao: VaultBackupDao = database.vaultBackupDao(),
        attachmentLifecycleManager: AttachmentLifecycleManager = attachmentRepository,
    ) = VaultBackupService(
        backupDao = backupDao,
        database = database,
        cryptoEngine = cryptoEngine,
        vaultRepository = vaultRepository,
        sessionManager = vaultRepository,
        attachmentBlobStore = blobStore,
        attachmentLifecycleManager = attachmentLifecycleManager,
    )

    private suspend fun rewriteBackupManifest(
        transform: (BackupStreamManifest) -> BackupStreamManifest,
    ) {
        val rewrittenPath = storageRoot.resolve("rewritten.pvault")
        withBackupPassword { password ->
            val source = PathBackupSource(backupPath)
            val magic = readMagic(source)
            val reader = BackupV2Reader.createAfterMagic(source, password, cryptoEngine, magic)
            val sink = PathBackupSink(rewrittenPath)
            val writer = BackupV2Writer.create(sink, password, cryptoEngine)
            var committed = false
            try {
                val manifestRecord = reader.readRecord(BackupRecordType.MANIFEST)
                val originalManifest = BackupEntityBinaryCodec.decodeManifest(manifestRecord.plaintext)
                val rewrittenManifest = BackupEntityBinaryCodec.encodeManifest(transform(originalManifest))
                try {
                    writer.writeRecord(BackupRecordType.MANIFEST, rewrittenManifest)
                } finally {
                    cryptoEngine.secureWipe(manifestRecord.plaintext)
                    cryptoEngine.secureWipe(rewrittenManifest)
                }

                copyBackupBody(reader, writer, originalManifest)
                reader.requireExhausted()
                sink.commit()
                committed = true
            } finally {
                reader.close()
                writer.clear()
                if (!committed) sink.abort()
            }
        }
        Files.move(rewrittenPath, backupPath, StandardCopyOption.REPLACE_EXISTING)
    }

    private suspend fun copyBackupBody(
        reader: BackupV2Reader,
        writer: BackupV2Writer,
        manifest: BackupStreamManifest,
    ) {
        copyRecords(reader, writer, BackupRecordType.METADATA, 1)
        copyRecords(reader, writer, BackupRecordType.FOLDER, manifest.folderCount)
        copyRecords(reader, writer, BackupRecordType.TAG, manifest.tagCount)
        copyRecords(reader, writer, BackupRecordType.CREDENTIAL, manifest.credentialCount)
        copyRecords(
            reader,
            writer,
            BackupRecordType.CREDENTIAL_FOLDER_REFERENCE,
            manifest.credentialFolderReferenceCount,
        )
        copyRecords(
            reader,
            writer,
            BackupRecordType.CREDENTIAL_TAG_REFERENCE,
            manifest.credentialTagReferenceCount,
        )
        copyRecords(reader, writer, BackupRecordType.ATTACHMENT, manifest.attachmentCount)
        copyRecords(reader, writer, BackupRecordType.PASSWORD_HISTORY, manifest.passwordHistoryCount)
        copyRecord(reader, writer, BackupRecordType.METADATA_END)
        repeat(manifest.managedAttachmentCount) { copyAttachmentRecords(reader, writer) }
        copyRecord(reader, writer, BackupRecordType.FINAL)
    }

    private suspend fun copyAttachmentRecords(reader: BackupV2Reader, writer: BackupV2Writer) {
        var encryptedObjectBytes = 0L
        copyRecord(reader, writer, BackupRecordType.ATTACHMENT_START) { plaintext ->
            encryptedObjectBytes = readAttachmentObjectBytes(plaintext)
        }
        val budget = AttachmentContentRecordBudget(encryptedObjectBytes)
        while (!budget.isComplete) {
            budget.requireRecordAvailable()
            copyRecord(reader, writer, BackupRecordType.ATTACHMENT_CONTENT) { plaintext ->
                budget.accept(plaintext.size)
            }
        }
        copyRecord(reader, writer, BackupRecordType.ATTACHMENT_END)
    }

    private suspend fun copyRecords(
        reader: BackupV2Reader,
        writer: BackupV2Writer,
        type: Int,
        count: Int,
    ) {
        repeat(count) { copyRecord(reader, writer, type) }
    }

    private suspend fun copyRecord(
        reader: BackupV2Reader,
        writer: BackupV2Writer,
        type: Int,
        inspect: (ByteArray) -> Unit = {},
    ) {
        val record = reader.readRecord(type)
        try {
            inspect(record.plaintext)
            writer.writeRecord(type, record.plaintext)
        } finally {
            cryptoEngine.secureWipe(record.plaintext)
        }
    }

    private fun readAttachmentObjectBytes(plaintext: ByteArray): Long {
        val source = Buffer().write(plaintext)
        val identifierBytes = source.readInt()
        require(identifierBytes in 1..MAX_BACKUP_IDENTIFIER_UTF8_BYTES)
        source.skip(identifierBytes.toLong())
        val objectBytes = source.readLong()
        require(objectBytes > 0L && source.exhausted())
        return objectBytes
    }

    private fun createTrackingBlobStore() = TrackingAttachmentBlobStore(
        LocalAttachmentBlobStore(
            rootPath = storageRoot.resolve("vault-files").toString(),
            availableBytesProvider = {
                capacityChecks++
                capacityFailure?.let { throw it }
                if (queuedAvailableBytes.isEmpty()) {
                    reportedAvailableBytes
                } else {
                    queuedAvailableBytes.removeFirst()
                }
            },
        ),
    )

    private suspend fun saveCredential(title: String) {
        val credential = sampleCredential(title)
        try {
            credentialRepository.save(credential).getOrThrow()
        } finally {
            credential.clearSensitiveValues()
        }
    }

    private suspend fun insertLegacyAttachment(index: Int) {
        val attachmentId = "legacy-attachment-$index"
        val keyContext = "legacy-context-$index"
        val plaintext = "legacy-$index.bin".encodeToByteArray()
        val associatedData = attachmentFilenameAssociatedData(attachmentId, credentialId.value)
        val vek = vaultRepository.withUnlockedSession { it.copyOf() }
        var key: ByteArray? = null
        try {
            key = cryptoEngine.deriveSubkey(vek, "attachment:$keyContext", 32).getOrThrow()
            val encrypted = PaddedPayload.encrypt(
                cryptoEngine = cryptoEngine,
                plaintext = plaintext,
                key = key,
                associatedData = associatedData,
                maxPlaintextBytes = AttachmentPolicy.MAX_FILE_NAME_CODE_POINTS * 4,
            ).getOrThrow()
            try {
                database.attachmentDao().insert(
                    AttachmentRecordEntity(
                        id = attachmentId,
                        credentialId = credentialId.value,
                        encryptedFilename = CryptoEnvelope.encode(encrypted),
                        filenameNonce = encrypted.nonce.copyOf(),
                        mimeType = "application/octet-stream",
                        sizeBytes = 1,
                        storagePath = "attachments/legacy-$index.enc",
                        keyDerivationContext = keyContext,
                        createdAt = index.toLong(),
                        contentFormatVersion = 0,
                        storageState = AttachmentRecordEntity.STORAGE_STATE_LEGACY,
                    ),
                )
            } finally {
                encrypted.clear()
            }
        } finally {
            cryptoEngine.secureWipe(plaintext)
            cryptoEngine.secureWipe(associatedData)
            key?.let(cryptoEngine::secureWipe)
            cryptoEngine.secureWipe(vek)
        }
    }

    private suspend fun assertLegacyAttachmentCount(expected: Int) {
        val credential = assertNotNull(credentialRepository.getById(credentialId).getOrThrow())
        try {
            assertEquals(expected, credential.attachments.size)
            assertTrue(credential.attachments.all { it.availability == AttachmentAvailability.LEGACY_METADATA_ONLY })
        } finally {
            credential.clearSensitiveValues()
        }
    }

    private suspend fun assertActiveVaultUnchanged(expectedObjectPaths: Set<String>) {
        assertNotNull(credentialRepository.getById(credentialId).getOrThrow()).clearSensitiveValues()
        assertEquals(expectedObjectPaths, objectPaths())
        val attachments = database.attachmentDao().getByCredential(credentialId.value)
        assertTrue(attachments.isNotEmpty())
        attachments.forEach { attachment ->
            assertTrue(
                attachmentRepository.verify(
                    credentialId,
                    com.passvault.core.domain.model.AttachmentId(attachment.id),
                ).isSuccess,
            )
        }
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

    private fun storedObjectBytesOnDisk(): Long {
        val directory = storageRoot.resolve("vault-files/objects")
        if (!Files.exists(directory)) return 0L
        return Files.list(directory).use { paths -> paths.mapToLong(Files::size).sum() }
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

    private class TrickleAttachmentSource(
        override val displayName: String,
        private val content: ByteArray,
        private val maximumReadBytes: Int,
    ) : AttachmentContentSource {
        override val claimedMimeType: String? = "application/octet-stream"
        override val declaredSizeBytes = content.size.toLong()
        private var offset = 0

        override suspend fun read(buffer: ByteArray): Int {
            if (offset == content.size) return -1
            val count = minOf(buffer.size, maximumReadBytes, content.size - offset)
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

    private class TransactionFailingBackupDao(
        private val delegate: VaultBackupDao,
    ) : VaultBackupDao by delegate {
        override suspend fun replaceVault(snapshot: VaultBackupEntities) {
            delegate.replaceVault(
                snapshot.copy(credentials = snapshot.credentials + snapshot.credentials.single()),
            )
        }
    }

    private class TrackingAttachmentLifecycleManager(
        private val delegate: AttachmentLifecycleManager,
    ) : AttachmentLifecycleManager by delegate {
        var inStableScope = false
            private set

        override suspend fun <T> withStableAttachments(block: suspend () -> T): T =
            delegate.withStableAttachments {
                check(!inStableScope)
                inStableScope = true
                try {
                    block()
                } finally {
                    inStableScope = false
                }
            }
    }

    private class TrackingAttachmentBlobStore(
        private val delegate: AttachmentBlobStore,
    ) : AttachmentBlobStore {
        var writeCalls = 0
            private set
        var storageFullOnWriteCall: Int? = null
        var storageFullMessage = "No space left on device"
        var suspendAfterWriteCall: Int? = null
        var publishedWrite: CompletableDeferred<Unit>? = null
        var adjustSizeOnReadCall: Int? = null
        var reportedSizeAdjustment = 0L
        var cleanupCalls = 0
            private set
        var cleanupFailure: Exception? = null
        var cleanupStarted: CompletableDeferred<Unit>? = null
        var allowCleanup: CompletableDeferred<Unit>? = null
        var onCleanup: (() -> Unit)? = null
        private var readCalls = 0

        override suspend fun availableBytes(): Long? = delegate.availableBytes()

        override suspend fun <T> writeAtomically(
            relativePath: String,
            writer: suspend (BufferedSink) -> T,
        ): T {
            writeCalls++
            val call = writeCalls
            val result = if (call == storageFullOnWriteCall) {
                delegate.writeAtomically(relativePath) {
                    throw IOException(storageFullMessage)
                }
            } else {
                delegate.writeAtomically(relativePath, writer)
            }
            if (call == suspendAfterWriteCall) {
                publishedWrite?.complete(Unit)
                awaitCancellation()
            }
            return result
        }

        override suspend fun <T> read(
            relativePath: String,
            maxBytes: Long,
            reader: suspend (BufferedSource, Long) -> T,
        ): T {
            readCalls++
            val call = readCalls
            return delegate.read(relativePath, maxBytes) { source, size ->
                val reportedSize = if (call == adjustSizeOnReadCall) size + reportedSizeAdjustment else size
                reader(source, reportedSize)
            }
        }

        override suspend fun delete(relativePath: String) = delegate.delete(relativePath)

        override suspend fun exists(relativePath: String): Boolean = delegate.exists(relativePath)

        override suspend fun removeUnreferencedObjects(referencedPaths: Set<String>) {
            cleanupCalls++
            cleanupStarted?.complete(Unit)
            allowCleanup?.await()
            onCleanup?.invoke()
            cleanupFailure?.let { throw it }
            delegate.removeUnreferencedObjects(referencedPaths)
        }

        fun resetCleanupTracking() {
            cleanupCalls = 0
            cleanupFailure = null
            cleanupStarted = null
            allowCleanup = null
            onCleanup = null
        }

        fun resetWriteTracking() {
            writeCalls = 0
            storageFullOnWriteCall = null
            storageFullMessage = "No space left on device"
            suspendAfterWriteCall = null
            publishedWrite = null
            adjustSizeOnReadCall = null
            reportedSizeAdjustment = 0L
            readCalls = 0
        }
    }

    private companion object {
        const val MAX_BACKUP_IDENTIFIER_UTF8_BYTES = 256 * 4
        const val MASTER_PASSWORD = "correct horse battery staple"
        const val BACKUP_PASSWORD = "independent streaming backup password"
        const val STREAM_READ_BUFFER_BYTES = 64 * 1024
    }
}
