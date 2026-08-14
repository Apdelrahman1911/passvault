package com.passvault.desktop.security.biometric

import com.passvault.core.security.BiometricCapability
import com.passvault.core.security.BiometricType
import com.sun.jna.IntegerType
import com.sun.jna.Library
import com.sun.jna.Memory
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.PointerByReference
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.TimeUnit
import kotlin.concurrent.withLock

internal class JnaDesktopBiometricBridge private constructor(
    override val type: BiometricType,
    private val native: NativeApi,
    private val context: Pointer,
    private val closeWaitNanos: Long,
) : DesktopBiometricBridge {
    private val nextOperationId = AtomicLong(1L)
    private val activeOperationId = AtomicLong(NO_OPERATION)
    private val cancelPendingOperation = AtomicBoolean(false)
    private val closed = AtomicBoolean(false)
    private val lifecycleLock = ReentrantLock()
    private val noNativeCalls = lifecycleLock.newCondition()
    private var nativeCallsInFlight = 0
    private var nativeContextDestroyClaimed = false

    override fun getCapability(): BiometricCapability {
        return withNativeCall {
            val availability = IntByReference()
            native.pv_bio_get_capability(context, availability).requireSuccess()
            BiometricCapability(type, nativeAvailability(availability.value))
        }
    }

    override fun attachWindow(nativeHandle: Long) {
        withNativeCall {
            native.pv_bio_set_parent_window(context, Pointer(nativeHandle)).requireSuccess()
        }
    }

    override fun contains(vaultHash: ByteArray): Boolean {
        ensureVaultHash(vaultHash)
        return withNativeCall {
            val present = IntByReference()
            withNativeBytes(vaultHash) { hash ->
                native.pv_bio_contains(context, hash, SizeT(vaultHash.size.toLong()), present).requireSuccess()
            }
            present.value == 1
        }
    }

    override fun enroll(vaultHash: ByteArray, vaultKey: ByteArray) {
        ensureVaultHash(vaultHash)
        require(vaultKey.size == VAULT_KEY_BYTES) { "Vault keys must be exactly 32 bytes" }
        withOperation { operationId ->
            withNativeBytes(vaultHash) { hash ->
                withNativeBytes(vaultKey) { key ->
                    native.pv_bio_enroll(
                        context,
                        operationId,
                        hash,
                        SizeT(vaultHash.size.toLong()),
                        key,
                        SizeT(vaultKey.size.toLong()),
                    ).requireSuccess()
                }
            }
        }
    }

    override fun retrieve(vaultHash: ByteArray): ByteArray {
        ensureVaultHash(vaultHash)
        val output = Memory(VAULT_KEY_BYTES.toLong())
        output.clear()
        return try {
            withOperation { operationId ->
                withNativeBytes(vaultHash) { hash ->
                    native.pv_bio_retrieve(
                        context,
                        operationId,
                        hash,
                        SizeT(vaultHash.size.toLong()),
                        output,
                        SizeT(VAULT_KEY_BYTES.toLong()),
                    ).requireSuccess()
                }
                output.getByteArray(0, VAULT_KEY_BYTES)
            }
        } finally {
            output.clear()
            output.close()
        }
    }

    override fun delete(vaultHash: ByteArray) {
        ensureVaultHash(vaultHash)
        withNativeCall {
            withNativeBytes(vaultHash) { hash ->
                native.pv_bio_delete(context, hash, SizeT(vaultHash.size.toLong())).requireSuccess()
            }
        }
    }

    override fun cancelActive() {
        val operationId = activeOperationId.get()
        if (operationId != NO_OPERATION && !closed.get()) {
            withNativeCall {
                native.pv_bio_cancel(context, operationId).requireSuccess(allowCancelled = true)
            }
        }
    }

    override fun cancelPendingOrActive() {
        if (closed.get()) return
        cancelPendingOperation.set(true)
        cancelActive()
    }

    override fun clearPendingCancellation() {
        cancelPendingOperation.set(false)
    }

    override fun close() {
        val shouldClose = lifecycleLock.withLock {
            if (closed.get()) false else {
                closed.set(true)
                cancelPendingOperation.set(true)
                true
            }
        }
        if (shouldClose) {
            val operationId = activeOperationId.get()
            if (operationId != NO_OPERATION) {
                runCatching { native.pv_bio_cancel(context, operationId) }
            }
            var interrupted = false
            var remainingNanos = closeWaitNanos
            val destroyNow = lifecycleLock.withLock {
                while (nativeCallsInFlight != 0 && remainingNanos > 0L) {
                    try {
                        remainingNanos = noNativeCalls.awaitNanos(remainingNanos)
                    } catch (_: InterruptedException) {
                        interrupted = true
                        break
                    }
                }
                claimNativeContextDestroyLocked()
            }
            if (destroyNow) native.pv_bio_destroy(context)
            if (interrupted) Thread.currentThread().interrupt()
        }
    }

    private inline fun <T> withOperation(block: (Long) -> T): T {
        ensureOpen()
        val operationId = nextOperationId.getAndUpdate { current ->
            if (current == Long.MAX_VALUE) 1L else current + 1L
        }
        if (!activeOperationId.compareAndSet(NO_OPERATION, operationId)) {
            throw DesktopBiometricBridgeException.Busy
        }
        return try {
            if (cancelPendingOperation.getAndSet(false)) {
                throw DesktopBiometricBridgeException.Cancelled
            }
            withNativeCall { block(operationId) }
        } finally {
            activeOperationId.compareAndSet(operationId, NO_OPERATION)
            destroyNativeContextIfReady()
        }
    }

    private fun ensureOpen() {
        if (closed.get()) throw DesktopBiometricBridgeException.NotAvailable
    }

    private inline fun <T> withNativeCall(block: () -> T): T {
        lifecycleLock.withLock {
            ensureOpen()
            nativeCallsInFlight += 1
        }
        return try {
            block()
        } finally {
            val destroyNow = lifecycleLock.withLock {
                nativeCallsInFlight -= 1
                check(nativeCallsInFlight >= 0) { "Desktop biometric native-call accounting underflow" }
                if (nativeCallsInFlight == 0) noNativeCalls.signalAll()
                claimNativeContextDestroyLocked()
            }
            if (destroyNow) native.pv_bio_destroy(context)
        }
    }

    /**
     * A timed-out close never frees a context still used by native code. The
     * last returning call claims destruction instead. If an OS API never
     * returns, the terminal Desktop process deadline reclaims the context with
     * the process and avoids both an unbounded close and a use-after-free.
     */
    private fun destroyNativeContextIfReady() {
        val destroyNow = lifecycleLock.withLock { claimNativeContextDestroyLocked() }
        if (destroyNow) native.pv_bio_destroy(context)
    }

    private fun claimNativeContextDestroyLocked(): Boolean {
        val ready = closed.get() &&
            nativeCallsInFlight == 0 &&
            activeOperationId.get() == NO_OPERATION &&
            !nativeContextDestroyClaimed
        if (ready) nativeContextDestroyClaimed = true
        return ready
    }

    internal companion object {
        fun create(
            type: BiometricType,
            native: NativeApi,
            dataDirectory: String,
            closeWaitMillis: Long = DEFAULT_CLOSE_WAIT_MILLIS,
        ): JnaDesktopBiometricBridge {
            require(closeWaitMillis > 0L) { "Desktop biometric close wait must be positive" }
            check(native.pv_bio_abi_version() == EXPECTED_ABI) {
                "Desktop biometric bridge ABI does not match"
            }
            val encodedDirectory = dataDirectory.toByteArray(StandardCharsets.UTF_8)
            require(encodedDirectory.isNotEmpty() && encodedDirectory.size <= MAX_DIRECTORY_BYTES) {
                "Desktop biometric data directory is invalid"
            }
            require(encodedDirectory.none { it == 0.toByte() }) {
                "Desktop biometric data directory contains a NUL"
            }
            val directory = Memory(encodedDirectory.size.toLong())
            val context = PointerByReference()
            return try {
                directory.write(0, encodedDirectory, 0, encodedDirectory.size)
                native.pv_bio_create(
                    directory,
                    SizeT(encodedDirectory.size.toLong()),
                    context,
                ).requireSuccess()
                val pointer = context.value ?: throw DesktopBiometricBridgeException.Internal
                JnaDesktopBiometricBridge(
                    type = type,
                    native = native,
                    context = pointer,
                    closeWaitNanos = TimeUnit.MILLISECONDS.toNanos(closeWaitMillis),
                )
            } finally {
                directory.clear()
                directory.close()
                encodedDirectory.fill(0)
            }
        }

        const val EXPECTED_ABI = 1
        private const val VAULT_HASH_BYTES = 32
        private const val VAULT_KEY_BYTES = 32
        private const val MAX_DIRECTORY_BYTES = 4096
        private const val NO_OPERATION = 0L
        private const val DEFAULT_CLOSE_WAIT_MILLIS = 1_500L

        private fun ensureVaultHash(hash: ByteArray) {
            require(hash.size == VAULT_HASH_BYTES) { "Vault hashes must be exactly 32 bytes" }
        }

        private inline fun <T> withNativeBytes(value: ByteArray, block: (Memory) -> T): T {
            val memory = Memory(value.size.toLong())
            return try {
                memory.write(0, value, 0, value.size)
                block(memory)
            } finally {
                memory.clear()
                memory.close()
            }
        }
    }
}

