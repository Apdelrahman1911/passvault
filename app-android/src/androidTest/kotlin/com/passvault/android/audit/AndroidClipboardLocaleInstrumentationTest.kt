package com.passvault.android.audit

import android.app.Activity
import android.app.Application
import android.app.Instrumentation
import android.app.UiAutomation
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.LocaleList
import android.os.Looper
import android.os.Process
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import com.passvault.android.BuildConfig
import com.passvault.android.MainActivity
import com.passvault.android.security.AndroidClipboardService
import com.passvault.android.security.androidBiometricPromptText
import com.passvault.core.domain.repository.AppSettingsStore
import com.passvault.core.domain.repository.LanguagePreference
import com.passvault.feature.settings.presentation.SettingsViewModel
import com.passvault.feature.settings.presentation.SettingsViewModel.AppLanguage
import com.passvault.feature.settings.presentation.SettingsViewModel.SettingsEvent
import com.passvault.shared.platform.currentNativeBiometricPromptStrings
import java.util.IdentityHashMap
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.koin.core.context.GlobalContext

/**
 * Two opt-in, real-framework cases for PVA-009/PVA-030; not a general test runner.
 *
 * BEFORE instrumentation launch, the external owner must independently admit a wholly synthetic
 * Android device/user/clipboard AND fresh isolated Debug app data, with bounded owned-process/device
 * cleanup already installed. Application/Koin startup can precede these in-test guards. An argument,
 * package suffix, or successful result cannot establish isolation or external worker settlement.
 * No prior clipboard is read/restored and no device-wide setting is changed. Real app startup must
 * access only the externally admitted synthetic storage; this fixture cannot prove it was fresh.
 *
 * Clipboard evidence is API29+ only: two unreadable endpoint reads across a real background wait and Main foreground
 * retry, not proof that the private timer rather than lifecycle clear caused pending cleanup. Null
 * primaryClip cannot by itself distinguish access denial from OS clearing. Replacement is written
 * by this same synthetic UID, not an external-app/revocation/history/OEM claim.
 * Locale evidence uses test-only per-Activity framework configuration and actual recreation. It is
 * not device-global locale propagation, Compose RTL geometry, displayed biometric UI, or hardware
 * evidence. The default-input native prompt helper is observed; no prompt/enrollment is requested.
 */
class AndroidClipboardLocaleInstrumentationTest : Instrumentation() {
    private val handler = Handler(Looper.getMainLooper())
    private val evidence = Bundle()
    private val captured = IdentityHashMap<MainActivity, Boolean>() // Accessed only on the main thread.
    private val knownLabels = mutableSetOf<String>()
    private var application: Application? = null
    private var clipboard: AndroidClipboardService? = null
    private var originalLocales: LocaleList? = null
    private var originalLocale: Locale? = null
    private var originalLocaleIndex = -1
    private var originalDisplayLocale: Locale? = null
    private var originalFormatLocale: Locale? = null
    private var defaultsTouched = false
    private var interruptionObserved = false
    private var terminalProblem: Throwable? = null
    private val failurePhases = arrayListOf<String>()
    private var clipboardTouched = false
    private var startedCases = 0
    private var passedCases = 0
    private var stage = "ARGUMENT_ADMISSION"
    @Volatile private var overrideLocale: Locale? = null
    @Volatile private var current: MainActivity? = null
    @Volatile private var resumed: Activity? = null
    @Volatile private var stopped: Activity? = null

    private val cases = listOf(
        AuditCase("realClipboardOwnershipAndForegroundRetry", ::realClipboardOwnershipAndForegroundRetry),
        AuditCase("frameworkLocaleRecreationAndExplicitToSystem", ::frameworkLocaleRecreationAndExplicitToSystem),
    )

    override fun onCreate(arguments: Bundle?) {
        super.onCreate(arguments)
        val allowed = setOf(ISOLATION_ARGUMENT, "additionalTestOutputDir")
        if (arguments == null || arguments.getString(ISOLATION_ARGUMENT) != ISOLATION_VALUE ||
            arguments.keySet().any { it !in allowed } ||
            (arguments.containsKey("additionalTestOutputDir") &&
                arguments.getString("additionalTestOutputDir")?.let { it.length <= 4096 } != true)
        ) {
            finishFailure("Missing isolation acknowledgement or unsupported fixed-selection arguments")
            return
        }
        // additionalTestOutputDir is inert AGP metadata: never opened or used for writes.
        start()
    }

