package com.passvault.feature.onboarding.ui

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.passvault.core.designsystem.components.EditorialIconTile
import com.passvault.core.designsystem.components.EditorialPageHeader
import com.passvault.core.designsystem.components.EditorialPanel
import com.passvault.core.designsystem.components.EditorialStatusBanner
import com.passvault.core.designsystem.platform.passVaultTopAppBarColors
import com.passvault.core.designsystem.platform.scaffoldVerticalScroll
import com.passvault.core.designsystem.tokens.ComponentSpacing
import com.passvault.core.designsystem.tokens.Spacing
import com.passvault.feature.onboarding.presentation.OnboardingViewModel

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SecurityExplanationScreen(
    onEvent: (OnboardingViewModel.OnboardingEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = {
                            onEvent(OnboardingViewModel.OnboardingEvent.OnBackClick)
                        },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.ui_go_back),
                        )
                    }
                },
                colors = passVaultTopAppBarColors(),
            )
        },
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .scaffoldVerticalScroll(rememberScrollState(), paddingValues)
                .imePadding()
                .padding(
                    horizontal = ComponentSpacing.screenHorizontal,
                    vertical = ComponentSpacing.screenVertical,
                )
                .widthIn(max = ComponentSpacing.formMaxWidth)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(ComponentSpacing.sectionSpacing),
        ) {
            EditorialPageHeader(
                eyebrow = stringResource(Res.string.ui_security_overview),
                title = stringResource(Res.string.ui_how_passvault_protects_you),
                subtitle = stringResource(
                    Res.string.ui_a_few_practical_details_help_you_use_a_password_manage,
                ),
            )

            EditorialPanel(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(Spacing.lg),
            ) {
                SecurityFeatureCard(
                    icon = Icons.Default.Key,
                    title = stringResource(Res.string.ui_your_master_password_stays_local),
                    description = stringResource(Res.string.ui_it_is_used_on_this_device_to_unlock_the_vault_passvaul),
                )
                SecurityFeatureCard(
                    icon = Icons.Default.Lock,
                    title = stringResource(Res.string.ui_authenticated_encryption),
                    description = stringResource(Res.string.ui_vault_records_use_xchacha20_poly1305_with_argon2id_key),
                )
                SecurityFeatureCard(
                    icon = Icons.Default.CloudOff,
                    title = stringResource(Res.string.ui_local_first_storage),
                    description = stringResource(Res.string.ui_your_vault_remains_on_this_device_unless_you_explicitl),
                )
                SecurityFeatureCard(
                    icon = Icons.Default.Timer,
                    title = stringResource(Res.string.ui_auto_lock_protection),
                    description = stringResource(Res.string.ui_the_configured_inactivity_timer_and_explicit_lock_acti),
                )
            }

            EditorialStatusBanner(
                icon = Icons.Default.Warning,
                title = stringResource(Res.string.ui_remember_your_password),
                message = stringResource(
                    Res.string.ui_if_you_forget_your_master_password_your_encrypted_vaul,
                ),
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            )

            Button(
                onClick = {
                    onEvent(OnboardingViewModel.OnboardingEvent.OnCompleteSetupClick)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                Text(stringResource(Res.string.ui_continue), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun SecurityFeatureCard(
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
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
