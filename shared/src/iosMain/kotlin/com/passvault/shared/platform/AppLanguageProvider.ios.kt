package com.passvault.shared.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.passvault.feature.settings.presentation.SettingsViewModel
import platform.Foundation.NSLocale
import platform.Foundation.NSUserDefaults
import platform.Foundation.currentLocale
import platform.Foundation.languageCode

private object IosAppLocales {
    private const val APPLE_LANGUAGES_KEY = "AppleLanguages"

    fun apply(language: SettingsViewModel.AppLanguage): String {
        val defaults = NSUserDefaults.standardUserDefaults
        when (language) {
            SettingsViewModel.AppLanguage.SYSTEM -> defaults.removeObjectForKey(APPLE_LANGUAGES_KEY)
            SettingsViewModel.AppLanguage.ENGLISH -> defaults.setObject(listOf("en"), APPLE_LANGUAGES_KEY)
            SettingsViewModel.AppLanguage.ARABIC -> defaults.setObject(listOf("ar"), APPLE_LANGUAGES_KEY)
        }
        defaults.synchronize()
        return when (language) {
            SettingsViewModel.AppLanguage.SYSTEM ->
                NSLocale.currentLocale.languageCode
            SettingsViewModel.AppLanguage.ENGLISH -> "en"
            SettingsViewModel.AppLanguage.ARABIC -> "ar"
        }
    }
}

@Composable
internal actual fun AppLanguageProvider(
    language: SettingsViewModel.AppLanguage,
    content: @Composable () -> Unit,
) {
    val languageTag = remember(language) { IosAppLocales.apply(language) }
    val baseDensity = LocalDensity.current
    val languageDensity = remember(language, baseDensity.density, baseDensity.fontScale) {
        AppLanguageDensity(baseDensity.density, baseDensity.fontScale)
    }
    val direction = if (languageTag.startsWith("ar")) LayoutDirection.Rtl else LayoutDirection.Ltr
    CompositionLocalProvider(
        LocalDensity provides languageDensity,
        LocalLayoutDirection provides direction,
    ) {
        content()
    }
}
