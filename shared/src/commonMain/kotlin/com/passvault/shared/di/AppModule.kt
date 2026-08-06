package com.passvault.shared.di

import com.passvault.core.crypto.CryptoEngine
import com.passvault.core.crypto.LibsodiumCryptoEngine
import com.passvault.core.crypto.VaultKeyHierarchy
import com.passvault.core.database.VaultDatabase
import com.passvault.core.database.backup.VaultBackupService
import com.passvault.core.database.dao.*
import com.passvault.core.database.repository.*
import com.passvault.core.domain.repository.*
import com.passvault.core.navigation.AppCommandDispatcher
import com.passvault.core.otp.StandardTotpService
import com.passvault.core.otp.TotpService
import com.passvault.core.security.BiometricUnlockService
import com.passvault.feature.onboarding.presentation.OnboardingViewModel
import com.passvault.feature.unlock.presentation.UnlockViewModel
import com.passvault.feature.vault.presentation.VaultViewModel
import com.passvault.feature.credential.presentation.CredentialViewModel
import com.passvault.feature.generator.presentation.GeneratorViewModel
import com.passvault.feature.settings.presentation.SettingsViewModel
import com.passvault.feature.backup.presentation.BackupViewModel
import com.passvault.feature.health.presentation.HealthViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Main application module providing all dependencies.
 */
object AppModule {
    
    fun getAllModules(platformModule: Module): List<Module> = listOf(
        platformModule,
        coreModule,
        databaseModule,
        repositoryModule,
        featureModule,
    )
    
    /**
     * Core module with crypto and domain.
     */
    val coreModule = module {
        // Crypto
        single<CryptoEngine> { LibsodiumCryptoEngine() }
        single { VaultKeyHierarchy(get()) }
        single { AppCommandDispatcher() }
        single<TotpService> { StandardTotpService() }
        
        // Coroutine scope for background operations
        single { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    }
    
    /**
     * Database module.
     */
    val databaseModule = module {
        // Database
        single { createDatabase(get()) }
        
        // DAOs
        single { get<VaultDatabase>().vaultMetadataDao() }
        single { get<VaultDatabase>().credentialDao() }
        single { get<VaultDatabase>().folderDao() }
        single { get<VaultDatabase>().tagDao() }
        single { get<VaultDatabase>().attachmentDao() }
        single { get<VaultDatabase>().passwordHistoryDao() }
        single { get<VaultDatabase>().migrationStateDao() }
        single { get<VaultDatabase>().vaultBackupDao() }
    }
    
    /**
     * Repository module.
     */
    val repositoryModule = module {
        // Repositories
        single<CredentialRepository> { 
            CredentialRepositoryImpl(
                credentialDao = get(),
                folderDao = get(),
                tagDao = get(),
                attachmentDao = get(),
                passwordHistoryDao = get(),
                cryptoEngine = get(),
                sessionManager = get(),
            )
        }
        
        single<FolderRepository> { 
            FolderRepositoryImpl(
                folderDao = get(),
                cryptoEngine = get(),
                sessionManager = get(),
            )
        }
        
        single<TagRepository> { 
            TagRepositoryImpl(
                tagDao = get(),
                cryptoEngine = get(),
                sessionManager = get(),
            )
        }
        
        single<VaultRepositoryImpl> {
            VaultRepositoryImpl(
                vaultMetadataDao = get(),
                cryptoEngine = get(),
                keyHierarchy = get(),
                applicationScope = get(),
            )
        }
        single<VaultRepository> { get<VaultRepositoryImpl>() }
        single<com.passvault.core.database.repository.VaultSessionManager> {
            get<VaultRepositoryImpl>()
        }
        single<BiometricUnlockService> {
            DefaultBiometricUnlockService(
                vaultRepository = get(),
                sessionManager = get(),
                keyStore = get(),
                cryptoEngine = get(),
            )
        }
        single { VaultBackupService(get(), get(), get()) }
    }
    
    /**
     * Feature module with ViewModels.
     */
    val featureModule = module {
        // Onboarding
        single { OnboardingViewModel(get()) }
        
        // Unlock
        single { UnlockViewModel(get(), get()) }
        
        // Vault
        single { VaultViewModel(get(), get(), get()) }
        
        // Credential
        single { CredentialViewModel(get(), get(), get()) }
        
        // Generator
        single { GeneratorViewModel(get()) }
        
        // Settings
        single { SettingsViewModel(get(), get(), get()) }
        
        // Backup
        single { BackupViewModel(get(), get(), get()) }
        
        // Health
        single { HealthViewModel(get()) }
    }
}
