package com.passvault.shared.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.passvault.feature.settings.presentation.SettingsViewModel
import java.util.Locale

private object DesktopAppLocales {
    val systemLocale: Locale = Locale.getDefault()

    fun apply(language: SettingsViewModel.AppLanguage): Locale {
        val locale = when (language) {
            SettingsViewModel.AppLanguage.SYSTEM -> systemLocale
            SettingsViewModel.AppLanguage.ENGLISH -> Locale.forLanguageTag("en")
            SettingsViewModel.AppLanguage.ARABIC -> Locale.forLanguageTag("ar")
        }
        Locale.setDefault(locale)
        return locale
    }
}

@Composable
internal actual fun AppLanguageProvider(
    language: SettingsViewModel.AppLanguage,
    content: @Composable () -> Unit,
) {
    val locale = remember(language) { DesktopAppLocales.apply(language) }
    val baseDensity = LocalDensity.current
    val languageDensity = remember(language, baseDensity.density, baseDensity.fontScale) {
        AppLanguageDensity(baseDensity.density, baseDensity.fontScale)
    }
    val direction = if (locale.language == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr
    CompositionLocalProvider(
        LocalDensity provides languageDensity,
        LocalLayoutDirection provides direction,
    ) {
        content()
    }
}
