package com.passvault.core.designsystem.platform

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

actual val drawsBehindSystemBars: Boolean = false

@Composable
actual fun ConfigureSystemBarAppearance(darkTheme: Boolean) {
    val view = LocalView.current
    val window = view.context.findActivity()?.window ?: return

    SideEffect {
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !darkTheme
            isAppearanceLightNavigationBars = !darkTheme
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
