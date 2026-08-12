package com.passvault.feature.vault.ui.components

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertTrue

class TagCloudContrastTest {
    @Test
    fun `selected tag palette keeps readable text for arbitrary colors`() {
        val surfaces = listOf(Color.White, Color(0xFF121212))
        val tagColors = listOf(
            Color.White,
            Color.Black,
            Color(0xFF777777),
            Color(0xFFFFFF00),
            Color(0xFF0066FF),
            Color(0xFFFF66CC),
        )

        surfaces.forEach { surface ->
            tagColors.forEach { tagColor ->
                val palette = tagChipPalette(tagColor, surface)
                assertTrue(
                    contrastRatio(palette.contentColor, palette.containerColor) >= 4.5f,
                    "Expected readable contrast for tag=$tagColor on surface=$surface",
                )
            }
        }
    }
}
