package com.passvault.shared.platform

import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigationevent.NavigationEvent
import kotlin.test.Test
import kotlin.test.assertEquals

class IosNavigationTransitionPolicyTest {
    @Test
    fun `iOS navigation uses the verified Kira-like base duration`() {
        val regularSpec = iosRegularNavigationTween<Float>()
        val predictiveSpec = iosPredictiveNavigationTween<Float>()

        assertEquals(200, IOS_NAVIGATION_TRANSITION_DURATION_MILLIS)
        assertEquals(IOS_NAVIGATION_TRANSITION_DURATION_MILLIS, regularSpec.durationMillis)
        assertEquals(IOS_NAVIGATION_TRANSITION_DURATION_MILLIS, predictiveSpec.durationMillis)
        assertEquals(0, regularSpec.delay)
        assertEquals(0, predictiveSpec.delay)
        assertEquals(LinearEasing, predictiveSpec.easing)
    }

    @Test
    fun `remaining settlement is bounded by the 200 millisecond transition`() {
        val duration = IOS_NAVIGATION_TRANSITION_DURATION_MILLIS

        assertEquals(150, ((1f - 0.25f) * duration).toInt())
        assertEquals(100, ((1f - 0.50f) * duration).toInt())
        assertEquals(50, ((1f - 0.75f) * duration).toInt())
        assertEquals(50, (0.25f * duration).toInt())
    }

    @Test
    fun `regular iOS navigation mirrors push and Back in RTL`() {
        assertEquals(
            HorizontalNavigationMotion.LEFT,
            forwardNavigationMotion(LayoutDirection.Ltr),
        )
        assertEquals(
            HorizontalNavigationMotion.RIGHT,
            backNavigationMotion(LayoutDirection.Ltr),
        )
        assertEquals(
            HorizontalNavigationMotion.RIGHT,
            forwardNavigationMotion(LayoutDirection.Rtl),
        )
        assertEquals(
            HorizontalNavigationMotion.LEFT,
            backNavigationMotion(LayoutDirection.Rtl),
        )
    }

    @Test
    fun `interactive iOS Back follows the physical gesture edge`() {
        assertEquals(
            HorizontalNavigationMotion.RIGHT,
            predictiveBackNavigationMotion(NavigationEvent.EDGE_LEFT, LayoutDirection.Ltr),
        )
        assertEquals(
            HorizontalNavigationMotion.LEFT,
            predictiveBackNavigationMotion(NavigationEvent.EDGE_RIGHT, LayoutDirection.Rtl),
        )
        assertEquals(
            HorizontalNavigationMotion.LEFT,
            predictiveBackNavigationMotion(NavigationEvent.EDGE_NONE, LayoutDirection.Rtl),
        )
    }
}
