package com.passvault.desktop

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.application

/** Return to Main's terminal cleanup before its explicit process exit. */
internal fun runDesktopApplicationLoop(content: @Composable ApplicationScope.() -> Unit) {
    application(exitProcessOnExit = false, content = content)
}
