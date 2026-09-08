package com.passvault.core.database.backup

import androidx.room.Room
import androidx.room.useReaderConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import com.passvault.core.crypto.CryptoEnvelope
import com.passvault.core.crypto.DesktopCryptoEngine
import com.passvault.core.crypto.PaddedPayload
import com.passvault.core.crypto.VaultKeyHierarchy
import com.passvault.core.database.VaultDatabase
import com.passvault.core.database.attachment.AttachmentContainerCodec
import com.passvault.core.database.attachment.AttachmentContentBinding
import com.passvault.core.database.attachment.AttachmentRepositoryImpl
import com.passvault.core.database.attachment.LocalAttachmentBlobStore
import com.passvault.core.database.attachment.attachmentFilenameAssociatedData
import com.passvault.core.database.entity.AttachmentRecordEntity
import com.passvault.core.database.entity.PasswordHistoryRecordEntity
import com.passvault.core.database.repository.CredentialRepositoryImpl
import com.passvault.core.database.repository.FolderRepositoryImpl
import com.passvault.core.database.repository.TagRepositoryImpl
import com.passvault.core.database.repository.VaultRepositoryImpl
import com.passvault.core.domain.model.AttachmentId
import com.passvault.core.domain.model.Credential
import com.passvault.core.domain.model.CredentialId
import com.passvault.core.domain.model.CredentialType
import com.passvault.core.domain.model.Folder
import com.passvault.core.domain.model.FolderId
import com.passvault.core.domain.model.PasswordHealth
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.model.Tag
import com.passvault.core.domain.model.TagId
import com.passvault.core.domain.repository.AttachmentContentSink
import com.passvault.core.domain.repository.AttachmentContentSource
import com.passvault.core.domain.repository.AttachmentPolicy
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import okio.Buffer
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Instant

/** Tiny synthetic vaults only: real Room, repository crypto, and backup I/O; no platform key store or clipboard. */
class VaultBackupUnicodePaginationTest {
    @Test
    fun `UTF8 export restores Unicode IDs relationships history and managed content`() = runTest {
        roundTrip(encodings[0])
    }

    @Test
    fun `UTF16LE export uses low-byte-first BINARY ordering and restores into UTF8`() = runTest {
        roundTrip(encodings[1])
    }

    @Test
    fun `UTF16BE export preserves its native order and restores into UTF8`() = runTest {
        roundTrip(encodings[2])
    }

    @Test
    fun `all eight generated Room page queries agree with their actual encoding`() = runTest {
        for (encoding in encodings) {
            withFixture(encoding.name) { fixture ->
                fixture.seed(encoding.ids, includeAttachments = true)
                val dao = fixture.database.vaultBackupDao()
                val order = BackupDatabaseTextOrder.fromPragma(fixture.encoding())
                val (low, high) = encoding.ids
                for (limit in listOf(1, 2)) {
                    assertEquals(encoding.ids, collectSimple(limit, order, dao::getCredentialPage) { it.id })
                    assertEquals(encoding.ids, collectSimple(limit, order, dao::getFolderPage) { it.id })
                    assertEquals(encoding.ids, collectSimple(limit, order, dao::getTagPage) { it.id })
                    assertEquals(encoding.ids,
                        collectSimple(limit, order, dao::getCanonicalCredentialFolderReferencePage) { it.credentialId })
                    assertEquals(encoding.ids, collectSimple(limit, order, dao::getAttachmentPage) { it.id })
                    assertEquals(encoding.ids, collectSimple(limit, order, dao::getManagedAttachmentPage) { it.id })
                    assertEquals(listOf(low to low, low to high, high to low), collectTuple(
                        limit, order, dao::getCredentialTagReferencePage, { it.credentialId }, { it.tagId },
                    ))
                    assertEquals(listOf(low to low, low to high, high to "a-history"), collectTuple(
                        limit, order, dao::getPasswordHistoryPage, { it.credentialId }, { it.id },
                    ))
                }
                fixture.assertContents(encoding.ids, includeAttachments = true)
            }
        }
    }

