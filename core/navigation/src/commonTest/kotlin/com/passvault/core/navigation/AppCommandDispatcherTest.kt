package com.passvault.core.navigation

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class AppCommandDispatcherTest {
    @Test
    fun `commands emitted before a host subscribes are never replayed`() = runTest {
        val dispatcher = AppCommandDispatcher()
        dispatcher.dispatch(AppCommand.IMPORT)
        val observed = Channel<AppCommand>(Channel.UNLIMITED)
        backgroundScope.launch { dispatcher.commands.collect(observed::send) }
        runCurrent()

        assertNull(observed.tryReceive().getOrNull())
        dispatcher.dispatch(AppCommand.EXPORT)
        runCurrent()
        assertEquals(AppCommand.EXPORT, observed.receive())
    }

    @Test
    fun `high-volume input remains bounded and does not block its producer`() = runTest {
        val dispatcher = AppCommandDispatcher()
        val observed = Channel<AppCommand>(Channel.UNLIMITED)
        backgroundScope.launch { dispatcher.commands.collect(observed::send) }
        runCurrent()

        repeat(100_000) { index ->
            dispatcher.dispatch(
                if (index == 99_999) AppCommand.CLEAR_CLIPBOARD else AppCommand.SEARCH,
            )
        }
        runCurrent()

        val delivered = buildList {
            while (true) add(observed.tryReceive().getOrNull() ?: break)
        }
        assertTrue(delivered.size <= 16, "Buffered ${delivered.size} commands")
        assertEquals(AppCommand.CLEAR_CLIPBOARD, delivered.last())
    }
}
