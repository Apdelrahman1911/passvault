package com.passvault.core.designsystem.components

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.passvault.core.designsystem.theme.VaultShapes
import kotlinx.coroutines.delay

/**
 * A secure text field for password input with visibility toggle.
 */
@Composable
fun SecureTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String? = null,
    isError: Boolean = false,
    icon: ImageVector? = null,
    shape: Shape = VaultShapes.InputField,
    maxLines: Int = 1,
    onGenerateClick: (() -> Unit)? = null,
    supportingText: String? = null,
    imeAction: ImeAction = ImeAction.Done,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    leadingIcon: ImageVector? = null,
    errorMessage: String? = null,
    initiallyVisible: Boolean = false
) {
    var passwordVisible by remember { mutableStateOf(initiallyVisible) }
    var hasObservedValue by remember { mutableStateOf(false) }
    val resolvedLeadingIcon = leadingIcon ?: icon
    val hasError = isError || errorMessage != null
    LaunchedEffect(value) {
        if (hasObservedValue) {
            passwordVisible = false
        } else {
            hasObservedValue = true
        }
    }
    LaunchedEffect(passwordVisible, value) {
        if (passwordVisible) {
            delay(15_000)
            passwordVisible = false
        }
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        label = if (label.isNullOrEmpty()) null else { @Composable { Text(label) } },
        isError = hasError,
        leadingIcon = resolvedLeadingIcon?.let { leading ->
            @Composable { Icon(leading, contentDescription = null) }
        },
        trailingIcon = {
            SecureFieldActions(
                passwordVisible = passwordVisible,
                enabled = enabled,
                onGenerateClick = onGenerateClick,
                onVisibilityChange = { passwordVisible = !passwordVisible },
            )
        },
        shape = shape,
        maxLines = maxLines,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction,
        ),
        keyboardActions = keyboardActions,
        singleLine = maxLines == 1,
        supportingText = secureFieldSupportingText(errorMessage, supportingText),
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (hasError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            errorBorderColor = MaterialTheme.colorScheme.error,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}

@Composable
private fun SecureFieldActions(
    passwordVisible: Boolean,
    enabled: Boolean,
    onGenerateClick: (() -> Unit)?,
    onVisibilityChange: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (onGenerateClick != null) {
            IconButton(onClick = onGenerateClick, enabled = enabled) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = stringResource(Res.string.action_generate),
                )
            }
        }
        IconButton(onClick = onVisibilityChange, enabled = enabled) {
            Icon(
                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                contentDescription = if (passwordVisible) {
                    stringResource(Res.string.secure_field_hide_password)
                } else {
                    stringResource(Res.string.secure_field_show_password)
                },
            )
        }
    }
}

@Composable
private fun secureFieldSupportingText(
    errorMessage: String?,
    supportingText: String?,
): (@Composable () -> Unit)? = when {
    errorMessage != null -> {
        @Composable {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
    supportingText != null -> {
        @Composable {
            Text(
                text = supportingText,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    else -> null
}
