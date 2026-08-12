package com.passvault.core.testing.fakes

import com.passvault.core.domain.model.Credential
import com.passvault.core.domain.model.CredentialId
import com.passvault.core.domain.model.CredentialSummary
import com.passvault.core.domain.model.CredentialType
import com.passvault.core.domain.model.FolderId
import com.passvault.core.domain.model.PasswordHealth
import com.passvault.core.domain.model.PasswordHistoryEntry
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.model.TagId
import com.passvault.core.domain.repository.CredentialHealthInput
import com.passvault.core.domain.repository.CredentialRepository
import com.passvault.core.domain.repository.CredentialTotpInput
import com.passvault.core.domain.repository.CredentialTotpRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Fake credential repository for testing.
 * Simulates credential CRUD operations in memory.
 *
 * Repository overrides and test controls stay together so cleanup of copied
 * sensitive values can be verified through one implementation.
 */
@Suppress("TooManyFunctions")
class FakeCredentialRepository : CredentialRepository, CredentialTotpRepository {

    private val credentials = mutableMapOf<CredentialId, Credential>()
    private val passwordChangedAtByCredential = mutableMapOf<CredentialId, Instant>()
    private var lastHealthInputs = emptyList<CredentialHealthInput>()
    private val operationMutex = Mutex()

    private var shouldFailNext = false
    private var failWith: Throwable? = null
    private var operationDelayMs: Long = 0

    /**
     * Pre-populate with test credentials.
     */
    fun setupCredentials(vararg creds: Credential) {
        clearLastHealthInputs()
        creds.forEach { credential ->
            credentials.put(credential.id, credential.deepCopy())?.clearSensitiveValues()
            val inferredPasswordChangedAt = credential.password
                ?.takeIf(SensitiveText::isNotEmpty)
                ?.let {
                    credential.passwordHistory.maxByOrNull(PasswordHistoryEntry::changedAt)?.changedAt
                        ?: credential.createdAt
                }
            if (inferredPasswordChangedAt == null) {
                passwordChangedAtByCredential.remove(credential.id)
            } else {
                passwordChangedAtByCredential[credential.id] = inferredPasswordChangedAt
            }
        }
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
        require(delayMs >= 0)
        operationDelayMs = delayMs
    }

    /**
     * Reset to empty state.
     */
    fun reset() {
        credentials.values.forEach(Credential::clearSensitiveValues)
        credentials.clear()
        passwordChangedAtByCredential.clear()
        clearLastHealthInputs()
        shouldFailNext = false
        failWith = null
        operationDelayMs = 0
    }

