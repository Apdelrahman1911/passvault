package com.passvault.shared.navigation

import androidx.navigation3.runtime.NavEntry
import com.passvault.core.navigation.AppNavigator
import com.passvault.core.navigation.AuthRoute
import com.passvault.core.navigation.BackDisposition
import com.passvault.core.navigation.NavigationMutation
import com.passvault.core.navigation.PassVaultNavigationState
import com.passvault.core.navigation.PassVaultRoute
import com.passvault.core.navigation.TopLevelDestination
import com.passvault.core.navigation.VaultRoute
import com.passvault.core.navigation.rootRoute
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class NavigationBackCoordinatorTest {
    @Test
    fun `blocked Back neither reveals nor mutates the previous destination`() {
        val navigator = unlockedNavigatorWithDetail()
        val coordinator = NavigationBackCoordinator(navigator)
        val token = navigator.currentToken()
        coordinator.register(BackRegistration(token, BackDisposition.Blocked, {}, {}, true))

        assertTrue(coordinator.requestBack())
        assertEquals(VaultRoute.CredentialDetail(CREDENTIAL_ID), navigator.state.currentRoute())
        assertIs<NavigationMutation.Rejected>(coordinator.completeInteractivePop())
    }

    @Test
    fun `in-place Back runs local cleanup exactly once without popping`() {
        val navigator = unlockedNavigatorWithDetail()
        val coordinator = NavigationBackCoordinator(navigator)
        val token = navigator.currentToken()
        var handled = 0
        coordinator.register(
            BackRegistration(token, BackDisposition.HandleInPlace, { handled++ }, {}, true),
        )

        assertTrue(coordinator.requestBack())
        assertEquals(1, handled)
        assertEquals(2, navigator.state.activeStack().size)
    }

    @Test
    fun `authorized interactive completion pops once and rejects stale duplicate completion`() {
        val navigator = unlockedNavigatorWithDetail()
        val coordinator = NavigationBackCoordinator(navigator)
        val token = navigator.currentToken()
        var cleanup = 0
        coordinator.register(BackRegistration(token, BackDisposition.PopNow, {}, { cleanup++ }, false))

        assertEquals(NavigationMutation.Applied, coordinator.completeInteractivePop())
        assertEquals(1, cleanup)
        assertEquals(listOf(VaultRoute.Vault), navigator.state.activeStack())
        assertIs<NavigationMutation.Rejected>(coordinator.completeInteractivePop())
    }

    @Test
    fun `application-root Back is not consumed`() {
        val navigator = unlockedNavigator()
        assertFalse(NavigationBackCoordinator(navigator).requestBack())
    }

    @Test
    fun `forward navigation fails closed until a guarded route registers its policy`() {
        val navigator = unlockedNavigatorWithDetail()
        val coordinator = NavigationBackCoordinator(navigator)

        assertFalse(coordinator.canLeaveForForwardNavigation())

        val token = navigator.currentToken()
        coordinator.register(BackRegistration(token, BackDisposition.PopNow, {}, {}, false))
        assertTrue(coordinator.canLeaveForForwardNavigation())
    }

    @Test
    fun `forward navigation obeys the active destination leave policy`() {
        val navigator = unlockedNavigatorWithDetail()
        val coordinator = NavigationBackCoordinator(navigator)
        val token = navigator.currentToken()

        coordinator.register(BackRegistration(token, BackDisposition.HandleInPlace, {}, {}, true))
        assertFalse(coordinator.canLeaveForForwardNavigation())

        coordinator.register(BackRegistration(token, BackDisposition.HandleInPlace, {}, {}, false))
        assertTrue(coordinator.canLeaveForForwardNavigation())

        coordinator.register(BackRegistration(token, BackDisposition.Blocked, {}, {}, false))
        assertFalse(coordinator.canLeaveForForwardNavigation())
    }

    @Test
    fun `blocked and in-place policies expose only the current entry to interactive Back`() {
        val entries = listOf(
            NavEntry<PassVaultRoute>(VaultRoute.Vault) {},
            NavEntry<PassVaultRoute>(VaultRoute.CredentialDetail(CREDENTIAL_ID)) {},
        )

        assertEquals(entries, entriesAllowedByBackPolicy(entries, BackDisposition.PopNow))
        assertEquals(entries.takeLast(1), entriesAllowedByBackPolicy(entries, BackDisposition.HandleInPlace))
        assertEquals(entries.takeLast(1), entriesAllowedByBackPolicy(entries, BackDisposition.Blocked))
    }

    @Test
    fun `disposing an old registration cannot remove its replacement`() {
        val navigator = unlockedNavigatorWithDetail()
        val coordinator = NavigationBackCoordinator(navigator)
        val oldToken = navigator.currentToken()
        val replacementToken = navigator.currentToken()
        coordinator.register(BackRegistration(oldToken, BackDisposition.Blocked, {}, {}, true))
        coordinator.register(BackRegistration(replacementToken, BackDisposition.PopNow, {}, {}, false))

        coordinator.unregister(oldToken)

        assertEquals(BackDisposition.PopNow, coordinator.effectiveDisposition())
    }

    private fun unlockedNavigatorWithDetail(): AppNavigator = unlockedNavigator().also { navigator ->
        navigator.push(VaultRoute.CredentialDetail(CREDENTIAL_ID), navigator.currentToken())
    }

    private fun unlockedNavigator(): AppNavigator {
        val live = TopLevelDestination.entries.associateWith { destination ->
            mutableListOf(destination.rootRoute())
        }
        val quarantine = TopLevelDestination.entries.associateWith { mutableListOf<PassVaultRoute>() }
        val navigator = AppNavigator(
            PassVaultNavigationState(
                authenticationBackStack = mutableListOf(AuthRoute.Unlock),
                mainBackStacks = live,
                quarantinedBackStacks = quarantine,
            ),
        )
        navigator.normalizeBootstrap(AuthRoute.Unlock)
        navigator.markSessionUnlocked()
        navigator.activateUnlocked()
        navigator.setHostResumed(true)
        return navigator
    }

    private companion object {
        const val CREDENTIAL_ID = "00000000-0000-0000-0000-000000000001"
    }
}
