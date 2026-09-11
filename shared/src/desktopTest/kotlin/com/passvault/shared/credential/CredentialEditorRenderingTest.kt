package com.passvault.shared.credential

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.passvault.core.crypto.SecurePasswordGenerator
import com.passvault.core.designsystem.theme.PassVaultTheme
import com.passvault.core.domain.model.CredentialId
import com.passvault.core.domain.model.CredentialType
import com.passvault.core.domain.model.CustomField
import com.passvault.core.domain.model.CustomFieldId
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.testing.TestData
import com.passvault.core.testing.fakes.FakeCredentialRepository
import com.passvault.core.testing.fakes.FakeCryptoEngine
import com.passvault.core.testing.fakes.FakeFolderRepository
import com.passvault.feature.credential.presentation.CredentialViewModel
import com.passvault.feature.credential.presentation.CredentialViewModel.CredentialEvent
import com.passvault.feature.credential.ui.CredentialEditScreen
import java.awt.GraphicsEnvironment
import java.awt.IllegalComponentStateException
import java.awt.Rectangle
import java.awt.Robot
import java.awt.Window
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.LinkOption.NOFOLLOW_LINKS
import java.nio.file.Path
import java.nio.file.StandardOpenOption.CREATE_NEW
import java.nio.file.StandardOpenOption.READ
import java.nio.file.StandardOpenOption.WRITE
import java.nio.file.attribute.PosixFilePermissions
import java.util.IdentityHashMap
import java.util.Locale
import java.util.concurrent.Callable
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import javax.accessibility.AccessibleContext
import javax.accessibility.AccessibleRole
import javax.accessibility.AccessibleState
import javax.accessibility.AccessibleText
import javax.imageio.ImageIO
import javax.imageio.stream.MemoryCacheImageOutputStream
import javax.swing.SwingUtilities
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assume.assumeTrue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * PVA-007/031: real production form, real ViewModel, real AWT mouse/keyboard input.
 * Persistence is a deep-copying synthetic in-memory repository. Reload replaces the
 * ViewModel and composed form, NOT a real Room database or full navigation host.
 *
 * Separately admit the exact source/runtime, private X display, English keyboard/locale,
 * evidence directory and cleanup before opting in. Missing opt-in skips; opted-in missing
 * bridge/targeting/native input fails setup. JDK accessibility reads locate controls only:
 * no accessible action, text mutation, focus request, hit-test or reflection is used.
 *
 * These are settled English/LTR Desktop flows, not a deterministic pre-frame race, IME,
 * mobile Back, screen-reader, RTL, hardware or universal owner-disposal retention test.
 * Cropped synthetic screenshots supplement assertions; they are not OCR/pixel oracles.
 */
@OptIn(ExperimentalComposeUiApi::class)
class CredentialEditorRenderingTest {
    @Test
    fun `native pending row draft survives page Save and a fresh rendered editor load`() = withEditor("page", 1) {
        clickEvent(button("Edit"), "OnCustomFieldEditStarted")
        replaceText("Field Name", "pagefield")
        replaceText("Value", "pagevalue")
        onEdt {
            val draft = assertNotNull(model.state.value.customFieldDrafts[CustomFieldId("auditfield0")])
            assertEquals("pagefield", draft.name, "PVA007_NATIVE_DRAFT_NAME")
            assertEquals("pagevalue", draft.value, "PVA007_NATIVE_DRAFT_VALUE")
        }
        // Do not click the row checkmark: the production page Save must adopt its visible draft.
        saveAndReload()
        assertEquals(listOf(FieldValue("auditfield0", "pagefield", "pagevalue", false)), fields())
        scrollTo(named("pagefield"))
        assertTrue(visible(named("pagevalue")), "PVA007_RELOADED_VALUE_NOT_VISIBLE")
        picture("01-page-save-reload.png")
    }

