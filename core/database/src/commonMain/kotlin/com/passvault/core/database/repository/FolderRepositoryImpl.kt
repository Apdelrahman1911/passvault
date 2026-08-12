package com.passvault.core.database.repository

import com.passvault.core.crypto.CryptoEngine
import com.passvault.core.crypto.CryptoEnvelope
import com.passvault.core.crypto.EncryptedData
import com.passvault.core.database.dao.FolderDao
import com.passvault.core.database.entity.FolderRecordEntity
import com.passvault.core.database.entity.FolderSummaryProjection
import com.passvault.core.domain.model.Folder
import com.passvault.core.domain.model.FolderId
import com.passvault.core.domain.repository.FolderRepository
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Repository implementation for folder operations.
 * Handles encryption/decryption of folder data.
 */
class FolderRepositoryImpl(
    private val folderDao: FolderDao,
    private val cryptoEngine: CryptoEngine,
    private val sessionManager: VaultSessionManager,
) : FolderRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Serializable
    private data class FolderPayload(
        val name: String,
        val description: String?,
        val color: String?,
    )

    // ==================== Read Operations ====================

    override suspend fun getAll(): Result<List<Folder>> {
        return repositoryResult {
            sessionManager.withUnlockedSession { vek ->
                val projections = folderDao.getAllSummaries()
                validateFolderHierarchy(projections)
                projections.map { projection ->
                    decryptFolder(projection, vek)
                }
            }
        }
    }

    override suspend fun getById(id: FolderId): Result<Folder?> {
        return repositoryResult {
            sessionManager.withUnlockedSession { vek ->
                id.value.requireRecordIdentifier("Folder ID")
                val entity = folderDao.getById(id.value)
                    ?: return@withUnlockedSession null
                validateStoredParentChain(entity)

                val projection = FolderSummaryProjection(
                    id = entity.id,
                    parentId = entity.parentId,
                    encryptedPayload = entity.encryptedPayload,
                    payloadNonce = entity.payloadNonce,
                    icon = entity.icon,
                    sortOrder = entity.sortOrder,
                    createdAt = entity.createdAt,
                    updatedAt = entity.updatedAt,
                )

                decryptFolder(projection, vek)
            }
        }
    }

    // ==================== Write Operations ====================

    override suspend fun save(folder: Folder): Result<FolderId> {
        return repositoryResult {
            sessionManager.withUnlockedSession { vek ->
                folder.id.value.requireRecordIdentifier("Folder ID")
                folder.parentId?.value?.requireRecordIdentifier("Parent folder ID")
                val normalizedName = folder.name.trim()
                require(normalizedName.isNotEmpty()) { "Folder name is required" }
                require(
                    normalizedName.hasAtMostCodePoints(MAX_FOLDER_NAME_LENGTH) &&
                        normalizedName.hasOnlySafeTextCodePoints(),
                ) {
                    "Folder name is too long"
                }
                folder.icon.requireBoundedMetadata("Folder icon", MAX_FOLDER_ICON_LENGTH)
                require(folder.sortOrder >= 0) { "Folder sort order must not be negative" }
                validateParent(folder)
                val existingCreatedAt = folderDao.getById(folder.id.value)?.createdAt
                val entity = encryptFolder(
                    folder = folder.copy(name = normalizedName),
                    vek = vek,
                    createdAt = existingCreatedAt ?: folder.createdAt.toEpochMilliseconds(),
                )
                folderDao.insertOrUpdate(entity)
                folder.id
            }
        }
    }

    override suspend fun delete(id: FolderId): Result<Unit> {
        return repositoryResult {
            sessionManager.withUnlockedSession {
                id.value.requireRecordIdentifier("Folder ID")
                require(folderDao.exists(id.value)) { "Folder does not exist" }
                // The folder column is canonical; reparent and delete atomically.
                folderDao.deleteAndMoveCredentialsToRoot(id.value)
            }
        }
    }

    override suspend fun reorder(id: FolderId, newOrder: Int): Result<Unit> {
        return repositoryResult {
            sessionManager.withUnlockedSession {
                id.value.requireRecordIdentifier("Folder ID")
                require(newOrder >= 0) { "Folder sort order must not be negative" }
                require(folderDao.exists(id.value)) { "Folder does not exist" }
                folderDao.updateSortOrder(id.value, newOrder)
            }
        }
    }

    // ==================== Helper Functions ====================

    private suspend fun deriveFolderKey(vek: ByteArray, folderId: String): ByteArray {
        return cryptoEngine.deriveSubkey(vek, "folder:$folderId", 32).getOrThrow()
    }

    private suspend fun encryptFolder(
        folder: Folder,
        vek: ByteArray,
        createdAt: Long,
    ): FolderRecordEntity {
        val folderKey = deriveFolderKey(vek, folder.id.value)
        var nameHash: ByteArray? = null
        var payloadJson: ByteArray? = null
        var encryptedPayload: EncryptedData? = null
        return try {
            val blindIndex = cryptoEngine.deriveSubkey(
                vek,
                "blind-index:folder-name:${folder.name.trim().lowercase()}",
                32,
            ).getOrThrow()
            nameHash = blindIndex
            require(
                folderDao.searchByNameHash(blindIndex)
                    .none { it.id != folder.id.value },
            ) { "A folder with this name already exists" }
            val payload = FolderPayload(
                name = folder.name,
                description = null,
                color = null,
            )
            payloadJson = json.encodeToString(payload).encodeToByteArray()
            require(payloadJson.size <= MAX_FOLDER_ENCRYPTED_PAYLOAD_BYTES)
            val encrypted = cryptoEngine.encrypt(
                plaintext = payloadJson,
                key = folderKey,
                associatedData = folderAssociatedData(folder.id.value),
            ).getOrThrow()
            encryptedPayload = encrypted
            FolderRecordEntity(
                id = folder.id.value,
                parentId = folder.parentId?.value,
                nameHash = blindIndex.copyOf(),
                encryptedPayload = CryptoEnvelope.encode(encrypted),
                payloadNonce = encrypted.nonce.copyOf(),
                icon = folder.icon,
                sortOrder = folder.sortOrder,
                createdAt = createdAt,
                updatedAt = Clock.System.now().toEpochMilliseconds(),
            )
        } finally {
            payloadJson?.let { cryptoEngine.secureWipe(it) }
            encryptedPayload?.clear()
            cryptoEngine.secureWipe(folderKey)
            nameHash?.let { cryptoEngine.secureWipe(it) }
        }
    }

    private suspend fun decryptFolder(
        projection: FolderSummaryProjection,
        vek: ByteArray
    ): Folder {
        projection.id.requireRecordIdentifier("Folder ID")
        projection.parentId?.requireRecordIdentifier("Parent folder ID")
        val folderKey = deriveFolderKey(vek, projection.id)
        var payloadJson: ByteArray? = null
        return try {
            require(projection.encryptedPayload.size <= MAX_FOLDER_ENCRYPTED_PAYLOAD_BYTES)
            val authenticated = cryptoEngine.decrypt(
                CryptoEnvelope.normalize(projection.encryptedPayload),
                projection.payloadNonce,
                folderKey,
                folderAssociatedData(projection.id)
            )
            payloadJson = authenticated.getOrThrow()
            val payload = json.decodeFromString<FolderPayload>(payloadJson.decodeUtf8Strict())
            require(
                payload.name.isNotBlank() &&
                    payload.name.hasAtMostCodePoints(MAX_FOLDER_NAME_LENGTH) &&
                    payload.name.hasOnlySafeTextCodePoints(),
            ) {
                "Stored folder name is invalid"
            }
            projection.icon.requireBoundedMetadata("Stored folder icon", MAX_FOLDER_ICON_LENGTH)
            require(projection.sortOrder >= 0) { "Stored folder sort order is invalid" }

            Folder(
                id = FolderId(projection.id),
                parentId = projection.parentId?.let { FolderId(it) },
                name = payload.name,
                icon = projection.icon,
                sortOrder = projection.sortOrder,
                createdAt = Instant.fromEpochMilliseconds(projection.createdAt)
            )
        } finally {
            cryptoEngine.secureWipe(folderKey)
            payloadJson?.let { cryptoEngine.secureWipe(it) }
        }
    }

    private suspend fun validateParent(folder: Folder) {
        val parentId = folder.parentId?.value ?: return
        require(parentId != folder.id.value) { "A folder cannot contain itself" }
        val existingParent = folderDao.getById(parentId)
            ?: throw IllegalArgumentException("Parent folder does not exist")

        val seen = mutableSetOf(folder.id.value)
        var current: String? = existingParent.parentId
        while (current != null) {
            current.requireRecordIdentifier("Parent folder ID")
            require(seen.add(current)) { "Folder hierarchy contains a cycle" }
            val ancestor = folderDao.getById(current)
                ?: throw IllegalArgumentException("Folder hierarchy contains a missing parent")
            current = ancestor.parentId
        }
    }

    private suspend fun validateStoredParentChain(folder: FolderRecordEntity) {
        val seen = mutableSetOf(folder.id)
        var current = folder.parentId
        while (current != null) {
            current.requireRecordIdentifier("Parent folder ID")
            require(seen.add(current)) { "Folder hierarchy contains a cycle" }
            val ancestor = folderDao.getById(current)
                ?: throw IllegalArgumentException("Folder hierarchy contains a missing parent")
            current = ancestor.parentId
        }
    }

    private fun validateFolderHierarchy(projections: List<FolderSummaryProjection>) {
        val parents = projections.associate { projection ->
            projection.id.requireRecordIdentifier("Folder ID")
            projection.parentId?.requireRecordIdentifier("Parent folder ID")
            projection.id to projection.parentId
        }
        require(parents.size == projections.size) { "Folder IDs must be unique" }
        require(parents.values.all { it == null || it in parents }) {
            "Folder hierarchy contains a missing parent"
        }

        val completed = mutableSetOf<String>()
        parents.keys.forEach { folderId ->
            if (folderId in completed) return@forEach
            val seen = mutableSetOf<String>()
            var current: String? = folderId
            while (current != null && current !in completed) {
                require(seen.add(current)) { "Folder hierarchy contains a cycle" }
                current = parents[current]
            }
            completed.addAll(seen)
        }
    }

    private companion object {
        const val MAX_FOLDER_NAME_LENGTH = 256
        const val MAX_FOLDER_ICON_LENGTH = 64
    }
}

private fun folderAssociatedData(id: String): ByteArray =
    "passvault:folder:$id:payload:v1".encodeToByteArray()
