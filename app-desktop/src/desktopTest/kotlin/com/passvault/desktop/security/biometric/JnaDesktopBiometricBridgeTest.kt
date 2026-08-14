package com.passvault.desktop.security.biometric

import com.passvault.core.security.BiometricType
import com.sun.jna.Pointer
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.PointerByReference
import java.nio.file.Files
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JnaDesktopBiometricBridgeTest {
    @Test
    fun `close cancels an active prompt and waits before destroying native context`() {
        val api = BlockingNativeApi()
        val dataDirectory = Files.createTempDirectory("passvault-jna-lifecycle-test")
        val bridge = JnaDesktopBiometricBridge.create(
            type = BiometricType.TOUCH_ID,
            native = api,
            dataDirectory = dataDirectory.toString(),
        )
        val executor = Executors.newFixedThreadPool(2)
        try {
            val retrieval = executor.submit {
                runCatching { bridge.retrieve(ByteArray(32)) }
            }
            assertTrue(api.retrieveEntered.await(5, TimeUnit.SECONDS))

            val close = executor.submit { bridge.close() }
            assertTrue(api.cancelCalled.await(5, TimeUnit.SECONDS))
            assertFalse(api.destroyedBeforeRetrieveFinished)
            close.get(5, TimeUnit.SECONDS)
            retrieval.get(5, TimeUnit.SECONDS)

            assertTrue(api.destroyCalled)
            assertFalse(api.destroyedBeforeRetrieveFinished)
        } finally {
            executor.shutdownNow()
            dataDirectory.toFile().deleteRecursively()
        }
    }

    @Test
    fun `pending cancellation is consumed before the next native prompt starts`() {
        val api = ImmediateNativeApi()
        val dataDirectory = Files.createTempDirectory("passvault-jna-pending-cancel-test")
        val bridge = JnaDesktopBiometricBridge.create(
            type = BiometricType.TOUCH_ID,
            native = api,
            dataDirectory = dataDirectory.toString(),
        )
        try {
            bridge.cancelPendingOrActive()

            assertFailsWith<DesktopBiometricBridgeException.Cancelled> {
                bridge.retrieve(ByteArray(32))
            }
            assertTrue(api.retrieveCalls == 0)

            bridge.retrieve(ByteArray(32))
            assertTrue(api.retrieveCalls == 1)
        } finally {
            bridge.close()
            dataDirectory.toFile().deleteRecursively()
        }
    }

    @Test
    fun `close returns at its deadline and defers destruction when a native prompt ignores cancellation`() {
        val api = UnresponsiveNativeApi()
        val dataDirectory = Files.createTempDirectory("passvault-jna-timeout-test")
        val bridge = JnaDesktopBiometricBridge.create(
            type = BiometricType.TOUCH_ID,
            native = api,
            dataDirectory = dataDirectory.toString(),
            closeWaitMillis = 25L,
        )
        val executor = Executors.newSingleThreadExecutor()
        try {
            val retrieval = executor.submit { runCatching { bridge.retrieve(ByteArray(32)) } }
            assertTrue(api.retrieveEntered.await(5, TimeUnit.SECONDS))

            val startedAt = System.nanoTime()
            bridge.close()
            val elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt)

            assertTrue(elapsedMillis < 1_000L)
            assertEquals(1, api.cancelCalls)
            assertFalse(api.destroyCalled)

            api.releaseRetrieve.countDown()
            retrieval.get(5, TimeUnit.SECONDS)
            assertTrue(api.destroyCalled)
            assertFalse(api.destroyedBeforeRetrieveFinished)
        } finally {
            api.releaseRetrieve.countDown()
            executor.shutdownNow()
            dataDirectory.toFile().deleteRecursively()
        }
    }
}

private class ImmediateNativeApi : NativeApi {
    var retrieveCalls = 0

    override fun pv_bio_abi_version() = 1

    override fun pv_bio_create(dataDirectory: Pointer, length: SizeT, outContext: PointerByReference): Int {
        outContext.value = Pointer(1)
        return 0
    }

    override fun pv_bio_destroy(context: Pointer) = Unit
    override fun pv_bio_set_parent_window(context: Pointer, nativeWindow: Pointer?) = 0

    override fun pv_bio_get_capability(context: Pointer, outAvailability: IntByReference): Int {
        outAvailability.value = NativeBiometricAvailability.AVAILABLE
        return 0
    }

    override fun pv_bio_contains(
        context: Pointer,
        vaultHash: Pointer,
        hashLength: SizeT,
        outContains: IntByReference,
    ) = 0

    override fun pv_bio_enroll(
        context: Pointer,
        operationId: Long,
        vaultHash: Pointer,
        hashLength: SizeT,
        vaultKey: Pointer,
        keyLength: SizeT,
    ) = 0

