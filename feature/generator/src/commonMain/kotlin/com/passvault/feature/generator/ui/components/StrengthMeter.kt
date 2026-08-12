package com.passvault.feature.generator.ui.components

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    StrengthMeterCard(
        presentation = strengthPresentation(strength),
        passwordLength = passwordLength,
        modifier = modifier,
    )
}

@Composable
private fun strengthPresentation(
    strength: GeneratorViewModel.PasswordStrength,
): StrengthPresentation =
    when (strength) {
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

@Composable
private fun StrengthMeterCard(
    presentation: StrengthPresentation,
    passwordLength: Int,
    modifier: Modifier = Modifier,
) {
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
            StrengthHeader(presentation)

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { presentation.progress },
                modifier = Modifier.fillMaxWidth(),
                color = presentation.color,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = pluralStringResource(
                    Res.plurals.ui_strength_character_count,
                    passwordLength,
                    presentation.description,
                    passwordLength,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StrengthHeader(presentation: StrengthPresentation) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.ui_password_strength),
            style = MaterialTheme.typography.labelMedium,
        )
        Surface(
            color = presentation.color.copy(alpha = 0.2f),
            shape = MaterialTheme.shapes.small,
        ) {
            Text(
                text = presentation.label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
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
