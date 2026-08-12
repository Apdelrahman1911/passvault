package com.passvault.feature.generator.ui.components

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun PasswordDisplay(
    password: String,
    isGenerating: Boolean,
    onCopy: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPassword by remember { mutableStateOf(false) }
    LaunchedEffect(password) {
        showPassword = false
    }
    LaunchedEffect(showPassword) {
        if (showPassword) {
            kotlinx.coroutines.delay(15_000)
            showPassword = false
        }
    }

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.inverseSurface,
        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            PasswordValue(
                password = password,
                visible = showPassword,
                isGenerating = isGenerating,
            )
            Spacer(modifier = Modifier.height(16.dp))
            PasswordActions(
                passwordAvailable = password.isNotEmpty(),
                passwordVisible = showPassword,
                isGenerating = isGenerating,
                onCopy = onCopy,
                onVisibilityChange = { showPassword = !showPassword },
                onRefresh = onRefresh,
            )
        }
    }
}

@Composable
private fun PasswordValue(
    password: String,
    visible: Boolean,
    isGenerating: Boolean,
) {
    val hiddenDescription = stringResource(Res.string.secure_field_password_hidden)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (isGenerating) {
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color = MaterialTheme.colorScheme.inversePrimary,
                strokeWidth = 2.dp,
            )
        } else {
            Text(
                text = when {
                    visible -> password
                    password.isEmpty() -> ""
                    else -> "\u2022".repeat(12)
                },
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.inverseOnSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (visible || password.isEmpty()) {
                            Modifier
                        } else {
                            Modifier.clearAndSetSemantics { contentDescription = hiddenDescription }
                        },
                    ),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PasswordActions(
    passwordAvailable: Boolean,
    passwordVisible: Boolean,
    isGenerating: Boolean,
    onCopy: () -> Unit,
    onVisibilityChange: () -> Unit,
    onRefresh: () -> Unit,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 2,
    ) {
        FilledTonalButton(
            onClick = onCopy,
            enabled = passwordAvailable,
            modifier = Modifier.weight(1f),
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(Res.string.action_copy))
        }
        FilledTonalButton(
            onClick = onVisibilityChange,
            enabled = passwordAvailable,
            modifier = Modifier.weight(1f),
        ) {
            val action = if (passwordVisible) Res.string.action_hide else Res.string.action_show
            Icon(
                imageVector = if (passwordVisible) {
                    Icons.Default.VisibilityOff
                } else {
                    Icons.Default.Visibility
                },
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(action))
        }
        FilledTonalButton(
            onClick = onRefresh,
            enabled = !isGenerating,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(Res.string.ui_new))
        }
    }
}
