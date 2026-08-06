package com.passvault.feature.unlock.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.text.UiText
import com.passvault.core.designsystem.text.uiText
import com.passvault.core.domain.model.SecurityError
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.model.VaultSessionState
import com.passvault.core.domain.repository.VaultRepository
import com.passvault.core.security.BiometricAvailability
import com.passvault.core.security.BiometricFailureReason
import com.passvault.core.security.BiometricOperationResult
import com.passvault.core.security.BiometricType
import com.passvault.core.security.BiometricUnlockService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

class UnlockViewModel(
    private val vaultRepository: VaultRepository,
    private val biometricUnlockService: BiometricUnlockService,
) : ViewModel() {

    private val _state = MutableStateFlow(UnlockState())
    val state: StateFlow<UnlockState> = _state.asStateFlow()

    private val _effect = Channel<UnlockEffect>(Channel.BUFFERED)
    val effect: Flow<UnlockEffect> = _effect.receiveAsFlow()
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
                            _effect.trySend(UnlockEffect.NavigateToVault)
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
                    is VaultSessionState.FatalError -> {
                        handleFatalError(sessionState.error)
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
            vaultRepository.exists().onSuccess { exists ->
                if (!exists && _state.value.failedAttempts == 0) {
                    _effect.trySend(UnlockEffect.NavigateToOnboarding)
                }
            }.onFailure {
                _state.update { it.copy(errorMessage = uiText(Res.string.error_unlock_inspect)) }
            }
        }
    }

    private fun handleFatalError(error: SecurityError) {
        val message = when (error) {
            is SecurityError.AuthenticationFailed -> {
                val remaining = 5 - error.attempts.coerceAtMost(5)
                uiText(Res.string.error_unlock_auth_remaining, remaining)
            }
            is SecurityError.CorruptedData -> uiText(Res.string.error_unlock_corrupted)
            is SecurityError.CryptoError -> uiText(Res.string.error_unlock_crypto)
            is SecurityError.SessionExpired -> uiText(Res.string.error_unlock_session_expired)
            // Fatal errors can contain implementation details or paths. Keep
            // those inside diagnostics and expose only a stable recovery hint.
            is SecurityError.Fatal -> uiText(Res.string.error_unlock_fatal)
        }
        _state.update {
            it.copy(
                isLoading = false,
                isBiometricLoading = false,
                errorMessage = message,
                failedAttempts = if (error is SecurityError.AuthenticationFailed) {
                    error.attempts
                } else it.failedAttempts
            )
        }
    }

    fun onEvent(event: UnlockEvent) {
        when (event) {
            is UnlockEvent.OnPasswordChanged -> {
                _state.update {
                    it.copy(
                        password = event.password.take(MAX_MASTER_PASSWORD_LENGTH),
                        errorMessage = null,
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
        if (_state.value.isLockedOut) {
            _state.update { it.copy(errorMessage = uiText(Res.string.error_unlock_cooldown)) }
            return
        }
        val password = _state.value.password
        if (password.isEmpty()) {
            _state.update { it.copy(errorMessage = uiText(Res.string.error_unlock_password_required)) }
            return
        }

        if (!unlockMutex.tryLock()) return
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val sensitivePassword = SensitiveText.from(password)
            try {
                vaultRepository.unlock(sensitivePassword)
                    .onSuccess {
                        lockoutResetJob?.cancel()
                        _state.update {
                            it.copy(
                                isLoading = false,
                                password = "",
                                failedAttempts = 0,
                            )
                        }
                    }
                    .onFailure {
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
            } catch (cancelled: CancellationException) {
                throw cancelled
            } finally {
                sensitivePassword.clear()
                unlockMutex.unlock()
            }
        }
    }

    private fun loadBiometricStatus() {
        biometricStatusJob?.cancel()
        biometricStatusJob = viewModelScope.launch {
            try {
                val status = biometricUnlockService.getStatus()
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
        when (currentState.biometricAvailability) {
            BiometricAvailability.NOT_ENROLLED -> {
                _state.update { it.copy(errorMessage = uiText(Res.string.error_biometric_not_enrolled)) }
                return
            }
            BiometricAvailability.UNAVAILABLE -> {
                _state.update { it.copy(errorMessage = uiText(Res.string.error_biometric_unavailable)) }
                return
            }
            BiometricAvailability.AVAILABLE -> Unit
        }
        if (!currentState.isBiometricEnabled) {
            _state.update { it.copy(errorMessage = uiText(Res.string.error_biometric_not_enabled)) }
            return
        }
        if (!unlockMutex.tryLock()) return

        _state.update {
            it.copy(isLoading = true, isBiometricLoading = true, errorMessage = null)
        }
        unlockJob = viewModelScope.launch {
            try {
                when (val result = biometricUnlockService.unlock()) {
                    BiometricOperationResult.Success -> Unit
                    BiometricOperationResult.Cancelled -> {
                        _state.update { it.copy(isLoading = false, isBiometricLoading = false) }
                    }
                    is BiometricOperationResult.Failure -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isBiometricLoading = false,
                                isBiometricEnabled = if (
                                    result.reason == BiometricFailureReason.INVALIDATED ||
                                    result.reason == BiometricFailureReason.NOT_ENABLED
                                ) false else it.isBiometricEnabled,
                                biometricAvailability = when (result.reason) {
                                    BiometricFailureReason.NOT_ENROLLED -> BiometricAvailability.NOT_ENROLLED
                                    BiometricFailureReason.NOT_AVAILABLE -> BiometricAvailability.UNAVAILABLE
                                    else -> it.biometricAvailability
                                },
                                errorMessage = result.reason.toUnlockMessage(),
                            )
                        }
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        isBiometricLoading = false,
                        errorMessage = uiText(Res.string.error_biometric_failed),
                    )
                }
            } finally {
                unlockMutex.unlock()
            }
        }
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
        val canUnlock: Boolean get() = password.isNotEmpty() && !isLoading && !isLockedOut
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
        const val MAX_MASTER_PASSWORD_LENGTH = 1_024
    }
}
