package com.passvault.core.navigation

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Bridges platform menus and keyboard shortcuts to the shared navigation host.
 */
class AppCommandDispatcher {
    private val _commands = MutableSharedFlow<AppCommand>(extraBufferCapacity = 16)
    val commands = _commands.asSharedFlow()

    fun dispatch(command: AppCommand) {
        _commands.tryEmit(command)
    }
}

enum class AppCommand {
    NEW_CREDENTIAL,
    SEARCH,
    LOCK,
    GENERATOR,
    HEALTH,
    SETTINGS,
    TOGGLE_THEME,
    IMPORT,
    EXPORT,
    HELP,
    ABOUT,
    CLEAR_CLIPBOARD,
    BACK,
}
