package com.passvault.shared.platform

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigationevent.NavigationEvent
import androidx.navigation3.scene.Scene
import com.passvault.core.navigation.PassVaultRoute

/**
 * Kira's Navigation 2 iOS defaults settle in 200 ms, while Navigation 3 UI 1.1.1 defaults to
 * 500 ms. Keep Navigation 3 as the transition owner, but use the shorter verified iOS base so a
 * velocity-qualified short flick does not retain a long remaining animation.
 */
internal const val IOS_NAVIGATION_TRANSITION_DURATION_MILLIS = 200

private val iosRegularNavigationEasing = CubicBezierEasing(0.2833f, 0.99f, 0.31833f, 0.99f)

/** Navigation 3's regular iOS visual curve with PassVault's verified base duration. */
internal fun <T> iosRegularNavigationTween(): TweenSpec<T> =
    iosNavigationTween(iosRegularNavigationEasing)

/** Linear progress is required while Navigation 3 seeks the transition from the user's finger. */
internal fun <T> iosPredictiveNavigationTween(): TweenSpec<T> = iosNavigationTween(LinearEasing)

private fun <T> iosNavigationTween(easing: Easing): TweenSpec<T> = tween(
    durationMillis = IOS_NAVIGATION_TRANSITION_DURATION_MILLIS,
    easing = easing,
)

internal enum class HorizontalNavigationMotion {
    LEFT,
    RIGHT,
}

internal fun forwardNavigationMotion(layoutDirection: LayoutDirection): HorizontalNavigationMotion =
    when (layoutDirection) {
        LayoutDirection.Ltr -> HorizontalNavigationMotion.LEFT
        LayoutDirection.Rtl -> HorizontalNavigationMotion.RIGHT
    }

internal fun backNavigationMotion(layoutDirection: LayoutDirection): HorizontalNavigationMotion =
    when (layoutDirection) {
        LayoutDirection.Ltr -> HorizontalNavigationMotion.RIGHT
        LayoutDirection.Rtl -> HorizontalNavigationMotion.LEFT
    }

internal fun predictiveBackNavigationMotion(
    @NavigationEvent.SwipeEdge swipeEdge: Int,
    layoutDirection: LayoutDirection,
): HorizontalNavigationMotion = when (swipeEdge) {
    NavigationEvent.EDGE_LEFT -> HorizontalNavigationMotion.RIGHT
    NavigationEvent.EDGE_RIGHT -> HorizontalNavigationMotion.LEFT
    else -> backNavigationMotion(layoutDirection)
}

internal data class PlatformNavigationTransitionSpecs(
    val forward: AnimatedContentTransitionScope<Scene<PassVaultRoute>>.() -> ContentTransform,
    val pop: AnimatedContentTransitionScope<Scene<PassVaultRoute>>.() -> ContentTransform,
    val predictivePop: AnimatedContentTransitionScope<Scene<PassVaultRoute>>.(
        @NavigationEvent.SwipeEdge Int,
    ) -> ContentTransform,
)

internal fun platformNavigationTransitionSpecs(
    layoutDirection: LayoutDirection,
): PlatformNavigationTransitionSpecs = PlatformNavigationTransitionSpecs(
    forward = platformTransitionSpec(layoutDirection),
    pop = platformPopTransitionSpec(layoutDirection),
    predictivePop = platformPredictivePopTransitionSpec(layoutDirection),
)

/** Supplies platform motion without changing the shared navigation state or gesture handling. */
internal expect fun platformTransitionSpec(
    layoutDirection: LayoutDirection,
): AnimatedContentTransitionScope<Scene<PassVaultRoute>>.() -> ContentTransform

/** Supplies platform Back motion without changing the shared Back policy. */
internal expect fun platformPopTransitionSpec(
    layoutDirection: LayoutDirection,
): AnimatedContentTransitionScope<Scene<PassVaultRoute>>.() -> ContentTransform

/** Supplies the transform that Navigation 3 seeks directly from interactive gesture progress. */
internal expect fun platformPredictivePopTransitionSpec(
    layoutDirection: LayoutDirection,
): AnimatedContentTransitionScope<Scene<PassVaultRoute>>.(
    @NavigationEvent.SwipeEdge Int,
) -> ContentTransform

/** Logical start edge used only to mirror non-interactive iOS toolbar/programmatic pops. */
internal fun logicalBackSwipeEdge(layoutDirection: LayoutDirection): Int = when (layoutDirection) {
    LayoutDirection.Ltr -> NavigationEvent.EDGE_LEFT
    LayoutDirection.Rtl -> NavigationEvent.EDGE_RIGHT
}
