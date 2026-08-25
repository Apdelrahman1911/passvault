@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.passvault.core.database.attachment

import com.passvault.core.crypto.CryptoEngine
import com.passvault.core.crypto.CryptoEnvelope
import com.passvault.core.crypto.PaddedPayload
import com.passvault.core.database.dao.AttachmentDao
import com.passvault.core.database.dao.CredentialDao
import com.passvault.core.database.entity.AttachmentRecordEntity
import com.passvault.core.database.repository.VaultSessionManager
import com.passvault.core.database.repository.repositoryResult
import com.passvault.core.domain.model.AttachmentAvailability
import com.passvault.core.domain.model.AttachmentId
import com.passvault.core.domain.model.AttachmentMetadata
import com.passvault.core.domain.model.CredentialId
import com.passvault.core.domain.model.codePointLength
import com.passvault.core.domain.model.takeCodePoints
import com.passvault.core.domain.repository.AttachmentContentSink
import com.passvault.core.domain.repository.AttachmentContentSource
import com.passvault.core.domain.repository.AttachmentCountLimitException
import com.passvault.core.domain.repository.AttachmentLegacyContentUnavailableException
import com.passvault.core.domain.repository.AttachmentPolicy
import com.passvault.core.domain.repository.AttachmentRepository
import com.passvault.core.domain.repository.AttachmentTotalSizeLimitException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

private object StableAttachmentOperationKey : CoroutineContext.Key<StableAttachmentOperation>

private object StableAttachmentOperation : AbstractCoroutineContextElement(StableAttachmentOperationKey)

