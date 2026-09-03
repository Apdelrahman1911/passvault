package com.passvault.shared.platform

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IosBiometricEnrollmentStateTest {
    @Test
    fun `Keychain presence restores a missing preferences marker`() {
        val fixture = Fixture(marked = false, accounts = setOf(ACTIVE_VAULT))

        assertTrue(fixture.state.contains(ACTIVE_VAULT).getOrThrow())

        assertTrue(fixture.marker.isMarked(ACTIVE_VAULT))
        assertEquals(listOf(ACTIVE_VAULT), fixture.keychain.probes)
    }

    @Test
    fun `missing Keychain item clears a stale preferences marker`() {
        val fixture = Fixture(marked = true)

        assertFalse(fixture.state.contains(ACTIVE_VAULT).getOrThrow())

        assertFalse(fixture.marker.isMarked(ACTIVE_VAULT))
    }

    @Test
    fun `failed deletion keeps marker and remains retryable`() {
        val fixture = Fixture(marked = true, accounts = setOf(ACTIVE_VAULT))
        fixture.keychain.deleteResults += Result.failure(IllegalStateException("temporarily unavailable"))
        fixture.keychain.deleteResults += Result.success(Unit)

        assertTrue(fixture.state.delete(ACTIVE_VAULT).isFailure)
        assertTrue(fixture.marker.isMarked(ACTIVE_VAULT))
        assertTrue(fixture.state.delete(ACTIVE_VAULT).isSuccess)

        assertFalse(fixture.marker.isMarked(ACTIVE_VAULT))
        assertEquals(listOf(ACTIVE_VAULT, ACTIVE_VAULT), fixture.keychain.deletions)
    }

    @Test
    fun `replacement keeps old enrollment visible until protected deletion succeeds`() {
        val fixture = Fixture(marked = true, accounts = setOf(ACTIVE_VAULT))
        fixture.keychain.deleteResults += Result.failure(IllegalStateException("temporarily unavailable"))
        var addCalls = 0

        val result = fixture.state.replace(ACTIVE_VAULT) {
            addCalls += 1
            Result.success(Unit)
        }

        assertTrue(result.isFailure)
        assertTrue(fixture.marker.isMarked(ACTIVE_VAULT))
        assertEquals(0, addCalls)
    }

    @Test
    fun `failed replacement add leaves no enabled marker`() {
        val fixture = Fixture(marked = true, accounts = setOf(ACTIVE_VAULT))
        fixture.keychain.deleteResults += Result.success(Unit)

        val result = fixture.state.replace(ACTIVE_VAULT) {
            assertFalse(fixture.marker.isMarked(ACTIVE_VAULT))
            Result.failure(IllegalStateException("add failed"))
        }

        assertTrue(result.isFailure)
        assertFalse(fixture.marker.isMarked(ACTIVE_VAULT))
    }

    @Test
    fun `successful replacement marks only after Keychain add`() {
        val fixture = Fixture(marked = true, accounts = setOf(ACTIVE_VAULT))
        fixture.keychain.deleteResults += Result.success(Unit)

        assertTrue(
            fixture.state.replace(ACTIVE_VAULT) {
                assertFalse(fixture.marker.isMarked(ACTIVE_VAULT))
                Result.success(Unit)
            }.isSuccess,
        )

        assertTrue(fixture.marker.isMarked(ACTIVE_VAULT))
    }

    @Test
    fun `reconciliation removes accounts that do not belong to the active vault`() {
        val fixture = Fixture(marked = false, accounts = setOf(ACTIVE_VAULT, ORPHAN_VAULT))
        fixture.marker.mark(ORPHAN_VAULT)

        assertTrue(fixture.state.contains(ACTIVE_VAULT).getOrThrow())

        assertEquals(listOf(ORPHAN_VAULT), fixture.keychain.deletions)
        assertFalse(fixture.marker.isMarked(ORPHAN_VAULT))
        assertTrue(fixture.keychain.accounts == setOf(ACTIVE_VAULT))
    }

    @Test
    fun `failed orphan cleanup is retried during the next reconciliation`() {
        val fixture = Fixture(marked = false, accounts = setOf(ACTIVE_VAULT, ORPHAN_VAULT))
        fixture.keychain.deleteResults += Result.failure(IllegalStateException("temporarily unavailable"))
        fixture.keychain.deleteResults += Result.success(Unit)

        assertTrue(fixture.state.contains(ACTIVE_VAULT).getOrThrow())
        assertTrue(fixture.state.contains(ACTIVE_VAULT).getOrThrow())

        assertEquals(listOf(ORPHAN_VAULT, ORPHAN_VAULT), fixture.keychain.deletions)
        assertTrue(fixture.keychain.accounts == setOf(ACTIVE_VAULT))
    }

    @Test
    fun `no active vault retires every surviving account from an earlier install`() {
        val fixture = Fixture(marked = false, accounts = setOf(ACTIVE_VAULT, ORPHAN_VAULT))
        fixture.marker.mark(ACTIVE_VAULT)
        fixture.marker.mark(ORPHAN_VAULT)

        assertTrue(fixture.state.reconcile(activeVaultId = null).isSuccess)

        assertEquals(1, fixture.keychain.deleteAllCalls)
        assertTrue(fixture.keychain.deletions.isEmpty())
        assertTrue(fixture.keychain.accounts.isEmpty())
        assertFalse(fixture.marker.isMarked(ACTIVE_VAULT))
        assertFalse(fixture.marker.isMarked(ORPHAN_VAULT))
    }

    @Test
    fun `failed no-vault cleanup remains retryable`() {
        val fixture = Fixture(marked = false, accounts = setOf(ORPHAN_VAULT))
        fixture.marker.mark(ORPHAN_VAULT)
        fixture.keychain.deleteAllResults += Result.failure(IllegalStateException("temporarily unavailable"))
        fixture.keychain.deleteAllResults += Result.success(Unit)

        assertTrue(fixture.state.reconcile(activeVaultId = null).isFailure)
        assertTrue(fixture.marker.isMarked(ORPHAN_VAULT))
        assertTrue(fixture.state.reconcile(activeVaultId = null).isSuccess)

        assertEquals(2, fixture.keychain.deleteAllCalls)
        assertTrue(fixture.keychain.accounts.isEmpty())
        assertFalse(fixture.marker.isMarked(ORPHAN_VAULT))
    }

    private class Fixture(
        marked: Boolean,
        accounts: Set<String> = emptySet(),
    ) {
        val marker = FakeMarker().apply {
            if (marked) mark(ACTIVE_VAULT)
        }
        val keychain = FakeKeychain(accounts.toMutableSet())
        val state = IosBiometricEnrollmentState(marker, keychain)
    }

    private class FakeMarker : IosBiometricEnrollmentMarker {
        private val vaultIds = mutableSetOf<String>()

        override fun mark(vaultId: String) {
            vaultIds += vaultId
        }

        override fun clear(vaultId: String) {
            vaultIds -= vaultId
        }

        fun isMarked(vaultId: String): Boolean = vaultId in vaultIds
    }

    private class FakeKeychain(
        val accounts: MutableSet<String>,
    ) : IosBiometricKeychainLifecycle {
        val probes = mutableListOf<String>()
        val deletions = mutableListOf<String>()
        val deleteResults = ArrayDeque<Result<Unit>>()
        val deleteAllResults = ArrayDeque<Result<Unit>>()
        var deleteAllCalls = 0

        override fun containsWithoutAuthentication(vaultId: String): Result<Boolean> {
            probes += vaultId
            return Result.success(vaultId in accounts)
        }

        override fun accountsWithoutAuthentication(): Result<Set<String>> = Result.success(accounts.toSet())

        override fun delete(vaultId: String): Result<Unit> {
            deletions += vaultId
            val result = deleteResults.removeFirstOrNull() ?: Result.success(Unit)
            if (result.isSuccess) accounts -= vaultId
            return result
        }

        override fun deleteAll(): Result<Unit> {
            deleteAllCalls += 1
            val result = deleteAllResults.removeFirstOrNull() ?: Result.success(Unit)
            if (result.isSuccess) accounts.clear()
            return result
        }
    }

    private companion object {
        const val ACTIVE_VAULT = "active-vault"
        const val ORPHAN_VAULT = "orphan-vault"
    }
}
