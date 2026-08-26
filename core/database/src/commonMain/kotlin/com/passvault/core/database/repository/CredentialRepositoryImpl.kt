package com.passvault.core.database.repository

import com.passvault.core.crypto.CryptoEngine
import com.passvault.core.crypto.CryptoEnvelope
import com.passvault.core.crypto.EncryptedData
import com.passvault.core.crypto.PaddedPayload
import com.passvault.core.database.dao.AttachmentDao
import com.passvault.core.database.dao.CredentialDao
import com.passvault.core.database.dao.FolderDao
import com.passvault.core.database.entity.CredentialTagCrossRef
import com.passvault.core.database.dao.PasswordHistoryDao
import com.passvault.core.database.dao.TagDao
import com.passvault.core.database.attachment.AttachmentLifecycleManager
import com.passvault.core.database.attachment.DatabaseOnlyAttachmentLifecycleManager
import com.passvault.core.database.attachment.AttachmentStorageKind
import com.passvault.core.database.attachment.requireStableStorageKind
import com.passvault.core.database.entity.AttachmentRecordEntity
import com.passvault.core.database.entity.CredentialRecordEntity
import com.passvault.core.database.entity.CredentialSummaryProjection
import com.passvault.core.database.entity.PasswordHistoryRecordEntity
import com.passvault.core.domain.model.AttachmentId
import com.passvault.core.domain.model.AttachmentAvailability
import com.passvault.core.domain.model.AttachmentMetadata
import com.passvault.core.domain.model.Credential
import com.passvault.core.domain.model.CredentialId
import com.passvault.core.domain.model.CredentialSummary
import com.passvault.core.domain.model.CredentialType
import com.passvault.core.domain.repository.CredentialHealthInput
import com.passvault.core.domain.repository.CredentialTotpInput
import com.passvault.core.domain.repository.AttachmentPolicy
import com.passvault.core.domain.model.CustomField
import com.passvault.core.domain.model.CustomFieldId
import com.passvault.core.domain.model.FolderId
import com.passvault.core.domain.model.PasswordHealth
import com.passvault.core.domain.model.PasswordHistoryEntry
import com.passvault.core.domain.model.PasswordScore
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.model.TagId
import com.passvault.core.domain.model.TotpAlgorithm
import com.passvault.core.domain.model.TotpConfiguration
import com.passvault.core.domain.model.UrlValue
import com.passvault.core.domain.repository.CredentialRepository
import com.passvault.core.domain.repository.CredentialTotpRepository
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Repository implementation for credential operations.
 * Handles encryption/decryption and database operations.
 */