class AttachmentRepositoryImpl(
    private val attachmentDao: AttachmentDao,
    private val credentialDao: CredentialDao,
    private val blobStore: AttachmentBlobStore,
    private val cryptoEngine: CryptoEngine,
    private val sessionManager: VaultSessionManager,
    private val clock: Clock = Clock.System,
) : AttachmentRepository, AttachmentLifecycleManager {
    private val operationMutex = Mutex()
    private val codec = AttachmentContainerCodec(blobStore, cryptoEngine)
    private var recoveryCompleted = false

    override suspend fun <T> withStableAttachments(block: suspend () -> T): T {
        checkNoNestedAttachmentOperation()
        return operationMutex.withLock {
            recoverInterruptedOperations()
            withContext(StableAttachmentOperation) { block() }
        }
    }

    private suspend fun <T> withAttachmentOperation(block: suspend () -> T): T {
        checkNoNestedAttachmentOperation()
        return operationMutex.withLock { block() }
    }

    private suspend fun checkNoNestedAttachmentOperation() {
        if (currentCoroutineContext()[StableAttachmentOperationKey] != null) {
            throw IllegalStateException("Attachment repository operations cannot run inside withStableAttachments")
        }
    }

    override suspend fun import(
        credentialId: CredentialId,
        source: AttachmentContentSource,
    ): Result<AttachmentMetadata> {
        return try {
            withAttachmentOperation {
                repositoryResult {
                    recoverInterruptedOperations()
                    sessionManager.withUnlockedSession { vek ->
                        importUnlocked(credentialId, source, vek)
                    }
                }
            }
        } finally {
            // The repository owns the picker handle for the complete import.
            // Closing in one outer finally avoids split ownership and a
            // second close when a platform close operation throws.
            withContext(NonCancellable) { runCatching { source.close() } }
        }
    }

    private suspend fun importUnlocked(
        credentialId: CredentialId,
        source: AttachmentContentSource,
        vek: ByteArray,
    ): AttachmentMetadata {
        require(credentialDao.exists(credentialId.value)) { "The credential does not exist" }
        val managedBytes = validateImportBounds(credentialId, source.declaredSizeBytes)
        val existingNames = attachmentDao.getByCredential(credentialId.value).map { decryptFilename(it, vek) }
        val fileName = uniqueFileName(AttachmentPolicy.validateFileName(source.displayName), existingNames)
        val attachmentId = Uuid.random().toString()
        val keyContext = Uuid.random().toString()
        val storagePath = "objects/${Uuid.random()}.pva"
        val key = deriveAttachmentKey(vek, keyContext)
        val encryptedFilename = encryptFilename(fileName, attachmentId, credentialId.value, key)
        var stagingInserted = false
        var completed = false
        try {
            val staging = newStagingEntity(
                attachmentId = attachmentId,
                credentialId = credentialId.value,
                keyContext = keyContext,
                storagePath = storagePath,
                encryptedFilename = encryptedFilename,
                declaredSize = source.declaredSizeBytes,
            )
            attachmentDao.insert(staging)
            stagingInserted = true
            val stored = codec.encryptToObject(
                relativePath = storagePath,
                source = source,
                key = key,
                bindingWithoutMime = staging.toBinding(),
                existingCredentialBytes = managedBytes,
            )
            val ready = staging.copy(
                mimeType = stored.mimeType,
                sizeBytes = stored.sizeBytes,
                storageState = AttachmentRecordEntity.STORAGE_STATE_READY,
            )
            attachmentDao.update(ready)
            completed = true
            return ready.toMetadata(fileName)
        } finally {
            if (!completed) cleanupFailedImport(attachmentId, storagePath, stagingInserted)
            encryptedFilename.clear()
            cryptoEngine.secureWipe(key)
        }
    }

    private suspend fun validateImportBounds(
        credentialId: CredentialId,
        declaredSize: Long?,
    ): Long {
        declaredSize?.let(AttachmentPolicy::validateFileSize)
        requireAttachmentSlot(attachmentDao.getManagedCount(credentialId.value))
        val managedBytes = attachmentDao.getManagedSizeBytes(credentialId.value)
        require(managedBytes in 0..AttachmentPolicy.MAX_TOTAL_SIZE_PER_CREDENTIAL_BYTES)
        declaredSize?.let { requireAggregateCapacity(managedBytes, it) }
        return managedBytes
    }

    private fun requireAttachmentSlot(managedCount: Int) {
        if (managedCount >= AttachmentPolicy.MAX_ATTACHMENTS_PER_CREDENTIAL) throw AttachmentCountLimitException()
    }

    private fun requireAggregateCapacity(existingBytes: Long, incomingBytes: Long) {
        if (existingBytes + incomingBytes > AttachmentPolicy.MAX_TOTAL_SIZE_PER_CREDENTIAL_BYTES) {
            throw AttachmentTotalSizeLimitException()
        }
    }

    private fun newStagingEntity(
        attachmentId: String,
        credentialId: String,
        keyContext: String,
        storagePath: String,
        encryptedFilename: com.passvault.core.crypto.EncryptedData,
        declaredSize: Long?,
    ) = AttachmentRecordEntity(
        id = attachmentId,
        credentialId = credentialId,
        encryptedFilename = CryptoEnvelope.encode(encryptedFilename),
        filenameNonce = encryptedFilename.nonce.copyOf(),
        mimeType = DEFAULT_MIME_TYPE,
        sizeBytes = declaredSize ?: 0,
        storagePath = storagePath,
        keyDerivationContext = keyContext,
        createdAt = clock.now().toEpochMilliseconds(),
        contentFormatVersion = AttachmentPolicy.CONTENT_FORMAT_VERSION,
        storageState = AttachmentRecordEntity.STORAGE_STATE_STAGING,
    )

    override suspend fun rename(
        credentialId: CredentialId,
        attachmentId: AttachmentId,
        newFileName: String,
    ): Result<AttachmentMetadata> = withAttachmentOperation {
        repositoryResult {
            recoverInterruptedOperations()
            sessionManager.withUnlockedSession { vek ->
                val entity = requireAttachment(credentialId, attachmentId)
                val existingNames = attachmentDao.getByCredential(credentialId.value)
                    .filterNot { it.id == attachmentId.value }
                    .map { decryptFilename(it, vek) }
                val fileName = uniqueFileName(AttachmentPolicy.validateFileName(newFileName), existingNames)
                val key = deriveAttachmentKey(vek, entity.keyDerivationContext)
                val encrypted = encryptFilename(fileName, entity.id, entity.credentialId, key)
                try {
                    attachmentDao.update(
                        entity.copy(
                            encryptedFilename = CryptoEnvelope.encode(encrypted),
                            filenameNonce = encrypted.nonce.copyOf(),
                        ),
                    )
                    entity.toMetadata(fileName)
                } finally {
                    encrypted.clear()
                    cryptoEngine.secureWipe(key)
                }
            }
        }
    }

    override suspend fun delete(
        credentialId: CredentialId,
        attachmentId: AttachmentId,
    ): Result<Unit> = withAttachmentOperation {
        repositoryResult {
            recoverInterruptedOperations()
            val entity = requireAttachment(credentialId, attachmentId)
            if (entity.storageState == AttachmentRecordEntity.STORAGE_STATE_LEGACY) {
                attachmentDao.deleteById(entity.id)
            } else {
                requireManagedEntity(entity)
                // The Room delete is the commit point. If it fails, the row
                // still references an intact object. If cleanup fails after a
                // committed delete, startup recovery removes the now-orphaned
                // encrypted object without risking user-data loss.
                attachmentDao.deleteById(entity.id)
                recoveryCompleted = false
                withContext(NonCancellable) {
                    runCatching { blobStore.delete(entity.storagePath) }
                }
            }
            Unit
        }
    }

    override suspend fun copyContentTo(
        credentialId: CredentialId,
        attachmentId: AttachmentId,
        sink: AttachmentContentSink,
    ): Result<Unit> = withAttachmentOperation {
        var committed = false
        try {
            repositoryResult {
                recoverInterruptedOperations()
                sessionManager.withUnlockedSession { vek ->
                    val entity = requireManagedEntity(requireAttachment(credentialId, attachmentId))
                    val key = deriveAttachmentKey(vek, entity.keyDerivationContext)
                    try {
                        codec.decryptObject(
                            relativePath = entity.storagePath,
                            expectedSizeBytes = entity.sizeBytes,
                            key = key,
                            binding = entity.toBinding(),
                            sink = sink,
                        )
                        sink.commit()
                        committed = true
                    } finally {
                        cryptoEngine.secureWipe(key)
                    }
                }
            }
        } finally {
            if (!committed) withContext(NonCancellable) { runCatching { sink.abort() } }
        }
    }

    override suspend fun verify(
        credentialId: CredentialId,
        attachmentId: AttachmentId,
    ): Result<Unit> = withAttachmentOperation {
        repositoryResult {
            recoverInterruptedOperations()
            sessionManager.withUnlockedSession { vek ->
                val entity = requireManagedEntity(requireAttachment(credentialId, attachmentId))
                val key = deriveAttachmentKey(vek, entity.keyDerivationContext)
                try {
                    codec.decryptObject(
                        relativePath = entity.storagePath,
                        expectedSizeBytes = entity.sizeBytes,
                        key = key,
                        binding = entity.toBinding(),
                        sink = DISCARDING_SINK,
                    )
                } finally {
                    cryptoEngine.secureWipe(key)
                }
            }
        }
    }

    override suspend fun deleteCredentialAndAttachments(
        credentialId: String,
        deleteCredential: suspend () -> Unit,
    ) = withAttachmentOperation {
        recoverInterruptedOperations()
        val attachments = attachmentDao.getByCredential(credentialId)
        val managedPaths = attachments
            .filter { it.storageState == AttachmentRecordEntity.STORAGE_STATE_READY }
            .map { entity -> requireManagedEntity(entity).storagePath }

        // Credential deletion (including Room's attachment-row cascade) is
        // the commit point. Objects are deleted only afterwards, so a failed
        // database operation cannot leave live attachment rows without data.
        deleteCredential()
        recoveryCompleted = false
        withContext(NonCancellable) {
            managedPaths.forEach { path -> runCatching { blobStore.delete(path) } }
        }
    }

    /** Deletes only objects whose database state proves an interrupted operation. */
    private suspend fun recoverInterruptedOperations() {
        if (recoveryCompleted) return
        attachmentDao.getPendingOperations().forEach { entity ->
            require(entity.storageState != AttachmentRecordEntity.STORAGE_STATE_LEGACY)
            require(entity.storageState != AttachmentRecordEntity.STORAGE_STATE_READY)
            blobStore.delete(entity.storagePath)
            attachmentDao.deleteById(entity.id)
        }
        blobStore.removeUnreferencedObjects(attachmentDao.getReadyStoragePaths().toSet())
        recoveryCompleted = true
    }

    private suspend fun cleanupFailedImport(
        attachmentId: String,
        storagePath: String,
        stagingInserted: Boolean,
    ) = withContext(NonCancellable) {
        recoveryCompleted = false
        val objectRemoved = runCatching { blobStore.delete(storagePath) }.isSuccess
        if (stagingInserted && objectRemoved) runCatching { attachmentDao.deleteById(attachmentId) }
    }

    private suspend fun requireAttachment(
        credentialId: CredentialId,
        attachmentId: AttachmentId,
    ): AttachmentRecordEntity = requireNotNull(
        attachmentDao.getById(attachmentId.value, credentialId.value),
    ) { "The attachment does not exist" }

    private fun requireManagedEntity(entity: AttachmentRecordEntity): AttachmentRecordEntity {
        if (entity.storageState != AttachmentRecordEntity.STORAGE_STATE_READY) {
            throw AttachmentLegacyContentUnavailableException()
        }
        require(entity.contentFormatVersion == AttachmentPolicy.CONTENT_FORMAT_VERSION)
        require(entity.sizeBytes in 0..AttachmentPolicy.MAX_FILE_SIZE_BYTES)
        return entity
    }

    private suspend fun deriveAttachmentKey(vek: ByteArray, context: String): ByteArray =
        cryptoEngine.deriveSubkey(vek, "attachment:$context", KEY_BYTES).getOrThrow()

    private suspend fun encryptFilename(
        fileName: String,
        attachmentId: String,
        credentialId: String,
        key: ByteArray,
    ): com.passvault.core.crypto.EncryptedData {
        val plaintext = fileName.encodeToByteArray(throwOnInvalidSequence = true)
        val associatedData = filenameAssociatedData(attachmentId, credentialId)
        return try {
            PaddedPayload.encrypt(
                cryptoEngine = cryptoEngine,
                plaintext = plaintext,
                key = key,
                associatedData = associatedData,
                maxPlaintextBytes = MAX_FILENAME_UTF8_BYTES,
            ).getOrThrow()
        } finally {
            cryptoEngine.secureWipe(plaintext)
            cryptoEngine.secureWipe(associatedData)
        }
    }

    private suspend fun decryptFilename(entity: AttachmentRecordEntity, vek: ByteArray): String {
        val key = deriveAttachmentKey(vek, entity.keyDerivationContext)
        var decrypted: ByteArray? = null
        return try {
            decrypted = PaddedPayload.decrypt(
                cryptoEngine = cryptoEngine,
                storedCiphertext = entity.encryptedFilename,
                nonce = entity.filenameNonce,
                key = key,
                associatedData = filenameAssociatedData(entity.id, entity.credentialId),
                maxPlaintextBytes = MAX_FILENAME_UTF8_BYTES,
            ).getOrThrow()
            AttachmentPolicy.validateStoredFileName(decrypted.decodeToString(throwOnInvalidSequence = true))
        } finally {
            decrypted?.let(cryptoEngine::secureWipe)
            cryptoEngine.secureWipe(key)
        }
    }

    private fun uniqueFileName(requested: String, existing: List<String>): String {
        val normalized = existing.mapTo(mutableSetOf(), AttachmentPolicy::canonicalFileNameKey)
        if (AttachmentPolicy.canonicalFileNameKey(requested) !in normalized) return requested
        val dot = requested.lastIndexOf('.').takeIf { it in 1 until requested.lastIndex }
        val stem = if (dot == null) requested else requested.substring(0, dot)
        val extension = if (dot == null) "" else requested.substring(dot)
        for (number in 2..AttachmentPolicy.MAX_ATTACHMENTS_PER_CREDENTIAL + 1) {
            val suffix = " ($number)"
            val allowedStemCodePoints = AttachmentPolicy.MAX_FILE_NAME_CODE_POINTS -
                extension.codePointLength() - suffix.codePointLength()
            require(allowedStemCodePoints > 0)
            val candidate = stem.takeCodePoints(allowedStemCodePoints) + suffix + extension
            if (AttachmentPolicy.canonicalFileNameKey(candidate) !in normalized) return candidate
        }
        error("A unique attachment filename could not be allocated")
    }

    private fun AttachmentRecordEntity.toBinding() = AttachmentContentBinding(
        attachmentId = id,
        credentialId = credentialId,
        keyDerivationContext = keyDerivationContext,
        mimeType = mimeType,
    )

    private fun AttachmentRecordEntity.toMetadata(fileName: String) = AttachmentMetadata(
        id = AttachmentId(id),
        fileName = fileName,
        mimeType = mimeType,
        sizeBytes = sizeBytes,
        createdAt = Instant.fromEpochMilliseconds(createdAt),
        availability = if (storageState == AttachmentRecordEntity.STORAGE_STATE_READY) {
            AttachmentAvailability.AVAILABLE
        } else {
            AttachmentAvailability.LEGACY_METADATA_ONLY
        },
    )

    private fun filenameAssociatedData(attachmentId: String, credentialId: String): ByteArray =
        "passvault:attachment:$attachmentId:$credentialId:filename:v1".encodeToByteArray()

    private companion object {
        const val KEY_BYTES = 32
        const val MAX_FILENAME_UTF8_BYTES = AttachmentPolicy.MAX_FILE_NAME_CODE_POINTS * 4
        const val DEFAULT_MIME_TYPE = "application/octet-stream"
        val DISCARDING_SINK = object : AttachmentContentSink {
            override suspend fun write(buffer: ByteArray, byteCount: Int) = Unit
            override suspend fun commit() = Unit
            override suspend fun abort() = Unit
        }
    }
}
