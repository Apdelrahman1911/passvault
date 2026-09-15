package com.passvault.feature.credential.presentation

import app.cash.turbine.test
import com.passvault.core.crypto.SecurePasswordGenerator
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.error_credential_link_invalid
import com.passvault.core.designsystem.text.UiText
import com.passvault.core.domain.model.CredentialType
import com.passvault.core.testing.fakes.FakeCredentialRepository
import com.passvault.core.testing.fakes.FakeCryptoEngine
import com.passvault.core.testing.fakes.FakeFolderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class CredentialUrlLaunchTest {

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
    fun `launch emits only accepted web url values`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        viewModel.createNewCredential(CredentialType.Login)

        viewModel.effect.test {
            viewModel.onEvent(CredentialViewModel.CredentialEvent.OnLaunchUrlClick("example.com"))
            viewModel.onEvent(CredentialViewModel.CredentialEvent.OnLaunchUrlClick("http://127.0.0.1:8080/login"))
            viewModel.onEvent(CredentialViewModel.CredentialEvent.OnLaunchUrlClick("https://[2001:db8::1]:8443"))

            assertEquals(CredentialViewModel.CredentialEffect.LaunchUrl("https://example.com"), awaitItem())
            assertEquals(
                CredentialViewModel.CredentialEffect.LaunchUrl("http://127.0.0.1:8080/login"),
                awaitItem(),
            )
            assertEquals(
                CredentialViewModel.CredentialEffect.LaunchUrl("https://[2001:db8::1]:8443"),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `launch rejects non web and ambiguous values without emitting effects`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        viewModel.createNewCredential(CredentialType.Login)
        val rejectedUrls = listOf(
            "javascript:alert(1)",
            "file:///private/data",
            "data:text/plain,hello",
            "intent://example.com",
            "ftp://example.com",
            "https://user@example.com",
            "https://example.com/path with space",
            "https://example.com\\redirect",
        )

        viewModel.effect.test {
            rejectedUrls.forEach { url ->
                viewModel.onEvent(CredentialViewModel.CredentialEvent.OnLaunchUrlClick(url))
            }
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(
            Res.string.error_credential_link_invalid,
            (viewModel.state.value.errorMessage as UiText.Resource).resource,
        )
    }

    private fun createViewModel(): CredentialViewModel = CredentialViewModel(
        credentialRepository = repository,
        folderRepository = FakeFolderRepository(),
        passwordGenerator = SecurePasswordGenerator(FakeCryptoEngine()),
    )
}
