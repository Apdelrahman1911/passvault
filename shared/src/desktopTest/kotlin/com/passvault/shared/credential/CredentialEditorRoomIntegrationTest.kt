package com.passvault.shared.credential

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
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
import com.passvault.core.crypto.DesktopCryptoEngine
import com.passvault.core.crypto.SecurePasswordGenerator
import com.passvault.core.crypto.VaultKeyHierarchy
import com.passvault.core.database.VaultDatabaseBootstrap
import com.passvault.core.database.VaultDatabaseBootstrapResult
import com.passvault.core.database.createDatabaseBootstrap
import com.passvault.core.database.repository.CredentialRepositoryImpl
import com.passvault.core.database.repository.FolderRepositoryImpl
import com.passvault.core.database.repository.VaultRepositoryImpl
import com.passvault.core.designsystem.theme.PassVaultTheme
import com.passvault.core.domain.model.CredentialId
import com.passvault.core.domain.model.CredentialType
import com.passvault.core.domain.model.CustomField
import com.passvault.core.domain.model.CustomFieldId
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.testing.TestData
import com.passvault.feature.credential.presentation.CredentialViewModel
import com.passvault.feature.credential.ui.CredentialEditScreen
import java.awt.GraphicsEnvironment
import java.awt.IllegalComponentStateException
import java.awt.Rectangle
import java.awt.Robot
import java.awt.Window
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.nio.file.Files
import java.nio.file.LinkOption.NOFOLLOW_LINKS
import java.nio.file.Path
import java.nio.file.attribute.PosixFileAttributes
import java.nio.file.attribute.PosixFilePermissions
import java.util.IdentityHashMap
import java.util.Locale
import java.util.concurrent.Callable
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import javax.accessibility.AccessibleContext
import javax.accessibility.AccessibleRole
import javax.accessibility.AccessibleState
import javax.accessibility.AccessibleText
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
 * One PVA-007/031 integration complement to GUI02, not a rerun of its fake-persistence cases.
 * Real native input -> production form/VM -> encrypted repository/Room -> close/reopen -> fresh VM.
 * No Koin, clipboard, navigation host, recovery operation, fake crypto/database or screenshots.
 *
 * An absent display opt-in skips without opening application/native storage. Once opted in, the
 * mandatory home guard precedes all bootstrap/Compose/AWT setup, including on missing home input.
 * The caller separately admits a newly owned private test JVM/home/display and eventual cleanup;
 * directory metadata alone cannot establish original allocation authority. Never use a real home.
 */
