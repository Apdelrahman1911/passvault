package com.passvault.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ThemeColorSchemeTest {
    @Test
    fun darkThemeUsesPureBlackCanvasForEveryAccent() {
        PassVaultAccent.entries.forEach { accent ->
            assertEquals(Color.Black, colorSchemeFor(darkTheme = true, accent = accent).background)
        }
    }

    @Test
    fun everyAccentHasDistinctLightAndDarkPrimaryColors() {
        val lightColors = PassVaultAccent.entries.map { colorSchemeFor(false, it).primary }
        val darkColors = PassVaultAccent.entries.map { colorSchemeFor(true, it).primary }

        assertEquals(PassVaultAccent.entries.size, lightColors.distinct().size)
        assertEquals(PassVaultAccent.entries.size, darkColors.distinct().size)
    }

    @Test
    fun accentTextRolesMeetReadableContrast() {
        listOf(false, true).forEach { darkTheme ->
            PassVaultAccent.entries.forEach { accent ->
                val scheme = colorSchemeFor(darkTheme, accent)
                assertTrue(
                    contrastRatio(scheme.primary, scheme.onPrimary) >= MIN_TEXT_CONTRAST,
                    "$accent primary contrast is too low for darkTheme=$darkTheme",
                )
                assertTrue(
                    contrastRatio(scheme.primaryContainer, scheme.onPrimaryContainer) >= MIN_TEXT_CONTRAST,
                    "$accent container contrast is too low for darkTheme=$darkTheme",
                )
            }
        }
    }

    private fun contrastRatio(first: Color, second: Color): Double {
        val firstLuminance = first.relativeLuminance()
        val secondLuminance = second.relativeLuminance()
        return (max(firstLuminance, secondLuminance) + 0.05) /
            (min(firstLuminance, secondLuminance) + 0.05)
    }

    private fun Color.relativeLuminance(): Double =
        0.2126 * red.toDouble().linearized() +
            0.7152 * green.toDouble().linearized() +
            0.0722 * blue.toDouble().linearized()

    private fun Double.linearized(): Double =
        if (this <= 0.04045) this / 12.92 else ((this + 0.055) / 1.055).pow(2.4)

    private companion object {
        const val MIN_TEXT_CONTRAST = 4.5
    }
}