    override fun pv_bio_retrieve(
        context: Pointer,
        operationId: Long,
        vaultHash: Pointer,
        hashLength: SizeT,
        outVaultKey: Pointer,
        outLength: SizeT,
    ): Int {
        retrieveCalls += 1
        return 0
    }

    override fun pv_bio_delete(context: Pointer, vaultHash: Pointer, hashLength: SizeT) = 0
    override fun pv_bio_cancel(context: Pointer, operationId: Long) = 0
}

private class BlockingNativeApi : NativeApi {
    val retrieveEntered = CountDownLatch(1)
    val cancelCalled = CountDownLatch(1)

    @Volatile
    var retrieveFinished = false

    @Volatile
    var destroyCalled = false

    @Volatile
    var destroyedBeforeRetrieveFinished = false

    override fun pv_bio_abi_version(): Int = 1

    override fun pv_bio_create(dataDirectory: Pointer, length: SizeT, outContext: PointerByReference): Int {
        outContext.value = Pointer(1)
        return 0
    }

    override fun pv_bio_destroy(context: Pointer) {
        destroyedBeforeRetrieveFinished = !retrieveFinished
        destroyCalled = true
    }

    override fun pv_bio_set_parent_window(context: Pointer, nativeWindow: Pointer?): Int = 0

    override fun pv_bio_get_capability(context: Pointer, outAvailability: IntByReference): Int {
        outAvailability.value = NativeBiometricAvailability.AVAILABLE
        return 0
    }

    override fun pv_bio_contains(
        context: Pointer,
        vaultHash: Pointer,
        hashLength: SizeT,
        outContains: IntByReference,
    ): Int = 0

    override fun pv_bio_enroll(
        context: Pointer,
        operationId: Long,
        vaultHash: Pointer,
        hashLength: SizeT,
        vaultKey: Pointer,
        keyLength: SizeT,
    ): Int = 0

    override fun pv_bio_retrieve(
        context: Pointer,
        operationId: Long,
        vaultHash: Pointer,
        hashLength: SizeT,
        outVaultKey: Pointer,
        outLength: SizeT,
    ): Int {
        retrieveEntered.countDown()
        cancelCalled.await(5, TimeUnit.SECONDS)
        retrieveFinished = true
        return 1
    }

    override fun pv_bio_delete(context: Pointer, vaultHash: Pointer, hashLength: SizeT): Int = 0

    override fun pv_bio_cancel(context: Pointer, operationId: Long): Int {
        cancelCalled.countDown()
        return 0
    }
}

private class UnresponsiveNativeApi : NativeApi {
    val retrieveEntered = CountDownLatch(1)
    val releaseRetrieve = CountDownLatch(1)

    @Volatile
    var retrieveFinished = false

    @Volatile
    var destroyCalled = false

    @Volatile
    var destroyedBeforeRetrieveFinished = false

    @Volatile
    var cancelCalls = 0

    override fun pv_bio_abi_version(): Int = 1

    override fun pv_bio_create(dataDirectory: Pointer, length: SizeT, outContext: PointerByReference): Int {
        outContext.value = Pointer(1)
        return 0
    }

    override fun pv_bio_destroy(context: Pointer) {
        destroyedBeforeRetrieveFinished = !retrieveFinished
        destroyCalled = true
    }

    override fun pv_bio_set_parent_window(context: Pointer, nativeWindow: Pointer?): Int = 0

    override fun pv_bio_get_capability(context: Pointer, outAvailability: IntByReference): Int {
        outAvailability.value = NativeBiometricAvailability.AVAILABLE
        return 0
    }

    override fun pv_bio_contains(
        context: Pointer,
        vaultHash: Pointer,
        hashLength: SizeT,
        outContains: IntByReference,
    ): Int = 0

    override fun pv_bio_enroll(
        context: Pointer,
        operationId: Long,
        vaultHash: Pointer,
        hashLength: SizeT,
        vaultKey: Pointer,
        keyLength: SizeT,
    ): Int = 0

    override fun pv_bio_retrieve(
        context: Pointer,
        operationId: Long,
        vaultHash: Pointer,
        hashLength: SizeT,
        outVaultKey: Pointer,
        outLength: SizeT,
    ): Int {
        retrieveEntered.countDown()
        releaseRetrieve.await(5, TimeUnit.SECONDS)
        retrieveFinished = true
        return 1
    }

    override fun pv_bio_delete(context: Pointer, vaultHash: Pointer, hashLength: SizeT): Int = 0

    override fun pv_bio_cancel(context: Pointer, operationId: Long): Int {
        cancelCalls += 1
        return 0
    }
}