@OptIn(ExperimentalComposeUiApi::class)
class CredentialEditorRoomIntegrationTest {
    @Test
    @Suppress("TooGenericExceptionCaught") // Teardown retains assertion/native failures as primary.
    fun `native capacity draft persists through page Save and a fresh Room database reopen`() {
        val display = System.getProperty("passvault.editor.syntheticDisplay")
        assumeTrue("Room editor check requires a separately admitted synthetic display", display != null)
        val home = AdmittedHome.acquire(requireNotNull(display))
        val editor = Editor(home)
        var failure: Throwable? = null
        try {
            editor.start()
            assertEquals(seedValues(), editor.fields(), "ROOM_EDITOR_SEED_49")
            assertTrue(editor.find(button("Add")).enabled)
            editor.click(button("Add"))
            editor.await("ROOM_EDITOR_ADD_DIALOG") { editor.dialogVisible() }
            editor.replaceText("Field Name", "roomadded")
            editor.replaceText("Value", "roomaddedvalue")
            editor.clickEvent(editor::dialogAdd, "OnCustomFieldAdded")
            editor.settle()
            assertFalse(editor.dialogVisible())
            val added = editor.fields()
            assertEquals(50, added.size)
            assertEquals(seedValues(), added.dropLast(1), "ROOM_EDITOR_ADD_CHANGED_SURVIVORS")
            assertEquals(50, added.map { it.id }.toSet().size)
            assertEquals(FieldValue(added.last().id, "roomadded", "roomaddedvalue", false), added.last())

            editor.clickEvent(button("Edit", last = true), "OnCustomFieldEditStarted")
            editor.replaceText("Field Name", "roomdraft")
            editor.replaceText("Value", "roomdraftvalue")
            assertFalse(editor.find(checkBox()).checked)
            editor.click(checkBox())
            editor.settle()
            assertTrue(editor.find(checkBox()).checked)
            onEdt {
                val state = editor.model.state.value
                val draft = assertNotNull(state.customFieldDrafts[CustomFieldId(added.last().id)])
                assertEquals("roomdraft", draft.name)
                assertEquals("roomdraftvalue", draft.value)
                assertTrue(draft.isSecret)
                assertTrue(state.hasUnsavedChanges)
            }
            // Deliberately do not save the row: the real page Save must consume its visible draft.
            editor.saveCloseAndReopen()
            val expected = seedValues() + FieldValue(added.last().id, "roomdraft", "roomdraftvalue", true)
            assertEquals(expected, editor.fields(), "ROOM_EDITOR_DISK_REOPEN_TUPLES")
            assertFalse(editor.find(button("Add")).enabled, "ROOM_EDITOR_REOPEN_CAPACITY")
            editor.scrollTo(named("roomdraft"))
            assertTrue(editor.visible(named("Password is hidden")), "ROOM_EDITOR_REOPEN_SECRET_MASKING")
            println("PASSVAULT_EDITOR_ROOM_RESULT\tfields=50\tsurvivors=49\troom_generations=2\tPASS")
        } catch (error: Throwable) {
            failure = error
            throw error
        } finally {
            editor.close(failure)
        }
    }

    private data class FieldValue(val id: String, val name: String, val value: String, val secret: Boolean)
    private fun seedValues() = List(49) { FieldValue("roomfield$it", "field$it", "value$it", false) }

    /** Metadata-only prerequisite. Fresh allocation/ancestor authority is the caller's separate admission. */
    private class AdmittedHome private constructor(
        private val path: Path,
        private val key: Any,
        private val user: String,
    ) {
        val databaseFile: Path get() = path.resolve(".passvault/vault.db")

        fun checkBound() {
            assertEquals(path.toString(), System.getProperty("passvault.editor.roomHome"), "ROOM_HOME_RECEIPT_CHANGED")
            assertEquals(path.toString(), System.getProperty("user.home"), "ROOM_HOME_PROPERTY_CHANGED")
            var ancestor = path.root
            path.forEach { part ->
                ancestor = ancestor.resolve(part)
                assertFalse(Files.isSymbolicLink(ancestor), "ROOM_HOME_LINK_COMPONENT")
            }
            assertEquals(path, path.toRealPath(), "ROOM_HOME_CANONICAL_CHANGED")
            val attributes = Files.readAttributes(path, PosixFileAttributes::class.java, NOFOLLOW_LINKS)
            assertTrue(attributes.isDirectory, "ROOM_HOME_NOT_DIRECTORY")
            assertEquals(key, attributes.fileKey(), "ROOM_HOME_IDENTITY_CHANGED")
            assertEquals(user, attributes.owner().name, "ROOM_HOME_OWNER")
            assertEquals(PRIVATE_MODE, attributes.permissions(), "ROOM_HOME_MODE")
        }

        companion object {
            private val PRIVATE_MODE = PosixFilePermissions.fromString("rwx------")

            fun acquire(display: String): AdmittedHome {
                assertEquals("Linux", System.getProperty("os.name"), "ROOM_EDITOR_LINUX_ONLY")
                assertTrue(Regex(":\\d{1,5}(?:\\.0)?").matches(display), "ROOM_EDITOR_DISPLAY_SYNTAX")
                assertEquals(display, System.getenv("DISPLAY"), "ROOM_EDITOR_DISPLAY_MISMATCH")
                assertTrue(System.getenv("WAYLAND_DISPLAY").isNullOrBlank(), "ROOM_EDITOR_X11_ONLY")
                assertEquals("en", Locale.getDefault().language, "ROOM_EDITOR_ENGLISH_ONLY")
                assertTrue(System.getProperty("compose.accessibility.enable") != "false", "ROOM_EDITOR_A11Y_DISABLED")
                assertEquals(null, System.getenv("COMPOSE_DISABLE_ACCESSIBILITY"), "ROOM_EDITOR_A11Y_DISABLED")
                val raw = requireNotNull(System.getProperty("passvault.editor.roomHome")) { "ROOM_HOME_REQUIRED" }
                assertTrue(raw.length in 1..2048 && Regex("/[A-Za-z0-9._/-]+").matches(raw), "ROOM_HOME_TEXT_BOUND")
                val path = Path.of(raw)
                assertTrue(path.isAbsolute && path.normalize() == path && path.toString() == raw, "ROOM_HOME_ABSOLUTE")
                assertEquals(raw, System.getProperty("user.home"), "ROOM_HOME_MUST_BE_PRIVATE_JVM_HOME")
                var ancestor = path.root
                path.forEach { part ->
                    ancestor = ancestor.resolve(part)
                    assertFalse(Files.isSymbolicLink(ancestor), "ROOM_HOME_LINK_COMPONENT")
                }
                val attributes = Files.readAttributes(path, PosixFileAttributes::class.java, NOFOLLOW_LINKS)
                val user = ProcessHandle.current().info().user().orElseThrow {
                    IllegalStateException("ROOM_PROCESS_USER")
                }
                val home = AdmittedHome(path, requireNotNull(attributes.fileKey()), user)
                home.checkBound()
                assertFalse(
                    Files.exists(path.resolve(".passvault"), NOFOLLOW_LINKS), "ROOM_HOME_PRIOR_APPLICATION_DATA",
                )
                Files.newDirectoryStream(path).use { assertFalse(it.iterator().hasNext(), "ROOM_HOME_NOT_FRESH_EMPTY") }
                home.checkBound()
                println("PASSVAULT_EDITOR_ROOM_HOME\t$path\t${attributes.fileKey()}")
                return home
            }
        }
    }

