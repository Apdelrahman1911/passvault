package com.passvault.feature.settings.ui

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.passvault.core.designsystem.components.EditorialPageHeader
import com.passvault.core.designsystem.components.EditorialPanel
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
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            AppearanceContent(
                state = state,
                usesDarkColors = usesDarkColors,
                onEvent = onEvent,
                paddingValues = paddingValues,
            )
        }
    }
}

@Composable
private fun AppearanceContent(
    state: SettingsViewModel.SettingsState,
    usesDarkColors: Boolean,
    onEvent: (SettingsViewModel.SettingsEvent) -> Unit,
    paddingValues: PaddingValues,
) {
    Column(
        modifier = Modifier
            .widthIn(max = 760.dp)
            .fillMaxWidth()
            .scaffoldVerticalScroll(rememberScrollState(), paddingValues)
            .padding(
                horizontal = ComponentSpacing.screenHorizontal,
                vertical = ComponentSpacing.screenVertical,
        ),
        verticalArrangement = Arrangement.spacedBy(ComponentSpacing.sectionSpacing),
    ) {
        SettingsScrollableTopBar(
            onBack = { onEvent(SettingsViewModel.SettingsEvent.OnBackClick) },
        )
        EditorialPageHeader(
            eyebrow = stringResource(Res.string.action_settings),
            title = stringResource(Res.string.ui_appearance),
        )
        ThemeSelectionPanel(
            selectedTheme = state.theme,
            onThemeChanged = { theme ->
                onEvent(SettingsViewModel.SettingsEvent.OnThemeChanged(theme))
            },
        )
        LanguageSelectionPanel(
            selectedLanguage = state.language,
            onLanguageChanged = { language ->
                onEvent(SettingsViewModel.SettingsEvent.OnLanguageChanged(language))
            },
        )
        AccentSelectionPanel(
            selectedAccent = state.accentColor,
            darkTheme = usesDarkColors,
            onAccentChanged = { accent ->
                onEvent(SettingsViewModel.SettingsEvent.OnAccentColorChanged(accent))
            },
        )
        ThemePreviewCard(modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun LanguageSelectionPanel(
    selectedLanguage: SettingsViewModel.AppLanguage,
    onLanguageChanged: (SettingsViewModel.AppLanguage) -> Unit,
) {
    EditorialPanel(
        modifier = Modifier
            .fillMaxWidth()
            .selectableGroup(),
        contentPadding = PaddingValues(Spacing.lg),
    ) {
        Text(
            text = stringResource(Res.string.ui_app_language),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp),
        )
        ThemeOption(
            icon = Icons.Default.BrightnessAuto,
            title = stringResource(Res.string.ui_system_default),
            description = stringResource(Res.string.ui_follow_system_language),
            selected = selectedLanguage == SettingsViewModel.AppLanguage.SYSTEM,
            onClick = { onLanguageChanged(SettingsViewModel.AppLanguage.SYSTEM) },
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        ThemeOption(
            icon = Icons.Default.Language,
            title = stringResource(Res.string.ui_language_english),
            description = stringResource(Res.string.ui_use_english),
            selected = selectedLanguage == SettingsViewModel.AppLanguage.ENGLISH,
            onClick = { onLanguageChanged(SettingsViewModel.AppLanguage.ENGLISH) },
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        ThemeOption(
            icon = Icons.Default.Language,
            title = stringResource(Res.string.ui_language_arabic),
            description = stringResource(Res.string.ui_use_arabic),
            selected = selectedLanguage == SettingsViewModel.AppLanguage.ARABIC,
            onClick = { onLanguageChanged(SettingsViewModel.AppLanguage.ARABIC) },
        )
    }
}

@Composable
private fun ThemeSelectionPanel(
    selectedTheme: SettingsViewModel.AppTheme,
    onThemeChanged: (SettingsViewModel.AppTheme) -> Unit,
) {
    EditorialPanel(
        modifier = Modifier
            .fillMaxWidth()
            .selectableGroup(),
        contentPadding = PaddingValues(Spacing.lg),
    ) {
        Text(
            text = stringResource(Res.string.ui_theme),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp),
        )
        ThemeOption(
            icon = Icons.Default.LightMode,
            title = stringResource(Res.string.ui_light),
            description = stringResource(Res.string.ui_always_use_light_theme),
            selected = selectedTheme == SettingsViewModel.AppTheme.LIGHT,
            onClick = { onThemeChanged(SettingsViewModel.AppTheme.LIGHT) },
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        ThemeOption(
            icon = Icons.Default.DarkMode,
            title = stringResource(Res.string.ui_dark),
            description = stringResource(Res.string.ui_always_use_dark_theme),
            selected = selectedTheme == SettingsViewModel.AppTheme.DARK,
            onClick = { onThemeChanged(SettingsViewModel.AppTheme.DARK) },
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        ThemeOption(
            icon = Icons.Default.BrightnessAuto,
            title = stringResource(Res.string.ui_system_default),
            description = stringResource(Res.string.ui_follow_system_theme_setting),
            selected = selectedTheme == SettingsViewModel.AppTheme.SYSTEM,
            onClick = { onThemeChanged(SettingsViewModel.AppTheme.SYSTEM) },
        )
    }
}

@Composable
private fun AccentSelectionPanel(
    selectedAccent: PassVaultAccent,
    darkTheme: Boolean,
    onAccentChanged: (PassVaultAccent) -> Unit,
) {
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
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup(),
        ) {
            val columnCount = if (maxWidth < 360.dp) 1 else 2
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                PassVaultAccent.entries.chunked(columnCount).forEach { accents ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    ) {
                        accents.forEach { accent ->
                            AccentColorOption(
                                accent = accent,
                                darkTheme = darkTheme,
                                selected = selectedAccent == accent,
                                onClick = { onAccentChanged(accent) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        repeat(columnCount - accents.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
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
        modifier = modifier.selectable(
            selected = selected,
            role = Role.RadioButton,
            onClick = onClick,
        ),
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
            if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            },
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
                    contentDescription = null,
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
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick,
            )
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
                contentDescription = null,
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
                modifier = Modifier.padding(bottom = 12.dp),
            )
            SampleUiPreview()
            Spacer(modifier = Modifier.height(12.dp))
            PalettePreview()
        }
    }
}

@Composable
private fun SampleUiPreview() {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                stringResource(Res.string.ui_sample_card),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(
                    Res.string.ui_this_is_how_cards_and_text_will_appear_with_your_selec,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(16.dp))
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                if (maxWidth < 320.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(Res.string.ui_button))
                        }
                        OutlinedButton(
                            onClick = {},
                            enabled = false,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(Res.string.ui_outline))
                        }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {}, enabled = false) {
                            Text(stringResource(Res.string.ui_button))
                        }
                        OutlinedButton(onClick = {}, enabled = false) {
                            Text(stringResource(Res.string.ui_outline))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PalettePreview() {
    Text(
        text = stringResource(Res.string.ui_color_palette),
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.padding(bottom = 8.dp),
    )
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val swatches = listOf(
            MaterialTheme.colorScheme.primary to stringResource(Res.string.ui_primary),
            MaterialTheme.colorScheme.secondary to stringResource(Res.string.ui_secondary),
            MaterialTheme.colorScheme.tertiary to stringResource(Res.string.ui_tertiary),
            MaterialTheme.colorScheme.error to stringResource(Res.string.ui_error),
        )
        val columnCount = if (maxWidth < 360.dp) 2 else 4
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            swatches.chunked(columnCount).forEach { rowSwatches ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    rowSwatches.forEach { (color, label) ->
                        ColorSwatch(color = color, label = label, modifier = Modifier.weight(1f))
                    }
                }
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
