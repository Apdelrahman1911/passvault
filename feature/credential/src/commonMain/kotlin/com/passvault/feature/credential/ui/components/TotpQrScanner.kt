package com.passvault.feature.credential.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.action_cancel
import com.passvault.core.designsystem.generated.resources.ui_totp_scanner_instruction
import com.passvault.core.designsystem.generated.resources.ui_totp_scanner_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TotpQrScannerDialog(
    onPayload: (String) -> Unit,
    onCancel: () -> Unit,
    onError: () -> Unit,
) {
    Dialog(onDismissRequest = onCancel) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(Res.string.ui_totp_scanner_title),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = stringResource(Res.string.ui_totp_scanner_instruction),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                PlatformTotpQrScanner(
                    onPayload = onPayload,
                    onError = onError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 220.dp, max = 480.dp),
                )
                TextButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(Res.string.action_cancel))
                }
            }
        }
    }
}

@Composable
internal expect fun PlatformTotpQrScanner(
    onPayload: (String) -> Unit,
    onError: () -> Unit,
    modifier: Modifier = Modifier,
)