    private class RoomOwner(val bootstrap: VaultDatabaseBootstrap) {
        var vault: VaultRepositoryImpl? = null
        var credentials: CredentialRepositoryImpl? = null
        var folders: FolderRepositoryImpl? = null
        var generator: SecurePasswordGenerator? = null
        var lockAttempted = false
        var closeAttempted = false
        var closed = false
    }

    /** This one driver is fixed to the real form; locating uses read-only accessibility, never actions. */
    @Suppress("TooManyFunctions") // One driver owns native input, real Room generations and their teardown.
    private inner class Editor(private val home: AdmittedHome) {
        private val rooms = mutableListOf<RoomOwner>()
        private val stores = CopyOnWriteArrayList<ViewModelStore>()
        private val jobs = CopyOnWriteArrayList<Job>()
        private val events = mutableMapOf<String, Int>()
        private val frameRequest = mutableIntStateOf(0)
        private val frameAck = AtomicInteger(-1)
        private val pressedKeys = mutableSetOf<Int>()
        private val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(240)
        private var mousePressed = false
        private var window: ComposeWindow? = null
        private var robot: Robot? = null
        private var currentModel: CredentialViewModel? = null
        private var databaseKey: Any? = null
        val model: CredentialViewModel get() = requireNotNull(currentModel)

        fun start() {
            assertFalse(GraphicsEnvironment.isHeadless(), "ROOM_EDITOR_HEADLESS")
            val room = openRoom(seed = true)
            onEdt {
                currentModel = newModel(room)
                val current = ComposeWindow().also { window = it }
                current.title = "PassVault synthetic Room editor audit"
                current.setSize(800, 640)
                current.setLocation(48, 48)
                current.isResizable = false
                render()
                current.isVisible = true
                current.toFront()
                current.requestFocus()
            }
            robot = Robot().apply { autoDelay = 12 }
            settle()
            await("ROOM_EDITOR_ACTIVE_RENDERED_WINDOW") {
                onEdt {
                    val current = requireNotNull(window)
                    current.isActive && current.windowHandle != 0L && current.renderApi.toString() != "UNKNOWN"
                }
            }
            awaitLoaded()
            assertEquals("Synthetic Room editor audit", find(textField("Title")).text, "ROOM_EDITOR_EDITABLE_TREE")
        }

        private fun openRoom(seed: Boolean): RoomOwner {
            home.checkBound()
            val owner = RoomOwner(createDatabaseBootstrap()).also(rooms::add) // Register before any Room/native work.
            runBlocking {
                withTimeout(60_000) {
                    assertEquals(
                        VaultDatabaseBootstrapResult.Ready, owner.bootstrap.openAndVerify(), "ROOM_EDITOR_HEALTH",
                    )
                    val database = owner.bootstrap.database()
                    val crypto = DesktopCryptoEngine()
                    val vault = VaultRepositoryImpl(database.vaultMetadataDao(), crypto, VaultKeyHierarchy(crypto))
                    owner.vault = vault
                    owner.credentials = CredentialRepositoryImpl(
                        database.credentialDao(), database.folderDao(), database.tagDao(), database.attachmentDao(),
                        database.passwordHistoryDao(), crypto, vault,
                    )
                    owner.folders = FolderRepositoryImpl(database.folderDao(), crypto, vault)
                    owner.generator = SecurePasswordGenerator(crypto)
                    val password = SensitiveText.from("Synthetic Room editor password 9!")
                    try {
                        if (seed) vault.create(password).getOrThrow()
                        vault.unlock(password).getOrThrow()
                    } finally { password.clear() }
                    if (seed) {
                        val credential = TestData.credential(
                            id = "rendered-room", type = CredentialType.SecureNote,
                            title = "Synthetic Room editor audit", username = "", password = "",
                            url = "https://example.invalid",
                        ).copy(customFields = seedValues().map {
                            CustomField(CustomFieldId(it.id), it.name, SensitiveText.from(it.value), it.secret)
                        })
                        try { requireNotNull(owner.credentials).save(credential).getOrThrow() }
                        finally { credential.clearSensitiveValues() }
                    }
                }
            }
            val file = Files.readAttributes(home.databaseFile, PosixFileAttributes::class.java, NOFOLLOW_LINKS)
            assertTrue(file.isRegularFile, "ROOM_EDITOR_REAL_DATABASE_FILE")
            assertTrue(file.size() in 1..4_194_304, "ROOM_EDITOR_DATABASE_SIZE_BOUND")
            if (seed) databaseKey = requireNotNull(file.fileKey())
            else assertEquals(databaseKey, file.fileKey(), "ROOM_EDITOR_REOPEN_REPLACED_DATABASE_FILE")
            println("PASSVAULT_EDITOR_ROOM_OPEN\tgeneration=${rooms.size}\tseed=$seed")
            return owner
        }

        private fun newModel(room: RoomOwner): CredentialViewModel {
            val store = ViewModelStore().also(stores::add)
            val result = ViewModelProvider.create(store, viewModelFactory {
                initializer {
                    CredentialViewModel(
                        requireNotNull(room.credentials), requireNotNull(room.folders), requireNotNull(room.generator),
                    )
                }
            })[CredentialViewModel::class]
            jobs += requireNotNull(result.viewModelScope.coroutineContext[Job])
            result.loadCredential(CredentialId("rendered-room"))
            return result
        }

        private fun render() {
            val owner = model
            requireNotNull(window).setContent {
                val frame = frameRequest.intValue
                LaunchedEffect(frame) {
                    withFrameNanos { }
                    withFrameNanos { }
                    frameAck.set(frame)
                }
                key(owner) {
                    val state by owner.state.collectAsState()
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        PassVaultTheme(darkTheme = false) {
                            CredentialEditScreen(state, { event ->
                                val type = event.javaClass.simpleName
                                events[type] = (events[type] ?: 0) + 1
                                owner.onEvent(event)
                            }, Modifier.fillMaxSize())
                        }
                    }
                }
            }
        }

