package com.passvault.desktop

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
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
import com.passvault.core.security.ClipboardService
import com.passvault.core.security.VaultUiSecurityCoordinator
import com.passvault.desktop.components.DesktopMenuBar
import com.passvault.desktop.components.KeyboardShortcuts
import com.passvault.desktop.security.DesktopWindowProtection
import com.passvault.desktop.tray.DesktopSystemTray
import com.passvault.desktop.tray.DesktopTrayStrings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource
import org.koin.core.context.GlobalContext
import java.awt.Dimension
import java.awt.GraphicsEnvironment
import java.awt.Toolkit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.prefs.Preferences

/**
 * Main desktop window for PassVault.
 * Provides window management, keyboard shortcuts, menu bar, and security features.
 */
@Composable
fun PassVaultDesktopWindow(
    onCloseRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    val securityScope = remember { GlobalContext.get().get<CoroutineScope>() }
    val vaultRepository = remember { GlobalContext.get().get<VaultRepository>() }
    val commandDispatcher = remember { GlobalContext.get().get<AppCommandDispatcher>() }
    val clipboardService = remember { GlobalContext.get().get<ClipboardService>() }
    val vaultUiSecurityCoordinator = remember { GlobalContext.get().get<VaultUiSecurityCoordinator>() }
    val sessionState by vaultRepository.getSessionState()
        .collectAsState(initial = VaultSessionState.Uninitialized)

    val windowState = rememberPersistentWindowState()
    val windowProtection = remember { GlobalContext.get().get<DesktopWindowProtection>() }
    val systemTray = remember { GlobalContext.get().get<DesktopSystemTray>() }
    val trayStrings = DesktopTrayStrings(
        tooltip = stringResource(Res.string.desktop_tray_tooltip),
        showApp = stringResource(Res.string.desktop_tray_show),
        lockVault = stringResource(Res.string.desktop_tray_lock),
        exit = stringResource(Res.string.desktop_tray_exit),
    )
    val focusRequester = remember { FocusRequester() }
    val requestClose = rememberCloseHandler(
        windowState,
        systemTray,
        windowProtection,
        securityScope,
        vaultRepository,
        clipboardService,
        onCloseRequest,
    )
    val keyboardShortcuts = rememberKeyboardShortcuts(
        commandDispatcher,
        windowProtection,
        sessionState,
        requestClose,
    )
    DesktopWindowEffects(
        focusRequester,
        trayStrings,
        systemTray,
        windowProtection,
        sessionState,
        securityScope,
        vaultRepository,
        clipboardService,
        vaultUiSecurityCoordinator,
        requestClose,
    )
    DesktopApplicationWindow(
        windowState,
        sessionState,
        focusRequester,
        keyboardShortcuts,
        commandDispatcher,
        windowProtection,
        systemTray,
        requestClose,
        content,
    )
}

@Composable
private fun DesktopApplicationWindow(
    windowState: WindowState,
    sessionState: VaultSessionState,
    focusRequester: FocusRequester,
    keyboardShortcuts: KeyboardShortcuts,
    commandDispatcher: AppCommandDispatcher,
    windowProtection: DesktopWindowProtection,
    systemTray: DesktopSystemTray,
    requestClose: () -> Unit,
    content: @Composable () -> Unit,
) {
    Window(
        onCloseRequest = requestClose,
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
        BindDesktopNativeWindow(windowProtection, systemTray)
        UpdateDesktopWindowTitle(sessionState, windowProtection)
        DesktopWindowMenu(
            commandDispatcher,
            windowProtection,
            sessionState,
            windowState,
            requestClose,
        )
        DesktopWindowContent(focusRequester, keyboardShortcuts, content)
    }
}

@Composable
private fun rememberCloseHandler(
    windowState: WindowState,
    systemTray: DesktopSystemTray,
    windowProtection: DesktopWindowProtection,
    securityScope: CoroutineScope,
    vaultRepository: VaultRepository,
    clipboardService: ClipboardService,
    onCloseRequest: () -> Unit,
): () -> Unit {
    val closeRequested = remember { AtomicBoolean(false) }
    return remember(
        windowState,
        systemTray,
        windowProtection,
        securityScope,
        vaultRepository,
        clipboardService,
        onCloseRequest,
    ) {
        {
            if (closeRequested.compareAndSet(false, true)) {
                windowProtection.prepareForShutdown()
                saveWindowState(windowState)
                systemTray.hide()
                securityScope.launch {
                    lockAndClear(vaultRepository, clipboardService)
                    withContext(Dispatchers.Main.immediate) { onCloseRequest() }
                }
            }
        }
    }
}

