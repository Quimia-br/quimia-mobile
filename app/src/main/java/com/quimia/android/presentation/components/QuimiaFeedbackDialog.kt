package com.quimia.android.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import buttons.QuimiaButton
import com.composables.icons.lucide.*
import com.quimia.android.presentation.system.HideSystemNavigationBar
import shapeIcon.QuimiaShapeIcon
import theme.LightTokens
import theme.QuimiaTextStyles
import theme.quimiaColorTokens
import theme.quimiaContrastBorder

internal enum class QuimiaFeedbackKind { Info, Success, Error, Destructive }

internal fun overlayWidth(screenWidth: Dp, phoneWidth: Dp, tabletMax: Dp = 720.dp, fraction: Float = .86f): Dp =
    if (screenWidth >= 600.dp) maxOf(phoneWidth, screenWidth * fraction).coerceAtMost(tabletMax) else phoneWidth

@Composable
internal fun quimiaOverlayWidth(phoneWidth: Dp, tabletMax: Dp = 720.dp, fraction: Float = .86f): Dp =
    overlayWidth(LocalConfiguration.current.screenWidthDp.dp, phoneWidth, tabletMax, fraction)

@Composable
internal fun QuimiaFeedbackDialog(
    title: String,
    onDismiss: () -> Unit,
    description: String? = null,
    kind: QuimiaFeedbackKind = QuimiaFeedbackKind.Info,
    action: String = "Entendi",
    onConfirm: () -> Unit = onDismiss,
    secondaryAction: String? = null,
    onSecondary: () -> Unit = onDismiss,
    busy: Boolean = false,
    dismissible: Boolean = true,
    statusContent: (@Composable () -> Unit)? = null,
) {
    Dialog(onDismissRequest = { if (dismissible && !busy) onDismiss() }, properties = DialogProperties(
        usePlatformDefaultWidth = false, dismissOnBackPress = dismissible && !busy, dismissOnClickOutside = dismissible && !busy,
    )) {
        HideSystemNavigationBar()
        val window = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect { window?.setDimAmount(.08f) }
        Box(Modifier.fillMaxWidth().statusBarsPadding().navigationBarsPadding().padding(24.dp), contentAlignment = Alignment.Center) {
            QuimiaFeedbackCard(title, description, kind, action, onConfirm, secondaryAction, onSecondary, busy, statusContent)
        }
    }
}

@Composable
internal fun QuimiaFeedbackCard(
    title: String, description: String?, kind: QuimiaFeedbackKind, action: String, onConfirm: () -> Unit,
    secondaryAction: String? = null, onSecondary: () -> Unit = {}, busy: Boolean = false,
    statusContent: (@Composable () -> Unit)? = null,
) {
    val tokens = quimiaColorTokens()
    val destructive = kind == QuimiaFeedbackKind.Destructive
    val danger = androidx.compose.ui.graphics.Color(0xFFFF2576)
    val icon = when (kind) { QuimiaFeedbackKind.Success -> Lucide.Check
        QuimiaFeedbackKind.Error, QuimiaFeedbackKind.Destructive -> Lucide.TriangleAlert
        else -> Lucide.Info }
    Surface(Modifier.widthIn(max = quimiaOverlayWidth(297.dp, 640.dp, .8f)).fillMaxWidth(),
        shape = RoundedCornerShape(40.dp), color = tokens.surfaceBase, border = quimiaContrastBorder()) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            QuimiaShapeIcon(size = 76.dp, icon = icon,
                containerColor = when (kind) { QuimiaFeedbackKind.Success -> androidx.compose.ui.graphics.Color(0xFF00E69D)
                    QuimiaFeedbackKind.Destructive, QuimiaFeedbackKind.Error -> danger; else -> tokens.surfaceBackground },
                iconColor = if (kind == QuimiaFeedbackKind.Error || destructive) tokens.white else tokens.foregroundPrimary)
            Text(title, Modifier.fillMaxWidth(), style = QuimiaTextStyles.medium16,
                color = tokens.foregroundPrimary, textAlign = TextAlign.Center)
            description?.takeIf(String::isNotBlank)?.let { Text(it, Modifier.fillMaxWidth(), style = QuimiaTextStyles.medium12,
                color = tokens.foregroundSubtle, textAlign = TextAlign.Center) }
            statusContent?.invoke()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                secondaryAction?.let { QuimiaButton(text = it, modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    textStyle = QuimiaTextStyles.medium16, iconSize = 0, espacamento = 0, centerContent = true, fillTextWidth = true,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp), containerColor = tokens.secondary,
                    textColor = tokens.foregroundPrimary, enabled = !busy, onClick = onSecondary) }
                QuimiaButton(text = action, modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    textStyle = QuimiaTextStyles.medium16, iconSize = 0, espacamento = 0, centerContent = true, fillTextWidth = true,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp),
                    containerColor = if (destructive) danger else tokens.primary,
                    textColor = if (destructive) tokens.white else LightTokens.foregroundPrimary, enabled = !busy, onClick = onConfirm)
            }
        }
    }
}
