package com.passvault.shared.platform

import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.LocalSystemTheme
import androidx.compose.ui.SystemTheme
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.lifecycle.viewModelScope
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.desktop_tray_exit
import com.passvault.core.designsystem.generated.resources.desktop_tray_lock
import com.passvault.core.designsystem.generated.resources.desktop_tray_show
import com.passvault.core.designsystem.generated.resources.desktop_tray_tooltip
import com.passvault.core.domain.repository.AppSettings
import com.passvault.core.domain.repository.AppSettingsStore
import com.passvault.core.domain.repository.LanguagePreference
import com.passvault.core.testing.fakes.FakeBiometricUnlockService
import com.passvault.core.testing.fakes.FakeVaultRepository
import com.passvault.feature.settings.presentation.SettingsViewModel
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.jetbrains.compose.resources.ResourceEnvironment
import org.jetbrains.compose.resources.getString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class, InternalComposeUiApi::class)
class DesktopAppResourceEnvironmentTest {
    @Test
    fun `selected settings language survives nested startup and parent recomposition without resetting content`() =
        runTest {
            CompositionFixture(this).use { fixture ->
                fixture.start()
                val preferences = MemorySettingsStore()
                var verifiedSettings by mutableStateOf<SettingsViewModel?>(null)
                var parentRevision by mutableIntStateOf(0)
                var appliedParentRevision = -1
                var outerEnvironment: ResourceEnvironment? = null
                var contentIdentity: Any? = null
                fixture.compose {
                    val revision = parentRevision
                    CompositionLocalProvider(LocalDensity provides Density(1f + revision / 10f)) {
                        val environment = rememberDesktopAppResourceEnvironment()
                        SideEffect { outerEnvironment = environment }
                        // Mirrors StartupTheme's immutable preference around verified app content.
                        AppLanguageProvider(SettingsViewModel.AppLanguage.ENGLISH) {
                            SideEffect { appliedParentRevision = revision }
                            verifiedSettings?.let { settings ->
                                val state by settings.state.collectAsState()
                                AppLanguageProvider(state.language) {
                                    PublishAppResourceEnvironment()
                                    val retained = remember { Any() }
                                    SideEffect { contentIdentity = retained }
                                }
                            }
                        }
                    }
                }
                fixture.settle()
                assertNull(desktopAppResourceEnvironment.value, "Startup content must not publish a second authority")
                assertNull(contentIdentity)

                val settings = fixture.createSettings(preferences)
                runCurrent()
                verifiedSettings = settings
                fixture.settle()
                val retainedContent = assertNotNull(contentIdentity)
                val selectedOwner = assertNotNull(desktopAppResourceEnvironment.value).owner
                assertEquals(ENGLISH, trayLabels(assertNotNull(outerEnvironment)))

                settings.selectLanguage(SettingsViewModel.AppLanguage.ARABIC)
                fixture.settle()
                val arabicEnvironment = assertNotNull(outerEnvironment)
                assertEquals(ARABIC, trayLabels(arabicEnvironment))
                assertEquals(LanguagePreference.ARABIC, preferences.saved.language)
                assertEquals("ar", currentNativeBiometricPromptStrings().languageTag)
                assertSame(retainedContent, contentIdentity)

                parentRevision++
                fixture.settle()
                assertEquals(1, appliedParentRevision)
                assertEquals(ARABIC, trayLabels(assertNotNull(outerEnvironment)))
                assertSame(selectedOwner, assertNotNull(desktopAppResourceEnvironment.value).owner)
                assertSame(retainedContent, contentIdentity)

                // A captured app environment cannot silently revert to an unrelated later OS/JVM default.
                Locale.setDefault(Locale.ENGLISH)
                assertEquals(ARABIC, trayLabels(arabicEnvironment))

                settings.selectLanguage(SettingsViewModel.AppLanguage.ENGLISH)
                fixture.settle()
                assertEquals(ENGLISH, trayLabels(assertNotNull(outerEnvironment)))
                assertEquals(LanguagePreference.ENGLISH, preferences.saved.language)
                assertEquals("en", currentNativeBiometricPromptStrings().languageTag)
                assertSame(retainedContent, contentIdentity)
            }
        }

    @Test
    fun `disposing an older publisher cannot clear the replacement owner`() = runTest {
        CompositionFixture(this).use { fixture ->
            fixture.start()
            val older = fixture.compose {
                AppLanguageProvider(SettingsViewModel.AppLanguage.ENGLISH) {
                    PublishAppResourceEnvironment()
                }
            }
            fixture.settle()
            val original = assertNotNull(desktopAppResourceEnvironment.value)
            assertEquals(ENGLISH, trayLabels(original.environment))

            val replacement = fixture.compose {
                AppLanguageProvider(SettingsViewModel.AppLanguage.ARABIC) {
                    PublishAppResourceEnvironment()
                }
            }
            fixture.settle()
            val current = assertNotNull(desktopAppResourceEnvironment.value)
            assertNotSame(original.owner, current.owner)
            assertEquals(ARABIC, trayLabels(current.environment))

            fixture.dispose(older)
            fixture.settle()
            assertSame(current, desktopAppResourceEnvironment.value)
            assertEquals(ARABIC, trayLabels(assertNotNull(desktopAppResourceEnvironment.value).environment))

            fixture.dispose(replacement)
            fixture.settle()
            assertNull(desktopAppResourceEnvironment.value)
        }
    }

