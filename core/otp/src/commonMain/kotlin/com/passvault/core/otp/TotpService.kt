package com.passvault.core.otp

import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.model.TotpAlgorithm
import com.passvault.core.domain.model.TotpCode
import com.passvault.core.domain.model.TotpConfiguration
import kotlin.time.Instant
import okio.ByteString.Companion.toByteString

interface TotpService {
    fun parse(
        input: String,
        manualOptions: TotpManualOptions = TotpManualOptions(),
    ): TotpParseResult

    fun generate(configuration: TotpConfiguration, at: Instant): Result<TotpCode>
}

data class TotpManualOptions(
    val algorithm: TotpAlgorithm = TotpAlgorithm.SHA1,
    val digits: Int = DEFAULT_DIGITS,
    val periodSeconds: Int = DEFAULT_PERIOD_SECONDS,
)

sealed interface TotpParseResult {
    data class Success(val configuration: TotpConfiguration) : TotpParseResult
    data class Error(val reason: TotpParseError) : TotpParseResult
}

enum class TotpParseError {
    INVALID_INPUT,
    INVALID_SECRET,
    UNSUPPORTED_TYPE,
    UNSUPPORTED_CONFIGURATION,
}

class StandardTotpService : TotpService {
    override fun parse(input: String, manualOptions: TotpManualOptions): TotpParseResult {
        val trimmed = input.trim()
        if (trimmed.isEmpty() || trimmed.length > MAX_ENROLLMENT_LENGTH) {
            return TotpParseResult.Error(TotpParseError.INVALID_INPUT)
        }
        return if (trimmed.startsWith(OTP_AUTH_PREFIX, ignoreCase = true)) {
            parseUri(trimmed)
        } else {
            parseManualSecret(trimmed, manualOptions)
        }
    }

    override fun generate(configuration: TotpConfiguration, at: Instant): Result<TotpCode> = runCatching {
        requireValidConfiguration(configuration)
        require(at.epochSeconds >= 0) { "Invalid TOTP timestamp" }

        val secretCharacters = configuration.secret.expose()
        var secretBytes: ByteArray? = null
        val counterBytes = ByteArray(COUNTER_BYTES)
        var digestBytes: ByteArray? = null
        try {
            val decodedSecret = decodeBase32(secretCharacters.concatToString())
                ?: throw IllegalArgumentException("Invalid TOTP configuration")
            secretBytes = decodedSecret
            require(decodedSecret.size in MIN_SECRET_BYTES..MAX_SECRET_BYTES) {
                "Invalid TOTP configuration"
            }

            val counter = at.epochSeconds / configuration.periodSeconds
            var remainingCounter = counter
            for (index in counterBytes.lastIndex downTo 0) {
                counterBytes[index] = (remainingCounter and BYTE_MASK).toByte()
                remainingCounter = remainingCounter ushr BITS_PER_BYTE
            }

            val message = counterBytes.toByteString()
            val key = decodedSecret.toByteString()
            val digest = when (configuration.algorithm) {
                TotpAlgorithm.SHA1 -> message.hmacSha1(key)
                TotpAlgorithm.SHA256 -> message.hmacSha256(key)
                TotpAlgorithm.SHA512 -> message.hmacSha512(key)
            }
            digestBytes = digest.toByteArray()
            val offset = digestBytes.last().toInt() and DYNAMIC_TRUNCATION_MASK
            require(offset + DYNAMIC_TRUNCATION_BYTES <= digestBytes.size) {
                "Invalid TOTP digest"
            }
            val binaryCode =
                ((digestBytes[offset].toInt() and SIGN_BIT_MASK) shl 24) or
                    ((digestBytes[offset + 1].toInt() and BYTE_MASK_INT) shl 16) or
                    ((digestBytes[offset + 2].toInt() and BYTE_MASK_INT) shl 8) or
                    (digestBytes[offset + 3].toInt() and BYTE_MASK_INT)
            val modulus = if (configuration.digits == 6) ONE_MILLION else ONE_HUNDRED_MILLION
            val value = (binaryCode % modulus).toString().padStart(configuration.digits, '0')
            val expiresAtSeconds = (counter + 1) * configuration.periodSeconds
            TotpCode(
                value = value,
                expiresAt = Instant.fromEpochSeconds(expiresAtSeconds),
            )
        } finally {
            secretCharacters.fill('\u0000')
            secretBytes?.fill(0)
            counterBytes.fill(0)
            digestBytes?.fill(0)
        }
    }

