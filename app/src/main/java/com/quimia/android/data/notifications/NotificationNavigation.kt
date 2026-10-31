package com.quimia.android.data.notifications

import android.content.Intent
import com.quimia.android.domain.notifications.NotificationDestination

internal const val OPEN_NOTIFICATION = "com.quimia.android.OPEN_NOTIFICATION"
internal const val NOTIFICATION_OWNER = "notification_owner"
internal const val NOTIFICATION_DESTINATION = "notification_destination"

internal data class NotificationOpenRequest(val ownerId: String, val destination: NotificationDestination)

internal fun notificationOpenRequest(intent: Intent?): NotificationOpenRequest? {
    if (intent?.action != OPEN_NOTIFICATION) return null
    val ownerId = intent.getStringExtra(NOTIFICATION_OWNER)?.takeIf { it.isNotBlank() } ?: return null
    val destination = NotificationDestination.entries.firstOrNull { it.name == intent.getStringExtra(NOTIFICATION_DESTINATION) }
        ?: return null
    return NotificationOpenRequest(ownerId, destination)
}
