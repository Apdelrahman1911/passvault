package com.passvault.shared.security

import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composition
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.Snapshot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class UserActivityCompositionTest {
    @Test
    fun `activity bursts do not invalidate the owning navigation composition`() = runTest {
        val frameClock = BroadcastFrameClock()
        val recomposer = Recomposer(coroutineContext + frameClock)
        val recomposerJob = launch(frameClock) { recomposer.runRecomposeAndApplyChanges() }
        val composition = Composition(UnitApplier(), recomposer)
        val signal = UserActivitySignal(MutableCompositionClock())
        val legacyComposeGeneration = mutableIntStateOf(0)
        var compositions = 0
        var observedLegacyGeneration = -1
        composition.setContent {
            remember(signal) { signal }
            observedLegacyGeneration = legacyComposeGeneration.intValue
            compositions++
        }
        runCurrent()
        frameClock.sendFrame(0L)
        runCurrent()
        val settledCompositionCount = compositions

        repeat(POINTER_EVENT_COUNT) { eventIndex ->
            signal.recordActivity()
            frameClock.sendFrame(eventIndex + 1L)
            runCurrent()
        }

        assertEquals(settledCompositionCount, compositions)

        // Control: the removed implementation wrote a Compose state value for
        // each event. Driving the same frame cadence proves the harness would
        // observe one owning-composition invalidation per pointer event.
        repeat(POINTER_EVENT_COUNT) { eventIndex ->
            legacyComposeGeneration.intValue++
            Snapshot.sendApplyNotifications()
            runCurrent()
            frameClock.sendFrame((POINTER_EVENT_COUNT + eventIndex + 1).toLong())
            runCurrent()
        }
        assertEquals(POINTER_EVENT_COUNT + settledCompositionCount, compositions)
        assertEquals(POINTER_EVENT_COUNT, observedLegacyGeneration)
        composition.dispose()
        recomposer.cancel()
        recomposerJob.cancelAndJoin()
    }

    private companion object {
        const val POINTER_EVENT_COUNT = 1_000
    }
}

private class UnitApplier : AbstractApplier<Unit>(Unit) {
    override fun insertTopDown(index: Int, instance: Unit) = Unit

    override fun insertBottomUp(index: Int, instance: Unit) = Unit

    override fun remove(index: Int, count: Int) = Unit

    override fun move(from: Int, to: Int, count: Int) = Unit

    override fun onClear() = Unit
}

private class MutableCompositionClock : AutoLockClock {
    private var now = 0L

    override fun nowMillis(): Long = now++
}
