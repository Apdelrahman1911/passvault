package com.passvault.core.domain.model

/**
 * One deterministic password-strength policy shared by onboarding, editors,
 * backup passwords, settings, and health analysis.
 *
 * This is a conservative local heuristic, not an entropy measurement, password
 * cracker, or breach lookup. Length determines the baseline score. Character
 * variety can improve it by at most one band, while common words, dates,
 * keyboard sequences, leetspeak variants, and repeated patterns lower it.
 */
object PasswordStrengthEvaluator {
    fun score(password: CharSequence): PasswordScore {
        val value = password.toString()
        val codePoints = value.toCodePointStrings()
        if (codePoints.size < MIN_PASSWORD_CODE_POINTS) return PasswordScore.VERY_WEAK

        val normalized = normalizeLeetspeak(value.lowercase())
        val characterClasses = characterClassCount(value)
        return if (hasExcessiveRepetition(normalized)) {
            PasswordScore.VERY_WEAK
        } else {
            val varietyBonus = if (characterClasses >= MIN_CLASSES_FOR_VARIETY_BONUS && hasHighDiversity(codePoints)) {
                1
            } else {
                0
            }
            val penaltyBands =
                (if (COMMON_WEAK_TERMS.any(normalized::contains)) COMMON_TERM_PENALTY_BANDS else 0) +
                    (if (hasSequence(normalized)) SEQUENCE_PENALTY_BANDS else 0) +
                    (if (hasLikelyWordAndYear(value)) DATE_PATTERN_PENALTY_BANDS else 0)
            baselineScore(codePoints.size).shiftBy(varietyBonus - penaltyBands)
        }
    }

    private fun characterClassCount(value: String): Int = listOf(
        value.any(Char::isLowerCase),
        value.any(Char::isUpperCase),
        value.any(Char::isDigit),
        value.any { !it.isLetterOrDigit() },
    ).count { it }

    private fun baselineScore(codePointCount: Int): PasswordScore = when {
        codePointCount >= VERY_STRONG_LENGTH -> PasswordScore.VERY_STRONG
        codePointCount >= STRONG_LENGTH -> PasswordScore.STRONG
        codePointCount >= GOOD_LENGTH -> PasswordScore.GOOD
        codePointCount >= FAIR_LENGTH -> PasswordScore.FAIR
        else -> PasswordScore.WEAK
    }

    private fun hasHighDiversity(codePoints: List<String>): Boolean =
        codePoints.toSet().size.toDouble() / codePoints.size >= MIN_DIVERSITY_RATIO

    private fun PasswordScore.shiftBy(bands: Int): PasswordScore {
        val currentIndex = STRENGTH_SCALE.indexOf(this)
        check(currentIndex >= 0) { "Cannot adjust an unknown password score" }
        return STRENGTH_SCALE[(currentIndex + bands).coerceIn(STRENGTH_SCALE.indices)]
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
        if (codePoints.windowed(REPEATED_RUN_LENGTH).any { chunk -> chunk.distinct().size == 1 }) return true

        val maximumPatternLength = minOf(MAX_REPEATED_PATTERN_LENGTH, codePoints.size / 2)
        return (1..maximumPatternLength).any { patternLength ->
            (0..codePoints.size - patternLength * 2).any { startIndex ->
                var matchedLength = patternLength
                while (
                    startIndex + matchedLength < codePoints.size &&
                    codePoints[startIndex + matchedLength] == codePoints[startIndex + matchedLength % patternLength]
                ) {
                    matchedLength++
                }
                matchedLength >= maxOf(MIN_REPEATED_PATTERN_SPAN, patternLength * 2)
            }
        }
    }

    private fun hasLikelyWordAndYear(value: String): Boolean =
        hasLetterRun(value, MIN_WORD_RUN_LENGTH) && hasLikelyYear(value)

    private fun hasLetterRun(value: String, minimumLength: Int): Boolean {
        var runLength = 0
        value.forEach { character ->
            runLength = if (character.isLetter()) runLength + 1 else 0
            if (runLength >= minimumLength) return true
        }
        return false
    }

    private fun hasLikelyYear(value: String): Boolean {
        var found = false
        if (value.length >= YEAR_LENGTH) {
            for (startIndex in 0..value.length - YEAR_LENGTH) {
                var year = 0
                var isAsciiNumber = true
                for (offset in 0 until YEAR_LENGTH) {
                    val character = value[startIndex + offset]
                    if (character !in '0'..'9') {
                        isAsciiNumber = false
                        break
                    }
                    year = year * DECIMAL_RADIX + (character - '0')
                }
                if (isAsciiNumber && year in MIN_LIKELY_YEAR..MAX_LIKELY_YEAR) {
                    found = true
                    break
                }
            }
        }
        return found
    }

    private const val MIN_PASSWORD_CODE_POINTS = 8
    private const val FAIR_LENGTH = 12
    private const val GOOD_LENGTH = 16
    private const val STRONG_LENGTH = 20
    private const val VERY_STRONG_LENGTH = 24
    private const val MIN_CLASSES_FOR_VARIETY_BONUS = 3
    private const val MIN_DIVERSITY_RATIO = 0.65
    private const val SEQUENCE_LENGTH = 4
    private const val REPEATED_RUN_LENGTH = 3
    private const val MAX_REPEATED_PATTERN_LENGTH = 8
    private const val MIN_REPEATED_PATTERN_SPAN = 8
    private const val MIN_WORD_RUN_LENGTH = 4
    private const val YEAR_LENGTH = 4
    private const val MIN_LIKELY_YEAR = 1900
    private const val MAX_LIKELY_YEAR = 2099
    private const val DECIMAL_RADIX = 10
    private const val COMMON_TERM_PENALTY_BANDS = 2
    private const val SEQUENCE_PENALTY_BANDS = 1
    private const val DATE_PATTERN_PENALTY_BANDS = 2
    private val STRENGTH_SCALE = listOf(
        PasswordScore.VERY_WEAK,
        PasswordScore.WEAK,
        PasswordScore.FAIR,
        PasswordScore.GOOD,
        PasswordScore.STRONG,
        PasswordScore.VERY_STRONG,
    )
    private val COMMON_WEAK_TERMS = listOf(
        "password",
        "passvault",
        "letmein",
        "welcome",
        "admin",
        "login",
        "secret",
        "monkey",
        "dragon",
        "qwerty",
        "iloveyou",
        "troubador",
        "trustno",
        "sunshine",
        "princess",
        "football",
        "baseball",
        "shadow",
        "master",
        "freedom",
        "whatever",
        "michael",
        "jessica",
        "charlie",
        "jordan",
        "daniel",
        "ferrari",
        "mustang",
        "samsung",
        "winter",
        "spring",
        "summer",
        "autumn",
        "january",
        "february",
        "march",
        "april",
        "june",
        "july",
        "august",
        "september",
        "october",
        "november",
        "december",
    )
    private val SEQUENCES = listOf(
        "abcdefghijklmnopqrstuvwxyz",
        "0123456789",
        "qwertyuiop",
        "asdfghjkl",
        "zxcvbnm",
    )
}
