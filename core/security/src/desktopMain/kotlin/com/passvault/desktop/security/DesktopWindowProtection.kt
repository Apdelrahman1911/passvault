package com.passvault.desktop.security

import java.awt.Frame
import java.awt.Window
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.event.WindowStateListener
import javax.swing.JFrame
import javax.swing.Timer

/**
 * Desktop window lifecycle boundary.
 *
 * Desktop platforms do not expose portable screenshot prevention. This class
 * therefore owns only behavior it can guarantee: locking on minimize or real
 * focus loss, minimizing locked content, and rearming after an unlock.
 */
class DesktopWindowProtection {
    private var frame: JFrame? = null
    private var locked = false
    private var autoLockOnMinimize = false
    private var autoLockOnFocusLost = false
    private var autoLockDelayMs = 0L
    private var lockTimer: Timer? = null
        set(value) {
            field?.stop()
            field = value
        }
    private var previousNonIconifiedState = Frame.NORMAL
    private var contentSecured = false
    private var restoreRequested = false
    private var contentSecurityInProgress = false

    private var windowListeners: DesktopWindowListeners? = null
    private var lockListener: (() -> Unit)? = null

    val isLocked: Boolean
        get() = locked

    val isMinimized: Boolean
        get() = frame?.let { it.extendedState and Frame.ICONIFIED != 0 } ?: false

    internal val isRestoreDeferred: Boolean
        get() = restoreRequested

    fun attachWindow(window: JFrame) {
        if (frame === window) return
        lockTimer = null
        windowListeners?.detach()
        frame = window
        windowListeners = DesktopWindowListeners(
            frame = window,
            onStateChanged = { event ->
                if (event.newState and Frame.ICONIFIED != 0) {
                    if (event.oldState and Frame.ICONIFIED == 0) {
                        previousNonIconifiedState = event.oldState
                    }
                    if (autoLockOnMinimize) lock()
                } else if (
                    shouldDeferDesktopWindowRestore(
                        oldState = event.oldState,
                        newState = event.newState,
                        locked = locked,
                        contentSecured = contentSecured,
                    )
                ) {
                    // Taskbar and window-manager restores do not pass through
                    // restoreWindow(). Keep the native surface concealed until
                    // Compose has observed the terminal Locked state, then
                    // honor the user's restore request.
                    restoreRequested = true
                    window.extendedState = event.newState or Frame.ICONIFIED
                    requestContentSecurity()
                }
            },
            onFocusLost = { event ->
                if (!event?.oppositeWindow.isOwnedBy(window) && autoLockOnFocusLost) {
                    if (autoLockDelayMs == 0L) {
                        lock()
                    } else {
                        val delay = autoLockDelayMs.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
                        lockTimer = Timer(delay) { lock() }.apply {
                            isRepeats = false
                            start()
                        }
                    }
                }
            },
            onFocusGained = { lockTimer = null },
            onClosed = ::cleanup,
        ).also(DesktopWindowListeners::attach)
    }

    fun lock() {
        prepareForShutdown()
        requestContentSecurity()
    }

    /**
     * Conceals the native window immediately while the shutdown owner performs
     * its own non-cancellable repository and clipboard cleanup.
     *
     * Unlike [lock], this does not notify the normal lock listener; doing so
     * would start a duplicate cleanup job and delay process termination behind
     * two serialized repository locks.
     */
    fun prepareForShutdown() {
        if (locked) return
        lockTimer = null
        locked = true
        contentSecured = false
        restoreRequested = false
        contentSecurityInProgress = false
        frame?.let { current ->
            val state = current.extendedState
            if (state and Frame.ICONIFIED == 0) {
                previousNonIconifiedState = state
            }
            current.extendedState = state or Frame.ICONIFIED
        }
    }

    fun unlock() {
        if (!locked) return
        locked = false
        contentSecured = false
        restoreRequested = false
        contentSecurityInProgress = false
        restoreDesktopNativeWindow(frame, previousNonIconifiedState)
    }

