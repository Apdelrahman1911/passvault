package com.passvault.desktop

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState
import com.passvault.core.domain.model.VaultSessionState
import com.passvault.core.domain.repository.VaultRepository
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.navigation.AppCommand
import com.passvault.core.navigation.AppCommandDispatcher
import com.passvault.desktop.components.DesktopMenuBar
import com.passvault.desktop.components.KeyboardShortcuts
import com.passvault.desktop.security.DesktopWindowProtection
import com.passvault.desktop.tray.DesktopSystemTray
import com.passvault.desktop.tray.DesktopTrayStrings
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.core.context.GlobalContext
import java.awt.Dimension
import java.awt.Toolkit
import java.util.prefs.Preferences

/**
 * Composition local for accessing the desktop window protection.
 */
val LocalWindowProtection = staticCompositionLocalOf<DesktopWindowProtection?> { null }

/**
 * Composition local for accessing the system tray.
 */
val LocalSystemTray = staticCompositionLocalOf<DesktopSystemTray?> { null }

/**
 * Main desktop window for PassVault.
 * Provides window management, keyboard shortcuts, menu bar, and security features.
 */
@Composable
fun PassVaultDesktopWindow(
    onCloseRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val vaultRepository = remember { GlobalContext.get().get<VaultRepository>() }
    val commandDispatcher = remember { GlobalContext.get().get<AppCommandDispatcher>() }
    val sessionState by vaultRepository.getSessionState()
        .collectAsState(initial = VaultSessionState.Uninitialized)

    // Window state with persistence
    val windowState = rememberPersistentWindowState()

    // Platform-specific window protection
    val windowProtection = remember {
        GlobalContext.get().get<DesktopWindowProtection>()
    }

    // System tray
    val systemTray = remember {
        GlobalContext.get().get<DesktopSystemTray>()
    }
    val trayStrings = DesktopTrayStrings(
        tooltip = stringResource(Res.string.desktop_tray_tooltip),
        showApp = stringResource(Res.string.desktop_tray_show),
        lockVault = stringResource(Res.string.desktop_tray_lock),
        exit = stringResource(Res.string.desktop_tray_exit),
    )

    // Focus requester for keyboard shortcuts
    val focusRequester = remember { FocusRequester() }
    var closeRequested by remember { mutableStateOf(false) }

    fun requestClose() {
        if (closeRequested) return
        closeRequested = true
        saveWindowState(windowState)
        systemTray.hide()
        scope.launch {
            try {
                vaultRepository.lock()
            } finally {
                onCloseRequest()
            }
        }
    }

    // Keyboard shortcuts handler
    val keyboardShortcuts = remember {
        KeyboardShortcuts(
            onNewCredential = { commandDispatcher.dispatch(AppCommand.NEW_CREDENTIAL) },
            onSearch = { commandDispatcher.dispatch(AppCommand.SEARCH) },
            onLock = {
                commandDispatcher.dispatch(AppCommand.LOCK)
                windowProtection.lock()
            },
            onGeneratePassword = { commandDispatcher.dispatch(AppCommand.GENERATOR) },
            onSettings = { commandDispatcher.dispatch(AppCommand.SETTINGS) },
            onHelp = { commandDispatcher.dispatch(AppCommand.HELP) },
            onBack = { commandDispatcher.dispatch(AppCommand.BACK) },
            onQuit = ::requestClose,
        )
    }

    // Window setup effect
    LaunchedEffect(Unit) {
        // Request focus for keyboard shortcuts
        focusRequester.requestFocus()

        // Setup system tray
        systemTray.setup(
            strings = trayStrings,
            onShow = { windowState.isMinimized = false },
            onLock = {
                commandDispatcher.dispatch(AppCommand.LOCK)
                scope.launch { vaultRepository.lock() }
                windowProtection.lock()
            },
            onExit = ::requestClose
        )
        windowProtection.setAutoLockOnMinimize(true)
        windowProtection.setAutoLockOnFocusLost(true, DEFAULT_FOCUS_LOCK_DELAY_MS)
    }

    DisposableEffect(windowProtection) {
        val lockListener: () -> Unit = {
            commandDispatcher.dispatch(AppCommand.LOCK)
            scope.launch { vaultRepository.lock() }
        }
        windowProtection.addLockListener(lockListener)
        onDispose { windowProtection.removeLockListener(lockListener) }
    }

    Window(
        onCloseRequest = {
            requestClose()
        },
        title = AppInfo.getVersionString(),
        state = windowState,
        visible = true,
        resizable = true,
        enabled = true,
        alwaysOnTop = false,
        focusable = true,
        icon = null,
        onPreviewKeyEvent = { event ->
            keyboardShortcuts.handleKeyEvent(event)
        },
    ) {
        // Attach window to protection service
        DisposableEffect(window) {
            windowProtection.attachWindow(window)

            // Set minimum size
            window.minimumSize = Dimension(480, 360)

            onDispose {
                systemTray.cleanup()
                windowProtection.cleanup()
            }
        }

        // Update window title based on lock state
        val lockedWindowTitle = stringResource(Res.string.desktop_window_title_locked, AppInfo.NAME)
        LaunchedEffect(sessionState, lockedWindowTitle) {
            window.title = if (sessionState is VaultSessionState.Unlocked) {
                AppInfo.getVersionString()
            } else {
                lockedWindowTitle
            }
        }

        // Menu bar (platform-specific)
        DesktopMenuBar(
            onNewCredential = keyboardShortcuts::triggerNewCredential,
            onImport = { commandDispatcher.dispatch(AppCommand.IMPORT) },
            onExport = { commandDispatcher.dispatch(AppCommand.EXPORT) },
            onSettings = keyboardShortcuts::triggerSettings,
            onLock = keyboardShortcuts::triggerLock,
            onSearch = keyboardShortcuts::triggerSearch,
            onToggleDarkMode = { commandDispatcher.dispatch(AppCommand.TOGGLE_THEME) },
            onToggleFullscreen = {
                windowState.placement = if (windowState.placement == WindowPlacement.Fullscreen) {
                    WindowPlacement.Floating
                } else {
                    WindowPlacement.Fullscreen
                }
            },
            onGenerator = { commandDispatcher.dispatch(AppCommand.GENERATOR) },
            onHealth = { commandDispatcher.dispatch(AppCommand.HEALTH) },
            onClearClipboard = { commandDispatcher.dispatch(AppCommand.CLEAR_CLIPBOARD) },
            onDocumentation = keyboardShortcuts::triggerHelp,
            onKeyboardShortcuts = { commandDispatcher.dispatch(AppCommand.HELP) },
            onAbout = { commandDispatcher.dispatch(AppCommand.ABOUT) },
            onQuit = keyboardShortcuts::triggerQuit,
        )

        CompositionLocalProvider(
            LocalWindowProtection provides windowProtection,
            LocalSystemTray provides systemTray,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .focusRequester(focusRequester)
                    .onPreviewKeyEvent { event ->
                        keyboardShortcuts.handleKeyEvent(event)
                    },
            ) {
                content()
            }
        }
    }
}

