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
        // File Menu
        Menu(
            text = stringResource(Res.string.desktop_menu_file),
            mnemonic = 'F',
        ) {
            Item(
                text = stringResource(Res.string.desktop_menu_new_credential),
                mnemonic = 'N',
                shortcut = KeyShortcut(Key.N, ctrl = true),
                onClick = onNewCredential,
            )
            Separator()
            Item(
                text = stringResource(Res.string.desktop_menu_import),
                mnemonic = 'I',
                onClick = onImport,
            )
            Item(
                text = stringResource(Res.string.desktop_menu_export),
                mnemonic = 'E',
                onClick = onExport,
            )
            Separator()
            Item(
                text = stringResource(Res.string.desktop_menu_lock_vault),
                mnemonic = 'L',
                shortcut = KeyShortcut(Key.L, ctrl = true),
                onClick = onLock,
            )
            Separator()
            Item(
                text = stringResource(Res.string.desktop_menu_settings),
                mnemonic = ',',
                shortcut = KeyShortcut(Key.Comma, ctrl = true),
                onClick = onSettings,
            )
            Separator()
            Item(
                text = stringResource(Res.string.desktop_menu_quit),
                mnemonic = 'Q',
                shortcut = KeyShortcut(Key.Q, ctrl = true),
                onClick = onQuit,
            )
        }

        // Edit Menu
        Menu(
            text = stringResource(Res.string.desktop_menu_edit),
            mnemonic = 'E',
        ) {
            Item(
                text = stringResource(Res.string.desktop_menu_search),
                mnemonic = 'F',
                shortcut = KeyShortcut(Key.F, ctrl = true),
                onClick = onSearch,
            )
        }

        // View Menu
        Menu(
            text = stringResource(Res.string.desktop_menu_view),
            mnemonic = 'V',
        ) {
            Item(
                text = stringResource(Res.string.desktop_menu_toggle_dark_mode),
                mnemonic = 'D',
                shortcut = KeyShortcut(Key.D, ctrl = true, shift = true),
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

        // Tools Menu
        Menu(
            text = stringResource(Res.string.desktop_menu_tools),
            mnemonic = 'T',
        ) {
            Item(
                text = stringResource(Res.string.desktop_menu_password_generator),
                mnemonic = 'G',
                shortcut = KeyShortcut(Key.G, ctrl = true, shift = true),
                onClick = onGenerator,
            )
            Item(
                text = stringResource(Res.string.desktop_menu_security_health),
                mnemonic = 'H',
                onClick = onHealth,
            )
            Separator()
            Item(
                text = stringResource(Res.string.desktop_menu_clear_clipboard),
                mnemonic = 'B',
                shortcut = KeyShortcut(Key.B, ctrl = true, shift = true),
                onClick = onClearClipboard,
            )
        }

        // Help Menu
        Menu(
            text = stringResource(Res.string.desktop_menu_help),
            mnemonic = 'H',
        ) {
            Item(
                text = stringResource(Res.string.desktop_menu_documentation),
                mnemonic = 'D',
                shortcut = KeyShortcut(Key.Slash, ctrl = true),
                onClick = onDocumentation,
            )
            Item(
                text = stringResource(Res.string.desktop_menu_keyboard_shortcuts),
                mnemonic = 'K',
                onClick = onKeyboardShortcuts,
            )
            Separator()
            Item(
                text = stringResource(Res.string.desktop_menu_about),
                mnemonic = 'A',
                onClick = onAbout,
            )
        }
    }
}

