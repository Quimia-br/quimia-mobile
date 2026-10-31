package com.quimia.android.data.notifications

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

internal const val SHELF_REMINDER_WORK = "quimia.shelf-review"

internal class ShelfReminderScheduler(context: Context) {
    private val work = WorkManager.getInstance(context.applicationContext)

    fun schedule(ownerId: String) {
        if (ownerId.isBlank()) return
        val request = PeriodicWorkRequestBuilder<ShelfReminderWorker>(7, TimeUnit.DAYS)
            .setInitialDelay(7, TimeUnit.DAYS)
            .setInputData(workDataOf(NOTIFICATION_OWNER to ownerId))
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
            .addTag(SHELF_REMINDER_WORK).build()
        // UPDATE preserves the cadence and replaces the owner when a different account signs in.
        work.enqueueUniquePeriodicWork(SHELF_REMINDER_WORK, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    fun cancel() { work.cancelAllWorkByTag(SHELF_REMINDER_WORK) }
}
