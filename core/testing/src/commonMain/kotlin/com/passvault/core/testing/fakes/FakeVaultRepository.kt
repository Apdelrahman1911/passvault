package com.passvault.core.testing.fakes

import com.passvault.core.domain.model.Folder
import com.passvault.core.domain.model.FolderId
import com.passvault.core.domain.model.MasterPasswordPolicy
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.model.SessionId
import com.passvault.core.domain.model.Tag
import com.passvault.core.domain.model.TagId
import com.passvault.core.domain.model.VaultId
import com.passvault.core.domain.model.VaultMetadata
import com.passvault.core.domain.model.VaultSessionState
import com.passvault.core.domain.model.codePointLength
import com.passvault.core.domain.model.hasOnlySafeSingleLineCodePoints
import com.passvault.core.domain.model.hasWellFormedUnicode
import com.passvault.core.domain.repository.FolderRepository
import com.passvault.core.domain.repository.LockReason
import com.passvault.core.domain.repository.TagRepository
import com.passvault.core.domain.repository.VaultRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock

/**
 * Fake vault repository for testing.
 * Simulates vault operations in memory.
 *
 * Repository overrides and lifecycle controls share one state machine and
 * therefore remain in a single test double.
 */
class FakeVaultRepository : VaultRepository {

    private var vaultExists = false
    private var currentVaultId: VaultId? = null
    private val sessionState = MutableStateFlow<VaultSessionState>(VaultSessionState.Uninitialized)
    private val vaultMetadata = mutableMapOf<VaultId, VaultMetadata>()
    private val operationMutex = Mutex()

    private var shouldFailNext = false
    private var failWith: Throwable? = null
    /** Optional test-only unlock latency. */
    var unlockDelayMillis: Long = 0
        set(value) {
            require(value >= 0)
            field = value
        }

    /**
     * Pre-configure a vault to exist.
     */
    fun setupExistingVault(
        vaultId: VaultId = VaultId("test-vault-123"),
        metadata: VaultMetadata = VaultMetadata(
            id = vaultId,
            formatVersion = 1,
            createdAt = Clock.System.now(),
            lastAccessedAt = null,
            entryCount = 0,
        ),
    ) {
        require(metadata.id == vaultId)
        vaultExists = true
        currentVaultId = vaultId
        vaultMetadata[vaultId] = metadata
    }

    /**
     * Configure the next operation to fail.
     */
    fun setShouldFail(error: Throwable? = RuntimeException("Fake vault error")) {
        shouldFailNext = error != null
        failWith = error
    }

    /**
     * Reset to initial state.
     */
    fun reset() {
        vaultExists = false
        currentVaultId = null
        sessionState.value = VaultSessionState.Uninitialized
        vaultMetadata.clear()
        shouldFailNext = false
        failWith = null
        unlockDelayMillis = 0
    }

    override suspend fun exists(): Result<Boolean> {
        return checkFailure { Result.success(vaultExists) }
    }

    override suspend fun create(masterPassword: SensitiveText): Result<VaultId> {
        return checkFailure {
            if (!MasterPasswordPolicy.accepts(masterPassword)) {
                return@checkFailure Result.failure(
                    IllegalArgumentException("Master password does not meet policy"),
                )
            }
            if (vaultExists) {
                return@checkFailure Result.failure(IllegalStateException("Vault already exists"))
            }
            val vaultId = VaultId("vault-${kotlin.uuid.Uuid.random()}")
            currentVaultId = vaultId
            vaultExists = true
            vaultMetadata[vaultId] = VaultMetadata(
                id = vaultId,
                formatVersion = 1,
                createdAt = Clock.System.now(),
                lastAccessedAt = null,
                entryCount = 0,
            )
            sessionState.value = VaultSessionState.Locked()
            Result.success(vaultId)
        }
    }

    override suspend fun unlock(masterPassword: SensitiveText): Result<SessionId> {
        return checkFailure {
            if (!MasterPasswordPolicy.acceptsExisting(masterPassword)) {
                return@checkFailure Result.failure(
                    IllegalArgumentException("Master password length is invalid"),
                )
            }
            if (!vaultExists) {
                return@checkFailure Result.failure(IllegalStateException("Vault does not exist"))
            }
            if (sessionState.value is VaultSessionState.Unlocked) {
                return@checkFailure Result.failure(IllegalStateException("Vault already unlocked"))
            }
            sessionState.value = VaultSessionState.Unlocking

            try {
                if (unlockDelayMillis > 0) {
                    kotlinx.coroutines.delay(unlockDelayMillis)
                }
            } catch (cancel: CancellationException) {
                sessionState.value = VaultSessionState.Locked()
                throw cancel
            }

            val sessionId = SessionId("session-${kotlin.uuid.Uuid.random()}")
            sessionState.value = VaultSessionState.Unlocked(sessionId)
            Result.success(sessionId)
        }
    }

