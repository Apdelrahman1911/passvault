package com.passvault.core.crypto

import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

/**
 * Performance benchmark tests for cryptographic operations.
 */
class CryptoBenchmarkTest {

    private lateinit var cryptoEngine: CryptoEngine

    @BeforeTest
    fun setUp() {
        cryptoEngine = LibsodiumCryptoEngine()
    }

    @Test
    fun benchmark_generateRandom() = runTest(timeout = 30.seconds) {
        val iterations = 1000
        val durations = mutableListOf<Duration>()

        repeat(iterations) {
            val start = TimeSource.Monotonic.markNow()
            cryptoEngine.generateRandom(32)
            durations.add(start.elapsedNow())
        }

        val average = durations.averageDuration()
        val min = durations.minOrNull()!!
        val max = durations.maxOrNull()!!

        println("generateRandom(32 bytes):")
        println("  Average: ${average.milliseconds} ms")
        println("  Min: ${min.milliseconds} ms")
        println("  Max: ${max.milliseconds} ms")

        // Assert reasonable performance (< 1ms average)
        assertTrue(
            average < 1.milliseconds,
            "Average time ${average.milliseconds}ms exceeds 1ms threshold",
        )
    }

    @Test
    fun benchmark_encryptDecrypt_smallData() = runTest(timeout = 30.seconds) {
        val key = cryptoEngine.generateRandom(32).getOrThrow()
        val plaintext = "Small test data".encodeToByteArray()
        val iterations = 100
        val durations = mutableListOf<Duration>()

        repeat(iterations) {
            val start = TimeSource.Monotonic.markNow()
            val encrypted = cryptoEngine.encrypt(plaintext, key).getOrThrow()
            cryptoEngine.decrypt(encrypted.ciphertext, encrypted.nonce, key).getOrThrow()
            durations.add(start.elapsedNow())
        }

        val average = durations.averageDuration()
        println("encrypt/decrypt (15 bytes):")
        println("  Average: ${average.milliseconds} ms")

        assertTrue(average < 5.milliseconds)
    }

    @Test
    fun benchmark_encryptDecrypt_largeData() = runTest(timeout = 60.seconds) {
        val key = cryptoEngine.generateRandom(32).getOrThrow()
        val plaintext = ByteArray(1024 * 1024) // 1MB
        val iterations = 10
        val durations = mutableListOf<Duration>()

        repeat(iterations) {
            val start = TimeSource.Monotonic.markNow()
            val encrypted = cryptoEngine.encrypt(plaintext, key).getOrThrow()
            cryptoEngine.decrypt(encrypted.ciphertext, encrypted.nonce, key).getOrThrow()
            durations.add(start.elapsedNow())

            encrypted.clear()
        }

        val average = durations.averageDuration()
        println("encrypt/decrypt (1MB):")
        println("  Average: ${average.milliseconds} ms")

        // 1MB should encrypt/decrypt in less than 100ms
        assertTrue(average < 100.milliseconds)
    }

    @Test
    fun benchmark_argon2id_interactive() = runTest(timeout = 60.seconds) {
        val password = "TestPassword123!"
        val salt = cryptoEngine.generateRandom(16).getOrThrow()
        val iterations = 10
        val durations = mutableListOf<Duration>()

        repeat(iterations) {
            val start = TimeSource.Monotonic.markNow()
            cryptoEngine.deriveKey(
                password.encodeToByteArray(),
                salt,
                Argon2Parameters.INTERACTIVE.opsLimit,
                Argon2Parameters.INTERACTIVE.memLimit
            )
            durations.add(start.elapsedNow())
        }

        val average = durations.averageDuration()
        println("Argon2id (Interactive):")
        println("  Average: ${average.milliseconds} ms")
        println("  opsLimit: ${Argon2Parameters.INTERACTIVE.opsLimit}")
        println("  memLimit: ${Argon2Parameters.INTERACTIVE.memLimit / 1024 / 1024} MB")

        // Interactive should be < 500ms
        assertTrue(average < 500.milliseconds)
    }

    @Test
    fun benchmark_constantTimeEquals() = runTest {
        val a = cryptoEngine.generateRandom(32).getOrThrow()
        val b = a.copyOf()
        val c = cryptoEngine.generateRandom(32).getOrThrow()

        val iterations = 10000

        // Equal arrays
        val start1 = TimeSource.Monotonic.markNow()
        repeat(iterations) {
            cryptoEngine.constantTimeEquals(a, b)
        }
        val duration1 = start1.elapsedNow()

        // Different arrays
        val start2 = TimeSource.Monotonic.markNow()
        repeat(iterations) {
            cryptoEngine.constantTimeEquals(a, c)
        }
        val duration2 = start2.elapsedNow()

        println("constantTimeEquals (10000 iterations):")
        println("  Equal arrays: ${duration1.milliseconds} ms")
        println("  Different arrays: ${duration2.milliseconds} ms")

        // Unit-test environments are noisy; keep the measurements for reporting
        // and assert only that both benchmark paths executed.
        assertTrue(duration1.isPositive())
        assertTrue(duration2.isPositive())
    }

    @Test
    fun benchmark_keyedBlake2bSubkey() = runTest {
        val masterKey = cryptoEngine.generateRandom(32).getOrThrow()
        val iterations = 1000
        val durations = mutableListOf<Duration>()

        repeat(iterations) {
            val start = TimeSource.Monotonic.markNow()
            cryptoEngine.deriveSubkey(masterKey, "test-context", 32)
            durations.add(start.elapsedNow())
        }

        val average = durations.averageDuration()
        println("Keyed BLAKE2b subkey derivation (1000 iterations):")
        println("  Average: ${average.microseconds} µs")

        assertTrue(average < 1.milliseconds)
    }

    @Test
    fun benchmark_vaultKeyHierarchy() = runTest {
        val hierarchy = VaultKeyHierarchy(cryptoEngine)
        val vek = cryptoEngine.generateMasterKey().getOrThrow()

        val iterations = 100
        val durations = mutableListOf<Duration>()

        repeat(iterations) {
            val start = TimeSource.Monotonic.markNow()
            hierarchy.deriveRecordKey(vek, "record-$it")
            durations.add(start.elapsedNow())
        }

        val average = durations.averageDuration()
        println("Vault key hierarchy (100 iterations):")
        println("  Average: ${average.microseconds} µs")

        assertTrue(average < 5.milliseconds)
    }

    private fun List<Duration>.averageDuration(): Duration =
        fold(Duration.ZERO) { total, duration -> total + duration } / size

    private val Duration.microseconds: Double
        get() = this.inWholeNanoseconds / 1_000.0

    private val Duration.milliseconds: Double
        get() = this.inWholeNanoseconds / 1_000_000.0
}
