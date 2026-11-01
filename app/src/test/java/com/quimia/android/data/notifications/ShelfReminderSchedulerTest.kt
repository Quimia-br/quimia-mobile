package com.quimia.android.data.notifications

import android.content.Context
import androidx.work.Configuration
import androidx.work.NetworkType
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ShelfReminderSchedulerTest {
    private lateinit var context: Context
    private lateinit var manager: WorkManager

    @Before fun setup() {
        context = RuntimeEnvironment.getApplication()
        val executor = SynchronousExecutor()
        WorkManagerTestInitHelper.initializeTestWorkManager(context,
            Configuration.Builder().setExecutor(executor).setTaskExecutor(executor).build())
        manager = WorkManager.getInstance(context)
    }

    @After fun cleanup() { WorkManagerTestInitHelper.closeWorkDatabase() }

    @Test fun openingTheAppRepeatedlyKeepsASingleWeeklyNetworkConstrainedJob() {
        val scheduler = ShelfReminderScheduler(context)
        scheduler.schedule("owner")
        scheduler.schedule("owner")
        val info = manager.getWorkInfosForUniqueWork(SHELF_REMINDER_WORK).get(5, TimeUnit.SECONDS).single()
        assertEquals(TimeUnit.DAYS.toMillis(7), info.periodicityInfo?.repeatIntervalMillis)
        assertEquals(TimeUnit.DAYS.toMillis(7), info.initialDelayMillis)
        assertEquals(NetworkType.CONNECTED, info.constraints.requiredNetworkType)
    }

    @Test fun disablingRemindersCancelsTheScheduledWork() {
        val scheduler = ShelfReminderScheduler(context)
        scheduler.schedule("owner")
        scheduler.cancel()
        assertEquals(WorkInfo.State.CANCELLED,
            manager.getWorkInfosForUniqueWork(SHELF_REMINDER_WORK).get(5, TimeUnit.SECONDS).single().state)
    }
}
