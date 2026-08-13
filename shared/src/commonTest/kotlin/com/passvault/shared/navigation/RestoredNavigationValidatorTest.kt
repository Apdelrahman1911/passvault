package com.passvault.shared.navigation

import com.passvault.core.navigation.BackupRoute
import com.passvault.core.navigation.ExternalNavigationIntent
import com.passvault.core.navigation.MainNavigationSnapshot
import com.passvault.core.navigation.SettingsRoute
import com.passvault.core.navigation.TopLevelDestination
import com.passvault.core.navigation.VaultRoute
import com.passvault.core.testing.TestData
import com.passvault.core.testing.fakes.FakeCredentialRepository
import com.passvault.core.testing.fakes.FakeFolderRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RestoredNavigationValidatorTest {
    @Test
    fun `valid protected route IDs reactivate after authentication`() = runTest {
        val credentialRepository = FakeCredentialRepository().apply {
            setupCredentials(TestData.credential(id = CREDENTIAL_ID))
        }
        val folderRepository = FakeFolderRepository().apply {
            setupFolders(TestData.folder(id = FOLDER_ID))
        }
        val validator = RestoredNavigationValidator(credentialRepository, folderRepository)
        val snapshot = MainNavigationSnapshot(
            selectedDestination = TopLevelDestination.SETTINGS,
            home = listOf(
                VaultRoute.Vault,
                VaultRoute.CredentialCreate(FOLDER_ID),
                VaultRoute.CredentialDetail(CREDENTIAL_ID),
            ),
            settings = listOf(SettingsRoute.Settings, SettingsRoute.Data, BackupRoute.Export),
        )

        assertEquals(snapshot, validator.validate(snapshot))
        assertTrue(validator.validateExternal(ExternalNavigationIntent.Credential(CREDENTIAL_ID)))
    }

    @Test
    fun `stale destination truncates that stack and every dependent descendant`() = runTest {
        val validator = RestoredNavigationValidator(FakeCredentialRepository(), FakeFolderRepository())
        val snapshot = MainNavigationSnapshot(
            home = listOf(
                VaultRoute.Vault,
                VaultRoute.CredentialDetail(CREDENTIAL_ID),
                VaultRoute.CredentialEdit(CREDENTIAL_ID),
            ),
        )

        assertEquals(listOf(VaultRoute.Vault), validator.validate(snapshot).home)
        assertFalse(validator.validateExternal(ExternalNavigationIntent.Credential(CREDENTIAL_ID)))
    }

    @Test
    fun `malformed IDs and cross-tab routes fail closed without replacing other stacks`() = runTest {
        val validator = RestoredNavigationValidator(FakeCredentialRepository(), FakeFolderRepository())
        val snapshot = MainNavigationSnapshot(
            home = listOf(VaultRoute.Vault, VaultRoute.CredentialDetail("not-an-id")),
            settings = listOf(SettingsRoute.Settings, VaultRoute.CredentialDetail(CREDENTIAL_ID)),
        )
        val validated = validator.validate(snapshot)

        assertEquals(listOf(VaultRoute.Vault), validated.home)
        assertEquals(listOf(SettingsRoute.Settings), validated.settings)
    }

    @Test
    fun `repository validation failure drops protected route`() = runTest {
        val credentials = FakeCredentialRepository().apply { setShouldFail() }
        val validator = RestoredNavigationValidator(credentials, FakeFolderRepository())

        assertEquals(
            listOf(VaultRoute.Vault),
            validator.validate(
                MainNavigationSnapshot(
                    home = listOf(VaultRoute.Vault, VaultRoute.CredentialDetail(CREDENTIAL_ID)),
                ),
            ).home,
        )
    }

    @Test
    fun `oversized restored stack is rejected as a unit`() = runTest {
        val validator = RestoredNavigationValidator(FakeCredentialRepository(), FakeFolderRepository())
        val oversized = buildList {
            add(SettingsRoute.Settings)
            repeat(32) { add(SettingsRoute.Data) }
        }

        val validated = validator.validate(MainNavigationSnapshot(settings = oversized))

        assertEquals(listOf(SettingsRoute.Settings), validated.settings)
    }

    private companion object {
        const val CREDENTIAL_ID = "00000000-0000-0000-0000-000000000001"
        const val FOLDER_ID = "00000000-0000-0000-0000-000000000002"
    }
}
