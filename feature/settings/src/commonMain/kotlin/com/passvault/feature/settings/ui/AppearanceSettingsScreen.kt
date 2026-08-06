package com.passvault.feature.settings.ui

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.passvault.core.designsystem.components.EditorialPageHeader
import com.passvault.core.designsystem.components.EditorialPanel
import com.passvault.core.designsystem.platform.passVaultTopAppBarColors
import com.passvault.core.designsystem.platform.scaffoldVerticalScroll
import com.passvault.core.designsystem.tokens.ComponentSpacing
import com.passvault.core.designsystem.tokens.Spacing
import com.passvault.core.designsystem.theme.PassVaultAccent
import com.passvault.core.designsystem.theme.previewColor
import com.passvault.feature.settings.presentation.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsScreen(
    state: SettingsViewModel.SettingsState,
    onEvent: (SettingsViewModel.SettingsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val systemDark = isSystemInDarkTheme()
    val usesDarkColors = when (state.theme) {
        SettingsViewModel.AppTheme.LIGHT -> false
        SettingsViewModel.AppTheme.DARK -> true
        SettingsViewModel.AppTheme.SYSTEM -> systemDark
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = {
                        onEvent(SettingsViewModel.SettingsEvent.OnBackClick)
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.ui_go_back)
                        )
                    }
                },
                colors = passVaultTopAppBarColors(),
            )
        },
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize(),
        ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 760.dp)
                .align(Alignment.TopCenter)
                .scaffoldVerticalScroll(rememberScrollState(), paddingValues)
                .padding(
                    horizontal = ComponentSpacing.screenHorizontal,
                    vertical = ComponentSpacing.screenVertical,
                ),
            verticalArrangement = Arrangement.spacedBy(ComponentSpacing.sectionSpacing),
        ) {
            EditorialPageHeader(
                eyebrow = stringResource(Res.string.action_settings),
                title = stringResource(Res.string.ui_appearance),
            )

            // Theme Selection
            EditorialPanel(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(Spacing.lg),
            ) {
                    Text(
                        text = stringResource(Res.string.ui_theme),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    ThemeOption(
                        icon = Icons.Default.LightMode,
                        title = stringResource(Res.string.ui_light),
                        description = stringResource(Res.string.ui_always_use_light_theme),
                        selected = state.theme == SettingsViewModel.AppTheme.LIGHT,
                        onClick = {
                            onEvent(SettingsViewModel.SettingsEvent.OnThemeChanged(SettingsViewModel.AppTheme.LIGHT))
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    ThemeOption(
                        icon = Icons.Default.DarkMode,
                        title = stringResource(Res.string.ui_dark),
                        description = stringResource(Res.string.ui_always_use_dark_theme),
                        selected = state.theme == SettingsViewModel.AppTheme.DARK,
                        onClick = {
                            onEvent(SettingsViewModel.SettingsEvent.OnThemeChanged(SettingsViewModel.AppTheme.DARK))
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    ThemeOption(
                        icon = Icons.Default.BrightnessAuto,
                        title = stringResource(Res.string.ui_system_default),
                        description = stringResource(Res.string.ui_follow_system_theme_setting),
                        selected = state.theme == SettingsViewModel.AppTheme.SYSTEM,
                        onClick = {
                            onEvent(SettingsViewModel.SettingsEvent.OnThemeChanged(SettingsViewModel.AppTheme.SYSTEM))
                        }
                    )
            }

            EditorialPanel(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(Spacing.lg),
            ) {
                Text(
                    text = stringResource(Res.string.ui_main_color),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(Res.string.ui_main_color_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                PassVaultAccent.entries.chunked(2).forEach { accents ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    ) {
                        accents.forEach { accent ->
                            AccentColorOption(
                                accent = accent,
                                darkTheme = usesDarkColors,
                                selected = state.accentColor == accent,
                                onClick = {
                                    onEvent(SettingsViewModel.SettingsEvent.OnAccentColorChanged(accent))
                                },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (accents.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            // Preview Card
            ThemePreviewCard(
                modifier = Modifier.fillMaxWidth()
            )
        }
        }
    }
}

@Composable
private fun AccentColorOption(
    accent: PassVaultAccent,
    darkTheme: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        contentColor = if (selected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Row(
            modifier = Modifier.padding(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Surface(
                modifier = Modifier.size(30.dp),
                shape = CircleShape,
                color = accent.previewColor(darkTheme),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {}
            Text(
                text = accentLabel(accent),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.weight(1f),
            )
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(Res.string.ui_selected),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun accentLabel(accent: PassVaultAccent): String = when (accent) {
    PassVaultAccent.NEUTRAL -> stringResource(Res.string.ui_color_neutral)
    PassVaultAccent.SAGE -> stringResource(Res.string.ui_color_sage)
    PassVaultAccent.BLUE -> stringResource(Res.string.ui_color_blue)
    PassVaultAccent.PURPLE -> stringResource(Res.string.ui_color_purple)
    PassVaultAccent.ROSE -> stringResource(Res.string.ui_color_rose)
    PassVaultAccent.AMBER -> stringResource(Res.string.ui_color_amber)
}

@Composable
private fun ThemeOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .clickable { onClick() }
            .padding(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = MaterialTheme.shapes.medium,
            color = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (selected) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = stringResource(Res.string.ui_selected),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ThemePreviewCard(
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(Res.string.ui_preview),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Sample UI elements
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(Res.string.ui_sample_card),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(Res.string.ui_this_is_how_cards_and_text_will_appear_with_your_selec),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row {
                        Button(onClick = {}, enabled = false) {
                            Text(stringResource(Res.string.ui_button))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(onClick = {}, enabled = false) {
                            Text(stringResource(Res.string.ui_outline))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Color palette preview
            Text(
                text = stringResource(Res.string.ui_color_palette),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ColorSwatch(
                    color = MaterialTheme.colorScheme.primary,
                    label = stringResource(Res.string.ui_primary)
                )
                ColorSwatch(
                    color = MaterialTheme.colorScheme.secondary,
                    label = stringResource(Res.string.ui_secondary)
                )
                ColorSwatch(
                    color = MaterialTheme.colorScheme.tertiary,
                    label = stringResource(Res.string.ui_tertiary)
                )
                ColorSwatch(
                    color = MaterialTheme.colorScheme.error,
                    label = stringResource(Res.string.ui_error)
                )
            }
        }
    }
}

@Composable
private fun ColorSwatch(
    color: androidx.compose.ui.graphics.Color,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = MaterialTheme.shapes.small,
            color = color
        ) {}
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
