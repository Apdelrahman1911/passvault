package com.passvault.core.designsystem.platform

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/** Whether this platform overlays its system bars on the Compose scene. */
expect val drawsBehindSystemBars: Boolean

/** Keeps native system-bar icon contrast aligned with the in-app theme. */
@Composable
expect fun ConfigureSystemBarAppearance(darkTheme: Boolean)

/**
 * Keeps the iOS scroll viewport edge-to-edge while treating Scaffold padding
 * as scrollable content spacing. Other platforms retain their existing layout.
 */
fun Modifier.scaffoldVerticalScroll(
    state: ScrollState,
    paddingValues: PaddingValues,
): Modifier = if (drawsBehindSystemBars) {
    verticalScroll(state)
        .padding(paddingValues)
        .consumeWindowInsets(paddingValues)
} else {
    padding(paddingValues)
        .verticalScroll(state)
}

/** Keeps a lazy viewport full-screen on iOS without losing safe-area insets. */
fun Modifier.scaffoldLazyViewport(paddingValues: PaddingValues): Modifier =
    if (drawsBehindSystemBars) {
        consumeWindowInsets(paddingValues)
    } else {
        padding(paddingValues)
    }

/** Makes fixed iOS app bars reveal content scrolling beneath the status bar. */
@Composable
fun passVaultTopAppBarColors(): TopAppBarColors {
    val containerColor = if (drawsBehindSystemBars) {
        Color.Transparent
    } else {
        MaterialTheme.colorScheme.background
    }
    return TopAppBarDefaults.topAppBarColors(
        containerColor = containerColor,
        scrolledContainerColor = containerColor,
    )
}

/** Avoids applying the iOS status-bar inset twice to an app bar inside a scroll container. */
@Composable
fun passVaultScrollableTopAppBarInsets(): WindowInsets =
    if (drawsBehindSystemBars) {
        WindowInsets(0, 0, 0, 0)
    } else {
        TopAppBarDefaults.windowInsets
    }
