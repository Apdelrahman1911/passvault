package com.passvault.feature.vault.ui.components

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
                    selectedContainerColor = tagColor.copy(alpha = 0.2f),
                    selectedLabelColor = tagColor
                )
            )
        }
    }
}

private fun parseTagColor(colorString: String?, fallback: Color): Color {
    if (colorString.isNullOrBlank()) {
        return fallback
    }
    return try {
        val hex = colorString.removePrefix("#")
        when (hex.length) {
            6 -> {
                val r = hex.substring(0, 2).toInt(16)
                val g = hex.substring(2, 4).toInt(16)
                val b = hex.substring(4, 6).toInt(16)
                Color(r, g, b)
            }
            8 -> {
                val a = hex.substring(0, 2).toInt(16)
                val r = hex.substring(2, 4).toInt(16)
                val g = hex.substring(4, 6).toInt(16)
                val b = hex.substring(6, 8).toInt(16)
                Color(r, g, b, a)
            }
            else -> fallback
        }
    } catch (e: Exception) {
        fallback
    }
}
