package com.passvault.desktop.settings

import com.passvault.core.domain.repository.AppSettings
import com.passvault.core.domain.repository.AppSettingsStore
import com.passvault.core.domain.repository.AccentColorPreference
import com.passvault.core.domain.repository.ThemePreference
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.prefs.Preferences

class DesktopAppSettingsStore(
    private val preferences: Preferences,
) : AppSettingsStore {
    override suspend fun load(): Result<AppSettings> = withContext(Dispatchers.IO) {
        try {
            Result.success(
                AppSettings(
                    theme = preferences.get(KEY_THEME, null)
                        ?.let { stored -> ThemePreference.entries.firstOrNull { it.name == stored } }
                        ?: ThemePreference.SYSTEM,
                    accentColor = preferences.get(KEY_ACCENT_COLOR, null)
                        ?.let { stored -> AccentColorPreference.entries.firstOrNull { it.name == stored } }
                        ?: AccentColorPreference.NEUTRAL,
                    autoLockTimeoutMinutes = preferences.getInt(
                        KEY_AUTO_LOCK_TIMEOUT,
                        AppSettings.DEFAULT_AUTO_LOCK_TIMEOUT_MINUTES,
                    ),
                    clipboardClearSeconds = preferences.getInt(
                        KEY_CLIPBOARD_CLEAR,
                        AppSettings.DEFAULT_CLIPBOARD_CLEAR_SECONDS,
                    ),
                ).normalized(),
            )
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    override suspend fun save(settings: AppSettings): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val normalized = settings.normalized()
            preferences.put(KEY_THEME, normalized.theme.name)
            preferences.put(KEY_ACCENT_COLOR, normalized.accentColor.name)
            preferences.putInt(KEY_AUTO_LOCK_TIMEOUT, normalized.autoLockTimeoutMinutes)
            preferences.putInt(KEY_CLIPBOARD_CLEAR, normalized.clipboardClearSeconds)
            preferences.flush()
            Result.success(Unit)
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    private companion object {
        const val KEY_THEME = "application.theme"
        const val KEY_ACCENT_COLOR = "application.accent_color"
        const val KEY_AUTO_LOCK_TIMEOUT = "application.auto_lock_timeout_minutes"
        const val KEY_CLIPBOARD_CLEAR = "application.clipboard_clear_seconds"
    }
}
