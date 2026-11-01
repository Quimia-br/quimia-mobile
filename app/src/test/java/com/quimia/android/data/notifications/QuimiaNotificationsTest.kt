package com.quimia.android.data.notifications

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import com.quimia.android.MainActivity
import com.quimia.android.domain.notifications.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class QuimiaNotificationsTest {
    private lateinit var context: Context
    private lateinit var manager: NotificationManager
    private lateinit var publisher: QuimiaNotifications

    @Before fun setup() {
        context = RuntimeEnvironment.getApplication()
        manager = context.getSystemService(NotificationManager::class.java)
        publisher = QuimiaNotifications(context)
        shadowOf(RuntimeEnvironment.getApplication()).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        manager.cancelAll()
    }

    @Test fun channelsSeparateOperationResultsFromQuietReminders() {
        publisher.createChannels()
        assertEquals(NotificationManager.IMPORTANCE_DEFAULT, manager.getNotificationChannel(MIXTURE_CHANNEL).importance)
        assertEquals(NotificationManager.IMPORTANCE_LOW, manager.getNotificationChannel(SHELF_CHANNEL).importance)
    }

    @Test fun noSystemNotificationIsPostedWithoutRuntimePermission() {
        shadowOf(RuntimeEnvironment.getApplication()).denyPermissions(Manifest.permission.POST_NOTIFICATIONS)
        assertFalse(publisher.post(mixtureResultNotification("owner", 17)))
        assertTrue(manager.activeNotifications.isEmpty())
    }

    @Test fun realResultPostsOnceAndUsesAnImmutableIntentToOpenHistory() {
        val message = mixtureResultNotification("owner", 17)
        assertTrue(publisher.post(message))
        assertTrue(publisher.post(message))
        val notification = manager.activeNotifications.single().notification
        assertEquals(message.title, notification.extras.getString(Notification.EXTRA_TITLE))
        assertEquals(Notification.VISIBILITY_PRIVATE, notification.visibility)
        assertEquals(MIXTURE_CHANNEL, notification.channelId)
        assertTrue(notification.flags and Notification.FLAG_ONLY_ALERT_ONCE != 0)
        assertEquals("Quimia", notification.publicVersion.extras.getString(Notification.EXTRA_TITLE))
        if (Build.VERSION.SDK_INT >= 31) assertTrue(notification.contentIntent.isImmutable)
        val intent = shadowOf(notification.contentIntent).savedIntent
        assertEquals(MainActivity::class.java.name, intent.component?.className)
        assertEquals(NotificationOpenRequest("owner", NotificationDestination.MixtureHistory), notificationOpenRequest(intent))
    }

    @Test fun reminderOpensShelfAndCanBeCancelledWithoutRemovingResults() {
        publisher.post(shelfReminderNotification("owner"))
        publisher.post(mixtureResultNotification("owner", 18))
        val reminder = manager.activeNotifications.first { it.notification.channelId == SHELF_CHANNEL }.notification
        assertEquals(NotificationDestination.Shelf, notificationOpenRequest(shadowOf(reminder.contentIntent).savedIntent)?.destination)
        publisher.cancel(NotificationKind.ShelfReminder)
        assertEquals(MIXTURE_CHANNEL, manager.activeNotifications.single().notification.channelId)
    }

    @Test fun aMutedChannelIsRespected() {
        manager.createNotificationChannel(NotificationChannel(MIXTURE_CHANNEL, "Muted", NotificationManager.IMPORTANCE_NONE))
        assertFalse(publisher.post(mixtureResultNotification("owner", 19)))
        assertTrue(manager.activeNotifications.isEmpty())
    }

    @Test fun malformedNavigationIsIgnoredInsteadOfOpeningAnArbitraryDestination() {
        assertNull(notificationOpenRequest(Intent()))
        assertNull(notificationOpenRequest(Intent(OPEN_NOTIFICATION).putExtra(NOTIFICATION_OWNER, "owner")
            .putExtra(NOTIFICATION_DESTINATION, "DeleteAccount")))
        assertNull(notificationOpenRequest(Intent(OPEN_NOTIFICATION).putExtra(NOTIFICATION_DESTINATION, "Shelf")))
    }
}
