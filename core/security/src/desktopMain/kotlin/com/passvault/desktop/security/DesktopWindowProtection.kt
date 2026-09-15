package com.passvault.desktop.security

import java.awt.Color
import java.awt.Component
import java.awt.Frame
import java.awt.Window
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.event.WindowStateListener
import javax.swing.JFrame
import javax.swing.JPanel
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
    /** True only while an app-owned OS prompt is allowed to defer focus-loss locking. */
    var focusLossAutoLockSuppressed: () -> Boolean = { false }
    private val focusLossLockPolicy = DesktopFocusLossLockPolicy(::monotonicTimeMillis)
    private var lockTimer: Timer? = null
    private var lockTimerToken: Any? = null
    private var previousNonIconifiedState = Frame.NORMAL
    private var contentSecured = false
    private var restoreRequested = false
    private var contentSecurityInProgress = false
    private var concealmentCurtain: JPanel? = null
    private var previousGlassPane: Component? = null

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
        cancelFocusLossTimer()
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
                if (!locked && !event?.oppositeWindow.isOwnedBy(window) && autoLockOnFocusLost) {
                    applyFocusLossDecision(
                        focusLossLockPolicy.onFocusLost(isFocusLossAutoLockSuppressed()),
                    )
                }
            },
            onFocusGained = {
                if (!locked) {
                    applyFocusLossDecision(
                        focusLossLockPolicy.onFocusGained(isFocusLossAutoLockSuppressed()),
                    )
                }
            },
            onClosed = ::cleanup,
        ).also(DesktopWindowListeners::attach)
    }

    fun lock() {
        if (!enterLockedState()) return
        installConcealmentCurtain()
        frame?.let { current ->
            val state = current.extendedState
            if (state and Frame.ICONIFIED == 0) {
                previousNonIconifiedState = state
            }
            current.extendedState = state or Frame.ICONIFIED
        }
        requestContentSecurity()
    }

    /**
     * Conceals the native content immediately while the shutdown owner performs
     * its own non-cancellable repository and clipboard cleanup. Terminal
     * shutdown deliberately does not iconify or change full-screen placement:
     * doing so races Compose window disposal against AppKit's asynchronous
     * full-screen transition and can prevent the JVM from terminating.
     *
     * Unlike [lock], this does not notify the normal lock listener; doing so
     * would start a duplicate cleanup job and delay process termination behind
     * two serialized repository locks.
     */
    fun prepareForShutdown() {
        enterLockedState()
        installConcealmentCurtain()
    }

    fun unlock() {
        cancelFocusLossTimer()
        focusLossLockPolicy.resetEpisode()
        if (!locked) return
        removeConcealmentCurtain()
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
        maximumSuppressedFocusLossMs: Long = focusLossDelayMs,
    ) {
        autoLockOnMinimize = lockOnMinimize
        autoLockOnFocusLost = lockOnFocusLost
        applyFocusLossDecision(
            focusLossLockPolicy.configure(
                enabled = lockOnFocusLost,
                focusLossDelayMillis = focusLossDelayMs,
                maximumSuppressedFocusLossMillis = maximumSuppressedFocusLossMs,
                suppressed = isFocusLossAutoLockSuppressed(),
            ),
        )
    }

    fun setLockListener(listener: (() -> Unit)?) {
        lockListener = listener
    }

    /** Ends biometric deferral and enforces any cumulative focus-loss budget already consumed. */
    fun onFocusLossSuppressionEnded() {
        applyFocusLossDecision(focusLossLockPolicy.onSuppressionEnded())
    }

    private fun applyFocusLossDecision(decision: DesktopFocusLossLockDecision) {
        when (decision) {
            DesktopFocusLossLockDecision.None -> cancelFocusLossTimer()
            DesktopFocusLossLockDecision.Lock -> lock()
            is DesktopFocusLossLockDecision.Schedule -> scheduleFocusLossTimer(decision.delayMillis)
        }
    }

    private fun scheduleFocusLossTimer(delayMillis: Long) {
        cancelFocusLossTimer()
        val token = Any()
        lockTimerToken = token
        val delay = delayMillis.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        lockTimer = Timer(delay) {
            if (lockTimerToken !== token) return@Timer
            cancelFocusLossTimer()
            applyFocusLossDecision(
                focusLossLockPolicy.onTimerFired(isFocusLossAutoLockSuppressed()),
            )
        }.apply {
            isRepeats = false
            start()
        }
    }

    private fun cancelFocusLossTimer() {
        lockTimer?.stop()
        lockTimer = null
        lockTimerToken = null
    }

    private fun isFocusLossAutoLockSuppressed(): Boolean =
        runCatching(focusLossAutoLockSuppressed).getOrDefault(false)

    fun cleanup() {
        removeConcealmentCurtain()
        cancelFocusLossTimer()
        windowListeners?.detach()
        windowListeners = null
        lockListener = null
        frame = null
        locked = false
        autoLockOnMinimize = false
        autoLockOnFocusLost = false
        focusLossLockPolicy.reset()
        focusLossAutoLockSuppressed = { false }
        previousNonIconifiedState = Frame.NORMAL
        contentSecured = false
        restoreRequested = false
        contentSecurityInProgress = false
    }

    private fun enterLockedState(): Boolean {
        if (locked) return false
        cancelFocusLossTimer()
        focusLossLockPolicy.resetEpisode()
        locked = true
        contentSecured = false
        restoreRequested = false
        contentSecurityInProgress = false
        return true
    }

    private fun installConcealmentCurtain() {
        frame?.let { current ->
            if (concealmentCurtain == null) {
                previousGlassPane = current.glassPane
                concealmentCurtain = JPanel().apply {
                    isOpaque = true
                    background = Color.BLACK
                    isFocusable = false
                }.also { curtain ->
                    current.glassPane = curtain
                    curtain.isVisible = true
                    curtain.revalidate()
                    curtain.repaint()
                }
            }
        }
    }

    private fun removeConcealmentCurtain() {
        val curtain = concealmentCurtain ?: return
        curtain.isVisible = false
        frame?.let { current ->
            if (current.glassPane === curtain) {
                previousGlassPane?.let { current.glassPane = it }
            }
        }
        concealmentCurtain = null
        previousGlassPane = null
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

private fun monotonicTimeMillis(): Long = System.nanoTime() / NANOSECONDS_PER_MILLISECOND

private const val NANOSECONDS_PER_MILLISECOND = 1_000_000L

internal fun shouldDeferDesktopWindowRestore(
    oldState: Int,
    newState: Int,
    locked: Boolean,
    contentSecured: Boolean,
): Boolean = locked &&
    !contentSecured &&
    oldState and Frame.ICONIFIED != 0 &&
    newState and Frame.ICONIFIED == 0
