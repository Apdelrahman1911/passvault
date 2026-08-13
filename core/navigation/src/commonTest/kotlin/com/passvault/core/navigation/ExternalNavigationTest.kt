package com.passvault.core.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class ExternalNavigationTest {
    @Test
    fun `parser accepts only the documented internal grammar`() {
        val credential = ExternalNavigationParser.parse(
            raw("credential", "00000000-0000-0000-0000-00000000000A"),
        )
        val accepted = assertIs<ExternalNavigationParseResult.Accepted>(credential)
        assertEquals(
            ExternalNavigationIntent.Credential("00000000-0000-0000-0000-00000000000a"),
            accepted.envelope.intent,
        )

        assertEquals(
            ExternalNavigationIntent.SecuritySettings,
            assertIs<ExternalNavigationParseResult.Accepted>(raw("settings", "security").parse()).envelope.intent,
        )
        assertEquals(
            ExternalNavigationIntent.Import,
            assertIs<ExternalNavigationParseResult.Accepted>(raw("backup", "import").parse()).envelope.intent,
        )
    }

    @Test
    fun `parser rejects malformed unsupported and oversized input`() {
        assertRejected(ExternalNavigationParseError.EmptyPath, raw())
        assertRejected(ExternalNavigationParseError.MalformedSegment, raw("settings/../../vault"))
        assertRejected(ExternalNavigationParseError.MalformedSegment, raw("credential", "%2e%2e"))
        assertRejected(ExternalNavigationParseError.InvalidIdentifier, raw("credential", "not-a-uuid"))
        assertRejected(ExternalNavigationParseError.UnsupportedDestination, raw("unknown"))
        assertRejected(
            ExternalNavigationParseError.InvalidDeliveryId,
            raw("settings").copy(deliveryId = "bad delivery id"),
        )
    }

    @Test
    fun `dispatcher is conflated has no replay backlog and consumes by delivery id`() {
        val dispatcher = ExternalNavigationDispatcher()
        val first = raw("settings").copy(deliveryId = "first")
        val second = raw("generator").copy(deliveryId = "second")

        dispatcher.submit(first)
        dispatcher.submit(second)
        assertEquals(second, dispatcher.pending.value)
        dispatcher.consume("first")
        assertEquals(second, dispatcher.pending.value)
        dispatcher.consume("second")
        assertNull(dispatcher.pending.value)
    }

    private fun RawExternalNavigationInput.parse(): ExternalNavigationParseResult =
        ExternalNavigationParser.parse(this)

    private fun assertRejected(
        expected: ExternalNavigationParseError,
        input: RawExternalNavigationInput,
    ) {
        assertEquals(expected, assertIs<ExternalNavigationParseResult.Rejected>(input.parse()).error)
    }

    private fun raw(vararg segments: String) = RawExternalNavigationInput(
        deliveryId = "test-delivery",
        source = ExternalNavigationSource.URL,
        pathSegments = segments.toList(),
    )
}
