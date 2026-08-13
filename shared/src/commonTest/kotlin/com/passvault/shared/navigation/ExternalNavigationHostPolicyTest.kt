package com.passvault.shared.navigation

import com.passvault.core.domain.model.SessionId
import com.passvault.core.domain.model.VaultSessionState
import com.passvault.core.navigation.NavigationRoot
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExternalNavigationHostPolicyTest {
    private val unlocked = VaultSessionState.Unlocked(SessionId("external-navigation-test"))

    @Test
    fun `pending external navigation applies only from an active unlocked main host`() {
        assertTrue(
            shouldApplyPendingExternalNavigation(
                sessionState = unlocked,
                hostResumed = true,
                navigationRoot = NavigationRoot.MAIN,
                canLeaveForForwardNavigation = true,
            ),
        )
        assertFalse(
            shouldApplyPendingExternalNavigation(
                sessionState = VaultSessionState.Locked(),
                hostResumed = true,
                navigationRoot = NavigationRoot.MAIN,
                canLeaveForForwardNavigation = true,
            ),
        )
        assertFalse(
            shouldApplyPendingExternalNavigation(
                sessionState = unlocked,
                hostResumed = false,
                navigationRoot = NavigationRoot.MAIN,
                canLeaveForForwardNavigation = true,
            ),
        )
        assertFalse(
            shouldApplyPendingExternalNavigation(
                sessionState = unlocked,
                hostResumed = true,
                navigationRoot = NavigationRoot.AUTHENTICATION,
                canLeaveForForwardNavigation = true,
            ),
        )
    }

    @Test
    fun `dirty or blocked destination keeps the external request pending`() {
        assertFalse(
            shouldApplyPendingExternalNavigation(
                sessionState = unlocked,
                hostResumed = true,
                navigationRoot = NavigationRoot.MAIN,
                canLeaveForForwardNavigation = false,
            ),
        )
    }
}
