package com.passvault.shared.di

import com.passvault.core.domain.repository.AppSettingsStore
import com.passvault.core.security.ClipboardService
import com.passvault.core.security.BiometricKeyStore
import com.passvault.feature.backup.BackupFileStore
import com.passvault.shared.platform.IosAppSettingsStore
import com.passvault.shared.platform.IosBackupFileStore
import com.passvault.shared.platform.IosClipboardService
import com.passvault.shared.platform.IosBiometricKeyStore
import org.koin.dsl.module

/** Platform dependencies needed by the shared Compose application on iOS. */
val iosModule = module {
    single<Any> { Unit }
    single<AppSettingsStore> { IosAppSettingsStore() }
    single<ClipboardService> { IosClipboardService() }
    single<BiometricKeyStore> { IosBiometricKeyStore() }
    single<BackupFileStore> { IosBackupFileStore() }
}
