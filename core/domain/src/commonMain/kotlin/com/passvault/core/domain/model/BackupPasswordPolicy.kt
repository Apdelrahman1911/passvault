package com.passvault.core.domain.model

/** Format-level passphrase policy for newly created encrypted backup files. */
object BackupPasswordPolicy {
    const val MIN_LENGTH = 12
    const val MAX_LENGTH = 1_024

    fun acceptsNew(length: Int): Boolean = length in MIN_LENGTH..MAX_LENGTH

    fun acceptsNew(value: String): Boolean = value.hasWellFormedUnicode() && acceptsNew(value.codePointLength())

    fun acceptsNew(value: SensitiveText): Boolean = value.hasWellFormedUnicode() && acceptsNew(value.length)

    /** Imports remain compatible with every non-empty passphrase older builds could accept. */
    fun acceptsExisting(length: Int): Boolean = length in 1..MAX_LENGTH

    fun acceptsExisting(value: String): Boolean =
        value.hasWellFormedUnicode() && acceptsExisting(value.codePointLength())

    fun acceptsExisting(value: SensitiveText): Boolean =
        value.hasWellFormedUnicode() && acceptsExisting(value.length)
}