    @Test
    fun `legacy logical backup with Unicode IDs reexports without renaming ciphertext`() = runTest {
        val ids = encodings[0].ids
        withFixture("UTF-8") { source ->
            source.seed(ids, includeAttachments = false)
            source.assertContents(ids, includeAttachments = false)
            val bytes = withPassword(BACKUP_PASSWORD) { source.backups.createBackup(it).getOrThrow() }
            try {
                withFixture("UTF-16le") { target ->
                    withPassword(BACKUP_PASSWORD) { target.backups.restoreBackup(bytes, it).getOrThrow() }
                    withPassword(MASTER_PASSWORD) { target.vault.unlock(it).getOrThrow() }
                    target.assertContents(ids, includeAttachments = false)
                    assertCiphertextPreserved(source, target, ids)
                    assertEquals("UTF-16le", target.encoding())
                    val reexport = MemorySink()
                    withPassword(BACKUP_PASSWORD) { target.backups.createBackup(it, reexport).getOrThrow() }
                    assertTrue(reexport.committed)
                    reexport.bytes().fill(0)
                }
            } finally {
                bytes.fill(0)
            }
        }
    }

    @Test
    fun `empty singleton and ASCII vaults remain exportable`() = runTest {
        for (encoding in encodings) {
            withFixture(encoding.name) { fixture ->
                for (id in listOf(null, "\uD800\uDC00", "ascii-a", "ascii-b")) {
                    if (id != null) fixture.saveCredential(id)
                    val sink = MemorySink()
                    val result = withPassword(BACKUP_PASSWORD) { fixture.backups.createBackup(it, sink).getOrThrow() }
                    assertEquals(fixture.database.vaultBackupDao().getCredentialCount(), result.credentialCount)
                    assertEquals(0, result.attachmentCount)
                    assertTrue(sink.committed)
                    assertFalse(sink.aborted)
                    sink.bytes().fill(0)
                }
            }
            // The cumulative fixture above also checks mixed Unicode/ASCII. Keep this control ASCII-only.
            withFixture(encoding.name) { fixture ->
                for (id in listOf("ascii-a", "ascii-b")) fixture.saveCredential(id)
                val sink = MemorySink()
                val result = withPassword(BACKUP_PASSWORD) { fixture.backups.createBackup(it, sink).getOrThrow() }
                assertEquals(2, result.credentialCount)
                assertEquals(0, result.attachmentCount)
                assertTrue(sink.committed)
                assertFalse(sink.aborted)
                sink.bytes().fill(0)
            }
        }
    }

    private suspend fun roundTrip(encoding: EncodingCase) {
        withFixture(encoding.name) { source ->
            source.seed(encoding.ids, includeAttachments = true)
            // This must pass BEFORE export: the regression is not invalid IDs or relabeled ciphertext.
            source.assertContents(encoding.ids, includeAttachments = true)
            val sink = MemorySink()
            val created = withPassword(BACKUP_PASSWORD) { source.backups.createBackup(it, sink).getOrThrow() }
            assertEquals(2, created.credentialCount)
            assertEquals(2, created.folderCount)
            assertEquals(2, created.tagCount)
            assertEquals(2, created.attachmentCount)
            assertTrue(sink.committed)
            assertFalse(sink.aborted)
            val bytes = sink.bytes()
            try {
                val inspectionInput = MemorySource(bytes)
                val inspected = withPassword(BACKUP_PASSWORD) {
                    source.backups.inspectBackup(inspectionInput, it).getOrThrow()
                }
                assertEquals(created, inspected)
                assertTrue(inspectionInput.closed)
                val destinationEncoding = if (encoding.name == "UTF-8") "UTF-16le" else "UTF-8"
                withFixture(destinationEncoding) { target ->
                    val restoreInput = MemorySource(bytes)
                    val restored = withPassword(BACKUP_PASSWORD) {
                        target.backups.restoreBackup(restoreInput, it).getOrThrow()
                    }
                    assertEquals(created, restored)
                    assertTrue(restoreInput.closed)
                    withPassword(MASTER_PASSWORD) { target.vault.unlock(it).getOrThrow() }
                    target.assertContents(encoding.ids, includeAttachments = true)
                    assertCiphertextPreserved(source, target, encoding.ids)
                    assertEquals(destinationEncoding, target.encoding())
                    val reexport = MemorySink()
                    withPassword(BACKUP_PASSWORD) { target.backups.createBackup(it, reexport).getOrThrow() }
                    assertTrue(reexport.committed)
                    reexport.bytes().fill(0)
                }
            } finally {
                bytes.fill(0)
            }
        }
    }

