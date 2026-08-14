package com.passvault.desktop.security.biometric

import com.passvault.core.security.BiometricAvailability
import com.passvault.core.security.BiometricCapability
import com.passvault.core.security.BiometricPromptController
import com.passvault.core.security.BiometricType
import java.awt.Window

internal interface DesktopBiometricBridge : AutoCloseable {
    val type: BiometricType

    fun getCapability(): BiometricCapability
    fun attachWindow(nativeHandle: Long)
    fun contains(vaultHash: ByteArray): Boolean
    fun enroll(vaultHash: ByteArray, vaultKey: ByteArray)
    fun retrieve(vaultHash: ByteArray): ByteArray
    fun delete(vaultHash: ByteArray)
    fun cancelActive()

    /**
     * Cancels the operation currently entering or executing the native bridge.
     * Unlike [cancelActive], this may arm a one-shot cancellation before the
     * native call has received its operation identifier.
     */
    fun cancelPendingOrActive() = cancelActive()

    /** Clears a pre-entry cancellation after the owning prompt has settled. */
    fun clearPendingCancellation() = Unit
}

class DesktopBiometricHost internal constructor(
    private val bridge: DesktopBiometricBridge?,
    private val promptCoordinator: DesktopBiometricPromptCoordinator = DesktopBiometricPromptCoordinator(),
    private val nativeWindowHandle: (Window) -> Long = ::awtNativeWindowHandle,
) : AutoCloseable, BiometricPromptController {
    @Volatile
    private var attachedWindow: Window? = null

    fun attach(window: Window) {
        if (attachedWindow === window) return
        detach()
        val handle = runCatching { nativeWindowHandle(window) }.getOrNull() ?: return
        if (runCatching { bridge?.attachWindow(handle) }.isSuccess) attachedWindow = window
    }

    fun detach(window: Window? = null) {
        if (window != null && attachedWindow !== window) return
        cancelPromptIfActive()
        runCatching { bridge?.attachWindow(0L) }
        attachedWindow = null
    }

    override fun cancelActive() {
        cancelPromptIfActive()
    }

    val isPromptActive: Boolean
        get() = promptCoordinator.isActive

    fun setPromptFinishedListener(listener: (() -> Unit)?) {
        promptCoordinator.setFinishedListener(listener)
    }

    override fun close() {
        promptCoordinator.setFinishedListener(null)
        detach()
        runCatching { bridge?.close() }
    }

    private fun cancelPromptIfActive() {
        runCatching {
            if (promptCoordinator.isActive) {
                bridge?.cancelPendingOrActive()
            } else {
                bridge?.cancelActive()
            }
        }
    }
}

private fun awtNativeWindowHandle(window: Window): Long {
    check(window.isDisplayable) { "Desktop window must have a native peer" }
    return com.sun.jna.Pointer.nativeValue(com.sun.jna.Native.getWindowPointer(window))
}

internal fun nativeAvailability(value: Int): BiometricAvailability = when (value) {
    NativeBiometricAvailability.AVAILABLE -> BiometricAvailability.AVAILABLE
    NativeBiometricAvailability.NOT_ENROLLED -> BiometricAvailability.NOT_ENROLLED
    NativeBiometricAvailability.LOCKED_OUT -> BiometricAvailability.LOCKED_OUT
    NativeBiometricAvailability.UNAVAILABLE -> BiometricAvailability.UNAVAILABLE
    else -> throw DesktopBiometricBridgeException.Internal
}

internal object NativeBiometricAvailability {
    const val AVAILABLE = 0
    const val NOT_ENROLLED = 1
    const val LOCKED_OUT = 2
    const val UNAVAILABLE = 3
}

internal sealed class DesktopBiometricBridgeException(message: String) : Exception(message) {
    data object Cancelled : DesktopBiometricBridgeException("Desktop biometric operation was cancelled")
    data object NotAvailable : DesktopBiometricBridgeException("Desktop biometric operation is unavailable")
    data object NotEnrolled : DesktopBiometricBridgeException("Desktop biometric authentication is not enrolled")
    data object LockedOut : DesktopBiometricBridgeException("Desktop biometric authentication is locked")
    data object NotEnabled : DesktopBiometricBridgeException("Desktop biometric unlock is not enabled")
    data object Invalidated : DesktopBiometricBridgeException("Desktop biometric enrollment is invalid")
    data object AuthenticationFailed : DesktopBiometricBridgeException("Desktop authentication failed")
    data object Busy : DesktopBiometricBridgeException("A Desktop biometric operation is already active")
    data object Internal : DesktopBiometricBridgeException("Desktop biometric operation failed")
}
