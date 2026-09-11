package com.passvault.desktop.tray

import java.awt.Frame
import java.awt.GraphicsEnvironment
import java.awt.Image
import java.awt.MenuItem
import java.awt.PopupMenu
import java.awt.Rectangle
import java.awt.Robot
import java.awt.SystemTray
import java.awt.TrayIcon
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.LinkOption.NOFOLLOW_LINKS
import java.nio.file.Path
import java.nio.file.StandardOpenOption.CREATE_NEW
import java.nio.file.StandardOpenOption.READ
import java.nio.file.StandardOpenOption.WRITE
import java.nio.file.attribute.PosixFilePermissions
import java.util.Locale
import java.util.concurrent.Callable
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import javax.imageio.ImageIO
import javax.imageio.stream.MemoryCacheImageOutputStream
import javax.swing.SwingUtilities
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.jetbrains.compose.resources.getSystemResourceEnvironment
import org.junit.Assume.assumeTrue

/**
 * PVA-027: one real installed AWT tray, three resource-language stages, native popup/input.
 * Root must separately admit a dedicated JVM, private X display, single systray manager,
 * session/bus/auth directories and hard process/cleanup bounds. This test launches nothing.
 * Missing opt-in skips; opted-in missing boundaries fail before Toolkit/resource access.
 *
 * Public TrayIcon.image supplies a synthetic locator, NOT product-icon evidence. The real
 * production labels, menu peers and listeners remain installed. Screenshots require an
 * independent text/shaping/highlight review; pixel geometry is only a readiness check.
 * This is not the Settings/publisher/window-effect chain or visual tooltip verification.
 */
class DesktopSystemTrayNativeIntegrationTest {
    private var tray: DesktopSystemTray? = null
    private var frame: Frame? = null
    private var native: NativeOwner? = null
    private var robot: Robot? = null
    private var screen = Rectangle()
    private var frameMask = Rectangle()
    private var deadline = 0L
    private var completedStages = 0
    private val rightPresses = AtomicInteger()
    private val actions = CopyOnWriteArrayList<String>()
    private val heldInput = linkedMapOf<String, () -> Unit>()
    private val observations = mutableListOf<String>()

    @Test
    // Assertion failures and cancellation remain primary while every owned cleanup is attempted.
    @Suppress("TooGenericExceptionCaught")
    fun `same real tray refreshes English Arabic English and accepts native popup input`() {
        val evidence = admittedEvidence()
        val original = LocaleDefaults(
            Locale.getDefault(), Locale.getDefault(Locale.Category.DISPLAY), Locale.getDefault(Locale.Category.FORMAT),
        )
        val originalImageCache = ImageIO.getUseCache()
        deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(60)
        var failure: Throwable? = null
        try {
            ImageIO.setUseCache(false) // Also keep production icon decoding in memory in this dedicated worker.
            // ResourceEnvironment has an internal constructor; capture via its public factory instead.
            // Only this dedicated worker owns the temporary JVM defaults; restore all categories below.
            Locale.setDefault(Locale.ENGLISH)
            val english = getSystemResourceEnvironment()
            Locale.setDefault(Locale.forLanguageTag("ar"))
            val arabic = getSystemResourceEnvironment()
            Locale.setDefault(Locale.ENGLISH)
            // In particular, resolving captured Arabic below must not read this later English default.
            initializeNative()
            listOf(english, arabic, english).forEachIndexed { stage, environment ->
                val strings = runBlocking { withTimeout(5_000) { desktopTrayStrings(environment) } }
                assertEquals(if (stage == 1) ARABIC else ENGLISH, strings, "PVA027_RESOURCE_SNAPSHOT_$stage")
                installOrRefresh(stage, strings)
                exercisePopup(stage, evidence)
            }
            assertEquals(3, actions.size, "PVA027_NATIVE_CALLBACK_TOTAL")
            assertEquals(3, rightPresses.get(), "PVA027_NATIVE_RIGHT_PRESS_TOTAL")
        } catch (error: Throwable) {
            failure = error
            throw error
        } finally {
            close(original, originalImageCache, evidence, failure)
        }
    }

