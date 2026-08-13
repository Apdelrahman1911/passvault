package com.passvault.core.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ExternalNavigationSource {
    URL,
    NOTIFICATION,
    PLATFORM_INTENT,
}

data class RawExternalNavigationInput(
    val deliveryId: String,
    val source: ExternalNavigationSource,
    /** Decoded path segments. Platform adapters must not pass a scheme, host, query, or fragment. */
    val pathSegments: List<String>,
)

sealed interface ExternalNavigationIntent {
    data class Credential(val credentialId: String) : ExternalNavigationIntent
    data object Generator : ExternalNavigationIntent
    data object Health : ExternalNavigationIntent
    data object TwoFactorCodes : ExternalNavigationIntent
    data object Settings : ExternalNavigationIntent
    data object SecuritySettings : ExternalNavigationIntent
    data object AppearanceSettings : ExternalNavigationIntent
    data object DataSettings : ExternalNavigationIntent
    data object Backup : ExternalNavigationIntent
    data object Import : ExternalNavigationIntent
    data object Export : ExternalNavigationIntent
}

data class ExternalNavigationEnvelope(
    val deliveryId: String,
    val source: ExternalNavigationSource,
    val intent: ExternalNavigationIntent,
)

enum class ExternalNavigationParseError {
    InvalidDeliveryId,
    EmptyPath,
    MalformedSegment,
    UnsupportedDestination,
    InvalidIdentifier,
}

sealed interface ExternalNavigationParseResult {
    data class Accepted(val envelope: ExternalNavigationEnvelope) : ExternalNavigationParseResult
    data class Rejected(val error: ExternalNavigationParseError) : ExternalNavigationParseResult
}

/** Strict internal parser. No platform URL scheme or associated domain is registered by this type. */
object ExternalNavigationParser {
    fun parse(input: RawExternalNavigationInput): ExternalNavigationParseResult {
        val error = input.validationError()
        return if (error != null) {
            ExternalNavigationParseResult.Rejected(error)
        } else {
            resolveIntent(input)
        }
    }

    private fun resolveIntent(input: RawExternalNavigationInput): ExternalNavigationParseResult {
        val intent = STATIC_DESTINATIONS[input.pathSegments] ?: parseCredential(input.pathSegments)
        return if (intent != null) {
            ExternalNavigationParseResult.Accepted(
                ExternalNavigationEnvelope(input.deliveryId, input.source, intent),
            )
        } else {
            ExternalNavigationParseResult.Rejected(
                if (input.pathSegments.firstOrNull() == "credential") {
                    ExternalNavigationParseError.InvalidIdentifier
                } else {
                    ExternalNavigationParseError.UnsupportedDestination
                },
            )
        }
    }

    private fun parseCredential(segments: List<String>): ExternalNavigationIntent.Credential? =
        segments.takeIf { it.size == 2 && it[0] == "credential" }
            ?.get(1)
            ?.takeIf(String::isCanonicalUuid)
            ?.let { id -> ExternalNavigationIntent.Credential(id.lowercase()) }

    private fun RawExternalNavigationInput.validationError(): ExternalNavigationParseError? = when {
        !deliveryId.isSafeToken(MAX_DELIVERY_ID_LENGTH) -> ExternalNavigationParseError.InvalidDeliveryId
        pathSegments.isEmpty() -> ExternalNavigationParseError.EmptyPath
        pathSegments.any { !it.isSafePathSegment() } -> ExternalNavigationParseError.MalformedSegment
        else -> null
    }

    private const val MAX_DELIVERY_ID_LENGTH = 128

    private val STATIC_DESTINATIONS = mapOf(
        listOf("generator") to ExternalNavigationIntent.Generator,
        listOf("health") to ExternalNavigationIntent.Health,
        listOf("two-factor") to ExternalNavigationIntent.TwoFactorCodes,
        listOf("settings") to ExternalNavigationIntent.Settings,
        listOf("settings", "security") to ExternalNavigationIntent.SecuritySettings,
        listOf("settings", "appearance") to ExternalNavigationIntent.AppearanceSettings,
        listOf("settings", "data") to ExternalNavigationIntent.DataSettings,
        listOf("backup") to ExternalNavigationIntent.Backup,
        listOf("backup", "import") to ExternalNavigationIntent.Import,
        listOf("backup", "export") to ExternalNavigationIntent.Export,
    )
}

/**
 * Cold-start-safe, conflated external input bridge.
 *
 * There is exactly one pending item and therefore no replay backlog. A unique delivery ID allows
 * two logically identical destinations to be submitted intentionally.
 */
class ExternalNavigationDispatcher {
    private val _pending = MutableStateFlow<RawExternalNavigationInput?>(null)
    val pending: StateFlow<RawExternalNavigationInput?> = _pending.asStateFlow()

    fun submit(input: RawExternalNavigationInput) {
        _pending.value = input
    }

    fun consume(deliveryId: String) {
        val current = _pending.value ?: return
        if (current.deliveryId == deliveryId) _pending.compareAndSet(current, null)
    }
}

private fun String.isSafePathSegment(): Boolean =
    isNotEmpty() &&
        length <= MAX_EXTERNAL_SEGMENT_LENGTH &&
        none { character -> character.isISOControl() || character == '/' || character == '\\' || character == '%' }

private fun String.isSafeToken(maxLength: Int): Boolean =
    isNotEmpty() && length <= maxLength && all { character ->
        character.isLetterOrDigit() || character == '-' || character == '_' || character == '.' || character == ':'
    }

private fun String.isCanonicalUuid(): Boolean {
    if (length != UUID_TEXT_LENGTH) return false
    return indices.all { index ->
        when (index) {
            8, 13, 18, 23 -> this[index] == '-'
            else -> this[index].isHexDigit()
        }
    }
}

private fun Char.isHexDigit(): Boolean = this in '0'..'9' || this in 'a'..'f' || this in 'A'..'F'

private const val UUID_TEXT_LENGTH = 36
private const val MAX_EXTERNAL_SEGMENT_LENGTH = 256
