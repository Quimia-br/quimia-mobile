package com.quimia.android.domain.notifications

import com.quimia.android.domain.preferences.ProfilePreferencesRepository

interface ShelfReminderAccount {
    suspend fun activeUserId(): String?
    suspend fun hasProducts(userId: String): Boolean
    fun isStillSignedIn(userId: String): Boolean
}

/** Recheck consent and ownership after the network request, including logout while it was in flight. */
class SendShelfReminder(
    private val preferences: ProfilePreferencesRepository,
    private val account: ShelfReminderAccount,
    private val publisher: AppNotificationPublisher,
) {
    suspend fun send(ownerId: String): Boolean {
        if (ownerId.isBlank() || !preferences.load().shelfReminders || !publisher.isAvailable()) return false
        if (account.activeUserId() != ownerId || !account.hasProducts(ownerId)) return false
        if (!preferences.load().shelfReminders || !account.isStillSignedIn(ownerId)) return false
        return publisher.post(shelfReminderNotification(ownerId))
    }
}
