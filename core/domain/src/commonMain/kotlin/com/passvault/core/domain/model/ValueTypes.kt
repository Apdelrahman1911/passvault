package com.passvault.core.domain.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Sensitive text wrapper that prevents accidental logging and serialization.
 */
@Serializable(with = SensitiveTextSerializer::class)
@Suppress("TooManyFunctions") // Secret lifecycle, validation, and redacted value semantics belong together.
class SensitiveText private constructor(
    private val characters: CharArray,
) {
    companion object {
        private const val REDACTED_HASH_CODE = 0

        fun from(value: String): SensitiveText {
            return SensitiveText(value.toCharArray())
        }

        fun from(value: CharArray): SensitiveText {
            return SensitiveText(value.copyOf())
        }

        fun from(value: ByteArray): SensitiveText {
            return SensitiveText(
                value.decodeToString(throwOnInvalidSequence = true).toCharArray(),
            )
        }
    }

    /**
     * Access the sensitive value. Caller is responsible for clearing when done.
     */
    fun expose(): CharArray {
        return characters.copyOf()
    }

    /**
     * Get the value as a String. Use with caution - strings are immutable.
     */
    fun toStringUnsafe(): String {
        return characters.concatToString()
    }

    /**
     * Encodes this value without replacing malformed UTF-16. The returned
     * bytes remain sensitive and must be cleared by the caller.
     */
    fun toUtf8ByteArray(): ByteArray {
        return characters.concatToString().encodeToByteArray(throwOnInvalidSequence = true)
    }

    /** Returns false when the value contains an unpaired UTF-16 surrogate. */
    fun hasWellFormedUnicode(): Boolean = characters.hasWellFormedUnicode()

    /** Returns false when this value could alter or escape a single-line label. */
    fun hasOnlySafeSingleLineCodePoints(): Boolean = characters.hasOnlySafeSingleLineCodePoints()

    /**
     * Clear the sensitive data from memory.
     */
    fun clear() {
        characters.fill('\u0000')
    }

    /**
     * Returns true if the value is empty.
     */
    fun isEmpty(): Boolean = characters.isEmpty()

    /**
     * Returns true if the value is not empty.
     */
    fun isNotEmpty(): Boolean = characters.isNotEmpty()

    /**
     * Length of the sensitive text.
     */
    val length: Int get() = characters.codePointLength()

    /**
     * Mask the value for display (e.g., "•••••" or first/last 3 chars).
     */
    fun mask(): String {
        val codePoints = characters.toCodePointStrings()
        return if (codePoints.size <= 6) {
            "•".repeat(codePoints.size)
        } else {
            "${codePoints.take(3).joinToString("")}•••${codePoints.takeLast(3).joinToString("")}"
        }
    }

    /**
     * For debugging - returns redacted representation.
     */
    override fun toString(): String = "[REDACTED]"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SensitiveText) return false
        if (characters.size != other.characters.size) return false
        var difference = 0
        characters.indices.forEach { index ->
            difference = difference or (characters[index].code xor other.characters[index].code)
        }
        return difference == 0
    }

    // Do not expose a predictable plaintext-derived fingerprint through logs,
    // metrics, hash-based debugging, or an enclosing data class. Unequal
    // secrets may deliberately collide; equality remains the authority.
    override fun hashCode(): Int = REDACTED_HASH_CODE

    protected fun finalize() {
        clear()
    }
}

/**
 * Serializer that prevents accidental serialization of sensitive data.
 * Both directions fail closed; encrypted persistence uses dedicated DTOs.
 */
object SensitiveTextSerializer : KSerializer<SensitiveText> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("SensitiveText", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: SensitiveText) {
        throw SerializationException("SensitiveText cannot be serialized. Use encrypted storage.")
    }

    override fun deserialize(decoder: Decoder): SensitiveText {
        // Deserializing sensitive text from storage should fail
        // Sensitive text should only be created programmatically
        throw SerializationException("SensitiveText cannot be deserialized. Use encrypted storage.")
    }
}

/**
 * Strongly-typed ID for type safety.
 */
@Serializable
sealed interface TypedId {
    val value: String
}

// Convert value classes to data classes since @JvmInline is not supported in Kotlin 2.x
@Serializable
data class CredentialId(override val value: String) : TypedId

@Serializable
data class FolderId(override val value: String) : TypedId

@Serializable
data class TagId(override val value: String) : TypedId

@Serializable
data class AttachmentId(override val value: String) : TypedId

@Serializable
data class CustomFieldId(override val value: String) : TypedId

@Serializable
data class VaultId(override val value: String) : TypedId

@Serializable
data class SessionId(override val value: String) : TypedId

/**
 * Folder model.
 */
@Serializable
data class Folder(
    val id: FolderId,
    val parentId: FolderId?,
    val name: String,
    val icon: String?, // Emoji or icon name
    val sortOrder: Int,
    val createdAt: kotlin.time.Instant,
)

/**
 * Tag model.
 */
@Serializable
data class Tag(
    val id: TagId,
    val name: String,
    val color: String?, // Hex color code
)

/**
 * Vault metadata.
 */
@Serializable
data class VaultMetadata(
    val id: VaultId,
    val formatVersion: Int,
    val createdAt: kotlin.time.Instant,
    val lastAccessedAt: kotlin.time.Instant?,
    val entryCount: Int,
)