/**
 * Remember window state with persistence.
 */
@Composable
private fun rememberPersistentWindowState(): WindowState {
    val prefs = remember { Preferences.userNodeForPackage(AppInfo::class.java) }

    // Default size and position
    val screenSize = Toolkit.getDefaultToolkit().screenSize
    val defaultWidth = 1200
    val defaultHeight = 800
    val defaultX = (screenSize.width - defaultWidth) / 2
    val defaultY = (screenSize.height - defaultHeight) / 2

    // Load saved state
    val savedWidth = prefs.getInt("window.width", defaultWidth)
        .coerceIn(MIN_WINDOW_WIDTH, screenSize.width)
    val savedHeight = prefs.getInt("window.height", defaultHeight)
        .coerceIn(MIN_WINDOW_HEIGHT, screenSize.height)
    val savedX = prefs.getInt("window.x", defaultX)
        .coerceIn(-savedWidth + MIN_VISIBLE_WINDOW_PIXELS, screenSize.width - MIN_VISIBLE_WINDOW_PIXELS)
    val savedY = prefs.getInt("window.y", defaultY)
        .coerceIn(0, screenSize.height - MIN_VISIBLE_WINDOW_PIXELS)
    val savedMaximized = prefs.getBoolean("window.maximized", false)

    return rememberWindowState(
        placement = if (savedMaximized) WindowPlacement.Maximized else WindowPlacement.Floating,
        size = DpSize(savedWidth.dp, savedHeight.dp),
        position = WindowPosition(savedX.dp, savedY.dp),
    )
}

/**
 * Save window state to preferences.
 */
private fun saveWindowState(windowState: WindowState) {
    val prefs = Preferences.userNodeForPackage(AppInfo::class.java)

    prefs.putInt("window.width", windowState.size.width.value.toInt())
    prefs.putInt("window.height", windowState.size.height.value.toInt())
    prefs.putInt("window.x", windowState.position.x.value.toInt())
    prefs.putInt("window.y", windowState.position.y.value.toInt())
    prefs.putBoolean("window.maximized", windowState.placement == WindowPlacement.Maximized)
    prefs.putLong("window.savedAt", System.currentTimeMillis())

    prefs.sync()
}

private const val MIN_WINDOW_WIDTH = 360
private const val MIN_WINDOW_HEIGHT = 280
private const val MIN_VISIBLE_WINDOW_PIXELS = 80
private const val DEFAULT_FOCUS_LOCK_DELAY_MS = 30_000L

