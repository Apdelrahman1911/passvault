package com.passvault.core.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AppNavigatorTest {
    @Test
    fun `push pop and single top are deterministic`() {
        val navigator = unlockedNavigator()
        val rootToken = navigator.currentToken()
        val detail = VaultRoute.CredentialDetail(CREDENTIAL_ID)

        assertEquals(NavigationMutation.Applied, navigator.push(detail, rootToken))
        assertEquals(listOf(VaultRoute.Vault, detail), navigator.state.activeStack())

        val detailToken = navigator.currentToken()
        assertEquals(
            NavigationMutation.Rejected(NavigationRejection.Duplicate),
            navigator.pushSingleTop(detail, detailToken),
        )
        assertEquals(NavigationMutation.Applied, navigator.pop(detailToken))
        assertEquals(listOf(VaultRoute.Vault), navigator.state.activeStack())
    }

    @Test
    fun `pushing an existing route pops to it instead of duplicating it`() {
        val navigator = unlockedNavigator()
        val detail = VaultRoute.CredentialDetail(CREDENTIAL_ID)
        navigator.push(detail, navigator.currentToken())
        navigator.push(VaultRoute.CredentialEdit(CREDENTIAL_ID), navigator.currentToken())

        assertEquals(NavigationMutation.Applied, navigator.push(detail, navigator.currentToken()))
        assertEquals(listOf(VaultRoute.Vault, detail), navigator.state.activeStack())
    }

    @Test
    fun `inactive lifecycle and stale route tokens cannot navigate`() {
        val navigator = unlockedNavigator()
        val inactive = navigator.createToken(VaultRoute.Vault, entryResumed = false)
        assertEquals(
            NavigationMutation.Rejected(NavigationRejection.EntryInactive),
            navigator.push(VaultRoute.CredentialCreate(), inactive),
        )

        val staleRoute = navigator.currentToken()
        navigator.push(VaultRoute.CredentialCreate(), staleRoute)
        assertEquals(
            NavigationMutation.Rejected(NavigationRejection.EntryInactive),
            navigator.push(HealthRoute.Health, staleRoute),
        )

        val staleSession = navigator.currentToken()
        navigator.requireAuthentication()
        assertEquals(
            NavigationMutation.Rejected(NavigationRejection.StaleSession),
            navigator.popAfterGuard(staleSession),
        )
    }

    @Test
    fun `host detachment immediately rejects navigation from the former active entry`() {
        val navigator = unlockedNavigator()
        val token = navigator.currentToken()

        navigator.setHostResumed(false)

        assertEquals(
            NavigationMutation.Rejected(NavigationRejection.HostInactive),
            navigator.push(VaultRoute.CredentialCreate(), token),
        )
    }

    @Test
    fun `tab stacks are isolated and reselect pops only the selected tab to root`() {
        val navigator = unlockedNavigator()
        val homeDetail = VaultRoute.CredentialDetail(CREDENTIAL_ID)
        navigator.push(homeDetail, navigator.currentToken())
        navigator.selectTab(TopLevelDestination.SETTINGS, navigator.currentToken())
        navigator.push(SettingsRoute.Security, navigator.currentToken())

        assertEquals(
            listOf(VaultRoute.Vault, homeDetail),
            navigator.state.stack(TopLevelDestination.HOME),
        )
        assertEquals(
            listOf(SettingsRoute.Settings, SettingsRoute.Security),
            navigator.state.stack(TopLevelDestination.SETTINGS),
        )

        navigator.selectTab(TopLevelDestination.HOME, navigator.currentToken())
        assertEquals(homeDetail, navigator.state.currentRoute())
        navigator.selectTab(TopLevelDestination.HOME, navigator.currentToken())
        assertEquals(listOf(VaultRoute.Vault), navigator.state.stack(TopLevelDestination.HOME))
        assertEquals(
            listOf(SettingsRoute.Settings, SettingsRoute.Security),
            navigator.state.stack(TopLevelDestination.SETTINGS),
        )
    }

    @Test
    fun `rapid repeated tab input cannot turn a switch into an accidental reselect`() {
        val navigator = unlockedNavigator()
        val detail = VaultRoute.CredentialDetail(CREDENTIAL_ID)
        navigator.push(detail, navigator.currentToken())
        val capturedToken = navigator.currentToken()

        assertEquals(
            NavigationMutation.Applied,
            navigator.selectTab(TopLevelDestination.SETTINGS, capturedToken),
        )
        assertEquals(
            NavigationMutation.Rejected(NavigationRejection.EntryInactive),
            navigator.selectTab(TopLevelDestination.SETTINGS, capturedToken),
        )
        assertEquals(listOf(VaultRoute.Vault, detail), navigator.state.stack(TopLevelDestination.HOME))
        assertEquals(listOf(SettingsRoute.Settings), navigator.state.activeStack())
    }

    @Test
    fun `route ownership is enforced by the navigator rather than adapters`() {
        val navigator = unlockedNavigator()

        assertEquals(
            NavigationMutation.Rejected(NavigationRejection.InvalidDestination),
            navigator.push(SettingsRoute.Security, navigator.currentToken()),
        )
        assertEquals(
            NavigationMutation.Rejected(NavigationRejection.InvalidDestination),
            navigator.openInTab(
                destination = TopLevelDestination.SETTINGS,
                route = HealthRoute.Health,
                resetStack = false,
                token = navigator.currentToken(),
            ),
        )
        assertEquals(listOf(VaultRoute.Vault), navigator.state.activeStack())
    }

    @Test
    fun `malformed route arguments are rejected before entering a live stack`() {
        val navigator = unlockedNavigator()

        assertEquals(
            NavigationMutation.Rejected(NavigationRejection.InvalidDestination),
            navigator.push(VaultRoute.CredentialDetail("not-an-id"), navigator.currentToken()),
        )
        assertEquals(
            NavigationMutation.Rejected(NavigationRejection.InvalidDestination),
            navigator.push(VaultRoute.CredentialCreate("invalid-folder"), navigator.currentToken()),
        )
        assertEquals(listOf(VaultRoute.Vault), navigator.state.activeStack())
    }

    @Test
    fun `locking quarantines stacks and unlock restores only the supplied validated snapshot`() {
        val navigator = unlockedNavigator()
        val detail = VaultRoute.CredentialDetail(CREDENTIAL_ID)
        navigator.push(detail, navigator.currentToken())
        navigator.selectTab(TopLevelDestination.SETTINGS, navigator.currentToken())
        navigator.push(SettingsRoute.Data, navigator.currentToken())

        navigator.requireAuthentication()

        assertEquals(NavigationRoot.AUTHENTICATION, navigator.state.root.value)
        assertEquals(AuthRoute.Unlock, navigator.state.currentRoute())
        assertEquals(listOf(VaultRoute.Vault), navigator.state.stack(TopLevelDestination.HOME))
        val quarantined = requireNotNull(navigator.state.quarantinedSnapshotOrNull())
        assertEquals(TopLevelDestination.SETTINGS, quarantined.selectedDestination)
        assertEquals(listOf(VaultRoute.Vault, detail), quarantined.home)

        val validated = quarantined.copy(settings = listOf(SettingsRoute.Settings))
        navigator.markSessionUnlocked()
        navigator.activateUnlocked(validated)

        assertEquals(NavigationRoot.MAIN, navigator.state.root.value)
        assertEquals(TopLevelDestination.SETTINGS, navigator.state.selectedDestination.value)
        assertEquals(listOf(SettingsRoute.Settings), navigator.state.activeStack())
        assertNull(navigator.state.quarantinedSnapshotOrNull())
    }

    @Test
    fun `protected navigation is rejected until the session is unlocked and activated`() {
        val state = navigationState(AuthRoute.Unlock)
        val navigator = AppNavigator(state)
        navigator.normalizeBootstrap(AuthRoute.Unlock)
        navigator.setHostResumed(true)

        assertEquals(
            NavigationMutation.Rejected(NavigationRejection.VaultLocked),
            navigator.activateUnlocked(),
        )
        val token = navigator.currentToken()
        assertEquals(
            NavigationMutation.Rejected(NavigationRejection.VaultLocked),
            navigator.push(VaultRoute.CredentialCreate(), token),
        )
        assertEquals(AuthRoute.Unlock, state.currentRoute())
    }

    @Test
    fun `process-restored protected stacks remain quarantined behind authentication`() {
        val detail = VaultRoute.CredentialDetail(CREDENTIAL_ID)
        val live: Map<TopLevelDestination, MutableList<PassVaultRoute>> =
            TopLevelDestination.entries.associateWith { destination ->
                if (destination == TopLevelDestination.HOME) {
                    mutableListOf(VaultRoute.Vault, detail)
                } else {
                    mutableListOf(destination.rootRoute())
                }
        }
        val quarantined = TopLevelDestination.entries.associateWith {
            mutableListOf<PassVaultRoute>()
        }
        val state = PassVaultNavigationState(
            authenticationBackStack = mutableListOf(AuthRoute.Unlock),
            mainBackStacks = live,
            quarantinedBackStacks = quarantined,
        )
        val navigator = AppNavigator(state)
        navigator.normalizeBootstrap(AuthRoute.Unlock)
        navigator.setHostResumed(true)

        assertEquals(NavigationRoot.AUTHENTICATION, state.root.value)
        assertEquals(AuthRoute.Unlock, state.currentRoute())
        navigator.requireAuthentication()
        assertEquals(listOf(VaultRoute.Vault), state.stack(TopLevelDestination.HOME))
        assertEquals(listOf(VaultRoute.Vault, detail), state.quarantinedSnapshotOrNull()?.home)

        navigator.markSessionUnlocked()
        navigator.activateUnlocked(
            state.restorableSnapshot().copy(home = listOf(VaultRoute.Vault)),
        )
        assertEquals(VaultRoute.Vault, state.currentRoute())
    }

    @Test
    fun `onboarding navigation is separate and resetting onboarding discards protected restoration`() {
        val state = navigationState(AuthRoute.Onboarding)
        val navigator = AppNavigator(state)
        navigator.normalizeBootstrap(AuthRoute.Onboarding)
        navigator.setHostResumed(true)

        assertEquals(
            NavigationMutation.Applied,
            navigator.pushAuthentication(AuthRoute.CreatePassword, navigator.currentToken()),
        )
        assertEquals(
            listOf(AuthRoute.Onboarding, AuthRoute.CreatePassword),
            state.authenticationStack,
        )
        navigator.requireOnboarding()
        assertEquals(listOf(AuthRoute.Onboarding), state.authenticationStack)
        assertNull(state.quarantinedSnapshotOrNull())
    }

    @Test
    fun `onboarding rejects external navigation without retaining a pending destination`() {
        val navigator = AppNavigator(navigationState(AuthRoute.Onboarding))
        navigator.normalizeBootstrap(AuthRoute.Onboarding)
        navigator.setHostResumed(true)
        val envelope = ExternalNavigationEnvelope(
            deliveryId = "onboarding-input",
            source = ExternalNavigationSource.PLATFORM_INTENT,
            intent = ExternalNavigationIntent.Settings,
        )

        assertEquals(
            NavigationMutation.Rejected(NavigationRejection.UnauthorizedExternalInput),
            navigator.submitExternal(envelope),
        )
        assertNull(navigator.pendingExternal())
        assertEquals(AuthRoute.Onboarding, navigator.state.currentRoute())
    }

    @Test
    fun `external credential navigation is auth gated and builds a deterministic stack`() {
        val state = navigationState(AuthRoute.Unlock)
        val navigator = AppNavigator(state)
        navigator.normalizeBootstrap(AuthRoute.Unlock)
        navigator.setHostResumed(true)
        val envelope = ExternalNavigationEnvelope(
            deliveryId = "notification-1",
            source = ExternalNavigationSource.NOTIFICATION,
            intent = ExternalNavigationIntent.Credential(CREDENTIAL_ID),
        )

        assertEquals(NavigationMutation.Deferred, navigator.submitExternal(envelope))
        assertEquals(AuthRoute.Unlock, state.currentRoute())

        navigator.markSessionUnlocked()
        navigator.activateUnlocked()
        assertEquals(NavigationMutation.Applied, navigator.applyValidatedExternal(envelope))
        assertEquals(TopLevelDestination.HOME, state.selectedDestination.value)
        assertEquals(
            listOf(VaultRoute.Vault, VaultRoute.CredentialDetail(CREDENTIAL_ID)),
            state.activeStack(),
        )
        assertEquals(
            NavigationMutation.Rejected(NavigationRejection.DuplicateExternalDelivery),
            navigator.applyValidatedExternal(envelope),
        )
    }

    @Test
    fun `validated external navigation waits while host is inactive and applies once resumed`() {
        val navigator = unlockedNavigator()
        val envelope = ExternalNavigationEnvelope(
            deliveryId = "background-input",
            source = ExternalNavigationSource.PLATFORM_INTENT,
            intent = ExternalNavigationIntent.SecuritySettings,
        )
        assertEquals(NavigationMutation.Deferred, navigator.submitExternal(envelope))

        navigator.setHostResumed(false)
        assertEquals(NavigationMutation.Deferred, navigator.applyValidatedExternal(envelope))
        assertEquals(TopLevelDestination.HOME, navigator.state.selectedDestination.value)
        assertEquals(envelope, navigator.pendingExternal())

        navigator.setHostResumed(true)
        assertEquals(NavigationMutation.Applied, navigator.applyValidatedExternal(envelope))
        assertEquals(TopLevelDestination.SETTINGS, navigator.state.selectedDestination.value)
        assertEquals(
            listOf(SettingsRoute.Settings, SettingsRoute.Security),
            navigator.state.activeStack(),
        )
        assertNull(navigator.pendingExternal())
    }

    @Test
    fun `every static external intent builds its documented owning stack`() {
        val cases = listOf(
            ExternalNavigationIntent.Generator to
                (TopLevelDestination.GENERATOR to listOf(GeneratorRoute.Generator)),
            ExternalNavigationIntent.Health to
                (TopLevelDestination.GENERATOR to listOf(GeneratorRoute.Generator, HealthRoute.Health)),
            ExternalNavigationIntent.TwoFactorCodes to
                (TopLevelDestination.TWO_FACTOR_CODES to listOf(TwoFactorRoute.Codes)),
            ExternalNavigationIntent.Settings to
                (TopLevelDestination.SETTINGS to listOf(SettingsRoute.Settings)),
            ExternalNavigationIntent.SecuritySettings to
                (TopLevelDestination.SETTINGS to listOf(SettingsRoute.Settings, SettingsRoute.Security)),
            ExternalNavigationIntent.AppearanceSettings to
                (TopLevelDestination.SETTINGS to listOf(SettingsRoute.Settings, SettingsRoute.Appearance)),
            ExternalNavigationIntent.DataSettings to
                (TopLevelDestination.SETTINGS to listOf(SettingsRoute.Settings, SettingsRoute.Data)),
            ExternalNavigationIntent.Backup to
                (TopLevelDestination.SETTINGS to listOf(SettingsRoute.Settings, BackupRoute.Backup)),
            ExternalNavigationIntent.Import to
                (TopLevelDestination.SETTINGS to listOf(SettingsRoute.Settings, BackupRoute.Import)),
            ExternalNavigationIntent.Export to
                (TopLevelDestination.SETTINGS to listOf(SettingsRoute.Settings, BackupRoute.Export)),
        )

        cases.forEachIndexed { index, (intent, expected) ->
            val navigator = unlockedNavigator()
            val envelope = ExternalNavigationEnvelope(
                deliveryId = "static-$index",
                source = ExternalNavigationSource.PLATFORM_INTENT,
                intent = intent,
            )
            assertEquals(NavigationMutation.Deferred, navigator.submitExternal(envelope))
            assertEquals(NavigationMutation.Applied, navigator.applyValidatedExternal(envelope))
            assertEquals(expected.first, navigator.state.selectedDestination.value, intent.toString())
            assertEquals(expected.second, navigator.state.activeStack(), intent.toString())
        }
    }

    @Test
    fun `new external input supersedes an older pending validation without stale mutation`() {
        val navigator = unlockedNavigator()
        val first = ExternalNavigationEnvelope(
            deliveryId = "first",
            source = ExternalNavigationSource.PLATFORM_INTENT,
            intent = ExternalNavigationIntent.Generator,
        )
        val second = ExternalNavigationEnvelope(
            deliveryId = "second",
            source = ExternalNavigationSource.PLATFORM_INTENT,
            intent = ExternalNavigationIntent.Settings,
        )

        assertEquals(NavigationMutation.Deferred, navigator.submitExternal(first))
        assertEquals(NavigationMutation.Deferred, navigator.submitExternal(second))
        assertEquals(
            NavigationMutation.Rejected(NavigationRejection.StaleExternalDelivery),
            navigator.applyValidatedExternal(first),
        )
        assertEquals(TopLevelDestination.HOME, navigator.state.selectedDestination.value)
        assertEquals(NavigationMutation.Applied, navigator.applyValidatedExternal(second))
        assertEquals(TopLevelDestination.SETTINGS, navigator.state.selectedDestination.value)
    }

    @Test
    fun `default back disposition distinguishes pop local tab return and application exit`() {
        val navigator = unlockedNavigator()
        assertEquals(BackDisposition.ExitApplication, navigator.defaultBackDisposition())

        navigator.selectTab(TopLevelDestination.GENERATOR, navigator.currentToken())
        assertEquals(BackDisposition.HandleInPlace, navigator.defaultBackDisposition())
        navigator.handleDefaultInPlaceBack(navigator.currentToken())
        assertEquals(TopLevelDestination.HOME, navigator.state.selectedDestination.value)

        navigator.push(VaultRoute.CredentialCreate(), navigator.currentToken())
        assertEquals(BackDisposition.PopNow, navigator.defaultBackDisposition())
        assertEquals(BackDisposition.Blocked, navigator.defaultBackDisposition(conservativeGuard = true))
    }

    private fun unlockedNavigator(): AppNavigator {
        val navigator = AppNavigator(navigationState(AuthRoute.Unlock))
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

internal fun navigationState(initialRoute: AuthRoute): PassVaultNavigationState {
    val live = TopLevelDestination.entries.associateWith { destination ->
        mutableListOf(destination.rootRoute())
    }
    val quarantined = TopLevelDestination.entries.associateWith {
        mutableListOf<PassVaultRoute>()
    }
    return PassVaultNavigationState(
        authenticationBackStack = mutableListOf(initialRoute),
        mainBackStacks = live,
        quarantinedBackStacks = quarantined,
    )
}
