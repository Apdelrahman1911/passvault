package com.passvault.core.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.passvault.core.designsystem.tokens.Breakpoints
import com.passvault.core.designsystem.tokens.ComponentSpacing
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Window size class for responsive layouts.
 */
enum class WindowSizeClass {
    COMPACT,    // < 600dp (phones)
    MEDIUM,     // 600-839dp (tablets portrait, phones landscape)
    EXPANDED    // >= 840dp (tablets landscape, desktop)
}

/**
 * Calculates window size class from width.
 *
 * @param widthDp The width in dp.
 * @return The window size class.
 */
fun calculateWindowSizeClass(widthDp: Int): WindowSizeClass {
    return when {
        widthDp < 600 -> WindowSizeClass.COMPACT
        widthDp < 840 -> WindowSizeClass.MEDIUM
        else -> WindowSizeClass.EXPANDED
    }
}

/**
 * Responsive scaffold that adapts to window size.
 *
 * @param windowSizeClass The current window size class.
 * @param modifier Modifier to be applied.
 * @param topBar Top bar content.
 * @param bottomBar Bottom bar content.
 * @param navigationRail Navigation rail content for medium/expanded screens.
 * @param snackbarHost Snackbar host.
 * @param floatingActionButton FAB content.
 * @param floatingActionButtonPosition FAB position.
 * @param containerColor Container color.
 * @param contentColor Content color.
 * @param content Window content.
 */
@Composable
fun ResponsiveScaffold(
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    navigationRail: @Composable (() -> Unit)? = null,
    snackbarHost: @Composable () -> Unit = { SnackbarHost(remember { SnackbarHostState() }) },
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = contentColorFor(containerColor),
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    content: @Composable (PaddingValues) -> Unit
) {
    val railContent = navigationRail?.takeIf { windowSizeClass != WindowSizeClass.COMPACT }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = containerColor,
        contentColor = contentColor
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Navigation rail for medium/expanded screens
            if (railContent != null) {
                Box(
                    modifier = Modifier.fillMaxHeight()
                ) {
                    railContent()
                }
            }

            // Main content area
            Scaffold(
                modifier = Modifier.weight(1f),
                topBar = topBar,
                bottomBar = if (railContent != null) {{}} else bottomBar,
                snackbarHost = snackbarHost,
                floatingActionButton = floatingActionButton,
                floatingActionButtonPosition = floatingActionButtonPosition,
                containerColor = Color.Transparent,
                contentWindowInsets = contentWindowInsets
            ) { paddingValues ->
                content(paddingValues)
            }
        }
    }
}

/**
 * Adaptive scaffold with built-in navigation support.
 *
 * @param windowSizeClass The current window size class.
 * @param modifier Modifier to be applied.
 * @param topBar Top bar content.
 * @param navigationBar Navigation bar for compact screens.
 * @param navigationRail Navigation rail for medium/expanded screens.
 * @param snackbarHost Snackbar host.
 * @param floatingActionButton FAB content.
 * @param content Window content.
 */
@Composable
fun AdaptiveScaffold(
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    navigationBar: @Composable () -> Unit = {},
    navigationRail: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = { SnackbarHost(remember { SnackbarHostState() }) },
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val useNavigationRail = windowSizeClass != WindowSizeClass.COMPACT

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Navigation rail
            AnimatedVisibility(
                visible = useNavigationRail,
                modifier = Modifier.fillMaxHeight()
            ) {
                navigationRail()
            }

            // Main scaffold
            Scaffold(
                modifier = Modifier.weight(1f),
                topBar = topBar,
                bottomBar = { if (!useNavigationRail) navigationBar() },
                snackbarHost = snackbarHost,
                floatingActionButton = floatingActionButton,
                containerColor = Color.Transparent
            ) { paddingValues ->
                content(paddingValues)
            }
        }
    }
}

/**
 * Two-pane scaffold for list-detail layouts.
 *
 * @param windowSizeClass The current window size class.
 * @param listPane List pane content.
 * @param detailPane Detail pane content.
 * @param modifier Modifier to be applied.
 * @param showDetail Whether to show the detail pane (on compact screens).
 */
@Composable
fun TwoPaneScaffold(
    windowSizeClass: WindowSizeClass,
    listPane: @Composable () -> Unit,
    detailPane: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    showDetail: Boolean = false
) {
    val isExpanded = windowSizeClass == WindowSizeClass.EXPANDED

    Row(modifier = modifier.fillMaxSize()) {
        // List pane
        Surface(
            modifier = if (isExpanded || !showDetail) {
                Modifier
                    .fillMaxHeight()
                    .weight(1f)
            } else {
                Modifier
                    .fillMaxHeight()
                    .weight(0f)
            },
            color = MaterialTheme.colorScheme.background
        ) {
            listPane()
        }

        // Divider for expanded
        if (isExpanded) {
            Spacer(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .padding(vertical = ComponentSpacing.md)
            )
        }

        // Detail pane
        AnimatedVisibility(
            visible = isExpanded || showDetail,
            modifier = if (isExpanded) {
                Modifier
                    .fillMaxHeight()
                    .weight(1.5f)
            } else {
                Modifier
                    .fillMaxHeight()
                    .weight(if (showDetail) 1f else 0f)
            }
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                detailPane()
            }
        }
    }
}

/**
 * Vault snackbar visuals with custom styling.
 */
class VaultSnackbarVisuals(
    override val message: String,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
    override val actionLabel: String? = null,
    override val withDismissAction: Boolean = true,
    val isError: Boolean = false
) : SnackbarVisuals

/**
 * Custom snackbar host with vault styling.
 *
 * @param hostState Snackbar host state.
 * @param modifier Modifier to be applied.
 */
@Composable
fun VaultSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier.padding(ComponentSpacing.md)
    ) { data ->
        val visuals = data.visuals as? VaultSnackbarVisuals
        
        Snackbar(
            snackbarData = data,
            containerColor = if (visuals?.isError == true) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.inverseSurface
            },
            contentColor = if (visuals?.isError == true) {
                MaterialTheme.colorScheme.onErrorContainer
            } else {
                MaterialTheme.colorScheme.inverseOnSurface
            },
            actionColor = if (visuals?.isError == true) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.inversePrimary
            }
        )
    }
}

/**
 * Helper to show a snackbar with vault styling.
 *
 * @param snackbarHostState Snackbar host state.
 * @param message Message to display.
 * @param actionLabel Optional action label.
 * @param duration Snackbar duration.
 * @param isError Whether this is an error snackbar.
 * @return Snackbar result.
 */
suspend fun showVaultSnackbar(
    snackbarHostState: SnackbarHostState,
    message: String,
    actionLabel: String? = null,
    duration: SnackbarDuration = SnackbarDuration.Short,
    isError: Boolean = false
): SnackbarResult {
    return snackbarHostState.showSnackbar(
        VaultSnackbarVisuals(
            message = message,
            duration = duration,
            actionLabel = actionLabel,
            withDismissAction = true,
            isError = isError
        )
    )
}

/**
 * Screen with automatic padding and content.
 *
 * @param modifier Modifier to be applied.
 * @param contentPadding Padding to apply to content.
 * @param content Screen content.
 */
@Composable
fun VaultScreenContent(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(ComponentSpacing.screenHorizontal),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .consumeWindowInsets(contentPadding)
    ) {
        content()
    }
}
