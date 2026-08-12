package com.passvault.feature.generator.presentation

import app.cash.turbine.test
import com.passvault.core.testing.fakes.FakeCryptoEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class GeneratorViewModelTest {
    private lateinit var dispatcher: TestDispatcher
    private lateinit var cryptoEngine: FakeCryptoEngine

    @BeforeTest
    fun setUp() {
        dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)
        cryptoEngine = FakeCryptoEngine()
    }

    @AfterTest
    fun tearDown() {
        dispatcher.scheduler.runCurrent()
        Dispatchers.resetMain()
    }

    @Test
    fun `default passphrase has at least eighty bits of generator entropy`() = runTest(dispatcher) {
        val viewModel = GeneratorViewModel(cryptoEngine)
        runCurrent()

        viewModel.onEvent(GeneratorViewModel.GeneratorEvent.OnPassphraseModeChanged)
        runCurrent()

        val state = viewModel.state.value
        assertEquals(GeneratorViewModel.DEFAULT_WORD_COUNT, state.wordCount)
        assertEquals(80, GeneratorViewModel.passphraseEntropyBits(state.wordCount))
        assertEquals(GeneratorViewModel.PasswordStrength.VERY_STRONG, state.passwordStrength)
        assertEquals(state.wordCount, state.generatedPassword.split(state.wordSeparator).size)
    }

    @Test
    fun `word count is clamped to the audited entropy range`() = runTest(dispatcher) {
        val viewModel = GeneratorViewModel(cryptoEngine)
        runCurrent()
        viewModel.onEvent(GeneratorViewModel.GeneratorEvent.OnPassphraseModeChanged)

        viewModel.onEvent(GeneratorViewModel.GeneratorEvent.OnWordCountChanged(1))
        runCurrent()
        assertEquals(GeneratorViewModel.MIN_WORD_COUNT, viewModel.state.value.wordCount)
        assertTrue(GeneratorViewModel.passphraseEntropyBits(viewModel.state.value.wordCount) >= 60)

        viewModel.onEvent(GeneratorViewModel.GeneratorEvent.OnWordCountChanged(100))
        runCurrent()
        assertEquals(GeneratorViewModel.MAX_WORD_COUNT, viewModel.state.value.wordCount)
    }

    @Test
    fun `word separator keeps complete supplementary characters at its boundary`() = runTest(dispatcher) {
        val viewModel = GeneratorViewModel(cryptoEngine)
        runCurrent()
        viewModel.onEvent(GeneratorViewModel.GeneratorEvent.OnPassphraseModeChanged)

        viewModel.onEvent(GeneratorViewModel.GeneratorEvent.OnWordSeparatorChanged("🔐".repeat(10)))
        runCurrent()

        assertEquals("🔐".repeat(4), viewModel.state.value.wordSeparator)
        assertTrue(viewModel.state.value.generatedPassword.contains("🔐".repeat(4)))
    }

    @Test
    fun `cleared generator is regenerated when its screen is entered again`() = runTest(dispatcher) {
        val viewModel = GeneratorViewModel(cryptoEngine)
        runCurrent()
        assertTrue(viewModel.state.value.generatedPassword.isNotEmpty())

        viewModel.clearForLock()
        assertTrue(viewModel.state.value.generatedPassword.isEmpty())

        viewModel.ensureGenerated()
        runCurrent()
        assertTrue(viewModel.state.value.generatedPassword.isNotEmpty())
    }

    @Test
    fun `invalid character selection clears the previous generated password`() = runTest(dispatcher) {
        val viewModel = GeneratorViewModel(cryptoEngine)
        runCurrent()
        assertTrue(viewModel.state.value.generatedPassword.isNotEmpty())

        viewModel.onEvent(GeneratorViewModel.GeneratorEvent.OnIncludeUppercaseChanged(false))
        viewModel.onEvent(GeneratorViewModel.GeneratorEvent.OnIncludeLowercaseChanged(false))
        viewModel.onEvent(GeneratorViewModel.GeneratorEvent.OnIncludeNumbersChanged(false))
        viewModel.onEvent(GeneratorViewModel.GeneratorEvent.OnIncludeSymbolsChanged(false))
        runCurrent()

        assertEquals("", viewModel.state.value.generatedPassword)
        assertTrue(viewModel.state.value.errorMessage != null)
    }

    @Test
    fun `random generation failure clears the previous generated password`() = runTest(dispatcher) {
        val viewModel = GeneratorViewModel(cryptoEngine)
        runCurrent()
        assertTrue(viewModel.state.value.generatedPassword.isNotEmpty())
        cryptoEngine.setShouldFail()

        viewModel.onEvent(GeneratorViewModel.GeneratorEvent.OnGenerateClick)
        runCurrent()

        assertEquals("", viewModel.state.value.generatedPassword)
        assertTrue(viewModel.state.value.errorMessage != null)
    }

    @Test
    fun `secret effects emitted without a screen are not replayed later`() = runTest(dispatcher) {
        val viewModel = GeneratorViewModel(cryptoEngine)
        runCurrent()
        viewModel.onEvent(GeneratorViewModel.GeneratorEvent.OnCopyClick)

        viewModel.effect.test {
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }

        assertFalse(viewModel.state.value.generatedPassword.isEmpty())
    }

    @Test
    fun `generation state ends when secure output is ready`() = runTest(dispatcher) {
        val viewModel = GeneratorViewModel(cryptoEngine)

        assertTrue(viewModel.state.value.isGenerating)
        runCurrent()

        assertFalse(viewModel.state.value.isGenerating)
        assertTrue(viewModel.state.value.generatedPassword.isNotEmpty())
    }

    @Test
    fun `clipboard result reflects the platform operation and can be dismissed`() = runTest(dispatcher) {
        val viewModel = GeneratorViewModel(cryptoEngine)
        runCurrent()

        viewModel.onEvent(GeneratorViewModel.GeneratorEvent.OnCopyResult(succeeded = true))
        assertTrue(viewModel.state.value.statusMessage != null)
        assertNull(viewModel.state.value.errorMessage)

        viewModel.onEvent(GeneratorViewModel.GeneratorEvent.OnCopyResult(succeeded = false))
        assertNull(viewModel.state.value.statusMessage)
        assertTrue(viewModel.state.value.errorMessage != null)

        viewModel.onEvent(GeneratorViewModel.GeneratorEvent.OnDismissMessage)
        assertNull(viewModel.state.value.statusMessage)
        assertNull(viewModel.state.value.errorMessage)
    }
}
