package com.passvault.core.domain.repository

import com.passvault.core.domain.model.CredentialId
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.model.TotpConfiguration
import kotlinx.coroutines.Job
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CredentialTotpInputLeaseTest {
    @Test
    fun `owner completion before transfer wipes inputs`() {
        val input = totpInput(TEST_SECRET)
        val owner = Job()
        val lease = CredentialTotpInputLease.ownedByCoroutine(listOf(input), owner)

        owner.cancel()

        assertNull(lease.take())
        assertTrue(input.configuration.secret.toStringUnsafe().all { it == '\u0000' })
    }

    @Test
    fun `atomic take transfers ownership exactly once`() {
        val input = totpInput(TEST_SECRET)
        val owner = Job()
        val lease = CredentialTotpInputLease.ownedByCoroutine(listOf(input), owner)

        val transferred = assertNotNull(lease.take())
        try {
            assertNull(lease.take())
            owner.cancel()
            assertEquals(TEST_SECRET, input.configuration.secret.toStringUnsafe())
        } finally {
            transferred.forEach(CredentialTotpInput::clear)
        }
        assertTrue(input.configuration.secret.toStringUnsafe().all { it == '\u0000' })
    }

    private fun totpInput(secret: String): CredentialTotpInput = CredentialTotpInput(
        id = CredentialId("credential"),
        title = "Example",
        displayUsername = "alice@example.com",
        configuration = TotpConfiguration(secret = SensitiveText.from(secret)),
    )

    private companion object {
        const val TEST_SECRET = "JBSWY3DPEHPK3PXP"
    }
}
