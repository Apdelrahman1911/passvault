package com.passvault.shared.di

/** Platform-neutral shutdown boundary for the application-owned Room database. */
fun interface AppDatabaseLifecycle {
    suspend fun close(): Result<Unit>
}
