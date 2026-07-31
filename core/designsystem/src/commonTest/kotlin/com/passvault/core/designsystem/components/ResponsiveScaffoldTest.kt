package com.passvault.core.designsystem.components

import kotlin.test.Test
import kotlin.test.assertEquals

class ResponsiveScaffoldTest {

    @Test
    fun `window size classes change at the documented boundaries`() {
        assertEquals(WindowSizeClass.COMPACT, calculateWindowSizeClass(0))
        assertEquals(WindowSizeClass.COMPACT, calculateWindowSizeClass(599))
        assertEquals(WindowSizeClass.MEDIUM, calculateWindowSizeClass(600))
        assertEquals(WindowSizeClass.MEDIUM, calculateWindowSizeClass(839))
        assertEquals(WindowSizeClass.EXPANDED, calculateWindowSizeClass(840))
        assertEquals(WindowSizeClass.EXPANDED, calculateWindowSizeClass(1_920))
    }
}
