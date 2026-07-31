package com.passvault.core.crypto

/**
 * Desktop compatibility name for the active shared libsodium adapter.
 *
 * All supported platforms use the same vault format and primitive set.
 */
class DesktopCryptoEngine : CryptoEngine by LibsodiumCryptoEngine()
