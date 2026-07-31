package com.passvault.desktop.security

import androidx.compose.ui.awt.ComposeWindow
import com.passvault.core.security.WindowProtection
import java.awt.Frame
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.util.concurrent.CopyOnWriteArrayList
import javax.swing.JFrame

/**
 * Desktop implementation of window protection.
 * Manages screenshot protection, window lock state, and security features
 * specific to desktop platforms (Windows, macOS, Linux).
 */
class DesktopWindowProtection : WindowProtection {

    private var composeWindow: ComposeWindow? = null
    private var jFrame: JFrame? = null
    private var isProtectionEnabled = false
    private var isLockedState = false
    private var autoLockOnMinimize = false
    private var autoLockOnFocusLost = false
    private var autoLockDelayMs = 0L
    private var lockTimer: javax.swing.Timer? = null

    // Listeners for lock/unlock events
    private val lockListeners = CopyOnWriteArrayList<() -> Unit>()
    private val unlockListeners = CopyOnWriteArrayList<() -> Unit>()
    private val minimizeListeners = CopyOnWriteArrayList<() -> Unit>()
    private val restoreListeners = CopyOnWriteArrayList<() -> Unit>()

    /**
     * Attach to a Compose window.
     */
    fun attachWindow(window: ComposeWindow) {
        this.composeWindow = window
        this.jFrame = window
        setupWindowListeners()
    }

    /**
     * Attach to a JFrame.
     */
    fun attachFrame(frame: JFrame) {
        this.jFrame = frame
        setupWindowListeners()
    }

    override fun enableProtection() {
        isProtectionEnabled = true

        // Platform-specific protection implementations
        when (getOperatingSystem()) {
            OperatingSystem.WINDOWS -> enableWindowsProtection()
            OperatingSystem.MACOS -> enableMacProtection()
            OperatingSystem.LINUX -> enableLinuxProtection()
            else -> {}
        }
    }

