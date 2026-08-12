package com.passvault.core.crypto

import com.passvault.core.testing.fakes.FakeCryptoEngine
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SecurePasswordGeneratorTest {
    @Test
    fun `default generation produces requested length and every enabled character class`() = runTest {
        val generator = SecurePasswordGenerator(FakeCryptoEngine())

        val password = generator.generate().getOrThrow()

        assertEquals(PasswordGenerationOptions.DEFAULT_PASSWORD_LENGTH, password.length)
        assertTrue(password.any(Char::isLowerCase))
        assertTrue(password.any(Char::isUpperCase))
        assertTrue(password.any(Char::isDigit))
        assertTrue(password.any { !it.isLetterOrDigit() })
    }

    @Test
    fun `generation rejects an empty character selection`() = runTest {
        val generator = SecurePasswordGenerator(FakeCryptoEngine())

        val result = generator.generate(
            PasswordGenerationOptions(
                includeUppercase = false,
                includeLowercase = false,
                includeNumbers = false,
                includeSymbols = false,
            ),
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `ambiguous characters are excluded when requested`() = runTest {
        val generator = SecurePasswordGenerator(FakeCryptoEngine())

        val password = generator.generate(
            PasswordGenerationOptions(length = 128, excludeAmbiguous = true),
        ).getOrThrow()

        assertFalse(password.any { it in "0O1lI" })
    }

    @Test
    fun `random source failure does not return partial output`() = runTest {
        val cryptoEngine = FakeCryptoEngine().apply { setShouldFail() }
        val generator = SecurePasswordGenerator(cryptoEngine)

        assertTrue(generator.generate().isFailure)
    }

    @Test
    fun `random source cancellation remains coroutine cancellation`() = runTest {
        val cryptoEngine = FakeCryptoEngine().apply {
            setShouldFail(CancellationException("cancel generation"))
        }
        val generator = SecurePasswordGenerator(cryptoEngine)

        assertFailsWith<CancellationException> { generator.generate() }
    }
}
