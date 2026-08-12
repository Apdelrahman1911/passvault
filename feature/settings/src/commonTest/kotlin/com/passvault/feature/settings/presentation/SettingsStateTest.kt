package com.passvault.feature.settings.presentation

import com.passvault.core.designsystem.theme.PassVaultAccent
import com.passvault.core.domain.repository.AccentColorPreference
import com.passvault.core.domain.repository.LanguagePreference
import com.passvault.core.domain.repository.ThemePreference
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsStateTest {
    @Test
    fun selectedThemeAndAccentAreConvertedForPersistence() {
        val settings = SettingsViewModel.SettingsState(
            theme = SettingsViewModel.AppTheme.DARK,
            language = SettingsViewModel.AppLanguage.ARABIC,
            accentColor = PassVaultAccent.ROSE,
        ).toAppSettings()

        assertEquals(ThemePreference.DARK, settings.theme)
        assertEquals(LanguagePreference.ARABIC, settings.language)
        assertEquals(AccentColorPreference.ROSE, settings.accentColor)
    }
}
