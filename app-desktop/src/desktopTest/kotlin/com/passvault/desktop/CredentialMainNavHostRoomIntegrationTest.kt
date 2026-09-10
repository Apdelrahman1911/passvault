package com.passvault.desktop

import com.passvault.core.database.VaultDatabaseBootstrap
import com.passvault.core.database.VaultDatabaseBootstrapResult
import com.passvault.core.domain.model.Credential
import com.passvault.core.domain.model.CredentialId
import com.passvault.core.domain.model.CredentialType
import com.passvault.core.domain.model.CustomField
import com.passvault.core.domain.model.CustomFieldId
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.repository.CredentialRepository
import com.passvault.core.domain.repository.LockReason
import com.passvault.core.domain.repository.VaultRepository
import com.passvault.desktop.di.desktopModule
import com.passvault.desktop.security.biometric.DesktopBiometricHost
import com.passvault.shared.di.AppModule
import com.passvault.feature.backup.presentation.BackupViewModel
import com.passvault.feature.settings.presentation.SettingsViewModel
import java.awt.MenuItem
import java.awt.PopupMenu
import java.awt.SystemTray
import java.awt.TrayIcon
import java.awt.event.ActionListener
import java.awt.AWTEvent
import java.awt.Dialog
import java.awt.FileDialog
import java.awt.Toolkit
import java.awt.event.AWTEventListener
import java.awt.event.MouseEvent
import java.util.concurrent.atomic.AtomicInteger
import org.koin.core.context.GlobalContext
import java.awt.Frame
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
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit
import javax.accessibility.AccessibleContext
import javax.accessibility.AccessibleRole
import javax.accessibility.AccessibleState
import javax.accessibility.AccessibleText
import javax.swing.SwingUtilities
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assume.assumeTrue
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication
import kotlin.system.exitProcess
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlin.time.Instant

/**
 * Opt-in Linux integration: each case uses three serial fresh JVM roles.
 * Real Main/Window/NavHost/native input, then real Room reopen after Main process exit.
 * No test-worker home mutation, fake providers, lifecycle locals, direct UI callbacks or Main graph cleanup.
 * Root must admit actual Test classpath/property forwarding, private X/WM, fresh roots and external cleanup.
 * Native accessibility and geometry require an admitted execution; source review alone does not verify them.
 * Linux Main reaches actual tray/window protection/instance lock; no hardware assurance.
 * PVA027 samples installed tray language properties, not native tray input or displayed tooltips.
 */
class CredentialMainNavHostRoomIntegrationTest {
    @Test
    fun `actual Main guards dirty Back tab and Add without changing durable Room tuples`() =
        runCase(MainNavCase.CREDENTIAL)

    internal fun runPvu003Chooser() {
        require(property("pvu003Chooser") == "true")
        requireNotNull(System.getProperty(PREFIX + "syntheticDisplay")) // Opted-in misconfiguration must fail.
        runCase(MainNavCase.PVU003_CHOOSER)
    }

    internal fun runPva027SettingsTray() {
        require(property("pva027SettingsTray") == "true")
        requireNotNull(System.getProperty(PREFIX + "syntheticDisplay")) // Opted-in misconfiguration must fail.
        runCase(MainNavCase.PVA027_SETTINGS_TRAY)
    }

    private fun runCase(case: MainNavCase) {
        val display = System.getProperty(PREFIX + "syntheticDisplay")
        assumeTrue("Requires separately admitted synthetic X display", display != null)
        checkPlatform(requireNotNull(display)) // Metadata only: parent never initializes AWT, Koin or Room.
        val runtime = MainNavDirectory.acquire(property(case.runtimeProperty), empty = true)
        val evidence = MainNavDirectory.acquire(property(case.evidenceProperty), empty = true)
        assertFalse(runtime.path.startsWith(evidence.path) || evidence.path.startsWith(runtime.path))
        val home = runtime.child("home")
        val classpath = property("childClasspath").also { require(it.isNotBlank()) }
        val authority = Path.of(requireNotNull(System.getenv("XAUTHORITY")))
        assertTrue(authority.isAbsolute && Files.isRegularFile(authority, NOFOLLOW_LINKS))
        var databaseKey: String? = null
        for (role in listOf("seed", "main", "verify")) {
            if (role != "seed") assertEquals(databaseKey, databaseIdentity(home))
            runRole(
                role, runtime, evidence, home, classpath, display, authority, databaseKey.orEmpty(), case,
            )
            if (role == "seed") databaseKey = databaseIdentity(home)
            else assertEquals(databaseKey, databaseIdentity(home), "MAIN_NAV_DATABASE_REPLACED")
        } // No next role after any assertion, child, cleanup, log or receipt failure.
    }

    private fun runRole(
        role: String, runtime: MainNavDirectory, evidence: MainNavDirectory, home: MainNavDirectory,
        classpath: String, display: String, authority: Path, databaseKey: String, case: MainNavCase,
    ) {
        val phase = runtime.child(role)
        val tmp = phase.child("tmp")
        val userPrefs = phase.child("prefs-user")
        val systemPrefs = phase.child("prefs-system")
        val trace = Files.createFile(evidence.path.resolve("$role.events"), PRIVATE_FILE)
        val log = Files.createFile(evidence.path.resolve("$role.log"), PRIVATE_FILE)
        val traceKey = fileKey(trace)
        val logKey = fileKey(log)
        val environment = mutableMapOf(
            "PATH" to "/usr/bin:/bin", "LANG" to "C.UTF-8", "LC_ALL" to "C.UTF-8", "TZ" to "UTC",
            "HOME" to home.path.toString(), "TMPDIR" to tmp.path.toString(),
            "TMP" to tmp.path.toString(), "TEMP" to tmp.path.toString(),
            "DISPLAY" to display, "XAUTHORITY" to authority.toString(),
        )
        // env.clear() must not discard the outer owner's private bus/bridge guards.
        environment.putAll(privateSessionEnvironment(authority))
        for (kind in listOf("CACHE", "CONFIG", "DATA", "STATE")) {
            environment["XDG_${kind}_HOME"] = phase.child("xdg-${kind.lowercase()}").path.toString()
        }
        val builder = ProcessBuilder(
            Path.of(System.getProperty("java.home"), "bin", "java").toString(),
            "-Xmx512m", "-XX:ActiveProcessorCount=1", "-XX:-UsePerfData", "-Dfile.encoding=UTF-8",
            "-XX:-CreateCoredumpOnCrash", "-XX:ErrorFile=${phase.path}/hs_err_pid%p.log",
            "-Djava.awt.headless=false", "-Duser.language=en", "-Duser.country=US",
            "-Duser.home=${home.path}", "-Djava.io.tmpdir=${tmp.path}",
            "-Djava.util.prefs.userRoot=${userPrefs.path}", "-Djava.util.prefs.systemRoot=${systemPrefs.path}",
            "-Djna.tmpdir=${tmp.path}", "-Dorg.sqlite.tmpdir=${tmp.path}",
            "-D${PREFIX}syntheticDisplay=$display", "-D${PREFIX}home=${home.path}",
            "-D${PREFIX}phase=${phase.path}", "-D${PREFIX}evidenceDir=${evidence.path}",
            "-D${PREFIX}pvu003Chooser=${case == MainNavCase.PVU003_CHOOSER}",
            "-D${PREFIX}pva027SettingsTray=${case == MainNavCase.PVA027_SETTINGS_TRAY}",
            "-D${PREFIX}ownedChild=true", "-cp", classpath, CredentialMainNavHostRoomProbe::class.java.name,
            role, trace.toString(), traceKey, databaseKey, case.name,
        ).directory(phase.path.toFile()).redirectErrorStream(true).redirectOutput(log.toFile())
        builder.environment().clear()
        builder.environment().putAll(environment)
        val failures = MainNavFailures()
        var child: Process? = null
        try {
            runtime.checkBound(); evidence.checkBound(); home.checkBound()
            val running = builder.start().also { child = it } // Captured before any wait/assertion.
            running.outputStream.close()
            val seconds = if (role == "main") 200L else 90L // <=380s + three 5s settlements, serial.
            val end = System.nanoTime() + TimeUnit.SECONDS.toNanos(seconds)
            while (!running.waitFor(100, TimeUnit.MILLISECONDS)) {
                assertTrue(System.nanoTime() < end, "MAIN_NAV_${role}_DEADLINE")
                checkedFile(log, logKey, MAX_LOG_BYTES)
                checkedFile(trace, traceKey, 8192)
                assertFalse(Files.readAllLines(trace).contains("DRIVER_FAILURE"), "MAIN_NAV_DRIVER_FAILED")
            }
            checkedFile(log, logKey, MAX_LOG_BYTES); checkedFile(trace, traceKey, 8192)
            assertEquals(0, running.exitValue(), "MAIN_NAV_${role}_EXIT_SEE_LOG")
            val output = Files.readString(log)
            FORBIDDEN_LOGS.forEach { assertFalse(output.contains(it), "MAIN_NAV_TERMINAL_DIAGNOSTIC_$it") }
            assertMainNavEvents(role, case, Files.readAllLines(trace))
        } catch (error: Throwable) { failures.add(error) }
        finally {
            failures.release {
                child?.let { running ->
                    if (running.isAlive) running.destroyForcibly() // Failure containment, never a passing close.
                    check(running.waitFor(5, TimeUnit.SECONDS)) { "MAIN_NAV_CHILD_UNSETTLED_CLEANUP_BLOCKED" }
                    Files.writeString(
                        evidence.path.resolve("$role.exit"), "${running.exitValue()}\n", CREATE_NEW, WRITE,
                    )
                }
            }
            failures.release { runtime.checkBound(); evidence.checkBound(); home.checkBound() }
            // No deletion or wrapper stop here; sole outer owner settles all owned workers/display and allowlists.
            failures.throwIfPresent()
        }
    }
}

