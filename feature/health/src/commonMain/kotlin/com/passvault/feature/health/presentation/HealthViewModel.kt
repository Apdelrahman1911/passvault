package com.passvault.feature.health.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.text.UiText
import com.passvault.core.designsystem.text.uiText
import com.passvault.core.domain.model.CredentialId
import com.passvault.core.domain.model.CredentialSummary
import com.passvault.core.domain.model.PasswordHealth
import com.passvault.core.domain.repository.CredentialRepository
import com.passvault.core.domain.repository.CredentialHealthInput
import com.passvault.core.security.EntrySensitiveStateOwner
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Performs a local-only password health scan.
 *
 * Password values never leave the process. Mutable buffers owned by the scan
 * are cleared as soon as analysis finishes; this does not claim that a managed
 * runtime can erase copies made below this boundary. Without an explicit
 * privacy-preserving breach service, this feature reports only strength,
 * duplicate use, and local password age.
 */
class HealthViewModel(
    private val credentialRepository: CredentialRepository,
    private val clock: Clock = Clock.System,
) : ViewModel(), EntrySensitiveStateOwner {

    private val _state = MutableStateFlow(HealthState())
    val state: StateFlow<HealthState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<HealthEffect>(extraBufferCapacity = 1)
    val effect: Flow<HealthEffect> = _effect.asSharedFlow()

    private var scanJob: Job? = null

    override fun onCleared() {
        clearForLock()
        super.onCleared()
    }

    fun onEvent(event: HealthEvent) {
        when (event) {
            is HealthEvent.OnTabChanged -> _state.update { it.copy(selectedTab = event.tab) }
            is HealthEvent.OnCredentialClick ->
                _effect.tryEmit(HealthEffect.NavigateToCredential(event.credentialId))
            is HealthEvent.OnFixWeakPasswordClick ->
                _effect.tryEmit(HealthEffect.NavigateToEditCredential(event.credentialId))
            is HealthEvent.OnFixOldPasswordClick ->
                _effect.tryEmit(HealthEffect.NavigateToEditCredential(event.credentialId))
            is HealthEvent.OnFixDuplicateClick ->
                _state.update { it.copy(showingDuplicateGroup = event.group) }
            HealthEvent.OnDismissDuplicateGroup ->
                _state.update { it.copy(showingDuplicateGroup = null) }
            HealthEvent.OnRefreshScan -> loadHealthData()
            HealthEvent.OnReviewIssues -> selectFirstIssueTab()
            HealthEvent.OnCopySummary -> copyHealthSummary()
            is HealthEvent.OnCopySummaryResult -> updateCopyResult(event.succeeded)
            HealthEvent.OnBackClick -> {
                if (_state.value.showingDuplicateGroup != null) {
                    _state.update { it.copy(showingDuplicateGroup = null) }
                } else {
                    _effect.tryEmit(HealthEffect.NavigateBack)
                }
            }
            HealthEvent.OnDismissMessage ->
                _state.update { it.copy(transientMessage = null) }
        }
    }

    private fun updateCopyResult(succeeded: Boolean) {
        _state.update {
            it.copy(
                transientMessage = if (succeeded) {
                    uiText(Res.string.message_health_summary_copied)
                } else {
                    uiText(Res.string.error_health_summary_copy)
                },
            )
        }
    }

    private fun loadHealthData() {
        scanJob?.cancel()
        _state.update {
            it.copy(
                isLoading = true,
                errorMessage = null,
                transientMessage = null,
                showingDuplicateGroup = null,
            )
        }
        scanJob = viewModelScope.launch {
            var credentials = emptyList<CredentialHealthInput>()
            try {
                val result = credentialRepository.getCredentialsForHealthAnalysis()
                if (result.isFailure) {
                    showScanError(Res.string.error_health_scan_locked)
                    return@launch
                }

                credentials = result.getOrThrow()
                currentCoroutineContext().ensureActive()
                val analysis = analyzePasswordHealth(credentials)
                val summaries = credentials.map { credential ->
                    credential.toHealthSummary(
                        analysis.healthByCredential[credential.id] ?: PasswordHealth.UNKNOWN,
                    )
                }
                val previousHealth = credentials.associate { it.id to it.passwordHealth }
                credentials.forEach(CredentialHealthInput::clearSensitiveHealthValues)
                credentials = emptyList()
                val persistenceFailures = persistChangedHealth(previousHealth, analysis.healthByCredential)
                currentCoroutineContext().ensureActive()
                _state.value = _state.value.withAnalysis(
                    analysis = analysis,
                    summaries = summaries,
                    scannedAt = clock.now(),
                    persistenceFailures = persistenceFailures,
                )
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Exception) {
                currentCoroutineContext().ensureActive()
                showScanError(Res.string.error_health_scan)
            } finally {
                credentials.forEach(CredentialHealthInput::clearSensitiveHealthValues)
            }
        }
    }

    internal fun analyzePasswordHealth(credentials: List<CredentialHealthInput>): HealthAnalysis =
        buildHealthAnalysis(credentials, clock.now())

    private suspend fun persistChangedHealth(
        previousHealth: Map<CredentialId, PasswordHealth>,
        healthByCredential: Map<CredentialId, PasswordHealth>,
    ): Int {
        return healthByCredential.count { (id, health) ->
            previousHealth[id] != health && credentialRepository.updateHealth(id, health).isFailure
        }
    }

    private fun showScanError(message: StringResource) {
        _state.update { it.copy(isLoading = false, errorMessage = uiText(message)) }
    }

    private fun selectFirstIssueTab() {
        val nextTab = when {
            _state.value.weakPasswords.isNotEmpty() -> HealthTab.WEAK_PASSWORDS
            _state.value.duplicatePasswords.isNotEmpty() -> HealthTab.DUPLICATES
            _state.value.oldPasswords.isNotEmpty() -> HealthTab.OLD_PASSWORDS
            else -> HealthTab.OVERVIEW
        }
        _state.update { it.copy(selectedTab = nextTab) }
    }

    private fun copyHealthSummary() {
        val current = _state.value
        if (current.isLoading || current.lastScanAt == null) return
        val report = uiText(
            Res.string.health_summary_report,
            current.lastScanAt.toString(),
            current.totalAnalyzed,
            current.overallScore,
            current.weakPasswords.size,
            current.duplicatePasswords.size,
            OLD_PASSWORD_DAYS,
            current.oldPasswords.size,
        )
        _effect.tryEmit(HealthEffect.CopySummary(report))
    }

    override fun clearForLock() {
        scanJob?.cancel()
        scanJob = null
        _state.value = HealthState()
    }

    data class HealthState(
        val selectedTab: HealthTab = HealthTab.OVERVIEW,
        val isLoading: Boolean = false,
        val credentials: List<CredentialSummary.Decrypted> = emptyList(),
        val overallScore: Int = 0,
        val totalAnalyzed: Int = 0,
        val lastScanAt: Instant? = null,
        val weakPasswords: List<WeakPasswordItem> = emptyList(),
        val duplicatePasswords: List<DuplicateGroup> = emptyList(),
        val oldPasswords: List<OldPasswordItem> = emptyList(),
        val showingDuplicateGroup: DuplicateGroup? = null,
        val errorMessage: UiText? = null,
        val transientMessage: UiText? = null,
    ) {
        val criticalIssues: Int get() = weakPasswords.size
        val warningIssues: Int get() = duplicatePasswords.size + oldPasswords.size
        val hasIssues: Boolean get() = criticalIssues > 0 || warningIssues > 0
        val scoreRating: ScoreRating get() = when {
            totalAnalyzed == 0 -> ScoreRating.NOT_SCANNED
            overallScore >= 80 -> ScoreRating.EXCELLENT
            overallScore >= 60 -> ScoreRating.GOOD
            overallScore >= 40 -> ScoreRating.FAIR
            else -> ScoreRating.POOR
        }
    }

    sealed interface HealthEvent {
        data class OnTabChanged(val tab: HealthTab) : HealthEvent
        data class OnCredentialClick(val credentialId: CredentialId) : HealthEvent
        data class OnFixWeakPasswordClick(val credentialId: CredentialId) : HealthEvent
        data class OnFixDuplicateClick(val group: DuplicateGroup) : HealthEvent
        data class OnFixOldPasswordClick(val credentialId: CredentialId) : HealthEvent
        data class OnCopySummaryResult(val succeeded: Boolean) : HealthEvent
        data object OnDismissDuplicateGroup : HealthEvent
        data object OnRefreshScan : HealthEvent
        data object OnReviewIssues : HealthEvent
        data object OnCopySummary : HealthEvent
        data object OnBackClick : HealthEvent
        data object OnDismissMessage : HealthEvent
    }

    sealed interface HealthEffect {
        data object NavigateBack : HealthEffect
        data class NavigateToCredential(val credentialId: CredentialId) : HealthEffect
        data class NavigateToEditCredential(val credentialId: CredentialId) : HealthEffect
        data class CopySummary(val report: UiText) : HealthEffect
    }

    data class HealthAnalysis(
        val weakPasswords: List<WeakPasswordItem>,
        val duplicatePasswords: List<DuplicateGroup>,
        val oldPasswords: List<OldPasswordItem>,
        val overallScore: Int,
        val totalAnalyzed: Int,
        val healthByCredential: Map<CredentialId, PasswordHealth>,
    )

    data class WeakPasswordItem(
        val credentialId: CredentialId,
        val title: String,
        val username: String?,
        val reason: WeakPasswordReason,
    )

    data class DuplicateGroup(
        val credentials: List<DuplicateItem>,
    ) {
        val count: Int get() = credentials.size
    }

    data class DuplicateItem(
        val credentialId: CredentialId,
        val title: String,
        val username: String?,
    )

    data class OldPasswordItem(
        val credentialId: CredentialId,
        val title: String,
        val username: String?,
        val ageDays: Int,
    )

    enum class HealthTab(val displayName: UiText) {
        OVERVIEW(uiText(Res.string.health_tab_overview)),
        WEAK_PASSWORDS(uiText(Res.string.health_tab_weak)),
        DUPLICATES(uiText(Res.string.health_tab_duplicates)),
        OLD_PASSWORDS(uiText(Res.string.health_tab_old)),
    }

    enum class WeakPasswordReason(val displayName: UiText) {
        TOO_SHORT(uiText(Res.string.health_reason_too_short)),
        NO_UPPERCASE(uiText(Res.string.health_reason_no_uppercase)),
        NO_LOWERCASE(uiText(Res.string.health_reason_no_lowercase)),
        NO_NUMBERS(uiText(Res.string.health_reason_no_numbers)),
        NO_SYMBOLS(uiText(Res.string.health_reason_no_symbols)),
        COMMON_OR_PREDICTABLE(uiText(Res.string.health_reason_predictable)),
    }

    enum class ScoreRating(val displayName: UiText, val description: UiText) {
        NOT_SCANNED(
            uiText(Res.string.health_rating_not_scanned),
            uiText(Res.string.health_rating_not_scanned_description),
        ),
        EXCELLENT(
            uiText(Res.string.health_rating_excellent),
            uiText(Res.string.health_rating_excellent_description),
        ),
        GOOD(
            uiText(Res.string.health_rating_good),
            uiText(Res.string.health_rating_good_description),
        ),
        FAIR(
            uiText(Res.string.health_rating_fair),
            uiText(Res.string.health_rating_fair_description),
        ),
        POOR(
            uiText(Res.string.health_rating_poor),
            uiText(Res.string.health_rating_poor_description),
        ),
    }

}
