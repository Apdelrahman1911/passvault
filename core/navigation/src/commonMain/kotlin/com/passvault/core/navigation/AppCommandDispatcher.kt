package com.passvault.core.navigation

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Bridges platform menus and keyboard shortcuts to the shared navigation host.
 */
class AppCommandDispatcher {
    // Platform callbacks are non-suspending. A channel preserves every menu
    // or keyboard command until the single application collector handles it;
    // MutableSharedFlow.tryEmit could silently discard commands during bursts.
    private val commandChannel = Channel<AppCommand>(capacity = Channel.UNLIMITED)
    val commands = commandChannel.receiveAsFlow()

    fun dispatch(command: AppCommand) {
        check(commandChannel.trySend(command).isSuccess) {
            "Application command channel is unavailable"
        }
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