/** Separate observation case: absent opt-in is an ignored test, never admitted/passing PVU003 evidence. */
class Pvu003NativeChooserAdmissionIntegrationTest {
    @Test
    fun `actual Main samples Home while the export chooser is modal then cancels`() {
        assumeTrue(
            "Requires separate PVU003 chooser admission; not integration02",
            System.getProperty(PREFIX + "pvu003Chooser") == "true",
        )
        CredentialMainNavHostRoomIntegrationTest().runPvu003Chooser()
    }
}

/** Separate property-only caller-chain case; no endpoint replay, tooltip pixels or tray callback claim. */
class Pva027MainSettingsTrayPropagationIntegrationTest {
    @Test
    fun `actual Main Settings updates installed tray properties through English Arabic English`() {
        assumeTrue(
            "Requires separate PVA027 Settings admission; not integration03",
            System.getProperty(PREFIX + "pva027SettingsTray") == "true",
        )
        CredentialMainNavHostRoomIntegrationTest().runPva027SettingsTray()
    }
}

/** Each role is a fresh JVM. Actual Main, not this fixture, owns the application role's terminal cleanup. */
internal object CredentialMainNavHostRoomProbe {
    @JvmStatic
    fun main(args: Array<String>) {
        try {
            require(args.size == 5 && args[0] in EVENTS && property("ownedChild") == "true")
            val role = args[0]
            val case = MainNavCase.valueOf(args[4])
            require(case != MainNavCase.PVU003_CHOOSER || property("pvu003Chooser") == "true")
            require(case != MainNavCase.PVA027_SETTINGS_TRAY || property("pva027SettingsTray") == "true")
            checkPlatform(property("syntheticDisplay"))
            val home = MainNavDirectory.acquire(property("home"), empty = role == "seed")
            val phase = MainNavDirectory.acquire(property("phase"), empty = false)
            assertFalse(home.path.startsWith(phase.path) || phase.path.startsWith(home.path))
            checkHome(home)
            assertEquals(home.path.toString(), System.getenv("HOME"))
            for ((directory, property) in listOf(
                "tmp" to "java.io.tmpdir", "prefs-user" to "java.util.prefs.userRoot",
                "prefs-system" to "java.util.prefs.systemRoot",
            )) {
                val owned = MainNavDirectory.acquire(phase.path.resolve(directory).toString(), empty = true)
                assertEquals(owned.path.toString(), System.getProperty(property))
            }
            val trace = MainNavTrace(role, Path.of(args[1]), args[2], case)
            if (role == "main") {
                assertEquals(args[3], databaseIdentity(home))
                assertFalse(GraphicsEnvironment.isHeadless())
                Thread({
                    try { NativeMainNav(trace, case).run() }
                    catch (error: Throwable) {
                        System.err.println("MAIN_NAV_FAILURE")
                        error.printStackTrace(System.err)
                        runCatching { trace.record("DRIVER_FAILURE") }
                    }
                }, "passvault-main-nav-synthetic-driver").start()
                com.passvault.desktop.main() // Real Main includes global Koin, wrapper, instance lock and System.exit.
                error("MAIN_NAV_MAIN_RETURNED_WITHOUT_OWNED_PROCESS_EXIT")
            }
            roomRole(role, home, args[3], trace)
        } catch (error: Throwable) {
            System.err.println("MAIN_NAV_FAILURE")
            error.printStackTrace(System.err)
            exitProcess(1)
        }
        exitProcess(0) // Seed/verify only; no Main cleanup or graph can run in these processes.
    }
}

private fun roomRole(role: String, home: MainNavDirectory, expectedKey: String, trace: MainNavTrace) {
    var application: KoinApplication? = null
    var bootstrap: VaultDatabaseBootstrap? = null
    var vault: VaultRepository? = null
    var biometric: DesktopBiometricHost? = null
    val failures = MainNavFailures()
    try {
        if (role != "seed") assertEquals(expectedKey, databaseIdentity(home))
        // Real lazy modules, local Koin only; no GlobalContext, UI VM, clipboard, tray or application scope.
        val app = koinApplication { modules(AppModule.getAllModules(desktopModule)) }.also { application = it }
        val store = app.koin.get<VaultDatabaseBootstrap>().also { bootstrap = it }
        io { assertEquals(VaultDatabaseBootstrapResult.Ready, store.openAndVerify()) }
        biometric = app.koin.get() // Linux's real unavailable host; register its one close before vault resolution.
        val session = app.koin.get<VaultRepository>().also { vault = it }
        val credentials = app.koin.get<CredentialRepository>() // Also resolves actual private attachment blob store.
        io {
            password { secret ->
                if (role == "seed") session.create(secret).getOrThrow()
                session.unlock(secret).getOrThrow()
            }
            if (role == "seed") {
                val item = seedCredential()
                try { credentials.save(item).getOrThrow() } finally { item.clearSensitiveValues() }
            }
            assertOriginal(credentials)
        }
        trace.record(if (role == "seed") "SEED_ORIGINAL_TUPLES" else "VERIFY_ORIGINAL_TUPLES")
    } catch (error: Throwable) { failures.add(error) }
    finally {
        // NonCancellable/native work can outlive these nominal limits: parent hard deadline remains required.
        failures.release { io(5_000) { vault?.lock(LockReason.Manual)?.getOrThrow() } }
        failures.release { io(5_000) { bootstrap?.checkpointAndClose()?.getOrThrow() } } // Exactly one attempt.
        failures.release { biometric?.close() }
        failures.release { application?.close() } // Not a substitute for the explicit real component closes above.
        failures.release { checkHome(home) }
    }
    failures.throwIfPresent()
    if (role != "seed") assertEquals(expectedKey, databaseIdentity(home))
    trace.record(if (role == "seed") "SEED_ROOM_CLOSED" else "VERIFY_ROOM_CLOSED")
}