    override fun onStart() {
        super.onStart()
        var problem: Throwable? = null
        var failureStage: String? = null
        try {
            stage = "DEBUG_TARGET_ADMISSION"
            if (Thread.currentThread().isInterrupted) throw InterruptedException("Interrupted before setup")
            requireEvidence(Build.VERSION.SDK_INT >= 29, "Requires API29+ for this fixed selection")
            requireEvidence(targetContext.packageName == DEBUG_PACKAGE, "Wrong target package")
            requireEvidence(BuildConfig.DEBUG && BuildConfig.BUILD_TYPE == "debug" &&
                !BuildConfig.STORE_SCREENSHOT_MODE, "Only the ordinary Debug variant is permitted")
            onMain {
                originalLocales = LocaleList.getDefault()
                originalLocale = Locale.getDefault()
                originalDisplayLocale = Locale.getDefault(Locale.Category.DISPLAY)
                originalFormatLocale = Locale.getDefault(Locale.Category.FORMAT)
                originalLocaleIndex = requireNotNull(originalLocales).indexOf(requireNotNull(originalLocale))
                requireEvidence(originalLocaleIndex >= 0, "Original default is absent from the process locale list")
                // A process-local initial control, before the first Main/provider composition only.
                overrideLocale = Locale.ENGLISH
                defaultsTouched = true
                LocaleList.setDefault(LocaleList(Locale.ENGLISH))
                Locale.setDefault(Locale.ENGLISH)
                application = (targetContext.applicationContext as Application).also {
                    it.registerActivityLifecycleCallbacks(callbacks)
                }
            }
            for (case in cases) {
                startedCases++
                sendStatus(1, caseStatus(case))
                try {
                    runBlocking { withTimeout(90_000) { case.body() } }
                } catch (error: Throwable) {
                    // Unsatisfied runtime conditions are errors, never passes or fabricated skips.
                    sendStatus(if (error is AssertionError) -2 else -1, caseStatus(case).apply {
                        putString("stack", "$stage: ${error.javaClass.simpleName}")
                    })
                    throw error
                }
                passedCases++
                sendStatus(0, caseStatus(case))
            }
        } catch (error: Throwable) {
            recordFailure(stage, error)
            problem = error
            failureStage = stage
        } finally {
            try {
                runBlocking { withTimeout(30_000) { cleanup() } }
                evidence.putBoolean("passvault.inProcessCleanup", true)
            } catch (cleanupError: Throwable) {
                recordFailure("CLEANUP_AGGREGATE", cleanupError)
                if (problem == null) failureStage = stage
                problem = mergeFailure(problem, cleanupError)
                evidence.putBoolean("passvault.inProcessCleanup", false)
            }
        }
        if (Thread.currentThread().isInterrupted) {
            val error = InterruptedException("Interrupted before terminal receipt")
            recordFailure("TERMINAL_INTERRUPT", error)
            problem = mergeFailure(problem, error)
        }
        terminalProblem = problem
        try {
            if (problem != null || interruptionObserved || startedCases != 2 || passedCases != 2) {
                finishFailure("${failureStage ?: stage}: ${problem?.javaClass?.simpleName ?: "IncompleteInventory"}")
            } else {
                finish(Activity.RESULT_OK, summary().apply { putString("stream", "\nOK (2 fixed Android cases)\n") })
            }
        } finally {
            if (interruptionObserved) Thread.currentThread().interrupt()
        }
    }

    override fun newActivity(loader: ClassLoader, className: String, intent: Intent): Activity =
        super.newActivity(loader, className, intent).also { activity ->
            // Public framework hook, before attach/theme/resource access, not callActivityOnCreate.
            if (activity is MainActivity) overrideLocale?.let { locale ->
                activity.applyOverrideConfiguration(Configuration().apply { setLocales(LocaleList(locale)) })
            }
        }