    private fun initializeNative() {
        assertFalse(GraphicsEnvironment.isHeadless(), "PVA027_SETUP_HEADLESS")
        val device = GraphicsEnvironment.getLocalGraphicsEnvironment().screenDevices.single()
        val configuration = device.defaultConfiguration
        screen = Rectangle(configuration.bounds)
        assertTrue(screen.x == 0 && screen.y == 0, "PVA027_SETUP_SCREEN_ORIGIN")
        assertTrue(screen.width in 640..1280 && screen.height in 480..1024, "PVA027_SETUP_SCREEN_BOUND")
        assertTrue(configuration.defaultTransform.isIdentity, "PVA027_SETUP_UNREVIEWED_DISPLAY_SCALING")
        robot = Robot(device).apply { autoDelay = 25 }
        onEdt {
            assertTrue(SystemTray.isSupported(), "PVA027_SETUP_TRAY_UNSUPPORTED")
            // A dedicated, independently admitted session is required before this global public query.
            assertTrue(SystemTray.getSystemTray().trayIcons.isEmpty(), "PVA027_SETUP_PREEXISTING_TRAY_ICON")
            tray = DesktopSystemTray() // Public constructor: no injected platform/event thread.
            val window = Frame("PVA-027 synthetic tray only").also { frame = it }
            window.isUndecorated = true
            window.isResizable = false
            window.background = java.awt.Color(0x303030)
            window.setBounds(screen.width / 2 - 120, screen.height / 2 - 70, 240, 140)
            window.isVisible = true
            assertTrue(window.isShowing, "PVA027_SETUP_FRAME_NOT_SHOWING")
            frameMask = Rectangle(window.bounds).apply { grow(12, 12) }
        }
    }