/** C ABI names are fixed by the reviewed native bridge header. */
@Suppress("FunctionNaming")
internal interface NativeApi : Library {
    fun pv_bio_abi_version(): Int
    fun pv_bio_create(dataDirectory: Pointer, length: SizeT, outContext: PointerByReference): Int
    fun pv_bio_destroy(context: Pointer)
    fun pv_bio_set_parent_window(context: Pointer, nativeWindow: Pointer?): Int
    fun pv_bio_get_capability(context: Pointer, outAvailability: IntByReference): Int
    fun pv_bio_contains(context: Pointer, vaultHash: Pointer, hashLength: SizeT, outContains: IntByReference): Int
    fun pv_bio_enroll(
        context: Pointer,
        operationId: Long,
        vaultHash: Pointer,
        hashLength: SizeT,
        vaultKey: Pointer,
        keyLength: SizeT,
    ): Int
    fun pv_bio_retrieve(
        context: Pointer,
        operationId: Long,
        vaultHash: Pointer,
        hashLength: SizeT,
        outVaultKey: Pointer,
        outLength: SizeT,
    ): Int
    fun pv_bio_delete(context: Pointer, vaultHash: Pointer, hashLength: SizeT): Int
    fun pv_bio_cancel(context: Pointer, operationId: Long): Int
}