    private suspend fun realClipboardOwnershipAndForegroundRetry() {
        stage = "CLIPBOARD_INITIAL_MAIN"
        onMain { targetContext.startActivity(mainIntent()) }
        awaitCondition("Initial real Main did not gain focus", precondition = true) { focused(current) }
        observeInitialReadyOnboarding() // Read-only UI witness; never calls bootstrap/open/retry itself.
        clipboard = GlobalContext.get().get<AndroidClipboardService>()
        val service = requireNotNull(clipboard)
        val manager = clipboardManager()
        stage = "CLIPBOARD_SAME_TEXT_REPLACEMENT"
        copyOwned(service, 60_000)
        knownLabels += REPLACEMENT_LABEL
        manager.setPrimaryClip(ClipData.newPlainText(REPLACEMENT_LABEL, SYNTHETIC_TEXT))
        service.clear() // Explicit ownership control; do not claim it exercises the expiry timer.
        assertClip(manager.primaryClip, REPLACEMENT_LABEL)
        assertAudit(!service.containsSensitive(), "Replacement did not retire only our ownership")
        manager.clearPrimaryClip() // Positively identified synthetic replacement, not any prior user clip.

        stage = "CLIPBOARD_BACKGROUND_INTERVAL"
        val token = copyOwned(service, 5_000)
        val activity = requireNotNull(current)
        onMain { requireEvidence(activity.moveTaskToBack(true), "Task could not move to background") }
        awaitCondition("No real stopped/unfocused interval", precondition = true) {
            stopped === activity && resumed !== activity && onMain { !activity.hasWindowFocus() }
        }
        requireEvidence(manager.primaryClip == null, "Runtime still exposes the background clip")
        val intervalStartedAt = SystemClock.elapsedRealtime()
        delay(5_500) // Starts after stopped/unfocused and the first null sample, not at copy time.
        requireEvidence(stopped === activity && resumed !== activity && onMain { !activity.hasWindowFocus() } &&
            manager.primaryClip == null, "Second stopped/unfocused/unreadable endpoint was not observed")
        val retained = service.containsSensitive()
        evidence.putLong("pva009.backgroundEndpointIntervalMillis", SystemClock.elapsedRealtime() - intervalStartedAt)
        stage = "CLIPBOARD_REAL_FOREGROUND_RETRY"
        foreground(activity)
        if (!retained) {
            assertAudit(manager.primaryClip?.description?.label?.toString() != token,
                "Ownership was lost while the original synthetic clip survived")
            throw EvidenceUnavailable("No retained owned/unreadable interval; early or OS clear is possible")
        }
        awaitCondition("Actual Main foreground did not retire pending cleanup") { !service.containsSensitive() }
        assertAudit(manager.primaryClip == null, "Foreground cleanup did not leave the synthetic clipboard empty")
        evidence.putBoolean("pva009.unavailableEndpointsThenForegroundRetirement", true)
    }

    private suspend fun frameworkLocaleRecreationAndExplicitToSystem() {
        stage = "LOCALE_ALREADY_RENDERED_APP"
        // The first case observed real ready-only onboarding before any background/recreation/VM lookup.
        val settings = onMain { GlobalContext.get().get<SettingsViewModel>() }
        val pid = Process.myPid()
        changeLanguage(settings, AppLanguage.SYSTEM, "en", "en")
        recreateMain(Locale.forLanguageTag("ar"))
        assertLanguage(settings, AppLanguage.SYSTEM, "ar", "ar")
        changeLanguage(settings, AppLanguage.ENGLISH, "ar", "en")
        changeLanguage(settings, AppLanguage.SYSTEM, "ar", "ar")
        recreateMain(Locale.ENGLISH)
        assertLanguage(settings, AppLanguage.SYSTEM, "en", "en")
        changeLanguage(settings, AppLanguage.ARABIC, "en", "ar")
        changeLanguage(settings, AppLanguage.SYSTEM, "en", "en")
        assertAudit(Process.myPid() == pid, "Recreation did not retain the instrumentation process")
        evidence.putInt("pva030.realMainRecreations", 2)
        evidence.putString("pva030.configurationSource", "TEST_ONLY_ACTIVITY_OVERRIDE_NOT_DEVICE_SETTING")
    }

    private suspend fun changeLanguage(settings: SettingsViewModel, choice: AppLanguage,
        framework: String, effective: String) {
        stage = "LOCALE_CHOICE_${choice.name}_${framework}_$effective"
        onMain { settings.onEvent(SettingsEvent.OnLanguageChanged(choice)) }
        val expected = LanguagePreference.valueOf(choice.name)
        awaitCondition("Expected synthetic preference readback was not observed") {
            GlobalContext.get().get<AppSettingsStore>().load().getOrThrow().language == expected &&
                settings.state.value.errorMessage == null
        }
        assertLanguage(settings, choice, framework, effective)
    }

