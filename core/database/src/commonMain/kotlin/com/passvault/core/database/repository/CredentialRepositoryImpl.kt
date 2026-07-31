package com.passvault.core.database.repository

import com.passvault.core.crypto.CryptoEngine
import com.passvault.core.crypto.CryptoEnvelope
import com.passvault.core.database.dao.AttachmentDao
import com.passvault.core.database.dao.CredentialDao
import com.passvault.core.database.dao.FolderDao
import com.passvault.core.database.entity.CredentialTagCrossRef
import com.passvault.core.database.dao.PasswordHistoryDao
import com.passvault.core.database.dao.TagDao
import com.passvault.core.database.entity.AttachmentRecordEntity
import com.passvault.core.database.entity.CredentialRecordEntity
import com.passvault.core.database.entity.CredentialSummaryProjection
import com.passvault.core.database.entity.PasswordHistoryRecordEntity
import com.passvault.core.domain.model.AttachmentId
import com.passvault.core.domain.model.AttachmentMetadata
import com.passvault.core.domain.model.Credential
import com.passvault.core.domain.model.CredentialId
import com.passvault.core.domain.model.CredentialSummary
import com.passvault.core.domain.model.CredentialType
import com.passvault.core.domain.model.CustomField
import com.passvault.core.domain.model.CustomFieldId
import com.passvault.core.domain.model.FolderId
import com.passvault.core.domain.model.PasswordHealth
import com.passvault.core.domain.model.PasswordHistoryEntry
import com.passvault.core.domain.model.PasswordScore
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.model.TagId
import com.passvault.core.domain.model.UrlValue
import com.passvault.core.domain.repository.CredentialRepository
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Repository implementation for credential operations.
 * Handles encryption/decryption and database operations.
 */
