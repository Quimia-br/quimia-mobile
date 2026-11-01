package com.quimia.android.domain.notifications

import com.quimia.android.domain.preferences.ProfilePreferences
import com.quimia.android.domain.preferences.ProfilePreferencesRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class SendShelfReminderTest {
    private val settings = Settings()
    private val account = Account()
    private val publisher = Publisher()
    private val reminder = SendShelfReminder(settings, account, publisher)

    @Test fun optedOutDoesNotQueryTheAccountOrTheApi() = runBlocking {
        settings.value = ProfilePreferences()
        assertFalse(reminder.send("owner"))
        assertEquals(0, account.calls)
        assertTrue(publisher.sent.isEmpty())
    }

    @Test fun deniedPermissionDoesNotQueryTheApi() = runBlocking {
        publisher.available = false
        assertFalse(reminder.send("owner"))
        assertEquals(0, account.calls)
    }

    @Test fun signedOutOrDifferentAccountCannotReceiveTheReminder() = runBlocking {
        account.userId = null
        assertFalse(reminder.send("owner"))
        account.userId = "another-user"
        assertFalse(reminder.send("owner"))
        assertEquals(0, account.productCalls)
        assertTrue(publisher.sent.isEmpty())
    }

    @Test fun emptyShelfDoesNotProduceANotification() = runBlocking {
        account.products = false
        assertFalse(reminder.send("owner"))
        assertTrue(publisher.sent.isEmpty())
    }

    @Test fun aRealShelfProducesOneReminderThatOpensTheShelf() = runBlocking {
        assertTrue(reminder.send("owner"))
        val notification = publisher.sent.single()
        assertEquals(NotificationDestination.Shelf, notification.destination)
        assertEquals("owner", notification.ownerId)
        assertEquals(NotificationKind.ShelfReminder, notification.kind)
    }

    @Test fun revokingConsentWhileTheApiIsLoadingPreventsDelivery() = runBlocking {
        account.onProducts = { settings.value = settings.value.copy(shelfReminders = false) }
        assertFalse(reminder.send("owner"))
        assertTrue(publisher.sent.isEmpty())
    }

    @Test fun logoutWhileTheApiIsLoadingPreventsDelivery() = runBlocking {
        account.onProducts = { account.userId = null }
        assertFalse(reminder.send("owner"))
        assertTrue(publisher.sent.isEmpty())
    }

    private class Settings : ProfilePreferencesRepository {
        var value = ProfilePreferences(shelfReminders = true)
        override suspend fun load() = value
        override suspend fun save(value: ProfilePreferences) { this.value = value }
    }
    private class Account : ShelfReminderAccount {
        var userId: String? = "owner"
        var products = true
        var calls = 0
        var productCalls = 0
        var onProducts: () -> Unit = {}
        override suspend fun activeUserId(): String? { calls++; return userId }
        override suspend fun hasProducts(userId: String): Boolean { productCalls++; onProducts(); return products }
        override fun isStillSignedIn(userId: String) = this.userId == userId
    }
    private class Publisher : AppNotificationPublisher {
        var available = true
        val sent = mutableListOf<AppNotification>()
        override fun isAvailable() = available
        override fun post(notification: AppNotification): Boolean { sent += notification; return true }
    }
}
