@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package com.passvault.desktop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.passvault.core.crypto.LibsodiumCryptoEngine
import com.passvault.core.crypto.VaultKeyHierarchy
import com.passvault.core.database.VaultDatabase
import com.passvault.core.database.VaultDatabaseBootstrap
import com.passvault.core.database.VaultDatabaseBootstrapResult
import com.passvault.core.database.createDatabaseBootstrap
import com.passvault.core.database.repository.CredentialRepositoryImpl
import com.passvault.core.database.repository.VaultRepositoryImpl
import com.passvault.core.domain.model.Credential
import com.passvault.core.domain.model.CredentialId
import com.passvault.core.domain.model.CredentialType
import com.passvault.core.domain.model.CustomField
import com.passvault.core.domain.model.CustomFieldId
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.model.VaultSessionState
import com.passvault.core.domain.repository.AppSettings
import com.passvault.core.domain.repository.AppSettingsStore
import com.passvault.core.domain.repository.CredentialRepository
import com.passvault.core.domain.repository.LanguagePreference
import com.passvault.core.domain.repository.LockReason
import com.passvault.core.domain.repository.ThemePreference
import com.passvault.core.domain.repository.VaultRepository
import com.passvault.core.security.ClipboardService
import com.passvault.core.security.VaultUiSecurityCoordinator
import com.passvault.desktop.attachment.DesktopAttachmentFileStore
import com.passvault.desktop.di.desktopModule
import com.passvault.desktop.security.biometric.DesktopBiometricHost
import com.passvault.feature.backup.presentation.BackupViewModel
import com.passvault.feature.onboarding.presentation.OnboardingViewModel
import com.passvault.feature.settings.presentation.SettingsViewModel
import com.passvault.feature.unlock.presentation.UnlockViewModel
import com.passvault.feature.vault.presentation.VaultViewModel
import com.passvault.shared.PassVaultApp
import com.passvault.shared.di.AppModule
import java.awt.GraphicsEnvironment
import java.awt.IllegalComponentStateException
import java.awt.Rectangle
import java.awt.Robot
import java.awt.Window as AwtWindow
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.LinkOption.NOFOLLOW_LINKS
import java.nio.file.Path
import java.nio.file.StandardOpenOption.APPEND
import java.nio.file.StandardOpenOption.CREATE_NEW
import java.nio.file.StandardOpenOption.WRITE
import java.nio.file.attribute.PosixFileAttributes
import java.nio.file.attribute.PosixFilePermissions
import java.util.IdentityHashMap
import java.util.Locale
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import javax.accessibility.AccessibleContext
import javax.accessibility.AccessibleRole
import javax.accessibility.AccessibleState
import javax.accessibility.AccessibleText
import javax.swing.SwingUtilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assume.assumeTrue
import org.koin.core.KoinApplication
import org.koin.core.context.GlobalContext
import kotlin.system.exitProcess
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlin.time.Instant

/**
 * DRAFT ONLY: proposed app-desktop/desktopTest placement, not a runnable admission.
 * One case: real Window -> PassVaultApp/NavHost -> real dirty leave coordinator -> Room reopen.
 * No Main/PassVaultDesktopWindow, fake ports, fake navigation callbacks, screenshots or clipboard IO.
 * The caller must admit a fresh X session, child classpath, allocation/ownership and external cleanup.
 */
