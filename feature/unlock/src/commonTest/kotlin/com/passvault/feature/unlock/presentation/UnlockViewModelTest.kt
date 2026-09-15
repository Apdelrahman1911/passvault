package com.passvault.feature.unlock.presentation

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.text.UiText
import app.cash.turbine.test
import com.passvault.core.domain.model.MasterPasswordPolicy
import com.passvault.core.domain.model.SessionId
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.model.VaultSessionState
import com.passvault.core.domain.model.codePointLength
import com.passvault.core.testing.fakes.FakeVaultRepository
import com.passvault.core.testing.fakes.FakeBiometricUnlockService
import com.passvault.core.security.BiometricAvailability
import com.passvault.core.security.BiometricFailureReason
import com.passvault.core.security.BiometricOperationResult
import com.passvault.core.security.BiometricType
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
    private lateinit var biometricService: FakeBiometricUnlockService

    @BeforeTest
    fun setUp() {
        dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)
        repository = FakeVaultRepository()
        biometricService = FakeBiometricUnlockService()
    }

    @AfterTest
    fun tearDown() {
        repository.reset()
        dispatcher.scheduler.runCurrent()
        Dispatchers.resetMain()
    }

    @Test
    fun `missing vault navigates to onboarding`() = runTest(dispatcher) {
        val viewModel = UnlockViewModel(repository, biometricService)

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
    fun `overlong password input keeps a sentinel and disables unlock`() = runTest(dispatcher) {
        val viewModel = existingVaultViewModel()

        viewModel.onEvent(
            UnlockViewModel.UnlockEvent.OnPasswordChanged("🔐".repeat(MasterPasswordPolicy.MAX_LENGTH + 50)),
        )

        assertEquals(MasterPasswordPolicy.MAX_LENGTH + 1, viewModel.state.value.password.codePointLength())
        assertEquals("🔐".repeat(MasterPasswordPolicy.MAX_LENGTH + 1), viewModel.state.value.password)
        assertEquals(
            Res.string.error_master_password_too_long,
            (viewModel.state.value.errorMessage as UiText.Resource).resource,
        )
        assertFalse(viewModel.state.value.canUnlock)
    }

    @Test
    fun `malformed password input is rejected before repository access`() = runTest(dispatcher) {
        val viewModel = existingVaultViewModel()

        viewModel.onEvent(UnlockViewModel.UnlockEvent.OnPasswordChanged("password\uD800"))

        assertEquals(
            Res.string.error_master_password_invalid,
            (viewModel.state.value.errorMessage as UiText.Resource).resource,
        )
        assertFalse(viewModel.state.value.canUnlock)
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
            assertEquals("", viewModel.state.value.password)
            assertTrue(viewModel.state.value.isLoading)

            runCurrent()

            assertIs<UnlockViewModel.UnlockEffect.NavigateToVault>(awaitItem())
            assertEquals("", viewModel.state.value.password)
            assertEquals(0, viewModel.state.value.failedAttempts)
            assertFalse(viewModel.state.value.isLoading)
            assertCleared(repository.lastUnlockPassword)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `repeated unlocked state for one session does not duplicate navigation`() = runTest(dispatcher) {
        val viewModel = existingVaultViewModel()
        val unlocked = VaultSessionState.Unlocked(SessionId("one-session"))

        viewModel.effect.test {
            repository.currentSessionState = unlocked
            runCurrent()
            assertIs<UnlockViewModel.UnlockEffect.NavigateToVault>(awaitItem())

            repository.currentSessionState = VaultSessionState.Locked()
            repository.currentSessionState = unlocked
            runCurrent()
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `failed unlock clears password and exposes a stable password error`() = runTest(dispatcher) {
        val viewModel = existingVaultViewModel()
        repository.setShouldFail(IllegalStateException("secret database path"))

        viewModel.onEvent(UnlockViewModel.UnlockEvent.OnPasswordChanged("wrong password"))
        viewModel.onEvent(UnlockViewModel.UnlockEvent.OnUnlockClick)
        assertEquals("", viewModel.state.value.password)
        runCurrent()

        assertEquals("", viewModel.state.value.password)
        assertEquals(1, viewModel.state.value.failedAttempts)
        assertEquals(
            Res.string.error_unlock_failed,
            (viewModel.state.value.errorMessage as UiText.Resource).resource,
        )
        assertCleared(repository.lastUnlockPassword)
    }

    @Test
    fun `rapid repeated submit starts only one unlock operation`() = runTest(dispatcher) {
        val viewModel = existingVaultViewModel()
        repository.unlockDelayMillis = 100

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
    fun `cancelling before a scheduled unlock runs releases the submit guard`() = runTest(dispatcher) {
        val viewModel = existingVaultViewModel()
        repository.unlockDelayMillis = 1_000

        viewModel.onEvent(UnlockViewModel.UnlockEvent.OnPasswordChanged("first-attempt"))
        viewModel.onEvent(UnlockViewModel.UnlockEvent.OnUnlockClick)
        assertEquals("", viewModel.state.value.password)
        viewModel.clearForLock()
        runCurrent()

        repository.unlockDelayMillis = 0
        viewModel.onEvent(UnlockViewModel.UnlockEvent.OnPasswordChanged("retry"))
        viewModel.onEvent(UnlockViewModel.UnlockEvent.OnUnlockClick)
        runCurrent()

        assertIs<VaultSessionState.Unlocked>(repository.currentSessionState)
        assertEquals("", viewModel.state.value.password)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `cancelling an active unlock clears the owned password`() = runTest(dispatcher) {
        val viewModel = existingVaultViewModel()
        repository.unlockDelayMillis = 1_000

        viewModel.onEvent(UnlockViewModel.UnlockEvent.OnPasswordChanged("first-attempt"))
        viewModel.onEvent(UnlockViewModel.UnlockEvent.OnUnlockClick)
        runCurrent()
        val cancelledPassword = requireNotNull(repository.lastUnlockPassword)

        viewModel.clearForLock()
        runCurrent()

        assertCleared(cancelledPassword)
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
        assertEquals(
            Res.string.error_unlock_cooldown,
            (viewModel.state.value.errorMessage as UiText.Resource).resource,
        )

        advanceTimeBy(30_000)
        runCurrent()

        assertEquals(0, viewModel.state.value.failedAttempts)
        assertFalse(viewModel.state.value.isLockedOut)
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
    fun `available enrolled biometrics are exposed for the unlock action`() = runTest(dispatcher) {
        biometricService.setStatus(
            availability = BiometricAvailability.AVAILABLE,
            enabled = true,
            type = BiometricType.FACE_ID,
        )
        val viewModel = existingVaultViewModel()
        viewModel.onEvent(UnlockViewModel.UnlockEvent.OnUnlockScreenReady)

        runCurrent()

        assertEquals(BiometricType.FACE_ID, viewModel.state.value.biometricType)
        assertEquals(BiometricAvailability.AVAILABLE, viewModel.state.value.biometricAvailability)
        assertTrue(viewModel.state.value.isBiometricEnabled)
    }

    @Test
    fun `biometric cancellation returns to idle without an error`() = runTest(dispatcher) {
        biometricService.setStatus(BiometricAvailability.AVAILABLE, enabled = true)
        biometricService.setUnlockResult(BiometricOperationResult.Cancelled)
        val viewModel = existingVaultViewModel()
        viewModel.onEvent(UnlockViewModel.UnlockEvent.OnUnlockScreenReady)
        runCurrent()

        viewModel.onEvent(UnlockViewModel.UnlockEvent.OnBiometricUnlockClick)
        runCurrent()

        assertEquals(1, biometricService.unlockCalls)
        assertFalse(viewModel.state.value.isLoading)
        assertFalse(viewModel.state.value.isBiometricLoading)
        assertNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun `invalidated biometric credential is disabled and requires password`() = runTest(dispatcher) {
        biometricService.setStatus(BiometricAvailability.AVAILABLE, enabled = true)
        biometricService.setUnlockResult(
            BiometricOperationResult.Failure(BiometricFailureReason.INVALIDATED),
        )
        val viewModel = existingVaultViewModel()
        viewModel.onEvent(UnlockViewModel.UnlockEvent.OnUnlockScreenReady)
        runCurrent()

        viewModel.onEvent(UnlockViewModel.UnlockEvent.OnBiometricUnlockClick)
        runCurrent()

        assertFalse(viewModel.state.value.isBiometricEnabled)
        assertEquals(
            Res.string.error_biometric_invalidated,
            (viewModel.state.value.errorMessage as UiText.Resource).resource,
        )
    }

    @Test
    fun `cold start confirms an ambiguous disabled biometric status after screen presentation`() = runTest(dispatcher) {
        biometricService.setStatus(BiometricAvailability.AVAILABLE, enabled = false, type = BiometricType.TOUCH_ID)
        val viewModel = existingVaultViewModel()

        runCurrent()
        assertFalse(viewModel.state.value.isBiometricStatusLoaded)

        viewModel.onEvent(UnlockViewModel.UnlockEvent.OnUnlockScreenReady)
        runCurrent()

        assertTrue(viewModel.state.value.isBiometricStatusLoaded)
        assertFalse(viewModel.state.value.canUseBiometrics)

        biometricService.setStatus(BiometricAvailability.AVAILABLE, enabled = true, type = BiometricType.TOUCH_ID)
        advanceTimeBy(150)
        runCurrent()

        assertTrue(viewModel.state.value.canUseBiometrics)
        assertEquals(BiometricType.TOUCH_ID, viewModel.state.value.biometricType)
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
        return UnlockViewModel(repository, biometricService)
    }

    private fun assertCleared(value: SensitiveText?) {
        val characters = requireNotNull(value).expose()
        try {
            assertTrue(characters.all { it == '\u0000' })
        } finally {
            characters.fill('\u0000')
        }
    }
}
