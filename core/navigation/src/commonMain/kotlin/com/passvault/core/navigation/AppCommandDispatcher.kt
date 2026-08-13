package com.passvault.core.navigation

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Bridges platform menus and keyboard shortcuts to the shared navigation host.
 */
class AppCommandDispatcher {
    // Native commands are transient UI input, not durable work. A bounded, no-replay flow prevents
    // stale menu clicks from navigating after authentication or after the host is recreated.
    private val mutableCommands = MutableSharedFlow<AppCommand>(
        replay = 0,
        extraBufferCapacity = COMMAND_BUFFER_CAPACITY,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val commands = mutableCommands.asSharedFlow()

    fun dispatch(command: AppCommand) {
        mutableCommands.tryEmit(command)
    }

    private companion object {
        const val COMMAND_BUFFER_CAPACITY = 16
    }
}

enum class AppCommand {
    NEW_CREDENTIAL,
    SEARCH,
    GENERATOR,
    HEALTH,
    SETTINGS,
    TOGGLE_THEME,
    IMPORT,
    EXPORT,
    HELP,
    ABOUT,
    CLEAR_CLIPBOARD,
}
