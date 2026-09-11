package com.passvault.desktop.security

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import java.awt.Component
import java.awt.Container
import java.awt.Frame
import java.awt.GraphicsEnvironment
import java.awt.Rectangle
import java.awt.Robot
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.LinkOption.NOFOLLOW_LINKS
import java.nio.file.Path
import java.nio.file.StandardOpenOption.CREATE_NEW
import java.nio.file.StandardOpenOption.WRITE
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import javax.imageio.ImageIO
import javax.imageio.stream.MemoryCacheImageOutputStream
import javax.swing.SwingUtilities
import javax.swing.UIManager
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.Assume.assumeTrue

/**
 * PVU-005's real-window rendering boundary, not repository/authentication verification.
 *
 * Root must separately admit a private synthetic X display, window manager, JDK/runtime,
 * worker/evidence directories and cleanup. No app entry point, Koin, vault, clipboard,
 * biometric provider, external command or renderer override is used here. The missing
 * opt-in is an explicit JUnit skip; opted-in setup failures are never passing UI evidence.
 */
@OptIn(ExperimentalComposeUiApi::class)
class DesktopCurtainRenderingTest {
    @Test
    fun `secured locked Compose window shows usable synthetic unlock content after restore`() {
        val admittedDisplay = System.getProperty("passvault.pvu005.syntheticDisplay")
        assumeTrue("PVU-005 requires a separately admitted synthetic display", admittedDisplay != null)
        assertTrue(Regex(":\\d{1,5}(?:\\.0)?").matches(requireNotNull(admittedDisplay)))
        assertEquals(admittedDisplay, System.getenv("DISPLAY"), "PVU005_SETUP_DISPLAY_MISMATCH")
        assertTrue(System.getenv("WAYLAND_DISPLAY").isNullOrBlank(), "PVU005_SETUP_NOT_X11_ONLY")
        assertFalse(GraphicsEnvironment.isHeadless(), "PVU005_SETUP_HEADLESS")
        // Main configures these before its window. Root supplies them at worker startup instead;
        // setting them after AWT/Java2D initialization would not establish the same configuration.
        assertEquals("true", System.getProperty("sun.java2d.xrender"), "PVU005_SETUP_XRENDER_PROPERTY")
        assertEquals("on", System.getProperty("awt.useSystemAAFontSettings"), "PVU005_SETUP_AA_PROPERTY")
        assertEquals("true", System.getProperty("swing.aatext"), "PVU005_SETUP_SWING_AA_PROPERTY")
        val evidence = Path.of(requireNotNull(System.getProperty("passvault.pvu005.evidenceDir")))
        assertTrue(evidence.isAbsolute && Files.isDirectory(evidence, NOFOLLOW_LINKS))
        // Root allocates this fresh directory; never create/adopt a directory or overwrite evidence.
        Files.newDirectoryStream(evidence).use { assertFalse(it.iterator().hasNext()) }

        RenderingScenario(requireNotNull(admittedDisplay), evidence).run()
    }

