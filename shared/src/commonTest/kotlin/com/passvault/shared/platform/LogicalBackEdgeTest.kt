package com.passvault.shared.platform

import androidx.compose.ui.unit.LayoutDirection
import androidx.navigationevent.NavigationEvent
import kotlin.test.Test
import kotlin.test.assertEquals

class LogicalBackEdgeTest {
    @Test
    fun `LTR uses the physical left edge`() {
        assertEquals(NavigationEvent.EDGE_LEFT, logicalBackSwipeEdge(LayoutDirection.Ltr))
    }

    @Test
    fun `RTL uses the physical right edge`() {
        assertEquals(NavigationEvent.EDGE_RIGHT, logicalBackSwipeEdge(LayoutDirection.Rtl))
    }
}
