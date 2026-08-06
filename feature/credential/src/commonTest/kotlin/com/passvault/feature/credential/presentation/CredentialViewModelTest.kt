package com.passvault.feature.credential.presentation

import app.cash.turbine.test
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.text.UiText
import com.passvault.core.domain.model.CustomField
import com.passvault.core.domain.model.CustomFieldId
import com.passvault.core.domain.model.CredentialType
import com.passvault.core.domain.model.FolderId
import com.passvault.core.domain.model.PasswordHealth
import com.passvault.core.domain.model.PasswordHistoryEntry
import com.passvault.core.domain.model.PasswordScore
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.model.TotpAlgorithm
import com.passvault.core.testing.TestData
import com.passvault.core.testing.fakes.FakeCredentialRepository
import com.passvault.core.testing.fakes.FakeFolderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class CredentialViewModelTest {

    private lateinit var dispatcher: TestDispatcher
    private lateinit var repository: FakeCredentialRepository
    private lateinit var folderRepository: FakeFolderRepository

    @BeforeTest
    fun setUp() {
        dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)
        repository = FakeCredentialRepository()
        folderRepository = FakeFolderRepository()
    }

    @AfterTest
    fun tearDown() {
        repository.reset()
        dispatcher.scheduler.runCurrent()
        Dispatchers.resetMain()
    }

    @Test
    fun `loading a credential preserves every sensitive field`() = runTest(dispatcher) {
        val source = TestData.credential(
            id = "full",
            title = "Full record",
            username = "user",
            email = "user@example.com",
            password = "secret",
            folderId = "work",
            tagIds = setOf("important"),
        ).copy(
            notes = SensitiveText.from("private note"),
            recoveryCodes = listOf(SensitiveText.from("recovery-1")),
            apiKeys = listOf(SensitiveText.from("api-key")),
            licenseKeys = listOf(SensitiveText.from("license-key")),
            customFields = listOf(
                CustomField(
                    id = CustomFieldId("field"),
                    name = "Account number",
                    value = SensitiveText.from("1234"),
                    isSecret = true,
                ),
            ),
            passwordHistory = listOf(
                PasswordHistoryEntry(
                    password = SensitiveText.from("old-secret"),
                    changedAt = TestData.now,
                ),
            ),
            isFavorite = true,
            passwordHealth = PasswordHealth(
                score = PasswordScore.WEAK,
                isDuplicate = true,
                isWeak = true,
                isOld = false,
                ageDays = 12,
            ),
        )
        repository.setupCredentials(source)

        val viewModel = createViewModel()
        viewModel.loadCredential(source.id)
        runCurrent()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("Full record", state.title)
        assertEquals("user", state.username)
        assertEquals("user@example.com", state.email)
        assertEquals("secret", state.password)
        assertEquals("private note", state.notes)
        assertEquals("recovery-1", state.recoveryCodes.single().toStringUnsafe())
        assertEquals("api-key", state.apiKeys.single().toStringUnsafe())
        assertEquals("license-key", state.licenseKeys.single().toStringUnsafe())
        assertEquals("1234", state.customFields.single().value.toStringUnsafe())
        assertEquals("old-secret", state.passwordHistory.single().password.toStringUnsafe())
        assertTrue(state.isFavorite)
        assertEquals(source.passwordHealth, state.passwordHealth)

        // The ViewModel must clear only its own copies, not mutate a fake or
        // repository-owned record returned by the data layer.
        val stored = repository.getAllCredentials().single()
        assertEquals("secret", stored.password?.toStringUnsafe())
        assertEquals("old-secret", stored.passwordHistory.single().password.toStringUnsafe())
    }

    @Test
    fun `new credential loads folders and defaults to no folder`() = runTest(dispatcher) {
        val workFolder = TestData.folder(id = "work", name = "Work")
        folderRepository.setupFolders(workFolder)
        val viewModel = createViewModel()

        viewModel.createNewCredential(CredentialType.Login)
        runCurrent()

        assertEquals(listOf(workFolder), viewModel.state.value.folders)
        assertEquals(null, viewModel.state.value.folderId)

        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnTitleChanged("Unfiled"))
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnSaveClick)
        runCurrent()

        assertEquals(null, repository.getAllCredentials().single().folderId)
    }

    @Test
    fun `new credential can be saved in a selected folder`() = runTest(dispatcher) {
        val workFolder = TestData.folder(id = "work", name = "Work")
        folderRepository.setupFolders(workFolder)
        val viewModel = createViewModel()

        viewModel.createNewCredential(CredentialType.Login)
        runCurrent()
        viewModel.onEvent(
            CredentialViewModel.CredentialEvent.OnFolderChanged(workFolder.id.value),
        )
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnTitleChanged("Work account"))
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnSaveClick)
        runCurrent()

        assertEquals(workFolder.id, repository.getAllCredentials().single().folderId)
    }

    @Test
    fun `editing a credential moves it to another folder`() = runTest(dispatcher) {
        val oldFolder = TestData.folder(id = "old", name = "Old")
        val newFolder = TestData.folder(id = "new", name = "New", sortOrder = 1)
        val credential = TestData.credential(id = "account", folderId = oldFolder.id.value)
        folderRepository.setupFolders(oldFolder, newFolder)
        repository.setupCredentials(credential)
        val viewModel = createViewModel()

        viewModel.loadCredential(credential.id)
        runCurrent()
        viewModel.onEvent(
            CredentialViewModel.CredentialEvent.OnFolderChanged(newFolder.id.value),
        )
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnSaveClick)
        runCurrent()

        assertEquals(FolderId("new"), repository.getAllCredentials().single().folderId)
    }

    @Test
    fun `invalid title and url are rejected before save`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        viewModel.createNewCredential(com.passvault.core.domain.model.CredentialType.Login)
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnTitleChanged(""))
        viewModel.onEvent(
            CredentialViewModel.CredentialEvent.OnPrimaryUrlChanged("javascript:alert(1)"),
        )
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnSaveClick)
        runCurrent()

        assertEquals(
            Res.string.validation_credential_title_required,
            (viewModel.state.value.titleError as UiText.Resource).resource,
        )
        assertEquals(
            Res.string.validation_credential_url_invalid,
            (viewModel.state.value.urlErrors[0] as UiText.Resource).resource,
        )
        assertFalse(viewModel.state.value.isSaving)
        assertEquals(0, repository.getCredentialCount())
    }

    @Test
    fun `repeated save submits persist one credential and emit one completion`() = runTest(dispatcher) {
        repository.setOperationDelay(100)
        val viewModel = createViewModel()
        viewModel.createNewCredential(com.passvault.core.domain.model.CredentialType.Login)
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnTitleChanged("New"))
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnPasswordChanged("StrongPassword123!"))

        viewModel.effect.test {
            repeat(3) {
                viewModel.onEvent(CredentialViewModel.CredentialEvent.OnSaveClick)
            }
            runCurrent()
            assertTrue(viewModel.state.value.isSaving)
            advanceTimeBy(100)
            runCurrent()

            assertIs<CredentialViewModel.CredentialEffect.SaveCompleted>(awaitItem())
            expectNoEvents()
            assertEquals(1, repository.getCredentialCount())
            assertFalse(viewModel.state.value.isSaving)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clear for lock cancels pending save and clears all sensitive state`() = runTest(dispatcher) {
        repository.setOperationDelay(1_000)
        val viewModel = createViewModel()
        viewModel.createNewCredential(com.passvault.core.domain.model.CredentialType.Login)
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnTitleChanged("Pending"))
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnPasswordChanged("sensitive"))
        viewModel.onEvent(
            CredentialViewModel.CredentialEvent.OnTotpSetupInputChanged("JBSWY3DPEHPK3PXP"),
        )
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnTotpAddClick)
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnSaveClick)
        runCurrent()

        viewModel.clearForLock()
        advanceTimeBy(1_000)
        runCurrent()

        assertEquals("", viewModel.state.value.password)
        assertEquals(null, viewModel.state.value.totpConfiguration)
        assertEquals("", viewModel.state.value.currentTotpCode)
        assertEquals(0, repository.getCredentialCount())
        assertFalse(viewModel.state.value.isSaving)
    }

    @Test
    fun `copy and launch events are explicit and normalize safe urls`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        viewModel.createNewCredential(com.passvault.core.domain.model.CredentialType.Login)
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnUsernameChanged("ada"))
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnLaunchUrlClick("example.com"))
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnCopyUsernameClick)

        viewModel.effect.test {
            // Events are buffered, so subscribe after dispatch and drain in
            // order to keep this test independent of Compose collection.
            assertEquals(
                CredentialViewModel.CredentialEffect.LaunchUrl("https://example.com"),
                awaitItem(),
            )
            assertEquals(
                CredentialViewModel.CredentialEffect.CopyToClipboard("ada"),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `TOTP setup generates a live code copies it and persists with the login`() =
        runTest(dispatcher) {
            val clock = FixedClock(Instant.fromEpochSeconds(59))
            val viewModel = createViewModel(clock)
            viewModel.createNewCredential(CredentialType.Login)
            viewModel.onEvent(CredentialViewModel.CredentialEvent.OnTitleChanged("Example"))
            viewModel.onEvent(
                CredentialViewModel.CredentialEvent.OnTotpSetupInputChanged(
                    "otpauth://totp/Example:alice?" +
                        "secret=GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ&digits=8",
                ),
            )
            viewModel.onEvent(CredentialViewModel.CredentialEvent.OnTotpAddClick)
            runCurrent()

            assertEquals("94287082", viewModel.state.value.currentTotpCode)
            assertEquals("Example", viewModel.state.value.totpConfiguration?.issuer)
            assertEquals("alice", viewModel.state.value.totpConfiguration?.accountName)

            viewModel.effect.test {
                viewModel.onEvent(CredentialViewModel.CredentialEvent.OnCopyTotpClick)
                assertEquals(
                    CredentialViewModel.CredentialEffect.CopyToClipboard("94287082"),
                    awaitItem(),
                )
                cancelAndIgnoreRemainingEvents()
            }

            viewModel.onEvent(CredentialViewModel.CredentialEvent.OnSaveClick)
            runCurrent()
            val stored = repository.getAllCredentials().single()
            assertEquals(TotpAlgorithm.SHA1, stored.totp?.algorithm)
            assertEquals(8, stored.totp?.digits)
            assertEquals(
                "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ",
                stored.totp?.secret?.toStringUnsafe(),
            )
            viewModel.clearForLock()
        }

    @Test
    fun `replacing and removing TOTP require confirmation`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        viewModel.createNewCredential(CredentialType.Login)
        viewModel.onEvent(
            CredentialViewModel.CredentialEvent.OnTotpSetupInputChanged(
                "JBSWY3DPEHPK3PXP",
            ),
        )
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnTotpAddClick)
        val original = viewModel.state.value.totpConfiguration?.secret?.toStringUnsafe()

        viewModel.onEvent(
            CredentialViewModel.CredentialEvent.OnTotpSetupInputChanged(
                "KRUGS4ZANFZSAYJA",
            ),
        )
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnTotpAddClick)
        assertTrue(viewModel.state.value.showTotpReplaceConfirmation)
        assertEquals(original, viewModel.state.value.totpConfiguration?.secret?.toStringUnsafe())

        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnTotpReplaceConfirm)
        assertEquals(
            "KRUGS4ZANFZSAYJA",
            viewModel.state.value.totpConfiguration?.secret?.toStringUnsafe(),
        )

        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnTotpRemoveClick)
        assertTrue(viewModel.state.value.showTotpRemoveConfirmation)
        assertTrue(viewModel.state.value.totpConfiguration != null)
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnTotpRemoveConfirm)
        assertEquals(null, viewModel.state.value.totpConfiguration)
        viewModel.clearForLock()
    }

    private class FixedClock(private val instant: Instant) : Clock {
        override fun now(): Instant = instant
    }

    private fun createViewModel(clock: Clock = Clock.System): CredentialViewModel =
        CredentialViewModel(
            credentialRepository = repository,
            folderRepository = folderRepository,
            clock = clock,
        )
}
