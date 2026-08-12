package com.passvault.core.testing

import com.passvault.core.domain.model.AttachmentId
import com.passvault.core.domain.model.AttachmentMetadata
import com.passvault.core.domain.model.Credential
import com.passvault.core.domain.model.CredentialId
import com.passvault.core.domain.model.CredentialType
import com.passvault.core.domain.model.CustomField
import com.passvault.core.domain.model.CustomFieldId
import com.passvault.core.domain.model.Folder
import com.passvault.core.domain.model.FolderId
import com.passvault.core.domain.model.PasswordHealth
import com.passvault.core.domain.model.PasswordHistoryEntry
import com.passvault.core.domain.model.PasswordScore
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.model.Tag
import com.passvault.core.domain.model.TagId
import com.passvault.core.domain.model.UrlValue
import com.passvault.core.domain.model.VaultId
import com.passvault.core.domain.model.VaultMetadata
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Test data factory for creating sample credentials and other domain objects.
 *
 * The factory intentionally keeps canonical domain fixtures under one
 * namespace so tests do not diverge on security-sensitive defaults.
 */
@Suppress("TooManyFunctions")
object TestData {
    private fun uniqueId(prefix: String): String =
        "$prefix-${kotlin.uuid.Uuid.random()}"

    val now: Instant
        get() = Clock.System.now()

    /**
     * Create a sample credential for testing.
     */
    fun credential(
        id: String = uniqueId("cred"),
        type: CredentialType = CredentialType.Login,
        title: String = "Test Credential",
        username: String = "testuser",
        email: String? = null,
        password: String = "TestPassword123!",
        url: String = "https://example.com",
        isFavorite: Boolean = false,
        folderId: String? = null,
        tagIds: Set<String> = emptySet(),
    ): Credential {
        return Credential(
            id = CredentialId(id),
            type = type,
            title = title,
            username = if (username.isNotEmpty()) SensitiveText.from(username) else null,
            email = email?.let { SensitiveText.from(it) },
            password = SensitiveText.from(password),
            urls = listOf(UrlValue(url)),
            notes = null,
            recoveryCodes = emptyList(),
            apiKeys = emptyList(),
            licenseKeys = emptyList(),
            customFields = emptyList(),
            folderId = folderId?.let { FolderId(it) },
            tagIds = tagIds.map { TagId(it) }.toSet(),
            isFavorite = isFavorite,
            attachments = emptyList(),
            passwordHistory = emptyList(),
            createdAt = now,
            updatedAt = now,
            lastUsedAt = null,
        )
    }

    /**
     * Create a sample login credential.
     */
    fun loginCredential(
        title: String = "Test Login",
        username: String = "testuser",
        password: String = "TestPass123!",
        url: String = "https://example.com",
        isFavorite: Boolean = false,
    ) = credential(
        type = CredentialType.Login,
        title = title,
        username = username,
        password = password,
        url = url,
        isFavorite = isFavorite,
    )

    /**
     * Create a sample secure note credential.
     */
    fun secureNoteCredential(
        title: String = "Test Note",
        notes: String = "This is a test secure note content.",
    ) = credential(
        type = CredentialType.SecureNote,
        title = title,
        username = "",
        password = "",
    ).copy(
        notes = SensitiveText.from(notes),
    )

    /**
     * Create a sample API key credential.
     */
    fun apiKeyCredential(
        title: String = "Test API Key",
        apiKey: String = "api-key-${randomString(32)}",
    ) = credential(
        type = CredentialType.ApiKey,
        title = title,
        username = "",
        password = apiKey,
    )

    /**
     * Create a sample WiFi credential.
     */
    fun wifiCredential(
        ssid: String = "TestNetwork",
        password: String = "WiFiPassword123!",
    ) = credential(
        type = CredentialType.WiFiCredential,
        title = ssid,
        username = "",
        password = password,
    )

    /**
     * Create a sample payment card credential.
     */
    fun paymentCardCredential(
        cardNumber: String = "4532123456789012",
        expiry: String = "12/25",
        cvv: String = "123",
        cardholder: String = "John Doe",
    ) = credential(
        type = CredentialType.PaymentCard,
        title = "****${cardNumber.takeLast(4)}",
        username = cardholder,
        password = cvv,
    ).copy(
        customFields = listOf(
            CustomField(
                id = CustomFieldId("card_number"),
                name = "Card Number",
                value = SensitiveText.from(cardNumber),
                isSecret = true,
            ),
            CustomField(
                id = CustomFieldId("expiry"),
                name = "Expiry",
                value = SensitiveText.from(expiry),
                isSecret = false,
            ),
        ),
    )