class CredentialNavHostRoomIntegrationTest {
    @Test
    fun `native dirty navigation stays or discards without changing the durable Room tuple`() {
        val display = System.getProperty(PREFIX + "syntheticDisplay")
        assumeTrue("NavHost case needs a separately admitted synthetic display", display != null)
        checkPlatform(requireNotNull(display)) // Metadata only; do not initialize AWT/Koin/preferences here.
        val runtime = PrivateDirectory.acquire(property("runtimeDir"), empty = true)
        val evidence = PrivateDirectory.acquire(property("evidenceDir"), empty = true)
        assertFalse(runtime.path.startsWith(evidence.path) || evidence.path.startsWith(runtime.path))
        val home = runtime.child("home")
        val tmp = runtime.child("tmp")
        val userPrefs = runtime.child("prefs-user")
        val systemPrefs = runtime.child("prefs-system")
        val classpath = property("childClasspath")
        require(classpath.isNotBlank()) { "NAV_ACTUAL_TEST_RUNTIME_CLASSPATH_REQUIRED" }
        val authority = Path.of(requireNotNull(System.getenv("XAUTHORITY")))
        assertTrue(authority.isAbsolute && Files.isRegularFile(authority, NOFOLLOW_LINKS))
        val tracePath = Files.createFile(evidence.path.resolve("navhost.events"), PRIVATE_FILE)
        val traceKey = Files.readAttributes(tracePath, PosixFileAttributes::class.java, NOFOLLOW_LINKS).fileKey()
        val log = Files.createFile(evidence.path.resolve("navhost.log"), PRIVATE_FILE)
        val environment = mutableMapOf(
            "PATH" to "/usr/bin:/bin", "LANG" to "C.UTF-8", "LC_ALL" to "C.UTF-8", "TZ" to "UTC",
            "HOME" to home.path.toString(), "TMPDIR" to tmp.path.toString(),
            "TMP" to tmp.path.toString(), "TEMP" to tmp.path.toString(),
            "DISPLAY" to display, "XAUTHORITY" to authority.toString(),
        )
        for (kind in listOf("CACHE", "CONFIG", "DATA", "STATE")) {
            environment["XDG_${kind}_HOME"] = runtime.child("xdg-${kind.lowercase()}").path.toString()
        }
        val builder = ProcessBuilder(
            Path.of(System.getProperty("java.home"), "bin", "java").toString(),
            "-Xmx512m", "-XX:ActiveProcessorCount=1", "-XX:-UsePerfData", "-Dfile.encoding=UTF-8",
            "-XX:-CreateCoredumpOnCrash", "-XX:ErrorFile=${runtime.path}/hs_err_pid%p.log",
            "-Djava.awt.headless=false", "-Duser.language=en", "-Duser.country=US",
            "-Duser.home=${home.path}", "-Djava.io.tmpdir=${tmp.path}",
            "-Djava.util.prefs.userRoot=${userPrefs.path}", "-Djava.util.prefs.systemRoot=${systemPrefs.path}",
            "-Djna.tmpdir=${tmp.path}", "-Dorg.sqlite.tmpdir=${tmp.path}",
            "-D${PREFIX}syntheticDisplay=$display", "-D${PREFIX}home=${home.path}",
            "-D${PREFIX}tmp=${tmp.path}", "-D${PREFIX}userPrefs=${userPrefs.path}",
            "-D${PREFIX}systemPrefs=${systemPrefs.path}", "-D${PREFIX}ownedChild=true",
            "-D${PREFIX}evidenceDir=${evidence.path}",
            "-cp", classpath, CredentialNavHostRoomProbe::class.java.name,
            tracePath.toString(), requireNotNull(traceKey).toString(),
        ).directory(runtime.path.toFile()).redirectErrorStream(true).redirectOutput(log.toFile())
        builder.environment().clear()
        builder.environment().putAll(environment)
        var child: Process? = null
        val failure = Failures()
        try {
            runtime.checkBound()
            evidence.checkBound()
            val running = builder.start().also { child = it } // Registered before waits or assertions.
            running.outputStream.close()
            val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(240)
            while (!running.waitFor(100, TimeUnit.MILLISECONDS)) {
                assertTrue(System.nanoTime() < deadline, "NAV_CHILD_DEADLINE")
                assertTrue(Files.size(log) <= MAX_LOG_BYTES, "NAV_CHILD_LOG_BOUND")
                assertTrue(Files.size(tracePath) <= 16_384, "NAV_CHILD_TRACE_BOUND")
            }
            assertTrue(Files.size(log) <= MAX_LOG_BYTES, "NAV_FINAL_LOG_BOUND")
            assertEquals(0, running.exitValue(), "NAV_CHILD_FAILED_SEE_COMPACT_LOG")
            assertEquals(traceKey, Files.readAttributes(
                tracePath, PosixFileAttributes::class.java, NOFOLLOW_LINKS,
            ).fileKey(), "NAV_TRACE_REPLACED")
            assertTrue(Files.size(tracePath) <= 16_384, "NAV_FINAL_TRACE_BOUND")
            assertEquals(EXPECTED_EVENTS, Files.readAllLines(tracePath), "NAV_ONE_CASE_STAGE_EVIDENCE")
        } catch (error: Throwable) {
            failure.add(error)
        } finally {
            failure.release {
                child?.let { running ->
                    if (running.isAlive) running.destroyForcibly() // This captured child only, never Gradle/other work.
                    check(running.waitFor(5, TimeUnit.SECONDS)) { "NAV_CHILD_UNSETTLED_CLEANUP_BLOCKED" }
                    Files.writeString(
                        evidence.path.resolve("navhost.exit"), "${running.exitValue()}\n", CREATE_NEW, WRITE,
                    )
                }
            }
            failure.release { runtime.checkBound(); evidence.checkBound() }
            // No deletion: root preserves evidence, stops its wrapper, settles workers, then allowlists outputs.
            failure.throwIfPresent()
        }
    }
}

/** Fresh child, never the application Main or an archived application/recovery runner. */
internal object CredentialNavHostRoomProbe {
    @JvmStatic
    fun main(args: Array<String>) {
        var trace: Trace? = null
        var graph: GraphOwner? = null
        var native: NativeNavHost? = null
        val failure = Failures()
        try {
            require(args.size == 2 && property("ownedChild") == "true")
            checkPlatform(property("syntheticDisplay"))
            val home = PrivateDirectory.acquire(property("home"), empty = true)
            val tmp = PrivateDirectory.acquire(property("tmp"), empty = true)
            val userPrefs = PrivateDirectory.acquire(property("userPrefs"), empty = true)
            val systemPrefs = PrivateDirectory.acquire(property("systemPrefs"), empty = true)
            val owned = listOf(home, tmp, userPrefs, systemPrefs)
            assertEquals(4, owned.map { it.path }.toSet().size)
            owned.forEach { left -> owned.filter { it !== left }.forEach { right ->
                assertFalse(left.path.startsWith(right.path), "NAV_OVERLAPPING_PRIVATE_ROOTS")
            } }
            assertEquals(home.path.toString(), System.getProperty("user.home"))
            assertEquals(home.path.toString(), System.getenv("HOME"))
            assertEquals(tmp.path.toString(), System.getProperty("java.io.tmpdir"))
            assertEquals(userPrefs.path.toString(), System.getProperty("java.util.prefs.userRoot"))
            assertEquals(systemPrefs.path.toString(), System.getProperty("java.util.prefs.systemRoot"))
            assertFalse(Files.exists(home.path.resolve(".passvault"), NOFOLLOW_LINKS))
            trace = Trace(Path.of(args[0]), args[1])
            assertFalse(GraphicsEnvironment.isHeadless(), "NAV_HEADLESS") // Only after private-home/prefs guards.
            graph = GraphOwner(home) // Register before starting any Koin/Room/native work.
            graph.startAndSeed()
            trace.record("SEED_LOCKED")
            native = NativeNavHost(graph, trace)
            native.runActualApplicationLoop()
        } catch (error: Throwable) {
            failure.add(error)
        } finally {
            failure.release { native?.finish() }
            failure.release { graph?.close(native?.storageCloseAllowed != false) }
        }
        if (!failure.failed) {
            failure.release {
                val old = requireNotNull(graph)
                check(old.closed) { "NAV_OLD_ROOM_NOT_CLOSED" }
                requireNotNull(trace).record("APP_GRAPH_CLOSED")
                verifyFreshRoom(old, requireNotNull(trace))
                requireNotNull(trace).record("COMPLETE")
            }
        }
        failure.problem()?.let { it.printStackTrace(System.err) }
        failure.restoreInterrupt()
        // Process return is also required: a hung real shutdown hook is a failed case, not cleanup success.
        exitProcess(if (failure.failed) 1 else 0)
    }
}