    private fun SettingsViewModel.selectLanguage(language: SettingsViewModel.AppLanguage) {
        onEvent(SettingsViewModel.SettingsEvent.OnLanguageChanged(language))
    }

    private suspend fun trayLabels(environment: ResourceEnvironment): List<String> = listOf(
        getString(environment, Res.string.desktop_tray_tooltip),
        getString(environment, Res.string.desktop_tray_show),
        getString(environment, Res.string.desktop_tray_lock),
        getString(environment, Res.string.desktop_tray_exit),
    )

    private class MemorySettingsStore : AppSettingsStore {
        var saved = AppSettings(language = LanguagePreference.ENGLISH)
        override suspend fun load(): Result<AppSettings> = Result.success(saved)
        override suspend fun save(settings: AppSettings): Result<Unit> {
            saved = settings
            return Result.success(Unit)
        }
    }

    /** No windows, Koin graph, persistent preferences, database, or native provider. */
    private class CompositionFixture(private val scope: TestScope) : java.io.Closeable {
        private val originalLocale = Locale.getDefault()
        private val originalPublication = desktopAppResourceEnvironment.value
        private val originalBiometricStrings = currentNativeBiometricPromptStrings()
        private val frameClock = BroadcastFrameClock()
        private val repository = FakeVaultRepository()
        private val settingsViewModels = mutableListOf<SettingsViewModel>()
        private val compositions = mutableListOf<Composition>()
        private var recomposer: Recomposer? = null
        private var recomposerJob: Job? = null
        private var mainWasReplaced = false
        private var frame = 0L

        // Called inside use, so every global mutation and launched job already has a cleanup owner.
        fun start() {
            Dispatchers.setMain(StandardTestDispatcher(scope.testScheduler))
            mainWasReplaced = true
            desktopAppResourceEnvironment.value = null
            val activeRecomposer = Recomposer(scope.coroutineContext + frameClock)
            recomposer = activeRecomposer
            recomposerJob = scope.launch(frameClock) { activeRecomposer.runRecomposeAndApplyChanges() }
        }

        fun createSettings(preferences: AppSettingsStore): SettingsViewModel =
            SettingsViewModel(repository, preferences, FakeBiometricUnlockService()).also(settingsViewModels::add)

        fun compose(content: @Composable () -> Unit): Composition {
            val composition = Composition(NoUiApplier(), checkNotNull(recomposer))
            compositions.add(composition)
            composition.setContent {
                // Explicit values avoid consulting the native theme provider or a UI host.
                CompositionLocalProvider(
                    LocalDensity provides Density(1f),
                    LocalSystemTheme provides SystemTheme.Light,
                    content = content,
                )
            }
            return composition
        }

        fun dispose(composition: Composition) {
            composition.dispose()
            compositions.remove(composition)
        }

        fun settle() {
            repeat(SETTLEMENT_FRAMES) {
                Snapshot.sendApplyNotifications()
                scope.runCurrent()
                frameClock.sendFrame(++frame)
                scope.runCurrent()
            }
        }

        override fun close() {
            var failure: Throwable? = null
            fun release(action: () -> Unit) {
                runCatching(action).onFailure { cleanupFailure ->
                    failure?.addSuppressed(cleanupFailure) ?: run { failure = cleanupFailure }
                }
            }
            compositions.asReversed().forEach { release(it::dispose) }
            settingsViewModels.forEach { settings ->
                release(settings::clearForLock)
                release { settings.viewModelScope.cancel() }
            }
            release { recomposer?.cancel() }
            release { recomposerJob?.cancel() }
            release { repeat(SETTLEMENT_FRAMES) { scope.runCurrent() } }
            release { assertTrue(recomposerJob?.isCompleted != false, "Composition cleanup did not settle") }
            release(repository::reset)
            release { desktopAppResourceEnvironment.value = originalPublication }
            release { publishNativeBiometricPromptLanguage(originalBiometricStrings.languageTag) }
            release { Locale.setDefault(originalLocale) }
            if (mainWasReplaced) release { Dispatchers.resetMain() }
            failure?.let { throw it }
        }
    }

    private class NoUiApplier : AbstractApplier<Unit>(Unit) {
        override fun insertTopDown(index: Int, instance: Unit) = Unit
        override fun insertBottomUp(index: Int, instance: Unit) = Unit
        override fun remove(index: Int, count: Int) = Unit
        override fun move(from: Int, to: Int, count: Int) = Unit
        override fun onClear() = Unit
    }

    private companion object {
        val ENGLISH = listOf("PassVault Password Manager", "Show PassVault", "Lock Vault", "Exit")
        val ARABIC = listOf("مدير كلمات المرور PassVault", "إظهار PassVault", "قفل الخزنة", "خروج")
        const val SETTLEMENT_FRAMES = 8
    }
}