    private fun installOrRefresh(stage: Int, strings: DesktopTrayStrings) = onEdt {
        assertTrue(System.nanoTime() < deadline, "PVA027_SOFT_DEADLINE")
        requireNotNull(tray).setup(
            strings, { actions += "$stage:show" }, { actions += "$stage:lock" }, { actions += "$stage:exit" },
        )
        val icon = SystemTray.getSystemTray().trayIcons.single()
        val popup = requireNotNull(icon.popupMenu)
        assertEquals(5, popup.itemCount, "PVA027_NATIVE_POPUP_STRUCTURE")
        val items = listOf(popup.getItem(0), popup.getItem(2), popup.getItem(4))
        if (native == null) {
            val size = icon.size
            assertTrue(size.width in 12..64 && size.height in 12..64, "PVA027_SETUP_ICON_SIZE")
            val marker = BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB)
            for (y in 0 until marker.height) for (x in 0 until marker.width) {
                val quadrant = if (y < marker.height / 2) 0 else if (x < marker.width / 2) 1 else 2
                marker.setRGB(x, y, MARKER_COLORS[quadrant])
            }
            val observer = object : MouseAdapter() {
                override fun mousePressed(event: MouseEvent) {
                    if (event.source === icon && event.button == MouseEvent.BUTTON3) rightPresses.incrementAndGet()
                }
            }
            // Register ownership before changing either public native object property/listener list.
            native = NativeOwner(icon, popup, items, icon.image, marker, observer)
            icon.addMouseListener(observer)
            icon.image = marker
        }
        val owner = requireNotNull(native)
        assertSame(owner.icon, icon, "PVA027_TRAY_ICON_REPLACED")
        assertSame(owner.popup, popup, "PVA027_POPUP_REPLACED")
        items.forEachIndexed { index, item ->
            assertSame(owner.items[index], item, "PVA027_MENU_ITEM_REPLACED")
            assertTrue(item.isEnabled, "PVA027_NATIVE_ITEM_DISABLED")
        }
        assertEquals(
            strings, DesktopTrayStrings(icon.toolTip, items[0].label, items[1].label, items[2].label),
            "PVA027_NATIVE_LABELS_$stage",
        )
        observations += "stage=$stage labels=${items.joinToString(" | ") { it.label }} tooltip=${icon.toolTip}"
    }

    private fun exercisePopup(stage: Int, evidence: Path) {
        val input = requireNotNull(robot)
        var target: Rectangle? = null
        val before = captureWhen("PVA027_SETUP_MARKER_$stage") { pixels ->
            target = locateMarker(pixels)
            target != null
        }
        try {
            val marker = requireNotNull(target)
            val previousActions = actions.size
            val previousPresses = rightPresses.get()
            input.mouseMove(marker.x + marker.width / 2, marker.y + marker.height / 2)
            nativeInput("button3", { input.mousePress(InputEvent.BUTTON3_DOWN_MASK) }) {
                input.mouseRelease(InputEvent.BUTTON3_DOWN_MASK)
            }
            await("PVA027_SETUP_NATIVE_TRAY_RIGHT_PRESS_$stage") {
                assertTrue(rightPresses.get() <= previousPresses + 1, "PVA027_EXTRA_RIGHT_PRESS")
                rightPresses.get() == previousPresses + 1
            }
            // A native event from the exact TrayIcon is required BEFORE any menu keyboard input.
            var popup: Rectangle? = null
            val opened = captureWhen("PVA027_SETUP_NATIVE_POPUP_$stage") { pixels ->
                popup = popupBounds(before, pixels, marker)
                popup != null
            }
            // No assumed Home/End support or assumed initial native selection. A visible change
            // after Down is readiness only; the actual highlighted label still needs human review.
            val selected = try {
                tap(KeyEvent.VK_DOWN)
                captureWhen("PVA027_SETUP_POPUP_AFTER_DOWN_$stage") { pixels ->
                    popup = popupBounds(before, pixels, marker)
                    popup?.let { changed(opened, pixels, it, marker).second >= 20 } == true
                }
            } finally { opened.flush() }
            try {
                retainPopupImage(stage, evidence, selected, requireNotNull(popup), marker)
            } finally { selected.flush() }
            tap(KeyEvent.VK_ENTER)
            await("PVA027_NATIVE_POPUP_CALLBACK_$stage") {
                assertTrue(actions.size <= previousActions + 1, "PVA027_EXTRA_NATIVE_CALLBACK")
                actions.size == previousActions + 1
            }
            val action = actions.last()
            assertTrue(action in listOf("$stage:show", "$stage:lock", "$stage:exit"), "PVA027_STALE_NATIVE_CALLBACK")
            // Move only into our blank frame; do not leave hover-tooltip pixels in the dismissal check.
            input.mouseMove(frameMask.centerX.toInt(), frameMask.centerY.toInt())
            captureWhen("PVA027_NATIVE_POPUP_NOT_DISMISSED_$stage") { pixels ->
                changed(before, pixels, requireNotNull(popup), marker).second < 16
            }.flush()
            completedStages++
            observations += "stage=$stage nativeCallback=$action rightPresses=${rightPresses.get()} popupDismissed=true"
        } finally { before.flush() }
    }

    private fun retainPopupImage(
        stage: Int, evidence: Path, selected: BufferedImage, popup: Rectangle, marker: Rectangle,
    ) {
        val crop = popup.union(marker).apply { grow(8, 8) }.intersection(screen)
        assertTrue(crop.width <= 700 && crop.height <= 400, "PVA027_SETUP_CROP_BOUND")
        val picture = selected.getSubimage(crop.x, crop.y, crop.width, crop.height)
        try {
            writeEvidence(evidence, IMAGE_NAMES[stage]) { channel ->
                MemoryCacheImageOutputStream(Channels.newOutputStream(channel)).use { output ->
                    assertTrue(ImageIO.write(picture, "png", output), "PVA027_SETUP_PNG_WRITER")
                    output.flush()
                    channel.force(true) // Force the ORIGINAL channel before the image stream closes it.
                }
            }
        } finally { picture.flush() }
        observations += "stage=$stage nativePopupCrop=${crop.x},${crop.y},${crop.width},${crop.height}"
    }

    private fun locateMarker(pixels: BufferedImage): Rectangle? {
        val boxes = Array(3) { intArrayOf(screen.width, screen.height, -1, -1, 0) }
        for (y in 0 until pixels.height) for (x in 0 until pixels.width) {
            val index = MARKER_COLORS.indexOf(pixels.getRGB(x, y) and 0xffffff)
            if (index >= 0) boxes[index].let {
                it[0] = minOf(it[0], x); it[1] = minOf(it[1], y)
                it[2] = maxOf(it[2], x); it[3] = maxOf(it[3], y); it[4]++
            }
        }
        if (boxes.any { it[4] < 8 }) return null
        val rectangles = boxes.map { Rectangle(it[0], it[1], it[2] - it[0] + 1, it[3] - it[1] + 1) }
        val (top, left, right) = rectangles
        val bounds = top.union(left).union(right)
        val marker = requireNotNull(native).marker
        assertTrue(
            bounds.width in marker.width / 2..marker.width + 4 &&
                bounds.height in marker.height / 2..marker.height + 4,
            "PVA027_SETUP_AMBIGUOUS_MARKER",
        )
        assertTrue(
            abs(top.x - left.x) <= 2 && abs(top.x + top.width - right.x - right.width) <= 2 &&
                top.y < left.y && left.x < right.x && abs(left.y - right.y) <= 2 &&
                abs(left.y - top.y - top.height) <= 2 && abs(left.height - right.height) <= 2,
            "PVA027_SETUP_MARKER_SHAPE",
        )
        rectangles.forEachIndexed { index, box ->
            assertTrue(boxes[index][4] >= box.width * box.height * 3 / 4, "PVA027_SETUP_MARKER_DENSITY")
        }
        return bounds
    }

    private fun popupBounds(before: BufferedImage, after: BufferedImage, marker: Rectangle): Rectangle? {
        val (bounds, count) = changed(before, after, screen, marker)
        if (bounds == null || bounds.width !in 80..560 || bounds.height !in 40..240) return null
        // A small tooltip, icon hover, blank-window focus or isolated pixels are not popup readiness.
        val hasPopupDensity = count >= 500 && count >= bounds.width * bounds.height / 3
        return if (hasPopupDensity && Rectangle(marker).apply { grow(560, 260) }.contains(bounds)) bounds else null
    }

    private fun changed(
        before: BufferedImage, after: BufferedImage, area: Rectangle, marker: Rectangle,
    ): Pair<Rectangle?, Int> {
        val excludedIcon = Rectangle(marker).apply { grow(4, 4) }
        var minX = screen.width; var minY = screen.height; var maxX = -1; var maxY = -1; var count = 0
        for (y in area.y until area.y + area.height) for (x in area.x until area.x + area.width) {
            if (!frameMask.contains(x, y) && !excludedIcon.contains(x, y) &&
                before.getRGB(x, y) != after.getRGB(x, y)
            ) {
                minX = minOf(minX, x); minY = minOf(minY, y)
                maxX = maxOf(maxX, x); maxY = maxOf(maxY, y); count++
            }
        }
        return (if (count == 0) null else Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1)) to count
    }

    private fun captureWhen(message: String, ready: (BufferedImage) -> Boolean): BufferedImage {
        var retained: BufferedImage? = null
        await(message) {
            val pixels = requireNotNull(robot).createScreenCapture(screen)
            var keep = false
            try {
                if (ready(pixels)) { retained = pixels; keep = true }
                keep
            } finally { if (!keep) pixels.flush() }
        }
        return requireNotNull(retained)
    }

    private fun tap(code: Int) {
        val input = requireNotNull(robot)
        nativeInput("key:$code", { input.keyPress(code) }) { input.keyRelease(code) }
    }

    // Always release after a partial press; retain a press failure or fail on a release-only error.
    @Suppress("TooGenericExceptionCaught", "ThrowingExceptionFromFinally")
    private fun nativeInput(id: String, press: () -> Unit, release: () -> Unit) {
        assertTrue(System.nanoTime() < deadline, "PVA027_SOFT_DEADLINE")
        check(id !in heldInput)
        heldInput[id] = release // Register BEFORE native press, including partial failure.
        var failure: Throwable? = null
        try { press() } catch (error: Throwable) { failure = error; throw error } finally {
            try { release(); heldInput.remove(id) } catch (error: Throwable) {
                val primary = failure
                if (primary == null) throw error else primary.addSuppressed(error)
            }
        }
    }

    private fun await(message: String, ready: () -> Boolean) {
        val end = minOf(deadline, System.nanoTime() + TimeUnit.SECONDS.toNanos(5))
        while (System.nanoTime() < end) {
            if (ready()) return
            Thread.sleep(250) // Read-only observation, not repeated native input; at most 20 captures per wait.
        }
        fail(message)
    }

    private fun close(original: LocaleDefaults, originalImageCache: Boolean, evidence: Path, primary: Throwable?) {
        var interrupted = Thread.interrupted() || primary is InterruptedException ||
            primary?.cause is InterruptedException
        var failure: Throwable? = null
        // Assertion/interruption failures cannot skip the other owned releases or become a pass.
        @Suppress("TooGenericExceptionCaught")
        fun release(block: () -> Unit) {
            try { block() } catch (error: Throwable) {
                if (error is InterruptedException || error.cause is InterruptedException) interrupted = true
                val previous = failure
                if (previous == null) failure = error else previous.addSuppressed(error)
            } finally { if (Thread.interrupted()) interrupted = true }
        }
        val nativeCleanup = NativeCleanupState()
        try {
            closeNativeOwners(nativeCleanup, ::release)
            release { ImageIO.setUseCache(originalImageCache) }
            release { Locale.setDefault(original.general) }
            release { Locale.setDefault(Locale.Category.DISPLAY, original.display) }
            release { Locale.setDefault(Locale.Category.FORMAT, original.format) }
            release {
                assertEquals(original.general, Locale.getDefault(), "PVA027_CLEANUP_DEFAULT_LOCALE")
                assertEquals(
                    original.display, Locale.getDefault(Locale.Category.DISPLAY), "PVA027_CLEANUP_DISPLAY_LOCALE",
                )
                assertEquals(original.format, Locale.getDefault(Locale.Category.FORMAT), "PVA027_CLEANUP_FORMAT_LOCALE")
            }
            retainCleanupObservations(evidence, primary, failure, nativeCleanup, ::release)
        } finally {
            if (Thread.interrupted()) interrupted = true
            if (interrupted) Thread.currentThread().interrupt()
        }
        failure?.let { if (primary == null) throw it else primary.addSuppressed(it) }
        // EDT/native stalls and cancellation can outlive soft waits: root must settle the original worker.
    }

    private fun closeNativeOwners(state: NativeCleanupState, release: (() -> Unit) -> Unit) {
        heldInput.toMap().forEach { (id, releaseInput) -> release { releaseInput(); heldInput.remove(id) } }
        release { assertTrue(heldInput.isEmpty(), "PVA027_CLEANUP_HELD_INPUT") }
        release { onEdt { native?.let { it.icon.removeMouseListener(it.observer) } } }
        release { onEdt { native?.let { it.icon.image = it.originalImage } } }
        release { onEdt { tray?.cleanup() } }
        release {
            onEdt {
                if (tray != null) {
                    assertTrue(SystemTray.getSystemTray().trayIcons.isEmpty(), "PVA027_CLEANUP_TRAY_LIVE")
                    state.removed = true
                }
            }
        }
        release { onEdt { frame?.dispose() } }
        release {
            onEdt { assertTrue(frame?.isDisplayable != true, "PVA027_CLEANUP_FRAME_LIVE"); state.disposed = true }
        }
        // Never flush an image that an uncertain, unremoved native icon might still own.
        if (state.removed) release { native?.marker?.flush() }
    }

    private fun retainCleanupObservations(
        evidence: Path, primary: Throwable?, failure: Throwable?, nativeCleanup: NativeCleanupState,
        release: (() -> Unit) -> Unit,
    ) {
        observations += "ownedTrayRemoved=${nativeCleanup.removed} ownedFrameDisposed=${nativeCleanup.disposed}"
        val scenarioStatus = if (primary == null && failure == null) "PASS" else "FAIL"
        observations += "scenarioAndOwnedCleanupStatus=$scenarioStatus"
        observations += "caseDeclarations=1 stagesPlanned=3 stagesCompleted=$completedStages"
        observations += "tooltipVisual=UNPROVED settingsPublisherChain=UNPROVED"
        observations += "productIconRendering=UNPROVED nativeTextReview=REQUIRED"
        observations += "traceWriteResultAndWorkerSettlement=EXTERNAL_JUNIT_AND_ROOT"
        release {
            val bytes = (observations.joinToString("\n") + "\n").toByteArray(Charsets.UTF_8)
            assertTrue(bytes.size <= 16_384, "PVA027_COMPACT_TRACE_BOUND")
            writeEvidence(evidence, "observations.txt") { channel ->
                val buffer = ByteBuffer.wrap(bytes)
                while (buffer.hasRemaining()) channel.write(buffer)
                channel.force(true)
            }
        }
    }

    private fun admittedEvidence(): Path {
        val display = System.getProperty("passvault.pva027.syntheticDisplay")
        assumeTrue("PVA-027 requires separately admitted private native tray execution", display != null)
        assertTrue(Regex(":\\d{1,5}(?:\\.0)?").matches(requireNotNull(display)))
        assertEquals("Linux", System.getProperty("os.name"), "PVA027_SETUP_NOT_LINUX")
        assertEquals("17", System.getProperty("java.specification.version"), "PVA027_SETUP_NOT_JDK17")
        assertEquals(display, System.getenv("DISPLAY"), "PVA027_SETUP_DISPLAY_MISMATCH")
        assertTrue(System.getenv("WAYLAND_DISPLAY").isNullOrBlank(), "PVA027_SETUP_NOT_X11_ONLY")
        val session = privateDirectory(requireNotNull(System.getProperty("passvault.pva027.privateSessionDir")))
        mapOf(
            "HOME" to "home", "XDG_CONFIG_HOME" to "config", "XDG_CACHE_HOME" to "cache",
            "XDG_DATA_HOME" to "data", "XDG_RUNTIME_DIR" to "runtime",
        ).forEach { (variable, leaf) ->
            val directory = privateDirectory(session.resolve(leaf).toString())
            assertEquals(directory.toString(), System.getenv(variable), "PVA027_SETUP_$variable")
        }
        assertEquals(session.resolve("home").toString(), System.getProperty("user.home"), "PVA027_SETUP_JVM_HOME")
        assertEquals(
            session.resolve("runtime").toString(), System.getProperty("java.io.tmpdir"), "PVA027_SETUP_JVM_TMP",
        )
        assertEquals(session.resolve("runtime").toString(), System.getenv("TMPDIR"), "PVA027_SETUP_TMPDIR")
        val auth = session.resolve("Xauthority")
        assertTrue(Files.isRegularFile(auth, NOFOLLOW_LINKS), "PVA027_SETUP_AUTH_FILE")
        assertEquals(PosixFilePermissions.fromString("rw-------"), Files.getPosixFilePermissions(auth, NOFOLLOW_LINKS))
        assertEquals(auth.toString(), System.getenv("XAUTHORITY"), "PVA027_SETUP_XAUTHORITY")
        val bus = session.resolve("bus")
        assertTrue(Files.exists(bus, NOFOLLOW_LINKS) && !Files.isSymbolicLink(bus), "PVA027_SETUP_BUS_PATH")
        val busPattern = Regex("unix:path=${Regex.escape(bus.toString())}(?:,guid=[0-9a-f]{32})?")
        assertTrue(busPattern.matches(System.getenv("DBUS_SESSION_BUS_ADDRESS").orEmpty()), "PVA027_SETUP_PRIVATE_BUS")
        val evidence = privateDirectory(requireNotNull(System.getProperty("passvault.pva027.evidenceDir")))
        Files.newDirectoryStream(evidence).use {
            assertFalse(it.iterator().hasNext(), "PVA027_SETUP_EVIDENCE_NOT_EMPTY")
        }
        return evidence
    }

    private fun privateDirectory(value: String): Path {
        val path = Path.of(value)
        assertTrue(path.isAbsolute && path == path.normalize(), "PVA027_SETUP_ABSOLUTE_NORMALIZED_PATH")
        var parent: Path? = path
        while (parent != null) {
            assertFalse(Files.isSymbolicLink(parent), "PVA027_SETUP_SYMLINK")
            parent = parent.parent
        }
        assertTrue(Files.isDirectory(path, NOFOLLOW_LINKS), "PVA027_SETUP_PRIVATE_DIRECTORY")
        assertEquals(PosixFilePermissions.fromString("rwx------"), Files.getPosixFilePermissions(path, NOFOLLOW_LINKS))
        return path
    }

    private fun writeEvidence(evidence: Path, name: String, write: (FileChannel) -> Unit) {
        assertTrue(name in IMAGE_NAMES || name == "observations.txt")
        val path = evidence.resolve(name)
        val mode = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------"))
        FileChannel.open(path, setOf(CREATE_NEW, WRITE), mode).use(write)
        FileChannel.open(evidence, READ).use { it.force(true) }
        assertTrue(Files.size(path) in 1L..1_048_576L, "PVA027_COMPACT_EVIDENCE_BOUND")
    }

    private fun <T> onEdt(block: () -> T): T {
        if (SwingUtilities.isEventDispatchThread()) return block()
        val task = FutureTask(Callable(block))
        SwingUtilities.invokeLater(task)
        var completed = false
        return try {
            task.get(5, TimeUnit.SECONDS).also { completed = true }
        } finally {
            // Suppress work not yet started; this cannot settle a stalled running native call.
            if (!completed) task.cancel(false)
        }
    }

    // Keep live flags until observation: a timed-out EDT call may still finish during later cleanup.
    private class NativeCleanupState {
        var removed = false
        var disposed = false
    }

    private data class NativeOwner(
        val icon: TrayIcon, val popup: PopupMenu, val items: List<MenuItem>, val originalImage: Image,
        val marker: BufferedImage, val observer: MouseAdapter,
    )
    private data class LocaleDefaults(val general: Locale, val display: Locale, val format: Locale)

    private companion object {
        val ENGLISH = DesktopTrayStrings("PassVault Password Manager", "Show PassVault", "Lock Vault", "Exit")
        val ARABIC = DesktopTrayStrings("مدير كلمات المرور PassVault", "إظهار PassVault", "قفل الخزنة", "خروج")
        val MARKER_COLORS = intArrayOf(0xEF11AD, 0x17CEDF, 0xF5D917)
        val IMAGE_NAMES = listOf("01-native-en.png", "02-native-ar.png", "03-native-en.png")
    }
}