    @Test
    fun `native row Save commits its tuple and row Cancel does not persist later edits`() = withEditor("row", 1) {
        clickEvent(button("Edit"), "OnCustomFieldEditStarted")
        replaceText("Field Name", "rowsaved")
        replaceText("Value", "rowsavedvalue")
        click(checkBox("Secret field"))
        settle()
        assertTrue(find(checkBox("Secret field")).checked, "PVA007_SECRET_CHECKBOX_NOT_CHECKED")
        clickEvent(button("Save", last = true), "OnCustomFieldEditSaved")
        settle()
        val expected = listOf(FieldValue("auditfield0", "rowsaved", "rowsavedvalue", true))
        assertEquals(expected, fields(), "PVA007_ROW_SAVE_TUPLE")

        clickEvent(button("Edit"), "OnCustomFieldEditStarted")
        replaceText("Field Name", "cancelledname")
        click(checkBox("Secret field"))
        settle()
        assertFalse(find(checkBox("Secret field")).checked)
        replaceText("Value", "cancelledvalue")
        clickEvent(button("Cancel"), "OnCustomFieldEditCancelled")
        settle()
        assertEquals(expected, fields(), "PVA007_ROW_CANCEL_CHANGED_COMMITTED_TUPLE")
        saveAndReload()
        assertEquals(expected, fields(), "PVA007_CANCELLED_VALUES_PERSISTED")
        scrollTo(named("rowsaved"))
        assertTrue(visible(named("Password is hidden")), "PVA007_RELOADED_SECRET_NOT_MASKED")
        picture("02-row-save-cancel-reload.png")
    }

    @Test
    fun `native capacity Add and retained composed input survive a controlled capacity transition`() =
        withEditor("capacity", 50) {
            scrollTo(button("Add"))
            assertFalse(find(button("Add")).enabled, "PVA031_FULL_CAPACITY_ADD_ENABLED")
            click(button("Add"), allowDisabled = true)
            settle()
            assertFalse(dialogVisible(), "PVA031_DISABLED_ADD_OPENED_DIALOG")
            assertEquals(seedValues(50), fields())

            clickEvent(button("Remove", first = true), "OnCustomFieldRemoved")
            settle()
            assertEquals(seedValues(50).drop(1), fields(), "PVA031_REMOVE_CHANGED_SURVIVORS")
            assertTrue(find(button("Add")).enabled)
            click(button("Add"))
            await("EDITOR_SETUP_ADD_DIALOG") { dialogVisible() }
            replaceText("Field Name", "capacityfield")
            replaceText("Value", "capacityvalue")

            // Explicit controlled state interference while the real dialog remains composed.
            // NOT native input, a second editor, concurrent persistence, or backend-ack evidence.
            onEdt { model.onEvent(CredentialEvent.OnCustomFieldAdded("injectedfield", "injectedvalue", false)) }
            settle()
            assertEquals(50, fields().size)
            assertFalse(find(this::dialogAdd).enabled, "PVA031_FULL_DIALOG_CONFIRM_ENABLED")
            click(this::dialogAdd, allowDisabled = true)
            settle()
            assertTrue(dialogVisible(), "PVA031_REJECTED_CONFIRM_DISPOSED_DIALOG")
            assertEquals("capacityfield", find(textField("Field Name")).text, "PVA031_RETAINED_NAME")
            assertEquals("capacityvalue", find(textField("Value")).text, "PVA031_RETAINED_VALUE")
            picture("03-capacity-retained-input.png")
            onEdt {
                val injected = model.state.value.customFields.single { it.name == "injectedfield" }
                model.onEvent(CredentialEvent.OnCustomFieldRemoved(injected.id))
            }
            settle()
            assertTrue(find(this::dialogAdd).enabled)
            clickEvent(this::dialogAdd, "OnCustomFieldAdded")
            settle()
            assertFalse(dialogVisible(), "PVA031_ACCEPTED_DIALOG_NOT_DISMISSED")
            val accepted = fields()
            assertEquals(50, accepted.size)
            assertEquals(seedValues(50).drop(1), accepted.dropLast(1), "PVA031_ADD_CHANGED_SURVIVORS")
            assertEquals(FieldValue(accepted.last().id, "capacityfield", "capacityvalue", false), accepted.last())
            assertEquals(50, accepted.map { it.id }.toSet().size)
            saveAndReload()
            assertEquals(accepted, fields(), "PVA031_NATIVE_SAVE_RELOAD_CHANGED_FIELDS")
            assertFalse(find(button("Add")).enabled)
            scrollTo(named("capacityfield"))
            assertTrue(visible(named("capacityvalue")))
            picture("04-capacity-save-reload.png")
        }