    /** One admitted invocation owns its window, callbacks, observations and terminal cleanup. */
    private inner class RenderingScenario(private val admittedDisplay: String, private val evidence: Path) {
        private val protection = DesktopWindowProtection()
        private val window = AtomicReference<ComposeWindow?>()
        private val safeContent = mutableStateOf(true)
        private val safeFrame = AtomicInteger(0)
        private val clicks = AtomicInteger(0)
        private val callbackLocks = AtomicReference<List<Boolean>>(emptyList())
        private val iconifiedEvents = AtomicInteger(0)
        private val restoredEvents = AtomicInteger(0)
        private val securityRequests = AtomicInteger(0)
        private val button = AtomicReference<Rectangle?>()
        private val trace = mutableListOf<String>()
        private var failure: Throwable? = null

        // Retain assertions and interruption until independent cleanup and evidence writes have run.
        @Suppress("TooGenericExceptionCaught")
        fun run() {
            try {
                showWindow()
                val surface = measureWindow()
                showBaseline(surface)
                val preAck = deferRestore(surface)
                val postAck = acknowledgeRestore(surface)
                checkRemovalControl(surface)
                assertLockedRestore(preAck, postAck)
            } catch (error: Throwable) {
                failure = error
            } finally {
                close()
            }
            failure?.let { throw it }
        }

        private fun showWindow() = onEdt {
            configureLookAndFeel()
            val current = ComposeWindow().also(window::set)
            current.title = "PVU-005 synthetic rendering only"
            current.setSize(640, 480)
            current.setLocation(64, 64)
            current.isResizable = false
            current.addWindowStateListener { event ->
                if (event.newState and Frame.ICONIFIED != 0) iconifiedEvents.incrementAndGet()
                if (event.oldState and Frame.ICONIFIED != 0 && event.newState and Frame.ICONIFIED == 0) {
                    restoredEvents.incrementAndGet()
                }
            }
            protection.attachWindow(current)
            protection.setLockListener { securityRequests.incrementAndGet() }
            current.setContent { renderContent() }
            current.isVisible = true
            current.toFront()
            current.requestFocus()
        }

        private fun configureLookAndFeel() {
            val gtkResult = setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel")
            val laf = UIManager.getSystemLookAndFeelClassName()
            val lafResult = setLookAndFeel(laf)
            trace += "systemLookAndFeel=$laf selected=${UIManager.getLookAndFeel().javaClass.name}" +
                " gtkSetup=$gtkResult systemSetup=$lafResult"
        }

        // Optional LAF attempts record any Exception; the selected LAF and real rendering remain observed.
        @Suppress("TooGenericExceptionCaught")
        private fun setLookAndFeel(name: String): String = try {
            UIManager.setLookAndFeel(name)
            "OK"
        } catch (error: Exception) {
            error.javaClass.simpleName
        }

        @Composable
        private fun renderContent() {
            val safe = safeContent.value
            val focus = androidx.compose.runtime.remember { FocusRequester() }
            LaunchedEffect(safe) {
                if (safe) {
                    // Same kind of real frame-clock signal as the caller, NOT screen readback
                    // or the production Locked/route/cleanup/coordinator-epoch handshake.
                    withFrameNanos { }
                    focus.requestFocus()
                    safeFrame.incrementAndGet()
                }
            }
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val palette = if (safe) SAFE else UNLOCKED
                    drawRect(color(palette[0]))
                    drawRect(color(palette[1]), size = Size(size.width / 4, size.height / 4))
                    drawRect(
                        color(palette[2]),
                        topLeft = Offset(size.width * 3 / 4, size.height * 3 / 4),
                        size = Size(size.width / 4, size.height / 4),
                    )
                }
                if (safe) {
                    // Foundation is already exposed to app-desktop. Do not add Material3
                    // or mistake this synthetic Button-role control for the real Unlock UI.
                    Box(
                        modifier = Modifier.size(240.dp, 80.dp).background(Color.White)
                            .focusRequester(focus).onGloballyPositioned { coordinates ->
                                val bounds = coordinates.boundsInRoot()
                                button.set(Rectangle(
                                    bounds.left.roundToInt(), bounds.top.roundToInt(),
                                    bounds.width.roundToInt(), bounds.height.roundToInt(),
                                ))
                            }.clickable(role = Role.Button) {
                                callbackLocks.updateAndGet { it + protection.isLocked }
                                clicks.incrementAndGet()
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText("Synthetic Unlock")
                    }
                }
            }
        }

        private fun measureWindow(): RenderSurface {
            val current = requireNotNull(window.get())
            val robot = Robot().apply { autoDelay = 20 }
            assertTrue(await { safeFrame.get() > 0 && onEdt { hasHeavyCanvas(current) } },
                "PVU005_SETUP_WINDOW_FRAME")
            val bounds = onEdt {
                assertTrue(hasHeavyCanvas(current), "PVU005_SETUP_NO_HEAVYWEIGHT_CANVAS")
                val api = current.renderApi.toString()
                val handle = current.windowHandle
                assertTrue(api.isNotBlank() && api != "UNKNOWN" && handle != 0L)
                trace += "display=$admittedDisplay renderApi=$api composeWindowHandle=${handle.toString(16)}"
                trace += "jdk=${System.getProperty("java.runtime.version")} os=${System.getProperty("os.name")}" +
                    " arch=${System.getProperty("os.arch")}; artifact hashes are root-admission evidence"
                Rectangle(current.contentPane.locationOnScreen, current.contentPane.size).also {
                    assertTrue(it.width in 320..800 && it.height in 240..600)
                    assertTrue(current.graphicsConfiguration.bounds.contains(it))
                    trace += "clientRectangle=$it"
                }
            }
            return RenderSurface(current, robot, bounds)
        }

        private fun showBaseline(surface: RenderSurface) {
            assertTrue(
                await { safeFrame.get() > 0 && hits(surface, SAFE) == SAMPLE_COUNT }, "PVU005_SETUP_BASELINE_PIXELS",
            )
            assertTrue(picture(surface, "01-safe-baseline").safe)
            val baselineInput = input(surface)
            trace += "baselineInput=$baselineInput"
            assertTrue(baselineInput.worked, "PVU005_SETUP_BASELINE_INPUT")
            assertEquals(listOf(false, false), baselineInput.callbackLocks)
            onEdt { safeContent.value = false }
            assertTrue(await { hits(surface, UNLOCKED) == SAMPLE_COUNT }, "PVU005_SETUP_UNLOCKED_MARKER")
            assertTrue(picture(surface, "02-unlocked-marker").unlocked)
        }

