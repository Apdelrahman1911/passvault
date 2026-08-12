package com.passvault.feature.health.presentation

import com.passvault.core.domain.model.PasswordHealth
import com.passvault.core.domain.model.PasswordScore
import com.passvault.core.domain.model.PasswordStrengthEvaluator
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.repository.CredentialHealthInput
import kotlin.time.Instant

internal const val OLD_PASSWORD_DAYS = 365
private const val RECOMMENDED_MIN_PASSWORD_LENGTH = 12

private data class CredentialHealthEvaluation(
    val health: PasswordHealth,
    val weakItem: HealthViewModel.WeakPasswordItem?,
    val oldItem: HealthViewModel.OldPasswordItem?,
)

internal fun buildHealthAnalysis(
    credentials: List<CredentialHealthInput>,
    now: Instant,
): HealthViewModel.HealthAnalysis {
    val passwordCredentials = credentials.filter { it.password?.isNotEmpty() == true }
    val passwordGroups = groupCredentialsByPassword(passwordCredentials)
    val duplicateIds = passwordGroups
        .filter { it.size > 1 }
        .flatten()
        .mapTo(mutableSetOf(), CredentialHealthInput::id)
    val evaluations = credentials.associate { credential ->
        credential.id to evaluateHealth(credential, credential.id in duplicateIds, now)
    }
    val healthByCredential = evaluations.mapValues { it.value.health }
    val healthyCount = passwordCredentials.count { credential ->
        healthByCredential[credential.id]?.isHealthy == true
    }
    return HealthViewModel.HealthAnalysis(
        weakPasswords = evaluations.values.mapNotNull { it.weakItem }.sortedByTitle(),
        duplicatePasswords = duplicateGroups(passwordGroups),
        oldPasswords = evaluations.values.mapNotNull { it.oldItem }.sortedByDescending { it.ageDays },
        overallScore = healthyPercentage(healthyCount, passwordCredentials.size),
        totalAnalyzed = passwordCredentials.size,
        healthByCredential = healthByCredential,
    )
}

/**
 * SensitiveText deliberately has one redacted hash code for every value. A
 * hash-based groupBy would therefore compare every password with almost every
 * other password. Sort temporary, wipeable character copies instead, then
 * return groups that retain only the original credential references.
 */
private fun groupCredentialsByPassword(
    credentials: List<CredentialHealthInput>,
): List<List<CredentialHealthInput>> {
    val exposed = credentials.map { credential ->
        ExposedCredentialPassword(
            credential = credential,
            characters = requireNotNull(credential.password).expose(),
        )
    }
    return try {
        val sorted = exposed.sortedWith { left, right ->
            comparePasswordCharacters(left.characters, right.characters)
        }
        val groups = mutableListOf<MutableList<CredentialHealthInput>>()
        var previousCharacters: CharArray? = null
        sorted.forEach { entry ->
            if (previousCharacters?.contentEquals(entry.characters) != true) {
                groups += mutableListOf(entry.credential)
            } else {
                groups.last() += entry.credential
            }
            previousCharacters = entry.characters
        }
        groups
    } finally {
        exposed.forEach { it.characters.fill('\u0000') }
    }
}

private fun comparePasswordCharacters(left: CharArray, right: CharArray): Int {
    val commonSize = minOf(left.size, right.size)
    var comparison = 0
    var index = 0
    while (index < commonSize && comparison == 0) {
        comparison = left[index].compareTo(right[index])
        index++
    }
    if (comparison == 0) comparison = left.size.compareTo(right.size)
    return comparison
}

private class ExposedCredentialPassword(
    val credential: CredentialHealthInput,
    val characters: CharArray,
)

private val PasswordHealth.isHealthy: Boolean
    get() = !isWeak && !isOld && !isDuplicate

private fun evaluateHealth(
    credential: CredentialHealthInput,
    isDuplicate: Boolean,
    now: Instant,
): CredentialHealthEvaluation {
    val password = credential.password
    val score = password?.takeIf(SensitiveText::isNotEmpty)?.let(::calculatePasswordScore)
        ?: PasswordScore.UNKNOWN
    val ageDays = password?.takeIf(SensitiveText::isNotEmpty)?.let {
        passwordAgeDays(now, credential.passwordChangedAt ?: credential.createdAt)
    }
    val isWeak = score == PasswordScore.VERY_WEAK || score == PasswordScore.WEAK
    val isOld = ageDays?.let { it >= OLD_PASSWORD_DAYS } == true
    val maskedUsername = credential.username?.mask() ?: credential.email?.mask()
    return CredentialHealthEvaluation(
        health = PasswordHealth(score, isDuplicate, isWeak, isOld, ageDays),
        weakItem = if (isWeak) credential.weakItem(maskedUsername, requireNotNull(password)) else null,
        oldItem = if (isOld) credential.oldItem(maskedUsername, requireNotNull(ageDays)) else null,
    )
}

private fun passwordAgeDays(now: Instant, changedAt: Instant): Int =
    (now - changedAt).inWholeDays
        .coerceIn(0L, Int.MAX_VALUE.toLong())
        .toInt()

private fun calculatePasswordScore(password: SensitiveText): PasswordScore {
    val chars = password.expose()
    return try {
        PasswordStrengthEvaluator.score(chars.concatToString())
    } finally {
        chars.fill('\u0000')
    }
}

private fun CredentialHealthInput.weakItem(
    maskedUsername: String?,
    password: SensitiveText,
) = HealthViewModel.WeakPasswordItem(
    credentialId = id,
    title = title,
    username = maskedUsername,
    reason = determineWeakness(password),
)

private fun CredentialHealthInput.oldItem(
    maskedUsername: String?,
    ageDays: Int,
) = HealthViewModel.OldPasswordItem(
    credentialId = id,
    title = title,
    username = maskedUsername,
    ageDays = ageDays,
)

private fun determineWeakness(password: SensitiveText): HealthViewModel.WeakPasswordReason {
    val chars = password.expose()
    return try {
        when {
            password.length < RECOMMENDED_MIN_PASSWORD_LENGTH -> HealthViewModel.WeakPasswordReason.TOO_SHORT
            chars.none(Char::isLowerCase) -> HealthViewModel.WeakPasswordReason.NO_LOWERCASE
            chars.none(Char::isUpperCase) -> HealthViewModel.WeakPasswordReason.NO_UPPERCASE
            chars.none(Char::isDigit) -> HealthViewModel.WeakPasswordReason.NO_NUMBERS
            chars.none { !it.isLetterOrDigit() } -> HealthViewModel.WeakPasswordReason.NO_SYMBOLS
            else -> HealthViewModel.WeakPasswordReason.COMMON_OR_PREDICTABLE
        }
    } finally {
        chars.fill('\u0000')
    }
}

private fun duplicateGroups(
    groups: Collection<List<CredentialHealthInput>>,
): List<HealthViewModel.DuplicateGroup> = groups
    .filter { it.size > 1 }
    .map { group ->
        HealthViewModel.DuplicateGroup(
            credentials = group.map { credential ->
                HealthViewModel.DuplicateItem(
                    credentialId = credential.id,
                    title = credential.title,
                    username = credential.username?.mask() ?: credential.email?.mask(),
                )
            }.sortedBy { it.title.lowercase() },
        )
    }
    .sortedByDescending { it.credentials.size }

private fun List<HealthViewModel.WeakPasswordItem>.sortedByTitle() =
    sortedBy { it.title.lowercase() }

private fun healthyPercentage(healthyCount: Int, totalCount: Int): Int =
    if (totalCount == 0) 0 else (healthyCount * 100) / totalCount