    fun restoreWindow() {
        if (locked && !contentSecured) {
            restoreRequested = true
            requestContentSecurity()
            return
        }
        restoreRequested = false
        restoreDesktopNativeWindow(frame, previousNonIconifiedState)
    }

    /** Allows a deferred restore only after shared sensitive UI has been scrubbed, guarded, and rendered. */
    fun onVaultContentSecured() {
        if (!locked) return
        contentSecurityInProgress = false
        contentSecured = true
        if (restoreRequested) {
            restoreRequested = false
            restoreDesktopNativeWindow(frame, previousNonIconifiedState)
        }
    }

    /** Rearms a user- or lifecycle-triggered retry without exposing the concealed native window. */
    fun onVaultContentSecurityFailed() {
        if (locked && !contentSecured) contentSecurityInProgress = false
    }

    private fun requestContentSecurity() {
        if (!locked || contentSecured || contentSecurityInProgress) return
        val listener = lockListener ?: return
        contentSecurityInProgress = true
        try {
            listener()
        } catch (_: Exception) {
            contentSecurityInProgress = false
        }
    }

    fun configureAutoLock(
        lockOnMinimize: Boolean,
        lockOnFocusLost: Boolean,
        focusLossDelayMs: Long = 0,
    ) {
        autoLockOnMinimize = lockOnMinimize
        autoLockOnFocusLost = lockOnFocusLost
        autoLockDelayMs = focusLossDelayMs.coerceAtLeast(0L)
        if (!lockOnFocusLost) lockTimer = null
    }

    fun setLockListener(listener: (() -> Unit)?) {
        lockListener = listener
    }

    fun cleanup() {
        lockTimer = null
        windowListeners?.detach()
        windowListeners = null
        lockListener = null
        frame = null
        locked = false
        autoLockOnMinimize = false
        autoLockOnFocusLost = false
        autoLockDelayMs = 0L
        previousNonIconifiedState = Frame.NORMAL
        contentSecured = false
        restoreRequested = false
        contentSecurityInProgress = false
    }

}

private fun restoreDesktopNativeWindow(frame: JFrame?, previousNonIconifiedState: Int) {
    frame?.let { current ->
        current.extendedState = previousNonIconifiedState and Frame.ICONIFIED.inv()
        current.toFront()
        current.requestFocus()
    }
}

private class DesktopWindowListeners(
    private val frame: JFrame,
    onStateChanged: (WindowEvent) -> Unit,
    onFocusLost: (WindowEvent?) -> Unit,
    onFocusGained: (WindowEvent?) -> Unit,
    onClosed: () -> Unit,
) {
    private val stateListener = WindowStateListener(onStateChanged)
    private val focusListener = object : WindowAdapter() {
        override fun windowLostFocus(event: WindowEvent?) = onFocusLost(event)

        override fun windowGainedFocus(event: WindowEvent?) = onFocusGained(event)
    }
    private val closeListener = object : WindowAdapter() {
        override fun windowClosed(event: WindowEvent?) = onClosed()
    }

    fun attach() {
        frame.addWindowStateListener(stateListener)
        frame.addWindowFocusListener(focusListener)
        frame.addWindowListener(closeListener)
    }

    fun detach() {
        frame.removeWindowStateListener(stateListener)
        frame.removeWindowFocusListener(focusListener)
        frame.removeWindowListener(closeListener)
    }
}

private fun Window?.isOwnedBy(owner: JFrame): Boolean {
    var window = this
    while (window != null) {
        if (window === owner) return true
        window = window.owner
    }
    return false
}

internal fun shouldDeferDesktopWindowRestore(
    oldState: Int,
    newState: Int,
    locked: Boolean,
    contentSecured: Boolean,
): Boolean = locked &&
    !contentSecured &&
    oldState and Frame.ICONIFIED != 0 &&
    newState and Frame.ICONIFIED == 0
