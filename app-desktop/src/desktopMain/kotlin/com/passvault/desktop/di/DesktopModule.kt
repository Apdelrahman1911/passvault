package com.passvault.desktop.di

import com.passvault.core.security.ClipboardService
import com.passvault.core.security.BiometricKeyStore
import com.passvault.core.security.BiometricPromptController
import com.passvault.core.domain.repository.AppSettingsStore
import com.passvault.desktop.security.DesktopClipboardService
import com.passvault.desktop.security.DesktopWindowProtection
import com.passvault.desktop.security.biometric.DesktopBiometricHost
import com.passvault.desktop.security.biometric.DesktopBiometricRuntime
import com.passvault.desktop.backup.DesktopBackupFileStore
import com.passvault.desktop.attachment.DesktopAttachmentFileStore
import com.passvault.desktop.settings.DesktopAppSettingsStore
import com.passvault.desktop.AppInfo
import com.passvault.desktop.tray.DesktopSystemTray
import com.passvault.feature.backup.BackupFileStore
import com.passvault.feature.credential.AttachmentFileStore
import org.koin.dsl.module
import java.util.prefs.Preferences

/**
 * Desktop-specific dependency injection module.
 * Provides platform-specific implementations of security and UI services.
 */
val desktopModule = module {

    // Shared database creation accepts an opaque platform value.
    single<Any> { Unit }

    // Security services - Desktop implementations
    single<ClipboardService> { DesktopClipboardService(scope = get()) }
    single { DesktopBiometricRuntime.create() }
    single<BiometricKeyStore> { get<DesktopBiometricRuntime>().keyStore }
    single<DesktopBiometricHost> { get<DesktopBiometricRuntime>().host }
    single<BiometricPromptController> { get<DesktopBiometricHost>() }
    single { DesktopWindowProtection() }

    // Desktop-specific services
    single { DesktopSystemTray() }

    // Preferences for desktop settings persistence
    single { Preferences.userNodeForPackage(AppInfo::class.java) }
    single<AppSettingsStore> { DesktopAppSettingsStore(get()) }
    single<BackupFileStore> { DesktopBackupFileStore() }
    single { DesktopAttachmentFileStore(vaultRepository = get(), cleanupScope = get()) }
    single<AttachmentFileStore> { get<DesktopAttachmentFileStore>() }
}
