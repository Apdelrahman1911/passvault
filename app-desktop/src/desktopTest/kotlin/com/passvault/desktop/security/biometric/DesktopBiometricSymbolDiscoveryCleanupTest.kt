package com.passvault.desktop.security.biometric

import kotlinx.coroutines.CancellationException
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

class DesktopBiometricSymbolDiscoveryCleanupTest {
    @Test
    fun `successful symbol discovery leaves the library open`() {
        val events = mutableListOf<String>()

        requireLocalizedBiometricPromptSymbols(closeOnFailure = { events += "close" }) { symbol ->
            events += symbol
        }

        assertEquals(listOf(ENROLL_SYMBOL, RETRIEVE_SYMBOL), events)
    }

    @Test
    fun `either missing symbol closes once after mapping to unavailable`() {
        listOf(ENROLL_SYMBOL, RETRIEVE_SYMBOL).forEach { missing ->
            val events = mutableListOf<String>()
            val failure = assertFailsWith<DesktopBiometricBridgeException.NotAvailable> {
                requireLocalizedBiometricPromptSymbols(closeOnFailure = { events += "close" }) { symbol ->
                    events += symbol
                    if (symbol == missing) throw UnsatisfiedLinkError("Synthetic missing symbol")
                }
            }

            assertSame(DesktopBiometricBridgeException.NotAvailable, failure)
            val expected = if (missing == ENROLL_SYMBOL) {
                listOf(ENROLL_SYMBOL, "close")
            } else {
                listOf(ENROLL_SYMBOL, RETRIEVE_SYMBOL, "close")
            }
            assertEquals(expected, events)
        }
    }

    @Test
    fun `resolver exceptions close once and retain exception identity`() {
        listOf(IllegalStateException("Synthetic resolver failure"), IOException("Synthetic I O failure"))
            .forEach { originalFailure ->
                val events = mutableListOf<String>()
                val failure = assertFailsWith<Exception> {
                    requireLocalizedBiometricPromptSymbols(closeOnFailure = { events += "close" }) { symbol ->
                        events += symbol
                        throw originalFailure
                    }
                }

                assertSame(originalFailure, failure)
                assertEquals(listOf(ENROLL_SYMBOL, "close"), events)
            }
    }

    @Test
    fun `cancelled discovery closes once and propagates the same cancellation`() {
        val originalFailure = CancellationException("Synthetic cancelled discovery")
        val events = mutableListOf<String>()
        val failure = assertFailsWith<CancellationException> {
            requireLocalizedBiometricPromptSymbols(closeOnFailure = { events += "close" }) { symbol ->
                events += symbol
                throw originalFailure
            }
        }

        assertSame(originalFailure, failure)
        assertEquals(listOf(ENROLL_SYMBOL, "close"), events)
    }

    @Test
    fun `non exception errors do not trigger cleanup or get masked`() {
        listOf(AssertionError("Synthetic assertion failure"), LinkageError("Synthetic linkage failure"))
            .forEach { originalFailure ->
                var closeCalls = 0
                val resolved = mutableListOf<String>()
                val failure = assertFailsWith<Error> {
                    requireLocalizedBiometricPromptSymbols(
                        closeOnFailure = {
                            closeCalls += 1
                            error("Cleanup must not run for an Error")
                        },
                    ) { symbol ->
                        resolved += symbol
                        throw originalFailure
                    }
                }

                assertSame(originalFailure, failure)
                assertEquals(0, closeCalls)
                assertEquals(listOf(ENROLL_SYMBOL), resolved)
            }
    }

    @Test
    fun `cleanup failures retain precedence without adding suppressed discovery failures`() {
        val discoveryFailures = listOf(
            IllegalStateException("Synthetic discovery failure"),
            CancellationException("Synthetic cancelled discovery"),
            UnsatisfiedLinkError("Synthetic missing symbol"),
        )
        val cleanupFailures = listOf(
            IllegalArgumentException("Synthetic cleanup failure"),
            AssertionError("Synthetic cleanup error"),
            UnsatisfiedLinkError("Synthetic cleanup linkage failure"),
        )
        discoveryFailures.forEach { discoveryFailure ->
            cleanupFailures.forEach { cleanupFailure ->
                val events = mutableListOf<String>()
                val failure = assertFailsWith<Throwable> {
                    requireLocalizedBiometricPromptSymbols(
                        closeOnFailure = {
                            events += "close"
                            throw cleanupFailure
                        },
                    ) { symbol ->
                        events += symbol
                        throw discoveryFailure
                    }
                }

                assertSame(cleanupFailure, failure)
                assertEquals(listOf(ENROLL_SYMBOL, "close"), events)
                assertTrue(failure.suppressedExceptions.isEmpty())
            }
        }
    }

    private companion object {
        const val ENROLL_SYMBOL = "pv_bio_enroll_localized"
        const val RETRIEVE_SYMBOL = "pv_bio_retrieve_localized"
    }
}