/** One real production graph. No test modules/overrides and no entry VM obtained outside Nav3. */
private class GraphOwner(val home: PrivateDirectory) {
    private var application: KoinApplication? = null
    private var bootstrap: VaultDatabaseBootstrap? = null
    private var applicationScope: CoroutineScope? = null
    private var clipboard: ClipboardService? = null
    private var attachmentStore: DesktopAttachmentFileStore? = null
    private var biometricHost: DesktopBiometricHost? = null
    private var security: VaultUiSecurityCoordinator? = null
    private val rootJobs = mutableListOf<Job>()
    private val rootClear = mutableListOf<() -> Unit>()
    private var terminalAttempted = false
    private var boundVault: VaultRepository? = null
    private var boundCredentials: CredentialRepository? = null
    var database: VaultDatabase? = null
        private set
    var databaseKey: Any? = null
        private set
    var closed = false
        private set
    val vault: VaultRepository get() = requireNotNull(boundVault)
    val credentials: CredentialRepository get() = requireNotNull(boundCredentials)

    fun startAndSeed() {
        checkHome(home)
        assertEquals(null, runCatching { GlobalContext.get() }.getOrNull(), "NAV_EXISTING_GLOBAL_KOIN")
        application = GlobalContext.startKoin { modules(AppModule.getAllModules(desktopModule)) }
        val koin = requireNotNull(application).koin
        applicationScope = koin.get()
        bootstrap = koin.get()
        io { assertEquals(VaultDatabaseBootstrapResult.Ready, requireNotNull(bootstrap).openAndVerify()) }
        database = requireNotNull(bootstrap).database()
        boundVault = koin.get()
        boundCredentials = koin.get()
        clipboard = koin.get()
        attachmentStore = koin.get()
        biometricHost = koin.get()
        security = koin.get()
        io {
            koin.get<AppSettingsStore>().save(AppSettings(
                language = LanguagePreference.ENGLISH, theme = ThemePreference.LIGHT,
            )).getOrThrow()
            password { vault.create(it).getOrThrow(); vault.unlock(it).getOrThrow() }
            val credential = seedCredential()
            try { credentials.save(credential).getOrThrow() } finally { credential.clearSensitiveValues() }
            assertOriginal(credentials)
            // Ordinary prelaunch lock, not terminal database shutdown; native Unlock must create the UI session.
            vault.lock(LockReason.Manual).getOrThrow()
            assertTrue(vault.getSessionState().first() is VaultSessionState.Locked)
        }
        databaseKey = databaseIdentity(home)
        onEdt {
            val onboarding: OnboardingViewModel = koin.get()
            registerRoot(onboarding, onboarding::clearForLock)
            val unlock: UnlockViewModel = koin.get()
            registerRoot(unlock, unlock::clearForLock)
            val vaultModel: VaultViewModel = koin.get()
            registerRoot(vaultModel, vaultModel::clearForLock)
            val settings: SettingsViewModel = koin.get()
            registerRoot(settings, settings::clearForLock)
            val backup: BackupViewModel = koin.get()
            registerRoot(backup, backup::clearForLock)
        }
        assertEquals(5, rootJobs.size)
    }

    private fun registerRoot(model: ViewModel, clear: () -> Unit) {
        rootClear += clear
        rootJobs += requireNotNull(model.viewModelScope.coroutineContext[Job])
    }

    fun close(uiSettled: Boolean) {
        if (terminalAttempted) return
        terminalAttempted = true // Never retry an uncertain lock, close or Koin shutdown.
        val failures = Failures()
        failures.release { onEdt { security?.clearEntrySensitiveStateForLock() } }
        rootClear.forEach { clear -> failures.release { onEdt(clear) } }
        rootJobs.forEach { job -> failures.release { job.cancel() } }
        val scopeJob = applicationScope?.coroutineContext?.get(Job)
        failures.release { applicationScope?.cancel() }
        (rootJobs + listOfNotNull(scopeJob)).forEach { job -> failures.release {
            runBlocking { withTimeout(5_000) { job.join() } }
        } }
        failures.release { io(5_000) { boundVault?.lock(LockReason.Manual)?.getOrThrow() } }
        failures.release { io(5_000) { clipboard?.clear() } } // No copy: production guard avoids OS clipboard IO.
        failures.release { io(5_000) { attachmentStore?.purgePreviews() } }
        failures.release { biometricHost?.close() } // Real Linux production unavailable host; no native prompt.
        failures.release {
            check(uiSettled && (rootJobs + listOfNotNull(scopeJob)).all { it.isCompleted }) {
                "NAV_OWNED_JOB_UNSETTLED_DATABASE_CLOSE_WITHHELD"
            }
            checkHome(home)
            io(5_000) { bootstrap?.checkpointAndClose()?.getOrThrow() }
            closed = true
        }
        failures.release {
            application?.let { owned ->
                assertTrue(GlobalContext.get() === owned.koin, "NAV_GLOBAL_KOIN_IDENTITY_CHANGED")
                GlobalContext.stopKoin() // Only the captured child's graph, never a pre-existing context.
            }
        }
        rootClear.clear()
        failures.throwIfPresent()
    }
}

/** No composition-local providers are substituted; lifecycle/store reads observe Window's actual owners. */
private class NativeNavHost(private val graph: GraphOwner, private val trace: Trace) {
    private val windowRef = AtomicReference<ComposeWindow?>()
    private val lifecycleRef = AtomicReference<Lifecycle?>()
    private val windowStore = AtomicReference<ViewModelStore?>()
    private val exitRequest = AtomicReference<(() -> Unit)?>()
    private val disposed = AtomicBoolean(false)
    private val frameRequest = mutableIntStateOf(0)
    private val frameAck = AtomicInteger(-1)
    private val pressedKeys = mutableSetOf<Int>()
    private var mousePressed = false
    private var robot: Robot? = null
    private var driver: Thread? = null
    private var task: FutureTask<Unit>? = null
    private var finishAttempted = false
    private val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(150)
    val driverSettled: Boolean get() = driver?.isAlive != true
    val storageCloseAllowed: Boolean
        get() = driverSettled && (windowRef.get() == null || disposed.get())

