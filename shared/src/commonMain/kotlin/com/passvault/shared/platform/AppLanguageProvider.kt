package com.passvault.shared.platform

import androidx.compose.runtime.Composable
import com.passvault.feature.settings.presentation.SettingsViewModel

/** Applies the selected resource locale and layout direction to the whole application tree. */
@Composable
internal expect fun AppLanguageProvider(
    language: SettingsViewModel.AppLanguage,
    content: @Composable () -> Unit,
)