        private fun awaitLoaded() = await("ROOM_EDITOR_REAL_LOAD") {
            onEdt {
                val state = model.state.value
                state.isCredentialLoaded && !state.isLoading && !state.isLoadingFolders &&
                    !state.folderLoadFailed && state.errorMessage == null
            }
        }

        fun fields(): List<FieldValue> = onEdt {
            model.state.value.customFields.map {
                FieldValue(it.id.value, it.name, it.value.toStringUnsafe(), it.isSecret)
            }
        }

        fun saveCloseAndReopen() {
            clickEvent(button("Save", first = true), "OnSaveClick")
            await("ROOM_EDITOR_REAL_SAVE_COMPLETION") {
                onEdt { model.state.value.let { !it.isSaving && !it.hasUnsavedChanges && it.errorMessage == null } }
            }
            val detached = AtomicBoolean(false)
            onEdt {
                requireNotNull(window).setContent {
                    LaunchedEffect(Unit) {
                        withFrameNanos { }
                        withFrameNanos { }
                        detached.set(true)
                    }
                }
            }
            await("ROOM_EDITOR_OLD_FORM_NOT_DETACHED") { detached.get() }
            onEdt {
                stores.last().clear()
                currentModel = null
            }
            runBlocking { withTimeout(5_000) { jobs.forEach { it.join() } } }
            assertTrue(jobs.all { it.isCompleted }, "ROOM_EDITOR_OLD_VM_NOT_SETTLED")
            val previousDatabase = rooms.single().bootstrap.database()
            closeRoom(rooms.single())
            assertTrue(rooms.single().closed, "ROOM_EDITOR_OLD_ROOM_NOT_CLOSED")
            val next = openRoom(seed = false)
            assertFalse(previousDatabase === next.bootstrap.database(), "ROOM_EDITOR_REUSED_ROOM_INSTANCE")
            onEdt { currentModel = newModel(next); render() }
            settle()
            awaitLoaded()
            onEdt {
                assertTrue(model.state.value.customFieldDrafts.isEmpty(), "ROOM_EDITOR_REOPEN_HAS_DRAFTS")
                assertFalse(model.state.value.hasUnsavedChanges, "ROOM_EDITOR_REOPEN_DIRTY")
            }
        }

