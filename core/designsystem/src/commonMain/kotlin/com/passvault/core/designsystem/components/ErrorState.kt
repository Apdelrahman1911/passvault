package com.passvault.core.designsystem.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.action_retry
import com.passvault.core.designsystem.generated.resources.error_state_message
import com.passvault.core.designsystem.generated.resources.error_state_title
import com.passvault.core.designsystem.tokens.ComponentSpacing
import org.jetbrains.compose.resources.stringResource

/** Full-screen bootstrap failure with a retry action. */
@Composable
fun ErrorState(
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(ComponentSpacing.xl)
            .animateContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error,
        )
        Spacer(modifier = Modifier.height(ComponentSpacing.lg))
        Text(
            text = stringResource(Res.string.error_state_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { heading() },
        )
        Spacer(modifier = Modifier.height(ComponentSpacing.sm))
        Text(
            text = stringResource(Res.string.error_state_message),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = ComponentSpacing.lg),
        )
        Spacer(modifier = Modifier.height(ComponentSpacing.xl))
        Button(
            onClick = onAction,
            modifier = Modifier
                .widthIn(max = 360.dp)
                .fillMaxWidth(),
        ) {
            Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = null,
                modifier = Modifier.padding(end = ComponentSpacing.sm),
            )
            Text(text = stringResource(Res.string.action_retry))
        }
    }
}
