package com.passvault.feature.health.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.passvault.core.designsystem.text.resolve
import com.passvault.core.designsystem.tokens.ComponentSpacing
import com.passvault.core.designsystem.tokens.Spacing
import com.passvault.feature.health.presentation.HealthViewModel
import com.passvault.feature.health.presentation.HealthViewModel.HealthTab

@Composable
internal fun HealthTabSelector(
    state: HealthViewModel.HealthState,
    onEvent: (HealthViewModel.HealthEvent) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.extraLarge,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        BoxWithConstraints(modifier = Modifier.padding(Spacing.xs)) {
            if (maxWidth >= 680.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    HealthTab.entries.forEach { tab ->
                        HealthTabButton(
                            tab = tab,
                            count = state.countFor(tab),
                            selected = state.selectedTab == tab,
                            onClick = { onEvent(HealthViewModel.HealthEvent.OnTabChanged(tab)) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    items(HealthTab.entries, key = HealthTab::name) { tab ->
                        HealthTabButton(
                            tab = tab,
                            count = state.countFor(tab),
                            selected = state.selectedTab == tab,
                            onClick = { onEvent(HealthViewModel.HealthEvent.OnTabChanged(tab)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthTabButton(
    tab: HealthTab,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        modifier = modifier
            .defaultMinSize(minHeight = ComponentSpacing.touchTargetMin)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick),
        color = containerColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(tab.icon(), contentDescription = null, modifier = Modifier.size(20.dp))
            Text(
                tab.displayName.resolve(),
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
            )
            if (count > 0) HealthTabCount(count = count, selected = selected)
        }
    }
}

@Composable
private fun HealthTabCount(count: Int, selected: Boolean) {
    Surface(
        color = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest
        },
        contentColor = if (selected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        shape = CircleShape,
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
        )
    }
}

private fun HealthViewModel.HealthState.countFor(tab: HealthTab): Int = when (tab) {
    HealthTab.OVERVIEW -> 0
    HealthTab.WEAK_PASSWORDS -> weakPasswords.size
    HealthTab.DUPLICATES -> duplicatePasswords.size
    HealthTab.OLD_PASSWORDS -> oldPasswords.size
}

private fun HealthTab.icon(): ImageVector = when (this) {
    HealthTab.OVERVIEW -> Icons.Default.Info
    HealthTab.WEAK_PASSWORDS -> Icons.Default.Warning
    HealthTab.DUPLICATES -> Icons.Default.ContentCopy
    HealthTab.OLD_PASSWORDS -> Icons.Default.Schedule
}
