package com.quimia.android.domain.notifications

enum class NotificationDestination { Home, Shelf, MixtureHistory }
enum class NotificationKind { MixtureResult, ShelfReminder, Test }

data class AppNotification(
    val kind: NotificationKind,
    val ownerId: String,
    val id: Int,
    val title: String,
    val body: String,
    val destination: NotificationDestination,
)

interface AppNotificationPublisher {
    fun isAvailable(): Boolean
    fun post(notification: AppNotification): Boolean
}

fun mixtureResultNotification(ownerId: String, mixtureId: Int) = AppNotification(
    NotificationKind.MixtureResult, ownerId, mixtureId, "Resultado da mistura disponível",
    "Confira o resultado registrado e as orientações no histórico do Quimia.", NotificationDestination.MixtureHistory,
)

fun shelfReminderNotification(ownerId: String) = AppNotification(
    NotificationKind.ShelfReminder, ownerId, 0, "Como está sua estante?",
    "Reserve um momento para revisar e organizar os produtos que você guardou.", NotificationDestination.Shelf,
)
