package com.passvault.core.crypto

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

/** Host checks for the production argument selector, not a substitute for Android32 KDF vectors. */
class RawPasswordHashArgumentTest {
    @Test
    fun `32-bit size_t is boxed as Int without changing the value`() {
        listOf(0, 8 * 1024, 64 * 1024 * 1024, Int.MAX_VALUE).forEach { limit ->
            val argument = passwordHashMemoryLimitArgument(limit, sizeTBytes = 4)
            assertEquals(limit, assertIs<Int>(argument))
        }
    }

    @Test
    fun `64-bit size_t is boxed as Long including LLP64 targets`() {
        listOf(0, 8 * 1024, 64 * 1024 * 1024, Int.MAX_VALUE).forEach { limit ->
            val argument = passwordHashMemoryLimitArgument(limit, sizeTBytes = 8)
            assertEquals(limit.toLong(), assertIs<Long>(argument))
        }
    }

    @Test
    fun `unsupported size_t widths fail instead of guessing an ABI`() {
        listOf(0, 2, 16).forEach { width ->
            assertFailsWith<IllegalStateException> {
                passwordHashMemoryLimitArgument(64 * 1024 * 1024, sizeTBytes = width)
            }
        }
    }

    @Test
    fun `negative limits are not marshalled as an unsigned allocation size`() {
        listOf(4, 8).forEach { width ->
            assertFailsWith<IllegalArgumentException> {
                passwordHashMemoryLimitArgument(-1, sizeTBytes = width)
            }
        }
    }
}
