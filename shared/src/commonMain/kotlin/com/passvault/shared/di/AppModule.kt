package com.passvault.shared.di

import androidx.lifecycle.ViewModel
import com.passvault.core.crypto.CryptoEngine
import com.passvault.core.crypto.LibsodiumCryptoEngine
import com.passvault.core.crypto.PasswordGenerator
import com.passvault.core.crypto.SecurePasswordGenerator
import com.passvault.core.crypto.VaultKeyHierarchy
import com.passvault.core.database.VaultDatabase
import com.passvault.core.database.VaultDatabaseBootstrap
import com.passvault.core.database.attachment.AttachmentRepositoryImpl
import com.passvault.core.database.attachment.AttachmentLifecycleManager
import com.passvault.core.database.backup.VaultBackupService
import com.passvault.core.database.repository.CredentialRepositoryImpl
import com.passvault.core.database.repository.DefaultBiometricUnlockService
import com.passvault.core.database.repository.FolderRepositoryImpl
import com.passvault.core.database.repository.TagRepositoryImpl
import com.passvault.core.database.repository.VaultRepositoryImpl
import com.passvault.core.database.repository.VaultSessionManager
import com.passvault.core.domain.repository.AttachmentRepository
import com.passvault.core.domain.repository.CredentialRepository
import com.passvault.core.domain.repository.CredentialTotpRepository
import com.passvault.core.domain.repository.FolderRepository
import com.passvault.core.domain.repository.TagRepository
import com.passvault.core.domain.repository.VaultRepository
import com.passvault.core.navigation.AppCommandDispatcher
import com.passvault.core.navigation.ExternalNavigationDispatcher
import com.passvault.core.otp.StandardTotpService
import com.passvault.core.otp.TotpService
import com.passvault.core.security.BiometricUnlockService
import com.passvault.core.security.EntrySensitiveStateOwner
import com.passvault.core.security.VaultUiSecurityCoordinator
import com.passvault.feature.onboarding.presentation.OnboardingViewModel
import com.passvault.feature.unlock.presentation.UnlockViewModel
import com.passvault.feature.vault.presentation.VaultViewModel
import com.passvault.feature.vault.presentation.TwoFactorCodesViewModel
import com.passvault.feature.credential.presentation.CredentialViewModel
import com.passvault.feature.generator.presentation.GeneratorViewModel
import com.passvault.feature.settings.presentation.SettingsViewModel
import com.passvault.feature.backup.presentation.BackupViewModel
import com.passvault.feature.health.presentation.HealthViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.core.definition.Definition
import org.koin.core.definition.KoinDefinition
import org.koin.core.module.dsl.viewModel
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
        single<PasswordGenerator> { SecurePasswordGenerator(get()) }
        single { VaultKeyHierarchy(get()) }
        single { AppCommandDispatcher() }
        single { ExternalNavigationDispatcher() }
        single { VaultUiSecurityCoordinator() }
        single<TotpService> { StandardTotpService() }

        // Coroutine scope for background operations
        single { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    }

    /**
     * Database module.
     */
    val databaseModule = module {
        // Database
        single { createDatabaseBootstrap(get()) }
        single<VaultDatabase> { get<VaultDatabaseBootstrap>().database() }
        single<AppDatabaseLifecycle> {
            AppDatabaseLifecycle { get<VaultDatabaseBootstrap>().close() }
        }
        single { createAttachmentBlobStore(get()) }

        // DAOs
        single { get<VaultDatabase>().vaultMetadataDao() }
        single { get<VaultDatabase>().credentialDao() }
        single { get<VaultDatabase>().folderDao() }
        single { get<VaultDatabase>().tagDao() }
        single { get<VaultDatabase>().attachmentDao() }
        single { get<VaultDatabase>().passwordHistoryDao() }
        single { get<VaultDatabase>().vaultBackupDao() }
    }

    /**
     * Repository module.
     */
    val repositoryModule = module {
        // Publish only the interfaces. Keeping one interface-owned singleton
        // prevents consumers from bypassing AttachmentRepositoryImpl's mutex.
        single<AttachmentRepository> {
            AttachmentRepositoryImpl(
                attachmentDao = get(),
                credentialDao = get(),
                blobStore = get(),
                cryptoEngine = get(),
                sessionManager = get(),
            )
        }
        single<AttachmentLifecycleManager> { get<AttachmentRepository>() as AttachmentLifecycleManager }

        // Repositories
        single<CredentialRepositoryImpl> {
            CredentialRepositoryImpl(
                credentialDao = get(),
                folderDao = get(),
                tagDao = get(),
                attachmentDao = get(),
                passwordHistoryDao = get(),
                cryptoEngine = get(),
                sessionManager = get(),
                attachmentLifecycleManager = get(),
            )
        }
        single<CredentialRepository> { get<CredentialRepositoryImpl>() }
        single<CredentialTotpRepository> { get<CredentialRepositoryImpl>() }

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
                biometricPromptController = get(),
            )
        }
        single<VaultRepository> { get<VaultRepositoryImpl>() }
        single<VaultSessionManager> {
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
        single {
            VaultBackupService(
                backupDao = get(),
                database = get(),
                cryptoEngine = get(),
                vaultRepository = get(),
                sessionManager = get(),
                biometricKeyStore = get(),
                attachmentBlobStore = get(),
                attachmentLifecycleManager = get(),
            )
        }
    }

    /**
     * Feature module with ViewModels.
     */
    val featureModule = module {
        // Onboarding
        single { OnboardingViewModel(get()) }

        // Unlock
        single { UnlockViewModel(get(), get()) }

        // App/session-scoped roots. These owners coordinate secure teardown
        // across every navigation entry and intentionally outlive one screen.
        single { VaultViewModel(get(), get(), get()) }
        single { SettingsViewModel(get(), get(), get()) }
        single { BackupViewModel(get(), get(), get()) }

        // Navigation-entry-scoped state. Nav3's ViewModelStore decorator owns
        // these instances, so a popped entry cannot retain sensitive UI state.
        lockSensitiveViewModel { TwoFactorCodesViewModel(get(), get()) }
        lockSensitiveViewModel {
            CredentialViewModel(
                credentialRepository = get(),
                folderRepository = get(),
                passwordGenerator = get(),
                totpService = get(),
                attachmentRepository = get(),
                attachmentFileStore = get(),
            )
        }

        // Generator
        lockSensitiveViewModel { GeneratorViewModel(get()) }
        lockSensitiveViewModel { HealthViewModel(get()) }
    }
}

private inline fun <reified T> Module.lockSensitiveViewModel(
    noinline definition: Definition<T>,
): KoinDefinition<T> where T : ViewModel, T : EntrySensitiveStateOwner = viewModel {
    definition(this, it).attachToLockSensitiveStateRegistry(get())
}

internal fun <T> T.attachToLockSensitiveStateRegistry(
    coordinator: VaultUiSecurityCoordinator,
): T where T : ViewModel, T : EntrySensitiveStateOwner {
    addCloseable(coordinator.registerEntrySensitiveState(this))
    return this
}