class CredentialRepositoryImpl(
    private val credentialDao: CredentialDao,
    private val folderDao: FolderDao,
    private val tagDao: TagDao,
    private val attachmentDao: AttachmentDao,
    private val passwordHistoryDao: PasswordHistoryDao,
    private val cryptoEngine: CryptoEngine,
    private val sessionManager: VaultSessionManager,
) : CredentialRepository {

    private val json = Json { 
        ignoreUnknownKeys = true 
        encodeDefaults = true
    }

    // ==================== Domain Model Serialization ====================

    @Serializable
    private data class SummaryPayload(
        val title: String,
        val usernameHint: String?,
        val emailHint: String?,
        val urlHosts: List<String>,
        val notesPreview: String?,
        val passwordHealth: SerializedPasswordHealth = SerializedPasswordHealth(),
    )

    @Serializable
    private data class SecretPayload(
        val username: String?,
        val email: String?,
        val password: String?,
        val urls: List<String>,
        val notes: String?,
        val recoveryCodes: List<String>,
        val apiKeys: List<String>,
        val licenseKeys: List<String>,
        val customFields: List<SerializedCustomField>,
        val tagIds: List<String>,
        val passwordHealth: SerializedPasswordHealth,
    )

    @Serializable
    private data class SerializedCustomField(
        val id: String,
        val name: String,
        val value: String,
        val isSecret: Boolean,
    )

    @Serializable
    private data class SerializedPasswordHealth(
        val score: String = PasswordScore.UNKNOWN.name,
        val isDuplicate: Boolean = false,
        val isWeak: Boolean = false,
        val isOld: Boolean = false,
        val ageDays: Int? = null,
    )

    // ==================== Read Operations ====================

    override suspend fun getAllSummaries(): Result<List<CredentialSummary.Decrypted>> {
        return repositoryResult {
            val vek = sessionManager.getCurrentVek()
                ?: return Result.failure(IllegalStateException("Vault not unlocked"))

            try {
                val projections = credentialDao.getAllSummaries()
                val tagsByCredential = if (projections.isEmpty()) {
                    emptyMap()
                } else {
                    credentialDao
                        .getTagCrossRefsForCredentials(projections.map { it.id })
                        .groupBy(
                            keySelector = { it.credentialId },
                            valueTransform = { TagId(it.tagId) },
                        )
                }
                projections.map { projection ->
                    decryptSummary(
                        projection = projection,
                        vek = vek,
                        tagIds = tagsByCredential[projection.id].orEmpty().toSet(),
                    )
                }
            } finally {
                cryptoEngine.secureWipe(vek)
            }
        }
    }

    override suspend fun getById(id: CredentialId): Result<Credential?> {
        return repositoryResult {
            val vek = sessionManager.getCurrentVek()
                ?: return Result.failure(IllegalStateException("Vault not unlocked"))

            try {
                val entity = credentialDao.getById(id.value)
                    ?: return@repositoryResult null
                decryptCredential(entity, vek)
            } finally {
                cryptoEngine.secureWipe(vek)
            }
        }
    }

    // ==================== Write Operations ====================

    override suspend fun save(credential: Credential): Result<CredentialId> {
        return repositoryResult {
            val vek = sessionManager.getCurrentVek()
                ?: return Result.failure(IllegalStateException("Vault not unlocked"))

            var previousPassword: SensitiveText? = null
            try {
                validateCredential(credential)
                credential.folderId?.let { folderId ->
                    require(folderDao.getById(folderId.value) != null) {
                        "Credential folder does not exist"
                    }
                }
                require(credential.tagIds.all { tagId -> tagDao.getById(tagId.value) != null }) {
                    "Credential tag does not exist"
                }
                val now = Clock.System.now()
                val previousEntity = credentialDao.getById(credential.id.value)
                previousPassword = previousEntity?.let { decryptPassword(it, vek) }
                val entity = encryptCredential(credential, vek, now)
                val isNew = previousEntity == null
                val history = if (
                    !isNew &&
                    previousPassword != null &&
                    (credential.password == null || previousPassword != credential.password)
                ) {
                    encryptPasswordHistory(credential.id, previousPassword, vek)
                } else {
                    null
                }

                credentialDao.updateCredentialWithTagsAndHistory(
                    entity,
                    credential.tagIds.map { it.value },
                    history,
                )

                credential.id
            } finally {
                previousPassword?.clear()
                cryptoEngine.secureWipe(vek)
            }
        }
    }

    override suspend fun delete(id: CredentialId): Result<Unit> {
        return repositoryResult {
            if (!isVaultUnlocked()) {
                return Result.failure(IllegalStateException("Vault not unlocked"))
            }
            if (credentialDao.exists(id.value)) {
                credentialDao.deleteCredentialAndRefreshCount(id.value)
            }
        }
    }

    override suspend fun updateFavorite(id: CredentialId, isFavorite: Boolean): Result<Unit> {
        return repositoryResult {
            if (!isVaultUnlocked()) {
                return Result.failure(IllegalStateException("Vault not unlocked"))
            }
            credentialDao.updateFavorite(id.value, isFavorite)
        }
    }

    override suspend fun moveToFolder(id: CredentialId, folderId: FolderId?): Result<Unit> {
        return repositoryResult {
            if (!isVaultUnlocked()) {
                return Result.failure(IllegalStateException("Vault not unlocked"))
            }
            require(credentialDao.exists(id.value)) { "Credential not found" }
            if (folderId != null) {
                require(folderDao.exists(folderId.value)) {
                    "Credential folder does not exist"
                }
            }
            credentialDao.updateFolderAndCrossReference(id.value, folderId?.value)
        }
    }

    override suspend fun addTag(id: CredentialId, tagId: TagId): Result<Unit> {
        return repositoryResult {
            if (!isVaultUnlocked()) {
                return Result.failure(IllegalStateException("Vault not unlocked"))
            }
            require(credentialDao.exists(id.value)) { "Credential not found" }
            require(tagDao.exists(tagId.value)) { "Credential tag does not exist" }
            credentialDao.addTagCrossRef(
                CredentialTagCrossRef(credentialId = id.value, tagId = tagId.value)
            )
        }
    }

    override suspend fun removeTag(id: CredentialId, tagId: TagId): Result<Unit> {
        return repositoryResult {
            if (!isVaultUnlocked()) {
                return Result.failure(IllegalStateException("Vault not unlocked"))
            }
            credentialDao.removeTagCrossRef(id.value, tagId.value)
        }
    }

    override suspend fun recordUsage(id: CredentialId, timestamp: Instant): Result<Unit> {
        return repositoryResult {
            if (!isVaultUnlocked()) {
                return Result.failure(IllegalStateException("Vault not unlocked"))
            }
            require(credentialDao.exists(id.value)) { "Credential not found" }
            credentialDao.updateLastUsed(id.value, timestamp.toEpochMilliseconds())
        }
    }

    override suspend fun addPasswordHistory(id: CredentialId, password: SensitiveText): Result<Unit> {
        return repositoryResult {
            val vek = sessionManager.getCurrentVek()
                ?: return Result.failure(IllegalStateException("Vault not unlocked"))
            try {
                val historyEntity = encryptPasswordHistory(id, password, vek)
                passwordHistoryDao.addWithLimit(historyEntity, 10)
            } finally {
                cryptoEngine.secureWipe(vek)
            }
        }
    }

    private suspend fun encryptPasswordHistory(
        id: CredentialId,
        password: SensitiveText,
        vek: ByteArray,
    ): PasswordHistoryRecordEntity {
        val changedAt = Clock.System.now().toEpochMilliseconds()
        val historyId = kotlin.uuid.Uuid.random().toString()
        val recordKey = deriveRecordKey(vek, "history:$historyId")
        val passwordBytes = password.toStringUnsafe().encodeToByteArray()
        return try {
            val encrypted = cryptoEngine.encrypt(
                plaintext = passwordBytes,
                key = recordKey,
                associatedData = historyAssociatedData(historyId, id.value),
            ).getOrThrow()
            PasswordHistoryRecordEntity(
                id = historyId,
                credentialId = id.value,
                encryptedPassword = CryptoEnvelope.encode(encrypted),
                passwordNonce = encrypted.nonce,
                changedAt = changedAt,
            )
        } finally {
            cryptoEngine.secureWipe(passwordBytes)
            cryptoEngine.secureWipe(recordKey)
        }
    }

    override suspend fun getCredentialsForHealthAnalysis(): Result<List<Credential>> {
        return repositoryResult {
            val vek = sessionManager.getCurrentVek()
                ?: return Result.failure(IllegalStateException("Vault not unlocked"))

            try {
                credentialDao.getLoginsForHealthAnalysis().map { entity ->
                    decryptCredential(entity, vek)
                }
            } finally {
                cryptoEngine.secureWipe(vek)
            }
        }
    }

    override suspend fun updateHealth(id: CredentialId, health: PasswordHealth): Result<Unit> {
        return repositoryResult {
            val vek = sessionManager.getCurrentVek()
                ?: return Result.failure(IllegalStateException("Vault not unlocked"))

            var credential: Credential? = null
            try {
                val entity = credentialDao.getById(id.value)
                    ?: return Result.failure(IllegalStateException("Credential not found"))

                credential = decryptCredential(entity, vek)
                val updated = credential.copy(
                    passwordHealth = health,
                    updatedAt = credential.updatedAt
                )

                val encryptedEntity = encryptCredential(updated, vek, updated.updatedAt)
                credentialDao.update(encryptedEntity)
            } finally {
                credential?.clearSensitiveValues()
                cryptoEngine.secureWipe(vek)
            }
        }
    }

    // ==================== Helper Functions ====================

    private suspend fun deriveRecordKey(vek: ByteArray, context: String): ByteArray {
        return cryptoEngine.deriveSubkey(vek, context, 32).getOrThrow()
    }

    private fun validateCredential(credential: Credential) {
        require(credential.id.value.isNotBlank() && credential.id.value.length <= MAX_IDENTIFIER_LENGTH)
        require(credential.title.isNotBlank() && credential.title.length <= MAX_TITLE_LENGTH)
        require(credential.urls.size <= MAX_URL_COUNT)
        require(credential.recoveryCodes.size <= MAX_SECRET_LIST_COUNT)
        require(credential.apiKeys.size <= MAX_SECRET_LIST_COUNT)
        require(credential.licenseKeys.size <= MAX_SECRET_LIST_COUNT)
        require(credential.customFields.size <= MAX_CUSTOM_FIELD_COUNT)
        require(credential.username?.length ?: 0 <= MAX_SENSITIVE_LENGTH)
        require(credential.email?.length ?: 0 <= MAX_SENSITIVE_LENGTH)
        require(credential.password?.length ?: 0 <= MAX_SENSITIVE_LENGTH)
        require(credential.notes?.length ?: 0 <= MAX_NOTES_LENGTH)
        require(credential.recoveryCodes.all { it.length <= MAX_SENSITIVE_LENGTH })
        require(credential.apiKeys.all { it.length <= MAX_SENSITIVE_LENGTH })
        require(credential.licenseKeys.all { it.length <= MAX_SENSITIVE_LENGTH })
        require(credential.customFields.all { field ->
            field.name.isNotBlank() &&
                field.name.length <= MAX_CUSTOM_FIELD_NAME_LENGTH &&
                field.value.length <= MAX_CUSTOM_FIELD_VALUE_LENGTH
        })
        require(credential.urls.all { it.value.length <= MAX_URL_LENGTH && it.host() != null })
    }

    private suspend fun isVaultUnlocked(): Boolean {
        val vek = sessionManager.getCurrentVek() ?: return false
        cryptoEngine.secureWipe(vek)
        return true
    }

    private suspend fun encryptCredential(
        credential: Credential,
        vek: ByteArray,
        now: Instant
    ): CredentialRecordEntity {
        val recordKey = deriveRecordKey(vek, "record:${credential.id.value}")
        var summaryJson: ByteArray? = null
        var secretJson: ByteArray? = null
        try {
            // Prepare summary payload
            val summaryPayload = SummaryPayload(
                title = credential.title,
                // This payload is itself encrypted with the credential summary
                // key. Keeping the complete username/email here preserves
                // unlocked search and list usability without adding plaintext
                // columns or indexes to the database.
                usernameHint = credential.username?.toStringUnsafe(),
                emailHint = credential.email?.toStringUnsafe(),
                urlHosts = credential.urls.mapNotNull { it.host() },
                notesPreview = credential.notes?.toStringUnsafe()?.take(100),
                passwordHealth = credential.passwordHealth.toSerialized(),
            )
            summaryJson = json.encodeToString(summaryPayload).encodeToByteArray()
            val encryptedSummary = cryptoEngine.encrypt(
                plaintext = summaryJson,
                key = recordKey,
                associatedData = credentialAssociatedData(credential.id.value, "summary")
            ).getOrThrow()

        // Prepare secret payload
        val secretPayload = SecretPayload(
            username = credential.username?.toStringUnsafe(),
            email = credential.email?.toStringUnsafe(),
            password = credential.password?.toStringUnsafe(),
            urls = credential.urls.map { it.value },
            notes = credential.notes?.toStringUnsafe(),
            recoveryCodes = credential.recoveryCodes.map { it.toStringUnsafe() },
            apiKeys = credential.apiKeys.map { it.toStringUnsafe() },
            licenseKeys = credential.licenseKeys.map { it.toStringUnsafe() },
            customFields = credential.customFields.map { field ->
                SerializedCustomField(
                    id = field.id.value,
                    name = field.name,
                    value = field.value.toStringUnsafe(),
                    isSecret = field.isSecret
                )
            },
            tagIds = credential.tagIds.map { it.value },
            passwordHealth = credential.passwordHealth.toSerialized()
        )
            secretJson = json.encodeToString(secretPayload).encodeToByteArray()
            val encryptedSecret = cryptoEngine.encrypt(
                plaintext = secretJson,
                key = recordKey,
                associatedData = credentialAssociatedData(credential.id.value, "secret")
            ).getOrThrow()

        // The index is a keyed blind index. It supports exact-match lookups for
        // future database-backed search without exposing a plaintext title or a
        // public hash. Prefix/fuzzy search remains in-memory after unlock.
        val titleHash = deriveBlindIndex(vek, "title", credential.title)

            return CredentialRecordEntity(
                id = credential.id.value,
                type = credential.type.toSerializedString(),
                titleHash = titleHash,
                summaryPayload = CryptoEnvelope.encode(encryptedSummary),
                summaryNonce = encryptedSummary.nonce,
                secretPayload = CryptoEnvelope.encode(encryptedSecret),
                secretNonce = encryptedSecret.nonce,
                folderId = credential.folderId?.value,
                isFavorite = credential.isFavorite,
                createdAt = credential.createdAt.toEpochMilliseconds(),
                updatedAt = now.toEpochMilliseconds(),
                lastUsedAt = credential.lastUsedAt?.toEpochMilliseconds()
            )
        } finally {
            summaryJson?.let { cryptoEngine.secureWipe(it) }
            secretJson?.let { cryptoEngine.secureWipe(it) }
            cryptoEngine.secureWipe(recordKey)
        }
    }

    private suspend fun decryptCredential(
        entity: CredentialRecordEntity,
        vek: ByteArray
    ): Credential {
        val recordKey = deriveRecordKey(vek, "record:${entity.id}")
        var summaryJson: ByteArray? = null
        var secretJson: ByteArray? = null
        return try {
            summaryJson = decryptPayload(
                ciphertext = entity.summaryPayload,
                nonce = entity.summaryNonce,
                key = recordKey,
                associatedData = credentialAssociatedData(entity.id, "summary"),
            )
            val summary = json.decodeFromString<SummaryPayload>(summaryJson.decodeToString())

            secretJson = decryptPayload(
                ciphertext = entity.secretPayload,
                nonce = entity.secretNonce,
                key = recordKey,
                associatedData = credentialAssociatedData(entity.id, "secret"),
            )
            val secret = json.decodeFromString<SecretPayload>(secretJson.decodeToString())
            val tagIds = credentialDao
                .getTagCrossRefsForCredential(entity.id)
                .mapTo(mutableSetOf()) { TagId(it.tagId) }
            val attachments = attachmentDao
                .getByCredential(entity.id)
                .map { it.toDomainModel(vek) }
            val history = passwordHistoryDao
                .getByCredential(entity.id)
                .map { it.toDomainModel(vek) }

            Credential(
                id = CredentialId(entity.id),
                type = entity.type.toCredentialType(),
                title = summary.title,
                username = secret.username?.let(SensitiveText::from),
                email = secret.email?.let(SensitiveText::from),
                password = secret.password?.let(SensitiveText::from),
                urls = secret.urls.map(::UrlValue),
                notes = secret.notes?.let(SensitiveText::from),
                recoveryCodes = secret.recoveryCodes.map(SensitiveText::from),
                apiKeys = secret.apiKeys.map(SensitiveText::from),
                licenseKeys = secret.licenseKeys.map(SensitiveText::from),
                customFields = secret.customFields.map { field ->
                    CustomField(
                        id = CustomFieldId(field.id),
                        name = field.name,
                        value = SensitiveText.from(field.value),
                        isSecret = field.isSecret,
                    )
                },
                folderId = entity.folderId?.let(::FolderId),
                tagIds = tagIds,
                isFavorite = entity.isFavorite,
                attachments = attachments,
                passwordHistory = history,
                createdAt = Instant.fromEpochMilliseconds(entity.createdAt),
                updatedAt = Instant.fromEpochMilliseconds(entity.updatedAt),
                lastUsedAt = entity.lastUsedAt?.let(Instant::fromEpochMilliseconds),
                passwordHealth = summary.passwordHealth.toDomain(),
            )
        } finally {
            summaryJson?.let { cryptoEngine.secureWipe(it) }
            secretJson?.let { cryptoEngine.secureWipe(it) }
            cryptoEngine.secureWipe(recordKey)
        }
    }

    private suspend fun decryptPassword(
        entity: CredentialRecordEntity,
        vek: ByteArray,
    ): SensitiveText? {
        val recordKey = deriveRecordKey(vek, "record:${entity.id}")
        var secretJson: ByteArray? = null
        return try {
            secretJson = decryptPayload(
                ciphertext = entity.secretPayload,
                nonce = entity.secretNonce,
                key = recordKey,
                associatedData = credentialAssociatedData(entity.id, "secret")
            )
            json.decodeFromString<SecretPayload>(secretJson.decodeToString())
                .password
                ?.let(SensitiveText::from)
        } finally {
            cryptoEngine.secureWipe(recordKey)
            secretJson?.let { cryptoEngine.secureWipe(it) }
        }
    }

    private suspend fun decryptSummary(
        projection: CredentialSummaryProjection,
        vek: ByteArray,
        tagIds: Set<TagId>,
    ): CredentialSummary.Decrypted {
        val recordKey = deriveRecordKey(vek, "record:${projection.id}")
        var summaryJson: ByteArray? = null
        return try {
            summaryJson = decryptPayload(
                ciphertext = projection.summaryPayload,
                nonce = projection.summaryNonce,
                key = recordKey,
                associatedData = credentialAssociatedData(projection.id, "summary"),
            )
            val summary = json.decodeFromString<SummaryPayload>(summaryJson.decodeToString())

            CredentialSummary.Decrypted(
                id = CredentialId(projection.id),
                type = projection.type.toCredentialType(),
                title = summary.title,
                displayUsername = summary.usernameHint ?: summary.emailHint,
                isFavorite = projection.isFavorite,
                folderId = projection.folderId?.let(::FolderId),
                tagIds = tagIds,
                passwordHealth = summary.passwordHealth.toDomain(),
                lastUsedAt = projection.lastUsedAt?.let(Instant::fromEpochMilliseconds),
                createdAt = Instant.fromEpochMilliseconds(projection.createdAt),
                updatedAt = Instant.fromEpochMilliseconds(projection.updatedAt),
            )
        } finally {
            summaryJson?.let { cryptoEngine.secureWipe(it) }
            cryptoEngine.secureWipe(recordKey)
        }
    }

    private suspend fun AttachmentRecordEntity.toDomainModel(vek: ByteArray): AttachmentMetadata {
        require(keyDerivationContext.isNotBlank()) { "Attachment key context is missing" }
        val attachmentKey = deriveRecordKey(vek, "attachment:$keyDerivationContext")
        var filenameBytes: ByteArray? = null
        return try {
            filenameBytes = decryptPayload(
                ciphertext = encryptedFilename,
                nonce = filenameNonce,
                key = attachmentKey,
                associatedData = attachmentAssociatedData(id, credentialId),
            )
            val filename = filenameBytes.decodeToString()
            require(filename.isNotBlank() && filename.length <= MAX_ATTACHMENT_FILENAME_LENGTH) {
                "Attachment filename is invalid"
            }
            require(filename.none { it == '/' || it == '\\' || it == '\u0000' }) {
                "Attachment filename contains path separators"
            }

            AttachmentMetadata(
                id = AttachmentId(id),
                fileName = filename,
                mimeType = mimeType,
                sizeBytes = sizeBytes,
                createdAt = Instant.fromEpochMilliseconds(createdAt),
            )
        } finally {
            filenameBytes?.let { cryptoEngine.secureWipe(it) }
            cryptoEngine.secureWipe(attachmentKey)
        }
    }

    private fun PasswordHealth.toSerialized() = SerializedPasswordHealth(
        score = score.name,
        isDuplicate = isDuplicate,
        isWeak = isWeak,
        isOld = isOld,
        ageDays = ageDays,
    )

    private fun SerializedPasswordHealth.toDomain() = PasswordHealth(
        score = runCatching { PasswordScore.valueOf(score) }.getOrDefault(PasswordScore.UNKNOWN),
        isDuplicate = isDuplicate,
        isWeak = isWeak,
        isOld = isOld,
        ageDays = ageDays,
    )

    private fun Credential.clearSensitiveValues() {
        username?.clear()
        email?.clear()
        password?.clear()
        notes?.clear()
        recoveryCodes.forEach(SensitiveText::clear)
        apiKeys.forEach(SensitiveText::clear)
        licenseKeys.forEach(SensitiveText::clear)
        customFields.forEach { it.value.clear() }
        passwordHistory.forEach { it.password.clear() }
    }

    private suspend fun PasswordHistoryRecordEntity.toDomainModel(vek: ByteArray): PasswordHistoryEntry {
        val historyKey = deriveRecordKey(vek, "history:$id")
        var passwordBytes: ByteArray? = null
        return try {
            passwordBytes = decryptPayload(
                ciphertext = encryptedPassword,
                nonce = passwordNonce,
                key = historyKey,
                associatedData = historyAssociatedData(id, credentialId),
            )
            PasswordHistoryEntry(
                password = SensitiveText.from(passwordBytes.decodeToString()),
                changedAt = Instant.fromEpochMilliseconds(changedAt),
            )
        } finally {
            passwordBytes?.let { cryptoEngine.secureWipe(it) }
            cryptoEngine.secureWipe(historyKey)
        }
    }

    /**
     * Normalize the persisted envelope before decrypting.  No unauthenticated
     * retry with a different AAD is allowed: an authenticated record must be
     * bound to its immutable identity and purpose.
     */
    private suspend fun decryptPayload(
        ciphertext: ByteArray,
        nonce: ByteArray,
        key: ByteArray,
        associatedData: ByteArray,
    ): ByteArray {
        return cryptoEngine.decrypt(
            CryptoEnvelope.normalize(ciphertext),
            nonce,
            key,
            associatedData,
        ).getOrThrow()
    }

    private fun credentialAssociatedData(id: String, purpose: String): ByteArray =
        "passvault:credential:$id:$purpose:v1".encodeToByteArray()

    private suspend fun deriveBlindIndex(
        vek: ByteArray,
        purpose: String,
        value: String,
    ): ByteArray {
        val normalized = value.trim().lowercase()
        require(normalized.isNotEmpty()) { "Indexed value must not be empty" }
        return cryptoEngine.deriveSubkey(
            vek,
            "blind-index:$purpose:$normalized",
            32,
        ).getOrThrow()
    }

    private fun historyAssociatedData(historyId: String, credentialId: String): ByteArray =
        "passvault:history:$historyId:$credentialId:v2".encodeToByteArray()

    private fun attachmentAssociatedData(attachmentId: String, credentialId: String): ByteArray =
        "passvault:attachment:$attachmentId:$credentialId:filename:v1".encodeToByteArray()

    private companion object {
        const val MAX_ATTACHMENT_FILENAME_LENGTH = 255
        const val MAX_IDENTIFIER_LENGTH = 256
        const val MAX_TITLE_LENGTH = 200
        const val MAX_SENSITIVE_LENGTH = 4_096
        const val MAX_NOTES_LENGTH = 100_000
        const val MAX_URL_LENGTH = 2_048
        const val MAX_URL_COUNT = 100
        const val MAX_SECRET_LIST_COUNT = 100
        const val MAX_CUSTOM_FIELD_COUNT = 50
        const val MAX_CUSTOM_FIELD_NAME_LENGTH = 200
        const val MAX_CUSTOM_FIELD_VALUE_LENGTH = 20_000
    }
}