    override suspend fun lock(reason: LockReason): Result<Unit> {
        val result = checkFailure { Result.success(Unit) }
        sessionState.value = VaultSessionState.Locking(reason)
        sessionState.value = VaultSessionState.Locked(reason)
        return result
    }

    override suspend fun changeMasterPassword(
        currentPassword: SensitiveText,
        newPassword: SensitiveText,
    ): Result<Unit> {
        return checkFailure {
            when {
                !MasterPasswordPolicy.accepts(newPassword) -> {
                    Result.failure(IllegalArgumentException("New master password does not meet policy"))
                }
                !MasterPasswordPolicy.acceptsExisting(currentPassword) -> {
                    Result.failure(IllegalArgumentException("Current master password length is invalid"))
                }
                sessionState.value !is VaultSessionState.Unlocked -> {
                    Result.failure(IllegalStateException("Vault must be unlocked"))
                }
                else -> Result.success(Unit)
            }
        }
    }

    override suspend fun getMetadata(): Result<VaultMetadata> {
        return checkFailure {
            currentVaultId?.let { vaultMetadata[it] }?.let { Result.success(it) }
                ?: Result.failure(IllegalStateException("No vault exists"))
        }
    }

    override fun getSessionState(): Flow<VaultSessionState> {
        return sessionState.asStateFlow()
    }

    /**
     * Direct session-state test hook. Production consumers continue to use
     * [getSessionState]; this property keeps fake setup and assertions terse.
     */
    var currentSessionState: VaultSessionState
        get() = sessionState.value
        set(value) {
            sessionState.value = value
        }

    private suspend fun <T> checkFailure(block: suspend () -> Result<T>): Result<T> {
        return operationMutex.withLock {
            if (shouldFailNext) {
                shouldFailNext = false
                val error = failWith ?: RuntimeException("Operation failed")
                if (error is CancellationException) throw error
                Result.failure(error)
            } else {
                block()
            }
        }
    }
}

/**
 * Fake folder repository for testing.
 */
class FakeFolderRepository : FolderRepository {

    private val folders = mutableMapOf<FolderId, Folder>()
    private val operationMutex = Mutex()
    private var shouldFailNext = false

    fun setupFolders(vararg foldersToAdd: Folder) {
        foldersToAdd.forEach { folders[it.id] = it }
    }

    fun setShouldFail(shouldFail: Boolean) {
        shouldFailNext = shouldFail
    }

    override suspend fun getAll(): Result<List<Folder>> {
        return checkFailure("Failed to get folders") {
            validateFolderHierarchy()?.let { return@checkFailure Result.failure(it) }
            Result.success(
                folders.values
                    .sortedWith(compareBy(Folder::sortOrder, Folder::createdAt)),
            )
        }
    }

    override suspend fun getById(id: FolderId): Result<Folder?> {
        return checkFailure("Failed to get folder") {
            validateRecordIdentifier(id.value, "Folder ID")?.let {
                return@checkFailure Result.failure(it)
            }
            val folder = folders[id] ?: return@checkFailure Result.success(null)
            validateStoredFolder(folder)?.let { return@checkFailure Result.failure(it) }
            validateParentChain(folder)?.let { return@checkFailure Result.failure(it) }
            Result.success(folder)
        }
    }

