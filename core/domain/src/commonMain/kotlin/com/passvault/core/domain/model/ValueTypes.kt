package com.passvault.core.domain.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Sensitive text wrapper that prevents accidental logging and serialization.
 */
@Serializable(with = SensitiveTextSerializer::class)
class SensitiveText private constructor(
    private val _value: CharArray
)
{
    companion object {
        fun from(value: String): SensitiveText {
            return SensitiveText(value.toCharArray())
        }

        fun from(value: CharArray): SensitiveText {
            return SensitiveText(value.copyOf())
        }

        fun from(value: ByteArray): SensitiveText {
            return SensitiveText(value.decodeToString().toCharArray())
        }
    }

    /**
     * Access the sensitive value. Caller is responsible for clearing when done.
     */
    fun expose(): CharArray {
        return _value.copyOf()
    }

    /**
     * Get the value as a String. Use with caution - strings are immutable.
     */
    fun toStringUnsafe(): String {
        return _value.concatToString()
    }

    /**
     * Clear the sensitive data from memory.
     */
    fun clear() {
        _value.fill('\u0000')
    }

    /**
     * Returns true if the value is empty.
     */
    fun isEmpty(): Boolean = _value.isEmpty()

    /**
     * Returns true if the value is not empty.
     */
    fun isNotEmpty(): Boolean = _value.isNotEmpty()

    /**
     * Length of the sensitive text.
     */
    val length: Int get() = codePointCount(_value)

    /**
     * Mask the value for display (e.g., "•••••" or first/last 3 chars).
     */
    fun mask(): String {
        val codePoints = _value.toCodePointStrings()
        return if (codePoints.size <= 6) {
            "•".repeat(codePoints.size)
        } else {
            "${codePoints.take(3).joinToString("")}•••${codePoints.takeLast(3).joinToString("")}"
        }
    }

    /**
     * For debugging - returns redacted representation.
     */
    override fun toString(): String = "[REDACTED: ${length} chars]"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SensitiveText) return false
        return _value.contentEquals(other._value)
    }

    override fun hashCode(): Int {
        return _value.contentHashCode()
    }

    protected fun finalize() {
        clear()
    }
}

/**
 * Serializer that prevents accidental serialization of sensitive data.
 * Always serializes as REDACTED marker.
 */
object SensitiveTextSerializer : KSerializer<SensitiveText> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("SensitiveText", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: SensitiveText) {
        // Never serialize actual content
        encoder.encodeString("[SENSITIVE]")
    }

    override fun deserialize(decoder: Decoder): SensitiveText {
        // Deserializing sensitive text from storage should fail
        // Sensitive text should only be created programmatically
        throw IllegalStateException("SensitiveText cannot be deserialized. Use encrypted storage.")
    }
}

private fun codePointCount(chars: CharArray): Int {
    var count = 0
    var index = 0
    while (index < chars.size) {
        if (chars[index].isHighSurrogate() &&
            index + 1 < chars.size &&
            chars[index + 1].isLowSurrogate()
        ) {
            index++
        }
        count++
        index++
    }
    return count
}

private fun CharArray.toCodePointStrings(): List<String> {
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
