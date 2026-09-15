package com.passvault.shared.di

import com.passvault.core.database.VaultDatabaseBootstrap

@Suppress("UNUSED_PARAMETER")
actual fun createDatabaseBootstrap(context: Any): VaultDatabaseBootstrap =
    com.passvault.core.database.createDatabaseBootstrap()
