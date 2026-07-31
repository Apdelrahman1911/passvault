package com.passvault.core.domain.model

/**
 * One deterministic password-strength policy shared by onboarding, editors,
 * backup passwords, settings, and health analysis.
 *
 * This is a local heuristic, not a password cracker or breach lookup. It
 * deliberately penalizes common words, keyboard sequences, leetspeak variants,
 * and repeated characters so visual feedback never treats "P@ssw0rd!" as a
 * strong password.
 */
object PasswordStrengthEvaluator {
    fun score(password: CharSequence): PasswordScore {
        if (password.isEmpty() || password.length < 8) return PasswordScore.VERY_WEAK

        val value = password.toString()
        val normalized = normalizeLeetspeak(value.lowercase())
        val characterClasses = listOf(
            value.any(Char::isLowerCase),
            value.any(Char::isUpperCase),
            value.any(Char::isDigit),
            value.any { !it.isLetterOrDigit() },
        ).count { it }

        var points = when {
            value.length >= 20 -> 4
            value.length >= 16 -> 3
            value.length >= 12 -> 2
            else -> 1
        }
        points += (characterClasses - 1).coerceAtLeast(0)

        val distinctRatio = value.toSet().size.toDouble() / value.length
        if (distinctRatio >= 0.65) points += 1

        if (COMMON_TOKENS.any(normalized::contains)) points -= 3
        if (hasSequence(normalized)) points -= 2
        if (hasExcessiveRepetition(normalized)) points -= 3
        if (characterClasses == 1) points -= 1

        return when {
            points <= 0 -> PasswordScore.VERY_WEAK
            points <= 2 -> PasswordScore.WEAK
            points == 3 -> PasswordScore.FAIR
            points == 4 -> PasswordScore.GOOD
            points <= 6 -> PasswordScore.STRONG
            else -> PasswordScore.VERY_STRONG
        }
    }

    private fun normalizeLeetspeak(value: String): String = buildString(value.length) {
        value.forEach { character ->
            append(
                when (character) {
                    '0' -> 'o'
                    '1', '!' -> 'i'
                    '3' -> 'e'
                    '4', '@' -> 'a'
                    '5', '$' -> 's'
                    '7' -> 't'
                    else -> character
                },
            )
        }
    }

    private fun hasSequence(value: String): Boolean {
        if (value.length < SEQUENCE_LENGTH) return false
        return SEQUENCES.any { sequence ->
            sequence.windowed(SEQUENCE_LENGTH).any(value::contains) ||
                sequence.reversed().windowed(SEQUENCE_LENGTH).any(value::contains)
        }
    }

    private fun hasExcessiveRepetition(value: String): Boolean {
        if (value.isEmpty()) return false
        if (value.toSet().size <= (value.length / 3).coerceAtLeast(1)) return true
        return value.windowed(3).any { chunk -> chunk.toSet().size == 1 }
    }

    private const val SEQUENCE_LENGTH = 4
    private val COMMON_TOKENS = listOf(
        "password",
        "passvault",
        "letmein",
        "welcome",
        "admin",
        "login",
        "secret",
        "monkey",
        "dragon",
    )
    private val SEQUENCES = listOf(
        "abcdefghijklmnopqrstuvwxyz",
        "0123456789",
        "qwertyuiop",
        "asdfghjkl",
        "zxcvbnm",
    )
}
