package com.passvault.core.domain.model

/**
 * Counts Unicode code points without relying on JVM-only String APIs.
 *
 * Valid surrogate pairs count as one code point. Unpaired surrogates count as
 * individual code units, matching the defensive behavior needed for display
 * bounds on malformed input.
 */
fun String.codePointLength(): Int {
    return codePointLengthOf(length) { index -> this[index] }
}

/**
 * Returns at most [maxCodePoints] complete Unicode code points without
 * splitting a valid surrogate pair at the boundary.
 */
fun String.takeCodePoints(maxCodePoints: Int): String {
    require(maxCodePoints >= 0) { "Maximum code-point count must not be negative" }
    var codePointCount = 0
    var endIndex = 0
    while (endIndex < length && codePointCount < maxCodePoints) {
        endIndex += if (
            this[endIndex].isHighSurrogate() &&
            endIndex + 1 < length &&
            this[endIndex + 1].isLowSurrogate()
        ) {
            2
        } else {
            1
        }
        codePointCount++
    }
    return if (endIndex == length) this else substring(0, endIndex)
}

/** Returns true when every UTF-16 surrogate belongs to a valid pair. */
fun String.hasWellFormedUnicode(): Boolean {
    return hasWellFormedUnicodeOf(length) { index -> this[index] }
}

internal fun CharArray.codePointLength(): Int {
    return codePointLengthOf(size) { index -> this[index] }
}

internal fun CharArray.hasWellFormedUnicode(): Boolean {
    return hasWellFormedUnicodeOf(size) { index -> this[index] }
}

internal fun String.toCodePointStrings(): List<String> =
    toCharArray().toCodePointStrings()

internal fun CharArray.toCodePointStrings(): List<String> {
    val values = mutableListOf<String>()
    var index = 0
    while (index < size) {
        val end = if (
            this[index].isHighSurrogate() &&
            index + 1 < size &&
            this[index + 1].isLowSurrogate()
        ) {
            index + 2
        } else {
            index + 1
        }
        values += concatToString(index, end)
        index = end
    }
    return values
}

private inline fun codePointLengthOf(size: Int, characterAt: (Int) -> Char): Int {
    var count = 0
    var index = 0
    while (index < size) {
        if (
            characterAt(index).isHighSurrogate() &&
            index + 1 < size &&
            characterAt(index + 1).isLowSurrogate()
        ) {
            index++
        }
        count++
        index++
    }
    return count
}

private inline fun hasWellFormedUnicodeOf(size: Int, characterAt: (Int) -> Char): Boolean {
    var index = 0
    var isWellFormed = true
    while (index < size && isWellFormed) {
        val character = characterAt(index)
        when {
            character.isHighSurrogate() -> {
                if (index + 1 < size && characterAt(index + 1).isLowSurrogate()) {
                    index += 2
                } else {
                    isWellFormed = false
                }
            }
            character.isLowSurrogate() -> isWellFormed = false
            else -> index++
        }
    }
    return isWellFormed
}
