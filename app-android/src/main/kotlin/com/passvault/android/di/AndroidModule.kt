package com.passvault.android.di

import android.app.AlarmManager
import android.content.ClipboardManager
import android.content.Context
import android.os.PowerManager
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import com.passvault.android.security.AndroidClipboardService
import com.passvault.android.security.AndroidScreenshotProtection
import com.passvault.android.backup.AndroidBackupFileStore
import com.passvault.android.settings.AndroidAppSettingsStore
import com.passvault.core.domain.repository.AppSettingsStore
import com.passvault.feature.backup.BackupFileStore
import com.passvault.core.security.ClipboardService
import com.passvault.core.security.ScreenshotProtection
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Koin module for Android-specific dependencies.
 * 
 * Provides:
 * - Security services (clipboard and screenshot protection)
 * - Android system services
 * - Platform-specific implementations of shared interfaces
 * - Vault session manager with Android-specific configuration
 */
val androidModule = module {

    single<AppSettingsStore> { AndroidAppSettingsStore(androidContext()) }
    single<AndroidBackupFileStore> { AndroidBackupFileStore(androidContext()) }
    single<BackupFileStore> { get<AndroidBackupFileStore>() }

    // ============================================================================
    // Security Services
    // ============================================================================

    /**
     * Screenshot protection implementation.
     * Singleton - shared across the application.
     */
    single<AndroidScreenshotProtection> { AndroidScreenshotProtection() }
    single<ScreenshotProtection> { get<AndroidScreenshotProtection>() }

    /**
     * Clipboard service implementation.
     * Handles secure clipboard operations with auto-clear.
     */
    single<AndroidClipboardService> { AndroidClipboardService(androidContext()) }
    single<ClipboardService> { get<AndroidClipboardService>() }

    // ============================================================================
    // Android System Services
    // ============================================================================

    /**
     * ClipboardManager for clipboard operations.
     */
    single {
        androidContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    /**
     * AlarmManager for scheduling clipboard clear operations.
     */
    single {
        androidContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    /**
     * PowerManager for checking device state.
     */
    single {
        androidContext().getSystemService(Context.POWER_SERVICE) as PowerManager
    }

    /**
     * WindowManager for display metrics and window operations.
     */
    single {
        androidContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    /**
     * InputMethodManager for keyboard operations.
     */
    single {
        androidContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    // ============================================================================
    // Android Context
    // ============================================================================

    /**
     * Shared database creation accepts an opaque platform context.
     * Delegate it to the Context registered by androidContext() at Koin startup.
     */
    single<Any> { get<Context>() }

}
