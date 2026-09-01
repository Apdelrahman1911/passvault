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
    fun score(password: CharSequence): PasswordScore = scoreCharacters(password)

    /** Scores a wipeable password buffer without first materializing it as an immutable [String]. */
    fun score(password: CharArray): PasswordScore = scoreCharacters(CharArrayView(password))

    private fun scoreCharacters(password: CharSequence): PasswordScore {
        val codePoints = password.toCodePointBuffer()
        val normalized = normalizeLeetspeak(codePoints)
        return try {
            if (codePoints.size < MIN_PASSWORD_CODE_POINTS) {
                PasswordScore.VERY_WEAK
            } else {
                val characterClasses = characterClassCount(password)
                if (hasExcessiveRepetition(normalized, codePoints.size)) {
                    PasswordScore.VERY_WEAK
                } else {
                    val varietyBonus =
                        if (
                            characterClasses >= MIN_CLASSES_FOR_VARIETY_BONUS &&
                            hasHighDiversity(codePoints)
                        ) {
                            1
                        } else {
                            0
                        }
                    val penaltyBands =
                        (if (COMMON_WEAK_TERMS.any { contains(normalized, codePoints.size, it) }) {
                            COMMON_TERM_PENALTY_BANDS
                        } else {
                            0
                        }) +
                            (if (hasSequence(normalized, codePoints.size)) SEQUENCE_PENALTY_BANDS else 0) +
                            (if (hasLikelyWordAndYear(password)) DATE_PATTERN_PENALTY_BANDS else 0)
                    baselineScore(codePoints.size).shiftBy(varietyBonus - penaltyBands)
                }
            }
        } finally {
            normalized.fill(0)
            codePoints.values.fill(0)
        }
    }

    private fun characterClassCount(value: CharSequence): Int {
        var hasLowercase = false
        var hasUppercase = false
        var hasDigit = false
        var hasSymbol = false
        for (index in value.indices) {
            val character = value[index]
            hasLowercase = hasLowercase || character.isLowerCase()
            hasUppercase = hasUppercase || character.isUpperCase()
            hasDigit = hasDigit || character.isDigit()
            hasSymbol = hasSymbol || !character.isLetterOrDigit()
        }
        return listOf(hasLowercase, hasUppercase, hasDigit, hasSymbol).count { it }
    }

    private fun baselineScore(codePointCount: Int): PasswordScore = when {
        codePointCount >= VERY_STRONG_LENGTH -> PasswordScore.VERY_STRONG
        codePointCount >= STRONG_LENGTH -> PasswordScore.STRONG
        codePointCount >= GOOD_LENGTH -> PasswordScore.GOOD
        codePointCount >= FAIR_LENGTH -> PasswordScore.FAIR
        else -> PasswordScore.WEAK
    }

    private fun hasHighDiversity(codePoints: CodePointBuffer): Boolean {
        var uniqueCount = 0
        for (index in 0 until codePoints.size) {
            val value = codePoints.values[index]
            if ((0 until index).none { previous -> codePoints.values[previous] == value }) uniqueCount++
        }
        return uniqueCount.toDouble() / codePoints.size >= MIN_DIVERSITY_RATIO
    }

    private fun PasswordScore.shiftBy(bands: Int): PasswordScore {
        val currentIndex = STRENGTH_SCALE.indexOf(this)
        check(currentIndex >= 0) { "Cannot adjust an unknown password score" }
        return STRENGTH_SCALE[(currentIndex + bands).coerceIn(STRENGTH_SCALE.indices)]
    }

    private fun normalizeLeetspeak(codePoints: CodePointBuffer): IntArray =
        IntArray(codePoints.size) { index ->
            val value = codePoints.values[index]
            val lowercase = if (value <= Char.MAX_VALUE.code) value.toChar().lowercaseChar().code else value
            when (lowercase) {
                '0'.code -> 'o'.code
                '1'.code, '!'.code -> 'i'.code
                '3'.code -> 'e'.code
                '4'.code, '@'.code -> 'a'.code
                '5'.code, '$'.code -> 's'.code
                '7'.code -> 't'.code
                else -> lowercase
            }
        }

    private fun hasSequence(value: IntArray, size: Int): Boolean {
        if (size < SEQUENCE_LENGTH) return false
        return SEQUENCES.any { sequence ->
            (0..sequence.length - SEQUENCE_LENGTH).any { sequenceStart ->
                containsSequenceWindow(value, size, sequence, sequenceStart, reversed = false) ||
                    containsSequenceWindow(value, size, sequence, sequenceStart, reversed = true)
            }
        }
    }

    private fun hasExcessiveRepetition(value: IntArray, size: Int): Boolean {
        if ((0..size - REPEATED_RUN_LENGTH).any { start ->
                (1 until REPEATED_RUN_LENGTH).all { offset -> value[start + offset] == value[start] }
            }
        ) {
            return true
        }

        val maximumPatternLength = minOf(MAX_REPEATED_PATTERN_LENGTH, size / 2)
        return (1..maximumPatternLength).any { patternLength ->
            (0..size - patternLength * 2).any { startIndex ->
                var matchedLength = patternLength
                while (
                    startIndex + matchedLength < size &&
                    value[startIndex + matchedLength] == value[startIndex + matchedLength % patternLength]
                ) {
                    matchedLength++
                }
                matchedLength >= maxOf(MIN_REPEATED_PATTERN_SPAN, patternLength * 2)
            }
        }
    }

    private fun hasLikelyWordAndYear(value: CharSequence): Boolean =
        hasLetterRun(value, MIN_WORD_RUN_LENGTH) && hasLikelyYear(value)

    private fun hasLetterRun(value: CharSequence, minimumLength: Int): Boolean {
        var runLength = 0
        for (index in value.indices) {
            val character = value[index]
            runLength = if (character.isLetter()) runLength + 1 else 0
            if (runLength >= minimumLength) return true
        }
        return false
    }

    private fun hasLikelyYear(value: CharSequence): Boolean {
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

    private fun contains(value: IntArray, size: Int, term: String): Boolean {
        if (term.length > size) return false
        return (0..size - term.length).any { start ->
            term.indices.all { offset -> value[start + offset] == term[offset].code }
        }
    }

    private fun containsSequenceWindow(
        value: IntArray,
        size: Int,
        sequence: String,
        sequenceStart: Int,
        reversed: Boolean,
    ): Boolean = (0..size - SEQUENCE_LENGTH).any { valueStart ->
        (0 until SEQUENCE_LENGTH).all { offset ->
            val sequenceOffset = if (reversed) SEQUENCE_LENGTH - 1 - offset else offset
            value[valueStart + offset] == sequence[sequenceStart + sequenceOffset].code
        }
    }

    private fun CharSequence.toCodePointBuffer(): CodePointBuffer {
        val values = IntArray(length)
        var sourceIndex = 0
        var resultSize = 0
        while (sourceIndex < length) {
            val first = this[sourceIndex]
            if (first.isHighSurrogate() && sourceIndex + 1 < length && this[sourceIndex + 1].isLowSurrogate()) {
                val second = this[sourceIndex + 1]
                values[resultSize++] =
                    ((first.code - HIGH_SURROGATE_START) shl SURROGATE_SHIFT) +
                        (second.code - LOW_SURROGATE_START) +
                        SUPPLEMENTARY_CODE_POINT_START
                sourceIndex += 2
            } else {
                values[resultSize++] = first.code
                sourceIndex++
            }
        }
        return CodePointBuffer(values, resultSize)
    }

    private class CodePointBuffer(
        val values: IntArray,
        val size: Int,
    )

    private class CharArrayView(
        private val value: CharArray,
        private val start: Int = 0,
        private val end: Int = value.size,
    ) : CharSequence {
        override val length: Int get() = end - start

        override fun get(index: Int): Char {
            require(index in indices) { "Index is outside the character view" }
            return value[start + index]
        }

        override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
            require(startIndex in 0..length && endIndex in startIndex..length) {
                "Subsequence is outside the character view"
            }
            return CharArrayView(value, start + startIndex, start + endIndex)
        }

        override fun toString(): String = "<redacted>"
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
    private const val HIGH_SURROGATE_START = 0xD800
    private const val LOW_SURROGATE_START = 0xDC00
    private const val SURROGATE_SHIFT = 10
    private const val SUPPLEMENTARY_CODE_POINT_START = 0x10000
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
