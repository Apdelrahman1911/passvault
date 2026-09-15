package com.passvault.core.security

/**
 * Owns decrypted or otherwise sensitive UI state that must be cleared at the lock boundary.
 *
 * Implementations must clear synchronously, cancel work that could repopulate the state, and make
 * repeated calls safe. Navigation teardown remains a second cleanup path, not the lock boundary.
 */
interface EntrySensitiveStateOwner {
    fun clearForLock()
}
