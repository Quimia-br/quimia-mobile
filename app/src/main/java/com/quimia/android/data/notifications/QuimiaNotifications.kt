package com.quimia.android.data.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.quimia.android.MainActivity
import com.quimia.android.R
import com.quimia.android.domain.notifications.*

internal const val MIXTURE_CHANNEL = "quimia.mixture-results"
internal const val SHELF_CHANNEL = "quimia.shelf-reminders"

internal class QuimiaNotifications(context: Context) : AppNotificationPublisher {
    private val context = context.applicationContext
    private val manager = this.context.getSystemService(NotificationManager::class.java)

    fun createChannels() {
        if (Build.VERSION.SDK_INT < 26) return
        manager.createNotificationChannels(listOf(
            NotificationChannel(MIXTURE_CHANNEL, "Resultados de misturas", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Avisos quando uma consulta de mistura feita no app é concluída."
            },
            NotificationChannel(SHELF_CHANNEL, "Lembrete semanal da Estante", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Um lembrete opcional por semana para revisar seus produtos."
            },
        ))
    }

    override fun isAvailable(): Boolean =
        (Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) &&
            NotificationManagerCompat.from(context).areNotificationsEnabled()

    fun canPost(kind: NotificationKind): Boolean {
        if (!isAvailable()) return false
        if (Build.VERSION.SDK_INT < 26) return true
        createChannels()
        return manager.getNotificationChannel(channel(kind))?.importance != NotificationManager.IMPORTANCE_NONE
    }

    override fun post(notification: AppNotification): Boolean {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return false
        if (!canPost(notification.kind)) return false
        val tag = "${notification.kind.name}:${notification.ownerId.hashCode()}"
        val intent = Intent(context, MainActivity::class.java).apply {
            action = OPEN_NOTIFICATION
            data = Uri.parse("quimia://notification/${notification.kind.name}/${notification.ownerId.hashCode()}/${notification.id}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(NOTIFICATION_OWNER, notification.ownerId)
            putExtra(NOTIFICATION_DESTINATION, notification.destination.name)
        }
        val pending = PendingIntent.getActivity(context, "$tag:${notification.id}".hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val public = NotificationCompat.Builder(context, channel(notification.kind))
            .setSmallIcon(R.drawable.ic_notification_quimia).setContentTitle("Quimia")
            .setContentText("Você tem uma atualização no app.").build()
        val builder = NotificationCompat.Builder(context, channel(notification.kind))
            .setSmallIcon(R.drawable.ic_notification_quimia).setColor(Color.rgb(77, 217, 229))
            .setContentTitle(notification.title).setContentText(notification.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notification.body))
            .setContentIntent(pending).setAutoCancel(true).setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE).setPublicVersion(public)
            .setCategory(if (notification.kind == NotificationKind.ShelfReminder) NotificationCompat.CATEGORY_REMINDER else NotificationCompat.CATEGORY_STATUS)
            .setPriority(if (notification.kind == NotificationKind.ShelfReminder) NotificationCompat.PRIORITY_LOW else NotificationCompat.PRIORITY_DEFAULT)
        return try {
            manager.notify(tag, notification.id, builder.build())
            true
        } catch (_: SecurityException) { false }
    }

    fun cancel(kind: NotificationKind) {
        manager.activeNotifications.filter { it.tag?.startsWith("${kind.name}:") == true }.forEach { manager.cancel(it.tag, it.id) }
    }

    fun cancelAll() = manager.cancelAll()

    fun settingsIntent() = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    private fun channel(kind: NotificationKind) = if (kind == NotificationKind.ShelfReminder) SHELF_CHANNEL else MIXTURE_CHANNEL
}
