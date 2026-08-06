package com.passvault.core.designsystem.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Displays the platform keyboard-dismiss affordance when one is needed. */
@Composable
expect fun KeyboardDismissButton(modifier: Modifier = Modifier)
