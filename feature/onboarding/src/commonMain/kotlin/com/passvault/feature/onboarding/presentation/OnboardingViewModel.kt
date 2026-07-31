package com.passvault.feature.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.text.UiText
import com.passvault.core.designsystem.text.uiText
import com.passvault.core.domain.model.PasswordScore
import com.passvault.core.domain.model.PasswordStrengthEvaluator
import com.passvault.core.domain.model.SensitiveText
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

class OnboardingViewModel(
    private val vaultRepository: VaultRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    private val _effect = Channel<OnboardingEffect>(Channel.BUFFERED)
    val effect: Flow<OnboardingEffect> = _effect.receiveAsFlow()

    private val createMutex = Mutex()
    private var createJob: Job? = null

    fun onEvent(event: OnboardingEvent) {
        when (event) {
            is OnboardingEvent.OnPasswordChanged -> updateMasterPassword(event.password)
            is OnboardingEvent.OnConfirmPasswordChanged -> updateConfirmation(event.password)
            OnboardingEvent.OnCreateVaultClick -> continueToConfirmation()
            OnboardingEvent.OnConfirmPasswordClick -> createAndVerifyVault()
            OnboardingEvent.OnBackClick -> {
                if (!_state.value.isLoading) {
                    clearPasswordInputs()
                    _effect.trySend(OnboardingEffect.NavigateBack)
                }
            }
            OnboardingEvent.OnContinueToSecurityClick -> {
                _effect.trySend(OnboardingEffect.NavigateToSecurityExplanation)
            }
            OnboardingEvent.OnCompleteSetupClick -> completeOnboarding()
            OnboardingEvent.OnGetStartedClick -> {
                _effect.trySend(OnboardingEffect.NavigateToMasterPasswordCreation)
            }
        }
    }

    private fun updateMasterPassword(password: String) {
        if (password.length > MAX_MASTER_PASSWORD_LENGTH) {
            _state.update {
                it.copy(
                    errorMessage = uiText(
                        Res.string.error_master_password_too_long,
                        MAX_MASTER_PASSWORD_LENGTH,
                    ),
                )
            }
            return
        }

        val strength = calculatePasswordStrength(password)
        _state.update {
            it.copy(
                masterPassword = password,
                confirmPassword = "",
                passwordsMatch = false,
                passwordStrength = strength,
                strengthFeedback = strength.feedback,
                errorMessage = null,
            )
        }
    }

    private fun updateConfirmation(password: String) {
        if (password.length > MAX_MASTER_PASSWORD_LENGTH) {
            _state.update {
                it.copy(errorMessage = uiText(Res.string.error_master_confirmation_too_long))
            }
            return
        }

        _state.update {
            it.copy(
                confirmPassword = password,
                passwordsMatch = password.isNotEmpty() && it.masterPassword == password,
                errorMessage = null,
            )
        }
    }

    private fun calculatePasswordStrength(password: String): PasswordStrength {
        if (password.length < MIN_MASTER_PASSWORD_LENGTH) return PasswordStrength.TOO_SHORT

        return when (PasswordStrengthEvaluator.score(password)) {
            PasswordScore.UNKNOWN,
            PasswordScore.VERY_WEAK,
            -> PasswordStrength.VERY_WEAK
            PasswordScore.WEAK -> PasswordStrength.WEAK
            PasswordScore.FAIR -> PasswordStrength.FAIR
            PasswordScore.GOOD -> PasswordStrength.GOOD
            PasswordScore.STRONG -> PasswordStrength.STRONG
            PasswordScore.VERY_STRONG -> PasswordStrength.VERY_STRONG
        }
    }

    private fun continueToConfirmation() {
        val currentState = _state.value
        if (currentState.isLoading) return

        when {
            currentState.masterPassword.length < MIN_MASTER_PASSWORD_LENGTH -> {
                _state.update {
                    it.copy(
                        errorMessage = uiText(
                            Res.string.error_master_password_too_short,
                            MIN_MASTER_PASSWORD_LENGTH,
                        ),
                    )
                }
            }
            currentState.passwordStrength < PasswordStrength.FAIR -> {
                _state.update {
                    it.copy(errorMessage = uiText(Res.string.error_master_password_predictable))
                }
            }
            else -> {
                _state.update { it.copy(errorMessage = null, confirmPassword = "", passwordsMatch = false) }
                _effect.trySend(OnboardingEffect.NavigateToMasterPasswordConfirmation)
            }
        }
    }

    private fun validateConfirmation(): Boolean {
        val currentState = _state.value
        val error = when {
            currentState.masterPassword.length < MIN_MASTER_PASSWORD_LENGTH ->
                uiText(Res.string.error_master_password_too_short, MIN_MASTER_PASSWORD_LENGTH)
            currentState.passwordStrength < PasswordStrength.FAIR ->
                uiText(Res.string.error_master_password_predictable)
            currentState.confirmPassword.isEmpty() ->
                uiText(Res.string.error_master_confirmation_required)
            currentState.masterPassword != currentState.confirmPassword ->
                uiText(Res.string.error_master_password_mismatch)
            else -> null
        }
        _state.update { it.copy(errorMessage = error) }
        return error == null
    }

    private fun createAndVerifyVault() {
        if (_state.value.isLoading || !validateConfirmation()) return
        val password = _state.value.masterPassword
        _state.update { it.copy(isLoading = true, errorMessage = null) }

        createJob?.cancel()
        createJob = viewModelScope.launch {
            createMutex.withLock {
                var createPassword: SensitiveText? = null
                var verifyPassword: SensitiveText? = null
                try {
                    val vaultExists = vaultRepository.exists().getOrElse {
                        showCreationError(uiText(Res.string.error_vault_setup_check))
                        return@withLock
                    }

                    if (!vaultExists) {
                        createPassword = SensitiveText.from(password)
                        vaultRepository.create(createPassword).getOrElse {
                            showCreationError(uiText(Res.string.error_vault_setup_create))
                            return@withLock
                        }
                        _state.update { it.copy(vaultCreated = true) }
                    } else if (!_state.value.vaultCreated) {
                        showCreationError(uiText(Res.string.error_vault_setup_exists))
                        return@withLock
                    }

                    verifyPassword = SensitiveText.from(password)
                    vaultRepository.unlock(verifyPassword)
                        .onSuccess {
                            _state.update {
                                it.copy(
                                    masterPassword = "",
                                    confirmPassword = "",
                                    passwordsMatch = false,
                                    isLoading = false,
                                    errorMessage = null,
                                )
                            }
                            _effect.trySend(OnboardingEffect.NavigateToSecurityExplanation)
                        }
                        .onFailure {
                            showCreationError(uiText(Res.string.error_vault_setup_verify))
                        }
                } catch (cancel: CancellationException) {
                    throw cancel
                } catch (_: Exception) {
                    showCreationError(uiText(Res.string.error_vault_setup_finish))
                } finally {
                    createPassword?.clear()
                    verifyPassword?.clear()
                    _state.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    private fun showCreationError(message: UiText) {
        _state.update { it.copy(isLoading = false, errorMessage = message) }
    }

    private fun completeOnboarding() {
        clearPasswordInputs()
        _effect.trySend(OnboardingEffect.NavigateToVault)
    }

    fun clearForLock() {
        createJob?.cancel()
        createJob = null
        clearPasswordInputs()
        _state.value = OnboardingState()
    }

    private fun clearPasswordInputs() {
        _state.update {
            it.copy(
                masterPassword = "",
                confirmPassword = "",
                passwordsMatch = false,
            )
        }
    }

    data class OnboardingState(
        val masterPassword: String = "",
        val confirmPassword: String = "",
        val passwordStrength: PasswordStrength = PasswordStrength.TOO_SHORT,
        val strengthFeedback: UiText = PasswordStrength.TOO_SHORT.feedback,
        val passwordsMatch: Boolean = false,
        val isLoading: Boolean = false,
        val vaultCreated: Boolean = false,
        val errorMessage: UiText? = null,
    ) {
        val canContinueToConfirmation: Boolean
            get() = masterPassword.length >= MIN_MASTER_PASSWORD_LENGTH &&
                passwordStrength >= PasswordStrength.FAIR &&
                !isLoading

        val canCreateVault: Boolean
            get() = canContinueToConfirmation &&
                confirmPassword.isNotEmpty() &&
                passwordsMatch
    }

    sealed interface OnboardingEvent {
        data class OnPasswordChanged(val password: String) : OnboardingEvent
        data class OnConfirmPasswordChanged(val password: String) : OnboardingEvent
        data object OnCreateVaultClick : OnboardingEvent
        data object OnConfirmPasswordClick : OnboardingEvent
        data object OnBackClick : OnboardingEvent
        data object OnContinueToSecurityClick : OnboardingEvent
        data object OnCompleteSetupClick : OnboardingEvent
        data object OnGetStartedClick : OnboardingEvent
    }

    sealed interface OnboardingEffect {
        data object NavigateBack : OnboardingEffect
        data object NavigateToMasterPasswordCreation : OnboardingEffect
        data object NavigateToMasterPasswordConfirmation : OnboardingEffect
        data object NavigateToSecurityExplanation : OnboardingEffect
        data object NavigateToVault : OnboardingEffect
    }

    enum class PasswordStrength(
        val feedback: UiText,
    ) {
        TOO_SHORT(uiText(Res.string.error_master_password_too_short, MIN_MASTER_PASSWORD_LENGTH)),
        VERY_WEAK(uiText(Res.string.feedback_master_very_weak)),
        WEAK(uiText(Res.string.feedback_master_weak)),
        FAIR(uiText(Res.string.feedback_master_fair)),
        GOOD(uiText(Res.string.feedback_master_good)),
        STRONG(uiText(Res.string.feedback_master_strong)),
        VERY_STRONG(uiText(Res.string.feedback_master_very_strong)),
    }

    private companion object {
        const val MIN_MASTER_PASSWORD_LENGTH = 12
        const val MAX_MASTER_PASSWORD_LENGTH = 1_024
    }
}
