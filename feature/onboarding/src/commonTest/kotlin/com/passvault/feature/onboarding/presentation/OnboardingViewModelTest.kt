package com.passvault.feature.onboarding.presentation

import app.cash.turbine.test
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.error_master_password_invalid
import com.passvault.core.designsystem.generated.resources.error_master_password_predictable
import com.passvault.core.designsystem.generated.resources.error_vault_setup_exists
import com.passvault.core.designsystem.text.UiText
import com.passvault.core.domain.model.MasterPasswordPolicy
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.model.VaultSessionState
import com.passvault.core.domain.model.codePointLength
import com.passvault.core.testing.fakes.FakeVaultRepository
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
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {
    private lateinit var dispatcher: TestDispatcher
    private lateinit var repository: FakeVaultRepository

    @BeforeTest
    fun setUp() {
        dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)
        repository = FakeVaultRepository()
    }

    @AfterTest
    fun tearDown() {
        repository.reset()
        dispatcher.scheduler.runCurrent()
        Dispatchers.resetMain()
    }

    @Test
    fun `master password and confirmation are bounded`() = runTest(dispatcher) {
        val viewModel = OnboardingViewModel(repository)
        val oversized = "🔐".repeat(MasterPasswordPolicy.MAX_LENGTH + 50)

        viewModel.onEvent(OnboardingViewModel.OnboardingEvent.OnPasswordChanged(oversized))
        assertEquals(
            MasterPasswordPolicy.MAX_LENGTH + 1,
            viewModel.state.value.masterPassword.codePointLength(),
        )

        viewModel.onEvent(OnboardingViewModel.OnboardingEvent.OnConfirmPasswordChanged(oversized))
        assertEquals(
            MasterPasswordPolicy.MAX_LENGTH + 1,
            viewModel.state.value.confirmPassword.codePointLength(),
        )
        assertFalse(viewModel.state.value.canCreateVault)
    }

    @Test
    fun `supplementary characters count as one password character`() = runTest(dispatcher) {
        val viewModel = OnboardingViewModel(repository)

        viewModel.onEvent(
            OnboardingViewModel.OnboardingEvent.OnPasswordChanged("🔐🔑🛡️🔒🔓🔏"),
        )

        assertEquals(OnboardingViewModel.PasswordStrength.TOO_SHORT, viewModel.state.value.passwordStrength)
        assertFalse(viewModel.state.value.canContinueToConfirmation)
    }

    @Test
    fun `malformed password text is rejected before vault creation`() = runTest(dispatcher) {
        val viewModel = OnboardingViewModel(repository)

        viewModel.onEvent(
            OnboardingViewModel.OnboardingEvent.OnPasswordChanged("$STRONG_PASSWORD\uD800"),
        )

        assertEquals(
            Res.string.error_master_password_invalid,
            (viewModel.state.value.errorMessage as UiText.Resource).resource,
        )
        assertFalse(viewModel.state.value.canContinueToConfirmation)
    }

    @Test
    fun `predictable policy-length password is rejected before vault creation`() = runTest(dispatcher) {
        val viewModel = OnboardingViewModel(repository)

        viewModel.onEvent(OnboardingViewModel.OnboardingEvent.OnPasswordChanged("Summer2024!!"))
        viewModel.onEvent(OnboardingViewModel.OnboardingEvent.OnCreateVaultClick)

        assertEquals(
            Res.string.error_master_password_predictable,
            (viewModel.state.value.errorMessage as UiText.Resource).resource,
        )
        assertFalse(viewModel.state.value.canContinueToConfirmation)
        assertFalse(repository.exists().getOrThrow())
    }

    @Test
    fun `back clears stale password validation feedback`() = runTest(dispatcher) {
        val viewModel = OnboardingViewModel(repository)
        viewModel.onEvent(OnboardingViewModel.OnboardingEvent.OnPasswordChanged("short"))
        viewModel.onEvent(OnboardingViewModel.OnboardingEvent.OnCreateVaultClick)
        assertTrue(viewModel.state.value.errorMessage != null)

        viewModel.onEvent(OnboardingViewModel.OnboardingEvent.OnBackClick)

        assertEquals("", viewModel.state.value.masterPassword)
        assertNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun `valid password advances once and navigation is not replayed`() = runTest(dispatcher) {
        val viewModel = OnboardingViewModel(repository)
        viewModel.onEvent(OnboardingViewModel.OnboardingEvent.OnPasswordChanged(STRONG_PASSWORD))

        viewModel.effect.test {
            viewModel.onEvent(OnboardingViewModel.OnboardingEvent.OnCreateVaultClick)
            assertIs<OnboardingViewModel.OnboardingEffect.NavigateToMasterPasswordConfirmation>(awaitItem())
            assertEquals("", viewModel.state.value.masterPassword)
            assertTrue(viewModel.state.value.hasPendingMasterPassword)
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.effect.test {
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `matching strong password creates unlocks and clears inputs`() = runTest(dispatcher) {
        val viewModel = OnboardingViewModel(repository)
        enterMatchingPassword(viewModel)

        viewModel.effect.test {
            viewModel.onEvent(OnboardingViewModel.OnboardingEvent.OnConfirmPasswordClick)
            assertEquals("", viewModel.state.value.confirmPassword)
            assertFalse(viewModel.state.value.hasPendingMasterPassword)
            assertTrue(viewModel.state.value.isLoading)
            runCurrent()

            assertIs<OnboardingViewModel.OnboardingEffect.NavigateToSecurityExplanation>(awaitItem())
            assertEquals("", viewModel.state.value.masterPassword)
            assertEquals("", viewModel.state.value.confirmPassword)
            assertTrue(viewModel.state.value.vaultCreated)
            assertFalse(viewModel.state.value.isLoading)
            assertIs<VaultSessionState.Unlocked>(repository.currentSessionState)
            assertCleared(repository.lastCreatePassword)
            assertCleared(repository.lastUnlockPassword)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `preexisting vault is never replaced by onboarding`() = runTest(dispatcher) {
        repository.setupExistingVault()
        val viewModel = OnboardingViewModel(repository)
        enterMatchingPassword(viewModel)

        viewModel.onEvent(OnboardingViewModel.OnboardingEvent.OnConfirmPasswordClick)
        runCurrent()

        assertEquals(
            Res.string.error_vault_setup_exists,
            (viewModel.state.value.errorMessage as UiText.Resource).resource,
        )
        assertFalse(viewModel.state.value.vaultCreated)
        assertFalse(viewModel.state.value.isLoading)
        assertEquals("", viewModel.state.value.masterPassword)
        assertEquals("", viewModel.state.value.confirmPassword)
        assertFalse(viewModel.state.value.hasPendingMasterPassword)
    }

    @Test
    fun `clear for lock cancels setup and removes password state`() = runTest(dispatcher) {
        val viewModel = OnboardingViewModel(repository)
        enterMatchingPassword(viewModel)

        viewModel.clearForLock()

        assertEquals("", viewModel.state.value.masterPassword)
        assertEquals("", viewModel.state.value.confirmPassword)
        assertFalse(viewModel.state.value.hasPendingMasterPassword)
        assertNull(viewModel.state.value.errorMessage)
        assertFalse(viewModel.state.value.passwordsMatch)
        assertFalse(viewModel.state.value.vaultCreated)
    }

    private fun enterMatchingPassword(viewModel: OnboardingViewModel) {
        viewModel.onEvent(OnboardingViewModel.OnboardingEvent.OnPasswordChanged(STRONG_PASSWORD))
        viewModel.onEvent(OnboardingViewModel.OnboardingEvent.OnCreateVaultClick)
        assertEquals("", viewModel.state.value.masterPassword)
        assertTrue(viewModel.state.value.hasPendingMasterPassword)
        viewModel.onEvent(OnboardingViewModel.OnboardingEvent.OnConfirmPasswordChanged(STRONG_PASSWORD))
        assertTrue(viewModel.state.value.canCreateVault)
    }

    private fun assertCleared(value: SensitiveText?) {
        val characters = requireNotNull(value).expose()
        try {
            assertTrue(characters.all { it == '\u0000' })
        } finally {
            characters.fill('\u0000')
        }
    }

    private companion object {
        const val STRONG_PASSWORD = "Cedar-Lantern_92!Orbit"
    }
}