private class NativeMainNav(private val trace: MainNavTrace, private val case: MainNavCase) {
    private lateinit var frame: Frame
    private lateinit var robot: Robot
    private val pressedKeys = mutableSetOf<Int>()
    private var mousePressed = false
    private val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(150)

    fun run() {
        await("MAIN_NAV_REAL_ACTIVE_WINDOW", 30) { onEdt {
            val candidates = AwtWindow.getWindows().filterIsInstance<Frame>()
                .filter { it.isShowing && it.title.startsWith("PassVault") }
            assertTrue(candidates.size <= 1, "MAIN_NAV_AMBIGUOUS_MAIN_WINDOW")
            candidates.singleOrNull()?.takeIf { it.isActive }?.also { frame = it } != null
        } }
        robot = Robot().apply { autoDelay = 8; isAutoWaitForIdle = false }
        val failures = MainNavFailures()
        try {
            if (case == MainNavCase.PVU003_CHOOSER) {
                val observed = pvu003ChooserHomeThenCancel() // Returns only after its native/listener cleanup.
                require(observed.frameDeliveryBits in 0..15)
                trace.record(
                    "PVU003_CHOOSER_HOME=${if (observed.homeSelectedWhileChooserShowing) 1 else 0};" +
                        "AFTER_CANCEL=${if (observed.homeSelectedAfterNativeCancel) 1 else 0};" +
                        "FRAME_DELIVERY=${observed.frameDeliveryBits.toString(16)}",
                )
            } else if (case == MainNavCase.PVA027_SETTINGS_TRAY) {
                settingsTrayPropagationOnlyScenario()
            } else {
                scenario()
            }
            releaseInput()
            trace.record("DRIVER_ASSERTIONS_COMPLETE")
            SwingUtilities.invokeLater {
                try {
                    assertNativeWindow(frame)
                    // EDT cannot handle queued Ctrl+Q before DOWN/UP and this receipt finish.
                    withKey(KeyEvent.VK_CONTROL) { withKey(KeyEvent.VK_Q) { } }
                    trace.record("QUIT_KEY_CALLS_COMPLETE") // Issuance, not physical key-state/cleanup proof.
                } catch (error: Throwable) {
                    // Report INSIDE EDT, before queued Quit can System.exit; no timed driver wait races this receipt.
                    System.err.println("MAIN_NAV_FAILURE")
                    error.printStackTrace(System.err)
                    runCatching { trace.record("DRIVER_FAILURE") }
                }
            }
        } catch (error: Throwable) { failures.add(error) }
        finally { if (failures.failed) failures.release { releaseInput() } }
        failures.throwIfPresent() // Main may already System.exit; no post-Main receipt/finalizer is relied upon.
    }

    private data class Pvu003ChooserObservation(
        val homeSelectedWhileChooserShowing: Boolean,
        val homeSelectedAfterNativeCancel: Boolean,
        // 1/2: frame press/release delivered while chooser showing; 4/8: delivered after hiding.
        // Zero is NOT proof that native input was absent or that a specific gate rejected it.
        val frameDeliveryBits: Int,
    )

