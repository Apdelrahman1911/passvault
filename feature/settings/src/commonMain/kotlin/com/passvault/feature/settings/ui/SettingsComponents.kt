package com.passvault.feature.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.passvault.core.designsystem.components.EditorialPanel
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.ui_go_back
import com.passvault.core.designsystem.platform.passVaultScrollableTopAppBarInsets
import com.passvault.core.designsystem.platform.passVaultTopAppBarColors
import com.passvault.core.designsystem.theme.PassVaultTextStyles
import com.passvault.core.designsystem.tokens.Spacing
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScrollableTopBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = {},
        modifier = modifier,
        windowInsets = passVaultScrollableTopAppBarInsets(),
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.ui_go_back),
                )
            }
        },
        actions = actions,
        colors = passVaultTopAppBarColors(),
    )
}

@Composable
internal fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Text(
            text = title,
            style = PassVaultTextStyles.Eyebrow,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.sm),
        )
        EditorialPanel(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(Spacing.xs),
            content = content,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = Color.Transparent,
    ) {
        ListItem(
            headlineContent = { Text(title, style = MaterialTheme.typography.bodyLarge) },
            supportingContent = subtitle?.let { text ->
                {
                    Text(
                        text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            leadingContent = {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            },
            trailingContent = trailing,
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
    }
}
