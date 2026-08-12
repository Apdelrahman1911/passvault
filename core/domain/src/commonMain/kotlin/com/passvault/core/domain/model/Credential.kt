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
    val totp: TotpConfiguration? = null,
) {
    /**
     * Destructively clears every mutable secret owned by this credential.
     * Call this only after the owner has finished using the instance because
     * any aliases to these values will observe the cleared contents.
     */
    fun clearSensitiveValues() {
        username?.clear()
        email?.clear()
        password?.clear()
        notes?.clear()
        recoveryCodes.forEach(SensitiveText::clear)
        apiKeys.forEach(SensitiveText::clear)
        licenseKeys.forEach(SensitiveText::clear)
        customFields.forEach { it.value.clear() }
        passwordHistory.forEach { it.password.clear() }
        totp?.clear()
    }
}

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
    val id: CredentialId
    val type: CredentialType
    val title: String
    val displayUsername: String?
    val isFavorite: Boolean
    val folderId: FolderId?
    val tagIds: Set<TagId>
    val passwordHealth: PasswordHealth
    val lastUsedAt: Instant?

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
    val availability: AttachmentAvailability = AttachmentAvailability.AVAILABLE,
)

@Serializable
enum class AttachmentAvailability {
    AVAILABLE,
    LEGACY_METADATA_ONLY,
}

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
        val authority = extractUrlAuthority(value) ?: return null
        return parseAuthority(authority)?.removePrefix("www.")
    }

    companion object {
        private fun parseAuthority(authority: String): String? =
            if (authority.startsWith('[')) {
                parseBracketedIpv6Authority(authority)
            } else {
                parseHostAuthority(authority)
            }

        private fun parseBracketedIpv6Authority(authority: String): String? {
            val closingBracket = authority.indexOf(']')
            val hasValidBrackets = closingBracket > 1 && authority.indexOf('[', startIndex = 1) < 0
            return if (hasValidBrackets) {
                val literal = authority.substring(1, closingBracket)
                val suffix = authority.substring(closingBracket + 1)
                literal.lowercase().takeIf {
                    suffix.isValidOptionalPort() && literal.isValidIpv6Address()
                }
            } else {
                null
            }
        }

        private fun parseHostAuthority(authority: String): String? {
            val colonCount = authority.count { it == ':' }
            val hasValidDelimiters = authority.none { it == '[' || it == ']' } && colonCount <= 1
            return if (hasValidDelimiters) {
                val host = if (colonCount == 1) authority.substringBeforeLast(':') else authority
                val port = if (colonCount == 1) authority.substringAfterLast(':') else null
                host.lowercase().takeIf {
                    (port == null || ":$port".isValidOptionalPort()) && it.isValidDnsHostOrIpv4()
                }
            } else {
                null
            }
        }

        private fun String.isValidOptionalPort(): Boolean =
            isEmpty() || takeIf { it.startsWith(':') }
                ?.substring(1)
                ?.let { port ->
                    port.isNotEmpty() &&
                        port.length <= MAX_PORT_DIGITS &&
                        port.all(Char::isDigit) &&
                        port.toIntOrNull() in MIN_PORT..MAX_PORT
                } == true

        private fun String.isValidDnsHostOrIpv4(): Boolean {
            val hasValidShape = isNotEmpty() &&
                length <= MAX_HOST_LENGTH &&
                !startsWith('.') &&
                !endsWith('.') &&
                !contains("..")
            return hasValidShape && if (all { it.isDigit() || it == '.' }) {
                isValidIpv4Address()
            } else {
                split('.').all { label ->
                    label.length in 1..MAX_DNS_LABEL_LENGTH &&
                        label.first().isAsciiLetterOrDigit() &&
                        label.last().isAsciiLetterOrDigit() &&
                        label.all { it.isAsciiLetterOrDigit() || it == '-' }
                }
            }
        }

        private fun Char.isAsciiLetterOrDigit(): Boolean =
            this in 'a'..'z' || this in 'A'..'Z' || this in '0'..'9'

        private fun String.isValidIpv4Address(): Boolean = split('.').let { octets ->
            octets.size == IPV4_OCTET_COUNT && octets.all { octet ->
                octet.isNotEmpty() &&
                    octet.length <= MAX_IPV4_OCTET_DIGITS &&
                    octet.all(Char::isDigit) &&
                    (octet.length == 1 || octet.first() != '0') &&
                    octet.toIntOrNull() in MIN_IPV4_OCTET..MAX_IPV4_OCTET
            }
        }

        private fun String.isValidIpv6Address(): Boolean {
            val hasValidShape = isNotEmpty() &&
                !contains('%') &&
                countSubstring("::") <= 1 &&
                !contains(":::")
            return hasValidShape && run {
                val hasCompression = contains("::")
                val sides = if (hasCompression) split("::", limit = 2) else listOf(this)
                val leftUnits = sides[0].ipv6UnitCount(allowIpv4 = !hasCompression)
                val rightUnits = if (hasCompression) sides[1].ipv6UnitCount(allowIpv4 = true) else 0
                leftUnits != null && rightUnits != null &&
                    if (hasCompression) {
                        leftUnits + rightUnits < IPV6_UNIT_COUNT
                    } else {
                        leftUnits == IPV6_UNIT_COUNT
                    }
            }
        }

        private fun String.ipv6UnitCount(allowIpv4: Boolean): Int? {
            return if (isEmpty()) {
                0
            } else {
                val groups = split(':')
                var units = 0
                var valid = groups.none(String::isEmpty)
                groups.forEachIndexed { index, group ->
                    if (group.contains('.')) {
                        val validIpv4Tail = allowIpv4 &&
                            index == groups.lastIndex &&
                            group.isValidIpv4Address()
                        valid = valid && validIpv4Tail
                        units += IPV4_AS_IPV6_UNITS
                    } else {
                        val validHexGroup = group.length in 1..MAX_IPV6_GROUP_LENGTH &&
                            group.all { it.isHexDigit() }
                        valid = valid && validHexGroup
                        units++
                    }
                }
                units.takeIf { valid }
            }
        }

        private fun Char.isHexDigit(): Boolean =
            this in '0'..'9' || this in 'a'..'f' || this in 'A'..'F'

        private fun String.countSubstring(value: String): Int {
            var count = 0
            var offset = 0
            while (true) {
                val next = indexOf(value, startIndex = offset)
                if (next < 0) break
                count++
                offset = next + value.length
            }
            return count
        }

        private const val MAX_HOST_LENGTH = 253
        private const val MAX_DNS_LABEL_LENGTH = 63
        private const val MAX_PORT_DIGITS = 5
        private const val MIN_PORT = 1
        private const val MAX_PORT = 65_535
        private const val IPV4_OCTET_COUNT = 4
        private const val MAX_IPV4_OCTET_DIGITS = 3
        private const val MIN_IPV4_OCTET = 0
        private const val MAX_IPV4_OCTET = 255
        private const val IPV6_UNIT_COUNT = 8
        private const val IPV4_AS_IPV6_UNITS = 2
        private const val MAX_IPV6_GROUP_LENGTH = 4
    }
}

private const val URL_SCHEME_SEPARATOR = "://"
private val URL_SUPPORTED_SCHEMES = setOf("http", "https")

private fun extractUrlAuthority(rawValue: String): String? {
    val trimmed = rawValue.trim()
    val schemeEnd = trimmed.indexOf(URL_SCHEME_SEPARATOR)
    val hasValidPrefix = trimmed == rawValue &&
        trimmed.hasOnlySafeSingleLineCodePoints() &&
        trimmed.none { it.isWhitespace() || it == '\\' } &&
        schemeEnd > 0 &&
        trimmed.substring(0, schemeEnd).lowercase() in URL_SUPPORTED_SCHEMES
    if (!hasValidPrefix) return null

    val remainder = trimmed.substring(schemeEnd + URL_SCHEME_SEPARATOR.length)
    val authorityEnd = remainder.indexOfFirst { it == '/' || it == '?' || it == '#' }
        .let { if (it < 0) remainder.length else it }
    return remainder.substring(0, authorityEnd).takeIf { authority ->
        authority.isNotEmpty() && !authority.contains('@')
    }
}
