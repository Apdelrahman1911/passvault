@file:OptIn(kotlin.io.encoding.ExperimentalEncodingApi::class)

package com.passvault.core.database.backup

import com.passvault.core.crypto.CryptoEnvelope
import com.passvault.core.crypto.CryptoEngine
import com.passvault.core.crypto.DerivedKey
import com.passvault.core.crypto.Argon2Parameters
import com.passvault.core.database.dao.VaultBackupDao
import com.passvault.core.database.dao.VaultBackupEntities
import com.passvault.core.database.VaultDatabase
import com.passvault.core.database.attachment.AttachmentBlobStore
import com.passvault.core.database.attachment.AttachmentLifecycleManager
import com.passvault.core.database.attachment.DatabaseOnlyAttachmentLifecycleManager
import com.passvault.core.database.attachment.AttachmentStorageKind
import com.passvault.core.database.attachment.requireStableStorageKind
import com.passvault.core.database.entity.AttachmentRecordEntity
import com.passvault.core.domain.repository.AttachmentPolicy
import com.passvault.core.database.entity.CredentialFolderCrossRef
import com.passvault.core.database.entity.CredentialRecordEntity
import com.passvault.core.database.entity.CredentialTagCrossRef
import com.passvault.core.database.entity.FolderRecordEntity
import com.passvault.core.database.entity.PasswordHistoryRecordEntity
import com.passvault.core.database.entity.TagRecordEntity
import com.passvault.core.database.entity.VaultMetadataEntity
import com.passvault.core.database.repository.VaultSessionManager
import com.passvault.core.database.repository.decodeUtf8Strict
import com.passvault.core.database.repository.hasAtMostCodePoints
import com.passvault.core.database.repository.hasOnlySafeTextCodePoints
import com.passvault.core.database.repository.MAX_ATTACHMENT_FILENAME_ENCRYPTED_PAYLOAD_BYTES
import com.passvault.core.database.repository.MAX_CREDENTIAL_ENCRYPTED_PAYLOAD_BYTES
import com.passvault.core.database.repository.MAX_FOLDER_ENCRYPTED_PAYLOAD_BYTES
import com.passvault.core.database.repository.MAX_PASSWORD_HISTORY_ENCRYPTED_PAYLOAD_BYTES
import com.passvault.core.database.repository.MAX_TAG_ENCRYPTED_PAYLOAD_BYTES
import com.passvault.core.domain.model.BackupPasswordPolicy
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.repository.LockReason
import com.passvault.core.domain.repository.VaultRepository
import com.passvault.core.security.BiometricKeyStore
import com.passvault.core.security.UnavailableBiometricKeyStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

private const val BACKUP_FORMAT_VERSION = 1
private const val KDF_ALGORITHM = "Argon2id"
private const val MIN_SUPPORTED_VAULT_FORMAT_VERSION = 1
private const val MAX_SUPPORTED_VAULT_FORMAT_VERSION = 2
private const val SUPPORTED_CRYPTO_FORMAT_VERSION = 2
private const val ARGON2_SALT_BYTES = 16
private const val XCHACHA_NONCE_BYTES = 24
private const val BACKUP_AAD = "passvault:backup:v1"
private const val MAX_SNAPSHOT_BYTES = BackupLimits.LEGACY_MAX_SNAPSHOT_BYTES
private const val BLIND_INDEX_BYTES = 32
private const val MAX_SALT_BYTES = 64
private const val MAX_NONCE_BYTES = 64
private const val MIN_ENCRYPTED_BYTES = 4 + 16
private const val FIXED_SECRET_BYTES = 32
private const val AEAD_MAGIC_BYTES = 4
private const val AEAD_TAG_BYTES = 16
private const val FIXED_SECRET_ENVELOPE_BYTES = AEAD_MAGIC_BYTES + FIXED_SECRET_BYTES + AEAD_TAG_BYTES
private const val MAX_FIXED_SECRET_ENVELOPE_BYTES = FIXED_SECRET_ENVELOPE_BYTES + AEAD_TAG_BYTES
private const val MAX_ENTITY_COUNT = BackupLimits.MAX_ENTITY_COUNT
private const val MAX_IDENTIFIER_LENGTH = 256
private const val MAX_TEXT_LENGTH = 16 * 1024
private const val MAX_SHORT_METADATA_LENGTH = 64
private const val MAX_ATTACHMENT_SIZE_BYTES = 4L * 1024L * 1024L * 1024L
private const val MAX_TAGS_PER_CREDENTIAL = 100
private const val MAX_PASSWORD_HISTORY_PER_CREDENTIAL = 10
private const val MIN_ARGON2_OPS = 2
private const val MAX_ARGON2_OPS = 10
private const val MIN_ARGON2_MEM = 32 * 1024 * 1024
private const val MAX_ARGON2_MEM = 256 * 1024 * 1024
private const val SUPPORTED_ARGON2_PARALLELISM = 1
private const val BACKUP_INVALID_MESSAGE = "The backup password is incorrect or the backup is corrupt."
private const val BACKUP_RESTORE_FAILED_MESSAGE = "The validated backup could not be restored."
private const val DEFAULT_STREAM_BUFFER_BYTES = 64 * 1024
private const val CUSTOM_CREDENTIAL_PREFIX = "Custom:"
private val SUPPORTED_CREDENTIAL_TYPES = setOf(
    "Login",
    "SecureNote",
    "ApiKey",
    "LicenseKey",
    "RecoveryCodes",
    "WiFiCredential",
    "Identity",
    "PaymentCard",
)
private val MANAGED_ATTACHMENT_PATH_REGEX =
    Regex("^objects/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.pva$")

private fun String.decodeBase64(maxBytes: Int): ByteArray {
    require(length <= maxBytes * 2)
    val decoded = Base64.decode(this)
    require(decoded.size <= maxBytes)
    return decoded
}

private fun ByteArray.toBase64(): String = Base64.encode(this)

internal fun validateBackupAttachmentAccounting(
    attachmentsIncluded: Boolean,
    attachmentRowCount: Int,
    omittedAttachmentCount: Int,
) {
    require(!attachmentsIncluded) {
        "This backup format does not package attachment files"
    }
    require(attachmentRowCount == 0) {
        "Metadata-only backups must not contain unusable attachment rows"
    }
    require(omittedAttachmentCount in 0..MAX_ENTITY_COUNT)
}

