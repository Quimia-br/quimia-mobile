package com.quimia.android.data.kemi

import com.quimia.android.data.auth.AuthRepository
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Headers
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Locale

@Serializable internal data class ChatRequest(val texto: String, val sessaoId: String)
@Serializable internal data class ChatReply(val texto: String, val sessaoId: String, val respondidoEm: String)
internal interface KemiApi {
    @Headers("Accept: application/json")
    @POST("api/v1/chat/mensagens") suspend fun send(@Header("Authorization") bearer: String, @Body body: ChatRequest): ChatReply
}
internal class KemiContractException(message: String) : Exception(message)

internal fun kemiResponseTime(value: String): Long? {
    val match = Regex("^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2})(?:\\.(\\d{1,9}))?(Z|[+-]\\d{2}:\\d{2})$").matchEntire(value) ?: return null
    val normalized = match.groupValues[1] + "." + match.groupValues[2].padEnd(3, '0').take(3) + match.groupValues[3]
    val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.ROOT).apply { isLenient = false }
    val position = ParsePosition(0)
    return parser.parse(normalized, position)?.time?.takeIf { position.index == normalized.length }
}

internal class KemiRepository(private val api: KemiApi, private val sessions: AuthRepository) {
    suspend fun send(userId: String, sessionId: String, text: String): ChatReply {
        require(sessionId.matches(Regex("[A-Za-z0-9_-]{1,64}")))
        require(text.isNotBlank() && text.length <= 2000)
        return sessions.withSession(userId) { api.send(it, ChatRequest(text.trim(), sessionId)) }.also {
            if (it.sessaoId != sessionId || it.texto.isBlank() || kemiResponseTime(it.respondidoEm) == null)
                throw KemiContractException("O Kemi não retornou uma mensagem válida para esta conversa.")
        }
    }
}
