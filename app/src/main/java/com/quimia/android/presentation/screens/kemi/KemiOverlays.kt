package com.quimia.android.presentation.screens.kemi

import android.view.WindowManager
import motion.QuimiaMotion
import motion.LocalQuimiaMotionEnabled
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import buttons.QuimiaButton
import buttons.QuimiaIconButton
import com.quimia.android.presentation.system.HideSystemNavigationBar
import com.composables.icons.lucide.*
import dragger.QuimiaDragger
import listItem.QuimiaListItem
import shapeIcon.QuimiaShapeIcon
import theme.LightTokens
import theme.QuimiaTextStyles
import theme.quimiaColorTokens
import kotlin.math.roundToInt

enum class KemiPanel { Hidden, Collapsed, Expanded }

@Composable
internal fun KemiHistoryPanel(panel: KemiPanel, history: List<KemiConversation>, now: Long,
    onPanelChange: (KemiPanel) -> Unit, onOpen: (String) -> Unit, onNewChat: () -> Unit,
    modifier: Modifier = Modifier) {
    if (panel == KemiPanel.Hidden) return
    val tokens = quimiaColorTokens()
    val motionEnabled = LocalQuimiaMotionEnabled.current
    BoxWithConstraints(modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        val contentHeight = minOf(547.dp, (maxHeight - 84.dp).coerceAtLeast(240.dp))
        val density = LocalDensity.current
        val collapsedPx = with(density) { (contentHeight + 22.dp - 68.dp).toPx() }
        val hiddenPx = with(density) { (contentHeight + 22.dp).toPx() }
        val offset = remember { Animatable(if (motionEnabled) hiddenPx else if (panel == KemiPanel.Expanded) 0f else collapsedPx) }
        var dragging by remember { mutableStateOf(false) }
        var dragOffset by remember { mutableFloatStateOf(0f) }
        LaunchedEffect(panel, collapsedPx, motionEnabled) {
            offset.animateTo(if (panel == KemiPanel.Expanded) 0f else collapsedPx, QuimiaMotion.settleSpec(motionEnabled))
        }
        val dragState = rememberDraggableState { delta -> dragOffset = (dragOffset + delta).coerceIn(0f, collapsedPx) }
        Surface(modifier = Modifier.widthIn(max = com.quimia.android.presentation.components.quimiaOverlayWidth(594.dp, 840.dp)).fillMaxWidth()
            .padding(horizontal = 17.dp).padding(bottom = 22.dp)
            .offset { IntOffset(0, (if (dragging) dragOffset else offset.value).roundToInt()) }
            .height(contentHeight), color = tokens.surfaceBase, shape = RoundedCornerShape(40.dp), shadowElevation = 24.dp) {
            Column(Modifier.padding(horizontal = 32.dp).padding(top = 32.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.fillMaxWidth()
                    .draggable(dragState, Orientation.Vertical,
                        onDragStarted = { offset.stop(); dragOffset = offset.value; dragging = true },
                        onDragStopped = { velocity ->
                            offset.snapTo(dragOffset)
                            dragging = false
                            val expand = if (velocity < -300f) true else if (velocity > 300f) false else dragOffset < collapsedPx / 2
                            onPanelChange(if (expand) KemiPanel.Expanded else KemiPanel.Collapsed)
                            // Also settle when a gesture ends at the already-selected anchor.
                            offset.animateTo(if (expand) 0f else collapsedPx, QuimiaMotion.settleSpec(motionEnabled))
                        })
                    .clickable(role = Role.Button, onClickLabel = "Expandir ou recolher histórico") {
                        onPanelChange(if (panel == KemiPanel.Expanded) KemiPanel.Collapsed else KemiPanel.Expanded)
                    }) {
                    QuimiaDragger(height = 3.dp)
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Histórico de chats", style = QuimiaTextStyles.medium24, color = tokens.foregroundPrimary,
                        modifier = Modifier.weight(1f))
                    QuimiaIconButton(icon = Lucide.X, onClick = { onPanelChange(KemiPanel.Hidden) },
                        containerColor = tokens.secondary, contentDescription = "Fechar histórico")
                }
                LazyColumn(Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (history.isEmpty()) item {
                        Text("Seus chats encerrados aparecerão aqui.", style = QuimiaTextStyles.medium14,
                            color = tokens.foregroundSubtle, modifier = Modifier.padding(vertical = 24.dp))
                    }
                    items(history, key = { it.id }) { chat ->
                        QuimiaListItem(title = chat.title, subtitle = kemiHistoryDate(chat.updatedAt, now),
                            titleMaxLines = 1, titleTextStyle = QuimiaTextStyles.medium14,
                            subtitleTextStyle = QuimiaTextStyles.medium12, textSpacing = 10.dp,
                            onClick = { onOpen(chat.id) })
                    }
                }
                QuimiaButton(text = "Novo chat", onClick = onNewChat, iconLeftVector = Lucide.SquarePen,
                    iconSize = 20, textStyle = QuimiaTextStyles.medium16, textColor = LightTokens.foregroundPrimary,
                    iconColor = LightTokens.foregroundPrimary, fillTextWidth = true, centerContent = true, espacamento = 0,
                    modifier = Modifier.fillMaxWidth().height(52.dp), contentPadding = PaddingValues(horizontal = 20.dp))
            }
        }
    }
}

@Composable
internal fun KemiNewChatDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    com.quimia.android.presentation.components.QuimiaFeedbackDialog(
        title = "Tem certeza que quer abrir um novo chat e encerrar o atual?",
        description = "Ao abrir um novo chat, você não pode voltar a interagir com chats anteriores.",
        onDismiss = onDismiss, action = "Abrir", onConfirm = onConfirm, secondaryAction = "Cancelar",
    )
}
