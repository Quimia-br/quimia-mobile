package com.quimia.android.presentation.screens.kemi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.quimia.android.presentation.preview.QuimiaScreenPreviews
import theme.QuimiaTheme

// Figma specimens are used only by previews, never by the app's chat controller.
internal const val KemiPreviewNow = 1_790_007_600_000L
private const val PreviewQuote = "Até cortar os próprios defeitos pode ser perigoso. Nunca se sabe qual é o defeito que sustenta nosso edifício inteiro."

internal fun kemiPreviewState(pending: Boolean = false, history: Boolean = false, includeAssistant: Boolean = true): KemiState {
    val active = KemiConversation("preview-current", KemiPreviewNow, listOf(
        KemiMessage("preview-user", PreviewQuote, KemiRole.User, KemiPreviewNow,
            if (pending) KemiDelivery.Pending else KemiDelivery.Delivered),
    ) + if (!includeAssistant) emptyList() else listOf(KemiMessage("preview-assistant", PreviewQuote, KemiRole.Assistant, KemiPreviewNow)))
    val archived = if (history) listOf(1, 3, 3).mapIndexed { index, days ->
        val time = KemiPreviewNow - days * 86_400_000L
        KemiConversation("preview-history-$index", time,
            listOf(KemiMessage("preview-history-message-$index", PreviewQuote, KemiRole.User, time)), closed = true)
    } else emptyList()
    return KemiState(archived + active, active.id, active.id)
}

@Composable
private fun KemiPreview(welcome: Boolean = false, pending: Boolean = false, copied: Boolean = false,
    panel: KemiPanel = KemiPanel.Hidden, confirm: Boolean = false) {
    QuimiaTheme(false) {
        val controller = remember { KemiController(if (welcome) KemiState() else
            kemiPreviewState(pending || copied, panel != KemiPanel.Hidden, includeAssistant = !copied)) }
        KemiScreen(controller, onClose = {}, displayName = "Gabriela", initialPanel = panel,
            initialCopiedMessageId = if (panel != KemiPanel.Hidden) "preview-assistant" else null,
            initialCopied = copied, initialNewChatConfirmation = confirm, now = KemiPreviewNow)
    }
}

@QuimiaScreenPreviews
@Preview(name = "Figma · Novo chat", group = "Kemi", widthDp = 360, heightDp = 800, showSystemUi = true)
@Composable
private fun KemiWelcomePreview() = KemiPreview(welcome = true)

@QuimiaScreenPreviews
@Preview(name = "Figma · Conversa 1", group = "Kemi", widthDp = 360, heightDp = 800, showSystemUi = true)
@Composable
private fun KemiConversationPreview() = KemiPreview()

@QuimiaScreenPreviews
@Preview(name = "Figma · Conversa 2", group = "Kemi", widthDp = 360, heightDp = 800, showSystemUi = true)
@Composable
private fun KemiConversationVariationPreview() = KemiPreview()

@QuimiaScreenPreviews
@Preview(name = "Figma · Aguardando resposta", group = "Kemi", widthDp = 360, heightDp = 800, showSystemUi = true)
@Composable
private fun KemiPendingPreview() = KemiPreview(pending = true)

@QuimiaScreenPreviews
@Preview(name = "Figma · Mensagem copiada", group = "Kemi", widthDp = 360, heightDp = 800, showSystemUi = true)
@Composable
private fun KemiCopiedPreview() = KemiPreview(copied = true)

@QuimiaScreenPreviews
@Preview(name = "Figma · Histórico expandido", group = "Kemi", widthDp = 360, heightDp = 800, showSystemUi = true)
@Composable
private fun KemiHistoryPreview() = KemiPreview(panel = KemiPanel.Expanded)

@QuimiaScreenPreviews
@Preview(name = "Figma · Histórico recolhido", group = "Kemi", widthDp = 360, heightDp = 800, showSystemUi = true)
@Composable
private fun KemiHistoryCollapsedPreview() = KemiPreview(panel = KemiPanel.Collapsed)

@QuimiaScreenPreviews
@Preview(name = "Figma · Confirmar novo chat", group = "Kemi", widthDp = 360, heightDp = 800, showSystemUi = true)
@Composable
private fun KemiNewChatPreview() = KemiPreview(confirm = true)