    private suspend fun assertCiphertextPreserved(source: Fixture, target: Fixture, ids: List<String>) {
        for (id in ids) {
            val before = assertNotNull(source.database.credentialDao().getById(id))
            val after = assertNotNull(target.database.credentialDao().getById(id))
            assertContentEquals(before.summaryPayload, after.summaryPayload)
            assertContentEquals(before.secretPayload, after.secretPayload)
            assertContentEquals(before.summaryNonce, after.summaryNonce)
            assertContentEquals(before.secretNonce, after.secretNonce)
            val oldFolder = assertNotNull(source.database.folderDao().getById(id))
            val newFolder = assertNotNull(target.database.folderDao().getById(id))
            assertContentEquals(oldFolder.encryptedPayload, newFolder.encryptedPayload)
            assertContentEquals(oldFolder.payloadNonce, newFolder.payloadNonce)
            val oldTag = assertNotNull(source.database.tagDao().getById(id))
            val newTag = assertNotNull(target.database.tagDao().getById(id))
            assertContentEquals(oldTag.encryptedPayload, newTag.encryptedPayload)
            assertContentEquals(oldTag.payloadNonce, newTag.payloadNonce)
            val histories = target.database.passwordHistoryDao().getByCredential(id).associateBy { it.id }
            for (history in source.database.passwordHistoryDao().getByCredential(id)) {
                val restored = assertNotNull(histories[history.id])
                assertContentEquals(history.encryptedPassword, restored.encryptedPassword)
                assertContentEquals(history.passwordNonce, restored.passwordNonce)
                assertEquals(history.credentialId, restored.credentialId)
            }
            val attachments = target.database.attachmentDao().getByCredential(id).associateBy { it.id }
            for (attachment in source.database.attachmentDao().getByCredential(id)) {
                val restored = assertNotNull(attachments[attachment.id])
                assertContentEquals(attachment.encryptedFilename, restored.encryptedFilename)
                assertContentEquals(attachment.filenameNonce, restored.filenameNonce)
                assertEquals(attachment.keyDerivationContext, restored.keyDerivationContext)
            }
        }
    }

    private suspend fun <T> collectSimple(
        limit: Int,
        order: BackupDatabaseTextOrder,
        fetch: suspend (String, Int) -> List<T>,
        key: (T) -> String,
    ): List<String> {
        val result = mutableListOf<String>()
        order.emitSingleKeyPages(limit, fetch, key) { result += key(it) }
        return result
    }

    private suspend fun <T> collectTuple(
        limit: Int,
        order: BackupDatabaseTextOrder,
        fetch: suspend (String, String, Int) -> List<T>,
        first: (T) -> String,
        second: (T) -> String,
    ): List<Pair<String, String>> {
        val result = mutableListOf<Pair<String, String>>()
        order.emitCompositeKeyPages(limit, fetch, first, second) { result += first(it) to second(it) }
        return result
    }

