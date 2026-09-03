package com.passvault.android.attachment

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.os.PersistableBundle
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Process-independent recovery for plaintext attachment leases. The service
 * name is retained so preview jobs scheduled by an older build remain valid.
 */
class AndroidAttachmentPreviewCleanupJobService : JobService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val runningJobs = ConcurrentHashMap<Int, Job>()

    override fun onStartJob(params: JobParameters): Boolean {
        val operationId = params.extras.getString(EXTRA_OPERATION_ID)
        val cacheRoot = persistedAttachmentPlaintextRoot(params.extras.getString(EXTRA_CACHE_ROOT))
        if (!operationId.isValidAttachmentOperationId() || cacheRoot == null) return false
        runningJobs[params.jobId] = scope.launch {
            val shouldRetry = runCatching {
                cleanupAttachmentPlaintextOperation(cacheDir, cacheRoot, checkNotNull(operationId))
            }.isFailure
            runningJobs.remove(params.jobId)
            jobFinished(params, shouldRetry)
        }
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        runningJobs.remove(params.jobId)?.cancel()
        return true
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}

internal class AndroidAttachmentPlaintextCleanupScheduler(
    private val context: Context,
) {
    private val scheduler: JobScheduler
        get() = checkNotNull(context.getSystemService(JobScheduler::class.java))

    @Synchronized
    fun schedule(cacheRoot: AttachmentPlaintextCacheRoot, operationId: String): Int? {
        require(operationId.isValidAttachmentOperationId())
        val component = ComponentName(context, AndroidAttachmentPreviewCleanupJobService::class.java)
        val pendingIds = scheduler.allPendingJobs
            .asSequence()
            .filter { pending -> pending.service == component }
            .mapTo(mutableSetOf()) { pending -> pending.id }
        var jobId = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .getInt(NEXT_JOB_ID_KEY, PLAINTEXT_JOB_ID_MIN)
            .takeIf(::isAttachmentPlaintextCleanupJobId)
            ?: PLAINTEXT_JOB_ID_MIN
        while (jobId in pendingIds) jobId = nextAttachmentPlaintextCleanupJobId(jobId)
        val extras = PersistableBundle().apply {
            putString(EXTRA_OPERATION_ID, operationId)
            putString(EXTRA_CACHE_ROOT, cacheRoot.persistedValue)
        }
        val job = JobInfo.Builder(
            jobId,
            component,
        )
            .setMinimumLatency(cacheRoot.minimumLifetimeMilliseconds)
            .setOverrideDeadline(cacheRoot.cleanupDeadlineMilliseconds)
            .setPersisted(true)
            .setExtras(extras)
            .build()
        if (scheduler.schedule(job) != JobScheduler.RESULT_SUCCESS) return null
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(NEXT_JOB_ID_KEY, nextAttachmentPlaintextCleanupJobId(jobId))
            .apply()
        return jobId
    }

    fun cancel(jobId: Int) {
        if (isAttachmentPlaintextCleanupJobId(jobId)) scheduler.cancel(jobId)
    }
}

internal enum class AttachmentPlaintextCacheRoot(
    val persistedValue: String,
    val directoryName: String,
    val minimumLifetimeMilliseconds: Long,
    val cleanupDeadlineMilliseconds: Long,
) {
    PREVIEW(
        persistedValue = "preview",
        directoryName = "attachment-previews",
        minimumLifetimeMilliseconds = PREVIEW_LIFETIME_MILLISECONDS,
        cleanupDeadlineMilliseconds = PREVIEW_CLEANUP_DEADLINE_MILLISECONDS,
    ),
    EXPORT(
        persistedValue = "export",
        directoryName = "attachment-exports",
        minimumLifetimeMilliseconds = EXPORT_STAGING_LIFETIME_MILLISECONDS,
        cleanupDeadlineMilliseconds = EXPORT_STAGING_CLEANUP_DEADLINE_MILLISECONDS,
    ),
}

internal fun persistedAttachmentPlaintextRoot(value: String?): AttachmentPlaintextCacheRoot? =
    if (value == null) {
        // Jobs created before export leases existed did not persist a root and
        // always referred to the preview tree.
        AttachmentPlaintextCacheRoot.PREVIEW
    } else {
        AttachmentPlaintextCacheRoot.entries.singleOrNull { root -> root.persistedValue == value }
    }

internal fun nextAttachmentPlaintextCleanupJobId(current: Int): Int {
    require(isAttachmentPlaintextCleanupJobId(current))
    return if (current == PLAINTEXT_JOB_ID_MAX) PLAINTEXT_JOB_ID_MIN else current + 1
}

internal fun isAttachmentPlaintextCleanupJobId(jobId: Int): Boolean =
    jobId in PLAINTEXT_JOB_ID_MIN..PLAINTEXT_JOB_ID_MAX

internal fun String?.isValidAttachmentOperationId(): Boolean = runCatching {
    this != null && UUID.fromString(this).toString() == this
}.getOrDefault(false)

internal const val PREVIEW_CLEANUP_DEADLINE_MILLISECONDS = 90_000L
internal const val PREVIEW_LIFETIME_MILLISECONDS = 60_000L
internal const val EXPORT_STAGING_LIFETIME_MILLISECONDS = 60_000L
internal const val EXPORT_STAGING_CLEANUP_DEADLINE_MILLISECONDS = 90_000L
private const val EXTRA_OPERATION_ID = "preview_operation_id"
private const val EXTRA_CACHE_ROOT = "attachment_plaintext_root"
// Retain the preference identity so upgrades continue the existing job-ID sequence.
private const val PREFERENCES_NAME = "attachment_preview_cleanup"
private const val NEXT_JOB_ID_KEY = "next_job_id"
internal const val PLAINTEXT_JOB_ID_MIN = 0x5A00_0000
internal const val PLAINTEXT_JOB_ID_MAX = 0x5A00_FFFF
