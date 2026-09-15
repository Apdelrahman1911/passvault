package com.passvault.android.picker

import android.os.Looper

/**
 * Owns an Activity and its complete launcher set on one thread. Registration is
 * performed before publication, so callers can never observe a partial set.
 */
internal class AndroidPickerHostState<Host : Any, Launchers : Any>(
    private val description: String,
    private val isFinishing: (Host) -> Boolean,
    private val assertOwnerThread: () -> Unit,
) {
    private var attachedHost: Host? = null
    private var launchers: Launchers? = null

    fun attach(host: Host, registerLaunchers: () -> Launchers) {
        assertOwnerThread()
        if (attachedHost === host) return
        val currentHost = attachedHost
        check(currentHost == null || isFinishing(currentHost)) {
            "Another activity is already attached to the $description"
        }

        // registerForActivityResult is a framework call and must not run under
        // the pending-request monitor. Main-thread confinement makes this
        // registration and the following complete publication indivisible.
        val registeredLaunchers = registerLaunchers()
        attachedHost = host
        launchers = registeredLaunchers
    }

    fun detach(host: Host, isChangingConfigurations: Boolean): AndroidPickerHostDetachDecision {
        assertOwnerThread()
        if (attachedHost !== host) return AndroidPickerHostDetachDecision.NOT_ATTACHED
        attachedHost = null
        launchers = null
        return AndroidPickerHostDetachDecision(
            detached = true,
            cancelPending = !isChangingConfigurations,
        )
    }

    fun launchersOrNull(): Launchers? {
        assertOwnerThread()
        return launchers
    }
}

internal data class AndroidPickerHostDetachDecision(
    val detached: Boolean,
    val cancelPending: Boolean,
) {
    companion object {
        val NOT_ATTACHED = AndroidPickerHostDetachDecision(
            detached = false,
            cancelPending = false,
        )
    }
}

internal fun assertAndroidMainThread() {
    check(Looper.myLooper() === Looper.getMainLooper()) {
        "Android picker state must be accessed on the main thread"
    }
}
