package com.passvault.desktop

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.awt.EventQueue
import java.awt.GraphicsEnvironment
import java.awt.event.WindowEvent
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.LinkOption.NOFOLLOW_LINKS
import java.nio.file.Path
import java.nio.file.StandardOpenOption.APPEND
import java.nio.file.StandardOpenOption.CREATE_NEW
import java.nio.file.StandardOpenOption.WRITE
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.Assume.assumeTrue

/**
 * One real-Compose/process-boundary case, not a vault/provider cleanup test.
 * Root must admit an isolated X session, fresh runtime/evidence directories and
 * the full Test runtime classpath. The outer owner removes runtime/cache outputs
 * after worker settlement; compact traces/logs stay outside that cleanup root.
 */
class DesktopApplicationLifecycleIntegrationTest {
    @Test
    fun composeExitReturnsThroughCleanupBeforeOwningJvmExit() {
        val display = System.getProperty(PREFIX + "syntheticDisplay")
        assumeTrue("Lifecycle integration requires an admitted synthetic X session", display != null)
        assertTrue(Regex(":\\d{1,5}(?:\\.0)?").matches(requireNotNull(display)))
        assertEquals(display, System.getenv("DISPLAY"))
        assertTrue(System.getenv("WAYLAND_DISPLAY").isNullOrBlank())
        assertFalse(GraphicsEnvironment.isHeadless())
        assertEquals(17, Runtime.version().feature())
        val runtime = freshDirectory("runtimeDir")
        val evidence = freshDirectory("evidenceDir")
        assertFalse(runtime.startsWith(evidence) || evidence.startsWith(runtime))
        val classpath = requireNotNull(System.getProperty(PREFIX + "childClasspath")).also {
            require(it.isNotBlank()) { "Inject the actual Test runtime classpath, not java.class.path" }
        }
        val authority = requireNotNull(System.getenv("XAUTHORITY"))
        assertTrue(Path.of(authority).isAbsolute && Files.isRegularFile(Path.of(authority), NOFOLLOW_LINKS))
        for (mode in listOf("default", "production")) {
            val (exit, events) = runChild(mode, runtime, evidence, classpath, display, authority)
            val expected = listOf("WINDOW_READY", "CLOSE_REQUEST")
            assertEquals(if (mode == "production") CONTINUATION_EXIT else 0, exit, mode)
            assertEquals(
                if (mode == "production") expected + listOf("CALLER_CLEANUP", "CONTINUATION") else expected,
                events,
                mode,
            )
        }
    }

    private fun freshDirectory(property: String): Path =
        Path.of(requireNotNull(System.getProperty(PREFIX + property))).also { path ->
            assertTrue(path.isAbsolute && path.normalize() == path && Files.isDirectory(path, NOFOLLOW_LINKS))
            Files.newDirectoryStream(path).use { assertFalse(it.iterator().hasNext()) }
        }

