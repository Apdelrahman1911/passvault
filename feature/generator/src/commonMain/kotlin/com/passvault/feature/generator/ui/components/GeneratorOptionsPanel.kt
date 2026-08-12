package com.passvault.feature.generator.ui.components

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.passvault.feature.generator.presentation.GeneratorViewModel

@Composable
fun GeneratorOptionsPanel(
    state: GeneratorViewModel.GeneratorState,
    onEvent: (GeneratorViewModel.GeneratorEvent) -> Unit,
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
            Text(
                text = stringResource(Res.string.ui_generator_options),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (state.isPassphraseMode) {
                PassphraseOptions(
                    wordCount = state.wordCount,
                    separator = state.wordSeparator,
                    onWordCountChange = {
                        onEvent(GeneratorViewModel.GeneratorEvent.OnWordCountChanged(it))
                    },
                    onSeparatorChange = {
                        onEvent(GeneratorViewModel.GeneratorEvent.OnWordSeparatorChanged(it))
                    }
                )
            } else {
                PasswordOptions(
                    length = state.length,
                    includeUppercase = state.includeUppercase,
                    includeLowercase = state.includeLowercase,
                    includeNumbers = state.includeNumbers,
                    includeSymbols = state.includeSymbols,
                    excludeAmbiguous = state.excludeAmbiguous,
                    onLengthChange = {
                        onEvent(GeneratorViewModel.GeneratorEvent.OnLengthChanged(it))
                    },
                    onUppercaseChange = {
                        onEvent(GeneratorViewModel.GeneratorEvent.OnIncludeUppercaseChanged(it))
                    },
                    onLowercaseChange = {
                        onEvent(GeneratorViewModel.GeneratorEvent.OnIncludeLowercaseChanged(it))
                    },
                    onNumbersChange = {
                        onEvent(GeneratorViewModel.GeneratorEvent.OnIncludeNumbersChanged(it))
                    },
                    onSymbolsChange = {
                        onEvent(GeneratorViewModel.GeneratorEvent.OnIncludeSymbolsChanged(it))
                    },
                    onAmbiguousChange = {
                        onEvent(GeneratorViewModel.GeneratorEvent.OnExcludeAmbiguousChanged(it))
                    }
                )
            }
        }
    }
}

@Composable
private fun PasswordOptions(
    length: Int,
    includeUppercase: Boolean,
    includeLowercase: Boolean,
    includeNumbers: Boolean,
    includeSymbols: Boolean,
    excludeAmbiguous: Boolean,
    onLengthChange: (Int) -> Unit,
    onUppercaseChange: (Boolean) -> Unit,
    onLowercaseChange: (Boolean) -> Unit,
    onNumbersChange: (Boolean) -> Unit,
    onSymbolsChange: (Boolean) -> Unit,
    onAmbiguousChange: (Boolean) -> Unit,
) {
    Column {
        // Length slider
        val lengthDescription = stringResource(Res.string.ui_password_length_value, length)
        Text(
            text = lengthDescription,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Slider(
            value = length.toFloat(),
            onValueChange = { onLengthChange(it.toInt()) },
            valueRange = GeneratorViewModel.MIN_PASSWORD_LENGTH.toFloat()..
                GeneratorViewModel.MAX_PASSWORD_LENGTH.toFloat(),
            steps = GeneratorViewModel.MAX_PASSWORD_LENGTH -
                GeneratorViewModel.MIN_PASSWORD_LENGTH -
                1,
            modifier = Modifier.semantics {
                contentDescription = lengthDescription
            },
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Character types
        OptionSwitch(
            icon = Icons.Default.TextFields,
            label = stringResource(Res.string.ui_uppercase_a_z),
            checked = includeUppercase,
            onCheckedChange = onUppercaseChange
        )

        OptionSwitch(
            icon = Icons.Default.TextFields,
            label = stringResource(Res.string.ui_lowercase_a_z),
            checked = includeLowercase,
            onCheckedChange = onLowercaseChange
        )

        OptionSwitch(
            icon = Icons.Default.Numbers,
            label = stringResource(Res.string.ui_numbers_0_9),
            checked = includeNumbers,
            onCheckedChange = onNumbersChange
        )

        OptionSwitch(
            icon = Icons.Default.Tag,
            label = stringResource(Res.string.ui_symbols_percent),
            checked = includeSymbols,
            onCheckedChange = onSymbolsChange
        )

        Spacer(modifier = Modifier.height(8.dp))

        HorizontalDivider()

        Spacer(modifier = Modifier.height(8.dp))

        OptionSwitch(
            icon = Icons.Default.FilterAlt,
            label = stringResource(Res.string.ui_exclude_ambiguous_0_o_l_i),
            checked = excludeAmbiguous,
            onCheckedChange = onAmbiguousChange
        )
    }
}

@Composable
private fun PassphraseOptions(
    wordCount: Int,
    separator: String,
    onWordCountChange: (Int) -> Unit,
    onSeparatorChange: (String) -> Unit,
) {
    Column {
        val wordCountDescription = stringResource(Res.string.ui_word_count_value, wordCount)
        Text(
            text = wordCountDescription,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Slider(
            value = wordCount.toFloat(),
            onValueChange = { onWordCountChange(it.toInt()) },
            valueRange = GeneratorViewModel.MIN_WORD_COUNT.toFloat()..
                GeneratorViewModel.MAX_WORD_COUNT.toFloat(),
            steps = GeneratorViewModel.MAX_WORD_COUNT - GeneratorViewModel.MIN_WORD_COUNT - 1,
            modifier = Modifier.semantics {
                contentDescription = wordCountDescription
            },
        )

        Spacer(modifier = Modifier.height(16.dp))

        SeparatorOptions(separator, onSeparatorChange)

        Spacer(modifier = Modifier.height(16.dp))
        PassphraseInfo()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SeparatorOptions(
    selectedSeparator: String,
    onSeparatorChange: (String) -> Unit,
) {
    Text(
        text = stringResource(Res.string.ui_word_separator),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(bottom = 8.dp),
    )
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        listOf("-", "_", " ", ".").forEach { separator ->
            FilterChip(
                selected = selectedSeparator == separator,
                onClick = { onSeparatorChange(separator) },
                label = { Text(separatorLabel(separator)) },
            )
        }
    }
}

@Composable
private fun separatorLabel(separator: String): String = when (separator) {
    " " -> stringResource(Res.string.ui_space)
    "-" -> stringResource(Res.string.ui_hyphen)
    "_" -> stringResource(Res.string.ui_underscore)
    "." -> stringResource(Res.string.ui_dot)
    else -> separator
}

@Composable
private fun PassphraseInfo() {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(
                    Res.string.ui_passphrases_are_easier_to_remember_and_often_more_secu,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        }
    }
}

@Composable
private fun OptionSwitch(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                role = Role.Switch,
                onValueChange = onCheckedChange,
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = null,
        )
    }
}