@Composable
private fun rememberKeyboardShortcuts(
    commandDispatcher: AppCommandDispatcher,
    windowProtection: DesktopWindowProtection,
    sessionState: VaultSessionState,
    requestClose: () -> Unit,
): KeyboardShortcuts {
    val canLock = areDesktopSessionActionsEnabled(sessionState)
    return remember(commandDispatcher, windowProtection, canLock, requestClose) {
        KeyboardShortcuts(
            sessionActionsEnabled = canLock,
            onNewCredential = { commandDispatcher.dispatch(AppCommand.NEW_CREDENTIAL) },
            onSearch = { commandDispatcher.dispatch(AppCommand.SEARCH) },
            onLock = { if (canLock) windowProtection.lock() },
            onGeneratePassword = { commandDispatcher.dispatch(AppCommand.GENERATOR) },
            onSettings = { commandDispatcher.dispatch(AppCommand.SETTINGS) },
            onHelp = { commandDispatcher.dispatch(AppCommand.HELP) },
            onQuit = requestClose,
        )
    }
}

@Composable
private fun DesktopWindowEffects(
    focusRequester: FocusRequester,
    trayStrings: DesktopTrayStrings,
    systemTray: DesktopSystemTray,
    windowProtection: DesktopWindowProtection,
    sessionState: VaultSessionState,
    securityScope: CoroutineScope,
    vaultRepository: VaultRepository,
    clipboardService: ClipboardService,
    vaultUiSecurityCoordinator: VaultUiSecurityCoordinator,
    requestClose: () -> Unit,
) {
    val currentSessionState by rememberUpdatedState(sessionState)
    LaunchedEffect(focusRequester, trayStrings, systemTray, windowProtection, requestClose) {
        focusRequester.requestFocus()
        systemTray.setup(
            strings = trayStrings,
            onShow = windowProtection::restoreWindow,
            onLock = {
                if (areDesktopSessionActionsEnabled(currentSessionState)) {
                    windowProtection.lock()
                }
            },
            onExit = requestClose,
        )
    }
    val canAutoLock = areDesktopSessionActionsEnabled(sessionState)
    LaunchedEffect(canAutoLock, windowProtection) {
        windowProtection.configureAutoLock(
            lockOnMinimize = canAutoLock,
            lockOnFocusLost = canAutoLock,
            focusLossDelayMs = DEFAULT_FOCUS_LOCK_DELAY_MS,
        )
    }
    DisposableEffect(
        windowProtection,
        securityScope,
        vaultRepository,
        clipboardService,
        vaultUiSecurityCoordinator,
    ) {
        val lockListener = {
            securityScope.launch {
                val contentSecured = lockClearAndAwaitUiSecurity(
                    vaultRepository,
                    clipboardService,
                    vaultUiSecurityCoordinator,
                )
                withContext(Dispatchers.Main.immediate) {
                    if (contentSecured) {
                        windowProtection.onVaultContentSecured()
                    } else {
                        windowProtection.onVaultContentSecurityFailed()
                    }
                }
            }
            Unit
        }
        windowProtection.setLockListener(lockListener)
        onDispose { windowProtection.setLockListener(null) }
    }
}

@Composable
private fun FrameWindowScope.BindDesktopNativeWindow(
    windowProtection: DesktopWindowProtection,
    systemTray: DesktopSystemTray,
) {
    DisposableEffect(window, windowProtection, systemTray) {
        windowProtection.attachWindow(window)
        val usableBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().maximumWindowBounds
        window.minimumSize = Dimension(
            MIN_WINDOW_WIDTH.coerceAtMost(usableBounds.width.coerceAtLeast(1)),
            MIN_WINDOW_HEIGHT.coerceAtMost(usableBounds.height.coerceAtLeast(1)),
        )
        onDispose {
            systemTray.cleanup()
            windowProtection.cleanup()
        }
    }
}

