package com.passvault.core.otp

import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.model.TotpAlgorithm
import com.passvault.core.domain.model.TotpCode
import com.passvault.core.domain.model.TotpConfiguration
import com.passvault.core.domain.model.codePointLength
import com.passvault.core.domain.model.hasOnlySafeSingleLineCodePoints
import kotlin.text.CharacterCodingException
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
    private val enrollmentParser = TotpEnrollmentParser()

    override fun parse(input: String, manualOptions: TotpManualOptions): TotpParseResult =
        enrollmentParser.parse(input, manualOptions)

    override fun generate(configuration: TotpConfiguration, at: Instant): Result<TotpCode> = runCatching {
        requireValidConfiguration(configuration)
        require(at.epochSeconds >= 0) { "Invalid TOTP timestamp" }

        val secretCharacters = configuration.secret.expose()
        var secretBytes: ByteArray? = null
        val counterBytes = ByteArray(COUNTER_BYTES)
        var digestBytes: ByteArray? = null
        try {
            val decodedSecret = Base32Codec.decode(secretCharacters.concatToString())
                ?: throw IllegalArgumentException("Invalid TOTP configuration")
            secretBytes = decodedSecret
            require(decodedSecret.size in MIN_SECRET_BYTES..MAX_SECRET_BYTES) {
                "Invalid TOTP configuration"
            }

            val counter = at.epochSeconds / configuration.periodSeconds
            require(counter < Long.MAX_VALUE / configuration.periodSeconds) {
                "Invalid TOTP timestamp"
            }
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
            val modulus = if (configuration.digits == DEFAULT_DIGITS) {
                ONE_MILLION
            } else {
                ONE_HUNDRED_MILLION
            }
            val value = (binaryCode % modulus).toString().padStart(configuration.digits, '0')
            TotpCode(
                value = value,
                expiresAt = Instant.fromEpochSeconds((counter + 1) * configuration.periodSeconds),
            )
        } finally {
            secretCharacters.fill('\u0000')
            secretBytes?.fill(0)
            counterBytes.fill(0)
            digestBytes?.fill(0)
        }
    }

    private fun requireValidConfiguration(configuration: TotpConfiguration) {
        require(isValidOptions(configuration.algorithm, configuration.digits, configuration.periodSeconds)) {
            "Invalid TOTP configuration"
        }
        require(isValidTotpLabel(configuration.issuer) && isValidTotpLabel(configuration.accountName)) {
            "Invalid TOTP configuration"
        }
        require(configuration.secret.length in MIN_SECRET_CHARACTERS..MAX_SECRET_CHARACTERS) {
            "Invalid TOTP configuration"
        }
    }
}

private class TotpEnrollmentParser {
    fun parse(input: String, manualOptions: TotpManualOptions): TotpParseResult {
        val trimmed = input
            .takeIf { it.length <= MAX_ENROLLMENT_LENGTH }
            ?.trim()
        return when {
            trimmed.isNullOrEmpty() -> {
                TotpParseResult.Error(TotpParseError.INVALID_INPUT)
            }
            trimmed.startsWith(OTP_AUTH_PREFIX, ignoreCase = true) -> parseUri(trimmed)
            else -> parseManualSecret(trimmed, manualOptions)
        }
    }

    private fun parseManualSecret(input: String, options: TotpManualOptions): TotpParseResult =
        parseEnrollment {
            ensureEnrollment(
                isValidOptions(options.algorithm, options.digits, options.periodSeconds),
                TotpParseError.UNSUPPORTED_CONFIGURATION,
            )
            configurationFrom(
                rawSecret = input,
                issuer = null,
                accountName = null,
                algorithm = options.algorithm,
                digits = options.digits,
                periodSeconds = options.periodSeconds,
            )
        }

    private fun parseUri(input: String): TotpParseResult = parseEnrollment {
        val parts = parseUriParts(input)
        val label = parseLabel(parts.label, parts.parameters[ISSUER_PARAMETER])
        val algorithm = parseAlgorithm(parts.parameters[ALGORITHM_PARAMETER])
        val digits = parseNumericOption(parts.parameters, DIGITS_PARAMETER, DEFAULT_DIGITS)
        val periodSeconds = parseNumericOption(parts.parameters, PERIOD_PARAMETER, DEFAULT_PERIOD_SECONDS)
        ensureEnrollment(
            isValidOptions(algorithm, digits, periodSeconds),
            TotpParseError.UNSUPPORTED_CONFIGURATION,
        )
        configurationFrom(
            rawSecret = parts.secret,
            issuer = label.issuer,
            accountName = label.accountName,
            algorithm = algorithm,
            digits = digits,
            periodSeconds = periodSeconds,
        )
    }

