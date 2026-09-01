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
 * Process-independent recovery for plaintext preview leases. JobScheduler
 * persists accepted jobs across ordinary process death and device reboot.
 */
class AndroidAttachmentPreviewCleanupJobService : JobService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val runningJobs = ConcurrentHashMap<Int, Job>()

    override fun onStartJob(params: JobParameters): Boolean {
        val operationId = params.extras.getString(EXTRA_OPERATION_ID)
        if (!operationId.isValidPreviewOperationId()) return false
        runningJobs[params.jobId] = scope.launch {
            val shouldRetry = runCatching {
                cleanupAttachmentPreviewOperation(cacheDir, checkNotNull(operationId))
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

internal class AndroidAttachmentPreviewCleanupScheduler(
    private val context: Context,
) {
    private val scheduler: JobScheduler
        get() = checkNotNull(context.getSystemService(JobScheduler::class.java))

    @Synchronized
    fun schedule(operationId: String): Int? {
        require(operationId.isValidPreviewOperationId())
        val component = ComponentName(context, AndroidAttachmentPreviewCleanupJobService::class.java)
        val pendingIds = scheduler.allPendingJobs
            .asSequence()
            .filter { pending -> pending.service == component }
            .mapTo(mutableSetOf()) { pending -> pending.id }
        var jobId = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .getInt(NEXT_JOB_ID_KEY, PREVIEW_JOB_ID_MIN)
            .takeIf(::isPreviewCleanupJobId)
            ?: PREVIEW_JOB_ID_MIN
        while (jobId in pendingIds) jobId = nextPreviewCleanupJobId(jobId)
        val extras = PersistableBundle().apply { putString(EXTRA_OPERATION_ID, operationId) }
        val job = JobInfo.Builder(
            jobId,
            component,
        )
            .setMinimumLatency(PREVIEW_LIFETIME_MILLISECONDS)
            .setOverrideDeadline(PREVIEW_CLEANUP_DEADLINE_MILLISECONDS)
            .setPersisted(true)
            .setExtras(extras)
            .build()
        if (scheduler.schedule(job) != JobScheduler.RESULT_SUCCESS) return null
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(NEXT_JOB_ID_KEY, nextPreviewCleanupJobId(jobId))
            .apply()
        return jobId
    }

    fun cancel(jobId: Int) {
        if (isPreviewCleanupJobId(jobId)) scheduler.cancel(jobId)
    }

    fun cancelAll() {
        val component = ComponentName(context, AndroidAttachmentPreviewCleanupJobService::class.java)
        scheduler.allPendingJobs
            .filter { pending -> pending.service == component }
            .forEach { pending -> scheduler.cancel(pending.id) }
    }
}

internal fun nextPreviewCleanupJobId(current: Int): Int {
    require(isPreviewCleanupJobId(current))
    return if (current == PREVIEW_JOB_ID_MAX) PREVIEW_JOB_ID_MIN else current + 1
}

internal fun isPreviewCleanupJobId(jobId: Int): Boolean = jobId in PREVIEW_JOB_ID_MIN..PREVIEW_JOB_ID_MAX

private fun String?.isValidPreviewOperationId(): Boolean = runCatching {
    this != null && UUID.fromString(this).toString() == this
}.getOrDefault(false)

internal const val PREVIEW_CLEANUP_DEADLINE_MILLISECONDS = 90_000L
private const val EXTRA_OPERATION_ID = "preview_operation_id"
private const val PREFERENCES_NAME = "attachment_preview_cleanup"
private const val NEXT_JOB_ID_KEY = "next_job_id"
internal const val PREVIEW_JOB_ID_MIN = 0x5A00_0000
internal const val PREVIEW_JOB_ID_MAX = 0x5A00_FFFF
