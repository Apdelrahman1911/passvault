package com.passvault.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.tokens.ComponentSpacing
import org.jetbrains.compose.resources.stringResource

/** Fail-closed startup screen for a structurally damaged local vault. */
@Composable
internal fun DatabaseRecoveryState(
    canPreserveAndReset: Boolean,
    isPreserving: Boolean,
    preservationFailed: Boolean,
    onRetry: () -> Unit,
    onPreserveAndReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showConfirmation by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(ComponentSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        DatabaseRecoveryMessage(
            canPreserveAndReset = canPreserveAndReset,
            preservationFailed = preservationFailed,
        )
        DatabaseRecoveryActions(
            canPreserveAndReset = canPreserveAndReset,
            isPreserving = isPreserving,
            onRetry = onRetry,
            onRequestConfirmation = { showConfirmation = true },
        )
    }

    if (showConfirmation) {
        DatabaseRecoveryConfirmation(
            isPreserving = isPreserving,
            onDismiss = { showConfirmation = false },
            onConfirm = onPreserveAndReset,
        )
    }
}

@Composable
private fun ColumnScope.DatabaseRecoveryMessage(
    canPreserveAndReset: Boolean,
    preservationFailed: Boolean,
) {
    Icon(
        imageVector = Icons.Outlined.WarningAmber,
        contentDescription = null,
        modifier = Modifier.size(64.dp),
        tint = MaterialTheme.colorScheme.error,
    )
    Spacer(modifier = Modifier.height(ComponentSpacing.lg))
    Text(
        text = stringResource(Res.string.database_recovery_title),
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center,
        modifier = Modifier.semantics { heading() },
    )
    Spacer(modifier = Modifier.height(ComponentSpacing.sm))
    Text(
        text = stringResource(Res.string.database_recovery_message),
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    if (!canPreserveAndReset) {
        Spacer(modifier = Modifier.height(ComponentSpacing.md))
        Text(
            text = stringResource(Res.string.database_recovery_manual_message),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    if (preservationFailed) {
        Spacer(modifier = Modifier.height(ComponentSpacing.md))
        Text(
            text = stringResource(Res.string.database_recovery_preserve_failed),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
private fun ColumnScope.DatabaseRecoveryActions(
    canPreserveAndReset: Boolean,
    isPreserving: Boolean,
    onRetry: () -> Unit,
    onRequestConfirmation: () -> Unit,
) {
    Spacer(modifier = Modifier.height(ComponentSpacing.xl))
    if (canPreserveAndReset) {
        Button(
            onClick = onRequestConfirmation,
            enabled = !isPreserving,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (isPreserving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onError,
                )
            } else {
                Text(stringResource(Res.string.database_recovery_preserve_action))
            }
        }
        Spacer(modifier = Modifier.height(ComponentSpacing.sm))
    }
    OutlinedButton(
        onClick = onRetry,
        enabled = !isPreserving,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(stringResource(Res.string.action_retry))
    }
}

@Composable
private fun DatabaseRecoveryConfirmation(
    isPreserving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!isPreserving) onDismiss() },
        title = { Text(stringResource(Res.string.database_recovery_confirm_title)) },
        text = { Text(stringResource(Res.string.database_recovery_confirm_message)) },
        confirmButton = {
            TextButton(
                enabled = !isPreserving,
                onClick = {
                    onDismiss()
                    onConfirm()
                },
            ) {
                Text(stringResource(Res.string.database_recovery_confirm_action))
            }
        },
        dismissButton = {
            TextButton(
                enabled = !isPreserving,
                onClick = onDismiss,
            ) {
                Text(stringResource(Res.string.action_cancel))
            }
        },
    )
}
