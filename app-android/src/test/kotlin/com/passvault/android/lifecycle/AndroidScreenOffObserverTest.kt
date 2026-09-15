package com.passvault.android.lifecycle

import kotlin.test.Test
import kotlin.test.assertEquals

class AndroidScreenOffObserverTest {
    @Test
    fun observationIsIdempotentAndCanRestartAfterStop() {
        var registrations = 0
        var closes = 0
        var observed: (() -> Unit)? = null
        val observer = AndroidScreenOffObserver { callback ->
            registrations++
            observed = callback
            AutoCloseable { closes++ }
        }
        var screenOffEvents = 0

        assertEquals(true, observer.start { screenOffEvents++ })
        assertEquals(true, observer.start { screenOffEvents += 100 })
        observed?.invoke()
        observer.stop()
        observer.stop()

        assertEquals(1, registrations)
        assertEquals(1, screenOffEvents)
        assertEquals(1, closes)

        assertEquals(true, observer.start { screenOffEvents++ })
        observed?.invoke()
        observer.stop()

        assertEquals(2, registrations)
        assertEquals(2, screenOffEvents)
        assertEquals(2, closes)
    }

    @Test
    fun failedRegistrationIsReportedAndCanBeRetried() {
        var attempts = 0
        var observed: (() -> Unit)? = null
        val observer = AndroidScreenOffObserver { callback ->
            attempts++
            if (attempts == 1) error("simulated registration failure")
            observed = callback
            AutoCloseable { }
        }
        var screenOffEvents = 0

        assertEquals(false, observer.start { screenOffEvents++ })
        assertEquals(true, observer.start { screenOffEvents++ })
        observed?.invoke()

        assertEquals(2, attempts)
        assertEquals(1, screenOffEvents)
    }
}
