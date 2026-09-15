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
 *
 * Native tray handles and callbacks are confined to the AWT event dispatch
 * thread. Calls made from another thread enqueue work without waiting for the
 * event thread, so shutdown cannot deadlock behind tray cleanup.
 */
class DesktopSystemTray internal constructor(
    private val platform: DesktopTrayPlatform,
    private val eventThread: DesktopEventThread,
) {

    constructor() : this(AwtDesktopTrayPlatform, SwingDesktopEventThread)

    private val logger = Logger.getLogger(DesktopSystemTray::class.java.name)

    private var trayIcon: DesktopTrayIconHandle? = null

    // Callbacks
    private var onShowCallback: (() -> Unit)? = null
    private var onLockCallback: (() -> Unit)? = null
    private var onExitCallback: (() -> Unit)? = null

    /**
     * Check if system tray is supported on this platform.
     */
    fun isSupported(): Boolean {
        return platform.isSupported()
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
        eventThread.dispatch {
            if (!platform.isSupported()) {
                logger.fine("System tray is not supported on this platform")
                return@dispatch
            }
            onShowCallback = onShow
            onLockCallback = onLock
            onExitCallback = onExit
            try {
                if (trayIcon == null) {
                    trayIcon = platform.install(
                        strings = strings,
                        onShow = { onShowCallback?.invoke() },
                        onLock = { onLockCallback?.invoke() },
                        onExit = { onExitCallback?.invoke() },
                        image = loadTrayIcon(),
                    )
                }
            } catch (_: Exception) {
                // install only returns after the native tray accepts the icon.
                // Keep a failed setup retryable for the lifetime of this singleton.
                trayIcon = null
                logger.warning("Unable to create the system tray icon")
            }
        }
    }

    /**
     * Hide the tray icon.
     */
    fun hide() {
        eventThread.dispatch {
            hideOnEventThread()
        }
    }

    private fun hideOnEventThread() {
        if (!platform.isSupported()) return
        val installedIcon = trayIcon ?: return
        try {
            installedIcon.remove()
            trayIcon = null
        } catch (_: Exception) {
            logger.warning("Unable to hide the system tray icon")
        }
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
        eventThread.dispatch {
            hideOnEventThread()
            onShowCallback = null
            onLockCallback = null
            onExitCallback = null
        }
    }
}

internal fun interface DesktopEventThread {
    fun dispatch(block: () -> Unit)
}

internal object SwingDesktopEventThread : DesktopEventThread {
    override fun dispatch(block: () -> Unit) {
        if (SwingUtilities.isEventDispatchThread()) {
            block()
        } else {
            SwingUtilities.invokeLater(block)
        }
    }
}

internal fun interface DesktopTrayIconHandle {
    fun remove()
}

internal interface DesktopTrayPlatform {
    fun isSupported(): Boolean

    fun install(
        strings: DesktopTrayStrings,
        onShow: () -> Unit,
        onLock: () -> Unit,
        onExit: () -> Unit,
        image: Image,
    ): DesktopTrayIconHandle
}

private object AwtDesktopTrayPlatform : DesktopTrayPlatform {
    override fun isSupported(): Boolean = SystemTray.isSupported()

    override fun install(
        strings: DesktopTrayStrings,
        onShow: () -> Unit,
        onLock: () -> Unit,
        onExit: () -> Unit,
        image: Image,
    ): DesktopTrayIconHandle {
        val popupMenu = PopupMenu().apply {
            add(MenuItem(strings.showApp).apply { addActionListener { onShow() } })
            addSeparator()
            add(MenuItem(strings.lockVault).apply { addActionListener { onLock() } })
            addSeparator()
            add(MenuItem(strings.exit).apply { addActionListener { onExit() } })
        }
        val newTrayIcon = TrayIcon(image, strings.tooltip, popupMenu).apply {
            isImageAutoSize = true
            addActionListener { onShow() }
        }
        val tray = SystemTray.getSystemTray()
        // Publish the handle only after the native tray accepts the icon. Some
        // Linux desktops report support but reject an add request.
        tray.add(newTrayIcon)
        return DesktopTrayIconHandle { tray.remove(newTrayIcon) }
    }
}

data class DesktopTrayStrings(
    val tooltip: String,
    val showApp: String,
    val lockVault: String,
    val exit: String,
)
