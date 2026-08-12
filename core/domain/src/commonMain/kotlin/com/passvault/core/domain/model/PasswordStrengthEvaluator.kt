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
        val value = password.toString()
        val codePoints = value.toCodePointStrings()
        if (codePoints.size < MIN_PASSWORD_CODE_POINTS) return PasswordScore.VERY_WEAK

        val normalized = normalizeLeetspeak(value.lowercase())
        val characterClasses = characterClassCount(value)
        val points = lengthPoints(codePoints.size) +
            (characterClasses - 1).coerceAtLeast(0) +
            diversityBonus(codePoints) -
            weaknessPenalty(normalized, characterClasses)
        return points.toPasswordScore()
    }

    private fun characterClassCount(value: String): Int = listOf(
        value.any(Char::isLowerCase),
        value.any(Char::isUpperCase),
        value.any(Char::isDigit),
        value.any { !it.isLetterOrDigit() },
    ).count { it }

    private fun lengthPoints(codePointCount: Int): Int = when {
        codePointCount >= 20 -> 4
        codePointCount >= 16 -> 3
        codePointCount >= 12 -> 2
        else -> 1
    }

    private fun diversityBonus(codePoints: List<String>): Int =
        if (codePoints.toSet().size.toDouble() / codePoints.size >= 0.65) 1 else 0

    private fun weaknessPenalty(normalized: String, characterClasses: Int): Int =
        (if (COMMON_TOKENS.any(normalized::contains)) 3 else 0) +
            (if (hasSequence(normalized)) 2 else 0) +
            (if (hasExcessiveRepetition(normalized)) 3 else 0) +
            (if (characterClasses == 1) 1 else 0)

    private fun Int.toPasswordScore(): PasswordScore = when {
        this <= 0 -> PasswordScore.VERY_WEAK
        this <= 2 -> PasswordScore.WEAK
        this == 3 -> PasswordScore.FAIR
        this == 4 -> PasswordScore.GOOD
        this <= 6 -> PasswordScore.STRONG
        else -> PasswordScore.VERY_STRONG
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
        val codePoints = value.toCodePointStrings()
        return codePoints.isNotEmpty() &&
            (
                codePoints.toSet().size <= (codePoints.size / 3).coerceAtLeast(1) ||
                    codePoints.windowed(3).any { chunk -> chunk.toSet().size == 1 }
            )
    }

    private const val MIN_PASSWORD_CODE_POINTS = 8
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
