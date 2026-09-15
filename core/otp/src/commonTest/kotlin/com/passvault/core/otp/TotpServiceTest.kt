package com.passvault.core.otp

import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.model.TotpAlgorithm
import com.passvault.core.domain.model.TotpConfiguration
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
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
    fun `TOTP label limit counts complete supplementary characters`() {
        val accepted = service.parse(
            "otpauth://totp/${"🔐".repeat(200)}?secret=JBSWY3DPEHPK3PXP",
        )
        val acceptedConfiguration = assertIs<TotpParseResult.Success>(accepted).configuration
        try {
            assertEquals("🔐".repeat(200), acceptedConfiguration.accountName)
        } finally {
            acceptedConfiguration.clear()
        }

        assertIs<TotpParseResult.Error>(
            service.parse("otpauth://totp/${"🔐".repeat(201)}?secret=JBSWY3DPEHPK3PXP"),
        )
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
    fun `enrollment preserves the published 80 bit authenticator interoperability boundary`() {
        val publishedSecret = "JBSWY3DPEHPK3PXP"
        val inputs = listOf(
            publishedSecret,
            "otpauth://totp/Example:ada?secret=$publishedSecret&issuer=Example",
        )

        inputs.forEach { input ->
            val configuration = assertIs<TotpParseResult.Success>(service.parse(input)).configuration
            try {
                assertEquals(publishedSecret, configuration.secret.toStringUnsafe())
            } finally {
                configuration.clear()
            }
        }
    }

    @Test
    fun `enrollment boundaries reject sub 80 bit keys while retaining compatible and RFC sized keys`() {
        val expectedAcceptance = mapOf(
            15 to false,
            16 to true,
            24 to true,
            // 25 characters cannot be a canonical whole-byte Base32 value;
            // 26 is the first canonical representation of a 16-byte key.
            25 to false,
            26 to true,
        )

        expectedAcceptance.forEach { (characters, accepted) ->
            val secret = "A".repeat(characters)
            listOf(secret, "otpauth://totp/Example:ada?secret=$secret").forEach { input ->
                when (val result = service.parse(input)) {
                    is TotpParseResult.Success -> {
                        try {
                            assertTrue(accepted, "$characters Base32 characters should have been rejected")
                        } finally {
                            result.configuration.clear()
                        }
                    }
                    is TotpParseResult.Error -> {
                        assertTrue(!accepted, "$characters Base32 characters should have been accepted")
                        assertEquals(TotpParseError.INVALID_SECRET, result.reason)
                    }
                }
            }
        }
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
        assertEquals(
            TotpParseError.UNSUPPORTED_CONFIGURATION,
            assertIs<TotpParseResult.Error>(
                service.parse("otpauth://totp/Example:ada?secret=JBSWY3DPEHPK3PXP&digits=six"),
            ).reason,
        )
        assertEquals(
            TotpParseError.UNSUPPORTED_CONFIGURATION,
            assertIs<TotpParseResult.Error>(
                service.parse("otpauth://totp/Example:ada?secret=JBSWY3DPEHPK3PXP&period=thirty"),
            ).reason,
        )
        assertIs<TotpParseResult.Error>(
            service.parse("otpauth://totp/Example%00:ada?secret=JBSWY3DPEHPK3PXP"),
        )
        assertIs<TotpParseResult.Error>(
            service.parse("otpauth://totp/Example:ada?secret=AAAAAAAAAAAAAAAAAB"),
        )
        assertIs<TotpParseResult.Error>(service.parse("ß".repeat(8)))
        assertIs<TotpParseResult.Error>(
            service.parse("otpauth://totp/Example%4:ada?secret=JBSWY3DPEHPK3PXP"),
        )
        assertIs<TotpParseResult.Error>(
            service.parse("otpauth://totp/Example%E2%80%AE:ada?secret=JBSWY3DPEHPK3PXP"),
        )
        assertIs<TotpParseResult.Error>(
            service.parse("otpauth://totp/Example%FF:ada?secret=JBSWY3DPEHPK3PXP"),
        )
        assertIs<TotpParseResult.Error>(service.parse(" ".repeat(8 * 1024 + 1)))
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

    @Test
    fun `generation rejects negative timestamps and keeps the enrolled secret owned by the caller`() {
        val configuration = TotpConfiguration(
            secret = SensitiveText.from("JBSWY3DPEHPK3PXP"),
        )

        val result = service.generate(configuration, Instant.fromEpochSeconds(-1))

        assertTrue(result.isFailure)
        assertEquals("JBSWY3DPEHPK3PXP", configuration.secret.toStringUnsafe())
        configuration.clear()
    }

    @Test
    fun `generation rejects an oversized secret before decoding`() {
        val configuration = TotpConfiguration(secret = SensitiveText.from("A".repeat(208)))

        try {
            assertTrue(service.generate(configuration, Instant.fromEpochSeconds(0)).isFailure)
        } finally {
            configuration.clear()
        }
    }

    @Test
    fun `mutable Base32 decoder preserves supported normalization and padding`() {
        val cases = listOf(
            "MY======" to "f",
            "MZXQ====" to "fo",
            "MZXW6===" to "foo",
            "MZXW6YQ=" to "foob",
            "MZXW6YTB" to "fooba",
            " mzxw 6ytb oi====== " to "foobar",
        )

        cases.forEach { (encoded, expected) ->
            val input = encoded.toCharArray()
            val decoded = requireNotNull(Base32Codec.decode(input))
            try {
                assertContentEquals(expected.encodeToByteArray(), decoded)
            } finally {
                input.fill('\u0000')
                decoded.fill(0)
            }
        }

        listOf("A", "AB", "M=Y=====", "MY=====", "MY======A").forEach { encoded ->
            val input = encoded.toCharArray()
            try {
                assertNull(Base32Codec.decode(input))
            } finally {
                input.fill('\u0000')
            }
        }
    }

    @Test
    fun `platform byte HMAC matches long-key RFC vectors`() {
        val data = "Test Using Larger Than Block-Size Key - Hash Key First".encodeToByteArray()
        val cases = listOf(
            HmacVector(
                algorithm = TotpAlgorithm.SHA1,
                keySize = 80,
                expectedHex = "aa4ae5e15272d00e95705637ce8a3b55ed402112",
            ),
            HmacVector(
                algorithm = TotpAlgorithm.SHA256,
                keySize = 131,
                expectedHex = "60e431591ee0b67f0d8a26aacbf5b77f8e0bc6213728c5140546040f0ee37f54",
            ),
            HmacVector(
                algorithm = TotpAlgorithm.SHA512,
                keySize = 131,
                expectedHex =
                    "80b24263c7c1a3ebb71493c1dd7be8b49b46d1f41b4aeec1121b013783f8f352" +
                        "6b56d037e05f2598bd0fd2215d6a1e5295e64f73f63f0aec8b915a985d786598",
            ),
        )

        try {
            cases.forEach { vector ->
                val key = ByteArray(vector.keySize) { 0xaa.toByte() }
                val digest = calculateTotpHmac(vector.algorithm, key, data)
                val expected = vector.expectedHex.hexBytes()
                try {
                    assertContentEquals(expected, digest)
                } finally {
                    key.fill(0)
                    digest.fill(0)
                    expected.fill(0)
                }
            }
        } finally {
            data.fill(0)
        }
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

    private data class HmacVector(
        val algorithm: TotpAlgorithm,
        val keySize: Int,
        val expectedHex: String,
    )
}

private fun String.hexBytes(): ByteArray = ByteArray(length / 2) { index ->
    substring(index * 2, index * 2 + 2).toInt(16).toByte()
}