        private fun deferRestore(surface: RenderSurface): Pixels {
            onEdt { protection.lock() }
            assertTrue(
                await { iconifiedEvents.get() > 0 && hits(surface, UNLOCKED) == 0 }, "PVU005_SETUP_ICONIFICATION",
            )
            assertTrue(locked())
            val priorRestores = restoredEvents.get()
            val priorIconifications = iconifiedEvents.get()
            onEdt {
                protection.restoreWindow() // In-app request: intentionally withhold secured ACK.
                surface.window.extendedState = Frame.NORMAL // Real native WM request; never dispatch a fake event.
            }
            val restoreDeferred = await {
                restoredEvents.get() > priorRestores && iconifiedEvents.get() > priorIconifications &&
                    onEdt { protection.isMinimized } && hits(surface, UNLOCKED) == 0
            }
            val preAck = picture(surface, "03-pre-ack-restore")
            trace += "nativeRestoreDeferred=$restoreDeferred iconifiedEvents=${iconifiedEvents.get()}" +
                " restoredEvents=${restoredEvents.get()} securityRequests=${securityRequests.get()}"
            assertTrue(restoreDeferred, "PVU005_SETUP_NATIVE_RESTORE_NOT_SETTLED")
            assertTrue(locked())
            assertEquals(1, securityRequests.get())
            return preAck
        }

        private fun acknowledgeRestore(surface: RenderSurface): LockedRestoreObservation {
            val priorSafeFrame = safeFrame.get()
            onEdt { safeContent.value = true }
            // Never restore early to force a hidden window to render. A timeout is an ACK-progress
            // precondition failure, not evidence that the retained curtain obstructed Unlock.
            assertTrue(await { safeFrame.get() > priorSafeFrame }, "PVU005_SETUP_ICONIFIED_FRAME_PROGRESS")
            val restoresBeforeAck = restoredEvents.get()
            onEdt { protection.onVaultContentSecured() }
            val postAckRestored = await {
                restoredEvents.get() > restoresBeforeAck && !onEdt { protection.isMinimized }
            }
            val postAckGeometry = postAckRestored && onEdt {
                Rectangle(
                    surface.window.contentPane.locationOnScreen, surface.window.contentPane.size,
                ) == surface.bounds
            }
            val postAckVisible = postAckGeometry && await { hits(surface, SAFE) == SAMPLE_COUNT }
            val postAck = picture(surface, "04-post-ack-locked")
            val postAckLocked = locked()
            val postAckInput = if (postAckVisible && postAck.safe) {
                input(surface)
            } else {
                null
            }
            val afterInputLocked = locked()
            trace += "postAckRestored=$postAckRestored postAckGeometry=$postAckGeometry" +
                " postAckVisible=$postAckVisible postAckLocked=$postAckLocked" +
                " postAckInput=$postAckInput afterInputLocked=$afterInputLocked"
            return LockedRestoreObservation(
                postAckRestored, postAckGeometry, postAckVisible, postAck,
                postAckLocked && afterInputLocked, postAckInput,
            )
        }

        private fun checkRemovalControl(surface: RenderSurface) {
            // Capture the suspect verdict BEFORE this control; never let removal erase it.
            // This explicit synthetic already-Unlocked transition is not an authentication result.
            onEdt { protection.unlock() }
            val removalVisible = await {
                !onEdt { protection.isMinimized } && onEdt {
                    Rectangle(
                        surface.window.contentPane.locationOnScreen, surface.window.contentPane.size,
                    ) == surface.bounds
                } && hits(surface, SAFE) == SAMPLE_COUNT
            }
            val removal = picture(surface, "05-unlocked-removal-control")
            val removalInput = if (removalVisible && removal.safe) {
                input(surface)
            } else {
                null
            }
            trace += "removalVisible=$removalVisible removalInput=$removalInput locked=${locked()}"
            assertTrue(removalVisible && removal.safe && removalInput?.worked == true, "PVU005_CONTROL_REMOVAL_FAILED")
            assertEquals(listOf(false, false), removalInput?.callbackLocks)
            assertFalse(locked())
        }

