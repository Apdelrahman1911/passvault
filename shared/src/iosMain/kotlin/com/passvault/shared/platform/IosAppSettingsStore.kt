package com.passvault.shared.platform

import com.passvault.core.domain.repository.AppSettings
import com.passvault.core.domain.repository.AppSettingsStore
import com.passvault.core.domain.repository.AccentColorPreference
import com.passvault.core.domain.repository.LanguagePreference
import com.passvault.core.domain.repository.ThemePreference
import platform.Foundation.NSUserDefaults

/** Stores non-sensitive application preferences in NSUserDefaults. */
class IosAppSettingsStore(
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults,
) : AppSettingsStore {
    override suspend fun load(): Result<AppSettings> = runCatching {
        AppSettings(
            theme = defaults.stringForKey(KEY_THEME)
                ?.let { stored -> ThemePreference.entries.firstOrNull { it.name == stored } }
                ?: ThemePreference.SYSTEM,
            language = defaults.stringForKey(IOS_APP_LANGUAGE_KEY)
                ?.let { stored -> LanguagePreference.entries.firstOrNull { it.name == stored } }
                ?: LanguagePreference.SYSTEM,
            accentColor = defaults.stringForKey(KEY_ACCENT_COLOR)
                ?.let { stored -> AccentColorPreference.entries.firstOrNull { it.name == stored } }
                ?: AccentColorPreference.NEUTRAL,
            autoLockTimeoutMinutes = defaults.intOrDefault(
                key = KEY_AUTO_LOCK_TIMEOUT,
                defaultValue = AppSettings.DEFAULT_AUTO_LOCK_TIMEOUT_MINUTES,
            ),
            clipboardClearSeconds = defaults.intOrDefault(
                key = KEY_CLIPBOARD_CLEAR,
                defaultValue = AppSettings.DEFAULT_CLIPBOARD_CLEAR_SECONDS,
            ),
        ).normalized()
    }

    override suspend fun save(settings: AppSettings): Result<Unit> = runCatching {
        val normalized = settings.normalized()
        defaults.setObject(normalized.theme.name, forKey = KEY_THEME)
        defaults.setObject(normalized.language.name, forKey = IOS_APP_LANGUAGE_KEY)
        defaults.setObject(normalized.accentColor.name, forKey = KEY_ACCENT_COLOR)
        defaults.setInteger(normalized.autoLockTimeoutMinutes.toLong(), forKey = KEY_AUTO_LOCK_TIMEOUT)
        defaults.setInteger(normalized.clipboardClearSeconds.toLong(), forKey = KEY_CLIPBOARD_CLEAR)
    }

    private fun NSUserDefaults.intOrDefault(key: String, defaultValue: Int): Int =
        if (objectForKey(key) == null) defaultValue else integerForKey(key).toInt()

    private companion object {
        const val KEY_THEME = "theme"
        const val KEY_ACCENT_COLOR = "accent_color"
        const val KEY_AUTO_LOCK_TIMEOUT = "auto_lock_timeout_minutes"
        const val KEY_CLIPBOARD_CLEAR = "clipboard_clear_seconds"
    }
}
