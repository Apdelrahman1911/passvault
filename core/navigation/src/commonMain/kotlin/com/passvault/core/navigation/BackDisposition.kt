package com.passvault.core.navigation

/**
 * One decision shared by toolbar, system, predictive, gesture, keyboard, and programmatic Back.
 *
 * A renderer must expose a previous entry to an interactive transition only for [PopNow].
 */
enum class BackDisposition {
    PopNow,
    HandleInPlace,
    Blocked,
    ExitApplication,
}