        private fun assertLockedRestore(preAck: Pixels, postAck: LockedRestoreObservation) {
            assertEquals(0, preAck.unlockedHits, "PVU005_PRE_ACK_SETTLED_MARKER_VISIBLE")
            assertTrue(postAck.stayedLocked, "PVU005_LOGICAL_LOCK_LOST")
            assertTrue(postAck.restored && postAck.geometryMatches, "PVU005_SETUP_POST_ACK_NATIVE_RESTORE_OR_GEOMETRY")
            assertTrue(postAck.visible && postAck.pixels.safe, "PVU005_POST_ACK_UNLOCK_PIXELS_OBSTRUCTED")
            assertEquals(null, postAck.input?.blockedAt, "PVU005_SETUP_POST_ACK_INPUT_SCOPE_CHANGED")
            assertTrue(postAck.input?.worked == true, "PVU005_POST_ACK_UNLOCK_INPUT_OBSTRUCTED")
            assertEquals(listOf(true, true), postAck.input?.callbackLocks)
        }

        private fun picture(surface: RenderSurface, name: String): Pixels =
            capture(surface.robot, surface.bounds, evidence.resolve("$name.png")).also {
                trace += "$name safeHits=${it.safeHits}/27 unlockedHits=${it.unlockedHits}/27"
            }

        private fun hits(surface: RenderSurface, palette: IntArray): Int =
            surface.robot.createScreenCapture(surface.bounds).let {
                try { matchingSamples(it, palette) } finally { it.flush() }
            }

        private fun locked() = onEdt { protection.isLocked }

        private fun input(surface: RenderSurface): InputObservation =
            interact(surface.robot, surface.window, surface.bounds, button, clicks, callbackLocks)

        private fun retain(error: Throwable) {
            if (failure == null) failure = error else failure?.addSuppressed(error)
        }