        // Single-attempt flags precede lock/close; preserve VM settlement and interruption/error ordering.
        @Suppress("CyclomaticComplexMethod", "TooGenericExceptionCaught")
        private fun closeRoom(owner: RoomOwner) {
            var interrupted = Thread.interrupted()
            var failure: Throwable? = null
            try {
                if (!owner.lockAttempted) {
                    owner.lockAttempted = true
                    try { runBlocking { withTimeout(5_000) { owner.vault?.lock()?.getOrThrow() } } }
                    catch (error: Throwable) {
                        if (error is InterruptedException || error.cause is InterruptedException) interrupted = true
                        failure = error
                    } finally { if (Thread.interrupted()) interrupted = true }
                }
                if (!owner.closeAttempted) {
                    owner.closeAttempted = true // A failed/uncertain close is never automatically retried.
                    try {
                        assertTrue(jobs.all { it.isCompleted }, "ROOM_EDITOR_CLOSE_WITH_LIVE_VM")
                        runBlocking { withTimeout(5_000) { owner.bootstrap.checkpointAndClose().getOrThrow() } }
                        owner.closed = true
                    } catch (error: Throwable) {
                        if (error is InterruptedException || error.cause is InterruptedException) interrupted = true
                        if (failure == null) failure = error else if (failure !== error) failure.addSuppressed(error)
                    } finally { if (Thread.interrupted()) interrupted = true }
                }
                failure?.let { throw it }
            } finally {
                if (Thread.interrupted()) interrupted = true
                if (interrupted) Thread.currentThread().interrupt()
            }
        }

        fun settle() {
            val requested = onEdt { ++frameRequest.intValue }
            await("ROOM_EDITOR_REAL_FRAME_PROGRESS") { frameAck.get() >= requested }
        }