    private fun pvu003ChooserHomeThenCancel(): Pvu003ChooserObservation {
        val failures = MainNavFailures()
        val delivery = AtomicInteger()
        var chooser: FileDialog? = null
        var model: BackupViewModel? = null
        var listener: AWTEventListener? = null
        var exportAttempted = false
        var homeDuring = false
        var homeAfter = false
        try {
            replaceText("Master password", PASSWORD, readable = false)
            click(button("Unlock vault"))
            await("PVU003_NATIVE_UNLOCK", 30) { onEdt { credentialCard(snapshot()) != null } }
            click(tab("Settings"))
            await("PVU003_NATIVE_SETTINGS") { onEdt { tab("Settings")(snapshot())?.selected == true } }
            click(named("Data Management"))
            click(button("Export"))
            val passphrase = "jveoqzxvcuomndkrpsghwaxyfitbel" // Synthetic; a..z native driver; not the master.
            replaceText("Backup passphrase", passphrase, readable = false)
            val backup = onEdt { GlobalContext.get().get<BackupViewModel>() }.also { model = it }
            await("PVU003_REAL_EXPORT_POLICY") { onEdt {
                val state = backup.state.value
                state.canExport && state.exportPassword == passphrase
            } }
            onEdt { assertEquals(null, pvu003VisibleChooser(), "PVU003_PREEXISTING_CHOOSER") }
            exportAttempted = true // Capture possible input ownership even if click/release throws.
            click(button("Save encrypted backup"))
            await("PVU003_ACTUAL_CHOOSER") { onEdt {
                pvu003VisibleChooser()?.also { chooser = it } != null
            } }
            val dialog = requireNotNull(chooser)
            val home = onEdt { pvu003ModalHomeTarget(dialog) }
            val rectangle = Rectangle(assertNotNull(home.screen))
            assertFalse(home.selected, "PVU003_HOME_ALREADY_SELECTED")
            onEdt { assertTrue(backup.state.value.isExporting, "PVU003_NOT_EXPORTING") }
            val observer = AWTEventListener { event ->
                if (event is MouseEvent && event.button == MouseEvent.BUTTON1 &&
                    (event.id == MouseEvent.MOUSE_PRESSED || event.id == MouseEvent.MOUSE_RELEASED)
                ) {
                    val source = event.component
                    val owner = (source as? AwtWindow) ?: SwingUtilities.getWindowAncestor(source)
                    if (owner === frame && rectangle.contains(event.xOnScreen, event.yOnScreen)) {
                        val pressed = event.id == MouseEvent.MOUSE_PRESSED
                        val bit = if (dialog.isShowing) { if (pressed) 1 else 2 } else { if (pressed) 4 else 8 }
                        delivery.getAndUpdate { it or bit } // Passive, constant-memory, no logging or native input.
                    }
                }
            }
            listener = observer // Registration may finish after a timed-out onEdt; preserve removal ownership.
            onEdt { Toolkit.getDefaultToolkit().addAWTEventListener(observer, AWTEvent.MOUSE_EVENT_MASK) }
            robot.mouseMove(rectangle.x + rectangle.width / 2, rectangle.y + rectangle.height / 2)
            onEdt {
                val current = pvu003ModalHomeTarget(dialog)
                assertTrue(current.context === home.context && current.screen == rectangle, "PVU003_MOVED_HOME")
                assertFalse(current.selected, "PVU003_HOME_CHANGED_BEFORE_INPUT")
                assertTrue(backup.state.value.isExporting, "PVU003_EXPORT_CHANGED_BEFORE_INPUT")
            }
            assertFalse(mousePressed)
            assertTrue(pressedKeys.isEmpty())
            val input = MainNavFailures()
            mousePressed = true
            try { robot.mousePress(InputEvent.BUTTON1_DOWN_MASK) } catch (error: Throwable) { input.add(error) }
            finally { input.release { robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK); mousePressed = false } }
            input.throwIfPresent() // Exactly one native Home press/release; no active-frame click/focus bypass.
            repeat(20) {
                pvu003ObservationPause()
                onEdt {
                    assertTrue(dialog.owner === frame && dialog.isShowing, "PVU003_CHOOSER_LEFT_OBSERVATION")
                    assertTrue(backup.state.value.isExporting, "PVU003_EXPORT_LEFT_OBSERVATION")
                    val selected = assertNotNull(tab("Home")(snapshot().filter { it.window === frame })).selected
                    homeDuring = homeDuring || selected
                }
            }
        } catch (error: Throwable) { failures.add(error) }
        finally {
            if (exportAttempted) failures.release {
                // One bounded cleanup, not another scenario. Never select a file, Save, invoke a callback,
                // send OnCancelOperation, change modality, call dispose, or act on an unowned window.
                val dialog = chooser ?: onEdt { pvu003VisibleChooser() }
                assertNotNull(dialog, "PVU003_NO_SAFE_NATIVE_CANCEL_TARGET")
                await("PVU003_CANCEL_TARGET_FOCUS") { onEdt {
                    assertTrue(dialog.owner === frame && dialog in ownedWindows(), "PVU003_CANCEL_OWNER_CHANGED")
                    assertEquals(FileDialog.SAVE, dialog.mode)
                    assertEquals("Save encrypted backup", dialog.title)
                    dialog.isShowing && dialog.isActive && dialog.isFocused
                } }
                assertNativeWindow(dialog)
                withKey(KeyEvent.VK_ESCAPE) { } // Exactly one Escape; no retry or synthesized Cancel callback.
                await("PVU003_NATIVE_CHOOSER_CANCELLED") { onEdt { !dialog.isShowing } }
                onEdt { assertEquals(null, dialog.file, "PVU003_UNEXPECTED_FILE_SELECTION") }
                if (!failures.failed) {
                    val backup = requireNotNull(model)
                    await("PVU003_EXPORT_IDLE_AFTER_CANCEL") { onEdt {
                        frame.isShowing && frame.isActive && !backup.state.value.hasActiveOperation
                    } }
                    repeat(10) {
                        pvu003ObservationPause()
                        onEdt {
                            assertFalse(dialog.isShowing)
                            val state = backup.state.value
                            assertFalse(state.hasActiveOperation)
                            assertEquals(null, state.successMessage, "PVU003_UNEXPECTED_EXPORT_SUCCESS")
                            val selected = assertNotNull(tab("Home")(snapshot().filter { it.window === frame })).selected
                            homeAfter = homeAfter || selected
                        }
                    }
                }
            }
            listener?.let { owned -> failures.release {
                onEdt { Toolkit.getDefaultToolkit().removeAWTEventListener(owned) }
            } }
        }
        failures.throwIfPresent() // Cancellation/listener failures preserve primary and suppressed errors.
        return Pvu003ChooserObservation(homeDuring, homeAfter, delivery.get())
    }

    // EDT-only source observation. Ambiguity/unsupported toolkit geometry is a failure, not permission to move windows.
    private fun pvu003VisibleChooser(): FileDialog? {
        check(SwingUtilities.isEventDispatchThread())
        val candidates = ownedWindows().filterIsInstance<FileDialog>().filter { it.isShowing }
        assertTrue(candidates.size <= 1, "PVU003_AMBIGUOUS_CHOOSER")
        return candidates.singleOrNull()
    }

    private fun pvu003ModalHomeTarget(dialog: FileDialog): MainNavAx {
        check(SwingUtilities.isEventDispatchThread())
        val visible = ownedWindows().filter { it.isShowing }
        assertTrue(visible.size == 2 && frame in visible && dialog in visible, "PVU003_UNEXPECTED_OWNED_SURFACE")
        assertTrue(dialog.owner === frame && frame.isShowing && frame.isDisplayable, "PVU003_WRONG_CHOOSER_OWNER")
        assertEquals(FileDialog.SAVE, dialog.mode)
        assertEquals("Save encrypted backup", dialog.title)
        assertTrue(dialog.isModal && dialog.isActive && dialog.isFocused, "PVU003_CHOOSER_NOT_FOCUSED_MODAL")
        assertEquals(Dialog.ModalityType.APPLICATION_MODAL, dialog.modalityType)
        assertEquals(Dialog.ModalExclusionType.NO_EXCLUDE, frame.modalExclusionType)
        clientBounds(dialog) // Existing admitted-display/client-size preconditions; never change window bounds.
        val home = assertNotNull(tab("Home")(snapshot().filter { it.window === frame }))
        val rectangle = assertNotNull(home.screen)
        assertTrue(home.enabled && rectangle.width > 0 && rectangle.height > 0, "PVU003_HOME_NOT_LIVE")
        assertTrue(clientBounds(frame).contains(rectangle), "PVU003_HOME_OUTSIDE_CLIENT")
        val chooserArea = Rectangle(dialog.locationOnScreen, dialog.size).apply { grow(16, 16) }
        assertFalse(chooserArea.intersects(rectangle), "PVU003_HOME_OCCLUDED_BY_CHOOSER")
        return home
    }

    private fun pvu003ObservationPause() {
        assertTrue(System.nanoTime() < deadline, "PVU003_DRIVER_DEADLINE")
        Thread.sleep(20) // Poll spacing only, never an acknowledgement of Compose/frame/coroutine progress.
    }
    private fun settingsTrayPropagationOnlyScenario() {
        val originalFrame = frame
        replaceText("Master password", PASSWORD, readable = false)
        click(button("Unlock vault"))
        await("PVA027_MAIN_UNLOCK", 30) { onEdt { credentialCard(snapshot()) != null } }
        click(tab("Settings"))
        await("PVA027_MAIN_SETTINGS") { onEdt { tab("Settings")(snapshot())?.selected == true } }
        pva027Click { nodes -> unique(nodes.filter { node ->
            node.actionCount > 0 &&
                (node.name == "App language" || node.name?.startsWith("App language, ") == true)
        }) }

        fun languageOption(label: String): (List<MainNavAx>) -> MainNavAx? = { nodes ->
            unique(nodes.filter { node ->
                node.role == AccessibleRole.RADIO_BUTTON &&
                    (node.name == label || node.name?.startsWith("$label, ") == true)
            })
        }
        pva027VisibleTarget(languageOption("English")) // Real route, target and scroll-ancestor geometry before VM lookup.
        // The real Appearance route already consumes this singleton. No early bootstrap/VM injection.
        val settings = onEdt { GlobalContext.get().get<SettingsViewModel>() }
        onEdt { assertEquals(SettingsViewModel.AppLanguage.SYSTEM, settings.state.value.language) }

        data class Owner(
            val icon: TrayIcon,
            val popup: PopupMenu,
            val items: List<MenuItem>,
            val iconListeners: List<ActionListener>,
            val itemListeners: List<List<ActionListener>>,
        )
        var observed: Owner? = null
        await("PVA027_ACTUAL_MAIN_TRAY_INSTALLED") { onEdt {
            assertTrue(SystemTray.isSupported(), "PVA027_NATIVE_TRAY_UNSUPPORTED")
            val icons = SystemTray.getSystemTray().trayIcons
            assertTrue(icons.size <= 1, "PVA027_EXTRA_PROCESS_TRAY_ICON")
            val icon = icons.singleOrNull() ?: return@onEdt false
            val popup = assertNotNull(icon.popupMenu)
            assertEquals(5, popup.itemCount, "PVA027_POPUP_STRUCTURE")
            val items = listOf(popup.getItem(0), popup.getItem(2), popup.getItem(4))
            val iconListeners = icon.actionListeners.toList()
            val itemListeners = items.map { it.actionListeners.toList() }
            assertTrue(iconListeners.isNotEmpty() && itemListeners.all { it.isNotEmpty() }, "PVA027_NO_LISTENERS")
            observed = Owner(icon, popup, items, iconListeners, itemListeners)
            true
        } }
        val owner = assertNotNull(observed)
        fun sameListeners(expected: List<ActionListener>, actual: List<ActionListener>): Boolean =
            expected.size == actual.size && expected.zip(actual).all { (before, after) -> before === after }

        fun trayPropertiesMatch(expected: List<String>): Boolean = onEdt {
            assertTrue(frame === originalFrame && frame.isDisplayable, "PVA027_MAIN_WINDOW_REPLACED")
            assertNativeWindow(originalFrame)
            val icons = SystemTray.getSystemTray().trayIcons
            assertTrue(icons.size == 1 && icons.single() === owner.icon, "PVA027_INSTALLED_ICON_REPLACED")
            assertTrue(owner.icon.popupMenu === owner.popup, "PVA027_INSTALLED_POPUP_REPLACED")
            assertEquals(5, owner.popup.itemCount, "PVA027_POPUP_STRUCTURE_CHANGED")
            val items = listOf(owner.popup.getItem(0), owner.popup.getItem(2), owner.popup.getItem(4))
            items.forEachIndexed { index, item ->
                assertTrue(item === owner.items[index] && item.isEnabled, "PVA027_MENU_ITEM_CHANGED_OR_DISABLED")
                assertTrue(sameListeners(owner.itemListeners[index], item.actionListeners.toList()),
                    "PVA027_MENU_LISTENER_REPLACED")
            }
            assertTrue(sameListeners(owner.iconListeners, owner.icon.actionListeners.toList()),
                "PVA027_ICON_LISTENER_REPLACED")
            listOf(owner.icon.toolTip, items[0].label, items[1].label, items[2].label) == expected
        }

        val english = listOf("PassVault Password Manager", "Show PassVault", "Lock Vault", "Exit")
        val arabic = listOf("مدير كلمات المرور PassVault", "إظهار PassVault", "قفل الخزنة", "خروج")
        await("PVA027_INITIAL_EN_PROPERTIES") { trayPropertiesMatch(english) }
        val choices = listOf(
            Triple("English", SettingsViewModel.AppLanguage.ENGLISH, english),
            Triple("Arabic", SettingsViewModel.AppLanguage.ARABIC, arabic),
            Triple("الإنجليزية", SettingsViewModel.AppLanguage.ENGLISH, english),
        )
        choices.forEachIndexed { stage, (label, choice, expected) ->
            pva027Click(languageOption(label)) // Native input only; never invoke onEvent/setup or publish an environment.
            await("PVA027_SETTINGS_TO_INSTALLED_TRAY_$stage") {
                val choiceObserved = onEdt {
                    settings.state.value.language == choice && settings.state.value.errorMessage == null
                }
                choiceObserved && trayPropertiesMatch(expected)
            }
            // Separate fixed case contract; never add these receipts to the original credential EVENTS.
            trace.record(listOf("PVA027_MAIN_TRAY_EN1", "PVA027_MAIN_TRAY_AR", "PVA027_MAIN_TRAY_EN2")[stage])
        }
        // No tray/image/listener/window mutation to restore. Original Main terminal cleanup remains its owner.
        // Matching state/labels does not establish preference-save, visual tooltip or native callback settlement.
    }

    private data class Pva027Target(
        val node: MainNavAx,
        val scrollAncestor: MainNavAx,
        val viewport: Rectangle,
    )

    // PVA027-only locator: preserve the original credential/PVU003 primitives byte-for-byte.
    private fun pva027Locate(
        selector: (List<MainNavAx>) -> MainNavAx?, nodes: List<MainNavAx>,
    ): Pva027Target? {
        check(SwingUtilities.isEventDispatchThread())
        assertNativeWindow(frame)
        val target = selector(nodes) ?: return null
        assertTrue(target.window === frame && target.enabled && target.actionCount > 0, "PVA027_TARGET_NOT_ACTIONABLE")
        val rectangle = target.screen ?: return null
        var ancestor = target.parent
        var childIndex = target.index
        var depth = 0
        val scrolls = mutableListOf<MainNavAx>()
        while (ancestor != null) {
            val index = requireNotNull(ancestor)
            assertTrue(++depth <= 48 && index in 0 until childIndex, "PVA027_ANCESTOR_BOUND")
            val node = nodes[index]
            assertTrue(node.window === frame, "PVA027_ANCESTOR_WINDOW_CHANGED")
            if (node.role == AccessibleRole.SCROLL_PANE) scrolls += node
            childIndex = index
            ancestor = node.parent
        }
        // Both production Settings surfaces use one non-lazy scaffoldVerticalScroll Column.
        // Missing/ambiguous toolkit scroll semantics fail; never guess any other scroll node.
        assertEquals(1, scrolls.size, "PVA027_EXACT_TARGET_SCROLL_ANCESTOR")
        val scroll = scrolls.single()
        val scrollArea = scroll.screen ?: return null
        assertTrue(scroll.enabled, "PVA027_SCROLL_ANCESTOR_DISABLED")
        val viewport = scrollArea.intersection(safeBounds(target, nodes)).apply { grow(-8, -8) }
        assertTrue(viewport.width in 100..1400 && viewport.height in 80..1000, "PVA027_VIEWPORT_GEOMETRY")
        assertTrue(rectangle.width in 1..viewport.width && rectangle.height in 1..viewport.height,
            "PVA027_TARGET_LARGER_THAN_SAFE_VIEWPORT")
        return Pva027Target(target, scroll, viewport)
    }

    private fun pva027Find(selector: (List<MainNavAx>) -> MainNavAx?): Pva027Target {
        var result: Pva027Target? = null
        await("PVA027_TARGET_WITH_SCROLL_ANCESTOR") {
            result = onEdt { pva027Locate(selector, snapshot()) }
            result != null
        }
        return requireNotNull(result)
    }

    private fun pva027SameGeometry(before: Pva027Target, after: Pva027Target): Boolean =
        before.node.context === after.node.context && before.node.screen == after.node.screen &&
            before.scrollAncestor.context === after.scrollAncestor.context && before.viewport == after.viewport

    private fun pva027VisibleTarget(selector: (List<MainNavAx>) -> MainNavAx?): Pva027Target {
        repeat(35) {
            val target = pva027Find(selector)
            val rectangle = assertNotNull(target.node.screen)
            if (target.viewport.contains(rectangle)) {
                var stable = target
                var since = System.nanoTime()
                await("PVA027_STABLE_TARGET_AND_VIEWPORT") {
                    val next = onEdt { pva027Locate(selector, snapshot()) }
                    if (next == null) {
                        since = System.nanoTime()
                        false
                    } else {
                        if (!pva027SameGeometry(stable, next)) since = System.nanoTime()
                        stable = next
                        next.viewport.contains(assertNotNull(next.node.screen)) &&
                            System.nanoTime() - since >= TimeUnit.MILLISECONDS.toNanos(100)
                    }
                }
                return stable
            }
            assertNativeWindow(frame)
            val viewport = target.viewport
            robot.mouseMove(viewport.x + viewport.width / 2, viewport.y + viewport.height / 2)
            val fresh = pva027Find(selector)
            assertTrue(pva027SameGeometry(target, fresh), "PVA027_MOVED_SCROLL_TARGET_OR_VIEWPORT")
            assertNativeWindow(frame)
            robot.mouseWheel(if (rectangle.y < viewport.y) -3 else 3)
            Thread.sleep(80) // Native scroll spacing only, not Compose/frame/persistence acknowledgement.
        }
        fail("PVA027_NATIVE_SCROLL_BOUND")
    }

    private fun pva027Click(selector: (List<MainNavAx>) -> MainNavAx?) {
        val target = pva027VisibleTarget(selector)
        val rectangle = assertNotNull(target.node.screen)
        assertNativeWindow(frame)
        robot.mouseMove(rectangle.x + rectangle.width / 2, rectangle.y + rectangle.height / 2)
        val fresh = pva027Find(selector)
        assertTrue(pva027SameGeometry(target, fresh) && fresh.viewport.contains(rectangle),
            "PVA027_MOVED_CLICK_TARGET_OR_VIEWPORT")
        assertNativeWindow(frame)
        assertFalse(mousePressed)
        assertTrue(pressedKeys.isEmpty())
        val failures = MainNavFailures()
        mousePressed = true
        try { robot.mousePress(InputEvent.BUTTON1_DOWN_MASK) } catch (error: Throwable) { failures.add(error) }
        finally { failures.release { robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK); mousePressed = false } }
        failures.throwIfPresent() // No alternative coordinates, direct AX action, or radio-click retry.
    }

    private fun scenario() {
        replaceText("Master password", PASSWORD, readable = false)
        click(button("Unlock vault"))
        await("MAIN_NAV_NATIVE_UNLOCK", 30) { onEdt { credentialCard(snapshot()) != null } }
        click(::credentialCard)
        click(button("Edit credential"))
        await("MAIN_NAV_EDITOR") { readText("Title") == TITLE }
        trace.record("MAIN_NATIVE_EDIT")
        click { fieldEdit(it, "originalname") }
        replaceText("Field Name", "draftname")
        replaceText("Value", "draftvalue")
        assertDraft()
        trace.record("PENDING_NATIVE_TUPLE")
        click(button("Back")); stay(); trace.record("TOOLBAR_STAY")
        assertNativeWindow(frame)
        withKey(KeyEvent.VK_ESCAPE) { }
        stay(); trace.record("ESCAPE_STAY")
        click(tab("Settings")); stay(); trace.record("TAB_STAY")
        click(named("Add credential"))
        await("MAIN_NAV_DIRTY_ADD_DIALOG") { onEdt { hasDialog(snapshot()) } }
        click(button("Discard"))
        await("MAIN_NAV_ADD_DISCARD_POP_NOT_FORWARD_REPLAY") { onEdt {
            val nodes = snapshot()
            !hasDialog(nodes) && button("Edit credential")(nodes) != null && textField("Title")(nodes) == null &&
                nodes.none { it.name == "New credential" || it.name == "draftname" }
        } }
        assertHome()
        trace.record("ADD_DISCARD_DETAIL")
        click(tab("Settings")) // Positive control: an implementation blocking all tabs must fail.
        await("MAIN_NAV_CLEAN_SETTINGS") { onEdt { tab("Settings")(snapshot())?.selected == true } }
        click(tab("Home"))
        await("MAIN_NAV_CLEAN_HOME_DETAIL") { onEdt {
            val nodes = snapshot()
            tab("Home")(nodes)?.selected == true && button("Edit credential")(nodes) != null
        } }
        trace.record("CLEAN_TAB_DETAIL")
    }

    private fun stay() {
        await("MAIN_NAV_DISCARD_DIALOG") { onEdt { hasDialog(snapshot()) } }
        assertHome()
        click(button("Keep editing"))
        await("MAIN_NAV_KEEP_EDITING") { onEdt { !hasDialog(snapshot()) } }
        assertDraft(); assertHome()
    }
    private fun assertDraft() {
        assertEquals("draftname", readText("Field Name"))
        assertEquals("draftvalue", readText("Value"))
        assertEquals(TITLE, readText("Title"))
        assertFalse(find { unique(it.filter { node ->
            node.role == AccessibleRole.CHECK_BOX && node.name == "Secret field"
        }) }.checked)
    }
    private fun assertHome() = onEdt {
        val nodes = snapshot()
        assertTrue(assertNotNull(tab("Home")(nodes)).selected)
        assertFalse(assertNotNull(tab("Settings")(nodes)).selected)
    }
    private fun readText(label: String): String? = find(textField(label)).text
    private fun replaceText(label: String, value: String, readable: Boolean = true) {
        require(value.all { it in 'a'..'z' })
        val selector = textField(label)
        click(selector)
        await("MAIN_NAV_FIELD_FOCUS") { find(selector).focused }
        val owner = find(selector).window
        assertNativeWindow(owner)
        withKey(KeyEvent.VK_CONTROL) { withKey(KeyEvent.VK_A) { } }
        value.forEach { char ->
            assertNativeWindow(owner)
            withKey(KeyEvent.getExtendedKeyCodeForChar(char.code)) { }
        }
        if (readable) await("MAIN_NAV_NATIVE_TEXT") { find(selector).text == value }
    }
    private fun withKey(code: Int, block: () -> Unit) {
        pressedKeys += code
        val failures = MainNavFailures()
        try { robot.keyPress(code); block() } catch (error: Throwable) { failures.add(error) }
        finally { failures.release { robot.keyRelease(code); pressedKeys -= code } }
        failures.throwIfPresent()
    }
    private fun click(selector: (List<MainNavAx>) -> MainNavAx?) {
        val target = scrollTo(selector)
        val rectangle = assertNotNull(target.screen)
        assertTrue(target.enabled)
        assertNativeWindow(target.window)
        robot.mouseMove(rectangle.x + rectangle.width / 2, rectangle.y + rectangle.height / 2)
        val fresh = find(selector)
        assertTrue(target.context === fresh.context && rectangle == fresh.screen, "MAIN_NAV_MOVED_TARGET")
        val failures = MainNavFailures()
        mousePressed = true
        try { robot.mousePress(InputEvent.BUTTON1_DOWN_MASK) } catch (error: Throwable) { failures.add(error) }
        finally { failures.release { robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK); mousePressed = false } }
        failures.throwIfPresent()
    }
    private fun scrollTo(selector: (List<MainNavAx>) -> MainNavAx?): MainNavAx {
        repeat(35) {
            val target = find(selector)
            val rectangle = assertNotNull(target.screen)
            val safe = onEdt { safeBounds(target, snapshot()) }
            if (safe.contains(rectangle) && rectangle.width > 0 && rectangle.height > 0) {
                var previous = rectangle
                var since = System.nanoTime()
                await("MAIN_NAV_STABLE_VISIBLE_TARGET") {
                    val next = find(selector)
                    if (next.screen != previous) { previous = assertNotNull(next.screen); since = System.nanoTime() }
                    onEdt { safeBounds(next, snapshot()).contains(previous) } &&
                        System.nanoTime() - since >= TimeUnit.MILLISECONDS.toNanos(100)
                }
                return find(selector)
            }
            assertTrue(rectangle.width in 1..safe.width, "MAIN_NAV_HORIZONTAL_CLIPPING")
            assertNativeWindow(target.window)
            robot.mouseMove(safe.x + safe.width / 2, safe.y + safe.height / 2)
            robot.mouseWheel(if (rectangle.y < safe.y) -3 else 3)
            Thread.sleep(80) // Poll accessibility afterward; not a fabricated frame-progress acknowledgement.
        }
        fail("MAIN_NAV_SCROLL_BOUND")
    }
    private fun safeBounds(target: MainNavAx, nodes: List<MainNavAx>): Rectangle {
        val client = clientBounds(target.window)
        if (target.role == AccessibleRole.PAGE_TAB || target.name == "Add credential") return client
        val dockTop = nodes.filter { it.window === target.window && it.role == AccessibleRole.PAGE_TAB }
            .mapNotNull { it.screen?.y }.minOrNull()
        // Conservatively exclude the whole dock strip, not merely a target's client-contained rectangle.
        return if (dockTop == null) client else Rectangle(
            client.x, client.y, client.width, (dockTop - client.y - 24).coerceAtLeast(1),
        )
    }
    private fun find(selector: (List<MainNavAx>) -> MainNavAx?): MainNavAx {
        var result: MainNavAx? = null
        await("MAIN_NAV_UNIQUE_TARGET") { result = onEdt { selector(snapshot()) }; result != null }
        return requireNotNull(result)
    }
    private fun snapshot(): List<MainNavAx> {
        val result = mutableListOf<MainNavAx>()
        val seen = IdentityHashMap<AccessibleContext, Boolean>()
        fun visit(context: AccessibleContext?, owner: AwtWindow, parent: Int?, depth: Int) {
            if (context == null || seen.put(context, true) != null) return
            assertTrue(depth <= 48 && result.size < 4096, "NAV_A11Y_TREE_BOUND")
            val component = context.accessibleComponent
            val rectangle = try {
                component?.locationOnScreen?.let { Rectangle(it, component.size) }
            } catch (_: IllegalComponentStateException) { null }
            // Only editable values are consumed; navigation selectors use metadata, not text payloads.
            val editableText = context.accessibleEditableText
            val text = editableText?.let { content ->
                val length = content.charCount
                assertTrue(length in 0..1024, "NAV_A11Y_TEXT_BOUND")
                buildString { repeat(length) { append(content.getAtIndex(AccessibleText.CHARACTER, it)) } }
            }
            val index = result.size
            val states = context.accessibleStateSet
            result += MainNavAx(
                index, parent, context, owner, context.accessibleRole, context.accessibleName, text,
                editableText != null, component?.isEnabled == true,
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
        fun visit(window: AwtWindow) {
            assertTrue(result.size < 8 && window !in result, "MAIN_NAV_WINDOW_BOUND")
            result += window
            window.ownedWindows.forEach(::visit)
        }
        visit(frame)
        return result
    }
    private fun clientBounds(owner: AwtWindow): Rectangle {
        val origin = owner.locationOnScreen
        val insets = owner.insets
        return Rectangle(origin.x + insets.left, origin.y + insets.top,
            owner.width - insets.left - insets.right, owner.height - insets.top - insets.bottom).also {
            assertTrue(it.width in 200..1400 && it.height in 150..1000, "MAIN_NAV_CLIENT_SIZE")
            assertTrue(owner.graphicsConfiguration.bounds.contains(it), "MAIN_NAV_OUTSIDE_ADMITTED_DISPLAY")
        }
    }
    private fun assertNativeWindow(owner: AwtWindow) = onEdt {
        assertTrue(owner in ownedWindows() && owner.isShowing && owner.isActive, "MAIN_NAV_WRONG_ACTIVE_WINDOW")
        clientBounds(owner)
        Unit
    }
    private fun await(message: String, seconds: Long = 5, condition: () -> Boolean) {
        val end = minOf(deadline, System.nanoTime() + TimeUnit.SECONDS.toNanos(seconds))
        while (System.nanoTime() < end) {
            if (Thread.currentThread().isInterrupted) throw InterruptedException("MAIN_NAV_DRIVER_INTERRUPTED")
            if (condition()) return
            Thread.sleep(20)
        }
        fail(message)
    }
    private fun releaseInput() {
        val failures = MainNavFailures()
        pressedKeys.toList().forEach { code -> failures.release { robot.keyRelease(code); pressedKeys -= code } }
        if (mousePressed) failures.release { robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK); mousePressed = false }
        failures.throwIfPresent()
    }
}

