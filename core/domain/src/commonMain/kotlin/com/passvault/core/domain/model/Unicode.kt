package com.passvault.core.domain.model

/**
 * Counts Unicode code points without relying on JVM-only String APIs.
 *
 * Valid surrogate pairs count as one code point. Unpaired surrogates count as
 * individual code units, matching the defensive behavior needed for display
 * bounds on malformed input.
 */
fun String.codePointLength(): Int {
    var count = 0
    var index = 0
    while (index < length) {
        if (
            this[index].isHighSurrogate() &&
            index + 1 < length &&
            this[index + 1].isLowSurrogate()
        ) {
            index++
        }
        count++
        index++
    }
    return count
}