    private suspend fun <T> withFixture(encoding: String, block: suspend (Fixture) -> T): T {
        require(encodings.any { it.name == encoding })
        val root = Files.createTempDirectory("passvault-pagination-")
        var database: VaultDatabase? = null
        var fixture: Fixture? = null
        try {
            val path = root.resolve("synthetic.db")
            BundledSQLiteDriver().open(path.toString()).use { connection ->
                // SQLite fixes the encoding at first schema creation; setting it after Room opens is too late.
                connection.execSQL("PRAGMA encoding = '$encoding'")
                connection.execSQL("CREATE TABLE pagination_encoding_seed (value INTEGER)")
                connection.execSQL("DROP TABLE pagination_encoding_seed")
            }
            val opened = Room.databaseBuilder<VaultDatabase>(name = path.toString())
                .setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(Dispatchers.IO)
                .build()
            database = opened
            val active = Fixture(opened, root)
            fixture = active
            assertEquals(encoding, active.encoding(), "Fixture must really retain its requested encoding")
            withPassword(MASTER_PASSWORD) { active.vault.create(it).getOrThrow() }
            withPassword(MASTER_PASSWORD) { active.vault.unlock(it).getOrThrow() }
            return block(active)
        } finally {
            withContext(NonCancellable) {
                try {
                    fixture?.vault?.lock()?.getOrThrow()
                } finally {
                    try {
                        database?.close()
                    } finally {
                        deleteOwnedFixture(root)
                    }
                }
            }
        }
    }

    private class Fixture(val database: VaultDatabase, root: Path) {
        val crypto = DesktopCryptoEngine()
        val vault = VaultRepositoryImpl(database.vaultMetadataDao(), crypto, VaultKeyHierarchy(crypto))
        val blobs = LocalAttachmentBlobStore(root.resolve("vault-files").toString())
        val attachments = AttachmentRepositoryImpl(database.attachmentDao(), database.credentialDao(), blobs, crypto, vault)
        val credentials = CredentialRepositoryImpl(
            database.credentialDao(), database.folderDao(), database.tagDao(), database.attachmentDao(),
            database.passwordHistoryDao(), crypto, vault, attachmentLifecycleManager = attachments,
        )
        val folders = FolderRepositoryImpl(database.folderDao(), crypto, vault)
        val tags = TagRepositoryImpl(database.tagDao(), crypto, vault)
        val backups = VaultBackupService(
            database.vaultBackupDao(), crypto, vault, vault, database = database,
            attachmentBlobStore = blobs, attachmentLifecycleManager = attachments,
        )

        suspend fun encoding(): String = database.useReaderConnection { connection ->
            connection.usePrepared("PRAGMA encoding") { statement ->
                check(statement.step())
                val value = statement.getText(0)
                check(!statement.step())
                value
            }
        }

        suspend fun seed(ids: List<String>, includeAttachments: Boolean) {
            // Reverse insertion order is intentional: neither SQL nor the expected order uses it.
            ids.withIndex().reversed().forEach { (index, id) ->
                folders.save(Folder(FolderId(id), null, "Folder $index", null, index, Instant.fromEpochMilliseconds(1)))
                    .getOrThrow()
                tags.save(Tag(TagId(id), "Tag $index", null)).getOrThrow()
            }
            ids.withIndex().reversed().forEach { (index, id) ->
                saveCredential(id, FolderId(id), if (index == 0) ids.map(::TagId).toSet() else setOf(TagId(ids[0])))
            }
            insertHistory(ids[0], ids[0], 0)
            insertHistory(ids[1], ids[0], 1)
            insertHistory("a-history", ids[1], 2)
            if (includeAttachments) ids.withIndex().reversed().forEach { (index, id) -> insertAttachment(id, ids[0], index) }
        }

        suspend fun saveCredential(id: String, folder: FolderId? = null, tagIds: Set<TagId> = emptySet()) {
            val credential = Credential(
                id = CredentialId(id), type = CredentialType.Login, title = "Synthetic pagination item",
                username = null, email = null, password = SensitiveText.from("synthetic-credential-secret"),
                urls = emptyList(), notes = null, recoveryCodes = emptyList(), apiKeys = emptyList(),
                licenseKeys = emptyList(), customFields = emptyList(), folderId = folder, tagIds = tagIds,
                isFavorite = false, attachments = emptyList(), passwordHistory = emptyList(),
                createdAt = Instant.fromEpochMilliseconds(1), updatedAt = Instant.fromEpochMilliseconds(1),
                lastUsedAt = null, passwordHealth = PasswordHealth.UNKNOWN,
            )
            try {
                credentials.save(credential).getOrThrow()
            } finally {
                credential.clearSensitiveValues()
            }
        }