    @Suppress("TooGenericExceptionCaught") // Preserve primary assertions/interruption across cleanup failure.
    private fun runChild(
        mode: String,
        runtime: Path,
        evidence: Path,
        classpath: String,
        display: String,
        authority: String,
    ): Pair<Int, List<String>> {
        val home = Files.createDirectory(runtime.resolve(mode))
        val tmp = Files.createDirectory(home.resolve("tmp"))
        val trace = Files.createFile(evidence.resolve("$mode.events"))
        val log = Files.createFile(evidence.resolve("$mode.log"))
        val environment = mutableMapOf(
            "PATH" to "/usr/bin:/bin", "LANG" to "C.UTF-8", "LC_ALL" to "C.UTF-8", "TZ" to "UTC",
            "HOME" to home.toString(), "TMPDIR" to tmp.toString(), "TMP" to tmp.toString(), "TEMP" to tmp.toString(),
            "DISPLAY" to display, "XAUTHORITY" to authority,
        )
        for (kind in listOf("CACHE", "CONFIG", "DATA", "STATE")) {
            environment["XDG_" + kind + "_HOME"] =
                Files.createDirectory(home.resolve("xdg-" + kind.lowercase())).toString()
        }
        val builder = ProcessBuilder(
            Path.of(System.getProperty("java.home"), "bin", "java").toString(),
            "-Xmx256m", "-XX:ActiveProcessorCount=1", "-XX:-UsePerfData", "-Dfile.encoding=UTF-8",
            "-XX:-CreateCoredumpOnCrash", "-XX:ErrorFile=$home/hs_err_pid%p.log",
            "-Djava.awt.headless=false", "-Duser.home=$home", "-Djava.io.tmpdir=$tmp",
            "-Djna.tmpdir=$tmp", "-Dorg.sqlite.tmpdir=$tmp", "-Dsun.java2d.xrender=true",
            "-Dawt.useSystemAAFontSettings=on", "-Dswing.aatext=true",
            "-cp", classpath, DesktopApplicationLifecycleProbe::class.java.name, mode, trace.toString(),
        ).directory(home.toFile()).redirectErrorStream(true).redirectOutput(log.toFile())
        builder.environment().clear()
        builder.environment().putAll(environment)
        var child: Process? = null
        var primaryFailure: Throwable? = null
        try {
            val running = builder.start().also { child = it }
            running.outputStream.close()
            val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(CHILD_SECONDS)
            while (!running.waitFor(100, TimeUnit.MILLISECONDS)) {
                assertTrue(System.nanoTime() < deadline, "$mode child deadline; see $log")
                assertTrue(Files.size(log) <= MAX_LOG_BYTES, "$mode child log bound")
            }
            assertTrue(Files.size(log) <= MAX_LOG_BYTES, "$mode final log bound")
            assertTrue(Files.size(trace) <= 4096, "$mode trace bound")
            return running.exitValue() to Files.readAllLines(trace)
        } catch (failure: Throwable) {
            primaryFailure = failure
            if (failure is InterruptedException) Thread.currentThread().interrupt()
            throw failure
        } finally {
            child?.let { running ->
                val interrupted = Thread.interrupted()
                try {
                    if (running.isAlive) running.destroyForcibly() // Only this captured child, never the Test worker.
                    check(running.waitFor(5, TimeUnit.SECONDS)) {
                        "$mode child did not settle; runtime cleanup BLOCKED"
                    }
                    Files.writeString(
                        evidence.resolve("$mode.exit"), running.exitValue().toString() + "\n", CREATE_NEW, WRITE,
                    )
                } catch (cleanupFailure: Throwable) {
                    if (cleanupFailure is InterruptedException) Thread.currentThread().interrupt()
                    val report = IllegalStateException(
                        "$mode child cleanup/evidence failed; runtime cleanup BLOCKED", cleanupFailure,
                    )
                    val primary = primaryFailure
                    if (primary == null) throw report
                    primary.addSuppressed(report)
                } finally {
                    if (interrupted) Thread.currentThread().interrupt()
                }
            }
        }
    }

    private companion object {
        const val PREFIX = "passvault.lifecycle."
        const val CHILD_SECONDS = 30L
        const val MAX_LOG_BYTES = 256L * 1024
    }
}

/** Launched only in a fresh JVM: real System.exit must not terminate Gradle's Test worker. */
internal object DesktopApplicationLifecycleProbe {
    @JvmStatic
    fun main(args: Array<String>) {
        require(args.size == 2 && args[0] in setOf("default", "production"))
        val trace = Path.of(args[1])
        fun record(event: String) {
            FileChannel.open(trace, WRITE, APPEND, NOFOLLOW_LINKS).use { channel ->
                val bytes = ByteBuffer.wrap((event + "\n").toByteArray(Charsets.UTF_8))
                while (bytes.hasRemaining()) check(channel.write(bytes) > 0)
                channel.force(true)
            }
        }
        val content: @Composable ApplicationScope.() -> Unit = {
            Window(
                title = "PassVault synthetic lifecycle only",
                onCloseRequest = {
                    record("CLOSE_REQUEST")
                    exitApplication()
                },
            ) {
                BasicText("Synthetic lifecycle boundary; no vault or clipboard")
                LaunchedEffect(Unit) {
                    withFrameNanos { }
                    withFrameNanos { }
                    check(window.isDisplayable && window.isShowing)
                    record("WINDOW_READY")
                    EventQueue.invokeLater {
                        window.dispatchEvent(WindowEvent(window, WindowEvent.WINDOW_CLOSING))
                    }
                }
            }
        }
        try {
            if (args[0] == "production") {
                runDesktopApplicationLoop(content)
            } else {
                application(content = content) // Historical real default: exit(0), without caller finally.
            }
        } finally {
            record("CALLER_CLEANUP") // Synthetic bounded cleanup marker, not a real provider or shutdown hook.
        }
        record("CONTINUATION")
        exitProcess(CONTINUATION_EXIT)
    }
}

private const val CONTINUATION_EXIT = 23
