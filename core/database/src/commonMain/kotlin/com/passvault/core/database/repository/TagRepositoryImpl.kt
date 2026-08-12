package com.passvault.core.database.repository

import com.passvault.core.crypto.CryptoEngine
import com.passvault.core.crypto.CryptoEnvelope
import com.passvault.core.crypto.EncryptedData
import com.passvault.core.database.dao.TagDao
import com.passvault.core.database.entity.TagRecordEntity
import com.passvault.core.database.entity.TagWithCountProjection
import com.passvault.core.domain.model.Tag
import com.passvault.core.domain.model.TagId
import com.passvault.core.domain.repository.TagRepository
import kotlin.time.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Repository implementation for tag operations.
 * Handles encryption/decryption of tag data.
 */
class TagRepositoryImpl(
    private val tagDao: TagDao,
    private val cryptoEngine: CryptoEngine,
    private val sessionManager: VaultSessionManager,
) : TagRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Serializable
    private data class TagPayload(
        val name: String,
        val description: String?,
    )

    // ==================== Read Operations ====================

    override suspend fun getAll(): Result<List<Tag>> {
        return repositoryResult {
            sessionManager.withUnlockedSession { vek ->
                val projections = tagDao.getAllWithCount()
                projections.map { projection ->
                    decryptTag(projection, vek)
                }
            }
        }
    }

    override suspend fun getById(id: TagId): Result<Tag?> {
        return repositoryResult {
            sessionManager.withUnlockedSession { vek ->
                id.value.requireRecordIdentifier("Tag ID")
                val entity = tagDao.getById(id.value)
                    ?: return@withUnlockedSession null

                val projection = TagWithCountProjection(
                    id = entity.id,
                    encryptedPayload = entity.encryptedPayload,
                    payloadNonce = entity.payloadNonce,
                    color = entity.color,
                    credentialCount = tagDao.getCredentialCount(id.value)
                )

                decryptTag(projection, vek)
            }
        }
    }

    // ==================== Write Operations ====================

    override suspend fun save(tag: Tag): Result<TagId> {
        return repositoryResult {
            sessionManager.withUnlockedSession { vek ->
                tag.id.value.requireRecordIdentifier("Tag ID")
                val normalizedName = tag.name.trim()
                require(normalizedName.isNotEmpty()) { "Tag name is required" }
                require(
                    normalizedName.hasAtMostCodePoints(MAX_TAG_NAME_LENGTH) &&
                        normalizedName.hasOnlySafeTextCodePoints(),
                ) {
                    "Tag name is too long"
                }
                tag.color.requireBoundedMetadata("Tag color", MAX_TAG_COLOR_LENGTH)
                val createdAt = tagDao.getById(tag.id.value)?.createdAt
                    ?: Clock.System.now().toEpochMilliseconds()
                val entity = encryptTag(tag.copy(name = normalizedName), vek, createdAt)
                tagDao.insertOrUpdate(entity)
                tag.id
            }
        }
    }

    override suspend fun delete(id: TagId): Result<Unit> {
        return repositoryResult {
            sessionManager.withUnlockedSession {
                id.value.requireRecordIdentifier("Tag ID")
                require(tagDao.exists(id.value)) { "Tag does not exist" }
                tagDao.deleteTagAndRemoveReferences(id.value)
            }
        }
    }

    // ==================== Helper Functions ====================

    private suspend fun deriveTagKey(vek: ByteArray, tagId: String): ByteArray {
        return cryptoEngine.deriveSubkey(vek, "tag:$tagId", 32).getOrThrow()
    }

    private suspend fun encryptTag(
        tag: Tag,
        vek: ByteArray,
        createdAt: Long,
    ): TagRecordEntity {
        val tagKey = deriveTagKey(vek, tag.id.value)
        var nameHash: ByteArray? = null
        var payloadJson: ByteArray? = null
        var encryptedPayload: EncryptedData? = null
        return try {
            val blindIndex = cryptoEngine.deriveSubkey(
                vek,
                "blind-index:tag-name:${tag.name.trim().lowercase()}",
                32,
            ).getOrThrow()
            nameHash = blindIndex
            require(
                tagDao.searchByNameHash(blindIndex)
                    .none { it.id != tag.id.value },
            ) { "A tag with this name already exists" }
            val payload = TagPayload(
                name = tag.name,
                description = null,
            )
            payloadJson = json.encodeToString(payload).encodeToByteArray()
            require(payloadJson.size <= MAX_TAG_ENCRYPTED_PAYLOAD_BYTES)
            val encrypted = cryptoEngine.encrypt(
                plaintext = payloadJson,
                key = tagKey,
                associatedData = tagAssociatedData(tag.id.value),
            ).getOrThrow()
            encryptedPayload = encrypted
            TagRecordEntity(
                id = tag.id.value,
                nameHash = blindIndex.copyOf(),
                encryptedPayload = CryptoEnvelope.encode(encrypted),
                payloadNonce = encrypted.nonce.copyOf(),
                color = tag.color,
                createdAt = createdAt,
            )
        } finally {
            payloadJson?.let { cryptoEngine.secureWipe(it) }
            encryptedPayload?.clear()
            cryptoEngine.secureWipe(tagKey)
            nameHash?.let { cryptoEngine.secureWipe(it) }
        }
    }

    private suspend fun decryptTag(
        projection: TagWithCountProjection,
        vek: ByteArray
    ): Tag {
        projection.id.requireRecordIdentifier("Tag ID")
        require(projection.credentialCount >= 0) { "Stored tag credential count is invalid" }
        val tagKey = deriveTagKey(vek, projection.id)
        var payloadJson: ByteArray? = null
        return try {
            require(projection.encryptedPayload.size <= MAX_TAG_ENCRYPTED_PAYLOAD_BYTES)
            payloadJson = cryptoEngine.decrypt(
                CryptoEnvelope.normalize(projection.encryptedPayload),
                projection.payloadNonce,
                tagKey,
                tagAssociatedData(projection.id),
            ).getOrThrow()
            val payload = json.decodeFromString<TagPayload>(payloadJson.decodeUtf8Strict())
            require(
                payload.name.isNotBlank() &&
                    payload.name.hasAtMostCodePoints(MAX_TAG_NAME_LENGTH) &&
                    payload.name.hasOnlySafeTextCodePoints(),
            ) {
                "Stored tag name is invalid"
            }
            projection.color.requireBoundedMetadata("Stored tag color", MAX_TAG_COLOR_LENGTH)

            Tag(
                id = TagId(projection.id),
                name = payload.name,
                color = projection.color,
            )
        } finally {
            payloadJson?.let { cryptoEngine.secureWipe(it) }
            cryptoEngine.secureWipe(tagKey)
        }
    }

    private fun tagAssociatedData(id: String): ByteArray =
        "passvault:tag:$id:payload:v1".encodeToByteArray()

    private companion object {
        const val MAX_TAG_NAME_LENGTH = 256
        const val MAX_TAG_COLOR_LENGTH = 64
    }
}
