package com.passvault.shared.platform

/** Non-secret cache retained for compatibility; Keychain metadata remains authoritative. */
internal interface IosBiometricEnrollmentMarker {
    fun mark(vaultId: String)

    fun clear(vaultId: String)
}

/** Keychain operations that never request authentication or return secret data. */
internal interface IosBiometricKeychainLifecycle {
    fun containsWithoutAuthentication(vaultId: String): Result<Boolean>

    fun accountsWithoutAuthentication(): Result<Set<String>>

    fun delete(vaultId: String): Result<Unit>

    fun deleteAll(): Result<Unit>
}

/**
 * Orders the independent Keychain and preferences mutations so a failed secure deletion stays retryable.
 * PassVault owns one active vault, so items for other vault identifiers are stale and can be retired.
 */
internal class IosBiometricEnrollmentState(
    private val marker: IosBiometricEnrollmentMarker,
    private val keychain: IosBiometricKeychainLifecycle,
) {
    fun contains(vaultId: String): Result<Boolean> {
        reconcile(vaultId)
        return keychain.containsWithoutAuthentication(vaultId).map { exists ->
            if (exists) marker.mark(vaultId) else marker.clear(vaultId)
            exists
        }
    }

    fun reconcile(activeVaultId: String?): Result<Unit> = if (activeVaultId == null) {
        val knownAccounts = keychain.accountsWithoutAuthentication().getOrNull().orEmpty()
        keychain.deleteAll().onSuccess {
            knownAccounts.forEach(marker::clear)
        }
    } else {
        keychain.accountsWithoutAuthentication().fold(
            onSuccess = { accounts -> removeOrphans(activeVaultId, accounts) },
            onFailure = { Result.failure(it) },
        )
    }

    private fun removeOrphans(activeVaultId: String, accounts: Set<String>): Result<Unit> {
        val failures = accounts.asSequence()
            .filterNot { it == activeVaultId }
            .mapNotNull { orphanVaultId ->
                keychain.delete(orphanVaultId).fold(
                    onSuccess = {
                        marker.clear(orphanVaultId)
                        null
                    },
                    onFailure = { it },
                )
            }
            .toList()
        return failures.firstOrNull()?.let { Result.failure(it) } ?: Result.success(Unit)
    }

    fun replace(vaultId: String, add: () -> Result<Unit>): Result<Unit> {
        val deleteResult = keychain.delete(vaultId)
        if (deleteResult.isFailure) return deleteResult

        marker.clear(vaultId)
        return add().onSuccess { marker.mark(vaultId) }
    }

    fun delete(vaultId: String): Result<Unit> = keychain.delete(vaultId).onSuccess {
        marker.clear(vaultId)
    }
}
