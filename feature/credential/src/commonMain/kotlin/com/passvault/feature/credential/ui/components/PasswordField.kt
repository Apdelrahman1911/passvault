package com.passvault.feature.credential.ui.components

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    onCopy: (() -> Unit)?,
    onGenerate: (() -> Unit)?,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    showGenerateButton: Boolean = false,
    readOnly: Boolean = false,
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val resolvedLabel = label ?: stringResource(Res.string.ui_password)
    val resolvedPlaceholder = placeholder ?: stringResource(Res.string.ui_enter_password)

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text(resolvedLabel) },
        placeholder = { Text(resolvedPlaceholder) },
        visualTransformation = if (passwordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next
        ),
        singleLine = true,
        readOnly = readOnly,
        trailingIcon = {
            Row {
                if (value.isNotEmpty() && onCopy != null) {
                    IconButton(onClick = onCopy) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = stringResource(Res.string.credential_card_copy_password)
                        )
                    }
                }

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) {
                            Icons.Default.VisibilityOff
                        } else {
                            Icons.Default.Visibility
                        },
                        contentDescription = if (passwordVisible) stringResource(Res.string.secure_field_hide_password) else stringResource(Res.string.secure_field_show_password)
                    )
                }

                if (showGenerateButton && onGenerate != null) {
                    IconButton(onClick = onGenerate) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(Res.string.ui_generate_password)
                        )
                    }
                }
            }
        }
    )
}
