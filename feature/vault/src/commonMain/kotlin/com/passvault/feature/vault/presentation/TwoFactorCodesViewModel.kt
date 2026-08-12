package com.passvault.feature.vault.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.text.UiText
import com.passvault.core.designsystem.text.uiText
import com.passvault.core.domain.model.CredentialId
import com.passvault.core.domain.repository.CredentialTotpInput
import com.passvault.core.domain.repository.CredentialTotpRepository
import com.passvault.core.otp.TotpService
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Clock

class TwoFactorCodesViewModel(
    private val credentialRepository: CredentialTotpRepository,
    private val totpService: TotpService,
    private val clock: Clock = Clock.System,
) : ViewModel() {
    private val _state = MutableStateFlow(TwoFactorCodesState())
    val state: StateFlow<TwoFactorCodesState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<TwoFactorCodesEffect>(extraBufferCapacity = 1)
    val effect: SharedFlow<TwoFactorCodesEffect> = _effect.asSharedFlow()

    private var accounts = emptyList<CredentialTotpInput>()
    private var loadJob: Job? = null
    private var tickerJob: Job? = null
    private var screenRevision = 0L

    fun onEvent(event: TwoFactorCodesEvent) {
        when (event) {
            TwoFactorCodesEvent.OnScreenVisible,
            TwoFactorCodesEvent.OnRefresh,
            -> refresh()
            TwoFactorCodesEvent.OnScreenHidden -> clearForHiddenScreen()
            is TwoFactorCodesEvent.OnCredentialClick ->
                _effect.tryEmit(TwoFactorCodesEffect.NavigateToCredential(event.credentialId))
            is TwoFactorCodesEvent.OnCopyCodeClick -> copyCode(event.credentialId)
            is TwoFactorCodesEvent.OnCopyResult -> _state.update {
                it.copy(
                    statusMessage = if (event.succeeded) uiText(Res.string.action_copy_success) else null,
                    errorMessage = if (event.succeeded) null else uiText(Res.string.error_credential_copy),
                )
            }
            TwoFactorCodesEvent.OnDismissMessage -> _state.update {
                it.copy(errorMessage = null, statusMessage = null)
            }
            TwoFactorCodesEvent.OnBackClick -> _effect.tryEmit(TwoFactorCodesEffect.NavigateBack)
        }
    }

    private fun refresh() {
        val revision = ++screenRevision
        loadJob?.cancel()
        tickerJob?.cancel()
        clearAccounts()
        _state.value = TwoFactorCodesState(isLoading = true)
        loadJob = viewModelScope.launch {
            var loaded = emptyList<CredentialTotpInput>()
            try {
                loaded = credentialRepository.getCredentialsForTotpDisplay().getOrThrow()
                currentCoroutineContext().ensureActive()
                if (revision != screenRevision) return@launch
                accounts = loaded
                loaded = emptyList()
                updateCodes()
                _state.update { it.copy(isLoading = false) }
                startTicker(revision)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                currentCoroutineContext().ensureActive()
                if (revision == screenRevision) {
                    _state.value = TwoFactorCodesState(
                        isLoading = false,
                        loadFailed = true,
                        errorMessage = uiText(Res.string.error_two_factor_codes_load),
                    )
                }
            } finally {
                loaded.forEach(CredentialTotpInput::clear)
            }
        }
    }

    private fun startTicker(revision: Long) {
        tickerJob?.cancel()
        if (accounts.isEmpty()) return
        tickerJob = viewModelScope.launch {
            while (isActive && revision == screenRevision) {
                val nowMillis = clock.now().toEpochMilliseconds()
                val delayMillis = (MILLIS_PER_SECOND - nowMillis.mod(MILLIS_PER_SECOND))
                    .coerceAtLeast(MIN_TICK_DELAY_MILLIS)
                delay(delayMillis)
                updateCodes()
            }
        }
    }

    private fun updateCodes() {
        val now = clock.now()
        val items = accounts.map { account ->
            val code = runCatching { totpService.generate(account.configuration, now).getOrNull() }.getOrNull()
            val remainingMillis = code?.let {
                (it.expiresAt.toEpochMilliseconds() - now.toEpochMilliseconds()).coerceAtLeast(0)
            } ?: 0L
            TwoFactorCodeItem(
                credentialId = account.id,
                title = account.title,
                displayUsername = account.displayUsername,
                issuer = account.configuration.issuer,
                accountName = account.configuration.accountName,
                code = code?.value.orEmpty(),
                secondsRemaining = if (code == null) {
                    0
                } else {
                    ((remainingMillis + MILLIS_PER_SECOND - 1) / MILLIS_PER_SECOND).toInt()
                },
                progress = if (code == null) {
                    0f
                } else {
                    (remainingMillis.toFloat() /
                        (account.configuration.periodSeconds * MILLIS_PER_SECOND)).coerceIn(0f, 1f)
                },
                generationFailed = code == null,
            )
        }
        _state.update { it.copy(items = items) }
    }

    private fun copyCode(credentialId: CredentialId) {
        _state.value.items
            .firstOrNull { it.credentialId == credentialId }
            ?.code
            ?.takeIf(String::isNotEmpty)
            ?.let { _effect.tryEmit(TwoFactorCodesEffect.CopyCode(it)) }
    }

    private fun clearForHiddenScreen() {
        screenRevision++
        loadJob?.cancel()
        tickerJob?.cancel()
        loadJob = null
        tickerJob = null
        clearAccounts()
        _state.value = TwoFactorCodesState()
    }

    fun clearForLock() = clearForHiddenScreen()

    override fun onCleared() {
        clearForHiddenScreen()
        super.onCleared()
    }

    private fun clearAccounts() {
        accounts.forEach(CredentialTotpInput::clear)
        accounts = emptyList()
    }

    data class TwoFactorCodesState(
        val items: List<TwoFactorCodeItem> = emptyList(),
        val isLoading: Boolean = false,
        val loadFailed: Boolean = false,
        val errorMessage: UiText? = null,
        val statusMessage: UiText? = null,
    )

    data class TwoFactorCodeItem(
        val credentialId: CredentialId,
        val title: String,
        val displayUsername: String?,
        val issuer: String?,
        val accountName: String?,
        val code: String,
        val secondsRemaining: Int,
        val progress: Float,
        val generationFailed: Boolean,
    )

    sealed interface TwoFactorCodesEvent {
        data object OnScreenVisible : TwoFactorCodesEvent
        data object OnScreenHidden : TwoFactorCodesEvent
        data object OnRefresh : TwoFactorCodesEvent
        data class OnCredentialClick(val credentialId: CredentialId) : TwoFactorCodesEvent
        data class OnCopyCodeClick(val credentialId: CredentialId) : TwoFactorCodesEvent
        data class OnCopyResult(val succeeded: Boolean) : TwoFactorCodesEvent
        data object OnDismissMessage : TwoFactorCodesEvent
        data object OnBackClick : TwoFactorCodesEvent
    }

    sealed interface TwoFactorCodesEffect {
        data object NavigateBack : TwoFactorCodesEffect
        data class NavigateToCredential(val credentialId: CredentialId) : TwoFactorCodesEffect
        data class CopyCode(val code: String) : TwoFactorCodesEffect
    }

    private companion object {
        const val MILLIS_PER_SECOND = 1_000L
        const val MIN_TICK_DELAY_MILLIS = 50L
    }
}