    fun runActualApplicationLoop() {
        val work = FutureTask(Callable {
            val failures = Failures()
            try { scenario() } catch (error: Throwable) { failures.add(error) }
            finally {
                failures.release { releaseInput() }
                failures.release { onEdt { requireNotNull(exitRequest.get()).invoke() } }
            }
            failures.throwIfPresent()
        }).also { task = it }
        val thread = Thread(work, "passvault-navhost-synthetic-driver").also { driver = it }
        thread.start()
        runDesktopApplicationLoop { Content { exitApplication() } }
        thread.join(5_000)
        assertFalse(thread.isAlive, "NAV_DRIVER_UNSETTLED_AFTER_LOOP")
        try { work.get(1, TimeUnit.SECONDS) } catch (error: ExecutionException) {
            throw requireNotNull(error.cause)
        }
        assertTrue(disposed.get(), "NAV_COMPOSITION_NOT_DISPOSED")
    }

    @Composable
    private fun Content(exit: () -> Unit) {
        Window(
            onCloseRequest = exit,
            title = "PassVault synthetic shared NavHost audit",
            state = rememberWindowState(width = 800.dp, height = 640.dp),
            resizable = false,
        ) {
            val nativeWindow = window
            val nativeLifecycle = LocalLifecycleOwner.current.lifecycle
            val nativeStore = requireNotNull(LocalViewModelStoreOwner.current).viewModelStore
            DisposableEffect(nativeWindow, nativeLifecycle, nativeStore) {
                windowRef.set(nativeWindow)
                lifecycleRef.set(nativeLifecycle)
                windowStore.set(nativeStore)
                exitRequest.set(exit)
                nativeWindow.toFront()
                nativeWindow.requestFocus()
                onDispose { disposed.set(true) }
            }
            val requested = frameRequest.intValue
            LaunchedEffect(requested) {
                withFrameNanos { }; withFrameNanos { }
                frameAck.set(requested) // Observation only; never a navigation/security acknowledgement.
            }
            PassVaultApp() // Owns the real NavHost, root/entry VMs, effects and live leave coordinator.
        }
    }

    private fun scenario() {
        await("NAV_REAL_RESUMED_WINDOW", seconds = 30) { onEdt {
            val current = windowRef.get()
            current != null && current.isActive && current.isShowing && current.windowHandle != 0L &&
                current.renderApi.toString() != "UNKNOWN" && lifecycleRef.get()?.currentState == Lifecycle.State.RESUMED
        } }
        robot = Robot().apply { autoDelay = 12 }
        replaceText("Master password", PASSWORD, checkReadable = false)
        click(button("Unlock vault"))
        await("NAV_REAL_NATIVE_UNLOCK", seconds = 30) { io {
            graph.vault.getSessionState().first() is VaultSessionState.Unlocked
        } }
        settle()
        assertSelectedHome()
        click(::credentialCard)
        click(button("Edit credential"))
        await("NAV_REAL_EDIT_ROUTE") { readText("Title") == TITLE }
        trace.record("NATIVE_UNLOCK_AND_EDIT")

        click { nodes -> fieldEdit(nodes, "originalname") }
        replaceText("Field Name", "draftname")
        replaceText("Value", "draftvalue")
        assertDraft()
        io { assertOriginal(graph.credentials) }
        trace.record("PENDING_TUPLE_NOT_PERSISTED")

        click(button("Back")) // Real toolbar -> event router -> effect collector; not a captured callback.
        stay()
        trace.record("TOOLBAR_KEEP_EDITING")
        pressEscape() // Actual host onPreviewKeyEvent -> NavigationBackCoordinator.
        stay()
        trace.record("HOST_ESCAPE_KEEP_EDITING")

        click(tab("Settings")) // Actual VaultTabShell, not a shell-equivalent conditional.
        stay()
        trace.record("DIRTY_TAB_STAYS_HOME")
        click(named("Add credential"))
        await("NAV_DIRTY_ADD_CONFIRMATION") { dialogVisible() }
        assertSelectedHome()
        click(button("Discard"))
        await("NAV_DISCARD_POP_TO_DETAIL") { onEdt {
            val nodes = snapshot()
            !hasDialog(nodes) && button("Edit credential")(nodes) != null && textField("Title")(nodes) == null &&
                nodes.none { it.name == "New credential" || it.name == "draftname" }
        } }
        assertSelectedHome()
        io { assertOriginal(graph.credentials) }
        trace.record("ADD_DISCARD_RETURNS_TO_DETAIL") // No queued Add replay: the previous detail is required.

        click(tab("Settings"))
        await("NAV_CLEAN_TAB_POSITIVE_CONTROL") { onEdt {
            val nodes = snapshot()
            tab("Settings")(nodes)?.selected == true && tab("Home")(nodes)?.selected == false
        } }
        click(tab("Home"))
        settle()
        assertSelectedHome()
        find(button("Edit credential")) // Home's real independent detail stack survived the round trip.
        trace.record("CLEAN_TAB_ROUNDTRIP_TO_DETAIL")
        pressEscape()
        await("NAV_CLEAN_BACK_TO_LIST") { onEdt {
            val nodes = snapshot()
            !hasDialog(nodes) && button("Edit credential")(nodes) == null && credentialCard(nodes) != null
        } }
        io { assertOriginal(graph.credentials) }
        trace.record("CLEAN_BACK_TO_LIST")
    }

    private fun stay() {
        await("NAV_REAL_DISCARD_DIALOG") { dialogVisible() }
        assertSelectedHome()
        click(button("Keep editing"))
        await("NAV_DIALOG_DISMISSED") { !dialogVisible() }
        settle()
        assertDraft()
        assertSelectedHome()
        io { assertOriginal(graph.credentials) }
    }

