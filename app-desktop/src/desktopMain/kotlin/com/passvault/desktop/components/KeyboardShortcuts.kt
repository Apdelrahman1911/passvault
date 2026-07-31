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
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Keyboard shortcuts handler for PassVault Desktop.
 * Manages global keyboard shortcuts for quick access to common features.
 */
class KeyboardShortcuts(
    private val onNewCredential: () -> Unit,
    private val onSearch: () -> Unit,
    private val onLock: () -> Unit,
    private val onGeneratePassword: () -> Unit,
    private val onSettings: () -> Unit,
    private val onHelp: () -> Unit,
    private val onBack: () -> Unit,
    private val onQuit: () -> Unit,
) {

    private val shortcutHandlers = CopyOnWriteArrayList<ShortcutHandler>()
    private var isEnabled = true

    /**
     * Handle a key event.
     * @return true if the event was consumed
     */
    fun handleKeyEvent(event: KeyEvent): Boolean {
        if (!isEnabled) return false
        if (event.type != KeyEventType.KeyDown) return false

        // Check registered handlers first
        shortcutHandlers.forEach { handler ->
            if (handler.matches(event)) {
                handler.action()
                return true
            }
        }

        // Check built-in shortcuts
        return when {
            // Ctrl/Cmd+N - New Credential
            matchesShortcut(event, Key.N, ctrl = true) -> {
                onNewCredential()
                true
            }

            // Ctrl/Cmd+F or Ctrl/Cmd+K - Search/Find
            matchesShortcut(event, Key.F, ctrl = true) ||
                matchesShortcut(event, Key.K, ctrl = true) -> {
                onSearch()
                true
            }

            // Ctrl/Cmd+L - Lock Vault
            matchesShortcut(event, Key.L, ctrl = true) -> {
                onLock()
                true
            }

            // Ctrl/Cmd+G - Generate Password
            matchesShortcut(event, Key.G, ctrl = true) -> {
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

            // Ctrl/Cmd+Q - Quit
            matchesShortcut(event, Key.Q, ctrl = true) -> {
                onQuit()
                true
            }

            // Esc - Cancel/Back
            event.key == Key.Escape -> {
                onBack()
                true
            }

            else -> false
        }
    }

    /**
     * Register a custom shortcut handler.
     */
    fun registerShortcut(handler: ShortcutHandler) {
        shortcutHandlers.add(handler)
    }

    /**
     * Unregister a shortcut handler.
     */
    fun unregisterShortcut(handler: ShortcutHandler) {
        shortcutHandlers.remove(handler)
    }

    /**
     * Enable shortcuts.
     */
    fun enable() {
        isEnabled = true
    }

    /**
     * Disable shortcuts.
     */
    fun disable() {
        isEnabled = false
    }

    /**
     * Check if shortcuts are enabled.
     */
    fun isEnabled(): Boolean = isEnabled

    /**
     * Trigger new credential action.
     */
    fun triggerNewCredential() = onNewCredential()

    /**
     * Trigger search action.
     */
    fun triggerSearch() = onSearch()

    /**
     * Trigger lock action.
     */
    fun triggerLock() = onLock()

    /**
     * Trigger generate password action.
     */
    fun triggerGeneratePassword() = onGeneratePassword()

    /**
     * Trigger settings action.
     */
    fun triggerSettings() = onSettings()

    /**
     * Trigger help action.
     */
    fun triggerHelp() = onHelp()

    /**
     * Trigger quit action.
     */
    fun triggerQuit() = onQuit()

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

/**
 * Handler for a keyboard shortcut.
 */
interface ShortcutHandler {
    /**
     * Check if this handler matches the given key event.
     */
    fun matches(event: KeyEvent): Boolean

    /**
     * Execute the shortcut action.
     */
    fun action()
}

/**
 * Builder for creating shortcut handlers.
 */
class ShortcutHandlerBuilder {
    private var key: Key? = null
    private var ctrl: Boolean = false
    private var shift: Boolean = false
    private var alt: Boolean = false
    private var meta: Boolean = false
    private var action: (() -> Unit)? = null

    fun key(key: Key) = apply { this.key = key }
    fun ctrl(value: Boolean = true) = apply { this.ctrl = value }
    fun shift(value: Boolean = true) = apply { this.shift = value }
    fun alt(value: Boolean = true) = apply { this.alt = value }
    fun meta(value: Boolean = true) = apply { this.meta = value }
    fun action(block: () -> Unit) = apply { this.action = block }

    fun build(): ShortcutHandler {
        require(key != null) { "Key must be set" }
        require(action != null) { "Action must be set" }

        val targetKey = key!!
        val targetCtrl = ctrl
        val targetShift = shift
        val targetAlt = alt
        val targetMeta = meta
        val targetAction = action!!

        return object : ShortcutHandler {
            override fun matches(event: KeyEvent): Boolean {
                if (event.key != targetKey) return false

                val isMac = System.getProperty("os.name").lowercase().contains("mac")

                return if (isMac) {
                    event.isMetaPressed == (targetCtrl || targetMeta) &&
                        event.isShiftPressed == targetShift &&
                        event.isAltPressed == targetAlt
                } else {
                    event.isCtrlPressed == targetCtrl &&
                        event.isShiftPressed == targetShift &&
                        event.isAltPressed == targetAlt &&
                        event.isMetaPressed == targetMeta
                }
            }

            override fun action() = targetAction()
        }
    }
}

/**
 * Global shortcuts that can be registered.
 */
object GlobalShortcuts {
    const val NEW_CREDENTIAL = "new_credential"
    const val SEARCH = "search"
    const val LOCK = "lock"
    const val GENERATE_PASSWORD = "generate_password"
    const val SETTINGS = "settings"
    const val HELP = "help"
    const val QUIT = "quit"
    const val COPY_USERNAME = "copy_username"
    const val COPY_PASSWORD = "copy_password"
    const val COPY_URL = "copy_url"
    const val EDIT_CREDENTIAL = "edit_credential"
    const val DELETE_CREDENTIAL = "delete_credential"
    const val NAVIGATE_UP = "navigate_up"
    const val NAVIGATE_DOWN = "navigate_down"
    const val NAVIGATE_LEFT = "navigate_left"
    const val NAVIGATE_RIGHT = "navigate_right"
    const val EXPAND_ALL = "expand_all"
    const val COLLAPSE_ALL = "collapse_all"
    const val TOGGLE_STARRED = "toggle_starred"
    const val QUICK_FILTER = "quick_filter"
}

/**
 * Default shortcut bindings.
 */
object DefaultShortcutBindings {
    val bindings = mapOf(
        GlobalShortcuts.NEW_CREDENTIAL to ShortcutBinding(Key.N, ctrl = true),
        GlobalShortcuts.SEARCH to ShortcutBinding(Key.F, ctrl = true),
        GlobalShortcuts.LOCK to ShortcutBinding(Key.L, ctrl = true),
        GlobalShortcuts.GENERATE_PASSWORD to ShortcutBinding(Key.G, ctrl = true, shift = true),
        GlobalShortcuts.SETTINGS to ShortcutBinding(Key.Comma, ctrl = true),
        GlobalShortcuts.HELP to ShortcutBinding(Key.Slash, ctrl = true),
        GlobalShortcuts.QUIT to ShortcutBinding(Key.Q, ctrl = true),
        GlobalShortcuts.COPY_USERNAME to ShortcutBinding(Key.U, ctrl = true, alt = true),
        GlobalShortcuts.COPY_PASSWORD to ShortcutBinding(Key.P, ctrl = true, alt = true),
        GlobalShortcuts.COPY_URL to ShortcutBinding(Key.L, ctrl = true, alt = true),
        GlobalShortcuts.EDIT_CREDENTIAL to ShortcutBinding(Key.E, ctrl = true),
        GlobalShortcuts.DELETE_CREDENTIAL to ShortcutBinding(Key.Delete),
        GlobalShortcuts.NAVIGATE_UP to ShortcutBinding(Key.DirectionUp),
        GlobalShortcuts.NAVIGATE_DOWN to ShortcutBinding(Key.DirectionDown),
        GlobalShortcuts.TOGGLE_STARRED to ShortcutBinding(Key.S, ctrl = true, shift = true),
        GlobalShortcuts.QUICK_FILTER to ShortcutBinding(Key.K, ctrl = true, shift = true),
    )
}

/**
 * Data class representing a keyboard shortcut.
 */
data class ShortcutBinding(
    val key: Key,
    val ctrl: Boolean = false,
    val shift: Boolean = false,
    val alt: Boolean = false,
    val meta: Boolean = false,
)
