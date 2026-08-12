package com.passvault.feature.health.presentation

import app.cash.turbine.test
import com.passvault.core.domain.model.CredentialId
import com.passvault.core.domain.model.PasswordHealth
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.model.TotpConfiguration
import com.passvault.core.domain.repository.CredentialRepository
import com.passvault.core.testing.TestData
import com.passvault.core.testing.fakes.FakeCredentialRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HealthViewModelSensitiveCleanupTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        dispatcher.scheduler.runCurrent()
        Dispatchers.resetMain()
    }

    @Test
    fun `completed health scan clears every secret in its minimal repository input`() = runTest(dispatcher) {
        val repository = FakeCredentialRepository()
        val source = TestData.loginCredential().copy(
            totp = TotpConfiguration(secret = SensitiveText.from("JBSWY3DPEHPK3PXP")),
        )
        repository.setupCredentials(source)
        val viewModel = HealthViewModel(repository)

        viewModel.onEvent(HealthViewModel.HealthEvent.OnRefreshScan)
        advanceUntilIdle()

        val scannedCredential = repository.getLastHealthInputsForTest().single()
        val exposedValues = listOfNotNull(
            scannedCredential.username?.expose(),
            scannedCredential.email?.expose(),
            scannedCredential.password?.expose(),
        )
        try {
            assertTrue(exposedValues.all { value -> value.all { it == '\u0000' } })
        } finally {
            exposedValues.forEach { it.fill('\u0000') }
            source.password?.clear()
            source.username?.clear()
            source.totp?.clear()
            repository.reset()
        }
    }

    @Test
    fun `weakness reasons count supplementary characters as single characters`() = runTest(dispatcher) {
        val repository = FakeCredentialRepository()
        val source = TestData.loginCredential().copy(
            password = SensitiveText.from("🔐".repeat(6)),
        )
        repository.setupCredentials(source)
        source.password?.clear()
        val viewModel = HealthViewModel(repository)

        viewModel.onEvent(HealthViewModel.HealthEvent.OnRefreshScan)
        advanceUntilIdle()

        assertEquals(
            HealthViewModel.WeakPasswordReason.TOO_SHORT,
            viewModel.state.value.weakPasswords.single().reason,
        )
        repository.reset()
    }

    @Test
    fun `navigation effects are not replayed to a later collector`() = runTest(dispatcher) {
        val viewModel = HealthViewModel(FakeCredentialRepository())

        viewModel.onEvent(
            HealthViewModel.HealthEvent.OnCredentialClick(CredentialId("credential-id")),
        )

        viewModel.effect.test {
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `back closes duplicate dialog before navigating`() = runTest(dispatcher) {
        val viewModel = HealthViewModel(FakeCredentialRepository())
        val group = HealthViewModel.DuplicateGroup(
            credentials = listOf(
                HealthViewModel.DuplicateItem(
                    credentialId = CredentialId("credential-id"),
                    title = "Example",
                    username = null,
                ),
            ),
        )

        viewModel.effect.test {
            viewModel.onEvent(HealthViewModel.HealthEvent.OnFixDuplicateClick(group))
            viewModel.onEvent(HealthViewModel.HealthEvent.OnBackClick)
            assertNull(viewModel.state.value.showingDuplicateGroup)
            expectNoEvents()

            viewModel.onEvent(HealthViewModel.HealthEvent.OnBackClick)
            assertEquals(HealthViewModel.HealthEffect.NavigateBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `dismissing a copy result preserves a scan persistence error`() = runTest(dispatcher) {
        val backingRepository = FakeCredentialRepository()
        val source = TestData.loginCredential(password = "weak")
        backingRepository.setupCredentials(source)
        source.password?.clear()
        source.username?.clear()
        val repository = object : CredentialRepository by backingRepository {
            override suspend fun updateHealth(id: CredentialId, health: PasswordHealth): Result<Unit> =
                Result.failure(IllegalStateException("Persistence failed"))
        }
        val viewModel = HealthViewModel(repository)

        viewModel.onEvent(HealthViewModel.HealthEvent.OnRefreshScan)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.errorMessage != null)

        viewModel.onEvent(HealthViewModel.HealthEvent.OnCopySummaryResult(succeeded = false))
        assertTrue(viewModel.state.value.transientMessage != null)
        viewModel.onEvent(HealthViewModel.HealthEvent.OnDismissMessage)

        assertNull(viewModel.state.value.transientMessage)
        assertTrue(viewModel.state.value.errorMessage != null)
        backingRepository.reset()
    }
}
