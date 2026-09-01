package com.passvault.core.domain.model

/**
 * One policy for newly created master passwords across UI and storage boundaries.
 * Strength is a conservative local common-pattern check, not an entropy estimate.
 */
object MasterPasswordPolicy {
    const val MIN_LENGTH = 12
    const val MAX_LENGTH = 1_024
    private val MIN_STRENGTH = PasswordScore.FAIR

    /** Reports only the input bound; use [accepts] for a new-password decision. */
    fun acceptsLength(length: Int): Boolean = length in MIN_LENGTH..MAX_LENGTH

    fun accepts(value: String): Boolean =
        value.hasWellFormedUnicode() &&
            acceptsLength(value.codePointLength()) &&
            PasswordStrengthEvaluator.score(value) >= MIN_STRENGTH

    fun accepts(value: SensitiveText): Boolean =
        value.hasWellFormedUnicode() &&
            acceptsLength(value.length) &&
            value.withExposed { exposed ->
                PasswordStrengthEvaluator.score(exposed) >= MIN_STRENGTH
            }

    /** Unlock remains compatible with every non-empty password older versions could create. */
    fun acceptsExisting(value: SensitiveText): Boolean =
        value.hasWellFormedUnicode() && value.length in 1..MAX_LENGTH
}
