@file:OptIn(kotlin.io.encoding.ExperimentalEncodingApi::class)

package com.passvault.core.database.backup

import com.passvault.core.crypto.CryptoEnvelope
import com.passvault.core.crypto.CryptoEngine
import com.passvault.core.crypto.DerivedKey
import com.passvault.core.crypto.Argon2Parameters
import com.passvault.core.database.dao.VaultBackupDao
import com.passvault.core.database.dao.VaultBackupEntities
import com.passvault.core.database.entity.AttachmentRecordEntity
import com.passvault.core.database.entity.CredentialFolderCrossRef
import com.passvault.core.database.entity.CredentialRecordEntity
import com.passvault.core.database.entity.CredentialTagCrossRef
import com.passvault.core.database.entity.FolderRecordEntity
import com.passvault.core.database.entity.PasswordHistoryRecordEntity
import com.passvault.core.database.entity.TagRecordEntity
import com.passvault.core.database.entity.VaultMetadataEntity
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.model.VaultSessionState
import com.passvault.core.domain.repository.VaultRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

private const val BACKUP_FORMAT_VERSION = 1
private const val KDF_ALGORITHM = "Argon2id"
private const val SUPPORTED_VAULT_FORMAT_VERSION = 1
private const val SUPPORTED_CRYPTO_FORMAT_VERSION = 2
private const val ARGON2_SALT_BYTES = 16
private const val XCHACHA_NONCE_BYTES = 24
private const val BACKUP_AAD = "passvault:backup:v1"
private const val MAX_BACKUP_BYTES = 128 * 1024 * 1024
private const val MAX_CIPHERTEXT_BYTES = MAX_BACKUP_BYTES
private const val MAX_PAYLOAD_BYTES = 64 * 1024 * 1024
private const val MAX_HASH_BYTES = 128
private const val MAX_SALT_BYTES = 64
private const val MAX_NONCE_BYTES = 64
private const val MIN_ENCRYPTED_BYTES = 4 + 16
private const val MAX_ENTITY_COUNT = 1_000_000
private const val MAX_IDENTIFIER_LENGTH = 256
private const val MAX_TEXT_LENGTH = 16 * 1024
private const val MAX_ATTACHMENT_SIZE_BYTES = 4L * 1024L * 1024L * 1024L
private const val MIN_ARGON2_OPS = 2
private const val MAX_ARGON2_OPS = 10
private const val MIN_ARGON2_MEM = 32 * 1024 * 1024
private const val MAX_ARGON2_MEM = 256 * 1024 * 1024
private const val MAX_ARGON2_PARALLELISM = 8
private const val BACKUP_INVALID_MESSAGE = "The backup password is incorrect or the backup is corrupt."

private fun String.decodeBase64(maxBytes: Int): ByteArray {
    require(length <= maxBytes * 2)
    val decoded = Base64.decode(this)
    require(decoded.size <= maxBytes)
    return decoded
}

private fun ByteArray.toBase64(): String = Base64.encode(this)

private fun String.isValidIdentifier(): Boolean =
    isNotBlank() &&
        length <= MAX_IDENTIFIER_LENGTH &&
        all { !it.isISOControl() && it != '/' && it != '\\' }

/**
 * Creates and restores an authenticated, versioned backup of the encrypted
 * vault records. The backup password is independent from the vault password.
 *
 * No decrypted credential value crosses this boundary. The database already
 * stores authenticated encrypted payloads, so the complete raw snapshot is
 * wrapped once more with a key derived from the backup password.
 */
