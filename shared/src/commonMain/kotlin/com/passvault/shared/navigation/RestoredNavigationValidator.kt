package com.passvault.shared.navigation

import com.passvault.core.domain.model.CredentialId
import com.passvault.core.domain.model.FolderId
import com.passvault.core.domain.repository.CredentialRepository
import com.passvault.core.domain.repository.FolderRepository
import com.passvault.core.navigation.ExternalNavigationIntent
import com.passvault.core.navigation.MainNavigationSnapshot
import com.passvault.core.navigation.PassVaultRoute
import com.passvault.core.navigation.TopLevelDestination
import com.passvault.core.navigation.VaultRoute
import com.passvault.core.navigation.isAllowedIn
import com.passvault.core.navigation.rootRoute
import kotlinx.coroutines.CancellationException

/** Validates quarantined route-only state after, never before, authentication. */
internal class RestoredNavigationValidator(
    private val credentialRepository: CredentialRepository,
    private val folderRepository: FolderRepository,
) {
    suspend fun validate(snapshot: MainNavigationSnapshot): MainNavigationSnapshot {
        val credentialValidity = mutableMapOf<String, Boolean>()
        val folderValidity = mutableMapOf<String, Boolean>()
        fun root(destination: TopLevelDestination) = listOf(destination.rootRoute())

        suspend fun validateStack(destination: TopLevelDestination): List<PassVaultRoute> {
            val source = snapshot.stack(destination)
            if (source.size > MAX_RESTORED_STACK_DEPTH || source.firstOrNull() != destination.rootRoute()) {
                return root(destination)
            }
            val accepted = mutableListOf<PassVaultRoute>(destination.rootRoute())
            for (route in source.drop(1)) {
                if (!route.isAllowedIn(destination) || !route.isStillValid(credentialValidity, folderValidity)) break
                accepted += route
            }
            return accepted
        }

        return MainNavigationSnapshot(
            selectedDestination = snapshot.selectedDestination,
            home = validateStack(TopLevelDestination.HOME),
            generator = validateStack(TopLevelDestination.GENERATOR),
            twoFactorCodes = validateStack(TopLevelDestination.TWO_FACTOR_CODES),
            settings = validateStack(TopLevelDestination.SETTINGS),
        )
    }

    suspend fun validateExternal(intent: ExternalNavigationIntent): Boolean = when (intent) {
        is ExternalNavigationIntent.Credential -> credentialExists(intent.credentialId)
        else -> true
    }

    private suspend fun PassVaultRoute.isStillValid(
        credentialValidity: MutableMap<String, Boolean>,
        folderValidity: MutableMap<String, Boolean>,
    ): Boolean = when (this) {
        is VaultRoute.CredentialDetail -> credentialValidity.getOrPutSuspending(credentialId) {
            credentialExists(credentialId)
        }
        is VaultRoute.CredentialEdit -> credentialValidity.getOrPutSuspending(credentialId) {
            credentialExists(credentialId)
        }
        is VaultRoute.CredentialCreate -> {
            val restoredFolderId = folderId
            restoredFolderId == null || folderValidity.getOrPutSuspending(restoredFolderId) {
                folderExists(restoredFolderId)
            }
        }
        else -> true
    }

    private suspend fun credentialExists(id: String): Boolean =
        if (!id.isCanonicalUuid()) {
            false
        } else {
            try {
                credentialRepository.getById(CredentialId(id)).getOrNull() != null
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                false
            }
        }

    private suspend fun folderExists(id: String): Boolean =
        if (!id.isCanonicalUuid()) {
            false
        } else {
            try {
                folderRepository.getById(FolderId(id)).getOrNull() != null
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                false
            }
        }

    private companion object {
        const val MAX_RESTORED_STACK_DEPTH = 32
    }
}

private fun String.isCanonicalUuid(): Boolean {
    if (length != 36) return false
    return indices.all { index ->
        when (index) {
            8, 13, 18, 23 -> this[index] == '-'
            else -> this[index] in '0'..'9' || this[index] in 'a'..'f' || this[index] in 'A'..'F'
        }
    }
}

private suspend fun <K, V> MutableMap<K, V>.getOrPutSuspending(
    key: K,
    defaultValue: suspend () -> V,
): V {
    this[key]?.let { return it }
    return defaultValue().also { value -> put(key, value) }
}
