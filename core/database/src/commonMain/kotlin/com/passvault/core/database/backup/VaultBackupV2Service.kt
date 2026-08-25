@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.passvault.core.database.backup

import androidx.room.deferredTransaction
import androidx.room.immediateTransaction
import androidx.room.useReaderConnection
import androidx.room.useWriterConnection
import com.passvault.core.crypto.CryptoEngine
import com.passvault.core.database.VaultDatabase
import com.passvault.core.database.attachment.AttachmentBlobStore
import com.passvault.core.database.attachment.AttachmentContainerCodec
import com.passvault.core.database.attachment.AttachmentContentBinding
import com.passvault.core.database.attachment.AttachmentLifecycleManager
import com.passvault.core.database.dao.VaultBackupDao
import com.passvault.core.database.entity.AttachmentRecordEntity
import com.passvault.core.domain.model.BackupPasswordPolicy
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.repository.AttachmentContentSink
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import okio.Buffer
import okio.BufferedSink
import kotlin.uuid.Uuid

/** Authenticated, row-streaming backup implementation for format version 2. */
@Suppress("LargeClass") // The class owns one ordered wire protocol and its symmetric two-pass state machine.
internal class VaultBackupV2Service(
    private val backupDao: VaultBackupDao,
    private val database: VaultDatabase,
    private val cryptoEngine: CryptoEngine,
    private val sessionManager: com.passvault.core.database.repository.VaultSessionManager,
    private val blobStore: AttachmentBlobStore,
    private val attachmentLifecycleManager: AttachmentLifecycleManager,
    private val newValidator: (BackupStreamManifest) -> BackupStreamValidator,
    private val activateRestore: suspend (suspend () -> Unit) -> Unit,
) {
    private val attachmentCodec = AttachmentContainerCodec(blobStore, cryptoEngine)

    suspend fun create(
        password: SensitiveText,
        sink: BackupContentSink,
        onProgress: (Int) -> Unit,
    ): Result<VaultBackupService.BackupInspection> {
        if (!BackupPasswordPolicy.acceptsNew(password)) {
            return rejectInvalidPassword(sink)
        }
        var writer: BackupV2Writer? = null
        var committed = false
        return try {
            val inspection = attachmentLifecycleManager.withStableAttachments {
                sessionManager.withUnlockedSession { vek ->
                    writer = BackupV2Writer.create(sink, password, cryptoEngine)
                    val activeWriter = requireNotNull(writer)
                    val validated = database.useReaderConnection { connection ->
                        connection.deferredTransaction {
                            writeMetadataStream(activeWriter, onProgress)
                        }
                    }
                    var totalObjectBytes = 0L
                    var managedIndex = 0
                    forEachManagedAttachment { attachment ->
                        require(managedIndex < validated.managedAttachmentIds.size)
                        require(attachment.id == validated.managedAttachmentIds[managedIndex])
                        try {
                            verifyManagedAttachment(attachment, vek)
                            totalObjectBytes += writeAttachment(activeWriter, attachment)
                        } finally {
                            BackupMetadataValue.Attachment(attachment).clear()
                        }
                        managedIndex++
                        onProgress(
                            20 + managedIndex * 75 /
                                validated.manifest.managedAttachmentCount.coerceAtLeast(1),
                        )
                    }
                    require(managedIndex == validated.manifest.managedAttachmentCount)
                    val finalPayload = finalPayload(
                        recordCountBeforeFinal = activeWriter.nextRecordIndex(),
                        managedAttachmentCount = managedIndex,
                        totalObjectBytes = totalObjectBytes,
                    )
                    try {
                        activeWriter.writeRecord(BackupRecordType.FINAL, finalPayload)
                    } finally {
                        cryptoEngine.secureWipe(finalPayload)
                    }
                    validated.manifest.toInspection()
                }
            }
            sink.commit()
            committed = true
            onProgress(100)
            Result.success(inspection)
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (_: Exception) {
            Result.failure(IllegalStateException("The encrypted backup could not be created"))
        } finally {
            writer?.clear()
            if (!committed) withContext(NonCancellable) { runCatching { sink.abort() } }
        }
    }

    suspend fun inspectAfterMagic(
        source: BackupContentSource,
        password: SensitiveText,
        magic: ByteArray,
    ): Result<VaultBackupService.BackupInspection> {
        var reader: BackupV2Reader? = null
        return try {
            reader = BackupV2Reader.createAfterMagic(source, password, cryptoEngine, magic)
            val firstPass = sessionManager.withUnlockedSession {
                parseCompleteBackup(
                    reader = requireNotNull(reader),
                    stageObjects = false,
                    stagedPaths = mutableMapOf(),
                    onProgress = {},
                )
            }
            cryptoEngine.secureWipe(firstPass.metadataTranscript)
            Result.success(firstPass.validated.manifest.toInspection())
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (_: Exception) {
            Result.failure(IllegalArgumentException(BACKUP_INVALID_MESSAGE))
        } finally {
            withContext(NonCancellable) {
                runCatching {
                    val activeReader = reader
                    if (activeReader == null) source.close() else activeReader.close()
                }
            }
        }
    }

    suspend fun restoreAfterMagic(
        source: BackupContentSource,
        password: SensitiveText,
        magic: ByteArray,
        onProgress: (Int) -> Unit,
    ): Result<VaultBackupService.BackupInspection> {
        var reader: BackupV2Reader? = null
        val stagedPaths = mutableMapOf<String, String>()
        var committed = false
        var firstTranscript: ByteArray? = null
        return try {
            attachmentLifecycleManager.withStableAttachments {
                reader = BackupV2Reader.createAfterMagic(source, password, cryptoEngine, magic)
                val firstPass = sessionManager.withUnlockedSession {
                    parseCompleteBackup(
                        reader = requireNotNull(reader),
                        stageObjects = true,
                        stagedPaths = stagedPaths,
                        onProgress = onProgress,
                    )
                }
                firstTranscript = firstPass.metadataTranscript
                requireNotNull(reader).close()
                reader = null
                source.rewind()
                val replayMagic = readMagic(source)
                reader = BackupV2Reader.createAfterMagic(source, password, cryptoEngine, replayMagic)
                val replayReader = requireNotNull(reader)
                val replayManifest = readManifest(replayReader)
                require(replayManifest == firstPass.validated.manifest)

                activateRestore {
                    database.useWriterConnection { connection ->
                        connection.immediateTransaction {
                            clearVaultTables()
                            replayMetadataIntoRoom(
                                reader = replayReader,
                                manifest = replayManifest,
                                expectedTranscript = requireNotNull(firstTranscript),
                                stagedPaths = stagedPaths,
                            )
                        }
                    }
                }
                committed = true
                cleanupUnreferencedObjects(stagedPaths)
                onProgress(100)
                Result.success(firstPass.validated.manifest.toInspection())
            }
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (_: Exception) {
            Result.failure(IllegalStateException(BACKUP_RESTORE_FAILED_MESSAGE))
        } finally {
            withContext(NonCancellable) {
                runCatching {
                    val activeReader = reader
                    if (activeReader == null) source.close() else activeReader.close()
                }
                if (!committed) {
                    stagedPaths.values.forEach { path -> runCatching { blobStore.delete(path) } }
                }
                firstTranscript?.let(cryptoEngine::secureWipe)
            }
        }
    }

    private suspend fun rejectInvalidPassword(
        sink: BackupContentSink,
    ): Result<VaultBackupService.BackupInspection> {
        withContext(NonCancellable) { runCatching { sink.abort() } }
        return Result.failure(IllegalArgumentException("Backup password length is invalid"))
    }

    private suspend fun cleanupUnreferencedObjects(stagedPaths: Map<String, String>) {
        runCatching { blobStore.removeUnreferencedObjects(stagedPaths.values.toSet()) }
    }

    private suspend fun writeMetadataStream(
        writer: BackupV2Writer,
        onProgress: (Int) -> Unit,
    ): ValidatedBackupStream {
        val manifest = readDatabaseManifest()
        val validator = newValidator(manifest)
        val manifestBytes = BackupEntityBinaryCodec.encodeManifest(manifest)
        try {
            writer.writeRecord(BackupRecordType.MANIFEST, manifestBytes)
        } finally {
            cryptoEngine.secureWipe(manifestBytes)
        }

        val metadata = requireNotNull(backupDao.getVaultMetadata()) { "Vault metadata is missing" }
        writeMetadataValue(writer, validator, BackupMetadataValue.Metadata(metadata))
        emitSingleKeyPages(
            limit = SMALL_PAGE_ROWS,
            fetch = backupDao::getFolderPage,
            key = { it.id },
        ) { writeMetadataValue(writer, validator, BackupMetadataValue.Folder(it)) }
        emitSingleKeyPages(
            limit = SMALL_PAGE_ROWS,
            fetch = backupDao::getTagPage,
            key = { it.id },
        ) { writeMetadataValue(writer, validator, BackupMetadataValue.Tag(it)) }
        emitSingleKeyPages(
            limit = LARGE_VALUE_PAGE_ROWS,
            fetch = backupDao::getCredentialPage,
            key = { it.id },
        ) { writeMetadataValue(writer, validator, BackupMetadataValue.Credential(it)) }
        emitSingleKeyPages(
            limit = REFERENCE_PAGE_ROWS,
            fetch = backupDao::getCanonicalCredentialFolderReferencePage,
            key = { it.credentialId },
        ) { writeMetadataValue(writer, validator, BackupMetadataValue.CredentialFolderReference(it)) }
        emitCompositeKeyPages(
            limit = REFERENCE_PAGE_ROWS,
            fetch = backupDao::getCredentialTagReferencePage,
            firstKey = { it.credentialId },
            secondKey = { it.tagId },
        ) { writeMetadataValue(writer, validator, BackupMetadataValue.CredentialTagReference(it)) }
        emitSingleKeyPages(
            limit = LARGE_VALUE_PAGE_ROWS,
            fetch = backupDao::getAttachmentPage,
            key = { it.id },
        ) { writeMetadataValue(writer, validator, BackupMetadataValue.Attachment(it)) }
        emitCompositeKeyPages(
            limit = LARGE_VALUE_PAGE_ROWS,
            fetch = backupDao::getPasswordHistoryPage,
            firstKey = { it.credentialId },
            secondKey = { it.id },
        ) { writeMetadataValue(writer, validator, BackupMetadataValue.PasswordHistory(it)) }

        val validated = validator.finish()
        writer.writeRecord(BackupRecordType.METADATA_END, EMPTY_RECORD)
        onProgress(20)
        return validated
    }

    private suspend fun writeMetadataValue(
        writer: BackupV2Writer,
        validator: BackupStreamValidator,
        value: BackupMetadataValue,
    ) {
        var encoded: ByteArray? = null
        try {
            validator.accept(value)
            encoded = BackupEntityBinaryCodec.encode(value)
            writer.writeRecord(value.recordType, encoded)
        } finally {
            encoded?.let(cryptoEngine::secureWipe)
            value.clear()
        }
    }

    private suspend fun parseCompleteBackup(
        reader: BackupV2Reader,
        stageObjects: Boolean,
        stagedPaths: MutableMap<String, String>,
        onProgress: (Int) -> Unit,
    ): FirstPass {
        val manifest = readManifest(reader)
        val validator = newValidator(manifest)
        readMetadataRows(reader, manifest, validator, consumer = null)
        readMetadataEnd(reader)
        val validated = validator.finish()
        val transcript = reader.metadataTranscript()
        var completed = false
        try {
            onProgress(20)
            var totalObjectBytes = 0L
            validated.managedAttachmentIds.forEachIndexed { index, attachmentId ->
                val newPath = if (stageObjects) randomObjectPath() else null
                totalObjectBytes += readAttachment(reader, attachmentId, newPath)
                if (newPath != null) stagedPaths[attachmentId] = newPath
                onProgress(
                    20 + (index + 1) * 75 /
                        validated.manifest.managedAttachmentCount.coerceAtLeast(1),
                )
            }
            val finalRecord = reader.readRecord(FINAL_PAYLOAD_MAX_BYTES)
            require(finalRecord.type == BackupRecordType.FINAL)
            try {
                validateFinalPayload(
                    bytes = finalRecord.plaintext,
                    expectedRecordIndex = finalRecord.index,
                    managedAttachmentCount = validated.manifest.managedAttachmentCount,
                    totalObjectBytes = totalObjectBytes,
                )
            } finally {
                cryptoEngine.secureWipe(finalRecord.plaintext)
            }
            reader.requireExhausted()
            completed = true
            return FirstPass(validated, transcript)
        } finally {
            if (!completed) cryptoEngine.secureWipe(transcript)
        }
    }

    private suspend fun replayMetadataIntoRoom(
        reader: BackupV2Reader,
        manifest: BackupStreamManifest,
        expectedTranscript: ByteArray,
        stagedPaths: Map<String, String>,
    ) {
        val validator = newValidator(manifest)
        readMetadataRows(reader, manifest, validator) { value ->
            insertMetadataValue(value, stagedPaths)
        }
        readMetadataEnd(reader)
        validator.finish()
        val replayTranscript = reader.metadataTranscript()
        try {
            require(cryptoEngine.constantTimeEquals(expectedTranscript, replayTranscript))
        } finally {
            cryptoEngine.secureWipe(replayTranscript)
        }
    }

    private suspend fun readManifest(reader: BackupV2Reader): BackupStreamManifest {
        val record = reader.readRecord(
            BackupEntityBinaryCodec.maximumPlaintextBytes(BackupRecordType.MANIFEST),
            includeInMetadataTranscript = true,
        )
        require(record.type == BackupRecordType.MANIFEST)
        return try {
            BackupEntityBinaryCodec.decodeManifest(record.plaintext)
        } finally {
            cryptoEngine.secureWipe(record.plaintext)
        }
    }

    private suspend fun readMetadataRows(
        reader: BackupV2Reader,
        manifest: BackupStreamManifest,
        validator: BackupStreamValidator,
        consumer: (suspend (BackupMetadataValue) -> Unit)?,
    ) {
        readTypedValues(reader, BackupRecordType.METADATA, 1, validator, consumer)
        readTypedValues(reader, BackupRecordType.FOLDER, manifest.folderCount, validator, consumer)
        readTypedValues(reader, BackupRecordType.TAG, manifest.tagCount, validator, consumer)
        readTypedValues(reader, BackupRecordType.CREDENTIAL, manifest.credentialCount, validator, consumer)
        readTypedValues(
            reader,
            BackupRecordType.CREDENTIAL_FOLDER_REFERENCE,
            manifest.credentialFolderReferenceCount,
            validator,
            consumer,
        )
        readTypedValues(
            reader,
            BackupRecordType.CREDENTIAL_TAG_REFERENCE,
            manifest.credentialTagReferenceCount,
            validator,
            consumer,
        )
        readTypedValues(reader, BackupRecordType.ATTACHMENT, manifest.attachmentCount, validator, consumer)
        readTypedValues(
            reader,
            BackupRecordType.PASSWORD_HISTORY,
            manifest.passwordHistoryCount,
            validator,
            consumer,
        )
    }

    private suspend fun readTypedValues(
        reader: BackupV2Reader,
        type: Int,
        count: Int,
        validator: BackupStreamValidator,
        consumer: (suspend (BackupMetadataValue) -> Unit)?,
    ) {
        repeat(count) {
            val record = reader.readRecord(
                BackupEntityBinaryCodec.maximumPlaintextBytes(type),
                includeInMetadataTranscript = true,
            )
            require(record.type == type)
            var value: BackupMetadataValue? = null
            try {
                value = BackupEntityBinaryCodec.decode(type, record.plaintext)
                validator.accept(value)
                consumer?.invoke(value)
            } finally {
                value?.clear()
                cryptoEngine.secureWipe(record.plaintext)
            }
        }
    }

    private suspend fun readMetadataEnd(reader: BackupV2Reader) {
        val record = reader.readRecord(0, includeInMetadataTranscript = true)
        try {
            require(record.type == BackupRecordType.METADATA_END)
            require(record.plaintext.isEmpty())
        } finally {
            cryptoEngine.secureWipe(record.plaintext)
        }
    }

    private suspend fun insertMetadataValue(
        value: BackupMetadataValue,
        stagedPaths: Map<String, String>,
    ) {
        when (value) {
            is BackupMetadataValue.Metadata -> backupDao.insertVaultMetadata(value.value)
            is BackupMetadataValue.Folder -> backupDao.insertFolders(listOf(value.value))
            is BackupMetadataValue.Tag -> backupDao.insertTags(listOf(value.value))
            is BackupMetadataValue.Credential -> backupDao.insertCredentials(listOf(value.value))
            is BackupMetadataValue.CredentialFolderReference ->
                backupDao.insertCredentialFolderReferences(listOf(value.value))
            is BackupMetadataValue.CredentialTagReference ->
                backupDao.insertCredentialTagReferences(listOf(value.value))
            is BackupMetadataValue.Attachment -> {
                val entity = if (value.value.storageState == AttachmentRecordEntity.STORAGE_STATE_READY) {
                    value.value.copy(
                        storagePath = requireNotNull(stagedPaths[value.value.id]) {
                            "The staged attachment object is missing"
                        },
                    )
                } else {
                    value.value
                }
                backupDao.insertAttachments(listOf(entity))
            }
            is BackupMetadataValue.PasswordHistory ->
                backupDao.insertPasswordHistory(listOf(value.value))
        }
    }

    private suspend fun clearVaultTables() {
        backupDao.deleteCredentialFolderReferences()
        backupDao.deleteCredentialTagReferences()
        backupDao.deletePasswordHistory()
        backupDao.deleteAttachments()
        backupDao.deleteCredentials()
        backupDao.deleteFolders()
        backupDao.deleteTags()
        backupDao.deleteVaultMetadata()
        backupDao.deleteMigrationState()
        backupDao.deleteCurrentVersionInfo()
        backupDao.deleteCorruptionLogs()
    }

    private suspend fun readDatabaseManifest() = BackupStreamManifest(
        credentialCount = backupDao.getCredentialCount(),
        folderCount = backupDao.getFolderCount(),
        tagCount = backupDao.getTagCount(),
        credentialFolderReferenceCount = backupDao.getCanonicalCredentialFolderReferenceCount(),
        credentialTagReferenceCount = backupDao.getCredentialTagReferenceCount(),
        attachmentCount = backupDao.getAttachmentCount(),
        managedAttachmentCount = backupDao.getManagedAttachmentCount(),
        passwordHistoryCount = backupDao.getPasswordHistoryCount(),
    )

    private suspend fun forEachManagedAttachment(block: suspend (AttachmentRecordEntity) -> Unit) {
        emitSingleKeyPages(
            limit = LARGE_VALUE_PAGE_ROWS,
            fetch = backupDao::getManagedAttachmentPage,
            key = { it.id },
            block = block,
        )
    }

    private suspend fun <T> emitSingleKeyPages(
        limit: Int,
        fetch: suspend (String, Int) -> List<T>,
        key: (T) -> String,
        block: suspend (T) -> Unit,
    ) {
        var after = ""
        while (true) {
            val page = fetch(after, limit)
            require(page.size <= limit)
            if (page.isEmpty()) return
            page.forEach { value ->
                val next = key(value)
                require(next > after)
                block(value)
                after = next
            }
        }
    }

    private suspend fun <T> emitCompositeKeyPages(
        limit: Int,
        fetch: suspend (String, String, Int) -> List<T>,
        firstKey: (T) -> String,
        secondKey: (T) -> String,
        block: suspend (T) -> Unit,
    ) {
        var afterFirst = ""
        var afterSecond = ""
        while (true) {
            val page = fetch(afterFirst, afterSecond, limit)
            require(page.size <= limit)
            if (page.isEmpty()) return
            page.forEach { value ->
                val nextFirst = firstKey(value)
                val nextSecond = secondKey(value)
                require(nextFirst > afterFirst || nextFirst == afterFirst && nextSecond > afterSecond)
                block(value)
                afterFirst = nextFirst
                afterSecond = nextSecond
            }
        }
    }

    private suspend fun writeAttachment(
        writer: BackupV2Writer,
        attachment: AttachmentRecordEntity,
    ): Long = blobStore.read(
        attachment.storagePath,
        AttachmentContainerCodec.MAX_ENCRYPTED_OBJECT_BYTES,
    ) { source, fileSize ->
        val start = attachmentStartPayload(attachment.id, fileSize)
        try {
            writer.writeRecord(BackupRecordType.ATTACHMENT_START, start)
        } finally {
            cryptoEngine.secureWipe(start)
        }
        val chunk = ByteArray(BackupLimits.RECORD_PLAINTEXT_BYTES)
        var totalBytes = 0L
        var chunkCount = 0L
        try {
            while (totalBytes < fileSize) {
                val requested = minOf(chunk.size.toLong(), fileSize - totalBytes).toInt()
                val count = source.read(chunk, 0, requested)
                require(count in 1..requested)
                val ownedChunk = chunk.copyOf(count)
                try {
                    writer.writeRecord(BackupRecordType.ATTACHMENT_CONTENT, ownedChunk)
                } finally {
                    cryptoEngine.secureWipe(ownedChunk)
                }
                totalBytes += count
                chunkCount++
            }
            require(source.exhausted())
            val end = attachmentEndPayload(attachment.id, totalBytes, chunkCount)
            try {
                writer.writeRecord(BackupRecordType.ATTACHMENT_END, end)
            } finally {
                cryptoEngine.secureWipe(end)
            }
            totalBytes
        } finally {
            cryptoEngine.secureWipe(chunk)
        }
    }

    private suspend fun readAttachment(
        reader: BackupV2Reader,
        attachmentId: String,
        stagedPath: String?,
    ): Long {
        val startRecord = reader.readRecord(ATTACHMENT_CONTROL_MAX_BYTES)
        require(startRecord.type == BackupRecordType.ATTACHMENT_START)
        val encryptedObjectBytes = try {
            parseAttachmentStart(startRecord.plaintext, attachmentId)
        } finally {
            cryptoEngine.secureWipe(startRecord.plaintext)
        }
        require(encryptedObjectBytes in 1..AttachmentContainerCodec.MAX_ENCRYPTED_OBJECT_BYTES)
        return if (stagedPath == null) {
            consumeAttachmentContent(reader, attachmentId, encryptedObjectBytes, sink = null)
        } else {
            blobStore.writeAtomically(stagedPath) { sink ->
                consumeAttachmentContent(reader, attachmentId, encryptedObjectBytes, sink)
            }
        }
    }

    private suspend fun consumeAttachmentContent(
        reader: BackupV2Reader,
        attachmentId: String,
        encryptedObjectBytes: Long,
        sink: BufferedSink?,
    ): Long {
        var totalBytes = 0L
        var chunkCount = 0L
        while (totalBytes < encryptedObjectBytes) {
            val record = reader.readRecord(BackupLimits.RECORD_PLAINTEXT_BYTES)
            require(record.type == BackupRecordType.ATTACHMENT_CONTENT)
            try {
                require(record.plaintext.isNotEmpty())
                require(totalBytes + record.plaintext.size <= encryptedObjectBytes)
                sink?.write(record.plaintext)
                totalBytes += record.plaintext.size
                chunkCount++
            } finally {
                cryptoEngine.secureWipe(record.plaintext)
            }
        }
        val endRecord = reader.readRecord(ATTACHMENT_CONTROL_MAX_BYTES)
        require(endRecord.type == BackupRecordType.ATTACHMENT_END)
        try {
            validateAttachmentEnd(endRecord.plaintext, attachmentId, totalBytes, chunkCount)
        } finally {
            cryptoEngine.secureWipe(endRecord.plaintext)
        }
        return totalBytes
    }

    private suspend fun verifyManagedAttachment(entity: AttachmentRecordEntity, vek: ByteArray) {
        val key = cryptoEngine.deriveSubkey(
            masterKey = vek,
            context = "attachment:${entity.keyDerivationContext}",
            size = ATTACHMENT_KEY_BYTES,
        ).getOrThrow()
        try {
            attachmentCodec.decryptObject(
                relativePath = entity.storagePath,
                expectedSizeBytes = entity.sizeBytes,
                key = key,
                binding = AttachmentContentBinding(
                    attachmentId = entity.id,
                    credentialId = entity.credentialId,
                    keyDerivationContext = entity.keyDerivationContext,
                    mimeType = entity.mimeType,
                ),
                sink = DISCARDING_ATTACHMENT_SINK,
            )
        } finally {
            cryptoEngine.secureWipe(key)
        }
    }

    private suspend fun readMagic(source: BackupContentSource): ByteArray {
        val expectedSize = BACKUP_V2_MAGIC.size
        val result = ByteArray(expectedSize)
        var offset = 0
        var completed = false
        try {
            while (offset < expectedSize) {
                val temporary = ByteArray(expectedSize - offset)
                try {
                    val count = source.read(temporary)
                    require(count in 1..temporary.size)
                    temporary.copyInto(result, destinationOffset = offset, endIndex = count)
                    offset += count
                } finally {
                    cryptoEngine.secureWipe(temporary)
                }
            }
            require(result.contentEquals(BACKUP_V2_MAGIC))
            completed = true
            return result
        } finally {
            if (!completed) cryptoEngine.secureWipe(result)
        }
    }

    private fun randomObjectPath(): String = "objects/${Uuid.random()}.pva"

    private data class FirstPass(
        val validated: ValidatedBackupStream,
        val metadataTranscript: ByteArray,
    )

    private companion object {
        const val ATTACHMENT_KEY_BYTES = 32
        const val ATTACHMENT_CONTROL_MAX_BYTES = 1024
        const val FINAL_PAYLOAD_MAX_BYTES = 64
        const val LARGE_VALUE_PAGE_ROWS = 1
        const val SMALL_PAGE_ROWS = 64
        const val REFERENCE_PAGE_ROWS = 512
        val EMPTY_RECORD = byteArrayOf()
        val DISCARDING_ATTACHMENT_SINK = object : AttachmentContentSink {
            override suspend fun write(buffer: ByteArray, byteCount: Int) = Unit
            override suspend fun commit() = Unit
            override suspend fun abort() = Unit
        }
    }
}

private fun BackupStreamManifest.toInspection() = VaultBackupService.BackupInspection(
    credentialCount = credentialCount,
    folderCount = folderCount,
    tagCount = tagCount,
    attachmentCount = attachmentCount,
    warnings = emptyList(),
)

private fun attachmentStartPayload(attachmentId: String, encryptedObjectBytes: Long): ByteArray =
    Buffer().writeLengthPrefixedUtf8(attachmentId).writeLong(encryptedObjectBytes).readByteArray()

private fun parseAttachmentStart(bytes: ByteArray, expectedAttachmentId: String): Long {
    val source = Buffer().write(bytes)
    require(source.readLengthPrefixedUtf8() == expectedAttachmentId)
    val size = source.readLong()
    require(source.exhausted())
    return size
}

private fun attachmentEndPayload(attachmentId: String, totalBytes: Long, chunkCount: Long): ByteArray =
    Buffer()
        .writeLengthPrefixedUtf8(attachmentId)
        .writeLong(totalBytes)
        .writeLong(chunkCount)
        .readByteArray()

private fun validateAttachmentEnd(
    bytes: ByteArray,
    expectedAttachmentId: String,
    expectedBytes: Long,
    expectedChunks: Long,
) {
    val source = Buffer().write(bytes)
    require(source.readLengthPrefixedUtf8() == expectedAttachmentId)
    require(source.readLong() == expectedBytes)
    require(source.readLong() == expectedChunks)
    require(source.exhausted())
}

private fun finalPayload(
    recordCountBeforeFinal: Long,
    managedAttachmentCount: Int,
    totalObjectBytes: Long,
): ByteArray = Buffer()
    .writeLong(recordCountBeforeFinal)
    .writeInt(managedAttachmentCount)
    .writeLong(totalObjectBytes)
    .readByteArray()

private fun validateFinalPayload(
    bytes: ByteArray,
    expectedRecordIndex: Long,
    managedAttachmentCount: Int,
    totalObjectBytes: Long,
) {
    val source = Buffer().write(bytes)
    require(source.readLong() == expectedRecordIndex)
    require(source.readInt() == managedAttachmentCount)
    require(source.readLong() == totalObjectBytes)
    require(source.exhausted())
}

private fun Buffer.writeLengthPrefixedUtf8(value: String): Buffer {
    val encoded = value.encodeToByteArray(throwOnInvalidSequence = true)
    try {
        require(encoded.size <= 512)
        return writeInt(encoded.size).write(encoded)
    } finally {
        encoded.fill(0)
    }
}

internal fun Buffer.readLengthPrefixedUtf8(): String {
    val size = readInt()
    require(size in 1..512)
    val encoded = readByteArray(size.toLong())
    return try {
        encoded.decodeToString(throwOnInvalidSequence = true)
    } finally {
        encoded.fill(0)
    }
}

private const val BACKUP_INVALID_MESSAGE = "The backup password is incorrect or the backup is corrupt."
private const val BACKUP_RESTORE_FAILED_MESSAGE = "The validated backup could not be restored."
