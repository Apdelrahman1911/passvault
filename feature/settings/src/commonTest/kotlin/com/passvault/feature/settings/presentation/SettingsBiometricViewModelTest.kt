package com.passvault.feature.settings.presentation

import com.passvault.core.domain.repository.AppSettings
import com.passvault.core.domain.repository.AppSettingsStore
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
                type = BiometricType.FACE,
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
        assertEquals(BiometricType.FACE, viewModel.state.value.biometricType)
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

    private fun createViewModel(): SettingsViewModel = SettingsViewModel(
        vaultRepository = repository,
        appSettingsStore = InMemoryAppSettingsStore(),
        biometricUnlockService = biometricService,
    )

    private class InMemoryAppSettingsStore : AppSettingsStore {
        private var settings = AppSettings()

        override suspend fun load(): Result<AppSettings> = Result.success(settings)

        override suspend fun save(settings: AppSettings): Result<Unit> {
            this.settings = settings
            return Result.success(Unit)
        }
    }
}
