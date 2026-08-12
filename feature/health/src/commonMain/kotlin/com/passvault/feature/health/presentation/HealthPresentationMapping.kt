package com.passvault.feature.health.presentation

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.error_health_save
import com.passvault.core.designsystem.text.uiText
import com.passvault.core.domain.model.CredentialSummary
import com.passvault.core.domain.model.PasswordHealth
import com.passvault.core.domain.repository.CredentialHealthInput
import kotlin.time.Instant

internal fun CredentialHealthInput.toHealthSummary(health: PasswordHealth) = CredentialSummary.Decrypted(
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

internal fun CredentialHealthInput.clearSensitiveHealthValues() {
    username?.clear()
    email?.clear()
    password?.clear()
}

internal fun HealthViewModel.HealthState.withAnalysis(
    analysis: HealthViewModel.HealthAnalysis,
    summaries: List<CredentialSummary.Decrypted>,
    scannedAt: Instant,
    persistenceFailures: Int,
) = copy(
    isLoading = false,
    credentials = summaries,
    weakPasswords = analysis.weakPasswords,
    duplicatePasswords = analysis.duplicatePasswords,
    oldPasswords = analysis.oldPasswords,
    overallScore = analysis.overallScore,
    totalAnalyzed = analysis.totalAnalyzed,
    lastScanAt = scannedAt,
    errorMessage = if (persistenceFailures > 0) uiText(Res.string.error_health_save) else null,
)
