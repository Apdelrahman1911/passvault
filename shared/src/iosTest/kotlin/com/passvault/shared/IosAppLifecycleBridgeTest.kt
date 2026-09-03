package com.passvault.shared

import com.passvault.core.domain.model.VaultSessionState
import com.passvault.core.domain.repository.LockReason
import com.passvault.core.domain.repository.VaultRepository
import com.passvault.core.security.ClipboardService
import com.passvault.core.security.VaultUiSecurityCoordinator
import com.passvault.core.testing.fakes.FakeVaultRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class IosAppLifecycleBridgeTest {
    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `resigning active preserves an owned sensitive clipboard`() = runTest {
        val clipboard = LifecycleRecordingClipboardService()
        startLifecycleKoin(backgroundScope, clipboard)

        IosAppLifecycleBridge().applicationWillResignActive()
        runCurrent()

        assertEquals(0, clipboard.clearCalls)
    }

    @Test
    fun `background lock preserves an owned sensitive clipboard`() = runTest {
        val clipboard = LifecycleRecordingClipboardService()
        val repository = FakeVaultRepository()
        val coordinator = VaultUiSecurityCoordinator()
        startLifecycleKoin(backgroundScope, clipboard, repository, coordinator)

        IosAppLifecycleBridge().applicationDidEnterBackground()
        runCurrent()
        coordinator.acknowledge(coordinator.requestedEpoch.value)
        runCurrent()

        assertEquals(VaultSessionState.Locked(LockReason.Background), repository.currentSessionState)
        assertEquals(0, clipboard.clearCalls)
    }

    private fun startLifecycleKoin(
        applicationScope: CoroutineScope,
        clipboard: ClipboardService,
        repository: VaultRepository = FakeVaultRepository(),
        coordinator: VaultUiSecurityCoordinator = VaultUiSecurityCoordinator(),
    ) {
        startKoin {
            modules(
                module {
                    single<CoroutineScope> { applicationScope }
                    single<ClipboardService> { clipboard }
                    single<VaultRepository> { repository }
                    single<VaultUiSecurityCoordinator> { coordinator }
                },
            )
        }
    }
}

private class LifecycleRecordingClipboardService : ClipboardService {
    var clearCalls = 0
        private set

    override suspend fun copySensitive(text: String, timeoutMs: Long) = Unit

    override suspend fun copy(text: String) = Unit

    override suspend fun clear() {
        clearCalls++
    }

    override suspend fun containsSensitive(): Boolean = clearCalls == 0
}
