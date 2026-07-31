package com.passvault.core.domain.repository

/**
 * Non-sensitive application preferences.
 *
 * Vault secrets and cryptographic material must never be added to this store.
 * Platform implementations use their native preferences mechanism so common
 * code remains portable to future Apple targets.
 */
data class AppSettings(
    val theme: ThemePreference = ThemePreference.SYSTEM,
    val autoLockTimeoutMinutes: Int = DEFAULT_AUTO_LOCK_TIMEOUT_MINUTES,
    val clipboardClearSeconds: Int = DEFAULT_CLIPBOARD_CLEAR_SECONDS,
) {
    fun normalized(): AppSettings = copy(
        autoLockTimeoutMinutes = autoLockTimeoutMinutes.coerceIn(
            MIN_AUTO_LOCK_TIMEOUT_MINUTES,
            MAX_AUTO_LOCK_TIMEOUT_MINUTES,
        ),
        clipboardClearSeconds = clipboardClearSeconds.coerceIn(
            MIN_CLIPBOARD_CLEAR_SECONDS,
            MAX_CLIPBOARD_CLEAR_SECONDS,
        ),
    )

    companion object {
        const val DEFAULT_AUTO_LOCK_TIMEOUT_MINUTES = 5
        const val MIN_AUTO_LOCK_TIMEOUT_MINUTES = 1
        const val MAX_AUTO_LOCK_TIMEOUT_MINUTES = 60

        const val DEFAULT_CLIPBOARD_CLEAR_SECONDS = 30
        const val MIN_CLIPBOARD_CLEAR_SECONDS = 5
        const val MAX_CLIPBOARD_CLEAR_SECONDS = 300
    }
}

enum class ThemePreference {
    LIGHT,
    DARK,
    SYSTEM,
}

interface AppSettingsStore {
    suspend fun load(): Result<AppSettings>

    suspend fun save(settings: AppSettings): Result<Unit>
}
