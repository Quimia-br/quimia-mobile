package com.quimia.android.presentation.screens.kemi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import buttons.QuimiaIconButton
import com.composables.icons.lucide.*
import com.quimia.android.R
import header.QuimiaSearchBar
import image.QuimiaImage
import theme.LightTokens
import theme.QuimiaTextStyles
import theme.quimiaColorTokens

@Composable
internal fun KemiBackground(modifier: Modifier = Modifier) {
    val tokens = quimiaColorTokens()
    Box(modifier.background(Brush.verticalGradient(
        0f to tokens.primary.copy(alpha = .82f).compositeOver(tokens.surfaceBackground),
        .47f to tokens.primary.copy(alpha = .245f).compositeOver(
            tokens.success.copy(alpha = .08f).compositeOver(tokens.surfaceBackground)),
        .86f to tokens.primary.copy(alpha = .05f).compositeOver(tokens.surfaceBackground),
        1f to tokens.surfaceBackground,
    )))
}

@Composable
internal fun KemiHeader(
    panel: KemiPanel, pending: Boolean, onClose: () -> Unit,
    onHistory: () -> Unit, onNewChat: () -> Unit,
    compact: Boolean = false,
) {
    val tokens = quimiaColorTokens()
    Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = if (compact) 8.dp else 16.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        QuimiaIconButton(icon = Lucide.ChevronLeft, onClick = onClose,
            containerColor = tokens.surfaceBase, contentDescription = "Voltar")
        Spacer(Modifier.weight(1f))
        if (panel != KemiPanel.Collapsed) QuimiaIconButton(
            icon = if (panel == KemiPanel.Expanded || pending) Lucide.Clock else Lucide.Menu,
            onClick = onHistory, containerColor = tokens.surfaceBase, contentDescription = "Histórico de chats")
        if (panel != KemiPanel.Hidden) QuimiaIconButton(icon = Lucide.SquarePen, onClick = onNewChat,
            containerColor = tokens.surfaceBase, contentDescription = "Novo chat")
    }
}

@Composable
internal fun KemiWelcome(displayName: String?, modifier: Modifier = Modifier) {
    val tokens = quimiaColorTokens()
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(11.dp, Alignment.CenterVertically)) {
        QuimiaImage(painterResource(R.drawable.kemi_character), "Kemi",
            modifier = Modifier.weight(1f, fill = false).widthIn(max = 188.dp)
                .heightIn(max = 158.dp).aspectRatio(188f / 158f), contentScale = ContentScale.Fit,
            shape = RectangleShape, backgroundColor = Color.Transparent)
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(displayName?.trim()?.takeIf { it.isNotEmpty() }?.let { "Olá, $it" } ?: "Olá!",
                style = QuimiaTextStyles.medium24.copy(fontSize = 25.sp, lineHeight = 30.sp), color = tokens.foregroundPrimary)
            Text("Como posso te ajudar?", style = QuimiaTextStyles.medium16, color = tokens.foregroundSubtle)
        }
    }
}

@Composable
internal fun KemiMessageBubble(message: KemiMessage, now: Long, copied: Boolean = false, onCopy: () -> Unit) {
    val tokens = quimiaColorTokens()
    val user = message.role == KemiRole.User
    val foreground = if (user) LightTokens.foregroundPrimary else tokens.foregroundPrimary
    Box(Modifier.fillMaxWidth(), contentAlignment = if (user) Alignment.CenterEnd else Alignment.CenterStart) {
        Surface(color = if (user) tokens.primary else tokens.surfaceBase,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp,
                bottomEnd = if (user) 4.dp else 32.dp, bottomStart = if (user) 32.dp else 4.dp),
            modifier = Modifier.widthIn(max = 257.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text(message.text, style = QuimiaTextStyles.medium14, color = foreground)
                Row(Modifier.align(Alignment.End), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(if (copied) Lucide.Check else Lucide.Copy, if (copied) "Mensagem copiada" else "Copiar mensagem", tint = foreground,
                        modifier = Modifier.size(12.dp).clickable(role = Role.Button, onClick = onCopy))
                    Text(kemiMessageTime(message.createdAt, now), style = QuimiaTextStyles.medium12, color = foreground)
                }
                if (message.delivery == KemiDelivery.Failed) {
                    Text("Não enviada", style = QuimiaTextStyles.medium12, color = foreground,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                }
            }
        }
    }
}

@Composable
internal fun KemiPendingBubble() {
    val tokens = quimiaColorTokens()
    Surface(color = tokens.surfaceBase,
        shape = RoundedCornerShape(32.dp, 32.dp, 32.dp, 4.dp),
        modifier = Modifier.semantics { contentDescription = "Kemi está respondendo"; liveRegion = LiveRegionMode.Polite }) {
        Row(Modifier.padding(24.dp), horizontalArrangement = Arrangement.spacedBy(3.976.dp)) {
            repeat(3) { Box(Modifier.size(5.26.dp).background(tokens.onSecondary, CircleShape)) }
        }
    }
}

@Composable
internal fun KemiComposer(draft: String, onDraftChange: (String) -> Unit, onSend: () -> Unit,
    onVoice: () -> Unit, enabled: Boolean, compact: Boolean = false) {
    BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = if (compact) 12.dp else 32.dp)) {
        QuimiaSearchBar(value = draft, onValueChange = onDraftChange,
            placeholder = "Envie uma mensagem", leadingIcon = Lucide.Mic, searchIconSize = 24.dp,
            leadingIconDescription = "Ditar mensagem", onLeadingIconClick = onVoice,
            icon = Lucide.SendHorizontal, iconDescription = "Enviar mensagem", trailingIconSize = 20.dp,
            onClick = onSend, onSearch = onSend, enabled = enabled,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSend() }),
            horizontalPadding = 16.dp, verticalPadding = 14.dp,
            spacedBy = if (maxWidth < 280.dp) 10.dp else 20.dp)
    }
}
