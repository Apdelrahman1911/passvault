package com.passvault.core.designsystem.text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

/**
 * Resource-backed text carried by presentation state without resolving a locale in a ViewModel.
 */
@Immutable
sealed interface UiText {
    @Immutable
    data class Resource(
        val resource: StringResource,
        val arguments: List<Any> = emptyList(),
    ) : UiText

    /**
     * Text originating outside the application catalog, such as a user-selected file name.
     */
    @Immutable
    data class Dynamic(val value: String) : UiText
}

fun uiText(resource: StringResource, vararg arguments: Any): UiText =
    UiText.Resource(resource, arguments.toList())

@Composable
fun UiText.resolve(): String = when (this) {
    is UiText.Resource -> stringResource(
        resource,
        *arguments.map { argument ->
            if (argument is UiText) argument.resolve() else argument
        }.toTypedArray(),
    )
    is UiText.Dynamic -> value
}

suspend fun UiText.resolveSuspending(): String = when (this) {
    is UiText.Resource -> getString(
        resource,
        *arguments.map { argument ->
            if (argument is UiText) argument.resolveSuspending() else argument
        }.toTypedArray(),
    )
    is UiText.Dynamic -> value
}