    private suspend fun assertLanguage(settings: SettingsViewModel, choice: AppLanguage,
        framework: String, effective: String) {
        stage = "LOCALE_OBSERVE_${choice.name}_${framework}_$effective"
        val expected = if (effective == "ar") {
            listOf("فعّل الفتح باستخدام المقاييس الحيوية لهذه الخزنة", "افتح PassVault", "إلغاء")
        } else listOf("Enable biometric unlock for this vault", "Unlock PassVault", "Cancel")
        awaitCondition("Production locale/default prompt inputs did not follow framework configuration") {
            onMain {
                val activity = requireNotNull(current)
                val strings = currentNativeBiometricPromptStrings()
                val enroll = androidBiometricPromptText(enrolling = true) // No supplied strings/mock publisher.
                val unlock = androidBiometricPromptText(enrolling = false)
                activity.resources.configuration.locales[0].language == framework &&
                    settings.state.value.language == choice && Locale.getDefault().language == effective &&
                    LocaleList.getDefault()[0].language == effective && strings.languageTag == effective &&
                    listOf(strings.enrollmentReason, strings.unlockReason, strings.cancel) == expected &&
                    enroll.reason == expected[0] && unlock.reason == expected[1] &&
                    enroll.cancel == expected[2] && unlock.cancel == expected[2]
            }
        }
    }

    private suspend fun recreateMain(locale: Locale) {
        stage = "LOCALE_RECREATE_${locale.language}"
        val previous = requireNotNull(current)
        onMain { overrideLocale = locale; previous.recreate() }
        awaitCondition("Real Main recreation did not destroy/create/focus distinct Activities") {
            current !== previous && focused(current) && onMain { previous.isDestroyed }
        }
    }

    private suspend fun copyOwned(service: AndroidClipboardService, timeoutMs: Long): String {
        requireEvidence(focused(current), "Synthetic copy requires actual Main focus")
        clipboardTouched = true
        service.copySensitive(SYNTHETIC_TEXT, timeoutMs)
        val clip = clipboardManager().primaryClip
        val token = clip?.description?.label?.toString()
        requireEvidence(token?.matches(Regex("PassVault:secret:[0-9a-f-]{36}")) == true,
            "Actual provider did not expose the newly copied ownership token")
        knownLabels += requireNotNull(token)
        assertClip(clip, token)
        if (Build.VERSION.SDK_INT >= 33) {
            assertAudit(clip?.description?.extras?.getBoolean(ClipDescription.EXTRA_IS_SENSITIVE) == true,
                "API33+ synthetic sensitive marker is missing")
        }
        return token
    }

    private fun assertClip(clip: ClipData?, label: String) {
        assertAudit(clip?.description?.label?.toString() == label, "Unexpected synthetic clipboard owner")
        assertAudit(clip != null && clip.itemCount == 1 && clip.getItemAt(0).text?.toString() == SYNTHETIC_TEXT,
            "Synthetic clipboard tuple differs") // Never coerce/read URI or Intent contents.
    }

    private suspend fun foreground(activity: MainActivity) {
        requireEvidence(onMain { !activity.isDestroyed && !activity.isFinishing }, "Captured Main no longer usable")
        if (!focused(activity)) onMain { targetContext.startActivity(mainIntent()) }
        awaitCondition("Captured Main did not really resume/focus", precondition = true) {
            current === activity && focused(activity)
        }
    }

    private fun focused(activity: MainActivity?): Boolean = activity != null && resumed === activity &&
        onMain { !activity.isDestroyed && !activity.isFinishing && activity.hasWindowFocus() }

