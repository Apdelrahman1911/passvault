package com.passvault.feature.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.text.UiText
import com.passvault.core.designsystem.text.uiText
import com.passvault.core.domain.model.MasterPasswordPolicy
import com.passvault.core.domain.model.PasswordScore
import com.passvault.core.domain.model.PasswordStrengthEvaluator
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.model.codePointLength
import com.passvault.core.domain.model.hasWellFormedUnicode
import com.passvault.core.domain.model.takeCodePoints
import com.passvault.core.domain.repository.VaultRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    private val _effect = MutableSharedFlow<OnboardingEffect>(extraBufferCapacity = 1)
    val effect: SharedFlow<OnboardingEffect> = _effect.asSharedFlow()

    private val createMutex = Mutex()
    private var createJob: Job? = null

    fun onEvent(event: OnboardingEvent) {
        when (event) {
            is OnboardingEvent.OnPasswordChanged -> updateMasterPassword(event.password)
            is OnboardingEvent.OnConfirmPasswordChanged -> updateConfirmation(event.password)
            OnboardingEvent.OnCreateVaultClick -> continueToConfirmation()
            OnboardingEvent.OnConfirmPasswordClick -> createAndVerifyVault()
            OnboardingEvent.OnBackClick -> {
                if (!_state.value.isLoading && !_state.value.vaultCreated) {
                    clearPasswordInputs()
                    _effect.tryEmit(OnboardingEffect.NavigateBack)
                }
            }
            OnboardingEvent.OnCompleteSetupClick -> completeOnboarding()
            OnboardingEvent.OnGetStartedClick -> {
                _effect.tryEmit(OnboardingEffect.NavigateToMasterPasswordCreation)
            }
        }
    }

    private fun updateMasterPassword(password: String) {
        val boundedPassword = password.takeCodePoints(MasterPasswordPolicy.MAX_LENGTH + 1)
        val validationError = when {
            password.codePointLength() > MasterPasswordPolicy.MAX_LENGTH -> uiText(
                Res.string.error_master_password_too_long,
                MasterPasswordPolicy.MAX_LENGTH,
            )
            !password.hasWellFormedUnicode() -> uiText(Res.string.error_master_password_invalid)
            else -> null
        }
        if (validationError != null) {
            _state.update {
                it.copy(
                    masterPassword = boundedPassword,
                    confirmPassword = "",
                    passwordsMatch = false,
                    errorMessage = validationError,
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
        val validationError = when {
            password.codePointLength() > MasterPasswordPolicy.MAX_LENGTH ->
                uiText(Res.string.error_master_confirmation_too_long)
            !password.hasWellFormedUnicode() -> uiText(Res.string.error_master_password_invalid)
            else -> null
        }
        if (validationError != null) {
            _state.update {
                it.copy(
                    confirmPassword = password.takeCodePoints(MasterPasswordPolicy.MAX_LENGTH + 1),
                    passwordsMatch = false,
                    errorMessage = validationError,
                )
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
        if (password.codePointLength() < MasterPasswordPolicy.MIN_LENGTH) return PasswordStrength.TOO_SHORT

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
        val passwordLength = currentState.masterPassword.codePointLength()

        when {
            passwordLength < MasterPasswordPolicy.MIN_LENGTH -> {
                _state.update {
                    it.copy(
                        errorMessage = uiText(
                            Res.string.error_master_password_too_short,
                            MasterPasswordPolicy.MIN_LENGTH,
                        ),
                    )
                }
            }
            passwordLength > MasterPasswordPolicy.MAX_LENGTH -> {
                _state.update {
                    it.copy(
                        errorMessage = uiText(
                            Res.string.error_master_password_too_long,
                            MasterPasswordPolicy.MAX_LENGTH,
                        ),
                    )
                }
            }
            !currentState.masterPassword.hasWellFormedUnicode() -> {
                _state.update {
                    it.copy(errorMessage = uiText(Res.string.error_master_password_invalid))
                }
            }
            !MasterPasswordPolicy.accepts(currentState.masterPassword) -> {
                _state.update {
                    it.copy(errorMessage = uiText(Res.string.error_master_password_predictable))
                }
            }
            else -> {
                _state.update { it.copy(errorMessage = null, confirmPassword = "", passwordsMatch = false) }
                _effect.tryEmit(OnboardingEffect.NavigateToMasterPasswordConfirmation)
            }
        }
    }

    private fun validateConfirmation(): Boolean {
        val currentState = _state.value
        val passwordLength = currentState.masterPassword.codePointLength()
        val error = when {
            passwordLength < MasterPasswordPolicy.MIN_LENGTH ->
                uiText(Res.string.error_master_password_too_short, MasterPasswordPolicy.MIN_LENGTH)
            passwordLength > MasterPasswordPolicy.MAX_LENGTH ->
                uiText(Res.string.error_master_password_too_long, MasterPasswordPolicy.MAX_LENGTH)
            !currentState.masterPassword.hasWellFormedUnicode() ||
                !currentState.confirmPassword.hasWellFormedUnicode() ->
                uiText(Res.string.error_master_password_invalid)
            !MasterPasswordPolicy.accepts(currentState.masterPassword) ->
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
                    val existsResult = vaultRepository.exists()
                    currentCoroutineContext().ensureActive()
                    val vaultExists = existsResult.getOrElse {
                        showCreationError(uiText(Res.string.error_vault_setup_check))
                        return@withLock
                    }

                    if (!vaultExists) {
                        createPassword = SensitiveText.from(password)
                        val createResult = vaultRepository.create(createPassword)
                        currentCoroutineContext().ensureActive()
                        if (createResult.isFailure) {
                            showCreationError(uiText(Res.string.error_vault_setup_create))
                            return@withLock
                        }
                        _state.update { it.copy(vaultCreated = true) }
                    } else if (!_state.value.vaultCreated) {
                        showCreationError(uiText(Res.string.error_vault_setup_exists))
                        return@withLock
                    }

                    verifyPassword = SensitiveText.from(password)
                    val unlockResult = vaultRepository.unlock(verifyPassword)
                    currentCoroutineContext().ensureActive()
                    if (unlockResult.isSuccess) {
                            _state.update {
                                it.copy(
                                    masterPassword = "",
                                    confirmPassword = "",
                                    passwordsMatch = false,
                                    isLoading = false,
                                    errorMessage = null,
                                )
                            }
                            _effect.tryEmit(OnboardingEffect.NavigateToSecurityExplanation)
                    } else {
                        currentCoroutineContext().ensureActive()
                        showCreationError(uiText(Res.string.error_vault_setup_verify))
                    }
                } catch (cancel: CancellationException) {
                    throw cancel
                } catch (_: Exception) {
                    currentCoroutineContext().ensureActive()
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
        _effect.tryEmit(OnboardingEffect.NavigateToVault)
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
                passwordStrength = PasswordStrength.TOO_SHORT,
                strengthFeedback = PasswordStrength.TOO_SHORT.feedback,
                errorMessage = null,
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
            get() = MasterPasswordPolicy.accepts(masterPassword) && !isLoading

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
        TOO_SHORT(uiText(Res.string.error_master_password_too_short, MasterPasswordPolicy.MIN_LENGTH)),
        VERY_WEAK(uiText(Res.string.feedback_master_very_weak)),
        WEAK(uiText(Res.string.feedback_master_weak)),
        FAIR(uiText(Res.string.feedback_master_fair)),
        GOOD(uiText(Res.string.feedback_master_good)),
        STRONG(uiText(Res.string.feedback_master_strong)),
        VERY_STRONG(uiText(Res.string.feedback_master_very_strong)),
    }

}
