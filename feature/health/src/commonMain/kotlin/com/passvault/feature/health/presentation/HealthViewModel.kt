package com.passvault.feature.health.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.text.UiText
import com.passvault.core.designsystem.text.uiText
import com.passvault.core.domain.model.Credential
import com.passvault.core.domain.model.CredentialId
import com.passvault.core.domain.model.CredentialSummary
import com.passvault.core.domain.model.PasswordHealth
import com.passvault.core.domain.model.PasswordScore
import com.passvault.core.domain.model.PasswordStrengthEvaluator
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.repository.CredentialRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Performs a local-only password health scan.
 *
 * Password values never leave the process and are cleared as soon as the scan
 * finishes. This feature intentionally makes no breach-database claim: without
 * an explicit privacy-preserving breach service, it reports only strength,
 * duplicate use, and local password age.
 */
class HealthViewModel(
    private val credentialRepository: CredentialRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HealthState())
    val state: StateFlow<HealthState> = _state.asStateFlow()

    private val _effect = Channel<HealthEffect>(Channel.BUFFERED)
    val effect: Flow<HealthEffect> = _effect.receiveAsFlow()

    private var scanJob: Job? = null

    fun onEvent(event: HealthEvent) {
        when (event) {
            is HealthEvent.OnTabChanged -> _state.update { it.copy(selectedTab = event.tab) }
            is HealthEvent.OnCredentialClick ->
                _effect.trySend(HealthEffect.NavigateToCredential(event.credentialId))
            is HealthEvent.OnFixWeakPasswordClick ->
                _effect.trySend(HealthEffect.NavigateToEditCredential(event.credentialId))
            is HealthEvent.OnFixOldPasswordClick ->
                _effect.trySend(HealthEffect.NavigateToEditCredential(event.credentialId))
            is HealthEvent.OnFixDuplicateClick ->
                _state.update { it.copy(showingDuplicateGroup = event.group) }
            HealthEvent.OnDismissDuplicateGroup ->
                _state.update { it.copy(showingDuplicateGroup = null) }
            HealthEvent.OnRefreshScan -> loadHealthData()
            HealthEvent.OnReviewIssues -> selectFirstIssueTab()
            HealthEvent.OnCopySummary -> copyHealthSummary()
            is HealthEvent.OnCopySummaryResult -> {
                _state.update {
                    it.copy(
                        transientMessage = if (event.succeeded) {
                            uiText(Res.string.message_health_summary_copied)
                        } else {
                            uiText(Res.string.error_health_summary_copy)
                        },
                    )
                }
            }
            HealthEvent.OnBackClick -> _effect.trySend(HealthEffect.NavigateBack)
            HealthEvent.OnDismissMessage ->
                _state.update { it.copy(errorMessage = null, transientMessage = null) }
        }
    }

    private fun loadHealthData() {
        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    transientMessage = null,
                    showingDuplicateGroup = null,
                )
            }

            val result = credentialRepository.getCredentialsForHealthAnalysis()
            if (result.isFailure) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = uiText(Res.string.error_health_scan_locked),
                    )
                }
                return@launch
            }

            val credentials = result.getOrThrow()
            try {
                val analysis = analyzePasswordHealth(credentials)
                val summaries = credentials.map { credential ->
                    credential.toSummary(
                        analysis.healthByCredential[credential.id] ?: PasswordHealth.UNKNOWN,
                    )
                }
                var persistenceFailures = 0
                analysis.healthByCredential.forEach { (id, health) ->
                    val previous = credentials.firstOrNull { it.id == id }?.passwordHealth
                    if (previous != health && credentialRepository.updateHealth(id, health).isFailure) {
                        persistenceFailures += 1
                    }
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        credentials = summaries,
                        weakPasswords = analysis.weakPasswords,
                        duplicatePasswords = analysis.duplicatePasswords,
                        oldPasswords = analysis.oldPasswords,
                        overallScore = analysis.overallScore,
                        totalAnalyzed = analysis.totalAnalyzed,
                        lastScanAt = Clock.System.now(),
                        errorMessage = if (persistenceFailures > 0) {
                            uiText(Res.string.error_health_save)
                        } else {
                            null
                        },
                    )
                }
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = uiText(Res.string.error_health_scan),
                    )
                }
            } finally {
                credentials.forEach { it.clearSensitiveValues() }
            }
        }
    }

    internal fun analyzePasswordHealth(credentials: List<Credential>): HealthAnalysis {
        val passwordCredentials = credentials.filter { it.password?.isNotEmpty() == true }
        val passwordGroups = passwordCredentials.groupBy { requireNotNull(it.password) }
        val duplicateIds = passwordGroups
            .filterValues { it.size > 1 }
            .values
            .flatten()
            .mapTo(mutableSetOf()) { it.id }
        val now = Clock.System.now()
        val weakPasswords = mutableListOf<WeakPasswordItem>()
        val oldPasswords = mutableListOf<OldPasswordItem>()
        val healthByCredential = mutableMapOf<CredentialId, PasswordHealth>()

        credentials.forEach { credential ->
            val password = credential.password
            val hasPassword = password?.isNotEmpty() == true
            val score = if (hasPassword) calculatePasswordScore(requireNotNull(password)) else PasswordScore.UNKNOWN
            val passwordChangedAt = credential.passwordHistory
                .maxByOrNull { it.changedAt }
                ?.changedAt
                ?: credential.createdAt
            val ageDays = if (hasPassword) {
                (now - passwordChangedAt).inWholeDays.coerceAtLeast(0).toInt()
            } else {
                null
            }
            val isWeak = hasPassword && score in setOf(PasswordScore.VERY_WEAK, PasswordScore.WEAK)
            val isOld = ageDays != null && ageDays >= OLD_PASSWORD_DAYS
            val health = PasswordHealth(
                score = score,
                isDuplicate = credential.id in duplicateIds,
                isWeak = isWeak,
                isOld = isOld,
                ageDays = ageDays,
            )
            healthByCredential[credential.id] = health

            if (isWeak) {
                weakPasswords += WeakPasswordItem(
                    credentialId = credential.id,
                    title = credential.title,
                    username = credential.username?.mask() ?: credential.email?.mask(),
                    reason = determineWeakness(requireNotNull(password)),
                )
            }
            if (isOld) {
                oldPasswords += OldPasswordItem(
                    credentialId = credential.id,
                    title = credential.title,
                    username = credential.username?.mask() ?: credential.email?.mask(),
                    ageDays = requireNotNull(ageDays),
                )
            }
        }

        val duplicatePasswords = passwordGroups.values
            .filter { it.size > 1 }
            .map { group ->
                DuplicateGroup(
                    credentials = group.map { credential ->
                        DuplicateItem(
                            credentialId = credential.id,
                            title = credential.title,
                            username = credential.username?.mask() ?: credential.email?.mask(),
                        )
                    }.sortedBy { it.title.lowercase() },
                )
            }
            .sortedByDescending { it.credentials.size }
        val healthyCount = passwordCredentials.count { credential ->
            healthByCredential[credential.id]?.let { !it.isWeak && !it.isOld && !it.isDuplicate } == true
        }
        val overallScore = if (passwordCredentials.isEmpty()) {
            0
        } else {
            (healthyCount * 100) / passwordCredentials.size
        }

        return HealthAnalysis(
            weakPasswords = weakPasswords.sortedBy { it.title.lowercase() },
            duplicatePasswords = duplicatePasswords,
            oldPasswords = oldPasswords.sortedByDescending { it.ageDays },
            overallScore = overallScore,
            totalAnalyzed = passwordCredentials.size,
            healthByCredential = healthByCredential,
        )
    }

    private fun calculatePasswordScore(password: SensitiveText): PasswordScore {
        val chars = password.expose()
        return try {
            PasswordStrengthEvaluator.score(chars.concatToString())
        } finally {
            chars.fill('\u0000')
        }
    }

    private fun determineWeakness(password: SensitiveText): WeakPasswordReason {
        val chars = password.expose()
        return try {
            when {
                chars.size < RECOMMENDED_MIN_PASSWORD_LENGTH -> WeakPasswordReason.TOO_SHORT
                chars.none(Char::isLowerCase) -> WeakPasswordReason.NO_LOWERCASE
                chars.none(Char::isUpperCase) -> WeakPasswordReason.NO_UPPERCASE
                chars.none(Char::isDigit) -> WeakPasswordReason.NO_NUMBERS
                chars.none { !it.isLetterOrDigit() } -> WeakPasswordReason.NO_SYMBOLS
                else -> WeakPasswordReason.COMMON_OR_PREDICTABLE
            }
        } finally {
            chars.fill('\u0000')
        }
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
        _effect.trySend(HealthEffect.CopySummary(report))
    }

    private fun Credential.toSummary(health: PasswordHealth) = CredentialSummary.Decrypted(
        id = id,
        type = type,
        title = title,
        displayUsername = username?.mask() ?: email?.mask(),
        isFavorite = isFavorite,
        folderId = folderId,
        tagIds = tagIds,
        passwordHealth = health,
        lastUsedAt = lastUsedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    private fun Credential.clearSensitiveValues() {
        username?.clear()
        email?.clear()
        password?.clear()
        notes?.clear()
        recoveryCodes.forEach(SensitiveText::clear)
        apiKeys.forEach(SensitiveText::clear)
        licenseKeys.forEach(SensitiveText::clear)
        customFields.forEach { it.value.clear() }
        passwordHistory.forEach { it.password.clear() }
    }

    fun clearForLock() {
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

    private companion object {
        const val OLD_PASSWORD_DAYS = 365
        const val RECOMMENDED_MIN_PASSWORD_LENGTH = 12
    }
}
