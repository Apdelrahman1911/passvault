package com.passvault.feature.vault.ui.components

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.passvault.core.domain.model.Tag
import com.passvault.core.domain.model.TagId

@Composable
fun TagCloud(
    tags: List<Tag>,
    selectedTagId: TagId?,
    onTagSelected: (TagId?) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        item {
            FilterChip(
                selected = selectedTagId == null,
                onClick = { onTagSelected(null) },
                label = { Text(stringResource(Res.string.ui_all)) },
                leadingIcon = if (selectedTagId == null) {
                    {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Label,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else null
            )
        }

        items(tags, key = { it.id.value }) { tag ->
            val isSelected = tag.id == selectedTagId
            val tagColor = parseTagColor(
                colorString = tag.color,
                fallback = MaterialTheme.colorScheme.primary,
            )
            val selectedPalette = tagChipPalette(
                tagColor = tagColor,
                surfaceColor = MaterialTheme.colorScheme.surface,
            )

            FilterChip(
                selected = isSelected,
                onClick = {
                    onTagSelected(if (isSelected) null else tag.id)
                },
                label = { Text(tag.name) },
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = selectedPalette.containerColor,
                    selectedLabelColor = selectedPalette.contentColor,
                    selectedLeadingIconColor = selectedPalette.contentColor,
                )
            )
        }
    }
}

internal data class TagChipPalette(
    val containerColor: Color,
    val contentColor: Color,
)

internal fun tagChipPalette(tagColor: Color, surfaceColor: Color): TagChipPalette {
    val containerColor = tagColor
        .copy(alpha = 0.2f)
        .compositeOver(surfaceColor)
    val blackContrast = contrastRatio(Color.Black, containerColor)
    val whiteContrast = contrastRatio(Color.White, containerColor)
    return TagChipPalette(
        containerColor = containerColor,
        contentColor = if (blackContrast >= whiteContrast) Color.Black else Color.White,
    )
}

internal fun contrastRatio(foreground: Color, background: Color): Float {
    val foregroundLuminance = foreground.compositeOver(background).luminance()
    val backgroundLuminance = background.luminance()
    val lighter = maxOf(foregroundLuminance, backgroundLuminance)
    val darker = minOf(foregroundLuminance, backgroundLuminance)
    return (lighter + 0.05f) / (darker + 0.05f)
}

private fun parseTagColor(colorString: String?, fallback: Color): Color {
    val hex = colorString?.takeUnless(String::isBlank)?.removePrefix("#")
    return hex?.let(::parseHexColor) ?: fallback
}

private fun parseHexColor(hex: String): Color? {
    val components = hex.chunked(2).mapNotNull { it.toIntOrNull(radix = 16) }
    return when {
        hex.length == 6 && components.size == 3 -> Color(
            red = components[0],
            green = components[1],
            blue = components[2],
        )
        hex.length == 8 && components.size == 4 -> Color(
            red = components[1],
            green = components[2],
            blue = components[3],
            alpha = components[0],
        )
        else -> null
    }
}
