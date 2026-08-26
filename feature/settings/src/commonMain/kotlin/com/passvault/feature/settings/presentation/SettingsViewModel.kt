package com.passvault.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.text.UiText
import com.passvault.core.designsystem.text.uiText
import com.passvault.core.designsystem.theme.PassVaultAccent
import com.passvault.core.domain.model.MasterPasswordPolicy
import com.passvault.core.domain.model.PasswordScore
import com.passvault.core.domain.model.PasswordStrengthEvaluator
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.model.VaultSessionState
import com.passvault.core.domain.model.codePointLength
import com.passvault.core.domain.model.hasWellFormedUnicode
import com.passvault.core.domain.model.takeCodePoints
import com.passvault.core.domain.repository.AppSettings
import com.passvault.core.domain.repository.AppSettingsStore
import com.passvault.core.domain.repository.AccentColorPreference
import com.passvault.core.domain.repository.LanguagePreference
import com.passvault.core.domain.repository.ThemePreference
import com.passvault.core.domain.repository.VaultRepository
import com.passvault.core.domain.repository.lockWithBoundedRetry
import com.passvault.core.security.BiometricAvailability
import com.passvault.core.security.BiometricFailureReason
import com.passvault.core.security.BiometricOperationResult
import com.passvault.core.security.BiometricType
import com.passvault.core.security.BiometricUnlockService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SettingsViewModel(
    private val vaultRepository: VaultRepository,
    private val appSettingsStore: AppSettingsStore,
    private val biometricUnlockService: BiometricUnlockService,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<SettingsEffect>(extraBufferCapacity = 1)
    val effect: Flow<SettingsEffect> = _effect.asSharedFlow()

    private val settingsSaveMutex = Mutex()
    private var settingsRevision = 0L
    private var preferencesSaveJob: Job? = null
    private var metadataJob: Job? = null
    private var masterPasswordChangeJob: Job? = null
    private var biometricJob: Job? = null

    init {
        loadPreferences()
        loadVaultMetadata()
        loadBiometricStatus()
        viewModelScope.launch {
            vaultRepository.getSessionState().collect { session ->
                if (session is VaultSessionState.Unlocked) {
                    loadVaultMetadata()
                    loadBiometricStatus()
                }
            }
        }
    }

    private fun loadPreferences() {
        val revisionAtStart = settingsRevision
        viewModelScope.launch {
            try {
                val result = appSettingsStore.load()
                currentCoroutineContext().ensureActive()
                result.onSuccess { settings ->
                    if (settingsRevision == revisionAtStart) {
                        _state.update {
                            it.copy(
                                theme = settings.theme.toAppTheme(),
                                language = settings.language.toAppLanguage(),
                                accentColor = settings.accentColor.toPassVaultAccent(),
                                autoLockTimeoutMinutes = settings.autoLockTimeoutMinutes,
                                clipboardClearSeconds = settings.clipboardClearSeconds,
                            )
                        }
                    }
                }
                .onFailure {
                    if (settingsRevision == revisionAtStart) {
                        _state.update {
                            it.copy(errorMessage = uiText(Res.string.error_settings_load))
                        }
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (settingsRevision == revisionAtStart) {
                    _state.update {
                        it.copy(errorMessage = uiText(Res.string.error_settings_load))
                    }
                }
            }
        }
    }

    private fun loadVaultMetadata() {
        metadataJob?.cancel()
        metadataJob = viewModelScope.launch {
            try {
                val result = vaultRepository.getMetadata()
                currentCoroutineContext().ensureActive()
                result
                .onSuccess { metadata ->
                    _state.update {
                        it.copy(
                            vaultCreatedAt = metadata.createdAt.toString(),
                            vaultEntryCount = metadata.entryCount,
                            isLoading = false,
                        )
                    }
                }
                .onFailure {
                    _state.update { state -> state.copy(isLoading = false) }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                currentCoroutineContext().ensureActive()
                _state.update { state -> state.copy(isLoading = false) }
            }
        }
    }

    /* This exhaustive router is the single public boundary for settings UI events. */
    @Suppress("CyclomaticComplexMethod", "LongMethod")
    fun onEvent(event: SettingsEvent) {
        if (_state.value.isChangingPassword && event.isPasswordInputChange()) return
        when (event) {
            is SettingsEvent.OnThemeChanged -> {
                persistPreferences { it.copy(theme = event.theme) }
            }
            is SettingsEvent.OnLanguageChanged -> {
                persistPreferences { it.copy(language = event.language) }
            }
            is SettingsEvent.OnAccentColorChanged -> {
                persistPreferences { it.copy(accentColor = event.accentColor) }
            }
            is SettingsEvent.OnAutoLockTimeoutChanged -> {
                persistPreferences {
                    it.copy(
                        autoLockTimeoutMinutes = event.minutes.coerceIn(
                            AppSettings.MIN_AUTO_LOCK_TIMEOUT_MINUTES,
                            AppSettings.MAX_AUTO_LOCK_TIMEOUT_MINUTES,
                        ),
                    )
                }
            }
            is SettingsEvent.OnClipboardClearChanged -> {
                persistPreferences {
                    it.copy(
                        clipboardClearSeconds = event.seconds.coerceIn(
                            AppSettings.MIN_CLIPBOARD_CLEAR_SECONDS,
                            AppSettings.MAX_CLIPBOARD_CLEAR_SECONDS,
                        ),
                    )
                }
            }
            SettingsEvent.OnSecurityClick -> _effect.tryEmit(SettingsEffect.NavigateToSecurity)
            SettingsEvent.OnAppearanceClick -> _effect.tryEmit(SettingsEffect.NavigateToAppearance)
            SettingsEvent.OnDataClick -> _effect.tryEmit(SettingsEffect.NavigateToData)
            SettingsEvent.OnBackClick -> requestBack()
            SettingsEvent.OnLockVaultClick -> lockVault()
            SettingsEvent.OnChangePasswordClick -> {
                _state.update { it.copy(showChangePasswordDialog = true, passwordError = null) }
            }
            is SettingsEvent.OnBiometricUnlockChanged -> {
                setBiometricUnlockEnabled(event.enabled)
            }
            SettingsEvent.OnExportClick -> _effect.tryEmit(SettingsEffect.ShowExportDialog)
            SettingsEvent.OnImportClick -> _effect.tryEmit(SettingsEffect.ShowImportDialog)
            SettingsEvent.OnBackupClick -> _effect.tryEmit(SettingsEffect.ShowBackupDialog)
            is SettingsEvent.OnCurrentPasswordChanged -> {
                val password = event.password.takeCodePoints(MasterPasswordPolicy.MAX_LENGTH + 1)
                _state.update { current ->
                    current.copy(
                        currentPassword = password,
                        passwordError = passwordInputFeedback(
                            currentPassword = password,
                            newPassword = current.newPassword,
                            confirmPassword = current.confirmPassword,
                        ),
                    )
                }
            }
            is SettingsEvent.OnNewPasswordChanged -> {
                val password = event.password.takeCodePoints(MasterPasswordPolicy.MAX_LENGTH + 1)
                _state.update { current ->
                    current.copy(
                        newPassword = password,
                        passwordStrength = passwordStrength(password),
                        passwordError = passwordInputFeedback(
                            currentPassword = current.currentPassword,
                            newPassword = password,
                            confirmPassword = current.confirmPassword,
                        ),
                    )
                }
            }
            is SettingsEvent.OnConfirmPasswordChanged -> {
                val password = event.password.takeCodePoints(MasterPasswordPolicy.MAX_LENGTH + 1)
                _state.update { current ->
                    current.copy(
                        confirmPassword = password,
                        passwordError = passwordInputFeedback(
                            currentPassword = current.currentPassword,
                            newPassword = current.newPassword,
                            confirmPassword = password,
                        ),
                    )
                }
            }
            SettingsEvent.OnChangePasswordConfirm -> changePassword()
            SettingsEvent.OnChangePasswordCancel -> {
                if (!_state.value.isChangingPassword) {
                    clearPasswordDialog()
                }
            }
            SettingsEvent.OnDismissError -> _state.update { it.copy(errorMessage = null) }
            SettingsEvent.OnDismissStatusMessage -> _state.update { it.copy(statusMessage = null) }
            SettingsEvent.OnVaultInfoClick -> {
                _state.update {
                    it.copy(
                        infoDialogTitle = uiText(Res.string.settings_info_vault_title),
                        infoDialogMessage = uiText(
                            Res.string.settings_info_vault_message,
                            it.vaultCreatedAt.takeIf(String::isNotBlank)
                                ?: uiText(Res.string.ui_unknown),
                            it.vaultEntryCount,
                        ),
                    )
                }
            }
            SettingsEvent.OnHelpClick -> {
                _state.update {
                    it.copy(
                        infoDialogTitle = uiText(Res.string.settings_info_help_title),
                        infoDialogMessage = uiText(Res.string.settings_info_help_message),
                    )
                }
            }
            SettingsEvent.OnPrivacyClick -> {
                _state.update {
                    it.copy(
                        infoDialogTitle = uiText(Res.string.settings_info_privacy_title),
                        infoDialogMessage = uiText(Res.string.settings_info_privacy_message),
                    )
                }
            }
            SettingsEvent.OnDismissInfo -> {
                _state.update { it.copy(infoDialogTitle = null, infoDialogMessage = null) }
            }
        }
    }

    private fun requestBack() {
        val current = _state.value
        when {
            current.isChangingPassword -> Unit
            current.showChangePasswordDialog -> clearPasswordDialog()
            current.infoDialogTitle != null || current.infoDialogMessage != null ->
                _state.update { it.copy(infoDialogTitle = null, infoDialogMessage = null) }
            current.errorMessage != null -> _state.update { it.copy(errorMessage = null) }
            else -> _effect.tryEmit(SettingsEffect.NavigateBack)
        }
    }

    private fun persistPreferences(transform: (SettingsState) -> SettingsState) {
        val updated = transform(_state.value).copy(errorMessage = null)
        val revision = ++settingsRevision
        _state.value = updated

        preferencesSaveJob?.cancel()
        preferencesSaveJob = viewModelScope.launch {
            settingsSaveMutex.withLock {
                if (revision != settingsRevision) return@withLock

                try {
                    val result = appSettingsStore.save(updated.toAppSettings())
                    currentCoroutineContext().ensureActive()
                    if (result.isFailure && revision == settingsRevision) {
                        _state.update { it.copy(errorMessage = uiText(Res.string.error_settings_save)) }
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    currentCoroutineContext().ensureActive()
                    if (revision == settingsRevision) {
                        _state.update { it.copy(errorMessage = uiText(Res.string.error_settings_save)) }
                    }
                }
            }
        }
    }

    private fun lockVault() {
        if (_state.value.isLockingVault) return
        _state.update { it.copy(isLockingVault = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                if (vaultRepository.lockWithBoundedRetry()) {
                    _effect.tryEmit(SettingsEffect.LockVault)
                } else {
                    currentCoroutineContext().ensureActive()
                    _state.update { it.copy(errorMessage = uiText(Res.string.error_settings_lock)) }
                }
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Exception) {
                _state.update { it.copy(errorMessage = uiText(Res.string.error_settings_lock)) }
            } finally {
                _state.update { it.copy(isLockingVault = false) }
            }
        }
    }

    private fun loadBiometricStatus() {
        biometricJob?.cancel()
        _state.update { it.copy(isBiometricLoading = true) }
        biometricJob = viewModelScope.launch {
            try {
                val status = biometricUnlockService.getStatus()
                currentCoroutineContext().ensureActive()
                _state.update {
                    it.copy(
                        biometricType = status.capability.type,
                        biometricAvailability = status.capability.availability,
                        isBiometricEnabled = status.isEnabled,
                        isBiometricLoading = false,
                    )
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                currentCoroutineContext().ensureActive()
                _state.update {
                    it.copy(
                        biometricAvailability = BiometricAvailability.UNAVAILABLE,
                        isBiometricEnabled = false,
                        isBiometricLoading = false,
                    )
                }
            }
        }
    }

    private fun setBiometricUnlockEnabled(enabled: Boolean) {
        if (_state.value.isBiometricLoading || enabled == _state.value.isBiometricEnabled) return
        biometricJob?.cancel()
        _state.update { it.copy(isBiometricLoading = true, errorMessage = null) }
        biometricJob = viewModelScope.launch {
            try {
                val result = if (enabled) {
                    biometricUnlockService.enable()
                } else {
                    biometricUnlockService.disable()
                }
                currentCoroutineContext().ensureActive()
                applyBiometricOperationResult(result, enabled)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                currentCoroutineContext().ensureActive()
                _state.update {
                    it.copy(
                        isBiometricLoading = false,
                        errorMessage = uiText(Res.string.error_biometric_failed),
                    )
                }
            }
        }
    }

    private fun applyBiometricOperationResult(
        result: BiometricOperationResult,
        enabled: Boolean,
    ) {
        _state.update { current ->
            when (result) {
                BiometricOperationResult.Success -> current.copy(
                    isBiometricEnabled = enabled,
                    isBiometricLoading = false,
                    statusMessage = uiText(
                        if (enabled) {
                            Res.string.message_biometric_enabled
                        } else {
                            Res.string.message_biometric_disabled
                        },
                    ),
                )
                BiometricOperationResult.Cancelled -> current.copy(isBiometricLoading = false)
                is BiometricOperationResult.Failure -> current.copy(
                    isBiometricEnabled = !enabled,
                    isBiometricLoading = false,
                    biometricAvailability = when (result.reason) {
                        BiometricFailureReason.NOT_ENROLLED -> BiometricAvailability.NOT_ENROLLED
                        BiometricFailureReason.LOCKED_OUT -> BiometricAvailability.LOCKED_OUT
                        BiometricFailureReason.NOT_AVAILABLE -> BiometricAvailability.UNAVAILABLE
                        else -> current.biometricAvailability
                    },
                    errorMessage = result.reason.toSettingsMessage(),
                )
            }
        }
    }

    private fun changePassword() {
        val currentState = _state.value
        if (currentState.isChangingPassword) return

        val validationError = passwordChangeValidationError(currentState)
        if (validationError != null) {
            _state.update { it.copy(passwordError = validationError) }
            return
        }

        val currentPassword = currentState.currentPassword
        val newPassword = currentState.newPassword
        _state.update { it.beginPasswordChange() }

        masterPasswordChangeJob?.cancel()
        masterPasswordChangeJob = viewModelScope.launch {
            val currentSensitive = SensitiveText.from(currentPassword)
            val newSensitive = SensitiveText.from(newPassword)
            try {
                val result = vaultRepository.changeMasterPassword(currentSensitive, newSensitive)
                currentCoroutineContext().ensureActive()
                if (result.isSuccess) {
                    _state.update {
                        it.copy(
                            isChangingPassword = false,
                            showChangePasswordDialog = false,
                            passwordStrength = PasswordStrength.EMPTY,
                            statusMessage = uiText(Res.string.message_master_password_changed),
                        )
                    }
                } else {
                    currentCoroutineContext().ensureActive()
                    _state.update {
                        it.copy(
                            isChangingPassword = false,
                            passwordStrength = PasswordStrength.EMPTY,
                            passwordError = uiText(
                                Res.string.error_master_password_change_verify,
                            ),
                        )
                    }
                }
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Exception) {
                currentCoroutineContext().ensureActive()
                _state.update {
                    it.copy(
                        isChangingPassword = false,
                        passwordStrength = PasswordStrength.EMPTY,
                        passwordError = uiText(Res.string.error_master_password_change),
                    )
                }
            } finally {
                currentSensitive.clear()
                newSensitive.clear()
                _state.update { it.copy(isChangingPassword = false) }
            }
        }
    }

    private fun clearPasswordDialog() {
        _state.update {
            it.copy(
                showChangePasswordDialog = false,
                currentPassword = "",
                newPassword = "",
                confirmPassword = "",
                passwordStrength = PasswordStrength.EMPTY,
                passwordError = null,
                isChangingPassword = false,
            )
        }
    }

    fun clearForLock() {
        masterPasswordChangeJob?.cancel()
        masterPasswordChangeJob = null
        metadataJob?.cancel()
        metadataJob = null
        biometricJob?.cancel()
        biometricJob = null
        clearPasswordDialog()
        _state.update {
            it.copy(
                vaultCreatedAt = "",
                vaultEntryCount = 0,
                errorMessage = null,
                statusMessage = null,
                infoDialogTitle = null,
                infoDialogMessage = null,
                isLoading = false,
                isBiometricLoading = false,
                isLockingVault = false,
            )
        }
    }

    data class SettingsState(
        val theme: AppTheme = AppTheme.SYSTEM,
        val language: AppLanguage = AppLanguage.SYSTEM,
        val accentColor: PassVaultAccent = PassVaultAccent.NEUTRAL,
        val autoLockTimeoutMinutes: Int = AppSettings.DEFAULT_AUTO_LOCK_TIMEOUT_MINUTES,
        val clipboardClearSeconds: Int = AppSettings.DEFAULT_CLIPBOARD_CLEAR_SECONDS,
        val vaultCreatedAt: String = "",
        val vaultEntryCount: Int = 0,
        val isLoading: Boolean = true,
        val errorMessage: UiText? = null,
        val statusMessage: UiText? = null,
        val showChangePasswordDialog: Boolean = false,
        val currentPassword: String = "",
        val newPassword: String = "",
        val confirmPassword: String = "",
        val passwordStrength: PasswordStrength = PasswordStrength.EMPTY,
        val passwordError: UiText? = null,
        val isChangingPassword: Boolean = false,
        val infoDialogTitle: UiText? = null,
        val infoDialogMessage: UiText? = null,
        val biometricType: BiometricType = BiometricType.GENERIC,
        val biometricAvailability: BiometricAvailability = BiometricAvailability.UNAVAILABLE,
        val isBiometricEnabled: Boolean = false,
        val isBiometricLoading: Boolean = false,
        val isLockingVault: Boolean = false,
    ) {
        val passwordsMatch: Boolean
            get() = newPassword == confirmPassword && confirmPassword.isNotEmpty()

        val canChangePassword: Boolean
            get() = currentPassword.codePointLength() in 1..MasterPasswordPolicy.MAX_LENGTH &&
                currentPassword.hasWellFormedUnicode() &&
                MasterPasswordPolicy.accepts(newPassword) &&
                passwordsMatch &&
                !isChangingPassword

        fun toAppSettings(): AppSettings = AppSettings(
            theme = when (theme) {
                AppTheme.LIGHT -> ThemePreference.LIGHT
                AppTheme.DARK -> ThemePreference.DARK
                AppTheme.SYSTEM -> ThemePreference.SYSTEM
            },
            language = when (language) {
                AppLanguage.SYSTEM -> LanguagePreference.SYSTEM
                AppLanguage.ENGLISH -> LanguagePreference.ENGLISH
                AppLanguage.ARABIC -> LanguagePreference.ARABIC
            },
            accentColor = when (accentColor) {
                PassVaultAccent.NEUTRAL -> AccentColorPreference.NEUTRAL
                PassVaultAccent.SAGE -> AccentColorPreference.SAGE
                PassVaultAccent.BLUE -> AccentColorPreference.BLUE
                PassVaultAccent.PURPLE -> AccentColorPreference.PURPLE
                PassVaultAccent.ROSE -> AccentColorPreference.ROSE
                PassVaultAccent.AMBER -> AccentColorPreference.AMBER
            },
            autoLockTimeoutMinutes = autoLockTimeoutMinutes,
            clipboardClearSeconds = clipboardClearSeconds,
        ).normalized()
    }

    enum class AppTheme {
        LIGHT,
        DARK,
        SYSTEM,
    }

    enum class AppLanguage {
        SYSTEM,
        ENGLISH,
        ARABIC,
    }

    enum class PasswordStrength {
        EMPTY,
        TOO_SHORT,
        WEAK,
        GOOD,
        STRONG,
    }

    sealed interface SettingsEvent {
        data class OnThemeChanged(val theme: AppTheme) : SettingsEvent
        data class OnLanguageChanged(val language: AppLanguage) : SettingsEvent
        data class OnAccentColorChanged(val accentColor: PassVaultAccent) : SettingsEvent
        data class OnAutoLockTimeoutChanged(val minutes: Int) : SettingsEvent
        data class OnClipboardClearChanged(val seconds: Int) : SettingsEvent
        data object OnSecurityClick : SettingsEvent
        data object OnAppearanceClick : SettingsEvent
        data object OnDataClick : SettingsEvent
        data object OnBackClick : SettingsEvent
        data object OnLockVaultClick : SettingsEvent
        data object OnChangePasswordClick : SettingsEvent
        data class OnBiometricUnlockChanged(val enabled: Boolean) : SettingsEvent
        data object OnExportClick : SettingsEvent
        data object OnImportClick : SettingsEvent
        data object OnBackupClick : SettingsEvent
        data class OnCurrentPasswordChanged(val password: String) : SettingsEvent
        data class OnNewPasswordChanged(val password: String) : SettingsEvent
        data class OnConfirmPasswordChanged(val password: String) : SettingsEvent
        data object OnChangePasswordConfirm : SettingsEvent
        data object OnChangePasswordCancel : SettingsEvent
        data object OnDismissError : SettingsEvent
        data object OnDismissStatusMessage : SettingsEvent
        data object OnVaultInfoClick : SettingsEvent
        data object OnHelpClick : SettingsEvent
        data object OnPrivacyClick : SettingsEvent
        data object OnDismissInfo : SettingsEvent
    }

    sealed interface SettingsEffect {
        data object NavigateBack : SettingsEffect
        data object NavigateToSecurity : SettingsEffect
        data object NavigateToAppearance : SettingsEffect
        data object NavigateToData : SettingsEffect
        data object LockVault : SettingsEffect
        data object ShowExportDialog : SettingsEffect
        data object ShowImportDialog : SettingsEffect
        data object ShowBackupDialog : SettingsEffect
    }

}

private fun SettingsViewModel.SettingsState.beginPasswordChange(): SettingsViewModel.SettingsState = copy(
    isChangingPassword = true,
    currentPassword = "",
    newPassword = "",
    confirmPassword = "",
    passwordError = null,
)

private fun BiometricFailureReason.toSettingsMessage(): UiText = when (this) {
    BiometricFailureReason.NOT_AVAILABLE -> uiText(Res.string.error_biometric_unavailable)
    BiometricFailureReason.NOT_ENROLLED -> uiText(Res.string.error_biometric_not_enrolled)
    BiometricFailureReason.LOCKED_OUT -> uiText(Res.string.error_biometric_locked_out)
    BiometricFailureReason.NOT_ENABLED -> uiText(Res.string.error_biometric_not_enabled)
    BiometricFailureReason.INVALIDATED -> uiText(Res.string.error_biometric_invalidated)
    BiometricFailureReason.VAULT_LOCKED -> uiText(Res.string.error_biometric_vault_locked)
    BiometricFailureReason.AUTHENTICATION_FAILED,
    BiometricFailureReason.INTERNAL_ERROR,
    -> uiText(Res.string.error_biometric_failed)
}

private fun passwordStrength(password: String): SettingsViewModel.PasswordStrength {
    val score = if (password.isEmpty()) PasswordScore.UNKNOWN else PasswordStrengthEvaluator.score(password)
    return when {
        password.isEmpty() -> SettingsViewModel.PasswordStrength.EMPTY
        password.codePointLength() < MasterPasswordPolicy.MIN_LENGTH -> SettingsViewModel.PasswordStrength.TOO_SHORT
        score <= PasswordScore.WEAK -> SettingsViewModel.PasswordStrength.WEAK
        score <= PasswordScore.GOOD -> SettingsViewModel.PasswordStrength.GOOD
        else -> SettingsViewModel.PasswordStrength.STRONG
    }
}

private fun passwordChangeValidationError(state: SettingsViewModel.SettingsState): UiText? = when {
    state.currentPassword.isEmpty() -> uiText(Res.string.error_current_password_required)
    state.currentPassword.codePointLength() > MasterPasswordPolicy.MAX_LENGTH ->
        uiText(Res.string.error_master_password_too_long, MasterPasswordPolicy.MAX_LENGTH)
    !state.currentPassword.hasWellFormedUnicode() -> uiText(Res.string.error_master_password_invalid)
    state.newPassword.codePointLength() < MasterPasswordPolicy.MIN_LENGTH ->
        uiText(Res.string.error_new_password_too_short)
    state.newPassword.codePointLength() > MasterPasswordPolicy.MAX_LENGTH ->
        uiText(Res.string.error_master_password_too_long, MasterPasswordPolicy.MAX_LENGTH)
    !state.newPassword.hasWellFormedUnicode() -> uiText(Res.string.error_master_password_invalid)
    !MasterPasswordPolicy.accepts(state.newPassword) ->
        uiText(Res.string.error_new_password_weak)
    state.confirmPassword.codePointLength() > MasterPasswordPolicy.MAX_LENGTH ->
        uiText(Res.string.error_master_confirmation_too_long)
    !state.confirmPassword.hasWellFormedUnicode() -> uiText(Res.string.error_master_password_invalid)
    state.newPassword != state.confirmPassword -> uiText(Res.string.error_master_password_mismatch)
    else -> null
}

private fun passwordInputFeedback(
    currentPassword: String,
    newPassword: String,
    confirmPassword: String,
): UiText? = when {
    currentPassword.codePointLength() > MasterPasswordPolicy.MAX_LENGTH ||
        newPassword.codePointLength() > MasterPasswordPolicy.MAX_LENGTH ->
        uiText(Res.string.error_master_password_too_long, MasterPasswordPolicy.MAX_LENGTH)
    confirmPassword.codePointLength() > MasterPasswordPolicy.MAX_LENGTH ->
        uiText(Res.string.error_master_confirmation_too_long)
    !currentPassword.hasWellFormedUnicode() ||
        !newPassword.hasWellFormedUnicode() ||
        !confirmPassword.hasWellFormedUnicode() ->
        uiText(Res.string.error_master_password_invalid)
    confirmPassword.isNotEmpty() && newPassword != confirmPassword ->
        uiText(Res.string.error_master_password_mismatch)
    else -> null
}

private fun SettingsViewModel.SettingsEvent.isPasswordInputChange(): Boolean = when (this) {
    is SettingsViewModel.SettingsEvent.OnCurrentPasswordChanged,
    is SettingsViewModel.SettingsEvent.OnNewPasswordChanged,
    is SettingsViewModel.SettingsEvent.OnConfirmPasswordChanged,
    -> true
    else -> false
}

private fun ThemePreference.toAppTheme(): SettingsViewModel.AppTheme = when (this) {
    ThemePreference.LIGHT -> SettingsViewModel.AppTheme.LIGHT
    ThemePreference.DARK -> SettingsViewModel.AppTheme.DARK
    ThemePreference.SYSTEM -> SettingsViewModel.AppTheme.SYSTEM
}

private fun LanguagePreference.toAppLanguage(): SettingsViewModel.AppLanguage = when (this) {
    LanguagePreference.SYSTEM -> SettingsViewModel.AppLanguage.SYSTEM
    LanguagePreference.ENGLISH -> SettingsViewModel.AppLanguage.ENGLISH
    LanguagePreference.ARABIC -> SettingsViewModel.AppLanguage.ARABIC
}

private fun AccentColorPreference.toPassVaultAccent(): PassVaultAccent = when (this) {
    AccentColorPreference.NEUTRAL -> PassVaultAccent.NEUTRAL
    AccentColorPreference.SAGE -> PassVaultAccent.SAGE
    AccentColorPreference.BLUE -> PassVaultAccent.BLUE
    AccentColorPreference.PURPLE -> PassVaultAccent.PURPLE
    AccentColorPreference.ROSE -> PassVaultAccent.ROSE
    AccentColorPreference.AMBER -> PassVaultAccent.AMBER
}
