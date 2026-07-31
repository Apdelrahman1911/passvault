package com.passvault.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.text.UiText
import com.passvault.core.designsystem.text.uiText
import com.passvault.core.domain.model.PasswordScore
import com.passvault.core.domain.model.PasswordStrengthEvaluator
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.model.VaultSessionState
import com.passvault.core.domain.repository.AppSettings
import com.passvault.core.domain.repository.AppSettingsStore
import com.passvault.core.domain.repository.ThemePreference
import com.passvault.core.domain.repository.VaultRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SettingsViewModel(
    private val vaultRepository: VaultRepository,
    private val appSettingsStore: AppSettingsStore,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private val _effect = Channel<SettingsEffect>(Channel.BUFFERED)
    val effect: Flow<SettingsEffect> = _effect.receiveAsFlow()

    private val settingsSaveMutex = Mutex()
    private var settingsRevision = 0L
    private var passwordChangeJob: Job? = null

    init {
        loadPreferences()
        loadVaultMetadata()
        viewModelScope.launch {
            vaultRepository.getSessionState().collect { session ->
                if (session is VaultSessionState.Unlocked) {
                    loadVaultMetadata()
                }
            }
        }
    }

    private fun loadPreferences() {
        val revisionAtStart = settingsRevision
        viewModelScope.launch {
            appSettingsStore.load()
                .onSuccess { settings ->
                    if (settingsRevision == revisionAtStart) {
                        _state.update {
                            it.copy(
                                theme = settings.theme.toAppTheme(),
                                autoLockTimeoutMinutes = settings.autoLockTimeoutMinutes,
                                clipboardClearSeconds = settings.clipboardClearSeconds,
                            )
                        }
                    }
                }
                .onFailure {
                    _state.update {
                        it.copy(errorMessage = uiText(Res.string.error_settings_load))
                    }
                }
        }
    }

    private fun loadVaultMetadata() {
        viewModelScope.launch {
            vaultRepository.getMetadata()
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
        }
    }

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.OnThemeChanged -> {
                persistPreferences { it.copy(theme = event.theme) }
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
            SettingsEvent.OnSecurityClick -> _effect.trySend(SettingsEffect.NavigateToSecurity)
            SettingsEvent.OnAppearanceClick -> _effect.trySend(SettingsEffect.NavigateToAppearance)
            SettingsEvent.OnDataClick -> _effect.trySend(SettingsEffect.NavigateToData)
            SettingsEvent.OnBackClick -> _effect.trySend(SettingsEffect.NavigateBack)
            SettingsEvent.OnLockVaultClick -> lockVault()
            SettingsEvent.OnChangePasswordClick -> {
                _state.update { it.copy(showChangePasswordDialog = true, passwordError = null) }
            }
            SettingsEvent.OnExportClick -> _effect.trySend(SettingsEffect.ShowExportDialog)
            SettingsEvent.OnImportClick -> _effect.trySend(SettingsEffect.ShowImportDialog)
            SettingsEvent.OnBackupClick -> _effect.trySend(SettingsEffect.ShowBackupDialog)
            is SettingsEvent.OnCurrentPasswordChanged -> {
                if (event.password.length <= MAX_PASSWORD_LENGTH) {
                    _state.update { it.copy(currentPassword = event.password, passwordError = null) }
                }
            }
            is SettingsEvent.OnNewPasswordChanged -> {
                if (event.password.length <= MAX_PASSWORD_LENGTH) {
                    _state.update { it.copy(newPassword = event.password, passwordError = null) }
                    evaluatePasswordStrength(event.password)
                }
            }
            is SettingsEvent.OnConfirmPasswordChanged -> {
                if (event.password.length <= MAX_PASSWORD_LENGTH) {
                    _state.update { it.copy(confirmPassword = event.password, passwordError = null) }
                }
            }
            SettingsEvent.OnChangePasswordConfirm -> changePassword()
            SettingsEvent.OnChangePasswordCancel -> {
                if (!_state.value.isChangingPassword) {
                    clearPasswordDialog()
                }
            }
            SettingsEvent.OnDismissError -> _state.update { it.copy(errorMessage = null) }
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

    private fun persistPreferences(transform: (SettingsState) -> SettingsState) {
        val updated = transform(_state.value)
        val revision = ++settingsRevision
        _state.value = updated

        passwordChangeJob?.cancel()
        passwordChangeJob = viewModelScope.launch {
            settingsSaveMutex.withLock {
                if (revision != settingsRevision) return@withLock

                appSettingsStore.save(updated.toAppSettings())
                    .onFailure {
                        if (revision == settingsRevision) {
                            _state.update {
                                it.copy(errorMessage = uiText(Res.string.error_settings_save))
                            }
                        }
                    }
            }
        }
    }

    private fun lockVault() {
        viewModelScope.launch {
            try {
                vaultRepository.lock()
                    .onSuccess { _effect.trySend(SettingsEffect.LockVault) }
                    .onFailure {
                        _state.update { it.copy(errorMessage = uiText(Res.string.error_settings_lock)) }
                    }
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Exception) {
                _state.update { it.copy(errorMessage = uiText(Res.string.error_settings_lock)) }
            }
        }
    }

    private fun evaluatePasswordStrength(password: String) {
        val score = if (password.isEmpty()) PasswordScore.UNKNOWN else PasswordStrengthEvaluator.score(password)
        val strength = when {
            password.isEmpty() -> PasswordStrength.EMPTY
            password.length < 12 -> PasswordStrength.TOO_SHORT
            score <= PasswordScore.WEAK -> PasswordStrength.WEAK
            score <= PasswordScore.GOOD -> PasswordStrength.GOOD
            else -> PasswordStrength.STRONG
        }
        _state.update { it.copy(passwordStrength = strength) }
    }

    private fun changePassword() {
        val currentState = _state.value
        if (currentState.isChangingPassword) return

        when {
            currentState.currentPassword.isEmpty() -> {
                _state.update { it.copy(passwordError = uiText(Res.string.error_current_password_required)) }
                return
            }
            currentState.newPassword.length < 12 -> {
                _state.update { it.copy(passwordError = uiText(Res.string.error_new_password_too_short)) }
                return
            }
            PasswordStrengthEvaluator.score(currentState.newPassword) < PasswordScore.FAIR -> {
                _state.update {
                    it.copy(passwordError = uiText(Res.string.error_new_password_weak))
                }
                return
            }
            currentState.newPassword != currentState.confirmPassword -> {
                _state.update { it.copy(passwordError = uiText(Res.string.error_master_password_mismatch)) }
                return
            }
        }

        val currentSensitive = SensitiveText.from(currentState.currentPassword)
        val newSensitive = SensitiveText.from(currentState.newPassword)
        _state.update {
            it.copy(
                isChangingPassword = true,
                currentPassword = "",
                newPassword = "",
                confirmPassword = "",
                passwordError = null,
            )
        }

        viewModelScope.launch {
            try {
                vaultRepository.changeMasterPassword(currentSensitive, newSensitive)
                    .onSuccess {
                        _state.update {
                            it.copy(
                                isChangingPassword = false,
                                showChangePasswordDialog = false,
                                passwordStrength = PasswordStrength.EMPTY,
                            )
                        }
                        _effect.trySend(
                            SettingsEffect.ShowMessage(
                                uiText(Res.string.message_master_password_changed),
                            ),
                        )
                    }
                    .onFailure {
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
        passwordChangeJob?.cancel()
        passwordChangeJob = null
        clearPasswordDialog()
        _state.update {
            it.copy(
                vaultCreatedAt = "",
                vaultEntryCount = 0,
                errorMessage = null,
                infoDialogTitle = null,
                infoDialogMessage = null,
                isLoading = false,
            )
        }
    }

    data class SettingsState(
        val theme: AppTheme = AppTheme.SYSTEM,
        val autoLockTimeoutMinutes: Int = AppSettings.DEFAULT_AUTO_LOCK_TIMEOUT_MINUTES,
        val clipboardClearSeconds: Int = AppSettings.DEFAULT_CLIPBOARD_CLEAR_SECONDS,
        val vaultCreatedAt: String = "",
        val vaultEntryCount: Int = 0,
        val isLoading: Boolean = true,
        val errorMessage: UiText? = null,
        val showChangePasswordDialog: Boolean = false,
        val currentPassword: String = "",
        val newPassword: String = "",
        val confirmPassword: String = "",
        val passwordStrength: PasswordStrength = PasswordStrength.EMPTY,
        val passwordError: UiText? = null,
        val isChangingPassword: Boolean = false,
        val infoDialogTitle: UiText? = null,
        val infoDialogMessage: UiText? = null,
    ) {
        val passwordsMatch: Boolean
            get() = newPassword == confirmPassword && confirmPassword.isNotEmpty()

        val canChangePassword: Boolean
            get() = currentPassword.isNotEmpty() &&
                newPassword.length >= 12 &&
                PasswordStrengthEvaluator.score(newPassword) >= PasswordScore.FAIR &&
                passwordsMatch &&
                !isChangingPassword

        fun toAppSettings(): AppSettings = AppSettings(
            theme = when (theme) {
                AppTheme.LIGHT -> ThemePreference.LIGHT
                AppTheme.DARK -> ThemePreference.DARK
                AppTheme.SYSTEM -> ThemePreference.SYSTEM
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

    enum class PasswordStrength {
        EMPTY,
        TOO_SHORT,
        WEAK,
        GOOD,
        STRONG,
    }

    sealed interface SettingsEvent {
        data class OnThemeChanged(val theme: AppTheme) : SettingsEvent
        data class OnAutoLockTimeoutChanged(val minutes: Int) : SettingsEvent
        data class OnClipboardClearChanged(val seconds: Int) : SettingsEvent
        data object OnSecurityClick : SettingsEvent
        data object OnAppearanceClick : SettingsEvent
        data object OnDataClick : SettingsEvent
        data object OnBackClick : SettingsEvent
        data object OnLockVaultClick : SettingsEvent
        data object OnChangePasswordClick : SettingsEvent
        data object OnExportClick : SettingsEvent
        data object OnImportClick : SettingsEvent
        data object OnBackupClick : SettingsEvent
        data class OnCurrentPasswordChanged(val password: String) : SettingsEvent
        data class OnNewPasswordChanged(val password: String) : SettingsEvent
        data class OnConfirmPasswordChanged(val password: String) : SettingsEvent
        data object OnChangePasswordConfirm : SettingsEvent
        data object OnChangePasswordCancel : SettingsEvent
        data object OnDismissError : SettingsEvent
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
        data class ShowMessage(val message: UiText) : SettingsEffect
    }

    private fun ThemePreference.toAppTheme(): AppTheme = when (this) {
        ThemePreference.LIGHT -> AppTheme.LIGHT
        ThemePreference.DARK -> AppTheme.DARK
        ThemePreference.SYSTEM -> AppTheme.SYSTEM
    }

    private companion object {
        const val MAX_PASSWORD_LENGTH = 1_024
    }
}
