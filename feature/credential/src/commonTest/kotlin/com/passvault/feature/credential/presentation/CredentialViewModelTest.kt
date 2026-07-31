package com.passvault.feature.credential.presentation

import app.cash.turbine.test
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.text.UiText
import com.passvault.core.domain.model.CustomField
import com.passvault.core.domain.model.CustomFieldId
import com.passvault.core.domain.model.PasswordHealth
import com.passvault.core.domain.model.PasswordHistoryEntry
import com.passvault.core.domain.model.PasswordScore
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.testing.TestData
import com.passvault.core.testing.fakes.FakeCredentialRepository
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

@OptIn(ExperimentalCoroutinesApi::class)
class CredentialViewModelTest {

    private lateinit var dispatcher: TestDispatcher
    private lateinit var repository: FakeCredentialRepository

    @BeforeTest
    fun setUp() {
        dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)
        repository = FakeCredentialRepository()
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

        val viewModel = CredentialViewModel(repository)
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
    fun `invalid title and url are rejected before save`() = runTest(dispatcher) {
        val viewModel = CredentialViewModel(repository)
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
        val viewModel = CredentialViewModel(repository)
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
        val viewModel = CredentialViewModel(repository)
        viewModel.createNewCredential(com.passvault.core.domain.model.CredentialType.Login)
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnTitleChanged("Pending"))
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnPasswordChanged("sensitive"))
        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnSaveClick)
        runCurrent()

        viewModel.clearForLock()
        advanceTimeBy(1_000)
        runCurrent()

        assertEquals("", viewModel.state.value.password)
        assertEquals(0, repository.getCredentialCount())
        assertFalse(viewModel.state.value.isSaving)
    }

    @Test
    fun `copy and launch events are explicit and normalize safe urls`() = runTest(dispatcher) {
        val viewModel = CredentialViewModel(repository)
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
}