    private fun parseManualSecret(input: String, options: TotpManualOptions): TotpParseResult {
        if (!isValidOptions(options.algorithm, options.digits, options.periodSeconds)) {
            return TotpParseResult.Error(TotpParseError.UNSUPPORTED_CONFIGURATION)
        }
        return configurationFrom(
            rawSecret = input,
            issuer = null,
            accountName = null,
            algorithm = options.algorithm,
            digits = options.digits,
            periodSeconds = options.periodSeconds,
        )
    }

    private fun parseUri(input: String): TotpParseResult {
        val withoutScheme = input.substring(OTP_AUTH_PREFIX.length)
        val type = withoutScheme.substringBefore('/')
        if (!type.equals(TOTP_TYPE, ignoreCase = true)) {
            return TotpParseResult.Error(TotpParseError.UNSUPPORTED_TYPE)
        }
        if ('#' in withoutScheme) return TotpParseResult.Error(TotpParseError.INVALID_INPUT)

        val pathAndQuery = withoutScheme.substringAfter('/', missingDelimiterValue = "")
        if (pathAndQuery.isEmpty()) return TotpParseResult.Error(TotpParseError.INVALID_INPUT)
        val encodedLabel = pathAndQuery.substringBefore('?')
        val label = percentDecode(encodedLabel, plusAsSpace = false)?.trim()
            ?.takeIf(String::isNotEmpty)
            ?: return TotpParseResult.Error(TotpParseError.INVALID_INPUT)
        val query = pathAndQuery.substringAfter('?', missingDelimiterValue = "")
        val parameters = parseQuery(query) ?: return TotpParseResult.Error(TotpParseError.INVALID_INPUT)
        val rawSecret = parameters[SECRET_PARAMETER]
            ?: return TotpParseResult.Error(TotpParseError.INVALID_SECRET)

        val labelParts = label.split(':', limit = 2)
        val labelIssuer = labelParts.takeIf { it.size == 2 }?.first()?.trim()?.takeIf(String::isNotEmpty)
        val accountName = labelParts.last().trim().takeIf(String::isNotEmpty)
            ?: return TotpParseResult.Error(TotpParseError.INVALID_INPUT)
        val queryIssuer = parameters[ISSUER_PARAMETER]?.trim()?.takeIf(String::isNotEmpty)
        if (labelIssuer != null && queryIssuer != null && labelIssuer != queryIssuer) {
            return TotpParseResult.Error(TotpParseError.INVALID_INPUT)
        }
        val issuer = queryIssuer ?: labelIssuer
        if (!isValidLabel(issuer) || !isValidLabel(accountName)) {
            return TotpParseResult.Error(TotpParseError.INVALID_INPUT)
        }

        val algorithm = when (parameters[ALGORITHM_PARAMETER]?.uppercase()?.replace("-", "")) {
            null, "SHA1" -> TotpAlgorithm.SHA1
            "SHA256" -> TotpAlgorithm.SHA256
            "SHA512" -> TotpAlgorithm.SHA512
            else -> return TotpParseResult.Error(TotpParseError.UNSUPPORTED_CONFIGURATION)
        }
        val digits = parameters[DIGITS_PARAMETER]?.toIntOrNull() ?: DEFAULT_DIGITS
        val periodSeconds = parameters[PERIOD_PARAMETER]?.toIntOrNull() ?: DEFAULT_PERIOD_SECONDS
        if (!isValidOptions(algorithm, digits, periodSeconds)) {
            return TotpParseResult.Error(TotpParseError.UNSUPPORTED_CONFIGURATION)
        }

        return configurationFrom(
            rawSecret = rawSecret,
            issuer = issuer,
            accountName = accountName,
            algorithm = algorithm,
            digits = digits,
            periodSeconds = periodSeconds,
        )
    }