@Composable
private fun FrameWindowScope.UpdateDesktopWindowTitle(
    sessionState: VaultSessionState,
    windowProtection: DesktopWindowProtection,
) {
    val lockedWindowTitle = stringResource(Res.string.desktop_window_title_locked, AppInfo.NAME)
    LaunchedEffect(sessionState, lockedWindowTitle, windowProtection) {
        when (sessionState) {
            is VaultSessionState.Unlocked -> windowProtection.unlock()
            else -> Unit
        }
        window.title = if (sessionState is VaultSessionState.Unlocked) {
            AppInfo.getVersionString()
        } else {
            lockedWindowTitle
        }
    }
}

@Composable
private fun FrameWindowScope.DesktopWindowMenu(
    commandDispatcher: AppCommandDispatcher,
    windowProtection: DesktopWindowProtection,
    sessionState: VaultSessionState,
    windowState: WindowState,
    requestClose: () -> Unit,
) {
    val canLock = areDesktopSessionActionsEnabled(sessionState)
    DesktopMenuBar(
        sessionActionsEnabled = canLock,
        onNewCredential = { commandDispatcher.dispatch(AppCommand.NEW_CREDENTIAL) },
        onImport = { commandDispatcher.dispatch(AppCommand.IMPORT) },
        onExport = { commandDispatcher.dispatch(AppCommand.EXPORT) },
        onSettings = { commandDispatcher.dispatch(AppCommand.SETTINGS) },
        onLock = { if (canLock) windowProtection.lock() },
        onSearch = { commandDispatcher.dispatch(AppCommand.SEARCH) },
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
        onDocumentation = { commandDispatcher.dispatch(AppCommand.HELP) },
        onKeyboardShortcuts = { commandDispatcher.dispatch(AppCommand.HELP) },
        onAbout = { commandDispatcher.dispatch(AppCommand.ABOUT) },
        onQuit = requestClose,
    )
}

@Composable
private fun DesktopWindowContent(
    focusRequester: FocusRequester,
    keyboardShortcuts: KeyboardShortcuts,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .onPreviewKeyEvent(keyboardShortcuts::handleKeyEvent),
    ) {
        content()
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
    val minimumWidth = MIN_WINDOW_WIDTH.coerceAtMost(screenSize.width.coerceAtLeast(1))
    val minimumHeight = MIN_WINDOW_HEIGHT.coerceAtMost(screenSize.height.coerceAtLeast(1))
    val visibleWidth = MIN_VISIBLE_WINDOW_PIXELS.coerceAtMost(screenSize.width.coerceAtLeast(1))
    val visibleHeight = MIN_VISIBLE_WINDOW_PIXELS.coerceAtMost(screenSize.height.coerceAtLeast(1))

    // Load saved state
    val savedWidth = prefs.getInt("window.width", defaultWidth)
        .coerceIn(minimumWidth, screenSize.width.coerceAtLeast(1))
    val savedHeight = prefs.getInt("window.height", defaultHeight)
        .coerceIn(minimumHeight, screenSize.height.coerceAtLeast(1))
    val savedX = prefs.getInt("window.x", defaultX)
        .coerceIn(-savedWidth + visibleWidth, screenSize.width - visibleWidth)
    val savedY = prefs.getInt("window.y", defaultY)
        .coerceIn(0, screenSize.height - visibleHeight)
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
    runCatching {
        val prefs = Preferences.userNodeForPackage(AppInfo::class.java)
        prefs.putInt("window.width", windowState.size.width.value.toInt())
        prefs.putInt("window.height", windowState.size.height.value.toInt())
        prefs.putInt("window.x", windowState.position.x.value.toInt())
        prefs.putInt("window.y", windowState.position.y.value.toInt())
        prefs.putBoolean("window.maximized", windowState.placement == WindowPlacement.Maximized)
        prefs.putLong("window.savedAt", System.currentTimeMillis())
        prefs.sync()
    }
}

private const val MIN_WINDOW_WIDTH = 480
private const val MIN_WINDOW_HEIGHT = 360
private const val MIN_VISIBLE_WINDOW_PIXELS = 80
private const val DEFAULT_FOCUS_LOCK_DELAY_MS = 30_000L
