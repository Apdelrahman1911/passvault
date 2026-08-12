package com.passvault.shared.platform

import androidx.compose.ui.unit.Density

/** Identity-scoped density forces resource consumers to re-read the process locale without resetting UI state. */
internal class AppLanguageDensity(
    override val density: Float,
    override val fontScale: Float,
) : Density
