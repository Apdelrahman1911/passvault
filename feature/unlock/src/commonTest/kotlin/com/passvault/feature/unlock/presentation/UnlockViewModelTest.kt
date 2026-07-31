package com.passvault.feature.unlock.presentation

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.text.UiText
import app.cash.turbine.test
import com.passvault.core.domain.model.SecurityError
import com.passvault.core.domain.model.SessionId
import com.passvault.core.domain.model.VaultSessionState
import com.passvault.core.testing.fakes.FakeVaultRepository
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class UnlockViewModelTest {

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
    fun `missing vault navigates to onboarding`() = runTest(dispatcher) {
        val viewModel = UnlockViewModel(repository)

        viewModel.effect.test {
            runCurrent()
            assertIs<UnlockViewModel.UnlockEffect.NavigateToOnboarding>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `existing vault remains on unlock screen`() = runTest(dispatcher) {
        val viewModel = existingVaultViewModel()

        viewModel.effect.test {
            runCurrent()
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `password input is bounded and enables unlock`() = runTest(dispatcher) {
        val viewModel = existingVaultViewModel()

        viewModel.onEvent(UnlockViewModel.UnlockEvent.OnPasswordChanged("a".repeat(2_000)))

        assertEquals(1_024, viewModel.state.value.password.length)
        assertTrue(viewModel.state.value.canUnlock)
    }

    @Test
    fun `empty password is rejected before repository access`() = runTest(dispatcher) {
        val viewModel = existingVaultViewModel()

        viewModel.onEvent(UnlockViewModel.UnlockEvent.OnUnlockClick)

        assertEquals(
            Res.string.error_unlock_password_required,
            (viewModel.state.value.errorMessage as UiText.Resource).resource,
        )
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `successful unlock clears password and navigates once`() = runTest(dispatcher) {
        val viewModel = existingVaultViewModel()

        viewModel.effect.test {
            viewModel.onEvent(UnlockViewModel.UnlockEvent.OnPasswordChanged("correct password"))
            viewModel.onEvent(UnlockViewModel.UnlockEvent.OnUnlockClick)
            assertTrue(viewModel.state.value.isLoading)

            runCurrent()

            assertIs<UnlockViewModel.UnlockEffect.NavigateToVault>(awaitItem())
            assertEquals("", viewModel.state.value.password)
            assertEquals(0, viewModel.state.value.failedAttempts)
            assertFalse(viewModel.state.value.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `repeated unlocked state for one session does not duplicate navigation`() = runTest(dispatcher) {
        val viewModel = existingVaultViewModel()
        val unlocked = VaultSessionState.Unlocked(SessionId("one-session"))

        viewModel.effect.test {
            repository.setSessionState(unlocked)
            runCurrent()
            assertIs<UnlockViewModel.UnlockEffect.NavigateToVault>(awaitItem())

            repository.setSessionState(VaultSessionState.Locked)
            repository.setSessionState(unlocked)
            runCurrent()
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `failed unlock clears password and exposes only a generic error`() = runTest(dispatcher) {
        val viewModel = existingVaultViewModel()
        repository.setShouldFail(IllegalStateException("secret database path"))

        viewModel.onEvent(UnlockViewModel.UnlockEvent.OnPasswordChanged("wrong password"))
        viewModel.onEvent(UnlockViewModel.UnlockEvent.OnUnlockClick)
        runCurrent()

        assertEquals("", viewModel.state.value.password)
        assertEquals(1, viewModel.state.value.failedAttempts)
        assertEquals(
            Res.string.error_unlock_failed,
            (viewModel.state.value.errorMessage as UiText.Resource).resource,
        )
    }

    @Test
    fun `rapid repeated submit starts only one unlock operation`() = runTest(dispatcher) {
        val viewModel = existingVaultViewModel()
        repository.setUnlockDelay(100)

        viewModel.onEvent(UnlockViewModel.UnlockEvent.OnPasswordChanged("password"))
        repeat(3) {
            viewModel.onEvent(UnlockViewModel.UnlockEvent.OnUnlockClick)
        }
        runCurrent()

        assertTrue(viewModel.state.value.isLoading)
        advanceTimeBy(100)
        runCurrent()
        assertEquals("", viewModel.state.value.password)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `five failures start a bounded cooldown and disable submit`() = runTest(dispatcher) {
        val viewModel = existingVaultViewModel()

        repeat(5) { attempt ->
            repository.setShouldFail(IllegalArgumentException("wrong"))
            viewModel.onEvent(UnlockViewModel.UnlockEvent.OnPasswordChanged("wrong-$attempt"))
            viewModel.onEvent(UnlockViewModel.UnlockEvent.OnUnlockClick)
            runCurrent()
        }

        assertTrue(viewModel.state.value.isLockedOut)
        assertFalse(viewModel.state.value.canUnlock)

        advanceTimeBy(30_000)
        runCurrent()

        assertEquals(0, viewModel.state.value.failedAttempts)
        assertFalse(viewModel.state.value.isLockedOut)
    }

    @Test
    fun `fatal session errors are mapped to stable redacted messages`() = runTest(dispatcher) {
        val viewModel = existingVaultViewModel()

        repository.setSessionState(
            VaultSessionState.FatalError(SecurityError.Fatal("C:\\Users\\person\\vault.db")),
        )
        runCurrent()

        assertEquals(
            Res.string.error_unlock_fatal,
            (viewModel.state.value.errorMessage as UiText.Resource).resource,
        )
    }

    @Test
    fun `authentication failure reports bounded remaining attempts`() = runTest(dispatcher) {
        val viewModel = existingVaultViewModel()

        repository.setSessionState(
            VaultSessionState.FatalError(SecurityError.AuthenticationFailed(attempts = 3)),
        )
        runCurrent()

        assertEquals(3, viewModel.state.value.failedAttempts)
        val message = viewModel.state.value.errorMessage as UiText.Resource
        assertEquals(Res.string.error_unlock_auth_remaining, message.resource)
        assertEquals(listOf(2), message.arguments)
    }

    @Test
    fun `recovery information is state driven and dismissible`() = runTest(dispatcher) {
        val viewModel = existingVaultViewModel()

        viewModel.onEvent(UnlockViewModel.UnlockEvent.OnForgotPasswordClick)
        assertTrue(viewModel.state.value.showRecoveryInfo)

        viewModel.onEvent(UnlockViewModel.UnlockEvent.OnDismissRecoveryInfo)
        assertFalse(viewModel.state.value.showRecoveryInfo)
    }

    @Test
    fun `clear for lock removes authentication material and transient state`() = runTest(dispatcher) {
        val viewModel = existingVaultViewModel()
        repository.setShouldFail(IllegalArgumentException("wrong"))
        viewModel.onEvent(UnlockViewModel.UnlockEvent.OnPasswordChanged("sensitive value"))
        viewModel.onEvent(UnlockViewModel.UnlockEvent.OnUnlockClick)
        runCurrent()

        viewModel.clearForLock()

        assertEquals("", viewModel.state.value.password)
        assertNull(viewModel.state.value.errorMessage)
        assertEquals(0, viewModel.state.value.failedAttempts)
        assertFalse(viewModel.state.value.showRecoveryInfo)
    }

    private fun existingVaultViewModel(): UnlockViewModel {
        repository.setupExistingVault()
        return UnlockViewModel(repository)
    }
}
