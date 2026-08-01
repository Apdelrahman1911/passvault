package com.passvault.feature.onboarding.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.EnhancedEncryption
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.passvault.core.designsystem.components.EditorialIconTile
import com.passvault.core.designsystem.components.EditorialPanel
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.ui_a_calm_local_first_password_manager_for_credentials_no
import com.passvault.core.designsystem.generated.resources.ui_create_an_independent_encrypted_backup_before_moving_d
import com.passvault.core.designsystem.generated.resources.ui_encrypted_backups
import com.passvault.core.designsystem.generated.resources.ui_encrypted_vault
import com.passvault.core.designsystem.generated.resources.ui_get_started
import com.passvault.core.designsystem.generated.resources.ui_master_password_protection
import com.passvault.core.designsystem.generated.resources.ui_passvault
import com.passvault.core.designsystem.generated.resources.ui_vault_records_are_authenticated_and_encrypted_before_t
import com.passvault.core.designsystem.generated.resources.ui_welcome_to_passvault
import com.passvault.core.designsystem.generated.resources.ui_your_master_password_is_used_to_unlock_the_vault_key_a
import com.passvault.core.designsystem.tokens.Breakpoints
import com.passvault.core.designsystem.tokens.ComponentSpacing
import com.passvault.core.designsystem.tokens.Spacing
import com.passvault.feature.onboarding.presentation.OnboardingViewModel
import org.jetbrains.compose.resources.stringResource

@Composable
fun WelcomeScreen(
    state: OnboardingViewModel.OnboardingState,
    onEvent: (OnboardingViewModel.OnboardingEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val expanded = maxWidth >= Breakpoints.expandedMin
            val horizontalScreenPadding = if (expanded) {
                ComponentSpacing.screenHorizontal
            } else {
                Spacing.smMd
            }
            val scrollState = rememberScrollState()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = horizontalScreenPadding),
                contentAlignment = Alignment.TopCenter,
            ) {
                if (expanded) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = ComponentSpacing.contentMaxWidth)
                            .heightIn(min = 680.dp)
                            .padding(vertical = Spacing.lg),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
                    ) {
                        WelcomeHero(Modifier.weight(1.12f))
                        WelcomeDetails(
                            state = state,
                            onEvent = onEvent,
                            modifier = Modifier.weight(0.88f),
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 640.dp)
                            .padding(vertical = Spacing.sm),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md),
                    ) {
                        WelcomeHero()
                        WelcomeDetails(
                            state = state,
                            onEvent = onEvent,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomeHero(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 380.dp),
        color = MaterialTheme.colorScheme.inverseSurface,
        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            modifier = Modifier.padding(Spacing.xl),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Surface(
                modifier = Modifier.size(58.dp),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.inverseOnSurface,
                contentColor = MaterialTheme.colorScheme.inverseSurface,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(Spacing.smMd)) {
                Text(
                    text = stringResource(Res.string.ui_passvault),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.68f),
                )
                Text(
                    text = stringResource(Res.string.ui_welcome_to_passvault),
                    style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    text = stringResource(
                        Res.string.ui_a_calm_local_first_password_manager_for_credentials_no,
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.76f),
                )
            }
        }
    }
}

@Composable
private fun WelcomeDetails(
    state: OnboardingViewModel.OnboardingState,
    onEvent: (OnboardingViewModel.OnboardingEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    WelcomeDetailsContent(state, onEvent, modifier)
}

@Composable
private fun WelcomeDetailsContent(
    @Suppress("UNUSED_PARAMETER") state: OnboardingViewModel.OnboardingState,
    onEvent: (OnboardingViewModel.OnboardingEvent) -> Unit,
    modifier: Modifier,
) {
    EditorialPanel(
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(Spacing.lg),
    ) {
        FeatureItem(
            icon = Icons.Default.EnhancedEncryption,
            title = stringResource(Res.string.ui_encrypted_vault),
            description = stringResource(
                Res.string.ui_vault_records_are_authenticated_and_encrypted_before_t,
            ),
        )
        FeatureItem(
            icon = Icons.Default.Security,
            title = stringResource(Res.string.ui_master_password_protection),
            description = stringResource(
                Res.string.ui_your_master_password_is_used_to_unlock_the_vault_key_a,
            ),
        )
        FeatureItem(
            icon = Icons.Default.Archive,
            title = stringResource(Res.string.ui_encrypted_backups),
            description = stringResource(
                Res.string.ui_create_an_independent_encrypted_backup_before_moving_d,
            ),
        )
        Button(
            onClick = { onEvent(OnboardingViewModel.OnboardingEvent.OnGetStartedClick) },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            shape = MaterialTheme.shapes.large,
        ) {
            Text(
                text = stringResource(Res.string.ui_get_started),
                style = MaterialTheme.typography.titleMedium,
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier
                    .padding(start = Spacing.sm)
                    .size(20.dp),
            )
        }
    }
}

@Composable
private fun FeatureItem(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalAlignment = Alignment.Top,
    ) {
        EditorialIconTile(
            icon = icon,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
