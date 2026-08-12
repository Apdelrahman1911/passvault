package com.passvault.core.domain.repository

import com.passvault.core.domain.model.CredentialId
import com.passvault.core.domain.model.TotpConfiguration

/**
 * Narrow decrypted-data boundary for displaying current authenticator codes.
 *
 * Callers take ownership of each returned TOTP secret and must clear it when
 * the authenticator screen is hidden or the vault locks.
 */
interface CredentialTotpRepository {
    suspend fun getCredentialsForTotpDisplay(): Result<List<CredentialTotpInput>>
}

data class CredentialTotpInput(
    val id: CredentialId,
    val title: String,
    val displayUsername: String?,
    val configuration: TotpConfiguration,
) {
    fun clear() {
        configuration.clear()
    }
}
