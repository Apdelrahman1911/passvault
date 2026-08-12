package com.passvault.core.domain.model

/**
 * One length policy for newly created master passwords across UI and storage
 * boundaries. Unlock still accepts every length this version could create.
 */
object MasterPasswordPolicy {
    const val MIN_LENGTH = 12
    const val MAX_LENGTH = 1_024

    fun accepts(length: Int): Boolean = length in MIN_LENGTH..MAX_LENGTH

    fun accepts(value: String): Boolean = value.hasWellFormedUnicode() && accepts(value.codePointLength())

    fun accepts(value: SensitiveText): Boolean = value.hasWellFormedUnicode() && accepts(value.length)

    fun acceptsExisting(value: SensitiveText): Boolean =
        value.hasWellFormedUnicode() && value.length in 1..MAX_LENGTH
}
