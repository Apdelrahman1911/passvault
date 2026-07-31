package com.passvault.desktop.di

import com.passvault.core.security.ClipboardService
import com.passvault.core.security.KeyringService
import com.passvault.core.security.WindowProtection
import com.passvault.core.domain.repository.AppSettingsStore
import com.passvault.desktop.security.DesktopClipboardService
import com.passvault.desktop.security.DesktopKeyringService
import com.passvault.desktop.security.DesktopWindowProtection
import com.passvault.desktop.backup.DesktopBackupFileStore
import com.passvault.desktop.settings.DesktopAppSettingsStore
import com.passvault.desktop.AppInfo
import com.passvault.desktop.tray.DesktopSystemTray
import com.passvault.feature.backup.BackupFileStore
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
    single<KeyringService> { DesktopKeyringService() }
    single { DesktopWindowProtection() }
    single<WindowProtection> { get<DesktopWindowProtection>() }

    // Desktop-specific services
    single { DesktopSystemTray(scope = get()) }

    // Preferences for desktop settings persistence
    single { Preferences.userNodeForPackage(AppInfo::class.java) }
    single<AppSettingsStore> { DesktopAppSettingsStore(get()) }
    single<BackupFileStore> { DesktopBackupFileStore() }
}
