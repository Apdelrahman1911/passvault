package com.passvault.core.domain.model

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Credential(
    val id: CredentialId,
    val type: CredentialType,
    val title: String,
    val username: SensitiveText?,
    val email: SensitiveText?,
    val password: SensitiveText?,
    val urls: List<UrlValue>,
    val notes: SensitiveText?,
    val recoveryCodes: List<SensitiveText>,
    val apiKeys: List<SensitiveText>,
    val licenseKeys: List<SensitiveText>,
    val customFields: List<CustomField>,
    val folderId: FolderId?,
    val tagIds: Set<TagId>,
    val isFavorite: Boolean,
    val attachments: List<AttachmentMetadata>,
    val passwordHistory: List<PasswordHistoryEntry>,
    val createdAt: Instant,
    val updatedAt: Instant,
    val lastUsedAt: Instant?,
    val passwordHealth: PasswordHealth = PasswordHealth.UNKNOWN,
)

@Serializable
sealed interface CredentialType {
    @Serializable data object Login : CredentialType
    @Serializable data object SecureNote : CredentialType
    @Serializable data object ApiKey : CredentialType
    @Serializable data object LicenseKey : CredentialType
    @Serializable data object RecoveryCodes : CredentialType
    @Serializable data object WiFiCredential : CredentialType
    @Serializable data object Identity : CredentialType
    @Serializable data object PaymentCard : CredentialType
    @Serializable data class Custom(val id: String) : CredentialType
}

@Serializable
sealed interface CredentialSummary {
    abstract val id: CredentialId
    abstract val type: CredentialType
    abstract val title: String
    abstract val displayUsername: String?
    abstract val isFavorite: Boolean
    abstract val folderId: FolderId?
    abstract val tagIds: Set<TagId>
    abstract val passwordHealth: PasswordHealth
    abstract val lastUsedAt: Instant?

    @Serializable
    data class Decrypted(
        override val id: CredentialId,
        override val type: CredentialType,
        override val title: String,
        override val displayUsername: String?,
        override val isFavorite: Boolean,
        override val folderId: FolderId?,
        override val tagIds: Set<TagId>,
        override val passwordHealth: PasswordHealth,
        override val lastUsedAt: Instant?,
        val createdAt: Instant,
        val updatedAt: Instant,
    ) : CredentialSummary
}

@Serializable
data class CustomField(
    val id: CustomFieldId,
    val name: String,
    val value: SensitiveText,
    val isSecret: Boolean,
)

@Serializable
data class AttachmentMetadata(
    val id: AttachmentId,
    val fileName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val createdAt: Instant,
)

@Serializable
data class PasswordHistoryEntry(
    val password: SensitiveText,
    val changedAt: Instant,
)

@Serializable
data class PasswordHealth(
    val score: PasswordScore,
    val isDuplicate: Boolean,
    val isWeak: Boolean,
    val isOld: Boolean,
    val ageDays: Int?,
) {
    companion object {
        val UNKNOWN = PasswordHealth(
            score = PasswordScore.UNKNOWN,
            isDuplicate = false,
            isWeak = false,
            isOld = false,
            ageDays = null,
        )
    }
}

@Serializable
enum class PasswordScore {
    UNKNOWN,
    VERY_WEAK,
    WEAK,
    FAIR,
    GOOD,
    STRONG,
    VERY_STRONG,
}

@Serializable
data class UrlValue(val value: String) {
    fun host(): String? {
        val trimmed = value.trim()
        val match = URL_PATTERN.matchEntire(trimmed) ?: return null
        val authority = match.groupValues[2]
        if (authority.contains('@')) return null
        val host = when {
            authority.startsWith("[") -> authority.substringBefore(']').removePrefix("[")
            else -> authority.substringBefore(':')
        }.removePrefix("www.")
        if (host.isBlank() || host.length > 253 || host.any(Char::isWhitespace)) return null
        if (host != "localhost" && !host.contains('.') && host.none(Char::isDigit)) return null
        if (host.startsWith('.') || host.endsWith('.') || host.contains("..")) return null
        return host
    }

    companion object {
        private val URL_PATTERN = Regex("""(?i)^(https?)://([^/?#\s]+)(?:[/?#].*)?$""")
    }
}
