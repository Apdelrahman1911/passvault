package com.passvault.core.crypto

import android.app.Activity
import android.app.Instrumentation
import android.os.Build
import android.os.Bundle
import android.os.Process
import com.ionspin.kotlin.crypto.LibsodiumInitializer
import java.io.File
import java.lang.reflect.Proxy
import kotlinx.coroutines.runBlocking

/**
 * Fixed real-native selection for PVA-001, not a general JUnit runner.
 *
 * Android device tests deliberately do not inherit commonTest: its test runner
 * dependencies and JVM method names are not part of this narrow device gate.
 * The four expected case IDs below must be independently reconciled with the
 * raw instrumentation transcript/XML. A Gradle task exit is not test evidence.
 *
 * Run only on an independently admitted, isolated synthetic Android32 target.
 * This class never opens vault storage, starts an Activity or reads clipboard,
 * account, biometric or signing state. A 64-bit process fails before any KDF.
 * Native library mapping/version observations do not prove packaged ELF bytes,
 * minified application behavior, create/unlock/backup or hardware security.
 */
class Android32KdfInstrumentation : Instrumentation() {
    private val runtimeEvidence = Bundle()
    private var startedCases = 0
    private var passedCases = 0

    private val cases = listOf(
        AuditCase("nativeRuntimeIs32BitAndLoadsSodium", ::nativeRuntimeIs32BitAndLoadsSodium),
        AuditCase("historicalBinaryPasswordVector", ::historicalBinaryPasswordVector),
        AuditCase("historicalTextPasswordVector", ::historicalTextPasswordVector),
        AuditCase("productionProfilesMatchReferenceVectors", ::productionProfilesMatchReferenceVectors),
    )

    override fun onCreate(arguments: Bundle?) {
        super.onCreate(arguments)
        // Allow only this inert AGP transport field; it is deliberately not
        // opened or used as an output directory. All other arguments fail:
        // ignoring filter/list/shard aliases could misrepresent this selection.
        val unsupported = arguments?.keySet().orEmpty() - ADDITIONAL_OUTPUT_DIRECTORY_ARGUMENT
        if (unsupported.isNotEmpty()) {
            finishFailure("Unsupported fixed-selection arguments: ${unsupported.sorted()}")
            return
        }
        if (arguments?.containsKey(ADDITIONAL_OUTPUT_DIRECTORY_ARGUMENT) == true) {
            val directory = arguments.getString(ADDITIONAL_OUTPUT_DIRECTORY_ARGUMENT)
            if (directory == null || directory.length > MAX_MAPPING_CHARS) {
                finishFailure("Invalid inert additional-output metadata")
                return
            }
        }
        start()
    }

    // Terminal test adapter: report all case throwables; never turn cancellation/linkage into a pass.
    @Suppress("TooGenericExceptionCaught")
    override fun onStart() {
        super.onStart()
        if (cases.size != EXPECTED_CASES || cases.map { it.name }.toSet().size != EXPECTED_CASES) {
            finishFailure("The fixed case inventory is inconsistent")
            return
        }
        for (case in cases) {
            startedCases += 1
            sendStatus(STATUS_START, caseStatus(case))
            try {
                runBlocking { case.body() }
            } catch (error: Throwable) {
                // Linkage errors and assertion failures must never become a
                // successful instrumentation completion. Unstarted cases stay
                // absent, not fabricated passes or silently skipped vectors.
                val failure = caseStatus(case).apply {
                    putString("stack", error.stackTraceToString().take(MAX_DIAGNOSTIC_CHARS))
                }
                sendStatus(if (error is AssertionError) STATUS_FAILURE else STATUS_ERROR, failure)
                finishFailure("${case.name}: ${error.javaClass.simpleName}")
                return
            }
            passedCases += 1
            sendStatus(STATUS_PASS, caseStatus(case))
        }
        // This terminal success is reachable only after every fixed case has
        // returned and its pass status has been sent successfully.
        check(startedCases == EXPECTED_CASES && passedCases == EXPECTED_CASES)
        finish(Activity.RESULT_OK, summary().apply {
            putString("stream", "\nOK ($EXPECTED_CASES fixed Android32 cases)\n")
        })
    }

    private fun caseStatus(case: AuditCase): Bundle = Bundle(runtimeEvidence).apply {
        putString("id", RUNNER_ID)
        putString("class", CASE_CLASS)
        putString("test", case.name)
        putInt("numtests", EXPECTED_CASES)
        putInt("current", startedCases)
    }

    private fun summary(): Bundle = Bundle(runtimeEvidence).apply {
        putString("id", RUNNER_ID)
        putInt("passvault.expectedCases", EXPECTED_CASES)
        putInt("passvault.startedCases", startedCases)
        putInt("passvault.passedCases", passedCases)
        putString("passvault.mappedElfBytes", "NOT_ESTABLISHED: reconcile the exact tested APK separately")
    }