    private fun assertDraft() {
        assertEquals("draftname", readText("Field Name"), "NAV_STAY_LOST_PENDING_NAME")
        assertEquals("draftvalue", readText("Value"), "NAV_STAY_LOST_PENDING_VALUE")
        assertEquals(TITLE, readText("Title"), "NAV_STAY_WRONG_EDITOR")
        assertFalse(find { nodes -> unique(nodes.filter {
            it.role == AccessibleRole.CHECK_BOX && it.name == "Secret field"
        }) }.checked)
    }

    private fun assertSelectedHome() = onEdt {
        val nodes = snapshot()
        assertTrue(assertNotNull(tab("Home")(nodes), "NAV_REAL_HOME_TAB").selected)
        assertFalse(assertNotNull(tab("Settings")(nodes), "NAV_REAL_SETTINGS_TAB").selected)
    }

    private fun pressEscape() {
        val current = requireNotNull(windowRef.get())
        await("NAV_MAIN_ACTIVE_FOR_ESCAPE") { onEdt { current.isActive } }
        assertNativeWindow(current)
        withKey(KeyEvent.VK_ESCAPE) { }
        settle()
    }

    private fun readText(label: String): String? = find(textField(label)).text
    private fun dialogVisible(): Boolean = onEdt { hasDialog(snapshot()) }
    private fun settle() {
        val requested = onEdt { ++frameRequest.intValue }
        await("NAV_REAL_FRAME_PROGRESS") { frameAck.get() >= requested }
    }

    private fun replaceText(label: String, value: String, checkReadable: Boolean = true) {
        require(value.all { it in 'a'..'z' }) { "NAV_LOWERCASE_SYNTHETIC_INPUT_ONLY" }
        val selector = textField(label)
        click(selector)
        await("NAV_NATIVE_FIELD_FOCUS") { find(selector).focused }
        val owner = find(selector).window
        assertNativeWindow(owner)
        withKey(KeyEvent.VK_CONTROL) { withKey(KeyEvent.VK_A) { } }
        value.forEach { char ->
            assertNativeWindow(owner)
            withKey(KeyEvent.getExtendedKeyCodeForChar(char.code)) { }
        }
        settle()
        if (checkReadable) assertEquals(value, find(selector).text, "NAV_NATIVE_INPUT_RESULT")
    }

    private fun withKey(code: Int, block: () -> Unit) {
        val current = requireNotNull(robot)
        pressedKeys += code
        val failures = Failures()
        try { current.keyPress(code); block() } catch (error: Throwable) { failures.add(error) }
        finally { failures.release { current.keyRelease(code); pressedKeys -= code } }
        failures.throwIfPresent()
    }

    private fun click(selector: (List<Ax>) -> Ax?) {
        val target = scrollTo(selector)
        assertTrue(target.enabled, "NAV_TARGET_DISABLED")
        val rectangle = checkedRectangle(target)
        val current = requireNotNull(robot)
        current.mouseMove(rectangle.x + rectangle.width / 2, rectangle.y + rectangle.height / 2)
        val fresh = find(selector)
        assertTrue(target.context === fresh.context, "NAV_TARGET_REPLACED")
        assertEquals(rectangle, checkedRectangle(fresh), "NAV_TARGET_MOVED")
        assertTrue(fresh.enabled, "NAV_TARGET_NOW_DISABLED")
        mousePressed = true
        val failures = Failures()
        try { current.mousePress(InputEvent.BUTTON1_DOWN_MASK) } catch (error: Throwable) { failures.add(error) }
        finally { failures.release { current.mouseRelease(InputEvent.BUTTON1_DOWN_MASK); mousePressed = false } }
        failures.throwIfPresent()
    }

    private fun scrollTo(selector: (List<Ax>) -> Ax?): Ax {
        repeat(50) {
            val target = find(selector)
            val rectangle = assertNotNull(target.screen, "NAV_SCROLL_GEOMETRY")
            val client = onEdt { clientBounds(target.window) }
            if (client.contains(rectangle) && rectangle.width > 0 && rectangle.height > 0) {
                var previous = rectangle
                var since = System.nanoTime()
                await("NAV_STABLE_TARGET") {
                    val next = requireNotNull(find(selector).screen)
                    if (previous != next) { previous = next; since = System.nanoTime() }
                    client.contains(next) && System.nanoTime() - since >= TimeUnit.MILLISECONDS.toNanos(80)
                }
                return find(selector)
            }
            assertTrue(rectangle.width in 1..client.width, "NAV_HORIZONTAL_CLIPPING")
            assertNativeWindow(target.window)
            requireNotNull(robot).mouseMove(client.x + client.width / 2, client.y + client.height / 2)
            requireNotNull(robot).mouseWheel(if (rectangle.y < client.y) -4 else 4)
            settle()
        }
        fail("NAV_SCROLL_BOUND")
    }

    private fun checkedRectangle(target: Ax): Rectangle = onEdt {
        assertNativeWindow(target.window)
        Rectangle(assertNotNull(target.screen, "NAV_NO_GEOMETRY")).also {
            assertTrue(it.width > 0 && it.height > 0 && clientBounds(target.window).contains(it), "NAV_CLIPPED")
        }
    }

    private fun find(selector: (List<Ax>) -> Ax?): Ax {
        var result: Ax? = null
        await("NAV_UNIQUE_NATIVE_TARGET") { result = onEdt { selector(snapshot()) }; result != null }
        return requireNotNull(result)
    }

