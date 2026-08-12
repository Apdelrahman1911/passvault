package com.passvault.feature.settings.presentation

import com.passvault.core.domain.repository.AppSettings
import com.passvault.core.domain.repository.AppSettingsStore
import com.passvault.core.domain.repository.ThemePreference
import com.passvault.core.testing.fakes.FakeBiometricUnlockService
import com.passvault.core.testing.fakes.FakeVaultRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsPreferencePersistenceTest {
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
    fun `lock cleanup does not cancel an in-flight preference save`() = runTest(dispatcher) {
        val repository = FakeVaultRepository().apply { setupExistingVault() }
        val store = BlockingAppSettingsStore()
        val viewModel = SettingsViewModel(
            vaultRepository = repository,
            appSettingsStore = store,
            biometricUnlockService = FakeBiometricUnlockService(),
        )
        runCurrent()

        viewModel.onEvent(
            SettingsViewModel.SettingsEvent.OnThemeChanged(SettingsViewModel.AppTheme.DARK),
        )
        runCurrent()
        assertTrue(store.saveStarted.isCompleted)

        viewModel.clearForLock()
        store.allowSave.complete(Unit)
        advanceUntilIdle()

        assertEquals(ThemePreference.DARK, store.saved?.theme)
        repository.reset()
    }

    private class BlockingAppSettingsStore : AppSettingsStore {
        val saveStarted = CompletableDeferred<Unit>()
        val allowSave = CompletableDeferred<Unit>()
        var saved: AppSettings? = null

        override suspend fun load(): Result<AppSettings> = Result.success(AppSettings())

        override suspend fun save(settings: AppSettings): Result<Unit> {
            saveStarted.complete(Unit)
            allowSave.await()
            saved = settings
            return Result.success(Unit)
        }
    }
}
