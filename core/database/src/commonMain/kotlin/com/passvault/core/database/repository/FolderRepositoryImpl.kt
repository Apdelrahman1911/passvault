package com.passvault.core.database.repository

import com.passvault.core.crypto.CryptoEngine
import com.passvault.core.crypto.CryptoEnvelope
import com.passvault.core.database.dao.FolderDao
import com.passvault.core.database.entity.FolderRecordEntity
import com.passvault.core.database.entity.FolderSummaryProjection
import com.passvault.core.domain.model.Folder
import com.passvault.core.domain.model.FolderId
import com.passvault.core.domain.repository.FolderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
            val vek = sessionManager.getCurrentVek()
                ?: return Result.failure(IllegalStateException("Vault not unlocked"))

            try {
                val projections = folderDao.getAllSummaries()
                projections.map { projection ->
                    decryptFolder(projection, vek)
                }
            } finally {
                cryptoEngine.secureWipe(vek)
            }
        }
    }

    override suspend fun getById(id: FolderId): Result<Folder?> {
        return repositoryResult {
            val vek = sessionManager.getCurrentVek()
                ?: return Result.failure(IllegalStateException("Vault not unlocked"))

            try {
                val entity = folderDao.getById(id.value)
                    ?: return@repositoryResult null

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
            } finally {
                cryptoEngine.secureWipe(vek)
            }
        }
    }

    // ==================== Write Operations ====================

    override suspend fun save(folder: Folder): Result<FolderId> {
        return repositoryResult {
            val vek = sessionManager.getCurrentVek()
                ?: return Result.failure(IllegalStateException("Vault not unlocked"))

            try {
                require(folder.name.isNotBlank()) { "Folder name is required" }
                require(folder.name.length <= MAX_FOLDER_NAME_LENGTH) {
                    "Folder name is too long"
                }
                validateParent(folder)
                val entity = encryptFolder(folder, vek)
                folderDao.insertOrUpdate(entity)
                folder.id
            } finally {
                cryptoEngine.secureWipe(vek)
            }
        }
    }

    override suspend fun delete(id: FolderId): Result<Unit> {
        return repositoryResult {
            if (!isVaultUnlocked()) {
                return Result.failure(IllegalStateException("Vault not unlocked"))
            }
            // The folder column is canonical; reparent and delete atomically.
            folderDao.deleteAndMoveCredentialsToRoot(id.value)
        }
    }

    override suspend fun reorder(id: FolderId, newOrder: Int): Result<Unit> {
        return repositoryResult {
            if (!isVaultUnlocked()) {
                return Result.failure(IllegalStateException("Vault not unlocked"))
            }
            folderDao.updateSortOrder(id.value, newOrder)
        }
    }

    // ==================== Helper Functions ====================

    private suspend fun deriveFolderKey(vek: ByteArray, folderId: String): ByteArray {
        return cryptoEngine.deriveSubkey(vek, "folder:$folderId", 32).getOrThrow()
    }

    private suspend fun isVaultUnlocked(): Boolean {
        val vek = sessionManager.getCurrentVek() ?: return false
        cryptoEngine.secureWipe(vek)
        return true
    }

    private suspend fun encryptFolder(folder: Folder, vek: ByteArray): FolderRecordEntity {
        val folderKey = deriveFolderKey(vek, folder.id.value)
        val nameHash = cryptoEngine.deriveSubkey(
            vek,
            "blind-index:folder-name:${folder.name.trim().lowercase()}",
            32,
        ).getOrThrow()
        var payloadJson: ByteArray? = null
        return try {
            require(
                folderDao.searchByNameHash(nameHash)
                    .none { it.id != folder.id.value },
            ) { "A folder with this name already exists" }
            val payload = FolderPayload(
                name = folder.name,
                description = null,
                color = null,
            )
            payloadJson = json.encodeToString(payload).encodeToByteArray()
            val encrypted = cryptoEngine.encrypt(
                plaintext = payloadJson,
                key = folderKey,
                associatedData = folderAssociatedData(folder.id.value),
            ).getOrThrow()
            FolderRecordEntity(
                id = folder.id.value,
                parentId = folder.parentId?.value,
                nameHash = nameHash.copyOf(),
                encryptedPayload = CryptoEnvelope.encode(encrypted),
                payloadNonce = encrypted.nonce,
                icon = folder.icon,
                sortOrder = folder.sortOrder,
                createdAt = folder.createdAt.toEpochMilliseconds(),
                updatedAt = Clock.System.now().toEpochMilliseconds(),
            )
        } finally {
            payloadJson?.let { cryptoEngine.secureWipe(it) }
            cryptoEngine.secureWipe(folderKey)
            cryptoEngine.secureWipe(nameHash)
        }
    }

    private suspend fun decryptFolder(
        projection: FolderSummaryProjection,
        vek: ByteArray
    ): Folder {
        val folderKey = deriveFolderKey(vek, projection.id)
        var payloadJson: ByteArray? = null
        return try {
            val authenticated = cryptoEngine.decrypt(
                CryptoEnvelope.normalize(projection.encryptedPayload),
                projection.payloadNonce,
                folderKey,
                folderAssociatedData(projection.id)
            )
            payloadJson = authenticated.getOrThrow()
            val payload = json.decodeFromString<FolderPayload>(payloadJson.decodeToString())

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
            require(seen.add(current)) { "Folder hierarchy contains a cycle" }
            current = folderDao.getById(current)?.parentId
        }
    }

    private fun folderAssociatedData(id: String): ByteArray =
        "passvault:folder:$id:payload:v1".encodeToByteArray()

    private companion object {
        const val MAX_FOLDER_NAME_LENGTH = 256
    }
}
