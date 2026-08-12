package com.passvault.desktop.components

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type

/**
 * Keyboard shortcuts handler for PassVault Desktop.
 * Manages global keyboard shortcuts for quick access to common features.
 */
class KeyboardShortcuts(
    private val sessionActionsEnabled: Boolean,
    private val onNewCredential: () -> Unit,
    private val onSearch: () -> Unit,
    private val onLock: () -> Unit,
    private val onGeneratePassword: () -> Unit,
    private val onSettings: () -> Unit,
    private val onHelp: () -> Unit,
    private val onQuit: () -> Unit,
) {
    /**
     * Handle a key event.
     * @return true if the event was consumed
     */
    fun handleKeyEvent(event: KeyEvent): Boolean = when {
        event.type != KeyEventType.KeyDown -> false
        else -> handleBuiltInShortcut(event)
    }

    private fun handleBuiltInShortcut(event: KeyEvent): Boolean {
        if (sessionActionsEnabled && handleSessionShortcut(event)) return true
        return handleGlobalShortcut(event)
    }

    private fun handleSessionShortcut(event: KeyEvent): Boolean = when {
        // Ctrl/Cmd+N - New Credential
        matchesShortcut(event, Key.N, ctrl = true) -> {
            onNewCredential()
            true
        }

        // Ctrl/Cmd+F or Ctrl/Cmd+K - Search/Find
        matchesShortcut(event, Key.F, ctrl = true) || matchesShortcut(event, Key.K, ctrl = true) -> {
            onSearch()
            true
        }

        // Ctrl/Cmd+L - Lock Vault
        matchesShortcut(event, Key.L, ctrl = true) -> {
            onLock()
            true
        }

        // Ctrl/Cmd+Shift+G - Generate Password
        matchesShortcut(event, Key.G, ctrl = true, shift = true) -> {
            onGeneratePassword()
            true
        }

        // Ctrl/Cmd+, - Settings
        matchesShortcut(event, Key.Comma, ctrl = true) -> {
            onSettings()
            true
        }

        // Ctrl/Cmd+/ - Help/Documentation
        matchesShortcut(event, Key.Slash, ctrl = true) -> {
            onHelp()
            true
        }

        else -> false
    }

    private fun handleGlobalShortcut(event: KeyEvent): Boolean = when {
        // Ctrl/Cmd+Q - Quit
        matchesShortcut(event, Key.Q, ctrl = true) -> {
            onQuit()
            true
        }

        else -> false
    }

    /**
     * Check if a key event matches the specified shortcut.
     */
    private fun matchesShortcut(
        event: KeyEvent,
        key: Key,
        ctrl: Boolean = false,
        shift: Boolean = false,
        alt: Boolean = false,
        meta: Boolean = false,
    ): Boolean {
        if (event.key != key) return false

        val isMac = System.getProperty("os.name").lowercase().contains("mac")

        return if (isMac) {
            // On macOS, use Command (Meta) instead of Ctrl for most shortcuts
            event.isMetaPressed == (ctrl || meta) &&
                !event.isCtrlPressed &&
                event.isShiftPressed == shift &&
                event.isAltPressed == alt
        } else {
            // On Windows/Linux, use Ctrl
            event.isCtrlPressed == ctrl &&
                event.isShiftPressed == shift &&
                event.isAltPressed == alt &&
                event.isMetaPressed == meta
        }
    }
}