    @Suppress("DEPRECATION") // recycle() releases API29-32 nodes; newer frameworks make it a no-op.
    private suspend fun observeInitialReadyOnboarding() {
        stage = "INITIAL_READY_ONBOARDING_AX"
        evidence.putString("passvault.axConnectionSettlement", "INSTRUMENTATION_FINISH_AND_OUTER_OWNER_REQUIRED")
        val automation = requireNotNull(getUiAutomation(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES))
        // Instrumentation owns this connection. No hidden disconnect, listener, serviceInfo, input or privilege API.
        awaitCondition("Ready-only English onboarding not observed", precondition = true) {
            if (!focused(current)) return@awaitCondition false
            val root = automation.rootInActiveWindow ?: return@awaitCondition false
            var nodes = 0
            var headings = 0
            var buttons = 0
            val window = root.windowId
            fun visit(node: AccessibilityNodeInfo, depth: Int) {
                var primary: Throwable? = null
                try {
                    requireEvidence(node.packageName?.toString() == DEBUG_PACKAGE && node.windowId == window,
                        "Readiness node is outside the Debug window") // Before any text or child traversal.
                    requireEvidence(++nodes <= 128 && depth <= 20 && node.childCount <= 32,
                        "Owned readiness tree exceeded its fixed bound")
                    if (node.isVisibleToUser && node.text?.toString() == "Welcome to PassVault" && node.isHeading) {
                        headings++
                    }
                    if (node.isVisibleToUser && node.text?.toString() == "Get started" &&
                        node.isClickable && node.isEnabled) buttons++
                    for (index in 0 until node.childCount) node.getChild(index)?.let { visit(it, depth + 1) }
                } catch (error: Throwable) { primary = error; throw error }
                finally {
                    try { node.recycle() } catch (error: Throwable) {
                        if (primary == null) throw error else mergeFailure(primary, error)
                    }
                }
            }
            visit(root, 0)
            requireEvidence(headings <= 1 && buttons <= 1, "Ambiguous ready-only onboarding nodes")
            headings == 1 && buttons == 1 && focused(current)
        }
    }

    private suspend fun cleanup() {
        stage = "IN_PROCESS_CLEANUP"
        var problem: Throwable? = null
        try {
            if (clipboardTouched) {
                foreground(requireNotNull(current))
                requireNotNull(clipboard).clear()
                val manager = clipboardManager()
                manager.primaryClip?.let { clip ->
                    requireEvidence(clip.description.label?.toString() in knownLabels,
                        "Unknown replacement preserved; external synthetic-device cleanup required")
                    manager.clearPrimaryClip()
                }
                requireEvidence(manager.primaryClip == null && !requireNotNull(clipboard).containsSensitive(),
                    "Synthetic clipboard cleanup not established")
            }
        } catch (error: Throwable) { recordFailure("CLEANUP_CLIPBOARD", error); problem = error }
        try {
            onMain {
                overrideLocale = null
                captured.keys.filterNot { it.isDestroyed }.forEach { it.finishAndRemoveTask() }
            }
            awaitCondition("Captured Main destruction did not settle", precondition = true) {
                onMain { captured.keys.all { it.isDestroyed } }
            }
            onMain {
                if (defaultsTouched) {
                    LocaleList.setDefault(requireNotNull(originalLocales), originalLocaleIndex)
                    Locale.setDefault(requireNotNull(originalLocale))
                    Locale.setDefault(Locale.Category.DISPLAY, requireNotNull(originalDisplayLocale))
                    Locale.setDefault(Locale.Category.FORMAT, requireNotNull(originalFormatLocale))
                    requireEvidence(LocaleList.getDefault() == originalLocales &&
                        Locale.getDefault() == originalLocale &&
                        Locale.getDefault(Locale.Category.DISPLAY) == originalDisplayLocale &&
                        Locale.getDefault(Locale.Category.FORMAT) == originalFormatLocale,
                        "Exact original process locale defaults were not restored")
                }
            } // Restore process defaults only after every captured composition's Activity is destroyed.
        } catch (error: Throwable) {
            recordFailure("CLEANUP_ACTIVITIES_AND_LOCALES", error)
            problem = mergeFailure(problem, error)
        } finally {
            overrideLocale = null
            try { application?.unregisterActivityLifecycleCallbacks(callbacks) }
            catch (error: Throwable) {
                recordFailure("CLEANUP_CALLBACK_REGISTRATION", error)
                problem = mergeFailure(problem, error)
            }
        }
        problem?.let { throw it }
        // No Koin/Room graph replacement, package killing, storage deletion, or device settlement claim.
    }

