package com.passvault.core.designsystem.tokens

import androidx.compose.animation.core.*
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * PassVault motion tokens.
 * Animation durations, easings, and specs for consistent motion.
 */

object MotionDuration {
    /**
     * Ultra short duration for micro-interactions.
     * Used for: Ripple effects, small state changes.
     */
    val instant: Duration = 50.milliseconds

    /**
     * Short duration for quick transitions.
     * Used for: Quick feedback, hover states.
     */
    val fast: Duration = 150.milliseconds

    /**
     * Standard duration for most transitions.
     * Used for: Button presses, simple reveals.
     */
    val normal: Duration = 200.milliseconds

    /**
     * Medium duration for more complex transitions.
     * Used for: Sheet opens, dialog transitions.
     */
    val medium: Duration = 300.milliseconds

    /**
     * Emphasized duration for important transitions.
     * Used for: Screen transitions, page opens.
     */
    val slow: Duration = 400.milliseconds

    /**
     * Long duration for emphasis.
     * Used for: Complex choreography, hero animations.
     */
    val emphasis: Duration = 500.milliseconds

    /**
     * Extra long for special emphasis.
     * Used for: Onboarding, empty states.
     */
    val long: Duration = 600.milliseconds

    /**
     * Stagger delay between items.
     */
    val stagger: Duration = 50.milliseconds
}

/**
 * Easing curves for smooth animations.
 */
object MotionEasing {
    /**
     * Standard easing - decelerate.
     * Used for: Elements entering the screen.
     */
    val standard: Easing = FastOutSlowInEasing

    /**
     * Accelerate easing.
     * Used for: Elements leaving the screen.
     */
    val accelerate: Easing = FastOutLinearInEasing

    /**
     * Decelerate easing.
     * Used for: Elements entering with emphasis.
     */
    val decelerate: Easing = LinearOutSlowInEasing

    /**
     * Linear easing.
     * Used for: Continuous animations, progress.
     */
    val linear: Easing = LinearEasing

    /**
     * Emphasized easing - Material 3 standard.
     * Used for: Emphasized motion, FAB transformations.
     */
    val emphasized: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

    /**
     * Emphasized decelerate.
     * Used for: Large surfaces entering.
     */
    val emphasizedDecelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)

    /**
     * Emphasized accelerate.
     * Used for: Large surfaces leaving.
     */
    val emphasizedAccelerate: Easing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)

    /**
     * Standard decelerate.
     * Used for: Standard enter transitions.
     */
    val standardDecelerate: Easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)

    /**
     * Standard accelerate.
     * Used for: Standard exit transitions.
     */
    val standardAccelerate: Easing = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)
}

/**
 * Animation specs for common use cases.
 */
object MotionSpec {
    /**
     * Standard tween for most animations.
     */
    fun <T> tween(
        duration: Duration = MotionDuration.normal,
        easing: Easing = MotionEasing.standard
    ): AnimationSpec<T> = androidx.compose.animation.core.tween<T>(
        durationMillis = duration.inWholeMilliseconds.toInt(),
        easing = easing
    )

    /**
     * Fast animation spec.
     */
    fun <T> fast(): AnimationSpec<T> = tween<T>(MotionDuration.fast, MotionEasing.standard)

    /**
     * Medium animation spec.
     */
    fun <T> medium(): AnimationSpec<T> = tween<T>(MotionDuration.medium, MotionEasing.standard)

    /**
     * Slow animation spec.
     */
    fun <T> slow(): AnimationSpec<T> = tween<T>(MotionDuration.slow, MotionEasing.emphasized)

    /**
     * Enter animation spec.
     */
    fun <T> enter(): AnimationSpec<T> = tween<T>(MotionDuration.medium, MotionEasing.standardDecelerate)

    /**
     * Exit animation spec.
     */
    fun <T> exit(): AnimationSpec<T> = tween<T>(MotionDuration.fast, MotionEasing.standardAccelerate)

    /**
     * Spring spec for bouncy animations.
     */
    fun <T> spring(
        dampingRatio: Float = Spring.DampingRatioMediumBouncy,
        stiffness: Float = Spring.StiffnessMedium
    ): AnimationSpec<T> = spring<T>(dampingRatio = dampingRatio, stiffness = stiffness)

    /**
     * Gentle spring for subtle bounciness.
     */
    fun <T> gentleSpring(): AnimationSpec<T> = spring<T>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow
    )
}

/**
 * Spring configurations for different animation types.
 */
object MotionSpring {
    /**
     * Standard spring for responsive UI.
     */
    val standard = Spring.StiffnessMedium

    /**
     * Fast spring for quick response.
     */
    val fast = Spring.StiffnessHigh

    /**
     * Gentle spring for subtle animations.
     */
    val gentle = Spring.StiffnessLow

    /**
     * No bounce damping.
     */
    val noBounce = Spring.DampingRatioNoBouncy

    /**
     * Low bounce damping.
     */
    val lowBounce = Spring.DampingRatioLowBouncy

    /**
     * Medium bounce damping.
     */
    val mediumBounce = Spring.DampingRatioMediumBouncy

    /**
     * High bounce damping.
     */
    val highBounce = Spring.DampingRatioHighBouncy
}

/**
 * Animation values for common transitions.
 */
object MotionValues {
    /**
     * Scale values.
     */
    object Scale {
        const val hidden = 0.8f
        const val visible = 1.0f
        const val pressed = 0.95f
        const val emphasized = 1.05f
    }

    /**
     * Alpha values.
     */
    object Alpha {
        const val hidden = 0f
        const val visible = 1f
        const val disabled = 0.38f
        const val placeholder = 0.6f
        const val scrim = 0.32f
    }

    /**
     * Offset values for slide animations.
     */
    object Offset {
        const val slideDistance = 30f
        const val slideUp = -30f
        const val slideDown = 30f
        const val slideLeft = -30f
        const val slideRight = 30f
    }
}

/**
 * Helper to animate content changes.
 * Use only for simple types like String, Int, Float, etc.
 */
@Composable
fun <T> animateContentValue(
    targetValue: T,
    label: String = "ContentAnimation",
    animationSpec: AnimationSpec<T> = MotionSpec.medium()
): State<T> {
    val transition = updateTransition(targetValue, label = label)
    return transition.animateValue(
        typeConverter = androidx.compose.animation.core.TwoWayConverter(
            convertToVector = { it?.let { androidx.compose.animation.core.AnimationVector1D(0f) } ?: androidx.compose.animation.core.AnimationVector1D(0f) },
            convertFromVector = { targetValue }
        ),
        transitionSpec = { 
            val spec = animationSpec
            when (spec) {
                is FiniteAnimationSpec<T> -> spec
                else -> tween<T>(
                    durationMillis = 300,
                    easing = MotionEasing.standard
                )
            }
        },
        label = label
    ) { state -> state }
}

/**
 * Creates a transition state for enter/exit animations.
 */
fun createTransitionState(
    initialState: Boolean = false,
    targetState: Boolean = true
): MutableTransitionState<Boolean> {
    val state = MutableTransitionState(initialState)
    state.targetState = targetState
    return state
}