    private fun snapshot(): List<Ax> {
        val result = mutableListOf<Ax>()
        val seen = IdentityHashMap<AccessibleContext, Boolean>()
        fun visit(context: AccessibleContext?, owner: AwtWindow, parent: Int?, depth: Int) {
            if (context == null || seen.put(context, true) != null) return
            assertTrue(depth <= 48 && result.size < 4096, "NAV_A11Y_TREE_BOUND")
            val component = context.accessibleComponent
            val rectangle = try {
                component?.locationOnScreen?.let { Rectangle(it, component.size) }
            } catch (_: IllegalComponentStateException) { null }
            val text = context.accessibleText?.let { content ->
                val length = content.charCount
                assertTrue(length in 0..1024, "NAV_A11Y_TEXT_BOUND")
                buildString { repeat(length) { append(content.getAtIndex(AccessibleText.CHARACTER, it)) } }
            }
            val index = result.size
            val states = context.accessibleStateSet
            result += Ax(
                index, parent, context, owner, context.accessibleRole, context.accessibleName, text,
                context.accessibleEditableText != null, component?.isEnabled == true,
                states.contains(AccessibleState.FOCUSED), states.contains(AccessibleState.CHECKED),
                states.contains(AccessibleState.SELECTED), context.accessibleAction?.accessibleActionCount ?: 0,
                rectangle,
            )
            val children = context.accessibleChildrenCount
            assertTrue(children in 0..4096, "NAV_A11Y_CHILD_BOUND")
            repeat(children) { visit(context.getAccessibleChild(it)?.accessibleContext, owner, index, depth + 1) }
        }
        ownedWindows().filter { it.isShowing }.forEach { visit(it.accessibleContext, it, null, 0) }
        return result // Read-only accessibility. Never call AccessibleAction/EditableText mutators.
    }

    private fun ownedWindows(): List<AwtWindow> {
        val result = mutableListOf<AwtWindow>()
        fun visit(current: AwtWindow) {
            assertTrue(result.size < 8 && current !in result, "NAV_OWNED_WINDOW_BOUND")
            result += current
            current.ownedWindows.forEach(::visit)
        }
        windowRef.get()?.let(::visit)
        return result
    }

    private fun clientBounds(owner: AwtWindow): Rectangle {
        val insets = owner.insets
        val origin = owner.locationOnScreen
        return Rectangle(origin.x + insets.left, origin.y + insets.top,
            owner.width - insets.left - insets.right, owner.height - insets.top - insets.bottom).also {
            assertTrue(it.width in 200..1000 && it.height in 150..800, "NAV_CLIENT_SIZE")
            assertTrue(owner.graphicsConfiguration.bounds.contains(it), "NAV_CLIENT_OUTSIDE_PRIVATE_DISPLAY")
        }
    }

    private fun assertNativeWindow(owner: AwtWindow) = onEdt {
        assertTrue(owner in ownedWindows() && owner.isShowing && owner.isActive, "NAV_WRONG_NATIVE_WINDOW")
        clientBounds(owner)
        Unit
    }

    private fun await(message: String, seconds: Long = 5, condition: () -> Boolean) {
        val end = minOf(deadline, System.nanoTime() + TimeUnit.SECONDS.toNanos(seconds))
        while (System.nanoTime() < end) {
            if (Thread.currentThread().isInterrupted) throw InterruptedException("NAV_OWNED_DRIVER_INTERRUPTED")
            if (condition()) return
            Thread.sleep(20)
        }
        fail(message)
    }

    private fun releaseInput() {
        val failures = Failures()
        pressedKeys.toList().forEach { code -> failures.release {
            requireNotNull(robot).keyRelease(code); pressedKeys -= code
        } }
        if (mousePressed) failures.release {
            requireNotNull(robot).mouseRelease(InputEvent.BUTTON1_DOWN_MASK); mousePressed = false
        }
        failures.throwIfPresent()
    }

    fun finish() {
        if (finishAttempted) return
        finishAttempted = true
        val failures = Failures()
        failures.release {
            driver?.let { if (it.isAlive) it.interrupt(); it.join(5_000) }
            check(driverSettled) { "NAV_DRIVER_UNSETTLED_EXTERNAL_SETTLEMENT_REQUIRED" }
            task?.let { result ->
                try { result.get(1, TimeUnit.SECONDS) }
                catch (error: ExecutionException) { throw requireNotNull(error.cause) }
            }
        }
        if (driverSettled) failures.release { releaseInput() }
        failures.release { onEdt {
            ownedWindows().asReversed().forEach { it.dispose() }
            windowStore.get()?.clear() // Actual Window owner; does not instantiate or replace entry VMs.
        } }
        failures.release { onEdt {
            check(ownedWindows().none { it.isDisplayable }) { "NAV_WINDOW_UNSETTLED" }
        } }
        failures.throwIfPresent()
    }
}

private fun verifyFreshRoom(previous: GraphOwner, trace: Trace) {
    checkHome(previous.home)
    val bootstrap = createDatabaseBootstrap() // Registered locally before any new Room/native work.
    var vault: VaultRepositoryImpl? = null
    val failures = Failures()
    try {
        io {
            assertEquals(VaultDatabaseBootstrapResult.Ready, bootstrap.openAndVerify())
            val database = bootstrap.database()
            assertFalse(database === previous.database, "NAV_REUSED_CLOSED_ROOM_OBJECT")
            assertEquals(previous.databaseKey, databaseIdentity(previous.home), "NAV_DATABASE_FILE_REPLACED")
            val crypto = LibsodiumCryptoEngine()
            val current = VaultRepositoryImpl(database.vaultMetadataDao(), crypto, VaultKeyHierarchy(crypto))
                .also { vault = it }
            val credentials = CredentialRepositoryImpl(
                database.credentialDao(), database.folderDao(), database.tagDao(), database.attachmentDao(),
                database.passwordHistoryDao(), crypto, current,
            )
            password { current.unlock(it).getOrThrow() }
            assertOriginal(credentials)
        }
        trace.record("FRESH_ROOM_ORIGINAL_TWO_TUPLES")
    } catch (error: Throwable) {
        failures.add(error)
    } finally {
        failures.release { io(5_000) { vault?.lock(LockReason.Manual)?.getOrThrow() } }
        failures.release { io(5_000) { bootstrap.checkpointAndClose().getOrThrow() } } // One attempt, no retry.
        failures.release { checkHome(previous.home) }
    }
    failures.throwIfPresent()
    trace.record("FRESH_ROOM_CLOSED")
}

