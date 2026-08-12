package com.passvault.core.domain.model

/**
 * Rejects controls, malformed UTF-16, Unicode format controls, and explicit
 * line/paragraph separators in values rendered as single-line labels.
 * Supplementary code points are decoded before validation so format controls
 * outside the BMP cannot hide inside an otherwise valid surrogate pair.
 */
fun String.hasOnlySafeSingleLineCodePoints(): Boolean =
    hasOnlySafeSingleLineCodePointsOf(length) { index -> this[index] }

internal fun CharArray.hasOnlySafeSingleLineCodePoints(): Boolean =
    hasOnlySafeSingleLineCodePointsOf(size) { index -> this[index] }

private inline fun hasOnlySafeSingleLineCodePointsOf(
    size: Int,
    characterAt: (Int) -> Char,
): Boolean {
    var index = 0
    var isSafe = true
    while (index < size && isSafe) {
        val first = characterAt(index).code
        var width = 1
        val codePoint = when {
            first in HIGH_SURROGATE_RANGE && index + 1 < size -> {
                val second = characterAt(index + 1).code
                if (second in LOW_SURROGATE_RANGE) {
                    width = 2
                    SUPPLEMENTARY_CODE_POINT_OFFSET +
                        ((first - HIGH_SURROGATE_START) shl SURROGATE_SHIFT) +
                        (second - LOW_SURROGATE_START)
                } else {
                    INVALID_CODE_POINT
                }
            }
            first in SURROGATE_RANGE -> INVALID_CODE_POINT
            else -> first
        }
        isSafe = codePoint != INVALID_CODE_POINT && !codePoint.isUnsafeSingleLineCodePoint()
        index += width
    }
    return isSafe
}

private fun Int.isUnsafeSingleLineCodePoint(): Boolean = when (this) {
    in 0x0000..0x001F,
    in 0x007F..0x009F,
    0x00AD,
    in 0x0600..0x0605,
    0x061C,
    0x06DD,
    0x070F,
    in 0x0890..0x0891,
    0x08E2,
    0x180E,
    in 0x200B..0x200F,
    in 0x2028..0x202E,
    in 0x2060..0x206F,
    0xFEFF,
    in 0xFFF9..0xFFFB,
    0x110BD,
    0x110CD,
    in 0x13430..0x1343F,
    in 0x1BCA0..0x1BCA3,
    in 0x1D173..0x1D17A,
    0xE0001,
    in 0xE0020..0xE007F,
    -> true
    else -> false
}

private const val HIGH_SURROGATE_START = 0xD800
private const val LOW_SURROGATE_START = 0xDC00
private const val INVALID_CODE_POINT = -1
private const val SURROGATE_SHIFT = 10
private const val SUPPLEMENTARY_CODE_POINT_OFFSET = 0x10000
private val HIGH_SURROGATE_RANGE = HIGH_SURROGATE_START..0xDBFF
private val LOW_SURROGATE_RANGE = LOW_SURROGATE_START..0xDFFF
private val SURROGATE_RANGE = HIGH_SURROGATE_START..0xDFFF
