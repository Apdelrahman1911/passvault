package com.passvault.shared.di

import com.passvault.core.database.VaultDatabase

@Suppress("UNUSED_PARAMETER")
actual fun createDatabase(context: Any): VaultDatabase = com.passvault.core.database.createDatabase()