    private fun finishFailure(message: String) {
        finish(Activity.RESULT_CANCELED, summary().apply {
            putString("shortMsg", message.take(MAX_DIAGNOSTIC_CHARS))
            putString("stream", "\nFAILED: $passedCases/$EXPECTED_CASES fixed cases passed\n")
        })
    }

    private suspend fun nativeRuntimeIs32BitAndLoadsSodium() {
        val is64Bit = Process.is64Bit()
        runtimeEvidence.putBoolean("passvault.processIs64Bit", is64Bit)
        runtimeEvidence.putInt("passvault.androidApi", Build.VERSION.SDK_INT)
        check(!is64Bit) { "Android32 evidence requires a 32-bit executing process" }

        // Reflection avoids adding a JNA device-test compile dependency and,
        // importantly, avoids inlining VERSION from compileOnly JNA5.19.1.
        // The historical packaged Android runtime is JNA5.18.1; retain the
        // observed value for reconciliation instead of assuming either one.
        val nativeClass = Class.forName("com.sun.jna.Native")
        val pointerSize = nativeClass.getField("POINTER_SIZE").getInt(null)
        val sizeTSize = nativeClass.getField("SIZE_T_SIZE").getInt(null)
        val longSize = nativeClass.getField("LONG_SIZE").getInt(null)
        // VERSION can be inherited from JNA's package-private Version
        // interface. Relax access only for this exact app-library constant,
        // never a platform hidden API, native-memory read or arbitrary field.
        val jnaVersion = nativeClass.getField("VERSION").apply {
            isAccessible = true
        }.get(null) as String
        runtimeEvidence.putInt("passvault.pointerBytes", pointerSize)
        runtimeEvidence.putInt("passvault.sizeTBytes", sizeTSize)
        runtimeEvidence.putInt("passvault.nativeLongBytes", longSize)
        runtimeEvidence.putString("passvault.jnaRuntimeVersion", jnaVersion)
        check(pointerSize == ANDROID32_BYTES && sizeTSize == ANDROID32_BYTES) {
            "Native pointer/size_t widths must both be four bytes"
        }
        check(jnaVersion.isNotBlank() && jnaVersion.length <= MAX_VERSION_CHARS)

        LibsodiumInitializer.initialize()
        val initializer = LibsodiumInitializer
        val sodiumProxy = initializer.javaClass.getMethod("getSodiumJna").invoke(initializer)
        val handler = Proxy.getInvocationHandler(sodiumProxy)
        val handlerClass = Class.forName("com.sun.jna.Library\$Handler")
        check(handlerClass.isInstance(handler)) { "Unexpected libsodium JNA adapter" }
        val library = handlerClass.getMethod("getNativeLibrary").invoke(handler)
        val libraryClass = Class.forName("com.sun.jna.NativeLibrary")
        val getFunction = libraryClass.getMethod("getFunction", String::class.java)
        val hashFunction = getFunction.invoke(library, "crypto_pwhash")
        val versionFunction = getFunction.invoke(library, "sodium_version_string")
        val functionClass = Class.forName("com.sun.jna.Function")
        val sodiumVersion = functionClass.getMethod(
            "invokeString",
            Array<Any>::class.java,
            Boolean::class.javaPrimitiveType,
        ).invoke(versionFunction, emptyArray<Any>(), false) as String
        check(sodiumVersion.isNotBlank() && sodiumVersion.length <= MAX_VERSION_CHARS)
        runtimeEvidence.putString("passvault.loadedSodiumVersion", sodiumVersion)
        val libraryFile = libraryClass.getMethod("getFile").invoke(library) as? File
        runtimeEvidence.putString("passvault.sodiumLibraryName", libraryFile?.path?.take(MAX_MAPPING_CHARS))

        val pointerClass = Class.forName("com.sun.jna.Pointer")
        val pointer = pointerClass.getMethod("nativeValue", pointerClass).invoke(null, hashFunction) as Long
        val address = pointer and ANDROID32_ADDRESS_MASK
        check(address != 0L) { "crypto_pwhash function address must not be null" }
        runtimeEvidence.putString("passvault.cryptoPwhashMapping", functionMapping(address))
    }

    /** Retain only the code-symbol's own mapping row, never a process memory dump. */
    // Optional mapping evidence keeps Exception failures explicit; Errors still reach the terminal test boundary.
    @Suppress("TooGenericExceptionCaught")
    private fun functionMapping(address: Long): String = try {
        var mapping: String? = null
        File("/proc/self/maps").bufferedReader().use { reader ->
            var lines = 0
            while (lines < MAX_MAPPING_LINES && mapping == null) {
                val line = reader.readLine() ?: break
                lines += 1
                check(line.length <= MAX_MAPPING_CHARS) { "Mapping line exceeds evidence bound" }
                if (mappingContainsAddress(line, address)) {
                    mapping = line
                }
            }
        }
        mapping?.let { "OBSERVED_NOT_ELF_BOUND: $it" }
            ?: "NOT_ESTABLISHED: no symbol mapping within the bounded read"
    } catch (error: Exception) {
        // Some Android policies may deny maps. Width/native-vector observations
        // remain distinct from that explicit mapping/packaged-image gap.
        "NOT_ESTABLISHED: ${error.javaClass.simpleName}"
    }