private suspend fun assertOriginal(repository: CredentialRepository) {
    assertEquals(listOf(CredentialId(ID)), repository.getAllSummaries().getOrThrow().map { it.id })
    val credential = assertNotNull(repository.getById(CredentialId(ID)).getOrThrow())
    try {
        assertEquals(TITLE, credential.title)
        assertEquals(CredentialType.SecureNote, credential.type)
        assertEquals(ORIGINAL_FIELDS, credential.customFields.map {
            FieldTuple(it.id.value, it.name, it.value.toStringUnsafe(), it.isSecret)
        }, "NAV_DURABLE_TUPLES_CHANGED")
    } finally { credential.clearSensitiveValues() }
}

private fun seedCredential() = Credential(
    id = CredentialId(ID), type = CredentialType.SecureNote, title = TITLE,
    username = null, email = null, password = null, urls = emptyList(), notes = null,
    recoveryCodes = emptyList(), apiKeys = emptyList(), licenseKeys = emptyList(),
    customFields = ORIGINAL_FIELDS.map {
        CustomField(CustomFieldId(it.id), it.name, SensitiveText.from(it.value), it.secret)
    },
    folderId = null, tagIds = emptySet(), isFavorite = false, attachments = emptyList(), passwordHistory = emptyList(),
    createdAt = Instant.fromEpochSeconds(1_700_000_000), updatedAt = Instant.fromEpochSeconds(1_700_000_000),
    lastUsedAt = null,
)

private suspend fun <T> password(block: suspend (SensitiveText) -> T): T {
    val password = SensitiveText.from(PASSWORD)
    return try { block(password) } finally { password.clear() }
}

private fun databaseIdentity(home: PrivateDirectory): Any {
    checkHome(home)
    val attributes = Files.readAttributes(
        home.path.resolve(".passvault/vault.db"), PosixFileAttributes::class.java, NOFOLLOW_LINKS,
    )
    assertTrue(attributes.isRegularFile && attributes.size() in 1..4_194_304, "NAV_REAL_BOUNDED_DATABASE_FILE")
    return requireNotNull(attributes.fileKey())
}

private data class Ax(
    val index: Int, val parent: Int?, val context: AccessibleContext, val window: AwtWindow,
    val role: AccessibleRole, val name: String?, val text: String?, val editable: Boolean, val enabled: Boolean,
    val focused: Boolean, val checked: Boolean, val selected: Boolean, val actionCount: Int, val screen: Rectangle?,
)

private fun unique(nodes: List<Ax>): Ax? {
    assertTrue(nodes.size <= 1, "NAV_AMBIGUOUS_TARGET")
    return nodes.singleOrNull()
}
private fun named(name: String): (List<Ax>) -> Ax? = { nodes -> unique(nodes.filter { it.name == name }) }
private fun button(name: String): (List<Ax>) -> Ax? = { nodes -> unique(nodes.filter {
    it.role == AccessibleRole.PUSH_BUTTON && it.name == name
}) }
private fun tab(name: String): (List<Ax>) -> Ax? = { nodes -> unique(nodes.filter {
    it.role == AccessibleRole.PAGE_TAB && it.name == name
}) }
private fun textField(label: String): (List<Ax>) -> Ax? = { nodes -> unique(nodes.filter {
    it.editable && (it.name == label || it.name?.startsWith("$label, ") == true)
}) }
private fun hasDialog(nodes: List<Ax>): Boolean = nodes.any { it.name == "Discard changes?" }
private fun credentialCard(nodes: List<Ax>): Ax? = unique(nodes.filter {
    it.actionCount > 0 && (it.name == TITLE || it.name?.startsWith("$TITLE, ") == true)
})

private fun fieldEdit(nodes: List<Ax>, fieldName: String): Ax? {
    val title = unique(nodes.filter { it.name == fieldName }) ?: return null
    var ancestor = title.parent
    while (ancestor != null) {
        val candidate = ancestor
        val members = nodes.filter { node ->
            var current: Int? = node.index
            while (current != null && current != candidate) current = nodes[current].parent
            current == candidate
        }
        val edits = members.filter { it.role == AccessibleRole.PUSH_BUTTON && it.name == "Edit" }
        if (edits.size == 1) return edits.single()
        ancestor = nodes[candidate].parent
    }
    return null
}

/** Metadata receipts complement, never replace, root's fresh allocation/admission. No filesystem deletion. */
private class PrivateDirectory private constructor(val path: Path, private val key: Any, private val user: String) {
    fun checkBound() {
        var ancestor = path.root
        path.forEach { ancestor = ancestor.resolve(it); assertFalse(Files.isSymbolicLink(ancestor), "NAV_LINK_PARENT") }
        assertEquals(path, path.toRealPath(), "NAV_NONCANONICAL_ROOT")
        val attributes = Files.readAttributes(path, PosixFileAttributes::class.java, NOFOLLOW_LINKS)
        assertTrue(attributes.isDirectory, "NAV_ROOT_NOT_DIRECTORY")
        assertEquals(key, attributes.fileKey(), "NAV_ROOT_REPLACED")
        assertEquals(user, attributes.owner().name, "NAV_ROOT_OWNER")
        assertEquals(PRIVATE_MODE, attributes.permissions(), "NAV_ROOT_MODE")
    }

    fun child(name: String): PrivateDirectory {
        require(Regex("[a-z-]+").matches(name))
        checkBound()
        return acquire(Files.createDirectory(path.resolve(name), PRIVATE_DIRECTORY).toString(), empty = true)
    }

