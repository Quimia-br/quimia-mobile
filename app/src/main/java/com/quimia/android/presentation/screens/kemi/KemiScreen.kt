package com.quimia.android.presentation.screens.kemi

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import buttons.QuimiaButton
import com.quimia.android.presentation.components.rememberVoiceInput
import com.quimia.android.presentation.screens.errors.GenericErrorAction
import com.quimia.android.presentation.screens.errors.GenericErrorKind
import com.quimia.android.presentation.screens.errors.GenericErrorScreen
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import theme.QuimiaTextStyles
import theme.quimiaColorTokens
import toast.QuimiaToast
import toast.QuimiaToastType

@Composable
fun KemiScreen(
    controller: KemiController,
    onClose: () -> Unit,
    displayName: String? = null,
    responder: KemiResponder? = null,
    onHome: () -> Unit = onClose,
    onSignInAgain: () -> Unit = onClose,
    initialPanel: KemiPanel = KemiPanel.Hidden,
    initialCopied: Boolean = false,
    initialNewChatConfirmation: Boolean = false,
    initialCopiedMessageId: String? = null,
    now: Long = System.currentTimeMillis(),
    historyNotice: String? = null,
) {
    val tokens = quimiaColorTokens()
    val state = controller.state
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val inspection = LocalInspectionMode.current
    val keyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    var draft by rememberSaveable(controller) { mutableStateOf("") }
    var panel by rememberSaveable { mutableStateOf(initialPanel) }
    var confirmation by rememberSaveable { mutableStateOf(initialNewChatConfirmation) }
    var error by rememberSaveable { mutableStateOf<GenericErrorKind?>(null) }
    var errorDetail by rememberSaveable { mutableStateOf<String?>(null) }
    var copied by rememberSaveable { mutableIntStateOf(if (initialCopied) 1 else 0) }
    var copiedMessageId by rememberSaveable { mutableStateOf(initialCopiedMessageId) }
    var voiceUnavailable by rememberSaveable { mutableStateOf(false) }
    var requestJob by remember { mutableStateOf<Job?>(null) }

    fun submit(request: KemiRequest?) {
        if (request == null) return
        keyboard?.hide()
        error = null
        errorDetail = null
        requestJob = scope.launch {
            val reply = requestKemiReply(responder, request)
            ensureActive()
            controller.complete(request, reply)
            if (reply is KemiReply.Failure && controller.state.current?.id == request.conversation.id) {
                error = reply.kind; errorDetail = reply.detail
            }
        }
    }
    fun close() {
        requestJob?.cancel()
        controller.interruptPending()
        keyboard?.hide()
        onClose()
    }
    fun openNewChat() {
        keyboard?.hide()
        if (state.active?.messages?.isNotEmpty() == true) confirmation = true
        else {
            controller.newChat()
            draft = ""
            panel = KemiPanel.Hidden
        }
    }
    fun send() {
        if (draft.isNotBlank()) {
            val request = controller.send(draft)
            if (request != null) { draft = ""; submit(request) }
        }
    }

    val voice = rememberVoiceInput(prompt = "Dite sua mensagem para a Kemi",
        onUnavailable = { voiceUnavailable = true }) { draft = it }
    DisposableEffect(controller) {
        onDispose { requestJob?.cancel(); controller.interruptPending() }
    }
    LaunchedEffect(copied) {
        if (copied > 0 && !inspection && !initialCopied) { delay(4_000); copied = 0 }
    }
    LaunchedEffect(voiceUnavailable) {
        if (voiceUnavailable) { delay(5_000); voiceUnavailable = false }
    }
    BackHandler {
        when {
            confirmation -> confirmation = false
            error != null -> error = null
            panel != KemiPanel.Hidden -> panel = KemiPanel.Hidden
            else -> close()
        }
    }

    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Box(Modifier.fillMaxSize().background(tokens.surfaceBackground).clipToBounds()
        .pointerInput(Unit) { detectTapGestures { } }) {
        KemiBackground(Modifier.fillMaxWidth().height(210.dp))
        Column(Modifier.align(Alignment.TopCenter).widthIn(max = 600.dp).fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars.union(WindowInsets(top = 62.dp)))
            .navigationBarsPadding().imePadding()
            .then(if (error != null) Modifier.clearAndSetSemantics { } else Modifier)) {
            KemiHeader(panel, state.pending || copied > 0, ::close,
                onHistory = { keyboard?.hide(); panel = if (panel == KemiPanel.Expanded) KemiPanel.Collapsed else KemiPanel.Expanded },
                onNewChat = ::openNewChat, compact = keyboardVisible)
            val messages = state.current?.messages.orEmpty()
            if (messages.isEmpty()) {
                KemiWelcome(displayName, Modifier.weight(1f).fillMaxWidth())
            } else {
                val listState = androidx.compose.foundation.lazy.rememberLazyListState()
                LaunchedEffect(state.current?.id, messages.size, state.pending) {
                    listState.scrollToItem((messages.size + if (state.pending) 1 else 0) - 1)
                }
                LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 24.dp),
                    state = listState, contentPadding = PaddingValues(top = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Bottom)) {
                    items(messages, key = { it.id }) { message ->
                        KemiMessageBubble(message, now, copied = message.id == copiedMessageId) {
                            clipboard.setText(AnnotatedString(message.text))
                            copiedMessageId = message.id
                            copied++
                        }
                    }
                    if (state.pending) item(key = "pending") { KemiPendingBubble() }
                    if (!state.readOnly && !state.pending && messages.lastOrNull()?.delivery == KemiDelivery.Failed) item(key = "retry") {
                        QuimiaButton(text = "Tentar novamente", iconSize = 0, espacamento = 0,
                            onClick = { submit(controller.retry()) }, modifier = Modifier.fillMaxWidth().height(43.dp))
                    }
                }
            }
            if (panel != KemiPanel.Hidden) {
                Spacer(Modifier.height(100.dp))
            } else if (state.readOnly) {
                Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Este chat foi encerrado. Você ainda pode ler e copiar as mensagens.",
                        style = QuimiaTextStyles.medium14, color = tokens.foregroundSubtle)
                    QuimiaButton(text = if (state.active != null) "Voltar ao chat atual" else "Novo chat",
                        iconSize = 0, espacamento = 0, modifier = Modifier.fillMaxWidth().height(43.dp),
                        onClick = { if (state.active != null) controller.resumeActive() else openNewChat() })
                }
            } else KemiComposer(draft, { draft = it }, ::send, onVoice = voice,
                enabled = !state.pending, compact = keyboardVisible)
        }
        if (error == null && (copied > 0 || voiceUnavailable)) {
            QuimiaToast(message = if (voiceUnavailable) "Entrada por voz indisponível neste aparelho" else "Mensagem copiada",
                type = if (voiceUnavailable) QuimiaToastType.Warning else QuimiaToastType.Success,
                statusIconContainerColor = if (voiceUnavailable) null else tokens.success,
                onDismiss = { copied = 0; voiceUnavailable = false },
                modifier = Modifier.align(Alignment.TopCenter).widthIn(max = 600.dp)
                    .padding(horizontal = 24.dp).padding(top = maxOf(62.dp, statusTop) + 93.dp))
        }
        if (error == null) KemiHistoryPanel(panel, state.history, now, onPanelChange = { panel = it },
            onOpen = { controller.viewConversation(it); panel = KemiPanel.Hidden; keyboard?.hide() }, onNewChat = ::openNewChat, notice = historyNotice)
        if (confirmation) KemiNewChatDialog(onDismiss = { confirmation = false }, onConfirm = {
            requestJob?.cancel()
            controller.newChat()
            draft = ""
            confirmation = false
            copied = 0
            copiedMessageId = null
            error = null
            panel = KemiPanel.Hidden
        })
        error?.let { kind ->
            Box(Modifier.fillMaxSize().pointerInput(Unit) { detectTapGestures { } }) {
                GenericErrorScreen(kind, onClose = { error = null }, descriptionOverride = errorDetail, onAction = { action ->
                    when (action) {
                        GenericErrorAction.Retry -> submit(controller.retry())
                        GenericErrorAction.Home -> { controller.interruptPending(); onHome() }
                        GenericErrorAction.SignIn -> { controller.interruptPending(); onSignInAgain() }
                    }
                })
            }
        }
    }
}
