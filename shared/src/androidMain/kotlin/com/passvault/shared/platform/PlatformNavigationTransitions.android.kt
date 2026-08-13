package com.passvault.shared.platform

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.defaultPopTransitionSpec
import androidx.navigation3.ui.defaultPredictivePopTransitionSpec
import androidx.navigation3.ui.defaultTransitionSpec
import androidx.navigationevent.NavigationEvent
import com.passvault.core.navigation.PassVaultRoute

internal actual fun platformTransitionSpec(
    layoutDirection: LayoutDirection,
): AnimatedContentTransitionScope<Scene<PassVaultRoute>>.() -> ContentTransform =
    defaultTransitionSpec()

internal actual fun platformPopTransitionSpec(
    layoutDirection: LayoutDirection,
): AnimatedContentTransitionScope<Scene<PassVaultRoute>>.() -> ContentTransform =
    defaultPopTransitionSpec()

internal actual fun platformPredictivePopTransitionSpec(
    layoutDirection: LayoutDirection,
): AnimatedContentTransitionScope<Scene<PassVaultRoute>>.(
    @NavigationEvent.SwipeEdge Int,
) -> ContentTransform = defaultPredictivePopTransitionSpec()