    // Preserve assertions, native failures and cancellation as primary while the owned fixture closes.
    @Suppress("TooGenericExceptionCaught")
    private fun withEditor(caseId: String, count: Int, block: Editor.() -> Unit) {
        val display = System.getProperty("passvault.editor.syntheticDisplay")
        assumeTrue("Rendered editor checks require a separately admitted synthetic display", display != null)
        val editor = Editor(caseId, count)
        var failure: Throwable? = null
        try {
            editor.start(requireNotNull(display))
            editor.block()
        } catch (error: Throwable) {
            failure = error
            throw error
        } finally {
            editor.close(failure)
        }
    }

    private data class FieldValue(val id: String, val name: String, val value: String, val secret: Boolean)

    private fun seedValues(count: Int) = List(count) { FieldValue("auditfield$it", "field$it", "value$it", false) }

    /** The only UI driver in this file; bounded and specific to this production form. */
    @Suppress("TooManyFunctions") // One driver owns the form, native input and ordered cleanup together.
    private inner class Editor(private val caseId: String, private val count: Int) {
        private val repository = FakeCredentialRepository()
        private val stores = CopyOnWriteArrayList<ViewModelStore>()
        private val jobs = CopyOnWriteArrayList<Job>()
        private val currentModel = mutableStateOf<CredentialViewModel?>(null)
        private val nativeEvents = mutableMapOf<String, Int>() // Types only; never retain event payloads.
        private val frameRequest = mutableIntStateOf(0)
        private val frameAck = AtomicInteger(-1)
        private var window: ComposeWindow? = null
        private var robot: Robot? = null
        private var evidence: Path? = null
        private var deadline = Long.MAX_VALUE
        private val pressedKeys = mutableSetOf<Int>()
        private var mousePressed = false
        val model: CredentialViewModel get() = requireNotNull(currentModel.value)

        // Keep admission, registered owner setup and the first real rendered frame in one ordered boundary.
        @Suppress("LongMethod")
        fun start(display: String) {
            deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(120)
            assertEquals("Linux", System.getProperty("os.name"), "EDITOR_SETUP_LINUX_ONLY")
            assertTrue(Regex(":\\d{1,5}(?:\\.0)?").matches(display), "EDITOR_SETUP_DISPLAY_SYNTAX")
            assertEquals(display, System.getenv("DISPLAY"), "EDITOR_SETUP_DISPLAY_MISMATCH")
            assertTrue(System.getenv("WAYLAND_DISPLAY").isNullOrBlank(), "EDITOR_SETUP_X11_ONLY")
            assertFalse(GraphicsEnvironment.isHeadless(), "EDITOR_SETUP_HEADLESS")
            assertEquals("en", Locale.getDefault().language, "EDITOR_SETUP_ENGLISH_LOCALE")
            assertTrue(System.getProperty("compose.accessibility.enable") != "false", "EDITOR_SETUP_A11Y_DISABLED")
            assertEquals(null, System.getenv("COMPOSE_DISABLE_ACCESSIBILITY"), "EDITOR_SETUP_A11Y_DISABLED")
            evidence = Path.of(requireNotNull(System.getProperty("passvault.editor.evidenceDir"))).also {
                assertTrue(it.isAbsolute && Files.isDirectory(it, NOFOLLOW_LINKS), "EDITOR_SETUP_EVIDENCE_DIRECTORY")
            }
            onEdt {
                val seed = TestData.credential(
                    id = "rendered-$caseId", type = CredentialType.SecureNote,
                    title = "Synthetic editor audit", username = "", password = "", url = "https://example.invalid",
                ).copy(customFields = seedValues(count).map {
                    CustomField(CustomFieldId(it.id), it.name, SensitiveText.from(it.value), it.secret)
                })
                try { repository.setupCredentials(seed) } finally { seed.clearSensitiveValues() }
                currentModel.value = newModel()
                val current = ComposeWindow().also { window = it }
                current.title = "PassVault synthetic editor audit"
                current.setSize(800, 640)
                current.setLocation(48, 48)
                current.isResizable = false
                current.setContent {
                    val frame = frameRequest.intValue
                    LaunchedEffect(frame) {
                        // A real window frame barrier, NOT a pixel oracle or synthetic recomposer.
                        withFrameNanos { }
                        withFrameNanos { }
                        frameAck.set(frame)
                    }
                    val owner = requireNotNull(currentModel.value)
                    key(owner) {
                        val state by owner.state.collectAsState()
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            PassVaultTheme(darkTheme = false) {
                                CredentialEditScreen(
                                    state = state,
                                    onEvent = { event ->
                                        val type = event.javaClass.simpleName
                                        nativeEvents[type] = (nativeEvents[type] ?: 0) + 1
                                        owner.onEvent(event)
                                    },
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }
                    }
                }
                current.isVisible = true
                current.toFront()
                current.requestFocus()
            }
            robot = Robot().apply { autoDelay = 12 }
            settle()
            await("EDITOR_SETUP_ACTIVE_RENDERED_WINDOW") {
                onEdt {
                    val current = requireNotNull(window)
                    current.isActive && current.windowHandle != 0L && current.renderApi.toString() != "UNKNOWN"
                }
            }
            assertTrue(onEdt { model.state.value.isCredentialLoaded }, "EDITOR_SETUP_SYNTHETIC_LOAD")
            // This is where absent actual Skia/JDK linkage is a setup failure, not a product finding.
            assertEquals("Synthetic editor audit", find(textField("Title")).text, "EDITOR_SETUP_EDITABLE_TREE")
        }

        private fun newModel(): CredentialViewModel {
            val store = ViewModelStore().also(stores::add)
            val result = ViewModelProvider.create(store, viewModelFactory {
                initializer {
                    CredentialViewModel(repository, FakeFolderRepository(), SecurePasswordGenerator(FakeCryptoEngine()))
                }
            })[CredentialViewModel::class]
            jobs += requireNotNull(result.viewModelScope.coroutineContext[Job])
            result.loadCredential(CredentialId("rendered-$caseId"))
            return result
        }

        fun fields(): List<FieldValue> = onEdt {
            model.state.value.customFields.map {
                FieldValue(it.id.value, it.name, it.value.toStringUnsafe(), it.isSecret)
            }
        }

        fun saveAndReload() {
            clickEvent(button("Save", first = true), "OnSaveClick")
            await("EDITOR_SAVE_DID_NOT_COMPLETE") {
                onEdt {
                    val state = model.state.value
                    !state.isSaving && !state.hasUnsavedChanges && state.errorMessage == null
                }
            }
            onEdt {
                val previous = stores.last()
                currentModel.value = newModel()
                previous.clear()
            }
            settle()
            assertTrue(onEdt { model.state.value.isCredentialLoaded }, "EDITOR_RELOADED_ENTRY_NOT_LOADED")
            assertTrue(onEdt { model.state.value.customFieldDrafts.isEmpty() })
        }

        fun settle() {
            val requested = onEdt { (++frameRequest.intValue) }
            await("EDITOR_SETUP_REAL_FRAME_PROGRESS") { frameAck.get() >= requested }
        }

        fun replaceText(label: String, value: String) {
            assertTrue(value.all { it in 'a'..'z' }, "Only synthetic lowercase ASCII input is admitted")
            val selector = textField(label)
            click(selector)
            await("EDITOR_SETUP_NATIVE_TEXT_FOCUS_$label") { find(selector).focused }
            val owner = find(selector).window
            assertNativeWindow(owner)
            withKey(KeyEvent.VK_CONTROL) { press(KeyEvent.VK_A) }
            value.forEach {
                assertNativeWindow(owner)
                press(KeyEvent.getExtendedKeyCodeForChar(it.code))
            }
            settle()
            assertEquals(value, find(selector).text, "EDITOR_NATIVE_TEXT_RESULT_$label")
        }

        private fun press(code: Int) = withKey(code) { }

        // Release even after a partial press; rethrow the primary and attach secondary release failures.
        @Suppress("TooGenericExceptionCaught", "ThrowingExceptionFromFinally")
        private fun withKey(code: Int, action: () -> Unit) {
            val native = requireNotNull(robot)
            pressedKeys += code // Register cleanup BEFORE the press, including a partially failing native call.
            var failure: Throwable? = null
            try {
                native.keyPress(code)
                action()
            } catch (error: Throwable) {
                failure = error
                throw error
            } finally {
                try {
                    native.keyRelease(code)
                    pressedKeys -= code
                } catch (error: Throwable) {
                    val primary = failure
                    if (primary == null) throw error else primary.addSuppressed(error)
                }
            }
        }

        fun clickEvent(selector: (List<Ax>) -> Ax?, event: String) {
            val before = onEdt { nativeEvents[event] ?: 0 }
            click(selector)
            await("EDITOR_SETUP_NATIVE_CALLBACK_$event") { onEdt { (nativeEvents[event] ?: 0) == before + 1 } }
        }

        // A release-only failure must fail the test; a failed press remains primary across release.
        @Suppress("TooGenericExceptionCaught", "ThrowingExceptionFromFinally")
        fun click(selector: (List<Ax>) -> Ax?, allowDisabled: Boolean = false) {
            val target = scrollTo(selector)
            assertTrue(target.enabled || allowDisabled, "EDITOR_TARGET_UNEXPECTEDLY_DISABLED")
            val native = requireNotNull(robot)
            val rectangle = checkedRectangle(target)
            native.mouseMove(rectangle.x + rectangle.width / 2, rectangle.y + rectangle.height / 2)
            val fresh = find(selector)
            assertTrue(target.context === fresh.context, "EDITOR_SETUP_TARGET_REPLACED")
            assertEquals(rectangle, checkedRectangle(fresh), "EDITOR_SETUP_TARGET_MOVED")
            assertTrue(fresh.enabled || allowDisabled, "EDITOR_TARGET_BECAME_DISABLED")
            mousePressed = true
            var failure: Throwable? = null
            try {
                native.mousePress(InputEvent.BUTTON1_DOWN_MASK)
            } catch (error: Throwable) {
                failure = error
                throw error
            } finally {
                try {
                    native.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
                    mousePressed = false
                } catch (error: Throwable) {
                    val primary = failure
                    if (primary == null) throw error else primary.addSuppressed(error)
                }
            }
        }

        private fun checkedRectangle(target: Ax): Rectangle = onEdt {
            assertNativeWindow(target.window)
            val rectangle = assertNotNull(target.screen, "EDITOR_SETUP_MISSING_TARGET_GEOMETRY")
            assertTrue(rectangle.width > 0 && rectangle.height > 0, "EDITOR_SETUP_EMPTY_TARGET_GEOMETRY")
            assertTrue(clientBounds(target.window).contains(rectangle), "EDITOR_SETUP_TARGET_CLIPPED")
            Rectangle(rectangle)
        }

        fun scrollTo(selector: (List<Ax>) -> Ax?): Ax {
            repeat(65) {
                val target = find(selector)
                val rectangle = assertNotNull(target.screen, "EDITOR_SETUP_TARGET_HAS_NO_SCREEN_GEOMETRY")
                val client = onEdt { clientBounds(target.window) }
                if (client.contains(rectangle) && rectangle.width > 0 && rectangle.height > 0) {
                    var previous = rectangle
                    var stableSince = System.nanoTime()
                    await("EDITOR_SETUP_TARGET_GEOMETRY_NOT_STABLE") {
                        val next = find(selector)
                        if (next.screen != previous) {
                            previous = requireNotNull(next.screen)
                            stableSince = System.nanoTime()
                        }
                        client.contains(previous) &&
                            System.nanoTime() - stableSince >= TimeUnit.MILLISECONDS.toNanos(80)
                    }
                    return find(selector)
                }
                assertTrue(rectangle.width in 1..client.width, "EDITOR_SETUP_HORIZONTAL_CLIPPING")
                assertNativeWindow(target.window)
                val native = requireNotNull(robot)
                native.mouseMove(client.x + client.width / 2, client.y + client.height / 2)
                native.mouseWheel(if (rectangle.y < client.y) -4 else 4)
                settle()
            }
            fail("EDITOR_SETUP_SCROLL_TARGET_NOT_REACHED")
        }

        fun visible(selector: (List<Ax>) -> Ax?): Boolean {
            val target = find(selector)
            return target.screen?.let {
                it.width > 0 && it.height > 0 && onEdt { clientBounds(target.window).contains(it) }
            } == true
        }

        fun find(selector: (List<Ax>) -> Ax?): Ax {
            var result: Ax? = null
            await("EDITOR_SETUP_UNIQUE_ACCESSIBLE_TARGET") {
                result = onEdt { selector(snapshot()) }
                result != null
            }
            return requireNotNull(result)
        }

        fun dialogVisible(): Boolean = onEdt { snapshot().any { it.name == "Add Custom Field" } }

        /** Lowest common semantic ancestor of the title and exactly two dialog edit controls. */
        fun dialogAdd(nodes: List<Ax>): Ax? {
            var ancestor = unique(nodes.filter { it.name == "Add Custom Field" })?.parent
            while (ancestor != null) {
                val candidate = ancestor
                val members = nodes.filter { node ->
                    var parent: Int? = node.index
                    while (parent != null && parent != candidate) parent = nodes[parent].parent
                    parent == candidate
                }
                if (members.count { it.editable } == 2 && members.any { it.isNamedField("Field Name") } &&
                    members.any { it.isNamedField("Value") }) {
                    return unique(members.filter { it.role == AccessibleRole.PUSH_BUTTON && it.name == "Add" })
                }
                ancestor = nodes[candidate].parent
            }
            return null
        }

        private fun snapshot(): List<Ax> {
            val result = mutableListOf<Ax>()
            val seen = IdentityHashMap<AccessibleContext, Boolean>()
            fun visit(context: AccessibleContext?, owner: Window, parent: Int?, depth: Int) {
                if (context == null || seen.put(context, true) != null) return
                assertTrue(depth <= 40 && result.size < 2048, "EDITOR_SETUP_ACCESSIBLE_TREE_LIMIT")
                val component = context.accessibleComponent
                val screen = try {
                    val position = component?.locationOnScreen
                    val size = component?.size
                    if (position != null && size != null) Rectangle(position, size) else null
                } catch (_: IllegalComponentStateException) { null }
                val text = context.accessibleText?.let { content ->
                    val length = content.charCount
                    assertTrue(length in 0..256, "EDITOR_SETUP_SYNTHETIC_TEXT_BOUND")
                    buildString { repeat(length) { append(content.getAtIndex(AccessibleText.CHARACTER, it)) } }
                }
                val index = result.size
                val states = context.accessibleStateSet
                result += Ax(
                    index, parent, context, owner, context.accessibleRole, context.accessibleName, text,
                    context.accessibleEditableText != null, component?.isEnabled == true,
                    states.contains(AccessibleState.FOCUSED), states.contains(AccessibleState.CHECKED), screen,
                )
                val count = context.accessibleChildrenCount
                assertTrue(count in 0..2048, "EDITOR_SETUP_ACCESSIBLE_CHILD_LIMIT")
                repeat(count) { visit(context.getAccessibleChild(it)?.accessibleContext, owner, index, depth + 1) }
            }
            ownedWindows().filter { it.isShowing }.forEach { visit(it.accessibleContext, it, null, 0) }
            return result
        }

        private fun ownedWindows(): List<Window> {
            val result = mutableListOf<Window>()
            fun visit(current: Window) {
                assertTrue(result.size < 8 && current !in result, "EDITOR_SETUP_OWNED_WINDOW_LIMIT")
                result += current
                current.ownedWindows.forEach(::visit)
            }
            window?.let(::visit)
            return result
        }

        private fun clientBounds(owner: Window): Rectangle {
            val insets = owner.insets
            val origin = owner.locationOnScreen
            return Rectangle(
                origin.x + insets.left, origin.y + insets.top,
                owner.width - insets.left - insets.right, owner.height - insets.top - insets.bottom,
            ).also {
                assertTrue(it.width in 200..1000 && it.height in 200..800, "EDITOR_SETUP_CLIENT_GEOMETRY")
                assertTrue(owner.graphicsConfiguration.bounds.contains(it), "EDITOR_SETUP_CLIENT_OUTSIDE_DISPLAY")
            }
        }

        private fun assertNativeWindow(owner: Window) = onEdt {
            assertTrue(owner in ownedWindows() && owner.isShowing && owner.isActive, "EDITOR_SETUP_NATIVE_WINDOW")
            clientBounds(owner)
            Unit
        }

        fun picture(name: String) {
            assertTrue(name in IMAGE_NAMES)
            val owner = onEdt { ownedWindows().single { it.isActive } }
            assertNativeWindow(owner)
            val rectangle = onEdt { clientBounds(owner) }
            val pixels = requireNotNull(robot).createScreenCapture(rectangle)
            try {
                // Existing fresh root-allocated directory, fixed basename, exclusive output, no ImageIO disk cache.
                val path = requireNotNull(evidence).resolve(name)
                val mode = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------"))
                FileChannel.open(path, setOf(CREATE_NEW, WRITE), mode).use { channel ->
                    MemoryCacheImageOutputStream(Channels.newOutputStream(channel)).use { output ->
                        assertTrue(ImageIO.write(pixels, "png", output), "EDITOR_SETUP_PNG_WRITER")
                        output.flush()
                        channel.force(true)
                    }
                }
                FileChannel.open(path.parent, READ).use { it.force(true) }
                assertTrue(Files.size(path) in 1..1_048_576, "EDITOR_SETUP_COMPACT_PNG_BOUND")
            } finally { pixels.flush() }
        }

        fun await(message: String, condition: () -> Boolean) {
            val end = minOf(deadline, System.nanoTime() + TimeUnit.SECONDS.toNanos(5))
            while (System.nanoTime() < end) {
                if (condition()) return
                Thread.sleep(20)
            }
            fail(message)
        }

        // Keep owner-local cancellation, settlement and interruption restoration in explicit order.
        @Suppress("CyclomaticComplexMethod")
        fun close(primary: Throwable?) {
            var interrupted = Thread.interrupted() || primary is InterruptedException
            var cleanupFailure: Throwable? = null
            // A failed release, including an assertion, must not skip the remaining owned resources.
            @Suppress("TooGenericExceptionCaught")
            fun release(block: () -> Unit) {
                try { block() } catch (error: Throwable) {
                    if (error is InterruptedException || error.cause is InterruptedException) interrupted = true
                    val previous = cleanupFailure
                    if (previous == null) cleanupFailure = error else previous.addSuppressed(error)
                } finally {
                    if (Thread.interrupted()) interrupted = true
                }
            }
            try {
                // Every owner receives a cancellation request before settlement; never enumerate global windows/jobs.
                pressedKeys.toList().forEach { code -> release {
                    requireNotNull(robot).keyRelease(code)
                    pressedKeys -= code
                } }
                if (mousePressed) release {
                    requireNotNull(robot).mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
                    mousePressed = false
                }
                var closingWindows = emptyList<Window>()
                release { closingWindows = onEdt { ownedWindows().asReversed() } }
                closingWindows.forEach { owned -> release { onEdt { owned.dispose() } } }
                release { onEdt { window?.takeIf { it !in closingWindows }?.dispose() } }
                stores.forEach { store -> release { onEdt { store.clear() } } }
                jobs.forEach { job -> release { job.cancel() } }
                jobs.forEach { job -> release { runBlocking { withTimeout(3_000) { job.join() } } } }
                release {
                    onEdt {
                        assertTrue(jobs.all { it.isCompleted }, "EDITOR_CLEANUP_VM_NOT_SETTLED")
                        repository.reset()
                        currentModel.value = null
                        nativeEvents.clear()
                    }
                }
                release { onEdt { assertTrue(ownedWindows().none { it.isDisplayable }, "EDITOR_CLEANUP_WINDOW_LIVE") } }
            } finally {
                if (Thread.interrupted()) interrupted = true
                if (interrupted) Thread.currentThread().interrupt()
            }
            cleanupFailure?.let { if (primary == null) throw it else primary.addSuppressed(it) }
            // External root admission must handle interruption/EDT/native stalls and original worker settlement.
        }
    }

    private data class Ax(
        val index: Int,
        val parent: Int?,
        val context: AccessibleContext,
        val window: Window,
        val role: AccessibleRole,
        val name: String?,
        val text: String?,
        val editable: Boolean,
        val enabled: Boolean,
        val focused: Boolean,
        val checked: Boolean,
        val screen: Rectangle?,
    ) {
        fun isNamedField(label: String): Boolean = editable && (name == label || name?.startsWith("$label, ") == true)
    }

    private fun unique(nodes: List<Ax>): Ax? {
        assertTrue(nodes.size <= 1, "EDITOR_SETUP_AMBIGUOUS_ACCESSIBLE_TARGET")
        return nodes.singleOrNull()
    }

    private fun textField(label: String): (List<Ax>) -> Ax? = { nodes ->
        unique(nodes.filter { it.isNamedField(label) })
    }
    private fun named(name: String): (List<Ax>) -> Ax? = { nodes -> unique(nodes.filter { it.name == name }) }
    private fun checkBox(name: String): (List<Ax>) -> Ax? = { nodes ->
        unique(nodes.filter { it.role == AccessibleRole.CHECK_BOX && it.name == name })
    }

    private fun button(name: String, first: Boolean = false, last: Boolean = false): (List<Ax>) -> Ax? = { nodes ->
        val matches = nodes.filter { it.role == AccessibleRole.PUSH_BUTTON && it.name == name }
        // In this fixed form Save is page-first/row-last; Remove order is the production keyed row order.
        if (matches.isEmpty()) null else {
            val expected = when {
                first && name == "Remove" -> 50..50
                first && name == "Save" -> 1..2
                last && name == "Save" -> 2..2
                else -> 1..1
            }
            assertTrue(matches.size in expected, "EDITOR_SETUP_UNEXPECTED_BUTTON_CARDINALITY")
            val extreme = when {
                first -> matches.minOf { requireNotNull(it.screen).y }
                last -> matches.maxOf { requireNotNull(it.screen).y }
                else -> null
            }
            unique(if (extreme == null) matches else matches.filter { it.screen?.y == extreme })
        }
    }

    @Suppress("TooGenericExceptionCaught") // Any failed wait must cancel an EDT action not yet started.
    private fun <T> onEdt(block: () -> T): T {
        if (SwingUtilities.isEventDispatchThread()) return block()
        val task = FutureTask(Callable(block))
        SwingUtilities.invokeLater(task)
        return try { task.get(5, TimeUnit.SECONDS) } catch (error: Throwable) {
            task.cancel(false) // Prevent a not-yet-started setup action from running after failure.
            throw error
        }
    }

    private companion object {
        val IMAGE_NAMES = setOf(
            "01-page-save-reload.png", "02-row-save-cancel-reload.png",
            "03-capacity-retained-input.png", "04-capacity-save-reload.png",
        )
    }
}
