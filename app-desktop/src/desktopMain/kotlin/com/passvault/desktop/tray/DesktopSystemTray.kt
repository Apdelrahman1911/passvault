package com.passvault.desktop.tray

import com.passvault.desktop.OperatingSystem
import com.passvault.desktop.getOperatingSystem
import java.awt.Image
import java.awt.MenuItem
import java.awt.PopupMenu
import java.awt.SystemTray
import java.awt.TrayIcon
import java.awt.image.BufferedImage
import java.util.logging.Logger
import javax.imageio.ImageIO
import javax.swing.SwingUtilities

/**
 * Desktop system tray integration for PassVault.
 * Provides tray icon, context menu, and notifications.
 */
class DesktopSystemTray {

    private val logger = Logger.getLogger(DesktopSystemTray::class.java.name)

    private var trayIcon: TrayIcon? = null
    private var isVisible = false
    private var strings: DesktopTrayStrings? = null

    // Callbacks
    private var onShowCallback: (() -> Unit)? = null
    private var onLockCallback: (() -> Unit)? = null
    private var onExitCallback: (() -> Unit)? = null

    /**
     * Check if system tray is supported on this platform.
     */
    fun isSupported(): Boolean {
        return SystemTray.isSupported()
    }

    /**
     * Setup the system tray.
     */
    fun setup(
        strings: DesktopTrayStrings,
        onShow: () -> Unit,
        onLock: () -> Unit,
        onExit: () -> Unit,
    ) {
        if (!isSupported()) {
            logger.fine("System tray is not supported on this platform")
            return
        }

        this.onShowCallback = onShow
        this.onLockCallback = onLock
        this.onExitCallback = onExit
        this.strings = strings

        runOnEventDispatchThread {
            try {
                if (trayIcon == null) createTrayIcon()
                isVisible = true
            } catch (_: Exception) {
                // createTrayIcon only publishes the property after SystemTray
                // accepts it, but also clear state here in case a later setup
                // step fails. A stale non-null icon would otherwise prevent all
                // future setup attempts for the lifetime of this singleton.
                trayIcon = null
                isVisible = false
                logger.warning("Unable to create the system tray icon")
            }
        }
    }

    /**
     * Hide the tray icon.
     */
    fun hide() {
        if (!isSupported() || !isVisible) return

        runOnEventDispatchThread {
            try {
                val tray = SystemTray.getSystemTray()
                trayIcon?.let { tray.remove(it) }
                trayIcon = null
                isVisible = false
            } catch (_: Exception) {
                logger.warning("Unable to hide the system tray icon")
            }
        }
    }

    /**
     * Create the tray icon and menu.
     */
    private fun createTrayIcon() {
        val tray = SystemTray.getSystemTray()
        val currentStrings = checkNotNull(strings) { "Tray strings must be configured before setup" }

        // Create popup menu
        val popupMenu = PopupMenu().apply {
            // Show window
            add(MenuItem(currentStrings.showApp).apply {
                addActionListener { onShowCallback?.invoke() }
            })

            addSeparator()

            // Lock
            add(MenuItem(currentStrings.lockVault).apply {
                addActionListener { onLockCallback?.invoke() }
            })

            addSeparator()

            // Exit
            add(MenuItem(currentStrings.exit).apply {
                addActionListener { onExitCallback?.invoke() }
            })
        }

        // Create tray icon
        val image = loadTrayIcon()
        val newTrayIcon = TrayIcon(image, currentStrings.tooltip, popupMenu).apply {
            isImageAutoSize = true

            // Double-click handler
            addActionListener { onShowCallback?.invoke() }

        }

        // Publish the icon only after the native tray accepts it. Some Linux
        // desktop environments report tray support but reject an add request;
        // retaining that rejected icon would make setup incorrectly look done.
        tray.add(newTrayIcon)
        trayIcon = newTrayIcon
    }

    /**
     * Load the tray icon image.
     */
    private fun loadTrayIcon(): Image {
        // Try to load icon from resources
        return try {
            val iconName = when (getOperatingSystem()) {
                OperatingSystem.MACOS -> "/icons/tray-mac.png"
                OperatingSystem.WINDOWS -> "/icons/tray-win.png"
                else -> "/icons/tray.png"
            }

            javaClass.getResourceAsStream(iconName)?.use(ImageIO::read)
                ?: createDefaultIcon()
        } catch (_: Exception) {
            createDefaultIcon()
        }
    }

    /**
     * Create a default icon if resource loading fails.
     */
    private fun createDefaultIcon(): Image {
        // Create a simple colored square as default icon
        val size = if (getOperatingSystem() == OperatingSystem.MACOS) 22 else 16
        val image = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
        val graphics = image.createGraphics()

        // Draw a lock icon shape
        graphics.color = java.awt.Color(0x1976D2)
        graphics.fillRoundRect(2, 2, size - 4, size - 4, 4, 4)

        // Draw lock body
        graphics.color = java.awt.Color.WHITE
        graphics.fillRoundRect(size / 4, size / 2, size / 2, size / 3, 2, 2)

        // Draw lock shackle
        graphics.drawArc(size / 4, size / 4, size / 2, size / 2, 0, 180)

        graphics.dispose()
        return image
    }

    /**
     * Clean up resources.
     */
    fun cleanup() {
        hide()
        onShowCallback = null
        onLockCallback = null
        onExitCallback = null
        strings = null
    }

    private fun runOnEventDispatchThread(block: () -> Unit) {
        if (SwingUtilities.isEventDispatchThread()) {
            block()
        } else {
            SwingUtilities.invokeAndWait(block)
        }
    }

}

data class DesktopTrayStrings(
    val tooltip: String,
    val showApp: String,
    val lockVault: String,
    val exit: String,
)
