package com.passvault.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import com.passvault.core.designsystem.components.*
import com.passvault.core.designsystem.theme.PassVaultTheme
import com.passvault.core.designsystem.theme.PassVaultAccent
import com.passvault.core.designsystem.theme.VaultKeeperColors
import com.passvault.core.designsystem.theme.VaultShapes
import com.passvault.core.designsystem.tokens.*

/**
 * Design System Module exports.
 */
object DesignSystem {
    // Re-export components as functions to avoid recursion and provide better IDE support

    @Composable
    fun SecureTextField(
        value: String,
        onValueChange: (String) -> Unit,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        label: String = "",
        isError: Boolean = false,
        icon: ImageVector? = null,
        shape: Shape = VaultShapes.InputField,
        maxLines: Int = 1
    ) = com.passvault.core.designsystem.components.SecureTextField(
        value, onValueChange, modifier, enabled, label, isError, icon, shape, maxLines
    )

    @Composable
    fun PrimaryActionButton(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        state: ButtonState = ButtonState.Idle,
        enabled: Boolean = true,
        icon: ImageVector? = null,
        shape: Shape = VaultShapes.Button
    ) = com.passvault.core.designsystem.components.PrimaryActionButton(
        text, onClick, modifier, state, enabled, icon, shape
    )

    // Colors
    val Colors = VaultKeeperColors

    // Shapes
    val Shapes = VaultShapes

    // Spacing
    val Spacing = com.passvault.core.designsystem.tokens.Spacing
    val ComponentSpacing = com.passvault.core.designsystem.tokens.ComponentSpacing

    // Theme
    @Composable
    fun Theme(
        darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
        accent: PassVaultAccent = PassVaultAccent.NEUTRAL,
        content: @Composable () -> Unit
    ) = PassVaultTheme(darkTheme = darkTheme, accent = accent, content = content)
}