    private fun parseUriParts(input: String): ParsedUriParts {
        val withoutScheme = input.substring(OTP_AUTH_PREFIX.length)
        ensureEnrollment(
            withoutScheme.substringBefore('/').equals(TOTP_TYPE, ignoreCase = true),
            TotpParseError.UNSUPPORTED_TYPE,
        )
        ensureEnrollment('#' !in withoutScheme, TotpParseError.INVALID_INPUT)

        val pathAndQuery = withoutScheme.substringAfter('/', missingDelimiterValue = "")
        ensureEnrollment(pathAndQuery.isNotEmpty(), TotpParseError.INVALID_INPUT)
        val label = PercentCodec.decode(pathAndQuery.substringBefore('?'), plusAsSpace = false)
            ?.trim()
            ?.takeIf(String::isNotEmpty)
            ?: rejectEnrollment(TotpParseError.INVALID_INPUT)
        val parameters = parseQuery(pathAndQuery.substringAfter('?', missingDelimiterValue = ""))
        val secret = parameters[SECRET_PARAMETER]
            ?: rejectEnrollment(TotpParseError.INVALID_SECRET)
        return ParsedUriParts(label = label, parameters = parameters, secret = secret)
    }

    private fun parseLabel(label: String, encodedIssuer: String?): ParsedLabel {
        val labelParts = label.split(':', limit = 2)
        val labelIssuer = labelParts
            .takeIf { it.size == 2 }
            ?.first()
            ?.trim()
            ?.takeIf(String::isNotEmpty)
        val accountName = labelParts.last().trim().takeIf(String::isNotEmpty)
            ?: rejectEnrollment(TotpParseError.INVALID_INPUT)
        val queryIssuer = encodedIssuer?.trim()?.takeIf(String::isNotEmpty)
        ensureEnrollment(
            labelIssuer == null || queryIssuer == null || labelIssuer == queryIssuer,
            TotpParseError.INVALID_INPUT,
        )
        val issuer = queryIssuer ?: labelIssuer
        ensureEnrollment(
            isValidTotpLabel(issuer) && isValidTotpLabel(accountName),
            TotpParseError.INVALID_INPUT,
        )
        return ParsedLabel(issuer = issuer, accountName = accountName)
    }

    private fun parseAlgorithm(value: String?): TotpAlgorithm =
        when (value?.uppercase()?.replace("-", "")) {
            null, "SHA1" -> TotpAlgorithm.SHA1
            "SHA256" -> TotpAlgorithm.SHA256
            "SHA512" -> TotpAlgorithm.SHA512
            else -> rejectEnrollment(TotpParseError.UNSUPPORTED_CONFIGURATION)
        }

    private fun parseNumericOption(
        parameters: Map<String, String>,
        name: String,
        defaultValue: Int,
    ): Int = parameters[name]?.toIntOrNull()
        ?: if (name in parameters) {
            rejectEnrollment(TotpParseError.UNSUPPORTED_CONFIGURATION)
        } else {
            defaultValue
        }

    private fun configurationFrom(
        rawSecret: String,
        issuer: String?,
        accountName: String?,
        algorithm: TotpAlgorithm,
        digits: Int,
        periodSeconds: Int,
    ): TotpConfiguration {
        val normalizedSecret = Base32Codec.normalize(rawSecret)
            ?: rejectEnrollment(TotpParseError.INVALID_SECRET)
        val decoded = Base32Codec.decode(normalizedSecret)
            ?: rejectEnrollment(TotpParseError.INVALID_SECRET)
        return try {
            ensureEnrollment(
                decoded.size in MIN_SECRET_BYTES..MAX_SECRET_BYTES,
                TotpParseError.INVALID_SECRET,
            )
            TotpConfiguration(
                secret = SensitiveText.from(normalizedSecret),
                issuer = issuer,
                accountName = accountName,
                algorithm = algorithm,
                digits = digits,
                periodSeconds = periodSeconds,
            )
        } finally {
            decoded.fill(0)
        }
    }

