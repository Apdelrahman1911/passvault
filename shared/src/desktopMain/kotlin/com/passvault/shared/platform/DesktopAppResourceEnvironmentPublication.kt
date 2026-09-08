package com.passvault.shared.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.jetbrains.compose.resources.ResourceEnvironment
import org.jetbrains.compose.resources.rememberResourceEnvironment

internal data class DesktopAppResourceEnvironmentPublication(
    val owner: Any,
    val environment: ResourceEnvironment,
)

/** Only verified app content, inside its resolved language provider, publishes this environment. */
internal val desktopAppResourceEnvironment = MutableStateFlow<DesktopAppResourceEnvironmentPublication?>(null)

/**
 * Native UI outside the application content uses the same resolved resource
 * environment, rather than independently guessing from the process locale.
 * Before application startup resolves its preferences, retain the system fallback.
 */
@Composable
fun rememberDesktopAppResourceEnvironment(): ResourceEnvironment {
    val publication by desktopAppResourceEnvironment.collectAsState()
    return publication?.environment ?: rememberResourceEnvironment()
}

@Composable
internal actual fun PublishAppResourceEnvironment() {
    val owner = remember { Any() }
    val environment = rememberResourceEnvironment()
    DisposableEffect(owner) {
        onDispose {
            desktopAppResourceEnvironment.update { publication ->
                publication?.takeUnless { it.owner === owner }
            }
        }
    }
    SideEffect {
        desktopAppResourceEnvironment.value = DesktopAppResourceEnvironmentPublication(owner, environment)
    }
}