internal class SizeT(value: Long = 0L) : IntegerType(Native.SIZE_T_SIZE, value, true) {
    override fun toByte(): Byte = toLong().toByte()
    override fun toShort(): Short = toLong().toShort()
}

private fun Int.requireSuccess(allowCancelled: Boolean = false) {
    val failure = when (this) {
        NativeBiometricStatus.OK -> null
        NativeBiometricStatus.CANCELLED -> if (allowCancelled) null else DesktopBiometricBridgeException.Cancelled
        NativeBiometricStatus.NOT_AVAILABLE -> DesktopBiometricBridgeException.NotAvailable
        NativeBiometricStatus.NOT_ENROLLED -> DesktopBiometricBridgeException.NotEnrolled
        NativeBiometricStatus.LOCKED_OUT -> DesktopBiometricBridgeException.LockedOut
        NativeBiometricStatus.NOT_ENABLED -> DesktopBiometricBridgeException.NotEnabled
        NativeBiometricStatus.INVALIDATED -> DesktopBiometricBridgeException.Invalidated
        NativeBiometricStatus.AUTHENTICATION_FAILED -> DesktopBiometricBridgeException.AuthenticationFailed
        NativeBiometricStatus.BUSY -> DesktopBiometricBridgeException.Busy
        NativeBiometricStatus.INTERNAL_ERROR,
        NativeBiometricStatus.BUFFER_TOO_SMALL,
        -> DesktopBiometricBridgeException.Internal
        else -> DesktopBiometricBridgeException.Internal
    }
    if (failure != null) throw failure
}

private object NativeBiometricStatus {
    const val OK = 0
    const val CANCELLED = 1
    const val NOT_AVAILABLE = 2
    const val NOT_ENROLLED = 3
    const val LOCKED_OUT = 4
    const val NOT_ENABLED = 5
    const val INVALIDATED = 6
    const val AUTHENTICATION_FAILED = 7
    const val BUSY = 8
    const val INTERNAL_ERROR = 9
    const val BUFFER_TOO_SMALL = 10
}
