package com.passvault.shared.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.model.TotpConfiguration
import com.passvault.core.domain.repository.CredentialTotpRepository
import com.passvault.core.otp.StandardTotpService
import com.passvault.core.otp.TotpService
import com.passvault.core.security.EntrySensitiveStateOwner
import com.passvault.core.security.VaultUiSecurityCoordinator
import com.passvault.core.testing.TestData
import com.passvault.core.testing.fakes.FakeCredentialRepository
import com.passvault.feature.vault.presentation.TwoFactorCodesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EntrySensitiveStateRegistrationTest {
    @Test
    fun `view model store teardown unregisters entry lock cleanup`() {
        val coordinator = VaultUiSecurityCoordinator()
        val store = ViewModelStore()
        val factory = viewModelFactory {
            initializer {
                RecordingViewModel().attachToLockSensitiveStateRegistry(coordinator)
            }
        }
        val viewModel = ViewModelProvider.create(store, factory)[RecordingViewModel::class]

        coordinator.clearEntrySensitiveStateForLock()
        assertEquals(1, viewModel.clearCount)

        store.clear()
        coordinator.clearEntrySensitiveStateForLock()
        assertEquals(2, viewModel.clearCount)
        assertEquals(1, viewModel.onClearedCount)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `two factor factory registers synchronous seed cleanup`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        val coordinator = VaultUiSecurityCoordinator()
        val repository = FakeCredentialRepository().apply {
            setupCredentials(
                TestData.credential(id = "entry-totp").copy(
                    totp = TotpConfiguration(
                        secret = SensitiveText.from(TEST_TOTP_SECRET),
                        issuer = "Example",
                        accountName = "entry@example.com",
                    ),
                ),
            )
        }
        val koinApplication = koinApplication {
            modules(
                module {
                    single<VaultUiSecurityCoordinator> { coordinator }
                    single<CredentialTotpRepository> { repository }
                    single<TotpService> { StandardTotpService() }
                },
                AppModule.featureModule,
            )
        }
        val viewModel = koinApplication.koin.get<TwoFactorCodesViewModel>()

        try {
            viewModel.onEvent(TwoFactorCodesViewModel.TwoFactorCodesEvent.OnScreenVisible)
            runCurrent()
            val ownedInput = repository.getLastTotpInputsForTest().single()
            assertEquals(TEST_TOTP_SECRET, ownedInput.configuration.secret.toStringUnsafe())

            coordinator.clearEntrySensitiveStateForLock()

            assertTrue(viewModel.state.value.items.isEmpty())
            assertTrue(ownedInput.configuration.secret.toStringUnsafe().all { it == '\u0000' })
        } finally {
            viewModel.clearForLock()
            repository.reset()
            koinApplication.close()
            runCurrent()
            Dispatchers.resetMain()
        }
    }

    private class RecordingViewModel : ViewModel(), EntrySensitiveStateOwner {
        var clearCount = 0
        var onClearedCount = 0

        override fun clearForLock() {
            clearCount++
        }

        override fun onCleared() {
            clearForLock()
            onClearedCount++
        }
    }

    private companion object {
        const val TEST_TOTP_SECRET = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ"
    }
}