    override suspend fun save(folder: Folder): Result<FolderId> {
        return checkFailure("Failed to save folder") {
            val normalizedName = folder.name.trim()
            validateRecordIdentifier(folder.id.value, "Folder ID")?.let {
                return@checkFailure Result.failure(it)
            }
            folder.parentId?.let { parentId ->
                validateRecordIdentifier(parentId.value, "Parent folder ID")?.let {
                    return@checkFailure Result.failure(it)
                }
            }
            validateName(normalizedName, "Folder", MAX_FOLDER_NAME_LENGTH)?.let {
                return@checkFailure Result.failure(it)
            }
            validateMetadata(folder.icon, "Folder icon", MAX_FOLDER_ICON_LENGTH)?.let {
                return@checkFailure Result.failure(it)
            }
            if (folder.sortOrder < 0) {
                return@checkFailure Result.failure(
                    IllegalArgumentException("Folder sort order must not be negative"),
                )
            }
            validateParentChain(folder)?.let { return@checkFailure Result.failure(it) }
            if (folders.values.any {
                    it.id != folder.id && it.name.trim().lowercase() == normalizedName.lowercase()
                }
            ) {
                return@checkFailure Result.failure(
                    IllegalArgumentException("A folder with this name already exists"),
                )
            }
            folders[folder.id] = folder.copy(
                name = normalizedName,
                createdAt = folders[folder.id]?.createdAt ?: folder.createdAt,
            )
            Result.success(folder.id)
        }
    }

    override suspend fun delete(id: FolderId): Result<Unit> {
        return checkFailure("Failed to delete folder") {
            validateRecordIdentifier(id.value, "Folder ID")?.let {
                return@checkFailure Result.failure(it)
            }
            val removed = folders.remove(id)
            if (removed == null) {
                return@checkFailure Result.failure(IllegalStateException("Folder does not exist"))
            }
            folders.keys.toList().forEach { childId ->
                val child = folders.getValue(childId)
                if (child.parentId == id) {
                    folders[childId] = child.copy(parentId = removed.parentId)
                }
            }
            Result.success(Unit)
        }
    }

    override suspend fun reorder(id: FolderId, newOrder: Int): Result<Unit> {
        return checkFailure("Failed to reorder folder") {
            validateRecordIdentifier(id.value, "Folder ID")?.let {
                return@checkFailure Result.failure(it)
            }
            val folder = folders[id]
                ?: return@checkFailure Result.failure(IllegalStateException("Folder does not exist"))
            if (newOrder < 0) {
                return@checkFailure Result.failure(IllegalArgumentException("Invalid folder order"))
            }
            folders[id] = folder.copy(sortOrder = newOrder)
            Result.success(Unit)
        }
    }

    private fun validateFolderHierarchy(): IllegalArgumentException? {
        val storedFailure = folders.values
            .asSequence()
            .mapNotNull(::validateStoredFolder)
            .firstOrNull()
        return storedFailure ?: folders.values
            .asSequence()
            .mapNotNull(::validateParentChain)
            .firstOrNull()
    }

    private fun validateStoredFolder(folder: Folder): IllegalArgumentException? = listOfNotNull(
        validateRecordIdentifier(folder.id.value, "Folder ID"),
        folder.parentId?.let { validateRecordIdentifier(it.value, "Parent folder ID") },
        validateName(folder.name, "Stored folder", MAX_FOLDER_NAME_LENGTH),
        validateMetadata(folder.icon, "Stored folder icon", MAX_FOLDER_ICON_LENGTH),
        IllegalArgumentException("Stored folder sort order is invalid").takeIf { folder.sortOrder < 0 },
    ).firstOrNull()

    private fun validateParentChain(folder: Folder): IllegalArgumentException? {
        val seen = mutableSetOf(folder.id)
        var current = folder.parentId
        var failure: IllegalArgumentException? = null
        while (current != null && failure == null) {
            if (!seen.add(current)) {
                failure = IllegalArgumentException("Folder hierarchy contains a cycle")
            } else {
                val ancestor = folders[current]
                if (ancestor == null) {
                    failure = IllegalArgumentException("Folder hierarchy contains a missing parent")
                } else {
                    current = ancestor.parentId
                }
            }
        }
        return failure
    }

    private suspend fun <T> checkFailure(message: String, block: () -> Result<T>): Result<T> =
        operationMutex.withLock {
            if (shouldFailNext) {
                shouldFailNext = false
                Result.failure(RuntimeException(message))
            } else {
                block()
            }
        }

    private companion object {
        const val MAX_FOLDER_NAME_LENGTH = 256
        const val MAX_FOLDER_ICON_LENGTH = 64
    }
}

/**
 * Fake tag repository for testing.
 */
class FakeTagRepository : TagRepository {

    private val tags = mutableMapOf<TagId, Tag>()
    private val operationMutex = Mutex()
    private var shouldFailNext = false

    fun setupTags(vararg tagsToAdd: Tag) {
        tagsToAdd.forEach { tags[it.id] = it }
    }

