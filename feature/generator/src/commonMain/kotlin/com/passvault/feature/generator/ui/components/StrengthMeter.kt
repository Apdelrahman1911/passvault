package com.passvault.feature.generator.ui.components

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.passvault.feature.generator.presentation.GeneratorViewModel

@Composable
fun StrengthMeter(
    strength: GeneratorViewModel.PasswordStrength,
    passwordLength: Int,
    modifier: Modifier = Modifier,
) {
    val presentation = when (strength) {
        GeneratorViewModel.PasswordStrength.WEAK ->
            StrengthPresentation(
                stringResource(Res.string.password_strength_weak),
                MaterialTheme.colorScheme.error,
                0.25f,
                stringResource(Res.string.ui_this_password_can_be_cracked_easily),
            )
        GeneratorViewModel.PasswordStrength.FAIR ->
            StrengthPresentation(
                stringResource(Res.string.password_strength_fair),
                MaterialTheme.colorScheme.tertiary,
                0.5f,
                stringResource(Res.string.ui_consider_making_it_longer_or_more_complex),
            )
        GeneratorViewModel.PasswordStrength.GOOD ->
            StrengthPresentation(
                stringResource(Res.string.password_strength_good),
                MaterialTheme.colorScheme.secondary,
                0.75f,
                stringResource(Res.string.ui_this_password_offers_decent_protection),
            )
        GeneratorViewModel.PasswordStrength.STRONG ->
            StrengthPresentation(
                stringResource(Res.string.password_strength_strong),
                MaterialTheme.colorScheme.primary,
                0.9f,
                stringResource(Res.string.ui_this_password_is_difficult_to_crack),
            )
        GeneratorViewModel.PasswordStrength.VERY_STRONG ->
            StrengthPresentation(
                stringResource(Res.string.password_strength_very_strong),
                MaterialTheme.colorScheme.primary,
                1f,
                stringResource(Res.string.ui_this_password_is_extremely_secure),
            )
    }
    val (label, color, progress, description) = presentation

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.ui_password_strength),
                    style = MaterialTheme.typography.labelMedium
                )
                Surface(
                    color = color.copy(alpha = 0.2f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = color,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = color,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = pluralStringResource(
                    Res.plurals.ui_strength_character_count,
                    passwordLength,
                    description,
                    passwordLength,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private data class StrengthPresentation(
    val label: String,
    val color: Color,
    val progress: Float,
    val description: String,
)