        fun replaceText(label: String, value: String) {
            assertTrue(value.all { it in 'a'..'z' }, "ROOM_EDITOR_SYNTHETIC_ASCII_ONLY")
            val selector = textField(label)
            click(selector)
            await("ROOM_EDITOR_NATIVE_TEXT_FOCUS") { find(selector).focused }
            val owner = find(selector).window
            assertNativeWindow(owner)
            withKey(KeyEvent.VK_CONTROL) { withKey(KeyEvent.VK_A) { } }
            value.forEach {
                assertNativeWindow(owner)
                withKey(KeyEvent.getExtendedKeyCodeForChar(it.code)) { }
            }
            settle()
            assertEquals(value, find(selector).text, "ROOM_EDITOR_NATIVE_TEXT_RESULT")
        }

        // Release even after a partial press; rethrow the primary and attach secondary release failures.
        @Suppress("TooGenericExceptionCaught", "ThrowingExceptionFromFinally")
        private fun withKey(code: Int, action: () -> Unit) {
            val native = requireNotNull(robot)
            pressedKeys += code
            var failure: Throwable? = null
            try { native.keyPress(code); action() }
            catch (error: Throwable) { failure = error; throw error }
            finally {
                try { native.keyRelease(code); pressedKeys -= code }
                catch (error: Throwable) { if (failure == null) throw error else failure.addSuppressed(error) }
            }
        }

        fun clickEvent(selector: (List<Ax>) -> Ax?, event: String) {
            val before = onEdt { events[event] ?: 0 }
            click(selector)
            await("ROOM_EDITOR_NATIVE_CALLBACK_$event") { onEdt { (events[event] ?: 0) == before + 1 } }
        }

        // A release-only failure must fail the test; a failed press remains primary across release.
        @Suppress("TooGenericExceptionCaught", "ThrowingExceptionFromFinally")
        fun click(selector: (List<Ax>) -> Ax?) {
            val target = scrollTo(selector)
            assertTrue(target.enabled, "ROOM_EDITOR_TARGET_DISABLED")
            val native = requireNotNull(robot)
            val rectangle = checkedRectangle(target)
            native.mouseMove(rectangle.x + rectangle.width / 2, rectangle.y + rectangle.height / 2)
            val fresh = find(selector)
            assertTrue(target.context === fresh.context, "ROOM_EDITOR_TARGET_REPLACED")
            assertEquals(rectangle, checkedRectangle(fresh), "ROOM_EDITOR_TARGET_MOVED")
            assertTrue(fresh.enabled, "ROOM_EDITOR_TARGET_BECAME_DISABLED")
            mousePressed = true
            var failure: Throwable? = null
            try { native.mousePress(InputEvent.BUTTON1_DOWN_MASK) }
            catch (error: Throwable) { failure = error; throw error }
            finally {
                try { native.mouseRelease(InputEvent.BUTTON1_DOWN_MASK); mousePressed = false }
                catch (error: Throwable) { if (failure == null) throw error else failure.addSuppressed(error) }
            }
        }

        private fun checkedRectangle(target: Ax): Rectangle = onEdt {
            assertNativeWindow(target.window)
            val rectangle = assertNotNull(target.screen, "ROOM_EDITOR_MISSING_GEOMETRY")
            assertTrue(rectangle.width > 0 && rectangle.height > 0, "ROOM_EDITOR_EMPTY_GEOMETRY")
            assertTrue(clientBounds(target.window).contains(rectangle), "ROOM_EDITOR_TARGET_CLIPPED")
            Rectangle(rectangle)
        }