@Suppress(
    "LargeClass",
    "TooManyFunctions",
) // Keeping authenticated decoding, validation, and secret cleanup in one boundary limits plaintext ownership.
class CredentialRepositoryImpl(
    private val credentialDao: CredentialDao,
    private val folderDao: FolderDao,
    private val tagDao: TagDao,
    private val attachmentDao: AttachmentDao,
    private val passwordHistoryDao: PasswordHistoryDao,
    private val cryptoEngine: CryptoEngine,
    private val sessionManager: VaultSessionManager,
    private val attachmentLifecycleManager: AttachmentLifecycleManager = DatabaseOnlyAttachmentLifecycleManager,
) : CredentialRepository, CredentialTotpRepository {

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
        val totp: SerializedTotp? = null,
        val passwordChangedAtEpochMillis: Long? = null,
    )

    /**
     * Narrow decoder used by password-health analysis. Unknown secret fields
     * are skipped instead of being materialized into additional Strings.
     */
    @Serializable
    private data class HealthSecretPayload(
        val username: String? = null,
        val email: String? = null,
        val password: String? = null,
        val passwordChangedAtEpochMillis: Long? = null,
    )

    /** Narrow decoder for the all-accounts authenticator screen. */
    @Serializable
    private data class TotpDisplaySecretPayload(
        val username: String? = null,
        val email: String? = null,
        val totp: SerializedTotp? = null,
    )

    @Serializable
    private data class PasswordStatePayload(
        val password: String? = null,
        val passwordChangedAtEpochMillis: Long? = null,
    )

    private data class PersistedPasswordState(
        val password: SensitiveText?,
        val changedAtEpochMillis: Long?,
    )

    private data class CredentialSavePlan(
        val credential: Credential,
        val passwordChangedAtEpochMillis: Long?,
        val previousPasswordForHistory: SensitiveText?,
    )

    private data class CredentialRelations(
        val tagIds: Set<TagId>,
        val attachments: List<AttachmentMetadata>,
        val passwordHistory: List<PasswordHistoryEntry>,
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

    @Serializable
    private data class SerializedTotp(
        val secret: String,
        val issuer: String? = null,
        val accountName: String? = null,
        val algorithm: String = TotpAlgorithm.SHA1.name,
        val digits: Int = DEFAULT_TOTP_DIGITS,
        val periodSeconds: Int = DEFAULT_TOTP_PERIOD_SECONDS,
    )

    // ==================== Read Operations ====================

    override suspend fun getAllSummaries(): Result<List<CredentialSummary.Decrypted>> {
        return repositoryResult {
            sessionManager.withUnlockedSession { vek ->
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
            }
        }
    }

    override suspend fun getById(id: CredentialId): Result<Credential?> {
        return repositoryResult {
            sessionManager.withUnlockedSession { vek ->
                id.value.requireRecordIdentifier("Credential ID")
                val entity = credentialDao.getById(id.value)
                    ?: return@withUnlockedSession null
                decryptCredential(entity, vek)
            }
        }
    }

    // ==================== Write Operations ====================

    override suspend fun save(credential: Credential): Result<CredentialId> {
        return repositoryResult {
            sessionManager.withUnlockedSession { vek ->
                val now = Clock.System.now()
                val plan = prepareCredentialSave(credential, vek, now)
                try {
                    val entity = encryptCredential(
                        credential = plan.credential,
                        vek = vek,
                        now = now,
                        passwordChangedAtEpochMillis = plan.passwordChangedAtEpochMillis,
                    )
                    val history = plan.previousPasswordForHistory?.let { previousPassword ->
                        encryptPasswordHistory(credential.id, previousPassword, vek, now)
                    }

                    credentialDao.updateCredentialWithTagsAndHistory(
                        entity,
                        plan.credential.tagIds.map { it.value },
                        history,
                        requiredVaultFormatVersion = plan.credential.totp?.let { TOTP_VAULT_FORMAT_VERSION },
                    )

                    credential.id
                } finally {
                    plan.previousPasswordForHistory?.clear()
                }
            }
        }
    }

    override suspend fun delete(id: CredentialId): Result<Unit> {
        return repositoryResult {
            sessionManager.withUnlockedSession {
                id.value.requireRecordIdentifier("Credential ID")
                if (credentialDao.exists(id.value)) {
                    attachmentLifecycleManager.deleteCredentialAndAttachments(id.value) {
                        credentialDao.deleteCredentialAndRefreshCount(id.value)
                    }
                }
            }
        }
    }

    override suspend fun updateFavorite(id: CredentialId, isFavorite: Boolean): Result<Unit> {
        return repositoryResult {
            sessionManager.withUnlockedSession {
                id.value.requireRecordIdentifier("Credential ID")
                require(credentialDao.exists(id.value)) { "Credential not found" }
                credentialDao.updateFavorite(id.value, isFavorite)
            }
        }
    }

    override suspend fun moveToFolder(id: CredentialId, folderId: FolderId?): Result<Unit> {
        return repositoryResult {
            sessionManager.withUnlockedSession {
                id.value.requireRecordIdentifier("Credential ID")
                folderId?.value?.requireRecordIdentifier("Folder ID")
                require(credentialDao.exists(id.value)) { "Credential not found" }
                if (folderId != null) {
                    require(folderDao.exists(folderId.value)) {
                        "Credential folder does not exist"
                    }
                }
                credentialDao.updateFolderAndCrossReference(id.value, folderId?.value)
            }
        }
    }

    override suspend fun addTag(id: CredentialId, tagId: TagId): Result<Unit> {
        return repositoryResult {
            sessionManager.withUnlockedSession {
                id.value.requireRecordIdentifier("Credential ID")
                tagId.value.requireRecordIdentifier("Tag ID")
                require(credentialDao.exists(id.value)) { "Credential not found" }
                require(tagDao.exists(tagId.value)) { "Credential tag does not exist" }
                val existingTags = credentialDao.getTagCrossRefsForCredential(id.value)
                require(
                    existingTags.any { it.tagId == tagId.value } || existingTags.size < MAX_TAG_COUNT,
                ) { "Credential tag limit reached" }
                credentialDao.addTagCrossRef(
                    CredentialTagCrossRef(credentialId = id.value, tagId = tagId.value),
                )
            }
        }
    }

    override suspend fun removeTag(id: CredentialId, tagId: TagId): Result<Unit> {
        return repositoryResult {
            sessionManager.withUnlockedSession {
                id.value.requireRecordIdentifier("Credential ID")
                tagId.value.requireRecordIdentifier("Tag ID")
                require(credentialDao.exists(id.value)) { "Credential not found" }
                credentialDao.removeTagCrossRef(id.value, tagId.value)
            }
        }
    }

    override suspend fun recordUsage(id: CredentialId, timestamp: Instant): Result<Unit> {
        return repositoryResult {
            sessionManager.withUnlockedSession {
                id.value.requireRecordIdentifier("Credential ID")
                require(credentialDao.exists(id.value)) { "Credential not found" }
                credentialDao.updateLastUsed(id.value, timestamp.toEpochMilliseconds())
            }
        }
    }

    private suspend fun encryptPasswordHistory(
        id: CredentialId,
        password: SensitiveText,
        vek: ByteArray,
        changedAt: Instant,
    ): PasswordHistoryRecordEntity {
        val historyId = kotlin.uuid.Uuid.random().toString()
        val recordKey = deriveRecordKey(vek, "history:$historyId")
        var passwordBytes: ByteArray? = null
        var encryptedPassword: EncryptedData? = null
        return try {
            val encodedPassword = password.toUtf8ByteArray()
            passwordBytes = encodedPassword
            val encrypted = PaddedPayload.encrypt(
                cryptoEngine = cryptoEngine,
                plaintext = encodedPassword,
                key = recordKey,
                associatedData = historyAssociatedData(historyId, id.value),
                maxPlaintextBytes = MAX_SENSITIVE_UTF8_BYTES,
            ).getOrThrow()
            encryptedPassword = encrypted
            PasswordHistoryRecordEntity(
                id = historyId,
                credentialId = id.value,
                encryptedPassword = CryptoEnvelope.encode(encrypted),
                passwordNonce = encrypted.nonce.copyOf(),
                changedAt = changedAt.toEpochMilliseconds(),
            )
        } finally {
            encryptedPassword?.clear()
            passwordBytes?.let { cryptoEngine.secureWipe(it) }
            cryptoEngine.secureWipe(recordKey)
        }
    }

    @Suppress("TooGenericExceptionCaught") // Every partially decrypted health input must be cleared on failure.
    override suspend fun getCredentialsForHealthAnalysis(): Result<List<CredentialHealthInput>> {
        return repositoryResult {
            sessionManager.withUnlockedSession { vek ->
                val inputs = mutableListOf<CredentialHealthInput>()
                try {
                    credentialDao.getLoginsForHealthAnalysis().forEach { entity ->
                        inputs += decryptHealthInput(entity, vek)
                    }
                    inputs
                } catch (error: Exception) {
                    inputs.forEach { it.clearSensitiveValues() }
                    throw error
                }
            }
        }
    }

    @Suppress("TooGenericExceptionCaught") // Every copied TOTP secret is cleared if the batch fails.
    override suspend fun getCredentialsForTotpDisplay(): Result<List<CredentialTotpInput>> {
        return repositoryResult {
            sessionManager.withUnlockedSession { vek ->
                val inputs = mutableListOf<CredentialTotpInput>()
                try {
                    credentialDao.getLoginsForTotpDisplay().forEach { entity ->
                        decryptTotpDisplayInput(entity, vek)?.let(inputs::add)
                    }
                    inputs.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title })
                } catch (error: Exception) {
                    inputs.forEach(CredentialTotpInput::clear)
                    throw error
                }
            }
        }
    }

    override suspend fun updateHealth(id: CredentialId, health: PasswordHealth): Result<Unit> {
        return repositoryResult {
            sessionManager.withUnlockedSession { vek ->
                id.value.requireRecordIdentifier("Credential ID")
                validatePasswordHealth(health)
                val entity = credentialDao.getById(id.value)
                    ?: throw IllegalStateException("Credential not found")
                updateEncryptedHealth(entity, health, vek)
            }
        }
    }

    // ==================== Helper Functions ====================

    private suspend fun deriveRecordKey(vek: ByteArray, context: String): ByteArray {
        return cryptoEngine.deriveSubkey(vek, context, 32).getOrThrow()
    }

    private suspend fun updateEncryptedHealth(
        entity: CredentialRecordEntity,
        health: PasswordHealth,
        vek: ByteArray,
    ) {
        val recordKey = deriveRecordKey(vek, "record:${entity.id}")
        var summaryJson: ByteArray? = null
        var updatedJson: ByteArray? = null
        var encryptedSummary: EncryptedData? = null
        try {
            summaryJson = decryptPayload(
                ciphertext = entity.summaryPayload,
                nonce = entity.summaryNonce,
                key = recordKey,
                associatedData = credentialAssociatedData(entity.id, "summary"),
            )
            val summary = json.decodeFromString<SummaryPayload>(summaryJson.decodeUtf8Strict())
            validateSummaryPayload(summary)
            updatedJson = json.encodeToString(
                summary.copy(passwordHealth = health.toSerialized()),
            ).encodeToByteArray()
            val encrypted = PaddedPayload.encrypt(
                cryptoEngine = cryptoEngine,
                plaintext = updatedJson,
                key = recordKey,
                associatedData = credentialAssociatedData(entity.id, "summary"),
                maxPlaintextBytes = MAX_CREDENTIAL_PLAINTEXT_BYTES,
            ).getOrThrow()
            encryptedSummary = encrypted
            credentialDao.updateEncryptedSummary(
                id = entity.id,
                summaryPayload = CryptoEnvelope.encode(encrypted),
                summaryNonce = encrypted.nonce.copyOf(),
            )
        } finally {
            summaryJson?.let { cryptoEngine.secureWipe(it) }
            updatedJson?.let { cryptoEngine.secureWipe(it) }
            encryptedSummary?.clear()
            cryptoEngine.secureWipe(recordKey)
        }
    }

    private suspend fun prepareCredentialSave(
        credential: Credential,
        vek: ByteArray,
        now: Instant,
    ): CredentialSavePlan {
        validateCredential(credential)
        validateCredentialRelations(credential)
        val previousEntity = credentialDao.getById(credential.id.value)
        var previousPasswordState: PersistedPasswordState? = null
        try {
            previousPasswordState = previousEntity?.let { decryptPasswordState(it, vek) }
            val previousPassword = previousPasswordState?.password
            val passwordChanged = previousEntity != null && previousPassword != credential.password
            val passwordChangedAt = resolvePasswordChangedAt(
                credential = credential,
                previousEntity = previousEntity,
                previousState = previousPasswordState,
                passwordChanged = passwordChanged,
                now = now,
            )
            val credentialToPersist = credential.copy(
                createdAt = previousEntity
                    ?.let { Instant.fromEpochMilliseconds(it.createdAt) }
                    ?: credential.createdAt,
                passwordHealth = if (passwordChanged) PasswordHealth.UNKNOWN else credential.passwordHealth,
            )
            return CredentialSavePlan(
                credential = credentialToPersist,
                passwordChangedAtEpochMillis = passwordChangedAt,
                previousPasswordForHistory = if (passwordChanged) previousPassword else null,
            ).also {
                if (!passwordChanged) previousPassword?.clear()
                previousPasswordState = null
            }
        } finally {
            previousPasswordState?.password?.clear()
        }
    }

    private suspend fun validateCredentialRelations(credential: Credential) {
        credential.folderId?.let { folderId ->
            require(folderDao.exists(folderId.value)) { "Credential folder does not exist" }
        }
        require(credential.tagIds.all { tagId -> tagDao.exists(tagId.value) }) {
            "Credential tag does not exist"
        }
    }

    private suspend fun resolvePasswordChangedAt(
        credential: Credential,
        previousEntity: CredentialRecordEntity?,
        previousState: PersistedPasswordState?,
        passwordChanged: Boolean,
        now: Instant,
    ): Long? = when {
        credential.password?.isNotEmpty() != true -> null
        previousEntity == null || passwordChanged -> now.toEpochMilliseconds()
        else -> previousState?.changedAtEpochMillis
            ?: passwordHistoryDao.getLatestByCredential(credential.id.value)?.changedAt
            ?: previousEntity.createdAt
    }

    private fun validateCredential(credential: Credential) {
        credential.id.value.requireRecordIdentifier("Credential ID")
        credential.folderId?.value?.requireRecordIdentifier("Folder ID")
        credential.tagIds.forEach { it.value.requireRecordIdentifier("Tag ID") }
        require(credential.tagIds.size <= MAX_TAG_COUNT)
        require(
            credential.title.isNotBlank() &&
                credential.title.hasAtMostCodePoints(MAX_TITLE_LENGTH) &&
                credential.title.hasOnlySafeTextCodePoints(),
        )
        require(credential.urls.size <= MAX_URL_COUNT)
        require(credential.recoveryCodes.size <= MAX_SECRET_LIST_COUNT)
        require(credential.apiKeys.size <= MAX_SECRET_LIST_COUNT)
        require(credential.licenseKeys.size <= MAX_SECRET_LIST_COUNT)
        require(credential.customFields.size <= MAX_CUSTOM_FIELD_COUNT)
        require(credential.username.hasValidSingleLineEncodingAndLength(MAX_SENSITIVE_LENGTH))
        require(credential.email.hasValidSingleLineEncodingAndLength(MAX_SENSITIVE_LENGTH))
        require(credential.password.hasValidEncodingAndLength(MAX_SENSITIVE_LENGTH))
        require(credential.notes.hasValidEncodingAndLength(MAX_NOTES_LENGTH))
        require(credential.recoveryCodes.all { it.hasValidEncodingAndLength(MAX_SENSITIVE_LENGTH) })
        require(credential.apiKeys.all { it.hasValidEncodingAndLength(MAX_SENSITIVE_LENGTH) })
        require(credential.licenseKeys.all { it.hasValidEncodingAndLength(MAX_SENSITIVE_LENGTH) })
        val customFieldIds = credential.customFields.map { field ->
            field.id.value.requireRecordIdentifier("Custom field ID")
            require(
                field.name.isNotBlank() &&
                    field.name.hasAtMostCodePoints(MAX_CUSTOM_FIELD_NAME_LENGTH) &&
                    field.name.hasOnlySafeTextCodePoints() &&
                    field.value.hasValidEncodingAndLength(MAX_CUSTOM_FIELD_VALUE_LENGTH),
            )
            field.id.value
        }
        require(customFieldIds.size == customFieldIds.toSet().size) {
            "Custom field IDs must be unique"
        }
        require(credential.urls.all { it.value.hasAtMostCodePoints(MAX_URL_LENGTH) && it.host() != null })
        validatePasswordHealth(credential.passwordHealth)
        (credential.type as? CredentialType.Custom)?.id?.requireRecordIdentifier("Custom credential type ID")
        credential.totp?.let { totp ->
            require(credential.type == CredentialType.Login)
            require(totp.secret.toStringUnsafe().isValidTotpSecret())
            require(totp.issuer.isValidTotpLabel())
            require(totp.accountName.isValidTotpLabel())
            require(totp.digits in SUPPORTED_TOTP_DIGITS)
            require(totp.periodSeconds in MIN_TOTP_PERIOD_SECONDS..MAX_TOTP_PERIOD_SECONDS)
        }
    }

    private suspend fun encryptCredential(
        credential: Credential,
        vek: ByteArray,
        now: Instant,
        passwordChangedAtEpochMillis: Long?,
    ): CredentialRecordEntity {
        val recordKey = deriveRecordKey(vek, "record:${credential.id.value}")
        var summaryJson: ByteArray? = null
        var secretJson: ByteArray? = null
        var encryptedSummary: EncryptedData? = null
        var encryptedSecret: EncryptedData? = null
        try {
            summaryJson = json.encodeToString(credential.toSummaryPayload()).encodeToByteArray()
            require(summaryJson.size <= MAX_CREDENTIAL_PLAINTEXT_BYTES)
            val summary = PaddedPayload.encrypt(
                cryptoEngine = cryptoEngine,
                plaintext = summaryJson,
                key = recordKey,
                associatedData = credentialAssociatedData(credential.id.value, "summary"),
                maxPlaintextBytes = MAX_CREDENTIAL_PLAINTEXT_BYTES,
            ).getOrThrow()
            encryptedSummary = summary

            secretJson = json.encodeToString(
                credential.toSecretPayload(passwordChangedAtEpochMillis),
            ).encodeToByteArray()
            require(secretJson.size <= MAX_CREDENTIAL_PLAINTEXT_BYTES)
            val secret = PaddedPayload.encrypt(
                cryptoEngine = cryptoEngine,
                plaintext = secretJson,
                key = recordKey,
                associatedData = credentialAssociatedData(credential.id.value, "secret"),
                maxPlaintextBytes = MAX_CREDENTIAL_PLAINTEXT_BYTES,
            ).getOrThrow()
            encryptedSecret = secret

            return credential.toRecordEntity(
                encryptedSummary = summary,
                encryptedSecret = secret,
                updatedAt = now,
            )
        } finally {
            summaryJson?.let { cryptoEngine.secureWipe(it) }
            secretJson?.let { cryptoEngine.secureWipe(it) }
            encryptedSummary?.clear()
            encryptedSecret?.clear()
            cryptoEngine.secureWipe(recordKey)
        }
    }

    private fun Credential.toSummaryPayload(): SummaryPayload = SummaryPayload(
        title = title,
        // The encrypted summary retains list/search usability without adding a
        // plaintext username/email column or public index.
        usernameHint = username?.toStringUnsafe(),
        emailHint = email?.toStringUnsafe(),
        passwordHealth = passwordHealth.toSerialized(),
    )

    private fun Credential.toSecretPayload(passwordChangedAtEpochMillis: Long?): SecretPayload = SecretPayload(
        username = username?.toStringUnsafe(),
        email = email?.toStringUnsafe(),
        password = password?.toStringUnsafe(),
        urls = urls.map { it.value },
        notes = notes?.toStringUnsafe(),
        recoveryCodes = recoveryCodes.map { it.toStringUnsafe() },
        apiKeys = apiKeys.map { it.toStringUnsafe() },
        licenseKeys = licenseKeys.map { it.toStringUnsafe() },
        customFields = customFields.map { field ->
            SerializedCustomField(
                id = field.id.value,
                name = field.name,
                value = field.value.toStringUnsafe(),
                isSecret = field.isSecret,
            )
        },
        totp = totp?.let { configuration ->
            SerializedTotp(
                secret = configuration.secret.toStringUnsafe(),
                issuer = configuration.issuer,
                accountName = configuration.accountName,
                algorithm = configuration.algorithm.name,
                digits = configuration.digits,
                periodSeconds = configuration.periodSeconds,
            )
        },
        passwordChangedAtEpochMillis = passwordChangedAtEpochMillis,
    )

    private fun Credential.toRecordEntity(
        encryptedSummary: EncryptedData,
        encryptedSecret: EncryptedData,
        updatedAt: Instant,
    ): CredentialRecordEntity = CredentialRecordEntity(
        id = id.value,
        type = type.toSerializedString(),
        summaryPayload = CryptoEnvelope.encode(encryptedSummary),
        summaryNonce = encryptedSummary.nonce.copyOf(),
        secretPayload = CryptoEnvelope.encode(encryptedSecret),
        secretNonce = encryptedSecret.nonce.copyOf(),
        folderId = folderId?.value,
        isFavorite = isFavorite,
        createdAt = createdAt.toEpochMilliseconds(),
        updatedAt = updatedAt.toEpochMilliseconds(),
        lastUsedAt = lastUsedAt?.toEpochMilliseconds(),
    )

    @Suppress("TooGenericExceptionCaught") // Cleanup applies to crypto, decoding, and validation failures alike.
    private suspend fun decryptCredential(
        entity: CredentialRecordEntity,
        vek: ByteArray,
    ): Credential {
        entity.id.requireRecordIdentifier("Credential ID")
        entity.folderId?.requireRecordIdentifier("Folder ID")
        val recordKey = deriveRecordKey(vek, "record:${entity.id}")
        var summaryJson: ByteArray? = null
        var secretJson: ByteArray? = null
        var history = emptyList<PasswordHistoryEntry>()
        var totp: TotpConfiguration? = null
        var credential: Credential? = null
        try {
            summaryJson = decryptPayload(
                ciphertext = entity.summaryPayload,
                nonce = entity.summaryNonce,
                key = recordKey,
                associatedData = credentialAssociatedData(entity.id, "summary"),
            )
            val summary = json.decodeFromString<SummaryPayload>(summaryJson.decodeUtf8Strict())
            validateSummaryPayload(summary)

            secretJson = decryptPayload(
                ciphertext = entity.secretPayload,
                nonce = entity.secretNonce,
                key = recordKey,
                associatedData = credentialAssociatedData(entity.id, "secret"),
            )
            val secret = json.decodeFromString<SecretPayload>(secretJson.decodeUtf8Strict())
            validateSummarySecretConsistency(summary, secret.username, secret.email)
            validateSecretPayload(secret)
            val relations = decryptCredentialRelations(entity, vek)
            history = relations.passwordHistory
            totp = secret.totp?.toDomain()

            credential = buildCredential(entity, summary, secret, relations, totp)
            validateCredential(credential)
            return requireNotNull(credential)
        } catch (error: Exception) {
            credential?.clearSensitiveValues() ?: run {
                history.forEach { it.password.clear() }
                totp?.clear()
            }
            throw error
        } finally {
            summaryJson?.let { cryptoEngine.secureWipe(it) }
            secretJson?.let { cryptoEngine.secureWipe(it) }
            cryptoEngine.secureWipe(recordKey)
        }
    }

    private suspend fun decryptCredentialRelations(
        entity: CredentialRecordEntity,
        vek: ByteArray,
    ): CredentialRelations {
        val tagIds = credentialDao
            .getTagCrossRefsForCredential(entity.id)
            .mapTo(mutableSetOf()) { reference ->
                reference.tagId.requireRecordIdentifier("Tag ID")
                TagId(reference.tagId)
            }
        require(tagIds.size <= MAX_TAG_COUNT)
        val attachments = attachmentDao
            .getByCredential(entity.id)
            .map { it.toDomainModel(entity.id, vek) }
        return CredentialRelations(
            tagIds = tagIds,
            attachments = attachments,
            passwordHistory = decryptPasswordHistory(entity.id, vek),
        )
    }

    private fun buildCredential(
        entity: CredentialRecordEntity,
        summary: SummaryPayload,
        secret: SecretPayload,
        relations: CredentialRelations,
        totp: TotpConfiguration?,
    ): Credential = Credential(
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
        tagIds = relations.tagIds,
        isFavorite = entity.isFavorite,
        attachments = relations.attachments,
        passwordHistory = relations.passwordHistory,
        createdAt = Instant.fromEpochMilliseconds(entity.createdAt),
        updatedAt = Instant.fromEpochMilliseconds(entity.updatedAt),
        lastUsedAt = entity.lastUsedAt?.let(Instant::fromEpochMilliseconds),
        passwordHealth = summary.passwordHealth.toDomain(),
        totp = totp,
    )

    private suspend fun decryptPasswordState(
        entity: CredentialRecordEntity,
        vek: ByteArray,
    ): PersistedPasswordState {
        val recordKey = deriveRecordKey(vek, "record:${entity.id}")
        var secretJson: ByteArray? = null
        return try {
            secretJson = decryptPayload(
                ciphertext = entity.secretPayload,
                nonce = entity.secretNonce,
                key = recordKey,
                associatedData = credentialAssociatedData(entity.id, "secret")
            )
            val secret = json.decodeFromString<PasswordStatePayload>(secretJson.decodeUtf8Strict())
            require(secret.password.hasValidEncodingAndLength(MAX_SENSITIVE_LENGTH))
            PersistedPasswordState(
                password = secret.password?.let(SensitiveText::from),
                changedAtEpochMillis = secret.passwordChangedAtEpochMillis,
            )
        } finally {
            cryptoEngine.secureWipe(recordKey)
            secretJson?.let { cryptoEngine.secureWipe(it) }
        }
    }

    @Suppress("TooGenericExceptionCaught") // Clear every decoded secret regardless of the failing boundary.
    private suspend fun decryptHealthInput(
        entity: CredentialRecordEntity,
        vek: ByteArray,
    ): CredentialHealthInput {
        entity.id.requireRecordIdentifier("Credential ID")
        entity.folderId?.requireRecordIdentifier("Folder ID")
        val recordKey = deriveRecordKey(vek, "record:${entity.id}")
        var summaryJson: ByteArray? = null
        var secretJson: ByteArray? = null
        var username: SensitiveText? = null
        var email: SensitiveText? = null
        var password: SensitiveText? = null
        try {
            summaryJson = decryptPayload(
                ciphertext = entity.summaryPayload,
                nonce = entity.summaryNonce,
                key = recordKey,
                associatedData = credentialAssociatedData(entity.id, "summary"),
            )
            val summary = json.decodeFromString<SummaryPayload>(summaryJson.decodeUtf8Strict())
            validateSummaryPayload(summary)
            secretJson = decryptPayload(
                ciphertext = entity.secretPayload,
                nonce = entity.secretNonce,
                key = recordKey,
                associatedData = credentialAssociatedData(entity.id, "secret"),
            )
            val secret = json.decodeFromString<HealthSecretPayload>(secretJson.decodeUtf8Strict())
            validateSummarySecretConsistency(summary, secret.username, secret.email)
            val tagIds = healthTagIds(entity.id)
            val passwordChangedAt = resolveHealthPasswordChangedAt(entity, secret)

            username = secret.username?.let(SensitiveText::from)
            email = secret.email?.let(SensitiveText::from)
            password = secret.password?.let(SensitiveText::from)
            return buildHealthInput(
                entity = entity,
                summary = summary,
                username = username,
                email = email,
                password = password,
                tagIds = tagIds,
                passwordChangedAtEpochMillis = passwordChangedAt,
            ).also(::validateHealthInput)
        } catch (error: Exception) {
            username?.clear()
            email?.clear()
            password?.clear()
            throw error
        } finally {
            summaryJson?.let { cryptoEngine.secureWipe(it) }
            secretJson?.let { cryptoEngine.secureWipe(it) }
            cryptoEngine.secureWipe(recordKey)
        }
    }

    @Suppress("TooGenericExceptionCaught") // A partially decoded seed must be cleared on every failure path.
    private suspend fun decryptTotpDisplayInput(
        entity: CredentialRecordEntity,
        vek: ByteArray,
    ): CredentialTotpInput? {
        entity.id.requireRecordIdentifier("Credential ID")
        require(entity.type.toCredentialType() == CredentialType.Login)
        val recordKey = deriveRecordKey(vek, "record:${entity.id}")
        var summaryJson: ByteArray? = null
        var secretJson: ByteArray? = null
        var configuration: TotpConfiguration? = null
        try {
            summaryJson = decryptPayload(
                ciphertext = entity.summaryPayload,
                nonce = entity.summaryNonce,
                key = recordKey,
                associatedData = credentialAssociatedData(entity.id, "summary"),
            )
            val summary = json.decodeFromString<SummaryPayload>(summaryJson.decodeUtf8Strict())
            validateSummaryPayload(summary)
            secretJson = decryptPayload(
                ciphertext = entity.secretPayload,
                nonce = entity.secretNonce,
                key = recordKey,
                associatedData = credentialAssociatedData(entity.id, "secret"),
            )
            val secret = json.decodeFromString<TotpDisplaySecretPayload>(secretJson.decodeUtf8Strict())
            validateSummarySecretConsistency(summary, secret.username, secret.email)
            configuration = secret.totp?.toDomain() ?: return null
            return CredentialTotpInput(
                id = CredentialId(entity.id),
                title = summary.title,
                displayUsername = summary.usernameHint ?: summary.emailHint,
                configuration = configuration,
            )
        } catch (error: Exception) {
            configuration?.clear()
            throw error
        } finally {
            summaryJson?.let { cryptoEngine.secureWipe(it) }
            secretJson?.let { cryptoEngine.secureWipe(it) }
            cryptoEngine.secureWipe(recordKey)
        }
    }

    private suspend fun healthTagIds(credentialId: String): Set<TagId> =
        credentialDao
            .getTagCrossRefsForCredential(credentialId)
            .mapTo(mutableSetOf()) { reference ->
                reference.tagId.requireRecordIdentifier("Tag ID")
                TagId(reference.tagId)
            }
            .also { require(it.size <= MAX_TAG_COUNT) }

    private suspend fun resolveHealthPasswordChangedAt(
        entity: CredentialRecordEntity,
        secret: HealthSecretPayload,
    ): Long? = if (secret.password.isNullOrEmpty()) {
        null
    } else {
        secret.passwordChangedAtEpochMillis
            ?: passwordHistoryDao.getLatestByCredential(entity.id)?.changedAt
            ?: entity.createdAt
    }

    private fun buildHealthInput(
        entity: CredentialRecordEntity,
        summary: SummaryPayload,
        username: SensitiveText?,
        email: SensitiveText?,
        password: SensitiveText?,
        tagIds: Set<TagId>,
        passwordChangedAtEpochMillis: Long?,
    ): CredentialHealthInput = CredentialHealthInput(
        id = CredentialId(entity.id),
        type = entity.type.toCredentialType(),
        title = summary.title,
        username = username,
        email = email,
        password = password,
        isFavorite = entity.isFavorite,
        folderId = entity.folderId?.let(::FolderId),
        tagIds = tagIds,
        createdAt = Instant.fromEpochMilliseconds(entity.createdAt),
        updatedAt = Instant.fromEpochMilliseconds(entity.updatedAt),
        lastUsedAt = entity.lastUsedAt?.let(Instant::fromEpochMilliseconds),
        passwordHealth = summary.passwordHealth.toDomain(),
        passwordChangedAt = passwordChangedAtEpochMillis?.let(Instant::fromEpochMilliseconds),
    )

    private fun validateHealthInput(input: CredentialHealthInput) {
        input.id.value.requireRecordIdentifier("Credential ID")
        input.folderId?.value?.requireRecordIdentifier("Folder ID")
        input.tagIds.forEach { it.value.requireRecordIdentifier("Tag ID") }
        require(
            input.title.isNotBlank() &&
                input.title.hasAtMostCodePoints(MAX_TITLE_LENGTH) &&
                input.title.hasOnlySafeTextCodePoints(),
        )
        require(input.username.hasValidSingleLineEncodingAndLength(MAX_SENSITIVE_LENGTH))
        require(input.email.hasValidSingleLineEncodingAndLength(MAX_SENSITIVE_LENGTH))
        require(input.password.hasValidEncodingAndLength(MAX_SENSITIVE_LENGTH))
        (input.type as? CredentialType.Custom)?.id?.requireRecordIdentifier("Custom credential type ID")
    }

    private suspend fun decryptSummary(
        projection: CredentialSummaryProjection,
        vek: ByteArray,
        tagIds: Set<TagId>,
    ): CredentialSummary.Decrypted {
        projection.id.requireRecordIdentifier("Credential ID")
        projection.folderId?.requireRecordIdentifier("Folder ID")
        tagIds.forEach { it.value.requireRecordIdentifier("Tag ID") }
        require(tagIds.size <= MAX_TAG_COUNT)
        val recordKey = deriveRecordKey(vek, "record:${projection.id}")
        var summaryJson: ByteArray? = null
        return try {
            summaryJson = decryptPayload(
                ciphertext = projection.summaryPayload,
                nonce = projection.summaryNonce,
                key = recordKey,
                associatedData = credentialAssociatedData(projection.id, "summary"),
            )
            val summary = json.decodeFromString<SummaryPayload>(summaryJson.decodeUtf8Strict())
            validateSummaryPayload(summary)

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

    private suspend fun AttachmentRecordEntity.toDomainModel(
        expectedCredentialId: String,
        vek: ByteArray,
    ): AttachmentMetadata {
        id.requireRecordIdentifier("Attachment ID")
        credentialId.requireRecordIdentifier("Credential ID")
        require(credentialId == expectedCredentialId)
        keyDerivationContext.requireRecordIdentifier("Attachment key context")
        require(mimeType.isNotBlank() && mimeType.hasAtMostCodePoints(MAX_ATTACHMENT_MIME_TYPE_LENGTH))
        require(mimeType.hasOnlySafeTextCodePoints())
        require(sizeBytes in 0..MAX_ATTACHMENT_SIZE_BYTES)
        val storageKind = requireStableStorageKind()
        if (storageKind == AttachmentStorageKind.MANAGED) {
            require(sizeBytes <= AttachmentPolicy.MAX_FILE_SIZE_BYTES)
            require(storagePath.isManagedAttachmentObjectPath())
        }
        val attachmentKey = deriveRecordKey(vek, "attachment:$keyDerivationContext")
        var filenameBytes: ByteArray? = null
        return try {
            filenameBytes = decryptPayload(
                ciphertext = encryptedFilename,
                nonce = filenameNonce,
                key = attachmentKey,
                associatedData = attachmentAssociatedData(id, credentialId),
            )
            val filename = filenameBytes.decodeUtf8Strict()
            require(filename.isNotBlank() && filename.hasAtMostCodePoints(MAX_ATTACHMENT_FILENAME_LENGTH)) {
                "Attachment filename is invalid"
            }
            require(
                filename != "." &&
                    filename != ".." &&
                    filename.hasOnlySafeTextCodePoints() &&
                    filename.none { it == '/' || it == '\\' },
            ) {
                "Attachment filename contains unsafe characters"
            }

            AttachmentMetadata(
                id = AttachmentId(id),
                fileName = filename,
                mimeType = mimeType,
                sizeBytes = sizeBytes,
                createdAt = Instant.fromEpochMilliseconds(createdAt),
                availability = when (storageKind) {
                    AttachmentStorageKind.MANAGED -> AttachmentAvailability.AVAILABLE
                    AttachmentStorageKind.LEGACY -> AttachmentAvailability.LEGACY_METADATA_ONLY
                },
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

    private fun validatePasswordHealth(health: PasswordHealth) {
        val ageDays = health.ageDays
        require(ageDays == null || ageDays >= 0)
    }

    private fun SerializedPasswordHealth.toDomain() = PasswordHealth(
        score = runCatching { PasswordScore.valueOf(score) }.getOrDefault(PasswordScore.UNKNOWN),
        isDuplicate = isDuplicate,
        isWeak = isWeak,
        isOld = isOld,
        ageDays = ageDays,
    ).also { health ->
        validatePasswordHealth(health)
    }

    private fun validateSummaryPayload(summary: SummaryPayload) {
        require(
            summary.title.isNotBlank() &&
                summary.title.hasAtMostCodePoints(MAX_TITLE_LENGTH) &&
                summary.title.hasOnlySafeTextCodePoints(),
        )
        require(summary.usernameHint.hasValidSingleLineEncodingAndLength(MAX_SENSITIVE_LENGTH))
        require(summary.emailHint.hasValidSingleLineEncodingAndLength(MAX_SENSITIVE_LENGTH))
        summary.passwordHealth.toDomain()
    }

    private fun validateSummarySecretConsistency(
        summary: SummaryPayload,
        username: String?,
        email: String?,
    ) {
        require(summary.usernameHint == username)
        require(summary.emailHint == email)
    }

    private fun validateSecretPayload(secret: SecretPayload) {
        require(secret.username.hasValidSingleLineEncodingAndLength(MAX_SENSITIVE_LENGTH))
        require(secret.email.hasValidSingleLineEncodingAndLength(MAX_SENSITIVE_LENGTH))
        require(secret.password.hasValidEncodingAndLength(MAX_SENSITIVE_LENGTH))
        require(secret.notes.hasValidEncodingAndLength(MAX_NOTES_LENGTH))
        require(secret.urls.size <= MAX_URL_COUNT)
        require(
            secret.urls.all { value ->
                value.hasAtMostCodePoints(MAX_URL_LENGTH) && UrlValue(value).host() != null
            },
        )
        require(secret.recoveryCodes.isValidSensitiveList())
        require(secret.apiKeys.isValidSensitiveList())
        require(secret.licenseKeys.isValidSensitiveList())
        validateSerializedCustomFields(secret.customFields)
        require(secret.password?.isNotEmpty() == true || secret.passwordChangedAtEpochMillis == null)
    }

    private fun List<String>.isValidSensitiveList(): Boolean =
        size <= MAX_SECRET_LIST_COUNT && all { it.hasValidEncodingAndLength(MAX_SENSITIVE_LENGTH) }

    private fun validateSerializedCustomFields(fields: List<SerializedCustomField>) {
        require(fields.size <= MAX_CUSTOM_FIELD_COUNT)
        val ids = fields.map { field ->
            field.id.requireRecordIdentifier("Custom field ID")
            require(
                field.name.isNotBlank() &&
                    field.name.hasAtMostCodePoints(MAX_CUSTOM_FIELD_NAME_LENGTH) &&
                    field.name.hasOnlySafeTextCodePoints() &&
                    field.value.hasValidEncodingAndLength(MAX_CUSTOM_FIELD_VALUE_LENGTH),
            )
            field.id
        }
        require(ids.size == ids.toSet().size) { "Custom field IDs must be unique" }
    }

    @Suppress("TooGenericExceptionCaught") // Invalid decoded configurations must clear their copied secret.
    private fun SerializedTotp.toDomain(): TotpConfiguration {
        val parsedAlgorithm = runCatching { TotpAlgorithm.valueOf(algorithm) }
            .getOrElse { throw IllegalArgumentException("Invalid TOTP configuration") }
        val configuration = TotpConfiguration(
            secret = SensitiveText.from(secret),
            issuer = issuer,
            accountName = accountName,
            algorithm = parsedAlgorithm,
            digits = digits,
            periodSeconds = periodSeconds,
        )
        return try {
            require(configuration.secret.toStringUnsafe().isValidTotpSecret())
            require(configuration.issuer.isValidTotpLabel())
            require(configuration.accountName.isValidTotpLabel())
            require(configuration.digits in SUPPORTED_TOTP_DIGITS)
            require(configuration.periodSeconds in MIN_TOTP_PERIOD_SECONDS..MAX_TOTP_PERIOD_SECONDS)
            configuration
        } catch (error: Exception) {
            configuration.clear()
            throw error
        }
    }

    private fun String?.isValidTotpLabel(): Boolean =
        this == null || (
            isNotBlank() &&
                hasAtMostCodePoints(MAX_TOTP_LABEL_LENGTH) &&
                hasOnlySafeTextCodePoints()
        )

    private fun String.isValidTotpSecret(): Boolean {
        val hasValidEncodingShape =
            length in MIN_TOTP_SECRET_LENGTH..MAX_TOTP_SECRET_LENGTH &&
                all { it in BASE32_ALPHABET } &&
                length % BASE32_BLOCK_CHARACTERS in VALID_BASE32_REMAINDERS
        if (!hasValidEncodingShape) return false

        val decodedByteCount = length * BASE32_BITS_PER_CHARACTER / BITS_PER_BYTE
        val unusedBitCount = length * BASE32_BITS_PER_CHARACTER % BITS_PER_BYTE
        val lastValue = BASE32_ALPHABET.indexOf(last())
        val hasCanonicalTrailingBits = unusedBitCount == 0 ||
            lastValue and ((1 shl unusedBitCount) - 1) == 0
        return decodedByteCount in MIN_TOTP_SECRET_BYTES..MAX_TOTP_SECRET_BYTES &&
            hasCanonicalTrailingBits
    }

    private fun SensitiveText?.hasValidEncodingAndLength(maxLength: Int): Boolean =
        this == null || (length <= maxLength && hasWellFormedUnicode())

    private fun SensitiveText?.hasValidSingleLineEncodingAndLength(maxLength: Int): Boolean =
        this == null || (
            length <= maxLength &&
                hasWellFormedUnicode() &&
                hasOnlySafeSingleLineCodePoints()
        )

    private fun String?.hasValidEncodingAndLength(maxLength: Int): Boolean =
        this == null || hasAtMostCodePoints(maxLength)

    private fun String?.hasValidSingleLineEncodingAndLength(maxLength: Int): Boolean =
        this == null || (hasAtMostCodePoints(maxLength) && hasOnlySafeTextCodePoints())

    private fun CredentialHealthInput.clearSensitiveValues() {
        username?.clear()
        email?.clear()
        password?.clear()
    }

    private suspend fun PasswordHistoryRecordEntity.toDomainModel(
        expectedCredentialId: String,
        vek: ByteArray,
    ): PasswordHistoryEntry {
        id.requireRecordIdentifier("Password history ID")
        credentialId.requireRecordIdentifier("Credential ID")
        require(credentialId == expectedCredentialId)
        val historyKey = deriveRecordKey(vek, "history:$id")
        var passwordBytes: ByteArray? = null
        return try {
            passwordBytes = decryptPayload(
                ciphertext = encryptedPassword,
                nonce = passwordNonce,
                key = historyKey,
                associatedData = historyAssociatedData(id, credentialId),
            )
            require(passwordBytes.size <= MAX_SENSITIVE_UTF8_BYTES) {
                "Password history value exceeds the supported limit"
            }
            val password = passwordBytes.decodeUtf8Strict()
            require(password.hasValidEncodingAndLength(MAX_SENSITIVE_LENGTH)) {
                "Password history value exceeds the supported limit"
            }
            PasswordHistoryEntry(
                password = SensitiveText.from(password),
                changedAt = Instant.fromEpochMilliseconds(changedAt),
            )
        } finally {
            passwordBytes?.let { cryptoEngine.secureWipe(it) }
            cryptoEngine.secureWipe(historyKey)
        }
    }

    @Suppress("TooGenericExceptionCaught") // Clear any history values decoded before a later row fails.
    private suspend fun decryptPasswordHistory(
        credentialId: String,
        vek: ByteArray,
    ): List<PasswordHistoryEntry> {
        val entities = passwordHistoryDao.getByCredential(credentialId)
        require(entities.size <= MAX_PASSWORD_HISTORY_COUNT) {
            "Password history exceeds the supported limit"
        }
        val history = mutableListOf<PasswordHistoryEntry>()
        try {
            entities.forEach { entity ->
                history += entity.toDomainModel(credentialId, vek)
            }
            return history
        } catch (error: Exception) {
            history.forEach { it.password.clear() }
            throw error
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
        require(ciphertext.size <= MAX_CREDENTIAL_ENCRYPTED_PAYLOAD_BYTES)
        return PaddedPayload.decrypt(
            cryptoEngine = cryptoEngine,
            storedCiphertext = ciphertext,
            nonce = nonce,
            key = key,
            associatedData = associatedData,
            maxPlaintextBytes = MAX_CREDENTIAL_PLAINTEXT_BYTES,
        ).getOrThrow()
    }

    private fun credentialAssociatedData(id: String, purpose: String): ByteArray =
        "passvault:credential:$id:$purpose:v1".encodeToByteArray()

    private fun historyAssociatedData(historyId: String, credentialId: String): ByteArray =
        "passvault:history:$historyId:$credentialId:v2".encodeToByteArray()

    private fun attachmentAssociatedData(attachmentId: String, credentialId: String): ByteArray =
        "passvault:attachment:$attachmentId:$credentialId:filename:v1".encodeToByteArray()

    private fun String.isManagedAttachmentObjectPath(): Boolean {
        val objectId = removePrefix("objects/").removeSuffix(".pva")
        return startsWith("objects/") &&
            endsWith(".pva") &&
            objectId.length == 36 &&
            objectId.indices.all { index ->
                if (index in UUID_HYPHEN_INDICES) {
                    objectId[index] == '-'
                } else {
                    objectId[index] in '0'..'9' || objectId[index] in 'a'..'f'
                }
            }
    }

    private companion object {
        const val MAX_ATTACHMENT_FILENAME_LENGTH = 255
        const val MAX_ATTACHMENT_MIME_TYPE_LENGTH = 255
        const val MAX_ATTACHMENT_SIZE_BYTES = 4L * 1024L * 1024L * 1024L
        val UUID_HYPHEN_INDICES = setOf(8, 13, 18, 23)
        const val TOTP_VAULT_FORMAT_VERSION = 2
        const val MIN_TOTP_SECRET_LENGTH = 16
        const val MAX_TOTP_SECRET_LENGTH = 205
        const val MIN_TOTP_SECRET_BYTES = 10
        const val MAX_TOTP_SECRET_BYTES = 128
        const val BASE32_BITS_PER_CHARACTER = 5
        const val BASE32_BLOCK_CHARACTERS = 8
        const val BITS_PER_BYTE = 8
        const val MAX_TOTP_LABEL_LENGTH = 200
        const val MIN_TOTP_PERIOD_SECONDS = 5
        const val MAX_TOTP_PERIOD_SECONDS = 300
        const val DEFAULT_TOTP_DIGITS = 6
        const val DEFAULT_TOTP_PERIOD_SECONDS = 30
        const val BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        val SUPPORTED_TOTP_DIGITS = setOf(6, 8)
        val VALID_BASE32_REMAINDERS = setOf(0, 2, 4, 5, 7)
        const val MAX_TITLE_LENGTH = 200
        const val MAX_SENSITIVE_LENGTH = 4_096
        const val MAX_SENSITIVE_UTF8_BYTES = MAX_SENSITIVE_LENGTH * 4
        const val MAX_NOTES_LENGTH = 100_000
        const val MAX_URL_LENGTH = 2_048
        const val MAX_URL_COUNT = 100
        const val MAX_SECRET_LIST_COUNT = 100
        const val MAX_CUSTOM_FIELD_COUNT = 50
        const val MAX_CUSTOM_FIELD_NAME_LENGTH = 200
        const val MAX_CUSTOM_FIELD_VALUE_LENGTH = 20_000
        const val MAX_TAG_COUNT = 100
        const val MAX_PASSWORD_HISTORY_COUNT = 10
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
    this.startsWith("Custom:") -> {
        val id = substringAfter(":")
        id.requireRecordIdentifier("Custom credential type ID")
        CredentialType.Custom(id)
    }
    else -> throw IllegalArgumentException("Unsupported credential type")
}
