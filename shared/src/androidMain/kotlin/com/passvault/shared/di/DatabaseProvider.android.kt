package com.passvault.shared.di

import android.content.Context
import com.passvault.core.database.VaultDatabase

actual fun createDatabase(context: Any): VaultDatabase =
    com.passvault.core.database.createDatabase(context as Context)
