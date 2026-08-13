package com.passvault.feature.generator.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passvault.core.crypto.CryptoEngine
import com.passvault.core.crypto.PasswordGenerationOptions
import com.passvault.core.crypto.SecurePasswordGenerator
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.text.UiText
import com.passvault.core.designsystem.text.uiText
import com.passvault.core.domain.model.PasswordScore
import com.passvault.core.domain.model.PasswordStrengthEvaluator
import com.passvault.core.domain.model.takeCodePoints
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GeneratorViewModel(
    private val cryptoEngine: CryptoEngine,
) : ViewModel() {

    private val passwordGenerator = SecurePasswordGenerator(cryptoEngine)

    private val _state = MutableStateFlow(GeneratorState())
    val state: StateFlow<GeneratorState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<GeneratorEffect>(extraBufferCapacity = 1)
    val effect: SharedFlow<GeneratorEffect> = _effect.asSharedFlow()
    private var generationJob: Job? = null

    init {
        generatePassword()
    }

    override fun onCleared() {
        clearForLock()
        super.onCleared()
    }

    fun onEvent(event: GeneratorEvent) {
        when (event) {
            is PasswordOptionEvent -> updatePasswordOptions(event)
            is PassphraseOptionEvent -> updatePassphraseOptions(event)
            GeneratorEvent.OnGenerateClick -> generateCurrentMode()
            GeneratorEvent.OnCopyClick -> emitGeneratedPassword(GeneratorEffect::CopyToClipboard)
            GeneratorEvent.OnUseClick -> emitGeneratedPassword(GeneratorEffect::UsePassword)
            GeneratorEvent.OnDismissMessage ->
                _state.update { it.copy(errorMessage = null, statusMessage = null) }
            is GeneratorEvent.OnCopyResult -> _state.update {
                it.copy(
                    statusMessage = if (event.succeeded) uiText(Res.string.action_copy_success) else null,
                    errorMessage = if (event.succeeded) null else uiText(Res.string.error_generator_copy),
                )
            }
            GeneratorEvent.OnPassphraseModeChanged -> {
                _state.update { it.copy(isPassphraseMode = !it.isPassphraseMode) }
                generateCurrentMode()
            }
        }
    }

    private fun updatePasswordOptions(event: PasswordOptionEvent) {
        _state.update { state ->
            when (event) {
                is GeneratorEvent.OnLengthChanged -> state.copy(
                    length = event.length.coerceIn(MIN_PASSWORD_LENGTH, MAX_PASSWORD_LENGTH),
                )
                is GeneratorEvent.OnIncludeUppercaseChanged -> state.copy(includeUppercase = event.include)
                is GeneratorEvent.OnIncludeLowercaseChanged -> state.copy(includeLowercase = event.include)
                is GeneratorEvent.OnIncludeNumbersChanged -> state.copy(includeNumbers = event.include)
                is GeneratorEvent.OnIncludeSymbolsChanged -> state.copy(includeSymbols = event.include)
                is GeneratorEvent.OnExcludeAmbiguousChanged -> state.copy(excludeAmbiguous = event.exclude)
            }
        }
        generatePassword()
    }

    private fun updatePassphraseOptions(event: PassphraseOptionEvent) {
        _state.update { state ->
            when (event) {
                is GeneratorEvent.OnWordCountChanged -> state.copy(
                    wordCount = event.count.coerceIn(MIN_WORD_COUNT, MAX_WORD_COUNT),
                )
                is GeneratorEvent.OnWordSeparatorChanged -> state.copy(
                    wordSeparator = event.separator.takeCodePoints(MAX_SEPARATOR_LENGTH),
                )
            }
        }
        generatePassphrase()
    }

    private fun emitGeneratedPassword(effect: (String) -> GeneratorEffect) {
        _state.value.generatedPassword
            .takeIf(String::isNotEmpty)
            ?.let { _effect.tryEmit(effect(it)) }
    }

    private fun generateCurrentMode() {
        if (_state.value.isPassphraseMode) generatePassphrase() else generatePassword()
    }

    private fun generatePassword() {
        generationJob?.cancel()
        clearGeneratedOutput()
        generationJob = viewModelScope.launch {
            val currentState = _state.value
            if (!currentState.hasSelectedCharacterSet()) {
                _state.update {
                    it.copy(
                        isGenerating = false,
                        errorMessage = uiText(Res.string.error_generator_character_type),
                    )
                }
                return@launch
            }

            try {
                val password = passwordGenerator.generate(currentState.toGenerationOptions()).getOrThrow()
                currentCoroutineContext().ensureActive()
                _state.update {
                    it.copy(
                        generatedPassword = password,
                        passwordStrength = passwordStrength(password),
                        isGenerating = false,
                        errorMessage = null,
                    )
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                currentCoroutineContext().ensureActive()
                _state.update {
                    it.copy(
                        generatedPassword = "",
                        passwordStrength = PasswordStrength.WEAK,
                        isGenerating = false,
                        errorMessage = uiText(Res.string.error_generator_password_failed),
                    )
                }
            }
        }
    }

    private fun generatePassphrase() {
        generationJob?.cancel()
        clearGeneratedOutput()
        generationJob = viewModelScope.launch {
            val currentState = _state.value
            try {
                val wordCount = currentState.wordCount.coerceIn(MIN_WORD_COUNT, MAX_WORD_COUNT)
                val passphrase = List(wordCount) {
                    val adjective = PASSPHRASE_ADJECTIVES[
                        secureRandomIndex(PASSPHRASE_ADJECTIVES.size)
                    ]
                    val noun = PASSPHRASE_NOUNS[secureRandomIndex(PASSPHRASE_NOUNS.size)]
                    adjective + noun.replaceFirstChar { it.uppercaseChar() }
                }.joinToString(currentState.wordSeparator)
                currentCoroutineContext().ensureActive()
                _state.update {
                    it.copy(
                        generatedPassword = passphrase,
                        passwordStrength = passphraseStrength(wordCount),
                        isGenerating = false,
                        errorMessage = null,
                    )
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                currentCoroutineContext().ensureActive()
                _state.update {
                    it.copy(
                        generatedPassword = "",
                        passwordStrength = PasswordStrength.WEAK,
                        isGenerating = false,
                        errorMessage = uiText(Res.string.error_generator_passphrase_failed),
                    )
                }
            }
        }
    }

    private fun clearGeneratedOutput() {
        _state.update {
            it.copy(
                generatedPassword = "",
                passwordStrength = PasswordStrength.WEAK,
                isGenerating = true,
                errorMessage = null,
                statusMessage = null,
            )
        }
    }

    private suspend fun secureRandomIndex(bound: Int): Int {
        require(bound in 1..65_535)
        val range = 65_536
        val rejectionLimit = range - (range % bound)
        while (true) {
            val bytes = cryptoEngine.generateRandom(2).getOrThrow()
            try {
                val value = ((bytes[0].toInt() and 0xff) shl 8) or (bytes[1].toInt() and 0xff)
                if (value < rejectionLimit) return value % bound
            } finally {
                cryptoEngine.secureWipe(bytes)
            }
        }
    }

    fun clearForLock() {
        generationJob?.cancel()
        generationJob = null
        _state.update {
            GeneratorState(
                length = it.length,
                wordCount = it.wordCount,
                includeUppercase = it.includeUppercase,
                includeLowercase = it.includeLowercase,
                includeNumbers = it.includeNumbers,
                includeSymbols = it.includeSymbols,
                excludeAmbiguous = it.excludeAmbiguous,
                isPassphraseMode = it.isPassphraseMode,
                wordSeparator = it.wordSeparator,
            )
        }
    }

    fun ensureGenerated() {
        if (_state.value.generatedPassword.isNotEmpty() || generationJob?.isActive == true) return
        generateCurrentMode()
    }

    data class GeneratorState(
        val length: Int = 16,
        val includeUppercase: Boolean = true,
        val includeLowercase: Boolean = true,
        val includeNumbers: Boolean = true,
        val includeSymbols: Boolean = true,
        val excludeAmbiguous: Boolean = false,
        val isPassphraseMode: Boolean = false,
        val wordCount: Int = DEFAULT_WORD_COUNT,
        val wordSeparator: String = "-",
        val generatedPassword: String = "",
        val passwordStrength: PasswordStrength = PasswordStrength.WEAK,
        val isGenerating: Boolean = false,
        val errorMessage: UiText? = null,
        val statusMessage: UiText? = null,
    )

    sealed interface GeneratorEvent {
        data class OnLengthChanged(val length: Int) : PasswordOptionEvent
        data class OnIncludeUppercaseChanged(val include: Boolean) : PasswordOptionEvent
        data class OnIncludeLowercaseChanged(val include: Boolean) : PasswordOptionEvent
        data class OnIncludeNumbersChanged(val include: Boolean) : PasswordOptionEvent
        data class OnIncludeSymbolsChanged(val include: Boolean) : PasswordOptionEvent
        data class OnExcludeAmbiguousChanged(val exclude: Boolean) : PasswordOptionEvent
        data class OnWordCountChanged(val count: Int) : PassphraseOptionEvent
        data class OnWordSeparatorChanged(val separator: String) : PassphraseOptionEvent
        data object OnGenerateClick : GeneratorEvent
        data object OnCopyClick : GeneratorEvent
        data object OnUseClick : GeneratorEvent
        data object OnDismissMessage : GeneratorEvent
        data class OnCopyResult(val succeeded: Boolean) : GeneratorEvent
        data object OnPassphraseModeChanged : GeneratorEvent
    }

    sealed interface PasswordOptionEvent : GeneratorEvent

    sealed interface PassphraseOptionEvent : GeneratorEvent

    sealed interface GeneratorEffect {
        data class CopyToClipboard(val password: String) : GeneratorEffect
        data class UsePassword(val password: String) : GeneratorEffect
    }

    enum class PasswordStrength {
        WEAK, FAIR, GOOD, STRONG, VERY_STRONG
    }

    companion object {
        internal const val MIN_PASSWORD_LENGTH = PasswordGenerationOptions.MIN_PASSWORD_LENGTH
        internal const val MAX_PASSWORD_LENGTH = PasswordGenerationOptions.MAX_PASSWORD_LENGTH
        internal const val MIN_WORD_COUNT = 6
        internal const val MAX_WORD_COUNT = 12
        internal const val DEFAULT_WORD_COUNT = 8
        private const val MAX_SEPARATOR_LENGTH = 4
        private val PASSPHRASE_ADJECTIVES = listOf(
            "amber", "autumn", "brave", "breezy", "bright", "calm", "clever", "coral",
            "crisp", "eager", "emerald", "gentle", "golden", "happy", "honest", "ivory",
            "jolly", "kind", "lively", "lunar", "mellow", "misty", "noble", "quiet",
            "rapid", "rosy", "silver", "steady", "sunny", "swift", "vivid", "warm",
        )
        private val PASSPHRASE_NOUNS = listOf(
            "acorn", "anchor", "badger", "beacon", "birch", "canyon", "cedar", "comet",
            "dolphin", "ember", "falcon", "fern", "glacier", "harbor", "heron", "island",
            "jasmine", "lantern", "maple", "meadow", "meteor", "ocean", "orchid", "pebble",
            "pine", "river", "robin", "summit", "thunder", "willow", "zephyr", "zinnia",
        )

        internal fun passphraseEntropyBits(wordCount: Int): Int =
            wordCount.coerceIn(MIN_WORD_COUNT, MAX_WORD_COUNT) * BITS_PER_COMPOUND_WORD

        internal fun passphraseStrength(wordCount: Int): PasswordStrength =
            when (passphraseEntropyBits(wordCount)) {
                in 0..39 -> PasswordStrength.WEAK
                in 40..49 -> PasswordStrength.FAIR
                in 50..59 -> PasswordStrength.GOOD
                in 60..79 -> PasswordStrength.STRONG
                else -> PasswordStrength.VERY_STRONG
            }

        private const val BITS_PER_COMPOUND_WORD = 10
    }
}

private fun GeneratorViewModel.GeneratorState.hasSelectedCharacterSet(): Boolean =
    includeLowercase || includeUppercase || includeNumbers || includeSymbols

private fun GeneratorViewModel.GeneratorState.toGenerationOptions() = PasswordGenerationOptions(
    length = length,
    includeUppercase = includeUppercase,
    includeLowercase = includeLowercase,
    includeNumbers = includeNumbers,
    includeSymbols = includeSymbols,
    excludeAmbiguous = excludeAmbiguous,
)

private fun passwordStrength(password: String): GeneratorViewModel.PasswordStrength =
    when (PasswordStrengthEvaluator.score(password)) {
        PasswordScore.UNKNOWN,
        PasswordScore.VERY_WEAK,
        PasswordScore.WEAK,
        -> GeneratorViewModel.PasswordStrength.WEAK
        PasswordScore.FAIR -> GeneratorViewModel.PasswordStrength.FAIR
        PasswordScore.GOOD -> GeneratorViewModel.PasswordStrength.GOOD
        PasswordScore.STRONG -> GeneratorViewModel.PasswordStrength.STRONG
        PasswordScore.VERY_STRONG -> GeneratorViewModel.PasswordStrength.VERY_STRONG
    }
