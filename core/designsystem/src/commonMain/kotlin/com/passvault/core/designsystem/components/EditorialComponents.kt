package com.passvault.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.passvault.core.designsystem.theme.PassVaultTextStyles
import com.passvault.core.designsystem.tokens.ComponentSpacing
import com.passvault.core.designsystem.tokens.Spacing

/**
 * Editorial page title with a compact security-context label and optional actions.
 */
@Composable
fun EditorialPageHeader(
    eyebrow: String,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            Text(
                text = eyebrow,
                style = PassVaultTextStyles.Eyebrow,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.semantics { heading() },
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            content = actions,
        )
    }
}

/**
 * Flat, outlined surface used for primary content groups.
 */
@Composable
fun EditorialPanel(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    contentPadding: PaddingValues = PaddingValues(ComponentSpacing.cardPadding),
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = containerColor,
        contentColor = contentColor,
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(ComponentSpacing.cardContentSpacing),
            content = content,
        )
    }
}

/**
 * Shape-safe icon container used by list rows, onboarding highlights, and metrics.
 */
@Composable
fun EditorialIconTile(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    circular: Boolean = false,
) {
    Surface(
        modifier = modifier.size(48.dp),
        shape = if (circular) CircleShape else MaterialTheme.shapes.medium,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

/**
 * Semantic inline feedback panel. Icon, title, and text ensure the state is never communicated by color alone.
 */
@Composable
fun EditorialStatusBanner(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    action: (@Composable RowScope.() -> Unit)? = null,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite },
        shape = MaterialTheme.shapes.large,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Column(
            modifier = Modifier.padding(Spacing.mdLg),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.smMd),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    Text(title, style = MaterialTheme.typography.titleSmall)
                    Text(
                        message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor.copy(alpha = 0.8f),
                    )
                }
            }
            if (action != null) {
                Row(
                    modifier = Modifier.align(Alignment.End),
                    content = action,
                )
            }
        }
    }
}
