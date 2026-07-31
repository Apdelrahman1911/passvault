package com.passvault.core.database.repository

import kotlinx.coroutines.CancellationException

/**
 * Result boundary for repository operations.
 *
 * Coroutine cancellation and VM-fatal errors are control flow, not domain
 * failures. Only ordinary exceptions are converted to [Result.failure].
 */
internal suspend inline fun <T> repositoryResult(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (cancel: CancellationException) {
        throw cancel
    } catch (error: Exception) {
        Result.failure(error)
    }
