package com.passvault.shared.navigation

import com.passvault.core.navigation.BackDisposition
import com.passvault.feature.credential.presentation.CredentialViewModel
import com.passvault.feature.backup.presentation.BackupViewModel
import com.passvault.feature.settings.presentation.SettingsViewModel
import com.passvault.feature.vault.presentation.VaultViewModel
import com.passvault.shared.navigation.adapters.backupBackDisposition
import com.passvault.shared.navigation.adapters.credentialBackDisposition
import com.passvault.shared.navigation.adapters.settingsBackDisposition
import com.passvault.shared.navigation.adapters.vaultBackDisposition
import kotlin.test.Test
import kotlin.test.assertEquals

class BackPolicyTest {
    @Test
    fun `dirty editor handles Back locally before any pop`() {
        assertEquals(
            BackDisposition.HandleInPlace,
            credentialBackDisposition(CredentialViewModel.CredentialState(isDirty = true)),
        )
    }

    @Test
    fun `active editor operation blocks every Back input`() {
        assertEquals(
            BackDisposition.Blocked,
            credentialBackDisposition(CredentialViewModel.CredentialState(isSaving = true)),
        )
    }

    @Test
    fun `clean idle editor permits Nav3 interactive pop`() {
        assertEquals(
            BackDisposition.PopNow,
            credentialBackDisposition(CredentialViewModel.CredentialState()),
        )
    }

    @Test
    fun `vault transient UI consumes Back in place`() {
        assertEquals(
            BackDisposition.HandleInPlace,
            vaultBackDisposition(
                VaultViewModel.VaultState(isSearchActive = true),
                BackDisposition.ExitApplication,
            ),
        )
    }

    @Test
    fun `settings dialog handles Back but password mutation blocks it`() {
        assertEquals(
            BackDisposition.HandleInPlace,
            settingsBackDisposition(
                SettingsViewModel.SettingsState(showChangePasswordDialog = true),
                BackDisposition.PopNow,
            ),
        )
        assertEquals(
            BackDisposition.Blocked,
            settingsBackDisposition(
                SettingsViewModel.SettingsState(isChangingPassword = true),
                BackDisposition.PopNow,
            ),
        )
    }

    @Test
    fun `backup confirmation handles Back and active restore blocks it`() {
        assertEquals(
            BackDisposition.HandleInPlace,
            backupBackDisposition(BackupViewModel.BackupState(showRestoreConfirmation = true)),
        )
        assertEquals(
            BackDisposition.Blocked,
            backupBackDisposition(BackupViewModel.BackupState(isImporting = true)),
        )
    }
}
