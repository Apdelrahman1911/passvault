package com.passvault.desktop.security

import com.github.javakeyring.Keyring
import com.passvault.core.security.KeyringService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * Desktop OS-keyring adapter.
 *
 * There is intentionally no file/XOR fallback.  A password-manager key must
 * not be silently downgraded to storage that is not protected by the operating
 * system.  Callers receive a clear failure and can continue with the master
 * password flow instead.
 */
class DesktopKeyringService : KeyringService {

    private val passwordStore: Keyring by lazy { Keyring.create() }
    private val serviceCache = ConcurrentHashMap<String, MutableSet<String>>()

    override suspend fun storeSecret(
        service: String,
        account: String,
        secret: ByteArray,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val encoded = java.util.Base64.getEncoder().encodeToString(secret)
            passwordStore.setPassword(service, account, encoded)
            serviceCache.getOrPut(service) { ConcurrentHashMap.newKeySet() }.add(account)
            Result.success(Unit)
        } catch (_: Exception) {
            Result.failure(keyringUnavailable())
        }
    }

    override suspend fun retrieveSecret(
        service: String,
        account: String,
    ): Result<ByteArray?> = withContext(Dispatchers.IO) {
        try {
            val encoded = passwordStore.getPassword(service, account)
            Result.success(encoded?.let { java.util.Base64.getDecoder().decode(it) })
        } catch (_: Exception) {
            Result.failure(keyringUnavailable())
        }
    }

    override suspend fun deleteSecret(service: String, account: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                passwordStore.deletePassword(service, account)
                serviceCache[service]?.remove(account)
                Result.success(Unit)
            } catch (_: Exception) {
                Result.failure(keyringUnavailable())
            }
        }

    override suspend fun hasSecret(service: String, account: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(passwordStore.getPassword(service, account) != null)
            } catch (_: Exception) {
                Result.failure(keyringUnavailable())
            }
        }

    override suspend fun listAccounts(service: String): Result<List<String>> =
        Result.success(serviceCache[service]?.toList().orEmpty())

    suspend fun storePassword(
        service: String,
        account: String,
        password: String,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            passwordStore.setPassword(service, account, password)
            serviceCache.getOrPut(service) { ConcurrentHashMap.newKeySet() }.add(account)
            Result.success(Unit)
        } catch (_: Exception) {
            Result.failure(keyringUnavailable())
        }
    }

    suspend fun getPassword(service: String, account: String): Result<String?> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(passwordStore.getPassword(service, account))
            } catch (_: Exception) {
                Result.failure(keyringUnavailable())
            }
        }

    suspend fun storeSecretsBatch(
        entries: List<Triple<String, String, ByteArray>>,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val stored = mutableListOf<Pair<String, String>>()
        try {
            entries.forEach { (service, account, secret) ->
                storeSecret(service, account, secret).getOrThrow()
                stored += service to account
            }
            Result.success(Unit)
        } catch (error: Exception) {
            stored.forEach { (service, account) ->
                runCatching { passwordStore.deletePassword(service, account) }
            }
            Result.failure(error)
        }
    }

    suspend fun deleteService(service: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            val accounts = serviceCache[service]?.toList().orEmpty()
            try {
                accounts.forEach { passwordStore.deletePassword(service, it) }
                serviceCache.remove(service)
                Result.success(Unit)
            } catch (_: Exception) {
                Result.failure(keyringUnavailable())
            }
        }

    /**
     * Retained for API compatibility; there is no insecure fallback to migrate.
     */
    suspend fun migrateFromFallback(): Result<Int> = Result.success(0)

    fun isKeyringAvailable(): Boolean =
        runCatching { Keyring.create().close(); true }.getOrDefault(false)

    fun getKeyringName(): String = if (isKeyringAvailable()) "OS keyring" else "Unavailable"

    fun isUsingFallback(): Boolean = false

    fun getStatistics(): KeyringStatistics = KeyringStatistics(
        totalServices = serviceCache.size,
        totalAccounts = serviceCache.values.sumOf { it.size },
        isUsingFallback = false,
        backendName = getKeyringName(),
    )

    fun clearCache() = serviceCache.clear()

    private fun keyringUnavailable(): IllegalStateException =
        IllegalStateException("Operating-system keyring is unavailable")

    data class KeyringStatistics(
        val totalServices: Int,
        val totalAccounts: Int,
        val isUsingFallback: Boolean,
        val backendName: String,
    )

    companion object {
        const val SERVICE_VAULT_KEY = "com.passvault.vault"
        const val ACCOUNT_MASTER_KEY = "master"
    }
}
