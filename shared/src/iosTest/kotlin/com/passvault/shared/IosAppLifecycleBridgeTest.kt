package com.passvault.shared

import com.passvault.core.domain.model.VaultSessionState
import com.passvault.core.domain.repository.LockReason
import com.passvault.core.domain.repository.VaultRepository
import com.passvault.core.security.ClipboardService
import com.passvault.core.security.VaultUiSecurityCoordinator
import com.passvault.core.testing.fakes.FakeVaultRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

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

    @Test
    fun `bounded lock failures expose recovery and an explicit retry can finish`() = runTest {
        val repository = SwitchableLockVaultRepository(failLocks = true)
        val coordinator = VaultUiSecurityCoordinator()
        startLifecycleKoin(
            applicationScope = this,
            clipboard = LifecycleRecordingClipboardService(),
            repository = repository,
            coordinator = coordinator,
        )
        val bridge = testBridge()
        var readyCalls = 0
        var recoveryCalls = 0

        bridge.applicationDidEnterBackground()
        bridge.applicationDidBecomeActive(
            onReady = { readyCalls++ },
            onRecoveryRequired = { recoveryCalls++ },
        )
        advanceUntilIdle()

        assertEquals(9, repository.lockCalls)
        assertEquals(0, readyCalls)
        assertEquals(1, recoveryCalls)

        repository.failLocks = false
        bridge.applicationDidBecomeActive(
            onReady = { readyCalls++ },
            onRecoveryRequired = { recoveryCalls++ },
        )
        runCurrent()
        coordinator.acknowledge(coordinator.requestedEpoch.value)
        runCurrent()

        assertEquals(10, repository.lockCalls)
        assertEquals(1, readyCalls)
        assertEquals(1, recoveryCalls)
    }

    @Test
    fun `acknowledgement timeouts reuse one locked epoch before exposing recovery`() = runTest {
        val repository = SwitchableLockVaultRepository()
        val coordinator = VaultUiSecurityCoordinator()
        startLifecycleKoin(
            applicationScope = this,
            clipboard = LifecycleRecordingClipboardService(),
            repository = repository,
            coordinator = coordinator,
        )
        val bridge = testBridge()
        var readyCalls = 0
        var recoveryCalls = 0

        bridge.applicationDidEnterBackground()
        bridge.applicationDidBecomeActive(
            onReady = { readyCalls++ },
            onRecoveryRequired = { recoveryCalls++ },
        )
        advanceUntilIdle()

        assertEquals(1, repository.lockCalls)
        assertEquals(1L, coordinator.requestedEpoch.value)
        assertEquals(0, readyCalls)
        assertEquals(1, recoveryCalls)
    }

    @Test
    fun `later acknowledgement retry invokes ready exactly once`() = runTest {
        val repository = SwitchableLockVaultRepository()
        val coordinator = VaultUiSecurityCoordinator()
        startLifecycleKoin(
            applicationScope = this,
            clipboard = LifecycleRecordingClipboardService(),
            repository = repository,
            coordinator = coordinator,
        )
        val bridge = testBridge()
        var readyCalls = 0
        var recoveryCalls = 0

        bridge.applicationDidEnterBackground()
        bridge.applicationDidBecomeActive(
            onReady = { readyCalls++ },
            onRecoveryRequired = { recoveryCalls++ },
        )
        runCurrent()
        val requestEpoch = coordinator.requestedEpoch.value

        advanceTimeBy(TEST_ACKNOWLEDGEMENT_TIMEOUT_MILLIS)
        runCurrent()
        advanceTimeBy(TEST_RETRY_DELAY_MILLIS)
        runCurrent()
        coordinator.acknowledge(requestEpoch)
        runCurrent()

        assertEquals(1, repository.lockCalls)
        assertEquals(1L, coordinator.requestedEpoch.value)
        assertEquals(1, readyCalls)
        assertEquals(0, recoveryCalls)
    }

    @Test
    fun `duplicate active observers cannot reset the acknowledgement retry budget`() = runTest {
        val repository = SwitchableLockVaultRepository()
        val coordinator = VaultUiSecurityCoordinator()
        val mainQueue = mutableListOf<() -> Unit>()
        startLifecycleKoin(
            applicationScope = this,
            clipboard = LifecycleRecordingClipboardService(),
            repository = repository,
            coordinator = coordinator,
        )
        val bridge = IosAppLifecycleBridge(
            dispatchToMain = mainQueue::add,
            acknowledgementTimeoutMillis = TEST_ACKNOWLEDGEMENT_TIMEOUT_MILLIS,
            retryPolicy = IosBackgroundCleanupRetryPolicy(
                maximumLockAttempts = 3,
                maximumAcknowledgementAttempts = 2,
                baseRetryDelayMillis = TEST_RETRY_DELAY_MILLIS,
            ),
        )
        var readyCalls = 0
        var recoveryCalls = 0

        bridge.applicationDidEnterBackground()
        bridge.applicationDidBecomeActive(
            onReady = { readyCalls++ },
            onRecoveryRequired = { recoveryCalls++ },
        )
        runCurrent()
        advanceTimeBy(TEST_ACKNOWLEDGEMENT_TIMEOUT_MILLIS)
        runCurrent()
        assertEquals(1, mainQueue.size)

        mainQueue.removeAt(0).invoke()
        advanceTimeBy(TEST_RETRY_DELAY_MILLIS)
        runCurrent()
        bridge.applicationDidBecomeActive(
            onReady = { readyCalls++ },
            onRecoveryRequired = { recoveryCalls++ },
        )
        runCurrent()
        advanceTimeBy(TEST_ACKNOWLEDGEMENT_TIMEOUT_MILLIS)
        runCurrent()
        assertEquals(2, mainQueue.size)

        // Run the later observer first. Its callback must use the episode's
        // existing budget rather than restarting at attempt one.
        mainQueue.removeAt(mainQueue.lastIndex).invoke()
        mainQueue.removeAt(0).invoke()

        assertEquals(1, repository.lockCalls)
        assertEquals(0, readyCalls)
        assertEquals(1, recoveryCalls)
    }

    @Test
    fun `unavailable replacement runtime exposes recovery instead of becoming silent`() = runTest {
        val repository = SwitchableLockVaultRepository(failLocks = true)
        startLifecycleKoin(
            applicationScope = this,
            clipboard = LifecycleRecordingClipboardService(),
            repository = repository,
        )
        val bridge = testBridge(maximumLockAttempts = 2)
        var readyCalls = 0
        var recoveryCalls = 0

        bridge.applicationDidEnterBackground()
        bridge.applicationDidBecomeActive(
            onReady = { readyCalls++ },
            onRecoveryRequired = { recoveryCalls++ },
        )
        runCurrent()
        stopKoin()
        advanceUntilIdle()

        assertEquals(3, repository.lockCalls)
        assertEquals(0, readyCalls)
        assertEquals(1, recoveryCalls)
    }

    @Test
    fun `lock retry policy uses increasing delays and a finite attempt budget`() {
        val policy = IosBackgroundCleanupRetryPolicy(
            maximumLockAttempts = 3,
            maximumAcknowledgementAttempts = 3,
            baseRetryDelayMillis = 25L,
        )

        val first = assertIs<IosBackgroundCleanupResolution.RetryLock>(
            policy.resolve(IosBackgroundCleanupOutcome.LockFailed, IosBackgroundCleanupRetryState()),
        )
        val second = assertIs<IosBackgroundCleanupResolution.RetryLock>(
            policy.resolve(IosBackgroundCleanupOutcome.LockFailed, first.retryState),
        )
        val terminal = policy.resolve(IosBackgroundCleanupOutcome.LockFailed, second.retryState)

        assertEquals(25L, first.delayMillis)
        assertEquals(50L, second.delayMillis)
        assertEquals(IosBackgroundCleanupResolution.RecoveryRequired, terminal)
    }

    private fun testBridge(
        maximumLockAttempts: Int = 3,
    ) = IosAppLifecycleBridge(
        dispatchToMain = { block -> block() },
        acknowledgementTimeoutMillis = TEST_ACKNOWLEDGEMENT_TIMEOUT_MILLIS,
        retryPolicy = IosBackgroundCleanupRetryPolicy(
            maximumLockAttempts = maximumLockAttempts,
            maximumAcknowledgementAttempts = 3,
            baseRetryDelayMillis = TEST_RETRY_DELAY_MILLIS,
        ),
    )

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

    private companion object {
        const val TEST_ACKNOWLEDGEMENT_TIMEOUT_MILLIS = 10L
        const val TEST_RETRY_DELAY_MILLIS = 1L
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

private class SwitchableLockVaultRepository(
    var failLocks: Boolean = false,
    private val delegate: FakeVaultRepository = FakeVaultRepository(),
) : VaultRepository by delegate {
    var lockCalls: Int = 0
        private set

    override suspend fun lock(reason: LockReason): Result<Unit> {
        lockCalls++
        return if (failLocks) {
            Result.failure(IllegalStateException("Injected lock failure"))
        } else {
            delegate.lock(reason)
        }
    }
}
