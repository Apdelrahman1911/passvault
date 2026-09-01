package com.passvault.feature.credential.presentation

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.error_totp_invalid_setup
import com.passvault.core.designsystem.text.uiText
import com.passvault.core.domain.model.TotpConfiguration
import com.passvault.core.otp.TotpManualOptions
import com.passvault.core.otp.TotpParseResult
import com.passvault.core.otp.TotpService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Clock

internal class CredentialTotpController(
    private val state: MutableStateFlow<CredentialViewModel.CredentialState>,
    private val scope: CoroutineScope,
    private val service: TotpService,
    private val clock: Clock,
) {
    private var tickerJob: Job? = null

    fun parseManualEnrollment(input: String) {
        val current = state.value
        val periodSeconds = current.totpPeriodInput.toIntOrNull()
        if (periodSeconds == null) {
            showSetupError()
            return
        }
        parseEnrollment(
            input = input,
            manualOptions = TotpManualOptions(
                algorithm = current.totpAlgorithm,
                digits = current.totpDigits,
                periodSeconds = periodSeconds,
            ),
        )
    }

    fun parseScannedEnrollment(payload: String) {
        // URI parameters are authoritative for a scanned otpauth payload. Defaults
        // only apply if a scanner deliberately supplies a manual secret instead.
        parseEnrollment(payload, TotpManualOptions())
    }

    private fun parseEnrollment(input: String, manualOptions: TotpManualOptions) {
        val result = runCatching { service.parse(input, manualOptions) }.getOrNull()
        when (result) {
            is TotpParseResult.Success -> stage(result.configuration)
            is TotpParseResult.Error,
            null, -> showSetupError()
        }
    }

    private fun showSetupError() {
        state.update {
            it.copy(
                showTotpScanner = false,
                totpSetupError = uiText(Res.string.error_totp_invalid_setup),
            )
        }
    }

    fun confirmReplacement() {
        val pending = state.value.pendingTotpConfiguration ?: return
        state.value.totpConfiguration?.clear()
        apply(pending)
    }

    fun cancelReplacement() {
        state.value.pendingTotpConfiguration?.clear()
        state.update {
            it.copy(
                pendingTotpConfiguration = null,
                showTotpReplaceConfirmation = false,
                totpSetupInput = "",
                totpSetupError = null,
            )
        }
    }

    fun remove() {
        state.value.totpConfiguration?.clear()
        state.value.pendingTotpConfiguration?.clear()
        stop()
        state.update {
            it.copy(
                totpConfiguration = null,
                pendingTotpConfiguration = null,
                currentTotpCode = "",
                totpSecondsRemaining = 0,
                totpProgress = 0f,
                totpGenerationError = false,
                showTotpRemoveConfirmation = false,
                showTotpReplaceConfirmation = false,
                isDirty = true,
            )
        }
    }

    fun start() {
        stop()
        if (state.value.totpConfiguration == null) return
        refresh()
        tickerJob = scope.launch {
            while (isActive) {
                val nowMillis = clock.now().toEpochMilliseconds()
                val delayMillis = (MILLIS_PER_SECOND - nowMillis.mod(MILLIS_PER_SECOND))
                    .coerceAtLeast(MIN_TICK_DELAY_MILLIS)
                delay(delayMillis)
                refresh()
            }
        }
    }

    fun stop() {
        tickerJob?.cancel()
        tickerJob = null
    }

    private fun stage(configuration: TotpConfiguration) {
        val current = state.value
        if (current.totpConfiguration == null) {
            apply(configuration)
            return
        }
        current.pendingTotpConfiguration?.clear()
        state.update {
            it.copy(
                pendingTotpConfiguration = configuration,
                showTotpReplaceConfirmation = true,
                showTotpScanner = false,
                totpSetupError = null,
            )
        }
    }

    private fun apply(configuration: TotpConfiguration) {
        state.update {
            it.copy(
                totpConfiguration = configuration,
                pendingTotpConfiguration = null,
                totpSetupInput = "",
                totpAlgorithm = configuration.algorithm,
                totpDigits = configuration.digits,
                totpPeriodInput = configuration.periodSeconds.toString(),
                totpSetupError = null,
                showTotpScanner = false,
                showTotpReplaceConfirmation = false,
                isDirty = true,
            )
        }
        start()
    }

    private fun refresh() {
        val configuration = state.value.totpConfiguration ?: return
        val now = clock.now()
        val code = runCatching { service.generate(configuration, now).getOrNull() }.getOrNull()
        if (code == null) {
            state.update {
                it.copy(
                    currentTotpCode = "",
                    totpSecondsRemaining = 0,
                    totpProgress = 0f,
                    totpGenerationError = true,
                )
            }
            return
        }
        val remainingMillis =
            (code.expiresAt.toEpochMilliseconds() - now.toEpochMilliseconds()).coerceAtLeast(0)
        val secondsRemaining = ((remainingMillis + MILLIS_PER_SECOND - 1) / MILLIS_PER_SECOND).toInt()
        val progress = (remainingMillis.toFloat() / (configuration.periodSeconds * MILLIS_PER_SECOND))
            .coerceIn(0f, 1f)
        state.update {
            it.copy(
                currentTotpCode = code.value,
                totpSecondsRemaining = secondsRemaining,
                totpProgress = progress,
                totpGenerationError = false,
            )
        }
    }

    private companion object {
        const val MILLIS_PER_SECOND = 1_000L
        const val MIN_TICK_DELAY_MILLIS = 50L
    }
}