    private fun mappingContainsAddress(line: String, address: Long): Boolean {
        val range = line.substringBefore(' ').split('-', limit = 2)
        if (range.size != 2) return false
        val start = range[0].toLongOrNull(HEX_RADIX)
        val end = range[1].toLongOrNull(HEX_RADIX)
        return if (start != null && end != null) {
            address >= start && address < end
        } else {
            false
        }
    }

    private suspend fun historicalBinaryPasswordVector() = deriveAndCheck(
        passwordFactory = { byteArrayOf(0x00, 0xFF.toByte(), 0x01) },
        saltFactory = { ByteArray(SALT_BYTES) { it.toByte() } },
        opsLimit = 1,
        memLimit = SMALL_MEMORY_BYTES,
        expectedHex = "2bc5a714c8397bb9e89e70c957231bd3cd4f0b4ffe32bb6ca1f5067196b60b15",
    )

    private suspend fun historicalTextPasswordVector() = deriveAndCheck(
        passwordFactory = { "TestPassword123!".encodeToByteArray() },
        saltFactory = { ByteArray(SALT_BYTES) { it.toByte() } },
        opsLimit = 1,
        memLimit = SMALL_MEMORY_BYTES,
        expectedHex = "4c1422bcf6ea79ca8b843170ffbaf713f854f1b97e4fd0df07d741a650cacab7",
    )

    private suspend fun productionProfilesMatchReferenceVectors() {
        // Same independently pinned vectors as commonTest/Argon2Test.kt, not
        // values computed by this adapter. Upstream Argon2 reference CLI tag
        // 20190702 / commit62358ba2123abd17fccf2a108a301d4b52c01a7c:
        // printf '5465737450617373776f726431323321' |
        //   ./argon2 0123456789abcdef -id -v 13 -t {3,4} -m 16 -p 1 -l 32 -r
        // This device selection checks both 64MiB KDF profiles, not benchmark
        // timing or the profile-selection policy. Hex compatibility is REQUIRED.
        val vectors = listOf(
            3 to "dc1aff4d7c74898c0aca2da51e33760dbe716e70abc86a89f6e9d55d5451b03c",
            4 to "ccd29a7f8888cf2ddce1df111f8ad5ba7939b0a177ac6606ad98beb6ac160172",
        )
        for ((opsLimit, expectedHex) in vectors) {
            deriveAndCheck(
                passwordFactory = { "TestPassword123!".encodeToByteArray() },
                saltFactory = { "0123456789abcdef".encodeToByteArray() },
                opsLimit = opsLimit,
                memLimit = PRODUCTION_MEMORY_BYTES,
                expectedHex = expectedHex,
            )
        }
    }

    private suspend fun deriveAndCheck(
        passwordFactory: () -> ByteArray,
        saltFactory: () -> ByteArray,
        opsLimit: Int,
        memLimit: Int,
        expectedHex: String,
    ) {
        var password: ByteArray? = null
        var salt: ByteArray? = null
        var expected: ByteArray? = null
        var derived: DerivedKey? = null
        try {
            password = passwordFactory()
            salt = saltFactory()
            expected = ByteArray(KEY_BYTES) { index ->
                expectedHex.substring(index * 2, index * 2 + 2).toInt(HEX_RADIX).toByte()
            }
            derived = LibsodiumCryptoEngine().deriveKey(password, salt, opsLimit, memLimit).getOrThrow()
            if (!expected.contentEquals(derived.key)) {
                throw AssertionError("Production native KDF did not match its pinned compatibility vector")
            }
        } finally {
            derived?.clear()
            expected?.fill(0)
            salt?.fill(0)
            password?.fill(0)
        }
    }

    private data class AuditCase(val name: String, val body: suspend () -> Unit)

    private companion object {
        const val RUNNER_ID = "PassVaultAndroid32Kdf"
        const val CASE_CLASS = "com.passvault.core.crypto.Android32KdfInstrumentation"
        const val EXPECTED_CASES = 4
        const val STATUS_START = 1
        const val STATUS_PASS = 0
        const val STATUS_ERROR = -1
        const val STATUS_FAILURE = -2
        const val ANDROID32_BYTES = 4
        const val ANDROID32_ADDRESS_MASK = 0xFFFF_FFFFL
        const val KEY_BYTES = 32
        const val SALT_BYTES = 16
        const val SMALL_MEMORY_BYTES = 8 * 1024
        const val PRODUCTION_MEMORY_BYTES = 64 * 1024 * 1024
        const val HEX_RADIX = 16
        const val MAX_VERSION_CHARS = 64
        const val MAX_MAPPING_CHARS = 4096
        const val MAX_MAPPING_LINES = 8192
        const val MAX_DIAGNOSTIC_CHARS = 8192
        const val ADDITIONAL_OUTPUT_DIRECTORY_ARGUMENT = "additionalTestOutputDir"
    }
}
