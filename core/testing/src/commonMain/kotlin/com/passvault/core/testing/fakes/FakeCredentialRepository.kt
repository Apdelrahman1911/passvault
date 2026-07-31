package com.passvault.core.testing.fakes

import com.passvault.core.domain.model.*
import com.passvault.core.domain.repository.CredentialRepository
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Fake credential repository for testing.
 * Simulates credential CRUD operations in memory.
 */
class FakeCredentialRepository : CredentialRepository {
    
    private val credentials = mutableMapOf<CredentialId, Credential>()
    private val healthData = mutableMapOf<CredentialId, PasswordHealth>()
    
    private var shouldFailNext = false
    private var failWith: Throwable? = null
    private var operationDelayMs: Long = 0
    
    /**
     * Pre-populate with test credentials.
     */
    fun setupCredentials(vararg creds: Credential) {
        creds.forEach { credentials[it.id] = it.deepCopy() }
    }
    
    /**
     * Configure the next operation to fail.
     */
    fun setShouldFail(error: Throwable? = RuntimeException("Fake credential error")) {
        shouldFailNext = error != null
        failWith = error
    }
    
    /**
     * Set delay for operations (to test loading states).
     */
    fun setOperationDelay(delayMs: Long) {
        operationDelayMs = delayMs
    }
    
    /**
     * Reset to empty state.
     */
    fun reset() {
        credentials.clear()
        healthData.clear()
        shouldFailNext = false
        failWith = null
        operationDelayMs = 0
    }
    
    override suspend fun getAllSummaries(): Result<List<CredentialSummary.Decrypted>> {
        return checkFailure {
            delayIfNeeded()
            val summaries = credentials.values.map { cred ->
                CredentialSummary.Decrypted(
                    id = cred.id,
                    type = cred.type,
                    title = cred.title,
                    displayUsername = cred.username?.toStringUnsafe() ?: cred.email?.toStringUnsafe(),
                    isFavorite = cred.isFavorite,
                    folderId = cred.folderId,
                    tagIds = cred.tagIds,
                    passwordHealth = healthData[cred.id] ?: PasswordHealth.UNKNOWN,
                    lastUsedAt = cred.lastUsedAt,
                    createdAt = cred.createdAt,
                    updatedAt = cred.updatedAt,
                )
            }
            Result.success(summaries)
        }
    }
    
    override suspend fun getById(id: CredentialId): Result<Credential?> {
        return checkFailure {
            delayIfNeeded()
            Result.success(credentials[id]?.deepCopy())
        }
    }
    
    override suspend fun save(credential: Credential): Result<CredentialId> {
        return checkFailure {
            delayIfNeeded()
            val credentialToSave = if (credentials.containsKey(credential.id)) {
                credential.copy(updatedAt = Clock.System.now())
            } else {
                credential
            }
            credentials[credential.id] = credentialToSave.deepCopy()
            Result.success(credential.id)
        }
    }
    
    override suspend fun delete(id: CredentialId): Result<Unit> {
        return checkFailure {
            delayIfNeeded()
            credentials.remove(id)
            healthData.remove(id)
            Result.success(Unit)
        }
    }
    
    override suspend fun updateFavorite(id: CredentialId, isFavorite: Boolean): Result<Unit> {
        return checkFailure {
            credentials[id]?.let { cred ->
                credentials[id] = cred.copy(
                    isFavorite = isFavorite,
                    updatedAt = Clock.System.now()
                )
            }
            Result.success(Unit)
        }
    }
    
    override suspend fun moveToFolder(id: CredentialId, folderId: FolderId?): Result<Unit> {
        return checkFailure {
            credentials[id]?.let { cred ->
                credentials[id] = cred.copy(
                    folderId = folderId,
                    updatedAt = Clock.System.now()
                )
            }
            Result.success(Unit)
        }
    }
    
    override suspend fun addTag(id: CredentialId, tagId: TagId): Result<Unit> {
        return checkFailure {
            credentials[id]?.let { cred ->
                val newTags = cred.tagIds + tagId
                credentials[id] = cred.copy(
                    tagIds = newTags,
                    updatedAt = Clock.System.now()
                )
            }
            Result.success(Unit)
        }
    }
    
    override suspend fun removeTag(id: CredentialId, tagId: TagId): Result<Unit> {
        return checkFailure {
            credentials[id]?.let { cred ->
                val newTags = cred.tagIds - tagId
                credentials[id] = cred.copy(
                    tagIds = newTags,
                    updatedAt = Clock.System.now()
                )
            }
            Result.success(Unit)
        }
    }
    
    override suspend fun recordUsage(id: CredentialId, timestamp: Instant): Result<Unit> {
        return checkFailure {
            credentials[id]?.let { cred ->
                credentials[id] = cred.copy(lastUsedAt = timestamp)
            }
            Result.success(Unit)
        }
    }
    
    override suspend fun addPasswordHistory(
        id: CredentialId,
        password: SensitiveText,
    ): Result<Unit> {
        return checkFailure {
            credentials[id]?.let { cred ->
                val newEntry = PasswordHistoryEntry(
                    password = password,
                    changedAt = Clock.System.now(),
                )
                val newHistory = (cred.passwordHistory + newEntry).takeLast(10) // Keep last 10
                credentials[id] = cred.copy(passwordHistory = newHistory)
            }
            Result.success(Unit)
        }
    }
    
    override suspend fun getCredentialsForHealthAnalysis(): Result<List<Credential>> {
        return checkFailure {
            Result.success(credentials.values.toList())
        }
    }
    
    override suspend fun updateHealth(id: CredentialId, health: PasswordHealth): Result<Unit> {
        return checkFailure {
            healthData[id] = health
            Result.success(Unit)
        }
    }
    
    private suspend fun delayIfNeeded() {
        if (operationDelayMs > 0) {
            kotlinx.coroutines.delay(operationDelayMs)
        }
    }
    
    private suspend fun <T> checkFailure(block: suspend () -> Result<T>): Result<T> {
        return if (shouldFailNext) {
            shouldFailNext = false
            Result.failure(failWith ?: RuntimeException("Operation failed"))
        } else {
            block()
        }
    }
    
    /**
     * Get all credentials (for test verification).
     */
    fun getAllCredentials(): List<Credential> = credentials.values.map { it.deepCopy() }
    
    /**
     * Get credential count.
     */
    fun getCredentialCount(): Int = credentials.size
    
    /**
     * Check if credential exists.
     */
    fun hasCredential(id: CredentialId): Boolean = credentials.containsKey(id)

    private fun Credential.deepCopy(): Credential = copy(
        username = username?.let { SensitiveText.from(it.toStringUnsafe()) },
        email = email?.let { SensitiveText.from(it.toStringUnsafe()) },
        password = password?.let { SensitiveText.from(it.toStringUnsafe()) },
        notes = notes?.let { SensitiveText.from(it.toStringUnsafe()) },
        recoveryCodes = recoveryCodes.map { SensitiveText.from(it.toStringUnsafe()) },
        apiKeys = apiKeys.map { SensitiveText.from(it.toStringUnsafe()) },
        licenseKeys = licenseKeys.map { SensitiveText.from(it.toStringUnsafe()) },
        customFields = customFields.map { field ->
            field.copy(value = SensitiveText.from(field.value.toStringUnsafe()))
        },
        passwordHistory = passwordHistory.map { entry ->
            entry.copy(password = SensitiveText.from(entry.password.toStringUnsafe()))
        },
    )
}
