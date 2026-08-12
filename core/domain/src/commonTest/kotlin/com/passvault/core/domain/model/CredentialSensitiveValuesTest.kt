package com.passvault.core.domain.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import kotlin.test.Test
import kotlin.time.Clock

class CredentialSensitiveValuesTest {

    @Test
    fun `clear sensitive values wipes every credential-owned secret and is idempotent`() {
        val ownedSecrets = mutableListOf<SensitiveText>()
        val credential = credentialWithEverySecret { value ->
            SensitiveText.from(value).also(ownedSecrets::add)
        }

        credential.clearSensitiveValues()
        credential.clearSensitiveValues()

        ownedSecrets.forEach { secret ->
            val characters = secret.expose()
            try {
                assertThat(characters.isNotEmpty()).isTrue()
                assertThat(characters.all { it == '\u0000' }).isTrue()
            } finally {
                characters.fill('\u0000')
            }
        }
        assertThat(credential.id).isEqualTo(CredentialId("credential-id"))
        assertThat(credential.title).isEqualTo("Non-sensitive title")
        assertThat(credential.urls).isEqualTo(listOf(UrlValue("https://example.com")))
    }

    private fun credentialWithEverySecret(secret: (String) -> SensitiveText): Credential {
        val now = Clock.System.now()
        return Credential(
            id = CredentialId("credential-id"),
            type = CredentialType.Login,
            title = "Non-sensitive title",
            username = secret("username-secret"),
            email = secret("email-secret"),
            password = secret("password-secret"),
            urls = listOf(UrlValue("https://example.com")),
            notes = secret("notes-secret"),
            recoveryCodes = listOf(secret("recovery-one"), secret("recovery-two")),
            apiKeys = listOf(secret("api-one"), secret("api-two")),
            licenseKeys = listOf(secret("license-one"), secret("license-two")),
            customFields = listOf(
                CustomField(CustomFieldId("custom-one"), "First", secret("custom-one"), true),
                CustomField(CustomFieldId("custom-two"), "Second", secret("custom-two"), false),
            ),
            folderId = FolderId("folder-id"),
            tagIds = setOf(TagId("tag-id")),
            isFavorite = true,
            attachments = emptyList(),
            passwordHistory = listOf(
                PasswordHistoryEntry(secret("history-one"), now),
                PasswordHistoryEntry(secret("history-two"), now),
            ),
            createdAt = now,
            updatedAt = now,
            lastUsedAt = now,
            totp = TotpConfiguration(secret = secret("JBSWY3DPEHPK3PXP")),
        )
    }
}