private suspend fun assertOriginal(repository: CredentialRepository) {
    assertEquals(listOf(CredentialId(ID)), repository.getAllSummaries().getOrThrow().map { it.id })
    val credential = assertNotNull(repository.getById(CredentialId(ID)).getOrThrow())
    try {
        assertEquals(TITLE, credential.title)
        assertEquals(CredentialType.SecureNote, credential.type)
        assertEquals(ORIGINAL_FIELDS, credential.customFields.map {
            MainNavTuple(it.id.value, it.name, it.value.toStringUnsafe(), it.isSecret)
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

private data class MainNavAx(
    val index: Int, val parent: Int?, val context: AccessibleContext, val window: AwtWindow,
    val role: AccessibleRole, val name: String?, val text: String?, val editable: Boolean, val enabled: Boolean,
    val focused: Boolean, val checked: Boolean, val selected: Boolean, val actionCount: Int, val screen: Rectangle?,
)

private fun unique(nodes: List<MainNavAx>): MainNavAx? {
    assertTrue(nodes.size <= 1, "NAV_AMBIGUOUS_TARGET")
    return nodes.singleOrNull()
}
private fun named(name: String): (List<MainNavAx>) -> MainNavAx? = { nodes -> unique(nodes.filter { it.name == name }) }
private fun button(name: String): (List<MainNavAx>) -> MainNavAx? = { nodes -> unique(nodes.filter {
    it.role == AccessibleRole.PUSH_BUTTON && it.name == name
}) }
private fun tab(name: String): (List<MainNavAx>) -> MainNavAx? = { nodes -> unique(nodes.filter {
    it.role == AccessibleRole.PAGE_TAB && it.name == name
}) }
private fun textField(label: String): (List<MainNavAx>) -> MainNavAx? = { nodes -> unique(nodes.filter {
    it.editable && (it.name == label || it.name?.startsWith("$label, ") == true)
}) }
private fun hasDialog(nodes: List<MainNavAx>): Boolean = nodes.any { it.name == "Discard changes?" }
private fun credentialCard(nodes: List<MainNavAx>): MainNavAx? = unique(nodes.filter {
    it.actionCount > 0 && (it.name == TITLE || it.name?.startsWith("$TITLE, ") == true)
})

private fun fieldEdit(nodes: List<MainNavAx>, fieldName: String): MainNavAx? {
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
private class MainNavDirectory private constructor(val path: Path, private val key: Any, private val user: String) {
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

    fun child(name: String): MainNavDirectory {
        require(Regex("[a-z-]+").matches(name))
        checkBound()
        return acquire(Files.createDirectory(path.resolve(name), PRIVATE_DIRECTORY).toString(), empty = true)
    }

    companion object {
        fun acquire(raw: String, empty: Boolean): MainNavDirectory {
            require(raw.length in 2..2048 && Regex("/[A-Za-z0-9._/-]+").matches(raw))
            val path = Path.of(raw)
            assertTrue(path.isAbsolute && path.normalize() == path && path.toString() == raw)
            val attributes = Files.readAttributes(path, PosixFileAttributes::class.java, NOFOLLOW_LINKS)
            val user = ProcessHandle.current().info().user().orElseThrow { IllegalStateException("NAV_PROCESS_USER") }
            return MainNavDirectory(path, requireNotNull(attributes.fileKey()), user).also {
                it.checkBound()
                if (empty) Files.newDirectoryStream(path).use { stream ->
                    assertFalse(stream.iterator().hasNext(), "NAV_ROOT_NOT_FRESH_EMPTY")
                }
            }
        }
    }
}

/** Fixture-local primary/suppressed/interruption handling; native calls still require external hard settlement. */
private class MainNavFailures {
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

private class MainNavTrace(
    role: String, private val path: Path, private val key: String, case: MainNavCase,
) {
    private val parent = MainNavDirectory.acquire(property("evidenceDir"), empty = false)
    private val allowed = mainNavExpectedEvents(role, case) + "DRIVER_FAILURE"
    private val allowPvu003Observation = case == MainNavCase.PVU003_CHOOSER && role == "main"
    init {
        assertEquals(parent.path.resolve("$role.events"), path)
        checkedFile(path, key, 0)
    }
    fun record(event: String) {
        require(event in allowed || (allowPvu003Observation && PVU003_OBSERVATION_RECEIPT.matches(event)))
        parent.checkBound()
        checkedFile(path, key, 8192)
        FileChannel.open(path, WRITE, APPEND, NOFOLLOW_LINKS).use { channel ->
            val bytes = ByteBuffer.wrap((event + "\n").toByteArray(Charsets.UTF_8))
            while (bytes.hasRemaining()) check(channel.write(bytes) > 0)
            channel.force(true)
        }
    }
}

private fun checkedFile(path: Path, key: String, limit: Long) {
    assertTrue(path.isAbsolute && path.normalize() == path && path.toRealPath() == path)
    val attributes = Files.readAttributes(path, PosixFileAttributes::class.java, NOFOLLOW_LINKS)
    assertTrue(attributes.isRegularFile && attributes.size() in 0..limit)
    assertEquals(key, attributes.fileKey().toString())
    assertEquals(ProcessHandle.current().info().user().orElseThrow(), attributes.owner().name)
    assertEquals(PosixFilePermissions.fromString("rw-------"), attributes.permissions())
}
private fun fileKey(path: Path): String = requireNotNull(Files.readAttributes(
    path, PosixFileAttributes::class.java, NOFOLLOW_LINKS,
).fileKey()).toString()
private fun databaseIdentity(home: MainNavDirectory): String {
    checkHome(home)
    MainNavDirectory.acquire(home.path.resolve(".passvault").toString(), empty = false).checkBound()
    assertFalse(Files.exists(home.path.resolve(".passvault/recovery"), NOFOLLOW_LINKS))
    val path = home.path.resolve(".passvault/vault.db")
    assertEquals(path, path.toRealPath())
    val attributes = Files.readAttributes(path, PosixFileAttributes::class.java, NOFOLLOW_LINKS)
    assertTrue(attributes.isRegularFile && attributes.size() in 1..4_194_304)
    assertEquals(ProcessHandle.current().info().user().orElseThrow(), attributes.owner().name)
    // The real app owns SQLite file permissions; the fixture requires the private 0700 ancestor, not a new mode policy.
    return requireNotNull(attributes.fileKey()).toString()
}
/** Validate only the outer owner's exact private-session contract; never select or connect to a bus. */
private fun privateSessionEnvironment(authority: Path): Map<String, String> {
    assertTrue(authority.isAbsolute && authority.normalize() == authority)
    assertTrue(Files.isRegularFile(authority, NOFOLLOW_LINKS))
    assertEquals("Xauthority", authority.fileName.toString())
    val session = MainNavDirectory.acquire(requireNotNull(authority.parent).toString(), empty = false)
    val sessionRuntime = MainNavDirectory.acquire(session.path.resolve("runtime").toString(), empty = false)
    val noSystemBus = sessionRuntime.path.resolve("no-system-bus")
    val expected = mapOf(
        "XDG_RUNTIME_DIR" to sessionRuntime.path.toString(),
        "DBUS_SESSION_BUS_ADDRESS" to "unix:path=${session.path.resolve("bus")}",
        "DBUS_SYSTEM_BUS_ADDRESS" to "unix:path=$noSystemBus",
        "NO_AT_BRIDGE" to "1", // Native GTK bridge suppression, not Java AccessibleContext suppression.
    )
    val inherited = expected.mapValues { (name, value) ->
        requireNotNull(System.getenv(name)) { "MAIN_NAV_MISSING_PRIVATE_ENV_$name" }.also {
            assertTrue(it == value, "MAIN_NAV_INVALID_PRIVATE_ENV_$name")
        }
    }
    assertTrue(Files.notExists(noSystemBus, NOFOLLOW_LINKS), "MAIN_NAV_SYSTEM_BUS_GUARD_NOT_ABSENT")
    session.checkBound(); sessionRuntime.checkBound()
    return inherited // Original parent values, not inferred/default bus authority.
}
private fun checkPlatform(display: String) {
    assertEquals("Linux", System.getProperty("os.name"))
    assertEquals(17, Runtime.version().feature())
    assertTrue(Regex(":\\d{1,5}(?:\\.0)?").matches(display))
    assertEquals(display, System.getenv("DISPLAY"))
    assertTrue(System.getenv("WAYLAND_DISPLAY").isNullOrBlank())
    assertTrue(System.getProperty("compose.accessibility.enable") != "false")
    assertEquals(null, System.getenv("COMPOSE_DISABLE_ACCESSIBILITY"))
    privateSessionEnvironment(Path.of(requireNotNull(System.getenv("XAUTHORITY"))))
}
private fun property(name: String): String =
    requireNotNull(System.getProperty(PREFIX + name)) { "MAIN_NAV_PROPERTY_$name" }
private fun checkHome(home: MainNavDirectory) {
    home.checkBound()
    // The test worker does metadata-only checks; only child JVMs bind user.home to the synthetic root.
    if (System.getProperty(PREFIX + "ownedChild") == "true") {
        assertEquals(home.path.toString(), property("home"))
        assertEquals(home.path.toString(), System.getProperty("user.home"))
        assertEquals("en", Locale.getDefault().language)
    }
}
private fun <T> io(timeoutMillis: Long = 60_000, block: suspend () -> T): T =
    runBlocking { withTimeout(timeoutMillis) { block() } }
private fun <T> onEdt(block: () -> T): T {
    if (SwingUtilities.isEventDispatchThread()) return block()
    val task = FutureTask(Callable(block))
    SwingUtilities.invokeLater(task)
    return try { task.get(5, TimeUnit.SECONDS) } catch (error: Throwable) {
        task.cancel(false) // Cannot stop already begun EDT/native work; captured-child deadline is still required.
        throw error
    }
}

private data class MainNavTuple(val id: String, val name: String, val value: String, val secret: Boolean)
private const val PREFIX = "passvault.mainnav."
private const val ID = "synthetic-main-nav-credential"
private const val TITLE = "syntheticmainnaventry"
private const val PASSWORD = "syntheticmainnavpasswordonly"
private const val MAX_LOG_BYTES = 256L * 1024
private val PRIVATE_MODE = PosixFilePermissions.fromString("rwx------")
private val PRIVATE_DIRECTORY = PosixFilePermissions.asFileAttribute(PRIVATE_MODE)
private val PRIVATE_FILE = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------"))
private val ORIGINAL_FIELDS = listOf(
    MainNavTuple("synthetic-main-nav-one", "originalname", "originalvalue", false),
    MainNavTuple("synthetic-main-nav-two", "survivorname", "survivorvalue", true),
)
private enum class MainNavCase(val runtimeProperty: String, val evidenceProperty: String) {
    CREDENTIAL("runtimeDir", "evidenceDir"),
    PVU003_CHOOSER("pvu003RuntimeDir", "pvu003EvidenceDir"),
    PVA027_SETTINGS_TRAY("pva027RuntimeDir", "pva027EvidenceDir"),
}
private val PVU003_MAIN_TERMINAL_EVENTS = listOf("DRIVER_ASSERTIONS_COMPLETE", "QUIT_KEY_CALLS_COMPLETE")
private val PVU003_OBSERVATION_RECEIPT = Regex(
    "PVU003_CHOOSER_HOME=[01];AFTER_CANCEL=[01];FRAME_DELIVERY=[0-9a-f]",
)
private val PVA027_MAIN_EVENTS = listOf(
    "PVA027_MAIN_TRAY_EN1", "PVA027_MAIN_TRAY_AR", "PVA027_MAIN_TRAY_EN2",
    "DRIVER_ASSERTIONS_COMPLETE", "QUIT_KEY_CALLS_COMPLETE",
)
private fun mainNavExpectedEvents(role: String, case: MainNavCase): List<String> = when {
    role != "main" -> EVENTS.getValue(role)
    case == MainNavCase.PVU003_CHOOSER -> PVU003_MAIN_TERMINAL_EVENTS
    case == MainNavCase.PVA027_SETTINGS_TRAY -> PVA027_MAIN_EVENTS
    else -> EVENTS.getValue(role)
}

private fun assertMainNavEvents(role: String, case: MainNavCase, events: List<String>) {
    val terminal = if (case == MainNavCase.PVU003_CHOOSER && role == "main") {
        assertEquals(3, events.size, "PVU003_EXACT_OBSERVATION_AND_TERMINAL_COUNT")
        assertTrue(PVU003_OBSERVATION_RECEIPT.matches(events.first()), "PVU003_OBSERVATION_SHAPE")
        events.drop(1)
    } else events
    assertEquals(mainNavExpectedEvents(role, case), terminal, "MAIN_NAV_${role}_ORDERED_EVIDENCE")
}

private val EVENTS = mapOf(
    "seed" to listOf("SEED_ORIGINAL_TUPLES", "SEED_ROOM_CLOSED"),
    "main" to listOf(
        "MAIN_NATIVE_EDIT", "PENDING_NATIVE_TUPLE", "TOOLBAR_STAY", "ESCAPE_STAY", "TAB_STAY",
        "ADD_DISCARD_DETAIL", "CLEAN_TAB_DETAIL", "DRIVER_ASSERTIONS_COMPLETE", "QUIT_KEY_CALLS_COMPLETE",
    ),
    "verify" to listOf("VERIFY_ORIGINAL_TUPLES", "VERIFY_ROOM_CLOSED"),
)
private val FORBIDDEN_LOGS = listOf(
    "MAIN_NAV_FAILURE", "Exception in thread", "[ERROR]",
    "PassVault is already running for this user.",
    "PassVault requires a graphical environment.",
    "PassVault Desktop could not secure its private data directory.",
    "PassVault Desktop terminated after an application failure.",
    "PassVault security cleanup exceeded the Desktop shutdown deadline;",
    "PassVault Desktop shutdown completed with ",
)