    /**
     * Create a sample folder.
     */
    fun folder(
        id: String = uniqueId("folder"),
        name: String = "Test Folder",
        parentId: String? = null,
        icon: String = "📁",
        sortOrder: Int = 0,
    ): Folder {
        return Folder(
            id = FolderId(id),
            parentId = parentId?.let { FolderId(it) },
            name = name,
            icon = icon,
            sortOrder = sortOrder,
            createdAt = now,
        )
    }

    /**
     * Create a sample tag.
     */
    fun tag(
        id: String = uniqueId("tag"),
        name: String = "Test Tag",
        color: String = "#FF5733",
    ): Tag {
        return Tag(
            id = TagId(id),
            name = name,
            color = color,
        )
    }

    /**
     * Create sample vault metadata.
     */
    fun vaultMetadata(
        id: String = uniqueId("vault"),
        formatVersion: Int = 1,
        entryCount: Int = 0,
    ): VaultMetadata {
        return VaultMetadata(
            id = VaultId(id),
            formatVersion = formatVersion,
            createdAt = now,
            lastAccessedAt = null,
            entryCount = entryCount,
        )
    }

    /**
     * Create a sample attachment metadata.
     */
    fun attachmentMetadata(
        id: String = uniqueId("attachment"),
        fileName: String = "test.txt",
        mimeType: String = "text/plain",
        sizeBytes: Long = 1024,
    ): AttachmentMetadata {
        return AttachmentMetadata(
            id = AttachmentId(id),
            fileName = fileName,
            mimeType = mimeType,
            sizeBytes = sizeBytes,
            createdAt = now,
        )
    }

    /**
     * Create a password history entry.
     */
    fun passwordHistoryEntry(
        password: String = "OldPassword123!",
        changedAt: Instant = now,
    ): PasswordHistoryEntry {
        return PasswordHistoryEntry(
            password = SensitiveText.from(password),
            changedAt = changedAt,
        )
    }

    /**
     * Create a password health assessment.
     */
    fun passwordHealth(
        score: PasswordScore = PasswordScore.GOOD,
        isDuplicate: Boolean = false,
        isWeak: Boolean = false,
        isOld: Boolean = false,
        ageDays: Int? = null,
    ): PasswordHealth {
        return PasswordHealth(
            score = score,
            isDuplicate = isDuplicate,
            isWeak = isWeak,
            isOld = isOld,
            ageDays = ageDays,
        )
    }

    /**
     * Create a list of test credentials.
     */
    fun sampleCredentials(count: Int = 5): List<Credential> {
        return (1..count).map { index ->
            credential(
                id = "cred-$index",
                title = "Test Credential $index",
                username = "user$index",
                password = "Password${index}!",
                isFavorite = index == 1,
            )
        }
    }

    /**
     * Create a list of test folders.
     */
    fun sampleFolders(count: Int = 3): List<Folder> {
        return (1..count).map { index ->
            folder(
                id = "folder-$index",
                name = "Folder $index",
                sortOrder = index,
            )
        }
    }

    /**
     * Create a list of test tags.
     */
    fun sampleTags(count: Int = 3): List<Tag> {
        val colors = listOf("#FF5733", "#33FF57", "#3357FF", "#FF33F5", "#F5FF33")
        return (1..count).map { index ->
            tag(
                id = "tag-$index",
                name = "Tag $index",
                color = colors[index % colors.size],
            )
        }
    }

    /**
     * Test byte arrays for crypto operations.
     */
    object Crypto {

        /**
         * Known test key (32 bytes).
         */
        val testKey: ByteArray
            get() = ByteArray(32) { (it + 1).toByte() }

        /**
         * Known test salt (16 bytes).
         */
        val testSalt: ByteArray
            get() = ByteArray(16) { (it + 100).toByte() }

        /**
         * Known test nonce (24 bytes for XChaCha20).
         */
        val testNonce: ByteArray
            get() = ByteArray(24) { (it + 50).toByte() }

        /**
         * Known plaintext.
         */
        val testPlaintext: ByteArray
            get() = "Hello, PassVault! This is test data.".encodeToByteArray()

        /**
         * Known password for key derivation.
         */
        val testPassword: ByteArray
            get() = "TestPassword123!".encodeToByteArray()
    }

    private fun randomString(length: Int = 16): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length).map { chars.random() }.joinToString("")
    }
}

/**
 * Extension to create a credential with modified properties.
 */
fun Credential.withId(newId: String) = copy(id = CredentialId(newId))
fun Credential.withTitle(newTitle: String) = copy(title = newTitle)
fun Credential.withPassword(newPassword: String) = copy(password = SensitiveText.from(newPassword))
fun Credential.withFavorite(favorite: Boolean) = copy(isFavorite = favorite)
fun Credential.withFolder(folderId: String?) = copy(folderId = folderId?.let { FolderId(it) })
fun Credential.withTags(vararg tags: String) = copy(tagIds = tags.map { TagId(it) }.toSet())