        fun scrollTo(selector: (List<Ax>) -> Ax?): Ax {
            repeat(65) {
                val target = find(selector)
                val rectangle = assertNotNull(target.screen, "ROOM_EDITOR_MISSING_SCROLL_GEOMETRY")
                val client = onEdt { clientBounds(target.window) }
                if (client.contains(rectangle) && rectangle.width > 0 && rectangle.height > 0) {
                    var previous = rectangle
                    var stableSince = System.nanoTime()
                    await("ROOM_EDITOR_GEOMETRY_NOT_STABLE") {
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
                assertTrue(rectangle.width in 1..client.width, "ROOM_EDITOR_HORIZONTAL_CLIPPING")
                assertNativeWindow(target.window)
                requireNotNull(robot).mouseMove(client.x + client.width / 2, client.y + client.height / 2)
                requireNotNull(robot).mouseWheel(if (rectangle.y < client.y) -4 else 4)
                settle()
            }
            fail("ROOM_EDITOR_SCROLL_TARGET_NOT_REACHED")
        }

        fun visible(selector: (List<Ax>) -> Ax?): Boolean = find(selector).let { target ->
            target.screen?.let {
                it.width > 0 && it.height > 0 && onEdt { clientBounds(target.window).contains(it) }
            } == true
        }

        fun find(selector: (List<Ax>) -> Ax?): Ax {
            var result: Ax? = null
            await("ROOM_EDITOR_UNIQUE_ACCESSIBLE_TARGET") { result = onEdt { selector(snapshot()) }; result != null }
            return requireNotNull(result)
        }

        fun dialogVisible(): Boolean = onEdt { snapshot().any { it.name == "Add Custom Field" } }

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
                assertTrue(depth <= 40 && result.size < 2048, "ROOM_EDITOR_ACCESSIBLE_TREE_BOUND")
                val component = context.accessibleComponent
                val screen = try {
                    val position = component?.locationOnScreen
                    val size = component?.size
                    if (position != null && size != null) Rectangle(position, size) else null
                } catch (_: IllegalComponentStateException) { null }
                // Only editable values are consumed; other selectors use accessibility metadata.
                val editableText = context.accessibleEditableText
                val text = editableText?.let { content ->
                    val length = content.charCount
                    assertTrue(length in 0..256, "ROOM_EDITOR_SYNTHETIC_TEXT_BOUND")
                    buildString { repeat(length) { append(content.getAtIndex(AccessibleText.CHARACTER, it)) } }
                }
                val index = result.size
                val states = context.accessibleStateSet
                result += Ax(
                    index, parent, context, owner, context.accessibleRole, context.accessibleName, text,
                    editableText != null, component?.isEnabled == true,
                    states.contains(AccessibleState.FOCUSED), states.contains(AccessibleState.CHECKED), screen,
                )
                val count = context.accessibleChildrenCount
                assertTrue(count in 0..2048, "ROOM_EDITOR_ACCESSIBLE_CHILD_BOUND")
                repeat(count) { visit(context.getAccessibleChild(it)?.accessibleContext, owner, index, depth + 1) }
            }
            ownedWindows().filter { it.isShowing }.forEach { visit(it.accessibleContext, it, null, 0) }
            return result
        }

        private fun ownedWindows(): List<Window> {
            val result = mutableListOf<Window>()
            fun visit(current: Window) {
                assertTrue(result.size < 8 && current !in result, "ROOM_EDITOR_OWNED_WINDOW_BOUND")
                result += current
                current.ownedWindows.forEach(::visit)
            }
            window?.let(::visit)
            return result
        }

        private fun clientBounds(owner: Window): Rectangle {
            val insets = owner.insets
            val origin = owner.locationOnScreen
            return Rectangle(origin.x + insets.left, origin.y + insets.top,
                owner.width - insets.left - insets.right, owner.height - insets.top - insets.bottom).also {
                assertTrue(it.width in 200..1000 && it.height in 200..800, "ROOM_EDITOR_CLIENT_GEOMETRY")
                assertTrue(owner.graphicsConfiguration.bounds.contains(it), "ROOM_EDITOR_CLIENT_OUTSIDE_DISPLAY")
            }
        }

