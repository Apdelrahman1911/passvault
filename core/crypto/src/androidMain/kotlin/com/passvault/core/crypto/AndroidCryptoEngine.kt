package com.passvault.core.crypto

/**
 * Android compatibility name for the active shared libsodium adapter.
 *
 * Keeping this class as a delegate preserves source compatibility while
 * ensuring Android uses the same reviewed libsodium implementation as the
 * other supported targets.
 */
class AndroidCryptoEngine : CryptoEngine by LibsodiumCryptoEngine()
