package com.passvault.feature.settings.presentation

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.error_master_password_mismatch
import com.passvault.core.designsystem.generated.resources.error_master_password_too_long
import com.passvault.core.designsystem.generated.resources.error_new_password_weak
import com.passvault.core.designsystem.text.UiText
import com.passvault.core.domain.model.MasterPasswordPolicy
import com.passvault.core.domain.model.codePointLength
import com.passvault.core.domain.repository.AppSettings
import com.passvault.core.domain.repository.AppSettingsStore
import com.passvault.core.domain.repository.LanguagePreference
import com.passvault.core.security.BiometricAvailability
import com.passvault.core.security.BiometricOperationResult
import com.passvault.core.security.BiometricType
import com.passvault.core.testing.fakes.FakeBiometricUnlockService
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsBiometricViewModelTest {
    private lateinit var dispatcher: TestDispatcher
    private lateinit var repository: FakeVaultRepository
    private lateinit var biometricService: FakeBiometricUnlockService

    @BeforeTest
    fun setUp() {
        dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)
        repository = FakeVaultRepository().apply { setupExistingVault() }
        biometricService = FakeBiometricUnlockService().apply {
            setStatus(
                availability = BiometricAvailability.AVAILABLE,
                enabled = false,
                type = BiometricType.FACE_ID,
            )
        }
    }

    @AfterTest
    fun tearDown() {
        repository.reset()
        dispatcher.scheduler.runCurrent()
        Dispatchers.resetMain()
    }

    @Test
    fun `enabling biometrics updates security state after platform success`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        runCurrent()

        viewModel.onEvent(SettingsViewModel.SettingsEvent.OnBiometricUnlockChanged(true))
        runCurrent()

        assertEquals(1, biometricService.enableCalls)
        assertTrue(viewModel.state.value.isBiometricEnabled)
        assertFalse(viewModel.state.value.isBiometricLoading)
        assertEquals(BiometricType.FACE_ID, viewModel.state.value.biometricType)
    }

    @Test
    fun `biometric capability check starts in a loading state`() = runTest(dispatcher) {
        val viewModel = createViewModel()

        assertTrue(viewModel.state.value.isBiometricLoading)

        runCurrent()
        assertFalse(viewModel.state.value.isBiometricLoading)
    }

    @Test
    fun `cancelled biometric enrollment leaves setting off without an error`() = runTest(dispatcher) {
        biometricService.setEnableResult(BiometricOperationResult.Cancelled)
        val viewModel = createViewModel()
        runCurrent()

        viewModel.onEvent(SettingsViewModel.SettingsEvent.OnBiometricUnlockChanged(true))
        runCurrent()

        assertFalse(viewModel.state.value.isBiometricEnabled)
        assertFalse(viewModel.state.value.isBiometricLoading)
        assertNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun `an unavailable enrolled method can still be explicitly disabled`() = runTest(dispatcher) {
        biometricService.setStatus(
            availability = BiometricAvailability.NOT_ENROLLED,
            enabled = true,
            type = BiometricType.WINDOWS_HELLO,
        )
        val viewModel = createViewModel()
        runCurrent()

        viewModel.onEvent(SettingsViewModel.SettingsEvent.OnBiometricUnlockChanged(false))
        runCurrent()

        assertEquals(1, biometricService.disableCalls)
        assertFalse(viewModel.state.value.isBiometricEnabled)
    }

    @Test
    fun `supplementary characters count as one new password character`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        runCurrent()

        viewModel.onEvent(
            SettingsViewModel.SettingsEvent.OnNewPasswordChanged("🔐🔑🛡️🔒🔓🔏"),
        )

        assertEquals(SettingsViewModel.PasswordStrength.TOO_SHORT, viewModel.state.value.passwordStrength)
        assertFalse(viewModel.state.value.canChangePassword)
    }

    @Test
    fun `overlong new master password is not silently truncated`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        runCurrent()

        viewModel.onEvent(
            SettingsViewModel.SettingsEvent.OnNewPasswordChanged(
                "🔐".repeat(MasterPasswordPolicy.MAX_LENGTH + 50),
            ),
        )

        assertEquals(
            MasterPasswordPolicy.MAX_LENGTH + 1,
            viewModel.state.value.newPassword.codePointLength(),
        )
        assertEquals(
            Res.string.error_master_password_too_long,
            (viewModel.state.value.passwordError as UiText.Resource).resource,
        )
        assertFalse(viewModel.state.value.canChangePassword)
    }

    @Test
    fun `predictable policy-length password is rejected before password change`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        runCurrent()
        val predictablePassword = "Summer2024!!"

        viewModel.onEvent(SettingsViewModel.SettingsEvent.OnCurrentPasswordChanged("legacy-current-password"))
        viewModel.onEvent(SettingsViewModel.SettingsEvent.OnNewPasswordChanged(predictablePassword))
        viewModel.onEvent(SettingsViewModel.SettingsEvent.OnConfirmPasswordChanged(predictablePassword))
        viewModel.onEvent(SettingsViewModel.SettingsEvent.OnChangePasswordConfirm)

        assertEquals(
            Res.string.error_new_password_weak,
            (viewModel.state.value.passwordError as UiText.Resource).resource,
        )
        assertFalse(viewModel.state.value.canChangePassword)
        assertFalse(viewModel.state.value.isChangingPassword)
    }

    @Test
    fun `confirmation mismatch is explained while editing`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        runCurrent()

        viewModel.onEvent(SettingsViewModel.SettingsEvent.OnNewPasswordChanged(STRONG_PASSWORD))
        viewModel.onEvent(SettingsViewModel.SettingsEvent.OnConfirmPasswordChanged("different-password"))

        assertEquals(
            Res.string.error_master_password_mismatch,
            (viewModel.state.value.passwordError as UiText.Resource).resource,
        )
        assertFalse(viewModel.state.value.canChangePassword)
    }

    @Test
    fun `language change updates the app state and persistent preference`() = runTest(dispatcher) {
        val settingsStore = InMemoryAppSettingsStore()
        val viewModel = createViewModel(settingsStore)
        runCurrent()

        viewModel.onEvent(
            SettingsViewModel.SettingsEvent.OnLanguageChanged(SettingsViewModel.AppLanguage.ARABIC),
        )
        runCurrent()

        assertEquals(SettingsViewModel.AppLanguage.ARABIC, viewModel.state.value.language)
        assertEquals(LanguagePreference.ARABIC, settingsStore.settings.language)
    }

    @Test
    fun `password inputs cannot repopulate after change starts`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        runCurrent()
        viewModel.onEvent(SettingsViewModel.SettingsEvent.OnCurrentPasswordChanged("current-password"))
        viewModel.onEvent(SettingsViewModel.SettingsEvent.OnNewPasswordChanged(STRONG_PASSWORD))
        viewModel.onEvent(SettingsViewModel.SettingsEvent.OnConfirmPasswordChanged(STRONG_PASSWORD))

        viewModel.onEvent(SettingsViewModel.SettingsEvent.OnChangePasswordConfirm)
        assertTrue(viewModel.state.value.isChangingPassword)
        viewModel.onEvent(SettingsViewModel.SettingsEvent.OnCurrentPasswordChanged("queued-secret"))

        assertEquals("", viewModel.state.value.currentPassword)
    }

    private fun createViewModel(
        settingsStore: AppSettingsStore = InMemoryAppSettingsStore(),
    ): SettingsViewModel = SettingsViewModel(
        vaultRepository = repository,
        appSettingsStore = settingsStore,
        biometricUnlockService = biometricService,
    )

    private class InMemoryAppSettingsStore : AppSettingsStore {
        var settings = AppSettings()
            private set

        override suspend fun load(): Result<AppSettings> = Result.success(settings)

        override suspend fun save(settings: AppSettings): Result<Unit> {
            this.settings = settings
            return Result.success(Unit)
        }
    }

    private companion object {
        const val STRONG_PASSWORD = "Cedar-Lantern_92!Orbit"
    }
}
