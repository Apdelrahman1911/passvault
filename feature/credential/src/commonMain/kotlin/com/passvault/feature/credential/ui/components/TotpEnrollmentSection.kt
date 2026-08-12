package com.passvault.feature.credential.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.passvault.core.designsystem.components.SecureTextField
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.text.resolve
import com.passvault.core.domain.model.TotpAlgorithm
import com.passvault.feature.credential.presentation.CredentialViewModel
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TotpEnrollmentSection(
    state: CredentialViewModel.CredentialState,
    onEvent: (CredentialViewModel.CredentialEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(Res.string.ui_totp_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(Res.string.ui_totp_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        state.totpConfiguration?.let {
            ConfiguredTotpSummary(configuration = it, onEvent = onEvent)
        }
        TotpSetupFields(state = state, onEvent = onEvent)
    }

    TotpEnrollmentDialogs(state = state, onEvent = onEvent)
}

@Composable
private fun ConfiguredTotpSummary(
    configuration: com.passvault.core.domain.model.TotpConfiguration,
    onEvent: (CredentialViewModel.CredentialEvent) -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                stringResource(Res.string.ui_totp_configured),
                style = MaterialTheme.typography.titleSmall,
            )
            configurationLabel(configuration.issuer, configuration.accountName)?.let { label ->
                Text(text = label, style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                text = stringResource(
                    Res.string.ui_totp_configuration_summary,
                    configuration.algorithm.name,
                    configuration.digits,
                    configuration.periodSeconds,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(
                onClick = { onEvent(CredentialViewModel.CredentialEvent.OnTotpRemoveClick) },
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Text(stringResource(Res.string.ui_totp_remove))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TotpSetupFields(
    state: CredentialViewModel.CredentialState,
    onEvent: (CredentialViewModel.CredentialEvent) -> Unit,
) {
    TotpSecretInput(state = state, onEvent = onEvent)
    TotpAlgorithmSelector(state = state, onEvent = onEvent)
    TotpCodeParameters(state = state, onEvent = onEvent)
    TotpSetupActions(state = state, onEvent = onEvent)
    Text(
        text = stringResource(Res.string.ui_totp_device_time_hint),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun TotpSecretInput(
    state: CredentialViewModel.CredentialState,
    onEvent: (CredentialViewModel.CredentialEvent) -> Unit,
) {
    SecureTextField(
        value = state.totpSetupInput,
        onValueChange = {
            onEvent(CredentialViewModel.CredentialEvent.OnTotpSetupInputChanged(it))
        },
        label = stringResource(Res.string.ui_totp_setup_key_or_uri),
        supportingText = stringResource(Res.string.ui_totp_setup_hint),
        errorMessage = state.totpSetupError?.resolve(),
        isError = state.totpSetupError != null,
        modifier = Modifier.fillMaxWidth(),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TotpAlgorithmSelector(
    state: CredentialViewModel.CredentialState,
    onEvent: (CredentialViewModel.CredentialEvent) -> Unit,
) {
    Text(stringResource(Res.string.ui_totp_algorithm), style = MaterialTheme.typography.labelLarge)
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TotpAlgorithm.entries.forEach { algorithm ->
            FilterChip(
                selected = state.totpAlgorithm == algorithm,
                onClick = {
                    onEvent(CredentialViewModel.CredentialEvent.OnTotpAlgorithmChanged(algorithm))
                },
                label = { Text(algorithm.name) },
            )
        }
    }
}

@Composable
private fun TotpCodeParameters(
    state: CredentialViewModel.CredentialState,
    onEvent: (CredentialViewModel.CredentialEvent) -> Unit,
) {
    Text(stringResource(Res.string.ui_totp_digits), style = MaterialTheme.typography.labelLarge)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(6, 8).forEach { digits ->
            FilterChip(
                selected = state.totpDigits == digits,
                onClick = {
                    onEvent(CredentialViewModel.CredentialEvent.OnTotpDigitsChanged(digits))
                },
                label = { Text(digits.toString()) },
            )
        }
    }
    OutlinedTextField(
        value = state.totpPeriodInput,
        onValueChange = { onEvent(CredentialViewModel.CredentialEvent.OnTotpPeriodChanged(it)) },
        label = { Text(stringResource(Res.string.ui_totp_period_seconds)) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}

@Composable
private fun TotpSetupActions(
    state: CredentialViewModel.CredentialState,
    onEvent: (CredentialViewModel.CredentialEvent) -> Unit,
) {
    Button(
        onClick = { onEvent(CredentialViewModel.CredentialEvent.OnTotpAddClick) },
        enabled = state.totpSetupInput.isNotBlank(),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            stringResource(
                if (state.totpConfiguration == null) {
                    Res.string.ui_totp_add
                } else {
                    Res.string.ui_totp_replace
                },
            ),
        )
    }
    OutlinedButton(
        onClick = { onEvent(CredentialViewModel.CredentialEvent.OnTotpScanClick) },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(Icons.Default.QrCodeScanner, contentDescription = null)
        Text(stringResource(Res.string.ui_totp_scan_qr))
    }
}

@Composable
private fun TotpEnrollmentDialogs(
    state: CredentialViewModel.CredentialState,
    onEvent: (CredentialViewModel.CredentialEvent) -> Unit,
) {
    if (state.showTotpScanner) {
        TotpQrScannerDialog(
            onPayload = { onEvent(CredentialViewModel.CredentialEvent.OnTotpQrScanned(it)) },
            onCancel = { onEvent(CredentialViewModel.CredentialEvent.OnTotpScanCancel) },
            onError = { onEvent(CredentialViewModel.CredentialEvent.OnTotpScanError) },
        )
    }
    if (state.showTotpReplaceConfirmation) {
        TotpReplaceDialog(onEvent = onEvent)
    }

    if (state.showTotpRemoveConfirmation) {
        TotpRemoveDialog(onEvent = onEvent)
    }
}

@Composable
private fun TotpReplaceDialog(onEvent: (CredentialViewModel.CredentialEvent) -> Unit) {
    AlertDialog(
        onDismissRequest = { onEvent(CredentialViewModel.CredentialEvent.OnTotpReplaceCancel) },
        title = { Text(stringResource(Res.string.ui_totp_replace_title)) },
        text = { Text(stringResource(Res.string.ui_totp_replace_message)) },
        confirmButton = {
            TextButton(
                onClick = { onEvent(CredentialViewModel.CredentialEvent.OnTotpReplaceConfirm) },
            ) {
                Text(stringResource(Res.string.ui_totp_replace))
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onEvent(CredentialViewModel.CredentialEvent.OnTotpReplaceCancel) },
            ) {
                Text(stringResource(Res.string.action_cancel))
            }
        },
    )
}

@Composable
private fun TotpRemoveDialog(onEvent: (CredentialViewModel.CredentialEvent) -> Unit) {
    AlertDialog(
        onDismissRequest = { onEvent(CredentialViewModel.CredentialEvent.OnTotpRemoveCancel) },
        title = { Text(stringResource(Res.string.ui_totp_remove_title)) },
        text = { Text(stringResource(Res.string.ui_totp_remove_message)) },
        confirmButton = {
            TextButton(
                onClick = { onEvent(CredentialViewModel.CredentialEvent.OnTotpRemoveConfirm) },
            ) {
                Text(
                    text = stringResource(Res.string.ui_totp_remove),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onEvent(CredentialViewModel.CredentialEvent.OnTotpRemoveCancel) },
            ) {
                Text(stringResource(Res.string.action_cancel))
            }
        },
    )
}

private fun configurationLabel(issuer: String?, accountName: String?): String? =
    listOfNotNull(issuer, accountName)
        .filter(String::isNotBlank)
        .joinToString(" · ")
        .takeIf(String::isNotBlank)
