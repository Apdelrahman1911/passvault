package com.passvault.feature.generator.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passvault.core.crypto.CryptoEngine
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.text.UiText
import com.passvault.core.designsystem.text.uiText
import com.passvault.core.domain.model.PasswordScore
import com.passvault.core.domain.model.PasswordStrengthEvaluator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GeneratorViewModel(
    private val cryptoEngine: CryptoEngine,
) : ViewModel() {

    private val _state = MutableStateFlow(GeneratorState())
    val state: StateFlow<GeneratorState> = _state.asStateFlow()

    private val _effect = Channel<GeneratorEffect>(Channel.BUFFERED)
    val effect: Flow<GeneratorEffect> = _effect.receiveAsFlow()
    private var generationJob: Job? = null

    init {
        generatePassword()
    }

    fun onEvent(event: GeneratorEvent) {
        when (event) {
            is GeneratorEvent.OnLengthChanged -> {
                _state.update {
                    it.copy(length = event.length.coerceIn(MIN_PASSWORD_LENGTH, MAX_PASSWORD_LENGTH))
                }
                generatePassword()
            }
            is GeneratorEvent.OnIncludeUppercaseChanged -> {
                _state.update { it.copy(includeUppercase = event.include) }
                generatePassword()
            }
            is GeneratorEvent.OnIncludeLowercaseChanged -> {
                _state.update { it.copy(includeLowercase = event.include) }
                generatePassword()
            }
            is GeneratorEvent.OnIncludeNumbersChanged -> {
                _state.update { it.copy(includeNumbers = event.include) }
                generatePassword()
            }
            is GeneratorEvent.OnIncludeSymbolsChanged -> {
                _state.update { it.copy(includeSymbols = event.include) }
                generatePassword()
            }
            is GeneratorEvent.OnExcludeAmbiguousChanged -> {
                _state.update { it.copy(excludeAmbiguous = event.exclude) }
                generatePassword()
            }
            GeneratorEvent.OnGenerateClick -> generatePassword()
            GeneratorEvent.OnCopyClick -> {
                _state.value.generatedPassword
                    .takeIf(String::isNotEmpty)
                    ?.let { _effect.trySend(GeneratorEffect.CopyToClipboard(it)) }
            }
            GeneratorEvent.OnUseClick -> {
                _state.value.generatedPassword
                    .takeIf(String::isNotEmpty)
                    ?.let { _effect.trySend(GeneratorEffect.UsePassword(it)) }
            }
            GeneratorEvent.OnDismissError -> {
                _state.update { it.copy(errorMessage = null) }
            }
            GeneratorEvent.OnPassphraseModeChanged -> {
                _state.update { it.copy(isPassphraseMode = !it.isPassphraseMode) }
                if (_state.value.isPassphraseMode) {
                    generatePassphrase()
                } else {
                    generatePassword()
                }
            }
            is GeneratorEvent.OnWordCountChanged -> {
                _state.update {
                    it.copy(wordCount = event.count.coerceIn(MIN_WORD_COUNT, MAX_WORD_COUNT))
                }
                generatePassphrase()
            }
            is GeneratorEvent.OnWordSeparatorChanged -> {
                _state.update { it.copy(wordSeparator = event.separator.take(MAX_SEPARATOR_LENGTH)) }
                generatePassphrase()
            }
        }
    }

    private fun generatePassword() {
        generationJob?.cancel()
        generationJob = viewModelScope.launch {
            val currentState = _state.value
            val selectedSets = buildList {
                if (currentState.includeLowercase) add(LOWERCASE)
                if (currentState.includeUppercase) add(UPPERCASE)
                if (currentState.includeNumbers) add(NUMBERS)
                if (currentState.includeSymbols) add(SYMBOLS)
            }.map { characters ->
                if (currentState.excludeAmbiguous) {
                    characters.filterNot { it in AMBIGUOUS }
                } else {
                    characters
                }
            }.filter { it.isNotEmpty() }

            if (selectedSets.isEmpty()) {
                _state.update {
                    it.copy(errorMessage = uiText(Res.string.error_generator_character_type))
                }
                return@launch
            }

            try {
                val allCharacters = selectedSets.joinToString("")
                val safeLength = currentState.length
                    .coerceIn(MIN_PASSWORD_LENGTH, MAX_PASSWORD_LENGTH)
                    .coerceAtLeast(selectedSets.size)
                val output = MutableList(safeLength) {
                    allCharacters[secureRandomIndex(allCharacters.length)]
                }

                selectedSets.forEachIndexed { index, characters ->
                    output[index] = characters[secureRandomIndex(characters.length)]
                }

                for (index in output.lastIndex downTo 1) {
                    val swapIndex = secureRandomIndex(index + 1)
                    val temporary = output[index]
                    output[index] = output[swapIndex]
                    output[swapIndex] = temporary
                }
                val password = output.joinToString("")
                _state.update {
                    it.copy(
                        generatedPassword = password,
                        passwordStrength = calculateStrength(password),
                        errorMessage = null,
                    )
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _state.update {
                    it.copy(errorMessage = uiText(Res.string.error_generator_password_failed))
                }
            }
        }
    }

    private fun generatePassphrase() {
        generationJob?.cancel()
        generationJob = viewModelScope.launch {
            val currentState = _state.value
            try {
                val passphrase = List(currentState.wordCount.coerceIn(MIN_WORD_COUNT, MAX_WORD_COUNT)) {
                    WORD_LIST[secureRandomIndex(WORD_LIST.size)]
                }.joinToString(currentState.wordSeparator)
                _state.update {
                    it.copy(
                        generatedPassword = passphrase,
                        passwordStrength = calculateStrength(passphrase),
                        errorMessage = null,
                    )
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _state.update {
                    it.copy(errorMessage = uiText(Res.string.error_generator_passphrase_failed))
                }
            }
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

    private fun calculateStrength(password: String): PasswordStrength {
        return when (PasswordStrengthEvaluator.score(password)) {
            PasswordScore.UNKNOWN,
            PasswordScore.VERY_WEAK,
            PasswordScore.WEAK,
            -> PasswordStrength.WEAK
            PasswordScore.FAIR -> PasswordStrength.FAIR
            PasswordScore.GOOD -> PasswordStrength.GOOD
            PasswordScore.STRONG -> PasswordStrength.STRONG
            PasswordScore.VERY_STRONG -> PasswordStrength.VERY_STRONG
        }
    }

    fun clearForLock() {
        generationJob?.cancel()
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

    data class GeneratorState(
        val length: Int = 16,
        val includeUppercase: Boolean = true,
        val includeLowercase: Boolean = true,
        val includeNumbers: Boolean = true,
        val includeSymbols: Boolean = true,
        val excludeAmbiguous: Boolean = false,
        val isPassphraseMode: Boolean = false,
        val wordCount: Int = 4,
        val wordSeparator: String = "-",
        val generatedPassword: String = "",
        val passwordStrength: PasswordStrength = PasswordStrength.GOOD,
        val errorMessage: UiText? = null,
    )

    sealed interface GeneratorEvent {
        data class OnLengthChanged(val length: Int) : GeneratorEvent
        data class OnIncludeUppercaseChanged(val include: Boolean) : GeneratorEvent
        data class OnIncludeLowercaseChanged(val include: Boolean) : GeneratorEvent
        data class OnIncludeNumbersChanged(val include: Boolean) : GeneratorEvent
        data class OnIncludeSymbolsChanged(val include: Boolean) : GeneratorEvent
        data class OnExcludeAmbiguousChanged(val exclude: Boolean) : GeneratorEvent
        data class OnWordCountChanged(val count: Int) : GeneratorEvent
        data class OnWordSeparatorChanged(val separator: String) : GeneratorEvent
        data object OnGenerateClick : GeneratorEvent
        data object OnCopyClick : GeneratorEvent
        data object OnUseClick : GeneratorEvent
        data object OnDismissError : GeneratorEvent
        data object OnPassphraseModeChanged : GeneratorEvent
    }

    sealed interface GeneratorEffect {
        data class CopyToClipboard(val password: String) : GeneratorEffect
        data class UsePassword(val password: String) : GeneratorEffect
    }

    enum class PasswordStrength {
        WEAK, FAIR, GOOD, STRONG, VERY_STRONG
    }

    companion object {
        private const val MIN_PASSWORD_LENGTH = 8
        private const val MAX_PASSWORD_LENGTH = 128
        private const val MIN_WORD_COUNT = 3
        private const val MAX_WORD_COUNT = 10
        private const val MAX_SEPARATOR_LENGTH = 4
        private const val UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        private const val LOWERCASE = "abcdefghijklmnopqrstuvwxyz"
        private const val NUMBERS = "0123456789"
        private const val SYMBOLS = "!@#$%^&*()_+-=[]{}|;:,.<>?"
        private const val AMBIGUOUS = "0O1lI"

        private val WORD_LIST = listOf(
            "apple", "banana", "cherry", "date", "elderberry", "fig", "grape", "honeydew",
            "kiwi", "lemon", "mango", "nectarine", "orange", "papaya", "quince", "raspberry",
            "strawberry", "tangerine", "ugli", "vanilla", "watermelon", "xigua", "yam", "zucchini",
            "alpha", "bravo", "charlie", "delta", "echo", "foxtrot", "golf", "hotel",
            "india", "juliet", "kilo", "lima", "mike", "november", "oscar", "papa",
            "quebec", "romeo", "sierra", "tango", "uniform", "victor", "whiskey", "xray",
            "yankee", "zulu", "red", "blue", "green", "yellow", "purple", "orange",
            "silver", "gold", "bronze", "white", "black", "pink", "brown", "gray",
            "cloud", "sky", "sun", "moon", "star", "rain", "snow", "wind",
            "fire", "earth", "water", "air", "forest", "ocean", "mountain", "river"
        )
    }
}
