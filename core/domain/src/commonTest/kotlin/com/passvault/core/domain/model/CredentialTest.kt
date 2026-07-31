package com.passvault.core.domain.model

import assertk.assertThat
import assertk.assertions.*
import kotlin.test.*
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Unit tests for Credential domain model.
 */
class CredentialTest {

    @Test
    fun `credential has all required fields`() {
        val credential = createTestCredential()

        assertThat(credential.id.value).isEqualTo("cred-123")
        assertThat(credential.title).isEqualTo("Test Credential")
        assertThat(credential.type).isEqualTo(CredentialType.Login)
    }

    @Test
    fun `credential id is typed`() {
        val id = CredentialId("test-id")
        val credential = createTestCredential(id = "test-id")

        assertThat(credential.id).isEqualTo(id)
    }

    @Test
    fun `credential type can be login`() {
        val credential = createTestCredential(type = CredentialType.Login)
        assertThat(credential.type).isEqualTo(CredentialType.Login)
    }

    @Test
    fun `credential type can be secure note`() {
        val credential = createTestCredential(type = CredentialType.SecureNote)
        assertThat(credential.type).isEqualTo(CredentialType.SecureNote)
    }

    @Test
    fun `credential type can be api key`() {
        val credential = createTestCredential(type = CredentialType.ApiKey)
        assertThat(credential.type).isEqualTo(CredentialType.ApiKey)
    }

    @Test
    fun `credential type can be wifi credential`() {
        val credential = createTestCredential(type = CredentialType.WiFiCredential)
        assertThat(credential.type).isEqualTo(CredentialType.WiFiCredential)
    }

    @Test
    fun `credential type can be payment card`() {
        val credential = createTestCredential(type = CredentialType.PaymentCard)
        assertThat(credential.type).isEqualTo(CredentialType.PaymentCard)
    }

    @Test
    fun `credential type can be identity`() {
        val credential = createTestCredential(type = CredentialType.Identity)
        assertThat(credential.type).isEqualTo(CredentialType.Identity)
    }

    @Test
    fun `credential type can be recovery codes`() {
        val credential = createTestCredential(type = CredentialType.RecoveryCodes)
        assertThat(credential.type).isEqualTo(CredentialType.RecoveryCodes)
    }

    @Test
    fun `credential type can be license key`() {
        val credential = createTestCredential(type = CredentialType.LicenseKey)
        assertThat(credential.type).isEqualTo(CredentialType.LicenseKey)
    }

    @Test
    fun `credential type can be custom`() {
        val customType = CredentialType.Custom("custom-id")
        val credential = createTestCredential(type = customType)
        assertThat(credential.type).isEqualTo(customType)
    }

    @Test
    fun `credential can be favorited`() {
        val credential = createTestCredential(isFavorite = true)
        assertThat(credential.isFavorite).isTrue()
    }

    @Test
    fun `credential tracks timestamps`() {
        val createdAt = Clock.System.now()
        val updatedAt = Clock.System.now()

        val credential = createTestCredential(
            createdAt = createdAt,
            updatedAt = updatedAt
        )

        assertThat(credential.createdAt).isEqualTo(createdAt)
        assertThat(credential.updatedAt).isEqualTo(updatedAt)
    }

    @Test
    fun `credential can have folder`() {
        val folderId = FolderId("folder-123")
        val credential = createTestCredential(folderId = folderId)

        assertThat(credential.folderId).isEqualTo(folderId)
    }

    @Test
    fun `credential can have tags`() {
        val tagIds = setOf(TagId("tag-1"), TagId("tag-2"))
        val credential = createTestCredential(tagIds = tagIds)

        assertThat(credential.tagIds).hasSize(2)
        assertThat(credential.tagIds).contains(TagId("tag-1"))
        assertThat(credential.tagIds).contains(TagId("tag-2"))
    }

