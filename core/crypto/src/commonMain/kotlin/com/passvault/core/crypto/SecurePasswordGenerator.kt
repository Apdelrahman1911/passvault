package com.passvault.core.crypto

import kotlin.coroutines.cancellation.CancellationException

data class PasswordGenerationOptions(
    val length: Int = DEFAULT_PASSWORD_LENGTH,
    val includeUppercase: Boolean = true,
    val includeLowercase: Boolean = true,
    val includeNumbers: Boolean = true,
    val includeSymbols: Boolean = true,
    val excludeAmbiguous: Boolean = false,
) {
    companion object {
        const val DEFAULT_PASSWORD_LENGTH = 16
        const val MIN_PASSWORD_LENGTH = 8
        const val MAX_PASSWORD_LENGTH = 128
    }
}

interface PasswordGenerator {
    suspend fun generate(options: PasswordGenerationOptions = PasswordGenerationOptions()): Result<String>
}

/** Cryptographically secure password generation shared by the generator and credential editor. */
class SecurePasswordGenerator(
    private val cryptoEngine: CryptoEngine,
) : PasswordGenerator {
    @Suppress("TooGenericExceptionCaught") // Result boundary preserves arbitrary ordinary crypto failures.
    override suspend fun generate(options: PasswordGenerationOptions): Result<String> =
        try {
            require(options.length in PasswordGenerationOptions.MIN_PASSWORD_LENGTH..
                PasswordGenerationOptions.MAX_PASSWORD_LENGTH)
            val selectedSets = options.selectedCharacterSets()
            require(selectedSets.isNotEmpty())

            val allCharacters = selectedSets.joinToString("")
            val safeLength = options.length.coerceAtLeast(selectedSets.size)
            val output = MutableList(safeLength) { '\u0000' }
            try {
                output.indices.forEach { index ->
                    output[index] = allCharacters[secureRandomIndex(allCharacters.length)]
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
                Result.success(output.joinToString(""))
            } finally {
                output.indices.forEach { output[it] = '\u0000' }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            Result.failure(error)
        }

    private suspend fun secureRandomIndex(bound: Int): Int {
        require(bound in 1..MAX_RANDOM_BOUND)
        val rejectionLimit = RANDOM_RANGE - (RANDOM_RANGE % bound)
        while (true) {
            val bytes = cryptoEngine.generateRandom(RANDOM_BYTE_COUNT).getOrThrow()
            try {
                val value = ((bytes[0].toInt() and BYTE_MASK) shl BITS_PER_BYTE) or
                    (bytes[1].toInt() and BYTE_MASK)
                if (value < rejectionLimit) return value % bound
            } finally {
                cryptoEngine.secureWipe(bytes)
            }
        }
    }
}

private const val UPPERCASE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
private const val LOWERCASE_CHARACTERS = "abcdefghijklmnopqrstuvwxyz"
private const val NUMBER_CHARACTERS = "0123456789"
private const val SYMBOL_CHARACTERS = "!@#$%^&*()_+-=[]{}|;:,.<>?"
private const val AMBIGUOUS_CHARACTERS = "0O1lI"
private const val MAX_RANDOM_BOUND = 65_535
private const val RANDOM_RANGE = 65_536
private const val RANDOM_BYTE_COUNT = 2
private const val BITS_PER_BYTE = 8
private const val BYTE_MASK = 0xff

private fun PasswordGenerationOptions.selectedCharacterSets(): List<String> = buildList {
    if (includeLowercase) add(LOWERCASE_CHARACTERS)
    if (includeUppercase) add(UPPERCASE_CHARACTERS)
    if (includeNumbers) add(NUMBER_CHARACTERS)
    if (includeSymbols) add(SYMBOL_CHARACTERS)
}.map { characters ->
    if (excludeAmbiguous) characters.filterNot { it in AMBIGUOUS_CHARACTERS } else characters
}.filter(String::isNotEmpty)
