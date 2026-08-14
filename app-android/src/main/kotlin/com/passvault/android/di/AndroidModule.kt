package com.passvault.android.di

import android.content.Context
import com.passvault.android.security.AndroidClipboardService
import com.passvault.android.security.AndroidScreenshotProtection
import com.passvault.android.security.AndroidBiometricKeyStore
import com.passvault.android.backup.AndroidBackupFileStore
import com.passvault.android.attachment.AndroidAttachmentFileStore
import com.passvault.android.lifecycle.AndroidLifecycleLockCoordinator
import com.passvault.android.settings.AndroidAppSettingsStore
import com.passvault.core.domain.repository.AppSettingsStore
import com.passvault.feature.backup.BackupFileStore
import com.passvault.feature.credential.AttachmentFileStore
import com.passvault.core.security.ClipboardService
import com.passvault.core.security.BiometricKeyStore
import com.passvault.core.security.BiometricPromptController
import com.passvault.core.security.NoOpBiometricPromptController
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

    single { AndroidLifecycleLockCoordinator(get(), get(), get(), get()) }
    single<AppSettingsStore> { AndroidAppSettingsStore(androidContext()) }
    single<AndroidBackupFileStore> { AndroidBackupFileStore(androidContext(), get()) }
    single<BackupFileStore> { get<AndroidBackupFileStore>() }
    single<AndroidAttachmentFileStore> { AndroidAttachmentFileStore(androidContext(), get()) }
    single<AttachmentFileStore> { get<AndroidAttachmentFileStore>() }

    // ============================================================================
    // Security Services
    // ============================================================================

    /**
     * Screenshot protection implementation.
     * Singleton - shared across the application.
     */
    single<AndroidScreenshotProtection> { AndroidScreenshotProtection() }
    single<ScreenshotProtection> { get<AndroidScreenshotProtection>() }
    single<AndroidBiometricKeyStore> { AndroidBiometricKeyStore(androidContext()) }
    single<BiometricKeyStore> { get<AndroidBiometricKeyStore>() }
    single<BiometricPromptController> { NoOpBiometricPromptController }

    /**
     * Clipboard service implementation.
     * Handles secure clipboard operations with auto-clear.
     */
    single<AndroidClipboardService> { AndroidClipboardService(androidContext()) }
    single<ClipboardService> { get<AndroidClipboardService>() }

    // ============================================================================
    // Android Context
    // ============================================================================

    /**
     * Shared database creation accepts an opaque platform context.
     * Delegate it to the Context registered by androidContext() at Koin startup.
     */
    single<Any> { get<Context>() }

}
