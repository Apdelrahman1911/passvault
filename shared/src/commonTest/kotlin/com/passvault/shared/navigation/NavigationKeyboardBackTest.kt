package com.passvault.shared.navigation

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NavigationKeyboardBackTest {
    @Test
    fun `Escape and Back request application Back only on key down`() {
        assertTrue(isApplicationBackKey(Key.Escape, KeyEventType.KeyDown))
        assertTrue(isApplicationBackKey(Key.Back, KeyEventType.KeyDown))
        assertFalse(isApplicationBackKey(Key.Escape, KeyEventType.KeyUp))
        assertFalse(isApplicationBackKey(Key.Enter, KeyEventType.KeyDown))
    }
}
