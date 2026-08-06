package com.passvault.core.otp

import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.model.TotpAlgorithm
import com.passvault.core.domain.model.TotpConfiguration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Instant

class TotpServiceTest {
    private val service = StandardTotpService()

    @Test
    fun `generates RFC 6238 vectors for every supported algorithm`() {
        val cases = listOf(
            Vector(59, "94287082", "46119246", "90693936"),
            Vector(1_111_111_109, "07081804", "68084774", "25091201"),
            Vector(1_111_111_111, "14050471", "67062674", "99943326"),
            Vector(1_234_567_890, "89005924", "91819424", "93441116"),
            Vector(2_000_000_000, "69279037", "90698825", "38618901"),
            Vector(20_000_000_000, "65353130", "77737706", "47863826"),
        )
        val secrets = mapOf(
            TotpAlgorithm.SHA1 to "12345678901234567890",
            TotpAlgorithm.SHA256 to "12345678901234567890123456789012",
            TotpAlgorithm.SHA512 to "1234567890123456789012345678901234567890123456789012345678901234",
        )

        cases.forEach { vector ->
            secrets.forEach { (algorithm, rawSecret) ->
                val expected = when (algorithm) {
                    TotpAlgorithm.SHA1 -> vector.sha1
                    TotpAlgorithm.SHA256 -> vector.sha256
                    TotpAlgorithm.SHA512 -> vector.sha512
                }
                val configuration = TotpConfiguration(
                    secret = SensitiveText.from(base32Encode(rawSecret.encodeToByteArray())),
                    algorithm = algorithm,
                    digits = 8,
                    periodSeconds = 30,
                )
                assertEquals(
                    expected,
                    service.generate(configuration, Instant.fromEpochSeconds(vector.epochSeconds)).getOrThrow().value,
                )
                configuration.clear()
            }
        }
    }

    @Test
    fun `parses otpauth URI and applies encoded metadata`() {
        val result = service.parse(
            "otpauth://totp/Example%20Co:ada%40example.com" +
                "?secret=JBSWY3DPEHPK3PXP&issuer=Example%20Co&algorithm=SHA256&digits=8&period=60",
        )

        val configuration = assertIs<TotpParseResult.Success>(result).configuration
        assertEquals("Example Co", configuration.issuer)
        assertEquals("ada@example.com", configuration.accountName)
        assertEquals(TotpAlgorithm.SHA256, configuration.algorithm)
        assertEquals(8, configuration.digits)
        assertEquals(60, configuration.periodSeconds)
        configuration.clear()
    }

    @Test
    fun `manual secret is normalized and uses selected options`() {
        val result = service.parse(
            "jbsw y3dp ehpk 3pxp",
            TotpManualOptions(TotpAlgorithm.SHA512, digits = 8, periodSeconds = 45),
        )

        val configuration = assertIs<TotpParseResult.Success>(result).configuration
        assertEquals("JBSWY3DPEHPK3PXP", configuration.secret.toStringUnsafe())
        assertEquals(TotpAlgorithm.SHA512, configuration.algorithm)
        assertEquals(8, configuration.digits)
        assertEquals(45, configuration.periodSeconds)
        configuration.clear()
    }

    @Test
    fun `accepts canonical padding and rejects noncanonical Base32 lengths`() {
        val unpadded = base32Encode("12345678901".encodeToByteArray())
        val padded = unpadded.padEnd((unpadded.length + 7) / 8 * 8, '=')

        val result = assertIs<TotpParseResult.Success>(service.parse(padded))
        assertEquals(unpadded, result.configuration.secret.toStringUnsafe())
        result.configuration.clear()
        assertIs<TotpParseResult.Error>(service.parse("AAAAAAAAAAAAAAAAA"))
        assertIs<TotpParseResult.Error>(service.parse("$unpadded="))
    }

    @Test
    fun `rejects unsupported or ambiguous enrollment input`() {
        assertIs<TotpParseResult.Error>(
            service.parse("otpauth://hotp/Example:ada?secret=JBSWY3DPEHPK3PXP&counter=1"),
        )
        assertIs<TotpParseResult.Error>(
            service.parse("otpauth://totp/One:ada?secret=JBSWY3DPEHPK3PXP&issuer=Two"),
        )
        assertIs<TotpParseResult.Error>(
            service.parse("otpauth://totp/Example:ada?secret=abc!"),
        )
        assertIs<TotpParseResult.Error>(
            service.parse("otpauth://totp/Example:ada?secret=JBSWY3DPEHPK3PXP&digits=7"),
        )
        assertIs<TotpParseResult.Error>(
            service.parse("otpauth://totp/Example:ada?secret=JBSWY3DPEHPK3PXP&secret=AAAAAAAAAAAAAAAA"),
        )
    }

    @Test
    fun `reports the next exact expiry boundary`() {
        val configuration = TotpConfiguration(
            secret = SensitiveText.from("JBSWY3DPEHPK3PXP"),
            periodSeconds = 30,
        )

        val code = service.generate(configuration, Instant.fromEpochSeconds(59)).getOrThrow()

        assertEquals(Instant.fromEpochSeconds(60), code.expiresAt)
        assertTrue(code.value.length == 6)
        configuration.clear()
    }

    private fun base32Encode(bytes: ByteArray): String {
        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        val result = StringBuilder()
        var buffer = 0
        var bitsInBuffer = 0
        bytes.forEach { byte ->
            buffer = (buffer shl 8) or (byte.toInt() and 0xff)
            bitsInBuffer += 8
            while (bitsInBuffer >= 5) {
                bitsInBuffer -= 5
                result.append(alphabet[(buffer shr bitsInBuffer) and 0x1f])
                buffer = if (bitsInBuffer == 0) 0 else buffer and ((1 shl bitsInBuffer) - 1)
            }
        }
        if (bitsInBuffer > 0) result.append(alphabet[(buffer shl (5 - bitsInBuffer)) and 0x1f])
        return result.toString()
    }

    private data class Vector(
        val epochSeconds: Long,
        val sha1: String,
        val sha256: String,
        val sha512: String,
    )
}
