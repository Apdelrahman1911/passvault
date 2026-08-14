package com.passvault.desktop.security.biometric

import com.passvault.core.security.BiometricAvailability
import com.passvault.core.security.BiometricCapability
import com.passvault.core.security.BiometricKeyStoreException
import com.passvault.core.security.BiometricType
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import java.util.ArrayDeque
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DesktopBiometricKeyStoreTest {
    @Test
    fun `vault identifiers cross the bridge only as fixed hashes`() = runTest {
        val bridge = RecordingBridge()
        val store = DesktopBiometricKeyStore(bridge)

        assertFalse(store.contains("vault-identifier"))

        assertEquals(32, bridge.lastVaultHash?.size)
        assertFalse(bridge.lastVaultHash!!.contentEquals("vault-identifier".encodeToByteArray()))
    }

    @Test
    fun `retrieved key is transferred only after a successful bridge call`() = runTest {
        val expected = ByteArray(32) { index -> (index + 1).toByte() }
        val bridge = RecordingBridge(retrievedKey = expected)
        val store = DesktopBiometricKeyStore(bridge)

        val result = store.retrieve("vault")

        assertTrue(result.isSuccess)
        assertContentEquals(expected, result.getOrThrow())
    }

    @Test
    fun `bridge invalidation maps to the common fail closed error`() = runTest {
        val bridge = RecordingBridge(retrieveFailure = DesktopBiometricBridgeException.Invalidated)
        val store = DesktopBiometricKeyStore(bridge)

        val result = store.retrieve("vault")

        assertIs<BiometricKeyStoreException.Invalidated>(result.exceptionOrNull())
    }

    @Test
    fun `high volume concurrent operations fail fast without a prompt backlog`() = runTest {
        val firstEntered = CompletableDeferred<Unit>()
        val releaseFirst = CompletableDeferred<Unit>()
        val bridge = RecordingBridge(
            onContains = {
                firstEntered.complete(Unit)
                releaseFirst.await()
            },
        )
        val store = DesktopBiometricKeyStore(bridge)

        val first = async { store.contains("vault") }
        firstEntered.await()
        val duplicates = List(DUPLICATE_OPERATION_COUNT) {
            async { runCatching { store.contains("vault") } }
        }
        runCurrent()

        assertEquals(1, bridge.containsCalls)
        duplicates.forEach { duplicate ->
            assertIs<DesktopBiometricBridgeException.Busy>(duplicate.await().exceptionOrNull())
        }

        releaseFirst.complete(Unit)
        first.await()
        assertEquals(1, bridge.containsCalls)
        assertEquals(1, bridge.maximumConcurrentCalls)
    }

    @Test
    fun `unavailable desktop runtime retains platform specific naming`() = runTest {
        val runtime = DesktopBiometricRuntime.create(com.passvault.desktop.OperatingSystem.WINDOWS) {
            throw IllegalStateException("missing bridge")
        }

        val capability = runtime.keyStore.getCapability()

        assertEquals(BiometricType.WINDOWS_HELLO, capability.type)
        assertEquals(BiometricAvailability.UNAVAILABLE, capability.availability)
    }

    @Test
    fun `prompt coordination is active only for the native prompt lifetime`() {
        val coordinator = DesktopBiometricPromptCoordinator()
        var completions = 0
        coordinator.setFinishedListener { completions += 1 }

        assertFailsWith<IllegalStateException> {
            coordinator.withPrompt {
                assertTrue(coordinator.isActive)
                coordinator.withPrompt { error("must not start") }
            }
        }

        assertFalse(coordinator.isActive)
        assertEquals(1, completions)
    }

    @Test
    fun `cancelling retrieval cancels native prompt waits for cleanup and wipes a late key`() = runTest {
        val bridge = CancellationBridge()
        val store = DesktopBiometricKeyStore(bridge)

        val retrieval = async { store.retrieve("vault") }
        yield()
        assertTrue(bridge.entered.await(5, TimeUnit.SECONDS))

        retrieval.cancel()
        yield()
        assertTrue(bridge.cancelled.await(5, TimeUnit.SECONDS))
        retrieval.cancelAndJoin()

        assertEquals(1, bridge.cancelCalls)
        assertTrue(bridge.producedKey?.all { byte -> byte == 0.toByte() } == true)
    }

    @Test
    fun `late cancellation cannot poison the next prompt`() = runTest {
        val bridge = CancellationBridge()
        val store = DesktopBiometricKeyStore(bridge)

        val first = async { store.retrieve("vault") }
        yield()
        assertTrue(bridge.entered.await(5, TimeUnit.SECONDS))
        first.cancel()
        first.cancelAndJoin()

        val second = store.retrieve("vault")

        assertTrue(second.isSuccess)
        assertEquals(2, bridge.retrieveCalls)
        second.getOrThrow().fill(0)
    }

    @Test
    fun `cancellation before the blocking worker starts prevents a prompt and does not poison retry`() = runTest {
        val bridge = RecordingBridge()
        val workerDispatcher = QueuedCoroutineDispatcher()
        val store = DesktopBiometricKeyStore(
            bridge = bridge,
            blockingDispatcher = workerDispatcher,
        )

        val first = async { store.retrieve("vault") }
        runCurrent()
        first.cancel()
        runCurrent()
        workerDispatcher.runAll()
        runCurrent()
        first.cancelAndJoin()

        assertEquals(1, bridge.cancelCalls)
        assertEquals(0, bridge.retrieveCalls)

        val retry = async { store.retrieve("vault") }
        runCurrent()
        workerDispatcher.runAll()
        runCurrent()
        val retryResult = retry.await()

        assertTrue(retryResult.isSuccess)
        assertEquals(1, bridge.retrieveCalls)
        retryResult.getOrThrow().fill(0)
    }

    private companion object {
        const val DUPLICATE_OPERATION_COUNT = 1_000
    }
}