    override fun disableProtection() {
        isProtectionEnabled = false

        // Restore normal window behavior
        jFrame?.let { frame ->
            try {
                frame.rootPane.putClientProperty("apple.awt.dragWindow", true)
                frame.rootPane.putClientProperty("apple.awt.window.shadow", true)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    override fun isProtected(): Boolean = isProtectionEnabled

    override fun minimize() {
        jFrame?.let { frame ->
            val state = frame.extendedState
            frame.extendedState = state or Frame.ICONIFIED
        }
        minimizeListeners.forEach { it() }
    }

    override fun lock() {
        if (!isLockedState) {
            isLockedState = true
            lockListeners.forEach { it() }
            minimize()
        }
    }

    override fun unlock() {
        if (isLockedState) {
            isLockedState = false
            restoreWindow()
            unlockListeners.forEach { it() }
        }
    }

    /**
     * Restore window from minimized state.
     */
    fun restoreWindow() {
        jFrame?.let { frame ->
            frame.extendedState = Frame.NORMAL
            frame.toFront()
            frame.requestFocus()
        }
        restoreListeners.forEach { it() }
    }

    /**
     * Check if window is currently locked.
     */
    fun isLocked(): Boolean = isLockedState

    /**
     * Check if window is minimized.
     */
    fun isMinimized(): Boolean {
        return jFrame?.extendedState?.and(Frame.ICONIFIED) != 0
    }

    /**
     * Set auto-lock on minimize.
     */
    fun setAutoLockOnMinimize(enabled: Boolean) {
        autoLockOnMinimize = enabled
    }

    /**
     * Set auto-lock on focus lost.
     */
    fun setAutoLockOnFocusLost(enabled: Boolean, delayMs: Long = 0) {
        autoLockOnFocusLost = enabled
        autoLockDelayMs = delayMs
    }

    /**
     * Add lock listener.
     */
    fun addLockListener(listener: () -> Unit) {
        lockListeners.add(listener)
    }

    /**
     * Remove lock listener.
     */
    fun removeLockListener(listener: () -> Unit) {
        lockListeners.remove(listener)
    }

    /**
     * Add unlock listener.
     */
    fun addUnlockListener(listener: () -> Unit) {
        unlockListeners.add(listener)
    }

    /**
     * Remove unlock listener.
     */
    fun removeUnlockListener(listener: () -> Unit) {
        unlockListeners.remove(listener)
    }

    /**
     * Add minimize listener.
     */
    fun addMinimizeListener(listener: () -> Unit) {
        minimizeListeners.add(listener)
    }

    /**
     * Remove minimize listener.
     */
    fun removeMinimizeListener(listener: () -> Unit) {
        minimizeListeners.remove(listener)
    }

    /**
     * Add restore listener.
     */
    fun addRestoreListener(listener: () -> Unit) {
        restoreListeners.add(listener)
    }

    /**
     * Remove restore listener.
     */
    fun removeRestoreListener(listener: () -> Unit) {
        restoreListeners.remove(listener)
    }

    /**
     * Blur window content (for lock screen).
     */
    fun blurContent() {
        // On desktop, we can't easily blur the entire window
        // This should be handled at the Compose layer
        isLockedState = true
        lockListeners.forEach { it() }
    }

    /**
     * Unblur window content.
     */
    fun unblurContent() {
        isLockedState = false
        unlockListeners.forEach { it() }
    }

    /**
     * Flash the window to get attention.
     */
    fun flashWindow() {
        jFrame?.let { frame ->
            when (getOperatingSystem()) {
                OperatingSystem.WINDOWS -> flashWindowsWindow(frame)
                OperatingSystem.MACOS -> flashMacWindow(frame)
                else -> {
                    frame.toFront()
                    frame.requestFocus()
                }
            }
        }
    }

    /**
     * Set window opacity.
     */
    fun setWindowOpacity(opacity: Float) {
        jFrame?.let { frame ->
            if (java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .defaultScreenDevice
                    .isWindowTranslucencySupported
                    (java.awt.GraphicsDevice.WindowTranslucency.TRANSLUCENT)
            ) {
                frame.opacity = opacity.coerceIn(0.0f, 1.0f)
            }
        }
    }

    /**
     * Get window bounds.
     */
    fun getWindowBounds(): java.awt.Rectangle? {
        return jFrame?.bounds
    }

    /**
     * Set window bounds.
     */
    fun setWindowBounds(bounds: java.awt.Rectangle) {
        jFrame?.bounds = bounds
    }

    /**
     * Get the underlying JFrame.
     */
    fun getFrame(): JFrame? = jFrame

    /**
     * Get the Compose window.
     */
    fun getComposeWindow(): ComposeWindow? = composeWindow

    private fun setupWindowListeners() {
        jFrame?.let { frame ->
            // Window state listener for minimize
            frame.addWindowStateListener { e ->
                if (e.newState and Frame.ICONIFIED != 0) {
                    // Window was minimized
                    minimizeListeners.forEach { it() }

                    if (autoLockOnMinimize) {
                        lock()
                    }
                } else if (e.oldState and Frame.ICONIFIED != 0) {
                    // Window was restored
                    restoreListeners.forEach { it() }
                }
            }

            // Focus listener
            frame.addWindowFocusListener(object : WindowAdapter() {
                override fun windowLostFocus(e: WindowEvent?) {
                    if (autoLockOnFocusLost) {
                        if (autoLockDelayMs > 0) {
                            lockTimer?.stop()
                            lockTimer = javax.swing.Timer(autoLockDelayMs.toInt()) {
                                lock()
                            }.apply { isRepeats = false; start() }
                        } else {
                            lock()
                        }
                    }
                }

                override fun windowGainedFocus(e: WindowEvent?) {
                    lockTimer?.stop()
                }
            })

            // Window closing listener
            frame.addWindowListener(object : WindowAdapter() {
                override fun windowClosing(e: WindowEvent?) {
                    // Clean up before closing
                    cleanup()
                }
            })
        }
    }

    private fun enableWindowsProtection() {
        jFrame?.let { frame ->
            // Windows-specific protection
            // Note: True screenshot protection requires native Windows APIs
            // This is a best-effort implementation
            try {
                // Set window to not appear in window switcher (optional)
                // frame.type = javax.swing.JWindow.Type.UTILITY
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    private fun enableMacProtection() {
        jFrame?.let { frame ->
            // macOS-specific protection
            try {
                // Disable window shadow in screenshots
                frame.rootPane.putClientProperty("apple.awt.window.shadow", false)
                // Disable window dragging during protection
                frame.rootPane.putClientProperty("apple.awt.dragWindow", false)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    private fun enableLinuxProtection() {
        jFrame?.let { frame ->
            // Linux-specific protection
            // Most Linux window managers don't support screenshot protection
            try {
                // Set window properties via X11 (if available)
                frame.rootPane.putClientProperty("AWT_TRANSPARENT_REDIRECT_POLICY", false)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    private fun flashWindowsWindow(frame: JFrame) {
        try {
            // Windows-specific flash
            frame.toFront()
            java.awt.Taskbar.getTaskbar()?.requestUserAttention(true, true)
        } catch (e: Exception) {
            frame.toFront()
        }
    }

    private fun flashMacWindow(frame: JFrame) {
        try {
            // macOS-specific flash via AppKit
            val app = java.awt.Desktop.getDesktop()
            frame.toFront()
        } catch (e: Exception) {
            frame.toFront()
        }
    }

    private fun getOperatingSystem(): OperatingSystem {
        val osName = System.getProperty("os.name").lowercase()
        return when {
            osName.contains("win") -> OperatingSystem.WINDOWS
            osName.contains("mac") -> OperatingSystem.MACOS
            osName.contains("nix") || osName.contains("nux") || osName.contains("aix") -> OperatingSystem.LINUX
            else -> OperatingSystem.UNKNOWN
        }
    }

    /**
     * Clean up resources.
     */
    fun cleanup() {
        lockTimer?.stop()
        lockListeners.clear()
        unlockListeners.clear()
        minimizeListeners.clear()
        restoreListeners.clear()
        composeWindow = null
        jFrame = null
    }

    /**
     * Get current window state.
     */
    fun getWindowState(): WindowState {
        return WindowState(
            isLocked = isLockedState,
            isMinimized = isMinimized(),
            isProtected = isProtectionEnabled,
            bounds = getWindowBounds(),
        )
    }

    private enum class OperatingSystem {
        WINDOWS,
        MACOS,
        LINUX,
        UNKNOWN
    }

    /**
     * Window state information.
     */
    data class WindowState(
        val isLocked: Boolean,
        val isMinimized: Boolean,
        val isProtected: Boolean,
        val bounds: java.awt.Rectangle?,
    )

    companion object {
        const val DEFAULT_AUTO_LOCK_DELAY_MS = 30000L // 30 seconds
    }
}
