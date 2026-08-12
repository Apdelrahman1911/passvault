package com.passvault.shared.platform

import android.os.LocaleList
import android.text.TextUtils
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import com.passvault.feature.settings.presentation.SettingsViewModel
import java.util.Locale

private object AndroidAppLocales {
    val systemLocales: LocaleList = LocaleList.getDefault()

    fun apply(language: SettingsViewModel.AppLanguage): Locale {
        val locales = when (language) {
            SettingsViewModel.AppLanguage.SYSTEM -> systemLocales
            SettingsViewModel.AppLanguage.ENGLISH -> LocaleList(Locale.forLanguageTag("en"))
            SettingsViewModel.AppLanguage.ARABIC -> LocaleList(Locale.forLanguageTag("ar"))
        }
        LocaleList.setDefault(locales)
        val primary = locales[0]
        Locale.setDefault(primary)
        return primary
    }
}

@Composable
internal actual fun AppLanguageProvider(
    language: SettingsViewModel.AppLanguage,
    content: @Composable () -> Unit,
) {
    val locale = remember(language) { AndroidAppLocales.apply(language) }
    val baseDensity = LocalDensity.current
    val languageDensity = remember(language, baseDensity.density, baseDensity.fontScale) {
        AppLanguageDensity(baseDensity.density, baseDensity.fontScale)
    }
    val direction = if (TextUtils.getLayoutDirectionFromLocale(locale) == android.view.View.LAYOUT_DIRECTION_RTL) {
        LayoutDirection.Rtl
    } else {
        LayoutDirection.Ltr
    }
    CompositionLocalProvider(
        LocalDensity provides languageDensity,
        LocalLayoutDirection provides direction,
    ) {
        content()
    }
}