private class QueuedCoroutineDispatcher : CoroutineDispatcher() {
    private val tasks = ArrayDeque<Runnable>()

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        tasks.addLast(block)
    }

    fun runAll() {
        while (tasks.isNotEmpty()) tasks.removeFirst().run()
    }
}

private class CancellationBridge : DesktopBiometricBridge {
    override val type = BiometricType.TOUCH_ID
    val entered = CountDownLatch(1)
    val cancelled = CountDownLatch(1)
    var cancelCalls = 0
    var retrieveCalls = 0
    var producedKey: ByteArray? = null
    private var cancellationPending = false

    override fun getCapability() = BiometricCapability(type, BiometricAvailability.AVAILABLE)
    override fun attachWindow(nativeHandle: Long) = Unit
    override fun contains(vaultHash: ByteArray) = true
    override fun enroll(vaultHash: ByteArray, vaultKey: ByteArray) = Unit

    override fun retrieve(vaultHash: ByteArray): ByteArray {
        retrieveCalls += 1
        if (cancellationPending) throw DesktopBiometricBridgeException.Cancelled
        if (retrieveCalls > 1) return ByteArray(32) { 3 }
        entered.countDown()
        check(cancelled.await(5, TimeUnit.SECONDS))
        return ByteArray(32) { 7 }.also { producedKey = it }
    }

    override fun delete(vaultHash: ByteArray) = Unit

    override fun cancelActive() {
        cancelPendingOrActive()
    }

    override fun cancelPendingOrActive() {
        cancelCalls += 1
        cancellationPending = true
        cancelled.countDown()
    }

    override fun clearPendingCancellation() {
        cancellationPending = false
    }

    override fun close() = Unit
}

private class RecordingBridge(
    private val retrievedKey: ByteArray = ByteArray(32),
    private val retrieveFailure: DesktopBiometricBridgeException? = null,
    private val onContains: suspend () -> Unit = {},
) : DesktopBiometricBridge {
    override val type: BiometricType = BiometricType.TOUCH_ID
    var lastVaultHash: ByteArray? = null
    var containsCalls = 0
    var retrieveCalls = 0
    var cancelCalls = 0
    var maximumConcurrentCalls = 0
    private var concurrentCalls = 0

    override fun getCapability(): BiometricCapability =
        BiometricCapability(type, BiometricAvailability.AVAILABLE)

    override fun attachWindow(nativeHandle: Long) = Unit

    override fun contains(vaultHash: ByteArray): Boolean {
        lastVaultHash = vaultHash.copyOf()
        containsCalls += 1
        concurrentCalls += 1
        maximumConcurrentCalls = maxOf(maximumConcurrentCalls, concurrentCalls)
        try {
            kotlinx.coroutines.runBlocking { onContains() }
        } finally {
            concurrentCalls -= 1
        }
        return false
    }

    override fun enroll(vaultHash: ByteArray, vaultKey: ByteArray) = Unit

    override fun retrieve(vaultHash: ByteArray): ByteArray {
        retrieveCalls += 1
        retrieveFailure?.let { throw it }
        return retrievedKey.copyOf()
    }

    override fun delete(vaultHash: ByteArray) = Unit
    override fun cancelActive() {
        cancelCalls += 1
    }

    override fun close() = Unit
}
