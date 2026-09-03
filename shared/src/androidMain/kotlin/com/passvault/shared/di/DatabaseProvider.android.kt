package com.passvault.shared.di

import android.content.Context
import com.passvault.core.database.VaultDatabaseBootstrap

actual fun createDatabaseBootstrap(context: Any): VaultDatabaseBootstrap =
    com.passvault.core.database.createDatabaseBootstrap(context as Context)