    private fun configurationFrom(
        rawSecret: String,
        issuer: String?,
        accountName: String?,
        algorithm: TotpAlgorithm,
        digits: Int,
        periodSeconds: Int,
    ): TotpParseResult {
        val normalizedSecret = normalizeBase32(rawSecret)
            ?: return TotpParseResult.Error(TotpParseError.INVALID_SECRET)
        val decoded = decodeBase32(normalizedSecret)
            ?: return TotpParseResult.Error(TotpParseError.INVALID_SECRET)
        return try {
            if (decoded.size !in MIN_SECRET_BYTES..MAX_SECRET_BYTES) {
                TotpParseResult.Error(TotpParseError.INVALID_SECRET)
            } else {
                TotpParseResult.Success(
                    TotpConfiguration(
                        secret = SensitiveText.from(normalizedSecret),
                        issuer = issuer,
                        accountName = accountName,
                        algorithm = algorithm,
                        digits = digits,
                        periodSeconds = periodSeconds,
                    ),
                )
            }
        } finally {
            decoded.fill(0)
        }
    }

    private fun parseQuery(query: String): Map<String, String>? {
        if (query.isEmpty()) return emptyMap()
        val result = mutableMapOf<String, String>()
        query.split('&').forEach { item ->
            if (item.isEmpty()) return null
            val encodedName = item.substringBefore('=')
            val encodedValue = item.substringAfter('=', missingDelimiterValue = "")
            val name = percentDecode(encodedName, plusAsSpace = true)?.lowercase() ?: return null
            val value = percentDecode(encodedValue, plusAsSpace = true) ?: return null
            if (name in RECOGNIZED_PARAMETERS && result.put(name, value) != null) return null
        }
        return result
    }

    private fun requireValidConfiguration(configuration: TotpConfiguration) {
        require(isValidOptions(configuration.algorithm, configuration.digits, configuration.periodSeconds)) {
            "Invalid TOTP configuration"
        }
        require(isValidLabel(configuration.issuer) && isValidLabel(configuration.accountName)) {
            "Invalid TOTP configuration"
        }
    }

    private fun isValidOptions(algorithm: TotpAlgorithm, digits: Int, periodSeconds: Int): Boolean =
        algorithm in TotpAlgorithm.entries &&
            digits in SUPPORTED_DIGITS &&
            periodSeconds in MIN_PERIOD_SECONDS..MAX_PERIOD_SECONDS

    private fun isValidLabel(value: String?): Boolean =
        value == null || (
            value.isNotBlank() &&
                value.length <= MAX_LABEL_LENGTH &&
                value.none(Char::isISOControl)
            )

    private fun normalizeBase32(input: String): String? {
        val withoutWhitespace = input.filterNot(Char::isWhitespace).uppercase()
        val firstPadding = withoutWhitespace.indexOf('=')
        if (firstPadding >= 0 && withoutWhitespace.drop(firstPadding).any { it != '=' }) return null
        val normalized = withoutWhitespace.substringBefore('=').takeIf(String::isNotEmpty) ?: return null
        if (normalized.length % BASE32_BLOCK_CHARACTERS !in VALID_BASE32_REMAINDERS) return null
        if (firstPadding >= 0) {
            val paddingLength = withoutWhitespace.length - firstPadding
            val expectedPadding = (BASE32_BLOCK_CHARACTERS - normalized.length % BASE32_BLOCK_CHARACTERS) %
                BASE32_BLOCK_CHARACTERS
            if (paddingLength != expectedPadding || withoutWhitespace.length % BASE32_BLOCK_CHARACTERS != 0) {
                return null
            }
        }
        return normalized.takeIf { value -> value.all { it in BASE32_ALPHABET } }
    }

