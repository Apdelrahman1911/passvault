package com.passvault.core.database.repository

import com.passvault.core.crypto.CryptoEngine
import com.passvault.core.crypto.CryptoEnvelope
import com.passvault.core.database.dao.TagDao
import com.passvault.core.database.entity.TagRecordEntity
import com.passvault.core.database.entity.TagWithCountProjection
import com.passvault.core.domain.model.Tag
import com.passvault.core.domain.model.TagId
import com.passvault.core.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
            val vek = sessionManager.getCurrentVek()
                ?: return Result.failure(IllegalStateException("Vault not unlocked"))

            try {
                val projections = tagDao.getAllWithCount()
                projections.map { projection ->
                    decryptTag(projection, vek)
                }
            } finally {
                cryptoEngine.secureWipe(vek)
            }
        }
    }

    override suspend fun getById(id: TagId): Result<Tag?> {
        return repositoryResult {
            val vek = sessionManager.getCurrentVek()
                ?: return Result.failure(IllegalStateException("Vault not unlocked"))

            try {
                val entity = tagDao.getById(id.value)
                    ?: return@repositoryResult null

                val projection = TagWithCountProjection(
                    id = entity.id,
                    encryptedPayload = entity.encryptedPayload,
                    payloadNonce = entity.payloadNonce,
                    color = entity.color,
                    credentialCount = tagDao.getCredentialCount(id.value)
                )

                decryptTag(projection, vek)
            } finally {
                cryptoEngine.secureWipe(vek)
            }
        }
    }

    // ==================== Write Operations ====================

    override suspend fun save(tag: Tag): Result<TagId> {
        return repositoryResult {
            val vek = sessionManager.getCurrentVek()
                ?: return Result.failure(IllegalStateException("Vault not unlocked"))

            try {
                require(tag.name.isNotBlank()) { "Tag name is required" }
                require(tag.name.length <= MAX_TAG_NAME_LENGTH) {
                    "Tag name is too long"
                }
                val entity = encryptTag(tag, vek)
                tagDao.insertOrUpdate(entity)
                tag.id
            } finally {
                cryptoEngine.secureWipe(vek)
            }
        }
    }

    override suspend fun delete(id: TagId): Result<Unit> {
        return repositoryResult {
            if (!isVaultUnlocked()) {
                return Result.failure(IllegalStateException("Vault not unlocked"))
            }
            tagDao.deleteTagAndRemoveReferences(id.value)
        }
    }

    // ==================== Helper Functions ====================

    private suspend fun deriveTagKey(vek: ByteArray, tagId: String): ByteArray {
        return cryptoEngine.deriveSubkey(vek, "tag:$tagId", 32).getOrThrow()
    }

    private suspend fun isVaultUnlocked(): Boolean {
        val vek = sessionManager.getCurrentVek() ?: return false
        cryptoEngine.secureWipe(vek)
        return true
    }

    private suspend fun encryptTag(tag: Tag, vek: ByteArray): TagRecordEntity {
        val tagKey = deriveTagKey(vek, tag.id.value)
        val nameHash = cryptoEngine.deriveSubkey(
            vek,
            "blind-index:tag-name:${tag.name.trim().lowercase()}",
            32,
        ).getOrThrow()
        var payloadJson: ByteArray? = null
        return try {
            require(
                tagDao.searchByNameHash(nameHash)
                    .none { it.id != tag.id.value },
            ) { "A tag with this name already exists" }
            val payload = TagPayload(
                name = tag.name,
                description = null,
            )
            payloadJson = json.encodeToString(payload).encodeToByteArray()
            val encrypted = cryptoEngine.encrypt(
                plaintext = payloadJson,
                key = tagKey,
                associatedData = tagAssociatedData(tag.id.value),
            ).getOrThrow()
            TagRecordEntity(
                id = tag.id.value,
                nameHash = nameHash.copyOf(),
                encryptedPayload = CryptoEnvelope.encode(encrypted),
                payloadNonce = encrypted.nonce,
                color = tag.color,
                createdAt = Clock.System.now().toEpochMilliseconds(),
            )
        } finally {
            payloadJson?.let { cryptoEngine.secureWipe(it) }
            cryptoEngine.secureWipe(tagKey)
        }
    }

    private suspend fun decryptTag(
        projection: TagWithCountProjection,
        vek: ByteArray
    ): Tag {
        val tagKey = deriveTagKey(vek, projection.id)
        var payloadJson: ByteArray? = null
        return try {
            payloadJson = cryptoEngine.decrypt(
                CryptoEnvelope.normalize(projection.encryptedPayload),
                projection.payloadNonce,
                tagKey,
                tagAssociatedData(projection.id),
            ).getOrThrow()
            val payload = json.decodeFromString<TagPayload>(payloadJson.decodeToString())

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
    }
}