// ==================== Extension Functions ====================

private fun CredentialType.toSerializedString(): String = when (this) {
    is CredentialType.Login -> "Login"
    is CredentialType.SecureNote -> "SecureNote"
    is CredentialType.ApiKey -> "ApiKey"
    is CredentialType.LicenseKey -> "LicenseKey"
    is CredentialType.RecoveryCodes -> "RecoveryCodes"
    is CredentialType.WiFiCredential -> "WiFiCredential"
    is CredentialType.Identity -> "Identity"
    is CredentialType.PaymentCard -> "PaymentCard"
    is CredentialType.Custom -> "Custom:$id"
}

private fun String.toCredentialType(): CredentialType = when {
    this == "Login" -> CredentialType.Login
    this == "SecureNote" -> CredentialType.SecureNote
    this == "ApiKey" -> CredentialType.ApiKey
    this == "LicenseKey" -> CredentialType.LicenseKey
    this == "RecoveryCodes" -> CredentialType.RecoveryCodes
    this == "WiFiCredential" -> CredentialType.WiFiCredential
    this == "Identity" -> CredentialType.Identity
    this == "PaymentCard" -> CredentialType.PaymentCard
    this.startsWith("Custom:") -> CredentialType.Custom(this.substringAfter(":"))
    else -> throw IllegalArgumentException("Unsupported credential type")
}