    private fun parseQuery(query: String): Map<String, String> {
        if (query.isEmpty()) return emptyMap()
        val result = mutableMapOf<String, String>()
        query.split('&').forEach { item ->
            ensureEnrollment(item.isNotEmpty(), TotpParseError.INVALID_INPUT)
            val encodedName = item.substringBefore('=')
            val encodedValue = item.substringAfter('=', missingDelimiterValue = "")
            val name = PercentCodec.decode(encodedName, plusAsSpace = true)?.lowercase()
                ?: rejectEnrollment(TotpParseError.INVALID_INPUT)
            val value = PercentCodec.decode(encodedValue, plusAsSpace = true)
                ?: rejectEnrollment(TotpParseError.INVALID_INPUT)
            val previous = result.put(name, value)
            ensureEnrollment(
                name !in RECOGNIZED_PARAMETERS || previous == null,
                TotpParseError.INVALID_INPUT,
            )
        }
        return result
    }
}

private object Base32Codec {
    fun normalize(input: String): String? {
        val withoutWhitespace = buildString(input.length) {
            input.forEach { character ->
                when {
                    character.isWhitespace() -> Unit
                    character in 'a'..'z' -> append(character.uppercaseChar())
                    else -> append(character)
                }
            }
        }
        val firstPadding = withoutWhitespace.indexOf('=')
        val normalized = if (firstPadding >= 0) {
            withoutWhitespace.substring(0, firstPadding)
        } else {
            withoutWhitespace
        }
        val hasOnlyTrailingPadding = firstPadding < 0 ||
            withoutWhitespace.drop(firstPadding).all { it == '=' }
        val hasCanonicalPadding = firstPadding < 0 || run {
            val paddingLength = withoutWhitespace.length - firstPadding
            val expectedPadding = (BASE32_BLOCK_CHARACTERS - normalized.length % BASE32_BLOCK_CHARACTERS) %
                BASE32_BLOCK_CHARACTERS
            paddingLength == expectedPadding && withoutWhitespace.length % BASE32_BLOCK_CHARACTERS == 0
        }
        return normalized.takeIf { value ->
            value.isNotEmpty() &&
                value.length % BASE32_BLOCK_CHARACTERS in VALID_BASE32_REMAINDERS &&
                value.all { it in BASE32_ALPHABET } &&
                hasOnlyTrailingPadding &&
                hasCanonicalPadding
        }
    }

    fun decode(input: String): ByteArray? {
        val normalized = normalize(input) ?: return null
        val output = ByteArray((normalized.length * BASE32_BITS_PER_CHARACTER) / BITS_PER_BYTE)
        var outputIndex = 0
        var buffer = 0
        var bitsInBuffer = 0
        normalized.forEach { character ->
            buffer = (buffer shl BASE32_BITS_PER_CHARACTER) or BASE32_ALPHABET.indexOf(character)
            bitsInBuffer += BASE32_BITS_PER_CHARACTER
            while (bitsInBuffer >= BITS_PER_BYTE) {
                bitsInBuffer -= BITS_PER_BYTE
                output[outputIndex++] = (buffer shr bitsInBuffer).toByte()
                buffer = if (bitsInBuffer == 0) 0 else buffer and ((1 shl bitsInBuffer) - 1)
            }
        }
        return if (bitsInBuffer == 0 || buffer == 0) {
            output
        } else {
            output.fill(0)
            null
        }
    }
}

private object PercentCodec {
    fun decode(input: String, plusAsSpace: Boolean): String? {
        val result = StringBuilder(input.length)
        var index = 0
        while (index < input.length) {
            val nextPercent = input.indexOf('%', startIndex = index)
                .let { found -> if (found < 0) input.length else found }
            result.appendPlainText(input.substring(index, nextPercent), plusAsSpace)
            if (nextPercent == input.length) break

            val decodedRun = decodePercentRun(input, nextPercent) ?: return null
            result.append(decodedRun.text)
            index = decodedRun.nextIndex
        }
        return result.toString()
    }

    private fun StringBuilder.appendPlainText(value: String, plusAsSpace: Boolean) {
        append(if (plusAsSpace) value.replace('+', ' ') else value)
    }

    private fun decodePercentRun(input: String, startIndex: Int): DecodedPercentRun? {
        val endIndex = percentRunEnd(input, startIndex) ?: return null
        val bytes = ByteArray((endIndex - startIndex) / PERCENT_ENCODED_LENGTH)
        return try {
            val isValid = bytes.indices.all { byteIndex ->
                val encodedIndex = startIndex + byteIndex * PERCENT_ENCODED_LENGTH
                val decodedByte = decodeHexByte(input, encodedIndex)
                if (decodedByte != null) bytes[byteIndex] = decodedByte
                decodedByte != null
            }
            if (isValid) {
                DecodedPercentRun(
                    text = bytes.decodeToString(throwOnInvalidSequence = true),
                    nextIndex = endIndex,
                )
            } else {
                null
            }
        } catch (_: CharacterCodingException) {
            null
        } finally {
            bytes.fill(0)
        }
    }