    @Test
    fun `credential can have urls`() {
        val urls = listOf(
            UrlValue("https://example.com"),
            UrlValue("https://www.example.com/login")
        )
        val credential = createTestCredential(urls = urls)

        assertThat(credential.urls).hasSize(2)
    }

    @Test
    fun `url value extracts host correctly`() {
        val url = UrlValue("https://www.example.com/path")
        assertThat(url.host()).isEqualTo("example.com")
    }

    @Test
    fun `url value handles url without www`() {
        val url = UrlValue("https://github.com/user/repo")
        assertThat(url.host()).isEqualTo("github.com")
    }

    @Test
    fun `url value returns null for invalid url`() {
        val url = UrlValue("not-a-valid-url")
        assertThat(url.host()).isNull()
    }

    @Test
    fun `credential summary contains essential fields`() {
        val now = Clock.System.now()
        val summary = CredentialSummary.Decrypted(
            id = CredentialId("cred-123"),
            type = CredentialType.Login,
            title = "Test",
            displayUsername = "user@example.com",
            isFavorite = false,
            folderId = null,
            tagIds = emptySet(),
            passwordHealth = PasswordHealth.UNKNOWN,
            lastUsedAt = null,
            createdAt = now,
            updatedAt = now
        )

        assertThat(summary.id.value).isEqualTo("cred-123")
        assertThat(summary.title).isEqualTo("Test")
        assertThat(summary.displayUsername).isEqualTo("user@example.com")
    }

    @Test
    fun `credential can have custom fields`() {
        val customFields = listOf(
            CustomField(
                id = CustomFieldId("cf-1"),
                name = "Security Question",
                value = SensitiveText.from("Answer"),
                isSecret = true
            )
        )
        val credential = createTestCredential(customFields = customFields)

        assertThat(credential.customFields).hasSize(1)
        assertThat(credential.customFields[0].name).isEqualTo("Security Question")
    }

    @Test
    fun `credential can have password history`() {
        val history = listOf(
            PasswordHistoryEntry(
                password = SensitiveText.from("old-password"),
                changedAt = Clock.System.now()
            )
        )
        val credential = createTestCredential(passwordHistory = history)

        assertThat(credential.passwordHistory).hasSize(1)
    }

    @Test
    fun `credential can have attachments`() {
        val attachments = listOf(
            AttachmentMetadata(
                id = AttachmentId("att-1"),
                fileName = "document.pdf",
                mimeType = "application/pdf",
                sizeBytes = 1024,
                createdAt = Clock.System.now()
            )
        )
        val credential = createTestCredential(attachments = attachments)

        assertThat(credential.attachments).hasSize(1)
        assertThat(credential.attachments[0].fileName).isEqualTo("document.pdf")
    }

    // Helper function
    private fun createTestCredential(
        id: String = "cred-123",
        type: CredentialType = CredentialType.Login,
        title: String = "Test Credential",
        isFavorite: Boolean = false,
        folderId: FolderId? = null,
        tagIds: Set<TagId> = emptySet(),
        urls: List<UrlValue> = emptyList(),
        customFields: List<CustomField> = emptyList(),
        passwordHistory: List<PasswordHistoryEntry> = emptyList(),
        attachments: List<AttachmentMetadata> = emptyList(),
        createdAt: Instant = Clock.System.now(),
        updatedAt: Instant = Clock.System.now()
    ): Credential {
        return Credential(
            id = CredentialId(id),
            type = type,
            title = title,
            username = SensitiveText.from("testuser"),
            email = SensitiveText.from("test@example.com"),
            password = SensitiveText.from("TestPassword123!"),
            urls = urls,
            notes = null,
            recoveryCodes = emptyList(),
            apiKeys = emptyList(),
            licenseKeys = emptyList(),
            customFields = customFields,
            folderId = folderId,
            tagIds = tagIds,
            isFavorite = isFavorite,
            attachments = attachments,
            passwordHistory = passwordHistory,
            createdAt = createdAt,
            updatedAt = updatedAt,
            lastUsedAt = null
        )
    }
}
