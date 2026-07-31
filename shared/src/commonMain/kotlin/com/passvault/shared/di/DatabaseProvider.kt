package com.passvault.shared.di

import com.passvault.core.database.VaultDatabase

/**
 * Platform-specific database creation.
 */
expect fun createDatabase(context: Any): VaultDatabase
