package com.passvault.desktop.tray

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.awt.Image
import java.awt.MenuItem
import java.awt.PopupMenu
import java.awt.SystemTray
import java.awt.TrayIcon
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import javax.swing.SwingUtilities
import java.util.logging.Logger

/**
 * Desktop system tray integration for PassVault.
 * Provides tray icon, context menu, and notifications.
 */
class DesktopSystemTray(
    private val scope: CoroutineScope,
) {

    private val logger = Logger.getLogger(DesktopSystemTray::class.java.name)

    private var trayIcon: TrayIcon? = null
    private var popupMenu: PopupMenu? = null
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

        scope.launch {
            SwingUtilities.invokeLater {
                try {
                    createTrayIcon()
                    isVisible = true
                } catch (_: Exception) {
                    logger.warning("Unable to create the system tray icon")
                }
            }
        }
    }

    /**
     * Hide the tray icon.
     */
    fun hide() {
        if (!isSupported() || !isVisible) return

        scope.launch {
            SwingUtilities.invokeLater {
                try {
                    val tray = SystemTray.getSystemTray()
                    trayIcon?.let { tray.remove(it) }
                    trayIcon = null
                    popupMenu = null
                    isVisible = false
                } catch (_: Exception) {
                    logger.warning("Unable to hide the system tray icon")
                }
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
        popupMenu = PopupMenu().apply {
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
        trayIcon = TrayIcon(image, currentStrings.tooltip, popupMenu).apply {
            isImageAutoSize = true

            // Double-click handler
            addActionListener { onShowCallback?.invoke() }

            // Mouse click handler
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    when (e.button) {
                        java.awt.event.MouseEvent.BUTTON1 -> {
                            // Left click - show window
                            if (e.clickCount == 2) {
                                onShowCallback?.invoke()
                            }
                        }
                    }
                }
            })
        }

        // Add to system tray
        tray.add(trayIcon)
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
        } catch (e: Exception) {
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
    }

    /**
     * Get the current operating system.
     */
    private fun getOperatingSystem(): OperatingSystem {
        val osName = System.getProperty("os.name").lowercase()
        return when {
            osName.contains("win") -> OperatingSystem.WINDOWS
            osName.contains("mac") -> OperatingSystem.MACOS
            osName.contains("nix") || osName.contains("nux") || osName.contains("aix") -> OperatingSystem.LINUX
            else -> OperatingSystem.UNKNOWN
        }
    }

    enum class OperatingSystem {
        WINDOWS, MACOS, LINUX, UNKNOWN
    }
}

data class DesktopTrayStrings(
    val tooltip: String,
    val showApp: String,
    val lockVault: String,
    val exit: String,
)

/**
 * Extension to convert AWT Image to Compose ImageBitmap.
 */
fun Image.toImageBitmap(): ImageBitmap {
    if (this is BufferedImage) {
        return toComposeImageBitmap()
    }

    // Convert to BufferedImage first
    val buffered = BufferedImage(
        getWidth(null),
        getHeight(null),
        BufferedImage.TYPE_INT_ARGB
    )
    val graphics = buffered.createGraphics()
    graphics.drawImage(this, 0, 0, null)
    graphics.dispose()

    return buffered.toComposeImageBitmap()
}