    override suspend fun getAllSummaries(): Result<List<CredentialSummary.Decrypted>> {
        return checkFailure {
            delayIfNeeded()
            val summaries = credentials.values.sortedByDescending(Credential::updatedAt).map { cred ->
                CredentialSummary.Decrypted(
                    id = cred.id,
                    type = cred.type,
                    title = cred.title,
                    displayUsername = cred.username?.toStringUnsafe() ?: cred.email?.toStringUnsafe(),
                    isFavorite = cred.isFavorite,
                    folderId = cred.folderId,
                    tagIds = cred.tagIds.toSet(),
                    passwordHealth = cred.passwordHealth,
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
            val existing = credentials[credential.id]
            val now = Clock.System.now()
            val passwordChangedAt = resolvePasswordChangedAt(credential, existing, now)
            val credentialToSave = credential.storedCopy(existing, now)
            credentials.put(credential.id, credentialToSave)?.clearSensitiveValues()
            if (passwordChangedAt == null) {
                passwordChangedAtByCredential.remove(credential.id)
            } else {
                passwordChangedAtByCredential[credential.id] = passwordChangedAt
            }
            Result.success(credential.id)
        }
    }

    override suspend fun delete(id: CredentialId): Result<Unit> {
        return checkFailure {
            delayIfNeeded()
            credentials.remove(id)?.clearSensitiveValues()
            passwordChangedAtByCredential.remove(id)
            Result.success(Unit)
        }
    }

    override suspend fun updateFavorite(id: CredentialId, isFavorite: Boolean): Result<Unit> {
        return checkFailure {
            updateStoredCredential(id) { credential ->
                credential.copy(isFavorite = isFavorite)
            }
        }
    }

    override suspend fun moveToFolder(id: CredentialId, folderId: FolderId?): Result<Unit> {
        return checkFailure {
            updateStoredCredential(id) { credential ->
                credential.copy(folderId = folderId)
            }
        }
    }

    override suspend fun addTag(id: CredentialId, tagId: TagId): Result<Unit> {
        return checkFailure {
            updateStoredCredential(id) { credential ->
                require(tagId in credential.tagIds || credential.tagIds.size < MAX_TAG_COUNT) {
                    "Credential tag limit reached"
                }
                credential.copy(tagIds = credential.tagIds + tagId)
            }
        }
    }

    override suspend fun removeTag(id: CredentialId, tagId: TagId): Result<Unit> {
        return checkFailure {
            updateStoredCredential(id) { credential ->
                credential.copy(tagIds = credential.tagIds - tagId)
            }
        }
    }

    override suspend fun recordUsage(id: CredentialId, timestamp: Instant): Result<Unit> {
        return checkFailure {
            updateStoredCredential(id) { credential -> credential.copy(lastUsedAt = timestamp) }
        }
    }

    override suspend fun getCredentialsForHealthAnalysis(): Result<List<CredentialHealthInput>> {
        return checkFailure {
            delayIfNeeded()
            clearLastHealthInputs()
            val inputs = credentials.values
                .filter { it.type == CredentialType.Login }
                .sortedBy(Credential::updatedAt)
                .map { credential ->
                    CredentialHealthInput(
                        id = credential.id,
                        type = credential.type,
                        title = credential.title,
                        username = credential.username?.let { SensitiveText.from(it.toStringUnsafe()) },
                        email = credential.email?.let { SensitiveText.from(it.toStringUnsafe()) },
                        password = credential.password?.let { SensitiveText.from(it.toStringUnsafe()) },
                        isFavorite = credential.isFavorite,
                        folderId = credential.folderId,
                        tagIds = credential.tagIds.toSet(),
                        createdAt = credential.createdAt,
                        updatedAt = credential.updatedAt,
                        lastUsedAt = credential.lastUsedAt,
                        passwordHealth = credential.passwordHealth,
                        passwordChangedAt = passwordChangedAtByCredential[credential.id],
                    )
                }
            lastHealthInputs = inputs
            Result.success(inputs)
        }
    }

    override suspend fun getCredentialsForTotpDisplay(): Result<List<CredentialTotpInput>> {
        return checkFailure {
            delayIfNeeded()
            val inputs = credentials.values
                .mapNotNull { credential ->
                    credential.totp?.let { configuration ->
                        CredentialTotpInput(
                            id = credential.id,
                            title = credential.title,
                            displayUsername = credential.username?.toStringUnsafe()
                                ?: credential.email?.toStringUnsafe(),
                            configuration = configuration.deepCopy(),
                        )
                    }
                }
                .sortedBy { it.title.lowercase() }
            Result.success(inputs)
        }
    }

    override suspend fun updateHealth(id: CredentialId, health: PasswordHealth): Result<Unit> {
        return checkFailure {
            val credential = credentials[id]
                ?: return@checkFailure Result.failure(IllegalStateException("Credential not found"))
            credentials[id] = credential.copy(passwordHealth = health)
            Result.success(Unit)
        }
    }

    private suspend fun delayIfNeeded() {
        if (operationDelayMs > 0) {
            kotlinx.coroutines.delay(operationDelayMs)
        }
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

    private inline fun updateStoredCredential(
        id: CredentialId,
        transform: (Credential) -> Credential,
    ): Result<Unit> {
        val credential = credentials[id]
            ?: return Result.failure(IllegalStateException("Credential not found"))
        return try {
            credentials[id] = transform(credential)
            Result.success(Unit)
        } catch (error: IllegalArgumentException) {
            Result.failure(error)
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

    fun getLastHealthInputsForTest(): List<CredentialHealthInput> = lastHealthInputs

    private fun clearLastHealthInputs() {
        lastHealthInputs.forEach { input ->
            input.username?.clear()
            input.email?.clear()
            input.password?.clear()
        }
        lastHealthInputs = emptyList()
    }

    private fun Credential.deepCopy(): Credential = copy(
        username = username?.let { SensitiveText.from(it.toStringUnsafe()) },
        email = email?.let { SensitiveText.from(it.toStringUnsafe()) },
        password = password?.let { SensitiveText.from(it.toStringUnsafe()) },
        urls = urls.toList(),
        notes = notes?.let { SensitiveText.from(it.toStringUnsafe()) },
        recoveryCodes = recoveryCodes.map { SensitiveText.from(it.toStringUnsafe()) },
        apiKeys = apiKeys.map { SensitiveText.from(it.toStringUnsafe()) },
        licenseKeys = licenseKeys.map { SensitiveText.from(it.toStringUnsafe()) },
        customFields = customFields.map { field ->
            field.copy(value = SensitiveText.from(field.value.toStringUnsafe()))
        },
        tagIds = tagIds.toSet(),
        attachments = attachments.toList(),
        passwordHistory = passwordHistory.map { entry ->
            entry.copy(password = SensitiveText.from(entry.password.toStringUnsafe()))
        },
        totp = totp?.deepCopy(),
    )

    private fun Credential.storedCopy(existing: Credential?, now: Instant): Credential {
        val stored = deepCopy()
        if (existing == null) {
            stored.passwordHistory.forEach { it.password.clear() }
            return stored.copy(
                attachments = emptyList(),
                passwordHistory = emptyList(),
                updatedAt = now,
            )
        }

        stored.passwordHistory.forEach { it.password.clear() }
        val passwordChanged = existing.password != password
        val history = buildList {
            if (passwordChanged) {
                existing.password?.let { previousPassword ->
                    add(
                        PasswordHistoryEntry(
                            password = SensitiveText.from(previousPassword.toStringUnsafe()),
                            changedAt = now,
                        ),
                    )
                }
            }
            existing.passwordHistory.take(MAX_PASSWORD_HISTORY - size).forEach { entry ->
                add(entry.copy(password = SensitiveText.from(entry.password.toStringUnsafe())))
            }
        }
        return stored.copy(
            attachments = existing.attachments.toList(),
            passwordHistory = history,
            createdAt = existing.createdAt,
            updatedAt = now,
            passwordHealth = if (passwordChanged) PasswordHealth.UNKNOWN else passwordHealth,
        )
    }

    private fun resolvePasswordChangedAt(
        credential: Credential,
        existing: Credential?,
        now: Instant,
    ): Instant? = when {
        credential.password?.isNotEmpty() != true -> null
        existing == null || existing.password != credential.password -> now
        else -> passwordChangedAtByCredential[credential.id]
            ?: existing.passwordHistory.maxByOrNull(PasswordHistoryEntry::changedAt)?.changedAt
            ?: existing.createdAt
    }

    private companion object {
        const val MAX_PASSWORD_HISTORY = 10
        const val MAX_TAG_COUNT = 100
    }
}
