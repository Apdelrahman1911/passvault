package com.passvault.feature.unlock.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.text.UiText
import com.passvault.core.designsystem.text.uiText
import com.passvault.core.domain.model.MasterPasswordPolicy
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.model.VaultSessionState
import com.passvault.core.domain.model.codePointLength
import com.passvault.core.domain.model.hasWellFormedUnicode
import com.passvault.core.domain.model.takeCodePoints
import com.passvault.core.domain.repository.VaultRepository
import com.passvault.core.security.BiometricAvailability
import com.passvault.core.security.BiometricFailureReason
import com.passvault.core.security.BiometricOperationResult
import com.passvault.core.security.BiometricType
import com.passvault.core.security.BiometricUnlockService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

class UnlockViewModel(
    private val vaultRepository: VaultRepository,
    private val biometricUnlockService: BiometricUnlockService,
) : ViewModel() {

    private val _state = MutableStateFlow(UnlockState())
    val state: StateFlow<UnlockState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<UnlockEffect>(extraBufferCapacity = 1)
    val effect: SharedFlow<UnlockEffect> = _effect.asSharedFlow()
    private val unlockMutex = Mutex()
    private var lockoutResetJob: Job? = null
    private var unlockJob: Job? = null
    private var biometricStatusJob: Job? = null
    private var lastNavigatedSession: String? = null

    init {
        observeSessionState()
        checkVaultExists()
        loadBiometricStatus()
    }

    private fun observeSessionState() {
        viewModelScope.launch {
            vaultRepository.getSessionState().collect { sessionState ->
                when (sessionState) {
                    is VaultSessionState.Unlocked -> {
                        _state.update { it.copy(isLoading = false, isBiometricLoading = false) }
                        val sessionId = sessionState.sessionId.value
                        if (lastNavigatedSession != sessionId) {
                            lastNavigatedSession = sessionId
                            _effect.tryEmit(UnlockEffect.NavigateToVault)
                        }
                    }
                    is VaultSessionState.Unlocking -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                    is VaultSessionState.Locking -> {
                        _state.update { it.copy(isLoading = false, isBiometricLoading = false) }
                    }
                    is VaultSessionState.Locked -> {
                        _state.update { it.copy(isLoading = false, isBiometricLoading = false) }
                    }
                    else -> { }
                }
            }
        }
    }

    private fun checkVaultExists() {
        unlockJob = viewModelScope.launch {
            // Allow bootstrap/provisioning to settle before the first probe.
            kotlinx.coroutines.yield()
            try {
                val result = vaultRepository.exists()
                currentCoroutineContext().ensureActive()
                result.onSuccess { exists ->
                    if (!exists && _state.value.failedAttempts == 0) {
                        _effect.tryEmit(UnlockEffect.NavigateToOnboarding)
                    }
                }.onFailure {
                    _state.update { it.copy(errorMessage = uiText(Res.string.error_unlock_inspect)) }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                currentCoroutineContext().ensureActive()
                _state.update { it.copy(errorMessage = uiText(Res.string.error_unlock_inspect)) }
            }
        }
    }

    fun onEvent(event: UnlockEvent) {
        when (event) {
            is UnlockEvent.OnPasswordChanged -> {
                val password = event.password.takeCodePoints(MasterPasswordPolicy.MAX_LENGTH + 1)
                _state.update {
                    it.copy(
                        password = password,
                        errorMessage = password.inputValidationError(),
                    )
                }
            }
            UnlockEvent.OnUnlockClick -> unlock()
            UnlockEvent.OnBiometricUnlockClick -> unlockWithBiometrics()
            UnlockEvent.OnDismissError -> {
                _state.update { it.copy(errorMessage = null) }
            }
            UnlockEvent.OnForgotPasswordClick -> {
                _state.update { it.copy(showRecoveryInfo = true) }
            }
            UnlockEvent.OnDismissRecoveryInfo -> {
                _state.update { it.copy(showRecoveryInfo = false) }
            }
        }
    }

    private fun unlock() {
        val current = _state.value
        val validationError = current.passwordUnlockError()
        when {
            validationError != null -> _state.update { it.copy(errorMessage = validationError) }
            unlockMutex.tryLock() -> startPasswordUnlock(current.password)
            else -> Unit
        }
    }

    private fun startPasswordUnlock(password: String) {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        unlockJob?.cancel()
        val job = viewModelScope.launch {
            val sensitivePassword = SensitiveText.from(password)
            try {
                val result = vaultRepository.unlock(sensitivePassword)
                currentCoroutineContext().ensureActive()
                if (result.isSuccess) {
                    lockoutResetJob?.cancel()
                    _state.update {
                        it.copy(
                            isLoading = false,
                            password = "",
                            failedAttempts = 0,
                        )
                    }
                } else {
                    currentCoroutineContext().ensureActive()
                    handlePasswordUnlockFailure()
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                currentCoroutineContext().ensureActive()
                handlePasswordUnlockFailure()
            } finally {
                sensitivePassword.clear()
            }
        }
        job.invokeOnCompletion { unlockMutex.unlock() }
        unlockJob = job
    }

    private fun handlePasswordUnlockFailure() {
        val attempts = _state.value.failedAttempts + 1
        _state.update {
            it.copy(
                isLoading = false,
                password = "",
                errorMessage = uiText(
                    if (attempts >= MAX_FAILED_ATTEMPTS) {
                        Res.string.error_unlock_cooldown
                    } else {
                        Res.string.error_unlock_failed
                    },
                ),
                failedAttempts = attempts,
            )
        }
        if (attempts >= MAX_FAILED_ATTEMPTS) scheduleLockoutReset()
    }

    private fun loadBiometricStatus() {
        biometricStatusJob?.cancel()
        biometricStatusJob = viewModelScope.launch {
            try {
                val status = biometricUnlockService.getStatus()
                currentCoroutineContext().ensureActive()
                _state.update {
                    it.copy(
                        biometricType = status.capability.type,
                        biometricAvailability = status.capability.availability,
                        isBiometricEnabled = status.isEnabled,
                        isBiometricStatusLoaded = true,
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
                        isBiometricStatusLoaded = true,
                    )
                }
            }
        }
    }

    private fun unlockWithBiometrics() {
        val currentState = _state.value
        val validationError = currentState.biometricUnlockError()
        when {
            validationError != null -> _state.update { it.copy(errorMessage = validationError) }
            unlockMutex.tryLock() -> startBiometricUnlock()
            else -> Unit
        }
    }

    private fun startBiometricUnlock() {
        _state.update { it.copy(isLoading = true, isBiometricLoading = true, errorMessage = null) }
        unlockJob?.cancel()
        val job = viewModelScope.launch {
            try {
                val result = biometricUnlockService.unlock()
                currentCoroutineContext().ensureActive()
                if (result !is BiometricOperationResult.Success) {
                    _state.update { it.afterBiometricResult(result) }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                currentCoroutineContext().ensureActive()
                _state.update {
                    it.copy(
                        isLoading = false,
                        isBiometricLoading = false,
                        errorMessage = uiText(Res.string.error_biometric_failed),
                    )
                }
            }
        }
        job.invokeOnCompletion { unlockMutex.unlock() }
        unlockJob = job
    }

    /**
     * Clears all user-entered authentication material when the application
     * locks or leaves the unlock flow.
     */
    fun clearForLock() {
        unlockJob?.cancel()
        unlockJob = null
        biometricStatusJob?.cancel()
        biometricStatusJob = null
        lockoutResetJob?.cancel()
        _state.value = UnlockState()
        loadBiometricStatus()
    }

    data class UnlockState(
        val password: String = "",
        val isLoading: Boolean = false,
        val errorMessage: UiText? = null,
        val failedAttempts: Int = 0,
        val showRecoveryInfo: Boolean = false,
        val biometricType: BiometricType = BiometricType.GENERIC,
        val biometricAvailability: BiometricAvailability = BiometricAvailability.UNAVAILABLE,
        val isBiometricEnabled: Boolean = false,
        val isBiometricLoading: Boolean = false,
        val isBiometricStatusLoaded: Boolean = false,
    ) {
        val canUnlock: Boolean
            get() = password.isNotEmpty() &&
                password.codePointLength() <= MasterPasswordPolicy.MAX_LENGTH &&
                password.hasWellFormedUnicode() &&
                !isLoading &&
                !isLockedOut
        val canUseBiometrics: Boolean
            get() = isBiometricStatusLoaded &&
                biometricAvailability == BiometricAvailability.AVAILABLE &&
                isBiometricEnabled
        // This is a bounded local cooldown; it is reset automatically and never
        // permanently denies access to the vault.
        val isLockedOut: Boolean get() = failedAttempts >= MAX_FAILED_ATTEMPTS
    }

    sealed interface UnlockEvent {
        data class OnPasswordChanged(val password: String) : UnlockEvent
        data object OnUnlockClick : UnlockEvent
        data object OnBiometricUnlockClick : UnlockEvent
        data object OnDismissError : UnlockEvent
        data object OnForgotPasswordClick : UnlockEvent
        data object OnDismissRecoveryInfo : UnlockEvent
    }

    sealed interface UnlockEffect {
        data object NavigateToVault : UnlockEffect
        data object NavigateToOnboarding : UnlockEffect
    }

    private fun scheduleLockoutReset() {
        lockoutResetJob?.cancel()
        lockoutResetJob = viewModelScope.launch {
            delay(LOCKOUT_WINDOW_MS)
            _state.update { it.copy(failedAttempts = 0, errorMessage = null) }
        }
    }

    private companion object {
        const val MAX_FAILED_ATTEMPTS = 5
        const val LOCKOUT_WINDOW_MS = 30_000L
    }
}

private fun UnlockViewModel.UnlockState.passwordUnlockError(): UiText? = when {
    isLockedOut -> uiText(Res.string.error_unlock_cooldown)
    password.isEmpty() -> uiText(Res.string.error_unlock_password_required)
    else -> password.inputValidationError()
}

private fun String.inputValidationError(): UiText? = when {
    codePointLength() > MasterPasswordPolicy.MAX_LENGTH ->
        uiText(Res.string.error_master_password_too_long, MasterPasswordPolicy.MAX_LENGTH)
    !hasWellFormedUnicode() -> uiText(Res.string.error_master_password_invalid)
    else -> null
}

private fun UnlockViewModel.UnlockState.biometricUnlockError(): UiText? = when {
    biometricAvailability == BiometricAvailability.NOT_ENROLLED ->
        uiText(Res.string.error_biometric_not_enrolled)
    biometricAvailability == BiometricAvailability.UNAVAILABLE ->
        uiText(Res.string.error_biometric_unavailable)
    !isBiometricEnabled -> uiText(Res.string.error_biometric_not_enabled)
    else -> null
}

private fun UnlockViewModel.UnlockState.afterBiometricResult(
    result: BiometricOperationResult,
): UnlockViewModel.UnlockState = when (result) {
    BiometricOperationResult.Success -> this
    BiometricOperationResult.Cancelled -> copy(
        isLoading = false,
        isBiometricLoading = false,
    )
    is BiometricOperationResult.Failure -> copy(
        isLoading = false,
        isBiometricLoading = false,
        isBiometricEnabled = if (
            result.reason == BiometricFailureReason.INVALIDATED ||
            result.reason == BiometricFailureReason.NOT_ENABLED
        ) {
            false
        } else {
            isBiometricEnabled
        },
        biometricAvailability = when (result.reason) {
            BiometricFailureReason.NOT_ENROLLED -> BiometricAvailability.NOT_ENROLLED
            BiometricFailureReason.NOT_AVAILABLE -> BiometricAvailability.UNAVAILABLE
            else -> biometricAvailability
        },
        errorMessage = result.reason.toUnlockMessage(),
    )
}

private fun BiometricFailureReason.toUnlockMessage(): UiText = when (this) {
    BiometricFailureReason.NOT_AVAILABLE -> uiText(Res.string.error_biometric_unavailable)
    BiometricFailureReason.NOT_ENROLLED -> uiText(Res.string.error_biometric_not_enrolled)
    BiometricFailureReason.NOT_ENABLED -> uiText(Res.string.error_biometric_not_enabled)
    BiometricFailureReason.INVALIDATED -> uiText(Res.string.error_biometric_invalidated)
    BiometricFailureReason.VAULT_LOCKED,
    BiometricFailureReason.AUTHENTICATION_FAILED,
    BiometricFailureReason.INTERNAL_ERROR,
    -> uiText(Res.string.error_biometric_failed)
}