    private fun decodeBase32(input: String): ByteArray? {
        val normalized = normalizeBase32(input) ?: return null
        val output = ByteArray((normalized.length * BASE32_BITS_PER_CHARACTER) / BITS_PER_BYTE)
        var outputIndex = 0
        var buffer = 0
        var bitsInBuffer = 0
        normalized.forEach { character ->
            val value = BASE32_ALPHABET.indexOf(character)
            if (value < 0) return null
            buffer = (buffer shl BASE32_BITS_PER_CHARACTER) or value
            bitsInBuffer += BASE32_BITS_PER_CHARACTER
            while (bitsInBuffer >= BITS_PER_BYTE) {
                bitsInBuffer -= BITS_PER_BYTE
                output[outputIndex++] = (buffer shr bitsInBuffer).toByte()
                buffer = if (bitsInBuffer == 0) 0 else buffer and ((1 shl bitsInBuffer) - 1)
            }
        }
        if (bitsInBuffer > 0 && buffer != 0) {
            output.fill(0)
            return null
        }
        return output
    }

    private fun percentDecode(input: String, plusAsSpace: Boolean): String? {
        val result = StringBuilder(input.length)
        var index = 0
        while (index < input.length) {
            if (input[index] != '%') {
                val nextPercent = input.indexOf('%', startIndex = index).let { if (it < 0) input.length else it }
                val segment = input.substring(index, nextPercent)
                result.append(if (plusAsSpace) segment.replace('+', ' ') else segment)
                index = nextPercent
                continue
            }

            val bytes = mutableListOf<Byte>()
            while (index < input.length && input[index] == '%') {
                if (index + 2 >= input.length) return null
                val high = input[index + 1].digitToIntOrNull(16) ?: return null
                val low = input[index + 2].digitToIntOrNull(16) ?: return null
                bytes += ((high shl 4) or low).toByte()
                index += 3
            }
            val decoded = try {
                bytes.toByteArray().decodeToString(throwOnInvalidSequence = true)
            } catch (_: IllegalArgumentException) {
                return null
            }
            result.append(decoded)
        }
        return result.toString()
    }

    private companion object {
        const val OTP_AUTH_PREFIX = "otpauth://"
        const val TOTP_TYPE = "totp"
        const val SECRET_PARAMETER = "secret"
        const val ISSUER_PARAMETER = "issuer"
        const val ALGORITHM_PARAMETER = "algorithm"
        const val DIGITS_PARAMETER = "digits"
        const val PERIOD_PARAMETER = "period"
        const val MAX_ENROLLMENT_LENGTH = 8 * 1024
        const val MAX_LABEL_LENGTH = 200
        const val MIN_SECRET_BYTES = 10
        const val MAX_SECRET_BYTES = 128
        const val MIN_PERIOD_SECONDS = 5
        const val MAX_PERIOD_SECONDS = 300
        const val COUNTER_BYTES = 8
        const val BITS_PER_BYTE = 8
        const val BASE32_BITS_PER_CHARACTER = 5
        const val BASE32_BLOCK_CHARACTERS = 8
        const val BYTE_MASK = 0xffL
        const val BYTE_MASK_INT = 0xff
        const val SIGN_BIT_MASK = 0x7f
        const val DYNAMIC_TRUNCATION_MASK = 0x0f
        const val DYNAMIC_TRUNCATION_BYTES = 4
        const val ONE_MILLION = 1_000_000
        const val ONE_HUNDRED_MILLION = 100_000_000
        const val BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        val SUPPORTED_DIGITS = setOf(6, 8)
        val VALID_BASE32_REMAINDERS = setOf(0, 2, 4, 5, 7)
        val RECOGNIZED_PARAMETERS = setOf(
            SECRET_PARAMETER,
            ISSUER_PARAMETER,
            ALGORITHM_PARAMETER,
            DIGITS_PARAMETER,
            PERIOD_PARAMETER,
        )
    }
}

const val DEFAULT_DIGITS = 6
const val DEFAULT_PERIOD_SECONDS = 30
