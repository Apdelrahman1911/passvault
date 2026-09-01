package com.passvault.feature.health.presentation

import com.passvault.core.domain.model.CredentialId
import com.passvault.core.domain.model.CredentialType
import com.passvault.core.domain.model.PasswordHealth
import com.passvault.core.domain.model.PasswordScore
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.repository.CredentialHealthInput
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class PasswordHealthAnalyzerTest {

    @Test
    fun `predictable password is scored through the buffer-native evaluator`() {
        val credential = healthInput("predictable", "Summer2024!!")

        try {
            val analysis = buildHealthAnalysis(listOf(credential), TEST_NOW)

            assertEquals(PasswordScore.VERY_WEAK, analysis.healthByCredential.getValue(credential.id).score)
            assertEquals(listOf(credential.id), analysis.weakPasswords.map { it.credentialId })
        } finally {
            credential.password?.clear()
        }
    }

    @Test
    fun `duplicate scan groups equal passwords without a plaintext hash`() {
        val credentials = listOf(
            healthInput("different-a", "Fir!River-4812"),
            healthInput("duplicate-c", "Shared!Secret-9274"),
            healthInput("different-b", "Sun!Harbor-6831"),
            healthInput("duplicate-a", "Shared!Secret-9274"),
            healthInput("duplicate-b", "Shared!Secret-9274"),
        )

        try {
            val analysis = buildHealthAnalysis(credentials, TEST_NOW)

            assertEquals(1, analysis.duplicatePasswords.size)
            assertEquals(
                setOf("duplicate-a", "duplicate-b", "duplicate-c"),
                analysis.duplicatePasswords.single().credentials
                    .map { it.credentialId.value }
                    .toSet(),
            )
        } finally {
            credentials.forEach { it.password?.clear() }
        }
    }
}

private fun healthInput(id: String, password: String): CredentialHealthInput = CredentialHealthInput(
    id = CredentialId(id),
    type = CredentialType.Login,
    title = id,
    username = null,
    email = null,
    password = SensitiveText.from(password),
    isFavorite = false,
    folderId = null,
    tagIds = emptySet(),
    createdAt = TEST_NOW,
    updatedAt = TEST_NOW,
    lastUsedAt = null,
    passwordHealth = PasswordHealth.UNKNOWN,
    passwordChangedAt = TEST_NOW,
)

private val TEST_NOW = Instant.fromEpochSeconds(1_800_000_000L)
