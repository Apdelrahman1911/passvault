package com.passvault.shared.navigation

import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composition
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.passvault.core.crypto.SecurePasswordGenerator
import com.passvault.core.domain.model.CredentialId
import com.passvault.core.domain.model.CustomField
import com.passvault.core.domain.model.CustomFieldId
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.navigation.AppNavigator
import com.passvault.core.navigation.AuthRoute
import com.passvault.core.navigation.BackDisposition
import com.passvault.core.navigation.NavigationMutation
import com.passvault.core.navigation.PassVaultNavigationState
import com.passvault.core.navigation.PassVaultRoute
import com.passvault.core.navigation.TopLevelDestination
import com.passvault.core.navigation.VaultRoute
import com.passvault.core.navigation.rootRoute
import com.passvault.core.testing.TestData
import com.passvault.core.testing.fakes.FakeCredentialRepository
import com.passvault.core.testing.fakes.FakeCryptoEngine
import com.passvault.core.testing.fakes.FakeFolderRepository
import com.passvault.feature.credential.presentation.CredentialViewModel
import com.passvault.feature.credential.presentation.CredentialViewModel.CredentialEvent
import com.passvault.shared.navigation.adapters.RegisterCredentialBack
import com.passvault.shared.navigation.adapters.credentialBackDisposition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Real Compose registration, navigator and production editor; copied in-memory fake persistence.
 * This does not render NavDisplay, deliver native keys/IME events, or prove gesture-preview safety.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CredentialBackFreshnessCompositionTest {
    @Test
    fun `inline draft edit is guarded before the next composition publishes its policy`() = runTest {
        withEditorComposition {
            val before = appliedCompositions
            changeDraft()

            // No scheduler drain or frame between the accepted edit and the Back request.
            assertTrue(model.state.value.hasUnsavedChanges)
            assertEquals(BackDisposition.PopNow, lastComposedDisposition)
            assertEquals(BackDisposition.HandleInPlace, coordinator.effectiveDisposition())
            assertTrue(coordinator.requestBack())
            assertTrue(model.state.value.showDiscardConfirmation)
            assertEquals(editRoute, navigator.state.currentRoute())
            assertEquals(before, appliedCompositions)
        }
    }

    @Test
    fun `same-tab reselection cannot discard the current inline draft before a frame`() = runTest {
        withEditorComposition {
            val before = appliedCompositions
            val stackBefore = navigator.state.activeStack().toList()
            changeDraft()

            // The production shell's same-tab branch would reset HOME to its
            // root if this gate admitted the stale composed PopNow policy.
            if (coordinator.canLeaveForForwardNavigation()) {
                navigator.selectTab(TopLevelDestination.HOME, navigator.currentToken())
            }
            assertEquals(stackBefore, navigator.state.activeStack())
            assertFalse(coordinator.canLeaveForForwardNavigation())
            assertEquals(TopLevelDestination.HOME, navigator.state.selectedDestination.value)
            assertEquals(editRoute, navigator.state.currentRoute())
            assertEquals(BackDisposition.PopNow, lastComposedDisposition)
            assertEquals(before, appliedCompositions)
        }
    }

    @Test
    fun `interactive completion rejects a newly dirty editor before another frame`() = runTest {
        withEditorComposition {
            val before = appliedCompositions
            changeDraft()

            assertIs<NavigationMutation.Rejected>(coordinator.completeInteractivePop())
            assertEquals(editRoute, navigator.state.currentRoute())
            assertFalse(model.state.value.showDiscardConfirmation)
            assertEquals(BackDisposition.PopNow, lastComposedDisposition)
            assertEquals(before, appliedCompositions)
        }
    }

    @Test
    fun `synchronous save admission blocks old clean Back and forward policy`() = runTest {
        withEditorComposition {
            val before = appliedCompositions
            model.onEvent(CredentialEvent.OnSaveClick)

            assertTrue(model.state.value.isSaving)
            assertEquals(BackDisposition.Blocked, coordinator.effectiveDisposition())
            assertFalse(coordinator.canLeaveForForwardNavigation())
            assertTrue(coordinator.requestBack())
            assertIs<NavigationMutation.Rejected>(coordinator.completeInteractivePop())
            assertFalse(model.state.value.showDiscardConfirmation)
            assertEquals(editRoute, navigator.state.currentRoute())
            assertEquals(BackDisposition.PopNow, lastComposedDisposition)
            assertEquals(before, appliedCompositions)
        }
    }

    @Test
    fun `row Cancel restores clean Back without waiting to replace a dirty registration`() = runTest {
        withEditorComposition {
            changeDraft()
            settleFrame()
            assertEquals(BackDisposition.HandleInPlace, lastComposedDisposition)
            val before = appliedCompositions

            model.onEvent(CredentialEvent.OnCustomFieldEditCancelled(fieldId))

            assertFalse(model.state.value.hasUnsavedChanges)
            assertEquals(BackDisposition.PopNow, coordinator.effectiveDisposition())
            assertTrue(coordinator.canLeaveForForwardNavigation())
            assertTrue(coordinator.requestBack())
            assertEquals(detailRoute, navigator.state.currentRoute())
            assertEquals(BackDisposition.HandleInPlace, lastComposedDisposition)
            assertEquals(before, appliedCompositions)
        }
    }

    @Test
    fun `unchanged draft remains clean and Back still pops exactly one entry`() = runTest {
        withEditorComposition {
            assertFalse(model.state.value.hasUnsavedChanges)
            assertTrue(model.state.value.customFieldDrafts.containsKey(fieldId))
            assertEquals(BackDisposition.PopNow, coordinator.effectiveDisposition())
            assertTrue(coordinator.canLeaveForForwardNavigation())

            assertTrue(coordinator.requestBack())
            assertEquals(detailRoute, navigator.state.currentRoute())
            assertIs<NavigationMutation.Rejected>(coordinator.completeInteractivePop())
            assertEquals(listOf(VaultRoute.Vault, detailRoute), navigator.state.activeStack())
        }
    }

    @Test
    fun `disposing the real registration leaves the guarded editor fail closed`() = runTest {
        withEditorComposition {
            composition.dispose()

            assertEquals(BackDisposition.Blocked, coordinator.effectiveDisposition())
            assertFalse(coordinator.canLeaveForForwardNavigation())
            assertTrue(coordinator.requestBack())
            assertIs<NavigationMutation.Rejected>(coordinator.completeInteractivePop())
            assertEquals(editRoute, navigator.state.currentRoute())
        }
    }

    private suspend fun TestScope.withEditorComposition(block: suspend EditorCompositionFixture.() -> Unit) {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        var primaryFailure: Throwable? = null
        try {
            val repository = FakeCredentialRepository()
            val store = ViewModelStore()
            val clock = BroadcastFrameClock()
            val recomposer = Recomposer(coroutineContext + clock)
            var composition: Composition? = null
            var recomposerJob: Job? = null
            var modelJob: Job? = null
            var bodyFailure: Throwable? = null
            try {
                seedRepository(repository)
                val model = ViewModelProvider.create(
                    store,
                    viewModelFactory {
                        initializer {
                            CredentialViewModel(
                                repository,
                                FakeFolderRepository(),
                                SecurePasswordGenerator(FakeCryptoEngine()),
                            )
                        }
                    },
                )[CredentialViewModel::class]
                modelJob = checkNotNull(model.viewModelScope.coroutineContext[Job])
                model.loadCredential(CredentialId(CREDENTIAL_ID))
                runCurrent()
                assertTrue(model.state.value.isCredentialLoaded)
                model.onEvent(CredentialEvent.OnCustomFieldEditStarted(fieldId))
                val navigator = unlockedNavigatorWithEditor()
                val coordinator = NavigationBackCoordinator(navigator)
                val token = navigator.currentToken()
                recomposerJob = launch(clock) { recomposer.runRecomposeAndApplyChanges() }
                val activeComposition = Composition(UnitApplier(), recomposer)
                composition = activeComposition
                val fixture = EditorCompositionFixture(
                    this, model, navigator, coordinator, activeComposition, clock,
                )
                activeComposition.setContent {
                    val state by model.state.collectAsState()
                    val disposition = credentialBackDisposition(state)
                    RegisterCredentialBack(coordinator, token, model, disposition)
                    SideEffect {
                        fixture.lastComposedDisposition = disposition
                        fixture.appliedCompositions++
                    }
                }
                fixture.settleFrame()
                assertEquals(BackDisposition.PopNow, fixture.lastComposedDisposition)
                assertTrue(fixture.appliedCompositions > 0)
                fixture.block()
            } catch (failure: Throwable) {
                bodyFailure = failure
                throw failure
            } finally {
                try {
                    closeFixture(composition, store, recomposer, recomposerJob, modelJob, repository)
                } catch (cleanupFailure: Throwable) {
                    val failure = bodyFailure
                    if (failure == null) throw cleanupFailure else failure.addSuppressed(cleanupFailure)
                }
            }
        } catch (failure: Throwable) {
            primaryFailure = failure
            throw failure
        } finally {
            try {
                Dispatchers.resetMain()
            } catch (cleanupFailure: Throwable) {
                val failure = primaryFailure
                if (failure == null) throw cleanupFailure else failure.addSuppressed(cleanupFailure)
            }
        }
    }

    private suspend fun closeFixture(
        composition: Composition?,
        store: ViewModelStore,
        recomposer: Recomposer,
        recomposerJob: Job?,
        modelJob: Job?,
        repository: FakeCredentialRepository,
    ) = withContext(NonCancellable) {
        var cleanupFailure: Throwable? = null
        suspend fun release(action: suspend () -> Unit) {
            try {
                action()
            } catch (failure: Throwable) {
                val previousFailure = cleanupFailure
                if (previousFailure == null) cleanupFailure = failure else previousFailure.addSuppressed(failure)
            }
        }

        // Request every cancellation before joining any owner. Each settlement
        // is separately bounded and a failure cannot skip the remaining cleanup.
        release { composition?.dispose() }
        release { store.clear() }
        release { recomposer.cancel() }
        release { recomposerJob?.cancel() }
        release { modelJob?.cancel() }
        release {
            withTimeout(CLEANUP_TIMEOUT_MILLIS) { recomposerJob?.join() }
            assertTrue(recomposerJob?.isCompleted != false)
        }
        release { withTimeout(CLEANUP_TIMEOUT_MILLIS) { recomposer.join() } }
        release {
            withTimeout(CLEANUP_TIMEOUT_MILLIS) { modelJob?.join() }
            assertTrue(modelJob?.isCompleted != false)
        }
        release { repository.reset() }
        cleanupFailure?.let { throw it }
    }

    private fun seedRepository(repository: FakeCredentialRepository) {
        val credential = TestData.credential(id = CREDENTIAL_ID).copy(
            customFields = listOf(
                CustomField(fieldId, "Synthetic field", SensitiveText.from("synthetic value"), true),
            ),
        )
        try {
            repository.setupCredentials(credential)
        } finally {
            credential.clearSensitiveValues()
        }
    }

    private fun unlockedNavigatorWithEditor(): AppNavigator {
        val navigator = AppNavigator(
            PassVaultNavigationState(
                authenticationBackStack = mutableListOf(AuthRoute.Unlock),
                mainBackStacks = TopLevelDestination.entries.associateWith { destination ->
                    mutableListOf(destination.rootRoute())
                },
                quarantinedBackStacks = TopLevelDestination.entries.associateWith {
                    mutableListOf<PassVaultRoute>()
                },
            ),
        )
        navigator.normalizeBootstrap(AuthRoute.Unlock)
        navigator.markSessionUnlocked()
        navigator.activateUnlocked()
        navigator.setHostResumed(true)
        assertEquals(NavigationMutation.Applied, navigator.push(detailRoute, navigator.currentToken()))
        assertEquals(NavigationMutation.Applied, navigator.push(editRoute, navigator.currentToken()))
        return navigator
    }

    private class EditorCompositionFixture(
        private val scope: TestScope,
        val model: CredentialViewModel,
        val navigator: AppNavigator,
        val coordinator: NavigationBackCoordinator,
        val composition: Composition,
        private val clock: BroadcastFrameClock,
    ) {
        var appliedCompositions = 0
        var lastComposedDisposition: BackDisposition? = null
        private var frameTimeNanos = 0L

        fun changeDraft() {
            val draft = model.state.value.customFieldDrafts.getValue(fieldId)
            model.onEvent(
                CredentialEvent.OnCustomFieldDraftChanged(fieldId, draft.copy(value = "edited synthetic value")),
            )
        }

        fun settleFrame() {
            scope.runCurrent()
            Snapshot.sendApplyNotifications()
            scope.runCurrent()
            clock.sendFrame(++frameTimeNanos)
            scope.runCurrent()
        }
    }

    private class UnitApplier : AbstractApplier<Unit>(Unit) {
        override fun insertTopDown(index: Int, instance: Unit) = Unit
        override fun insertBottomUp(index: Int, instance: Unit) = Unit
        override fun remove(index: Int, count: Int) = Unit
        override fun move(from: Int, to: Int, count: Int) = Unit
        override fun onClear() = Unit
    }

    private companion object {
        const val CLEANUP_TIMEOUT_MILLIS = 1_000L
        const val CREDENTIAL_ID = "00000000-0000-0000-0000-000000000071"
        val fieldId = CustomFieldId("synthetic-back-field")
        val detailRoute = VaultRoute.CredentialDetail(CREDENTIAL_ID)
        val editRoute = VaultRoute.CredentialEdit(CREDENTIAL_ID)
    }
}
