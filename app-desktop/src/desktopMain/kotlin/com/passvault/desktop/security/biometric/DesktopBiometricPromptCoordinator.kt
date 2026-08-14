package com.passvault.desktop.security.biometric

import java.util.concurrent.atomic.AtomicBoolean

internal class DesktopBiometricPromptCoordinator {
    private val active = AtomicBoolean(false)

    @Volatile
    private var finishedListener: (() -> Unit)? = null

    val isActive: Boolean
        get() = active.get()

    fun <T> withPrompt(block: () -> T): T {
        check(active.compareAndSet(false, true)) { "A desktop biometric prompt is already active" }
        return try {
            block()
        } finally {
            active.set(false)
            runCatching { finishedListener?.invoke() }
        }
    }

    fun setFinishedListener(listener: (() -> Unit)?) {
        finishedListener = listener
    }
}
