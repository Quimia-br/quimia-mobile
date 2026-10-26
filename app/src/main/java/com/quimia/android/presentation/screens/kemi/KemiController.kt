package com.quimia.android.presentation.screens.kemi

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.setValue
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

@Stable
class KemiController(initialState: KemiState = KemiState()) {
    var state by mutableStateOf(initialState)
        private set

    fun send(text: String, now: Long = System.currentTimeMillis()): KemiRequest? {
        if (text.isBlank() || state.readOnly || state.pending) return null
        val current = state.active ?: KemiConversation(UUID.randomUUID().toString(), now)
        val message = KemiMessage(UUID.randomUUID().toString(), text.trim(), KemiRole.User, now, KemiDelivery.Pending)
        val updated = current.copy(messages = current.messages + message)
        state = state.copy(conversations = state.conversations.filterNot { it.id == updated.id } + updated,
            activeId = updated.id, viewedId = updated.id)
        return KemiRequest(updated, message.id)
    }

    fun retry(): KemiRequest? {
        if (state.readOnly || state.pending) return null
        val conversation = state.current ?: return null
        val message = conversation.messages.lastOrNull { it.role == KemiRole.User && it.delivery == KemiDelivery.Failed } ?: return null
        val updated = conversation.copy(messages = conversation.messages.map {
            if (it.id == message.id) it.copy(delivery = KemiDelivery.Pending) else it
        })
        replace(updated)
        return KemiRequest(updated, message.id)
    }

    fun complete(request: KemiRequest, reply: KemiReply, now: Long = System.currentTimeMillis()) {
        val conversation = state.conversations.firstOrNull { it.id == request.conversation.id } ?: return
        if (conversation.closed || state.activeId != conversation.id ||
            conversation.messages.none { it.id == request.messageId && it.delivery == KemiDelivery.Pending }) return
        val answer = (reply as? KemiReply.Answer)?.text?.takeIf { it.isNotBlank() }
        val messages = conversation.messages.map {
            if (it.id == request.messageId) it.copy(delivery = if (answer != null) KemiDelivery.Delivered else KemiDelivery.Failed) else it
        }
        replace(conversation.copy(messages = if (answer == null) messages else
            messages + KemiMessage(UUID.randomUUID().toString(), answer, KemiRole.Assistant, now)))
    }

    fun interruptPending() {
        state = state.copy(conversations = state.conversations.map { conversation ->
            conversation.copy(messages = conversation.messages.map {
                if (it.delivery == KemiDelivery.Pending) it.copy(delivery = KemiDelivery.Failed) else it
            })
        })
    }

    fun newChat() {
        interruptPending()
        state = state.copy(conversations = state.conversations.map {
            if (it.id == state.activeId && it.messages.isNotEmpty()) it.copy(closed = true) else it
        }, activeId = null, viewedId = null)
    }

    fun viewConversation(id: String) {
        if (state.conversations.any { it.id == id }) state = state.copy(viewedId = id)
    }

    fun resumeActive() { state = state.copy(viewedId = state.activeId) }

    private fun replace(conversation: KemiConversation) {
        state = state.copy(conversations = state.conversations.map { if (it.id == conversation.id) conversation else it })
    }
}

val KemiControllerSaver = Saver<KemiController, String>(
    save = { Json.encodeToString(it.state) },
    restore = { KemiController(restoreKemiState(it)) },
)