        suspend fun assertContents(ids: List<String>, includeAttachments: Boolean) {
            ids.forEachIndexed { index, id ->
                assertEquals("Folder $index", folders.getById(FolderId(id)).getOrThrow()?.name)
                assertEquals("Tag $index", tags.getById(TagId(id)).getOrThrow()?.name)
                val credential = assertNotNull(credentials.getById(CredentialId(id)).getOrThrow())
                try {
                    assertEquals(id, credential.id.value)
                    assertEquals("synthetic-credential-secret", credential.password?.toStringUnsafe())
                    assertEquals(FolderId(id), credential.folderId)
                    assertEquals(if (index == 0) ids.map(::TagId).toSet() else setOf(TagId(ids[0])), credential.tagIds)
                    val expectedHistory = if (index == 0) setOf("older-0", "older-1") else setOf("older-2")
                    assertEquals(expectedHistory, credential.passwordHistory.map { it.password.toStringUnsafe() }.toSet())
                    val expectedAttachments = if (includeAttachments && index == 0) ids.toSet() else emptySet()
                    assertEquals(expectedAttachments, credential.attachments.map { it.id.value }.toSet())
                    credential.attachments.forEach { attachment ->
                        assertEquals("fixture-${ids.indexOf(attachment.id.value)}.bin", attachment.fileName)
                        val output = MemorySink()
                        attachments.copyContentTo(CredentialId(id), AttachmentId(attachment.id.value), output).getOrThrow()
                        assertTrue(output.committed)
                        val actual = output.bytes()
                        try { assertContentEquals(CONTENT, actual) } finally { actual.fill(0) }
                    }
                } finally {
                    credential.clearSensitiveValues()
                }
            }
        }

        private suspend fun insertHistory(historyId: String, owner: String, index: Int) {
            val plaintext = "older-$index".encodeToByteArray()
            val aad = "passvault:history:$historyId:$owner:v2".encodeToByteArray()
            try {
                vault.withUnlockedSession { vek ->
                    val key = crypto.deriveSubkey(vek, "history:$historyId", 32).getOrThrow()
                    try {
                        val encrypted = PaddedPayload.encrypt(crypto, plaintext, key, aad, 4_096 * 4).getOrThrow()
                        try {
                            database.passwordHistoryDao().insert(PasswordHistoryRecordEntity(
                                historyId, owner, CryptoEnvelope.encode(encrypted), encrypted.nonce.copyOf(), index.toLong(),
                            ))
                        } finally { encrypted.clear() }
                    } finally { crypto.secureWipe(key) }
                }
            } finally {
                plaintext.fill(0)
                aad.fill(0)
            }
        }

        private suspend fun insertAttachment(id: String, owner: String, index: Int) {
            val context = "synthetic-context-$index"
            val path = "objects/00000000-0000-0000-0000-00000000000${index + 1}.pva"
            val filename = "fixture-$index.bin".encodeToByteArray()
            val aad = attachmentFilenameAssociatedData(id, owner)
            val input = AttachmentInput()
            try {
                vault.withUnlockedSession { vek ->
                    val key = crypto.deriveSubkey(vek, "attachment:$context", 32).getOrThrow()
                    try {
                        val stored = AttachmentContainerCodec(blobs, crypto).encryptToObject(
                            path, input, key, AttachmentContentBinding(id, owner, context, ""), existingCredentialBytes = 0,
                        )
                        val encrypted = PaddedPayload.encrypt(
                            crypto, filename, key, aad, AttachmentPolicy.MAX_FILE_NAME_CODE_POINTS * 4,
                        ).getOrThrow()
                        try {
                            database.attachmentDao().insert(AttachmentRecordEntity(
                                id, owner, CryptoEnvelope.encode(encrypted), encrypted.nonce.copyOf(), stored.mimeType,
                                stored.sizeBytes, path, context, index.toLong(), AttachmentPolicy.CONTENT_FORMAT_VERSION,
                                AttachmentRecordEntity.STORAGE_STATE_READY,
                            ))
                        } finally { encrypted.clear() }
                    } finally { crypto.secureWipe(key) }
                }
            } finally {
                input.close()
                filename.fill(0)
                aad.fill(0)
            }
        }
    }