    fun setShouldFail(shouldFail: Boolean) {
        shouldFailNext = shouldFail
    }

    override suspend fun getAll(): Result<List<Tag>> {
        return checkFailure("Failed to get tags") {
            tags.values.forEach { tag ->
                validateStoredTag(tag)?.let { return@checkFailure Result.failure(it) }
            }
            Result.success(tags.values.toList())
        }
    }

    override suspend fun getById(id: TagId): Result<Tag?> {
        return checkFailure("Failed to get tag") {
            validateRecordIdentifier(id.value, "Tag ID")?.let {
                return@checkFailure Result.failure(it)
            }
            val tag = tags[id] ?: return@checkFailure Result.success(null)
            validateStoredTag(tag)?.let { return@checkFailure Result.failure(it) }
            Result.success(tag)
        }
    }

    override suspend fun save(tag: Tag): Result<TagId> {
        return checkFailure("Failed to save tag") {
            val normalizedName = tag.name.trim()
            validateRecordIdentifier(tag.id.value, "Tag ID")?.let {
                return@checkFailure Result.failure(it)
            }
            validateName(normalizedName, "Tag", MAX_TAG_NAME_LENGTH)?.let {
                return@checkFailure Result.failure(it)
            }
            validateMetadata(tag.color, "Tag color", MAX_TAG_COLOR_LENGTH)?.let {
                return@checkFailure Result.failure(it)
            }
            if (tags.values.any {
                    it.id != tag.id && it.name.trim().lowercase() == normalizedName.lowercase()
                }
            ) {
                return@checkFailure Result.failure(
                    IllegalArgumentException("A tag with this name already exists"),
                )
            }
            tags[tag.id] = tag.copy(name = normalizedName)
            Result.success(tag.id)
        }
    }

    override suspend fun delete(id: TagId): Result<Unit> {
        return checkFailure("Failed to delete tag") {
            validateRecordIdentifier(id.value, "Tag ID")?.let {
                return@checkFailure Result.failure(it)
            }
            if (tags.remove(id) == null) {
                return@checkFailure Result.failure(IllegalStateException("Tag does not exist"))
            }
            Result.success(Unit)
        }
    }

    private fun validateStoredTag(tag: Tag): IllegalArgumentException? = listOfNotNull(
        validateRecordIdentifier(tag.id.value, "Tag ID"),
        validateName(tag.name, "Stored tag", MAX_TAG_NAME_LENGTH),
        validateMetadata(tag.color, "Stored tag color", MAX_TAG_COLOR_LENGTH),
    ).firstOrNull()

    private suspend fun <T> checkFailure(message: String, block: () -> Result<T>): Result<T> =
        operationMutex.withLock {
            if (shouldFailNext) {
                shouldFailNext = false
                Result.failure(RuntimeException(message))
            } else {
                block()
            }
        }

    private companion object {
        const val MAX_TAG_NAME_LENGTH = 256
        const val MAX_TAG_COLOR_LENGTH = 64
    }
}

private fun validateRecordIdentifier(value: String, fieldName: String): IllegalArgumentException? {
    val hasInvalidPathCharacter = value.any { it == '/' || it == '\\' }
    val isInvalid = value.isBlank() ||
        !value.hasBoundedSafeText(MAX_RECORD_IDENTIFIER_LENGTH) ||
        hasInvalidPathCharacter
    return if (isInvalid) {
        IllegalArgumentException("$fieldName is invalid")
    } else {
        null
    }
}

private fun validateName(value: String, fieldName: String, maxLength: Int): IllegalArgumentException? =
    if (value.isEmpty()) {
        IllegalArgumentException("$fieldName name is required")
    } else if (!value.hasBoundedSafeText(maxLength)) {
        IllegalArgumentException("$fieldName name is too long")
    } else {
        null
    }

private fun validateMetadata(value: String?, fieldName: String, maxLength: Int): IllegalArgumentException? =
    if (value != null && !value.hasBoundedSafeText(maxLength)) {
        IllegalArgumentException("$fieldName is invalid")
    } else {
        null
    }

private fun String.hasBoundedSafeText(maxLength: Int): Boolean =
    hasWellFormedUnicode() &&
        codePointLength() <= maxLength &&
        hasOnlySafeSingleLineCodePoints()

private const val MAX_RECORD_IDENTIFIER_LENGTH = 256