    companion object {
        fun acquire(raw: String, empty: Boolean): PrivateDirectory {
            require(raw.length in 2..2048 && Regex("/[A-Za-z0-9._/-]+").matches(raw))
            val path = Path.of(raw)
            assertTrue(path.isAbsolute && path.normalize() == path && path.toString() == raw)
            val attributes = Files.readAttributes(path, PosixFileAttributes::class.java, NOFOLLOW_LINKS)
            val user = ProcessHandle.current().info().user().orElseThrow { IllegalStateException("NAV_PROCESS_USER") }
            return PrivateDirectory(path, requireNotNull(attributes.fileKey()), user).also {
                it.checkBound()
                if (empty) Files.newDirectoryStream(path).use { stream ->
                    assertFalse(stream.iterator().hasNext(), "NAV_ROOT_NOT_FRESH_EMPTY")
                }
            }
        }
    }
}

private class Trace(private val path: Path, private val key: String) {
    private val parent = PrivateDirectory.acquire(property("evidenceDir"), empty = false)
    init {
        assertEquals(parent.path.resolve("navhost.events"), path, "NAV_TRACE_OUTSIDE_ADMITTED_EVIDENCE")
        assertTrue(path.isAbsolute && path.normalize() == path && path.toRealPath() == path)
        val attributes = Files.readAttributes(path, PosixFileAttributes::class.java, NOFOLLOW_LINKS)
        assertTrue(attributes.isRegularFile && attributes.size() == 0L)
        assertEquals(key, attributes.fileKey().toString())
        assertEquals(ProcessHandle.current().info().user().orElseThrow(), attributes.owner().name)
        assertEquals(PosixFilePermissions.fromString("rw-------"), attributes.permissions())
    }

    fun record(event: String) {
        require(event in EXPECTED_EVENTS)
        parent.checkBound()
        assertEquals(key, Files.readAttributes(
            path, PosixFileAttributes::class.java, NOFOLLOW_LINKS,
        ).fileKey().toString())
        FileChannel.open(path, WRITE, APPEND, NOFOLLOW_LINKS).use { channel ->
            val bytes = ByteBuffer.wrap((event + "\n").toByteArray(Charsets.UTF_8))
            while (bytes.hasRemaining()) check(channel.write(bytes) > 0)
            channel.force(true)
        }
    }
}

/** Fixture-local primary/suppressed/interruption handling; native calls still require external hard settlement. */
private class Failures {
    private var first: Throwable? = null
    private var interrupted = Thread.currentThread().isInterrupted
    val failed: Boolean get() = first != null
    fun add(error: Throwable) {
        if (error is InterruptedException || error.cause is InterruptedException) interrupted = true
        val primary = first
        if (primary == null) first = error else if (primary !== error) primary.addSuppressed(error)
        if (Thread.interrupted()) interrupted = true
    }
    fun release(block: () -> Unit) {
        if (Thread.interrupted()) interrupted = true
        try { block() } catch (error: Throwable) { add(error) }
        finally { if (Thread.interrupted()) interrupted = true }
    }
    fun problem(): Throwable? = first
    fun restoreInterrupt() {
        if (Thread.interrupted()) interrupted = true
        if (interrupted) Thread.currentThread().interrupt()
    }
    fun throwIfPresent() { restoreInterrupt(); first?.let { throw it } }
}

private fun checkPlatform(display: String) {
    assertEquals("Linux", System.getProperty("os.name"))
    assertEquals(17, Runtime.version().feature())
    assertTrue(Regex(":\\d{1,5}(?:\\.0)?").matches(display))
    assertEquals(display, System.getenv("DISPLAY"))
    assertTrue(System.getenv("WAYLAND_DISPLAY").isNullOrBlank())
    assertEquals("en", Locale.getDefault().language)
    assertTrue(System.getProperty("compose.accessibility.enable") != "false")
    assertEquals(null, System.getenv("COMPOSE_DISABLE_ACCESSIBILITY"))
}
private fun property(name: String): String = requireNotNull(System.getProperty(PREFIX + name)) { "NAV_PROPERTY_$name" }
private fun checkHome(home: PrivateDirectory) {
    home.checkBound()
    assertEquals(home.path.toString(), property("home"), "NAV_HOME_RECEIPT_CHANGED")
    assertEquals(home.path.toString(), System.getProperty("user.home"), "NAV_JVM_HOME_CHANGED")
}
private fun <T> io(timeoutMillis: Long = 60_000, block: suspend () -> T): T =
    runBlocking { withTimeout(timeoutMillis) { block() } }
private fun <T> onEdt(block: () -> T): T {
    if (SwingUtilities.isEventDispatchThread()) return block()
    val task = FutureTask(Callable(block))
    SwingUtilities.invokeLater(task)
    return try { task.get(5, TimeUnit.SECONDS) } catch (error: Throwable) { task.cancel(false); throw error }
}

private data class FieldTuple(val id: String, val name: String, val value: String, val secret: Boolean)
private const val PREFIX = "passvault.navhost."
private const val ID = "synthetic-navhost-credential"
private const val TITLE = "syntheticnavhostentry"
private const val PASSWORD = "syntheticnavhostpasswordonly"
private const val MAX_LOG_BYTES = 256L * 1024
private val PRIVATE_MODE = PosixFilePermissions.fromString("rwx------")
private val PRIVATE_DIRECTORY = PosixFilePermissions.asFileAttribute(PRIVATE_MODE)
private val PRIVATE_FILE = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------"))
private val ORIGINAL_FIELDS = listOf(
    FieldTuple("synthetic-nav-field-one", "originalname", "originalvalue", false),
    FieldTuple("synthetic-nav-field-two", "survivorname", "survivorvalue", true),
)
private val EXPECTED_EVENTS = listOf(
    "SEED_LOCKED", "NATIVE_UNLOCK_AND_EDIT", "PENDING_TUPLE_NOT_PERSISTED", "TOOLBAR_KEEP_EDITING",
    "HOST_ESCAPE_KEEP_EDITING", "DIRTY_TAB_STAYS_HOME", "ADD_DISCARD_RETURNS_TO_DETAIL",
    "CLEAN_TAB_ROUNDTRIP_TO_DETAIL", "CLEAN_BACK_TO_LIST", "APP_GRAPH_CLOSED",
    "FRESH_ROOM_ORIGINAL_TWO_TUPLES", "FRESH_ROOM_CLOSED", "COMPLETE",
)