    private class MemorySink : BackupContentSink, AttachmentContentSink {
        private val buffer = Buffer()
        var committed = false
            private set
        var aborted = false
            private set
        override suspend fun write(buffer: ByteArray, byteCount: Int) {
            check(!committed && !aborted)
            require(byteCount in 0..buffer.size)
            require(this.buffer.size + byteCount <= 256 * 1024) { "Synthetic fixture exceeded its small memory budget" }
            this.buffer.write(buffer, 0, byteCount)
        }
        override suspend fun commit() { check(!aborted); committed = true }
        override suspend fun abort() {
            if (!committed) { aborted = true; buffer.clear() }
        }
        fun bytes(): ByteArray { check(committed); return buffer.readByteArray() }
    }

    private class MemorySource(private val bytes: ByteArray) : BackupContentSource {
        override val declaredSizeBytes = bytes.size.toLong()
        private var offset = 0
        var closed = false
            private set
        override suspend fun read(buffer: ByteArray): Int {
            check(!closed)
            if (offset == bytes.size) return -1
            val count = minOf(buffer.size, bytes.size - offset, 19)
            bytes.copyInto(buffer, startIndex = offset, endIndex = offset + count)
            offset += count
            return count
        }
        override suspend fun close() { closed = true }
        override suspend fun rewind() { closed = false; offset = 0 }
    }

    private class AttachmentInput : AttachmentContentSource {
        override val displayName = "synthetic.bin"
        override val claimedMimeType = "application/octet-stream"
        override val declaredSizeBytes = CONTENT.size.toLong()
        private var offset = 0
        override suspend fun read(buffer: ByteArray): Int {
            if (offset == CONTENT.size) return -1
            val count = minOf(buffer.size, CONTENT.size - offset)
            CONTENT.copyInto(buffer, startIndex = offset, endIndex = offset + count)
            offset += count
            return count
        }
        override suspend fun close() = Unit
    }

    private data class EncodingCase(val name: String, val ids: List<String>)

    private companion object {
        const val MASTER_PASSWORD = "correct horse battery staple"
        const val BACKUP_PASSWORD = "independent synthetic backup password"
        val CONTENT = byteArrayOf(1, 2, 3, 4, 5)
        // Explicit unsigned-byte order; none of these expectations is generated by the implementation.
        val encodings = listOf(
            EncodingCase("UTF-8", listOf("\uFB00", "\uD800\uDC00")),
            EncodingCase("UTF-16le", listOf("\u0100", "\u00FF")),
            EncodingCase("UTF-16be", listOf("\uD800\uDC00", "\uFB00")),
        )

        suspend fun <T> withPassword(value: String, block: suspend (SensitiveText) -> T): T {
            val password = SensitiveText.from(value)
            return try { block(password) } finally { password.clear() }
        }

        fun deleteOwnedFixture(root: Path) {
            check(root.fileName.toString().startsWith("passvault-pagination-"))
            check(!Files.isSymbolicLink(root))
            check(root.parent.toRealPath() == Path.of(System.getProperty("java.io.tmpdir")).toRealPath())
            // No FOLLOW_LINKS: a foreign symlink fails cleanup rather than traversing outside this one owned root.
            Files.walkFileTree(root, object : SimpleFileVisitor<Path>() {
                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    check(!attrs.isSymbolicLink)
                    Files.delete(file)
                    return FileVisitResult.CONTINUE
                }
                override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
                    if (exc != null) throw exc
                    Files.delete(dir)
                    return FileVisitResult.CONTINUE
                }
            })
            check(!Files.exists(root))
        }
    }
}