private fun String.isValidIdentifier(): Boolean =
    isNotBlank() &&
        hasAtMostCodePoints(MAX_IDENTIFIER_LENGTH) &&
        hasOnlySafeTextCodePoints() &&
        all { it != '/' && it != '\\' }

/**
 * Creates and restores an authenticated, versioned backup of the encrypted
 * vault records. The backup password is independent from the vault password.
 *
 * No decrypted credential value crosses this boundary. The database already
 * stores authenticated encrypted payloads, so the complete raw snapshot is
 * wrapped once more with a key derived from the backup password.
 */
@OptIn(ExperimentalEncodingApi::class)
@Suppress(
    "LargeClass",
    "TooManyFunctions",
) // Legacy-v1 compatibility keeps its strict DTO/validation boundary isolated in this service.
class VaultBackupService(
    private val backupDao: VaultBackupDao,
    private val cryptoEngine: CryptoEngine,
    private val vaultRepository: VaultRepository,
    private val sessionManager: VaultSessionManager,
    private val biometricKeyStore: BiometricKeyStore = UnavailableBiometricKeyStore(),
    private val attachmentBlobStore: AttachmentBlobStore? = null,
    private val attachmentLifecycleManager: AttachmentLifecycleManager =
        DatabaseOnlyAttachmentLifecycleManager,
    private val database: VaultDatabase? = null,
) {
    private val operationMutex = Mutex()

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = false
        explicitNulls = true
    }

    private val v2Service by lazy {
        VaultBackupV2Service(
            backupDao = backupDao,
            database = requireNotNull(database) {
                "The Room database is required for streaming backups"
            },
            cryptoEngine = cryptoEngine,
            sessionManager = sessionManager,
            blobStore = requireNotNull(attachmentBlobStore) {
                "Attachment storage is required for streaming backups"
            },
            attachmentLifecycleManager = attachmentLifecycleManager,
            newValidator = ::newStreamValidator,
            activateRestore = ::activateStreamingRestore,
        )
    }

    suspend fun createBackup(
        password: SensitiveText,
        sink: BackupContentSink,
        onProgress: (Int) -> Unit = {},
    ): Result<BackupInspection> = operationMutex.withLock {
        v2Service.create(password, sink, onProgress)
    }

    suspend fun inspectBackup(
        source: BackupContentSource,
        password: SensitiveText,
    ): Result<BackupInspection> = operationMutex.withLock {
        inspectStreamingSource(source, password)
    }

    suspend fun restoreBackup(
        source: BackupContentSource,
        password: SensitiveText,
        onProgress: (Int) -> Unit = {},
    ): Result<BackupInspection> = operationMutex.withLock {
        restoreStreamingSource(source, password, onProgress)
    }

    suspend fun createBackup(
        password: SensitiveText,
        includeAttachments: Boolean = false,
    ): Result<ByteArray> = operationMutex.withLock {
        if (!BackupPasswordPolicy.acceptsNew(password)) {
            return@withLock Result.failure(
                IllegalArgumentException("Backup password length is invalid"),
            )
        }
        if (includeAttachments) {
            return@withLock Result.failure(
                IllegalArgumentException(
                    "Attachment files cannot be packaged by this build. " +
                        "Create a metadata-only backup instead.",
                ),
            )
        }

        return try {
            sessionManager.withUnlockedSession {
                val rawSnapshot = backupDao.readSnapshot()
                val snapshotWithDerivedCount = rawSnapshot.copy(
                    metadata = rawSnapshot.metadata.copy(entryCount = rawSnapshot.credentials.size),
                )
                validateSnapshot(snapshotWithDerivedCount)
                val snapshot = canonicalizeFolderRelationships(snapshotWithDerivedCount)
                val payload = SnapshotDto.from(snapshot, attachmentsIncluded = false)
                val plaintext = json.encodeToString(payload).encodeToByteArray()
                try {
                    require(plaintext.size <= MAX_SNAPSHOT_BYTES)
                    encryptPayload(plaintext, password)
                } finally {
                    cryptoEngine.secureWipe(plaintext)
                }
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
        val source = ByteArrayBackupContentSource(bytes)
        try {
            inspectLegacyBackup(LegacyBackupEnvelopeReader(source, ByteArray(0)).read(), password)
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (_: Exception) {
            Result.failure(IllegalArgumentException(BACKUP_INVALID_MESSAGE))
        }
    }

    private suspend fun inspectLegacyBackup(
        envelope: LegacyBackupEnvelope,
        password: SensitiveText,
    ): Result<BackupInspection> =
        try {
            sessionManager.withUnlockedSession {
                val snapshot = decryptPayload(envelope, password)
                snapshot.validateAttachmentAccounting()
                validateSnapshot(snapshot.validationEntities())
                Result.success(
                    BackupInspection(
                        credentialCount = snapshot.credentials.size,
                        folderCount = snapshot.folders.size,
                        tagCount = snapshot.tags.size,
                        attachmentCount = snapshot.reportedAttachmentCount,
                        warnings = buildList {
                            if (!snapshot.attachmentsIncluded && snapshot.reportedAttachmentCount > 0) {
                                add(BackupWarning.ATTACHMENT_FILES_NOT_INCLUDED_IN_PREVIEW)
                            }
                        },
                    ),
                )
            }
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (_: Exception) {
            Result.failure(IllegalArgumentException(BACKUP_INVALID_MESSAGE))
        } finally {
            envelope.clear()
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
        val source = ByteArrayBackupContentSource(bytes)
        try {
            restoreLegacyBackup(LegacyBackupEnvelopeReader(source, ByteArray(0)).read(), password)
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (_: Exception) {
            Result.failure(IllegalArgumentException(BACKUP_INVALID_MESSAGE))
        }
    }

    private suspend fun restoreLegacyBackup(
        envelope: LegacyBackupEnvelope,
        password: SensitiveText,
    ): Result<BackupInspection> {
        val (snapshot, entities) = try {
            val snapshot = decryptPayload(envelope, password)
            snapshot.validateAttachmentAccounting()
            validateSnapshot(snapshot.validationEntities())
            val rawEntities = snapshot.restorableEntities()
            // The direct credential_records.folder_id column is the canonical
            // relationship. Older schemas also populated a redundant
            // cross-reference table, so rebuild that table from the
            // authoritative column during restore.
            snapshot to canonicalizeFolderRelationships(rawEntities)
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (_: Exception) {
            return Result.failure(IllegalArgumentException(BACKUP_INVALID_MESSAGE))
        } finally {
            envelope.clear()
        }

        return try {
            val attachmentCleanupSucceeded = activateSnapshot(entities)

            Result.success(
                BackupInspection(
                    credentialCount = entities.credentials.size,
                    folderCount = entities.folders.size,
                    tagCount = entities.tags.size,
                    attachmentCount = snapshot.reportedAttachmentCount,
                    warnings = buildList {
                        if (!snapshot.attachmentsIncluded && snapshot.reportedAttachmentCount > 0) {
                            add(BackupWarning.ATTACHMENT_FILES_NOT_INCLUDED_AFTER_RESTORE)
                        }
                        if (!attachmentCleanupSucceeded) {
                            add(BackupWarning.OBSOLETE_ATTACHMENT_CLEANUP_FAILED)
                        }
                    },
                ),
            )
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (_: Exception) {
            // The Room transaction rolls back on any insert/FK failure. Keep
            // implementation details private without misreporting a validated
            // file as an incorrect password or corrupt backup.
            Result.failure(IllegalStateException(BACKUP_RESTORE_FAILED_MESSAGE))
        }
    }

    private suspend fun inspectStreamingSource(
        source: BackupContentSource,
        password: SensitiveText,
    ): Result<BackupInspection> {
        var prefix: ByteArray? = null
        return try {
            prefix = source.readPrefix(BACKUP_V2_MAGIC.size)
            if (prefix.contentEquals(BACKUP_V2_MAGIC)) {
                v2Service.inspectAfterMagic(source, password, prefix).also { prefix = null }
            } else {
                val legacy = LegacyBackupEnvelopeReader(source, prefix).read()
                prefix = null
                try {
                    inspectLegacyBackup(legacy, password)
                } finally {
                    source.close()
                }
            }
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (_: Exception) {
            runCatching { source.close() }
            Result.failure(IllegalArgumentException(BACKUP_INVALID_MESSAGE))
        } finally {
            prefix?.let(cryptoEngine::secureWipe)
        }
    }

    private suspend fun restoreStreamingSource(
        source: BackupContentSource,
        password: SensitiveText,
        onProgress: (Int) -> Unit,
    ): Result<BackupInspection> {
        var prefix: ByteArray? = null
        return try {
            prefix = source.readPrefix(BACKUP_V2_MAGIC.size)
            if (prefix.contentEquals(BACKUP_V2_MAGIC)) {
                v2Service.restoreAfterMagic(source, password, prefix, onProgress).also { prefix = null }
            } else {
                val legacy = LegacyBackupEnvelopeReader(source, prefix).read()
                prefix = null
                try {
                    restoreLegacyBackup(legacy, password)
                } finally {
                    source.close()
                }
            }
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (_: Exception) {
            runCatching { source.close() }
            Result.failure(IllegalArgumentException(BACKUP_INVALID_MESSAGE))
        } finally {
            prefix?.let(cryptoEngine::secureWipe)
        }
    }

    private suspend fun BackupContentSource.readPrefix(size: Int): ByteArray {
        val prefix = ByteArray(size)
        var offset = 0
        while (offset < size) {
            val buffer = ByteArray(size - offset)
            try {
                val count = read(buffer)
                if (count == -1) return prefix.copyOf(offset).also { cryptoEngine.secureWipe(prefix) }
                require(count in 1..buffer.size)
                buffer.copyInto(prefix, destinationOffset = offset, endIndex = count)
                offset += count
            } finally {
                cryptoEngine.secureWipe(buffer)
            }
        }
        return prefix
    }

    @Suppress("TooGenericExceptionCaught") // Convert arbitrary crypto/encoding failures while preserving cancellation.
    private suspend fun encryptPayload(
        plaintext: ByteArray,
        password: SensitiveText,
    ): Result<ByteArray> {
        require(BackupPasswordPolicy.acceptsNew(password))
        var passwordBytes: ByteArray? = null
        var salt: ByteArray? = null
        var derivedKey: DerivedKey? = null
        var encrypted: com.passvault.core.crypto.EncryptedData? = null
        return try {
            val parameters = cryptoEngine.benchmarkArgon2().safeForBackup()
            salt = cryptoEngine.generateRandom(ARGON2_SALT_BYTES).getOrThrow()
            passwordBytes = password.toUtf8ByteArray()
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
                argon2Parallelism = SUPPORTED_ARGON2_PARALLELISM,
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
        envelope: LegacyBackupEnvelope,
        password: SensitiveText,
    ): SnapshotDto {
        require(BackupPasswordPolicy.acceptsExisting(password))
        require(envelope.formatVersion == BACKUP_FORMAT_VERSION)
        require(envelope.kdfAlgorithm == KDF_ALGORITHM)
        require(envelope.argon2OpsLimit in MIN_ARGON2_OPS..MAX_ARGON2_OPS)
        require(envelope.argon2MemLimit in MIN_ARGON2_MEM..MAX_ARGON2_MEM)
        require(envelope.argon2Parallelism == SUPPORTED_ARGON2_PARALLELISM)

        val salt = envelope.salt
        val nonce = envelope.nonce
        val ciphertext = envelope.ciphertext
        require(salt.size == ARGON2_SALT_BYTES)
        require(nonce.size == XCHACHA_NONCE_BYTES)
        require(ciphertext.size >= MIN_ENCRYPTED_BYTES)

        var passwordBytes: ByteArray? = null
        var derivedKey: DerivedKey? = null
        var plaintext: ByteArray? = null
        try {
            passwordBytes = password.toUtf8ByteArray()
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
            require(plaintext.size <= MAX_SNAPSHOT_BYTES)
            return json.decodeFromString<SnapshotDto>(plaintext.decodeUtf8Strict())
        } finally {
            passwordBytes?.let { cryptoEngine.secureWipe(it) }
            derivedKey?.clear()
            plaintext?.let { cryptoEngine.secureWipe(it) }
        }
    }

    internal fun validateSnapshot(snapshot: VaultBackupEntities) {
        validateMetadata(snapshot.metadata, snapshot.credentials.size)
        validateEntityCounts(snapshot)

        val credentialSet = validatedIdentifierSet(snapshot.credentials.map(CredentialRecordEntity::id))
        val folderSet = validatedIdentifierSet(snapshot.folders.map(FolderRecordEntity::id))
        val tagSet = validatedIdentifierSet(snapshot.tags.map(TagRecordEntity::id))

        validateCredentials(snapshot.credentials, folderSet)
        validateFolders(snapshot.folders, folderSet)
        validateTags(snapshot.tags)
        validateReferences(snapshot, credentialSet, folderSet, tagSet)
        validateAttachments(snapshot.attachments, credentialSet)
        validatePasswordHistory(snapshot.passwordHistory, credentialSet)
    }

    private fun validateMetadata(metadata: VaultMetadataEntity, credentialCount: Int) {
        require(metadata.id == 1)
        require(
            metadata.vaultFormatVersion in
                MIN_SUPPORTED_VAULT_FORMAT_VERSION..MAX_SUPPORTED_VAULT_FORMAT_VERSION,
        )
        require(metadata.cryptoFormatVersion == SUPPORTED_CRYPTO_FORMAT_VERSION)
        require(metadata.vaultId.isValidIdentifier())
        require(metadata.argon2AlgorithmId == KDF_ALGORITHM)
        require(metadata.argon2Salt.size == ARGON2_SALT_BYTES)
        require(metadata.argon2OpsLimit in MIN_ARGON2_OPS..MAX_ARGON2_OPS)
        require(metadata.argon2MemLimit in MIN_ARGON2_MEM..MAX_ARGON2_MEM)
        require(metadata.argon2Parallelism == SUPPORTED_ARGON2_PARALLELISM)
        requireFixedSecretPayload(metadata.wrappedVek)
        require(metadata.vekNonce.size == XCHACHA_NONCE_BYTES)
        requireFixedSecretPayload(metadata.encryptedVerificationRecord)
        require(metadata.verificationNonce.size == XCHACHA_NONCE_BYTES)
        require(metadata.entryCount >= 0)
        require(metadata.entryCount == credentialCount)
    }

    private fun validateEntityCounts(snapshot: VaultBackupEntities) {
        require(snapshot.credentials.size <= MAX_ENTITY_COUNT)
        require(snapshot.folders.size <= MAX_ENTITY_COUNT)
        require(snapshot.tags.size <= MAX_ENTITY_COUNT)
        require(snapshot.credentialFolderReferences.size <= MAX_ENTITY_COUNT)
        require(snapshot.credentialTagReferences.size <= MAX_ENTITY_COUNT)
        require(snapshot.attachments.size <= MAX_ENTITY_COUNT)
        require(snapshot.passwordHistory.size <= MAX_ENTITY_COUNT)
    }

    private fun validatedIdentifierSet(ids: List<String>): Set<String> {
        require(ids.all(String::isValidIdentifier))
        return ids.toSet().also { require(it.size == ids.size) }
    }

    private fun validateCredentials(credentials: List<CredentialRecordEntity>, folderIds: Set<String>) {
        credentials.forEach { credential ->
            require(credential.type.isSupportedCredentialType())
            requirePayload(
                credential.summaryPayload,
                credential.summaryNonce,
                MAX_CREDENTIAL_ENCRYPTED_PAYLOAD_BYTES,
            )
            requirePayload(
                credential.secretPayload,
                credential.secretNonce,
                MAX_CREDENTIAL_ENCRYPTED_PAYLOAD_BYTES,
            )
            require(credential.folderId == null || credential.folderId in folderIds)
        }
    }

    private fun validateFolders(folders: List<FolderRecordEntity>, folderIds: Set<String>) {
        folders.forEach { folder ->
            require(folder.parentId == null || folder.parentId in folderIds)
            require(folder.nameHash.size == BLIND_INDEX_BYTES)
            requirePayload(folder.encryptedPayload, folder.payloadNonce, MAX_FOLDER_ENCRYPTED_PAYLOAD_BYTES)
            require(folder.icon.isSafeOptionalMetadataText(MAX_SHORT_METADATA_LENGTH))
            require(folder.sortOrder >= 0)
        }
        validateFolderCycles(folders)
    }

    private fun validateTags(tags: List<TagRecordEntity>) {
        tags.forEach { tag ->
            require(tag.nameHash.size == BLIND_INDEX_BYTES)
            requirePayload(tag.encryptedPayload, tag.payloadNonce, MAX_TAG_ENCRYPTED_PAYLOAD_BYTES)
            require(tag.color.isSafeOptionalMetadataText(MAX_SHORT_METADATA_LENGTH))
        }
    }

    private fun validateReferences(
        snapshot: VaultBackupEntities,
        credentialIds: Set<String>,
        folderIds: Set<String>,
        tagIds: Set<String>,
    ) {
        val folderReferences = snapshot.credentialFolderReferences.map { reference ->
            require(reference.credentialId in credentialIds)
            require(reference.folderId in folderIds)
            "${reference.credentialId}\u0000${reference.folderId}"
        }
        require(folderReferences.size == folderReferences.toSet().size)

        val tagCounts = mutableMapOf<String, Int>()
        val tagReferences = snapshot.credentialTagReferences.map { reference ->
            require(reference.credentialId in credentialIds)
            require(reference.tagId in tagIds)
            val count = tagCounts.getOrElse(reference.credentialId) { 0 } + 1
            require(count <= MAX_TAGS_PER_CREDENTIAL)
            tagCounts[reference.credentialId] = count
            "${reference.credentialId}\u0000${reference.tagId}"
        }
        require(tagReferences.size == tagReferences.toSet().size)
    }

    private fun validateAttachments(attachments: List<AttachmentRecordEntity>, credentialIds: Set<String>) {
        val readyCounts = mutableMapOf<String, Int>()
        val readyBytes = mutableMapOf<String, Long>()
        val attachmentIds = attachments.map { attachment ->
            require(attachment.credentialId in credentialIds)
            require(attachment.id.isValidIdentifier())
            requirePayload(
                attachment.encryptedFilename,
                attachment.filenameNonce,
                MAX_ATTACHMENT_FILENAME_ENCRYPTED_PAYLOAD_BYTES,
            )
            require(attachment.mimeType.isSafeMetadataText())
            require(attachment.storagePath.isSafeRelativePath())
            require(attachment.keyDerivationContext.isValidIdentifier())
            when (attachment.requireStableStorageKind()) {
                AttachmentStorageKind.MANAGED -> {
                    require(attachment.sizeBytes in 0..AttachmentPolicy.MAX_FILE_SIZE_BYTES)
                    require(attachment.storagePath.matches(MANAGED_ATTACHMENT_PATH_REGEX))
                    val count = readyCounts.getOrElse(attachment.credentialId) { 0 } + 1
                    require(count <= AttachmentPolicy.MAX_ATTACHMENTS_PER_CREDENTIAL)
                    readyCounts[attachment.credentialId] = count
                    val totalBytes = readyBytes.getOrElse(attachment.credentialId) { 0L } + attachment.sizeBytes
                    require(totalBytes <= AttachmentPolicy.MAX_TOTAL_SIZE_PER_CREDENTIAL_BYTES)
                    readyBytes[attachment.credentialId] = totalBytes
                }
                AttachmentStorageKind.LEGACY -> {
                    require(attachment.sizeBytes in 0..MAX_ATTACHMENT_SIZE_BYTES)
                }
            }
            attachment.id
        }
        require(attachmentIds.size == attachmentIds.toSet().size)
    }

    private fun validatePasswordHistory(
        passwordHistory: List<PasswordHistoryRecordEntity>,
        credentialIds: Set<String>,
    ) {
        val historyCounts = mutableMapOf<String, Int>()
        val historyIds = passwordHistory.map { history ->
            require(history.credentialId in credentialIds)
            require(history.id.isValidIdentifier())
            requirePayload(
                history.encryptedPassword,
                history.passwordNonce,
                MAX_PASSWORD_HISTORY_ENCRYPTED_PAYLOAD_BYTES,
            )
            val count = historyCounts.getOrElse(history.credentialId) { 0 } + 1
            require(count <= MAX_PASSWORD_HISTORY_PER_CREDENTIAL)
            historyCounts[history.credentialId] = count
            history.id
        }
        require(historyIds.size == historyIds.toSet().size)
    }

    internal fun canonicalizeFolderRelationships(snapshot: VaultBackupEntities): VaultBackupEntities {
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

    internal fun newStreamValidator(
        manifest: BackupStreamManifest,
        retainedIdentifierBytes: Long = BackupLimits.MAX_RETAINED_IDENTIFIER_BYTES,
    ): BackupStreamValidator = StreamingValidator(
        manifest = manifest,
        identifierBudget = RetainedIdentifierBudget(retainedIdentifierBytes),
    )

    /**
     * Validates one encrypted Room row at a time. Only identifiers, relationship
     * sets, and per-credential counters survive between records; ciphertext
     * payload arrays can be wiped immediately by the caller.
     */
    private inner class StreamingValidator(
        override val manifest: BackupStreamManifest,
        private val identifierBudget: RetainedIdentifierBudget,
    ) : BackupStreamValidator {
        private var metadataCount = 0
        private var credentialCount = 0
        private var folderCount = 0
        private var tagCount = 0
        private var folderReferenceCount = 0
        private var tagReferenceCount = 0
        private var attachmentCount = 0
        private var passwordHistoryCount = 0
        private val folderIds = mutableSetOf<String>()
        private val folderParents = mutableMapOf<String, String?>()
        private val tagIds = mutableSetOf<String>()
        private val credentialIds = mutableSetOf<String>()
        private val credentialFolders = mutableMapOf<String, String>()
        private val folderReferenceCredentialIds = mutableSetOf<String>()
        private val tagReferences = mutableSetOf<Pair<String, String>>()
        private val tagReferenceCounts = mutableMapOf<String, Int>()
        private val attachmentIds = mutableSetOf<String>()
        private val readyAttachmentCounts = mutableMapOf<String, Int>()
        private val readyAttachmentBytes = mutableMapOf<String, Long>()
        private val managedAttachmentIds = mutableListOf<String>()
        private val passwordHistoryIds = mutableSetOf<String>()
        private val passwordHistoryCounts = mutableMapOf<String, Int>()

        init {
            listOf(
                manifest.credentialCount,
                manifest.folderCount,
                manifest.tagCount,
                manifest.credentialFolderReferenceCount,
                manifest.credentialTagReferenceCount,
                manifest.attachmentCount,
                manifest.managedAttachmentCount,
                manifest.passwordHistoryCount,
            ).forEach { require(it in 0..MAX_ENTITY_COUNT) }
            require(manifest.credentialFolderReferenceCount <= manifest.credentialCount)
            require(manifest.managedAttachmentCount <= manifest.attachmentCount)
            require(
                manifest.credentialTagReferenceCount.toLong() <=
                    manifest.credentialCount.toLong() * MAX_TAGS_PER_CREDENTIAL,
            )
            require(
                manifest.managedAttachmentCount.toLong() <=
                    manifest.credentialCount.toLong() * AttachmentPolicy.MAX_ATTACHMENTS_PER_CREDENTIAL,
            )
            require(
                manifest.passwordHistoryCount.toLong() <=
                    manifest.credentialCount.toLong() * MAX_PASSWORD_HISTORY_PER_CREDENTIAL,
            )
            manifest.requireRetentionBound()
            if (manifest.metadataSchemaVersion >= STORAGE_ACCOUNTING_METADATA_SCHEMA_VERSION) {
                val objectBytes = requireNotNull(manifest.managedAttachmentObjectBytes)
                require(objectBytes in 0..BackupLimits.MAX_BACKUP_BYTES)
                require((manifest.managedAttachmentCount == 0) == (objectBytes == 0L))
            } else {
                require(manifest.managedAttachmentObjectBytes == null)
            }
        }

        override fun accept(value: BackupMetadataValue) {
            when (value) {
                is BackupMetadataValue.Metadata -> acceptMetadata(value.value)
                is BackupMetadataValue.Folder -> acceptFolder(value.value)
                is BackupMetadataValue.Tag -> acceptTag(value.value)
                is BackupMetadataValue.Credential -> acceptCredential(value.value)
                is BackupMetadataValue.CredentialFolderReference -> acceptFolderReference(value.value)
                is BackupMetadataValue.CredentialTagReference -> acceptTagReference(value.value)
                is BackupMetadataValue.Attachment -> acceptAttachment(value.value)
                is BackupMetadataValue.PasswordHistory -> acceptPasswordHistory(value.value)
            }
        }

        override fun finish(): ValidatedBackupStream {
            require(metadataCount == 1)
            require(credentialCount == manifest.credentialCount)
            require(folderCount == manifest.folderCount)
            require(tagCount == manifest.tagCount)
            require(folderReferenceCount == manifest.credentialFolderReferenceCount)
            require(tagReferenceCount == manifest.credentialTagReferenceCount)
            require(attachmentCount == manifest.attachmentCount)
            require(managedAttachmentIds.size == manifest.managedAttachmentCount)
            require(passwordHistoryCount == manifest.passwordHistoryCount)
            require(folderParents.values.all { it == null || it in folderIds })
            require(folderReferenceCredentialIds == credentialFolders.keys)
            validateFolderParentMap(folderParents)
            return ValidatedBackupStream(manifest, managedAttachmentIds.toList())
        }

        private fun acceptMetadata(value: VaultMetadataEntity) {
            require(metadataCount++ == 0)
            validateMetadata(value, manifest.credentialCount)
        }

        private fun acceptFolder(value: FolderRecordEntity) {
            require(++folderCount <= manifest.folderCount)
            require(value.id.isValidIdentifier())
            identifierBudget.retain(value.id, value.parentId)
            require(folderIds.add(value.id))
            require(value.parentId == null || value.parentId.isValidIdentifier())
            require(value.nameHash.size == BLIND_INDEX_BYTES)
            requirePayload(value.encryptedPayload, value.payloadNonce, MAX_FOLDER_ENCRYPTED_PAYLOAD_BYTES)
            require(value.icon.isSafeOptionalMetadataText(MAX_SHORT_METADATA_LENGTH))
            require(value.sortOrder >= 0)
            folderParents[value.id] = value.parentId
        }

        private fun acceptTag(value: TagRecordEntity) {
            require(++tagCount <= manifest.tagCount)
            require(value.id.isValidIdentifier())
            identifierBudget.retain(value.id)
            require(tagIds.add(value.id))
            require(value.nameHash.size == BLIND_INDEX_BYTES)
            requirePayload(value.encryptedPayload, value.payloadNonce, MAX_TAG_ENCRYPTED_PAYLOAD_BYTES)
            require(value.color.isSafeOptionalMetadataText(MAX_SHORT_METADATA_LENGTH))
        }

        private fun acceptCredential(value: CredentialRecordEntity) {
            require(++credentialCount <= manifest.credentialCount)
            require(value.id.isValidIdentifier())
            identifierBudget.retain(value.id, value.folderId)
            require(credentialIds.add(value.id))
            require(value.type.isSupportedCredentialType())
            requirePayload(
                value.summaryPayload,
                value.summaryNonce,
                MAX_CREDENTIAL_ENCRYPTED_PAYLOAD_BYTES,
            )
            requirePayload(
                value.secretPayload,
                value.secretNonce,
                MAX_CREDENTIAL_ENCRYPTED_PAYLOAD_BYTES,
            )
            require(value.folderId == null || value.folderId in folderIds)
            value.folderId?.let { credentialFolders[value.id] = it }
        }

        private fun acceptFolderReference(value: CredentialFolderCrossRef) {
            require(++folderReferenceCount <= manifest.credentialFolderReferenceCount)
            identifierBudget.retain(value.credentialId, value.folderId)
            require(value.credentialId in credentialIds)
            require(value.folderId in folderIds)
            require(credentialFolders[value.credentialId] == value.folderId)
            require(folderReferenceCredentialIds.add(value.credentialId))
        }

        private fun acceptTagReference(value: CredentialTagCrossRef) {
            require(++tagReferenceCount <= manifest.credentialTagReferenceCount)
            identifierBudget.retain(value.credentialId, value.tagId)
            require(value.credentialId in credentialIds)
            require(value.tagId in tagIds)
            require(tagReferences.add(value.credentialId to value.tagId))
            val perCredential = tagReferenceCounts.getOrElse(value.credentialId) { 0 } + 1
            require(perCredential <= MAX_TAGS_PER_CREDENTIAL)
            tagReferenceCounts[value.credentialId] = perCredential
        }

        private fun acceptAttachment(value: AttachmentRecordEntity) {
            require(++attachmentCount <= manifest.attachmentCount)
            identifierBudget.retain(value.id, value.credentialId)
            require(value.credentialId in credentialIds)
            require(value.id.isValidIdentifier())
            require(attachmentIds.add(value.id))
            requirePayload(
                value.encryptedFilename,
                value.filenameNonce,
                MAX_ATTACHMENT_FILENAME_ENCRYPTED_PAYLOAD_BYTES,
            )
            require(value.mimeType.isSafeMetadataText())
            require(value.storagePath.isSafeRelativePath())
            require(value.keyDerivationContext.isValidIdentifier())
            when (value.requireStableStorageKind()) {
                AttachmentStorageKind.MANAGED -> {
                    require(value.sizeBytes in 0..AttachmentPolicy.MAX_FILE_SIZE_BYTES)
                    require(value.storagePath.matches(MANAGED_ATTACHMENT_PATH_REGEX))
                    val count = readyAttachmentCounts.getOrElse(value.credentialId) { 0 } + 1
                    require(count <= AttachmentPolicy.MAX_ATTACHMENTS_PER_CREDENTIAL)
                    readyAttachmentCounts[value.credentialId] = count
                    val bytes = readyAttachmentBytes.getOrElse(value.credentialId) { 0L } + value.sizeBytes
                    require(bytes <= AttachmentPolicy.MAX_TOTAL_SIZE_PER_CREDENTIAL_BYTES)
                    readyAttachmentBytes[value.credentialId] = bytes
                    managedAttachmentIds += value.id
                }
                AttachmentStorageKind.LEGACY -> {
                    require(value.sizeBytes in 0..MAX_ATTACHMENT_SIZE_BYTES)
                }
            }
        }

        private fun acceptPasswordHistory(value: PasswordHistoryRecordEntity) {
            require(++passwordHistoryCount <= manifest.passwordHistoryCount)
            identifierBudget.retain(value.id, value.credentialId)
            require(value.credentialId in credentialIds)
            require(value.id.isValidIdentifier())
            require(passwordHistoryIds.add(value.id))
            requirePayload(
                value.encryptedPassword,
                value.passwordNonce,
                MAX_PASSWORD_HISTORY_ENCRYPTED_PAYLOAD_BYTES,
            )
            val count = passwordHistoryCounts.getOrElse(value.credentialId) { 0 } + 1
            require(count <= MAX_PASSWORD_HISTORY_PER_CREDENTIAL)
            passwordHistoryCounts[value.credentialId] = count
        }

        private fun validateFolderParentMap(parents: Map<String, String?>) {
            val completed = mutableSetOf<String>()
            parents.keys.forEach { folderId ->
                if (folderId in completed) return@forEach
                val seen = mutableSetOf<String>()
                var current: String? = folderId
                while (current != null && current !in completed) {
                    require(seen.add(current))
                    current = parents[current]
                }
                completed.addAll(seen)
            }
        }
    }

    /**
     * Retires biometric material and atomically replaces Room data, then reconciles the separate
     * attachment object store before cancellation can resume. A null store means this service has
     * no external attachment storage to reconcile. If Room rolls back after biometric deletion,
     * make one authenticated best-effort attempt to restore the former enrollment with the leased old VEK.
     */
    private suspend fun activateSnapshot(entities: VaultBackupEntities): Boolean {
        require(entities.attachments.isEmpty()) { "Legacy restore cannot publish attachment rows" }
        return attachmentLifecycleManager.withStableAttachments {
            activateStreamingRestore(
                referencedAttachmentPaths = emptySet(),
                replaceVault = { backupDao.replaceVault(entities) },
            )
        }
    }

    @Suppress("TooGenericExceptionCaught") // Preserve any transaction failure while attaching key-store rollback.
    private suspend fun activateStreamingRestore(
        referencedAttachmentPaths: Set<String>,
        replaceVault: suspend () -> Unit,
    ): Boolean {
        val previousVaultId = vaultRepository.getMetadata().getOrThrow().id.value
        val biometricWasEnabled = biometricKeyStore.contains(previousVaultId)
        val rollbackVek = if (biometricWasEnabled) {
            sessionManager.withUnlockedSession { vek -> vek.copyOf() }
        } else {
            null
        }
        var biometricDeleteAttempted = false
        var databaseCommitted = false
        var attachmentCleanupSucceeded = true
        try {
            sessionManager.lockAndRun(LockReason.Restore) {
                biometricDeleteAttempted = true
                biometricKeyStore.delete(previousVaultId).getOrThrow()
                withContext(kotlinx.coroutines.NonCancellable) {
                    replaceVault()
                    databaseCommitted = true
                    attachmentCleanupSucceeded = attachmentBlobStore?.let { blobStore ->
                        runCatching {
                            blobStore.removeUnreferencedObjects(referencedAttachmentPaths)
                        }.isSuccess
                    } ?: true
                }
            }
        } catch (error: Exception) {
            if (biometricDeleteAttempted && !databaseCommitted && rollbackVek != null) {
                withContext(kotlinx.coroutines.NonCancellable) {
                    biometricKeyStore.enroll(previousVaultId, rollbackVek)
                        .exceptionOrNull()
                        ?.let(error::addSuppressed)
                }
            }
            throw error
        } finally {
            rollbackVek?.let(cryptoEngine::secureWipe)
        }
        return attachmentCleanupSucceeded
    }

    private fun requirePayload(payload: ByteArray, nonce: ByteArray, maxPayloadBytes: Int) {
        require(payload.size <= maxPayloadBytes && CryptoEnvelope.isSupportedPayload(payload))
        require(nonce.size == XCHACHA_NONCE_BYTES)
    }

    private fun requireFixedSecretPayload(payload: ByteArray) {
        require(payload.size in FIXED_SECRET_ENVELOPE_BYTES..MAX_FIXED_SECRET_ENVELOPE_BYTES)
        require(CryptoEnvelope.isSupportedPayload(payload))
        require(CryptoEnvelope.normalize(payload).size == FIXED_SECRET_ENVELOPE_BYTES)
    }

    private fun validateFolderCycles(folders: List<FolderRecordEntity>) {
        val parents = folders.associate { it.id to it.parentId }
        val completed = mutableSetOf<String>()
        folders.forEach { folder ->
            if (folder.id in completed) return@forEach
            val seen = mutableSetOf<String>()
            var current: String? = folder.id
            while (current != null && current !in completed) {
                require(seen.add(current))
                current = parents[current]
            }
            completed.addAll(seen)
        }
    }

    private fun Argon2Parameters.safeForBackup(): Argon2Parameters =
        copy(
            opsLimit = opsLimit.coerceIn(MIN_ARGON2_OPS, MAX_ARGON2_OPS),
            memLimit = memLimit.coerceIn(MIN_ARGON2_MEM, MAX_ARGON2_MEM),
        )

    private class ByteArrayBackupContentSource(private val bytes: ByteArray) : BackupContentSource {
        override val declaredSizeBytes: Long = bytes.size.toLong()
        private var offset = 0

        override suspend fun read(buffer: ByteArray): Int {
            if (offset == bytes.size) return -1
            val count = minOf(buffer.size, bytes.size - offset)
            bytes.copyInto(buffer, destinationOffset = 0, startIndex = offset, endIndex = offset + count)
            offset += count
            return count
        }

        override suspend fun rewind() {
            offset = 0
        }

        override suspend fun close() = Unit
    }

    private fun String.isSafeRelativePath(): Boolean =
        isNotBlank() &&
            hasAtMostCodePoints(MAX_TEXT_LENGTH) &&
            !startsWith('/') &&
            !startsWith('\\') &&
            !contains(':') &&
            hasOnlySafeTextCodePoints() &&
            split('/', '\\').none { it == "." || it == ".." || it.isBlank() }

    private fun String.isSafeMetadataText(): Boolean =
        isNotBlank() &&
            hasAtMostCodePoints(MAX_TEXT_LENGTH) &&
            hasOnlySafeTextCodePoints()

    private fun String?.isSafeOptionalMetadataText(maxLength: Int): Boolean =
        this == null || (hasAtMostCodePoints(maxLength) && hasOnlySafeTextCodePoints())

    private fun String.isSupportedCredentialType(): Boolean =
        this in SUPPORTED_CREDENTIAL_TYPES ||
            (startsWith(CUSTOM_CREDENTIAL_PREFIX) &&
                removePrefix(CUSTOM_CREDENTIAL_PREFIX).isValidIdentifier())

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
        OBSOLETE_ATTACHMENT_CLEANUP_FAILED,
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
        val omittedAttachmentCount: Int = 0,
    ) {
        val reportedAttachmentCount: Int
            get() = attachments.size + omittedAttachmentCount

        fun validationEntities(): VaultBackupEntities = entities(includeAttachmentRows = true)

        fun restorableEntities(): VaultBackupEntities = entities(includeAttachmentRows = attachmentsIncluded)

        fun validateAttachmentAccounting() {
            validateBackupAttachmentAccounting(
                attachmentsIncluded = attachmentsIncluded,
                attachmentRowCount = attachments.size,
                omittedAttachmentCount = omittedAttachmentCount,
            )
        }

        private fun entities(includeAttachmentRows: Boolean): VaultBackupEntities = VaultBackupEntities(
            metadata = metadata.toEntity(),
            credentials = credentials.map(CredentialDto::toEntity),
            folders = folders.map(FolderDto::toEntity),
            tags = tags.map(TagDto::toEntity),
            credentialFolderReferences = credentialFolderReferences.map(CredentialFolderReferenceDto::toEntity),
            credentialTagReferences = credentialTagReferences.map(CredentialTagReferenceDto::toEntity),
            attachments = if (includeAttachmentRows) attachments.map(AttachmentDto::toEntity) else emptyList(),
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
                    attachments = if (attachmentsIncluded) {
                        snapshot.attachments.map(AttachmentDto::from)
                    } else {
                        emptyList()
                    },
                    passwordHistory = snapshot.passwordHistory.map(PasswordHistoryDto::from),
                    attachmentsIncluded = attachmentsIncluded,
                    omittedAttachmentCount = if (attachmentsIncluded) 0 else snapshot.attachments.size,
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
            wrappedVek = wrappedVek.decodeBase64(MAX_FIXED_SECRET_ENVELOPE_BYTES),
            vekNonce = vekNonce.decodeBase64(MAX_NONCE_BYTES),
            encryptedVerificationRecord =
                encryptedVerificationRecord.decodeBase64(MAX_FIXED_SECRET_ENVELOPE_BYTES),
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
        @EncodeDefault(EncodeDefault.Mode.NEVER)
        val titleHash: String? = null,
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
        fun toEntity(): CredentialRecordEntity {
            titleHash?.decodeBase64(BLIND_INDEX_BYTES)?.let { legacyHash ->
                try {
                    require(legacyHash.size == BLIND_INDEX_BYTES)
                } finally {
                    legacyHash.fill(0)
                }
            }
            return CredentialRecordEntity(
                id = id,
                type = type,
                summaryPayload = summaryPayload.decodeBase64(MAX_CREDENTIAL_ENCRYPTED_PAYLOAD_BYTES),
                summaryNonce = summaryNonce.decodeBase64(MAX_NONCE_BYTES),
                secretPayload = secretPayload.decodeBase64(MAX_CREDENTIAL_ENCRYPTED_PAYLOAD_BYTES),
                secretNonce = secretNonce.decodeBase64(MAX_NONCE_BYTES),
                folderId = folderId,
                isFavorite = isFavorite,
                createdAt = createdAt,
                updatedAt = updatedAt,
                lastUsedAt = lastUsedAt,
            )
        }

        companion object {
            fun from(value: CredentialRecordEntity) = CredentialDto(
                id = value.id,
                type = value.type,
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
            nameHash = nameHash.decodeBase64(BLIND_INDEX_BYTES),
            encryptedPayload = encryptedPayload.decodeBase64(MAX_FOLDER_ENCRYPTED_PAYLOAD_BYTES),
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
            nameHash = nameHash.decodeBase64(BLIND_INDEX_BYTES),
            encryptedPayload = encryptedPayload.decodeBase64(MAX_TAG_ENCRYPTED_PAYLOAD_BYTES),
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
        val contentFormatVersion: Int = 0,
        val storageState: String = AttachmentRecordEntity.STORAGE_STATE_LEGACY,
    ) {
        fun toEntity() = AttachmentRecordEntity(
            id = id,
            credentialId = credentialId,
            encryptedFilename = encryptedFilename.decodeBase64(MAX_ATTACHMENT_FILENAME_ENCRYPTED_PAYLOAD_BYTES),
            filenameNonce = filenameNonce.decodeBase64(MAX_NONCE_BYTES),
            mimeType = mimeType,
            sizeBytes = sizeBytes,
            storagePath = storagePath,
            keyDerivationContext = keyDerivationContext,
            createdAt = createdAt,
            contentFormatVersion = contentFormatVersion,
            storageState = storageState,
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
                contentFormatVersion = value.contentFormatVersion,
                storageState = value.storageState,
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
            encryptedPassword = encryptedPassword.decodeBase64(MAX_PASSWORD_HISTORY_ENCRYPTED_PAYLOAD_BYTES),
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
