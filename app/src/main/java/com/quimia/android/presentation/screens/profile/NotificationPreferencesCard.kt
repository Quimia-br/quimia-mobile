package com.quimia.android.presentation.screens.profile

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import buttons.QuimiaButton
import card.QuimiaCard
import com.quimia.android.data.auth.AuthRepositoryProvider
import com.quimia.android.data.auth.SessionState
import com.quimia.android.data.notifications.QuimiaNotifications
import com.quimia.android.domain.notifications.*
import controls.QuimiaSwitch
import theme.QuimiaTextStyles
import theme.quimiaColorTokens

@Composable
internal fun NotificationPreferencesCard(preferences: ProfilePreferences, onChange: (ProfilePreferences) -> Unit) {
    val context = LocalContext.current
    val inspection = LocalInspectionMode.current
    val publisher = remember(context, inspection) { if (inspection) null else QuimiaNotifications(context) }
    val latestPreferences by rememberUpdatedState(preferences)
    val latestChange by rememberUpdatedState(onChange)
    var available by remember { mutableStateOf(publisher?.isAvailable() == true) }
    var pending by rememberSaveable { mutableStateOf<String?>(null) }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    val tokens = quimiaColorTokens()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, publisher) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) available = publisher?.isAvailable() == true
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    fun update(kind: String, value: Boolean) {
        latestChange(if (kind == "mixtures") latestPreferences.copy(mixtureNotifications = value)
            else latestPreferences.copy(shelfReminders = value))
    }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        available = granted && publisher?.isAvailable() == true
        pending?.let { kind -> if (available) { update(kind, true); message = null }
            else message = "As notificações estão bloqueadas. Você pode permitir nos ajustes do Android." }
        pending = null
    }
    fun toggle(kind: String, value: Boolean) {
        message = null
        if (!value || inspection || publisher?.isAvailable() == true) {
            update(kind, value)
        } else if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            pending = kind
            permission.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else message = "As notificações estão bloqueadas. Você pode permitir nos ajustes do Android."
    }

    QuimiaCard(title = "Notificações", description = null, endIcon = null,
        primaryActionLabel = null, secondaryActionLabel = null,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp), contentPadding = 24.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            NotificationOption("Resultados de misturas", "Receba um aviso quando uma consulta feita no app terminar.",
                preferences.mixtureNotifications) { toggle("mixtures", it) }
            NotificationOption("Lembrete da Estante", "Um lembrete por semana para revisar seus produtos. Pode chegar mais tarde para economizar bateria.",
                preferences.shelfReminders) { toggle("shelf", it) }
            if (!available && (preferences.mixtureNotifications || preferences.shelfReminders)) {
                Text("O Android está bloqueando as notificações deste app.", style = QuimiaTextStyles.medium14, color = tokens.foregroundSubtle)
            }
            TextButton(onClick = {
                if (publisher != null) try { context.startActivity(publisher.settingsIntent()) }
                catch (_: ActivityNotFoundException) {
                    context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")))
                }
            }) { Text("Configurar no Android", style = QuimiaTextStyles.medium14, color = tokens.foregroundPrimary) }
            QuimiaButton(text = "Enviar notificação de teste", iconSize = 0, espacamento = 0,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp), fillTextWidth = true,
                enabled = available && (preferences.mixtureNotifications || preferences.shelfReminders),
                containerColor = tokens.secondary, textColor = tokens.foregroundPrimary, onClick = {
                    val owner = (AuthRepositoryProvider.get(context).session.value as? SessionState.Authenticated)?.profile?.id
                    if (owner != null && publisher != null) {
                        val kind = if (preferences.mixtureNotifications) NotificationKind.Test else NotificationKind.ShelfReminder
                        val sent = publisher.post(AppNotification(kind, owner, -1, "Teste do Quimia",
                            "As notificações deste aparelho estão funcionando.", NotificationDestination.Home))
                        message = if (sent) "Enviada. Confira a barra de notificações do aparelho."
                            else "Este tipo de notificação está bloqueado nos ajustes do Android."
                    }
                })
            message?.let { Text(it, Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                style = QuimiaTextStyles.medium14, color = tokens.foregroundSubtle) }
        }
    }
}

@Composable
private fun NotificationOption(title: String, description: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    val tokens = quimiaColorTokens()
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = QuimiaTextStyles.medium16, color = tokens.foregroundPrimary)
            Text(description, style = QuimiaTextStyles.medium14, color = tokens.foregroundSubtle)
        }
        QuimiaSwitch(checked = checked, onCheckedChange = onChange, contentDescription = title)
    }
}