@OptIn(ExperimentalEncodingApi::class)
class VaultBackupService(
    private val backupDao: VaultBackupDao,
    private val cryptoEngine: CryptoEngine,
    private val vaultRepository: VaultRepository,
) {
    private val operationMutex = Mutex()

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = false
        explicitNulls = true
    }

    suspend fun createBackup(
        password: SensitiveText,
        includeAttachments: Boolean = false,
    ): Result<ByteArray> = operationMutex.withLock {
        if (includeAttachments) {
            return@withLock Result.failure(
                IllegalArgumentException(
                    "Attachment files cannot be packaged by this build. " +
                        "Create a metadata-only backup instead.",
                ),
            )
        }

        return try {
            val rawSnapshot = backupDao.readSnapshot()
            validateSnapshot(rawSnapshot)
            val snapshot = canonicalizeFolderRelationships(rawSnapshot)
            val payload = SnapshotDto.from(snapshot, attachmentsIncluded = false)
            val plaintext = json.encodeToString(payload).encodeToByteArray()
            try {
                encryptPayload(plaintext, password)
            } finally {
                cryptoEngine.secureWipe(plaintext)
            }
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (_: Exception) {
            Result.failure(IllegalStateException("The encrypted backup could not be created"))
        }
    }

    /**
     * Decrypts and validates a backup without changing the database. This is
     * used to show a restore preview and is intentionally side-effect free.
     */
    suspend fun inspectBackup(
        bytes: ByteArray,
        password: SensitiveText,
    ): Result<BackupInspection> = operationMutex.withLock {
        try {
            val snapshot = decryptPayload(bytes, password)
            validateSnapshot(snapshot.entities())
            Result.success(
                BackupInspection(
                    credentialCount = snapshot.credentials.size,
                    folderCount = snapshot.folders.size,
                    tagCount = snapshot.tags.size,
                    attachmentCount = snapshot.attachments.size,
                    warnings = buildList {
                        if (!snapshot.attachmentsIncluded) {
                            add(BackupWarning.ATTACHMENT_FILES_NOT_INCLUDED_IN_PREVIEW)
                        }
                    },
                ),
            )
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (_: Exception) {
            Result.failure(IllegalArgumentException(BACKUP_INVALID_MESSAGE))
        }
    }

    /**
     * Validates the complete encrypted backup before replacing any data.
     * Replacement occurs in one Room transaction and always leaves the vault
     * locked, even when the restore succeeds.
     */
    suspend fun restoreBackup(
        bytes: ByteArray,
        password: SensitiveText,
    ): Result<BackupInspection> = operationMutex.withLock {
        try {
            val snapshot = decryptPayload(bytes, password)
            val rawEntities = snapshot.entities()
            validateSnapshot(rawEntities)
            // The direct credential_records.folder_id column is the canonical
            // relationship. Older schemas also populated a redundant
            // cross-reference table, so rebuild that table from the
            // authoritative column during restore.
            val entities = canonicalizeFolderRelationships(rawEntities)

            vaultRepository.lock().getOrElse {
                return@withLock Result.failure(IllegalStateException("The vault could not be locked for restore"))
            }
            backupDao.replaceVault(entities)

            Result.success(
                BackupInspection(
                    credentialCount = entities.credentials.size,
                    folderCount = entities.folders.size,
                    tagCount = entities.tags.size,
                    attachmentCount = entities.attachments.size,
                    warnings = buildList {
                        if (!snapshot.attachmentsIncluded) {
                            add(BackupWarning.ATTACHMENT_FILES_NOT_INCLUDED_AFTER_RESTORE)
                        }
                    },
                ),
            )
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (_: Exception) {
            // The Room transaction rolls back on any insert/FK failure. Do not
            // expose parser, SQL, or cryptographic details to the UI.
            Result.failure(IllegalArgumentException(BACKUP_INVALID_MESSAGE))
        }
    }

    private suspend fun encryptPayload(
        plaintext: ByteArray,
        password: SensitiveText,
    ): Result<ByteArray> {
        var passwordBytes: ByteArray? = null
        var salt: ByteArray? = null
        var derivedKey: DerivedKey? = null
        var encrypted: com.passvault.core.crypto.EncryptedData? = null
        return try {
            val parameters = cryptoEngine.benchmarkArgon2().safeForBackup()
            salt = cryptoEngine.generateRandom(ARGON2_SALT_BYTES).getOrThrow()
            passwordBytes = password.toStringUnsafe().encodeToByteArray()
            derivedKey = cryptoEngine.deriveKey(
                password = passwordBytes,
                salt = salt,
                opsLimit = parameters.opsLimit,
                memLimit = parameters.memLimit,
            ).getOrThrow()
            encrypted = cryptoEngine.encrypt(
                plaintext = plaintext,
                key = derivedKey.key,
                associatedData = BACKUP_AAD.encodeToByteArray(),
            ).getOrThrow()

            val envelope = BackupEnvelope(
                formatVersion = BACKUP_FORMAT_VERSION,
                kdfAlgorithm = KDF_ALGORITHM,
                argon2OpsLimit = parameters.opsLimit,
                argon2MemLimit = parameters.memLimit,
                argon2Parallelism = parameters.parallelism,
                salt = salt.toBase64(),
                nonce = encrypted.nonce.toBase64(),
                ciphertext = CryptoEnvelope.encode(encrypted).toBase64(),
            )
            Result.success(json.encodeToString(envelope).encodeToByteArray())
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (error: Exception) {
            Result.failure(error)
        } finally {
            passwordBytes?.let { cryptoEngine.secureWipe(it) }
            salt?.let { cryptoEngine.secureWipe(it) }
            derivedKey?.clear()
            encrypted?.clear()
        }
    }

    private suspend fun decryptPayload(
        bytes: ByteArray,
        password: SensitiveText,
    ): SnapshotDto {
        require(bytes.isNotEmpty() && bytes.size <= MAX_BACKUP_BYTES)
        val envelope = json.decodeFromString<BackupEnvelope>(bytes.decodeToString())
        require(envelope.formatVersion == BACKUP_FORMAT_VERSION)
        require(envelope.kdfAlgorithm == KDF_ALGORITHM)
        require(envelope.argon2OpsLimit in MIN_ARGON2_OPS..MAX_ARGON2_OPS)
        require(envelope.argon2MemLimit in MIN_ARGON2_MEM..MAX_ARGON2_MEM)
        require(envelope.argon2Parallelism in 1..MAX_ARGON2_PARALLELISM)

        val salt = envelope.salt.decodeBase64(MAX_SALT_BYTES)
        val nonce = envelope.nonce.decodeBase64(MAX_NONCE_BYTES)
        val ciphertext = envelope.ciphertext.decodeBase64(MAX_CIPHERTEXT_BYTES)
        require(salt.size == ARGON2_SALT_BYTES)
        require(nonce.size == XCHACHA_NONCE_BYTES)
        require(ciphertext.size >= MIN_ENCRYPTED_BYTES)

        var passwordBytes: ByteArray? = null
        var derivedKey: DerivedKey? = null
        var plaintext: ByteArray? = null
        try {
            passwordBytes = password.toStringUnsafe().encodeToByteArray()
            derivedKey = cryptoEngine.deriveKey(
                password = passwordBytes,
                salt = salt,
                opsLimit = envelope.argon2OpsLimit,
                memLimit = envelope.argon2MemLimit,
            ).getOrThrow()
            plaintext = cryptoEngine.decrypt(
                ciphertext = ciphertext,
                nonce = nonce,
                key = derivedKey.key,
                associatedData = BACKUP_AAD.encodeToByteArray(),
            ).getOrThrow()
            return json.decodeFromString<SnapshotDto>(plaintext.decodeToString())
        } finally {
            cryptoEngine.secureWipe(salt)
            cryptoEngine.secureWipe(nonce)
            cryptoEngine.secureWipe(ciphertext)
            passwordBytes?.let { cryptoEngine.secureWipe(it) }
            derivedKey?.clear()
            plaintext?.let { cryptoEngine.secureWipe(it) }
        }
    }

    private fun validateSnapshot(snapshot: VaultBackupEntities) {
        require(snapshot.metadata.id == 1)
        require(snapshot.metadata.vaultFormatVersion == SUPPORTED_VAULT_FORMAT_VERSION)
        require(snapshot.metadata.cryptoFormatVersion == SUPPORTED_CRYPTO_FORMAT_VERSION)
        require(snapshot.metadata.vaultId.isValidIdentifier())
        require(snapshot.metadata.argon2AlgorithmId == KDF_ALGORITHM)
        require(snapshot.metadata.argon2Salt.size == ARGON2_SALT_BYTES)
        require(snapshot.metadata.argon2OpsLimit in MIN_ARGON2_OPS..MAX_ARGON2_OPS)
        require(snapshot.metadata.argon2MemLimit in MIN_ARGON2_MEM..MAX_ARGON2_MEM)
        require(snapshot.metadata.argon2Parallelism in 1..MAX_ARGON2_PARALLELISM)
        require(
            snapshot.metadata.wrappedVek.size <= MAX_PAYLOAD_BYTES &&
                CryptoEnvelope.isSupportedPayload(snapshot.metadata.wrappedVek),
        )
        require(snapshot.metadata.vekNonce.size == XCHACHA_NONCE_BYTES)
        require(
            snapshot.metadata.encryptedVerificationRecord.size <= MAX_PAYLOAD_BYTES &&
                CryptoEnvelope.isSupportedPayload(snapshot.metadata.encryptedVerificationRecord),
        )
        require(snapshot.metadata.verificationNonce.size == XCHACHA_NONCE_BYTES)
        require(snapshot.metadata.entryCount >= 0)
        require(snapshot.metadata.entryCount == snapshot.credentials.size)

        require(snapshot.credentials.size <= MAX_ENTITY_COUNT)
        require(snapshot.folders.size <= MAX_ENTITY_COUNT)
        require(snapshot.tags.size <= MAX_ENTITY_COUNT)
        require(snapshot.attachments.size <= MAX_ENTITY_COUNT)
        require(snapshot.passwordHistory.size <= MAX_ENTITY_COUNT)

        val credentialIds = snapshot.credentials.map { it.id }.also { require(it.all { id -> id.isValidIdentifier() }) }
        val folderIds = snapshot.folders.map { it.id }.also { require(it.all { id -> id.isValidIdentifier() }) }
        val tagIds = snapshot.tags.map { it.id }.also { require(it.all { id -> id.isValidIdentifier() }) }
        require(credentialIds.size == credentialIds.toSet().size)
        require(folderIds.size == folderIds.toSet().size)
        require(tagIds.size == tagIds.toSet().size)
        val credentialSet = credentialIds.toSet()
        val folderSet = folderIds.toSet()
        val tagSet = tagIds.toSet()

        snapshot.credentials.forEach { credential ->
            require(credential.type.isNotBlank() && credential.type.length <= MAX_TEXT_LENGTH)
            require(credential.titleHash.isNotEmpty() && credential.titleHash.size <= MAX_HASH_BYTES)
            requirePayload(credential.summaryPayload, credential.summaryNonce)
            requirePayload(credential.secretPayload, credential.secretNonce)
            require(credential.folderId == null || credential.folderId in folderSet)
        }

        snapshot.folders.forEach { folder ->
            require(folder.parentId == null || folder.parentId in folderSet)
            require(folder.nameHash.isNotEmpty() && folder.nameHash.size <= MAX_HASH_BYTES)
            requirePayload(folder.encryptedPayload, folder.payloadNonce)
        }
        validateFolderCycles(snapshot.folders)

        snapshot.tags.forEach { tag ->
            require(tag.nameHash.isNotEmpty() && tag.nameHash.size <= MAX_HASH_BYTES)
            requirePayload(tag.encryptedPayload, tag.payloadNonce)
        }

        val folderRefs = snapshot.credentialFolderReferences.map {
            require(it.credentialId in credentialSet)
            require(it.folderId in folderSet)
            "${it.credentialId}\u0000${it.folderId}"
        }
        require(folderRefs.size == folderRefs.toSet().size)

        val tagRefs = snapshot.credentialTagReferences.map {
            require(it.credentialId in credentialSet)
            require(it.tagId in tagSet)
            "${it.credentialId}\u0000${it.tagId}"
        }
        require(tagRefs.size == tagRefs.toSet().size)

        val attachmentIds = snapshot.attachments.map { attachment ->
            require(attachment.credentialId in credentialSet)
            require(attachment.id.isValidIdentifier())
            requirePayload(attachment.encryptedFilename, attachment.filenameNonce)
            require(attachment.mimeType.isSafeMetadataText())
            require(attachment.storagePath.isSafeRelativePath())
            require(attachment.keyDerivationContext.isValidIdentifier())
            require(attachment.sizeBytes in 0..MAX_ATTACHMENT_SIZE_BYTES)
            attachment.id
        }
        require(attachmentIds.size == attachmentIds.toSet().size)

        val historyIds = snapshot.passwordHistory.map { history ->
            require(history.credentialId in credentialSet)
            require(history.id.isValidIdentifier())
            requirePayload(history.encryptedPassword, history.passwordNonce)
            history.id
        }
        require(historyIds.size == historyIds.toSet().size)
    }

    private fun canonicalizeFolderRelationships(snapshot: VaultBackupEntities): VaultBackupEntities {
        val canonicalReferences = snapshot.credentials.mapNotNull { credential ->
            credential.folderId?.let { folderId ->
                CredentialFolderCrossRef(
                    credentialId = credential.id,
                    folderId = folderId,
                )
            }
        }
        return snapshot.copy(credentialFolderReferences = canonicalReferences)
    }

    private fun requirePayload(payload: ByteArray, nonce: ByteArray) {
        require(payload.size <= MAX_PAYLOAD_BYTES && CryptoEnvelope.isSupportedPayload(payload))
        require(nonce.size == XCHACHA_NONCE_BYTES)
    }

    private fun validateFolderCycles(folders: List<FolderRecordEntity>) {
        val parents = folders.associate { it.id to it.parentId }
        folders.forEach { folder ->
            val seen = mutableSetOf<String>()
            var current: String? = folder.id
            while (current != null) {
                require(seen.add(current))
                current = parents[current]
            }
        }
    }

    private fun Argon2Parameters.safeForBackup(): Argon2Parameters =
        copy(
            opsLimit = opsLimit.coerceIn(MIN_ARGON2_OPS, MAX_ARGON2_OPS),
            memLimit = memLimit.coerceIn(MIN_ARGON2_MEM, MAX_ARGON2_MEM),
            parallelism = parallelism.coerceIn(1, MAX_ARGON2_PARALLELISM),
        )

    private fun String.isSafeRelativePath(): Boolean =
        isNotBlank() &&
            length <= MAX_TEXT_LENGTH &&
            !startsWith('/') &&
            !startsWith('\\') &&
            !contains(':') &&
            split('/', '\\').none { it == ".." || it.isBlank() }

    private fun String.isSafeMetadataText(): Boolean =
        isNotBlank() &&
            length <= MAX_TEXT_LENGTH &&
            none { it.isISOControl() }

    data class BackupInspection(
        val credentialCount: Int,
        val folderCount: Int,
        val tagCount: Int,
        val attachmentCount: Int,
        val warnings: List<BackupWarning>,
    )

    enum class BackupWarning {
        ATTACHMENT_FILES_NOT_INCLUDED_IN_PREVIEW,
        ATTACHMENT_FILES_NOT_INCLUDED_AFTER_RESTORE,
    }

    @Serializable
    private data class BackupEnvelope(
        val formatVersion: Int,
        val kdfAlgorithm: String,
        val argon2OpsLimit: Int,
        val argon2MemLimit: Int,
        val argon2Parallelism: Int,
        val salt: String,
        val nonce: String,
        val ciphertext: String,
    )

    @Serializable
    private data class SnapshotDto(
        val metadata: MetadataDto,
        val credentials: List<CredentialDto>,
        val folders: List<FolderDto>,
        val tags: List<TagDto>,
        val credentialFolderReferences: List<CredentialFolderReferenceDto>,
        val credentialTagReferences: List<CredentialTagReferenceDto>,
        val attachments: List<AttachmentDto>,
        val passwordHistory: List<PasswordHistoryDto>,
        val attachmentsIncluded: Boolean,
    ) {
        fun entities(): VaultBackupEntities = VaultBackupEntities(
            metadata = metadata.toEntity(),
            credentials = credentials.map(CredentialDto::toEntity),
            folders = folders.map(FolderDto::toEntity),
            tags = tags.map(TagDto::toEntity),
            credentialFolderReferences = credentialFolderReferences.map(CredentialFolderReferenceDto::toEntity),
            credentialTagReferences = credentialTagReferences.map(CredentialTagReferenceDto::toEntity),
            attachments = attachments.map(AttachmentDto::toEntity),
            passwordHistory = passwordHistory.map(PasswordHistoryDto::toEntity),
        )

        companion object {
            fun from(snapshot: VaultBackupEntities, attachmentsIncluded: Boolean): SnapshotDto =
                SnapshotDto(
                    metadata = MetadataDto.from(snapshot.metadata),
                    credentials = snapshot.credentials.map(CredentialDto::from),
                    folders = snapshot.folders.map(FolderDto::from),
                    tags = snapshot.tags.map(TagDto::from),
                    credentialFolderReferences =
                        snapshot.credentialFolderReferences.map(CredentialFolderReferenceDto::from),
                    credentialTagReferences =
                        snapshot.credentialTagReferences.map(CredentialTagReferenceDto::from),
                    attachments = snapshot.attachments.map(AttachmentDto::from),
                    passwordHistory = snapshot.passwordHistory.map(PasswordHistoryDto::from),
                    attachmentsIncluded = attachmentsIncluded,
                )
        }
    }

    @Serializable
    private data class MetadataDto(
        val id: Int,
        val vaultFormatVersion: Int,
        val cryptoFormatVersion: Int,
        val vaultId: String,
        val argon2AlgorithmId: String,
        val argon2Salt: String,
        val argon2OpsLimit: Int,
        val argon2MemLimit: Int,
        val argon2Parallelism: Int,
        val wrappedVek: String,
        val vekNonce: String,
        val encryptedVerificationRecord: String,
        val verificationNonce: String,
        val createdAt: Long,
        val lastAccessedAt: Long?,
        val entryCount: Int,
    ) {
        fun toEntity() = VaultMetadataEntity(
            id = id,
            vaultFormatVersion = vaultFormatVersion,
            cryptoFormatVersion = cryptoFormatVersion,
            vaultId = vaultId,
            argon2AlgorithmId = argon2AlgorithmId,
            argon2Salt = argon2Salt.decodeBase64(MAX_SALT_BYTES),
            argon2OpsLimit = argon2OpsLimit,
            argon2MemLimit = argon2MemLimit,
            argon2Parallelism = argon2Parallelism,
            wrappedVek = wrappedVek.decodeBase64(MAX_PAYLOAD_BYTES),
            vekNonce = vekNonce.decodeBase64(MAX_NONCE_BYTES),
            encryptedVerificationRecord = encryptedVerificationRecord.decodeBase64(MAX_PAYLOAD_BYTES),
            verificationNonce = verificationNonce.decodeBase64(MAX_NONCE_BYTES),
            createdAt = createdAt,
            lastAccessedAt = lastAccessedAt,
            entryCount = entryCount,
        )

        companion object {
            fun from(value: VaultMetadataEntity) = MetadataDto(
                id = value.id,
                vaultFormatVersion = value.vaultFormatVersion,
                cryptoFormatVersion = value.cryptoFormatVersion,
                vaultId = value.vaultId,
                argon2AlgorithmId = value.argon2AlgorithmId,
                argon2Salt = value.argon2Salt.toBase64(),
                argon2OpsLimit = value.argon2OpsLimit,
                argon2MemLimit = value.argon2MemLimit,
                argon2Parallelism = value.argon2Parallelism,
                wrappedVek = value.wrappedVek.toBase64(),
                vekNonce = value.vekNonce.toBase64(),
                encryptedVerificationRecord = value.encryptedVerificationRecord.toBase64(),
                verificationNonce = value.verificationNonce.toBase64(),
                createdAt = value.createdAt,
                lastAccessedAt = value.lastAccessedAt,
                entryCount = value.entryCount,
            )
        }
    }

    @Serializable
    private data class CredentialDto(
        val id: String,
        val type: String,
        val titleHash: String,
        val summaryPayload: String,
        val summaryNonce: String,
        val secretPayload: String,
        val secretNonce: String,
        val folderId: String?,
        val isFavorite: Boolean,
        val createdAt: Long,
        val updatedAt: Long,
        val lastUsedAt: Long?,
    ) {
        fun toEntity() = CredentialRecordEntity(
            id = id,
            type = type,
            titleHash = titleHash.decodeBase64(MAX_HASH_BYTES),
            summaryPayload = summaryPayload.decodeBase64(MAX_PAYLOAD_BYTES),
            summaryNonce = summaryNonce.decodeBase64(MAX_NONCE_BYTES),
            secretPayload = secretPayload.decodeBase64(MAX_PAYLOAD_BYTES),
            secretNonce = secretNonce.decodeBase64(MAX_NONCE_BYTES),
            folderId = folderId,
            isFavorite = isFavorite,
            createdAt = createdAt,
            updatedAt = updatedAt,
            lastUsedAt = lastUsedAt,
        )

        companion object {
            fun from(value: CredentialRecordEntity) = CredentialDto(
                id = value.id,
                type = value.type,
                titleHash = value.titleHash.toBase64(),
                summaryPayload = value.summaryPayload.toBase64(),
                summaryNonce = value.summaryNonce.toBase64(),
                secretPayload = value.secretPayload.toBase64(),
                secretNonce = value.secretNonce.toBase64(),
                folderId = value.folderId,
                isFavorite = value.isFavorite,
                createdAt = value.createdAt,
                updatedAt = value.updatedAt,
                lastUsedAt = value.lastUsedAt,
            )
        }
    }

    @Serializable
    private data class FolderDto(
        val id: String,
        val parentId: String?,
        val nameHash: String,
        val encryptedPayload: String,
        val payloadNonce: String,
        val icon: String?,
        val sortOrder: Int,
        val createdAt: Long,
        val updatedAt: Long,
    ) {
        fun toEntity() = FolderRecordEntity(
            id = id,
            parentId = parentId,
            nameHash = nameHash.decodeBase64(MAX_HASH_BYTES),
            encryptedPayload = encryptedPayload.decodeBase64(MAX_PAYLOAD_BYTES),
            payloadNonce = payloadNonce.decodeBase64(MAX_NONCE_BYTES),
            icon = icon,
            sortOrder = sortOrder,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

        companion object {
            fun from(value: FolderRecordEntity) = FolderDto(
                id = value.id,
                parentId = value.parentId,
                nameHash = value.nameHash.toBase64(),
                encryptedPayload = value.encryptedPayload.toBase64(),
                payloadNonce = value.payloadNonce.toBase64(),
                icon = value.icon,
                sortOrder = value.sortOrder,
                createdAt = value.createdAt,
                updatedAt = value.updatedAt,
            )
        }
    }

    @Serializable
    private data class TagDto(
        val id: String,
        val nameHash: String,
        val encryptedPayload: String,
        val payloadNonce: String,
        val color: String?,
        val createdAt: Long,
    ) {
        fun toEntity() = TagRecordEntity(
            id = id,
            nameHash = nameHash.decodeBase64(MAX_HASH_BYTES),
            encryptedPayload = encryptedPayload.decodeBase64(MAX_PAYLOAD_BYTES),
            payloadNonce = payloadNonce.decodeBase64(MAX_NONCE_BYTES),
            color = color,
            createdAt = createdAt,
        )

        companion object {
            fun from(value: TagRecordEntity) = TagDto(
                id = value.id,
                nameHash = value.nameHash.toBase64(),
                encryptedPayload = value.encryptedPayload.toBase64(),
                payloadNonce = value.payloadNonce.toBase64(),
                color = value.color,
                createdAt = value.createdAt,
            )
        }
    }

    @Serializable
    private data class CredentialFolderReferenceDto(
        val credentialId: String,
        val folderId: String,
    ) {
        fun toEntity() = CredentialFolderCrossRef(credentialId, folderId)

        companion object {
            fun from(value: CredentialFolderCrossRef) =
                CredentialFolderReferenceDto(value.credentialId, value.folderId)
        }
    }

    @Serializable
    private data class CredentialTagReferenceDto(
        val credentialId: String,
        val tagId: String,
    ) {
        fun toEntity() = CredentialTagCrossRef(credentialId, tagId)

        companion object {
            fun from(value: CredentialTagCrossRef) =
                CredentialTagReferenceDto(value.credentialId, value.tagId)
        }
    }

    @Serializable
    private data class AttachmentDto(
        val id: String,
        val credentialId: String,
        val encryptedFilename: String,
        val filenameNonce: String,
        val mimeType: String,
        val sizeBytes: Long,
        val storagePath: String,
        val keyDerivationContext: String,
        val createdAt: Long,
    ) {
        fun toEntity() = AttachmentRecordEntity(
            id = id,
            credentialId = credentialId,
            encryptedFilename = encryptedFilename.decodeBase64(MAX_PAYLOAD_BYTES),
            filenameNonce = filenameNonce.decodeBase64(MAX_NONCE_BYTES),
            mimeType = mimeType,
            sizeBytes = sizeBytes,
            storagePath = storagePath,
            keyDerivationContext = keyDerivationContext,
            createdAt = createdAt,
        )

        companion object {
            fun from(value: AttachmentRecordEntity) = AttachmentDto(
                id = value.id,
                credentialId = value.credentialId,
                encryptedFilename = value.encryptedFilename.toBase64(),
                filenameNonce = value.filenameNonce.toBase64(),
                mimeType = value.mimeType,
                sizeBytes = value.sizeBytes,
                storagePath = value.storagePath,
                keyDerivationContext = value.keyDerivationContext,
                createdAt = value.createdAt,
            )
        }
    }

    @Serializable
    private data class PasswordHistoryDto(
        val id: String,
        val credentialId: String,
        val encryptedPassword: String,
        val passwordNonce: String,
        val changedAt: Long,
    ) {
        fun toEntity() = PasswordHistoryRecordEntity(
            id = id,
            credentialId = credentialId,
            encryptedPassword = encryptedPassword.decodeBase64(MAX_PAYLOAD_BYTES),
            passwordNonce = passwordNonce.decodeBase64(MAX_NONCE_BYTES),
            changedAt = changedAt,
        )

        companion object {
            fun from(value: PasswordHistoryRecordEntity) = PasswordHistoryDto(
                id = value.id,
                credentialId = value.credentialId,
                encryptedPassword = value.encryptedPassword.toBase64(),
                passwordNonce = value.passwordNonce.toBase64(),
                changedAt = value.changedAt,
            )
        }
    }

}
