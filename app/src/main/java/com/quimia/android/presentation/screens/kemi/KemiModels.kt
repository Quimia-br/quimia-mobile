package com.quimia.android.presentation.screens.kemi

import com.quimia.android.presentation.screens.errors.GenericErrorKind
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.SocketTimeoutException

@Serializable
enum class KemiRole { User, Assistant }

@Serializable
enum class KemiDelivery { Pending, Delivered, Failed }

@Serializable
data class KemiMessage(
    val id: String, val text: String, val role: KemiRole, val createdAt: Long,
    val delivery: KemiDelivery = KemiDelivery.Delivered,
)

@Serializable
data class KemiConversation(
    val id: String, val createdAt: Long, val messages: List<KemiMessage> = emptyList(), val closed: Boolean = false,
) {
    val title: String get() = messages.firstOrNull { it.role == KemiRole.User }?.text ?: "Novo chat"
    val updatedAt: Long get() = messages.lastOrNull()?.createdAt ?: createdAt
}

@Serializable
data class KemiState(
    val conversations: List<KemiConversation> = emptyList(),
    val activeId: String? = null, val viewedId: String? = null,
) {
    val current: KemiConversation? get() = conversations.firstOrNull { it.id == (viewedId ?: activeId) }
    val active: KemiConversation? get() = conversations.firstOrNull { it.id == activeId }
    val history: List<KemiConversation> get() = conversations.filter { it.closed && it.messages.isNotEmpty() }.sortedByDescending { it.updatedAt }
    val readOnly: Boolean get() = current?.closed == true || (current != null && current?.id != activeId)
    val pending: Boolean get() = current?.messages?.any { it.delivery == KemiDelivery.Pending } == true
}

data class KemiRequest(val conversation: KemiConversation, val messageId: String)

sealed interface KemiReply {
    data class Answer(val text: String) : KemiReply
    data class Failure(val kind: GenericErrorKind) : KemiReply
}

typealias KemiResponder = suspend (KemiRequest) -> KemiReply

internal suspend fun requestKemiReply(responder: KemiResponder?, request: KemiRequest): KemiReply = try {
    if (responder == null) KemiReply.Failure(GenericErrorKind.ServiceUnavailable)
    else when (val reply = responder(request)) {
        is KemiReply.Answer -> if (reply.text.isBlank()) KemiReply.Failure(GenericErrorKind.ContentUnavailable) else reply
        is KemiReply.Failure -> reply
    }
} catch (cancelled: CancellationException) {
    throw cancelled
} catch (_: SocketTimeoutException) {
    KemiReply.Failure(GenericErrorKind.Timeout)
} catch (_: Exception) {
    KemiReply.Failure(GenericErrorKind.Unknown)
}

internal fun restoreKemiState(encoded: String): KemiState = runCatching {
    val state = Json.decodeFromString<KemiState>(encoded)
    state.copy(conversations = state.conversations.map { conversation ->
        conversation.copy(messages = conversation.messages.map { message ->
            if (message.delivery == KemiDelivery.Pending) message.copy(delivery = KemiDelivery.Failed) else message
        })
    })
}.getOrDefault(KemiState())