        private fun assertNativeWindow(owner: Window) = onEdt {
            assertTrue(owner in ownedWindows() && owner.isShowing && owner.isActive, "ROOM_EDITOR_NATIVE_WINDOW")
            clientBounds(owner)
            Unit
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
            var failure: Throwable? = null
            // A failed release, including an assertion, must not skip the remaining owned resources.
            @Suppress("TooGenericExceptionCaught")
            fun release(block: () -> Unit) {
                try { block() } catch (error: Throwable) {
                    if (error is InterruptedException || error.cause is InterruptedException) interrupted = true
                    if (failure == null) failure = error else requireNotNull(failure).addSuppressed(error)
                } finally { if (Thread.interrupted()) interrupted = true }
            }
            try {
                pressedKeys.toList().forEach { code -> release {
                    requireNotNull(robot).keyRelease(code)
                    pressedKeys -= code
                } }
                if (mousePressed) release {
                    requireNotNull(robot).mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
                    mousePressed = false
                }
                var closing = emptyList<Window>()
                release { closing = onEdt { ownedWindows().asReversed() } }
                closing.forEach { owner -> release { onEdt { owner.dispose() } } }
                release { onEdt { window?.takeIf { it !in closing }?.dispose() } }
                stores.forEach { owner -> release { onEdt { owner.clear() } } }
                jobs.forEach { job -> release { job.cancel() } }
                jobs.forEach { job -> release { runBlocking { withTimeout(3_000) { job.join() } } } }
                rooms.asReversed().forEach { owner -> release { closeRoom(owner) } }
                release { assertTrue(rooms.all { it.closed }, "ROOM_EDITOR_CLEANUP_DATABASE_UNSETTLED") }
                release { onEdt {
                    assertTrue(ownedWindows().none { it.isDisplayable }, "ROOM_EDITOR_CLEANUP_WINDOW_LIVE")
                } }
                release { onEdt { currentModel = null; events.clear() } }
                // No filesystem deletion. The caller owns the exact generated home/bundle after worker settlement.
                println(
                    "PASSVAULT_EDITOR_ROOM_CLEANUP\trooms=${rooms.size}\tclosed=${rooms.count { it.closed }}" +
                        "\tvm_jobs_settled=${jobs.all { it.isCompleted }}\tinterrupted=$interrupted" +
                        "\terror=${failure != null}",
                )
            } finally {
                if (Thread.interrupted()) interrupted = true
                if (interrupted) Thread.currentThread().interrupt()
            }
            failure?.let { if (primary == null) throw it else primary.addSuppressed(it) }
            // EDT/native/Room stalls need external bounded settlement; timeouts are not arbitrary-worker proof.
        }
    }

    private data class Ax(
        val index: Int, val parent: Int?, val context: AccessibleContext, val window: Window,
        val role: AccessibleRole, val name: String?, val text: String?, val editable: Boolean,
        val enabled: Boolean, val focused: Boolean, val checked: Boolean, val screen: Rectangle?,
    ) {
        fun isNamedField(label: String): Boolean = editable && (name == label || name?.startsWith("$label, ") == true)
    }

    private fun unique(nodes: List<Ax>): Ax? {
        assertTrue(nodes.size <= 1, "ROOM_EDITOR_AMBIGUOUS_ACCESSIBLE_TARGET")
        return nodes.singleOrNull()
    }
    private fun textField(label: String): (List<Ax>) -> Ax? = { nodes ->
        unique(nodes.filter { it.isNamedField(label) })
    }
    private fun named(name: String): (List<Ax>) -> Ax? = { nodes -> unique(nodes.filter { it.name == name }) }
    private fun checkBox(): (List<Ax>) -> Ax? = { nodes ->
        unique(nodes.filter { it.role == AccessibleRole.CHECK_BOX && it.name == "Secret field" })
    }
    private fun button(name: String, first: Boolean = false, last: Boolean = false): (List<Ax>) -> Ax? = { nodes ->
        val matches = nodes.filter { it.role == AccessibleRole.PUSH_BUTTON && it.name == name }
        if (matches.isEmpty()) null else {
            val expected = when {
                name == "Edit" && last -> 50..50
                name == "Save" && first -> 2..2 // Pending row Save and page Save, page is topmost.
                else -> 1..1
            }
            assertTrue(matches.size in expected, "ROOM_EDITOR_BUTTON_CARDINALITY")
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
            task.cancel(false)
            throw error
        }
    }
}
