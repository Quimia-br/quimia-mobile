package com.quimia.android.data.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.quimia.android.BuildConfig
import com.quimia.android.data.auth.*
import com.quimia.android.data.catalog.CatalogApi
import com.quimia.android.data.catalog.validateCatalogPage
import com.quimia.android.data.preferences.RoomProfilePreferencesRepository
import com.quimia.android.domain.notifications.SendShelfReminder
import com.quimia.android.domain.notifications.ShelfReminderAccount
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import java.io.IOException

class ShelfReminderWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result {
        val owner = inputData.getString(NOTIFICATION_OWNER) ?: return Result.success()
        val publisher = QuimiaNotifications(applicationContext)
        if (!publisher.canPost(com.quimia.android.domain.notifications.NotificationKind.ShelfReminder)) return Result.success()
        val sessions = AuthRepositoryProvider.get(applicationContext)
        val account = object : ShelfReminderAccount {
            override suspend fun activeUserId(): String? {
                if (sessions.session.value == SessionState.Checking) sessions.restore()
                if (sessions.session.value is SessionState.Unavailable) throw IOException("Session temporarily unavailable")
                return (sessions.session.value as? SessionState.Authenticated)?.profile?.id
            }
            override suspend fun hasProducts(userId: String): Boolean = sessions.withSession(userId) { bearer ->
                val api = AuthApiFactory.create(BuildConfig.AUTH_BASE_URL, CatalogApi::class.java)
                val page = api.shelf(bearer, 0, 1)
                validateCatalogPage(page, 0, 1)
                page.totalItens > 0
            }
            override fun isStillSignedIn(userId: String) =
                (sessions.session.value as? SessionState.Authenticated)?.profile?.id == userId
        }
        return try {
            withTimeout(25_000) {
                SendShelfReminder(RoomProfilePreferencesRepository(applicationContext), account, publisher).send(owner)
            }
            Result.success()
        } catch (_: TimeoutCancellationException) {
            retryIfUseful()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            if (sessions.session.value == SessionState.SignedOut) Result.success() else retryIfUseful()
        }
    }

    private fun retryIfUseful() = if (runAttemptCount < 3) Result.retry() else Result.success()
}
