package com.passvault.shared.platform

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InvalidatedResourceTest {
    @Test
    fun `invalidates resource after a successful operation`() = runTest {
        val resource = FakeResource()

        val result = withInvalidatedResource(resource, FakeResource::invalidate) { "result" }

        assertEquals("result", result)
        assertEquals(1, resource.invalidationCount)
    }

    @Test
    fun `invalidates resource when the operation fails`() = runTest {
        val resource = FakeResource()

        val exception = try {
            withInvalidatedResource(resource, FakeResource::invalidate) {
                throw IllegalStateException("expected")
            }
            null
        } catch (error: IllegalStateException) {
            error
        }

        assertEquals("expected", exception?.message)
        assertEquals(1, resource.invalidationCount)
    }

    @Test
    fun `invalidates resource when the operation is cancelled`() = runTest {
        val resource = FakeResource()
        val operation = launch {
            withInvalidatedResource(resource, FakeResource::invalidate) {
                awaitCancellation()
            }
        }
        yield()

        operation.cancelAndJoin()

        assertTrue(operation.isCancelled)
        assertEquals(1, resource.invalidationCount)
    }

    private class FakeResource {
        var invalidationCount: Int = 0
            private set

        fun invalidate() {
            invalidationCount += 1
        }
    }
}
