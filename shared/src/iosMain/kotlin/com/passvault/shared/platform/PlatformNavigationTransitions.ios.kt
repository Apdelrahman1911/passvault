package com.passvault.shared.platform

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.unveilIn
import androidx.compose.animation.veilOut
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigationevent.NavigationEvent
import androidx.navigation3.scene.Scene
import com.passvault.core.navigation.PassVaultRoute

@OptIn(ExperimentalAnimationApi::class)
internal actual fun platformTransitionSpec(
    layoutDirection: LayoutDirection,
): AnimatedContentTransitionScope<Scene<PassVaultRoute>>.() -> ContentTransform = {
    val towards = forwardNavigationMotion(layoutDirection).slideDirection()
    ContentTransform(
        targetContentEnter = slideIntoContainer(
            towards = towards,
            animationSpec = iosRegularNavigationTween(),
        ),
        initialContentExit = slideOutOfContainer(
            towards = towards,
            targetOffset = { it / 4 },
            animationSpec = iosRegularNavigationTween(),
        ) + veilOut(animationSpec = iosRegularNavigationTween()),
    )
}

@OptIn(ExperimentalAnimationApi::class)
internal actual fun platformPopTransitionSpec(
    layoutDirection: LayoutDirection,
): AnimatedContentTransitionScope<Scene<PassVaultRoute>>.() -> ContentTransform = {
    regularPopTransform(backNavigationMotion(layoutDirection).slideDirection())
}

@OptIn(ExperimentalAnimationApi::class)
internal actual fun platformPredictivePopTransitionSpec(
    layoutDirection: LayoutDirection,
): AnimatedContentTransitionScope<Scene<PassVaultRoute>>.(
    @NavigationEvent.SwipeEdge Int,
) -> ContentTransform = { swipeEdge ->
    // Linear transition progress keeps the scene directly tied to the finger while dragging.
    predictivePopTransform(
        predictiveBackNavigationMotion(swipeEdge, layoutDirection).slideDirection(),
    )
}

private fun HorizontalNavigationMotion.slideDirection(): AnimatedContentTransitionScope.SlideDirection =
    when (this) {
        HorizontalNavigationMotion.LEFT -> AnimatedContentTransitionScope.SlideDirection.Left
        HorizontalNavigationMotion.RIGHT -> AnimatedContentTransitionScope.SlideDirection.Right
    }

@OptIn(ExperimentalAnimationApi::class)
private fun AnimatedContentTransitionScope<Scene<PassVaultRoute>>.regularPopTransform(
    towards: AnimatedContentTransitionScope.SlideDirection,
): ContentTransform = ContentTransform(
    targetContentEnter = slideIntoContainer(
        towards = towards,
        initialOffset = { it / 4 },
        animationSpec = iosRegularNavigationTween(),
    ) + unveilIn(animationSpec = iosRegularNavigationTween()),
    initialContentExit = slideOutOfContainer(
        towards = towards,
        animationSpec = iosRegularNavigationTween(),
    ),
)

@OptIn(ExperimentalAnimationApi::class)
private fun AnimatedContentTransitionScope<Scene<PassVaultRoute>>.predictivePopTransform(
    towards: AnimatedContentTransitionScope.SlideDirection,
): ContentTransform = ContentTransform(
    targetContentEnter = slideIntoContainer(
        towards = towards,
        initialOffset = { it / 4 },
        animationSpec = iosPredictiveNavigationTween(),
    ) + unveilIn(animationSpec = iosPredictiveNavigationTween()),
    initialContentExit = slideOutOfContainer(
        towards = towards,
        animationSpec = iosPredictiveNavigationTween(),
    ),
)
