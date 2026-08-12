package com.passvault.desktop

import androidx.compose.ui.window.application
import com.passvault.desktop.di.desktopModule
import com.passvault.shared.PassVaultApp
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.logger.Level
import java.awt.GraphicsEnvironment
import java.awt.SplashScreen
import javax.swing.UIManager

/**
 * PassVault Desktop Application Entry Point
 *
 * Main entry point for the PassVault password manager desktop application.
 * Supports Windows, macOS, and Linux platforms.
 */
fun main() {
    // Check if running in headless mode
    if (GraphicsEnvironment.isHeadless()) {
        System.err.println("PassVault requires a graphical environment. Headless mode is not supported.")
        System.exit(1)
    }

    // Setup desktop environment
    setupDesktopEnvironment()

    // Initialize Koin DI
    initializeKoin()

    // Launch the application
    launchApplication()
}

/**
 * Setup desktop-specific environment and look-and-feel.
 */
private fun setupDesktopEnvironment() {
    // Set system properties for better desktop integration
    System.setProperty("awt.useSystemAAFontSettings", "on")
    System.setProperty("swing.aatext", "true")

    // Platform-specific setup
    when (val os = getOperatingSystem()) {
        OperatingSystem.MACOS -> setupMacOSEnvironment()
        OperatingSystem.WINDOWS -> setupWindowsEnvironment()
        OperatingSystem.LINUX -> setupLinuxEnvironment()
        else -> setupDefaultEnvironment()
    }

    // Set up Look and Feel
    try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    } catch (_: Exception) {
        // The platform default look and feel is a safe fallback.
    }

    // Close splash screen if present
    try {
        SplashScreen.getSplashScreen()?.close()
    } catch (_: Exception) {
        // Ignore
    }
}

/**
 * Initialize Koin dependency injection.
 */
private fun initializeKoin() {
    startKoin {
        printLogger(Level.ERROR) // Reduce logging noise
        modules(
            com.passvault.shared.di.AppModule.getAllModules(desktopModule)
        )
    }
}

/**
 * Launch the desktop application window.
 */
private fun launchApplication() {
    application {
        PassVaultDesktopWindow(
            onCloseRequest = { exitApplication() },
        ) {
            PassVaultApp()
        }
    }
}

/**
 * Setup macOS-specific environment.
 */
private fun setupMacOSEnvironment() {
    // macOS-specific settings
    System.setProperty("apple.laf.useScreenMenuBar", "true")
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", "PassVault")
    System.setProperty("apple.awt.application.name", "PassVault")
    System.setProperty("apple.awt.enableTemplate", "true")

    // Set up menu bar name
    try {
        val appClass = Class.forName("com.apple.eawt.Application")
        val application = appClass.getMethod("getApplication").invoke(null)
        appClass.getMethod("setDockIconImage", java.awt.Image::class.java)
            ?.invoke(application, getAppIcon())
    } catch (_: Exception) {
        // Not on macOS or library not available
    }
}

/**
 * Setup Windows-specific environment.
 */
private fun setupWindowsEnvironment() {
    // Windows-specific settings
    System.setProperty("sun.java2d.d3d", "true")
    System.setProperty("sun.java2d.ddforcevram", "true")

    // Enable DPI awareness
    System.setProperty("sun.java2d.uiScale", "true")
}

/**
 * Setup Linux-specific environment.
 */
private fun setupLinuxEnvironment() {
    // Linux-specific settings
    System.setProperty("sun.java2d.xrender", "true")

    // GTK theme integration
    try {
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel")
    } catch (_: Exception) {
        // Fallback to default
    }
}

/**
 * Setup default environment for other platforms.
 */
private fun setupDefaultEnvironment() {
    // Default settings
}

/**
 * Get application icon for the platform.
 */
private fun getAppIcon(): java.awt.Image? {
    return try {
        val iconPath = when (getOperatingSystem()) {
            OperatingSystem.MACOS -> "/icons/app-icon-mac.png"
            OperatingSystem.WINDOWS -> "/icons/app-icon-win.png"
            else -> "/icons/app-icon.png"
        }
        AppInfo::class.java.getResourceAsStream(iconPath)?.use(javax.imageio.ImageIO::read)
    } catch (_: Exception) {
        null
    }
}

/**
 * Detect the current operating system.
 */
internal fun getOperatingSystem(): OperatingSystem =
    operatingSystemFromName(System.getProperty("os.name").orEmpty())

internal fun operatingSystemFromName(osName: String): OperatingSystem {
    val normalizedName = osName.lowercase()
    return when {
        normalizedName.contains("mac") || normalizedName.contains("darwin") -> OperatingSystem.MACOS
        normalizedName.contains("win") -> OperatingSystem.WINDOWS
        normalizedName.contains("nix") ||
            normalizedName.contains("nux") ||
            normalizedName.contains("aix") -> OperatingSystem.LINUX
        else -> OperatingSystem.UNKNOWN
    }
}

/**
 * Operating system types.
 */
internal enum class OperatingSystem {
    WINDOWS,
    MACOS,
    LINUX,
    UNKNOWN
}

/**
 * Application version information.
 */
object AppInfo {
    const val VERSION = com.passvault.core.domain.PassVaultBuildInfo.VERSION
    const val NAME = "PassVault"
    const val FULL_NAME = "PassVault Password Manager"
    const val COPYRIGHT = "© 2026 PassVault project"

    fun getVersionString(): String = "$NAME v$VERSION"
}
