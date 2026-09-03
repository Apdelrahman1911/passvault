package com.passvault.shared.di

import com.passvault.core.database.VaultDatabaseBootstrap

/** Creates the platform database bootstrap and recovery boundary. */
expect fun createDatabaseBootstrap(context: Any): VaultDatabaseBootstrap