        // Cleanup/disposal and trace writing are independent, including AssertionError and interruption.
        @Suppress("TooGenericExceptionCaught")
        private fun close() {
            try {
                val cleanupErrors = onEdt {
                    val errors = mutableListOf<Throwable>()
                    try { protection.cleanup() } catch (error: Throwable) { errors += error }
                    try { window.get()?.dispose() } catch (error: Throwable) { errors += error }
                    errors
                }
                cleanupErrors.forEach(::retain)
                trace += "cleanupErrors=${cleanupErrors.map { it.javaClass.simpleName }}"
            } catch (error: Throwable) {
                retain(error)
            }
            try {
                trace += "preEvidenceWriteOutcome=${failure?.javaClass?.simpleName ?: "PASS"}; " +
                    "actual XML/worker/cleanup outcome required; finite samples, not no-flash proof"
                Files.writeString(evidence.resolve("observations.txt"), trace.joinToString("\n", postfix = "\n"),
                    CREATE_NEW, WRITE)
            } catch (error: Throwable) {
                retain(error)
            }
        }
    }

    private data class RenderSurface(val window: ComposeWindow, val robot: Robot, val bounds: Rectangle)

    private data class LockedRestoreObservation(
        val restored: Boolean,
        val geometryMatches: Boolean,
        val visible: Boolean,
        val pixels: Pixels,
        val stayedLocked: Boolean,
        val input: InputObservation?,
    )

    private fun interact(
        robot: Robot,
        window: ComposeWindow,
        client: Rectangle,
        control: AtomicReference<Rectangle?>,
        clicks: AtomicInteger,
        callbackLocks: AtomicReference<List<Boolean>>,
    ): InputObservation {
        val callbackStart = callbackLocks.get().size
        fun result(pointer: Int, key: Int, blockedAt: String? = null) = InputObservation(
            pointer, key, callbackLocks.get().drop(callbackStart), blockedAt,
        )
        if (!await { onEdt { window.isActive } }) return result(0, 0, "before-pointer:NOT_ACTIVE")
        val target = control.get()?.let(::Rectangle)
        fun scopeFailure(): String? = inputScopeFailure(window, client, control, requireNotNull(target))
        var blockedAt = if (target == null) "CONTROL_MISSING" else scopeFailure()?.let { "before-move:$it" }
        var beforePointer = 0
        if (blockedAt == null) {
            val admittedTarget = requireNotNull(target)
            val pointX = client.x + admittedTarget.x + admittedTarget.width / 2
            val pointY = client.y + admittedTarget.y + admittedTarget.height / 2
            beforePointer = clicks.get()
            robot.mouseMove(pointX, pointY)
            blockedAt = scopeFailure()?.let { "before-press:$it" }
        }
        var pointerDelta = 0
        if (blockedAt == null) {
            try { robot.mousePress(InputEvent.BUTTON1_DOWN_MASK) } finally {
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
            }
            robot.waitForIdle()
            await { clicks.get() == beforePointer + 1 }
            pointerDelta = clicks.get() - beforePointer
            blockedAt = scopeFailure()?.let { "before-key:$it" }
        }
        var keyDelta = 0
        if (blockedAt == null) {
            val beforeKey = clicks.get()
            try { robot.keyPress(KeyEvent.VK_SPACE) } finally { robot.keyRelease(KeyEvent.VK_SPACE) }
            robot.waitForIdle()
            await { clicks.get() == beforeKey + 1 }
            keyDelta = clicks.get() - beforeKey
            blockedAt = scopeFailure()?.let { "after-key:$it" }
        }
        return result(pointerDelta, keyDelta, blockedAt)
    }

    private fun inputScopeFailure(
        window: ComposeWindow, client: Rectangle, control: AtomicReference<Rectangle?>, target: Rectangle,
    ): String? = onEdt {
        when {
            !window.isDisplayable || !window.isShowing -> "NOT_DISPLAYABLE_OR_SHOWING"
            window.extendedState and Frame.ICONIFIED != 0 || !window.isActive -> "NOT_ACTIVE_OR_ICONIFIED"
            Rectangle(window.contentPane.locationOnScreen, window.contentPane.size) != client -> "CLIENT_CHANGED"
            control.get() != target -> "CONTROL_CHANGED"
            target.width <= 0 || target.height <= 0 ||
                !Rectangle(0, 0, client.width, client.height).contains(target) -> "CONTROL_OUTSIDE_CLIENT"
            else -> null
        }
    }

    private fun capture(robot: Robot, bounds: Rectangle, destination: Path): Pixels {
        val image = robot.createScreenCapture(bounds)
        return try {
            Files.newOutputStream(destination, CREATE_NEW, WRITE).use { output ->
                MemoryCacheImageOutputStream(output).use { check(ImageIO.write(image, "png", it)) }
            }
            Pixels(matchingSamples(image, SAFE), matchingSamples(image, UNLOCKED))
        } finally {
            image.flush()
        }
    }

    private fun matchingSamples(image: BufferedImage, palette: IntArray): Int {
        val samples = listOf(
            Triple(image.width / 8, image.height / 2, palette[0]),
            Triple(image.width / 8, image.height / 8, palette[1]),
            Triple(image.width * 7 / 8, image.height * 7 / 8, palette[2]),
        )
        var hits = 0
        for ((x, y, expected) in samples) {
            for (dx in -1..1) for (dy in -1..1) {
                val actual = image.getRGB(x + dx, y + dy)
                if (listOf(0, 8, 16).all { shift ->
                    abs(((actual shr shift) and 255) - ((expected shr shift) and 255)) <= 2
                }) hits++
            }
        }
        return hits
    }

    private fun await(condition: () -> Boolean): Boolean {
        val deadline = System.nanoTime() + 5_000_000_000L
        var consecutive = 0
        do {
            consecutive = if (condition()) consecutive + 1 else 0
            if (consecutive == 2) return true
            Thread.sleep(50)
        } while (System.nanoTime() < deadline)
        return false
    }

    private fun hasHeavyCanvas(component: Component): Boolean =
        (component is java.awt.Canvas && component.isDisplayable && !component.isLightweight) ||
            (component is Container && component.components.any(::hasHeavyCanvas))

    private fun <T> onEdt(action: () -> T): T {
        check(!SwingUtilities.isEventDispatchThread())
        val result = AtomicReference<Result<T>>()
        // Root's outer deadline remains necessary if native code or the EDT stops responding.
        SwingUtilities.invokeAndWait { result.set(runCatching(action)) }
        return result.get().getOrThrow()
    }

    private data class Pixels(val safeHits: Int, val unlockedHits: Int) {
        val safe: Boolean get() = safeHits == SAMPLE_COUNT
        val unlocked: Boolean get() = unlockedHits == SAMPLE_COUNT
    }

    private data class InputObservation(
        val pointerDelta: Int,
        val keyDelta: Int,
        val callbackLocks: List<Boolean>,
        val blockedAt: String?,
    ) {
        val worked: Boolean get() = blockedAt == null && pointerDelta == 1 && keyDelta == 1
    }

    private companion object {
        const val SAMPLE_COUNT = 27
        val SAFE = intArrayOf(0x124060, 0xD030F0, 0x20E0B0)
        val UNLOCKED = intArrayOf(0x682088, 0xFFF020, 0xE04020)

        fun color(rgb: Int): Color = Color(0xFF000000L or rgb.toLong())
    }
}