    private val callbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, state: Bundle?) {
            if (activity is MainActivity) { captured[activity] = true; current = activity }
        }
        override fun onActivityResumed(activity: Activity) { if (activity is MainActivity) resumed = activity }
        override fun onActivityPaused(activity: Activity) { if (resumed === activity) resumed = null }
        override fun onActivityStopped(activity: Activity) { if (activity is MainActivity) stopped = activity }
        override fun onActivityStarted(activity: Activity) = Unit
        override fun onActivitySaveInstanceState(activity: Activity, state: Bundle) = Unit
        override fun onActivityDestroyed(activity: Activity) = Unit
    }

    private suspend fun awaitCondition(message: String, precondition: Boolean = false,
        condition: suspend () -> Boolean) {
        val deadline = SystemClock.elapsedRealtime() + 10_000
        while (!condition()) {
            if (SystemClock.elapsedRealtime() >= deadline) {
                if (precondition) throw EvidenceUnavailable(message) else throw AssertionError(message)
            }
            delay(50)
        }
    }

    private fun <T> onMain(body: () -> T): T {
        if (Looper.myLooper() == Looper.getMainLooper()) return body()
        val result = AtomicReference<Result<T>?>()
        val done = CountDownLatch(1)
        val task = Runnable { try { result.set(runCatching(body)) } finally { done.countDown() } }
        requireEvidence(handler.post(task), "Main callback was not accepted")
        try {
            requireEvidence(done.await(5, TimeUnit.SECONDS), "Main callback did not settle")
            return requireNotNull(result.get()).getOrThrow()
        } finally { handler.removeCallbacks(task) } // Cannot stop a callback already executing; outer bound required.
    }

    private fun mainIntent() = Intent(targetContext, MainActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
    private fun clipboardManager() = requireNotNull(targetContext.getSystemService(ClipboardManager::class.java))
    private fun caseStatus(case: AuditCase) = Bundle(evidence).apply {
        putString("id", "PassVaultAndroidClipboardLocale")
        putString("class", CASE_CLASS)
        putString("test", case.name)
        putInt("numtests", 2)
        putInt("current", startedCases)
    }
    private fun summary() = Bundle(evidence).apply {
        putInt("passvault.expectedCases", 2)
        putInt("passvault.startedCases", startedCases)
        putInt("passvault.passedCases", passedCases)
        putString("passvault.externalTargetSettlement", "REQUIRED_NOT_PROVEN_BY_INSTRUMENTATION")
        putBoolean("passvault.interruptionObserved", interruptionObserved)
        putStringArrayList("passvault.failurePhases", failurePhases)
        val tree = arrayListOf<String>()
        val seen = IdentityHashMap<Throwable, Boolean>()
        fun visit(error: Throwable, path: String) {
            if (tree.size >= 16 || seen.put(error, true) != null) return
            tree += "$path:${error.javaClass.name.take(160)}"
            error.suppressed.take(8).forEachIndexed { index, child -> visit(child, "$path.suppressed[$index]") }
        }
        terminalProblem?.let { visit(it, "primary") }
        putStringArrayList("passvault.primaryAndSuppressedFailureTypes", tree)
    }
    private fun finishFailure(message: String) = finish(Activity.RESULT_CANCELED, summary().apply {
        putString("shortMsg", message)
        putString("stream", "\nFAILED: $passedCases/2 fixed cases passed; inspect cleanup/outer evidence\n")
    })
    private fun assertAudit(condition: Boolean, message: String) { if (!condition) throw AssertionError(message) }
    private fun requireEvidence(condition: Boolean, message: String) {
        if (!condition) throw EvidenceUnavailable(message)
    }
    private fun recordFailure(phase: String, error: Throwable) {
        if (failurePhases.size < 16) failurePhases += "$phase:${error.javaClass.name.take(160)}"
        if (Thread.interrupted() || error is InterruptedException) interruptionObserved = true
        // Preserve the observation, clear only to attempt bounded cleanup, restore after terminal reporting.
    }
    private fun mergeFailure(first: Throwable?, next: Throwable): Throwable = first?.also {
        if (it !== next) it.addSuppressed(next)
    } ?: next
    private class EvidenceUnavailable(message: String) : IllegalStateException(message)
    private data class AuditCase(val name: String, val body: suspend () -> Unit)
    private companion object {
        const val DEBUG_PACKAGE = "com.passvault.android.debug"
        const val ISOLATION_ARGUMENT = "passvault.syntheticAndroid"
        const val ISOLATION_VALUE = "fresh-debug-app-and-synthetic-device-v1"
        const val SYNTHETIC_TEXT = "PassVault synthetic clipboard audit only"
        const val REPLACEMENT_LABEL = "PassVault:audit:PVA009:replacement"
        const val CASE_CLASS = "com.passvault.android.audit.AndroidClipboardLocaleInstrumentationTest"
    }
}
