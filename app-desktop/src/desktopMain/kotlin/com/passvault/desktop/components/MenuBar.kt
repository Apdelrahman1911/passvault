package com.passvault.desktop.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.MenuScope
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/**
 * Desktop menu bar with File, Edit, View, and Help menus.
 * Provides platform-appropriate menu structure.
 */
@Composable
fun FrameWindowScope.DesktopMenuBar(
    sessionActionsEnabled: Boolean,
    onNewCredential: () -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onSettings: () -> Unit,
    onLock: () -> Unit,
    onSearch: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onToggleFullscreen: () -> Unit,
    onGenerator: () -> Unit,
    onHealth: () -> Unit,
    onClearClipboard: () -> Unit,
    onDocumentation: () -> Unit,
    onKeyboardShortcuts: () -> Unit,
    onAbout: () -> Unit,
    onQuit: () -> Unit,
) {
    MenuBar {
        Menu(
            text = stringResource(Res.string.desktop_menu_file),
            mnemonic = 'F',
        ) {
            FileMenuItems(
                sessionActionsEnabled,
                onNewCredential,
                onImport,
                onExport,
                onLock,
                onSettings,
                onQuit,
            )
        }
        Menu(
            text = stringResource(Res.string.desktop_menu_edit),
            mnemonic = 'E',
        ) {
            EditMenuItems(sessionActionsEnabled, onSearch)
        }
        Menu(
            text = stringResource(Res.string.desktop_menu_view),
            mnemonic = 'V',
        ) {
            ViewMenuItems(onToggleDarkMode, onToggleFullscreen)
        }
        Menu(
            text = stringResource(Res.string.desktop_menu_tools),
            mnemonic = 'T',
        ) {
            ToolsMenuItems(sessionActionsEnabled, onGenerator, onHealth, onClearClipboard)
        }
        Menu(
            text = stringResource(Res.string.desktop_menu_help),
            mnemonic = 'H',
        ) {
            HelpMenuItems(sessionActionsEnabled, onDocumentation, onKeyboardShortcuts, onAbout)
        }
    }
}

@Composable
private fun MenuScope.FileMenuItems(
    sessionActionsEnabled: Boolean,
    onNewCredential: () -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onLock: () -> Unit,
    onSettings: () -> Unit,
    onQuit: () -> Unit,
) {
    Item(
        text = stringResource(Res.string.desktop_menu_new_credential),
        mnemonic = 'N',
        shortcut = primaryShortcut(Key.N),
        enabled = sessionActionsEnabled,
        onClick = onNewCredential,
    )
    Separator()
    Item(
        stringResource(Res.string.desktop_menu_import),
        mnemonic = 'I',
        enabled = sessionActionsEnabled,
        onClick = onImport,
    )
    Item(
        stringResource(Res.string.desktop_menu_export),
        mnemonic = 'E',
        enabled = sessionActionsEnabled,
        onClick = onExport,
    )
    Separator()
    Item(
        text = stringResource(Res.string.desktop_menu_lock_vault),
        mnemonic = 'L',
        shortcut = primaryShortcut(Key.L),
        enabled = sessionActionsEnabled,
        onClick = onLock,
    )
    Separator()
    Item(
        text = stringResource(Res.string.desktop_menu_settings),
        mnemonic = ',',
        shortcut = primaryShortcut(Key.Comma),
        enabled = sessionActionsEnabled,
        onClick = onSettings,
    )
    Separator()
    Item(
        text = stringResource(Res.string.desktop_menu_quit),
        mnemonic = 'Q',
        shortcut = primaryShortcut(Key.Q),
        onClick = onQuit,
    )
}

@Composable
private fun MenuScope.EditMenuItems(sessionActionsEnabled: Boolean, onSearch: () -> Unit) {
    Item(
        text = stringResource(Res.string.desktop_menu_search),
        mnemonic = 'F',
        shortcut = primaryShortcut(Key.F),
        enabled = sessionActionsEnabled,
        onClick = onSearch,
    )
}

@Composable
private fun MenuScope.ViewMenuItems(
    onToggleDarkMode: () -> Unit,
    onToggleFullscreen: () -> Unit,
) {
    Item(
        text = stringResource(Res.string.desktop_menu_toggle_dark_mode),
        mnemonic = 'D',
        shortcut = primaryShortcut(Key.D, shift = true),
        onClick = onToggleDarkMode,
    )
    Separator()
    Item(
        text = stringResource(Res.string.desktop_menu_toggle_fullscreen),
        mnemonic = 'F',
        shortcut = KeyShortcut(Key.F11),
        onClick = onToggleFullscreen,
    )
}

@Composable
private fun MenuScope.ToolsMenuItems(
    sessionActionsEnabled: Boolean,
    onGenerator: () -> Unit,
    onHealth: () -> Unit,
    onClearClipboard: () -> Unit,
) {
    Item(
        text = stringResource(Res.string.desktop_menu_password_generator),
        mnemonic = 'G',
        shortcut = primaryShortcut(Key.G, shift = true),
        enabled = sessionActionsEnabled,
        onClick = onGenerator,
    )
    Item(
        text = stringResource(Res.string.desktop_menu_security_health),
        mnemonic = 'H',
        enabled = sessionActionsEnabled,
        onClick = onHealth,
    )
    Separator()
    Item(
        text = stringResource(Res.string.desktop_menu_clear_clipboard),
        mnemonic = 'B',
        shortcut = primaryShortcut(Key.B, shift = true),
        onClick = onClearClipboard,
    )
}

@Composable
private fun MenuScope.HelpMenuItems(
    sessionActionsEnabled: Boolean,
    onDocumentation: () -> Unit,
    onKeyboardShortcuts: () -> Unit,
    onAbout: () -> Unit,
) {
    Item(
        text = stringResource(Res.string.desktop_menu_documentation),
        mnemonic = 'D',
        shortcut = primaryShortcut(Key.Slash),
        enabled = sessionActionsEnabled,
        onClick = onDocumentation,
    )
    Item(
        text = stringResource(Res.string.desktop_menu_keyboard_shortcuts),
        mnemonic = 'K',
        enabled = sessionActionsEnabled,
        onClick = onKeyboardShortcuts,
    )
    Separator()
    Item(
        text = stringResource(Res.string.desktop_menu_about),
        mnemonic = 'A',
        enabled = sessionActionsEnabled,
        onClick = onAbout,
    )
}

private fun primaryShortcut(key: Key, shift: Boolean = false): KeyShortcut =
    if (System.getProperty("os.name").contains("mac", ignoreCase = true)) {
        KeyShortcut(key = key, meta = true, shift = shift)
    } else {
        KeyShortcut(key = key, ctrl = true, shift = shift)
    }
