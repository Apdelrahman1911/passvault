package com.passvault.core.database.backup

/** Parsed representation of the exact legacy-v1 envelope emitted by PassVault. */
internal data class LegacyBackupEnvelope(
    val formatVersion: Int,
    val kdfAlgorithm: String,
    val argon2OpsLimit: Int,
    val argon2MemLimit: Int,
    val argon2Parallelism: Int,
    val salt: ByteArray,
    val nonce: ByteArray,
    val ciphertext: ByteArray,
) {
    fun clear() {
        salt.fill(0)
        nonce.fill(0)
        ciphertext.fill(0)
    }
}

/**
 * Reads v1 without retaining the JSON document or materialising its ciphertext as a String.
 *
 * Property order is deliberately the order used by every shipped v1 writer. Backups are opaque
 * application artifacts rather than a general JSON interchange format; requiring their canonical
 * shape lets malformed input fail before allocating the only large field.
 */
internal class LegacyBackupEnvelopeReader(
    source: BackupContentSource,
    prefix: ByteArray,
) {
    private val input = BoundedByteReader(source, prefix)

    @Suppress("LongMethod") // Linear parsing keeps allocation admission visibly ahead of ciphertext decoding.
    suspend fun read(): LegacyBackupEnvelope {
        var salt: ByteArray? = null
        var nonce: ByteArray? = null
        var ciphertext: ByteArray? = null
        var transferred = false
        try {
            input.skipWhitespace()
            input.expect('{'.code)

            input.expectProperty("formatVersion")
            val formatVersion = input.readInt()
            input.expectPropertySeparator()

            input.expectProperty("kdfAlgorithm")
            val kdfAlgorithm = input.readAsciiString(MAX_ALGORITHM_CHARS)
            input.expectPropertySeparator()

            input.expectProperty("argon2OpsLimit")
            val argon2OpsLimit = input.readInt()
            input.expectPropertySeparator()

            input.expectProperty("argon2MemLimit")
            val argon2MemLimit = input.readInt()
            input.expectPropertySeparator()

            input.expectProperty("argon2Parallelism")
            val argon2Parallelism = input.readInt()
            input.expectPropertySeparator()

            input.expectProperty("salt")
            salt = input.readBase64(MAX_SALT_BYTES)
            input.expectPropertySeparator()

            input.expectProperty("nonce")
            nonce = input.readBase64(MAX_NONCE_BYTES)
            input.expectPropertySeparator()

            // Validate every small, attacker-controlled header field before admitting the
            // potentially large ciphertext allocation.
            require(formatVersion == LEGACY_FORMAT_VERSION)
            require(kdfAlgorithm == LEGACY_KDF_ALGORITHM)
            require(argon2OpsLimit in MIN_ARGON2_OPS..MAX_ARGON2_OPS)
            require(argon2MemLimit in MIN_ARGON2_MEM..MAX_ARGON2_MEM)
            require(argon2Parallelism == SUPPORTED_ARGON2_PARALLELISM)
            require(salt.size == ARGON2_SALT_BYTES)
            require(nonce.size == XCHACHA_NONCE_BYTES)

            input.expectProperty("ciphertext")
            ciphertext = input.readBase64(BackupLimits.LEGACY_MAX_CIPHERTEXT_BYTES)
            require(ciphertext.size >= MIN_ENCRYPTED_BYTES)

            input.skipWhitespace()
            input.expect('}'.code)
            input.skipWhitespace()
            require(input.readByte() == END_OF_INPUT)

            transferred = true
            return LegacyBackupEnvelope(
                formatVersion = formatVersion,
                kdfAlgorithm = kdfAlgorithm,
                argon2OpsLimit = argon2OpsLimit,
                argon2MemLimit = argon2MemLimit,
                argon2Parallelism = argon2Parallelism,
                salt = salt,
                nonce = nonce,
                ciphertext = ciphertext,
            )
        } finally {
            input.clear()
            if (!transferred) {
                salt?.fill(0)
                nonce?.fill(0)
                ciphertext?.fill(0)
            }
        }
    }

    private class BoundedByteReader(
        private val source: BackupContentSource,
        private val prefix: ByteArray,
    ) {
        private val buffer = ByteArray(INPUT_BUFFER_BYTES)
        private var prefixOffset = 0
        private var bufferOffset = 0
        private var bufferLimit = 0
        private var loadedBytes = prefix.size.toLong()
        private var pushedBack: Int? = null

        init {
            source.declaredSizeBytes?.let { require(it in 1..BackupLimits.LEGACY_MAX_BACKUP_BYTES) }
            require(loadedBytes <= BackupLimits.LEGACY_MAX_BACKUP_BYTES)
        }

        suspend fun readByte(): Int {
            var result = pushedBack
            if (result != null) {
                pushedBack = null
            } else if (prefixOffset < prefix.size) {
                result = prefix[prefixOffset++].toInt() and BYTE_MASK
            } else {
                if (bufferOffset >= bufferLimit) {
                    val count = source.read(buffer)
                    if (count == END_OF_INPUT) {
                        result = END_OF_INPUT
                    } else {
                        require(count in 1..buffer.size)
                        loadedBytes += count
                        require(loadedBytes <= BackupLimits.LEGACY_MAX_BACKUP_BYTES)
                        bufferOffset = 0
                        bufferLimit = count
                    }
                }
                if (result == null) result = buffer[bufferOffset++].toInt() and BYTE_MASK
            }
            return result
        }

        suspend fun skipWhitespace() {
            while (true) {
                when (val value = readByte()) {
                    ' '.code, '\t'.code, '\r'.code, '\n'.code -> Unit
                    else -> {
                        pushBack(value)
                        return
                    }
                }
            }
        }

        suspend fun expect(expected: Int) {
            require(readByte() == expected)
        }

        suspend fun expectProperty(expectedName: String) {
            skipWhitespace()
            require(readAsciiString(MAX_PROPERTY_NAME_CHARS) == expectedName)
            skipWhitespace()
            expect(':'.code)
            skipWhitespace()
        }

        suspend fun expectPropertySeparator() {
            skipWhitespace()
            expect(','.code)
        }

        suspend fun readInt(): Int {
            val value = StringBuilder(MAX_INTEGER_CHARS)
            var next = readByte()
            if (next == '-'.code) {
                value.append('-')
                next = readByte()
            }
            require(next in '0'.code..'9'.code)
            value.append(next.toChar())
            if (next == '0'.code) {
                val following = readByte()
                require(following !in '0'.code..'9'.code)
                pushBack(following)
            } else {
                while (true) {
                    next = readByte()
                    if (next !in '0'.code..'9'.code) {
                        pushBack(next)
                        break
                    }
                    require(value.length < MAX_INTEGER_CHARS)
                    value.append(next.toChar())
                }
            }
            return requireNotNull(value.toString().toIntOrNull())
        }

        suspend fun readAsciiString(maxChars: Int): String {
            val output = StringBuilder(minOf(maxChars, INITIAL_STRING_CAPACITY))
            readJsonString(maxChars) { output.append(it.toChar()) }
            return output.toString()
        }

        @Suppress("TooGenericExceptionCaught") // Wipe decoder storage on cancellation and fatal parser failures too.
        suspend fun readBase64(maxBytes: Int): ByteArray {
            val decoder = StreamingBase64Decoder(maxBytes)
            return try {
                readJsonString(encodedBase64Length(maxBytes), decoder::accept)
                decoder.finish()
            } catch (error: Throwable) {
                decoder.clear()
                throw error
            }
        }

        private suspend fun readJsonString(maxChars: Int, consume: (Int) -> Unit) {
            expect('"'.code)
            var count = 0
            while (true) {
                val value = readByte()
                require(value != END_OF_INPUT && value >= ASCII_SPACE)
                if (value == '"'.code) return
                val decoded = if (value == '\\'.code) readEscape() else value
                require(decoded in ASCII_SPACE..ASCII_MAX)
                count++
                require(count <= maxChars)
                consume(decoded)
            }
        }

        private suspend fun readEscape(): Int = when (val escaped = readByte()) {
            '"'.code, '\\'.code, '/'.code -> escaped
            'b'.code -> '\b'.code
            'f'.code -> 0x0C
            'n'.code -> '\n'.code
            'r'.code -> '\r'.code
            't'.code -> '\t'.code
            'u'.code -> {
                var value = 0
                repeat(4) {
                    value = (value shl 4) or hexValue(readByte())
                }
                value
            }
            else -> throw IllegalArgumentException("Invalid JSON escape")
        }

        private fun pushBack(value: Int) {
            check(pushedBack == null)
            pushedBack = value
        }

        fun clear() {
            prefix.fill(0)
            buffer.fill(0)
        }
    }

    private class StreamingBase64Decoder(private val maxBytes: Int) {
        private var output = ByteArray(minOf(INITIAL_DECODED_CAPACITY, maxBytes))
        private val quartet = IntArray(4)
        private var quartetSize = 0
        private var outputSize = 0
        private var padded = false
        private var transferred = false

        fun accept(character: Int) {
            check(!transferred)
            require(!padded)
            quartet[quartetSize++] = if (character == '='.code) PADDING else base64Value(character)
            if (quartetSize == quartet.size) decodeQuartet()
        }

        fun finish(): ByteArray {
            check(!transferred)
            when (quartetSize) {
                0 -> Unit
                2 -> {
                    require(quartet[0] >= 0 && quartet[1] >= 0 && quartet[1] and 0x0F == 0)
                    append((quartet[0] shl 2) or (quartet[1] shr 4))
                }
                3 -> {
                    require(quartet[0] >= 0 && quartet[1] >= 0 && quartet[2] >= 0)
                    require(quartet[2] and 0x03 == 0)
                    append((quartet[0] shl 2) or (quartet[1] shr 4))
                    append((quartet[1] shl 4) or (quartet[2] shr 2))
                }
                else -> throw IllegalArgumentException("Invalid Base64 length")
            }
            require(outputSize > 0)
            val result = if (outputSize == output.size) output else output.copyOf(outputSize).also { output.fill(0) }
            transferred = true
            output = EMPTY_BYTES
            return result
        }

        private fun decodeQuartet() {
            require(quartet[0] >= 0 && quartet[1] >= 0)
            when {
                quartet[2] == PADDING -> {
                    require(quartet[3] == PADDING && quartet[1] and 0x0F == 0)
                    append((quartet[0] shl 2) or (quartet[1] shr 4))
                    padded = true
                }
                quartet[3] == PADDING -> {
                    require(quartet[2] >= 0 && quartet[2] and 0x03 == 0)
                    append((quartet[0] shl 2) or (quartet[1] shr 4))
                    append((quartet[1] shl 4) or (quartet[2] shr 2))
                    padded = true
                }
                else -> {
                    require(quartet[2] >= 0 && quartet[3] >= 0)
                    append((quartet[0] shl 2) or (quartet[1] shr 4))
                    append((quartet[1] shl 4) or (quartet[2] shr 2))
                    append((quartet[2] shl 6) or quartet[3])
                }
            }
            quartetSize = 0
        }

        private fun append(value: Int) {
            require(outputSize < maxBytes)
            if (outputSize == output.size) {
                val nextSize = minOf(maxBytes, maxOf(output.size * 2, outputSize + 1))
                val previous = output
                output = previous.copyOf(nextSize)
                previous.fill(0)
            }
            output[outputSize++] = value.toByte()
        }

        fun clear() {
            output.fill(0)
            quartet.fill(0)
            output = EMPTY_BYTES
        }
    }

    private companion object {
        const val LEGACY_FORMAT_VERSION = 1
        const val LEGACY_KDF_ALGORITHM = "Argon2id"
        const val ARGON2_SALT_BYTES = 16
        const val XCHACHA_NONCE_BYTES = 24
        const val MIN_ENCRYPTED_BYTES = 4 + 16
        const val MIN_ARGON2_OPS = 2
        const val MAX_ARGON2_OPS = 10
        const val MIN_ARGON2_MEM = 32 * 1024 * 1024
        const val MAX_ARGON2_MEM = 256 * 1024 * 1024
        const val SUPPORTED_ARGON2_PARALLELISM = 1
        const val MAX_SALT_BYTES = 64
        const val MAX_NONCE_BYTES = 64
        const val MAX_ALGORITHM_CHARS = 32
        const val MAX_PROPERTY_NAME_CHARS = 64
        const val MAX_INTEGER_CHARS = 16
        const val INITIAL_STRING_CAPACITY = 64
        const val INPUT_BUFFER_BYTES = 8 * 1024
        const val INITIAL_DECODED_CAPACITY = 4 * 1024
        const val ASCII_SPACE = 0x20
        const val ASCII_MAX = 0x7F
        const val BYTE_MASK = 0xFF
        const val END_OF_INPUT = -1
        const val PADDING = -1
        val EMPTY_BYTES = ByteArray(0)

        fun encodedBase64Length(byteCount: Int): Int = ((byteCount + 2) / 3) * 4

        fun hexValue(value: Int): Int = when (value) {
            in '0'.code..'9'.code -> value - '0'.code
            in 'a'.code..'f'.code -> value - 'a'.code + 10
            in 'A'.code..'F'.code -> value - 'A'.code + 10
            else -> throw IllegalArgumentException("Invalid JSON unicode escape")
        }

        fun base64Value(value: Int): Int = when (value) {
            in 'A'.code..'Z'.code -> value - 'A'.code
            in 'a'.code..'z'.code -> value - 'a'.code + 26
            in '0'.code..'9'.code -> value - '0'.code + 52
            '+'.code -> 62
            '/'.code -> 63
            else -> throw IllegalArgumentException("Invalid Base64 character")
        }
    }
}
