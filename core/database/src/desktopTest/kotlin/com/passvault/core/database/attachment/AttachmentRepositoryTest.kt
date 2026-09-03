package com.passvault.core.database.attachment

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.passvault.core.crypto.DesktopCryptoEngine
import com.passvault.core.crypto.CryptoEnvelope
import com.passvault.core.crypto.PaddedPayload
import com.passvault.core.crypto.VaultKeyHierarchy
import com.passvault.core.database.VaultDatabase
import com.passvault.core.database.entity.AttachmentRecordEntity
import com.passvault.core.database.repository.CredentialRepositoryImpl
import com.passvault.core.database.repository.VaultRepositoryImpl
import com.passvault.core.domain.model.AttachmentAvailability
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
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
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Suppress(
    "LargeClass",
    "TooManyFunctions",
) // Cohesive filesystem/database security fixture shares one expensive vault setup.
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
    fun `attachment filenames use padded buckets and remain readable after rename`() = runTest {
        val first = attachmentRepository.import(
            credentialId,
            ByteArraySource("a.txt", null, byteArrayOf(1)),
        ).getOrThrow()
        val second = attachmentRepository.import(
            credentialId,
            ByteArraySource("twelve12.txt", null, byteArrayOf(2)),
        ).getOrThrow()
        val firstEntity = requireEntity(first.id)
        val secondEntity = requireEntity(second.id)

        assertTrue(CryptoEnvelope.isPaddedPayload(firstEntity.encryptedFilename))
        assertTrue(CryptoEnvelope.isPaddedPayload(secondEntity.encryptedFilename))
        assertEquals(firstEntity.encryptedFilename.size, secondEntity.encryptedFilename.size)

        val renamed = attachmentRepository.rename(credentialId, first.id, "renamed.txt").getOrThrow()
        assertEquals("renamed.txt", renamed.fileName)
        assertTrue(CryptoEnvelope.isPaddedPayload(requireEntity(first.id).encryptedFilename))
    }

    @Test
    fun `filename ciphertext cannot be replayed across independently keyed attachments`() = runTest {
        val first = attachmentRepository.import(
            credentialId,
            ByteArraySource("first.txt", null, byteArrayOf(1)),
        ).getOrThrow()
        val second = attachmentRepository.import(
            credentialId,
            ByteArraySource("second.txt", null, byteArrayOf(2)),
        ).getOrThrow()
        val firstEntity = requireEntity(first.id)
        val secondEntity = requireEntity(second.id)

        database.attachmentDao().update(
            secondEntity.copy(
                encryptedFilename = firstEntity.encryptedFilename.copyOf(),
                filenameNonce = firstEntity.filenameNonce.copyOf(),
            ),
        )

        val loaded = assertNotNull(credentialRepository.getById(credentialId).getOrThrow())
        assertEquals(
            AttachmentAvailability.CORRUPTED_FILENAME,
            loaded.attachments.first { it.id == second.id }.availability,
        )
        assertEquals(
            AttachmentAvailability.AVAILABLE,
            loaded.attachments.first { it.id == first.id }.availability,
        )
    }

    @Test
    fun `legacy attachment filename remains readable and is padded on rename`() = runTest {
        val attachment = attachmentRepository.import(
            credentialId,
            ByteArraySource("legacy.txt", null, byteArrayOf(1)),
        ).getOrThrow()
        val stored = requireEntity(attachment.id)
        val vek = vaultRepository.withUnlockedSession { it.copyOf() }
        var key: ByteArray? = null
        var plaintext: ByteArray? = null
        val aad = "passvault:attachment:${stored.id}:${stored.credentialId}:filename:v1".encodeToByteArray()
        try {
            key = cryptoEngine.deriveSubkey(vek, "attachment:${stored.keyDerivationContext}", 32).getOrThrow()
            plaintext = PaddedPayload.decrypt(
                cryptoEngine,
                stored.encryptedFilename,
                stored.filenameNonce,
                key,
                aad,
                AttachmentPolicy.MAX_FILE_NAME_CODE_POINTS * 4,
            ).getOrThrow()
            val legacy = cryptoEngine.encrypt(plaintext, key, aad).getOrThrow()
            try {
                database.attachmentDao().update(
                    stored.copy(
                        encryptedFilename = CryptoEnvelope.encode(legacy),
                        filenameNonce = legacy.nonce.copyOf(),
                    ),
                )
            } finally {
                legacy.clear()
            }
        } finally {
            plaintext?.let(cryptoEngine::secureWipe)
            key?.let(cryptoEngine::secureWipe)
            cryptoEngine.secureWipe(aad)
            cryptoEngine.secureWipe(vek)
        }

        val renamed = attachmentRepository.rename(credentialId, attachment.id, "modern.txt").getOrThrow()
        assertEquals("modern.txt", renamed.fileName)
        assertTrue(CryptoEnvelope.isPaddedPayload(requireEntity(attachment.id).encryptedFilename))
    }

    @Test
    fun `short first reads are accumulated before MIME detection`() = runTest {
        val source = TrickleSource(
            name = "short-read.pdf",
            content = "%PDF-1.7\nattachment".encodeToByteArray(),
            maximumReadBytes = 2,
        )

        val attachment = attachmentRepository.import(credentialId, source).getOrThrow()

        assertEquals("application/pdf", attachment.mimeType)
        assertTrue(attachmentRepository.verify(credentialId, attachment.id).isSuccess)
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
    fun `corrupt filename is quarantined without blocking sibling import rename repair or delete`() = runTest {
        val healthy = attachmentRepository.import(
            credentialId,
            ByteArraySource("healthy.txt", null, byteArrayOf(1)),
        ).getOrThrow()
        val corrupted = attachmentRepository.import(
            credentialId,
            ByteArraySource("corrupted.txt", null, byteArrayOf(2)),
        ).getOrThrow()
        corruptFilenameCiphertext(corrupted.id)

        val loaded = assertNotNull(credentialRepository.getById(credentialId).getOrThrow())
        val quarantined = assertNotNull(loaded.attachments.firstOrNull { it.id == corrupted.id })
        assertEquals(AttachmentAvailability.CORRUPTED_FILENAME, quarantined.availability)
        assertTrue(quarantined.fileName.contains(corrupted.id.value))

        val imported = attachmentRepository.import(
            credentialId,
            ByteArraySource("healthy.txt", null, byteArrayOf(3)),
        ).getOrThrow()
        assertEquals("healthy (2).txt", imported.fileName)

        val renamed = attachmentRepository.rename(credentialId, healthy.id, "renamed.txt").getOrThrow()
        assertEquals("renamed.txt", renamed.fileName)

        val repaired = attachmentRepository.rename(credentialId, corrupted.id, "repaired.txt").getOrThrow()
        assertEquals("repaired.txt", repaired.fileName)
        assertEquals(
            AttachmentAvailability.AVAILABLE,
            assertNotNull(credentialRepository.getById(credentialId).getOrThrow()).attachments
                .first { it.id == corrupted.id }
                .availability,
        )

        corruptFilenameNonce(corrupted.id)
        assertTrue(attachmentRepository.delete(credentialId, corrupted.id).isSuccess)
        assertNull(database.attachmentDao().getById(corrupted.id.value, credentialId.value))
    }

    @Test
    fun `corrupt legacy filename is quarantined and does not block import or rename`() = runTest {
        val legacy = attachmentRepository.import(
            credentialId,
            ByteArraySource("legacy.txt", null, byteArrayOf(1)),
        ).getOrThrow()
        val healthy = attachmentRepository.import(
            credentialId,
            ByteArraySource("healthy.txt", null, byteArrayOf(2)),
        ).getOrThrow()
        val stored = requireEntity(legacy.id)
        val corruptedCiphertext = stored.encryptedFilename.copyOf().also { bytes ->
            bytes[bytes.lastIndex] = (bytes.last().toInt() xor 1).toByte()
        }
        database.attachmentDao().update(
            stored.copy(
                encryptedFilename = corruptedCiphertext,
                contentFormatVersion = 0,
                storageState = AttachmentRecordEntity.STORAGE_STATE_LEGACY,
            ),
        )

        assertTrue(
            attachmentRepository.import(
                credentialId,
                ByteArraySource("new.txt", null, byteArrayOf(3)),
            ).isSuccess,
        )
        assertEquals(
            "renamed.txt",
            attachmentRepository.rename(credentialId, healthy.id, "renamed.txt").getOrThrow().fileName,
        )
        assertEquals(
            AttachmentAvailability.CORRUPTED_FILENAME,
            assertNotNull(credentialRepository.getById(credentialId).getOrThrow()).attachments
                .first { it.id == legacy.id }
                .availability,
        )
    }

    @Test
    fun `historical unsafe filename remains visible for collision checks but requires rename`() = runTest {
        val historical = attachmentRepository.import(
            credentialId,
            ByteArraySource("historical.txt", null, byteArrayOf(1)),
        ).getOrThrow()
        replaceFilename(historical.id, "historical:name.txt")

        val loaded = assertNotNull(credentialRepository.getById(credentialId).getOrThrow())
        val attachment = loaded.attachments.first { it.id == historical.id }
        assertEquals("historical:name.txt", attachment.fileName)
        assertEquals(AttachmentAvailability.FILENAME_REQUIRES_RENAME, attachment.availability)

        val imported = attachmentRepository.import(
            credentialId,
            ByteArraySource("historical:name.txt", null, byteArrayOf(2)),
        )
        assertIs<AttachmentInvalidFileNameException>(imported.exceptionOrNull())

        val repaired = attachmentRepository.rename(credentialId, historical.id, "historical-name.txt").getOrThrow()
        assertEquals("historical-name.txt", repaired.fileName)
        assertEquals(
            AttachmentAvailability.AVAILABLE,
            assertNotNull(credentialRepository.getById(credentialId).getOrThrow())
                .attachments.single().availability,
        )
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
    fun `state downgrade is quarantined without deleting an intact object`() = runTest {
        val attachment = attachmentRepository.import(
            credentialId,
            ByteArraySource("state-tamper.bin", null, byteArrayOf(1, 2, 3)),
        ).getOrThrow()
        val stored = requireEntity(attachment.id)
        val path = objectPath(stored)
        database.attachmentDao().update(
            stored.copy(storageState = AttachmentRecordEntity.STORAGE_STATE_LEGACY),
        )
        val restarted = AttachmentRepositoryImpl(
            attachmentDao = database.attachmentDao(),
            credentialDao = database.credentialDao(),
            blobStore = blobStore,
            cryptoEngine = cryptoEngine,
            sessionManager = vaultRepository,
        )

        assertIs<AttachmentCorruptedException>(
            restarted.copyContentTo(credentialId, attachment.id, RecordingSink()).exceptionOrNull(),
        )
        assertIs<AttachmentCorruptedException>(
            restarted.verify(credentialId, attachment.id).exceptionOrNull(),
        )
        assertIs<AttachmentCorruptedException>(
            restarted.delete(credentialId, attachment.id).exceptionOrNull(),
        )
        assertIs<AttachmentCorruptedException>(
            restarted.rename(credentialId, attachment.id, "must-not-change.bin").exceptionOrNull(),
        )
        assertIs<AttachmentCorruptedException>(
            credentialRepository.getById(credentialId).exceptionOrNull(),
        )
        assertContentEquals(stored.encryptedFilename, requireEntity(attachment.id).encryptedFilename)
        assertTrue(Files.exists(path))
    }

    @Test
    fun `format downgrade is quarantined and remains protected and quota accounted`() = runTest {
        val attachment = attachmentRepository.import(
            credentialId,
            ByteArraySource("format-tamper.bin", null, byteArrayOf(1, 2, 3, 4)),
        ).getOrThrow()
        val stored = requireEntity(attachment.id)
        val path = objectPath(stored)
        database.attachmentDao().update(stored.copy(contentFormatVersion = 0))
        val restarted = AttachmentRepositoryImpl(
            attachmentDao = database.attachmentDao(),
            credentialDao = database.credentialDao(),
            blobStore = blobStore,
            cryptoEngine = cryptoEngine,
            sessionManager = vaultRepository,
        )

        assertIs<AttachmentCorruptedException>(
            restarted.copyContentTo(credentialId, attachment.id, RecordingSink()).exceptionOrNull(),
        )
        assertEquals(1, database.attachmentDao().getOccupiedSlotCount(credentialId.value))
        assertEquals(4, database.attachmentDao().getManagedSizeBytes(credentialId.value))
        assertEquals(1, database.vaultBackupDao().getManagedAttachmentCount())
        assertEquals(
            listOf(attachment.id.value),
            database.vaultBackupDao().getManagedAttachmentPage("", 10).map { it.id },
        )
        assertNotNull(database.attachmentDao().getById(attachment.id.value, credentialId.value))
        assertTrue(Files.exists(path))
    }

    @Test
    fun `pending-state tamper cannot authorize deletion of a published object`() = runTest {
        val attachment = attachmentRepository.import(
            credentialId,
            ByteArraySource("pending-tamper.bin", null, byteArrayOf(4, 5, 6)),
        ).getOrThrow()
        val stored = requireEntity(attachment.id)
        val path = objectPath(stored)
        database.attachmentDao().update(
            stored.copy(storageState = AttachmentRecordEntity.STORAGE_STATE_STAGING),
        )
        val restarted = AttachmentRepositoryImpl(
            attachmentDao = database.attachmentDao(),
            credentialDao = database.credentialDao(),
            blobStore = blobStore,
            cryptoEngine = cryptoEngine,
            sessionManager = vaultRepository,
        )

        assertFailsWith<AttachmentCorruptedException> {
            restarted.withStableAttachments { }
        }
        assertNotNull(database.attachmentDao().getById(attachment.id.value, credentialId.value))
        assertTrue(Files.exists(path))
    }

    @Test
    fun `genuine legacy metadata remains deletable without a managed object`() = runTest {
        val attachment = attachmentRepository.import(
            credentialId,
            ByteArraySource("legacy-metadata.bin", null, byteArrayOf(7)),
        ).getOrThrow()
        val stored = requireEntity(attachment.id)
        blobStore.delete(stored.storagePath)
        database.attachmentDao().update(
            stored.copy(
                contentFormatVersion = 0,
                storageState = AttachmentRecordEntity.STORAGE_STATE_LEGACY,
            ),
        )

        assertTrue(attachmentRepository.delete(credentialId, attachment.id).isSuccess)
        assertNull(database.attachmentDao().getById(attachment.id.value, credentialId.value))
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
    fun `teardown failure does not close an import source twice`() = runTest {
        val source = ThrowingCloseSource()

        assertTrue(attachmentRepository.import(credentialId, source).isSuccess)

        assertEquals(1, source.closeCalls)
        assertEquals(1, database.attachmentDao().getByCredential(credentialId.value).size)
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
    fun `nested attachment operations fail fast and release the operation lock`() = runTest {
        val source = ByteArraySource("nested.txt", null, byteArrayOf(1, 2, 3))
        attachmentRepository.withStableAttachments { }

        val error = assertFailsWith<IllegalStateException> {
            withTimeout(10_000) {
                attachmentRepository.withStableAttachments {
                    attachmentRepository.import(credentialId, source).getOrThrow()
                }
            }
        }

        assertTrue(error.message.orEmpty().contains("withStableAttachments"))
        assertTrue(source.closed)

        // The failed nested call must not leave the sole repository mutex held.
        assertTrue(
            attachmentRepository.import(
                credentialId,
                ByteArraySource("after-nested.txt", null, byteArrayOf(4)),
            ).isSuccess,
        )
    }

    @Test
    fun `nested credential deletion returns a bounded failure instead of deadlocking`() = runTest {
        attachmentRepository.withStableAttachments { }

        val result = withContext(Dispatchers.Default) {
            withTimeout(2_000) {
                attachmentRepository.withStableAttachments {
                    credentialRepository.delete(credentialId)
                }
            }
        }

        assertIs<IllegalStateException>(result.exceptionOrNull())
        assertEquals("credential-attachments", credentialRepository.getById(credentialId).getOrNull()?.id?.value)
        assertTrue(
            attachmentRepository.import(
                credentialId,
                ByteArraySource("after-delete.txt", null, byteArrayOf(5)),
            ).isSuccess,
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
    fun `legacy and managed metadata share the visible attachment slot limit`() = runTest {
        repeat(AttachmentPolicy.MAX_ATTACHMENTS_PER_CREDENTIAL) { index ->
            attachmentRepository.import(
                credentialId,
                ByteArraySource("legacy-$index.bin", null, byteArrayOf(index.toByte())),
            ).getOrThrow()
        }
        database.attachmentDao().getByCredential(credentialId.value).dropLast(1).forEach { entity ->
            convertToLegacyMetadata(entity)
        }
        val source = ByteArraySource("one-too-many.bin", null, byteArrayOf(1))

        assertEquals(
            database.attachmentDao().getByCredential(credentialId.value).size,
            database.attachmentDao().getOccupiedSlotCount(credentialId.value),
        )
        assertIs<AttachmentCountLimitException>(
            attachmentRepository.import(credentialId, source).exceptionOrNull(),
        )
        assertEquals(0, source.readCalls)
    }

    @Test
    fun `legacy declared size does not consume managed object byte quota`() = runTest {
        val legacy = attachmentRepository.import(
            credentialId,
            ByteArraySource("legacy-large.bin", null, byteArrayOf(1)),
        ).getOrThrow()
        convertToLegacyMetadata(
            entity = requireEntity(legacy.id),
            reportedSizeBytes = AttachmentPolicy.MAX_TOTAL_SIZE_PER_CREDENTIAL_BYTES,
        )

        assertEquals(1, database.attachmentDao().getOccupiedSlotCount(credentialId.value))
        assertEquals(0, database.attachmentDao().getManagedSizeBytes(credentialId.value))
        assertTrue(
            attachmentRepository.import(
                credentialId,
                ByteArraySource("managed.bin", null, byteArrayOf(2)),
            ).isSuccess,
        )
        assertEquals(2, database.attachmentDao().getOccupiedSlotCount(credentialId.value))
        assertEquals(1, database.attachmentDao().getManagedSizeBytes(credentialId.value))
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

    private suspend fun corruptFilenameCiphertext(id: AttachmentId) {
        val stored = requireEntity(id)
        val ciphertext = stored.encryptedFilename.copyOf().also { bytes ->
            bytes[bytes.lastIndex] = (bytes.last().toInt() xor 1).toByte()
        }
        database.attachmentDao().update(stored.copy(encryptedFilename = ciphertext))
    }

    private suspend fun corruptFilenameNonce(id: AttachmentId) {
        val stored = requireEntity(id)
        val nonce = stored.filenameNonce.copyOf().also { bytes ->
            bytes[bytes.lastIndex] = (bytes.last().toInt() xor 1).toByte()
        }
        database.attachmentDao().update(stored.copy(filenameNonce = nonce))
    }

    private suspend fun replaceFilename(id: AttachmentId, fileName: String) {
        val stored = requireEntity(id)
        val vek = vaultRepository.withUnlockedSession { it.copyOf() }
        var key: ByteArray? = null
        var plaintext: ByteArray? = null
        val aad = "passvault:attachment:${stored.id}:${stored.credentialId}:filename:v1".encodeToByteArray()
        try {
            key = cryptoEngine.deriveSubkey(vek, "attachment:${stored.keyDerivationContext}", 32).getOrThrow()
            plaintext = fileName.encodeToByteArray(throwOnInvalidSequence = true)
            val encrypted = PaddedPayload.encrypt(
                cryptoEngine,
                plaintext,
                key,
                aad,
                AttachmentPolicy.MAX_FILE_NAME_CODE_POINTS * 4,
            ).getOrThrow()
            try {
                database.attachmentDao().update(
                    stored.copy(
                        encryptedFilename = CryptoEnvelope.encode(encrypted),
                        filenameNonce = encrypted.nonce.copyOf(),
                    ),
                )
            } finally {
                encrypted.clear()
            }
        } finally {
            plaintext?.let(cryptoEngine::secureWipe)
            key?.let(cryptoEngine::secureWipe)
            cryptoEngine.secureWipe(aad)
            cryptoEngine.secureWipe(vek)
        }
    }

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

    private suspend fun convertToLegacyMetadata(
        entity: AttachmentRecordEntity,
        reportedSizeBytes: Long = entity.sizeBytes,
    ) {
        blobStore.delete(entity.storagePath)
        database.attachmentDao().update(
            entity.copy(
                sizeBytes = reportedSizeBytes,
                contentFormatVersion = 0,
                storageState = AttachmentRecordEntity.STORAGE_STATE_LEGACY,
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

    private class TrickleSource(
        name: String,
        private val content: ByteArray,
        private val maximumReadBytes: Int,
    ) : AttachmentContentSource {
        override val displayName = name
        override val claimedMimeType: String? = null
        override val declaredSizeBytes: Long = content.size.toLong()
        private var offset = 0

        override suspend fun read(buffer: ByteArray): Int {
            if (offset == content.size) return -1
            val count = minOf(buffer.size, maximumReadBytes, content.size - offset)
            content.copyInto(buffer, endIndex = offset + count, startIndex = offset)
            offset += count
            return count
        }

        override suspend fun close() = Unit
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

    private class ThrowingCloseSource : AttachmentContentSource {
        override val displayName = "close-failure.bin"
        override val claimedMimeType: String? = null
        override val declaredSizeBytes: Long? = 1
        var closeCalls = 0
        private var read = false

        override suspend fun read(buffer: ByteArray): Int = if (read) -1 else {
            read = true
            buffer[0] = 7
            1
        }

        override suspend fun close() {
            closeCalls++
            error("simulated teardown failure")
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