    private fun percentRunEnd(input: String, startIndex: Int): Int? {
        var index = startIndex
        while (index < input.length && input[index] == '%') {
            if (index + PERCENT_ENCODED_LENGTH > input.length) return null
            index += PERCENT_ENCODED_LENGTH
        }
        return index
    }

    private fun decodeHexByte(input: String, encodedIndex: Int): Byte? {
        val high = input[encodedIndex + 1].digitToIntOrNull(HEX_RADIX)
        val low = input[encodedIndex + 2].digitToIntOrNull(HEX_RADIX)
        return if (high != null && low != null) ((high shl 4) or low).toByte() else null
    }
}

private data class DecodedPercentRun(
    val text: String,
    val nextIndex: Int,
)

private inline fun parseEnrollment(block: () -> TotpConfiguration): TotpParseResult =
    try {
        TotpParseResult.Success(block())
    } catch (failure: TotpEnrollmentException) {
        TotpParseResult.Error(failure.reason)
    }

private fun ensureEnrollment(condition: Boolean, reason: TotpParseError) {
    if (!condition) rejectEnrollment(reason)
}

private fun rejectEnrollment(reason: TotpParseError): Nothing = throw TotpEnrollmentException(reason)

private fun isValidOptions(algorithm: TotpAlgorithm, digits: Int, periodSeconds: Int): Boolean =
    algorithm in TotpAlgorithm.entries &&
        digits in SUPPORTED_DIGITS &&
        periodSeconds in MIN_PERIOD_SECONDS..MAX_PERIOD_SECONDS

private fun isValidTotpLabel(value: String?): Boolean =
    value == null ||
        (
            value.isNotBlank() &&
                value.codePointLength() <= MAX_LABEL_LENGTH &&
                value.hasOnlySafeSingleLineCodePoints()
        )

private class TotpEnrollmentException(val reason: TotpParseError) : IllegalArgumentException()

private data class ParsedUriParts(
    val label: String,
    val parameters: Map<String, String>,
    val secret: String,
)

private data class ParsedLabel(
    val issuer: String?,
    val accountName: String,
)

const val DEFAULT_DIGITS = 6
const val DEFAULT_PERIOD_SECONDS = 30

private const val OTP_AUTH_PREFIX = "otpauth://"
private const val TOTP_TYPE = "totp"
private const val SECRET_PARAMETER = "secret"
private const val ISSUER_PARAMETER = "issuer"
private const val ALGORITHM_PARAMETER = "algorithm"
private const val DIGITS_PARAMETER = "digits"
private const val PERIOD_PARAMETER = "period"
private const val MAX_ENROLLMENT_LENGTH = 8 * 1024
private const val MAX_LABEL_LENGTH = 200
private const val MIN_SECRET_BYTES = 10
private const val MAX_SECRET_BYTES = 128
private const val MIN_SECRET_CHARACTERS = 16
private const val MAX_SECRET_CHARACTERS = 205
private const val MIN_PERIOD_SECONDS = 5
private const val MAX_PERIOD_SECONDS = 300
private const val COUNTER_BYTES = 8
private const val BITS_PER_BYTE = 8
private const val BASE32_BITS_PER_CHARACTER = 5
private const val BASE32_BLOCK_CHARACTERS = 8
private const val PERCENT_ENCODED_LENGTH = 3
private const val HEX_RADIX = 16
private const val BYTE_MASK = 0xffL
private const val BYTE_MASK_INT = 0xff
private const val SIGN_BIT_MASK = 0x7f
private const val DYNAMIC_TRUNCATION_MASK = 0x0f
private const val DYNAMIC_TRUNCATION_BYTES = 4
private const val ONE_MILLION = 1_000_000
private const val ONE_HUNDRED_MILLION = 100_000_000
private const val BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
private val SUPPORTED_DIGITS = setOf(DEFAULT_DIGITS, 8)
private val VALID_BASE32_REMAINDERS = setOf(0, 2, 4, 5, 7)
private val RECOGNIZED_PARAMETERS = setOf(
    SECRET_PARAMETER,
    ISSUER_PARAMETER,
    ALGORITHM_PARAMETER,
    DIGITS_PARAMETER,
    PERIOD_PARAMETER,
)
