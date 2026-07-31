package com.passvault.android.settings

import android.content.Context
import com.passvault.core.domain.repository.AppSettings
import com.passvault.core.domain.repository.AppSettingsStore
import com.passvault.core.domain.repository.ThemePreference
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidAppSettingsStore(
    context: Context,
) : AppSettingsStore {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE,
    )

    override suspend fun load(): Result<AppSettings> = withContext(Dispatchers.IO) {
        try {
            Result.success(
                AppSettings(
                    theme = preferences.getString(KEY_THEME, null)
                        ?.let { stored -> ThemePreference.entries.firstOrNull { it.name == stored } }
                        ?: ThemePreference.SYSTEM,
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
            check(
                preferences.edit()
                    .putString(KEY_THEME, normalized.theme.name)
                    .putInt(KEY_AUTO_LOCK_TIMEOUT, normalized.autoLockTimeoutMinutes)
                    .putInt(KEY_CLIPBOARD_CLEAR, normalized.clipboardClearSeconds)
                    .commit(),
            ) {
                "Application preferences could not be persisted"
            }
            Result.success(Unit)
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    private companion object {
        const val PREFERENCES_NAME = "passvault_application_settings"
        const val KEY_THEME = "theme"
        const val KEY_AUTO_LOCK_TIMEOUT = "auto_lock_timeout_minutes"
        const val KEY_CLIPBOARD_CLEAR = "clipboard_clear_seconds"
    }
}
