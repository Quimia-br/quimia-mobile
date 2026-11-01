package com.quimia.android.presentation.system

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quimia.android.data.auth.AuthRepositoryProvider
import com.quimia.android.data.auth.SessionState
import com.quimia.android.data.notifications.QuimiaNotifications
import com.quimia.android.data.notifications.ShelfReminderScheduler
import com.quimia.android.domain.notifications.NotificationKind
import com.quimia.android.domain.preferences.ProfilePreferences

@Composable
internal fun NotificationEffects(preferences: ProfilePreferences) {
    val context = LocalContext.current.applicationContext
    val sessions = remember(context) { AuthRepositoryProvider.get(context) }
    val session by sessions.session.collectAsStateWithLifecycle()
    val userId = (session as? SessionState.Authenticated)?.profile?.id
    val signedOut = session == SessionState.SignedOut
    val publisher = remember(context) { QuimiaNotifications(context) }
    LaunchedEffect(Unit) { publisher.createChannels() }
    LaunchedEffect(preferences.mixtureNotifications) {
        if (!preferences.mixtureNotifications) publisher.cancel(NotificationKind.MixtureResult)
    }
    LaunchedEffect(preferences.mixtureNotifications, preferences.shelfReminders) {
        if (!preferences.mixtureNotifications && !preferences.shelfReminders) publisher.cancel(NotificationKind.Test)
    }
    LaunchedEffect(userId, signedOut, preferences.shelfReminders) {
        val scheduler = ShelfReminderScheduler(context)
        if (!preferences.shelfReminders || signedOut) {
            scheduler.cancel()
            publisher.cancel(NotificationKind.ShelfReminder)
        } else userId?.let(scheduler::schedule)
        if (signedOut) publisher.cancelAll()
    }
}
